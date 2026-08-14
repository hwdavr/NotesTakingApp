package com.example.notesapp.voice

import com.example.notesapp.data.voice.RecordingTranscriptCoordinator
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.TranscriptRecognitionEvent
import com.example.notesapp.domain.voice.TranscriptStartRequest
import com.example.notesapp.domain.voice.TranscriptWarning
import com.example.notesapp.domain.voice.VoiceTranscriptRecognizer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingTranscriptWatchdogTest {
    @Test
    fun silentChunkTimesOutAfter65SecondsAndContinuesWithFailureMarker() {
        val recognizer = RecordingRecognizer()
        val watchdogScope = TestScope(StandardTestDispatcher())
        val coordinator = RecordingTranscriptCoordinator(recognizer)
        coordinator.useWatchdogScope(watchdogScope)

        coordinator.start(
            RecordingSessionMetadata(
                sessionId = "session",
                noteId = "note",
                blockId = "block",
                audioFilePath = "/private/voice.m4a",
                format = AudioFormat.AAC
            )
        )

        watchdogScope.advanceTimeBy(65_001L)
        watchdogScope.runCurrent()

        assertEquals(
            "<transcription failed for this segment>",
            coordinator.state.value.committedText
        )
        assertTrue(coordinator.state.value.warning is TranscriptWarning.ChunkTimedOut)
    }

    private class RecordingRecognizer : VoiceTranscriptRecognizer {
        override fun start(request: TranscriptStartRequest, onEvent: (TranscriptRecognitionEvent) -> Unit) = Unit

        override fun pause() = Unit

        override fun resume() = Unit

        override fun stop() = Unit

        override fun cancel() = Unit
    }
}
