package com.example.notesapp.ui.editor.mapper

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
    val selectedColumnId: String,
    val selectedColumnIndex: Int,
    val selectedColumnHeader: String?,
    val points: List<ChartPoint>
)

data class ChartColumnOption(
    val id: String,
    val index: Int,
    val fallbackPosition: Int
)

object ChartTableParser {
    fun parse(block: EditorBlock.ChartBlock): ChartData {
        val normalizedBlock = block.normalized()
        val columnIds = normalizedBlock.columnIds
        val selectedColumnId = normalizedBlock.selectedColumnId
        val selectedColumnIndex = columnIds.indexOf(selectedColumnId).takeIf { it > 0 } ?: 1
        val selectedColumnHeader = normalizedBlock.rows.firstOrNull()
            ?.getOrNull(selectedColumnIndex)
            ?.cellText()
            ?.ifBlank { null }
        val points = if (selectedColumnIndex < 1) {
            emptyList()
        } else {
            normalizedBlock.rows.drop(1).mapIndexedNotNull { offset, row ->
                val category = row.getOrNull(0).cellText().orEmpty().trim()
                val valueText = row.getOrNull(selectedColumnIndex).cellText().orEmpty().trim()
                val value = valueText.toFloatOrNull()
                if (category.isBlank() || value == null || !value.isFinite()) {
                    null
                } else {
                    ChartPoint(category = category, value = value, rowIndex = offset + 1)
                }
            }.filter { point -> normalizedBlock.chartType != ChartType.PIE || point.value > 0f }
        }
        return ChartData(
            chartType = normalizedBlock.chartType,
            title = normalizedBlock.title.ifBlank { DEFAULT_CHART_TITLE },
            selectedColumnId = selectedColumnId,
            selectedColumnIndex = selectedColumnIndex,
            selectedColumnHeader = selectedColumnHeader,
            points = points
        )
    }
}

fun EditorBlock.ChartBlock.normalized(): EditorBlock.ChartBlock {
    val columnCount = maxOf(2, columnIds.size, rows.maxOfOrNull { it.size } ?: 0)
    val normalizedColumnIds = normalizedColumnIds(columnIds, columnCount)
    val normalizedRows = (rows.ifEmpty { defaultChartRowsForParser() }).map { row ->
        (0 until columnCount).map { index -> row.getOrNull(index) ?: emptyChartCell() }
    }
    return copy(
        rows = normalizedRows,
        columnIds = normalizedColumnIds,
        selectedColumnId = selectedColumnId.takeIf { it in normalizedColumnIds.drop(1) }
            ?: normalizedColumnIds[1]
    )
}

fun EditorBlock.ChartBlock.columnOptions(): List<ChartColumnOption> = normalized().columnIds
    .drop(1)
    .mapIndexed { offset, id ->
        ChartColumnOption(
            id = id,
            index = offset + 1,
            fallbackPosition = offset + 2
        )
    }

fun EditorBlock.ChartBlock.selectedDataColumnId(): String = normalized().selectedColumnId

fun EditorBlock.ChartBlock.nextColumnId(): String {
    val existing = normalized().columnIds.toSet()
    var suffix = existing.size + 1
    var candidate = "c_column_$suffix"
    while (candidate in existing) {
        suffix += 1
        candidate = "c_column_$suffix"
    }
    return candidate
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

fun EditorBlock.ChartBlock.toMarkdown(includeImage: Boolean = true, imageFailureMessage: String = ""): String {
    val chartData = ChartTableParser.parse(this)
    val header = rows.firstOrNull().orEmpty().take(columnIds.size).joinToString(" | ") { it.cellText() }
    val divider = columnIds.joinToString(" | ") { "---" }
    val body = rows.drop(1).joinToString("\n") { row ->
        "| " + columnIds.indices.joinToString(" | ") { index -> row.getOrNull(index).cellText() } + " |"
    }
    val title = chartData.title
    val sections = buildList {
        add("### $title (${chartType.storageValue})")
        if (includeImage) {
            add("![$title](${chartAssetPath(id)})")
        } else if (imageFailureMessage.isNotBlank()) {
            add("> $imageFailureMessage")
        }
        add("| $header |\n| $divider |\n$body")
    }
    return sections.joinToString("\n\n")
}

fun chartAssetPath(blockId: String): String {
    val safeId = blockId.filter { character ->
        character.isLetterOrDigit() || character == '.' || character == '_' || character == '-'
    }.ifBlank { "block" }
    return "assets/chart_$safeId.png"
}

private fun normalizedColumnIds(source: List<String>, columnCount: Int): List<String> {
    val used = mutableSetOf<String>()
    return (0 until columnCount).map { index ->
        val fallback = if (index == 0) "c_category" else "c_column_${index + 1}"
        var candidate = source.getOrNull(index)?.trim().orEmpty().ifBlank { fallback }
        var suffix = index + 1
        while (!used.add(candidate)) {
            suffix += 1
            candidate = if (index == 0) "c_category_$suffix" else "c_column_$suffix"
        }
        candidate
    }
}

private fun List<RichText>?.cellText(): String = this.orEmpty().joinToString("") { it.text }

private fun emptyTableRowForChart(columnCount: Int): List<List<RichText>> = List(columnCount) { listOf(RichText("")) }

private fun emptyChartCell(): List<RichText> = listOf(RichText(""))

private fun defaultChartRowsForParser(): List<List<List<RichText>>> = listOf(
    listOf(listOf(RichText("Category")), listOf(RichText("Value")))
)
