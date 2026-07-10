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
class MediaPipeTextFolderCategorizer @Inject constructor(
    private val embeddingClient: FolderTextEmbeddingClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FolderCategorizer {

    override suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder? =
        withContext(ioDispatcher) {
            if (folders.isEmpty()) return@withContext null

            try {
                val noteText = buildNoteText(title, content)
                val folderPaths = buildFolderPaths(folders)
                val bestMatch = folders
                    .filter { folder -> folder.name.isNotBlank() }
                    .map { folder ->
                        val similarity = embeddingClient.similarity(
                            noteText,
                            folder.toEmbeddingText(folderPaths.getValue(folder.id))
                        )
                        FolderScore(folder = folder, score = similarity + keywordBonus(title, content, folder))
                    }
                    .maxWithOrNull(compareBy<FolderScore> { score -> score.score }.thenBy { score -> score.folder.id })

                if (bestMatch != null && bestMatch.score >= MIN_SEMANTIC_SCORE) {
                    Log.d(TAG, "MediaPipe categorization matched folder; score=${bestMatch.score.roundForLog()}")
                    return@withContext bestMatch.folder
                }
                Log.d(TAG, "MediaPipe categorization confidence too low; score=${bestMatch?.score?.roundForLog()}")
            } catch (e: Exception) {
                Log.w(TAG, "MediaPipe categorization unavailable, using fallback; cause=${e.javaClass.simpleName}")
            }

            runKeywordMatching(title, content, folders)
                ?: chooseRootFallback(folders)
        }

    private fun buildNoteText(title: String, content: String): String =
        "Title: $title\nContent: ${content.take(MAX_EMBEDDING_CONTENT_LENGTH)}"

    private fun Folder.toEmbeddingText(folderPath: String): String {
        val trimmedDescription = description.trim()
        return if (trimmedDescription.isBlank()) {
            "Folder path: $folderPath"
        } else {
            "Folder path: $folderPath. Description: ${trimmedDescription.take(MAX_FOLDER_DESCRIPTION_LENGTH)}"
        }
    }

    private fun runKeywordMatching(title: String, content: String, folders: List<Folder>): Folder? {
        val lowerTitle = title.lowercase()
        val lowerContent = content.lowercase()
        var bestMatch: Folder? = null
        var highestScore = 0

        for (folder in folders) {
            val score = keywordScore(lowerTitle, lowerContent, folder)
            if (score > highestScore) {
                highestScore = score
                bestMatch = folder
            }
        }

        return if (highestScore > 0) bestMatch else null
    }

    private fun keywordBonus(title: String, content: String, folder: Folder): Double {
        val normalizedKeywordScore = keywordScore(title.lowercase(), content.lowercase(), folder)
            .toDouble()
            .coerceAtMost(MAX_KEYWORD_SCORE) / MAX_KEYWORD_SCORE
        return normalizedKeywordScore * MAX_KEYWORD_BONUS
    }

    private fun keywordScore(lowerTitle: String, lowerContent: String, folder: Folder): Int {
        val folderName = folder.name.lowercase()
        if (folderName.isBlank()) return 0

        var score = 0
        if (lowerTitle.contains(folderName)) {
            score += EXACT_TITLE_MATCH_SCORE
        }
        if (lowerContent.contains(folderName)) {
            val occurrences = lowerContent.split(folderName).size - 1
            score += occurrences * EXACT_CONTENT_MATCH_SCORE
        }

        val words = folderName.split("\\s+".toRegex()).filter { word -> word.length > MIN_KEYWORD_LENGTH }
        if (words.size > 1) {
            for (word in words) {
                if (lowerTitle.contains(word)) {
                    score += WORD_TITLE_MATCH_SCORE
                }
                if (lowerContent.contains(word)) {
                    val occurrences = lowerContent.split(word).size - 1
                    score += occurrences
                }
            }
        }

        return score
    }

    private fun buildFolderPaths(folders: List<Folder>): Map<String, String> {
        val foldersById = folders.associateBy { folder -> folder.id }
        return folders.associate { folder ->
            folder.id to buildFolderPath(folder, foldersById)
        }
    }

    private fun buildFolderPath(folder: Folder, foldersById: Map<String, Folder>): String {
        val pathSegments = ArrayDeque<String>()
        val visitedIds = mutableSetOf<String>()
        var current: Folder? = folder

        while (current != null && visitedIds.add(current.id)) {
            pathSegments.addFirst(current.name)
            current = current.parentFolderId?.let(foldersById::get)
        }

        return pathSegments.joinToString(separator = FOLDER_PATH_SEPARATOR)
    }

    private fun chooseRootFallback(folders: List<Folder>): Folder? = folders
        .filter { folder -> folder.parentFolderId == null }
        .sortedWith(
            compareBy<Folder> { folder -> folder.sortKey }
                .thenBy { folder -> folder.name }
                .thenBy { folder -> folder.id }
        )
        .firstOrNull()

    private fun Double.roundForLog(): String = "%.3f".format(this)

    private data class FolderScore(
        val folder: Folder,
        val score: Double
    )

    private companion object {
        const val TAG = "NotesApp/MediaPipeTextFolderCategorizer"
        const val MAX_EMBEDDING_CONTENT_LENGTH = 2_000
        const val MAX_FOLDER_DESCRIPTION_LENGTH = 300
        const val MIN_SEMANTIC_SCORE = 0.2
        const val MAX_KEYWORD_SCORE = 20.0
        const val MAX_KEYWORD_BONUS = 0.15
        const val EXACT_TITLE_MATCH_SCORE = 10
        const val EXACT_CONTENT_MATCH_SCORE = 5
        const val WORD_TITLE_MATCH_SCORE = 2
        const val MIN_KEYWORD_LENGTH = 2
        const val FOLDER_PATH_SEPARATOR = " / "
    }
}
