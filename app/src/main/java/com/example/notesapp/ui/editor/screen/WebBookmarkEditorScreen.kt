package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.editor.viewmodel.WebBookmarkEditorUiState
import com.example.notesapp.ui.editor.viewmodel.WebBookmarkEditorViewModel
import com.example.notesapp.ui.editor.viewmodel.WebBookmarkSaveResult
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun WebBookmarkEditorScreen(
    onBack: () -> Unit,
    onSaved: (WebBookmarkSaveResult) -> Unit,
    viewModel: WebBookmarkEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.saveResult.collect { result -> onSaved(result) }
    }
    WebBookmarkEditorScreenContent(
        uiState = uiState,
        onUrlChanged = viewModel::onUrlChanged,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@Composable
fun WebBookmarkEditorScreenContent(
    uiState: WebBookmarkEditorUiState,
    onUrlChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("web_bookmark_editor_page")
    ) {
        WebBookmarkEditorTopBar(isEditMode = uiState.isEditMode, onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.web_bookmark_guidance),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.url,
                onValueChange = onUrlChanged,
                label = { Text(stringResource(R.string.web_bookmark_url_label)) },
                supportingText = {
                    if (uiState.showInvalidUrlError) {
                        Text(
                            text = stringResource(R.string.web_bookmark_invalid_url_error),
                            color = colors.error,
                            modifier = Modifier.testTag("web_bookmark_inline_error")
                        )
                    } else {
                        Text(stringResource(R.string.web_bookmark_url_supporting))
                    }
                },
                isError = uiState.showInvalidUrlError,
                enabled = !uiState.isSaving,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSave() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("web_bookmark_url_field")
            )
            WebBookmarkMetadataStatus(uiState = uiState)
        }
        WebBookmarkBottomActions(
            isSaveEnabled = uiState.isSaveEnabled,
            onSave = onSave,
            onCancel = onBack
        )
    }
}

@Composable
private fun WebBookmarkEditorTopBar(isEditMode: Boolean, onBack: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("web_bookmark_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.web_bookmark_back_content_description),
                tint = colors.textPrimary
            )
        }
        Text(
            text = stringResource(
                if (isEditMode) {
                    R.string.web_bookmark_editor_edit_title
                } else {
                    R.string.web_bookmark_editor_add_title
                }
            ),
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(start = 8.dp)
                .testTag("web_bookmark_editor_page_title")
        )
    }
}

@Composable
private fun WebBookmarkMetadataStatus(uiState: WebBookmarkEditorUiState) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .testTag("web_bookmark_metadata_status")
    ) {
        when {
            uiState.isSaving -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .testTag("web_bookmark_metadata_progress"),
                    strokeWidth = 2.dp,
                    color = colors.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.web_bookmark_metadata_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            uiState.showsMetadataFallbackStatus -> Text(
                text = stringResource(R.string.web_bookmark_metadata_fallback),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            else -> Box(modifier = Modifier)
        }
    }
}

@Composable
private fun WebBookmarkBottomActions(isSaveEnabled: Boolean, onSave: () -> Unit, onCancel: () -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("web_bookmark_bottom_actions")
    ) {
        Button(
            onClick = onSave,
            enabled = isSaveEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("web_bookmark_save_button")
        ) {
            Text(text = stringResource(R.string.web_bookmark_save))
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("web_bookmark_cancel_button")
        ) {
            Text(
                text = stringResource(R.string.web_bookmark_cancel),
                color = colors.primary
            )
        }
    }
}
