package com.example.notesapp.ui.editor.model

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class MentionsCalculatorTest {

    private val clock = Clock.fixed(
        Instant.parse("2026-09-12T12:00:00Z"),
        ZoneId.of("Asia/Singapore")
    )

    @Test
    fun `date suggestions are stable for the current day and include display dates`() {
        val suggestions = MentionsCalculator(clock).getDateSuggestions()

        assertEquals(
            listOf("@Today", "@Tomorrow", "@Next Tuesday 3pm"),
            suggestions.map { it.insertText }
        )
        assertEquals(
            listOf("12 Sep 2026", "13 Sep 2026", "15 Sep 2026"),
            suggestions.map { it.formattedDate }
        )
    }
}
