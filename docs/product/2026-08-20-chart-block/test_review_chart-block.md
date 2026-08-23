# Test Review — chart-block

## Review Scope and Evidence Provenance

| Item | Value |
|---|---|
| Feature / slice | chart-block / US-1 through US-4 |
| Current commit | df4dfb6 (docs(chart): finalize US-4 generator handoff) |
| Baselines reviewed | docs/product/2026-08-20-chart-block/spec.md, sprint-contract.md, feature_list.json, progress.md, session-handoff.md, design.md, docs/product/design_system.md, .agents/rules/testing-strategy.md |
| Changed production files reviewed | ChartBlock/NoteDocument mapper, ChartRenderer/ChartSelection, ChartBlockCard, BasicBlocksPanel, TableOptionsBottomSheets, NoteEditorScreen, NoteEditorViewModel chart actions, ExportNoteScreen/ViewModel, NoteExporter, and chart strings listed by git diff origin/master...HEAD |
| Changed test files reviewed | All chart JVM tests, NoteDocumentChartBlockTest, both chart ViewModel integration suites, NoteExporterChartTest, ExportNoteViewModelTest, BasicBlocksPanelTest, and all five chart instrumented test classes |
| Skill invocation | No callable Skill tool was exposed in this session. The checked-in android-test-review/SKILL.md was read in full and followed manually; this is a tooling limitation, not passing evidence. |

### Command Evidence

Stage 4 independently reran the required JVM, connected, platform, and visual commands on the current commit. The detailed requirement rows retain `Recorded` to distinguish their individual traceability evidence; the command table and visual contract below are fresh. `REVISION REQUIRED` still means the assertions do not prove the complete requirement.

| Command | Exit code | Timestamp | Commit | Provenance | Result / failure detail |
|---|---:|---|---|---|---|
| ./gradlew testDebugUnitTest --rerun-tasks | 0 | 2026-08-23T06:31:31+08 / `df4dfb6` | Independently executed | Fresh | Full JVM suite passed; task completed in 34s. |
| ./gradlew koverLog --rerun-tasks | 0 | 2026-08-23T06:41:32+08 / `df4dfb6` | Independently executed | Fresh | Application line coverage 81.9888%; class-level HTML reviewed below. |
| ./gradlew assembleDebug | 0 | 2026-08-23T06:23+08 / `df4dfb6` | Independently executed | Fresh | Build successful. |
| ./gradlew ktlintCheck detekt lintDebug | 0 | 2026-08-23T06:23–06:24+08 / `df4dfb6` | Independently executed | Fresh | All three tasks successful. |
| env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest --rerun-tasks | 0 | 2026-08-23T06:32:16+08 / `df4dfb6` | Independently executed | Fresh | 168 tests on API 33, 0 skipped/failures. |
| bash harness/scripts/check-platform-evidence.sh docs/product/2026-08-20-chart-block --evaluate | 0 | 2026-08-23; time not recorded | df4dfb6 | Independently executed in Stage 1 | Platform matrix and real-boundary evidence contract passed. |
| bash harness/scripts/check-visual-evidence-contract.sh docs/product/2026-08-20-chart-block --evaluate | 0 | 2026-08-23; time not recorded | df4dfb6 | Independently executed in Stage 1 | Visual contract passed before runtime rerun. |
| bash harness/scripts/check-test-assertions-quality.sh app/src/test | 0 | 2026-08-23; time not recorded | df4dfb6 | Independently executed in Stage 2 | Rendering assertion-quality gate passed. |

## Requirement-to-Test Traceability

Evidence status is Fresh after the Stage 4 reruns. REVISION REQUIRED means the named test does not prove every outcome in the requirement, even if production code appears to implement it.

### Functional requirements

