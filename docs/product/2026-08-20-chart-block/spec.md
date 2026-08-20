# Feature Spec — Table to Chart Block

**Date**: 2026-08-20  
**Status**: Draft  
**Related design**: `design.md`  

---

## Objective

Add Bar, Line, and Pie chart blocks to the existing Note Editor. A chart owns an editable table-data view, renders one user-selected data column against the first category column, updates immediately when the data changes, and remains entirely local to the note. Users can create a new chart from the Basic Blocks panel or convert an existing table through Table Options.

## User Goal

As a note author, I want to turn tabular data into a Bar, Line, or Pie chart and choose which data column to visualize so that I can explore different measures without leaving my note or losing the underlying table.

## Scope

### In Scope

- Add a persisted `EditorBlock.ChartBlock` with chart type (`bar`, `line`, or `pie`), title, owned table rows, stable column identifiers, and the selected data-column identifier.
- Add separate Bar chart, Line chart, and Pie chart actions to the Advanced Basic Blocks panel.
- Create a new chart from the Basic Blocks panel with a two-column `Category` / `Value` table and an empty data area. Users can add rows and columns in the table-data view.
- Add Bar, Line, and Pie conversion actions to the existing Table Options flow. Conversion changes the selected `TableBlock` into one `ChartBlock` in the same document position, preserving its rows, columns, and order.
- Add a top-left current-view CTA that displays `Chart` or `Data` and opens a bottom sheet with Chart and Data choices.
- Add a top-right chart-options CTA that opens a two-level bottom-sheet flow. The first sheet offers Data column, Add row, and Add column; Data column opens a second sheet where the user selects the plotted column. The selected column applies to Bar, Line, and Pie charts.
- Allow cell, row, and column editing in the table-data view using the existing table operations, while maintaining a valid category column and at least one candidate data column.
- Recalculate chart data from the selected table column on every edit and auto-save it through the existing note editor persistence path.
- Map the first table column to category labels and every later column to candidate data series. The chart plots the selected candidate column only. The default selection is the first data column.
- Skip invalid numeric cells and rows with no usable chart value rather than blocking table editing. Show a localized empty-state message when the selected column has no usable chart data.
- Render clickable bars, line points, and pie slices with a tooltip/data callout containing the selected category and value. No zoom interaction is required.
- Use a Compose-compatible chart library behind an app-owned rendering/selection adapter. The selected library must support Bar, Line, Pie, click selection, tooltips, theme colors, API 24+, and a renderable bitmap/export path.
- Render charts in light and dark themes using `LocalAppColors` semantic tokens.
- Preserve chart data, type, columns, and selected column in local note JSON and display charts/table data in read-only notes without edit controls.
- Export Markdown as a ZIP package containing the Markdown file, the chart table representation, and sibling PNG chart assets referenced by relative Markdown image links.
- Render each chart as an image in PDF export.

### Out Of Scope

- A separate linked `TableBlock` behind a chart. The chart owns its table-data view; there is one document block and one deletion lifecycle.
- Displaying multiple data columns in one chart at the same time. The user selects one candidate data column per chart.
- Formulas, computed fields, sorting, filtering, spreadsheet-style formatting, column reordering, or column renaming outside editing the header cell.
- Pinch-to-zoom, pan, fullscreen chart viewing, animation tuning, or live collaboration.
- Cloud/API chart rendering or network-dependent data sources.
- Converting a chart back into a separate `TableBlock`.

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---|---|---|
| Jetpack Compose Material 3 | Existing project version | Chart card, table-data view, bottom sheets, controls, empty/error states, and accessibility semantics. |
| Compose chart library supporting Bar/Line/Pie selection | New dependency; pin the currently compatible release during implementation | Plot rendering, hit testing, selected-point/bar state, tooltip anchors, and theme-aware chart primitives. YCharts is the initial candidate; the adapter must isolate the app from library-specific models and APIs. |
| Android `Bitmap`, `Canvas`, and `PdfDocument` | Existing Android SDK | Deterministic chart bitmap generation for PDF and Markdown package assets. |
| Room note repository + `org.json` document serializer | Existing project implementation | Local note persistence and backward-compatible block JSON. |
| Kotlin Coroutines / StateFlow | Existing project implementation | Debounced auto-save and reactive chart/table UI state. |

### Key Technical Decisions

