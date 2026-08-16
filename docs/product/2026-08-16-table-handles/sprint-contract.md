# Sprint Contract — Table Column, Row & Table Handles

## 🏃 Sprint Overview

* **Sprint:** P08-16-table-handles
* **Feature:** Table Column, Row & Table Handles
* **Duration:** 1 sprint

## 🎯 Scope

### In Scope

* [ ] Focus-driven column, row, and table handles in editable table blocks.
* [ ] Column, row, and table option sheets with Delete as the final action.
* [ ] Insert, clear, delete, duplicate, and fit-to-width document operations.
* [ ] Read-only safety, multi-table focus isolation, immediate UI update, and existing auto-save.
* [ ] Backward-compatible `fitToWidth` document serialization and unit/instrumented verification.

### Out of Scope

* Drag reorder, resize, multi-cell selection, merge, per-cell formatting, sorting, undo/redo, API changes, and database migrations.

## Platform Capability & Environment Contract

See [platform-capability-matrix.md](platform-capability-matrix.md). This is not a special platform-bound feature, so the root platform validation flag is false. Android-runtime Compose tests remain mandatory and missing emulator evidence fails loudly.

## Spec Coverage Matrix

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001, FR-002, FR-017 | Cell tap reveals column, row, and table handles | US-2 | TC-US-2-01 | In scope |
| FR-003, FR-023 | Handles are focus-scoped and disappear when focus leaves | US-2 | TC-US-2-02 | In scope |
| FR-004, FR-005, FR-018 | Each handle opens its four-option sheet | US-2 | TC-US-2-03 | In scope |
| FR-006, FR-007, FR-010, FR-011 | Insert rows/columns at the requested side | US-1 | TC-US-1-01 | In scope |
| FR-008, FR-012, FR-021 | Delete column/row/table, including last-row/column block removal | US-1 | TC-US-1-02 | In scope |
| FR-009, FR-013, FR-019 | Clear column/row/entire table | US-1 | TC-US-1-03 | In scope |
| FR-014 | Disable table interactions in read-only notes | US-2 | TC-US-2-04 | In scope |
| FR-015, FR-016 | Immediate update, auto-save, and sheet dismissal | US-3 | TC-US-3-01 | In scope |
| FR-020 | Deep-copy duplicate immediately after original | US-1 | TC-US-1-04 | In scope |
| FR-022 | Toggle equal-width fit-to-width mode | US-1 | TC-US-1-05 | In scope |
| AC-001, AC-020 | All three handles appear beside focused cell | US-2 | TC-US-2-01 | In scope |
| AC-002, AC-003, AC-015 | Correct sheet opens for each handle | US-2 | TC-US-2-03 | In scope |
| AC-004, AC-005 | Column insertion positions are correct | US-1 | TC-US-1-01 | In scope |
| AC-006, AC-010 | Delete changes dimensions and removes final block | US-1 | TC-US-1-02 | In scope |
| AC-007, AC-011, AC-016 | Clear operations empty only the requested scope | US-1 | TC-US-1-03 | In scope |
| AC-008, AC-009 | Row insertion positions are correct | US-1 | TC-US-1-01 | In scope |
| AC-012, AC-013 | Read-only and outside-table focus behavior | US-2 | TC-US-2-02 | In scope |
| AC-014 | Action dismisses sheet and updates table | US-3 | TC-US-3-01 | In scope |
| AC-017 | Duplicate appears below original | US-1 | TC-US-1-04 | In scope |
| AC-018 | Delete table removes block | US-1 | TC-US-1-02 | In scope |
| AC-019 | Fit-to-width toggles equal sizing | US-1 | TC-US-1-05 | In scope |
| Edge: single row/column | Final delete removes whole block | US-1 | TC-US-1-02 | In scope |
| Edge: focus lost during sheet | Stored target still completes action | US-3 | TC-US-3-02 | In scope |
| Edge: multiple tables | Only focused table shows handles | US-3 | TC-US-3-03 | In scope |
| NFR: accessibility | Labels, descriptions, focus order, 48dp targets | US-2 | TC-US-2-03 | In scope |
| NFR: persistence | Existing JSON/auto-save path is reused | US-1 | TC-US-1-06 | In scope |
| Design: Delete ordering | Delete is final row with divider before it | US-3 | TC-US-3-04 | In scope |

