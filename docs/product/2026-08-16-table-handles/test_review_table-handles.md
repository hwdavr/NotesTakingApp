# Test Review — table-handles

## Review Scope and Evidence Provenance

| Item | Value |
|---|---|
| Feature / slice | `table-handles`; US-1, US-2, and US-3 |
| Current commit | `a0c9533` (`fix(editor-table-handles): resolve evaluator findings from code_review and test_review`) |
| Baselines reviewed | `docs/product/2026-08-16-table-handles/spec.md`, `sprint-contract.md`, `feature_list.json`, `progress.md`, `session-handoff.md`, `platform-capability-matrix.md`, `docs/product/design_system.md`, `design.md`, and `rules/testing-strategy.md` |
| Changed production files reviewed | `NoteDocument.kt`, `NoteEditorScreen.kt`, `TableHandleComponents.kt`, `TableLayout.kt`, `TableHandleAction.kt`, `TableOptionsBottomSheets.kt`, `NoteEditorViewModel.kt`, `strings.xml` |
| Changed test files reviewed | `NoteDocumentTest.kt`, `NoteEditorViewModelTest.kt`, `TableHandlesScreenTest.kt` |

### Command Evidence

| Command | Exit code | Timestamp | Commit | Provenance | Result / failure detail |
|---|---:|---|---|---|---|
| `bash scripts/check-feature-lifecycle.sh` | 0 | 2026-08-16T03:59Z | `d26b625` | Independently executed in Stage 1 | Tracker valid; 3 features, 0 in progress. |
| `bash scripts/check-platform-evidence.sh docs/product/2026-08-16-table-handles/ --evaluate` | 0 | 2026-08-16T03:59Z | `d26b625` | Independently executed in Stage 1 | Platform validation explicitly not required; Compose runtime evidence remains required. |
| `bash scripts/check-visual-evidence-contract.sh docs/product/2026-08-16-table-handles/` | 0 | 2026-08-16T03:59Z | `d26b625` | Independently executed in Stage 1 | Visual commands, contract rows, acceptance IDs, and connected evidence align. |
| `./gradlew testDebugUnitTest` | 0 | 2026-08-16 (Fix-Stage 5) | `a0c9533` | Fresh Fix-Stage 5 execution | 359 XML-reported tests, 0 failures/errors. |
| `./gradlew koverLog` | 0 | 2026-08-16 (Fix-Stage 5) | `a0c9533` | Fresh Fix-Stage 5 execution | 84.027% application line coverage; gate ≥80%. |
| `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` | 0 | 2026-08-16 (Fix-Stage 5) | `a0c9533` | Fresh Fix-Stage 5 execution | 20/20 on `Medium_Phone(AVD) - 13`, 0 skipped, 0 failed. |

Recorded evidence is not treated as a fresh passing execution. Fix-Stage 5 reran the required commands; the results below supersede the historical generator evidence labels while preserving their provenance.

### Fix-Stage 5 Re-verification

| Command | Exit code | Timestamp | Device / scope | Result |
|---|---:|---|---|---|
| `./gradlew testDebugUnitTest` | 0 | 2026-08-16 (Fix-Stage 5) | JVM; 359 XML-reported tests, 0 failures/errors | PASS |
| `./gradlew koverLog` | 0 | 2026-08-16 (Fix-Stage 5) | Debug/release unit coverage | PASS — 84.027% application line coverage |
| `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` | 0 | 2026-08-16 (Fix-Stage 5) | `Medium_Phone(AVD) - 13`; 20/20, 0 skipped, 0 failed | PASS |
| `...#captureFocusedTableState` | 0 | 2026-08-16 (Fix-Stage 5) | `Medium_Phone(AVD) - 13`; 1/1 | PASS |
| `...#captureColumnOptionsSheet` | 0 | 2026-08-16 (Fix-Stage 5) | `Medium_Phone(AVD) - 13`; 1/1 | PASS |
| `...#captureRowOptionsSheet` | 0 | 2026-08-16 (Fix-Stage 5) | `Medium_Phone(AVD) - 13`; 1/1 | PASS |
| `...#captureTableOptionsSheet` | 0 | 2026-08-16 (Fix-Stage 5) | `Medium_Phone(AVD) - 13`; 1/1 | PASS |
| `test -s` for four `visual_evidence/*.png` artifacts | 0 | 2026-08-16 (Fix-Stage 5) | Local feature workspace | PASS — all four artifacts non-empty |

