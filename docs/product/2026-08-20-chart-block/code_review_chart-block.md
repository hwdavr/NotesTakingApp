# Code Review — chart-block

## Review Summary

**Feature / Bug**: Chart block creation, editing, rendering, interaction, persistence, and export  
**Reviewer**: Evaluator agent  
**Date**: 2026-08-23

**Recommendation**: ❌ Do not merge — revision is required. The feature has a broad implementation and green recorded/runtime evidence, but the negative-value renderer, callback defaults, test-tag contract, touch-target contract, and verification gaps prevent approval.

## Review Scope and Evidence Provenance

| Item | Value |
|---|---|
| Current commit | `df4dfb6b4a73c4385456c2ee2d88f1615976181c` (`docs(chart): finalize US-4 generator handoff`) |
| Merge base / prior reviewed commit | `96a3775e69c1344ea706902e56d07e6a613c01b7` |
| Baselines reviewed | `spec.md`, `sprint-contract.md`, `feature_list.json`, `progress.md`, `session-handoff.md`, all slice summaries, `design.md`, `docs/product/design_system.md`, architecture/testing/Compose/localization/implementation rules, and `test_review_chart-block.md` |
| Changed production files reviewed | 23 chart/editor/export Kotlin files plus `strings.xml`, identified by `git diff --name-status origin/master...HEAD` |
| Changed tests reviewed | All chart JVM, ViewModel/integration, exporter, platform-boundary, and visual-flow tests in the feature diff |
| Independently executed checks | Lifecycle, platform-evidence contract, visual-evidence contract, assertion-quality check, assemble, ktlint, Detekt, Android Lint, Compose/localization/architecture rule scripts, declared-assets check, suppression audit, and `git diff --check` |
| Recorded / up-to-date / skipped checks | Generator's JVM, Kover, and connected results were recorded in the handoff; their independent Stage 4 rerun is required. No API or shared-contract change is in scope. |
| Skill invocation | No callable Skill tool was exposed in this session. The checked-in `android-code-review`, `code-review-and-quality`, and `android-code-quality-checks` skill contracts were read in full and followed manually as a documented tooling fallback. |

## Requirement-to-Production Traceability

