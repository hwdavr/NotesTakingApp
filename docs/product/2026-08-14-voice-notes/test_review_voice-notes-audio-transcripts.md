# Test Review — voice-notes-audio-transcripts

## Review Scope and Evidence Provenance

| Item | Value |
|---|---|
| Feature / slice | `voice-notes-audio-transcripts` / US-1 through US-5 |
| Current commit | `8c45b8b` (`fix(quality): clear global architecture and localization gates`) |
| Reviewed implementation range | `3fb6066^..HEAD` (feature implementation plus harness records) |
| Baselines reviewed | `AGENTS.md`, `harness-evaluation.md`, `sprint-contract.md`, `feature_list.json`, `spec.md`, `progress.md`, `session-handoff.md`, `design.md`, `design_system.md`, `spike-us1-capture.md` |
| Changed production/test scope | 126 paths across recording, transcription, navigation, editor persistence/playback, Settings, tests, resources, and harness docs |
| Skill invocation | The callable Skill tool was unavailable in this environment; the checked-in `android-test-review` instructions were read and applied manually. This is an evidence limitation, not a passing invocation. |

## Command Evidence

The following results are recorded implementation-stage evidence, not independently executed during Stage 2. Stage 4 is required to refresh the runtime results.

| Command | Exit code | Timestamp | Commit | Provenance | Result / failure detail |
|---|---:|---|---|---|---|
| `./gradlew testDebugUnitTest --console=plain` | 0 | 2026-08-15 (fix pass) | `e0e468e` | Fresh fix-pass execution | Full JVM unit/integration suite passed, including rollback, notification, format-policy, and watchdog tests. |
| `./gradlew koverLog --console=plain` | 0 | 2026-08-15 (fix pass) | `e0e468e` | Fresh fix-pass execution | 81.8898% overall application line coverage. |
| `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest --console=plain` | 0 | 2026-08-15 (recorded) | `6e86183` | Recorded implementation evidence | 74/74 on API 33 only. No API 24, 31, or 34 runtime evidence. |
| `./gradlew assembleDebug --console=plain` | 0 | 2026-08-15 (recorded) | `6e86183` | Recorded implementation evidence | Debug build passed. |
| `./gradlew ktlintCheck --console=plain` | 0 | 2026-08-15 (recorded) | `6e86183` | Recorded implementation evidence | Passed per `summary_US-5.md`. |
| `./gradlew detekt --console=plain` | 0 | 2026-08-15 (recorded) | `6e86183` | Recorded implementation evidence | Passed per `summary_US-5.md`. |
| `./gradlew lintDebug --console=plain` | 0 | 2026-08-15 (recorded) | `6e86183` | Recorded implementation evidence | Passed per `summary_US-5.md`. |
| `./gradlew testDebugUnitTest --rerun-tasks --console=plain` | 0 | 2026-08-15 (evaluator Stage 4) | `6864e13` | Fresh evaluator execution | BUILD SUCCESSFUL; JVM/unit and integration suite completed. |
| `./gradlew koverLog --rerun-tasks --console=plain` | 0 | 2026-08-15 (evaluator Stage 4) | `6864e13` | Fresh evaluator execution | BUILD SUCCESSFUL; fresh aggregate line coverage 83.2334%. |
| `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest --rerun-tasks --console=plain` | 0 | 2026-08-15 (evaluator Stage 4) | `6864e13` | Fresh API-33 emulator execution | 74/74 instrumented tests passed; API 24/31/34 remain unverified. |
| Four declared `VoiceNotesVisualFlowTest` screenshot commands | 0 each | 2026-08-15 (fix pass) | `e0e468e` | Fresh API-33 emulator execution | Each one-test command passed, exported its in-test target-state capture, and produced a non-empty host PNG; production route reachability remains a separate residual risk. |

## Requirement-to-Test Traceability

`PASS` below means the mapped test proves the required observable behavior at the appropriate boundary. A test that only renders a stateless Composable or manually emits a callback is marked `REVISION REQUIRED` when the requirement is production wiring or lifecycle behavior.

