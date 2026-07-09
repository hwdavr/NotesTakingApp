# Editor Back Save Autosave Race

## Bug Reference

**Title**: Editor back save raced stale autosave  
**Date fixed**: 2026-07-09  
**Severity**: high  
**Affected version**: unknown

---

## Symptom

When a user edited a note and quickly pressed Back, the loading indicator appeared, but the persisted note could miss the last typed text. Smart categorization was also inconsistent because the back flow could run while an older autosave was still active.

---

## Root Cause

```text
Root cause:
The bug happens because NoteEditorViewModel canceled autosave jobs without waiting for every active autosave to settle, triggered when an older autosave had already entered repository save and a newer edit scheduled another autosave, causing the explicit back save to race an older note snapshot.
```

`scheduleAutoSave()` also replaced the `autoSaveJob` reference when newer text arrived, so the older in-flight autosave could be lost from the ViewModel's cancellation/join path.

---

## Regression Test

| Test Class | Type | Scenario | Fails Before Fix | Passes After Fix |
|------------|------|----------|-----------------|-----------------|
| `NoteEditorViewModelCategorizeTest.kt` | Unit | Stale autosave is in flight, newer text is typed, then Back is pressed | ✅ | ✅ |

### Test description

```kotlin
@Test
fun `given stale autosave is in flight when handleBackPress then back save waits for autosave to settle`() {
    // The test holds the first repository save open, types newer text,
    // presses Back, and verifies the back save waits before saving latest content.
}
```

---

## Edge Cases Covered

- [ ] Null / missing data
- [ ] Partial response
- [ ] Unknown enum value
- [x] Concurrent request
- [ ] Retry after failure
- [ ] Old app / old backend version

---

## Fix Summary

**Files changed**:
- `app/src/main/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModel.kt` — tracks all active autosave jobs and joins them before explicit save, share, back-save, and categorization decisions.
- `app/src/test/java/com/example/notesapp/ui/editor/viewmodel/NoteEditorViewModelCategorizeTest.kt` — adds the regression test that proves stale autosave settlement before back save.

**Change type**: coroutine ordering fix

---

## Prevention

- Keep a regression test for any editor back-save behavior that can race autosave.
- Do not assume `Job.cancel()` has finished work; use deterministic settlement when later logic depends on no older save still running.
- Do not overwrite the only reference to an in-flight save job when a newer save is scheduled; keep active jobs tracked until completion.
