#!/usr/bin/env bash
set -uo pipefail
export PATH="$HOME/.local/bin:/opt/homebrew/bin:/usr/local/bin:$PATH"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROMPT_FILE="$ROOT_DIR/scripts/.harness-prompt.tmp"
PRIMARY_CMD="codex --yolo"
FALLBACK_CMD="agy --dangerously-skip-permissions -i"
PRIMARY_NAME="codex"
FALLBACK_NAME="agy"
QUOTA_PATTERN="Individual.quota|quota.reached|rate.limit|429|RESOURCE_EXHAUSTED|token.*(exhaust|exceed|run.out|depleted)|insufficient.*(quota|credit|balance)|usage.*limit|daily.*limit"
CAN_FALLBACK="1"

cd "$ROOT_DIR" || exit 1
PROMPT="$(cat "$PROMPT_FILE")"
rm -f "$PROMPT_FILE"

echo ">>> Running ${PRIMARY_NAME}..."
stderr_file=$(mktemp -t harness-agent-stderr.XXXXXX)
exit_code=0
$PRIMARY_CMD "$PROMPT" 2>"$stderr_file" || exit_code=$?

if [ "$exit_code" -ne 0 ] && [ "$CAN_FALLBACK" -eq 1 ]; then
  err_text=$(cat "$stderr_file" 2>/dev/null || true)
  if echo "$err_text" | grep -qiE "$QUOTA_PATTERN"; then
    echo ""
    echo ">>> ${PRIMARY_NAME} quota exhausted. Switching to ${FALLBACK_NAME}..."
    echo "    Error: $(echo "$err_text" | head -3)"
    echo ""
    rm -f "$stderr_file"
    exec $FALLBACK_CMD "$PROMPT"
  fi
fi
rm -f "$stderr_file"
exit "$exit_code"