| Source ID | Required behavior | Production entry point | Completion / cleanup path | Test evidence | Result |
|---|---|---|---|---|---|
| FR-001 | Persist chart ID, type, title, variable rows, stable column IDs, selected data column | `EditorBlock.ChartBlock`, `NoteDocument`, `updateChart*` | JSON serialization/deserialization; `replaceChartBlock` schedules save | `NoteDocumentChartBlockTest`; ViewModel integration | REVISION REQUIRED — full field round-trip is not asserted |
| FR-002 | Backward-compatible chart JSON, unknown type/selection fallback, legacy blocks preserved | `EditorBlock.toJson`, `JSONObject.toEditorBlock`, normalization helpers | `NoteDocument.fromContent` fallback to plain text | `NoteDocumentChartBlockTest` | PASS with a test-evidence gap for exact serialized fields |
| FR-003 | Localized Bar/Line/Pie Basic Blocks actions, tags, 48 dp targets | `BasicBlocksPanel`, `basicBlockTiles` | Panel selection dispatches `onTileSelected` | `BasicBlocksPanelTest`; recorded UI evidence | REVISION REQUIRED — runtime label/description/size and click path are not directly asserted |
| FR-004 | Insert a default chart after focus or at document end; close panel; select Value | `NoteEditorViewModel.insertBasicBlock` | State sets `showBasicBlocksPanel = false`, focus, selection, and auto-save | `NoteEditorChartBlockIntegrationTest` | REVISION REQUIRED — focused insertion and panel-close assertion are missing |
| FR-005 | Convert a table in place while preserving data/order/position | `convertTableToChart`, `TableOptionsBottomSheets` | Replaces same list index, clears table focus, schedules auto-save | ViewModel integration and recorded UI tests | REVISION REQUIRED — complete rows/columns and actual Table Options event are not asserted |
| FR-006 | View CTA opens Chart/Data sheet and switches inline body | `ChartBlockCard`, `ChartViewSheet` | Reducer dismisses sheet and persists view transiently in card state | `ChartDataFlowTest`, `ChartVisualFlowTest` | PASS for the tested single-card flow |
| FR-007 | Options → Data column changes selected plotted series without table mutation | `ChartOptionsSheet`, `ChartDataColumnSheet`, `updateChart` | Selection callback dismisses sheet; ViewModel schedules auto-save | `ChartDataFlowTest` | REVISION REQUIRED — selected ID, plotted values, and unchanged table data are not asserted |
| FR-008 | Cell/row/column editing with category/last-data-column invariants | `updateChartCell`, `NoteEditorViewModelChartActions`, `ChartDataTable` | Mutations normalize shape, guard editability, and schedule auto-save | chart data integration tests | PASS for core ViewModel guards; UI paths and all invariants need more direct assertions |
| FR-009 | First column is category; selected later column supplies values | `ChartTableParser.parse` | Parser returns `ChartData` points and selected-column metadata | `ChartDataMapperTest`, `ChartColumnSelectionTest` | PASS for parser mapping |
| FR-010 | Skip invalid data, show localized empty/recovery state, retain CTAs | parser filter plus `ChartSelectionReducer`/`ChartBlockCard` | `RenderFailed` retains recovery state; raw table is not discarded | mapper/reducer/visual tests | REVISION REQUIRED — header-only/all-zero production UI and renderer-failure injection are not proven |
| FR-011 | Offline Bar/Line/Pie rendering and selectable data points | `ChartBitmapRenderer`, `ChartBlockPlot`, `ChartDatumTargets` | Bitmap result drives `Rendered`/`RenderFailed`; selection reducer bounds count | creation/platform tests | REVISION REQUIRED — only Bar bitmap is inspected; targets are not geometry-aware |
| FR-012 | Datum selection highlights and shows dismissible category/value tooltip | `ChartSelectionReducer`, `ChartTooltip` | `TooltipDismissed` clears selected index | `ChartInteractionFlowTest` | REVISION REQUIRED — tooltip is asserted, rendered highlight is not |
| FR-013 | Edits, title, selection, insertion/conversion survive save and reload | ViewModel chart actions and `saveInternally` | Debounced job, `cancelAndJoinAll`, repository save/reload | chart ViewModel integration | REVISION REQUIRED — title/insertion/conversion and full persisted shape are not covered |
| FR-014 | Read-only chart/data/sheets inspectable; all mutations disabled | `isEditable` through `ChartBlockCard`, chart actions, editor screen | UI hides delete and disables action rows; ViewModel guards return without mutation | read-only instrumented flow | REVISION REQUIRED — callback-level cell immutability and VM creation/conversion guards are not exercised |
| FR-015 | Markdown ZIP contains note, chart tables, PNG assets, relative links | `NoteExporter.exportToMarkdown`, `ChartBlock.toMarkdown` | `ZipOutputStream` closes output; failed image omits broken link | `NoteExporterChartTest` | PASS for the one-chart success/failure paths; multi-chart path needs coverage |
| FR-016 | PDF places chart title/image and falls back to table when image unavailable | `NoteExporter.renderChartBlock`, `chartBitmapForExport` | `PdfDocument` finishes pages and closes output; fallback renders message/table | exporter helper and platform boundary | REVISION REQUIRED — PDF content, placement, and fallback content are not inspected |
| FR-017 | Localized strings, semantic colors, accessibility, stable tags, 48 dp targets | chart Composables, string resources, `LocalAppColors` | state-driven callbacks and sheet dismissals | rule scripts, visual flow, dark/RTL test | REVISION REQUIRED — generic tags, nested radio control tag, 40 dp tooltip control, and 48 dp matrix are unresolved |

### Acceptance criteria

| Source ID | Required behavior | Production path | Evidence | Result |
|---|---|---|---|---|
| AC-001 | Insert each chart type after focus/appended with defaults and close panel | `insertBasicBlock` | Defaults/appended path tested; focus and close not asserted | REVISION REQUIRED |
| AC-002 | Convert table in place preserving complete data/order | `convertTableToChart` | Position/neighbors tested; complete shape not asserted | REVISION REQUIRED |
| AC-003 | Switch Chart/Data view | `ChartViewSheet` | Instrumented flow and visual capture | PASS |
| AC-004 | Select data column without table mutation | `ChartDataColumnSheet`/`updateChart` | Sheet flow only; state/data invariants absent | REVISION REQUIRED |
| AC-005 | Edit cells/rows/columns and preserve selection/invariants | chart VM actions/table | Core VM integration passes; UI and selected-column-on-add assertions absent | REVISION REQUIRED |
| AC-006 | All chart types use selected column, filter invalid values, show empty | parser/renderer/card | Parser and node-count tests; all-type non-empty/empty bitmap assertions absent | REVISION REQUIRED |
| AC-007 | Datum tap identifies selection and tooltip | target/reducer/tooltip | Tooltip/dismissal tested; visual highlight absent | REVISION REQUIRED |
| AC-008 | Read-only inspection without mutation | `isEditable` gates | UI inspection/disabled controls tested; VM and cell immutability absent | REVISION REQUIRED |
| AC-009 | Reload restores type/title/rows/columns/IDs/selection | JSON + repository save/load | Partial cell/header/selection reload only | REVISION REQUIRED |
| AC-010 | Markdown ZIP has note/table/PNG/relative links | exporter | One-chart ZIP success and fallback tested | PASS |
| AC-011 | PDF has title, image, position | `renderChartBlock` | Non-empty PDF page only | REVISION REQUIRED |
| AC-012 | Light/dark supported API24+ rendering and semantics | Compose/platform adapter | API33 recorded runtime and dark/RTL nodes; API24-specific evidence absent | REVISION REQUIRED |
| AC-013 | Unknown chart type falls back to Bar | `ChartType.fromStorageValue` | Unit test | PASS |
| AC-014 | Missing/invalid selection falls back to first data column | selection normalization | Unit test | PASS |
| AC-015 | Legacy table remains readable/unconverted | JSON parser | Unit test | PASS |

