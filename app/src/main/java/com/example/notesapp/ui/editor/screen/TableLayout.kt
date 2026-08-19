package com.example.notesapp.ui.editor.screen

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.editor.mapper.EditorBlock

internal val TableColumnMinWidth: Dp = 96.dp

internal val TableHandleGutterWidth: Dp = 24.dp

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

/**
 * Converts relative column weights into fixed column widths. Columns keep their
 * proportional share of the available viewport width, but never shrink below
 * [TableColumnMinWidth]. When too many columns are present, the sum exceeds the
 * viewport and the table becomes horizontally scrollable instead of squeezing
 * every cell into unreadable slivers.
 */
internal fun tableColumnWidths(weights: List<Float>, viewportWidth: Dp): List<Dp> {
    if (weights.isEmpty()) return emptyList()
    val totalWeight = weights.sum()
    return weights.map { weight ->
        maxOf(viewportWidth * (weight / totalWeight), TableColumnMinWidth)
    }
}