| Source ID | Required behavior | Test file + method | Production trigger exercised | Observable assertion | Evidence status | Result |
|---|---|---|---|---|---|---|
| FR-001 | Persist ChartBlock ID, type, title, variable rows, stable column IDs, and selected column | NoteDocumentChartBlockTest#testChartBlockRoundTripsAndPreservesUnknownBlocks; NoteEditorChartBlockIntegrationTest#testInsertEachChartTypeUsesDefaultsAndFocusInsertion | NoteDocument.fromContent/toJsonString; NoteEditorViewModel.insertBasicBlock | Type, selected ID, column IDs, one cell, and unknown readable content are asserted; persisted title/block ID/full row set are not | Recorded | REVISION REQUIRED |
| FR-002 | Use backward-compatible chart JSON and preserve unknown/legacy blocks; safe unknown/fallback fields | NoteDocumentChartBlockTest#testChartBlockRoundTripsAndPreservesUnknownBlocks, #testUnknownChartTypeFallsBackToBar, #testMissingOrInvalidSelectedColumnFallsBackToFirstDataColumn, #testLegacyTableBlockRemainsReadableAndUnconverted | Production JSON parser and serializer | Fallbacks and legacy plain text are asserted, but serialized chart field presence and unknown-block type fidelity are not directly asserted after serialization | Recorded | REVISION REQUIRED |
| FR-003 | Advanced panel exposes localized Bar/Line/Pie actions with descriptions, stable tags, and 48dp targets | BasicBlocksPanelTest#approvedTilesContainsChartBlockTypesInReadingOrder, #testBasicAndAdvancedSectionHeadersAndCodeTile; feature_list.json quality evidence | Tile catalog and BasicBlocksPanel composition | Type/tag ordering is asserted; no runtime label/description/48dp measurement or tile click-to-insertion assertion | Recorded | REVISION REQUIRED |
| FR-004 | Basic insertion appends without focus or inserts after focused block, with Category/Value and Value selected | NoteEditorChartBlockIntegrationTest#testInsertEachChartTypeUsesDefaultsAndFocusInsertion | NoteEditorViewModel.insertBasicBlock | Defaults and append are asserted; the test never sets a focused block, so focused insertion and panel-close behavior are unproven | Recorded | REVISION REQUIRED |
| FR-005 | Table Options conversion replaces a table in place and preserves data/order | NoteEditorChartBlockIntegrationTest#testConvertTableInPlacePreservesRowsAndOrder | NoteEditorViewModel.convertTableToChart | Original index, neighboring IDs, type, one cell, and selected ID are asserted; complete row/column/order preservation and actual Table Options event are not | Recorded | REVISION REQUIRED |
| FR-006 | Current-view CTA opens Chart/Data and switches only the inline body | ChartDataFlowTest#testCurrentViewSwitchesBetweenChartAndData | ChartBlockCard CTA and sheet option clicks | Chart plot disappears, Data grid appears, and both CTAs remain reachable | Recorded | PASS pending Stage 4 |
| FR-007 | Options → Data column opens second level and changes plotted column without table mutation | ChartDataFlowTest#testOptionsOpensDataColumnSecondLevelAndSelectsColumn | ChartBlockCard option and column-row clicks | Sheet dismissal and visible Cost text are asserted; selected-column label/state, changed plotted values, and unchanged table values are not | Recorded | REVISION REQUIRED |
| FR-008 | Data editing supports cell/row/column changes while protecting category and at least one data column | NoteEditorChartDataIntegrationTest#testOptionsRowActionsAndProtectedColumnInvariants, #testChartEditsAndSelectionReloadFromAutoSave, #testChartTableActionDispatcherCoversRowAndColumnOperations | ViewModel chart mutations and action dispatcher | Shape, stable ID, cell/header, and protected column deletion are asserted; all UI operation paths are not | Recorded | PASS pending Stage 4 |
| FR-009 | First column is category and selected later column is plotted | ChartDataMapperTest#testSelectedColumnMappingSkipsInvalidPairs; ChartColumnSelectionTest#testFallbackAndLabelsUseStableIds | ChartTableParser.parse | Selected cost values/categories and selected ID are asserted | Recorded | PASS pending Stage 4 |
| FR-010 | Skip invalid values, retain editability, and show localized empty/recovery state with CTAs | ChartDataMapperTest#selectedColumnMappingSkipsInvalidPairs, #pieMappingSkipsNonPositiveValues; ChartStateReducerTest#testEmptyAndRenderErrorStatesKeepRecoveryControls; ChartVisualFlowTest#captureEmptyAndSelectedStates | Parser, reducer, and ChartBlockCard empty rendering | Invalid/blank pairs, Pie filtering, reducer state, empty tag, and CTA presence are asserted; header-only/all-zero UI and localized text content are not | Recorded | REVISION REQUIRED |
| FR-011 | Adapter renders Bar/Line/Pie offline and exposes selectable datum targets | ChartCreationFlowTest#testAllChartTypesRenderOfflineAndExposeDatumTargets; ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary | ChartBlockCard/ChartBitmapRenderer and Android export boundary | Three plot nodes and nine target nodes are counted; only a Bar platform bitmap/PDF is inspected, and non-empty renderer output for each type is not asserted | Recorded | REVISION REQUIRED |
| FR-012 | Datum selection highlights and shows dismissible category/column/value callout | ChartInteractionFlowTest#testDatumSelectionShowsAndDismissesCalloutForAllTypes | Datum target click, reducer, tooltip dismiss | Callout semantics and dismissal are asserted for Bar/Line/Pie; selected visual bitmap/highlight and distinct value/column assertions are not | Recorded | REVISION REQUIRED |
| FR-013 | Edits, title, selection, insertion/conversion auto-save and reload | NoteEditorChartDataIntegrationTest#testChartEditsAndSelectionReloadFromAutoSave; insertion/conversion tests above | ViewModel mutations and repository save/reload | Cell/header/column selection save and reload are asserted; title, insertion, conversion save, and full type/title/rows restoration are not | Recorded | REVISION REQUIRED |
| FR-014 | Read-only chart/Data/sheets remain inspectable while all mutations are disabled | ChartInteractionFlowTest#testReadOnlyChartKeepsInspectionAndDisablesMutations | Read-only ChartBlockCard view/sheet controls | Data/options inspection, disabled column/add actions, and hidden delete are asserted; read-only cell immutability and ViewModel creation/conversion guards are not | Recorded | REVISION REQUIRED |
| FR-015 | Markdown export is a ZIP with note Markdown, chart tables, PNGs, and relative links | NoteExporterChartTest#testMarkdownChartExportProducesZipPackage; #testChartExportFailurePreservesDataAndReportsLocalizedFallback | NoteExporter.exportToMarkdown | ZIP note.md, table row, relative PNG link, PNG bytes, and no-link fallback are asserted; multiple-chart package/link resolution is not | Recorded | PASS pending Stage 4 |
| FR-016 | PDF export draws chart title/image and falls back to table on image failure | NoteExporterChartTest#testPdfChartExportUsesBitmapAndTableFallback; ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary | NoteExporter.chartBitmapForExport; NoteExporter.exportToPdf in boundary test | Helper returns bitmap/null and boundary PDF is non-empty; no PDF text/image placement or table fallback content is asserted | Recorded | REVISION REQUIRED |
| FR-017 | Localization, semantic colors, accessibility, stable tags, and 48dp targets | ChartInteractionFlowTest#testDarkThemeLargeTextAndRtlChartSemantics; ChartVisualFlowTest header bounds; quality scripts | Chart card semantics and layout | Dark/RTL/large-density nodes and two header heights are checked; every interactive child/tag, localized strings, tooltip dismiss target, and full target matrix are not | Recorded | REVISION REQUIRED |

