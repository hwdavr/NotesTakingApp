package com.example.notesapp.ui.notes.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun NoteCard(
    title: String,
    preview: String,
    meta: String,
    color: androidx.compose.ui.graphics.Color,
    onMoreClick: (() -> Unit)? = null,
    moreActionsTestTag: String? = null,
    badgeTestTag: String? = null
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(24.dp),
        color = color
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (onMoreClick != null) {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = if (moreActionsTestTag != null) {
                            Modifier.testTag(moreActionsTestTag)
                        } else {
                            Modifier
                        }
                    ) {
                        Icon(Icons.Outlined.MoreHoriz, contentDescription = "More")
                    }
                } else {
                    Icon(Icons.Outlined.MoreHoriz, contentDescription = "More")
                }
            }
            Text(text = preview, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    modifier = Modifier
                        .then(
                            if (badgeTestTag != null) {
                                Modifier.testTag(badgeTestTag)
                            } else {
                                Modifier
                            }
                        )
                        .background(
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.55f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
