package com.example.notesapp.ui.editor.screen

import androidx.compose.runtime.Composable
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import com.example.notesapp.ui.editor.components.EditorNoteActionsSheet
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState

/**
 * Wires the editor's sheet-local state and callbacks into [NoteActionsSheetSection] so the editor
 * content composable stays within the file's method-length budget.
 */
@Composable
internal fun NoteActionsSheetHost(
    state: NoteEditorUiState,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveNote: () -> Unit,
    onStartRename: () -> Unit,
    onDelete: () -> Unit,
    onExportNote: () -> Unit
) {
    NoteActionsSheetSection(
        state = state,
        onDismiss = onDismiss,
        onAddToFavorites = {
            onToggleFavorite()
            onDismiss()
        },
        onMoveTo = {
            onDismiss()
            onMoveNote()
        },
        onRename = {
            onDismiss()
            onStartRename()
        },
        onDelete = {
            onDismiss()
            onDelete()
        },
        onExport = {
            onDismiss()
            onExportNote()
        }
    )
}

@Composable
internal fun NoteActionsSheetSection(
    state: NoteEditorUiState,
    onDismiss: () -> Unit,
    onAddToFavorites: () -> Unit,
    onMoveTo: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    val currentNote =
        Note(
            id = state.noteId.orEmpty(),
            title = state.title,
            content = state.document.toJsonString(),
            folderId = state.folderId,
            sortKey = "",
            version = 0,
            deviceId = "",
            createdAt = state.createdAt,
            updatedAt = System.currentTimeMillis(),
            isFavorite = state.isFavorite,
            accessRole = if (state.isEditable) NoteAccessRole.FULL_ACCESS else NoteAccessRole.READ_ONLY
        )
    EditorNoteActionsSheet(
        note = currentNote,
        onDismiss = onDismiss,
        onAddToFavorites = onAddToFavorites,
        onMoveTo = onMoveTo,
        onRename = onRename,
        onDelete = onDelete,
        onExport = onExport
    )
}
