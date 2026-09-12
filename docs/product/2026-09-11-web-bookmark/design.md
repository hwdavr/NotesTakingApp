# Feature Design — Web Bookmark in Notes

**Date**: 2026-09-11  
**Status**: Approved — 2026-09-11  
**Source request**: Add a web bookmark to a note.  
**Related spec**: `spec.md`  
**Project design system**: [`docs/product/design_system.md`](../design_system.md)  
**Approved design-system exceptions**: On 2026-09-12, the user approved the actions-sheet
mockup as authoritative over the earlier close-button prose: omit a header close button and use
the mockup's title, plain Edit row, divider, and destructive Delete row. M3 scrim tap,
swipe-down, and system Back remain the dismissal paths.

## Baseline Reset After Review

The actual Note Editor baseline was captured from the deterministic `BasicBlocksPanelScreenTest` on the Android emulator. It preserves the current top action bar, lavender breadcrumb card, white editor canvas, horizontal editing rail, Basic Blocks panel, Advanced section, system bars, type scale, and spacing. The untouched captures below are the visual approval baseline. This design specifies only the Web Bookmark delta against them.

The previously generated generic mockups remain in `design/` only as superseded exploration history. They are not referenced as current design evidence.

## Existing Surface Baseline

| Existing surface | Baseline decision | Source test | Test method | Test-produced capture | Pulled baseline asset | Emulator execution evidence |
|------------------|-------------------|-------------|-------------|-----------------------|-----------------------|-----------------------------|
| Note Editor — Web Bookmark Card | Required | `app/src/androidTest/java/com/example/notesapp/ui/editor/screen/BasicBlocksPanelScreenTest.kt` | `captureBasicBlocksPanelTopState` | `notesapp_basic_blocks_panel_top.png` | `design/baseline_editor_basic_blocks_top.png` | `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.notesapp.ui.editor.screen.BasicBlocksPanelScreenTest` — BUILD SUCCESSFUL; 9/9 tests passed. |
| Add/Edit Web Bookmark Full Page | Not applicable — new destination; current editor baseline informs only the shell and navigation return | N/A — new destination | N/A — new destination | N/A — new destination | N/A — new destination | New surface will be verified by the production journey test after implementation. |
| Web Bookmark Actions Bottom Sheet | Not applicable — new action surface; current editor baseline informs only the scrim and sheet treatment | N/A — new surface | N/A — new surface | N/A — new surface | N/A — new surface | New surface will be verified by the production journey test after implementation. |

## Conditional Keyboard-Visible Mockup Contract

The Add/Edit Web Bookmark full page contains one URL text field. The keyboard-visible mockup `design/mockup_add_web_bookmark_full_page_keyboard_from_baseline_v2.png` shows the page still open above the IME with the web-address field focused and the bottom-pinned Save/Cancel actions reachable. The actions bottom sheet has no text input and is not present while the URL page is typing; tapping the URL field does not dismiss the page. `imePadding()` keeps the field and bottom action area reachable.

## Screens Covered

| # | Screen / Surface | Status |
|---|------------------|--------|
| 1 | Note Editor — Web Bookmark Card | Updated |
| 2 | Add/Edit Web Bookmark Full Page | New |
| 3 | Web Bookmark Actions Bottom Sheet | New |

## Screen 1 — Note Editor — Web Bookmark Card

### Purpose

Let a note author recognize, open, edit, and remove a saved web reference inline with the rest of the note document.

### UX Principles

- Preserve the existing Note Editor hierarchy and block order; a bookmark is an editor block, not a separate destination.
- Make the URL’s purpose and destination legible before opening it, while keeping the card compact beside existing code, diagram, chart, image, table, and voice blocks.
- Use the established Material 3 card family, semantic theme tokens, localized labels, and 48dp interactive targets.

### Entry And Exit

- **Entry points**: Existing Note Editor; add through Basic Blocks → Advanced → Web Bookmark. Existing cards are visible when the note is reopened.
- **Primary success exit**: The Add page returns to the editor and a card appears after the focused block or at the end of the document. Edit returns and updates the same card in place.
- **Cancel/back behavior**: Full-page Cancel, top-bar back, or system back discards only the unsaved URL draft. Card actions dismiss on scrim/back; deletion requires confirmation.
- **Failure exit or recovery**: Invalid URL stays on the full page with inline error. Metadata failure saves with host/blank fallback. Browser launch failure shows recoverable localized feedback and leaves the card unchanged.

