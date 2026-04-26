package com.example.notesapp.data.repository

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.remote.CreateFolderRequest
import com.example.notesapp.data.sync.ItemsSyncCoordinator
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.util.DeviceIdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao,
    private val api: NotesApiService,
    private val syncCoordinator: ItemsSyncCoordinator,
    private val deviceIdProvider: DeviceIdProvider
) : FolderRepository {

    override fun getFolders(): Flow<List<Folder>> =
        folderDao.getFolders().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(folder: Folder) {
        val folderId = folder.id.ifBlank { "folder_${UUID.randomUUID()}" }
        try {
            api.createFolder(
                CreateFolderRequest(
                    id = folderId,
                    parentId = folder.parentFolderId,
                    name = folder.name,
                    sortKey = folder.sortKey.ifBlank { System.currentTimeMillis().toString() },
                    deviceId = deviceIdProvider.deviceId
                )
            )
            syncCoordinator.syncAll()
        } catch (_: Exception) {
            folderDao.insert(
                folder.copy(
                    id = folderId,
                    sortKey = folder.sortKey.ifBlank { System.currentTimeMillis().toString() },
                    version = folder.version + 1,
                    deviceId = deviceIdProvider.deviceId
                ).toEntity()
            )
        }
    }

    override suspend fun sync() {
        try {
            syncCoordinator.syncAll()
        } catch (_: Exception) {
        }
    }
}
