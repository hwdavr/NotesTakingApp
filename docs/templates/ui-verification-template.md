# UI Verification — v<N>

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
| <Screen> — <state> | ✅ / ❌ | ✅ / ❌ | `evidence/<screen>_<state>.png` |

**Reference design**: `design/<approved_mockup_or_screenshot>.png`

### Reference Anchor Verification

Record every design-critical relationship, including exact edge/center alignment, spacing,
overlay anchoring, and visual sizing inside a larger touch target. The `testTag` must select the
actual visual bounds when those differ from the tap target.

| Screen / State | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |
|----------------|------------------|---------------|-----------------------|-------------------|--------|
| <Screen> — <state> | <reference relationship> | `<TestClass>#<method>`; testTag: `<visual_bounds_tag>` | `<visualBounds>.<edge> == <anchorBounds>.<edge> ± <tolerance>dp` | `evidence/<screen>_<state>.png` | PASS / FAIL |

### Design Deviations

<none / list each visual deviation from the original design screenshot, with description>

### Verdict

PASS / FAIL — <reason if fail>
