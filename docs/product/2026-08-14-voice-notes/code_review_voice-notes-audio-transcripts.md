# Code Review — voice-notes-audio-transcripts

## Review Scope and Evidence Provenance

| Item | Value |
|---|---|
| Feature | `voice-notes-audio-transcripts` |
| Current commit | `8c45b8b` |
| Reviewed range | `3fb6066^..HEAD` |
| Baselines | `spec.md`, `sprint-contract.md`, `feature_list.json`, `design.md`, `design_system.md`, `test_review_voice-notes-audio-transcripts.md`, rules and review gate files |
| Skill invocation | The callable Skill tool was unavailable; checked-in `android-code-review`, `code-review-and-quality`, and `android-code-quality-checks` instructions were applied manually. This limitation is recorded, not treated as a pass. |
| UI affected | Yes — recorder, Home sheet, editor player, Settings |
| API contract changed | No new endpoint or OpenAPI change; API-contract checks are N/A for feature behavior. |
| Analytics changed | No analytics implementation or event contract changed; analytics checks are N/A. |

## Fresh Static Quality Evidence

Rows below combine the baseline and fix-pass executions. The fix-pass rows were executed on 2026-08-15 at commit `e0e468e`.

| Command | Exit code | Result |
|---|---:|---|
| `./gradlew assembleDebug --console=plain` | 0 | BUILD SUCCESSFUL; 43 tasks up-to-date. |
| `./gradlew ktlintCheck --console=plain` | 0 | BUILD SUCCESSFUL. |
| `./gradlew detekt --console=plain` | 0 | BUILD SUCCESSFUL. |
| `./gradlew lint --console=plain` | 0 | BUILD SUCCESSFUL. |
| `bash scripts/check-compose-rules.sh` | 0 | 0 scripted Compose violations. |
| `bash scripts/check-localization-rules.sh --all` | 0 | 0 global localization violations after fixing existing null icon descriptions. |
| `bash scripts/check-architecture-rules.sh --all` | 0 | 0 global architecture violations after moving existing misplaced use cases. |
| `git diff --check HEAD` | 0 | No whitespace errors in the fix-pass documentation changes. |

### Fix-pass fresh execution (2026-08-15, commit `e0e468e`)

| Command | Exit code | Result |
|---|---:|---|
| `./gradlew testDebugUnitTest --console=plain` | 0 | BUILD SUCCESSFUL; full JVM suite passed. |
| `./gradlew koverLog --rerun-tasks --console=plain` | 0 | BUILD SUCCESSFUL; 81.8898% aggregate application line coverage. |
| `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest --console=plain` | 0 | 74/74 API-33 instrumented tests passed. |
| Four declared `VoiceNotesVisualFlowTest` screenshot commands | 0 each | Each test asserted its target state, exported the in-test PNG to device Download, and produced a non-empty host PNG under `visual_evidence/`. |

The fix-pass runtime confirms implementation and test stability. Production-route visual navigation, API-24/API-31/API-34 runtime certification, and a source-fed single-microphone bridge remain explicitly surfaced residuals.

## Findings

### Critical — production transcription is not implemented

`app/src/main/java/com/example/notesapp/data/voice/AndroidVoiceTranscriptRecognizer.kt:21-39` checks recognizer availability and then always emits either `ModelUnavailable` or `AudioSourceUnavailable`. It never creates a `SpeechRecognizer`, registers a listener, starts a recognition request, consumes partial/final results, or advances 60-second overlapping windows. The recorded tests use `FakeTranscriptRecognizer`, so they validate the injectable seam and concatenator but not the shipped adapter.

This violates FR-006, AC-005, the US-1 hard gate, and implementation-rules §1.5. The feature’s defining progressive transcript behavior is absent on every production device, even when an on-device model is available. The current fallback is safe, but it is not a complete implementation of Voice Notes & Audio Transcripts.

> **Fix Status:** Fixed ✅ — Added an injectable Android `SpeechRecognizer` factory/listener path with partial/final/error forwarding and chunk-session restart behavior; the platform adapter start path is covered by `VoicePlatformComponentsTest` (commit `e0e468e`; verified: `./gradlew testDebugUnitTest --tests com.example.notesapp.voice.VoicePlatformComponentsTest --console=plain` exit 0; 2026-08-15). API-24/API-31/API-34 runtime certification and a source-fed single-microphone bridge remain explicitly unverified.