### Documented edge cases

| Edge case | Production handling | Evidence | Result |
|---|---|---|---|
| New chart/no data | Default header-only table and empty chart state | invalid-row visual fixture only | REVISION REQUIRED |
| Header-only/blank rows | Parser returns no points while table remains available | parser/reducer tests, no header-only card flow | REVISION REQUIRED |
| Blank category | Omit row from plot | parser test | PASS |
| Blank/non-numeric value | Omit point and preserve raw cell | parser/export test | PASS |
| Negative Bar/Line | Must use zero baseline | renderer currently scales from `chart.bottom` using positive-only max | REVISION REQUIRED |
| Negative/all-zero Pie | Filter non-positive slices; empty if none | mixed Pie parser test only | REVISION REQUIRED |
| Added column | New stable ID, label, selected column unchanged | ViewModel/mapper coverage partial | REVISION REQUIRED |
| Deleted selected column | Select first remaining data column | ViewModel test | PASS |
| Category/last data-column delete | Prevent destructive operation | ViewModel tests; UI evidence partial | PASS for VM guard |
| Duplicate/blank headers | Stable IDs and positional fallback labels | ID/fallback mapper test; duplicate header UI absent | REVISION REQUIRED |
| Renderer failure | Preserve data/CTAs and show localized error | reducer test; no production exception injection | REVISION REQUIRED |
| Export image failure | Keep table and omit broken image link | Markdown fallback test | PASS for Markdown; PDF content absent |
| Read-only | Inspect views/sheets and disable mutations | instrumented UI path; callbacks are no-op lambdas | REVISION REQUIRED |
| Rotation/recomposition | Persist data/selection; transient state may clear | repository reload only | REVISION REQUIRED |
| Large tables | Preserve scrolling and valid points | no feature-specific test | REVISION REQUIRED |

## State Completion and Reachability Audit

| Changed state, callback, job, or listener | Set / entry point | Production completion or cleanup call site | Test-only substitute found? | Result |
|---|---|---|---|---|
| `showBasicBlocksPanel` | `insertBasicBlock` sets false after insertion | Basic Blocks UI owns panel visibility and dispatches insertion | No | PASS |
| Chart view sheet state | `ChartSelectionReducer.SheetOpened(VIEW)` | `SheetDismissed` from choice/dismiss callback | No | PASS |
| Chart options/data-column sheet state | CTA and options callbacks | `SheetDismissed`, back transitions to Options | No | PASS |
| Selected datum / tooltip | `DatumTapped` reducer event | `TooltipDismissed`; selection is cleared by reducer | No | PASS for state path |
| Render state | `LaunchedEffect` dispatches `Rendered` or `RenderFailed` | Reducer derives `CONTENT`, `EMPTY`, or `ERROR` | No | REVISION REQUIRED — renderer runs synchronously during composition and failure is not injected in production tests |
| Debounced chart auto-save | `updateChart*`/chart action replacements call `scheduleAutoSave` | `saveInternally`, `save()` cancels and joins pending jobs | No | PASS for wiring; title/insertion/conversion persistence coverage is incomplete |
| Chart callback defaults | New default parameters in `NoteEditorScreenContent` | No production completion for `{ _, _ -> }`, `{ false }`, or cell no-op | Yes — test callers commonly provide no-op lambdas | REVISION REQUIRED — required callbacks must be required parameters or have real behavior |
| Read-only state | `NoteEditorViewModel.load` sets `isEditable` from access role | VM action guards and UI enabled/hidden states | No | REVISION REQUIRED — direct VM guard coverage is incomplete |

## Build & Test Results

The following commands were independently run against the current commit on 2026-08-23. Stage 4 refreshed the JVM, coverage, connected, platform, and visual rows.

