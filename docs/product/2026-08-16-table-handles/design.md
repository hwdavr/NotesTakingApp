# Feature Design — Table Column, Row & Table Handles

**Date**: 2026-08-16
**Status**: Final
**Source request**: User requests interactive column/row/table handles on table cells with bottom sheet options for structural and table-level operations.
**Related spec**: `spec.md`
**Project design system**: `docs/product/design_system.md`
**Approved design-system exceptions**: None

---

## Conditional Keyboard-Visible Mockup Contract

Not applicable — the bottom sheets in this feature contain only tappable action rows with no editable controls or typing surfaces.

---

## Screens Covered

| # | Screen / Surface | Status |
|---|---|---|
| 1 | Table Block with Handles (Note Editor) | Updated |
| 2 | Column Options Bottom Sheet | New |
| 3 | Row Options Bottom Sheet | New |
| 4 | Table Options Bottom Sheet | New |

---

## Screen 1 — Table Block with Column, Row & Table Handles

### Purpose

Provides visual affordances (handles) for users to access table structure operations on the currently focused column and row, plus table-level operations via a corner icon. This extends the existing `TableDocumentBlock` composable within the Note Editor.

### UX Principles

- **Progressive disclosure**: Handles only appear when a cell is focused, keeping the table clean by default.
- **Contextual relevance**: The column handle appears above the focused column; the row handle appears to the left of the focused row; the table options handle sits at the top-right corner, spatially reinforcing scope.
- **Minimal intrusion**: Column/row handles are subtle highlighted bars; the table options handle is a compact icon button that doesn’t obstruct cell content.

### Entry And Exit

- **Entry points**: User taps any cell in a `TableBlock` within the Note Editor (edit mode only).
- **Primary success exit**: User taps a handle to open the options bottom sheet, or taps away to dismiss handles.
- **Cancel/back behavior**: Tapping outside the table dismisses the handles; no data is lost.
- **Failure exit or recovery**: No failure state — handles are local UI overlays.

### Information Architecture

1. **Column Handle Strip**: A thin highlighted bar positioned directly above the focused cell’s column, spanning the full column width. Contains a small centered grip dot as a visual affordance. Uses `primary` color (`#7C6CF2`) at 15% opacity for the background.
2. **Row Handle Strip**: A thin highlighted bar positioned directly to the left of the focused cell’s row, spanning the full row height. Contains a small centered grip dot. Uses `primary` color (`#7C6CF2`) at 15% opacity for the background.
3. **Table Options Handle**: A small circular or rounded-square icon button at the top-right corner of the table, containing a “more options” (three-dot ellipsis) icon in `primary` (`#7C6CF2`) with a subtle `primary` at 15% opacity background. Appears whenever any cell in the table is focused.
4. **Focused Cell Highlight**: The focused cell has a subtle highlight using `primary` at 8% opacity to reinforce which cell is active.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Column Handle Strip | Opens column options bottom sheet | visible/hidden | `table_column_handle` |
| Row Handle Strip | Opens row options bottom sheet | visible/hidden | `table_row_handle` |
| Table Options Handle | Opens table options bottom sheet | visible/hidden | `table_options_handle` |
| Focused Cell Highlight | Indicates which cell is active | highlighted/normal | `editor_table_cell` (existing) |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| No cell focused | Standard table without handles | Tap any cell to focus |
| Cell focused | Column handle above column, row handle left of row, table options handle at top-right, cell highlighted | Tap any handle to open options, tap another cell, tap outside to dismiss |
| Read-only | Standard table without handles | View table content only |

### Interaction Rules

- **Column handle tap**: Opens the Column Options bottom sheet targeting the focused column index.
- **Row handle tap**: Opens the Row Options bottom sheet targeting the focused row index.
- **Table options handle tap**: Opens the Table Options bottom sheet.
- **Cell tap**: Focuses the cell; shows column handle, row handle, and table options handle.
- **Tap outside table**: Dismisses all handles.
- **Read-only mode**: No handles are rendered.
- **Gestures**: No swipe or drag gestures; tap only.

### Copy Requirements

No text labels on the handles themselves — they are purely visual affordances (bars with grip dots).

### Accessibility

- Column handle: `contentDescription = stringResource(R.string.table_column_handle_description)` — e.g., "Column options".
- Row handle: `contentDescription = stringResource(R.string.table_row_handle_description)` — e.g., "Row options".
- Table options handle: `contentDescription = stringResource(R.string.table_options_handle_description)` — e.g., "Table options".
- All handles meet minimum 48×48dp touch target requirement.
- Focus order: cell → column handle → row handle → table options handle → next cell.

### Responsive And Configuration Behavior

- On narrow phones, handles should not overflow; column handle width matches column width, row handle height matches row height.
- Configuration change (rotation) preserves focused cell state via ViewModel.

