# Target Architecture

## Vision

A framework for V-model compliant software development, built as three independent pillars that can be used standalone or combined. Supports human-only, agent-only, or mixed development teams.

## Three Pillars

```
PILLAR 1                        PILLAR 2                       PILLAR 3
V-Model Compliance              Traceability                   Agentic Skills

┌────────────────────┐         ┌────────────────────┐         ┌────────────────────┐
│                    │         │                    │         │                    │
│ Artifact schemas   │         │ Link data model    │         │ Craft skills       │
│ Templates          │         │ Link type defs     │         │ (standalone,       │
│ Checklists         │         │ Validation rules   │         │  composable)       │
│ Assurance config   │         │                    │         │                    │
│ Translation docs   │         │ Phase A (now):     │         │ Orchestration      │
│                    │         │   Data model +     │         │ (framework         │
│ Scaffold tool:     │         │   agent skill as   │         │  interaction)      │
│ Create blank       │         │   temp engine      │         │                    │
│ artifacts from     │         │                    │         │                    │
│ templates          │         │ Phase B (later):   │         │                    │
│                    │         │   Deterministic    │         │                    │
│                    │         │   CLI tool (fast,  │         │                    │
│                    │         │   portable, CI)    │         │                    │
│                    │         │                    │         │                    │
│                    │         │ Phase C (future):  │         │                    │
│                    │         │   Web GUI for      │         │                    │
│                    │         │   non-developers   │         │                    │
│                    │         │                    │         │                    │
│ Usable by:         │         │ Usable by:         │         │ Usable by:         │
│ humans, agents,    │         │ humans, CI/CD,     │         │ AI agents          │
│ or both            │         │ agents, anyone     │         │                    │
└────────────────────┘         └────────────────────┘         └────────────────────┘
      independent                   independent                   independent
```

**No pillar depends on the others to function.**

- Pillar 1 alone: human team gets V-model templates and checklists
- Pillar 1 + 2: human team gets templates + automated completeness checking
- Pillar 1 + 2 + 3: full agentic V-model development
- Pillar 3 craft skills alone: someone just wants a good requirement-writing or review prompt

## Pillar 1: V-Model Compliance

### Purpose

Define what artifacts are needed for V-model compliance, their structure, and their relationships. This is the **definitional** layer — it says what things look like, not how to create them or how to check them.

### Components

**Artifact Schemas** — YAML definitions for each V-level artifact type:
- System Requirements, SW Requirements, Architecture, Detailed Design
- Test Specifications, Test Results, Coverage Reports
- Review Records (separate artifact type, linked via traceability)
- Plans (development, verification, CM, QA)
- Source Code metadata

Each schema defines: required fields, optional fields, field types. Schemas do NOT enforce specific content patterns (e.g., EARS is not required by the schema).

**Assurance Level Configuration** — How rigor scales:
- Optional property on artifacts (not universal)
- Defines coverage targets, verification methods, documentation formality per level
- Default behavior is high-rigor (benefits agent output quality)
- Maps to DAL (aviation), ASIL (automotive), or quality levels (ASPICE)

**Translation Layer** — Domain-specific vocabulary:
- Generic V-model terms -> DO-178C terms
- Generic V-model terms -> ASPICE terms
- Applied to reports and exports, not to internal data

**Scaffold Tool** (concept, CLI for later):
- Creates blank artifacts from templates
- Humans can populate artifacts without any agent involvement
- Could be same tool as Pillar 2 engine, or separate lightweight CLI

### Artifact Envelope

Every artifact shares a common envelope. Type-specific content goes in the body:

```yaml
artifact_id: "SWREQ-042"           # human-readable, type-prefixed
artifact_type: "sw-requirement"     # references an artifact schema
version: "1.0.0"
status: "draft"                     # draft | in_review | approved | baselined | superseded
assurance_level: 3                  # OPTIONAL — not all artifact types need this

body:                               # type-specific content
  title: "Fuel rate limiting"
  statement: "..."                  # free text, any format
  rationale: "..."
  verification_method: "test"
```

Fields explicitly NOT in the envelope:
- `created_by` — irrelevant to compliance assessment
- `reviewed_by` — insufficient; review records are separate artifacts
- `trace_up` / `trace_down` — traceability is Pillar 2's concern

### Artifact ID Strategy

Two-tier identification:
- **Human-facing:** `TYPE-nnn` (e.g., `SWREQ-042`) — readable, stable
- **Machine-facing:** content hash appended for change detection (e.g., `SWREQ-042@a3f7c2`)

The hash changes when content changes, enabling automatic staleness detection.

## Pillar 2: Traceability

### Purpose

Link artifacts together, validate completeness, detect gaps. This is the **relational** layer — it manages the graph between artifacts.

