# Platform Capability Matrix — Note Editor Basic Blocks Panel

## Classification

This feature has no special platform-bound capability. It uses the existing Jetpack Compose editor, Android Back dispatch, local JSON document serialization, and existing auto-save path. It introduces no permission, hardware resource, OS service, model, locale service, external API, or new Android adapter.

Minimum API: 24. Target API: 34.

feature_list.json declares platform_validation.required as false for this reason. Emulator-based instrumented UI tests are nevertheless mandatory because touch, scrolling, Back dispatch, semantics, font scale, and Compose layout are Android-runtime behavior.

## Scope

- Feature/slice: US-1 through US-3, Note Editor Basic Blocks Panel.
- Platform boundary: Existing Compose rendering and standard Android Back behavior only.
- Minimum API: 24.
- Target API: 34.
- Single resource owner: NoteEditorScreenContent owns transient panel visibility; NoteEditorViewModel owns document mutation and auto-save.
- Input/output contract: Toolbar tap opens or closes a screen-local panel; tile tap invokes the production ViewModel insertion command and updates the existing Note.content JSON; Android Back closes an open panel before normal editor navigation.

## Runtime Matrix

| Runtime/API | Capability under test | Required behavior | Test ID / exact command | Environment evidence | Status |
|---|---|---|---|---|---|
| API 24 | Compose toolbar tap and inline layout | Plus opens a normal panel below the 56 dp toolbar with no overlay. | TC-US-2-01; env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest | Planned Medium_Phone emulator, Android runtime. | Planned |
| API 24 | Compose lazy-grid scrolling and accessibility semantics | 48 dp actions scroll through Quote and expose labels/roles/tags. | TC-US-3-01 and TC-US-3-03; env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest | Planned Medium_Phone emulator, Android runtime. | Planned |
| API 24 | Android Back dispatch | Inner panel handler consumes Back before normal Note Editor navigation. | TC-US-3-04; env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest | Planned Medium_Phone emulator, Android runtime. | Planned |
| API 24 | Local JSON and existing auto-save | New type/default/toggle data survives the existing document save/reload path. | TC-US-1-04; ./gradlew testDebugUnitTest --tests 'com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelIntegrationTest' | Planned deterministic local DAO and MockWebServer fixture. | Planned |
| API 34 | Light/dark rendering and capture flow | Compact top and scrolled panel states can be asserted and captured on the target emulator. | TC-US-3-VIS-01 and TC-US-3-VIS-02; env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest | Planned Medium_Phone emulator, Android runtime. | Planned |

## Real Platform Boundary Test

- Required: No. The feature does not introduce a new Android platform adapter, device resource, permission, hardware capability, model, locale service, or OS-managed API contract.
- Test IDs: N/A.
- Instrumented test file(s): N/A for a real platform-bound adapter; app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt is still required for normal Android-runtime UI behavior.
- Real-platform signal: N/A; standard Compose editor interactions are covered by production instrumented UI tests.
- Exact command(s): The connectedDebugAndroidTest commands in the Runtime Matrix.
- Fixture/data source: Deterministic local document fixtures and FakeNoteRepository; no live backend.
- Assertion: Production editor panel state, document mutation, Back behavior, semantics, bounds, and screenshots.

## Unsupported Environment Policy

The policy is fail_loudly. A missing emulator, Android runtime, or screenshot-capable device makes the connectedDebugAndroidTest or adb pull command fail non-zero, or the owning slice must be marked Blocked or Revise. It is never recorded as a skipped or passing result.

- Policy: fail_loudly.
- Missing environment result: Non-zero connectedDebugAndroidTest or adb command; otherwise Blocked or Revise.
- Explicit fallback for a genuinely unsupported API: No API-specific fallback is needed; minSdk 24 is the supported baseline.
- Evidence owner: Generator records command output and screenshot evidence; Evaluator validates the matrix and visual contract.
