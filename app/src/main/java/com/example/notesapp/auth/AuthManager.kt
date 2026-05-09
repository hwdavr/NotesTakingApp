package com.example.notesapp.auth

import android.content.Context
import android.util.Base64
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.notesapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

@Singleton
open class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStorage: TokenStorage
) {
    private val account = Auth0(
        context.getString(R.string.auth0_client_id),
        context.getString(R.string.auth0_domain)
    )
    companion object {
        private const val TAG = "AuthManager"
    }
    private val _isLoggedIn = MutableStateFlow(false)
    open val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    private val _profileEmail = MutableStateFlow<String?>(null)
    open val profileEmail: StateFlow<String?> = _profileEmail.asStateFlow()
    init {
        checkSession()
    }
    /**
     * Launches the Auth0 web login flow.
     */
    fun login(activityContext: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        Log.d(TAG, "Starting login flow")
        WebAuthProvider.login(account)
            .withScheme(context.getString(R.string.auth0_scheme))
            .withScope("openid profile email offline_access")
            .withAudience(context.getString(R.string.auth0_audience))
            .start(
                activityContext,
                object : Callback<Credentials, AuthenticationException> {
                    override fun onSuccess(result: Credentials) {
                        Log.d(TAG, "Login successful!")
                        tokenStorage.saveTokens(result.accessToken, result.refreshToken, result.idToken)
                        _profileEmail.value = extractEmailFromIdToken(result.idToken)
                        _isLoggedIn.value = true
                        onSuccess()
                    }
                    override fun onFailure(error: AuthenticationException) {
                        Log.e(TAG, "Login failed: ${error.getDescription()}", error)
                        onError(error.getDescription())
                    }
                }
            )
    }
    /**
     * Launches the Auth0 web logout flow.
     */
    open fun logout(activityContext: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        WebAuthProvider.logout(account)
            .withScheme(context.getString(R.string.auth0_scheme))
            .start(
                activityContext,
                object : Callback<Void?, AuthenticationException> {
                    override fun onSuccess(result: Void?) {
                        Log.d(TAG, "Logout successful")
                        tokenStorage.clearTokens()
                        _profileEmail.value = null
                        _isLoggedIn.value = false
                        onSuccess()
                    }
                    override fun onFailure(error: AuthenticationException) {
                        Log.e(TAG, "Logout failed: ${error.getDescription()}", error)
                        onError(error.getDescription())
                    }
                }
            )
    }
    /**
     * Checks if a valid access token exists in secure storage.
     */
    fun checkSession() {
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken != null) {
            Log.d(TAG, "Session found")
            _profileEmail.value = extractEmailFromIdToken(tokenStorage.getIdToken())
            _isLoggedIn.value = true
        } else {
            Log.d(TAG, "No session found")
            _profileEmail.value = null
            _isLoggedIn.value = false
        }
    }
    private fun extractEmailFromIdToken(idToken: String?): String? {
        if (idToken.isNullOrBlank()) return null
        val parts = idToken.split(".")
        if (parts.size < 2) return null
        return runCatching {
            val payload = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            JSONObject(String(payload, StandardCharsets.UTF_8)).optString("email").takeIf { it.isNotBlank() }
        }.getOrNull()
    }
}
