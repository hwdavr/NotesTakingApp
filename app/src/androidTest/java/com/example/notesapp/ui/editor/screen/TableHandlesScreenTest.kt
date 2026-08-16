@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.screen

import android.graphics.Bitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.FakeFolderRepository
import com.example.notesapp.FakeNoteRepository
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderCategorizer
import com.example.notesapp.domain.folder.usecase.CategorizeNoteUseCase
import com.example.notesapp.domain.summary.NoteSummarizer
import com.example.notesapp.domain.summary.NoteSummary
import com.example.notesapp.domain.summary.usecase.SummarizeNoteUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteAudioUseCase
import com.example.notesapp.domain.voice.usecase.DeleteVoiceNoteBlockUseCase
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.onTableAction
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import io.mockk.mockk
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TableHandlesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var currentNoteRepository: FakeNoteRepository

    @Test
    fun focusedCellShowsAllHandles() {
        setEditorContent(isEditable = true)

        focusFirstTableCell()

        composeRule.onNodeWithTag("table_column_handle").assertIsDisplayed()
        composeRule.onNodeWithTag("table_row_handle").assertIsDisplayed()
        composeRule.onNodeWithTag("table_options_handle").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Focused table cell").assertExists()
    }

    @Test
    fun handlesDismissWhenFocusLeavesTable() {
        setEditorContent(
            isEditable = true,
            blocks = listOf(
                tableBlock(),
                EditorBlock.TextBlock(id = "text_1", children = listOf(RichText("Outside")))
            )
        )

        focusFirstTableCell()
        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.waitForIdle()

        assertHandlesAbsent()
    }

    @Test
    fun eachHandleOpensOrderedSheet() {
        setEditorContent(isEditable = true)

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_column_handle").performClick()
        composeRule.onNodeWithTag("table_column_options_sheet").assertIsDisplayed()
        composeRule.onNodeWithText("Insert column left").assertExists()
        composeRule.onNodeWithText("Insert column right").assertExists()
        composeRule.onNodeWithText("Clear column").assertExists()
        composeRule.onNodeWithText("Delete column").assertExists()
        composeRule.onNodeWithTag("table_column_options_sheet_delete_divider").assertExists()
        composeRule.onNodeWithText("Insert column left").performClick()
        assertSheetAbsent("table_column_options_sheet")

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_row_handle").performClick()
        composeRule.onNodeWithTag("table_row_options_sheet").assertIsDisplayed()
        composeRule.onNodeWithText("Insert row above").assertExists()
        composeRule.onNodeWithText("Insert row below").assertExists()
        composeRule.onNodeWithText("Clear row").assertExists()
        composeRule.onNodeWithText("Delete row").assertExists()
        composeRule.onNodeWithTag("table_row_options_sheet_delete_divider").assertExists()
        composeRule.onNodeWithText("Insert row above").performClick()
        assertSheetAbsent("table_row_options_sheet")

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_options_handle").performClick()
        composeRule.onNodeWithTag("table_options_sheet").assertIsDisplayed()
        composeRule.onNodeWithText("Clear entire table").assertExists()
        composeRule.onNodeWithText("Duplicate table").assertExists()
        composeRule.onNodeWithText("Fit to width").assertExists()
        composeRule.onNodeWithText("Delete table").assertExists()
        composeRule.onNodeWithTag("table_options_sheet_delete_divider").assertExists()
    }

    @Test
    fun readOnlyTableHasNoHandles() {
        setEditorContent(isEditable = false)

        composeRule.onAllNodesWithTag("editor_table_cell", useUnmergedTree = true)
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        assertHandlesAbsent()
        assertSheetAbsent("table_column_handle")
        assertSheetAbsent("table_row_handle")
        assertSheetAbsent("table_options_handle")
    }

    @Test
    fun selectingOptionUpdatesAndDismisses() {
        val viewModel = setProductionEditorContent()

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_column_handle").performClick()
        composeRule.onNodeWithTag("table_clear_column").performClick()
        composeRule.waitForIdle()

        assertSheetAbsent("table_column_options_sheet")
        val table = viewModel.uiState.value.document.blocks.single() as EditorBlock.TableBlock
        assertTrue(table.rows.all { row -> row[0].joinToString("") { it.text }.isEmpty() })
        composeRule.onNodeWithText("B1").assertExists()
    }

    @Test
    fun storedTargetSurvivesSheetFocusLoss() {
        val viewModel = setProductionEditorContent(
            blocks = listOf(
                tableBlock(),
                EditorBlock.TextBlock(id = "outside", children = listOf(RichText("Outside")))
            )
        )

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_column_handle").performClick()
        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.onNodeWithTag("table_clear_column").performClick()
        composeRule.waitForIdle()

        assertSheetAbsent("table_column_options_sheet")
        val table = viewModel.uiState.value.document.blocks.first() as EditorBlock.TableBlock
        assertTrue(table.rows.all { row -> row[0].joinToString("") { it.text }.isEmpty() })
    }

    @Test
    fun onlyFocusedTableShowsHandles() {
        val viewModel = setProductionEditorContent(
            blocks = listOf(
                tableBlock(id = "table_1"),
                tableBlock(id = "table_2", firstColumnText = "C1", secondColumnText = "D1")
            )
        )

        focusFirstTableCell()
        assertTrue(
            composeRule.onAllNodesWithTag("table_column_handle", useUnmergedTree = true)
                .fetchSemanticsNodes().size == 1
        )
        composeRule.onNodeWithTag("table_column_handle").performClick()
        composeRule.onNodeWithTag("table_clear_column").performClick()
        composeRule.waitForIdle()

        assertTrue(viewModel.uiState.value.document.blocks[1] is EditorBlock.TableBlock)
        val secondTable = viewModel.uiState.value.document.blocks[1] as EditorBlock.TableBlock
        assertEquals("C1", secondTable.rows[0][0].joinToString("") { it.text })
    }

    @Test
    fun deleteIsFinalActionInEverySheet() {
        setProductionEditorContent()
        focusFirstTableCell()

        composeRule.onNodeWithTag("table_column_handle").performClick()
        assertDeleteActionIsLast(
            actionTags = listOf(
                "table_insert_column_left",
                "table_insert_column_right",
                "table_clear_column",
                "table_delete_column"
            ),
            dividerTag = "table_column_options_sheet_delete_divider"
        )
        pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("table_row_handle").performClick()
        assertDeleteActionIsLast(
            actionTags = listOf(
                "table_insert_row_above",
                "table_insert_row_below",
                "table_clear_row",
                "table_delete_row"
            ),
            dividerTag = "table_row_options_sheet_delete_divider"
        )
        pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("table_options_handle").performClick()
        assertDeleteActionIsLast(
            actionTags = listOf(
                "table_clear_all",
                "table_duplicate",
                "table_fit_to_width",
                "table_delete"
            ),
            dividerTag = "table_options_sheet_delete_divider"
        )
    }

    @Test
    fun operationPersistsAfterEditorReload() {
        val viewModel = setProductionEditorContent()

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_row_handle").performClick()
        composeRule.onNodeWithTag("table_insert_row_below").performClick()
        composeRule.waitForIdle()
        assertEquals(
            3,
            (viewModel.uiState.value.document.blocks.single() as EditorBlock.TableBlock).rows.size
        )

        var saveCompleted = false
        viewModel.save { saveCompleted = true }
        composeRule.waitUntil(5_000) { saveCompleted }

        val reloadedViewModel = createProductionViewModel(currentNoteRepository)
        reloadedViewModel.load("table-note")
        composeRule.waitUntil(5_000) {
            reloadedViewModel.uiState.value.isLoaded &&
                reloadedViewModel.uiState.value.document.blocks
                    .filterIsInstance<EditorBlock.TableBlock>()
                    .singleOrNull()?.rows?.size == 3
        }

        val reloadedTable = reloadedViewModel.uiState.value.document.blocks
            .filterIsInstance<EditorBlock.TableBlock>().single()
        assertEquals(3, reloadedTable.rows.size)
    }

    @Test
    fun tableOptionsFlowCompletes() {
        val viewModel = setProductionEditorContent()

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_options_handle").performClick()
        composeRule.onNodeWithTag("table_duplicate").performClick()
        composeRule.waitForIdle()
        assertEquals(
            2,
            viewModel.uiState.value.document.blocks.count { it is EditorBlock.TableBlock }
        )
    }

    @Test
    fun tableFitToWidthFlowCompletes() {
        val viewModel = setProductionEditorContent()

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_options_handle").performClick()
        composeRule.onNodeWithTag("table_fit_to_width").performClick()
        composeRule.waitForIdle()
        assertTrue((viewModel.uiState.value.document.blocks.first() as EditorBlock.TableBlock).fitToWidth)
    }

    @Test
    fun tableDeleteFlowCompletes() {
        val viewModel = setProductionEditorContent()

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_options_handle").performClick()
        composeRule.onNodeWithTag("table_delete").performClick()
        composeRule.waitForIdle()
        assertTrue(viewModel.uiState.value.document.blocks.none { it is EditorBlock.TableBlock })
    }

    @Test
    fun captureFocusedTableState() {
        setProductionEditorContent(
            blocks = listOf(
                EditorBlock.TextBlock(id = "intro", children = listOf(RichText("Project Team"))),
                tableBlock(id = "visual_table", firstColumnText = "Alice", secondColumnText = "Designer")
            )
        )

        focusFirstTableCell()
        composeRule.onNodeWithTag("table_column_handle").assertIsDisplayed()
        composeRule.onNodeWithTag("table_row_handle").assertIsDisplayed()
        composeRule.onNodeWithTag("table_options_handle").assertIsDisplayed()
        captureVisualEvidence("notesapp_table_handles_focused")
    }

    @Test
    fun captureColumnOptionsSheet() {
        setProductionEditorContent()
        focusFirstTableCell()
        composeRule.onNodeWithTag("table_column_handle").performClick()
        composeRule.onNodeWithText("Column Options").assertIsDisplayed()
        composeRule.onNodeWithText("Insert column left").assertIsDisplayed()
        composeRule.onNodeWithText("Delete column").assertIsDisplayed()
        captureVisualEvidence("notesapp_table_column_sheet")
    }

    @Test
    fun captureRowOptionsSheet() {
        setProductionEditorContent()
        focusFirstTableCell()
        composeRule.onNodeWithTag("table_row_handle").performClick()
        composeRule.onNodeWithText("Row Options").assertIsDisplayed()
        composeRule.onNodeWithText("Insert row above").assertIsDisplayed()
        composeRule.onNodeWithText("Delete row").assertIsDisplayed()
        captureVisualEvidence("notesapp_table_row_sheet")
    }

    @Test
    fun captureTableOptionsSheet() {
        setProductionEditorContent()
        focusFirstTableCell()
        composeRule.onNodeWithTag("table_options_handle").performClick()
        composeRule.onNodeWithText("Table Options").assertIsDisplayed()
        composeRule.onNodeWithText("Clear entire table").assertIsDisplayed()
        composeRule.onNodeWithText("Delete table").assertIsDisplayed()
        captureVisualEvidence("notesapp_table_options_sheet")
    }

    private fun focusFirstTableCell() {
        composeRule.onAllNodesWithTag("editor_table_cell", useUnmergedTree = true)
            .onFirst()
            .performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("table_column_handle", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        pressBack()
        composeRule.waitForIdle()
    }

    private fun assertHandlesAbsent() {
        assertTrue(
            composeRule.onAllNodesWithTag("table_column_handle", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
        assertTrue(
            composeRule.onAllNodesWithTag("table_row_handle", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
        assertTrue(
            composeRule.onAllNodesWithTag("table_options_handle", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    private fun assertSheetAbsent(tag: String) {
        assertTrue(
            composeRule.onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    private fun assertDeleteActionIsLast(actionTags: List<String>, dividerTag: String) {
        val actionTops = actionTags.map { tag ->
            composeRule.onNodeWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNode().boundsInRoot.top
        }
        val deleteTop = actionTops.last()
        assertTrue(deleteTop > actionTops.dropLast(1).max())
        val dividerTop = composeRule.onNodeWithTag(dividerTag, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue(dividerTop < deleteTop)
    }

    private fun setEditorContent(isEditable: Boolean, blocks: List<EditorBlock> = listOf(tableBlock())) {
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(
                    noteId = "note_1",
                    title = "Table note",
                    document = NoteDocument(blocks = blocks),
                    isEditable = isEditable,
                    isLoaded = true
                ),
                onBack = {},
                onShareRequested = {},
                onDelete = {},
                onTitleChange = {},
                onRename = {},
                onToggleFavorite = {},
                onMoveNote = {},
                onExportNote = {},
                onOpenVoiceRecorder = { _, _ -> },
                onTextBlockChange = { _, _ -> },
                onToggleCheckbox = {},
                onToggleCheckboxChecked = {},
                onToggleMark = { _, _ -> },
                onAddParagraph = {},
                onAddImage = {},
                onEmojiSelected = {},
                onEmojiQueryChange = {},
                onEmojiClearQuery = {},
                onEmojiCategorySelected = {},
                onEmojiSkinToneRequested = {},
                onEmojiSkinToneDismissed = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }
        composeRule.waitForIdle()
    }

    private fun setProductionEditorContent(
        isEditable: Boolean = true,
        blocks: List<EditorBlock> = listOf(tableBlock())
    ): NoteEditorViewModel {
        currentNoteRepository = FakeNoteRepository()
        val viewModel = createProductionViewModel(currentNoteRepository)
        resetProductionEditorState(viewModel, isEditable, blocks)
        composeRule.setContent {
            NotesTakingAppTheme {
                val state by viewModel.uiState.collectAsState()
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "table-note",
                    state = state,
                    onBack = {},
                    onShareRequested = {},
                    onDelete = {},
                    onTitleChange = viewModel::onTitleChange,
                    onRename = viewModel::rename,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onMoveNote = {},
                    onExportNote = {},
                    onOpenVoiceRecorder = { _, _ -> },
                    onTextBlockChange = viewModel::onTextBlockChange,
                    onToggleCheckbox = viewModel::toggleCheckbox,
                    onToggleCheckboxChecked = viewModel::toggleCheckboxChecked,
                    onToggleMark = viewModel::toggleBlockMark,
                    onAddParagraph = viewModel::addParagraphBlock,
                    onAddImage = viewModel::addImageBlock,
                    onEmojiSelected = {},
                    onEmojiQueryChange = {},
                    onEmojiClearQuery = {},
                    onEmojiCategorySelected = {},
                    onEmojiSkinToneRequested = {},
                    onEmojiSkinToneDismissed = {},
                    onImageChange = viewModel::updateImageBlock,
                    onAddTable = viewModel::addTableBlock,
                    onTableCellChange = viewModel::updateTableCell,
                    onFolderSelected = viewModel::onFolderSelected,
                    onToggleFormattingToolbar = viewModel::toggleFormattingToolbar,
                    onBlockFocused = viewModel::setFocusedBlock,
                    onSelectionChange = viewModel::updateSelection,
                    onDeleteBlock = viewModel::deleteBlock,
                    onTableAction = viewModel::onTableAction
                )
            }
        }
        composeRule.waitForIdle()
        return viewModel
    }

    private fun resetProductionEditorState(
        viewModel: NoteEditorViewModel,
        isEditable: Boolean = true,
        blocks: List<EditorBlock> = listOf(tableBlock())
    ) {
        viewModel.uiStateInternal.value = NoteEditorUiState(
            noteId = "table-note",
            title = "Table note",
            document = NoteDocument(blocks = blocks),
            isEditable = isEditable,
            isLoaded = true
        )
        composeRule.waitForIdle()
    }

    private fun createProductionViewModel(repository: FakeNoteRepository): NoteEditorViewModel {
        return NoteEditorViewModel(
            repository,
            FakeFolderRepository(),
            SummarizeNoteUseCase(
                object : NoteSummarizer {
                    override suspend fun summarize(title: String, noteText: String): NoteSummary =
                        NoteSummary("Table summary")
                }
            ),
            CategorizeNoteUseCase(
                object : FolderCategorizer {
                    override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? =
                        null
                }
            ),
            mockk<DeleteVoiceNoteAudioUseCase>(relaxed = true),
            mockk<DeleteVoiceNoteBlockUseCase>(relaxed = true)
        )
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

    private fun tableBlock(
        id: String = "table_1",
        firstColumnText: String = "A1",
        secondColumnText: String = "B1"
    ): EditorBlock.TableBlock = EditorBlock.TableBlock(
        id = id,
        rows = listOf(
            listOf(listOf(RichText(firstColumnText)), listOf(RichText(secondColumnText))),
            listOf(
                listOf(RichText("${firstColumnText}2")),
                listOf(RichText("${secondColumnText}2"))
            )
        )
    )
}
