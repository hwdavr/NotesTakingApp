# Sprint Contract — Table to Chart Block

## 🏃 Sprint Overview

* **Sprint:** P08-20
* **Feature:** Table to Chart Block
* **Duration:** 1 sprint, delivered in four vertical slices

## 🎯 Scope

### In Scope

* [ ] Persisted `EditorBlock.ChartBlock` with backward-compatible JSON and table-owned data.
* [ ] Bar, Line, and Pie creation from Advanced Basic Blocks and in-place Table Options conversion.
* [ ] Chart/Data view switching with matching current-view controls.
* [ ] Editable chart Data view with protected category/data-column invariants.
* [ ] Two-level Options flow: Data column → candidate-column selection; Add row and Add column as main-sheet rows.
* [ ] Local chart rendering, data filtering, datum selection, tooltips, themes, empty/error states, and read-only inspection.
* [ ] Markdown ZIP and PDF chart export with deterministic bitmap/table fallbacks.
* [ ] Real Android rendering/export boundary verification and in-test visual evidence.

### Out of Scope

* [ ] Multiple plotted series at once, formulas, sorting, filtering, formatting, column reordering, or external data sources.
* [ ] Chart-type changes from the chart card or Options sheet; type is chosen at creation/conversion.
* [ ] Pinch zoom, pan, fullscreen chart viewing, live collaboration, cloud rendering, or network access.
* [ ] A separate linked TableBlock or conversion back to a separate TableBlock.

## Platform Capability & Environment Contract

See `platform-capability-matrix.md`. This feature is platform-bound because the shipped chart renderer and export path use Android Canvas/Bitmap/PdfDocument and must be verified on an API 24+ Android runtime. The required boundary test is `TC-US-4-PLATFORM`; a missing emulator or runtime is an evidence failure and must fail loudly.

