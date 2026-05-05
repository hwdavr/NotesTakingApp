@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.notesapp.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Archive
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
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
import com.example.notesapp.R
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.editor.components.EditorNoteActionsSheet
import com.example.notesapp.ui.editor.document.EditorBlock
import com.example.notesapp.ui.editor.document.text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    parentPadding: PaddingValues,
    noteId: String?,
    folderId: String? = null,
    onBack: () -> Unit,
    onMoveNote: (String) -> Unit,
    onExportNote: (String) -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(noteId, folderId) {
        viewModel.load(noteId, folderId)
    }

    NoteEditorScreenContent(
        parentPadding = parentPadding,
        noteId = noteId,
        state = state,
        onBack = onBack,
        onSave = { viewModel.save(onDone = onBack) },
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
@Composable
fun NoteEditorScreenContent(
    parentPadding: PaddingValues,
    noteId: String?,
    state: NoteEditorUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
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
    val breadcrumbText = buildBreadcrumb(
        folders = state.availableFolders,
        selectedFolder = selectedFolder,
        title = state.title.ifBlank { stringResource(R.string.editor_untitled_note) }
    )
    val activeTextBlockId = state.focusedBlockId
        ?: state.document.blocks.filterIsInstance<EditorBlock.TextBlock>().firstOrNull()?.id

    Scaffold(
        modifier = Modifier.padding(top = parentPadding.calculateTopPadding()),
        containerColor = Color.White,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            EditorTopBar(
                onBack = onBack,
                onSave = onSave,
                onMore = { showNoteActionsSheet = true }
            )

            HorizontalDivider(color = Color(0xFFD9E2FF), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF4F7FF))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = folderMenuExpanded,
                    onExpandedChange = { folderMenuExpanded = !folderMenuExpanded }
                ) {
                    Surface(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        color = Color(0xFFEAF1FF),
                        shape = RoundedCornerShape(8.dp),
                        onClick = { folderMenuExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = Color(0xFF7281A7),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = breadcrumbText,
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF7281A7),
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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
                                    text = stringResource(R.string.editor_title_placeholder),
                                    color = Color(0xFFAAB8C2)
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                color = Color(0xFF1F2A44)
                            ),
                            colors = editorFieldColors(),
                            singleLine = true
                        )

                        DocumentBlockList(
                            blocks = state.document.blocks,
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

            HorizontalDivider(color = Color(0xFFD9E2FF), thickness = 1.dp)
            EditorBottomBar(
                activeTextBlockId = activeTextBlockId,
                isFormattingToolbarVisible = state.isFormattingToolbarVisible,
                onToggleMark = onToggleMark,
                onAddParagraph = onAddParagraph,
                onAddImage = onAddImage,
                onAddTable = onAddTable,
                onToggleFormattingToolbar = onToggleFormattingToolbar
            )
        }

        if (showNoteActionsSheet) {
            val currentNote = Note(
                id = state.noteId.orEmpty(),
                title = state.title,
                content = state.document.toJsonString(),
                folderId = state.folderId,
                sortKey = "",
                version = 0,
                deviceId = "",
                createdAt = state.createdAt,
                updatedAt = System.currentTimeMillis(),
                isFavorite = state.isFavorite
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
                    ) {
                        Text(stringResource(R.string.folders_create_action))
                    }
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
            focusRequesters[id]?.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rich_document_blocks"),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        blocks.forEach { block ->
            val focusRequester = focusRequesters.getOrPut(block.id) { FocusRequester() }
            when (block) {
                is EditorBlock.TextBlock -> TextDocumentBlock(
                    block = block,
                    onChange = { onTextBlockChange(block.id, it) },
                    onFocus = { onBlockFocused(block.id) },
                    onSelectionChange = onSelectionChange,
                    onDelete = { onDeleteBlock(block.id) },
                    focusRequester = focusRequester
                )
                is EditorBlock.ImageBlock -> ImageDocumentBlock(
                    block = block,
                    onUrlChange = { onImageChange(block.id, it, null) },
                    onCaptionChange = { onImageChange(block.id, null, it) }
                )
                is EditorBlock.TableBlock -> TableDocumentBlock(
                    block = block,
                    onCellChange = { row, cell, value -> onTableCellChange(block.id, row, cell, value) }
                )
            }
        }
    }
}

