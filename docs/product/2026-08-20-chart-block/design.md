# Feature Design — Table to Chart Block

**Date**: 2026-08-20  
**Status**: Draft  
**Source request**: Add a chart block that converts table data into Bar, Line, or Pie charts  
**Related spec**: `spec.md`  
**Project design system**: `docs/product/design_system.md`  
**Approved design-system exceptions**: None. The chart block follows the existing Note Editor card, table, Basic Blocks panel, Material 3 bottom sheets, and semantic color contracts.  

---

## Conditional Keyboard-Visible Mockup Contract

Not applicable. Chart data editing remains an inline table surface. The Chart View and Chart Options bottom sheets contain selection controls only. If implementation introduces a bottom-sheet editing control, a separate keyboard-visible mockup must be added before the design stage can pass.

## Screens Covered

| # | Screen / Surface | Status |
|---|---|---|
| 1 | Note Editor — Chart Preview Block | Updated |
| 2 | Note Editor — Chart Table-Data View | Updated |
| 3 | Chart View and Chart Options Bottom Sheets | New |
| 4 | Basic Blocks and Table Conversion Actions | Updated |

---

## Screen 1 — Note Editor — Chart Preview Block

### Purpose

Show a compact, readable chart directly in the note while exposing two deliberate controls: the top-left Current View CTA shows Chart/Data mode, and the top-right Options CTA chooses which data column is plotted.

### UX Principles

- **Chart first, data always available**: The visual is the default, and the top-left Current View CTA makes the exact editable data one sheet action away.
- **Explicit data selection**: The top-right Options CTA makes the plotted column visible and changeable rather than silently choosing a series.
- **Stable editor card family**: Reuse the elevated 12 dp card treatment already established by Code and Mermaid blocks.
- **Selection explains itself**: Tapping a bar, line point, or pie slice highlights that datum and shows its category/value tooltip; no zoom or gesture-only discovery is required.
- **Empty is recoverable**: Empty or invalid data shows a calm localized message while both CTAs remain available.

### Entry And Exit

- **Entry points**:
  - Tap `Bar chart`, `Line chart`, or `Pie chart` in the Advanced Basic Blocks panel to create a new `Category` / `Value` chart.
  - Focus an existing table, open Table Options, and select `Convert to Bar chart`, `Convert to Line chart`, or `Convert to Pie chart`.
  - Open a saved note containing a ChartBlock.
- **Primary success exit**: The chart remains inline in the Note Editor and auto-saves with the note.
- **Current View CTA**: Top-left CTA displays Chart or Data and opens the Chart/Data bottom sheet; selecting an option changes the inline body and dismisses the sheet.
- **Options CTA**: Top-right CTA opens the Chart Options bottom sheet; selecting a candidate column updates the plot and selection, then dismisses the sheet.
- **Failure recovery**: Keep both CTAs and the table-data view available when rendering or export fails; never discard table rows or columns.

### Information Architecture

1. **Card container**: Full editor content width with 16 dp horizontal content inset, white/surface background, subtle border, 12 dp rounded corners, and 2 dp elevation.
2. **Header row**:
- **Top-left Current View CTA**: 48 dp-or-larger labeled button showing the current view (`Chart` or `Data`). It opens the view sheet, whose choices are `Chart` and `Data`; it is the first focusable control in the card.
   - Center title: Editable title in 16 sp semibold `textPrimary` when editable; plain title when read-only.
   - Small non-interactive chart type label (`Bar chart`, `Line chart`, or `Pie chart`) and selected data-column label may sit beside/below the title without competing with the CTAs. Chart type is chosen only during creation or table conversion.
   - **Top-right Options CTA**: 48 dp icon/button with a localized `Chart options` description. It is always visible when the chart is inspectable.
   - Delete action follows existing block deletion behavior and is hidden/disabled in read-only notes.
3. **Chart body**:
   - Responsive chart surface with one selected numeric series, axes/legend/labels supplied by the chart adapter, and semantic app colors.
   - No zoom or pan controls.
