package com.example.notesapp.navigation

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DestinationsTest {
    @Test
    fun givenReservedCharacters_whenCreatingEditorRoute_thenArgumentsAreEncoded() {
        val route = Destinations.Editor.createRoute(
            noteId = "note/one?two",
            folderId = "folder&one"
        )

        assertEquals("editor?noteId=note%2Fone%3Ftwo&folderId=folder%26one", route)
    }

    @Test
    fun givenReservedCharacters_whenCreatingCollectionRoute_thenArgumentsAreEncoded() {
        val route = Destinations.CollectionNotes.createRoute(
            type = "label&type",
            label = "Label / One",
            folderId = "folder/one"
        )

        assertEquals(
            "collectionNotes?type=label%26type&folderId=folder%2Fone&label=Label%20%2F%20One",
            route
        )
    }

    @Test
    fun givenReservedCharacters_whenCreatingExportRoute_thenNoteIdIsEncoded() {
        val route = Destinations.ExportNote.createRoute("note/one")

        assertEquals("exportNote/note%2Fone", route)
    }
}
