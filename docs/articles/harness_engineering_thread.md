# Harness Engineering — Conversation Export

## 1. What is Harness Engineering?

Harness engineering, in the context of AI-assisted software development, is the engineering discipline of building the **system around an AI coding agent** so that the agent can reliably perform real software-engineering work.

The model is only one component. The harness determines **what context the model sees, what actions it can take, how work is decomposed, how results are checked, and what happens when something goes wrong**.

A useful mental model is:

> **Model = reasoning engine**  
> **Harness = execution environment + workflow + controls + feedback loop**

For example, telling an agent:

> “Migrate this Android XML screen to Compose.”

is just prompting.

A harness might instead run:

```text
Requirement / Spec
      ↓
Context Builder
      ↓
Planner Agent
      ↓
Implementation Agent
      ↓
Build / Static Analysis
      ↓
Unit + UI Tests
      ↓
Screenshot / Visual Evaluator
      ↓
Code Review Evaluator
      ↓
Pass → Commit
Fail → Diagnose → Retry / Escalate
```

That surrounding machinery is the **harness**.

## 2. Basic Concepts of a Harness System

A production-grade coding harness can be divided into roughly **nine fundamental concepts**.

### 2.1 Source of Truth

The agent needs an authoritative description of what “correct” means.

Typical sources include:

```text
spec.md
architecture.md
design-system.md
API schema
acceptance criteria
security requirements
coding conventions
existing tests
```

Without a source of truth, the agent tends to infer requirements from the existing code, which can reproduce legacy mistakes.

Example structure:

```text
/specs
  feature.md
  acceptance-criteria.md
  ui-spec.yaml
  api-contract.yaml

/architecture
  android.md
  ios.md
  security.md
```

The harness should explicitly tell the agent which artifacts have higher authority:

```text
Product Spec
   >
Architecture Rules
   >
Design System
   >
Existing Code
```

### 2.2 Context Engineering

An agent should not simply receive the entire repository.

The harness determines the **minimum useful context** for the task.

Example:

```text
Task:
Change login error handling

Context builder selects:

AuthViewModel.kt
AuthRepository.kt
LoginScreen.kt
ApiError.kt
auth-api.yaml
architecture/auth.md
related unit tests
```

Good context engineering usually includes:

- repository maps
- dependency graphs
- code search
- symbol search
- relevant specs
- historical decisions
- related tests
- previous failures

### 2.3 Task Decomposition and Planning

Large tasks should normally not be sent as one giant instruction.

Example:

```text
Migrate Profile screen to Compose
```

becomes:

```text
1. Analyze existing XML behavior
2. Extract UI states
3. Identify design-system components
4. Create Compose screen
5. Preserve ViewModel contract
6. Add Compose tests
7. Compare screenshots
8. Run regression tests
```

A good harness makes tasks **small enough to verify independently**.

### 2.4 Tools and Execution Environment

Typical tools include:

```text
read_file
search_code
edit_file
run_gradle
run_xcodebuild
run_tests
launch_simulator
capture_screenshot
git_diff
git_commit
query_issue_tracker
```

The harness controls what the agent can do:

```text
Agent
 ├── Can read source
 ├── Can modify feature module
 ├── Can run tests
 ├── Can run emulator
 ├── Cannot access production
 ├── Cannot modify signing configuration
 └── Cannot push directly to main
```

### 2.5 Guardrails

Guardrails define what an agent **may and may not do**.

Architectural guardrails:

```text
UI → ViewModel → UseCase → Repository

UI must not call Repository directly.
```

Security guardrails:

```text
Never log:
- access tokens
- refresh tokens
- passwords
- payment information
```

Change-scope guardrails:

```text
Allowed:
/feature/profile/**
/tests/profile/**

Protected:
/auth/**
/payment/**
/buildSrc/**
```

Operational guardrails:

```text
No:
git push --force
production deployment
certificate modification
database deletion
```

Important rules should be **machine-enforced**, not merely written in a prompt.

### 2.6 Evaluators

Evaluators answer:

> “Did the agent actually solve the problem correctly?”

Example:

