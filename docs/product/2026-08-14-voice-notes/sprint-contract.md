# Sprint Contract — Voice Notes & Audio Transcripts

## 🏃 Sprint Overview

*   **Sprint:** P01-01
*   **Feature:** Voice Notes & Audio Transcripts
*   **Duration:** Five vertical slices, one implementation session per slice, with the US-1 capture/STT feasibility spike as a hard gate before production coding.

### Complexity and dependency assessment

- **Distinct codebase areas touched:** Home navigation/FAB, Recorder UI and lifecycle service, Android microphone/STT platform adapters, Room/data repositories and private-file cleanup, editor document mapping and Media3 playback, Settings/DataStore, accessibility/test-tag contracts, and visual verification.
- **Natural dependency order:** US-1 establishes the supported recording/session/file contract; US-2 consumes that adapter for progressive transcript events; US-3 wires production Home/editor entry contexts; US-4 persists and renders the audio-plus-TextBlock document pair; US-5 adds settings behavior and verifies the completed Light Theme flow.
- **Highest technical risk:** The current plan must not assume that Android SpeechRecognizer can read MediaRecorder file chunks while another microphone client is active. US-1 must prove a supported single-microphone path on API 24, API 31, and the target API before US-2 is implemented.

## 🎯 Scope

### In Scope

*   [ ] Prove and document a supported single-microphone capture/STT adapter across API 24, API 31, and the target API; stop or re-plan if the platform cannot support the assumption.
*   [ ] Implement unlimited private audio recording with foreground-service lifecycle, AAC/OPUS selection, storage/permission/microphone preflights, notification actions, pause/resume, save, discard, and cleanup.
*   [ ] Implement progressive on-device transcript preview, overlap deduplication, timeout/unavailable fallbacks, and cancellation.
*   [ ] Add Home FAB mini-sheet and Note Editor bottom-toolbar Mic entry points with placeholder-note allocation and the single-session context guard.
*   [ ] Persist VoiceNote metadata in Room and render the EditorBlock.Voice audio player paired with the existing EditorBlock.TextBlock transcript.
*   [ ] Implement Media3 playback, seek, audio-only deletion, note/block cascade cleanup, and editable transcript behavior.
*   [ ] Add Settings Voice Notes format/storage section and final Light Theme integration/visual verification against the four v3 mockups.
*   [ ] Add unit, integration, instrumented, accessibility, state-restoration, and final visual evidence required by the mapped acceptance tests.

### Out of Scope

*   *   Speaker diarization and speaker labels (explicit v1 non-goal).
*   *   Cloud storage, encrypted cloud sync of audio files, and cross-device audio replication (explicit v1 non-goal).
*   *   Transcript search/indexing across notes (deferred to Offline Full-Text Search).
*   *   Playback-to-transcript synchronization, word-level correction UI, playback speed/skip/volume controls, and recorder-side playback preview (explicit design/spec boundaries).
*   *   Automatic audio purge, Settings per-clip deletion, and user-accessible audio sharing (explicit v1 boundaries).
*   *   User-tunable bitrate/sample-rate/chunk-length controls (fixed sensible defaults and implementation detail in v1).

## Spec Coverage Matrix (required)

