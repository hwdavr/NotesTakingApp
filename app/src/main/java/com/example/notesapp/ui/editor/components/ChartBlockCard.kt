@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
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
import com.example.notesapp.ui.editor.chart.ChartBitmapColors
import com.example.notesapp.ui.editor.chart.ChartBitmapRenderer
import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun ChartBlockCard(
    block: EditorBlock.ChartBlock,
    isEditable: Boolean,
    onUpdateTitle: (String) -> Unit,
    onUpdateCell: (rowIndex: Int, columnIndex: Int, value: String) -> Unit,
    onSelectedColumnChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalAppColors.current
    val chartData = remember(block) { ChartTableParser.parse(block) }
    var isDataView by rememberSaveable(block.id) { mutableStateOf(false) }
    var selectedPointIndex by rememberSaveable(block.id) { mutableStateOf<Int?>(null) }
    var showViewSheet by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    val bitmap = remember(
        block,
        colors.background,
        colors.primary,
        colors.textPrimary,
        colors.divider
    ) {
        ChartBitmapRenderer.render(
            block = block,
            colors = ChartBitmapColors(
                background = colors.surface.toArgb(),
                primary = colors.primary.toArgb(),
                text = colors.textPrimary.toArgb(),
                grid = colors.divider.toArgb()
            )
        )
    }
    val typeLabel = stringResource(chartTypeLabel(block.chartType))
    val viewDescription = stringResource(
        if (isDataView) R.string.editor_chart_view_data else R.string.editor_chart_view_chart
    )
    val optionsDescription = stringResource(R.string.editor_chart_options_description)
    val plotDescription = stringResource(
        R.string.editor_chart_plot_description,
        typeLabel,
        chartData.title,
        chartData.points.size
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, colors.border), RoundedCornerShape(12.dp))
            .background(colors.surface, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("editor_chart_block"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = {
                    selectedPointIndex = null
                    showViewSheet = true
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("editor_chart_view_cta")
                    .semantics {
                        role = Role.Button
                        contentDescription = viewDescription
                    }
            ) {
                Icon(Icons.Outlined.Visibility, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(
                        if (isDataView) R.string.editor_chart_view_data else R.string.editor_chart_view_chart
                    )
                )
            }
            if (isEditable) {
                BasicTextField(
                    value = block.title,
                    onValueChange = onUpdateTitle,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_chart_title"),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    singleLine = true,
                    decorationBox = { field ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colors.primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (block.title.isBlank()) {
                                Text(
                                    stringResource(R.string.editor_chart_title_placeholder),
                                    color = colors.textTertiary
                                )
                            }
                            field()
                        }
                    }
                )
            } else {
                Text(
                    text = chartData.title,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_chart_title"),
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            IconButton(
                onClick = {
                    selectedPointIndex = null
                    showOptionsSheet = true
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("editor_chart_options_cta")
                    .semantics { contentDescription = optionsDescription }
            ) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = stringResource(R.string.editor_chart_options_icon_description),
                    tint = colors.textPrimary
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().testTag("editor_chart_type"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(chartTypeIcon(block.chartType), contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(8.dp))
            Text(typeLabel, color = colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Text(
                text = chartData.selectedColumnLabel,
                modifier = Modifier.testTag("editor_chart_selected_column"),
                color = colors.primary,
                fontSize = 13.sp
            )
        }
        if (isDataView) {
            ChartDataTable(
                block = block,
                isEditable = isEditable,
                onUpdateCell = onUpdateCell,
                modifier = Modifier.testTag("editor_chart_table_view")
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("editor_chart_plot")
                    .semantics { contentDescription = plotDescription },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = plotDescription,
                    modifier = Modifier.fillMaxWidth()
                )
                if (chartData.points.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.editor_chart_empty_message),
                            color = colors.textSecondary,
                            modifier = Modifier.testTag("editor_chart_empty")
                        )
                        Text(
                            stringResource(R.string.editor_chart_empty_hint),
                            color = colors.textTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
                chartData.points.getOrNull(selectedPointIndex ?: -1)?.let { point ->
                    Text(
                        text = stringResource(
                            R.string.editor_chart_tooltip,
                            point.category,
                            point.value.toString()
                        ),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .background(colors.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("editor_chart_tooltip"),
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    chartData.points.forEachIndexed { index, point ->
                        val pointDescription = stringResource(
                            R.string.editor_chart_datum_description,
                            point.category,
                            point.value.toString()
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { selectedPointIndex = index }
                                .testTag("editor_chart_datum_target")
                                .semantics {
                                    role = Role.Button
                                    contentDescription = pointDescription
                                }
                        )
                    }
                }
            }
        }
        if (isEditable) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.End)
                    .size(48.dp)
                    .testTag("editor_chart_delete")
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.editor_chart_delete_description),
                    tint = colors.textSecondary
                )
            }
        }
    }
    if (showViewSheet) {
        ChartViewSheet(
            isDataView = isDataView,
            onSelect = { nextDataView ->
                isDataView = nextDataView
                showViewSheet = false
            },
            onDismiss = { showViewSheet = false }
        )
    }
    if (showOptionsSheet) {
        ChartOptionsSheet(
            block = block,
            isEditable = isEditable,
            onSelectColumn = { columnId ->
                onSelectedColumnChange(columnId)
                showOptionsSheet = false
            },
            onDismiss = { showOptionsSheet = false }
        )
    }
}