## Spec Coverage Matrix

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001 | ChartBlock stores ID, type, title, rows, column IDs, selected column | US-1 | TC-US-1-01 | In scope |
| FR-002 | Chart JSON is backward-compatible; unknown chartType/selectedColumn fallback and legacy blocks preserved | US-1 | TC-US-1-01, TC-US-1-06, TC-US-1-07, TC-US-1-08 | In scope |
| FR-003 | Advanced panel has localized Bar/Line/Pie actions and tags | US-1 | TC-US-1-02 | In scope |
| FR-004 | Basic insertion creates Category/Value chart after focus or at end | US-1 | TC-US-1-02 | In scope |
| FR-005 | Table Options converts table in place and preserves data/order | US-1 | TC-US-1-03 | In scope |
| FR-006 | Current-view CTA shows Chart/Data and switches owned view | US-2 | TC-US-2-01 | In scope |
| FR-007 | Main Options sheet opens Data column second-level selection | US-2 | TC-US-2-02 | In scope |
| FR-008 | Data editing supports rows/columns with protected invariants | US-2 | TC-US-2-03 | In scope |
| FR-009 | First column is category; selected later column is plotted | US-2 | TC-US-2-04 | In scope |
| FR-010 | Invalid values are skipped and empty state is localized | US-3 | TC-US-3-02 | In scope |
| FR-011 | Adapter renders Bar/Line/Pie and exposes hit selection | US-1 | TC-US-1-04 | In scope |
| FR-012 | Datum selection shows dismissible category/value callout | US-3 | TC-US-3-01 | In scope |
| FR-013 | Edits, selection, title, insertion/conversion auto-save and reload | US-2 | TC-US-2-05 | In scope |
| FR-014 | Read-only chart/Data/sheets inspectable; mutations disabled | US-3 | TC-US-3-03 | In scope |
| FR-015 | Markdown export is ZIP with Markdown tables and relative PNG links | US-4 | TC-US-4-01 | In scope |
| FR-016 | PDF exports chart bitmap and table fallback | US-4 | TC-US-4-02 | In scope |
| FR-017 | Localization, semantic colors, tags, accessibility, touch targets | US-4 | TC-US-4-04 | In scope |
| AC-001 | Basic Blocks creates Bar/Line/Pie chart with defaults | US-1 | TC-US-1-02 | In scope |
| AC-002 | Table converts in place preserving rows/order | US-1 | TC-US-1-03 | In scope |
| AC-003 | Current-view CTA opens Chart/Data sheet and switches view | US-2 | TC-US-2-01 | In scope |
| AC-004 | Data column selection updates plot without table mutation | US-2 | TC-US-2-02 | In scope |
| AC-005 | Row/column edits preserve shape and selection rules | US-2 | TC-US-2-03 | In scope |
| AC-006 | All chart types map selected column and filter invalid data | US-3 | TC-US-3-02 | In scope |
| AC-007 | Bar/point/slice selection shows category/value callout | US-3 | TC-US-3-01 | In scope |
| AC-008 | Read-only chart/Data/sheets are inspectable without mutation | US-3 | TC-US-3-03 | In scope |
| AC-009 | Reload restores persisted chart data and selected column | US-2 | TC-US-2-05 | In scope |
| AC-010 | Markdown ZIP contains note, chart tables, PNGs, relative links | US-4 | TC-US-4-01 | In scope |
| AC-011 | PDF contains chart title and non-empty chart image | US-4 | TC-US-4-02 | In scope |
| AC-012 | Light/dark API 24+ runtime remains readable and functional | US-4 | TC-US-4-04 | In scope |
| AC-013 | Unknown `chartType` falls back to Bar and preserves table content | US-1 | TC-US-1-06 | In scope |
| AC-014 | Missing/invalid `selectedColumnId` selects the first data column | US-1 | TC-US-1-07 | In scope |
| AC-015 | Legacy `TableBlock` remains readable and unconverted | US-1 | TC-US-1-08 | In scope |
| Edge: header-only/blank rows | Editable table remains and empty state is shown | US-3 | TC-US-3-02 | In scope |
| Edge: blank category | Row is retained but omitted from plot | US-3 | TC-US-3-02 | In scope |
| Edge: negative/all-zero Pie | Non-positive slices are ignored; empty state if none remain | US-3 | TC-US-3-02 | In scope |
| Edge: delete protected columns | Category and last data column cannot be deleted | US-2 | TC-US-2-03 | In scope |
| Edge: duplicate/blank headers | Stable IDs remain distinct and positional fallback is shown | US-2 | TC-US-2-02 | In scope |
| Edge: renderer/export failure | Data remains and localized fallback is shown/exported | US-4 | TC-US-4-03 | In scope |
| NFR: local-only/offline | No network or cloud dependency | US-1 | TC-US-1-04 | In scope |
| NFR: API 24/target 34 | Runtime compatibility is verified | US-4 | TC-US-4-PLATFORM | In scope |
| NFR: architecture | UI → presentation → domain ← data boundaries are preserved | US-1 | TC-US-1-05 | In scope |
| NFR: testability | Pure parser/selection/filtering logic has JVM tests | US-2 | TC-US-2-04 | In scope |
| Design: chart card | Uses current-view Chart/Data control and compact three-dot control | US-3 | TC-US-4-VIS-01 | In scope |
| Design: Data view | Matches chart card; no inline Add row/Add column or Live banner | US-2 | TC-US-4-VIS-02 | In scope |
| Design: Options flow | Two levels; main rows are Data column/Add row/Add column | US-2 | TC-US-4-VIS-03 | In scope |
| Design: accessibility | Tags, semantics, labels, themes, and 48dp targets | US-4 | TC-US-4-04 | In scope |

## User Scenarios & Testing

### US-1: Create, convert, persist, and render chart blocks (Priority: P1)

An author inserts a Bar, Line, or Pie chart from Advanced Basic Blocks or converts an existing table from Table Options. The chart appears in place, owns its table data, renders locally, and survives note reload.

