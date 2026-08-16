#!/usr/bin/env bash
# Validates the ad-hoc UI verification artifact. A broad screenshot comparison
# cannot prove exact placement, so each approved reference needs concrete
# bounds-based anchor evidence tied to a tagged runtime node.

set -e

DOCS_DIR="${1:-}"

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

if [ -z "$DOCS_DIR" ]; then
  echo "Usage: bash scripts/check-ui-verification-artifact.sh <docs-directory>" >&2
  exit 2
fi

[ -d "$DOCS_DIR" ] || fail "missing artifact directory $DOCS_DIR"

REPORT="$DOCS_DIR/ui_verification.md"
[ -f "$REPORT" ] || fail "missing $REPORT"
grep -Fq "### Reference Anchor Verification" "$REPORT" \
  || fail "$REPORT has no '### Reference Anchor Verification' section"
grep -Fq "| Screen / State | Reference anchor | Runtime proof | Measured relationship | Actual screenshot | Result |" "$REPORT" \
  || fail "$REPORT has no required reference-anchor table header"

REFERENCE_ASSET=$(sed -n 's/^\*\*Reference design\*\*: `\(design\/[^`]*\)`[[:space:]]*$/\1/p' "$REPORT" | head -n 1)
[ -n "$REFERENCE_ASSET" ] \
  || fail "$REPORT must declare one backticked design/ reference asset"
case "$REFERENCE_ASSET" in
  *..*) fail "$REPORT reference asset must stay under design/" ;;
esac
[ -s "$DOCS_DIR/$REFERENCE_ASSET" ] \
  || fail "$REPORT references missing or empty design asset $REFERENCE_ASSET"

ANCHOR_ROWS=$(grep -E '^\|.*\|[[:space:]]*PASS[[:space:]]*\|[[:space:]]*$' "$REPORT" || true)
[ -n "$ANCHOR_ROWS" ] \
  || fail "$REPORT needs at least one passing reference-anchor row"

while IFS= read -r anchor_row; do
  [ -n "$anchor_row" ] || continue
  printf '%s\n' "$anchor_row" | grep -Eq 'testTag:[[:space:]]*`[^`]+`' \
    || fail "$REPORT reference-anchor row must name a visual bounds testTag"
  printf '%s\n' "$anchor_row" | grep -Eq '`[^`]*#[A-Za-z_][A-Za-z0-9_]*`' \
    || fail "$REPORT reference-anchor row must name the runtime test method"
  printf '%s\n' "$anchor_row" | grep -Eq '[A-Za-z]+Bounds(\.[A-Za-z]+)?[[:space:]]*(==|>=|<=|>|<)' \
    || fail "$REPORT reference-anchor row must record a concrete bounds relationship"
  SCREENSHOT_PATH=$(printf '%s\n' "$anchor_row" | grep -oE 'evidence/[[:alnum:]_./-]+\.(png|jpg|jpeg)' | head -n 1 || true)
  [ -n "$SCREENSHOT_PATH" ] \
    || fail "$REPORT reference-anchor row must cite an evidence/ screenshot"
  case "$SCREENSHOT_PATH" in
    *..*) fail "$REPORT screenshot path must stay under evidence/" ;;
  esac
  [ -s "$DOCS_DIR/$SCREENSHOT_PATH" ] \
    || fail "$REPORT references missing or empty screenshot $SCREENSHOT_PATH"
done <<EOF
$ANCHOR_ROWS
EOF

echo "PASS: UI verification artifact includes non-empty reference and actual images plus bounds-based anchor proof."