- **Single source of truth**: `ChartBlock` stores its table rows and column IDs. The chart preview is derived from the selected column; the table-data view edits the same rows. Deleting the chart deletes both the visual and its data because no second block exists.
- **Variable table shape**: A new chart starts with two columns (`Category`, `Value`) and empty data rows. Users may insert/delete rows and columns through the table-data view. The first column remains the category axis; the implementation guards against deleting the category column or leaving no data column.
- **Stable column selection**: Persist `columnIds` and `selectedColumnId` in the ChartBlock. Header text is user-editable but does not identify a column. When the selected column is deleted, fall back to the first remaining data column; when a new chart is created, select the `Value` column.
- **Chart data mapping**: The first column is the category label. Every later column is a candidate numeric series. The selected candidate column alone is plotted for all chart types. Blank/non-numeric cells are ignored; rows with no valid category/value pair are omitted from the plot.
- **Bottom-sheet navigation**: The top-left view CTA opens a standard Material 3 bottom sheet with Chart and Table choices. The top-right options CTA opens a standard Material 3 bottom sheet with candidate data-column choices and the current selection. These are UI state and are not persisted.
- **Chart library boundary**: Introduce an app-owned chart model and renderer/selection adapter so UI, persistence, and export do not depend directly on the chosen library’s data classes. A library compatibility check must prove Bar, Line, Pie, click selection, tooltip content, API 24 support, and bitmap rendering before the dependency is finalized.
- **View state**: Selected view, selected datum, open sheet, and tooltip visibility are transient UI state. Chart type, title, column IDs, rows, and selected data-column ID are persisted.
- **Export package**: Markdown export is a portable ZIP containing one `.md` file and one PNG per chart, with relative paths such as `assets/chart_<stable-block-id>.png`. PDF export draws the same chart bitmap into the existing PDF renderer.
- **Layering**: Table-to-chart parsing, column selection, and invalid-data filtering remain pure and testable outside Composables. ViewModels coordinate use cases and persistence; Composables receive state and callbacks; the chart library is isolated in a UI/platform adapter.

### External APIs / Services

- None. Chart rendering, storage, and export are local-only.

### Platform & Compatibility Constraints

- **Min SDK**: API 24 / Android 7.0.
- **Target SDK**: API 34.
- **Permissions required**: None for chart creation or rendering. Storage access follows the existing Storage Access Framework export flow.
- **Network**: Not required; the chart library and all rendering assets must work offline.
- **Runtime boundary**: Instrumented Android verification is required for the real chart library renderer, click selection/tooltip behavior, bottom sheets, bitmap generation, and PDF/Markdown package output. Missing emulator/runtime support is an evidence failure and must fail loudly.

## Functional Requirements

- **FR-001**: The document model MUST support `EditorBlock.ChartBlock` with stable block ID, chart type, title, variable table rows, stable column IDs, and a selected data-column ID.
- **FR-002**: Chart block JSON MUST use a backward-compatible `type: "chart"` representation with chart type, columns, selected column, and rows, and preserve unknown/legacy blocks during deserialization.
- **FR-003**: The Advanced Basic Blocks panel MUST expose separate Bar chart, Line chart, and Pie chart actions with localized labels, descriptions, stable test tags, and at least 48 dp touch targets.
- **FR-004**: Selecting a chart action from the Basic Blocks panel MUST append a new chart after the focused block, or at the end when no block is focused, with `Category` / `Value` columns, the `Value` column selected, and no valid data rows.
- **FR-005**: The Table Options flow MUST expose Bar, Line, and Pie conversion actions for a selected `TableBlock` and convert it in place while preserving data, column order, and document position. The first data column becomes selected by default.
- **FR-006**: A chart’s top-left current-view CTA MUST display `Chart` or `Data` and open a bottom sheet with Chart and Data choices. Choosing Data MUST show the chart’s owned editable table-data view; choosing Chart MUST show the plot.
- **FR-007**: A chart’s top-right options CTA MUST open a first-level Options sheet with Data column, Add row, and Add column rows. Choosing Data column MUST open a second-level sheet listing candidate data columns from column two onward. Choosing a column MUST update the plotted series and selected state without changing table values.
- **FR-008**: Chart table editing MUST allow cell, row, and column changes while preserving the first category column and at least one candidate data column. Added columns MUST receive stable IDs and appear in the options sheet.
- **FR-009**: The chart parser MUST treat the first column as category labels and later columns as candidate numeric series. The selected candidate column MUST be used for Bar, Line, and Pie charts.
- **FR-010**: Invalid numeric cells and rows with no usable chart value MUST be skipped without crashing or preventing further table edits. A chart with no usable values in the selected column MUST show a localized empty message while keeping both bottom-sheet CTAs available.
- **FR-011**: The selected chart library adapter MUST render Bar, Line, and Pie charts and expose click selection for bars, line points, and pie slices.
- **FR-012**: Selecting a datum MUST show a localized tooltip/data callout containing its category and selected-column value, and the selection MUST be dismissible without changing table data.
- **FR-013**: Chart data edits, column selection, chart title edits, row/column operations, and conversion/insertion actions MUST use the existing debounced auto-save path. Reloading the note MUST restore the chart, its variable table, and selected column.
- **FR-014**: Read-only notes MUST render the chart and allow the table-data view and bottom sheets to be inspected, but MUST disable cell/row/column edits, chart creation/conversion, and destructive actions.
- **FR-015**: Markdown export MUST produce a ZIP containing the note Markdown, a Markdown table for every chart, and sibling PNG chart assets referenced by relative image links.
- **FR-016**: PDF export MUST render each chart as a bitmap image with its title and must continue exporting the owned data table representation when a chart cannot produce a non-empty image.
- **FR-017**: All chart controls, data-table controls, bottom sheets, tooltips, empty states, and conversion actions MUST use localized strings, semantic app colors, accessible labels, stable test tags, and minimum touch targets.

