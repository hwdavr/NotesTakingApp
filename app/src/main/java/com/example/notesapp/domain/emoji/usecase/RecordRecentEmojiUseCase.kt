package com.example.notesapp.domain.emoji.usecase

import com.example.notesapp.domain.emoji.repository.RecentEmojiRepository
import javax.inject.Inject

class RecordRecentEmojiUseCase @Inject constructor(
    private val repository: RecentEmojiRepository
) {
    suspend operator fun invoke(emoji: String) {
        repository.recordSelectedEmoji(emoji)
    }
}