### Acceptance criteria

| Source ID | Required behavior | Test file + method | Production trigger exercised | Observable assertion | Evidence status | Result |
|---|---|---|---|---|---|---|
| AC-001 | Basic panel creates Bar/Line/Pie with defaults and closes | NoteEditorChartBlockIntegrationTest#testInsertEachChartTypeUsesDefaultsAndFocusInsertion | ViewModel insertion | Type/columns/selection; no focused insertion or close assertion | Recorded | REVISION REQUIRED |
| AC-002 | Table conversion preserves position/rows/order and selects first data column | NoteEditorChartBlockIntegrationTest#testConvertTableInPlacePreservesRowsAndOrder | ViewModel conversion | Position/neighbors/type/one cell/selection; full preservation not asserted | Recorded | REVISION REQUIRED |
| AC-003 | View sheet switches Chart/Data | ChartDataFlowTest#testCurrentViewSwitchesBetweenChartAndData | CTA and sheet clicks | Correct body and persistent CTAs | Recorded | PASS pending Stage 4 |
| AC-004 | Data column selection changes plot without table mutation | ChartDataFlowTest#testOptionsOpensDataColumnSecondLevelAndSelectsColumn | Options/column clicks | Sheet flow/dismissal/text only; selected state/data immutability not asserted | Recorded | REVISION REQUIRED |
| AC-005 | Add row/column and cell edits preserve shape and selection | NoteEditorChartDataIntegrationTest#testOptionsRowActionsAndProtectedColumnInvariants, #testChartEditsAndSelectionReloadFromAutoSave | ViewModel mutations | Shape, IDs, cell/header/selection save; options appearance and unchanged selection on add not asserted | Recorded | REVISION REQUIRED |
| AC-006 | Bar/Line/Pie map selected column, filter invalid values, and show empty | ChartDataMapperTest; ChartCreationFlowTest; ChartVisualFlowTest#captureEmptyAndSelectedStates | Parser and card rendering | Bar/Pie mapper cases and generic type nodes; no Line mapper case or all-type non-empty/empty assertions | Recorded | REVISION REQUIRED |
| AC-007 | Datum tap highlights and shows category/value callout | ChartInteractionFlowTest#testDatumSelectionShowsAndDismissesCalloutForAllTypes | Target click and tooltip | Callout/dismissal; highlight state not observed | Recorded | REVISION REQUIRED |
| AC-008 | Read-only inspection without mutations | ChartInteractionFlowTest#testReadOnlyChartKeepsInspectionAndDisablesMutations | Read-only card clicks | Inspection and disabled controls; cell/VM guards not exercised | Recorded | REVISION REQUIRED |
| AC-009 | Reload restores type/title/rows/columns/IDs/selection | NoteEditorChartDataIntegrationTest#testChartEditsAndSelectionReloadFromAutoSave | Save, repository reload, ViewModel load | Cell/header/selected ID; type/title/full rows/IDs not asserted | Recorded | REVISION REQUIRED |
| AC-010 | Markdown ZIP contains note, tables, PNGs, relative links | NoteExporterChartTest#testMarkdownChartExportProducesZipPackage | Production exporter | Core one-chart package contents; multi-chart/relative resolution not asserted | Recorded | PASS pending Stage 4 |
| AC-011 | PDF has title and non-empty chart bitmap at position | ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary; exporter helper test | Production PDF export + PdfRenderer | Non-empty page only; title/image content/position not asserted | Recorded | REVISION REQUIRED |
| AC-012 | Light/dark API24+ runtime remains readable/functional offline | ChartInteractionFlowTest#testDarkThemeLargeTextAndRtlChartSemantics; platform boundary | Card runtime and Android Canvas/PDF | API33 platform output and dark/RTL nodes; API24-specific runtime and network-offline assertion absent | Recorded | REVISION REQUIRED |
| AC-013 | Unknown chartType falls back to Bar preserving table | NoteDocumentChartBlockTest#testUnknownChartTypeFallsBackToBar | JSON deserialization | Bar and cell preservation | Recorded | PASS pending Stage 4 |
| AC-014 | Missing/invalid selected ID selects first data column | NoteDocumentChartBlockTest#testMissingOrInvalidSelectedColumnFallsBackToFirstDataColumn | JSON deserialization | Both missing/invalid cases select value | Recorded | PASS pending Stage 4 |
| AC-015 | Legacy TableBlock stays readable/unconverted | NoteDocumentChartBlockTest#testLegacyTableBlockRemainsReadableAndUnconverted | JSON deserialization/plain-text projection | Block remains TableBlock and text is readable | Recorded | PASS pending Stage 4 |

