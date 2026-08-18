# Platform Capability Matrix — Note Editor Mermaid Chart & Preview

## Classification

This feature uses the standard Android System `WebView` to execute bundled offline JavaScript (`mermaid.min.js`) locally on-device. It introduces no external network permissions, hardware sensors, proprietary OS services, or cloud APIs. The rendering pipeline executes completely within the local application sandbox using `file:///android_asset/`.

Minimum API: 24. Target API: 34.

feature_list.json declares platform_validation.required as false for this reason. Standard emulator-based instrumented UI tests and local integration tests are used to verify Android runtime behavior, touch gestures, WebView SVG rendering, and Compose layouts.

## Scope

- Feature/slice: US-1 through US-4, Note Editor Mermaid Chart & Preview.
- Platform boundary: Local `WebView` asset execution and standard Compose touch/zoom gestures.
- Minimum API: 24.
- Target API: 34.
- Single resource owner: `MermaidRenderer` owns local WebView JS execution; `NoteEditorViewModel` owns document persistence and auto-save.
- Input/output contract: User Mermaid code string is evaluated locally via offline JavaScript; output SVG string or error payload is returned to Compose UI; document JSON is persisted to local Room database.

## Runtime Matrix

| Runtime/API | Capability under test | Required behavior | Test ID / exact command | Environment evidence | Status |
|---|---|---|---|---|---|
| API 24 | Basic blocks panel insertion & document schema | "Mermaid Diagram" tile inserts starter block; JSON schema serializes and reloads. | TC-US-1-01; ./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.mapper.NoteDocumentTest' | Planned JVM unit test | Planned |
| API 24 | Local offline Mermaid rendering engine | Bundled JS executes offline in WebView and outputs SVG with theme tokens. | TC-US-2-01; ./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.components.MermaidRendererTest' | Planned JVM / AndroidView test | Planned |
| API 24 | Compose card mode toggle & pinch zoom | Card switches between Preview and Code mode; touch gestures zoom diagram. | TC-US-3-01; env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.components.MermaidBlockCardTest | Planned Medium_Phone emulator, Android runtime | Planned |
| API 34 | Fullscreen viewer & visual flow capture | Fullscreen canvas pans/zooms; visual evidence captured against design. | TC-US-4-01, TC-US-4-VIS-01; env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.FullscreenDiagramViewerTest | Planned Medium_Phone emulator, Android runtime | Planned |

## Real Platform Boundary Test

- Required: No. The feature does not introduce a new platform-bound hardware adapter, device permission, or cloud API.
- Test IDs: N/A.
- Instrumented test file(s): `app/src/androidTest/java/com/example/notesapp/ui/editor/components/MermaidBlockCardTest.kt` and `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/FullscreenDiagramViewerTest.kt` are required for runtime UI behavior.
- Real-platform signal: N/A; local asset execution is validated via production instrumented UI tests.
- Exact command(s): The connectedDebugAndroidTest commands in the Runtime Matrix.
- Fixture/data source: Deterministic local document fixtures and starter templates; no live backend.
- Assertion: Production diagram card rendering, SVG output, mode switching, zoom bounds, and screenshot captures.

## Unsupported Environment Policy

The policy is fail_loudly. A missing emulator, Android runtime, or screenshot-capable environment causes connectedDebugAndroidTest to fail non-zero, or the owning slice must be marked Blocked or Revise. It is never recorded as a skipped or passing result.

- Policy: fail_loudly.
- Missing environment result: Non-zero connectedDebugAndroidTest command; otherwise Blocked or Revise.
- Explicit fallback for a genuinely unsupported API: minSdk 24 is the supported baseline.
- Evidence owner: Generator records test execution outputs and screenshots; Evaluator validates evidence contract.
