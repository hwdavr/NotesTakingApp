package com.example.notesapp.domain.summary

data class NoteSummary(
    val text: String
)

sealed interface NoteSummaryResult {
    data class Success(val summary: NoteSummary) : NoteSummaryResult
    data object Empty : NoteSummaryResult
    data object Unavailable : NoteSummaryResult
}

class NoteSummaryUnavailableException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
