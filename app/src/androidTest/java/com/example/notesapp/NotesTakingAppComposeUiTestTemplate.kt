package com.example.notesapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Template Compose UI tests for NotesTakingApp.
 *
 * This file is intentionally simple and acts as a starter harness example.
 * Replace Placeholder* composables with the real app screens/routes as the project evolves.
 *
 * Suggested stable testTag names for this project:
 * - notes_tab
 * - folders_tab
 * - settings_tab
 * - notes_screen
 * - folders_screen
 * - settings_screen
 * - new_note_button
 * - note_editor_screen
 * - note_title_input
 * - note_content_input
 * - save_note_button
 * - delete_note_button
 * - archive_toggle
 * - folder_picker
 * - folder_item_<id>
 * - note_item_<id>
 * - empty_state
 * - search_button
 * - search_input
 */
@RunWith(AndroidJUnit4::class)
class NotesTakingAppComposeUiTestTemplate {
    @get:Rule
    val composeRule = createComposeRule()
    @Test
    fun bottomNavigation_notesTab_isDisplayed() {
        composeRule.setContent {
            PlaceholderBottomNavScreen(selected = "notes")
        }
        composeRule.onNodeWithTag("notes_tab").assertIsDisplayed()
        composeRule.onNodeWithTag("notes_screen").assertIsDisplayed()
    }
    @Test
    fun bottomNavigation_clickFolders_showsFoldersScreen() {
        composeRule.setContent {
            PlaceholderBottomNavScreen(selected = "notes")
        }
        composeRule.onNodeWithTag("folders_tab").performClick()
        composeRule.onNodeWithTag("folders_screen").assertIsDisplayed()
    }
    @Test
    fun bottomNavigation_clickSettings_showsSettingsScreen() {
        composeRule.setContent {
            PlaceholderBottomNavScreen(selected = "notes")
        }
        composeRule.onNodeWithTag("settings_tab").performClick()
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
    }
    @Test
    fun newNoteButton_opensEditor() {
        composeRule.setContent {
            PlaceholderNotesScreen(showEditor = false)
        }
        composeRule.onNodeWithTag("new_note_button").performClick()
        composeRule.onNodeWithTag("note_editor_screen").assertIsDisplayed()
    }
}
@Composable
private fun PlaceholderBottomNavScreen(selected: String) {
    var currentSelected by remember { mutableStateOf(selected) }
    Box(modifier = Modifier.fillMaxSize()) {
        when (currentSelected) {
            "notes" -> PlaceholderNotesScreen(showEditor = false)
            "folders" -> Box(modifier = Modifier.fillMaxSize().testTag("folders_screen")) {
                Text("Folders")
            }
            "settings" -> Box(modifier = Modifier.fillMaxSize().testTag("settings_screen")) {
                Text("Settings")
            }
        }
        // Simple placeholders to demonstrate tag names used by tests.
        // Arranged in a Row to avoid overlapping.
        Row(modifier = Modifier.align(Alignment.BottomCenter)) {
            Box(modifier = Modifier.size(48.dp).testTag("notes_tab").clickable { currentSelected = "notes" })
            Box(modifier = Modifier.size(48.dp).testTag("folders_tab").clickable { currentSelected = "folders" })
            Box(modifier = Modifier.size(48.dp).testTag("settings_tab").clickable { currentSelected = "settings" })
        }
    }
}
@Composable
private fun PlaceholderNotesScreen(showEditor: Boolean) {
    var isEditorVisible by remember { mutableStateOf(showEditor) }
    Box(modifier = Modifier.fillMaxSize().testTag("notes_screen")) {
        Text("Notes")
    }
    Box(modifier = Modifier.size(1.dp).testTag("new_note_button").clickable { isEditorVisible = true })
    if (isEditorVisible) {
        Box(modifier = Modifier.fillMaxSize().testTag("note_editor_screen")) {
            Text("Editor")
        }
    }
}
