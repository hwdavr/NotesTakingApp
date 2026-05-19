package com.example.notesapp.ui.share.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.share.model.ManageAccessPermission
import com.example.notesapp.ui.share.model.ManageAccessUserUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManageAccessScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun manageAccessScreen_rendersDesignContent() {
        composeRule.setContent {
            ManageAccessScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteTitle = "Force update strategy",
                users = listOf(
                    ManageAccessUserUiModel(
                        id = "share1",
                        name = "Ben Lee",
                        email = "ben@notesapp.com",
                        initials = "BL",
                        accentColor = Color(0xFF6E56CF),
                        currentPermission = ManageAccessPermission.EDITOR,
                        selectedPermission = ManageAccessPermission.EDITOR,
                        isPending = false
                    )
                ),
                isLoading = false,
                isSubmitting = false,
                errorMessageRes = null,
                isConfirmEnabled = true,
                onBack = {},
                onPermissionSelected = { _, _ -> },
                onConfirm = {}
            )
        }

        composeRule.onNodeWithText("Manage Access").assertIsDisplayed()
        composeRule.onNodeWithTag("manage_access_note_title").assertIsDisplayed()
        composeRule.onNodeWithText("People with access").assertIsDisplayed()
        composeRule.onNodeWithText("Ben Lee").assertIsDisplayed()
        composeRule.onNodeWithText("Viewer").assertIsDisplayed()
        composeRule.onNodeWithText("Editor").assertIsDisplayed()
        composeRule.onNodeWithText("Delete").assertIsDisplayed()
        composeRule.onNodeWithTag("manage_access_confirm").assertIsEnabled()
    }

    @Test
    fun manageAccessConfirm_invokesCallback() {
        var confirmed = false

        composeRule.setContent {
            ManageAccessScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteTitle = "Force update strategy",
                users = listOf(
                    ManageAccessUserUiModel(
                        id = "share1",
                        name = "Ben Lee",
                        email = "ben@notesapp.com",
                        initials = "BL",
                        accentColor = Color(0xFF6E56CF),
                        currentPermission = ManageAccessPermission.EDITOR,
                        selectedPermission = ManageAccessPermission.VIEWER,
                        isPending = false
                    )
                ),
                isLoading = false,
                isSubmitting = false,
                errorMessageRes = null,
                isConfirmEnabled = true,
                onBack = {},
                onPermissionSelected = { _, _ -> },
                onConfirm = { confirmed = true }
            )
        }

        composeRule.onNodeWithTag("manage_access_confirm").performClick()

        assertTrue(confirmed)
    }

    @Test
    fun manageAccessConfirm_isDisabledWithoutChanges() {
        composeRule.setContent {
            ManageAccessScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteTitle = "Force update strategy",
                users = listOf(
                    ManageAccessUserUiModel(
                        id = "share1",
                        name = "Ben Lee",
                        email = "ben@notesapp.com",
                        initials = "BL",
                        accentColor = Color(0xFF6E56CF),
                        currentPermission = ManageAccessPermission.EDITOR,
                        selectedPermission = ManageAccessPermission.EDITOR,
                        isPending = false
                    )
                ),
                isLoading = false,
                isSubmitting = false,
                errorMessageRes = null,
                isConfirmEnabled = false,
                onBack = {},
                onPermissionSelected = { _, _ -> },
                onConfirm = {}
            )
        }

        composeRule.onNodeWithTag("manage_access_confirm").assertIsNotEnabled()
    }
}
