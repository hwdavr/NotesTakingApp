# Sprint Contract — Note Editor Basic Blocks Panel

## Sprint Overview

* **Sprint:** P08-16-basic-blocks-panel
* **Feature:** Note Editor Basic Blocks Panel
* **Duration:** 1 sprint

## Scope

### In Scope

* [ ] Backward-compatible JSON, edit, render, and export support for the 11 approved basic block choices.
* [ ] Inline Basic blocks catalog under the unchanged Note Editor toolbar, with no Page action.
* [ ] Focus-aware insertion, no-focus append, empty defaults, auto-save, and selection focus.
* [ ] Expanded Toggle list state, read-only protection, Android Back behavior, accessibility, and compact scrolling.
* [ ] JVM, integration, instrumented UI, runtime geometry, and visual-evidence verification.

### Out of Scope

* Modal, overlay, or swipe-dismiss block pickers.
* Page blocks, child notes, navigation, images, tables, voice, links, mentions, undo/redo, or changes to other toolbar actions.
* Nested toggles, drag reorder, slash commands, search, favorites, recents, custom templates, and list nesting.
* Room migrations, API changes, permissions, hardware capability, or external services.

## Platform Capability & Environment Contract

See [platform-capability-matrix.md](platform-capability-matrix.md). This is not a special platform-bound feature: it uses existing Compose UI, Android Back, and local JSON state with no new Android service or hardware adapter. Instrumented UI tests are still mandatory for touch, scrolling, Back, semantics, geometry, and rendering. Missing emulator evidence fails loudly.

