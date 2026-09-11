# Navigation Re-entry Reloads Discard In-memory Edits

## Context

The Note Editor opens a nested destination (Add Web Bookmark) and returns to the editor after a
successful save. The nested destination receives only the URL, and the editor inserts the resolved
bookmark block into its in-memory document before the autosave debounce elapses.

`NoteEditorScreen` started the note load from a `LaunchedEffect(noteId, folderId)` keyed on the
route arguments:

```kotlin
LaunchedEffect(noteId, folderId) { viewModel.load(noteId, folderId) }
```

## Symptom

The first bookmark saved from the Advanced panel appeared in the editor and then disappeared while
the editor was still open. Instrumented production-entry tests showed the card visible immediately
after the pop back, then `UiState.document` reverting to the previously autosaved content on the
next frame.

## Root cause

Navigation Compose disposes a destination's composition while another destination is on top of the
back stack. When the editor became visible again, its `LaunchedEffect` restarted and called
`load()`, which replaced the document with the repository copy. The freshly inserted bookmark had
not been autosaved yet, so the reload silently dropped it.

## Fix

Only load when the requested note is not already loaded:

```kotlin
LaunchedEffect(noteId, folderId) {
    val state = viewModel.uiState.value
    if (!state.isLoaded || (noteId.orEmpty().isNotBlank() && state.noteId != noteId)) {
        viewModel.load(noteId, folderId)
    }
}
```

## Prevention

- Treat "load on entry" effects as destructive: they overwrite in-memory state, including unsaved
  edits, and they restart whenever the destination is re-entered.
- Cover cross-destination return paths with a production-entry journey test that asserts the
  post-return state, not only the destination's own screen.
- Prefer a nested-route result handoff (SavedStateHandle/insert in the editor) plus an
  already-loaded guard over reloading from the repository.