**Why this priority**: It establishes the persisted block and proves the highest-risk rendering dependency through an existing editor entry point.

**Independent Test**: Open an editable note, insert or convert a chart, reload the note, and assert the production editor contains the same chart type/data/selection.

**Acceptance Criteria**:

1. **AC-US-1-01 Given** valid ChartBlock JSON, **When** it is deserialized and serialized, **Then** chart fields and unknown blocks are preserved.
2. **AC-US-1-02 Given** an editable note, **When** Bar, Line, or Pie is chosen from Advanced Basic Blocks, **Then** the correct default chart is inserted after focus or appended.
3. **AC-US-1-03 Given** a focused table, **When** a chart conversion is chosen, **Then** the table is replaced in place with preserved rows/order and the first data column selected.
4. **AC-US-1-04 Given** chart data for each chart type, **When** the production adapter renders it offline, **Then** a non-empty chart surface and selectable datum targets are available on API 24+.
5. **AC-US-1-05 Given** a new chart foundation, **When** code is reviewed and tested, **Then** UI/presentation/domain/data boundaries and localized interactive tags are maintained.
6. **AC-US-1-06 Given** chart JSON with an unknown `chartType`, **When** it is deserialized, **Then** the chart falls back to Bar and the readable table content is preserved.
7. **AC-US-1-07 Given** chart JSON with a missing or invalid `selectedColumnId`, **When** it is deserialized, **Then** the first data column is selected.
8. **AC-US-1-08 Given** a note containing a legacy `TableBlock`, **When** it is deserialized, **Then** the table remains readable and is not converted into a chart.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-1-01 | AC-US-1-01 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentChartBlockTest.kt#testChartBlockRoundTripsAndPreservesUnknownBlocks` | Parse JSON containing chart and unknown block, then serialize/reload | Assert all chart fields, stable IDs, and unknown block readable content survive | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentChartBlockTest.testChartBlockRoundTripsAndPreservesUnknownBlocks"` |
| TC-US-1-02 | AC-US-1-02 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorChartBlockIntegrationTest.kt#testInsertEachChartTypeUsesDefaultsAndFocusInsertion` | Invoke production ViewModel insertion for each BasicBlockType | Assert block order, Category/Value columns, selected Value, and auto-save | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorChartBlockIntegrationTest.testInsertEachChartTypeUsesDefaultsAndFocusInsertion"` |
| TC-US-1-03 | AC-US-1-03 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorChartBlockIntegrationTest.kt#testConvertTableInPlacePreservesRowsAndOrder` | Focus a table and invoke production conversion action | Assert one ChartBlock occupies the original index with source rows/columns and first data selected | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorChartBlockIntegrationTest.testConvertTableInPlacePreservesRowsAndOrder"` |
| TC-US-1-04 | AC-US-1-04 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartCreationFlowTest.kt#testAllChartTypesRenderOfflineAndExposeDatumTargets` | Render deterministic local fixtures through production chart adapter | Assert Bar/Line/Pie surfaces are non-empty and hit targets expose selection callbacks with network disabled | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartCreationFlowTest#testAllChartTypesRenderOfflineAndExposeDatumTargets` |
| TC-US-1-05 | AC-US-1-05 | JVM quality | Architecture and localization checks over changed production files | Run project quality gates after slice implementation | Assert no forbidden layer imports, no hardcoded interactive strings, and stable tags exist | `./gradlew ktlintCheck detekt` |
| TC-US-1-06 | AC-US-1-06 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentChartBlockTest.kt#testUnknownChartTypeFallsBackToBar` | Parse chart JSON containing an unknown `chartType` value | Assert the chart deserializes as Bar and the owned table rows remain intact | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentChartBlockTest.testUnknownChartTypeFallsBackToBar"` |
| TC-US-1-07 | AC-US-1-07 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentChartBlockTest.kt#testMissingOrInvalidSelectedColumnFallsBackToFirstDataColumn` | Parse chart JSON with a missing `selectedColumnId` and with a deleted/invalid ID | Assert both deserialize to the first data column selected | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentChartBlockTest.testMissingOrInvalidSelectedColumnFallsBackToFirstDataColumn"` |
| TC-US-1-08 | AC-US-1-08 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentChartBlockTest.kt#testLegacyTableBlockRemainsReadableAndUnconverted` | Parse a note containing a legacy `TableBlock` alongside a chart | Assert the legacy table is readable and is not converted into a ChartBlock | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentChartBlockTest.testLegacyTableBlockRemainsReadableAndUnconverted"` |

