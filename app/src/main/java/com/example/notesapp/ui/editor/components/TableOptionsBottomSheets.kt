package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.ui.editor.model.TableHandleAction
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun TableColumnOptionsSheet(
    blockId: String,
    columnIndex: Int,
    onDismiss: () -> Unit,
    onAction: (TableHandleAction) -> Unit
) {
    TableOptionsBottomSheet(
        titleRes = R.string.table_column_options_title,
        sheetTag = "table_column_options_sheet",
        onDismiss = onDismiss,
        actions = listOf(
            TableSheetAction(
                labelRes = R.string.table_insert_column_left,
                icon = Icons.Outlined.Add,
                testTag = "table_insert_column_left",
                onClick = {
                    onAction(TableHandleAction.InsertColumnLeft(blockId, columnIndex))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_insert_column_right,
                icon = Icons.Outlined.Add,
                testTag = "table_insert_column_right",
                onClick = {
                    onAction(TableHandleAction.InsertColumnRight(blockId, columnIndex))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_clear_column,
                icon = Icons.Outlined.Clear,
                testTag = "table_clear_column",
                onClick = {
                    onAction(TableHandleAction.ClearColumn(blockId, columnIndex))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_delete_column,
                icon = Icons.Outlined.Delete,
                testTag = "table_delete_column",
                isDestructive = true,
                onClick = {
                    onAction(TableHandleAction.DeleteColumn(blockId, columnIndex))
                    onDismiss()
                }
            )
        )
    )
}

@Composable
fun TableRowOptionsSheet(
    blockId: String,
    rowIndex: Int,
    onDismiss: () -> Unit,
    onAction: (TableHandleAction) -> Unit
) {
    TableOptionsBottomSheet(
        titleRes = R.string.table_row_options_title,
        sheetTag = "table_row_options_sheet",
        onDismiss = onDismiss,
        actions = listOf(
            TableSheetAction(
                labelRes = R.string.table_insert_row_above,
                icon = Icons.Outlined.Add,
                testTag = "table_insert_row_above",
                onClick = {
                    onAction(TableHandleAction.InsertRowAbove(blockId, rowIndex))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_insert_row_below,
                icon = Icons.Outlined.Add,
                testTag = "table_insert_row_below",
                onClick = {
                    onAction(TableHandleAction.InsertRowBelow(blockId, rowIndex))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_clear_row,
                icon = Icons.Outlined.Clear,
                testTag = "table_clear_row",
                onClick = {
                    onAction(TableHandleAction.ClearRow(blockId, rowIndex))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_delete_row,
                icon = Icons.Outlined.Delete,
                testTag = "table_delete_row",
                isDestructive = true,
                onClick = {
                    onAction(TableHandleAction.DeleteRow(blockId, rowIndex))
                    onDismiss()
                }
            )
        )
    )
}

@Composable
fun TableOptionsSheet(
    blockId: String,
    onDismiss: () -> Unit,
    onAction: (TableHandleAction) -> Unit
) {
    TableOptionsBottomSheet(
        titleRes = R.string.table_options_title,
        sheetTag = "table_options_sheet",
        onDismiss = onDismiss,
        actions = listOf(
            TableSheetAction(
                labelRes = R.string.table_clear_entire,
                icon = Icons.Outlined.Clear,
                testTag = "table_clear_all",
                onClick = {
                    onAction(TableHandleAction.ClearTable(blockId))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_duplicate,
                icon = Icons.Outlined.ContentCopy,
                testTag = "table_duplicate",
                onClick = {
                    onAction(TableHandleAction.DuplicateTable(blockId))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_fit_to_width,
                icon = Icons.Outlined.OpenInFull,
                testTag = "table_fit_to_width",
                onClick = {
                    onAction(TableHandleAction.ToggleTableFitToWidth(blockId))
                    onDismiss()
                }
            ),
            TableSheetAction(
                labelRes = R.string.table_delete,
                icon = Icons.Outlined.Delete,
                testTag = "table_delete",
                isDestructive = true,
                onClick = {
                    onAction(TableHandleAction.DeleteTable(blockId))
                    onDismiss()
                }
            )
        )
    )
}

private data class TableSheetAction(
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableOptionsBottomSheet(
    titleRes: Int,
    sheetTag: String,
    onDismiss: () -> Unit,
    actions: List<TableSheetAction>
) {
    val colors = LocalAppColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.testTag(sheetTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(titleRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp)
                    .testTag(sheetTag + "_title"),
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            actions.forEachIndexed { index, action ->
                if (index == actions.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(sheetTag + "_delete_divider"),
                        color = colors.divider,
                        thickness = 1.dp
                    )
                }
                TableSheetActionRow(action = action)
            }
        }
    }
}

@Composable
private fun TableSheetActionRow(action: TableSheetAction) {
    val colors = LocalAppColors.current
    val label = stringResource(action.labelRes)
    val tint = if (action.isDestructive) colors.error else colors.textPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(
                role = Role.Button,
                onClick = action.onClick
            )
            .semantics {
                contentDescription = label
                role = Role.Button
            }
            .testTag(action.testTag)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
