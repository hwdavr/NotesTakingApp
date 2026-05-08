package com.example.notesapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.notes.CollectionNotesScreen
import com.example.notesapp.ui.notes.CollectionNotesViewModel
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollectionNotesScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val screen = CollectionNotesScreenRobot(composeRule)

    @Test
    fun folderCollectionRendersRepositoryChildrenAndOpensNote() {
        var openedNoteId: String? = null
        val viewModel = CollectionNotesViewModel(
            folderRepository = FakeFolderRepository(
                initialFolders = listOf(
                    testFolder(id = "folder_001", name = "Work"),
                    testFolder(id = "folder_002", name = "Archive Drafts", parentFolderId = "folder_001")
                )
            ),
            noteRepository = FakeNoteRepository(
                initialNotes = listOf(
                    testNote(
                        id = "note_001",
                        title = "My Note",
                        content = "Updated content",
                        folderId = "folder_001"
                    )
                )
            ),
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "type" to "folder",
                    "label" to "Work",
                    "folderId" to "folder_001"
                )
            )
        )

        composeRule.setContent {
            NotesTakingAppTheme {
                CollectionNotesScreen(
                    parentPadding = PaddingValues(0.dp),
                    onBack = {},
                    onAddNote = {},
                    onOpenCollection = { _, _, _ -> },
                    onOpenNote = { openedNoteId = it },
                    viewModel = viewModel
                )
            }
        }

        screen.waitForItem("Archive Drafts")
        screen.assertItemVisible("My Note")
        screen.assertItemVisible("Updated content")

        screen.openItem("My Note")

        assertEquals("note_001", openedNoteId)
    }

    @Test
    fun archiveCollectionRendersArchivedFoldersAndNotes() {
        val viewModel = CollectionNotesViewModel(
            folderRepository = FakeFolderRepository(
                initialFolders = listOf(
                    testFolder(id = "folder_archived", name = "Archived Folder", deletedAt = 10)
                )
            ),
            noteRepository = FakeNoteRepository(
                initialNotes = listOf(
                    testNote(
                        id = "note_archived",
                        title = "Archived Note",
                        content = "Archived content",
                        folderId = "folder_archived",
                        deletedAt = 10
                    )
                )
            ),
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "type" to "archive",
                    "label" to "Archive",
                    "folderId" to ""
                )
            )
        )

        step("Render archive collection screen with real ViewModel and fake repositories") {
            composeRule.setContent {
                NotesTakingAppTheme {
                    CollectionNotesScreen(
                        parentPadding = PaddingValues(0.dp),
                        onBack = {},
                        onAddNote = {},
                        onOpenCollection = { _, _, _ -> },
                        onOpenNote = {},
                        viewModel = viewModel
                    )
                }
            }
        }

        step("Verify archived folder and note render") {
            screen.waitForItem("Archived Folder")
            screen.assertItemVisible("Archived Note")
            screen.assertItemVisible("Archived content")
        }
    }

    private fun step(description: String, action: () -> Unit) {
        action()
    }
}

private class CollectionNotesScreenRobot(
    private val composeRule: ComposeContentTestRule
) {
    fun waitForItem(text: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun assertItemVisible(text: String) {
        composeRule.onNodeWithText(text).assertIsDisplayed()
    }

    fun openItem(text: String) {
        composeRule.onNodeWithText(text).performClick()
    }
}

private fun testFolder(id: String, name: String, parentFolderId: String? = null, deletedAt: Long? = null): Folder =
    Folder(
        id = id,
        name = name,
        parentFolderId = parentFolderId,
        deletedAt = deletedAt,
        createdAt = 0,
        updatedAt = 0
    )

private fun testNote(id: String, title: String, content: String, folderId: String?, deletedAt: Long? = null): Note =
    Note(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        deletedAt = deletedAt,
        createdAt = 0,
        updatedAt = 0
    )
