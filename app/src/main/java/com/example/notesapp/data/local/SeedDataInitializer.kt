package com.example.notesapp.data.local

class SeedDataInitializer(
    private val folderDao: FolderDao,
    private val noteDao: NoteDao
) {
    suspend fun seedIfNeeded() {
        if (folderDao.getFolderCount() > 0 || noteDao.getNoteCount() > 0) return

        val now = System.currentTimeMillis()

        val personalId = folderDao.insert(
            FolderEntity(name = "Personal", createdAt = now)
        )
        val workId = folderDao.insert(
            FolderEntity(name = "Work", createdAt = now)
        )
        val ideasId = folderDao.insert(
            FolderEntity(name = "Ideas", createdAt = now)
        )

        noteDao.insert(
            NoteEntity(
                title = "Trip planning",
                content = "Book flights, shortlist stays, and prepare itinerary notes.",
                folderId = personalId,
                createdAt = now,
                updatedAt = now
            )
        )
        noteDao.insert(
            NoteEntity(
                title = "Sprint tasks",
                content = "Polish Android UI, add folder tree, and connect note editor.",
                folderId = workId,
                isFavorite = true,
                createdAt = now,
                updatedAt = now
            )
        )
        noteDao.insert(
            NoteEntity(
                title = "Startup ideas",
                content = "Offline-first note app with nested folders and calm pastel UI.",
                folderId = ideasId,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
