# VModelWorkflow — AI-Augmented Spec-Driven Development Framework

## What This Project Is

Four independent components designed to work together for AI-augmented spec-driven development with V-model rigor:

1. **Documentation** — Single source of truth for all domain knowledge. Per artifact type: 7 sections (V-model context, best practices, anti-patterns, examples, framework integration, AI skills integration, Quality Bar). AI skills are derived from this. Non-negotiable foundation.
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

- **Phase 0** — Archival — **DONE** (commit `69330f3`, 2026-04-18).
- **Phase 1** — Foundation rewrite (BACKLOG, TARGET_ARCHITECTURE, this CLAUDE.md) — **IN PROGRESS**.
- **Phase 2** — Per-artifact documentation (6 types, ~1/4 current size, best practices only first pass).
- **Phase 3** — Schemas derived from docs.
- **Phase 4** — Product Briefs for purpose-built tools (context only; can run parallel with Phase 5).
- **Phase 5** — Skills (craft authoring + review per artifact; framework skills; Quality Bar YAML prereq).
- **Phase 6** — Tools (each a separate product repo, built via framework — dogfooding). Gates on Build workflow design (deferred).
- **Phase 7** — Retrofit-specific additions.

Full details in `docs/plan/BACKLOG.md`.

**Pre-pivot work preserved and operational:**
- Lower V docs (`source-code.html`, `unit-test.html`, `detailed-design.html`) — to be condensed ~75% in Phase 2.
- Skills `develop-code`, `derive-test-cases`, `vmodel-skill-review-code` — operational.
- C2–C4 DD skills paused; superseded by Phase 5 structure.

---

## Documentation

### docs/guide/ (Interactive HTML)

**Keep documentation in sync with every change.** Whenever components are updated (schemas, trace model, skills), the corresponding section in `docs/guide/` must also be updated.

The documentation uses a **domain translation plugin system** (SOLID: content uses generic terms; domain-specific vocabulary applied at runtime from JSON plugins). Never hard-couple domain-specific terms into HTML content.

**Proactively raise this:** when working on component changes, always remind that `docs/guide/` needs a corresponding update before the work is considered complete.

### Per-Artifact Documentation Structure (7 sections)

1. **V-model context** — what, where, why.
2. **Best practices** — how to produce quality output (the bulk).
3. **Anti-patterns** — common mistakes.
4. **Examples** — good and bad.
5. **Framework integration** — template, traceability links, AI skills.
6. **AI skills integration** — how craft and framework skills relate.
7. **Quality Bar** — concrete Yes/No checklist grouped by concern; also extracted as canonical YAML consumed by templates + authoring/review skills.

This documentation is the source of truth. AI craft skills are distilled from it. Write docs first, then derive skills.

---

## Key Concept: Spec Ambiguity Test

Every specification artifact — at every layer — must be unambiguous enough that a junior engineer or low-mid-tier AI could act on it without guessing. This is the meta-gate for all specification work and the completeness test for every artifact.

---

## Domain

- V-model as **engineering infrastructure** — safety-specific framing removed. The techniques (layered decomposition, traceability, design-test coupling) apply at any rigor level.
- **Generic terminology** internally; standards vocabulary (DO-178C, ASPICE, ISO 26262) applied via translation plugins.
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
archive/                — Pre-pivot content preserved (pre-pivot-2026-04-18/)
research/               — Research documents on standards, patterns, strategies
docs/plan/              — Architecture (TARGET_ARCHITECTURE.md) and backlog (BACKLOG.md)
docs/guide/             — Interactive HTML documentation (V-model guide + framework docs)
  css/                  — Styling
  js/                   — domain.js (translation plugin), app.js (nav), v-diagram.js (SVG)
  domains/              — Domain translation plugins (generic.json, do178c.json, aspice.json)
  artifacts/            — Per-artifact HTML pages (to be condensed + reworked in Phase 2)
schemas/
  core/                 — Meta-schemas (skill contracts, pipeline contracts)
  artifacts/            — Artifact type schemas (Phase 3 rewrite against 6-artifact model)
  traceability/         — Link model and validation rules (Phase 3 update)
  translations/         — Domain-specific term mappings
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
- Terminology: Generic V-model internally; standards vocabulary via translation layer.
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