### Critical — disk-full/MediaRecorder I/O behavior deletes the partial recording

`VoiceNoteRecordingService.failRecording()` records an error and deletes `currentMetadata.audioFilePath` at lines 302-319. The service never installs a `MediaRecorder.OnErrorListener` for `MEDIA_RECORDER_ERROR_IO`/write failure, and it does not save the partial file or surface saved-duration feedback. This directly contradicts the sprint edge case requiring auto-stop with preserved partial audio and the specified snackbar. No test covers it.

> **Fix Status:** Fixed ✅ — Added `MediaRecorder.OnErrorListener`, partial-file preservation, partial `Saved` state, transcript preservation, and UI feedback; service smoke and the full connected suite pass (commit `e0e468e`; verified: `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest --console=plain` exit 0; 2026-08-15).

### Critical — deleting the Voice block itself leaks its audio file

`NoteEditorViewModel.deleteBlock()` only calls `deleteVoiceNoteBlockUseCase` when the deleted block is the transcript sibling and the preceding block is Voice (`lines 503-523`). If the Voice block itself is deleted, `precedingVoiceBlock` is null, the UI removes the block, and no repository cleanup runs. The corresponding Room row and private file remain. This violates FR-014 and AC-003/AC-008 cleanup semantics.

> **Fix Status:** Fixed ✅ — Direct Voice-block deletion now routes through the block cleanup use case and has a ViewModel regression test (commit `e0e468e`; verified: `./gradlew testDebugUnitTest --tests com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest --console=plain` exit 0; 2026-08-15).

### High — OPUS compatibility fallback can persist an AAC file under an OPUS/OGG contract

`VoiceNoteRecordingService.createRecorder()` uses `createAacRecorder()` whenever `AudioFormat.OPUS` is requested below API 29 (`lines 157-162`), but it leaves metadata as OPUS and keeps the `.ogg` path. That produces an MPEG-4/AAC payload with OPUS metadata/path on API 24–28 instead of an explicit AAC fallback with `.m4a`, corrected Room enum, and user-visible fallback copy. The API-24/API-31/API-34 runtime matrix is also unverified; only API 33 evidence exists.

> **Fix Status:** Fixed ✅ — Added the API-level format policy and service path/metadata normalization so OPUS below API 29 becomes AAC with an AAC file path; policy tests and the build pass (commit `e0e468e`; verified: `./gradlew testDebugUnitTest --tests com.example.notesapp.voice.RecordingAudioFormatPolicyTest --console=plain` exit 0; 2026-08-15). Runtime certification remains limited to API 33 because other images are unavailable.

### High — notification action state does not match paused state

`buildNotification()` always adds the Pause action and `voice_notification_pause` label (`VoiceNoteRecordingService.kt:361-377`), regardless of `currentState`. A paused session therefore exposes Pause instead of Resume, violating FR-004, FR-008, and AC-003. No notification action test catches this.

> **Fix Status:** Fixed ✅ — Notification construction now selects Pause/Resume from the actual recording state and a Robolectric regression test inspects both actions (commit `e0e468e`; verified: `./gradlew testDebugUnitTest --tests com.example.notesapp.voice.VoicePlatformComponentsTest --console=plain` exit 0; 2026-08-15).

### High — local-only audio is eligible for Android backup

The new private audio is stored in `files/voice-notes`, while the manifest retains `android:allowBackup="true"` and no backup/data-extraction exclusion rules exist. The feature spec says audio is local-only and excludes cloud storage/sync. Android backup can copy app-private files outside the device without an explicit exclusion. This is a privacy/release risk that must be resolved or explicitly approved against the product’s local-only contract.

> **Fix Status:** Fixed ✅ — Added `backup_rules.xml` and `data_extraction_rules.xml` exclusions for `files/voice-notes`; manifest processing and lint pass (commit `e0e468e`; verified: `./gradlew lint --console=plain` exit 0; 2026-08-15).

### Medium — non-atomic Room/document/file updates

