package com.example.notesapp.navigation

sealed class Destinations(val route: String) {
    data object Notes : Destinations("notes")
    data object Folders : Destinations("folders")
    data object Settings : Destinations("settings")
}
