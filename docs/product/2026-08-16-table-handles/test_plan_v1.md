# Test Plan — Table Column, Row & Table Handles

## Feature / Bug

Verify table handle visibility, bottom-sheet actions, document transformations, persistence compatibility, and read-only safety.

## Layer Selection

| Layer | Included | Reason |
|------|----------|--------|
| Unit tests (`app/src/test/`) | ✅ | Pure document operations and ViewModel state/save behavior are deterministic and lowest-cost. |
| Integration tests (`app/src/test/`) | ❌ | No API, Room schema, or repository contract changes. |
| Instrumented UI tests (`app/src/androidTest/`) | ✅ | Handles, focus, modal sheets, touch targets, and Compose rendering require Android runtime verification. |

## Test Cases

### `NoteEditorViewModelTest.kt` — Unit

| ID | Given | When | Then |
|----|-------|------|------|
| T1 | Editable 2×2 table and focused cell at (0,0) | insert column left | table becomes 2×3 with an empty column at 0 |
| T2 | Editable 2×2 table and focused cell at (1,1) | insert column right | table becomes 2×3 with an empty column at 2 |
| T3 | Editable table with content in a column | clear column | every cell in target column becomes empty and other cells remain unchanged |
| T4 | Editable 2×2 table | delete a non-final column | all rows lose the target column |
| T5 | Editable single-column table | delete the final column | the entire `TableBlock` is removed |
| T6 | Editable 2×2 table and focused row 0 | insert row above | table becomes 3×2 with an empty row at 0 |
| T7 | Editable 2×2 table and focused row 1 | insert row below | table becomes 3×2 with an empty row at 2 |
| T8 | Editable table with content in a row | clear row | every cell in target row becomes empty and other rows remain unchanged |
| T9 | Editable 2×2 table | delete a non-final row | target row is removed |
| T10 | Editable single-row table | delete the final row | the entire `TableBlock` is removed |
| T11 | Editable table with content and a following block | duplicate table | a deep-equal table is inserted immediately after the original with distinct block identity |
| T12 | Editable table with content | clear entire table | all cells become empty |
| T13 | Editable table | delete table | the table block is removed |
| T14 | Editable table with `fitToWidth = false` | toggle fit to width | field becomes true; toggling again returns false |
| T15 | Read-only note with a table | invoke every table command | document and repository state remain unchanged |
| T16 | Editable table operation | execute command | auto-save serializes the updated document |

### `NoteDocumentTest.kt` — Unit

| ID | Given | When | Then |
|----|-------|------|------|
| T17 | Table JSON with `fitToWidth: true` | parse document | table restores `fitToWidth == true` |
| T18 | Legacy table JSON without `fitToWidth` | parse and serialize document | field defaults false and document remains readable |

### `TableHandlesScreenTest.kt` — Instrumented UI

| ID | Given | When | Then |
|----|-------|------|------|
| T19 | Editable note containing a table with no focused cell | render editor | no column, row, or table-options handle is visible |
| T20 | Editable note containing a table | tap a cell | focused cell, column handle, row handle, and table-options handle are visible with stable test tags |
| T21 | Focused cell | tap column handle | column sheet opens with Insert left, Insert right, Clear column, Delete column in that order |
| T22 | Focused cell | tap row handle | row sheet opens with Insert above, Insert below, Clear row, Delete row in that order |
| T23 | Focused cell | tap table-options handle | table sheet opens with Clear entire table, Duplicate table, Fit to width, Delete table in that order |
| T24 | Focused cell | tap outside table | all handles disappear |
| T25 | Read-only note | render and tap table cell | no handles or actionable sheet entry points are available |
| T26 | Focused 2×2 table | open column sheet and choose Clear column | sheet dismisses and target column cells are empty |
| T27 | Focused table | open table sheet and choose Delete table | sheet dismisses and table block is absent |

## Shared JSON Scenarios

Not applicable. This feature has no API endpoints and requires no shared JSON API fixtures. Document JSON fixtures remain local to `NoteDocumentTest` and ViewModel tests.

## Coverage Targets

| Scope | Minimum |
|-------|---------|
| Overall project | ≥ 80% line coverage |
| New/modified ViewModel logic | ≥ 90% line coverage |
| Compose screens | excluded |

## Verification Commands

```bash
./gradlew testDebugUnitTest
./gradlew koverLog
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest
./gradlew ktlintCheck detekt lintDebug
bash scripts/check-feature-lifecycle.sh
```
