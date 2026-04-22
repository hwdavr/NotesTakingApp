package com.example.notesapp.ui.notes

data class NoteUiModel(
    val id: Long,
    val title: String,
    val preview: String,
    val isFavorite: Boolean,
    val colorIndex: Int
)

data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: List<NoteUiModel> = emptyList()
)
