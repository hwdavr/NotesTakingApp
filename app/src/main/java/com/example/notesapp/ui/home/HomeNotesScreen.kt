package com.example.notesapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.common.components.AddFab
import com.example.notesapp.ui.notes.NoteUiModel

@Composable
fun HomeNotesScreen(
    onAddNote: () -> Unit,
    onOpenNote: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = { AddFab(onClick = onAddNote) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF3F7FF), Color(0xFFEDF3FF))
                    )
                )
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(62.dp)) // Status bar space

            // Header
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(R.string.home_notes_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E24)
                )
                Text(
                    text = stringResource(R.string.home_notes_subtitle),
                    fontSize = 14.sp,
                    color = Color(0xFF5C5C73)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Folders Section
            Text(
                text = stringResource(R.string.home_recent_folders),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E1E24)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.recentFolders.forEach { folder ->
                    FolderCard(
                        name = folder.name,
                        noteCount = folder.noteCount,
                        modifier = Modifier.weight(1f),
                        isPrimary = folder.isPrimary
                    )
                }
                if (state.recentFolders.isEmpty()) {
                    // Placeholder or empty state if no folders
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
                } else if (state.recentFolders.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Notes Section
            Text(
                text = stringResource(R.string.home_recent_notes),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E1E24)
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (state.recentNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(text = "No recent notes", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        items(state.recentNotes) { note ->
                            NoteItem(note, onClick = { onOpenNote(note.id) })
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                thickness = 0.5.dp,
                                color = Color(0xFFE1E3EC)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderCard(name: String, noteCount: Int, modifier: Modifier = Modifier, isPrimary: Boolean) {
    Card(
        modifier = modifier.height(116.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) Color(0xFF6E63F6) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPrimary) Color.White else Color(0xFF1E1E24)
            )
            Text(
                text = String.format(stringResource(R.string.home_note_count), noteCount),
                fontSize = 13.sp,
                color = if (isPrimary) Color.White.copy(alpha = 0.8f) else Color(0xFF5C5C73)
            )
        }
    }
}

@Composable
fun NoteItem(note: NoteUiModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = note.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E1E24)
        )
        Text(
            text = note.preview,
            fontSize = 13.sp,
            color = Color(0xFF5C5C73),
            maxLines = 1
        )
    }
}
