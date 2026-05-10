package com.example.notesapp.ui.home.model

import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.notes.model.NoteUiModel

data class FolderUiModel(
    val id: String,
    val name: String,
    val noteCount: Int,
    val isPrimary: Boolean = false,
    val isShared: Boolean = false
)
data class HomeUiState(
    val recentNotes: List<NoteUiModel> = emptyList(),
    val noteActions: Map<String, Note> = emptyMap(),
    val recentFolders: List<FolderUiModel> = emptyList(),
    val selectedFolderId: String = "all_notes",
    val isLoading: Boolean = false
)
