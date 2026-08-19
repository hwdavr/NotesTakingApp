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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertEmoticon
import androidx.compose.material.icons.outlined.KeyboardArrowDown
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
import androidx.compose.ui.unit.TextUnit
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
import com.example.notesapp.ui.editor.components.BasicBlocksPanel
import com.example.notesapp.ui.editor.components.CodeBlockCard
import com.example.notesapp.ui.editor.components.EditorNoteActionsSheet
import com.example.notesapp.ui.editor.components.EmojiPickerBottomSheet
import com.example.notesapp.ui.editor.components.MermaidBlockCard
import com.example.notesapp.ui.editor.components.TableColumnOptionsSheet
import com.example.notesapp.ui.editor.components.TableOptionsSheet
import com.example.notesapp.ui.editor.components.TableRowOptionsSheet
import com.example.notesapp.ui.editor.components.VoiceNotePlayer
import com.example.notesapp.ui.editor.mapper.BasicBlockType
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.basicBlockType
import com.example.notesapp.ui.editor.mapper.splitAtOffsets
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.mapper.toAnnotatedString
import com.example.notesapp.ui.editor.model.EmojiPickerUiState
import com.example.notesapp.ui.editor.model.TableFocusTarget
import com.example.notesapp.ui.editor.model.TableHandleAction
import com.example.notesapp.ui.editor.viewmodel.EmojiPickerViewModel
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.NoteSummaryUiState
import com.example.notesapp.ui.editor.viewmodel.addImageBlock
import com.example.notesapp.ui.editor.viewmodel.addTableBlock
import com.example.notesapp.ui.editor.viewmodel.deleteVoiceAudio
import com.example.notesapp.ui.editor.viewmodel.onTableAction
import com.example.notesapp.ui.editor.viewmodel.setFocusedBlock
import com.example.notesapp.ui.editor.viewmodel.updateCodeBlock
import com.example.notesapp.ui.editor.viewmodel.updateMermaidBlock
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
        onToggleToggleExpanded = { blockId -> viewModel.toggleToggleExpanded(blockId) },
        onToggleMark = viewModel::toggleBlockMark,
        onInsertBasicBlock = viewModel::insertBasicBlock,
        onAddImage = viewModel::addImageBlock,
        onImageChange = viewModel::updateImageBlock,
        onAddTable = viewModel::addTableBlock,
        onTableAction = viewModel::onTableAction,
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
        onCancelManualMove = { viewModel.cancelCategorization(onBack) },
        onUpdateMermaidTitle = { blockId, title -> viewModel.updateMermaidBlock(blockId, title = title) },
        onUpdateMermaidCode = { blockId, code -> viewModel.updateMermaidBlock(blockId, code = code) },
        onUpdateCodeBlockCode = { blockId, code -> viewModel.updateCodeBlock(blockId, code = code) },
        onUpdateCodeBlockLanguage = { blockId, language ->
            viewModel.updateCodeBlock(blockId, language = language)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    onToggleToggleExpanded: (String) -> Unit = { blockId ->
        error("NoteEditorScreenContent requires an onToggleToggleExpanded callback for $blockId")
    },
    onToggleMark: (String, String) -> Unit,
    onInsertBasicBlock: (BasicBlockType) -> Boolean = { false },
    onAddParagraph: () -> Unit = {},
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
    onTableAction: (TableHandleAction) -> Unit = { action ->
        error(
            "NoteEditorScreenContent requires an onTableAction callback; " +
                "received ${action::class.simpleName}"
        )
    },
    onConfirmCategorization: () -> Unit = {},
    onCancelCategorization: () -> Unit = {},
    onConfirmManualMove: () -> Unit = {},
    onCancelManualMove: () -> Unit = {},
    onUpdateMermaidTitle: (blockId: String, title: String) -> Unit = { _, _ -> },
    onUpdateMermaidCode: (blockId: String, code: String) -> Unit = { _, _ -> },
    onUpdateCodeBlockCode: (blockId: String, code: String) -> Unit = { _, _ -> },
    onUpdateCodeBlockLanguage: (blockId: String, language: String) -> Unit = { _, _ -> },
    onOpenMermaidFullscreen: (EditorBlock.MermaidBlock) -> Unit = {}
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
    var showBasicBlocksPanel by rememberSaveable { mutableStateOf(false) }
    var isSelectionInFlight by remember { mutableStateOf(false) }
    BackHandler(enabled = showBasicBlocksPanel) { showBasicBlocksPanel = false }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTextFieldValue by remember { mutableStateOf("") }
    var activeFullscreenMermaidBlock by remember { mutableStateOf<EditorBlock.MermaidBlock?>(null) }
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
                onBack = {
                    if (showBasicBlocksPanel) {
                        showBasicBlocksPanel = false
                    } else {
                        onBack()
                    }
                },
                onShare = {
                    if (showBasicBlocksPanel) {
                        showBasicBlocksPanel = false
                    } else {
                        onShareRequested()
                    }
                },
                onMore = {
                    if (showBasicBlocksPanel) {
                        showBasicBlocksPanel = false
                    } else {
                        showNoteActionsSheet = true
                    }
                }
            )
            HorizontalDivider(color = colors.border, thickness = 1.dp)
            Column(
                modifier =
                Modifier.weight(1f)
                    .fillMaxWidth()
                    .background(colors.background)
                    .pointerInput(showBasicBlocksPanel) {
                        if (showBasicBlocksPanel) {
                            awaitEachGesture {
                                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                                down.consume()
                                showBasicBlocksPanel = false
                            }
                        }
                    }
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
                                if (showBasicBlocksPanel) {
                                    showBasicBlocksPanel = false
                                } else {
                                    focusLastBlockTrigger++
                                    tableFocusResetTrigger++
                                }
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
                            onToggleToggleExpanded = onToggleToggleExpanded,
                            onImageChange = onImageChange,
                            onTableCellChange = onTableCellChange,
                            focusedTableCells = state.focusedTableCells,
                            onTableAction = onTableAction,
                            onBlockFocused = onBlockFocused,
                            onSelectionChange = onSelectionChange,
                            onDeleteBlock = onDeleteBlock,
                            onDeleteVoiceAudio = onDeleteVoiceAudio,
                            onUpdateMermaidTitle = onUpdateMermaidTitle,
                            onUpdateMermaidCode = onUpdateMermaidCode,
                            onUpdateCodeBlockCode = onUpdateCodeBlockCode,
                            onUpdateCodeBlockLanguage = onUpdateCodeBlockLanguage,
                            onOpenMermaidFullscreen = { block ->
                                activeFullscreenMermaidBlock = block
                                onOpenMermaidFullscreen(block)
                            },
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
                onAddImage = onAddImage,
                onAddTable = onAddTable,
                onOpenEmojiPicker = { showEmojiPicker = true },
                onOpenVoiceRecorder = {
                    onOpenVoiceRecorder(state.noteId.orEmpty(), state.focusedBlockId)
                },
                onToggleFormattingToolbar = onToggleFormattingToolbar,
                isBasicBlocksPanelOpen = showBasicBlocksPanel,
                onToggleBasicBlocksPanel = {
                    if (state.isEditable) {
                        showBasicBlocksPanel = !showBasicBlocksPanel
                    }
                }
            )
            if (showBasicBlocksPanel && state.isEditable) {
                BasicBlocksPanelSection(
                    onTileSelected = { type ->
                        if (!isSelectionInFlight) {
                            isSelectionInFlight = true
                            val success = onInsertBasicBlock(type)
                            if (!success && type == BasicBlockType.PARAGRAPH) {
                                onAddParagraph()
                            }
                            if (success) {
                                showBasicBlocksPanel = false
                            }
                            isSelectionInFlight = false
                        }
                    }
                )
            }
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
        if (showEmojiPicker) {
            EmojiPickerBottomSheet(
                uiState = emojiPickerState,
                isImeVisible = WindowInsets.isImeVisible,
                onDismiss = { showEmojiPicker = false },
                onQueryChange = onEmojiQueryChange,
                onClearQuery = onEmojiClearQuery,
                onCategorySelected = onEmojiCategorySelected,
                onEmojiSelected = onEmojiSelected,
                onSkinToneRequested = onEmojiSkinToneRequested,
                onSkinToneDismissed = onEmojiSkinToneDismissed
            )
        }
        if (showRenameDialog) {
            NoteEditorRenameDialog(
                value = renameTextFieldValue,
                onValueChange = { renameTextFieldValue = it },
                onConfirm = {
                    onRename(renameTextFieldValue)
                    showRenameDialog = false
                },
                onDismiss = { showRenameDialog = false }
            )
        }
        NoteEditorCategorizationOverlay(
            state = state,
            onConfirmCategorization = onConfirmCategorization,
            onCancelCategorization = onCancelCategorization,
            onConfirmManualMove = onConfirmManualMove,
            onCancelManualMove = onCancelManualMove
        )
        activeFullscreenMermaidBlock?.let { block ->
            FullscreenDiagramViewerDialog(
                block = block,
                onDismiss = { activeFullscreenMermaidBlock = null }
            )
        }
    }
}

