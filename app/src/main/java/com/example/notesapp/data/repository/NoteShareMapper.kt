package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteShareEntity
import com.example.notesapp.data.remote.NoteShareDto
import com.example.notesapp.domain.share.NoteShare
import com.example.notesapp.domain.share.NoteShareAccessRole
import com.example.notesapp.domain.share.NoteShareStatus
import java.time.Instant

fun NoteShareEntity.toDomain(): NoteShare = NoteShare(
    id = id,
    noteId = noteId,
    userId = userId,
    email = email,
    displayName = displayName,
    accessRole = accessRole.toDomainAccessRole(),
    status = status.toDomainStatus(),
    invitedByUserId = invitedByUserId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun NoteShareDto.toEntity(): NoteShareEntity = NoteShareEntity(
    id = id,
    noteId = noteId,
    userId = userId,
    email = email,
    displayName = displayName,
    accessRole = accessRole,
    status = status,
    invitedByUserId = invitedByUserId,
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    updatedAt = Instant.parse(updatedAt).toEpochMilli()
)

fun NoteShareAccessRole.toApiValue(): String = when (this) {
    NoteShareAccessRole.READ_ONLY -> "read_only"
    NoteShareAccessRole.FULL_ACCESS -> "full_access"
}

private fun String.toDomainAccessRole(): NoteShareAccessRole = when (this) {
    "full_access" -> NoteShareAccessRole.FULL_ACCESS
    else -> NoteShareAccessRole.READ_ONLY
}

private fun String.toDomainStatus(): NoteShareStatus = when (this) {
    "active" -> NoteShareStatus.ACTIVE
    else -> NoteShareStatus.PENDING
}
