# Test Review Template

Use this template when producing the test review summary in the review stage.

---

## Test Review — v<N>

### Test Results (from test_report_v<N>.md — not re-run)
| Check | Result |
|-------|--------|
| testDebugUnitTest | ✅ PASS / ❌ FAIL |
| koverLog overall | ✅ X% (target ≥ 80%) |
| koverLog new classes | ✅ X% (target ≥ 90%) |
| connectedDebugAndroidTest | ✅ PASS / SKIPPED |

### Coverage Distribution
- <class name>: X% — <note if borderline or missing branch>

### Test Quality Findings
- [ ] Naming follows descriptive pattern
- [ ] Assertions are specific (no tautological asserts)
- [ ] Unit tests are fully isolated (no real dependencies)
- [ ] Shared JSON scenarios used — no inline mock payloads
- [ ] Edge cases and error states covered per API endpoint
- [ ] Regression test confirmed (if bug fix)

### Verdict
APPROVED / REVISION REQUIRED — <specific blocking issue with file + line reference>
