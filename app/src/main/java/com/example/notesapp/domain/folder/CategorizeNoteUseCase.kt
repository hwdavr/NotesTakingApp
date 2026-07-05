package com.example.notesapp.domain.folder

import javax.inject.Inject

class CategorizeNoteUseCase @Inject constructor(
    private val folderCategorizer: FolderCategorizer
) {
    suspend operator fun invoke(title: String, content: String, folders: List<Folder>): Folder? {
        if (folders.isEmpty()) return null
        val normalizedTitle = title.trim()
        val normalizedContent = content.trim()
        // If both are empty, do not run categorization
        if (normalizedTitle.isEmpty() && normalizedContent.isEmpty()) return null
        return folderCategorizer.categorize(normalizedTitle, normalizedContent, folders)
    }
}