### Information Architecture

1. **Document position**: The bookmark card occupies one block position in the scrollable note document.
2. **Card header**: Globe icon, metadata-derived title, and one compact More/actions control when the note is editable.
3. **Card context**: Optional description followed by the URL host or bounded URL text.
4. **Open affordance**: The card surface and a labeled Open action launch the validated URL in the external browser.
5. **Mutation affordance**: More/actions opens the Web Bookmark Actions bottom sheet. Edit navigates to the full-page editor; Delete opens the existing confirmation dialog.
6. **Advanced entry placement**: In the existing two-column Advanced grid, Web Bookmark becomes the right tile in the first visible row beside Code. Mermaid Diagram and chart choices continue in the following rows.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| Advanced Web Bookmark tile | Navigates to the Add full page from the existing Basic Blocks panel | Enabled, read-only unavailable | `basic_blocks_web_bookmark` |
| Bookmark card container | Presents one persisted bookmark block | Content, long text, dark theme, read-only | `editor_web_bookmark_block_<stable-block-id>` |
| Bookmark card visual bounds | Measures the visible card separately from its touch area | Content | `editor_web_bookmark_card_visual_<stable-block-id>` |
| Bookmark title | Shows metadata title or host fallback | Content, fallback, large text | `editor_web_bookmark_title_<stable-block-id>` |
| Bookmark description | Shows optional sanitized description | Present, absent, wrapped | `editor_web_bookmark_description_<stable-block-id>` |
| Bookmark URL/host | Exposes destination context without opening it automatically | HTTP, HTTPS, long URL | `editor_web_bookmark_url_<stable-block-id>` |
| Open bookmark action | Starts validated external URI launch | Enabled, no-handler error | `editor_web_bookmark_open_<stable-block-id>` |
| Bookmark actions control | Opens the bookmark actions bottom sheet | Editable only | `editor_web_bookmark_actions_<stable-block-id>` |
| Edit bookmark action | Navigates to the existing block in the full-page editor | Editable only | `web_bookmark_actions_edit` |
| Delete bookmark action | Starts confirmed deletion from the actions bottom sheet | Editable only | `web_bookmark_actions_delete` |
| Delete confirmation dialog | Prevents accidental data loss | Shown, dismissed, confirmed | `web_bookmark_delete_confirmation` |
| Browser/metadata feedback | Communicates recoverable failure | Error/snackbar | `web_bookmark_error_feedback` |

Dynamic tag suffixes use the immutable persisted block ID, never an index, timestamp, random value, or user-generated text. The stable prefix is the documented contract for tests and visual bounds.

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|------------------------|-----------------------|------------------------------|
| Bookmark card follows the preceding document block with the existing editor block spacing | `editor_web_bookmark_card_visual_<stable-block-id>`, preceding block visual tag | Card top-to-preceding-block bottom remains within the established 8dp vertical block rhythm. |
| Card’s visible border is inset from the document surface while its complete touch target remains accessible | `editor_web_bookmark_card_visual_<stable-block-id>`, `editor_web_bookmark_block_<stable-block-id>` | Visual card bounds are contained by the semantic/touch bounds; no visible edge is clipped. |
| Title and URL remain inside the card’s content column beside the globe icon and actions | `editor_web_bookmark_title_<stable-block-id>`, `editor_web_bookmark_url_<stable-block-id>`, card visual tag | Text bounds stay within card content bounds at default and large font scale; action targets retain 48dp minimum. |

### Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Loading | Existing note loading follows the current editor loading behavior; metadata loading is shown on the full page rather than as a partial card. | Wait or cancel the page; duplicate submission is disabled. |
| Empty | No bookmark card is shown in a note with no bookmarks; the Advanced panel still exposes the Web Bookmark tile for editable notes. | Add a bookmark or continue editing other blocks. |
| Content | White `surface` card on the `background` editor surface, 12dp corners, 1dp `border`, 2dp elevation, `primary` globe/open accent, `textPrimary` title, `textSecondary` context, and optional wrapped description. | Open, edit, or delete according to note access. Multiple cards remain ordered. |
| Error | Card remains present after metadata or browser failure; localized feedback identifies the recoverable issue. Invalid data is never opened. | Retry opening, edit the URL, or dismiss feedback. |

### Interaction Rules

