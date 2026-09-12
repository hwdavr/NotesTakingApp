package com.example.notesapp.ui.editor.model

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class MentionDateKind {
    TODAY,
    TOMORROW,
    NEXT_TUESDAY
}

data class MentionDateSuggestion(
    val kind: MentionDateKind,
    val formattedDate: String,
    val insertText: String
)

data class MentionUserSuggestion(
    val email: String,
    val displayName: String,
    val isYou: Boolean,
    val isOwner: Boolean,
    val insertText: String
)

data class MentionNoteSuggestion(
    val id: String,
    val title: String,
    val folderName: String?,
    val insertText: String
)

class MentionsCalculator(private val clock: Clock = Clock.systemDefaultZone()) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    fun getDateSuggestions(): List<MentionDateSuggestion> {
        val today = LocalDate.now(clock)
        return listOf(
            MentionDateSuggestion(
                kind = MentionDateKind.TODAY,
                formattedDate = today.format(dateFormatter),
                insertText = "@Today"
            ),
            MentionDateSuggestion(
                kind = MentionDateKind.TOMORROW,
                formattedDate = today.plusDays(1).format(dateFormatter),
                insertText = "@Tomorrow"
            ),
            MentionDateSuggestion(
                kind = MentionDateKind.NEXT_TUESDAY,
                formattedDate = today
                    .with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
                    .format(dateFormatter),
                insertText = "@Next Tuesday 3pm"
            )
        )
    }
}
