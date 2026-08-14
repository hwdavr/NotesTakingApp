# Feature Spec — Voice Notes & Audio Transcripts

**Date**: 2026-08-14
**Status**: Final
**Related design**: `design.md`

---

## Objective

Add voice note capture and on-device audio transcription directly into the NotesTakingApp editor and home surface. Users can record audio of any length (quick 10-second memos through multi-hour meetings) using a full-screen immersive recorder. Recordings are transcribed progressively using Android's on-device Speech Recognition engine. Each saved recording becomes a **VoiceNote document block** embedded in the target note: an inline audio player plus the full editable transcript text inserted as a normal rich-text block in the document. Audio files remain 100% on-device in the app's private internal storage.

## User Goal

As a note-taker, I want to record audio directly into my notes and see the live transcript appear as I speak, so that I can capture ideas hands-free while walking/driving and preserve full meeting audio with a searchable transcript for later review.

## Scope

### In Scope

- Unlimited-length audio recording via Android `MediaRecorder` with configurable format (AAC/M4A default, OPUS/OGG toggle in Settings).
- Android Foreground Service with persistent `Recording… [MM:SS]` notification supporting Stop / Pause / Resume action buttons. Recording survives screen-off and app-background state.
- Full-screen immersive Recorder screen with live amplitude waveform bars, elapsed timer, progressive/live transcript preview text, and Pause / Resume / Stop / Discard controls.
- Progressive on-device transcription via chunked `SpeechRecognizer` sessions (60-second chunks) with result concatenation. Supports any recording length.
- Two entry points:
  1. **Note Editor Mic icon** → opens Recorder; on Stop, inserts VoiceNote block at the focused block/caret position in the *currently open* note.
  2. **Home FAB** → shows mini bottom sheet with two options: "Text Note" (existing FAB behavior) / "Record Note" (opens Recorder); on Stop, creates a new untitled note with the VoiceNote block inserted and navigates directly to the Note Editor for that new note.
- New **VoiceNote document block** type (`EditorBlock.Voice`) rendered inline within the note body, containing:
  - Audio player row: Play/Pause button, seekable progress bar, elapsed/total time labels, file-size label, delete-audio (keep transcript) trash action.
  - Editable transcript text block (standard rich-text block immediately following the audio player row; part of the same logical VoiceNote block but rendered as editable text).
- `RECORD_AUDIO` permission flow: pre-rationale informative dialog → system permission request → permanent-deny snackbar with "Open Settings" deep-link action.
- App-wide single active recording session guard. If recording was started via Home→Record and the user taps Editor Mic, the **existing recording is discarded and a new session begins in the editor context** (chosen explicitly; no confirmation dialog on context switch).
- Room persistence: new `voice_note_blocks` table (or extension of the document model) storing per-block metadata: `blockId`, `noteId`, `audioFilePath`, `audioFormat`, `durationMs`, `fileSizeBytes`, `createdAt`.
- App-specific private storage: `files/voice-notes/` subfolder. Filename pattern: `vn_{noteId}_{blockId}_{timestamp}.{m4a|ogg}`.
- Cascade file cleanup: deleting a note OR deleting a VoiceNote block → corresponding audio file deleted from `files/voice-notes/` on disk.
- Settings → Voice Notes section: (a) audio format toggle (AAC ↔ OPUS), (b) total storage used by all voice clips, (c) per-format quality/bps info.

### Out Of Scope

- **Speaker diarization** (who said what) — no speaker labels or identification; single continuous transcript.
- **Cloud storage / encrypted cloud sync of audio files** — audio stays local-only; not included in the future Encrypted Cloud Sync feature's sync set for v1.
- **Transcript search across notes** — transcript text is not indexed by FTS5 in v1; arrives together with the "Offline Full-Text Search" roadmap feature.
- **Playback-to-transcript sync** — on-device STT provides no word-level timestamps, so transcript scrolling / seeking on playback is not implemented.
- **Word-level correction UI** for STT errors — user edits transcript as normal free-form text; no correction confidence UI or alternative word suggestions.
- **Auto-purge of audio after N days** — storage management is fully manual in v1.

