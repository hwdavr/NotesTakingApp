package com.example.notesapp.ui.settings.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.ui.settings.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenIntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()
    private class FakeAuthManager : AuthManager(
        context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext,
        tokenStorage = object : com.example.notesapp.auth.TokenStorage(
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        ) {
            override fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) {}
            override fun getAccessToken(): String? = null
            override fun getRefreshToken(): String? = null
            override fun getIdToken(): String? = null
            override fun clearTokens() {}
        }
    ) {
        override val isLoggedIn = MutableStateFlow(true)
        override val profileEmail = MutableStateFlow<String?>("user@example.com")
        var logoutCalled = false
        override fun logout(
            activityContext: android.content.Context,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
        ) {
            logoutCalled = true
            onSuccess()
        }
    }
    private val authManager = FakeAuthManager()
    private val viewModel = SettingsViewModel(authManager)

    // Screen Object abstraction
    private val settingsScreen = object {
        fun clickLogout() {
            composeRule.waitUntil(10000) {
                composeRule.onAllNodesWithText("Logout").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Logout").performClick()
        }
    }

    @Test
    fun clickingLogout_triggersAuthManagerLogoutAndCallback() {
        var logoutCallbackCalled = false
        // Given: SettingsScreen is rendered with a FakeAuthManager
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
            settingsScreen.clickLogout()
        }
        // Then: AuthManager.logout is called and UI callback is triggered
        step("Verify logout success") {
            assertTrue("AuthManager.logout should have been called", authManager.logoutCalled)
            assertTrue("Logout callback should have been called", logoutCallbackCalled)
        }
    }
    private fun step(description: String, action: () -> Unit) {
        action()
    }
}
