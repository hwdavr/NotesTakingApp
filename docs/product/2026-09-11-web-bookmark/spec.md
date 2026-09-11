# Feature Spec — Web Bookmark in Notes

**Date**: 2026-09-11  
**Status**: Approved — 2026-09-11  
**Related design**: `design.md`

## Objective

Add a durable Web Bookmark block to the existing note editor so users can capture a useful web reference without leaving the note. The bookmark stores the validated URL locally and derives its title and description from page metadata when available, while remaining useful offline when metadata cannot be fetched.

## User Goal

As a note author, I want to add a web page bookmark to my note so that I can recognize and reopen the reference later without losing its context.

## Scope

### In Scope

- A `Web Bookmark` tile in the existing Basic Blocks → Advanced panel.
- A full-page Add/Edit Web Bookmark destination with a required URL only; title and description are never manually entered.
- Strict URL validation for absolute `http://` and `https://` URLs with a host.
- Best-effort, unauthenticated HTTPS page-metadata retrieval for the bookmark title and description.
- A persisted bookmark block rendered as a themed card with metadata-derived title, description when available, host/URL, open, and a single actions control.
- A bookmark actions bottom sheet containing Edit and Delete operations; deletion still requires confirmation.
- Multiple bookmark blocks in one note, inserted after the focused block or appended when no block is focused.
- Read-only behavior: bookmark cards can be opened, while add, edit, and delete mutations are unavailable.
- Local autosave, reload persistence, Markdown/plain-text/PDF export representations, and readable compatibility fallback behavior.
- Recoverable feedback for invalid input, metadata-fetch failure, and unavailable browser handlers.

### Out Of Scope

- Scraping or rendering page body content.
- Downloading or persisting remote preview images, favicons, thumbnails, or page assets.
- A bookmark-specific Room table or backend/OpenAPI endpoint; bookmark data remains inside the existing note-document JSON content.
- In-app WebView browsing, deep links, bookmark collections, deduplication, tags, reminders, or visit history.
- Fetching metadata for `http://` URLs because the application keeps cleartext traffic disabled. HTTP bookmarks are still accepted, stored, exported, and offered to the device browser.

## Rule Applicability

Complete every row before approval. The external page and browser interactions are client-side platform boundaries, not application API-contract changes.

| Rule ID | Rule document | Decision | Feature-specific evidence or reason |
|---|---|---|---|
| ARCH | `android-architecture.md` | Required | Keep URL validation and metadata behavior behind domain contracts/use cases; keep OkHttp parsing and Android intent launchers in data/platform adapters; keep Compose rendering on UiState and callbacks; prove with architecture checks and review. |
| IMPL | `implementation-rules.md` | Required | Add real validation, metadata fallback, persistence, editing, deletion, export, browser-launch, and error paths with no stubs or no-op callbacks; prove with acceptance tests and source-rule checks. |
| TEST | `testing-strategy.md` | Required | Cover pure validation/parsing/export behavior with JVM tests, ViewModel/repository flow with integration tests, and the Android URI/intent plus production editor journey with instrumented tests. |
| SUI | `compose-rules.md` | Required | Add Basic Blocks tile, bookmark card, full-page editor, actions bottom sheet, loading/error/disabled states, test tags, theme tokens, 48dp targets, and keyboard-visible behavior; prove with design artifacts and Compose/runtime tests. |
| L10N | `localization-rules.md` | Required | Add localized labels, validation/error/status copy, accessibility descriptions, and dynamic host/metadata formatting through string resources; prove with localization checks and UI assertions. |
| NAV | `navigation-rules.md` | Required | Add/Edit is a note-scoped full-page destination with explicit entry, back-stack, cancel, and return behavior; verify the production editor journey and state restoration. External browser opening remains a separate platform boundary. |
| API | `api-contract-rules.md` | Not applicable — no application API endpoint, DTO, schema, or OpenAPI change; page metadata is a best-effort direct HTTPS fetch of a user-selected URL. | Use `N/A — no API` for acceptance-test contract references; verify `sharedContracts/openapi.yaml` is unchanged. |
| OBS | `observability.md` | Required | Metadata and browser operations have asynchronous failure/recovery boundaries; record structured outcome/error logs without URLs, note content, PII, credentials, or response bodies, and verify failure states. |
| ANL | `analytics-rules.md` | Not applicable — analytics: none | No product-approved screen, funnel, action, or error events are required for this local utility feature. |
| SEC | `android-security.md` | Required | Treat URL, page HTML, redirects, and intent resolution as untrusted; allow-list schemes/hosts, reject credentials and malformed/oversized input, use a dedicated unauthenticated HTTPS client, avoid WebView/HTML sinks, and verify a real Android external-intent boundary. |

