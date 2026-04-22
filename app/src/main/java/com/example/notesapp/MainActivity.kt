package com.example.notesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.notesapp.auth.AuthManager
import com.example.notesapp.navigation.AppNavGraph
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager.checkSession()

        setContent {
            NotesTakingAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(authManager = authManager, activity = this)
                }
            }
        }
    }
}
