package com.example.notesapp.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    open fun saveTokens(accessToken: String, refreshToken: String?, idToken: String? = null) {
        sharedPreferences.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("id_token", idToken)
            apply()
        }
    }

    open fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    open fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    open fun getIdToken(): String? {
        return sharedPreferences.getString("id_token", null)
    }

    open fun clearTokens() {
        sharedPreferences.edit().apply {
            remove("access_token")
            remove("refresh_token")
            remove("id_token")
            apply()
        }
    }
}
