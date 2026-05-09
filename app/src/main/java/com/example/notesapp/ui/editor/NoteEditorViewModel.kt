package com.example.notesapp.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.editor.document.EditorBlock
import com.example.notesapp.ui.editor.document.NoteDocument
import com.example.notesapp.ui.editor.document.RichText
import com.example.notesapp.ui.editor.document.newBlockId
import com.example.notesapp.ui.editor.document.parseMarkdownTextBlock
import com.example.notesapp.ui.editor.document.text
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
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
    val isFavorite: Boolean = false
) {
    val content: String
        get() = document.toPlainText()
}

@HiltViewModel
open class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    open val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    fun toggleFormattingToolbar() {
        _uiState.value = _uiState.value.copy(
            isFormattingToolbarVisible = !_uiState.value.isFormattingToolbarVisible
        )
    }

    fun setFocusedBlock(blockId: String?) {
        _uiState.value = _uiState.value.copy(focusedBlockId = blockId)
    }

    fun updateSelection(start: Int, end: Int) {
        _uiState.value = _uiState.value.copy(
            selectionStart = start,
            selectionEnd = end
        )
    }

    private var autoSaveJob: Job? = null

    fun load(noteId: String?, folderId: String? = null) {
        viewModelScope.launch {
            folderRepository.sync()
            val folders = folderRepository.getFolders().first()
            if (noteId.isNullOrBlank()) {
                _uiState.value = NoteEditorUiState(
                    noteId = "note_${UUID.randomUUID()}",
                    availableFolders = folders,
                    folderId = folderId,
                    isLoaded = true
                )
                return@launch
            }

            val note = noteRepository.getNoteById(noteId)
            _uiState.value = if (note != null) {
                NoteEditorUiState(
                    noteId = note.id,
                    title = note.title,
                    document = NoteDocument.fromContent(note.content).ensureEditableTextBlock(),
                    folderId = note.folderId,
                    availableFolders = folders,
                    createdAt = note.createdAt,
                    isFavorite = note.isFavorite,
                    isLoaded = true
                )
            } else {
                NoteEditorUiState(
                    noteId = "note_${UUID.randomUUID()}",
                    availableFolders = folders,
                    isLoaded = true
                )
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
        scheduleAutoSave()
    }

    fun rename(newName: String) {
        _uiState.value = _uiState.value.copy(title = newName)
        viewModelScope.launch {
            saveInternally()
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value
        val newFavorite = !current.isFavorite
        _uiState.value = current.copy(isFavorite = newFavorite)
        viewModelScope.launch {
            val note = noteRepository.getNoteById(current.noteId ?: return@launch) ?: return@launch
            noteRepository.save(note.copy(isFavorite = newFavorite, updatedAt = System.currentTimeMillis()))
        }
    }

    fun onContentChange(value: String) {
        val current = _uiState.value
        val blocks = current.document.blocks
        val firstTextIndex = blocks.indexOfFirst { it is EditorBlock.TextBlock }
        val updatedBlocks = if (firstTextIndex >= 0) {
            blocks.mapIndexed { index, block ->
                if (index == firstTextIndex && block is EditorBlock.TextBlock) {
                    parseMarkdownTextBlock(id = block.id, text = value)
                } else {
                    block
                }
            }
        } else {
            listOf(parseMarkdownTextBlock(text = value)) + blocks
        }
        _uiState.value = current.copy(document = current.document.copy(blocks = updatedBlocks))
        scheduleAutoSave()
    }

    fun onTextBlockChange(blockId: String, value: String) {
        if (value.contains('\n')) {
            splitTextBlock(blockId, value)
            return
        }
        updateBlock(blockId) { block ->
            if (block is EditorBlock.TextBlock) parseMarkdownTextBlock(id = block.id, text = value) else block
        }
    }

    fun toggleBlockMark(blockId: String, mark: String) {
        val state = _uiState.value
        val start = state.selectionStart
        val end = state.selectionEnd

        updateBlock(blockId) { block ->
            if (block !is EditorBlock.TextBlock) return@updateBlock block

            val text = block.text()
            if (start == end || start < 0 || end > text.length) {
                // If no selection, toggle for the whole block as before
                val hasMark = block.children.any { mark in it.marks }
                return@updateBlock block.copy(
                    children = block.children.map { child ->
                        val marks = if (hasMark) child.marks - mark else (child.marks + mark).distinct()
                        child.copy(marks = marks)
                    }
                )
            }

            // If there's a selection, insert markdown markers
            val marker = when (mark) {
                "bold" -> "**"
                "italic" -> "*"
                "code" -> "`"
                else -> ""
            }
            if (marker.isEmpty()) return@updateBlock block

            val selectedText = text.substring(start, end)
            val newText = if (selectedText.startsWith(marker) && selectedText.endsWith(marker)) {
                // Remove markers if already present
                text.substring(0, start) +
                    selectedText.substring(marker.length, selectedText.length - marker.length) +
                    text.substring(end)
            } else {
                // Add markers
                text.substring(0, start) + marker + selectedText + marker + text.substring(end)
            }

            parseMarkdownTextBlock(id = block.id, text = newText)
        }
    }

    fun addParagraphBlock() {
        appendBlock(EditorBlock.TextBlock(children = listOf(RichText(""))))
    }

    fun addImageBlock() {
        appendBlock(EditorBlock.ImageBlock())
    }

    fun updateImageBlock(blockId: String, url: String? = null, caption: String? = null) {
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
        appendBlock(EditorBlock.TableBlock())
    }

    fun updateTableCell(blockId: String, rowIndex: Int, cellIndex: Int, value: String) {
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
        _uiState.value = _uiState.value.copy(folderId = folderId)
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(2000)
            saveInternally()
        }
    }

    private suspend fun saveInternally() {
        val current = _uiState.value
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
            isFavorite = current.isFavorite
        )
        noteRepository.save(note)

        // Update state with generated ID and createdAt to avoid duplicate creations
        _uiState.value = _uiState.value.copy(
            noteId = noteId,
            createdAt = note.createdAt
        )
    }

    fun save(onDone: () -> Unit) {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            saveInternally()
            onDone()
        }
    }

    fun shareCurrentNote(onReady: (String) -> Unit) {
        autoSaveJob?.cancel()
        viewModelScope.launch {
            saveInternally()
            _uiState.value.noteId?.let(onReady)
        }
    }

    fun delete(onDone: () -> Unit) {
        val current = _uiState.value
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
                    updatedAt = System.currentTimeMillis()
                )
            )
            onDone()
        }
    }

    private fun appendBlock(block: EditorBlock) {
        val current = _uiState.value
        _uiState.value = current.copy(
            document = current.document.copy(blocks = current.document.blocks + block)
        )
        scheduleAutoSave()
    }

    private fun updateBlock(blockId: String, transform: (EditorBlock) -> EditorBlock) {
        val current = _uiState.value
        _uiState.value = current.copy(
            document = current.document.copy(
                blocks = current.document.blocks.map { block ->
                    if (block.id == blockId) transform(block) else block
                }
            )
        )
        scheduleAutoSave()
    }

    private fun splitTextBlock(blockId: String, value: String) {
        val current = _uiState.value
        val lines = value.split('\n')
        var nextFocusId: String? = null

        val updatedBlocks = current.document.blocks.flatMap { block ->
            if (block.id == blockId && block is EditorBlock.TextBlock) {
                val newBlocks = lines.mapIndexed { index, line ->
                    parseMarkdownTextBlock(
                        id = if (index == 0) block.id else newBlockId(),
                        text = line
                    )
                }
                nextFocusId = newBlocks.lastOrNull()?.id
                newBlocks
            } else {
                listOf(block)
            }
        }

        _uiState.value = current.copy(
            document = current.document.copy(blocks = updatedBlocks),
            focusedBlockId = nextFocusId ?: current.focusedBlockId
        )
        scheduleAutoSave()
    }

    fun deleteBlock(blockId: String) {
        val current = _uiState.value
        val blocks = current.document.blocks
        if (blocks.size <= 1) return

        val index = blocks.indexOfFirst { it.id == blockId }
        if (index == -1) return

        val nextFocusId = if (index > 0) {
            blocks[index - 1].id
        } else {
            blocks[index + 1].id
        }

        val updatedBlocks = blocks.filter { it.id != blockId }
        _uiState.value = current.copy(
            document = current.document.copy(blocks = updatedBlocks),
            focusedBlockId = nextFocusId
        )
        scheduleAutoSave()
    }
}
