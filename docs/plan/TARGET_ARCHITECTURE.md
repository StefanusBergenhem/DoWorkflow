# Target Architecture

## Vision

A framework for V-model structured software development that combines documentation, templates, traceability, and AI skills into a coherent system. Each component is independently usable but designed to work together. The V-model is treated as engineering infrastructure, not just safety compliance — safety is the highest-rigor application, but the techniques have value at any rigor level. The end goal: well-structured software, developed by AI agents, fully understood and verified by human engineers.

## Components

Four independent components plus two primary use cases:

```
TEMPLATES & SCHEMAS          TRACEABILITY              DOCUMENTATION             AI SKILLS
(artifact definitions)       (link model + validation)  (single source of truth)  (AI-optimized knowledge)

┌──────────────────┐        ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│                  │        │                  │      │                  │      │                  │
│ Artifact envelope│        │ Link data model  │      │ V-model education│      │ Craft skills     │
│ Type schemas     │        │ Link type catalog│      │  (what, where,   │      │ (standalone best │
│ Checklists       │        │ Validation rules │      │   why, how)      │      │  practices, not  │
│ Assurance config │        │ Coverage analysis│      │                  │      │  framework-aware)│
│ Translation layer│        │ Impact analysis  │      │ Best practices   │      │                  │
│                  │        │                  │      │  per artifact    │      │ Framework skills │
│                  │        │ Coupled to our   │      │  (exhaustive)    │      │ (use templates,  │
│                  │        │ templates        │      │                  │      │  traceability,   │
│                  │        │                  │      │ Anti-patterns    │      │  orchestration)  │
│ Usable by:       │        │ Usable by:       │      │ Examples         │      │                  │
│ humans, agents,  │        │ humans, CI/CD,   │      │                  │      │ Usable by:       │
│ or both          │        │ agents, anyone   │      │ Framework manual │      │ AI agents        │
└──────────────────┘        └──────────────────┘      └──────────────────┘      └──────────────────┘

         │                          │                         │                         │
         └──────────────────────────┴─────────────────────────┴─────────────────────────┘
                                              │
                                    USE CASES (combine components)
                              ┌───────────────┴───────────────┐
                              │                               │
                     Greenfield V-model              Legacy Retrofit
                     development (top-down)           (bottom-up RE)
```

**No component depends on the others to function.**

- Templates alone: human team gets V-model artifact definitions and checklists
- Traceability alone: automated completeness checking for any project using our templates
- Documentation alone: comprehensive V-model education and best practices reference
- Craft skills alone: someone just wants quality requirement-writing or code development skills
- All together: full agentic V-model development with traceability and compliance

## Component Details

### Templates & Schemas

Define what artifacts are needed for V-model compliance, their structure, and their relationships. This is the **definitional** layer — it says what things look like, not how to create them or how to check them.

**Artifact Envelope** — Common fields shared by all artifacts:
```yaml
artifact_id: "SWREQ-042"           # human-readable, type-prefixed
artifact_type: "sw-requirement"     # references an artifact schema
version: "1.0.0"
status: "draft"                     # draft | in_review | approved | baselined | superseded
assurance_level: 3                  # OPTIONAL
body:                               # type-specific content
  ...
```

**Artifact Type Schemas** — YAML definitions for each V-level:

Pre-requirements (project entry point):
- Stakeholder Register, Stakeholder Need, Concept of Operations (ConOps)
- Completeness Analysis Record (generic for FTA/FMEA/STPA/PBR results)
- Allocation Matrix, Requirements Baseline

Left side (development):
- System Requirements, SW Requirements, Architecture, Detailed Design
- Source code is real files, linked via traceability trace files

Right side (verification):
- System Test Cases (YAML artifact — often manual procedures)
- Lower-level tests are real test files (unit, integration, qualification), linked via trace files
- Test results and coverage are tool output (JUnit XML, jacoco), linked via trace files
- Review Records (separate artifact type — also used for PBR multi-perspective reviews)

Plans:
- Development, Verification, CM, QA

**Assurance Level Configuration** — How rigor scales with safety level (DAL, ASIL, etc.)

**Translation Layer** — Generic V-model terms mapped to domain-specific vocabularies (DO-178C, ASPICE, ISO 26262)

### Traceability

Link artifacts together, validate completeness, detect gaps. This is the **relational** layer — it manages the graph between artifacts.

**Coupled to our templates** — the traceability framework validates against our artifact schemas and link type rules.

