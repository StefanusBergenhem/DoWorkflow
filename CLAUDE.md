# DoWorkflow — V-Model Development Framework

## What This Project Is

Three independent pillars that can be used standalone or combined:

1. **V-Model Compliance** — Schemas, templates, checklists defining what artifacts are needed for V-model compliance. Usable by humans, agents, or both.
2. **Traceability** — Data model and engine for linking artifacts, validating completeness, detecting gaps. Usable by humans, CI/CD, agents, or anyone.
3. **Agentic Skills** — Prompt packages that guide AI agents to produce quality V-model artifacts. Craft skills (standalone, composable) + orchestration (framework interaction).

**No pillar depends on the others.** A human team can use Pillar 1 alone. Pillar 2 can validate any YAML artifacts regardless of how they were created. Craft skills in Pillar 3 can be used individually outside this framework.

## Working Process

- **Discuss before writing.** Never start writing files without first explaining the approach, showing structure visually (ASCII diagrams), motivating the design choices, and getting explicit approval.
- **Structured back-and-forth.** Present ideas, ask for input or approval, then implement. This applies to schemas, skills, prompts, and any new files.
- **Small increments.** One concept at a time. Get alignment, then move on.

## Design Principles

- **Three independent pillars**: V-Model Compliance, Traceability, Agentic Skills. No cross-pillar dependencies where not strictly needed.
- **Craft vs Orchestration separation**: Craft skills teach *how* to do one thing well. Orchestration handles *when* and *what* to hand off. SOLID applied to the skill framework.
- **Contract-driven**: Skills communicate through typed YAML schemas. Orchestration routes using contracts, not internal knowledge.
- **Language-agnostic**: Must be portable across Java, C++, and other languages.
- **Model-tier aware**: Skills must work with smaller/older LLMs — crisp, unambiguous instructions.
- **Incremental**: Works on legacy codebases module-by-module, not all-at-once.
- **Human-gated**: Every artifact starts as draft, requires human approval.
- **Composable**: Many small skills, recombined into different workflows via orchestration.
- **Deterministic where possible**: Traceability validation is a tool concern, not an agent concern. Agents create, tools verify.

## Domain

- Targets DO-178C/DO-330 (aviation), ASPICE/ISO 26262 (automotive), and other V-model standards
- Uses **generic V-model terminology** with translation documentation per domain
- EARS syntax is a craft skill preference, not a framework requirement

## First Pilot Target

- Java 17, Gradle, JUnit 5
- Legacy codebase: 100k+ lines, ~10% test coverage, no documentation
- Requirements in mixed formats (Word, spreadsheets, DOORS) — possibly incomplete or stale

## Key Concept: DRTDD

Design-Requirement-Test Driven Development extends TDD:
```
REQUIRE -> DESIGN -> TEST(red) -> IMPLEMENT(green) -> REFACTOR -> VERIFY
```
Each phase produces traceable artifacts. Human gates between phases.

## Repository Structure

```
research/              — Research documents on standards, patterns, strategies
docs/plan/             — Architecture and backlog documents
docs/guide/            — Interactive HTML documentation (V-model guide + framework docs)
  css/                 — Styling
  js/                  — domain.js (translation plugin), app.js (nav), v-diagram.js (SVG)
  domains/             — Domain translation plugins (generic.json, do178c.json, aspice.json)
schemas/
  core/                — Meta-schemas (skill contracts, pipeline contracts)
  artifacts/           — V-model artifact type definitions (Pillar 1)
  traceability/        — Link model and validation rules (Pillar 2 data model)
  translations/        — Domain-specific term mappings
  safety-levels/       — Assurance level configurations
skills/
  craft/               — Atomic domain skills (Pillar 3)
  orchestration/       — Workflow pipelines (Pillar 3)
```

## Documentation (docs/guide/)

**Keep documentation in sync with every change.** Whenever any of the three pillars is updated (schemas, trace model, skills, orchestration), the corresponding section in `docs/guide/index.html` must also be updated. This includes:

- New or changed artifact schemas → update the artifact section + examples
- New or changed link types / trace rules → update the traceability section
- New or changed craft skills / orchestration → update the framework section
- New domain translations → add or update the JSON plugin in `docs/guide/domains/`
- Calculator E2E walkthrough should be extended when new artifact types are added

The documentation uses a domain translation plugin system (SOLID: content uses generic terms via `data-term` attributes, domain-specific vocabulary is applied at runtime from JSON plugins). Never hard-couple domain-specific terms into the HTML content.

**Proactively raise this**: when working on pillar changes, always remind that docs/guide/ needs a corresponding update before the work is considered complete.

## Conventions

- Artifact schemas: YAML
- Terminology: Generic V-model (with translation docs for DO-178C, ASPICE, etc.)
- Assurance level: optional property on artifacts, not universal. Default behavior is high-rigor.
- All framework outputs must include verification checklists
- EARS is used by our craft skills but not enforced by schemas
