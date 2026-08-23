package com.example.notesapp.ui.editor.model

import com.example.notesapp.ui.editor.mapper.ChartColumnOption
import com.example.notesapp.ui.editor.mapper.ChartData
import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.columnOptions
import com.example.notesapp.ui.editor.mapper.normalized

/** Presentation data prepared before the chart card is composed. */
data class ChartBlockCardModel(
    val block: EditorBlock.ChartBlock,
    val chartData: ChartData,
    val columnOptions: List<ChartColumnOption>
) {
    companion object {
        fun from(block: EditorBlock.ChartBlock): ChartBlockCardModel {
            val normalizedBlock = block.normalized()
            return ChartBlockCardModel(
                block = normalizedBlock,
                chartData = ChartTableParser.parse(normalizedBlock),
                columnOptions = normalizedBlock.columnOptions()
            )
        }
    }
}
