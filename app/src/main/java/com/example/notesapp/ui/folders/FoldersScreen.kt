package com.example.notesapp.ui.folders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.notesapp.ui.folders.components.FolderRow

@Composable
fun FoldersScreen(parentPadding: PaddingValues) {
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
                    onValueChange = { search = it },
                    placeholder = "Search folders"
                )
            }

            item { SectionTitle(title = "Smart collections") }
            item { FolderRow(name = "All Notes", count = "12") }
            item { FolderRow(name = "Favorites", count = "4") }
            item { FolderRow(name = "Archive", count = "2") }

            item { SectionTitle(title = "My folders") }
            item { FolderRow(name = "Personal", count = "6") }
            item { FolderRow(name = "Work", count = "5") }
            item { FolderRow(name = "Travel Plans", count = "3") }
            item { FolderRow(name = "Ideas", count = "8") }
        }
    }
}