One early attempt to batch visual commands overlapped emulator sessions and produced a non-zero/zero-test instrumentation failure. It is recorded as a tooling failure, not as feature evidence; all four visual commands were rerun sequentially and passed.

## Requirement-to-Test Traceability

The following rows include every FR, AC, and documented edge/NFR/design item in the active specification and sprint contract. Historical `Recorded pass; independent pending` labels are superseded by the Fix-Stage 5 command evidence above; every row remains passing unless a `Fix Status` cell explicitly records the finding that was closed.

| Source ID | Required behavior | Test file + method | Production trigger exercised | Observable assertion | Evidence status | Result | Fix Status |
|---|---|---|---|---|---|---|---|
| FR-001 | Cell tap shows the column handle above the focused column. | `TableHandlesScreenTest#focusedCellShowsAllHandles` | Production `BasicTextField` cell tap. | `table_column_handle` is displayed. | Recorded pass; independent pending | PASS |
| FR-002 | Cell tap shows the row handle beside the focused row. | `TableHandlesScreenTest#focusedCellShowsAllHandles` | Production cell focus. | `table_row_handle` is displayed. | Recorded pass; independent pending | PASS |
| FR-003 | Column/row handles are focus-scoped and disappear when focus leaves. | `TableHandlesScreenTest#handlesDismissWhenFocusLeavesTable` | Production table cell tap followed by outside text-block tap. | All three handle tags are absent. | Recorded pass; independent pending | PASS |
| FR-004 | Column handle opens the four-option column sheet. | `TableHandlesScreenTest#eachHandleOpensOrderedSheet`, `#deleteIsFinalActionInEverySheet` | Production column-handle tap and sheet-row taps. | Four labels, divider, and final-action geometry are asserted. | Recorded pass; independent pending | PASS |
| FR-005 | Row handle opens the four-option row sheet. | `TableHandlesScreenTest#eachHandleOpensOrderedSheet`, `#deleteIsFinalActionInEverySheet` | Production row-handle tap and sheet-row taps. | Four labels, divider, and final-action geometry are asserted. | Recorded pass; independent pending | PASS |
| FR-006 | Insert a new empty column to the left. | `NoteEditorViewModelTest#tableInsertOperations`, `#tableActionDispatcherRoutesEveryProductionCommand` | Production ViewModel command and dispatcher. | Dimensions, position, and empty cells are asserted. | Recorded pass; independent pending | PASS |
| FR-007 | Insert a new empty column to the right. | `NoteEditorViewModelTest#tableInsertOperations`, `#tableActionDispatcherRoutesEveryProductionCommand` | Production ViewModel command and dispatcher. | Dimensions, position, and empty cells are asserted. | Recorded pass; independent pending | PASS |
| FR-008 | Delete a column; deleting the final column removes the block. | `NoteEditorViewModelTest#tableDeleteOperations`, `TableHandlesScreenTest#tableDeleteFlowCompletes` | Production ViewModel delete command and production table sheet delete. | Remaining dimensions and block absence are asserted. | Recorded pass; independent pending | PASS |
| FR-009 | Clear every cell in the focused column only. | `NoteEditorViewModelTest#tableClearOperations`, `TableHandlesScreenTest#selectingOptionUpdatesAndDismisses` | Production ViewModel command and column-sheet clear row. | Target column empties while another column and UI remain present. | Recorded pass; independent pending | PASS |
| FR-010 | Insert an empty row above. | `NoteEditorViewModelTest#tableInsertOperations`, `#tableActionDispatcherRoutesEveryProductionCommand` | Production ViewModel command and dispatcher. | Row count, empty inserted row, and shifted content are asserted. | Recorded pass; independent pending | PASS |
| FR-011 | Insert an empty row below. | `NoteEditorViewModelTest#tableInsertOperations`, `TableHandlesScreenTest#operationPersistsAfterEditorReload` | Production ViewModel command and production row-sheet insert. | Row count and persisted reload dimensions are asserted. | Recorded pass; independent pending | PASS |
| FR-012 | Delete a row; deleting the final row removes the block. | `NoteEditorViewModelTest#tableDeleteOperations` | Production ViewModel delete command. | Remaining row and block absence are asserted. | Recorded pass; independent pending | PASS |
| FR-013 | Clear every cell in the focused row only. | `NoteEditorViewModelTest#tableClearOperations` | Production ViewModel clear-row command. | Non-target row remains populated; target cells empty. | Recorded pass; independent pending | PASS |
| FR-014 | Read-only notes expose no table operations. | `TableHandlesScreenTest#readOnlyTableHasNoHandles`, `NoteEditorViewModelTest#readOnlyTableCommandsAreNoOps` | Production read-only cell focus and all ViewModel commands. | No handles/sheets, no document mutation, and no save. | Recorded pass; independent pending | PASS |
| FR-015 | Insert/delete operations update immediately and use auto-save. | `NoteEditorViewModelTest#tableOperationsAutoSaveUpdatedDocument`, `TableHandlesScreenTest#operationPersistsAfterEditorReload` | Production mutation and save/reload path. | Saved JSON contains the changed table and reload restores it. | Recorded pass; independent pending | PASS |
| FR-016 | A selected sheet option dismisses the sheet. | `TableHandlesScreenTest#eachHandleOpensOrderedSheet`, `#selectingOptionUpdatesAndDismisses`, `#tableOptionsFlowCompletes`, `#tableFitToWidthFlowCompletes`, `#tableDeleteFlowCompletes` | Production column/row/table option clicks. | Dismissal is asserted for column, table duplicate, fit, delete, and clear-all actions. | Fresh focused rerun: 20/20 | Fixed ✅ |
| FR-017 | Cell tap shows the table-options handle. | `TableHandlesScreenTest#focusedCellShowsAllHandles` | Production cell focus. | `table_options_handle` is displayed. | Recorded pass; independent pending | PASS |
| FR-018 | Table-options handle opens the four-option table sheet. | `TableHandlesScreenTest#eachHandleOpensOrderedSheet`, `#deleteIsFinalActionInEverySheet` | Production table-options-handle tap. | Four labels, divider, and order geometry are asserted. | Recorded pass; independent pending | PASS |
| FR-019 | Clear-entire-table empties all cells. | `NoteEditorViewModelTest#tableClearOperations`, `#tableActionDispatcherRoutesEveryProductionCommand` | Production ViewModel command and dispatcher. | All cells are asserted empty. | Recorded pass; independent pending | PASS |
| FR-020 | Duplicate creates an adjacent deep copy. | `NoteEditorViewModelTest#duplicateTableDeepCopies`, `TableHandlesScreenTest#tableOptionsFlowCompletes` | Production ViewModel duplicate and table-sheet duplicate. | Deep-copy identity/content and adjacent block count are asserted. | Recorded pass; independent pending | PASS |
| FR-021 | Delete-table removes the whole block. | `NoteEditorViewModelTest#tableDeleteOperations`, `TableHandlesScreenTest#tableDeleteFlowCompletes` | Production ViewModel and table-sheet delete. | No table blocks remain. | Recorded pass; independent pending | PASS |
| FR-022 | Fit-to-width toggles equal-width rendered columns. | `NoteEditorViewModelTest#toggleTableFitToWidth`, `TableLayoutTest`, `TableHandlesScreenTest#tableFitToWidthFlowCompletes` | Production table-sheet fit action. | JVM weights and rendered bounds become equal, then return to default sizing. | Fresh focused rerun: 20/20 | Fixed ✅ |
| FR-023 | Table-options handle is visible only while a cell in that table is focused. | `TableHandlesScreenTest#focusedCellShowsAllHandles`, `#handlesDismissWhenFocusLeavesTable`, `#onlyFocusedTableShowsHandles` | Production cell focus/outside focus/multi-table flow. | Handle presence/absence and other-table isolation are asserted. | Recorded pass; independent pending | PASS |
| AC-001 | Focused cell shows column and row handles. | `TableHandlesScreenTest#focusedCellShowsAllHandles` | Cell tap. | Both tags displayed. | Recorded pass; independent pending | PASS |
| AC-002 | Column sheet has the exact four options. | `TableHandlesScreenTest#eachHandleOpensOrderedSheet` | Column handle tap. | Four labels and delete divider exist. | Recorded pass; independent pending | PASS |
| AC-003 | Row sheet has the exact four options. | `TableHandlesScreenTest#eachHandleOpensOrderedSheet` | Row handle tap. | Four labels and delete divider exist. | Recorded pass; independent pending | PASS |
| AC-004 | Insert-left on column 0 yields a 2×3 table with empty index 0. | `NoteEditorViewModelTest#tableInsertOperations` | ViewModel insert-left command. | Row dimensions, empty index 0, and shifted content asserted. | Recorded pass; independent pending | PASS |
| AC-005 | Insert-right on column 1 yields empty index 2. | `NoteEditorViewModelTest#tableInsertOperations` | ViewModel insert-right command. | Dimensions, original index 1, and empty index 2 asserted. | Recorded pass; independent pending | PASS |
| AC-006 | Delete column changes dimensions and removes final block. | `NoteEditorViewModelTest#tableDeleteOperations` | ViewModel delete-column command. | 2×1 result and final block absence asserted. | Recorded pass; independent pending | PASS |
| AC-007 | Clear column empties only that column. | `NoteEditorViewModelTest#tableClearOperations`, `TableHandlesScreenTest#selectingOptionUpdatesAndDismisses` | ViewModel and production column-sheet clear. | Target empty, other column/content visible. | Recorded pass; independent pending | PASS |
| AC-008 | Insert-above adds an empty row at index 0. | `NoteEditorViewModelTest#tableInsertOperations` | ViewModel insert-row-above command. | Empty row index 0 and shifted content asserted. | Recorded pass; independent pending | PASS |
| AC-009 | Insert-below on row 1 adds an empty row at index 2. | `NoteEditorViewModelTest#tableInsertOperations`, `TableHandlesScreenTest#operationPersistsAfterEditorReload` | ViewModel and production row-sheet insert. | Empty row position and reload row count asserted. | Recorded pass; independent pending | PASS |
| AC-010 | Delete row changes dimensions and removes final block. | `NoteEditorViewModelTest#tableDeleteOperations` | ViewModel delete-row command. | 1×2 result and final block absence asserted. | Recorded pass; independent pending | PASS |
| AC-011 | Clear row empties only the target row. | `NoteEditorViewModelTest#tableClearOperations` | ViewModel clear-row command. | Target row empty and other row remains populated. | Recorded pass; independent pending | PASS |
| AC-012 | Read-only cell tap shows no handles. | `TableHandlesScreenTest#readOnlyTableHasNoHandles` | Read-only production cell tap. | All handle tags and sheet tags absent. | Recorded pass; independent pending | PASS |
| AC-013 | Tapping outside dismisses all handles. | `TableHandlesScreenTest#handlesDismissWhenFocusLeavesTable` | Production outside text-block tap. | All three handles absent. | Recorded pass; independent pending | PASS |
| AC-014 | Any insert/delete/clear dismisses the sheet and updates immediately. | `TableHandlesScreenTest#selectingOptionUpdatesAndDismisses`, `#clearEntireTableUpdatesAndDismisses`, `#tableOptionsFlowCompletes`, `#tableFitToWidthFlowCompletes`, `#tableDeleteFlowCompletes` | Production clear, duplicate, fit, and delete actions. | Each action dismisses its sheet and updates document/UI state. | Fresh focused rerun: 20/20 | Fixed ✅ |
| AC-015 | Table handle opens the exact table options sheet. | `TableHandlesScreenTest#eachHandleOpensOrderedSheet` | Production table handle tap. | Four labels and divider exist. | Recorded pass; independent pending | PASS |
| AC-016 | Clear entire table empties every cell. | `NoteEditorViewModelTest#tableClearOperations`, `#tableActionDispatcherRoutesEveryProductionCommand`, `TableHandlesScreenTest#clearEntireTableUpdatesAndDismisses` | Production table-sheet `table_clear_all` click. | Sheet dismisses and all table cells are empty in the ViewModel document. | Fresh focused rerun: 20/20 | Fixed ✅ |
| AC-017 | Duplicate inserts an identical table immediately below. | `NoteEditorViewModelTest#duplicateTableDeepCopies`, `TableHandlesScreenTest#tableOptionsFlowCompletes` | Production duplicate command and table-sheet click. | Deep-copy identity and adjacent block count asserted. | Recorded pass; independent pending | PASS |
| AC-018 | Delete table removes the block. | `NoteEditorViewModelTest#tableDeleteOperations`, `TableHandlesScreenTest#tableDeleteFlowCompletes` | Production delete command and table-sheet click. | No table blocks remain. | Recorded pass; independent pending | PASS |
| AC-019 | Fit-to-width makes columns equal width. | `TableLayoutTest`, `TableHandlesScreenTest#tableFitToWidthFlowCompletes` | Production fit sheet click. | Rendered cell bounds are equal after fit and unequal again after toggling back. | Fresh focused rerun: 20/20 | Fixed ✅ |
| AC-020 | All three handles appear together on cell focus. | `TableHandlesScreenTest#focusedCellShowsAllHandles` | Production cell tap. | Three stable tags and focused-cell semantics asserted. | Recorded pass; independent pending | PASS |
| Edge: single-column/row final delete | Final delete removes the entire block. | `NoteEditorViewModelTest#tableDeleteOperations` | ViewModel final row/column delete. | Block absence asserted. | Recorded pass; independent pending | PASS |
| Edge: focus loss during sheet | Stored target still receives the action. | `TableHandlesScreenTest#storedTargetSurvivesSheetFocusLoss` | Sheet open, outside focus change, then production clear action. | Original table column is cleared. | Recorded pass; independent pending | PASS |
| Edge: multiple tables | Only focused table shows handles and other table is unchanged. | `TableHandlesScreenTest#onlyFocusedTableShowsHandles` | Production focus and action on first of two tables. | Exactly one handle and second table content remain. | Recorded pass; independent pending | PASS |
| Edge: large/wide table | Handles remain correct for many rows/columns. | `TableHandlesScreenTest#wideTableKeepsAllHandlesAvailable` | Production focus on an 8×12 table. | All three handles display and all 96 cells are present. | Fresh focused rerun: 20/20 | Fixed ✅ |
| NFR: accessibility | Labels/descriptions, focus order, and 48dp targets are present. | `TableHandlesScreenTest#handlesAndActionsExposeAccessibleLabelsAndMinimumBounds` | Production focus and table sheet actions. | Localized handle/action descriptions and minimum 48dp bounds are asserted. | Fresh focused rerun: 20/20 | Fixed ✅ |
| NFR: persistence | Existing JSON/auto-save path is reused. | `NoteEditorViewModelTest#tableOperationsAutoSaveUpdatedDocument`, `TableHandlesScreenTest#operationPersistsAfterEditorReload`, `NoteDocumentTest#legacyTableDefaultsFitToWidth` | Production mutation, auto-save, reload, and mapper. | Saved content, restored rows, and legacy default are asserted. | Recorded pass; independent pending | PASS |
| Design: Delete ordering | Delete is final with a divider before it in every sheet. | `TableHandlesScreenTest#deleteIsFinalActionInEverySheet` | Production sheet opening. | Bounds prove delete is below every action and divider. | Recorded pass; independent pending | PASS |

