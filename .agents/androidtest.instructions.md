---
applyTo: "app/src/androidTest/**"
---

# Android instrumented test instructions

## Purpose
This folder verifies user-visible behavior on Android runtime.

## Use for
- screen rendering from mocked data
- loading / empty / error / success states
- button click and visible effects
- navigation
- critical multi-screen flows

## Shared JSON scenario usage
When a shared scenario file exists:
- use apiMocks to control inputs
- assert expected.ui for visible outcomes

## Rules
- prefer Kaspresso if already used in the repository
- use Given / When / Then through step blocks
- use Screen/Page Object abstractions
- do not use Thread.sleep
- mocked data only
- do not put the full API error matrix here
- keep one main business scenario per test

## Good assertions
- screen shown
- list or content rendered
- button visible or enabled
- empty/error state shown
- destination screen shown after click

## Kaspresso instructions

- extend the common base UI test class if available
- use step { } blocks
- keep selectors in Screen objects
- keep assertions business-readable
- use stable selectors only
- no Thread.sleep
- one business scenario per test
- prefer fixture-driven or mock-server-driven setup
