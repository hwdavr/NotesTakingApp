package com.example.notesapp.domain.summary

interface NoteSummarizer {
    suspend fun summarize(noteText: String): NoteSummary
}
