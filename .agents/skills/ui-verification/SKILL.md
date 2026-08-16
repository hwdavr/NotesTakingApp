---
name: ui-verification
description: Verifies Android UI screens visually and interactively using screenshots and mockups.
---

# Skill — UI Verification

## Purpose
Verify that the implemented UI renders correctly and matches the design specified in `spec_v<N>.md`.
This stage runs after implementation is complete and before (or as part of) the code review.

Use the cheapest reliable check first — build and static analysis → instrumented UI tests → visual screenshot verification.

---

## Load
- `docs/product/design_system.md` — project-wide visual and component baseline
- `rules/android-architecture.md` — ensure no layer violations (UI importing data classes, etc.)
- `docs/current/spec_v<N>.md` — UiState design and visual specification from the Requirement, Impact & Design Analysis stage
- `docs/current/design/` — **original design screenshots** provided by the user in the Requirement, Impact & Design Analysis stage (e.g. `design.png`)
- `docs/current/implementation_plan_v<N>.md` — list of changed Composables

---

## Execute

### 1. Build and static checks
```bash
./gradlew assembleDebug
./gradlew lintDebug
./gradlew ktlintCheck
```
All must pass before proceeding to visual steps.

### 2. Instrumented UI test verification
Run existing instrumented tests for any changed screen (target an emulator first, e.g. using `ANDROID_SERIAL=emulator-5554`, and fall back to a connected physical device only if no emulator is present):
```bash
ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest
```

For each changed Composable, verify:
- [ ] Loading state renders correctly
- [ ] Success / content state renders correctly
- [ ] Empty state renders correctly (if applicable)
- [ ] Error state renders correctly (if applicable)
- [ ] CTAs are visible and correctly enabled/disabled
- [ ] Navigation triggers work as expected

### 3. Visual verification (adb)
Install the app and navigate to the target screen:
```bash
./gradlew installDebug
adb shell am start -n <package>/<activity>
```

Confirm you are on the correct screen before capturing:
```bash
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml /tmp/ui.xml
grep -oP 'text="[^"]+"' /tmp/ui.xml | grep -v 'text=""'
```

Capture a screenshot:
```bash
adb exec-out screencap -p > screenshot.png
```

Scroll to reveal off-screen content if needed:
```bash
adb shell input swipe 540 1200 540 400
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml /tmp/ui.xml
```

**Compare every text string** against the design in `spec_v<N>.md`. Record any mismatches.

### 4. Side-by-side comparison against original design screenshot and design system
Locate the original design screenshot from `docs/current/design/` (uploaded in the Requirement, Impact & Design Analysis stage).

For each screen state that has a reference design:
1. Place the original design screenshot and the captured `screenshot.png` side by side.
2. Compare:
   - Layout and element positions
   - Typography (font size, weight, color)
   - Colors and backgrounds
   - Icons and images
   - Spacing and padding
   - Any UI element that differs from the reference
3. Record every visual deviation — no matter how minor — in the output report.

Also compare colors, typography, spacing, shapes, opacity, toolbars, overlays/sheets, and control states against `docs/product/design_system.md`. A feature-local difference passes only when `design.md` records it as an explicit user-approved exception.

If no original design screenshot exists, use the approved generated mockup. If neither original nor
approved generated reference exists, do not record a visual PASS: return to the reference-design
stage or request the missing reference. A UI comparison cannot be skipped as passing evidence.

### 4a. Reference-anchor evidence for exact placement

A broad screenshot comparison cannot prove exact placement when the reference encodes a spatial
relationship. For every design-critical relationship (for example, an edge meeting a table/grid
line, a center anchored to a border, equal spacing, an overlay corner, or a compact visual inside
a 48dp touch target), add a bounds-based instrumented assertion and record it in the report.

- Give the rendered visual shape its own stable `testTag` when the outer interactive target is
  larger than the visible icon, strip, or pill. Do not measure only the touch target when the
  reference concerns the visual's edge or size.