| Source requirement | Requirement summary | Primary user story | Primary acceptance test | Handling |
|---|---|---|---|---|
| FR-001 | Home FAB opens a two-option Text Note / Record Note mini-sheet | US-3 | TC-US-3-01 | In scope |
| FR-002 | Home Record Note and editor Mic open the full-screen Recorder | US-3 | TC-US-3-01 | In scope |
| FR-003 | Rationale, system permission, and permanent-denial Settings recovery | US-1 | TC-US-1-01 | In scope |
| FR-004 | Foreground service and persistent Stop/Pause/Resume notification | US-1 | TC-US-1-02 | In scope |
| FR-005 | Selected AAC/OPUS format, private path, and filename contract | US-1 | TC-US-1-02 | In scope |
| FR-006 | Progressive overlapping on-device STT and live append | US-2 | TC-US-2-01 | In scope |
| FR-007 | Recorder waveform, timer, transcript preview, and controls | US-1 | TC-US-1-01 | In scope |
| FR-008 | Pause/resume from Recorder and notification with timer/waveform state | US-1 | TC-US-1-02 | In scope |
| FR-009 | Discard with explicit destructive confirmation and cleanup | US-1 | TC-US-1-02 | In scope |
| FR-010 | Editor-entry Stop inserts Voice metadata plus transcript TextBlock at focus | US-4 | TC-US-4-01 | In scope |
| FR-011 | Home-entry Stop creates untitled Note, inserts at position 0, and navigates to editor | US-4 | TC-US-4-01 | In scope |
| FR-012 | Inline player exposes play/pause, seek, time, size, and audio-only trash | US-4 | TC-US-4-02 | In scope |
| FR-013 | Transcript remains a fully editable standard rich-text TextBlock | US-4 | TC-US-4-01 | In scope |
| FR-014 | Note/block deletion removes corresponding private audio files | US-4 | TC-US-4-03 | In scope |
| FR-015 | Settings shows total storage and AAC/OPUS toggle | US-5 | TC-US-5-01 | In scope |
| FR-016 | Only one session; new context silently discards old and starts new | US-3 | TC-US-3-02 | In scope |
| FR-017 | 128 MB storage preflight blocks start with descriptive error | US-1 | TC-US-1-03 | In scope |
| AC-001 | First Home Record flow grants permission and starts recording | US-1 | TC-US-1-01 | In scope |
| AC-002 | Background/screen-off notification remains at elapsed time with actions | US-1 | TC-US-1-02 | In scope |
| AC-003 | Resume restores timer and waveform from paused value | US-1 | TC-US-1-02 | In scope |
| AC-004 | Editor Mic context switch discards Home session and starts fresh | US-3 | TC-US-3-02 | In scope |
| AC-005 | 45-minute OPUS stop saves file/transcript/new note and navigates to editor | US-4 | TC-US-4-01 | In scope |
| AC-006 | Saved player seek moves playback to midpoint and updates elapsed time | US-4 | TC-US-4-02 | In scope |
| AC-007 | Audio-only delete removes player/file but keeps transcript | US-4 | TC-US-4-03 | In scope |
| AC-008 | Note delete cascades all VoiceNote audio files | US-4 | TC-US-4-03 | In scope |
| AC-009 | Settings OPUS toggle affects extension and Room audioFormat | US-5 | TC-US-5-01 | In scope |
| AC-010 | 50 MB preflight blocks start and does not start service | US-1 | TC-US-1-03 | In scope |
| Edge case: chunk boundary | Overlapping windows and deduplication prevent repeated/split transcript fragments | US-2 | TC-US-2-01 | In scope |
| Edge case: model unavailable | Warn and save audio-only without aborting recording | US-2 | TC-US-2-02 | In scope |
| Edge case: silent STT callback | 65-second watchdog marks segment failure and continues | US-2 | TC-US-2-02 | In scope |
| Edge case: disk fills mid-recording | Auto-stop, preserve partial audio, and show saved-duration feedback | US-1 | TC-US-1-02 | In scope |
| Edge case: app data clear/uninstall | Rely on Android private app-data cleanup; no export/sync path is introduced | US-4 | TC-US-4-03 | In scope |
| Edge case: audio focus loss | Auto-pause on focus loss and resume on regain with notification state | US-1 | TC-US-1-02 | In scope |
| Edge case: Home placeholder allocation | Allocate before recording and roll back on discard | US-3 | TC-US-3-01 | In scope |
| Edge case: discard during STT | Cancel recognizer, delete partial audio, and remove placeholder note | US-2 | TC-US-2-02 | In scope |
| NFR: API compatibility | Support minSdk 24; branch API 31 on-device recognizer and API 24–30 offline fallback | US-1 | TC-US-1-01 | In scope |
| NFR: private local storage | Keep audio under files/voice-notes; no cloud or external-files path | US-1 | TC-US-1-02 | In scope |
| NFR: background resilience | Foreground service owns capture across screen-off/background/configuration change | US-1 | TC-US-1-02 | In scope |
| NFR: accessibility | Copy, semantics, live-region, contrast, 48dp targets, and dynamic scaling match design contract | US-5 | TC-US-5-02 | In scope |
| NFR: single active session | Singleton manager serializes all contexts and cleans old files | US-3 | TC-US-3-02 | In scope |
| Verification: unit | Reducer, concatenator, filename, storage preflight, and editor insertion tests | US-1 | TC-US-1-03 | In scope |
| Verification: integration | Repository cascade, settings persistence, permission recovery, and service notification tests | US-4 | TC-US-4-03 | In scope |
| Verification: instrumented UI | Recorder happy/discard, player controls, and settings toggle production flows | US-4 | TC-US-4-01 | In scope |
| Verification: manual/visual | Compare Recorder, Editor player, Home sheet, Settings, plus background smoke against approved designs | US-5 | TC-US-5-02 | In scope |
| Design: Light Theme baseline | Use updated design-system tokens and existing component families; no approved exception | US-5 | TC-US-5-02 | In scope |
| Design: Recorder | Full-screen light recorder, waveform/status/timer/transcript and bottom controls | US-1 | TC-US-1-01 | In scope |
| Design: Home sheet | Existing Home shell and M3 modal sheet with two tiles and stable tags | US-3 | TC-US-3-01 | In scope |
| Design: Editor block | Existing editor toolbar Mic plus bordered player and following TextBlock | US-4 | TC-US-4-02 | In scope |
| Design: Settings | Existing Settings shell with Voice Notes storage card and AAC/OPUS segmented control | US-5 | TC-US-5-01 | In scope |
| Design: state/configuration | Loading, ready, recording, paused, saving, empty/content/error states and rotation/background resilience | US-1 | TC-US-1-02 | In scope |
| Out of scope: speaker diarization | No labels or speaker identification in v1 | — | — | Out of scope by approved spec |
| Out of scope: cloud audio sync | Audio remains local-only in v1 | — | — | Out of scope by approved spec |
| Out of scope: transcript cross-note search | No FTS indexing in this feature | — | — | Out of scope by approved spec |
| Out of scope: playback sync/correction | No timestamps or word-level correction UI | — | — | Out of scope by approved spec |

