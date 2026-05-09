package com.example.notesapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
val bottomNavItems = listOf(
    BottomNavItem("Home", Destinations.Notes.route, Icons.AutoMirrored.Outlined.StickyNote2),
    BottomNavItem("Folders", Destinations.Folders.route, Icons.Outlined.Folder),
    BottomNavItem("Settings", Destinations.Settings.route, Icons.Outlined.Settings)
)
