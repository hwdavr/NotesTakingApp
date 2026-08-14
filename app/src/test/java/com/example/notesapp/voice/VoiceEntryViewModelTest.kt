package com.example.notesapp.voice

import com.example.notesapp.base.BaseViewModelTest
import com.example.notesapp.domain.note.Note
import com.example.notesapp.domain.voice.usecase.VoiceNotePlaceholderUseCase
import com.example.notesapp.ui.voice.viewmodel.VoiceEntryViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceEntryViewModelTest : BaseViewModelTest() {
    @Test
    fun createHomePlaceholderEmitsThePersistedNoteId() = runTest {
        val placeholder = Note(
            id = "voice_placeholder_test",
            title = "",
            content = "",
            createdAt = 1L,
            updatedAt = 1L
        )
        val useCase = mockk<VoiceNotePlaceholderUseCase>()
        coEvery { useCase.create() } returns placeholder
        val viewModel = VoiceEntryViewModel(useCase)
        var createdId: String? = null

        viewModel.createHomePlaceholder { createdId = it }
        advanceUntilIdle()

        assertEquals(placeholder.id, createdId)
        coVerify(exactly = 1) { useCase.create() }
    }
}