---

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---------------|---------|---------|
| `android.media.MediaRecorder` | Platform | Low-level audio capture; outputs AAC (M4A container) or OPUS (OGG container). |
| `android.speech.SpeechRecognizer` | Platform (API 24+) | On-device speech recognition using the device's default RecognitionService; used in chunked 60-second sessions for progressive/live transcription. Must verify `RecognitionInfo#getOnDeviceLanguage()` or fallback to the bundled on-device model. |
| `androidx.media3:media3-exoplayer` | latest stable (1.3.x) | Audio playback with seek support in the inline VoiceNote player; replaces legacy `MediaPlayer` for reliable async playback and state handling. |
| `androidx.core:core-ktx` | latest | Foreground service launch helpers, permission compat, `File` ktx. |
| `androidx.lifecycle:lifecycle-service` | latest | `LifecycleService` base for recording foreground service, enabling coroutine-scoped recording/transcription operations. |
| Hilt / `@AndroidEntryPoint` | Project current | DI into Recorder screen ViewModel and VoiceNoteRecordingService. |

### Key Technical Decisions

- **Progressive STT is an implementation-gated risk**: Android `SpeechRecognizer` sessions have practical per-session limits (~1–5 min depending on the OEM), and the platform API must not be assumed to accept arbitrary `MediaRecorder` file chunks. Before implementation, a technical spike must prove a supported single-microphone capture path that preserves one contiguous audio file while supplying 60-second overlapping windows to the on-device recognizer on API 24, API 31, and the target API. Each verified chunk result is appended into a single running transcript `StateFlow` with cursor position tracking. Do not ship concurrent `MediaRecorder` + `SpeechRecognizer` microphone clients without this proof.
  - Risk: Chunk boundaries may split words, producing artifacts like "…back[60s chunk end]ground …". Mitigation: Chunks overlap 1.5s; deduplicator trims trailing fragment of chunk N and leading fragment of chunk N+1.
- **Single VoiceNote block = audio player UI + following text block (two EditorBlock siblings)**: Rather than inventing a nested block UI, a `Voice` metadata block defines the audio player, immediately followed by the existing `EditorBlock.TextBlock` holding the transcript. This reuses the existing editor rendering and editing pipeline for transcript text without modification.
- **Foreground service for recording state, ViewModel for recorder UI state**: Recording session is owned by a `VoiceNoteRecordingService` (started/stopped via intents) holding the `MediaRecorder`, `SpeechRecognizer` chunk runner, and audio file writer. Recorder screen ViewModel subscribes to a `RecordingSessionState` broadcast / `Flow` from the service and drives UI.
- **App-wide singleton session manager**: `RecordingSessionManager` (singleton in AppModule, `@Singleton`) holds one reference to the active service recording token. Tapping Mic in a different context cancels the old session through this manager and begins a new one — matching the user-confirmed destructive-switch behavior.

### External APIs / Services

- **Android Speech Recognition Service (on-device)**: Provided by the OEM via `SpeechRecognizer.createOnDeviceSpeechRecognizer(context)` when available, or fallback to the default `SpeechRecognizer.createSpeechRecognizer(context)` with a pre-flight check that the language is downloaded for offline use. No network calls; 100% on-device. Rate/quota governed by OEM, not us.

### Platform & Compatibility Constraints

- **Min SDK**: Project default (24) — `SpeechRecognizer` and `MediaRecorder` available since well before API 24. Note: `createOnDeviceSpeechRecognizer()` requires API 31+; gracefully fall back to regular `SpeechRecognizer` on API 24–30 and check offline availability via `RecognizerIntent.EXTRA_PREFER_OFFLINE`.
- **Permissions required**: `RECORD_AUDIO` (runtime permission; prompted with rationale on first use). `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MICROPHONE` (manifest-declared; implicit grant at install time on target SDK 34). `POST_NOTIFICATIONS` (runtime, for foreground service notification visibility on Android 13+).
- **Other constraints**: Microphone hardware must be present (`PackageManager.FEATURE_MICROPHONE`); gracefully disable/hide Mic entry points with explanatory message if no mic. Device must have at least 128 MB of free disk space before allowing a new recording to start (show "Not enough storage" error otherwise).
- **Visual baseline**: All feature mockups and new UI use the project Light Theme tokens (`background = #F8F7FF`, `surface = #FFFFFF`, `primary = #7C6CF2`, `textPrimary = #191627`) from `docs/product/design_system.md`; dark theme remains supported by the shared theme but is not the design baseline.

---

## Functional Requirements

