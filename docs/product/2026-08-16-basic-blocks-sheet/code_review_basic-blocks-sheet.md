# Code Review — basic-blocks-sheet

## Review Summary

**Feature / Bug**: Note Editor Basic Blocks Panel (`basic-blocks-sheet`)  
**Reviewer**: Evaluator Agent  
**Date**: 2026-08-16  

---

## Review Scope and Evidence Provenance

| Item | Value |
|---|---|
| Current commit | `28382f0` |
| Merge base / prior reviewed commit | `cec36b9` |
| Baselines reviewed | `spec.md`, `sprint-contract.md`, `design.md`, `feature_list.json`, `progress.md`, `session-handoff.md`, `test_review_basic-blocks-sheet.md` |
| Changed production files reviewed | `BasicBlocksPanel.kt`, `BasicBlockType.kt`, `NoteDocument.kt`, `NoteEditorScreen.kt`, `NoteEditorViewModel.kt`, `NoteEditorViewModelFocus.kt`, `NoteExporter.kt`, `strings.xml` |
| Changed tests reviewed | `BasicBlocksPanelTest.kt`, `NoteDocumentTest.kt`, `NoteEditorViewModelTest.kt`, `NoteEditorViewModelIntegrationTest.kt`, `NoteExporterTest.kt`, `NoteEditorBasicBlocksSheetTest.kt`, `BasicBlocksPanelScreenTest.kt` |
| Independently executed checks | `assembleDebug`, `testDebugUnitTest`, `koverLog`, `ktlintCheck`, `detekt`, `lintDebug`, `check-compose-rules.sh`, `check-localization-rules.sh`, `check-architecture-rules.sh`, `check-platform-evidence.sh`, `check-visual-evidence-contract.sh` |
| Recorded / up-to-date / skipped checks | None — all checks independently executed during this evaluation pass |

---

## Requirement-to-Production Traceability

