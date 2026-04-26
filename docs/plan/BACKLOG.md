# Backlog

Execution plan for the VModelWorkflow framework following the 2026-04-18 pivot. Action-oriented. For architectural rationale, see `TARGET_ARCHITECTURE.md`.

---

## 1. Current State

- **Pivot committed 2026-04-18.** Pre-pivot design record preserved at `archive/pre-pivot-2026-04-18/`.
- **Phase 0** — archival — **DONE** (commit `69330f3`, 2026-04-18).
- **Phase 1** — foundation rewrite (this document + `TARGET_ARCHITECTURE.md` + `CLAUDE.md`) — **DONE**.
- **Phase 2** — per-artifact documentation + cleanup — **DONE** (2026-04-22). Six artifact pages authored under the 5-section structure, landing-page rewrite against TARGET_ARCHITECTURE, domain-plugin machinery unwired, pre-pivot artifact pages archived. See §3.2.
- **Phase 3** — schemas + traceability + Quality Bar — **DONE** (2026-04-23). Six per-artifact JSON Schemas (draft 2020-12), envelope + common-defs, traceability link-types + validation-rules catalogs, Quality Bar container + six per-artifact checklists, six minimal-example fixtures round-tripping clean through the schemas. `PHASE3_AUTHORING_PATTERN.md` archived to `archive/phase3/` on completion. See §3.3.
- Phases 4–7 — pending.

**Preserved and operational:**
- Craft skills `develop-code` (Haiku eval 91%, +13% over baseline), `derive-test-cases` (100%, +3%), `vmodel-skill-review-code` (95.8%, +25%).
- C2–C4 detailed-design skills (develop-dd, retrofit-dd, review-dd) — paused; superseded by Phase 5 structure.
- 20 research documents in `research/` (system-level, implementation, detailed-design, pillar3) — reusable **as secondary substrate with an explicit safety-bias caveat** (authored pre-pivot with ASPICE/DO-178C framing; extract craft, discard framing).

**Archived during Phase 2:**
- Pre-pivot artifact HTML pages (`completeness-analysis`, `conops`, `stakeholder-needs`, `system-requirements`, `source-code`, `unit-test`) — moved to `archive/pre-pivot-2026-04-18/docs-guide-artifacts/`; superseded by the six post-pivot pages.
- Domain translation layer — `docs/guide/domains/*.json` archived to `archive/pre-pivot-2026-04-18/domains/`; `js/domain.js` deleted; CSS and page wiring stripped. Plugin mechanism parked for post-Phase 2 revisit (see `TARGET_ARCHITECTURE §14`, §15).
- `PHASE2_AUTHORING_PATTERN.md` — moved to `archive/phase2/` after Phase 2 completion. Historical reference for the authoring conventions established while pages were being written.

---

## 2. Pivot Reference

The canonical 2026-04-18 pivot decisions are recorded in:

- `archive/pre-pivot-2026-04-18/status.md` — full design-session record (Q1–Q7 and the NQ questions).
- `archive/pre-pivot-2026-04-18/README.md` — pivot summary + old → new concept mapping.

Architectural rationale — including the full Q8–Q15 and NQ-B/C/D/E decisions — is captured in `TARGET_ARCHITECTURE.md`. This backlog indexes it; it does not duplicate.

---

## 3. Migration Phases

### 3.1 Phase 1 — Foundation Rewrite (in progress)

**Goal:** Align `BACKLOG.md`, `TARGET_ARCHITECTURE.md`, and `CLAUDE.md` with the new model so future sessions can pick up without needing the 2026-04-18 design transcript.

**Tasks:**
- [x] Rewrite `docs/plan/TARGET_ARCHITECTURE.md` against the 6-artifact model.
- [x] Rewrite `docs/plan/BACKLOG.md` as the post-pivot execution plan.
- [x] Update `CLAUDE.md` to remove pre-pivot framing and introduce the new plan + principles.

**Deliverables:** This commit.

**Success criteria:** A future Claude session loading `CLAUDE.md` + MEMORY.md + these two planning files can understand the framework and next steps without needing the 2026-04-18 session transcript.

**Dependencies:** None (first commit after Phase 0).

### 3.2 Phase 2 — Per-artifact Documentation — DONE (2026-04-22)

