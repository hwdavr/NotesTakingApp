# Feature Spec — Table Column, Row & Table Handles

**Date**: 2026-08-16
**Status**: Final
**Related design**: `design.md`

---

## Objective

Enhance the existing table block in the Note Editor so that tapping any cell reveals interactive column, row, and table-level handles. These handles provide quick access to structural table operations (insert, delete, clear) and table-level operations (clear all, duplicate, delete table, fit to width) via modal bottom sheets, enabling users to manage table structure directly without leaving the editor.

## User Goal

As a note-taking user, I want to quickly insert, delete, or clear columns and rows — and manage the entire table — by tapping intuitive handles, so that I can restructure tables efficiently without external tools.

## Scope

### In Scope

- Column handle: highlighted bar/strip above the focused cell's column, appears on cell tap.
- Row handle: highlighted bar/strip to the left of the focused cell's row, appears on cell tap.
- Table options handle: icon button at the top-right corner of the table, appears on cell tap.
- Column options bottom sheet: triggered by tapping the column handle; contains "Insert column left", "Insert column right", "Delete column", "Clear column".
- Row options bottom sheet: triggered by tapping the row handle; contains "Insert row above", "Insert row below", "Delete row", "Clear row".
- Table options bottom sheet: triggered by tapping the table options handle; contains "Clear entire table", "Duplicate table", "Delete table", "Fit to width".
- Deleting the last remaining column or row deletes the entire TableBlock.
- Handles only appear for the focused cell's column and row; the table options handle appears when any cell is focused. They all disappear when no cell is focused.
- Table operations update the NoteDocument immediately (local state).
- All operations are disabled when the note is read-only.

### Out Of Scope

- Column/row drag-to-reorder.
- Column/row resize.
- Multi-cell selection.
- Table-level formatting (merge cells, cell background color, text alignment per cell).
- Column sorting (ascending/descending).
- Undo/redo for table structure changes (existing undo/redo scope unchanged).
- Table resize by dragging ("Fit to width" is a one-shot toggle, not drag resize).

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---|---|---|
| Jetpack Compose | project default | UI rendering of handles and bottom sheets |
| Material 3 | project default | `ModalBottomSheet` for column/row/table options |

### Key Technical Decisions

- **Handle visibility is cell-focus-driven**: Handles appear only when a cell in the table has focus. When focus leaves the table, handles disappear.
- **Handles are overlays positioned relative to the table grid**: Column handle sits directly above the focused column; row handle sits directly to the left of the focused row; table options handle sits at the top-right corner of the table.
- **Delete-last-column/row removes entire block**: When the user deletes the only remaining column or the only remaining row, the system removes the entire `TableBlock` from the document.
- **Operations modify `EditorBlock.TableBlock.rows` in ViewModel**: All insert/delete/clear operations are pure list transformations on the `rows: List<List<List<RichText>>>` structure.
- **Duplicate table**: Creates a deep copy of the entire `TableBlock` and inserts it immediately after the original in the document's block list.
- **Fit to width**: Toggles the table between its default column sizing and a mode where columns share equal width to fill the available screen width. This is stored as a boolean property on the `TableBlock`.

### External APIs / Services

- None — all operations are local, on-device state transformations.

### Platform & Compatibility Constraints

- **Min SDK**: project default (API 24)
- **Permissions required**: None
- **Other constraints**: None

---

## Functional Requirements