## Technical Spec

### Libraries & Dependencies

| Library / SDK | Version | Purpose |
|---------------|---------|---------|
| OkHttp | `4.12.0` (existing) | Dedicated unauthenticated HTTPS request for bounded page metadata retrieval; do not reuse the authenticated application client. |
| Jetpack Compose Material 3 | Existing project BOM | Basic Blocks tile, full-page editor destination, actions bottom sheet, form field, card, loading/error states, and themed actions. |
| Android platform `Intent` / `PackageManager` | Project API 24–34 | Open a validated URL in an external browser and detect the no-handler failure path. |

### Key Technical Decisions

- **Document representation**: Extend the existing editor block model with `EditorBlock.WebBookmarkBlock(id, url, title, description)` and persist it as a version-compatible `type: "web_bookmark"` JSON block. Title and description are resolved metadata/fallback values, not editable form fields. No Room migration is required because note content is already serialized as JSON.
- **Metadata resolution**: The full-page form accepts only a URL. On Save, metadata is fetched for eligible HTTPS URLs; the title extraction order is `og:title`, `twitter:title`, HTML `<title>`, then the URL host, and the description extraction order is `og:description`, standard `description`, then blank. There are no manual title or description values to take precedence over metadata.
- **Metadata request boundary**: Fetch only after the user submits a valid URL, with a short bounded timeout, a bounded response body, an HTML content-type check, 2xx status handling, and no script, image, WebView, or JavaScript execution. HTTPS redirects remain eligible only when the final URL is HTTP(S); HTTP input is saved without an in-app metadata request.
- **Authenticated-client isolation**: The metadata client must not contain the app’s `AuthInterceptor`, `TokenAuthenticator`, or body/header logging that could expose credentials or user content to an arbitrary host.
- **Form state**: Add a nullable `WebBookmarkEditorUiState` to the existing editor state. It owns Add/Edit mode, stable target/insertion IDs, URL draft, validation error, metadata status, and submission error. Full-page navigation and the bookmark actions sheet are presentation concerns driven by ViewModel state/callbacks, not Composable-local business rules.
- **External browser launch**: Validate again at the platform boundary, create an `ACTION_VIEW` intent for the validated URI, resolve a handler before launch, and report a localized recoverable error when no handler exists. Do not add an exported component or URI grant.
- **Compatibility fallback**: Include a bounded readable fallback field in the serialized bookmark block so an older document reader can preserve the URL and title as text rather than silently dropping the block. The current reader still recognizes the structured block and does not rewrite it unless the note is edited.
- **Export representation**: Markdown emits a normal titled Markdown link and optional description; plain text and PDF emit title, description, and URL as readable text. Export does not make network requests or download remote assets.

### External APIs / Services

- **User-selected web page** — optional unauthenticated HTTPS metadata source. No authentication headers, application tokens, private note content, or page-body storage are sent beyond the URL request itself. The request is best effort and never prevents saving a valid bookmark.

### Platform & Compatibility Constraints

- **Min SDK**: Project default, API 24.
- **Permissions required**: No new permission. Existing `android.permission.INTERNET` is used for optional HTTPS metadata retrieval.
- **Other constraints**: `android:usesCleartextTraffic="false"` remains unchanged; no WebView is introduced; browser availability varies by device and must be represented in the platform capability matrix and fail loudly if the required runtime boundary is unavailable.

## Functional Requirements

