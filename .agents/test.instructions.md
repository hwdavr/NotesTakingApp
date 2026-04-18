---
applyTo: "app/src/test/**"
---

# JVM unit and integration test instructions

## Purpose
This folder is the main logic verification layer.

## Use for
- ViewModel tests
- use case tests
- mapper tests
- reducer / UiState mapping tests
- repository + MockWebServer tests
- API success and error handling
- Room or cache behavior if configured here
- data-layer integration without real UI

## Shared JSON scenario usage
When a shared scenario file exists:
- load apiMocks for stubbing
- assert expected.domain
- keep assertions at logic/state level, not UI level

## Must cover when relevant
- success response
- 4xx and 5xx response handling
- malformed payload
- empty body or partial payload
- network disconnect / timeout
- unknown enum / fallback logic
- retry or fallback behavior

## Rules
- no UI assertions
- deterministic only
- one main scenario per test
- prefer this layer before adding UI tests