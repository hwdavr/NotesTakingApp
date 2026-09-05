# Visual Reference Anchor Verification

**Reference design**: `design/mockup_note_link_picker.png`

## Verification Scope

The area of interest encompasses the formatting toolbar rail, responsive formula sheet, and note link picker screens. Runtime captures were produced on `emulator-5554` at API 33 in the active test window. System status/navigation bars and runtime clock values are treated as dynamic chrome; editor and sheet components are evaluated structurally and against promoted golden baselines.

The visual checks cover:
1. Toolbar with selected text and active controls.
2. Toolbar positioned above the soft keyboard with pending bold mark.
3. Note link picker candidate list, search input, and folder subtitles.
4. Formula sheet default state with source input and preview.
5. Formula sheet validation error state with localized feedback.
6. Formula sheet above the keyboard with horizontally scrollable preview.
7. Formula sheet in dark theme mode.

## Structural and Perceptual Findings

- All required controls are present and reachable through stable `testTag` selectors.
- Toolbar height conforms to the 56dp spec and action items meet >= 48dp touch target requirements.
- Note link picker candidates exclude the caller note and display folder subtitles or "No folder".
- Formula preview scrolls horizontally without text wrapping or clipping.
- Read-only and dark theme modes render semantic tokens consistently.
- No visual or layout regressions were detected.

## Reference Anchor Verification

| Visual Test ID | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|---|---|---|---|---|---|
| TC-US-4-VIS-001 | `design/mockup_editor_formatting.png` selection formatting toolbar | `FormattingToolbarVisualFlowTest#captureToolbarSelection`; testTag: `editor_formatting_bottom_bar` | `toolbarBounds.height >= 48.dp`; `toolbarBounds.left >= screenBounds.left` | `visual_evidence/formatting_toolbar_selection.png` | PASS |
| TC-US-4-VIS-002 | `design/mockup_editor_formatting_keyboard.png` toolbar positioned above keyboard | `FormattingToolbarVisualFlowTest#captureEditorKeyboard`; testTag: `editor_formatting_bottom_bar` | `toolbarBounds.bottom <= keyboardBounds.top`; `toolbarBounds.height >= 48.dp` | `visual_evidence/formatting_toolbar_keyboard.png` | PASS |
| TC-US-4-VIS-003 | `design/mockup_note_link_picker.png` link candidate list and search | `FormattingToolbarVisualFlowTest#captureLinkPicker`; testTag: `note_link_picker_results` | `resultsBounds.top >= searchBounds.bottom + 16.dp`; `resultsBounds.height >= 48.dp` | `visual_evidence/note_link_picker.png` | PASS |
| TC-US-4-VIS-004 | `design/mockup_formula_sheet.png` default formula sheet | `FormattingToolbarVisualFlowTest#captureFormulaDefault`; testTag: `editor_formula_sheet` | `sheetBounds.left >= screenBounds.left`; `sheetBounds.right <= screenBounds.right` | `visual_evidence/formula_sheet_default.png` | PASS |
| TC-US-4-VIS-005 | `design/mockup_formula_sheet.png` formula error state | `FormattingToolbarVisualFlowTest#captureFormulaInvalid`; testTag: `editor_formula_sheet` | `errorBounds.top >= previewBounds.bottom`; `sheetBounds.height >= 48.dp` | `visual_evidence/formula_sheet_invalid.png` | PASS |
| TC-US-4-VIS-006 | `design/mockup_formula_sheet_keyboard.png` formula sheet above keyboard | `FormattingToolbarVisualFlowTest#captureFormulaSheetKeyboard`; testTag: `editor_formula_sheet` | `previewBounds.right <= screenBounds.right`; `actionsBounds.height >= 48.dp` | `visual_evidence/formula_sheet_keyboard.png` | PASS |
| TC-US-4-VIS-007 | `design/mockup_formula_sheet.png` dark theme formula sheet | `FormattingToolbarVisualFlowTest#captureFormulaSheetDarkTheme`; testTag: `editor_formula_sheet` | `sheetBounds.left >= screenBounds.left`; `sheetBounds.right <= screenBounds.right` | `visual_evidence/formula_sheet_dark_theme.png` | PASS |