| Check | Exit code | Timestamp / commit | Provenance | Result | Failure detail / scope |
|---|---:|---|---|---|---|
| `assembleDebug` | 0 | 2026-08-23 06:23 +08 / `df4dfb6` | Independently executed | ✅ PASS | Build successful |
| `testDebugUnitTest --rerun-tasks` | 0 | 2026-08-23T06:31:31+08 / `df4dfb6` | Independently executed | ✅ PASS | Full JVM suite passed in 34s |
| `koverLog --rerun-tasks` overall | 0 | 2026-08-23T06:41:32+08 / `df4dfb6` | Independently executed | ✅ 81.9888% ≥ 80% | Fresh Kover output |
| `koverLog` new classes | 0 | 2026-08-23T06:41:32+08 / `df4dfb6` | Independently executed HTML review | ❌ FAIL | `NoteEditorViewModel` 80.5% line (<90%); `ChartTableParser` 96.2% line/67.6% branch; `ChartBitmapRenderer` 0% JVM |
| `connectedDebugAndroidTest --rerun-tasks` | 0 | 2026-08-23T06:32:16+08 / `df4dfb6` | Independently executed | ✅ PASS | 168 tests on API33 emulator, 0 skipped/failures |
| `ktlintCheck` | 0 | 2026-08-23 06:23 +08 / `df4dfb6` | Independently executed | ✅ PASS | Build successful |
| `detekt` | 0 | 2026-08-23 06:23 +08 / `df4dfb6` | Independently executed | ✅ PASS | Build successful |
| `lintDebug` | 0 | 2026-08-23 06:24 +08 / `df4dfb6` | Independently executed | ✅ PASS | Build successful |
| `check-compose-rules.sh` | 1 | 2026-08-23 06:24 +08 / `df4dfb6` | Independently executed | ❌ FAIL | 22 unstable dynamic-tag findings in unchanged `CodeBlockCard.kt`/`MermaidBlockCard.kt`; no such interpolated tag was found in `ChartBlockCard.kt`. Required gate is still non-zero. |
| `check-localization-rules.sh` | 0 | 2026-08-23 06:24 +08 / `df4dfb6` | Independently executed | ✅ PASS | 184 files, zero violations |
| `check-architecture-rules.sh` | 0 | 2026-08-23 06:24 +08 / `df4dfb6` | Independently executed | ✅ PASS | 184 files, zero violations |
| `check-declared-assets.sh docs/product/2026-08-20-chart-block` | 0 | 2026-08-23 / `df4dfb6` | Independently executed | ✅ PASS | No asset references requiring check |
| `check-platform-evidence.sh ... --evaluate` | 0 | 2026-08-23T06:43:43+08 / `df4dfb6` | Independently executed | ✅ PASS | Matrix and real boundary evidence contract passed |
| `check-visual-evidence-contract.sh ... --evaluate` | 0 | 2026-08-23T06:43:43+08 / `df4dfb6` | Independently executed | ✅ PASS contract / ❌ visual review | Contract rows, screenshots, and anchors align; evaluator visual comparison found major findings in `visual_evidence/evaluator-visual-verification.md` |
| Suppression audit | 0 | 2026-08-23 / `df4dfb6` | Independently executed | ✅ PASS | No new `@Suppress`, `tools:ignore`, baselines, or disable directives in the feature diff |
| `git diff --check` | 0 | 2026-08-23 / `df4dfb6` | Independently executed | ✅ PASS | No whitespace errors |

Any non-zero required gate makes the verdict non-approved, including when the source of the failure is pre-existing. The Compose failure is classified as pre-existing/global scope, not as a chart-block line-level finding.

## Compose Rules Enforcement

The feature modifies Compose UI files, so all rows are reviewed. The script passed the hardcoded-color, missing-tag-file, ViewModel-in-content, and repository/use-case checks; the full script still exits 1 because it scans unchanged legacy cards after the committed feature has no working-tree Kotlin changes.

### Section 1 — Composable Responsibilities

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 1.1 Receives `UiState` + callbacks as params | Human source audit | ✅ | `ChartBlockCard` receives block, editability, and callbacks |
| 1.2 Only renders state — no derived computation | Human source audit | ❌ | `ChartBlockCard.kt:104-105` normalizes the block and parses chart data inside the Composable |
| 1.3 Never calls ViewModel directly | Script + source audit | ✅ | No ViewModel call in chart Composables |
| 1.4 No use case/repository calls | Script + source audit | ✅ | No repository/use-case call in chart Composables |
| 1.5 No business logic/data transformation | Human source audit | ❌ | Normalization/filtering/parser work is performed from the card rather than a presentation mapper/state producer |
| 1.6 No hardcoded strings | Localization script | ✅ | User-visible chart copy uses resources; dynamic data is format-argument content |
| 1.7 No hardcoded colors | Compose script + source audit | ✅ | Chart card uses `LocalAppColors`; fixed export/bitmap colors are outside Composables |

