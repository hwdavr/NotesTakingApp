package com.example.notesapp.ui.editor.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteEditorRenameDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun exposesStableTagsForFieldAndActions() {
        composeRule.setContent {
            NotesTakingAppTheme {
                NoteEditorRenameDialog(
                    value = "Existing note",
                    onValueChange = {},
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }

        composeRule.onNodeWithTag("note_editor_rename_text_field").assertIsDisplayed()
        composeRule.onNodeWithTag("note_editor_rename_confirm_button").assertIsDisplayed()
        composeRule.onNodeWithTag("note_editor_rename_cancel_button").assertIsDisplayed()
    }
}
