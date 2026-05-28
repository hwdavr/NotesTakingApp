# Review Template

Use this template when producing the review summary in the relevant stage.

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
| `check-compose-rules.sh` | ✅ PASS / ❌ FAIL / ⏭ SKIPPED (no Compose changes) | |

---

## Compose Rules Enforcement

> Skip this section entirely if the change contains no Compose (`*.kt` UI) file modifications.

For each rule, record how it was checked for **this change** and its outcome.

**Status key**

| Symbol | Meaning |
|--------|---------|
| ✅ | Checked — no violations found |
| ❌ | Checked — violation(s) found (list below) |
| 👁️ **Human** | Not checked by script or AI — requires human review before merge |
| ⏭ | Not applicable to this change |

### Section 1 — Composable Responsibilities

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 1.1 Receives `UiState` + callbacks as params | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 1.2 Only renders state — no derived computation | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 1.3 Never calls ViewModel directly | 🤖 Check 4 + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 1.4 No use case / repository calls | 🤖 Check 5 + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 1.5 No business logic / data transformation | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 1.6 No hardcoded strings — uses `stringResource()` | 🤖 Check 1 | ✅ / ❌ | |
| 1.7 No hardcoded colors — uses `LocalAppColors` | 🤖 Check 2 | ✅ / ❌ | |

### Section 2 — Stateless / Stateful Pattern

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 2.1 Screen split into `*Screen` + `*Content` pair | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 2.2 Only `*Screen` calls `hiltViewModel()` / `collectAsStateWithLifecycle()` | 🤖 Check 4 + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 2.3 UI tests target `*Content`, not `*Screen` | 🧠 Evaluator | ✅ / ❌ / ⏭ | |

### Section 3 — Test Tags

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 3.1 All interactive elements have `testTag` | 🤖 Check 3 + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 3.2 Key content containers have `testTag` | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 3.3 `testTag` names are descriptive and stable | 🤖 Check 6 + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |

### Section 4 — String Resources

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 4.1 All user-visible text uses `stringResource()` | 🤖 Check 1 | ✅ / ❌ | |
| 4.2 Resource keys follow `<screen>_<element>_<type>` naming | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |

### Section 5 — Colors

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 5.1 No `Color(0x...)` outside `AppColors.kt` | 🤖 Check 2a | ✅ / ❌ | |
| 5.2 No named `Color.*` outside `AppColors.kt` | 🤖 Check 2b | ✅ / ❌ | |
| 5.3 Colors accessed via `LocalAppColors.current.<token>` | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 5.4 Color tokens named by semantic purpose | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 5.5 New color added to both Light **and** Dark theme | 🤖 Script + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |

### Section 6 — Component Extraction

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 6.1 Reused UI extracted to `components/` | 👁️ Human + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 6.2 Complex / stateful components extracted | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 6.3 One visual responsibility per component | 👁️ Human + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |

### Section 7 — State Hoisting

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 7.1 State hoisted to the lowest common ancestor | 👁️ Human + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 7.2 State not hoisted higher than necessary | 👁️ Human + 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 7.3 No `remember {}` inside `*Content` composables | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |

### Section 8 — Performance

| Rule | How Checked | Status | Violations |
|------|-------------|--------|------------|
| 8.1 `LazyColumn` instead of `Column` + `forEach` | 🤖 Check 7 | ✅ / ❌ | |
| 8.2 Stable parameter types to avoid recompositions | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 8.3 `key()` used in lazy lists with stable IDs | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |
| 8.4 Lambdas passed as parameters, not created inline | 🧠 Evaluator | ✅ / ❌ / 👁️ Human | |

### Compose Rule Violations Detail

> List each violation found above. Delete this section if there are none.

- **Rule X.Y** — `<file>:<line>`: `<description>`

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