- **FR-001**: When a user taps on any table cell, the system MUST show a column handle above the tapped cell's column.
- **FR-002**: When a user taps on any table cell, the system MUST show a row handle to the left of the tapped cell's row.
- **FR-003**: Column and row handles MUST only be visible while a cell in that table is focused. When focus leaves the table, handles disappear.
- **FR-004**: When the user taps the column handle, the system MUST display a modal bottom sheet with options: "Insert column left", "Insert column right", "Delete column", "Clear column".
- **FR-005**: When the user taps the row handle, the system MUST display a modal bottom sheet with options: "Insert row above", "Insert row below", "Delete row", "Clear row".
- **FR-006**: "Insert column left" MUST insert a new empty column at the position to the left of the focused column.
- **FR-007**: "Insert column right" MUST insert a new empty column at the position to the right of the focused column.
- **FR-008**: "Delete column" MUST remove the focused column from all rows. If it is the last column, the entire TableBlock is removed from the document.
- **FR-009**: "Clear column" MUST replace all cell content in the focused column with empty `RichText("")`.
- **FR-010**: "Insert row above" MUST insert a new empty row above the focused row.
- **FR-011**: "Insert row below" MUST insert a new empty row below the focused row.
- **FR-012**: "Delete row" MUST remove the focused row. If it is the last row, the entire TableBlock is removed from the document.
- **FR-013**: "Clear row" MUST replace all cell content in the focused row with empty `RichText("")`.
- **FR-014**: All table handle interactions and bottom sheet options MUST be disabled when the note is in read-only mode.
- **FR-015**: After any structural operation (insert/delete), the table MUST update immediately in the editor and auto-save via the existing auto-save mechanism.
- **FR-016**: The bottom sheet MUST dismiss after an option is selected.
- **FR-017**: When a user taps on any table cell, the system MUST show a table options handle at the top-right corner of the table.
- **FR-018**: When the user taps the table options handle, the system MUST display a modal bottom sheet with options: "Clear entire table", "Duplicate table", "Delete table", "Fit to width".
- **FR-019**: "Clear entire table" MUST replace all cell content in all rows and columns with empty `RichText("")`.
- **FR-020**: "Duplicate table" MUST create a deep copy of the entire `TableBlock` (including all cell content) and insert it immediately after the original block in the document.
- **FR-021**: "Delete table" MUST remove the entire `TableBlock` from the document.
- **FR-022**: "Fit to width" MUST toggle the table between default column sizing and equal-width columns that fill the available width.
- **FR-023**: The table options handle MUST only be visible while a cell in that table is focused, consistent with column and row handles.

## Acceptance Criteria

- **AC-001**: Given a table with multiple cells, when the user taps a cell, then a column handle appears above the focused column and a row handle appears to the left of the focused row.
- **AC-002**: Given a focused table cell, when the user taps the column handle, then a bottom sheet appears with options: "Insert column left", "Insert column right", "Delete column", "Clear column".
- **AC-003**: Given a focused table cell, when the user taps the row handle, then a bottom sheet appears with options: "Insert row above", "Insert row below", "Delete row", "Clear row".
- **AC-004**: Given a 2×2 table, when the user selects "Insert column left" on column 0, then the table becomes 2×3 with a new empty column at index 0.
- **AC-005**: Given a 2×2 table, when the user selects "Insert column right" on column 1, then the table becomes 2×3 with a new empty column at index 2.
- **AC-006**: Given a 2×2 table, when the user selects "Delete column" on column 0, then the table becomes 2×1. If the user deletes the remaining column, the table block is removed entirely.
- **AC-007**: Given a 2×2 table with content in column 0, when the user selects "Clear column", then all cells in column 0 become empty.
- **AC-008**: Given a 2×2 table, when the user selects "Insert row above" on row 0, then the table becomes 3×2 with a new empty row at index 0.
- **AC-009**: Given a 2×2 table, when the user selects "Insert row below" on row 1, then the table becomes 3×2 with a new empty row at index 2.
- **AC-010**: Given a 2×2 table, when the user selects "Delete row" on row 0, then the table becomes 1×2. If the user deletes the remaining row, the table block is removed entirely.
- **AC-011**: Given a 2×2 table with content in row 0, when the user selects "Clear row", then all cells in row 0 become empty.
- **AC-012**: Given a read-only note, when the user taps on a table cell, then no handles are visible.
- **AC-013**: Given a focused cell, when the user taps outside the table, then all handles (column, row, table) disappear.
- **AC-014**: After any insert/delete/clear operation, the bottom sheet dismisses and the table updates immediately.
- **AC-015**: Given a focused table cell, when the user taps the table options handle at the top-right corner, then a bottom sheet appears with options: "Clear entire table", "Duplicate table", "Delete table", "Fit to width".
- **AC-016**: Given a 2×2 table with content, when the user selects "Clear entire table", then all cells in all rows become empty.
- **AC-017**: Given a table, when the user selects "Duplicate table", then an identical copy of the table appears immediately below the original in the document.
- **AC-018**: Given a table, when the user selects "Delete table", then the entire TableBlock is removed from the document.
- **AC-019**: Given a table in default sizing, when the user selects "Fit to width", then the table columns resize to share equal width filling the available screen width.
- **AC-020**: Given a focused cell, when the user taps a cell, then the table options handle at the top-right corner is visible alongside the column and row handles.

## Data And Persistence

- Table structure changes modify the in-memory `NoteDocument` immediately.
- Persistence occurs via the existing auto-save mechanism that serializes `NoteDocument` to JSON and stores it in Room.
- No new database tables, entities, or migrations are required.

## Edge Cases

