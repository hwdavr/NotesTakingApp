package com.example.notesapp.data.remote

import com.example.notesapp.auth.SessionInvalidator
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionInvalidator: SessionInvalidator
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        sessionInvalidator.invalidateSession()
        return null
    }
}