### US-2: Edit chart data and choose the plotted column (Priority: P2)

An author opens Data, edits the owned table, opens Options, uses Add row/Add column, or enters Data column to choose another series.

**Why this priority**: It delivers the core user value of exploring measures without losing the source table.

**Independent Test**: Start with a persisted ChartBlock fixture, switch to Data, edit cells and columns through production callbacks, select a second-level data column, and reload.

**Acceptance Criteria**:

1. **AC-US-2-01 Given** a chart in either view, **When** the current-view control is tapped and Chart/Data is chosen, **Then** the inline body changes without changing persisted data.
2. **AC-US-2-02 Given** candidate columns, **When** Options → Data column is opened and another column is selected, **Then** the selected ID and plotted values change while table values remain unchanged.
3. **AC-US-2-03 Given** an editable Data view, **When** Add row/Add column or cell/row/column operations occur through Options/table controls, **Then** shape and stable IDs update while protected columns cannot be deleted.
4. **AC-US-2-04 Given** mixed table values, **When** chart data is derived, **Then** the first column is category, selected later column is plotted, and invalid pairs are filtered deterministically.
5. **AC-US-2-05 Given** edited/converted chart data, **When** the note reloads, **Then** type, title, rows, columns, IDs, and selected column are restored.
6. **AC-US-2-06 Given** duplicate/blank headers or a deleted selected column, **When** Options is opened, **Then** stable IDs disambiguate labels and selection falls back to the first remaining data column.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-2-01 | AC-US-2-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartDataFlowTest.kt#testCurrentViewSwitchesBetweenChartAndData` | Tap production current-view CTA and choose Chart/Data | Assert only body mode changes and both controls remain reachable | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartDataFlowTest#testCurrentViewSwitchesBetweenChartAndData` |
| TC-US-2-02 | AC-US-2-02 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartDataFlowTest.kt#testOptionsOpensDataColumnSecondLevelAndSelectsColumn` | Tap Options → Data column → Cost | Assert second sheet, selected Cost, updated chart, unchanged grid values | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartDataFlowTest#testOptionsOpensDataColumnSecondLevelAndSelectsColumn` |
| TC-US-2-03 | AC-US-2-03 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorChartDataIntegrationTest.kt#testOptionsRowActionsAndProtectedColumnInvariants` | Invoke Add row/Add column and deletion operations through production ViewModel | Assert row/column shape, stable IDs, and protected deletion behavior | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorChartDataIntegrationTest.testOptionsRowActionsAndProtectedColumnInvariants"` |
| TC-US-2-04 | AC-US-2-04 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/chart/ChartDataMapperTest.kt#testSelectedColumnMappingSkipsInvalidPairs` | Map fixture with blank category, invalid number, negative, and all-zero values | Assert deterministic valid point/slice output and Pie positivity rule | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.chart.ChartDataMapperTest.testSelectedColumnMappingSkipsInvalidPairs"` |
| TC-US-2-05 | AC-US-2-05 | JVM integration | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorChartDataIntegrationTest.kt#testChartEditsAndSelectionReloadFromAutoSave` | Edit title/cell/column and select Cost, then recreate ViewModel | Assert persisted chart fields and selected column are restored | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorChartDataIntegrationTest.testChartEditsAndSelectionReloadFromAutoSave"` |
| TC-US-2-06 | AC-US-2-06 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/chart/ChartColumnSelectionTest.kt#testFallbackAndLabelsUseStableIds` | Provide duplicate/blank headers and delete selected column | Assert positional fallback labels and first remaining data selection | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.chart.ChartColumnSelectionTest.testFallbackAndLabelsUseStableIds"` |

