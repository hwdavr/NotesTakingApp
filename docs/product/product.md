# NotesTakingApp — Product Document

**Platform**: Android (Kotlin · Jetpack Compose · Material 3 · Room · Hilt)
**Minimum SDK**: Android 7.0 (API 24) / Target SDK 34
**Privacy**: 100% on-device note storage in local Room database with optional AI note summarization (Gemini Nano) and sharing capabilities.
**Last updated**: 2026-08-23

---

## Document Guide

| Section | Purpose |
|---|---|
| [Product Vision](#product-vision) | The product promise and primary user outcomes. |
| [Harness Feature Tracker](#harness-feature-tracker) | **Lifecycle source of truth** for every stable complex-feature workspace. |
| [Current Product Capabilities](#current-product-capabilities) | Detailed inventory of behavior already shipped. |
| [Roadmap — Planned Features](#roadmap--planned-features) | Product requirements and ideas that are not fully delivered. |
| [Product Portfolio Summary](#product-portfolio-summary) | Compact status view across the complete product surface. |
| [Technical Architecture](#technical-architecture) | Current platform and implementation boundaries. |

---

## Product Vision

NotesTakingApp is a production-grade Android notes taking application that provides users with fast, organized, and secure note creation and document management. Users can capture rich text notes, organize items into folders, summarize notes using on-device AI models (Gemini Nano), share notes securely, and export notes into standard formats.

The product is designed around three core user moments:

1. **Capture & Edit** — create, format, and organize notes effortlessly.
2. **Organize & Categorize** — organize notes with smart folders, tags, and AI categorization.
3. **Summarize & Share** — summarize lengthy notes with AI and collaborate securely.

---

## Harness Feature Tracker

This table is the single lifecycle source of truth for complex harness features. Agents must run `bash scripts/check-feature-lifecycle.sh` before selecting work and after every tracker transition.

Lifecycle rules:

- Every complex feature uses one stable dated workspace under `docs/product/` for its entire lifecycle.
- Use only: `Planning`, `Awaiting specification approval`, `Awaiting implementation approval`, `In Progress`, `Blocked`, `To be reviewed`, `To be fixed`, `To be human reviewed`, or `Complete`.
- `To be fixed` is set by the Evaluator when the overall score is below 5.0/5; the Generator then resolves every `code_review_{feature_id}.md` / `test_review_{feature_id}.md` finding before transitioning to `To be human reviewed`.
- The Markdown link label and target must both resolve to that exact product workspace.
- `Complete` is valid only when every slice in the workspace's `feature_list.json` is `passing` and final gates have passed.
- At most one feature may be `In Progress`.

<!-- HARNESS_TRACKER_START -->
| ID | Feature | Workspace | Status | Updated | Notes |
|---|---|---|---|---|---|
| basic-blocks-sheet | Basic Blocks Panel | [docs/product/2026-08-16-basic-blocks-sheet/](2026-08-16-basic-blocks-sheet/) | To be human reviewed | 2026-08-16 | Evaluator Verdict: Accept (5.0/5). US-1/US-2/US-3/US-4 passing. US-4 (Amendment v1 / Q12 — auto-collapse on outside interaction) delivered and verified. Code and test reviews pass cleanly. |
| note-emoji | Note Emoji | [docs/product/2026-08-15-note-emoji/](2026-08-15-note-emoji/) | To be human reviewed | 2026-08-15 | Fix pass plus UI revisions applied: two-fifths-height picker when the IME is hidden, full available height above the keyboard while search is focused, no `Emoji` title or header cross button, search-clear retained, and three additional emojis per category. v2/keyboard mockups and runtime screenshots are recorded; 4/4 code findings and 15/15 originally revision-required or missing test-evidence rows remain fixed, with full JVM, 95/95 connected, 83.4701% coverage, quality, platform, lifecycle, and visual gates passing. |
| voice-notes-audio-transcripts | Voice Notes & Audio Transcripts | [docs/product/2026-08-14-voice-notes/](2026-08-14-voice-notes/) | To be human reviewed | 2026-08-15 | Fix pass `f0d28d0` plus v4 transcription-finalization and v5 acknowledged-note durability fixes applied; stale or temporarily absent item lists no longer erase a saved voice document before editor reload. Test-review residuals and unavailable API runtimes remain documented for human review. |
| table-handles | Table Column & Row Handles | [docs/product/2026-08-16-table-handles/](2026-08-16-table-handles/) | To be human reviewed | 2026-08-16 | Fix pass applied; approved v2-based UI polish anchors the column, row, and Table options visuals to the grid borders and shortens only the Table options height. 9/9 findings fixed; JVM, 116/116 connected UI, visual, quality, platform, coverage (84.027%), and lifecycle gates pass. |
| mermaid-chart-preview | Mermaid Chart & Preview | [docs/product/2026-08-18-mermaid-chart-preview/](2026-08-18-mermaid-chart-preview/) | To be human reviewed | 2026-08-18 | Fix pass applied; re-verification evidence attached; 1/1 findings fixed. |
| code-block | Code Block | [docs/product/2026-08-18-code-block/](2026-08-18-code-block/) | To be human reviewed | 2026-08-20 | Fix pass applied; re-verification evidence attached; 4/4 findings fixed. Strengthened PDF assertion (PdfRenderer back-render), corrected stale platform matrix, fixed stale test comment, added large-snippet/long-line tests + documented clipboard/orientation non-goals. JVM suite, koverLog 82.68%, ktlint/detekt/lint, and 11/11 instrumented tests pass. |
| chart-block | Table to Chart Block | [docs/product/2026-08-20-chart-block/](2026-08-20-chart-block/) | In Progress | 2026-08-23 | US-1, US-2, and US-3 passing: ChartBlock persistence, insertion/conversion, local Bar/Line/Pie rendering, editable data table, protected row/column operations, selected-column options, datum callouts, and read-only inspection semantics are evidenced. US-4 export/platform/visual verification remains. |
<!-- HARNESS_TRACKER_END -->

---

## Current Product Capabilities

### ✅ Note Creation & Editing

| Feature | Status | Notes |
|---|---|---|
| Note Editor Screen | ✅ Done | Rich text editing surface with title, content body, and action sheets. |
| Note Operations | ✅ Done | Create, update, view, delete, favorite, and pin/unpin notes. |
| Note Actions Sheet | ✅ Done | Bottom sheet for quick actions: move to folder, export, delete, favorite. |
| Document Block Structure | ✅ Done | Modular note document model for structured content blocks. |
| Basic document block compatibility (US-1) | ✅ Slice done | Stable paragraph, H1–H4, bulleted, numbered, to-do, Toggle, Callout, and Quote mappings preserve legacy/unknown readable content, Toggle state, auto-save/reload, and Markdown/PDF treatment. |
| Basic blocks catalog insertion (US-2) | ✅ Slice done | Inline attached 2-column Basic blocks panel under unchanged 56 dp toolbar with 11 block actions (excluding Page), focus-aware insertion after focused block and append when no focus, empty defaults, auto-save, and single-tap collapse. |
| Basic blocks compact & accessible experience (US-3) | ✅ Slice done | Capped panel height min(280 dp, 40% usable height), 48 dp baseline tile targets, vertical scrolling through Quote, inner BackHandler dismissal, read-only trigger visibility/disabled state, light/dark theme support, accessibility semantics, and verified top/scrolled visual anchor proof. |
| Basic blocks auto-collapse on outside interaction (US-4) | ✅ Slice done | Open Basic blocks panel collapses on outside tap (editor content or non-trigger toolbar controls) without block insertion, focus change, or document mutation; trigger toggle and tile insertion contracts preserved. |
| Table structure operations (US-1) | ✅ Slice done | Backward-compatible `fitToWidth` JSON, row/column insert/clear/delete, final-row/column block removal, deep-copy duplicate, table delete, read-only guards, and existing auto-save persistence. |
| Chart block foundation (US-1) | ✅ Slice done | Backward-compatible ChartBlock JSON with stable IDs and selected-column fallback, Bar/Line/Pie insertion and focused-table conversion, local bitmap rendering, chart card Chart/Data/Options shell, title/data persistence callbacks, localized accessibility semantics, and acceptance coverage. Remaining export, platform-boundary, and final visual verification are tracked in US-4. |
| Chart block data editing (US-2) | ✅ Slice done | Editable ChartBlock-owned table with stable column selection, localized two-level Options flow, Add row/Add column and row/column operation sheets, protected category/last-data-column invariants, invalid-value filtering, auto-save/reload, and JVM plus connected acceptance coverage. |
| Chart block interaction and read-only inspection (US-3) | ✅ Slice done | Bar/Line/Pie datum targets expose selected visual state and localized dismissible callouts; empty/render-error states retain recovery guidance; Chart/Data/Options inspection remains available in read-only notes while mutations and destructive actions are disabled, with dark-theme, large-text, RTL, JVM, and connected acceptance coverage. |
| Focused table handles and option sheets (US-2) | ✅ Slice done | Editable table focus reveals localized column, row, and table-options handles; accessible Material 3 sheets retain the focused target, keep Delete last, and hide safely for read-only or outside focus. |
| Complete table-handle editing flow (US-3) | ✅ Slice done | Production table actions update and dismiss sheets immediately, preserve targets across focus changes, isolate multiple tables, persist after reload, and include approved focused/editor, column-sheet, row-sheet, and table-sheet runtime captures. |
| Emoji insertion foundation (US-1) | ✅ Slice done | Existing editor control opens a localized picker for editable notes, inserts Unicode at the cursor/range or a new paragraph, and stays disabled for read-only notes. |
| Emoji discovery catalog (US-2) | ✅ Slice done | Expanded app-bundled Unicode catalog covers nine approved categories with three additional localized entries per category, localized name/keyword search, clearable empty states, and exact default plus five skin-tone variants; selection reuses the editor insertion path and keeps the compact one-third-height sheet open. |
| Emoji Recent persistence and runtime validation (US-3) | ✅ Slice done | Exact inserted Unicode, including skin-tone variants, is stored as a bounded local MRU and restored after repository/app recreation with an empty fallback on read failure; the shipped picker has real Android glyph evidence and approved content, read-only, and empty-search visual captures. |
| Mermaid block model foundation (US-1) | ✅ Slice done | Added EditorBlock.MermaidBlock model with type: 'mermaid' JSON persistence, 'Mermaid Diagram' tile addition to BasicBlocksPanel, auto-save integration in NoteEditorViewModel, and Markdown/PDF export support. |
| Mermaid local rendering engine (US-2) | ✅ Slice done | On-device MermaidRenderer backed by local assets, theme token synchronization (Light/Dark AppColors), SVG string generation, and non-crashing structured error handling for invalid syntax. |
| Mermaid diagram card & mode toggle (US-3) | ✅ Slice done | Elevated Material 3 card container (#FFFFFF surface, #E7E3F6 border, 12dp rounded corners), diagram title editing, 'Edit Code' / 'View Chart' mode toggle, quick template chips (Flowchart, Sequence, Class, State), monospace code editor, syntax validation status badge, inline pinch-to-zoom/pan viewport, and read-only mode protection. |
| Mermaid fullscreen viewer & visual verification (US-4) | ✅ Slice done | FullscreenDiagramViewerDialog with edge-to-edge canvas, zoom controls (+, -, 100%, Fit to Screen), code copy to clipboard, SVG export/sharing, connected UI test suite (TC-US-4-01..03), 3 state-verifying screenshots, and reference anchor proof. |
| Code block model, persistence & panel insertion (US-1) | ✅ Slice done | `EditorBlock.CodeBlock(id, language, code)` with backward-compatible `type: "code"` JSON serialization, `BasicBlockType.CODE` mapping, `BasicBlocksPanel` Basic/Advanced section reorganization with Code tile under Advanced, `insertBasicBlock(BasicBlockType.CODE)` focus-aware insertion with auto-save, Markdown fenced code block export (```<language>\n<code>\n```), and PDF monospace box export path. TC-US-1-01..04 PASS. |
| Code block card, syntax highlighting & actions (US-2) | ✅ Slice done | Elevated Material 3 card with a 14-language selector dropdown, synchronized line-number gutter, real-time regex-based `CodeSyntaxHighlighter`, one-tap clipboard copy with checkmark feedback, delete action with auto-save, and read-only highlighted rendering. TC-US-2-01..07 PASS. |
| Code block read-only flows & visual verification (US-3) | ✅ Slice done | Connected editor coverage verifies editable interaction, Advanced panel insertion, and read-only copy/disabled controls; in-test screenshots and concrete bounds evidence are recorded for the Code Block card and Advanced Basic Blocks panel. TC-US-3-01..03 and TC-US-3-VIS-01..02 PASS. |

### ✅ Folder & Collection Management

| Feature | Status | Notes |
|---|---|---|
| Folders & Collections | ✅ Done | Create, list, rename, and delete folders and collections. |
| Move Operations | ✅ Done | Move notes between folders and move folders into sub-folders. |
| Favorites Collection | ✅ Done | Quick-access collection view for favorite notes and folders. |
| Smart Folder Categorization | ✅ Done | AI-assisted folder categorization using Gemini Nano and MediaPipe text embeddings. |

### ✅ AI Note Summarization

| Feature | Status | Notes |
|---|---|---|
| Gemini Nano Summarizer | ✅ Done | On-device AI note summarization using Gemini Nano AICore. |
| Downloading & Status Handling | ✅ Done | Handles model download states, progress updates, and fallback mechanisms. |
| Summary Card Component | ✅ Done | Renders generated summaries directly within the note editor interface. |

### ✅ Voice Notes & Audio Transcripts

| Feature | Status | Notes |
|---|---|---|
| Safe recording session (US-1) | ✅ Done | Private AAC/OPUS file lifecycle, 128 MB preflight, foreground service notification controls, permission recovery, session guard, reducer/ViewModel state bridge, deterministic cleanup, and fix-pass I/O/backup hardening. |
| Progressive transcription with safe fallback (US-2) | ✅ Done | Injectable Android recognizer path, single PCM capture/encoder ownership, API-33+ on-device source-fed transcription intent with quality formatting request, overlapping partial/final preview, stop-time partial finalization, timeout-without-marker fallback, model-unavailable audio-only fallback, and cancellation cleanup. OEM/runtime matrix evidence remains documented for review. |
| Home/editor recording entry points (US-3) | ✅ Done | Home Create sheet allocates a placeholder before recording; editor Mic opens the recorder with note/focus context; context switches cleanly replace active Home sessions. |
| Inline editor persistence and playback (US-4) | ✅ Done | Room-backed VoiceNote metadata, focused/Home document insertion, acknowledgement-first document persistence with stale/missing-list retention, editable transcript TextBlocks, save/reload regression for non-null audio paths, Media3 playback/seek, audio-only deletion, and private-file cascade cleanup. |
| Voice Notes settings and final visual verification (US-5) | ✅ Done | DataStore-backed AAC/OPUS selection, Room-derived private storage totals, recorder format wiring, and fix-pass Light Theme evidence across Home, Recorder, Editor, and Settings. |

### ✅ Note Sharing & Access Control

| Feature | Status | Notes |
|---|---|---|
| Invite & Share Screen | ✅ Done | Share notes with other users via invite link or email. |
| Manage Access Screen | ✅ Done | Manage permissions (view-only vs. edit access) for shared users. |
| Shared Users List | ✅ Done | View all users with access to a specific note or collection. |

### ✅ Export & Formatting

| Feature | Status | Notes |
|---|---|---|
| Export Formats | ✅ Done | Export notes to plain text, Markdown (.md), and PDF formats under `Documents/NotesTakingApp/`. |
| Export Screen | ✅ Done | Dedicated export destination screen with preview and share target options. |

### ✅ Security & Onboarding

| Feature | Status | Notes |
|---|---|---|
| Onboarding Flow | ✅ Done | Welcome and feature introduction screens for new users. |
| Password Validation | ✅ Done | Password strength validation rules and UI indicators. |
| Note Protection | ✅ Done | PIN and password lock for sensitive or protected notes. |

### ✅ Navigation & Design System

| Feature | Status | Notes |
|---|---|---|
| App Navigation | ✅ Done | Jetpack Compose Navigation Graph with Bottom Navigation Bar (Home, Notes, Folders, Settings). |
| Design System | ✅ Done | Centralized color tokens (`AppColors.kt`), Material 3 theme (`Theme.kt`), and typography (`Type.kt`). |

---

## Roadmap — Planned Features

### Group 1 — Rich Content & Attachments
- **Image & File Attachments**: Attach photos, diagrams, and PDF files to notes with inline preview.
- **Checklists & Task Lists**: Interactive checkable list items within note documents.
- **Basic Blocks Panel**: ✅ Implemented (awaiting evaluation review) — document block compatibility, inline catalog insertion, compact scrollable panel geometry, accessibility, and visual reference proof delivered.
- **Chart Blocks**: 🚧 US-1 through US-3 implemented — chart block persistence, insertion/conversion, local Bar/Line/Pie rendering, editable data tables, protected row/column operations, selected-column options, datum callouts, empty/error recovery, and read-only inspection are delivered; export, platform-boundary, and final visual verification remain in US-4.

### Group 2 — Search & Organization
- **Offline Full-Text Search**: Fast FTS5 Room database search with highlight matching across titles and note bodies.
- **Tagging & Labeling System**: Custom color-coded tags and multi-tag filtering across all notes.
- **Auto-Archive & Trash Recovery**: Soft delete trash bin with 30-day auto-purge and restore options.

### Group 3 — Synchronization & Cloud Backup
- **Encrypted Cloud Sync**: End-to-end encrypted backup and multi-device sync backend.
- **Version History**: Review past revisions of notes and restore to previous snapshots.

---

## Product Portfolio Summary

| Area | Status | Key Capabilities |
|---|---|---|
| **Note Editor** | 🟢 Complete (To be reviewed) | Text editing, complete basic-block panel & persistence, document blocks, summary cards, action sheets, emoji path, table path, ChartBlock US-1 foundation, US-2 data editing, and US-3 interaction/read-only inspection, Mermaid diagram preview & fullscreen interactive viewer, and code block card with syntax highlighting, line numbers, language selection, copy & delete, read-only behavior, and visual evidence |
| **Folders & Categories** | ✅ Shipped | Tree structure, move operations, smart AI categorization |
| **AI Summarizer** | ✅ Shipped | Gemini Nano on-device summarization, status handling |
| **Sharing & Security** | ✅ Shipped | Invite sharing, manage permissions, password validation |
| **Export & Export UI** | ✅ Shipped | Text, Markdown, PDF export screens |
| **App Shell & Theme** | ✅ Shipped | M3 theme, bottom bar navigation, light/dark mode |
| **Voice Notes** | ✅ Shipped | Private recording, PCM source-fed progressive transcription/fallback with API-33 formatting request and stop-time partial finalization, centered recorder waveform, Home/editor entry points, acknowledgement-first Room persistence with stale/missing-list protection, inline VoiceNote playback, editable transcripts, local cleanup, AAC/OPUS settings, storage totals, and fix-pass Light Theme evidence; residual production-route/API runtime gaps are documented |

---

## Technical Architecture

- **Architecture**: Android Clean Architecture with Unidirectional Data Flow (UDF).
  - `ui/`: Jetpack Compose screens, ViewModels (`StateFlow<UiState>`), and reusable M3 components.
  - `domain/`: Business models, use cases (`SummarizeNoteUseCase`, `CategorizeNoteUseCase`), repository contracts.
  - `data/`: Room Database (`NoteDao`, `FolderDao`), Remote API interfaces, MediaPipe / Gemini Nano AI clients, Data Mappers.
- **Tech Stack**: Kotlin 1.9+ · Jetpack Compose · Material 3 · Navigation Compose · Room · Hilt · KSP · Java 17 · minSdk 24 / targetSdk 34.

*Document last updated: 2026-08-23*
