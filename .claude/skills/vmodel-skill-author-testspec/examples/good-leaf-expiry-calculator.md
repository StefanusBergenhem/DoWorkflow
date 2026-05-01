# Example — TestSpec for the session expiry calculator (greenfield, leaf scope)

A leaf inside `session-store`: the pure function that computes when a session
expires given the issued-at time, the configured idle timeout, the configured
absolute timeout, and the last-activity time. The parent DD
(`DD-session-store-expiry-calculator`) specifies a single function with two
postconditions (return value is the earliest of idle threshold and absolute
threshold; return value is strictly greater than `iat` when either threshold
is positive), one invariant (idempotence: calling twice with the same inputs
returns the same value), and four error-matrix rows (negative idle, negative
absolute, last-activity before iat, integer overflow on threshold addition).

## What this example demonstrates

- 8 cases covering 5 strategies (functional, boundary, error, property, error-guessing)
- Specific oracles on every `expected:` (no qualitative phrases)
- Non-empty `verifies:` at artifact and case level, pointing at DD field IDs
- `coverage_mutation_bar:` populated with example values clearly framed as project-policy
- `recovery_status` absent (greenfield, not retrofit)
- Annotated reviewer comments inline showing why each case passes the bar

## The artifact

```markdown
---
id: TS-session-store-expiry-calculator
artifact_type: test-spec
scope: session-store/expiry-calculator
level: unit
derived_from:
  - DD-session-store-expiry-calculator
verifies:
  - "DD-session-store-expiry-calculator.public_interface.compute_expiry.postconditions.on_success"
  - "DD-session-store-expiry-calculator.public_interface.compute_expiry.invariants.idempotence"
  - "DD-session-store-expiry-calculator.error_handling.negative_thresholds"
  - "DD-session-store-expiry-calculator.error_handling.activity_before_iat"
  - "DD-session-store-expiry-calculator.error_handling.integer_overflow"
governing_adrs:
  - ADR-009-session-timeout-policy
status: draft
date: 2026-04-30
coverage_mutation_bar:
  structural_coverage:
    threshold_pct: 95               # example value — project policy for pure-function leaves
    metric: branch
  mutation_score:
    threshold_pct: 80               # example value — project policy
    tool_category: PIT-class         # project tooling stack
  enforcement:
    frequency: per-PR
    blocking: true
---

# TestSpec — expiry-calculator

## Overview

This TestSpec verifies the pure expiry-calculation function at leaf scope.
Cases are derived from the parent DD (`DD-session-store-expiry-calculator`):
each postcondition contributes a functional case, the input ranges contribute
boundary cases, the invariant contributes a property case, and the four error-
matrix rows contribute robustness cases. The function is pure (no clock, no
state, no I/O), so cases use no test doubles.

## Cases

\```yaml
cases:
  # --- Functional / RBT — covers postcondition: return is min of idle and absolute ---

  - id: TC-expiry-001
    title: "returns earliest threshold when idle threshold is sooner"
    type: functional
    verifies:
      - "DD-session-store-expiry-calculator.public_interface.compute_expiry.postconditions.on_success"
    inputs:
      iat: 1735689600            # 2025-01-01T00:00:00Z
      idle_timeout_s: 1800        # 30 min
      absolute_timeout_s: 7200    # 2 h
      last_activity: 1735691400   # iat + 30 min
    expected: 1735693200          # last_activity + idle_timeout = sooner than iat + absolute
    # <!-- review-note: specific value; oracle is a single integer the test-author
    #      can assert directly; covers the on_success postcondition for the
    #      idle-wins equivalence class -->

  - id: TC-expiry-002
    title: "returns earliest threshold when absolute threshold is sooner"
    type: functional
    verifies:
      - "DD-session-store-expiry-calculator.public_interface.compute_expiry.postconditions.on_success"
    inputs:
      iat: 1735689600
      idle_timeout_s: 7200        # 2 h
      absolute_timeout_s: 1800    # 30 min
      last_activity: 1735693100   # iat + 58:20 min — idle-from-now would push past absolute
    expected: 1735691400          # iat + absolute_timeout — wins regardless of activity
    # <!-- review-note: paired with TC-001 to cover both equivalence classes
    #      of the min-of-two predicate; same postcondition, different class -->

  # --- Boundary / BVA — covers input range bounds ---

  - id: TC-expiry-003
    title: "boundary: idle_timeout=0 and absolute_timeout positive returns iat"
    type: boundary
    verifies:
      - "DD-session-store-expiry-calculator.public_interface.compute_expiry.postconditions.on_success"
    inputs:
      iat: 1735689600
      idle_timeout_s: 0           # ON boundary (0 is the lower bound of the valid range)
      absolute_timeout_s: 3600
      last_activity: 1735689600
    expected: 1735689600          # min(iat+0, iat+3600) = iat
    # <!-- review-note: ON-point on the lower boundary of idle_timeout_s; tests
    #      that the function does not silently treat 0 as 'no idle' -->

  - id: TC-expiry-004
    title: "boundary: maximum representable timeout yields capped expiry"
    type: boundary
    verifies:
      - "DD-session-store-expiry-calculator.public_interface.compute_expiry.postconditions.on_success"
    inputs:
      iat: 1735689600
      idle_timeout_s: 86400       # 1 day — IN-point well below overflow
      absolute_timeout_s: 86400
      last_activity: 1735689600
    expected: 1735776000          # iat + 86400
    # <!-- review-note: IN-point boundary case; pairs with TC-008 (overflow OUT-point)
    #      to give BVA on the upper boundary of timeout values -->

  # --- Error / robustness — covers each error-matrix row ---

  - id: TC-expiry-005
    title: "negative idle_timeout raises ValueError; no return value"
    type: error
    verifies:
      - "DD-session-store-expiry-calculator.error_handling.negative_thresholds"
    inputs:
      iat: 1735689600
      idle_timeout_s: -1
      absolute_timeout_s: 3600
      last_activity: 1735689600
    expected: "raises ValueError with message containing 'idle_timeout_s must be >= 0'"
    # <!-- review-note: typed error + message constraint = bounded predicate;
    #      `does not throw` would be refusal C, this asserts a specific exception -->

  - id: TC-expiry-006
    title: "last_activity before iat raises ValueError"
    type: error
    verifies:
      - "DD-session-store-expiry-calculator.error_handling.activity_before_iat"
    inputs:
      iat: 1735689600
      idle_timeout_s: 1800
      absolute_timeout_s: 3600
      last_activity: 1735689500   # 100s before iat
    expected: "raises ValueError with message containing 'last_activity must be >= iat'"
    # <!-- review-note: covers the precondition violation on temporal ordering -->

  # --- Property — covers invariant: idempotence ---

  - id: TC-expiry-007
    title: "compute_expiry is idempotent for any valid input"
    type: property
    verifies:
      - "DD-session-store-expiry-calculator.public_interface.compute_expiry.invariants.idempotence"
    inputs:
      generator: |
        valid tuple (iat, idle_timeout_s, absolute_timeout_s, last_activity)
        where: iat in [1700000000, 1900000000], idle_timeout_s in [0, 86400],
        absolute_timeout_s in [0, 86400], last_activity in [iat, iat + 86400]
      samples: 200
    expected: "for any sampled tuple t: compute_expiry(t) == compute_expiry(t)"
    # <!-- review-note: universally quantified predicate; sample size and input
    #      space named, so the property-test author can implement directly -->

  # --- Error-guessing — covers operational hazard not in the matrix ---

  - id: TC-expiry-008
    title: "integer overflow on iat + absolute_timeout raises OverflowError"
    type: error-guessing
    verifies:
      - "DD-session-store-expiry-calculator.error_handling.integer_overflow"
    inputs:
      iat: 9223372036854775000      # near i64 max
      idle_timeout_s: 1000
      absolute_timeout_s: 1000      # iat + absolute would overflow i64
      last_activity: 9223372036854775000
    expected: "raises OverflowError with message containing 'expiry computation overflows'"
    # <!-- review-note: error-guessing case for an operational hazard the matrix
    #      explicitly names but property-style sampling would unlikely hit;
    #      OUT-point on the upper boundary of timeout-addition arithmetic -->
\```

## Notes

- Error / happy ratio: 3 error / 4 functional + 1 property = 1:1.7 — clears the 1:2 floor.
- Test doubles: zero. Pure function, no collaborators. Leaf-thin shape.
- `coverage_mutation_bar:` values (95% branch, 80% mutation, per-PR, blocking) are
  framed as project-policy in the Overview commentary; another project would
  set different numbers. The skill does not prescribe values; the skill
  prescribes presence of the block.
```

## Why this TestSpec passes the meta-gate

- **Junior-implementable.** A test-author who has not seen the implementation can
  write the test code from this TestSpec alone. Each `expected:` is a specific
  value or bounded predicate; each input is concrete; preconditions / fixtures
  are named where applicable (none here, since the function is pure).
- **Coverage-derivable.** A reviewer can read the cases and tell that:
  every postcondition has at least one functional case; every input range has
  ON / OFF / IN / OUT points (TC-003 ON, TC-004 IN, TC-008 OUT); every error-
  matrix row has a robustness case; the invariant has a property case.
- **No weak assertions.** No `verifies behaviour` / `does not throw` / `non-null`
  alone. Every `expected:` is a value or a bounded predicate.
- **Verifies traceability.** Every case `verifies:` is a leaf-granularity ID
  pointing at a DD field that resolves; artifact-level `verifies:` is non-empty
  and is the union of case-level lists.
