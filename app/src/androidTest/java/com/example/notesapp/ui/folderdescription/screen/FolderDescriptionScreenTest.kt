package com.example.notesapp.ui.folderdescription.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.R
import com.example.notesapp.ui.folderdescription.viewmodel.FolderDescriptionUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FolderDescriptionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun contentState_rendersFolderNameAndDescription() {
        composeRule.setContent {
            NotesTakingAppTheme {
                FolderDescriptionContent(
                    parentPadding = PaddingValues(0.dp),
                    state = FolderDescriptionUiState(
                        isLoading = false,
                        folderName = "Receipts",
                        description = "Client receipts",
                        canSave = false
                    ),
                    onDescriptionChanged = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("folder_description_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Receipts").assertIsDisplayed()
        composeRule.onNodeWithTag("folder_description_text_field").assertIsDisplayed()
        composeRule.onNodeWithTag("folder_description_save_button").assertIsNotEnabled()
    }

    @Test
    fun editingDescription_andSaveEnabled_emitsCallbacks() {
        var editedDescription = ""
        var saveClicked = false
        composeRule.setContent {
            NotesTakingAppTheme {
                var description by remember { mutableStateOf("") }
                FolderDescriptionContent(
                    parentPadding = PaddingValues(0.dp),
                    state = FolderDescriptionUiState(
                        isLoading = false,
                        folderName = "Receipts",
                        description = description,
                        canSave = description.isNotBlank()
                    ),
                    onDescriptionChanged = {
                        description = it
                        editedDescription = it
                    },
                    onSave = { saveClicked = true },
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("folder_description_text_field").performTextReplacement("Travel receipts")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("folder_description_save_button").assertIsEnabled()
        composeRule.onNodeWithTag("folder_description_save_button").performClick()

        assertEquals("Travel receipts", editedDescription)
        assertTrue(saveClicked)
    }

    @Test
    fun backButton_emitsBackCallback() {
        var backClicked = false
        composeRule.setContent {
            NotesTakingAppTheme {
                FolderDescriptionContent(
                    parentPadding = PaddingValues(0.dp),
                    state = FolderDescriptionUiState(isLoading = false, folderName = "Receipts"),
                    onDescriptionChanged = {},
                    onSave = {},
                    onBack = { backClicked = true }
                )
            }
        }

        composeRule.onNodeWithTag("folder_description_back_button").performClick()

        assertTrue(backClicked)
    }

    @Test
    fun loadingState_rendersProgress() {
        composeRule.setContent {
            NotesTakingAppTheme {
                FolderDescriptionContent(
                    parentPadding = PaddingValues(0.dp),
                    state = FolderDescriptionUiState(isLoading = true),
                    onDescriptionChanged = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("folder_description_loading").assertIsDisplayed()
    }

    @Test
    fun errorState_rendersError() {
        composeRule.setContent {
            NotesTakingAppTheme {
                FolderDescriptionContent(
                    parentPadding = PaddingValues(0.dp),
                    state = FolderDescriptionUiState(
                        isLoading = false,
                        errorMessageRes = R.string.folder_description_missing_error
                    ),
                    onDescriptionChanged = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithTag("folder_description_error").assertIsDisplayed()
        composeRule.onNodeWithText("Folder is no longer available.").assertIsDisplayed()
    }

    @Test
    fun givenTopParentPadding_whenRendering_thenTopAppBarIsAtTopAndNotShifted() {
        composeRule.setContent {
            NotesTakingAppTheme {
                FolderDescriptionContent(
                    parentPadding = PaddingValues(top = 100.dp),
                    state = FolderDescriptionUiState(
                        isLoading = false,
                        folderName = "Receipts",
                        description = "Client receipts"
                    ),
                    onDescriptionChanged = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }

        // Under the bug, the Scaffold's modifier padding(top = 100.dp) shifts the TopAppBar down,
        // so its top position will be >= 100.dp.
        // Under the fix, the TopAppBar should be at y = 0.
        val topPosition = composeRule.onNodeWithText("Folder description").getUnclippedBoundsInRoot().top
        val message = "TopAppBar should not be shifted down by parentPadding top: topPosition = $topPosition"
        assertTrue(
            message,
            topPosition < 50.dp
        )
    }
}
