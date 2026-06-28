# macOS Bash 3 Rule Scripts

## Context

The custom rule scripts were run on macOS, where `/bin/bash` is GNU Bash 3.2. The scripts used `mapfile` and `set -u` with empty arrays, which are not safe on Bash 3.2.

## Symptom

Running scripts such as `bash scripts/check-compose-rules.sh` or `bash scripts/check-architecture-rules.sh` failed before evaluating rules:

- `mapfile: command not found`
- `kt_files[@]: unbound variable`
- `repo_impl_files[@]: unbound variable`

## Fix

Use a portable `while IFS= read -r` helper for file collection and avoid `set -u` in scripts that rely on empty arrays.

## Prevention

When adding shell rule scripts, keep them compatible with macOS `/bin/bash` unless the script explicitly checks for and requires a newer Bash.
