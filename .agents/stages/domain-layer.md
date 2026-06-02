# Stage — Domain Layer

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
Implement domain layer changes: use cases, domain models, and repository interfaces.
The domain layer must remain platform-independent.

---

## Load
- `rules/android-architecture.md`
- `coding/implementation_plan_adhoc.md` (Implementation Plan stage output)

---

## Execute

### 1. Domain model changes
1. Add or remove fields in domain model data classes
2. Import **no Android framework classes** (`Context`, `Bundle`, SDK types)
3. If an enum is added, include an `UNKNOWN` / fallback variant

### 2. Repository interface changes
1. Add or update method signatures in the repository interface (defined in domain layer)
2. Keep interfaces stable and framework-independent
3. Use `suspend` functions or `Flow` based on existing conventions in the codebase
4. Confirm the interface change matches the implementation added in the Data Layer stage

### 3. Use case changes
1. Create or update use cases — one use case does one thing
2. Use cases may coordinate multiple repository methods but must not call data sources directly
3. Use cases should be easily unit testable with mocked repository implementations
4. Implement business validation, filtering, and decision logic here — not in the ViewModel

**Business logic that belongs in use cases (not ViewModel or Composable):**
- Access permission checks
- Filter / sort logic driven by business rules
- Validation before mutations
- Data combination from multiple repositories

---

## Output

Update `coding/coding_report_adhoc.md` with a Domain Layer section (e.g. `coding_report_t1.md` for Task 1):
```
## Domain Layer Changes

### Files Changed
| File | Action | Notes |
|------|--------|-------|

### Use Case Responsibilities
<describe what each new/modified use case does>

### Interface Contract Changes
<list repository interface changes>
```

Update `summary_adhoc.md`: mark the Domain Layer stage complete.

---

## Gate

**Conditions to pass — all must be mechanically verifiable:**
- [ ] No Android framework classes imported in domain layer files (`grep -r "import android\." domain/`)
- [ ] Repository interfaces updated and match the Data Layer stage implementation
- [ ] Use cases are single-responsibility (one observable outcome per use case)
- [ ] New enum domain models include `UNKNOWN` / fallback variant
- [ ] Build passes: `./gradlew assembleDebug`

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →** fix the domain layer issue, re-run `assembleDebug`, re-evaluate gate.
**Rollback trigger:** If build fails due to a repository interface mismatch, fix the interface contract before proceeding.
