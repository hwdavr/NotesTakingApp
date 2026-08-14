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
| voice-notes-audio-transcripts | Voice Notes & Audio Transcripts | [docs/product/2026-08-14-voice-notes/](2026-08-14-voice-notes/) | In Progress | 2026-08-14 | US-1 recording core is passing with API-33 runtime evidence; US-2 through US-5 remain, and US-5 owns final Light Theme visual verification. |
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

### 🚧 Voice Notes & Audio Transcripts

| Feature | Status | Notes |
|---|---|---|
| Safe recording session (US-1) | ✅ Done | Private AAC/OPUS file lifecycle, 128 MB preflight, foreground service notification controls, permission recovery, session guard, reducer/ViewModel state bridge, and deterministic cleanup. |
| Progressive transcription, entry points, editor persistence, and settings | 🚧 Planned | Delivered in the remaining approved vertical slices US-2 through US-5. |

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
- **Voice Notes & Audio Transcripts**: US-1 recording core is delivered; progressive transcription, Home/editor entry points, inline editor persistence/playback, and Voice Notes settings remain planned in US-2 through US-5.
- **Image & File Attachments**: Attach photos, diagrams, and PDF files to notes with inline preview.
- **Checklists & Task Lists**: Interactive checkable list items within note documents.

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
| **Note Editor** | ✅ Shipped | Text editing, document blocks, summary cards, action sheets |
| **Folders & Categories** | ✅ Shipped | Tree structure, move operations, smart AI categorization |
| **AI Summarizer** | ✅ Shipped | Gemini Nano on-device summarization, status handling |
| **Sharing & Security** | ✅ Shipped | Invite sharing, manage permissions, password validation |
| **Export & Export UI** | ✅ Shipped | Text, Markdown, PDF export screens |
| **App Shell & Theme** | ✅ Shipped | M3 theme, bottom bar navigation, light/dark mode |
| **Voice Notes** | 🚧 In Progress | US-1 safe private recording core; transcription and cross-surface integration remain |

---

## Technical Architecture

- **Architecture**: Android Clean Architecture with Unidirectional Data Flow (UDF).
  - `ui/`: Jetpack Compose screens, ViewModels (`StateFlow<UiState>`), and reusable M3 components.
  - `domain/`: Business models, use cases (`SummarizeNoteUseCase`, `CategorizeNoteUseCase`), repository contracts.
  - `data/`: Room Database (`NoteDao`, `FolderDao`), Remote API interfaces, MediaPipe / Gemini Nano AI clients, Data Mappers.
- **Tech Stack**: Kotlin 1.9+ · Jetpack Compose · Material 3 · Navigation Compose · Room · Hilt · KSP · Java 17 · minSdk 24 / targetSdk 34.

*Document last updated: 2026-08-14*