- **FR-001**: System MUST present a Home FAB mini-bottom-sheet with two options ("Text Note", "Record Note") on FAB tap, replacing the previous instant-text-note FAB behavior.
- **FR-002**: System MUST open a full-screen immersive Recorder screen when the user selects "Record Note" from the Home FAB sheet or taps the Mic icon in the Note Editor toolbar.
- **FR-003**: System MUST prompt the user with a `RECORD_AUDIO` pre-rationale dialog before the first recording attempt, then launch the system permission request; on permanent denial the user is shown an inline error with a deep-link to App Settings.
- **FR-004**: System MUST start a microphone foreground service with a persistent "Recording… [elapsed]" notification supporting Stop / Pause / Resume action buttons; the service survives screen-off and process-background state.
- **FR-005**: System MUST record audio in the user-selected format (AAC/M4A default, OPUS/OGG if toggled in Settings) and save to app-private `files/voice-notes/` using filename pattern `vn_{noteId}_{blockId}_{timestamp}.{ext}` (for home-recordings, `noteId` is allocated before recording starts).
- **FR-006**: System MUST run progressive on-device speech recognition in 60-second overlapping chunks and append incremental transcript results to a live transcript preview on the Recorder screen in real time.
- **FR-007**: Recorder screen MUST display live amplitude waveform bars, an MM:SS/HH:MM:SS elapsed timer, pause/stop/discard buttons, and a scrollable live transcript preview area.
- **FR-008**: User MUST be able to Pause and Resume an active recording via the Recorder UI and the notification action buttons; timer and waveform pause during paused state.
- **FR-009**: User MUST be able to Discard an in-progress recording with an explicit "Delete this recording? All unsaved audio and transcript will be lost." confirmation dialog.
- **FR-010**: On Stop (save) from the Note Editor entry point, system MUST insert a new VoiceNote document block pair at the current caret/focused-block position (audio player metadata block + transcript as rich-text block) and persist to Room.
- **FR-011**: On Stop (save) from the Home→Record entry point, system MUST create a new untitled Note with the VoiceNote block inserted at document position 0 and navigate the user directly to the Note Editor screen for the new note.
- **FR-012**: VoiceNote audio player inline block MUST expose Play/Pause toggle, seekable progress bar with current/duration timestamps (MM:SS), file size label, and a trash action that deletes only the audio file from disk while keeping the transcript text block.
- **FR-013**: Transcript text within a VoiceNote MUST be fully editable as a standard rich-text block inside the editor; editing does NOT attempt to preserve playback sync.
- **FR-014**: When a Note or a VoiceNote block is deleted from the editor, system MUST also delete the corresponding audio file from `files/voice-notes/` on disk.
- **FR-015**: A Settings → Voice Notes section MUST display total audio storage used across all voice clips and allow the user to toggle audio capture format between AAC (M4A) and OPUS (OGG).
- **FR-016**: Only one recording session may exist app-wide. If user taps a Mic entry while recording is active in another context, system MUST discard the in-progress recording and start a new session in the newly selected context without a confirmation dialog.
- **FR-017**: Before starting a new recording, system MUST check that at least 128 MB of free internal storage is available and block recording with a descriptive "Not enough available storage" error if below threshold.

## Acceptance Criteria

- **AC-001**: Given no prior permission grant, when user taps Home FAB → Record Note, then app shows "Voice Notes needs microphone access to record" dialog → upon Grant → launches system permission picker → upon Allow → Recorder screen opens and recording starts automatically with waveform and timer ticking.
- **AC-002**: Given an active recording of 2 minutes 15 seconds, when user presses Home button or turns screen off, then the foreground notification persists showing "Recording… 02:15" with Pause and Stop actions.
- **AC-003**: Given a paused recording, when user taps Resume in-app or via notification, then timer resumes from its paused value and waveform continues updating.
- **AC-004**: Given an in-progress recording started from Home FAB → Record, when user navigates to any Note Editor and taps its Mic icon, then the home recording session is silently terminated (audio file deleted, service stopped) and a fresh recording begins in the editor context.
- **AC-005**: Given a 45-minute meeting recording in OPUS format, when user taps Stop, then (a) a new untitled note is created in Room, (b) the full ~45min audio file is persisted at `files/voice-notes/vn_{noteId}_{blockId}_{ts}.ogg`, (c) the concatenated full transcript from all 60-second chunks appears as the editable text block following the audio player row, and (d) app navigates to the editor for the new note.
- **AC-006**: Given a saved VoiceNote block inside a note, when user taps Play and drags the seek bar to the midpoint, then playback jumps to ~50% of total duration and the elapsed time label updates accordingly.
- **AC-007**: Given a saved VoiceNote block, when user taps the trash icon on the audio player row and confirms, then the audio file is deleted from disk, the Voice player row is removed from the document, and the transcript rich-text block remains in place as normal text.
- **AC-008**: Given a note containing two VoiceNote blocks, when the note is deleted, then both corresponding audio files are removed from `files/voice-notes/` within the same transaction/save operation.
- **AC-009**: Given Settings → Voice Notes, when user toggles format from AAC to OPUS and starts a new recording, then the new audio file on disk ends with `.ogg` and the block's `audioFormat` field in Room stores `opus`.
- **AC-010**: Given the device free storage is 50 MB, when user attempts to start a recording, then a blocking error appears: "Not enough available storage. Free up at least 128 MB to record." and no foreground service is started.

