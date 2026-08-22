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
                        block = chartBlock(chartType),
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

        composeRule.onAllNodesWithTag("editor_chart_datum_target").assertCountEquals(9)
        listOf(0, 3, 6).forEach { targetIndex ->
            composeRule.onAllNodesWithTag("editor_chart_datum_target")
                .get(targetIndex)
                .performClick()
            composeRule.onAllNodesWithTag("editor_chart_tooltip").assertCountEquals(1)
            composeRule.onNodeWithContentDescription("A · Value · 10.0").assertIsDisplayed()
            composeRule.onNodeWithTag("editor_chart_tooltip_dismiss").performClick()
            composeRule.onAllNodesWithTag("editor_chart_tooltip").assertCountEquals(0)
        }
    }

    @Test
    fun testReadOnlyChartKeepsInspectionAndDisablesMutations() {
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    block = chartBlock(ChartType.BAR).copy(
                        columnIds = listOf("category", "value", "cost"),
                        rows = listOf(
                            row("Category", "Value", "Cost"),
                            row("A", "10", "8")
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

        composeRule.onNodeWithTag("editor_chart_view_cta").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data").performClick()
        composeRule.onNodeWithTag("editor_chart_data_grid").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_data_cell").assertCountEquals(6)
        composeRule.onNodeWithTag("editor_chart_view_cta").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_chart").performClick()

        composeRule.onNodeWithTag("editor_chart_options_cta").performClick()
        composeRule.onNodeWithTag("editor_chart_option_data_column").performClick()
        composeRule.onNodeWithTag("editor_chart_data_column_sheet").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_option_column").assertCountEquals(2)
        composeRule.onAllNodesWithTag("editor_chart_option_column").get(0).assertIsNotEnabled()
        composeRule.onAllNodesWithTag("editor_chart_option_column").get(1).assertIsNotEnabled()
        composeRule.onNodeWithTag("editor_chart_data_column_back").performClick()
        composeRule.onNodeWithTag("editor_chart_add_row").assertIsNotEnabled()
        composeRule.onNodeWithTag("editor_chart_add_column").assertIsNotEnabled()
        composeRule.onNodeWithTag("editor_chart_options_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_delete").assertCountEquals(0)
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
                        block = chartBlock(ChartType.LINE),
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

        composeRule.onNodeWithTag("editor_chart_view_cta").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_plot").assertIsDisplayed()
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
