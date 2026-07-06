# Portable Regex in Architecture Checker Scripts

## Pitfall

Do not use direct `grep -P` calls in repository rule-check scripts. macOS/BSD grep may not support `-P`, and scripts can silently misclassify valid code when stderr is redirected.

## Symptom

`scripts/check-architecture-rules.sh` reported:

```text
RepositoryImpl missing @Singleton annotation
```

for `NoteRepositoryImpl.kt` even though the file had `@Singleton` directly above the class.

## Cause

The script used direct `grep -qP` checks outside its `rg`-backed search helper. On environments without PCRE grep support, the command returned non-zero and the rule interpreted that as "annotation missing."

## Fix

Use portable extended regex for direct grep checks:

```bash
grep -qE '@Singleton' "$f"
grep -qE '^[[:space:]]*class[[:space:]]+[[:alnum:]_]+RepositoryImpl\b' "$f"
```

## Prevention

- Prefer `rg` for regex-heavy checks.
- If using `grep` directly in shared scripts, use POSIX/BSD-compatible options and character classes.
- Avoid redirecting regex-engine errors in a way that turns tool incompatibility into a false rule violation.
