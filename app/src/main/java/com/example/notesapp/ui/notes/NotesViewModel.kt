package com.example.notesapp.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NotesViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")

    private val allNotes = noteRepository.getActiveNotes()

    val uiState: StateFlow<List<NoteEntity>> = combine(allNotes, searchQuery) { notes, query ->
        if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun onSearchChanged(query: String) {
        searchQuery.value = query
    }

    class Factory(private val noteRepository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(noteRepository) as T
        }
    }
}
