@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.domain.comment.model.NoteBlockComment
import com.example.notesapp.ui.editor.model.MentionDateKind
import com.example.notesapp.ui.editor.model.MentionDateSuggestion
import com.example.notesapp.ui.editor.model.MentionNoteSuggestion
import com.example.notesapp.ui.editor.model.MentionUserSuggestion
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiscussionBottomSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun visibleSheet_rendersDiscussionAndMentionSections_andSelectsNote() {
        var selectedMention = ""
        composeRule.setContent {
            NotesTakingAppTheme {
                DiscussionBottomSheet(
                    isVisible = true,
                    comments = listOf(
                        NoteBlockComment(
                            id = "comment-1",
                            noteId = "note-1",
                            blockId = "block-1",
                            authorUserId = "user-1",
                            authorDisplayName = "Alice",
                            authorEmail = "alice@example.com",
                            body = "Please check the roadmap.",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    ),
                    focusedBlockText = "The paragraph under review",
                    currentUserInitial = "M",
                    commentText = "@",
                    selectionStart = 1,
                    selectionEnd = 1,
                    onCommentValueChange = { _, _, _ -> },
                    onMentionButtonClick = {},
                    onSendComment = {},
                    onDismiss = {},
                    isMentionSuggestionsVisible = true,
                    mentionDates = listOf(
                        MentionDateSuggestion(
                            kind = MentionDateKind.TODAY,
                            formattedDate = "12 Sep 2026",
                            insertText = "@Today"
                        )
                    ),
                    mentionUsers = listOf(
                        MentionUserSuggestion(
                            email = "alice@example.com",
                            displayName = "Alice",
                            isYou = false,
                            isOwner = false,
                            insertText = "@Alice"
                        )
                    ),
                    mentionNotes = listOf(
                        MentionNoteSuggestion(
                            id = "note-2",
                            title = "Roadmap",
                            folderName = "Planning",
                            insertText = "@Roadmap"
                        )
                    ),
                    isMentionFooterVisible = true,
                    mentionFooterText = "2",
                    onMentionSelect = { selectedMention = it }
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("discussion_sheet_title").assertIsDisplayed()
        composeRule.onNodeWithTag("discussion_focused_block_text").assertIsDisplayed()
        composeRule.onNodeWithTag("discussion_comment_card").assertIsDisplayed()
        composeRule.onNodeWithTag("discussion_comment_input").assertIsDisplayed()
        composeRule.onNodeWithTag("mention_suggestions_popup").assertIsDisplayed()
        composeRule.onNodeWithText("Dates").assertIsDisplayed()
        composeRule.onNodeWithText("Collaborators").assertIsDisplayed()
        composeRule.onNodeWithText("Other notes").assertIsDisplayed()
        composeRule.onNodeWithTag("mention_note_item").performClick()

        assertEquals("@Roadmap", selectedMention)
    }

    @Test
    fun hiddenSheet_doesNotAddDiscussionSemantics() {
        composeRule.setContent {
            NotesTakingAppTheme {
                DiscussionBottomSheet(
                    isVisible = false,
                    comments = emptyList(),
                    focusedBlockText = "",
                    currentUserInitial = "M",
                    commentText = "",
                    selectionStart = 0,
                    selectionEnd = 0,
                    onCommentValueChange = { _, _, _ -> },
                    onMentionButtonClick = {},
                    onSendComment = {},
                    onDismiss = {},
                    isMentionSuggestionsVisible = false,
                    mentionDates = emptyList(),
                    mentionUsers = emptyList(),
                    mentionNotes = emptyList(),
                    isMentionFooterVisible = false,
                    mentionFooterText = "",
                    onMentionSelect = {}
                )
            }
        }

        assertTrue(composeRule.onAllNodesWithTag("discussion_bottom_sheet").fetchSemanticsNodes().isEmpty())
    }
}
