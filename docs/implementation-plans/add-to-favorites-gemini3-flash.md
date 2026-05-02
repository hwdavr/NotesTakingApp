# Implementation Plan: Add to Favorites

This plan outlines the steps to implement the "Add to Favorites" feature for folders and notes in the NotesTakingApp.

## 1. Requirement Summary
- **Goal**: Allow users to mark folders and notes as favorites from the folder screen.
- **Scope**: Folders and Notes in the `FoldersScreen`.
- **Backend Integration**: New PATCH endpoint `/v1/items/{itemID}/favorite` and `isFavorite` field in items.

## 2. Impact Analysis
- **API**: New endpoint and schema change (backward compatible).
- **Domain**: `Folder` and `Note` models will have an `isFavorite` property.
- **Data**: Local database schema update (Room version bump).
- **UI**: "Add to Favorites" action in `FolderItemActionsSheet` and `NoteItemActionsSheet` will be functional.
- **Tests**: New unit and integration tests for favoriting logic.

## 3. API Compatibility Assessment
- **Change**: Backward compatible. Existing clients will ignore the new field and won't use the new endpoint.
- **Force Update**: Not required.

## 4. Architecture/Design Summary
- **Pattern**: Follow existing repository and ViewModel patterns.
- **State**: `isFavorite` will be part of the `Folder` and `Note` domain models, which are used to build the `FoldersUiState`.

## 5. Implementation Plan

### Phase 1: Contract & Models
1.  **Modify `sharedContracts/openapi.yaml`**:
    - Add `isFavorite` (boolean) to `Item` schema.
    - Add `UpdateFavoriteRequest` schema.
    - Add `PATCH /v1/items/{itemID}/favorite` endpoint.
2.  **Modify `ApiModels.kt`**:
    - Add `isFavorite: Boolean` to `ApiItem`.
    - Add `UpdateFavoriteRequest(val isFavorite: Boolean, val deviceId: String, val lastSyncedVersion: Long)`.
3.  **Modify `NotesApiService.kt`**:
    - Add `favoriteItem(@Path("itemID") itemId: String, @Body request: UpdateFavoriteRequest): MutationResultDto`.
4.  **Modify `Folder.kt` & `Note.kt`**:
    - Add `val isFavorite: Boolean = false` to both.

### Phase 2: Local Storage & Mappers
5.  **Modify `FolderEntity.kt` & `NoteEntity.kt`**:
    - Add `val isFavorite: Boolean`.
6.  **Modify `AppDatabase.kt`**:
    - Bump version to 3.
7.  **Modify `ApiMappers.kt`**:
    - Update `toFolderEntity()` and `toNoteEntity()` to map `isFavorite`.
8.  **Modify `FolderMapper.kt` & `NoteMapper.kt`**:
    - Update `toDomain()` and `toEntity()` to map `isFavorite`.

### Phase 3: Repository & ViewModel
9.  **Modify `FolderRepository.kt` & `NoteRepository.kt` (Interfaces)**:
    - Add `suspend fun toggleFavorite(folder: Folder)` and `suspend fun toggleFavorite(note: Note)`.
10. **Modify `FolderRepositoryImpl.kt` & `NoteRepositoryImpl.kt`**:
    - Implement `toggleFavorite` calling the new API endpoint and fallback to local update.
11. **Modify `FoldersViewModel.kt`**:
    - Add `toggleFolderFavorite(folder: Folder)` and `toggleNoteFavorite(note: Note)`.
    - Update `refreshCounts()` to count favorites based on the `isFavorite` flag instead of a folder named "Favorites".

### Phase 4: UI
12. **Modify `FoldersScreen.kt`**:
    - Connect `onAddToFavorites` in `FolderItemActionsSheet` and `NoteItemActionsSheet` to ViewModel calls.
    - Update icons or labels in the sheet based on the current favorite status (e.g., "Remove from Favorites" if already favorited).

## 6. Test Plan
- **Unit Tests**:
    - `FoldersViewModelTest`: Verify `toggleFolderFavorite` and `toggleNoteFavorite` call repositories.
    - Repository tests: Verify API calls and local fallback for favoriting.
- **Integration Tests**:
    - `FoldersViewModelIntegrationTest`: New test case for favoriting an item and verifying UI state.
- **UI Tests**:
    - `FoldersScreenTest`: Verify the favorite action is visible and clickable.

## 7. Security/Privacy Considerations
- No sensitive data involved. Favorite status is standard user metadata.

## 8. Rollout/Release Considerations
- Standard rollout. Backend must be updated before or with the app.

## 9. Files to Change
- `sharedContracts/openapi.yaml`
- `app/src/main/java/com/example/notesapp/data/remote/ApiModels.kt`
- `app/src/main/java/com/example/notesapp/data/remote/NotesApiService.kt`
- `app/src/main/java/com/example/notesapp/domain/folder/Folder.kt`
- `app/src/main/java/com/example/notesapp/domain/note/Note.kt`
- `app/src/main/java/com/example/notesapp/data/local/FolderEntity.kt`
- `app/src/main/java/com/example/notesapp/data/local/NoteEntity.kt`
- `app/src/main/java/com/example/notesapp/data/local/AppDatabase.kt`
- `app/src/main/java/com/example/notesapp/data/remote/ApiMappers.kt`
- `app/src/main/java/com/example/notesapp/data/repository/FolderMapper.kt`
- `app/src/main/java/com/example/notesapp/data/repository/NoteMapper.kt`
- `app/src/main/java/com/example/notesapp/domain/folder/FolderRepository.kt`
- `app/src/main/java/com/example/notesapp/domain/note/NoteRepository.kt`
- `app/src/main/java/com/example/notesapp/data/repository/FolderRepository.kt` (Impl)
- `app/src/main/java/com/example/notesapp/data/repository/NoteRepository.kt` (Impl)
- `app/src/main/java/com/example/notesapp/ui/folders/FoldersViewModel.kt`
- `app/src/main/java/com/example/notesapp/ui/folders/FoldersScreen.kt`

## 10. Open Questions or Assumptions
- **Assumption**: The backend will support the new endpoint. I will update the `openapi.yaml` but I don't have access to the backend implementation in this context (unless it's in the same repo, which I should check).
- **Question**: Should we also show a "Star" icon next to favorited items in the list? (Recommended for better UX).