- **Primary action**: Tap the card or Open action to validate and launch the URL through the external browser. No in-app WebView opens.
- **Secondary actions**: More/actions opens the bottom sheet. Edit navigates to the full-page editor. Delete opens confirmation. The card does not open when the actions control is tapped.
- **Validation**: The URL is validated before any metadata request and again immediately before external launch. HTTP and HTTPS are accepted; unsupported schemes, credentials, malformed values, and over-limit values are rejected.
- **Destructive actions**: Delete requires an M3 confirmation dialog. Confirm removes only this block and autosaves; cancel leaves the card unchanged.
- **Gestures**: Normal vertical document scrolling is supported. No swipe-only bookmark action is introduced. Scrim tap and system back dismiss the actions sheet according to the established M3 sheet behavior; full-page back returns to the editor.

### Copy Requirements

| Element | Copy |
|---------|------|
| Advanced tile label | `Web Bookmark` |
| Advanced tile description | `Save a web page with its title and description.` |
| Open action | `Open` |
| Actions control content description | `More actions for web bookmark` |
| Delete dialog title | `Delete bookmark?` |
| Delete dialog body | `This bookmark will be removed from the note.` |
| Delete confirmation | `Delete` |
| Browser error | `No browser is available to open this link.` |
| Metadata fallback | `Page details unavailable. The bookmark was saved with fallback details.` |

All copy is localized through `stringResource()`. Dynamic title, host, URL, and error values are bounded and passed as format arguments; raw page HTML is never displayed.

### Accessibility

- Merge the card’s descriptive content for TalkBack while retaining separately labeled Open and More/actions controls. The actions sheet exposes Edit and Delete.
- Provide localized content descriptions for the globe and every icon-only action; decorative icons use `null`.
- Expose read-only and loading/disabled state descriptions through semantics and do not communicate errors by color alone.
- Keep every action at least 48×48dp, preserve focus order as title → description → URL → Open → More/actions, and support keyboard/TalkBack activation.
- Support dynamic font scaling, wrapping, RTL mirroring, and contrast targets from the project design system. User-generated title/description text is treated as untrusted plain text.

### Responsive And Configuration Behavior

- Use 16dp phone and 24dp tablet horizontal content padding from the design system; card text wraps rather than clips.
- Keep the card within the existing vertical document scroll in portrait, landscape, narrow phones, and tablets.
- Use theme tokens from `LocalAppColors.current`: Light `background` `#F8F7FF`, `surface` `#FFFFFF`, `primary` `#7C6CF2`, `textPrimary` `#191627`, `textSecondary` `#7B7694`, `border` `#E7E3F6`, `divider` `#E7EBF0`, and `error` `#C44A4A`; dark values come from `DarkAppColors`.
- Preserve bookmark block data and the current Add/Edit URL draft through configuration changes via ViewModel/state restoration. Reopening the editor reloads the same JSON block.

### Design Assets

- **Source-fed baseline**: `design/baseline_editor_basic_blocks_top.png` — unchanged editor and Basic Blocks top state captured inside the active emulator test.
- **Source-fed entry baseline**: `design/baseline_editor_basic_blocks_scrolled.png` — unchanged Advanced-grid state captured inside the active emulator test.
- **Baseline-derived entry delta**: `design/mockup_editor_web_bookmark_entry.png` — Web Bookmark placed in the first visible Advanced row while the existing choices flow below.
- **Baseline-derived card delta**: `design/mockup_editor_web_bookmark_card_from_baseline_v2.png` — saved metadata-derived bookmark card with a single More/actions control in the unchanged editor canvas.

### Out Of Scope For This Design

- Remote favicon, thumbnail, or preview-image rendering.
- In-app web browsing or deep links into the new Add/Edit Navigation Compose destination.
- Automatic URL deduplication or bookmark-specific collections.

## Screen 2 — Add/Edit Web Bookmark Full Page

### Purpose

Collect or revise a bounded web address in a dedicated note-scoped destination. On Save, resolve the page title and description from safe, bounded metadata without asking the user to type either value.

### UX Principles

- Keep the destination-focused task calm and explicit: one required URL field, one clear save action, and no manual metadata fields.
- Use the existing app top-bar, typography, spacing, surface, and button tokens so the new destination feels like part of the current editor.
- Keep the page open while typing and during metadata work; preserve the URL draft until the user explicitly cancels or goes back.

### Entry And Exit

