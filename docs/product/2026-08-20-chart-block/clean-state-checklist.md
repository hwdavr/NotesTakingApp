# Clean State Checklist — Chart Block US-4

| # | Check Item | Category | Status | Notes |
|---|---|---|---|---|
| 1 | `./gradlew assembleDebug` | Build | PASS | Exit code 0 after the export/ViewModel changes. |
| 2 | Compiler warning review | Build | PASS | No new compiler warnings observed in the active module. |
| 3 | Duplicate dependency/class safety | Build | PASS | Assemble and Android Lint completed without conflicts. |
| 4 | `./gradlew ktlintCheck` | Code Quality | PASS | Exit code 0 after formatting touched imports/helpers. |
| 5 | `./gradlew detekt` | Code Quality | PASS | Exit code 0; no new findings. |
| 6 | `./gradlew lintDebug` | Code Quality | PASS | Exit code 0; no Android Lint findings. |
| 7 | Suppression audit | Code Quality | PASS | No new suppressions, baselines, ignore directives, or rule exclusions. |
| 8 | Layer boundaries and UDF | Architecture | PASS | Export format detection is ViewModel state; Composables only render state and dispatch events. |
| 9 | Localization and interactive tags | Standards | PASS | New export/fallback copy uses resources; custom Compose/localization checks report 0 violations. |
| 10 | Secret/API alignment review | Security | PASS | No secrets and no API contract changes; the slice is local-only. |
| 11 | Dispatcher/resource review | Runtime | PASS | Export remains on the ViewModel IO dispatcher; PDF/PNG resources are closed/recycled. |
| 12 | `./gradlew testDebugUnitTest` | Testing | PASS | Full JVM suite exited 0; focused chart exporter and ViewModel tests passed. |
| 13 | `./gradlew clean koverLog --rerun-tasks` | Coverage | PASS | Application line coverage 83.569%, above the required 80%; NoteEditorViewModel 96.5% and ChartBitmapRenderer 95.1% line coverage. |
| 14 | Real platform boundary | Evidence | PASS | `ChartPlatformBoundaryTest#testProductionCanvasBitmapAndPdfDocumentBoundary` passed on emulator API 33. |
| 15 | Visual flow captures | Evidence | PASS | Five required screenshots plus supplemental empty-state capture were produced from active `takeScreenshot()` calls. |
| 16 | Visual reference anchors | Evidence | PASS | `check-visual-evidence-contract.sh --evaluate` passed; five rows tie methods/tags/bounds to non-empty PNGs. |
| 17 | Platform capability matrix | Evidence | PASS | `check-platform-evidence.sh --evaluate --slice US-4` passed with fail-loudly policy. |
| 18 | Full connected regression | Testing | PASS | `env ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest` completed `172/172 tests` with 0 skipped/failures. |
| 19 | Compose/localization/architecture/assertion scripts | Standards | PASS | All four custom rule checkers exited 0 with 0 violations. |
| 20 | Observability review | Observability | PASS | No IPC, service, or background invocation was added. |
| 21 | Cleanliness/state reset review | Cleanliness | PASS | No production cache/database reset behavior changed; build outputs remain ignored. |
| 22 | Lifecycle tracker | Harness | PASS | `check-feature-lifecycle.sh` exited 0 after the tracker moved to `To be human reviewed`. |
| 23 | Product/workspace documentation | Documentation | PASS | `feature_list.json`, `product.md`, summary, matrix, anchor report, and visual artifacts are updated. |
| 24 | Harness compatibility fix | Harness | PASS | Platform evidence gate now type-guards legacy string evidence while requiring structured active-slice evidence. |
| 25 | Session handoff | Documentation | PASS | `session-handoff.md` records the verified runtime, remaining API-level caveat, exact commands, and evaluator handoff. |
| 26 | Debug installation | Device | PASS | `env ANDROID_SERIAL=emulator-5554 ./gradlew installDebug` reported `Installed on 1 device.` |
| 27 | Git state ownership | Repository | PASS | Verified implementation/evidence baseline is commit `7545d61`; final documentation/tracker commit follows. |
