package com.example.notesapp.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.domain.comment.model.NoteBlockComment
import com.example.notesapp.ui.editor.components.DiscussionBottomSheet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscussionSheetUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun discussionSheet_rendersCorrectly_withEmptyComments() {
        composeRule.setContent {
            DiscussionBottomSheet(
                isVisible = true,
                comments = emptyList(),
                focusedBlockText = "Focused text block preview context snippet.",
                commentText = "",
                onCommentTextChange = {},
                onSendComment = {},
                onDismiss = {}
            )
        }

        // Assert sheet title and focused block content
        composeRule.onNodeWithTag("discussion_sheet_title").assertIsDisplayed().assertTextEquals("Discussion")
        composeRule.onNodeWithTag("discussion_focused_block_accent").assertIsDisplayed()
        composeRule.onNodeWithTag("discussion_focused_block_text")
            .assertIsDisplayed()
            .assertTextEquals("Focused text block preview context snippet.")

        // Assert empty placeholder is shown
        composeRule.onNodeWithTag("discussion_empty_placeholder").assertIsDisplayed()
    }

    @Test
    fun discussionSheet_rendersCommentsList_correctly() {
        val dummyComments = listOf(
            NoteBlockComment(
                id = "c1",
                noteId = "n1",
                blockId = "b1",
                authorUserId = "u1",
                authorDisplayName = "Walter Huang",
                authorEmail = "walter@example.com",
                body = "This is a great point!",
                // 10 mins ago
                createdAt = System.currentTimeMillis() - 600000,
                updatedAt = System.currentTimeMillis() - 600000
            ),
            NoteBlockComment(
                id = "c2",
                noteId = "n1",
                blockId = "b1",
                authorUserId = "u2",
                authorDisplayName = "Huang Guest",
                authorEmail = "guest@example.com",
                body = "I agree, let's proceed offline.",
                // 1 min ago
                createdAt = System.currentTimeMillis() - 60000,
                updatedAt = System.currentTimeMillis() - 60000
            )
        )

        composeRule.setContent {
            DiscussionBottomSheet(
                isVisible = true,
                comments = dummyComments,
                focusedBlockText = "Focused block preview",
                commentText = "",
                onCommentTextChange = {},
                onSendComment = {},
                onDismiss = {}
            )
        }

        // Verify comments lazy column is rendered
        composeRule.onNodeWithTag("discussion_comments_list").assertIsDisplayed()

        // Verify first comment card components
        composeRule.onAllNodesWithTag("discussion_comment_card")[0].assertIsDisplayed()
        composeRule.onAllNodesWithTag("comment_avatar")[0].assertIsDisplayed().assertTextEquals("W")
        composeRule.onAllNodesWithTag("comment_author")[0].assertIsDisplayed().assertTextEquals("Walter Huang")
        composeRule.onAllNodesWithTag("comment_body")[0].assertIsDisplayed().assertTextEquals("This is a great point!")

        // Verify second comment card components
        composeRule.onAllNodesWithTag("discussion_comment_card")[1].assertIsDisplayed()
        composeRule.onAllNodesWithTag("comment_avatar")[1].assertIsDisplayed().assertTextEquals("H")
        composeRule.onAllNodesWithTag("comment_author")[1].assertIsDisplayed().assertTextEquals("Huang Guest")
        composeRule.onAllNodesWithTag("comment_body")[1]
            .assertIsDisplayed()
            .assertTextEquals("I agree, let's proceed offline.")
    }

    @Test
    fun discussionSheet_typingAndSendingComment_invokesCallbacks() {
        var textEntered = ""
        var sendClicked = false

        composeRule.setContent {
            var input by remember { mutableStateOf("") }
            DiscussionBottomSheet(
                isVisible = true,
                comments = emptyList(),
                focusedBlockText = "Context",
                commentText = input,
                onCommentTextChange = {
                    input = it
                    textEntered = it
                },
                onSendComment = {
                    sendClicked = true
                },
                onDismiss = {}
            )
        }

        // Assert empty input field is visible
        val inputNode = composeRule.onNodeWithTag("discussion_comment_input")
        inputNode.assertIsDisplayed()

        // Type text
        inputNode.performTextInput("Hello World")
        assertEquals("Hello World", textEntered)

        // Perform click on send button
        composeRule.onNodeWithTag("discussion_send_button").performClick()
        assertTrue(sendClicked)
    }
}