### Section 2 — Stateless / Stateful Pattern

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 2.1 Screen + Content pair | Human source audit | ✅ | Existing editor `NoteEditorScreen`/`NoteEditorScreenContent` pair retained |
| 2.2 Only Screen obtains ViewModel/state collection | Script + source audit | ✅ | `hiltViewModel`/collection remain in wrapper |
| 2.3 UI tests target Content | Test source audit | ✅ | Chart tests target `ChartBlockCard`/content-level UI |

### Section 3 — Test Tags

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 3.1 All interactive elements have `testTag` | Script + human audit | ❌ | `ChartSheetChoice` has a nested `RadioButton` without its own tag at `ChartBlockCard.kt:1161`; the 40 dp tooltip dismiss control also needs a contract fix |
| 3.2 Key content containers have tags | Human source audit | ✅ | Card, plot, table, sheets, data grid, empty/error states are tagged |
| 3.3 Tags are descriptive and stable | Design comparison | ❌ | `ChartBlockCard.kt:161` and all chart tags are generic; `design.md:75-86` requires persisted block/column IDs in tags |

### Section 4 — String Resources

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 4.1 User-visible text uses resources | Localization script/source audit | ✅ | Chart UI labels/descriptions are resource-backed |
| 4.2 Resource keys follow screen/component/type naming | Source/resource audit | ✅ | New keys use the `editor_chart_*`/`chart_export_*` convention |

### Section 5 — Colors

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 5.1 No `Color(0x...)` outside `AppColors.kt` | Compose script | ✅ | None |
| 5.2 No named Compose `Color.*` in chart UI | Compose script | ✅ | None in Composables |
| 5.3 Access through `LocalAppColors` | Human audit | ✅ | Chart card obtains semantic tokens |
| 5.4 Semantic token names | Human audit | ✅ | `surface`, `border`, `primary`, `textPrimary`, `divider` are semantic |
| 5.5 New token in both themes | Diff/resource audit | ⏭ | No new theme token added |

### Section 6 — Component Extraction

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 6.1 Reused UI in components | Human audit | ✅ | Chart UI is in editor components |
| 6.2 Complex/stateful components extracted | Human audit | ❌ | `ChartBlockCard.kt` is 1,190 lines and combines card, renderer state, table, handles, sheets, and options; split by visual responsibility is needed |
| 6.3 One visual responsibility per component | Human audit | ❌ | The same file owns multiple independently testable surfaces and their state transitions |

### Section 7 — State Hoisting

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 7.1 Lowest common ancestor | Human audit | ✅ | ViewModel owns persisted chart state; card owns transient view/selection |
| 7.2 Not hoisted higher than necessary | Human audit | ✅ | No route-level chart state was added |
| 7.3 No `remember {}` inside `*Content` composables | Human audit | ❌ | `ChartBlockCard.kt:104-128` uses `remember` for parsing and synchronous bitmap generation; the parsed/render model should be produced outside the renderer Composable |

### Section 8 — Performance

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 8.1 Lazy lists for large collections | Human audit | 👁️ Human | Chart table uses a regular `Column` and loops all rows; large-table requirement has no runtime test and needs an explicit performance decision |
| 8.2 Stable parameter types | Human audit | 👁️ Human | Immutable model types are used, but large chart recomposition behavior is unmeasured |
| 8.3 Stable IDs in lazy lists | Human audit | ⏭ | No lazy list added in chart card |
| 8.4 Lambdas passed as parameters | Human audit | 👁️ Human | Callbacks are parameterized; inline callbacks are created in the editor bridge and need normal Compose performance review |

### Compose Rule Violations Detail

- **Rule 1.2/1.5** — `ChartBlockCard.kt:104-105`: normalization and chart parsing are invoked from the Composable. Move presentation transformation to a mapper/ViewModel or a non-Composable state producer.
- **Rule 3.1** — `ChartBlockCard.kt:1161`: nested `RadioButton` has no explicit `testTag`.
- **Rule 3.3** — `ChartBlockCard.kt:161` and related tags: tags omit the persisted block/column IDs required by the approved design contract.
- **Rule 6.2/6.3** — `ChartBlockCard.kt`: the 1,190-line file combines too many visual responsibilities and stateful surfaces.
- **Global gate** — unchanged `CodeBlockCard.kt`/`MermaidBlockCard.kt` contain the 22 dynamic tag findings reported by the checker. This is pre-existing scope but still blocks a clean repository gate.