- **FR-001**: In an editable note, the existing Basic Blocks → Advanced panel MUST expose a `Web Bookmark` tile; selecting it MUST navigate to the Add Web Bookmark full page without mutating the document.
- **FR-002**: The Add/Edit Web Bookmark full page MUST require a bounded URL and MUST accept only absolute `http` or `https` URLs with a valid host; it MUST not expose title or description input fields.
- **FR-003**: Invalid, blank, oversized, credential-bearing, or unsupported URLs MUST show a localized inline validation error, MUST not make a metadata request, and MUST not persist a block.
- **FR-004**: For a valid HTTPS URL, submission MUST show a loading state and make a bounded best-effort metadata request; for HTTP URLs or any request failure, the operation MUST save with the URL host as the title fallback and a blank description without blocking bookmark creation.
- **FR-005**: Submitting a valid bookmark MUST insert one stable-ID Web Bookmark block after the captured focused block, or append it when no block is focused, and MUST autosave it without changing unrelated blocks.
- **FR-006**: The bookmark card MUST render the saved title or host fallback, optional description, and URL/host, and tapping the card/open action MUST launch the validated URL in the external browser.
- **FR-007**: Choosing Edit from the bookmark actions sheet MUST navigate to the same full-page editor for the same block, preserve the current URL, refresh metadata when the URL changes, and update the same block in place after success; cancel/back MUST leave the document unchanged.
- **FR-008**: Deleting a bookmark MUST require confirmation, remove only the selected block, maintain a valid editable document when it is the last block, and autosave the result.
- **FR-009**: Read-only notes MUST expose no bookmark mutation controls and MUST continue to allow opening a valid bookmark URL; failed browser resolution MUST leave the note unchanged and show recoverable feedback.
- **FR-010**: Plain-text, Markdown, and PDF exports MUST retain each bookmark’s title/context and URL without fetching remote content during export.
- **FR-011**: Existing note JSON without bookmarks MUST round-trip unchanged in meaning; malformed or unknown bookmark data MUST degrade to readable text or a deterministic safe fallback without crashing or silently exposing an unsafe URI.
- **FR-012**: The full-page editor, actions sheet, and card MUST preserve the specified state and accessibility behavior across configuration changes, keyboard visibility, font scaling, RTL, dark theme, narrow screens, and tablet layouts.

## Acceptance Criteria

- **AC-001**: Given an editable note, when the user opens Basic Blocks → Advanced, then a localized `Web Bookmark` tile with a stable test tag and at least a 48dp target is visible.
- **AC-002**: Given the Advanced panel is open, when the user selects `Web Bookmark`, then the Add Web Bookmark full page opens and the document remains unchanged.
- **AC-003**: Given the Add/Edit full page, when it is displayed, then only the required Web address field is present, title and description input fields are absent, and the Save/Cancel actions are aligned to the bottom safe area with back navigation reachable.
- **AC-004**: Given a blank, malformed, unsupported-scheme, credential-bearing, or oversized URL, when the user attempts submission, then an inline localized validation error remains visible, no metadata fetch occurs, and no bookmark is inserted or changed.
- **AC-005**: Given a valid absolute HTTP(S) URL, when the user enters it, then validation passes and submission becomes available.
- **AC-006**: Given a valid HTTPS URL and an HTML page returning bounded metadata, when the user submits, then the full page shows progress, resolves title/description from the metadata, and the bookmark is saved.
- **AC-007**: Given a valid URL whose metadata is partial or missing, when the user submits, then the title falls back to the URL host and the description falls back to blank for missing values; no manual title or description is requested.
- **AC-008**: Given an HTTP URL, a timeout, an offline device, a non-HTML response, a non-2xx response, an oversized response, or malformed metadata, when the user submits, then the valid bookmark still saves with deterministic host/blank fallbacks.
- **AC-009**: Given a focused block, when the user successfully submits a bookmark, then exactly one bookmark block appears immediately after that block and the full page returns to the editor.
- **AC-010**: Given no focused block, when the user successfully submits a bookmark, then the bookmark is appended and multiple bookmark blocks can coexist in the same note.
- **AC-011**: Given a newly saved bookmark, when the note autosaves, is closed, and is reopened, then the URL, title, description, block order, and stable block ID round-trip from the existing note content store.
- **AC-012**: Given a rendered bookmark card, when the user views it, then the card exposes title/host/URL context, themed surfaces, localized semantics, and stable test tags for the card and actions.
- **AC-013**: Given a valid bookmark URL and an available browser handler, when the user taps the card/open action, then Android receives an `ACTION_VIEW` intent for that validated URL and the note remains intact.
- **AC-014**: Given a valid bookmark URL and no browser handler, when the user taps the card/open action, then no crash occurs, a localized recoverable error is shown, and the bookmark remains present.
- **AC-015**: Given an existing bookmark, when the user opens the card actions, chooses Edit, and submits an unchanged URL, then the same block ID and position remain and its stored metadata persists.
- **AC-016**: Given an existing bookmark, when the user changes the URL and submits successfully, then metadata refresh is attempted according to the HTTPS rules and the same block is updated in place.
- **AC-017**: Given an existing bookmark, when the user cancels or backs out of the edit page, then no URL or metadata changes are persisted.
- **AC-018**: Given an editable note with a bookmark, when the user opens the card actions, then a bottom sheet exposes Edit and Delete; choosing Delete opens confirmation, and only confirmation removes the bookmark and autosaves.
- **AC-019**: Given a note containing only one bookmark block, when the user confirms deletion, then the editor retains a valid empty editable text block rather than an invalid empty document.
- **AC-020**: Given a read-only note with a bookmark, when the user views it, then add/edit/delete controls are unavailable while opening remains available; attempted mutation callbacks do not change state.
- **AC-021**: Given a note containing bookmarks, when the user exports plain text, Markdown, and PDF, then each output contains the bookmark title/context and URL and export performs no network fetch.
- **AC-022**: Given legacy note JSON without bookmark blocks, when it is loaded and saved, then all existing blocks retain their prior meaning; given malformed bookmark JSON, loading remains non-crashing and produces readable safe fallback content.
- **AC-023**: Given the full-page editor is open with the URL field focused, when the IME appears or the device recreates the Activity, then the page remains open, the URL field and actions remain reachable above the keyboard, and draft state is retained.
- **AC-024**: Given dark theme, RTL, large font scale, a narrow phone, or a tablet, when the editor/card/sheet renders, then text remains readable and controls retain their localized semantics and minimum touch targets without clipping.

