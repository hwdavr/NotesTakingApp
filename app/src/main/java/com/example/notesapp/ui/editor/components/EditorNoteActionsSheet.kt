package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.common.components.SheetActionRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorNoteActionsSheet(
    note: Note,
    onDismiss: () -> Unit,
    onAddToFavorites: () -> Unit,
    onMoveTo: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = Color(0xFF5F6770),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = note.title.ifBlank { stringResource(R.string.editor_untitled_note) },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF2F343A)
                    )
                )
            }
            HorizontalDivider(color = Color(0xFFE7EBF0), thickness = 1.dp)
            SheetActionRow(
                icon = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                label = stringResource(
                    if (note.isFavorite) {
                        R.string.folders_remove_from_favorites_action
                    } else {
                        R.string.folders_add_to_favorites_action
                    }
                ),
                onClick = onAddToFavorites,
                modifier = Modifier.testTag("add_to_favorites_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Folder,
                label = stringResource(R.string.folders_move_to_action),
                onClick = onMoveTo,
                modifier = Modifier.testTag("move_item_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Edit,
                label = stringResource(R.string.folders_rename_action),
                onClick = onRename,
                modifier = Modifier.testTag("rename_item_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.FileDownload,
                label = stringResource(R.string.editor_export_action),
                onClick = onExport,
                modifier = Modifier.testTag("export_item_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Archive,
                label = stringResource(R.string.folders_delete_action),
                onClick = onDelete,
                iconTint = Color(0xFFC44A4A),
                textColor = Color(0xFFC44A4A),
                modifier = Modifier.testTag("delete_item_action")
            )
        }
    }
}