@Composable
private fun TextDocumentBlock(
    block: EditorBlock.TextBlock,
    onChange: (String) -> Unit,
    onFocus: () -> Unit,
    onSelectionChange: (Int, Int) -> Unit,
    onDelete: () -> Unit,
    focusRequester: FocusRequester
) {
    var textFieldValue by remember(block.id) {
        mutableStateOf(TextFieldValue(block.toAnnotatedString()))
    }

    val currentAnnotatedText = block.toAnnotatedString()
    if (textFieldValue.annotatedString != currentAnnotatedText) {
        textFieldValue = textFieldValue.copy(annotatedString = currentAnnotatedText)
    }

    BasicTextField(
        value = textFieldValue,
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
            .fillMaxWidth()
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
            .testTag("editor_text_block_${block.id}"),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = if (block.type == "heading") 22.sp else 14.sp,
            color = Color(0xFF1F2A44),
            lineHeight = if (block.type == "heading") 28.sp else 20.sp,
            fontWeight = if (block.type == "heading") FontWeight.Bold else FontWeight.Normal
        ),
        cursorBrush = SolidColor(Color(0xFF6E7BFF)),
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = textFieldValue.text,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = false,
                visualTransformation = VisualTransformation.None,
                interactionSource = remember { MutableInteractionSource() },
                placeholder = {
                    Text(
                        text = stringResource(R.string.editor_content_placeholder),
                        color = Color(0xFFAAB8C2)
                    )
                },
                leadingIcon = if (block.type == "bulleted") {
                    { Text("•", color = Color(0xFF7281A7), fontSize = 20.sp) }
                } else {
                    null
                },
                colors = editorFieldColors(),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled = true,
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

private fun EditorBlock.TextBlock.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        children.forEach { child ->
            withStyle(
                SpanStyle(
                    fontWeight = if ("bold" in child.marks) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if ("italic" in child.marks) FontStyle.Italic else FontStyle.Normal,
                    background = if ("code" in child.marks) Color(0xFFF4F7FF) else Color.Transparent
                )
            ) {
                append(child.text)
            }
        }
    }
}

@Composable
private fun ImageDocumentBlock(
    block: EditorBlock.ImageBlock,
    onUrlChange: (String) -> Unit,
    onCaptionChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_image_block_${block.id}"),
        color = Color(0xFFF4F7FF),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Image, contentDescription = null, tint = Color(0xFF6E7BFF))
                Text("Image", fontWeight = FontWeight.Bold, color = Color(0xFF1F2A44))
            }
            BasicTextField(
                value = block.url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                cursorBrush = SolidColor(Color(0xFF6E7BFF)),
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = block.url,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = remember { MutableInteractionSource() },
                        placeholder = { Text("Image URL", color = Color(0xFFAAB8C2)) },
                        colors = editorFieldColors(),
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled = true,
                                isError = false,
                                interactionSource = remember { MutableInteractionSource() },
                                colors = editorFieldColors(),
                                shape = OutlinedTextFieldDefaults.shape
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            )
            BasicTextField(
                value = block.caption,
                onValueChange = onCaptionChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium,
                cursorBrush = SolidColor(Color(0xFF6E7BFF)),
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = block.caption,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = remember { MutableInteractionSource() },
                        placeholder = { Text("Caption", color = Color(0xFFAAB8C2)) },
                        colors = editorFieldColors(),
                        container = {
                            OutlinedTextFieldDefaults.ContainerBox(
                                enabled = true,
                                isError = false,
                                interactionSource = remember { MutableInteractionSource() },
                                colors = editorFieldColors(),
                                shape = OutlinedTextFieldDefaults.shape
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun TableDocumentBlock(
    block: EditorBlock.TableBlock,
    onCellChange: (rowIndex: Int, cellIndex: Int, value: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_table_block_${block.id}"),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text("Table", fontWeight = FontWeight.Bold, color = Color(0xFF1F2A44), fontSize = 13.sp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFD9E2FF), RoundedCornerShape(4.dp))
                .clip(RoundedCornerShape(4.dp))
        ) {
            block.rows.forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) {
                    HorizontalDivider(color = Color(0xFFD9E2FF), thickness = 1.dp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    row.forEachIndexed { cellIndex, cell ->
                        if (cellIndex > 0) {
                            VerticalDivider(
                                color = Color(0xFFD9E2FF),
                                thickness = 1.dp
                            )
                        }
                        BasicTextField(
                            value = cell.joinToString("") { it.text },
                            onValueChange = { onCellChange(rowIndex, cellIndex, it) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("editor_table_cell_${block.id}_${rowIndex}_$cellIndex"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            cursorBrush = SolidColor(Color(0xFF6E7BFF)),
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorTopBar(onBack: () -> Unit, onSave: () -> Unit, onMore: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.collection_notes_back),
                tint = Color(0xFF1F2A44)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = null, tint = Color(0xFF7281A7))
            }
            IconButton(onClick = {}) {
                Icon(Icons.AutoMirrored.Outlined.Redo, contentDescription = null, tint = Color(0xFF7281A7))
            }
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = Color(0xFF1F2A44))
            }
            Text(
                text = stringResource(R.string.editor_done),
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.Transparent)
                        )
                    )
                    .clickable(onClick = onSave),
                color = Color(0xFF6E7BFF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onMore) {
                Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = Color(0xFF1F2A44))
            }
        }
    }
}