| Source ID | Required behavior | Test file + method | Production trigger exercised | Observable assertion | Evidence status | Result |
|---|---|---|---|---|---|---|
| FR-001 | Home FAB opens Text Note / Record Note sheet and preserves Text Note route. | `VoiceEntryNavigationTest#homeRecordAllocatesPlaceholderAndOpensRecorder` | `HomeNotesScreenContent` FAB and tile only; no `AppNavigationHost` route. | Sheet/tile tags and fake callback state. | Recorded API-33 test | REVISION REQUIRED — production route and unchanged Text Note navigation are not asserted. |
| FR-002 | Home Record Note and editor Mic open the full-screen Recorder. | `VoiceEntryNavigationTest#homeRecordAllocatesPlaceholderAndOpensRecorder`; `VoiceNotesVisualFlowTest#editorVoiceBlockLightTheme` | Direct Content callbacks; no production navigation assertion. | Tags exist; callback does not prove destination. | Recorded API-33 test | REVISION REQUIRED |
| FR-003 | Rationale, permission request, permanent-denial recovery, and Settings deep link. | No mapped test. | None. | None. | Missing evidence | REVISION REQUIRED |
| FR-004 | Foreground service and persistent Stop/Pause/Resume notification across background. | `VoiceRecordingServiceIntegrationTest#foregroundServiceOwnsOnePrivateContiguousFile` | Starts real service and MediaRecorder. | File exists and is non-empty. Notification actions, elapsed copy, background survival, and stop cleanup are not asserted. | Recorded API-33 test | REVISION REQUIRED |
| FR-005 | Selected AAC/OPUS format, private path, and filename contract. | `AudioFilenameGeneratorTest`; `VoicePlatformComponentsTest#private file system creates measures and deletes recording files`; service smoke | Unit/file-system and AAC service seams. | Filename/path and AAC file are checked; no production OPUS selection-to-file assertion. | Recorded | REVISION REQUIRED |
| FR-006 | 60-second overlapping on-device STT and live append. | `VoiceRecorderTranscriptIntegrationTest#appendsOverlappingChunksThroughProductionViewModel`; `ChunkedTranscriptConcatenatorTest` | ViewModel uses injectable fake recognizer; Android production recognizer emits no transcript. | Fake overlap result is deduplicated. | Recorded | REVISION REQUIRED — production adapter explicitly returns `AudioSourceUnavailable` and never starts SpeechRecognizer. |
| FR-007 | Waveform, timer, transcript preview, and controls. | `VoiceRecorderLifecycleTest#startsRecordingThroughProductionEntryPoint`; visual recorder test | Direct `VoiceRecorderContent` with preloaded `UiState`. | Tags are displayed; no ticking service/ViewModel boundary. | Recorded | REVISION REQUIRED |
| FR-008 | Pause/resume from Recorder and notification preserves elapsed/waveform state. | `VoiceRecordingSessionStateReducerTest` methods; `VoiceRecorderLifecycleTest` | Reducer and direct Content callback; notification action not exercised. | Reducer state values pass; no notification or real service pause/resume assertion. | Recorded | REVISION REQUIRED |
| FR-009 | Discard has explicit confirmation and deterministic audio/transcript/placeholder cleanup. | No complete mapped test. | Direct dialog rendering exists; no production start→discard UI flow. | Dialog tags are present in source; cleanup outcome is not asserted through UI. | Missing/partial | REVISION REQUIRED |
| FR-010 | Editor Stop inserts Voice metadata and following editable TextBlock at focus. | `EditorVoiceNoteInsertionTest#savesEditorRecordingAtFocusedPositionWithEditableTranscript` | Direct `SaveVoiceNoteRecordingUseCase` invocation. | Document order and persisted metadata are asserted. | Recorded | REVISION REQUIRED — contract names `savesBothEntryContextsThroughProductionUseCase`; actual editor stop/navigation trigger is absent. |
| FR-011 | Home Stop creates untitled note at position 0 and navigates to editor. | `EditorVoiceNoteInsertionTest#savesHomeRecordingAtDocumentPositionZero`; navigation Content test | Direct use case plus fake placeholder; no actual stop-to-navigation flow. | Position zero is asserted; navigation result is not. | Recorded | REVISION REQUIRED |
| FR-012 | Player play/pause, seek, elapsed/total, size, audio-only delete. | `VoiceNoteEditorFlowTest#playsAndSeeksInlineVoicePlayer` | Direct `NoteEditorScreenContent` with seeded block and fake file. | Controls/labels exist and delete callback ID is observed; elapsed midpoint change is not asserted. | Recorded | REVISION REQUIRED |
| FR-013 | Transcript is a fully editable standard TextBlock. | `VoiceNoteEditorFlowTest#playsAndSeeksInlineVoicePlayer`; insertion tests | Seeded TextBlock rendered directly. | Text is displayed; no text input/edit/save assertion. | Recorded | REVISION REQUIRED |
| FR-014 | Note/block deletion removes private files; audio-only deletion keeps transcript. | `VoiceNoteRepositoryIntegrationTest` three methods; editor use-case tests | Repository/use-case direct calls. | Fake repository/file outcomes are checked. | Recorded | PASS for repository logic; production editor/note-delete UI trigger remains untested. |
| FR-015 | Settings shows storage totals and AAC/OPUS toggle. | `VoiceSettingsRepositoryTest`; `VoiceSettingsFlowTest#settingsShowsVoiceNotesAndPersistsOpusSelection` | Repository direct and Settings Content callback. | Totals, selection, and helper copy are asserted. | Recorded | PASS for isolated Settings state/repository behavior; next-recording production wiring is not asserted here. |
| FR-016 | One active session; context switch silently discards old and starts new. | `RecordingSessionManagerTest#replacesHomeSessionWhenEditorMicStarts` | Manager direct replacement callback. | One token and discard callback are asserted. | Recorded | REVISION REQUIRED — real controller/service/file/placeholder replacement is not exercised. |
| FR-017 | 128 MB storage preflight blocks start and service is not started. | `RecordingStoragePreflighterTest` | Preflighter direct. | Threshold/result bytes are checked. | Recorded | REVISION REQUIRED — exact 50 MB copy and no-service-start boundary are not asserted. |
| AC-001 | Permission grant from Home Record starts Recorder automatically. | No production-flow test; `VoiceEntryNavigationTest` is Content-only. | None. | No system permission result or service start assertion. | Missing | REVISION REQUIRED |
| AC-002 | Background/screen-off notification remains with elapsed time and actions. | No mapped long-running/background test. | None. | None. | Missing | REVISION REQUIRED |
| AC-003 | Resume restores paused timer/waveform. | Reducer unit tests. | Reducer only. | Paused elapsed is checked. | Recorded | REVISION REQUIRED — real service and notification path absent. |
| AC-004 | Editor Mic silently replaces Home session. | `RecordingSessionManagerTest`; Content-only editor tests. | Manager direct. | Replacement callback/token state. | Recorded | REVISION REQUIRED |
| AC-005 | 45-minute OPUS stop saves full file/transcript/new note and navigates. | Home/editor insertion tests use short fake metadata; no long OPUS production flow. | Direct use case. | Metadata/document order only. | Partial | REVISION REQUIRED |
| AC-006 | Media3 midpoint seek updates elapsed time. | `VoiceNoteEditorFlowTest`. | Direct Content/player with generated WAV. | Slider/labels exist; no midpoint elapsed assertion. | Recorded | REVISION REQUIRED |
| AC-007 | Audio-only delete removes player/file but keeps transcript. | Repository/editor use-case tests. | Direct repository/use-case. | Nullable path and transcript preservation. | Recorded | PASS for domain/data behavior; UI confirmation/file path is not end-to-end. |
| AC-008 | Note deletion cascades all VoiceNote files. | `VoiceNoteRepositoryIntegrationTest#deletesAllFilesWhenNoteIsDeleted`. | Repository fake. | Both files deleted. | Recorded | PASS for tested repository behavior; no actual Room transaction or editor route evidence. |
| AC-009 | OPUS setting changes next recording extension and Room enum. | `VoiceSettingsRepositoryTest`; Settings UI test. | Settings only; no next recording. | Preference state and UI selected state. | Recorded | REVISION REQUIRED |
| AC-010 | 50 MB storage blocks start with exact copy and no service. | `RecordingStoragePreflighterTest`. | Preflighter only. | Generic threshold behavior. | Recorded | REVISION REQUIRED |
| Edge: chunk boundary | Overlap deduplication prevents repeated/split fragments. | `ChunkedTranscriptConcatenatorTest`; transcript integration test. | Concatenator and fake recognizer. | Ordered deduplicated text. | Recorded | PASS for domain concatenator; real Android recognition remains unproven. |
| Edge: model unavailable | Warn and continue audio-only. | `VoiceRecorderTranscriptIntegrationTest#fallsBackAndCancelsCleanly`; fallback UI test. | Fake recognizer/direct Content. | Warning/status and controls. | Recorded | PASS for fallback boundary; production model availability is only a source-unavailable signal. |
| Edge: silent callback | 65-second watchdog marks failed segment and continues. | No real-clock/watchdog test; integration manually emits `ChunkTimedOut`. | Manual event injection. | Marker is asserted. | Partial | REVISION REQUIRED — watchdog scheduling and continuation are not independently exercised. |
| Edge: disk fills | Auto-stop, preserve partial audio, and show saved-duration feedback. | No mapped test. | `VoiceNoteRecordingService.failRecording` deletes the current file. | No preserved partial file/snackbar evidence. | Missing | REVISION REQUIRED |
| Edge: app data clear/uninstall | Private audio is removed by Android app-data cleanup. | No mapped test. | None. | None. | Missing | REVISION REQUIRED or document as platform-only N/A with evidence. |
| Edge: audio focus loss | Auto-pause on focus loss and resume on regain. | No mapped test and no `AudioManager` production path found. | None. | None. | Missing | REVISION REQUIRED |
| Edge: placeholder allocation | Allocate before recording and roll back on discard. | Placeholder use-case tests; navigation Content test. | Use case direct. | Allocation and isolated discard repository call. | Recorded | REVISION REQUIRED — production discard flow/transaction is not asserted. |
| Edge: discard during STT | Cancel recognizer, delete partial audio, remove placeholder. | Transcript integration manually calls `viewModel.onDiscard()`. | ViewModel fake controller. | Fake cancellation/status. | Recorded | REVISION REQUIRED — file and production placeholder cleanup are absent. |
| NFR: API compatibility | Runtime behavior on API 24, 31, and target API. | `spike-us1-capture.md`; API-33 tests. | API-33 only. | Source compatibility claim, not runtime proof. | Recorded/partial | REVISION REQUIRED |
| NFR: private local storage | Audio stays under app-private `files/voice-notes`. | `VoicePlatformComponentsTest`; service smoke. | File-system/service seam. | Parent directory and file lifecycle. | Recorded | PASS |
| NFR: background resilience | Foreground service owns capture through screen-off/background/configuration. | No long-running smoke test. | None. | None. | Missing | REVISION REQUIRED |
| NFR: accessibility | Labels, live-region, contrast, target sizes, scaling. | Rule scripts and direct tag checks; no dedicated accessibility test. | Partial Content rendering. | Some tags present; full contract not asserted. | Recorded/partial | REVISION REQUIRED |
| NFR: single active session | Singleton manager serializes contexts and cleans old files. | Manager unit test. | Manager direct. | Token replacement. | Recorded | REVISION REQUIRED |

