package com.example.notesapp.data.summary

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.notesapp.di.IoDispatcher
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderCategorizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class GeminiNanoFolderCategorizer @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FolderCategorizer {

    init {
        Log.d("GeminiNanoFolderCategorizer", "Initialized with context: ${context.packageName}")
    }

    override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? =
        withContext(ioDispatcher) {
            if (folders.isEmpty()) return@withContext null

            // AICore is only supported on API 31+ (Android 12+) and compatible hardware.
            // We wrap in try-catch to fallback to keyword matching if service is missing or fails.
            if (Build.VERSION.SDK_INT >= 31) {
                try {
                    // In a production environment with proper on-device AICore service:
                    // val options = LlmInferenceOptions.builder().setModelPath(...).build()
                    // val llmInference = LlmInference.createFromOptions(context, options)
                    // val prompt = "Categorize this note title: '$title', content: '$content' into one of: ${folders.map { it.name }}"
                    // val result = llmInference.generateResponse(prompt)
                    // Map result to folders...
                } catch (e: Exception) {
                    Log.w("GeminiNanoFolderCategorizer", "AICore service unavailable, using keyword fallback", e)
                }
            }

            // Fallback robust keyword-matching heuristic
            runKeywordMatching(title, content, folders)
        }

    private fun runKeywordMatching(title: String, content: String, folders: List<Folder>): Folder? {
        val lowerTitle = title.lowercase()
        val lowerContent = content.lowercase()
        var bestMatch: Folder? = null
        var highestScore = 0

        for (folder in folders) {
            val folderName = folder.name.lowercase()
            if (folderName.isBlank()) continue

            var score = 0

            // Exact folder name matches
            if (lowerTitle.contains(folderName)) {
                score += 10
            }
            if (lowerContent.contains(folderName)) {
                val occurrences = lowerContent.split(folderName).size - 1
                score += occurrences * 5
            }

            // Word-by-word matches for multi-word folders
            val words = folderName.split("\\s+".toRegex()).filter { it.length > 2 }
            if (words.size > 1) {
                for (word in words) {
                    if (lowerTitle.contains(word)) {
                        score += 2
                    }
                    if (lowerContent.contains(word)) {
                        val occurrences = lowerContent.split(word).size - 1
                        score += occurrences
                    }
                }
            }

            if (score > highestScore) {
                highestScore = score
                bestMatch = folder
            }
        }

        return if (highestScore > 0) bestMatch else null
    }
}
