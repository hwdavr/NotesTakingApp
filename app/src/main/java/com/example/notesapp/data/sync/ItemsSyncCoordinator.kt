package com.example.notesapp.data.sync

import com.example.notesapp.data.local.FolderDao
import com.example.notesapp.data.local.NoteDao
import com.example.notesapp.data.remote.NotesApiService
import com.example.notesapp.data.remote.toFolderEntity
import com.example.notesapp.data.remote.toNoteEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemsSyncCoordinator @Inject constructor(
    private val api: NotesApiService,
    private val folderDao: FolderDao,
    private val noteDao: NoteDao
) {
    suspend fun syncAll() {
        val items = api.listItems(includeDeleted = true)
        val folders = items.filter { it.type == "folder" }.map { it.toFolderEntity() }
        val notes = items.filter { it.type == "note" }.map { it.toNoteEntity() }

        folderDao.clearAll()
        noteDao.clearAll()
        folderDao.insertAll(folders)
        noteDao.insertAll(notes)
    }
}
