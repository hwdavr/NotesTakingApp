package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteSummaryUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorSummaryPanelTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun summaryPanel_showsLoadingState() {
        renderSummary(NoteSummaryUiState.Loading)

        assertSummaryPanelExists()
        assertTextExists("Generating summary...")
    }

    @Test
    fun summaryPanel_showsContentState() {
        renderSummary(NoteSummaryUiState.Content("This note captures the project decisions."))

        assertSummaryPanelExists()
        assertTextExists("This note captures the project decisions.")
    }

    @Test
    fun summaryPanel_showsErrorStateWithoutHidingEditor() {
        renderSummary(NoteSummaryUiState.Error)

        assertSummaryPanelExists()
        assertTextExists("Summary is unavailable on this device.")
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertIsDisplayed()
    }

    private fun assertSummaryPanelExists() {
        composeRule.waitForIdle()
        assertTrue(
            composeRule.onAllNodesWithTag("editor_summary_panel", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
    }

    private fun assertTextExists(text: String) {
        composeRule.waitForIdle()
        assertTrue(
            composeRule.onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        )
    }

    private fun renderSummary(summaryState: NoteSummaryUiState) {
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(
                    noteId = "note_1",
                    title = "Project notes",
                    summaryState = summaryState,
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
    }
}
