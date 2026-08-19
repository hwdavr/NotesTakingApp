package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock

fun NoteEditorViewModel.updateCodeBlock(blockId: String, language: String? = null, code: String? = null) {
    if (!uiState.value.isEditable) return
    updateBlock(blockId) { block ->
        if (block is EditorBlock.CodeBlock) {
            block.copy(
                language = language ?: block.language,
                code = code ?: block.code
            )
        } else {
            block
        }
    }
}
