package com.example.notesapp.ui.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SettingsUiState(
    val profileTitle: String = "Guest"
)

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {
    open val uiState: StateFlow<SettingsUiState> = combine(
        authManager.isLoggedIn,
        authManager.profileEmail
    ) { isLoggedIn, email ->
        SettingsUiState(
            profileTitle = if (isLoggedIn) email?.takeIf { it.isNotBlank() } ?: "Guest" else "Guest"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState()
    )
    fun logout(activityContext: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authManager.logout(activityContext, onSuccess, onError)
    }
}
