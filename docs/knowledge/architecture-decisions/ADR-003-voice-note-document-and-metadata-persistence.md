# ADR-003 — Keep VoiceNote metadata and editor document state in sync

## Status

Accepted

## Context

Voice recordings need two different durable representations: the editor must restore an ordered audio block followed by an editable transcript block, while local cleanup and future storage reporting need queryable VoiceNote metadata and private-file paths. Putting either responsibility entirely in the other representation would make editor ordering or file lifecycle operations brittle.

## Decision

Persist the ordered `EditorBlock.Voice` plus transcript `EditorBlock.TextBlock` in the note document JSON, and persist the matching `VoiceNoteBlock` metadata in Room. Route recording insertion and audio-only deletion through domain use cases so the document and metadata updates happen together from the application boundary. Route block and note deletion through the VoiceNote repository so private files are deleted even when a file is already missing.

## Consequences

- Editor reloads preserve block ordering and transcript editability without introducing a second rich-text model.
- Room can query audio metadata independently for cleanup and the later Voice Notes settings slice.
- Any future VoiceNote document mutation must update both representations through a domain boundary; UI code must not edit either persistence format directly.
- The Room migration and JSON schema are part of the local compatibility contract and must be covered when either representation changes.

## Date

2026-08-14
