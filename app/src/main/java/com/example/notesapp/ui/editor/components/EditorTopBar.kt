package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.notesapp.R
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
internal fun EditorTopBar(
    isBasicBlocksPanelOpen: Boolean,
    onCloseBasicBlocksPanel: () -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (isBasicBlocksPanelOpen) onCloseBasicBlocksPanel() else onBack()
            },
            modifier = Modifier.testTag("editor_top_back")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.collection_notes_back),
                tint = colors.textPrimary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    if (isBasicBlocksPanelOpen) onCloseBasicBlocksPanel() else onShare()
                },
                modifier = Modifier.testTag("editor_top_share")
            ) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.editor_share_description),
                    tint = colors.textPrimary
                )
            }
            IconButton(
                onClick = {
                    if (isBasicBlocksPanelOpen) onCloseBasicBlocksPanel() else onMore()
                },
                modifier = Modifier.testTag("editor_top_more")
            ) {
                Icon(
                    Icons.Outlined.MoreHoriz,
                    contentDescription = stringResource(R.string.editor_more_description),
                    tint = colors.textPrimary
                )
            }
        }
    }
}
