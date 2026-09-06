package com.example.notesapp.ui.editor.screen

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.example.notesapp.ui.editor.viewmodel.UndoRedoShortcutAction
import com.example.notesapp.ui.editor.viewmodel.resolveUndoRedoShortcut

/**
 * Decodes a hardware-keyboard chord and, when it is an undo/redo shortcut on an editable note with
 * a matching available action, invokes the callback and reports the event as consumed. Kept in its
 * own file so the chord logic stays out of the large editor screen composable and the pure decision
 * table stays JVM-unit-tested in [resolveUndoRedoShortcut].
 */
internal fun consumeUndoRedoShortcut(
    keyEvent: KeyEvent,
    editable: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit
): Boolean {
    if (!editable || keyEvent.type != KeyEventType.KeyUp) return false
    return when (
        resolveUndoRedoShortcut(
            isCtrlPressed = keyEvent.isCtrlPressed || keyEvent.isMetaPressed,
            isShiftPressed = keyEvent.isShiftPressed,
            isKeyZ = keyEvent.key == Key.Z,
            isKeyY = keyEvent.key == Key.Y,
            canUndo = canUndo,
            canRedo = canRedo
        )
    ) {
        UndoRedoShortcutAction.Undo -> {
            onUndo()
            true
        }
        UndoRedoShortcutAction.Redo -> {
            onRedo()
            true
        }
        UndoRedoShortcutAction.None -> false
    }
}
