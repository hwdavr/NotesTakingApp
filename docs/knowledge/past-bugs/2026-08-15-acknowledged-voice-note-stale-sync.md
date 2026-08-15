# Acknowledged Voice Note Lost During Stale Sync

## Bug Reference

**Title**: A saved voice note reopened as an empty editor after recording  
**Date fixed**: 2026-08-15  
**Severity**: high  
**Affected version**: unknown

## Symptom

> After a user completed a recording and tapped save, the routed `Untitled note` editor was blank: neither the inline player nor the transcript appeared.

## Root Cause

```
Root cause:
The bug happens because NoteRepositoryImpl.save discarded the acknowledged API item and
immediately ran a full Room-table rebuild. When GET /v1/items was stale or temporarily
omitted the placeholder, ItemsSyncCoordinator replaced the newer voice document with the
old blank item, causing NoteEditorViewModel.load to initialize an empty editor.
```

## Regression Test

| Test Class | Type | Scenario | Fails Before Fix | Passes After Fix |
|---|---|---|---|---|
| `NoteRepositoryImplTest.kt` | Unit | Content-update acknowledgement is persisted before sync | ✅ | ✅ |
| `ItemsSyncCoordinatorTest.kt` | Unit | Stale, absent, and newer tombstone item lists resolve by version | ✅ | ✅ |
| `NoteRepositoryImplIntegrationTest.kt` | Integration | Shared PATCH-success / stale-GET / conflict-retry sequence | ✅ | ✅ |

### Test description

```kotlin
@Test
fun `save keeps acknowledged voice document when shared item list is stale`()
```

The integration test uses `voice_note_stale_sync_001.json` and asserts that the original note ID retains its acknowledged voice-document JSON after the stale list and retry conflict.

## Edge Cases Covered

- [x] Null / missing data — a temporarily omitted active note remains locally available.
- [x] Partial response — an older blank list item cannot replace a newer document.
- [ ] Unknown enum value
- [x] Concurrent request — the existing serialized `syncAll` regression remains green.
- [x] Retry after failure — a retry conflict retains the acknowledged local revision.
- [x] Old app / old backend version — no DTO, schema, or endpoint contract changed.

## Fix Summary

**Files changed**:

- `app/src/main/java/com/example/notesapp/data/repository/NoteRepositoryImpl.kt` — persists the returned API item before best-effort sync; sync failure does not convert an acknowledgement into an offline fallback.
- `app/src/main/java/com/example/notesapp/data/sync/ItemsSyncCoordinator.kt` — retains newer local notes across stale or missing list responses and accepts equal/newer remote entries, including tombstones.
- `app/src/test/java/com/example/notesapp/data/repository/NoteRepositoryImplIntegrationTest.kt` — verifies the Retrofit and fake-Room path.
- `sharedContracts/test-scenarios/voice_note_stale_sync_001.json` — captures the cross-platform API sequence.

**Change type**: synchronization state correction

## Prevention

Successful mutation responses are now treated as durable local state before any table-replacement sync. The unit and shared-contract integration regressions protect both stale-content and missing-item responses.
