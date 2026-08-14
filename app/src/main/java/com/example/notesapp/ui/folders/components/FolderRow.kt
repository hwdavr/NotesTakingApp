package com.example.notesapp.ui.folders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.notesapp.R
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun FolderRow(
    name: String,
    count: String,
    modifier: Modifier = Modifier,
    depth: Int = 0,
    onAddClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .background(LocalAppColors.current.surface, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.Folder,
                contentDescription = stringResource(R.string.folders_folder_icon_description),
                tint = LocalAppColors.current.textPrimary
            )
            Text(text = name, color = LocalAppColors.current.textPrimary, style = MaterialTheme.typography.bodyLarge)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = count,
                color = LocalAppColors.current.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            if (onAddClick != null) {
                Surface(
                    onClick = onAddClick,
                    shape = CircleShape,
                    color = LocalAppColors.current.primary.copy(alpha = 0.1f),
                    contentColor = LocalAppColors.current.primary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.folders_add_subfolder_description),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.folders_open_folder_description),
                    tint = LocalAppColors.current.textSecondary
                )
            }
        }
    }
}