### Fix Status column — 2026-08-15

The following fix-pass column applies to the traceability rows above. `Fixed ✅` means the reported test gap or implementation boundary was addressed and re-verified; `Unresolved ⚠️` identifies residual production-route, runtime-matrix, or platform-boundary evidence that still needs human review; `PASS (unchanged)` was already sufficient and was not changed.

| Source ID | Fix Status |
|---|---|
| FR-001 | Unresolved ⚠️ |
| FR-002 | Unresolved ⚠️ |
| FR-003 | Unresolved ⚠️ |
| FR-004 | Unresolved ⚠️ |
| FR-005 | Fixed ✅ |
| FR-006 | Unresolved ⚠️ |
| FR-007 | Unresolved ⚠️ |
| FR-008 | Fixed ✅ |
| FR-009 | Unresolved ⚠️ |
| FR-010 | Unresolved ⚠️ |
| FR-011 | Unresolved ⚠️ |
| FR-012 | Unresolved ⚠️ |
| FR-013 | Unresolved ⚠️ |
| FR-014 | Fixed ✅ |
| FR-015 | PASS (unchanged) |
| FR-016 | Unresolved ⚠️ |
| FR-017 | Unresolved ⚠️ |
| AC-001 | Unresolved ⚠️ |
| AC-002 | Unresolved ⚠️ |
| AC-003 | Fixed ✅ |
| AC-004 | Unresolved ⚠️ |
| AC-005 | Unresolved ⚠️ |
| AC-006 | Unresolved ⚠️ |
| AC-007 | Fixed ✅ |
| AC-008 | Unresolved ⚠️ |
| AC-009 | Fixed ✅ |
| AC-010 | Unresolved ⚠️ |
| Edge: chunk boundary | PASS (unchanged) |
| Edge: model unavailable | Fixed ✅ |
| Edge: silent callback | Fixed ✅ |
| Edge: disk fills | Unresolved ⚠️ |
| Edge: app data clear/uninstall | Unresolved ⚠️ |
| Edge: audio focus loss | Unresolved ⚠️ |
| Edge: placeholder allocation | Unresolved ⚠️ |
| Edge: discard during STT | Unresolved ⚠️ |
| NFR: API compatibility | Unresolved ⚠️ |
| NFR: private local storage | PASS (unchanged) |
| NFR: background resilience | Unresolved ⚠️ |
| NFR: accessibility | Fixed ✅ |
| NFR: single active session | Unresolved ⚠️ |