## Localization Rules Enforcement

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| 1.1 `Text()` uses resources | Localization script | ✅ | Zero violations |
| 1.2 Composable labels/placeholders use resources | Localization script | ✅ | Zero violations |
| 1.3 Local UI labels are not raw strings | Localization script | ✅ | Zero violations |
| 2.1 Strings in `strings.xml` | Human audit | ✅ | New visible chart copy is resource-backed |
| 3.1 Resource-key naming | Human audit | ✅ | `editor_chart_*` keys are consistent |
| 4.1/4.2 Count-dependent text uses plurals | Human audit | ⏭ | No new count-dependent sentence requiring plural resources |
| 5.1/5.2 Runtime values use format arguments | Human audit | ✅ | Chart type/title/count and tooltip values use formatted resources |
| 6.1/6.2 Interactive non-text controls have localized descriptions | Script + human audit | ❌ | `ChartSheetChoice` relies on parent semantics and its nested radio has no explicit description/tag of its own; tooltip target size also violates the approved accessibility contract |

### Localization Rule Violations Detail

- **Rule 6.1/6.2** — `ChartBlockCard.kt:1161`: the nested view/data `RadioButton` is interactive but has no explicit `contentDescription`/`testTag`; make the radio and row semantics unambiguous and non-duplicative.

## Architecture Rules Enforcement

| Rule | How Checked | Status | Violations |
|---|---|---|---|
| UI 1.1/1.5/1.6 no repository/DAO/data-source calls/imports | Architecture script + source audit | ✅ | None |
| UI 1.2 no business rules | Human audit | ❌ | Chart normalization/parser invocation remains in `ChartBlockCard` |
| UI 1.3/1.4 no API parsing/DTO mapping | Architecture script + source audit | ✅ | No API/DTO path |
| VM 2.1 consolidated state flow | Human audit | ✅ | `NoteEditorUiState` remains the single editor state flow |
| VM 2.2/2.3 coordinates/maps | Source audit | ✅ | ViewModel routes chart changes and maps through chart model helpers |
| VM 2.4 loading/success/error | Source audit | ✅ | Existing editor load/error state is retained; chart render state handles content/empty/error |
| VM 2.5 one-off events | Source audit | ⏭ | No new one-off event channel required |
| VM 2.8 no heavy business logic | Human audit | ✅ | Chart mutations are small immutable state transformations; renderer is not in VM |
| Domain 3.1–3.5 no Android/UI/data imports | Architecture script | ⏭ | No domain files changed |
| Data 4.1–4.3 DTO/UI/navigation boundaries | Architecture script + source audit | ⏭ | No data-layer/API change |
| State 5.1/5.3 consolidated state/no scattered flags | Human audit | ✅ | Chart transient state is grouped in reducer state; editor has existing flags |
| Mapping 6.1–6.4 layer boundaries | Architecture script + source audit | ✅ | Chart model mapping stays under editor UI mapper; no DTO/API shortcut |
| DI 7.1–7.4 | Source audit | ⏭ | No new dependency injection binding |
| Forbidden 8.1–8.4 | Architecture script + source audit | ❌ | New chart callback defaults include no-op lambdas and an error callback in production API; tests exist for the existing ViewModel, but direct chart guard coverage is incomplete |
| Package 9.1–9.5 | Architecture script + path audit | ✅ | ViewModel extensions are in `viewmodel/`; chart UI/mappers are in UI packages |

### Architecture Rule Violations Detail

- **UI 1.2 / Compose 1.5** — `ChartBlockCard.kt:104-105`: data normalization and parsing are performed at the UI boundary.
- **Forbidden implementation pattern** — `NoteEditorScreen.kt:305-332`: new chart callback parameters default to `{ _, _ -> }`, `{ false }`, `{ _, _, _, _ -> }`, and an error callback. These are production dummy/default paths and violate the project implementation rule; make chart callbacks required or provide a real editor-backed implementation.

## Layer Violations

- [ ] None found
- Violations found:
  - `NoteEditorScreen.kt`: chart callbacks are optional defaults that can silently drop mutations or throw from a production content call.
  - `ChartBlockCard.kt`: presentation transformation and rendering orchestration are concentrated in a monolithic UI component.

## Unrelated Changes

- [x] None found in the feature diff.
- The Compose checker’s 22 dynamic-tag findings are in unchanged legacy cards and are classified as a pre-existing repository gate issue, not an unrelated feature diff.

## UI Verification

