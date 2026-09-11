# Sprint Contract — Web Bookmark in Notes

## 🏃 Sprint Overview

*   **Sprint:** `P06-01`
*   **Feature:** `Web Bookmark in Notes`
*   **Duration:** `1 sprint`

## 🎯 Scope

### In Scope

*   [ ] Add a standalone persisted Web Bookmark block from Basic Blocks → Advanced.
*   [ ] Provide the URL-only Add/Edit full page with submit-time page metadata enrichment and bottom-pinned Save/Cancel actions.
*   [ ] Render the metadata-derived bookmark card with external browser opening and a single More control.
*   [ ] Provide the existing app-style actions bottom sheet for Edit/Delete, confirmation-gated deletion, and read-only protection.
*   [ ] Preserve bookmark order/identity, autosave/reload behavior, safe fallback handling, and plain-text/Markdown/PDF export.
*   [ ] Verify the complete production flow and approved visuals on a real Android emulator, including IME, browser, dark theme, RTL, large text, narrow phone, and tablet-sensitive behavior where supported.

### Out of Scope

*   *   Page body scraping or rendering, WebView browsing, favicons, thumbnails, preview images, and remote asset storage (deferred; explicitly excluded by the approved spec).
*   *   Backend/OpenAPI changes, bookmark-specific tables, collections, tags, deduplication, reminders, visit history, and deep links (deferred or excluded by the approved spec).
*   *   HTTP metadata retrieval while cleartext traffic remains disabled. HTTP bookmarks are still stored, exported, and opened in the external browser.

### Dependency Order And Vertical Slice Check

1. **US-1** is the riskiest/foundational end-to-end slice: model/serialization, metadata boundary, full-page route, insertion, and autosave. After it ships alone, a user can reach the existing Note Editor, choose Web Bookmark, save a URL, and observe the resulting card.
2. **US-2** depends on the card and route produced by US-1 and completes the feature: card actions, Edit/Delete, browser opening, export/compatibility, and final visual/runtime verification. After it ships on top of US-1, the complete bookmark workflow is usable and evidenced.

## Platform Capability & Environment Contract

The feature is platform-bound. The capability matrix is [platform-capability-matrix.md](platform-capability-matrix.md). The root `feature_list.json` declares `platform_validation.required: true`, `unsupported_environment_policy: fail_loudly`, and the owning real Android boundary tests.

Missing emulator, browser handler, permission, API runtime, or other required platform capability is evidence failure. The evaluator must return non-zero or mark the relevant slice `Blocked`/`Revise`; it must not convert an unavailable runtime into a pass.

## Rule Applicability Contract

| Rule ID | Rule document | Decision | Slice evidence |
|---|---|---|---|
| ARCH | `android-architecture.md` | Required | US-1 separates domain URL/metadata contracts, data parsing, platform browser launch, ViewModel state, and Compose UI; US-2 completes the remaining boundaries and review evidence. |
| IMPL | `implementation-rules.md` | Required | Both slices must implement real validation, metadata fallback, persistence, navigation, actions, export, browser, and error behavior without stubs or no-op production callbacks. |
| TEST | `testing-strategy.md` | Required | JVM unit/integration tests, production-entry instrumented journeys, real Android browser boundary evidence, and final visual-flow screenshots are assigned below. |
| SUI | `compose-rules.md` | Required | US-1 defines the URL-only full page and bottom-pinned actions; US-2 defines the card/actions sheet, final visual states, tags, semantics, and responsive proof. |
| L10N | `localization-rules.md` | Required | All tile, page, sheet, validation, metadata, browser, confirmation, export, and accessibility copy is localized and asserted in UI tests. |
| NAV | `navigation-rules.md` | Required | US-1 and US-2 own production editor → destination/sheet → return journeys; `WebBookmarkJourneyTest` is the named journey owner for both slices. |
| API | `api-contract-rules.md` | Not applicable — no application API endpoint, DTO, schema, or OpenAPI change; page metadata is a direct unauthenticated user-URL fetch. | Every acceptance row declares `N/A — no API`; `sharedContracts/openapi.yaml` remains unchanged. |
| OBS | `observability.md` | Required | Metadata, browser, export, and compatibility outcomes/errors are structured without URLs, page bodies, note content, credentials, or PII; failure paths are tested. |
| ANL | `analytics-rules.md` | Not applicable — analytics: none | No product-approved analytics event is introduced by this local note utility. |
| SEC | `android-security.md` | Required | URL/HTML/redirect/intent inputs are untrusted; HTTPS metadata client is isolated and bounded; URI schemes, credentials, response content, and browser resolution are guarded and tested. |

