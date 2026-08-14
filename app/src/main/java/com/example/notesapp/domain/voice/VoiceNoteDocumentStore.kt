package com.example.notesapp.domain.voice

data class VoiceNoteDocumentInsertion(
    val block: VoiceNoteBlock,
    val transcript: String,
    val focusedBlockId: String?
)

interface VoiceNoteDocumentStore {
    fun insertVoiceNote(content: String, insertion: VoiceNoteDocumentInsertion): String

    fun updateAudioFilePath(content: String, blockId: String, audioFilePath: String?): String
}