## Test Quality Findings

- [x] Names generally describe the Given / When / Then behavior.
- [x] Each mapped test exercises a production trigger, not only a setter, reducer, helper, or preloaded final state. `AC-016` now clicks `table_clear_all` through the production sheet.
- [x] Each mapped test has a direct observable assertion for the requirement. Dismissal, rendered equal sizing, accessibility descriptions/bounds, and wide-table behavior now have direct assertions.
- [x] No tautological assertions or empty verification blocks were found in the mapped tests.
- [x] Unit/UI test isolation is appropriate for the tested layer; UI tests use deterministic fake repositories and no real backend.
- [x] API/shared JSON scenarios are N/A: this feature introduces no API endpoint.
- [x] Import hygiene in the mapped test files is clean; no wildcard or inline fully qualified calls were found.

### Conditional Categories

| Category | In scope? | Coverage / N/A reason | Result |
|---|---|---|---|
| Runtime permissions | No | No permissions or platform service are introduced. | N/A |
| Asynchronous callbacks and animation | Yes | Sheet callbacks are production-wired; focused runtime tests assert dismissal for table-level and destructive paths. | Fixed ✅ |
| Lifecycle and navigation cleanup | Yes | Focus-out/back behavior and ViewModel-backed focus restoration through composition recreation are exercised. | Fixed ✅ |
| Error and retry behavior | No | The spec defines no error/retry state for local list transformations. | N/A |
| API/data error matrix | No | No API or data-source contract is added; JSON compatibility is covered by mapper tests. | N/A |

