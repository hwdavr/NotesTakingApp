#!/usr/bin/env bash

set -e

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
VALIDATOR="$REPO_ROOT/scripts/check-ui-verification-artifact.sh"
STAGE_VALIDATOR="$REPO_ROOT/scripts/check-stage-artifacts.sh"
fixture_root=$(mktemp -d "${TMPDIR:-/tmp}/ui-verification-artifact-test.XXXXXX")
trap 'rm -rf "$fixture_root"' EXIT

write_valid_fixture() {
  local docs_dir="$1"
  mkdir -p "$docs_dir/design" "$docs_dir/evidence"
  printf 'reference mockup' > "$docs_dir/design/mockup_editor.png"
  printf 'actual screenshot' > "$docs_dir/evidence/editor_actual.png"
  printf '%s\n' \
    '# UI Verification — v1' \
    '' \
    '**Reference design**: `design/mockup_editor.png`' \
    '' \
    '### Reference Anchor Verification' \
    '' \
    '| Screen / State | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |' \
    '|---|---|---|---|---|---|' \
    '| Note editor — focused table | Row handle visual ends on the table grid left border. | `TableHandlesScreenTest#handlesAlignToGridGeometry`; testTag: `table_row_handle_visual` | `rowVisualBounds.right == gridBounds.left ± 2dp` | `evidence/editor_actual.png` | PASS |' \
    '' \
    '### Verdict' \
    'PASS — reference anchors are verified.' \
    > "$docs_dir/ui_verification.md"
}

expect_failure() {
  local expected="$1"
  shift
  local output
  if output=$("$@" 2>&1); then
    echo "FAIL: validator unexpectedly accepted fixture" >&2
    exit 1
  fi
  printf '%s\n' "$output" | grep -Fq "$expected" || {
    echo "FAIL: validator did not report '$expected'." >&2
    printf '%s\n' "$output" >&2
    exit 1
  }
}

valid="$fixture_root/valid"
write_valid_fixture "$valid"
(cd "$REPO_ROOT" && bash "$VALIDATOR" "$valid")
(cd "$REPO_ROOT" && bash "$STAGE_VALIDATOR" create-ui-and-verify ui-verification "$valid")

missing_anchor="$fixture_root/missing-anchor"
write_valid_fixture "$missing_anchor"
sed '/### Reference Anchor Verification/,$d' "$missing_anchor/ui_verification.md" \
  > "$missing_anchor/ui_verification.tmp"
mv "$missing_anchor/ui_verification.tmp" "$missing_anchor/ui_verification.md"
expect_failure "has no '### Reference Anchor Verification' section" \
  bash "$VALIDATOR" "$missing_anchor"

missing_tag="$fixture_root/missing-tag"
write_valid_fixture "$missing_tag"
sed 's/testTag:/boundsTag:/' "$missing_tag/ui_verification.md" > "$missing_tag/ui_verification.tmp"
mv "$missing_tag/ui_verification.tmp" "$missing_tag/ui_verification.md"
expect_failure "must name a visual bounds testTag" bash "$VALIDATOR" "$missing_tag"

missing_screenshot="$fixture_root/missing-screenshot"
write_valid_fixture "$missing_screenshot"
mv "$missing_screenshot/evidence/editor_actual.png" "$missing_screenshot/evidence/editor_actual.missing"
expect_failure "references missing or empty screenshot evidence/editor_actual.png" \
  bash "$VALIDATOR" "$missing_screenshot"

echo "PASS: UI verification artifact validator rejects screenshot-only and unanchored visual evidence."
