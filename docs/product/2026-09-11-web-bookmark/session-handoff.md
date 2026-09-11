# Session Handoff

## Verified Now

- What is currently working:
  - Basic Blocks → Advanced exposes a localized, tagged **Web Bookmark** tile that opens a dedicated
    note-scoped Add Web Bookmark destination without mutating the document.
  - The destination accepts only a URL, pins Save/Cancel to the bottom safe area, shows inline
    validation, resolves HTTPS metadata on Save, falls back to host/blank, and restores the URL
    draft across Activity recreation.
  - Saving inserts exactly one `EditorBlock.WebBookmarkBlock` after the focused block (append when
    nothing is focused), autosaves through the existing repository, and renders the bookmark card in
    the originating editor.
- What verification actually ran:
  - 12/12 US-1 acceptance commands exit 0 — logs in `evidence/US-1/TC-US-1-01.log` … `TC-US-1-12.log`.
  - `./gradlew testDebugUnitTest` → 539 tests / 0 failures (includes 32 new bookmark domain,
    integration, and ViewModel tests).
  - Instrumented regression set (editor screen, basic blocks, editor actions sheet, navigation
    contract) → 34/34 passed on `emulator-5554`.
  - `check-journey-registry.sh --run-all` → 6/6 journeys including the new `J-WEB-BOOKMARK-ADD`.
  - `check-full-source-rules.sh`, `ktlintCheck`, `detekt`, `lintDebug`, `assembleDebug` → exit 0;
    kover project-owned line coverage 82.03%.

## Changed This Session

- Code or behavior added:
  - Domain: `WebBookmarkMetadata`, `WebBookmarkMetadataParser`, `WebBookmarkMetadataSource`,
    `WebBookmarkUrlValidator` (bounded, credential-free `http`/`https` validation).
  - Data: `HttpsWebBookmarkMetadataSource` (isolated unauthenticated HTTPS client, SSL-redirect
    downgrades disabled, 10 s timeouts, 256 KiB response bound) and `WebBookmarkModule`.
  - UI: `WebBookmarkEditorScreen` (URL-only full page), `WebBookmarkBlockCard`,
    `WebBookmarkEditorViewModel`, `WebBookmarkActions`.
  - Integration: `BasicBlockType.WEB_BOOKMARK`, `EditorBlock.WebBookmarkBlock` JSON mapping,
    Advanced-panel tile, editor insert/append, `Destinations.WebBookmarkEditor`,
    `AppNavigationHost` route, Markdown/PDF export text.
- Infrastructure or harness changes:
  - `HiltTestRunner` + debug-only `HiltTestActivity` and the Hilt test dependencies, so
    instrumented tests can inject fakes into the production graph. Verified behavior-preserving:
    the 5 pre-existing critical journeys and 34 editor/navigation tests pass.
  - `NoteActionsSheetHost` extracted into `NoteActionsSheetSection.kt` to keep
    `NoteEditorScreenContent` under the detekt method-length ceiling.
  - `docs/harness/documented-dynamic-test-tags.json` extended for the bookmark card's dynamic tags.

## Broken Or Unverified

- Known defect: none observed in the verified US-1 scope.
- Unverified path: HTTP bookmarks are stored/exported/opened but never fetch metadata (intentional,
  cleartext stays disabled); no device-level browser launch exists yet.
- Risk for the next session: the `testInstrumentationRunner` is now project-owned. Any new suite
  that relies on the production `Application` class must be checked against `HiltTestRunner`.
- `Stage 7` commit `cb7375e` (`feat(editor): add metadata-enriched web bookmark block with URL
  validation`) contains the whole US-1 slice, its tests, evidence, and state docs. It was created
  with explicit user approval.
- Risk for the next session: the `.harness` submodule is still dirty — its working tree carries an
  uncommitted existing-screen-baseline gate (`harness/scripts/check-existing-screen-baseline-contract.sh`,
  `ci-checks.md`, planning/design templates) on top of `9286bd5`, while this repo's committed pointer
  remains `1078686`. Commit inside the harness repo before relying on that gate in CI.

## Next Best Step

- Highest-priority unfinished feature: `US-2` — Complete bookmark management, export, and visual
  verification.
- Why it is next: it is the only remaining slice; it owns the real Android browser boundary
  (`TC-US-2-02`/`TC-US-2-03`) and the feature's visual verification.
- What counts as passing: every US-2 acceptance command in
  `feature_list.json#features[id=US-2].verification` exits 0, including the real
  `ACTION_VIEW`/`PackageManager` boundary test on an emulator, plus
  `check-platform-evidence.sh "docs/product/2026-09-11-web-bookmark" --evaluate` and
  `check-visual-evidence-contract.sh "docs/product/2026-09-11-web-bookmark"`.
- What must not change during that step: the US-1 bookmark model/JSON shape, the URL validation and
  metadata fallback contract, the advanced-panel tile tag `basic_blocks_web_bookmark`, and the
  already-loaded guard in `NoteEditorScreen`.

## Commands

- Startup: `./gradlew installDebug`
- Verification: `./gradlew testDebugUnitTest` and
  `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=<class>#<method>`
- Focused debug command:
  `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#opensAddBookmarkPageAndReturnsWithSavedCard`
