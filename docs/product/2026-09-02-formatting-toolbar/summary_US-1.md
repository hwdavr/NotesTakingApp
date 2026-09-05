# Change Summary — Insert, edit, and atomically delete inline formulas

**Type**: feature
**Started**: 2026-09-05 23:18
**Status**: In Progress
**Feature ID**: US-1
**Workspace**: `docs/product/2026-09-02-formatting-toolbar/`

---

## Stage Progress

### Complex Feature (`harness-generator` workflow)

| Stage | Status | Timestamp | Notes |
|---|---|---|---|
| Orient | ✅ Complete | 2026-09-05 23:18 | Lifecycle validated; US-1 selected; tracker set to In Progress. |
| Setup | ✅ Complete | 2026-09-05 23:18 | emulator-5554 online and ready for instrumented tests. |
| Verify Baseline | ✅ Complete | 2026-09-05 23:20 | assembleDebug and testDebugUnitTest pass cleanly (0 failures). |
| Implement | ✅ Complete | 2026-09-05 23:25 | Data, domain, and UI layers implemented; assembleDebug passes. |
| Test | ✅ Complete | 2026-09-05 23:33 | 8 acceptance verification commands passed (exit 0); coverage 80.15%; journeys 4/4 passed. |
| Code Quality Fix | ✅ Complete | 2026-09-05 23:35 | ktlintCheck, detekt, check-architecture-rules, assembleDebug, and testDebugUnitTest pass cleanly. |
| Update State | ✅ Complete | 2026-09-05 23:36 | feature_list.json passing, product.md updated, lifecycle valid. |
| Clean Exit | ⏳ In Progress | 2026-09-05 23:36 | Verifying clean exit and writing handoff. |
| Install App To Device | ⏳ Pending | | Installed debug APK to target device/emulator. |

---

## Baseline Goals and Scope

- Insert rendered inline LaTex formula replacing a non-empty selection, at a collapsed cursor, or appending/focusing a paragraph if no text block is focused.
- Tapping a rendered formula reopens the formula sheet with source prefilled.
- Invalid formula source stays editable in the open sheet without mutating the note.
- Deleting an inline formula removes the whole formula atom atomically without leaving raw LaTex.
- Offline, API 24 compatible, light/dark theme deterministic LaTex rendering.
- Markdown export (`$source$`) and PDF export support.

## Key Decisions

- Use dependency-free offline formula renderer for deterministic API-24 rendering without external heavy math libraries or network.
- Extend `RichText` with optional backward-compatible formula source and stable identity.
- Atomic deletion removes the entire formula placeholder unit.

## Knowledge Artifacts

- `docs/knowledge/architecture-decisions/001-separate-editor-actions-sheet.md`
- `docs/knowledge/pitfalls/2026-07-09-compose-scroll-container-display-assertions.md`

## Open Items

- Complete implementation of US-1 across data, domain, and UI layers.

---

## Stage Evidence

### Orient
- Command: `bash harness/scripts/check-feature-lifecycle.sh`
- Result: exit 0 (Feature lifecycle tracker valid: 8 feature(s), 1 in progress)

### Setup
- Command: `adb devices`
- Result: exit 0 (emulator-5554 device)

### Implement
- Created files:
  - `app/src/main/java/com/example/notesapp/ui/editor/components/InlineFormulaRenderer.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/components/FormulaEditorSheet.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/mapper/NoteDocumentFormula.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/screen/NoteActionsSheetSection.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/viewmodel/FormulaEditorActions.kt`
- Modified files:
  - `app/src/main/res/values/strings.xml`
  - `app/src/main/java/com/example/notesapp/ui/editor/mapper/NoteDocument.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModel.kt`
  - `app/src/main/java/com/example/notesapp/ui/editor/screen/NoteEditorScreen.kt`
- Build status: `./gradlew assembleDebug` passed (exit 0)

### Test
- Acceptance verification commands:
  - TC-US-1-01: `NoteEditorFormulaSheetTest#formulaActionReplacesSelectionWithRenderedFormula` (exit 0)
  - TC-US-1-02: `NoteEditorFormulaSheetTest#formulaActionInsertsAtCollapsedCursor` (exit 0)
  - TC-US-1-03: `NoteEditorFormulaSheetTest#formulaActionAppendsAndFocusesWhenNoTextBlockIsFocused` (exit 0)
  - TC-US-1-04: `NoteEditorFormulaSheetTest#invalidFormulaStaysEditableWithoutChangingDocument` (exit 0)
  - TC-US-1-05: `NoteEditorFormulaSheetTest#tappingFormulaReopensSourceAndValidUpdatePersists` (exit 0)
  - TC-US-1-06: `NoteEditorFormulaSheetTest#deletingInlineFormulaRemovesWholeFormulaAtom` (exit 0)
  - TC-US-1-07: `NoteDocumentTest` (exit 0)
  - TC-US-1-08: `InlineFormulaRendererTest` (exit 0)
- Full Unit Test Suite: `./gradlew testDebugUnitTest` passed (exit 0)
- Journey Registry Regression: `bash harness/scripts/check-journey-registry.sh --run-all` passed (exit 0, 4/4 passed)
- Acceptance Traceability: `bash harness/scripts/check-acceptance-test-traceability.sh docs/product/2026-09-02-formatting-toolbar --test US-1` passed (exit 0)
- Platform Capability Evidence: `bash harness/scripts/check-platform-evidence.sh docs/product/2026-09-02-formatting-toolbar --evaluate --slice US-1` passed (exit 0)
- Code Coverage: `bash harness/scripts/check-coverage.sh app/build/reports/kover/reportDebug.xml` passed (exit 0, 80.15% >= 80%)

### Code Quality Fix
- Formatting: `./gradlew ktlintCheck` passed (exit 0)
- Static analysis: `./gradlew detekt` passed (exit 0)
- Architecture rules: `bash harness/scripts/check-architecture-rules.sh` passed (exit 0, 0 violations)
- Re-verify build: `./gradlew assembleDebug` passed (exit 0)
- Re-verify tests: `./gradlew testDebugUnitTest` passed (exit 0)
