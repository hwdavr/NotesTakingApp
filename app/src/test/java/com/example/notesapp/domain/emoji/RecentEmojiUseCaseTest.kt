package com.example.notesapp.domain.emoji

import com.example.notesapp.domain.emoji.repository.RecentEmojiRepository
import com.example.notesapp.domain.emoji.usecase.ObserveRecentEmojiUseCase
import com.example.notesapp.domain.emoji.usecase.RecordRecentEmojiUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentEmojiUseCaseTest {
    @Test
    fun observeUseCaseReturnsRepositoryRecentFlow() = runTest {
        val repository = FakeRecentEmojiRepository(flowOf(listOf("😀", "👍🏽")))

        assertEquals(
            listOf("😀", "👍🏽"),
            ObserveRecentEmojiUseCase(repository)().first()
        )
    }

    @Test
    fun recordUseCaseForwardsExactUnicodeSequence() = runTest {
        val repository = FakeRecentEmojiRepository(flowOf(emptyList()))

        RecordRecentEmojiUseCase(repository)("👍🏽")

        assertEquals("👍🏽", repository.recordedEmoji)
    }

    private class FakeRecentEmojiRepository(
        override val recentEmoji: Flow<List<String>>
    ) : RecentEmojiRepository {
        var recordedEmoji: String? = null

        override suspend fun recordSelectedEmoji(emoji: String) {
            recordedEmoji = emoji
        }
    }
}
