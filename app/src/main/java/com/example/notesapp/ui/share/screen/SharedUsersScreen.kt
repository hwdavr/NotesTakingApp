package com.example.notesapp.ui.share.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.share.model.AccessRole
import com.example.notesapp.ui.share.model.SharedUserUiModel
import com.example.notesapp.ui.share.viewmodel.SharedUsersViewModel
import com.example.notesapp.ui.theme.NotesTakingAppTheme

internal val SharedUsersBackground = Color(0xFFF4F7FF)
internal val SharedUsersCard = Color(0xFFF7FAFF)
internal val SharedUsersPrimary = Color(0xFF4C6FFF)
internal val SharedUsersTextPrimary = Color(0xFF1F2A44)
internal val SharedUsersTextSecondary = Color(0xFF7281A7)
@Composable
fun SharedUsersScreen(
    parentPadding: PaddingValues,
    noteId: String,
    onBack: () -> Unit,
    onManageAccess: () -> Unit,
    onShareToNewUser: () -> Unit,
    viewModel: SharedUsersViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(noteId) {
        viewModel.load(noteId)
    }
    SharedUsersScreenContent(
        parentPadding = parentPadding,
        noteTitle = state.noteTitle.ifBlank { stringResource(R.string.editor_untitled_note) },
        users = state.users,
        isLoading = state.isLoading,
        errorMessageRes = state.errorMessageRes,
        onBack = onBack,
        onManageAccess = onManageAccess,
        onShareToNewUser = onShareToNewUser
    )
}
@Composable
fun SharedUsersScreenContent(
    parentPadding: PaddingValues,
    noteTitle: String,
    users: List<SharedUserUiModel>,
    isLoading: Boolean,
    errorMessageRes: Int?,
    onBack: () -> Unit,
    onManageAccess: () -> Unit,
    onShareToNewUser: () -> Unit
) {
    val collaboratorCount = users.count { it.role != AccessRole.OWNER }
    Scaffold(
        modifier = Modifier.padding(top = parentPadding.calculateTopPadding()),
        containerColor = SharedUsersBackground,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SharedUsersBackground)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SharedUsersBackground)
            ) {
                SharedUsersTopBar(
                    onBack = onBack,
                    title = stringResource(R.string.shared_users_title)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .testTag("shared_users_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 112.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = noteTitle,
                                color = SharedUsersTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("shared_users_note_title")
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.shared_users_section_title),
                                    color = SharedUsersTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = stringResource(R.string.shared_users_manage_access),
                                    color = SharedUsersPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable(onClick = onManageAccess)
                                        .testTag("shared_users_manage_access")
                                )
                            }
                            if (errorMessageRes != null) {
                                Text(
                                    text = stringResource(errorMessageRes),
                                    color = Color(0xFFC44A4A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = SharedUsersPrimary,
                                    modifier = Modifier.testTag("shared_users_loading")
                                )
                            }
                        }
                    } else {
                        items(users, key = { it.id }) { user ->
                            SharedUserRow(user = user)
                        }
                        if (collaboratorCount == 0) {
                            item {
                                Text(
                                    text = stringResource(R.string.shared_users_empty_state),
                                    color = SharedUsersTextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.testTag("shared_users_empty_state")
                                )
                            }
                        }
                    }
                }
            }
            Button(
                onClick = onShareToNewUser,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("shared_users_cta"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharedUsersPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.shared_users_cta),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.Outlined.PersonAddAlt1,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
@Composable
internal fun SharedUsersTopBar(onBack: () -> Unit, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("shared_users_back")) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.collection_notes_back),
                tint = SharedUsersPrimary
            )
        }
        Text(
            text = title,
            color = SharedUsersTextPrimary,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}
@Composable
private fun SharedUserRow(user: SharedUserUiModel) {
    Surface(
        color = SharedUsersCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(user.accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = user.name,
                    color = SharedUsersTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = user.email,
                    color = SharedUsersTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (user.isPending) {
                    Text(
                        text = stringResource(R.string.shared_users_status_pending),
                        color = SharedUsersPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            AccessRolePill(role = user.role)
        }
    }
}
@Composable
private fun AccessRolePill(role: AccessRole) {
    val backgroundColor = when (role) {
        AccessRole.OWNER -> Color(0xFFEFF3FF)
        AccessRole.FULL_ACCESS -> Color(0xFFF1EEFF)
        AccessRole.READ_ONLY -> Color(0xFFFFF7ED)
    }
    val textColor = when (role) {
        AccessRole.OWNER -> Color(0xFF4C6FFF)
        AccessRole.FULL_ACCESS -> Color(0xFF6E4CFF)
        AccessRole.READ_ONLY -> Color(0xFFF59E0B)
    }
    val textRes = when (role) {
        AccessRole.OWNER -> R.string.shared_users_role_owner
        AccessRole.FULL_ACCESS -> R.string.shared_users_role_full_access
        AccessRole.READ_ONLY -> R.string.shared_users_role_read_only
    }
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = stringResource(textRes),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SharedUsersScreenPreview() {
    NotesTakingAppTheme {
        SharedUsersScreenContent(
            parentPadding = PaddingValues(),
            noteTitle = "Force update strategy",
            users = listOf(
                SharedUserUiModel(
                    id = "owner",
                    name = "Owner User",
                    email = "owner@example.com",
                    initials = "OU",
                    accentColor = Color(0xFF6E7BFF),
                    role = AccessRole.OWNER,
                    isPending = false
                ),
                SharedUserUiModel(
                    id = "share_1",
                    name = "Hannah Lee",
                    email = "hannah.lee@example.com",
                    initials = "HL",
                    accentColor = Color(0xFF2DB7A3),
                    role = AccessRole.FULL_ACCESS,
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
}
