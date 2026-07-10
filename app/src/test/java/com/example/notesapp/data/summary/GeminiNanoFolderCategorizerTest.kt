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
class GeminiNanoFolderCategorizerTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `given semantic note and no keyword overlap when categorize then returns AI selected folder`() =
        runTest(testDispatcher) {
            // Arrange
            val categorizer = categorizer(promptResult = "folder_trips")
            val folders = listOf(
                folder(id = "folder_admin", name = "Admin"),
                folder(id = "folder_trips", name = "Trips")
            )

            // Act
            val result = categorizer.categorize(
                title = "Boarding pass",
                content = "Flight SQ 321 departs from Singapore at 09:30 with hotel check-in after landing.",
                folders = folders
            )

            // Assert
            assertEquals(folders[1], result)
        }

    @Test
    fun `given prompt client fails and keyword matches when categorize then returns keyword folder`() =
        runTest(testDispatcher) {
            // Arrange
            val categorizer = categorizer(promptFailure = IllegalStateException("AICore unavailable"))
            val folders = listOf(
                folder(id = "folder_work", name = "Work"),
                folder(id = "folder_personal", name = "Personal")
            )

            // Act
            val result = categorizer.categorize(
                title = "Work tasks",
                content = "Prepare work agenda",
                folders = folders
            )

            // Assert
            assertEquals(folders[0], result)
        }

    @Test
    fun `given prompt returns unknown folder and keyword matches when categorize then returns keyword folder`() =
        runTest(testDispatcher) {
            // Arrange
            val categorizer = categorizer(promptResult = "folder_unknown")
            val folders = listOf(
                folder(id = "folder_work", name = "Work"),
                folder(id = "folder_personal", name = "Personal")
            )

            // Act
            val result = categorizer.categorize(
                title = "Personal reminder",
                content = "Personal appointment tomorrow",
                folders = folders
            )

            // Assert
            assertEquals(folders[1], result)
        }

    @Test
    fun `given prompt returns partial folder id when categorize then uses root fallback instead of longer id`() =
        runTest(testDispatcher) {
            // Arrange
            val categorizer = categorizer(promptResult = "folder_1")
            val folders = listOf(
                folder(id = "folder_10", name = "Archive"),
                folder(id = "folder_20", name = "Reference")
            )

            // Act
            val result = categorizer.categorize(
                title = "Boarding pass",
                content = "Flight departs at 09:30.",
                folders = folders
            )

            // Assert
            assertEquals(folders[0], result)
        }

    @Test
    fun `given prompt returns unknown text and no keyword match when categorize then returns stable root folder`() =
        runTest(testDispatcher) {
            // Arrange
            val categorizer = categorizer(promptResult = "I cannot decide.")
            val folders = listOf(
                folder(id = "folder_work", name = "Work"),
                folder(id = "folder_personal", name = "Personal")
            )

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
    fun `given prompt returns blank and no keyword match when categorize then returns stable root folder`() =
        runTest(testDispatcher) {
            // Arrange
            val categorizer = categorizer(promptResult = "")
            val folders = listOf(
                folder(id = "folder_work", name = "Work"),
                folder(id = "folder_personal", name = "Personal")
            )

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
    fun `given no root folder and no match when categorize then returns null`() = runTest(testDispatcher) {
        // Arrange
        val categorizer = categorizer(promptResult = "unknown")
        val folders = listOf(
            folder(id = "folder_child", name = "Child", parentFolderId = "folder_missing")
        )

        // Act
        val result = categorizer.categorize(
            title = "Untitled",
            content = "A short note without category words.",
            folders = folders
        )

        // Assert
        assertNull(result)
    }

    @Test
    fun `given subfolder when categorize then prompt includes parent path`() = runTest(testDispatcher) {
        // Arrange
        val promptClient = RecordingPromptClient(promptResult = "folder_invoices")
        val categorizer = categorizer(promptClient = promptClient)
        val folders = listOf(
            folder(id = "folder_work", name = "Work"),
            folder(id = "folder_invoices", name = "Invoices", parentFolderId = "folder_work")
        )

        // Act
        categorizer.categorize(
            title = "Client invoice",
            content = "Invoice for project delivery.",
            folders = folders
        )

        // Assert
        assertContains(promptClient.lastPrompt, "- folder_invoices: Work / Invoices")
    }

    @Test
    fun `given folder description when categorize then prompt includes description`() = runTest(testDispatcher) {
        // Arrange
        val promptClient = RecordingPromptClient(promptResult = "folder_receipts")
        val categorizer = categorizer(promptClient = promptClient)
        val folders = listOf(
            folder(
                id = "folder_receipts",
                name = "Receipts",
                description = "Client receipts, travel costs, and invoices"
            )
        )

        // Act
        categorizer.categorize(
            title = "Hotel bill",
            content = "Room charge from the Singapore trip.",
            folders = folders
        )

        // Assert
        assertContains(
            promptClient.lastPrompt,
            "- folder_receipts: Receipts | Description: Client receipts, travel costs, and invoices"
        )
    }

    @Test
    fun `given blank folder description when categorize then prompt omits description suffix`() =
        runTest(testDispatcher) {
            // Arrange
            val promptClient = RecordingPromptClient(promptResult = "folder_receipts")
            val categorizer = categorizer(promptClient = promptClient)
            val folders = listOf(
                folder(id = "folder_receipts", name = "Receipts", description = " ")
            )

            // Act
            categorizer.categorize(
                title = "Hotel bill",
                content = "Room charge from the Singapore trip.",
                folders = folders
            )

            // Assert
            assertContains(promptClient.lastPrompt, "- folder_receipts: Receipts")
            assertTrue(
                "Expected prompt to omit blank description suffix",
                promptClient.lastPrompt?.contains("| Description:") == false
            )
        }

    @Test
    fun `given long folder description when categorize then prompt truncates description`() = runTest(testDispatcher) {
        // Arrange
        val promptClient = RecordingPromptClient(promptResult = "folder_receipts")
        val categorizer = categorizer(promptClient = promptClient)
        val longDescription = "a".repeat(350)
        val folders = listOf(
            folder(id = "folder_receipts", name = "Receipts", description = longDescription)
        )

        // Act
        categorizer.categorize(
            title = "Hotel bill",
            content = "Room charge from the Singapore trip.",
            folders = folders
        )

        // Assert
        assertContains(promptClient.lastPrompt, "| Description: ${"a".repeat(300)}")
        assertTrue(
            "Expected prompt to truncate long description",
            promptClient.lastPrompt?.contains("a".repeat(301)) == false
        )
    }

    @Test
    fun `given description explains folder intent when model returns described folder then categorize returns it`() =
        runTest(testDispatcher) {
            // Arrange
            val categorizer = categorizer(promptResult = "folder_receipts")
            val folders = listOf(
                folder(id = "folder_admin", name = "Admin", description = "Generic account administration"),
                folder(id = "folder_receipts", name = "Receipts", description = "Travel costs and client invoices")
            )

            // Act
            val result = categorizer.categorize(
                title = "Hotel bill",
                content = "Room charge from the Singapore trip.",
                folders = folders
            )

            // Assert
            assertEquals(folders[1], result)
        }

    @Test
    fun `given root folders when categorize then prompt instructs root fallback`() = runTest(testDispatcher) {
        // Arrange
        val promptClient = RecordingPromptClient(promptResult = "folder_work")
        val categorizer = categorizer(promptClient = promptClient)
        val folders = listOf(
            folder(id = "folder_work", name = "Work"),
            folder(id = "folder_personal", name = "Personal")
        )

        // Act
        categorizer.categorize(
            title = "Ambiguous note",
            content = "Something loosely related to office planning.",
            folders = folders
        )

        // Assert
        assertContains(
            promptClient.lastPrompt,
            "If no exact folder or subfolder fits, choose the closest existing root folder."
        )
    }

    @Test
    fun `given root folders when categorize then prompt instructs id only output`() = runTest(testDispatcher) {
        // Arrange
        val promptClient = RecordingPromptClient(promptResult = "folder_work")
        val categorizer = categorizer(promptClient = promptClient)
        val folders = listOf(
            folder(id = "folder_work", name = "Work"),
            folder(id = "folder_personal", name = "Personal")
        )

        // Act
        categorizer.categorize(
            title = "Ambiguous note",
            content = "Something loosely related to office planning.",
            folders = folders
        )

        // Assert
        assertContains(promptClient.lastPrompt, "Reply only with exactly one folder id from the Folders list.")
        assertContains(promptClient.lastPrompt, "Do not include prose, markdown, JSON, punctuation, or explanation.")
    }

    @Test
    fun `given prompt returns exact subfolder path when categorize then returns subfolder`() = runTest(testDispatcher) {
        // Arrange
        val categorizer = categorizer(promptResult = "Work / Invoices")
        val folders = listOf(
            folder(id = "folder_work", name = "Work"),
            folder(id = "folder_invoices", name = "Invoices", parentFolderId = "folder_work")
        )

        // Act
        val result = categorizer.categorize(
            title = "Client invoice",
            content = "Invoice for project delivery.",
            folders = folders
        )

        // Assert
        assertEquals(folders[1], result)
    }

    @Test
    fun `given nested subfolder when categorize then prompt includes full root to leaf path`() =
        runTest(testDispatcher) {
            // Arrange
            val promptClient = RecordingPromptClient(promptResult = "folder_receipts")
            val categorizer = categorizer(promptClient = promptClient)
            val folders = listOf(
                folder(id = "folder_work", name = "Work"),
                folder(id = "folder_client", name = "Client A", parentFolderId = "folder_work"),
                folder(id = "folder_receipts", name = "Receipts", parentFolderId = "folder_client")
            )

            // Act
            categorizer.categorize(
                title = "Hotel receipt",
                content = "Receipt for the client trip.",
                folders = folders
            )

            // Assert
            assertContains(promptClient.lastPrompt, "- folder_receipts: Work / Client A / Receipts")
        }

    @Test
    fun `given folder has missing parent when categorize then prompt includes folder name`() = runTest(testDispatcher) {
        // Arrange
        val promptClient = RecordingPromptClient(promptResult = "folder_orphan")
        val categorizer = categorizer(promptClient = promptClient)
        val folders = listOf(
            folder(id = "folder_orphan", name = "Orphan", parentFolderId = "missing_parent")
        )

        // Act
        categorizer.categorize(
            title = "Loose note",
            content = "No known parent exists.",
            folders = folders
        )

        // Assert
        assertContains(promptClient.lastPrompt, "- folder_orphan: Orphan")
    }

    @Test
    fun `given no folders when categorize then returns null without prompting`() = runTest(testDispatcher) {
        // Arrange
        val promptClient = RecordingPromptClient(promptResult = "folder_trips")
        val categorizer = categorizer(promptClient = promptClient)

        // Act
        val result = categorizer.categorize(
            title = "Boarding pass",
            content = "Flight departs at 09:30.",
            folders = emptyList()
        )

        // Assert
        assertNull(result)
        assertEquals(0, promptClient.callCount)
    }

    private fun categorizer(
        promptResult: String? = null,
        promptFailure: Exception? = null,
        promptClient: GeminiNanoFolderCategoryPromptClient = RecordingPromptClient(promptResult, promptFailure)
    ): GeminiNanoFolderCategorizer = GeminiNanoFolderCategorizer(
        promptClient = promptClient,
        ioDispatcher = testDispatcher
    )

    private fun folder(id: String, name: String, description: String = "", parentFolderId: String? = null): Folder =
        Folder(
            id = id,
            name = name,
            description = description,
            parentFolderId = parentFolderId,
            createdAt = 0L,
            updatedAt = 0L
        )

    private fun assertContains(actual: String?, expectedSubstring: String) {
        assertTrue(
            "Expected <$actual> to contain <$expectedSubstring>",
            actual?.contains(expectedSubstring) == true
        )
    }

    private class RecordingPromptClient(
        private val promptResult: String?,
        private val promptFailure: Exception? = null
    ) : GeminiNanoFolderCategoryPromptClient {
        var callCount = 0
            private set
        var lastPrompt: String? = null
            private set

        override suspend fun generateFolderCategory(prompt: String): String? {
            callCount += 1
            lastPrompt = prompt
            promptFailure?.let { throw it }
            return promptResult
        }
    }
}