## Generated Context Index

At the start of each slice, run:

```bash
bash harness/scripts/print-context-index.sh --feature-dir "$FEATURE_DIR" --slice "$FEATURE_ID"
```

This output is disposable execution context derived from the approved contract and feature list; it is not a second source of truth.

## Production Journey Planning Contract

| User story | Journey required | Journey reason | Journey acceptance test ID | Production entry point | Planned test file and method | User actions | Return boundary | Post-return assertion |
|---|---|---|---|---|---|---|---|---|
| US-1 | Yes | This slice crosses the production editor to Add Web Bookmark destination and returns with a persisted card visible in the editor. | `TC-US-1-02` | `AppNavigationHost` | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#opensAddBookmarkPageAndReturnsWithSavedCard` | Launch the production navigation host, open the note editor, open Basic Blocks, scroll to Advanced, tap Web Bookmark, enter a valid URL, tap Save bookmark, and return to the note editor. | The Add Web Bookmark destination pops back to the originating note editor after successful save. | The saved Web Bookmark card and its metadata-derived title are visible in the originating note editor. |
| US-2 | Yes | This slice crosses the bookmark card action sheet into the full-page Edit destination and returns to the same editor card. | `TC-US-2-05` | `AppNavigationHost` | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#opensEditFromActionsAndReturnsWithSameBookmark` | Launch the production navigation host, open a note containing a bookmark, tap the card More control, tap Edit bookmark, change or inspect the URL, save, and return to the note editor. | The Edit Web Bookmark destination pops back to the originating note editor after the same block is updated. | The original bookmark card remains at the same position with the same stable identity and updated visible content. |

