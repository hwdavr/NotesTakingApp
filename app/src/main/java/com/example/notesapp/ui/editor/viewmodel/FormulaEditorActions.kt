package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.components.InlineFormulaRenderer
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.INLINE_FORMULA_PLACEHOLDER
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.mergeAdjacentWithSameMarks
import com.example.notesapp.ui.editor.mapper.newInlineFormulaId
import com.example.notesapp.ui.editor.mapper.text

fun NoteEditorViewModel.openFormulaSheet() {
    if (!uiStateInternal.value.isEditable) return
    uiStateInternal.value = uiStateInternal.value.copy(
        formulaSheet = FormulaSheetUiState()
    )
}

fun NoteEditorViewModel.openFormulaSheetForEdit(blockId: String, inlineId: String) {
    if (!uiStateInternal.value.isEditable) return
    val formula = (
        uiStateInternal.value.document.blocks
            .firstOrNull { it.id == blockId } as? EditorBlock.TextBlock
        )
        ?.children
        ?.firstOrNull { it.inlineId == inlineId && it.isFormula }
        ?: return
    uiStateInternal.value = uiStateInternal.value.copy(
        formulaSheet = FormulaSheetUiState(
            source = formula.formulaSource.orEmpty(),
            editingBlockId = blockId,
            editingInlineId = inlineId
        )
    )
}

fun NoteEditorViewModel.updateFormulaSource(source: String) {
    val sheet = uiStateInternal.value.formulaSheet ?: return
    uiStateInternal.value = uiStateInternal.value.copy(
        formulaSheet = sheet.copy(source = source, hasValidationError = false)
    )
}

fun NoteEditorViewModel.cancelFormula() {
    if (uiStateInternal.value.formulaSheet == null) return
    uiStateInternal.value = uiStateInternal.value.copy(formulaSheet = null)
}

fun NoteEditorViewModel.submitFormula(): Boolean {
    val current = uiStateInternal.value
    val sheet = current.formulaSheet
    return when {
        !current.isEditable || sheet == null -> false
        else -> submitFormulaDraft(current, sheet)
    }
}

private fun NoteEditorViewModel.submitFormulaDraft(current: NoteEditorUiState, sheet: FormulaSheetUiState): Boolean {
    val rendered = InlineFormulaRenderer.render(sheet.source)
    if (!rendered.isValid) {
        uiStateInternal.value = current.copy(
            formulaSheet = sheet.copy(hasValidationError = true)
        )
        return false
    }

    return if (sheet.isEditing) {
        updateExistingFormula(current, sheet, rendered.source)
    } else {
        insertNewFormula(current, rendered.source)
    }
}

private fun NoteEditorViewModel.updateExistingFormula(
    current: NoteEditorUiState,
    sheet: FormulaSheetUiState,
    source: String
): Boolean {
    val editingBlockId = sheet.editingBlockId ?: return false
    val editingInlineId = sheet.editingInlineId ?: return false
    val blockIndex = current.document.blocks.indexOfFirst { it.id == editingBlockId }
    val block = current.document.blocks.getOrNull(blockIndex) as? EditorBlock.TextBlock
        ?: return false
    val childIndex = block.children.indexOfFirst { it.inlineId == editingInlineId && it.isFormula }
    if (childIndex < 0) return false

    val updatedChildren = block.children.toMutableList().apply {
        val existing = this[childIndex]
        this[childIndex] = existing.copy(
            text = INLINE_FORMULA_PLACEHOLDER,
            formulaSource = source,
            inlineId = existing.inlineId ?: editingInlineId
        )
    }
    val updatedBlocks = current.document.blocks.toMutableList().apply {
        this[blockIndex] = block.copy(children = updatedChildren)
    }
    val formulaOffset = block.children.take(childIndex).sumOf { it.text.length }
    uiStateInternal.value = current.copy(
        document = current.document.copy(blocks = updatedBlocks),
        formulaSheet = null,
        focusedBlockId = block.id,
        selectionStart = formulaOffset + 1,
        selectionEnd = formulaOffset + 1
    )
    scheduleAutoSave()
    return true
}

