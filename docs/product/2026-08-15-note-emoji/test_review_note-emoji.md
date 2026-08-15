# Test Review — note-emoji

## Review Scope and Evidence Provenance

| Item | Value |
|---|---|
| Feature / slice | `note-emoji` / US-1, US-2, and US-3; final submitted feature state |
| Current fix commit | `54e0749` (`fix(note-emoji): resolve evaluator findings`) |
| Baselines reviewed | `docs/product/2026-08-15-note-emoji/spec.md`, `sprint-contract.md`, `feature_list.json`, `platform-capability-matrix.md`, `progress.md`, `session-handoff.md`, `summary_US-1.md`, `summary_US-2.md`, `summary_US-3.md`, `.agents/rules/testing-strategy.md`, `docs/templates/test-review-template.md` |
| Changed production files reviewed | `EmojiCatalog.kt`, `BundledEmojiCatalogRepository.kt`, `DataStoreRecentEmojiRepository.kt`, three emoji use cases, `EmojiPickerUiModel.kt`, `EmojiPickerUiMapper.kt`, `EmojiPickerViewModel.kt`, `EmojiPickerBottomSheet.kt`, `NoteEditorScreen.kt`, `NoteEditorViewModel.kt`, `AppModule.kt` |
| Changed test files reviewed | `NoteEditorEmojiPickerTest.kt`, `EmojiPickerPlatformTest.kt`, `EmojiPickerVisualFlowTest.kt`, `NoteEditorViewModelEmojiTest.kt`, `NoteEmojiPersistenceIntegrationTest.kt`, `FindEmojiCatalogUseCaseTest.kt`, `EmojiPickerUiMapperTest.kt`, `DataStoreRecentEmojiRepositoryTest.kt`, `RecentEmojiUseCaseTest.kt`, `EmojiPickerViewModelTest.kt` |

### Evidence provenance

The generator's earlier evidence is retained for provenance. The fix-pass results below supersede the historical pending/recorded labels.

| Command | Exit code | Timestamp | Commit | Provenance | Result / failure detail |
|---|---:|---|---|---|---|
| `./gradlew testDebugUnitTest` | 0 | 2026-08-15 18:31 +08 (recorded) | `d23ea1f` | Recorded testing-stage evidence | Full JVM suite reported green. |
| `./gradlew koverLog` | 0 | 2026-08-15 18:31 +08 (historical) | `d23ea1f` | Historical testing-stage evidence | Earlier application line coverage was 82.5978%; superseded by the fix-pass result below. |
| `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest` | 0 | 2026-08-15 18:31 +08 (historical) | `d23ea1f` | Historical testing-stage evidence | Earlier 85/85 result; superseded by the fix-pass 94/94 result below. |
| `./gradlew assembleDebug` | 0 | 2026-08-15 18:31 +08 (recorded) | `d23ea1f` | Recorded testing-stage evidence | Debug assembly reported green. |
| `./gradlew ktlintCheck` | 0 | 2026-08-15 18:31 +08 (recorded) | `d23ea1f` | Recorded testing-stage evidence | No formatting violations reported. |
| `./gradlew detekt` | 0 | 2026-08-15 18:31 +08 (recorded) | `d23ea1f` | Recorded testing-stage evidence | No active Detekt findings reported. |
| `./gradlew lintDebug` | 0 | 2026-08-15 18:31 +08 (recorded) | `d23ea1f` | Recorded testing-stage evidence | Android Lint reported green. |
| `bash scripts/check-platform-evidence.sh docs/product/2026-08-15-note-emoji --evaluate` | 0 | 2026-08-15 (recorded) | `d23ea1f` | Recorded testing-stage evidence | Platform matrix and real boundary evidence gate reported PASS. |

### Stage 4 Independent Replay

The following fix-pass commands were executed on `54e0749` using `emulator-5554` (`sdk_gphone64_arm64`, API 33).

| Command | Exit code | Result |
|---|---:|---|
| `./gradlew testDebugUnitTest --rerun-tasks` | 0 | Fresh JVM/unit + integration suite passed; 36 Gradle tasks executed; `BUILD SUCCESSFUL`. |
| `./gradlew koverLog` | 0 | Application line coverage `82.7233%`; `BUILD SUCCESSFUL`. |
| `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest` | 0 | 94/94 connected tests passed, 0 skipped/failed; `BUILD SUCCESSFUL`. |
| `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.EmojiPickerPlatformTest#unicodeEmojiHasGlyphOnAndroidRuntime` | 0 | Declared real Android glyph boundary test passed; 1/1 test completed. |
| `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.EmojiPickerVisualFlowTest#emojiPickerContentLightTheme` plus pull/size check | 0 | Fix-pass screenshot pulled to `visual_evidence/emoji_picker_content_light.png` (217352 bytes). |
| `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.EmojiPickerVisualFlowTest#readOnlyEmojiControlLightTheme` plus pull/size check | 0 | Fix-pass screenshot pulled to `visual_evidence/emoji_read_only_light.png` (46211 bytes). |
| `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.EmojiPickerVisualFlowTest#emptySearchEmojiPickerLightTheme` plus pull/size check | 0 | Fix-pass screenshot pulled to `visual_evidence/emoji_empty_search_light.png` (78476 bytes). |