## Spec Coverage Matrix

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001 | The editable plus toggles the embedded panel instead of adding a paragraph. | US-2 | TC-US-2-01 | In scope |
| FR-002 | Panel is a non-modal region directly below the unchanged toolbar. | US-2 | TC-US-2-01 | In scope |
| FR-003 | Two-column catalog has exactly the eleven approved localized tiles in order. | US-2 | TC-US-2-02 | In scope |
| FR-004 | Page is absent and cannot create or navigate to a child note. | US-2 | TC-US-2-02 | In scope |
| FR-005 | Tiles have labels, descriptions, tags, and 48 dp targets. | US-3 | TC-US-3-01 | In scope |
| FR-006 | A selection inserts immediately after the focused block. | US-2 | TC-US-2-03 | In scope |
| FR-007 | A selection appends when no body block is focused. | US-2 | TC-US-2-04 | In scope |
| FR-008 | Inserted block focuses at zero selection, saves, and collapses the panel. | US-2 | TC-US-2-03 | In scope |
| FR-009 | Each text, heading, list, and to-do type has its specified default. | US-1 | TC-US-1-01 | In scope |
| FR-010 | Toggle is expanded by default and preserves exposed state. | US-1 | TC-US-1-03 | In scope |
| FR-011 | Callout and Quote retain type after auto-save and reload. | US-1 | TC-US-1-04 | In scope |
| FR-012 | Second plus tap or Android Back collapses the panel without mutation. | US-3 | TC-US-3-04 | In scope |
| FR-013 | Read-only plus is visible, disabled, and cannot mutate the note. | US-3 | TC-US-3-05 | In scope |
| FR-014 | Panel and controls use the approved design-system tokens and semantics. | US-3 | TC-US-3-07 | In scope |
| FR-015 | Toolbar remains 56 dp; panel cap is min(280 dp, 40% usable height). | US-3 | TC-US-3-02 | In scope |
| FR-016 | Grid has 48 dp baseline tiles, 8 dp spacing, and scrolls all actions. | US-3 | TC-US-3-03 | In scope |
| FR-017 | Font scaling and device configurations scroll rather than clip. | US-3 | TC-US-3-06 | In scope |
| FR-018 | Existing documents load, edit, export, and persist without data loss. | US-1 | TC-US-1-02 | In scope |
| FR-019 | Panel has no typing, search, or filtering control. | US-2 | TC-US-2-05 | In scope |
| AC-001 | Plus expands an inline panel with no overlay or scrim. | US-2 | TC-US-2-01 | In scope |
| AC-002 | Grid contains the exact eleven labels, final full-width Quote, and no Page. | US-2 | TC-US-2-02 | In scope |
| AC-003 | Accessibility traversal exposes each tile's localized action semantics. | US-3 | TC-US-3-01 | In scope |
| AC-004 | Standard viewport meets toolbar, panel-cap, and tile-baseline geometry. | US-3 | TC-US-3-02 | In scope |
| AC-005 | Scrolling reaches every tile from Text through Quote. | US-3 | TC-US-3-03 | In scope |
| AC-006 | Heading 2 selection after focused text, image, table, or voice inserts and focuses correctly. | US-2 | TC-US-2-03 | In scope |
| AC-007 | Text selection with no focus appends and collapses. | US-2 | TC-US-2-04 | In scope |
| AC-008 | Selecting each type creates the visible expected type and initial state. | US-2 | TC-US-2-06 | In scope |
| AC-009 | Toggle expansion action exposes state and survives save/reload. | US-1 | TC-US-1-03 | In scope |
| AC-010 | Block order, type, text, to-do, and toggle state survive auto-save/reopen. | US-1 | TC-US-1-04 | In scope |
| AC-011 | Second plus or Back collapses the open panel without insertion. | US-3 | TC-US-3-04 | In scope |
| AC-012 | Read-only trigger is visible/disabled and cannot open or mutate. | US-3 | TC-US-3-05 | In scope |
| AC-013 | Larger fonts, narrow phones, landscape, and tablets remain reachable. | US-3 | TC-US-3-06 | In scope |
| AC-014 | Existing documents retain content and behavior after load, edit, save, export, reload. | US-1 | TC-US-1-02 | In scope |
| Edge: no focused body block | Append at document end and focus the chosen new block. | US-2 | TC-US-2-04 | In scope |
| Edge: focused non-text block | Insert after focused image, table, or voice without replacement. | US-2 | TC-US-2-03 | In scope |
| Edge: empty new note | Retain editor's empty paragraph when focused; otherwise append the selected block. | US-2 | TC-US-2-04 | In scope |
| Edge: panel toggle | Plus opens/closes without document mutation. | US-3 | TC-US-3-04 | In scope |
| Edge: Android Back while open | Consume Back to close panel before normal editor navigation. | US-3 | TC-US-3-04 | In scope |
| Edge: compact viewport | Retain 56 dp toolbar, cap panel, and scroll grid. | US-3 | TC-US-3-02 | In scope |
| Edge: read-only note | Expose disabled trigger and reject any selection mutation. | US-3 | TC-US-3-05 | In scope |
| Edge: rapid tile taps | Commit only the first accepted selection while panel collapses. | US-2 | TC-US-2-05 | In scope |
| Edge: unknown stored block type | Preserve readable children through a safe compatibility fallback. | US-1 | TC-US-1-02 | In scope |
| Edge: toggle state | Preserve expanded/collapsed state through auto-save and reload. | US-1 | TC-US-1-03 | In scope |
| Edge: small viewport / large text | Grow only as needed and scroll rather than clip or hide a tile. | US-3 | TC-US-3-06 | In scope |
| NFR: dependency and persistence boundary | Reuse existing dependencies and Note.content; no Room/API/permission change. | US-1 | TC-US-1-02 | In scope |
| NFR: strings and testability | Localize all new copy and attach stable tags to interactive elements. | US-3 | TC-US-3-01 | In scope |
| NFR: no IME from selector | Do not add a search or text-input control. | US-2 | TC-US-2-05 | In scope |
| Design: attached flat panel | Panel follows toolbar with divider and never becomes a sheet or overlay. | US-2 | TC-US-2-01 | In scope |
| Design: catalog arrangement | Two equal columns, approved order, Quote spans final row. | US-2 | TC-US-2-02 | In scope |
| Design: compact density | 16 dp horizontal/8 dp vertical panel padding, 8 dp grid gap, 48 dp baseline tile. | US-3 | TC-US-3-02 | In scope |
| Design: scroll and responsive behavior | LazyVerticalGrid exposes Quote in all supported viewports. | US-3 | TC-US-3-03 | In scope |
| Design: visual approval asset | Top and scrolled production states are compared to mockup_basic_blocks_panel_compact.png. | US-3 | TC-US-3-VIS-01 | In scope |
| Verification: JVM and integration | Model, ViewModel, export, and repository-backed persistence tests run. | US-1 | TC-US-1-04 | In scope |
| Verification: runtime UI | Production editor tests prove catalog, insertion, semantics, Back, and read-only behavior. | US-2 | TC-US-2-06 | In scope |
| Verification: visual states | State-verified screenshots and reference-anchor report are required before US-3 passes. | US-3 | TC-US-3-VIS-02 | In scope |

