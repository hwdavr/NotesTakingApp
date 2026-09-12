package com.example.notesapp.ui.editor.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BasicBlocksPanelKeyboardDismissalReproductionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun makeActivityEdgeToEdge() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            WindowCompat.setDecorFitsSystemWindows(composeRule.activity.window, false)
        }
        composeRule.waitForIdle()
    }

    @Test
    fun givenKeyboardVisible_whenBasicBlocksTriggerTapped_thenKeyboardIsDismissed() {
        val state by mutableStateOf(
            NoteEditorUiState(
                noteId = "note-1",
                isLoaded = true,
                isEditable = true,
                document = NoteDocument(
                    blocks = listOf(
                        EditorBlock.TextBlock(
                            id = "block-1",
                            children = listOf(RichText(text = "Editable text"))
                        )
                    )
                )
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

        composeRule.onNodeWithTag("editor_text_block").performClick()
        composeRule.onNodeWithTag("editor_text_block").performTextInput(" more")
        composeRule.waitUntil(timeoutMillis = 5_000) { isImeVisible() }

        composeRule.onNodeWithTag("editor_basic_blocks_trigger").performClick()
        composeRule.onNodeWithTag("basic_blocks_panel").assertIsDisplayed()
        composeRule.waitForIdle()

        assertFalse(
            "The basic blocks trigger must dismiss the software keyboard",
            isImeVisible()
        )
    }

    private fun isImeVisible(): Boolean {
        val decorView = composeRule.activity.window.decorView
        val insets = ViewCompat.getRootWindowInsets(decorView) ?: return false
        return insets.isVisible(WindowInsetsCompat.Type.ime())
    }
}
