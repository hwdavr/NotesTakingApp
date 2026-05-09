package com.example.notesapp.ui.share.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.ui.share.viewmodel.ShareInviteEvent
import com.example.notesapp.ui.share.viewmodel.ShareInviteViewModel
import com.example.notesapp.ui.theme.NotesTakingAppTheme

data class InvitePermissionUiModel(
    val id: String,
    val title: String,
    val subtitle: String
)
@Composable
fun ShareInviteScreen(
    parentPadding: PaddingValues,
    noteId: String,
    onBack: () -> Unit,
    onInviteSuccess: () -> Unit,
    viewModel: ShareInviteViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(noteId) {
        viewModel.load(noteId)
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is ShareInviteEvent.InviteSucceeded) {
                onInviteSuccess()
            }
        }
    }
    ShareInviteScreenContent(
        parentPadding = parentPadding,
        email = state.email,
        permissions = invitePermissions(),
        selectedPermissionId = state.selectedRole.toPermissionId(),
        errorMessageRes = state.errorMessageRes,
        isInviteEnabled = state.isInviteEnabled,
        onEmailChange = viewModel::onEmailChange,
        onPermissionSelected = { permissionId ->
            viewModel.onRoleSelected(
                if (permissionId == PermissionIds.READ_ONLY) {
                    NoteShareAccessRole.READ_ONLY
                } else {
                    NoteShareAccessRole.FULL_ACCESS
                }
            )
        },
        onBack = onBack,
        onInvite = viewModel::invite
    )
}
@Composable
fun ShareInviteScreenContent(
    parentPadding: PaddingValues,
    email: String,
    permissions: List<InvitePermissionUiModel>,
    selectedPermissionId: String,
    errorMessageRes: Int?,
    isInviteEnabled: Boolean,
    onEmailChange: (String) -> Unit,
    onPermissionSelected: (String) -> Unit,
    onBack: () -> Unit,
    onInvite: () -> Unit
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
                    title = stringResource(R.string.share_invite_title)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.share_invite_email_label),
                            color = SharedUsersTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_invite_email"),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            textStyle = androidx.compose.material3.LocalTextStyle.current.copy(
                                color = SharedUsersTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SharedUsersCard,
                                unfocusedContainerColor = SharedUsersCard,
                                disabledContainerColor = SharedUsersCard,
                                focusedBorderColor = Color(0xFFD9E2FF),
                                unfocusedBorderColor = Color(0xFFD9E2FF),
                                cursorColor = SharedUsersPrimary
                            )
                        )
                        if (errorMessageRes != null) {
                            Text(
                                text = stringResource(errorMessageRes),
                                color = Color(0xFFC44A4A),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("share_invite_error")
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.share_invite_permissions_label),
                            color = SharedUsersTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        permissions.forEach { permission ->
                            PermissionOptionRow(
                                permission = permission,
                                selected = permission.id == selectedPermissionId,
                                onClick = { onPermissionSelected(permission.id) }
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onInvite,
                enabled = isInviteEnabled,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .navigationBarsPadding()
                    .imePadding()
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("share_invite_cta"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharedUsersPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFAAB8C2),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.share_invite_cta),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
@Composable
private fun PermissionOptionRow(
    permission: InvitePermissionUiModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) Color(0xFFEEF3FF) else SharedUsersCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("share_invite_permission_${permission.id}"),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) SharedUsersPrimary else Color(0xFFD9E2FF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (selected) Icons.Outlined.Check else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (selected) SharedUsersPrimary else SharedUsersTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = permission.title,
                    color = SharedUsersTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = permission.subtitle,
                    color = SharedUsersTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
private object PermissionIds {
    const val READ_ONLY = "read_only"
    const val FULL_ACCESS = "full_access"
}
@Composable
private fun invitePermissions(): List<InvitePermissionUiModel> = listOf(
    InvitePermissionUiModel(
        id = PermissionIds.READ_ONLY,
        title = stringResource(R.string.share_invite_read_only_title),
        subtitle = stringResource(R.string.share_invite_read_only_subtitle)
    ),
    InvitePermissionUiModel(
        id = PermissionIds.FULL_ACCESS,
        title = stringResource(R.string.share_invite_full_access_title),
        subtitle = stringResource(R.string.share_invite_full_access_subtitle)
    )
)
private fun NoteShareAccessRole.toPermissionId(): String = when (this) {
    NoteShareAccessRole.READ_ONLY -> PermissionIds.READ_ONLY
    NoteShareAccessRole.FULL_ACCESS -> PermissionIds.FULL_ACCESS
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ShareInviteScreenPreview() {
    NotesTakingAppTheme {
        ShareInviteScreenContent(
            parentPadding = PaddingValues(),
            email = "new.user@example.com",
            permissions = listOf(
                InvitePermissionUiModel("read_only", "Read only", "Can view but not edit"),
                InvitePermissionUiModel("full_access", "Full access", "Can view and edit")
            ),
            selectedPermissionId = "full_access",
            errorMessageRes = null,
            isInviteEnabled = true,
            onEmailChange = {},
            onPermissionSelected = {},
            onBack = {},
            onInvite = {}
        )
    }
}
