package com.example.notesapp.navigation

sealed class Destinations(val route: String) {
    data object Onboarding : Destinations("onboarding")
    data object Notes : Destinations("notes")
    data object Folders : Destinations("folders")
    data object Settings : Destinations("settings")
    data object Editor : Destinations("editor?noteId={noteId}") {
        fun createRoute(noteId: Long? = null): String {
            return if (noteId == null) "editor?noteId=-1" else "editor?noteId=$noteId"
        }
    }
}
