# Stage — Code Review

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
This is the Evaluator stage — a separate review pass focused on architecture compliance, code quality, and UI correctness.
The article principle: "Separating the agent that does the work from the agent that judges the work is a powerful lever."

---

## Load

Load **all** of the following before starting the review. Do not skip any rule file — omitting a rules file means its constraints will not be checked.

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
- `request_analysis/spec.md` (for design compliance)

---

## Execute

### 1. Build and Static Quality Checks
Run all checks and record results:
```bash
./gradlew assembleDebug
./gradlew ktlintCheck
./gradlew detekt
./gradlew lintDebug
```

### 2. Architecture & Design Validation
Review every changed file against the designs in `spec.md`:
- **UiState compliance**: Does the implementation match the designed `UiState`?
- **Layer boundary check**:
  - UI → Presentation only
  - Presentation → Domain only
  - Data → Domain (implements interfaces only)
  - No upward or cross-layer dependencies
- **DI Scope**: Verify Hilt scopes (`@Singleton`, `@ViewModelScoped`) match the plan.
- **Domain purity**: No Android framework classes in domain layer.

### 3. Per-Rule Diff Review

**Mandatory process — must not be skipped:**

For every rules file loaded in the **Load** section, scan every changed file against it and record each violation explicitly. Do not summarise or shortcut — work through each rule file in turn:

#### 3a. `rules/compose-rules.md`
For each changed Composable:
- [ ] No hardcoded colors — all via `LocalAppColors.current.<token>` (no `Color(0x...)`, `Color.White`, `Color.Black`, etc.)
- [ ] All new color tokens added to **both** `LightAppColors` and `DarkAppColors` in `AppColors.kt`
- [ ] No hardcoded strings in any `Text()`, `Button()`, hint, or label — all via `stringResource()`
- [ ] All interactive elements have `Modifier.testTag(...)` with stable, descriptive names
- [ ] Stateless Content composable separated from the stateful Screen wrapper
- [ ] No lambda created inline in composable body — all passed as parameters

#### 3b. `rules/localization-rules.md`
For each changed file with user-visible text:
- [ ] Every `Text()` call uses `stringResource()` or `pluralStringResource()`
- [ ] Every new string is defined in `strings.xml` with the pattern `<screen>_<component>_<type>`
- [ ] Plural strings use `<plurals>` — not conditional string concatenation
- [ ] All non-text interactive elements have `contentDescription = stringResource(...)` — not `null`

#### 3c. `rules/android-architecture.md`
- [ ] No UI layer file imports a repository, DAO, or data-layer class directly
- [ ] No ViewModel imports Retrofit, Room, or data-layer implementation classes
- [ ] No domain class imports Android framework types (`Context`, `Bundle`, SDK)
- [ ] No fully-qualified class names used inline in function bodies (e.g. `com.example.Foo()`) — use `import`
- [ ] DTOs are not exposed outside the data layer

#### 3d. `rules/navigation-rules.md` *(if navigation changed)*
- [ ] Check against navigation rules — record any violations or mark N/A.

#### 3e. `rules/api-contract-rules.md` *(if API or data layer changed)*
- [ ] Check against API contract rules — record any violations or mark N/A.

#### 3f. `rules/analytics-rules.md` *(if analytics events changed)*
- [ ] Check against analytics rules — record any violations or mark N/A.

#### 3g. `gates/review-checklist.md` — full checklist
Work through every item in the checklist and mark it PASS, FAIL, or N/A. Do not leave items blank.

### 4. UI Verification (if any Composable changed)
```bash
./gradlew installDebug
adb shell am start -n <package>/<activity>
sleep 3
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml /tmp/ui.xml
grep -oP 'text="[^"]+"' /tmp/ui.xml | grep -v 'text=""'
adb exec-out screencap -p > screenshot.png
```
Verify no raw string literals appear in the UI dump output — all text must be resolved from `strings.xml`.

### 5. Security and Release Risk
Verify secrets, PII logging, and backward compatibility.

---

## Output

Produce `coding/review/code_review_v<N>.md` using `docs/templates/review-template.md`. The report **must** include all of the following sections — none may be omitted:

```
## Code Review — v<N>

### Build & Quality Results
| Check | Result |
|-------|--------|
| assembleDebug | ✅ PASS / ❌ FAIL |
| ktlintCheck | ✅ PASS / ❌ FAIL |
| detekt | ✅ PASS / ❌ FAIL |
| lintDebug | ✅ PASS / ❌ FAIL |

### Architecture & Design Validation
- [ ] UiState matches design in spec.md
- [ ] Layer boundaries respected (no violations)
- [ ] DI scopes are correct
- [ ] Domain layer is pure

### Per-Rule Violations
For each rules file checked, list every violation found (file, line, description) or write "No violations found."

#### compose-rules.md
<list violations or "No violations found.">

#### localization-rules.md
<list violations or "No violations found.">

#### android-architecture.md
<list violations or "No violations found.">

#### navigation-rules.md
<list violations or "N/A — no navigation changes.">

#### api-contract-rules.md
<list violations or "N/A — no API/data layer changes.">

#### analytics-rules.md
<list violations or "N/A — no analytics changes.">

### gates/review-checklist.md — Full Results
<Paste the completed checklist with every item marked PASS / FAIL / N/A>

### Verdict
APPROVED / REVISION REQUIRED — <blocking issue if REVISION REQUIRED>
```

Update `summary.md`: mark the relevant stage complete with review verdict.

---

## Gate

**Conditions to pass — all must be mechanically verifiable:**
- [ ] `assembleDebug` — exit code 0
- [ ] `ktlintCheck` — exit code 0
- [ ] `detekt` — exit code 0
- [ ] No architecture or layer violations found
- [ ] `compose-rules.md` — all checks PASS or N/A (zero open violations)
- [ ] `localization-rules.md` — all checks PASS or N/A (zero open violations)
- [ ] `android-architecture.md` — all checks PASS or N/A (zero open violations)
- [ ] `navigation-rules.md` — all checks PASS or N/A (zero open violations)
- [ ] `api-contract-rules.md` — all checks PASS or N/A (zero open violations)
- [ ] `analytics-rules.md` — all checks PASS or N/A (zero open violations)
- [ ] `gates/review-checklist.md` — every item marked PASS or N/A (none left blank)
- [ ] UI matches the designed states in `spec.md`
- [ ] `coding/review/code_review_v<N>.md` exists with verdict filled in and all Per-Rule Violations sections completed

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →**
- Compilation error → return to the stage that introduced it (03 / 04 / 05)
- Architecture violation → return to the relevant implementation stage
- Design mismatch → return to the relevant stage (UI)
- Rule violation (string/color/accessibility) → return to the UI Layer stage

**Iteration cap:** 2 rounds of review. If unresolved after 2 rounds, surface the specific issue to the user for a decision.
