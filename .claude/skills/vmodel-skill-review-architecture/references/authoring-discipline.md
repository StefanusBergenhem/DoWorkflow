---
purpose: Cross-cutting authoring discipline. Author skills follow; review skills enforce as `check.discipline.<rule>` findings.
audience: author skills, review skills, framework maintainers
status: active (Phase 6)
applies_to: all author and review skills for the 6-artifact set
source_of_truth: this file. Per-skill copies at `references/authoring-discipline.md` are verbatim. Sync via `scripts/sync-authoring-discipline.sh`.
---

# Authoring Discipline

Nine rules. They exist because spec-tier work runs on a token budget, bloat caps which model tier can author the artifact, and structural conventions need pinning before they ossify wrong.

**Contents.** Rule 0 product-shape only · Rule 1 boundary-only · Rule 2 small-system collapse · Rule 3 rationale-as-citation · Rule 4 diagram-or-prose · Rule 5 cite-don't-restate · Rule 6 defer-marker semantics · Rule 7 scope tree by layer · Rule 8 architecture multi-file bundle · Review enforcement · Distribution.

---

## Rule 0 — Product-shape only

The artifact describes the product as it is. It does not describe the template that shaped it, the artifact itself, or how it was authored. If a slot does not apply, omit it.

**Forbidden manifestations.**
- `n/a + justification` entries explaining why a template slot was left empty.
- *Departures from template* / *Authoring conventions used* sections.
- *Notes / Self-attestation* sections.
- "Honest departure from..." framings that defend absences.

**Test.** Is this section's subject the product? If the answer is "the template", "this artifact", or "how this artifact was authored", delete or push to front-matter.

**Allowed.**
- *Open follow-ups* (gaps in the **product's** spec, not the artifact's process).
- Front-matter metadata (status, version, derivation links, recovery_status).
- *Glossary*.

**Narrow exception.** When an upstream spec explicitly mandates a slot the product genuinely lacks, one sentence is warranted — but the right resolution is to fix the upstream binding (e.g., introduce a scope-shape template variant), not defend in the child.

**Anti-examples.** `architecture.md`'s *Middleware stack — n/a — single-process CLI...* (template-shape) and the *Notes / Self-attestation* section at end-of-file (artifact-shape). Both delete entirely.

---

## Rule 1 — Boundary-only at non-leaf scope

Architecture describes seams: each child's purpose, the interface contract at each seam (preconditions, postconditions, invariants, typed errors, thread-safety), the composition pattern, quality-attribute allocation. It does NOT describe internal data structures, algorithms, internal state, or implementation choices of any child.

**Test.** Could two implementers, given only this content, build the children with non-overlapping concerns and have them compose at runtime? Anything beyond what's needed for that test is DD leakage.

**Tells.** A responsibility reads like a procedure ("first parses X, then validates Y"). Internal types appear in body content (not just at boundary signatures). Library names appear in `responsibilities` or `purpose` (vs. in `rationale` citing the governing ADR — see Rule 3).

**Boundary content is protected.** Interface signatures, DbC clauses (preconditions, postconditions, invariants), and typed error enums are *the* deliverable at this scope; do not compress them. If they collectively bloat the artifact past the working budget, that is a template-level structural concern (e.g., factor shared invariants into a referenced block), not a discipline concern.

---

## Rule 2 — Small-system collapse

When all children of a scope are leaves AND the count is fewer than 5, architecture and DD MAY be authored as one combined artifact (`architecture-and-design.md`, front-matter `kind: architecture-and-design`) at the scope. Otherwise, separate `architecture.md` (boundary-only per Rule 1) and per-child `<child>/detailed_design.md`.

**Combined shape.** Standard architecture sections (Structure Diagram, Decomposition, Composition, Quality Attributes) plus a *Detailed Design* section per child carrying the standard DD shape (Public Interface, Data Structures, Algorithms, State, Error Handling).

**Sticky decision.** Set at scope-authoring time. Flipping later is a rewrite, not a refactor.

