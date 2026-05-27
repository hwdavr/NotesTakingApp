---
description: Implement or update the Android UI to match the provided screenshot.
---

# Workflow: Create UI and Verify

## When to use
Use this workflow when:
- Implementing a new screen from a design screenshot
- Updating an existing screen to match a revised design

---

## Stages

### Stage 1 — UI Implementation
Load: `stages/ui-layer.md`

Implement the UI changes. Save the provided design screenshot to `request_analysis/design/` before starting.

### Stage 2 — UI Verification ↩️ Loop
Load: `stages/ui-verification.md`

Compare the implemented UI against the original design screenshot in `request_analysis/design/`.

**Loop rule — if verification FAILS:**
- Return to **Stage 1 — UI Implementation** to fix the implementation.
- Re-run **Stage 2 — UI Verification** after each fix.
- **Maximum 3 loops total.**
- If still failing after 3 loops, stop and surface the deviation to the user with the screenshot attached.

**PASS →** proceed to Stage 3 — Code + Test Review.

### Stage 3 — Code + Test Review
Load: `stages/review.md`

## Best Practices
- **Handling Long Content**: For scrollable screens or bottom sheets, ensure the UI handles scrolling properly. In tests, use `performScrollToNode()` to find off-screen elements.
- **Bottom Sheets**: Use `skipPartiallyExpanded = true` for bottom sheets with significant content to improve immediate visibility and test reliability.
- **Duplicate Text**: When multiple nodes share the same text, use `onAllNodesWithText()[index]` to avoid ambiguity in assertions.