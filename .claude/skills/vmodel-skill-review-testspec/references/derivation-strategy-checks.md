# Derivation strategy checks

Mirrors `derivation-strategies.md` on the author side. Walk every case for `type` validity and walk the parent spec for uncovered derivation targets. The `type` enum is closed by schema; absence or out-of-enum value is hard.

## Table of contents

- [check.derivation.type-missing](#checkderivationtype-missing-hard--schema-invariant) — schema invariant, hard
- [check.derivation.type-not-in-enum](#checkderivationtype-not-in-enum-hard--schema-invariant) — schema invariant, hard
- [check.derivation.rbt-uncovered-rule](#checkderivationrbt-uncovered-rule-soft) — DD behaviour rule with no functional case
- [check.derivation.bva-missing-at-boundary](#checkderivationbva-missing-at-boundary-soft) — input range with no boundary case
- [check.derivation.error-path-uncovered](#checkderivationerror-path-uncovered-soft) — DD error matrix row with no case

## check.derivation.type-missing (HARD ★ schema invariant)

**Check that** every case carries a `type` field.

**Reject when** any case is missing `type`.

**Approve when** every case has `type` populated from the closed enum.

**recommended_action:** *"Populate `type` from the closed enum per the matched author skill's derivation-strategy reference."*

## check.derivation.type-not-in-enum (HARD ★ schema invariant)

**Check that** every case `type` value is in the closed enum: `functional` / `boundary` / `error` / `fault-injection` / `property` / `state-transition` / `contract` / `performance` / `security` / `accessibility` / `error-guessing`.

**Reject when** a case `type` is outside the enum (e.g. `smoke`, `regression`, `manual`).

**Approve when** every case `type` is in the enum.

**recommended_action:** *"Choose a `type` from the closed enum. The matched author skill's derivation-strategy reference describes when each applies."*

## check.derivation.rbt-uncovered-rule (soft)

**Check that** when the parent DD declares behaviour rules (`functional` postconditions on a public function, decision-table rows, state-machine transitions), each rule has at least one case of `type: functional` (or `type: state-transition` for transitions) verifying it.

**Reject when** a parent-DD behaviour rule has no functional / state-transition case `verifies` pointing at it.

**Approve when** every behaviour rule maps to ≥ 1 functional case.

**Evidence pattern:** name the parent-DD function or transition; quote the rule; note absence of any case `verifies` resolving to it.

**recommended_action:** *"Derive a functional case per parent-DD behaviour rule. Requirement-Based Testing — one case per rule, minimum."*

## check.derivation.bva-missing-at-boundary (soft)

**Check that** every input range named in the parent DD (precondition value range, ECP partition, numeric domain) has at least one boundary case (`type: boundary`).

**Reject when** an input range exists in the parent DD with no boundary case in this TestSpec.

**Approve when** every input range has ≥ 1 boundary case (typically: at-boundary; one-side-of-boundary; the schema does not prescribe a count).

**Evidence pattern:** name the parent-DD parameter and range; note absence of boundary case for that range.

**recommended_action:** *"Add a boundary case per input range. Boundary Value Analysis pairs with the equivalence partition; the boundary is where defects cluster."*

## check.derivation.error-path-uncovered (soft)

**Check that** every error-matrix row at the parent DD has at least one case of `type: error` (or `type: fault-injection` for upstream-failure modes) verifying it.

**Reject when** a parent-DD error-matrix row has no error / fault-injection case in this TestSpec.

**Approve when** every error-matrix row maps to ≥ 1 case.

**Evidence pattern:** name the parent-DD error-matrix row; note absence of any case `verifies` resolving to it.

**recommended_action:** *"Add an error or fault-injection case per error-matrix row. The matched author skill's dd-traceability reference describes the seam."*

## Cross-link

`per-layer-weight-checks.md` · `case-quality-checks.md` · `dd-traceability-checks.md` (error-matrix → cases) · `quality-bar-gate.md` (Derivation card)