## Spec Coverage Matrix

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001 | Advanced panel exposes Web Bookmark and opens the Add full page without document mutation | US-1 | TC-US-1-02 | In scope |
| FR-002 | Full page accepts only bounded absolute HTTP(S) URL and has no title/description inputs | US-1 | TC-US-1-03 | In scope |
| FR-003 | Unsafe or invalid URL shows localized error and causes no request or mutation | US-1 | TC-US-1-04 | In scope |
| FR-004 | HTTPS metadata is best effort and HTTP/failure uses host/blank fallback without blocking save | US-1 | TC-US-1-08 | In scope |
| FR-005 | Valid bookmark inserts stable block after focus or appends and autosaves | US-1 | TC-US-1-09 | In scope |
| FR-006 | Card renders title/description/URL and opens validated URL externally | US-2 | TC-US-2-02 | In scope |
| FR-007 | Edit uses same full page/block identity and cancel/back leaves the document unchanged | US-2 | TC-US-2-05 | In scope |
| FR-008 | Delete is confirmation-gated, removes one block, preserves empty-document invariant, and autosaves | US-2 | TC-US-2-08 | In scope |
| FR-009 | Read-only cards hide mutation controls while valid opening remains available | US-2 | TC-US-2-10 | In scope |
| FR-010 | Plain text, Markdown, and PDF retain bookmark context and URL without network fetch | US-2 | TC-US-2-11 | In scope |
| FR-011 | Legacy/unknown/malformed JSON remains readable, safe, and non-crashing | US-2 | TC-US-2-12 | In scope |
| FR-012 | Page, sheet, and card retain specified state/accessibility across configuration, IME, scale, theme, RTL, and layouts | US-2 | TC-US-2-13 | In scope |
| AC-001 | Localized Advanced Web Bookmark tile has stable tag and 48dp target | US-1 | TC-US-1-01 | In scope |
| AC-002 | Tile opens Add full page without document mutation | US-1 | TC-US-1-02 | In scope |
| AC-003 | URL-only page has bottom-safe-area Save/Cancel and reachable back | US-1 | TC-US-1-03 | In scope |
| AC-004 | Invalid URL keeps inline error and does not fetch or mutate | US-1 | TC-US-1-04 | In scope |
| AC-005 | Valid HTTP(S) URL enables submission | US-1 | TC-US-1-05 | In scope |
| AC-006 | HTTPS HTML metadata resolves title/description and saves bookmark | US-1 | TC-US-1-06 | In scope |
| AC-007 | Partial/missing metadata independently uses host/blank fallback | US-1 | TC-US-1-07 | In scope |
| AC-008 | HTTP and metadata failures save with deterministic fallback | US-1 | TC-US-1-08 | In scope |
| AC-009 | Focused insertion places exactly one card after focused block and returns | US-1 | TC-US-1-09 | In scope |
| AC-010 | No-focus insertion appends and multiple bookmarks coexist | US-1 | TC-US-1-10 | In scope |
| AC-011 | Autosave/close/reopen round-trips URL, metadata, order, and stable ID | US-1 | TC-US-1-11 | In scope |
| AC-012 | Card exposes metadata, themed surfaces, semantics, and stable tags | US-2 | TC-US-2-01 | In scope |
| AC-013 | Available browser receives validated ACTION_VIEW URI | US-2 | TC-US-2-02 | In scope |
| AC-014 | Missing browser handler produces recoverable error without mutation | US-2 | TC-US-2-03 | In scope |
| AC-015 | Actions → Edit → unchanged URL preserves same ID and position | US-2 | TC-US-2-05 | In scope |
| AC-016 | Changed URL refreshes metadata and updates same block in place | US-2 | TC-US-2-06 | In scope |
| AC-017 | Cancel/back from Edit preserves existing bookmark | US-2 | TC-US-2-07 | In scope |
| AC-018 | Actions sheet exposes Edit/Delete and Delete requires confirmation | US-2 | TC-US-2-08 | In scope |
| AC-019 | Deleting final bookmark leaves valid editable empty text block | US-2 | TC-US-2-09 | In scope |
| AC-020 | Read-only note hides mutation controls while Open remains | US-2 | TC-US-2-10 | In scope |
| AC-021 | All three export formats retain context/URL and perform no fetch | US-2 | TC-US-2-11 | In scope |
| AC-022 | Legacy/malformed content remains safe and readable | US-2 | TC-US-2-12 | In scope |
| AC-023 | Full page retains draft/actions through IME and recreation | US-1 | TC-US-1-12 | In scope |
| AC-024 | Theme, RTL, font-scale, narrow, and tablet layouts remain readable and unclipped | US-2 | TC-US-2-13 | In scope |
| Edge case: unsafe URL | Inline error, no metadata request, no mutation | US-1 | TC-US-1-04 | In scope |
| Edge case: HTTP URL | Store/open/export succeeds and metadata request is skipped | US-1 | TC-US-1-08 | In scope |
| Edge case: HTTPS/network/HTML failure | Save with host/blank fallback | US-1 | TC-US-1-08 | In scope |
| Edge case: unsafe metadata text | Sanitize, bound, and render as plain text only | US-1 | TC-US-1-07 | In scope |
| Edge case: missing browser handler | Recoverable localized feedback and unchanged card | US-2 | TC-US-2-03 | In scope |
| Edge case: delete during metadata request | Cancel/ignore stale request and avoid partial persistence | US-2 | TC-US-2-08 | In scope |
| Edge case: duplicate URL | Allow multiple bookmark blocks without deduplication | US-1 | TC-US-1-10 | In scope |
| Edge case: final bookmark deletion | Preserve valid empty text-block invariant | US-2 | TC-US-2-09 | In scope |
| Edge case: recreation/IME | Restore URL draft and full-page mode safely | US-1 | TC-US-1-12 | In scope |
| Edge case: legacy/malformed content | Non-crashing readable safe fallback | US-2 | TC-US-2-12 | In scope |
| NFR: architecture boundaries | Domain/data/platform/UI responsibilities remain separated | US-1 | TC-US-1-06 | In scope |
| NFR: security/privacy | Bounded isolated fetch and validated external intent never expose credentials/content | US-2 | TC-US-2-02 | In scope |
| NFR: localization | All static and accessibility copy uses resources | US-2 | TC-US-2-13 | In scope |
| NFR: observability | Async metadata/browser outcomes are structured without sensitive values | US-2 | TC-US-2-03 | In scope |
| NFR: no API contract | No OpenAPI/shared scenario change | US-2 | TC-US-2-11 | In scope |
| NFR: platform evidence | Real emulator/browser boundary is required and fail-loudly | US-2 | TC-US-2-02 | In scope |
| Design: URL-only full page | Add/Edit screen has no title/description input | US-1 | TC-US-1-03 | In scope |
| Design: bottom-pinned page actions | Save/Cancel align to the bottom safe area and move above IME | US-1 | TC-US-1-03 | In scope |
| Design: existing actions sheet | Bookmark options uses EditorNoteActionsSheet/SheetActionRow treatment | US-2 | TC-US-2-08 | In scope |
| Design: card More control | Card has one action control, not inline Edit/Delete buttons | US-2 | TC-US-2-01 | In scope |
| Design: source-fed baseline | Updated editor surface is derived from the emulator-captured baseline | US-2 | TC-US-2-VIS-01 | In scope |
| Design: keyboard state | URL field and bottom actions remain reachable above IME | US-2 | TC-US-2-VIS-03 | In scope |
| Design: accessibility/responsive | Tags, bounds, semantics, theme, RTL, scale, and layout states are verified | US-2 | TC-US-2-13 | In scope |

