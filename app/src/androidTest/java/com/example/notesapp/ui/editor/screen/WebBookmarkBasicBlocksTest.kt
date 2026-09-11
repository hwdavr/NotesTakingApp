package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebBookmarkBasicBlocksTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun advancedPanelShowsWebBookmarkTile() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true
            )
        )

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note-1",
                state = state,
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

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_grid")
            .performScrollToNode(hasTestTag("basic_blocks_web_bookmark"))

        val tile = composeRule.onNodeWithTag("basic_blocks_web_bookmark")
        tile.assertIsDisplayed()
        tile.assertHasClickAction()
        val bounds = tile.getUnclippedBoundsInRoot()
        assertTrue("Web Bookmark tile height ${bounds.height} must be >= 48dp", bounds.height >= 48.dp)
        composeRule.onNodeWithText("Web Bookmark").assertIsDisplayed()
    }
}