### US-3: Select chart data and inspect read-only charts (Priority: P3)

An author selects bars, points, or slices and receives a localized callout. A read-only note remains fully inspectable but cannot mutate chart data or persistence.

**Why this priority**: It completes the interactive chart behavior and makes invalid/read-only states safe before export.

**Independent Test**: Render each chart type with deterministic data, tap a datum, dismiss the callout, then open the same chart in read-only mode and assert mutation controls are disabled.

**Acceptance Criteria**:

1. **AC-US-3-01 Given** valid chart data, **When** a bar, line point, or pie slice is tapped, **Then** it is highlighted and a callout shows category, selected column, and value.
2. **AC-US-3-02 Given** invalid/empty selected data, **When** the chart renders, **Then** a localized empty/error state appears while Chart/Data and Options remain available.
3. **AC-US-3-03 Given** a read-only note, **When** chart/Data/Options are inspected, **Then** values and sheets are visible but edits, selection persistence, creation/conversion, and destructive actions are disabled.
4. **AC-US-3-04 Given** a selected datum or open sheet, **When** it is dismissed or another view is opened, **Then** transient state clears without changing table data.
5. **AC-US-3-05 Given** light and dark themes, **When** the chart and sheets render, **Then** semantic colors, labels, tags, TalkBack semantics, RTL, and large text remain usable.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-3-01 | AC-US-3-01 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartInteractionFlowTest.kt#testDatumSelectionShowsAndDismissesCalloutForAllTypes` | Tap deterministic Bar/Line/Pie datum targets | Assert highlight, category/column/value semantics, and dismissal | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartInteractionFlowTest#testDatumSelectionShowsAndDismissesCalloutForAllTypes` |
| TC-US-3-02 | AC-US-3-02 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/chart/ChartStateReducerTest.kt#testEmptyAndRenderErrorStatesKeepRecoveryControls` | Reduce no-valid-data and renderer-failure states | Assert localized fallback state and available Chart/Data/Options actions | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.chart.ChartStateReducerTest.testEmptyAndRenderErrorStatesKeepRecoveryControls"` |
| TC-US-3-03 | AC-US-3-03 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartInteractionFlowTest.kt#testReadOnlyChartKeepsInspectionAndDisablesMutations` | Open read-only note with chart and Data view | Assert inspection controls exist and mutations/destructive controls are disabled/absent | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartInteractionFlowTest#testReadOnlyChartKeepsInspectionAndDisablesMutations` |
| TC-US-3-04 | AC-US-3-04 | JVM unit | `app/src/test/java/com/example/notesapp/ui/editor/chart/ChartStateReducerTest.kt#testTransientSelectionAndSheetDismissalDoesNotMutateBlock` | Apply selection/sheet open/dismiss events | Assert persisted ChartBlock equality before and after transient events | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.chart.ChartStateReducerTest.testTransientSelectionAndSheetDismissalDoesNotMutateBlock"` |
| TC-US-3-05 | AC-US-3-05 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartInteractionFlowTest.kt#testDarkThemeLargeTextAndRtlChartSemantics` | Render dark theme with large font and RTL locale fixture | Assert readable controls, localized content descriptions, tags, and traversal semantics | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartInteractionFlowTest#testDarkThemeLargeTextAndRtlChartSemantics` |

### US-4: Export charts and verify the complete visual flow (Priority: P4)

