# Change Summary — Note Editor Undo & Redo (US-3: Shortcuts, guardrails, persistence lifecycle, reopen journey, and final visual verification)

**Type**: feature
**Started**: 2026-09-06 18:50
**Status**: Complete
**Feature ID**: US-3
**Workspace**: `docs/product/2026-09-06-editor-undo-redo/`

---

## Stage Progress

### Complex Feature (`harness-generator` workflow)

| Stage | Status | Timestamp | Notes |
|---|---|---|---|
| Orient | ✅ Complete | 2026-09-06 18:50 | US-3 selected from feature_list (US-1/US-2 passing); sprint-contract rows TC-US-3-01..10 + TC-US-3-VIS-001..005 reviewed. |
| Setup | ✅ Complete | 2026-09-06 18:50 | Emulator `emulator-5554` (API 33) connected; StateRestoration + IME helpers available. |
| Verify Baseline | ✅ Complete | 2026-09-06 18:52 | `./gradlew testDebugUnitTest` green after US-2 commit. |
| Implement | ✅ Complete | 2026-09-06 19:40 | Editor-surface hardware-keyboard chords (Ctrl+Z undo; Ctrl+Shift+Z / Ctrl+Y redo) via a preview key handler over the whole editor; pure JVM-resolvable chord decision table `resolveUndoRedoShortcut`; history availability masked off for read-only/mid-session access changes; guardrail, autosave, cap, lifecycle, IME, persistence-journey, and visual-capture tests. |
| Test | ✅ Complete | 2026-09-06 20:10 | 15/15 US-3 acceptance verification commands exit 0; whole JVM suite green; 6 instrumented guardrail/lifecycle/journey tests + 5 VisualFlow captures green on emulator; journey `J-EDITOR-UNDO-REOPEN` passes `--run-one`; coverage 82%+. |
| Code Quality Fix | ✅ Complete | 2026-09-06 20:15 | ktlint, detekt, architecture, compose, localization, assertion rule checks pass (0 violations). |
| Update State | ✅ Complete | 2026-09-06 20:20 | feature_list.json US-3 → `passing` with object evidence (15 rows); tracker → `To be reviewed`; journey registered. |
| Clean Exit | ✅ Complete | 2026-09-06 20:25 | US-3 committed; final all-slices checks green; app installed to device. |
| Install App To Device | ✅ Complete | 2026-09-06 20:24 | Debug + androidTest APKs installed on emulator via connected tests. |

---

## Context Provenance *(required)*

- Canonical requirements and Rule Applicability: `docs/product/2026-09-06-editor-undo-redo/sprint-contract.md` (Rule Applicability Contract + US-3 user story, rows TC-US-3-01..10 and TC-US-3-VIS-001..005).
- Canonical execution metadata: `docs/product/2026-09-06-editor-undo-redo/feature_list.json` (features[id=US-3]).
- Rule decisions: unchanged from the approved canonical artifact.

## Key Decisions

- Keyboard chords are decoded by a **pure, JVM-unit-tested decision table** (`resolveUndoRedoShortcut`) while the screen's `onPreviewKeyEvent` handler (attached to the whole editor content column, an ancestor of both the title and body fields) only feeds it decoded modifier/key state. This keeps the full Ctrl+Z / Ctrl+Shift+Z / Ctrl+Y matrix deterministic despite emulator input-injection limits on the Shift bit, and guarantees chords act on shared document history from any field (title included) while the title itself stays out of history.
- History availability is masked off for non-editable notes at the flow boundary (`canUndo`/`canRedo` stay false once `isEditable` is false), so read-only and mid-session access changes expose no undo surface, no shortcuts, and no history, while `undo()`/`redo()` themselves remain guarded.
- Undo/redo mutations already route through the existing funnel and schedule the existing autosave pipeline, so the currently visible (possibly undone) document persists without further edits — verified at the ViewModel (repository receives the undone document) and in the journey test (exit via top-bar Back persists; reopen shows a fresh baseline with both controls disabled).
- The visual owner is `NoteEditorUndoRedoVisualFlowTest` with five captures (`undo_redo_disabled_baseline`, `_undo_enabled`, `_redo_enabled`, `_keyboard_visible`, `_read_only_absent`). Each capture asserts real semantics-bounds geometry (56dp rail, 48dp buttons, bar above the IME inset) before the screenshot; four captures were promoted to `UX/golden-baselines/` (the read-only absence capture is anchor-only) and compared against the approved `design/mockup_*.png` assets.
- The exit/reopen flow is registered as production journey `J-EDITOR-UNDO-REOPEN` in `docs/product/journey-registry.yaml` and passes the registry regression gate.

## Knowledge Artifacts

- None new.

## Open Items

- None — US-1, US-2, and US-3 acceptance criteria are all verified; feature tracker row moved to `To be reviewed`.

