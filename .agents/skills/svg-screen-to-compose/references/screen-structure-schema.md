# Screen structure JSON schema

Use this as the normalized intermediate representation between raw SVG and generated Compose UI.

## Goals

- preserve screen-level structure
- separate semantic UI from decorative fallback regions
- support deterministic downstream Compose generation
- make inspection/debugging easier than working from raw SVG directly

## Top-level shape

```json
{
  "version": 1,
  "source": {
    "type": "svg",
    "path": "input.svg"
  },
  "artboard": {
    "x": 0,
    "y": 0,
    "width": 360,
    "height": 800
  },
  "tokens": {
    "colors": [],
    "spacing": [],
    "cornerRadii": []
  },
  "regions": [],
  "components": [],
  "notes": []
}
```

## regions

Use `regions` for broad screen sections.

Example roles:

- `header`
- `hero`
- `content`
- `list`
- `footer`
- `bottom-nav`
- `floating-action`
- `background-decoration`

Example item:

```json
{
  "id": "region-hero",
  "role": "hero",
  "bounds": { "x": 24, "y": 96, "width": 312, "height": 140 },
  "children": ["component-card-hero"],
  "fallbackType": null
}
```

## components

Use `components` for inferred UI pieces.

Common fields:

```json
{
  "id": "component-title",
  "type": "text",
  "inferredRole": "screen-title",
  "bounds": { "x": 24, "y": 56, "width": 0, "height": 0 },
  "text": {
    "value": "Home",
    "fontSize": 24,
    "fontWeight": null
  },
  "style": {
    "fill": "#000000",
    "cornerRadius": null
  },
  "children": [],
  "repeatGroupId": null,
  "fallbackType": null,
  "confidence": 0.92
}
```

## Component types

Suggested `type` values:

- `text`
- `card`
- `button`
- `image`
- `icon`
- `list-row`
- `container`
- `bottom-nav`
- `input`
- `divider`
- `unknown-decoration`

## inferredRole

Suggested `inferredRole` values:

- `screen-title`
- `section-title`
- `body-text`
- `caption`
- `hero-card`
- `primary-action`
- `secondary-action`
- `list-item`
- `nav-item`
- `background`
- `decorative-asset`

## fallbackType

Use when a component or region should remain an asset rather than be rebuilt semantically.

Suggested values:

- `vector-asset`
- `image-asset`
- `manual-rebuild-needed`
- `none`

## tokens

Use tokens to capture extracted reusable visual values.

```json
{
  "colors": ["#FFFFFF", "#F4F4F4", "#111111"],
  "spacing": [8, 12, 16, 24],
  "cornerRadii": [12, 16]
}
```

## notes

Use `notes` for uncertainty and limitations.

Examples:

- `Text outlines detected; text roles inferred visually.`
- `Large decorative region kept as vector asset.`
- `Repeated cards converted into one repeat group.`
