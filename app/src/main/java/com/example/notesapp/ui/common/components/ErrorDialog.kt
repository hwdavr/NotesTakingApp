package com.example.notesapp.ui.common.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource

@Composable
fun ErrorDialog(title: String = "Error", message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("error_dialog_confirm_button")
            ) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}
