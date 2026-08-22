package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.nextColumnId
import com.example.notesapp.ui.editor.mapper.normalized
import com.example.notesapp.ui.editor.model.ChartTableAction

fun NoteEditorViewModel.addChartRow(blockId: String): Boolean = mutateChartBlock(blockId) { block ->
    val normalizedBlock = block.normalized()
    normalizedBlock.copy(
        rows = normalizedBlock.rows + listOf(emptyChartRow(normalizedBlock.columnIds.size))
    )
}

fun NoteEditorViewModel.addChartColumn(blockId: String): Boolean = mutateChartBlock(blockId) { block ->
    val normalizedBlock = block.normalized()
    val newColumnId = normalizedBlock.nextColumnId()
    normalizedBlock.copy(
        columnIds = normalizedBlock.columnIds + newColumnId,
        rows = normalizedBlock.rows.map { row -> row + listOf(emptyChartCell()) }
    )
}

fun NoteEditorViewModel.insertChartColumnLeft(blockId: String, columnIndex: Int): Boolean =
    mutateChartBlock(blockId) { block ->
        val normalizedBlock = block.normalized()
        val insertionIndex = columnIndex.coerceIn(1, normalizedBlock.columnIds.size)
        val newColumnId = normalizedBlock.nextColumnId()
        normalizedBlock.copy(
            columnIds = normalizedBlock.columnIds.toMutableList().apply {
                add(insertionIndex, newColumnId)
            },
            rows = normalizedBlock.rows.map { row ->
                row.toMutableList().apply { add(insertionIndex, emptyChartCell()) }
            }
        )
    }

fun NoteEditorViewModel.insertChartColumnRight(blockId: String, columnIndex: Int): Boolean =
    mutateChartBlock(blockId) { block ->
        val normalizedBlock = block.normalized()
        if (columnIndex !in normalizedBlock.columnIds.indices) return@mutateChartBlock null
        val insertionIndex = (columnIndex + 1).coerceAtMost(normalizedBlock.columnIds.size)
        val newColumnId = normalizedBlock.nextColumnId()
        normalizedBlock.copy(
            columnIds = normalizedBlock.columnIds.toMutableList().apply {
                add(insertionIndex, newColumnId)
            },
            rows = normalizedBlock.rows.map { row ->
                row.toMutableList().apply { add(insertionIndex, emptyChartCell()) }
            }
        )
    }

fun NoteEditorViewModel.deleteChartColumn(blockId: String, columnIndex: Int): Boolean {
    val block = currentChartBlock(blockId)?.normalized() ?: return false
    if (columnIndex <= 0 || columnIndex >= block.columnIds.size || block.columnIds.size <= 2) {
        return false
    }
    val deletedColumnId = block.columnIds[columnIndex]
    val remainingColumnIds = block.columnIds.toMutableList().apply { removeAt(columnIndex) }
    val selectedColumnId = if (deletedColumnId == block.selectedColumnId) {
        remainingColumnIds[1]
    } else {
        block.selectedColumnId
    }
    return replaceChartBlock(
        blockId = blockId,
        block = block.copy(
            columnIds = remainingColumnIds,
            selectedColumnId = selectedColumnId,
            rows = block.rows.map { row ->
                row.toMutableList().apply { removeAt(columnIndex) }
            }
        )
    )
}

fun NoteEditorViewModel.insertChartRowAbove(blockId: String, rowIndex: Int): Boolean =
    mutateChartBlock(blockId) { block ->
        val normalizedBlock = block.normalized()
        if (rowIndex !in normalizedBlock.rows.indices) return@mutateChartBlock null
        normalizedBlock.copy(
            rows = normalizedBlock.rows.toMutableList().apply {
                add(rowIndex, emptyChartRow(normalizedBlock.columnIds.size))
            }
        )
    }

fun NoteEditorViewModel.insertChartRowBelow(blockId: String, rowIndex: Int): Boolean =
    mutateChartBlock(blockId) { block ->
        val normalizedBlock = block.normalized()
        if (rowIndex !in normalizedBlock.rows.indices) return@mutateChartBlock null
        normalizedBlock.copy(
            rows = normalizedBlock.rows.toMutableList().apply {
                add(rowIndex + 1, emptyChartRow(normalizedBlock.columnIds.size))
            }
        )
    }

