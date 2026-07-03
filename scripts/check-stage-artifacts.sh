#!/usr/bin/env bash
# Verifies required stage artifacts exist on disk before advancing a workflow stage.
#
# Usage: bash scripts/check-stage-artifacts.sh <workflow> <stage>
#   workflow: feature-delivery | bug-fixing | api-contract-update | harness-planning | create-ui-and-verify
#   stage:    requirement-analysis | implementation-plan | requirement-capture | slice-planning
#
# Exits 0 if required artifacts are present, 1 otherwise.
# Designed to run on macOS /bin/bash (Bash 3.2) — no mapfile, no arrays with set -u.

set -e

WORKFLOW="${1:-}"
STAGE="${2:-}"
DOCS_DIR="docs/current"

if [ -z "$WORKFLOW" ] || [ -z "$STAGE" ]; then
  echo "Usage: $0 <workflow> <stage>" >&2
  echo "Workflows: feature-delivery, bug-fixing, api-contract-update, harness-planning, create-ui-and-verify" >&2
  echo "Stages: requirement-analysis, implementation-plan, requirement-capture, slice-planning" >&2
  exit 2
fi

if [ ! -d "$DOCS_DIR" ]; then
  echo "FAIL: $DOCS_DIR does not exist — run the stage's skill to produce artifacts first." >&2
  exit 1
fi

require_file() {
  local pattern="$1"
  local label="$2"
  local found
  found=$(find "$DOCS_DIR" -maxdepth 1 -name "$pattern" 2>/dev/null | head -n 1)
  if [ -z "$found" ]; then
    echo "FAIL: no file matching '$pattern' in $DOCS_DIR ($label)." >&2
    exit 1
  fi
  echo "OK: $found"
}

warn_if_missing() {
  local pattern="$1"
  local label="$2"
  local found
  found=$(find "$DOCS_DIR" -maxdepth 1 -name "$pattern" 2>/dev/null | head -n 1)
  if [ -z "$found" ]; then
    echo "WARN: no file matching '$pattern' in $DOCS_DIR ($label) — recommended but not required for this workflow." >&2
  fi
}

case "$WORKFLOW/$STAGE" in
  feature-delivery/requirement-analysis)
    require_file "summary_v*.md" "stage progress tracker"
    require_file "spec_v*.md" "requirement/impact/design spec"
    ;;
  feature-delivery/implementation-plan)
    require_file "implementation_plan_v*.md" "implementation plan"
    require_file "test_plan_v*.md" "test plan"
    ;;
  bug-fixing/requirement-analysis)
    require_file "summary_v*.md" "stage progress tracker"
    require_file "spec_v*.md" "bug context/root cause spec"
    ;;
  bug-fixing/implementation-plan)
    require_file "implementation_plan_v*.md" "fix plan"
    warn_if_missing "test_plan_v*.md" "test plan (required by feature-delivery, optional for bug-fixing)"
    ;;
  api-contract-update/requirement-analysis)
    require_file "summary_v*.md" "stage progress tracker"
    require_file "spec_v*.md" "requirement/impact/design spec"
    ;;
  api-contract-update/implementation-plan)
    require_file "implementation_plan_v*.md" "implementation plan"
    require_file "test_plan_v*.md" "test plan"
    ;;
  harness-planning/requirement-capture)
    require_file "requirement-summary.md" "requirement summary"
    ;;
  harness-planning/slice-planning)
    require_file "feature_list.json" "feature list"
    require_file "sprint-contract.md" "sprint contract"
    require_file "progress.md" "progress tracker"
    ;;
  create-ui-and-verify/*)
    echo "SKIP: create-ui-and-verify has no doc-artifact gates."
    ;;
  *)
    echo "FAIL: unknown workflow/stage '$WORKFLOW/$STAGE'." >&2
    echo "Known workflow/stage pairs:" >&2
    echo "  feature-delivery/requirement-analysis" >&2
    echo "  feature-delivery/implementation-plan" >&2
    echo "  bug-fixing/requirement-analysis" >&2
    echo "  bug-fixing/implementation-plan" >&2
    echo "  api-contract-update/requirement-analysis" >&2
    echo "  api-contract-update/implementation-plan" >&2
    echo "  harness-planning/requirement-capture" >&2
    echo "  harness-planning/slice-planning" >&2
    echo "  create-ui-and-verify/* (no gates)" >&2
    exit 2
    ;;
esac

echo "Stage '$WORKFLOW/$STAGE' artifacts present."