## User Scenarios & Testing

### US-1: Add a metadata-enriched web bookmark (Priority: P1)

An editable note author opens the existing Basic Blocks → Advanced panel, selects Web Bookmark, enters a valid web address on the dedicated full page, and saves. The app fetches safe page metadata when eligible, persists the resolved bookmark, and returns to the editor with the card visible. Invalid input stays on the page; fetch failures never block valid local saving.

**Why this priority**: It creates the core user value and establishes the model, metadata boundary, persistence, and navigation path required by the later management and export slices.

**Independent Test**: Launch the existing production editor entry, complete Add mode with a deterministic HTTPS fixture, and verify the returned card and persisted document. JVM parser/integration tests cover deterministic metadata and fallback outcomes.

**Acceptance Criterion**:

1. **AC-US-1-01 Given** an editable production note with Basic Blocks → Advanced open, **When** the user inspects the catalog, **Then** the localized Web Bookmark tile has the stable tag and minimum target.
2. **AC-US-1-02 Given** the Advanced panel, **When** the user selects Web Bookmark and saves a valid URL, **Then** the full page opens without premature document mutation and the saved card is visible after return.
3. **AC-US-1-03 Given** the Add/Edit page, **When** it is displayed, **Then** only the URL field is present and bottom-safe-area Save/Cancel plus back are reachable.
4. **AC-US-1-04 Given** an unsafe URL, **When** the user submits, **Then** inline validation remains visible and neither metadata retrieval nor document mutation occurs.
5. **AC-US-1-05 Given** a valid HTTP or HTTPS URL, **When** it is entered, **Then** Save becomes available.
6. **AC-US-1-06 Given** an HTTPS fixture with bounded HTML metadata, **When** Save is submitted, **Then** title and description resolve from metadata and the bookmark is saved.
7. **AC-US-1-07 Given** partial or missing metadata, **When** Save is submitted, **Then** each missing field independently uses host/blank fallback and no manual metadata fields are requested.
8. **AC-US-1-08 Given** HTTP, timeout, offline, non-HTML, non-2xx, oversized, or malformed metadata, **When** Save is submitted, **Then** the valid bookmark saves with host/blank fallback.
9. **AC-US-1-09 Given** a focused block, **When** a bookmark is saved, **Then** one card appears immediately after it and the full page returns.
10. **AC-US-1-10 Given** no focused block, **When** a bookmark is saved, **Then** it appends and multiple bookmark blocks are allowed.
11. **AC-US-1-11 Given** a saved bookmark, **When** the note is closed and reopened, **Then** URL, metadata, order, and stable ID round-trip.
12. **AC-US-1-12 Given** the full page has a focused URL field, **When** the IME appears or the Activity recreates, **Then** the URL draft and bottom actions remain reachable.

### US-2: Complete bookmark management, export, and visual verification (Priority: P2)

An editable note author taps the bookmark card’s More control to open the existing app-style bottom sheet. Edit leads to the URL-only full page and preserves block identity; Delete leads to confirmation. Opening a valid URL uses the device browser, while read-only notes expose only Open. The completed slice also preserves bookmark context in plain-text, Markdown, and PDF exports, safely handles legacy or malformed content, and verifies the full flow against source-fed visual references across approved configuration, IME, theme, RTL, scale, and layout states.

**Why this priority**: It makes the persisted card maintainable and completes the security-sensitive browser boundary, export/compatibility behavior, and final visual proof after the core save path exists.

**Independent Test**: Start from a deterministic persisted bookmark in the production editor, exercise More → Edit/Delete/Open and read-only rendering, verify block identity, confirmation, browser resolution, and no-handler recovery, then run network-disabled export/compatibility checks and the dedicated source-fed visual-flow captures.

**Acceptance Criterion**:

