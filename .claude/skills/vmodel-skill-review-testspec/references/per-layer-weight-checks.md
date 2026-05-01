# Per-layer weight checks

Each TestSpec layer has a different case shape. Mirrors `per-layer-weight.md` on the author side. Walk every case and verify it matches the shape for the artifact's scope position.

## check.per-layer.leaf-overweight (info)

**Check that** leaf cases stay thin: title, type, verifies, inputs, expected. Steps and preconditions are usually omitted at leaf (the unit-under-test is small enough that the contract is the whole story).

**Reject (info)** when a leaf case carries heavy `preconditions` (multiple fixtures, environment setup) or a long `steps` block — the case is doing branch-level work at leaf.

**Approve when** leaf cases are thin or carry only minimal preconditions (e.g. a frozen clock seed).

**Evidence pattern:** name the case id; cite step / precondition count.

**recommended_action:** *"Trim leaf case to the thin shape (title, type, verifies, inputs, expected). Heavy preconditions / steps belong at the branch above."*

## check.per-layer.branch-underweight (soft)

**Check that** branch cases name fixtures, doubles, seeds, environment in `preconditions`, and enumerate cross-child interactions in `steps`.

**Reject when** a branch case has no `preconditions` block, or `steps` is one line, or fixtures / doubles are unnamed.

**Approve when** preconditions name the integration-test setup explicitly and steps enumerate the cross-child interactions whose composition is under test.

**Evidence pattern:** name the case id; cite the missing block.

**recommended_action:** *"Populate the branch case shape: preconditions naming fixtures / doubles / seeds / environment; steps enumerating cross-child interactions. The matched author skill's per-layer-weight reference describes the shape."*

## check.per-layer.root-internal-vocab (soft)

**Check that** root cases express `expected` in Product Brief vocabulary (user-observable outcomes), not internal API or class names.

**Reject when** a root case `expected` names internal classes, function names, database tables, or implementation-internal vocabulary.

**Approve when** root cases read as user journeys with stakeholder-vocabulary expected outcomes.

**Evidence pattern:** name the case id; quote the offending internal-vocabulary phrase.

**recommended_action:** *"Restate `expected` in Product Brief vocabulary. Root cases describe what the user observes; the layer below makes that observable."*

## check.per-layer.scope-derived-level-mismatch (soft)

**Check that** declared `level` in front-matter matches the scope position: `unit` at leaf, `integration` at non-leaf branches, `system` at root.

**Reject when** a leaf TestSpec declares `level: integration`, a branch declares `level: system`, etc. Distinguish from `check.shape.level-scope-mismatch` — that one fires on missing-or-malformed; this one fires on legal-but-wrong values.

**Approve when** `level` matches scope position.

**recommended_action:** *"Reconcile `level` with scope position. The matched author skill's per-layer-weight reference covers level derivation."*

## Cross-link

`testspec-shape-checks.md` (level field presence) · `case-quality-checks.md` (per-case quality regardless of layer) · `quality-bar-gate.md` (Per-layer card)