## Test Quality Findings

- [x] Names generally describe the intended behavior and the JVM suite has useful focused tests.
- [ ] Every mapped test exercises the required production trigger. Several acceptance tests render `*Content` with preloaded state and fake callbacks; they do not prove `AppNavigationHost`, permission, service, or editor completion wiring.
- [ ] Every mapped test has a direct observable assertion for the full requirement. Notification actions, exact permission/storage copy, midpoint elapsed update, text edit persistence, and production navigation are missing.
- [x] No obvious tautological assertion was found in the feature-specific tests reviewed.
- [ ] Boundary tests are incomplete: no real watchdog timing, audio focus, disk-full partial save, background/screen-off, API 24/31/34, or OPUS end-to-end recording test.
- [x] API-specific shared JSON scenarios are not applicable; this feature has no new API endpoint.
- [ ] The sprint contract names several methods that do not exist exactly, including `pauseResumeBackgroundStopAndDiscard`, `blocksBelowThreshold`, `savesBothEntryContextsThroughProductionUseCase`, and `persistsFormatAndReportsStorage`. The implementation evidence runs broader classes instead, so the mandatory contract rows are not mechanically traceable.
- [ ] Visual-flow tests are not production navigation tests: `VoiceNotesVisualFlowTest` stacks Content composables and supplies seeded UI state. The screenshot can prove component appearance, not reachability through the Home → Recorder → Editor → Settings journey.