## Data And Persistence

### Planning Review Clarifications

- The canonical `VoiceNoteBlock.audioFilePath` is nullable after an audio-only delete; the transcript `EditorBlock.TextBlock` remains in the note document.
- The canonical transcript sibling is the existing `EditorBlock.TextBlock`; `EditorBlock.RichText` is not a type in the current editor model.

- **Room tables**: New `voice_note_block` entity (see Data Layer columns below) and a new `voice_settings` key-value row in app state store (or DataStore preference) for the selected audio format enum `AAC | OPUS`.
- **VoiceNoteBlock columns**: `blockId: String` (PK, UUID), `noteId: String` (FK → Note, indexed), `audioFilePath: String?` (nullable after audio-only deletion), `audioFormat: String` enum ('aac'|'opus'), `durationMs: Long`, `fileSizeBytes: Long`, `sampleRateHertz: Int`, `channels: Int`, `createdAt: Long`, `updatedAt: Long`.
- **NoteDocument/EditorBlock extension**: A new `EditorBlock.Voice(blockId, audioFilePath, format, durationMs, fileSizeBytes, …)` block type pairs one-to-one with the Room row and is immediately followed by the existing `EditorBlock.TextBlock` holding the editable transcript.
- **Transcript storage**: Transcript text lives inside the following `EditorBlock.TextBlock` content (reuses existing Room Note document serialization); no separate transcript column is necessary in `voice_note_block` unless explicit indexing (later FTS5) requires it — leave column out of schema for v1.
- **File system**: `Context.filesDir/voice-notes/` holds all audio files; `Context.getExternalFilesDir()` NOT used (private-only).
- **Cascade cleanup rule**: On `NoteDao.delete(noteId)`, repository iterates `voice_note_block` rows for the note and calls `File(filePath).delete()` for each non-null path. On "remove audio only" action from UI, delete the file and set `audioFilePath = null` (keeping the metadata and transcript text; the player UI hides when the path is null).

## Edge Cases

