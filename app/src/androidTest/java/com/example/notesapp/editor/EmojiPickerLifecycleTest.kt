package com.example.notesapp.editor

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import com.example.notesapp.ui.editor.screen.NoteEditorScreenContent
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmojiPickerLifecycleTest {
    @get:Rule
    val activityRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun pickerOmitsTitleAndHeaderCloseButton() {
        renderActivityContent()
        openPicker(activityRule)

        assertTrue(
            activityRule.onAllNodesWithText("Emoji", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
        assertTrue(
            activityRule.onAllNodesWithTag("emoji_picker_close", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    @Test
    fun scrimTapDismissesSheetWithoutChangingDocument() {
        renderActivityContent()
        openPicker(activityRule)

        activityRule.onAllNodes(isRoot(), useUnmergedTree = true).onLast().performTouchInput {
            click(Offset(8f, 8f))
        }
        activityRule.waitForIdle()

        assertSheetDismissed(activityRule)
        assertDocumentUnchanged(activityRule)
    }

    @Test
    fun systemBackDismissesSheetBeforeLeavingEditor() {
        renderActivityContent()
        openPicker(activityRule)

        pressBack()
        activityRule.waitForIdle()

        assertSheetDismissed(activityRule)
        assertDocumentUnchanged(activityRule)
        assertTrue(activityRule.activity.isFinishing.not())
    }

    @Test
    fun savedPresentationStateRestoresOpenSheetAfterRecreation() {
        val restorationTester = StateRestorationTester(activityRule)
        restorationTester.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "lifecycle-note",
                state = editorState(),
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
                emojiPickerState = EmojiPickerUiState.empty(),
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

        openPicker(activityRule)
        restorationTester.emulateSavedInstanceStateRestore()

        activityRule.onNodeWithTag("emoji_picker_sheet").assertIsDisplayed()
        assertDocumentUnchanged(activityRule)
    }

    private fun renderActivityContent() {
        activityRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "lifecycle-note",
                state = editorState(),
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
                emojiPickerState = EmojiPickerUiState.empty(),
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
    }

    private fun openPicker(rule: ComposeContentTestRule) {
        rule.onNodeWithTag("editor_default_bottom_bar").performTouchInput { swipeLeft() }
        rule.onNodeWithTag("editor_insert_emoji").performClick()
        rule.waitForIdle()
        rule.onNodeWithTag("emoji_picker_sheet").assertIsDisplayed()
    }

    private fun assertSheetDismissed(rule: ComposeContentTestRule) {
        assertTrue(rule.onAllNodesWithTag("emoji_picker_sheet").fetchSemanticsNodes().isEmpty())
    }

    private fun assertDocumentUnchanged(rule: ComposeContentTestRule) {
        rule.onNodeWithText("Lifecycle body", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun editorState(): NoteEditorUiState {
        val block = EditorBlock.TextBlock(
            id = "lifecycle-body",
            children = listOf(RichText("Lifecycle body"))
        )
        return NoteEditorUiState(
            noteId = "lifecycle-note",
            title = "Lifecycle title",
            document = NoteDocument(blocks = listOf(block)),
            isLoaded = true,
            focusedBlockId = block.id,
            selectionStart = block.text().length,
            selectionEnd = block.text().length
        )
    }
}