## Coverage Distribution

| Scope / class | Coverage | Branches or requirements not proven | Result |
|---|---:|---|---|
| Overall project | 84.027% line (Fix-Stage 5 rerun) | No remaining coverage or behavior gap identified. | PASS |
| `NoteEditorViewModel` | 95.3% line; `NoteEditorViewModelKt` 95.2% line | Focus dispatch and table operations are covered; rendered UI branches are covered by the connected suite. | PASS |
| `TableLayout.tableColumnWeights` | Covered by `TableLayoutTest` | Direct tests prove equal fit weights and restoration to content-based default weights. | Fixed ✅ |

## Regression Verification

| Item | Evidence | Result |
|---|---|---|
| Reproduction test red before fix (bug fixes only) | Feature is not classified as a bug fix. | N/A |
| Reproduction test green after fix | Feature is not classified as a bug fix. | N/A |
| No uncontrolled timing or threading | Tests use coroutine test utilities and Compose `waitUntil`/`waitForIdle`; no `Thread.sleep` found. | PASS |

## Fix Pass Summary

- **Findings fixed:** 8/8; **unresolved:** 0.
- **Fixed rows:** FR-016, FR-022, AC-014, AC-016, AC-019, Edge: large/wide table, NFR: accessibility, and the lifecycle/configuration category.
- **Focused runtime evidence:** `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.TableHandlesScreenTest` exited 0 with 20/20 tests passing on `Medium_Phone(AVD) - 13`.
- **Unresolved rows:** None.

## Verdict

**FIXED ✅** — The previously revision-required production and evidence gaps are covered by ViewModel state, JVM layout tests, and production-backed emulator tests. Final sprint-contract and global quality-gate results are recorded in the Fix-Stage 5 evidence.