---

## Rule 3 — Rationale as citation

When a decision is captured in an ADR, `rationale` is one line plus ADR citation. No re-narration of the ADR's *Decision* or *Rationale*.

**Format.**

```yaml
rationale: "Stable iteration order required for byte-deterministic output; see ADR-001."
```

**Smell.** Multi-paragraph rationale that does not cite an ADR usually hides a sub-decision that should be its own ADR. When no ADR governs and the rationale exceeds one sentence, ask whether this is a real decision needing its own ADR.

---

## Rule 4 — Diagram-or-prose, not both

A sequence diagram and an interface entry that state the same call flow are duplication. Pick one per interaction.

- **Diagram** when interaction has temporal ordering, multiple participants, async edges, or fan-out / fan-in.
- **Interface entry** when the boundary is a single call with DbC-shaped contract.

**Test.** Delete the diagram — is the interface entry still complete? If yes, the diagram is redundant.

**Borderline.** A multi-step interaction across child A → B → C with state observable across the chain warrants a diagram. The per-call contracts the diagram references should NOT also re-narrate the call sequence.

---

## Rule 5 — Cite, don't restate

Refer to upstream content (REQ, ADR, IC, ARCH-path) by ID with at most one summary line. Do not re-state upstream content verbatim. Reader follows the citation if full text is needed.

**Replace.**

```
"REQ-025 (the system shall produce a self-contained HTML report containing
verdict, findings, and traceability summary, with deterministic ordering...)..."
```

**With.**

```
"REQ-025"   — or, when orientation is genuinely needed —   "REQ-025 (HTML report shape)"
```

**Tell.** Same upstream ID restated verbatim in 3+ places = paying for the upstream content N+1 times.

---

## Rule 6 — Defer-marker semantics

A `[DEFER-<TARGET>: <topic>]` marker names a deferred decision and the artifact at which it will be answered. Targets are `DD` and `ADR`. The marker does NOT create the existence of that artifact — every leaf has a DD by mandate (`TARGET_ARCHITECTURE §5.2`); every cross-cutting decision-shaped gap has a home in the existing artifact set.

**Routing.**

- `[DEFER-DD: <component> — <topic>]` when the gap belongs inside a leaf component's internals.
- `[DEFER-ADR: <topic>]` when the gap is cross-cutting / non-component AND has named alternatives (or alternatives can reasonably be enumerated).
- **Inline content, no marker** when the gap belongs in an existing root-Architecture section (deployment intent, quality attributes, fitness functions, observability + security, resilience). Those sections already prescribe the home — write the content there.
- **Inline `<TBD>` placeholder, no marker** when the gap is parametric (numeric threshold / factual value to be calibrated). Tooling locates `<TBD>` by section context.
- None fit — push back on the marker. Likely artificial, or a decision whose alternatives haven't been thought about.

**Rationale.** Markers were drifting to mean "this artifact is missing", inflating perceived scope and reading like a TOC of artifacts to create. Every leaf already has a mandatory DD; every decision-shaped cross-cutting gap has an ADR home; everything else is a parameter or a section in an existing artifact.

---

## Rule 7 — Scope tree by layer

The spec tree is a strict hierarchy. Each scope is a directory. Children are subdirectories of their parent scope's directory. Root scope is `specs/`.

**Reserved names.** At any scope, the following names are reserved for canonical artifact files or artifact bundles. Child scopes cannot use them as scope IDs:

`architecture, requirements, testspec, adrs, needs, product_brief, detailed_design, architecture-and-design`

**Directory-is-scope rule.** A subdirectory inside a scope is a **child scope** iff its name is not reserved. Otherwise it is an **artifact bundle** (Rule 8) or a fixed structural directory (`adrs/`).

**Canonical filenames per scope role.**

