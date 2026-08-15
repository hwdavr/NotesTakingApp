# Sprint Contract — Note Emoji

## 🏃 Sprint Overview

- **Sprint:** `P08-15-note-emoji`
- **Feature:** Note Emoji
- **Duration:** One implementation sprint, delivered in three vertical slices.

---

## 🎯 Scope

### In Scope

- [ ] Activate the existing Note Editor Insert emoticon toolbar action for editable notes and retain an accessible disabled state for read-only notes.
- [ ] Insert standard Unicode emoji only into body text at the exact cursor/selection, creating a focused paragraph when no body block is active.
- [ ] Provide an on-device, app-bundled catalog with the nine approved categories, name/keyword search, skin-tone selection, and non-blocking empty/fallback states.
- [ ] Persist the exact selected Unicode string as device-local Recent data across app restarts.
- [ ] Preserve selected emoji through existing note persistence, sync/share, and exports; prove Android glyph behavior on a real runtime and capture the completed visual states.

### Out of Scope

- Custom emoji packs (deferred product work).
- GIFs or stickers (deferred rich-media work).
- Emoji reactions (deferred collaboration work).
- Note-cover/icon emoji and title insertion (explicitly excluded from this feature).
- A backend API, Room schema, DTO, permission, or third-party emoji SDK change.

## Platform Capability & Environment Contract

See [platform-capability-matrix.md](platform-capability-matrix.md). This feature is platform-bound because it relies on the real Android text/font renderer, Compose text selection, and DataStore behavior. The policy is `fail_loudly`; unavailable emulator/API/runtime evidence blocks acceptance rather than becoming a skip.

## Spec Coverage Matrix

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001 | Existing editor emoticon control opens picker for editable notes | US-1 | TC-US-1-01 | In scope |
| FR-002 | Read-only control stays visible but disabled | US-1 | TC-US-1-02 | In scope |
| FR-003 | Local name/keyword search, approved categories, Recent | US-2 | TC-US-2-01 | In scope |
| FR-004 | Exact skin-tone variant selection | US-2 | TC-US-2-03 | In scope |
| FR-005 | Insert/replace at focused body cursor only | US-1 | TC-US-1-03 | In scope |
| FR-006 | No focused block appends/focuses a paragraph before insert | US-1 | TC-US-1-04 | In scope |
| FR-007 | Sheet stays open and cursor advances | US-1 | TC-US-1-03 | In scope |
| FR-008 | Exact selected Unicode persists in Recent | US-3 | TC-US-3-01 | In scope |
| FR-009 | Unicode survives document save/reload/sync/share/export | US-1 | TC-US-1-05 | In scope |
| AC-001 | Editable control opens picker with Recent selected and no navigation | US-1 | TC-US-1-01 | In scope |
| AC-002 | Read-only visible disabled control cannot open picker | US-1 | TC-US-1-02 | In scope |
| AC-003 | Approved category results and empty Recent state | US-2 | TC-US-2-01 | In scope |
| AC-004 | Name/keyword search and clearable no-result state | US-2 | TC-US-2-02 | In scope |
| AC-005 | Exact skin-tone variant inserts and appears in Recent | US-2 | TC-US-2-03 | In scope; US-3 provides durable restart proof. |
| AC-006 | Cursor/selection insertion and picker remains open | US-1 | TC-US-1-03 | In scope |
| AC-007 | Unfocused insertion creates paragraph and leaves title unchanged | US-1 | TC-US-1-04 | In scope |
| AC-008 | Recent survives app restart | US-3 | TC-US-3-01 | In scope |
| AC-009 | Saved/reloaded/synced/shared/exported Unicode remains unchanged | US-1 | TC-US-1-05 | In scope |
| Edge case: no focused body block | Append/focus paragraph before insert | US-1 | TC-US-1-04 | In scope |
| Edge case: selected cursor range | Replace selected range with Unicode sequence | US-1 | TC-US-1-03 | In scope |
| Edge case: empty Recent | Localized explanatory state; browse/search remains usable | US-2 | TC-US-2-01 | In scope |
| Edge case: no search match | Localized no-result state with clear action | US-2 | TC-US-2-02 | In scope |
| Edge case: Recents read failure | Empty Recent fallback; catalog/insertion remains available | US-3 | TC-US-3-01 | In scope |
| Edge case: device font lacks glyph | Preserve original Unicode and fail platform capability evidence loudly | US-3 | TC-US-3-REAL-UNICODE | In scope |
| Edge case: configuration/process recreation | Restore presentation state and reload persisted Recent | US-3 | TC-US-3-01 | In scope |
| NFR: architecture | UI emits events; ViewModel/use cases/repositories retain layer boundaries | US-1 | TC-US-1-03 | In scope |
| NFR: no new API/schema/permission | Reuse document content and existing save flows | US-1 | TC-US-1-05 | In scope |
| NFR: platform API 24 / target 34 | Android text font and picker path tested on real runtime | US-3 | TC-US-3-REAL-UNICODE | In scope |
| NFR: accessibility | 48dp targets, localized descriptions, semantics, RTL/font scaling support | US-3 | TC-US-3-VIS-002 | In scope |
| Design: standard M3 sheet | Existing editor visual tokens, sheet, toolbar, and no new navigation | US-1 | TC-US-1-01 | In scope |
| Design: searchable category grid / skin tone | Search, tabs, 48dp grid, compact selector, selected semantics | US-2 | TC-US-2-01 | In scope |
| Design: final visible states | Content, disabled, empty search screenshots match approved design | US-3 | TC-US-3-VIS-001 | In scope |