4. **Selection overlay**:
   - Selected bar/point/slice uses the library selection state plus a high-contrast tooltip/data callout.
   - Tooltip contains category, selected column label, and value; it remains inside the card and can be dismissed by tapping the plot or opening either sheet.
5. **Empty/error body**:
   - Centered localized message and a short hint to use View → Table to enter data or Options to choose another column.
   - Both top-corner CTAs remain enabled for inspection in editable and read-only notes.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Chart card container | Own the chart block layout | Content, empty, error, read-only | `editor_chart_block_<stable-block-id>` |
| Current View CTA | Open Chart/Data view selector sheet | Chart selected, Data selected, enabled/read-only | `editor_chart_view_cta_<stable-block-id>` |
| Chart title | Name the chart | Editable, read-only, blank fallback | `editor_chart_title_<stable-block-id>` |
| Chart type pill | Communicate selected Bar/Line/Pie type | Bar, Line, Pie | `editor_chart_type_<stable-block-id>` |
| Selected column label | Show plotted header | Valid, positional fallback, empty | `editor_chart_selected_column_<stable-block-id>` |
| Options CTA | Open data-column options sheet | Enabled, read-only inspect | `editor_chart_options_cta_<stable-block-id>` |
| Chart plot | Render and receive selection | Loading, content, selected datum, empty/error | `editor_chart_plot_<stable-block-id>` |
| Selection tooltip | Show category and selected value | Hidden, visible, dismissible | `editor_chart_tooltip_<stable-block-id>` |
| Empty state | Explain missing usable data | Empty, render fallback | `editor_chart_empty_<stable-block-id>` |
| Delete chart action | Delete chart and owned data | Editable, read-only disabled/hidden | `editor_chart_delete_<stable-block-id>` |

Dynamic tags use immutable persisted block IDs and column IDs; row indexes and user-generated text are not used in test tags.

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Chart card aligns with editor content | `editor_chart_block_<id>`, `note_editor_content` | Card left/right are 16 dp inside content bounds, within ±2 dp. |
| Current View CTA anchors the top-left header | `editor_chart_view_cta_<id>`, `editor_chart_block_<id>` | CTA has 48 dp target and begins at the card’s content start within ±2 dp. |
| Options CTA anchors the top-right header | `editor_chart_options_cta_<id>`, `editor_chart_block_<id>` | CTA has 48 dp target and ends at the card’s content end within ±2 dp. |
| Header title stays between CTAs | `editor_chart_view_cta_<id>`, `editor_chart_title_<id>`, `editor_chart_options_cta_<id>` | Title bounds do not overlap either CTA and centers vertically within ±2 dp. |
| Plot stays inside card body | `editor_chart_plot_<id>`, `editor_chart_block_<id>` | Plot bounds remain inside card bounds with at least 12 dp body padding. |
| Tooltip stays attached to selected plot | `editor_chart_tooltip_<id>`, `editor_chart_plot_<id>` | Tooltip overlaps the plot content region without leaving the card’s horizontal bounds. |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Loading | Card chrome with compact progress indicator in the plot region | Open either sheet; wait for render |
| Empty | Chart icon, localized empty message, Current View CTA, Options CTA | Open Data view and edit data; choose another column |
| Content | Bar/Line/Pie plot, selected-column label, title, and both corner CTAs | Tap a datum, inspect tooltip, open either sheet, edit title, delete chart |
| Selected datum | Highlighted bar/point/slice and tooltip with category/value | Dismiss selection, select another datum, open either sheet |
| Render error | Localized non-crashing error message with both CTAs | Fix data in table view or choose another column |
| Read-only | Content/empty chart with both inspection CTAs; mutation controls disabled | Inspect chart, select data, inspect table view and options |

### Interaction Rules