1. **AC-US-2-01 Given** a saved editable bookmark card, **When** it renders, **Then** metadata, URL, theme, semantics, and stable tags are visible.
2. **AC-US-2-02 Given** a valid bookmark and available browser handler, **When** Open is activated, **Then** Android receives a validated `ACTION_VIEW` URI and the note remains intact.
3. **AC-US-2-03 Given** a valid bookmark and no browser handler, **When** Open is activated, **Then** localized recoverable feedback appears without mutation or crash.
4. **AC-US-2-04 Given** an editable bookmark, **When** More then Edit is selected, **Then** the full page opens in Edit mode with the current URL.
5. **AC-US-2-05 Given** Edit mode with an unchanged URL, **When** the user saves, **Then** the same block ID and position remain and the user returns to the editor.
6. **AC-US-2-06 Given** Edit mode with a changed URL, **When** the user saves, **Then** new metadata is attempted and the same block updates in place.
7. **AC-US-2-07 Given** Edit mode, **When** the user cancels or backs out, **Then** no URL or metadata mutation persists.
8. **AC-US-2-08 Given** an editable bookmark, **When** More and Delete are selected, **Then** the existing-style actions sheet is used, confirmation appears, and only confirmation deletes/autosaves.
9. **AC-US-2-09 Given** the bookmark is the only block, **When** deletion is confirmed, **Then** a valid empty editable text block remains.
10. **AC-US-2-10 Given** a read-only bookmark note, **When** it renders, **Then** mutation controls are absent and Open remains available.
11. **AC-US-2-11 Given** a note with bookmarks and a network-disabled export fixture, **When** plain text, Markdown, and PDF are generated, **Then** each contains bookmark context and URL and no network request occurs.
12. **AC-US-2-12 Given** legacy note JSON or malformed bookmark JSON, **When** it is loaded and saved, **Then** existing meaning remains intact, loading does not crash, and unsafe data degrades to readable safe fallback.
13. **AC-US-2-13 Given** the completed bookmark flow on supported Android configurations, **When** it renders in dark theme, RTL, large font scale, narrow phone, and tablet-sensitive layouts, **Then** text, controls, semantics, and approved page/card/sheet placement remain readable and unclipped.

## Acceptance Test Cases

Every row has one primary AC, a concrete planned Kotlin method, a suite-scoped command, and the shared scenario declaration required by the harness. No application API contract applies, so every row uses `N/A — no API`.