## User Scenarios & Testing

## Acceptance Test Cases

The acceptance-test matrices below are the implementation authorization contract; each test ID maps to one primary acceptance criterion and an executable Gradle command.

### US-1: Apply table operations (Priority: P1)

Given an editable note with a table, the ViewModel applies the requested structural or table-level operation to the document and persists it through the existing editor path.

**Why this priority:** It is the core behavior and safest first slice because it is deterministic and independent of Compose layout.

**Independent Test:** JVM tests invoke the production ViewModel commands and assert document structure, JSON, read-only guards, and save scheduling.

**Acceptance Criteria:**

1. Given a focused table target, when an insert/clear/delete command runs, then only the requested rows/columns/cells change and final-row/column deletion removes the block.
2. Given a table, when duplicate, delete-table, or fit-to-width runs, then the expected block order/content/flag is produced.
3. Given legacy JSON, when parsed, then missing `fitToWidth` defaults false; updated documents auto-save.

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-1-01 | insert ACs | JVM unit | `NoteEditorViewModelTest.kt#tableInsertOperations` | Build 2×2 table; invoke left/right/above/below | Dimensions and empty insertion positions | `./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest'` |
| TC-US-1-02 | delete ACs | JVM unit | `NoteEditorViewModelTest.kt#tableDeleteOperations` | Delete non-final and final rows/columns/table | Correct dimensions and block removal | `./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest'` |
| TC-US-1-03 | clear ACs | JVM unit | `NoteEditorViewModelTest.kt#tableClearOperations` | Populate target scopes; clear them | Only target cells are empty | `./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest'` |
| TC-US-1-04 | duplicate AC | JVM unit | `NoteEditorViewModelTest.kt#duplicateTableDeepCopies` | Duplicate populated table | Adjacent deep copy with distinct ID | `./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest'` |
| TC-US-1-05 | fit AC | JVM unit | `NoteEditorViewModelTest.kt#toggleTableFitToWidth` | Toggle twice | true then false | `./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest'` |
| TC-US-1-06 | persistence/read-only AC | JVM unit | `NoteDocumentTest.kt#legacyTableDefaultsFitToWidth` and `NoteEditorViewModelTest.kt#readOnlyTableCommandsAreNoOps` | Parse legacy JSON; invoke commands on read-only note | Backward compatibility and no mutation | `./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.mapper.NoteDocumentTest' --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest'` |

### US-2: Reveal contextual handles and sheets (Priority: P2)

Given an editable table, the user taps a cell and sees the contextual handles and correctly ordered option sheet; read-only and outside-table states remain safe.

**Why this priority:** It provides the interaction surface that exposes the core operations.

**Independent Test:** Instrumented Compose tests render the production editor content, tap cells/handles, and assert semantics, tags, sheet labels, and dismissal.

**Acceptance Criteria:**

1. Given no focused cell, when the editor renders, then handles are hidden; tapping a cell shows all three handles.
2. Given a focused cell, when each handle is tapped, then the correct sheet opens with Delete last and accessible 48dp actions.
3. Given read-only content or a tap outside the table, then handles are absent or dismissed.

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-2-01 | handle display ACs | Instrumented UI | `TableHandlesScreenTest.kt#focusedCellShowsAllHandles` | Render editable table; tap cell | Three tags and focused-cell semantics exist | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-2-02 | focus visibility ACs | Instrumented UI | `TableHandlesScreenTest.kt#handlesDismissWhenFocusLeavesTable` | Focus cell; tap outside | All handles disappear | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-2-03 | sheet/order/accessibility ACs | Instrumented UI | `TableHandlesScreenTest.kt#eachHandleOpensOrderedSheet` | Tap each handle | Exact labels, Delete last, divider, tags/content descriptions | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-2-04 | read-only AC | Instrumented UI | `TableHandlesScreenTest.kt#readOnlyTableHasNoHandles` | Render read-only note; tap cell | No handles or action sheets | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |

### US-3: Complete the production editing flow (Priority: P3)

Given a focused table in the production Note Editor, users can select an option, see the table update immediately, and retain correct behavior across multiple tables and focus loss.

**Why this priority:** It proves the end-to-end user-visible behavior and owns final visual verification.

**Independent Test:** Instrumented tests navigate through the production editor entry point, perform sheet actions, assert the resulting table, and capture verified focused/sheet states.

**Acceptance Criteria:**

1. Given a focused table and an option selection, when the action completes, then the sheet dismisses and the table reflects the operation.
2. Given focus loss or multiple tables, when an action is completed, then the stored target remains correct and other tables remain unchanged.
3. Given the completed editor flow, when verified screenshots are captured, then focused-table and all three sheet states match the approved design references.

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-3-01 | immediate update AC | Instrumented UI | `TableHandlesScreenTest.kt#selectingOptionUpdatesAndDismisses` | Open sheet; choose clear/insert | Sheet gone and table state updated | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-3-02 | focus-loss edge | Instrumented UI | `TableHandlesScreenTest.kt#storedTargetSurvivesSheetFocusLoss` | Open sheet; cause keyboard/focus change; choose action | Original target is modified | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-3-03 | multiple-table edge | Instrumented UI | `TableHandlesScreenTest.kt#onlyFocusedTableShowsHandles` | Render two tables; focus one | Other table has no handles and remains unchanged | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-3-04 | Delete ordering/design | Instrumented UI | `TableHandlesScreenTest.kt#deleteIsFinalActionInEverySheet` | Open all sheets | Delete row is final visible action with divider before it | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-3-05 | persistence AC | Instrumented UI | `TableHandlesScreenTest.kt#operationPersistsAfterEditorReload` | Execute operation; reload note | Updated document is restored | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-3-06 | final flow | Instrumented UI | `TableHandlesScreenTest.kt#tableOptionsFlowCompletes` | Duplicate/delete/fit via production entry | Observable document and UI outcomes match | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` |
| TC-US-3-VIS-01 | focused state | Visual verification | `TableHandlesScreenTest.kt#captureFocusedTableState` | Assert focused cell and three handles, then capture | Non-empty `visual_evidence/table_handles_focused.png` | `test -s "$FEATURE_DIR/visual_evidence/table_handles_focused.png"` |
| TC-US-3-VIS-02 | column sheet | Visual verification | `TableHandlesScreenTest.kt#captureColumnOptionsSheet` | Assert column sheet labels/order, then capture | Non-empty `visual_evidence/table_column_sheet.png` | `test -s "$FEATURE_DIR/visual_evidence/table_column_sheet.png"` |
| TC-US-3-VIS-03 | row sheet | Visual verification | `TableHandlesScreenTest.kt#captureRowOptionsSheet` | Assert row sheet labels/order, then capture | Non-empty `visual_evidence/table_row_sheet.png` | `test -s "$FEATURE_DIR/visual_evidence/table_row_sheet.png"` |
| TC-US-3-VIS-04 | table sheet | Visual verification | `TableHandlesScreenTest.kt#captureTableOptionsSheet` | Assert table sheet labels/order, then capture | Non-empty `visual_evidence/table_options_sheet.png` | `test -s "$FEATURE_DIR/visual_evidence/table_options_sheet.png"` |

## 🧾 Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| Planning | Planner | Harness slice plan compiled | Three vertical slices; US-3 owns visual evidence. |
| Implementation | Generator | Pending approval | No production code written. |
| Review 1 | Evaluator | Pending | |
| Final Review | Evaluator | Pending | |
