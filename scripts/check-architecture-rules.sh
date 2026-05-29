#!/usr/bin/env bash
# =============================================================================
# check-architecture-rules.sh
#
# Checks Kotlin source files for violations of the project's
# android-architecture.md constraints. Uses ripgrep (rg) when available,
# falls back to grep -P otherwise.
#
# Usage:
#   ./scripts/check-architecture-rules.sh [--all] [<source-root>]
#
# <source-root> defaults to app/src/main/java
#
# Exit codes:
#   0 — no violations found
#   1 — one or more violations found
# =============================================================================

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# ---------- argument parsing --------------------------------------------------
SCAN_ALL=false
if [[ "${1:-}" == "--all" ]]; then
    SCAN_ALL=true
    shift
fi

SOURCE_ROOT="${1:-$PROJECT_ROOT/app/src/main/java}"

# Derived layer roots (absolute paths, used for scoping per-layer checks)
BASE_PKG="$SOURCE_ROOT/com/example/notesapp"
UI_ROOT="$BASE_PKG/ui"
DOMAIN_ROOT="$BASE_PKG/domain"
DATA_ROOT="$BASE_PKG/data"

# ---------- file collection ---------------------------------------------------
kt_files=()
if [[ "$SCAN_ALL" == "true" ]]; then
    mapfile -t kt_files < <(find "$SOURCE_ROOT" -name "*.kt" -type f)
