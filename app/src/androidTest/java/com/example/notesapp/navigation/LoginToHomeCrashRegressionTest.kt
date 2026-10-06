package com.example.notesapp.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.HiltTestActivity
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.auth.TokenStorage
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class LoginToHomeCrashRegressionTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    private val authManager = FakeLoginAuthManager()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun successfulLoginNavigatesToHomeWithoutCrashing() {
        composeRule.setContent {
            NotesTakingAppTheme {
                val navController = rememberNavController()
                val isLoggedIn by authManager.isLoggedIn.collectAsState()
                AppNavigationHost(
                    navController = navController,
                    innerPadding = PaddingValues(0.dp),
                    isLoggedIn = isLoggedIn,
                    onLogin = { onSuccess, _ ->
                        authManager.completeLogin()
                        onSuccess()
                    },
                    onAuthError = {}
                )
            }
        }

        composeRule.onNodeWithTag("onboarding_login_text").performClick()
        composeRule.waitUntil(LOGIN_NAVIGATION_TIMEOUT_MS) {
            composeRule.onAllNodesWithTag("home_add_fab").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("home_add_fab").assertIsDisplayed()
    }

    private class FakeLoginAuthManager : AuthManager(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        tokenStorage = object : TokenStorage(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) = Unit

            override fun getAccessToken(): String? = null

            override fun getRefreshToken(): String? = null

            override fun getIdToken(): String? = null

            override fun clearTokens() = Unit
        }
    ) {
        override val isLoggedIn = MutableStateFlow(false)
        override val logoutMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)

        fun completeLogin() {
            isLoggedIn.value = true
        }
    }

    private companion object {
        const val LOGIN_NAVIGATION_TIMEOUT_MS = 20_000L
    }
}