- Use Compose semantics bounds (for example, `fetchSemanticsNode().boundsInRoot`) and an explicit
  density-derived tolerance to compare the two named anchors.
- Capture the actual screenshot in the same state as the bounds assertion.
- Record the exact reference asset, test method, visual `testTag`, measured relationship, actual
  screenshot path, and result. A generic “matches screenshot” claim is not evidence of alignment.

### 5. Checklist — what to verify
- [ ] Screen is shown with no crash
- [ ] All text strings match the design (`spec_v<N>.md`)
- [ ] Correct content renders from mocked / real data
- [ ] CTAs visible and correctly enabled/disabled per state
- [ ] Loading / empty / error / success states all render
- [ ] Navigation destination or back-stack behavior matches design
- [ ] No layout overflow or clipping on common screen sizes
- [ ] Long content handled correctly (scrolling verified)
- [ ] Bottom sheets with long content use `skipPartiallyExpanded = true` for reliable accessibility
- [ ] Actual screenshot matches the original or approved generated reference
- [ ] Actual screenshot conforms to `docs/product/design_system.md` except for explicitly documented approved exceptions

---

## Output

Produce `docs/current/ui_verification.md` from
`docs/templates/ui-verification-template.md`:
```
## UI Verification — v<N>

### Build & Static Checks
| Check | Result |
|-------|--------|
| assembleDebug | ✅ PASS / ❌ FAIL |
| lintDebug | ✅ PASS / ❌ FAIL |
| ktlintCheck | ✅ PASS / ❌ FAIL |

### Instrumented Tests
- Result: <N passed / N total>

### Visual Verification
| Screen / State | Texts Match Design | Matches Approved Reference | Screenshot |
|----------------|-------------------|----------------------------|------------|
| <Screen> — Loading | ✅ / ❌ | ✅ / ❌ | screenshot.png |
| <Screen> — Success | ✅ / ❌ | ✅ / ❌ | screenshot.png |
| <Screen> — Error | ✅ / ❌ | ✅ / ❌ | screenshot.png |

**Reference design**: `design/<approved_mockup_or_screenshot>.png`

### Reference Anchor Verification
| Screen / State | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|----------------|------------------|---------------|-----------------------|-------------------|--------|
| <Screen> — <state> | <visual edge/center/spacing relationship from the approved reference> | `<TestClass>#<method>`; testTag: `<visual_bounds_tag>` | `<visualBounds>.<edge> == <anchorBounds>.<edge> ± <tolerance>dp` | `evidence/<screen>_<state>.png` | PASS / FAIL |

### Design Deviations
<none / list each visual deviation from the original design screenshot, with description>

### Verdict
PASS / FAIL — <reason if fail>
```

Update `docs/current/summary_v<N>.md`: mark UI Verification stage complete.

---

## Done When

**This stage is complete when all of the following are true — all must be mechanically verifiable:**
- [ ] `assembleDebug` — exit code 0
- [ ] `lintDebug` and `ktlintCheck` — exit code 0
- [ ] Instrumented UI tests pass (if present): `./gradlew connectedDebugAndroidTest`
- [ ] All text strings verified against design — no unresolved mismatches
- [ ] Actual screenshot compared against the original or approved generated reference — no unresolved visual deviations
- [ ] Each design-critical spatial relationship has a reference-anchor row tied to a visual bounds tag, runtime test, measured relation, and actual screenshot
- [ ] All UiState variants (loading, success, empty, error) confirmed rendering correctly
- [ ] `docs/current/ui_verification.md` exists with verdict filled in
- [ ] `bash scripts/check-stage-artifacts.sh create-ui-and-verify ui-verification docs/current` exits 0

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →**
- Build failure → fix compilation error in the UI implementation stage
- Text mismatch → fix hardcoded strings or string resource values, re-run verification
- State rendering issue → return to UI implementation stage and fix ViewModel or Composable

**Iteration cap:** 2 rounds. If a layout issue cannot be resolved through text/state verification alone, surface it to the user with the screenshot attached.
