# Screen Design — <Screen Name>

**Date**: YYYY-MM-DD
**Status**: Draft / Final
**Source request**: <short reference to the user request>
**Related spec**: `docs/current/spec.md`

---

## Purpose

<What this screen helps the user accomplish and why it exists.>

## UX Principles

- <principle that guides layout and behavior>
- <principle that guides interaction or density>

## Entry And Exit

- **Entry points**: <where this screen is opened from>
- **Primary success exit**: <where the user goes after success>
- **Cancel/back behavior**: <exact behavior>
- **Failure exit or recovery**: <exact behavior>

## Information Architecture

Describe the screen from top to bottom.

1. **<Region name>**: <content and purpose>
2. **<Region name>**: <content and purpose>
3. **<Region name>**: <content and purpose>

## Component Inventory

| Component | Purpose | Required States | Test Tag |
|-----------|---------|-----------------|----------|
| <component> | <purpose> | <default/loading/selected/disabled/error/etc.> | `<tag>` |

## Visual States

| State | User Sees | User Can Do |
|-------|-----------|-------------|
| Loading | <visual behavior> | <allowed actions> |
| Empty | <visual behavior> | <allowed actions> |
| Content | <visual behavior> | <allowed actions> |
| Error | <visual behavior> | <allowed actions> |

## Interaction Rules

- **Primary action**: <trigger and result>
- **Secondary actions**: <trigger and result>
- **Validation**: <inline errors, blocking rules, disabled states>
- **Destructive actions**: <confirmation and recovery behavior>
- **Gestures**: <supported gestures and non-gesture alternatives>

## Copy Requirements

| Element | Copy |
|---------|------|
| Title | <exact text> |
| Primary action | <exact text> |
| Empty state | <exact text> |
| Error state | <exact text> |

## Accessibility

- <content descriptions, roles, focus order, dynamic type, contrast, minimum touch target>

## Responsive And Configuration Behavior

- <portrait/landscape/tablet behavior>
- <configuration-change survival expectations>

## Design Assets

- <paths to screenshots/mockups saved under docs/current/design/, or "None">

## Out Of Scope For This Design

- <explicit non-goal>
