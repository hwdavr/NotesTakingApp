package com.example.notesapp.ui.settings.viewmodel

import android.content.Context
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.base.BaseViewModelTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest : BaseViewModelTest() {
    private val authManager: AuthManager = mockk(relaxed = true)
    private val isLoggedIn = MutableStateFlow(false)
    private val profileEmail = MutableStateFlow<String?>(null)
    private lateinit var viewModel: SettingsViewModel
    @Before
    fun setup() {
        every { authManager.isLoggedIn } returns isLoggedIn
        every { authManager.profileEmail } returns profileEmail
        viewModel = SettingsViewModel(authManager)
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
}
