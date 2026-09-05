package com.example.notesapp.ui.editor.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.notesapp.R
import com.example.notesapp.ui.editor.viewmodel.FormulaSheetUiState
import com.example.notesapp.ui.theme.LocalAppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaEditorSheet(
    state: FormulaSheetUiState?,
    onSourceChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    if (state == null) return

    val colors = LocalAppColors.current
    val sourceFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        repeat(10) {
            withFrameNanos { }
            var focused = false
            try {
                sourceFocusRequester.requestFocus()
                focused = true
            } catch (_: IllegalStateException) {
                focused = false
            }
            if (focused) return@LaunchedEffect
        }
    }
    val preview = InlineFormulaRenderer.render(state.source)
    val previewText = when {
        state.source.isBlank() -> stringResource(R.string.editor_formula_preview_empty)
        preview.isValid -> preview.displayText
        else -> preview.displayText.ifBlank { stringResource(R.string.editor_formula_preview_unavailable) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        modifier = Modifier.testTag("editor_formula_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(
                    if (state.isEditing) {
                        R.string.editor_formula_sheet_edit_title
                    } else {
                        R.string.editor_formula_sheet_title
                    }
                ),
                color = colors.textPrimary,
                style = MaterialTheme.typography.headlineSmall
            )
            OutlinedTextField(
                value = state.source,
                onValueChange = onSourceChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 112.dp)
                    .focusRequester(sourceFocusRequester)
                    .testTag("editor_formula_source_input"),
                label = { Text(stringResource(R.string.editor_formula_input_label)) },
                isError = state.hasValidationError,
                minLines = 3,
                maxLines = 6
            )
            if (state.hasValidationError) {
                Text(
                    text = stringResource(R.string.editor_formula_validation_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("editor_formula_validation_error")
                )
            }
            Text(
                text = stringResource(R.string.editor_formula_preview),
                color = colors.textSecondary,
                style = MaterialTheme.typography.titleMedium
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .horizontalScroll(rememberScrollState())
                    .testTag("editor_formula_preview")
            ) {
                Text(
                    text = previewText,
                    color = if (preview.isValid) colors.textPrimary else colors.textSecondary,
                    style = MaterialTheme.typography.headlineSmall,
                    softWrap = false,
                    modifier = Modifier.testTag("editor_formula_preview_text")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("editor_formula_cancel")
                ) {
                    Text(stringResource(R.string.editor_formula_cancel))
                }
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("editor_formula_submit")
                ) {
                    Text(
                        stringResource(
                            if (state.isEditing) {
                                R.string.editor_formula_update
                            } else {
                                R.string.editor_formula_insert
                            }
                        )
                    )
                }
            }
        }
    }
}
