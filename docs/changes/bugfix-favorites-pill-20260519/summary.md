# Change Summary — Home Favorites Pill Filtering Fix

**Type**: bugfix
**Started**: 2026-05-19
**Status**: Complete

## Stage Progress

| Stage | Status | Date | Notes |
|-------|--------|------|-------|
| Bug Context, Localization & Root Cause | ✅ Complete | 2026-05-19 | Root cause localized to HomeViewModel's favorites filtering logic |
| Fix Plan | ✅ Complete | 2026-05-19 | Approved by user: yes |
| Implementation | ✅ Complete | 2026-05-19 | Filter logic fixed inside HomeViewModel.kt |
| Code Review | ✅ Complete | 2026-05-19 | code_review_v1.md created. APPROVED |
| Regression Test | ✅ Complete | 2026-05-19 | Failing test selectFolder favorites filters notes by isFavorite even if Favorites folder is missing added and passes after fix |
| Test Review | ✅ Complete | 2026-05-19 | test_review_v1.md created. APPROVED |
| Knowledge Capture | ✅ Complete | 2026-05-19 | Past bug documentation entry created |

## Key Decisions
1. Treat the "Favorites" pill on the Home Screen as a smart filter rather than a folder-binding. Filter active notes directly using `it.isFavorite` boolean.

## Files Changed
- `app/src/main/java/com/example/notesapp/ui/home/viewmodel/HomeViewModel.kt` (modified)
- `app/src/test/java/com/example/notesapp/ui/home/viewmodel/HomeViewModelTest.kt` (modified)

## Knowledge Artifacts
- `docs/knowledge/past-bugs/2026-05-19-home-favorites-pill.md` — Documents the Favorites filtering issue and the regression test details.

## Open Items
None.