- **Current View CTA**: Shows the current `Chart` or `Data` view and opens the View Mode sheet; it does not directly mutate persisted chart data or navigate to a new route.
- **Options CTA**: Opens the Chart Options sheet with candidate columns from the second column onward. The current selection is checked/highlighted.
- **Column selection**: Choosing a column updates the selected column ID and chart immediately, persists through the existing debounce, and dismisses the sheet.
- **Selection**: A tap on a bar, point, or slice invokes the adapter callback and updates tooltip state. Selection does not mutate table data.
- **Title**: Editable title changes use the existing debounced auto-save path; a blank title displays localized `Chart`.
- **Delete**: Uses the existing block deletion path; because the table is owned by the chart, both disappear together. Read-only notes cannot delete.
- **No zoom**: Do not add pinch, pan, fullscreen, or zoom controls.

### Copy Requirements

| Element | Copy |
|---|---|
| Default title | `Chart` |
| Chart type labels | `Bar chart`, `Line chart`, `Pie chart` |
| Current View CTA | `Chart` / `Data` |
| Options CTA | `Chart options` |
| Empty state | Localized message equivalent to `Add valid numeric data or choose another column to see the chart` |
| Render error | Localized message equivalent to `Chart unavailable; review the table data` |
| Selected column | Header text or localized positional fallback such as `Column 2` |
| Tooltip | Localized category, selected column, and value labels |
| Delete action | Existing localized editor delete-chart action |

### Accessibility

- Chart plot exposes a concise chart summary: chart type, title, selected column, and number of valid categories.
- View and Options CTAs have distinct localized content descriptions and are first/last header controls in TalkBack order.
- Selection tooltip is announced through semantics when a bar/point/slice is tapped.
- Selected data is communicated by both visual highlight and text tooltip; color alone is never the only signal.
- All interactive controls use at least 48×48 dp targets, support TalkBack traversal, font scaling, RTL, and dark theme.

### Responsive And Configuration Behavior

- Card width follows the editor content width in portrait, landscape, and tablets.
- Plot labels may wrap or simplify according to library support; the table remains the authoritative fallback for dense data.
- Chart rows, columns, type, and selected column survive recomposition, rotation, and note reload through the persisted document.
- Bottom sheets respect safe drawing insets and clear transient tooltip selection when dismissed.

### Design Assets

- **Mockup image — chart preview**: `design/mockup_chart_block_preview.png` — regenerated light-theme chart card with top-left View CTA, top-right Options CTA, selected data column, and tooltip. The original vector source remains available as `design/mockup_chart_block_preview.svg`.
- **Keyboard-visible mockup**: `Not applicable`.

### Out Of Scope For This Design

- Chart zoom/pan/fullscreen.
- Showing multiple data columns at the same time.
- Column setup wizard beyond the existing table editor.

---

## Screen 2 — Note Editor — Chart Table-Data View

### Purpose

Expose the exact rows and columns behind the chart for editing and inspection. This is a view of the same ChartBlock, not a second document block.

### UX Principles

- **Data fidelity**: Show persisted table values and headers exactly as entered.
- **Flexible shape**: Allow the user to add rows and columns, while preserving the first category column and at least one candidate data column.
- **Reuse existing table conventions**: Follow the current grid, focused-cell behavior, scrolling, row/column handles, and table typography where compatible.

### Entry And Exit

- **Entry**: Current View CTA → View Mode sheet → Data.
- **Primary success exit**: Current View CTA → View Mode sheet → Chart; edits remain in the ChartBlock and auto-save.
- **Back/cancel**: Dismisses the view sheet without undoing committed edits.
- **Failure recovery**: Invalid cells remain visible for correction; the plot can show an empty/partial state.

### Information Architecture

