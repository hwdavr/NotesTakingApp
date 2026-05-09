package com.example.notesapp.auth

import android.content.Context
import android.util.Log
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
    private var sharedPreferences = createSharedPreferences()
    private fun createSharedPreferences() = try {
        EncryptedSharedPreferences.create(
            context,
            "secure_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("TokenStorage", "Error creating EncryptedSharedPreferences", e)
        deleteSharedPreferences()
        // Try one more time after deleting
        EncryptedSharedPreferences.create(
            context,
            "secure_tokens",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    private fun deleteSharedPreferences() {
        try {
            context.deleteSharedPreferences("secure_tokens")
        } catch (e: Exception) {
            Log.e("TokenStorage", "Error deleting SharedPreferences", e)
        }
    }
    open fun saveTokens(accessToken: String, refreshToken: String?, idToken: String? = null) {
        try {
            sharedPreferences.edit().apply {
                putString("access_token", accessToken)
                putString("refresh_token", refreshToken)
                putString("id_token", idToken)
                apply()
            }
        } catch (e: Exception) {
            Log.e("TokenStorage", "Error saving tokens", e)
            handleStorageError()
        }
    }
    open fun getAccessToken(): String? = try {
        sharedPreferences.getString("access_token", null)
    } catch (e: Exception) {
        Log.e("TokenStorage", "Error getting access token", e)
        handleStorageError()
        null
    }
    open fun getRefreshToken(): String? = try {
        sharedPreferences.getString("refresh_token", null)
    } catch (e: Exception) {
        Log.e("TokenStorage", "Error getting refresh token", e)
        handleStorageError()
        null
    }
    open fun getIdToken(): String? = try {
        sharedPreferences.getString("id_token", null)
    } catch (e: Exception) {
        Log.e("TokenStorage", "Error getting id token", e)
        handleStorageError()
        null
    }
    private fun handleStorageError() {
        deleteSharedPreferences()
        sharedPreferences = createSharedPreferences()
    }
    open fun clearTokens() {
        try {
            sharedPreferences.edit().apply {
                remove("access_token")
                remove("refresh_token")
                remove("id_token")
                apply()
            }
        } catch (e: Exception) {
            Log.e("TokenStorage", "Error clearing tokens", e)
            handleStorageError()
        }
    }
}
