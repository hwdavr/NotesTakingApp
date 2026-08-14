package com.example.notesapp.data.repository

import com.example.notesapp.data.local.VoiceNoteBlockDao
import com.example.notesapp.data.voice.AudioFileSystem
import com.example.notesapp.domain.voice.VoiceNoteBlock
import com.example.notesapp.domain.voice.VoiceNoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceNoteRepositoryImpl @Inject constructor(
    private val voiceNoteBlockDao: VoiceNoteBlockDao,
    private val audioFileSystem: AudioFileSystem
) : VoiceNoteRepository {
    override suspend fun getForNote(noteId: String): List<VoiceNoteBlock> =
        voiceNoteBlockDao.getForNote(noteId).map { it.toDomain() }

    override suspend fun getByBlockId(blockId: String): VoiceNoteBlock? =
        voiceNoteBlockDao.getByBlockId(blockId)?.toDomain()

    override suspend fun upsert(block: VoiceNoteBlock) {
        voiceNoteBlockDao.upsert(block.toEntity())
    }

    override suspend fun deleteAudioOnly(blockId: String): VoiceNoteBlock? {
        val block = voiceNoteBlockDao.getByBlockId(blockId)?.toDomain() ?: return null
        block.audioFilePath?.let(audioFileSystem::delete)
        val updated = block.copy(
            audioFilePath = null,
            updatedAt = System.currentTimeMillis()
        )
        voiceNoteBlockDao.update(updated.toEntity())
        return updated
    }

    override suspend fun deleteBlock(blockId: String) {
        val block = voiceNoteBlockDao.getByBlockId(blockId)?.toDomain()
        block?.audioFilePath?.let(audioFileSystem::delete)
        voiceNoteBlockDao.delete(blockId)
    }

    override suspend fun deleteForNote(noteId: String) {
        voiceNoteBlockDao.getForNote(noteId)
            .map { it.toDomain() }
            .forEach { block -> block.audioFilePath?.let(audioFileSystem::delete) }
        voiceNoteBlockDao.deleteForNote(noteId)
    }
}
