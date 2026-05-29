package com.example.notesapp.ui.notes.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
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
import com.example.notesapp.ui.notes.viewmodel.NotesViewModel
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun NotesScreen(
    parentPadding: PaddingValues,
    onAddNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    val colors = LocalAppColors.current
    val cardColors = listOf(colors.accentYellow, colors.accentPink, colors.accentMint, colors.accentBlue)
    Scaffold(
        modifier = Modifier.padding(parentPadding),
        floatingActionButton = { AddFab(onClick = onAddNote) },
        contentWindowInsets = WindowInsets(0)
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
                    placeholder = stringResource(R.string.notes_search_placeholder)
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(
                        title = stringResource(R.string.notes_recent_folders_title),
                        actionLabel = stringResource(R.string.notes_see_all_action)
                    )
                    FolderChipsRow(
                        items = listOf(
                            stringResource(R.string.notes_folder_personal),
                            stringResource(R.string.notes_folder_work),
                            stringResource(R.string.notes_folder_ideas)
                        )
                    )
                }
            }
            item {
                SectionTitle(
                    title = stringResource(
                        if (state.notes.isEmpty()) {
                            R.string.notes_no_notes_title
                        } else {
                            R.string.notes_latest_notes_title
                        }
                    )
                )
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
                            meta = stringResource(R.string.notes_updated_meta),
                            color = cardColors[note.colorIndex]
                        )
                    }
                }
            }
        }
    }
}
