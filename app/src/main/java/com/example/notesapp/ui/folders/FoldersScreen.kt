package com.example.notesapp.ui.folders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesapp.NotesApplication
import com.example.notesapp.data.repository.FolderRepository
import com.example.notesapp.data.repository.NoteRepository
import com.example.notesapp.ui.common.components.AddFab
import com.example.notesapp.ui.common.components.AppSearchBar
import com.example.notesapp.ui.common.components.SectionTitle
import com.example.notesapp.ui.folders.components.FolderRow

@Composable
fun FoldersScreen(parentPadding: PaddingValues) {
    val context = LocalContext.current.applicationContext as NotesApplication
    val viewModel: FoldersViewModel = viewModel(
        factory = FoldersViewModel.Factory(
            FolderRepository(context.database.folderDao()),
            NoteRepository(context.database.noteDao())
        )
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.padding(parentPadding),
        floatingActionButton = { AddFab(onClick = { }) }
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
                    Text(text = "Folders", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "Organize notes by category and projects.", style = MaterialTheme.typography.bodyMedium)
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

            item { SectionTitle(title = "Smart collections") }
            item { FolderRow(name = "All Notes", count = state.smartCounts.allNotes.toString()) }
            item { FolderRow(name = "Favorites", count = state.smartCounts.favorites.toString()) }
            item { FolderRow(name = "Archive", count = state.smartCounts.archive.toString()) }

            item { SectionTitle(title = if (state.folders.isEmpty()) "No folders yet" else "My folders") }

            if (state.folders.isEmpty()) {
                item {
                    Text(
                        text = "Your folders will appear here once they are created.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(state.folders) { item ->
                    FolderRow(name = item.folder.name, count = item.noteCount.toString())
                }
            }
        }
    }
}
