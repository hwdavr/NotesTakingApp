# Stage — Data Layer

> **Routing**: When this stage is complete, return to the active **workflow** file to determine the next stage.

## Purpose
Implement data layer changes: DTOs, Room entities, DAOs, repository implementations, and API mappers.
Implement in small, working slices. Verify the build passes before proceeding.

---

## Load
- `rules/android-architecture.md`
- `rules/api-contract-rules.md`
- `docs/current/implementation_plan_adhoc.md` (Implementation Plan stage output)

---

## Execute

### 1. API / DTO changes
If the API contract changed:
1. Update `sharedContracts/openapi.yaml` to reflect the new contract — do this first
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

### 2. Room / local data changes
If local storage is affected:
1. Create or modify Room entity in `data/local/`
2. Update DAO with new query methods
3. **Increment `AppDatabase` version**
4. Add a migration — only use `fallbackToDestructiveMigration` if data loss is explicitly acceptable and stated in the plan

### 3. Repository implementation
1. Implement or update the repository method
2. Map DTO → Domain model inside the repository — **never pass DTOs to upper layers**
3. Translate API errors to domain errors before they leave this layer
4. If caching is needed, follow the existing cache-first or network-first pattern in the codebase

### 4. Mapper
1. Create or update mapper in `data/mapper/` or the relevant `ApiMappers.kt`
2. Map every field explicitly — no reflection, no structural mapping
3. Handle null and fallback values defensively: `dto.field ?: defaultValue`
4. If a new field is optional in the API but required in the domain, define a sensible default

---

## Output

- Updated / created DTO files
- Updated / created Room entity, DAO, and migration files
- Updated / created repository implementation
- Updated / created mapper
- Updated `sharedContracts/openapi.yaml` (if API changed)

Produce `coding/coding_report_adhoc.md`:
```
## Coding Report — Data Layer — v<N>

### Files Changed
| File | Action | Notes |
|------|--------|-------|

### Key Decisions
<any non-obvious implementation choices>

### Known Gaps
<anything intentionally deferred>
```

Update `summary_adhoc.md`: mark the Data Layer stage complete.

---

## Gate

**Conditions to pass — all must be mechanically verifiable:**
- [ ] `sharedContracts/openapi.yaml` matches the new contract (if API changed)
- [ ] No DTOs referenced outside the data layer
- [ ] All new enum fields have an `UNKNOWN` / fallback variant
- [ ] Room schema version incremented and migration added (if schema changed)
- [ ] Repository method returns domain models, not DTOs
- [ ] Build passes: `./gradlew assembleDebug`

**APPROVED →** Return to the active workflow file and proceed to the next stage defined there.

**REVISION REQUIRED →** fix the data layer issue identified, re-run `assembleDebug`, re-evaluate gate.
**Rollback trigger:** If build fails due to a missing mapper field or DTO mismatch, fix in this stage — do not proceed with a broken build.