**Link Schema:**
```yaml
source: "SWREQ-042"
target: "TS-042-01"
link_type: "verified-by"
status: "draft"
```

Links are many-to-many, typed, attributed, and stored separately from artifacts.

**Validation capabilities:**
- Orphan detection (artifact with no required links)
- Coverage analysis (% of requirements with tests, scaled to safety level)
- Staleness detection (content hash changed, links not re-validated)
- Change impact analysis (requirement changed → which designs, code, tests affected?)
- Bidirectional consistency checks

**Phased delivery:**
- **Phase A (current):** Data model + agent skill as temporary engine
- **Phase B (next):** Deterministic CLI tool (Rust/Go) — fast, CI/CD-embeddable
- **Phase C (future):** Web GUI — visual traceability graphs for non-developer stakeholders

### Documentation

The single source of truth for all domain knowledge. Everything else (templates, skills) is derived from or validated against the documentation.

**Per artifact type, documentation follows a 6-section structure (see BACKLOG.md for details):**
1. **What it is** — V-model independent introduction to the artifact type
2. **V-model context** — Where does this artifact sit? What level? What are the inputs/outputs? Standards perspective.
3. **Producing a quality workproduct** — THE BULK. Organized by knowledge domains (not procedures). Best practices, patterns, anti-patterns, quality metrics, concrete examples for every principle. V-model independent, craft-level. Enough for a junior engineer to produce industry-standard output.
4. **V-model specific considerations** — Additional V-model gotchas beyond general quality (traceability, independence, dead code rules)
5. **Framework integration** — Which template to use, how it links in the traceability model, which AI skills are coupled to this artifact type.
6. **AI skills integration** — How the corresponding craft/framework skills relate to this documentation (stub until skills are built).

**Each documentation page requires thorough research before writing** — see "Procedure: Research Before Documentation" in BACKLOG.md. Typical research: 1 standards doc + 3-5 craft best practices docs + 1 AI-specific doc.

**Scope:** Not a summary — equivalent in depth to the union of major V-model standards (DO-178C, ISO 26262, ASPICE, IEC 62304, EN 50128), expressed in generic V-model terms. Covers the full cascade from stakeholder identification through ConOps and completeness analysis to system requirements, SW requirements, architecture, detailed design, and code. This is a major content effort but is non-negotiable: the documentation proves the framework is sound, and AI skills are derived from it.

**Long-term goal:** Could serve as a standalone "generic V-model engineering" reference — not just safety compliance, but structured engineering at any rigor level.

**Documentation-to-skill pipeline:**
```
Documentation (human-readable)          AI Skill (AI-optimized)
┌─────────────────────────────┐        ┌──────────────────────────┐
│ V-model context             │        │ Same knowledge, distilled│
│ Best practices              │───────>│ Step-by-step procedure   │
│ Anti-patterns               │        │ Quality checklist        │
│ Examples                    │        │ Anti-pattern guards      │
│ Framework integration       │        │ Output template          │
└─────────────────────────────┘        └──────────────────────────┘
```

### AI Skills

> **Detailed design document:** [`docs/guide/skills-architecture.html`](../guide/skills-architecture.html) — the authoritative design for all skills, agents, orchestration, contracts, and configuration. The summary below is kept in sync; the HTML page is the source of truth.

Three-layer architecture, organized by concern:

**Layer 1 — Skills (atomic knowledge units):**
- *Craft skills* — standalone best practices, framework-independent, derived from documentation. Each skill teaches one thing well. Independently usable on any project. Follows agentskills.io open standard (SKILL.md format). Must work on cheapest viable model tier — tested with baseline comparison.
- *Framework skills* — thin VModelWorkflow adapters. Template formats, traceability link creation, tool check execution. No craft knowledge — only schema and format information. If templates/schemas change, only these update.

**Layer 2 — Agents (specialized execution environments):**
- Subagents with isolated context windows, scoped tool access, and structured input/output contracts
- Compose Layer 1 skills into roles: TDD developer, code reviewer, DD developer, DD reviewer, task decomposer
- Fresh context per task — no pollution from prior tasks
- Producer agents (developers) have write access; reviewer agents are read-only

**Layer 3 — Orchestration (pipeline control):**
- Research/plan runs in its own session (context-intensive human discussion produces a clean artifact)
- Task decomposer runs as subagent (reads plan, produces scoped task contracts)
- Pipeline controller is a lean skill that reads state files and spawns agents
- Human gates: after task decomposition (approve scope) and after all tasks (final review)

