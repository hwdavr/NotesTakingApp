package com.example.notesapp.ui.editor.chart

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.components.ChartBlockCard
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChartCreationFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testAllChartTypesRenderOfflineAndExposeDatumTargets() {
        val chartTypes = listOf(ChartType.BAR, ChartType.LINE, ChartType.PIE)
        composeRule.setContent {
            NotesTakingAppTheme {
                chartTypes.forEach { chartType ->
                    ChartBlockCard(
                        block = chartBlock(chartType),
                        isEditable = false,
                        onUpdateTitle = {},
                        onUpdateCell = { _, _, _ -> },
                        onSelectedColumnChange = {},
                        onDelete = {}
                    )
                }
            }
        }

        composeRule.onAllNodesWithTag("editor_chart_plot").assertCountEquals(3)
        composeRule.onAllNodesWithTag("editor_chart_datum_target").assertCountEquals(9)
    }

    private fun chartBlock(chartType: ChartType): EditorBlock.ChartBlock = EditorBlock.ChartBlock(
        id = chartType.storageValue,
        chartType = chartType,
        title = "Offline chart",
        rows = listOf(
            row("Category", "Value"),
            row("A", "10"),
            row("B", "20"),
            row("C", "15")
        )
    )

    private fun row(category: String, value: String): List<List<RichText>> =
        listOf(listOf(RichText(category)), listOf(RichText(value)))
}
