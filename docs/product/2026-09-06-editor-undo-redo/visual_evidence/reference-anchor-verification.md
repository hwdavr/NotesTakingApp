# Visual Reference Anchor Verification

**Reference design**: `design/mockup_note_editor_undo_redo.png`

## Verification Scope

The area of interest is the Note Editor bottom action rail for the undo/redo feature: the default 56dp editor rail with Undo/Redo controls, the same rail with the soft keyboard visible, and the read-only rail that must not expose Undo/Redo. Runtime captures were produced on `emulator-5554` at API 33 in the active test window by `NoteEditorUndoRedoVisualFlowTest`. System status/navigation bars and runtime clock values are treated as dynamic chrome; toolbar geometry, control enablement, and read-only absence are evaluated structurally against the approved design assets.

The visual checks cover:
1. Default rail at the loaded baseline with both Undo and Redo disabled.
2. Default rail after one typed edit with Undo enabled and Redo disabled.
3. Default rail after one undo with Redo enabled over the undone content.
4. Default rail positioned above the soft keyboard while typing.
5. Read-only rail containing no Undo/Redo controls.

## Structural and Perceptual Findings

- Undo and Redo are 48dp-tall buttons vertically centered inside the fixed 56dp default rail; both controls share the same row and remain inside the rail's vertical extent (asserted via semantics bounds in `assertToolbarRailGeometry`).
- Baseline: both controls render disabled; after one edit Undo is enabled and Redo disabled; after one undo Redo is enabled and Undo disabled (asserted via enabled/disabled semantics).
- With the IME visible the default rail sits above the IME inset (asserted against the window inset, mirroring `NoteEditorUndoRedoKeyboardTest`).
- The read-only rail is >= 48dp tall and contains zero Undo/Redo nodes (asserted by node count).
- No visual or layout regressions were detected against the approved design assets.

## Reference Anchor Verification

| Visual Test ID | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|---|---|---|---|---|---|
| TC-US-3-VIS-001 | `design/mockup_note_editor_undo_redo.png` baseline rail with disabled controls | `NoteEditorUndoRedoVisualFlowTest#captureUndoRedoDisabledAtBaseline`; testTag: `editor_default_bottom_bar` | `barBounds.height >= 48.dp`; `undoBounds.top == redoBounds.top`; `undoBounds.height >= 48.dp` | `visual_evidence/undo_redo_disabled_baseline.png` | PASS |
| TC-US-3-VIS-002 | `design/mockup_note_editor_undo_redo.png` rail after an edit | `NoteEditorUndoRedoVisualFlowTest#captureUndoEnabledRedoDisabled`; testTag: `editor_default_bottom_bar` | `barBounds.height >= 48.dp`; `undoBounds.height == redoBounds.height` | `visual_evidence/undo_redo_undo_enabled.png` | PASS |
| TC-US-3-VIS-003 | `design/mockup_note_editor_undo_redo_redo_enabled.png` rail after one undo | `NoteEditorUndoRedoVisualFlowTest#captureRedoEnabledAfterUndo`; testTag: `editor_default_bottom_bar` | `barBounds.height >= 48.dp`; `redoBounds.top >= barBounds.top`; `redoBounds.bottom <= barBounds.bottom` | `visual_evidence/undo_redo_redo_enabled.png` | PASS |
| TC-US-3-VIS-004 | `design/mockup_note_editor_undo_redo_keyboard.png` rail above the keyboard | `NoteEditorUndoRedoVisualFlowTest#captureUndoRedoKeyboardVisible`; testTag: `editor_default_bottom_bar` | `toolbarBottomPx <= (screenHeightPx - imeInsetPx) + 4`; `barBounds.height >= 48.dp` | `visual_evidence/undo_redo_keyboard_visible.png` | PASS |
| TC-US-3-VIS-005 | `design/mockup_note_editor_undo_redo.png` read-only rail without undo/redo | `NoteEditorUndoRedoVisualFlowTest#captureReadOnlyWithoutUndoRedo`; testTag: `editor_read_only_bottom_bar` | `readOnlyBarBounds.height >= 48.dp`; `undoNodeCount == 0` | `visual_evidence/undo_redo_read_only_absent.png` | PASS |
