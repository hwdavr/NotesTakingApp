#!/usr/bin/env bash

set -e

PRODUCT_FILE="docs/product/product.md"
VALIDATOR="scripts/check-feature-lifecycle.sh"

bash "$VALIDATOR" "$PRODUCT_FILE"

fixture_dir=$(mktemp -d "${TMPDIR:-/tmp}/feature-lifecycle-test.XXXXXX")
trap 'rm -rf "$fixture_dir"' EXIT

write_tracker() {
  local target="$1"
  shift
  {
    echo "# Fixture"
    echo "<!-- HARNESS_TRACKER_START -->"
    echo "| ID | Feature | Workspace | Status | Updated | Notes |"
    echo "|---|---|---|---|---|---|"
    printf '%s\n' "$@"
    echo "<!-- HARNESS_TRACKER_END -->"
  } > "$target"
}

expect_failure() {
  local fixture="$1"
  local expected="$2"
  local output
  if output=$(bash "$VALIDATOR" "$fixture" 2>&1); then
    echo "FAIL: validator unexpectedly accepted $fixture" >&2
    exit 1
  fi
  printf '%s\n' "$output" | grep -Fq "$expected" || {
    echo "FAIL: validator did not report '$expected'." >&2
    echo "$output" >&2
    exit 1
  }
}

active_path="[docs/product/2026-08-02-background-object-editing/](2026-08-02-background-object-editing/)"
incomplete_feature_path="[docs/product/2026-07-31 AI-workflow-1/](2026-07-31 AI-workflow-1/)"

write_tracker "$fixture_dir/unknown-status.md" \
  "| feature-a | Feature A | $active_path | Unknown | 2026-08-04 | Fixture |"
expect_failure "$fixture_dir/unknown-status.md" "unsupported status"

write_tracker "$fixture_dir/duplicate-id.md" \
  "| duplicate | Feature A | $active_path | Planning | 2026-08-04 | Fixture |" \
  "| duplicate | Feature B | $active_path | Blocked | 2026-08-04 | Fixture |"
expect_failure "$fixture_dir/duplicate-id.md" "is duplicated"

write_tracker "$fixture_dir/two-active.md" \
  "| feature-a | Feature A | $active_path | In Progress | 2026-08-04 | Fixture |" \
  "| feature-b | Feature B | $active_path | In Progress | 2026-08-04 | Fixture |"
expect_failure "$fixture_dir/two-active.md" "at most one In Progress"

write_tracker "$fixture_dir/missing-path.md" \
  "| feature-a | Feature A | [docs/product/does-not-exist/](does-not-exist/) | Planning | 2026-08-04 | Fixture |"
expect_failure "$fixture_dir/missing-path.md" "points to missing directory"

write_tracker "$fixture_dir/broken-link.md" \
  "| feature-a | Feature A | [docs/product/2026-08-02-background-object-editing/](wrong-target/) | Planning | 2026-08-04 | Fixture |"
expect_failure "$fixture_dir/broken-link.md" "link target must be"

write_tracker "$fixture_dir/outside-product.md" \
  "| feature-a | Feature A | [docs/knowledge/](../knowledge/) | Planning | 2026-08-04 | Fixture |"
expect_failure "$fixture_dir/outside-product.md" "must point under docs/product"

write_tracker "$fixture_dir/complete-with-incomplete-slices.md" \
  "| feature-a | Feature A | $incomplete_feature_path | Complete | 2026-08-04 | Fixture |"
expect_failure "$fixture_dir/complete-with-incomplete-slices.md" "contains non-passing feature slices"

echo "PASS: feature lifecycle validator rejects invalid tracker states."
