# Visual Reference Anchor Verification

**Reference design**: `design/mockup_code_block_editor.png`

The Advanced Basic Blocks panel row uses the companion approved reference `design/mockup_basic_blocks_panel_advanced.png` from the same feature design workspace.

## Reference Anchor Verification

| Visual Test ID | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|----------------|------------------|---------------|-----------------------|-------------------|--------|
| TC-US-3-VIS-01 | Code card is inset from the editor content surface by the standard 16dp horizontal content padding; header controls share one center line; line gutter starts with the editor body. | `CodeBlockVisualFlowTest#captureCodeBlockEditor`; testTag: `editor_code_block_visual-code-1`, `note_editor_content`, `editor_code_lang_selector_visual-code-1`, `editor_code_copy_btn_visual-code-1`, `editor_code_delete_btn_visual-code-1`, `editor_code_line_numbers_visual-code-1`, `editor_code_editor_visual-code-1` | `cardBounds.left == contentBounds.left + 16dp ± 2dp`; `contentBounds.right == cardBounds.right + 16dp ± 2dp`; `languageCenter == copyCenter == deleteCenter ± 2dp`; `lineNumbersBounds.top == editorBounds.top ± 2dp` | `visual_evidence/code_block_editor.png` | PASS |
| TC-US-3-VIS-02 | Advanced panel begins immediately after its divider and places the Code tile below the Advanced section header. | `CodeBlockVisualFlowTest#captureBasicBlocksPanelAdvanced`; testTag: `basic_blocks_panel`, `basic_blocks_panel_divider`, `basic_blocks_section_advanced`, `basic_blocks_code` | `panelBounds.top == dividerBounds.bottom ± 2dp`; `advancedHeaderBounds.bottom <= codeTileBounds.top` | `visual_evidence/basic_blocks_panel_advanced.png` | PASS |