| Test ID | Covers AC | Test layer | Test file and method | Shared scenario(s) | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|---|
| TC-US-1-01 | AC-001 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkBasicBlocksTest.kt#advancedPanelShowsWebBookmarkTile` | N/A — no API | Launch the production editor and open Basic Blocks → Advanced. | Web Bookmark is localized, tagged `basic_blocks_web_bookmark`, clickable, and at least 48dp. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkBasicBlocksTest#advancedPanelShowsWebBookmarkTile |
| TC-US-1-02 | AC-002 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#opensAddBookmarkPageAndReturnsWithSavedCard` | N/A — no API | Through `AppNavigationHost`, open the editor, open Advanced, tap Web Bookmark, enter a deterministic valid URL, save, and return. | Add page is reached without early mutation; return shows the saved card and derived title. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#opensAddBookmarkPageAndReturnsWithSavedCard |
| TC-US-1-03 | AC-003 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkEditorScreenTest.kt#showsUrlOnlyEditorWithBottomActions` | N/A — no API | Render Add and Edit production destinations at idle state. | Only URL input exists; title/description inputs are absent; bottom actions and back are visible and tagged. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkEditorScreenTest#showsUrlOnlyEditorWithBottomActions |
| TC-US-1-04 | AC-004 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkEditorScreenTest.kt#invalidUrlShowsInlineErrorWithoutSaving` | N/A — no API | Enter blank, malformed, unsupported-scheme, credential-bearing, hostless, and oversized values and submit. | Inline localized error remains; metadata source is not called and document/repository state is unchanged. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkEditorScreenTest#invalidUrlShowsInlineErrorWithoutSaving |
| TC-US-1-05 | AC-005 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkEditorScreenTest.kt#enablesSaveForHttpAndHttpsUrl` | N/A — no API | Enter valid HTTP and HTTPS URLs in Add mode. | Validation passes and Save is enabled for both schemes. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkEditorScreenTest#enablesSaveForHttpAndHttpsUrl |
| TC-US-1-06 | AC-006 | JVM integration | `app/src/test/java/com/example/notesapp/integration/WebBookmarkMetadataIntegrationTest.kt#savesMetadataFromHttpsHtml` | N/A — no API | Serve bounded HTML from MockWebServer with og:title, og:description, and fallback tags; submit through the production ViewModel/data source. | HTTPS request is isolated and bounded; sanitized metadata is stored and exposed in resulting block state. | ./gradlew testDebugUnitTest --tests "com.example.notesapp.integration.WebBookmarkMetadataIntegrationTest" |
| TC-US-1-07 | AC-007 | JVM integration | `app/src/test/java/com/example/notesapp/integration/WebBookmarkMetadataIntegrationTest.kt#usesHostAndBlankFallbackForPartialMetadata` | N/A — no API | Return partial, whitespace-only, markup-bearing, and missing metadata candidates. | Valid candidates are sanitized/bounded; missing title resolves to host and missing description to blank independently. | ./gradlew testDebugUnitTest --tests "com.example.notesapp.integration.WebBookmarkMetadataIntegrationTest" |
| TC-US-1-08 | AC-008 | JVM integration | `app/src/test/java/com/example/notesapp/integration/WebBookmarkMetadataIntegrationTest.kt#savesFallbackForMetadataFailures` | N/A — no API | Exercise HTTP, timeout, offline/error, non-HTML, non-2xx, oversized, malformed, and unsupported redirect fixtures. | Valid bookmark saves without blocking; title is host, description blank, and HTTP makes no metadata request. | ./gradlew testDebugUnitTest --tests "com.example.notesapp.integration.WebBookmarkMetadataIntegrationTest" |
| TC-US-1-09 | AC-009 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#insertsBookmarkAfterFocusedBlock` | N/A — no API | From the production editor focus a block, open Add, save a valid URL, and return. | Exactly one card is immediately after the focused block, unrelated blocks are unchanged, and return succeeds. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#insertsBookmarkAfterFocusedBlock |
| TC-US-1-10 | AC-010 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#appendsBookmarkWhenNoBlockIsFocused` | N/A — no API | From the production editor clear focus, add two valid bookmarks, and return after each save. | Each bookmark appends in order and both stable cards coexist. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#appendsBookmarkWhenNoBlockIsFocused |
| TC-US-1-11 | AC-011 | JVM integration | `app/src/test/java/com/example/notesapp/integration/WebBookmarkPersistenceIntegrationTest.kt#roundTripsBookmarkThroughNoteContentStore` | N/A — no API | Persist a note containing a bookmark, close/reload through the repository, and parse content. | URL, resolved metadata, order, and stable ID round-trip; legacy non-bookmark blocks retain meaning. | ./gradlew testDebugUnitTest --tests "com.example.notesapp.integration.WebBookmarkPersistenceIntegrationTest" |
| TC-US-1-12 | AC-023 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#retainsUrlDraftAcrossRecreationAndKeyboard` | N/A — no API | Open the production Add page, focus URL, show IME, type draft, recreate Activity, and inspect the page. | Full-page route and URL draft remain; Save/Cancel stay above IME and no title/description field appears. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#retainsUrlDraftAcrossRecreationAndKeyboard |
| TC-US-2-01 | AC-012 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkCardTest.kt#rendersMetadataAndActionsWithStableTags` | N/A — no API | Render an editable and read-only note containing a persisted bookmark block. | Card exposes title/description/URL, visual/touch tags, Open, and More only when editable; no inline Edit/Delete controls exist. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkCardTest#rendersMetadataAndActionsWithStableTags |
| TC-US-2-02 | AC-013 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/platform/WebBookmarkPlatformBoundaryTest.kt#opensValidatedUrlWithRealActionViewBrowser` | N/A — no API | On an emulator with a browser handler, render a persisted bookmark and tap Open. | Shipped launcher validates the URI, resolves a real handler, sends `Intent.ACTION_VIEW`, and note state remains unchanged. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.platform.WebBookmarkPlatformBoundaryTest#opensValidatedUrlWithRealActionViewBrowser |
| TC-US-2-03 | AC-014 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/platform/WebBookmarkPlatformBoundaryTest.kt#reportsMissingBrowserHandlerWithoutMutation` | N/A — no API | On an emulator configured with no browser handler, tap Open for a valid bookmark. | No crash; localized recoverable feedback is visible and block/note content is unchanged. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.platform.WebBookmarkPlatformBoundaryTest#reportsMissingBrowserHandlerWithoutMutation |
| TC-US-2-04 | AC-015 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkCardActionsTest.kt#opensEditWithExistingUrl` | N/A — no API | Tap More then Edit on an editable persisted card. | Actions sheet dismisses; Edit page is shown with the same URL and stable target ID. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkCardActionsTest#opensEditWithExistingUrl |
| TC-US-2-05 | AC-015 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#opensEditFromActionsAndReturnsWithSameBookmark` | N/A — no API | Through `AppNavigationHost`, open More, Edit, leave the URL unchanged, save, and return. | The same ID and position remain and the card is visible after the Edit destination returns. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#opensEditFromActionsAndReturnsWithSameBookmark |
| TC-US-2-06 | AC-016 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#changesUrlAndRefreshesMetadataInPlace` | N/A — no API | From Edit mode change URL to a deterministic HTTPS fixture and save. | Metadata refresh is attempted; same block ID/position remains with new URL and resolved metadata. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#changesUrlAndRefreshesMetadataInPlace |
| TC-US-2-07 | AC-017 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/navigation/WebBookmarkJourneyTest.kt#backsOutOfEditWithoutMutation` | N/A — no API | Open Edit from the production card, change URL, then press back/cancel. | Editor returns with original URL, metadata, ID, and position unchanged. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#backsOutOfEditWithoutMutation |
| TC-US-2-08 | AC-018 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkCardActionsTest.kt#showsEditDeleteSheetAndRequiresDeleteConfirmation` | N/A — no API | Tap More, inspect existing-style sheet, select Delete, cancel once, then confirm. | Plain unboxed rows, divider, standard handle/spacing, localized Edit/Delete; cancel preserves card and confirm removes/autosaves it. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkCardActionsTest#showsEditDeleteSheetAndRequiresDeleteConfirmation |
| TC-US-2-09 | AC-019 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkCardActionsTest.kt#deletingLastBookmarkLeavesEditableTextBlock` | N/A — no API | Delete the only bookmark in an editable note and confirm. | Bookmark disappears; valid editable empty text block remains and autosaves. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkCardActionsTest#deletingLastBookmarkLeavesEditableTextBlock |
| TC-US-2-10 | AC-020 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkCardTest.kt#hidesMutationControlsInReadOnlyNote` | N/A — no API | Render read-only note with persisted bookmark and attempt mutation gestures. | More/Edit/Delete are absent or disabled; Open remains available and callbacks cannot mutate state. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkCardTest#hidesMutationControlsInReadOnlyNote |
| TC-US-2-11 | AC-021 | JVM integration | `app/src/test/java/com/example/notesapp/integration/WebBookmarkExportIntegrationTest.kt#exportsBookmarkContextWithoutNetworkFetch` | N/A — no API | Create a note with metadata-derived bookmark and export with network client spy disabled. | Plain text, Markdown, and PDF contain title/context and URL; export makes zero metadata calls. | ./gradlew testDebugUnitTest --tests "com.example.notesapp.integration.WebBookmarkExportIntegrationTest" |
| TC-US-2-12 | AC-022 | JVM integration | `app/src/test/java/com/example/notesapp/integration/WebBookmarkCompatibilityIntegrationTest.kt#preservesLegacyAndMalformedBookmarkContentSafely` | N/A — no API | Load legacy no-bookmark JSON and malformed/unknown bookmark records, then save. | No crash; existing blocks retain meaning; malformed values produce readable safe fallback and never unsafe URI launch data. | ./gradlew testDebugUnitTest --tests "com.example.notesapp.integration.WebBookmarkCompatibilityIntegrationTest" |
| TC-US-2-13 | AC-024 | Instrumented UI | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkVisualFlowTest.kt#supportsDarkRtlAndLargeTextWithoutClipping` | N/A — no API | Render completed surfaces under dark theme, RTL, large font, narrow phone, and tablet-sensitive constraints. | Text remains readable; test tags/semantics remain present; controls and bottom actions do not clip. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkVisualFlowTest#supportsDarkRtlAndLargeTextWithoutClipping |
| TC-US-2-VIS-01 | AC-024 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkVisualFlowTest.kt#capturesBookmarkCardState` | N/A — no API | Render the completed production editor with a saved bookmark, wait for idle, and capture from the active test window. | Pull non-empty `visual_evidence/web_bookmark_card_content.png` and compare the card/More control to the approved source-fed baseline delta. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkVisualFlowTest#capturesBookmarkCardState && adb -s emulator-5554 pull /sdcard/Download/web_bookmark_card_content.png "$FEATURE_DIR/visual_evidence/web_bookmark_card_content.png" && test -s "$FEATURE_DIR/visual_evidence/web_bookmark_card_content.png" |
| TC-US-2-VIS-02 | AC-024 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkVisualFlowTest.kt#capturesAddBookmarkPageState` | N/A — no API | Render the completed Add Web Bookmark full page at idle and capture after `waitForIdle()`. | Pull non-empty `visual_evidence/web_bookmark_add_page.png` and compare full-page shell, URL-only form, and bottom-pinned actions to the approved mockup. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkVisualFlowTest#capturesAddBookmarkPageState && adb -s emulator-5554 pull /sdcard/Download/web_bookmark_add_page.png "$FEATURE_DIR/visual_evidence/web_bookmark_add_page.png" && test -s "$FEATURE_DIR/visual_evidence/web_bookmark_add_page.png" |
| TC-US-2-VIS-03 | AC-024 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkVisualFlowTest.kt#capturesAddBookmarkPageKeyboardState` | N/A — no API | Render Add page, focus URL, show IME, wait for idle, and capture from the active test window. | Pull non-empty `visual_evidence/web_bookmark_add_page_keyboard.png` and compare focused URL plus bottom actions above the IME. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkVisualFlowTest#capturesAddBookmarkPageKeyboardState && adb -s emulator-5554 pull /sdcard/Download/web_bookmark_add_page_keyboard.png "$FEATURE_DIR/visual_evidence/web_bookmark_add_page_keyboard.png" && test -s "$FEATURE_DIR/visual_evidence/web_bookmark_add_page_keyboard.png" |
| TC-US-2-VIS-04 | AC-024 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkVisualFlowTest.kt#capturesBookmarkActionsSheetState` | N/A — no API | Render the completed editor/card, tap the More control, wait for idle, and capture from the active test window. | Pull non-empty `visual_evidence/web_bookmark_actions_sheet.png` and compare standard existing-style sheet handle/header/divider/unboxed rows and destructive styling. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkVisualFlowTest#capturesBookmarkActionsSheetState && adb -s emulator-5554 pull /sdcard/Download/web_bookmark_actions_sheet.png "$FEATURE_DIR/visual_evidence/web_bookmark_actions_sheet.png" && test -s "$FEATURE_DIR/visual_evidence/web_bookmark_actions_sheet.png" |

