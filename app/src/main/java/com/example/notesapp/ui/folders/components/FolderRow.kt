package com.example.notesapp.ui.folders.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.theme.LavenderPrimary
import com.example.notesapp.ui.theme.SurfaceCard
import com.example.notesapp.ui.theme.TextPrimary
import com.example.notesapp.ui.theme.TextSecondary

@Composable
fun FolderRow(
    name: String,
    count: String,
    modifier: Modifier = Modifier,
    depth: Int = 0,
    onAddClick: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .background(SurfaceCard, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Outlined.Folder, contentDescription = null, tint = TextPrimary)
            Text(text = name, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        }
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = count, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            
            if (onAddClick != null) {
                Surface(
                    onClick = onAddClick,
                    shape = CircleShape,
                    color = LavenderPrimary.copy(alpha = 0.1f),
                    contentColor = LavenderPrimary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add sub-item",
                        modifier = Modifier.padding(6.dp)
                    )
                }
            } else {
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}
