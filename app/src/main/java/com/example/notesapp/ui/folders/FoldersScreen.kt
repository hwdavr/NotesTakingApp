package com.example.notesapp.ui.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.common.components.SearchHeader

sealed class QuickActionItem {
    data class FolderItem(val folder: Folder) : QuickActionItem()
    data class NoteItem(val note: Note) : QuickActionItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    parentPadding: PaddingValues,
    onAddNote: (String) -> Unit = {},
    onOpenNote: (String) -> Unit = {},
    onOpenCollection: (type: String, label: String, folderId: String?) -> Unit = { _, _, _ -> },
    onMoveFolder: (Folder) -> Unit = {},
    onMoveNote: (Note) -> Unit = {},
    viewModel: FoldersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FoldersScreenContent(
        parentPadding = parentPadding,
        state = state,
        onSearchChanged = viewModel::onSearchChanged,
        onAddFolder = viewModel::addFolder,
        onRenameFolder = viewModel::renameFolder,
        onRenameNote = viewModel::renameNote,
        onDeleteFolder = viewModel::deleteFolder,
        onDeleteNote = viewModel::deleteNote,
        onAddToFavoritesFolder = viewModel::addFolderToFavorites,
        onAddToFavoritesNote = viewModel::addNoteToFavorites,
        onAddNote = onAddNote,
        onOpenNote = onOpenNote,
        onOpenCollection = onOpenCollection,
        onMoveFolder = onMoveFolder,
        onMoveNote = onMoveNote
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreenContent(
    parentPadding: PaddingValues,
    state: FoldersUiState,
    onSearchChanged: (String) -> Unit,
    onAddFolder: (String, String?) -> Unit,
    onRenameFolder: (Folder, String) -> Unit,
    onRenameNote: (Note, String) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onAddToFavoritesFolder: (Folder) -> Unit = {},
    onAddToFavoritesNote: (Note) -> Unit = {},
    onAddNote: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenCollection: (type: String, label: String, folderId: String?) -> Unit,
    onMoveFolder: (Folder) -> Unit = {},
    onMoveNote: (Note) -> Unit = {}
) {
    var search by rememberSaveable { mutableStateOf("") }
    var selectedItemForQuickActions by remember { mutableStateOf<QuickActionItem?>(null) }
    var selectedFolderForQuickAdd by remember { mutableStateOf<Folder?>(null) }
    var selectedFolderForAdd by remember { mutableStateOf<Folder?>(null) }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by rememberSaveable { mutableStateOf("") }
    var itemToRename by remember { mutableStateOf<QuickActionItem?>(null) }
    var renameTextFieldValue by rememberSaveable { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<QuickActionItem?>(null) }

    Scaffold(
        modifier = Modifier.padding(parentPadding),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF3F7FF), Color(0xFFEDF3FF))
                    )
                )
                .padding(innerPadding)
                .testTag("folders_screen")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
                    .padding(horizontal = 16.dp)
            ) {
                SearchHeader(
                    value = search,
                    placeholder = stringResource(R.string.folders_search_placeholder),
                    onValueChange = {
                        search = it
                        onSearchChanged(it)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                CollectionStats(
                    allNotes = state.smartCounts.allNotes,
                    favorites = state.smartCounts.favorites,
                    archive = state.smartCounts.archive,
                    onAllNotesClick = { onOpenCollection("all", "All Notes", null) },
                    onFavoritesClick = { onOpenCollection("favorites", "Favorites", null) },
                    onArchiveClick = { onOpenCollection("archive", "Archive", null) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                NotesTreeHeader(
                    onAddClick = {
                        selectedFolderForAdd = null
                        showAddFolderDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (state.treeItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                if (state.isSearchActive) {
                                    R.string.folders_search_empty_state
                                } else {
                                    R.string.folders_empty_state
                                }
                            ),
                            color = Color(0xFF7D848B),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 104.dp)
                    ) {
                        itemsIndexed(state.treeItems) { index, item ->
                            when (item) {
                                is FolderTreeItem.FolderItem -> FolderTreeRow(
                                    item = item,
                                    highlighted = !state.isSearchActive && item.depth == 0 && index == 0,
                                    onOpenCollection = {
                                        onOpenCollection("folder", item.folder.name, item.folder.id)
                                    },
                                    onQuickActions = {
                                        selectedItemForQuickActions = QuickActionItem.FolderItem(item.folder)
                                    },
                                    onQuickAdd = { selectedFolderForQuickAdd = item.folder }
                                )

                                is FolderTreeItem.NoteItem -> NoteTreeRow(
                                    note = item.note,
                                    depth = item.depth,
                                    onClick = { onOpenNote(item.note.id) },
                                    onQuickActions = {
                                        selectedItemForQuickActions = QuickActionItem.NoteItem(item.note)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HomeStyleFab(
                onClick = {
                    selectedFolderForAdd = null
                    showAddFolderDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 28.dp)
            )
        }
    }

    if (selectedFolderForQuickAdd != null) {
        FolderAddActionsSheet(
            folder = selectedFolderForQuickAdd!!,
            onDismiss = { selectedFolderForQuickAdd = null },
            onAddFolder = {
                selectedFolderForAdd = selectedFolderForQuickAdd
                selectedFolderForQuickAdd = null
                showAddFolderDialog = true
            },
            onAddNote = {
                val folderId = selectedFolderForQuickAdd?.id
                selectedFolderForQuickAdd = null
                if (folderId != null) {
                    onAddNote(folderId)
                }
            }
        )
    }

    if (selectedItemForQuickActions != null) {
        when (val item = selectedItemForQuickActions) {
            is QuickActionItem.FolderItem -> FolderItemActionsSheet(
                folder = item.folder,
                onDismiss = { selectedItemForQuickActions = null },
                onAddToFavorites = {
                    onAddToFavoritesFolder(item.folder)
                    selectedItemForQuickActions = null
                },
                onMoveTo = {
                    selectedItemForQuickActions = null
                    onMoveFolder(item.folder)
                },
                onRename = {
                    itemToRename = selectedItemForQuickActions
                    renameTextFieldValue = item.folder.name
                    selectedItemForQuickActions = null
                },
                onDelete = {
                    itemToDelete = selectedItemForQuickActions
                    selectedItemForQuickActions = null
                }
            )

            is QuickActionItem.NoteItem -> NoteItemActionsSheet(
                note = item.note,
                onDismiss = { selectedItemForQuickActions = null },
                onAddToFavorites = {
                    onAddToFavoritesNote(item.note)
                    selectedItemForQuickActions = null
                },
                onMoveTo = {
                    selectedItemForQuickActions = null
                    onMoveNote(item.note)
                },
                onRename = {
                    itemToRename = selectedItemForQuickActions
                    renameTextFieldValue = item.note.title
                    selectedItemForQuickActions = null
                },
                onDelete = {
                    itemToDelete = selectedItemForQuickActions
                    selectedItemForQuickActions = null
                }
            )

            null -> Unit
        }
    }

    if (showAddFolderDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddFolderDialog = false
                newFolderName = ""
            },
            title = { Text(stringResource(R.string.folders_new_folder_title)) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text(stringResource(R.string.folders_folder_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onAddFolder(newFolderName, selectedFolderForAdd?.id)
                            showAddFolderDialog = false
                            newFolderName = ""
                        }
                    }
                ) {
                    Text(stringResource(R.string.folders_create_action))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddFolderDialog = false
                        newFolderName = ""
                    }
                ) {
                    Text(stringResource(R.string.folders_cancel_action))
                }
            }
        )
    }

    if (itemToRename != null) {
        AlertDialog(
            onDismissRequest = {
                itemToRename = null
                renameTextFieldValue = ""
            },
            title = {
                Text(
                    text = stringResource(
                        if (itemToRename is QuickActionItem.FolderItem) {
                            R.string.folders_rename_folder_title
                        } else {
                            R.string.folders_rename_note_title
                        }
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = renameTextFieldValue,
                    onValueChange = { renameTextFieldValue = it },
                    label = {
                        Text(
                            text = stringResource(
                                if (itemToRename is QuickActionItem.FolderItem) {
                                    R.string.folders_folder_name_label
                                } else {
                                    R.string.folders_note_title_label
                                }
                            )
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rename_text_field")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameTextFieldValue.isNotBlank()) {
                            when (val item = itemToRename) {
                                is QuickActionItem.FolderItem -> onRenameFolder(item.folder, renameTextFieldValue)
                                is QuickActionItem.NoteItem -> onRenameNote(item.note, renameTextFieldValue)
                                null -> Unit
                            }
                            itemToRename = null
                            renameTextFieldValue = ""
                        }
                    },
                    modifier = Modifier.testTag("rename_confirm_button")
                ) {
                    Text(stringResource(R.string.folders_rename_action))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        itemToRename = null
                        renameTextFieldValue = ""
                    }
                ) {
                    Text(stringResource(R.string.folders_cancel_action))
                }
            }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    text = stringResource(
                        if (itemToDelete is QuickActionItem.FolderItem) {
                            R.string.folders_delete_folder_confirm_title
                        } else {
                            R.string.folders_delete_note_confirm_title
                        }
                    )
                )
            },
            text = {
                Text(text = stringResource(R.string.folders_delete_confirm_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (val item = itemToDelete) {
                            is QuickActionItem.FolderItem -> onDeleteFolder(item.folder)
                            is QuickActionItem.NoteItem -> onDeleteNote(item.note)
                            null -> Unit
                        }
                        itemToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(
                        text = stringResource(R.string.folders_delete_action),
                        color = Color(0xFFC44A4A)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.folders_cancel_action))
                }
            }
        )
    }
}

@Composable
private fun CollectionStats(
    allNotes: Int,
    favorites: Int,
    archive: Int,
    onAllNotesClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CollectionStatRow(
            icon = Icons.AutoMirrored.Outlined.StickyNote2,
            label = stringResource(R.string.folders_stat_all_notes),
            count = allNotes,
            onClick = onAllNotesClick
        )
        CollectionStatRow(
            icon = Icons.Outlined.Folder,
            label = stringResource(R.string.folders_stat_favorites),
            count = favorites,
            onClick = onFavoritesClick
        )
        CollectionStatRow(
            icon = Icons.Outlined.CreateNewFolder,
            label = stringResource(R.string.folders_stat_archive),
            count = archive,
            onClick = onArchiveClick
        )
    }
}

