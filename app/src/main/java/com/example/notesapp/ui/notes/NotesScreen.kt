package com.example.notesapp.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.common.components.AddFab
import com.example.notesapp.ui.common.components.AppSearchBar
import com.example.notesapp.ui.common.components.SectionTitle
import com.example.notesapp.ui.notes.components.FolderChipsRow
import com.example.notesapp.ui.notes.components.NoteCard
import com.example.notesapp.ui.theme.AccentMint
import com.example.notesapp.ui.theme.AccentPink
import com.example.notesapp.ui.theme.AccentYellow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

@Composable
fun NotesScreen(parentPadding: PaddingValues) {
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
                    Text(text = "My Notes", style = MaterialTheme.typography.headlineMedium)
                    Text(text = "Capture ideas, plans, and reminders.", style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                AppSearchBar(
                    value = search,
                    onValueChange = { search = it },
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
                SectionTitle(title = "Latest notes")
            }

            item {
                NoteCard(
                    title = "Trip planning",
                    preview = "Book flights, shortlist places to stay, and prepare itinerary notes for the weekend.",
                    meta = "Personal • Updated 2h ago",
                    color = AccentYellow
                )
            }

            item {
                NoteCard(
                    title = "Sprint tasks",
                    preview = "Polish Android UI, add folder tree, and connect note editor to local storage.",
                    meta = "Work • Updated today",
                    color = AccentPink
                )
            }

            item {
                NoteCard(
                    title = "Startup ideas",
                    preview = "Offline-first note app with folder nesting, quick search, and calm pastel UI.",
                    meta = "Ideas • Yesterday",
                    color = AccentMint
                )
            }
        }
    }
}
