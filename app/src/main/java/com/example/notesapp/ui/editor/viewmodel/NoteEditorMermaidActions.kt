package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock

fun NoteEditorViewModel.updateMermaidBlock(blockId: String, code: String? = null, title: String? = null) {
    if (!uiState.value.isEditable) return
    updateBlock(blockId) { block ->
        if (block is EditorBlock.MermaidBlock) {
            block.copy(
                code = code ?: block.code,
                title = title ?: block.title
            )
        } else {
            block
        }
    }
}
