package com.example.notesapp.data.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.notesapp.domain.voice.TranscriptRecognitionEvent
import com.example.notesapp.domain.voice.TranscriptStartRequest
import com.example.notesapp.domain.voice.VoiceTranscriptRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceTranscriptRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val factory: SpeechRecognizerFactory = AndroidSpeechRecognizerFactory()
) : VoiceTranscriptRecognizer {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var sessionId: String? = null
    private var languageTag = Locale.US.toLanguageTag()
    private var callback: ((TranscriptRecognitionEvent) -> Unit)? = null
    private var paused = false
    private var chunkIndex = 0
    private var listening = false

    override fun start(request: TranscriptStartRequest, onEvent: (TranscriptRecognitionEvent) -> Unit) {
        stopRecognizer()
        sessionId = request.sessionId
        languageTag = request.languageTag
        callback = onEvent
        paused = false
        chunkIndex = 0
        if (!isRecognitionModelAvailable()) {
            emitUnavailable(request.sessionId)
            return
        }
        mainHandler.post {
            if (sessionId != request.sessionId) return@post
            runCatching {
                recognizer = createRecognizer()
                recognizer?.setRecognitionListener(listener)
                startListening(request.languageTag)
            }.onFailure {
                emit(
                    TranscriptRecognitionEvent.Failed(
                        sessionId = request.sessionId,
                        chunkIndex = chunkIndex
                    )
                )
                stopRecognizer()
            }
        }
    }

    override fun pause() {
        if (sessionId != null && !paused) {
            paused = true
            mainHandler.post { recognizer?.stopListening() }
        }
    }

    override fun resume() {
        if (sessionId != null && paused) {
            paused = false
            mainHandler.post {
                if (sessionId != null) startListening(languageTag)
            }
        }
    }

    override fun stop() {
        stopRecognizer()
        callback = null
        sessionId = null
        paused = false
        chunkIndex = 0
    }

    override fun cancel() {
        val currentSessionId = sessionId
        if (currentSessionId != null) {
            emit(TranscriptRecognitionEvent.Cancelled(currentSessionId))
        }
        stop()
    }

    private fun startListening(language: String) {
        val speechRecognizer = recognizer ?: return
        if (paused || sessionId == null || listening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1_500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1_500L)
        }
        listening = true
        speechRecognizer.startListening(intent)
    }

    private fun createRecognizer(): SpeechRecognizer = factory.create(context)

    private fun stopRecognizer() {
        listening = false
        mainHandler.post {
            recognizer?.runCatching { cancel() }
            recognizer?.destroy()
            recognizer = null
        }
    }

    private fun emitUnavailable(currentSessionId: String) {
        emit(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !factory.isOnDeviceRecognitionAvailable(context)
            ) {
                TranscriptRecognitionEvent.ModelUnavailable(currentSessionId, languageTag)
            } else {
                TranscriptRecognitionEvent.AudioSourceUnavailable(currentSessionId, languageTag)
            }
        )
    }

    private fun emit(event: TranscriptRecognitionEvent) {
        if (event.sessionId == sessionId) callback?.invoke(event)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            listening = false
        }

        override fun onError(error: Int) {
            listening = false
            val currentSessionId = sessionId ?: return
            if (paused) return
            emit(
                TranscriptRecognitionEvent.Failed(
                    sessionId = currentSessionId,
                    chunkIndex = chunkIndex
                )
            )
            chunkIndex += 1
            mainHandler.postDelayed({
                if (sessionId == currentSessionId && !paused) startListening(languageTag)
            }, RESTART_DELAY_MS)
        }

        override fun onResults(results: Bundle?) {
            listening = false
            val currentSessionId = sessionId ?: return
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (text.isNotEmpty()) {
                emit(
                    TranscriptRecognitionEvent.Final(
                        sessionId = currentSessionId,
                        chunkIndex = chunkIndex,
                        text = text
                    )
                )
            }
            chunkIndex += 1
            if (!paused) {
                mainHandler.post { startListening(languageTag) }
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val currentSessionId = sessionId ?: return
            val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
                .orEmpty()
            if (text.isNotEmpty()) {
                emit(
                    TranscriptRecognitionEvent.Partial(
                        sessionId = currentSessionId,
                        chunkIndex = chunkIndex,
                        text = text
                    )
                )
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun isRecognitionModelAvailable(): Boolean = factory.isRecognitionAvailable(context)

    private companion object {
        const val RESTART_DELAY_MS = 250L
    }
}

interface SpeechRecognizerFactory {
    fun isRecognitionAvailable(context: Context): Boolean

    fun isOnDeviceRecognitionAvailable(context: Context): Boolean

    fun create(context: Context): SpeechRecognizer
}

@Singleton
class AndroidSpeechRecognizerFactory @Inject constructor() : SpeechRecognizerFactory {
    override fun isRecognitionAvailable(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context) ||
                SpeechRecognizer.isRecognitionAvailable(context)
        } else {
            SpeechRecognizer.isRecognitionAvailable(context)
        }
    }

    override fun isOnDeviceRecognitionAvailable(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

    override fun create(context: Context): SpeechRecognizer {
        return if (isOnDeviceRecognitionAvailable(context)) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
    }
}