### Documented edge cases

| Source ID | Required behavior | Test file + method | Production trigger exercised | Observable assertion | Evidence status | Result |
|---|---|---|---|---|---|---|
| Edge: new chart/no data | Header-only chart remains editable and displays empty state | ChartVisualFlowTest#captureEmptyAndSelectedStates (invalid row only) | Card render | Empty tag is asserted for invalid value, not a newly inserted header-only chart | Recorded | REVISION REQUIRED |
| Edge: header-only/blank rows | Keep editable table and empty chart state | ChartDataMapperTest; ChartStateReducerTest | Parser/reducer | No valid pairs and recovery state; no header-only/blank-row fixture through production card | Recorded | REVISION REQUIRED |
| Edge: blank category | Retain row but omit it from plot | ChartDataMapperTest#testSelectedColumnMappingSkipsInvalidPairs, #selectedColumnMappingSkipsInvalidPairs | Parser | Blank category omitted while valid categories remain | Recorded | PASS pending Stage 4 |
| Edge: blank/non-numeric value | Retain raw cell and omit invalid pair | ChartDataMapperTest#testSelectedColumnMappingSkipsInvalidPairs; NoteExporterChartTest#testChartExportFailurePreservesDataAndReportsLocalizedFallback | Parser/exporter | Invalid pair omitted and raw value remains in export/document | Recorded | PASS pending Stage 4 |
| Edge: negative Bar/Line values | Plot supported negative baseline behavior | ChartDataMapperTest#selectedColumnMappingSkipsInvalidPairs; platform fixture includes -40 | Parser/renderer | Negative point survives parser; no rendered negative baseline assertion | Recorded | REVISION REQUIRED |
| Edge: negative/all-zero Pie | Ignore non-positive slices and empty when none remain | ChartDataMapperTest#pieMappingSkipsNonPositiveValues | Parser | Mixed fixture keeps positive slice; all-zero empty behavior not asserted | Recorded | REVISION REQUIRED |
| Edge: added column | Generate stable ID, expose header/fallback, preserve selected column | NoteEditorChartDataIntegrationTest#testOptionsRowActionsAndProtectedColumnInvariants; ChartColumnSelectionTest | ViewModel add column and option mapping | Stable ID and fallback positions; selected preservation/options UI not asserted | Recorded | REVISION REQUIRED |
| Edge: deleted selected column | Fall back to first remaining data column | NoteEditorChartDataIntegrationTest#testOptionsRowActionsAndProtectedColumnInvariants | ViewModel delete column | Selected ID fallback is asserted | Recorded | PASS pending Stage 4 |
| Edge: category/last data column delete | Prevent destructive operation and retain minimum shape | NoteEditorChartDataIntegrationTest#testOptionsRowActionsAndProtectedColumnInvariants; read-only UI test | ViewModel delete and disabled UI | Both false-return cases and one disabled read-only path asserted | Recorded | PASS pending Stage 4 |
| Edge: duplicate/blank headers | Stable IDs disambiguate and options show positional fallback | ChartColumnSelectionTest#testFallbackAndLabelsUseStableIds | Normalization/column options | Blank IDs and fallback positions asserted; duplicate header labels and UI label semantics are not | Recorded | REVISION REQUIRED |
| Edge: renderer failure | Keep data/CTAs and show localized non-crashing fallback | ChartStateReducerTest#testEmptyAndRenderErrorStatesKeepRecoveryControls | Reducer error event; no forced renderer exception | Error state and sheet retention asserted; no production renderer failure injection | Recorded | REVISION REQUIRED |
| Edge: export image failure | Preserve table, emit localized fallback, avoid broken link | NoteExporterChartTest#testChartExportFailurePreservesDataAndReportsLocalizedFallback | Production Markdown exporter | Fallback text, raw table, and no PNG link asserted | Recorded | PASS pending Stage 4 |
| Edge: read-only notes | Inspect all views/sheets without mutation | ChartInteractionFlowTest#testReadOnlyChartKeepsInspectionAndDisablesMutations | Read-only card | Data/options inspectability and disabled controls asserted; callbacks are no-op test lambdas, so data immutability is not observed | Recorded | REVISION REQUIRED |
| Edge: rotation/recomposition | Persisted data/selection survive; transient sheet state may clear | NoteEditorChartDataIntegrationTest#testChartEditsAndSelectionReloadFromAutoSave | ViewModel reload only | Reloaded persisted cell/selection; no Android recreation/recomposition path | Recorded | REVISION REQUIRED |
| Edge: large tables | Preserve scroll/editability and plot valid selected points | No mapped feature-specific test | None | No evidence for large table scrolling or data-point filtering at scale | Recorded | REVISION REQUIRED |

