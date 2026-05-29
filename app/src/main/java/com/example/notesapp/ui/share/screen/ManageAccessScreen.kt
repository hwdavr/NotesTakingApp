package com.example.notesapp.ui.share.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.example.notesapp.ui.theme.LocalAppColors
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
    errorMessageRes: Int?,
    isConfirmEnabled: Boolean,
    onBack: () -> Unit,
    onPermissionSelected: (String, ManageAccessPermission) -> Unit,
    onConfirm: () -> Unit
) {
    val colors = LocalAppColors.current
    Scaffold(
        modifier = Modifier.padding(top = parentPadding.calculateTopPadding()),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
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
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.testTag("manage_access_note_title")
                            )
                            Text(
                                text = stringResource(R.string.shared_users_section_title),
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (errorMessageRes != null) {
                                Text(
                                    text = stringResource(errorMessageRes),
                                    color = colors.error,
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
                                    color = colors.primary,
                                    modifier = Modifier.testTag("manage_access_loading")
                                )
                            }
                        }
                    } else if (users.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.shared_users_empty_state),
                                color = colors.textSecondary,
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
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    disabledContainerColor = colors.textTertiary,
                    disabledContentColor = colors.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.manage_access_confirm),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
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
    val colors = LocalAppColors.current
    Surface(
        color = colors.surface,
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
                        .background(colors.avatarPreset(user.accentColorIndex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.initials,
                        color = colors.onAccent,
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
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = user.email,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.isPending) {
                        Text(
                            text = stringResource(R.string.shared_users_status_pending),
                            color = colors.primary,
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
    val colors = LocalAppColors.current
    val isDelete = permission == ManageAccessPermission.DELETE
    val backgroundColor = when {
        !selected && !isDelete -> colors.surface
        !selected && isDelete -> colors.surface
        isDelete -> colors.error.copy(alpha = 0.12f)
        else -> colors.highlight
    }
    val borderColor = when {
        selected && isDelete -> colors.error
        selected -> colors.primary
        else -> colors.border
    }
    val contentColor = when {
        isDelete -> colors.error
        else -> colors.textPrimary
    }
    val iconTint = when {
        selected && isDelete -> colors.error
        selected -> colors.primary
        isDelete -> colors.error
        else -> colors.textSecondary
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(permission) }
            .testTag("manage_access_option")
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
@Suppress("UnusedPrivateMember")
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
                    accentColorIndex = 0,
                    currentPermission = ManageAccessPermission.EDITOR,
                    selectedPermission = ManageAccessPermission.EDITOR,
                    isPending = false
                ),
                ManageAccessUserUiModel(
                    id = "share_2",
                    name = "Clara Wong",
                    email = "clara@notesapp.com",
                    initials = "CW",
                    accentColorIndex = 1,
                    currentPermission = ManageAccessPermission.VIEWER,
                    selectedPermission = ManageAccessPermission.VIEWER,
                    isPending = false
                )
            ),
            isLoading = false,
            errorMessageRes = null,
            isConfirmEnabled = true,
            onBack = {},
            onPermissionSelected = { _, _ -> },
            onConfirm = {}
        )
    }
}
