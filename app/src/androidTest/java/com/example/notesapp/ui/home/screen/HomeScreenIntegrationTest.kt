package com.example.notesapp.ui.home.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.*
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.FakeFolderRepository
import com.example.notesapp.FakeNoteRepository
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.home.viewmodel.HomeViewModel
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenIntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()
    private val screen = HomeScreenRobot(composeRule)
    @Test
    fun selectingFolderChipUpdatesRenderedNotesThroughViewModel() {
        val viewModel = HomeViewModel(
            noteRepository = FakeNoteRepository(
                initialNotes = listOf(
                    note(id = "note_001", title = "Project Plan", content = "Launch tasks", folderId = "work"),
                    note(id = "note_002", title = "Grocery List", content = "Milk and bread", folderId = "personal")
                )
            ),
            folderRepository = FakeFolderRepository(
                initialFolders = listOf(
                    folder(id = "work", name = "Work"),
                    folder(id = "personal", name = "Personal")
                )
            )
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                HomeNotesScreen(
                    parentPadding = PaddingValues(0.dp),
                    onAddNote = {},
                    onOpenNote = {},
                    viewModel = viewModel
                )
            }
        }
        screen.waitForNote("Project Plan")
        screen.assertNoteVisible("Grocery List")
        screen.selectFolder("Personal")
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithText("Grocery List").fetchSemanticsNodes().isNotEmpty()
        }
        screen.assertNoteVisible("Grocery List")
        screen.assertNoteMissing("Project Plan")
    }
    @Test
    fun clickingNoteMoreActionsOpensFolderScreenNoteActionsSheet() {
        val viewModel = HomeViewModel(
            noteRepository = FakeNoteRepository(
                initialNotes = listOf(
                    note(id = "note_001", title = "Project Plan", content = "Launch tasks", folderId = "work")
                )
            ),
            folderRepository = FakeFolderRepository(
                initialFolders = listOf(folder(id = "work", name = "Work"))
            )
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                HomeNotesScreen(
                    parentPadding = PaddingValues(0.dp),
                    onAddNote = {},
                    onOpenNote = {},
                    viewModel = viewModel
                )
            }
        }
        screen.waitForNote("Project Plan")
        screen.openNoteActions("note_001")
        composeRule.onNodeWithText("Add to Favorites").assertIsDisplayed()
        composeRule.onNodeWithText("Move to").assertIsDisplayed()
        composeRule.onNodeWithText("Rename").assertIsDisplayed()
        composeRule.onNodeWithText("Archive").assertIsDisplayed()
    }
    @Test
    fun favoritingHomeNoteShowsFavoriteBadge() {
        val viewModel = HomeViewModel(
            noteRepository = FakeNoteRepository(
                initialNotes = listOf(
                    note(id = "note_001", title = "Project Plan", content = "Launch tasks", folderId = "work")
                )
            ),
            folderRepository = FakeFolderRepository(
                initialFolders = listOf(folder(id = "work", name = "Work"))
            )
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                HomeNotesScreen(
                    parentPadding = PaddingValues(0.dp),
                    onAddNote = {},
                    onOpenNote = {},
                    viewModel = viewModel
                )
            }
        }
        composeRule.waitForIdle()
        screen.waitForNote("Project Plan")
        composeRule.onNodeWithTag("home_note_favorite_badge_note_001").assertDoesNotExist()
        screen.openNoteActions("note_001")
        // Wait for bottom sheet to be visible using tag
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag("add_to_favorites_action", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("add_to_favorites_action", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(20000) {
            composeRule.onAllNodesWithTag("home_note_favorite_badge_note_001", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("home_note_favorite_badge_note_001", useUnmergedTree = true).assertIsDisplayed()
    }
    private fun step(description: String, action: () -> Unit) {
        action()
    }
}
private class HomeScreenRobot(
    private val composeRule: ComposeContentTestRule
) {
    fun waitForNote(title: String) {
        composeRule.waitUntil(15000) {
            composeRule.onAllNodesWithText(title, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }
    fun assertNoteVisible(title: String) {
        composeRule.onNodeWithText(title).assertIsDisplayed()
    }
    fun assertNoteMissing(title: String) {
        assertTrue(composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isEmpty())
    }
    fun selectFolder(name: String) {
        composeRule.onNodeWithText(name).performClick()
    }
    fun openNoteActions(noteId: String) {
        composeRule.onNodeWithTag("home_note_more_actions_$noteId").performClick()
    }
}
private fun folder(id: String, name: String, parentFolderId: String? = null): Folder = Folder(
    id = id,
    name = name,
    parentFolderId = parentFolderId,
    createdAt = 0,
    updatedAt = 0
)
private fun note(id: String, title: String, content: String, folderId: String?): Note = Note(
    id = id,
    title = title,
    content = content,
    folderId = folderId,
    createdAt = 0,
    updatedAt = 0
)