## Data And Persistence

- Extend the existing serialized editor document with `EditorBlock.WebBookmarkBlock` fields: stable `id`, validated `url`, resolved `title`, and resolved `description`.
- Use the existing note repository and JSON content field; no Room schema or DAO migration is required.
- Store only bounded URL, title, and description text values. Do not store HTML, response headers, cookies, remote images, favicon bytes, or fetch credentials.
- Save through the existing ViewModel autosave/back-navigation path. Add/Edit drafts remain transient until successful submission.
- Preserve stable block identity and order on edit; remove only the selected block on delete.
- Ensure markdown/plain-text/PDF export consumes the stored block and never performs a network request.

## Edge Cases

- Blank, whitespace-only, control-character, invalid, unsupported-scheme, credential-bearing, hostless, or over-limit URL: inline validation error and no request.
- HTTP URL: accepted for storage/open/export; metadata request skipped because cleartext traffic is disabled.
- HTTPS timeout, DNS/TLS failure, redirect to unsupported scheme, non-2xx response, non-HTML content, malformed HTML, or response over the configured byte limit: save with host/blank fallback.
- Metadata title/description contains markup, whitespace, or excessive length: decode as text, strip markup/control characters, bound length, and never render as HTML.
- Page metadata is partial: use the first valid bounded value for each field and apply host/blank fallback independently.
- Browser missing or `ActivityNotFoundException`: localized snackbar/error effect; card and note remain unchanged.
- User taps Delete while a metadata request is active: cancel the request or finish it safely without applying a stale draft; no partial block is persisted.
- Multiple bookmarks with identical URLs: allowed; no implicit deduplication.
- Bookmark is the final document block: deletion leaves the existing empty text-block invariant.
- App recreation or keyboard/IME transition during Add/Edit: retain URL draft and full-page mode through the state-saving path; cancel/back still discards the draft.
- Older or malformed document content: preserve readable URL/title fallback and avoid unsafe intent launch until the URL passes current validation.

## Explicit Assumptions

| # | Assumption | Risk if Wrong |
|---|---|---|
| A1 | The response `es` for item 6 means yes: use a bottom sheet for bookmark actions such as Edit/Delete. | If this was not intended, the card action surface needs revision before slicing. |
| A2 | “Page metadata” for v1 means title and description text only; remote images and favicons are excluded. | Adding visual previews would require new storage, image-loading, privacy, and platform decisions. |
| A3 | “Call the website metadata to get it” means fetch title/description on Save rather than on every URL keystroke; no manual title or description override is offered in v1. | Fetch-on-type would add network churn and intermediate UI states; a later requirement can add explicit refresh behavior. |
| A4 | HTTP links are valid bookmark data, but the app’s metadata request is HTTPS-only to honor the existing cleartext policy. | Users may expect HTTP metadata enrichment; that can be addressed in a later security-approved change without weakening the app-wide policy. |
| A5 | Existing note sharing/sync transports the note content JSON without a separate block schema, so no OpenAPI change is needed. | If a backend validates block types, the contract must be updated before implementation. |

## Open Questions

All questions are ✅ Answered before this document is approved.

