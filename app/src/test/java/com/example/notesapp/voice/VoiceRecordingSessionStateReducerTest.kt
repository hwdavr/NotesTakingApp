package com.example.notesapp.voice

import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.RecordingSessionEvent
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import com.example.notesapp.domain.voice.RecordingSessionState
import com.example.notesapp.domain.voice.RecordingSessionStateReducer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceRecordingSessionStateReducerTest {
    private val reducer = RecordingSessionStateReducer()
    private val metadata = RecordingSessionMetadata(
        sessionId = "session",
        noteId = "note",
        blockId = "block",
        audioFilePath = "/data/data/app/files/voice-notes/vn_note_block_1.m4a",
        format = AudioFormat.AAC
    )

    @Test
    fun `active session pauses and resumes from the same elapsed value`() {
        val recording = reducer.reduce(
            RecordingSessionState.Idle,
            RecordingSessionEvent.Started(metadata)
        )
        val ticking = reducer.reduce(recording, RecordingSessionEvent.Tick(12_500L, 0.7f))
        val paused = reducer.reduce(ticking, RecordingSessionEvent.PauseRequested)
        val resumed = reducer.reduce(paused, RecordingSessionEvent.ResumeRequested)

        assertEquals(12_500L, (paused as RecordingSessionState.Paused).elapsedMs)
        assertEquals(12_500L, (resumed as RecordingSessionState.Recording).elapsedMs)
        assertEquals(listOf(0.7f), (resumed as RecordingSessionState.Recording).amplitudes)
    }

    @Test
    fun `stop then save produces a terminal saved state with file size`() {
        val recording = reducer.reduce(
            RecordingSessionState.Idle,
            RecordingSessionEvent.Started(metadata)
        )
        val saving = reducer.reduce(
            reducer.reduce(recording, RecordingSessionEvent.Tick(2_000L, 0.2f)),
            RecordingSessionEvent.StopRequested
        )
        val saved = reducer.reduce(saving, RecordingSessionEvent.SaveCompleted(4_096L))

        assertTrue(saving is RecordingSessionState.Saving)
        assertEquals(4_096L, (saved as RecordingSessionState.Saved).fileSizeBytes)
        assertEquals(2_000L, saved.elapsedMs)
    }

    @Test
    fun `illegal pause and stop events do not advance an idle session`() {
        val afterPause = reducer.reduce(RecordingSessionState.Idle, RecordingSessionEvent.PauseRequested)
        val afterStop = reducer.reduce(afterPause, RecordingSessionEvent.StopRequested)

        assertEquals(RecordingSessionState.Idle, afterPause)
        assertEquals(RecordingSessionState.Idle, afterStop)
    }

    @Test
    fun `discard cleans an active session state`() {
        val recording = reducer.reduce(
            RecordingSessionState.Idle,
            RecordingSessionEvent.Started(metadata)
        )

        assertEquals(
            RecordingSessionState.Idle,
            reducer.reduce(recording, RecordingSessionEvent.Discarded)
        )
    }
}