## User Scenarios & Testing (mandatory)

### US-1: Prove and persist a safe recording session (Priority: P1)

The user opens the production Recorder, completes the microphone/storage preflight, records with a foreground service, pauses/resumes if needed, and either saves a private audio file or discards it with explicit confirmation. The implementation first proves the selected single-microphone capture/STT-window adapter on API 24, API 31, and the target API.

**Why this priority**: Every later slice depends on a real, lifecycle-safe recording and file contract. The platform capture assumption is the highest technical risk and must be resolved before building transcript or editor behavior.

**Independent Test**: Launch the Recorder production entry with a deterministic test fixture, grant microphone permission, start/advance a fake clock, exercise Pause/Resume and Stop or Discard, then assert service/notification/file/session state and the visible recorder state.

**Acceptance Criterion**:

1. **AC-US-1-01 Given** the supported capture adapter has passed the API 24/API 31/target-API spike and microphone permission/preflight pass, **When** the user starts recording, **Then** the full-screen Recorder enters Recording state with timer/waveform/control semantics and the foreground service owns a contiguous private audio file.
2. **AC-US-1-02 Given** an active session, **When** the user pauses/resumes, backgrounds the app, or stops/discards including an IO/focus event, **Then** notification and state transitions remain correct, save/discard cleanup is deterministic, and the session cannot leave an orphan file.
3. **AC-US-1-03 Given** microphone permission is permanently denied, no microphone exists, or free storage is below 128 MB, **When** the user attempts to start, **Then** the specified recovery/error state is shown and no recording service starts.

