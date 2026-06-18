package com.example.notesapp.domain.share

enum class NoteShareAccessRole {
    VIEWER,
    EDITOR
}
enum class NoteShareStatus {
    PENDING,
    ACTIVE
}
data class NoteShare(
    val id: String,
    val noteId: String,
    val userId: String?,
    val email: String,
    val displayName: String?,
    val accessRole: NoteShareAccessRole,
    val status: NoteShareStatus,
    val invitedByUserId: String,
    val createdAt: Long,
    val updatedAt: Long
)

sealed class NoteShareException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class DuplicateShareException(message: String, cause: Throwable? = null) : NoteShareException(message, cause)
    class GenericShareException(message: String, cause: Throwable? = null) : NoteShareException(message, cause)
}
