package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock

fun NoteEditorViewModel.addImageBlock() {
    if (!uiState.value.isEditable) return
    appendBlock(EditorBlock.ImageBlock())
}

fun NoteEditorViewModel.addTableBlock() {
    if (!uiState.value.isEditable) return
    appendBlock(EditorBlock.TableBlock())
}