1. **Header**: Current View CTA at top left, chart title/type/selected column, Options CTA at top right.
2. **Variable-width grid**: First column is the category label; later columns are candidate data series. Use existing table border/divider tokens and horizontal scrolling for wide data.
3. **Row/column controls**: Editable notes reuse existing row and column insertion/deletion/clear actions, exposed from the Options bottom sheet rather than inline in the Data view. Guards prevent deletion of the category column or the last candidate data column.
4. **Data hint**: A localized supporting message explains that the chart uses the selected column and invalid numeric cells are skipped.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Data view container | Own table mode | Editable, read-only | `editor_chart_table_view_<stable-block-id>` |
| Variable grid | Display/edit rows and columns | Focused, horizontal scroll, read-only | `editor_chart_data_grid_<stable-block-id>` |
| Cell editor | Edit a cell value | Editable, focused, read-only | `editor_chart_data_cell_<stable-block-id>` |
| Add row action | Add a row across current columns from Options | Editable, disabled/read-only | `editor_chart_add_row_<stable-block-id>` |
| Add column action | Add a candidate data column from Options | Editable, disabled/read-only | `editor_chart_add_column_<stable-block-id>` |
| Row options action | Clear/delete row | Editable, disabled/read-only | `editor_chart_row_options_<stable-block-id>` |
| Column options action | Clear/delete column with invariants | Editable, disabled/read-only | `editor_chart_column_options_<stable-block-id>` |
| Data hint | Explain selected-column mapping/filtering | Visible, localized | `editor_chart_data_hint_<stable-block-id>` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Data grid aligns with chart card | `editor_chart_data_grid_<id>`, `editor_chart_block_<id>` | Grid left/right align with card content inset within ±2 dp. |
| Data hint follows grid | `editor_chart_data_hint_<id>`, `editor_chart_data_grid_<id>` | Hint begins after the grid with at least 8 dp spacing and remains inside card width. |
| Row/column targets remain accessible | `editor_chart_data_cell_<id>`, `editor_chart_row_options_<id>`, `editor_chart_column_options_<id>` | Interactive controls meet the project’s 48 dp target contract. |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Editable data | Variable-width grid with row/column actions and hint | Edit cells, add/delete rows, add/delete columns within guards, open View/Options sheets |
| Empty data | Header row and empty data treatment | Add rows/columns and values, choose a column, show chart |
| Invalid/mixed data | Raw values remain visible; hint explains skipped values; chart state is not destructive | Correct cells, add rows/columns, choose another column |
| Read-only data | Same values and headers without mutation controls | Scroll/inspect, open View/Options sheets |

### Interaction Rules

- Cell edits replace line breaks with spaces using existing table input rules.
- Column insertion generates a stable column ID and an empty header/value column; it does not automatically change the selected column.
- Column deletion removes the selected column from the options list. If the selected column is deleted, select the first remaining data column; guard deletion when no data column would remain.
- Row insertion creates one empty cell for every current column. Row deletion removes only that row and recalculates the chart.
- Returning to Chart view uses the latest state immediately; no explicit save button is required. Add row and Add column are not shown inline and are available from the Options sheet.

### Copy Requirements

| Element | Copy |
|---|---|
| Data hint | Localized equivalent to `The chart uses the selected column; invalid numeric cells are skipped.` |
| Empty data hint | Localized equivalent to `Add a category and numeric value to create the chart.` |
| Add row | Existing localized add-row action adapted for chart data |
| Add column | Existing localized add-column action adapted for chart data |
| Current View CTA | `Chart` / `Data` |
| Options CTA | `Chart options` |

### Accessibility

- Grid semantics expose row/column position, category/data meaning, and read-only state.
- The selected plotted column is announced in the data hint and options sheet.
- Column and row guards expose localized disabled explanations rather than silently dropping actions.
- Cell, row, column, View, and Options actions have localized content descriptions and stable chart-scoped tags.

### Responsive And Configuration Behavior

- Keep the current horizontal table scrolling behavior for wide converted tables.
- Preserve variable column IDs and selected column through rotation and JSON reload.
- Support large font scales without clipping cell text or hiding the View/Options CTAs.

### Design Assets

