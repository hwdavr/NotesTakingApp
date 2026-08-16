#!/usr/bin/env bash
# Ensures every final visual verification command is declared in the sprint
# contract and has successful connected-test evidence before a feature passes.

set -e

FEATURE_DIR="${1:-}"
MODE="${2:---evaluate}"

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

if [ -z "$FEATURE_DIR" ]; then
  echo "Usage: bash scripts/check-visual-evidence-contract.sh <feature-directory> [--planning|--evaluate]" >&2
  exit 2
fi
[ "$MODE" = "--planning" ] || [ "$MODE" = "--evaluate" ] || fail "mode must be --planning or --evaluate"

FEATURE_JSON="$FEATURE_DIR/feature_list.json"
CONTRACT="$FEATURE_DIR/sprint-contract.md"
[ -f "$FEATURE_JSON" ] || fail "missing $FEATURE_JSON"
[ -f "$CONTRACT" ] || fail "missing $CONTRACT"

VISUAL_OWNER_COUNT=$(jq '[.features[]? | select(.requires_visual_verification == true)] | length' "$FEATURE_JSON")
if [ "$VISUAL_OWNER_COUNT" -eq 0 ]; then
  echo "PASS: no visual-verification owner is declared."
  exit 0
fi
[ "$VISUAL_OWNER_COUNT" -eq 1 ] || fail "feature_list.json must declare exactly one visual-verification owner"

VISUAL_OWNER=$(jq -r '.features[] | select(.requires_visual_verification == true) | .id' "$FEATURE_JSON")
CONTRACT_ROWS=$(grep -E "^\|[[:space:]]*TC-${VISUAL_OWNER}-VIS-[^|[:space:]]+[[:space:]]*\|" "$CONTRACT" || true)
[ -n "$CONTRACT_ROWS" ] || fail "visual-verification owner $VISUAL_OWNER has no visual rows in sprint-contract.md"

CONTRACT_IDS=$(printf '%s\n' "$CONTRACT_ROWS" | sed -n 's/^|[[:space:]]*\(TC-[^|[:space:]]*-VIS-[^|[:space:]]*\)[[:space:]]*|.*/\1/p')
[ -n "$CONTRACT_IDS" ] || fail "visual rows for $VISUAL_OWNER have no parseable Test IDs"

FEATURE_IDS=$(jq -r --arg owner "$VISUAL_OWNER" '
  .features[]
  | select(.id == $owner)
  | (.acceptance_test_ids // [])[]
  | select(test("-VIS-"))
' "$FEATURE_JSON")

for test_id in $CONTRACT_IDS; do
  printf '%s\n' "$FEATURE_IDS" | grep -Fxq "$test_id" \
    || fail "$test_id is declared in sprint-contract.md but missing from feature_list.json acceptance_test_ids"

  EVIDENCE_COUNT=$(jq --arg owner "$VISUAL_OWNER" --arg id "$test_id" '
    [
      .features[]
      | select(.id == $owner)
      | (.evidence // [])[]
      | select(.test_id == $id and .exit_status == 0 and ((.executed_command // "") | contains("connectedDebugAndroidTest")))
    ] | length
  ' "$FEATURE_JSON")
  if [ "$MODE" = "--evaluate" ]; then
    [ "$EVIDENCE_COUNT" -gt 0 ] \
      || fail "$test_id has no successful connected-test evidence in feature_list.json"
  fi
done

for test_id in $FEATURE_IDS; do
  printf '%s\n' "$CONTRACT_IDS" | grep -Fxq "$test_id" \
    || fail "$test_id is in feature_list.json but missing from sprint-contract.md"
done

VISUAL_COMMANDS=$(jq -r --arg owner "$VISUAL_OWNER" '
  .features[]
  | select(.id == $owner)
  | (.verification // [])[]
  | select(test("connectedDebugAndroidTest") and test("testInstrumentationRunnerArguments.class=.*#"))
' "$FEATURE_JSON")
VISUAL_METHODS=""
while IFS= read -r command; do
  [ -n "$command" ] || continue
  method=$(printf '%s\n' "$command" | sed -n 's/.*#\([^[:space:]\"]*\).*/\1/p')
  [ -n "$method" ] || continue
  VISUAL_METHODS="$VISUAL_METHODS
$method"
  printf '%s\n' "$CONTRACT_ROWS" | grep -Fq "$method" \
    || fail "visual verification method $method is not named by a $VISUAL_OWNER visual row"
done <<EOF
$VISUAL_COMMANDS
EOF

CONTRACT_METHODS=$(printf '%s\n' "$CONTRACT_ROWS" | sed -n 's/.*#\([^`|[:space:]]*\).*/\1/p')
[ -n "$CONTRACT_METHODS" ] || fail "visual rows for $VISUAL_OWNER have no parseable test methods"
for method in $CONTRACT_METHODS; do
  printf '%s\n' "$VISUAL_METHODS" | grep -Fxq "$method" \
    || fail "visual contract method $method is not listed in feature_list.json verification"
done

echo "PASS: visual verification commands, sprint-contract rows, acceptance IDs, and connected evidence are aligned."
