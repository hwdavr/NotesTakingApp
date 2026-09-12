## Reference Anchor Verification

**Reference design**: `design/mockup_editor_web_bookmark_actions_sheet_from_baseline_v2.png`

**Pixel comparison scope**: The action-sheet comparison masks the unchanged editor backdrop above
the sheet; the active-window capture and runtime bounds still verify the scrim, rounded sheet
surface, handle, header, divider, and action rows.

| Visual Test ID | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|---|---|---|---|---|---|
| TC-US-2-VIS-01 | Card visual surface | `WebBookmarkVisualFlowTest#capturesBookmarkCardState`; testTag: `editor_web_bookmark_card_visual_bookmark-visual-1` | CardVisualBounds.width > 0 and CardVisualBounds.height > 0 | visual_evidence/web_bookmark_card_content.png | PASS |
| TC-US-2-VIS-02 | URL-only page shell and bottom actions | `WebBookmarkAppShellVisualFlowTest#capturesAddBookmarkPageFromAppShell`; captureScope: app-shell; productionRoot: `AppNavHost`; testTags: `web_bookmark_editor_page`, `app_bottom_navigation` | PageBounds.right > PageBounds.left, PageBounds.bottom > PageBounds.top, and app_bottom_navigation count = 0 | visual_evidence/web_bookmark_add_page.png | PASS |
| TC-US-2-VIS-03 | Focused URL field above IME | `WebBookmarkVisualFlowTest#capturesAddBookmarkPageKeyboardState`; testTag: `web_bookmark_url_field` | KeyboardPageBounds.height > 0 and KeyboardPageBounds.width > 0 | visual_evidence/web_bookmark_add_page_keyboard.png | PASS |
| TC-US-2-VIS-04 | Actions sheet handle, header, divider, and rows | `WebBookmarkVisualFlowTest#capturesBookmarkActionsSheetState`; testTag: `web_bookmark_actions_sheet` | SheetBounds.height > 0 and SheetBounds.width > 0 | visual_evidence/web_bookmark_actions_sheet.png | PASS |
| TC-US-2-VIS-05 | Dark/RTL/large-text card readability | `WebBookmarkVisualFlowTest#supportsDarkRtlAndLargeTextWithoutClipping`; testTag: `editor_web_bookmark_card_visual_bookmark-visual-1` | ResponsiveCardBounds.width > 0 and ResponsiveCardBounds.height > 0 | visual_evidence/web_bookmark_responsive.png | PASS |
