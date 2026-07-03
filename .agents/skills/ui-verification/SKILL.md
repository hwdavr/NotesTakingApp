---
name: ui-verification
description: Verifies Android UI screens visually and interactively using screenshots and mockups.
---

# Skill — UI Verification

## Purpose
Verify that the implemented UI renders correctly and matches the design specified in `spec_t<taskId>.md`.
This stage runs after implementation is complete and before (or as part of) the code review.

Use the cheapest reliable check first — build and static analysis → instrumented UI tests → visual screenshot verification.

---

## Load
- `skills/android-ui-verification/SKILL.md`
- `rules/android-architecture.md` — ensure no layer violations (UI importing data classes, etc.)
- `request_analysis/spec_t<taskId>.md` — UiState design and visual specification from the Requirement, Impact & Design Analysis stage
- `request_analysis/design/` — **original design screenshots** provided by the user in the Requirement, Impact & Design Analysis stage (e.g. `design.png`)
- `coding/implementation_plan_t<taskId>.md` — list of changed Composables

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
Run existing instrumented tests for any changed screen:
```bash
./gradlew connectedDebugAndroidTest
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

**Compare every text string** against the design in `spec_t<taskId>.md`. Record any mismatches.

### 4. Side-by-side comparison against original design screenshot
Locate the original design screenshot from `request_analysis/design/` (uploaded in the Requirement, Impact & Design Analysis stage).

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

If no original design screenshot exists, mark this step as `SKIPPED — no design reference provided`.

### 5. Checklist — what to verify
- [ ] Screen is shown with no crash
- [ ] All text strings match the design (`spec_t<taskId>.md`)
- [ ] Correct content renders from mocked / real data
- [ ] CTAs visible and correctly enabled/disabled per state
- [ ] Loading / empty / error / success states all render
- [ ] Navigation destination or back-stack behavior matches design
- [ ] No layout overflow or clipping on common screen sizes
- [ ] Long content handled correctly (scrolling verified)
- [ ] Bottom sheets with long content use `skipPartiallyExpanded = true` for reliable accessibility
- [ ] Actual screenshot matches the original design screenshot (or SKIPPED — no design reference)

---

## Output

Produce `coding/ui_verification.md`:
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
| Screen / State | Texts Match Design | Matches Original Screenshot | Screenshot |
|----------------|-------------------|-----------------------------|------------|
| <Screen> — Loading | ✅ / ❌ | ✅ / ❌ / ⏭ SKIPPED | screenshot.png |
| <Screen> — Success | ✅ / ❌ | ✅ / ❌ / ⏭ SKIPPED | screenshot.png |
| <Screen> — Error | ✅ / ❌ | ✅ / ❌ / ⏭ SKIPPED | screenshot.png |

### Design Deviations
<none / list each visual deviation from the original design screenshot, with description>

### Verdict
PASS / FAIL — <reason if fail>
```

Update `summary_t<taskId>.md`: mark UI Verification stage complete.

---

## Done When

**This stage is complete when all of the following are true — all must be mechanically verifiable:**
- [ ] `assembleDebug` — exit code 0
- [ ] `lintDebug` and `ktlintCheck` — exit code 0
- [ ] Instrumented UI tests pass (if present): `./gradlew connectedDebugAndroidTest`
- [ ] All text strings verified against design — no unresolved mismatches
- [ ] Actual screenshot compared against original design screenshot — no unresolved visual deviations (or SKIPPED with reason)
- [ ] All UiState variants (loading, success, empty, error) confirmed rendering correctly
- [ ] `coding/ui_verification.md` exists with verdict filled in

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →**
- Build failure → fix compilation error in the UI implementation stage
- Text mismatch → fix hardcoded strings or string resource values, re-run verification
- State rendering issue → return to UI implementation stage and fix ViewModel or Composable

**Iteration cap:** 2 rounds. If a layout issue cannot be resolved through text/state verification alone, surface it to the user with the screenshot attached.
