# ADR purpose and shape — checks

Threshold-and-orphan checks for the second sweep step. The ADR-worthy threshold is **all three** of: load-bearing AND ≥2 real options AND contingent on changeable assumptions. Failing any one triggers a finding.

## When the ADR fails the threshold

When the decision is not load-bearing — changing it later does not break the system or invalidate downstream design.
- **check_failed**: `check.threshold.condition-load-bearing-missing`
- **severity**: `soft_reject`
- **evidence pattern**: quote the Decision; note the absence of any consequence that could break a downstream artifact.
- **recommended_action**: *"Drop the ADR and record the choice inline in Architecture or Detailed Design — non-load-bearing decisions do not warrant an ADR."*

When only one real option was on the table (the other "options" are straw men or "do nothing").
- **check_failed**: `check.threshold.condition-options-missing`
- **severity**: `soft_reject` (also see refusal C `check.alternatives.fewer-than-two-real`)
- **recommended_action**: *"Either surface ≥2 real alternatives, or drop the ADR. Description-of-what-was-done is not a decision."*

When the decision is not contingent on anything that may change — no revisit trigger possible.
- **check_failed**: `check.threshold.condition-contingency-missing`
- **severity**: `soft_reject`
- **recommended_action**: *"Either name the assumptions that may change (becoming revisit triggers), or drop the ADR."*

## Routine-choice ADR — refusal E

When the ADR captures a naming convention, import organisation, method signature, or directory layout choice. Steady-state cadence test: more than 1–2 ADRs per scope per sprint suggests routine-choice noise.

- **check_failed**: `check.threshold.routine-choice` (aliases `anti-pattern.routine-choice`)
- **severity**: `hard_reject` ★ (refusal E)
- **evidence pattern**: quote the Decision; cite refusal E.
- **recommended_action**: *"Drop the ADR; record the convention inline in the relevant Architecture or Detailed Design section."*

## Orphan-ADR check (cross-link)

The orphan-ADR check (`anti-pattern.orphan-adr` / `check.linkage.scope-tags-empty`) is also a hard-reject (broken-reference integrity) but is detected by the linkage sweep — see `linkage-and-lineage-checks.md`.

## Sweep order in this step

1. Read `scope_tags`, body sections, and the Decision section.
2. Apply the threshold-question pass — three conditions, one finding per missing condition.
3. Apply the routine-choice tell.
4. Defer the orphan check to the linkage sweep.

## When inputs are missing

When the ADR's parent Architecture is referenced (e.g., the ADR was extracted from a `[NEEDS-ADR: …]` stub) but the Architecture is not provided as input AND a SAT failure suspects an upstream-traceable cause, halt with `missing-inputs` per SKILL.md HALT condition 4. Otherwise proceed; the threshold check is self-contained at this layer.
