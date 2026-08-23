# Platform Capability Matrix — Table to Chart Block

## Classification

This feature uses Android runtime rendering and export APIs through an app-owned chart adapter. It must render Bar, Line, and Pie charts, expose touch hit selection, generate Android bitmaps, and draw PDF output using `Canvas`, `Bitmap`, and `PdfDocument`. The feature has no network, storage permission, hardware sensor, or cloud dependency, but the shipped Android rendering/export boundary is platform-bound and requires real instrumented evidence.

Minimum API: 24. Target API: 34.

## Scope

* Feature/slices: US-1 through US-4, Table to Chart Block.
* Platform boundaries: Compose chart renderer/selection adapter, Android Canvas/Bitmap chart image generation, PdfDocument chart placement, and the existing Storage Access Framework export flow.
* Resource owners: chart renderer adapter owns chart-library models and hit testing; chart export renderer owns bitmap generation; NoteExporter owns PDF/ZIP composition; NoteEditorViewModel owns persistence and auto-save.
* Input/output contract: persisted ChartBlock domain data enters the renderer; renderer returns a local chart surface/selection result and a non-empty bitmap when valid; exporter consumes the bitmap/table fallback and returns a PDF or Markdown ZIP through the existing export entry point.

## Runtime Matrix

| Runtime/API | Capability under test | Required behavior | Test ID / exact command | Environment evidence | Status |
|---|---|---|---|---|---|
| API 24 | Minimum-runtime compatibility boundary | Chart renderer, touch selection, and export must remain functional on the minimum supported runtime | TC-US-4-PLATFORM; same production boundary command as the API33 row | This workspace provides API33 only; an API24 system image requires separate environment provisioning | Human review environment required |
| API 33 | Available Android bitmap/export boundary | Production adapter generates non-empty Bar/Line/Pie Bitmap/Canvas results and PdfDocument output from local fixtures | TC-US-4-PLATFORM; `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary` | `emulator-5554`, `sdk_gphone64_arm64`, API 33 runtime; 1 test passed | Verified on API33 |
| API 33 | Data view and two-level Options flow | Chart/Data control, Options → Data column, Add row, Add column, protected columns, and read-only behavior render and interact | TC-US-2-02; `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartDataFlowTest#testOptionsOpensDataColumnSecondLevelAndSelectsColumn` | `emulator-5554`, `sdk_gphone64_arm64`, API 33 runtime; test passed | Verified on API33 |
| Target 34 / API 34 | Target-runtime visual flow | Chart, Data, both Options layers, empty/selected, read-only/dark states are captured from active rendering | TC-US-4-VIS-01..05; `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartVisualFlowTest` | targetSdk 34 build verified on API33; an API34 system image requires separate environment provisioning | Build target verified; human runtime review required |

## Real Platform Boundary Test

* Required: Yes.
* Test ID: `TC-US-4-PLATFORM`.
* Instrumented test file: `app/src/androidTest/java/com/example/notesapp/ui/editor/chart/ChartPlatformBoundaryTest.kt`.
* Real platform signal: production `AndroidCanvas`/`Bitmap`/`PdfDocument` path exercised on an Android runtime; no fake renderer may satisfy this row.
* Exact command: `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary`.
* Fixture/data source: deterministic local ChartBlock fixture with valid, invalid, negative, and empty values; no live backend or network.
* Assertion: bitmap dimensions and byte count are non-zero, PDF output contains at least one page and chart title, and a render failure produces the table fallback without data loss.

## Unsupported Environment Policy

The policy is `fail_loudly`. A missing emulator, API level, chart runtime, or screenshot-capable environment causes the connected test command to exit non-zero or the owning slice to be marked Blocked/Revise. It is never recorded as skipped or passing evidence.

* Policy: `fail_loudly`.
* Missing environment result: non-zero `connectedDebugAndroidTest`, or the slice remains `Blocked`/`Revise`.
* Supported baseline: API 24; target API 34.
* Evidence owner: Generator records commands, exit codes, and pulled screenshots; Evaluator validates platform and visual evidence contracts.

## Direct Runtime Note

The available SDK contains only the API33 emulator used by the passing real boundary and visual tests. The API24 minimum-runtime and API34 target-runtime rows are intentionally routed to human review because their direct system images are not provisioned in this workspace; no API-level evidence is inferred from the API33 run.
