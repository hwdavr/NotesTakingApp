@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.screen

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertEmoticon
import androidx.compose.material.icons.outlined.KeyboardHide
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.notesapp.R
import com.example.notesapp.domain.emoji.EmojiCategory
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import com.example.notesapp.ui.editor.components.EditorNoteActionsSheet
import com.example.notesapp.ui.editor.components.EmojiPickerBottomSheet
import com.example.notesapp.ui.editor.components.TableColumnOptionsSheet
import com.example.notesapp.ui.editor.components.TableOptionsSheet
import com.example.notesapp.ui.editor.components.TableRowOptionsSheet
import com.example.notesapp.ui.editor.components.VoiceNotePlayer
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.splitAtOffsets
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.mapper.toAnnotatedString
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import com.example.notesapp.ui.editor.model.TableHandleAction
import com.example.notesapp.ui.editor.viewmodel.EmojiPickerViewModel
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.NoteSummaryUiState
import com.example.notesapp.ui.editor.viewmodel.clearTable
import com.example.notesapp.ui.editor.viewmodel.clearTableColumn
import com.example.notesapp.ui.editor.viewmodel.clearTableRow
import com.example.notesapp.ui.editor.viewmodel.deleteTable
import com.example.notesapp.ui.editor.viewmodel.deleteTableColumn
import com.example.notesapp.ui.editor.viewmodel.deleteTableRow
import com.example.notesapp.ui.editor.viewmodel.deleteVoiceAudio
import com.example.notesapp.ui.editor.viewmodel.duplicateTable
import com.example.notesapp.ui.editor.viewmodel.insertTableColumnLeft
import com.example.notesapp.ui.editor.viewmodel.insertTableColumnRight
import com.example.notesapp.ui.editor.viewmodel.insertTableRowAbove
import com.example.notesapp.ui.editor.viewmodel.insertTableRowBelow
import com.example.notesapp.ui.editor.viewmodel.toggleTableFitToWidth
import com.example.notesapp.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    parentPadding: PaddingValues,
    noteId: String?,
    folderId: String? = null,
    onBack: () -> Unit,
    onShareNote: (String) -> Unit,
    onMoveNote: (String) -> Unit,
    onExportNote: (String) -> Unit,
    onOpenVoiceRecorder: (String, String?) -> Unit,
    voiceNoteSaved: Boolean = false,
    onVoiceNoteSavedConsumed: () -> Unit = {},
    viewModel: NoteEditorViewModel = hiltViewModel(),
    emojiPickerViewModel: EmojiPickerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val emojiPickerState by emojiPickerViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(noteId, folderId) { viewModel.load(noteId, folderId) }
    LaunchedEffect(voiceNoteSaved) {
        if (voiceNoteSaved) {
            viewModel.load(noteId, folderId)
            onVoiceNoteSavedConsumed()
        }
    }

    // Intercept physical system back gesture/button
    BackHandler(enabled = state.isEditable) {
        viewModel.handleBackPress(onBack)
    }

    NoteEditorScreenContent(
        parentPadding = parentPadding,
        noteId = noteId,
        state = state,
        onBack = { viewModel.handleBackPress(onBack) },
        onShareRequested = { viewModel.shareCurrentNote(onShareNote) },
        onDelete = { viewModel.delete(onDone = onBack) },
        onTitleChange = viewModel::onTitleChange,
        onRename = viewModel::rename,
        onToggleFavorite = viewModel::toggleFavorite,
        onMoveNote = { state.noteId?.let { onMoveNote(it) } },
        onExportNote = { state.noteId?.let { onExportNote(it) } },
        onOpenVoiceRecorder = { _, focusedBlockId ->
            viewModel.save {
                state.noteId?.let { onOpenVoiceRecorder(it, focusedBlockId) }
            }
        },
        onTextBlockChange = viewModel::onTextBlockChange,
        onToggleCheckbox = viewModel::toggleCheckbox,
        onToggleCheckboxChecked = viewModel::toggleCheckboxChecked,
        onToggleMark = viewModel::toggleBlockMark,
        onAddParagraph = viewModel::addParagraphBlock,
        onAddImage = viewModel::addImageBlock,
        onImageChange = viewModel::updateImageBlock,
        onAddTable = viewModel::addTableBlock,
        onTableAction = { action ->
            when (action) {
                is TableHandleAction.InsertColumnLeft ->
                    viewModel.insertTableColumnLeft(action.blockId, action.columnIndex)
                is TableHandleAction.InsertColumnRight ->
                    viewModel.insertTableColumnRight(action.blockId, action.columnIndex)
                is TableHandleAction.DeleteColumn ->
                    viewModel.deleteTableColumn(action.blockId, action.columnIndex)
                is TableHandleAction.ClearColumn ->
                    viewModel.clearTableColumn(action.blockId, action.columnIndex)
                is TableHandleAction.InsertRowAbove ->
                    viewModel.insertTableRowAbove(action.blockId, action.rowIndex)
                is TableHandleAction.InsertRowBelow ->
                    viewModel.insertTableRowBelow(action.blockId, action.rowIndex)
                is TableHandleAction.DeleteRow ->
                    viewModel.deleteTableRow(action.blockId, action.rowIndex)
                is TableHandleAction.ClearRow ->
                    viewModel.clearTableRow(action.blockId, action.rowIndex)
                is TableHandleAction.ClearTable -> viewModel.clearTable(action.blockId)
                is TableHandleAction.DuplicateTable -> viewModel.duplicateTable(action.blockId)
                is TableHandleAction.DeleteTable -> viewModel.deleteTable(action.blockId)
                is TableHandleAction.ToggleTableFitToWidth ->
                    viewModel.toggleTableFitToWidth(action.blockId)
            }
        },
        onEmojiSelected = { emoji ->
            if (viewModel.insertEmoji(emoji)) {
                emojiPickerViewModel.onEmojiSelected(emoji)
            }
        },
        emojiPickerState = emojiPickerState,
        onEmojiQueryChange = emojiPickerViewModel::onQueryChange,
        onEmojiClearQuery = emojiPickerViewModel::onClearQuery,
        onEmojiCategorySelected = emojiPickerViewModel::onCategorySelected,
        onEmojiSkinToneRequested = emojiPickerViewModel::onSkinToneRequested,
        onEmojiSkinToneDismissed = emojiPickerViewModel::onSkinToneDismissed,
        onTableCellChange = viewModel::updateTableCell,
        onFolderSelected = viewModel::onFolderSelected,
        onToggleFormattingToolbar = viewModel::toggleFormattingToolbar,
        onBlockFocused = viewModel::setFocusedBlock,
        onSelectionChange = viewModel::updateSelection,
        onDeleteBlock = viewModel::deleteBlock,
        onDeleteVoiceAudio = { blockId -> viewModel.deleteVoiceAudio(blockId) },
        onConfirmCategorization = { viewModel.confirmCategorization(onBack) },
        onCancelCategorization = { viewModel.cancelCategorization(onBack) },
        onConfirmManualMove = { viewModel.confirmCategorization(onBack, onMoveNote) },
        onCancelManualMove = { viewModel.cancelCategorization(onBack) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongParameterList")
@Composable
fun NoteEditorScreenContent(
    parentPadding: PaddingValues,
    noteId: String?,
    state: NoteEditorUiState,
    onBack: () -> Unit,
    onShareRequested: () -> Unit,
    onDelete: () -> Unit,
    onTitleChange: (String) -> Unit,
    onRename: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveNote: () -> Unit,
    onExportNote: () -> Unit,
    onOpenVoiceRecorder: (String, String?) -> Unit,
    onTextBlockChange: (String, String) -> Unit,
    onToggleCheckbox: (String) -> Unit,
    onToggleCheckboxChecked: (String) -> Unit,
    onToggleMark: (String, String) -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    emojiPickerState: EmojiPickerUiState = EmojiPickerUiState.empty(),
    onEmojiQueryChange: (String) -> Unit,
    onEmojiClearQuery: () -> Unit,
    onEmojiCategorySelected: (EmojiCategory) -> Unit,
    onEmojiSkinToneRequested: (String) -> Unit,
    onEmojiSkinToneDismissed: () -> Unit,
    onImageChange: (blockId: String, url: String?, caption: String?) -> Unit,
    onAddTable: () -> Unit,
    onTableCellChange: (blockId: String, rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onFolderSelected: (String?) -> Unit,
    onToggleFormattingToolbar: () -> Unit,
    onBlockFocused: (String?) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onDeleteBlock: (String) -> Unit,
    onDeleteVoiceAudio: ((String) -> Unit)? = null,
    onTableAction: (TableHandleAction) -> Unit = {},
    onConfirmCategorization: () -> Unit = {},
    onCancelCategorization: () -> Unit = {},
    onConfirmManualMove: () -> Unit = {},
    onCancelManualMove: () -> Unit = {}
) {
    val colors = LocalAppColors.current
    if (!state.isLoaded) {
        NoteEditorLoading(parentPadding = parentPadding)
        return
    }
    var folderMenuExpanded by remember { mutableStateOf(false) }
    var showNoteActionsSheet by remember { mutableStateOf(false) }
    var showEmojiPicker by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = showEmojiPicker) { showEmojiPicker = false }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTextFieldValue by remember { mutableStateOf("") }
    val selectedFolder = state.availableFolders.firstOrNull { it.id == state.folderId }
    val breadcrumbText =
        buildBreadcrumb(
            folders = state.availableFolders,
            selectedFolder = selectedFolder,
            title = state.title.ifBlank { stringResource(R.string.editor_untitled_note) }
        )
    val activeTextBlockId = state.activeTextBlockId()
    val activeBlock = state.document.blocks.find { it.id == activeTextBlockId }
    val isCheckboxActive = activeBlock is EditorBlock.TextBlock && activeBlock.type == "checkbox"
    var focusLastBlockTrigger by remember { mutableIntStateOf(0) }
    var tableFocusResetTrigger by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = Modifier.padding(top = parentPadding.calculateTopPadding()),
        containerColor = colors.surface,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier =
            Modifier.fillMaxSize()
                .background(colors.surface)
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            EditorTopBar(
                onBack = onBack,
                onShare = onShareRequested,
                onMore = { showNoteActionsSheet = true }
            )
            HorizontalDivider(color = colors.border, thickness = 1.dp)
            Column(
                modifier =
                Modifier.weight(1f)
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = folderMenuExpanded,
                    onExpandedChange = {
                        if (state.isEditable) {
                            folderMenuExpanded = !folderMenuExpanded
                        }
                    }
                ) {
                    Surface(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        color = colors.border,
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            if (state.isEditable) {
                                folderMenuExpanded = true
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = breadcrumbText,
                                modifier = Modifier.weight(1f),
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    ExposedDropdownMenu(
                        expanded = folderMenuExpanded,
                        onDismissRequest = { folderMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.editor_no_folder)) },
                            onClick = {
                                onFolderSelected(null)
                                folderMenuExpanded = false
                            }
                        )
                        state.availableFolders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = {
                                    onFolderSelected(folder.id)
                                    folderMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    color = colors.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier =
                        Modifier.fillMaxSize()
                            .testTag("editor_content_scrollable")
                            .verticalScroll(rememberScrollState())
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = state.isEditable
                            ) {
                                focusLastBlockTrigger++
                                tableFocusResetTrigger++
                            }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        NoteSummaryPanel(summaryState = state.summaryState)
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = onTitleChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text =
                                    stringResource(
                                        R.string.editor_title_placeholder
                                    ),
                                    color = colors.textTertiary
                                )
                            },
                            textStyle =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = colors.textPrimary
                            ),
                            colors = editorFieldColors(),
                            singleLine = true,
                            enabled = state.isEditable
                        )
                        DocumentBlockList(
                            blocks = state.document.blocks,
                            isEditable = state.isEditable,
                            onTextBlockChange = onTextBlockChange,
                            onToggleCheckboxChecked = onToggleCheckboxChecked,
                            onImageChange = onImageChange,
                            onTableCellChange = onTableCellChange,
                            onTableAction = onTableAction,
                            onBlockFocused = onBlockFocused,
                            onSelectionChange = onSelectionChange,
                            onDeleteBlock = onDeleteBlock,
                            onDeleteVoiceAudio = onDeleteVoiceAudio,
                            focusedBlockId = state.focusedBlockId,
                            selectionStart = state.selectionStart,
                            selectionEnd = state.selectionEnd,
                            tableFocusResetTrigger = tableFocusResetTrigger,
                            focusLastBlockTrigger = focusLastBlockTrigger
                        )
                    }
                }
            }
            HorizontalDivider(color = colors.border, thickness = 1.dp)
            EditorBottomBar(
                state = state,
                activeTextBlockId = activeTextBlockId,
                isCheckboxActive = isCheckboxActive,
                onToggleCheckbox = onToggleCheckbox,
                onToggleMark = onToggleMark,
                onAddParagraph = onAddParagraph,
                onAddImage = onAddImage,
                onAddTable = onAddTable,
                onOpenEmojiPicker = { showEmojiPicker = true },
                onOpenVoiceRecorder = {
                    onOpenVoiceRecorder(state.noteId.orEmpty(), state.focusedBlockId)
                },
                onToggleFormattingToolbar = onToggleFormattingToolbar
            )
        }
        if (showNoteActionsSheet) {
            val currentNote =
                Note(
                    id = state.noteId.orEmpty(),
                    title = state.title,
                    content = state.document.toJsonString(),
                    folderId = state.folderId,
                    sortKey = "",
                    version = 0,
                    deviceId = "",
                    createdAt = state.createdAt,
                    updatedAt = System.currentTimeMillis(),
                    isFavorite = state.isFavorite,
                    accessRole = if (state.isEditable) NoteAccessRole.FULL_ACCESS else NoteAccessRole.READ_ONLY
                )
            EditorNoteActionsSheet(
                note = currentNote,
                onDismiss = { showNoteActionsSheet = false },
                onAddToFavorites = {
                    onToggleFavorite()
                    showNoteActionsSheet = false
                },
                onMoveTo = {
                    showNoteActionsSheet = false
                    onMoveNote()
                },
                onRename = {
                    showNoteActionsSheet = false
                    renameTextFieldValue = state.title
                    showRenameDialog = true
                },
                onDelete = {
                    showNoteActionsSheet = false
                    onDelete()
                },
                onExport = {
                    showNoteActionsSheet = false
                    onExportNote()
                }
            )
        }
        EmojiPickerOverlay(
            isVisible = showEmojiPicker,
            onDismiss = { showEmojiPicker = false },
            onEmojiSelected = onEmojiSelected,
            uiState = emojiPickerState,
            onQueryChange = onEmojiQueryChange,
            onClearQuery = onEmojiClearQuery,
            onCategorySelected = onEmojiCategorySelected,
            onSkinToneRequested = onEmojiSkinToneRequested,
            onSkinToneDismissed = onEmojiSkinToneDismissed
        )
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text(stringResource(R.string.folders_rename_note_title)) },
                text = {
                    OutlinedTextField(
                        value = renameTextFieldValue,
                        onValueChange = { renameTextFieldValue = it },
                        label = { Text(stringResource(R.string.folders_note_title_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onRename(renameTextFieldValue)
                            showRenameDialog = false
                        }
                    ) { Text(stringResource(R.string.folders_create_action)) }
                },
                dismissButton = {
                    Button(onClick = { showRenameDialog = false }) {
                        Text(stringResource(R.string.folders_cancel_action))
                    }
                }
            )
        }
        if (state.showCategorizationDialog) {
            AlertDialog(
                onDismissRequest = onCancelCategorization,
                modifier = Modifier.testTag("smart_categorization_dialog"),
                title = { Text(stringResource(R.string.smart_categorization_dialog_title)) },
                text = {
                    Text(
                        text = stringResource(R.string.smart_categorization_dialog_text) + "\n\n" +
                            (state.recommendedFolder?.name ?: "")
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmCategorization,
                        modifier = Modifier.testTag("smart_categorization_ok")
                    ) {
                        Text(stringResource(R.string.smart_categorization_ok_button))
                    }
                },
                dismissButton = {
                    Button(
                        onClick = onCancelCategorization,
                        modifier = Modifier.testTag("smart_categorization_cancel")
                    ) {
                        Text(stringResource(R.string.smart_categorization_cancel_button))
                    }
                }
            )
        }
        if (state.showCategorizationNoMatchDialog) {
            SmartCategorizationNoMatchDialog(
                onConfirmManualMove = onConfirmManualMove,
                onCancelManualMove = onCancelManualMove
            )
        }
        if (state.isCategorizing || state.isBackSyncing) {
            val progressTag =
                if (state.isBackSyncing) "editor_back_sync_progress" else "smart_categorization_progress"
            val progressText =
                if (state.isBackSyncing) {
                    stringResource(R.string.editor_syncing_before_back)
                } else {
                    stringResource(R.string.smart_categorization_analyzing)
                }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.surface.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        modifier = Modifier.testTag(progressTag)
                    )
                    Text(
                        text = progressText,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteEditorLoading(parentPadding: PaddingValues) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .padding(top = parentPadding.calculateTopPadding()),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colors.primary,
            modifier = Modifier.testTag("editor_loading_indicator")
        )
    }
}

