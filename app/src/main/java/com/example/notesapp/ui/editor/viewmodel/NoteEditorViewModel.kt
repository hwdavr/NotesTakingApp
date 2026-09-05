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
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.applyTextDiff
import com.example.notesapp.ui.editor.mapper.basicBlockType
import com.example.notesapp.ui.editor.mapper.createEmptyTextBlock
import com.example.notesapp.ui.editor.mapper.marksAtOffset
import com.example.notesapp.ui.editor.mapper.mergeAdjacentWithSameMarks
import com.example.notesapp.ui.editor.mapper.newBlockId
import com.example.notesapp.ui.editor.mapper.normalized
import com.example.notesapp.ui.editor.mapper.parseMarkdownTextBlock
import com.example.notesapp.ui.editor.mapper.splitAtOffsets
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.mapper.toChartBlock
import com.example.notesapp.ui.editor.model.ChartBlockCardModel
import com.example.notesapp.ui.editor.model.TableFocusTarget
import com.example.notesapp.ui.editor.model.TableHandleAction
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
    val showBasicBlocksPanel: Boolean = false,
    val focusedBlockId: String? = null,
    val focusedTableCells: Map<String, TableFocusTarget> = emptyMap(),
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val isFavorite: Boolean = false,
    val isEditable: Boolean = true,
    val summaryState: NoteSummaryUiState = NoteSummaryUiState.Idle,
    val isCategorizing: Boolean = false,
    val isBackSyncing: Boolean = false,
    val recommendedFolder: Folder? = null,
    val showCategorizationDialog: Boolean = false,
    val showCategorizationNoMatchDialog: Boolean = false,
    val formulaSheet: FormulaSheetUiState? = null,
    val pendingTypingMarks: Set<String> = emptySet()
) {
    val content: String
        get() = document.toPlainText()

    val chartCardModels: Map<String, ChartBlockCardModel>
        get() = document.blocks.filterIsInstance<EditorBlock.ChartBlock>()
            .associate { block -> block.id to ChartBlockCardModel.from(block) }
}

