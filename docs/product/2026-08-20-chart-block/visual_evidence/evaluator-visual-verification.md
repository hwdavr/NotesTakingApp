# Evaluator Visual Verification — chart-block

**Date**: 2026-08-23
**Runtime**: `emulator-5554`, API 33, 1080×2400 px, physical density 420 dpi (approximately 411×914 dp), target SDK 34
**Locale/font scale**: English/default font scale; the emulator did not expose a non-default `font_scale` value
**Reference assets**: `design/mockup_chart_block_preview.png`, `mockup_chart_block_table_view.png`, `mockup_chart_block_options_sheet.png`, `mockup_chart_block_data_column_sheet.png`, and `mockup_chart_block_creation_panel.png`

The runtime captures were produced inside `ChartVisualFlowTest` after `composeRule.waitForIdle()` with `uiAutomation.takeScreenshot()`, copied from `/sdcard/Download`, and freshly pulled during Stage 4. They were not post-test device screencaps.

## Phase 1 — Normalize

Reference images have mixed source sizes (1024×1536, 1122×1402, and 864×1820) and omit system bars. Runtime images are 1080×2400 and include status/navigation chrome. Comparison therefore uses the chart-card logical coordinate space and the runtime's approximately 411×914 dp space; the status bar, navigation bar, clock, network indicator, and dark-mode system chrome are excluded from visual judgments. Light references are compared with light captures; `chart_read_only_dark.png` is compared against the dark-theme contract rather than a light mockup.

## Phase 2 — Scope

**Area of interest**: the complete ChartBlock card and reachable Chart/Data, Options, Data column, empty, selected-datum, and dark read-only surfaces. This follows FR-006–FR-017, AC-003–AC-012, and the approved `design.md` component/anchor contracts.

**Full verification**:

- card/header and current-view CTA;
- title/type/selected-column metadata;
- chart plot and datum selection;
- Data grid and data hint;
- first-level Options and second-level Data column sheets;
- empty/error recovery;
- selected tooltip;
- read-only dark Data view.

**Regression-only**: the surrounding editor toolbar, note title, document block list, editor navigation, and export destination UI. The visual owner tests render the card in isolation, so those surrounding regions are not freshly proven by these captures.

## Phase 3 — Region decomposition

| Region | Approximate runtime bounds / relationship | Reference basis |
|---|---|---|
| Card container | Full test root width; card begins below system status bar and contains the chart body | Preview/table mockups; `editor_chart_block` |
| Header | View CTA, centered title, Options CTA; both header controls are at least 48 dp and top-aligned | Preview mockup; `editor_chart_view_cta`, `editor_chart_options_cta` |
| Metadata | Chart icon/type and selected-column label under title | Preview/table mockups; `editor_chart_type`, `editor_chart_selected_column` |
| Plot | Inside the card body; plot bounds are inside card bounds in the fresh structural assertions | Preview mockup; `editor_chart_plot` |
| Data grid | Full-width bordered grid below the data hint; expected to show header plus populated rows | Table mockup; `editor_chart_data_grid` |
| Options surface | Bottom sheet over the chart; expected first level has Data column/Add row/Add column | Options mockup; `editor_chart_options_sheet` |
| Data-column surface | Second-level sheet with back affordance and one row per candidate column | Data-column mockup; `editor_chart_data_column_sheet` |
| Empty/selected state | Empty message or selected chart/tooltip inside the card | Spec empty state and preview mockup |
| Dark read-only state | Same hierarchy in dark colors with data edits disabled | Design system dark semantics; `chart_read_only_dark.png` |

## Phase 4 — Structural verification

Default tolerances: position ±4 dp, size ±5%, spacing ±4 dp.

