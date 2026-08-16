package com.example.notesapp.ui.editor.viewmodel

fun NoteEditorViewModel.setFocusedBlock(blockId: String?) {
    uiStateInternal.value = uiStateInternal.value.copy(focusedBlockId = blockId)
}
