package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.notesapp.R
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
internal fun TableColumnHandle(modifier: Modifier, onClick: () -> Unit) {
    TableHandleStrip(
        modifier = modifier,
        tag = "table_column_handle",
        visualTag = "table_column_handle_visual",
        description = stringResource(R.string.table_column_handle_description),
        isHorizontal = true,
        contentAlignment = Alignment.BottomCenter,
        onClick = onClick
    )
}

@Composable
internal fun TableRowHandle(modifier: Modifier, onClick: () -> Unit) {
    TableHandleStrip(
        modifier = modifier,
        tag = "table_row_handle",
        visualTag = "table_row_handle_visual",
        description = stringResource(R.string.table_row_handle_description),
        isHorizontal = false,
        contentAlignment = Alignment.CenterEnd,
        onClick = onClick
    )
}

@Composable
private fun TableHandleStrip(
    modifier: Modifier,
    tag: String,
    visualTag: String,
    description: String,
    isHorizontal: Boolean,
    contentAlignment: Alignment,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .then(if (isHorizontal) Modifier.fillMaxHeight() else Modifier.fillMaxWidth())
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            }
            .testTag(tag),
        contentAlignment = contentAlignment
    ) {
        Box(
            modifier = if (isHorizontal) {
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        colors.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    )
                    .testTag(visualTag)
            } else {
                Modifier
                    .fillMaxHeight()
                    .width(12.dp)
                    .background(
                        colors.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    )
                    .testTag(visualTag)
            }
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(colors.primary, CircleShape)
        )
    }
}
