package com.example.notesapp.voice

import com.example.notesapp.domain.voice.AudioFilenameGenerator
import com.example.notesapp.domain.voice.AudioFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AudioFilenameGeneratorTest {
    private val generator = AudioFilenameGenerator()

    @Test
    fun `generates private m4a filename for AAC`() {
        val filename = generator.generate(
            noteId = "note-123",
            blockId = "block-456",
            timestampMs = 1_725_000_000_000,
            format = AudioFormat.AAC
        )

        assertEquals("vn_note-123_block-456_1725000000000.m4a", filename)
    }

    @Test
    fun `generates ogg filename for OPUS`() {
        val filename = generator.generate(
            noteId = "Note 123",
            blockId = "Block/456",
            timestampMs = 10L,
            format = AudioFormat.OPUS
        )

        assertEquals("vn_note_123_block_456_10.ogg", filename)
    }

    @Test
    fun `rejects negative timestamp`() {
        assertThrows(IllegalArgumentException::class.java) {
            generator.generate("note", "block", -1L, AudioFormat.AAC)
        }
    }
}
