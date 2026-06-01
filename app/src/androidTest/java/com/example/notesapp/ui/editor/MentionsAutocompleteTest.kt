package com.example.notesapp.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.ui.editor.components.DiscussionBottomSheet
import com.example.notesapp.ui.editor.model.MentionDateSuggestion
import com.example.notesapp.ui.editor.model.MentionNoteSuggestion
import com.example.notesapp.ui.editor.model.MentionUserSuggestion
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MentionsAutocompleteTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mentionsAutocomplete_popupDisplays_andCompletesOnClick() {
        val dummyDates = listOf(
            MentionDateSuggestion(
                description = "Today",
                formattedDate = "15 May 2026",
                insertText = "@Today"
            )
        )
        val dummyUsers = listOf(
            MentionUserSuggestion(
                email = "walter@example.com",
                displayName = "Walter Huang",
                isYou = true,
                isOwner = true,
                displayBadge = "You",
                insertText = "@Walter Huang"
            )
        )
        val dummyNotes = listOf(
            MentionNoteSuggestion(
                id = "n1",
                title = "Important Note",
                folderBreadcrumb = "Work",
                insertText = "@Important Note"
            )
        )

        var completedText = ""
        var isDismissedOrClosed = false

        composeRule.setContent {
            var input by remember { mutableStateOf("@") }
            DiscussionBottomSheet(
                isVisible = true,
                comments = emptyList(),
                focusedBlockText = "Context block text snippet",
                commentText = input,
                onCommentTextChange = { input = it },
                onSendComment = {},
                onDismiss = { isDismissedOrClosed = true },
                isMentionSuggestionsVisible = true,
                mentionDates = dummyDates,
                mentionUsers = dummyUsers,
                mentionNotes = dummyNotes,
                isMentionFooterVisible = true,
                mentionFooterText = "... 5 more results",
                onMentionSelect = {
                    completedText = it
                }
            )
        }

        // Wait for bottom sheet animation to complete
        composeRule.waitForIdle()

        // Verify that mention suggestions popup card and its items are rendered
        composeRule.onNodeWithTag("mention_suggestions_popup").assertIsDisplayed()
        composeRule.onAllNodesWithTag("mention_date_item")[0].assertIsDisplayed()
        composeRule.onAllNodesWithTag("mention_user_item")[0].assertIsDisplayed()
        composeRule.onAllNodesWithTag("mention_note_item")[0].assertIsDisplayed()
        composeRule.onNodeWithTag("mention_suggestions_popup_footer").assertIsDisplayed()

        // Tap on a date suggestion and assert completion trigger
        composeRule.onAllNodesWithTag("mention_date_item")[0].performClick()
        assertEquals("@Today", completedText)
    }

    @Test
    fun mentionsAutocomplete_mentionButtonAppendsAtSymbol() {
        var textEntered = ""

        composeRule.setContent {
            var input by remember { mutableStateOf("") }
            DiscussionBottomSheet(
                isVisible = true,
                comments = emptyList(),
                focusedBlockText = "Context block text",
                commentText = input,
                onCommentTextChange = {
                    input = it
                    textEntered = it
                },
                onSendComment = {},
                onDismiss = {}
            )
        }

        // Wait for bottom sheet animation to complete
        composeRule.waitForIdle()

        // Initially input is empty
        assertEquals("", textEntered)

        // Perform click on alternate email (mention) button
        composeRule.onNodeWithTag("discussion_mention_button").performClick()

        // Assert that '@' character has been appended
        assertEquals("@", textEntered)
    }
}
