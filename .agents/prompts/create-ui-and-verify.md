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
- After implementing the screen and test, take a screenshot of the rendered UI in the test environment.
- Compare it against the provided screenshot at a practical engineering level:
  - layout structure
  - key text blocks
  - major spacing/alignment
  - button placement
  - major visual differences
- Make sure all the elements in the screenshot are visible in the UI.
- Make sure all the copies/texts are exactly same as the screenshot.

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
- Keep one main UI scenario per test.
- Match repository style and architecture.

## If repository context is incomplete
- inspect nearby screens, tests, screen objects, and helpers first
- make the smallest safe assumption
- keep code buildable and consistent with the repo