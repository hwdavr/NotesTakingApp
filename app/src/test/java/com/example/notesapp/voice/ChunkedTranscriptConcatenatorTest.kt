package com.example.notesapp.voice

import com.example.notesapp.domain.voice.ChunkedTranscriptConcatenator
import org.junit.Assert.assertEquals
import org.junit.Test

class ChunkedTranscriptConcatenatorTest {
    @Test
    fun `overlapping final chunks append each boundary word once`() {
        val concatenator = ChunkedTranscriptConcatenator()

        concatenator.appendFinal(0, "The launch timeline needs to be finalized")
        val result = concatenator.appendFinal(1, "to be finalized by Friday")

        assertEquals("The launch timeline needs to be finalized by Friday", result)
    }

    @Test
    fun `partial result is replaced and final result commits it`() {
        val concatenator = ChunkedTranscriptConcatenator()

        assertEquals("The launch is", concatenator.appendPartial(0, "The launch is"))
        assertEquals("The launch is ready", concatenator.appendPartial(0, "The launch is ready"))
        assertEquals("The launch is ready today", concatenator.appendFinal(0, "The launch is ready today"))
        assertEquals("The launch is ready today", concatenator.currentText())
    }

    @Test
    fun `boundary fragments prefer the complete word from the next chunk`() {
        val concatenator = ChunkedTranscriptConcatenator()

        concatenator.appendFinal(0, "The back")
        val result = concatenator.appendFinal(1, "background task is ready")

        assertEquals("The background task is ready", result)
    }
}
