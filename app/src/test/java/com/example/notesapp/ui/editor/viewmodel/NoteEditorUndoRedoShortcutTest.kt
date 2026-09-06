package com.example.notesapp.ui.editor.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteEditorUndoRedoShortcutTest {

    private fun resolve(
        ctrl: Boolean = true,
        shift: Boolean = false,
        keyZ: Boolean = false,
        keyY: Boolean = false,
        canUndo: Boolean = true,
        canRedo: Boolean = true
    ): UndoRedoShortcutAction = resolveUndoRedoShortcut(
        isCtrlPressed = ctrl,
        isShiftPressed = shift,
        isKeyZ = keyZ,
        isKeyY = keyY,
        canUndo = canUndo,
        canRedo = canRedo
    )

    @Test
    fun ctrlZUndoesOnlyWhenUndoAvailable() {
        assertEquals(UndoRedoShortcutAction.Undo, resolve(keyZ = true))
        assertEquals(UndoRedoShortcutAction.None, resolve(keyZ = true, canUndo = false))
    }

    @Test
    fun ctrlShiftZRedoesOnlyWhenRedoAvailable() {
        assertEquals(UndoRedoShortcutAction.Redo, resolve(shift = true, keyZ = true))
        assertEquals(UndoRedoShortcutAction.None, resolve(shift = true, keyZ = true, canRedo = false))
        // Shift+Z takes precedence over plain Z undo when both directions exist.
        assertEquals(
            UndoRedoShortcutAction.Redo,
            resolve(shift = true, keyZ = true, canUndo = true, canRedo = true)
        )
    }

    @Test
    fun ctrlYRedoesOnlyWhenRedoAvailable() {
        assertEquals(UndoRedoShortcutAction.Redo, resolve(keyY = true))
        assertEquals(UndoRedoShortcutAction.None, resolve(keyY = true, canRedo = false))
    }

    @Test
    fun chordsWithoutCtrlOrOtherKeysAreIgnored() {
        assertEquals(UndoRedoShortcutAction.None, resolve(ctrl = false, keyZ = true))
        assertEquals(UndoRedoShortcutAction.None, resolve(ctrl = false, shift = true, keyZ = true))
        assertEquals(UndoRedoShortcutAction.None, resolve(ctrl = false, keyY = true))
        assertEquals(UndoRedoShortcutAction.None, resolve(keyZ = false, keyY = false))
    }

    @Test
    fun shiftWithoutZOrCtrlDoesNothing() {
        assertEquals(UndoRedoShortcutAction.None, resolve(shift = true))
        assertEquals(UndoRedoShortcutAction.None, resolve(ctrl = false, shift = true, keyY = true))
    }
}
