package com.example.notesapp.data.remote

import com.example.notesapp.auth.SessionInvalidator
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TokenAuthenticatorTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var sessionInvalidator: SessionInvalidator
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        sessionInvalidator = mockk(relaxed = true)

        client = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(sessionInvalidator))
            .build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `given API returns 401, when request made, then sessionInvalidator invalidateSession called`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        // Act
        client.newCall(request).execute()

        // Assert
        verify(exactly = 1) { sessionInvalidator.invalidateSession() }
    }

    @Test
    fun `given API returns 200 OK, when request is made, then sessionInvalidator is not called`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        // Act
        client.newCall(request).execute()

        // Assert
        verify(exactly = 0) { sessionInvalidator.invalidateSession() }
    }

    @Test
    fun `given API returns 403 Forbidden, when request is made, then sessionInvalidator is not called`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(403))
        val request = Request.Builder()
            .url(mockWebServer.url("/test"))
            .build()

        // Act
        client.newCall(request).execute()

        // Assert
        verify(exactly = 0) { sessionInvalidator.invalidateSession() }
    }
}