- **Mockup image — chart table-data view**: `design/mockup_chart_block_table_view.png` — regenerated Data view with the current-view `Data` control, Options control, editable chart data grid, focused cell, and row/column actions. The original vector source remains available as `design/mockup_chart_block_table_view.svg`.
- **Keyboard-visible mockup**: `Not applicable`.

### Out Of Scope For This Design

- Spreadsheet formulas, sorting, filtering, or cell formatting.
- Reordering columns or plotting multiple columns simultaneously.

---

## Screen 3 — Chart View and Chart Options Bottom Sheets

### Purpose

Provide explicit, accessible bottom-sheet controls for switching the inline body and choosing the plotted data column without adding persistent toolbar clutter to the chart card.

### UX Principles

- **One choice per sheet**: View Mode controls where the user is looking; Chart Options controls what data is plotted.
- **Material 3 consistency**: Use existing `ModalBottomSheet` geometry, surface, 16 dp top corners, 56 dp action rows, safe insets, and semantic app colors.
- **Immediate feedback**: The current choice is selected/highlighted; applying a choice updates the card and dismisses the sheet.

### Entry And Exit

- **View Mode entry**: Tap the top-left Current View CTA on a chart or data view.
- **Options entry**: Tap the top-right Options CTA on a chart or table-data view.
- **Exit**: Tap Chart/Data or a data-column row to apply and dismiss; system back/outside tap dismisses without changing the current choice.
- **Read-only**: Sheets remain inspectable, but selection changes are disabled and semantically announced.

### Information Architecture

1. **View Mode sheet**:
   - Title `View`.
   - Two 56 dp rows: `Chart` and `Data`.
   - Selected row uses `highlight` background, primary icon/check, and selected semantics.
2. **Options sheet**:
   - Title `Options`.
   - Three action rows: `Data column`, `Add row`, and `Add column`.
   - Tapping `Data column` opens the second-level Data column sheet; Add row and Add column execute directly.
3. **Data column sheet**:
   - Title `Data column` with a back action.
   - Supporting text: `Choose the data column to plot`.
   - One radio/list row per candidate column after the category column. Use header text, or `Column N` fallback for blank headers.
   - Current selected column uses `highlight` and a check/radio indicator.
   - If there are no candidate data columns, show an empty message and a disabled/absent selection action.
3. **Sheet footer**: Respect safe drawing/navigation insets; no keyboard state is needed.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| View Mode sheet | Select Chart/Data body | Open, dismissed, Chart selected, Data selected, read-only | `editor_chart_view_sheet` |
| Chart option | Select plot body | Selected/unselected, enabled/disabled | `editor_chart_view_option_chart` |
| Data option | Select data body | Selected/unselected, enabled/disabled | `editor_chart_view_option_data` |
| Chart Options sheet | Select data column | Open, dismissed, populated, empty, read-only | `editor_chart_options_sheet` |
| Data-column option | Select candidate data column | Selected/unselected, enabled/disabled | `editor_chart_option_column_<stable-column-id>` |
| Sheet supporting text | Explain current choice | Localized | `editor_chart_sheet_supporting_text` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Sheet top meets rounded surface | `editor_chart_view_sheet`, `editor_chart_options_sheet` | Top corners are 16 dp and sheet respects bottom safe inset. |
| Sheet rows use standard action height | `editor_chart_view_option_chart`, `editor_chart_option_column_<id>` | Each row is at least 56 dp and has a 48 dp interactive target. |
| Current selection is visually distinct | selected option tag, sheet container | Selected row uses `highlight` plus icon/semantics, not color alone. |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| View sheet open | Chart/Data rows with current selection | Choose either view or dismiss |
| Options sheet open | Candidate data columns with current selection | Choose a column or dismiss |
| No data columns | Localized message explaining that a data column must be added | Dismiss and add a column in Data view |
| Read-only | Current selections visible but disabled | Inspect and dismiss |

### Interaction Rules

