package com.example.notesapp.data.repository

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.remote.CreateFolderRequest
import com.example.notesapp.data.remote.DeleteItemRequest
import com.example.notesapp.data.remote.FavoriteItemRequest
import com.example.notesapp.data.remote.MoveItemRequest
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.remote.RenameItemRequest
import com.example.notesapp.data.sync.ItemsSyncCoordinator
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import com.example.notesapp.util.DeviceIdProvider
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao,
    private val api: NotesApiService,
    private val syncCoordinator: ItemsSyncCoordinator,
    private val deviceIdProvider: DeviceIdProvider
) : FolderRepository {
    override fun getFolders(): Flow<List<Folder>> = folderDao.getFolders().map { list -> list.map { it.toDomain() } }
    override fun getArchivedFolders(): Flow<List<Folder>> =
        folderDao.getArchivedFolders().map { list -> list.map { it.toDomain() } }
    override suspend fun getArchivedFolderCount(): Int = folderDao.getArchivedFolderCount()
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
    override suspend fun update(folder: Folder) {
        try {
            api.renameItem(
                folder.id,
                RenameItemRequest(
                    name = folder.name,
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = folder.version
                )
            )
            syncCoordinator.syncAll()
        } catch (_: Exception) {
            folderDao.insert(
                folder.copy(
                    version = folder.version + 1,
                    deviceId = deviceIdProvider.deviceId
                ).toEntity()
            )
        }
    }
    override suspend fun move(folder: Folder, parentFolderId: String?) {
        try {
            api.moveItem(
                folder.id,
                MoveItemRequest(
                    parentId = parentFolderId,
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = folder.version
                )
            )
            syncCoordinator.syncAll()
        } catch (_: Exception) {
            folderDao.insert(
                folder.copy(
                    parentFolderId = parentFolderId,
                    version = folder.version + 1,
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = folder.version,
                    updatedAt = System.currentTimeMillis()
                ).toEntity()
            )
        }
    }
    override suspend fun delete(folder: Folder) {
        try {
            api.deleteItem(
                folder.id,
                DeleteItemRequest(
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = folder.version
                )
            )
            syncCoordinator.syncAll()
        } catch (_: Exception) {
            folderDao.insert(
                folder.copy(
                    version = folder.version + 1,
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = folder.version,
                    deletedAt = System.currentTimeMillis()
                ).toEntity()
            )
        }
    }
    override suspend fun toggleFavorite(folder: Folder) {
        val newFavoriteStatus = !folder.isFavorite
        try {
            api.favoriteItem(
                folder.id,
                FavoriteItemRequest(
                    isFavorite = newFavoriteStatus,
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = folder.version
                )
            )
            syncCoordinator.syncAll()
        } catch (_: Exception) {
            folderDao.insert(
                folder.copy(
                    isFavorite = newFavoriteStatus,
                    version = folder.version + 1,
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = folder.version,
                    updatedAt = System.currentTimeMillis()
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
