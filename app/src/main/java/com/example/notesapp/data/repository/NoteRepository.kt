package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.remote.CreateNoteRequest
import com.example.notesapp.data.remote.DeleteItemRequest
import com.example.notesapp.data.remote.MoveItemRequest
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.remote.RenameItemRequest
import com.example.notesapp.data.remote.UpdateNoteContentRequest
import com.example.notesapp.data.sync.ItemsSyncCoordinator
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.note.NoteRepository
import com.example.notesapp.util.DeviceIdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val api: NotesApiService,
    private val syncCoordinator: ItemsSyncCoordinator,
    private val deviceIdProvider: DeviceIdProvider
) : NoteRepository {

    override fun getActiveNotes(): Flow<List<Note>> =
        noteDao.getActiveNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun getNoteById(id: String): Note? =
        noteDao.getNoteById(id)?.toDomain()

    override suspend fun getActiveNoteCount(): Int = noteDao.getActiveNoteCount()

    override suspend fun getActiveNoteCountForFolder(folderId: String): Int =
        noteDao.getActiveNoteCountForFolder(folderId)

    override suspend fun save(note: Note) {
        val existing = note.id.takeIf { it.isNotBlank() }?.let { noteDao.getNoteById(it)?.toDomain() }
        val deviceId = deviceIdProvider.deviceId
        val noteId = note.id.ifBlank { "note_${UUID.randomUUID()}" }

        try {
            if (existing == null) {
                api.createNote(
                    CreateNoteRequest(
                        id = noteId,
                        parentId = note.folderId,
                        name = note.title,
                        content = note.content,
                        sortKey = note.sortKey.ifBlank { defaultSortKey() },
                        deviceId = deviceId
                    )
                )
            } else {
                if (existing.title != note.title) {
                    api.renameItem(
                        note.id,
                        RenameItemRequest(
                            name = note.title,
                            deviceId = deviceId,
                            lastSyncedVersion = existing.version
                        )
                    )
                }
                if (existing.content != note.content) {
                    api.updateNoteContent(
                        note.id,
                        UpdateNoteContentRequest(
                            content = note.content,
                            deviceId = deviceId,
                            lastSyncedVersion = existing.version
                        )
                    )
                }
                if (existing.folderId != note.folderId) {
                    api.moveItem(
                        note.id,
                        MoveItemRequest(
                            parentId = note.folderId,
                            deviceId = deviceId,
                            lastSyncedVersion = existing.version
                        )
                    )
                }
            }
            syncCoordinator.syncAll()
        } catch (_: Exception) {
            val fallback = note.copy(
                id = noteId,
                sortKey = note.sortKey.ifBlank { defaultSortKey() },
                version = (existing?.version ?: 0) + 1,
                deviceId = deviceId,
                lastSyncedVersion = existing?.version ?: 0
            )
            noteDao.insert(fallback.toEntity())
        }
    }

    override suspend fun delete(note: Note) {
        try {
            api.deleteItem(
                note.id,
                DeleteItemRequest(
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = note.version
                )
            )
            syncCoordinator.syncAll()
        } catch (_: Exception) {
            noteDao.insert(
                note.copy(
                    version = note.version + 1,
                    deviceId = deviceIdProvider.deviceId,
                    lastSyncedVersion = note.version,
                    deletedAt = System.currentTimeMillis()
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

    private fun defaultSortKey(): String = System.currentTimeMillis().toString()
}
