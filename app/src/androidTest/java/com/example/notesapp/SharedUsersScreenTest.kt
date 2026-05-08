package com.example.notesapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.share.AccessRole
import com.example.notesapp.ui.share.SharedUserUiModel
import com.example.notesapp.ui.share.SharedUsersScreenContent
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
                        accentColor = Color(0xFF6E7BFF),
                        role = AccessRole.OWNER
                    )
                ),
                onBack = {},
                onShareToNewUser = {}
            )
        }

        composeRule.onNodeWithText("Shared users").assertIsDisplayed()
        composeRule.onNodeWithTag("shared_users_note_title").assertIsDisplayed()
        composeRule.onNodeWithText("People with access").assertIsDisplayed()
        composeRule.onNodeWithText("Hannah Lee").assertIsDisplayed()
        composeRule.onNodeWithText("Owner").assertIsDisplayed()
        composeRule.onNodeWithTag("shared_users_cta").assertIsDisplayed()
    }
}
