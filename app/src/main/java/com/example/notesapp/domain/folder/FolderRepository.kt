package com.example.notesapp.domain.folder

import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getFolders(): Flow<List<Folder>>
    fun getFolder(folderId: String): Flow<Folder?>
    fun getArchivedFolders(): Flow<List<Folder>>
    suspend fun getArchivedFolderCount(): Int
    suspend fun insert(folder: Folder)
    suspend fun update(folder: Folder)
    suspend fun updateDescription(folder: Folder, description: String)
    suspend fun move(folder: Folder, parentFolderId: String?)
    suspend fun delete(folder: Folder)
    suspend fun toggleFavorite(folder: Folder)
    suspend fun sync()
}
