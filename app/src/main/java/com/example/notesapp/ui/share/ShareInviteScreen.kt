package com.example.notesapp.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import androidx.compose.material3.Icon

data class InvitePermissionUiModel(
    val id: String,
    val title: String,
    val subtitle: String
)

@Composable
fun ShareInviteScreen(
    parentPadding: PaddingValues,
    onBack: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("new.user@example.com") }
    var selectedPermissionId by rememberSaveable { mutableStateOf("full_access") }
    val permissions = remember {
        listOf(
            InvitePermissionUiModel(
                id = "read_only",
                title = "Read only",
                subtitle = "Can view but not edit"
            ),
            InvitePermissionUiModel(
                id = "full_access",
                title = "Full access",
                subtitle = "Can view and edit"
            )
        )
    }

    ShareInviteScreenContent(
        parentPadding = parentPadding,
        email = email,
        permissions = permissions,
        selectedPermissionId = selectedPermissionId,
        onEmailChange = { email = it },
        onPermissionSelected = { selectedPermissionId = it },
        onBack = onBack,
        onInvite = {}
    )
}

@Composable
fun ShareInviteScreenContent(
    parentPadding: PaddingValues,
    email: String,
    permissions: List<InvitePermissionUiModel>,
    selectedPermissionId: String,
    onEmailChange: (String) -> Unit,
    onPermissionSelected: (String) -> Unit,
    onBack: () -> Unit,
    onInvite: () -> Unit
) {
    Scaffold(
        containerColor = SharedUsersBackground
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("share_invite_cta"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SharedUsersPrimary,
                    contentColor = Color.White
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
            onEmailChange = {},
            onPermissionSelected = {},
            onBack = {},
            onInvite = {}
        )
    }
}
