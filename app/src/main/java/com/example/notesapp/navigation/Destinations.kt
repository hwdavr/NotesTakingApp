package com.example.notesapp.navigation

import android.net.Uri

sealed class Destinations(val route: String) {
    val baseRoute: String
        get() = route.substringBefore('?').substringBefore('/')

    data object Onboarding : Destinations("onboarding")
    data object Notes : Destinations("notes")
    data object Folders : Destinations("folders")
    data object Settings : Destinations("settings")
    data object CollectionNotes : Destinations("collectionNotes?type={type}&folderId={folderId}&label={label}") {
        fun createRoute(type: String, label: String, folderId: String? = null): String {
            val encodedType = Uri.encode(type)
            val encodedFolderId = Uri.encode(folderId.orEmpty())
            val encodedLabel = Uri.encode(label)
            return "$baseRoute?type=$encodedType&folderId=$encodedFolderId&label=$encodedLabel"
        }
    }
    data object MoveTo : Destinations("moveTo?itemType={itemType}&itemId={itemId}") {
        fun createRoute(itemType: String, itemId: String): String =
            "$baseRoute?itemType=${Uri.encode(itemType)}&itemId=${Uri.encode(itemId)}"
    }
    data object FolderDescription : Destinations("folderDescription/{folderId}") {
        fun createRoute(folderId: String): String = "$baseRoute/${Uri.encode(folderId)}"
    }
    data object Editor : Destinations("editor?noteId={noteId}&folderId={folderId}") {
        fun createRoute(noteId: String? = null, folderId: String? = null): String {
            val notePart = "noteId=${Uri.encode(noteId.orEmpty())}"
            val folderPart = folderId?.let { "&folderId=${Uri.encode(it)}" }.orEmpty()
            return "$baseRoute?$notePart$folderPart"
        }
    }
    data object VoiceRecorder : Destinations(
        "voiceRecorder?noteId={noteId}&source={source}&focusedBlockId={focusedBlockId}"
    ) {
        fun createRoute(noteId: String? = null, source: String = "EDITOR", focusedBlockId: String? = null): String =
            "$baseRoute?noteId=${Uri.encode(noteId.orEmpty())}" +
                "&source=${Uri.encode(source)}" +
                "&focusedBlockId=${Uri.encode(focusedBlockId.orEmpty())}"
    }
    data object ExportNote : Destinations("exportNote/{noteId}") {
        fun createRoute(noteId: String): String = "$baseRoute/${Uri.encode(noteId)}"
    }
    data object SharedUsers : Destinations("sharedUsers/{noteId}") {
        fun createRoute(noteId: String): String = "$baseRoute/${Uri.encode(noteId)}"
    }
    data object ShareInvite : Destinations("shareInvite/{noteId}") {
        fun createRoute(noteId: String): String = "$baseRoute/${Uri.encode(noteId)}"
    }
    data object ManageAccess : Destinations("manageAccess/{noteId}") {
        fun createRoute(noteId: String): String = "$baseRoute/${Uri.encode(noteId)}"
    }
    data object NoteLinkPicker : Destinations(
        "noteLinkPicker?callerNoteId={callerNoteId}&hasExistingLink={hasExistingLink}"
    ) {
        fun createRoute(callerNoteId: String, hasExistingLink: Boolean = false): String =
            "$baseRoute?callerNoteId=${Uri.encode(callerNoteId)}&hasExistingLink=$hasExistingLink"
    }
}
