package com.example.notesapp.ui.editor.mapper

import kotlin.math.max

enum class ChartType(val storageValue: String) {
    BAR("bar"),
    LINE("line"),
    PIE("pie");

    companion object {
        fun fromStorageValue(value: String): ChartType = entries.firstOrNull { it.storageValue == value } ?: BAR
    }
}

data class ChartPoint(
    val category: String,
    val value: Float,
    val rowIndex: Int
)

data class ChartData(
    val chartType: ChartType,
    val title: String,
    val selectedColumnId: String?,
    val selectedColumnLabel: String,
    val points: List<ChartPoint>
)

object ChartTableParser {
    fun parse(block: EditorBlock.ChartBlock): ChartData {
        val columnIds = block.columnIds.normalizedFor(block.rows)
        val selectedColumnId = block.selectedColumnId.takeIf { it in columnIds.drop(1) }
        val selectedColumnIndex = columnIds.indexOf(selectedColumnId).takeIf { it > 0 } ?: -1
        val selectedColumnLabel = block.rows.firstOrNull()
            ?.getOrNull(selectedColumnIndex)
            ?.cellText()
            ?.ifBlank { null }
            ?: positionalColumnLabel(selectedColumnIndex, columnIds.size)
        val points = if (selectedColumnIndex < 1) {
            emptyList()
        } else {
            block.rows.drop(1).mapIndexedNotNull { offset, row ->
                val category = row.getOrNull(0).cellText().orEmpty().trim()
                val valueText = row.getOrNull(selectedColumnIndex).cellText().orEmpty().trim()
                val value = valueText.toFloatOrNull()
                if (category.isBlank() || value == null) {
                    null
                } else {
                    ChartPoint(category = category, value = value, rowIndex = offset + 1)
                }
            }.filter { point -> block.chartType != ChartType.PIE || point.value > 0f }
        }
        return ChartData(
            chartType = block.chartType,
            title = block.title.ifBlank { DEFAULT_CHART_TITLE },
            selectedColumnId = selectedColumnId,
            selectedColumnLabel = selectedColumnLabel,
            points = points
        )
    }

    fun positionalColumnLabel(columnIndex: Int, columnCount: Int): String {
        val safeIndex = if (columnIndex > 0) columnIndex else max(1, columnCount - 1)
        return "Column ${safeIndex + 1}"
    }
}

fun EditorBlock.TableBlock.toChartBlock(chartType: ChartType): EditorBlock.ChartBlock {
    val columnCount = maxOf(2, rows.maxOfOrNull { it.size } ?: 0)
    val normalizedRows = (rows.ifEmpty { listOf(emptyTableRowForChart(columnCount)) }).map { row ->
        (0 until columnCount).map { index -> row.getOrNull(index) ?: listOf(RichText("")) }
    }
    val columnIds = (0 until columnCount).map { index ->
        if (index == 0) "c_category" else "c_column_${index + 1}"
    }
    return EditorBlock.ChartBlock(
        id = id,
        chartType = chartType,
        title = DEFAULT_CHART_TITLE,
        rows = normalizedRows,
        columnIds = columnIds,
        selectedColumnId = columnIds[1]
    )
}

fun EditorBlock.ChartBlock.toMarkdown(): String {
    val chartData = ChartTableParser.parse(this)
    val header = rows.firstOrNull().orEmpty().take(columnIds.size).joinToString(" | ") { it.cellText() }
    val divider = columnIds.joinToString(" | ") { "---" }
    val body = rows.drop(1).joinToString("\n") { row ->
        "| " + columnIds.indices.joinToString(" | ") { index -> row.getOrNull(index).cellText() } + " |"
    }
    val imagePath = "assets/chart_$id.png"
    val title = chartData.title
    return "### $title (${chartType.storageValue})\n\n![$title]($imagePath)\n\n| $header |\n| $divider |\n$body"
}

private fun List<String>.normalizedFor(rows: List<List<List<RichText>>>): List<String> {
    val count = maxOf(2, size, rows.maxOfOrNull { it.size } ?: 0)
    return (0 until count).map { index ->
        getOrNull(index)?.ifBlank { null } ?: if (index == 0) "c_category" else "c_column_${index + 1}"
    }
}

private fun List<RichText>?.cellText(): String = this.orEmpty().joinToString("") { it.text }

private fun emptyTableRowForChart(columnCount: Int): List<List<RichText>> = List(columnCount) { listOf(RichText("")) }
