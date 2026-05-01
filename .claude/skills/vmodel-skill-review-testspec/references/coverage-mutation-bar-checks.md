# Coverage and mutation bar checks

The artifact-level `coverage_mutation_bar` block is load-bearing — its presence is hard-required, but its sub-thresholds are soft (placeholder values acceptable; project policy fills them later).

## check.coverage-mutation.section-missing (HARD ★ derived hard)

**Check that** front-matter declares a `coverage_mutation_bar:` block.

**Reject when** the block is absent entirely from front-matter.

**Approve when** the block is present, even if its sub-fields hold placeholder values.

**Severity rationale:** load-bearing structural invariant — the matched author skill refuses to ship without the section, so review hard-rejects symmetrically. This is the one derived-hard reject beyond refusals A / B / C / D.

**Evidence pattern:** quote the front-matter showing absence; cite the load-bearing flag.

**recommended_action:** *"Add the `coverage_mutation_bar:` block per the matched author skill's coverage-mutation-bar reference. Placeholder values are acceptable; absence is not."*

## check.coverage-mutation.structural-threshold-missing (soft)

**Check that** the `coverage_mutation_bar` block declares a structural coverage threshold (typically branch coverage, expressed as a percentage or as `placeholder` for project policy).

**Reject when** the field is absent or empty.

**Approve when** a value or explicit `placeholder` token is present.

**recommended_action:** *"Populate the structural threshold field, or use the project-policy placeholder. The matched author skill's coverage-mutation-bar reference does not prescribe values."*

## check.coverage-mutation.mutation-threshold-missing (soft)

**Check that** the block declares a mutation score threshold.

**Reject when** the field is absent or empty.

**Approve when** a value or `placeholder` is present.

**recommended_action:** *"Populate the mutation threshold, or use the placeholder. Mutation testing closes the gap structural coverage leaves."*

## check.coverage-mutation.tool-unnamed (soft)

**Check that** the block declares the mutation tool category — e.g. PIT-class (JVM), Stryker-class (JS/TS/.NET/Scala), mutmut-class (Python). Specific tool name is fine; category is the floor.

**Reject when** the field is absent.

**Approve when** a category or specific tool is named.

**recommended_action:** *"Name the mutation tool category (PIT-class / Stryker-class / mutmut-class) or specific tool. Project-policy may override the choice; declaration is the bar."*

## check.coverage-mutation.frequency-unnamed (soft)

**Check that** the block declares the enforcement frequency — per-PR / nightly / weekly.

**Reject when** the field is absent.

**Approve when** a frequency is named.

**recommended_action:** *"Name the enforcement frequency (per-PR / nightly / weekly). Frequency drives the operational cost and thus the threshold."*

## Cross-link

`testspec-shape-checks.md` (front-matter required fields) · `quality-bar-gate.md` (Coverage-mutation card) · `anti-patterns-catalog.md` (coverage-as-quality-metric 9)
