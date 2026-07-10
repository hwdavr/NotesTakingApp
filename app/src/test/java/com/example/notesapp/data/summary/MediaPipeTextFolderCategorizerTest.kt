package com.example.notesapp.data.summary

import com.example.notesapp.domain.folder.Folder
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaPipeTextFolderCategorizerTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `given semantic match from folder description when categorize then returns matching folder`() =
        runTest(testDispatcher) {
            // Arrange
            val folders = listOf(
                folder(id = "folder_admin", name = "Admin", description = "Account settings and passwords"),
                folder(id = "folder_receipts", name = "Receipts", description = "Travel costs and client invoices")
            )
            val categorizer = categorizer(
                embeddingClient = FakeFolderTextEmbeddingClient(
                    mapOf(
                        "Folder path: Admin. Description: Account settings and passwords" to 0.28,
                        "Folder path: Receipts. Description: Travel costs and client invoices" to 0.84
                    )
                )
            )

            // Act
            val result = categorizer.categorize(
                title = "Hotel bill",
                content = "Room charge from the Singapore client trip.",
                folders = folders
            )

            // Assert
            assertEquals(folders[1], result)
        }

    @Test
    fun `given subfolder when categorize then embeds full parent path`() = runTest(testDispatcher) {
        // Arrange
        val folders = listOf(
            folder(id = "folder_work", name = "Work"),
            folder(id = "folder_invoices", name = "Invoices", parentFolderId = "folder_work")
        )
        val embeddingClient = FakeFolderTextEmbeddingClient(
            mapOf(
                "Folder path: Work" to 0.25,
                "Folder path: Work / Invoices" to 0.76
            )
        )
        val categorizer = categorizer(embeddingClient = embeddingClient)

        // Act
        val result = categorizer.categorize(
            title = "Client invoice",
            content = "Invoice for project delivery.",
            folders = folders
        )

        // Assert
        assertEquals(folders[1], result)
        assertTrue(embeddingClient.seenSecondTexts.contains("Folder path: Work / Invoices"))
    }

    @Test
    fun `given low semantic confidence and keyword match when categorize then returns keyword fallback`() =
        runTest(testDispatcher) {
            // Arrange
            val folders = listOf(
                folder(id = "folder_work", name = "Work"),
                folder(id = "folder_personal", name = "Personal")
            )
            val categorizer = categorizer(embeddingClient = FakeFolderTextEmbeddingClient(defaultScore = 0.05))

            // Act
            val result = categorizer.categorize(
                title = "Work tasks",
                content = "Prepare work agenda.",
                folders = folders
            )

            // Assert
            assertEquals(folders[0], result)
        }

    @Test
    fun `given embedding client fails when categorize then returns stable root fallback`() = runTest(testDispatcher) {
        // Arrange
        val folders = listOf(
            folder(id = "folder_work", name = "Work", sortKey = "b"),
            folder(id = "folder_personal", name = "Personal", sortKey = "a")
        )
        val categorizer = categorizer(embeddingClient = FailingFolderTextEmbeddingClient())

        // Act
        val result = categorizer.categorize(
            title = "Untitled",
            content = "A short note without category words.",
            folders = folders
        )

        // Assert
        assertEquals(folders[1], result)
    }

    @Test
    fun `given no folders when categorize then returns null without embedding`() = runTest(testDispatcher) {
        // Arrange
        val embeddingClient = FakeFolderTextEmbeddingClient(defaultScore = 0.5)
        val categorizer = categorizer(embeddingClient = embeddingClient)

        // Act
        val result = categorizer.categorize(
            title = "Untitled",
            content = "A short note.",
            folders = emptyList()
        )

        // Assert
        assertNull(result)
        assertEquals(0, embeddingClient.callCount)
    }

    private fun categorizer(embeddingClient: FolderTextEmbeddingClient): MediaPipeTextFolderCategorizer =
        MediaPipeTextFolderCategorizer(
            embeddingClient = embeddingClient,
            ioDispatcher = testDispatcher
        )

    private fun folder(
        id: String,
        name: String,
        description: String = "",
        parentFolderId: String? = null,
        sortKey: String = ""
    ): Folder = Folder(
        id = id,
        name = name,
        description = description,
        parentFolderId = parentFolderId,
        sortKey = sortKey,
        createdAt = 0L,
        updatedAt = 0L
    )

    private class FakeFolderTextEmbeddingClient(
        private val scoresBySecondText: Map<String, Double> = emptyMap(),
        private val defaultScore: Double = 0.0
    ) : FolderTextEmbeddingClient {
        val seenSecondTexts = mutableListOf<String>()
        var callCount = 0
            private set

        override fun similarity(firstText: String, secondText: String): Double {
            callCount += 1
            seenSecondTexts += secondText
            return scoresBySecondText[secondText] ?: defaultScore
        }
    }

    private class FailingFolderTextEmbeddingClient : FolderTextEmbeddingClient {
        override fun similarity(firstText: String, secondText: String): Double {
            error("MediaPipe unavailable")
        }
    }
}
