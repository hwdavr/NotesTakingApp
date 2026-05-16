# Stage 06 — Code Review

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
This is the Evaluator stage — a separate review pass focused on architecture compliance, code quality, and UI correctness.
The article principle: "Separating the agent that does the work from the agent that judges the work is a powerful lever."

---

## Load
- `skills/code-review-and-quality/SKILL.md`
- `skills/android-code-quality-checks/SKILL.md`
- `rules/android-architecture.md`
- `rules/compose-rules.md`
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

### 3. Diff Review (using `gates/review-checklist.md`)
- No hardcoded strings (use `stringResource()`).
- All interactive elements have `testTag`.
- No unrelated changes mixed into the diff.

### 4. UI Verification (if any Composable changed)
```bash
./gradlew installDebug
adb shell am start -n <package>/<activity>
sleep 3
adb shell uiautomator dump /sdcard/ui.xml && adb pull /sdcard/ui.xml /tmp/ui.xml
grep -oP 'text="[^"]+"' /tmp/ui.xml | grep -v 'text=""'
adb exec-out screencap -p > screenshot.png
```

### 5. Security and Release Risk
Verify secrets, PII logging, and backward compatibility.

---

## Output

Produce `coding/review/code_review_v<N>.md` using `docs/templates/review-template.md`:
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

### Verdict
APPROVED / REVISION REQUIRED — <blocking issue>
```

Update `summary.md`: mark Stage 06 complete with review verdict.

---

## Gate

**Conditions to pass — all must be mechanically verifiable:**
- [ ] `assembleDebug` — exit code 0
- [ ] `ktlintCheck` — exit code 0
- [ ] No architecture or layer violations found
- [ ] UI matches the designed states in `spec.md`
- [ ] `coding/review/code_review_v<N>.md` exists with verdict filled in

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →**
- Compilation error → return to the stage that introduced it (03 / 04 / 05)
- Architecture violation → return to the relevant implementation stage
- Design mismatch → return to Stage 05 (UI)

**Iteration cap:** 2 rounds of review. If unresolved after 2 rounds, surface the specific issue to the user for a decision.
