# Visual Reference Anchor Verification

**Reference design**: `design/mockup_table_handles_v2.png`

The focused-table evidence was refreshed on `emulator-5554` after the border-anchor correction.
The sheet evidence was refreshed in the same 21/21 `TableHandlesScreenTest` runtime run.

## Reference Anchor Verification

| Visual Test ID | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|----------------|------------------|---------------|-----------------------|-------------------|--------|
| TC-US-3-VIS-01 | The focused column visual ends at the grid top, the row visual ends at the grid left, and the compact table-options visual is centered on the top-right border. | `TableHandlesScreenTest#handlesAlignToGridGeometryAndTableOptionsVisualIsShallow`; testTag: `table_column_handle_visual`, `table_row_handle_visual`, `table_options_visual`, `editor_table_grid` | `columnVisualBounds.bottom == gridBounds.top ± 2dp`; `rowVisualBounds.right == gridBounds.left ± 2dp`; `optionsVisualBounds.centerY == gridBounds.top ± 2dp` | `visual_evidence/table_handles_focused.png` | PASS |
| TC-US-3-VIS-02 | The Column Options sheet keeps the destructive action after all non-destructive actions, separated by its divider. | `TableHandlesScreenTest#deleteIsFinalActionInEverySheet`; testTag: `table_delete_column`, `table_column_options_sheet_delete_divider` | `deleteBounds.top > earlierActionBounds.top`; `dividerBounds.top < deleteBounds.top` | `visual_evidence/table_column_sheet.png` | PASS |
| TC-US-3-VIS-03 | The Row Options sheet keeps the destructive action after all non-destructive actions, separated by its divider. | `TableHandlesScreenTest#deleteIsFinalActionInEverySheet`; testTag: `table_delete_row`, `table_row_options_sheet_delete_divider` | `deleteBounds.top > earlierActionBounds.top`; `dividerBounds.top < deleteBounds.top` | `visual_evidence/table_row_sheet.png` | PASS |
| TC-US-3-VIS-04 | The Table Options sheet keeps the destructive action after all non-destructive actions, separated by its divider. | `TableHandlesScreenTest#deleteIsFinalActionInEverySheet`; testTag: `table_delete`, `table_options_sheet_delete_divider` | `deleteBounds.top > earlierActionBounds.top`; `dividerBounds.top < deleteBounds.top` | `visual_evidence/table_options_sheet.png` | PASS |
