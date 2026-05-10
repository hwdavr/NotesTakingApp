package com.example.notesapp.data.remote

data class ApiItem(
    val id: String,
    val userId: String,
    val type: String,
    val parentId: String?,
    val name: String,
    val content: String,
    val sortKey: String,
    val version: Long,
    val deviceId: String,
    val lastSyncedVersion: Long,
    val deletedAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val isFavorite: Boolean = false,
    val isShared: Boolean = false
)
data class MutationResultDto(
    val status: String,
    val item: ApiItem,
    val conflictFields: List<String>? = null,
    val message: String? = null
)
data class CreateFolderRequest(
    val id: String?,
    val parentId: String?,
    val name: String,
    val sortKey: String,
    val deviceId: String
)
data class CreateNoteRequest(
    val id: String?,
    val parentId: String?,
    val name: String,
    val content: String,
    val sortKey: String,
    val deviceId: String
)
data class RenameItemRequest(
    val name: String,
    val deviceId: String,
    val lastSyncedVersion: Long
)
data class MoveItemRequest(
    val parentId: String?,
    val deviceId: String,
    val lastSyncedVersion: Long
)
data class ReorderItemRequest(
    val sortKey: String,
    val deviceId: String,
    val lastSyncedVersion: Long
)
data class UpdateNoteContentRequest(
    val content: String,
    val deviceId: String,
    val lastSyncedVersion: Long
)
data class DeleteItemRequest(
    val deviceId: String,
    val lastSyncedVersion: Long
)
data class FavoriteItemRequest(
    val isFavorite: Boolean,
    val deviceId: String,
    val lastSyncedVersion: Long
)
data class NoteShareDto(
    val id: String,
    val noteId: String,
    val userId: String?,
    val email: String,
    val displayName: String?,
    val accessRole: String,
    val status: String,
    val invitedByUserId: String,
    val createdAt: String,
    val updatedAt: String
)
data class CreateNoteShareRequest(
    val email: String,
    val accessRole: String
)
data class UpdateNoteShareRequest(
    val accessRole: String
)
