---
name: android-code-review
description: Performs a structured code review across correctness, security, performance, and architecture.
---

# Skill — Android Code Review

## Purpose
An evaluator pass covering the implementation — build quality, static analysis, architecture compliance, and rule adherence — always run as the second half of a review cycle, immediately after Test Review.

---

## Load

Load before starting (android-test-review SKILL.md context should already be loaded — do not re-load what is already in context):

- `skills/code-review-and-quality/SKILL.md`
- `skills/android-code-quality-checks/SKILL.md`
- `rules/android-architecture.md`
- `rules/compose-rules.md`
- `rules/localization-rules.md`
- `rules/navigation-rules.md`  *(if navigation changed)*
- `rules/api-contract-rules.md` *(if API or data layer changed)*
- `rules/analytics-rules.md`   *(if analytics events changed)*
- `gates/review-checklist.md`
- `coding/coding_report_t<taskId>_v<N>.md`
- `request_analysis/spec_t<taskId>.md` (for design compliance)

---

## Execute

### 1. Build and Static Quality Checks

Run all checks and record results:
```bash
./gradlew assembleDebug
./gradlew ktlintCheck
./gradlew detekt
./gradlew lintDebug
bash scripts/check-compose-rules.sh
bash scripts/check-localization-rules.sh
bash scripts/check-architecture-rules.sh
```

On Windows (using PowerShell or Command Prompt), run the native script launchers instead:
```powershell
scripts\check-compose-rules.cmd
scripts\check-localization-rules.cmd
scripts\check-architecture-rules.cmd
```

Record the exit code and any flagged violations from all three scripts in the **Build & Test Results** table of the review report.

### 2. Architecture & Design Validation

Review every changed file against the designs in `spec_t<taskId>.md`:
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

For every rules file loaded in the **Load** section, scan every changed file against it and record each violation explicitly. Work through each rule file in turn:

#### 3a. `rules/compose-rules.md`

Only run this section if the change touches Compose (UI `*.kt`) files. Otherwise mark the entire section N/A in the review report.

**Step 1 — Run the script (Scripted rules)**

The script has already run in step 1. Refer to its output to fill in the 🤖 rows in the Compose Rules Enforcement table. Mark each as ✅ (no violations) or ❌ (violations — list them in the Violations column).

Rules automatically covered by the script:
- **1.6** No hardcoded strings (`Text()`, `label=`, etc.) → Check 1
- **1.7** No hardcoded colors `Color(0x...)` / named `Color.*` → Check 2a/2b
- **1.3 / 2.2** `hiltViewModel()` / `viewModel()` not in `*Content` → Check 4
- **1.4** No repository/use-case calls inside Composable → Check 5
- **3.1** Files with interactive elements but no `testTag` → Check 3
- **3.3** No string interpolation in `testTag` values → Check 6
- **4.1** All user-visible text uses `stringResource()` → Check 1
- **5.1** No `Color(0x...)` outside `AppColors.kt` → Check 2a
- **5.2** No named `Color.*` outside `AppColors.kt` → Check 2b
- **8.1** `LazyColumn` instead of `Column + forEach` → Check 7

**Step 2 — Evaluate remaining rules (Evaluator rules)**

For each changed Composable file, read the source and evaluate the following rules that the script cannot check:
- **1.1** Composable receives `UiState` + callbacks as params — no data objects from lower layers exposed directly
- **1.2** Composable only renders state — no sorting, filtering, or formatting logic inside composable body
- **1.5** No business logic or data transformation anywhere inside the composable body
- **2.1** Each screen has a `*Screen` stateful wrapper and a `*Content` stateless composable pair
- **2.3** UI tests target `*Content`, not `*Screen` (check test files)
- **3.2** Key content containers (list items, empty/error states, loading indicators, nav elements) have `testTag`
- **3.3** `testTag` names are descriptive — flag any `"btn"`, `"item"`, or single-word tags
- **4.2** String resource keys follow `<screen>_<element>_<type>` naming pattern
- **5.3** Colors accessed via `LocalAppColors.current.<token>` — not via module-level `val` workarounds
- **5.4** Color token names describe semantic purpose (`textSecondary`) not value (`gray`)
- **5.5** Any new color added to **both** `LightAppColors` and `DarkAppColors` in `AppColors.kt`
- **6.1** Repeated UI structure extracted to `components/` when it appears on more than one screen
- **6.2** Components with internal state or complexity extracted to their own composable
- **6.3** Each component has one visual responsibility
- **7.1** State hoisted to the lowest common ancestor that needs it
- **7.2** State not hoisted higher than necessary
- **7.3** No `remember {}` inside `*Content` composables
- **8.2** Stable types passed as parameters (no raw `List<>`, `Map<>`, inline lambdas that cause recomposition)
- **8.3** `key()` used in `items()` / `itemsIndexed()` when items have stable IDs
- **8.4** Lambdas passed as parameters — not created inside the composable body