| Region / element | Expected | Fresh runtime evidence | Result |
|---|---|---|---|
| Header View CTA | Visible, first header control, height ≥48 dp | `ChartVisualFlowTest.assertHeaderLayout` measured `editor_chart_view_cta`; test passed | PASS |
| Header Options CTA | Visible, height ≥48 dp, aligned with View CTA | `assertHeaderLayout` measured `editor_chart_options_cta`; test passed | PASS |
| Plot containment | Plot left/right remain inside card | `assertChartLayout` passed for `chart_preview` and selected flow | PASS |
| Data-grid containment | Grid left/right remain inside card | `assertDataLayout` passed for Data and dark read-only flows | PASS structurally |
| Data grid content | Header plus fixture rows are visible and not clipped | Fresh `chart_data_view.png` visibly shows `Category / Revenue / Cost` but no `January / 120 / 80` row values; grid occupies an anomalously tall blank region | **FAIL — major** |
| Options first level | Captured surface visibly contains Data column, Add row, Add column | Test asserts first-level nodes before navigating, but the pulled screenshot is after navigation and shows only Data column sheet | **FAIL — evidence gap** |
| Tooltip dismiss affordance | Minimum 48 dp and no overlap with tooltip text | Source is `.size(40.dp)` at `ChartBlockCard.kt:521`; selected capture shows the close glyph over the tooltip’s right edge | **FAIL — major** |
| Sheet bounds | Sheet stays inside root and uses safe insets | Fresh options capture shows a bounded bottom sheet and the contract check passed | PASS |
| Dark read-only hierarchy | Data grid and top controls remain visible/readable | Fresh dark capture has readable text and controls; data values are still absent | PASS for contrast; data-content finding remains open |

## Phase 5 — Dynamic content masking

- Status-bar time, network indicator, and navigation gesture bar: runtime chrome.
- Chart numeric values and category labels: deterministic fixture content, compared for presence/layout rather than pixel identity.
- Selected tooltip text and selected-bar outline: interaction state.
- Dark/light system surface differences: compared only to the matching theme contract.

## Phase 6 — Perceptual comparison

| Region | Confidence | Assessment |
|---|---|---|
| Header/card | High | Runtime uses the expected purple semantic accent, rounded bordered card, eye/view affordance, centered title, chart icon, type label, and options affordance. The standalone capture fills the test root more than the centered reference mockup; this is partly caused by the card-only test harness and is not treated as the primary defect. |
| Plot | High | Bar geometry, labels, grid, selected outline, and empty-state typography are coherent and readable. The runtime chart is more compact vertically than the reference, but remains inside the card. |
| Data view | High | The header/type/hint structure matches the intended hierarchy, but the visible grid contains only headers and a large empty region; the reference contains populated January–April rows. This is a major data-fidelity/layout defect. |
| Options | High | The captured second-level sheet is visually readable and follows the Material bottom-sheet hierarchy, but the approved first-level Options mockup is not the captured final state. The visual evidence is incomplete for the first-level surface. |
| Empty/selected | High | Empty state is centered and localized. The selected bar is visibly outlined and tooltip text is readable, but the dismiss control overlaps the tooltip and is undersized. |
| Dark read-only | High | Dark surfaces, text, purple accent, and disabled/editable distinction are generally readable. The same missing data-row content appears in the grid. |

## Phase 7 — Defect classification

| ID | Region | Severity | Description | Suggested fix | Status |
|---|---|---|---|---|---|
| VIS-01 | Data grid | major | Fresh runtime Data capture shows no visible data-row values despite the deterministic fixture containing data; the grid is an anomalously tall blank surface compared with the table mockup. | Reproduce with the production editor and fix row measurement/rendering so every normalized row is visible; add an assertion for row text and a bounds/scroll test. | open |
| VIS-02 | Options evidence | major | `captureOptionsSheetsState` screenshots only the second-level Data column sheet after navigation, while the approved Options mockup is the first-level surface. | Capture both first-level and second-level states with separate methods/files and tie each to a contract row/reference anchor. | open |
| VIS-03 | Selected tooltip | major | Dismiss control is 40 dp and visually overlaps the tooltip text in `chart_empty_selected.png`; the design requires 48 dp minimum targets. | Use a 48 dp target with layout-aware placement/semantics, and assert non-overlap and bounds. | open |
| VIS-04 | Chart data rendering | major | The custom renderer maps Bar/Line values from a positive-only maximum and has no zero baseline, so negative values can be drawn outside the intended chart area. | Compute a domain containing negative and positive values and draw bars/lines relative to zero. | open |