**Acceptance Test Cases (required for implementation authorization)**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-1-01 | AC-US-1-01 | Instrumented UI + service integration | app/src/androidTest/java/com/example/notesapp/voice/VoiceRecorderLifecycleTest.kt#startsRecordingThroughProductionEntryPoint | On API 24/API 31/target fixtures, grant permission and pass storage/mic preflight, then start through Recorder production route | Assert Recorder is Recording, timer/waveform/control semantics exist, service is foreground, one contiguous private file is open, and spike evidence is present | ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.voice.VoiceRecorderLifecycleTest#startsRecordingThroughProductionEntryPoint |
| TC-US-1-02 | AC-US-1-02 | JVM state/repository integration + instrumented service | app/src/test/java/com/example/notesapp/voice/VoiceRecordingSessionStateReducerTest.kt#pauseResumeBackgroundStopAndDiscard | Drive active/paused/focus-loss/IO-error/stop/discard events with fake clock and FakeAudioFileSystem; cover notification action intents in the service test | Assert legal transitions, paused elapsed value, auto-pause/resume, partial-save copy, notification actions, file cleanup, and no orphan session after discard | ./gradlew testDebugUnitTest --tests "com.example.notesapp.voice.VoiceRecordingSessionStateReducerTest" |
| TC-US-1-03 | AC-US-1-03 | JVM unit + instrumented permission/storage UI | app/src/test/java/com/example/notesapp/voice/RecordingStoragePreflighterTest.kt#blocksBelowThreshold | Use StatFs fixtures for 50 MB/127 MB/128 MB, no-microphone fixture, and permanent-denial permission fixture | Assert exact error copy, Settings deep-link intent for permanent denial, and foreground service is not started on failed preflight | ./gradlew testDebugUnitTest --tests "com.example.notesapp.voice.RecordingStoragePreflighterTest" |

### US-2: Show progressive transcription with safe fallback (Priority: P2)

The user sees the transcript preview grow during recording. Overlapping chunk results are concatenated without avoidable duplicates; unavailable models and silent OEM callbacks degrade to audio-only capture with clear copy instead of losing the recording.

**Why this priority**: Progressive STT is the feature’s differentiator, but it must consume the supported capture contract from US-1 and remain safe when OEM recognition behavior is incomplete.

**Independent Test**: Inject a deterministic recognizer adapter into the production Recorder ViewModel/service boundary, emit partial/final/timeout/model-unavailable events, and assert the visible StateFlow plus saved transcript output.

**Acceptance Criterion**:

1. **AC-US-2-01 Given** a supported recording session receiving overlapping partial/final results, **When** successive 60-second windows complete, **Then** the Recorder preview and final transcript append deduplicated text in order.
2. **AC-US-2-02 Given** a missing model, silent callback, or discard during recognition, **When** the failure/watchdog/cancel event occurs, **Then** recording continues or cleans up as specified, the exact fallback copy is visible, and no stale recognizer/file remains.

**Acceptance Test Cases (required for implementation authorization)**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-2-01 | AC-US-2-01 | JVM integration through Recorder ViewModel | app/src/test/java/com/example/notesapp/voice/VoiceRecorderTranscriptIntegrationTest.kt#appendsOverlappingChunksThroughProductionViewModel | Start production ViewModel with fake 60-second partial/final events whose boundaries overlap by 1.5 seconds | Assert live preview StateFlow and final saved transcript have ordered words, trimmed overlap, preserved partial text, and no duplicate boundary sentence | ./gradlew testDebugUnitTest --tests "com.example.notesapp.voice.VoiceRecorderTranscriptIntegrationTest" |
| TC-US-2-02 | AC-US-2-02 | JVM integration + instrumented UI | app/src/test/java/com/example/notesapp/voice/VoiceRecorderTranscriptIntegrationTest.kt#fallsBackAndCancelsCleanly | Emit unavailable-model, 65-second timeout, and discard-while-recognizing events through the production adapter boundary | Assert warning/banner copy, audio-only continuation, failed-segment marker, recognizer cancellation, partial-file deletion, and placeholder rollback | ./gradlew testDebugUnitTest --tests "com.example.notesapp.voice.VoiceRecorderTranscriptIntegrationTest" |

### US-3: Start recording from Home or the editor (Priority: P3)