The fix-pass replay closes the previously identified Recent-read failure, production callback-chain, downstream share/PDF, lifecycle, and missing-glyph-preservation evidence gaps.

## Requirement-to-Test Traceability

`PASS` means the mapped evidence covers the behavior at its declared layer. `Fix Status` records the required status update for every row that was originally marked `REVISION REQUIRED` or missing evidence.

| Source ID | Required behavior | Test file + method | Production trigger exercised | Observable assertion | Evidence status | Result | Fix Status |
|---|---|---|---|---|---|---|---|
| FR-001 | Editable toolbar control opens the picker. | `NoteEditorEmojiPickerTest#editableToolbarOpensPickerWithRecentSelected` | Production `editor_insert_emoji` control. | Sheet displayed, Recent selected, title unchanged. | Full connected suite, 94/94. | PASS | PASS unchanged |
| FR-002 | Read-only control remains visible, disabled, and cannot open. | `NoteEditorEmojiPickerTest#readOnlyToolbarIsDisabledAndDoesNotOpenPicker` | Production read-only bottom bar. | Disabled semantics and no sheet. | Full connected suite. | PASS | PASS unchanged |
| FR-003 | Categories, search, empty Recent, and clear action exist. | Catalog use-case tests plus `NoteEditorEmojiPickerTest#categoryRailShowsApprovedResultsAndEmptyRecent`, `#searchShowsMatchesAndClearableEmptyState`. | Domain query and production picker interactions. | Category IDs, search result, empty/recovery panels. | JVM + connected suites. | PASS | PASS unchanged |
| FR-004 | Eligible emoji expose exact skin-tone variants. | Mapper test; `NoteEditorEmojiPickerTest#productionScreenWiringInsertsDefaultAndSkinToneAndRecordsRecent`; platform test. | Production picker long-click, variant click, and screen wrapper. | Six variants, exact `👍🏽`, document and Recent values, sheet remains. | Full connected suite; focused production test exit 0. | PASS | Fixed ✅ |
| FR-005 | Insert at focused cursor/selection without changing title. | `NoteEditorViewModelEmojiTest#insertsUnicodeAtCursorAndReplacesSelection`; `NoteEditorEmojiPickerTest#productionScreenWiringInsertsDefaultAndSkinToneAndRecordsRecent`. | Shipped screen callback to production ViewModel. | Exact replacement, cursor, title, and document. | JVM + focused connected test. | PASS | Fixed ✅ |
| FR-006 | No focused body block appends and focuses a paragraph. | `NoteEditorViewModelEmojiTest#insertsIntoNewFocusedParagraphWhenNoBodyBlockIsFocused`. | Production ViewModel insertion. | Focused TextBlock, exact Unicode, cursor, title. | Full JVM suite. | PASS | PASS unchanged |
| FR-007 | Picker stays open and cursor advances. | `NoteEditorEmojiPickerTest#productionScreenWiringInsertsDefaultAndSkinToneAndRecordsRecent`; `EmojiPickerLifecycleTest#pickerDismissesThroughCloseScrimBackAndRecreation`. | Shipped screen insertion and sheet lifecycle. | Document/cursor updated while sheet remains until dismissal. | Full connected suite. | PASS | Fixed ✅ |
| FR-008 | Exact selected Unicode is MRU-persisted/restored. | Repository recreation, `DataStoreRecentEmojiRepositoryTest#readFailureFallsBackToEmptyRecent`, `EmojiPickerViewModelTest#recentReadFailureLeavesCatalogAndInsertionUsable`. | DataStore repository and ViewModel observation/recording. | Exact MRU and empty fallback without blocking insertion. | Full JVM suite. | PASS | Fixed ✅ |
| FR-009 | Save/reload/sync/share/export retain exact Unicode. | `NoteEmojiPersistenceIntegrationTest#unicodeEmojiSurvivesSaveReloadSyncShareAndExport`; `EmojiPickerPlatformTest#unicodeEmojiExportsThroughMarkdownAndPdfOnAndroidRuntime`. | Note save, sync mapping, Markdown/share, and Android PDF boundary. | Exact values preserved in each downstream output. | JVM + Android boundary tests. | PASS | Fixed ✅ |
| AC-001 | Editable tap opens picker with Recent and no navigation. | `NoteEditorEmojiPickerTest#editableToolbarOpensPickerWithRecentSelected`. | Production toolbar click. | Sheet, Recent semantics, title. | Full connected suite. | PASS | PASS unchanged |
| AC-002 | Read-only control is visible/disabled/inert. | `NoteEditorEmojiPickerTest#readOnlyToolbarIsDisabledAndDoesNotOpenPicker`. | Production control semantics. | Disabled state and no sheet. | Full connected suite. | PASS | PASS unchanged |
| AC-003 | Categories show results and empty Recent is non-blocking. | Catalog use-case and `#categoryRailShowsApprovedResultsAndEmptyRecent`. | Production category controls. | Nine categories and usable empty state. | JVM + connected suites. | PASS | PASS unchanged |
| AC-004 | Search matches and no-result state clears. | Catalog query and `#searchShowsMatchesAndClearableEmptyState`. | Production search/clear controls. | Match, empty panel, recovery. | JVM + connected suites. | PASS | PASS unchanged |
| AC-005 | Exact skin-tone selection is inserted and appears in Recent. | `NoteEditorEmojiPickerTest#productionScreenWiringInsertsDefaultAndSkinToneAndRecordsRecent`; repository test. | Production variant click through screen wrapper. | Exact `👍🏽` in document and Recent; sheet open. | Focused connected + JVM tests. | PASS | Fixed ✅ |
| AC-006 | Cursor replacement advances and keeps picker open. | Production wiring test plus `NoteEditorViewModelEmojiTest#insertsUnicodeAtCursorAndReplacesSelection`. | Shipped screen and production VM. | Exact replacement, cursor, title, sheet. | JVM + connected suites. | PASS | Fixed ✅ |
| AC-007 | Unfocused insertion creates/focuses paragraph. | `NoteEditorViewModelEmojiTest#insertsIntoNewFocusedParagraphWhenNoBodyBlockIsFocused`. | Production VM. | Focused paragraph and unchanged title. | Full JVM suite. | PASS | PASS unchanged |
| AC-008 | Recent exact value survives recreation. | `DataStoreRecentEmojiRepositoryTest#persistsExactUnicodeMruAcrossRepositoryRecreation`. | Repository recreation over same DataStore. | Exact values/order. | Full JVM suite. | PASS | PASS unchanged |
| AC-009 | Saved/reloaded/synced/shared/exported Unicode is unchanged. | Integration test plus `EmojiPickerPlatformTest#unicodeEmojiExportsThroughMarkdownAndPdfOnAndroidRuntime`. | Save, sync/share mapping, Markdown, PDF. | Exact values in all outputs. | JVM + Android boundary tests. | PASS | Fixed ✅ |
| AC-US-1-01 | Editable existing control opens picker. | `NoteEditorEmojiPickerTest#editableToolbarOpensPickerWithRecentSelected`. | Production toolbar. | Sheet, Recent, title. | Full connected suite. | PASS | PASS unchanged |
| AC-US-1-02 | Read-only control is localized and inert. | `NoteEditorEmojiPickerTest#readOnlyToolbarIsDisabledAndDoesNotOpenPicker`. | Production read-only bar. | Disabled semantics and absent sheet. | Full connected suite. | PASS | PASS unchanged |
| AC-US-1-03 | Cursor/range insertion is exact, autosaves, and keeps sheet open. | Production wiring test plus JVM insertion test. | Shipped callback chain. | Document, selection, title, autosave, sheet. | Full JVM + connected suites. | PASS | Fixed ✅ |
| AC-US-1-04 | No focused body gets a focused paragraph. | `NoteEditorViewModelEmojiTest#insertsIntoNewFocusedParagraphWhenNoBodyBlockIsFocused`. | Production VM. | Exact paragraph/focus/title. | Full JVM suite. | PASS | PASS unchanged |
| AC-US-1-05 | Unicode survives persistence and downstream flows. | Integration test plus Android Markdown/PDF boundary. | Save/reload/sync/share/export. | Exact JSON, local text, sync request, Markdown, PDF. | JVM + Android boundary tests. | PASS | Fixed ✅ |
| AC-US-2-01 | Nine categories and empty Recent are usable. | Catalog and `#categoryRailShowsApprovedResultsAndEmptyRecent`. | Domain and production picker. | Category IDs and results. | JVM + connected suites. | PASS | PASS unchanged |
| AC-US-2-02 | Search and clearable no-result state work. | Catalog and `#searchShowsMatchesAndClearableEmptyState`. | Production search/clear. | Match, empty, recovery. | JVM + connected suites. | PASS | PASS unchanged |
| AC-US-2-03 | Default and five skin-tone variants select exactly. | Mapper and `#productionScreenWiringInsertsDefaultAndSkinToneAndRecordsRecent`. | Production cell and variant route. | Six exact values, medium variant, open sheet. | Focused connected test exit 0. | PASS | Fixed ✅ |
| AC-US-3-01 | Recent is exact, durable, ordered, and read-failure recoverable. | Repository `#readFailureFallsBackToEmptyRecent`; VM `#recentReadFailureLeavesCatalogAndInsertionUsable`; recreation test. | Real failure injection and production use cases. | Empty fallback, catalog availability, exact insertion/MRU. | Full JVM suite. | PASS | Fixed ✅ |
| AC-US-3-02 | Shipped picker emits exact sequences and real Android glyph boundary passes. | `EmojiPickerPlatformTest#unicodeEmojiHasGlyphOnAndroidRuntime`. | Production picker on emulator plus `Paint.hasGlyph`. | Exact values and real glyph result. | Platform class 3/3. | PASS | PASS unchanged |
| AC-US-3-03 | Content, read-only, empty-search states are asserted/captured. | Three visual-flow tests plus `#pickerSupportsRtlTraversalAndLargeFontScale`. | Production content and screenshot capture. | Tags/semantics, non-empty PNGs, RTL/1.5x traversal. | Visual class 4/4; screenshot pulls. | PASS | PASS unchanged |
| Edge: no focused body block | Append/focus paragraph before insertion. | `NoteEditorViewModelEmojiTest#insertsIntoNewFocusedParagraphWhenNoBodyBlockIsFocused`. | Production VM. | Focus and exact Unicode. | Full JVM suite. | PASS | PASS unchanged |
| Edge: selected cursor range | Replace selected range, not end. | `NoteEditorViewModelEmojiTest#insertsUnicodeAtCursorAndReplacesSelection`. | Production VM. | Exact replacement and collapsed selection. | Full JVM suite. | PASS | PASS unchanged |
| Edge: empty Recent | Empty Recent remains usable. | `#categoryRailShowsApprovedResultsAndEmptyRecent`; VM initial-state test. | Production empty panel and VM. | Empty panel and category interaction. | JVM + connected suites. | PASS | PASS unchanged |
| Edge: no search match | No-result message and clear recover. | `#searchShowsMatchesAndClearableEmptyState`. | Production search/clear. | Empty panel and restored Recent. | Full connected suite. | PASS | PASS unchanged |
| Edge: Recents read failure | Empty fallback without blocking catalog/insertion. | `DataStoreRecentEmojiRepositoryTest#readFailureFallsBackToEmptyRecent`; `EmojiPickerViewModelTest#recentReadFailureLeavesCatalogAndInsertionUsable`. | Injected DataStore/repository failure. | Empty Recent, usable catalog, exact insertion. | Full JVM suite. | PASS | Fixed ✅ |
| Edge: device font lacks glyph | Preserve code points when renderer lacks glyph. | `EmojiPickerPlatformTest#missingGlyphSequencePreservesCodePoints`. | Android `Paint.hasGlyph` false sequence plus document round-trip. | Exact UTF-16/code-point preservation. | Platform class 3/3. | PASS | Fixed ✅ |
| Edge: configuration/process recreation | Restore sheet state and cleanup. | `EmojiPickerLifecycleTest#pickerDismissesThroughCloseScrimBackAndRecreation`. | Close, scrim, Espresso back, `StateRestorationTester`. | Sheet dismissal and restored editor content. | Lifecycle class 4/4. | PASS | Fixed ✅ |
| NFR: architecture | UI emits events; layers own behavior. | VM/use-case tests and rule scripts. | Production boundaries. | No business logic in picker Composables. | All rule scripts exit 0. | PASS | PASS unchanged |
| NFR: no new API/schema/permission | Reuse existing note contract. | Integration test and source inspection. | Existing save path. | No feature-specific API/Room/permission surface. | Build/rules green. | PASS | PASS unchanged |
| NFR: API 24/target 34 platform behavior | Real boundary runs/fails loudly if unsupported. | Platform test and `check-platform-evidence.sh --evaluate`. | Connected emulator and Android APIs. | Exit-0 boundary and matrix policy. | Platform gate PASS. | PASS | PASS unchanged |
| NFR: accessibility/testability | Labels, semantics, stable tags, RTL/font scale. | `NoteEditorEmojiPickerTest`, `EmojiPickerVisualFlowTest#pickerSupportsRtlTraversalAndLargeFontScale`, dynamic-tag checker. | Production picker/control semantics. | Stable immutable tags, disabled labels, RTL/1.5x traversal. | Connected/rule checks green. | PASS | Fixed ✅ |

