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
| API 24 (minSdk) | CodeBlock JSON persistence and syntax highlighting | Supported behavior (minSdk baseline, JVM-verifiable) | `TC-US-1-01` `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.mapper.NoteDocumentTest.testCodeBlockSerializationAndDeserialization"`; `TC-US-2-01` `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.components.CodeSyntaxHighlighterTest.testSyntaxHighlightingForSupportedLanguages"` | Local JVM / Android Unit Test Runner | Passing (exit 0) |
| API 24+ (system clipboard) | Clipboard copying (real instrumented boundary) | Supported behavior | `TC-US-2-06` `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.CodeBlockCardTest#testCopyCodeToClipboard` | Medium Phone AVD / Pixel Emulator (`emulator-5554`) | Passing (exit 0) |
| API 33/34 (targetSdk runtime) | Full visual flow, Compose rendering, language selection, read-only behavior, block deletion, PDF export | Supported behavior | `TC-US-3-01`..`TC-US-3-03` `CodeBlockScreenTest`; `TC-US-3-VIS-01`..`TC-US-3-VIS-02` `CodeBlockVisualFlowTest`; `NoteExporterTest#testExportToPdfWithCodeBlock` — all via `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest` | Medium Phone AVD / Pixel Emulator (`emulator-5554`) | Passing (exit 0) |

## Real Platform Boundary Test

- Required: **No** — Code Block editing, syntax highlighting, and the Basic Blocks panel operate entirely on-device within the standard Android framework (Jetpack Compose, Room, Android ClipboardManager) and do not depend on external hardware, OEM voice engines, or AI models.
- Test IDs: `N/A` (not required). For completeness, the clipboard copy boundary is still exercised by the real instrumented `TC-US-2-06` (`CodeBlockCardTest#testCopyCodeToClipboard`), and PDF export by `NoteExporterTest#testExportToPdfWithCodeBlock`.
- Instrumented test file(s): `app/src/androidTest/java/com/example/notesapp/ui/editor/components/CodeBlockCardTest.kt`, `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/CodeBlockScreenTest.kt`, `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/CodeBlockVisualFlowTest.kt`, `app/src/androidTest/java/com/example/notesapp/util/NoteExporterTest.kt`
- Real-platform signal: System clipboard content and a back-rendered PDF page.
- Exact command(s): see Runtime Matrix rows above.
- Fixture/data source: Deterministic local fixtures in unit and instrumented tests.
- Assertion: Deterministic UI state, clipboard content, rendered PDF content, and Room persistence.

## Unsupported Environment Policy

The evaluator must fail loudly when a required emulator, device, model, locale, permission, hardware capability, or platform service is unavailable. The test must return a non-zero result or the feature must be marked `Blocked`/`Revise`; it must not be converted into a passing result through a skip, warning, or missing-evidence note.

- Policy: `fail_loudly`
- Missing environment result: Non-zero exit code / `Blocked` / `Revise`
- Explicit fallback for a genuinely unsupported API: N/A (Standard Android APIs from minSdk 24+ are fully supported)
- Evidence owner: Generator / Evaluator Agent
