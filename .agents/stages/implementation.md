# Stage — Implementation (Data + Domain + UI)

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
Implement the full change across all three layers — Data, Domain, and UI — in a single pass.
Work in small, vertically-sliced increments: implement one layer, verify the build, then proceed to the next.

> This is the **compact implementation stage** used by `feature-delivery` and `bug-fixing` workflows.
> For granular layer-by-layer control, use the individual stages `03-data-layer.md`, `04-domain-layer.md`, and `05-ui-layer.md`.

---

## Load
- `skills/android-ui-verification/SKILL.md`
- `rules/android-architecture.md`
- `rules/api-contract-rules.md`
- `rules/compose-rules.md`
- `rules/navigation-rules.md`
- `rules/analytics-rules.md`
- `rules/localization-rules.md`
- `coding/implementation_plan.md` (Implementation Plan stage output)
- `request_analysis/spec.md` — UiState and API design from the Requirement, Impact & Design Analysis stage

---

## Execute

### Layer 1 — Data Layer
Implement in this order. Run `./gradlew assembleDebug` after this layer before continuing.

#### 1.1 API / DTO changes
If the API contract changed:
1. Update `sharedContracts/openapi.yaml` to reflect the new contract — **do this first**
2. Create or modify DTO data classes in `data/remote/dto/`
3. Use correct nullability: `String?` for optional fields, `String` for required
4. Handle unknown enum values with a fallback variant:
   ```kotlin
   enum class NoteStatus {
       ACTIVE, ARCHIVED, UNKNOWN;
       companion object {
           fun fromString(value: String) = entries.firstOrNull { it.name == value } ?: UNKNOWN
       }
   }
   ```

#### 1.2 Room / local data changes
If local storage is affected:
1. Create or modify Room entity in `data/local/`
2. Update DAO with new query methods
3. **Increment `AppDatabase` version**
4. Add a migration — only use `fallbackToDestructiveMigration` if data loss is explicitly acceptable and stated in the plan

#### 1.3 Repository implementation & Mapper
1. Implement or update the repository method
2. Map DTO → Domain model inside the repository — **never pass DTOs to upper layers**
3. Translate API errors to domain errors before they leave this layer
4. Map every field explicitly — no reflection, no structural mapping
5. Handle null defensively: `dto.field ?: defaultValue`

✅ **Build check**: `./gradlew assembleDebug` must pass before proceeding to Domain Layer.

---

### Layer 2 — Domain Layer
Implement in this order. Run `./gradlew assembleDebug` after this layer before continuing.

#### 2.1 Domain model changes
1. Add or remove fields in domain model data classes
2. Import **no Android framework classes** (`Context`, `Bundle`, SDK types)
3. If an enum is added, include an `UNKNOWN` / fallback variant

#### 2.2 Repository interface changes
1. Add or update method signatures in the repository interface (defined in domain layer)
2. Keep interfaces stable and framework-independent
3. Use `suspend` functions or `Flow` based on existing conventions in the codebase
4. Confirm the interface change matches the Data Layer implementation above

#### 2.3 Use case changes
1. Create or update use cases — one use case does one thing
2. Use cases may coordinate multiple repository methods but must not call data sources directly
3. Implement business validation, filtering, and decision logic here — not in the ViewModel

**Business logic that belongs in use cases (not ViewModel or Composable):**
- Access permission checks
- Filter / sort logic driven by business rules
- Validation before mutations
- Data combination from multiple repositories

✅ **Build check**: `./gradlew assembleDebug` must pass before proceeding to UI Layer.

---

### Layer 3 — UI Layer
Implement in this order.

#### 3.1 ViewModel
1. Expose screen state as `StateFlow<UiState>` — one state object per screen
2. Handle all states: loading, success, empty, error, retry, permission
3. Emit one-off events (navigation, toast, dialog) via a separate `Channel<Event>`
4. Call use cases only — **never call repositories or data sources directly**
5. Do not import `retrofit2.*`, `androidx.room.*`, or any data-layer class

#### 3.2 UI model and mapper
1. Create or update UI model data classes if the domain model needs formatting for display
2. Create or update the Domain → UI mapper in the Presentation layer
3. Do not pass domain models directly to Composables when UI formatting is needed

#### 3.3 Composable screen
1. Split every screen into stateless `Content` + stateful `Screen` wrapper (see `rules/compose-rules.md`)
2. The stateless `Content` Composable receives `UiState` and callbacks — it does not call the ViewModel
3. Use `stringResource()` for all user-visible text — **no hardcoded strings**
4. Add `Modifier.testTag("stable_name")` to all interactive elements and key content areas

#### 3.4 Navigation, Analytics & String resources
1. Update the navigation graph if new routes are added — use serializable argument types only
2. Fire analytics events from the ViewModel — not from Composables
3. Add all new user-visible text to `res/values/strings.xml`

---

## Output

Produce `coding/coding_report_v<N>.md`:
```
## Coding Report — v<N>

### Files Changed
| File | Layer | Action | Notes |
|------|-------|--------|-------|

### Key Decisions
<any non-obvious implementation choices>

### UiState Implemented
<confirm loading / success / empty / error coverage>

### testTags Added
<list key testTag values>

### Known Gaps
<anything intentionally deferred>
```

Update `summary.md`: mark the Implementation (Data + Domain + UI) stage complete.

Update `request_analysis/tasks.md`: mark each completed task item as `[x]`.

---

## Gate

**Conditions to pass — all must be mechanically verifiable:**
- [ ] `sharedContracts/openapi.yaml` updated (if API changed)
- [ ] No DTOs referenced outside the data layer
- [ ] All new enum fields have an `UNKNOWN` / fallback variant
- [ ] Room schema version incremented and migration added (if schema changed)
- [ ] Repository methods return domain models, not DTOs
- [ ] No Android framework classes imported in domain layer (`grep -r "import android\." domain/`)
- [ ] Use cases are single-responsibility
- [ ] ViewModel does not import `retrofit2.*`, `androidx.room.*`, or any data-layer class
- [ ] Composable screens do not contain business logic
- [ ] All user-visible text uses `stringResource()` — no hardcoded strings
- [ ] All interactive elements have `Modifier.testTag(...)` with a stable name
- [ ] UiState covers loading, success, empty, and error states
- [ ] Build passes: `./gradlew assembleDebug`

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →** fix the issue at its layer (Data / Domain / UI), re-run `assembleDebug`, re-evaluate gate.
**Rollback trigger:** Never proceed to the next stage with a failing build.