@Composable
private fun NoteEditorCategorizationOverlay(
    state: NoteEditorUiState,
    onConfirmCategorization: () -> Unit,
    onCancelCategorization: () -> Unit,
    onConfirmManualMove: () -> Unit,
    onCancelManualMove: () -> Unit
) {
    val colors = LocalAppColors.current
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
    onToggleToggleExpanded: (String) -> Unit,
    onImageChange: (blockId: String, url: String?, caption: String?) -> Unit,
    onTableCellChange: (blockId: String, rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    focusedTableCells: Map<String, TableFocusTarget>,
    onTableAction: (TableHandleAction) -> Unit,
    onBlockFocused: (String?) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onDeleteBlock: (String) -> Unit,
    onDeleteVoiceAudio: ((String) -> Unit)? = null,
    onUpdateMermaidTitle: ((String, String) -> Unit)? = null,
    onUpdateMermaidCode: ((String, String) -> Unit)? = null,
    onUpdateCodeBlockCode: ((String, String) -> Unit)? = null,
    onUpdateCodeBlockLanguage: ((String, String) -> Unit)? = null,
    onOpenMermaidFullscreen: ((EditorBlock.MermaidBlock) -> Unit)? = null,
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
    Column {
        blocks.forEach { block ->
            when (block) {
                is EditorBlock.TextBlock -> {
                    val requester = focusRequesters.getOrPut(block.id) { FocusRequester() }
                    TextDocumentBlock(
                        block = block,
                        isEditable = isEditable,
                        onChange = { text -> onTextBlockChange(block.id, text) },
                        onToggleCheckboxChecked = { onToggleCheckboxChecked(block.id) },
                        onToggleExpanded = { onToggleToggleExpanded(block.id) },
                        onFocus = { onBlockFocused(block.id) },
                        onSelectionChange = onSelectionChange,
                        onDelete = { onDeleteBlock(block.id) },
                        focusRequester = requester,
                        selectionStart = selectionStart,
                        selectionEnd = selectionEnd,
                        isFocused = (block.id == focusedBlockId),
                        focusTrigger = if (block.id == lastTextBlockId) focusLastBlockTrigger else 0
                    )
                }
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
                        focusedCell = focusedTableCells[block.id],
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
                is EditorBlock.MermaidBlock -> {
                    MermaidBlockCard(
                        block = block,
                        isEditable = isEditable,
                        onUpdateTitle = { title -> onUpdateMermaidTitle?.invoke(block.id, title) },
                        onUpdateCode = { code -> onUpdateMermaidCode?.invoke(block.id, code) },
                        onOpenFullscreen = { onOpenMermaidFullscreen?.invoke(block) }
                    )
                }
                is EditorBlock.CodeBlock -> {
                    CodeBlockCard(
                        block = block,
                        isEditable = isEditable,
                        onUpdateCode = { code -> onUpdateCodeBlockCode?.invoke(block.id, code) },
                        onUpdateLanguage = { language ->
                            onUpdateCodeBlockLanguage?.invoke(block.id, language)
                        },
                        onDelete = { onDeleteBlock(block.id) }
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
    onToggleExpanded: () -> Unit,
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
    LaunchedEffect(
        block.id,
        block.children,
        block.type,
        block.checked,
        block.isExpanded,
        selectionStart,
        selectionEnd,
        isFocused
    ) {
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

    val blockType = block.basicBlockType()
    val toggleContentDescription = stringResource(
        if (block.isExpanded) {
            R.string.editor_toggle_collapse_description
        } else {
            R.string.editor_toggle_expand_description
        }
    )
    val toggleStateDescription = stringResource(
        if (block.isExpanded) {
            R.string.editor_toggle_expanded_state
        } else {
            R.string.editor_toggle_collapsed_state
        }
    )
    val presentationModifier = when (blockType) {
        BasicBlockType.CALLOUT ->
            Modifier
                .background(colors.highlight, RoundedCornerShape(8.dp))
                .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                .padding(8.dp)
                .testTag("editor_callout_block")
        BasicBlockType.QUOTE ->
            Modifier
                .height(IntrinsicSize.Min)
                .testTag("editor_quote_block")
        BasicBlockType.TOGGLE_LIST -> Modifier.testTag("editor_toggle_list_block")
        else -> Modifier
    }

    Row(
        modifier = Modifier.fillMaxWidth().then(presentationModifier),
        verticalAlignment = Alignment.Top
    ) {
        BasicBlockRenderer.LeadingControl(
            block = block,
            blockType = blockType,
            isEditable = isEditable,
            onToggleCheckboxChecked = onToggleCheckboxChecked,
            onToggleExpanded = onToggleExpanded,
            toggleContentDescription = toggleContentDescription,
            toggleStateDescription = toggleStateDescription
        )

        if (blockType != BasicBlockType.TOGGLE_LIST || block.isExpanded) {
            BasicBlockRenderer.TextField(
                textFieldValue = textFieldValue,
                isEditable = isEditable,
                onTextFieldValueChange = { textFieldValue = it },
                onTextChange = onChange,
                onSelectionChange = onSelectionChange,
                visualTransformation = visualTransformation,
                focusRequester = focusRequester,
                onFocus = onFocus,
                onDelete = onDelete,
                blockType = blockType,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private object BasicBlockRenderer {
    @Composable
    fun LeadingControl(
        block: EditorBlock.TextBlock,
        blockType: BasicBlockType,
        isEditable: Boolean,
        onToggleCheckboxChecked: () -> Unit,
        onToggleExpanded: () -> Unit,
        toggleContentDescription: String,
        toggleStateDescription: String
    ) {
        val colors = LocalAppColors.current
        when (blockType) {
            BasicBlockType.TODO_LIST -> {
                IconButton(
                    onClick = onToggleCheckboxChecked,
                    enabled = isEditable,
                    modifier = Modifier.testTag("editor_checkbox_icon")
                ) {
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
                        tint = if (block.checked) colors.primary else colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            BasicBlockType.BULLETED_LIST,
            BasicBlockType.NUMBERED_LIST -> {
                val isBulleted = blockType == BasicBlockType.BULLETED_LIST
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            if (isBulleted) {
                                R.string.editor_bulleted_list_marker
                            } else {
                                R.string.editor_numbered_list_marker
                            }
                        ),
                        color = colors.textSecondary,
                        fontSize = if (isBulleted) 20.sp else 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            BasicBlockType.TOGGLE_LIST -> {
                IconButton(
                    onClick = onToggleExpanded,
                    enabled = isEditable,
                    modifier = Modifier
                        .semantics { stateDescription = toggleStateDescription }
                        .testTag("editor_toggle_list_control")
                ) {
                    Icon(
                        imageVector = if (block.isExpanded) {
                            Icons.Outlined.KeyboardArrowDown
                        } else {
                            Icons.AutoMirrored.Outlined.KeyboardArrowRight
                        },
                        contentDescription = toggleContentDescription,
                        tint = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            BasicBlockType.CALLOUT -> {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            BasicBlockType.QUOTE -> {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().width(3.dp),
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            else -> Unit
        }
    }

    @Composable
    fun TextField(
        textFieldValue: TextFieldValue,
        isEditable: Boolean,
        onTextFieldValueChange: (TextFieldValue) -> Unit,
        onTextChange: (String) -> Unit,
        onSelectionChange: (Int, Int) -> Unit,
        visualTransformation: VisualTransformation,
        focusRequester: FocusRequester,
        onFocus: () -> Unit,
        onDelete: () -> Unit,
        blockType: BasicBlockType,
        modifier: Modifier
    ) {
        val colors = LocalAppColors.current
        val typography = editorTypography(blockType)
        BasicTextField(
            value = textFieldValue,
            readOnly = !isEditable,
            onValueChange = { nextValue ->
                val selectionChanged = textFieldValue.selection != nextValue.selection
                val textChanged = textFieldValue.text != nextValue.text

                onTextFieldValueChange(nextValue)

                if (textChanged) {
                    onTextChange(nextValue.text)
                }
                if (selectionChanged) {
                    onSelectionChange(nextValue.selection.start, nextValue.selection.end)
                }
            },
            modifier = modifier
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
                fontSize = typography.fontSize,
                color = colors.textPrimary,
                lineHeight = typography.lineHeight,
                fontWeight = typography.fontWeight
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

    private data class BasicBlockTypography(
        val fontSize: TextUnit,
        val lineHeight: TextUnit,
        val fontWeight: FontWeight
    )

    private fun editorTypography(blockType: BasicBlockType): BasicBlockTypography = when (blockType) {
        BasicBlockType.HEADING_1 -> BasicBlockTypography(26.sp, 32.sp, FontWeight.Bold)
        BasicBlockType.HEADING_2 -> BasicBlockTypography(22.sp, 28.sp, FontWeight.Bold)
        BasicBlockType.HEADING_3 -> BasicBlockTypography(18.sp, 24.sp, FontWeight.Bold)
        BasicBlockType.HEADING_4 -> BasicBlockTypography(16.sp, 22.sp, FontWeight.Bold)
        else -> BasicBlockTypography(14.sp, 20.sp, FontWeight.Normal)
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
    focusedCell: TableFocusTarget?,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onAction: (TableHandleAction) -> Unit,
    clearFocusTrigger: Int
) {
    var tableHasFocus by remember(block.id) { mutableStateOf(focusedCell != null) }
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
            tableHasFocus = false
            activeSheet = null
            onAction(TableHandleAction.ClearFocus(block.id))
        }
    }
    LaunchedEffect(isEditable) {
        if (!isEditable) {
            tableHasFocus = false
            activeSheet = null
            onAction(TableHandleAction.ClearFocus(block.id))
        }
    }
    LaunchedEffect(focusedCell) {
        tableHasFocus = focusedCell != null
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
                tableHasFocus = true
                onAction(TableHandleAction.FocusCell(block.id, cell.rowIndex, cell.columnIndex))
            } else if (!hasFocus && targetCell == cell) {
                tableHasFocus = false
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
    targetCell: TableFocusTarget?,
    focusedColumnIndex: Int?,
    focusedRowIndex: Int?,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onCellFocusChanged: (cell: TableFocusTarget, hasFocus: Boolean) -> Unit,
    onColumnHandleClick: () -> Unit,
    onRowHandleClick: () -> Unit,
    onTableHandleClick: () -> Unit
) {
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
                onCellChange = onCellChange,
                onCellFocusChanged = onCellFocusChanged,
                onRowHandleClick = onRowHandleClick
            )
            if (focusedColumnIndex != null) {
                TableColumnHandleRow(
                    columnWeights = block.tableColumnWeights(),
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
    targetCell: TableFocusTarget?,
    focusedRowIndex: Int?,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onCellFocusChanged: (cell: TableFocusTarget, hasFocus: Boolean) -> Unit,
    onRowHandleClick: () -> Unit
) {
    val columnWeights = block.tableColumnWeights()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                top = 24.dp
            )
            .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(4.dp))
            .testTag("editor_table_grid")
    ) {
        block.rows.forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) {
                HorizontalDivider(color = LocalAppColors.current.border, thickness = 1.dp)
            }
            TableGridRow(
                rowIndex = rowIndex,
                row = row,
                columnWeights = columnWeights,
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
    columnWeights: List<Float>,
    isEditable: Boolean,
    targetCell: TableFocusTarget?,
    focusedRowIndex: Int?,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onCellFocusChanged: (cell: TableFocusTarget, hasFocus: Boolean) -> Unit,
    onRowHandleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .height(IntrinsicSize.Min)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight()
        ) {
            row.forEachIndexed { cellIndex, cell ->
                if (cellIndex > 0) {
                    VerticalDivider(color = LocalAppColors.current.border, thickness = 1.dp)
                }
                TableGridCell(
                    rowIndex = rowIndex,
                    cellIndex = cellIndex,
                    cell = cell,
                    columnWeight = columnWeights.getOrElse(cellIndex) { 1f },
                    isEditable = isEditable,
                    isFocusedCell = targetCell == TableFocusTarget(rowIndex, cellIndex),
                    onCellChange = onCellChange,
                    onCellFocusChanged = onCellFocusChanged
                )
            }
        }
        if (rowIndex == focusedRowIndex) {
            TableRowHandle(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-24).dp)
                    .width(24.dp)
                    .fillMaxHeight(),
                onClick = onRowHandleClick
            )
        }
    }
}

@Composable
private fun RowScope.TableGridCell(
    rowIndex: Int,
    cellIndex: Int,
    cell: List<RichText>,
    columnWeight: Float,
    isEditable: Boolean,
    isFocusedCell: Boolean,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onCellFocusChanged: (cell: TableFocusTarget, hasFocus: Boolean) -> Unit
) {
    val focusedCellDescription = stringResource(R.string.table_focused_cell_description)
    Box(
        modifier = Modifier
            .weight(columnWeight)
            .fillMaxHeight()
            .testTag("editor_table_cell_bounds")
    ) {
        BasicTextField(
            value = cell.joinToString("") { it.text },
            readOnly = !isEditable,
            onValueChange = { onCellChange(rowIndex, cellIndex, it) },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isFocusedCell) {
                        LocalAppColors.current.primary.copy(alpha = 0.08f)
                    } else {
                        LocalAppColors.current.transparent
                    }
                )
                .onFocusChanged { focusState ->
                    onCellFocusChanged(
                        TableFocusTarget(rowIndex, cellIndex),
                        focusState.isFocused
                    )
                }
                .semantics {
                    if (isFocusedCell) {
                        contentDescription = focusedCellDescription
                    }
                }
                .testTag("editor_table_cell")
                .padding(horizontal = 8.dp, vertical = 6.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            cursorBrush = SolidColor(LocalAppColors.current.primary),
            singleLine = true
        )
    }
}

@Composable
private fun BoxScope.TableColumnHandleRow(
    columnWeights: List<Float>,
    focusedColumnIndex: Int,
    onColumnHandleClick: () -> Unit,
    onTableHandleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(start = 24.dp)
    ) {
        columnWeights.forEachIndexed { columnIndex, columnWeight ->
            if (columnIndex == focusedColumnIndex) {
                TableColumnHandle(
                    modifier = Modifier.weight(columnWeight),
                    onClick = onColumnHandleClick
                )
            } else {
                Spacer(modifier = Modifier.weight(columnWeight))
            }
            if (columnIndex < columnWeights.lastIndex) {
                Spacer(modifier = Modifier.width(1.dp))
            }
        }
    }
    IconButton(
        onClick = onTableHandleClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(y = 0.dp)
            .size(48.dp)
            .testTag("table_options_handle")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    color = LocalAppColors.current.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                )
                .testTag("table_options_visual"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = stringResource(R.string.table_options_handle_description),
                tint = LocalAppColors.current.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun TableHandleSheets(
    activeSheet: TableHandleSheet?,
    targetCell: TableFocusTarget?,
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
    onAddImage: () -> Unit,
    onAddTable: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onOpenVoiceRecorder: () -> Unit,
    onToggleFormattingToolbar: () -> Unit,
    isBasicBlocksPanelOpen: Boolean,
    onToggleBasicBlocksPanel: () -> Unit
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
            onAddImage = onAddImage,
            onAddTable = onAddTable,
            onOpenEmojiPicker = onOpenEmojiPicker,
            onOpenVoiceRecorder = onOpenVoiceRecorder,
            isBasicBlocksPanelOpen = isBasicBlocksPanelOpen,
            onToggleBasicBlocksPanel = onToggleBasicBlocksPanel
        )
    }
}

@Composable
private fun BasicBlocksPanelSection(onTileSelected: (BasicBlockType) -> Unit) {
    val colors = LocalAppColors.current
    HorizontalDivider(
        modifier = Modifier.testTag("basic_blocks_panel_divider"),
        color = colors.border,
        thickness = 1.dp
    )
    BasicBlocksPanel(onTileSelected = onTileSelected)
}

@Composable
private fun DefaultBottomBar(
    activeTextBlockId: String?,
    isCheckboxActive: Boolean,
    onToggleCheckbox: (String) -> Unit,
    onToggleFormattingToolbar: () -> Unit,
    onAddImage: () -> Unit,
    onAddTable: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onOpenVoiceRecorder: () -> Unit,
    isBasicBlocksPanelOpen: Boolean,
    onToggleBasicBlocksPanel: () -> Unit
) {
    val colors = LocalAppColors.current
    val handleToolbarClick: (() -> Unit) -> Unit = { action ->
        if (isBasicBlocksPanelOpen) {
            onToggleBasicBlocksPanel()
        } else {
            action()
        }
    }
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
                onClick = onToggleBasicBlocksPanel,
                modifier = Modifier.testTag("editor_basic_blocks_trigger")
            ) {
                Icon(
                    Icons.Outlined.AddCircle,
                    contentDescription = stringResource(
                        if (isBasicBlocksPanelOpen) {
                            R.string.editor_basic_blocks_trigger_hide_description
                        } else {
                            R.string.editor_basic_blocks_trigger_description
                        }
                    ),
                    tint = colors.primary
                )
            }
        }
        item {
            Box(
                modifier =
                Modifier.size(36.dp)
                    .background(colors.border, RoundedCornerShape(8.dp))
                    .clickable(onClick = { handleToolbarClick(onToggleFormattingToolbar) })
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
                onClick = { handleToolbarClick { activeTextBlockId?.let { onToggleCheckbox(it) } } },
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
            EditorBarButton(onClick = { handleToolbarClick {} }) {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = stringResource(R.string.editor_link_description),
                    tint = colors.textPrimary
                )
            }
        }
        item {
            EditorBarButton(onClick = { handleToolbarClick {} }) {
                Text(
                    stringResource(R.string.editor_mention_action),
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EmojiInsertionControl(
                onOpenEmojiPicker = { handleToolbarClick(onOpenEmojiPicker) },
                isEditable = true
            )
        }
        item {
            EditorBarButton(onClick = { handleToolbarClick {} }) {
                Icon(
                    Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = stringResource(R.string.editor_undo_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = { handleToolbarClick {} }) {
                Icon(
                    Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = stringResource(R.string.editor_redo_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = { handleToolbarClick {} }) {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.editor_camera_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(
                onClick = { handleToolbarClick(onAddImage) },
                modifier = Modifier.testTag("editor_add_image")
            ) {
                Icon(
                    Icons.Outlined.Image,
                    contentDescription = stringResource(R.string.editor_image_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(
                onClick = { handleToolbarClick(onOpenVoiceRecorder) },
                modifier = Modifier.testTag("editor_mic_btn")
            ) {
                Icon(
                    Icons.Outlined.Mic,
                    contentDescription = stringResource(R.string.editor_mic_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(
                onClick = { handleToolbarClick(onAddTable) },
                modifier = Modifier.testTag("editor_add_table")
            ) {
                Icon(
                    Icons.Outlined.TableChart,
                    contentDescription = stringResource(R.string.editor_table_description),
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = { handleToolbarClick {} }) {
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
        EditorBarButton(
            onClick = {},
            enabled = false,
            modifier = Modifier.testTag("editor_basic_blocks_trigger")
        ) {
            Icon(
                Icons.Outlined.AddCircle,
                contentDescription = stringResource(R.string.editor_basic_blocks_trigger_disabled_description),
                tint = colors.textSecondary.copy(alpha = 0.38f)
            )
        }
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
