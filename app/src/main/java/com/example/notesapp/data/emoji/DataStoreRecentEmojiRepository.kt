package com.example.notesapp.data.emoji

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.notesapp.domain.emoji.repository.RecentEmojiRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray

@Singleton
class DataStoreRecentEmojiRepository @Inject constructor(
    @Named("emojiRecentDataStore")
    private val dataStore: DataStore<Preferences>
) : RecentEmojiRepository {
    override val recentEmoji: Flow<List<String>> = dataStore.data
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            Log.w(TAG, "Unable to read recent emoji preferences", throwable)
            emit(emptyPreferences())
        }
        .map { preferences -> decode(preferences[RECENT_EMOJI_KEY]) }

    override suspend fun recordSelectedEmoji(emoji: String) {
        if (emoji.isEmpty()) return

        dataStore.edit { preferences ->
            val current = decode(preferences[RECENT_EMOJI_KEY])
            val updated = buildList {
                add(emoji)
                current.filter { it != emoji }.take(MAX_RECENT_EMOJI_COUNT - 1).forEach(::add)
            }
            preferences[RECENT_EMOJI_KEY] = encode(updated)
        }
    }

    private companion object {
        const val TAG = "NotesApp/RecentEmojiRepository"
        const val MAX_RECENT_EMOJI_COUNT = 20
        val RECENT_EMOJI_KEY = stringPreferencesKey("recent_emoji_json")

        fun encode(emoji: List<String>): String = JSONArray().apply {
            emoji.forEach(::put)
        }.toString()

        fun decode(serialized: String?): List<String> {
            if (serialized.isNullOrBlank()) return emptyList()

            return runCatching {
                val array = JSONArray(serialized)
                buildList(array.length()) {
                    repeat(array.length()) { index ->
                        array.optString(index).takeIf(String::isNotEmpty)?.let(::add)
                    }
                }
            }.getOrDefault(emptyList())
        }
    }
}
