# Stage — Testing

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
Write all tests for the change and mechanically verify they pass.
This stage **generates** — it does not evaluate quality. That is the Test Review stage's job.

The article principle: write the failing test *before* touching the application code for bug fixes and new behavior.

---

## Load
- `skills/android-unit-test/SKILL.md`
- `skills/android-instrumented-ui-test/SKILL.md`
- `skills/shared-json-scenarios/SKILL.md`
- `rules/testing-strategy.md`
- `coding/implementation_plan.md` — test plan section

---

## Execute

### 1. Select test layers
Read `rules/testing-strategy.md` to decide the minimum layers required.
Default: always start at the lowest layer that gives enough confidence.

### 2. Unit tests (`app/src/test/`)
Write unit tests for all new or modified:
- Domain use case logic
- ViewModel state transitions
- Mapper logic (DTO → Domain, Domain → UI)
- Formatting and fallback logic

Rules:
- All ViewModel unit tests inherit from `BaseViewModelTest`
- Class name ends with `Test.kt`
- One main scenario per test
- 90% line coverage target for new ViewModel and domain classes

### 3. Integration tests (`app/src/test/`)
Write integration tests if an API is involved.

For each changed API endpoint, test:
- Success response (2xx)
- 4xx client error
- 5xx server error
- Malformed or partial payload
- Network timeout / disconnect
- Unknown enum value (must not crash — must return fallback)

Rules:
- All ViewModel integration tests inherit from `BaseViewModelIntegrationTest`
- Class name ends with `IntegrationTest.kt`
- **Use shared JSON scenarios — do not inline mock data** (read `skills/shared-json-scenarios/SKILL.md`)
- Store scenarios in `sharedContracts/test-scenarios/`
- If API used by a ViewModel: assert `expected.ui` from the scenario
- If API used only by repo / use case: assert `expected.domain`

### 4. Instrumented UI tests (`app/src/androidTest/`)
Write instrumented tests only when Android runtime or real UI rendering is required.

Rules:
- Use `createComposeRule()` — **not** `createAndroidComposeRule` unless Activity is strictly required
- Test the stateless `Content` Composable — not the Hilt-wired `Screen` wrapper
- Do not use `Thread.sleep` — use `waitUntil` or `waitForIdle`
- One main business scenario per test

### 5. Import hygiene — applies to ALL test layers
These rules apply to every test file regardless of layer:

- **No fully-qualified class names** inline in property declarations, function parameters, or function bodies — always use a top-level `import` statement
  - ❌ `private val mock: com.example.auth.AuthManager = io.mockk.mockk(relaxed = true)`
  - ✅ `import com.example.auth.AuthManager` + `import io.mockk.mockk` then `private val mock: AuthManager = mockk(relaxed = true)`
- **No wildcard imports** — all imports must be explicit
  - ❌ `import io.mockk.*`
  - ✅ `import io.mockk.mockk`, `import io.mockk.every`, `import io.mockk.verify`
- **Imports sorted lexicographically** with no blank lines between entries

### 6. Run and record results
```bash
./gradlew testDebugUnitTest
./gradlew koverLog
```
If instrumented tests were added: `./gradlew connectedDebugAndroidTest`

Record every result number in the output report below. Do not summarize — copy actual pass/fail counts and coverage percentages verbatim from the tool output.

---

## Output

New or updated test files.
New or updated shared JSON scenarios in `sharedContracts/test-scenarios/`.

Produce `unit_test/test_report_v<N>.md`:
```
## Test Report — v<N>

### Test Layers Used
- Unit: <class names>
- Integration: <class names>
- Instrumented UI: <class names> or SKIPPED

### Shared JSON Scenarios
- <scenario file> — <endpoint> — <covers: success / 4xx / 5xx / malformed>

### Coverage (verbatim from koverLog output)
- Overall: X%
- New classes: X%

### Test Results (verbatim from Gradle output)
- Unit + Integration: <N passed / N total>
- Instrumented: <N passed / N total> or SKIPPED
```

Update `summary.md`: mark the Testing stage complete with test count and coverage.

---

## Gate

**Conditions to pass — all must be mechanically verifiable:**
- [ ] `./gradlew testDebugUnitTest` — exit code 0
- [ ] `./gradlew koverLog` — overall ≥ 80%, new classes ≥ 90%
- [ ] Total test count `> 0` (not `0/0` — this is a gate failure)
- [ ] At least one integration test per new or changed API endpoint
- [ ] Shared JSON scenarios used — no inline mock response data in test files
- [ ] Instrumented tests pass (if added): `./gradlew connectedDebugAndroidTest`
- [ ] `unit_test/test_report_v<N>.md` exists with coverage numbers filled in verbatim

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →**
- If `total_tests == 0` → return to the Testing stage, add missing tests
- If coverage < 80% → return to the Testing stage, add missing unit tests
- If test failures exist → fix the failing tests (which may require fixing application code in Stages 03–05)
- If a compilation error was introduced → return to the stage that caused it (03 / 04 / 05)

**Iteration cap:** 2 rounds of test revision. If still failing, surface the specific failure to the user.
