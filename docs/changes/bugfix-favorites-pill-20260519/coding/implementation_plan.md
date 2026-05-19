# Implementation Plan — Home Favorites Pill Filtering Fix

---

## Feature / Bug

Home Favorites pill does not filter notes by favorite status (shows empty list instead).

---

## Requirement Summary

When the user clicks on the "Favorites" pill on the Home Screen, the notes list should show only notes that have been favorited (`isFavorite = true`). Currently, the ViewModel searches for a database folder named "Favorites" and filters by that folder ID. Since favorites are a property on the note rather than a folder association, this results in an empty or incorrect list. We will fix this logic by checking the `isFavorite` property of the `Note` entity.

---

## Impact Summary

| Layer | Files Affected | Change Type |
|-------|---------------|-------------|
| Presentation | `app/src/main/java/com/example/notesapp/ui/home/viewmodel/HomeViewModel.kt` | modify |
| Tests | `app/src/test/java/com/example/notesapp/ui/home/viewmodel/HomeViewModelTest.kt` | modify |

---

## API Changes

- **Classification**: none
- **Force update required**: no
- **Fields added**: none
- **Fields removed**: none
- **Fields changed**: none
- **OpenAPI Status**: Already defined in sharedContracts/openapi.yaml (no changes)

---

## Files to Create

*None*

---

## Files to Modify

| File | What Changes |
|------|-------------|
| `app/src/main/java/com/example/notesapp/ui/home/viewmodel/HomeViewModel.kt` | Update the `when (selectedId)` block for `"favorites"` to filter the active `notes` list where `it.isFavorite` is true, instead of looking up a folder named `"Favorites"`. |
| `app/src/test/java/com/example/notesapp/ui/home/viewmodel/HomeViewModelTest.kt` | Update the setup and mock values so that one of the notes has `isFavorite = true` to accurately test favorites. Fix the existing test `selectFolder favorites handles missing Favorites folder` (since "Favorites" is no longer a physical folder filter) and replace/add a new test specifically verifying filtering behavior for the `"favorites"` selected folder ID. |

---

## Files to Delete

*None*

---

## UiState Design

No changes to the UiState class. The existing `HomeUiState` successfully contains `recentNotes` (list of `NoteUiModel`) which will be updated with the filtered notes.

---

## Test Plan

### Test Layer Selection
| Layer | Included | Reason |
|-------|----------|--------|
| Unit tests (`app/src/test/`) | ✅ | We will write a JVM unit test to verify that the `HomeViewModel` correctly filters notes by `isFavorite` when selecting the `"favorites"` folder ID. |
| Integration tests (`app/src/test/`) | ❌ | No backend / DB boundary changes are introduced, existing unit/integration tests cover the flow. |
| Instrumented UI tests (`app/src/androidTest/`) | ❌ | UI itself doesn't change, Compose rendering of pills is already tested. |

### Unit Tests
| Test Class | What It Tests |
|------------|--------------|
| `HomeViewModelTest.kt` | 1. `selectFolder favorites filters notes by isFavorite` - asserts that only notes with `isFavorite = true` are shown when `"favorites"` pill is clicked.<br>2. Fix or rewrite `selectFolder favorites handles missing Favorites folder` to reflect the new logic. |

### Integration Tests
*None needed for this UI-only presenter logic change.*

### Shared JSON Scenarios
*None modified or created.*

### Instrumented UI Tests
*None modified or created.*

### Coverage Target
- Overall: ≥ 80%
- New/modified ViewModel lines: 100%

### Verification Commands
```bash
./gradlew testDebugUnitTest
./gradlew koverLog
```

---

## Explicit Assumptions

1. The "Favorites" pill is a virtual filter pill on the home screen, not a folder.
2. Favorite notes are those where `note.isFavorite == true`.
3. Non-favorite notes have `isFavorite == false`.

---

## Risks

1. Risk: Breaking existing counts or behavior for user-created folders if they named a folder "Favorites".
   - Mitigation: The "Favorites" folder count is handled separately in `FoldersViewModel` and `CollectionNotesViewModel`. On the Home Screen, the "Favorites" pill is reserved exclusively for the smart favorite filter. By filtering `it.isFavorite`, we correctly show all favorited notes regardless of their folder.

---

## Migration / Compatibility Notes

*None*

---

## Out of Scope

* Modifying the Folder/Favorites logic in `CollectionNotesViewModel` or `FoldersViewModel` where "Favorites" is treated as a smart aggregation (which already works correctly).
* Modifying the Note item action sheets.
