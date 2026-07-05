package com.example.notesapp.auth

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.example.notesapp.R
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthManagerLogoutTest {
    private lateinit var context: Context
    private lateinit var tokenStorage: TokenStorage
    private lateinit var authManager: AuthManager

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } answers {
            val str = firstArg<CharSequence?>()
            str.isNullOrEmpty()
        }

        mockkStatic(Base64::class)
        every { Base64.encode(any(), any()) } answers { ByteArray(0) }
        every { Base64.encodeToString(any(), any()) } returns ""

        context = mockk(relaxed = true)
        tokenStorage = mockk(relaxed = true)
        every { context.getString(R.string.auth0_client_id) } returns "client_id"
        every { context.getString(R.string.auth0_domain) } returns "domain"
    }

    @Test
    fun `given user is logged in, when forceLogout is called, then tokens are cleared and isLoggedIn is false`() {
        // Arrange
        every { tokenStorage.getAccessToken() } returns "valid_token"
        every { tokenStorage.getIdToken() } returns null
        authManager = AuthManager(context, tokenStorage)
        assertTrue(authManager.isLoggedIn.value)

        // Act
        authManager.forceLogout()

        // Assert
        assertFalse(authManager.isLoggedIn.value)
        verify { tokenStorage.clearTokens() }
    }

    @Test
    fun `already logged out, forceLogout does not clear tokens again`() {
        // Arrange
        every { tokenStorage.getAccessToken() } returns null
        authManager = AuthManager(context, tokenStorage)
        assertFalse(authManager.isLoggedIn.value)

        // Act
        authManager.forceLogout()

        // Assert
        assertFalse(authManager.isLoggedIn.value)
        verify(exactly = 0) { tokenStorage.clearTokens() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `given user is logged in, when forceLogout is called, then logout message is emitted`() =
        runTest(UnconfinedTestDispatcher()) {
            // Arrange
            val expectedMessage = "Your session has expired. Please log in again."
            every { tokenStorage.getAccessToken() } returns "valid_token"
            every { tokenStorage.getIdToken() } returns null
            every { context.getString(R.string.auth_session_expired) } returns expectedMessage
            authManager = AuthManager(context, tokenStorage)
            assertTrue(authManager.isLoggedIn.value)

            val messages = mutableListOf<String>()
            val collectJob = launch {
                authManager.logoutMessage.toList(messages)
            }

            // Act
            authManager.forceLogout()

            // Assert
            assertEquals(1, messages.size)
            assertEquals(expectedMessage, messages.first())
            collectJob.cancel()
        }
}
