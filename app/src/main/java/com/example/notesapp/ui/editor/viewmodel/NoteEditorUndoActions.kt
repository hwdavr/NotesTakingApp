package com.example.notesapp.ui.editor.viewmodel

/** Reverts the most recent document step and schedules the visible state for autosave. */
fun NoteEditorViewModel.undo() {
    if (uiStateInternal.undo()) {
        scheduleAutoSave()
    }
}

/** Re-applies the most recently undone step and schedules the visible state for autosave. */
fun NoteEditorViewModel.redo() {
    if (uiStateInternal.redo()) {
        scheduleAutoSave()
    }
}