## User Scenarios & Testing

## Acceptance Test Cases

The matrices below are the implementation authorization contract. Every test invokes a production mapper, ViewModel, exporter, or stateless production NoteEditorScreenContent entry point; visual rows assert the target UI state before capturing it.

### US-1: Persist and render basic document block types (Priority: P1)

Given a note containing any supported basic block, the editor can load, edit, save, reload, and export it without degrading existing note content.

**Why this priority:** The current model has only generic heading, bullet, and checkbox types. Compatibility and stable persistence are the highest-risk foundation for all later selection UI.

**Independent Test:** JVM tests exercise the production JSON mapper, ViewModel mutation paths, and exporter; a repository-backed integration test proves the existing auto-save/reload path.

**Acceptance Criteria:**

1. Given every approved block type, when its default model is created and encoded, then its storage value and default state are deterministic.
2. Given legacy and unknown stored text blocks, when they decode and re-encode, then readable child content remains available with a safe fallback.
3. Given an expanded Toggle list, when it is collapsed and saved, then its expanded semantics survive reload.
4. Given a saved note containing new types, when it is reopened and exported, then order, type, text, to-do state, and type-specific visual/export treatment remain intact.

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-1-01 | US-1 AC 1; FR-009 | JVM unit | app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentTest.kt#basicBlockTypesRoundTripWithDefaults and app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelTest.kt#basicBlockFactoryCreatesExpectedDefaults | Create every supported basic type through production factory/mapper. | Stable IDs/types; empty text; H1-H4 distinct; numbered/bulleted markers; unchecked to-do; expanded toggle; callout/quote preserved. | ./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.mapper.NoteDocumentTest' --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest' |
| TC-US-1-02 | US-1 AC 2; AC-014; FR-018 | JVM unit | app/src/test/java/com/example/notesapp/ui/editor/mapper/NoteDocumentTest.kt#legacyAndUnknownBlocksKeepReadableContent and app/src/test/java/com/example/notesapp/util/NoteExporterTest.kt#legacyDocumentExportsAfterBasicBlockExtension | Decode legacy generic heading, existing blocks, and unknown text-like type; edit/export/reload. | Generic heading renders as Heading 1; known legacy content unchanged; unknown children remain readable; Markdown/PDF path does not drop existing content. | ./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.mapper.NoteDocumentTest' --tests 'com.example.notesapp.util.NoteExporterTest' |
| TC-US-1-03 | US-1 AC 3; AC-009; FR-010 | JVM unit | app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelTest.kt#toggleExpandedStatePersistsAcrossDocumentRoundTrip | Create an empty expanded toggle, invoke the production expanded-state command, serialize, and decode. | Expanded state is exposed before/after mutation and restored exactly after JSON reload. | ./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest' |
| TC-US-1-04 | US-1 AC 4; AC-010; FR-011 | JVM integration | app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelIntegrationTest.kt#basicBlockAutoSaveAndReloadPreservesDocument | Use the existing local repository/mock-server fixture to load a note, mutate types through the ViewModel, advance auto-save, and reopen. | Saved Note.content and reloaded state preserve block order, text, to-do, Toggle state, Callout, and Quote. | ./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelIntegrationTest' |

### US-2: Insert basic blocks from the inline catalog (Priority: P2)

