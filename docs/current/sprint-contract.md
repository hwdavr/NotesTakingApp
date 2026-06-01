# Sprint Contract — Collaborative Block Comments and Discussion

---

## 🏃 Sprint Overview

*   **Sprint:** P06-01
*   **Feature:** Collaborative Block Comments, Discussion Bottom Sheet, and @ Mentions
*   **Duration:** 1 sprint

---

## 🎯 Scope

### In Scope
*   **Database & Storage**:
    *   Create Room database tables and DAO for caching comments (`NoteBlockCommentEntity`).
    *   Local offline caching and API integration for `GET` and `POST` comments per note/block.
*   **Editor Interface**:
    *   Add comment button to the editor default bottom bar.
    *   Open "Discussion" bottom sheet when clicking the comment button while a text block is active/focused.
*   **Discussion Bottom Sheet UI**:
    *   Render focused block snippet with a yellow vertical accent bar as contextual preview.
    *   Display scrollable chronological comments list with user avatars, relative times (e.g. "26m"), and bodies.
    *   Add comment input field with circular avatar, attachment button (visual only), `@` mention button, and blue circular send button.
*   **@ Mention System**:
    *   Typing `@` or clicking the `@` button triggers a custom floating autocomplete list showing:
        *   **Dates**: e.g., "Today [Exact Date]", "Tomorrow", "Next Tuesday 3pm".
        *   **Collaborators**: Note owner and shared users from `NoteShareRepository`, with role badges (`Guest`, `You`).
        *   **Other Notes**: Local user notes showing titles and folder path breadcrumbs.
        *   **Footer**: "... N more results" summary row.
    *   Selecting a suggestion auto-inserts the plain-text/date mention into the text input.

### Out of Scope
*   Adding actual file attachments via the paperclip icon (visual button only).
*   Live WebSockets real-time sync (standard HTTP pull/push sync is sufficient).
*   Functional reactions (smiley face) or comment deletion/options (three dots).
*   Thread resolution status or marking comments as resolved (checkmark icon is static).

---

## 👥 Roles

| Role | Responsibility | Handover Trigger |
| :--- | :--- | :--- |
| **Planner** | Define acceptance criteria, scope boundaries, and the verification plan before implementation begins. | Hands off `sprint-contract.md` to **Generator**. |
| **Generator** | Implement the component, application layers, and tests based on the planner specification. | Hands off code and passing tests to **Evaluator**. |
| **Evaluator** | Review implementation against acceptance criteria, run static analysis/tests, and verify coverage. | Hands off final APPROVED audit reports to the User. |

---

## 📐 Acceptance Criteria

1. **Database Schema**: Room entity `NoteBlockCommentEntity` is defined matching OpenAPI contract `NoteBlockComment`, registered in `AppDatabase`, and fully tested inside `NoteBlockCommentDaoTest` for correct primary key mapping and insertion conflict resolution.
2. **API & Repository Integration**: Retrofit network client `NotesApiService` covers comments endpoints. `NoteCommentRepository` exposes cache flows and implements sync-on-load. Integration tests run against mock web server using shared JSON scenarios (e.g., `comments_sync.json`) without inline mock data.
3. **Editor Comment Action**: Comment button in the bottom bar has test tag `note_editor_comment_button`. Clicking it while a text block is focused launches the bottom sheet, verified in UI tests.
4. **Discussion Bottom Sheet Context**: Bottom sheet renders centered header "Discussion" (test tag `discussion_header`, text loaded via `stringResource(R.string.discussion_title)`) and close button (`discussion_close_button`). Contextual block preview is prefixed by a vertical bar styled with design system secondary color token `MaterialTheme.colorScheme.tertiary` (accent yellow).
5. **Comments List UI**: Scrollable list (test tag `discussion_comments_list`) renders comments in chronological order. Comments render round avatar initials, display names, formatted relative times (no hardcoded string segments), and comment bodies.
6. **Sending Comments**: Tapping the primary send button (`discussion_send_button`, colored `MaterialTheme.colorScheme.primary`) in the input field (`discussion_comment_input`) immediately updates the cache, triggers `POST` endpoint, and appends to UI state immediately.
7. **@ Mention Trigger**: Typing `@` or tapping the mention button (`discussion_mention_button`) in the input field presents the suggestions popup (`mention_suggestions_popup`).
8. **Prepopulated Date Suggestions**: Date suggestions (e.g. "Today", "Tomorrow", "Next Tuesday 3pm") are computed deterministically by injecting a configurable `Clock` instance, allowing predictable, robust JVM Unit tests inside `MentionsCalculatorTest`.
9. **Dynamic User & Note Suggestions**: Collaborator suggestions render roles via badges with localized text (`R.string.mention_badge_you` for "You", `R.string.mention_badge_guest` for "Guest"). Note suggestions render matching titles and folder breadcrumbs without hardcoded texts.
10. **Mention Auto-Completion**: Clicking a suggestion auto-completes plain-text representation (e.g. `@Today` or `@Walter Huang`) at the selection cursor and closes the popup, verified via Compose UI tests.

