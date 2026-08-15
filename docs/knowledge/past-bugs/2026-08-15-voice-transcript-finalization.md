# Regression Report — Voice Transcript Finalization and Punctuation

## Bug Reference

**Title**: Voice recordings lost the last transcript preview and emitted unpunctuated text  
**Date fixed**: 2026-08-15  
**Severity**: medium  
**Affected version**: unknown

## Symptom

The recorder showed a continuous sequence of words without sentence punctuation. Tapping the check/save action could omit the latest words visible in the recorder, and the returned editor note did not have a regression proving the transcript and playback block survived together.

## Root Cause

```
Root cause:
AndroidVoiceTranscriptRecognizer built an API-33 source-fed intent without
EXTRA_ENABLE_FORMATTING, so the on-device recognizer was not asked for quality
punctuation. Separately, RecordingTranscriptCoordinator.stop() returned only
committed chunks while the visible latest partial result lived in a separate
preview field. Stopping before a final callback therefore persisted stale or
empty text.
```

## Regression Test

| Test Class | Type | Scenario | Fails Before Fix | Passes After Fix |
|---|---|---|---|---|
| `RecordingTranscriptCoordinatorTest` | Unit | Stop with a non-blank partial and no final callback | ✅ | ✅ |
| `VoiceTranscriptionFailureReproductionTest` | Robolectric | Source-fed API-33 intent requests quality formatting | ✅ | ✅ |
| `VoiceRecorderTranscriptIntegrationTest` | JVM integration | Production ViewModel saves the partial-only transcript | ✅ behavior covered | ✅ |
| `EditorVoiceNoteInsertionTest` | JVM integration | Saved document keeps a non-null audio path followed by transcript text | ✅ | ✅ |
| `VoiceNoteEditorFlowTest` | Instrumented UI | JSON-round-tripped returned editor document renders transcript and inline player controls | ✅ existing coverage | ✅ |

### Test description

```kotlin
@Test
fun `stop commits the latest partial transcript when no final callback arrived`() {
    coordinator.start(metadata)
    recognizer.emit(TranscriptRecognitionEvent.Partial("session", 0, "Are you okay"))

    assertEquals("Are you okay", coordinator.stop())
}
```

## Edge Cases Covered

- [ ] Null / missing data — existing audio-only and missing-audio behavior remains covered by prior tests.
- [x] Partial response
- [ ] Unknown enum value — no API enum involved.
- [x] Concurrent request — late final callbacks remain keyed by chunk and session; existing overlap tests pass.
- [x] Retry after failure — existing fallback/cancellation tests pass.
- [x] Old app / old backend version — API-33 formatting is inside the existing platform guard; no persisted schema changed.

## Fix Summary

**Files changed**:
- `app/src/main/java/com/example/notesapp/domain/voice/ChunkedTranscriptConcatenator.kt` — promote the active partial chunk once at stop.
- `app/src/main/java/com/example/notesapp/data/voice/RecordingTranscriptCoordinator.kt` — return the finalized transcript.
- `app/src/main/java/com/example/notesapp/data/voice/AndroidVoiceTranscriptRecognizer.kt` — request platform quality formatting on API 33+.

**Change type**: state correction and Android platform intent contract fix

## Prevention

The RED reproductions remain as permanent GREEN regression tests. The save/reload tests also require the persisted voice block to retain its audio path and transcript sibling, while the instrumented editor test protects the existing player semantics.
