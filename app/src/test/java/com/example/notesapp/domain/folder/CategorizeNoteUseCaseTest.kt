package com.example.notesapp.domain.folder

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategorizeNoteUseCaseTest {

    private val folderCategorizer = mockk<FolderCategorizer>()
    private val useCase = CategorizeNoteUseCase(folderCategorizer)

    private val testFolders = listOf(
        Folder(id = "f1", name = "Work", createdAt = 0L, updatedAt = 0L),
        Folder(id = "f2", name = "Personal", createdAt = 0L, updatedAt = 0L),
        Folder(id = "f3", name = "Shopping List", createdAt = 0L, updatedAt = 0L)
    )

    @Test
    fun `given matching folder name in title when invoke then returns matching folder`() = runTest {
        // Arrange
        val title = "Work Stuff"
        val content = "This is a work related note."
        coEvery { folderCategorizer.categorize(title, content, testFolders) } returns testFolders[0]

        // Act
        val result = useCase(title, content, testFolders)

        // Assert
        assertEquals(testFolders[0], result)
    }

    @Test
    fun `given empty title and content when invoke then returns null and does not run AI`() = runTest {
        // Act
        val result = useCase("", "", testFolders)

        // Assert
        assertNull(result)
    }

    @Test
    fun `given no matching folders when invoke then returns null`() = runTest {
        // Arrange
        val title = "Random"
        val content = "No match here."
        coEvery { folderCategorizer.categorize(title, content, testFolders) } returns null

        // Act
        val result = useCase(title, content, testFolders)

        // Assert
        assertNull(result)
    }

    @Test
    fun `given folders is empty when invoke then returns null and does not run AI`() = runTest {
        // Act
        val result = useCase("Title", "Content", emptyList())

        // Assert
        assertNull(result)
    }
}
