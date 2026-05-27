# Stage — Code + Test Review

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
A single evaluator pass that covers both the implementation and the tests written in the Testing stage.
The reviewer sees the full change — code and tests together — before the user is asked to approve.

The article principle: *"Separating generation from evaluation creates an honest feedback loop. Agents can't objectively judge their own work."*

---

## Load

Load **all** of the following before starting the review. Do not skip any rule file.

- `skills/code-review-and-quality/SKILL.md`
- `skills/android-code-quality-checks/SKILL.md`
- `rules/android-architecture.md`
- `rules/compose-rules.md`
- `rules/localization-rules.md`
- `rules/navigation-rules.md`  *(if navigation changed)*
- `rules/api-contract-rules.md` *(if API or data layer changed)*
- `rules/analytics-rules.md`   *(if analytics events changed)*
- `gates/review-checklist.md`
- `coding/coding_report_v<N>.md`
- `unit_test/test_report_v<N>.md` (Testing stage output — source of truth for test results)
- `request_analysis/spec.md` (for design compliance)

---

## Execute

### Part A — Code Review

#### A1. Build and Static Quality Checks
Run all checks and record results:
```bash
./gradlew assembleDebug
./gradlew ktlintCheck
./gradlew detekt
./gradlew lintDebug
```

#### A2. Architecture & Design Validation
Review every changed file against the designs in `spec.md`:
- **UiState compliance**: Does the implementation match the designed `UiState`?
- **Layer boundary check**:
  - UI → Presentation only
  - Presentation → Domain only
  - Data → Domain (implements interfaces only)
  - No upward or cross-layer dependencies
- **DI Scope**: Verify Hilt scopes (`@Singleton`, `@ViewModelScoped`) match the plan.
- **Domain purity**: No Android framework classes in domain layer.

#### A3. Per-Rule Diff Review

**Mandatory process — must not be skipped:**

For every rules file loaded in the **Load** section, scan every changed file against it and record each violation explicitly. Work through each rule file in turn:

##### A3a. `rules/compose-rules.md`
For each changed Composable:
- [ ] No hardcoded colors — all via `LocalAppColors.current.<token>` (no `Color(0x...)`, `Color.White`, `Color.Black`, etc.)
- [ ] All new color tokens added to **both** `LightAppColors` and `DarkAppColors` in `AppColors.kt`
- [ ] No hardcoded strings in any `Text()`, `Button()`, hint, or label — all via `stringResource()`
- [ ] All interactive elements have `Modifier.testTag(...)` with stable, descriptive names
- [ ] Stateless Content composable separated from the stateful Screen wrapper
- [ ] No lambda created inline in composable body — all passed as parameters

##### A3b. `rules/localization-rules.md`
For each changed file with user-visible text:
- [ ] Every `Text()` call uses `stringResource()` or `pluralStringResource()`
- [ ] Every new string is defined in `strings.xml` with the pattern `<screen>_<component>_<type>`
- [ ] Plural strings use `<plurals>` — not conditional string concatenation
- [ ] All non-text interactive elements have `contentDescription = stringResource(...)` — not `null`

##### A3c. `rules/android-architecture.md`
- [ ] No UI layer file imports a repository, DAO, or data-layer class directly
- [ ] No ViewModel imports Retrofit, Room, or data-layer implementation classes
- [ ] No domain class imports Android framework types (`Context`, `Bundle`, SDK)
- [ ] No fully-qualified class names used inline in any file — production or test code
- [ ] DTOs are not exposed outside the data layer

##### A3d. `rules/navigation-rules.md` *(if navigation changed)*
- [ ] Check against navigation rules — record any violations or mark N/A.

##### A3e. `rules/api-contract-rules.md` *(if API or data layer changed)*
- [ ] Check against API contract rules — record any violations or mark N/A.

##### A3f. `rules/analytics-rules.md` *(if analytics events changed)*
- [ ] Check against analytics rules — record any violations or mark N/A.

##### A3g. `gates/review-checklist.md` — full checklist
Work through every item and mark it PASS, FAIL, or N/A. Do not leave items blank.

#### A4. UI Verification (if any Composable changed)
```bash
./gradlew installDebug
adb shell am start -n <package>/<activity>
sleep 3
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml /tmp/ui.xml
grep -oP 'text="[^"]+"' /tmp/ui.xml | grep -v 'text=""'
adb exec-out screencap -p > screenshot.png
```
Verify no raw string literals appear in the UI dump output — all text must be resolved from `strings.xml`.

#### A5. Security and Release Risk
Verify secrets, PII logging, and backward compatibility.

---

### Part B — Test Review

#### B1. Read the test report
Open `unit_test/test_report_v<N>.md`. Do not re-run `testDebugUnitTest` or `koverLog` — these results are already recorded by the Testing stage.

#### B2. Coverage distribution review
From the report's coverage numbers, assess:
- Is coverage concentrated on trivial paths rather than business logic branches?
- Are any new domain use case or ViewModel classes below 90%? Flag specifically which class and what's missing.
- Is overall coverage at or above 80%?

