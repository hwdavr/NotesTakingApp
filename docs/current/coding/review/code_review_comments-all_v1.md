# Code Review — comments-all v1

**Feature / Bug**: Collaborative Block Comments, Discussion Bottom Sheet, and @ Mentions  
**Reviewer**: Agent  
**Date**: 2026-06-01  
**Verdict**: BLOCK

## Build & Test Results

| Check | Result | Notes |
|-------|--------|-------|
| `assembleDebug` | FAIL | `NoteEditorScreen.kt:145`, `:146`, `:148` unresolved references to `addParagraphBlock`, `addImageBlock`, `addTableBlock`. |
| `testDebugUnitTest` | FAIL | Fails before tests execute due to same `compileDebugKotlin` errors. |
| `koverLog` overall | FAIL | Fails before coverage can be generated due to same compile errors. Prior report claims 81.7456%, but it is stale for this working tree. |
| `koverLog` new classes | FAIL | Fails before coverage can be generated. Prior report claims 100%, but it is stale for this working tree. |
| `connectedDebugAndroidTest` | FAIL | Fails before installation/test execution due to same compile errors. |
| `ktlintCheck` | FAIL | Unused import in `NoteEditorScreen.kt:111`, unsorted imports in `AppModule.kt:3`, unused import in `DiscussionBottomSheet.kt:52`. |
| `detekt` | PASS | Exit code 0. |
| `lintDebug` | FAIL | Fails before lint due to same `compileDebugKotlin` errors. |
| `check-compose-rules.sh` | PASS | Exit code 0; 0 scripted compose violations. |
| `check-localization-rules.sh` | FAIL | 19 null `contentDescription` violations in touched UI files. |
| `check-architecture-rules.sh` | PASS | Exit code 0; 0 scripted architecture violations. |
| Suppression audit | PASS | Script reports no new suppressions in the app/source diff. |

## Required Findings

1. **Critical: feature does not compile.**  
   `app/src/main/java/com/example/notesapp/ui/editor/screen/NoteEditorScreen.kt:145` references `viewModel::addParagraphBlock`, `:146` references `viewModel::addImageBlock`, and `:148` references `viewModel::addTableBlock`, but `NoteEditorViewModel` replaced those APIs with `addBlock(block: EditorBlock)`. This blocks all build, test, lint, coverage, and UI verification gates.

2. **Required: localization/accessibility gate fails.**  
   `scripts/check-localization-rules.sh` reports 19 violations where interactive icons use `contentDescription = null`, including the newly added comment button at `NoteEditorScreen.kt:971`. `DiscussionBottomSheet.kt` also contains hardcoded user-visible strings such as `"Discussion"` (`:111`), `"No comments yet. Start the discussion!"` (`:176`), `"Type a comment..."` (`:249`), `"Attach file"` (`:280`), `"Mention"` (`:293`), `"Send"` (`:317`), `"Dates"` (`:452`), `"Collaborators"` (`:499`), and `"Other Notes"` (`:563`). These violate the no-hardcoded-strings and content-description rules even though the script only surfaced part of the issue set.

3. **Required: implementation does not match the sprint contract test tags.**  
   The contract requires `note_editor_comment_button`, `discussion_header`, and `discussion_close_button`. The implementation uses `editor_comment_button` (`NoteEditorScreen.kt:963`), `discussion_sheet_title` (`DiscussionBottomSheet.kt:115`), and `discussion_sheet_close` (`DiscussionBottomSheet.kt:121`). Contract-driven UI tests looking for the required tags will fail.

4. **Required: mention trigger and completion are wired to the wrong selection source.**  
   `NoteEditorViewModel.getMentionQuery()` uses `selectionStart` from the editor block selection (`NoteEditorViewModel.kt:454-466`), but the discussion comment `OutlinedTextField` never reports its cursor selection back to the ViewModel (`DiscussionBottomSheet.kt:244-304`). Typing `@` or pressing the mention button can leave `selectionStart` at an unrelated editor cursor or `0`, causing mention suggestions and completion to fail in real use. The current UI tests inject `isMentionSuggestionsVisible = true` directly and therefore do not verify the ViewModel integration path required by acceptance criteria 7 and 10.

5. **Required: API/test contract evidence is missing.**  
   The sprint contract requires `NoteBlockCommentDaoTest`, MockWebServer integration using a shared JSON scenario such as `comments_sync.json`, and no inline mock data. There is no `NoteBlockCommentDaoTest`, no `sharedContracts/test-scenarios/comments_sync.json`, and `NoteCommentRepositoryTest.kt` directly mocks `NotesApiService` with inline `ApiNoteBlockComment` data. This fails acceptance criteria 1 and 2 and the shared scenario testing rule.

6. **Required: new API error-path coverage is incomplete.**  
   The test review found no 4xx, 5xx, malformed payload, or unknown/fallback response coverage for the new comments endpoints. The repository test covers happy path plus thrown exception fallback only.

7. **Required: hardcoded UI/domain labels leak into state and models.**  
   `NoteEditorViewModel.kt:500`, `:511`, `:528`, and `:560` hardcode `"You"`, `"Guest"`, `"My Notes"`, and `"... $remaining more results"`. `MentionsCalculator.kt:44`, `:49`, and `:54` hardcode user-visible date labels. These values are later rendered in UI and should be represented as localizable UI resources or structured values that the UI maps to resources.

8. **Required: fully-qualified class names are used inline.**  
   The project forbids inline fully-qualified class names. Violations include `NoteEditorViewModel.kt:53-55`, `NoteEditorViewModel.kt:75`, and `DiscussionBottomSheet.kt:74-76`, `:429-431`.

9. **Required: comment timestamps are calculated inside a Composable with wall-clock time.**  
   `CommentCard` computes relative times via `System.currentTimeMillis()` and hardcoded string segments at `DiscussionBottomSheet.kt:336-342`. This is UI-layer logic, nondeterministic, not localized, and not injectable/testable.

10. **Required: comments are not sorted by the UI or guaranteed at component boundary.**  
    The DAO query orders by `createdAt ASC`, but `DiscussionBottomSheet` renders the `comments` parameter as received (`DiscussionBottomSheet.kt:189-190`). Acceptance criterion 5 says the comments list renders chronological order; the UI component tests pass preordered data and do not catch unordered state.

## Rule Enforcement Summary

| Area | Status | Notes |
|------|--------|-------|
| Compose rules | FAIL | Script passed, but manual review found hardcoded strings, UI-side formatting/time logic, and test tag mismatch. |
| Localization rules | FAIL | Script failed with 19 null content-description violations; manual review found additional hardcoded text. |
| Architecture rules | FAIL | Script passed, but manual review found inline FQCNs and business/formatting logic in UI. |
| API contract rules | FAIL | OpenAPI contains the endpoints, but integration tests do not use shared JSON scenarios and error-path coverage is missing. |
| Review checklist | FAIL | Build, tests, lint, localization, shared scenarios, and coverage gates fail or cannot be executed. |

## Overall

Block. The feature is not in a mergeable state because it does not compile. Even after fixing compilation, the implementation must address localization/accessibility, contract tag mismatches, missing DAO/shared-scenario integration tests, cursor handling for mentions, and hardcoded UI labels before another evaluation pass.
