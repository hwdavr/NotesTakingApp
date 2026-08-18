# Feature Design — Mermaid Chart & Preview in Note Editor

**Date**: 2026-08-18  
**Status**: Approved  
**Source request**: Mermaid chart diagramming, editing, and interactive preview inside the note editor  
**Related spec**: `spec.md`  
**Project design system**: `docs/product/design_system.md`  
**Approved design-system exceptions**: None. All components strictly adhere to `docs/product/design_system.md` tokens and Material 3 guidelines.  

---

## Conditional Keyboard-Visible Mockup Contract

Not applicable. The Mermaid diagram feature renders inline inside the Note Editor content area and provides a dedicated Fullscreen Diagram Viewer screen/dialog; it does not introduce any bottom modal text-input surface.

---

## Screens Covered

| # | Screen / Surface | Status |
|---|---|---|
| 1 | Note Editor — Mermaid Diagram Block Component | Updated |
| 2 | Fullscreen Diagram Viewer Screen | New |

---

## Screen 1 — Note Editor — Mermaid Diagram Block Component

### Purpose

Provide an inline, self-contained diagramming block within the Note Editor where users can view rendered Mermaid diagrams (flowcharts, sequence diagrams, class models, etc.) directly in place, toggle into a code editor to modify the Mermaid DSL, and interactively pan/zoom the visual output.

### UX Principles

- **Preview by Default**: Diagrams are rendered visually as high-quality SVGs on load so the note remains clean and readable without exposing raw code syntax unless requested.
- **Instant Mode Switching**: A single tap on "Edit Code" smoothly transitions the block into code editing mode, and tapping "View Chart" returns to the rendered preview.
- **Uncompromised Offline Performance**: Bundled local JavaScript executes 100% on-device inside a local WebView sandbox with zero network requests and sub-second rendering latency.

### Entry And Exit

- **Entry points**: 
  - User opens an existing note containing a Mermaid block.
  - User taps the "Mermaid Diagram" tile in the Basic Blocks panel.
- **Primary success exit**: Diagram renders in preview card; changes auto-save with the note document.
- **Cancel/back behavior**: Tapping "View Chart" exits code editing mode back to rendered diagram preview.
- **Failure exit or recovery**: If invalid syntax is entered, an inline non-blocking error badge displays the syntax issue while the user remains in code mode to correct it.

### Information Architecture

1. **Card Container**: Elevated white surface (`#FFFFFF`), subtle border stroke (`#E7E3F6`), 12dp rounded corners.
2. **Card Header**:
   - Left: Diagram icon and editable title field (`titleSmall` semibold in `#191627`).
   - Right: Mode toggle pill button ("Edit Code" / "View Chart" with icon), Fullscreen expand button, and block options menu.
3. **Card Body (Preview Mode)**:
   - Centered responsive SVG canvas displaying the rendered Mermaid chart.
   - Inline touch viewport supporting two-finger pinch-to-zoom and one-finger drag-to-pan.
   - Bottom-left overlay: Zoom reset chip indicator (`- / + / 100%`).
