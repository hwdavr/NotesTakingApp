package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock

/**
 * Applies the bookmark produced by the full-page Add/Edit destination to the open document and
 * schedules autosave.
 *
 * A non-blank [blockId] that matches an existing block updates that block in place and keeps its
 * stable identity. A new bookmark is inserted immediately after the focused block, or appended
 * when no block is focused, so insertion order is deterministic.
 */
fun NoteEditorViewModel.upsertWebBookmark(blockId: String?, url: String, title: String, description: String) {
    if (!uiStateInternal.value.isEditable) return
    val current = uiStateInternal.value
    val existingIndex = blockId
        ?.takeIf { it.isNotBlank() }
        ?.let { id -> current.document.blocks.indexOfFirst { block -> block.id == id } }
        ?: -1
    val existing = current.document.blocks.getOrNull(existingIndex)

    val bookmark = EditorBlock.WebBookmarkBlock(
        id = existing?.id ?: EditorBlock.WebBookmarkBlock().id,
        url = url,
        title = title,
        description = description
    )
    val updatedBlocks = current.document.blocks.toMutableList().apply {
        if (existingIndex >= 0) {
            this[existingIndex] = bookmark
        } else {
            val focusedIndex = current.focusedBlockId?.let { focusedId ->
                indexOfFirst { block -> block.id == focusedId }.takeIf { it >= 0 }
            }
            if (focusedIndex != null) {
                add(focusedIndex + 1, bookmark)
            } else {
                add(bookmark)
            }
        }
    }

    uiStateInternal.value = current.copy(
        document = current.document.copy(blocks = updatedBlocks),
        focusedBlockId = bookmark.id,
        selectionStart = 0,
        selectionEnd = 0
    )
    scheduleAutoSave()
}