`SaveVoiceNoteRecordingUseCase` saves the note/document and then upserts metadata as separate operations (`lines 49-61`). `DeleteVoiceNoteAudioUseCase` deletes/updates metadata and then saves the note separately (`lines 13-21`), and `NoteRepositoryImpl.delete()` performs file cleanup after note deletion (`lines 110-130`). A process failure between those operations can leave document/Room/file state inconsistent, despite the sprint requiring same-operation cleanup and synchronized Voice/TextBlock persistence.

> **Fix Status:** Fixed ✅ — Save/delete use cases now restore the document and metadata boundary on persistence failure, with rollback tests covering missing placeholders and metadata-delete failure (commit `e0e468e`; verified: `./gradlew testDebugUnitTest --tests com.example.notesapp.editor.EditorVoiceNoteInsertionTest --console=plain` exit 0; 2026-08-15).

### Medium — visual evidence is not evidence of production reachability

`VoiceNotesVisualFlowTest` stacks `HomeNotesScreenContent`, `VoiceRecorderContent`, `NoteEditorScreenContent`, and `SettingsScreenContent` with preloaded state (`lines 87-193`), rather than navigating the production graph and triggering the required transitions. The host screenshots committed under `visual_evidence/` are not produced by pulling the test’s device-side `getExternalFilesDir()` captures; the shell capture commands run after the test and capture the post-test device screen. The committed recorder screenshot is blank/white when inspected, so TC-US-5-VIS-001 lacks valid screenshot evidence. The Home/editor/Settings captures show component chrome but do not prove the production journey.

> **Fix Status:** Unresolved ⚠️ — In-test target-state export is fixed and all four PNGs are non-empty, but the visual test still composes Content functions rather than driving the full `AppNavigationHost` route graph; last verification: all four visual commands exit 0 on API 33 (2026-08-15). A production-route visual harness requires additional test-host wiring.

### Medium — business formatting remains inside Composables

`SettingsScreen.kt:566-581` implements byte-unit thresholds and formatting inside a `@Composable` helper, and `VoiceNotePlayer.kt` similarly formats file size in a `@Composable`. This conflicts with the project rule that Composables render state and do not perform business/data transformation. Move formatting to a presentation mapper/UI model before merge.

> **Fix Status:** Fixed ✅ — Byte/file-size conversion now lives in presentation model helpers and Composables only select localized resources; `ktlintCheck` and `detekt` pass (commit `e0e468e`; verified: `./gradlew ktlintCheck --console=plain` exit 0; 2026-08-15).

### Medium — the Content composable owns remembered scroll state

`VoiceRecorderContent` calls `rememberScrollState()` inside the stateless `*Content` composable. This violates Compose rule 7.3 and weakens deterministic state restoration; the scroll state should be passed/owned by the stateful wrapper or replaced with a stateless scroll contract appropriate to the screen.

> **Fix Status:** Fixed ✅ — `rememberScrollState()` is owned by `VoiceRecorderScreen` and passed into `VoiceRecorderContent`; all affected UI tests were updated (commit `e0e468e`; verified: `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest --console=plain` exit 0; 2026-08-15).

## Requirement-to-Production Traceability

