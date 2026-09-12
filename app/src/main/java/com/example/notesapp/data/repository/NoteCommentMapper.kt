package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteBlockCommentEntity
import com.example.notesapp.data.remote.ApiNoteBlockComment
import com.example.notesapp.domain.comment.model.NoteBlockComment
import java.time.Instant

fun NoteBlockCommentEntity.toDomain(): NoteBlockComment = NoteBlockComment(
    id = id,
    noteId = noteId,
    blockId = blockId,
    authorUserId = authorUserId,
    authorDisplayName = authorDisplayName,
    authorEmail = authorEmail,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ApiNoteBlockComment.toEntity(): NoteBlockCommentEntity = NoteBlockCommentEntity(
    id = id,
    noteId = noteId,
    blockId = blockId,
    authorUserId = authorUserId,
    authorDisplayName = authorDisplayName,
    authorEmail = authorEmail,
    body = body,
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    updatedAt = Instant.parse(updatedAt).toEpochMilli()
)

fun ApiNoteBlockComment.toDomain(): NoteBlockComment = NoteBlockComment(
    id = id,
    noteId = noteId,
    blockId = blockId,
    authorUserId = authorUserId,
    authorDisplayName = authorDisplayName,
    authorEmail = authorEmail,
    body = body,
    createdAt = Instant.parse(createdAt).toEpochMilli(),
    updatedAt = Instant.parse(updatedAt).toEpochMilli()
)
