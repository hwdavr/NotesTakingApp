# Rule Violation Suppression Is Not a Fix

**Date**: 2026-06-01

## Problem

Agents resolving project-rule failures may make the check pass by adding suppression directives instead of fixing the root cause.

Examples of suppression-style fixes:
- `@Suppress(...)`
- `@SuppressLint(...)`
- `tools:ignore`
- ktlint or detekt disable comments
- new baselines
- broader excludes in lint, detekt, ktlint, Gradle, or custom scripts

## Required Behavior

Treat suppressions as policy exceptions, not fixes. The default response to a rule failure is to change the code so it satisfies the rule.

If a warning is a real false positive, the agent must stop and ask the user before adding an exception. The request must include:
- the exact rule or check
- the exact file and line
- why the tool result is invalid
- why a code change would be worse than an exception

## Prevention

`scripts/check-architecture-rules.sh` includes a suppression-control section that fails when the current diff adds suppression or ignore directives in app source, Gradle config, detekt config, or editor config.
