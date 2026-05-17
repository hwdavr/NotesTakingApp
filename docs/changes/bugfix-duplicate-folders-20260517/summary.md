# Change Summary — Duplicate Folder Pills & Shared Notes Empty State Fix

**Type**: bugfix
**Started**: 2026-05-17
**Status**: Complete

## Stage Progress

| Stage | Status | Date | Notes |
|-------|--------|------|-------|
| Bug Context, Root Cause | ✅ Complete | 2026-05-17 | Analyzed duplicate pills and folders screen empty state |
| Fix Plan | ✅ Complete | 2026-05-17 | Approved by user: yes |
| Implementation | ✅ Complete | 2026-05-17 | UI filters and empty state logic corrected |
| Code Review | ✅ Complete | 2026-05-17 | APPROVED |
| Regression Test | ✅ Complete | 2026-05-17 | 41 tests, 100% passing |
| Test Review | ✅ Complete | 2026-05-17 | APPROVED |
| Knowledge Capture | ✅ Complete | 2026-05-17 | Regression reports registered for both bugs |

## Key Decisions
- **Duplicate folder pills**: Standardized filtering in the UI layer (`HomeNotesScreen.kt`) to cleanly prevent duplication while preserving platform-agnostic models in the ViewModel.
- **Empty state shared notes**: Fixed the empty state logic in `FoldersScreen.kt` to only trigger when *both* `treeItems` and `sharedTreeItems` are empty, allowing shared notes to render when personal folders are empty.

## Files Changed
- `app/src/main/java/com/example/notesapp/ui/home/screen/HomeNotesScreen.kt` (modified)
- `app/src/androidTest/java/com/example/notesapp/ui/home/screen/HomeScreenIntegrationTest.kt` (modified)
- `app/src/main/java/com/example/notesapp/ui/folders/screen/FoldersScreen.kt` (modified)

## Knowledge Artifacts
- `docs/knowledge/past-bugs/2026-05-17-duplicate-folder-pills.md`
- `docs/knowledge/past-bugs/2026-05-17-folders-screen-empty-state-shared-notes.md`

## Open Items
- None
