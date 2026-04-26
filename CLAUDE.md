# VModelWorkflow — AI-Augmented Spec-Driven Development Framework

## What This Project Is

Four independent components designed to work together for AI-augmented spec-driven development with V-model rigor:

1. **Documentation** — Single source of truth for all domain knowledge. Per artifact type: 5 sections (V-model context, best practices, anti-patterns, examples, Quality Bar). AI skills are derived from this. Non-negotiable foundation.
2. **Templates & Schemas** — Artifact definitions, structural-rigor schemas, canonical Quality Bar checklists. Usable by humans, agents, or both.
3. **Traceability** — Link model + validation rules. Forward links embedded in artifacts; reverse relationships derived by tooling.
4. **AI Skills** — Craft skills (standalone best practices, derived from documentation) and framework skills (orchestration, template integration, traceability use, retrofit mode).

**End goal:** well-structured software, developed by AI agents under tight human design review, where the human leverage point is the spec layer — not code review.

**Primary use cases:**
- **Greenfield development** — top-down through the scope tree.
- **Legacy retrofit** — bottom-up reverse-engineering from existing code. Primary market entry point.

**Each component is independently usable.** The real value is the combination.

---

## Pivot Reference (2026-04-18)

The project pivoted on 2026-04-18 from a safety-specific V-model framework (HW/SW split, tier-based rigor) to a generic layered spec-driven-dev framework with uniform high rigor. See:

- `docs/plan/TARGET_ARCHITECTURE.md` — authoritative architectural reference (6-artifact model, workflows, principles).
- `docs/plan/BACKLOG.md` — execution plan (phases 0–7).
- `archive/pre-pivot-2026-04-18/` — pre-pivot design record with concept mapping.

---

## Working Process

- **Discuss before writing.** Never start writing files without first explaining the approach, showing structure visually (ASCII diagrams), motivating the design choices, and getting explicit approval.
- **Structured back-and-forth.** Present ideas, ask for input or approval, then implement. Applies to schemas, skills, prompts, and any new files.
- **Small increments.** One concept at a time. Get alignment, then move on.
- **Dispatch concrete execution to subagents.** Keep the main conversation focused on design, alignment, and sign-offs; file operations, multi-step lookups, and content rewrites go to subagents.
- **Per-phase handoff docs.** Non-trivial phases get their own `PHASE{N}_AUTHORING_PATTERN.md` under `docs/plan/` — session handoff scaffolding, locked decisions, open points, recommended next step. Load alongside `CLAUDE.md` + `BACKLOG.md` + `TARGET_ARCHITECTURE.md` at session start. Archive to `archive/phase{n}/` on phase completion. Precedents: `archive/phase2/PHASE2_AUTHORING_PATTERN.md`, `archive/phase3/PHASE3_AUTHORING_PATTERN.md`, `archive/phase4/PHASE4_AUTHORING_PATTERN.md`.

---

## Core Principles

Ten load-bearing principles. Canonical wording in `TARGET_ARCHITECTURE.md §3`. Short version:

1. **Documentation is the foundation** — derive schemas, templates, skills from docs.
2. **Spec Ambiguity Test** — every spec at every layer must be implementable by a junior engineer or low-mid-tier AI without guessing.
3. **Uniform high rigor** — no tiers; Quality Bar checklists encode rigor per artifact.
4. **Workflow decoupling** — Spec, Build, Retrofit independent; artifact-only coupling.
5. **Tool/skill split** — tools mechanical (no LLM); skills interpretive (LLM-driven).
6. **Tools as independent products** — framework bundles no tools; tools live in their own repos.
7. **Retrofit never fabricates** — human-only content defaults to `recovery_status: unknown`.
8. **Human leverage at the spec level** — review specs, not code.
9. **Components are independent (SOLID)** — adopt incrementally.
10. **Tests derive from the layer's artifact, not from the code below.**

---

## Artifact Set (6 types)

