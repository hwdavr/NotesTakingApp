package com.example.notesapp.domain.voice

data class RecordingSessionMetadata(
    val sessionId: String,
    val noteId: String,
    val blockId: String,
    val audioFilePath: String,
    val format: AudioFormat
)

sealed interface RecordingSessionState {
    data object Idle : RecordingSessionState

    data class Recording(
        val metadata: RecordingSessionMetadata,
        val elapsedMs: Long,
        val amplitudes: List<Float>
    ) : RecordingSessionState

    data class Paused(
        val metadata: RecordingSessionMetadata,
        val elapsedMs: Long,
        val amplitudes: List<Float>
    ) : RecordingSessionState

    data class Saving(
        val metadata: RecordingSessionMetadata,
        val elapsedMs: Long
    ) : RecordingSessionState

    data class Saved(
        val metadata: RecordingSessionMetadata,
        val elapsedMs: Long,
        val fileSizeBytes: Long
    ) : RecordingSessionState

    data class Error(
        val message: String,
        val metadata: RecordingSessionMetadata? = null,
        val elapsedMs: Long = 0L
    ) : RecordingSessionState
}

sealed interface RecordingSessionEvent {
    data class Started(val metadata: RecordingSessionMetadata) : RecordingSessionEvent

    data class Tick(
        val elapsedMs: Long,
        val amplitude: Float
    ) : RecordingSessionEvent

    data object PauseRequested : RecordingSessionEvent
    data object ResumeRequested : RecordingSessionEvent
    data object StopRequested : RecordingSessionEvent
    data class SaveCompleted(val fileSizeBytes: Long) : RecordingSessionEvent
    data class SaveFailed(val message: String) : RecordingSessionEvent
    data class RecordingFailed(val message: String) : RecordingSessionEvent
    data object Discarded : RecordingSessionEvent
}
