# Notes Taking App Design System

**Status**: Active project-wide UI contract  
**Last synchronized with code**: 2026-08-14  
**Applies to**: Requirements, UX design, mockups, Compose implementation, UI tests, visual verification, and review

---

## Purpose And Authority

This document is the shared visual source of truth for future product work. It is derived from the current Compose theme and note editor components so generated designs do not invent a new visual language for each feature.

Precedence for UI decisions:

1. Explicit user direction for the active task.
2. User-provided design or screenshot for the active task.
3. This project-wide design system.
4. Feature-local `design.md` decisions that extend, but do not silently contradict, this file.
5. Generic Material 3 defaults.

If a feature needs a new token or intentionally changes an existing pattern, document the exception in its `design.md`, obtain user approval, and update this file and the Compose token source in the same delivered change. Never silently invent colors, typography, shapes, or component behavior in a generated mockup.

## Code Sources

| Concern | Source of truth |
|---------|-----------------|
| Semantic app and editor colors | `app/src/main/java/com/example/notesapp/ui/theme/AppColors.kt` |
| Material theme wiring | `app/src/main/java/com/example/notesapp/ui/theme/Theme.kt` |
| Editor note action sheet | `app/src/main/java/com/example/notesapp/ui/editor/components/EditorNoteActionsSheet.kt` |
| Note editor screen | `app/src/main/java/com/example/notesapp/ui/editor/screen/NoteEditorScreen.kt` |

Code remains the runtime source of truth. This document is the design and workflow contract. If they diverge, stop UI work, reconcile them explicitly, and update both rather than choosing one silently.

## Product Visual Modes

### App Shell & Note Editor

- The application uses **Light Theme** as its primary visual identity.
- Clean light background (`#F8F7FF`) for home, notes list, folders, settings, and note editor screens.
- Clean white surfaces (`#FFFFFF`) for cards, dialogs, floating panels, and modal bottom sheets.
- Purple/Violet (`#7C6CF2`) is the primary brand accent and main action color, supported by light lavender (`#9B8CFF`) for secondary interactive accents.
- High-contrast dark text (`textPrimary`: `#191627`) with subtle card borders (`border`: `#E7E3F6`) and dividers (`divider`: `#E7EBF0`).
- Dark theme (`#121212` background, `#1E1E1E` surface, `#9B8CFF` primary) remains supported via system settings (`DarkAppColors`), but Light Theme is the visual baseline for all design contracts, mockups, and UI components.

## Color Tokens

Semantic tokens are defined in `AppColors` (`LightAppColors` & `DarkAppColors`) and accessed via `LocalAppColors.current`. Light theme values specify the primary app baseline.

### Primary & Surface Tokens

| Token | Light Hex | Dark Hex | Intended role |
|-------|-----------|----------|---------------|
| `primary` | `#7C6CF2` | `#9B8CFF` | Primary app actions, selected tabs, floating action buttons, primary branding. |
| `secondary` | `#9B8CFF` | `#7C6CF2` | Supporting interactive elements, secondary chips and badges. |
| `background` | `#F8F7FF` | `#121212` | Main screen background surface (Light theme baseline). |
| `surface` | `#FFFFFF` | `#1E1E1E` | Card containers, dialogs, bottom sheets, and elevated elements in light theme. |
| `textPrimary` | `#191627` | `#E1E1E1` | Main text titles, body text, and prominent headers on light surfaces. |
| `textSecondary` | `#7B7694` | `#B0B0B0` | Subtitles, metadata, timestamps, and supporting labels. |
| `textTertiary` | `#A0A6AC` | `#808080` | Placeholder text, disabled labels, and quiet hints. |
| `onPrimary` | `#FFFFFF` | `#121212` | Content rendered over `primary` surfaces. |
| `border` | `#E7E3F6` | `#333333` | Outlines, card borders, and input field strokes. |
| `divider` | `#E7EBF0` | `#2C2C2C` | Separator lines between list items and sections. |
| `error` | `#C44A4A` | `#CF6679` | Destructive actions, validation error text, and error states. |

### Accents & Highlights

| Token | Light Hex | Dark Hex | Intended role |
|-------|-----------|----------|---------------|
| `accentYellow` | `#FFD66B` | `#D4AF37` | Favorite items, star ratings, and warning indicators. |
| `accentPink` | `#FFBFD7` | `#D81B60` | Secondary soft accent highlights. |
| `accentMint` | `#C6F1E7` | `#00897B` | Success states, mint badges, and tag chips. |
| `accentBlue` | `#CFE1FF` | `#1976D2` | Soft blue highlights and informational chips. |
| `highlight` | `#F0F4FF` | `#2A2A3A` | Active note item highlight and selected background state. |
| `searchBackground` | `#EEEFF1` | `#2C2C2C` | Search field container background in light theme. |
| `searchIcon` | `#8E959B` | `#B0B0B0` | Search field icon tint. |

### Gradients & Special Tokens

| Token | Light Hex | Dark Hex | Intended role |
|-------|-----------|----------|---------------|
| `proBadgeStart` / `proBadgeEnd` | `#9B8CFF` → `#E06FD8` | `#7C6CF2` → `#C569E0` | Pro feature badge gradient. |
| `heroBannerStart` / `heroBannerEnd` | `#7C6CF2` → `#C569E0` | `#5A4EB3` → `#8E4AA3` | Dashboard hero banner gradient. |
| `roleOwnerBg` / `roleOwnerText` | `#EFF3FF` / `#4C6FFF` | `#1A237E` / `#82B1FF` | Owner role badge background & text in note sharing. |
| `roleEditorBg` / `roleEditorText` | `#F1EEFF` / `#6E4CFF` | `#311B92` / `#B388FF` | Editor role badge background & text in note sharing. |
| `roleViewerBg` / `roleViewerText` | `#FFF7ED` / `#F59E0B` | `#F57F17` / `#FFE57F` | Viewer role badge background & text in note sharing. |