### Design Assets

- **Mockup image**: `design/mockup_table_handles_v2.png` — Regenerated reference showing the complete 3×3 table, focused "Designer" cell, column handle above the "Role" column, row handle to the left of row 2, and table options handle at the top-right corner with non-overlapping callouts. The prior `mockup_table_handles.png` remains as historical design evidence.
- **Keyboard-visible mockup**: Not applicable.

### Out Of Scope For This Design

- Handle drag-to-reorder columns or rows.
- Handle for selecting entire column/row for bulk formatting.

---

## Screen 2 — Column Options Bottom Sheet

### Purpose

Presents the available structural operations for the focused table column in a standard M3 modal bottom sheet.

### UX Principles

- **Consistent with existing sheets**: Follows the same visual pattern as `EditorNoteActionsSheet` — white surface, rounded top corners (16dp), drag handle, action rows.
- **Destructive action separation**: "Delete column" is visually separated by a divider and uses the `error` color (`#C44A4A`) for both icon and text to warn the user.

### Entry And Exit

- **Entry points**: User taps the column handle strip.
- **Primary success exit**: User selects an option; sheet dismisses and table updates immediately.
- **Cancel/back behavior**: Swipe down or tap outside to dismiss without any action.
- **Failure exit or recovery**: No failure state.

### Information Architecture

1. **Drag Handle**: Standard M3 drag handle bar at the top center.
2. **Title**: "Column Options" — bold, `textPrimary` (`#191627`), 16sp.
3. **Action Rows** (56dp height each):
   - Row 1: Insert column left icon + "Insert column left" label
   - Row 2: Insert column right icon + "Insert column right" label
   - Divider: `divider` (`#E7EBF0`)
   - Row 3: Clear/eraser icon + "Clear column" label
   - Divider
   - Row 4: Delete icon (`error` tint) + "Delete column" label (`error` color)

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Insert Column Left Row | Inserts empty column at position left of focused | default/pressed | `table_insert_column_left` |
| Insert Column Right Row | Inserts empty column at position right of focused | default/pressed | `table_insert_column_right` |
| Delete Column Row | Removes focused column (or entire block if last) | default/pressed | `table_delete_column` |
| Clear Column Row | Clears all cells in focused column | default/pressed | `table_clear_column` |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Visible | Sheet with 4 options, destructive action in red | Tap an option or dismiss |

### Interaction Rules

- **Tap any option**: Executes the operation, dismisses the sheet, updates the table.
- **Tap "Delete column" on last column**: Removes entire `TableBlock` from the document.
- **Swipe down / tap outside**: Dismisses without action.

### Copy Requirements

| Element | Copy |
|---|---|
| Title | Column Options |
| Option 1 | Insert column left |
| Option 2 | Insert column right |
| Option 3 | Clear column |
| Option 4 | Delete column |

### Accessibility

- Each action row has `contentDescription` matching the label text.
- Minimum touch target: 48×48dp (row height 56dp satisfies this).
- Focus order follows visual order: Insert left → Insert right → Delete → Clear.

### Responsive And Configuration Behavior

- Standard bottom sheet behavior; fills width on phones, may be narrower on tablets per M3 guidelines.

### Design Assets

- **Mockup image**: `design/mockup_column_bottom_sheet.png` — Shows the Column Options bottom sheet with all four options, dimmed background with table visible behind.
- **Keyboard-visible mockup**: Not applicable.

### Out Of Scope For This Design

- Column sort options.
- Column rename/header edit from this sheet.

---

## Screen 3 — Row Options Bottom Sheet

### Purpose

Presents the available structural operations for the focused table row in a standard M3 modal bottom sheet.

### UX Principles

- Same visual pattern and principles as the Column Options bottom sheet for consistency.

### Entry And Exit

- **Entry points**: User taps the row handle strip.
- **Primary success exit**: User selects an option; sheet dismisses and table updates immediately.
- **Cancel/back behavior**: Swipe down or tap outside to dismiss without any action.
- **Failure exit or recovery**: No failure state.

### Information Architecture

1. **Drag Handle**: Standard M3 drag handle bar at the top center.
2. **Title**: "Row Options" — bold, `textPrimary` (`#191627`), 16sp.
3. **Action Rows** (56dp height each):
   - Row 1: Insert row above icon + "Insert row above" label
   - Row 2: Insert row below icon + "Insert row below" label
   - Divider: `divider` (`#E7EBF0`)
   - Row 3: Clear/eraser icon + "Clear row" label
   - Divider
   - Row 4: Delete icon (`error` tint) + "Delete row" label (`error` color)

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Insert Row Above Row | Inserts empty row above focused | default/pressed | `table_insert_row_above` |
| Insert Row Below Row | Inserts empty row below focused | default/pressed | `table_insert_row_below` |
| Delete Row Row | Removes focused row (or entire block if last) | default/pressed | `table_delete_row` |
| Clear Row Row | Clears all cells in focused row | default/pressed | `table_clear_row` |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Visible | Sheet with 4 options, destructive action in red | Tap an option or dismiss |

