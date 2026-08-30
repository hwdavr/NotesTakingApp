# Feature Design — Code Block in Note Editor

**Date**: 2026-08-18  
**Status**: Approved  
**Source request**: Add code block to note editor with language selection, monospace editing, copy action, and advanced panel section  
**Related spec**: `spec.md`  
**Project design system**: `docs/product/design_system.md`  
**Approved design-system exceptions**: One — the Code Block adds a dedicated `code*` syntax token family (`codeKeyword`, `codeType`, `codeString`, `codeComment`, `codeNumber`, `codeOperator`) to `AppColors.kt` and `docs/product/design_system.md` so syntax highlighting has an accessible, theme-adaptive palette instead of reusing unrelated semantic tokens. All other tokens, colors, typography, shapes, and touch targets strictly adhere to `docs/product/design_system.md`.  

---

## Conditional Keyboard-Visible Mockup Contract

Not applicable. The Code Block feature renders inline inside the Note Editor content area and the Basic Blocks panel is an attached bottom tile panel; it does not introduce any bottom modal text-input surface.

---

## Screens Covered

| # | Screen / Surface | Status |
|---|---|---|
| 1 | Note Editor — Code Block Component | New Component |
| 2 | Note Editor — Basic Blocks Panel (Advanced Section) | Updated Surface |

---

## Screen 1 — Note Editor — Code Block Component

### Purpose

Provide a clean, elevated monospace code editing block inside the Note Editor where users can write and view syntax-highlighted code snippets across multiple programming languages, view synchronized line numbers, change the language tag via a dropdown menu, and copy snippet text directly to the clipboard with one tap.

### UX Principles

- **Readability First**: High-contrast syntax tokens on a clean card container (`#FFFFFF` in light mode, `#1E1E1E` in dark mode) ensure code snippets stand out from regular prose while maintaining visual harmony with the note document.
- **Effortless Code Utility**: Dedicated one-tap "Copy Code" button and language picker chip eliminate friction when referencing or sharing code on mobile devices.
- **Lightweight Responsiveness**: Instant regex-based client-side syntax highlighting ensures smooth 60fps typing performance without frame drops or lag.

### Entry And Exit

- **Entry points**:
  - User opens a note containing a Code Block.
  - User taps the "Code" tile in the Basic Blocks panel.
- **Primary success exit**: Code is edited and automatically persisted in the note document.
- **Cancel/back behavior**: Tapping outside the block or navigating back preserves the block and its content.
- **Failure exit or recovery**: If code is empty, placeholder guidance is displayed.

### Information Architecture

1. **Card Container**: Elevated white surface (`#FFFFFF` in light theme, `#1E1E1E` in dark theme), subtle border stroke (`#E7E3F6`), 12dp rounded corners.
2. **Card Header**:
   - Left: Language selector pill chip/button (`#F1EEFF` background, `#7C6CF2` text in Light theme) displaying current language name (e.g. "Kotlin") with a dropdown chevron icon. Tapping opens a Material 3 dropdown menu with supported languages.
   - Right: Action buttons row containing:
     - "Copy Code" button (`#191627` icon / button, 48×48dp touch target) that copies code to the clipboard and briefly displays a checkmark confirmation.
     - "Delete Block" icon button (`Icons.Outlined.Delete` or trash can icon, visible when `isEditable = true`, 48×48dp touch target) that removes the code block from the document.
