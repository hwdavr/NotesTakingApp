# Bug Specification — Home Favorites Pill Filtering Fix

## Requirement Summary
Selecting the "Favorites" pill on the Home screen should filter the active notes list to show only notes where `isFavorite = true`. Currently, selecting "Favorites" results in an empty notes list or unexpected behavior because the app filters notes based on a database folder named "Favorites" instead of the note's `isFavorite` boolean field.

- **Expected Behavior**: Clicking the "Favorites" pill on the Home Screen displays all favorite notes (notes with `isFavorite == true`).
- **Actual Behavior**: The "Favorites" selection attempts to look for a physical folder in the DB named "Favorites" and filters by folder ID. Since favorites are set via quick action (`toggleFavorite`) rather than placing them in a physical folder named "Favorites", the list remains empty or does not show the actual favorited notes.

## Impact Analysis (Affected Files)

| File | Layer | Change Type | Notes |
|------|-------|-------------|-------|
| `HomeViewModel.kt` | UI / Presenter | `modify` | Change filtering logic for the `"favorites"` case from physical folder lookup to checking `note.isFavorite`. |
| `HomeViewModelTest.kt` | Test | `modify` | Add unit tests to verify `"favorites"` filtering and fix the existing mock test. |

## API Impact
- Classification: `none`
- Force update: `no`
- **APIs Needed**: None (entirely local filtering logic change)

## UI State Design
No changes needed for the UI State data class itself (`HomeUiState`). The `recentNotes` list inside the state will be properly populated with the filtered list of favorites.

## Navigation Design
No changes to navigation.

## Explicit Assumptions
1. Favorite notes are notes that have `note.isFavorite == true`.
2. There is no physical folder named "Favorites" expected to hold these notes; the "Favorites" pill acts as a smart filter over all active notes.
3. The count of active favorite notes is computed on-device and displayed accordingly.
