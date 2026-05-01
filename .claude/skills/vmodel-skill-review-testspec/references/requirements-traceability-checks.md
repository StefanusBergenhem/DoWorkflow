# Requirements traceability checks (TestSpec ↔ Requirements + Product Brief seam, root only)

When the TestSpec is root-scoped, walk the root Requirements and the Product Brief and verify each load-bearing element has at least one corresponding case at root level. Conditional gating: root-scope only.

## check.requirements-traceability.requirement-unverified (soft)

**Check that** every requirement in the root Requirements document has at least one case across the scope tree — typically at root, but a leaf or branch case may also satisfy it (the cross-tree count is the floor).

**Reject when** a root requirement has no case `verifies` resolving to it across the scope tree (this TestSpec being the most recent visibility into root coverage).

**Approve when** every root requirement has ≥ 1 case at the appropriate layer.

**Evidence pattern:** name the unverified requirement id; note absence.

**recommended_action:** *"Derive a case per root requirement. Root requirements without test coverage cannot be acceptance-tested."*

## check.requirements-traceability.outcome-unverified (soft)

**Check that** every Product Brief outcome statement has at least one user-journey case at root — `type: functional` system-level case in Product Brief vocabulary.

**Reject when** a Product Brief outcome has no journey case `verifies` resolving to it.

**Approve when** every outcome maps to ≥ 1 user-journey case.

**Evidence pattern:** name the Product Brief outcome statement; note absence.

**recommended_action:** *"Derive a user-journey case per Product Brief outcome. Root cases describe what the user observes; Product Brief outcomes describe what the user must observe to call the product successful."*

## check.requirements-traceability.nfr-no-threshold-case (soft)

**Check that** every quality-attribute (NFR) requirement carries a measurable specialised case — `type: performance` / `security` / `accessibility` — with a named threshold drawn from the requirement's five-element measurability slot.

**Reject when** an NFR requirement (e.g. *"the system shall handle 10,000 requests/second at p99 latency under 200 ms"*) has no specialised case naming the threshold and the measurement context.

**Approve when** every NFR has ≥ 1 specialised case naming the threshold.

**Evidence pattern:** name the NFR requirement and its threshold; note absence of specialised case.

**recommended_action:** *"Derive a specialised case naming the NFR's threshold and the measurement context. Five-element NFRs without measurable cases are aspirational, not testable."*

## Cross-link

`derivation-strategy-checks.md` (per-strategy coverage) · `verifies-traceability-checks.md` (granularity per layer) · `integration-and-system-checks.md` (specialised QA cases) · `quality-bar-gate.md` (Requirements-traceability card) · `dd-traceability-checks.md` (sibling seam at leaf) · `architecture-traceability-checks.md` (sibling seam at branch)