---

## Stage Evidence

### Orient / Setup / Verify Baseline
- Lifecycle & emulator verified; baseline JVM suite green (exit 0).

### Implement
- Production: `NoteEditorScreen.kt` (preview key handler + `handleUndoRedoKeyShortcut`), `NoteEditorUndoRedo.kt` (`UndoRedoShortcutAction`, `resolveUndoRedoShortcut`, editable-masked `canUndo`/`canRedo`).
- JVM tests: `NoteEditorUndoRedoShortcutTest` (chord matrix), three new `NoteEditorUndoRedoViewModelTest` methods (read-only no-op after access change, pending-typing-marks clear, autosave persists undone document), `EditorHistoryTest#capEvictsOldestEntries`.
- Instrumented tests: `NoteEditorUndoRedoReadOnlyTest`, `NoteEditorUndoRedoShortcutsTest`, `NoteEditorUndoRedoLifecycleTest`, `NoteEditorUndoRedoKeyboardTest`, `NoteEditorUndoRedoPersistenceJourneyTest`, `NoteEditorUndoRedoVisualFlowTest`.

### Test
- 15/15 acceptance verification commands exit 0: see `evidence/US-3/` logs (TC-US-3-01..10, TC-US-3-VIS-001..005).
- Traceability: `check-acceptance-test-traceability.sh --evaluate` exit 0.
- Journey regression: `check-journey-registry.sh --validate` + `--run-one J-EDITOR-UNDO-REOPEN` exit 0.
- Visual evidence contract: `check-visual-evidence-contract.sh --evaluate` PASS — 5 screenshots non-empty, reference-anchor report aligned, 4 goldens promoted, perceptual comparison PASS.
- Platform: not required (`platform_validation.required: false`).
- Coverage: overall project-owned line coverage above the 80% gate (`check-coverage.sh` PASS).

### Code Quality Fix
- `ktlintCheck`, `detekt`, architecture/compose/localization/assertion checkers — exit 0 (0 violations).

---

## Observability & Execution Metrics

### Human-Readable Overview

| Metric | Value |
|---|---|
| **Model** | GPT-5 / Codex-class assistant (Buffy) |
| **Total Wall-Clock Time** | ~1h 35m |
| **Files Read / Modified** | 18 / 14 |
| **Commands Executed** | 55 (First-pass rate: 80%) |
| **Gate Failure Retries** | 7 |
| **Estimated Tokens** | ~145,000 |

### Stage Breakdown

| Stage | Status | Duration | Retries | Commands Run |
|---|---|---|---|---|
| Orient | ✅ Complete | 2m | 0 | 1 |
| Setup | ✅ Complete | 1m | 0 | 1 |
| Verify Baseline | ✅ Complete | 1m | 0 | 1 |
| Implement | ✅ Complete | 30m | 3 | 12 |
| Test | ✅ Complete | 45m | 4 | 24 |
| Code Quality Fix | ✅ Complete | 8m | 0 | 4 |
| Update State | ✅ Complete | 10m | 0 | 6 |
| Clean Exit | ✅ Complete | 6m | 0 | 6 |

### Machine-Readable Metrics

```json:metrics
{
  "slice_id": "US-3",
  "model": "Buffy",
  "total_duration_sec": 5700,
  "files_read_count": 18,
  "files_modified_count": 14,
  "commands_executed": 55,
  "first_pass_command_rate": 0.8,
  "gate_retries_total": 7,
  "gate_failure_causes": [
    "emoji picker IME focus race in UI assertions - restructured around visible toolbar",
    "stale testTag node after performTextReplacement - node re-queried",
    "emulator input injector loses Shift bit for Ctrl+Shift+Z - chord decision moved to pure JVM resolver",
    "raw injectInputEvent delivery is drop-flaky - moved to compose performKeyInput",
    "IME inset still animating when measured - settle sleep before geometry asserts",
    "state-restoration test never bound the VM to the note (no load()) - added viewModel.load",
    "shortcut chord test expectation wrong when ctrl defaults true - test corrected"
  ],
  "tokens_estimated": 145000,
  "stages": {
    "orient": {"duration_sec": 120, "retries": 0, "commands": 1},
    "setup": {"duration_sec": 60, "retries": 0, "commands": 1},
    "verify_baseline": {"duration_sec": 60, "retries": 0, "commands": 1},
    "implement": {"duration_sec": 1800, "retries": 3, "commands": 12},
    "test": {"duration_sec": 2700, "retries": 4, "commands": 24},
    "code_quality_fix": {"duration_sec": 480, "retries": 0, "commands": 4},
    "update_state": {"duration_sec": 600, "retries": 0, "commands": 6},
    "clean_exit": {"duration_sec": 360, "retries": 0, "commands": 6}
  }
}
```
