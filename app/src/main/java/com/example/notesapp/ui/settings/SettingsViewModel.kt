package com.example.notesapp.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.notesapp.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    fun logout(activityContext: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authManager.logout(activityContext, onSuccess, onError)
    }
}