**Step 3 — Mark unchecked rules**

For any rule in the Compose Rules Enforcement table that was neither run by the script nor evaluated in Step 2, set the Status to `👁️ Human` in the review report. This flags it explicitly for human review before merge.

#### 3b. `rules/localization-rules.md`

Only skip this section if the change adds no user-visible text and no Kotlin UI file is modified. Otherwise mark the entire section N/A in the review report.

**Step 1 — Run the script (Scripted rules)**

The script has already run in step 1. Refer to its output to fill in the 🤖 rows in the Localization Rules Enforcement table. Mark each as ✅ (no violations) or ❌ (violations — list them in the Violations column).

Rules automatically covered by the script:
- **1.1** `Text()` called with a raw string literal → Check 1
- **1.2** `label=`, `title=`, `placeholder=`, `hint=` set as a raw string → Check 2
- **1.3** Local UI label variables assigned a raw string → Check 3
- **6.2** `contentDescription = null` on interactive icons → Check 4

**Step 2 — Evaluate remaining rules (Evaluator rules)**

For each changed source file and `strings.xml`, read the code and evaluate the following rules that the script cannot check:
- **2.1** All new string values are defined in `strings.xml` — not as Kotlin `const val` or companion object properties
- **3.1** Every new string resource key follows the `<screen>_<component>_<type>` naming pattern
- **4.1** Any count-dependent text uses `<plurals>` — not `if (count == 1)` string concatenation
- **4.2** Plural strings are accessed via `pluralStringResource()` at the call site
- **5.1** Strings with dynamic values use format arguments (`%s`, `%d`) in `strings.xml` — not string concatenation in Kotlin
- **5.2** Format arguments are passed correctly via `stringResource(R.string.key, arg)` at the call site
- **6.1** All non-text interactive elements (icon buttons, image buttons) have a non-null `contentDescription = stringResource(...)` — not missing entirely

**Step 3 — Mark unchecked rules**

For any rule in the Localization Rules Enforcement table that was neither run by the script nor evaluated in Step 2, set the Status to `👁️ Human`.

#### 3c. `rules/android-architecture.md`

Only skip this section if the change touches no Kotlin source files. Otherwise mark the entire section N/A in the review report.

**Step 1 — Run the script (Scripted rules)**

The script has already run in step 1. Refer to its output to fill in the 🤖 rows in the Architecture Rules Enforcement table. Mark each as ✅ (no violations) or ❌ (violations — list them in the Violations column).

Rules automatically covered by the script:
- **1.1 / 1.6** UI files with `data.(remote|local|repository)` imports → §1a
- **1.4** UI files importing DTO/Entity/Request/Response types → §1b
- **1.5** UI files calling `ApiService.*` or DAO directly → §1c §1d
- **2.6** ViewModel importing Retrofit / Room / calling ApiService → §2a §2b §2c
- **2.9** ViewModel importing `data.(remote|local)` packages → §2d
- **3.1** Domain files importing `android.*` / `androidx.*` → §3a
- **3.2** Domain files importing `ui.*` → §3e
- **3.3** Domain files importing `retrofit2.*` → §3b
- **3.4** Domain files importing `androidx.room.*` → §3c
- **3.5** Domain files importing `data.*` → §3d
- **4.1** Non-data-layer files importing DTO/Entity → §4a
- **4.2** Data-layer files referencing `UiState` → §4b
- **5.3** ViewModel with ≥3 `StateFlow<Boolean>` → §5a
- **2.5 / 5.4** Permanent state fields named `showDialog`, `navigateTo`, etc. → §5b
- **6.1** Domain files importing DTO types → §6b
- **6.3** UI files importing DTO/ApiModel types → §6a
- **7.2** RepositoryImpl missing `@Singleton` → §7b
- **3.1 / 7.4** Domain constructors receiving `Context` → §7a
- **8.1** Fully-qualified class names used inline → §8a
- **8.2** `enqueue` / `execute` / `await` in ViewModel bodies → §8b
- **8.3** `when/if` on domain model fields inside `@Composable` → §8c
- **8.4** ViewModel without matching test file → §8d
- **9.1–9.4** Misplaced ViewModel / UseCase / RepositoryImpl / Mapper files → §9a–d

