# Verifies traceability checks

The `verifies` field is the spine of the TestSpec. Empty `verifies` is refusal B (orphan tests). This file enforces non-emptiness, resolution, and per-layer granularity.

## check.verifies.artifact-level-empty (HARD ★ refusal B)

**Check that** front-matter declares `verifies:` with at least one resolvable element.

**Reject when** front-matter `verifies` is `[]`, missing, or only contains placeholders.

**Approve when** `verifies` lists ≥ 1 upstream-resolved id.

**Evidence pattern:** quote the empty / missing front-matter line; cite refusal B.

**recommended_action:** *"Populate artifact-level `verifies` with the upstream artifact id(s) the TestSpec verifies. See refusal B in `SKILL.md`."*

## check.verifies.case-level-empty (HARD ★ refusal B)

**Check that** every case carries a non-empty `verifies` list.

**Reject when** any case is missing `verifies`, has `verifies: []`, or contains only an empty / placeholder string.

**Approve when** every case lists ≥ 1 upstream-resolved id.

**Evidence pattern:** name the case id; quote the empty / missing line.

**recommended_action:** *"Populate the case's `verifies` field with the upstream id(s) it verifies. Orphan cases are refusal B."*

## check.verifies.unresolvable (HARD ★ refusal B; alias `anti-pattern.orphan-tests`)

**Check that** every `verifies` element resolves to a live id in an upstream spec — at the layer's parent (DD at leaf, Architecture at branch, Requirements + Product Brief at root).

**Reject when** a `verifies` element does not resolve, OR is a filename rather than an artifact id (e.g. `claim_service.py`), OR points at this same TestSpec (self-reference).

**Approve when** every `verifies` element resolves to a live upstream id.

**Evidence pattern:** quote the unresolvable id; name the upstream spec(s) checked.

**recommended_action:** *"Repair the `verifies` link to point at a live upstream id. Reference artifacts by id, not by file path. Refusal B applies."*

## check.verifies.granularity-mismatch (soft)

**Check that** `verifies` granularity matches the layer:
- **Leaf**: case `verifies` points at a DD field (function name, error-matrix row id, postcondition id, invariant id) — not at a root-level requirement
- **Branch**: case `verifies` points at an Architecture interface, decomposition entry, or composition invariant
- **Root**: case `verifies` points at a Requirement id or Product Brief outcome statement

**Reject when** a leaf case `verifies` resolves to a root requirement (skipping the DD), or a branch case `verifies` resolves to a leaf DD field (jumping down a layer), or a root case `verifies` resolves to a leaf DD field.

**Approve when** every `verifies` link is at the layer-correct granularity. Field-level qualification is accepted (e.g. `DD-x.behavior.foo`).

**Evidence pattern:** name the case id; quote the granularity-jumping `verifies` link; name the layer it should resolve to.

**recommended_action:** *"Restate `verifies` at the layer-correct granularity. The matched author skill's verifies-traceability reference covers per-layer rules."*

## Cross-link

`testspec-shape-checks.md` (front-matter required fields) · `dd-traceability-checks.md` · `architecture-traceability-checks.md` · `requirements-traceability-checks.md` · `quality-bar-gate.md` (Verifies card) · `anti-patterns-catalog.md` (orphan-tests)
