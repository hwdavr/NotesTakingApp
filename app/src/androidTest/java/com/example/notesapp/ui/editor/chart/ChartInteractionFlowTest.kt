package com.example.notesapp.ui.editor.chart

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.components.ChartBlockCard
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.model.ChartBlockCardModel
import com.example.notesapp.ui.editor.model.ChartTableAction
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChartInteractionFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testDatumSelectionShowsAndDismissesCalloutForAllTypes() {
        composeRule.setContent {
            NotesTakingAppTheme {
                listOf(ChartType.BAR, ChartType.LINE, ChartType.PIE).forEach { chartType ->
                    ChartBlockCard(
                        model = ChartBlockCardModel.from(chartBlock(chartType)),
                        isEditable = true,
                        onUpdateTitle = {},
                        onUpdateCell = { _, _, _ -> },
                        onSelectedColumnChange = {},
                        onDelete = {},
                        onAddRow = {},
                        onAddColumn = {},
                        onTableAction = { _: ChartTableAction -> }
                    )
                }
            }
        }

        listOf(ChartType.BAR, ChartType.LINE, ChartType.PIE).forEach { chartType ->
            val blockId = "chart-interaction-${chartType.storageValue}"
            composeRule.onAllNodesWithTag("editor_chart_datum_target_1_$blockId").assertCountEquals(1)
            composeRule.onAllNodesWithTag("editor_chart_datum_target_1_$blockId").get(0).performClick()
            composeRule.onAllNodesWithTag("editor_chart_tooltip_$blockId").assertCountEquals(1)
            composeRule.onNodeWithContentDescription("A · Value · 10.0").assertIsDisplayed()
            composeRule.onNodeWithContentDescription("Data point A, value 10.0. Selected.").assertIsDisplayed()
            composeRule.onNodeWithTag("editor_chart_tooltip_dismiss_$blockId").performClick()
            composeRule.onAllNodesWithTag("editor_chart_tooltip_$blockId").assertCountEquals(0)
        }
    }

    @Test
    fun testReadOnlyChartKeepsInspectionAndDisablesMutations() {
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    model = ChartBlockCardModel.from(
                        chartBlock(ChartType.BAR).copy(
                            columnIds = listOf("category", "value", "cost"),
                            rows = listOf(
                                row("Category", "Value", "Cost"),
                                row("A", "10", "8")
                            )
                        )
                    ),
                    isEditable = false,
                    onUpdateTitle = {},
                    onUpdateCell = { _, _, _ -> },
                    onSelectedColumnChange = {},
                    onDelete = {},
                    onAddRow = {},
                    onAddColumn = {},
                    onTableAction = { _: ChartTableAction -> }
                )
            }
        }

        val readOnlyId = "chart-interaction-bar"
        composeRule.onNodeWithTag("editor_chart_view_cta_$readOnlyId").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data_$readOnlyId").performClick()
        composeRule.onNodeWithTag("editor_chart_data_grid_$readOnlyId").assertIsDisplayed()
        listOf("category", "value", "cost").forEach { columnId ->
            composeRule.onAllNodesWithTag("editor_chart_data_cell_${columnId}_$readOnlyId")
                .assertCountEquals(2)
        }
        composeRule.onNodeWithTag("editor_chart_view_cta_$readOnlyId").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_chart_$readOnlyId").performClick()

        composeRule.onNodeWithTag("editor_chart_options_cta_$readOnlyId").performClick()
        composeRule.onNodeWithTag("editor_chart_option_data_column_$readOnlyId").performClick()
        composeRule.onNodeWithTag("editor_chart_data_column_sheet_$readOnlyId").assertIsDisplayed()
        listOf("value", "cost").forEach { columnId ->
            composeRule.onNodeWithTag("editor_chart_option_column_${columnId}_$readOnlyId")
                .assertIsNotEnabled()
        }
        composeRule.onNodeWithTag("editor_chart_data_column_back_$readOnlyId").performClick()
        composeRule.onNodeWithTag("editor_chart_add_row_$readOnlyId").assertIsNotEnabled()
        composeRule.onNodeWithTag("editor_chart_add_column_$readOnlyId").assertIsNotEnabled()
        composeRule.onNodeWithTag("editor_chart_options_sheet_$readOnlyId").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta_$readOnlyId").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_delete_$readOnlyId").assertCountEquals(0)
    }

    @Test
    fun testDarkThemeLargeTextAndRtlChartSemantics() {
        composeRule.setContent {
            NotesTakingAppTheme(darkTheme = true) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl,
                    LocalDensity provides Density(1f, 1.3f)
                ) {
                    ChartBlockCard(
                        model = ChartBlockCardModel.from(chartBlock(ChartType.LINE)),
                        isEditable = false,
                        onUpdateTitle = {},
                        onUpdateCell = { _, _, _ -> },
                        onSelectedColumnChange = {},
                        onDelete = {},
                        onAddRow = {},
                        onAddColumn = {},
                        onTableAction = { _: ChartTableAction -> }
                    )
                }
            }
        }

        val blockId = "chart-interaction-line"
        composeRule.onNodeWithTag("editor_chart_view_cta_$blockId").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta_$blockId").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_plot_$blockId").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Chart options").assertIsDisplayed()
    }

    private fun chartBlock(chartType: ChartType): EditorBlock.ChartBlock = EditorBlock.ChartBlock(
        id = "chart-interaction-${chartType.storageValue}",
        chartType = chartType,
        title = "Sales",
        columnIds = listOf("category", "value"),
        selectedColumnId = "value",
        rows = listOf(
            row("Category", "Value"),
            row("A", "10"),
            row("B", "20"),
            row("C", "15")
        )
    )

    private fun row(vararg values: String): List<List<RichText>> {
        return values.map { value -> listOf(RichText(value)) }
    }
}
