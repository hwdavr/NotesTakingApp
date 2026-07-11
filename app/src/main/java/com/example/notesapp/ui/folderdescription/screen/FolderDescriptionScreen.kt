package com.example.notesapp.ui.folderdescription.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.folderdescription.viewmodel.FolderDescriptionUiState
import com.example.notesapp.ui.folderdescription.viewmodel.FolderDescriptionViewModel
import com.example.notesapp.ui.theme.LocalAppColors

@Composable
fun FolderDescriptionScreen(
    parentPadding: PaddingValues,
    onBack: () -> Unit,
    viewModel: FolderDescriptionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect {
            onBack()
        }
    }
    FolderDescriptionContent(
        parentPadding = parentPadding,
        state = state,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onSave = viewModel::save,
        onBack = viewModel::cancel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDescriptionContent(
    parentPadding: PaddingValues,
    state: FolderDescriptionUiState,
    onDescriptionChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalAppColors.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.folder_description_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("folder_description_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.folder_description_back_action)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = state.canSave && !state.isSaving,
                        modifier = Modifier.testTag("folder_description_save_button")
                    ) {
                        Text(stringResource(R.string.folder_description_save_action))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.textPrimary,
                    actionIconContentColor = colors.primary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
                .padding(bottom = parentPadding.calculateBottomPadding())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("folder_description_screen")
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("folder_description_loading"),
                    color = colors.primary
                )
                state.errorMessageRes != null -> FolderDescriptionError(
                    message = stringResource(state.errorMessageRes),
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> FolderDescriptionEditor(
                    state = state,
                    onDescriptionChanged = onDescriptionChanged
                )
            }
        }
    }
}

@Composable
private fun FolderDescriptionEditor(state: FolderDescriptionUiState, onDescriptionChanged: (String) -> Unit) {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = state.folderName,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            ),
            modifier = Modifier.testTag("folder_description_folder_name")
        )
        Text(
            text = stringResource(R.string.folder_description_helper_text),
            style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = onDescriptionChanged,
            label = { Text(stringResource(R.string.folder_description_field_label)) },
            placeholder = { Text(stringResource(R.string.folder_description_field_placeholder)) },
            minLines = 8,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("folder_description_text_field")
        )
        if (state.isSaving) {
            Row(
                modifier = Modifier.testTag("folder_description_saving"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = colors.primary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = stringResource(R.string.folder_description_saving_label),
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                )
            }
        }
    }
}

@Composable
private fun FolderDescriptionError(message: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("folder_description_error"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(color = colors.textSecondary)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
