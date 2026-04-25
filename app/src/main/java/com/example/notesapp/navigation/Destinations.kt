package com.example.notesapp.navigation

sealed class Destinations(val route: String) {
    data object Onboarding : Destinations("onboarding")
    data object Notes : Destinations("notes")
    data object Folders : Destinations("folders")
    data object Settings : Destinations("settings")
    data object Editor : Destinations("editor?noteId={noteId}&folderId={folderId}") {
        fun createRoute(noteId: Long? = null, folderId: Long? = null): String {
            val notePart = if (noteId == null) "noteId=-1" else "noteId=$noteId"
            val folderPart = if (folderId == null) "" else "&folderId=$folderId"
            return "editor?$notePart$folderPart"
        }
    }
}