**Key architectural decision — document-based handoffs:**
Every boundary between pipeline components is a file on disk (YAML). No direct agent-to-agent communication. Any session can crash, run out of context, or need human intervention — and restart from the last written artifact.

**Naming convention:** `vmodel-skill-*` for skills, `vmodel-agent-*` for agents.

**Model tier classification (vendor-neutral):**
- Tier 1 (Reasoning): planning, decomposition, safety-critical review
- Tier 2 (Workhorse): implementation, standard review, pipeline control
- Tier 3 (Fast): craft skills standalone, file finding, simple transformations

**SOLID for skills:** Single responsibility per skill. Shared knowledge factored into `references/*.md` files, duplicated into each skill directory for self-containment per agentskills.io spec.

## Human-Agent Interaction Model

Human drives at the strategic level, AI executes at the tactical level, human verifies at the quality gate.

**Per V-model layer (top to bottom):**

```
HUMAN-DRIVEN                      AGENT-ORCHESTRATED                HUMAN-DRIVEN
┌───────────────────┐            ┌───────────────────────┐         ┌──────────────┐
│ Research & Plan   │            │ Implementation Loop   │         │ Final Review │
│                   │            │                       │         │              │
│ Human provides    │──agreed──>│ Agent writes artifact  │──done──>│ Human reviews│
│ input & context   │  plan     │ Agent self-checks      │         │ and approves │
│ AI gathers context│            │ Agent sends to review  │         │ or rejects   │
│ Impact analysis   │            │ Review feedback loop   │         │              │
│ Back-and-forth    │            │ Traceability updated   │         │              │
│ discussion        │            │                       │         │              │
└───────────────────┘            └───────────────────────┘         └──────────────┘
                                                                          │
                                                                    human transitions
                                                                    to next V-model
                                                                    layer ↓
```

**Key principle:** This is NOT an autonomous pipeline with human checkpoints. It is a human-orchestrated workflow where AI handles implementation within each layer. The human transitions between layers, participates in research/planning, and does final review.

**Agent autonomy varies by V-level (refined 2026-04-12):**

The interaction model above is the general pattern, but the balance of effort shifts across the V. Eval data from lower V skills shows agents already perform well at code/test level given clear design input (+3% to +13% over baseline). The real value is at the upper V, where ambiguity, trade-offs, and stakeholder intent live.

