# ADR-004 — Persist Voice Notes format settings separately from storage usage

## Status

Accepted

## Context

Voice Notes settings need one durable user preference (AAC or OPUS) and one live aggregate of existing private audio files. The preference must be available before a recording starts, while storage totals should remain derived from the Room metadata that already owns file size and path state.

## Decision

Store the selected audio format in an application-scoped Preferences DataStore repository and expose it as a `StateFlow`. Derive total audio bytes and recording count from `VoiceNoteBlockDao` flows, and combine both streams in the Settings ViewModel. The recording controller reads the repository's current format at start time, preserving AAC as the default/fallback when no preference has been persisted.

## Consequences

- Settings survives process restarts without duplicating preference state in the UI layer.
- Storage totals remain queryable and consistent with Room metadata rather than scanning private files from Compose.
- Format selection remains testable at the domain/data boundary and the next recording honors the latest persisted value.
- Codec availability and platform fallback remain responsibilities of the recording controller; the Settings screen only presents the selected preference and format guidance.

## Date

2026-08-15