private fun NoteEditorUiState.activeTextBlockId(): String? =
    focusedBlockId ?: document.blocks.filterIsInstance<EditorBlock.TextBlock>().firstOrNull()?.id

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun EmojiPickerOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    uiState: EmojiPickerUiState,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onCategorySelected: (EmojiCategory) -> Unit,
    onSkinToneRequested: (String) -> Unit,
    onSkinToneDismissed: () -> Unit
) {
    val isImeVisible = WindowInsets.isImeVisible
    if (isVisible) {
        EmojiPickerBottomSheet(
            uiState = uiState,
            isImeVisible = isImeVisible,
            onDismiss = onDismiss,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
            onCategorySelected = onCategorySelected,
            onEmojiSelected = onEmojiSelected,
            onSkinToneRequested = onSkinToneRequested,
            onSkinToneDismissed = onSkinToneDismissed
        )
    }
}

@Composable
private fun SmartCategorizationNoMatchDialog(onConfirmManualMove: () -> Unit, onCancelManualMove: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelManualMove,
        modifier = Modifier.testTag("smart_categorization_no_match_dialog"),
        title = { Text(stringResource(R.string.smart_categorization_no_match_title)) },
        text = { Text(stringResource(R.string.smart_categorization_no_match_text)) },
        confirmButton = {
            Button(
                onClick = onConfirmManualMove,
                modifier = Modifier.testTag("smart_categorization_no_match_yes")
            ) {
                Text(stringResource(R.string.smart_categorization_no_match_yes_button))
            }
        },
        dismissButton = {
            Button(
                onClick = onCancelManualMove,
                modifier = Modifier.testTag("smart_categorization_no_match_no")
            ) {
                Text(stringResource(R.string.smart_categorization_no_match_no_button))
            }
        }
    )
}