| V-Level | Agent Role | Human Role | Agent Autonomy |
|---|---|---|---|
| Stakeholder Analysis | Suggests categories, elicitation questions, gap checks | Drives all content (only humans know real stakeholders) | Low |
| ConOps | Structures scenarios, checks mode completeness | Drives operational vision | Low |
| Completeness Analysis | Brainstorms failure modes (strong suit), guides methods | Owns judgment (what's a real hazard vs. noise) | Low-Medium |
| System Requirements | Consistency checker, allocation assistant, scribe | Drives content, captures stakeholder intent | Low-Medium |
| SW Requirements | Structures, checks completeness, suggests patterns | Drives content, validates against system intent | Medium |
| SW Architecture | Proposes decomposition, checks consistency | Decides trade-offs (system-wide consequences) | Medium |
| Detailed Design | Heavy lifting on specification | Validates trade-offs, approves | Medium-High |
| Code + Unit Tests | Autonomous executor (given good design) | Reviews output | High |

**Implication for skill design:** Upper V skills must be designed as interactive advisors (structure human thinking, check consistency, suggest completions), not autonomous producers. Lower V skills can be autonomous executors.

**Research/Plan phase produces a handoff artifact** — a structured "implementation contract" that scopes what the agent-orchestrated loop does. This captures design decisions and rationale from the discussion.

## Use Cases

### Greenfield V-Model Development (top-down)

Start from system requirements, work down through the V-model layers. Each layer follows the research/plan → implement → review cycle. Traceability is built as artifacts are created.

### Legacy Retrofit (bottom-up, primary market entry)

Start from existing code, reverse-engineer V-model artifacts upward. Specialized analysis skills examine code and infer implicit requirements, designs, and test coverage gaps. Produces improvement suggestions based on best practices.

This is the **primary selling point** — the volume of legacy codebases needing V-model compliance far exceeds greenfield development. The unique value is the analysis/inference skills combined with the standard framework components.

**Legacy RE is a use case, not a separate component.** It combines:
- Analysis/inference craft skills (understand existing code)
- Templates (produce artifacts in V-model format)
- Traceability (link discovered artifacts)
- Best practices documentation (suggest improvements)

## Per-Layer Component Set

For each V-model layer, we develop (see [skills-architecture.html](../guide/skills-architecture.html) for full details):

| Component | Layer | Purpose |
|-----------|-------|---------|
| **vmodel-skill-develop-**** | L1 Craft | Best practices for producing this artifact type |
| **vmodel-skill-review-**** | L1 Craft | Best practices for reviewing this artifact type |
| **vmodel-skill-*-template** | L1 Framework | Schema format and ID conventions for this layer's template |
| **vmodel-skill-traceability** | L1 Framework | Trace link creation (shared across all layers) |
| **vmodel-skill-tool-checks** | L1 Framework | Config-driven automated checks (shared across all layers) |
| **vmodel-agent-*-developer** | L2 Agent | Producer role: composes craft + framework skills, runs TDD/design |
| **vmodel-agent-*-reviewer** | L2 Agent | Reviewer role: read-only, produces verdict (APPROVED/REJECTED/DESIGN_ISSUE) |
| **vmodel-agent-task-decomposer** | L2 Agent | Breaks implementation plan into agent-sized task contracts |
| **Research/Plan** | L3 Orchestration | Own session, interactive with human, produces implementation-plan.yaml |
| **Pipeline Controller** | L3 Orchestration | Lean skill, reads state files, spawns agents, manages feedback loops |

## Key Design Decisions

### 1. Documentation is the Foundation
All knowledge lives in documentation first. Templates are derived from it. Skills are distilled from it. This is non-negotiable — if we can't explain what we're doing in documentation, we can't prove the framework works.

### 2. Components are Independent (SOLID)
No forced dependencies. Adopt incrementally. But designed to work together for maximum value.

### 3. Traceability is Coupled to Templates
Decision: the traceability framework validates against our artifact schemas. Not a generic graph-linking tool.

### 4. Individual Orchestration Per Layer
Each V-model layer gets its own implementation loop. Optimized for the specific concerns of that layer. Refactor to shared patterns only after building 3-4 layers and seeing what's common.

### 5. YAML for All Machine-Readable Data
Human-readable, git-diffable, easy for both humans and agents to produce.

### 6. Artifacts Live Alongside Code
In the target repository, version-controlled, diffable, PR-reviewable.

### 7. Deterministic Where Possible
Traceability validation is a tool concern, not an agent concern. Agents create, tools verify.

### 8. Generic V-Model + Translation
Internal terms are generic. Domain-specific vocabulary applied via translation layer.

### 9. Top-Down Documentation, Then Skills
Documentation follows the V-model top-down: stakeholder needs → ConOps → completeness analysis → system requirements → SW requirements → architecture → detailed design → code. This reveals the complete information cascade, including both downward handoffs and upward feedback loops. Skills are built after documentation coverage is complete, informed by the full picture. (Note: initial lower-V documentation was built bottom-up; direction changed 2026-04-12 to prioritize understanding the complete flow. Scope expanded 2026-04-13 to include stakeholder analysis, ConOps, and completeness analysis as the true project entry point.)

### 10. V-Model as Engineering Infrastructure, Not Just Safety
The framework generalizes V-model techniques beyond safety compliance. Safety is the highest-rigor application, but structured requirements, completeness analysis, traceability, and formal verification have value at any rigor level. Completeness analysis (FTA, FMEA, STPA, PBR) is framed as "techniques for discovering what's missing" — applicable to any quality goal, not just hazard mitigation. The assurance level configuration scales rigor: safety-critical projects get full analysis, non-safety projects benefit from lightweight versions of the same techniques.

### 11. Target User: Mid-Senior Engineers with AI
Not autonomous AI. Not manual-only humans. Engineers who orchestrate AI agents and verify output. Human at strategic level, AI at tactical level, human at quality gate.

## Quality Attributes

| Attribute | Approach |
|---|---|
| **Independence** | Each component usable standalone; no forced AI dependency |
| **Portability** | Language-agnostic schemas; language-specific adapters |
| **Extensibility** | New skills, translations, artifact types without changing existing |
| **Reliability** | Deterministic tools for validation; agents for creative work |
| **Usability** | Works for human-only, agent-only, or mixed teams |
| **Maintainability** | Documentation is source of truth; skills derived from it |
| **Testability** | Skills evaluated via skill-creator; engine has deterministic expected outputs |
| **Composability** | Small craft skills, independently usable; orchestration per layer |