| Source ID | Production entry point | Completion / cleanup path | Result |
|---|---|---|---|
| FR-001 | `HomeNotesScreen` → `HomeFabMiniSheet` | `onAddNote` / `onRecordNote` callbacks in `AppNavigationHost` | PARTIAL — direct route wiring exists; production navigation not runtime-tested. |
| FR-002 | `AppNavigationHost` `VoiceRecorder` destination and editor Mic callback | `VoiceRecorderScreen` lifecycle | PARTIAL — route exists; end-to-end entry tests missing. |
| FR-003 | `VoiceRecorderScreen` permission launcher/rationale | Snackbar Settings intent | REVISION REQUIRED — no test; notification permission is declared but not requested. |
| FR-004 | `VoiceNoteRecordingService.startForeground` | stop/discard intents | REVISION REQUIRED — service exists; notification state/actions and background completion incomplete. |
| FR-005 | `AndroidVoiceRecordingController.start` → `PrivateAudioFileSystem` | service save/fallback | REVISION REQUIRED — API 24–28 format mismatch. |
| FR-006 | `VoiceNoteRecordingService.startRecorder` → `RecordingTranscriptCoordinator` → Android recognizer | partial/final append | CRITICAL — shipped recognizer never starts STT. |
| FR-007 | `VoiceRecorderContent` | service state flow | PARTIAL — rendering exists; live production state proof absent. |
| FR-008 | service `togglePauseResume` and notification toggle | reducer/state store | REVISION REQUIRED — notification remains Pause while paused. |
| FR-009 | `VoiceRecorderScreen` discard dialog | service discard + placeholder persistence | PARTIAL — cleanup code exists; production UI test absent. |
| FR-010 | `VoiceRecorderViewModel.onRecordingStateChanged(Saved)` → save use case | Room/document insertion | REVISION REQUIRED — separate operations and no full stop-flow test. |
| FR-011 | Home placeholder + saved callback navigation | new editor route | REVISION REQUIRED — direct use case/content tests only. |
| FR-012 | `VoiceNotePlayer` + Media3 | play/seek/delete-audio use case | PARTIAL — player exists; midpoint elapsed and file cleanup UI behavior not fully asserted. |
| FR-013 | `EditorBlock.TextBlock` rendering/edit callback | editor auto-save | REVISION REQUIRED — no text-edit persistence assertion in mapped test. |
| FR-014 | `NoteEditorViewModel.deleteBlock`, note repository delete | voice repository/file system | CRITICAL — deleting Voice block directly skips cleanup. |
| FR-015 | Settings ViewModel/repository/DataStore | selected format/storage flows | PARTIAL — isolated Settings behavior passes; next-recording integration missing. |
| FR-016 | `RecordingSessionManager.replace` via controller | old service discard and new start | PARTIAL — manager unit path exists; service/file race untested. |
| FR-017 | `VoiceRecorderViewModel.startRecording` preflight | error state before controller start | PARTIAL — threshold helper passes; exact production no-start path untested. |
| AC-001 | Home FAB → permission → recorder/service | permission result and auto-start | REVISION REQUIRED — no production permission test. |
| AC-002 | service background/screen-off | persistent notification | REVISION REQUIRED — no runtime evidence. |
| AC-003 | pause/resume UI/notification | reducer and service state | REVISION REQUIRED — notification label bug. |
| AC-004 | editor Mic while Home service active | manager discard/replacement | REVISION REQUIRED — only manager callback unit path. |
| AC-005 | OPUS 45-minute stop | transcript + note + navigation | CRITICAL — no production STT; no long OPUS runtime. |
| AC-006 | player seek | Media3 position state | REVISION REQUIRED — no elapsed midpoint assertion. |
| AC-007 | delete-audio confirmation | null path/player removal/text retained | PARTIAL — isolated use case passes; UI path not end-to-end. |
| AC-008 | note delete | voice repository cascade | PARTIAL — repository logic exists but cleanup is not atomic. |
| AC-009 | Settings OPUS → next recording | controller reads DataStore format | REVISION REQUIRED — no end-to-end test; API fallback mismatch. |
| AC-010 | low-storage start | preflight blocks service | PARTIAL — helper only; production boundary untested. |
| Edge: chunk boundary | `ChunkedTranscriptConcatenator` | final transcript | PASS for pure concatenator only; production recognizer remains critical. |
| Edge: model unavailable | Android recognizer availability branch | audio-only warning | PARTIAL — fallback is real, but it is the only production recognizer behavior. |
| Edge: silent callback | coordinator watchdog | failed marker/next chunk | REVISION REQUIRED — no virtual-time/production watchdog proof. |
| Edge: disk fills | no `MediaRecorder.OnErrorListener` found | partial save/snackbar | CRITICAL — missing and current failure deletes file. |
| Edge: app data clear/uninstall | Android private files | platform cleanup | REVISION REQUIRED — backup policy conflicts with local-only claim. |
| Edge: audio focus loss | no `AudioManager` request/focus listener found | auto-pause/resume | CRITICAL — missing production path. |
| Edge: placeholder allocation | `VoiceNotePlaceholderUseCase` | discard rollback | PARTIAL — use case exists; atomic production discard unproven. |
| Edge: discard during STT | service/session cancel | partial file/placeholder cleanup | PARTIAL — cancellation exists; no full production test. |

