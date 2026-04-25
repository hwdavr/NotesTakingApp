package com.example.notesapp.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteEditorUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val availableFolders: List<Folder> = emptyList(),
    val createdAt: Long = 0L,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isLoaded: Boolean = false
)

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    fun load(noteId: Long, folderId: Long? = null) {
        viewModelScope.launch {
            val folders = folderRepository.getFolders().first()
            if (noteId < 0L) {
                _uiState.value = _uiState.value.copy(
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
                    content = note.content,
                    folderId = note.folderId,
                    availableFolders = folders,
                    createdAt = note.createdAt,
                    isFavorite = note.isFavorite,
                    isArchived = note.isArchived,
                    isLoaded = true
                )
            } else {
                NoteEditorUiState(availableFolders = folders, isLoaded = true)
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onContentChange(value: String) {
        _uiState.value = _uiState.value.copy(content = value)
    }

    fun onFolderSelected(folderId: Long?) {
        _uiState.value = _uiState.value.copy(folderId = folderId)
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }

    fun toggleArchived() {
        _uiState.value = _uiState.value.copy(isArchived = !_uiState.value.isArchived)
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = _uiState.value
            val note = Note(
                id = current.noteId ?: 0,
                title = current.title.ifBlank { "Untitled note" },
                content = current.content,
                folderId = current.folderId,
                isFavorite = current.isFavorite,
                isArchived = current.isArchived,
                createdAt = if (current.noteId == null) now else current.createdAt,
                updatedAt = now
            )
            noteRepository.save(note)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val current = _uiState.value
        if (current.noteId == null) {
            onDone()
            return
        }

        viewModelScope.launch {
            noteRepository.delete(
                Note(
                    id = current.noteId,
                    title = current.title,
                    content = current.content,
                    folderId = current.folderId,
                    isFavorite = current.isFavorite,
                    isArchived = current.isArchived,
                    createdAt = current.createdAt,
                    updatedAt = System.currentTimeMillis()
                )
            )
            onDone()
        }
    }
}
