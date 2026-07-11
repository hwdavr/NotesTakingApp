package com.example.notesapp.ui.theme

import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightAppColors.primary,
    secondary = LightAppColors.secondary,
    background = LightAppColors.background,
    surface = LightAppColors.surface,
    onBackground = LightAppColors.textPrimary,
    onSurface = LightAppColors.textPrimary,
    error = LightAppColors.error
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAppColors.primary,
    secondary = DarkAppColors.secondary,
    background = DarkAppColors.background,
    surface = DarkAppColors.surface,
    onBackground = DarkAppColors.textPrimary,
    onSurface = DarkAppColors.textPrimary,
    error = DarkAppColors.error
)

@Composable
fun NotesTakingAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            val activity = context as? Activity ?: (context as? ContextWrapper)?.baseContext as? Activity
            activity?.window?.let { window ->
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
