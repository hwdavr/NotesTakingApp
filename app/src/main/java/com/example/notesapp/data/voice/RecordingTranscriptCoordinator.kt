package com.example.notesapp.data.voice

import com.example.notesapp.domain.voice.ChunkedTranscriptConcatenator
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.TranscriptRecognitionEvent
import com.example.notesapp.domain.voice.TranscriptSessionState
import com.example.notesapp.domain.voice.TranscriptSessionStatus
import com.example.notesapp.domain.voice.TranscriptStartRequest
import com.example.notesapp.domain.voice.TranscriptWarning
import com.example.notesapp.domain.voice.VoiceTranscriptRecognizer
import com.example.notesapp.domain.voice.VoiceTranscriptSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Singleton
class RecordingTranscriptCoordinator @Inject constructor(
    private val recognizer: VoiceTranscriptRecognizer
) : VoiceTranscriptSession {
    private var watchdogScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    internal fun useWatchdogScope(scope: CoroutineScope) {
        watchdogScope = scope
    }

    private val mutableState = MutableStateFlow(TranscriptSessionState())
    private var activeSessionId: String? = null
    private var concatenator = ChunkedTranscriptConcatenator()
    private var watchdogJob: Job? = null
    private var nextChunkIndex = 0

    override val state: StateFlow<TranscriptSessionState> = mutableState.asStateFlow()

    override fun start(metadata: RecordingSessionMetadata) {
        activeSessionId = metadata.sessionId
        concatenator = ChunkedTranscriptConcatenator()
        nextChunkIndex = 0
        watchdogJob?.cancel()
        mutableState.value = TranscriptSessionState(
            sessionId = metadata.sessionId,
            status = TranscriptSessionStatus.Recognizing
        )
        recognizer.start(
            request = TranscriptStartRequest(
                sessionId = metadata.sessionId,
                audioFilePath = metadata.audioFilePath
            ),
            onEvent = ::onRecognitionEvent
        )
        if (mutableState.value.status == TranscriptSessionStatus.Recognizing) {
            armChunkWatchdog(metadata.sessionId, nextChunkIndex)
        }
    }

    override fun pause() {
        if (mutableState.value.status != TranscriptSessionStatus.Recognizing) return
        recognizer.pause()
        mutableState.value = mutableState.value.copy(status = TranscriptSessionStatus.Paused)
    }

    override fun resume() {
        if (mutableState.value.status != TranscriptSessionStatus.Paused) return
        recognizer.resume()
        mutableState.value = mutableState.value.copy(
            status = TranscriptSessionStatus.Recognizing,
            warning = null
        )
    }

    override fun stop(): String {
        watchdogJob?.cancel()
        recognizer.stop()
        val current = mutableState.value
        mutableState.value = current.copy(
            committedText = concatenator.currentText(),
            partialText = "",
            status = TranscriptSessionStatus.Completed
        )
        return concatenator.currentText()
    }

    override fun cancel() {
        watchdogJob?.cancel()
        activeSessionId?.let {
            recognizer.cancel()
        }
        activeSessionId = null
        concatenator.reset()
        mutableState.value = TranscriptSessionState()
    }

    private fun onRecognitionEvent(event: TranscriptRecognitionEvent) {
        if (event.sessionId != activeSessionId) return
        val current = mutableState.value
        if (current.status == TranscriptSessionStatus.AudioOnly && event.isIgnoredInAudioOnly()) {
            return
        }
        mutableState.value = when (event) {
            is TranscriptRecognitionEvent.Partial -> current.copy(
                committedText = concatenator.currentText().also {
                    concatenator.appendPartial(event.chunkIndex, event.text)
                },
                partialText = concatenator.currentPartialText(),
                status = TranscriptSessionStatus.Recognizing
            )

            is TranscriptRecognitionEvent.Final -> current.copy(
                committedText = concatenator.appendFinal(event.chunkIndex, event.text),
                partialText = "",
                status = TranscriptSessionStatus.Recognizing,
                warning = null
            )

            is TranscriptRecognitionEvent.ModelUnavailable -> current.copy(
                status = TranscriptSessionStatus.AudioOnly,
                warning = TranscriptWarning.ModelUnavailable(event.languageTag),
                partialText = ""
            )

            is TranscriptRecognitionEvent.AudioSourceUnavailable -> current.copy(
                status = TranscriptSessionStatus.AudioOnly,
                warning = TranscriptWarning.AudioSourceUnavailable(event.languageTag),
                partialText = ""
            )

            is TranscriptRecognitionEvent.ChunkTimedOut -> current.copy(
                partialText = "",
                status = TranscriptSessionStatus.Recognizing,
                warning = TranscriptWarning.ChunkTimedOut(event.chunkIndex)
            )

            is TranscriptRecognitionEvent.Failed -> current.copy(
                partialText = "",
                status = TranscriptSessionStatus.Recognizing,
                warning = TranscriptWarning.RecognitionFailed(event.chunkIndex)
            )

            is TranscriptRecognitionEvent.Cancelled -> TranscriptSessionState()
        }.also {
            when (event) {
                is TranscriptRecognitionEvent.ModelUnavailable,
                is TranscriptRecognitionEvent.AudioSourceUnavailable -> watchdogJob?.cancel()
                is TranscriptRecognitionEvent.Final -> {
                    nextChunkIndex = maxOf(nextChunkIndex, event.chunkIndex + 1)
                    armChunkWatchdog(event.sessionId, nextChunkIndex)
                }
                is TranscriptRecognitionEvent.ChunkTimedOut -> {
                    nextChunkIndex = maxOf(nextChunkIndex, event.chunkIndex + 1)
                    armChunkWatchdog(event.sessionId, nextChunkIndex)
                }
                is TranscriptRecognitionEvent.Failed -> {
                    nextChunkIndex = maxOf(nextChunkIndex, event.chunkIndex + 1)
                    armChunkWatchdog(event.sessionId, nextChunkIndex)
                }
                is TranscriptRecognitionEvent.Cancelled,
                is TranscriptRecognitionEvent.Partial -> Unit
            }
        }
    }

    private fun TranscriptRecognitionEvent.isIgnoredInAudioOnly(): Boolean = when (this) {
        is TranscriptRecognitionEvent.ModelUnavailable,
        is TranscriptRecognitionEvent.AudioSourceUnavailable,
        is TranscriptRecognitionEvent.Cancelled -> false
        else -> true
    }

    private fun armChunkWatchdog(sessionId: String, chunkIndex: Int) {
        watchdogJob?.cancel()
        watchdogJob = watchdogScope.launch {
            delay(SILENT_CHUNK_TIMEOUT_MS)
            if (isActive && activeSessionId == sessionId) {
                onRecognitionEvent(
                    TranscriptRecognitionEvent.ChunkTimedOut(
                        sessionId = sessionId,
                        chunkIndex = chunkIndex
                    )
                )
            }
        }
    }

    private companion object {
        const val SILENT_CHUNK_TIMEOUT_MS = 65_000L
    }
}
