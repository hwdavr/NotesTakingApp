package com.example.notesapp.ui.share.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.share.model.AccessRole
import com.example.notesapp.ui.share.model.SharedUserUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedUsersScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sharedUsersScreen_rendersDesignContent() {
        composeRule.setContent {
            SharedUsersScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteTitle = "Force update strategy",
                users = listOf(
                    SharedUserUiModel(
                        id = "1",
                        name = "Hannah Lee",
                        email = "hannah.lee@example.com",
                        initials = "HL",
                        accentColorIndex = 0,
                        role = AccessRole.OWNER,
                        isPending = false
                    )
                ),
                isLoading = false,
                errorMessageRes = null,
                onBack = {},
                onManageAccess = {},
                onShareToNewUser = {}
            )
        }
        composeRule.onNodeWithText("Shared users").assertIsDisplayed()
        composeRule.onNodeWithTag("shared_users_note_title").assertIsDisplayed()
        composeRule.onNodeWithText("People with access").assertIsDisplayed()
        composeRule.onNodeWithTag("shared_users_manage_access").assertIsDisplayed()
        composeRule.onNodeWithText("Hannah Lee").assertIsDisplayed()
        composeRule.onNodeWithText("Owner").assertIsDisplayed()
        composeRule.onNodeWithTag("shared_users_cta").assertIsDisplayed()
    }

    @Test
    fun manageAccessLink_invokesCallback() {
        var clicked = false

        composeRule.setContent {
            SharedUsersScreenContent(
                parentPadding = PaddingValues(0.dp),
                noteTitle = "Force update strategy",
                users = emptyList(),
                isLoading = false,
                errorMessageRes = null,
                onBack = {},
                onManageAccess = { clicked = true },
                onShareToNewUser = {}
            )
        }

        composeRule.onNodeWithTag("shared_users_manage_access").performClick()

        assertTrue(clicked)
    }
}
