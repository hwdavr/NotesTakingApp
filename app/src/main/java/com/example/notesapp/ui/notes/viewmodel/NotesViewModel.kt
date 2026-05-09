package com.example.notesapp.ui.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.ui.editor.mapper.noteContentPreview
import com.example.notesapp.ui.notes.model.NoteUiModel
import com.example.notesapp.ui.notes.model.NotesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    init {
        viewModelScope.launch {
            noteRepository.sync()
        }
    }
    val uiState: StateFlow<NotesUiState> = combine(
        noteRepository.getActiveNotes(),
        searchQuery
    ) { notes, query ->
        val filtered = if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                val preview = noteContentPreview(it.content)
                it.title.contains(query, ignoreCase = true) ||
                    preview.contains(query, ignoreCase = true)
            }
        }
        NotesUiState(
            isLoading = false,
            notes = filtered.mapIndexed { index, note ->
                NoteUiModel(
                    id = note.id,
                    title = note.title,
                    preview = noteContentPreview(note.content),
                    colorIndex = note.id.hashCode().mod(4).let { if (it < 0) it + 4 else it }
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesUiState(isLoading = true)
    )
    fun onSearchChanged(query: String) {
        searchQuery.value = query
    }
}
