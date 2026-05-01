# Quality Bar checklist (author self-check)

A Yes/No checklist the author runs before declaring the TestSpec complete. Eight QB groups + the Spec Ambiguity Test meta-gate. Items that cannot be answered Yes are flagged inline in the document, not silently passed.

This checklist is the author-side self-check. The matched review skill carries the canonical catalog with severities and check-IDs; this list mirrors the structure but does not duplicate the catalog.

## How to run

Walk top to bottom. For each item:
- **Yes** — the document satisfies the bar
- **No** — flag inline at the relevant location with `[QB-FAIL: <item>]`; do not pass silently
- **N/A** — item does not apply at this layer / posture (note why in `notes:` of the relevant case or in the Overview)

When any "Yes" cannot be honestly answered, do not ship — surface the gap.

## Group 1 — Shape

- [ ] Front-matter has all required fields (`id`, `artifact_type: test-spec`, `scope`, `level`, `derived_from`, `verifies`, `governing_adrs`, `status`, `date`).
- [ ] `level:` matches scope position (root → system, non-leaf → integration, leaf → unit).
- [ ] Every case has `id`, `title`, `type`, `verifies` and the layer-appropriate fields (inputs / preconditions / steps / expected).
- [ ] Case IDs follow the project convention (e.g., `TC-<scope>-NNN`).

## Group 2 — Derivation

- [ ] Every case carries `type:` from the eleven-strategy enum.
- [ ] Every behaviour rule in the parent spec has a `functional` (RBT) case.
- [ ] Every input range in the parent spec has `boundary` (BVA) cases on its bounds.
- [ ] Every error-matrix row / precondition / typed error has an `error` case.
- [ ] Every invariant has a `property` case.
- [ ] Every state-machine transition has a `state-transition` case.
- [ ] Error / happy ratio is at least 1:2.

## Group 3 — Per-layer weight

- [ ] Leaf cases are thin (no fixtures-rich preconditions; usually no `steps:`).
- [ ] Branch cases name fixtures / doubles / seeds / environment in `preconditions:`.
- [ ] Root cases are user-journey narratives in PB vocabulary in `expected:`.
- [ ] No leaf case names > 2 test doubles.
- [ ] No root case `expected:` mentions internal API terms or class names.

## Group 4 — Case quality / oracle

- [ ] Every `expected:` is a specific value, enumerated set, or bounded predicate (no qualitative phrases — refusal C).
- [ ] Every case is self-validating (pass/fail decision in `expected:`, no "verify by inspection").
- [ ] Every case is independent of other cases (no "after TC-001" residue).
- [ ] Clocks, random seeds, ordering are named in `preconditions:` where load-bearing.
- [ ] Each case has one Act (no compound steps without splitting).

## Group 5 — Verifies traceability

- [ ] Artifact-level `verifies:` is non-empty (refusal B).
- [ ] Every case `verifies:` is non-empty (refusal B).
- [ ] Every `verifies:` element resolves to a live ID in the upstream spec (refusal B).
- [ ] Granularity matches layer (leaf → DD field; branch → ARCH interface / composition; root → REQ / PB outcome).

## Group 6 — Test doubles

- [ ] Every double has its type named (dummy / stub / spy / mock / fake) in `preconditions:`.
- [ ] Every fake has a paired contract case.
- [ ] Interaction verification (mocks / spies) is reserved for cases where interaction is the observable behaviour.
- [ ] Third-party APIs are wrapped behind project-owned interfaces; doubles target the wrapper.

## Group 7 — Coverage and mutation bar

- [ ] Front-matter has `coverage_mutation_bar:` block (derived-hard refusal — must be present).
- [ ] Block declares `structural_coverage` (threshold + metric, or placeholder).
- [ ] Block declares `mutation_score` (threshold + tool category, or placeholder).
- [ ] Block declares enforcement `frequency` and `blocking` posture.
- [ ] No specific threshold values invented by the author — placeholders are explicit (`"TBD-by-project-policy"`).

## Group 8 — Retrofit discipline (retrofit only)

- [ ] `recovery_status:` declared at front-matter when retrofit posture applies.
- [ ] Cases derived from spec FIRST; existing tests mapped after.
- [ ] `title:` and `notes:` left empty (`# HUMAN-ONLY`) until human supplies intent (refusal A).
- [ ] Reconstructed `verifies` carry `recovery_status: unknown` (refusal A).
- [ ] Retrofit Gap Report section is present and populated (or each bucket marked `(none observed)`).

## Spec Ambiguity Test (meta-gate, override — refusal D)

Two questions in sequence:
1. **Could a junior engineer or mid-tier AI, reading only this TestSpec (plus the layer's spec artifact and governing ADRs), write test code implementing every case as specified?**
2. **Could a reviewer, reading only this TestSpec, tell whether every equivalence class, every boundary, every error path was considered, without reading implementation or test code?**

If either answer is No → the TestSpec is not done. This test overrides every Yes/No box: a TestSpec that passes all other groups but fails this one has not done the job a TestSpec exists to do.

When SAT fails, the author does one of:
- Add specificity until both answers can be Yes
- Mark `[NEEDS-CLARIFICATION: <what>]` inline and HALT pending human input
- Surface as DESIGN_ISSUE upstream when the gap is in the parent spec

## Cross-link

`anti-patterns.md` (the failure-mode sweep that pairs with this checklist) · `verifies-traceability.md` (Group 5) · `coverage-mutation-bar.md` (Group 7) · `retrofit-discipline.md` (Group 8)