The user taps Home’s add button to choose Text Note or Record Note, or taps the editor’s bottom-toolbar Mic. The selected source context is carried into the Recorder, and a new context always replaces the old active session through the singleton guard.

**Why this priority**: Entry-point clarity and context ownership are required before a saved recording can be inserted into the correct note without data loss.

**Independent Test**: Run production navigation from Home and Note Editor with deterministic repository/session fakes, assert stable test tags and destination arguments, then trigger the context-switch route and inspect cleanup.

**Acceptance Criterion**:

1. **AC-US-3-01 Given** Home is visible, **When** the user taps home_add_fab and chooses Record Note, **Then** the light M3 sheet is shown, a placeholder note is allocated before capture, and Recorder opens with Home source context; choosing Text Note preserves the existing route.
2. **AC-US-3-02 Given** a Home recording is active, **When** the user opens a Note Editor and taps editor_mic_btn, **Then** the old session/file/placeholder is silently discarded and a new editor-context recording starts without a confirmation dialog.

**Acceptance Test Cases (required for implementation authorization)**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-3-01 | AC-US-3-01 | Instrumented UI + Room/navigation integration | app/src/androidTest/java/com/example/notesapp/navigation/VoiceEntryNavigationTest.kt#homeRecordAllocatesPlaceholderAndOpensRecorder | Tap production Home add button, assert sheet, select Record Note, and inspect repository/navigation state | Assert all stable test tags, exact tile copy, Recorder destination/source arguments, placeholder note before service start, and unchanged Text Note route | ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.navigation.VoiceEntryNavigationTest#homeRecordAllocatesPlaceholderAndOpensRecorder |
| TC-US-3-02 | AC-US-3-02 | JVM session integration + instrumented UI | app/src/test/java/com/example/notesapp/voice/RecordingSessionManagerTest.kt#replacesHomeSessionWhenEditorMicStarts | Start Home session, navigate to editor production route, tap bottom-toolbar Mic, and observe manager/service events | Assert exactly one active token, old service stopped, old audio/placeholder deleted without confirmation, and new session has editor noteId/focus context | ./gradlew testDebugUnitTest --tests "com.example.notesapp.voice.RecordingSessionManagerTest" |

### US-4: Save and edit inline VoiceNote blocks (Priority: P4)

The user stops from either source context and sees a saved audio player paired with an editable transcript TextBlock in the correct note. Playback, seeking, audio-only deletion, full note deletion, and orphan-file recovery are deterministic.

**Why this priority**: This is the durable product value: captured audio and transcript become normal, editable note content with reliable local cleanup.

**Independent Test**: Seed Room with a note and fake private audio file, invoke the production save/insertion repository path, navigate to the editor, exercise player/delete/note-delete actions, and assert Room/document/file outcomes.

**Acceptance Criterion**:

1. **AC-US-4-01 Given** a stopped recording with selected format and transcript, **When** save completes from editor or Home, **Then** the correct Voice metadata plus following TextBlock is persisted at the requested position and Home flow creates/navigates to the new note.
2. **AC-US-4-02 Given** a saved player with an available audio file, **When** the user plays and seeks, **Then** Media3 updates playback and elapsed/total labels reflect the requested position.
3. **AC-US-4-03 Given** a saved VoiceNote or note, **When** the user deletes audio-only, a block, or the full note, **Then** prescribed player/transcript/file cascade semantics are applied, including missing-file recovery.