| # | Question | Status | Answer |
|---|---|---|---|
| Q1 | Should bookmarks be standalone blocks with multiple bookmarks allowed? | ✅ Answered | Yes; insert via Basic Blocks → Advanced and allow multiple per note. |
| Q2 | Should the form contain a required URL plus optional title and description? | ✅ Answered | No; the full page contains only a required URL and derives title/description from page metadata. |
| Q3 | Should the app attempt page metadata enrichment when possible? | ✅ Answered | Yes; best effort, with deterministic fallback and no blocking save. |
| Q4 | Which URL schemes and open behavior are required? | ✅ Answered | Accept HTTP and HTTPS; validate and open via the device browser; report missing-handler errors. |
| Q5 | Should edit/delete and read-only behavior be supported? | ✅ Answered | Yes; editable notes can mutate, read-only notes can open only. |
| Q6 | Should exports retain bookmark context and URL? | ✅ Answered | Yes; preserve them in plain text, Markdown, and PDF. |
| Q7 | Should bookmark actions use a bottom sheet? | ✅ Answered | Yes; the card opens a bottom sheet with Edit and Delete operations. |

## Screen States

| State | Requirement | Acceptance Criteria |
|-------|-------------|---------------------|
| Loading | Existing note loads normally; bookmark submission shows progress and disables duplicate submission while metadata is requested. | AC-006, AC-023 |
| Empty | An empty note can navigate to the full-page editor; the page shows required URL guidance and no title/description fields. | AC-001–AC-005 |
| Content | Saved bookmark cards show title/host/URL and optional description; multiple cards retain order and actions obey editability. | AC-009–AC-013, AC-015–AC-021 |
| Error | URL validation stays inline; metadata/browser failures are recoverable and do not discard valid local bookmark data. | AC-004, AC-008, AC-014, AC-022 |

## Navigation

- **Entry**: Existing Note Editor → default bottom toolbar → Basic Blocks trigger → Advanced → Web Bookmark tile → note-scoped Add Web Bookmark full page. Card actions → Edit enters the same destination in Edit mode; card actions → Delete opens confirmation.
- **Back/cancel**: Full-page back, Cancel, or system back returns to the editor and discards only the transient URL draft. The card action sheet dismisses on scrim/back; while metadata is loading, cancellation cancels or ignores the in-flight result.
- **Success**: Valid submission returns from the full page and inserts/updates the card in the current note; autosave persists it.
- **Error recovery**: Validation keeps the full page open with inline error. Metadata failure saves with fallback. External browser failure shows recoverable feedback and leaves the card in place. The Add/Edit destination is a real in-app back-stack entry; no deep link is introduced.

## Traceability

| Requirement | Design Section | Acceptance Criteria |
|-------------|----------------|---------------------|
| FR-001–FR-005 | `design.md` Screen 1 and Screen 2; Entry and Exit; Interaction Rules | AC-001–AC-011 |
| FR-006, FR-009 | `design.md` Screen 1; Component Inventory; Visual States; Accessibility | AC-012–AC-014, AC-020 |
| FR-007–FR-008 | `design.md` Screen 1 and Screen 2; Interaction Rules | AC-015–AC-019 |
| FR-010–FR-011 | `design.md` Screen 1; Out Of Scope and export notes | AC-021–AC-022 |
| FR-012 | `design.md` Screen 1 and Screen 2; Responsive and Configuration Behavior | AC-023–AC-024 |

## Verification Expectations

- **Unit**: URL validator, URL normalization/bounds, metadata tag parser and sanitization, metadata candidate selection, host fallback, serialized block parsing, compatibility fallback, Markdown/plain-text rendering, and export no-network behavior.
- **Integration**: ViewModel + metadata data source + note repository flow using deterministic MockWebServer responses for success, timeout/error, non-HTML, oversized, and malformed metadata; verify add/edit/cancel/autosave/reload and read-only guards. No application API endpoint is affected; shared scenario reference is `N/A — no API`.
- **Instrumented UI**: Production editor entry to Advanced panel, full-page Add/Edit destination, IME-visible URL field, insertion after focused/no-focus, card actions bottom sheet, edit/delete/read-only behavior, configuration/return persistence, and real Android external browser intent resolution/failure boundary.
- **Manual/visual**: Compare the approved editor-card, full-page editor, actions-sheet, and keyboard-visible full-page mockups in Light Theme; verify dark theme, RTL, large text, narrow phone/tablet layout, state tags, anchor bounds, and no remote preview/image loading.

## No Open Questions Gate

- [x] All requirements are specific and testable.
- [x] All non-goals are explicit.
- [x] No unresolved assumptions remain; assumptions are listed for approval.
- [x] All visual states are defined in `design.md`.
- [x] All navigation outcomes are defined.
- [x] All ten Rule Applicability rows are complete.
