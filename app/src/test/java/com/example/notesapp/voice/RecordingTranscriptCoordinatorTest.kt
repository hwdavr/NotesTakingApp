package com.example.notesapp.voice

import com.example.notesapp.data.voice.RecordingTranscriptCoordinator
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.TranscriptRecognitionEvent
import com.example.notesapp.domain.voice.TranscriptSessionStatus
import com.example.notesapp.domain.voice.TranscriptStartRequest
import com.example.notesapp.domain.voice.VoiceTranscriptRecognizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingTranscriptCoordinatorTest {
    private val recognizer = FakeRecognizer()
    private val coordinator = RecordingTranscriptCoordinator(recognizer)
    private val metadata = RecordingSessionMetadata(
        sessionId = "session",
        noteId = "note",
        blockId = "block",
        audioFilePath = "/tmp/voice.m4a",
        format = AudioFormat.AAC
    )

    @Test
    fun `partial final pause resume and stop preserve transcript state`() {
        coordinator.start(metadata)
        recognizer.emit(TranscriptRecognitionEvent.Partial("session", 0, "hello"))
        assertEquals("hello", coordinator.state.value.previewText)

        coordinator.pause()
        assertEquals(TranscriptSessionStatus.Paused, coordinator.state.value.status)
        coordinator.resume()
        assertEquals(TranscriptSessionStatus.Recognizing, coordinator.state.value.status)
        recognizer.emit(TranscriptRecognitionEvent.Final("session", 0, "hello world"))

        assertEquals("hello world", coordinator.stop())
        assertEquals(TranscriptSessionStatus.Completed, coordinator.state.value.status)
    }

    @Test
    fun `source unavailable and stale events do not corrupt active transcript`() {
        coordinator.start(metadata)
        recognizer.emit(TranscriptRecognitionEvent.AudioSourceUnavailable("session", "en-US"))
        assertEquals(TranscriptSessionStatus.AudioOnly, coordinator.state.value.status)

        recognizer.emit(TranscriptRecognitionEvent.Final("different-session", 0, "stale"))
        assertEquals("", coordinator.state.value.previewText)

        coordinator.stop()
    }

    @Test
    fun `failed segment is marked and cancellation clears the session`() {
        coordinator.start(metadata)
        recognizer.emit(TranscriptRecognitionEvent.Failed("session", 0))

        assertTrue(coordinator.state.value.previewText.contains("transcription failed for this segment"))

        coordinator.cancel()

        assertEquals(TranscriptSessionStatus.Idle, coordinator.state.value.status)
        assertEquals("", coordinator.state.value.previewText)
        assertTrue(recognizer.cancelled)
    }

    private class FakeRecognizer : VoiceTranscriptRecognizer {
        private var callback: ((TranscriptRecognitionEvent) -> Unit)? = null
        var cancelled = false

        override fun start(request: TranscriptStartRequest, onEvent: (TranscriptRecognitionEvent) -> Unit) {
            callback = onEvent
            cancelled = false
        }

        override fun pause() = Unit

        override fun resume() = Unit

        override fun stop() {
            callback = null
        }

        override fun cancel() {
            cancelled = true
            callback = null
        }

        fun emit(event: TranscriptRecognitionEvent) {
            callback?.invoke(event)
        }
    }
}
