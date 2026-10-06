package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorTitleWrappingTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longTitle_wrapsToMultipleLinesWithoutHorizontalScrolling() {
        val longTitle =
            "This is a deliberately long note title that should wrap across multiple lines " +
                "instead of scrolling horizontally"

        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(
                    noteId = "note_1",
                    title = longTitle,
                    isLoaded = true,
                    isEditable = true
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
                onToggleFormattingToolbar = {},
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }

        composeRule.waitForIdle()
        val titleNode = composeRule.onNodeWithTag("editor_title_input").fetchSemanticsNode()
        val titleBounds = composeRule.onNodeWithTag("editor_title_input").getUnclippedBoundsInRoot()

        assertTrue(
            "A wrapped title should be taller than a single-line title",
            titleBounds.bottom - titleBounds.top >= 112.dp
        )
        assertTrue(
            "The title field must not scroll horizontally",
            SemanticsProperties.HorizontalScrollAxisRange !in titleNode.config
        )
    }
}
