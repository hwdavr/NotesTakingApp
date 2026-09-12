package com.example.notesapp.ui.editor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.domain.comment.model.NoteBlockComment
import com.example.notesapp.domain.comment.repository.NoteCommentRepository
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.domain.share.NoteShare
import com.example.notesapp.domain.share.NoteShareRepository
import com.example.notesapp.domain.share.NoteShareStatus
import com.example.notesapp.ui.editor.model.MentionDateSuggestion
import com.example.notesapp.ui.editor.model.MentionNoteSuggestion
import com.example.notesapp.ui.editor.model.MentionUserSuggestion
import com.example.notesapp.ui.editor.model.MentionsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val EMPTY_UI_TEXT = ""

data class DiscussionUiState(
    val isVisible: Boolean = false,
    val noteId: String? = null,
    val blockId: String? = null,
    val focusedBlockText: String = EMPTY_UI_TEXT,
    val currentUserInitial: String = EMPTY_UI_TEXT,
    val comments: List<NoteBlockComment> = emptyList(),
    val commentText: String = EMPTY_UI_TEXT,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val mentionDates: List<MentionDateSuggestion> = emptyList(),
    val mentionUsers: List<MentionUserSuggestion> = emptyList(),
    val mentionNotes: List<MentionNoteSuggestion> = emptyList(),
    val isMentionSuggestionsVisible: Boolean = false,
    val isMentionFooterVisible: Boolean = false,
    val mentionFooterText: String = EMPTY_UI_TEXT
)

