package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.model.TableFocusTarget
import kotlinx.coroutines.flow.MutableStateFlow

/** Maximum number of retained undo steps. Oldest entries are dropped first. */
internal const val HISTORY_CAPACITY = 100

/** Continuous typing commits arriving within this window into the same target coalesce into one step. */
internal const val HISTORY_COALESCE_WINDOW_MS = 1_000L

/**
 * Immutable editing context captured for one document state. Undo/redo restore the document plus the
 * focus/caret context that existed while that state was current, so reverting or replaying a change
 * returns the user to the block and offset where the change happened.
 */
internal data class EditorSnapshot(
    val document: NoteDocument,
    val focusedBlockId: String?,
    val selectionStart: Int,
    val selectionEnd: Int,
    val focusedTableCells: Map<String, TableFocusTarget>
) {
    companion object {
        fun of(state: NoteEditorUiState): EditorSnapshot = EditorSnapshot(
            document = state.document,
            focusedBlockId = state.focusedBlockId,
            selectionStart = state.selectionStart,
            selectionEnd = state.selectionEnd,
            focusedTableCells = state.focusedTableCells
        )
    }
}

/** Outcome of a keyboard shortcut chord. */
internal enum class UndoRedoShortcutAction { Undo, Redo, None }

/**
 * Pure decision table for the editor keyboard chords (Ctrl+Z undo, Ctrl+Shift+Z and Ctrl+Y redo).
 * Kept free of Android types so the full chord matrix is unit-testable on the JVM; the screen
 * handler feeds it the decoded key/modifier state from the actual [android KeyEvent]s.
 */
internal fun resolveUndoRedoShortcut(
    isCtrlPressed: Boolean,
    isShiftPressed: Boolean,
    isKeyZ: Boolean,
    isKeyY: Boolean,
    canUndo: Boolean,
    canRedo: Boolean
): UndoRedoShortcutAction {
    if (!isCtrlPressed) return UndoRedoShortcutAction.None
    return when {
        isKeyZ && isShiftPressed -> if (canRedo) UndoRedoShortcutAction.Redo else UndoRedoShortcutAction.None
        isKeyZ -> if (canUndo) UndoRedoShortcutAction.Undo else UndoRedoShortcutAction.None
        isKeyY -> if (canRedo) UndoRedoShortcutAction.Redo else UndoRedoShortcutAction.None
        else -> UndoRedoShortcutAction.None
    }
}

/**
 * Pure, in-memory, linear document history used by the Note Editor.
 *
 * Snapshot-based semantics: every undo step stores the document (and editing context) that was current
 * *before* the step's edit landed, so undoing pops back to the exact pre-edit state. Continuous typing
 * commits into the same target that arrive within [coalesceWindowMs] merge into the open run (no new
 * step is pushed); any discrete action, target change, focus move, pause beyond the window, undo, or
 * redo closes the run. A new edit after an undo truncates the redo tail. History is bounded by
 * [capacity], dropping the oldest steps. None of this state is ever persisted.
 */