- **Entry points**: Web Bookmark tile navigates to Add mode; Edit from the bookmark actions bottom sheet navigates to Edit mode with the same block ID and current URL.
- **Primary success exit**: Save resolves metadata or deterministic fallback, persists the block, and returns to the editor at the prior note position.
- **Cancel/back behavior**: Top-bar back, Cancel, or system back returns to the editor and discards the transient URL draft. The actions sheet is not part of this page.
- **Failure exit or recovery**: Invalid URL keeps the page open with inline error. Metadata failure still saves with host/blank fallback. A persistence failure keeps the page open with retryable feedback and does not claim success.

### Information Architecture

1. **Top app bar**: Back navigation, `Add Web Bookmark` or `Edit Web Bookmark` title, and no destructive action.
2. **Destination guidance**: Short explanation that page title and description will be fetched automatically when possible.
3. **Required web-address field**: Outlined text field with visible `Web address` label and URL-specific validation.
4. **Metadata status**: Progress indicator and “Fetching page details…” status during eligible HTTPS submission; fallback status is shown when enrichment is unavailable.
5. **Bottom action area**: Primary Save bookmark button and secondary Cancel action are pinned to the bottom safe area of the full page. The action area moves above the IME when visible, but does not become a sheet or floating card.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| Full-page editor destination | Hosts Add/Edit flow | Add, Edit, loading, keyboard-visible | `web_bookmark_editor_page` |
| Page title | Identifies Add vs Edit mode | Add, Edit | `web_bookmark_editor_page_title` |
| Back action | Returns without mutation | Enabled, loading cancellation | `web_bookmark_back_button` |
| URL field | Required validated destination | Empty, valid, invalid, focused, disabled/loading | `web_bookmark_url_field` |
| Metadata guidance/status | Explains automatic metadata and fallback | Idle, loading, fallback | `web_bookmark_metadata_status` |
| Metadata progress | Shows bounded enrichment work | Hidden, visible | `web_bookmark_metadata_progress` |
| Inline validation/error | Explains invalid URL or persistence error | Hidden, visible | `web_bookmark_inline_error` |
| Bottom action area | Anchors actions to the page bottom safe area | Idle, loading, keyboard-visible | `web_bookmark_bottom_actions` |
| Save bookmark button | Submits valid draft | Disabled, enabled, loading | `web_bookmark_save_button` |
| Cancel action | Returns without mutation | Enabled, loading cancellation | `web_bookmark_cancel_button` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|------------------------|-----------------------|------------------------------|
| The page follows the existing app top-bar and uses the same content margins as the editor | `web_bookmark_editor_page`, `web_bookmark_editor_page_title`, page/window bounds | Title and page content remain inside the design-system horizontal margins on phone and tablet widths. |
| The actions remain aligned to the page bottom rather than inside a raised surface | `web_bookmark_bottom_actions`, `web_bookmark_save_button`, `web_bookmark_cancel_button`, page/window bounds | The action area is bottom-aligned with safe insets in the base state and remains above the IME in the keyboard state. |
| The URL field and Save action remain reachable while the IME is visible | `web_bookmark_url_field`, `web_bookmark_save_button`, IME/window bounds | URL field and Save button are fully visible and clickable above the IME in the keyboard state. |
| Inline error remains associated with the URL field without clipping the action row | `web_bookmark_inline_error`, `web_bookmark_url_field`, `web_bookmark_save_button` | Error is visible below the URL field; the page scrolls and Save remains reachable. |

### Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Loading | Save is disabled, a progress indicator and localized fetching status appear, and the URL field prevents duplicate submission. | Wait, cancel/back, or allow the fallback save path to complete. |
| Empty | Add mode shows guidance, a required Web address field, and a bottom-pinned disabled Save until a valid URL is entered. No title or description fields appear. | Focus the URL field, type, scroll, cancel, or go back. |
| Content | Edit mode prepopulates only the existing URL; Add mode shows a valid URL draft. The bottom-pinned Save uses `primary` and Cancel uses standard text-action styling. | Change the URL, submit, cancel, or go back. |
| Error | Invalid URL or persistence error is shown inline in `error` color with an icon and text; the page remains open. Metadata failure is a non-blocking fallback status. | Correct/retry the URL, resubmit, or cancel without changing the existing bookmark. |

### Interaction Rules

