package com.example.notesapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.folders.*
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import com.example.notesapp.domain.folder.Folder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestFoldersScreen(
    state: FoldersUiState,
    onDeleteFolder: (Folder) -> Unit = {}
) {
    NotesTakingAppTheme {
        FoldersScreenContent(
            parentPadding = PaddingValues(0.dp),
            state = state,
            onSearchChanged = {},
            onAddFolder = { _, _ -> },
            onRenameFolder = { _, _ -> },
            onRenameNote = { _, _ -> },
            onDeleteFolder = onDeleteFolder,
            onDeleteNote = {},
            onAddNote = {},
            onOpenNote = {},
            onOpenCollection = { _, _, _ -> }
        )
    }
}

@RunWith(AndroidJUnit4::class)
class FoldersScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun deleteFolder_showsConfirmationDialog_andDeletesOnConfirm() {
        val onDeleteFolder = mockk<(Folder) -> Unit>(relaxed = true)
        val folder = Folder(id = "f1", name = "Test Folder", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.FolderItem(folder, 0, 0, false))
        )

        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onDeleteFolder = onDeleteFolder
            )
        }

        // 1. Open more actions for folder f1
        composeRule.onNodeWithTag("folder_more_actions_f1").performClick()

        // 2. Wait for and click delete in the action sheet
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("delete_item_action").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("delete_item_action").performClick()

        // 3. Verify confirmation dialog is displayed
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("Delete Folder").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Delete Folder").assertIsDisplayed()
        
        // 4. Click Delete in the confirmation dialog
        composeRule.onNodeWithTag("confirm_delete_button").performClick()

        // 5. Wait for idle and verify onDeleteFolder was called
        composeRule.waitForIdle()
        verify { onDeleteFolder(folder) }

        // 6. Verify dialog is gone
        composeRule.onNodeWithText("Delete Folder").assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun deleteFolder_cancelDoesNotDelete() {
        val onDeleteFolder = mockk<(Folder) -> Unit>(relaxed = true)
        val folder = Folder(id = "f1", name = "Test Folder", createdAt = 0, updatedAt = 0)
        val state = FoldersUiState(
            treeItems = listOf(FolderTreeItem.FolderItem(folder, 0, 0, false))
        )

        composeRule.setContent {
            TestFoldersScreen(
                state = state,
                onDeleteFolder = onDeleteFolder
            )
        }

        // 1. Open more actions
        composeRule.onNodeWithTag("folder_more_actions_f1").performClick()

        // 2. Wait for and click delete
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("delete_item_action").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("delete_item_action").performClick()

        // 3. Click Cancel
        composeRule.onNodeWithText("Cancel").performClick()

        // 4. Verify onDeleteFolder was NOT called
        verify(exactly = 0) { onDeleteFolder(any()) }

        // 5. Verify dialog is gone
        composeRule.onNodeWithText("Delete Folder").assertDoesNotExist()
    }
}