```text
                    Agent output
                         │
        ┌────────────────┼────────────────┐
        ↓                ↓                ↓
     Build Eval       Test Eval       Lint Eval
        │                │                │
        └────────────────┼────────────────┘
                         ↓
                   Security Eval
                         ↓
                    UI / Visual Eval
                         ↓
                    Spec Evaluator
```

For mobile engineering:

| Evaluator | Checks |
|---|---|
| Compiler | Does it build? |
| Unit tests | Logic correctness |
| UI tests | User interactions |
| Screenshot evaluator | Visual fidelity |
| Accessibility evaluator | Labels, contrast, touch targets |
| Architecture evaluator | Layer boundaries |
| Security evaluator | Unsafe API/token/logging patterns |
| Spec evaluator | Acceptance criteria |
| Regression evaluator | Existing behavior |

A key rule:

> **The implementation agent should not be the only judge of its own work.**

### 2.7 Feedback and Repair Loop

Failures should become **structured feedback**.

Example:

```text
Implementation
      ↓
Tests
      ↓
FAIL

Expected:
LoginError.InvalidCredentials

Actual:
LoginError.Unknown

Relevant test:
LoginViewModelTest.kt:183
      ↓
Diagnosis agent
      ↓
Patch
      ↓
Tests again
```

Core loop:

```text
PLAN
 ↓
ACT
 ↓
VERIFY
 ↓
FAIL ───────┐
 ↑          │
 └─ REPAIR ←┘

PASS
 ↓
DONE
```

Possible retry policy:

```text
Attempt 1:
Agent repairs independently

Attempt 2:
Give more repository context

Attempt 3:
Use stronger reasoning model

Attempt 4:
Escalate to human
```

### 2.8 State and Memory

Long-running agents need memory beyond the current prompt.

Task state:

```text
Completed:
✓ Repository analysis
✓ API mapping
✓ ViewModel migration

Pending:
○ Compose UI
○ screenshot tests
```

Repository knowledge:

```text
This project uses:
MVI
Compose
Hilt
Coroutines
Navigation Compose
```

Failure knowledge:

```text
Previous issue:
Using collectAsState() caused lifecycle bug.

Preferred:
collectAsStateWithLifecycle()
```

This can become a **regression knowledge base**.

### 2.9 Observability

Record:

```text
Task
Model
Prompt/context
Files read
Files modified
Commands executed
Tests executed
Evaluator results
Retries
Token usage
Execution time
Final diff
```

Useful metrics include:

```text
Task success rate
First-pass success rate
Average retries
Regression rate
Human intervention rate
Cost per successful task
Time per successful task
```

## 3. Minimal Harness Architecture

```text
                ┌───────────────┐
                │ Requirement   │
                └───────┬───────┘
                        ↓
                ┌───────────────┐
                │ Context       │
                │ Builder       │
                └───────┬───────┘
                        ↓
                ┌───────────────┐
                │ Planner       │
                └───────┬───────┘
                        ↓
                ┌───────────────┐
                │ Coding Agent  │
                └───────┬───────┘
                        ↓
       ┌────────────────┼─────────────────┐
       ↓                ↓                 ↓
     Build            Tests           Static Checks
       └────────────────┼─────────────────┘
                        ↓
                ┌───────────────┐
                │ Evaluator     │
                └───────┬───────┘
                        │
               ┌────────┴────────┐
               ↓                 ↓
             PASS               FAIL
               │                 │
               ↓                 ↓
            Commit          Diagnose/Repair
                                 │
                                 └──────→ Agent
```

A simple useful harness can already be:

```text
spec
  ↓
coding agent
  ↓
build
  ↓
tests
  ↓
lint
  ↓
git diff review
```

## 4. Prompt Engineering vs Context Engineering vs Harness Engineering

```text
Prompt engineering
      ↓
How should I ask the model?

Context engineering
      ↓
What information should the model receive?

Harness engineering
      ↓
What system should surround the model
so that it can repeatedly accomplish the task safely?
```

A broader view:

```text
Agent Engineering
│
├── Model selection
├── Prompt engineering
├── Context engineering
├── Tool engineering
├── Harness engineering
├── Evaluation
└── Observability
```

---

# Validation, Testing, and Oracles

