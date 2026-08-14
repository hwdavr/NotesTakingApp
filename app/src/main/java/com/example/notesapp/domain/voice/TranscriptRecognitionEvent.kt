package com.example.notesapp.domain.voice

sealed interface TranscriptRecognitionEvent {
    val sessionId: String

    data class Partial(
        override val sessionId: String,
        val chunkIndex: Int,
        val text: String
    ) : TranscriptRecognitionEvent

    data class Final(
        override val sessionId: String,
        val chunkIndex: Int,
        val text: String
    ) : TranscriptRecognitionEvent

    data class ModelUnavailable(
        override val sessionId: String,
        val languageTag: String
    ) : TranscriptRecognitionEvent

    data class AudioSourceUnavailable(
        override val sessionId: String,
        val languageTag: String
    ) : TranscriptRecognitionEvent

    data class ChunkTimedOut(
        override val sessionId: String,
        val chunkIndex: Int
    ) : TranscriptRecognitionEvent

    data class Failed(
        override val sessionId: String,
        val chunkIndex: Int
    ) : TranscriptRecognitionEvent

    data class Cancelled(
        override val sessionId: String
    ) : TranscriptRecognitionEvent
}
