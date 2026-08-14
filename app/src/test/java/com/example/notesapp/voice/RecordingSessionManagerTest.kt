package com.example.notesapp.voice

import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.RecordingSessionManager
import com.example.notesapp.domain.voice.RecordingSessionMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecordingSessionManagerTest {
    private val manager = RecordingSessionManager()
    private val firstMetadata = metadata("first")
    private val secondMetadata = metadata("second")

    @Test
    fun `replacing active session invokes discard and keeps only new token`() {
        val discarded = mutableListOf<String>()

        manager.replace(firstMetadata) { discarded += it.metadata.sessionId }
        val replacement = manager.replace(secondMetadata) { discarded += it.metadata.sessionId }

        assertEquals(listOf("first"), discarded)
        assertEquals("second", manager.current()?.metadata?.sessionId)
        assertEquals(replacement.token, manager.current()?.token)
    }

    @Test
    fun `clearing an old token does not clear the replacement`() {
        val first = manager.replace(firstMetadata) {}
        manager.replace(secondMetadata) {}

        manager.clear(first.token)

        assertEquals("second", manager.current()?.metadata?.sessionId)
    }

    @Test
    fun `clearing the active token leaves no session`() {
        val active = manager.replace(firstMetadata) {}

        manager.clear(active.token)

        assertNull(manager.current())
    }

    private fun metadata(sessionId: String) = RecordingSessionMetadata(
        sessionId = sessionId,
        noteId = "note-$sessionId",
        blockId = "block-$sessionId",
        audioFilePath = "/data/data/app/files/voice-notes/$sessionId.m4a",
        format = AudioFormat.AAC
    )
}