@Composable
private fun EditorBottomBar(
    activeTextBlockId: String?,
    isFormattingToolbarVisible: Boolean,
    onToggleMark: (String, String) -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onAddTable: () -> Unit,
    onToggleFormattingToolbar: () -> Unit
) {
    if (isFormattingToolbarVisible) {
        FormattingBottomBar(
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp)
            .testTag("editor_default_bottom_bar"),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EditorBarButton(onClick = onAddParagraph, modifier = Modifier.testTag("editor_add_paragraph")) {
            Icon(Icons.Outlined.AddCircle, contentDescription = null, tint = Color(0xFF6E7BFF))
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFEAF1FF), RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleFormattingToolbar)
                .testTag("editor_toggle_formatting"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Aa",
                color = Color(0xFF6E7BFF),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        EditorBarButton(onClick = {}) {
            Icon(Icons.Outlined.CheckBox, contentDescription = null, tint = Color(0xFF1F2A44))
        }
        EditorBarButton(onClick = {}) {
            Icon(Icons.Outlined.Link, contentDescription = null, tint = Color(0xFF1F2A44))
        }
        EditorBarButton(onClick = {}) {
            Text("@", color = Color(0xFF1F2A44), fontWeight = FontWeight.Bold)
        }
        EditorBarButton(onClick = {}) {
            Icon(Icons.Outlined.InsertEmoticon, contentDescription = null, tint = Color(0xFF7281A7))
        }
        EditorBarButton(onClick = {}) {
            Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = null, tint = Color(0xFF7281A7))
        }
        EditorBarButton(onClick = {}) {
            Icon(Icons.AutoMirrored.Outlined.Redo, contentDescription = null, tint = Color(0xFF7281A7))
        }
        EditorBarButton(onClick = {}) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color(0xFF7281A7))
        }
        EditorBarButton(onClick = onAddImage, modifier = Modifier.testTag("editor_add_image")) {
            Icon(Icons.Outlined.Image, contentDescription = null, tint = Color(0xFF7281A7))
        }
        EditorBarButton(onClick = {}) {
            Icon(Icons.Outlined.Mic, contentDescription = null, tint = Color(0xFF7281A7))
        }
        EditorBarButton(onClick = onAddTable, modifier = Modifier.testTag("editor_add_table")) {
            Icon(Icons.Outlined.TableChart, contentDescription = null, tint = Color(0xFF7281A7))
        }
        EditorBarButton(onClick = {}) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF7281A7))
        }
    }
}

@Composable
private fun FormattingBottomBar(
    activeTextBlockId: String?,
    onToggleMark: (String, String) -> Unit,
    onHideToolbar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp)
            .testTag("editor_formatting_bottom_bar"),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EditorBarButton(onClick = { /* Body click */ }, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text("Body", fontWeight = FontWeight.Bold, color = Color(0xFF1F2A44), fontSize = 14.sp)
        }

        EditorBarButton(onClick = { activeTextBlockId?.let { onToggleMark(it, "bold") } }) {
            Text(
                "B",
                modifier = Modifier.testTag("editor_bold_action"),
                color = Color(0xFF6E7BFF),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        EditorBarButton(onClick = { activeTextBlockId?.let { onToggleMark(it, "italic") } }) {
            Text(
                "I",
                modifier = Modifier.testTag("editor_italic_action"),
                color = Color(0xFF6E7BFF),
                fontSize = 18.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold
            )
        }
        EditorBarButton(onClick = { /* underline logic */ }) {
            Text(
                "U",
                color = Color(0xFF1F2A44),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        EditorBarButton(onClick = { /* strikethrough logic */ }) {
            Text(
                "S",
                color = Color(0xFF7281A7),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        EditorBarButton(onClick = { /* link logic */ }) {
            Icon(Icons.Outlined.Link, contentDescription = null, tint = Color(0xFF1F2A44))
        }

        EditorBarButton(onClick = { /* code logic */ }) {
            Text("<>", color = Color(0xFF1F2A44), fontWeight = FontWeight.Bold)
        }

        EditorBarButton(onClick = { /* formula logic */ }) {
            Text("fx", color = Color(0xFF7281A7), fontWeight = FontWeight.Bold)
        }

        EditorBarButton(onClick = onHideToolbar, modifier = Modifier.testTag("editor_hide_formatting")) {
            Icon(Icons.Outlined.KeyboardHide, contentDescription = null, tint = Color(0xFF7281A7))
        }
    }
}

@Composable
private fun EditorBarButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .size(width = 40.dp, height = 48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun editorFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    cursorColor = Color(0xFF6E7BFF)
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
