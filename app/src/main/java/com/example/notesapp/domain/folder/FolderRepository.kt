package com.example.notesapp.domain.folder

import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getFolders(): Flow<List<Folder>>
    suspend fun insert(folder: Folder): Long
}