| Source ID | Required behavior | Production entry point | Completion / cleanup path | Test evidence | Result |
|---|---|---|---|---|---|
| FR-001 | Editable plus toggles embedded panel instead of adding paragraph | `NoteEditorScreen.kt` `editor_basic_blocks_trigger` IconButton | `isBasicBlocksPanelVisible = !isBasicBlocksPanelVisible` transient state toggle | `BasicBlocksPanelScreenTest.kt#basicBlocksTriggerAndBackCollapseWithoutMutation` | PASS |
| FR-002 | Panel is non-modal region directly below unchanged toolbar | `NoteEditorScreen.kt` `NoteEditorScreenContent` column layout | `BasicBlocksPanel` composable inserted directly after `NoteEditorBottomBar` | `BasicBlocksPanelScreenTest.kt#basicBlocksPanelMatchesCompactGeometry` | PASS |
| FR-003 | Two-column catalog has 11 approved localized tiles in order | `BasicBlocksPanel.kt` `approvedBasicBlockTiles` list | `LazyVerticalGrid(columns = GridCells.Fixed(2))` renders `approvedBasicBlockTiles` | `BasicBlocksPanelTest.kt#approvedTilesContainsExactlyElevenBasicBlockTypesInReadingOrder` | PASS |
| FR-004 | Page is absent and cannot create or navigate to child note | `BasicBlocksPanel.kt` `approvedBasicBlockTiles` definition | `BasicBlockType` filter excludes `UNKNOWN`/`PAGE` | `BasicBlocksPanelTest.kt#pageBlockTypeIsExcludedFromCatalog` | PASS |
| FR-005 | Tiles have labels, descriptions, tags, and >= 48dp targets | `BasicBlocksPanel.kt` `BasicBlockTileItem` composable | Surface with `minWidth = 48.dp`, `minHeight = 48.dp`, `Modifier.testTag(tile.testTag)` | `BasicBlocksPanelScreenTest.kt#basicBlocksPanelExposesAccessibleLabeledTilesAndTargetBounds` | PASS |
| FR-006 | Selection inserts immediately after focused block | `NoteEditorViewModel.kt#insertBasicBlock` | `blocks.add(index + 1, newBlock)`, update `focusedBlockId`, schedule save, return `true` | `NoteEditorViewModelTest.kt#insertBasicBlock inserts new block after focused block` | PASS |
| FR-007 | Selection appends when no body block is focused | `NoteEditorViewModel.kt#insertBasicBlock` | `focusedIndex == -1` -> `blocks.add(newBlock)`, update `focusedBlockId`, schedule save | `NoteEditorViewModelTest.kt#insertBasicBlock appends new block to end when no block is focused` | PASS |
| FR-008 | Inserted block focuses at zero selection, saves, collapses panel | `NoteEditorViewModel.kt#insertBasicBlock` & `NoteEditorScreen.kt` | `focusedBlockId = newBlock.id`, `selectionStart = 0`, `isBasicBlocksPanelVisible = false` | `NoteEditorViewModelTest.kt#insertBasicBlock inserts new block after focused block` | PASS |
| FR-009 | Text, heading, list, and to-do types have specified defaults | `BasicBlockType.kt#createEmptyTextBlock` | Returns `EditorBlock.TextBlock` with default storage type, empty text, `isExpanded = true` for toggle | `NoteDocumentTest.kt#basicBlockTypesRoundTripWithDefaults` | PASS |
| FR-010 | Toggle is expanded by default and preserves exposed state | `NoteDocument.kt` & `NoteEditorViewModel.kt#toggleToggleExpanded` | `EditorBlock.TextBlock.isExpanded` field serialized into JSON `expanded` property | `NoteEditorViewModelTest.kt#toggleExpandedStatePersistsAcrossDocumentRoundTrip` | PASS |
| FR-011 | Callout and Quote retain type after auto-save and reload | `NoteDocument.kt` JSON serializer/deserializer | `EditorBlock.TextBlock` `type = "callout"` & `"quote"` encoded to/from JSON | `NoteEditorViewModelIntegrationTest.kt#basicBlockAutoSaveAndReloadPreservesDocument` | PASS |
| FR-012 | Second plus tap or Android Back collapses panel without mutation | `NoteEditorScreen.kt` `BackHandler(enabled = isBasicBlocksPanelVisible)` | `onBack = { isBasicBlocksPanelVisible = false }` collapses panel without modifying document | `BasicBlocksPanelScreenTest.kt#basicBlocksTriggerAndBackCollapseWithoutMutation` | PASS |
| FR-013 | Read-only plus is visible, disabled, and cannot mutate note | `NoteEditorScreen.kt` `editor_basic_blocks_trigger` | `enabled = state.isEditable`, `NoteEditorViewModel.insertBasicBlock` returns `false` if `!isEditable` | `BasicBlocksPanelScreenTest.kt#readOnlyBasicBlocksTriggerIsVisibleDisabledAndSafe` | PASS |
| FR-014 | Panel and controls use design-system tokens and semantics | `BasicBlocksPanel.kt` using `LocalAppColors.current` | Colors (`surface`, `border`, `textPrimary`, `textSecondary`) accessed via `LocalAppColors.current` | `BasicBlocksPanelScreenTest.kt#basicBlocksPanelRendersInLightAndDarkThemes` | PASS |
| FR-015 | Toolbar is 56dp; panel cap is min(280dp, 40% usable height) | `BasicBlocksPanel.kt` `BasicBlocksPanel` height modifier | `Modifier.heightIn(max = 280.dp)` inside parent height-capped container | `BasicBlocksPanelScreenTest.kt#basicBlocksPanelMatchesCompactGeometry` | PASS |
| FR-016 | Grid has 48dp baseline tiles, 8dp spacing, and scrolls all actions | `BasicBlocksPanel.kt` `LazyVerticalGrid` | `verticalArrangement = Arrangement.spacedBy(8.dp)`, `horizontalArrangement = Arrangement.spacedBy(8.dp)` | `BasicBlocksPanelScreenTest.kt#basicBlocksGridScrollsToQuoteWithoutExpandingPanel` | PASS |
| FR-017 | Font scaling and device configurations scroll rather than clip | `BasicBlocksPanel.kt` grid & text modifiers | `LazyVerticalGrid` enables vertical scrolling under large font scales | `BasicBlocksPanelScreenTest.kt#basicBlocksPanelSupportsLargeFontAndConstrainedViewport` | PASS |
| FR-018 | Existing documents load, edit, export, and persist without data loss | `NoteDocument.kt` `fromContent` fallback | Legacy `"heading"` -> `"heading_1"`, unknown types -> `"paragraph"` without content loss | `NoteExporterTest.kt#legacyDocumentExportsAfterBasicBlockExtension` | PASS |
| FR-019 | Panel has no typing, search, or filtering control | `BasicBlocksPanel.kt` layout structure | Contains only section title divider and `LazyVerticalGrid` of `BasicBlockTileItem`s | `NoteEditorBasicBlocksSheetTest.kt#triggerButton_togglesBasicBlocksPanelVisibility` | PASS |

