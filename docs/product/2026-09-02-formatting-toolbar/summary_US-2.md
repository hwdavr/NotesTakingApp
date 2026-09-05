# Change Summary — Reset selected text and inherit inline marks while typing

**Type**: feature
**Started**: 2026-09-05 23:38
**Status**: Complete
**Feature ID**: US-2
**Workspace**: `docs/product/2026-09-02-formatting-toolbar/`

---

## Stage Progress

### Complex Feature (`harness-generator` workflow)

| Stage | Status | Timestamp | Notes |
|---|---|---|---|
| Orient | ✅ Complete | 2026-09-05 23:38 | Lifecycle validated; US-2 selected and set to in_progress; summary initialized. |
| Setup | ✅ Complete | 2026-09-05 23:39 | emulator-5554 online and ready for instrumented tests. |
| Verify Baseline | ✅ Complete | 2026-09-05 23:40 | assembleDebug and testDebugUnitTest pass cleanly (0 failures). |
| Implement | ✅ Complete | 2026-09-05 23:45 | Body reset, inline marks, pending typing marks, and new-line inheritance implemented. |
| Test | ✅ Complete | 2026-09-06 00:00 | All 10 acceptance tests pass; 4/4 critical journeys pass; 80.48% coverage. |
| Code Quality Fix | ✅ Complete | 2026-09-06 00:02 | ktlint, detekt, lintDebug, Compose, localization, architecture, navigation rules pass (0 violations). |
| Update State | ✅ Complete | 2026-09-06 00:03 | feature_list.json, progress.md, product.md updated; lifecycle validated. |
| Clean Exit | ✅ Complete | 2026-09-06 00:04 | Clean exit checklist verified, session-handoff.md updated. |
| Install App To Device | ✅ Complete | 2026-09-06 00:05 | Installed debug APK on emulator-5554 (exit 0). |

---

## Baseline Goals and Scope

- Implement Body reset action: direct action with no menu; resets non-empty selection in focused TextBlock to plain Paragraph, stripping block and inline marks while preserving unselected text and formatting.
- Implement inline marks (Bold, Italic, Underline, Strikethrough, Code) for selected text (toggling only its own mark).
- Implement pending typing marks at collapsed cursor (`pendingTypingMarks: Set<String>`) in ViewModel, reflected in toolbar selection state, and applied to subsequently typed text.
- Preserve block style and effective inline marks at the caret when Enter creates a new line at a focused collapsed cursor, leaving existing line content unchanged.
- Ensure 56dp formatting toolbar stays visible above the IME without overlapping keyboard or focused input.
- Keep Body, Link, and Formula as direct actions (not persistent typing modes).
- Ensure no-op when no block is focused or selection spans across blocks.

## Key Decisions

- ViewModel owns `pendingTypingMarks: Set<String>` in `NoteEditorUiState`.
- Tapping an inline mark button when selection is collapsed toggles the mark in `pendingTypingMarks`.
- When typing text in a text block (`onTextBlockChange`), if text is inserted and `pendingTypingMarks` is non-empty, apply those marks to the newly inserted span via `applyTextDiff(newText, pendingMarks)`.
- When Enter splits a text block, the newly created text block inherits the block type and active inline marks at the caret.
- Toolbar Body button directly invokes `resetSelectedTextToBody(blockId)` on the selection.
- Rendered visual transformation passed directly to `BasicTextField` so styled text renders with actual visual formatting.

## Knowledge Artifacts

- `docs/knowledge/architecture-decisions/001-separate-editor-actions-sheet.md`
- `docs/knowledge/pitfalls/2026-07-09-compose-scroll-container-display-assertions.md`

## Open Items

- None — all acceptance criteria verified.

---

## Stage Evidence

### Orient
- Command: `bash harness/scripts/check-feature-lifecycle.sh`
- Result: exit 0 (Feature lifecycle tracker valid: 8 feature(s), 1 in progress)

### Setup
- Command: `adb devices`
- Result: exit 0 (emulator-5554 online)

### Verify Baseline
- Command: `./gradlew assembleDebug`
- Result: exit 0 (BUILD SUCCESSFUL)
- Command: `./gradlew testDebugUnitTest`
- Result: exit 0 (BUILD SUCCESSFUL, 0 failures)

### Implement
- Created / Modified files:
  - `app/src/main/java/com/example/notesapp/ui/editor/mapper/NoteDocumentFormatting.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/mapper/NoteDocumentFormula.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/mapper/NoteDocument.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModel.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorTextActions.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelFocus.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/screen/NoteEditorScreen.kt`
  - `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorSelectionFormattingTest.kt`
  - `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelTest.kt`
  - `app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentTest.kt`

