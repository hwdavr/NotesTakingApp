package com.example.notesapp.domain.voice.usecase

import com.example.notesapp.domain.voice.VoiceNoteRepository
import javax.inject.Inject

class DeleteVoiceNoteBlockUseCase @Inject constructor(
    private val voiceNoteRepository: VoiceNoteRepository
) {
    suspend operator fun invoke(blockId: String) {
        voiceNoteRepository.deleteBlock(blockId)
    }
}