Given an editable Note Editor, the person opens the attached Basic blocks panel and selects one of the approved actions to insert an empty block at the intended position.

**Why this priority:** This is the primary requested interaction and makes the documented block model reachable without a modal surface.

**Independent Test:** An Android-runtime Compose test uses the production ViewModel and NoteEditorScreenContent, taps the toolbar and catalog, and asserts rendered state and document mutations.

**Acceptance Criteria:**

1. Given a closed editable editor, when plus is tapped, then a flat inline panel opens after the toolbar with no overlay.
2. Given the panel is open, when it is inspected, then exactly the approved labels/order are present and Page is absent.
3. Given a focused text, image, table, or voice block, when a tile is selected, then the new empty block is immediately after it, focused at zero selection, saved, and the panel closes.
4. Given no focused block or an empty new note, when a tile is selected, then the correct append behavior occurs.
5. Given the catalog receives rapid taps, when a selection is accepted, then only one insertion occurs and no input/search control exists.
6. Given each catalog tile is selected, then its visible model/default matches the approved type.

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-2-01 | US-2 AC 1; AC-001; FR-001; FR-002 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksTriggerOpensInlinePanelWithoutOverlay | Render production editor, tap editor_basic_blocks_trigger. | basic_blocks_panel appears immediately after editor_default_bottom_bar; no modal/scrim tag or semantics; existing body remains in layout. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-2-02 | US-2 AC 2; AC-002; FR-003; FR-004 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksPanelListsExactTilesAndExcludesPage | Open panel at top scroll position. | Exact 11 localized tile tags/labels in reading order; Quote has full-width span; Page has no node or action. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-2-03 | US-2 AC 3; AC-006; FR-006; FR-008 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksSelectionInsertsAfterFocusedBlockAndCollapses | Set focused text, image, table, then voice fixture; select Heading 2. | One empty heading_2 immediately follows each focused block; focusedBlockId and selection are zero; panel disappears; auto-save is scheduled. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-2-04 | US-2 AC 4; AC-007; FR-007 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksSelectionAppendsWhenNoBlockIsFocused | Clear focusedBlockId for populated and initial-empty document fixtures; select Text. | Exactly one empty paragraph is last, is focused, and panel collapses. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-2-05 | US-2 AC 5; FR-019 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksPanelHasNoInputAndCommitsRapidSelectionOnce | Open panel, inspect semantics, then perform rapid tile clicks. | No editable/search/filter semantics; first tile commits one block; later taps cannot duplicate it while collapsing. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-2-06 | US-2 AC 6; AC-008 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksTilesCreateAllApprovedDefaults | Reopen panel and select each tile through the production callback. | Each type renders its correct hierarchy/marker/state, including unchecked to-do and empty expanded Toggle. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |

### US-3: Complete the compact, accessible basic-blocks experience (Priority: P3)

Given the completed inline insertion flow, users can navigate, scroll, dismiss, and view it safely across supported editor states and device configurations.

**Why this priority:** It makes the final flow accessible and shippable, and is the sole owner of visual verification because all completed user-visible states are reachable only after US-1 and US-2.

**Independent Test:** The production editor content is rendered with a real ViewModel on an emulator; tests measure tagged nodes, perform touch/Back/scroll actions, assert semantics and document safety, and capture state-verified screenshots.

**Acceptance Criteria:**