- **Chunk boundary word-splitting artifact**: STT chunk N ends mid-word, chunk N+1 starts with that word remainder → transcript reads "…back ground…". Mitigation: overlapping 1.5s chunks + trailing/leading trim on concat; if the last 3 words of N match first 3 of N+1, prefer N+1's start to avoid duplicate sentences. If artifacts still occur, user can edit the text manually.
- **SpeechRecognizer on-device model not available / language pack not downloaded**: Pre-flight on Recorder screen open → show warning banner "Offline transcription unavailable for [language]". Recording still works, but no transcript is generated. Live preview area shows "Transcription unavailable — audio will save without text." User can still manually type transcript.
- **SpeechRecognizer partialResult callback silent (OEM broken)**: Implement a timeout watchdog per chunk (65s max); if no final result, flag chunk as `<transcription failed for this segment>` and continue with next chunk — do not abort the entire recording.
- **Disk fills mid-recording**: `MediaRecorder.OnErrorListener` watches for `MEDIA_RECORDER_ERROR_IO` / `MEDIA_ERROR_WRITE_FAILED`; on signal, auto-stop, save what was recorded up to that point, and show "Recording stopped: device storage full. Saved [partial duration]." snackbar.
- **User clears app data / uninstalls**: Audio files in `files/voice-notes/` are automatically cleaned up by Android as part of app-data clear (relied on, no custom logic).
- **Recording while call/Siri active**: `AudioManager` requests `AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE`; if focus is lost (incoming call), recording auto-pauses. Focus regain → auto-resume with notification.
- **Home FAB new note allocation**: Before recording starts from Home→Record, allocate a placeholder Note (title empty, empty document) so `noteId` exists for filename purposes. If user Discards, roll back the placeholder note in the same discard transaction.
- **Discard mid-transcription**: If SpeechRecognizer chunk is running while user hits Discard, cancel the recognizer session, delete the partial audio file, null the placeholder note (Home entry) — fully clean state.

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|------------|---------------|
| A1 | Android `SpeechRecognizer` can sustain 60-second chunks with `EXTRA_PARTIAL_RESULTS` on 95% of target minSdk 24+ devices. | On some OEMs, chunking fails or returns empty; transcript is blank or truncated → fall back to post-recording single-session STT, then to "audio-only, no transcript" banner. |
| A2 | User-selected destructive-switch behavior (no confirm) on context change: "old recording discarded when tapping Mic in another context" is acceptable UX. | Users with long recordings may lose data by accidental navigation → the behavior is user-confirmed and documented in copy; can add a confirmation in v2 if feedback warrants. |
| A3 | `filesDir` is an acceptable location for audio files (not user-accessible via file manager) for v1 given the private-notes positioning. | Users want to share audio via other apps → add "Share Audio File" action in a future version. No change needed now. |
| A4 | 60-second chunk size works well for accuracy (not too short, not too long for Android SpeechRecognizer) across all languages that use Latin/CJK scripts. | Per-language tuning needed later → can expose chunk length in hidden Developer options, but not user-facing for v1. |
| A5 | `MediaRecorder` supports both AAC/M4A and OPUS/OGG output formats on all minSdk 24+ target devices. | OPUS container support added API 24+ but some OEM codecs may fail; recorder pre-flight codec check wraps `MediaRecorder.setAudioEncoder()` with try/catch → on OPUS failure, auto-fallback to AAC and show one-time banner "Your device does not support OPUS; using AAC instead." |
| A6 | A supported single-microphone capture path can provide both the contiguous saved audio file and progressive 60-second on-device STT windows on API 24, API 31, and the target API. | If the spike fails, the implementation must select a supported on-device capture/recognizer adapter before coding the slices; it must not rely on concurrent microphone clients or file input unsupported by `SpeechRecognizer`. |

## Open Questions

All questions have been ✅ Answered before document approval.

| # | Question | Status | Answer |
|---|----------|--------|--------|
| Q1 | Primary user & success definition | ✅ Answered | Both quick on-the-go captures AND long unlimited meetings; success = 1-tap record, progressive live transcript, player inline in notes. |
| Q2 | Entry points | ✅ Answered | Note Editor Mic icon + Home FAB → mini sheet ("Text Note / Record Note") |
| Q3 | Transcription engine | ✅ Answered | On-device Android SpeechRecognizer only; no cloud STT. |
| Q4 | Recording limits & format | ✅ Answered | Unlimited length; configurable AAC (default) / OPUS via Settings. |
| Q5 | Transcript + player integration | ✅ Answered | Editable text block (normal richtext) directly following inline audio player block; no playback-to-text sync. |
| Q6 | Out of scope v1 | ✅ Answered | Speaker diarization, cloud sync of audio files, transcript cross-note search. |
| Q7 | Transcription timing | ✅ Answered | Progressive/live during recording (60s overlapping chunks, concat). |
| Q8 | Home FAB exact flow | ✅ Answered | FAB → mini sheet: "Text Note" (existing) / "Record Note" (new) |
| Q9 | Foreground service notification | ✅ Answered | Ongoing notif: "Recording… [elapsed]" with Stop / Pause / Resume actions. |
| Q10 | Recording controls | ✅ Answered | Pause/Resume + Stop (save) + Discard (with confirmation dialog). |
| Q11 | Storage management UI | ✅ Answered | Per-block file size display + Settings shows total audio storage used; fully manual delete. |
| Q12 | Recorder UI visuals | ✅ Answered | Full-screen immersive: live waveform, elapsed timer, live transcript preview, controls. |
| Q13 | Recorder UI container | ✅ Answered | Full-screen immersive activity/fragment (not bottom sheet, not inline bar). |
| Q14 | Inline player UI saved state | ✅ Answered | Play/Pause, seek bar, elapsed/total time, file size, trash icon. No playback waveform. |
| Q15 | Permission flow | ✅ Answered | Rationale dialog → permission request → permanent deny → snackbar with Settings deep-link. |
| Q16 | Dual entry context conflict | ✅ Answered | New context wins: old recording discarded, new session starts (no confirmation dialog). |
| Q17 | Transcript editing and sync | ✅ Answered | Full editable; sync never implemented (no timestamps available from on-device STT). |
| Q18 | Audio file storage path & naming | ✅ Answered | `files/voice-notes/`; pattern `vn_{noteId}_{blockId}_{ts}.{m4a|ogg}`. App-private only. |

