package com.example.notesapp.ui.home

import com.example.notesapp.ui.notes.NoteUiModel

data class FolderUiModel(
    val id: Long,
    val name: String,
    val noteCount: Int,
    val isPrimary: Boolean = false
)

data class HomeUiState(
    val recentNotes: List<NoteUiModel> = emptyList(),
    val recentFolders: List<FolderUiModel> = emptyList(),
    val isLoading: Boolean = false
)