1. Given the panel is open, when accessibility traverses it, then all controls have localized action semantics, stable tags, and 48 dp targets.
2. Given a standard viewport, when it opens, then the 56 dp toolbar, capped panel, and 48 dp tile baseline meet their bounds contract.
3. Given hidden rows exist, when the grid scrolls, then Quote and all remaining actions are reachable without changing panel height.
4. Given an open panel, when plus is tapped again or Android Back is pressed, then it closes without inserting or navigating.
5. Given a read-only note, when the toolbar renders or is tapped, then the Basic blocks trigger is visible, disabled, and cannot mutate or open the panel.
6. Given large font, narrow, landscape, or tablet constraints, when the panel opens, then labels remain reachable without clipping.
7. Given light and dark theme, when the panel renders, then it uses LocalAppColors and approved Material 3 typography/surface behavior.

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-3-01 | US-3 AC 1; AC-003; FR-005 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksPanelExposesAccessibleLabeledTilesAndTargetBounds | Open production panel and inspect every tile/trigger tag and semantics. | Localized label/action, button role, expanded/collapsed or disabled state, and minimum 48 by 48 dp bounds are present. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-3-02 | US-3 AC 2; AC-004; FR-015 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksPanelMatchesCompactGeometry | Open panel in standard emulator viewport and measure tagged toolbar/divider/panel/tile nodes. | Toolbar is 56 dp; panel starts after divider, is at most min(280 dp, 40% usable height), and default tile height is 48 dp within tolerance. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-3-03 | US-3 AC 3; AC-005; FR-016 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksGridScrollsToQuoteWithoutExpandingPanel | Record panel height, scroll basic_blocks_grid to Quote. | Quote is displayed/full width, all tile tags become reachable, and panel height remains capped. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-3-04 | US-3 AC 4; AC-011; FR-012 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksTriggerAndBackCollapseWithoutMutation | Open panel, close with plus, reopen, then send Android Back. | Panel disappears both times; document count/order unchanged; back callback is not invoked while panel was open. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-3-05 | US-3 AC 5; AC-012; FR-013 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#readOnlyBasicBlocksTriggerIsVisibleDisabledAndSafe | Render read-only note and attempt trigger action. | editor_basic_blocks_trigger exists with disabled semantics/description; panel never appears; document is unchanged. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-3-06 | US-3 AC 6; AC-013; FR-017 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksPanelSupportsLargeFontAndConstrainedViewport | Render increased font scale and constrained dimensions, then scroll grid. | Labels are not clipped, tiles remain at least 48 dp, and Quote is reachable on each fixture. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-3-07 | US-3 AC 7; FR-014 | Instrumented UI | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#basicBlocksPanelRendersInLightAndDarkThemes | Render the production panel under both existing themes. | Tagged panel, title, tiles, and disabled state display under semantic theme colors without raw-color-only behavior. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest |
| TC-US-3-VIS-01 | Compact top visual state | Visual verification | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#captureBasicBlocksPanelTopState | Assert toolbar/panel/tile tags and compact top position, capture device image, then pull it to visual_evidence/basic_blocks_panel_top.png. | Verified open top state and non-empty workspace screenshot for comparison to design/mockup_basic_blocks_panel_compact.png. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest#captureBasicBlocksPanelTopState && mkdir -p "$FEATURE_DIR/visual_evidence" && adb -s emulator-5554 pull /sdcard/Download/notesapp_basic_blocks_panel_top.png "$FEATURE_DIR/visual_evidence/basic_blocks_panel_top.png" && test -s "$FEATURE_DIR/visual_evidence/basic_blocks_panel_top.png" |
| TC-US-3-VIS-02 | Compact scrolled visual state | Visual verification | app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt#captureBasicBlocksPanelScrolledState | Assert Quote is reached after scrolling, capture device image, then pull it to visual_evidence/basic_blocks_panel_scrolled.png. | Verified scrolled state and non-empty workspace screenshot prove catalog reachability against the approved compact mockup. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest#captureBasicBlocksPanelScrolledState && mkdir -p "$FEATURE_DIR/visual_evidence" && adb -s emulator-5554 pull /sdcard/Download/notesapp_basic_blocks_panel_scrolled.png "$FEATURE_DIR/visual_evidence/basic_blocks_panel_scrolled.png" && test -s "$FEATURE_DIR/visual_evidence/basic_blocks_panel_scrolled.png" |

## Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| Planning | Planner | Sprint contract compiled | Three vertical slices; US-1 resolves compatibility risk first and US-3 owns all visual evidence. |
| Implementation | Generator | Pending user approval | No application code has been written. |
| Review 1 | Evaluator | Pending | |
| Final Review | Evaluator | Pending | |
