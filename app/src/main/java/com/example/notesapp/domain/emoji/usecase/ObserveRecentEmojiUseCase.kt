package com.example.notesapp.domain.emoji.usecase

import com.example.notesapp.domain.emoji.repository.RecentEmojiRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveRecentEmojiUseCase @Inject constructor(
    private val repository: RecentEmojiRepository
) {
    operator fun invoke(): Flow<List<String>> = repository.recentEmoji
}
