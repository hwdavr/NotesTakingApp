# Stage 09 — Knowledge Capture

## Purpose
Record decisions, findings, and lessons learned so every piece of institutional knowledge outlives this change.

The article principle: "Every time you discover an agent has made a mistake, you take the time to engineer a solution so that it can never make that mistake again." (Mitchell Hashimoto)

---

## Load
- `skills/documentation-and-adrs/SKILL.md`
- `coding/review/code_review_v<N>.md` (Stage 06 output)
- `coding/review/test_review_v<N>.md` (Stage 08 output)
- `summary.md` (full change history)

---

## Execute

### 1. Architecture decisions
If this change made a significant architectural decision (new pattern, new abstraction, new convention), record it as an ADR in `docs/knowledge/architecture-decisions/` using the format in that folder's README.

Ask: "Did anything in this change require a deliberate architectural trade-off?" If yes, write the ADR.

### 2. Past bugs
If this change fixed a bug, record it in `docs/knowledge/past-bugs/<YYYY-MM-DD>-<slug>.md` using `docs/templates/regression-template.md`.

Ask: "Could the same bug happen again in a different part of the codebase?" If yes, record it.

### 3. Pitfalls
If this change revealed a non-obvious footgun or anti-pattern that is easy to stumble into again, record it in `docs/knowledge/pitfalls/<slug>.md`.

Ask: "Would another developer or agent make the same mistake without this knowledge?" If yes, record it.

### 4. Update rules if needed
If this change revealed that an existing rule was incomplete, incorrect, or missing, update the relevant `rules/*.md` file.

Ask: "Is there a constraint we followed in this change that should be encoded in the rules so it is always enforced?"

### 5. Update shared JSON scenarios
If new or changed scenarios were created, confirm they are committed to `sharedContracts/test-scenarios/` and cross-referenced with the endpoint in `sharedContracts/openapi.yaml`.

### 6. Finalize summary.md
Update `summary.md` to mark all stages complete and record the final state:
```
## Change Summary — <name>

| Stage | Status | Notes |
|-------|--------|-------|
| 01 Requirement, Impact & Design | ✅ Complete | <date> |
| 02 Implementation Plan | ✅ Complete | Approved by user |
| 03 Data Layer | ✅ Complete | |
| 04 Domain Layer | ✅ Complete | |
| 05 UI Layer | ✅ Complete | |
| 06 Code Review | ✅ Complete | APPROVED |
| 07 Testing | ✅ Complete | N tests, X% coverage |
| 08 Test Review | ✅ Complete | APPROVED |
| 09 Knowledge Capture | ✅ Complete | <date> |

## Knowledge Artifacts Produced
- <path> — <description>
```

---

## Output

Zero or more of:
- `docs/knowledge/architecture-decisions/ADR-NNN-<title>.md`
- `docs/knowledge/past-bugs/<YYYY-MM-DD>-<slug>.md`
- `docs/knowledge/pitfalls/<slug>.md`
- Updated `rules/<rule-file>.md`
- Finalized `summary.md`

---

## Gate

**Conditions to pass:**
- [ ] Every question in Execute steps was answered (even if the answer is "not applicable")
- [ ] Any architectural decision is recorded as an ADR (or explicitly marked N/A)
- [ ] Any bug that could recur is recorded in `docs/knowledge/past-bugs/`
- [ ] Any non-obvious pitfall is recorded in `docs/knowledge/pitfalls/`
- [ ] `rules/` files updated if a coding convention changed
- [ ] `summary.md` is finalized with all stages marked ✅ Complete

**APPROVED →** Change is complete. Notify the user that the full 9-stage pipeline is done.