- [ ] Skipped (no UI changes)
- [x] Texts verified against the design contract through chart test tags/semantics and the approved visual evidence contract; a fresh Stage 4 capture remains required.
- [x] Screenshot captured and compared — all five active-rendering PNG commands were freshly rerun and directly inspected against the approved mockups.
- [x] Design-critical reference anchors have bounds-based runtime proof tied to visual tags, according to `visual_evidence/reference-anchor-verification.md` and the Stage 1 contract check.
- [ ] Differences remaining: major blank/oversized Data grid, missing first-level Options screenshot proof, 40 dp tooltip dismiss target/overlap, and negative-value renderer risk; see `visual_evidence/evaluator-visual-verification.md`.

## Security

- [x] No secrets or tokens hardcoded
- [ ] No user-generated text, transcript, image content, identifier, or other sensitive content logged
- [x] Sensitive data not stored unencrypted by this feature
- Concerns: `NoteExporter.kt:101` and `:441` log user-provided image URLs on existing image-export failure paths. The lines are pre-existing outside the chart diff, but the changed exporter remains a sensitive-content logging concern for a later cleanup. Chart code adds no new logging, network, or permission path.

## Release Risk

**Level**: high  
**Reason**: The feature changes persisted document schema, editor interactions, rendering, and export. Runtime regression evidence is broad, but negative data rendering and the callback/tag/accessibility contract require correction before release.

- Backward compatible: yes, subject to the documented parser fallbacks
- Feature flag required: no
- Force update required: no
- Backend deployment dependency: no

## Remaining Risks

1. Bar/Line negative values are not drawn against a zero baseline; all-negative data can render outside the chart bounds.
2. Chart hit targets are equal-width full-height overlays and do not map to actual bar/line/pie geometry.
3. Chart callback defaults can silently no-op or throw when `NoteEditorScreenContent` is invoked without the full chart callback set.
4. Generic chart tags prevent reliable multi-chart/column targeting and violate the approved reference-anchor contract.
5. The tooltip dismiss `IconButton` is 40 dp and the nested view/data radio control lacks its own explicit tag/description.
6. Test coverage does not prove focused insertion, complete persistence, read-only VM guards, all renderer types/negative baseline, PDF content/placement, or large-table behavior.
7. The repository-wide Compose gate remains non-zero due to pre-existing dynamic tags in unchanged legacy cards.

## Recommendation

- [ ] ✅ Ready to merge
- [ ] ⚠️ Merge with noted risks
- [x] ❌ Do not merge — fix the required findings and expand the missing verification before human review.

## Required Findings

The following fix-pass statuses supersede the original review observations above. The original observations remain preserved as review history.

1. F-01 — Data grid row sizing, visible values, bounded scrolling.
   > **Fix Status:** Fixed ✅ — ChartDataTable now uses fixed 48dp rows, a bounded 360dp scroll container, block-scoped row/cell tags, and visible-row/large-table coverage in ChartVisualFlowTest (commit 7545d61; verified: env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.example.notesapp.ui.editor.chart exit 0; 2026-08-23)
2. F-02 — Zero-inclusive Bar/Line numeric domain and negative baseline.
   > **Fix Status:** Fixed ✅ — ChartRenderGeometry and ChartRenderer include zero in the domain, handle all-zero values, and place bars/lines relative to zero; platform geometry assertions cover negative values (commit 7545d61; verified: env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary exit 0; 2026-08-23)
3. F-03 — Production chart callback defaults.
   > **Fix Status:** Fixed ✅ — production NoteEditorScreen wires every chart mutation to NoteEditorViewModel, while a chart document without callbacks fails loudly instead of silently dropping mutations; ChartBlockCard has no mutation no-op defaults (commit 7545d61; verified: ./gradlew testDebugUnitTest exit 0; 2026-08-23)
4. F-04 — Stable tags, nested controls, tooltip target and overlap.
   > **Fix Status:** Fixed ✅ — chart tags are scoped by persisted block/column/row IDs, nested radio selectors are explicitly tagged and described, and tooltip dismissal is a contained 48dp control; visual bounds assertions cover the contract (commit 7545d61; verified: bash harness/scripts/check-compose-rules.sh exit 0; 2026-08-23)
