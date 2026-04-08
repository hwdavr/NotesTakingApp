---
name: svg-screen-to-compose
description: Convert full-screen or multi-section SVG design files into Jetpack Compose UI code. Use when a user wants an SVG mockup, artboard, landing page, mobile screen, dashboard, or exported design screen turned into Compose layout code with inferred structure such as Box/Row/Column, spacing, typography, cards, buttons, and vector/image fallbacks for purely decorative regions.
---

# Svg Screen To Compose

## Overview

Turn a whole SVG design into Compose UI, not just vector paths. Infer semantic layout where possible, preserve visual hierarchy, and use vector or image fallback only for parts that are too decorative or too ambiguous to represent cleanly as normal Compose layout.

## Primary goal

Produce screen-level Compose code that a developer can continue editing. Prefer maintainable UI structure over blindly translating every SVG node into an unreadable wall of drawing instructions.

## Workflow

1. Inspect the SVG as a full-screen design, not as an icon.
2. Segment the design into likely layout regions.
3. Infer semantic UI components where confidence is reasonable.
4. Separate layout from decoration.
5. Generate Compose UI for structure and reusable components.
6. Use vector/image fallback for complex artwork or impossible-to-infer regions.
7. Return assumptions, ambiguities, and suggested cleanup steps.

## Core rule

Do not translate the whole screen into one giant custom drawing unless the user explicitly wants a pure visual clone. Default to editable Compose UI.

## What to infer from the SVG

Try to infer these from geometry, grouping, repeated styles, alignment, spacing, and text placement:

- screen bounds and safe padding
- top bars, headers, footers, tab bars, and bottom navigation
- cards, panels, list rows, chips, and buttons
- avatar/image placeholders
- text hierarchy: title, subtitle, body, caption, label
- input-like regions and call-to-action blocks
- repeated items that should become data-driven composables
- backgrounds, shadows, separators, and decorative illustrations

## Layout reconstruction strategy

### Prefer semantic Compose primitives

Use normal Compose layout first:

- `Box` for overlapping layers
- `Column` for vertically stacked groups
- `Row` for horizontal groups
- `LazyColumn` / `LazyRow` when the design clearly represents repeated scrollable items
- `Text`, `Button`, `Icon`, `Image`, `Card`, `Divider`, `Spacer`

### Infer grouping heuristics

Treat elements as belonging together when they have combinations of:

- close spatial proximity
- aligned edges or centers
- consistent spacing intervals
- shared fill/stroke/text styles
- common background container
- repeated structure across siblings

### Infer repetition

When 3 or more sibling regions share nearly identical geometry and structure, prefer a reusable item composable rather than duplicating code.

Example output direction:

- `DashboardCard(...)`
- `MenuRow(...)`
- `FeatureItem(...)`

## Separate layout from decoration

### Rebuild as Compose layout when the region is mostly

- rectangles with spacing and alignment
- text blocks
- buttons or chips
- cards and simple panels
- simple icons
- profile rows, settings rows, feed items, stat tiles

### Keep as vector/image fallback when the region is mostly

- complex freeform illustration
- dense background ornament
- many layered bezier paths with no semantic meaning
- complex shadows, masks, or filters
- shape-heavy hero artwork that would be wasteful to hand-rebuild as layout

For such regions, say clearly that the output uses a fallback asset and identify where the fallback belongs in the Compose hierarchy.

## Output expectations

Always return:

1. **Screen structure summary**
   - what major sections were inferred
2. **Compose code**
   - screen-level composable
   - extracted subcomposables when useful
3. **Fallback assets summary**
   - which SVG regions should remain vector/image assets
4. **Assumptions and ambiguities**
   - anything uncertain about spacing, font, interaction, or semantics
5. **Next-step cleanup suggestions**
   - how a developer should refine the generated UI

## Code quality rules

- Prefer readable Kotlin over exact but unreadable visual cloning.
- Use modifiers for spacing, sizing, clipping, background, and alignment.
- Name composables by role, not by coordinates.
- Extract repeated UI into smaller composables.
- If the design obviously matches Material concepts, use Material components naturally.
- If typography is unclear, provide sensible placeholder text styles and call them out.
- If colors can be extracted confidently, use them; otherwise centralize them in constants.

## Fallback policy

When exact semantic reconstruction is weak, use this descending preference order:

1. semantic Compose layout
2. Compose layout plus embedded vector asset for decorative fragments
3. image/vector placeholder with explicit note
4. only as last resort, a large custom drawing block

## Handling text

- Preserve visible text content if it exists in the SVG.
- Infer likely text roles based on size, weight, placement, and grouping.
- If the SVG converts text to outlines, state that text semantics were inferred visually and may be wrong.
- If fonts are unknown, use reasonable Compose typography placeholders and note the mismatch.

## Handling constraints and responsiveness

Assume the SVG is a fixed design reference, then map it into a Compose layout that can still stretch reasonably.

- use dp-based spacing inferred from the artboard
- avoid absolute positioning unless overlap is essential
- prefer alignment and arrangement over coordinate placement
- if a section is truly free-positioned, isolate it inside a `Box`

## Recommended response pattern

Use this structure:

### 1. Inferred structure

Summarize the screen in plain language.

### 2. Compose implementation

Return the main screen composable and extracted children.

### 3. Fallback regions

List any pieces that should stay vector/image based.

### 4. Notes

List limitations and likely manual fixes.

## Concrete examples

### Example: mobile app home screen SVG

If the user says:

- `Convert this exported mobile screen SVG into Compose UI`

Then try to infer:

- top app bar
- hero card
- action buttons
- section headings
- repeated content rows
- bottom navigation

Return a full screen composable with extracted reusable child composables.

### Example: dashboard SVG

If the user says:

- `Turn this dashboard artboard into Jetpack Compose`

Then identify:

- page scaffold
- side/top navigation
- KPI cards
- charts or chart placeholders
- filters and action areas

If the chart is just decorative SVG, keep it as an asset placeholder and reconstruct the surrounding dashboard layout in Compose.

### Example: marketing landing page SVG

If the user says:

- `Convert this landing page SVG to Compose`

Reconstruct the broad layout and call-to-action sections semantically, but preserve complex hero illustrations as vector/image fallback where necessary.

## Supporting resources

- Read `references/inference-guide.md` for heuristics on turning SVG geometry into Compose layout.
- Read `references/screen-structure-schema.md` for the normalized JSON intermediate representation.
- Use `scripts/analyze_svg_screen.py` for a quick structural summary of an SVG file before generating code.
- Use `scripts/export_screen_structure.py` to export a normalized screen-structure JSON file.
- Use `scripts/generate_compose_screen.py` to produce starter screen-level Compose code directly from simpler exported SVG layouts.
- Use `scripts/generate_compose_from_structure.py` to generate Compose from the normalized JSON structure.
- If the task narrows to pure vector conversion for a subregion or icon asset, the lower-level `svg-to-compose` skill is a good companion.

## Practical execution pattern

When asked to convert a screen SVG, prefer this sequence:

1. Run `scripts/analyze_svg_screen.py` on the SVG.
2. Run `scripts/export_screen_structure.py` to produce a normalized JSON intermediate file whenever the task is more than trivial.
3. Inspect whether the screen looks mostly layout-driven or illustration-driven.
4. If it is mostly layout-driven, either:
   - run `scripts/generate_compose_screen.py` for direct SVG-to-starter-code generation, or
   - run `scripts/generate_compose_from_structure.py` for the cleaner JSON-to-Compose path.
5. Use the JSON structure as the source of truth when refining semantics, naming, component extraction, spacing, typography, and fallback handling.
6. If specific subregions are vector-heavy, use the separate `svg-to-compose` skill for those pieces.

## JSON export

Use this when the user wants a reusable machine-readable structure for the screen.

Example:

```bash
python3 scripts/export_screen_structure.py screen.svg --out screen-structure.json
```

The JSON should be treated as an intermediate representation for inspection, debugging, and downstream Compose generation.

Generate Compose from that JSON with:

```bash
python3 scripts/generate_compose_from_structure.py screen-structure.json --out GeneratedScreen.kt
```

## Generator caveats

`scripts/generate_compose_screen.py` is intentionally a starter generator, not a full fidelity compiler. It currently works best when the SVG is an exported screen with clear:

- background rectangles
- card-like containers
- visible text nodes
- repeated rows or tiles
- simple top-level screen structure

It is weaker when the SVG is heavily flattened, outline-based, or illustration-dominant. In those cases, use mixed reconstruction: semantic Compose for the obvious parts and vector/image fallback for the rest.
