package com.example.notesapp.ui.editor.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.summary.NoteSummaryResult
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.mergeAdjacentWithSameMarks
import com.example.notesapp.ui.editor.mapper.newBlockId
import com.example.notesapp.ui.editor.mapper.parseInlineMarkdown
import com.example.notesapp.ui.editor.mapper.parseMarkdownTextBlock
import com.example.notesapp.ui.editor.mapper.splitAtOffsets
import com.example.notesapp.ui.editor.mapper.text
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val document: NoteDocument = NoteDocument.empty(),
    val folderId: String? = null,
    val availableFolders: List<Folder> = emptyList(),
    val createdAt: Long = 0L,
    val isLoaded: Boolean = false,
    val isFormattingToolbarVisible: Boolean = false,
    val focusedBlockId: String? = null,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val isFavorite: Boolean = false,
    val isEditable: Boolean = true,
    val summaryState: NoteSummaryUiState = NoteSummaryUiState.Idle,
    val isCategorizing: Boolean = false,
    val isBackSyncing: Boolean = false,
    val recommendedFolder: Folder? = null,
    val showCategorizationDialog: Boolean = false,
    val showCategorizationNoMatchDialog: Boolean = false
) {
    val content: String
        get() = document.toPlainText()
}

sealed interface NoteSummaryUiState {
    data object Idle : NoteSummaryUiState
    data object Loading : NoteSummaryUiState
    data object Empty : NoteSummaryUiState
    data class Content(val text: String) : NoteSummaryUiState
    data object Error : NoteSummaryUiState
}