## Conditional Categories

| Category | In scope? | Coverage / N/A reason | Result |
|---|---|---|---|
| Runtime permissions | Yes | Permission request/recovery implementation is present, but no mapped system grant/denial/permanent-denial/Settings runtime test is available. | Unresolved ⚠️ |
| Asynchronous callbacks and animation | Yes | Injectable production recognizer start, fake events, and virtual-time watchdog are covered; source-fed single-mic runtime behavior remains unverified. | Unresolved ⚠️ |
| Lifecycle and navigation cleanup | Yes | Reducer/manager/service smoke tests pass; production background, stop/discard, and route completion remain unverified. | Unresolved ⚠️ |
| Error and retry behavior | Yes | Fallback, threshold, rollback, and partial-save code paths are covered; disk-full fault injection remains unverified. | Unresolved ⚠️ |
| API/data error matrix | No | No new network API endpoint; Room/file/DataStore behavior is covered by repository/use-case tests. | PASS (unchanged) |

## Coverage Distribution

| Scope / class | Coverage | Branches or requirements not proven | Result |
|---|---:|---|---|
| Overall project | 81.8898% fresh | Meets 80% aggregate threshold; this does not compensate for missing production boundary tests. | PASS on aggregate threshold |
| New ViewModels / use cases | Fresh Kover HTML published for each class | ViewModel/use-case line coverage is independently verifiable and meets the 90% target for the new use cases and recorder ViewModel. | PASS on line-coverage target |
| `VoiceRecorderViewModel` | Fresh Kover HTML: 93.4% line, 77.8% branch, 100% method | Branch-level gaps remain in platform/error paths; line target is met. | PASS on line target; residual branch evidence |
| `VoiceEntryViewModel` / `SettingsViewModel` | Fresh Kover HTML: 100% line each | Narrow classes are covered, but this does not cover the missing integration paths. | PASS for measured line coverage |
| `ChunkedTranscriptConcatenator` / `RecordingSessionManager` | Fresh Kover HTML: 93.1% / 100% line | Pure/session helpers are covered; Android STT/service behavior is not. | PASS for measured line coverage |
| `SaveVoiceNoteRecordingUseCase` / `DeleteVoiceNoteAudioUseCase` / `DeleteVoiceNoteBlockUseCase` | Fresh Kover HTML: 100% / 100% / 100% line | Rollback, missing-note, metadata failure, and direct block-delete paths are covered. | PASS on line-coverage target |

