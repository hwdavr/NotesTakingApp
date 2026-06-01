# Requirement Summary — Collaborative Block Comments and Discussion

**Date**: 2026-05-31
**Status**: Final

---

## Design Reference

- Original Mockup: [discusion_bottom_sheet.png](file:///mnt/data/Projects/NotesApp/NotesTakingApp/docs/current/design/discusion_bottom_sheet.png)

---

## User Goal

> As an Android app user editing or viewing notes, I want to leave block-level comments and participate in discussion threads on shared notes, as well as use @ mentions for collaborators, dates, or other notes, so that we can collaborate asynchronously and keep our context organized.

---

## Requirement Summary

We will build an interactive discussion system integrated into the note editor. A new comment button will be placed on the editor's bottom bar. Selecting any text block and clicking the comment button will display a "Discussion" bottom sheet tailored to that block. Inside the bottom sheet, users will see existing comments, view the current block snippet context, and add new comments. Collaborative discussions will be fully supported for shared notes, enabling different users to comment, reply, and view each other's posts in real-time or via local offline sync. The comment input field will support a `@` mention system, which opens a custom dropdown suggestion list as the user types or clicks the `@` button, providing autocomplete options for other collaborators, dates, and related notes.

---

## Expected Behavior

1. **Comment Button in Bottom Bar**: A comment button (represented by a speech bubble or similar icon) is visible in the editor's bottom bar when editing/viewing a note.
2. **Launch Discussion Sheet**: Tapping the comment button while a text block is focused opens a bottom sheet with the title "Discussion".
3. **Block Context Snippet**: The bottom sheet displays a preview/snippet of the focused text block content at the top of the discussion, prefixed with a yellow vertical accent bar as shown in the design mockup.
4. **Threaded Comments List**: Comments attached to the block are displayed chronologically, showing the author's avatar (circular, with initial), author's display name, relative timestamp (e.g., "26m"), and comment body text.
5. **Multi-User Discussion**: For shared notes, when different users add comments, their comments are displayed in the list.
6. **New Comment Input Field**: A text field at the bottom of the sheet with a circular user avatar on the left, an attachment icon, a `@` mention icon, and a blue circular "send/upload" action button.
7. **Send Comment**: Typing text and clicking the send button persists the comment locally, pushes it to the backend via the API, and appends it to the discussion list immediately.
8. **@ Autocomplete Trigger**: Clicking the `@` button or typing `@` inside the comment text input triggers a floating autocomplete suggestion list below or overlaying the sheet.
9. **Mention Suggestions**: The suggestion list supports:
   - **Dates**: Clock icon, bold relative day description (e.g. "Today", "Next Tuesday 3pm"), and full date format (e.g., "15 May 2026").
   - **Users/Collaborators**: Avatar, user's display name, and optional role label (e.g. "Walter Huang (You)", "Huang Guest").
   - **Other Notes**: Document icon, note title in bold, and note folder/collection name path (e.g. "鼻炎按摩" under "Life / 健康").
   - **Summary Row**: A footer item showing "... N more results".
10. **Selecting Mention**: Tapping a suggestion inserts the selected mention into the text input at the cursor position (e.g. `@Today` or `@Walter Huang`).

---

## Business Rules

1. **Comment Creation Access**: Any collaborator with view or edit access to a note can view comments and add comments to its blocks.
2. **Offline Resilience**: Comments must be stored in the local SQLite database and support offline caching/sync. If the device is offline, comments can still be drafted and queued for remote sync.
3. **Owner Identification**: The note owner must be displayed correctly in comments and collaborator mentions.

---

## Known Constraints

1. **WSL and Gradle Build**: Must compile cleanly under the project's WSL Gradle environment with minSdk 24 and targetSdk 34.
2. **OpenAPI Compatibility**: Must align with the existing `openapi.yaml` endpoints:
   - `GET /v1/notes/{itemID}/blocks/{blockID}/comments`
   - `POST /v1/notes/{itemID}/blocks/{blockID}/comments`
3. **Compose UI / Material 3**: Must use standard Jetpack Compose Material 3 components, such as `ModalBottomSheet` or similar, styled premium and harmoniously to fit the existing visual palette.
4. **No Placeholders**: Must not use empty placeholders or unverified network images; use generated assets and local mock values where appropriate.

---

## Non-Goals

- Thread resolution or mark-as-done state persistence in the backend (unless specified in OpenAPI or simple local field).
- Editing or deleting comments after they are posted (unless requested in later phases).
- Real-time WebSockets synchronization (sync-on-resume and manual refresh, or periodic background polling via standard app repository sync is sufficient).
- Adding actual file attachments via the paperclip icon (this button is represented in the UI but is non-functional in this sprint).

---

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|------------|---------------|
| A1 | Comments are only allowed on Text/Paragraph blocks, not on Table or Image blocks. | If comments are needed on Tables or Images, we need block tracking models and special UI indicators for non-text blocks. |
| A2 | User display names and avatars can be resolved from the NoteShare database and current user's profile. | If collaborator profiles cannot be resolved, comment cards will only display raw email addresses or "Unknown User". |
| A3 | The local room database needs new tables to cache comments and synchronize them offline. | If comments are API-only and not cached locally, users cannot view or write comments while offline, violating the app's offline-first architecture. |

---

## Open Questions

All questions must be ✅ Answered before this document is approved.

| # | Question | Status | Answer |
|---|----------|--------|--------|
| Q1 | **Supported Block Types**: Can users comment on any block (including Images and Tables), or only on Text blocks as suggested by the focused-text-block pattern? | ✅ Answered | Only Text/Paragraph blocks are commentable. Comments on Table or Image blocks are out of scope. |
| Q2 | **Mentions Storage Format**: How should `@` mentions be saved in the database/backend? Are they stored as plain text (e.g., "@Today") or do we need structured metadata format (e.g. `@[user:123]` or custom JSON annotation inside the comment body)? | ✅ Answered | Dates are stored as exact ISO dates/timestamps, translated to relative dates (Today, Tomorrow, etc.) upon display. Collaborator mentions are stored as-is in the comment body. |
| Q3 | **Mention Data Sources**: <br>1. For **Users**: Should we query collaborators in the note's active shares? <br>2. For **Notes**: Should we query all local user notes? <br>3. For **Dates**: What predefined date suggestions should be generated (e.g., Today, Tomorrow, Next Tuesday)? | ✅ Answered | 1. Users are queried from NoteShares (+ owner). <br>2. Notes are queried from local SQLite database of notes. <br>3. Predefined date suggestions (Today, Tomorrow, Next Tuesday 3pm) will be generated locally. |
| Q4 | **Discussion Actions in Mockup**: The mockup shows action buttons on each comment card (smiley face icon for reactions, checkmark for resolve, and three-dots for more options). Are these actions out-of-scope/static for this release, or do any need behavior? | ✅ Answered | Reactions, resolve status, and more options menu are static visual elements and are out-of-scope for this release. |
| Q5 | **Current User Identity**: The mockup shows "Walter Huang (You)". Since `AuthManager` only stores `profileEmail`, how should the app resolve the current user's display name and initial (e.g., should we use the email prefix, or query `NoteShare` collaborators matching current user's email, or is there a local display name)? | ✅ Answered | Fall back to the current user's email prefix as their display name, matching against local NoteShare/owner records where possible to display their configured display name, and append `(You)`. |

