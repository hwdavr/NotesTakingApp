# Review Template

Use this template when producing the review summary in **Stage 09**.

---

## Review Summary

**Feature / Bug**: `<brief description>`  
**Reviewer**: Agent  
**Date**: `<date>`

---

## Build & Test Results

| Check | Result | Notes |
|-------|--------|-------|
| `assembleDebug` | ✅ PASS / ❌ FAIL | |
| `testDebugUnitTest` | ✅ PASS / ❌ FAIL | |
| `koverLog` overall | ✅ X% ≥ 80% / ❌ | |
| `koverLog` new classes | ✅ X% ≥ 90% / ❌ | |
| `connectedDebugAndroidTest` | ✅ PASS / ❌ FAIL / ⏭ SKIPPED | |
| `ktlintCheck` | ✅ PASS / ❌ FAIL | |
| `detekt` | ✅ PASS / ❌ FAIL | |
| `lintDebug` | ✅ PASS / ❌ FAIL | |

---

## Layer Violations

- [ ] None found
- Violations found:
  - `<file>`: `<description of violation>`

---

## Unrelated Changes

- [ ] None found
- Found:
  - `<file>`: `<description>`

---

## UI Verification

- [ ] Skipped (no UI changes)
- [ ] Texts verified against design via `adb uiautomator dump`
- [ ] Screenshot captured and compared
- [ ] Differences remaining: `<list or "none">`

---

## Security

- [ ] No secrets or tokens hardcoded
- [ ] No PII logged
- [ ] Sensitive data not stored unencrypted
- Concerns: `<list or "none">`

---

## Release Risk

**Level**: low / medium / high  
**Reason**: `<explanation>`

- Backward compatible: yes / no
- Feature flag required: yes / no
- Force update required: yes / no
- Backend deployment dependency: yes / no

---

## Remaining Risks

1. `<risk>`
2. `<risk>`

---

## Recommendation

- ✅ Ready to merge
- ⚠️ Merge with noted risks
- ❌ Do not merge — `<blocking issue>`