internal class EditorHistory(
    private val capacity: Int = HISTORY_CAPACITY,
    private val coalesceWindowMs: Long = HISTORY_COALESCE_WINDOW_MS
) {
    private val undoSteps = ArrayList<EditorSnapshot>(capacity)
    private val redoSteps = ArrayList<EditorSnapshot>(capacity)
    private var openTypingKey: String? = null
    private var openTypingBlockId: String? = null
    private var lastTypingAtMs: Long = Long.MIN_VALUE

    val canUndo: Boolean
        get() = undoSteps.isNotEmpty()

    val canRedo: Boolean
        get() = redoSteps.isNotEmpty()

    fun reset() {
        undoSteps.clear()
        redoSteps.clear()
        closeRun()
    }

    /**
     * Records a document edit. [before] is the snapshot of the state that was current immediately before
     * the edit; [typingKey] (and its [typingBlockId]) is non-null only for continuous text commits so the
     * funnel can coalesce typing runs. [nowMs] is the commit clock time.
     */
    fun commit(before: EditorSnapshot, typingKey: String?, typingBlockId: String?, nowMs: Long) {
        val isTyping = typingKey != null
        val withinWindow = nowMs - lastTypingAtMs <= coalesceWindowMs
        val mergesIntoRun =
            isTyping && openTypingKey == typingKey && redoSteps.isEmpty() && withinWindow
        if (!mergesIntoRun) {
            undoSteps.add(before)
            if (undoSteps.size > capacity) {
                undoSteps.removeAt(0)
            }
            redoSteps.clear()
        }
        if (isTyping) {
            openTypingKey = typingKey
            openTypingBlockId = typingBlockId
            lastTypingAtMs = nowMs
        } else {
            closeRun()
        }
    }

    /** Closes the open typing run when focus moved away from the block it targets. */
    fun noteFocusMovedTo(focusedBlockId: String?) {
        if (openTypingKey != null && focusedBlockId != openTypingBlockId) {
            closeRun()
        }
    }

    /**
     * Moves the pointer one step back. The current snapshot becomes the redo target and the pre-edit
     * snapshot at the top of the undo stack is restored. Returns the restored snapshot or null when
     * nothing can be undone.
     */
    fun performUndo(current: EditorSnapshot): EditorSnapshot? {
        if (undoSteps.isEmpty()) return null
        val target = undoSteps.removeAt(undoSteps.lastIndex)
        redoSteps.add(current)
        closeRun()
        return target
    }

    /**
     * Moves the pointer one step forward, re-applying the most recently undone snapshot. Returns the
     * re-applied snapshot or null when nothing can be redone.
     */
    fun performRedo(current: EditorSnapshot): EditorSnapshot? {
        if (redoSteps.isEmpty()) return null
        val target = redoSteps.removeAt(redoSteps.lastIndex)
        undoSteps.add(current)
        if (undoSteps.size > capacity) {
            undoSteps.removeAt(0)
        }
        closeRun()
        return target
    }

    private fun closeRun() {
        openTypingKey = null
        openTypingBlockId = null
        lastTypingAtMs = Long.MIN_VALUE
    }
}

/**
 * State flow that owns the editor's [NoteEditorUiState] and funnels every document write through
 * [EditorHistory] so all document mutations (typing, blocks, tables, charts, code, mermaid, formulas,
 * links, emoji, voice, images) are recorded uniformly without per-action bookkeeping. It also derives
 * [NoteEditorUiState.canUndo]/[NoteEditorUiState.canRedo] synchronously so toolbar state stays in lock
 * step with history availability.
 */
