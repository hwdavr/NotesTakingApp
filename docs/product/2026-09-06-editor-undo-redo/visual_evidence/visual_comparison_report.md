# Visual Comparison Evaluation Report
**Feature Directory**: `2026-09-06-editor-undo-redo`
**Threshold**: `0.95` (binding on golden-baseline regression comparisons; design-mockup comparisons are informational)
**Overall Status**: `PASS`

| Actual Screenshot | Reference Design | Gate Role | Matched Via | Similarity Score | Diff % | Diff Overlay | Status |
|---|---|---|---|---|---|---|---|
| `undo_redo_read_only_absent.png` | — | — | explicit-map(null) | — | — | — | **ANCHOR_ONLY** |
| `undo_redo_undo_enabled.png` | `mockup_note_editor_undo_redo.png` | informational | explicit-map | 0.8986 | 10.14% | [`undo_redo_undo_enabled_diff.png`](undo_redo_undo_enabled_diff.png) | **INFO** |
| `undo_redo_keyboard_visible.png` | `mockup_note_editor_undo_redo_keyboard.png` | informational | explicit-map | 0.8705 | 12.95% | [`undo_redo_keyboard_visible_diff.png`](undo_redo_keyboard_visible_diff.png) | **INFO** |
| `undo_redo_disabled_baseline.png` | `mockup_note_editor_undo_redo.png` | informational | explicit-map | 0.9231 | 7.69% | [`undo_redo_disabled_baseline_diff.png`](undo_redo_disabled_baseline_diff.png) | **INFO** |
| `undo_redo_redo_enabled.png` | `mockup_note_editor_undo_redo_redo_enabled.png` | informational | explicit-map | 0.9019 | 9.81% | [`undo_redo_redo_enabled_diff.png`](undo_redo_redo_enabled_diff.png) | **INFO** |
