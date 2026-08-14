# Session Handoff

## Verified Now

- What is currently working: US-1 exposes the production Recorder route, requests/recoveries microphone permission, blocks insufficient storage, owns one private AAC/OPUS recording through a foreground `LifecycleService`, supports pause/resume/stop/discard cleanup, and maps service state into stable recorder UI controls and test tags. The API-33 emulator produced a non-empty private AAC file through the instrumented service test.
- What verification actually ran: `assembleDebug`, full `testDebugUnitTest`, `koverLog` (82.5092% overall line coverage), Kover HTML report, `ktlintCheck`, `detekt`, `lintDebug`, Compose/localization/architecture scripts, exact feature-list verification commands, and API-33 Recorder UI/service instrumented tests. All in-scope feature and build checks passed.

## Changed This Session

- Code or behavior added: Domain recording contracts/reducer/session guard, filename and storage preflight rules, Android private file/microphone adapters, Hilt controller and state store, foreground service with notification actions and codec fallback, Recorder ViewModel/render-state mapper/Compose screen, navigation route, manifest permissions, localized copy, and JVM/instrumented tests.
- Infrastructure or harness changes: `minSdk` is 24 with explicit manifest library compatibility overrides for existing API-26 AI dependencies. The MediaRecorder service is isolated in `data.voice.service` and excluded from JVM Kover because its framework lifecycle is verified by connected service tests. Product tracker/capability docs and US-1 evidence were updated. Implementation commit: `3fb6066`.

## Broken Or Unverified

- Known defect: None identified in the US-1 implementation or its required verification commands.
- Unverified path: API-24, API-31, and API-34 runtime capture certification remains pending; only `emulator-5554` (API 33) was available. The spike records source compatibility and the API-33 result. Progressive STT, Home/editor entry points, editor persistence/playback, and settings are intentionally deferred to US-2 through US-5.
- Risk for the next session: Do not add a second microphone client or make SpeechRecognizer consume MediaRecorder chunks until US-1's single-microphone boundary is preserved and the next adapter is proven.
- Baseline checklist findings preserved without unrelated edits: repository-wide localization script reports four existing null icon descriptions in `ui/folders`, `ui/editor`, and `ui/common`; architecture script reports two existing use-case files outside `domain/*/usecase/`. They were not introduced by this slice.

## Next Best Step

- Highest-priority unfinished feature: US-2 — Show progressive transcription with safe fallback.
- Why it is next: US-2 consumes the capture boundary established by US-1 and owns recognizer windows, timeout/model-unavailable fallback, and transcript preview.
- What counts as passing: Implement the injectable STT adapter without a concurrent unsupported microphone client; pass the approved US-2 acceptance commands, overall/ViewModel coverage gates, and connected fallback test.
- What must not change during that step: Keep `RecordingSessionManager` as the single-session authority, keep audio private under `files/voice-notes`, preserve US-1 service cleanup, and do not reassign US-3/US-4/US-5 responsibilities.

## Commands

- Startup: `adb devices`; `bash scripts/check-feature-lifecycle.sh`
- Verification: `./gradlew assembleDebug && ./gradlew testDebugUnitTest && ./gradlew koverLog && ./gradlew ktlintCheck && ./gradlew detekt && ./gradlew lintDebug`
- Focused debug command: `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.voice.VoiceRecordingServiceIntegrationTest`