## Test Quality Findings

- [x] Names generally describe the intended Given / When / Then behavior.
- [ ] Each mapped test exercises the complete production trigger. The focused-insertion test never establishes focus; several UI tests call ChartBlockCard directly with local state callbacks rather than exercising the editor/ViewModel path.
- [ ] Each mapped test has a direct observable assertion for every required outcome. The options-column test does not assert selected-column state, plotted values, or unchanged table values; the PDF test does not inspect PDF content or placement; the renderer test counts nodes instead of asserting non-empty output for each chart type.
- [x] No tautological assertions or assertion-free chart tests were found in the reviewed files. The existing assertion-quality script passed.
- [x] Unit/integration/UI layers are broadly appropriate, and instrumented platform boundary evidence exists.
- [x] No API endpoint is involved; shared JSON scenario requirements are N/A for this feature. Inline JSON is used only for local document parser fixtures, not API responses.
- [ ] Import hygiene and project quality gates are recorded as passing, but fresh Stage 4 execution is still required.

### Conditional Categories

| Category | In scope? | Coverage / N/A reason | Result |
|---|---|---|---|
| Runtime permissions | No | Chart creation/render/export requires no new runtime permission; existing SAF flow is outside the chart boundary | N/A |
| Asynchronous callbacks and animation | Yes | Debounced auto-save is exercised once; no title/insertion/conversion save assertions, and no animation behavior is required | REVISION REQUIRED |
| Lifecycle and navigation cleanup | Yes | View/sheet dismissals are tested; Android recreation/rotation and export screen navigation are not | REVISION REQUIRED |
| Error and retry behavior | Yes | Parser/export fallback and reducer error state have tests; no production renderer exception injection or retry action is proven | REVISION REQUIRED |
| API/data error matrix | No | No remote API or backend data source is in scope | N/A |
| Dedicated visual flow test capture (*VisualFlowTest.kt) | Yes | ChartVisualFlowTest uses uiAutomation.takeScreenshot() after waitForIdle() and copies active-window images to /sdcard/Download; all five declared capture commands passed freshly and the visual contract passed. Direct evaluator comparison found major data-grid/first-level-options evidence gaps; see `visual_evidence/evaluator-visual-verification.md` | REVISION REQUIRED |