One of the hardest parts of harness engineering is the **failure → repair → re-validation loop**.

The central problem is:

> **“Tests pass” is not equivalent to “the AI implemented the right thing.”**

A harness needs an independent definition of correctness—an **oracle**—and then multiple validators that compare the implementation against that oracle.

## 5. Validate Against What?

Ideally, **not against the code generated by the AI**.

Use an independent chain:

```text
             SOURCE OF TRUTH
                    │
                    ↓
              Acceptance criteria
                    │
         ┌──────────┼──────────┐
         ↓          ↓          ↓
      Tests      Invariants   Visual spec
         │          │          │
         └──────────┼──────────┘
                    ↓
               VALIDATORS
                    │
                    ↓
             Generated code
```

Example requirement:

```text
"When the user taps Pay:
- amount must be > 0
- invalid amount shows inline error
- valid amount calls payment API once
- loading indicator appears
- double tap cannot create two payments
- success navigates to receipt
- network failure stays on screen and shows retry"
```

This gives the harness something concrete to validate.

---

## 6. Five Levels of Validation

### 6.1 Level 1 — Structural Correctness

Question:

> Can this code exist safely in our repository?

Deterministic checks:

```text
compile
lint
format
dependency rules
architecture rules
API compatibility
forbidden API checks
security scans
```

Example:

```text
./gradlew compileDebugKotlin
./gradlew lint
./gradlew detekt
./gradlew test
```

Harness decision:

```text
Any failure → hard FAIL
```

### 6.2 Level 2 — Behavioral Correctness

Question:

> Given input X, does the system produce behavior Y?

A critical harness rule:

> **Don't let the same coding agent invent both the implementation and the only test for that implementation.**

Bad pattern:

```text
Requirement:
Expired session should force re-authentication.
```

Agent misunderstands:

```text
Expired session → silently refresh token
```

Then writes:

```kotlin
@Test
fun expiredSession_refreshesToken() {
   ...
}
```

Implementation passes. Test passes. Feature is still wrong.

Better architecture:

```text
                 Feature spec
                 /          \
                /            \
               ↓              ↓
       Implementation     Test Planner
           Agent              Agent
               │              │
               ↓              ↓
             Code       Test scenarios
               │              │
               └───────┬──────┘
                       ↓
                    Execute
```

The test planner should derive tests from:

```text
spec
acceptance criteria
API contract
business rules
historical bugs
architecture rules
```

### 6.3 Level 3 — Scenario / Journey Correctness

Unit tests won't catch many mobile defects.

Example journey:

```text
Login → MFA → Home
```

Maintain a relatively small set of **critical journeys**.

Example:

```text
Authentication
├── normal login
├── invalid credentials
├── MFA
├── expired session
└── logout

Payment
├── successful payment
├── rejected payment
├── network timeout
└── duplicate payment prevention

Billing
├── view bills
├── download bill
└── empty state
```

Possible tooling:

```text
Compose UI tests
XCUITest
Appium
Maestro
```

### 6.4 Level 4 — Invariant Testing

Define properties that **must always remain true**.

Authentication invariants:

```text
I1: unauthenticated user cannot access protected API
I2: access token must never appear in logs
I3: logout removes local session
I4: expired credentials never grant access
```

Payment invariants:

```text
P1: one user action ≤ one financial transaction
P2: amount sent to API = amount confirmed by user
P3: unsuccessful payment cannot show success state
P4: payment cannot occur without authenticated session
```

From one invariant, generate many scenarios:

```text
Invariant:
A submitted payment cannot execute twice.

Generate variants:

tap once
tap twice rapidly
rotate screen during payment
background/foreground
network timeout
retry
API slow response
process recreation
```

Verify:

```text
paymentApi.callCount <= 1
```

### 6.5 Level 5 — Semantic / Experiential Correctness

Deterministic testing is insufficient for some UI and UX properties.

A screen can:

```text
✓ build
✓ button works
✓ navigation works
✓ accessibility node exists
```

but still have:

```text
Wrong spacing
Wrong typography
Wrong hierarchy
Clipped content
Poor loading state
Incorrect responsive behavior
```

