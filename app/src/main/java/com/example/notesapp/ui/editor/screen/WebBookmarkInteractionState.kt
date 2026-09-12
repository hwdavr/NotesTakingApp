package com.example.notesapp.ui.editor.screen

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.notesapp.R
import com.example.notesapp.ui.editor.components.WebBookmarkActionsSheet
import com.example.notesapp.ui.editor.components.WebBookmarkDeleteConfirmationDialog
import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.viewmodel.NoteEditorUiState

class WebBookmarkInteractionState internal constructor(
    private val activeActionsBlockIdState: MutableState<String?>,
    private val pendingDeleteBlockIdState: MutableState<String?>,
    private val browserErrorState: MutableState<Boolean>
) {
    val activeActionsBlockId: String?
        get() = activeActionsBlockIdState.value
    val pendingDeleteBlockId: String?
        get() = pendingDeleteBlockIdState.value
    val showBrowserError: Boolean
        get() = browserErrorState.value

    fun openActions(blockId: String) {
        activeActionsBlockIdState.value = blockId
    }

    fun dismissActions() {
        activeActionsBlockIdState.value = null
    }

    fun requestDelete(blockId: String) {
        pendingDeleteBlockIdState.value = blockId
    }

    fun dismissDelete() {
        pendingDeleteBlockIdState.value = null
    }

    fun showBrowserErrorMessage() {
        browserErrorState.value = true
    }

    fun consumeBrowserError() {
        browserErrorState.value = false
    }
}

@Composable
fun rememberWebBookmarkInteractionState(): WebBookmarkInteractionState {
    val activeActionsBlockId = rememberSaveable { mutableStateOf<String?>(null) }
    val pendingDeleteBlockId = rememberSaveable { mutableStateOf<String?>(null) }
    val browserError = rememberSaveable { mutableStateOf(false) }
    return remember(activeActionsBlockId, pendingDeleteBlockId, browserError) {
        WebBookmarkInteractionState(activeActionsBlockId, pendingDeleteBlockId, browserError)
    }
}

@Composable
fun WebBookmarkInteractionState.RenderSnackbarHost() {
    val hostState = remember { SnackbarHostState() }
    val browserErrorMessage = stringResource(R.string.web_bookmark_browser_error)
    LaunchedEffect(showBrowserError) {
        if (showBrowserError) {
            hostState.showSnackbar(browserErrorMessage)
            consumeBrowserError()
        }
    }
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier.testTag("web_bookmark_error_feedback")
    )
}

@Composable
fun WebBookmarkActionsOverlay(
    state: NoteEditorUiState,
    interactionState: WebBookmarkInteractionState,
    onEdit: (String, String, String, String) -> Unit,
    onDeleteBlock: (String) -> Unit
) {
    state.document.blocks
        .firstOrNull { it.id == interactionState.activeActionsBlockId }
        ?.let { block ->
            if (block is EditorBlock.WebBookmarkBlock && state.isEditable) {
                WebBookmarkActionsSheet(
                    onDismiss = interactionState::dismissActions,
                    onEdit = {
                        interactionState.dismissActions()
                        onEdit(block.id, block.url, block.title, block.description)
                    },
                    onDelete = {
                        interactionState.dismissActions()
                        interactionState.requestDelete(block.id)
                    }
                )
            }
        }
    interactionState.pendingDeleteBlockId?.let { blockId ->
        WebBookmarkDeleteConfirmationDialog(
            onDismiss = interactionState::dismissDelete,
            onConfirm = {
                interactionState.dismissDelete()
                onDeleteBlock(blockId)
            }
        )
    }
}