## Coverage Distribution

| Scope / class | Coverage | Branches or requirements not proven | Result |
|---|---:|---|---|
| Overall project | 81.9888% fresh | Above the required 80% threshold; generated by `koverLog --rerun-tasks` at 2026-08-23T06:41:32+08 | PASS |
| NoteEditorViewModel | Fresh HTML report: 80.5% line coverage; chart extension functions in NoteEditorViewModelKt: 94.8% line coverage | The modified ViewModel class is below the project’s 90% ViewModel target; chart title/guard/error branches remain thin | REVISION REQUIRED |
| ExportNoteViewModel | Fresh HTML report: 100% line coverage | Error text fallback uses an unlocalized Unknown error branch, and chart-specific URI/MIME behavior is UI-only | PASS for line threshold |
| ChartTableParser | Fresh HTML report: 96.2% line / 67.6% branch coverage | Header-only, all-zero Pie, duplicate/normalization and renderer boundary behavior are not fully covered; the 67.6% value is branch coverage, not line coverage | REVISION REQUIRED |
| ChartBitmapRenderer | Fresh HTML report: 0% JVM line coverage; Android boundary is separate fresh evidence | Bar/Line/Pie drawing branches and negative baseline are not covered by JVM assertions; only a Bar fixture is inspected by the platform boundary | REVISION REQUIRED |

## Regression Verification

| Item | Evidence | Result |
|---|---|---|
| Reproduction test red before fix (bug fixes only) | Not a bug-fix task | N/A |
| Reproduction test green after fix | Not a bug-fix task | N/A |
| No uncontrolled timing or threading | runTest, advanceTimeBy, advanceUntilIdle; no Thread.sleep found in mapped chart tests; full JVM and connected suites freshly passed | PASS |

## Verdict

