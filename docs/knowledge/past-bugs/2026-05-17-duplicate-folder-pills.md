# Regression Report — Duplicate Folder Pills

## Bug Reference

**Title**: Duplicate "All Notes" and "Shared" folder pills on home page  
**Date fixed**: 2026-05-17  
**Severity**: low  
**Affected version**: unknown  

---

## Symptom

Under "Recent folders:" on the home screen, the virtual folder pills "All Notes" and "Shared" were duplicated (rendered twice).

---

## Root Cause

```
Root cause:
The bug happens because the FolderChipsRow statically displays "All Notes", "Favorites", and "Shared" pills at the beginning of the row, but then iterates over all items in state.recentFolders without filtering out these virtual folders, triggered when the home screen is composed with a non-empty recentFolders list, causing duplicate pills to render for "All Notes" and "Shared".
```

---

## Regression Test

| Test Class | Type | Scenario | Fails Before Fix | Passes After Fix |
|------------|------|----------|-----------------|-----------------|
| `HomeScreenIntegrationTest.kt` | Instrumented UI | `folderPillsAreNotDuplicated` | ✅ | ✅ |

### Test description

```kotlin
    @Test
    fun folderPillsAreNotDuplicated() {
        val viewModel = HomeViewModel(
            noteRepository = FakeNoteRepository(),
            folderRepository = FakeFolderRepository(
                initialFolders = listOf(
                    folder(id = "work", name = "Work")
                )
            )
        )
        composeRule.setContent {
            NotesTakingAppTheme {
                HomeNotesScreen(
                    parentPadding = PaddingValues(0.dp),
                    onAddNote = {},
                    onOpenNote = {},
                    viewModel = viewModel
                )
            }
        }
        composeRule.waitForIdle()
        // Verify that virtual and database folder pills are displayed exactly once
        composeRule.onAllNodesWithText("All Notes").assertCountEquals(1)
        composeRule.onAllNodesWithText("Shared").assertCountEquals(1)
        composeRule.onAllNodesWithText("Favorites").assertCountEquals(1)
        composeRule.onAllNodesWithText("Work").assertCountEquals(1)
    }
```

---

## Edge Cases Covered

- [x] Null / missing data
- [ ] Partial response
- [ ] Unknown enum value
- [ ] Concurrent request
- [ ] Retry after failure
- [ ] Old app / old backend version

---

## Fix Summary

**Files changed**:
- `app/src/main/java/com/example/notesapp/ui/home/screen/HomeNotesScreen.kt` — Added filter predicate to `FolderChipsRow` to exclude `"all_notes"` and `"shared"` virtual folder IDs in addition to filtering out `"Favorites"`.

**Change type**: state correction / rendering filter fix

---

## Prevention

We added a dedicated instrumented UI test in `HomeScreenIntegrationTest.kt` that explicitly checks the exact count of each folder pill rendered on the Home screen to prevent duplication regressions in the future.
