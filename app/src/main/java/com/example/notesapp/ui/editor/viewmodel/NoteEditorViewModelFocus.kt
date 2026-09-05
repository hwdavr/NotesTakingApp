package com.example.notesapp.ui.editor.viewmodel

fun NoteEditorViewModel.setFocusedBlock(blockId: String?) {
    val current = uiStateInternal.value
    uiStateInternal.value = current.copy(
        focusedBlockId = blockId,
        pendingTypingMarks = if (current.focusedBlockId != blockId) emptySet() else current.pendingTypingMarks
    )
}
