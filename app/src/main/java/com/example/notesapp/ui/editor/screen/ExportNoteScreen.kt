package com.example.notesapp.ui.editor.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.editor.viewmodel.ExportFormat
import com.example.notesapp.ui.editor.viewmodel.ExportNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportNoteScreen(
    parentPadding: PaddingValues,
    noteId: String,
    onBack: () -> Unit,
    viewModel: ExportNoteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.export_success)
    val errorMessage = stringResource(R.string.export_error)
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }
    LaunchedEffect(state.exportSuccess) {
        if (state.exportSuccess) {
            snackbarHostState.showSnackbar(successMessage)
            viewModel.resetStatus()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.resetStatus()
        }
    }
    // SAF Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            if (state.selectedFormat == ExportFormat.Markdown) "text/markdown" else "application/pdf"
        )
    ) { uri ->
        uri?.let { viewModel.exportToUri(it) }
    }
    Scaffold(
        modifier = Modifier.padding(parentPadding),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.export_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF8FAFF)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            state.note?.let { note ->
                Text(
                    text = note.title.ifBlank { stringResource(R.string.editor_untitled_note) },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2A44)
                    )
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.export_format_label),
                    modifier = Modifier.align(Alignment.Start),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7281A7)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                FormatOption(
                    title = stringResource(R.string.export_markdown),
                    icon = Icons.Outlined.Description,
                    selected = state.selectedFormat == ExportFormat.Markdown,
                    onClick = { viewModel.selectFormat(ExportFormat.Markdown) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                FormatOption(
                    title = stringResource(R.string.export_pdf),
                    icon = Icons.Outlined.PictureAsPdf,
                    selected = state.selectedFormat == ExportFormat.PDF,
                    onClick = { viewModel.selectFormat(ExportFormat.PDF) }
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val extension = if (state.selectedFormat == ExportFormat.Markdown) ".md" else ".pdf"
                        val filename = (note.title.ifBlank { "note" }) + extension
                        launcher.launch(filename)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6E7BFF)
                    ),
                    enabled = !state.isExporting
                ) {
                    if (state.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.export_button),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6E7BFF))
            }
        }
    }
}

@Composable
private fun FormatOption(title: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val borderColor = if (selected) Color(0xFF6E7BFF) else Color(0xFFEAF1FF)
    val backgroundColor = if (selected) Color(0xFFF0F4FF) else Color.White
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFEAF1FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF6E7BFF),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2A44)
                )
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF6E7BFF),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, Color(0xFFEAF1FF), RoundedCornerShape(12.dp))
                )
            }
        }
    }
}