## User Scenarios & Testing

### US-1: Insert emoji from the existing toolbar (Priority: P1)

An editor opens the existing emoji control, inserts ordinary Unicode into a focused body TextBlock or a new paragraph, and saves through the same note document path already used for normal text.

**Why this priority**: It establishes the highest-risk behavior—correct cursor/selection mutation without title, schema, or repository boundary violations—and leaves a useful, shippable editor path.

**Independent Test**: An editable note opens the picker, inserts an emoji at a cursor/selection or into a new paragraph, and persists/reloads the same Unicode with the picker still open. A read-only note visibly disables the trigger.

**Acceptance Criteria**:

1. **AC-US-1-01 Given** an editable loaded note, **When** the user taps the existing Insert emoticon control, **Then** the picker opens with Recent selected and no navigation occurs.
2. **AC-US-1-02 Given** a read-only note, **When** the editor renders, **Then** the same control is visible, disabled, and cannot open the picker.
3. **AC-US-1-03 Given** a focused body TextBlock with a cursor or selected range, **When** an emoji is chosen, **Then** it is inserted/replaces the range at that point, the cursor advances, and the picker remains open.
4. **AC-US-1-04 Given** no focused editable body block, **When** an emoji is chosen, **Then** a focused new paragraph receives it and the title is unchanged.
5. **AC-US-1-05 Given** picker-inserted Unicode emoji, **When** the note saves, reloads, flows through existing sync/share mapping, and exports, **Then** the original sequence remains unchanged.

#### Acceptance Test Cases

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-1-01 | AC-US-1-01 | Instrumented Compose UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorEmojiPickerTest.kt#editableToolbarOpensPickerWithRecentSelected` | Render production Note Editor content with an editable loaded state; tap `editor_insert_emoji`. | Sheet/tag exists; Recent is selected; editor route/content remains visible; no title mutation. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorEmojiPickerTest#editableToolbarOpensPickerWithRecentSelected` |
| TC-US-1-02 | AC-US-1-02 | Instrumented Compose UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorEmojiPickerTest.kt#readOnlyToolbarIsDisabledAndDoesNotOpenPicker` | Render production Note Editor content for a read-only shared note; attempt the existing toolbar action. | `editor_insert_emoji` is visible, disabled in semantics, exposes its localized state description, and no sheet appears. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorEmojiPickerTest#readOnlyToolbarIsDisabledAndDoesNotOpenPicker` |
| TC-US-1-03 | AC-US-1-03 | JVM ViewModel + instrumented Compose UI | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelEmojiTest.kt#insertsUnicodeAtCursorAndReplacesSelection; app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorEmojiPickerTest.kt#selectionInsertsEmojiAndKeepsPickerOpen` | Seed a focused TextBlock with cursor and selected-range fixtures; invoke the production picker event. | Exact document text and selection offsets; selection replacement; autosave request; sheet remains open; title unchanged. | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelEmojiTest"` |
| TC-US-1-04 | AC-US-1-04 | JVM ViewModel | `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelEmojiTest.kt#insertsIntoNewFocusedParagraphWhenNoBodyBlockIsFocused` | Seed no focused TextBlock and invoke the production picker event. | One appended paragraph is focused and contains the Unicode emoji; title value is identical. | `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelEmojiTest"` |
| TC-US-1-05 | AC-US-1-05 | JVM integration | `app/src/test/java/com/example/notesapp/editor/NoteEmojiPersistenceIntegrationTest.kt#unicodeEmojiSurvivesSaveReloadSyncShareAndExport` | Save a note containing default and skin-tone Unicode through production document/repository/export paths with deterministic local fakes. | JSON round-trip, local reload, sync/share payload mapping, plain text/Markdown/PDF export source all retain exact sequences; no schema/API change. | `./gradlew testDebugUnitTest --tests "com.example.notesapp.editor.NoteEmojiPersistenceIntegrationTest"` |

