package com.example.notesapp.ui.common.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.theme.LavenderPrimary

@Composable
fun AddFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = LavenderPrimary,
        contentColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Add")
    }
}
