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
                    Log.d(TAG, "AICore categorization mapped output; responseLength=${modelResult.orEmpty().length}")
                    return@withContext folder
                }
                Log.d(TAG, "AICore categorization output unmapped; responseLength=${modelResult.orEmpty().length}")
            } catch (e: Exception) {
                Log.w(TAG, "AICore categorization unavailable, using fallback; ${e.toAicoreDiagnosticMessage()}")
            }

            runKeywordMatching(title, content, folders)
                ?: chooseRootFallback(folders)
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
        val folderPaths = buildFolderPaths(folders)
        val folderOptions = folders.joinToString(separator = "\n") { folder ->
            "- ${folder.id}: ${folderPaths.getValue(folder.id)}${folder.descriptionPromptSuffix()}"
        }
        return """
            Choose the best folder for this note.
            Reply only with exactly one folder id from the Folders list.
            Do not include prose, markdown, JSON, punctuation, or explanation.
            Use the full folder path to understand subfolders.
            If no exact folder or subfolder fits, choose the closest existing root folder.
            Do not invent a folder id.

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
        if (normalizedResult.isBlank()) {
            return null
        }

        val folderPaths = buildFolderPaths(folders)
        val idMatch = folders.firstOrNull { folder ->
            normalizedResult.containsExactToken(folder.id)
        }
        val pathMatch = folders.firstOrNull { folder ->
            normalizedResult.equals(folderPaths.getValue(folder.id), ignoreCase = true)
        }
        val nameMatch = folders.firstOrNull { folder ->
            normalizedResult.equals(folder.name, ignoreCase = true)
        }

        return idMatch ?: pathMatch ?: nameMatch
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

    private fun Folder.descriptionPromptSuffix(): String {
        val trimmedDescription = description.trim()
        return if (trimmedDescription.isBlank()) {
            ""
        } else {
            " | Description: ${trimmedDescription.take(MAX_FOLDER_DESCRIPTION_PROMPT_LENGTH)}"
        }
    }

    private fun chooseRootFallback(folders: List<Folder>): Folder? = folders
        .filter { folder -> folder.parentFolderId == null }
        .sortedWith(
            compareBy<Folder> { folder -> folder.sortKey }
                .thenBy { folder -> folder.name }
                .thenBy { folder -> folder.id }
        )
        .firstOrNull()

    private companion object {
        const val TAG = "NotesApp/GeminiNanoFolderCategorizer"
        const val MAX_PROMPT_CONTENT_LENGTH = 2_000
        const val MAX_FOLDER_DESCRIPTION_PROMPT_LENGTH = 300
        const val FOLDER_PATH_SEPARATOR = " / "
    }
}

private fun String.containsExactToken(token: String): Boolean {
    val escapedToken = Regex.escape(token)
    return Regex("(^|[^A-Za-z0-9_-])$escapedToken($|[^A-Za-z0-9_-])", RegexOption.IGNORE_CASE)
        .containsMatchIn(this)
}
