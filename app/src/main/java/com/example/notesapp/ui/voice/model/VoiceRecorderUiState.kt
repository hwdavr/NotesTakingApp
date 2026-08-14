package com.example.notesapp.ui.voice.model

import com.example.notesapp.domain.voice.AudioFormat

enum class VoiceRecorderStatus {
    Loading,
    PermissionRequired,
    Ready,
    Recording,
    Paused,
    Saving,
    Saved,
    Error
}

enum class VoiceRecorderError {
    MicrophoneUnavailable,
    StorageInsufficient,
    RecordingFailed,
    SavingFailed
}

enum class VoiceRecorderTranscriptStatus {
    Idle,
    Recognizing,
    Paused,
    AudioOnly,
    Completed
}

enum class VoiceRecorderTranscriptWarning {
    ModelUnavailable,
    AudioSourceUnavailable,
    ChunkTimedOut,
    RecognitionFailed
}

data class VoiceRecorderUiState(
    val status: VoiceRecorderStatus = VoiceRecorderStatus.Loading,
    val format: AudioFormat = AudioFormat.AAC,
    val elapsedMs: Long = 0L,
    val amplitudes: List<Float> = emptyList(),
    val error: VoiceRecorderError? = null,
    val permissionPermanentlyDenied: Boolean = false,
    val savedFilePath: String? = null,
    val savedFileSizeBytes: Long = 0L,
    val transcript: String = "",
    val transcriptPartial: String = "",
    val transcriptStatus: VoiceRecorderTranscriptStatus = VoiceRecorderTranscriptStatus.Idle,
    val transcriptWarning: VoiceRecorderTranscriptWarning? = null
) {
    val transcriptPreview: String
        get() = when {
            transcript.isBlank() -> transcriptPartial
            transcriptPartial.isBlank() -> transcript
            else -> "$transcript $transcriptPartial"
        }
}
