# Session Handoff

## Verified Now

- What is currently working: ChartBlock creation/conversion, complete persisted JSON, editable Data view, selected-column Options flow, Bar/Line/Pie rendering with negative/all-zero handling, datum selection/tooltips, read-only inspection, Markdown ZIP/PDF export fallbacks, and all declared visual states.
- What verification actually ran: 437 JVM tests; clean Kover 83.569% overall with NoteEditorViewModel 96.5% line and ChartBitmapRenderer 95.1% line; 172/172 connected Android tests on emulator-5554 API33; chart package 16/16; assembleDebug; ktlintCheck; detekt; lint; Compose/localization/architecture/assertion-quality checks; UI, visual, platform, and lifecycle validators; and five active-window screenshot pulls.

## Changed This Session

- Code or behavior added: normalized ChartBlockCardModel preparation, zero-inclusive ChartRenderGeometry, asynchronous bitmap rendering, geometry-aware datum targets, fixed/bounded Data rows, block/column/row-scoped tags, nested selector semantics, real editor chart callback wiring, complete persistence/export/renderer/platform regression tests, and large-table coverage.
- Infrastructure or harness changes: updated feature-list evidence counts, platform capability matrix, sprint visual command/anchor records, ui_verification.json, visual artifacts, evaluator fix-pass report sections, and product tracker documentation. No harness source was changed.

## Broken Or Unverified

- Known defect: No unresolved chart-block finding remains in the code review, test review, or visual review reports.
- Unverified path: Direct API24 minimum-runtime and API34 target-runtime executions could not run because those system images are not provisioned in this workspace. API33 runtime evidence and targetSdk34 build evidence pass; the matrix records the unsupported direct-runtime environments under a fail-loudly policy for human review.
- Risk for the next session: Preserve ChartBlock JSON field names, stable IDs, two-level Options semantics, active-window screenshot paths, and the API capability matrix. Do not convert the documented API24/API34 environment requirement into a claimed pass without real devices.

## Next Best Step

- Highest-priority unfinished feature: Human review of chart-block at tracker status `To be human reviewed`.
- Why it is next: Fix-Stages 1–6 are complete, all four feature slices remain passing, and the required fix-pass reports/evidence are attached.
- What counts as passing: Human confirmation of the 16/16 code findings, zero unresolved test/visual findings, fresh quality and runtime evidence, and the documented API capability matrix.
- What must not change during that step: Keep the persisted ChartBlock contract, test IDs/tags, visual evidence paths, `ui_verification.json`, and fail-loud API boundary policy stable unless a new review finding requires a new fix pass.

## Commands

- Startup: `adb devices`; open the existing Note Editor and use Advanced Basic Blocks or focused Table Options.
- Verification: `./gradlew testDebugUnitTest`; `./gradlew clean koverLog --rerun-tasks`; `./gradlew assembleDebug`; `./gradlew ktlintCheck`; `./gradlew detekt`; `./gradlew lint`; `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest`; `bash harness/scripts/check-ui-verification-artifact.sh docs/product/2026-08-20-chart-block`; `bash harness/scripts/check-visual-evidence-contract.sh docs/product/2026-08-20-chart-block --evaluate`; `bash harness/scripts/check-platform-evidence.sh docs/product/2026-08-20-chart-block --evaluate`; `bash harness/scripts/check-feature-lifecycle.sh`.
- Focused debug command: `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.chart.ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary`.

## Workflow Note

The checked-in `.agents/workflows/harness-fix.md` defines Fix-Stages 1–6. The user-requested Fix-Stages 7–8 have no checked-in definition; no behavior was inferred. The runtime Skill tool was unavailable, so the checked-in skill contracts were read and followed manually.
