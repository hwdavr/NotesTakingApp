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
- after installing the app on an emulator, navigate to the target screen using `adb shell uiautomator dump` to find the correct tab/button coordinates, then tap to navigate
- capture: `adb exec-out screencap -p > screenshot.png`
- verify all texts using `adb shell uiautomator dump` + `grep -oP 'text="[^"]+"'` — compare every string against the design
- scroll to reveal off-screen items (`adb shell input swipe`) and dump again if the list is long
- use `tesseract <design.png> stdout` for OCR comparison if available
- recommend snapshot verification for layout-level regressions

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

## Common pitfalls
- Capturing a screenshot before the app has fully loaded or before navigating to the target screen — always use `uiautomator dump` to confirm you are on the right screen first
- Assuming `onNodeWithText()` is unambiguous when the same text appears in multiple places — use `onAllNodesWithText()[index]` when duplicates exist
- Missing `androidTestImplementation` dependencies (`ui-test-junit4`, `junit`, `espresso-core`) — verify in `app/build.gradle.kts` before writing tests
