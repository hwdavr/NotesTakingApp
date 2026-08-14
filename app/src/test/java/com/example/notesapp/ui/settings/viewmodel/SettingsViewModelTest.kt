package com.example.notesapp.ui.settings.viewmodel

import android.content.Context
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.voice.AudioFormat
import com.example.notesapp.domain.voice.VoiceSettingsRepository
import com.example.notesapp.domain.voice.VoiceStorageUsage
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest : BaseViewModelTest() {
    private val authManager: AuthManager = mockk(relaxed = true)
    private val isLoggedIn = MutableStateFlow(false)
    private val profileEmail = MutableStateFlow<String?>(null)
    private val selectedAudioFormat = MutableStateFlow(AudioFormat.AAC)
    private val voiceStorageUsage = MutableStateFlow(VoiceStorageUsage())
    private val voiceSettingsRepository: VoiceSettingsRepository = mockk(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        every { authManager.isLoggedIn } returns isLoggedIn
        every { authManager.profileEmail } returns profileEmail
        every { voiceSettingsRepository.selectedAudioFormat } returns selectedAudioFormat
        every { voiceSettingsRepository.storageUsage } returns voiceStorageUsage
        viewModel = SettingsViewModel(authManager, voiceSettingsRepository)
    }

    @Test
    fun `logout calls authManager logout`() {
        val context: Context = mockk()
        val onSuccess: () -> Unit = {}
        val onError: (String) -> Unit = {}
        viewModel.logout(context, onSuccess, onError)
        verify { authManager.logout(context, any(), any()) }
    }

    @Test
    fun `profile title defaults to Guest when logged out`() {
        isLoggedIn.value = false
        profileEmail.value = "user@example.com"
        assertEquals("Guest", viewModel.uiState.value.profileTitle)
    }

    @Test
    fun `profile title uses email when logged in`() {
        isLoggedIn.value = true
        profileEmail.value = "user@example.com"
        assertEquals("user@example.com", viewModel.uiState.value.profileTitle)
    }

    @Test
    fun `selecting opus persists the future recording format`() = runTest {
        viewModel.selectVoiceAudioFormat(SettingsAudioFormat.OPUS)
        advanceUntilIdle()

        coVerify { voiceSettingsRepository.setAudioFormat(AudioFormat.OPUS) }
    }
}