### US-2: Browse, search, and choose skin-tone variants (Priority: P2)

An editor discovers emoji through every approved category, finds them by local terms, and selects a default or exact skin-tone form through the same insertion path.

**Why this priority**: This provides the requested discovery experience once US-1 proves text mutation is safe, without binding the user to a remote service or third-party catalog.

**Independent Test**: The picker returns representative category items and keyword matches, reports empty states safely, and routes default/variant choices to the US-1 insertion event while staying open.

**Acceptance Criteria**:

1. **AC-US-2-01 Given** the picker is open, **When** each approved category is selected, **Then** its results are shown and empty Recent is clear but non-blocking.
2. **AC-US-2-02 Given** the picker is open, **When** the user searches a name or keyword, **Then** matches appear; no match has a clearable “No emoji found” state.
3. **AC-US-2-03 Given** an eligible emoji, **When** the user chooses a skin-tone variant, **Then** the exact Unicode variant is inserted and handed to Recent tracking.

#### Acceptance Test Cases

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-2-01 | AC-US-2-01, AC-003 | JVM domain/mapper + Instrumented Compose UI | `app/src/test/java/com/example/notesapp/domain/emoji/FindEmojiCatalogUseCaseTest.kt#returnsEveryApprovedCategory; app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorEmojiPickerTest.kt#categoryRailShowsApprovedResultsAndEmptyRecent` | Query every required category and render the production picker with no stored Recent item. | All nine category IDs resolve representative results; Recent shows localized empty state; category rail/grid semantics remain usable. | `./gradlew testDebugUnitTest --tests "com.example.notesapp.domain.emoji.FindEmojiCatalogUseCaseTest"` |
| TC-US-2-02 | AC-US-2-02, AC-004 | JVM domain/mapper + Instrumented Compose UI | `app/src/test/java/com/example/notesapp/domain/emoji/FindEmojiCatalogUseCaseTest.kt#matchesEmojiNamesAndKeywords; app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorEmojiPickerTest.kt#searchShowsMatchesAndClearableEmptyState` | Search known display name/common keyword and unmatched phrase via production picker state. | Correct result IDs for each query; unmatched query displays localized no-result text and clear action restores category view. | `./gradlew testDebugUnitTest --tests "com.example.notesapp.domain.emoji.FindEmojiCatalogUseCaseTest"` |
| TC-US-2-03 | AC-US-2-03, AC-005 | JVM domain/mapper + Instrumented Compose UI | `app/src/test/java/com/example/notesapp/ui/editor/mapper/EmojiPickerUiMapperTest.kt#mapsExactSkinToneVariants; app/src/androidTest/java/com/example/notesapp/ui/editor/screen/NoteEditorEmojiPickerTest.kt#skinToneChoiceInsertsExactVariantAndKeepsSheetOpen` | Long-press an eligible production picker cell and choose a named variant. | Selector has Default plus five modifiers; inserted text is exact Unicode sequence; picker remains open; the exact string is emitted to Recent tracking. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.NoteEditorEmojiPickerTest#skinToneChoiceInsertsExactVariantAndKeepsSheetOpen` |

### US-3: Remember selected emoji and validate the completed picker (Priority: P3)

An editor sees exact previously selected emoji first in Recent after a restart, while the completed picker is validated on a real Android font/runtime and against the approved visual design.

**Why this priority**: Durable recents and platform/visual proof make the picker complete and trustworthy after core insertion/discovery behavior is independently working.

**Independent Test**: Select a default and skin-tone emoji, recreate the repository/app state, reopen the production picker, and observe the exact ordered strings in Recent. Run the real Android font test and capture each final visual state.

**Acceptance Criteria**:

1. **AC-US-3-01 Given** an emoji or selected skin-tone variant was inserted, **When** the Recent repository/app state is recreated, **Then** that exact sequence appears first in Recent and a read failure falls back to empty Recent.
2. **AC-US-3-02 Given** a configured Android runtime, **When** the shipped picker/text path validates catalog sequences, **Then** Android `Paint.hasGlyph` produces the required real font capability result; unavailable runtime fails loudly.
3. **AC-US-3-03 Given** completed picker states, **When** production UI tests navigate to content, read-only, and empty-search states, **Then** each state is asserted and its non-empty screenshot is captured for design comparison.