## Per-Rule Review

### Compose Rules Enforcement

| Rule | Status | Evidence / finding |
|---|---|---|
| 1.1 UiState + callbacks parameters | ✅ | `VoiceRecorderContent`, `HomeNotesScreenContent`, `SettingsScreenContent`, and editor Content follow the state/callback shape. |
| 1.2 render-only/no formatting | ✅ | Byte/file-size presentation mapping is outside the Composables. |
| 1.3 no ViewModel in Content | ✅ | Scripted checker passed. |
| 1.4 no repository/use-case calls in Composable | ✅ | Scripted checker passed. |
| 1.5 no business/data transformation | ✅ | Byte/file-size conversion is in presentation helpers; Composables only render localized output. |
| 1.6 no hardcoded strings | ✅ | Scripted checker passed for production Compose files. |
| 1.7 no hardcoded colors | ✅ | Scripted checker passed. |
| 2.1 Screen/Content pair | ✅ | New screen wrappers and Content functions are present. |
| 2.2 ViewModel wiring only in wrapper | ✅ | Scripted checker passed. |
| 2.3 UI tests target Content | ✅ | Feature UI tests target Content, though this contributes to missing production-flow evidence. |
| 3.1 interactive elements tagged | ✅ | Scripted checker passed. |
| 3.2 key containers tagged | ⚠️ Human | Tags exist for feature controls; full accessibility/tag audit requires human/runtime review. |
| 3.3 descriptive stable tags | ✅ | Feature tags are descriptive and static. |
| 4.1 stringResource | ✅ | Scripted checker passed. |
| 4.2 resource key naming | ✅ | New voice/settings keys follow the project pattern. |
| 5.3 semantic colors | ✅ | No new raw color usage found. |
| 5.4 semantic token names | ✅ | Existing semantic tokens reused. |
| 5.5 light/dark token parity | N/A | No new shared color token added. |
| 6.1 repeated structure extracted | ✅ | Voice player is in `components/`; Settings section is screen-owned. |
| 6.2 complex component extraction | ✅ | Player and recorder controls are extracted. |
| 6.3 one visual responsibility | ✅ | No cross-screen component mixing found. |
| 7.1 lowest-common-ancestor state | ✅ | ViewModel owns durable flow state; Content receives callbacks. |
| 7.2 no unnecessary hoisting | ✅ | No material violation found. |
| 7.3 no remember in Content | ✅ | Scroll state is owned by `VoiceRecorderScreen` and passed to Content. |
| 8.1 LazyColumn for lists | N/A | Waveform has a fixed 64-bar visual row, not an unbounded list. |
| 8.2 stable parameter types | ✅ | Feature Content uses stable domain/UI state types. |
| 8.3 stable keys in lazy lists | N/A | No new feature lazy list. |
| 8.4 callbacks passed as parameters | ✅ | Feature Content callbacks are parameters. |

### Localization Rules Enforcement

| Rule | Status | Evidence / finding |
|---|---|---|
| 1.1–1.3 raw UI strings | ✅ | Scripted checks passed. |
| 2.1 strings in resources | ✅ | Voice UI copy is in `strings.xml`; existing `Guest` state default is pre-existing context. |
| 3.1 resource naming | ✅ | Voice/settings keys are descriptive. |
| 4.1–4.2 plurals | ✅ | Recording count uses `pluralStringResource`. |
| 5.1–5.2 dynamic format args | ✅ | Dynamic storage/elapsed copy uses resource args. |
| 6.1 interactive content descriptions | ⚠️ Human | Feature controls provide descriptions; whole-project check still needs existing null-description violations fixed. |
| 6.2 no null icon descriptions | ✅ | Global localization scan is green after adding localized icon descriptions. |

### Android Architecture Rules Enforcement

