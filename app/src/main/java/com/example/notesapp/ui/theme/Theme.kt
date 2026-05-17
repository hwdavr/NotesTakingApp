package com.example.notesapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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
fun NotesTakingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkAppColors else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
