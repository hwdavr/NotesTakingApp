# Stage — Test Review

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
Verify the quality, coverage, and correctness of the tests implemented in the relevant stage.
Ensure the change is fully verified before final knowledge capture.

---

## Load
- `skills/android-code-quality-checks/SKILL.md`
- `gates/ci-checks.md`
- `unit_test/test_report_v<N>.md` (the relevant stage output)
- `coding/implementation_plan.md` — test plan section

---

## Execute

### 1. Test Execution & Coverage
Verify the results reported in the relevant stage:
```bash
./gradlew testDebugUnitTest
./gradlew koverLog
```
If instrumented tests were added: `./gradlew connectedDebugAndroidTest`

### 2. Test Quality Review
Check for:
- **Shared JSON Scenarios**: Are they used for all API-related tests? No inline mock data?
- **Naming**: Do test names follow the `When_Condition_Then_Expected` or similar pattern?
- **Completeness**: Are edge cases, error states (4xx, 5xx), and unknown enum variants covered?
- **Isolation**: Do unit tests mock dependencies correctly? No real repositories in unit tests?
- **Assertive**: Are assertions specific? No `assertTrue(true)`?

### 3. Regression Verification (for bugs)
If this was a bug fix:
1. Confirm the regression test failed before the fix.
2. Confirm the regression test passes after the fix.
3. Confirm the test is stable and not flaky.

---

## Output

Produce `coding/review/test_review_v<N>.md`:
```
## Test Review — v<N>

### Test Execution Results
| Check | Result |
|-------|--------|
| testDebugUnitTest | ✅ PASS / ❌ FAIL |
| koverLog overall | ✅ X% (target 80%) |
| koverLog new classes | ✅ X% (target 90%) |
| connectedDebugAndroidTest | ✅ PASS / SKIPPED |

### Test Quality Checklist
- [ ] Shared JSON scenarios used for all API calls
- [ ] Edge cases and error states covered
- [ ] Unit tests are properly isolated (mocked)
- [ ] Regression verified (if bug fix)

### Verdict
APPROVED / REVISION REQUIRED — <blocking issue>
```

Update `summary.md`: mark the relevant stage complete with test review verdict.

---

## Gate

**Conditions to pass — all must be mechanically verifiable:**
- [ ] `testDebugUnitTest` — exit code 0
- [ ] `koverLog` — overall ≥ 80%, new classes ≥ 90%
- [ ] All API-related tests use shared JSON scenarios
- [ ] Regression test confirmed failing before and passing after (for bugs)
- [ ] `coding/review/test_review_v<N>.md` exists with verdict filled in

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →**
- Test failure or coverage gap → return to the relevant stage
- Flaky test found → return to the relevant stage to stabilize
- Shared scenario missing → return to the relevant stage