#### B3. Test quality review
For each test file listed in the report, review the source:
- **Naming**: Do test names follow the `when_condition_thenExpected` pattern? Names must describe the scenario, not just the method.
- **Assertiveness**: Are assertions specific? Flag `assertTrue(true)`, `assertNotNull` without follow-up, or empty `verify` blocks.
- **Isolation**: Do unit tests mock all external dependencies? Flag any real repository, database, or network calls in unit tests.
- **Shared scenarios**: Are shared JSON scenarios used for every API-related test? Flag any inline `""" { ... } """` mock response strings.
- **Edge case completeness**: For each API endpoint, are 4xx, 5xx, malformed payload, and unknown enum cases present?
- **Import hygiene**: No fully-qualified class names inline, no wildcard imports, imports sorted lexicographically.

#### B4. Regression verification (for bug fixes only)
If this was a bug fix:
1. Confirm from the report that the regression test was listed as failing before the fix.
2. Confirm it is now in the passing count.
3. Check the test source for flakiness indicators (time-dependent logic, uncontrolled threading, hardcoded delays).

---

## Output

Produce `coding/review/review_v<N>.md` using `docs/templates/review-template.md`:

```
## Code + Test Review — v<N>

### Part A — Code Review

#### Build & Quality Results
| Check | Result |
|-------|--------|
| assembleDebug | ✅ PASS / ❌ FAIL |
| ktlintCheck | ✅ PASS / ❌ FAIL |
| detekt | ✅ PASS / ❌ FAIL |
| lintDebug | ✅ PASS / ❌ FAIL |

#### Architecture & Design Validation
- [ ] UiState matches design in spec.md
- [ ] Layer boundaries respected (no violations)
- [ ] DI scopes are correct
- [ ] Domain layer is pure

#### Per-Rule Violations
<For each rules file checked, list every violation (file, line, description) or "No violations found.">

#### gates/review-checklist.md — Full Results
<Paste the completed checklist with every item marked PASS / FAIL / N/A>

#### Code Verdict
APPROVED / REVISION REQUIRED — <blocking issue if REVISION REQUIRED>

---

### Part B — Test Review

#### Test Results (from test_report_v<N>.md — not re-run)
| Check | Result |
|-------|--------|
| testDebugUnitTest | ✅ PASS / ❌ FAIL |
| koverLog overall | ✅ X% (target ≥ 80%) |
| koverLog new classes | ✅ X% (target ≥ 90%) |
| connectedDebugAndroidTest | ✅ PASS / SKIPPED |

#### Coverage Distribution
- <class name>: X% — <note if borderline or missing branch>

#### Test Quality Findings
- [ ] Naming follows descriptive pattern
- [ ] Assertions are specific (no tautological asserts)
- [ ] Unit tests are fully isolated (no real dependencies)
- [ ] Shared JSON scenarios used — no inline mock payloads
- [ ] Edge cases and error states covered per API endpoint
- [ ] Import hygiene: no fully-qualified names, no wildcards, sorted imports
- [ ] Regression test confirmed failing-before / passing-after (if bug fix)

#### Test Verdict
APPROVED / REVISION REQUIRED — <specific blocking issue with file + line reference>

---

### Overall Verdict
APPROVED / REVISION REQUIRED — <summary of any blocking issues>
```

Update `summary.md`: mark the Review stage complete with overall verdict.

---

## Gate

**Code review conditions — all must pass:**
- [ ] `assembleDebug` — exit code 0
- [ ] `ktlintCheck` — exit code 0
- [ ] `detekt` — exit code 0
- [ ] No architecture or layer violations found
- [ ] `compose-rules.md` — all checks PASS or N/A
- [ ] `localization-rules.md` — all checks PASS or N/A
- [ ] `android-architecture.md` — all checks PASS or N/A
- [ ] `navigation-rules.md` — all checks PASS or N/A
- [ ] `api-contract-rules.md` — all checks PASS or N/A
- [ ] `analytics-rules.md` — all checks PASS or N/A
- [ ] `gates/review-checklist.md` — every item marked PASS or N/A
- [ ] UI matches the designed states in `spec.md`

**Test review conditions — all must pass:**
- [ ] Test report confirms: `testDebugUnitTest` exit code 0
- [ ] Test report confirms: overall ≥ 80%, new classes ≥ 90%
- [ ] No tautological assertions found
- [ ] No inline mock payloads — shared JSON scenarios used for all API calls
- [ ] All API endpoints have error-path coverage (4xx, 5xx, malformed, unknown enum)
- [ ] Regression test confirmed failing-before / passing-after (for bug fixes)

**Artifact condition:**
- [ ] `coding/review/review_v<N>.md` exists with all sections completed and overall verdict filled in

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →** Check the active workflow file to identify the correct stage to return to:
- Compilation error or architecture violation → return to the Implementation stage
- Design mismatch or rule violation (string/color/accessibility) → return to the UI implementation stage
- Test failure or coverage gap → return to the Testing stage
- Tautological/non-isolated tests or missing shared scenarios → return to the Testing stage
- Flaky test → return to the Testing stage to stabilize

**Iteration cap:** 2 rounds of revision. If unresolved after 2 rounds, surface the specific issue to the user for a decision.
