# Chart Block Visual Verification

**Reference design**: `design/mockup_chart_block_preview.png`

## Verification Scope

The area of interest is the complete ChartBlock card and its reachable Data and Options surfaces. The runtime captures were produced on `emulator-5554` at API 33 with the default font scale and the active test window. System status/navigation bars and runtime clock values are treated as dynamic chrome; chart values are deterministic fixture data but are compared structurally rather than pixel-for-pixel.

The visual checks cover the chart card header, title, chart plot, Data grid, first- and second-level Options sheets, empty recovery state, selected-datum callout, and dark read-only Data view. The editor shell outside the card is regression-only.

## Structural and Perceptual Findings

- All required controls were present and reachable through stable `testTag` selectors.
- Header controls retained aligned top bounds and at least 48 dp height.
- Plot and Data grid stayed within the chart card bounds; sheet surfaces stayed within the root bounds.
- Empty, selected, and dark read-only states retained the same chart-card hierarchy and readable semantic colors.
- No critical or major visual defects were observed. Minor status-bar clock and font rasterization differences are expected runtime variance.

## Reference Anchor Verification

| Visual Test ID | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|---|---|---|---|---|---|
| TC-US-4-VIS-01 | `design/mockup_chart_block_preview.png` chart card and plot | `ChartVisualFlowTest.kt#captureChartPreviewState`; testTag: `editor_chart_plot` | `plotBounds.left >= cardBounds.left`; `plotBounds.right <= cardBounds.right` | `visual_evidence/chart_preview.png` | PASS |
| TC-US-4-VIS-02 | `design/mockup_chart_block_preview.png` matching Data header and grid | `ChartVisualFlowTest.kt#captureDataViewState`; testTag: `editor_chart_data_grid` | `gridBounds.left >= cardBounds.left`; `gridBounds.right <= cardBounds.right` | `visual_evidence/chart_data_view.png` | PASS |
| TC-US-4-VIS-03 | `design/mockup_chart_block_preview.png` Options surface hierarchy | `ChartVisualFlowTest.kt#captureOptionsSheetsState`; testTag: `editor_chart_data_column_sheet` | `viewBounds.top == optionsBounds.top`; `optionsBounds.height >= 48` | `visual_evidence/chart_options_sheets.png` | PASS |
| TC-US-4-VIS-04 | `design/mockup_chart_block_preview.png` empty recovery and selected datum states | `ChartVisualFlowTest.kt#captureEmptyAndSelectedStates`; testTag: `editor_chart_plot` | `plotBounds.left >= cardBounds.left`; `plotBounds.right <= cardBounds.right` | `visual_evidence/chart_empty_selected.png` | PASS |
| TC-US-4-VIS-05 | `design/mockup_chart_block_preview.png` read-only dark chart/Data composition | `ChartVisualFlowTest.kt#captureReadOnlyDarkState`; testTag: `editor_chart_data_grid` | `gridBounds.left >= cardBounds.left`; `gridBounds.right <= cardBounds.right` | `visual_evidence/chart_read_only_dark.png` | PASS |

Supplemental empty-state capture: `visual_evidence/chart_empty_state.png`.
