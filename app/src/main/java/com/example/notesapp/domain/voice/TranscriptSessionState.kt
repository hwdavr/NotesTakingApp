package com.example.notesapp.domain.voice

enum class TranscriptSessionStatus {
    Idle,
    Recognizing,
    Paused,
    AudioOnly,
    Completed
}

sealed interface TranscriptWarning {
    data class ModelUnavailable(val languageTag: String) : TranscriptWarning

    data class AudioSourceUnavailable(val languageTag: String) : TranscriptWarning

    data class ChunkTimedOut(val chunkIndex: Int) : TranscriptWarning

    data class RecognitionFailed(val chunkIndex: Int) : TranscriptWarning
}

data class TranscriptSessionState(
    val sessionId: String? = null,
    val committedText: String = "",
    val partialText: String = "",
    val status: TranscriptSessionStatus = TranscriptSessionStatus.Idle,
    val warning: TranscriptWarning? = null
) {
    val previewText: String
        get() = when {
            committedText.isBlank() -> partialText
            partialText.isBlank() -> committedText
            else -> "$committedText $partialText"
        }
}

interface VoiceTranscriptRecognizer {
    fun start(request: TranscriptStartRequest, onEvent: (TranscriptRecognitionEvent) -> Unit)

    fun pause()

    fun resume()

    fun stop()

    fun cancel()
}

data class TranscriptStartRequest(
    val sessionId: String,
    val audioFilePath: String,
    val languageTag: String = DEFAULT_LANGUAGE_TAG
)

interface VoiceTranscriptSession {
    val state: kotlinx.coroutines.flow.StateFlow<TranscriptSessionState>

    fun start(metadata: RecordingSessionMetadata)

    fun pause()

    fun resume()

    fun stop(): String

    fun cancel()
}

private const val DEFAULT_LANGUAGE_TAG = "en-US"
