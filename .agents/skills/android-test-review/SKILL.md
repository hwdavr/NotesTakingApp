---
name: android-test-review
description: Verifies test suite completion, boundary testing, and coverage targets.
---

# Skill — Android Test Review

## Purpose
An evaluator pass covering test coverage, test quality, shared scenario usage, and regression verification — always run as the first half of a review cycle, immediately before Code Review.

---

## Load

Load before starting (do not load what is already in context):

- `unit_test/test_report_t<taskId>_v<N>.md` (Testing stage output — source of truth for test results)

---

## Execute

### B1. Read the test report
Open `unit_test/test_report_t<taskId>_v<N>.md`. Do not re-run `testDebugUnitTest` or `koverLog` — these results are already recorded by the Testing stage.

### B2. Coverage distribution review
From the report's coverage numbers, assess:
- Is coverage concentrated on trivial paths rather than business logic branches?
- Are any new domain use case or ViewModel classes below 90%? Flag specifically which class and what's missing.
- Is overall coverage at or above 80%?

### B3. Test quality review
For each test file listed in the report, review the source:
- **Naming**: Do test names follow the `when_condition_thenExpected` pattern? Names must describe the scenario, not just the method.
- **Assertiveness**: Are assertions specific? Flag `assertTrue(true)`, `assertNotNull` without follow-up, or empty `verify` blocks.
- **Isolation**: Do unit tests mock all external dependencies? Flag any real repository, database, or network calls in unit tests.
- **Shared scenarios**: Are shared JSON scenarios used for every API-related test? Flag any inline `""" { ... } """` mock response strings.
- **Edge case completeness**: For each API endpoint, are 4xx, 5xx, malformed payload, and unknown enum cases present?
- **Import hygiene**: No fully-qualified class names inline, no wildcard imports, imports sorted lexicographically.

### B4. Regression verification (for bug fixes only)
If this was a bug fix:
1. Confirm from the report that the regression test was listed as failing before the fix.
2. Confirm it is now in the passing count.
3. Check the test source for flakiness indicators (time-dependent logic, uncontrolled threading, hardcoded delays).

---

## Output

Produce:
- `coding/review/test_review_t<taskId>_v<N>.md` by copying and filling in the template from `docs/templates/test-review-template.md`.

Update `summary_t<taskId>.md`: mark the Review stage complete with overall verdict (combining code and test review verdicts).

---

## Gate

All conditions must pass before proceeding to `skills/android-code-review/SKILL.md`:

- [ ] Test report confirms: `testDebugUnitTest` exit code 0
- [ ] Test report confirms: overall ≥ 80%, new classes ≥ 90%
- [ ] No tautological assertions found
- [ ] No inline mock payloads — shared JSON scenarios used for all API calls
- [ ] All API endpoints have error-path coverage (4xx, 5xx, malformed, unknown enum)
- [ ] Regression test confirmed failing-before / passing-after (for bug fixes)
- [ ] `coding/review/test_review_t<taskId>_v<N>.md` exists with all sections completed and overall verdict filled in

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →** Check the active workflow file to identify the correct stage to return to:
- Compilation error or architecture violation → return to the Implementation stage
- Design mismatch or rule violation (string/color/accessibility) → return to the UI implementation stage
- Test failure or coverage gap → return to the Testing stage
- Tautological/non-isolated tests or missing shared scenarios → return to the Testing stage
- Flaky test → return to the Testing stage to stabilize

**Iteration cap:** 2 rounds of revision. If unresolved after 2 rounds, surface the specific issue to the user for a decision.