## Acceptance Criteria

- **AC-001**: Given an editable note, when the user taps Bar chart, Line chart, or Pie chart in the Basic Blocks panel, then a new chart block is inserted after the focused block (or appended), the panel closes, and the chart starts with `Category` and `Value` columns with `Value` selected.
- **AC-002**: Given an editable note containing a table, when the user opens Table Options and selects a chart type, then the table is converted in place into one chart block with the same rows, columns, document position, and first data column selected.
- **AC-003**: Given a chart preview, when the user taps the top-left current-view CTA, then a bottom sheet appears with Chart and Data choices; choosing Data shows the owned table-data view and choosing Chart shows the plot.
- **AC-004**: Given a chart with at least two candidate data columns, when the user taps the top-right options CTA and selects another column, then the bottom sheet reflects the selection and the chart redraws from the newly selected column without changing table values.
- **AC-005**: Given a chart table-data view, when the user adds a row or column and edits its cells, then the table keeps the new shape, the new column appears in the options sheet, and the selected column remains selected unless it was deleted.
- **AC-006**: Given a table with category labels and a selected numeric column, when the chart renders, then Bar, Line, and Pie use that selected column; invalid cells and rows are skipped, and no-valid-data shows the localized empty message.
- **AC-007**: Given a chart with valid data, when the user taps a bar, line point, or pie slice, then a tooltip/data callout shows its category and selected-column value and the selected datum is visually identified.
- **AC-008**: Given a read-only note containing a chart, when the user opens it, then the chart, data view, view sheet, and options sheet are inspectable while all mutations and destructive actions are disabled.
- **AC-009**: Given an edited, resized, or converted chart, when the note is saved and reloaded, then chart type, title, rows, columns, column IDs, selected column, and both view states remain functionally correct without data loss.
- **AC-010**: Given a note containing charts, when Markdown export is selected, then a ZIP is produced containing the Markdown note, a table for each chart, each referenced chart PNG, and valid relative image links.
- **AC-011**: Given a note containing a chart, when PDF export is selected, then the PDF contains the chart title and a non-empty rendered chart image at the correct document position.
- **AC-012**: Given light or dark theme and a supported API 24+ device, when a chart is rendered, a bottom sheet is opened, and a datum is selected, then colors, labels, selection, tooltip, controls, and sheet semantics remain readable and functional without network access.

## Data And Persistence

`ChartBlock` is persisted inside the existing `Note.content` JSON document. The planned shape is:

```json
{
  "id": "b_chart_123",
  "type": "chart",
  "chartType": "bar",
  "title": "Monthly sales",
  "columnIds": ["c_category", "c_value", "c_cost"],
  "selectedColumnId": "c_value",
  "rows": [
    [[{"text": "Category"}], [{"text": "Value"}], [{"text": "Cost"}]],
    [[{"text": "January"}], [{"text": "120"}], [{"text": "80"}]]
  ]
}
```

- `chartType` accepts `bar`, `line`, or `pie`; unknown values fall back to Bar for safe rendering while preserving readable table content.
- The first `columnIds` entry is the category column. Later IDs identify candidate data columns.
- `selectedColumnId` identifies the plotted data column. If absent or invalid, deserialization selects the first data column.
- Existing `TableBlock` JSON remains supported for notes that have not been converted.
- Chart view/table view, open sheet, selected datum, and tooltip visibility are transient UI state.
- Conversion replaces the selected table block in the document; it does not leave a second source block.

