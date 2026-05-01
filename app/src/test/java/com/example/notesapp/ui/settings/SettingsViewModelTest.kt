package com.example.notesapp.ui.settings

import android.content.Context
import com.example.notesapp.auth.AuthManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private val authManager: AuthManager = mockk(relaxed = true)
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
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
}