- Selecting Chart or Data updates only transient view state; it does not change persisted table data.
- Tapping Data column opens the second-level Data column sheet. Selecting a data column updates `selectedColumnId`, recomputes the plot, schedules auto-save, and dismisses the sheet.
- Add row and Add column are direct actions from the first-level Options sheet and return to the chart/data view after applying.
- Blank/duplicate headers are disambiguated by stable column IDs and positional fallback labels.
- Sheets contain selection controls only and have no keyboard-dependent interaction.

### Copy Requirements

| Element | Copy |
|---|---|
| View title | `View` |
| Chart option | `Chart` |
| Data option | `Data` |
| Options title | `Chart options` |
| Options supporting text | `Choose the data column to plot` |
| No data columns | `Add a data column in the table view to create a chart` |
| Selected semantics | Localized equivalent to `Selected` |

### Accessibility

- Modal sheet title and current selection are announced when each sheet opens.
- View options use radio-button semantics; column options use single-selection semantics.
- Disabled read-only selections expose a localized reason.
- All rows meet 48 dp touch targets and support TalkBack, large text, RTL, and dark theme.

### Responsive And Configuration Behavior

- Sheets use full available width, safe drawing insets, and standard 16 dp top radii.
- Large data-column labels wrap rather than clip.
- Recomposition does not duplicate selections or rows; dismissing a sheet leaves the selected persisted value intact.

### Design Assets

- **Mockup image — View Mode sheet**: `design/mockup_chart_block_view_sheet.svg`.
- **Mockup image — Options sheet**: `design/mockup_chart_block_options_sheet.png` — first-level sheet with Data column, Add row, and Add column rows.
- **Mockup image — Data column sheet**: `design/mockup_chart_block_data_column_sheet.png` — second-level sheet opened from Data column, with Revenue, Cost, and Tax selection.
- **Keyboard-visible mockup**: `Not applicable`.

### Out Of Scope For This Design

- Multi-select data columns.
- Search/filter controls inside the options sheet.
- Chart type changes from the options sheet; chart type is chosen at creation/conversion.

---

## Screen 4 — Basic Blocks and Table Conversion Actions

### Purpose

Provide discoverable actions for creating new charts and converting existing tables without changing the established editor toolbar geometry.

### UX Principles

- **Parallel chart choices**: Bar, Line, and Pie appear as separate, equally discoverable Advanced actions.
- **Conversion stays contextual**: Existing tables are converted from their Table Options surface, keeping source selection explicit.
- **Compact panel**: Preserve the current Basic Blocks panel’s 56 dp toolbar, 48 dp tile targets, capped height, and scroll behavior.

### Entry And Exit

- **New chart entry**: Editor toolbar → Basic Blocks trigger → Advanced → Bar/Line/Pie tile.
- **Conversion entry**: Focus table cell → Table Options → Convert to Bar/Line/Pie action.
- **Exit**: Insertion closes the Basic Blocks panel; conversion closes the options sheet and replaces the selected table in place.
- **Read-only**: Trigger remains visible but disabled according to existing Basic Blocks behavior; conversion actions are absent/disabled.

### Information Architecture

