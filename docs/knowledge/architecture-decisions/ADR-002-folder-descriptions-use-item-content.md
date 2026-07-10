# ADR-002 — Store Folder Descriptions as Item Content

## Status
Accepted

## Context
Smart category needs more context than folder names and hierarchy paths can provide. The shared tree API already has a required `content` field on every item, but the contract previously described folder content as an empty string. Adding a separate `description` field would duplicate item text semantics and require a wider API/model migration.

## Decision
Use `Item.content` as the folder description when `Item.type == "folder"`. Notes continue to use `content` as the note body. Android maps folder `content` into `Folder.description`, persists it in Room, and uses the generalized `PATCH /v1/items/{itemID}/content` mutation for folder descriptions.

## Consequences
- Smart category can use user-authored folder intent without adding a parallel API field.
- Existing note content behavior remains unchanged.
- API clients must interpret `content` by item type.
- Backend support for `PATCH /v1/items/{itemID}/content` is required for cross-device folder description sync; Android keeps local fallback behavior if the mutation is unavailable.

## Date
2026-07-09