4. **Card Body (Code Editor Mode)**:
   - Quick template chips row (`#F1EEFF` background, `#7C6CF2` text: "Flowchart", "Sequence", "Class", "State").
   - Syntax-highlighted code editor area (`#1E1E2E` or monospace `#191627` on light surface) for editing Mermaid code.
   - Syntax validation status badge (Green check for valid, Amber/Red alert for syntax error with line snippet).

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| `MermaidCardContainer` | Main block card container | Default, Focused | `editor_mermaid_block_{id}` |
| `MermaidHeaderTitle` | Editable title for the diagram | Default, Editing | `editor_mermaid_title_{id}` |
| `MermaidToggleModeButton` | Switch between Preview and Code editor | "Edit Code", "View Chart" | `editor_mermaid_toggle_mode_{id}` |
| `MermaidFullscreenButton` | Open fullscreen diagram viewer | Default, Disabled (empty) | `editor_mermaid_fullscreen_btn_{id}` |
| `MermaidPreviewCanvas` | Zoomable & pannable SVG diagram area | Loading, Content, Error, Empty | `editor_mermaid_preview_canvas_{id}` |
| `MermaidCodeEditor` | Monospace text field for Mermaid code | Focused, Unfocused | `editor_mermaid_code_editor_{id}` |
| `MermaidTemplateChip` | Quick-insert boilerplate template | Normal, Pressed | `editor_mermaid_template_chip_{template}` |
| `MermaidSyntaxBadge` | Indicates validation status | Valid, Error | `editor_mermaid_syntax_badge_{id}` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Mermaid card horizontal alignment | `editor_mermaid_block_{id}`, `note_editor_content` | `card.left == content.left + 16dp`, `card.right == content.right - 16dp` |
| Mode toggle pill button in card header | `editor_mermaid_toggle_mode_{id}`, `editor_mermaid_title_{id}` | `toggleBtn.centerY == title.centerY` |
| Monospace editor padding | `editor_mermaid_code_editor_{id}`, `editor_mermaid_block_{id}` | `editor.padding == 12dp` |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Loading | Subtle pulsing shimmer placeholder within card bounds | Wait for SVG render |
| Empty | Placeholder card with "Empty Diagram. Tap Edit Code to start" | Tap "Edit Code" |
| Preview (Content) | Rendered vector flowchart / diagram with title and header actions | Pinch to zoom, drag to pan, tap Fullscreen, tap "Edit Code" |
| Code Editor | Monospace text editor with template chips and syntax validation indicator | Type code, insert templates, edit title, tap "View Chart" |
| Syntax Error | Red/amber alert banner inside card with error details | Edit code to fix syntax, view error hint |

### Interaction Rules

- **Primary action**: Tapping "Edit Code" toggles the card to code editor mode; tapping "View Chart" re-renders and toggles to preview mode.
- **Gesture interaction**: Pinch-to-zoom and single-finger pan are isolated inside the `MermaidPreviewCanvas` without intercepting vertical note scrolling when gestures are outside the canvas bounds.
- **Template selection**: Tapping a template chip (e.g. "Sequence") replaces or inserts starter Mermaid DSL code into the editor.
- **Auto-save**: Any edit to diagram title or code triggers debounced note auto-save without requiring explicit save action.

### Copy Requirements

| Element | Copy |
|---|---|
| Mode Toggle (to Code) | "Edit Code" |
| Mode Toggle (to Preview) | "View Chart" |
| Default Diagram Title | "Mermaid Diagram" |
| Empty State Hint | "Tap Edit Code to create a diagram" |
| Valid Syntax Status | "Valid Mermaid syntax" |
| Syntax Error Header | "Syntax error in diagram code" |
| Template Chips | "Flowchart", "Sequence", "Class", "State" |

### Accessibility

- Card header toggle button has localized content description: "Switch to code editor" / "Switch to diagram preview".
- Zoom indicator and full-screen buttons have minimum 48×48dp touch targets.
- Monospace editor supports screen reader text inspection and keyboard navigation.

### Responsive And Configuration Behavior

- In landscape and tablet modes, the diagram card expands dynamically to fill available content width while preserving aspect ratio.
- Diagram zoom and pan offsets persist across device rotation and recomposition.

### Design Assets

- **Mockup image (Preview mode)**: `design/mockup_mermaid_card_preview.png`
- **Mockup image (Code Editor mode)**: `design/mockup_mermaid_card_code_editor.png`
- **Keyboard-visible mockup**: `Not applicable`

### Out Of Scope For This Design

- Graphical drag-and-drop WYSIWYG diagram node editor.

---

## Screen 2 — Fullscreen Diagram Viewer Screen

### Purpose

