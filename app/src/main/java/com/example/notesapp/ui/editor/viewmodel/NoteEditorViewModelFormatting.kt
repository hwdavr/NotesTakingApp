package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.applyLinkToRange
import com.example.notesapp.ui.editor.mapper.hasLinkInRange
import com.example.notesapp.ui.editor.mapper.insertLinkedText
import com.example.notesapp.ui.editor.mapper.removeLinkAtOffset
import com.example.notesapp.ui.editor.mapper.text

fun NoteEditorViewModel.toggleFormattingToolbar() {
    uiStateInternal.value = uiStateInternal.value.copy(
        isFormattingToolbarVisible = !uiStateInternal.value.isFormattingToolbarVisible
    )
}

fun NoteEditorViewModel.updateSelection(start: Int, end: Int) {
    uiStateInternal.value = uiStateInternal.value.copy(
        selectionStart = start,
        selectionEnd = end
    )
}

fun NoteEditorViewModel.hasLinkAtCurrentSelection(): Boolean {
    val current = uiStateInternal.value
    val focusedBlock = current.focusedBlockId?.let { id ->
        current.document.blocks.find { it.id == id } as? EditorBlock.TextBlock
    } ?: return false
    val (start, end) = current.selectionRangeWithin(focusedBlock.text().length)
    return focusedBlock.children.hasLinkInRange(start, end)
}

fun NoteEditorViewModel.onTargetNoteSelected(targetId: String, targetTitle: String) {
    if (!uiStateInternal.value.isEditable || targetId.isBlank()) return
    val current = uiStateInternal.value
    val focusedBlock = current.focusedBlockId?.let { id ->
        current.document.blocks.find { it.id == id } as? EditorBlock.TextBlock
    }

    if (focusedBlock == null) {
        val linkTitle = targetTitle.ifBlank { "Untitled" }
        val newBlock = EditorBlock.TextBlock(
            children = listOf(
                RichText(text = linkTitle, linkTargetId = targetId, inlineId = targetId)
            )
        )
        val updatedBlocks = if (current.document.blocks.size == 1 &&
            (current.document.blocks[0] as? EditorBlock.TextBlock)?.text()?.isEmpty() == true
        ) {
            listOf(newBlock)
        } else {
            current.document.blocks + newBlock
        }
        uiStateInternal.value = current.copy(
            document = current.document.copy(blocks = updatedBlocks),
            focusedBlockId = newBlock.id,
            selectionStart = linkTitle.length,
            selectionEnd = linkTitle.length
        )
        scheduleAutoSave()
        return
    }

    val textLength = focusedBlock.text().length
    val (selectionStart, selectionEnd) = current.selectionRangeWithin(textLength)

    if (selectionStart != selectionEnd) {
        val updatedChildren = focusedBlock.children.applyLinkToRange(
            start = selectionStart,
            end = selectionEnd,
            targetId = targetId
        )
        val updatedBlock = focusedBlock.copy(children = updatedChildren)
        uiStateInternal.value = current.copy(
            document = current.document.copy(
                blocks = current.document.blocks.map { block ->
                    if (block.id == updatedBlock.id) updatedBlock else block
                }
            )
        )
    } else {
        val linkTitle = targetTitle.ifBlank { "Untitled" }
        val updatedChildren = focusedBlock.children.insertLinkedText(
            offset = selectionStart,
            text = linkTitle,
            targetId = targetId
        )
        val updatedBlock = focusedBlock.copy(children = updatedChildren)
        val nextCursor = selectionStart + linkTitle.length
        uiStateInternal.value = current.copy(
            document = current.document.copy(
                blocks = current.document.blocks.map { block ->
                    if (block.id == updatedBlock.id) updatedBlock else block
                }
            ),
            selectionStart = nextCursor,
            selectionEnd = nextCursor
        )
    }
    scheduleAutoSave()
}

fun NoteEditorViewModel.onRemoveLinkSelected() {
    if (!uiStateInternal.value.isEditable) return
    val current = uiStateInternal.value
    val focusedBlock = current.focusedBlockId?.let { id ->
        current.document.blocks.find { it.id == id } as? EditorBlock.TextBlock
    } ?: return

    val textLength = focusedBlock.text().length
    val (selectionStart, selectionEnd) = current.selectionRangeWithin(textLength)

    val updatedChildren = if (selectionStart != selectionEnd) {
        focusedBlock.children.applyLinkToRange(selectionStart, selectionEnd, null)
    } else {
        focusedBlock.children.removeLinkAtOffset(selectionStart)
    }
    val updatedBlock = focusedBlock.copy(children = updatedChildren)
    uiStateInternal.value = current.copy(
        document = current.document.copy(
            blocks = current.document.blocks.map { block ->
                if (block.id == updatedBlock.id) updatedBlock else block
            }
        )
    )
    scheduleAutoSave()
}
