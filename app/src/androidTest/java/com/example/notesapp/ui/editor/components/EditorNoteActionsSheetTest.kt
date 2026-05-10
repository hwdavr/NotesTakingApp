@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorNoteActionsSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun readOnlyNote_hidesMutatingActionsInBottomSheet() {
        composeRule.setContent {
            EditorNoteActionsSheet(
                note = Note(
                    id = "note_1",
                    title = "Shared roadmap",
                    content = "Locked content",
                    createdAt = 1L,
                    updatedAt = 1L,
                    accessRole = NoteAccessRole.READ_ONLY
                ),
                onDismiss = {},
                onAddToFavorites = {},
                onMoveTo = {},
                onRename = {},
                onDelete = {},
                onExport = {}
            )
        }

        composeRule.onNodeWithTag("export_item_action").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("add_to_favorites_action").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("move_item_action").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("rename_item_action").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithTag("delete_item_action").fetchSemanticsNodes().isEmpty())
    }
}