@Composable
private fun NoteSummaryPanel(summaryState: NoteSummaryUiState) {
    val panelModel = summaryState.toSummaryPanelModel() ?: return

    val colors = LocalAppColors.current
    val bodyText = panelModel.text ?: stringResource(panelModel.textResId)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_summary_panel"),
        color = colors.highlight,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.editor_summary_title),
                color = colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = bodyText,
                color = colors.textPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

private data class NoteSummaryPanelModel(
    val text: String?,
    @StringRes val textResId: Int
)

private fun NoteSummaryUiState.toSummaryPanelModel(): NoteSummaryPanelModel? = when (this) {
    is NoteSummaryUiState.Content -> NoteSummaryPanelModel(text = text, textResId = R.string.editor_summary_title)
    NoteSummaryUiState.Empty -> NoteSummaryPanelModel(text = null, textResId = R.string.editor_summary_empty)
    NoteSummaryUiState.Error -> NoteSummaryPanelModel(text = null, textResId = R.string.editor_summary_unavailable)
    NoteSummaryUiState.Loading -> NoteSummaryPanelModel(text = null, textResId = R.string.editor_summary_loading)
    NoteSummaryUiState.Idle -> null
}

@Composable
private fun DocumentBlockList(
    blocks: List<EditorBlock>,
    isEditable: Boolean,
    onTextBlockChange: (String, String) -> Unit,
    onToggleCheckboxChecked: (String) -> Unit,
    onImageChange: (blockId: String, url: String?, caption: String?) -> Unit,
    onTableCellChange: (blockId: String, rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onTableAction: (TableHandleAction) -> Unit,
    onBlockFocused: (String?) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onDeleteBlock: (String) -> Unit,
    onDeleteVoiceAudio: ((String) -> Unit)? = null,
    focusedBlockId: String?,
    selectionStart: Int,
    selectionEnd: Int,
    tableFocusResetTrigger: Int = 0,
    focusLastBlockTrigger: Int = 0
) {
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    val lastTextBlockId = remember(blocks) {
        blocks.filterIsInstance<EditorBlock.TextBlock>().lastOrNull()?.id
    }
    LaunchedEffect(focusedBlockId) {
        focusedBlockId?.let { id ->
            val block = blocks.find { it.id == id }
            if (block is EditorBlock.TextBlock) {
                focusRequesters[id]?.requestFocus()
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().testTag("rich_document_blocks"),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        blocks.forEach { block ->
            val focusRequester = focusRequesters.getOrPut(block.id) { FocusRequester() }
            when (block) {
                is EditorBlock.TextBlock ->
                    TextDocumentBlock(
                        block = block,
                        isEditable = isEditable,
                        onChange = { onTextBlockChange(block.id, it) },
                        onToggleCheckboxChecked = { onToggleCheckboxChecked(block.id) },
                        onFocus = { onBlockFocused(block.id) },
                        onSelectionChange = onSelectionChange,
                        onDelete = { onDeleteBlock(block.id) },
                        focusRequester = focusRequester,
                        selectionStart = selectionStart,
                        selectionEnd = selectionEnd,
                        isFocused = block.id == focusedBlockId,
                        focusTrigger = if (block.id == lastTextBlockId) focusLastBlockTrigger else 0
                    )
                is EditorBlock.ImageBlock ->
                    ImageDocumentBlock(
                        block = block,
                        isEditable = isEditable,
                        onUrlChange = { onImageChange(block.id, it, null) },
                        onCaptionChange = { onImageChange(block.id, null, it) },
                        onDelete = { onDeleteBlock(block.id) }
                    )
                is EditorBlock.TableBlock ->
                    TableDocumentBlock(
                        block = block,
                        isEditable = isEditable,
                        onCellChange = { row, cell, value ->
                            onTableCellChange(block.id, row, cell, value)
                        },
                        onAction = onTableAction,
                        clearFocusTrigger = tableFocusResetTrigger
                    )
                is EditorBlock.Voice -> if (block.audioFilePath != null) {
                    VoiceNotePlayer(
                        block = block,
                        isEditable = isEditable,
                        onDeleteAudio = { onDeleteVoiceAudio?.invoke(block.blockId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TextDocumentBlock(
    block: EditorBlock.TextBlock,
    isEditable: Boolean,
    onChange: (String) -> Unit,
    onToggleCheckboxChecked: () -> Unit,
    onFocus: () -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onDelete: () -> Unit,
    focusRequester: FocusRequester,
    selectionStart: Int,
    selectionEnd: Int,
    isFocused: Boolean,
    focusTrigger: Int = 0
) {
    val colors = LocalAppColors.current
    var textFieldValue by
        remember(block.id) {
            mutableStateOf(
                TextFieldValue(
                    text = block.text()
                )
            )
        }

    LaunchedEffect(focusTrigger) {
        if (focusTrigger > 0) {
            val textLength = textFieldValue.text.length
            textFieldValue = textFieldValue.copy(
                selection = TextRange(textLength)
            )
            focusRequester.requestFocus()
        }
    }

    // Keep textFieldValue in sync with external changes (e.g. note load, folder sync, markdown stripping)
    LaunchedEffect(block.id, block.children, block.type, block.checked, selectionStart, selectionEnd, isFocused) {
        val vmText = block.text()
        if (vmText != textFieldValue.text) {
            val currentSelection = textFieldValue.selection
            val newSelection = if (isFocused && selectionStart <= vmText.length && selectionEnd <= vmText.length) {
                TextRange(selectionStart, selectionEnd)
            } else if (currentSelection.start <= vmText.length && currentSelection.end <= vmText.length) {
                currentSelection
            } else {
                TextRange(vmText.length)
            }
            textFieldValue = TextFieldValue(
                text = vmText,
                selection = newSelection
            )
        }
    }

    val visualTransformation = remember(block.children, colors.background, colors.transparent) {
        VisualTransformation { text ->
            val annotated = block.toAnnotatedString(
                codeBackground = colors.background,
                transparentBackground = colors.transparent
            )
            if (annotated.text == text.text) {
                TransformedText(annotated, OffsetMapping.Identity)
            } else {
                TransformedText(text, OffsetMapping.Identity)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (block.type == "checkbox") {
            Icon(
                imageVector = if (block.checked) {
                    Icons.Filled.CheckBox
                } else {
                    Icons.Outlined.CheckBoxOutlineBlank
                },
                contentDescription = stringResource(
                    if (block.checked) {
                        R.string.editor_checkbox_checked_description
                    } else {
                        R.string.editor_checkbox_unchecked_description
                    }
                ),
                tint = if (block.checked) colors.primary else colors.textSecondary,
                modifier = Modifier
                    .clickable(enabled = isEditable) {
                        onToggleCheckboxChecked()
                    }
                    .testTag("editor_checkbox_icon")
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (block.type == "bulleted") {
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "•",
                    color = colors.textSecondary,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        BasicTextField(
            value = textFieldValue,
            readOnly = !isEditable,
            onValueChange = {
                val selectionChanged = textFieldValue.selection != it.selection
                val textChanged = textFieldValue.text != it.text

                textFieldValue = it

                if (textChanged) {
                    onChange(it.text)
                }
                if (selectionChanged) {
                    onSelectionChange(it.selection.start, it.selection.end)
                }
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocus() }
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Backspace && textFieldValue.text.isEmpty()) {
                        onDelete()
                        true
                    } else {
                        false
                    }
                }
                .testTag("editor_text_block"),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = if (block.type == "heading") 22.sp else 14.sp,
                color = colors.textPrimary,
                lineHeight = if (block.type == "heading") 28.sp else 20.sp,
                fontWeight = if (block.type == "heading") {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            ),
            cursorBrush = SolidColor(colors.primary),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = textFieldValue.text,
                    innerTextField = innerTextField,
                    enabled = isEditable,
                    singleLine = false,
                    visualTransformation = visualTransformation,
                    interactionSource = remember { MutableInteractionSource() },
                    placeholder = null,
                    leadingIcon = null,
                    colors = editorFieldColors(),
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = isEditable,
                            isError = false,
                            interactionSource = remember { MutableInteractionSource() },
                            colors = editorFieldColors(),
                            shape = OutlinedTextFieldDefaults.shape
                        )
                    },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp)
                )
            }
        )
    }
}

@Composable
private fun ImageDocumentBlock(
    block: EditorBlock.ImageBlock,
    isEditable: Boolean,
    onUrlChange: (String) -> Unit,
    onCaptionChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("editor_image_block"),
        color = LocalAppColors.current.background,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = LocalAppColors.current.primary)
                    Text(
                        stringResource(R.string.editor_image_label),
                        fontWeight = FontWeight.Bold,
                        color = LocalAppColors.current.textPrimary
                    )
                }
                if (block.url.isNotBlank()) {
                    val imageDescription = block.caption.ifBlank { stringResource(R.string.editor_image_label) }
                    SubcomposeAsyncImage(
                        model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(block.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = imageDescription,
                        modifier =
                        Modifier.fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("editor_image_preview"),
                        contentScale = ContentScale.Crop,
                        loading = { ShimmerEffect() },
                        error = {
                            Box(
                                modifier =
                                Modifier.fillMaxSize()
                                    .background(LocalAppColors.current.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = stringResource(R.string.editor_image_error_description),
                                    tint = LocalAppColors.current.error
                                )
                            }
                        }
                    )
                }
                BasicTextField(
                    value = block.url,
                    readOnly = !isEditable,
                    onValueChange = onUrlChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    cursorBrush = SolidColor(LocalAppColors.current.primary),
                    decorationBox = { innerTextField ->
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = block.url,
                            innerTextField = innerTextField,
                            enabled = isEditable,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = remember { MutableInteractionSource() },
                            placeholder = {
                                Text(
                                    stringResource(R.string.editor_image_url_placeholder),
                                    color = LocalAppColors.current.textTertiary
                                )
                            },
                            colors = editorFieldColors(),
                            container = {
                                OutlinedTextFieldDefaults.ContainerBox(
                                    enabled = isEditable,
                                    isError = false,
                                    interactionSource =
                                    remember { MutableInteractionSource() },
                                    colors = editorFieldColors(),
                                    shape = OutlinedTextFieldDefaults.shape
                                )
                            },
                            contentPadding =
                            PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                )
                BasicTextField(
                    value = block.caption,
                    readOnly = !isEditable,
                    onValueChange = onCaptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    cursorBrush = SolidColor(LocalAppColors.current.primary),
                    decorationBox = { innerTextField ->
                        OutlinedTextFieldDefaults.DecorationBox(
                            value = block.caption,
                            innerTextField = innerTextField,
                            enabled = isEditable,
                            singleLine = true,
                            visualTransformation = VisualTransformation.None,
                            interactionSource = remember { MutableInteractionSource() },
                            placeholder = {
                                Text(
                                    stringResource(R.string.editor_caption_placeholder),
                                    color = LocalAppColors.current.textTertiary
                                )
                            },
                            colors = editorFieldColors(),
                            container = {
                                OutlinedTextFieldDefaults.ContainerBox(
                                    enabled = isEditable,
                                    isError = false,
                                    interactionSource =
                                    remember { MutableInteractionSource() },
                                    colors = editorFieldColors(),
                                    shape = OutlinedTextFieldDefaults.shape
                                )
                            },
                            contentPadding =
                            PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                )
            }
            IconButton(
                onClick = onDelete,
                enabled = isEditable,
                modifier =
                Modifier.align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .testTag("editor_image_block_delete")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.editor_delete_image_description),
                    tint = LocalAppColors.current.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TableDocumentBlock(
    block: EditorBlock.TableBlock,
    isEditable: Boolean,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onAction: (TableHandleAction) -> Unit,
    clearFocusTrigger: Int
) {
    var focusedCell by remember(block.id) { mutableStateOf<FocusedTableCell?>(null) }
    var tableHasFocus by remember(block.id) { mutableStateOf(false) }
    var activeSheet by remember(block.id) { mutableStateOf<TableHandleSheet?>(null) }
    val columnCount = block.rows.maxOfOrNull { it.size } ?: 0
    val targetCell = focusedCell?.takeIf { target ->
        target.rowIndex in block.rows.indices &&
            target.columnIndex in 0 until columnCount
    }
    val handlesVisible = isEditable && targetCell != null &&
        (tableHasFocus || activeSheet != null)
    val focusedColumnIndex = targetCell?.takeIf { handlesVisible }?.columnIndex
    val focusedRowIndex = targetCell?.takeIf { handlesVisible }?.rowIndex

    LaunchedEffect(clearFocusTrigger) {
        if (clearFocusTrigger > 0) {
            focusedCell = null
            tableHasFocus = false
            activeSheet = null
        }
    }
    LaunchedEffect(isEditable) {
        if (!isEditable) {
            focusedCell = null
            tableHasFocus = false
            activeSheet = null
        }
    }

    TableDocumentBlockContent(
        block = block,
        isEditable = isEditable,
        targetCell = targetCell,
        focusedColumnIndex = focusedColumnIndex,
        focusedRowIndex = focusedRowIndex,
        onCellChange = onCellChange,
        onCellFocusChanged = { cell, hasFocus ->
            if (hasFocus && isEditable) {
                focusedCell = cell
                tableHasFocus = true
            } else if (!hasFocus && focusedCell == cell) {
                tableHasFocus = false
                if (activeSheet == null) {
                    focusedCell = null
                }
            }
        },
        onColumnHandleClick = { activeSheet = TableHandleSheet.Column },
        onRowHandleClick = { activeSheet = TableHandleSheet.Row },
        onTableHandleClick = { activeSheet = TableHandleSheet.Table }
    )
    TableHandleSheets(
        activeSheet = activeSheet,
        targetCell = targetCell,
        blockId = block.id,
        onDismiss = { activeSheet = null },
        onAction = onAction
    )
}

@Composable
private fun TableDocumentBlockContent(
    block: EditorBlock.TableBlock,
    isEditable: Boolean,
    targetCell: FocusedTableCell?,
    focusedColumnIndex: Int?,
    focusedRowIndex: Int?,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onCellFocusChanged: (cell: FocusedTableCell, hasFocus: Boolean) -> Unit,
    onColumnHandleClick: () -> Unit,
    onRowHandleClick: () -> Unit,
    onTableHandleClick: () -> Unit
) {
    val handlesVisible = targetCell != null
    Column(
        modifier = Modifier.fillMaxWidth().testTag("editor_table_block"),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            stringResource(R.string.editor_table_label),
            fontWeight = FontWeight.Bold,
            color = LocalAppColors.current.textPrimary,
            fontSize = 13.sp
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            TableGrid(
                block = block,
                isEditable = isEditable,
                targetCell = targetCell,
                focusedRowIndex = focusedRowIndex,
                handlesVisible = handlesVisible,
                onCellChange = onCellChange,
                onCellFocusChanged = onCellFocusChanged,
                onRowHandleClick = onRowHandleClick
            )
            if (focusedColumnIndex != null) {
                TableColumnHandleRow(
                    columnCount = block.rows.maxOfOrNull { it.size } ?: 0,
                    focusedColumnIndex = focusedColumnIndex,
                    onColumnHandleClick = onColumnHandleClick,
                    onTableHandleClick = onTableHandleClick
                )
            }
        }
    }
}

@Composable
private fun TableGrid(
    block: EditorBlock.TableBlock,
    isEditable: Boolean,
    targetCell: FocusedTableCell?,
    focusedRowIndex: Int?,
    handlesVisible: Boolean,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onCellFocusChanged: (cell: FocusedTableCell, hasFocus: Boolean) -> Unit,
    onRowHandleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (handlesVisible) 48.dp else 0.dp)
            .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .testTag("editor_table_grid")
    ) {
        block.rows.forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) {
                HorizontalDivider(color = LocalAppColors.current.border, thickness = 1.dp)
            }
            TableGridRow(
                rowIndex = rowIndex,
                row = row,
                isEditable = isEditable,
                targetCell = targetCell,
                focusedRowIndex = focusedRowIndex,
                onCellChange = onCellChange,
                onCellFocusChanged = onCellFocusChanged,
                onRowHandleClick = onRowHandleClick
            )
        }
    }
}

@Composable
private fun TableGridRow(
    rowIndex: Int,
    row: List<List<RichText>>,
    isEditable: Boolean,
    targetCell: FocusedTableCell?,
    focusedRowIndex: Int?,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onCellFocusChanged: (cell: FocusedTableCell, hasFocus: Boolean) -> Unit,
    onRowHandleClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        if (focusedRowIndex != null) {
            if (rowIndex == focusedRowIndex) {
                TableRowHandle(
                    modifier = Modifier.width(48.dp).fillMaxHeight(),
                    onClick = onRowHandleClick
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp).fillMaxHeight())
            }
        }
        row.forEachIndexed { cellIndex, cell ->
            if (cellIndex > 0) {
                VerticalDivider(color = LocalAppColors.current.border, thickness = 1.dp)
            }
            TableGridCell(
                rowIndex = rowIndex,
                cellIndex = cellIndex,
                cell = cell,
                isEditable = isEditable,
                isFocusedCell = targetCell == FocusedTableCell(rowIndex, cellIndex),
                onCellChange = onCellChange,
                onCellFocusChanged = onCellFocusChanged
            )
        }
    }
}

@Composable
private fun RowScope.TableGridCell(
    rowIndex: Int,
    cellIndex: Int,
    cell: List<RichText>,
    isEditable: Boolean,
    isFocusedCell: Boolean,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onCellFocusChanged: (cell: FocusedTableCell, hasFocus: Boolean) -> Unit
) {
    val focusedCellDescription = stringResource(R.string.table_focused_cell_description)
    BasicTextField(
        value = cell.joinToString("") { it.text },
        readOnly = !isEditable,
        onValueChange = { onCellChange(rowIndex, cellIndex, it) },
        modifier = Modifier
            .weight(1f)
            .background(
                color = if (isFocusedCell) {
                    LocalAppColors.current.primary.copy(alpha = 0.08f)
                } else {
                    LocalAppColors.current.transparent
                }
            )
            .onFocusChanged { focusState ->
                onCellFocusChanged(
                    FocusedTableCell(rowIndex, cellIndex),
                    focusState.isFocused
                )
            }
            .semantics {
                if (isFocusedCell) {
                    contentDescription = focusedCellDescription
                }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("editor_table_cell"),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
        cursorBrush = SolidColor(LocalAppColors.current.primary),
        singleLine = true
    )
}

@Composable
private fun BoxScope.TableColumnHandleRow(
    columnCount: Int,
    focusedColumnIndex: Int,
    onColumnHandleClick: () -> Unit,
    onTableHandleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(start = 48.dp, end = 48.dp)
    ) {
        repeat(columnCount) { columnIndex ->
            if (columnIndex == focusedColumnIndex) {
                TableColumnHandle(
                    modifier = Modifier.weight(1f),
                    onClick = onColumnHandleClick
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
    IconButton(
        onClick = onTableHandleClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(48.dp)
            .background(
                color = LocalAppColors.current.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("table_options_handle")
    ) {
        Icon(
            imageVector = Icons.Outlined.MoreHoriz,
            contentDescription = stringResource(R.string.table_options_handle_description),
            tint = LocalAppColors.current.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun TableHandleSheets(
    activeSheet: TableHandleSheet?,
    targetCell: FocusedTableCell?,
    blockId: String,
    onDismiss: () -> Unit,
    onAction: (TableHandleAction) -> Unit
) {
    targetCell?.let { cell ->
        when (activeSheet) {
            TableHandleSheet.Column ->
                TableColumnOptionsSheet(
                    blockId = blockId,
                    columnIndex = cell.columnIndex,
                    onDismiss = onDismiss,
                    onAction = onAction
                )
            TableHandleSheet.Row ->
                TableRowOptionsSheet(
                    blockId = blockId,
                    rowIndex = cell.rowIndex,
                    onDismiss = onDismiss,
                    onAction = onAction
                )
            TableHandleSheet.Table ->
                TableOptionsSheet(
                    blockId = blockId,
                    onDismiss = onDismiss,
                    onAction = onAction
                )
            null -> Unit
        }
    }
}

private data class FocusedTableCell(
    val rowIndex: Int,
    val columnIndex: Int
)

private enum class TableHandleSheet {
    Column,
    Row,
    Table
}

@Composable
private fun EditorTopBar(onBack: () -> Unit, onShare: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.collection_notes_back),
                tint = LocalAppColors.current.textPrimary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onShare) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.editor_share_description),
                    tint = LocalAppColors.current.textPrimary
                )
            }
            IconButton(onClick = onMore) {
                Icon(
                    Icons.Outlined.MoreHoriz,
                    contentDescription = stringResource(R.string.editor_more_description),
                    tint = LocalAppColors.current.textPrimary
                )
            }
        }
    }
}

@Composable
private fun EditorBottomBar(
    state: NoteEditorUiState,
    activeTextBlockId: String?,
    isCheckboxActive: Boolean,
    onToggleCheckbox: (String) -> Unit,
    onToggleMark: (String, String) -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onAddTable: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onOpenVoiceRecorder: () -> Unit,
    onToggleFormattingToolbar: () -> Unit
) {
    if (!state.isEditable) {
        ReadOnlyEmojiBottomBar()
        return
    }
    if (state.isFormattingToolbarVisible && state.isEditable) {
        FormattingBottomBar(
            state = state,
            activeTextBlockId = activeTextBlockId,
            onToggleMark = onToggleMark,
            onHideToolbar = onToggleFormattingToolbar
        )
    } else {
        DefaultBottomBar(
            activeTextBlockId = activeTextBlockId,
            isCheckboxActive = isCheckboxActive,
            onToggleCheckbox = onToggleCheckbox,
            onToggleFormattingToolbar = onToggleFormattingToolbar,
            onAddParagraph = onAddParagraph,
            onAddImage = onAddImage,
            onAddTable = onAddTable,
            onOpenEmojiPicker = onOpenEmojiPicker,
            onOpenVoiceRecorder = onOpenVoiceRecorder
        )
    }
}

@Composable
private fun DefaultBottomBar(
    activeTextBlockId: String?,
    isCheckboxActive: Boolean,
    onToggleCheckbox: (String) -> Unit,
    onToggleFormattingToolbar: () -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onAddTable: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onOpenVoiceRecorder: () -> Unit
) {
    val colors = LocalAppColors.current
    LazyRow(
        modifier =
        Modifier.fillMaxWidth()
            .height(56.dp)
            .background(colors.surface)
            .padding(horizontal = 4.dp)
            .testTag("editor_default_bottom_bar"),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            EditorBarButton(
                onClick = onAddParagraph,
                modifier = Modifier.testTag("editor_add_paragraph")
            ) {
                Icon(
                    Icons.Outlined.AddCircle,
                    contentDescription = stringResource(R.string.editor_add_paragraph_description),
                    tint = colors.primary
                )
            }
        }
        item {
            Box(
                modifier =
                Modifier.size(36.dp)
                    .background(colors.border, RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleFormattingToolbar)
                    .testTag("editor_toggle_formatting"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.editor_format_text_style),
                    color = colors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EditorBarButton(
                onClick = { activeTextBlockId?.let { onToggleCheckbox(it) } },
                modifier = Modifier.testTag("editor_checkbox_action")
            ) {
                Icon(
                    Icons.Outlined.CheckBox,
                    contentDescription = stringResource(R.string.editor_checkbox_action_description),
                    tint = if (isCheckboxActive) colors.primary else colors.textPrimary
                )
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = stringResource(R.string.editor_link_description),
                    tint = colors.textPrimary
                )
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Text(
                    stringResource(R.string.editor_mention_action),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EmojiInsertionControl(onOpenEmojiPicker = onOpenEmojiPicker, isEditable = true)
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = stringResource(R.string.editor_undo_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = stringResource(R.string.editor_redo_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.editor_camera_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = onAddImage, modifier = Modifier.testTag("editor_add_image")) {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = stringResource(R.string.editor_image_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = onOpenVoiceRecorder, modifier = Modifier.testTag("editor_mic_btn")) {
                Icon(
                    Icons.Outlined.Mic,
                    contentDescription = stringResource(R.string.editor_mic_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = onAddTable, modifier = Modifier.testTag("editor_add_table")) {
                Icon(
                    Icons.Outlined.TableChart,
                    contentDescription = stringResource(R.string.editor_table_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.editor_close_description),
                    tint = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ReadOnlyEmojiBottomBar() {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.surface)
            .padding(horizontal = 4.dp)
            .testTag("editor_read_only_bottom_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmojiInsertionControl(onOpenEmojiPicker = null, isEditable = false)
    }
}

@Composable
private fun EmojiInsertionControl(onOpenEmojiPicker: (() -> Unit)?, isEditable: Boolean) {
    val colors = LocalAppColors.current
    EditorBarButton(
        onClick = { onOpenEmojiPicker?.invoke() },
        enabled = isEditable && onOpenEmojiPicker != null,
        width = 48.dp,
        modifier = Modifier.testTag("editor_insert_emoji")
    ) {
        Icon(
            Icons.Outlined.InsertEmoticon,
            contentDescription = stringResource(
                if (isEditable) R.string.editor_emoticon_description else R.string.emoji_picker_read_only_description
            ),
            tint = if (isEditable) colors.textPrimary else colors.textSecondary.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun FormattingBottomBar(
    state: NoteEditorUiState,
    activeTextBlockId: String?,
    onToggleMark: (String, String) -> Unit,
    onHideToolbar: () -> Unit
) {
    val colors = LocalAppColors.current
    val isBoldActive = isMarkActive(state, "bold")
    val isItalicActive = isMarkActive(state, "italic")
    val isUnderlineActive = isMarkActive(state, "underline")
    val isStrikethroughActive = isMarkActive(state, "strikethrough")
    LazyRow(
        modifier =
        Modifier.fillMaxWidth()
            .height(56.dp)
            .background(colors.surface)
            .padding(horizontal = 4.dp)
            .testTag("editor_formatting_bottom_bar"),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            EditorBarButton(
                onClick = { /* Body click */ },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    stringResource(R.string.editor_format_body),
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 14.sp
                )
            }
        }
        item {
            EditorBarButton(
                onClick = { activeTextBlockId?.let { onToggleMark(it, "bold") } },
                modifier = Modifier.testTag("editor_bold_action")
            ) {
                Text(
                    stringResource(R.string.editor_bold_action),
                    color = if (isBoldActive) colors.primary else colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        item {
            EditorBarButton(
                onClick = { activeTextBlockId?.let { onToggleMark(it, "italic") } },
                modifier = Modifier.testTag("editor_italic_action")
            ) {
                Text(
                    stringResource(R.string.editor_italic_action),
                    color = if (isItalicActive) colors.primary else colors.textPrimary,
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EditorBarButton(
                onClick = { activeTextBlockId?.let { onToggleMark(it, "underline") } },
                modifier = Modifier.testTag("editor_underline_action")
            ) {
                Text(
                    stringResource(R.string.editor_underline_action),
                    color = if (isUnderlineActive) colors.primary else colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
        item {
            EditorBarButton(
                onClick = { activeTextBlockId?.let { onToggleMark(it, "strikethrough") } },
                modifier = Modifier.testTag("editor_strikethrough_action")
            ) {
                Text(
                    stringResource(R.string.editor_strikethrough_action),
                    color = if (isStrikethroughActive) colors.primary else colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.LineThrough
                )
            }
        }
        item {
            EditorBarButton(onClick = { /* link logic */ }) {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = stringResource(R.string.editor_link_description),
                    tint = colors.textPrimary
                )
            }
        }
        item {
            EditorBarButton(onClick = { /* code logic */ }) {
                Text(
                    stringResource(R.string.editor_code_action),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EditorBarButton(onClick = { /* formula logic */ }) {
                Text(
                    stringResource(R.string.editor_formula_action),
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EditorBarButton(
                onClick = onHideToolbar,
                modifier = Modifier.testTag("editor_hide_formatting")
            ) {
                Icon(
                    Icons.Outlined.KeyboardHide,
                    contentDescription = stringResource(R.string.editor_keyboard_hide_description),
                    tint = colors.textSecondary
                )
            }
        }
    }
}

private fun isMarkActive(state: NoteEditorUiState, mark: String): Boolean {
    val blockId = state.focusedBlockId ?: return false
    val block = state.document.blocks.firstOrNull { it.id == blockId } as? EditorBlock.TextBlock ?: return false
    val text = block.text()
    val start = state.selectionStart
    val end = state.selectionEnd

    if (start == end || start < 0 || end > text.length) {
        return false
    }

    val splitChildren = block.children.splitAtOffsets(listOf(start, end))
    var currentOffset = 0
    return splitChildren.any { child ->
        val childStart = currentOffset
        val childEnd = currentOffset + child.text.length
        currentOffset = childEnd

        childStart >= start && childEnd <= end && mark in child.marks
    }
}

@Composable
private fun EditorBarButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    width: Dp = 40.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.size(width = width, height = 48.dp).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun editorFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = LocalAppColors.current.transparent,
    unfocusedContainerColor = LocalAppColors.current.transparent,
    disabledContainerColor = LocalAppColors.current.transparent,
    focusedBorderColor = LocalAppColors.current.transparent,
    unfocusedBorderColor = LocalAppColors.current.transparent,
    disabledBorderColor = LocalAppColors.current.transparent,
    cursorColor = LocalAppColors.current.primary
)
private fun buildBreadcrumb(folders: List<Folder>, selectedFolder: Folder?, title: String): String {
    if (selectedFolder == null) {
        return "Notes / $title"
    }
    val byId = folders.associateBy { it.id }
    val names = mutableListOf<String>()
    var current: Folder? = selectedFolder
    while (current != null) {
        names += current.name
        current = current.parentFolderId?.let(byId::get)
    }
    return buildString {
        append("Notes")
        append(" / ")
        append(names.asReversed().joinToString(" / "))
        append(" / ")
        append(title)
    }
}

@Composable
private fun ShimmerEffect() {
    val shimmerAnimationName = "shimmer"
    val infiniteTransition = rememberInfiniteTransition(label = shimmerAnimationName)
    val alphaAnimationName = "alpha"
    val alpha by
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.5f,
            animationSpec =
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = alphaAnimationName
        )
    Box(
        modifier =
        Modifier.fillMaxWidth()
            .height(200.dp)
            .background(LocalAppColors.current.border.copy(alpha = alpha))
    )
}
