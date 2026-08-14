package com.example.notesapp.ui.voice.model

import com.example.notesapp.R
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.formatElapsedTime

enum class VoiceRecorderStatusLabel {
    Ready,
    Recording,
    Paused
}

data class VoiceRecorderRenderState(
    val statusLabel: VoiceRecorderStatusLabel,
    val isRecording: Boolean,
    val isPaused: Boolean,
    val isActive: Boolean,
    val isReady: Boolean,
    val isSaved: Boolean,
    val elapsedText: String,
    val amplitudes: List<Float>,
    val error: VoiceRecorderError?,
    val errorTitleRes: Int?,
    val errorMessageRes: Int?,
    val errorDialogTag: String?,
    val showLoading: Boolean,
    val showPermissionRetry: Boolean,
    val formatLabelRes: Int
)

fun VoiceRecorderUiState.toRenderState(): VoiceRecorderRenderState {
    val isRecording = status == VoiceRecorderStatus.Recording
    val isPaused = status == VoiceRecorderStatus.Paused
    val errorDetails = error?.let(::errorDetails)

    return VoiceRecorderRenderState(
        statusLabel = when {
            isRecording -> VoiceRecorderStatusLabel.Recording
            isPaused -> VoiceRecorderStatusLabel.Paused
            else -> VoiceRecorderStatusLabel.Ready
        },
        isRecording = isRecording,
        isPaused = isPaused,
        isActive = isRecording || isPaused,
        isReady = status == VoiceRecorderStatus.Ready,
        isSaved = status == VoiceRecorderStatus.Saved,
        elapsedText = formatElapsedTime(elapsedMs),
        amplitudes = amplitudes,
        error = error,
        errorTitleRes = errorDetails?.titleRes,
        errorMessageRes = errorDetails?.messageRes,
        errorDialogTag = errorDetails?.dialogTag,
        showLoading = status == VoiceRecorderStatus.Loading,
        showPermissionRetry = status == VoiceRecorderStatus.PermissionRequired,
        formatLabelRes = if (format == AudioFormat.OPUS) {
            R.string.voice_recorder_format_opus
        } else {
            R.string.voice_recorder_format_aac
        }
    )
}

private data class VoiceRecorderErrorDetails(
    val titleRes: Int,
    val messageRes: Int,
    val dialogTag: String
)

private fun errorDetails(error: VoiceRecorderError): VoiceRecorderErrorDetails = when (error) {
    VoiceRecorderError.StorageInsufficient -> VoiceRecorderErrorDetails(
        titleRes = R.string.voice_recorder_storage_error_title,
        messageRes = R.string.voice_recorder_storage_error_message,
        dialogTag = "recorder_storage_full_dialog"
    )

    VoiceRecorderError.MicrophoneUnavailable -> VoiceRecorderErrorDetails(
        titleRes = R.string.voice_recorder_microphone_error_title,
        messageRes = R.string.voice_recorder_microphone_error_message,
        dialogTag = "recorder_error_dialog"
    )

    VoiceRecorderError.RecordingFailed -> VoiceRecorderErrorDetails(
        titleRes = R.string.voice_recorder_recording_error_title,
        messageRes = R.string.voice_recorder_recording_error_message,
        dialogTag = "recorder_error_dialog"
    )

    VoiceRecorderError.SavingFailed -> VoiceRecorderErrorDetails(
        titleRes = R.string.voice_recorder_saving_error_title,
        messageRes = R.string.voice_recorder_saving_error_message,
        dialogTag = "recorder_error_dialog"
    )
}
