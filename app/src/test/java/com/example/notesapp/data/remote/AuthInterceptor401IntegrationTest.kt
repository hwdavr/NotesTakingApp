package com.example.notesapp.data.remote

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.example.notesapp.R
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.auth.TokenStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.http.GET

@OptIn(ExperimentalCoroutinesApi::class)
class AuthInterceptor401IntegrationTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var tokenStorage: TokenStorage
    private lateinit var context: Context
    private lateinit var authManager: AuthManager
    private lateinit var apiService: TestApiService

    interface TestApiService {
        @GET("/test")
        suspend fun makeCall()
    }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

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
        every { context.getString(R.string.auth0_client_id) } returns "client_id"
        every { context.getString(R.string.auth0_domain) } returns "domain"

        tokenStorage = mockk(relaxed = true)
        every { tokenStorage.getAccessToken() } returns "valid_token"

        authManager = AuthManager(context, tokenStorage)

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStorage))
            .authenticator(TokenAuthenticator(authManager))
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .build()
            .create(TestApiService::class.java)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `given user is logged in, when API returns 401, then AuthManager is logged out`() = runTest {
        // Arrange
        assertTrue(authManager.isLoggedIn.value)
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // Act
        runCatching { apiService.makeCall() }

        // Assert
        assertFalse(authManager.isLoggedIn.value)
        verify(exactly = 1) { tokenStorage.clearTokens() }
    }

    @Test
    fun `concurrent 401 calls logout and clear tokens once`() = runTest {
        // Arrange
        assertTrue(authManager.isLoggedIn.value)
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        // Act
        val call1 = async {
            runCatching { apiService.makeCall() }
        }
        val call2 = async {
            runCatching { apiService.makeCall() }
        }
        awaitAll(call1, call2)

        // Assert
        assertFalse(authManager.isLoggedIn.value)
        verify(exactly = 1) { tokenStorage.clearTokens() }
    }
}
