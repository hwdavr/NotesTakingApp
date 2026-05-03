package com.example.notesapp.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertEmoticon
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.ui.editor.document.EditorBlock
import com.example.notesapp.ui.editor.document.text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    parentPadding: PaddingValues,
    noteId: String?,
    folderId: String? = null,
    onBack: () -> Unit,
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
        onTextBlockChange = viewModel::onTextBlockChange,
        onToggleMark = viewModel::toggleBlockMark,
        onAddParagraph = viewModel::addParagraphBlock,
        onAddImage = viewModel::addImageBlock,
        onImageChange = viewModel::updateImageBlock,
        onAddTable = viewModel::addTableBlock,
        onTableCellChange = viewModel::updateTableCell,
        onFolderSelected = viewModel::onFolderSelected
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
    onTextBlockChange: (String, String) -> Unit,
    onToggleMark: (String, String) -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onImageChange: (blockId: String, url: String?, caption: String?) -> Unit,
    onAddTable: () -> Unit,
    onTableCellChange: (blockId: String, rowIndex: Int, cellIndex: Int, value: String) -> Unit,
    onFolderSelected: (String?) -> Unit
) {
    var folderMenuExpanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val selectedFolder = state.availableFolders.firstOrNull { it.id == state.folderId }
    val breadcrumbText = buildBreadcrumb(
        folders = state.availableFolders,
        selectedFolder = selectedFolder,
        title = state.title.ifBlank { stringResource(R.string.editor_untitled_note) }
    )
    val activeTextBlockId = state.document.blocks.filterIsInstance<EditorBlock.TextBlock>().firstOrNull()?.id

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
                onMore = { moreMenuExpanded = true }
            )

            HorizontalDivider(color = Color(0xFFD9E2FF), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF4F7FF))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = onTitleChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(stringResource(R.string.editor_title_placeholder))
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
                            onTableCellChange = onTableCellChange
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFD9E2FF), thickness = 1.dp)
            EditorBottomBar(
                activeTextBlockId = activeTextBlockId,
                onToggleMark = onToggleMark,
                onAddParagraph = onAddParagraph,
                onAddImage = onAddImage,
                onAddTable = onAddTable
            )
        }

        DropdownMenu(
            expanded = moreMenuExpanded,
            onDismissRequest = { moreMenuExpanded = false }
        ) {
            if (!noteId.isNullOrBlank()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.editor_delete_action)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Archive,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        moreMenuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun DocumentBlockList(
    blocks: List<EditorBlock>,
    onTextBlockChange: (String, String) -> Unit,
    onImageChange: (blockId: String, url: String?, caption: String?) -> Unit,
    onTableCellChange: (blockId: String, rowIndex: Int, cellIndex: Int, value: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rich_document_blocks"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is EditorBlock.TextBlock -> TextDocumentBlock(
                    block = block,
                    onChange = { onTextBlockChange(block.id, it) }
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
    onChange: (String) -> Unit
) {
    val isBold = block.children.any { "bold" in it.marks }
    val isItalic = block.children.any { "italic" in it.marks }
    OutlinedTextField(
        value = block.text(),
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editor_text_block_${block.id}"),
        placeholder = {
            Text(stringResource(R.string.editor_content_placeholder))
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontSize = if (block.type == "heading") 22.sp else 14.sp,
            color = Color(0xFF1F2A44),
            fontWeight = if (block.type == "heading" || isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            lineHeight = if (block.type == "heading") 28.sp else 20.sp
        ),
        leadingIcon = if (block.type == "bulleted") {
            { Text("•", color = Color(0xFF7281A7), fontSize = 20.sp) }
        } else {
            null
        },
        colors = editorFieldColors(),
        minLines = 1
    )
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
            OutlinedTextField(
                value = block.url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Image URL") },
                singleLine = true,
                colors = editorFieldColors()
            )
            OutlinedTextField(
                value = block.caption,
                onValueChange = onCaptionChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Caption") },
                singleLine = true,
                colors = editorFieldColors()
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Table", fontWeight = FontWeight.Bold, color = Color(0xFF1F2A44), fontSize = 13.sp)
        block.rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                row.forEachIndexed { cellIndex, cell ->
                    OutlinedTextField(
                        value = cell.joinToString("") { it.text },
                        onValueChange = { onCellChange(rowIndex, cellIndex, it) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("editor_table_cell_${block.id}_${rowIndex}_$cellIndex"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        colors = editorFieldColors(),
                        singleLine = true
                    )
                }
            }
        }
    }
}


@Composable
private fun EditorTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    onMore: () -> Unit
) {
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
    onToggleMark: (String, String) -> Unit,
    onAddParagraph: () -> Unit,
    onAddImage: () -> Unit,
    onAddTable: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.White)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAddParagraph, modifier = Modifier.testTag("editor_add_paragraph")) {
            Icon(Icons.Outlined.AddCircle, contentDescription = null, tint = Color(0xFF6E7BFF))
        }
        Text(
            "B",
            modifier = Modifier
                .clickable { activeTextBlockId?.let { onToggleMark(it, "bold") } }
                .testTag("editor_bold_action"),
            color = Color(0xFF6E7BFF),
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "I",
            modifier = Modifier
                .clickable { activeTextBlockId?.let { onToggleMark(it, "italic") } }
                .testTag("editor_italic_action"),
            color = Color(0xFF6E7BFF),
            fontSize = 16.sp,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold
        )
        Icon(Icons.Outlined.CheckBox, contentDescription = null, tint = Color(0xFF1F2A44))
        Icon(Icons.Outlined.Link, contentDescription = null, tint = Color(0xFF1F2A44))
        Text(
            "Table",
            modifier = Modifier
                .clickable(onClick = onAddTable)
                .testTag("editor_add_table"),
            color = Color(0xFF1F2A44),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(Icons.Outlined.InsertEmoticon, contentDescription = null, tint = Color(0xFF7281A7))
        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color(0xFF7281A7))
        IconButton(onClick = onAddImage, modifier = Modifier.testTag("editor_add_image")) {
            Icon(Icons.Outlined.Image, contentDescription = null, tint = Color(0xFF7281A7))
        }
        Icon(Icons.Outlined.AttachFile, contentDescription = null, tint = Color(0xFF7281A7))
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

private fun buildBreadcrumb(
    folders: List<Folder>,
    selectedFolder: Folder?,
    title: String
): String {
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
