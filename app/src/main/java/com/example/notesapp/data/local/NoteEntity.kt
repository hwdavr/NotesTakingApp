package com.example.notesapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId")]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val folderId: String? = null,
    val sortKey: String,
    val version: Long,
    val deviceId: String,
    val lastSyncedVersion: Long,
    val deletedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
