package com.example.notesapp.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.notesapp.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesapp.NotesApplication
import com.example.notesapp.data.repository.FolderRepository
import com.example.notesapp.data.repository.NoteRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    parentPadding: PaddingValues,
    noteId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current.applicationContext as NotesApplication
    val viewModel: NoteEditorViewModel = viewModel(
        factory = NoteEditorViewModel.Factory(
            NoteRepository(context.database.noteDao()),
            FolderRepository(context.database.folderDao())
        )
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var folderMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        viewModel.load(noteId)
    }

    Scaffold(modifier = Modifier.padding(parentPadding)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (noteId >= 0) "Edit Note" else "New Note",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(onClick = { viewModel.toggleArchived() }) {
                        Icon(Icons.Outlined.Archive, contentDescription = "Archive")
                    }
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Favorite"
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = folderMenuExpanded,
                onExpandedChange = { folderMenuExpanded = !folderMenuExpanded }
            ) {
                val selectedFolderName = state.availableFolders.firstOrNull { it.id == state.folderId }?.name ?: stringResource(R.string.editor_no_folder)
                OutlinedTextField(
                    value = selectedFolderName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.editor_folder_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderMenuExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = folderMenuExpanded,
                    onDismissRequest = { folderMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.editor_no_folder)) },
                        onClick = {
                            viewModel.onFolderSelected(null)
                            folderMenuExpanded = false
                        }
                    )
                    state.availableFolders.forEach { folder ->
                        DropdownMenuItem(
                            text = { Text(folder.name) },
                            onClick = {
                                viewModel.onFolderSelected(folder.id)
                                folderMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.editor_title_label)) },
                singleLine = true
            )

            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text(stringResource(R.string.editor_write_note)) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.save(onDone = onBack) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.editor_save))
                }
                if (noteId >= 0) {
                    Button(
                        onClick = { viewModel.delete(onDone = onBack) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Text(stringResource(R.string.editor_delete))
                    }
                }
            }
        }
    }
}