else
    if git rev-parse --is-inside-work-tree &>/dev/null; then
        mapfile -t kt_files < <(
            {
                git diff --name-only --diff-filter=d HEAD 2>/dev/null
                git diff --name-only --cached --diff-filter=d 2>/dev/null
                git ls-files --others --exclude-standard 2>/dev/null
            } | grep '\.kt$' | sort -u | sed "s|^|$PROJECT_ROOT/|" | grep "^$SOURCE_ROOT/"
        )
    fi
    if [[ ${#kt_files[@]} -eq 0 ]]; then
        mapfile -t kt_files < <(find "$SOURCE_ROOT" -name "*.kt" -type f)
    fi
fi

# Helper: filter kt_files to those under a given directory
_files_under() {
    local dir="$1"
    local result=()
    for f in "${kt_files[@]}"; do
        if [[ "$f" == "$dir"/* ]]; then
            result+=("$f")
        fi
    done
    echo "${result[@]}"
}

# ---------- colour helpers ----------------------------------------------------
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

# ---------- search back-end ---------------------------------------------------
if command -v rg &>/dev/null; then
    _search() {
        local pattern="$1"; shift
        local rg_args=()
        local files=()
        local excludes=()
        while [[ $# -gt 0 ]]; do
            case "$1" in
                --type)    rg_args+=(--type "$2"); shift 2 ;;
                --type=*)  rg_args+=("$1");         shift   ;;
                --exclude) excludes+=("$2"); rg_args+=(--glob "!$2" --glob "!**/$2"); shift 2 ;;
                --)        shift; files+=("$@");    break   ;;
                *)         rg_args+=("$1");          shift   ;;
            esac
        done
        if [[ ${#excludes[@]} -gt 0 && ${#files[@]} -gt 0 ]]; then
            local filtered_files=()
            local file exclude skip
            for file in "${files[@]}"; do
                skip=false
                for exclude in "${excludes[@]}"; do
                    if [[ "$(basename "$file")" == "$exclude" ]]; then
                        skip=true; break
                    fi
                done
                [[ "$skip" == "false" ]] && filtered_files+=("$file")
            done
            files=("${filtered_files[@]}")
        fi
        [[ ${#files[@]} -eq 0 ]] && return 0
        rg --color never -n "${rg_args[@]}" "$pattern" "${files[@]}" || true
    }
else
    _search() {
        local pattern="$1"; shift
        local grep_args=()
        local files=()
        while [[ $# -gt 0 ]]; do
            case "$1" in
                --type|--type=kotlin) shift; [[ "$1" != --* ]] && shift || true ;;
                --multiline|--pcre2) shift ;;
                --exclude) grep_args+=("--exclude=$2"); shift 2 ;;
                --) shift; files+=("$@"); break ;;
                *) grep_args+=("$1"); shift ;;
            esac
        done
        grep -rn -P --include='*.kt' "${grep_args[@]}" "$pattern" "${files[@]}" 2>/dev/null || true
    }
fi

# ---------- state -------------------------------------------------------------
TOTAL_VIOLATIONS=0

# ---------- helpers -----------------------------------------------------------
_header() {
    echo -e "\n${CYAN}${BOLD}▶ $1${RESET}"
}

_rule_header() {
    echo -e "  ${YELLOW}Rule: $1${RESET}"
}

_print_match() {
    echo -e "    ${RED}$1${RESET}"
}

_run_check() {
    local rule_name="$1"
    local pattern="$2"
    shift 2
    local extra_args=()
    local scoped_files=("${kt_files[@]}")

    # Allow caller to pass --files arr... as final args after --
    local new_args=()
    local in_files=false
    for arg in "$@"; do
        if [[ "$arg" == "--" ]]; then
            in_files=true
        elif [[ "$in_files" == "true" ]]; then
            # caller passes pre-filtered file list; ignore default kt_files
            scoped_files=("$@")
            break
        else
            extra_args+=("$arg")
        fi
    done

    _rule_header "$rule_name"

    local results=""
    if [[ ${#scoped_files[@]} -gt 0 ]]; then
        results=$(_search "$pattern" "${extra_args[@]}" -- "${scoped_files[@]}" 2>/dev/null || true)
    fi

    if [[ -z "$results" ]]; then
        echo -e "    ${GREEN}✓ No violations${RESET}"
    else
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                _print_match "$line"
                (( TOTAL_VIOLATIONS++ ))
            fi
        done <<< "$results"
    fi
}

# Variant that operates on an explicit file list (already filtered by layer)
_run_check_files() {
    local rule_name="$1"
    local pattern="$2"
    shift 2
    # remaining args: [grep/rg flags...] then file list
    local grep_flags=()
    local files=()
    local reading_files=false
    for arg in "$@"; do
        if [[ "$arg" == "--files" ]]; then
            reading_files=true
        elif [[ "$reading_files" == "true" ]]; then
            files+=("$arg")
        else
            grep_flags+=("$arg")
        fi
    done

    _rule_header "$rule_name"

    local results=""
    if [[ ${#files[@]} -gt 0 ]]; then
        results=$(_search "$pattern" "${grep_flags[@]}" -- "${files[@]}" 2>/dev/null || true)
    fi

    if [[ -z "$results" ]]; then
        echo -e "    ${GREEN}✓ No violations${RESET}"
    else
        while IFS= read -r line; do
            if [[ -n "$line" ]]; then
                _print_match "$line"
                (( TOTAL_VIOLATIONS++ ))
            fi
        done <<< "$results"
    fi
}

# =============================================================================
# PRE-COMPUTE LAYER FILE SETS
# =============================================================================
ui_files=()
domain_files=()
data_files=()
viewmodel_files=()
for f in "${kt_files[@]}"; do
    [[ "$f" == "$UI_ROOT"/* ]]     && ui_files+=("$f")
    [[ "$f" == "$DOMAIN_ROOT"/* ]] && domain_files+=("$f")
    [[ "$f" == "$DATA_ROOT"/* ]]   && data_files+=("$f")
    [[ "$f" == *"/viewmodel/"* ]]  && viewmodel_files+=("$f")
done

# =============================================================================
# CHECKS
# =============================================================================

echo -e "\n${BOLD}======================================================${RESET}"
echo -e "${BOLD}  Architecture Rules Checker — $(date '+%Y-%m-%d %H:%M:%S')${RESET}"
echo -e "${BOLD}======================================================${RESET}"
echo -e "  Source root : ${SOURCE_ROOT}"
echo -e "  Files scanned: ${#kt_files[@]}"
echo -e "    UI files    : ${#ui_files[@]}"
echo -e "    Domain files: ${#domain_files[@]}"
echo -e "    Data files  : ${#data_files[@]}\n"

# =============================================================================
# SECTION 1 — UI LAYER VIOLATIONS
# =============================================================================
_header "1 · UI Layer — Forbidden Imports & Direct Data Access"
echo -e "  ${YELLOW}UI must not import data-layer classes, call repositories, or call Retrofit directly.${RESET}"

# 1a. UI importing data layer packages
_rule_header 'UI files importing data layer packages (data.remote / data.local / data.repository)'
ui_import_data_violations=()
for f in "${ui_files[@]}"; do
    if grep -qP 'import com\.example\.notesapp\.data\.(remote|local|repository)\.' "$f" 2>/dev/null; then
        ui_import_data_violations+=("$f")
    fi
done
if [[ ${#ui_import_data_violations[@]} -eq 0 ]]; then
    echo -e "    ${GREEN}✓ No violations${RESET}"
else
    for f in "${ui_import_data_violations[@]}"; do
        # Show the offending import lines
        while IFS= read -r line; do
            [[ -n "$line" ]] && _print_match "$line" && (( TOTAL_VIOLATIONS++ ))
        done < <(grep -nP 'import com\.example\.notesapp\.data\.(remote|local|repository)\.' "$f" 2>/dev/null || true)
    done
fi

# 1b. UI importing DTO types from the project's own data layer (ApiModels, *Dto, *Entity, *Request, *Response)
if [[ ${#ui_files[@]} -gt 0 ]]; then
    _run_check_files \
        'UI files importing project DTO / Entity / Request / Response types' \
        'import com\.example\.notesapp\.data\.(remote|local)\.(ApiModels|ApiItem|.*Dto|.*Entity|.*Request|.*Response)' \
        --type kotlin --pcre2 \
        --files "${ui_files[@]}"
fi

# 1c. UI calling Retrofit (service) directly
if [[ ${#ui_files[@]} -gt 0 ]]; then
    _run_check_files \
        'UI Composable calling Retrofit API service directly' \
        'ApiService\s*\.' \
        --type kotlin \
        --files "${ui_files[@]}"
fi

# 1d. UI calling Room DAO directly
if [[ ${#ui_files[@]} -gt 0 ]]; then
    _run_check_files \
        'UI Composable calling Room DAO directly' \
        '\bDao\b.*\.(get|insert|update|delete|query)\b' \
        --type kotlin \
        --files "${ui_files[@]}"
fi

# 1e. Repository called directly inside @Composable function bodies
if [[ ${#ui_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Composable calling repository or use case directly (not via ViewModel)' \
        '(?s)@Composable\b[^{]*fun\s+\w+[^{]*\{[^}]*(Repository|UseCase|DataSource)\s*\.' \
        --multiline --pcre2 --type kotlin \
        --files "${ui_files[@]}"
fi

# =============================================================================
# SECTION 2 — PRESENTATION LAYER VIOLATIONS
# =============================================================================
_header "2 · Presentation Layer — ViewModel Forbidden Imports"
echo -e "  ${YELLOW}ViewModels must not import Retrofit, Room, or data-source implementation classes.${RESET}"

# 2a. ViewModel importing Retrofit
if [[ ${#viewmodel_files[@]} -gt 0 ]]; then
    _run_check_files \
        'ViewModel importing Retrofit classes' \
        'import retrofit2\.' \
        --type kotlin \
        --files "${viewmodel_files[@]}"
fi

# 2b. ViewModel importing Room / DAO directly
if [[ ${#viewmodel_files[@]} -gt 0 ]]; then
    _run_check_files \
        'ViewModel importing Room / DAO classes' \
        'import androidx\.room\.|import.*\.(.*Dao)\b' \
        --type kotlin \
        --files "${viewmodel_files[@]}"
fi

# 2c. ViewModel calling Retrofit API service directly
if [[ ${#viewmodel_files[@]} -gt 0 ]]; then
    _run_check_files \
        'ViewModel calling Retrofit API service directly' \
        '\bApiService\s*\.\s*(get|post|put|patch|delete|create|fetch|update)\b' \
        --pcre2 --type kotlin \
        --files "${viewmodel_files[@]}"
fi

# 2d. ViewModel importing data-layer implementation packages
if [[ ${#viewmodel_files[@]} -gt 0 ]]; then
    _run_check_files \
        'ViewModel importing data-layer implementation (remote/local) packages' \
        'import com\.example\.notesapp\.data\.(remote|local)\.' \
        --type kotlin \
        --files "${viewmodel_files[@]}"
fi

# =============================================================================
# SECTION 3 — DOMAIN LAYER VIOLATIONS
# =============================================================================
_header "3 · Domain Layer — Android Framework Imports"
echo -e "  ${YELLOW}Domain layer must stay platform-independent — no Android SDK, Retrofit, or Room imports.${RESET}"

# 3a. Domain importing Android framework
if [[ ${#domain_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Domain file importing Android framework classes (android.* / androidx.*)' \
        'import (android\.|androidx\.)' \
        --type kotlin \
        --files "${domain_files[@]}"
fi

# 3b. Domain importing Retrofit
if [[ ${#domain_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Domain file importing Retrofit' \
        'import retrofit2\.' \
        --type kotlin \
        --files "${domain_files[@]}"
fi

# 3c. Domain importing Room
if [[ ${#domain_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Domain file importing Room' \
        'import androidx\.room\.' \
        --type kotlin \
        --files "${domain_files[@]}"
fi

# 3d. Domain importing data layer
if [[ ${#domain_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Domain file importing data-layer implementation classes' \
        'import com\.example\.notesapp\.data\.' \
        --type kotlin \
        --files "${domain_files[@]}"
fi

# 3e. Domain importing UI classes
if [[ ${#domain_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Domain file importing UI layer classes' \
        'import com\.example\.notesapp\.ui\.' \
        --type kotlin \
        --files "${domain_files[@]}"
fi

# =============================================================================
# SECTION 4 — DATA LAYER VIOLATIONS
# =============================================================================
_header "4 · Data Layer — DTO Exposure & UI State Logic"
echo -e "  ${YELLOW}DTOs / Entities must not leak into presentation or UI layers.${RESET}"

# 4a. Any non-data-layer file importing DTO / Entity types
# Exclude di/ (Hilt modules legitimately wire data-layer classes) and data/ itself
non_data_files=()
for f in "${kt_files[@]}"; do
    [[ "$f" != "$DATA_ROOT"/* && "$f" != "$BASE_PKG/di/"* ]] && non_data_files+=("$f")
done

if [[ ${#non_data_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Non-data-layer file (outside di/) importing DTO / Entity from data.remote or data.local' \
        'import com\.example\.notesapp\.data\.(remote|local)\.' \
        --type kotlin \
        --files "${non_data_files[@]}"
fi

# 4b. Data layer containing UiState references
if [[ ${#data_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Data layer file referencing UiState (UI state logic must not live in data layer)' \
        '\bUiState\b' \
        --type kotlin \
        --files "${data_files[@]}"
fi

# =============================================================================
# SECTION 5 — STATE MANAGEMENT VIOLATIONS
# =============================================================================
_header "5 · State Management"
echo -e "  ${YELLOW}Each screen must render from a single UiState. No scattered boolean flags.${RESET}"

# 5a. Multiple separate StateFlow<Boolean> fields in a single ViewModel (heuristic)
_rule_header 'ViewModel with multiple StateFlow<Boolean> properties (scattered boolean flag smell)'
bool_flow_violations=()
for f in "${viewmodel_files[@]}"; do
    count=$(grep -cP 'StateFlow\s*<\s*Boolean\s*>' "$f" 2>/dev/null || true)
    if [[ "$count" =~ ^[0-9]+$ ]] && [[ "$count" -ge 3 ]]; then
        bool_flow_violations+=("${f#$PROJECT_ROOT/} (${count} StateFlow<Boolean>)")
    fi
done
if [[ ${#bool_flow_violations[@]} -eq 0 ]]; then
    echo -e "    ${GREEN}✓ No violations${RESET}"
else
    for v in "${bool_flow_violations[@]}"; do
        _print_match "$v"
        (( TOTAL_VIOLATIONS++ ))
    done
fi

# 5b. One-off events stored as permanent state fields (Boolean/String UiState fields named isShown/isVisible)
if [[ ${#viewmodel_files[@]} -gt 0 ]]; then
    _run_check_files \
        'One-off event stored as a permanent UiState field (use Channel/SharedFlow instead)' \
        'val\s+(showDialog|showToast|showSnackbar|navigateTo|isNavigating|navigationEvent)\s*[=:]' \
        --type kotlin --pcre2 \
        --files "${viewmodel_files[@]}"
fi

# =============================================================================
# SECTION 6 — MAPPING RULES
# =============================================================================
_header "6 · Mapping Rules — DTO-to-UI shortcuts"
echo -e "  ${YELLOW}Mapping must only happen in the correct layer: DTO→Domain in data, Domain→UI in presentation.${RESET}"

# 6a. UI files containing project-specific DTO/ApiModel type references
if [[ ${#ui_files[@]} -gt 0 ]]; then
    _run_check_files \
        'UI file referencing project DTO / ApiModel / Entity types directly' \
        'import com\.example\.notesapp\.data\.(remote|local)\.(ApiItem|ApiModels|MutationResultDto|.*Dto|.*Entity)' \
        --type kotlin --pcre2 \
        --files "${ui_files[@]}"
fi

# 6b. Domain files referencing project-specific DTO types
if [[ ${#domain_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Domain file referencing project DTO / ApiModel / Entity types' \
        'import com\.example\.notesapp\.data\.(remote|local)\.(ApiItem|ApiModels|.*Dto|.*Entity)' \
        --type kotlin --pcre2 \
        --files "${domain_files[@]}"
fi

# =============================================================================
# SECTION 7 — DEPENDENCY INJECTION
# =============================================================================
_header "7 · Dependency Injection — Hilt Scoping"
echo -e "  ${YELLOW}Singletons must be @Singleton, ViewModel deps @ViewModelScoped. Context must not leak into domain/data.${RESET}"

# 7a. Context injected into domain layer
if [[ ${#domain_files[@]} -gt 0 ]]; then
    _run_check_files \
        'Domain class receiving Context as constructor / inject parameter' \
        '(fun\s+\w+|constructor)\s*\([^)]*\bContext\b' \
        --type kotlin --pcre2 \
        --files "${domain_files[@]}"
fi

# 7b. Missing @Singleton on repository implementations
_rule_header 'RepositoryImpl missing @Singleton annotation (should be app-scoped)'
repo_impl_files=()
for f in "${data_files[@]}"; do
    [[ "$(basename "$f")" == *RepositoryImpl* ]] && repo_impl_files+=("$f")
done
missing_singleton=()
for f in "${repo_impl_files[@]}"; do
    if ! grep -qP '@Singleton' "$f" 2>/dev/null; then
        missing_singleton+=("${f#$PROJECT_ROOT/}")
    fi
done
if [[ ${#missing_singleton[@]} -eq 0 ]]; then
    echo -e "    ${GREEN}✓ No violations${RESET}"
else
    for v in "${missing_singleton[@]}"; do
        _print_match "$v"
        (( TOTAL_VIOLATIONS++ ))
    done
fi

# =============================================================================
# SECTION 8 — FORBIDDEN PATTERNS
# =============================================================================
_header "8 · Forbidden Patterns"
echo -e "  ${YELLOW}Global rules that must never be violated in any file.${RESET}"

# 8a. Fully-qualified class names used inline (not in import statements)
_run_check \
    'Fully-qualified class name used inline (use import at top of file instead)' \
    '(?<!import )(com\.example\.\w+(\.\w+){3,}|io\.mockk\.\w+|retrofit2\.\w+|androidx\.\w+\.\w+)\s*[(<{]' \
    --type kotlin --pcre2 \
    --exclude 'build.gradle.kts'

# 8b. ViewModel calling Retrofit directly (any file)
_run_check \
    'Direct Retrofit API call in ViewModel (must go through repository/use case)' \
    'class\s+\w+ViewModel[^{]*\{[^}]*\.\s*(enqueue|execute|await)\s*\(' \
    --type kotlin --multiline --pcre2

# 8c. Business rules inside Composable
_run_check \
    'Calculation / business logic branch inside @Composable (if/when on domain models)' \
    '(?s)@Composable\b[^{]*fun\s+\w+[^{]*\{[^}]*(when\s*\(\s*\w+\s*\)\s*\{|if\s*\([^)]*\.(status|state|type|role)\b)' \
    --type kotlin --multiline --pcre2

# 8d. Adding feature code without test file (heuristic — ViewModel without matching *Test.kt)
_rule_header 'ViewModels missing a corresponding *Test.kt or *IntegrationTest.kt'
test_root="$PROJECT_ROOT/app/src/test"
missing_tests=()
for f in "${viewmodel_files[@]}"; do
    vm_name="$(basename "$f" .kt)"
    if ! find "$test_root" -name "${vm_name}Test.kt" -o -name "${vm_name}IntegrationTest.kt" 2>/dev/null | grep -q .; then
        missing_tests+=("${f#$PROJECT_ROOT/}")
    fi
done
if [[ ${#missing_tests[@]} -eq 0 ]]; then
    echo -e "    ${GREEN}✓ No violations${RESET}"
else
    for v in "${missing_tests[@]}"; do
        _print_match "$v (no matching *Test.kt or *IntegrationTest.kt found)"
        (( TOTAL_VIOLATIONS++ ))
    done
fi

# =============================================================================
# SECTION 9 — PACKAGE STRUCTURE
# =============================================================================
_header "9 · Package Structure — Misplaced Files"
echo -e "  ${YELLOW}Files must reside in their canonical layer folder.${RESET}"

# 9a. ViewModel files NOT inside a viewmodel/ folder
_rule_header 'ViewModel class files placed outside a viewmodel/ folder'
vm_misplaced=()
for f in "${kt_files[@]}"; do
    if grep -qP '^\s*class\s+\w+ViewModel\b' "$f" 2>/dev/null; then
        if [[ "$f" != *"/viewmodel/"* ]]; then
            vm_misplaced+=("${f#$PROJECT_ROOT/}")
        fi
    fi
done
if [[ ${#vm_misplaced[@]} -eq 0 ]]; then
    echo -e "    ${GREEN}✓ No violations${RESET}"
else
    for v in "${vm_misplaced[@]}"; do
        _print_match "$v"
        (( TOTAL_VIOLATIONS++ ))
    done
fi

# 9b. UseCase files NOT inside a usecase/ folder
_rule_header 'UseCase class files placed outside a usecase/ folder'
uc_misplaced=()
for f in "${kt_files[@]}"; do
    if grep -qP '^\s*class\s+\w+UseCase\b' "$f" 2>/dev/null; then
        if [[ "$f" != *"/usecase/"* ]]; then
            uc_misplaced+=("${f#$PROJECT_ROOT/}")
        fi
    fi
done
if [[ ${#uc_misplaced[@]} -eq 0 ]]; then
    echo -e "    ${GREEN}✓ No violations${RESET}"
else
    for v in "${uc_misplaced[@]}"; do
        _print_match "$v"
        (( TOTAL_VIOLATIONS++ ))
    done
fi

# 9c. RepositoryImpl files NOT inside a data/repository/ folder
_rule_header 'RepositoryImpl class files placed outside data/repository/ folder'
repo_misplaced=()
for f in "${kt_files[@]}"; do
    if grep -qP '^\s*class\s+\w+RepositoryImpl\b' "$f" 2>/dev/null; then
        if [[ "$f" != "$DATA_ROOT/repository/"* ]]; then
            repo_misplaced+=("${f#$PROJECT_ROOT/}")
        fi
    fi
done
if [[ ${#repo_misplaced[@]} -eq 0 ]]; then
    echo -e "    ${GREEN}✓ No violations${RESET}"
else
    for v in "${repo_misplaced[@]}"; do
        _print_match "$v"
        (( TOTAL_VIOLATIONS++ ))
    done
fi

# 9d. Mapper files in wrong layer (DTO→Domain mapper must be in data/, Domain→UI mapper must be in ui/)
_rule_header 'DTO→Domain mapper placed outside data/ layer'
for f in "${kt_files[@]}"; do
    fname="$(basename "$f")"
    # Files named *Mapper.kt in data layer are fine; in domain layer is a smell
    if [[ "$fname" == *Mapper* ]] && [[ "$f" == "$DOMAIN_ROOT"/* ]]; then
        _print_match "${f#$PROJECT_ROOT/} (mapper belongs in data/ or ui/, not domain/)"
        (( TOTAL_VIOLATIONS++ ))
    fi
done
echo -e "    ${GREEN}✓ Check complete${RESET}" 2>/dev/null || true

# =============================================================================
# SUMMARY
# =============================================================================
echo ""
echo -e "${BOLD}======================================================${RESET}"
if [[ $TOTAL_VIOLATIONS -eq 0 ]]; then
    echo -e "${GREEN}${BOLD}  ✓ All architecture rules passed — 0 violations${RESET}"
    echo -e "${BOLD}======================================================${RESET}"
    exit 0
else
    echo -e "${RED}${BOLD}  ✗ $TOTAL_VIOLATIONS violation(s) found — see above${RESET}"
    echo -e "${BOLD}======================================================${RESET}"
    exit 1
fi