#### Acceptance Test Cases

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-3-01 | AC-US-3-01, AC-008 | JVM DataStore integration | `app/src/test/java/com/example/notesapp/data/emoji/DataStoreRecentEmojiRepositoryTest.kt#persistsExactUnicodeMruAcrossRepositoryRecreation` | Store default and skin-tone values, recreate the production repository, and simulate a recoverable preferences read failure. | Exact strings and MRU order survive recreation; read failure exposes empty Recent without preventing catalog/insertion events. | `./gradlew testDebugUnitTest --tests "com.example.notesapp.data.emoji.DataStoreRecentEmojiRepositoryTest"` |
| TC-US-3-REAL-UNICODE | AC-US-3-02, NFR: platform API 24 / target 34 | Instrumented Android platform boundary | `app/src/androidTest/java/com/example/notesapp/editor/EmojiPickerPlatformTest.kt#unicodeEmojiHasGlyphOnAndroidRuntime` | On the configured emulator, select catalog default and skin-tone sequences from the shipped picker/text rendering path, then call Android `Paint.hasGlyph` on each exact sequence. | Production picker emits the expected exact Unicode strings; Android device font reports glyph support; missing runtime/device makes the command fail rather than skip. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.EmojiPickerPlatformTest#unicodeEmojiHasGlyphOnAndroidRuntime` |
| TC-US-3-VIS-001 | AC-US-3-03, Design: final visible states | Visual verification | `app/src/androidTest/java/com/example/notesapp/editor/EmojiPickerVisualFlowTest.kt#emojiPickerContentLightTheme` | Assert the production editor picker has title, search, selected Recent, category rail, grid, and skin-tone affordance; test exports device screenshot. | Target-state assertions pass; non-empty content-state capture is saved and later compared to `design.md`/mockup. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.EmojiPickerVisualFlowTest#emojiPickerContentLightTheme && mkdir -p "docs/product/2026-08-15-note-emoji/visual_evidence" && adb -s emulator-5554 pull /sdcard/Download/notesapp_emoji_picker_content_light.png "docs/product/2026-08-15-note-emoji/visual_evidence/emoji_picker_content_light.png" && test -s "docs/product/2026-08-15-note-emoji/visual_evidence/emoji_picker_content_light.png"` |
| TC-US-3-VIS-002 | AC-US-3-03, FR-002, Design: disabled state | Visual verification | `app/src/androidTest/java/com/example/notesapp/editor/EmojiPickerVisualFlowTest.kt#readOnlyEmojiControlLightTheme` | Assert a production read-only editor exposes visible disabled emoji control; test exports device screenshot. | Target-state assertions pass; non-empty disabled-state capture is saved and later compared to `design.md`/mockup. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.EmojiPickerVisualFlowTest#readOnlyEmojiControlLightTheme && mkdir -p "docs/product/2026-08-15-note-emoji/visual_evidence" && adb -s emulator-5554 pull /sdcard/Download/notesapp_emoji_read_only_light.png "docs/product/2026-08-15-note-emoji/visual_evidence/emoji_read_only_light.png" && test -s "docs/product/2026-08-15-note-emoji/visual_evidence/emoji_read_only_light.png"` |
| TC-US-3-VIS-003 | AC-US-3-03, AC-004, Design: empty search | Visual verification | `app/src/androidTest/java/com/example/notesapp/editor/EmojiPickerVisualFlowTest.kt#emptySearchEmojiPickerLightTheme` | Assert the production picker query has no results and exposes localized copy plus clear search; test exports device screenshot. | Target-state assertions pass; non-empty empty-search capture is saved and later compared to `design.md`/mockup. | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.EmojiPickerVisualFlowTest#emptySearchEmojiPickerLightTheme && mkdir -p "docs/product/2026-08-15-note-emoji/visual_evidence" && adb -s emulator-5554 pull /sdcard/Download/notesapp_emoji_empty_search_light.png "docs/product/2026-08-15-note-emoji/visual_evidence/emoji_empty_search_light.png" && test -s "docs/product/2026-08-15-note-emoji/visual_evidence/emoji_empty_search_light.png"` |

## Delivery Order And Authorization Gate

1. Implement and prove US-1 only; it is the highest-risk cursor/document vertical slice.
2. Implement and prove US-2 only after US-1 passes.
3. Implement and prove US-3 only after US-2 passes; US-3 is the sole visual-verification owner and runs the real Android boundary test.

No production code may be written until the user approves this contract, `feature_list.json`, and `platform-capability-matrix.md`. After approval, implementation proceeds through the harness-generator workflow one user story at a time.
