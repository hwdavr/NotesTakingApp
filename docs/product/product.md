# NotesTakingApp — Product Document

**Platform**: Android (Kotlin · Jetpack Compose · Material 3 · Room · Hilt)
**Minimum SDK**: Android 7.0 (API 24) / Target SDK 34
**Privacy**: 100% on-device note storage in local Room database with optional AI note summarization (Gemini Nano) and sharing capabilities.

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
| note-emoji | Note Emoji | [docs/product/2026-08-15-note-emoji/](2026-08-15-note-emoji/) | To be human reviewed | 2026-08-15 | Fix pass plus UI revisions applied: two-fifths-height picker when the IME is hidden, full available height above the keyboard while search is focused, no `Emoji` title or header cross button, search-clear retained, and three additional emojis per category. v2/keyboard mockups and runtime screenshots are recorded; 4/4 code findings and 15/15 originally revision-required or missing test-evidence rows remain fixed, with full JVM, 95/95 connected, 83.4701% coverage, quality, platform, lifecycle, and visual gates passing. |
| voice-notes-audio-transcripts | Voice Notes & Audio Transcripts | [docs/product/2026-08-14-voice-notes/](2026-08-14-voice-notes/) | To be human reviewed | 2026-08-15 | Fix pass `f0d28d0` plus v4 transcription-finalization and v5 acknowledged-note durability fixes applied; stale or temporarily absent item lists no longer erase a saved voice document before editor reload. Test-review residuals and unavailable API runtimes remain documented for human review. |
| table-handles | Table Column & Row Handles | [docs/product/2026-08-16-table-handles/](2026-08-16-table-handles/) | In Progress | 2026-08-16 | US-1 table operations and US-2 focused handle/sheet UI are passing; US-3 production action flow and visual verification remain not started. |
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
| Table structure operations (US-1) | ✅ Slice done | Backward-compatible `fitToWidth` JSON, row/column insert/clear/delete, final-row/column block removal, deep-copy duplicate, table delete, read-only guards, and existing auto-save persistence. |
| Focused table handles and option sheets (US-2) | ✅ Slice done | Editable table focus reveals localized column, row, and table-options handles; accessible Material 3 sheets retain the focused target, keep Delete last, and hide safely for read-only or outside focus. |
| Emoji insertion foundation (US-1) | ✅ Slice done | Existing editor control opens a localized picker for editable notes, inserts Unicode at the cursor/range or a new paragraph, and stays disabled for read-only notes. |
| Emoji discovery catalog (US-2) | ✅ Slice done | Expanded app-bundled Unicode catalog covers nine approved categories with three additional localized entries per category, localized name/keyword search, clearable empty states, and exact default plus five skin-tone variants; selection reuses the editor insertion path and keeps the compact one-third-height sheet open. |
| Emoji Recent persistence and runtime validation (US-3) | ✅ Slice done | Exact inserted Unicode, including skin-tone variants, is stored as a bounded local MRU and restored after repository/app recreation with an empty fallback on read failure; the shipped picker has real Android glyph evidence and approved content, read-only, and empty-search visual captures. |

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
- **Table production editing flow and visual verification (US-3)**: Complete production action wiring, immediate editor updates, multi-table focus boundaries, and approved mockup evidence on top of the passing table operation and handle/sheet slices.

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
| **Note Editor** | 🟡 In progress | Text editing, document blocks, summary cards, action sheets, the complete emoji path, passing table structure/persistence operations, and focused table handle/sheet UI; production action flow and visual evidence remain on the roadmap |
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

*Document last updated: 2026-08-16*