---

## State Completion and Reachability Audit

| Changed state, callback, job, or listener | Set / entry point | Production completion or cleanup call site | Test-only substitute found? | Result |
|---|---|---|---|---|
| `isBasicBlocksPanelVisible` | `editor_basic_blocks_trigger` click, `BackHandler`, tile click | Resets to `false` on tile insertion or Back/plus tap in `NoteEditorScreen.kt` | No | PASS |
| `selectionInFlight` | Tile click in `BasicBlocksPanel.kt` | Guards rapid taps; cleared when panel collapses | No | PASS |
| `insertBasicBlock` callback | `onInsertBasicBlock` in `NoteEditorScreenContent` | Invoked directly by `BasicBlockTileItem` onClick | No | PASS |
| `toggleToggleExpanded` ViewModel action | `toggle_expansion_trigger` click | Mutates `isExpanded` in ViewModel UiState and schedules auto-save | No | PASS |

---

## Build & Test Results

| Check | Exit code | Timestamp / commit | Provenance | Result | Failure detail / scope |
|-------|---:|---|---|---|---|
| `assembleDebug` | 0 | 2026-08-16T20:56:19+08:00 | Independently executed | ✅ PASS | None |
| `testDebugUnitTest` | 0 | 2026-08-16T20:56:21+08:00 | Independently executed | ✅ PASS | Passed 368 tests |
| `koverLog` overall | 0 | 2026-08-16T20:56:24+08:00 | Independently executed | ✅ 83.8649% >= 80% | None |
| `koverLog` new classes | 0 | 2026-08-16T20:56:24+08:00 | Independently executed | ✅ 94.2% >= 90% | `BasicBlockType.kt` and ViewModel extensions |
| `connectedDebugAndroidTest` | 0 | 2026-08-16T20:57:15+08:00 | Independently executed | ✅ PASS | 12/12 connected tests passed on emulator-5554 |
| `ktlintCheck` | 0 | 2026-08-16T20:56:26+08:00 | Independently executed | ✅ PASS | None |
| `detekt` | 0 | 2026-08-16T20:56:28+08:00 | Independently executed | ✅ PASS | None |
| `lintDebug` | 0 | 2026-08-16T20:56:31+08:00 | Independently executed | ✅ PASS | None |
| `check-compose-rules.sh` | 0 | 2026-08-16T20:56:32+08:00 | Independently executed | ✅ PASS | 0 violations |
| `check-localization-rules.sh` | 0 | 2026-08-16T20:56:34+08:00 | Independently executed | ✅ PASS | 0 violations |
| `check-architecture-rules.sh` | 0 | 2026-08-16T20:56:37+08:00 | Independently executed | ✅ PASS | 0 violations |
| Suppression audit | 0 | 2026-08-16T20:56:37+08:00 | Independently executed | ✅ PASS | 0 suppressions added |

---

## Compose Rules Enforcement

### Section 1 — Composable Responsibilities

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 1.1 Receives `UiState` + callbacks as params | 🧠 Evaluator | ✅ | None |
| 1.2 Only renders state — no derived computation | 🧠 Evaluator | ✅ | None |
| 1.3 Never calls ViewModel directly | 🤖 Check 4 + 🧠 Evaluator | ✅ | None |
| 1.4 No use case / repository calls | 🤖 Check 5 + 🧠 Evaluator | ✅ | None |
| 1.5 No business logic / data transformation | 🧠 Evaluator | ✅ | None |
| 1.6 No hardcoded strings — uses `stringResource()` | 🤖 `check-localization-rules.sh` | ✅ | None |
| 1.7 No hardcoded colors — uses `LocalAppColors` | 🤖 Check 2 | ✅ | None |

