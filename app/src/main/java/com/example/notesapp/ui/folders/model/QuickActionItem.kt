package com.example.notesapp.ui.folders.model

import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note

sealed class QuickActionItem {
    data class FolderItem(val folder: Folder) : QuickActionItem()
    data class NoteItem(val note: Note) : QuickActionItem()
}
