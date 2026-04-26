package com.example.notesapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentFolderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentFolderId")]
)
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentFolderId: String? = null,
    val sortKey: String,
    val version: Long,
    val deviceId: String,
    val lastSyncedVersion: Long,
    val deletedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
