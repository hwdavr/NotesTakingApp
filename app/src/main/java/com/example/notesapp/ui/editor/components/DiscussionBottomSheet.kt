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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AlternateEmail
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.domain.comment.model.NoteBlockComment
import com.example.notesapp.ui.editor.model.MentionDateKind
import com.example.notesapp.ui.editor.model.MentionDateSuggestion
import com.example.notesapp.ui.editor.model.MentionNoteSuggestion
import com.example.notesapp.ui.editor.model.MentionUserSuggestion
import com.example.notesapp.ui.editor.viewmodel.DiscussionUiState
import com.example.notesapp.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionBottomSheet(
    isVisible: Boolean,
    comments: List<NoteBlockComment>,
    focusedBlockText: String,
    currentUserInitial: String,
    commentText: String,
    selectionStart: Int,
    selectionEnd: Int,
    onCommentValueChange: (String, Int, Int) -> Unit,
    onMentionButtonClick: () -> Unit,
    onSendComment: () -> Unit,
    onDismiss: () -> Unit,
    isMentionSuggestionsVisible: Boolean,
    mentionDates: List<MentionDateSuggestion>,
    mentionUsers: List<MentionUserSuggestion>,
    mentionNotes: List<MentionNoteSuggestion>,
    isMentionFooterVisible: Boolean,
    mentionFooterText: String,
    onMentionSelect: (String) -> Unit
) {
    if (!isVisible) return

    val colors = LocalAppColors.current
    val safeStart = selectionStart.coerceIn(0, commentText.length)
    val safeEnd = selectionEnd.coerceIn(0, commentText.length)
    val commentValue = TextFieldValue(
        text = commentText,
        selection = TextRange(safeStart, safeEnd)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.border) },
        modifier = Modifier.testTag("discussion_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp)
        ) {
            DiscussionHeader(onDismiss = onDismiss)
            FocusedBlockPreview(text = focusedBlockText)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
            ) {
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("discussion_empty_placeholder"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.discussion_empty_state),
                            style = MaterialTheme.typography.bodyMedium,
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
                        items(comments, key = { it.id }) { comment ->
                            CommentCard(comment = comment)
                        }
                    }
                }

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
            DiscussionComposer(
                value = commentValue,
                currentUserInitial = currentUserInitial,
                onValueChange = onCommentValueChange,
                onMentionButtonClick = onMentionButtonClick,
                onSendComment = onSendComment
            )
        }
    }
}

@Composable
internal fun DiscussionBottomSheetHost(
    state: DiscussionUiState,
    onValueChange: (String, Int, Int) -> Unit,
    onMentionButtonClick: () -> Unit,
    onSendComment: () -> Unit,
    onDismiss: () -> Unit,
    onMentionSelect: (String) -> Unit
) {
    DiscussionBottomSheet(
        isVisible = state.isVisible,
        comments = state.comments,
        focusedBlockText = state.focusedBlockText,
        currentUserInitial = state.currentUserInitial,
        commentText = state.commentText,
        selectionStart = state.selectionStart,
        selectionEnd = state.selectionEnd,
        onCommentValueChange = onValueChange,
        onMentionButtonClick = onMentionButtonClick,
        onSendComment = onSendComment,
        onDismiss = onDismiss,
        isMentionSuggestionsVisible = state.isMentionSuggestionsVisible,
        mentionDates = state.mentionDates,
        mentionUsers = state.mentionUsers,
        mentionNotes = state.mentionNotes,
        isMentionFooterVisible = state.isMentionFooterVisible,
        mentionFooterText = state.mentionFooterText,
        onMentionSelect = onMentionSelect
    )
}

@Composable
private fun DiscussionHeader(onDismiss: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(48.dp))
        Text(
            text = stringResource(R.string.discussion_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.testTag("discussion_sheet_title")
        )
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("discussion_sheet_close")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.discussion_close_description),
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun FocusedBlockPreview(text: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(colors.background, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(36.dp)
                .background(colors.accentYellow, RoundedCornerShape(2.dp))
                .testTag("discussion_focused_block_accent")
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .testTag("discussion_focused_block_text")
        )
    }
}

