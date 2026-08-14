package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.parseMarkdownTextBlock

fun NoteEditorViewModel.onContentChange(value: String) {
    val current = uiStateInternal.value
    if (!current.isEditable) return
    val blocks = current.document.blocks
    val firstTextIndex = blocks.indexOfFirst { it is EditorBlock.TextBlock }
    val updatedBlocks = if (firstTextIndex >= 0) {
        blocks.mapIndexed { index, block ->
            if (index == firstTextIndex && block is EditorBlock.TextBlock) {
                parseMarkdownTextBlock(id = block.id, text = value)
            } else {
                block
            }
        }
    } else {
        listOf(parseMarkdownTextBlock(text = value)) + blocks
    }
    uiStateInternal.value = current.copy(document = current.document.copy(blocks = updatedBlocks))
    scheduleAutoSave()
}
