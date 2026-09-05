package com.example.notesapp.ui.editor.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.editor.viewmodel.NoteLinkPickerItem
import com.example.notesapp.ui.editor.viewmodel.NoteLinkPickerUiState
import com.example.notesapp.ui.editor.viewmodel.NoteLinkPickerViewModel
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun NoteLinkPickerScreen(
    onBack: () -> Unit,
    onSelectNote: (noteId: String, noteTitle: String) -> Unit,
    onRemoveLink: () -> Unit,
    viewModel: NoteLinkPickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NoteLinkPickerScreenContent(
        uiState = uiState,
        onBack = onBack,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onRetry = viewModel::retry,
        onSelectNote = onSelectNote,
        onRemoveLink = onRemoveLink
    )
}

@Composable
fun NoteLinkPickerScreenContent(
    uiState: NoteLinkPickerUiState,
    onBack: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onRetry: () -> Unit,
    onSelectNote: (noteId: String, noteTitle: String) -> Unit,
    onRemoveLink: () -> Unit
) {
    val colors = LocalAppColors.current
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("note_link_picker_screen"),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            NoteLinkPickerTopBar(onBack = onBack)

            val currentQuery = when (uiState) {
                is NoteLinkPickerUiState.Content -> uiState.searchQuery
                is NoteLinkPickerUiState.Empty -> uiState.searchQuery
                else -> ""
            }

            NoteLinkPickerSearchBox(
                query = currentQuery,
                onQueryChange = onSearchQueryChanged
            )

            // 16dp spacing between search input and result list
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is NoteLinkPickerUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("note_link_picker_loading"),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                is NoteLinkPickerUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("note_link_picker_error"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.message.ifBlank { stringResource(R.string.note_link_picker_error) },
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.testTag("note_link_picker_retry")
                        ) {
                            Text(stringResource(R.string.note_link_picker_retry))
                        }
                    }
                }
                is NoteLinkPickerUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("note_link_picker_empty"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.note_link_picker_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary
                        )
                    }
                }
                is NoteLinkPickerUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("note_link_picker_results")
                    ) {
                        if (uiState.hasExistingLink) {
                            item(key = "remove_link_action") {
                                RemoveLinkRow(onRemoveLink = onRemoveLink)
                            }
                        }
                        items(
                            items = uiState.notes,
                            key = { it.id }
                        ) { note ->
                            NoteLinkCandidateRow(
                                candidate = note,
                                onClick = { onSelectNote(note.id, note.title) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteLinkPickerTopBar(onBack: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("note_link_picker_back")
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = stringResource(R.string.note_link_picker_back),
                tint = colors.textPrimary
            )
        }
        Text(
            text = stringResource(R.string.note_link_picker_title),
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun NoteLinkPickerSearchBox(query: String, onQueryChange: (String) -> Unit) {
    val colors = LocalAppColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.searchBackground,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = colors.searchIcon
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("note_link_picker_search"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.note_link_picker_search_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.Clear,
                        contentDescription = stringResource(R.string.note_link_picker_clear_search),
                        tint = colors.searchIcon
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoveLinkRow(onRemoveLink: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onRemoveLink)
            .testTag("note_link_picker_remove_link")
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.LinkOff,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.note_link_picker_remove_link),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.error,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NoteLinkCandidateRow(candidate: NoteLinkPickerItem, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    val folderSubtitle = candidate.folderName ?: stringResource(R.string.note_link_picker_no_folder)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .testTag("note_link_picker_note_${candidate.id}")
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.StickyNote2,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = candidate.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = folderSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
