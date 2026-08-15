# ADR-005 — Persist emoji Recents separately from note content

## Status

Accepted

## Context

The emoji picker needs to restore the exact Unicode sequence selected by the user, including skin-tone modifiers, after repository or app recreation. Recent selections are presentation history rather than note content, so storing them in the Room note document would couple picker history to sync, sharing, export, and note-schema migrations.

## Decision

Expose a domain `RecentEmojiRepository` contract backed by an application-scoped Preferences DataStore. Store a bounded most-recently-used list of exact Unicode strings, move duplicate selections to the front, ignore empty values, and expose a recoverable empty flow when preferences cannot be read. Record a value only after the editor confirms that the Unicode sequence was inserted successfully.

## Consequences

- Recent ordering survives repository/app recreation without changing the Room schema or remote contracts.
- Exact Unicode sequences remain intact while note content continues through the existing save/sync/share/export path.
- DataStore corruption or read failure degrades the picker to an empty Recent state while catalog browsing remains available.
- The application-scoped file path is a stable preference boundary; future changes must preserve the bounded MRU behavior and avoid persisting picker history into note documents.

## Date

2026-08-15
