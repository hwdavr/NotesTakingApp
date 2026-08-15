# Feature Spec — Note Emoji

**Date**: 2026-08-15  
**Status**: Draft — awaiting specification approval  
**Related design**: `design.md`

---

## Objective

Enable people editing a note to insert standard Unicode emoji from the existing Note Editor toolbar control. The picker makes expressive text fast to write without introducing a separate note metadata type or changing the note model.

## User Goal

As a note editor, I want to find and insert an emoji at my current body-text cursor so that I can express context and tone in a note without leaving the app.

## Scope

### In Scope

- Activate the existing Note Editor **Insert emoticon** toolbar control.
- Provide an in-app emoji picker with a searchable Unicode emoji catalog and these categories: Recent; Smileys & Emotion; People & Body; Animals & Nature; Food & Drink; Activities; Travel & Places; Objects; Symbols; and Flags.
- Support skin-tone variants for eligible people/body emoji.
- Insert a selected emoji into the focused editable body text block at the current cursor or selection. If no body text block is focused, create and focus a new paragraph, then insert the emoji.
- Keep the picker open after insertion so users can insert multiple emoji.
- Persist selected emoji as normal Unicode rich-text content in the note document, including local save, sync, sharing, and text/Markdown/PDF export.
- Maintain a locally persisted Recent list across app restarts; a selected skin-tone variant appears in Recent as that exact variant.
- Display the toolbar control disabled for read-only/shared notes.

### Out Of Scope

- Custom emoji packs.
- GIFs, stickers, or other media insertion.
- Emoji reactions.
- Emoji note covers/icons.
- Emoji insertion in note titles.

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---------------|---------|---------|
| Existing Jetpack Compose Material 3 | Existing project dependency | Render the picker, search field, tabs, and compact skin-tone selector. |
| Existing AndroidX DataStore Preferences | Existing project dependency | Persist the device-local Recent emoji list. |
| Android Unicode emoji font support | Platform API 24+ | Render selected Unicode emoji in note text. |

No third-party emoji SDK, network API, permission, database migration, or backend-contract change is required.

### Key Technical Decisions

- **Emoji representation**: Store emoji as ordinary Unicode strings in existing `RichText`; do not introduce an emoji block, attribute, DTO, or note-column change.
- **Picker catalog**: Ship a deterministic, app-bundled catalog with Unicode text, localized display name/keywords, category, and optional skin-tone variants. Search matches display names and common keywords locally.
- **Insertion boundary**: The presentation layer owns the insertion request and document update. The screen owns only focused text-field selection and passes a typed insertion action to the ViewModel; a Composable does not mutate a repository or document directly.
- **Recents**: Persist the Most Recently Used emoji strings locally through a data/domain contract backed by DataStore. The exact selected Unicode sequence, including a skin-tone modifier, is recorded.
- **Picker presentation**: Use the project-standard Material 3 `ModalBottomSheet`, not a new destination or fullscreen screen. The sheet does not cause navigation or a network operation.

### External APIs / Services

- None. The emoji catalog, search, recents, and insertion operate fully on-device.

### Platform & Compatibility Constraints

- **Min SDK**: Project default API 24; target SDK 34.
- **Permissions required**: None.
- **Other constraints**: Rendering depends on the device’s installed emoji font; unsupported Unicode sequences remain stored and exported as their original text sequence. The picker must support light/dark theme, TalkBack, RTL, font scaling, narrow phones, landscape, and tablets.

---

## Functional Requirements

- **FR-001**: The existing Insert emoticon control in the Note Editor MUST be active for editable notes and open the emoji picker on tap.
- **FR-002**: For a read-only/shared note, the Insert emoticon control MUST remain visible but disabled, with disabled semantics and no picker-opening action.
- **FR-003**: The picker MUST provide local search by emoji name and common keyword, a clear search action, the nine approved categories, and a Recent category.
- **FR-004**: The picker MUST expose a skin-tone selection path for every catalog emoji with supported skin-tone variants and insert the exact chosen Unicode variant.
- **FR-005**: Selecting an emoji MUST insert it into the currently focused editable body text block at the current cursor or replace the current body-text selection. It MUST NOT alter the title.
- **FR-006**: If an editable body text block is not focused when an emoji is selected, the system MUST append a new paragraph, focus it, and insert the selected emoji there.
- **FR-007**: The picker MUST remain open after each successful insertion and the body cursor MUST move immediately after the inserted Unicode sequence.
- **FR-008**: Each successfully selected emoji MUST be saved to locally persisted Recents across app restarts, ordered by latest selection; a selected skin-tone variant is saved as that exact variant.
- **FR-009**: Emoji inserted through the picker MUST be serialized with the existing note document, survive note reload and local/offline save, and be represented unchanged by existing sync, sharing, and export paths.

## Acceptance Criteria

- **AC-001**: Given an editable loaded note, when the user taps Insert emoticon, then the emoji bottom sheet opens above the Note Editor with Recent selected and the editor remains the current navigation destination.
- **AC-002**: Given a read-only loaded note, when the user encounters Insert emoticon, then it is visible with disabled semantics and cannot open the picker.
- **AC-003**: Given the picker is open, when the user taps each approved category, then the corresponding catalog results are shown; when no persisted items exist, Recent shows its defined empty state.
- **AC-004**: Given the picker is open, when the user searches an emoji name or common keyword, then matching catalog results are shown; when no result matches, then the picker states “No emoji found” and offers a clear-search action.
- **AC-005**: Given an emoji with skin-tone variants, when the user opens its variant selector and chooses a variant, then that exact variant is inserted and displayed in Recent.
- **AC-006**: Given a focused body-text block with a cursor or selected text, when the user selects an emoji, then the emoji is inserted/replaces the selection at that exact body position, the cursor advances after it, and the picker remains open.
- **AC-007**: Given no body text block is focused, when the user selects an emoji, then a new paragraph is appended, focused, and contains the selected emoji; the title remains unchanged.
- **AC-008**: Given a selected emoji, when the app is restarted and the picker opens, then that exact emoji appears in Recent.
- **AC-009**: Given a note containing picker-inserted emoji, when it is saved, reloaded, synchronized/shared, or exported, then the same Unicode emoji is retained as normal note text.

