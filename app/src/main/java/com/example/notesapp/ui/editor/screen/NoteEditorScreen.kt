@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckBox
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.notesapp.R
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteAccessRole
import com.example.notesapp.ui.editor.components.EditorNoteActionsSheet
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.splitAtOffsets
import com.example.notesapp.ui.editor.mapper.text
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModel
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
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(noteId, folderId) { viewModel.load(noteId, folderId) }
    NoteEditorScreenContent(
        parentPadding = parentPadding,
        noteId = noteId,
        state = state,
        onBack = onBack,
        onShareRequested = { viewModel.shareCurrentNote(onShareNote) },
        onDelete = { viewModel.delete(onDone = onBack) },
        onTitleChange = viewModel::onTitleChange,
        onRename = viewModel::rename,
        onToggleFavorite = viewModel::toggleFavorite,
        onMoveNote = { state.noteId?.let { onMoveNote(it) } },
        onExportNote = { state.noteId?.let { onExportNote(it) } },
        onTextBlockChange = viewModel::onTextBlockChange,
        onToggleMark = viewModel::toggleBlockMark,
        onAddParagraph = viewModel::addParagraphBlock,
        onAddImage = viewModel::addImageBlock,
        onImageChange = viewModel::updateImageBlock,
        onAddTable = viewModel::addTableBlock,
        onTableCellChange = viewModel::updateTableCell,
        onFolderSelected = viewModel::onFolderSelected,
        onToggleFormattingToolbar = viewModel::toggleFormattingToolbar,
        onBlockFocused = viewModel::setFocusedBlock,
        onSelectionChange = viewModel::updateSelection,
        onDeleteBlock = viewModel::deleteBlock
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
    onTextBlockChange: (String, String) -> Unit,
    onToggleMark: (String, String) -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onImageChange: (blockId: String, url: String?, caption: String?) -> Unit,
    onAddTable: () -> Unit,
    onTableCellChange: (blockId: String, rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onFolderSelected: (String?) -> Unit,
    onToggleFormattingToolbar: () -> Unit,
    onBlockFocused: (String?) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onDeleteBlock: (String) -> Unit
) {
    var folderMenuExpanded by remember { mutableStateOf(false) }
    var showNoteActionsSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTextFieldValue by remember { mutableStateOf("") }
    val selectedFolder = state.availableFolders.firstOrNull { it.id == state.folderId }
    val breadcrumbText =
        buildBreadcrumb(
            folders = state.availableFolders,
            selectedFolder = selectedFolder,
            title = state.title.ifBlank { stringResource(R.string.editor_untitled_note) }
        )
    val activeTextBlockId =
        state.focusedBlockId
            ?: state.document
                .blocks
                .filterIsInstance<EditorBlock.TextBlock>()
                .firstOrNull()
                ?.id
    val colors = LocalAppColors.current
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
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
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
                            onImageChange = onImageChange,
                            onTableCellChange = onTableCellChange,
                            onBlockFocused = onBlockFocused,
                            onSelectionChange = onSelectionChange,
                            onDeleteBlock = onDeleteBlock,
                            focusedBlockId = state.focusedBlockId
                        )
                    }
                }
            }
            HorizontalDivider(color = colors.border, thickness = 1.dp)
            EditorBottomBar(
                state = state,
                activeTextBlockId = activeTextBlockId,
                onToggleMark = onToggleMark,
                onAddParagraph = onAddParagraph,
                onAddImage = onAddImage,
                onAddTable = onAddTable,
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
    }
}

@Composable
private fun DocumentBlockList(
    blocks: List<EditorBlock>,
    isEditable: Boolean,
    onTextBlockChange: (String, String) -> Unit,
    onImageChange: (blockId: String, url: String?, caption: String?) -> Unit,
    onTableCellChange: (blockId: String, rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onBlockFocused: (String?) -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onDeleteBlock: (String) -> Unit,
    focusedBlockId: String?
) {
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
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
                        onFocus = { onBlockFocused(block.id) },
                        onSelectionChange = onSelectionChange,
                        onDelete = { onDeleteBlock(block.id) },
                        focusRequester = focusRequester
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
                        }
                    )
            }
        }
    }
}

