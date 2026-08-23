package com.example.notesapp.ui.editor.chart

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.components.ChartBlockCard
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.model.ChartBlockCardModel
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
        composeRule.onNodeWithTag("editor_chart_block_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_plot_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta_visual-chart").assertIsDisplayed()
        assertChartLayout("visual-chart")
    }

    @Test
    fun captureChartPreviewState() {
        setChartContent(chartBlock())
        composeRule.onNodeWithTag("editor_chart_block_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_plot_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta_visual-chart").assertIsDisplayed()
        assertChartLayout("visual-chart")
        captureVisualEvidence("chart_preview")
    }

    @Test
    fun captureDataViewState() {
        setChartContent(chartBlockWithMultipleColumns())
        composeRule.onNodeWithTag("editor_chart_view_cta_visual-chart").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data_visual-chart").performClick()
        composeRule.onNodeWithTag("editor_chart_table_view_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_grid_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_hint_visual-chart").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_plot_visual-chart").assertCountEquals(0)
        composeRule.onNodeWithText("January").assertIsDisplayed()
        composeRule.onNodeWithText("120").assertIsDisplayed()
        composeRule.onNodeWithText("80").assertIsDisplayed()
        assertDataLayout("visual-chart")
        captureVisualEvidence("chart_data_view")
    }

    @Test
    fun captureOptionsSheetsState() {
        setChartContent(chartBlockWithMultipleColumns())
        composeRule.onNodeWithTag("editor_chart_options_cta_visual-chart").performClick()
        composeRule.onNodeWithTag("editor_chart_options_sheet_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_option_data_column_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_add_row_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_add_column_visual-chart").assertIsDisplayed()
        assertHeaderLayout("visual-chart")
        captureVisualEvidence("chart_options_sheets")
        composeRule.onNodeWithTag("editor_chart_option_data_column_visual-chart").performClick()
        composeRule.onNodeWithTag("editor_chart_data_column_sheet_visual-chart").assertIsDisplayed()
        listOf("revenue", "cost").forEach { columnId ->
            composeRule.onNodeWithTag("editor_chart_option_column_${columnId}_visual-chart")
                .assertIsDisplayed()
        }
        captureVisualEvidence("chart_data_column_sheet")
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
                    model = ChartBlockCardModel.from(blockState.value),
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
        composeRule.onNodeWithTag("editor_chart_empty_visual-empty-selected").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta_visual-empty-selected").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta_visual-empty-selected").assertIsDisplayed()
        assertWithinChartHost("visual-empty-selected", "editor_chart_empty_visual-empty-selected")
        captureVisualEvidence("chart_empty_state")

        composeRule.runOnIdle { blockState.value = chartBlock().copy(id = "visual-selected") }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_chart_datum_target_2_visual-selected").performClick()
        composeRule.onNodeWithTag("editor_chart_tooltip_visual-selected").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_tooltip_dismiss_visual-selected").assertIsDisplayed()
        assertTooltipLayout("visual-selected")
        assertChartLayout("visual-selected")
        captureVisualEvidence("chart_empty_selected")
    }

    @Test
    fun captureReadOnlyDarkState() {
        composeRule.setContent {
            NotesTakingAppTheme(darkTheme = true) {
                ChartBlockCard(
                    model = ChartBlockCardModel.from(chartBlock()),
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
        composeRule.onNodeWithTag("editor_chart_view_cta_visual-chart").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data_visual-chart").performClick()
        composeRule.onNodeWithTag("editor_chart_table_view_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta_visual-chart").assertIsDisplayed()
        composeRule.onAllNodesWithTag("editor_chart_delete_visual-chart").assertCountEquals(0)
        assertDataLayout("visual-chart")
        composeRule.onNodeWithText("January").assertIsDisplayed()
        captureVisualEvidence("chart_read_only_dark")
    }

    @Test
    fun rendererFailureKeepsRecoveryControlsAndDataAccessible() {
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    model = ChartBlockCardModel.from(chartBlock()),
                    isEditable = true,
                    onUpdateTitle = {},
                    onUpdateCell = { _, _, _ -> },
                    onSelectedColumnChange = {},
                    onDelete = {},
                    onAddRow = {},
                    onAddColumn = {},
                    onTableAction = { _: ChartTableAction -> },
                    bitmapRenderer = { _, _, _ -> Result.failure(IllegalStateException("test renderer failure")) }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_chart_error_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_options_cta_visual-chart").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_view_cta_visual-chart").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data_visual-chart").assertIsDisplayed()
    }

    @Test
    fun headerOnlyAndAllZeroStatesRemainRecoverableForEveryChartType() {
        val blockState = mutableStateOf(chartBlock())
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    model = ChartBlockCardModel.from(blockState.value),
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
        listOf(ChartType.BAR, ChartType.LINE, ChartType.PIE).forEach { chartType ->
            val headerOnlyId = "visual-header-only-${chartType.storageValue}"
            composeRule.runOnIdle {
                blockState.value = chartBlock().copy(
                    id = headerOnlyId,
                    chartType = chartType,
                    rows = listOf(row("Category", "Value"))
                )
            }
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("editor_chart_empty_$headerOnlyId").assertIsDisplayed()
            composeRule.onNodeWithTag("editor_chart_view_cta_$headerOnlyId").assertIsDisplayed()
            composeRule.onNodeWithTag("editor_chart_options_cta_$headerOnlyId").assertIsDisplayed()

            val allZeroId = "visual-all-zero-${chartType.storageValue}"
            composeRule.runOnIdle {
                blockState.value = chartBlock().copy(
                    id = allZeroId,
                    chartType = chartType,
                    rows = listOf(
                        row("Category", "Value"),
                        row("January", "0"),
                        row("February", "0")
                    )
                )
            }
            composeRule.waitForIdle()
            if (chartType == ChartType.PIE) {
                composeRule.onNodeWithTag("editor_chart_empty_$allZeroId").assertIsDisplayed()
            } else {
                composeRule.onNodeWithTag("editor_chart_plot_$allZeroId").assertIsDisplayed()
                composeRule.onNodeWithTag("editor_chart_datum_target_1_$allZeroId").assertIsDisplayed()
                composeRule.onNodeWithTag("editor_chart_datum_target_2_$allZeroId").assertIsDisplayed()
            }
        }
    }

    @Test
    fun emptyChartDataViewDoesNotCrashAndShowsRecoveryHint() {
        // Regression for harness-retro-2026-08-23-chart-empty-data-view:
        // switching an empty/header-only chart to Data view crashed with
        // "infinite maximum height constraints" because heightIn(max) was
        // applied after verticalScroll in ChartDataTable.
        val blockState = mutableStateOf(
            chartBlock().copy(
                id = "reg-empty-hdr",
                rows = listOf(row("Category", "Value"))
            )
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    model = ChartBlockCardModel.from(blockState.value),
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
        composeRule.onNodeWithTag("editor_chart_view_cta_reg-empty-hdr").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data_reg-empty-hdr").performClick()
        composeRule.onNodeWithTag("editor_chart_table_view_reg-empty-hdr").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_grid_reg-empty-hdr").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_empty_hint_reg-empty-hdr").assertIsDisplayed()
        assertDataLayout("reg-empty-hdr")

        composeRule.runOnIdle {
            blockState.value = chartBlock().copy(
                id = "reg-empty-row",
                rows = listOf(row("Category", "Value"), row("", ""))
            )
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_chart_view_cta_reg-empty-row").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data_reg-empty-row").performClick()
        composeRule.onNodeWithTag("editor_chart_table_view_reg-empty-row").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_grid_reg-empty-row").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_chart_data_hint_reg-empty-row").assertIsDisplayed()
        assertDataLayout("reg-empty-row")
    }

    @Test
    fun largeDataViewRemainsBoundedAndScrollable() {
        val rows = buildList {
            add(row("Category", "Value"))
            repeat(200) { index -> add(row("Row $index", "${index + 1}")) }
        }
        setChartContent(
            EditorBlock.ChartBlock(
                id = "large-chart",
                chartType = ChartType.BAR,
                columnIds = listOf("category", "value"),
                selectedColumnId = "value",
                rows = rows
            )
        )
        composeRule.onNodeWithTag("editor_chart_view_cta_large-chart").performClick()
        composeRule.onNodeWithTag("editor_chart_view_option_data_large-chart").performClick()
        val grid = composeRule.onNodeWithTag("editor_chart_data_grid_large-chart")
        assertTrue(grid.getUnclippedBoundsInRoot().height.value <= 362f)
        repeat(40) {
            grid.performTouchInput { swipeUp() }
        }
        composeRule.onNodeWithTag("editor_chart_data_row_200_large-chart").assertExists()
    }

    private fun setChartContent(block: EditorBlock.ChartBlock) {
        composeRule.setContent {
            NotesTakingAppTheme {
                ChartBlockCard(
                    model = ChartBlockCardModel.from(block),
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

    private fun assertChartLayout(blockId: String) {
        assertHeaderLayout(blockId)
        val cardBounds = composeRule.onNodeWithTag("editor_chart_block_$blockId").getUnclippedBoundsInRoot()
        val plotBounds = composeRule.onNodeWithTag("editor_chart_plot_$blockId").getUnclippedBoundsInRoot()
        assertTrue(plotBounds.left.value >= cardBounds.left.value)
        assertTrue(plotBounds.right.value <= cardBounds.right.value)
    }

    private fun assertDataLayout(blockId: String) {
        assertHeaderLayout(blockId)
        val cardBounds = composeRule.onNodeWithTag("editor_chart_block_$blockId").getUnclippedBoundsInRoot()
        val gridBounds = composeRule.onNodeWithTag("editor_chart_data_grid_$blockId").getUnclippedBoundsInRoot()
        assertTrue(gridBounds.left.value >= cardBounds.left.value)
        assertTrue(gridBounds.right.value <= cardBounds.right.value)
        assertTrue(gridBounds.height.value <= 360f + 2f)
    }

    private fun assertHeaderLayout(blockId: String) {
        val viewBounds = composeRule.onNodeWithTag("editor_chart_view_cta_$blockId").getUnclippedBoundsInRoot()
        val optionsBounds = composeRule.onNodeWithTag("editor_chart_options_cta_$blockId").getUnclippedBoundsInRoot()
        assertTrue(viewBounds.height.value >= 48f)
        assertTrue(optionsBounds.height.value >= 48f)
        assertEquals(viewBounds.top.value, optionsBounds.top.value, 2f)
    }

    private fun assertWithinChartHost(blockId: String, tag: String) {
        val hostBounds = composeRule.onNodeWithTag("editor_chart_block_$blockId").getUnclippedBoundsInRoot()
        val bounds = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
        assertTrue(bounds.left.value >= hostBounds.left.value - 4f)
        assertTrue(bounds.right.value <= hostBounds.right.value + 4f)
    }

    private fun assertTooltipLayout(blockId: String) {
        val tooltipBounds = composeRule.onNodeWithTag("editor_chart_tooltip_$blockId").getUnclippedBoundsInRoot()
        val dismissBounds = composeRule.onNodeWithTag("editor_chart_tooltip_dismiss_$blockId")
            .getUnclippedBoundsInRoot()
        assertTrue(dismissBounds.height.value >= 48f)
        assertTrue(dismissBounds.left.value >= tooltipBounds.left.value)
        assertTrue(dismissBounds.right.value <= tooltipBounds.right.value)
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