fun NoteEditorViewModel.deleteChartRow(blockId: String, rowIndex: Int): Boolean {
    val block = currentChartBlock(blockId)?.normalized() ?: return false
    if (rowIndex !in block.rows.indices || block.rows.size <= 1) return false
    return replaceChartBlock(
        blockId = blockId,
        block = block.copy(rows = block.rows.filterIndexed { index, _ -> index != rowIndex })
    )
}

fun NoteEditorViewModel.clearChartColumn(blockId: String, columnIndex: Int): Boolean =
    mutateChartBlock(blockId) { block ->
        val normalizedBlock = block.normalized()
        if (columnIndex !in normalizedBlock.columnIds.indices) return@mutateChartBlock null
        normalizedBlock.copy(
            rows = normalizedBlock.rows.map { row ->
                row.mapIndexed { index, cell ->
                    if (index == columnIndex) emptyChartCell() else cell
                }
            }
        )
    }

fun NoteEditorViewModel.clearChartRow(blockId: String, rowIndex: Int): Boolean = mutateChartBlock(blockId) { block ->
    val normalizedBlock = block.normalized()
    if (rowIndex !in normalizedBlock.rows.indices) return@mutateChartBlock null
    normalizedBlock.copy(
        rows = normalizedBlock.rows.mapIndexed { index, row ->
            if (index == rowIndex) emptyChartRow(normalizedBlock.columnIds.size) else row
        }
    )
}

fun NoteEditorViewModel.onChartTableAction(action: ChartTableAction) {
    when (action) {
        is ChartTableAction.InsertColumnLeft -> insertChartColumnLeft(action.blockId, action.columnIndex)
        is ChartTableAction.InsertColumnRight -> insertChartColumnRight(action.blockId, action.columnIndex)
        is ChartTableAction.DeleteColumn -> deleteChartColumn(action.blockId, action.columnIndex)
        is ChartTableAction.ClearColumn -> clearChartColumn(action.blockId, action.columnIndex)
        is ChartTableAction.InsertRowAbove -> insertChartRowAbove(action.blockId, action.rowIndex)
        is ChartTableAction.InsertRowBelow -> insertChartRowBelow(action.blockId, action.rowIndex)
        is ChartTableAction.DeleteRow -> deleteChartRow(action.blockId, action.rowIndex)
        is ChartTableAction.ClearRow -> clearChartRow(action.blockId, action.rowIndex)
    }
}

private fun NoteEditorViewModel.mutateChartBlock(
    blockId: String,
    transform: (EditorBlock.ChartBlock) -> EditorBlock.ChartBlock?
): Boolean {
    if (!uiStateInternal.value.isEditable) return false
    val current = uiStateInternal.value
    val currentBlock = current.document.blocks.firstOrNull { it.id == blockId }
        as? EditorBlock.ChartBlock
    if (currentBlock == null) return false
    val updatedBlock = transform(currentBlock)
    return when {
        updatedBlock == null -> false
        updatedBlock == currentBlock -> false
        else -> replaceChartBlock(blockId, updatedBlock)
    }
}

private fun NoteEditorViewModel.replaceChartBlock(blockId: String, block: EditorBlock.ChartBlock): Boolean {
    if (!uiStateInternal.value.isEditable) return false
    val current = uiStateInternal.value
    if (current.document.blocks.none { it.id == blockId }) return false
    uiStateInternal.value = current.copy(
        document = current.document.copy(
            blocks = current.document.blocks.map { existing ->
                if (existing.id == blockId) block.normalized() else existing
            }
        )
    )
    scheduleAutoSave()
    return true
}

private fun NoteEditorViewModel.currentChartBlock(blockId: String): EditorBlock.ChartBlock? =
    uiStateInternal.value.document.blocks.firstOrNull { it.id == blockId } as? EditorBlock.ChartBlock

private fun emptyChartCell(): List<RichText> = listOf(RichText(""))

private fun emptyChartRow(columnCount: Int): List<List<RichText>> = List(columnCount) { emptyChartCell() }
