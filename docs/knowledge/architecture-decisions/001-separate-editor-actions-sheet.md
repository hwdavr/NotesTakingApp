# ADR-001: Separate Editor Note Actions Sheet

## Status
Accepted

## Date
2026-05-05

## Context
Initially, the `NoteItemActionsSheet` was shared between the Home screen, Folder screen, and Note Editor to maintain UI consistency. However, the Note Editor requires a higher degree of customization and is expected to include editor-specific options (e.g., formatting settings, page layout, export options) that are not relevant to the list-based views.

Sharing a single component would lead to complex conditional logic within the component, making it harder to maintain and test as the feature sets for the editor and list views diverge.

## Decision
Create a separate `EditorNoteActionsSheet` specifically for the Note Editor. This component will initially be a copy of the shared `NoteItemActionsSheet` but will reside in the `ui.editor.components` package to allow for independent evolution.

## Alternatives Considered

### Maintain a Single Shared Component
- Pros: Maximum reuse, guaranteed consistency.
- Cons: Component becomes "bloated" with `if/else` checks for different screens. Increases risk of regressions in list views when modifying editor features.

### Composition with Sub-components
- Pros: Cleanest architectural approach.
- Cons: Higher initial complexity. The immediate need is for simple separation to unblock editor-specific features.

## Consequences
- The `NoteEditorScreen` now uses `EditorNoteActionsSheet`.
- Future editor-specific actions should be added to `EditorNoteActionsSheet`.
- Changes to the general `NoteItemActionsSheet` (for list views) will no longer automatically reflect in the Editor, which is intentional to allow for divergence.
- We must manually ensure that shared aesthetics (colors, padding) remain consistent between the two components if desired.
