package com.example.notesapp.data.sync

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.FolderEntity
import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.remote.UpdateItemContentRequest
import com.example.notesapp.data.remote.UpdateNoteContentRequest
import com.example.notesapp.data.remote.toFolderEntity
import com.example.notesapp.data.remote.toNoteEntity
import com.example.notesapp.util.DeviceIdProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ItemsSyncCoordinator @Inject constructor(
    private val api: NotesApiService,
    private val folderDao: FolderDao,
    private val noteDao: NoteDao,
    private val deviceIdProvider: DeviceIdProvider
) {
    private val syncMutex = Mutex()

    suspend fun syncAll() = syncMutex.withLock {
        val initialItems = api.listItems(includeDeleted = true)
        var hasUpdates = false
        val retainedLocalFolders = mutableMapOf<String, FolderEntity>()
        for (apiItem in initialItems) {
            if (apiItem.type == "note") {
                val localNote = noteDao.getNoteById(apiItem.id)
                if (localNote != null && localNote.version > apiItem.version) {
                    try {
                        api.updateNoteContent(
                            localNote.id,
                            UpdateNoteContentRequest(
                                content = localNote.content,
                                deviceId = deviceIdProvider.deviceId,
                                lastSyncedVersion = apiItem.version
                            )
                        )
                        hasUpdates = true
                    } catch (e: CancellationException) {
                        throw e
                    } catch (ignored: Exception) {
                        // Keep local version for next sync if API call fails
                    }
                }
            } else if (apiItem.type == "folder") {
                val localFolder = folderDao.getFolderById(apiItem.id)
                if (localFolder != null && localFolder.version > apiItem.version) {
                    try {
                        api.updateItemContent(
                            localFolder.id,
                            UpdateItemContentRequest(
                                content = localFolder.description,
                                deviceId = deviceIdProvider.deviceId,
                                lastSyncedVersion = apiItem.version
                            )
                        )
                        hasUpdates = true
                    } catch (e: CancellationException) {
                        throw e
                    } catch (ignored: Exception) {
                        retainedLocalFolders[localFolder.id] = localFolder
                    }
                }
            }
        }
        val items = if (hasUpdates) api.listItems(includeDeleted = true) else initialItems
        val folders = items
            .filter { it.type == "folder" }
            .map { apiItem -> retainedLocalFolders[apiItem.id] ?: apiItem.toFolderEntity() }
        val notes = items.filter { it.type == "note" }.map { it.toNoteEntity() }
        folderDao.clearAll()
        noteDao.clearAll()
        folderDao.insertAll(folders)
        noteDao.insertAll(notes)
    }
}
