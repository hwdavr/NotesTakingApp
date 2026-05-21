# Missing Shared Notes on Home Screen

## Bug Description
When the "All notes" pill was selected on the Home screen, shared notes were missing from the list.

## Root Cause
In `HomeViewModel.kt`, the `uiState` flow combine block was filtering notes based on `selectedFolderId`. When `selectedId` was `"all_notes"`, it only returned `notes` (which maps to `noteRepository.getActiveNotes()`), completely omitting the `shared` notes (`noteRepository.getSharedNotes()`). 

Additionally, this bug was codified in the shared test scenario `home_shared_pill_001.json`, where it incorrectly expected the initial state (which defaults to "All notes") to only contain 1 note (the personal one).

## Fix
1. Modified `HomeViewModel.kt` to return `notes + shared` when `"all_notes"` is selected.
2. Updated the shared JSON scenario `home_shared_pill_001.json` to expect the initial note count to include both personal and shared notes.
3. Updated unit tests to correctly assert the inclusion of shared notes in the "All notes" state.
