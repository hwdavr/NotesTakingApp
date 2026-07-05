package com.example.notesapp.domain.folder

interface FolderCategorizer {
    suspend fun categorize(title: String, content: String, folders: List<Folder>): Folder?
}