| Type | Lives at | Structure reference |
|---|---|---|
| **Product Brief** | Root only | `TARGET_ARCHITECTURE §5.3` |
| **Requirements** | Non-leaf scopes | `TARGET_ARCHITECTURE §5.3` |
| **Architecture** | Non-leaf scopes | `TARGET_ARCHITECTURE §5.3` (Composition section mandatory) |
| **ADR** | Cross-cutting | `TARGET_ARCHITECTURE §5.3` (Reversibility sub-prompt mandatory) |
| **Detailed Design** | Leaf scopes | `TARGET_ARCHITECTURE §5.3` (junior-implementable) |
| **TestSpec** | Every scope | `TARGET_ARCHITECTURE §5.3` (`verifies` mandatory non-empty) |

---

## Build Order

- **Phase 0** — Archival — **DONE** (2026-04-18).
- **Phase 1** — Foundation rewrite (BACKLOG, TARGET_ARCHITECTURE, this CLAUDE.md) — **DONE** (2026-04-18).
- **Phase 2** — Per-artifact documentation (6 types, 5-section structure) — **DONE** (2026-04-22).
- **Phase 3** — Schemas (per-artifact JSON Schema draft 2020-12 + traceability catalogs + Quality Bar JSON + minimal fixtures) — **DONE** (2026-04-23).
- **Phase 4** — Product Descriptions for purpose-built tools — **CLOSED** (2026-04-26) without producing PDs. The vmodel-core pilot surfaced that PD was a category error: the actual gap is a missing **elicitation skill** at root scope. Framework retains 6 artifact types. Phase 5 picks up `vmodel-skill-elicit-requirements`. See `BACKLOG §3.4` + `archive/phase4/PHASE4_AUTHORING_PATTERN.md` §7.
- **Phase 5** — Skills (craft authoring + review per artifact; framework skills; **stakeholder-elicitation skill carried over from Phase 4**). Consumes Phase 3 schemas + Quality Bar JSON directly.
- **Phase 6** — Tools (each a separate product repo, built via framework — dogfooding). Gates on Build workflow design (deferred).
- **Phase 7** — Retrofit-specific additions.

Full details in `docs/plan/BACKLOG.md`.

**Pre-pivot work preserved and operational:**
- Lower V docs (`source-code.html`, `unit-test.html`, `detailed-design.html`) — treated as pre-pivot outputs; content mined as reference, pages replaced in Phase 2.
- Skills `develop-code`, `derive-test-cases`, `vmodel-skill-review-code` — operational.
- C2–C4 DD skills paused; superseded by Phase 5 structure.

---

## Documentation

### docs/guide/ (Interactive HTML)

**Keep documentation in sync with every change.** Whenever components are updated (schemas, trace model, skills), the corresponding section in `docs/guide/` must also be updated.

**Domain translation plugin — parked.** Content is authored in direct software-engineering English. The runtime translation plugin and domain JSON files were archived to `archive/pre-pivot-2026-04-18/domains/` in Phase 2; `js/domain.js` was removed; page-level `[data-term]` wiring was stripped. The mechanism (or a simpler successor) may be reintroduced once content is stable (see `TARGET_ARCHITECTURE §15`).

**Proactively raise this:** when working on component changes, always remind that `docs/guide/` needs a corresponding update before the work is considered complete.

### Per-Artifact Documentation Structure (5 sections)

1. **V-model context** — what, where, why (how the artifact fits in the development flow).
2. **Best practices** — how to produce quality output (the bulk).
3. **Anti-patterns** — common mistakes.
4. **Examples** — good and bad.
5. **Quality Bar** — concrete Yes/No checklist grouped by concern. Canonical JSON form lives under `schemas/artifacts/quality-bar/` (extracted in Phase 3) and is consumed by templates + authoring/review skills.

Framework-integration and AI-skills-integration coverage — which appeared as separate sections in an earlier draft — are craft-orthogonal and belong in tool docs and skill docs respectively, not in per-artifact craft pages.

This documentation is the source of truth for craft. AI craft skills are distilled from it. Write docs first, then derive skills.

---

## Key Concept: Spec Ambiguity Test

Every specification artifact — at every layer — must be unambiguous enough that a junior engineer or low-mid-tier AI could act on it without guessing. This is the meta-gate for all specification work and the completeness test for every artifact.