## Data And Persistence

- The note document continues to store body content as existing `NoteDocument`/`RichText` JSON. A selected emoji is a normal Unicode substring, with no new Room field or migration.
- Recent emoji are device-local DataStore preferences, separate from note content. They are not synced, shared, or exported.
- Existing note save/autosave and repository/sync flows persist the changed document. The picker itself performs no direct database, repository, or network access.

## Edge Cases

| Condition | Required behavior |
|-----------|-------------------|
| No focused body block | Append and focus a new paragraph, then insert the selection. |
| Cursor range is selected | Replace the selected range with the selected Unicode emoji. |
| Read-only note | Keep control visible and disabled; picker cannot open. |
| Empty Recent list | Show a localized empty state; category and search controls remain available. |
| No search match | Show localized “No emoji found” state and a clear-search action. |
| Catalog/Recents read failure | Show the deterministic bundled catalog and an empty Recent list; note editing and insertion remain available. |
| Device font lacks a glyph | Preserve the original Unicode sequence for save, reload, sync, share, and export; platform rendering may show its fallback glyph. |
| Configuration change or process recreation | Restore the editor state; persisted Recents reload from DataStore. |

## Explicit Assumptions

No material product assumptions remain. The user confirmed the insertion location, picker behavior, categories, search behavior, recents persistence, skin-tone behavior, and non-goals on 2026-08-15.

## Open Questions

All questions are ✅ Answered before this document is approved.

| # | Question | Status | Answer |
|---|----------|--------|--------|
| Q1 | Where should emoji be inserted? | ✅ Answered | Body text only; never the title. Insert at the focused cursor, or create a new paragraph when none is focused. |
| Q2 | How should the existing control behave? | ✅ Answered | Use the existing toolbar control; keep it visible but disabled for read-only notes. |
| Q3 | What picker capabilities are required? | ✅ Answered | Categories, local search, Recent, and skin-tone variants. The picker stays open after selection. |
| Q4 | What categories are required? | ✅ Answered | Recent, Smileys & Emotion, People & Body, Animals & Nature, Food & Drink, Activities, Travel & Places, Objects, Symbols, and Flags. |
| Q5 | How are emoji and recents persisted? | ✅ Answered | Emoji are normal Unicode note text; Recent persists locally across restarts, including the exact selected skin-tone variant. |
| Q6 | What is excluded? | ✅ Answered | Custom packs, GIFs/stickers, reactions, and note-cover emoji. |

## Screen States

| State | Requirement | Acceptance Criteria |
|-------|-------------|---------------------|
| Closed | The existing toolbar shows its regular enabled or disabled state; no additional editor chrome is visible. | AC-001, AC-002 |
| Content | The modal picker shows Recent or an approved category with search, tabs, and emoji cells. | AC-001, AC-003, AC-005, AC-006 |
| Empty Recent | A localized empty-state message explains that chosen emoji will appear here; category/search controls remain usable. | AC-003 |
| Empty Search | A localized “No emoji found” message and clear-search action are shown. | AC-004 |
| Recoverable data failure | The bundled catalog remains available and Recent behaves as empty without blocking note editing. | AC-003, AC-009 |

## Navigation

- **Entry**: Tap the existing Insert emoticon control in the Note Editor for an editable note.
- **Back/cancel**: Back gesture, sheet scrim, or close action dismisses the picker and returns focus to the existing Note Editor without changing note content.
- **Success**: Insertion occurs in the existing Note Editor; the picker stays open and no navigation occurs.
- **Error recovery**: Search/Recent failures remain inside the sheet; the user can clear search, choose another category, or dismiss it.

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|-------------|----------------|---------------------|
| FR-001, FR-002 | Screen 1 — Toolbar trigger and disabled state | AC-001, AC-002 |
| FR-003 | Screen 1 — Picker header, category rail, results grid, states | AC-003, AC-004 |
| FR-004 | Screen 1 — Emoji grid and skin-tone selector | AC-005 |
| FR-005, FR-006, FR-007 | Screen 1 — Selection and insertion rules | AC-006, AC-007 |
| FR-008, FR-009 | Screen 1 — Recent and persistence behavior | AC-008, AC-009 |

## Verification Expectations

- **Unit**: Catalog search/category filtering, skin-tone variant selection, recent-MRU serialization/fallback, and ViewModel document insertion at a cursor/selection or into a new paragraph.
- **Integration**: DataStore-backed Recent persistence across repository recreation and note-document serialization/save/reload with Unicode emoji. No API-specific scenario is required because this feature has no new endpoint.
- **Instrumented UI**: Editable/read-only toolbar state, opening/dismissing the bottom sheet, category/search/empty states, skin-tone selection, cursor insertion, no-focused-block paragraph insertion, and a configuration-safe rendered Unicode path.
- **Manual/visual**: Compare the editable, read-only, populated Recent, empty search, and skin-tone states to `design.md` and `design/mockup_note_editor_emoji_picker.png` in Light and Dark themes, portrait and landscape.

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved assumptions remain.
- [x] All visual states are defined in `design.md`.
- [x] All navigation outcomes are defined.
