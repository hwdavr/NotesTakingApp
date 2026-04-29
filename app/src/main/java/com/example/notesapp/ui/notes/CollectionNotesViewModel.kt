package com.example.notesapp.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.domain.note.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CollectionNotesUiState(
    val isLoading: Boolean = false,
    val label: String = "",
    val type: String = "all",
    val folderId: String? = null,
    val notes: List<NoteUiModel> = emptyList()
)

@HiltViewModel
class CollectionNotesViewModel @Inject constructor(
    noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val type: String = savedStateHandle["type"] ?: "all"
    private val folderId: String? = savedStateHandle.get<String>("folderId")?.ifBlank { null }
    private val label: String = savedStateHandle["label"] ?: defaultLabel(type)

    val uiState: StateFlow<CollectionNotesUiState> = noteRepository.getActiveNotes()
        .map { notes ->
            val filtered = when (type) {
                "folder" -> notes.filter { it.folderId == folderId }
                "favorites" -> notes.filter { false }
                "archive" -> notes.filter { false }
                else -> notes
            }

            CollectionNotesUiState(
                isLoading = false,
                label = label,
                type = type,
                folderId = folderId,
                notes = filtered.map { note ->
                    NoteUiModel(
                        id = note.id,
                        title = note.title,
                        preview = note.content,
                        colorIndex = note.id.hashCode().mod(4).let { if (it < 0) it + 4 else it }
                    )
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CollectionNotesUiState(isLoading = true)
        )
}

private fun defaultLabel(type: String): String = when (type) {
    "favorites" -> "Favorites"
    "archive" -> "Archive"
    else -> "All Notes"
}
