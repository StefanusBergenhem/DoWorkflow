---
purpose: Phase 6 dogfooding findings from vmodel-core pilot, organised by cluster, with recommended decision order.
audience: framework maintainer
status: living
source: dogfood_issues_found.md (raw issues, in date order)
---

# Phase 6 dogfooding findings — vmodel-core pilot

Source: `dogfood_issues_found.md` (vmodel-core, sessions 2026-05-01 → 2026-05-04). 23 issues. This doc reframes the raw list by cluster + decision priority. Read this first; consult the source for issue-level detail.

---

## Headline

The framework concept is holding up. None of the 23 issues say the artifact model, scope tree, V-model layering, or Spec Ambiguity Test is wrong. They are sharp-edge gaps in skill execution and convention specification — exactly what first-real-use dogfooding is meant to surface.

The single dealbreaker is Cluster 1. Everything else is incremental.

---

## Session log

- **2026-05-04 — opened doc, attacked Cluster 1 (Issue 22).**
  - Authored `docs/authoring-discipline.md` (6 cross-cutting rules: product-shape, boundary-only, small-system collapse, rationale-as-citation, diagram-or-prose, cite-don't-restate).
  - Embedded as verbatim per-skill copy at `references/authoring-discipline.md` in all 10 affected skills (5 author + 5 review for the 6-artifact set, excl. product-brief). Sync script at `scripts/sync-authoring-discipline.sh` (idempotent, fails-loud, md5-verified across 21 paths).
  - Validated via re-author of `vmodel-core/specs/architecture.md`: **−46% prose** (10,239 → 5,561 words), no architectural loss, all 32 REQs / 9 ICs / 2 ADRs preserved. Markers `NEEDS-ADR-CONSIDER` introduced during validation, then folded into existing `NEEDS-ADR` (per stakeholder call: no new middle-step markers; the ADR review process is the gate).
  - Issue 24 candidate (negative-space content) **resolved** — folded into Rule 0 of the discipline.
  - Discipline pass took the architecture artifact as far as prose discipline can go. Remaining ~7,200 tokens sit mostly inside YAML DbC blocks (preconditions, postconditions, invariants, errors) — Rule 1 protects them. **Two structural moves required to fully close Issue 22, deferred to next session** (see Cluster 1 below).

---

## Cluster 1 — Existential (decide first)

**Issue 22 — Token budget per session caps adopter tier.** Status: **partially closed (2026-05-04).**

`architecture.md` reached ~30k tokens; a single author session burned 250–300k; review burned ~100k. The discipline pass on 2026-05-04 brought the architecture artifact to ~7,200 tokens (−46% on prose). Adopter-tier viability for full pipeline workloads still requires two structural moves:

1. **YAML DbC block factoring.** ~3,500 of the post-discipline 5,561 words sit inside boundary YAML. Rule 1 (boundary-only) protects them. The lever is structural — factor shared invariants and shared error enums into a single block per scope, with per-interface entries citing the shared definitions. Open design questions: where do shared blocks live, what's the citation syntax, schema validation impact, review-skill enforcement impact.
2. **Scope-shape template variants.** The "Honest departure from template" pattern existed in the original artifact because the architecture template is one-size-fits-all. CLI tools, libraries, and services have genuinely different concern sets; defending absences in the artifact is a workaround for a missing template variant. Open design: introduce `kind: architecture-cli` / `architecture-service` / `architecture-library` variants, each with concern-set-specific templates. Author skill needs to pick the right variant based on declared `kind:`.

**Both deferred to the next session.** Continuation point picks up here with option (b) per session-end agreement (2026-05-04).

**Decision shape.** Architectural — affects template, schema, and author/review skill pair behaviour. Larger than the discipline pass; likely two design discussions before any file-writing.

---

## Cluster 2 — Foundational decisions (decide before more skill work)

Small-text issues with big-implication ripples. Don't let skill-level cleanup begin until these settle.

**Issue 13 — File-based review handover.** Review verdicts + findings live only in conversation transcripts today. Should be canonical YAML at a known path, mirroring the build-side `.workflow/` idiom. Changes the API of every author/review skill pair.

**Issue 23 — What does testspec verify (architecture vs requirements).** A foundational ambiguity in the testspec layer that will compound across every testspec authored. Settle the layering rule once.

---

## Cluster 3 — Missing author-time gate (5 issues, 1 structural fix)

Five issues, same shape: review skills catch what author skills emit without a corresponding gate. Not five fixes — one structural decision applied to five surfaces.

| # | Surface |
|---|---|
| 11 | ADR/architecture skills write requirement-shaped content without invoking requirements-skill discipline |
| 15 | Mermaid parser-breaking characters in author templates |
| 16 | ADR-bound library bindings land in wrong architecture field |
| 20 | Typed-error enum coverage in testspec |
| 21 | Implicit-`verifies` references uncited in testspec cases |

**Decision shape.** Pick one of: (A) recursive cross-skill invocation, (B) shared author-time checklist imports, (C) accept review as the gate. Decide once; ripple through the five surfaces.

---

## Cluster 4 — Convention pin-downs in TARGET_ARCHITECTURE

Mechanical; pin once and ripple through skill templates. Low-risk, high-value cleanup.

| # | What needs pinning |
|---|---|
| 1  | `needs.md` placement and parent-directory creation rule |
| 6  | Schema field name divergence (`governing_decisions` vs `governing_adrs`) — audit every template |
| 12 | Canonical machine-readable index of pending items across the spec tree |
| 14 | Specs-global glossary as a first-class artifact location |
| 17 | Empty-scope ID encoding for `id:`, `verifies:`, dotted-path references |
| 18 | Partial-parent fallback (testspec at root scope when Product Brief absent) |

---

## Cluster 5 — No-fabrication discipline extensions

The discipline is sound for vocabulary; it needs to extend into numbers and standard-convention imports. Each is a small reference addition.

| # | Extension |
|---|---|
| 4  | Premature numerical commitment as design-smuggling |
| 7  | Deferred NFR thresholds (Planguage with `pending — <calibration source>`) |
| 8  | Standard-engineering-convention imports as permissible rationale |
| 19 | Placeholder oracles in testspec (structured `TBD-by-X` distinct from weak assertions) |

---

## Cluster 6 — Process and UX

Skill-flow refinements. Mostly small per-skill updates.

| # | Refinement |
|---|---|
| 2  | Needs elicitation cascade (parent-scope before per-product) |
| 3  | Readback silence after flag = informed agreement |
| 9  | Generalised "no canonical upstream" protocol across all author skills |
| 10 | Redundant readback during elicitation; consolidate to one final pass |

---

## Recommended decision order

1. **Cluster 1 (Issue 22) — discipline pass DONE 2026-05-04. Structural pass NEXT.** Take the two structural moves (YAML factoring + scope-shape variants) before more skill work — they affect every author skill's template.
2. **Cluster 2 (Issues 13, 23)** — settle file-based handover and testspec semantics before reshaping skills.
3. **Cluster 3 as one design pass** — pick A/B/C, ripple through 5 surfaces.
4. **Cluster 4 + 5 + 6 in parallel** — mechanical, batchable, low-risk.

---

## Resolved candidate issues

- **Issue 24 candidate (editorial / negative-space content) — RESOLVED 2026-05-04.** Folded into Rule 0 of `docs/authoring-discipline.md` (Product-shape only). Manifests as `n/a + justification`, "Honest departure from template", and *Notes / Self-attestation* sections; review skills will catch via `check.discipline.product-shape` finding.

---

## Issues 25 + 26 — surfaced 2026-05-05, resolved by Rules 6 + 7 + 8

**Issue 25 — Marker semantics conflate "artifact missing" with "decision deferred".**
- Surface: `[NEEDS-DD: ...]` markers in vmodel-core's architecture read like a TOC of DDs to write, but every leaf already has a mandatory DD per `TARGET_ARCHITECTURE §5.2`. Some markers also pointed to non-component topics (e.g., build-pipeline release surface) — category errors.
- Resolution: **Rule 6** in `authoring-discipline.md`. Markers renamed `NEEDS-X` → `DEFER-X`. Routing rule: DD for component-internal gaps, ADR for cross-cutting decisions with named alternatives, inline `<TBD>` for parametric gaps, push back if none fit.

**Issue 26 — Cross-cutting non-component decisions had no documented home.**
- Surface: markers like `[NEEDS-DD: build-pipeline release surface]` named topics that aren't leaf components, but the framework had no documented routing.
- Resolution: **Rule 6 routing table.** Three routes: ADR (decision with alternatives) / inline content in existing root-Architecture sections (deployment intent, fitness functions, observability) / `<TBD>` (parametric). No new artifact type; no ADR sub-category.

---

## What to monitor (not yet issues, but watch)

- **Cumulative spec budget per scope.** Architecture is ~30k tokens; DD has not yet been authored. If DD repeats 50%+ of architecture content, the cumulative budget per scope explodes. Validate the architecture/DD layering rule produces clean separation before the first DD is authored.
- **Review-skill judgment-as-precedent.** Issue 19's resolution was reviewer judgment, not written rule. Track how often review verdicts hinge on undocumented reasoning; high rate = framework-rule-deficit signal.
- **Per-artifact defect rate at draft time.** Issue 11 logged 75% defect rate at the propagation interface; Issue 20 logged 50% typed-error coverage at draft. Establish a baseline and watch the trend as author-time gates land.