3. **Card Body (Code Area)**:
   - Left: Line numbers column in a dedicated gutter (`textSecondary` `#7B7694` / `textTertiary` `#A0A6AC`, `FontFamily.Monospace`, 13sp), dynamically numbered `1, 2, 3...`.
   - Right: Multi-line monospace text editing area (`FontFamily.Monospace`, 13sp, 20sp line height) with real-time syntax highlighting for keywords, strings, comments, numbers, and types.

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| `CodeBlockCard` | Elevated card container for the code block | Default, Focused | `editor_code_block_{id}` |
| `CodeBlockHeader` | Header bar containing language selector, copy button, and delete button | Default, ReadOnly | `editor_code_header_{id}` |
| `CodeBlockLanguageSelector` | Badge chip button that opens language menu | Default, Expanded | `editor_code_lang_selector_{id}` |
| `CodeBlockLanguageDropdown` | Dropdown menu listing supported programming languages | Open, Closed | `editor_code_lang_dropdown_{id}` |
| `CodeBlockLanguageItem_{lang}` | Individual language menu item | Normal, Selected | `editor_code_lang_item_{lang}` |
| `CodeBlockCopyButton` | One-tap button to copy code content to clipboard | Default, Copied | `editor_code_copy_btn_{id}` |
| `CodeBlockDeleteButton` | One-tap icon button to remove code block from note | Default, Hidden in ReadOnly | `editor_code_delete_btn_{id}` |
| `CodeBlockReadOnlyIndicator` | Marks a code block as read-only when editing is unavailable | ReadOnly | `editor_code_readonly_{id}` |
| `CodeBlockLineNumbers` | Left gutter displaying line numbers | Default | `editor_code_line_numbers_{id}` |
| `CodeBlockTextEditor` | Monospace text field with syntax highlighting | Focused, Unfocused, ReadOnly | `editor_code_editor_{id}` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Code Block card horizontal alignment | `editor_code_block_{id}`, `note_editor_content` | `card.left == content.left + 16dp`, `card.right == content.right - 16dp` |
| Language chip and header actions vertical alignment | `editor_code_lang_selector_{id}`, `editor_code_copy_btn_{id}`, `editor_code_delete_btn_{id}` | `langSelector.centerY == copyBtn.centerY == deleteBtn.centerY` |
| Line numbers gutter alignment with code | `editor_code_line_numbers_{id}`, `editor_code_editor_{id}` | `lineNumbers.top == codeEditor.top` |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Empty Code Block | Card with language chip ("Kotlin"), Copy button, Delete button, line `1`, and placeholder text `"// Enter code here..."` | Tap to focus editor, change language, delete block |
| Content / Editing | Monospace syntax-colored code, active cursor, line numbers gutter, Copy & Delete buttons | Type code, change language, copy code, delete block |
| Copied State | Copy button transitions to checkmark icon with visual confirmation | Continue editing or viewing |
| Read-Only Mode | Highlighted code with line numbers, active Copy button, Delete button hidden, disabled editing | Scroll, select code, copy code |

### Interaction Rules

- **Language selection**: Tapping the language badge opens a dropdown menu; selecting a language immediately updates the syntax coloring and persists the change.
- **Copy Code action**: Tapping the Copy button copies code to clipboard; button icon temporarily changes from `ContentCopy` to `Check` for 1.5 seconds.
- **Delete action**: Tapping the Delete button (`editor_code_delete_btn_{id}`) immediately removes the Code Block from the note document, returns focus to the adjacent block, and auto-saves the document.
- **Line numbering**: Typing a newline automatically increments line numbers; deleting lines decrements line numbers.
- **Auto-save**: Every code or language edit dispatches debounced document persistence.

### Copy Requirements

| Element | Copy |
|---|---|
| Default Language | "Plain Text" (or "Kotlin") |
| Copy Action Tooltip / Description | "Copy code" |
| Copied Confirmation | "Code copied to clipboard" |
| Placeholder Text | "// Enter code here..." |
| Supported Language Labels | "Kotlin", "Java", "Python", "JavaScript", "TypeScript", "HTML", "CSS", "JSON", "SQL", "Shell", "C/C++", "Rust", "Go", "Plain Text" |

### Accessibility

- Language selector chip has content description: `"Code language: %s, tap to change"`.
- Copy button has accessible role `Button` and content description: `"Copy code to clipboard"`.
- Monospace editor provides full keyboard navigation, text selection, and screen reader TalkBack accessibility.
- Minimum 48×48dp accessible touch target on all interactive controls.

### Responsive And Configuration Behavior

- Full-width adaptive card layout respecting phone (16dp margin) and tablet (24dp margin) horizontal padding.
- Preserves cursor position, line scroll position, and language selection across device orientation changes.

### Design Assets

- **Mockup image**: `design/mockup_code_block_editor.png` — Visual mockup showing the Code Block card with language selector chip, Copy button, line numbers gutter, and syntax highlighted Kotlin code.
- **Keyboard-visible mockup**: `Not applicable`

### Out Of Scope For This Design

