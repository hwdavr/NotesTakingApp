package com.example.notesapp.ui.editor.chart

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.components.ChartBlockCard
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.model.ChartTableAction
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChartVisualFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun captureCompletedChartVisualStates() {
        setChartContent(chartBlock())
        composeRule.onNodeWithTag("editor_chart_block").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_plot").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta").assertIsDisplayed()
        assertChartLayout()
    }

    @Test
    fun captureChartPreviewState() {
        setChartContent(chartBlock())
        composeRule.onNodeWithTag("editor_chart_block").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_plot").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta").assertIsDisplayed()
        assertChartLayout()
        captureVisualEvidence("chart_preview")
    }

    @Test
    fun captureDataViewState() {
        setChartContent(chartBlockWithMultipleColumns())
        composeRule.onNodeWithTag("editor_chart_view_cta").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data").performClick()
        composeRule.onNodeWithTag("editor_chart_table_view").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_grid").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_hint").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_plot").assertCountEquals(0)
        assertDataLayout()
        captureVisualEvidence("chart_data_view")
    }

    @Test
    fun captureOptionsSheetsState() {
        setChartContent(chartBlockWithMultipleColumns())
        composeRule.onNodeWithTag("editor_chart_options_cta").performClick()
        composeRule.onNodeWithTag("editor_chart_options_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_option_data_column").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_add_row").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_add_column").assertIsDisplayed()
        assertHeaderLayout()
        composeRule.onNodeWithTag("editor_chart_option_data_column").performClick()
        composeRule.onNodeWithTag("editor_chart_data_column_sheet").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_option_column").assertCountEquals(2)
        captureVisualEvidence("chart_options_sheets")
    }

    @Test
    fun captureEmptyAndSelectedStates() {
        val blockState = mutableStateOf(
            chartBlock().copy(
                id = "visual-empty-selected",
                rows = listOf(row("Category", "Value"), row("January", "invalid"))
            )
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    block = blockState.value,
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
        composeRule.onNodeWithTag("editor_chart_empty").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta").assertIsDisplayed()
        assertWithinChartHost("editor_chart_empty")
        captureVisualEvidence("chart_empty_state")

        composeRule.runOnIdle { blockState.value = chartBlock().copy(id = "visual-selected") }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag("editor_chart_datum_target").get(1).performClick()
        composeRule.onNodeWithTag("editor_chart_tooltip").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_tooltip_dismiss").assertIsDisplayed()
        assertChartLayout()
        captureVisualEvidence("chart_empty_selected")
    }

    @Test
    fun captureReadOnlyDarkState() {
        composeRule.setContent {
            NotesTakingAppTheme(darkTheme = true) {
                ChartBlockCard(
                    block = chartBlock(),
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
        composeRule.onNodeWithTag("editor_chart_table_view").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_delete").assertCountEquals(0)
        assertDataLayout()
        captureVisualEvidence("chart_read_only_dark")
    }

    private fun setChartContent(block: EditorBlock.ChartBlock) {
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    block = block,
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

    private fun assertChartLayout() {
        assertHeaderLayout()
        val cardBounds = composeRule.onNodeWithTag("editor_chart_block").getUnclippedBoundsInRoot()
        val plotBounds = composeRule.onNodeWithTag("editor_chart_plot").getUnclippedBoundsInRoot()
        assertTrue(plotBounds.left.value >= cardBounds.left.value)
        assertTrue(plotBounds.right.value <= cardBounds.right.value)
    }

    private fun assertDataLayout() {
        assertHeaderLayout()
        val cardBounds = composeRule.onNodeWithTag("editor_chart_block").getUnclippedBoundsInRoot()
        val gridBounds = composeRule.onNodeWithTag("editor_chart_data_grid").getUnclippedBoundsInRoot()
        assertTrue(gridBounds.left.value >= cardBounds.left.value)
        assertTrue(gridBounds.right.value <= cardBounds.right.value)
    }

    private fun assertHeaderLayout() {
        val viewBounds = composeRule.onNodeWithTag("editor_chart_view_cta").getUnclippedBoundsInRoot()
        val optionsBounds = composeRule.onNodeWithTag("editor_chart_options_cta").getUnclippedBoundsInRoot()
        assertTrue(viewBounds.height.value >= 48f)
        assertTrue(optionsBounds.height.value >= 48f)
        assertEquals(viewBounds.top.value, optionsBounds.top.value, 2f)
    }

    private fun assertWithinChartHost(tag: String) {
        val hostBounds = composeRule.onNodeWithTag("editor_chart_block").getUnclippedBoundsInRoot()
        val bounds = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
        assertTrue(bounds.left.value >= hostBounds.left.value - 4f)
        assertTrue(bounds.right.value <= hostBounds.right.value + 4f)
    }

    private fun captureVisualEvidence(fileName: String) {
        composeRule.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("Could not capture visual evidence: $fileName")
        val directory = InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir("visual_evidence")
            ?: error("External files directory unavailable")
        directory.mkdirs()
        val screenshot = File(directory, "$fileName.png")
        screenshot.outputStream().use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Could not write visual evidence: $fileName"
            }
        }
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("cp ${screenshot.absolutePath} /sdcard/Download/$fileName.png")
            .use { }
    }

    private fun chartBlock(): EditorBlock.ChartBlock = EditorBlock.ChartBlock(
        id = "visual-chart",
        chartType = ChartType.BAR,
        title = "Monthly Sales",
        columnIds = listOf("category", "value"),
        selectedColumnId = "value",
        rows = listOf(
            row("Category", "Value"),
            row("January", "120"),
            row("February", "180"),
            row("March", "140"),
            row("April", "220")
        )
    )

    private fun chartBlockWithMultipleColumns(): EditorBlock.ChartBlock = chartBlock().copy(
        columnIds = listOf("category", "revenue", "cost"),
        rows = listOf(
            row("Category", "Revenue", "Cost"),
            row("January", "120", "80"),
            row("February", "180", "110")
        )
    )

    private fun row(vararg values: String): List<List<RichText>> = values.map { value ->
        listOf(RichText(value))
    }
}
