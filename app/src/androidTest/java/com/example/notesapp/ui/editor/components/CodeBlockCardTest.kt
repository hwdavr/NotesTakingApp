package com.example.notesapp.ui.editor.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CodeBlockCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCopyCodeToClipboard() {
        val testCode = "fun main() {\n    println(\"Hello\")\n}"
        val block = EditorBlock.CodeBlock(id = "c1", language = "Kotlin", code = testCode)

        composeTestRule.setContent {
            NotesTakingAppTheme {
                CodeBlockCard(
                    block = block,
                    isEditable = true,
                    onUpdateCode = {},
                    onUpdateLanguage = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_code_copy_btn_c1")
            .assertIsDisplayed()
            .performClick()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()

        assertEquals(testCode, clipText)
    }

    @Test
    fun testLanguageSelectionInvokesCallback() {
        var selectedLanguage: String? = null
        val block = EditorBlock.CodeBlock(id = "c2", language = "Plain Text", code = "")

        composeTestRule.setContent {
            NotesTakingAppTheme {
                CodeBlockCard(
                    block = block,
                    isEditable = true,
                    onUpdateCode = {},
                    onUpdateLanguage = { selectedLanguage = it },
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_code_lang_selector_c2").performClick()
        composeTestRule.onNodeWithTag("editor_code_lang_item_python").performClick()

        assertEquals("Python", selectedLanguage)
    }

    @Test
    fun testDeleteButtonInvokesCallback() {
        var deleted = false
        val block = EditorBlock.CodeBlock(id = "c3", language = "Kotlin", code = "fun main() {}")

        composeTestRule.setContent {
            NotesTakingAppTheme {
                CodeBlockCard(
                    block = block,
                    isEditable = true,
                    onUpdateCode = {},
                    onUpdateLanguage = {},
                    onDelete = { deleted = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_code_delete_btn_c3").performClick()

        assertTrue(deleted)
    }

    @Test
    fun testReadOnlyShowsCodeAndHidesEditingControls() {
        val block = EditorBlock.CodeBlock(id = "c4", language = "Kotlin", code = "fun main() {}")

        composeTestRule.setContent {
            NotesTakingAppTheme {
                CodeBlockCard(
                    block = block,
                    isEditable = false,
                    onUpdateCode = {},
                    onUpdateLanguage = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("editor_code_readonly_c4").assertIsDisplayed()
        composeTestRule.onNodeWithTag("editor_code_line_numbers_c4").assertIsDisplayed()
        composeTestRule.onNodeWithTag("editor_code_delete_btn_c4").assertDoesNotExist()
        composeTestRule.onNodeWithTag("editor_code_editor_c4").assertDoesNotExist()
    }
}
