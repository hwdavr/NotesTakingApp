package com.example.notesapp.ui.notes

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.common.components.AddFab
import com.example.notesapp.ui.common.components.AppSearchBar
import com.example.notesapp.ui.common.components.SectionTitle
import com.example.notesapp.ui.notes.components.FolderChipsRow
import com.example.notesapp.ui.notes.components.NoteCard
import com.example.notesapp.ui.theme.AccentBlue
import com.example.notesapp.ui.theme.AccentMint
import com.example.notesapp.ui.theme.AccentPink
import com.example.notesapp.ui.theme.AccentYellow

@Composable
fun NotesScreen(
    parentPadding: PaddingValues,
    onAddNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    val cardColors = listOf(AccentYellow, AccentPink, AccentMint, AccentBlue)

    Scaffold(
        modifier = Modifier.padding(parentPadding),
        floatingActionButton = { AddFab(onClick = onAddNote) }
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
                    Text(text = stringResource(R.string.notes_title), style = MaterialTheme.typography.headlineMedium)
                    Text(text = stringResource(R.string.notes_subtitle), style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                AppSearchBar(
                    value = search,
                    onValueChange = {
                        search = it
                        viewModel.onSearchChanged(it)
                    },
                    placeholder = "Search notes"
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(title = "Recent folders", actionLabel = "See all")
                    FolderChipsRow(items = listOf("Personal", "Work", "Ideas"))
                }
            }

            item {
                SectionTitle(title = if (state.notes.isEmpty()) "No notes yet" else "Latest notes")
            }

            if (state.notes.isEmpty() && !state.isLoading) {
                item {
                    Text(
                        text = stringResource(R.string.notes_empty_state),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(state.notes) { note ->
                    Column(modifier = Modifier.clickable { onOpenNote(note.id) }) {
                        NoteCard(
                            title = note.title,
                            preview = note.preview,
                            meta = buildString {
                                append("Note • Updated")
                            },
                            color = cardColors[note.colorIndex]
                        )
                    }
                }
            }
        }
    }
}
