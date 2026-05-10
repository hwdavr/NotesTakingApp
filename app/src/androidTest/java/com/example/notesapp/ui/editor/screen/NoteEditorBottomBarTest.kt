package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorBottomBarTest {
    @get:Rule val composeRule = createComposeRule()
    @Test
    fun bottomBar_togglesBetweenDefaultAndFormatting() {
        val isFormattingVisible = mutableStateOf(false)
        composeRule.setContent {
            NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "note_1",
                    state =
                            NoteEditorUiState(
                                    noteId = "note_1",
                                    isFormattingToolbarVisible = isFormattingVisible.value,
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
                    onTextBlockChange = { _, _ -> },
                    onToggleMark = { _, _ -> },
                    onAddParagraph = {},
                    onAddImage = {},
                    onImageChange = { _, _, _ -> },
                    onAddTable = {},
                    onTableCellChange = { _, _, _, _ -> },
                    onFolderSelected = {},
                    onToggleFormattingToolbar = {
                        isFormattingVisible.value = !isFormattingVisible.value
                    },
                    onBlockFocused = {},
                    onSelectionChange = { _, _ -> },
                    onDeleteBlock = {}
            )
        }
        // Initially default bottom bar is shown
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("editor_formatting_bottom_bar").fetchSemanticsNodes().isEmpty())
        // Click Aa button
        composeRule.onNodeWithTag("editor_toggle_formatting").performClick()
        // Now formatting bottom bar is shown
        composeRule.onNodeWithTag("editor_formatting_bottom_bar").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("editor_default_bottom_bar").fetchSemanticsNodes().isEmpty())
        // Click hide button
        composeRule.onNodeWithTag("editor_hide_formatting").performClick()
        // Back to default bottom bar
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("editor_formatting_bottom_bar").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun readOnlyNote_hidesBottomBar() {
        composeRule.setContent {
            NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "note_1",
                    state =
                            NoteEditorUiState(
                                    noteId = "note_1",
                                    isLoaded = true,
                                    isEditable = false
                            ),
                    onBack = {},
                    onShareRequested = {},
                    onDelete = {},
                    onTitleChange = {},
                    onRename = {},
                    onToggleFavorite = {},
                    onMoveNote = {},
                    onExportNote = {},
                    onTextBlockChange = { _, _ -> },
                    onToggleMark = { _, _ -> },
                    onAddParagraph = {},
                    onAddImage = {},
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
        assertTrue(composeRule.onAllNodesWithTag("editor_default_bottom_bar").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("editor_formatting_bottom_bar").fetchSemanticsNodes().isEmpty())
    }
}