@Composable
private fun CollectionStatRow(
    icon: ImageVector,
    label: String,
    count: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFDCE1E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF5F6770),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF3E444A)
                )
            )
        }

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF7D848B)
            )
        )
    }
}

@Composable
private fun NotesTreeHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.folders_tree_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF2F343A)
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.folders_add_folder_action),
                tint = Color(0xFF8B9199)
            )
        }
    }
}

@Composable
private fun FolderTreeRow(
    item: FolderTreeItem.FolderItem,
    highlighted: Boolean,
    onOpenCollection: () -> Unit,
    onQuickActions: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlighted) Color(0xFFF0F4FF) else Color.Transparent)
            .clickable(onClick = onOpenCollection)
            .padding(
                start = if (highlighted) 8.dp else (item.depth * 22).dp,
                top = if (highlighted) 6.dp else 0.dp,
                end = if (highlighted) 8.dp else 0.dp,
                bottom = if (highlighted) 6.dp else 0.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (item.hasChildren) {
            Icon(
                imageVector = if (item.depth == 0) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF666F77),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Spacer(modifier = Modifier.size(20.dp))
        }

        Icon(
            imageVector = folderIconForName(item.folder.name),
            contentDescription = null,
            tint = Color(0xFF666F77),
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = item.folder.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color(0xFF3E444A)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.noteCount.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7D848B)
            )
        )

        IconButton(
            onClick = onQuickActions,
            modifier = Modifier
                .size(28.dp)
                .testTag("folder_more_actions_${item.folder.id}")
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = stringResource(R.string.folders_more_actions),
                tint = if (highlighted) Color(0xFF5F6EFA) else Color(0xFF8B9199)
            )
        }

        IconButton(onClick = onQuickAdd, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.folders_add_folder_action),
                tint = Color(0xFF8B9199)
            )
        }
    }
}

