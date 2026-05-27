# Stage — Test Review

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
Evaluate the **quality** of the tests written in the Testing stage.
This stage **evaluates** — it does not re-run CI. The Testing stage already owns execution and pass/fail.

The article principle: *agents can't objectively judge their own work. Separating generation from evaluation creates an honest feedback loop.*

The evaluator's job is to be skeptical on quality axes that the generator cannot self-assess:
- Are tests actually proving behavior, or are they tautological?
- Are edge cases genuinely covered, or just the happy path?
- Are shared scenarios used, or did inline mocks slip in?

---

## Load
- `unit_test/test_report_v<N>.md` (Testing stage output — source of truth for results)
- `coding/implementation_plan.md` — test plan section

---

## Execute

### 1. Read the test report
Open `unit_test/test_report_v<N>.md`. Do not re-run `testDebugUnitTest` or `koverLog` — these results are already recorded.
Use the report as the source of truth for pass/fail and coverage numbers.

### 2. Coverage distribution review
From the report's coverage numbers, assess:
- Is coverage concentrated on trivial paths (constructors, getters) rather than business logic branches?
- Are any new domain use case or ViewModel classes below 90%? Flag specifically which class and what's missing.
- Is overall coverage at or above 80%?

### 3. Test quality review
For each test file listed in the report, review the source:
- **Naming**: Do test names follow the `when_condition_thenExpected` pattern (or similar)? Names must describe the scenario, not just the method.
- **Assertiveness**: Are assertions specific? Flag `assertTrue(true)`, `assertNotNull` without follow-up, or empty `verify` blocks.
- **Isolation**: Do unit tests mock all external dependencies? Flag any real repository, database, or network calls in unit tests.
- **Shared scenarios**: Are shared JSON scenarios used for every API-related test? Flag any inline `""" { ... } """` mock response strings.
- **Edge case completeness**: For each API endpoint, are 4xx, 5xx, malformed payload, and unknown enum cases present?

### 4. Regression verification (for bug fixes only)
If this was a bug fix:
1. Confirm from the report that the regression test was listed as failing before the fix.
2. Confirm it is now in the passing count.
3. Check the test source for flakiness indicators (time-dependent logic, uncontrolled threading, hardcoded delays).

---

## Output

Produce `coding/review/test_review_v<N>.md`:
```
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
```

Update `summary.md`: mark the Test Review stage complete with verdict.

---

## Gate

**Conditions to pass:**
- [ ] Test report from Testing stage confirms: `testDebugUnitTest` exit code 0
- [ ] Test report confirms: overall ≥ 80%, new classes ≥ 90%
- [ ] No tautological assertions found
- [ ] No inline mock payloads — shared JSON scenarios used for all API calls
- [ ] All API endpoints have error-path coverage (4xx, 5xx, malformed, unknown enum)
- [ ] Regression test confirmed failing-before / passing-after (for bug fixes)
- [ ] `coding/review/test_review_v<N>.md` exists with verdict filled in

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →**
- Coverage gap or test failure → return to the Testing stage
- Tautological or non-isolated tests found → return to the Testing stage to fix
- Shared scenario missing → return to the Testing stage
- Flaky test found → return to the Testing stage to stabilize