- **Primary action**: Save validates first. Valid HTTPS submissions enter Loading while bounded metadata retrieval runs; valid HTTP submissions skip retrieval and save with host/blank fallback.
- **Secondary actions**: Cancel/back discard the draft. Editing never mutates the existing block until Save succeeds. An unchanged Edit URL preserves the existing stored metadata; a changed URL resolves new metadata.
- **Validation**: URL must be absolute HTTP(S), host-bearing, credential-free, and within the configured bound. The page does not expose title or description inputs.
- **Metadata resolution**: Use `og:title`, `twitter:title`, HTML `<title>`, then host for title; use `og:description`, standard `description`, then blank for description. Sanitize and bound every value.
- **Gestures**: The page supports normal vertical scrolling. Field taps, IME actions, and system back do not discard the draft unexpectedly. The bottom action area uses `imePadding()` and stays above the keyboard without becoming a bottom sheet.

### Copy Requirements

| Element | Copy |
|---------|------|
| Add title | `Add Web Bookmark` |
| Edit title | `Edit Web Bookmark` |
| Guidance | `Page title and description will be fetched automatically when available.` |
| URL label | `Web address` |
| URL supporting text | `Paste a web address starting with http:// or https://.` |
| Save action | `Save bookmark` |
| Cancel action | `Cancel` |
| Back content description | `Back to note` |
| Metadata loading | `Fetching page details…` |
| Metadata fallback | `Page details unavailable. The bookmark was saved with fallback details.` |
| Invalid URL error | `Enter a valid HTTP or HTTPS URL.` |
| Persistence error | `Bookmark could not be saved. Try again.` |

All copy is localized through `stringResource()`. Do not put raw metadata or exception text directly in a Composable.

### Accessibility

- Use a visible label, not a placeholder alone, for the URL field; expose required semantics and inline error state.
- Provide a localized content description for the back action, stable test tags for every interactive control, and a predictable focus order from URL to Save/Cancel.
- Announce Loading and validation errors through semantics; keep Save disabled while invalid or loading.
- Maintain 48×48dp minimum targets, support TalkBack/keyboard navigation, font scaling, RTL, and high-contrast theme tokens.

### Responsive And Configuration Behavior

- Use 16dp phone/24dp tablet horizontal padding, the existing app top-bar treatment, 8–16dp field spacing, and a vertically scrollable page body.
- Use a bottom-aligned action Column/Spacer arrangement with safe bottom insets; apply `imePadding()` so the full page stays open above the keyboard and the action row never sits behind the IME.
- On narrow screens or large fonts, guidance and actions wrap/scroll rather than clip. RTL mirrors action/icon placement while URL text remains legible in a directionally appropriate field.
- ViewModel-owned URL draft and Add/Edit mode survive configuration changes; a successful save returns to the editor and a canceled draft is not persisted.

### Design Assets

- **Source-fed shell baseline**: `design/baseline_editor_basic_blocks_top.png` — unchanged editor shell captured inside the active emulator test.
- **Baseline-derived base page**: `design/mockup_add_web_bookmark_full_page_from_baseline_v2.png` — full-page Add mode with no raised form container and bottom-aligned actions.
- **Baseline-derived keyboard page**: `design/mockup_add_web_bookmark_full_page_keyboard_from_baseline_v2.png` — the full page stays open with the focused Web address field and bottom-aligned Save/Cancel actions above the IME.

### Out Of Scope For This Design

- Page body preview, remote image/favicon loading, and browser content inside the page.
- Backend metadata proxy or application API contract changes.
- Deep links into the Add/Edit destination.

## Screen 3 — Web Bookmark Actions Bottom Sheet

### Purpose

Keep bookmark cards compact while giving editable users a clear place to access Edit and Delete operations.

### UX Principles

- Use one More/actions control on the card instead of competing inline mutation icons.
- Reuse the existing `EditorNoteActionsSheet` and `SheetActionRow` treatment: plain surface, centered handle, simple header, divider, unboxed rows, and standard spacing.
- Make the sheet action-oriented and short: Edit and Delete are explicit rows with localized labels and adequate targets.
- Keep deletion safe: choosing Delete dismisses the actions sheet only after opening the existing confirmation dialog.

### Entry And Exit