**Goal:** Rewrite `docs/guide/artifacts/*.html` for the six artifact types under the **5-section structure** (V-model context, best practices, anti-patterns, examples, Quality Bar). Whole document per artifact; no length target; information density over length.

**Artifact pages** — all six authored under the 5-section structure:

- [x] **ADR** — 2026-04-19 (commit `8b4fea2`). Pattern-setter; established voice, section ordering, example style.
- [x] **Detailed Design** — 2026-04-19 (commit `8328d33`). Compression exercise against the largest pre-pivot page; pre-pivot Section 8 "Test Strategy" removed.
- [x] **Product Brief** — 2026-04-19. Consolidates pre-pivot `stakeholder-needs` + `conops` + `completeness-analysis` into one ~6.5k-word page. NFR Discovery Checklist (10 canonical dimensions) and Mermaid Onion Model introduced.
- [x] **Requirements** — 2026-04-20. Eight Best-Practice subsections; seven worked examples in one session-management domain. Generalist-gap audit formalised as authoring step.
- [x] **Architecture** — 2026-04-20. Greenfield page; nine Best-Practice subsections including dedicated Composition-patterns catalog; four Mermaid diagrams. `TARGET_ARCHITECTURE` §5.1 + §6 + §8.4 + §15 amended pre-authoring after industry-practice challenge to the wiring-as-leaf-sibling rule.
- [x] **TestSpec** — 2026-04-21. Greenfield replacement for pre-pivot `unit-test.html`. Nine Best-Practice subsections with per-layer weight table; four §1 bridge cards; three Mermaid diagrams; empirical LLM testing data carried on page per "docs are superset of skills" rule; 13 anti-patterns.

**Cross-cutting tasks** — all complete as of 2026-04-22:

- [x] **Domain translation plugin machinery removed** — `js/domain.js` and `js/v-diagram.js` deleted; domain-switcher UI, `.domain-switcher` and `[data-term]` CSS, per-page translation hooks stripped.
- [x] **Domain JSON files archived** — `docs/guide/domains/*.json` moved to `archive/pre-pivot-2026-04-18/domains/`; domains directory removed.
- [x] **Pre-pivot artifact pages archived** — `completeness-analysis.html`, `conops.html`, `stakeholder-needs.html`, `system-requirements.html`, `source-code.html`, `unit-test.html` moved to `archive/pre-pivot-2026-04-18/docs-guide-artifacts/`. Pre-pivot validation script moved alongside.
- [x] **Landing page rewrite** — `docs/guide/index.html` rewritten against `TARGET_ARCHITECTURE`: Introduction, Core Principles, Artifact Model, Per-Artifact Craft links, Workflows, Further Reading. E2E walkthrough, Three Pillars, DRTDD, Assurance Levels, domain vocabulary, and pre-pivot traceability sections removed along with stale inline artifact stubs.
- [x] **Post-pivot artifact quality review** — light-edit pass against mid-senior audience fit, voice discipline, and TARGET alignment. Hard rule violations (HALT reference, DO-178C standards-defensive citation, residual `js/domain.js` script tag) removed. Voice trims and density trims applied across all six pages. `TARGET_ARCHITECTURE` reconciled: `affects` → `affected_scopes`; TestSpec `type` enum expanded from 5 to 11 values to match the craft page.

**Deliverables (all produced):**
- Six `docs/guide/artifacts/*.html` pages under the 5-section structure.
- Rewritten `docs/guide/index.html`.
- Pre-pivot artifact pages, domain plugin JSON, and domain plugin runtime archived.
- `PHASE2_AUTHORING_PATTERN.md` — written during the phase and archived to `archive/phase2/` on Phase 2 completion.

**Dependencies:** Phase 1. **Blocks:** Phase 3 (schemas + Quality Bar JSON derive from these HTML pages).

### 3.3 Phase 3 — Schemas — DONE (2026-04-23)

**Goal:** Per-artifact schemas derived from the Phase 2 docs; traceability schema; **Quality Bar JSON extraction (canonical machine format)** per artifact. All schemas in one phase. JSON Schema draft 2020-12 throughout (see `PHASE3_AUTHORING_PATTERN.md §2`, archived on Phase 3 completion).