internal class NoteEditorUndoRedoFlow(
    private val history: EditorHistory,
    private val delegate: MutableStateFlow<NoteEditorUiState>
) : MutableStateFlow<NoteEditorUiState> by delegate {

    constructor(
        history: EditorHistory = EditorHistory(),
        initial: NoteEditorUiState = NoteEditorUiState()
    ) : this(history, MutableStateFlow(initial))

    /** Clock used for coalescing decisions; overridable from tests. */
    internal var nowMs: () -> Long = { System.currentTimeMillis() }

    private var pendingTypingKey: String? = null

    /** Set false around internal document rewrites (e.g. link resolution) that are not user edits. */
    internal var recordUserCommits: Boolean = true

    /** Flags the next document-changing write as a continuous text commit in [key]. */
    fun beginTypingRun(key: String) {
        pendingTypingKey = key
    }

    /** Establishes the current state as a fresh history baseline (note load / reload). */
    fun resetBaseline() {
        history.reset()
        pendingTypingKey = null
        val current = delegate.value
        if (current.canUndo || current.canRedo) {
            delegate.value = current.copy(canUndo = false, canRedo = false)
        }
    }

    override var value: NoteEditorUiState
        get() = delegate.value
        set(newValue) {
            val old = delegate.value
            val typingKey = pendingTypingKey
            pendingTypingKey = null
            val docChanged = old.document != newValue.document
            val newCanUndo: Boolean
            val newCanRedo: Boolean
            when {
                // A note finished loading: the freshly loaded (or empty) document is the new baseline.
                !old.isLoaded && newValue.isLoaded -> {
                    history.reset()
                    newCanUndo = false
                    newCanRedo = false
                }
                old.isLoaded && newValue.isLoaded && docChanged && recordUserCommits -> {
                    history.commit(
                        before = EditorSnapshot.of(old),
                        typingKey = typingKey,
                        typingBlockId = newValue.focusedBlockId,
                        nowMs = nowMs()
                    )
                    newCanUndo = history.canUndo
                    newCanRedo = history.canRedo
                }
                old.isLoaded && newValue.isLoaded && docChanged -> {
                    // Internal rewrite (suppressed recording): no step, baseline shifts to the new doc.
                    newCanUndo = history.canUndo
                    newCanRedo = history.canRedo
                }
                else -> {
                    // Non-document writes: mirror focus moves so an open typing run closes when the
                    // user moves to a different block.
                    if (old.isLoaded && old.focusedBlockId != newValue.focusedBlockId) {
                        history.noteFocusMovedTo(newValue.focusedBlockId)
                    }
                    newCanUndo = history.canUndo
                    newCanRedo = history.canRedo
                }
            }
            // History availability only surfaces on editable notes: read-only/mid-session access
            // changes expose no undo surface and no shortcuts (guards stay in undo()/redo()).
            val visibleCanUndo = newCanUndo && newValue.isEditable
            val visibleCanRedo = newCanRedo && newValue.isEditable
            delegate.value = if (newValue.canUndo == visibleCanUndo && newValue.canRedo == visibleCanRedo) {
                newValue
            } else {
                newValue.copy(canUndo = visibleCanUndo, canRedo = visibleCanRedo)
            }
        }

    /** Reverts the most recent document step; returns false when nothing was undone (or note read-only). */
    fun undo(): Boolean {
        val current = delegate.value
        if (!current.isLoaded || !current.isEditable) return false
        val restored = history.performUndo(EditorSnapshot.of(current)) ?: return false
        applyRestored(current, restored)
        return true
    }

    /** Re-applies the most recently undone step; returns false when nothing can be redone. */
    fun redo(): Boolean {
        val current = delegate.value
        if (!current.isLoaded || !current.isEditable) return false
        val restored = history.performRedo(EditorSnapshot.of(current)) ?: return false
        applyRestored(current, restored)
        return true
    }

    private fun applyRestored(current: NoteEditorUiState, restored: EditorSnapshot) {
        val fallbackFocus = resolveFallbackFocus(
            restoredBlocks = restored.document.blocks,
            previousBlocks = current.document.blocks,
            focusedBlockId = restored.focusedBlockId
        )
        val focusIntact = restored.focusedBlockId != null && restored.focusedBlockId == fallbackFocus
        delegate.value = current.copy(
            document = restored.document,
            focusedBlockId = fallbackFocus,
            selectionStart = if (focusIntact) restored.selectionStart else 0,
            selectionEnd = if (focusIntact) restored.selectionEnd else 0,
            focusedTableCells = restored.focusedTableCells.filterKeys { blockId ->
                restored.document.blocks.any { block -> block.id == blockId }
            },
            pendingTypingMarks = emptySet(),
            canUndo = history.canUndo,
            canRedo = history.canRedo
        )
    }
}

/**
 * When an undo/redo snapshot's focused block no longer exists in the restored document, focus falls
 * back to the nearest block preceding it in the pre-change layout that survived the undo; if there is
 * no such block, focus is cleared.
 */
internal fun resolveFallbackFocus(
    restoredBlocks: List<EditorBlock>,
    previousBlocks: List<EditorBlock>,
    focusedBlockId: String?
): String? {
    if (focusedBlockId == null) return null
    if (restoredBlocks.any { it.id == focusedBlockId }) return focusedBlockId
    val previousIndex = previousBlocks.indexOfFirst { it.id == focusedBlockId }
    if (previousIndex < 0) return null
    for (candidate in previousBlocks.take(previousIndex).asReversed()) {
        if (restoredBlocks.any { it.id == candidate.id }) return candidate.id
    }
    return null
}