---

## Screen States

| State | Requirement | Acceptance Criteria |
|-------|-------------|---------------------|
| Loading (Recorder open) | Pre-flight checks: mic hardware, free storage ≥ 128 MB, on-device STT availability. Spinner during checks. | FR-017, FR-003; AC-010 |
| Recording — Ready (Pre-Record) | Ready UI shown after all pre-flights pass. "Tap to start recording" big button shown; timer at 00:00; waveform flat. | FR-002; AC-001 |
| Recording — In Progress | Waveform animating live; timer ticking HH:MM:SS; transcript area appending partial results; Pause and Stop buttons active; Discard active. Foreground notification visible. | FR-004, FR-005, FR-006, FR-007; AC-002 |
| Recording — Paused | Waveform frozen; timer paused; "PAUSED" label; Pause becomes Resume button; notification shows Pause→Resume action. Discard and Stop still active. | FR-008; AC-003 |
| Recording — Saving (Post-Stop) | Saving spinner; "Saving audio and transcript…" text; navigates to target note editor on completion. | FR-010, FR-011; AC-005 |
| Empty (no VoiceNote blocks in editor) | Editor shows no VoiceNote blocks. Mic icon in toolbar is enabled and tappable. | FR-002 |
| Content (editor with VoiceNote blocks) | Inline VoiceNote player row renders for each block; transcript renders as following editable richtext. | FR-012, FR-013; AC-006 |
| Error (Storage Full) | Blocking dialog on recording start: "Not enough available storage…" | FR-017; AC-010 |
| Error (No Microphone) | Mic icon is disabled on devices without `FEATURE_MICROPHONE`; tapping any entry shows "No microphone available" snackbar. | Platform Constraints |
| Error (Speech model unavailable) | Recorder screen shows top warning banner: "Offline transcription unavailable for [English (US)]". Recording still proceeds; no transcript generated; user can add text manually. | Edge Cases |

## Navigation

- **Entry (Recorder)**:
  - Path A: `HomeNotesScreen` → tap FAB → `HomeFabMiniSheet` (Text Note | Record Note) → select Record Note → `VoiceRecorderScreen` (full-screen).
  - Path B: `NoteEditorScreen` → tap `MicIconButton` (testTag: `editor_mic_btn`) → `VoiceRecorderScreen` (full-screen), with current `noteId` passed as argument.
- **Back/cancel (Recorder)**:
  - Back gesture or system Back button → triggers same flow as Discard button: "Delete this recording? All unsaved audio and transcript will be lost." confirmation dialog → Discard → returns to the source screen (Home or Editor) without saving.
- **Success (Recorder Stop → Save)**:
  - Path A (Editor entry): returns to `NoteEditorScreen` with VoiceNote block inserted at the focused position + auto-save triggers.
  - Path B (Home → Record entry): navigates forward to `NoteEditorScreen` with the newly created noteId and shows the new note with VoiceNote block at top.