Possible UI oracles:

```text
Figma
Pencil design
golden screenshot
design tokens
UI specification
accessibility rules
```

Pipeline:

```text
Generated screen
      ↓
Launch emulator
      ↓
Navigate to state
      ↓
Capture screenshot
      ↓
Compare
```

Possible comparison layers:

1. Deterministic pixel/screenshot comparison
2. Structural UI/accessibility tree comparison
3. Vision-model evaluator

Example structured result:

```yaml
result: FAIL

violations:
  - severity: high
    component: PaymentSummary
    issue: Total amount is visually less prominent than required.

  - severity: medium
    component: CTA
    issue: Bottom margin differs significantly from reference.

score: 0.79
```

---

# Test Quality Validation

## 7. Tests Themselves Also Need Validation

Suppose AI generates 30 tests and all 30 pass.

The harness still needs to know whether those tests are useful.

### 7.1 Requirement Coverage

Map each acceptance criterion to test evidence:

```text
AC-01 → tests?
AC-02 → tests?
AC-03 → tests?
```

Example:

```yaml
acceptance_criteria:

  AC01:
    requirement: Invalid password shows error
    tests:
      - LoginViewModelTest.invalidPassword
      - LoginScreenTest.invalidPasswordDisplaysError

  AC02:
    requirement: Double submit prohibited
    tests: []

  AC03:
    requirement: Session expiration requires login
    tests:
      - SessionTest.expiredSession
```

Harness detects:

```text
AC02 → uncovered

FAIL
```

This is more meaningful than raw code coverage.

### 7.2 Mutation Testing

Mutation testing is one of the strongest techniques for validating AI-generated tests.

Original:

```kotlin
if (amount > 0) {
    submit()
}
```

Mutation:

```kotlin
if (amount >= 0) {
    submit()
}
```

Tests should fail.

If they don't:

```text
Your test suite did not actually protect this behavior.
```

Possible mutations:

```text
> → >=
true → false
remove function call
invert condition
return null
remove authorization check
```

Mutation score:

```text
Mutation score =
killed mutations / total meaningful mutations
```

This can be more valuable than:

```text
Line coverage = 92%
```

### 7.3 Bug Injection / Negative Validation

The harness can intentionally create known bad variants:

```text
Original implementation
       ↓
Fault injector
       ↓
Variant 1: remove validation
Variant 2: bypass auth
Variant 3: duplicate network call
Variant 4: wrong navigation
Variant 5: omit error state
       ↓
Run generated tests
```

If tests cannot detect these faults, they are not strong enough.

### 7.4 Test Evaluator Agent

A separate model can inspect:

```text
spec
test code
coverage map
```

Questions:

```text
What behaviors from the specification are not tested?

Which tests merely verify implementation details?

Which tests could pass if the feature were broken?

Which boundary conditions are missing?

Which failure paths are missing?
```

The output should be **test gaps**, not just a vague quality score.

---

# Independent Roles

## 8. Separate Implementation, Test Planning, and Verification

Avoid:

```text
Coding Agent
     ↓
writes code
     ↓
writes tests
     ↓
runs tests
     ↓
says "looks good"
```

Prefer:

```text
                  SPEC
                 / | \
                /  |  \
               ↓   ↓   ↓
       Test Planner | Requirements Evaluator
                    |
             Implementation Agent
                    |
                    ↓
                Candidate
                    |
          ┌─────────┼─────────┐
          ↓         ↓         ↓
      Tests       Static     Visual
          │         │         │
          └─────────┼─────────┘
                    ↓
             Verification Agent
                    │
             PASS / REPAIR
```

The roles are different:

- **Test planner:** What must be tested?
- **Implementation agent:** How should I build it?
- **Verification agent:** What evidence proves the requirements are satisfied?

---

# AI-Coding Test Pyramid

## 9. A Modified Test Pyramid

Traditional:

```text
       E2E
     Integration
       Unit
```

AI coding harness:

```text
                Human acceptance
                      ▲
                AI semantic eval
                      ▲
             Journey / E2E tests
                      ▲
             Integration / contract
                      ▲
                 Unit tests
                      ▲
           Static deterministic checks
                      ▲
              Compiler / build
```