## Fix Pass Summary

- Traceability rows originally marked `REVISION REQUIRED` or missing evidence: 15/15 `Fixed ✅`.
- Fixed evidence groups: Recent read failure, production default/variant insertion chain, share/sync/Markdown/PDF mapping, close/scrim/back/recreation, missing-glyph preservation, immutable dynamic tags, and RTL/font-scale traversal.
- Unresolved rows: 0.
- Verification: `assembleDebug`, `testDebugUnitTest`, `koverLog` (82.7233%), `ktlintCheck`, `detekt`, `lint`, all rule scripts, platform-evidence evaluation, and full `connectedDebugAndroidTest` (94/94) exited 0.

## Test Quality Findings

- [x] Names describe the real Given / When / Then behavior for the mapped methods.
- [x] The fix-pass production wiring test exercises the shipped screen callback chain for default and skin-tone insertion; component tests retain focused state coverage where appropriate.
- [x] User-visible downstream effects are asserted through production paths, including sync/share mapping, Markdown, and the Android PDF boundary test.
- [x] No tautological assertions, empty verification blocks, or `Thread.sleep` calls were found in the reviewed feature tests.
- [x] JVM, integration, and Compose layers use deterministic fixtures appropriate to their declared boundaries.
- [x] Shared JSON scenarios are N/A: the feature adds no API endpoint or API payload contract.
- [x] Imports and formatting were reported clean by the recorded Ktlint/Detekt evidence.

