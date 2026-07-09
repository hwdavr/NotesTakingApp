# Compose Scroll Container Display Assertions

## Symptom

Instrumented Compose tests for editor content can fail with:

```text
java.lang.AssertionError: Assert failed: The component is not displayed!
```

This can happen even when the tagged node is composed and present in the semantics tree.

## Cause

`assertIsDisplayed()` is viewport-sensitive. The note editor renders content inside a scrollable editor surface, and large nested containers such as `rich_document_blocks` or summary panels can be clipped or outside the current visible bounds on a specific AVD size.

## What to do instead

For off-viewport editor content, assert semantic presence with `onAllNodesWithTag(..., useUnmergedTree = true).fetchSemanticsNodes()` after `waitForIdle()`. Keep `assertIsDisplayed()` for stable visible controls such as dialogs, bottom bars, and buttons.

## Reference

Observed while adding the smart-categorization no-match dialog tests on 2026-07-09. The affected tests were `NoteEditorSummaryPanelTest` and `NoteEditorRichDocumentScreenTest`.