### Color Rules

- Always access colors via `LocalAppColors.current.<token>`; never hardcode raw `Color(0x...)` hex values in `@Composable` functions.
- The default theme is **Light Theme**. The primary brand accent is **Vibrant Purple (`#7C6CF2`)**.
- Use `background` (`#F8F7FF`) for screen backgrounds and `surface` (`#FFFFFF`) for cards, elevated sheets, and dialogs.
- Text contrast must follow Material 3 accessibility standards: `textPrimary` (`#191627`) for high emphasis on light surfaces, `textSecondary` (`#7B7694`) for medium emphasis.
- Disabled interactive elements must use 38% alpha (`0.38f`) on text/icon content.
- State changes (selection, errors, roles) must be communicated by combining color with iconography, typography, or shape.

## Typography

- Font family: platform sans-serif through the existing Compose theme/components.
- Editor top action title / Screen header title: 24sp, bold, `textPrimary` (`#191627`).
- Editor control label: 14sp, semibold unless an established component requires otherwise.
- Editor supporting/status text: 12–13sp with sufficient line height.
- Editor tool label: 10sp, 11sp line height, semibold; selected label bold.
- Use Material typography roles for new app-shell screens and map them to the existing visual hierarchy.
- Support system font scaling without clipping; allow wrapping or scrolling where the component cannot expand horizontally.

## Spacing, Shape, And Touch

- Base spacing scale: 4dp, 8dp, 12dp, 16dp, 24dp, 32dp.
- Standard screen horizontal padding: 16dp on phones, 24dp on tablets unless an existing surface defines otherwise.
- All interactive targets: minimum 48×48dp.
- Existing editor tool tile: 78×60dp, 8dp corner radius, 6dp internal padding, 4dp icon/label gap, 22dp icon.
- Use rounded surfaces only when the existing component family uses them. Do not convert flat tool rails or overlays into modal bottom sheets/cards.
- Respect `WindowInsets.safeDrawing`, status bars, navigation bars, cutouts, and gesture insets.

## Component Contracts

### Top Action Bar / Header Bar

- Clean top header surface (`background` `#F8F7FF` / `surface` `#FFFFFF`) with back action, title, search button, and note action menu.
- Action icons and screen title inherit `textPrimary` (`#191627`) with 48×48dp accessible touch targets.
- Screen title uses `titleMedium` / `headlineMedium` semibold text in `textPrimary` (`#191627`).

### Note Action Sheet (`EditorNoteActionsSheet`)

- Modal bottom sheet (`surface` `#FFFFFF`) with rounded top corners (16dp).
- Options list (Move to folder, Add to favorites, Export note, Delete note) using standard row height (56dp) and `textPrimary` (`#191627`) labels.
- Destructive options (e.g. Delete) use `error` (`#C44A4A`) tint for icons and text.

### Search & Filter Controls

- Search input container uses `searchBackground` (`#EEEFF1` light theme) with rounded corners (12dp).
- Search icon uses `searchIcon` tint (`#8E959B`) with `textSecondary` (`#7B7694`) placeholder text.

### Overlays And Sheets

- Standard M3 `ModalBottomSheet` for action pickers and folder selectors, using `surface` (`#FFFFFF`).
- Rounded surface corners (16dp top radius for bottom sheets; 12dp for cards and dialogs).
- Respect `WindowInsets.safeDrawing`, status bars, navigation bars, and gesture insets.

## Accessibility And Testability

- Every interactive element has a localized label/content description and a stable `testTag`.
- Selected, disabled, loading, error, and value states are exposed through semantics.
- Body text contrast target: at least 4.5:1; large text/icons: at least 3:1.
- Minimum target size: 48×48dp, including icon-only actions.
- Support TalkBack traversal, keyboard focus, RTL, font scaling, narrow phones, landscape, and tablets.
- Never rely on gesture-only behavior when an accessible button/action alternative is practical.

## Workflow Requirements

For every UI-affecting task:

1. Read this file before requirements/design work.
2. Inspect the relevant existing Compose screen/components and theme tokens.
3. Cite this file in the feature `design.md` and identify any approved exception.
4. Include exact applicable tokens and component rules in every generated mockup prompt.
5. Before implementation, view the approved mockups and map their elements back to semantic tokens/components.
6. During verification, compare the implementation to both the approved feature design and this design system.
7. If code changes a reusable visual token or component contract, update this file in the same change.

## Mockup Prompt Baseline

Every AI-generated UI mockup prompt must include:

> Follow `docs/product/design_system.md` as the visual source of truth. Reuse its exact semantic colors, typography, spacing, shapes, opacity, and editor component patterns. Do not invent a new accent palette, glassmorphism, gradients, or component family. Any feature-specific exception must be stated explicitly in the approved `design.md`.

## Design-System Verification Checklist

- [ ] Feature `design.md` links to `docs/product/design_system.md`.
- [ ] Mockup uses exact relevant design-system tokens with no unexplained colors.
- [ ] Compose implementation uses `LocalAppColors`/shared components instead of raw values.
- [ ] Toolbars, overlays, sheets, sliders, typography, and spacing match established component contracts.
- [ ] Accessibility and test-tag requirements are specified and implemented.
- [ ] Visual verification reports any approved exceptions and any unintended deviations.
