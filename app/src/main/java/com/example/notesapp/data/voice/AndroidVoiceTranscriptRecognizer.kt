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
import androidx.annotation.RequiresApi
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
    private val factory: SpeechRecognizerFactory,
    private val audioSourceRegistry: TranscriptAudioSourceRegistry
) : VoiceTranscriptRecognizer {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var sessionId: String? = null
    private var languageTag = Locale.US.toLanguageTag()
    private var callback: ((TranscriptRecognitionEvent) -> Unit)? = null
    private var paused = false
    private var chunkIndex = 0
    private var listening = false
    private var sourceFed = false

    override fun start(request: TranscriptStartRequest, onEvent: (TranscriptRecognitionEvent) -> Unit) {
        stopRecognizer()
        sessionId = request.sessionId
        languageTag = request.languageTag
        callback = onEvent
        paused = false
        chunkIndex = 0
        sourceFed = false
        val source = if (canStartSourceFedRecognition()) {
            audioSourceRegistry.find(request.sessionId)
        } else {
            null
        }
        if (source == null) {
            emitUnavailable(request.sessionId)
        } else {
            mainHandler.post {
                if (sessionId != request.sessionId) return@post
                runCatching {
                    recognizer = createRecognizer()
                    recognizer?.setRecognitionListener(listener)
                    startListeningIfSupported(request.languageTag, source)
                }.onFailure {
                    source.disable()
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
                if (sessionId != null && !sourceFed) {
                    val source = sessionId?.let(audioSourceRegistry::find)
                    if (source != null) startListeningIfSupported(languageTag, source)
                }
            }
        }
    }

    override fun stop() {
        stopRecognizer()
        callback = null
        sessionId = null
        paused = false
        chunkIndex = 0
        sourceFed = false
    }

    override fun cancel() {
        val currentSessionId = sessionId
        if (currentSessionId != null) {
            emit(TranscriptRecognitionEvent.Cancelled(currentSessionId))
        }
        stop()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun startListening(language: String, source: PcmAudioSource) {
        val speechRecognizer = recognizer ?: return
        if (paused || sessionId == null || listening) return
        val audioSource = source.attachForRecognizer() ?: run {
            emitUnavailable(sessionId ?: return)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, audioSource)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, source.channelCount)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, source.encoding)
            putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, source.sampleRateHertz)
            putExtra(RecognizerIntent.EXTRA_SEGMENTED_SESSION, RecognizerIntent.EXTRA_AUDIO_SOURCE)
            putExtra(RecognizerIntent.EXTRA_ENABLE_FORMATTING, RecognizerIntent.FORMATTING_OPTIMIZE_QUALITY)
        }
        listening = true
        sourceFed = true
        speechRecognizer.startListening(intent)
    }

    private fun startListeningIfSupported(language: String, source: PcmAudioSource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startListening(language, source)
        } else {
            source.disable()
            sessionId?.let(::emitUnavailable)
        }
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
            if (!factory.isRecognitionAvailable(context)) {
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
            if (sourceFed) {
                audioSourceRegistry.find(currentSessionId)?.disable()
            }
            emit(
                TranscriptRecognitionEvent.Failed(
                    sessionId = currentSessionId,
                    chunkIndex = chunkIndex
                )
            )
            chunkIndex += 1
            if (!sourceFed) {
                mainHandler.postDelayed({
                    if (sessionId == currentSessionId && !paused) {
                        val source = audioSourceRegistry.find(currentSessionId)
                        if (source != null) startListeningIfSupported(languageTag, source)
                    }
                }, RESTART_DELAY_MS)
            }
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
                if (!sourceFed) {
                    mainHandler.post {
                        val source = sessionId?.let(audioSourceRegistry::find)
                        if (source != null) startListeningIfSupported(languageTag, source)
                    }
                }
            }
        }

        override fun onSegmentResults(segmentResults: Bundle) {
            onResults(segmentResults)
        }

        override fun onEndOfSegmentedSession() {
            listening = false
            sessionId?.let { audioSourceRegistry.find(it)?.disable() }
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

    private fun canStartSourceFedRecognition(): Boolean = factory.isRecognitionAvailable(context) &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        factory.isOnDeviceRecognitionAvailable(context)

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
