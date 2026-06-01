# Test Review — comments-all v1

**Feature**: Collaborative Block Comments, Discussion Bottom Sheet, and @ Mentions  
**Date**: 2026-06-01  
**Verdict**: REVISION REQUIRED

## Test Results

| Check | Result |
|-------|--------|
| `testDebugUnitTest` | FAIL — `compileDebugKotlin` fails before tests execute. |
| `koverLog` overall | FAIL — coverage cannot be generated from the current working tree. |
| `koverLog` new classes | FAIL — coverage cannot be generated from the current working tree. |
| `connectedDebugAndroidTest` | FAIL — `compileDebugKotlin` fails before instrumented tests execute. |

## Coverage Distribution

The existing `docs/current/unit_test/test_report_comments-001_v1.md` reports overall 81.7456% and new classes 100%, but that evidence is stale because the current working tree does not compile. No current coverage number is valid for acceptance.

## Test Quality Findings

| Check | Result | Notes |
|-------|--------|-------|
| Naming follows descriptive pattern | PARTIAL | New tests use mixed naming styles; e.g. `testGetDateSuggestions_withFixedClock` does not follow the requested behavior pattern. |
| Assertions are specific | PARTIAL | Basic assertions exist, but UI tests often assert callbacks on isolated `DiscussionBottomSheet` rather than the integrated ViewModel behavior required by the contract. |
| Unit tests are isolated | PARTIAL | Repository tests mock DAO/API directly; useful as unit tests, but not a substitute for required integration tests. |
| Shared JSON scenarios used | FAIL | No `comments_sync.json` or comments shared scenario exists. `NoteCommentRepositoryTest.kt` uses inline DTO data and mocked `NotesApiService`. |
| API edge cases covered | FAIL | Missing 4xx, 5xx, malformed payload, and unknown/fallback coverage for comments endpoints. |
| DAO coverage | FAIL | Contract requires `NoteBlockCommentDaoTest`; no such test exists. |
| UI acceptance coverage | FAIL | Tests use implementation tags (`discussion_sheet_title`) rather than contract tags (`discussion_header`) and do not verify the editor comment button tag `note_editor_comment_button`. |
| Mention integration coverage | FAIL | `MentionsAutocompleteTest` passes `isMentionSuggestionsVisible = true` directly and only asserts callback output, so it does not prove typing `@` through the ViewModel opens suggestions or inserts completion at the actual cursor. |
| Regression test confirmed | N/A | This is feature work, not a bug fix. |

## Blocking Test Gaps

1. Add/fix the compile-blocking production API references before tests can run.
2. Add `NoteBlockCommentDaoTest` covering primary key replacement and note/block query ordering.
3. Add `sharedContracts/test-scenarios/comments_sync.json` and MockWebServer integration coverage for `listNoteBlockComments` and `createNoteBlockComment`.
4. Add error-path tests for 4xx, 5xx, malformed payloads, and invalid/unknown response values.
5. Add integrated ViewModel/UI tests for `@` typing, mention button behavior, cursor-position insertion, popup dismissal, and the contract test tags.
6. Re-run `testDebugUnitTest`, `connectedDebugAndroidTest`, and `koverLog` after the build is green.
