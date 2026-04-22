package com.example.notesapp.data.repository

import com.example.notesapp.data.local.NoteEntity
import com.example.notesapp.domain.note.Note

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    folderId = folderId,
    isFavorite = isFavorite,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    folderId = folderId,
    isFavorite = isFavorite,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)
