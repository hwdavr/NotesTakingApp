# SVG screen to Compose inference guide

## Goal

Infer editable Compose UI structure from a design-oriented SVG without overfitting to raw coordinates.

## High-level method

1. Detect the artboard bounds.
2. Collect visible elements by type: text, path, rect, image, circle, group.
3. Sort by visual layering and coordinates.
4. Cluster elements into screen regions.
5. Identify repeated structures.
6. Infer semantic roles.
7. Reconstruct the UI with Compose layout primitives.
8. Mark non-semantic artwork as fallback assets.

## Useful clues

### Containers

Likely containers often appear as:

- large rects with background fills
- rounded rects behind a cluster of text/icons
- padded shapes containing aligned children
- repeated same-style boxes in a list or grid

### Rows

Likely rows often show:

- multiple children sharing a common vertical center
- left-to-right ordering
- stable gaps between siblings
- repeated pattern across several y positions

### Columns

Likely columns often show:

- multiple children sharing a common left or horizontal center
- top-to-bottom flow
- repeating vertical gaps

### Buttons and chips

Common signs:

- rounded container plus centered text
- icon plus label inside a compact shape
- repeated action controls with matching height

### Text hierarchy

Infer tentatively from:

- font size differences
- weight differences
- color contrast
- position within a container
- distance from surrounding elements

Typical mapping:

- largest prominent text -> title/headline
- medium bold text -> section heading or button label
- smaller muted text -> secondary text/caption

## When to use Box

Use `Box` when:

- elements overlap meaningfully
- badges float over cards or images
- background art sits behind semantic content
- exact z-order matters

But avoid using `Box` for everything. Default to Row/Column when normal structure exists.

## Repetition detection

Treat a group as repeatable when several siblings have:

- similar width and height
- matching child count or arrangement
- same text/icon positions relative to the container
- same style tokens

Then extract a composable rather than duplicating blocks.

## Ambiguity handling

When uncertain, prefer maintainability:

- pick a sane spacing scale
- keep text editable
- use placeholders for unknown images
- call out assumptions explicitly

## Warning signs

Do not overclaim semantic certainty if the SVG contains:

- all text converted to outlines
- no grouping metadata
- many overlapping decorative paths
- absolute-positioned everything with no obvious layout rhythm
- imported design exports with heavy flattening

In those cases, provide a mixed result: semantic Compose for what is clear, fallback assets for what is not.
