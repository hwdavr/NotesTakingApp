package com.example.notesapp.ui.voice.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notesapp.R
import com.example.notesapp.ui.theme.LocalAppColors
import com.example.notesapp.ui.voice.model.VoiceRecorderStatusLabel
import com.example.notesapp.ui.voice.model.VoiceRecorderUiState
import com.example.notesapp.ui.voice.model.toRenderState
import com.example.notesapp.ui.voice.viewmodel.VoiceRecorderViewModel

@Composable
fun VoiceRecorderScreen(
    noteId: String?,
    onSaved: (String) -> Unit,
    onDiscarded: () -> Unit,
    onBack: () -> Unit,
    viewModel: VoiceRecorderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val renderState = state.toRenderState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPermissionRationale by rememberSaveable { mutableStateOf(false) }
    var showDiscardConfirmation by rememberSaveable { mutableStateOf(false) }
    var discardSubmitted by rememberSaveable { mutableStateOf(false) }
    val permissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val permanentlyDenied = !granted && (context as? Activity)?.let {
            !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                it,
                Manifest.permission.RECORD_AUDIO
            )
        } == true
        viewModel.onPermissionResult(granted, permanentlyDenied)
        if (!granted && !permanentlyDenied) {
            showPermissionRationale = true
        }
    }

    LaunchedEffect(permissionGranted) {
        viewModel.onScreenReady(noteId, permissionGranted)
        if (!permissionGranted && !state.permissionPermanentlyDenied) {
            showPermissionRationale = true
        }
    }
    LaunchedEffect(state.permissionPermanentlyDenied) {
        if (state.permissionPermanentlyDenied) {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.voice_recorder_permission_denied),
                actionLabel = context.getString(R.string.voice_recorder_open_settings)
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
            viewModel.clearPermissionDenial()
        }
    }
    LaunchedEffect(renderState.isSaved, discardSubmitted) {
        if (renderState.isSaved) {
            onSaved(state.savedFilePath.orEmpty())
        }
        if (renderState.isReady && discardSubmitted) {
            discardSubmitted = false
            onDiscarded()
        }
    }

    Scaffold(
        containerColor = LocalAppColors.current.background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        VoiceRecorderContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
            showDiscardConfirmation = showDiscardConfirmation,
            onClose = { showDiscardConfirmation = true },
            onDiscardRequest = { showDiscardConfirmation = true },
            onDiscardConfirm = {
                showDiscardConfirmation = false
                discardSubmitted = true
                viewModel.onDiscard()
            },
            onDiscardCancel = { showDiscardConfirmation = false },
            onPauseResume = viewModel::onPauseResume,
            onStop = viewModel::onStop,
            onBack = onBack,
            onPermissionGrant = {
                showPermissionRationale = false
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        )
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text(stringResource(R.string.voice_recorder_permission_title)) },
            text = { Text(stringResource(R.string.voice_recorder_permission_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.testTag("voice_permission_grant")
                ) {
                    Text(stringResource(R.string.voice_recorder_permission_grant))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionRationale = false },
                    modifier = Modifier.testTag("voice_permission_cancel")
                ) {
                    Text(stringResource(R.string.voice_recorder_cancel_action))
                }
            }
        )
    }
}