@Composable
private fun ChartDataTable(
    block: EditorBlock.ChartBlock,
    isEditable: Boolean,
    onUpdateCell: (rowIndex: Int, columnIndex: Int, value: String) -> Unit,
    modifier: Modifier
) {
    val colors = LocalAppColors.current
    Column(modifier = modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(8.dp))) {
        block.rows.forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) HorizontalDivider(color = colors.divider)
            Row(modifier = Modifier.fillMaxWidth()) {
                block.columnIds.indices.forEach { columnIndex ->
                    if (columnIndex > 0) Spacer(Modifier.width(1.dp).height(48.dp).background(colors.divider))
                    BasicTextField(
                        value = row.getOrNull(columnIndex).orEmpty().joinToString("") { it.text },
                        readOnly = !isEditable,
                        onValueChange = { onUpdateCell(rowIndex, columnIndex, it) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                            .testTag("editor_chart_data_cell"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartViewSheet(isDataView: Boolean, onSelect: (Boolean) -> Unit, onDismiss: () -> Unit) {
    val colors = LocalAppColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        modifier = Modifier.testTag("editor_chart_view_sheet")
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(stringResource(R.string.editor_chart_view_sheet_title), style = MaterialTheme.typography.titleLarge)
            ChartSheetChoice(
                label = stringResource(R.string.editor_chart_view_chart),
                selected = !isDataView,
                testTag = "editor_chart_view_option_chart",
                onClick = { onSelect(false) }
            )
            ChartSheetChoice(
                label = stringResource(R.string.editor_chart_view_data),
                selected = isDataView,
                testTag = "editor_chart_view_option_data",
                onClick = { onSelect(true) }
            )
        }
    }
}

@Composable
private fun ChartOptionsSheet(
    block: EditorBlock.ChartBlock,
    isEditable: Boolean,
    onSelectColumn: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        modifier = Modifier.testTag("editor_chart_options_sheet")
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(stringResource(R.string.editor_chart_options_title), style = MaterialTheme.typography.titleLarge)
            block.columnIds.drop(1).forEachIndexed { offset, columnId ->
                val label = block.rows.firstOrNull()?.getOrNull(offset + 1).orEmpty().joinToString("") { it.text }
                    .ifBlank { stringResource(R.string.editor_chart_column_fallback, offset + 2) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable(enabled = isEditable) { onSelectColumn(columnId) }
                        .testTag("editor_chart_option_column"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = columnId == block.selectedColumnId,
                        onClick = if (isEditable) ({ onSelectColumn(columnId) }) else null
                    )
                    Text(label, color = colors.textPrimary)
                }
            }
        }
    }
}

@Composable
private fun ChartSheetChoice(label: String, selected: Boolean, testTag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

private fun chartTypeLabel(chartType: ChartType): Int = when (chartType) {
    ChartType.BAR -> R.string.editor_chart_type_bar
    ChartType.LINE -> R.string.editor_chart_type_line
    ChartType.PIE -> R.string.editor_chart_type_pie
}

private fun chartTypeIcon(chartType: ChartType) = when (chartType) {
    ChartType.BAR -> Icons.Outlined.BarChart
    ChartType.LINE -> Icons.AutoMirrored.Outlined.ShowChart
    ChartType.PIE -> Icons.Outlined.PieChart
}