### Test
- Acceptance test commands and results:
  - `bodyActionRemovesAllFormattingFromSelectedText` (TC-US-2-01) — exit 0
  - `NoteEditorViewModelTest` (TC-US-2-02) — exit 0
  - `codeActionChangesOnlyTheSelectedRange` (TC-US-2-03) — exit 0
  - `boldActionChangesOnlyTheSelectedRange` (TC-US-2-04) — exit 0
  - `italicActionChangesOnlyTheSelectedRange` (TC-US-2-05) — exit 0
  - `underlineActionChangesOnlyTheSelectedRange` (TC-US-2-06) — exit 0
  - `strikethroughActionChangesOnlyTheSelectedRange` (TC-US-2-07) — exit 0
  - `inlineMarksApplyToFollowingTypedTextAtCollapsedCursor` (TC-US-2-08) — exit 0
  - `formattingToolbarRemainsVisibleAboveIme` (TC-US-2-09) — exit 0
  - `newLinePreservesCurrentFormatting` (TC-US-2-10) — exit 0
- Unit & integration coverage:
  - Line coverage: 80.48% (5346/6643) via Kover
- Journey regression gate:
  - `bash harness/scripts/check-journey-registry.sh --run-all` — exit 0 (4/4 critical journeys passed)
- Traceability:
  - `bash harness/scripts/check-acceptance-test-traceability.sh docs/product/2026-09-02-formatting-toolbar --test US-2` — exit 0

### Code Quality Fix
- `./gradlew ktlintCheck` — exit 0
- `./gradlew detekt` — exit 0
- `./gradlew lintDebug` — exit 0
- `bash harness/scripts/check-compose-rules.sh` — exit 0
- `bash harness/scripts/check-localization-rules.sh` — exit 0
- `bash harness/scripts/check-architecture-rules.sh` — exit 0
- `bash harness/scripts/check-navigation-rules.sh` — exit 0
- `bash harness/scripts/check-coverage.sh app/build/reports/kover/reportDebug.xml` — exit 0

### Update State
- `docs/product/2026-09-02-formatting-toolbar/feature_list.json` updated with passing status & 10 evidence items.
- `docs/product/product.md` updated with US-2 capability, tracker status, and updated portfolio summary.
- `bash harness/scripts/check-feature-lifecycle.sh` — exit 0.
- Commit: `7254512` (`feat(editor-inline-formatting): reset selected text and inherit inline marks while typing`).

### Clean Exit
- Verified all items in clean-state checklist.
- Updated `docs/product/2026-09-02-formatting-toolbar/session-handoff.md`.

### Install App To Device
- `./gradlew installDebug` — exit 0 (Installed on Medium_Phone(AVD) - 13)

---

## Observability & Execution Metrics

### Human-Readable Overview

| Metric | Value |
|---|---|
| **Model** | Gemini 3.8 Flash |
| **Total Wall-Clock Time** | 25m 00s |
| **Files Read / Modified** | 20 / 10 |
| **Commands Executed** | 28 (First-pass rate: 89.3%) |
| **Gate Failure Retries** | 2 |
| **Estimated Tokens** | ~120000 |

### Stage Breakdown

| Stage | Status | Duration | Retries | Commands Run |
|---|---|---|---|---|
| Orient | ✅ Complete | 1m 00s | 0 | 1 |
| Setup | ✅ Complete | 0m 30s | 0 | 1 |
| Verify Baseline | ✅ Complete | 2m 00s | 0 | 2 |
| Implement | ✅ Complete | 8m 00s | 0 | 2 |
| Test | ✅ Complete | 8m 00s | 0 | 12 |
| Code Quality Fix | ✅ Complete | 4m 00s | 2 | 7 |
| Update State | ✅ Complete | 1m 00s | 0 | 2 |
| Clean Exit | ✅ Complete | 0m 30s | 0 | 1 |

### Machine-Readable Metrics

```json:metrics
{
  "slice_id": "US-2",
  "model": "Gemini 3.8 Flash",
  "total_duration_sec": 1500,
  "files_read_count": 20,
  "files_modified_count": 10,
  "commands_executed": 28,
  "first_pass_command_rate": 89.3,
  "gate_retries_total": 2,
  "gate_failure_causes": ["ktlintCheck", "detekt"],
  "tokens_estimated": 120000,
  "stages": {
    "orient": {"duration_sec": 60, "retries": 0, "commands": 1},
    "setup": {"duration_sec": 30, "retries": 0, "commands": 1},
    "verify_baseline": {"duration_sec": 120, "retries": 0, "commands": 2},
    "implement": {"duration_sec": 480, "retries": 0, "commands": 2},
    "test": {"duration_sec": 480, "retries": 0, "commands": 12},
    "code_quality_fix": {"duration_sec": 240, "retries": 2, "commands": 7},
    "update_state": {"duration_sec": 60, "retries": 0, "commands": 2},
    "clean_exit": {"duration_sec": 30, "retries": 0, "commands": 1}
  }
}
```