- **Single-column table + Delete column**: The entire TableBlock is removed from the document.
- **Single-row table + Delete row**: The entire TableBlock is removed from the document.
- **Large table (many rows/columns)**: Handles should still appear correctly; horizontal scroll may be needed for wide tables (out of scope for this feature, but handles must not break).
- **Cell focus lost during bottom sheet display**: If the keyboard closes or the user scrolls away, the bottom sheet still completes the selected operation based on the stored column/row index.
- **Multiple tables in one document**: Only the focused table shows handles; other tables remain unaffected.

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|---|---|
| A1 | The existing `TableBlock.rows` structure (`List<List<List<RichText>>>`) is sufficient for column/row operations without migration. | Low — the structure already supports arbitrary rows × columns. |
| A2 | Users are okay without undo for table structural changes (insert/delete). | Medium — users may expect undo, but it is out of scope. |
| A3 | The column handle and row handle do not need drag-to-reorder behavior in this iteration. | Low — explicitly scoped out. |

## Open Questions

All questions must be ✅ Answered before this document is approved.

| # | Question | Status | Answer |
|---|---|---|---|
| Q1 | What column options should the bottom sheet provide? | ✅ Answered | Insert column left, Insert column right, Delete column, Clear column |
| Q2 | What row options should the bottom sheet provide? | ✅ Answered | Insert row above, Insert row below, Delete row, Clear row |
| Q3 | What should the handle look like? | ✅ Answered | A simple highlighted bar/strip along the column top or row left edge |
| Q4 | When should handles appear? | ✅ Answered | Only when any cell in that table is focused/tapped |
| Q5 | Should deleting the last column/row be protected? | ✅ Answered | No — deleting the last column/row deletes the entire table block |
| Q6 | What options should the table options handle provide? | ✅ Answered | Clear entire table, Duplicate table, Delete table, Fit to width |
| Q7 | When should the table options handle be visible? | ✅ Answered | Only when any cell in the table is focused (same as column/row handles) |

## Screen States

| State | Requirement | Acceptance Criteria |
|---|---|---|
| No cell focused | Table renders normally without handles | AC-013 |
| Cell focused | Column handle above focused column, row handle left of focused row, table options handle at top-right | AC-001, AC-020 |
| Column bottom sheet open | Modal bottom sheet with 4 column options | AC-002 |
| Row bottom sheet open | Modal bottom sheet with 4 row options | AC-003 |
| Table bottom sheet open | Modal bottom sheet with 4 table options | AC-015 |
| Read-only | No handles shown, no interaction | AC-012 |

## Navigation

- **Entry**: User taps any cell in an existing `TableBlock` within the Note Editor.
- **Back/cancel**: Tapping outside the table or bottom sheet dismisses handles/sheet.
- **Success**: Table updates in place; user stays in the editor.
- **Error recovery**: No error state expected (all operations are in-memory list transformations).

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|---|---|---|
| FR-001, FR-002, FR-017 | Handle Display | AC-001, AC-020 |
| FR-003, FR-023 | Handle Visibility | AC-012, AC-013 |
| FR-004 | Column Bottom Sheet | AC-002 |
| FR-005 | Row Bottom Sheet | AC-003 |
| FR-018 | Table Bottom Sheet | AC-015 |
| FR-006, FR-007 | Column Insert Operations | AC-004, AC-005 |
| FR-008 | Column Delete | AC-006 |
| FR-009 | Column Clear | AC-007 |
| FR-010, FR-011 | Row Insert Operations | AC-008, AC-009 |
| FR-012 | Row Delete | AC-010 |
| FR-013 | Row Clear | AC-011 |
| FR-014 | Read-only | AC-012 |
| FR-015, FR-016 | Post-operation behavior | AC-014 |
| FR-019 | Clear Entire Table | AC-016 |
| FR-020 | Duplicate Table | AC-017 |
| FR-021 | Delete Table | AC-018 |
| FR-022 | Fit to Width | AC-019 |

## Verification Expectations

- **Unit**: ViewModel table operations (insert column left/right, delete column, clear column, insert row above/below, delete row, clear row, delete-last-removes-block, clear entire table, duplicate table, delete table, fit to width toggle).
- **Integration**: Not required — no repository/API changes.
- **Instrumented UI**: Table renders handles on cell focus; bottom sheets open on handle tap; operations produce correct table structure; table options handle visible at top-right.
- **Manual/visual**: Visual verification that all three handles appear at correct positions and all bottom sheets match design system.

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved assumptions remain.
- [x] All visual states are defined in `design.md`.
- [x] All navigation outcomes are defined.
