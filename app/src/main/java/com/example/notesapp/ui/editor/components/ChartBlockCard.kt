@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Add
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notesapp.R
import com.example.notesapp.ui.editor.chart.ChartBitmapColors
import com.example.notesapp.ui.editor.chart.ChartBitmapRenderer
import com.example.notesapp.ui.editor.chart.ChartInteractionState
import com.example.notesapp.ui.editor.chart.ChartRenderState
import com.example.notesapp.ui.editor.chart.ChartSelectionEvent
import com.example.notesapp.ui.editor.chart.ChartSelectionReducer
import com.example.notesapp.ui.editor.chart.ChartSheet
import com.example.notesapp.ui.editor.mapper.ChartColumnOption
import com.example.notesapp.ui.editor.mapper.ChartData
import com.example.notesapp.ui.editor.mapper.ChartPoint
import com.example.notesapp.ui.editor.mapper.ChartTableParser
import com.example.notesapp.ui.editor.mapper.ChartType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.columnOptions
import com.example.notesapp.ui.editor.mapper.normalized
import com.example.notesapp.ui.editor.model.ChartTableAction
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun ChartBlockCard(
    block: EditorBlock.ChartBlock,
    isEditable: Boolean,
    onUpdateTitle: (String) -> Unit,
    onUpdateCell: (rowIndex: Int, columnIndex: Int, value: String) -> Unit,
    onSelectedColumnChange: (String) -> Unit,
    onDelete: () -> Unit,
    onAddRow: () -> Unit,
    onAddColumn: () -> Unit,
    onTableAction: (ChartTableAction) -> Unit
) {
    val colors = LocalAppColors.current
    val normalizedBlock = remember(block) { block.normalized() }
    val chartData = remember(normalizedBlock) { ChartTableParser.parse(normalizedBlock) }
    var isDataView by rememberSaveable(block.id) { mutableStateOf(false) }
    var interactionState by remember(block.id) { mutableStateOf(ChartInteractionState()) }
    val bitmapResult = remember(
        normalizedBlock,
        interactionState.selectedPointIndex,
        colors.background,
        colors.primary,
        colors.textPrimary,
        colors.divider
    ) {
        runCatching {
            ChartBitmapRenderer.render(
                block = normalizedBlock,
                selectedPointIndex = interactionState.selectedPointIndex,
                colors = ChartBitmapColors(
                    background = colors.surface.toArgb(),
                    primary = colors.primary.toArgb(),
                    text = colors.textPrimary.toArgb(),
                    grid = colors.divider.toArgb()
                )
            )
        }
    }
    LaunchedEffect(bitmapResult.isSuccess, chartData.points.size) {
        interactionState = ChartSelectionReducer.reduce(
            interactionState,
            if (bitmapResult.isSuccess) {
                ChartSelectionEvent.Rendered(chartData.points.size)
            } else {
                ChartSelectionEvent.RenderFailed
            }
        )
    }
    val bitmap = bitmapResult.getOrNull()
    val selectedPointIndex = interactionState.selectedPointIndex
    val typeLabel = stringResource(chartTypeLabel(normalizedBlock.chartType))
    val selectedColumnLabel = chartData.selectedColumnHeader
        ?: stringResource(R.string.editor_chart_column_fallback, chartData.selectedColumnIndex + 1)
    val viewLabel = stringResource(
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
                    interactionState = ChartSelectionReducer.reduce(
                        interactionState,
                        ChartSelectionEvent.SheetOpened(ChartSheet.VIEW)
                    )
                },
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("editor_chart_view_cta")
                    .semantics {
                        role = Role.Button
                        contentDescription = viewLabel
                    }
            ) {
                Icon(Icons.Outlined.Visibility, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(viewLabel)
            }
            if (isEditable) {
                BasicTextField(
                    value = normalizedBlock.title,
                    onValueChange = onUpdateTitle,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_chart_title"),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
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
                            if (normalizedBlock.title.isBlank()) {
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
                    textAlign = TextAlign.Center
                )
            }
            IconButton(
                onClick = {
                    interactionState = ChartSelectionReducer.reduce(
                        interactionState,
                        ChartSelectionEvent.SheetOpened(ChartSheet.OPTIONS)
                    )
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag("editor_chart_type"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(chartTypeIcon(normalizedBlock.chartType), contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(8.dp))
            Text(typeLabel, color = colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Text(
                text = selectedColumnLabel,
                modifier = Modifier.testTag("editor_chart_selected_column"),
                color = colors.primary,
                fontSize = 13.sp
            )
        }
        if (isDataView) {
            ChartDataTable(
                block = normalizedBlock,
                selectedColumnLabel = selectedColumnLabel,
                isEditable = isEditable,
                onUpdateCell = onUpdateCell,
                onTableAction = onTableAction,
                modifier = Modifier.testTag("editor_chart_table_view")
            )
        } else {
            ChartBlockPlot(
                chartData = chartData,
                renderState = interactionState.renderState,
                bitmap = bitmap,
                selectedPointIndex = selectedPointIndex,
                selectedColumnLabel = selectedColumnLabel,
                plotDescription = plotDescription,
                onDatumTapped = { index ->
                    interactionState = ChartSelectionReducer.reduce(
                        interactionState,
                        ChartSelectionEvent.DatumTapped(index)
                    )
                },
                onTooltipDismissed = {
                    interactionState = ChartSelectionReducer.reduce(
                        interactionState,
                        ChartSelectionEvent.TooltipDismissed
                    )
                }
            )
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
    ChartSheetHost(
        openSheet = interactionState.openSheet,
        block = normalizedBlock,
        isEditable = isEditable,
        isDataView = isDataView,
        onSelectView = { nextDataView ->
            isDataView = nextDataView
            interactionState = ChartSelectionReducer.reduce(
                interactionState,
                ChartSelectionEvent.SheetDismissed
            )
        },
        onOpenDataColumn = {
            interactionState = ChartSelectionReducer.reduce(
                interactionState,
                ChartSelectionEvent.SheetOpened(ChartSheet.DATA_COLUMN)
            )
        },
        onAddRow = {
            onAddRow()
            interactionState = ChartSelectionReducer.reduce(
                interactionState,
                ChartSelectionEvent.SheetDismissed
            )
        },
        onAddColumn = {
            onAddColumn()
            interactionState = ChartSelectionReducer.reduce(
                interactionState,
                ChartSelectionEvent.SheetDismissed
            )
        },
        onSelectColumn = { columnId ->
            onSelectedColumnChange(columnId)
            interactionState = ChartSelectionReducer.reduce(
                interactionState,
                ChartSelectionEvent.SheetDismissed
            )
        },
        onBack = {
            interactionState = ChartSelectionReducer.reduce(
                interactionState,
                ChartSelectionEvent.SheetOpened(ChartSheet.OPTIONS)
            )
        },
        onDismiss = {
            interactionState = ChartSelectionReducer.reduce(
                interactionState,
                ChartSelectionEvent.SheetDismissed
            )
        }
    )
}

@Composable
private fun ChartBlockPlot(
    chartData: ChartData,
    renderState: ChartRenderState,
    bitmap: Bitmap?,
    selectedPointIndex: Int?,
    selectedColumnLabel: String,
    plotDescription: String,
    onDatumTapped: (Int) -> Unit,
    onTooltipDismissed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .testTag("editor_chart_plot")
            .semantics { contentDescription = plotDescription },
        contentAlignment = Alignment.Center
    ) {
        if (renderState == ChartRenderState.ERROR || bitmap == null) {
            ChartRenderError()
        } else {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = plotDescription,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (renderState == ChartRenderState.EMPTY) {
            ChartEmptyState()
        }
        if (chartData.points.isNotEmpty() && bitmap != null) {
            ChartDatumTargets(
                chartData = chartData,
                selectedPointIndex = selectedPointIndex,
                onDatumTapped = onDatumTapped
            )
        }
        chartData.points.getOrNull(selectedPointIndex ?: -1)?.let { point ->
            ChartTooltip(
                point = point,
                selectedColumnLabel = selectedColumnLabel,
                alignmentModifier = Modifier.align(Alignment.TopCenter),
                onDismiss = onTooltipDismissed
            )
        }
    }
}

@Composable
private fun ChartRenderError() {
    val colors = LocalAppColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(R.string.editor_chart_render_error_message),
            color = colors.textSecondary,
            modifier = Modifier.testTag("editor_chart_error")
        )
        Text(
            stringResource(R.string.editor_chart_render_error_hint),
            color = colors.textTertiary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ChartEmptyState() {
    val colors = LocalAppColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("editor_chart_empty")
    ) {
        Text(
            stringResource(R.string.editor_chart_empty_message),
            color = colors.textSecondary
        )
        Text(
            stringResource(R.string.editor_chart_empty_hint),
            color = colors.textTertiary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ChartDatumTargets(chartData: ChartData, selectedPointIndex: Int?, onDatumTapped: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        chartData.points.forEachIndexed { index, point ->
            val pointDescription = stringResource(
                R.string.editor_chart_datum_description,
                point.category,
                point.value.toString()
            )
            val semanticsDescription = if (selectedPointIndex == index) {
                stringResource(
                    R.string.editor_chart_datum_selected_content_description,
                    pointDescription,
                    stringResource(R.string.editor_chart_datum_selected_description)
                )
            } else {
                pointDescription
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onDatumTapped(index) }
                    .testTag("editor_chart_datum_target")
                    .semantics {
                        role = Role.Button
                        contentDescription = semanticsDescription
                    }
            )
        }
    }
}

@Composable
private fun ChartTooltip(
    point: ChartPoint,
    selectedColumnLabel: String,
    alignmentModifier: Modifier,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val tooltipText = stringResource(
        R.string.editor_chart_tooltip,
        point.category,
        selectedColumnLabel,
        point.value.toString()
    )
    Text(
        text = tooltipText,
        modifier = alignmentModifier
            .background(colors.surface, RoundedCornerShape(8.dp))
            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
            .padding(start = 10.dp, top = 6.dp, bottom = 6.dp)
            .testTag("editor_chart_tooltip")
            .semantics { contentDescription = tooltipText },
        color = colors.textPrimary,
        style = MaterialTheme.typography.bodySmall
    )
    IconButton(
        onClick = onDismiss,
        modifier = alignmentModifier
            .padding(start = 128.dp)
            .size(40.dp)
            .testTag("editor_chart_tooltip_dismiss")
    ) {
        Icon(
            Icons.Outlined.Close,
            contentDescription = stringResource(R.string.editor_chart_tooltip_dismiss_description),
            tint = colors.textSecondary
        )
    }
}

@Composable
private fun ChartSheetHost(
    openSheet: ChartSheet,
    block: EditorBlock.ChartBlock,
    isEditable: Boolean,
    isDataView: Boolean,
    onSelectView: (Boolean) -> Unit,
    onOpenDataColumn: () -> Unit,
    onAddRow: () -> Unit,
    onAddColumn: () -> Unit,
    onSelectColumn: (String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    when (openSheet) {
        ChartSheet.VIEW -> ChartViewSheet(
            isDataView = isDataView,
            onSelect = onSelectView,
            onDismiss = onDismiss
        )

        ChartSheet.OPTIONS -> ChartOptionsSheet(
            isEditable = isEditable,
            onOpenDataColumn = onOpenDataColumn,
            onAddRow = onAddRow,
            onAddColumn = onAddColumn,
            onDismiss = onDismiss
        )

        ChartSheet.DATA_COLUMN -> ChartDataColumnSheet(
            block = block,
            isEditable = isEditable,
            onSelectColumn = onSelectColumn,
            onBack = onBack,
            onDismiss = onDismiss
        )

        ChartSheet.NONE -> Unit
    }
}

@Composable
private fun ChartDataTable(
    block: EditorBlock.ChartBlock,
    selectedColumnLabel: String,
    isEditable: Boolean,
    onUpdateCell: (rowIndex: Int, columnIndex: Int, value: String) -> Unit,
    onTableAction: (ChartTableAction) -> Unit,
    modifier: Modifier
) {
    val colors = LocalAppColors.current
    var focusedCell by remember(block.id) { mutableStateOf<ChartCellTarget?>(null) }
    var gridHasFocus by remember(block.id) { mutableStateOf(false) }
    var activeSheet by remember(block.id) { mutableStateOf<ChartTableHandleSheet?>(null) }
    val targetCell = focusedCell?.takeIf { target ->
        target.rowIndex in block.rows.indices && target.columnIndex in block.columnIds.indices
    }
    val handlesVisible = isEditable && targetCell != null && (gridHasFocus || activeSheet != null)
    val columnHandleDescription = stringResource(R.string.table_column_handle_description)
    val rowHandleDescription = stringResource(R.string.table_row_handle_description)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.editor_chart_data_hint, selectedColumnLabel),
            color = colors.textSecondary,
            fontSize = 13.sp,
            modifier = Modifier.testTag("editor_chart_data_hint")
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .testTag("editor_chart_data_grid")
        ) {
            Column {
                if (handlesVisible) {
                    Row(modifier = Modifier.height(48.dp)) {
                        Spacer(Modifier.width(48.dp))
                        for (columnId in block.columnIds) {
                            if (columnId == block.columnIds[targetCell?.columnIndex ?: -1]) {
                                IconButton(
                                    onClick = { activeSheet = ChartTableHandleSheet.Column },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("editor_chart_column_options")
                                        .semantics {
                                            contentDescription = columnHandleDescription
                                        }
                                ) {
                                    Icon(
                                        Icons.Outlined.MoreVert,
                                        contentDescription = null,
                                        tint = colors.primary
                                    )
                                }
                            } else {
                                Spacer(Modifier.width(128.dp))
                            }
                        }
                    }
                }
                block.rows.forEachIndexed { rowIndex, row ->
                    if (rowIndex > 0) HorizontalDivider(color = colors.divider)
                    Row(modifier = Modifier.heightIn(min = 48.dp)) {
                        if (handlesVisible && rowIndex == targetCell?.rowIndex) {
                            IconButton(
                                onClick = { activeSheet = ChartTableHandleSheet.Row },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("editor_chart_row_options")
                                    .semantics {
                                        contentDescription = rowHandleDescription
                                    }
                            ) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = null,
                                    tint = colors.primary
                                )
                            }
                        } else if (handlesVisible) {
                            Spacer(Modifier.width(48.dp))
                        }
                        for (columnIndex in block.columnIds.indices) {
                            if (columnIndex > 0) {
                                VerticalDivider(color = colors.divider, thickness = 1.dp)
                            }
                            val cellDescription = stringResource(
                                R.string.editor_chart_data_cell_description,
                                rowIndex + 1,
                                columnIndex + 1
                            ).let { description ->
                                if (isEditable) {
                                    description
                                } else {
                                    stringResource(
                                        R.string.editor_chart_read_only_cell_description,
                                        description
                                    )
                                }
                            }
                            val cellTarget = ChartCellTarget(rowIndex, columnIndex)
                            Box(
                                modifier = Modifier
                                    .width(128.dp)
                                    .heightIn(min = 48.dp)
                                    .background(
                                        if (targetCell == cellTarget) {
                                            colors.primary.copy(alpha = 0.08f)
                                        } else {
                                            colors.transparent
                                        }
                                    )
                            ) {
                                BasicTextField(
                                    value = cellText(row.getOrNull(columnIndex)),
                                    readOnly = !isEditable,
                                    onValueChange = { value ->
                                        if (isEditable) onUpdateCell(rowIndex, columnIndex, value)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused && isEditable) {
                                                focusedCell = cellTarget
                                                gridHasFocus = true
                                            }
                                        }
                                        .testTag("editor_chart_data_cell")
                                        .semantics {
                                            contentDescription = cellDescription
                                        }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = colors.textPrimary
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }
        if (block.rows.size <= 1) {
            Text(
                text = stringResource(R.string.editor_chart_data_empty_hint),
                color = colors.textTertiary,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .testTag("editor_chart_data_empty_hint")
            )
        }
    }
    ChartTableHandleSheets(
        activeSheet = activeSheet,
        targetCell = targetCell,
        block = block,
        onDismiss = { activeSheet = null },
        onAction = { action ->
            onTableAction(action)
            activeSheet = null
        }
    )
}

private data class ChartCellTarget(val rowIndex: Int, val columnIndex: Int)

private enum class ChartTableHandleSheet { Row, Column }

@Composable
private fun ChartTableHandleSheets(
    activeSheet: ChartTableHandleSheet?,
    targetCell: ChartCellTarget?,
    block: EditorBlock.ChartBlock,
    onDismiss: () -> Unit,
    onAction: (ChartTableAction) -> Unit
) {
    val cell = targetCell ?: return
    when (activeSheet) {
        ChartTableHandleSheet.Column -> ChartColumnOperationsSheet(
            block = block,
            columnIndex = cell.columnIndex,
            onDismiss = onDismiss,
            onAction = onAction
        )

        ChartTableHandleSheet.Row -> ChartRowOperationsSheet(
            block = block,
            rowIndex = cell.rowIndex,
            onDismiss = onDismiss,
            onAction = onAction
        )

        null -> Unit
    }
}

@Composable
private fun ChartColumnOperationsSheet(
    block: EditorBlock.ChartBlock,
    columnIndex: Int,
    onDismiss: () -> Unit,
    onAction: (ChartTableAction) -> Unit
) {
    val colors = LocalAppColors.current
    val canDelete = columnIndex > 0 && block.columnIds.size > 2
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        modifier = Modifier.testTag("editor_chart_column_options_sheet")
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                stringResource(R.string.table_column_options_title),
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            ChartOptionActionRow(
                label = stringResource(R.string.table_insert_column_left),
                icon = Icons.Outlined.Add,
                testTag = "editor_chart_insert_column_left",
                enabled = true,
                onClick = {
                    onAction(ChartTableAction.InsertColumnLeft(block.id, columnIndex))
                    onDismiss()
                }
            )
            ChartOptionActionRow(
                label = stringResource(R.string.table_insert_column_right),
                icon = Icons.Outlined.Add,
                testTag = "editor_chart_insert_column_right",
                enabled = true,
                onClick = {
                    onAction(ChartTableAction.InsertColumnRight(block.id, columnIndex))
                    onDismiss()
                }
            )
            ChartOptionActionRow(
                label = stringResource(R.string.table_clear_column),
                icon = Icons.Outlined.Close,
                testTag = "editor_chart_clear_column",
                enabled = true,
                onClick = {
                    onAction(ChartTableAction.ClearColumn(block.id, columnIndex))
                    onDismiss()
                }
            )
            ChartOptionActionRow(
                label = stringResource(R.string.table_delete_column),
                icon = Icons.Outlined.Close,
                testTag = "editor_chart_delete_column",
                enabled = canDelete,
                onClick = {
                    onAction(ChartTableAction.DeleteColumn(block.id, columnIndex))
                    onDismiss()
                }
            )
            if (!canDelete) {
                Text(
                    stringResource(
                        if (columnIndex == 0) {
                            R.string.editor_chart_protected_category_column
                        } else {
                            R.string.editor_chart_protected_data_column
                        }
                    ),
                    color = colors.textTertiary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ChartRowOperationsSheet(
    block: EditorBlock.ChartBlock,
    rowIndex: Int,
    onDismiss: () -> Unit,
    onAction: (ChartTableAction) -> Unit
) {
    val colors = LocalAppColors.current
    val canDelete = block.rows.size > 1
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        modifier = Modifier.testTag("editor_chart_row_options_sheet")
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                stringResource(R.string.table_row_options_title),
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            ChartOptionActionRow(
                label = stringResource(R.string.table_insert_row_above),
                icon = Icons.Outlined.Add,
                testTag = "editor_chart_insert_row_above",
                enabled = true,
                onClick = {
                    onAction(ChartTableAction.InsertRowAbove(block.id, rowIndex))
                    onDismiss()
                }
            )
            ChartOptionActionRow(
                label = stringResource(R.string.table_insert_row_below),
                icon = Icons.Outlined.Add,
                testTag = "editor_chart_insert_row_below",
                enabled = true,
                onClick = {
                    onAction(ChartTableAction.InsertRowBelow(block.id, rowIndex))
                    onDismiss()
                }
            )
            ChartOptionActionRow(
                label = stringResource(R.string.table_clear_row),
                icon = Icons.Outlined.Close,
                testTag = "editor_chart_clear_row",
                enabled = true,
                onClick = {
                    onAction(ChartTableAction.ClearRow(block.id, rowIndex))
                    onDismiss()
                }
            )
            ChartOptionActionRow(
                label = stringResource(R.string.table_delete_row),
                icon = Icons.Outlined.Close,
                testTag = "editor_chart_delete_row",
                enabled = canDelete,
                onClick = {
                    onAction(ChartTableAction.DeleteRow(block.id, rowIndex))
                    onDismiss()
                }
            )
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
            Text(
                stringResource(R.string.editor_chart_view_sheet_title),
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge
            )
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
    isEditable: Boolean,
    onOpenDataColumn: () -> Unit,
    onAddRow: () -> Unit,
    onAddColumn: () -> Unit,
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
            Text(
                stringResource(R.string.editor_chart_options_title),
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            ChartOptionActionRow(
                label = stringResource(R.string.editor_chart_options_data_column),
                icon = Icons.Outlined.BarChart,
                testTag = "editor_chart_option_data_column",
                enabled = true,
                onClick = onOpenDataColumn
            )
            ChartOptionActionRow(
                label = stringResource(R.string.editor_chart_options_add_row),
                icon = Icons.Outlined.Add,
                testTag = "editor_chart_add_row",
                enabled = isEditable,
                disabledDescription = stringResource(
                    R.string.editor_chart_read_only_action_description
                ),
                onClick = onAddRow
            )
            ChartOptionActionRow(
                label = stringResource(R.string.editor_chart_options_add_column),
                icon = Icons.Outlined.Add,
                testTag = "editor_chart_add_column",
                enabled = isEditable,
                disabledDescription = stringResource(
                    R.string.editor_chart_read_only_action_description
                ),
                onClick = onAddColumn
            )
        }
    }
}

@Composable
private fun ChartDataColumnSheet(
    block: EditorBlock.ChartBlock,
    isEditable: Boolean,
    onSelectColumn: (String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalAppColors.current
    val normalizedBlock = remember(block) { block.normalized() }
    val options = remember(normalizedBlock) { normalizedBlock.columnOptions() }
    val backDescription = stringResource(R.string.editor_chart_data_column_back_description)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        modifier = Modifier.testTag("editor_chart_data_column_sheet")
    ) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("editor_chart_data_column_back")
                        .semantics {
                            contentDescription = backDescription
                        }
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = backDescription)
                }
                Text(
                    stringResource(R.string.editor_chart_data_column_title),
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                stringResource(R.string.editor_chart_data_column_supporting_text),
                color = colors.textSecondary,
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 8.dp)
                    .testTag("editor_chart_sheet_supporting_text")
            )
            if (options.isEmpty()) {
                Text(
                    stringResource(R.string.editor_chart_no_data_columns),
                    color = colors.textSecondary,
                    modifier = Modifier.testTag("editor_chart_no_data_columns")
                )
            } else {
                for (option in options) {
                    ChartColumnChoice(
                        block = normalizedBlock,
                        option = option,
                        isEditable = isEditable,
                        onClick = { onSelectColumn(option.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartColumnChoice(
    block: EditorBlock.ChartBlock,
    option: ChartColumnOption,
    isEditable: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val label = chartColumnLabel(block, option)
    val optionDescription = if (isEditable) {
        label
    } else {
        stringResource(R.string.editor_chart_read_only_option_description, label)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(enabled = isEditable, role = Role.RadioButton, onClick = onClick)
            .testTag("editor_chart_option_column")
            .semantics {
                role = Role.RadioButton
                contentDescription = optionDescription
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = option.id == block.selectedColumnId,
            onClick = if (isEditable) onClick else null,
            modifier = Modifier.testTag("editor_chart_option_column_selector")
        )
        Text(label, color = colors.textPrimary)
    }
}

@Composable
private fun ChartOptionActionRow(
    label: String,
    icon: ImageVector,
    testTag: String,
    enabled: Boolean,
    disabledDescription: String? = null,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .testTag(testTag)
            .semantics {
                role = Role.Button
                contentDescription = if (enabled) label else disabledDescription ?: label
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) colors.primary else colors.textTertiary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            color = if (enabled) colors.textPrimary else colors.textTertiary,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = if (enabled) colors.textSecondary else colors.textTertiary
        )
    }
}

@Composable
private fun ChartSheetChoice(label: String, selected: Boolean, testTag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .testTag(testTag)
            .semantics {
                role = Role.RadioButton
                contentDescription = label
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

@Composable
private fun chartColumnLabel(block: EditorBlock.ChartBlock, option: ChartColumnOption): String {
    val header = block.rows.firstOrNull()
        ?.getOrNull(option.index)
        .orEmpty()
        .joinToString("") { it.text }
        .trim()
    return header.ifBlank {
        stringResource(R.string.editor_chart_column_fallback, option.fallbackPosition)
    }
}

private fun cellText(cell: List<RichText>?): String = cell.orEmpty().joinToString("") { it.text }

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