- **Error recovery**:
  - Storage-full pre-flight blocks navigation to Recorder and stays on the current screen.
  - Mid-recording IO/storage-full error → auto-Stop, partial save, navigates to target note editor with whatever audio saved and snackbar explaining truncation.
  - In-progress recording swapped contexts (Home → Editor tap) → old session discarded, new session starts (no navigation back; user stays in-editor).

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|-------------|----------------|---------------------|
| FR-001 (Home FAB mini sheet) | Screen: HomeScreen — FAB Mini Sheet | — |
| FR-002 (Open Recorder) | Screen 1: Voice Recorder — Entry/Exit; Purpose | AC-001 |
| FR-003 (Permission flow) | Screen 1: Accessibility; Interaction Rules | AC-001 |
| FR-004 (Foreground service) | Screen 1: Interaction Rules; Visual States In-Progress | AC-002 |
| FR-005 (Audio format + storage) | Design decisions; Data And Persistence | AC-005, AC-009 |
| FR-006 (Progressive STT chunks) | Technical Spec — Key Decisions; Edge Cases A1/A2/A3 | AC-005 |
| FR-007 (Recorder UI) | Screen 1: Component Inventory; Visual States | AC-002 |
| FR-008 (Pause/Resume) | Screen 1: Interaction Rules; Visual States Paused | AC-003 |
| FR-009 (Discard + confirm) | Screen 1: Interaction Rules — Destructive Actions | — |
| FR-010 (Insert into editor) | Screen 3: Note Editor Inline Block; Navigation Success | AC-005 |
| FR-011 (Home→Record → New note) | Navigation Success; Data And Persistence (placeholder note) | AC-005 |
| FR-012 (Inline player controls) | Screen 3: VoiceNote Player Component; Component Inventory | AC-006 |
| FR-013 (Editable transcript) | Screen 3: Transcript TextBlock | — |
| FR-014 (Cascade file delete) | Data And Persistence; Traceability → Settings/Delete flows | AC-008 |
| FR-015 (Settings section) | Screen 4: Settings — Voice Notes | AC-009 |
| FR-016 (Single session guard) | Technical — RecordingSessionManager | AC-004 |
| FR-017 (Storage pre-flight) | Screen 1: Visual States — Error; Edge Cases | AC-010 |

---

## Verification Expectations

- **Unit**:
  - `ChunkedTranscriptConcatenator` — overlap trim logic, duplicate word removal, chunk boundary handling (parameterized tests with N word overlap fixtures).
  - `VoiceNoteRecordingSessionStateReducer` — transitions (Ready → Recording → Paused → Saving → Saved / Discarded) and illegal-event guards.
  - `AudioFilenameGenerator` — produces correct `vn_{noteId}_{blockId}_{ts}.{ext}` pattern, validates characters, enforces ext from format enum.
  - `RecordingStoragePreflighter` — threshold logic (≥ 128MB = pass, < threshold = fail) with mocked `StatFs`.
  - `Editor` ViewModel integration: `insertVoiceNoteBlockAt(position)` correctly places `EditorBlock.Voice` + following `EditorBlock.TextBlock` pair and leaves caret in the transcript block.
- **Integration**:
  - `VoiceNoteRepositoryImpl` + `NoteRepositoryImpl` → delete note cascades audio file deletion (FakeAudioFileSystem). Delete block-only deletes the file but keeps transcript text.
  - Settings format toggle (DataStore) → next `startRecording()` receives correct encoder and container.
  - Permission flow: permanent deny → snackbar intent launches `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` for the app package.
  - Foreground service start/stop: starting recording posts notification via `NotificationManager`; stopping recording removes the notification and unbinds the service.
- **Instrumented UI**:
  - `VoiceRecorderScreen` happy path: Tap Home FAB → Record Note → permission grant → Start → record 5s → Stop → verify navigates to NoteEditorScreen with a VoiceNote player block + transcript rendered.
  - Inline VoiceNote player: Play → verify elapsed timer advances 2s → tap Pause → verify timer stopped.
  - Recorder discard path: Start → 2s → Discard → confirm dialog → verify back to Home AND `files/voice-notes/` directory is empty AND no placeholder note in DB.
  - Settings format toggle: Navigate to Settings → Voice Notes → switch from AAC → OPUS → back to Recorder → record 3s → stop → assert file extension `.ogg`.
- **Manual/visual**:
  - Compare `VoiceRecorderScreen` implementation to approved `design/mockup_recorder_screen_v3.png` — Light Theme colors, typography, spacing, waveform density, button sizes.
  - Verify inline player matches `design/mockup_editor_voice_block_v3.png` (no playback waveform; seekbar, time labels, file size, trash icon placement).
  - Long recording smoke test: record ≥ 3 minutes in background mode (screen off), unlock → verify foreground notification still alive with correct elapsed.

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved assumptions remain (5 explicit assumptions listed with risk mitigations).
- [x] All visual states are defined in `design.md` and Screen States table above.
- [x] All navigation outcomes are defined in Navigation section above.