## Edge Cases

- **New chart with no data**: Start with a `Category` / `Value` header row, select `Value`, and show the localized empty message until a valid row exists.
- **Header-only or blank rows**: Keep the table editable and show the empty message.
- **Blank category**: Ignore the row for plotting while retaining it in the table.
- **Blank/non-numeric selected value**: Ignore that cell. If no usable value remains in the row, omit the row from the plot.
- **Negative Bar/Line values**: Plot them using the chart library’s supported baseline behavior.
- **Negative or all-zero Pie values**: Ignore non-positive/invalid slices; show the empty message when no positive slice remains.
- **Added column**: Generate a stable column ID, use the header cell as its options label, and leave the existing selected column unchanged.
- **Deleted selected column**: Select the first remaining data column. If no data column remains, keep the table editable but show the empty message and guide the user to add a column.
- **Attempt to delete category column or last data column**: Prevent the destructive operation and expose a localized explanation; the chart must retain a valid minimum shape.
- **Duplicate/blank headers**: Keep stable IDs distinct and use localized positional fallbacks such as `Column 2` in the options sheet.
- **Read-only notes**: Show chart, table data, view sheet, and options sheet but disable all mutations; options selection may be inspected but cannot be saved.
- **Library render failure**: Keep both bottom-sheet CTAs available and show a localized non-crashing chart error/empty state; do not discard persisted data.
- **Export image failure**: Keep the Markdown table, add an export error note rather than a broken image link, and render a PDF fallback table/text block.
- **Rotation/recomposition**: Preserve persisted data and selected column; transient sheet/tooltip state may clear without changing data.
- **Large tables**: Maintain existing horizontal/vertical table scrolling and show only valid points from the selected column.

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|---|---|
| A1 | “Independent table and chart” means one chart block owns two views of the same table data, not two independently deletable document blocks. | A separate linked-table model would require a different persistence and deletion contract. |
| A2 | New charts start with two columns and users may add/delete rows and columns after insertion; the first category column and at least one data column are protected invariants. | A different minimum-shape rule would change table operation guards. |
| A3 | The selected data column applies to all Bar, Line, and Pie chart types; new charts default to the first data column and converted charts default to the first source data column. | A pie-only first-series rule would require a separate type-specific selection contract. |
| A4 | Markdown package export may change the existing single-file Markdown export flow to a ZIP containing the Markdown and sibling assets. | Existing consumers that expect a standalone `.md` file need to handle the package format. |
| A5 | The selected Compose chart library can provide deterministic bitmap output or can be rendered through the adapter onto an Android `Canvas`. | A library replacement or dedicated export renderer would be needed if this is false. |
| A6 | Existing table row/column operations can be reused while protecting the first category and last data-column invariants. | A dedicated chart table editor may be required if current table handles cannot safely enforce these guards. |

## Open Questions

All questions have been clarified with the user.

| # | Question | Status | Answer |
|---|---|---|---|
| Q1 | How can users create charts? | ✅ Answered | Both Basic Blocks Bar/Line/Pie actions and Table Options conversion actions. |
| Q2 | How is chart data related to the table? | ✅ Answered | One chart owns a toggleable editable table-data view; source edits update the chart and data cannot be deleted separately. |
| Q3 | Can chart table rows and columns change? | ✅ Answered | Yes. Users can add rows and columns in the table-data view. |
| Q4 | How does the user choose chart data? | ✅ Answered | A top-right chart CTA opens a bottom sheet to select which data column is plotted. |
| Q5 | How does the user switch chart/data views? | ✅ Answered | A top-left current-view CTA displays Chart or Data and opens a bottom sheet with Chart and Data choices. |
| Q6 | How are invalid values handled? | ✅ Answered | Skip invalid cells/rows and show an empty message when the selected column has no usable data. |
| Q7 | What export formats are required? | ✅ Answered | PDF uses a rendered image; Markdown is a ZIP package with table Markdown and sibling PNG assets/image references. |
| Q8 | What chart interactions are required? | ✅ Answered | Bar/Line/Pie charts support click selection and tooltips; zoom is not required. |
| Q9 | What is the new-chart table shape? | ✅ Answered | Start with `Category` / `Value`; later rows and columns can be added. |

## Screen States