An author exports a note containing charts and uses the completed flow in the final visual states. This slice owns the real Android bitmap/PDF boundary and all screenshot evidence.

**Why this priority**: Export depends on the completed renderer and persistence path; visual verification is most meaningful only when all user flows are reachable.

**Independent Test**: Export deterministic chart notes to Markdown ZIP and PDF through the existing export screen, execute the real Android boundary test, and capture each designated state from the active instrumented test.

**Acceptance Criteria**:

1. **AC-US-4-01 Given** a note with charts, **When** Markdown export is selected, **Then** a ZIP contains note Markdown, one chart table per chart, PNG assets, and valid relative image links.
2. **AC-US-4-02 Given** a note with a chart, **When** PDF export is selected, **Then** title and non-empty chart bitmap appear at document position, with table fallback on image failure.
3. **AC-US-4-03 Given** renderer or export image failure, **When** export runs, **Then** persisted table data remains and localized table/error fallback is produced without a broken link.
4. **AC-US-4-04 Given** API 24+ Android runtime and deterministic local fixtures, **When** the production renderer and PdfDocument boundary run, **Then** bitmap/PDF output is non-empty and test failure is loud if runtime is missing.
5. **AC-US-4-05 Given** the completed chart feature, **When** the visual flow test captures Chart, Data, Options, empty/selected, and read-only/dark states, **Then** screenshots are saved from active rendering and match the approved mockups' UI chrome.

