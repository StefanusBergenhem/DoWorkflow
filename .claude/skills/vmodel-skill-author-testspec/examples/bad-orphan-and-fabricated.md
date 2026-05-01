# Counter-example — orphan, fabricated retrofit, weak assertions (refuse to ship)

A team retrofits the same `session-store/expiry-calculator` from existing tests
without applying retrofit discipline. They run a half-hearted derivation pass
that copies test method names into `title:`, leaves the artifact-level
`verifies:` empty, uses `expected: "verifies behaviour"` to paper over
under-specified oracles, marks reconstructed `verifies` as `verified` on
every case, and skips the `coverage_mutation_bar:` block entirely.

A retrofit run with insufficient guardrails produces the artifact below.
Every inline `<!-- VIOLATION -->` annotation marks what the matched review
skill's hard-rejects would catch.

## The fabricated artifact (refuse to ship this)

```markdown
---
id: TS-session-store-expiry-calculator
artifact_type: test-spec
scope: session-store/expiry-calculator
level: unit
derived_from:
  - DD-session-store-expiry-calculator
verifies: []
# <!-- VIOLATION (refusal B): artifact-level `verifies:` is empty.
#      anti-pattern.orphan-tests; check.verifies.artifact-level-empty (HARD). -->
governing_adrs: []
status: draft
date: 2026-04-30
# <!-- VIOLATION (derived-hard refusal): `coverage_mutation_bar:` block missing
#      from front-matter entirely.
#      check.coverage-mutation.section-missing (HARD). -->
recovery_status:
  verifies: verified
# <!-- VIOLATION (refusal A): `recovery_status: verified` declared globally
#      on retrofit without confirming any individual reconstructed link with
#      a human. anti-pattern.fabricated-retrofit-intent;
#      check.retrofit.recovery-status-reconstructed-verifies (HARD). -->
---

# TestSpec — expiry-calculator

## Overview

Retrofitted from existing tests in test_expiry.py. The tests were written by
someone who has since left the team. We mapped each test to a derived case
and kept the case names intact for continuity. The current suite verifies
the calculator works correctly across happy-path inputs.
<!-- VIOLATION: "the current suite verifies the calculator works correctly"
     is qualitative spec-laundering. Overview cites tests, not the parent DD.
     This is anti-pattern.test-as-requirement-inversion in narrative form. -->

## Cases

\```yaml
cases:
  - id: TC-expiry-001
    title: "verifies user gets correct expiry timestamp"
    # <!-- VIOLATION (refusal A): `title` is inferred intent on a retrofit case;
    #      derived from a test method name `test_user_gets_correct_expiry`,
    #      not from the parent DD. `# HUMAN-ONLY` until human confirms.
    #      anti-pattern.fabricated-retrofit-intent. -->
    type: functional
    verifies:
      - "DD-session-store-expiry-calculator"
    # <!-- VIOLATION (soft): granularity-mismatched — leaf case should point at
    #      a DD field (`...public_interface.compute_expiry.postconditions...`),
    #      not at the DD root.
    #      check.verifies.granularity-mismatch. -->
    inputs:
      iat: 1735689600
      idle_timeout_s: 1800
      absolute_timeout_s: 7200
      last_activity: 1735691400
    expected: "verifies behaviour"
    # <!-- VIOLATION (refusal C): qualitative non-bounded oracle.
    #      anti-pattern.weak-assertions; check.oracle.weak-assertion (HARD). -->

  - id: TC-expiry-002
    title: "happy path"
    type: functional
    verifies: []
    # <!-- VIOLATION (refusal B): per-case `verifies:` empty.
    #      check.verifies.case-level-empty (HARD). -->
    inputs:
      iat: 1735689600
      idle_timeout_s: 7200
      absolute_timeout_s: 1800
      last_activity: 1735693100
    expected: "non-null"
    # <!-- VIOLATION (refusal C): `non-null` alone is qualitative. The
    #      function returns an integer; "the result satisfies type::int" is
    #      already given by the type system and adds nothing. -->

  - id: TC-expiry-003
    title: "another happy path with different inputs"
    type: functional
    verifies:
      - "DD-session-store-expiry-calculator.public_interface.compute_expiry"
    inputs:
      iat: 1700000000
      idle_timeout_s: 60
      absolute_timeout_s: 60
      last_activity: 1700000000
    expected: "does not throw"
    # <!-- VIOLATION (refusal C): `does not throw` alone is qualitative; the
    #      case observes absence of an exception but does not assert the value
    #      returned. A naive implementation `return 0` would pass. -->

  - id: TC-expiry-004
    title: "edge case from production"
    type: functional
    verifies:
      - "DD-session-store-expiry-calculator.public_interface.compute_expiry"
    inputs:
      iat: 1735689600
      idle_timeout_s: 3600
      absolute_timeout_s: 7200
      last_activity: 1735693200
    expected: 1735696800
    # <!-- soft tell — happy-path-bias: TC-001 through TC-004 are all
    #      happy-path. Zero error / boundary / property cases.
    #      anti-pattern.happy-path-bias; error/happy ratio = 0:4 < 1:2. -->