## Phase 8 — Structured AI visual evaluation

```json
{
  "status": "FAIL",
  "area_of_interest": "ChartBlock card, Data view, Options surfaces, empty/selected states, and dark read-only state",
  "issues": [
    {
      "region": "data_grid",
      "severity": "major",
      "issue": "Runtime Data capture has the expected header but no visible fixture row values and an oversized blank grid.",
      "suggestion": "Fix production row layout/rendering and assert representative row text and scroll behavior."
    },
    {
      "region": "options_surface",
      "severity": "major",
      "issue": "The screenshot evidence ends on the second-level Data column sheet and does not show the first-level Options surface in the approved mockup.",
      "suggestion": "Capture and anchor first-level and second-level sheets independently."
    },
    {
      "region": "selected_tooltip",
      "severity": "major",
      "issue": "The close affordance overlaps the tooltip and is smaller than the 48 dp contract.",
      "suggestion": "Re-layout the dismiss target and verify measured bounds/non-overlap."
    }
  ],
  "regression_summary": {
    "editor_shell": "Not freshly proven; visual owner renders the chart card in isolation.",
    "chart_header": "PASS — expected hierarchy and controls are visible.",
    "plot_and_empty_state": "PASS with renderer negative-value risk — structure is visible and empty state is readable.",
    "dark_read_only": "PASS for contrast and inspection hierarchy; data-row visibility finding remains open."
  }
}
```

## Verdict

**FAIL / REVISION REQUIRED**. The runtime contract and reference-anchor script pass, but the direct screenshot comparison exposes unresolved major data-grid and tooltip defects, and the Options visual evidence does not capture the required first-level state. There are no critical wrong-screen/navigation failures in the captured flows, but the major findings prevent visual approval.

## Fix Pass

The original visual review is preserved above as history. The final active-rendering captures and structural assertions resolve all four visual findings at commit 7545d61.

| ID | Fix Status | Evidence |
|---|---|---|
| VIS-01 | Fixed ✅ | DataTable uses fixed 48dp rows inside a 360dp bounded scroll container; ChartVisualFlowTest asserts January/120/80, bounded height, and a 200-row scroll gesture. `chart_data_view.png` was freshly pulled from the active test window. |
| VIS-02 | Fixed ✅ | `captureOptionsSheetsState` pulls `chart_options_sheets.png` at the first-level sheet and `chart_data_column_sheet.png` after second-level navigation; both contract rows and reference anchors pass. |
| VIS-03 | Fixed ✅ | Tooltip dismissal is a contained 48dp IconButton in the tooltip Row; bounds/non-overlap assertions pass in the selected-state flow and `chart_empty_selected.png` was freshly pulled. |
| VIS-04 | Fixed ✅ | ChartRenderGeometry provides zero-inclusive Bar/Line domains and the platform boundary asserts negative-value output; `chart_preview.png` and selected-state captures pass. |

### Final visual evidence

- `ui_verification.json` records 16/16 chart-package instrumented tests, zero unresolved major findings, and PASS AI evaluation.
- `bash harness/scripts/check-ui-verification-artifact.sh docs/product/2026-08-20-chart-block` exited 0.
- `bash harness/scripts/check-visual-evidence-contract.sh docs/product/2026-08-20-chart-block --evaluate` exited 0.
- `bash harness/scripts/check-platform-evidence.sh docs/product/2026-08-20-chart-block --evaluate` exited 0.
- Direct API24/API34 emulator images were not provisioned; the capability matrix documents this as a human-review environment requirement under the fail-loudly policy.

> **Fix Pass:** VIS-01 through VIS-04 fixed; 0 unresolved visual findings (2026-08-23).

## Final Verdict

**PASS / READY FOR HUMAN REVIEW** — the previously reported Data-grid, Options evidence, tooltip, and negative-domain findings are resolved with fresh active-window screenshots, bounds assertions, and machine-checkable evidence.