private fun NoteEditorViewModel.insertNewFormula(current: NoteEditorUiState, source: String): Boolean {
    val focusedBlock = current.focusedBlockId
        ?.let { focusedId ->
            current.document.blocks.find { it.id == focusedId } as? EditorBlock.TextBlock
        }
    val formula = RichText(
        text = INLINE_FORMULA_PLACEHOLDER,
        formulaSource = source,
        inlineId = newInlineFormulaId()
    )
    if (focusedBlock == null) {
        val newBlock = EditorBlock.TextBlock(children = listOf(formula))
        uiStateInternal.value = current.copy(
            document = current.document.copy(blocks = current.document.blocks + newBlock),
            focusedBlockId = newBlock.id,
            selectionStart = 1,
            selectionEnd = 1,
            formulaSheet = null
        )
    } else {
        val (selectionStart, selectionEnd) = current.selectionRangeWithin(focusedBlock.text().length)
        val updatedBlock = focusedBlock.copy(
            children = focusedBlock.children.replaceRangeWithFormula(
                start = selectionStart,
                end = selectionEnd,
                formula = formula
            )
        )
        uiStateInternal.value = current.copy(
            document = current.document.copy(
                blocks = current.document.blocks.map { block ->
                    if (block.id == updatedBlock.id) updatedBlock else block
                }
            ),
            focusedBlockId = updatedBlock.id,
            selectionStart = selectionStart + 1,
            selectionEnd = selectionStart + 1,
            formulaSheet = null
        )
    }
    scheduleAutoSave()
    return true
}

private fun List<RichText>.replaceRangeWithFormula(start: Int, end: Int, formula: RichText): List<RichText> {
    if (isEmpty()) return listOf(formula)

    val selectionStart = minOf(start, end)
    val selectionEnd = maxOf(start, end)
    val updatedChildren = mutableListOf<RichText>()
    var currentOffset = 0
    var formulaInserted = false

    forEach { child ->
        val childStart = currentOffset
        val childEnd = childStart + child.text.length
        currentOffset = childEnd

        when {
            childEnd <= selectionStart -> updatedChildren += child
            childStart >= selectionEnd && selectionStart != selectionEnd -> {
                if (!formulaInserted) {
                    updatedChildren += formula
                    formulaInserted = true
                }
                updatedChildren += child
            }
            childStart >= selectionStart && childStart >= selectionEnd -> {
                if (!formulaInserted) {
                    updatedChildren += formula
                    formulaInserted = true
                }
                updatedChildren += child
            }
            else -> {
                val localStart = (selectionStart - childStart).coerceIn(0, child.text.length)
                val localEnd = (selectionEnd - childStart).coerceIn(0, child.text.length)
                if (localStart > 0) {
                    updatedChildren += child.copy(text = child.text.substring(0, localStart))
                }
                if (!formulaInserted) {
                    updatedChildren += formula
                    formulaInserted = true
                }
                if (localEnd < child.text.length) {
                    updatedChildren += child.copy(text = child.text.substring(localEnd))
                }
            }
        }
    }

    if (!formulaInserted) updatedChildren += formula
    return updatedChildren.mergeAdjacentWithSameMarks()
}

internal data class FormulaPlaceholderReplacement(
    val children: List<RichText>,
    val nextFormulaIndex: Int
)

internal fun List<RichText>.replaceFormulaPlaceholders(
    formulas: List<RichText>,
    startingFormulaIndex: Int = 0
): FormulaPlaceholderReplacement {
    if (formulas.isEmpty()) return FormulaPlaceholderReplacement(this, startingFormulaIndex)

    val updatedChildren = mutableListOf<RichText>()
    var formulaIndex = startingFormulaIndex
    forEach { child ->
        var segmentStart = 0
        while (segmentStart < child.text.length) {
            val markerIndex = child.text.indexOf(INLINE_FORMULA_PLACEHOLDER, segmentStart)
            if (markerIndex < 0) {
                updatedChildren += child.copy(text = child.text.substring(segmentStart))
                segmentStart = child.text.length
            } else {
                if (markerIndex > segmentStart) {
                    updatedChildren += child.copy(
                        text = child.text.substring(segmentStart, markerIndex)
                    )
                }
                val existingFormula = formulas.getOrNull(formulaIndex)
                if (existingFormula != null) {
                    val formulaMarks = child.marks.ifEmpty { existingFormula.marks }
                    updatedChildren += existingFormula.copy(
                        text = INLINE_FORMULA_PLACEHOLDER,
                        marks = formulaMarks
                    )
                    formulaIndex++
                } else {
                    updatedChildren += RichText(
                        text = INLINE_FORMULA_PLACEHOLDER,
                        marks = child.marks
                    )
                }
                segmentStart = markerIndex + INLINE_FORMULA_PLACEHOLDER.length
            }
        }
        if (child.text.isEmpty()) updatedChildren += child
    }
    return FormulaPlaceholderReplacement(
        children = updatedChildren,
        nextFormulaIndex = formulaIndex
    )
}