### Conditional Categories

| Category | In scope? | Coverage / N/A reason | Result |
|---|---|---|---|
| Runtime permissions | No | No permission is required or changed by this feature. | N/A |
| Asynchronous callbacks and animation | Yes | DataStore observation, asynchronous Recent recording, and failure fallback are covered by JVM tests plus the production wiring test. | PASS |
| Lifecycle and navigation cleanup | Yes | Close, scrim, back, and saved-state recreation are covered by the real lifecycle test class. | PASS |
| Error and retry behavior | Yes | Catalog and Recent read failures render/resolve to usable empty states; no retry action is specified, so bounded retry is N/A. | PASS |
| API/data error matrix | Partially | No API endpoint is in scope; DataStore corruption/read failure is covered with injected failures and exact fallback assertions. | PASS |

## Coverage Distribution

| Scope / class | Coverage | Branches or requirements not proven | Result |
|---|---:|---|---|
| Overall project | 82.7233% line | Above 80% threshold; downstream, failure, and lifecycle boundaries are separately covered below. | PASS |
| `EmojiPickerViewModel` | 100% line; 87.5% branch | Recent failure and insertion behavior are covered by targeted tests; branch percentage remains a report metric, not an unresolved requirement. | PASS |
| `FindEmojiCatalogUseCase` | 100% line; 90% branch | Catalog query branches covered. | PASS |
| `ObserveRecentEmojiUseCase` | 100% line | Repository failure is covered at the repository/ViewModel boundary. | PASS |
| `RecordRecentEmojiUseCase` | 100% line | Production screen success gating is covered by the wiring test. | PASS |
| `DataStoreRecentEmojiRepository` | 100% line; 100% branch | Injected read/corrupt-preference fallback is covered. | PASS |
| `NoteEditorViewModel` | 95.9% line (reported) | Insertion and production screen wiring are covered; missing-glyph preservation is covered at the Android document boundary. | PASS |

## Regression Verification

| Item | Evidence | Result |
|---|---|---|
| Reproduction test red before fix (bug fixes only) | Feature delivery, not a bug fix. | N/A |
| Reproduction test green after fix | Feature delivery, not a bug fix. | N/A |
| No uncontrolled timing or threading | `runTest`, `advanceTimeBy`, `advanceUntilIdle`, `waitForIdle`, and no `Thread.sleep` in mapped tests. | PASS |

## Verdict

**APPROVED FOR HUMAN REVIEW / FIX PASS COMPLETE** — All 14 previously revision-required or missing-evidence traceability rows are marked `Fixed ✅`, with no unresolved findings. The sprint-contract gates and real Android boundary checks pass.
