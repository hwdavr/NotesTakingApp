package com.example.notesapp.data.summary

import android.util.Log
import com.example.notesapp.di.IoDispatcher
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderCategorizer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class GeminiNanoFolderCategorizer @Inject constructor(
    private val promptClient: GeminiNanoFolderCategoryPromptClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FolderCategorizer {

    override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? =
        withContext(ioDispatcher) {
            if (folders.isEmpty()) return@withContext null

            try {
                val modelResult = promptClient.generateFolderCategory(
                    buildCategoryPrompt(title = title, content = content, folders = folders)
                )
                parseModelFolder(modelResult, folders)?.let { folder ->
                    return@withContext folder
                }
            } catch (e: Exception) {
                Log.w(TAG, "AICore categorization unavailable, using keyword fallback", e)
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

    private fun buildCategoryPrompt(title: String, content: String, folders: List<Folder>): String {
        val folderOptions = folders.joinToString(separator = "\n") { folder ->
            "- ${folder.id}: ${folder.name}"
        }
        return """
            Choose the best folder for this note.
            Reply with exactly one folder id from the list, or NONE if no folder fits.

            Folders:
            $folderOptions

            Note title:
            $title

            Note content:
            ${content.take(MAX_PROMPT_CONTENT_LENGTH)}
        """.trimIndent()
    }

    private fun parseModelFolder(modelResult: String?, folders: List<Folder>): Folder? {
        val normalizedResult = modelResult?.trim().orEmpty()
        if (normalizedResult.isBlank() || normalizedResult.equals(NO_MATCH_RESPONSE, ignoreCase = true)) {
            return null
        }

        folders.firstOrNull { folder ->
            normalizedResult.containsExactToken(folder.id)
        }?.let { return it }

        return folders.firstOrNull { folder ->
            normalizedResult.equals(folder.name, ignoreCase = true)
        }
    }

    private companion object {
        const val TAG = "NotesApp/GeminiNanoFolderCategorizer"
        const val MAX_PROMPT_CONTENT_LENGTH = 2_000
        const val NO_MATCH_RESPONSE = "NONE"
    }
}

private fun String.containsExactToken(token: String): Boolean {
    val escapedToken = Regex.escape(token)
    return Regex("(^|[^A-Za-z0-9_-])$escapedToken($|[^A-Za-z0-9_-])", RegexOption.IGNORE_CASE)
        .containsMatchIn(this)
}
