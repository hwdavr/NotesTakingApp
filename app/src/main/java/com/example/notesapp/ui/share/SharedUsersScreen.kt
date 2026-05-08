package com.example.notesapp.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.notesapp.R
import com.example.notesapp.ui.theme.NotesTakingAppTheme

internal val SharedUsersBackground = Color(0xFFF4F7FF)
internal val SharedUsersCard = Color(0xFFF7FAFF)
internal val SharedUsersPrimary = Color(0xFF4C6FFF)
internal val SharedUsersTextPrimary = Color(0xFF1F2A44)
internal val SharedUsersTextSecondary = Color(0xFF7281A7)

enum class AccessRole {
    OWNER, EDITOR, VIEWER
}

data class SharedUserUiModel(
    val id: String,
    val name: String,
    val email: String,
    val initials: String,
    val accentColor: Color,
    val role: AccessRole
)

@Composable
fun SharedUsersScreen(
    parentPadding: PaddingValues,
    noteTitle: String,
    onBack: () -> Unit,
    onShareToNewUser: () -> Unit
) {
    SharedUsersScreenContent(
        parentPadding = parentPadding,
        noteTitle = noteTitle.ifBlank { stringResource(R.string.editor_untitled_note) },
        users = sampleSharedUsers(),
        onBack = onBack,
        onShareToNewUser = onShareToNewUser
    )
}

@Composable
fun SharedUsersScreenContent(
    parentPadding: PaddingValues,
    noteTitle: String,
    users: List<SharedUserUiModel>,
    onBack: () -> Unit,
    onShareToNewUser: () -> Unit
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
                            Text(
                                text = stringResource(R.string.shared_users_section_title),
                                color = SharedUsersTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(users, key = { it.id }) { user ->
                        SharedUserRow(user = user)
                    }
                }
            }

            Button(
                onClick = onShareToNewUser,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
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
            }

            AccessRolePill(role = user.role)
        }
    }
}

@Composable
private fun AccessRolePill(role: AccessRole) {
    val backgroundColor = when (role) {
        AccessRole.OWNER -> Color(0xFFEFF3FF)
        AccessRole.EDITOR -> Color(0xFFF1EEFF)
        AccessRole.VIEWER -> Color(0xFFFFF7ED)
    }
    val textColor = when (role) {
        AccessRole.OWNER -> Color(0xFF4C6FFF)
        AccessRole.EDITOR -> Color(0xFF6E4CFF)
        AccessRole.VIEWER -> Color(0xFFF59E0B)
    }
    val text = when (role) {
        AccessRole.OWNER -> "Owner"
        AccessRole.EDITOR -> "Editor"
        AccessRole.VIEWER -> "Viewer"
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun sampleSharedUsers(): List<SharedUserUiModel> = listOf(
    SharedUserUiModel(
        id = "hannah",
        name = "Hannah Lee",
        email = "hannah.lee@example.com",
        initials = "HL",
        accentColor = Color(0xFF6E7BFF),
        role = AccessRole.OWNER
    ),
    SharedUserUiModel(
        id = "marcus",
        name = "Marcus Chen",
        email = "marcus.chen@example.com",
        initials = "MC",
        accentColor = Color(0xFF2DB7A3),
        role = AccessRole.EDITOR
    ),
    SharedUserUiModel(
        id = "priya",
        name = "Priya Nair",
        email = "priya.nair@example.com",
        initials = "PN",
        accentColor = Color(0xFFF59E0B),
        role = AccessRole.VIEWER
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SharedUsersScreenPreview() {
    NotesTakingAppTheme {
        SharedUsersScreenContent(
            parentPadding = PaddingValues(),
            noteTitle = "Force update strategy",
            users = sampleSharedUsers(),
            onBack = {},
            onShareToNewUser = {}
        )
    }
}
