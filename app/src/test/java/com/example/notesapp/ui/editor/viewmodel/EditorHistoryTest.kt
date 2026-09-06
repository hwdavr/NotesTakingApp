package com.example.notesapp.ui.editor.viewmodel

import com.example.notesapp.ui.editor.mapper.EditorBlock
import com.example.notesapp.ui.editor.mapper.NoteDocument
import com.example.notesapp.ui.editor.mapper.RichText
import com.example.notesapp.ui.editor.mapper.text
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class EditorHistoryTest {

    private fun block(id: String, text: String): EditorBlock.TextBlock =
        EditorBlock.TextBlock(id = id, children = listOf(RichText(text)))

    private fun documentOf(vararg blocks: EditorBlock): NoteDocument = NoteDocument(blocks = blocks.toList())

    private fun snapshot(document: NoteDocument, focus: String? = null): EditorSnapshot = EditorSnapshot(
        document = document,
        focusedBlockId = focus,
        selectionStart = 0,
        selectionEnd = 0,
        focusedTableCells = emptyMap()
    )

    private fun EditorHistory.mustUndo(current: EditorSnapshot): EditorSnapshot =
        performUndo(current) ?: error("Expected an undo step to be available")

    private fun EditorHistory.mustRedo(current: EditorSnapshot): EditorSnapshot =
        performRedo(current) ?: error("Expected a redo step to be available")

    private fun textOf(snapshot: EditorSnapshot): String =
        snapshot.document.blocks.filterIsInstance<EditorBlock.TextBlock>().joinToString("") { it.text() }

    @Test
    fun `baseline offers nothing to undo or redo`() {
        val history = EditorHistory()
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
        assertNull(history.performUndo(snapshot(documentOf(block("b0", "")))))
        assertNull(history.performRedo(snapshot(documentOf(block("b0", "")))))
    }

    @Test
    fun `undo returns to pre-edit state and redo re-applies it`() {
        val history = EditorHistory()
        history.commit(
            before = snapshot(documentOf(block("b0", ""))),
            typingKey = null,
            typingBlockId = null,
            nowMs = 0L
        )
        history.commit(
            before = snapshot(documentOf(block("b0", "a"))),
            typingKey = null,
            typingBlockId = null,
            nowMs = 10L
        )

        assertTrue(history.canUndo)
        val undoneFirst = history.mustUndo(snapshot(documentOf(block("b0", "ab"))))
        assertEquals("a", textOf(undoneFirst))
        assertTrue(history.canRedo)
        assertTrue(history.canUndo)

        val undoneBaseline = history.mustUndo(undoneFirst)
        assertEquals("", textOf(undoneBaseline))
        assertFalse(history.canUndo)

        val redoneFirst = history.mustRedo(undoneBaseline)
        assertEquals("a", textOf(redoneFirst))
        val redoneHead = history.mustRedo(redoneFirst)
        assertEquals("ab", textOf(redoneHead))
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun typingCommitsWithinWindowMergeIntoSingleStep() {
        val history = EditorHistory()
        val baseline = documentOf(block("b0", ""))
        history.commit(before = snapshot(baseline), typingKey = "block:b0", typingBlockId = "b0", nowMs = 0L)
        history.commit(
            before = snapshot(documentOf(block("b0", "a"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 50L
        )
        history.commit(
            before = snapshot(documentOf(block("b0", "ab"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 120L
        )

        // All commits merged into one top step: a single undo restores the pre-run baseline.
        val undone = history.mustUndo(snapshot(documentOf(block("b0", "abc"))))
        assertEquals(baseline, undone.document)
        assertFalse(history.canUndo)

        // The stack stays bounded at the declared capacity: beyond HISTORY_CAPACITY new steps,
        // the oldest entries are evicted and unreachable.
        repeat(HISTORY_CAPACITY + 2) { index ->
            history.commit(
                before = snapshot(documentOf(block("b0", "step$index"))),
                typingKey = null,
                typingBlockId = null,
                nowMs = index.toLong() + 200L
            )
        }
        var reachableSteps = 0
        var current = snapshot(documentOf(block("b0", "head")))
        while (history.canUndo) {
            current = history.mustUndo(current)
            reachableSteps++
        }
        assertEquals(HISTORY_CAPACITY, reachableSteps)
    }

    @Test
    fun `a pause longer than the coalescing window splits runs into separate steps`() {
        val history = EditorHistory(coalesceWindowMs = 1_000L)
        history.commit(
            before = snapshot(documentOf(block("b0", ""))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 0L
        )
        history.commit(
            before = snapshot(documentOf(block("b0", "a"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 100L
        )

        // Pause beyond the window, then keep typing in the same block.
        history.commit(
            before = snapshot(documentOf(block("b0", "a"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 2_000L
        )
        history.commit(
            before = snapshot(documentOf(block("b0", "ab"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 2_100L
        )

        var undone = history.mustUndo(snapshot(documentOf(block("b0", "abc"))))
        assertEquals("a", textOf(undone)) // removes only the second run
        assertTrue(history.canUndo)
        undone = history.mustUndo(undone)
        assertEquals("", textOf(undone)) // removes the first run
        assertFalse(history.canUndo)
    }

    @Test
    fun `typing in a different target never coalesces`() {
        val history = EditorHistory()
        val baseline = documentOf(block("b0", ""), block("b1", ""))
        history.commit(before = snapshot(baseline), typingKey = "block:b0", typingBlockId = "b0", nowMs = 0L)
        // First typing run: "a" lands in b0.
        history.commit(
            before = snapshot(documentOf(block("b0", "a"), block("b1", ""))),
            typingKey = "block:b1",
            typingBlockId = "b1",
            nowMs = 5L
        )
        // Second typing run in b1: "b", then "c" merges into it.
        history.commit(
            before = snapshot(documentOf(block("b0", "a"), block("b1", "b"))),
            typingKey = "block:b1",
            typingBlockId = "b1",
            nowMs = 8L
        )
        history.commit(
            before = snapshot(documentOf(block("b0", "a"), block("b1", "bc"))),
            typingKey = "block:b1",
            typingBlockId = "b1",
            nowMs = 12L
        )

        var undone = history.mustUndo(snapshot(documentOf(block("b0", "a"), block("b1", "bcd"))))
        assertEquals("a", textOf(undone)) // b1's run undone; b0 text intact
        assertTrue(history.canUndo)
        undone = history.mustUndo(undone)
        assertEquals(baseline, undone.document) // b0's run undone
        assertFalse(history.canUndo)
    }

    @Test
    fun `discrete action between typing runs produces matching undo steps`() {
        val history = EditorHistory()
        // typing run 1 -> "a"
        history.commit(
            before = snapshot(documentOf(block("b0", ""))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 0L
        )
        // discrete emoji insertion -> "aE"
        history.commit(
            before = snapshot(documentOf(block("b0", "a"))),
            typingKey = null,
            typingBlockId = null,
            nowMs = 5L
        )
        // typing run 2 -> "aEbc"
        history.commit(
            before = snapshot(documentOf(block("b0", "aE"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 10L
        )
        history.commit(
            before = snapshot(documentOf(block("b0", "aEb"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 12L
        )

        var undone = history.mustUndo(snapshot(documentOf(block("b0", "aEbc"))))
        assertEquals("aE", textOf(undone)) // whole typing run 2 removed
        undone = history.mustUndo(undone)
        assertEquals("a", textOf(undone)) // emoji step removed
        undone = history.mustUndo(undone)
        assertEquals("", textOf(undone)) // typing run 1 removed
        assertFalse(history.canUndo)
    }

    @Test
    fun `typing after an undo never merges and truncates the redo tail`() {
        val history = EditorHistory()
        history.commit(
            before = snapshot(documentOf(block("b0", ""))),
            typingKey = null,
            typingBlockId = null,
            nowMs = 0L
        )
        history.commit(
            before = snapshot(documentOf(block("b0", "a"))),
            typingKey = null,
            typingBlockId = null,
            nowMs = 5L
        )

        history.mustUndo(snapshot(documentOf(block("b0", "ab"))))
        assertTrue(history.canRedo)

        // New edit after undo: no merge (redo tail exists) and redo is discarded.
        history.commit(
            before = snapshot(documentOf(block("b0", "a"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 10L
        )
        assertFalse(history.canRedo)

        // The whole new typing run is one step.
        history.commit(
            before = snapshot(documentOf(block("b0", "ax"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 12L
        )
        val undone = history.mustUndo(snapshot(documentOf(block("b0", "axy"))))
        assertEquals("a", textOf(undone))
        assertTrue(history.canUndo)
    }

    @Test
    fun `history drops the oldest steps beyond its capacity`() {
        val capacity = 5
        val history = EditorHistory(capacity = capacity)
        repeat(capacity) { index ->
            history.commit(
                before = snapshot(documentOf(block("b0", "s$index"))),
                typingKey = null,
                typingBlockId = null,
                nowMs = index.toLong()
            )
        }
        history.commit(
            before = snapshot(documentOf(block("b0", "overflow"))),
            typingKey = null,
            typingBlockId = null,
            nowMs = capacity.toLong()
        )

        var steps = 0
        var current = snapshot(documentOf(block("b0", "last")))
        while (history.canUndo) {
            current = history.mustUndo(current)
            steps++
        }
        assertEquals(capacity, steps)
        // The dropped oldest entry is unreachable: the last undo restores "s1".
        assertEquals("s1", textOf(current))
    }

    @Test
    fun `reset clears both directions`() {
        val history = EditorHistory()
        history.commit(
            before = snapshot(documentOf(block("b0", ""))),
            typingKey = null,
            typingBlockId = null,
            nowMs = 0L
        )
        history.mustUndo(snapshot(documentOf(block("b0", "a"))))
        history.reset()
        assertFalse(history.canUndo)
        assertFalse(history.canRedo)
        assertNull(history.performUndo(snapshot(documentOf(block("b0", "a")))))
        assertNull(history.performRedo(snapshot(documentOf(block("b0", "a")))))
    }

    @Test
    fun `focus move to another block closes the open typing run`() {
        val history = EditorHistory()
        history.commit(
            before = snapshot(documentOf(block("b0", ""))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 0L
        )
        history.noteFocusMovedTo("b1")
        // Even immediately after the focus move, typing in b0 again is a new step.
        history.commit(
            before = snapshot(documentOf(block("b0", "a"))),
            typingKey = "block:b0",
            typingBlockId = "b0",
            nowMs = 2L
        )

        val undone = history.mustUndo(snapshot(documentOf(block("b0", "ab"))))
        assertEquals("a", textOf(undone))
        assertTrue(history.canUndo)
    }

    @Test
    fun `fallback focus resolves to the nearest surviving preceding block`() {
        val blocks = listOf("a", "b", "c").map { block(it, it) }
        // Focused block "c" is gone from the restored document; "b" survives.
        val restored = resolveFallbackFocus(
            restoredBlocks = blocks.take(2),
            previousBlocks = blocks,
            focusedBlockId = "c"
        )
        assertEquals("b", restored)

        // Focused block "a" gone with no surviving predecessor -> focus cleared.
        val cleared = resolveFallbackFocus(
            restoredBlocks = blocks.drop(1),
            previousBlocks = blocks,
            focusedBlockId = "a"
        )
        assertNull(cleared)

        // Existing block keeps its focus unchanged.
        val intact = resolveFallbackFocus(
            restoredBlocks = blocks,
            previousBlocks = blocks,
            focusedBlockId = "b"
        )
        assertEquals("b", intact)
    }
}
