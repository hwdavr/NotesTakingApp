package com.example.notesapp.data.summary

import com.example.notesapp.domain.folder.Folder
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `given prompt returns partial folder id when categorize then does not match longer id`() =
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
            assertNull(result)
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

    private fun folder(id: String, name: String): Folder = Folder(
        id = id,
        name = name,
        createdAt = 0L,
        updatedAt = 0L
    )

    private class RecordingPromptClient(
        private val promptResult: String?,
        private val promptFailure: Exception? = null
    ) : GeminiNanoFolderCategoryPromptClient {
        var callCount = 0
            private set

        override suspend fun generateFolderCategory(prompt: String): String? {
            callCount += 1
            promptFailure?.let { throw it }
            return promptResult
        }
    }
}
