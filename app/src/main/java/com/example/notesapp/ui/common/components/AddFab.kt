package com.example.notesapp.ui.common.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun AddFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = LocalAppColors.current.primary,
        contentColor = LocalAppColors.current.onPrimary,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.testTag("add_fab")
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Add")
    }
}
