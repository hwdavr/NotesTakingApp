---
name: knowledge-capture
description: Records ADRs, post-mortems, and learnings after feature or bug resolution.
---

# Skill — Knowledge Capture

## Purpose
Record decisions, findings, and lessons learned so every piece of institutional knowledge outlives this change.

The article principle: "Every time you discover an agent has made a mistake, you take the time to engineer a solution so that it can never make that mistake again." (Mitchell Hashimoto)

---

## Load
- `skills/documentation-and-adrs/SKILL.md`
- `coding/review/code_review_t<taskId>_v<N>.md` (Code Review stage output)
- `coding/review/test_review_t<taskId>_v<N>.md` (Test Review stage output)
- `summary_t<taskId>.md` (full change history)

---

## Execute

### 1. Architecture decisions
If this change made a significant architectural decision (new pattern, new abstraction, new convention), record it as an ADR in `docs/knowledge/architecture-decisions/` using the format in that folder's README.

Ask: "Did anything in this change require a deliberate architectural trade-off?" If yes, write the ADR.

### 2. Past bugs
Only record a bug if it was **non-obvious, hard to diagnose, or systemic** (e.g., subtle race condition, misleading error message, framework gotcha, broad architectural misunderstanding). Routine bugs — typos, simple null-pointer fixes, missing null-checks — do **not** qualify.

If it qualifies, record it in `docs/knowledge/past-bugs/<YYYY-MM-DD>-<slug>.md` using `docs/templates/regression-template.md`.

Ask: "Would a skilled developer waste significant time diagnosing this same bug in the future without this record?" Only if yes, record it.

### 3. Pitfalls
If this change revealed a non-obvious footgun or anti-pattern that is easy to stumble into again, record it in `docs/knowledge/pitfalls/<slug>.md`.

Ask: "Would another developer or agent make the same mistake without this knowledge?" If yes, record it.

### 4. Update rules if needed
If this change revealed that an existing rule was incomplete, incorrect, or missing, update the relevant `rules/*.md` file.

Ask: "Is there a constraint we followed in this change that should be encoded in the rules so it is always enforced?"

### 5. Update shared JSON scenarios
If new or changed scenarios were created, confirm they are committed to `sharedContracts/test-scenarios/` and cross-referenced with the endpoint in `sharedContracts/openapi.yaml`.

### 6. Finalize summary_t<taskId>.md
Update `summary_t<taskId>.md` to mark all stages complete and record the final state:
```
## Change Summary — <name>

> *Ensure the Stage Progress table (generated dynamically based on the active workflow) has all stages marked as ✅ Complete.*

## Knowledge Artifacts Produced
- <path> — <description>
```

### 7. Mark Task Complete in Sliced Plan
If a sliced plan exists in `docs/current/progress.md`:
1. Find the task that you just completed in `docs/current/progress.md`.
2. Update its progress status to ✅ Complete and set its completion date.

---

## Output

Zero or more of:
- `docs/knowledge/architecture-decisions/ADR-NNN-<title>.md`
- `docs/knowledge/past-bugs/<YYYY-MM-DD>-<slug>.md`
- `docs/knowledge/pitfalls/<slug>.md`
- Updated `rules/<rule-file>.md`
- Finalized `summary_t<taskId>.md`
- Updated `docs/current/progress.md` (if a sliced plan is active)

---

## Done When

**This stage is complete when all of the following are true:**
- [ ] Every question in Execute steps was answered (even if the answer is "not applicable")
- [ ] Any architectural decision is recorded as an ADR (or explicitly marked N/A)
- [ ] Any **non-obvious, hard-to-diagnose, or systemic** bug is recorded in `docs/knowledge/past-bugs/` (routine/simple bugs are explicitly excluded)
- [ ] Any non-obvious pitfall is recorded in `docs/knowledge/pitfalls/`
- [ ] `rules/` files updated if a coding convention changed
- [ ] `summary_t<taskId>.md` is finalized with all stages marked ✅ Complete
- [ ] `docs/current/progress.md` is updated to mark the completed task as ✅ Complete (if a sliced plan is active)

**APPROVED →** Change is complete. Notify the user that the workflow pipeline is done.
