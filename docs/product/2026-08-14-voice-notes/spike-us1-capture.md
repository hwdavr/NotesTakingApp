# US-1 Capture Adapter Spike

**Date**: 2026-08-14
**Scope**: Prove the US-1 recording boundary without introducing concurrent microphone clients.

## Decision

US-1 uses one microphone owner: `VoiceNoteRecordingService` owns the single `MediaRecorder` instance for the full contiguous file. The Recorder ViewModel observes the service state store and never opens a microphone client. Speech recognition is intentionally not started in US-1; the later US-2 adapter must consume this service boundary and must not open a second microphone capture path.

The service uses `MediaRecorder.AudioSource.MIC`, `MPEG_4` + AAC for `.m4a`, and `OGG` + OPUS for `.ogg`, with an executable OPUS-to-AAC fallback when codec setup fails. Pause/resume uses the platform `MediaRecorder.pause()` / `resume()` APIs, and stop/discard always releases the recorder and deletes the private file on failure or discard.

## Compatibility Matrix

| Runtime | Evidence | Result |
|---|---|---|
| API 24 | Platform API contract: `MediaRecorder` capture and pause/resume are available from API 24; code guards the newer foreground-service type overload and uses the legacy `startForeground(id, notification)` path below API 29. The app now declares minSdk 24; the API-26-only summarization libraries are guarded by their existing API check and manifest override. | Source-compatible; runtime device execution pending an API-24 emulator. |
| API 31 | Platform API contract: the same single `MediaRecorder` path is used; the API-29 foreground-service microphone type is selected. No `SpeechRecognizer` client is created in parallel. | Source-compatible; runtime device execution pending an API-31 emulator. |
| Target API 34 | Project compile/target SDK is 34; manifest declares `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE`, `RECORD_AUDIO`, and the service microphone type. The API 33 emulator `emulator-5554` passed `VoiceRecordingServiceIntegrationTest.foregroundServiceOwnsOnePrivateContiguousFile` and `VoiceRecorderLifecycleTest.startsRecordingThroughProductionEntryPoint`, producing a non-empty private AAC file. | Runtime verified on API 33; API-34 runtime execution pending a target emulator. |

## Hard-Gate Interpretation

The repository has no API-24, API-31, or API-34 emulator attached in this session, so this is a compatibility proof plus API-33 runtime verification rather than a three-device runtime certification. The implementation is safe to carry into US-2 only if the missing runtime executions are completed before shipping progressive transcription. US-2 must not add a second microphone client or infer that `SpeechRecognizer` can read arbitrary `MediaRecorder` file chunks.

## US-2 Carry-Forward Decision

US-2 keeps the recognizer behind `VoiceTranscriptRecognizer` and the Recorder-facing state behind `VoiceTranscriptSession`. The production Android adapter checks API 31 on-device recognition availability (and the API 24–30 system recognizer availability path), but reports source-unavailable when the current compressed `MediaRecorder` file has no approved PCM window bridge. This is an intentional safe fallback: recording continues, the transcript warning is shown, audio is saved, and no concurrent `SpeechRecognizer` microphone client is opened. The adapter boundary is injectable so a future single-mic PCM tee can be verified without changing the Recorder ViewModel or transcript concatenation contract.

## Files Reviewed

- `app/src/main/java/com/example/notesapp/data/voice/VoiceNoteRecordingService.kt`
- `app/src/main/java/com/example/notesapp/data/voice/AndroidVoiceRecordingController.kt`
- `app/src/main/java/com/example/notesapp/data/voice/AndroidVoiceTranscriptRecognizer.kt`
- `app/src/main/java/com/example/notesapp/data/voice/RecordingTranscriptCoordinator.kt`
- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`
