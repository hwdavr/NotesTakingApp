package com.example.notesapp.ui.share.screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.example.notesapp.ui.share.model.ManageAccessPermission
import com.example.notesapp.ui.share.model.ManageAccessUserUiModel
import com.example.notesapp.ui.share.viewmodel.ManageAccessEvent
import com.example.notesapp.ui.share.viewmodel.ManageAccessViewModel
import com.example.notesapp.ui.theme.NotesTakingAppTheme

@Composable
fun ManageAccessScreen(
    parentPadding: PaddingValues,
    noteId: String,
    onBack: () -> Unit,
    onConfirmSuccess: () -> Unit,
    viewModel: ManageAccessViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(noteId) {
        viewModel.load(noteId)
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is ManageAccessEvent.ConfirmSucceeded) {
                onConfirmSuccess()
            }
        }
    }

    ManageAccessScreenContent(
        parentPadding = parentPadding,
        noteTitle = state.noteTitle.ifBlank { stringResource(R.string.editor_untitled_note) },
        users = state.users,
        isLoading = state.isLoading,
        isSubmitting = state.isSubmitting,
        errorMessageRes = state.errorMessageRes,
        isConfirmEnabled = state.isConfirmEnabled,
        onBack = onBack,
        onPermissionSelected = viewModel::onPermissionSelected,
        onConfirm = viewModel::confirmChanges
    )
}

@Composable
fun ManageAccessScreenContent(
    parentPadding: PaddingValues,
    noteTitle: String,
    users: List<ManageAccessUserUiModel>,
    isLoading: Boolean,
    isSubmitting: Boolean,
    errorMessageRes: Int?,
    isConfirmEnabled: Boolean,
    onBack: () -> Unit,
    onPermissionSelected: (String, ManageAccessPermission) -> Unit,
    onConfirm: () -> Unit
) {
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
                    title = stringResource(R.string.manage_access_title)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .testTag("manage_access_list"),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 112.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = noteTitle,
                                color = SharedUsersTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("manage_access_note_title")
                            )
                            Text(
                                text = stringResource(R.string.shared_users_section_title),
                                color = SharedUsersTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (errorMessageRes != null) {
                                Text(
                                    text = stringResource(errorMessageRes),
                                    color = Color(0xFFC44A4A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.testTag("manage_access_error")
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
                                    modifier = Modifier.testTag("manage_access_loading")
                                )
                            }
                        }
                    } else if (users.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.shared_users_empty_state),
                                color = SharedUsersTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.testTag("manage_access_empty_state")
                            )
                        }
                    } else {
                        items(users, key = { it.id }) { user ->
                            ManageAccessUserCard(
                                user = user,
                                onPermissionSelected = { permission ->
                                    onPermissionSelected(user.id, permission)
                                }
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("manage_access_confirm"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharedUsersPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFAAB8C2),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.manage_access_confirm),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Done,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ManageAccessUserCard(
    user: ManageAccessUserUiModel,
    onPermissionSelected: (ManageAccessPermission) -> Unit
) {
    Surface(
        color = SharedUsersCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(user.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
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
                        fontSize = 15.sp
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
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ManageAccessPermissionOption(
                    permission = ManageAccessPermission.VIEWER,
                    label = stringResource(R.string.manage_access_viewer),
                    selected = user.selectedPermission == ManageAccessPermission.VIEWER,
                    onClick = onPermissionSelected
                )
                ManageAccessPermissionOption(
                    permission = ManageAccessPermission.EDITOR,
                    label = stringResource(R.string.manage_access_editor),
                    selected = user.selectedPermission == ManageAccessPermission.EDITOR,
                    onClick = onPermissionSelected
                )
                ManageAccessPermissionOption(
                    permission = ManageAccessPermission.DELETE,
                    label = stringResource(R.string.manage_access_delete),
                    selected = user.selectedPermission == ManageAccessPermission.DELETE,
                    onClick = onPermissionSelected
                )
            }
        }
    }
}

@Composable
private fun ManageAccessPermissionOption(
    permission: ManageAccessPermission,
    label: String,
    selected: Boolean,
    onClick: (ManageAccessPermission) -> Unit
) {
    val isDelete = permission == ManageAccessPermission.DELETE
    val backgroundColor = when {
        !selected && !isDelete -> Color.White
        !selected && isDelete -> Color.White
        isDelete -> Color(0xFFFFF4F1)
        else -> Color(0xFFEEF3FF)
    }
    val borderColor = when {
        selected && isDelete -> Color(0xFFD93C15)
        selected -> SharedUsersPrimary
        else -> Color(0xFFD9E2FF)
    }
    val contentColor = when {
        isDelete -> Color(0xFFD93C15)
        permission == ManageAccessPermission.EDITOR -> SharedUsersTextPrimary
        else -> SharedUsersTextPrimary
    }
    val iconTint = when {
        selected && isDelete -> Color(0xFFD93C15)
        selected -> SharedUsersPrimary
        isDelete -> Color(0xFFD93C15)
        else -> SharedUsersTextSecondary
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(permission) }
            .testTag("manage_access_option_${permission.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (selected) Icons.Outlined.Check else Icons.Outlined.Circle,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ManageAccessScreenPreview() {
    NotesTakingAppTheme {
        ManageAccessScreenContent(
            parentPadding = PaddingValues(),
            noteTitle = "Force update strategy",
            users = listOf(
                ManageAccessUserUiModel(
                    id = "share_1",
                    name = "Ben Lee",
                    email = "ben@notesapp.com",
                    initials = "BL",
                    accentColor = Color(0xFF6E56CF),
                    currentPermission = ManageAccessPermission.EDITOR,
                    selectedPermission = ManageAccessPermission.EDITOR,
                    isPending = false
                ),
                ManageAccessUserUiModel(
                    id = "share_2",
                    name = "Clara Wong",
                    email = "clara@notesapp.com",
                    initials = "CW",
                    accentColor = Color(0xFFF59E0B),
                    currentPermission = ManageAccessPermission.VIEWER,
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
            onConfirm = {}
        )
    }
}
