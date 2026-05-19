# Regression Report: Home Favorites Pill Filtering

---

## Bug Reference

**Title**: Home Favorites Pill Filtering  
**Date fixed**: 2026-05-19  
**Severity**: high  
**Affected version**: unknown

---

## Symptom

When selecting the "Favorites" pill on the Home Screen, the notes list does not show favorited notes (remaining empty or showing incorrect notes).

---

## Root Cause

```
Root cause:
The bug happens because the "favorites" selectedFolderId filtering logic in HomeViewModel.kt attempted to find and filter by a physical folder whose name matches "Favorites" (ignoring case), instead of filtering by the note's isFavorite boolean property. This is triggered when the user clicks the "Favorites" pill on the Home Screen, causing favorited notes (marked via toggleFavorite quick action) to be excluded since they do not reside inside a folder specifically named "Favorites".
```

---

## Regression Test

| Test Class | Type | Scenario | Fails Before Fix | Passes After Fix |
|------------|------|----------|-----------------|-----------------|
| `HomeViewModelTest.kt` | Unit | `selectFolder favorites filters notes by isFavorite even if Favorites folder is missing` | ✅ | ✅ |

### Test description

```kotlin
    @Test
    fun `selectFolder favorites filters notes by isFavorite even if Favorites folder is missing`() = runTest {
        every { folderRepository.getFolders() } returns flowOf(listOf(testFolders[0])) // Only f1, no Favorites
        viewModel = HomeViewModel(noteRepository, folderRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        
        viewModel.selectFolder("favorites")
        val state = viewModel.uiState.value
        assertEquals("favorites", state.selectedFolderId)
        // Should show both favorite notes n2 and n3 even if there's no folder named Favorites
        assertEquals(2, state.recentNotes.size)
        assertTrue(state.recentNotes.any { it.id == "n2" })
        assertTrue(state.recentNotes.any { it.id == "n3" })
    }
```

---

## Edge Cases Covered

- [x] Null / missing data (handles missing "Favorites" folder gracefully)
- [x] Notes not favorited are correctly filtered out

---

## Fix Summary

**Files changed**:
- `app/src/main/java/com/example/notesapp/ui/home/viewmodel/HomeViewModel.kt` — Filter notes list using `notes.filter { it.isFavorite }` under `"favorites"` case rather than looking up a database folder named `"Favorites"`.

**Change type**: business logic / state filtering correction

---

## Prevention

A robust regression test was added to `HomeViewModelTest.kt` ensuring that selecting the favorites pill successfully returns all favorited notes (i.e. those with `isFavorite = true`) even if no folder named `"Favorites"` exists in the repository.