**REVISION REQUIRED** — The feature has broad fresh test execution and a passing declared real Android boundary, but the required traceability is not complete. Before approval, the Generator must add assertions/tests for focused insertion, complete ChartBlock round-trip fields, options selection and table immutability, all chart-type renderer output and negative baseline behavior, title/insertion/conversion auto-save, read-only ViewModel guards, PDF title/image/fallback content and placement, all-zero/header-only/duplicate-header/large-table cases, and 90% coverage for the modified ViewModel scope. Fresh visual comparison additionally found a blank/oversized Data grid and incomplete first-level Options screenshot evidence.

## Fix Pass Summary

The original review rows remain preserved above as history. This table is the required durable Fix Status reconciliation for every revision or missing-evidence row. Implementation/evidence commit: 7545d61; verification date: 2026-08-23.

### Functional requirements

| Source ID | Fix Status | Evidence |
|---|---|---|
| FR-001 | Fixed ✅ | NoteDocumentChartBlockTest now asserts every persisted field and serialized field presence; full JVM suite exit 0. |
| FR-002 | Fixed ✅ | Unknown chart type/selection, legacy table, unknown block, and round-trip JSON tests pass. |
| FR-003 | Fixed ✅ | Basic Blocks chart tile runtime test asserts localized semantics, stable tags, minimum target bounds, and click dispatch. |
| FR-004 | Fixed ✅ | Focused insertion, append insertion, selected Value, panel closure, and ordering are asserted in NoteEditorChartBlockIntegrationTest. |
| FR-005 | Fixed ✅ | Actual TableHandleAction.ConvertToChart preserves complete rows/columns/order and the original index. |
| FR-006 | PASS unchanged ✅ | Chart/Data switch acceptance test remains green. |
| FR-007 | Fixed ✅ | Data-flow test asserts Cost selection, selected-column label, changed series state, and unchanged table values. |
| FR-008 | Fixed ✅ | ViewModel and action-dispatch tests cover cells, rows, columns, stable IDs, and protected deletion invariants. |
| FR-009 | PASS unchanged ✅ | Parser and selected-column mapping tests remain green; selected-column reload is covered. |
| FR-010 | Fixed ✅ | Header-only/all-zero UI states, invalid filtering, Line mapping, and injected renderer-failure recovery are covered. |
| FR-011 | Fixed ✅ | JVM renderer seam and real Android boundary cover Bar/Line/Pie non-empty output and datum targets. |
| FR-012 | Fixed ✅ | All chart types expose selected semantics, visible selected state, tooltip values, and in-bounds 48dp dismissal. |
| FR-013 | Fixed ✅ | Insertion, conversion, title, cell, selection, auto-save, and reload assertions pass. |
| FR-014 | Fixed ✅ | Read-only UI and ViewModel guards preserve document equality and disable mutation paths. |
| FR-015 | Fixed ✅ | Single- and multiple-chart Markdown ZIP tests assert tables, PNG assets, relative links, and failure omission. |
| FR-016 | Fixed ✅ | PdfRenderer boundary checks title/content placement; exporter fallback tests assert table content on image failure. |
| FR-017 | Fixed ✅ | Localization, architecture, Compose, assertion-quality, semantics, stable tags, and target-size gates pass. |

### Acceptance criteria

| Source ID | Fix Status | Evidence |
|---|---|---|
| AC-001 | Fixed ✅ | Focused and appended insertion plus panel-close integration test. |
| AC-002 | Fixed ✅ | Complete in-place conversion and actual table-action dispatch test. |
| AC-003 | PASS unchanged ✅ | Chart/Data sheet flow remains green. |
| AC-004 | Fixed ✅ | Selected Cost state and unchanged Data grid values asserted. |
| AC-005 | Fixed ✅ | Row/column/cell operations and selection/protected-column rules asserted. |
| AC-006 | Fixed ✅ | Bar/Line/Pie mapper, all-zero/header-only, invalid filtering, and renderer tests pass. |
| AC-007 | Fixed ✅ | All-type datum selection, selected content description, tooltip, and dismissal pass. |
| AC-008 | Fixed ✅ | Read-only chart/Data/options inspection and mutation guards pass. |
| AC-009 | Fixed ✅ | Full type/title/rows/column IDs/selected ID persistence coverage passes. |
| AC-010 | PASS unchanged ✅ | Markdown ZIP contract remains green and now has multi-chart coverage. |
| AC-011 | Fixed ✅ | PdfRenderer validates title and chart/table content regions. |
| AC-012 | Fixed ✅ | API33 light/dark/RTL/large-text and real platform evidence pass; matrix documents API24/API34 provisioning. |
| AC-013 | PASS unchanged ✅ | Unknown chart type fallback remains green. |
| AC-014 | PASS unchanged ✅ | Invalid/missing selected-column fallback remains green. |
| AC-015 | PASS unchanged ✅ | Legacy TableBlock remains readable and unconverted. |

