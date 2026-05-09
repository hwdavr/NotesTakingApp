package com.example.notesapp.ui.notes.model

data class NoteUiModel(
    val id: String,
    val title: String,
    val preview: String,
    val colorIndex: Int,
    val isFavorite: Boolean = false
)
data class NotesUiState(
    val isLoading: Boolean = false,
    val notes: List<NoteUiModel> = emptyList()
)