data class FormulaSheetUiState(
    val source: String = "",
    val editingBlockId: String? = null,
    val editingInlineId: String? = null,
    val hasValidationError: Boolean = false
) {
    val isEditing: Boolean
        get() = editingBlockId != null && editingInlineId != null
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

    fun insertEmoji(emoji: String): Boolean {
        if (!uiStateInternal.value.isEditable || emoji.isEmpty()) return false

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

    init {
        observeLinkTargetChanges()
    }

    private fun observeLinkTargetChanges() {
        viewModelScope.launch {
            try {
                combine(
                    noteRepository.getActiveNotes(),
                    noteRepository.getArchivedNotes()
                ) { active, archived ->
                    val activeIds = active.map { it.id }.toSet()
                    val deletedIds = archived.map { it.id }.toSet()
                    Pair(activeIds, deletedIds)
                }.collect { (activeIds, deletedIds) ->
                    val current = uiStateInternal.value
                    if (current.isLoaded && current.document.hasLinkAnnotations()) {
                        val resolved = current.document.resolveLinks(activeIds, deletedIds)
                        if (resolved != current.document) {
                            uiStateInternal.value = current.copy(document = resolved)
                            scheduleAutoSave()
                        }
                    }
                }
            } catch (_: Throwable) {
                // Ignore if repository flows are unmocked or empty
            }
        }
    }

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
            val activeNotes = runCatching { noteRepository.getActiveNotes().firstOrNull() }.getOrNull().orEmpty()
            val archivedNotes = runCatching { noteRepository.getArchivedNotes().firstOrNull() }.getOrNull().orEmpty()
            val activeIds = activeNotes.map { it.id }.toSet()
            val deletedIds = archivedNotes.map { it.id }.toSet()
            val note = noteRepository.getNoteById(noteId)
            val loadedState = if (note != null) {
                val resolvedDoc = NoteDocument.fromContent(note.content)
                    .resolveLinks(activeIds, deletedIds)
                    .ensureEditableTextBlock()
                NoteEditorUiState(
                    noteId = note.id,
                    title = note.title,
                    document = resolvedDoc,
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
        if (!uiStateInternal.value.isEditable) return
        uiStateInternal.value = uiStateInternal.value.copy(title = value)
        scheduleAutoSave()
    }
    fun rename(newName: String) {
        if (!uiStateInternal.value.isEditable) return
        uiStateInternal.value = uiStateInternal.value.copy(title = newName)
        viewModelScope.launch {
            saveInternally()
        }
    }
    fun toggleFavorite() {
        if (!uiStateInternal.value.isEditable) return
        val current = uiStateInternal.value
        val newFavorite = !current.isFavorite
        uiStateInternal.value = current.copy(isFavorite = newFavorite)
        viewModelScope.launch {
            val note = noteRepository.getNoteById(current.noteId ?: return@launch) ?: return@launch
            noteRepository.save(note.copy(isFavorite = newFavorite, updatedAt = System.currentTimeMillis()))
        }
    }
    fun onTextBlockChange(blockId: String, value: String) {
        if (!uiStateInternal.value.isEditable) return
        if (value.contains('\n')) {
            splitTextBlock(blockId, value)
            return
        }
        val state = uiStateInternal.value
        updateBlock(blockId) { block ->
            if (block is EditorBlock.TextBlock) {
                val trimmed = value.trimStart()
                val hasPrefix = trimmed.startsWith("- [ ] ") ||
                    trimmed.startsWith("- [x] ") ||
                    trimmed.startsWith("# ") ||
                    trimmed.startsWith("- ")
                val updated = if (hasPrefix) {
                    parseMarkdownTextBlock(id = block.id, text = value)
                } else {
                    val newChildren = block.children.applyTextDiff(
                        newText = value,
                        pendingMarks = state.pendingTypingMarks
                    )
                    block.copy(children = newChildren)
                }
                if (block.children.any { it.isFormula }) {
                    updated.copy(
                        children = updated.children.replaceFormulaPlaceholders(
                            formulas = block.children.filter { it.isFormula }
                        ).children
                    )
                } else {
                    updated
                }
            } else {
                block
            }
        }
    }
    fun toggleCheckbox(blockId: String) {
        if (!uiStateInternal.value.isEditable) return
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
        if (!uiStateInternal.value.isEditable) return
        updateBlock(blockId) { block ->
            if (block is EditorBlock.TextBlock && block.type == "checkbox") {
                block.copy(checked = !block.checked)
            } else {
                block
            }
        }
    }
    fun toggleBlockMark(blockId: String, mark: String) {
        val state = uiStateInternal.value
        val focusedId = state.focusedBlockId
        if (!state.isEditable || (focusedId != null && focusedId != blockId)) return
        val block = state.document.blocks.find { it.id == blockId } as? EditorBlock.TextBlock ?: return
        val text = block.text()
        val start = state.selectionStart
        val end = state.selectionEnd
        if (start == end) {
            val currentPending = state.pendingTypingMarks
            val newPending = if (mark in currentPending) {
                currentPending - mark
            } else {
                currentPending + mark
            }
            uiStateInternal.value = state.copy(pendingTypingMarks = newPending)
        } else {
            val selStart = minOf(start, end).coerceIn(0, text.length)
            val selEnd = maxOf(start, end).coerceIn(0, text.length)
            if (selStart < selEnd) {
                updateBlock(blockId) { b ->
                    if (b !is EditorBlock.TextBlock) return@updateBlock b
                    val splitChildren = b.children.splitAtOffsets(listOf(selStart, selEnd))

                    var currentOffset = 0
                    val childrenWithOffsets = splitChildren.map { child ->
                        val childStart = currentOffset
                        val childEnd = currentOffset + child.text.length
                        currentOffset = childEnd
                        Triple(child, childStart, childEnd)
                    }

                    val selectionChildren = childrenWithOffsets.filter { (_, childStart, childEnd) ->
                        childStart >= selStart && childEnd <= selEnd
                    }

                    val hasMark = selectionChildren.any { (child, _, _) -> mark in child.marks }

                    val updatedChildren = childrenWithOffsets.map { (child, childStart, childEnd) ->
                        if (childStart >= selStart && childEnd <= selEnd) {
                            val marks = if (hasMark) child.marks - mark else (child.marks + mark).distinct()
                            child.copy(marks = marks)
                        } else {
                            child
                        }
                    }

                    b.copy(
                        children = updatedChildren.mergeAdjacentWithSameMarks()
                    )
                }
            }
        }
    }

    fun insertBasicBlock(type: BasicBlockType): Boolean {
        if (!uiStateInternal.value.isEditable) return false

        val current = uiStateInternal.value
        val newBlock: EditorBlock = when (type) {
            BasicBlockType.MERMAID -> EditorBlock.MermaidBlock()
            BasicBlockType.CODE -> EditorBlock.CodeBlock()
            BasicBlockType.BAR_CHART -> EditorBlock.ChartBlock(chartType = ChartType.BAR)
            BasicBlockType.LINE_CHART -> EditorBlock.ChartBlock(chartType = ChartType.LINE)
            BasicBlockType.PIE_CHART -> EditorBlock.ChartBlock(chartType = ChartType.PIE)
            else -> type.createEmptyTextBlock()
        }
        val focusedIndex = current.focusedBlockId?.let { focusedId ->
            current.document.blocks.indexOfFirst { it.id == focusedId }.takeIf { it >= 0 }
        }

        val updatedBlocks = current.document.blocks.toMutableList().apply {
            if (focusedIndex != null) {
                add(focusedIndex + 1, newBlock)
            } else {
                add(newBlock)
            }
        }

        uiStateInternal.value = current.copy(
            document = current.document.copy(blocks = updatedBlocks),
            focusedBlockId = newBlock.id,
            showBasicBlocksPanel = false,
            selectionStart = 0,
            selectionEnd = 0
        )
        scheduleAutoSave()
        return true
    }

    fun toggleToggleExpanded(blockId: String): Boolean {
        if (!uiStateInternal.value.isEditable) return false

        val current = uiStateInternal.value
        val blockIndex = current.document.blocks.indexOfFirst { it.id == blockId }
        val toggleBlock = current.document.blocks.getOrNull(blockIndex) as? EditorBlock.TextBlock
            ?: return false
        if (toggleBlock.basicBlockType() != BasicBlockType.TOGGLE_LIST) return false

        val updatedBlocks = current.document.blocks.toMutableList().apply {
            this[blockIndex] = toggleBlock.copy(isExpanded = !toggleBlock.isExpanded)
        }
        uiStateInternal.value = current.copy(document = current.document.copy(blocks = updatedBlocks))
        scheduleAutoSave()
        return true
    }

    fun updateImageBlock(blockId: String, url: String? = null, caption: String? = null) {
        if (!uiStateInternal.value.isEditable) return
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
    fun updateTableCell(blockId: String, rowIndex: Int, cellIndex: Int, value: String) {
        if (!uiStateInternal.value.isEditable) return
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

    fun updateChart(blockId: String, title: String? = null, selectedColumnId: String? = null) {
        if (!uiStateInternal.value.isEditable) return
        updateBlock(blockId) { block ->
            if (block !is EditorBlock.ChartBlock) return@updateBlock block
            val normalizedBlock = block.normalized()
            val nextSelectedColumnId = selectedColumnId
                ?.takeIf { it in normalizedBlock.columnIds.drop(1) }
                ?: normalizedBlock.selectedColumnId
            normalizedBlock.copy(
                title = title ?: block.title,
                selectedColumnId = nextSelectedColumnId
            )
        }
    }

    fun updateChartCell(blockId: String, rowIndex: Int, columnIndex: Int, value: String) {
        if (!uiStateInternal.value.isEditable) return
        updateBlock(blockId) { block ->
            if (block !is EditorBlock.ChartBlock) return@updateBlock block
            val normalizedBlock = block.normalized()
            if (rowIndex !in normalizedBlock.rows.indices || columnIndex !in normalizedBlock.columnIds.indices) {
                return@updateBlock normalizedBlock
            }
            normalizedBlock.copy(
                rows = normalizedBlock.rows.mapIndexed { currentRowIndex, row ->
                    if (currentRowIndex != rowIndex) {
                        row
                    } else {
                        row.mapIndexed { currentColumnIndex, cell ->
                            if (currentColumnIndex == columnIndex) {
                                listOf(RichText(value.replace("\n", " ")))
                            } else {
                                cell
                            }
                        }
                    }
                }
            )
        }
    }

    fun convertTableToChart(blockId: String, chartType: ChartType): Boolean {
        if (!uiStateInternal.value.isEditable) return false
        val current = uiStateInternal.value
        val index = current.document.blocks.indexOfFirst { it.id == blockId }
        val table = current.document.blocks.getOrNull(index) as? EditorBlock.TableBlock ?: return false
        val chart = table.toChartBlock(chartType)
        val updatedBlocks = current.document.blocks.toMutableList().apply { this[index] = chart }
        uiStateInternal.value = current.copy(
            document = current.document.copy(blocks = updatedBlocks),
            focusedBlockId = chart.id,
            focusedTableCells = current.focusedTableCells - blockId
        )
        scheduleAutoSave()
        return true
    }

    fun onFolderSelected(folderId: String?) {
        if (!uiStateInternal.value.isEditable) return
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

fun NoteEditorViewModel.onTableAction(action: TableHandleAction) {
    when (action) {
        is TableHandleAction.FocusCell -> if (tableCanEdit()) {
            val table = currentTableBlock(action.blockId)
            if (table != null &&
                action.rowIndex in table.rows.indices &&
                action.columnIndex in 0 until table.columnCount()
            ) {
                val current = uiStateInternal.value
                val target = TableFocusTarget(
                    rowIndex = action.rowIndex,
                    columnIndex = action.columnIndex
                )
                if (current.focusedTableCells[action.blockId] != target) {
                    uiStateInternal.value = current.copy(
                        focusedTableCells = current.focusedTableCells + (action.blockId to target)
                    )
                }
            }
        }
        is TableHandleAction.ClearFocus -> {
            val current = uiStateInternal.value
            if (action.blockId in current.focusedTableCells) {
                uiStateInternal.value = current.copy(
                    focusedTableCells = current.focusedTableCells - action.blockId
                )
            }
        }
        is TableHandleAction.InsertColumnLeft -> insertTableColumnLeft(action.blockId, action.columnIndex)
        is TableHandleAction.InsertColumnRight -> insertTableColumnRight(action.blockId, action.columnIndex)
        is TableHandleAction.DeleteColumn -> deleteTableColumn(action.blockId, action.columnIndex)
        is TableHandleAction.ClearColumn -> clearTableColumn(action.blockId, action.columnIndex)
        is TableHandleAction.InsertRowAbove -> insertTableRowAbove(action.blockId, action.rowIndex)
        is TableHandleAction.InsertRowBelow -> insertTableRowBelow(action.blockId, action.rowIndex)
        is TableHandleAction.DeleteRow -> deleteTableRow(action.blockId, action.rowIndex)
        is TableHandleAction.ClearRow -> clearTableRow(action.blockId, action.rowIndex)
        is TableHandleAction.ClearTable -> clearTable(action.blockId)
        is TableHandleAction.DuplicateTable -> duplicateTable(action.blockId)
        is TableHandleAction.DeleteTable -> deleteTable(action.blockId)
        is TableHandleAction.ToggleTableFitToWidth -> toggleTableFitToWidth(action.blockId)
        is TableHandleAction.ConvertToChart -> convertTableToChart(action.blockId, action.chartType)
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
        focusedBlockId = nextFocusedBlockId,
        focusedTableCells = current.focusedTableCells - blockId
    )
}

private fun NoteEditorViewModel.commitTableDocument(
    document: NoteDocument,
    focusedBlockId: String? = uiStateInternal.value.focusedBlockId,
    focusedTableCells: Map<String, TableFocusTarget> = uiStateInternal.value.focusedTableCells
) {
    val current = uiStateInternal.value
    if (
        document == current.document &&
        focusedBlockId == current.focusedBlockId &&
        focusedTableCells == current.focusedTableCells
    ) {
        return
    }
    uiStateInternal.value = current.copy(
        document = document,
        focusedBlockId = focusedBlockId,
        focusedTableCells = focusedTableCells
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

internal fun NoteEditorViewModel.appendBlock(block: EditorBlock) {
    val current = uiStateInternal.value
    uiStateInternal.value = current.copy(
        document = current.document.copy(blocks = current.document.blocks + block)
    )
    scheduleAutoSave()
}

internal fun NoteEditorViewModel.updateBlock(blockId: String, transform: (EditorBlock) -> EditorBlock) {
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
    var carriedMarks: Set<String> = emptySet()
    val updatedBlocks = current.document.blocks.flatMap { block ->
        if (block.id == blockId && block is EditorBlock.TextBlock) {
            val blockType = block.basicBlockType()
            val isCheckbox = blockType == BasicBlockType.TODO_LIST
            val isEmptyCheckbox = isCheckbox && block.text().trim().isEmpty()
            val formulas = block.children.filter { it.isFormula }
            var formulaIndex = 0

            val splitOffset = lines[0].length
            val effectiveMarks = if (current.pendingTypingMarks.isNotEmpty()) {
                current.pendingTypingMarks
            } else {
                block.marksAtOffset(splitOffset).toSet()
            }
            carriedMarks = effectiveMarks

            val newBlocks = lines.mapIndexed { index, line ->
                val id = if (index == 0) block.id else newBlockId()
                val type = if (isCheckbox) {
                    if (isEmptyCheckbox) BasicBlockType.PARAGRAPH else BasicBlockType.TODO_LIST
                } else {
                    blockType.takeUnless { it == BasicBlockType.UNKNOWN } ?: BasicBlockType.PARAGRAPH
                }
                val checked = if (isCheckbox && !isEmptyCheckbox) {
                    if (index == 0) block.checked else false
                } else {
                    false
                }
                val rawChildren = if (index == 0) {
                    block.children.applyTextDiff(line)
                } else {
                    if (line.isEmpty()) {
                        listOf(RichText("", marks = effectiveMarks.toList()))
                    } else {
                        listOf(RichText(line, marks = effectiveMarks.toList()))
                    }
                }
                val replacement = if (formulas.isEmpty()) {
                    FormulaPlaceholderReplacement(children = rawChildren, nextFormulaIndex = formulaIndex)
                } else {
                    rawChildren.replaceFormulaPlaceholders(
                        formulas = formulas,
                        startingFormulaIndex = formulaIndex
                    )
                }
                formulaIndex = replacement.nextFormulaIndex
                EditorBlock.TextBlock(
                    id = id,
                    type = type.storageValue,
                    children = replacement.children,
                    checked = checked,
                    isExpanded = if (blockType == BasicBlockType.TOGGLE_LIST && index == 0) {
                        block.isExpanded
                    } else {
                        true
                    }
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
        focusedBlockId = nextFocusId ?: current.focusedBlockId,
        pendingTypingMarks = carriedMarks
    )
    scheduleAutoSave()
}

private suspend fun MutableSet<Job>.cancelAndJoinAll() {
    toList().forEach { job ->
        job.cancelAndJoin()
    }
}

internal fun NoteEditorUiState.selectionRangeWithin(textLength: Int): Pair<Int, Int> {
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
