package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.domain.comment.model.NoteBlockComment
import com.example.notesapp.domain.comment.repository.NoteCommentRepository
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.newBlockId
import com.example.notesapp.ui.editor.mapper.parseMarkdownTextBlock
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.model.MentionDateSuggestion
import com.example.notesapp.ui.editor.model.MentionNoteSuggestion
import com.example.notesapp.ui.editor.model.MentionUserSuggestion
import com.example.notesapp.ui.editor.model.MentionsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
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
    val isFavorite: Boolean = false,
    val isEditable: Boolean = true,
    val isDiscussionSheetVisible: Boolean = false,
    val comments: List<NoteBlockComment> = emptyList(),
    val activeBlockCommentText: String = "",
    val mentionDates: List<com.example.notesapp.ui.editor.model.MentionDateSuggestion> = emptyList(),
    val mentionUsers: List<com.example.notesapp.ui.editor.model.MentionUserSuggestion> = emptyList(),
    val mentionNotes: List<com.example.notesapp.ui.editor.model.MentionNoteSuggestion> = emptyList(),
    val isMentionSuggestionsVisible: Boolean = false,
    val isMentionFooterVisible: Boolean = false,
    val mentionFooterText: String = ""
) {
    val content: String
        get() = document.toPlainText()
}

@HiltViewModel
open class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val commentRepository: NoteCommentRepository,
    private val noteShareRepository: NoteShareRepository,
    private val authManager: AuthManager,
    private val clock: Clock
) : ViewModel() {

    private var localNotes: List<Note> = emptyList()
    private var noteShares: List<com.example.notesapp.domain.share.NoteShare> = emptyList()
    private var sharesJob: Job? = null

    init {
        viewModelScope.launch {
            noteRepository.getActiveNotes().collect { list ->
                localNotes = list
            }
        }
    }

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
                    isEditable = true,
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
                    isEditable = note.accessRole != NoteAccessRole.READ_ONLY,
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
        if (!_uiState.value.isEditable) return
        _uiState.value = _uiState.value.copy(title = value)
        scheduleAutoSave()
    }
    fun rename(newName: String) {
        if (!_uiState.value.isEditable) return
        _uiState.value = _uiState.value.copy(title = newName)
        viewModelScope.launch {
            saveInternally()
        }
    }
    fun toggleFavorite() {
        if (!_uiState.value.isEditable) return
        val current = _uiState.value
        val newFavorite = !current.isFavorite
        _uiState.value = current.copy(isFavorite = newFavorite)
        viewModelScope.launch {
            val note = noteRepository.getNoteById(current.noteId ?: return@launch) ?: return@launch
            noteRepository.save(note.copy(isFavorite = newFavorite, updatedAt = System.currentTimeMillis()))
        }
    }
    fun onContentChange(value: String) {
        if (!_uiState.value.isEditable) return
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
        if (!_uiState.value.isEditable) return
        if (value.contains('\n')) {
            splitTextBlock(blockId, value)
            return
        }
        updateBlock(blockId) { block ->
            if (block is EditorBlock.TextBlock) parseMarkdownTextBlock(id = block.id, text = value) else block
        }
    }
    fun toggleBlockMark(blockId: String, mark: String) {
        if (!_uiState.value.isEditable) return
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
    fun addBlock(block: EditorBlock) {
        if (!_uiState.value.isEditable) return
        val current = _uiState.value
        _uiState.value = current.copy(
            document = current.document.copy(blocks = current.document.blocks + block)
        )
        scheduleAutoSave()
    }
    fun updateImageBlock(blockId: String, url: String? = null, caption: String? = null) {
        if (!_uiState.value.isEditable) return
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
        if (!_uiState.value.isEditable) return
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
        if (!_uiState.value.isEditable) return
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
    private var commentsJob: Job? = null

    fun setDiscussionSheetVisible(visible: Boolean) {
        if (visible) {
            val currentNoteId = _uiState.value.noteId ?: return
            val currentBlockId = _uiState.value.focusedBlockId
                ?: _uiState.value.document.blocks.filterIsInstance<EditorBlock.TextBlock>().firstOrNull()?.id
                ?: return

            if (_uiState.value.focusedBlockId == null) {
                setFocusedBlock(currentBlockId)
            }

            _uiState.value = _uiState.value.copy(
                isDiscussionSheetVisible = true,
                comments = emptyList(),
                activeBlockCommentText = ""
            )

            commentsJob?.cancel()
            commentsJob = viewModelScope.launch {
                try {
                    commentRepository.refreshComments(currentNoteId, currentBlockId)
                } catch (ignored: Exception) {
                    // Ignore sync errors
                }

                commentRepository.observeComments(currentNoteId, currentBlockId).collect { list ->
                    _uiState.value = _uiState.value.copy(comments = list)
                }
            }

            sharesJob?.cancel()
            sharesJob = viewModelScope.launch {
                try {
                    noteShareRepository.refreshNoteShares(currentNoteId)
                } catch (ignored: Exception) {
                    // Ignore
                }
                noteShareRepository.observeNoteShares(currentNoteId).collect { shares ->
                    noteShares = shares
                    filterMentions(_uiState.value.activeBlockCommentText)
                }
            }
        } else {
            commentsJob?.cancel()
            commentsJob = null
            sharesJob?.cancel()
            sharesJob = null
            _uiState.value = _uiState.value.copy(isDiscussionSheetVisible = false)
        }
    }

    private fun getMentionQuery(text: String): String? {
        val cursor = _uiState.value.selectionStart
        if (cursor in 0..text.length) {
            val lastAt = text.lastIndexOf('@', cursor - 1)
            if (lastAt != -1) {
                val sub = text.substring(lastAt + 1, cursor)
                if (!sub.contains(' ')) {
                    return sub
                }
            }
        }
        return null
    }

    private fun filterMentions(text: String) {
        val query = getMentionQuery(text)
        if (query == null) {
            _uiState.value = _uiState.value.copy(
                isMentionSuggestionsVisible = false,
                mentionDates = emptyList(),
                mentionUsers = emptyList(),
                mentionNotes = emptyList(),
                isMentionFooterVisible = false,
                mentionFooterText = ""
            )
            return
        }

        val calculator = MentionsCalculator(clock)

        // 1. Filter Dates
        val allDates = calculator.getDateSuggestions()
        val filteredDates = if (query.isEmpty()) {
            allDates
        } else {
            allDates.filter { it.description.contains(query, ignoreCase = true) }
        }

        // 2. Filter Users
        val ownerEmail = authManager.profileEmail.value ?: "me@example.com"
        val ownerName = ownerEmail.substringBefore('@')
        val ownerSuggestion = MentionUserSuggestion(
            email = ownerEmail,
            displayName = ownerName,
            isYou = true,
            isOwner = true,
            displayBadge = "You",
            insertText = "@$ownerName"
        )
        val collaboratorSuggestions = noteShares.map { share ->
            val isYou = share.email.equals(ownerEmail, ignoreCase = true)
            val displayName = share.displayName ?: share.email.substringBefore('@')
            MentionUserSuggestion(
                email = share.email,
                displayName = displayName,
                isYou = isYou,
                isOwner = false,
                displayBadge = if (isYou) "You" else "Guest",
                insertText = "@$displayName"
            )
        }
        val allUsers = (listOf(ownerSuggestion) + collaboratorSuggestions).distinctBy { it.email }
        val filteredUsers = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter {
                it.displayName.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
            }
        }

        // 3. Filter Notes
        val allNotes = localNotes.map { note ->
            val folder = _uiState.value.availableFolders.find { it.id == note.folderId }
            val folderPath = folder?.name ?: "My Notes"
            MentionNoteSuggestion(
                id = note.id,
                title = note.title,
                folderBreadcrumb = folderPath,
                insertText = "@${note.title}"
            )
        }
        val filteredNotes = if (query.isEmpty()) {
            allNotes
        } else {
            allNotes.filter {
                it.title.contains(query, ignoreCase = true) || it.folderBreadcrumb.contains(query, ignoreCase = true)
            }
        }

        // Limit to 3 items per section
        val maxSuggestions = 3
        val datesLimited = filteredDates.take(maxSuggestions)
        val usersLimited = filteredUsers.take(maxSuggestions)
        val notesLimited = filteredNotes.take(maxSuggestions)

        val totalMatches = filteredDates.size + filteredUsers.size + filteredNotes.size
        val displayedMatches = datesLimited.size + usersLimited.size + notesLimited.size
        val remaining = totalMatches - displayedMatches

        _uiState.value = _uiState.value.copy(
            isMentionSuggestionsVisible = true,
            mentionDates = datesLimited,
            mentionUsers = usersLimited,
            mentionNotes = notesLimited,
            isMentionFooterVisible = remaining > 0,
            mentionFooterText = if (remaining > 0) "... $remaining more results" else ""
        )
    }

    fun onCommentTextChange(text: String) {
        _uiState.value = _uiState.value.copy(activeBlockCommentText = text)
        filterMentions(text)
    }

    fun applyMentionCompletion(insertText: String) {
        val currentText = _uiState.value.activeBlockCommentText
        val cursor = _uiState.value.selectionStart
        if (cursor < 0 || cursor > currentText.length) return
        val lastAt = currentText.lastIndexOf('@', cursor - 1)
        if (lastAt == -1) return

        val prefix = currentText.substring(0, lastAt)
        val suffix = currentText.substring(cursor)
        val completedText = prefix + insertText + " " + suffix

        _uiState.value = _uiState.value.copy(
            activeBlockCommentText = completedText,
            selectionStart = lastAt + insertText.length + 1,
            selectionEnd = lastAt + insertText.length + 1
        )
        filterMentions(completedText)
    }

    fun sendComment() {
        val current = _uiState.value
        val noteId = current.noteId ?: return
        val blockId = current.focusedBlockId
            ?: current.document.blocks.filterIsInstance<EditorBlock.TextBlock>().firstOrNull()?.id
            ?: return
        val body = current.activeBlockCommentText
        if (body.isBlank()) return

        viewModelScope.launch {
            try {
                commentRepository.addComment(noteId, blockId, body)
                _uiState.value = _uiState.value.copy(activeBlockCommentText = "")
                filterMentions("")
            } catch (ignored: Exception) {
                // Ignore
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        commentsJob?.cancel()
        sharesJob?.cancel()
    }
}