## Regression Verification

| Item | Evidence | Result |
|---|---|---|
| Reproduction test red before fix (bug fixes only) | Not a bug-fix task. | N/A |
| Reproduction test green after fix | Not a bug-fix task. | N/A |
| No uncontrolled timing or threading | `RecordingTranscriptWatchdogTest` injects a test scope and advances virtual time through the 65-second watchdog. | Fixed ✅ |

## Fix Pass Summary

- Fixed: 9 traceability rows, including notification-state coverage, direct Voice-block cleanup, format fallback policy, production adapter start seam, watchdog virtual-time scheduling, and per-class Kover evidence.
- Passed unchanged: 3 rows (`FR-015`, chunk-boundary, and private-local-storage behavior).
- Unresolved: 28 rows. Residuals are production `AppNavigationHost` route coverage, system permission/background/screen-off/focus/disk-full runtime tests, API-24/API-31/API-34 runtime certification, the source-fed single-microphone bridge, and full production visual navigation.
- Fix-pass evidence: `e0e468e`; `./gradlew testDebugUnitTest`, `./gradlew koverLog`, `./gradlew ktlintCheck`, `./gradlew detekt`, `./gradlew lint`, and full API-33 connected tests all exited 0 on 2026-08-15.

## Verdict

**Fix-pass verdict:** The test review findings that were actionable within the available runtime and test seams are fixed and re-verified. The unresolved rows above are explicitly surfaced for human review rather than represented as passing evidence.

### Required test follow-up

1. Human review must decide whether the source-fed single-microphone STT bridge is acceptable or requires a follow-up design/spike.
2. Add production-boundary tests for permission recovery, background/screen-off, audio focus, disk-full partial save, and editor/Home stop navigation when a suitable host/runtime seam is available.
3. Replace stacked/preloaded visual Content tests with state-verifying production navigation tests when the Hilt navigation host can be exercised in the test process.