- Live code compiler / execution runner.

---

## Screen 2 — Note Editor — Basic Blocks Panel (Advanced Section)

### Purpose

Reorganize the Basic Blocks catalog panel into structured "Basic" and "Advanced" categories so users can easily distinguish foundational text formatting blocks from specialized modular blocks (Code Block and Mermaid Diagram).

### UX Principles

- **Clear Scannability**: Section headers group blocks logically, preventing visual clutter as the catalog expands.
- **Consistent Tile Geometry**: All tiles maintain uniform 48dp minimum touch targets, 8dp rounded corner borders, and accessible semantics.

### Entry And Exit

- **Entry points**: Tapping the "+" / Basic Blocks icon on the note editor bottom bar.
- **Primary success exit**: Tapping "Code" inserts an empty Code Block and auto-collapses the panel.
- **Cancel/back behavior**: Tapping outside or system Back collapses the panel.

### Information Architecture

1. **Panel Surface**: White background (`#FFFFFF`), top border (`#E7E3F6`), capped height `min(280dp, 40% screen height)`.
2. **"Basic" Section**:
   - Header label: "Basic" (`textPrimary` `#191627`, 14sp semibold).
   - 2-column grid tiles: Text, Heading 1, Heading 2, Heading 3, Heading 4, Bullet list, Number list, To-do list, Toggle list, Callout, Quote (full span).
3. **"Advanced" Section**:
   - Header label: "Advanced" (`textPrimary` `#191627`, 14sp semibold).
   - 2-column grid tiles:
     - **Code**: Code icon (`Icons.Outlined.Code`), label "Code", description "Code block with syntax highlighting".
     - **Mermaid Diagram**: Diagram icon (`Icons.Outlined.AutoAwesomeMosaic`), label "Mermaid Diagram", description "Mermaid diagram and chart".

### Component Inventory

| Component | Purpose | Required States | Test Tag |
|---|---|---|---|
| `BasicBlocksPanel` | Root panel container | Visible, Collapsed | `basic_blocks_panel` |
| `BasicBlocksSectionHeader_Basic` | Header for basic text blocks | Default | `basic_blocks_section_basic` |
| `BasicBlocksSectionHeader_Advanced` | Header for advanced blocks | Default | `basic_blocks_section_advanced` |
| `BasicBlockTile_Code` | Tile button for inserting Code block | Default, Pressed | `basic_blocks_code` |
| `BasicBlockTile_Mermaid` | Tile button for inserting Mermaid diagram | Default, Pressed | `basic_blocks_mermaid` |

### Reference Anchor Contract

| Reference relationship | Visual bounds node(s) | Required runtime measurement |
|---|---|---|
| Panel attached above editor bottom bar | `basic_blocks_panel`, `editor_bottom_bar` | `panel.bottom == bottomBar.top` |
| Advanced section header below basic tiles | `basic_blocks_section_advanced`, `basic_blocks_quote` | `advancedHeader.top > quoteTile.bottom` |

### Visual States

| State | User Sees | User Can Do |
|---|---|---|
| Panel Open | "Basic" section with text/list tiles and "Advanced" section with Code and Mermaid tiles | Scroll catalog, tap tile to insert, tap outside to collapse |
| Tile Tap | Brief ripple/press feedback on selected tile | Block is inserted and panel dismisses |

### Copy Requirements

| Element | Copy |
|---|---|
| Section 1 Header | "Basic" |
| Section 2 Header | "Advanced" |
| Code Tile Label | "Code" |
| Code Tile Description | "Code block with syntax highlighting" |
| Mermaid Tile Label | "Mermaid Diagram" |
| Mermaid Tile Description | "Mermaid diagram and chart" |

### Accessibility

- Section headers marked with semantic heading role where applicable.
- Each tile provides full `contentDescription` and `Role.Button`.
- Touch target minimum 48×48dp.

### Design Assets

- **Mockup image**: `design/mockup_basic_blocks_panel_advanced.png` — Visual mockup showing the open Basic Blocks panel with "Basic" and "Advanced" section headers and the Code tile.
- **Keyboard-visible mockup**: `Not applicable`

### Out Of Scope For This Design

- Custom user-created block plugins or third-party extension marketplaces.