@Composable
private fun TextDocumentBlock(
    block: EditorBlock.TextBlock,
    isEditable: Boolean,
    onChange: (String) -> Unit,
    onFocus: () -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onDelete: () -> Unit,
    focusRequester: FocusRequester
) {
    val colors = LocalAppColors.current
    var textFieldValue by
        remember(block.id) {
            mutableStateOf(
                TextFieldValue(
                    block.toAnnotatedString(
                        codeBackground = colors.background,
                        transparentBackground = colors.transparent
                    )
                )
            )
        }
    val currentAnnotatedText = block.toAnnotatedString(
        codeBackground = colors.background,
        transparentBackground = colors.transparent
    )
    if (textFieldValue.annotatedString != currentAnnotatedText) {
        textFieldValue = textFieldValue.copy(annotatedString = currentAnnotatedText)
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
        modifier =
        Modifier.fillMaxWidth()
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
        textStyle =
        MaterialTheme.typography.bodyLarge.copy(
            fontSize = if (block.type == "heading") 22.sp else 14.sp,
            color = colors.textPrimary,
            lineHeight = if (block.type == "heading") 28.sp else 20.sp,
            fontWeight =
            if (block.type == "heading") {
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
                visualTransformation = VisualTransformation.None,
                interactionSource = remember { MutableInteractionSource() },
                placeholder = {
                    Text(
                        text = stringResource(R.string.editor_content_placeholder),
                        color = colors.textTertiary
                    )
                },
                leadingIcon =
                if (block.type == "bulleted") {
                    val bulletChar = "•"
                    { Text(bulletChar, color = colors.textSecondary, fontSize = 20.sp) }
                } else {
                    null
                },
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

private fun EditorBlock.TextBlock.toAnnotatedString(
    codeBackground: Color,
    transparentBackground: Color
): AnnotatedString {
    return buildAnnotatedString {
        children.forEach { child ->
            withStyle(
                SpanStyle(
                    fontWeight =
                    if ("bold" in child.marks) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    fontStyle =
                    if ("italic" in child.marks) {
                        FontStyle.Italic
                    } else {
                        FontStyle.Normal
                    },
                    background =
                    if ("code" in child.marks) {
                        codeBackground
                    } else {
                        transparentBackground
                    }
                )
            ) { append(child.text) }
        }
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
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit
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
        Column(
            modifier =
            Modifier.fillMaxWidth()
                .border(1.dp, LocalAppColors.current.border, RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        ) {
            block.rows.forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) {
                    HorizontalDivider(color = LocalAppColors.current.border, thickness = 1.dp)
                }
                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    row.forEachIndexed { cellIndex, cell ->
                        if (cellIndex > 0) {
                            VerticalDivider(color = LocalAppColors.current.border, thickness = 1.dp)
                        }
                        BasicTextField(
                            value = cell.joinToString("") { it.text },
                            readOnly = !isEditable,
                            onValueChange = { onCellChange(rowIndex, cellIndex, it) },
                            modifier =
                            Modifier.weight(1f)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("editor_table_cell"),
                            textStyle =
                            MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            cursorBrush = SolidColor(LocalAppColors.current.primary),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
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
                Icon(Icons.Outlined.Share, contentDescription = null, tint = LocalAppColors.current.textPrimary)
            }
            IconButton(onClick = onMore) {
                Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = LocalAppColors.current.textPrimary)
            }
        }
    }
}

@Composable
private fun EditorBottomBar(
    state: NoteEditorUiState,
    activeTextBlockId: String?,
    onToggleMark: (String, String) -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onAddTable: () -> Unit,
    onToggleFormattingToolbar: () -> Unit
) {
    if (!state.isEditable) return
    if (state.isFormattingToolbarVisible) {
        FormattingBottomBar(
            state = state,
            activeTextBlockId = activeTextBlockId,
            onToggleMark = onToggleMark,
            onHideToolbar = onToggleFormattingToolbar
        )
    } else {
        DefaultBottomBar(
            onToggleFormattingToolbar = onToggleFormattingToolbar,
            onAddParagraph = onAddParagraph,
            onAddImage = onAddImage,
            onAddTable = onAddTable
        )
    }
}

@Composable
private fun DefaultBottomBar(
    onToggleFormattingToolbar: () -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onAddTable: () -> Unit
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
            ) { Icon(Icons.Outlined.AddCircle, contentDescription = null, tint = colors.primary) }
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
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.Outlined.CheckBox,
                    contentDescription = null,
                    tint = colors.textPrimary
                )
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = null,
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
            EditorBarButton(onClick = {}) {
                Icon(Icons.Outlined.InsertEmoticon, contentDescription = null, tint = colors.textSecondary)
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Outlined.Undo,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Outlined.Redo,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = colors.textSecondary)
            }
        }
        item {
            EditorBarButton(onClick = onAddImage, modifier = Modifier.testTag("editor_add_image")) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = colors.textSecondary)
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(Icons.Outlined.Mic, contentDescription = null, tint = colors.textSecondary)
            }
        }
        item {
            EditorBarButton(onClick = onAddTable, modifier = Modifier.testTag("editor_add_table")) {
                Icon(Icons.Outlined.TableChart, contentDescription = null, tint = colors.textSecondary)
            }
        }
        item {
            EditorBarButton(onClick = {}) {
                Icon(
                    Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            }
        }
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
            EditorBarButton(onClick = { /* underline logic */ }) {
                Text(
                    stringResource(R.string.editor_underline_action),
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EditorBarButton(onClick = { /* strikethrough logic */ }) {
                Text(
                    stringResource(R.string.editor_strikethrough_action),
                    color = colors.textSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            EditorBarButton(onClick = { /* link logic */ }) {
                Icon(
                    Icons.Outlined.Link,
                    contentDescription = null,
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
                    contentDescription = null,
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
        return block.children.any { mark in it.marks }
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
private fun EditorBarButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.size(width = 40.dp, height = 48.dp).clickable(onClick = onClick),
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
