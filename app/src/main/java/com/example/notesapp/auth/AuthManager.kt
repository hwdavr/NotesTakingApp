package com.example.notesapp.auth

import android.content.Context
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.example.notesapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenStorage: TokenStorage
) {
    private val TAG = "AuthManager"
    private val account = Auth0(
        context.getString(R.string.auth0_client_id),
        context.getString(R.string.auth0_domain)
    )

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

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
            .start(activityContext, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    Log.d(TAG, "Login successful!")
                    tokenStorage.saveTokens(result.accessToken, result.refreshToken)
                    _isLoggedIn.value = true
                    onSuccess()
                }

                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Login failed: ${error.getDescription()}", error)
                    onError(error.getDescription())
                }
            })
    }

    /**
     * Launches the Auth0 web logout flow.
     */
    fun logout(activityContext: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        WebAuthProvider.logout(account)
            .withScheme(context.getString(R.string.auth0_scheme))
            .start(activityContext, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) {
                    Log.d(TAG, "Logout successful")
                    tokenStorage.clearTokens()
                    _isLoggedIn.value = false
                    onSuccess()
                }

                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Logout failed: ${error.getDescription()}", error)
                    onError(error.getDescription())
                }
            })
    }

    /**
     * Checks if a valid access token exists in secure storage.
     */
    fun checkSession() {
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken != null) {
            Log.d(TAG, "Session found")
            _isLoggedIn.value = true
        } else {
            Log.d(TAG, "No session found")
            _isLoggedIn.value = false
        }
    }
}
