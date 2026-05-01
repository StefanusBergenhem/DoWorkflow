# Architecture traceability checks (TestSpec ↔ parent Architecture seam, branch only)

When the TestSpec is branch-scoped (non-leaf, non-root), walk the parent Architecture and verify each load-bearing element has at least one corresponding case. Conditional gating: branch-scope only.

## check.architecture-traceability.interface-uncovered (soft)

**Check that** every interface entry in the parent Architecture has at least one integration case in this TestSpec — `type: contract` (or `type: functional` integration-flavoured).

**Reject when** a parent-Architecture interface has no integration case `verifies` resolving to it.

**Approve when** every interface maps to ≥ 1 integration case.

**Evidence pattern:** name the parent-Architecture interface; note absence of any case `verifies` resolving to it.

**recommended_action:** *"Derive an integration case per parent-Architecture interface. Interfaces with Design-by-Contract clauses (preconditions, postconditions, invariants) demand contract cases."*

## check.architecture-traceability.composition-invariant-uncovered (soft)

**Check that** every composition invariant in the parent Architecture's Composition section has at least one cross-child integration case in this TestSpec.

**Reject when** a parent-Architecture composition invariant has no integration case `verifies` resolving to it.

**Approve when** every composition invariant maps to ≥ 1 cross-child case.

**Evidence pattern:** name the composition invariant (or runtime-pattern element it belongs to); note absence.

**recommended_action:** *"Derive a cross-child integration case per composition invariant. Composition invariants describe properties that emerge from the runtime pattern; they live at branch only."*

## check.architecture-traceability.quality-attribute-unallocated (soft)

**Check that** every quality-attribute allocation to a child or interface in the parent Architecture has at least one specialised case at branch — `type: performance` / `security` / `accessibility` / etc.

**Reject when** a parent-Architecture QA allocation (e.g. *"latency budget 200 ms allocated to component X"*) has no specialised case in this TestSpec.

**Approve when** every QA allocation maps to ≥ 1 specialised case at branch.

**Evidence pattern:** name the QA allocation and its parent-Architecture target; note absence.

**recommended_action:** *"Derive a specialised case per QA allocation. The matched author skill's architecture-traceability reference covers seam derivation."*

## Cross-link

`derivation-strategy-checks.md` (per-strategy coverage) · `verifies-traceability-checks.md` (granularity per layer) · `integration-and-system-checks.md` (contract-testing presence) · `quality-bar-gate.md` (Architecture-traceability card) · `dd-traceability-checks.md` (sibling seam at leaf) · `requirements-traceability-checks.md` (sibling seam at root)
