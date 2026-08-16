@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
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
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TableHandlesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

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

    private fun tableBlock(): EditorBlock.TableBlock = EditorBlock.TableBlock(
        id = "table_1",
        rows = listOf(
            listOf(listOf(RichText("A1")), listOf(RichText("B1"))),
            listOf(listOf(RichText("A2")), listOf(RichText("B2")))
        )
    )
}
