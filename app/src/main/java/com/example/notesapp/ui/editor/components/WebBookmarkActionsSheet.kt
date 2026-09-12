package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.ui.common.components.SheetActionRow
import com.example.notesapp.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebBookmarkActionsSheet(onDismiss: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val colors = LocalAppColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.testTag("web_bookmark_actions_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.web_bookmark_actions_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .testTag("web_bookmark_actions_sheet_title"),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
            )
            SheetActionRow(
                icon = Icons.Outlined.Edit,
                label = stringResource(R.string.web_bookmark_actions_edit),
                iconDescription = stringResource(R.string.web_bookmark_actions_edit_content_description),
                onClick = onEdit,
                modifier = Modifier.testTag("web_bookmark_actions_edit")
            )
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            SheetActionRow(
                icon = Icons.Outlined.Delete,
                label = stringResource(R.string.web_bookmark_actions_delete),
                iconDescription = stringResource(R.string.web_bookmark_actions_delete_content_description),
                onClick = onDelete,
                iconTint = colors.error,
                textColor = colors.error,
                modifier = Modifier.testTag("web_bookmark_actions_delete")
            )
        }
    }
}

@Composable
fun WebBookmarkDeleteConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val colors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("web_bookmark_delete_confirmation"),
        title = { Text(stringResource(R.string.web_bookmark_delete_title)) },
        text = { Text(stringResource(R.string.web_bookmark_delete_message)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag("web_bookmark_delete_confirm")
            ) {
                Text(
                    text = stringResource(R.string.web_bookmark_delete_confirm),
                    color = colors.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("web_bookmark_delete_cancel")
            ) {
                Text(stringResource(R.string.web_bookmark_delete_cancel))
            }
        }
    )
}
