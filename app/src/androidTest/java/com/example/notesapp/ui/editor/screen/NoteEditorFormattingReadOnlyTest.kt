package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorFormattingReadOnlyTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun formattingControlsAreVisibleDisabledAndInert() {
        val initialDoc = NoteDocument(
            blocks = listOf(
                EditorBlock.TextBlock(
                    id = "b1",
                    children = listOf(
                        RichText(text = "Read only text with "),
                        RichText(text = "link", linkTargetId = "target_123", inlineId = "target_123"),
                        RichText(text = " and formula: "),
                        RichText(text = "formula", formulaSource = "a+b=c", inlineId = "form_123")
                    )
                )
            )
        )

        var linkPickerOpened = false
        var formulaSheetOpened = false
        var markToggled = false
        var bodyReset = false

        val readOnlyState = NoteEditorUiState(
            noteId = "readonly_note",
            title = "Read Only Note",
            document = initialDoc,
            isLoaded = true,
            isEditable = false,
            isFormattingToolbarVisible = true
        )

        composeRule.setContent {
            NotesTakingAppTheme {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "readonly_note",
                    state = readOnlyState,
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
                    onToggleMark = { _, _ -> markToggled = true },
                    onResetBody = { bodyReset = true },
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
                    onDeleteBlock = {},
                    onOpenNoteLinkPicker = { linkPickerOpened = true },
                    onOpenFormula = { formulaSheetOpened = true }
                )
            }
        }

        // 1. Verify bottom bar is displayed
        composeRule.onNodeWithTag("editor_formatting_bottom_bar").assertIsDisplayed()

        // 2. Verify all 8 controls are displayed and semantically disabled
        val controls = listOf(
            "editor_body_action",
            "editor_bold_action",
            "editor_italic_action",
            "editor_underline_action",
            "editor_strikethrough_action",
            "editor_link_action",
            "editor_code_action",
            "editor_formula_action"
        )

        for (tag in controls) {
            val node = composeRule.onNodeWithTag(tag)
            node.assertIsDisplayed()
            node.assertIsNotEnabled()
            // Clicking disabled node must be inert
            node.performClick()
        }

        // 3. Verify no mutation or navigation was triggered
        assertFalse(linkPickerOpened)
        assertFalse(formulaSheetOpened)
        assertFalse(markToggled)
        assertFalse(bodyReset)
        assertEquals(initialDoc, readOnlyState.document)
    }
}
