package com.example.notesapp.ui.editor.model

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class MentionDateSuggestion(
    val description: String,
    val formattedDate: String,
    val insertText: String
)

data class MentionUserSuggestion(
    val email: String,
    val displayName: String,
    val isYou: Boolean,
    val isOwner: Boolean,
    // "You" or "Guest"
    val displayBadge: String,
    val insertText: String
)

data class MentionNoteSuggestion(
    val id: String,
    val title: String,
    val folderBreadcrumb: String,
    val insertText: String
)

class MentionsCalculator(private val clock: Clock = Clock.systemDefaultZone()) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)

    fun getDateSuggestions(): List<MentionDateSuggestion> {
        val today = LocalDate.now(clock)
        val tomorrow = today.plusDays(1)
        val nextTuesday = today.with(TemporalAdjusters.next(DayOfWeek.TUESDAY))

        return listOf(
            MentionDateSuggestion(
                description = "Today",
                formattedDate = today.format(dateFormatter),
                insertText = "@Today"
            ),
            MentionDateSuggestion(
                description = "Tomorrow",
                formattedDate = tomorrow.format(dateFormatter),
                insertText = "@Tomorrow"
            ),
            MentionDateSuggestion(
                description = "Next Tuesday 3pm",
                formattedDate = nextTuesday.format(dateFormatter),
                insertText = "@Next Tuesday 3pm"
            )
        )
    }
}