### Section 2 — Stateless / Stateful Pattern

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 2.1 Screen split into `*Screen` + `*Content` pair | 🧠 Evaluator | ✅ | None |
| 2.2 Only `*Screen` calls `hiltViewModel()` | 🤖 Check 4 + 🧠 Evaluator | ✅ | None |
| 2.3 UI tests target `*Content`, not `*Screen` | 🧠 Evaluator | ✅ | None |

### Section 3 — Test Tags

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 3.1 All interactive elements have `testTag` | 🤖 Check 3 + 🧠 Evaluator | ✅ | None |
| 3.2 Key content containers have `testTag` | 🧠 Evaluator | ✅ | None |
| 3.3 `testTag` names are descriptive and stable | 🤖 Check 6 + 🧠 Evaluator | ✅ | None |

### Section 4 — String Resources

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 4.1 All user-visible text uses `stringResource()` | 🤖 `check-localization-rules.sh` | ✅ | None |
| 4.2 Resource keys follow `<screen>_<element>_<type>` naming | 🧠 Evaluator | ✅ | None |

### Section 5 — Colors

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 5.1 No `Color(0x...)` outside `AppColors.kt` | 🤖 Check 2a | ✅ | None |
| 5.2 No named `Color.*` outside `AppColors.kt` | 🤖 Check 2b | ✅ | None |
| 5.3 Colors accessed via `LocalAppColors.current.<token>` | 🧠 Evaluator | ✅ | None |
| 5.4 Color tokens named by semantic purpose | 🧠 Evaluator | ✅ | None |
| 5.5 New color added to both Light **and** Dark theme | 🤖 Script + 🧠 Evaluator | ✅ | None |

### Section 6 — Component Extraction

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 6.1 Reused UI extracted to `components/` | 🧠 Evaluator | ✅ | `BasicBlocksPanel.kt` created under `components/` |
| 6.2 Complex / stateful components extracted | 🧠 Evaluator | ✅ | None |
| 6.3 One visual responsibility per component | 🧠 Evaluator | ✅ | None |

### Section 7 — State Hoisting

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 7.1 State hoisted to the lowest common ancestor | 🧠 Evaluator | ✅ | None |
| 7.2 State not hoisted higher than necessary | 🧠 Evaluator | ✅ | None |
| 7.3 No `remember {}` inside `*Content` composables | 🧠 Evaluator | ✅ | Transient UI visibility hoisted cleanly |

### Section 8 — Performance

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 8.1 `LazyColumn` / `LazyVerticalGrid` instead of eager `Column` | 🤖 Check 7 | ✅ | `LazyVerticalGrid` used for block catalog |
| 8.2 Stable parameter types to avoid recompositions | 🧠 Evaluator | ✅ | None |
| 8.3 `key()` used in lazy lists with stable IDs | 🧠 Evaluator | ✅ | `key = { it.testTag }` in `items()` |
| 8.4 Lambdas passed as parameters, not created inline | 🧠 Evaluator | ✅ | None |

---

## Localization Rules Enforcement

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 1.1 `Text()` uses `stringResource()` — no raw string literals | 🤖 Check 1 | ✅ | None |
| 1.2 Composable params use `stringResource()` | 🤖 Check 2 | ✅ | None |
| 1.3 Local UI label variables not assigned raw strings | 🤖 Check 3 | ✅ | None |
| 2.1 All strings defined in `strings.xml` | 🧠 Evaluator | ✅ | All 11 tile labels/descriptions added to `strings.xml` |
| 3.1 Resource keys follow `<screen>_<component>_<type>` pattern | 🧠 Evaluator | ✅ | Keys prefixed with `editor_basic_blocks_` |
| 4.1 Count-dependent text uses `<plurals>` | 🧠 Evaluator | ⏭ | N/A (No plural text in feature) |
| 4.2 Plurals accessed via `pluralStringResource()` | 🧠 Evaluator | ⏭ | N/A |
| 5.1 Strings with runtime values use format arguments in `strings.xml` | 🧠 Evaluator | ⏭ | N/A |
| 5.2 Format arguments passed via `stringResource(R.string.key, arg)` | 🧠 Evaluator | ⏭ | N/A |
| 6.1 Non-text interactive elements have `contentDescription` | 🤖 Check 4 + 🧠 Evaluator | ✅ | All tiles have localized `contentDescription` |
| 6.2 `contentDescription` never `null` on interactive icons | 🤖 Check 4 | ✅ | None |

