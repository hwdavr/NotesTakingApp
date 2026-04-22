package com.example.notesapp.data.repository

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    override fun getFolders(): Flow<List<Folder>> =
        folderDao.getFolders().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(folder: Folder): Long =
        folderDao.insert(folder.toEntity())
}
