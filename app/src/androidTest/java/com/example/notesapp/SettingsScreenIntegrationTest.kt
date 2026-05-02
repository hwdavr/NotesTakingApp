package com.example.notesapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.ui.settings.SettingsScreen
import com.example.notesapp.ui.settings.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class SettingsScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val authManager = mockk<AuthManager>(relaxed = true)
    private val isLoggedIn = MutableStateFlow(true)
    private val profileEmail = MutableStateFlow<String?>("user@example.com")
    private val viewModel by lazy {
        every { authManager.isLoggedIn } returns isLoggedIn
        every { authManager.profileEmail } returns profileEmail
        SettingsViewModel(authManager)
    }

    // Screen Object abstraction
    private val settingsScreen = object {
        val logoutButton = composeRule.onNodeWithText("Logout")
        
        fun clickLogout() {
            logoutButton.performClick()
        }
    }

    @Test
    fun clickingLogout_triggersAuthManagerLogoutAndCallback() {
        var logoutCallbackCalled = false

        // Given: SettingsScreen is rendered with a mocked AuthManager
        step("Prepare SettingsScreen") {
            composeRule.setContent {
                SettingsScreen(
                    parentPadding = PaddingValues(0.dp),
                    onLogoutSuccess = { logoutCallbackCalled = true },
                    viewModel = viewModel
                )
            }
        }

        // When: Logout button is clicked
        step("Click Logout") {
            every { 
                authManager.logout(any(), any(), any()) 
            } answers {
                val onSuccess = secondArg<() -> Unit>()
                onSuccess()
            }

            settingsScreen.clickLogout()
        }

        // Then: AuthManager.logout is called and UI callback is triggered
        step("Verify logout success") {
            verify { authManager.logout(any(), any(), any()) }
            assertTrue("Logout callback should have been called", logoutCallbackCalled)
        }
    }

    private fun step(description: String, action: () -> Unit) {
        // Simple wrapper for business-readable test steps
        action()
    }
}