@Composable
private fun DiscussionComposer(
    value: TextFieldValue,
    currentUserInitial: String,
    onValueChange: (String, Int, Int) -> Unit,
    onMentionButtonClick: () -> Unit,
    onSendComment: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(colors.avatarBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentUserInitial.ifBlank {
                    stringResource(R.string.discussion_current_user_initial)
                },
                color = colors.avatarIcon,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        BasicTextField(
            value = value,
            onValueChange = { changed ->
                onValueChange(changed.text, changed.selection.start, changed.selection.end)
            },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp, max = 104.dp)
                .background(colors.background, RoundedCornerShape(24.dp))
                .border(1.dp, colors.border, RoundedCornerShape(24.dp))
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                .testTag("discussion_comment_input"),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = colors.textPrimary,
                fontSize = 14.sp
            ),
            maxLines = 4,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.discussion_comment_input_placeholder),
                                color = colors.textTertiary,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                    IconButton(
                        onClick = onMentionButtonClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("discussion_mention_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AlternateEmail,
                            contentDescription = stringResource(R.string.discussion_mention_description),
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        )

        IconButton(
            onClick = onSendComment,
            enabled = value.text.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .testTag("discussion_send_button")
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (value.text.isNotBlank()) colors.primary else colors.textTertiary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = stringResource(R.string.discussion_send_description),
                    tint = colors.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CommentCard(comment: NoteBlockComment) {
    val colors = LocalAppColors.current
    val authorName = comment.authorDisplayName?.takeIf { it.isNotBlank() }
        ?: comment.authorEmail?.substringBefore('@')?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.discussion_unknown_user)
    val avatarInitial = authorName.firstOrNull()?.uppercase() ?: stringResource(
        R.string.discussion_unknown_user_initial
    )
    val elapsed = (System.currentTimeMillis() - comment.createdAt).coerceAtLeast(0L)
    val relativeTime = when {
        elapsed < 60_000L -> stringResource(R.string.discussion_time_just_now)
        elapsed < 3_600_000L -> stringResource(
            R.string.discussion_time_minutes,
            elapsed / 60_000L
        )
        elapsed < 86_400_000L -> stringResource(
            R.string.discussion_time_hours,
            elapsed / 3_600_000L
        )
        else -> stringResource(
            R.string.discussion_time_days,
            elapsed / 86_400_000L
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("discussion_comment_card"),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.testTag("comment_author")
                )
                Text(
                    text = relativeTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    modifier = Modifier.testTag("comment_time")
                )
            }
            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
                modifier = Modifier.testTag("comment_body")
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.InsertEmoticon,
                    contentDescription = stringResource(R.string.discussion_react_description),
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.discussion_resolve_description),
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.discussion_more_description),
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingSuggestionsPopup(
    dates: List<MentionDateSuggestion>,
    users: List<MentionUserSuggestion>,
    notes: List<MentionNoteSuggestion>,
    isFooterVisible: Boolean,
    footerText: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .heightIn(max = 240.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .background(colors.surface, RoundedCornerShape(12.dp))
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
            .testTag("mention_suggestions_popup")
    ) {
        if (dates.isNotEmpty()) {
            SuggestionSectionTitle(text = stringResource(R.string.mention_dates_title))
            dates.forEach { date ->
                SuggestionDateRow(date = date, onClick = { onSelect(date.insertText) })
            }
        }

        if (users.isNotEmpty()) {
            SuggestionDivider(visible = dates.isNotEmpty())
            SuggestionSectionTitle(text = stringResource(R.string.mention_collaborators_title))
            users.forEach { user ->
                SuggestionUserRow(user = user, onClick = { onSelect(user.insertText) })
            }
        }

        if (notes.isNotEmpty()) {
            SuggestionDivider(visible = dates.isNotEmpty() || users.isNotEmpty())
            SuggestionSectionTitle(text = stringResource(R.string.mention_other_notes_title))
            notes.forEach { note ->
                SuggestionNoteRow(note = note, onClick = { onSelect(note.insertText) })
            }
        }

        if (isFooterVisible) {
            HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = stringResource(R.string.mention_more_results, footerText),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("mention_suggestions_popup_footer")
            )
        }
    }
}

@Composable
private fun SuggestionSectionTitle(text: String) {
    val colors = LocalAppColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = colors.textTertiary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun SuggestionDivider(visible: Boolean) {
    if (visible) {
        HorizontalDivider(
            color = LocalAppColors.current.border,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun SuggestionDateRow(date: MentionDateSuggestion, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            text = stringResource(date.kind.labelRes),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = date.formattedDate,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun SuggestionUserRow(user: MentionUserSuggestion, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("mention_user_item"),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(
                if (user.isYou) R.string.mention_badge_you else R.string.mention_badge_guest
            ),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            modifier = Modifier
                .background(colors.background, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun SuggestionNoteRow(note: MentionNoteSuggestion, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                text = note.title.ifBlank { stringResource(R.string.editor_untitled_note) },
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary
            )
            Text(
                text = note.folderName ?: stringResource(R.string.mention_my_notes),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private val MentionDateKind.labelRes: Int
    get() = when (this) {
        MentionDateKind.TODAY -> R.string.mention_date_today
        MentionDateKind.TOMORROW -> R.string.mention_date_tomorrow
        MentionDateKind.NEXT_TUESDAY -> R.string.mention_date_next_tuesday
    }
