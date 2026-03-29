# Backlog

Organized by pillar. Items within a pillar can often be parallelized.
Cross-pillar dependencies are noted where they exist.

---

## Pillar 1: V-Model Compliance

Schemas, templates, checklists. Usable by humans, agents, or both.

### 1.1 Common Artifact Envelope

- [x] Define common fields shared by all artifacts (artifact_id, artifact_type, version, status)
- [x] Define artifact_id format (TYPE-nnn) and content hash strategy for change detection
- [x] Define status lifecycle (draft -> in_review -> approved -> baselined -> superseded)
- [x] Define assurance_level as optional field with rationale for when it applies
- [x] Define body as type-specific extension point

> Done: `schemas/artifacts/artifact-envelope.schema.yaml`

### 1.2 Artifact Type Schemas

Define YAML schemas for each V-model artifact type (the `body` content).

**Left side (development):**
- [ ] System Requirements schema
- [x] SW Requirements schema
- [x] SW Architecture schema (includes tool-generated Mermaid diagram)
- [x] Detailed Design schema (includes optional `realization` section)
- [ ] ~~Source Code metadata schema~~ — replaced by pragmatic approach: source code is real files, linked via Pillar 2 trace files

**Right side (verification):**
- [ ] ~~Test Specification schema~~ — replaced by pragmatic approach: tests are real test files (unit, integration, qualification), linked via Pillar 2 trace files
- [ ] ~~Test Results schema~~ — replaced by pragmatic approach: test results are tool output (JUnit XML, gtest XML), linked via Pillar 2 trace files with `format` field
- [ ] ~~Coverage Report schema~~ — same as above (jacoco, gcov, lcov), linked via trace files
- [ ] Review Record schema (checklist, findings, verdict, reviewer qualification)

**Plans:**
- [ ] Development Plan schema
- [ ] Verification Plan schema
- [ ] Configuration Management Plan schema
- [ ] Quality Assurance Plan schema

### 1.3 Assurance Level Configuration

- [ ] Define generic assurance level scale (1-5, mapping to DAL A-E / ASIL D-QM)
- [ ] Define rigor parameters per level (coverage targets, verification methods, formality, review independence)
- [ ] Define default configuration (high-rigor)
- [ ] Define override mechanism (project can customize)

### 1.4 Translation Layer

- [ ] Define translation schema (generic term -> domain term)
- [ ] Create DO-178C translation
- [ ] Create ASPICE/ISO 26262 translation
- [ ] Create IEC 62304 translation (stretch goal)

### 1.5 Scaffold Tool (concept)

- [ ] Define template format for blank artifacts
- [ ] Define CLI interface concept (e.g., `vmodel new sw-requirement`)
- [ ] Define auto-ID generation strategy
- [ ] Note: actual implementation deferred to Phase B alongside traceability engine

---

## Pillar 2: Traceability

### 2A: Data Model (now)

- [x] Define trace schema (source, source_hash, links with target + target_hash)
- [x] Define link type catalog (verified-by, allocated-to, derived-from, implemented-by, results, coverage)
- [ ] Define link type rules (which artifact types can be source/target per link type)
- [ ] Define completeness rules (which links are required per artifact type per assurance level)
- [ ] Define orphan detection rules
- [ ] Define coverage metrics definitions
- [x] Define staleness detection (content hash comparison — tool computes current hash vs stored hash)
- [ ] Define trace matrix output format

### 2A-temp: Agent Skill as Temporary Engine

- [ ] Create trace-validation craft skill (reads YAML files, validates links, reports gaps)
- [ ] This is explicitly temporary — replaced by deterministic tool in Phase 2B

### 2B: Deterministic Engine (later, own project)

- [ ] Choose language (fast, portable — Rust, Go, or similar)
- [ ] Implement CLI that reads YAML artifacts + trace files
- [ ] Implement validation rules from 2A data model
- [ ] Implement gap report generation
- [ ] Implement coverage metrics calculation
- [ ] CI/CD integration (exit codes, machine-readable output)
- [ ] Scaffold tool integration (create blank artifacts from Pillar 1 templates)

### 2C: Web GUI (future)

- [ ] Web interface for non-developer documentation interaction
- [ ] Input mechanism to get changes into the repo
- [ ] Deferred — design when overall framework is stable

---

## Pillar 3: Agentic Skills

### 3.0 Skill Contract Framework

- [x] Define craft skill contract schema
- [x] Define orchestration pipeline contract schema

> Done: `schemas/core/craft-skill.schema.yaml`, `schemas/core/orchestration-pipeline.schema.yaml`

### 3.1 Craft Skills — Analysis

Single-responsibility skills for analyzing existing code. Each independently usable.

- [ ] Code Structure Analysis skill
- [ ] Test Coverage Analysis skill
- [ ] Behavior Characterization skill
- [ ] Requirement Candidate Generation skill
- [ ] Change Impact Analysis skill

### 3.2 Craft Skills — Requirements

- [ ] Requirement Writing skill (uses EARS as its approach)
- [ ] Requirement Review skill
- [ ] Requirement Decomposition skill

### 3.3 Craft Skills — Design

- [ ] Architecture Design skill
- [ ] Detailed Design skill
- [ ] Design Review skill

### 3.4 Craft Skills — Verification

- [ ] Test Writing skill
- [ ] Test Review skill
- [ ] Coverage Analysis skill (interpret tool output, not generate numbers)

### 3.5 Craft Skills — Implementation

- [ ] Implementation skill (from failing tests + design)
- [ ] Refactor skill
- [ ] Code Review skill

### 3.6 Craft Skills — Traceability

- [ ] Trace Link Creation skill
- [ ] Trace Validation skill (temporary engine — see 2A-temp)

### 3.7 Orchestration Pipelines

- [ ] DRTDD pipeline (phase sequencing, handoffs, gates, retry)
- [ ] Scan pipeline (module-by-module analysis, gap report assembly)
- [ ] Report pipeline (compliance evidence aggregation)
- [ ] Batch orchestration (mature usage, parallel work items)

---

## Phase Sequencing

### Now (Phase 1)

```
Pillar 1 (all)  +  Pillar 2A (data model)  +  Pillar 3 (skills)
                   Pillar 2A-temp (agent skill as temp engine)
```

Within Phase 1, suggested order:
1. Pillar 1.1 (envelope) — everything references this
2. Pillar 1.2 (artifact schemas) + Pillar 2A (link model) — can parallel
3. Pillar 1.3 + 1.4 (assurance levels, translations) — builds on schemas
4. Pillar 3.1-3.6 (craft skills) — builds on schemas
5. Pillar 3.7 (orchestration) — composes craft skills

### Later (Phase 2)

```
Pillar 2B (deterministic engine + scaffold tool)
```

### Future (Phase 3)

```
Pillar 2C (web GUI)
Integration pilot on legacy Java codebase
```

---

## Guiding Principles

1. **Pillars are independent.** No cross-pillar dependencies where not strictly needed.
2. **Deterministic where possible.** Validation is a tool concern, not an agent concern.
3. **EARS is a skill preference, not a framework requirement.** Schemas accept any requirement syntax.
4. **Discuss before writing.** Explain approach, show visually, motivate, get approval.
5. **Schema before skill.** Define what an artifact looks like before writing prompts that produce it.
6. **Validate everything.** AI output gets deterministic validation.
7. **Human gates are mandatory.** No artifact enters baseline without human approval.
8. **Incremental always.** Module-by-module. Requirement-by-requirement.
9. **Model-tier aware.** Skills have frontier, mid-tier, and small-model variants.
10. **Composability over monoliths.** Small skills, recombined via orchestration.