### Interaction Rules

- **Tap any option**: Executes the operation, dismisses the sheet, updates the table.
- **Tap "Delete row" on last row**: Removes entire `TableBlock` from the document.
- **Swipe down / tap outside**: Dismisses without action.

### Copy Requirements

| Element | Copy |
|---|---|
| Title | Row Options |
| Option 1 | Insert row above |
| Option 2 | Insert row below |
| Option 3 | Clear row |
| Option 4 | Delete row |

### Accessibility

- Each action row has `contentDescription` matching the label text.
- Minimum touch target: 48×48dp (row height 56dp satisfies this).
- Focus order follows visual order: Insert above → Insert below → Delete → Clear.

### Responsive And Configuration Behavior

- Standard bottom sheet behavior; fills width on phones, may be narrower on tablets per M3 guidelines.

### Design Assets

- **Mockup image**: `design/mockup_row_bottom_sheet.png` — Shows the Row Options bottom sheet with all four options, dimmed background with table visible behind.
- **Keyboard-visible mockup**: Not applicable.

### Out Of Scope For This Design

- Row drag-to-reorder from this sheet.
- Row duplication (explicitly scoped out by user).

---

## Screen 4 — Table Options Bottom Sheet

### Purpose

Presents the available table-level operations for the entire focused table in a standard M3 modal bottom sheet.

### UX Principles

- **Consistent with existing sheets**: Follows the same visual pattern as Column/Row Options sheets — white surface, rounded top corners (16dp), drag handle, action rows.
- **Destructive action separation**: "Delete table" is visually separated by a divider and uses the `error` color (`#C44A4A`) for both icon and text.

### Entry And Exit

- **Entry points**: User taps the table options handle at the top-right corner of the table.
- **Primary success exit**: User selects an option; sheet dismisses and table updates immediately.
- **Cancel/back behavior**: Swipe down or tap outside to dismiss without any action.
- **Failure exit or recovery**: No failure state.

### Information Architecture

1. **Drag Handle**: Standard M3 drag handle bar at the top center.
2. **Title**: "Table Options" — bold, `textPrimary` (`#191627`), 16sp.
3. **Action Rows** (56dp height each):
   - Row 1: Clear/sweep icon + "Clear entire table" label
   - Row 2: Copy/duplicate icon + "Duplicate table" label
   - Divider: `divider` (`#E7EBF0`)
   - Row 3: Fit-to-width/expand icon + "Fit to width" label
   - Divider
   - Row 4: Delete icon (`error` tint) + "Delete table" label (`error` color)

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Clear Entire Table Row | Clears all cell content to empty | default/pressed | `table_clear_all` |
| Duplicate Table Row | Creates a deep copy of the table below the original | default/pressed | `table_duplicate` |
| Delete Table Row | Removes the entire TableBlock | default/pressed | `table_delete` |
| Fit to Width Row | Toggles equal-width column sizing | default/pressed | `table_fit_to_width` |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Visible | Sheet with 4 options, destructive action in red | Tap an option or dismiss |

### Interaction Rules

- **Tap "Clear entire table"**: Replaces all cell content with empty `RichText("")`, dismisses sheet.
- **Tap "Duplicate table"**: Deep-copies the entire `TableBlock` and inserts the copy immediately after the original in the document, dismisses sheet.
- **Tap "Delete table"**: Removes the entire `TableBlock` from the document, dismisses sheet.
- **Tap "Fit to width"**: Toggles the table between default column sizing and equal-width columns filling the available width, dismisses sheet.
- **Swipe down / tap outside**: Dismisses without action.

### Copy Requirements

| Element | Copy |
|---|---|
| Title | Table Options |
| Option 1 | Clear entire table |
| Option 2 | Duplicate table |
| Option 3 | Fit to width |
| Option 4 | Delete table |

### Accessibility

- Each action row has `contentDescription` matching the label text.
- Minimum touch target: 48×48dp (row height 56dp satisfies this).
- Focus order follows visual order: Clear → Duplicate → Delete → Fit to width.

### Responsive And Configuration Behavior

- Standard bottom sheet behavior; fills width on phones, may be narrower on tablets per M3 guidelines.

### Design Assets

- **Mockup image**: `design/mockup_table_options_sheet.png` — Shows the Table Options bottom sheet with all four options, dimmed background with table visible behind.
- **Keyboard-visible mockup**: Not applicable.

### Out Of Scope For This Design

- Table-level formatting (merge, cell colors, alignment).
- Table column/row count summary or statistics.
