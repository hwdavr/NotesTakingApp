package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.domain.bookmark.webBookmarkHost
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.LocalAppColors

/**
 * Read-only presentation of one persisted web bookmark block inside the note document.
 * The title falls back to the URL host when stored metadata is blank so legacy or
 * partially-saved bookmarks remain legible.
 */
@Composable
fun WebBookmarkBlockCard(block: EditorBlock.WebBookmarkBlock, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val displayTitle = block.title.ifBlank { webBookmarkHost(block.url) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("editor_web_bookmark_block_${block.id}")
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Public,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = displayTitle,
                    modifier = Modifier.testTag("editor_web_bookmark_title_${block.id}"),
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (block.description.isNotBlank()) {
                    Text(
                        text = block.description,
                        modifier = Modifier.testTag("editor_web_bookmark_description_${block.id}"),
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (block.url.isNotBlank()) {
                    Text(
                        text = block.url,
                        modifier = Modifier.testTag("editor_web_bookmark_url_${block.id}"),
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
