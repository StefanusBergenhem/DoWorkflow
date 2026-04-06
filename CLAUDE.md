# VModelWorkflow — V-Model Development Framework

## What This Project Is

Four independent components designed to work together for V-model compliant software development:

1. **Documentation** — Single source of truth for all domain knowledge. Per artifact type: V-model context, best practices, anti-patterns, examples, framework integration. AI skills are derived from this. Non-negotiable foundation.
2. **Templates & Schemas** — Artifact definitions, envelopes, checklists. If followed, produce V-model compliant artifacts that pass assessment. Usable by humans, agents, or both.
3. **Traceability** — Data model and validation engine for linking artifacts, validating completeness, detecting gaps. Coupled to our templates. Usable by humans, CI/CD, agents, or anyone.
4. **AI Skills** — Two categories: craft skills (standalone best practices, framework-independent, derived from documentation) and framework skills (VModelWorkflow-specific orchestration, template integration, traceability).

**End goal:** V-model compliant software, developed by AI agents, fully understood and verified by human engineers.

**Primary use cases:**
- **Greenfield development** (top-down through V-model layers)
- **Legacy retrofit** (bottom-up reverse-engineering from existing code — primary market entry point)

**Each component is independently usable.** A human team can use templates alone. Someone can use just a craft skill. But the real value is the combination.

## Working Process

- **Discuss before writing.** Never start writing files without first explaining the approach, showing structure visually (ASCII diagrams), motivating the design choices, and getting explicit approval.
- **Structured back-and-forth.** Present ideas, ask for input or approval, then implement. This applies to schemas, skills, prompts, and any new files.
- **Small increments.** One concept at a time. Get alignment, then move on.

## Human-Agent Interaction Model

Human drives at the strategic level, AI executes at the tactical level, human verifies at the quality gate. This is NOT an autonomous pipeline with human checkpoints.

Per V-model layer:
1. **Research/Plan (human-driven):** Human provides context, AI gathers and analyzes, back-and-forth discussion until agreed plan
2. **Implementation (agent-orchestrated):** AI writes artifacts, self-checks, sends to review agent, feedback loop, traceability updated
3. **Final Review (human-driven):** Human reviews output, approves or rejects
4. **Human transitions to next V-model layer**

## Build Order

Bottom-up, one V-model layer at a time. For each layer: **documentation first**, then template, then craft skill, then framework skill.

Current: Phases 1+2 documentation complete (Code, Unit Tests, Detailed Design)
Next: Skills for the lower V (code + unit tests + detailed design)

See `docs/plan/BACKLOG.md` for full backlog and `docs/plan/TARGET_ARCHITECTURE.md` for architecture.

## Design Principles

- **Documentation is the foundation**: Write docs first, derive everything else from them. If we can't explain it, we can't claim AI skills will produce compliant output.
- **Components are independent (SOLID)**: No forced coupling. Adopt incrementally.
- **Individual orchestration per layer**: Each V-model layer gets its own implementation loop. Refactor to shared patterns only after building 3-4 layers.
- **Craft vs Framework skill separation**: Craft skills are standalone best practices (no framework knowledge). Framework skills handle templates, traceability, orchestration.
- **Contract-driven**: Skills communicate through typed YAML schemas.
- **Language-agnostic**: Portable across Java, C++, and other languages.
- **Model-tier aware**: Skills must work with smaller/older LLMs — test on Haiku with baseline comparison.
- **Incremental**: Works on legacy codebases module-by-module, not all-at-once.
- **Human-gated**: Every artifact starts as draft, requires human approval.
- **Deterministic where possible**: Traceability validation is a tool concern, not an agent concern. Agents create, tools verify.

## Domain

- Targets DO-178C/DO-330 (aviation), ASPICE/ISO 26262 (automotive), and other V-model standards
- Uses **generic V-model terminology** with translation documentation per domain
- EARS syntax is a craft skill preference, not a framework requirement

## First Pilot Target

- Java 17, Gradle, JUnit 5
- Legacy codebase: 100k+ lines, ~10% test coverage, no documentation
- Requirements in mixed formats (Word, spreadsheets, DOORS) — possibly incomplete or stale
- Target user: mid-senior engineers orchestrating AI agents

## Key Concept: DRTDD

Design-Requirement-Test Driven Development extends TDD:
```
REQUIRE -> DESIGN -> TEST(red) -> IMPLEMENT(green) -> REFACTOR -> VERIFY
```
Each phase produces traceable artifacts. Human gates between phases.

## Repository Structure

```
research/              — Research documents on standards, patterns, strategies
docs/plan/             — Architecture (TARGET_ARCHITECTURE.md) and backlog (BACKLOG.md)
docs/guide/            — Interactive HTML documentation (V-model guide + framework docs)
  css/                 — Styling
  js/                  — domain.js (translation plugin), app.js (nav), v-diagram.js (SVG)
  domains/             — Domain translation plugins (generic.json, do178c.json, aspice.json)
schemas/
  core/                — Meta-schemas (skill contracts, pipeline contracts)
  artifacts/           — V-model artifact type definitions
  traceability/        — Link model and validation rules
  translations/        — Domain-specific term mappings
  safety-levels/       — Assurance level configurations
skills/
  craft/               — Standalone domain skills (framework-independent)
  orchestration/       — Framework-specific workflow skills
```

## Documentation

### docs/guide/ (Interactive HTML)

**Keep documentation in sync with every change.** Whenever components are updated (schemas, trace model, skills), the corresponding section in `docs/guide/index.html` must also be updated.

The documentation uses a domain translation plugin system (SOLID: content uses generic terms via `data-term` attributes, domain-specific vocabulary is applied at runtime from JSON plugins). Never hard-couple domain-specific terms into the HTML content.

**Proactively raise this**: when working on component changes, always remind that docs/guide/ needs a corresponding update before the work is considered complete.

### Per-Artifact Documentation

Each artifact type gets comprehensive documentation covering:
1. V-model context (what, where, why)
2. Best practices (how to produce quality output)
3. Anti-patterns (common mistakes)
4. Examples (good and bad)
5. Framework integration (template, traceability links, AI skills)

This documentation is the source of truth. AI craft skills are distilled from it. Write documentation first, then derive skills.

## Conventions

- Artifact schemas: YAML
- Terminology: Generic V-model (with translation docs for DO-178C, ASPICE, etc.)
- Assurance level: optional property on artifacts, not universal. Default behavior is high-rigor.
- All framework outputs must include verification checklists
- EARS is used by our craft skills but not enforced by schemas
