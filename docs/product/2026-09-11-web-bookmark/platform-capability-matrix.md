# Platform Capability Matrix

## Scope

- Feature/slice: `Web Bookmark in Notes / US-1..US-2`
- Platform boundary: Android Navigation Compose, IME/window insets, `android.permission.INTERNET`, HTTPS metadata retrieval, and external browser URI resolution
- Minimum API: `minSdk 24`
- Target API: `targetSdk 34`
- Single resource owner: `WebBookmarkMetadataDataSource` owns metadata requests; `WebBookmarkBrowserLauncher` owns URI intent creation and handler resolution; the destination owns IME/inset behavior
- Input/output contract: bounded user URL in; sanitized title/description metadata or host/blank fallback out; validated `ACTION_VIEW` URI intent out; no HTML, credentials, cookies, or remote assets persisted

## Runtime Matrix

Every required runtime boundary must appear as a row. `Unsupported (explicit fallback)` is valid only when the required fallback is implemented and tested. `Pending`, `Unavailable`, `Blocked`, and `Skipped` are never passing evaluation results.

| Runtime/API | Capability under test | Required behavior | Test ID / exact command | Environment evidence | Status |
|---|---|---|---|---|---|
| API 24 | Navigation destination, safe drawing, and IME-aware full-page Add/Edit layout | Full-page destination opens from the production editor, keeps URL draft/actions reachable above the keyboard, and returns without losing state | `TC-US-1-02` / `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.WebBookmarkJourneyTest#opensAddBookmarkPageAndReturnsWithSavedCard` | Android emulator API 24 or an explicitly selected supported API 24-compatible runtime | Planned |
| API 24 | HTTPS metadata request and bounded response handling | Eligible HTTPS pages can return sanitized title/description; timeout, non-HTML, non-2xx, malformed, oversized, and network failures use host/blank fallback; HTTP skips retrieval | `TC-US-1-06`, `TC-US-1-07`, `TC-US-1-08` / `./gradlew testDebugUnitTest --tests "com.example.notesapp.integration.WebBookmarkMetadataIntegrationTest"` | Deterministic MockWebServer fixture plus Android INTERNET permission configuration | Planned |
| API 24 | Android `Intent.ACTION_VIEW` and `PackageManager.resolveActivity` | Valid HTTP(S) bookmark opens through a real browser handler; no-handler resolution reports recoverable feedback without mutation | `TC-US-2-02`, `TC-US-2-03` / `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.platform.WebBookmarkPlatformBoundaryTest` | Emulator with a browser handler for success and an explicit no-handler package-manager fixture for failure | Planned |
| API 34 | Navigation, saved state, IME, and external URI behavior under target SDK | Same supported behavior and fallback contract as API 24; missing runtime capability fails the command or marks the slice Blocked/Revise | `TC-US-1-12`, `TC-US-2-02`, `TC-US-2-03` / `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.platform.WebBookmarkPlatformBoundaryTest` | Android emulator API 34, target SDK 34, explicit device/model/build output | Planned |

## Real Platform Boundary Test

- Required: Yes
- Test IDs: `TC-US-2-02`, `TC-US-2-03`
- Instrumented test file(s): `app/src/androidTest/java/com/example/notesapp/ui/editor/platform/WebBookmarkPlatformBoundaryTest.kt`
- Real-platform signal: Android `Intent.ACTION_VIEW`, `PackageManager.resolveActivity`, and `startActivity`
- Exact command(s): `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.platform.WebBookmarkPlatformBoundaryTest`
- Fixture/data source: Deterministic persisted bookmark fixture and a controlled emulator browser-handler/no-handler configuration; no live backend is required for URI resolution
- Assertion: The shipped Android launcher resolves and sends the validated URI to a real browser handler, and the no-handler path emits a localized recoverable result without changing note state

The metadata path is separately verified with MockWebServer and bounded parser integration tests. The external browser path cannot be satisfied by a fake launcher or JVM-only intent assertion.

## Unsupported Environment Policy

The evaluator must fail loudly when a required emulator, device, model, runtime, locale, permission, hardware capability, or platform service is unavailable. The test must return a non-zero result or the feature must be marked `Blocked`/`Revise`; it must not be converted into a passing result through a skip, warning, or missing-evidence note.

- Policy: `fail_loudly`
- Missing environment result: non-zero connected-test command or `Blocked`/`Revise`
- Explicit fallback for a genuinely unsupported API: HTTP metadata lookup skips network access; failed HTTPS metadata uses host/blank fallback; missing browser handler emits recoverable feedback. Covered by `TC-US-1-08` and `TC-US-2-03`.
- Evidence owner: Generator during slice delivery; Evaluator during final platform review
