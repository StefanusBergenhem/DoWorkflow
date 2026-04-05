# Target Architecture

## Vision

A framework for V-model compliant software development that combines documentation, templates, traceability, and AI skills into a coherent system. Each component is independently usable but designed to work together. The end goal: V-model compliant software, developed by AI agents, fully understood and verified by human engineers.

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

Left side (development):
- System Requirements, SW Requirements, Architecture, Detailed Design
- Source code is real files, linked via traceability trace files

Right side (verification):
- System Test Cases (YAML artifact — often manual procedures)
- Lower-level tests are real test files (unit, integration, qualification), linked via trace files
- Test results and coverage are tool output (JUnit XML, jacoco), linked via trace files
- Review Records (separate artifact type)

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

**Per artifact type / V-model activity, documentation covers:**
1. **V-model context** — Where does this artifact sit? What level? What are the inputs/outputs? Why does it exist?
2. **Best practices** — How to produce a high-quality artifact. Detailed, exhaustive guidance. Enough for a junior engineer to produce industry-standard output.
3. **Anti-patterns** — What to avoid, with examples of common mistakes.
4. **Examples** — Concrete illustrations of good and bad artifacts.
5. **Framework integration** — Which template to use, how it links in the traceability model, which AI skills are coupled to this artifact type.

**Scope:** Not a summary — equivalent in depth to the union of major V-model standards (DO-178C, ISO 26262, ASPICE, IEC 62304, EN 50128), expressed in generic V-model terms. This is a major content effort but is non-negotiable: the documentation proves the framework is sound, and AI skills are derived from it.

**Long-term goal:** Could serve as a standalone "generic V-model standard" reference.

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

Two distinct categories of skills, plus agentic engineering practices baked into all skills:

**Craft Skills (standalone, framework-independent):**
- AI-optimized versions of the documentation best practices
- Each skill teaches how to do one thing well (write requirements, derive tests, review code)
- No knowledge of our templates, schemas, or traceability
- Independently usable on any project, in any framework
- Follows agentskills.io open standard (SKILL.md format)
- Must work on smaller/cheaper models (Haiku, Sonnet) — tested with baseline comparison

**Framework Skills (VModelWorkflow-specific):**
- How to use our templates, produce trace artifacts, follow our schemas
- Per-layer orchestration: research/plan → implement → review loops
- Human transition between V-model layers, agent orchestration within layers
- Thin bridge: if templates/schemas change, only framework skills update

**Agentic Engineering Practices (cross-cutting, baked into skill design):**
- Context management (small, focused context windows)
- Task splitting (break large work into scoped pieces)
- Handoff discipline (structured contracts between phases)
- These are design principles of the skills, not a separate skill category

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

## Per-Layer Skill Set

For each V-model layer, we develop:

| Skill | Type | Purpose |
|-------|------|---------|
| **Research/Plan** | Framework, interactive | Context gathering, impact analysis, back-and-forth with human, produces implementation contract |
| **Implement** | Framework, orchestrated | Agent writes artifact following template + craft skill, self-checks against checklist |
| **Review** | Framework, orchestrated | Review agent validates against best practices + template + traceability |
| **Craft: Write** | Standalone | Best practices for producing this artifact type (derived from documentation) |
| **Craft: Review** | Standalone | Best practices for reviewing this artifact type (derived from documentation) |

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

### 9. Bottom-Up Build Order
Start from code + unit tests (lowest V-level), work upward through detailed design, architecture, requirements. Prove each layer end-to-end before moving to the next.

### 10. Target User: Mid-Senior Engineers with AI
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
