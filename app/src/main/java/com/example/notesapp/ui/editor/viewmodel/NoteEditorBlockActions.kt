package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock

fun NoteEditorViewModel.addImageBlock() {
    if (!uiState.value.isEditable) return
    insertRequestedBlock(EditorBlock.ImageBlock())
}

fun NoteEditorViewModel.addTableBlock() {
    if (!uiState.value.isEditable) return
    insertRequestedBlock(EditorBlock.TableBlock())
}

private fun NoteEditorViewModel.insertRequestedBlock(block: EditorBlock) {
    val current = uiStateInternal.value
    val focusedIndex = current.focusedBlockId
        ?.let { focusedId -> current.document.blocks.indexOfFirst { it.id == focusedId } }
        ?.takeIf { it >= 0 }
    val fallbackIndex = current.document.blocks.indexOfLast { it is EditorBlock.TextBlock }
        .takeIf { it >= 0 }
    val insertionIndex = (focusedIndex ?: fallbackIndex)?.plus(1)
        ?: current.document.blocks.size
    val updatedBlocks = current.document.blocks.toMutableList().apply {
        add(insertionIndex.coerceIn(0, size), block)
    }
    uiStateInternal.value = current.copy(
        document = current.document.copy(blocks = updatedBlocks),
        blockIdToReveal = block.id
    )
    scheduleAutoSave()
}