1. **Advanced section**: Existing Code and Mermaid tiles remain; Bar chart, Line chart, and Pie chart tiles follow them in the same two-column grid.
2. **Table Options**: Existing row/column/table actions remain available. Add a `Convert to chart` group with Bar, Line, and Pie actions; Delete table remains the final destructive action.
3. **Result**: New chart uses `Category` / `Value` columns with Value selected. Conversion uses the existing table’s columns, rows, and first data column selected.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| Bar chart tile | Create new Bar ChartBlock | Enabled, read-only disabled | `basic_blocks_bar_chart` |
| Line chart tile | Create new Line ChartBlock | Enabled, read-only disabled | `basic_blocks_line_chart` |
| Pie chart tile | Create new Pie ChartBlock | Enabled, read-only disabled | `basic_blocks_pie_chart` |
| Convert-to-chart group | Expose contextual conversions | Expanded, dismissed | `table_convert_chart_group` |
| Convert Bar action | Convert selected TableBlock | Enabled, unavailable | `table_convert_bar_chart` |
| Convert Line action | Convert selected TableBlock | Enabled, unavailable | `table_convert_line_chart` |
| Convert Pie action | Convert selected TableBlock | Enabled, unavailable | `table_convert_pie_chart` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Advanced chart tiles preserve panel geometry | `basic_blocks_bar_chart`, `basic_blocks_line_chart`, `basic_blocks_pie_chart`, `basic_blocks_panel` | Panel remains capped at the existing geometry and each tile is at least 48 dp high. |
| Conversion group follows table actions | `table_convert_chart_group`, `table_options_sheet` | Conversion group is inside the existing sheet and Delete remains the final destructive action. |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Advanced panel | Bar, Line, and Pie tiles with chart icon/labels | Create a new chart with one tap |
| Conversion actions | Bar, Line, and Pie conversion choices | Convert focused table or dismiss |
| Read-only | Chart creation/conversion disabled or hidden | Continue inspecting note |

### Interaction Rules

- Tapping any Basic chart tile always creates a new chart with `Category` / `Value` columns, even when a table is focused.
- Existing table conversion is available only from Table Options and replaces the selected TableBlock in place.
- Insertion/conversion uses localized labels and auto-save behavior; no network is accessed.

### Copy Requirements

| Element | Copy |
|---|---|
| Section | Existing `Advanced` label |
| Bar tile | `Bar chart` / `Insert Bar chart` |
| Line tile | `Line chart` / `Insert Line chart` |
| Pie tile | `Pie chart` / `Insert Pie chart` |
| Conversion group | `Convert to chart` |
| Conversion actions | `Convert to Bar chart`, `Convert to Line chart`, `Convert to Pie chart` |

### Accessibility

- Each chart tile and conversion action has a localized content description, Button role, stable test tag, and 48 dp minimum target.
- The selected/focused table is announced before conversion options.
- Read-only disabled state is exposed semantically, not by color alone.

### Responsive And Configuration Behavior

- Use the existing two-column Basic Blocks grid and capped scrolling panel.
- Keep chart actions reachable under large font scale by allowing tile labels to wrap/ellipsis according to existing panel rules.
- Conversion sheet respects safe drawing insets and does not introduce editable controls or IME behavior.

### Design Assets

- **Mockup image — creation and conversion actions**: `design/mockup_chart_block_creation_panel.png` — regenerated from the current `2026-08-16-basic-blocks-sheet/design/mockup_basic_blocks_panel.png` visual language, with separate Bar chart, Line chart, and Pie chart insertion actions. The original chart-block vector source remains available as `design/mockup_chart_block_creation_panel.svg`.
- **Keyboard-visible mockup**: `Not applicable`.

### Out Of Scope For This Design

- A chart setup wizard beyond the existing table-data view.
- Automatic conversion of a focused table from the Basic Blocks tile.
- Multi-select column configuration.

## Design-System Verification Checklist

- [x] Feature design links to `docs/product/design_system.md`.
- [x] Light-theme baseline uses `background` `#F8F7FF`, `surface` `#FFFFFF`, `primary` `#7C6CF2`, `textPrimary` `#191627`, `textSecondary` `#7B7694`, `border` `#E7E3F6`, and `divider` `#E7EBF0` through semantic tokens.
- [x] Chart cards reuse the established Code/Mermaid 12 dp rounded, bordered, elevated card family.
- [x] Chart View and Chart Options use the existing Material 3 bottom-sheet contract with 16 dp top corners, surface background, safe insets, and 56 dp rows.
- [x] Basic Blocks panel preserves the existing compact 56 dp toolbar, capped scroll region, two-column grid, and 48 dp tile targets.
- [x] All interactive elements have planned localized labels and stable test tags.
- [x] Dark theme, large text, RTL, read-only, empty, invalid, selected-data, and no-data-column states are specified.
- [x] No design-system exception was introduced.
