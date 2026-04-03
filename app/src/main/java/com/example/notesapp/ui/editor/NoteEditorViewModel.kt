package com.example.notesapp.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NoteEditorUiState(
    val noteId: Long? = null,
    val title: String = "",
    val content: String = "",
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val isLoaded: Boolean = false
)

class NoteEditorViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    fun load(noteId: Long) {
        if (noteId < 0L || _uiState.value.isLoaded) {
            _uiState.value = _uiState.value.copy(isLoaded = true)
            return
        }

        viewModelScope.launch {
            val note = noteRepository.getNoteById(noteId)
            _uiState.value = if (note != null) {
                NoteEditorUiState(
                    noteId = note.id,
                    title = note.title,
                    content = note.content,
                    isFavorite = note.isFavorite,
                    isArchived = note.isArchived,
                    isLoaded = true
                )
            } else {
                NoteEditorUiState(isLoaded = true)
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onContentChange(value: String) {
        _uiState.value = _uiState.value.copy(content = value)
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }

    fun save(onDone: () -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val current = _uiState.value
            val note = NoteEntity(
                id = current.noteId ?: 0,
                title = current.title.ifBlank { "Untitled note" },
                content = current.content,
                isFavorite = current.isFavorite,
                isArchived = current.isArchived,
                createdAt = now,
                updatedAt = now
            )
            noteRepository.insert(note)
            onDone()
        }
    }

    class Factory(private val noteRepository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteEditorViewModel(noteRepository) as T
        }
    }
}
