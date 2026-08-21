# Pitfall — Negative Input Language in Bottom-Sheet Designs

**Date**: 2026-08-16  
**Area**: Harness planning keyboard-mockup validator

The keyboard-mockup validator previously searched for text-field terms anywhere in a design document. A correct statement such as “the sheet has no text field” therefore triggered a false requirement for a keyboard-visible mockup.

When adding keyword-driven validators, distinguish affirmative product controls from explicit negative scope statements. The regression fixture in scripts/tests/keyboard-mockup-contract-test.sh covers a bottom sheet with tappable actions and no text field; it must pass without a keyboard asset, while an affirmative search text field must still require the keyboard-visible design state and asset.

**2026-08-21 extension**: the validator now also triggers on screen content with text input and a bottom toolbar (not just bottom sheets), so the negative filter was extended with `read-only` / `readonly`. Read-only/display-only “text fields” (e.g. a rendered code-block editor row with a `ReadOnly` state) are not text input and must not require a keyboard-visible mockup — the `readonly-field` regression fixture locks this in.