**Acceptance Test Cases (required for implementation authorization)**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-4-01 | AC-US-4-01 | JVM repository/editor integration + instrumented UI | app/src/test/java/com/example/notesapp/editor/EditorVoiceNoteInsertionTest.kt#savesBothEntryContextsThroughProductionUseCase | Seed focused editor document and Home placeholder, stop a fake OPUS recording with a full transcript, then invoke production save use case | Assert Room metadata/path/opus, Voice metadata block plus following EditorBlock.TextBlock at focus or position 0, editable text, and Home navigation noteId | ./gradlew testDebugUnitTest --tests "com.example.notesapp.editor.EditorVoiceNoteInsertionTest" |
| TC-US-4-02 | AC-US-4-02 | Instrumented UI with Media3 test asset | app/src/androidTest/java/com/example/notesapp/editor/VoiceNoteEditorFlowTest.kt#playsAndSeeksInlineVoicePlayer | Open production editor seeded with deterministic short audio asset, tap play, drag seek slider to midpoint, then pause | Assert player card/test tags, Play→Pause state, midpoint seek tolerance, elapsed label update, total label, and file-size label | ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.editor.VoiceNoteEditorFlowTest#playsAndSeeksInlineVoicePlayer |
| TC-US-4-03 | AC-US-4-03 | JVM repository integration + instrumented editor UI | app/src/test/java/com/example/notesapp/voice/VoiceNoteRepositoryIntegrationTest.kt#deletesAudioOnlyAndCascadesNoteFiles | Seed two VoiceNote rows/files plus transcript TextBlocks, invoke audio-only delete, block delete, note delete, and missing-file delete | Assert nullable audioFilePath, transcript preservation, player removal, both files deleted in same repository operation, orphan dismissal, and no external/cloud path | ./gradlew testDebugUnitTest --tests "com.example.notesapp.voice.VoiceNoteRepositoryIntegrationTest" |

### US-5: Configure Voice Notes and verify the completed Light Theme flow (Priority: P5)

The user configures format and sees storage totals in the existing Settings shell. After all prior slices are complete, deterministic production navigation reaches the Home sheet, Recorder, Editor VoiceNote, and Settings states for final review against the updated Light Theme design system and v3 mockups.

**Why this priority**: Settings is the final configuration surface and the final visual gate should assess the integrated product rather than an isolated intermediate Composable.

**Independent Test**: Toggle format through production Settings, assert DataStore/repository output on the next recording, then run the four state-verifying screenshot tests from production navigation and compare saved evidence with design.md.

**Acceptance Criterion**:

1. **AC-US-5-01 Given** Settings → Voice Notes is open, **When** the user toggles AAC/OPUS, **Then** the selection persists, storage totals and format-quality copy are visible, and the next recording uses the selected container/Room enum.
2. **AC-US-5-02 Given** US-1 through US-4 are passing, **When** the final visual-flow test navigates through Home, Recorder, Editor, and Settings, **Then** each target state is deterministically asserted before capture and the UI chrome follows the updated Light Theme tokens, component families, spacing, copy, accessibility, and placement in design.md.

**Acceptance Test Cases (required for implementation authorization)**:

