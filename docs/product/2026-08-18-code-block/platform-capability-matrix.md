# Platform Capability Matrix

## Scope

- Feature/slice: `code-block`
- Platform boundary: Standard Android framework components (Jetpack Compose, Room persistence, Android `ClipboardManager`)
- Minimum API: `24` (Android 7.0)
- Target API: `34` (Android 14)
- Single resource owner: `NoteEditorViewModel` / `CodeBlockCard`
- Input/output contract: Local document JSON schema in Room database, text editing via Compose `VisualTransformation`, and system clipboard copy via `ClipboardManager`

## Runtime Matrix

Every required runtime boundary must appear as a row. `Unsupported (explicit fallback)` is valid only when the required fallback is implemented and tested. `Pending`, `Unavailable`, `Blocked`, and `Skipped` are never passing evaluation results.

| Runtime/API | Capability under test | Required behavior | Test ID / exact command | Environment evidence | Status |
|---|---|---|---|---|---|
| API 24 (minSdk) | Basic Blocks insertion, CodeBlock JSON persistence, syntax highlighting | Supported behavior (minSdk baseline) | `TC-US-1-01` `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentTest.testCodeBlockSerializationAndDeserialization"` | Local JVM / Android Unit Test Runner | Planned |
| API 33 (Tiramisu) | Clipboard copying, IME text editing, and UI rendering | Supported behavior | `TC-US-3-02` `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelIntegrationTest.testUpdateCodeBlockContent"` | Local JVM / Android Unit Test Runner | Planned |
| API 34 (targetSdk) | Full visual flow, Compose rendering, language selection, block deletion | Supported behavior | `TC-US-4-01` `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.CodeBlockVisualFlowTest#testCodeBlockCardRenderingAndInteraction` | Medium Phone AVD / Pixel Emulator | Planned |

## Real Platform Boundary Test

- Required: No — Code Block editing, syntax highlighting, and basic blocks panel operate entirely on-device within the standard Android framework (Jetpack Compose, Room, Android ClipboardManager) and do not depend on external hardware, OEM voice engines, or AI models.
- Test IDs: `N/A`
- Instrumented test file(s): `N/A`
- Real-platform signal: `N/A`
- Exact command(s): `N/A`
- Fixture/data source: Deterministic local fixtures in unit and instrumented tests.
- Assertion: Deterministic UI state, clipboard content, and Room persistence.

## Unsupported Environment Policy

The evaluator must fail loudly when a required emulator, device, model, locale, permission, hardware capability, or platform service is unavailable. The test must return a non-zero result or the feature must be marked `Blocked`/`Revise`; it must not be converted into a passing result through a skip, warning, or missing-evidence note.

- Policy: `fail_loudly`
- Missing environment result: Non-zero exit code / `Blocked` / `Revise`
- Explicit fallback for a genuinely unsupported API: N/A (Standard Android APIs from minSdk 24+ are fully supported)
- Evidence owner: Generator / Evaluator Agent