\```
```

## Tells of fabrication, orphan, and weak assertions

| Tell | Where it appears | What review hard-rejects |
|---|---|---|
| `verifies: []` at artifact level | Front-matter | `check.verifies.artifact-level-empty` (HARD; refusal B; `anti-pattern.orphan-tests`) |
| `verifies: []` on TC-002 | Per-case | `check.verifies.case-level-empty` (HARD; refusal B) |
| `recovery_status: verified` on retrofit without per-link confirmation | Front-matter | `check.retrofit.recovery-status-reconstructed-verifies` (HARD; refusal A) |
| `title: "verifies user gets correct expiry timestamp"` from test method name | TC-001 | `check.retrofit.intent-on-title` (HARD; refusal A) |
| `expected: "verifies behaviour"` | TC-001 | `check.oracle.weak-assertion` (HARD; refusal C) |
| `expected: "non-null"` alone | TC-002 | `check.oracle.weak-assertion` (HARD; refusal C) |
| `expected: "does not throw"` alone | TC-003 | `check.oracle.weak-assertion` (HARD; refusal C) |
| Missing `coverage_mutation_bar:` block | Front-matter | `check.coverage-mutation.section-missing` (HARD; derived-hard refusal) |
| Granularity mismatch — leaf case verifies DD root, not field | TC-001, TC-003, TC-004 | `check.verifies.granularity-mismatch` (soft) |
| All-happy-path mix; zero error / boundary / property | All cases | `anti-pattern.happy-path-bias` (soft); `check.derivation.error-path-uncovered` (soft) |
| Overview narrative laundering tests as spec | Overview | `anti-pattern.test-as-requirement-inversion` (soft) |
| Missing Retrofit Gap Report section | Document | Retrofit context demands the report; absent → `anti-pattern.fabricated-retrofit-intent` confirmed (soft signal) |

## What the honest version looks like

The honest retrofit:

1. Walks the parent DD FIRST and derives cases per the seam (`dd-traceability-cues.md`).
2. Maps each existing test against a derived case, leaving `title:` and `notes:` empty
   (`# HUMAN-ONLY`) until a human supplies intent.
3. Marks every reconstructed `verifies:` as `recovery_status: unknown`.
4. Populates `coverage_mutation_bar:` (with `"TBD-by-project-policy"` placeholders if no
   project policy exists yet — the block must be present, the values may be deferred).
5. Replaces every weak `expected:` with a specific value or bounded predicate, derived
   from the DD's postconditions / invariants — not from re-reading the test code.
6. Ships a Retrofit Gap Report with all four buckets populated (or `(none observed)`).

The honest retrofit reads less smoothly. That is the signal that it is honest —
laundering is what produces the smooth read.
