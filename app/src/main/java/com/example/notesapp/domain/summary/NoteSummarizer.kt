package com.example.notesapp.domain.summary

interface NoteSummarizer {
    suspend fun summarize(title: String, noteText: String): NoteSummary
}
