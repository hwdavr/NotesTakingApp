package com.example.notesapp.ui.notes.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.notes.viewmodel.MoveToFolderDestination
import com.example.notesapp.ui.notes.viewmodel.MoveToUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoveToScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun moveToScreen_rendersActionBarSearchRootAndRecentFolders() {
        val state = MoveToUiState(
            recentFolders = listOf(
                MoveToFolderDestination(id = "folder_1", name = "Work"),
                MoveToFolderDestination(id = "folder_2", name = "Personal")
            ),
            folderResults = listOf(
                MoveToFolderDestination(id = "folder_1", name = "Work"),
                MoveToFolderDestination(id = "folder_2", name = "Personal")
            )
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                MoveToScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    state = state,
                    onBack = {},
                    onSearchChanged = {},
                    onDestinationSelected = {}
                )
            }
        }
        composeRule.onNodeWithTag("move_to_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Move to").assertIsDisplayed()
        composeRule.onNodeWithTag("move_to_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("move_to_root_destination").assertIsDisplayed()
        composeRule.onNodeWithText("Recent folders").assertIsDisplayed()
        composeRule.onNodeWithTag("move_to_folder_folder_1").assertIsDisplayed()
        composeRule.onNodeWithText("Work").assertIsDisplayed()
    }

    @Test
    fun moveToScreen_searchInputEmitsQuery() {
        var query = ""
        composeRule.setContent {
            NotesTakingAppTheme {
                MoveToScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    state = MoveToUiState(),
                    onBack = {},
                    onSearchChanged = { query = it },
                    onDestinationSelected = {}
                )
            }
        }
        composeRule.onNode(hasSetTextAction()).performTextInput("work")
        assertEquals("work", query)
    }
}
