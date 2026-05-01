# TestSpec shape checks

Front-matter required-field checks, id pattern checks, level-vs-scope alignment checks, case-block well-formedness checks. All identifiers cataloged in `quality-bar-gate.md`.

## check.shape.frontmatter-missing-required-field (HARD ★ schema invariant)

**Check that** front-matter declares `id`, `scope`, `level`, `verifies`, `derived_from`, `coverage_mutation_bar`. When `recovery_status` is declared (retrofit mode), it is well-formed.

**Reject when** any required field is absent.

**Approve when** every required field is present (values may be placeholders for `coverage_mutation_bar` sub-items; the section itself must exist).

**Severity rationale:** schema invariant — the document is structurally invalid without these fields.

**recommended_action:** *"Populate the missing front-matter field per the matched author skill's front-matter template."*

## check.shape.id-pattern-violation (soft)

**Check that** the artifact `id` follows the project's id pattern (kebab-case, scope-prefix, no trailing whitespace, no spaces).

**Reject when** the id contains spaces, uppercase letters in violation of the convention, or a free-form path rather than a stable identifier.

**Approve when** the id is a stable identifier matching the convention.

**recommended_action:** *"Reissue the id per the project id pattern. IDs are stable across renames; treat them as primary keys."*

## check.shape.level-scope-mismatch (soft)

**Check that** declared `level` follows the scope position: `system` at root, `integration` at non-leaf branches, `unit` at leaves.

**Reject when** a leaf TestSpec declares `level: integration`, a branch declares `level: unit`, or a root declares `level: unit` / `level: integration`.

**Approve when** declared `level` matches scope position.

**recommended_action:** *"Align `level` with scope position. The matched author skill's per-layer-weight reference covers level derivation."*

## check.shape.case-block-malformed (soft)

**Check that** every case block has the minimum well-formed structure: `id`, `title`, `type`, `verifies`, plus the per-layer fields.

**Reject when** a case block is missing `id`, has duplicate `id` with another case, has malformed YAML, or has `verifies` typed incorrectly (e.g. string instead of list).

**Approve when** every case block parses and carries the minimum fields.

**recommended_action:** *"Repair the case block per the matched author skill's per-layer case template."*

## Cross-link

`quality-bar-gate.md` (Shape card) · `derivation-strategy-checks.md` (per-case `type`) · `verifies-traceability-checks.md` (per-case `verifies` non-emptiness)