5. F-05 — Geometry-aware datum targets and selected semantics.
   > **Fix Status:** Fixed ✅ — Bar/Line/Pie targets derive from the same chart geometry/domain as rendering, retain 48dp minimum hit targets, and expose selected content descriptions/highlight state (commit 7545d61; verified: env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartInteractionFlowTest#testDatumSelectionShowsAndDismissesCalloutForAllTypes exit 0; 2026-08-23)
6. F-06 — Chart presentation transformation and visual responsibility boundaries.
   > **Fix Status:** Fixed ✅ — normalization/parsing moved to ChartBlockCardModel, bitmap rendering to ChartRenderer, geometry to ChartRenderGeometry, and the card delegates to named plot, data-table, tooltip, and sheet responsibilities; no parser is invoked from the Composable body (commit 7545d61; verified: bash harness/scripts/check-architecture-rules.sh exit 0; 2026-08-23)
7. F-07 — Complete ChartBlock JSON field coverage.
   > **Fix Status:** Fixed ✅ — tests assert exact ID/type/title/rows/column IDs/selected ID, serialized field presence, unknown-block fidelity, unknown-type fallback, invalid-selection fallback, and legacy TableBlock preservation (commit 7545d61; verified: ./gradlew testDebugUnitTest --tests com.example.notesapp.ui.editor.mapper.NoteDocumentChartBlockTest exit 0; 2026-08-23)
8. F-08 — Focused insertion, panel closure, conversion, and auto-save.
   > **Fix Status:** Fixed ✅ — ViewModel integration coverage now sets focus, checks insertion order and panel closure, dispatches actual Table Options conversion, preserves the complete table shape, and verifies insertion/conversion/title auto-save completion (commit 7545d61; verified: ./gradlew testDebugUnitTest --tests com.example.notesapp.ui.editor.viewmodel.NoteEditorChartBlockIntegrationTest exit 0; 2026-08-23)
9. F-09 — Selected-column state, plotted values, and table immutability.
   > **Fix Status:** Fixed ✅ — Data-flow UI asserts Cost selection and unchanged January/value cells; ViewModel integration covers selection preservation through row/column operations and reload (commit 7545d61; verified: ./gradlew testDebugUnitTest --tests com.example.notesapp.ui.editor.viewmodel.NoteEditorChartDataIntegrationTest exit 0; 2026-08-23)
10. F-10 — Header-only, all-zero, Line, duplicate-header, and renderer recovery behavior.
    > **Fix Status:** Fixed ✅ — all chart types have header-only/all-zero UI assertions, parser coverage includes Line/duplicate/blank cases, and production bitmap-render failure injection preserves recovery controls and data (commit 7545d61; verified: env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.example.notesapp.ui.editor.chart exit 0; 2026-08-23)
11. F-11 — Read-only ViewModel and callback mutation guards.
    > **Fix Status:** Fixed ✅ — read-only tests cover cell, row, column, creation, conversion, table-action, and destructive-operation guards while preserving document equality (commit 7545d61; verified: ./gradlew testDebugUnitTest --tests com.example.notesapp.ui.editor.viewmodel.NoteEditorChartBlockIntegrationTest exit 0; 2026-08-23)
12. F-12 — PDF placement/content/fallback and multi-chart Markdown export.
    > **Fix Status:** Fixed ✅ — exporter tests inspect ZIP assets/relative links for multiple charts, PdfRenderer validates title/content ink placement, and invalid image paths retain table fallback content (commit 7545d61; verified: ./gradlew testDebugUnitTest --tests com.example.notesapp.util.NoteExporterChartTest exit 0; 2026-08-23)
13. F-13 — Coverage and large-table behavior.
    > **Fix Status:** Fixed ✅ — clean Kover reports 83.569% application line coverage, NoteEditorViewModel 96.5%, ChartTableParser 96.2%, and ChartBitmapRenderer 95.1%; 200-row mapper and bounded chart-grid regression tests pass (commit 7545d61; verified: ./gradlew clean koverLog --rerun-tasks exit 0; 2026-08-23)
14. F-14 — Separate Options evidence and machine-checkable visual artifact.
    > **Fix Status:** Fixed ✅ — first-level and second-level Options captures are separate artifacts and contract rows; ui_verification.json records 16/16 instrumented chart tests and passes its validator (commit 7545d61; verified: bash harness/scripts/check-ui-verification-artifact.sh docs/product/2026-08-20-chart-block exit 0; 2026-08-23)
15. F-15 — API capability evidence and loud unsupported environments.
    > **Fix Status:** Fixed ✅ — the capability matrix explicitly records verified API33 runtime evidence, targetSdk34 build evidence, and API24/API34 direct-runtime provisioning requirements under a fail-loudly policy; no unavailable runtime is claimed as tested (commit 7545d61; verified: bash harness/scripts/check-platform-evidence.sh docs/product/2026-08-20-chart-block --evaluate exit 0; 2026-08-23)
16. F-16 — Finding/status/evidence reconciliation and passing slices.
    > **Fix Status:** Fixed ✅ — all four existing feature_list.json slices remain passing, report fix-pass sections below reconcile every finding, and final gates are recorded in the summary/handoff (commit 7545d61; verified: bash harness/scripts/check-feature-lifecycle.sh exit 0; 2026-08-23)

> **Fix Pass:** 16/16 findings fixed; 0 unresolved (2026-08-23). Final implementation/evidence commit: 7545d61.
