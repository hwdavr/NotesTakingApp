# Android UI verification skill

Use this skill after AI generates or modifies Android UI code.

## Goal
Verify UI changes using the cheapest reliable checks first, then higher-confidence UI checks.

## Verification order
1. build and static checks
- compile
- lint
- relevant unit tests

2. Android-native UI behavior verification
- run instrumented UI integration tests for the changed screen or flow
- use mocked or stubbed data
- verify user-visible behavior

3. visual verification
- recommend screenshot or snapshot verification when layout or presentation changed

4. thin outer smoke
- Appium only for minimal packaged-app smoke if required

## Preferred main verifier
The main verifier for AI-generated Android UI changes is Android-native instrumented UI integration testing.

## What to verify
- screen shown
- no crash
- correct content rendering from mocked data
- CTA visible and enabled/disabled correctly
- loading / empty / error / success states
- navigation destination or destination content
- major layout sanity when applicable

## What not to rely on alone
- Appium alone
- screenshot tests alone
- manual verification alone
- unit tests alone