@Composable
fun VoiceRecorderContent(
    state: VoiceRecorderUiState,
    modifier: Modifier = Modifier,
    showDiscardConfirmation: Boolean = false,
    onClose: () -> Unit,
    onDiscardRequest: () -> Unit,
    onDiscardConfirm: () -> Unit,
    onDiscardCancel: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    onBack: () -> Unit,
    onPermissionGrant: () -> Unit
) {
    val colors = LocalAppColors.current
    val renderState = state.toRenderState()
    val statusText = when (renderState.statusLabel) {
        VoiceRecorderStatusLabel.Recording -> {
            stringResource(R.string.voice_recorder_status_recording, renderState.elapsedText)
        }

        VoiceRecorderStatusLabel.Paused -> stringResource(R.string.voice_recorder_status_paused)
        else -> stringResource(R.string.voice_recorder_status_ready)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("recorder_close")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.voice_recorder_close_description),
                    tint = colors.textPrimary
                )
            }
            Surface(
                modifier = Modifier.testTag("recorder_status_pill"),
                shape = RoundedCornerShape(28.dp),
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (renderState.isRecording) {
                        colors.error
                    } else {
                        colors.textSecondary
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (renderState.error != null) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = stringResource(R.string.voice_recorder_error_description),
                    tint = colors.accentYellow,
                    modifier = Modifier.testTag("recorder_stt_warning")
                )
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = renderState.elapsedText,
            modifier = Modifier.testTag("recorder_elapsed_timer"),
            style = MaterialTheme.typography.displayLarge,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Waveform(
            amplitudes = renderState.amplitudes,
            paused = renderState.isPaused,
            modifier = Modifier.testTag("recorder_waveform")
        )
        Spacer(modifier = Modifier.height(28.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("recorder_transcript_preview"),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.voice_recorder_live_transcript_empty),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }
        if (renderState.showLoading) {
            CircularProgressIndicator(
                modifier = Modifier.testTag("recorder_loading"),
                color = colors.primary
            )
            Text(
                text = stringResource(R.string.voice_recorder_checking_preflight),
                color = colors.textSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (renderState.showPermissionRetry) {
            TextButton(
                onClick = onPermissionGrant,
                modifier = Modifier.testTag("voice_permission_retry")
            ) {
                Text(stringResource(R.string.voice_recorder_permission_grant))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        RecorderControls(
            enabled = renderState.isActive,
            paused = renderState.isPaused,
            onDiscard = onDiscardRequest,
            onPauseResume = onPauseResume,
            onStop = onStop
        )
        Spacer(modifier = Modifier.height(16.dp))
        FormatChip(labelRes = renderState.formatLabelRes)
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (renderState.error != null) {
        AlertDialog(
            onDismissRequest = onBack,
            modifier = Modifier.testTag(renderState.errorDialogTag.orEmpty()),
            title = {
                Text(
                    stringResource(
                        renderState.errorTitleRes ?: R.string.voice_recorder_microphone_error_title
                    )
                )
            },
            text = {
                Text(
                    stringResource(
                        renderState.errorMessageRes ?: R.string.voice_recorder_microphone_error_message
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onBack, modifier = Modifier.testTag("recorder_error_ok")) {
                    Text(stringResource(R.string.voice_recorder_ok))
                }
            }
        )
    }
    if (showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = onDiscardCancel,
            modifier = Modifier.testTag("recorder_discard_dialog"),
            title = { Text(stringResource(R.string.voice_recorder_discard_title)) },
            text = { Text(stringResource(R.string.voice_recorder_discard_message)) },
            confirmButton = {
                TextButton(
                    onClick = onDiscardConfirm,
                    modifier = Modifier.testTag("recorder_discard_confirm")
                ) {
                    Text(stringResource(R.string.voice_recorder_discard_action), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDiscardCancel,
                    modifier = Modifier.testTag("recorder_discard_cancel")
                ) {
                    Text(stringResource(R.string.voice_recorder_cancel_action))
                }
            }
        )
    }
}

@Composable
private fun Waveform(amplitudes: List<Float>, paused: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier.fillMaxWidth().height(128.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        List(64) { index ->
            val amplitude = amplitudes.getOrNull(index) ?: 0.08f
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((12f + amplitude * 100f).coerceAtMost(100f).dp)
                    .alpha(if (paused) 0.5f else 1f)
                    .background(
                        if (index % 3 == 0) colors.secondary else colors.primary,
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
private fun RecorderControls(
    enabled: Boolean,
    paused: Boolean,
    onDiscard: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDiscard,
            enabled = enabled,
            modifier = Modifier
                .size(64.dp)
                .border(1.dp, colors.border, CircleShape)
                .testTag("recorder_discard_btn")
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.voice_recorder_discard_description),
                tint = colors.textPrimary
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        IconButton(
            onClick = onPauseResume,
            enabled = enabled,
            modifier = Modifier
                .size(72.dp)
                .background(colors.surface, CircleShape)
                .testTag("recorder_toggle_record_btn")
        ) {
            Icon(
                imageVector = if (paused) {
                    Icons.Filled.PlayArrow
                } else {
                    Icons.Filled.Pause
                },
                contentDescription = stringResource(
                    if (paused) {
                        R.string.voice_recorder_resume_description
                    } else {
                        R.string.voice_recorder_pause_description
                    }
                ),
                tint = colors.primary
            )
        }
        Spacer(modifier = Modifier.width(20.dp))
        IconButton(
            onClick = onStop,
            enabled = enabled,
            modifier = Modifier
                .size(64.dp)
                .background(colors.primary, CircleShape)
                .testTag("recorder_stop_btn")
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.voice_recorder_stop_description),
                tint = colors.onPrimary
            )
        }
    }
}

@Composable
private fun FormatChip(labelRes: Int) {
    val colors = LocalAppColors.current
    Surface(
        modifier = Modifier.testTag("recorder_format_chip"),
        shape = RoundedCornerShape(10.dp),
        color = colors.transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            color = colors.textSecondary,
            fontSize = 12.sp
        )
    }
}
