# Visual Reference Anchor Verification

**Reference design**: `design/mockup_mermaid_fullscreen_viewer.png`

## Reference Anchor Verification

| Visual Test ID | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|---|---|---|---|---|---|
| TC-US-4-VIS-01 | Mermaid card preview horizontal margin | `MermaidBlockCardTest#testMermaidCardDefaultsToPreviewMode`; testTag: `editor_mermaid_preview_canvas_m1` | `canvasBounds.width == screenBounds.width - 32dp` | `visual_evidence/mermaid_card_preview.png` | PASS |
| TC-US-4-VIS-02 | Mermaid code editor padding and layout | `MermaidBlockCardTest#testToggleBetweenPreviewAndCodeEditor`; testTag: `editor_mermaid_code_editor_m2` | `editorBounds.width == screenBounds.width - 32dp` | `visual_evidence/mermaid_card_code_editor.png` | PASS |
| TC-US-4-VIS-03 | Fullscreen diagram canvas edge-to-edge layout | `FullscreenDiagramViewerTest#testOpenFullscreenViewerAndNavigateBack`; testTag: `fullscreen_diagram_canvas` | `canvasBounds.width == screenBounds.width` | `visual_evidence/mermaid_fullscreen_viewer.png` | PASS |
