package com.example.notesapp.data.repository

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.FolderEntity
import kotlinx.coroutines.flow.Flow

class FolderRepository(private val folderDao: FolderDao) {
    fun getFolders(): Flow<List<FolderEntity>> = folderDao.getFolders()

    suspend fun insert(folder: FolderEntity): Long = folderDao.insert(folder)
}