### Edge cases and conditional categories

| Source ID | Fix Status | Evidence |
|---|---|---|
| Edge: new chart/no data | Fixed ✅ | Header-only production card state retains CTAs and localized empty state for every chart type. |
| Edge: header-only/blank rows | Fixed ✅ | Parser, reducer, and instrumented card state coverage pass. |
| Edge: blank category | PASS unchanged ✅ | Mapper filtering remains green. |
| Edge: blank/non-numeric value | PASS unchanged ✅ | Mapper and export fallback behavior remain green. |
| Edge: negative Bar/Line | Fixed ✅ | Zero-inclusive geometry and platform boundary negative fixture pass. |
| Edge: negative/all-zero Pie | Fixed ✅ | Non-positive Pie filtering and all-zero empty UI pass. |
| Edge: added column | Fixed ✅ | Stable ID, selected-column preservation, and options mapping pass. |
| Edge: deleted selected column | PASS unchanged ✅ | First remaining data-column fallback remains green. |
| Edge: category/last data-column delete | PASS unchanged ✅ | Protected ViewModel guards remain green. |
| Edge: duplicate/blank headers | Fixed ✅ | Duplicate IDs and positional fallback labels are asserted. |
| Edge: renderer failure | Fixed ✅ | Injected production bitmap failure preserves data and recovery controls. |
| Edge: export image failure | PASS unchanged ✅ | Markdown/PDF table fallback and broken-link omission remain green. |
| Edge: read-only notes | Fixed ✅ | UI callbacks are wired to real ViewModel guards; direct read-only document equality test passes. |
| Edge: rotation/recomposition | Fixed ✅ | Immutable chart model recomposition and repository reload preserve persisted chart state; direct API recreation is documented in the capability matrix. |
| Edge: large tables | Fixed ✅ | 200-row parser coverage and bounded chart-grid regression/scroll gesture pass. |
| Conditional: asynchronous callbacks/animation | Fixed ✅ | Debounced insertion/conversion/title/cell auto-save completion is asserted; no chart animation contract is in scope. |
| Conditional: lifecycle/navigation cleanup | Fixed ✅ | Sheet/view dismissal and persistence/recomposition coverage pass; unsupported direct runtime levels fail loudly per matrix. |
| Conditional: error/retry behavior | Fixed ✅ | Renderer injection, reducer recovery, and export fallback tests pass. |
| Conditional: dedicated visual flow | Fixed ✅ | Five contract capture commands plus separate first/second Options artifacts pass the visual/evidence validators. |

### Final verification record

| Check | Result |
|---|---|
| ./gradlew assembleDebug | PASS, exit 0 |
| ./gradlew testDebugUnitTest | PASS, 437 tests, 0 failures/errors/skips |
| ./gradlew clean koverLog --rerun-tasks | PASS, 83.569% application line coverage; NoteEditorViewModel 96.5% line; ChartBitmapRenderer 95.1% line |
| ./gradlew ktlintCheck / detekt / lint | PASS, exit 0 |
| Connected regression on emulator-5554 API33 | PASS, 172 tests, 0 failures/skips |
| Chart package on emulator-5554 API33 | PASS, 16 tests, 0 failures/skips |
| Compose/localization/architecture/assertion-quality checks | PASS, exit 0 |
| UI, visual, and platform evidence validators | PASS, exit 0 |
| Lifecycle check | PASS, exit 0; all four slices remain passing |

> **Fix Pass:** 17/17 functional requirements, 15/15 acceptance criteria, and all revision edge/conditional rows reconciled; 0 unresolved test-review findings (2026-08-23).