| Filename | Where |
|---|---|
| `needs.md` | root only |
| `product_brief.md` | root only |
| `requirements.md` | every scope (root + non-root) |
| `architecture.md` | non-leaf scope (helicopter; see Rule 8) |
| `detailed_design.md` | leaf scope |
| `architecture-and-design.md` | small-system collapse (Rule 2) |
| `testspec.md` | every scope |
| `adrs/<adr-id>.md` | scope-local ADRs at any scope |

**Front-matter `scope:` is authoritative.** Path is derivable from scope; cross-tree references use IDs, never paths. ID stability across file moves is preserved.

---

## Rule 8 — Architecture multi-file bundle

An Architecture artifact at non-leaf scope MAY be authored as a helicopter file plus a detail bundle. Tooling treats helicopter + bundle as one logical artifact for graph construction, Spec Ambiguity Test, and review.

**Helicopter file** (`architecture.md` at the scope) carries: Overview, Structure Diagram, Decomposition, Interface roster (slim form below), Composition (Runtime pattern, Wiring, Sequence diagrams, Deployment intent at root), Quality Attributes, Resilience, Observability + security, Evolution + fitness functions.

**Detail bundle** (`architecture/` subdirectory at the same scope) carries per-element detail. Initial subtype: `architecture/interfaces/<NAME>.md` for per-interface DbC detail.

**Detail file front-matter.**

```yaml
---
id: ARCH-IF-<NAME>                  # globally unique
belongs_to: <helicopter-id>         # back-link to the architecture artifact
kind: architecture-interface-detail # subtype within the bundle
subject: <element-id>               # e.g. interface name
scope: <scope-of-the-helicopter>
status: ...
date: ...
version: ...
---
```

**Helicopter interface entry — slim form.** Keep `name, from, to, protocol, contract.operation, contract.summary_postcondition, contract.key_invariants, contract.rationale, detail`.

- `summary_postcondition` — one-sentence summary of the load-bearing success guarantee plus halt/error behaviour. Derived from existing detailed clauses; not a new claim.
- `key_invariants` — 1-3-element list of the most architecturally load-bearing IDs (REQ / IC / ADR) this seam structurally enforces.
- `detail` — relative path to the detail file.

**Detail file content.** Full `preconditions, postconditions` (multi-branch), `invariants, errors, quality_attributes, authentication, authorisation, version, deprecation_policy`. Lifted verbatim from the contract; no editorial changes between helicopter and detail.

**Decomposition entries — banned fields.** `bounded_context_line`, `owning_team_type`, `test_seam`. Team-topology-derived; not load-bearing for AI-first frameworks where Conway's-law concerns do not apply.

**Test surface.** Architecture does NOT carry default test-design content (`fake_strategy`, `driving_ports`, `driven_ports`). Test-design content lives in TestSpec at the corresponding scope. When testing forces an additional architectural seam, it appears as a regular interface entry — not in a separate test block.

**Bundle scope.** Multi-file bundle is currently **architecture-only**. Other artifact types stay single-file pending Phase 6 evidence that the same compression problem appears.

---

## Review enforcement

| Rule | Check ID | Severity |
|---|---|---|
| 0 | `check.discipline.product-shape` | soft_reject |
| 1 | `check.discipline.dd-leakage-into-architecture` | soft_reject |
| 2 | `check.discipline.collapse-eligibility-misapplied` | info |
| 3 | `check.discipline.rationale-narration` | soft_reject |
| 4 | `check.discipline.diagram-prose-duplication` | soft_reject |
| 5 | `check.discipline.upstream-restatement` | soft_reject |
| 7 | `check.discipline.scope-tree-shape` | soft_reject |
| 8 | `check.discipline.architecture-bundle-shape` | soft_reject |

---

## Distribution

This file is the maintainer source of truth. Skills do not reference it directly — they ship a verbatim copy at `references/authoring-discipline.md`. Affected skills (10): `vmodel-skill-author-{requirements, architecture, detailed-design, testspec, adr}` and `vmodel-skill-review-{requirements, architecture, detailed-design, testspec, adr}`. Sync via `scripts/sync-authoring-discipline.sh` (TBD; manual propagation until built).