@HiltViewModel
open class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val summarizeNoteUseCase: SummarizeNoteUseCase,
    private val categorizeNoteUseCase: CategorizeNoteUseCase,
    internal val deleteVoiceNoteAudioUseCase: DeleteVoiceNoteAudioUseCase,
    private val deleteVoiceNoteBlockUseCase: DeleteVoiceNoteBlockUseCase
) : ViewModel() {
    internal val uiStateInternal = MutableStateFlow(NoteEditorUiState())
    open val uiState: StateFlow<NoteEditorUiState> = uiStateInternal.asStateFlow()
    private fun canEdit(): Boolean = uiStateInternal.value.isEditable
    fun toggleFormattingToolbar() {
        uiStateInternal.value = uiStateInternal.value.copy(
            isFormattingToolbarVisible = !uiStateInternal.value.isFormattingToolbarVisible
        )
    }
    fun setFocusedBlock(blockId: String?) {
        uiStateInternal.value = uiStateInternal.value.copy(focusedBlockId = blockId)
    }
    fun updateSelection(start: Int, end: Int) {
        uiStateInternal.value = uiStateInternal.value.copy(
            selectionStart = start,
            selectionEnd = end
        )
    }

    fun insertEmoji(emoji: String): Boolean {
        if (!canEdit() || emoji.isEmpty()) return false

        val current = uiStateInternal.value
        val focusedTextBlock = current.focusedBlockId
            ?.let { focusedBlockId ->
                current.document.blocks.find { block -> block.id == focusedBlockId } as? EditorBlock.TextBlock
            }

        if (focusedTextBlock == null) {
            val newBlock = EditorBlock.TextBlock(children = listOf(RichText(emoji)))
            uiStateInternal.value = current.copy(
                document = current.document.copy(blocks = current.document.blocks + newBlock),
                focusedBlockId = newBlock.id,
                selectionStart = emoji.length,
                selectionEnd = emoji.length
            )
            scheduleAutoSave()
            return true
        }

        val textLength = focusedTextBlock.text().length
        val (selectionStart, selectionEnd) = current.selectionRangeWithin(textLength)
        val nextSelection = selectionStart + emoji.length
        val updatedBlock = focusedTextBlock.copy(
            children = focusedTextBlock.children.replaceRangeWithEmoji(
                start = selectionStart,
                end = selectionEnd,
                emoji = emoji
            )
        )
        uiStateInternal.value = current.copy(
            document = current.document.copy(
                blocks = current.document.blocks.map { block ->
                    if (block.id == updatedBlock.id) updatedBlock else block
                }
            ),
            selectionStart = nextSelection,
            selectionEnd = nextSelection
        )
        scheduleAutoSave()
        return true
    }

    private var autoSaveJob: Job? = null
    private val autoSaveJobs = mutableSetOf<Job>()
    private var summaryJob: Job? = null
    fun load(noteId: String?, folderId: String? = null) {
        viewModelScope.launch {
            summaryJob?.cancel()
            folderRepository.sync()
            val folders = folderRepository.getFolders().first()
            if (noteId.isNullOrBlank()) {
                uiStateInternal.value = NoteEditorUiState(
                    noteId = "note_${UUID.randomUUID()}",
                    availableFolders = folders,
                    folderId = folderId,
                    isEditable = true,
                    isLoaded = true,
                    summaryState = NoteSummaryUiState.Empty
                )
                return@launch
            }
            val note = noteRepository.getNoteById(noteId)
            val loadedState = if (note != null) {
                NoteEditorUiState(
                    noteId = note.id,
                    title = note.title,
                    document = NoteDocument.fromContent(note.content).ensureEditableTextBlock(),
                    folderId = note.folderId,
                    availableFolders = folders,
                    createdAt = note.createdAt,
                    isFavorite = note.isFavorite,
                    isEditable = note.accessRole != NoteAccessRole.READ_ONLY,
                    isLoaded = true,
                    summaryState = NoteSummaryUiState.Loading
                )
            } else {
                NoteEditorUiState(
                    noteId = "note_${UUID.randomUUID()}",
                    availableFolders = folders,
                    isLoaded = true,
                    summaryState = NoteSummaryUiState.Empty
                )
            }
            uiStateInternal.value = loadedState
            generateSummaryForLoadedNote(loadedState)
        }
    }
    fun onTitleChange(value: String) {
        if (!canEdit()) return
        uiStateInternal.value = uiStateInternal.value.copy(title = value)
        scheduleAutoSave()
    }
    fun rename(newName: String) {
        if (!canEdit()) return
        uiStateInternal.value = uiStateInternal.value.copy(title = newName)
        viewModelScope.launch {
            saveInternally()
        }
    }
    fun toggleFavorite() {
        if (!canEdit()) return
        val current = uiStateInternal.value
        val newFavorite = !current.isFavorite
        uiStateInternal.value = current.copy(isFavorite = newFavorite)
        viewModelScope.launch {
            val note = noteRepository.getNoteById(current.noteId ?: return@launch) ?: return@launch
            noteRepository.save(note.copy(isFavorite = newFavorite, updatedAt = System.currentTimeMillis()))
        }
    }
    fun onTextBlockChange(blockId: String, value: String) {
        if (!canEdit()) return
        if (value.contains('\n')) {
            splitTextBlock(blockId, value)
            return
        }
        updateBlock(blockId) { block ->
            if (block is EditorBlock.TextBlock) {
                val trimmed = value.trimStart()
                val hasPrefix = trimmed.startsWith("- [ ] ") ||
                    trimmed.startsWith("- [x] ") ||
                    trimmed.startsWith("# ") ||
                    trimmed.startsWith("- ")
                if (hasPrefix) {
                    parseMarkdownTextBlock(id = block.id, text = value)
                } else if (block.type != "paragraph") {
                    block.copy(children = parseInlineMarkdown(value))
                } else {
                    parseMarkdownTextBlock(id = block.id, text = value)
                }
            } else {
                block
            }
        }
    }
    fun toggleCheckbox(blockId: String) {
        if (!canEdit()) return
        updateBlock(blockId) { block ->
            if (block is EditorBlock.TextBlock) {
                if (block.type == "checkbox") {
                    block.copy(type = "paragraph", checked = false)
                } else {
                    block.copy(type = "checkbox", checked = false)
                }
            } else {
                block
            }
        }
    }
    fun toggleCheckboxChecked(blockId: String) {
        if (!canEdit()) return
        updateBlock(blockId) { block ->
            if (block is EditorBlock.TextBlock && block.type == "checkbox") {
                block.copy(checked = !block.checked)
            } else {
                block
            }
        }
    }
    fun toggleBlockMark(blockId: String, mark: String) {
        if (!canEdit()) return
        val state = uiStateInternal.value
        val start = state.selectionStart
        val end = state.selectionEnd
        updateBlock(blockId) { block ->
            if (block !is EditorBlock.TextBlock) return@updateBlock block
            val text = block.text()
            if (start == end || start < 0 || end > text.length) {
                // No selection: only selected text should be edited, so this is a no-op.
                return@updateBlock block
            }
            // If there's a selection, modify children directly instead of using raw markers in text
            val splitChildren = block.children.splitAtOffsets(listOf(start, end))

            var currentOffset = 0
            val childrenWithOffsets = splitChildren.map { child ->
                val childStart = currentOffset
                val childEnd = currentOffset + child.text.length
                currentOffset = childEnd
                Triple(child, childStart, childEnd)
            }

            val selectionChildren = childrenWithOffsets.filter { (_, childStart, childEnd) ->
                childStart >= start && childEnd <= end
            }

            val hasMark = selectionChildren.any { (child, _, _) -> mark in child.marks }

            val updatedChildren = childrenWithOffsets.map { (child, childStart, childEnd) ->
                if (childStart >= start && childEnd <= end) {
                    val marks = if (hasMark) child.marks - mark else (child.marks + mark).distinct()
                    child.copy(marks = marks)
                } else {
                    child
                }
            }

            block.copy(
                children = updatedChildren.mergeAdjacentWithSameMarks()
            )
        }
    }
    fun addParagraphBlock() {
        if (!canEdit()) return
        appendBlock(EditorBlock.TextBlock(children = listOf(RichText(""))))
    }
    fun addImageBlock() {
        if (!canEdit()) return
        appendBlock(EditorBlock.ImageBlock())
    }
    fun updateImageBlock(blockId: String, url: String? = null, caption: String? = null) {
        if (!canEdit()) return
        updateBlock(blockId) { block ->
            if (block is EditorBlock.ImageBlock) {
                block.copy(
                    url = url ?: block.url,
                    caption = caption ?: block.caption
                )
            } else {
                block
            }
        }
    }
    fun addTableBlock() {
        if (!canEdit()) return
        appendBlock(EditorBlock.TableBlock())
    }
    fun updateTableCell(blockId: String, rowIndex: Int, cellIndex: Int, value: String) {
        if (!canEdit()) return
        updateBlock(blockId) { block ->
            if (block !is EditorBlock.TableBlock) return@updateBlock block
            block.copy(
                rows = block.rows.mapIndexed { rowPosition, row ->
                    if (rowPosition != rowIndex) {
                        row
                    } else {
                        row.mapIndexed { cellPosition, cell ->
                            if (cellPosition == cellIndex) listOf(RichText(value.replace("\n", " "))) else cell
                        }
                    }
                }
            )
        }
    }

    fun onFolderSelected(folderId: String?) {
        if (!canEdit()) return
        uiStateInternal.value = uiStateInternal.value.copy(folderId = folderId)
        scheduleAutoSave()
    }
    internal fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        val job = viewModelScope.launch {
            delay(2000)
            saveInternally()
        }
        autoSaveJob = job
        autoSaveJobs += job
        job.invokeOnCompletion {
            autoSaveJobs -= job
            if (autoSaveJob == job) {
                autoSaveJob = null
            }
        }
    }
    private suspend fun saveInternally() {
        val current = uiStateInternal.value
        if (!current.isEditable && current.createdAt != 0L) return
        // Don't auto-save if both title and content are empty
        if (current.title.isBlank() && current.content.isBlank()) return
        val now = System.currentTimeMillis()
        val noteId = current.noteId ?: "note_${UUID.randomUUID()}"
        val note = Note(
            id = noteId,
            title = current.title.ifBlank { "Untitled note" },
            content = current.document.toJsonString(),
            folderId = current.folderId,
            sortKey = now.toString(),
            deviceId = "",
            createdAt = if (current.createdAt == 0L) now else current.createdAt,
            updatedAt = now,
            isFavorite = current.isFavorite,
            accessRole = if (current.isEditable) NoteAccessRole.FULL_ACCESS else NoteAccessRole.READ_ONLY
        )
        noteRepository.save(note)
        // Update state with generated ID and createdAt to avoid duplicate creations
        uiStateInternal.value = uiStateInternal.value.copy(
            noteId = noteId,
            createdAt = note.createdAt
        )
    }
    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            autoSaveJobs.cancelAndJoinAll()
            autoSaveJob = null
            saveInternally()
            onDone()
        }
    }
    fun handleBackPress(onNavigateBack: () -> Unit) {
        if (
            uiStateInternal.value.isBackSyncing ||
            uiStateInternal.value.isCategorizing ||
            uiStateInternal.value.showCategorizationNoMatchDialog
        ) {
            return
        }
        viewModelScope.launch {
            val initial = uiStateInternal.value
            if (initial.title.isNotBlank() || initial.content.isNotBlank()) {
                uiStateInternal.value = initial.copy(isBackSyncing = true)
            }
            autoSaveJobs.cancelAndJoinAll()
            autoSaveJob = null
            val current = uiStateInternal.value
            suspend fun saveBeforeNavigatingBack() {
                val latest = uiStateInternal.value
                if (latest.title.isBlank() && latest.content.isBlank()) {
                    onNavigateBack()
                    return
                }
                uiStateInternal.value = latest.copy(isBackSyncing = true)
                try {
                    saveInternally()
                    uiStateInternal.value = uiStateInternal.value.copy(isBackSyncing = false)
                    onNavigateBack()
                } finally {
                    if (uiStateInternal.value.isBackSyncing) {
                        uiStateInternal.value = uiStateInternal.value.copy(isBackSyncing = false)
                    }
                }
            }

            val isEligible = current.folderId == null &&
                (current.title.isNotBlank() || current.content.isNotBlank()) &&
                current.availableFolders.isNotEmpty() &&
                current.isEditable

            if (!isEligible) {
                saveBeforeNavigatingBack()
                return@launch
            }

            uiStateInternal.value = current.copy(isCategorizing = true, isBackSyncing = false)
            try {
                val recommendation = categorizeNoteUseCase(
                    title = current.title,
                    content = current.content,
                    folders = current.availableFolders
                )
                if (recommendation != null) {
                    uiStateInternal.value = uiStateInternal.value.copy(
                        isCategorizing = false,
                        recommendedFolder = recommendation,
                        showCategorizationDialog = true,
                        showCategorizationNoMatchDialog = false
                    )
                } else {
                    uiStateInternal.value = uiStateInternal.value.copy(
                        isCategorizing = false,
                        recommendedFolder = null,
                        showCategorizationDialog = false,
                        showCategorizationNoMatchDialog = true
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Smart categorization failed", e)
                uiStateInternal.value = uiStateInternal.value.copy(isCategorizing = false)
                saveBeforeNavigatingBack()
            }
        }
    }
    fun confirmCategorization(onNavigateBack: () -> Unit, onMoveManually: ((String) -> Unit)? = null) {
        val current = uiStateInternal.value
        val recommendedFolderId = current.recommendedFolder?.id
        uiStateInternal.value = current.copy(
            isCategorizing = true,
            showCategorizationDialog = false,
            showCategorizationNoMatchDialog = false,
            folderId = recommendedFolderId
        )
        viewModelScope.launch {
            try {
                saveInternally()
                val savedNoteId = uiStateInternal.value.noteId
                if (recommendedFolderId == null && onMoveManually != null && !savedNoteId.isNullOrBlank()) {
                    onMoveManually(savedNoteId)
                    return@launch
                }
                onNavigateBack()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save note during categorization", e)
            } finally {
                uiStateInternal.value = uiStateInternal.value.copy(isCategorizing = false)
            }
        }
    }
    fun cancelCategorization(onNavigateBack: () -> Unit) {
        uiStateInternal.value = uiStateInternal.value.copy(
            showCategorizationDialog = false,
            showCategorizationNoMatchDialog = false
        )
        viewModelScope.launch {
            saveInternally()
            onNavigateBack()
        }
    }
    fun shareCurrentNote(onReady: (String) -> Unit) {
        viewModelScope.launch {
            autoSaveJobs.cancelAndJoinAll()
            autoSaveJob = null
            saveInternally()
            uiStateInternal.value.noteId?.let(onReady)
        }
    }
    fun delete(onDone: () -> Unit) {
        val current = uiStateInternal.value
        if (!current.isEditable) {
            onDone()
            return
        }
        // If not saved yet, just finish
        if (current.createdAt == 0L) {
            onDone()
            return
        }
        viewModelScope.launch {
            noteRepository.delete(
                Note(
                    id = current.noteId.orEmpty(),
                    title = current.title,
                    content = current.document.toJsonString(),
                    folderId = current.folderId,
                    sortKey = "",
                    version = 0,
                    deviceId = "",
                    createdAt = current.createdAt,
                    updatedAt = System.currentTimeMillis(),
                    accessRole = if (current.isEditable) NoteAccessRole.FULL_ACCESS else NoteAccessRole.READ_ONLY
                )
            )
            onDone()
        }
    }

    fun deleteBlock(blockId: String) {
        val current = uiStateInternal.value
        val blocks = current.document.blocks
        if (blocks.size <= 1 && blocks.firstOrNull() !is EditorBlock.Voice) return
        val index = blocks.indexOfFirst { it.id == blockId }
        if (index == -1) return
        val precedingVoiceBlock = blocks.getOrNull(index - 1) as? EditorBlock.Voice
        val directVoiceBlock = blocks[index] as? EditorBlock.Voice
        val idsToDelete = if (precedingVoiceBlock != null && blocks[index] is EditorBlock.TextBlock) {
            setOf(blockId, precedingVoiceBlock.id)
        } else {
            setOf(blockId)
        }
        val updatedBlocks = blocks.filterNot { it.id in idsToDelete }.ifEmpty {
            listOf(EditorBlock.TextBlock())
        }
        val nextFocusId = if (index > 0) {
            updatedBlocks.getOrNull((index - 1).coerceAtMost(updatedBlocks.lastIndex))?.id
        } else {
            updatedBlocks.firstOrNull()?.id
        }
        uiStateInternal.value = current.copy(
            document = current.document.copy(blocks = updatedBlocks),
            focusedBlockId = nextFocusId
        )
        (directVoiceBlock ?: precedingVoiceBlock)?.let { voiceBlock ->
            viewModelScope.launch { deleteVoiceNoteBlockUseCase(voiceBlock.blockId) }
        }
        scheduleAutoSave()
    }

    private fun generateSummaryForLoadedNote(state: NoteEditorUiState) {
        summaryJob?.cancel()
        if (state.noteId.isNullOrBlank()) {
            uiStateInternal.value = state.copy(summaryState = NoteSummaryUiState.Empty)
            return
        }
        val noteText = state.content
        summaryJob = viewModelScope.launch {
            val summaryState = when (val result = summarizeNoteUseCase(state.title, noteText)) {
                is NoteSummaryResult.Success -> NoteSummaryUiState.Content(result.summary.text)
                NoteSummaryResult.Empty -> NoteSummaryUiState.Empty
                NoteSummaryResult.Unavailable -> NoteSummaryUiState.Error
            }
            uiStateInternal.value = uiStateInternal.value.copy(summaryState = summaryState)
        }
    }
}

fun NoteEditorViewModel.insertTableColumnLeft(blockId: String, columnIndex: Int) {
    mutateTableBlock(blockId) { block ->
        if (columnIndex !in 0 until block.columnCount()) return@mutateTableBlock null
        block.copy(
            rows = block.rows.map { row ->
                row.withInsertedCell(columnIndex)
            }
        )
    }
}

fun NoteEditorViewModel.insertTableColumnRight(blockId: String, columnIndex: Int) {
    mutateTableBlock(blockId) { block ->
        if (columnIndex !in 0 until block.columnCount()) return@mutateTableBlock null
        block.copy(
            rows = block.rows.map { row ->
                row.withInsertedCell(columnIndex + 1)
            }
        )
    }
}

fun NoteEditorViewModel.deleteTableColumn(blockId: String, columnIndex: Int) {
    if (!tableCanEdit()) return
    val table = currentTableBlock(blockId) ?: return
    if (columnIndex !in 0 until table.columnCount()) return
    if (table.columnCount() == 1) {
        removeTableBlock(blockId)
        return
    }
    mutateTableBlock(blockId) { block ->
        block.copy(
            rows = block.rows.map { row ->
                row.toMutableList().apply {
                    if (columnIndex < size) removeAt(columnIndex)
                }
            }
        )
    }
}

fun NoteEditorViewModel.clearTableColumn(blockId: String, columnIndex: Int) {
    mutateTableBlock(blockId) { block ->
        if (columnIndex !in 0 until block.columnCount()) return@mutateTableBlock null
        block.copy(
            rows = block.rows.map { row ->
                row.mapIndexed { cellIndex, cell ->
                    if (cellIndex == columnIndex) emptyTableCell() else cell
                }
            }
        )
    }
}

fun NoteEditorViewModel.insertTableRowAbove(blockId: String, rowIndex: Int) {
    mutateTableBlock(blockId) { block ->
        if (rowIndex !in block.rows.indices) return@mutateTableBlock null
        block.copy(
            rows = block.rows.toMutableList().apply {
                add(rowIndex, emptyTableRow(block.columnCount()))
            }
        )
    }
}

fun NoteEditorViewModel.insertTableRowBelow(blockId: String, rowIndex: Int) {
    mutateTableBlock(blockId) { block ->
        if (rowIndex !in block.rows.indices) return@mutateTableBlock null
        block.copy(
            rows = block.rows.toMutableList().apply {
                add(rowIndex + 1, emptyTableRow(block.columnCount()))
            }
        )
    }
}

fun NoteEditorViewModel.deleteTableRow(blockId: String, rowIndex: Int) {
    if (!tableCanEdit()) return
    val table = currentTableBlock(blockId) ?: return
    if (rowIndex !in table.rows.indices) return
    if (table.rows.size == 1) {
        removeTableBlock(blockId)
        return
    }
    mutateTableBlock(blockId) { block ->
        block.copy(rows = block.rows.filterIndexed { index, _ -> index != rowIndex })
    }
}

fun NoteEditorViewModel.clearTableRow(blockId: String, rowIndex: Int) {
    mutateTableBlock(blockId) { block ->
        if (rowIndex !in block.rows.indices) return@mutateTableBlock null
        block.copy(
            rows = block.rows.mapIndexed { index, row ->
                if (index == rowIndex) row.map { emptyTableCell() } else row
            }
        )
    }
}

fun NoteEditorViewModel.clearTable(blockId: String) {
    mutateTableBlock(blockId) { block ->
        block.copy(rows = block.rows.map { row -> row.map { emptyTableCell() } })
    }
}

fun NoteEditorViewModel.duplicateTable(blockId: String) {
    if (!tableCanEdit()) return
    val current = uiStateInternal.value
    val index = current.document.blocks.indexOfFirst { it.id == blockId }
    val table = current.document.blocks.getOrNull(index) as? EditorBlock.TableBlock ?: return
    val duplicatedTable = table.deepCopy()
    val blocks = current.document.blocks.toMutableList().apply {
        add(index + 1, duplicatedTable)
    }
    commitTableDocument(current.document.copy(blocks = blocks))
}

fun NoteEditorViewModel.deleteTable(blockId: String) {
    if (currentTableBlock(blockId) == null) return
    removeTableBlock(blockId)
}

fun NoteEditorViewModel.toggleTableFitToWidth(blockId: String) {
    mutateTableBlock(blockId) { block ->
        block.copy(fitToWidth = !block.fitToWidth)
    }
}

private fun NoteEditorViewModel.tableCanEdit(): Boolean = uiStateInternal.value.isEditable

private fun NoteEditorViewModel.currentTableBlock(blockId: String): EditorBlock.TableBlock? =
    uiStateInternal.value.document.blocks.firstOrNull { it.id == blockId } as? EditorBlock.TableBlock

private fun NoteEditorViewModel.mutateTableBlock(
    blockId: String,
    transform: (EditorBlock.TableBlock) -> EditorBlock.TableBlock?
) {
    if (!tableCanEdit()) return
    val current = uiStateInternal.value
    val index = current.document.blocks.indexOfFirst { it.id == blockId }
    val table = current.document.blocks.getOrNull(index) as? EditorBlock.TableBlock ?: return
    val updatedTable = transform(table) ?: return
    if (updatedTable == table) return
    val blocks = current.document.blocks.toMutableList().apply {
        this[index] = updatedTable
    }
    commitTableDocument(current.document.copy(blocks = blocks))
}

private fun NoteEditorViewModel.removeTableBlock(blockId: String) {
    if (!tableCanEdit()) return
    val current = uiStateInternal.value
    val index = current.document.blocks.indexOfFirst { it.id == blockId }
    if (index < 0 || current.document.blocks[index] !is EditorBlock.TableBlock) return
    val remainingBlocks = current.document.blocks.filterNot { it.id == blockId }
        .ifEmpty { listOf(EditorBlock.TextBlock()) }
    val nextFocusedBlockId = if (current.focusedBlockId == blockId) {
        remainingBlocks[index.coerceAtMost(remainingBlocks.lastIndex)].id
    } else {
        current.focusedBlockId
    }
    commitTableDocument(
        document = current.document.copy(blocks = remainingBlocks),
        focusedBlockId = nextFocusedBlockId
    )
}

private fun NoteEditorViewModel.commitTableDocument(
    document: NoteDocument,
    focusedBlockId: String? = uiStateInternal.value.focusedBlockId
) {
    val current = uiStateInternal.value
    if (document == current.document && focusedBlockId == current.focusedBlockId) return
    uiStateInternal.value = current.copy(
        document = document,
        focusedBlockId = focusedBlockId
    )
    scheduleAutoSave()
}

private fun EditorBlock.TableBlock.columnCount(): Int = rows.maxOfOrNull { it.size } ?: 0

private fun List<List<RichText>>.withInsertedCell(index: Int): List<List<RichText>> {
    val updatedRow = toMutableList()
    while (updatedRow.size < index) {
        updatedRow += emptyTableCell()
    }
    updatedRow.add(index, emptyTableCell())
    return updatedRow
}

private fun emptyTableCell(): List<RichText> = listOf(RichText(""))

private fun emptyTableRow(columnCount: Int): List<List<RichText>> = List(columnCount) { emptyTableCell() }

private fun EditorBlock.TableBlock.deepCopy(): EditorBlock.TableBlock = copy(
    id = newBlockId(),
    rows = rows.map { row ->
        row.map { cell ->
            cell.map { richText -> richText.copy(marks = richText.marks.toList()) }
        }
    }
)

private const val TAG = "NotesApp/NoteEditorViewModel"

private fun NoteEditorViewModel.appendBlock(block: EditorBlock) {
    val current = uiStateInternal.value
    uiStateInternal.value = current.copy(
        document = current.document.copy(blocks = current.document.blocks + block)
    )
    scheduleAutoSave()
}

private fun NoteEditorViewModel.updateBlock(blockId: String, transform: (EditorBlock) -> EditorBlock) {
    val current = uiStateInternal.value
    uiStateInternal.value = current.copy(
        document = current.document.copy(
            blocks = current.document.blocks.map { block ->
                if (block.id == blockId) transform(block) else block
            }
        )
    )
    scheduleAutoSave()
}

private fun NoteEditorViewModel.splitTextBlock(blockId: String, value: String) {
    val current = uiStateInternal.value
    val lines = value.split('\n')
    var nextFocusId: String? = null
    val updatedBlocks = current.document.blocks.flatMap { block ->
        if (block.id == blockId && block is EditorBlock.TextBlock) {
            val isCheckbox = block.type == "checkbox"
            val isEmptyCheckbox = isCheckbox && block.text().trim().isEmpty()
            val newBlocks = lines.mapIndexed { index, line ->
                val id = if (index == 0) block.id else newBlockId()
                val type = if (isCheckbox) {
                    if (isEmptyCheckbox) "paragraph" else "checkbox"
                } else {
                    "paragraph"
                }
                val checked = if (isCheckbox && !isEmptyCheckbox) {
                    if (index == 0) block.checked else false
                } else {
                    false
                }
                EditorBlock.TextBlock(
                    id = id,
                    type = type,
                    children = parseInlineMarkdown(line),
                    checked = checked
                )
            }
            nextFocusId = newBlocks.lastOrNull()?.id
            newBlocks
        } else {
            listOf(block)
        }
    }
    uiStateInternal.value = current.copy(
        document = current.document.copy(blocks = updatedBlocks),
        focusedBlockId = nextFocusId ?: current.focusedBlockId
    )
    scheduleAutoSave()
}

private suspend fun MutableSet<Job>.cancelAndJoinAll() {
    toList().forEach { job ->
        job.cancelAndJoin()
    }
}

private fun NoteEditorUiState.selectionRangeWithin(textLength: Int): Pair<Int, Int> {
    val start = selectionStart
    val end = selectionEnd
    return if (start in 0..textLength && end in 0..textLength) {
        minOf(start, end) to maxOf(start, end)
    } else {
        textLength to textLength
    }
}

private fun List<RichText>.replaceRangeWithEmoji(start: Int, end: Int, emoji: String): List<RichText> {
    if (isEmpty()) return listOf(RichText(emoji))

    val updatedChildren = mutableListOf<RichText>()
    var currentOffset = 0
    var emojiInserted = false

    forEach { child ->
        val childStart = currentOffset
        val childEnd = childStart + child.text.length
        currentOffset = childEnd

        when {
            childEnd <= start -> updatedChildren += child
            childStart >= end -> {
                if (!emojiInserted) {
                    updatedChildren += RichText(emoji)
                    emojiInserted = true
                }
                updatedChildren += child
            }

            else -> {
                val startOffset = (start - childStart).coerceIn(0, child.text.length)
                val endOffset = (end - childStart).coerceIn(0, child.text.length)
                if (startOffset > 0) {
                    updatedChildren += RichText(child.text.substring(0, startOffset), child.marks)
                }
                if (!emojiInserted) {
                    updatedChildren += RichText(emoji)
                    emojiInserted = true
                }
                if (endOffset < child.text.length) {
                    updatedChildren += RichText(child.text.substring(endOffset), child.marks)
                }
            }
        }
    }

    if (!emojiInserted) {
        updatedChildren += RichText(emoji)
    }
    return updatedChildren.mergeAdjacentWithSameMarks()
}
