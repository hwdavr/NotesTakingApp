package com.example.notesapp.domain.emoji.repository

import kotlinx.coroutines.flow.Flow

interface RecentEmojiRepository {
    val recentEmoji: Flow<List<String>>

    suspend fun recordSelectedEmoji(emoji: String)
}
