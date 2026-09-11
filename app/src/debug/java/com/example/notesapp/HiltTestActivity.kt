package com.example.notesapp

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Debug-only, empty [ComponentActivity] entry point used by instrumented Compose tests that need
 * a Hilt-aware host. It is never part of a release build.
 */
@AndroidEntryPoint
class HiltTestActivity : ComponentActivity()