| TC-US-2-VIS-05 | AC-024 | Visual verification | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/WebBookmarkVisualFlowTest.kt#supportsDarkRtlAndLargeTextWithoutClipping` | N/A — no API | Render the completed bookmark flow under dark theme, RTL, large font scale, narrow phone, and tablet-sensitive constraints, then capture the responsive proof state. | Pull non-empty `visual_evidence/web_bookmark_responsive.png`; text remains readable, tags/semantics remain present, and controls/bottom actions do not clip. | env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.WebBookmarkVisualFlowTest#supportsDarkRtlAndLargeTextWithoutClipping && adb -s emulator-5554 pull /sdcard/Download/web_bookmark_responsive.png "$FEATURE_DIR/visual_evidence/web_bookmark_responsive.png" && test -s "$FEATURE_DIR/visual_evidence/web_bookmark_responsive.png" |

## Verification Plan

- US-1: run the scoped JVM validation/parser/metadata/persistence suites and the scoped production-entry Add journey tests on the emulator. Confirm no unsafe URL request, bounded fallback, insertion order, reload identity, full-page route return, and IME draft retention.
- US-2: run card/action-sheet/read-only UI tests plus the real Android platform boundary suite, export/compatibility JVM suites, responsive UI test, and all five method-scoped `WebBookmarkVisualFlowTest` captures. Confirm `ACTION_VIEW` resolution and loud no-handler failure, same-ID Edit return, confirmation-gated deletion, autosave, export/compatibility safety, and visual fidelity. Pull each screenshot from `/sdcard/Download/`, create `visual_evidence/reference-anchor-verification.md`, and compare against the approved `design/` mockups. No post-test `adb exec-out screencap` is allowed.
- Before any slice can become `passing`, record its scoped command, exit status 0, result summary, and artifact/screenshot paths in `feature_list.json` evidence and the slice progress log. Run `check-acceptance-test-traceability.sh` in test/evaluation modes after implementation evidence exists.

## Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| **Planning** | Planner | `sprint-contract.md` compiled | Two vertical slices ordered by dependency and risk; US-2 is the sole visual owner and owns the real browser boundary. |
| **Implementation** | Generator | Awaiting implementation approval | No application code has been written. |
| **Review 1** | Evaluator | Pending | |
| **Revision 1** | Generator | Pending | |
| **Final Review** | Evaluator | Pending | |
