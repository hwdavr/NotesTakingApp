# Platform Capability Matrix

> Retrofit note: this matrix is added as the new evaluator hard gate. Its `--evaluate` check intentionally fails until the real source-fed platform test is implemented and executed; the existing tracker status is not re-transitioned by this workflow change.

## Scope

- Feature/slice: `voice-notes-audio-transcripts` / US-1 through US-5
- Platform boundary: Android audio capture, `SpeechRecognizer`, foreground microphone service, and API-level codec/permission behavior
- Minimum API: 24
- Target API: 34
- Single resource owner: `VoiceNoteRecordingService` owns the PCM capture stream; recognition may consume that stream only through the supported source-fed boundary
- Input/output contract: PCM frames with explicit sample rate, channel count, and encoding; encoded AAC/OPUS output remains private recording storage; recognition emits partial/final/error callbacks

## Runtime Matrix

| Runtime/API | Capability under test | Required behavior | Test ID / exact command | Environment evidence | Status |
|---|---|---|---|---|---|
| API 24–32 | Source-fed `SpeechRecognizer` availability | Use explicit audio-only fallback; never open a second microphone client | `TC-US-2-02-source-contract`; JVM contract and fallback tests | API-33 emulator verifies fallback branch; API-24/API-31 runtime images unavailable | Pending |
| API 33 | On-device source-fed recognition with PCM input | Feed the service-owned PCM source through `EXTRA_AUDIO_SOURCE` and observe real recognition output | `TC-US-2-REAL-PLATFORM`; connected instrumented test to be added | API-33 emulator connected; current tests verify intent/listener seams, not real speech output | Pending |
| API 34 | Target-level source-fed recognition and foreground microphone behavior | Same source contract, permissions, and lifecycle behavior as target release | `TC-US-2-REAL-PLATFORM`; connected instrumented test to be added | API-34 runtime unavailable | Pending |

## Real Platform Boundary Test

- Required: Yes
- Test IDs: `TC-US-2-REAL-PLATFORM`
- Instrumented test file(s): `app/src/androidTest/java/com/example/notesapp/voice/VoiceSourceFedRecognitionInstrumentedTest.kt`
- Real-platform signal: `SpeechRecognizer`, `AndroidVoiceTranscriptRecognizer`, and `RecognizerIntent`
- Exact command: `ANDROID_SERIAL=<configured-api-33-device> ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.voice.VoiceSourceFedRecognitionInstrumentedTest`
- Fixture/data source: deterministic local PCM speech fixture and configured on-device recognition model; no live backend
- Assertion: a real platform recognizer returns a bounded final result from the supplied PCM source; unsupported API/model behavior is asserted as an explicit non-success fallback

The existing JVM source-intent and fake-listener tests are supplemental. They do not satisfy this real boundary test because they do not prove a shipped `SpeechRecognizer` consumes the PCM source.

## Unsupported Environment Policy

The evaluator must fail loudly when a required emulator, device, model, locale, permission, hardware capability, or platform service is unavailable. The test must return a non-zero result or the feature must be marked `Blocked`/`Revise`; it must not be converted into a passing result through a skip, warning, or missing-evidence note.

- Policy: `fail_loudly`
- Missing environment result: non-zero command / `Blocked` / `Revise`
- Explicit fallback for genuinely unsupported API: API 24–32 audio-only recording with localized unavailable-transcription state, covered by `TC-US-2-02-source-contract`
- Evidence owner: Generator implementation and Evaluator runtime verification
