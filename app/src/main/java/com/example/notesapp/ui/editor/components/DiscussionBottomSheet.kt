package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.InsertEmoticon
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.domain.comment.model.NoteBlockComment
import com.example.notesapp.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionBottomSheet(
    isVisible: Boolean,
    comments: List<NoteBlockComment>,
    focusedBlockText: String,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSendComment: () -> Unit,
    onDismiss: () -> Unit,
    isMentionSuggestionsVisible: Boolean = false,
    mentionDates: List<com.example.notesapp.ui.editor.model.MentionDateSuggestion> = emptyList(),
    mentionUsers: List<com.example.notesapp.ui.editor.model.MentionUserSuggestion> = emptyList(),
    mentionNotes: List<com.example.notesapp.ui.editor.model.MentionNoteSuggestion> = emptyList(),
    isMentionFooterVisible: Boolean = false,
    mentionFooterText: String = "",
    onMentionSelect: (String) -> Unit = {}
) {
    if (!isVisible) return

    val colors = LocalAppColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = colors.border)
        },
        modifier = Modifier.testTag("discussion_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(32.dp))
                Text(
                    text = "Discussion",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.testTag("discussion_sheet_title")
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("discussion_sheet_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Yellow accent block preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(colors.background, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accent Bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .background(colors.accentYellow, RoundedCornerShape(2.dp))
                        .testTag("discussion_focused_block_accent")
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = focusedBlockText,
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("discussion_focused_block_text")
                )
            }

            // Comments list or Empty placeholder with FloatingSuggestionsPopup overlay
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("discussion_empty_placeholder"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No comments yet. Start the discussion!",
                            fontSize = 14.sp,
                            color = colors.textTertiary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("discussion_comments_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(comments) { comment ->
                            CommentCard(comment = comment)
                        }
                    }
                }

                // Mentions Autocomplete suggestions popup overlaying the comments area
                val hasSuggestions = mentionDates.isNotEmpty() ||
                    mentionUsers.isNotEmpty() ||
                    mentionNotes.isNotEmpty()
                if (isMentionSuggestionsVisible && hasSuggestions) {
                    FloatingSuggestionsPopup(
                        dates = mentionDates,
                        users = mentionUsers,
                        notes = mentionNotes,
                        isFooterVisible = isMentionFooterVisible,
                        footerText = mentionFooterText,
                        onSelect = onMentionSelect,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = colors.border, thickness = 1.dp)

            // Input Row Wrapper
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Static User Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.avatarBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "W",
                            color = colors.avatarIcon,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Text field container
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = onCommentTextChange,
                        placeholder = {
                            Text(
                                text = "Type a comment...",
                                color = colors.textTertiary,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 100.dp)
                            .testTag("discussion_comment_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colors.background,
                            unfocusedContainerColor = colors.background,
                            focusedBorderColor = colors.border,
                            unfocusedBorderColor = colors.border,
                            cursorColor = colors.primary
                        ),
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("discussion_attach_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AttachFile,
                                        contentDescription = "Attach file",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onCommentTextChange(commentText + "@") },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("discussion_mention_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AlternateEmail,
                                        contentDescription = "Mention",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.textPrimary,
                            fontSize = 14.sp
                        )
                    )

                    // Send Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.primary, CircleShape)
                            .clickable(onClick = onSendComment)
                            .testTag("discussion_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Send",
                            tint = colors.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentCard(comment: NoteBlockComment) {
    val colors = LocalAppColors.current
    val authorName = comment.authorDisplayName?.ifBlank { null }
        ?: comment.authorEmail?.substringBefore("@")
        ?: "User"
    val avatarInitial = authorName.firstOrNull()?.uppercase() ?: "U"

    // Calculate relative timestamp placeholder (e.g. 26m)
    val timeDiff = System.currentTimeMillis() - comment.createdAt
    val relativeTime = when {
        timeDiff < 60000 -> "Just now"
        timeDiff < 3600000 -> "${timeDiff / 60000}m ago"
        timeDiff < 86400000 -> "${timeDiff / 3600000}h ago"
        else -> "${timeDiff / 86400000}d ago"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discussion_comment_card"),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Author Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(colors.avatarPreset(comment.id.hashCode()), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarInitial,
                color = colors.onAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.testTag("comment_avatar")
            )
        }

        // Comment Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = authorName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    modifier = Modifier.testTag("comment_author")
                )
                Text(
                    text = relativeTime,
                    fontSize = 11.sp,
                    color = colors.textTertiary,
                    modifier = Modifier.testTag("comment_time")
                )
            }
            Text(
                text = comment.body,
                fontSize = 13.sp,
                color = colors.textPrimary,
                modifier = Modifier.testTag("comment_body")
            )

            // Static Action Icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.InsertEmoticon,
                    contentDescription = "React",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Resolve",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "More options",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingSuggestionsPopup(
    dates: List<com.example.notesapp.ui.editor.model.MentionDateSuggestion>,
    users: List<com.example.notesapp.ui.editor.model.MentionUserSuggestion>,
    notes: List<com.example.notesapp.ui.editor.model.MentionNoteSuggestion>,
    isFooterVisible: Boolean,
    footerText: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
            .testTag("mention_suggestions_popup")
    ) {
        // 1. Dates Section
        if (dates.isNotEmpty()) {
            Text(
                text = "Dates",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            dates.forEachIndexed { index, date ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(date.insertText) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("mention_date_item"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = stringResource(R.string.discussion_icon_schedule_description),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = date.description,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = date.formattedDate,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // 2. Collaborators Section
        if (users.isNotEmpty()) {
            if (dates.isNotEmpty()) {
                HorizontalDivider(
                    color = colors.border,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Text(
                text = "Collaborators",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            users.forEachIndexed { index, user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(user.insertText) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("mention_user_item"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Collaborator initials avatar
                    val initials = user.displayName.take(2).uppercase()
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(colors.avatarPreset(user.email.hashCode()), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = colors.onAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val badgeRes = if (user.displayBadge == "You") {
                        R.string.mention_badge_you
                    } else {
                        R.string.mention_badge_guest
                    }
                    Text(
                        text = stringResource(badgeRes),
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        modifier = Modifier
                            .background(colors.background, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // 3. Notes Section
        if (notes.isNotEmpty()) {
            if (dates.isNotEmpty() || users.isNotEmpty()) {
                HorizontalDivider(
                    color = colors.border,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Text(
                text = "Other Notes",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            notes.forEachIndexed { index, note ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(note.insertText) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("mention_note_item"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = stringResource(R.string.discussion_icon_note_description),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = note.folderBreadcrumb,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }

        // 4. Footer Section
        if (isFooterVisible) {
            HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("mention_suggestions_popup_footer"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = footerText,
                    fontSize = 11.sp,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
