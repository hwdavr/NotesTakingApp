# Shared JSON scenarios skill

Use this skill when defining or consuming cross-platform test scenarios shared by Android and iOS.

## Purpose
A shared JSON scenario is the contract for:
- API mocking
- expected domain behavior
- expected UI behavior

## Recommended shape
Each scenario should contain:
- id
- description
- apiMocks
- expected.domain
- expected.ui
- tags

## Rule by layer
- integration tests should consume expected.domain
- instrumented UI tests should consume expected.ui
- do not force every UI detail into the shared contract
- keep expectations logical, not pixel-perfect

## Good shared fields
- itemCount
- statusLabel
- actionEnabled
- selectedId
- destinationScreen
- emptyStateVisible

## Avoid in shared contract
- exact color values
- exact spacing
- platform-specific widget classes
- fragile UI hierarchy assumptions
