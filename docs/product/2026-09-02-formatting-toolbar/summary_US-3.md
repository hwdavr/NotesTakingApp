# Change Summary — Keep long formula previews usable above the keyboard

**Type**: feature
**Started**: 2026-09-06 00:04
**Status**: Complete
**Feature ID**: US-3
**Workspace**: `docs/product/2026-09-02-formatting-toolbar/`

---

## Stage Progress

### Complex Feature (`harness-generator` workflow)

| Stage | Status | Timestamp | Notes |
|---|---|---|---|
| Orient | ✅ Complete | 2026-09-06 00:05 | Lifecycle validated; US-3 selected and marked in_progress. |
| Setup | ✅ Complete | 2026-09-06 00:06 | emulator-5554 online and ready for instrumented tests. |
| Verify Baseline | ✅ Complete | 2026-09-06 00:07 | assembleDebug and testDebugUnitTest pass cleanly (0 failures). |
| Implement | ✅ Complete | 2026-09-06 00:10 | FormulaEditorSheet bounded layout, imePadding, and horizontal preview scroll implemented. |
| Test | ✅ Complete | 2026-09-06 00:15 | All 3 acceptance tests pass; 4/4 critical journeys pass; 80.46% coverage. |
| Code Quality Fix | ✅ Complete | 2026-09-06 00:18 | ktlint, detekt, lintDebug, Compose, localization, architecture, navigation rules pass (0 violations). |
| Update State | ✅ Complete | 2026-09-06 00:20 | feature_list.json, progress.md, product.md updated; lifecycle validated. |
| Clean Exit | ✅ Complete | 2026-09-06 00:21 | Clean exit checklist verified, session-handoff.md updated. |
| Install App To Device | ✅ Complete | 2026-09-06 00:22 | Installed debug APK on emulator-5554 (exit 0). |

---

## Baseline Goals and Scope

- Harden the production formula sheet delivered in US-1 for long source and rendered previews.
- Ensure the formula preview is bounded, non-wrapping, non-clipping, and horizontally scrollable when the rendered formula exceeds viewport width.
- Ensure the sheet remains open and bounded above the keyboard/IME insets; source input, preview, Cancel button, and Insert/Update action remain reachable.
- Ensure the editor formatting toolbar is absent/not rendered behind the open formula sheet.
- Normalize formula sheet submit action to exactly one text-only button: "Insert" in new formula mode, "Update" in edit mode, with no plus-icon variant.

## Key Decisions

- Use `Modifier.horizontalScroll(rememberScrollState())` on the preview container, bound height with `heightIn(min = 56.dp, max = 120.dp)`, and set `softWrap = false` on the preview Text node.
- Apply `.navigationBarsPadding().imePadding()` before `.heightIn(max = 620.dp).verticalScroll(rememberScrollState())` on the modal bottom sheet container so the scrollable area is properly bounded within the visible screen area above the IME keyboard.
- In `NoteEditorScreen.kt`, `if (state.formulaSheet != null) return` prevents the editor bottom bar / formatting toolbar from rendering behind the formula sheet.
- Submit button uses standard text-only buttons with localized string resources `editor_formula_insert` or `editor_formula_update`, without any plus icon.

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
  - `app/src/main/java/com/example/notesapp/ui/editor/components/FormulaEditorSheet.kt`
  - `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorFormulaSheetResponsiveTest.kt`

### Test
- Acceptance test commands and results:
  - `longFormulaPreviewScrollsHorizontallyWithoutWrapping` (TC-US-3-01) — exit 0
  - `formulaSheetRemainsOpenAndActionsReachableAboveIme` (TC-US-3-02) — exit 0
  - `formulaSheetUsesSingleTextOnlyInsertAction` (TC-US-3-03) — exit 0
- Unit & integration coverage:
  - Line coverage: 80.46% (5345/6643) via Kover
- Journey regression gate:
  - `bash harness/scripts/check-journey-registry.sh --run-all` — exit 0 (4/4 critical journeys passed)
- Traceability:
  - `bash harness/scripts/check-acceptance-test-traceability.sh docs/product/2026-09-02-formatting-toolbar --test US-3` — exit 0
- Platform evaluation:
  - `bash harness/scripts/check-platform-evidence.sh docs/product/2026-09-02-formatting-toolbar --evaluate --slice US-3` — exit 0

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
- `docs/product/2026-09-02-formatting-toolbar/feature_list.json` updated with passing status & 3 evidence items.
- `docs/product/2026-09-02-formatting-toolbar/progress.md` updated with Session 005.
- `docs/product/product.md` updated with US-3 capability, tracker status, and updated portfolio summary.
- `bash harness/scripts/check-feature-lifecycle.sh` — exit 0.

### Clean Exit
- Verified all items in clean-state checklist.
- Updated `docs/product/2026-09-02-formatting-toolbar/session-handoff.md`.

### Install App To Device
- `./gradlew installDebug` — exit 0 (Installed on emulator-5554)

---

## Observability & Execution Metrics

### Human-Readable Overview

| Metric | Value |
|---|---|
| **Model** | Gemini 3.8 Flash |
| **Total Wall-Clock Time** | 18m 00s |
| **Files Read / Modified** | 12 / 6 |
| **Commands Executed** | 22 (First-pass rate: 95.5%) |
| **Gate Failure Retries** | 1 |
| **Estimated Tokens** | ~95000 |

### Stage Breakdown

| Stage | Status | Duration | Retries | Commands Run |
|---|---|---|---|---|
| Orient | ✅ Complete | 1m 00s | 0 | 1 |
| Setup | ✅ Complete | 0m 30s | 0 | 1 |
| Verify Baseline | ✅ Complete | 1m 30s | 0 | 2 |
| Implement | ✅ Complete | 4m 00s | 0 | 2 |
| Test | ✅ Complete | 6m 00s | 1 | 8 |
| Code Quality Fix | ✅ Complete | 3m 00s | 0 | 5 |
| Update State | ✅ Complete | 1m 00s | 0 | 2 |
| Clean Exit | ✅ Complete | 0m 30s | 0 | 1 |

### Machine-Readable Metrics

```json:metrics
{
  "slice_id": "US-3",
  "model": "Gemini 3.8 Flash",
  "total_duration_sec": 1080,
  "files_read_count": 12,
  "files_modified_count": 6,
  "commands_executed": 22,
  "first_pass_command_rate": 95.5,
  "gate_retries_total": 1,
  "gate_failure_causes": ["NoteEditorFormulaSheetResponsiveTest child assertions"],
  "tokens_estimated": 95000,
  "stages": {
    "orient": {"duration_sec": 60, "retries": 0, "commands": 1},
    "setup": {"duration_sec": 30, "retries": 0, "commands": 1},
    "verify_baseline": {"duration_sec": 90, "retries": 0, "commands": 2},
    "implement": {"duration_sec": 240, "retries": 0, "commands": 2},
    "test": {"duration_sec": 360, "retries": 1, "commands": 8},
    "code_quality_fix": {"duration_sec": 180, "retries": 0, "commands": 5},
    "update_state": {"duration_sec": 60, "retries": 0, "commands": 2},
    "clean_exit": {"duration_sec": 30, "retries": 0, "commands": 1}
  }
}
```
