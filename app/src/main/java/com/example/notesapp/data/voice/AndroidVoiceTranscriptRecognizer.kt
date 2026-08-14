package com.example.notesapp.data.voice

import android.content.Context
import android.os.Build
import android.speech.SpeechRecognizer
import com.example.notesapp.domain.voice.TranscriptRecognitionEvent
import com.example.notesapp.domain.voice.TranscriptStartRequest
import com.example.notesapp.domain.voice.VoiceTranscriptRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceTranscriptRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) : VoiceTranscriptRecognizer {
    private var sessionId: String? = null
    private var callback: ((TranscriptRecognitionEvent) -> Unit)? = null
    private var paused = false

    override fun start(request: TranscriptStartRequest, onEvent: (TranscriptRecognitionEvent) -> Unit) {
        sessionId = request.sessionId
        callback = onEvent
        paused = false
        if (!isRecognitionModelAvailable()) {
            onEvent(
                TranscriptRecognitionEvent.ModelUnavailable(
                    sessionId = request.sessionId,
                    languageTag = request.languageTag
                )
            )
        } else {
            onEvent(
                TranscriptRecognitionEvent.AudioSourceUnavailable(
                    sessionId = request.sessionId,
                    languageTag = request.languageTag
                )
            )
        }
    }

    override fun pause() {
        if (sessionId != null && !paused) {
            paused = true
        }
    }

    override fun resume() {
        if (sessionId != null && paused) {
            paused = false
        }
    }

    override fun stop() {
        callback = null
        sessionId = null
        paused = false
    }

    override fun cancel() {
        val currentSessionId = sessionId
        if (currentSessionId != null) {
            callback?.invoke(TranscriptRecognitionEvent.Cancelled(currentSessionId))
        }
        callback = null
        sessionId = null
        paused = false
    }

    private fun isRecognitionModelAvailable(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    } else {
        SpeechRecognizer.isRecognitionAvailable(context)
    }
}
