package com.example.notesapp.ui.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.VoiceSettingsRepository
import com.example.notesapp.domain.voice.VoiceStorageUsage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val profileTitle: String = "Guest",
    val voiceAudioFormat: SettingsAudioFormat = SettingsAudioFormat.AAC,
    val voiceStorage: VoiceStorageUiState = VoiceStorageUiState()
)

enum class SettingsAudioFormat {
    AAC,
    OPUS;

    fun toDomain(): AudioFormat = when (this) {
        AAC -> AudioFormat.AAC
        OPUS -> AudioFormat.OPUS
    }

    companion object {
        fun fromDomain(format: AudioFormat): SettingsAudioFormat = when (format) {
            AudioFormat.AAC -> AAC
            AudioFormat.OPUS -> OPUS
        }
    }
}

data class VoiceStorageUiState(
    val totalBytes: Long = 0L,
    val recordingCount: Int = 0
)

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val voiceSettingsRepository: VoiceSettingsRepository
) : ViewModel() {
    open val uiState: StateFlow<SettingsUiState> = combine(
        authManager.isLoggedIn,
        authManager.profileEmail,
        voiceSettingsRepository.selectedAudioFormat,
        voiceSettingsRepository.storageUsage
    ) { isLoggedIn, email, audioFormat, storageUsage ->
        SettingsUiState(
            profileTitle = if (isLoggedIn) email?.takeIf { it.isNotBlank() } ?: "Guest" else "Guest",
            voiceAudioFormat = SettingsAudioFormat.fromDomain(audioFormat),
            voiceStorage = storageUsage.toUiState()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState()
    )
    fun logout(activityContext: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authManager.logout(activityContext, onSuccess, onError)
    }

    fun selectVoiceAudioFormat(format: SettingsAudioFormat) {
        viewModelScope.launch {
            voiceSettingsRepository.setAudioFormat(format.toDomain())
        }
    }

    private fun VoiceStorageUsage.toUiState(): VoiceStorageUiState = VoiceStorageUiState(
        totalBytes = totalBytes,
        recordingCount = recordingCount
    )
}