---

## Domain

- V-model as **engineering infrastructure** — safety-specific framing removed. The techniques (layered decomposition, traceability, design-test coupling) apply at any rigor level.
- **Direct software-engineering English** in authored content; the pre-pivot translation plugin for standards vocabulary (DO-178C, ASPICE, ISO 26262) is deferred (see Documentation section above).
- EARS syntax is a craft skill preference, not a framework requirement.

---

## First Pilot Target

- Java 17, Gradle, JUnit 5.
- Legacy codebase: ~100k+ lines, ~10% test coverage, no documentation.
- Requirements in mixed formats (Word, spreadsheets, DOORS) — possibly incomplete or stale.
- Target user: mid-senior engineers orchestrating AI agents.

---

## Key Concept: DRTDD

Design-Requirement-Test Driven Development extends TDD:
```
REQUIRE → DESIGN → TEST(red) → IMPLEMENT(green) → REFACTOR → VERIFY
```
Each phase produces traceable artifacts. Human gates between phases.

---

## Repository Structure

```
archive/                — Pre-pivot content preserved (pre-pivot-2026-04-18/) + per-phase handoff docs (phase2/, phase3/)
research/               — Research documents on standards, patterns, strategies
docs/plan/              — Architecture (TARGET_ARCHITECTURE.md) and backlog (BACKLOG.md); per-phase handoff docs live here while a phase is active
docs/guide/             — Interactive HTML documentation (V-model guide + framework docs)
  css/                  — Styling
  js/                   — app.js (nav)
  artifacts/            — Per-artifact HTML pages (post-pivot 5-section structure)
  skills-architecture.html — pre-pivot; to be rewritten in Phase 5
schemas/
  core/                 — Meta-schemas (skill contracts, pipeline contracts)
  artifacts/            — Per-artifact JSON schemas (draft 2020-12) + envelope + common-defs
    quality-bar/        — Quality Bar meta-schema + per-artifact checklists
    fixtures/            — Minimal-example Markdown fixtures (one per artifact type)
  traceability/         — Link-type + validation-rule catalogs (with their schemas)
.claude/skills/         — Installed AI skills (some operational, more in Phase 5)
```

**Planned directory** (Phase 2+ introduces as artifact authoring begins):

```
/specs/                 — Spec artifacts (scope tree = directory tree)
  product_brief.md      — root
  requirements.md       — root
  architecture.md       — root
  testspec.md           — root
  /adrs/                — flat, cross-cutting
  /{scope}/             — branch / leaf scopes recurse
```

---

## Conventions

- Artifact files: single-file Markdown, YAML front-matter + embedded YAML blocks + Mermaid.
- Terminology: direct software-engineering English; standards-vocabulary translation layer deferred (see Documentation section).
- **Uniform high rigor.** No per-artifact rigor tiers.
- All framework outputs include verification via Quality Bar checklists.
- IDs are stable across file renames; traceability links reference IDs, not paths.
- EARS is used by craft skills but not enforced by schemas.

---

## Engineering Codex (shared knowledge bank)

A persistent knowledge bank lives at `/home/stefanus/repos/engineering-codex/`. It contains synthesized, cited research on software engineering standards, code quality, system design, and AI-augmented development — material that informs decisions in this project.

**Before doing fresh research on a topic, check the codex first:**

1. Read `/home/stefanus/repos/engineering-codex/CLAUDE.md` to understand the schema (one-time per session).
2. Read `/home/stefanus/repos/engineering-codex/index.md` to see what pages exist.
3. Read the relevant wiki pages. Load-bearing claims should be verified against the cited source pages.

**After doing research that produced reusable findings, offer to file them back:**

Tell the human: "I found X while researching Y. This looks like a candidate for the engineering-codex. Want me to ingest the source and update relevant pages?"

**Do not duplicate codex content here.** Link to it instead.

**The codex is read-only from this project unless the human explicitly asks you to ingest something into it.** Treat writes to `engineering-codex/` as a deliberate cross-repo action requiring confirmation.
