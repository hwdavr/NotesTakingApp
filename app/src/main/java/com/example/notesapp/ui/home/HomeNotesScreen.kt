package com.example.notesapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeNotesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = "My Notes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E24)
            )
            Text(
                text = "Quickly capture and revisit your ideas",
                fontSize = 14.sp,
                color = Color(0xFF5C5C73)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Folders Section
        Text(
            text = "Recent folders",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E1E24)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FolderCard(
                name = "Product",
                noteCount = 12,
                modifier = Modifier.weight(1f),
                isPrimary = false
            )
            FolderCard(
                name = "Personal",
                noteCount = 8,
                modifier = Modifier.weight(1f),
                isPrimary = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Notes Section
        Text(
            text = "Recent notes",
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
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(sampleNotes) { note ->
                    NoteItem(note)
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
                text = "$noteCount notes",
                fontSize = 13.sp,
                color = if (isPrimary) Color.White.copy(alpha = 0.8f) else Color(0xFF5C5C73)
            )
        }
    }
}

@Composable
fun NoteItem(note: NoteSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            text = note.snippet,
            fontSize = 13.sp,
            color = Color(0xFF5C5C73),
            maxLines = 1
        )
    }
}

data class NoteSummary(val title: String, val snippet: String)

val sampleNotes = listOf(
    NoteSummary("Sprint retro takeaways", "Great progress on the UI framework..."),
    NoteSummary("Meeting with Design Team", "Discussed the new onboarding flow..."),
    NoteSummary("Grocery List", "Milk, Eggs, Bread, Coffee, Fruits..."),
    NoteSummary("Project Ideas", "Brainstorming session for the next app...")
)
