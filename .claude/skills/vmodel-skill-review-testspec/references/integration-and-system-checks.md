# Integration and system specifics checks

Branch and root TestSpecs carry integration / system concerns: contract testing, environment shape, specialised cases for quality-attribute requirements, version pinning. Mirrors `integration-and-system-specifics.md` on the author side.

## check.integration.contract-testing-absent (soft)

**Check that** branch TestSpecs declare contract tests for inter-component contracts at the seams the parent Architecture defined.

**Reject when** the parent Architecture declares an interface contract (preconditions / postconditions / invariants) at a child boundary, AND the branch TestSpec has no `type: contract` case verifying that interface.

**Approve when** every parent-Architecture interface has a contract case (consumer-driven OR provider-driven; declaration is the bar).

**Evidence pattern:** name the parent-Architecture interface; note absence of a contract case `verifies` resolving to it.

**recommended_action:** *"Add a `type: contract` case per parent-Architecture interface. Specify whether the contract test is consumer-driven or provider-driven."*

## check.integration.environment-unnamed (soft)

**Check that** every branch and root case declares the environment shape in `preconditions` — in-process / test-containers / shared staging / production-like. Conditional gating: applies at branch and root, not at leaf.

**Reject when** a branch / root case has cross-process or cross-system steps but does not name the environment shape.

**Approve when** the environment is named explicitly, or the case is in-process and that is stated.

**Evidence pattern:** name the case id; quote the precondition without environment naming.

**recommended_action:** *"Name the environment shape (in-process / test-containers / shared staging / production-like) in preconditions. Environment shape drives reproducibility."*

## check.integration.qa-specialised-case-absent (soft)

**Check that** every quality-attribute requirement (perf / sec / a11y / availability) has a specialised case at the appropriate layer — `type: performance` / `type: security` / `type: accessibility`.

**Reject when** a parent-spec quality-attribute requirement has no specialised case in this TestSpec (and the layer is the appropriate layer for that QA — typically root for stakeholder-facing QA, branch for component-allocated QA).

**Approve when** every QA requirement maps to ≥ 1 specialised case.

**Evidence pattern:** name the parent-spec QA requirement; note absence of specialised case.

**recommended_action:** *"Add a specialised case (`type: performance` / `security` / `accessibility`) per QA requirement at the layer that owns the QA. The matched author skill's integration-and-system-specifics reference covers derivation."*

## check.integration.version-pinning-missing (soft)

**Check that** when the case's environment depends on specific versions (database engine, library, runtime, browser), preconditions pin the version.

**Reject when** preconditions reference a versioned dependency (e.g. *"PostgreSQL"*, *"the SDK"*) without naming the version.

**Approve when** preconditions pin versions explicitly (e.g. *"PostgreSQL 15.4"*, *"SDK 2.7.x"*) OR explicitly state version-agnosticism.

**Evidence pattern:** name the case id; quote the unpinned dependency.

**recommended_action:** *"Pin the dependency version in preconditions, or state version-agnosticism explicitly. Unpinned environments produce non-repeatable cases."*

## Cross-link

`per-layer-weight-checks.md` (branch / root case shape) · `architecture-traceability-checks.md` · `requirements-traceability-checks.md` · `quality-bar-gate.md` (Integration card)
