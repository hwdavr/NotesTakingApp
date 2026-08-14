package com.example.notesapp.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.notesapp.data.local.VoiceNoteBlockDao
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.VoiceSettingsRepository
import com.example.notesapp.domain.voice.VoiceStorageUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.voiceSettingsDataStore by preferencesDataStore(name = "voice_settings")

@Singleton
class VoiceSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voiceNoteBlockDao: VoiceNoteBlockDao
) : VoiceSettingsRepository {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val selectedFormat = MutableStateFlow(AudioFormat.AAC)

    override val selectedAudioFormat: StateFlow<AudioFormat> = selectedFormat

    override val storageUsage: Flow<VoiceStorageUsage> = combine(
        voiceNoteBlockDao.observeTotalAudioBytes(),
        voiceNoteBlockDao.observeAudioRecordingCount()
    ) { totalBytes, recordingCount ->
        VoiceStorageUsage(totalBytes = totalBytes, recordingCount = recordingCount)
    }

    init {
        repositoryScope.launch {
            context.voiceSettingsDataStore.data
                .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
                .map { preferences ->
                    AudioFormat.fromStorageValue(
                        preferences[VOICE_FORMAT_KEY].orEmpty()
                    )
                }
                .collect { selectedFormat.value = it }
        }
    }

    override suspend fun setAudioFormat(format: AudioFormat) {
        selectedFormat.value = format
        context.voiceSettingsDataStore.edit { preferences ->
            preferences[VOICE_FORMAT_KEY] = format.storageValue
        }
    }

    companion object {
        private val VOICE_FORMAT_KEY = stringPreferencesKey("voice_audio_format")
    }
}