---

## 🧪 Verification Plan

1. **Verification 1 (Database Schema)**:
   *   *Verification Method*: Write and execute SQLite Room DAO tests validating insertion, data integrity, and replacement.
   *   *Command*: `./gradlew testDebugUnitTest --tests "com.example.notesapp.data.local.NoteBlockCommentDaoTest"`
2. **Verification 2 (API & Repository)**:
   *   *Verification Method*: Run JVM integration tests using the shared JSON scenario `comments_sync.json` to verify Retrofit parsing and repository sync flows.
   *   *Command*: `./gradlew testDebugUnitTest --tests "com.example.notesapp.data.repository.NoteCommentRepositoryIntegrationTest"`
3. **Verification 3 (Editor Comment Action)**:
   *   *Verification Method*: Run instrumented Compose UI tests asserting `note_editor_comment_button` interaction opens the bottom sheet.
   *   *Command*: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.DiscussionSheetUiTest#testCommentButtonLaunchesSheet`
4. **Verification 4 (Discussion Bottom Sheet Context)**:
   *   *Verification Method*: Run instrumented Compose UI tests validating header strings from localized resources and checking the prefix vertical line has color matching `MaterialTheme.colorScheme.tertiary`.
   *   *Command*: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.DiscussionSheetUiTest#testContextBlockPreviewAndStyling`
5. **Verification 5 (Comments List UI)**:
   *   *Verification Method*: Run instrumented Compose UI tests asserting chronological list ordering and relative timestamp rendering logic.
   *   *Command*: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.DiscussionSheetUiTest#testCommentsChronologicalRendering`
6. **Verification 6 (Sending Comments)**:
   *   *Verification Method*: Write JVM Unit tests inside `NoteEditorViewModelTest` asserting immediate state emission on message sending, and integration tests confirming network API triggering.
   *   *Command*: `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.viewmodel.NoteEditorViewModelTest#testSendCommentFlow"`
7. **Verification 7 (@ Mention Trigger)**:
   *   *Verification Method*: Run instrumented Compose UI tests checking that inputting `@` or clicking the mention button displays `mention_suggestions_popup`.
   *   *Command*: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.MentionsAutocompleteTest#testMentionTriggerDropdown`
8. **Verification 8 (Prepopulated Date Suggestions)**:
   *   *Verification Method*: Run JVM Unit tests in `MentionsCalculatorTest` supplying a fixed, mocked `Clock` to assert exact, predictable date suggestion strings.
   *   *Command*: `./gradlew testDebugUnitTest --tests "com.example.notesapp.ui.editor.model.MentionsCalculatorTest"`
9. **Verification 9 (Dynamic User & Note Suggestions)**:
   *   *Verification Method*: Run instrumented Compose UI tests verifying localized role badges (You/Guest) and local notes matching titles/breadcrumbs.
   *   *Command*: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.MentionsAutocompleteTest#testCollaboratorAndNoteSuggestions`
10. **Verification 10 (Mention Auto-Completion)**:
    *   *Verification Method*: Run instrumented Compose UI tests asserting that clicking a suggestion inserts completion text at the correct cursor index and dismisses the suggestions dialog.
    *   *Command*: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.MentionsAutocompleteTest#testAutoCompletionAndClose`

---

## 📊 Sprint Log

| Phase | Agent | Target / Outcome | Notes & Core Decisions |
| :--- | :--- | :--- | :--- |
| **Planning** | Planner | `sprint-contract.md` compiled | Criteria defined and scope boundaries set. |
| **Review 1** | Evaluator | REJECTED | Demanded 1-to-1 verification mapping, testTag declarations, Clock injection for unit-testing, and localization checks. |
| **Revision 1** | Planner | APPROVED WITH AMENDMENTS | Addressed all 10 criteria with explicit 1-to-1 verification plans, exact testTags, test commands, Clock provider, and string resources. |
| **Final Review** | Evaluator | APPROVED | Refined contract satisfies all safety, testability, and architectural rules. |
