package com.example.notesapp.ui.share.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShareInviteScreenTest {
    @get:Rule
    val composeRule = createComposeRule()
    @Test
    fun shareInviteScreen_rendersDesignContentAndSelection() {
        composeRule.setContent {
            ShareInviteScreenContent(
                parentPadding = PaddingValues(0.dp),
                email = "new.user@example.com",
                permissions = listOf(
                    InvitePermissionUiModel(
                        id = "read_only",
                        title = "Read only",
                        subtitle = "Can view but not edit"
                    ),
                    InvitePermissionUiModel(
                        id = "full_access",
                        title = "Full access",
                        subtitle = "Can view and edit"
                    )
                ),
                selectedPermissionId = "full_access",
                errorMessageRes = null,
                isInviteEnabled = true,
                onEmailChange = {},
                onPermissionSelected = {},
                onBack = {},
                onInvite = {}
            )
        }
        composeRule.onNodeWithText("Share to new user").assertIsDisplayed()
        composeRule.onNodeWithTag("share_invite_email").assertIsDisplayed()
        composeRule.onNodeWithText("Permissions").assertIsDisplayed()
        composeRule.onNodeWithText("Full access").assertIsDisplayed()
        composeRule.onNodeWithTag("share_invite_cta").assertIsDisplayed()
    }
    @Test
    fun sharedUsersCta_triggersNavigationCallback() {
        var clicked = false
        composeRule.setContent {
            SharedUsersScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteTitle = "Force update strategy",
                users = emptyList(),
                isLoading = false,
                errorMessageRes = null,
                onBack = {},
                onManageAccess = {},
                onShareToNewUser = { clicked = true }
            )
        }
        composeRule.onNodeWithTag("shared_users_cta").performClick()
        assertTrue(clicked)
    }
}
