package com.example.notesapp.data.emoji

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.notesapp.domain.emoji.repository.RecentEmojiRepository
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DataStoreRecentEmojiRepositoryTest {
    private lateinit var dataStoreFile: File
    private lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
        dataStoreFile = Files.createTempDirectory("notesapp-emoji").toFile()
            .resolve("emoji_recent.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create { dataStoreFile }
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        dataStoreFile.parentFile?.deleteRecursively()
    }

    @Test
    fun persistsExactUnicodeMruAcrossRepositoryRecreation() = runTest {
        val repository = DataStoreRecentEmojiRepository(dataStore)

        repository.recordSelectedEmoji("😀")
        repository.recordSelectedEmoji("👍🏽")
        repository.recordSelectedEmoji("😀")

        val recreatedRepository: RecentEmojiRepository = DataStoreRecentEmojiRepository(dataStore)

        assertEquals(listOf("😀", "👍🏽"), recreatedRepository.recentEmoji.first())
    }

    @Test
    fun boundsRecentValuesAndKeepsLatestSelectionFirst() = runTest {
        val repository = DataStoreRecentEmojiRepository(dataStore)

        repeat(25) { index -> repository.recordSelectedEmoji("emoji-$index") }
        repository.recordSelectedEmoji("emoji-4")

        val recent = repository.recentEmoji.first()

        assertEquals(20, recent.size)
        assertEquals("emoji-4", recent.first())
        assertTrue("emoji-0" !in recent)
        assertEquals(1, recent.count { it == "emoji-4" })
    }

    @Test
    fun emptySelectionDoesNotCreateRecentEntry() = runTest {
        val repository = DataStoreRecentEmojiRepository(dataStore)

        repository.recordSelectedEmoji("")

        assertEquals(emptyList<String>(), repository.recentEmoji.first())
    }

    @Test
    fun corruptPreferenceFallsBackToEmptyRecent() = runTest {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("recent_emoji_json")] = "not-json"
        }

        val repository = DataStoreRecentEmojiRepository(dataStore)

        assertEquals(emptyList<String>(), repository.recentEmoji.first())
    }

    @Test
    fun readFailureFallsBackToEmptyRecent() = runTest {
        val repository = DataStoreRecentEmojiRepository(FailingDataStore())

        assertEquals(emptyList<String>(), repository.recentEmoji.first())
    }

    private class FailingDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow {
            throw IOException("preferences unavailable")
        }

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            throw IOException("preferences unavailable")
        }
    }
}
