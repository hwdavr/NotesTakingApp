package com.example.notesapp.ui.editor.chart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.components.ChartBlockCard
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.model.ChartTableAction
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChartDataFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testCurrentViewSwitchesBetweenChartAndData() {
        setChartContent()

        composeRule.onNodeWithTag("editor_chart_plot").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta").performClick()
        composeRule.onNodeWithTag("editor_chart_view_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_option_data").performClick()

        composeRule.onNodeWithTag("editor_chart_table_view").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_grid").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_plot").assertCountEquals(0)
        composeRule.onNodeWithTag("editor_chart_view_cta").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta").assertIsDisplayed()
    }

    @Test
    fun testOptionsOpensDataColumnSecondLevelAndSelectsColumn() {
        setChartContent(initialBlock = chartBlockWithMultipleColumns())

        composeRule.onNodeWithTag("editor_chart_options_cta").performClick()
        composeRule.onNodeWithTag("editor_chart_options_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_option_data_column").performClick()
        composeRule.onNodeWithTag("editor_chart_data_column_sheet").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Cost").performClick()

        composeRule.onAllNodesWithTag("editor_chart_data_column_sheet").assertCountEquals(0)
        composeRule.onAllNodesWithTag("editor_chart_options_sheet").assertCountEquals(0)
        composeRule.onNodeWithText("Cost").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_data_grid").assertCountEquals(0)
        composeRule.onNodeWithTag("editor_chart_plot").assertIsDisplayed()
    }

    private fun setChartContent(initialBlock: EditorBlock.ChartBlock = chartBlock()) {
        var currentBlock by mutableStateOf(initialBlock)
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    block = currentBlock,
                    isEditable = true,
                    onUpdateTitle = { value -> currentBlock = currentBlock.copy(title = value) },
                    onUpdateCell = { rowIndex, columnIndex, value ->
                        currentBlock = currentBlock.copy(
                            rows = currentBlock.rows.mapIndexed { currentRow, row ->
                                if (currentRow != rowIndex) {
                                    row
                                } else {
                                    row.mapIndexed { currentColumn, cell ->
                                        if (currentColumn == columnIndex) listOf(RichText(value)) else cell
                                    }
                                }
                            }
                        )
                    },
                    onSelectedColumnChange = { columnId ->
                        currentBlock = currentBlock.copy(selectedColumnId = columnId)
                    },
                    onDelete = {},
                    onAddRow = {
                        currentBlock = currentBlock.copy(
                            rows = currentBlock.rows + listOf(
                                List(currentBlock.columnIds.size) { listOf(RichText("")) }
                            )
                        )
                    },
                    onAddColumn = {
                        currentBlock = currentBlock.copy(
                            columnIds = currentBlock.columnIds + "c_column_4",
                            rows = currentBlock.rows.map { row -> row + listOf(listOf(RichText(""))) }
                        )
                    },
                    onTableAction = { action ->
                        when (action) {
                            is ChartTableAction.InsertColumnLeft,
                            is ChartTableAction.InsertColumnRight,
                            is ChartTableAction.DeleteColumn,
                            is ChartTableAction.ClearColumn,
                            is ChartTableAction.InsertRowAbove,
                            is ChartTableAction.InsertRowBelow,
                            is ChartTableAction.DeleteRow,
                            is ChartTableAction.ClearRow -> Unit
                        }
                    }
                )
            }
        }
    }

    private fun chartBlock(): EditorBlock.ChartBlock = EditorBlock.ChartBlock(
        id = "chart-flow",
        chartType = ChartType.BAR,
        title = "Monthly sales",
        columnIds = listOf("c_category", "c_revenue"),
        selectedColumnId = "c_revenue",
        rows = listOf(row("Category", "Revenue"), row("January", "120"))
    )

    private fun chartBlockWithMultipleColumns(): EditorBlock.ChartBlock = chartBlock().copy(
        columnIds = listOf("c_category", "c_revenue", "c_cost"),
        rows = listOf(
            row("Category", "Revenue", "Cost"),
            row("January", "120", "80")
        )
    )

    private fun row(vararg values: String): List<List<RichText>> {
        return values.map { value -> listOf(RichText(value)) }
    }
}
