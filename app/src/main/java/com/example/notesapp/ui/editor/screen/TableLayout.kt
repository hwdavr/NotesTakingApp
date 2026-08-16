package com.example.notesapp.ui.editor.screen

import com.example.notesapp.ui.editor.mapper.EditorBlock

internal fun EditorBlock.TableBlock.tableColumnWeights(): List<Float> {
    val columnCount = rows.maxOfOrNull { row -> row.size } ?: return emptyList()
    return List(columnCount) { columnIndex ->
        if (fitToWidth) {
            1f
        } else {
            rows.maxOfOrNull { row ->
                row.getOrNull(columnIndex)?.joinToString(separator = "") { it.text }?.length ?: 0
            }?.coerceIn(1, 24)?.toFloat() ?: 1f
        }
    }
}
