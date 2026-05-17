# Regression Report — Folders Screen Empty State Overriding Shared Notes

## Bug Reference

**Title**: Shared notes not displayed on Folders screen if personal folders/notes are empty  
**Date fixed**: 2026-05-17  
**Severity**: high  
**Affected version**: unknown  

---

## Symptom

When a user has no personal folders or notes, but does have shared notes, the Folders screen displays the empty state placeholder ("No folders or notes yet") and completely hides/omits the "Shared" notes section.

---

## Root Cause

```
Root cause:
The bug happens because FoldersScreenContent checks if state.treeItems.isEmpty() to conditionally render either the empty state Box or the entire LazyColumn (which also hosts the shared notes section). When the user has no personal folders/notes but does have shared notes, state.treeItems is empty, triggering the empty state Box and preventing the LazyColumn from ever being composed.
```

---

## Regression Test

| Test Class | Type | Scenario | Fails Before Fix | Passes After Fix |
|------------|------|----------|-----------------|-----------------|
| `FoldersScreenTest.kt` | Instrumented UI | `sharedNotes_rendersInSharedSection` | ✅ | ✅ |

### Test description

```kotlin
    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun sharedNotes_rendersInSharedSection() {
        val sharedNote = Note(
            id = "shared_1",
            title = "Shared Note",
            content = "",
            folderId = null,
            isShared = true,
            createdAt = 0,
            updatedAt = 0
        )
        val state = FoldersUiState(
            sharedTreeItems = listOf(FolderTreeItem.NoteItem(sharedNote, 0))
        )
        composeRule.setContent {
            TestFoldersScreen(state = state)
        }
        
        // Check for shared section title
        composeRule.onNodeWithText("Shared").assertIsDisplayed()
        // Check for the shared note title
        composeRule.onNodeWithText("Shared Note").assertIsDisplayed()
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
- `app/src/main/java/com/example/notesapp/ui/folders/screen/FoldersScreen.kt` — Changed the empty state rendering condition to check if **both** `state.treeItems` and `state.sharedTreeItems` are empty before displaying the empty state placeholder.

**Change type**: state correction / rendering logic fix

---

## Prevention

We verified and reactivated the stateless `FoldersScreenTest.sharedNotes_rendersInSharedSection` Compose test case to guarantee that shared notes are correctly rendered in their dedicated section even when personal notes/folders lists are completely empty.