- **Entry point**: Tap `More actions for web bookmark` on an editable bookmark card.
- **Edit exit**: Tap Edit to dismiss the sheet and navigate to the full-page editor in Edit mode for the same stable block ID.
- **Delete exit**: Tap Delete to dismiss the sheet and open the existing confirmation dialog; only confirmation removes the block.
- **Cancel/back behavior**: Scrim tap, swipe-down, or system back dismisses the sheet without mutation.
- **Read-only behavior**: The More/actions control and mutation sheet are absent in read-only notes; Open remains available.

### Information Architecture

1. **Sheet header**: `Bookmark options` with the standard centered drag handle.
2. **Edit row**: Unboxed `SheetActionRow` treatment with an edit icon; navigates to the full-page editor without changing the note.
3. **Delete row**: Unboxed `SheetActionRow` treatment with the destructive error icon/text; opens confirmation.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| Actions bottom sheet | Hosts bookmark mutations using the existing editor sheet family | Open, dismissing | `web_bookmark_actions_sheet` |
| Sheet title | Identifies the selected bookmark actions | Visible | `web_bookmark_actions_sheet_title` |
| Edit action row | Navigates to full-page Edit mode | Enabled, standard `SheetActionRow` | `web_bookmark_actions_edit` |
| Delete action row | Opens delete confirmation | Enabled, destructive `SheetActionRow` | `web_bookmark_actions_delete` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|------------------------|-----------------------|------------------------------|
| The sheet is visually anchored to the current editor with the standard M3 scrim, centered handle, and rounded top corners | `web_bookmark_actions_sheet`, window bounds | Sheet top corners and scrim remain visible; sheet stays within the window bounds. |
| Edit and Delete rows retain separate, accessible unboxed targets | `web_bookmark_actions_edit`, `web_bookmark_actions_delete` | Each row follows `SheetActionRow` spacing, is at least 48dp high, and exposes distinct semantics/test tags. |

### Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Open | Existing editor-style bottom sheet with a centered handle, simple title, divider, and plain Edit/Delete rows over the current editor. | Edit, Delete, swipe down, tap scrim, or press back. |
| Dismissing | The sheet closes without changing the note. | Return to the card or continue with the selected next destination. |
| Read-only | No actions control or mutation sheet is shown. | Open the bookmark only. |

### Interaction Rules

- More/actions opens the sheet and never opens the URL.
- The sheet uses the existing `EditorNoteActionsSheet` contract: `surface` container, 20dp top corners from the current implementation, 16dp horizontal padding, 12dp vertical spacing, header/divider, and `SheetActionRow` rows; no outlined row cards or chevrons.
- Edit dismisses the sheet before navigating to the full-page editor; the card remains unchanged until Save.
- Delete dismisses the sheet and opens confirmation; cancel leaves the card unchanged, while confirm removes only that block and autosaves.
- Scrim tap, swipe-down, and system back dismiss without mutation.

### Copy Requirements

| Element | Copy |
|---------|------|
| Sheet title | `Bookmark options` |
| Edit action | `Edit bookmark` |
| Edit content description | `Edit web bookmark` |
| Delete action | `Delete bookmark` |
| Delete content description | `Delete web bookmark` |

All copy is localized through `stringResource()` and destructive actions use the design-system error token plus text, not color alone.

### Accessibility

- Give the sheet a localized heading and expose Edit/Delete as separate button semantics with clear action names.
- Keep rows at least 48×48dp, preserve focus order as title → Edit → Delete → Close, and support TalkBack/keyboard activation.
- Announce the destructive action and confirmation transition; do not rely on scrim or color alone to communicate state.

### Responsive And Configuration Behavior

- Use the existing editor action-sheet width, `EditorNoteActionsSheet` top-corner shape, header/divider treatment, 16dp horizontal padding, and `SheetActionRow` spacing. The current implementation uses 20dp top corners; preserve that established value.
- Keep the sheet compact on narrow screens and tablets; labels wrap rather than clip at large font scales.
- Preserve the pending action-sheet visibility through transient configuration changes, but never persist a mutation until the user confirms the relevant action.

### Design Assets

- **Baseline-derived card delta**: `design/mockup_editor_web_bookmark_card_from_baseline_v2.png` — current editor card with a single More/actions control.
- **Baseline-derived actions sheet**: `design/mockup_editor_web_bookmark_actions_sheet_from_baseline_v2.png` — existing editor-style actions sheet over the current editor/card with plain Edit and Delete rows.

### Out Of Scope For This Design

- Swipe actions embedded in the card.
- Inline title/description editing or a second mutation menu.
