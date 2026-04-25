package com.example.notesapp.ui.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.ui.common.components.AddFab
import com.example.notesapp.ui.common.components.AppSearchBar
import com.example.notesapp.ui.common.components.SectionTitle
import com.example.notesapp.ui.folders.components.FolderRow
import com.example.notesapp.ui.folders.components.NoteRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    parentPadding: PaddingValues,
    onAddNote: (Long) -> Unit = {},
    onOpenNote: (Long) -> Unit = {},
    viewModel: FoldersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    
    var selectedFolderForAdd by remember { mutableStateOf<Folder?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.padding(parentPadding),
        floatingActionButton = { AddFab(onClick = { 
            selectedFolderForAdd = null
            showAddFolderDialog = true 
        }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = stringResource(R.string.folders_title), style = MaterialTheme.typography.headlineMedium)
                    Text(text = stringResource(R.string.folders_subtitle), style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                AppSearchBar(
                    value = search,
                    onValueChange = {
                        search = it
                        viewModel.onSearchChanged(it)
                    },
                    placeholder = "Search folders"
                )
            }

            if (!state.isSearchActive) {
                item { SectionTitle(title = "Smart collections") }
                item { FolderRow(name = "All Notes", count = state.smartCounts.allNotes.toString()) }
                item { FolderRow(name = "Favorites", count = state.smartCounts.favorites.toString()) }
                item { FolderRow(name = "Archive", count = state.smartCounts.archive.toString()) }

                item { SectionTitle(title = if (state.treeItems.isEmpty()) "No folders yet" else "My folders") }
            } else {
                item { SectionTitle(title = "Search results") }
            }

            if (state.treeItems.isEmpty() && !state.isSearchActive) {
                item {
                    Text(
                        text = stringResource(R.string.folders_empty_state),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(state.treeItems) { item ->
                    when (item) {
                        is FolderTreeItem.FolderItem -> {
                            FolderRow(
                                name = item.folder.name, 
                                count = item.noteCount.toString(),
                                depth = item.depth,
                                onAddClick = {
                                    selectedFolderForAdd = item.folder
                                    showSheet = true
                                }
                            )
                        }
                        is FolderTreeItem.NoteItem -> {
                            NoteRow(
                                title = item.note.title,
                                preview = item.note.content,
                                depth = item.depth,
                                onClick = { onOpenNote(item.note.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Add to \"${selectedFolderForAdd?.name}\"",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                ListItem(
                    headlineContent = { Text("Add Folder") },
                    leadingContent = { Icon(Icons.Outlined.CreateNewFolder, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showSheet = false
                        showAddFolderDialog = true
                    }
                )
                ListItem(
                    headlineContent = { Text("Add Note") },
                    leadingContent = { Icon(Icons.Outlined.Description, contentDescription = null) },
                    modifier = Modifier.clickable {
                        selectedFolderForAdd?.let { parent ->
                            onAddNote(parent.id)
                        }
                        showSheet = false
                    }
                )
            }
        }
    }
    
    if (showAddFolderDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddFolderDialog = false
                newFolderName = ""
            },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.addFolder(newFolderName, selectedFolderForAdd?.id)
                            showAddFolderDialog = false
                            newFolderName = ""
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddFolderDialog = false
                    newFolderName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