| Test ID | Covers AC | Test layer | Test file and method | Setup and action | Required assertions | Exact command |
|---|---|---|---|---|---|---|
| TC-US-5-01 | AC-US-5-01 | JVM DataStore/repository integration + instrumented UI | app/src/test/java/com/example/notesapp/settings/VoiceSettingsRepositoryTest.kt#persistsFormatAndReportsStorage | Seed voice files, open production Settings state, toggle AAC→OPUS, then invoke next recording configuration | Assert immediate persisted enum, total bytes/count, exact AAC/OPUS helper copy, and subsequent .ogg/opus configuration | ./gradlew testDebugUnitTest --tests "com.example.notesapp.settings.VoiceSettingsRepositoryTest" |
| TC-US-5-02 | AC-US-5-02 | Instrumented UI + manual visual review | app/src/androidTest/java/com/example/notesapp/voice/VoiceNotesVisualFlowTest.kt#allTargetStatesAreReachableAndAsserted | Navigate production Home→Recorder→Stop→Editor and existing Settings route, asserting state and accessibility/test-tag contracts before each screenshot | Assert all four target states are reachable, stable Light Theme tokens/copy/semantics are present, and screenshot rows below have target-state proof | ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.voice.VoiceNotesVisualFlowTest#allTargetStatesAreReachableAndAsserted |
| TC-US-5-VIS-001 | AC-US-5-02 | Visual verification | app/src/androidTest/java/com/example/notesapp/voice/VoiceNotesVisualFlowTest.kt#recorderInProgressLightTheme | Assert production Recorder is in Recording state with timer, waveform, transcript region, and bottom controls, then capture | Save non-empty $FEATURE_DIR/visual_evidence/recorder_in_progress_light.png; compare UI chrome to mockup_recorder_screen_v3.png and design.md | ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.voice.VoiceNotesVisualFlowTest#recorderInProgressLightTheme && mkdir -p "$FEATURE_DIR/visual_evidence" && adb exec-out screencap -p > "$FEATURE_DIR/visual_evidence/recorder_in_progress_light.png" && test -s "$FEATURE_DIR/visual_evidence/recorder_in_progress_light.png" |
| TC-US-5-VIS-002 | AC-US-5-02 | Visual verification | app/src/androidTest/java/com/example/notesapp/voice/VoiceNotesVisualFlowTest.kt#homeFabSheetLightTheme | Assert Home is visible and the standard Create sheet is open with both tiles and stable tags, then capture | Save non-empty $FEATURE_DIR/visual_evidence/home_fab_sheet_light.png; compare Home shell/sheet/tile UI chrome to mockup_home_fab_sheet_v3.png and design.md | ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.voice.VoiceNotesVisualFlowTest#homeFabSheetLightTheme && mkdir -p "$FEATURE_DIR/visual_evidence" && adb exec-out screencap -p > "$FEATURE_DIR/visual_evidence/home_fab_sheet_light.png" && test -s "$FEATURE_DIR/visual_evidence/home_fab_sheet_light.png" |
| TC-US-5-VIS-003 | AC-US-5-02 | Visual verification | app/src/androidTest/java/com/example/notesapp/voice/VoiceNotesVisualFlowTest.kt#editorVoiceBlockLightTheme | Assert production editor shows bottom Mic action and a Voice player card followed by editable TextBlock, then capture | Save non-empty $FEATURE_DIR/visual_evidence/editor_voice_block_light.png; compare player/toolbar/editor chrome to mockup_editor_voice_block_v3.png and design.md | ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.voice.VoiceNotesVisualFlowTest#editorVoiceBlockLightTheme && mkdir -p "$FEATURE_DIR/visual_evidence" && adb exec-out screencap -p > "$FEATURE_DIR/visual_evidence/editor_voice_block_light.png" && test -s "$FEATURE_DIR/visual_evidence/editor_voice_block_light.png" |
| TC-US-5-VIS-004 | AC-US-5-02 | Visual verification | app/src/androidTest/java/com/example/notesapp/voice/VoiceNotesVisualFlowTest.kt#settingsVoiceNotesLightTheme | Assert production Settings is scrolled to Voice Notes with storage card and AAC/OPUS toggle, then capture | Save non-empty $FEATURE_DIR/visual_evidence/settings_voice_notes_light.png; compare Settings shell/card/toggle chrome to mockup_settings_voice_v3.png and design.md | ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.voice.VoiceNotesVisualFlowTest#settingsVoiceNotesLightTheme && mkdir -p "$FEATURE_DIR/visual_evidence" && adb exec-out screencap -p > "$FEATURE_DIR/visual_evidence/settings_voice_notes_light.png" && test -s "$FEATURE_DIR/visual_evidence/settings_voice_notes_light.png" |

### Verification Rules

1. Every acceptance criterion has one primary test row; secondary rows are used only for distinct visual states.
2. Cross-layer criteria exercise a production route or production ViewModel/repository boundary with fakes at external/platform seams.
3. Visual evidence is owned only by US-5, the final user-reachable slice. Each screenshot command proves its target state before capture and writes a non-empty file under $FEATURE_DIR/visual_evidence.
4. The Generator records command, exit status, target-state proof, and evidence path before marking any slice passing. The Evaluator compares UI chrome, not recorded media content, against the v3 mockups and docs/product/design_system.md.

## 📊 Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| **Planning** | Planner | sprint-contract.md compiled | Five vertical slices; US-1 is the capture/STT feasibility and lifecycle gate; US-5 is the sole visual owner. |
| **Implementation** | Generator | Pending implementation approval | No production code is authorized in this planning stage. |
| **Review 1** | Evaluator | Pending | |
| **Revision 1** | Generator | Pending | |
| **Final Review** | Evaluator | Pending | |
