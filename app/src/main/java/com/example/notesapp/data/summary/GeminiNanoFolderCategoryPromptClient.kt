package com.example.notesapp.data.summary

interface GeminiNanoFolderCategoryPromptClient {
    suspend fun generateFolderCategory(prompt: String): String?
}
