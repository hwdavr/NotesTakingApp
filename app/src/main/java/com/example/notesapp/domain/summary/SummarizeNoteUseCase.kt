package com.example.notesapp.domain.summary

import javax.inject.Inject

private const val MIN_SUMMARY_INPUT_CHARS = 400
private const val MAX_SUMMARY_INPUT_CHARS = 12_000

class SummarizeNoteUseCase @Inject constructor(
    private val noteSummarizer: NoteSummarizer
) {
    suspend operator fun invoke(title: String, noteText: String): NoteSummaryResult {
        val normalizedText = noteText.trim()
        if (normalizedText.length < MIN_SUMMARY_INPUT_CHARS) {
            return NoteSummaryResult.Empty
        }

        return runCatching {
            val summary = noteSummarizer.summarize(title, normalizedText.take(MAX_SUMMARY_INPUT_CHARS))
            if (summary.text.isBlank()) {
                NoteSummaryResult.Unavailable
            } else {
                NoteSummaryResult.Success(summary)
            }
        }.getOrElse {
            NoteSummaryResult.Unavailable
        }
    }
}
