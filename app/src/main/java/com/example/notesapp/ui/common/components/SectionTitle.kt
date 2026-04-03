package com.example.notesapp.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.notesapp.ui.theme.TextSecondary

@Composable
fun SectionTitle(
    title: String,
    actionLabel: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (actionLabel != null) {
            Text(text = actionLabel, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