Provide an expansive, immersive canvas for inspecting complex, large-scale Mermaid diagrams with unrestricted pan, smooth zooming (up to 400%), fit-to-screen centering, code copying, and SVG sharing/export.

### UX Principles

- **Uncluttered Focus**: Edge-to-edge canvas gives diagrams maximum screen real estate.
- **Precision Navigation**: Dedicated zoom overlay buttons (+, -, 100%, Fit) provide accessible precision navigation in addition to touch pinch/pan gestures.

### Entry And Exit

- **Entry points**: Tapping the Fullscreen icon (`editor_mermaid_fullscreen_btn_{id}`) on any Mermaid diagram card.
- **Primary success exit**: Tapping the Back arrow / Close button returns directly to the note editor.
- **Cancel/back behavior**: System Android back button or top bar back arrow dismisses the viewer.

### Information Architecture

1. **Top App Bar**:
   - Left: Back arrow icon (`textPrimary` `#191627`).
   - Center: Diagram title (18sp bold) and subtitle "Mermaid Diagram" (`textSecondary` `#7B7694`).
   - Right Actions: Reset Zoom icon, Share/Export SVG icon, Copy Code icon.
2. **Main Canvas**:
   - Edge-to-edge interactive SVG rendering surface on `#F8F7FF` background.
   - Smooth gesture-based pinch zoom (50% to 400%) and unrestricted 2D panning.
3. **Floating Bottom Zoom Controls**:
   - Floating pill overlay (`#FFFFFF` surface with subtle elevation and border `#E7E3F6`).
   - Zoom Out (-) button, Zoom Level indicator (e.g. "125%"), Zoom In (+) button, and "Fit to Screen" button.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| `FullscreenViewerTopBar` | Header bar with title and actions | Default | `fullscreen_diagram_top_bar` |
| `FullscreenViewerCanvas` | Edge-to-edge pannable/zoomable SVG surface | Rendered, Loading | `fullscreen_diagram_canvas` |
| `FullscreenZoomControls` | Floating zoom & fit action bar | Default, MinZoom, MaxZoom | `fullscreen_zoom_controls` |
| `FullscreenExportButton` | Share / export rendered SVG file | Default, Pressed | `fullscreen_export_btn` |
| `FullscreenCopyCodeButton`| Copy raw Mermaid code to clipboard | Default, Copied | `fullscreen_copy_code_btn` |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Loading | Centered progress spinner | Wait for full diagram load |
| Rendered | Full diagram centered with zoom controls | Pinch, pan, zoom in/out, fit to screen, copy code, share SVG |
| Max / Min Zoom | Zoom button disabled at boundaries | Pan, reset zoom, fit to screen |

### Interaction Rules

- **Pinch and Pan**: Smooth two-finger zoom and one-finger canvas drag.
- **Zoom In / Out**: Each tap on `+` / `-` adjusts scale by ±25%.
- **Fit to Screen**: Scales and centers the entire diagram within the current viewport.
- **Copy Code**: Copies Mermaid source DSL to Android clipboard with a brief confirmation snackbar.
- **Share / Export**: Exports the rendered SVG file via Android system share dialog (`Intent.ACTION_SEND`).

### Copy Requirements

| Element | Copy |
|---|---|
| Screen Subtitle | "Mermaid Flowchart" / "Mermaid Diagram" |
| Zoom In | "Zoom in" |
| Zoom Out | "Zoom out" |
| Fit to Screen | "Fit to Screen" |
| Code Copied Message | "Mermaid code copied to clipboard" |

### Accessibility

- All toolbar and floating controls have minimum 48×48dp touch targets and localized content descriptions.
- Screen readers announce diagram title and current zoom percentage.

### Design Assets

- **Mockup image**: `design/mockup_mermaid_fullscreen_viewer.png`
- **Keyboard-visible mockup**: `Not applicable`

### Out Of Scope For This Design

- Diagram node color re-theming from inside the fullscreen viewer.
