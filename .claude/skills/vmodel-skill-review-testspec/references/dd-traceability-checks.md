# DD traceability checks (TestSpec ↔ parent DD seam, leaf only)

When the TestSpec is leaf-scoped, walk the parent DD and verify each load-bearing element has at least one corresponding case. Conditional gating: leaf-scope only.

## check.dd-traceability.error-matrix-uncovered (soft)

**Check that** every parent-DD error-matrix row has at least one case in this TestSpec — `type: error` (or `type: fault-injection` for upstream-failure rows).

**Reject when** a parent-DD error-matrix row has no robustness case `verifies` resolving to it.

**Approve when** every error-matrix row maps to ≥ 1 case.

**Evidence pattern:** name the parent-DD error-matrix row id; note absence of any case `verifies` resolving to it.

**recommended_action:** *"Derive a robustness case per error-matrix row. Each row corresponds to an observable failure mode the unit must handle."*

## check.dd-traceability.postcondition-uncovered (soft)

**Check that** every load-bearing postcondition (`on_success` and `on_failure` per public function) in the parent DD has at least one contract-test case verifying it.

**Reject when** a parent-DD postcondition has no `type: contract` (or `type: functional`) case `verifies` resolving to it.

**Approve when** every postcondition maps to ≥ 1 case.

**Evidence pattern:** name the parent-DD function and postcondition branch; note absence.

**recommended_action:** *"Derive a contract case per parent-DD postcondition. Postconditions without test cases cannot be verified at the unit boundary."*

## check.dd-traceability.invariant-uncovered (soft)

**Check that** every load-bearing invariant (function invariant, data-structure invariant, state invariant) in the parent DD has at least one case — typically `type: property`.

**Reject when** a parent-DD invariant has no property-test case `verifies` resolving to it.

**Approve when** every invariant maps to ≥ 1 case.

**Evidence pattern:** name the parent-DD invariant; note absence.

**recommended_action:** *"Derive a property case per parent-DD invariant. Property tests close the gap example-based tests leave on universal claims."*

## check.dd-traceability.marker-unresolved (soft)

**Check that** every `[NEEDS-TEST: ...]` marker in the parent DD has been resolved into a case in this TestSpec.

**Reject when** a parent-DD `[NEEDS-TEST: ...]` marker is present and no case in this TestSpec addresses it.

**Approve when** every marker is resolved (the case exists), OR the marker is explicitly out-of-scope for this TestSpec layer (and that is stated).

**Evidence pattern:** quote the unresolved marker; name the parent-DD location.

**recommended_action:** *"Resolve the `[NEEDS-TEST: ...]` marker into a case. Markers are the parent-DD's request for test-coverage; unresolved markers leak into implementation."*

## Cross-link

`derivation-strategy-checks.md` (per-strategy coverage) · `verifies-traceability-checks.md` (granularity per layer) · `quality-bar-gate.md` (DD-traceability card) · `architecture-traceability-checks.md` (sibling seam at branch) · `requirements-traceability-checks.md` (sibling seam at root)