### Data Model (Phase A — now)

**Link Schema** — defines how artifacts connect:

```yaml
source: "SWREQ-042"
target: "TS-042-01"
link_type: "verified-by"
status: "draft"
```

Links are:
- **Many-to-many** — a requirement can trace to multiple tests, designs, review records
- **Typed** — each link has a defined type (verified-by, allocated-to, reviewed-in, etc.)
- **Attributed** — links carry their own metadata (status, etc.)
- **Stored separately** — not embedded in artifacts

**Link Type Definitions** — what link types exist and their rules:
- Which artifact types can be source/target for each link type
- Which links are required for completeness at each assurance level
- Bidirectional validation rules

**Validation Rules** — completeness and consistency checks:
- Orphan detection (artifact with no required links)
- Coverage analysis (% of requirements with tests)
- Staleness detection (content hash changed, links not re-validated)
- Bidirectional consistency

### Temporary Agent Skill (Phase A — now)

An agent craft skill that reads YAML artifact and trace files and performs validation. Serves as stand-in until the deterministic engine is built. Good enough to get started and validate the data model.

### Deterministic Engine (Phase B — later)

A CLI tool:
- Written in a fast, portable language
- Reads YAML artifacts and trace files
- Validates completeness, detects gaps, produces reports
- Runs in CI/CD pipelines
- No AI dependency — purely deterministic

### Web GUI (Phase C — future)

For non-developer stakeholders:
- View and edit documentation artifacts
- Input flows into the repo via defined process

## Pillar 3: Agentic Skills

### Purpose

Prompt packages that guide AI agents to produce quality V-model artifacts. Two sub-layers with SOLID separation.

### Craft Skills (standalone, composable)

Each craft skill:
- Has **one responsibility** (write a requirement, review a design, characterize code)
- Defines **typed input/output contracts** referencing Pillar 1 artifact schemas
- Has **no workflow knowledge** (doesn't know what comes before or after)
- Has **model-tier variants** (frontier, mid-tier, small model prompts)
- Has **deterministic validation** rules on its output
- Has **examples** (test cases + few-shot material)
- Is **independently usable** — someone can use just this skill outside the framework

Skill categories:

| Category | Examples |
|---|---|
| Requirements | Requirement writing (uses EARS), requirement review, decomposition |
| Design | Architecture design, detailed design, design review |
| Verification | Test writing, test review, coverage analysis |
| Implementation | Implement from tests, refactor, code review |
| Analysis | Code structure analysis, behavior characterization, change impact |
| Traceability | Trace link creation, trace validation (temp engine) |

EARS is a preference of our craft skills, not a framework requirement. The requirement-writing skill uses EARS syntax and validates against EARS patterns. Other users could write their own requirement-writing skill using a different approach.

### Orchestration (framework interaction)

Composes craft skills into workflows:
- **Sequencing** — which skill runs in what order
- **Handoffs** — routing outputs to inputs via contracts
- **Gates** — human approval points
- **Retry logic** — escalation on failure
- **Progress tracking** — status across work items

Orchestration pipelines:
- DRTDD pipeline (new development or retrofit)
- Scan pipeline (legacy codebase analysis)
- Report pipeline (compliance evidence assembly)

## Key Design Decisions

### 1. Three Independent Pillars

No cross-pillar dependencies where not strictly needed. A craft skill references Pillar 1 schemas via `schema_ref` for its contracts, but the schemas don't know about skills. The traceability engine reads artifacts defined by Pillar 1 schemas, but the schemas don't know about the engine.

### 2. YAML for All Machine-Readable Data

Human-readable, git-diffable, easy for both humans and agents to produce.

### 3. Artifacts Live Alongside Code

In the target repository, version-controlled, diffable, PR-reviewable.

### 4. Deterministic Where Possible

Traceability validation is a tool concern, not an agent concern. Coverage checking is deterministic. Schema validation is deterministic. Agents create, tools verify.

### 5. Incremental by Design

Everything works on subsets: module-by-module, requirement-by-requirement.

### 6. Generic V-Model + Translation

Internal terms are generic. Domain-specific vocabulary applied via translation layer for reports and exports.

## Quality Attributes

| Attribute | Approach |
|---|---|
| **Independence** | Each pillar usable standalone; no forced AI dependency |
| **Portability** | Language-agnostic schemas; language-specific adapters |
| **Extensibility** | New skills, translations, artifact types without changing existing |
| **Reliability** | Deterministic tools for validation; agents for creative work |
| **Usability** | Works for human-only, agent-only, or mixed teams |
| **Maintainability** | YAML schemas are self-documenting; skills are self-contained |
| **Testability** | Skills have example I/O; engine has deterministic expected outputs |
| **Composability** | Small craft skills recombined into workflows via orchestration |