**Acceptance Test Cases**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-4-01 | AC-US-4-01 | JVM integration | `app/src/test/java/com/example/notesapp/util/NoteExporterChartTest.kt#testMarkdownChartExportProducesZipPackage` | Export deterministic note through production exporter | Assert ZIP entries, chart tables, PNG assets, relative links, and no broken links | `./gradlew testDebugUnitTest --tests "com.example.notesapp.util.NoteExporterChartTest.testMarkdownChartExportProducesZipPackage"` |
| TC-US-4-02 | AC-US-4-02 | JVM integration | `app/src/test/java/com/example/notesapp/util/NoteExporterChartTest.kt#testPdfChartExportUsesBitmapAndTableFallback` | Export valid and failing-render fixtures through production exporter | Assert title/image placement for success and table/text fallback for failure | `./gradlew testDebugUnitTest --tests "com.example.notesapp.util.NoteExporterChartTest.testPdfChartExportUsesBitmapAndTableFallback"` |
| TC-US-4-03 | AC-US-4-03 | JVM unit | `app/src/test/java/com/example/notesapp/util/NoteExporterChartTest.kt#testChartExportFailurePreservesDataAndReportsLocalizedFallback` | Force renderer/image failure in deterministic fixture | Assert original ChartBlock/table remains and export contains fallback/error note | `./gradlew testDebugUnitTest --tests "com.example.notesapp.util.NoteExporterChartTest.testChartExportFailurePreservesDataAndReportsLocalizedFallback"` |
| TC-US-4-04 | AC-US-4-04 | Instrumented platform boundary | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartPlatformBoundaryTest.kt#testProductionCanvasBitmapAndPdfDocumentBoundary` | Run shipped adapter/export path on API 24+ emulator with local fixture | Assert Android Bitmap/Canvas render and PdfDocument output are non-empty; unavailable runtime fails non-zero | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary` |
| TC-US-4-05 | AC-US-4-05 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartVisualFlowTest#captureCompletedChartVisualStates` | Navigate through production editor and capture five states from active test window | Assert each state and save non-empty PNG evidence for visual comparison | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartVisualFlowTest#captureCompletedChartVisualStates` |
| TC-US-4-PLATFORM | AC-US-4-04 | Instrumented platform boundary | `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartPlatformBoundaryTest.kt#testProductionCanvasBitmapAndPdfDocumentBoundary` | Exercise shipped Android Canvas/Bitmap/PdfDocument adapters with local fixture | Assert real platform output and no fake-only substitute | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary` |
| TC-US-4-VIS-01 | AC-US-4-05 | Visual verification | `ChartVisualFlowTest.kt#captureChartPreviewState` | Render active Chart view and capture `/sdcard/Download/chart_preview.png` | Assert chart card, current-view button, title, type label, plot, and Options control | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartVisualFlowTest#captureChartPreviewState && adb -s emulator-5554 pull /sdcard/Download/chart_preview.png "docs/product/2026-08-20-chart-block/visual_evidence/chart_preview.png" && test -s "docs/product/2026-08-20-chart-block/visual_evidence/chart_preview.png"` |
| TC-US-4-VIS-02 | AC-US-4-05 | Visual verification | `ChartVisualFlowTest.kt#captureDataViewState` | Render active Data view and capture `/sdcard/Download/chart_data_view.png` | Assert matching header controls, title, grid, focused cell, and no inline row/column buttons/banner | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartVisualFlowTest#captureDataViewState && adb -s emulator-5554 pull /sdcard/Download/chart_data_view.png "docs/product/2026-08-20-chart-block/visual_evidence/chart_data_view.png" && test -s "docs/product/2026-08-20-chart-block/visual_evidence/chart_data_view.png"` |
| TC-US-4-VIS-03 | AC-US-4-05 | Visual verification | `ChartVisualFlowTest.kt#captureOptionsSheetsState` | Capture first-level Options and second-level Data column sheets | Assert three main rows, nested column selection, and sheet styling | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartVisualFlowTest#captureOptionsSheetsState && adb -s emulator-5554 pull /sdcard/Download/chart_options_sheets.png "docs/product/2026-08-20-chart-block/visual_evidence/chart_options_sheets.png" && test -s "docs/product/2026-08-20-chart-block/visual_evidence/chart_options_sheets.png"` |
| TC-US-4-VIS-04 | AC-US-4-05 | Visual verification | `ChartVisualFlowTest.kt#captureEmptyAndSelectedStates` | Capture empty/error and selected-datum states | Assert localized empty/error message, selected datum, callout, and recovery controls | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartVisualFlowTest#captureEmptyAndSelectedStates && adb -s emulator-5554 pull /sdcard/Download/chart_empty_selected.png "docs/product/2026-08-20-chart-block/visual_evidence/chart_empty_selected.png" && test -s "docs/product/2026-08-20-chart-block/visual_evidence/chart_empty_selected.png"` |
| TC-US-4-VIS-05 | AC-US-4-05 | Visual verification | `ChartVisualFlowTest.kt#captureReadOnlyDarkState` | Capture read-only dark-theme Chart/Data inspection | Assert readable semantic colors and disabled/absent mutation controls | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartVisualFlowTest#captureReadOnlyDarkState && adb -s emulator-5554 pull /sdcard/Download/chart_read_only_dark.png "docs/product/2026-08-20-chart-block/visual_evidence/chart_read_only_dark.png" && test -s "docs/product/2026-08-20-chart-block/visual_evidence/chart_read_only_dark.png"` |

## Verification Rules

* All ViewModel/domain/parser behavior is covered by JVM tests; production user-visible flows are covered by instrumented tests.
* The platform boundary and visual owner must run on `emulator-5554`; absent runtime evidence fails loudly.
* Visual screenshots must be captured from the active test window and pulled from `/sdcard/Download`; post-test external screencaps are prohibited.
* `ChartVisualFlowTest` is the only visual-verification owner.

## 📊 Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| **Planning** | Planner | `spec.md`, `design.md`, `sprint-contract.md`, `feature_list.json` compiled | Four vertical slices; US-4 owns platform and visual evidence. |
| **Implementation** | Generator | Pending user approval | |
| **Review 1** | Evaluator | Pending implementation | |
| **Revision 1** | Generator | Pending review | |
| **Final Review** | Evaluator | Pending implementation | |
