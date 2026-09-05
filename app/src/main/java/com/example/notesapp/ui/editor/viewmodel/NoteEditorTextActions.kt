package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.basicBlockType
import com.example.notesapp.ui.editor.mapper.mergeAdjacentWithSameMarks
import com.example.notesapp.ui.editor.mapper.newBlockId
import com.example.notesapp.ui.editor.mapper.parseMarkdownTextBlock
import com.example.notesapp.ui.editor.mapper.splitAtOffsets
import com.example.notesapp.ui.editor.mapper.text

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

fun NoteEditorViewModel.resetSelectedTextToBody(blockId: String) {
    if (!uiStateInternal.value.isEditable) return
    val state = uiStateInternal.value
    val focusedId = state.focusedBlockId
    if (focusedId != null && focusedId != blockId) return
    val block = state.document.blocks.find { it.id == blockId } as? EditorBlock.TextBlock ?: return
    val text = block.text()
    val start = state.selectionStart
    val end = state.selectionEnd
    val selStart = minOf(start, end)
    val selEnd = maxOf(start, end)
    if (selStart == selEnd || selStart < 0 || selEnd > text.length) return

    val splitChildren = block.children.splitAtOffsets(listOf(selStart, selEnd))
    var currentOffset = 0
    val prefixChildren = mutableListOf<RichText>()
    val selectedChildren = mutableListOf<RichText>()
    val suffixChildren = mutableListOf<RichText>()

    for (child in splitChildren) {
        val childStart = currentOffset
        val childEnd = childStart + child.text.length
        currentOffset = childEnd
        when {
            childEnd <= selStart -> prefixChildren.add(child)
            childStart >= selEnd -> suffixChildren.add(child)
            else -> selectedChildren.add(
                child.copy(
                    marks = emptyList(),
                    linkTargetId = null,
                    formulaSource = null,
                    inlineId = null
                )
            )
        }
    }

    val isParagraph = block.type == "paragraph" || block.basicBlockType() == BasicBlockType.PARAGRAPH
    val updatedBlocks: List<EditorBlock> = if (isParagraph) {
        val mergedChildren = (prefixChildren + selectedChildren + suffixChildren).mergeAdjacentWithSameMarks()
        val updatedBlock = block.copy(type = "paragraph", children = mergedChildren, checked = false)
        state.document.blocks.map { if (it.id == blockId) updatedBlock else it }
    } else {
        if (prefixChildren.isEmpty() && suffixChildren.isEmpty()) {
            val updatedBlock = block.copy(
                type = "paragraph",
                children = selectedChildren.mergeAdjacentWithSameMarks(),
                checked = false
            )
            state.document.blocks.map { if (it.id == blockId) updatedBlock else it }
        } else {
            val newBlocks = mutableListOf<EditorBlock>()
            if (prefixChildren.isNotEmpty()) {
                newBlocks.add(
                    block.copy(
                        id = block.id,
                        children = prefixChildren.mergeAdjacentWithSameMarks()
                    )
                )
            }
            val selectedBlock = EditorBlock.TextBlock(
                id = newBlockId(),
                type = "paragraph",
                children = selectedChildren.mergeAdjacentWithSameMarks(),
                checked = false
            )
            newBlocks.add(selectedBlock)
            if (suffixChildren.isNotEmpty()) {
                newBlocks.add(
                    block.copy(
                        id = newBlockId(),
                        children = suffixChildren.mergeAdjacentWithSameMarks()
                    )
                )
            }
            state.document.blocks.flatMap { b ->
                if (b.id == blockId) newBlocks else listOf(b)
            }
        }
    }

    uiStateInternal.value = state.copy(
        document = state.document.copy(blocks = updatedBlocks)
    )
    scheduleAutoSave()
}
