package com.example.notesapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.domain.note.Note
import com.example.notesapp.ui.common.components.SearchHeader
import com.example.notesapp.ui.folders.NoteItemActionsSheet
import com.example.notesapp.ui.notes.components.NoteCard
import com.example.notesapp.ui.theme.AccentBlue
import com.example.notesapp.ui.theme.AccentMint
import com.example.notesapp.ui.theme.AccentPink
import com.example.notesapp.ui.theme.AccentYellow

@Composable
fun HomeNotesScreen(
    parentPadding: PaddingValues,
    onAddNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    onMoveNote: (Note) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeNotesScreenContent(
        parentPadding = parentPadding,
        state = state,
        onAddNote = onAddNote,
        onOpenNote = onOpenNote,
        onSelectFolder = viewModel::selectFolder,
        onAddToFavoritesNote = viewModel::addNoteToFavorites,
        onRenameNote = viewModel::renameNote,
        onDeleteNote = viewModel::deleteNote,
        onMoveNote = onMoveNote
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNotesScreenContent(
    parentPadding: PaddingValues,
    state: HomeUiState,
    onAddNote: () -> Unit,
    onOpenNote: (String) -> Unit,
    onSelectFolder: (String) -> Unit,
    onAddToFavoritesNote: (Note) -> Unit = {},
    onRenameNote: (Note, String) -> Unit = { _, _ -> },
    onDeleteNote: (Note) -> Unit = {},
    onMoveNote: (Note) -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedNoteForQuickActions by remember { mutableStateOf<Note?>(null) }
    var noteToRename by remember { mutableStateOf<Note?>(null) }
    var renameTextFieldValue by rememberSaveable { mutableStateOf("") }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    val cardColors = remember { listOf(AccentYellow, AccentPink, AccentMint, AccentBlue) }
    val filteredNotes = remember(state.recentNotes, searchQuery) {
        if (searchQuery.isBlank()) {
            state.recentNotes
        } else {
            val query = searchQuery.trim()
            state.recentNotes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                    note.preview.contains(query, ignoreCase = true)
            }
        }
    }

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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
                    .padding(horizontal = 16.dp)
            ) {
                SearchHeader(
                    value = searchQuery,
                    placeholder = stringResource(R.string.home_search_placeholder),
                    onValueChange = { searchQuery = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.home_recent_folders),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF26262B)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                FolderChipsRow(
                    items = state.recentFolders,
                    selectedId = state.selectedFolderId,
                    onSelect = onSelectFolder
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF5F6EFA))
                        }
                    }

                    filteredNotes.isEmpty() -> {
                        EmptyNotesState(
                            modifier = Modifier.weight(1f),
                            searchActive = searchQuery.isNotBlank()
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 104.dp)
                        ) {
                            items(filteredNotes) { note ->
                                Box(modifier = Modifier.clickable { onOpenNote(note.id) }) {
                                    NoteCard(
                                        title = note.title,
                                        preview = note.preview.ifBlank {
                                            stringResource(R.string.home_note_preview_fallback)
                                        },
                                        meta = if (state.noteActions[note.id]?.isFavorite == true) {
                                            stringResource(R.string.folders_stat_favorites)
                                        } else {
                                            ""
                                        },
                                        color = cardColors[note.colorIndex],
                                        onMoreClick = {
                                            selectedNoteForQuickActions = state.noteActions[note.id]
                                        },
                                        moreActionsTestTag = "home_note_more_actions_${note.id}",
                                        badgeTestTag = if (state.noteActions[note.id]?.isFavorite == true) {
                                            "home_note_favorite_badge_${note.id}"
                                        } else {
                                            null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            HomeAddButton(
                onClick = onAddNote,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 28.dp)
            )
        }
    }

    selectedNoteForQuickActions?.let { note ->
        NoteItemActionsSheet(
            note = note,
            onDismiss = { selectedNoteForQuickActions = null },
            onAddToFavorites = {
                onAddToFavoritesNote(note)
                selectedNoteForQuickActions = null
            },
            onMoveTo = {
                selectedNoteForQuickActions = null
                onMoveNote(note)
            },
            onRename = {
                noteToRename = note
                renameTextFieldValue = note.title
                selectedNoteForQuickActions = null
            },
            onDelete = {
                noteToDelete = note
                selectedNoteForQuickActions = null
            }
        )
    }

    noteToRename?.let { note ->
        AlertDialog(
            onDismissRequest = {
                noteToRename = null
                renameTextFieldValue = ""
            },
            title = { Text(stringResource(R.string.folders_rename_note_title)) },
            text = {
                OutlinedTextField(
                    value = renameTextFieldValue,
                    onValueChange = { renameTextFieldValue = it },
                    label = { Text(stringResource(R.string.folders_note_title_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("rename_text_field")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameTextFieldValue.isNotBlank()) {
                            onRenameNote(note, renameTextFieldValue)
                            noteToRename = null
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
                        noteToRename = null
                        renameTextFieldValue = ""
                    }
                ) {
                    Text(stringResource(R.string.folders_cancel_action))
                }
            }
        )
    }

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(stringResource(R.string.folders_delete_note_confirm_title)) },
            text = { Text(stringResource(R.string.folders_delete_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteNote(note)
                        noteToDelete = null
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
                TextButton(onClick = { noteToDelete = null }) {
                    Text(stringResource(R.string.folders_cancel_action))
                }
            }
        )
    }
}

@Composable
private fun FolderChipsRow(items: List<FolderUiModel>, selectedId: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All Notes Pill
        FolderPill(
            id = "all_notes",
            name = stringResource(R.string.folders_stat_all_notes),
            isSelected = selectedId == "all_notes",
            onClick = { onSelect("all_notes") }
        )

        // Favorites Pill
        FolderPill(
            id = "favorites",
            name = stringResource(R.string.folders_stat_favorites),
            isSelected = selectedId == "favorites",
            onClick = { onSelect("favorites") }
        )

        items.filter { !it.name.equals("Favorites", ignoreCase = true) }.forEach { folder ->
            FolderPill(
                id = folder.id,
                name = folder.name,
                isSelected = selectedId == folder.id,
                onClick = { onSelect(folder.id) }
            )
        }
    }
}

@Composable
private fun FolderPill(id: String, name: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) Color(0xFFDFECE7) else Color(0xFFEFEFF1)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = if (isSelected) Color(0xFF5E6A64) else Color(0xFF7A7A82),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        )
    }
}

@Composable
private fun EmptyNotesState(modifier: Modifier = Modifier, searchActive: Boolean) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(
                if (searchActive) R.string.home_search_empty_state else R.string.home_notes_empty_state
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7A7A82)
        )
    }
}

@Composable
private fun HomeAddButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
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