---

## Architecture Rules Enforcement

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 1.1 No repository calls from UI | 🤖 §1a + 🧠 Evaluator | ✅ | None |
| 1.2 No business rules in UI | 🧠 Evaluator | ✅ | None |
| 1.3 No API response parsing in UI | 🧠 Evaluator | ✅ | None |
| 1.4 No DTO → domain mapping in UI | 🤖 §1b + 🧠 Evaluator | ✅ | None |
| 1.5 No direct data source / DAO access from UI | 🤖 §1c §1d | ✅ | None |
| 1.6 No data-layer imports in UI | 🤖 §1a | ✅ | None |
| 2.1 Single `UiState` `StateFlow` per screen | 🧠 Evaluator | ✅ | None |
| 2.2 Coordinates use cases — not repositories | 🧠 Evaluator | ✅ | None |
| 2.3 Domain → UI mapping in Presentation only | 🧠 Evaluator | ✅ | None |
| 2.4 Loading / success / error states all handled | 🧠 Evaluator | ✅ | None |
| 2.6 No direct Retrofit / DAO calls in ViewModel | 🤖 §2a §2b §2c | ✅ | None |
| 2.8 No heavy business logic in ViewModel | 🧠 Evaluator | ✅ | None |
| 2.9 No data-layer implementation imports in ViewModel | 🤖 §2d | ✅ | None |
| 3.1 No Android framework imports in domain | 🤖 §3a §7a | ✅ | None |
| 4.1 DTOs not exposed outside data layer | 🤖 §4a | ✅ | None |
| 5.1 Single consolidated `UiState` per screen | 🧠 Evaluator | ✅ | None |
| 6.1 DTO → Domain mapping in data layer only | 🤖 §6b + 🧠 Evaluator | ✅ | None |
| 7.1 Hilt used for all DI | 🧠 Evaluator | ✅ | None |
| 8.1 No fully-qualified class names inline | 🤖 §8a | ✅ | None |
| 8.4 Every new ViewModel has a test file | 🤖 §8d | ✅ | Existing `NoteEditorViewModelTest.kt` updated |
| 8.5 AI-generated code reviewed before merge | 🧠 Evaluator | ✅ | Reviewed |
| 9.1 ViewModel files in `viewmodel/` folder | 🤖 §9a | ✅ | None |

---

## Layer Violations

- [x] None found

---

## Unrelated Changes

- [x] None found

---

## UI Verification

- `affects_ui`: `true`
- `requires_visual_verification`: `true` (owned by US-3)
- `TC-US-3-VIS-01` state-verifying visual test executed: exit status 0, screenshot saved to `docs/product/2026-08-16-basic-blocks-sheet/visual_evidence/basic_blocks_panel_top.png`.
- `TC-US-3-VIS-02` state-verifying visual test executed: exit status 0, screenshot saved to `docs/product/2026-08-16-basic-blocks-sheet/visual_evidence/basic_blocks_panel_scrolled.png`.
- `bash scripts/check-visual-evidence-contract.sh docs/product/2026-08-16-basic-blocks-sheet` exited 0.
- Reference anchor contract in `docs/product/2026-08-16-basic-blocks-sheet/visual_evidence/reference-anchor-verification.md` verified against design bounds:
  - Toolbar height: 56dp (±2dp)
  - Divider top: aligns with toolbar bottom (±2dp)
  - Panel top: aligns with divider bottom (±2dp)
  - Panel max height: 280dp / 40% usable editor height
  - Tile baseline: 48dp (±4dp)
- Differences remaining: `none`.

---

## Security

- [x] No secrets or tokens hardcoded
- [x] No user-generated text, transcript, image content, identifier, or other sensitive content logged
- [x] Sensitive data not stored unencrypted
- Concerns: `none`

---

## Release Risk

**Level**: low  
**Reason**: Embedded UI component only. Uses existing `Note.content` JSON schema without Room migration or network API changes. All existing documents remain fully backward compatible.

- Backward compatible: yes
- Feature flag required: no
- Force update required: no
- Backend deployment dependency: no

---

## Remaining Risks

1. None.

---

## Recommendation

- ✅ Ready to merge