| State | Requirement | Acceptance Criteria |
|---|---|---|
| Basic Blocks chart actions | Bar, Line, and Pie insertion tiles are visible and disabled in read-only notes | AC-001, AC-008 |
| Chart preview — empty | Card shows top-left view CTA, top-right options CTA, title, chart type, and localized empty message | AC-001, AC-006 |
| Chart preview — content | Card shows the selected chart and both top corner CTAs | AC-004, AC-006, AC-012 |
| Chart preview — selected datum | Selected bar/point/slice is visually identified and tooltip shows category/value data | AC-007 |
| Chart/data view sheet | Bottom sheet shows Chart and Data choices with current selection | AC-003, AC-012 |
| Chart options sheet | Bottom sheet lists candidate data columns with current selected column | AC-004, AC-008, AC-012 |
| Chart table-data view — editable | Variable-width table supports cell, row, and column operations | AC-005, AC-006 |
| Chart table-data view — read-only | Data is inspectable; all mutation controls are disabled | AC-008 |
| Conversion options | Table Options offers Bar, Line, and Pie conversion actions and dismisses after selection | AC-002 |
| Render error/fallback | Non-crashing localized error/empty state keeps both sheets and table data accessible | AC-006, AC-011 |
| Exporting | Markdown package and PDF export show progress/success/error using existing export state patterns | AC-010, AC-011 |

## Navigation

- **Entry — new chart**: Note Editor → Basic Blocks panel → Bar chart, Line chart, or Pie chart.
- **Entry — conversion**: Note Editor → focused existing table → Table Options → Convert to Bar/Line/Pie.
- **View selection**: Chart top-left current-view CTA → bottom sheet → Chart or Data; selection updates the inline block without a route change.
- **Column selection**: Chart top-right CTA → bottom sheet → select data column; selection updates the inline plot without a route change.
- **Back/cancel**: Dismisses either sheet without changing unsaved table data; returning to Chart view preserves edits.
- **Success**: The selected chart remains in the note, auto-saves, and is restored after reload.
- **Error recovery**: Keep both sheets and the table-data view available; the user changes selection or data without losing the chart block.

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|---|---|---|
| FR-001–FR-002 | Chart block model and persistence | AC-009 |
| FR-003–FR-005 | Creation and conversion surfaces | AC-001, AC-002 |
| FR-006–FR-007 | View and options bottom sheets | AC-003, AC-004 |
| FR-008–FR-010 | Variable table and data mapping | AC-005, AC-006 |
| FR-011–FR-012 | Chart library adapter and selection tooltip | AC-007, AC-012 |
| FR-013–FR-014 | Persistence and read-only behavior | AC-008, AC-009 |
| FR-015–FR-016 | Markdown package and PDF rendering | AC-010, AC-011 |
| FR-017 | Accessibility, localization, and themes | AC-001, AC-003, AC-004, AC-012 |

## Verification Expectations

- **Unit**:
  - `ChartTableParserTest`: variable-width rows, stable column IDs, selected-column mapping, numeric parsing, invalid-cell/row filtering, Pie/Bar/Line behavior, negative/all-zero handling, and minimum-shape guards.
  - `NoteDocumentTest`: ChartBlock JSON round-trip, selected-column persistence/fallback, legacy TableBlock preservation, and variable row/column normalization.
  - `ChartExportModelTest`: Markdown table/image-reference generation, selected-column chart asset generation, and deterministic asset names.
- **Integration**:
  - `NoteEditorViewModelIntegrationTest`: Basic Blocks insertion, Table Options conversion, view selection, data-column selection, row/column edits, live chart state, auto-save, reload, read-only guards, and deletion lifecycle.
  - `NoteExporterTest`: Markdown ZIP contents/image references and PDF chart image/fallback behavior.
- **Instrumented UI**:
  - `ChartBlockCardTest`: top-left view sheet, top-right options sheet, chart/table view, selectable columns, variable table editing, empty state, and tooltip accessibility.
  - `ChartLibraryPlatformTest`: real selected library rendering of Bar/Line/Pie and click selection on API 24+ emulator.
  - `ChartVisualFlowTest`: production editor chart preview, both bottom sheets, table-data view, Basic Blocks chart actions, conversion options, selected tooltip, and export-ready chart states with in-test screenshots.
- **Manual/visual**:
  - Compare Light/Dark chart card, variable table view, view sheet, options sheet, Basic Blocks panel, conversion options, and selected tooltip against `design.md` and the scalable mockups.
  - Verify exported ZIP contains valid relative PNG references and PDF contains a visible chart image.

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved product assumptions remain.
- [x] All visual states are defined in `design.md`.
- [x] All navigation and export outcomes are defined.