@HiltViewModel
class DiscussionViewModel @Inject constructor(
    private val commentRepository: NoteCommentRepository,
    private val noteRepository: NoteRepository,
    private val noteShareRepository: NoteShareRepository,
    private val authManager: AuthManager,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscussionUiState())
    val uiState: StateFlow<DiscussionUiState> = _uiState.asStateFlow()

    private var activeNotes: List<Note> = emptyList()
    private var noteShares: List<NoteShare> = emptyList()
    private var folderNamesById: Map<String, String> = emptyMap()
    private var commentsJob: Job? = null
    private var sharesJob: Job? = null

    init {
        viewModelScope.launch {
            noteRepository.getActiveNotes()
                .catch { emit(emptyList()) }
                .collect { notes ->
                    activeNotes = notes
                    refreshMentions()
                }
        }
    }

    fun open(
        noteId: String?,
        blockId: String?,
        focusedBlockText: String,
        folders: List<Folder>,
        initialText: String = "@"
    ) {
        if (noteId.isNullOrBlank() || blockId.isNullOrBlank()) return

        commentsJob?.cancel()
        sharesJob?.cancel()
        noteShares = emptyList()
        folderNamesById = folders.associate { folder -> folder.id to folder.name }
        val currentUserInitial = authManager.profileEmail.value
            ?.substringBefore('@')
            ?.firstOrNull()
            ?.uppercase()
            ?: EMPTY_UI_TEXT
        _uiState.value = DiscussionUiState(
            isVisible = true,
            noteId = noteId,
            blockId = blockId,
            focusedBlockText = focusedBlockText,
            currentUserInitial = currentUserInitial,
            commentText = initialText,
            selectionStart = initialText.length,
            selectionEnd = initialText.length
        )
        refreshMentions()

        commentsJob = viewModelScope.launch {
            refreshWithoutBreakingCache {
                commentRepository.refreshComments(noteId, blockId)
            }
            commentRepository.observeComments(noteId, blockId)
                .catch { emit(emptyList()) }
                .collect { comments ->
                    _uiState.update { it.copy(comments = comments) }
                }
        }

        sharesJob = viewModelScope.launch {
            refreshWithoutBreakingCache {
                noteShareRepository.refreshNoteShares(noteId)
            }
            noteShareRepository.observeNoteShares(noteId)
                .catch { emit(emptyList()) }
                .collect { shares ->
                    noteShares = shares.filter { it.status == NoteShareStatus.ACTIVE }
                    refreshMentions()
                }
        }
    }

    fun dismiss() {
        commentsJob?.cancel()
        commentsJob = null
        sharesJob?.cancel()
        sharesJob = null
        _uiState.update { it.copy(isVisible = false, isMentionSuggestionsVisible = false) }
    }

    fun onCommentValueChange(text: String, selectionStart: Int, selectionEnd: Int) {
        val safeStart = selectionStart.coerceIn(0, text.length)
        val safeEnd = selectionEnd.coerceIn(0, text.length)
        _uiState.update {
            it.copy(
                commentText = text,
                selectionStart = safeStart,
                selectionEnd = safeEnd
            )
        }
        refreshMentions()
    }

    fun onMentionButtonClick() {
        val current = _uiState.value
        val cursor = current.selectionStart.coerceIn(0, current.commentText.length)
        val updatedText = current.commentText.substring(0, cursor) + "@" +
            current.commentText.substring(cursor)
        _uiState.value = current.copy(
            commentText = updatedText,
            selectionStart = cursor + 1,
            selectionEnd = cursor + 1
        )
        refreshMentions()
    }

    fun applyMentionCompletion(insertText: String) {
        val current = _uiState.value
        val cursor = current.selectionStart.coerceIn(0, current.commentText.length)
        val atIndex = current.commentText.lastIndexOf('@', cursor - 1)
        if (atIndex < 0) return

        val completedText = current.commentText.substring(0, atIndex) + insertText + " " +
            current.commentText.substring(cursor)
        val nextCursor = atIndex + insertText.length + 1
        _uiState.value = current.copy(
            commentText = completedText,
            selectionStart = nextCursor,
            selectionEnd = nextCursor
        )
        refreshMentions()
    }

    fun sendComment() {
        val current = _uiState.value
        val noteId = current.noteId ?: return
        val blockId = current.blockId ?: return
        val body = current.commentText.trim()
        if (body.isEmpty()) return

        viewModelScope.launch {
            commentRepository.addComment(noteId, blockId, body)
            _uiState.update {
                it.copy(
                    commentText = "",
                    selectionStart = 0,
                    selectionEnd = 0,
                    isMentionSuggestionsVisible = false,
                    mentionDates = emptyList(),
                    mentionUsers = emptyList(),
                    mentionNotes = emptyList()
                )
            }
        }
    }

    private fun refreshMentions() {
        val current = _uiState.value
        val queryStart = current.commentText.lastIndexOf('@', current.selectionStart - 1)
        val query = if (queryStart >= 0 &&
            current.commentText.substring(queryStart + 1, current.selectionStart).none { it.isWhitespace() }
        ) {
            current.commentText.substring(queryStart + 1, current.selectionStart)
        } else {
            null
        }

        if (query == null) {
            _uiState.update {
                it.copy(
                    isMentionSuggestionsVisible = false,
                    mentionDates = emptyList(),
                    mentionUsers = emptyList(),
                    mentionNotes = emptyList(),
                    isMentionFooterVisible = false,
                    mentionFooterText = ""
                )
            }
            return
        }

        val dates = MentionsCalculator(clock).getDateSuggestions().filter { suggestion ->
            query.isEmpty() || dateLabel(suggestion).contains(query, ignoreCase = true)
        }
        val users = buildUserSuggestions().filter { user ->
            query.isEmpty() || user.displayName.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true)
        }
        val notes = activeNotes
            .asSequence()
            .filter { note -> note.id != current.noteId }
            .map { note ->
                MentionNoteSuggestion(
                    id = note.id,
                    title = note.title,
                    folderName = note.folderId?.let(folderNamesById::get),
                    insertText = "@${note.title}"
                )
            }
            .filter { note ->
                query.isEmpty() || note.title.contains(query, ignoreCase = true) ||
                    note.folderName.orEmpty().contains(query, ignoreCase = true)
            }
            .toList()

        val maxSuggestions = 3
        val datesToShow = dates.take(maxSuggestions)
        val usersToShow = users.take(maxSuggestions)
        val notesToShow = notes.take(maxSuggestions)
        val total = dates.size + users.size + notes.size
        val displayed = datesToShow.size + usersToShow.size + notesToShow.size
        val remaining = total - displayed
        _uiState.update {
            it.copy(
                isMentionSuggestionsVisible = total > 0,
                mentionDates = datesToShow,
                mentionUsers = usersToShow,
                mentionNotes = notesToShow,
                isMentionFooterVisible = remaining > 0,
                mentionFooterText = remaining.toString()
            )
        }
    }

    private fun buildUserSuggestions(): List<MentionUserSuggestion> {
        val ownerEmail = authManager.profileEmail.value
        val ownerShare = ownerEmail?.let { email ->
            noteShares.firstOrNull { share -> share.email.equals(email, ignoreCase = true) }
        }
        val ownerSuggestion = ownerEmail?.let { email ->
            val displayName = ownerShare?.displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore('@')
            MentionUserSuggestion(
                email = email,
                displayName = displayName,
                isYou = true,
                isOwner = true,
                insertText = "@$displayName"
            )
        }
        val collaborators = noteShares.map { share ->
            val isYou = ownerEmail != null && share.email.equals(ownerEmail, ignoreCase = true)
            val displayName = share.displayName?.takeIf { it.isNotBlank() }
                ?: share.email.substringBefore('@')
            MentionUserSuggestion(
                email = share.email,
                displayName = displayName,
                isYou = isYou,
                isOwner = false,
                insertText = "@$displayName"
            )
        }
        return (listOfNotNull(ownerSuggestion) + collaborators).distinctBy { it.email.lowercase() }
    }

    private fun dateLabel(suggestion: MentionDateSuggestion): String = suggestion.insertText.removePrefix("@")

    private suspend fun refreshWithoutBreakingCache(action: suspend () -> Unit) {
        try {
            action()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // Cached observations remain available when the refresh cannot reach the server.
        }
    }

    override fun onCleared() {
        commentsJob?.cancel()
        sharesJob?.cancel()
        super.onCleared()
    }
}
