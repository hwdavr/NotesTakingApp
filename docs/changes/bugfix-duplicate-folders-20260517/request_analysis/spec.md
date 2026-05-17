# Spec — Duplicate Folder Pills Fix

## Bug Context, Localization & Root Cause

### Bug Description
At the home page, under "Recent folders:", the "All Notes" and "Shared" pills are displayed twice (duplicated).

### Expected Behavior
The "All Notes", "Favorites", and "Shared" folder pills should appear exactly once at the beginning of the horizontal list of folders. Subsequent folder pills should display user-created or sync'ed folders (excluding favorites and duplicates of virtual folders).

### Actual Behavior
"All Notes" and "Shared" are displayed once statically at the beginning, but are also appended from the `recentFolders` list, causing duplicate pills for both "All Notes" and "Shared".

### Fault Localization
- **UI Layer**: `app/src/main/java/com/example/notesapp/ui/home/screen/HomeNotesScreen.kt` inside `FolderChipsRow` composable.
- **VM Layer**: `app/src/main/java/com/example/notesapp/ui/home/viewmodel/HomeViewModel.kt` constructs `recentFolders` including virtual folders `"all_notes"` and `"shared"` which the UI relies on for counts and selections (e.g., in integration/unit tests).

### Root Cause Statement
Triggered when the Home screen renders the `FolderChipsRow` with the `state.recentFolders` list, causing "All Notes" and "Shared" folder pills to be rendered twice because the `FolderChipsRow` statically displays the "All Notes", "Favorites", and "Shared" pills first, and then maps over `state.recentFolders` without filtering out the `"all_notes"` and `"shared"` virtual folders.

### Design the Fix
We will modify `FolderChipsRow` in `HomeNotesScreen.kt` to filter out `"all_notes"` and `"shared"` virtual folders, in addition to excluding `"Favorites"` folder names. Specifically, the filter predicate will become:
```kotlin
items.filter { 
    it.id != "all_notes" && 
    it.id != "shared" && 
    !it.name.equals("Favorites", ignoreCase = true) 
}.forEach { folder ->
    FolderPill(...)
}
```

## Explicit Assumptions
1. `recentFolders` in `HomeViewModel` must continue to contain `"all_notes"` and `"shared"` virtual folders for correct count calculation and existing unit/integration tests compatibility.
2. The UI is the correct place to prevent virtual folders from being rendered as generic folders, since the UI already handles them as special static pills.
