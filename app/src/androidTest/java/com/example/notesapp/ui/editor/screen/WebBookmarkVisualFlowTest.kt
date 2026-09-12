package com.example.notesapp.ui.editor.screen

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.notesapp.ui.editor.components.WebBookmarkBlockCard
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState
import com.example.notesapp.ui.editor.viewmodel.WebBookmarkEditorUiState
import com.example.notesapp.ui.theme.NotesTakingAppTheme
import kotlin.math.min
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebBookmarkVisualFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun capturesBookmarkCardState() {
        composeRule.setContent {
            NotesTakingAppTheme {
                WebBookmarkBlockCard(
                    block = bookmark(),
                    isEditable = true,
                    onOpen = {},
                    onMore = {},
                    modifier = Modifier.width(360.dp)
                )
            }
        }
        composeRule.waitForIdle()
        saveCapture(
            "web_bookmark_card_content.png",
            composeRule.onNodeWithTag("editor_web_bookmark_card_visual_${bookmark().id}").captureToImage()
        )
    }

    @Test
    fun capturesAddBookmarkPageState() {
        composeRule.setContent {
            NotesTakingAppTheme {
                WebBookmarkEditorScreenContent(
                    uiState = WebBookmarkEditorUiState(),
                    onUrlChanged = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }
        composeRule.waitForIdle()
        saveCapture(
            "web_bookmark_add_page_content_only.png",
            composeRule.onNodeWithTag("web_bookmark_editor_page").captureToImage()
        )
    }

    @Test
    fun capturesAddBookmarkPageKeyboardState() {
        composeRule.setContent {
            NotesTakingAppTheme {
                WebBookmarkEditorScreenContent(
                    uiState = WebBookmarkEditorUiState(url = "https://example.com"),
                    onUrlChanged = {},
                    onSave = {},
                    onBack = {}
                )
            }
        }
        composeRule.onNodeWithTag("web_bookmark_url_field").performClick()
        composeRule.waitForIdle()
        saveCapture(
            "web_bookmark_add_page_keyboard.png",
            composeRule.onNodeWithTag("web_bookmark_editor_page").captureToImage()
        )
    }

    @Test
    fun capturesBookmarkActionsSheetState() {
        composeRule.setContent {
            NotesTakingAppTheme {
                NoteEditorScreenContent(
                    parentPadding = PaddingValues(0.dp),
                    noteId = "note-visual",
                    state = NoteEditorUiState(
                        noteId = "note-visual",
                        document = NoteDocument(blocks = listOf(bookmark())),
                        isLoaded = true,
                        isEditable = true
                    ),
                    onBack = {},
                    onShareRequested = {},
                    onDelete = {},
                    onTitleChange = {},
                    onRename = {},
                    onToggleFavorite = {},
                    onMoveNote = {},
                    onExportNote = {},
                    onOpenVoiceRecorder = { _, _ -> },
                    onTextBlockChange = { _, _ -> },
                    onToggleCheckbox = {},
                    onToggleCheckboxChecked = {},
                    onToggleMark = { _, _ -> },
                    onAddImage = {},
                    onEmojiSelected = {},
                    onEmojiQueryChange = {},
                    onEmojiClearQuery = {},
                    onEmojiCategorySelected = {},
                    onEmojiSkinToneRequested = {},
                    onEmojiSkinToneDismissed = {},
                    onImageChange = { _, _, _ -> },
                    onAddTable = {},
                    onTableCellChange = { _, _, _, _ -> },
                    onFolderSelected = {},
                    onToggleFormattingToolbar = {},
                    onBlockFocused = {},
                    onSelectionChange = { _, _ -> },
                    onDeleteBlock = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("editor_web_bookmark_actions_${bookmark().id}").performClick()
        composeRule.onNodeWithTag("web_bookmark_actions_sheet_title").assertIsDisplayed()
        Thread.sleep(500)
        composeRule.onNodeWithTag("web_bookmark_actions_edit").captureToImage()
        saveActiveWindowCapture("web_bookmark_actions_sheet.png")
    }

    @Test
    fun supportsDarkRtlAndLargeTextWithoutClipping() {
        var darkMode by mutableStateOf(true)
        composeRule.setContent {
            NotesTakingAppTheme(darkTheme = darkMode) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (darkMode) LayoutDirection.Rtl else LayoutDirection.Ltr,
                    androidx.compose.ui.platform.LocalDensity provides Density(
                        1f,
                        if (darkMode) 1.5f else 1f
                    )
                ) {
                    WebBookmarkBlockCard(
                        block = bookmark(),
                        isEditable = true,
                        onOpen = {},
                        onMore = {},
                        modifier = Modifier.width(300.dp)
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val darkImage = composeRule
            .onNodeWithTag("editor_web_bookmark_card_visual_${bookmark().id}")
            .captureToImage()
        val darkBounds = composeRule
            .onNodeWithTag("editor_web_bookmark_card_visual_${bookmark().id}")
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(darkBounds.width > 0f && darkBounds.height > 0f)

        composeRule.runOnIdle { darkMode = false }
        composeRule.waitForIdle()
        val lightImage = composeRule
            .onNodeWithTag("editor_web_bookmark_card_visual_${bookmark().id}")
            .captureToImage()
        assertTrue(differingPixelCount(darkImage, lightImage) > 0)
        saveCapture("web_bookmark_responsive.png", darkImage)
    }

    private fun saveActiveWindowCapture(fileName: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        try {
            writeBitmapCapture(fileName, bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private fun saveCapture(fileName: String, image: ImageBitmap) {
        val bitmap = image.asAndroidBitmap()
        try {
            writeBitmapCapture(fileName, bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private fun writeBitmapCapture(fileName: String, bitmap: Bitmap) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Unable to create visual evidence item $fileName in $DOWNLOAD_CAPTURE_DIRECTORY")
        try {
            context.contentResolver.openOutputStream(uri).use { output ->
                checkNotNull(output)
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "Unable to encode visual evidence item $fileName"
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.update(
                    uri,
                    ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) },
                    null,
                    null
                )
            }
        } catch (error: Exception) {
            context.contentResolver.delete(uri, null, null)
            throw error
        }
    }

    private fun differingPixelCount(first: ImageBitmap, second: ImageBitmap): Int {
        val firstBitmap = first.asAndroidBitmap()
        val secondBitmap = second.asAndroidBitmap()
        val width = min(firstBitmap.width, secondBitmap.width)
        val height = min(firstBitmap.height, secondBitmap.height)
        var differences = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (firstBitmap.getPixel(x, y) != secondBitmap.getPixel(x, y)) differences++
            }
        }
        return differences
    }

    private fun bookmark() = EditorBlock.WebBookmarkBlock(
        id = "bookmark-visual-1",
        url = "https://example.com/visual",
        title = "Visual bookmark title",
        description = "Readable description across approved layouts"
    )

    private companion object {
        const val DOWNLOAD_CAPTURE_DIRECTORY = "/sdcard/Download/"
    }
}
