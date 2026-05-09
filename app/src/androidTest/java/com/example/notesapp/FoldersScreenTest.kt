package com.example.notesapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.folders.screen.FoldersScreenContent
import com.example.notesapp.ui.folders.viewmodel.FolderTreeItem
import com.example.notesapp.ui.folders.viewmodel.FoldersUiState
import com.example.notesapp.ui.folders.viewmodel.SmartCollectionCounts
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestFoldersScreen(
    state: FoldersUiState,
    onDeleteFolder: (Folder) -> Unit = {},
    onRenameFolder: (Folder, String) -> Unit = { _, _ -> },
    onRenameNote: (Note, String) -> Unit = { _, _ -> },
    onMoveFolder: (Folder) -> Unit = {},
    onMoveNote: (Note) -> Unit = {},
    onAddToFavoritesNote: (Note) -> Unit = {},
    onAddToFavoritesFolder: (Folder) -> Unit = {}
) {
    NotesTakingAppTheme {
        FoldersScreenContent(
            parentPadding = PaddingValues(0.dp),
            state = state,
            onSearchChanged = {},
            onAddFolder = { _, _ -> },
            onRenameFolder = onRenameFolder,
            onRenameNote = onRenameNote,
            onDeleteFolder = onDeleteFolder,
            onDeleteNote = {},
            onAddToFavoritesFolder = onAddToFavoritesFolder,
            onAddToFavoritesNote = onAddToFavoritesNote,
            onAddNote = {},
            onOpenNote = {},
            onOpenCollection = { _, _, _ -> },
            onMoveFolder = onMoveFolder,
            onMoveNote = onMoveNote
        )
    }
}
@RunWith(AndroidJUnit4::class)
class FoldersScreenTest {
    @get:Rule
    val composeRule = createComposeRule()
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun folderAddScenario_rendersExpectedFolder() {
        val folder = Folder(id = "folder_001", name = "Work", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            smartCounts = SmartCollectionCounts(allNotes = 0),
            treeItems = listOf(FolderTreeItem.FolderItem(folder, 0, 0, false))
        )
        composeRule.setContent {
            TestFoldersScreen(state = state)
        }
        composeRule.onNodeWithTag("folders_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Work").assertIsDisplayed()
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun searchingWithoutMatches_showsSearchEmptyState() {
        var searchQuery = ""
        val state = FoldersUiState(
            treeItems = emptyList(),
            isSearchActive = true
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                FoldersScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    state = state,
                    onSearchChanged = { searchQuery = it },
                    onAddFolder = { _, _ -> },
                    onRenameFolder = { _, _ -> },
                    onRenameNote = { _, _ -> },
                    onDeleteFolder = {},
                    onDeleteNote = {},
                    onAddNote = {},
                    onOpenNote = {},
                    onOpenCollection = { _, _, _ -> }
                )
            }
        }
        composeRule.onNode(hasSetTextAction()).performTextInput("missing")
        composeRule.onNodeWithText("No folders or notes match your search.").assertIsDisplayed()
        assertEquals("missing", searchQuery)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun moveFolderAction_emitsMoveFolderCallback() {
        var folderToMove: Folder? = null
        val folder = Folder(id = "f1", name = "Move Me", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.FolderItem(folder, 0, 0, false))
        )
        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onMoveFolder = { folderToMove = it }
            )
        }
        composeRule.onNodeWithTag("folder_more_actions_f1").performClick()
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag("move_item_action", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("move_item_action", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        assertEquals(folder, folderToMove)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun moveNoteAction_emitsMoveNoteCallback() {
        var noteToMove: Note? = null
        val note = Note(id = "n1", title = "Move Note", content = "", folderId = "f1", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.NoteItem(note, 0))
        )
        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onMoveNote = { noteToMove = it }
            )
        }
        composeRule.onNodeWithTag("note_more_actions_n1").performClick()
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag("move_item_action", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("move_item_action", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        assertEquals(note, noteToMove)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun archiveFolder_showsConfirmationDialog_andArchivesOnConfirm() {
        var archivedFolder: Folder? = null
        val folder = Folder(id = "f1", name = "Test Folder", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.FolderItem(folder, 0, 0, false))
        )
        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onDeleteFolder = { archivedFolder = it }
            )
        }
        // 1. Open more actions for folder f1
        composeRule.onNodeWithTag("folder_more_actions_f1").performClick()
        // 2. Wait for and click archive in the action sheet
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag(
                "delete_item_action",
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("delete_item_action", useUnmergedTree = true).performClick()
        // 3. Verify confirmation dialog is displayed
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithText("Archive Folder", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Archive Folder", ignoreCase = true).assertIsDisplayed()
        // 4. Click Archive in the confirmation dialog
        composeRule.onNodeWithTag("confirm_delete_button").performClick()
        // 5. Verify onDeleteFolder was called
        composeRule.waitForIdle()
        assertEquals(folder, archivedFolder)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun renameFolder_updatesFolderName() {
        var renamedFolder: Pair<Folder, String>? = null
        val folder = Folder(id = "f1", name = "Old Name", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.FolderItem(folder, 0, 0, false))
        )
        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onRenameFolder = { renamed, name -> renamedFolder = renamed to name }
            )
        }
        // 1. Open more actions
        composeRule.onNodeWithTag("folder_more_actions_f1").performClick()
        // 2. Click Rename in the action sheet
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag(
                "rename_item_action",
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("rename_item_action", useUnmergedTree = true).performClick()
        // 3. Verify dialog and enter new name
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithText("Rename Folder", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Rename Folder", ignoreCase = true).assertIsDisplayed()
        composeRule.onNodeWithTag("rename_text_field").performTextReplacement("New Name")
        // Wait for dialog to be fully interactive
        composeRule.waitUntil(5000) {
            composeRule.onAllNodesWithTag("rename_confirm_button").fetchSemanticsNodes().isNotEmpty()
        }
        // 4. Click Rename confirm
        composeRule.onNodeWithTag("rename_confirm_button").performClick()
        // 5. Verify onRenameFolder was called
        composeRule.waitForIdle()
        assertEquals(folder to "New Name", renamedFolder)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun renameNote_updatesNoteTitle() {
        var renamedNote: Pair<Note, String>? = null
        val note = Note(id = "n1", title = "Old Note", content = "", folderId = "f1", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.NoteItem(note, 0))
        )
        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onRenameNote = { renamed, title -> renamedNote = renamed to title }
            )
        }
        // 1. Open more actions
        composeRule.onNodeWithTag("note_more_actions_n1").performClick()
        // 2. Click Rename in the action sheet
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag(
                "rename_item_action",
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("rename_item_action", useUnmergedTree = true).performClick()
        // 3. Verify dialog and enter new title
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithText("Rename Note", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Rename Note", ignoreCase = true).assertIsDisplayed()
        composeRule.onNodeWithTag("rename_text_field").performTextReplacement("New Note")
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag("rename_confirm_button").fetchSemanticsNodes().isNotEmpty()
        }
        // 4. Click Rename confirm
        composeRule.onNodeWithTag("rename_confirm_button").performClick()
        // 5. Verify onRenameNote was called
        composeRule.waitForIdle()
        assertEquals(note to "New Note", renamedNote)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun addToFavoritesNoteAction_emitsFavoriteNoteCallback() {
        var favoritedNote: Note? = null
        val note = Note(id = "n1", title = "Fav Note", content = "", folderId = "f1", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.NoteItem(note, 0))
        )
        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onAddToFavoritesNote = { favoritedNote = it }
            )
        }
        composeRule.onNodeWithTag("note_more_actions_n1").performClick()
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag(
                "add_to_favorites_action",
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("add_to_favorites_action", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        assertEquals(note, favoritedNote)
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun addToFavoritesFolderAction_emitsFavoriteFolderCallback() {
        var favoritedFolder: Folder? = null
        val folder = Folder(id = "f1", name = "Fav Folder", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.FolderItem(folder, 0, 0, false))
        )
        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onAddToFavoritesFolder = { favoritedFolder = it }
            )
        }
        composeRule.onNodeWithTag("folder_more_actions_f1").performClick()
        composeRule.waitUntil(10000) {
            composeRule.onAllNodesWithTag(
                "add_to_favorites_action",
                useUnmergedTree = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("add_to_favorites_action", useUnmergedTree = true).performClick()
        composeRule.waitForIdle()
        assertEquals(folder, favoritedFolder)
    }
}
