package com.example.notesapp.ui.editor.model

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class MentionsCalculatorTest {

    @Test
    fun testGetDateSuggestions_withFixedClock() {
        // Monday, June 1, 2026 12:00:00 UTC
        val fixedInstant = Instant.parse("2026-06-01T12:00:00Z")
        val fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

        val calculator = MentionsCalculator(fixedClock)
        val suggestions = calculator.getDateSuggestions()

        assertEquals(3, suggestions.size)

        // Today is June 1
        assertEquals("Today", suggestions[0].description)
        assertEquals("01 Jun 2026", suggestions[0].formattedDate)
        assertEquals("@Today", suggestions[0].insertText)

        // Tomorrow is June 2
        assertEquals("Tomorrow", suggestions[1].description)
        assertEquals("02 Jun 2026", suggestions[1].formattedDate)
        assertEquals("@Tomorrow", suggestions[1].insertText)

        // Next Tuesday is June 2 (since June 1 is Monday, the very next Tuesday is June 2)
        assertEquals("Next Tuesday 3pm", suggestions[2].description)
        assertEquals("02 Jun 2026", suggestions[2].formattedDate)
        assertEquals("@Next Tuesday 3pm", suggestions[2].insertText)
    }
}
