package com.example.notesapp.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<NotesUiState> = combine(
        noteRepository.getActiveNotes(),
        searchQuery
    ) { notes, query ->
        val filtered = if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
            }
        }
        NotesUiState(
            isLoading = false,
            notes = filtered.mapIndexed { index, note ->
                NoteUiModel(
                    id = note.id,
                    title = note.title,
                    preview = note.content,
                    isFavorite = note.isFavorite,
                    colorIndex = (note.id % 4).toInt()
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
