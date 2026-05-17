# Implementation Plan — Duplicate Folder Pills Fix

## Feature / Bug

Fix duplicate "All Notes" and "Shared" folder pills under "Recent folders:" on the home screen.

---

## Requirement Summary

On the home screen under "Recent folders:", "All Notes" and "Shared" are statically added as pills. However, because they are also retrieved from the ViewModel's `recentFolders` list, they get rendered twice. We need to filter them out of the `recentFolders` list in the UI to prevent duplication.

---

## Impact Summary

| Layer | Files Affected | Change Type |
|-------|---------------|-------------|
| UI | `app/src/main/java/com/example/notesapp/ui/home/screen/HomeNotesScreen.kt` | modify |
| Tests | `app/src/androidTest/java/com/example/notesapp/ui/home/screen/HomeScreenIntegrationTest.kt` | modify |

---

## API Changes

- **Classification**: none
- **Force update required**: no
- **Fields added**: none
- **Fields removed**: none
- **Fields changed**: none
- **OpenAPI Status**: Already defined in sharedContracts/openapi.yaml / no updates required

---

## Files to Create

None

---

## Files to Modify

| File | What Changes |
|------|-------------|
| `app/src/main/java/com/example/notesapp/ui/home/screen/HomeNotesScreen.kt` | Filter out folder items with ID `"all_notes"` or `"shared"` inside `FolderChipsRow`. |
| `app/src/androidTest/java/com/example/notesapp/ui/home/screen/HomeScreenIntegrationTest.kt` | Add `folderPillsAreNotDuplicated` instrumented UI test to reproduce the duplication before fixing and verify the fix afterwards. |

---

## Files to Delete

None

---

## UiState Design

No changes to UiState design are required because the bug is in the presentation/UI logic of `FolderChipsRow` rather than in state structures.

---

## Test Plan

### Test Layer Selection
| Layer | Included | Reason |
|-------|----------|--------|
| Unit tests (`app/src/test/`) | ❌ | Not needed, existing `HomeViewModelTest` verifies virtual folders count |
| Integration tests (`app/src/test/`) | ❌ | Covered by existing `HomeViewModelIntegrationTest` |
| Instrumented UI tests (`app/src/androidTest/`) | ✅ | Needed to verify Compose UI renders folder pills without duplicates |

### Unit Tests
None

### Integration Tests
None

### Shared JSON Scenarios
None

### Instrumented UI Tests
| Test Class | Scenarios |
|------------|-----------|
| `HomeScreenIntegrationTest.kt` | `folderPillsAreNotDuplicated` verifies that "All Notes", "Shared", "Favorites", and other folders are displayed exactly once. |

### Coverage Target
- Overall: ≥ 80%
- New classes: N/A

### Verification Commands
```bash
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.home.screen.HomeScreenIntegrationTest
```

---

## Explicit Assumptions

1. `HomeViewModel.recentFolders` must continue to contain `"all_notes"` and `"shared"` virtual folders.
2. The UI is the correct layer to perform filtering of these virtual folders.

---

## Risks

1. Risk: None expected. — Mitigation: Standard regression testing.

---

## Migration / Compatibility Notes

None

---

## Out of Scope

- Modifying the folder structure in database or ViewModel.
- Modifying styling or layout of the pills.