Surround it with:

```text
        Requirements coverage
               │
               │
    ┌──────────┴──────────┐
    │                     │
Mutation testing     Regression suite
    │                     │
    └──────────┬──────────┘
               │
          Test quality
```

---

# Differential Testing

## 10. “Before vs After” as a Strong Oracle

For migration and refactoring work, the existing application is often a very strong executable oracle.

Examples:

```text
XML → Compose
React Native → Native iOS
UIKit → SwiftUI
```

Comparison model:

```text
OLD APP             NEW APP
   │                   │
   ↓                   ↓
same input          same input
   │                   │
   ↓                   ↓
behavior A          behavior B
   │                   │
   └──── comparison ────┘
```

Compare:

```text
API calls
navigation
state transitions
analytics events
screenshots
accessibility tree
persisted state
error behavior
```

This is a form of **differential testing**.

---

# Example: XML → Compose Migration

## 11. Verification Package

Task:

```text
Migrate AccountSummaryFragment from XML to Compose.
```

Before implementation, create:

```text
/spec
    account-summary.md

/oracle
    states.yaml
    actions.yaml

/golden
    normal.png
    loading.png
    error.png
    zero-balance.png
```

Example `states.yaml`:

```yaml
states:

  normal:
    balance: 125.40
    outstanding: 20.00

  loading:
    api_delay_ms: 5000

  error:
    api_response: 500

  zero_balance:
    balance: 0
```

Validation:

```text
1. Build passes

2. Unit tests
   ViewModel state transitions

3. Contract tests
   API interaction unchanged

4. UI structural tests
   expected semantic elements exist

5. Journey tests
   tap bill → bill detail

6. Screenshot comparison
   old vs new

7. Analytics comparison
   same events

8. Mutation test
   ensure important tests detect changes

9. Requirements evaluator
   every acceptance criterion mapped to evidence
```

---

# Evidence Matrix

## 12. Harness Completion Should Be Evidence-Based

Instead of:

> “All tests passed.”

Require an evidence matrix:

```yaml
verification:

  AC01:
    requirement: Balance displayed correctly
    evidence:
      - AccountViewModelTest.balanceMapping
      - AccountScreenTest.balanceDisplayed
    status: PASS

  AC02:
    requirement: Error allows retry
    evidence:
      - AccountScreenTest.retryAfterFailure
    status: PASS

  AC03:
    requirement: Visual output matches existing app
    evidence:
      - golden/normal.diff
      - golden/error.diff
    score: 0.97
    status: PASS

  AC04:
    requirement: Analytics behavior unchanged
    evidence:
      - analytics-diff.json
    status: PASS
```

Definition of completion:

```text
Not:

"tests passed"

But:

"every requirement has independent verification evidence"
```

---

# Order of Trust

## 13. Evidence Hierarchy

A useful order of trust:

```text
1. Business/spec invariant
        ↓
2. Deterministic executable oracle
        ↓
3. Independent test/eval
        ↓
4. AI evaluator
        ↓
5. Coding agent self-assessment
```

The further down the list, the weaker the evidence.

Use AI-as-judge mainly when no reliable deterministic oracle exists.

---

# Mobile Harness Validation Architecture

## 14. Suggested Architecture

```text
                        SPEC
                          │
                   Acceptance Criteria
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
   Test Generator   Invariant Generator   UI Oracle
        │                 │                 │
        ↓                 ↓                 ↓
   Unit/Integration   Property tests    Golden states
        │                 │                 │
        └─────────────┬───┴─────────────────┘
                      ↓
               Coding Agent
                      ↓
                  Candidate
                      ↓
 ┌──────────┬──────────┬──────────┬───────────┐
 ↓          ↓          ↓          ↓           ↓
Build     Unit      Journey     Visual     Security
 ↓          ↓          ↓          ↓           ↓
 └──────────┴──────────┴────┬─────┴───────────┘
                            ↓
                   Coverage Evaluator
                            ↓
                    Mutation Testing
                            ↓
                   Requirement/Evidence
                         Matrix
                            ↓
                   PASS / REPAIR
```

The most important design principle:

> **Tests are not your source of truth. Tests are evidence.**

The source of truth should be **specifications, business invariants, contracts, designs, and known existing behavior**.

The harness should continuously ask:

> **“What independent evidence proves each requirement?”**

That makes the repair loop far more reliable because failures become specific:

```text
AC-07 has no evidence
mutation M14 survived
visual evaluator found missing empty state
journey J03 regressed
```

rather than:

```text
Please review your work.
```

---

# Game Demo Concept

## 15. Harness Engineer Game

A small interactive game was proposed where the player acts as the **harness controller**.

The player:

- sends an AI coding agent on tasks
- chooses validators
- catches failures
- repairs them
- re-validates
- learns why “tests passed” is not sufficient

The game included missions such as:

### Mission 1 — Login Hotfix

The AI changed login error handling.

Candidate:

```text
AI changed expired-session handling and added 3 unit tests.
It reports: “All tests passed.”
```

Requirements:

```text
R1 — Invalid credentials show an inline error.
R2 — Expired sessions force re-authentication.
R3 — Access tokens must never appear in logs.
```

Hidden defect:

```text
Expired sessions are silently refreshed instead of forcing login.
```

Useful verification gates:

- Build / Compiler
- Unit Tests
- Journey / E2E
- Requirement Coverage
- Invariant Tests
- Mutation Testing
- Visual Evaluator
- Agent Self-Review

The important lesson:

> The result is not simply “a test failed.” Independent evidence contradicted the candidate.

---

# Mario-Style Harness Game Concept

## 16. Coin Quest

The user then requested a **Mario-style platform game** to demonstrate:

- **state**
- **gates**
- **deterministic scripts**

The mission:

> **Collect as many coins as possible.**

The player moves through a platform level and collects coins.

### Harness State

Example game state:

```text
player.alive
coins.count
checkpoint
jump.valid
run.status
```

This demonstrates that a harness maintains explicit state about what is currently true.

### Gates

Example rules:

```text
Gate 1:
coins ≥ 3

Gate 2:
coins ≥ 7 and player alive

Gate 3:
coins ≥ 12 and all prior gates passed
```

A player may physically reach the gate but cannot progress unless the state satisfies the rule.

This illustrates:

> A gate blocks progress until explicit conditions are satisfied.

### Deterministic Scripts

When the player reaches a gate, a script evaluates state.

Example:

```text
$ validate gate_2
coins=6, alive=true, checkpoint=1
RESULT=FAIL
```

Or:

```text
$ validate gate_2
coins=8, alive=true, checkpoint=1
RESULT=PASS
```

The key concept:

> **Same state → same deterministic result every time.**

### Failure and Repair

If the player falls:

```text
$ runtime_check player.alive
RESULT=FAIL
```

The game restores the player to the last valid checkpoint.

This maps naturally to a harness loop:

```text
STATE
  ↓
GATE
  ↓
DETERMINISTIC CHECK
  ↓
PASS → continue
FAIL → restore/repair
  ↓
RE-VALIDATE
```

### Final Validation

At the end:

```text
$ final_validation
coins=14, gates=3/3
SHIP=PASS
```

or:

```text
$ final_validation
coins=9, gates=2/3
SHIP=BLOCKED
```

The game analogy demonstrates an important harness principle:

> The goal is not merely to move forward. The system must maintain valid state and pass every required gate before the run can be accepted.

---

# Core Takeaways

1. **The model is not the harness.**
2. **The harness defines context, tools, rules, state, validation, and recovery.**
3. **State records what is currently true.**
4. **Gates prevent progress unless explicit conditions are satisfied.**
5. **Deterministic scripts provide reproducible pass/fail checks.**
6. **Tests are evidence, not the source of truth.**
7. **Each important requirement should map to independent verification evidence.**
8. **The coding agent should not be the sole judge of its own implementation.**
9. **Mutation testing, invariant testing, and differential testing are especially useful for AI-generated code.**
10. **A repair is not trusted until the same independent validation runs again.**
11. **A strong harness turns vague failures into structured signals that an agent can repair.**
12. **The goal of harness engineering is repeatable, bounded, observable, evidence-based software delivery.**