@Composable
private fun NoteTreeRow(
    note: Note,
    depth: Int,
    onClick: () -> Unit,
    onQuickActions: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = (36 + depth * 22).dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            tint = Color(0xFF808890),
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = note.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF3E444A)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onQuickActions,
            modifier = Modifier
                .size(28.dp)
                .testTag("note_more_actions_${note.id}")
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = null,
                tint = Color(0xFF8B9199),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderAddActionsSheet(
    folder: Folder,
    onDismiss: () -> Unit,
    onAddFolder: () -> Unit,
    onAddNote: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = folderIconForName(folder.name),
                    contentDescription = null,
                    tint = Color(0xFF5F6770),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF2F343A)
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFE7EBF0), thickness = 1.dp)

            SheetActionRow(
                icon = Icons.Outlined.Folder,
                label = stringResource(R.string.folders_add_subfolder_action),
                onClick = onAddFolder
            )
            SheetActionRow(
                icon = Icons.Outlined.Description,
                label = stringResource(R.string.folders_add_note_action),
                onClick = onAddNote
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderItemActionsSheet(
    folder: Folder,
    onDismiss: () -> Unit,
    onAddToFavorites: () -> Unit,
    onMoveTo: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = folderIconForName(folder.name),
                    contentDescription = null,
                    tint = Color(0xFF5F6770),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF2F343A)
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFE7EBF0), thickness = 1.dp)

            SheetActionRow(
                icon = if (folder.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                label = stringResource(
                    if (folder.isFavorite) R.string.folders_remove_from_favorites_action
                    else R.string.folders_add_to_favorites_action
                ),
                onClick = onAddToFavorites,
                modifier = Modifier.testTag("add_to_favorites_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Folder,
                label = stringResource(R.string.folders_move_to_action),
                onClick = onMoveTo,
                modifier = Modifier.testTag("move_item_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Edit,
                label = stringResource(R.string.folders_rename_action),
                onClick = onRename,
                modifier = Modifier.testTag("rename_item_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Delete,
                label = stringResource(R.string.folders_delete_action),
                onClick = onDelete,
                iconTint = Color(0xFFC44A4A),
                textColor = Color(0xFFC44A4A),
                modifier = Modifier.testTag("delete_item_action")
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteItemActionsSheet(
    note: Note,
    onDismiss: () -> Unit,
    onAddToFavorites: () -> Unit,
    onMoveTo: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = Color(0xFF5F6770),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF2F343A)
                    )
                )
            }

            HorizontalDivider(color = Color(0xFFE7EBF0), thickness = 1.dp)

            SheetActionRow(
                icon = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                label = stringResource(
                    if (note.isFavorite) R.string.folders_remove_from_favorites_action
                    else R.string.folders_add_to_favorites_action
                ),
                onClick = onAddToFavorites,
                modifier = Modifier.testTag("add_to_favorites_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Folder,
                label = stringResource(R.string.folders_move_to_action),
                onClick = onMoveTo,
                modifier = Modifier.testTag("move_item_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Edit,
                label = stringResource(R.string.folders_rename_action),
                onClick = onRename,
                modifier = Modifier.testTag("rename_item_action")
            )
            SheetActionRow(
                icon = Icons.Outlined.Delete,
                label = stringResource(R.string.folders_delete_action),
                onClick = onDelete,
                iconTint = Color(0xFFC44A4A),
                textColor = Color(0xFFC44A4A)
            )
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color = Color(0xFF4C5560),
    textColor: Color = Color(0xFF2F343A),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        )
    }
}

@Composable
private fun HomeStyleFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF6E6E73),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun folderIconForName(name: String): ImageVector {
    val normalized = name.lowercase()
    return when {
        "work" in normalized -> Icons.Outlined.Folder
        "idea" in normalized -> Icons.Outlined.Lightbulb
        "team" in normalized || "shared" in normalized -> Icons.Outlined.CreateNewFolder
        else -> Icons.Outlined.Folder
    }
}
