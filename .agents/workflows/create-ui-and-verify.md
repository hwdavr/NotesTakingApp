# Create Android UI from screenshot and verify it

You are working in an Android repository.

## Goal
Implement or update the Android UI to match the provided screenshot as closely as practical while following the repository’s existing architecture and UI patterns.

## Required workflow

### 1. Inspect existing code first
- Find the target screen or the closest existing screen/component.
- Reuse existing UI components, themes, styles, and architecture.
- Do not introduce a new UI framework if the target screen already uses another one.
- If the target screen is View/XML based, keep it View/XML based unless explicitly instructed otherwise.
- If the target screen is Compose based, keep it Compose based.

### 2. Check for existing instrumented/UI tests
- Search for existing Android instrumented UI tests for the target screen or nearest related flow.
- If an existing test already covers the screen or flow:
  - update and reuse that test
  - do not create a duplicate test
- If no suitable test exists:
  - create a new instrumented UI test using the appropriate skill
  - prefer Kaspresso if the repository already uses it

### 3. Choose the right verification layer
- For visible UI rendering and navigation, use Android instrumented UI tests.
- Do not move detailed business logic checks into UI tests.
- If backend-driven states are needed, use mocked/stubbed data.
- Prefer existing test hooks, fake repositories, DI overrides, or MockWebServer already used in the repo.

### 4. Implement the UI
- Build the screen to match the provided screenshot.
- Reuse existing design system components where possible.
- Add stable selectors for testing:
  - View/XML: stable ids where appropriate
  - Compose: stable testTag where appropriate
- Avoid brittle selectors based only on visible text if stronger selectors can be added.

### 5. Add or update the UI test
- If an existing test exists, update it to verify the new UI.
- Otherwise create a new thin instrumented UI test that verifies:
  - screen is shown
  - key content is rendered
  - key CTA/buttons are shown
  - important states are rendered correctly
- Keep the test focused on user-visible behavior.

### 6. Capture screenshot for verification
After implementing the screen, install the app on the emulator and capture a real screenshot:

#### 6a. Install and navigate to the screen
1. Run `./gradlew installDebug` to install the latest build.
2. Launch the app: `adb shell am start -n <package>/<activity>`.
3. Wait for the app to load (`sleep 3` or more).
4. Use `adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml /tmp/ui.xml` to inspect the current screen.
5. Find the tap target coordinates: `grep -oP 'text="[^"]+"[^/]+bounds="[^"]+"' /tmp/ui.xml` or `grep -oP 'text="<label>"[^/]+/>' /tmp/ui.xml`.
6. Tap the correct navigation element: `adb shell input tap <x> <y>`.
7. Wait (`sleep 2`) then capture: `adb exec-out screencap -p > screenshot.png`.

#### 6b. Verify all texts match the design exactly
- Use `adb shell uiautomator dump` and grep to extract all visible text nodes:
  ```
  adb shell uiautomator dump /sdcard/screen.xml && adb pull /sdcard/screen.xml /tmp/screen.xml
  grep -oP 'text="[^"]+"' /tmp/screen.xml | grep -v 'text=""'
  ```
- Compare every text string against the design screenshot — **all copies must be identical**.
- Scroll if needed (`adb shell input swipe 540 1200 540 400`) and dump again to verify off-screen items.
- Use `tesseract <design.png> stdout` if available for OCR comparison against the design image.

#### 6c. Acceptance criteria
- Every text string visible in the design screenshot is present in the UI (on-screen or accessible by scroll).
- No text string in the UI differs from the design in wording, casing, or punctuation.
- Layout structure matches the design (hero card, section order, row groupings).

### 7. Report result
Return:
1. files created or updated
2. whether an existing test was reused or a new one was created
3. which skills were used
4. what was verified by instrumented UI test
5. whether screenshot verification passed or where differences remain
6. any stable selectors/test tags/ids that were added

## Rules
- Prefer reuse over rewrite.
- Prefer existing tests over new duplicate tests.
- Prefer Android-native UI verification over Appium.
- Use Appium only if explicitly required for a top-level smoke path.
- Do not use Thread.sleep in tests.
- Use `performScrollTo()` to assert items that are below the visible area in scrollable lists.
- If a text string appears more than once on the screen, use `onAllNodesWithText()[index]` to avoid ambiguous selector errors.
- Keep one main UI scenario per test.
- Match repository style and architecture.
- Before writing instrumented tests, verify that `androidTestImplementation` dependencies for `ui-test-junit4` and `junit` are present in `app/build.gradle.kts`.

## If repository context is incomplete
- inspect nearby screens, tests, screen objects, and helpers first
- make the smallest safe assumption
- keep code buildable and consistent with the repo