**Step 2 — Evaluate remaining rules (Evaluator rules)**

For each changed source file, read the code and evaluate the following rules that the script cannot check:
- **1.2** No business rules (sorting, filtering, validation logic) inside Composable or Fragment
- **1.3** UI never parses or interprets API response fields directly
- **2.1** Each ViewModel has one primary `StateFlow<*UiState>` — not multiple independent streams
- **2.2** ViewModel injects domain use cases or repository interfaces — not concrete data implementations
- **2.3** Domain → UI model mapping is invoked in ViewModel or mapper, not inside Composables
- **2.4** All three states (loading / success / error) are represented in UiState and rendered
- **2.7** ViewModel body contains no Room / file I/O calls
- **2.8** Complex business logic lives in a UseCase, not inline in ViewModel `launch {}` blocks
- **4.3** Data-layer classes contain no NavController references or route strings
- **5.1** Screen renders from a single consolidated UiState — not from multiple scattered streams
- **5.2** `sealed class` used only when screen modes are truly distinct — prefer `data class` with nullable fields
- **6.2** Domain → UI mapping is invoked in Presentation layer only — not inside Composables or data classes
- **6.4** Composable parameters are domain or UI model types — no raw API response objects passed in
- **7.1** All dependencies are provided via Hilt — no manual `= MyRepository()` construction
- **7.3** Hilt modules use `@ViewModelScoped` for ViewModel-bound bindings
- **9.5** Domain → UI mapper files live under `ui/**/mapper/` — not in `domain/`

**Step 3 — Mark unchecked rules**

For any rule in the Architecture Rules Enforcement table that was neither run by the script nor evaluated in Step 2, set the Status to `👁️ Human` in the review report. Rule **8.5** (AI-generated code reviewed before merge) is always `👁️ Human`.

#### 3d. `rules/navigation-rules.md` *(if navigation changed)*
- [ ] Check against navigation rules — record any violations or mark N/A.

#### 3e. `rules/api-contract-rules.md` *(if API or data layer changed)*
- [ ] Check against API contract rules — record any violations or mark N/A.

#### 3f. `rules/analytics-rules.md` *(if analytics events changed)*
- [ ] Check against analytics rules — record any violations or mark N/A.

#### 3g. `gates/review-checklist.md` — full checklist
Work through every item and mark it PASS, FAIL, or N/A. Do not leave items blank.

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

Produce:
- `coding/review/code_review_t<taskId>_v<N>.md` by copying and filling in the template from `docs/templates/code-review-template.md`.

---

## Done When

All conditions must pass before returning to the workflow:

- [ ] `assembleDebug` — exit code 0
- [ ] `ktlintCheck` — exit code 0
- [ ] `detekt` — exit code 0
- [ ] `check-compose-rules.sh` or `check-compose-rules.cmd` — exit code 0 (or skipped with no Compose changes)
- [ ] `check-localization-rules.sh` or `check-localization-rules.cmd` — exit code 0
- [ ] `check-architecture-rules.sh` or `check-architecture-rules.cmd` — exit code 0
- [ ] Compose Rules Enforcement table completed — every rule is ✅, ❌ (acknowledged), ⏭, or `👁️ Human` (no blanks)
- [ ] All `❌` compose rule violations are either fixed or explicitly accepted with justification
- [ ] All compose `👁️ Human` rows acknowledged by the human reviewer before merge
- [ ] Localization Rules Enforcement table completed — every rule is ✅, ❌ (acknowledged), ⏭, or `👁️ Human` (no blanks)
- [ ] All `❌` localization rule violations are either fixed or explicitly accepted with justification
- [ ] All localization `👁️ Human` rows acknowledged by the human reviewer before merge
- [ ] Architecture Rules Enforcement table completed — every rule is ✅, ❌ (acknowledged), ⏭, or `👁️ Human` (no blanks)
- [ ] All `❌` architecture rule violations are either fixed or explicitly accepted with justification
- [ ] All architecture `👁️ Human` rows (including rule 8.5) acknowledged by the human reviewer before merge
- [ ] `navigation-rules.md` — all checks PASS or N/A
- [ ] `api-contract-rules.md` — all checks PASS or N/A
- [ ] `analytics-rules.md` — all checks PASS or N/A
- [ ] `gates/review-checklist.md` — every item marked PASS or N/A
- [ ] UI matches the designed states in `spec_t<taskId>.md`
- [ ] `coding/review/code_review_t<taskId>_v<N>.md` exists with all sections completed and overall verdict filled in