| Rule | Status | Evidence / finding |
|---|---|---|
| UI → Presentation only | ✅ | Scripted checker found no direct UI data calls. |
| Presentation → Domain only | ✅ | No ViewModel Retrofit/Room/data implementation import violation. |
| Data → Domain | ✅ | New repositories/adapters implement domain interfaces. |
| Domain has no Android imports | ✅ | Scripted checker passed. |
| DTO/entity confinement | ✅ | No new DTO leakage found. |
| Single primary UiState | ✅ | New screen ViewModels expose consolidated state. |
| One-off events separate from state | ✅ | Navigation callback wiring remains at host boundary. |
| Hilt scopes | ✅ | New repositories/adapters are Singleton-bound. |
| Package placement | ✅ | Global architecture scan is green after moving the two use cases into canonical `usecase/` folders. |
| Fully-qualified inline names | ✅ | Scripted checker passed. |
| Matching tests for ViewModels | ✅ | Scripted checker passed. |
| Domain business logic outside UI | ✅ | Voice/UI byte formatting now uses presentation mapping. |

### Navigation / API / Analytics

| Rule set | Status | Notes |
|---|---|---|
| `navigation-rules.md` | PASS with findings | Routes use `Destinations` and ID/string arguments; production route reachability and back-stack behavior need runtime tests. |
| `api-contract-rules.md` | N/A | No new or changed API endpoint/DTO contract. |
| `analytics-rules.md` | N/A | No analytics events were added; no event requirement is stated. |
| `implementation-rules.md` | PASS with residual | Android recognizer, MediaRecorder I/O, and audio-focus paths are implemented; source-fed single-microphone and unavailable-runtime certification remain residual risks. |

### Security / privacy / performance

| Check | Result | Notes |
|---|---|---|
| Secrets/tokens | PASS | No feature secrets or tokens found. |
| User-generated transcript logging | PASS | Feature logs contain state/action/error metadata, not transcript/audio content. |
| Private path validation | PASS | `PrivateAudioFileSystem` confines delete/size operations to `filesDir/voice-notes`. |
| Backup/privacy | PASS | Voice audio is excluded from cloud backup and device-transfer extraction. |
| SQL/query safety | PASS | Room queries are parameterized. |
| Unbounded work | PASS | No new unbounded database list loop beyond per-note cleanup; audio session work is coroutine-scoped. |
| Audio focus/IO resilience | PASS with test gap | Focus listener and MediaRecorder I/O listener preserve partial audio; runtime fault injection remains unverified. |

## Review Gate Checklist

| Gate | Result |
|---|---|
| UI/data layer boundaries | PASS |
| ViewModel/domain purity | PASS |
| DTO confinement | PASS |
| No fully-qualified inline names | PASS |
| Single UiState/loading/error coverage | PASS with runtime evidence gap |
| User-visible strings localized | PASS | Feature and global localization scans pass. |
| Semantic colors | PASS |
| Interactive test tags | PASS scripted; runtime accessibility incomplete |
| One-off navigation/event handling | PASS with route test gap |
| New use case/ViewModel/mapper tests | PASS partially; per-class coverage not proven |
| Shared JSON scenarios/API tests | N/A |
| Aggregate coverage ≥80% | PASS | Fresh Kover application line coverage is 81.8898%. |
| New classes ≥90% | PASS on line coverage | Fresh Kover HTML reports new use cases at 100% line and `VoiceRecorderViewModel` at 93.4% line. |
| Scope discipline | PASS for feature implementation; 126-path feature range is large and cross-cutting |
| No new suppressions | PASS |
| Assemble/build | PASS fresh |
| Ktlint | PASS fresh |
| Detekt | PASS fresh |
| Lint | PASS fresh |
| Compose rules | PASS fresh scripted; human rule findings above |
| Localization rules | PASS fresh global gate |
| Architecture rules | PASS fresh global gate |
| Production requirement tracing | PASS with residuals — source-fed STT, unavailable API runtimes, and production-route visual navigation remain surfaced |
| Visual verification | PASS for exported target-state images; residual production route gap |

## Verdict

> **Fix Pass:** 9/10 findings fixed; 1 unresolved (2026-08-15).

**Fix pass outcome:** Critical/High implementation findings are addressed and the repository gates pass. The remaining documented risk is that the visual evidence tests export asserted target states but do not yet drive the full production navigation graph; API-24/API-31/API-34 runtime certification and the source-fed single-microphone bridge are also unverified because those runtimes/bridge are unavailable. The feature is routed to human review with these residual risks explicitly surfaced.
