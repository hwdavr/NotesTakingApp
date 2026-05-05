package com.example.notesapp.navigation

import android.net.Uri

sealed class Destinations(val route: String) {
    data object Onboarding : Destinations("onboarding")
    data object Notes : Destinations("notes")
    data object Folders : Destinations("folders")
    data object Settings : Destinations("settings")
    data object CollectionNotes : Destinations("collectionNotes?type={type}&folderId={folderId}&label={label}") {
        fun createRoute(type: String, label: String, folderId: String? = null): String {
            val encodedLabel = Uri.encode(label)
            return "collectionNotes?type=$type&folderId=${folderId ?: ""}&label=$encodedLabel"
        }
    }
    data object MoveTo : Destinations("moveTo?itemType={itemType}&itemId={itemId}") {
        fun createRoute(itemType: String, itemId: String): String =
            "moveTo?itemType=${Uri.encode(itemType)}&itemId=${Uri.encode(itemId)}"
    }
    data object Editor : Destinations("editor?noteId={noteId}&folderId={folderId}") {
        fun createRoute(noteId: String? = null, folderId: String? = null): String {
            val notePart = if (noteId.isNullOrBlank()) "noteId=" else "noteId=$noteId"
            val folderPart = if (folderId == null) "" else "&folderId=$folderId"
            return "editor?$notePart$folderPart"
        }
    }
    data object ExportNote : Destinations("exportNote/{noteId}") {
        fun createRoute(noteId: String): String = "exportNote/$noteId"
    }
}
