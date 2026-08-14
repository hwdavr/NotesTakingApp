package com.example.notesapp.domain.voice

interface VoiceNoteRepository {
    suspend fun getForNote(noteId: String): List<VoiceNoteBlock>

    suspend fun getByBlockId(blockId: String): VoiceNoteBlock?

    suspend fun upsert(block: VoiceNoteBlock)

    suspend fun deleteAudioOnly(blockId: String): VoiceNoteBlock?

    suspend fun deleteBlock(blockId: String)

    suspend fun deleteForNote(noteId: String)
}
