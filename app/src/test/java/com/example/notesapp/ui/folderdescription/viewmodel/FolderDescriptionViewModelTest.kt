package com.example.notesapp.ui.folderdescription.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.example.notesapp.R
import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.folder.Folder
import com.example.notesapp.domain.folder.FolderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FolderDescriptionViewModelTest : BaseViewModelTest() {
    private val folderRepository: FolderRepository = mockk(relaxed = true)
    private val folder = Folder(
        id = "f1",
        name = "Receipts",
        description = "Client receipts",
        createdAt = 1000L,
        updatedAt = 1000L
    )
    private val folderFlow = MutableStateFlow<Folder?>(folder)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { folderRepository.getFolder("f1") } returns folderFlow
        every { folderRepository.getFolder("") } returns MutableStateFlow(null)
    }

    @Test
    fun `given existing folder when initialized then exposes folder description`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Receipts", state.folderName)
        assertEquals("Client receipts", state.description)
        assertFalse(state.canSave)
    }

    @Test
    fun `given changed description when onDescriptionChanged then enables save`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onDescriptionChanged("Travel receipts")

        val state = viewModel.uiState.value
        assertEquals("Travel receipts", state.description)
        assertTrue(state.canSave)
    }

    @Test
    fun `given changed description when save succeeds then updates repository and navigates back`() = runTest {
        coEvery { folderRepository.updateDescription(any(), "Travel receipts") } returns Unit
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onDescriptionChanged("Travel receipts")

        viewModel.save()
        advanceUntilIdle()

        coVerify { folderRepository.updateDescription(folder, "Travel receipts") }
    }

    @Test
    fun `given whitespace description when save then trims before repository update`() = runTest {
        coEvery { folderRepository.updateDescription(any(), "") } returns Unit
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onDescriptionChanged("   ")

        viewModel.save()
        advanceUntilIdle()

        coVerify { folderRepository.updateDescription(folder, "") }
    }

    @Test
    fun `given missing folder when initialized then exposes error and disables save`() = runTest {
        every { folderRepository.getFolder("missing") } returns MutableStateFlow(null)

        val viewModel = viewModel(folderId = "missing")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(R.string.folder_description_missing_error, state.errorMessageRes)
        assertFalse(state.canSave)
    }

    @Test
    fun `given repository save fails when save then exposes save error`() = runTest {
        coEvery { folderRepository.updateDescription(any(), "Travel receipts") } throws RuntimeException("boom")
        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.onDescriptionChanged("Travel receipts")

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertEquals(R.string.folder_description_save_error, state.errorMessageRes)
    }

    private fun viewModel(folderId: String = "f1") = FolderDescriptionViewModel(
        savedStateHandle = SavedStateHandle(mapOf("folderId" to folderId)),
        folderRepository = folderRepository
    )
}
