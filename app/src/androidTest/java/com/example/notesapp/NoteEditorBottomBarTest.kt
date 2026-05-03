package com.example.notesapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.NoteEditorScreenContent
import com.example.notesapp.ui.editor.NoteEditorUiState
import com.example.notesapp.ui.editor.document.NoteDocument
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorBottomBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomBar_togglesBetweenDefaultAndFormatting() {
        val isFormattingVisible = mutableStateOf(false)
        
        composeRule.setContent {
            NoteEditorScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteId = "note_1",
                state = NoteEditorUiState(
                    noteId = "note_1",
                    isFormattingToolbarVisible = isFormattingVisible.value,
                    isLoaded = true
                ),
                onBack = {},
                onSave = {},
                onDelete = {},
                onTitleChange = {},
                onTextBlockChange = { _, _ -> },
                onToggleMark = { _, _ -> },
                onAddParagraph = {},
                onAddImage = {},
                onImageChange = { _, _, _ -> },
                onAddTable = {},
                onTableCellChange = { _, _, _, _ -> },
                onFolderSelected = {},
                onToggleFormattingToolbar = { isFormattingVisible.value = !isFormattingVisible.value },
                onBlockFocused = {},
                onSelectionChange = { _, _ -> },
                onDeleteBlock = {}
            )
        }

        // Initially default bottom bar is shown
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formatting_bottom_bar").assertDoesNotExist()

        // Click Aa button
        composeRule.onNodeWithTag("editor_toggle_formatting").performClick()

        // Now formatting bottom bar is shown
        composeRule.onNodeWithTag("editor_formatting_bottom_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertDoesNotExist()

        // Click hide button
        composeRule.onNodeWithTag("editor_hide_formatting").performClick()

        // Back to default bottom bar
        composeRule.onNodeWithTag("editor_default_bottom_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("editor_formatting_bottom_bar").assertDoesNotExist()
    }
}
