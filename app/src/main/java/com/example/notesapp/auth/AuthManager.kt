package com.example.notesapp.auth

import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.notesapp.R

class AuthManager(private val context: Context) {

    private val account = Auth0(
        context.getString(R.string.auth0_client_id),
        context.getString(R.string.auth0_domain)
    )

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        WebAuthProvider.login(account)
            .withScheme(context.getString(R.string.auth0_scheme))
            .withScope("openid profile email")
            .start(context, object : Callback<Credentials, AuthenticationException> {
                override fun onSuccess(result: Credentials) {
                    _isLoggedIn.value = true
                    onSuccess()
                }

                override fun onFailure(error: AuthenticationException) {
                    onError(error.getDescription())
                }
            })
    }

    fun logout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        WebAuthProvider.logout(account)
            .withScheme(context.getString(R.string.auth0_scheme))
            .start(context, object : Callback<Void?, AuthenticationException> {
                override fun onSuccess(result: Void?) {
                    _isLoggedIn.value = false
                    onSuccess()
                }

                override fun onFailure(error: AuthenticationException) {
                    onError(error.getDescription())
                }
            })
    }

    // Mock check for session (in a real app, use CredentialsManager to check stored tokens)
    fun checkSession() {
        // For prototype, we'll keep it simple
    }
}