**Filename convention.** Tasks below originally listed `.schema.yaml`; Phase 3 landed on JSON Schema draft 2020-12 with `.schema.json` extensions (see archived `PHASE3_AUTHORING_PATTERN.md §2`). The `.json` suffix is the as-built state.

**Tasks:**
- [x] `schemas/artifacts/product-brief.schema.json` — new (commit `5b84d62`).
- [x] `schemas/artifacts/requirements.schema.json` — new against post-pivot Requirements structure (commit `7742783`).
- [x] `schemas/artifacts/architecture.schema.json` — new with mandatory Composition section (commit `a392c94`).
- [x] `schemas/artifacts/adr.schema.json` — new with Nygard status vocabulary + reversibility sub-prompt + human-only-field `recovery_status` narrowing (commit `0dcba63`).
- [x] `schemas/artifacts/detailed-design.schema.json` — new; pre-pivot Section 8 "Test Strategy" removed; DbC shape lifted into `$defs/public_interface_entry` (commit `3854f9e`).
- [x] `schemas/artifacts/test-spec.schema.json` — new; load-bearing `verifies: minItems 1` at both artifact and per-case levels (commit `c103964`).
- [x] `schemas/artifacts/envelope.schema.json` + `schemas/artifacts/common-defs.schema.json` — foundation; vocabulary ownership lock (envelope = shape; per-artifact = enum) landed in `4a6a4d7` after ADR's Nygard override (commits `a00181f`, `4a6a4d7`).
- [x] `schemas/traceability/` — four files: `link-types.schema.json` + `link-types.catalog.json` (9 link types), `validation-rules.schema.json` + `validation-rules.catalog.json` (13 rules) (commit `ca07b4b`). Pre-pivot `link-types.yaml` + `trace.schema.yaml` archived alongside.
- [x] **Quality Bar container + six per-artifact JSON files** — `quality-bar.schema.json` meta-schema + ADR exemplar (`d012dec`), Product Brief (`c3b6800`), Requirements (`1ae007e`), Detailed Design (`7a4920a`), Architecture (`81b20f7`), TestSpec (`d16972b`). Five parallel Sonnet subagent extractions; `applies_at` exercised root-only (Architecture) and across all three layers with one multi-value case (TestSpec).
- [x] **Minimal-example fixtures per artifact** (task #13) — six Markdown fixtures under `schemas/artifacts/fixtures/`, round-tripping clean through their respective schema + envelope + common-defs registries. LinkSnip URL-shortener scenario with cross-references (REQS → PB, ARCH → PB+REQS, DD → ARCH+REQs, TS → DD) (commit `f194465`).
- [x] **TRV-QB-001 activation** — Quality Bar cascade rule flipped from `deferred_until: phase_3_task_3_and_12` to active on Phase 3 closeout. Runtime enforcement deferred to Phase 5/6 tooling.

**Deliverables (all produced):**
- Six per-artifact schemas + envelope + common-defs under `schemas/artifacts/`.
- Four traceability schemas/catalogs under `schemas/traceability/`.
- Seven files under `schemas/artifacts/quality-bar/` (one container schema + six data files).
- Six minimal-example Markdown fixtures under `schemas/artifacts/fixtures/`.
- `PHASE3_AUTHORING_PATTERN.md` — written during the phase, archived to `archive/phase3/` on Phase 3 completion.

**Success criteria (met):** All schemas mechanically validate `schemas/artifacts/fixtures/*.example.md` with zero errors via `jsonschema` (draft 2020-12) + `referencing`. Quality Bar JSON parses against the meta-schema and is consumable by a mechanical checker (per-group `kind`, per-item `applies_at` + `references`).

**Dependencies:** Phase 2 (docs are source of truth; Quality Bar HTML sections authored there).

### 3.4 Phase 4 — Product Descriptions for Purpose-built Tools

**Goal:** Kick off dogfooding by authoring Product Descriptions (PD) for the three purpose-built tool products defined in `TARGET_ARCHITECTURE §10`. Context only — does not depend on skills existing yet.

**Pivot during Phase 4 (2026-04-26):** The original deliverable was Product Briefs (PB). Authoring the `vmodel-core` PB surfaced that the PB craft shape is implicitly business-product-flavoured and produces ceremonial overweight for engineering-internal / dogfooded products. A new artifact type — **Product Description (PD)** — was introduced as the engineering-flavoured alternative. PB is preserved as an option for products with real markets; PD is the right anchor for engineering products. See `PHASE4_AUTHORING_PATTERN.md` Finding #9 for the full reasoning and follow-up actions.

**Rationale for three products (not ten capabilities):** the anchor artifact (PD or PB) anchors a product, not a function. The ten `§10` capabilities are cohesion-grouped into three products by stakeholder cut and lifecycle. See `TARGET_ARCHITECTURE §10` for the full grouping.

**Tasks:**
- [x] Product Brief for `vmodel-core` (business-PB experiment; archived to `archive/phase4-business-pb-experiment/`).
- [ ] Product Description for `vmodel-core` (pilot — pattern-setter for PD shape).
- [ ] Product Description for `vmodel-author`.
- [ ] Product Description for `vmodel-retrofit`.

**Deliverables:** Three Product Descriptions under `docs/plan/phase4-tool-briefs/{core,author,retrofit}/product_description.md` — staging directory, lifts to each tool's own repo at Phase 6 kickoff. Each PD includes a §6 *References and Dependencies* manifest listing the framework files (TARGET_ARCHITECTURE excerpts, schemas, traceability rules, sibling-tool references) that must accompany the PD when it lifts to its own repo.

**Success criteria:** Each PD covers vision, functional scope, non-functional scope, out-of-scope, assumptions, and references with sufficient density that a fresh repo containing the PD plus the §6-listed dependencies allows independent design and build to continue. Schema validation deferred — PD schema is authored after the three concrete PDs land (schema-after-artifact, see Finding #9 in `PHASE4_AUTHORING_PATTERN.md`).

**Dependencies:** Phase 2 docs (PB craft doc as informative reference; PD craft doc to be authored in Phase 2 revision after Phase 4 closeout).

### 3.5 Phase 5 — Skills (craft + framework)

**Goal:** Build per-artifact authoring + review skills, plus framework skills (orchestration, traceability, retrofit). Quality Bar content is already in place — HTML in Phase 2, canonical JSON in Phase 3 — and is consumed here.

**Tasks (per-artifact skills):**
- [ ] `vmodel-skill-author-product-brief` and `vmodel-skill-review-product-brief`.
- [ ] `vmodel-skill-author-requirements` and `vmodel-skill-review-requirements`.
- [ ] `vmodel-skill-author-architecture` and `vmodel-skill-review-architecture`.
- [ ] `vmodel-skill-author-adr` and `vmodel-skill-review-adr`.
- [ ] `vmodel-skill-author-detailed-design` and `vmodel-skill-review-detailed-design` (supersedes C2–C4).
- [ ] `vmodel-skill-author-testspec` and `vmodel-skill-review-testspec`.

**Tasks (framework skills):**
- [ ] `vmodel-skill-traceability` — link creation, validation wrapper.
- [ ] `vmodel-skill-orchestration` — pipeline controller, research / plan session handoffs.
- [ ] `vmodel-skill-retrofit` — four-phase retrofit mode (topology → leaves → branches+root → optional Product Brief), enforces `recovery_status` discipline and the no-fabrication rule at the skill level.

**Tasks (skills architecture rewrite):**
- [ ] Rewrite `docs/guide/skills-architecture.html` for the new 6-artifact model (pre-pivot version is stale).

**Deliverables:** Skill directories under `.claude/skills/`; updated `skills-architecture.html`. (Quality Bar JSON is a Phase 3 deliverable, consumed here.)

**Success criteria:**
- Each skill passes `/skill-creator` evaluation on Haiku at agreed thresholds.
- Review skills reject artifacts that fail the Spec Ambiguity Test.
- Retrofit skill refuses to populate human-only fields with AI inference (tested adversarially).

**Dependencies:** Phase 3 (schemas); Phase 2 docs.

### 3.6 Phase 6 — Tools (purpose-built)

**Goal:** Build the purpose-built tools from `TARGET_ARCHITECTURE §10`, each as its own project repo, using our framework (dogfooding).

**Tasks:**
- [ ] For each tool: execute Build workflow on its Product Brief (Phase 4) → Requirements → Architecture → Detailed Design → TestSpec → code + tests.
  - Artifact parser, schema validator, traceability validator, Quality Bar structural runner, graph builder, query engine, scaffolder, renderer, topology discovery, gap report aggregator.

**Deliverables:** Independently built tool repos; compiled binaries available; each project using the framework configures which tools are present.

**Success criteria:**
- Each tool has a complete specification.
- Each tool has a test suite derived from its TestSpec.
- CLI contracts match `TARGET_ARCHITECTURE §10` (`--format json|text`, TTY-aware defaults, actionable errors).

**Dependencies:** Phase 5 (skills for authoring + reviewing specs); the Build workflow (deferred — see §4).

> **Note:** Phase 6 cannot complete until the Build workflow is designed. Phase 6 Product Briefs and specs (Phases 4+5 output) can be prepared now; actual code generation waits on the Build workflow design session.

### 3.7 Phase 7 — Retrofit-specific Additions

**Goal:** Specialised retrofit capabilities beyond the generic Spec-workflow-reversed flow.

**Tasks:**
- [ ] Topology discovery — code → scope tree proposal (dependency analysis, bounded-context detection).
- [ ] Recovery-status handling — enforce allowed states per field type; skill-level refusal of fabrication on human-only fields.
- [ ] Gap report generation — aggregate the six gap categories from `TARGET_ARCHITECTURE §8.2`.
- [ ] Legacy retrofit skills — code structure analysis, behaviour characterisation, requirement inference, design inference, gap analysis, cross-session handoff documents for large analyses.

**Deliverables:** Retrofit-ready skill + tool set; first-pilot retrofit possible.

**Success criteria:** A legacy Java 17 / Gradle / JUnit 5 codebase (pilot target) can be retrofitted into a scope tree + DDs + Architectures + Requirements + gap report — without AI-fabricated rationale.

**Dependencies:** Phases 5 + 6.

---

## 4. Deferred / Parked

Items explicitly out of current phasing:

- **Build workflow design** — separate design session.
- **Human guards** — to revisit if uniform high rigor proves insufficient.
- **Rigor tiers** — may return if human guards require them; not before.
- **Web GUI for traceability** — after the framework stabilises.
- **Plan schemas** — development plan, verification plan, CM plan, QA plan.
- **C2–C4 detailed-design skills** — paused; superseded by Phase 5 structure.
- **System test documentation + skills as separate artifacts** — folded into TestSpec at root scope.
- **Review Record schema** as a separate artifact — deferred until a concrete need surfaces.
- **Safety-level / assurance-level configuration** — removed by pivot; preserved in archive.
- **Scaffold tool concept as a standalone design** — absorbed into the scaffolder in Tools (`TARGET_ARCHITECTURE §10`).

---

## 5. Preserved Content Inventory

Content that survives the pivot verbatim or with minor adaptation:

**Documentation (pre-pivot outputs — inspection-only reference for Phase 2, not substrate; see §3.2):**
- `docs/guide/artifacts/source-code.html` — reference only; content feeds the future Build-workflow / implementation doc (not a Phase 2 artifact-page target).
- `docs/guide/artifacts/unit-test.html` — reference only; craft insights feed the TestSpec (unit-level) page.
- `docs/guide/artifacts/detailed-design.html` — reference only; Layer 1/2/3 model worth mining for the new Detailed Design page.
- `docs/guide/artifacts/adr.html` — reference only; closest to pattern, still replaced under the 5-section structure.
- `docs/guide/domains/` — **to be archived** to `archive/pre-pivot-2026-04-18/domains/` as a Phase 2 task. Plugin mechanism deferred; may be reintroduced post-Phase 2 (see `TARGET_ARCHITECTURE §14`, §15).

**Research (under `research/`):**
- `system-level/01-standards-system-level-processes.md` — standards overview; reference material for any future translation layer work (§3.2 defers the plugin).
- `system-level/02a–02d` (stakeholder craft) — feeds Product Brief authoring knowledge.
- `system-level/03a–03d` (requirements craft) — feeds Requirements authoring.
- `system-level/05a–05f` (architecture craft) — feeds Architecture authoring.
- `system-level/06a` (AI at system level) — feeds upper-V skill design.
- `implementation/*` (unit test, clean code, etc.) — feeds TestSpec and Build-workflow reference.
- `detailed-design/*` (except archived `design-scaling-tiering.md`) — feeds Detailed Design authoring and retrofit.
- `pillar3/*` — feeds skills architecture rewrite.

**Skills (operational):**
- `.claude/skills/develop-code/` — integrated into Phase 5 author-detailed-design skill or evolves as an implementation skill when Build workflow lands.
- `.claude/skills/derive-test-cases/` — evolves into Phase 5 author-testspec skill.
- `.claude/skills/vmodel-skill-review-code/` — role refined in Phase 5.

**Schemas:**
- `schemas/core/craft-skill.schema.yaml`, `schemas/core/vmodel-config.schema.yaml` — refresh in Phase 3 if needed.
- `schemas/traceability/` — update in Phase 3 against `TARGET_ARCHITECTURE §7`.

**Operational procedures to preserve:**
- The "Research Before Documentation" discipline (three research categories: standards, craft, AI-specific; attributed claims, real sources, no fabrication). Reframe for new artifact set but preserve the rigor bar.
- `/skill-creator` usage conventions and Haiku-baseline evaluation for all skills.

---

## 6. Operational Open Questions

Questions tied to upcoming phases. Architectural open questions live in `TARGET_ARCHITECTURE §15`.

1. **Quality Bar container format.** Structure for the canonical per-artifact checklist file. Resolved 2026-04-23 (commit `d012dec`): JSON Schema draft 2020-12; `schemas/artifacts/quality-bar/` layout; see `PHASE3_AUTHORING_PATTERN.md §2 Quality Bar container`. HTML Quality Bar sections from Phase 2 are the authoritative source for content.
2. **Topology discovery algorithm choice.** Dependency analysis vs package boundaries vs bounded-context detection. Phase 7 decision; pilot-driven.
3. **Scaffolder CLI shape.** Exact commands and options for the scaffolder tool. Phase 6.
4. **Project config format (e.g., `.vmodel/tools.yaml`).** How projects declare available tools. Draft needed before Phase 5's framework skills reference it.
5. ~~**Dogfooding kickoff order.**~~ Resolved 2026-04-23: three-product consolidation (`vmodel-core`, `vmodel-author`, `vmodel-retrofit`) per §3.4 update. `vmodel-core` is the pattern-setter pilot; `vmodel-author` and `vmodel-retrofit` follow once the pattern is locked.
6. **Legacy pilot start date.** When to attempt a pilot retrofit (Java 17 / Gradle / JUnit 5 / ~100k LOC / ~10% coverage). Depends on Phases 5 + 7 being at least partially operational.

---

## 7. Guiding Principles (cross-reference)

The ten core principles are defined in `TARGET_ARCHITECTURE §3`. This list is a quick index; the canonical wording is there.

1. Documentation is the foundation.
2. Spec Ambiguity Test.
3. Uniform high rigor (no tiers).
4. Workflow decoupling.
5. Tool/skill split.
6. Tools as independent products.
7. Retrofit no-fabrication.
8. Human leverage at the spec level.
9. Components are independent (SOLID).
10. Tests derive from the layer's artifact.

**Additional operational principles** (not duplicated in `TARGET_ARCHITECTURE`):

- **Discuss before writing.** Explain approach, show structure visually, motivate choices, get approval.
- **Small increments.** One concept at a time.
- **Model-tier aware.** Skills must work on cheapest viable tier; test with baseline comparison.
- **Follow agentskills.io SKILL.md format.**
- **Use `/skill-creator`** for skill development: draft, test, evaluate, iterate.
- **Dispatch concrete execution to subagents** — keep main conversation focused on design and alignment.

---

## 8. Build Order (summary)

```
Phase 0 (done) → Phase 1 (this commit) → Phase 2 (docs)
                                          │
                                          ▼
                                        Phase 3 (schemas)
                                          │
                             ┌────────────┴────────────┐
                             ▼                         ▼
                        Phase 4 (tool PBs)       Phase 5 (skills)
                                                       │
                                                       ▼ (needs Build workflow, deferred)
                                                   Phase 6 (tools)
                                                       │
                                                       ▼
                                                   Phase 7 (retrofit additions)
```

**Critical path:** 0 → 1 → 2 → 3 → 5 → 6 → 7. Phase 4 can proceed in parallel with Phase 5. Phase 6 gates on the **Build workflow** design (separate effort — see §4 Deferred).

---

*Last updated: 2026-04-23 (Phase 4 kickoff — §3.4 three-product consolidation; §6 Q5 closed).*
