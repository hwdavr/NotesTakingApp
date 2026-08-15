package com.example.notesapp.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.test.core.app.ApplicationProvider
import com.example.notesapp.data.voice.AndroidVoiceTranscriptRecognizer
import com.example.notesapp.data.voice.PcmAudioSource
import com.example.notesapp.data.voice.SpeechRecognizerFactory
import com.example.notesapp.data.voice.TranscriptAudioSourceRegistry
import com.example.notesapp.domain.voice.TranscriptStartRequest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class VoiceTranscriptionFailureReproductionTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun givenRecorderOwnsMicrophone_whenRecognizerStarts_thenIntentUsesRecordedAudioSource() {
        val speechRecognizer = mockk<SpeechRecognizer>(relaxed = true)
        val factory = mockk<SpeechRecognizerFactory>()
        val intentSlot = slot<Intent>()
        every { factory.isRecognitionAvailable(context) } returns true
        every { factory.isOnDeviceRecognitionAvailable(context) } returns true
        every { factory.create(context) } returns speechRecognizer
        every { speechRecognizer.setRecognitionListener(any()) } just Runs
        every { speechRecognizer.startListening(capture(intentSlot)) } just Runs
        val registry = TranscriptAudioSourceRegistry()
        registry.register("session", PcmAudioSource(sampleRateHertz = 44_100))
        val adapter = AndroidVoiceTranscriptRecognizer(context, factory, registry)

        adapter.start(
            request = TranscriptStartRequest(
                sessionId = "session",
                audioFilePath = "/private/voice.m4a"
            ),
            onEvent = {}
        )
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue(
            "The recognizer started a microphone-only intent instead of using the recording source",
            intentSlot.captured.hasExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE)
        )
        adapter.stop()
        registry.remove("session")
    }
}
