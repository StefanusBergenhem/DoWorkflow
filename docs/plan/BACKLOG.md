# Backlog

Organized by component. Build order is bottom-up: start from the lowest V-model layer (code + unit tests) and work upward through each layer, completing documentation → template → craft skill → framework skill for each layer before moving to the next.

---

## Open Questions

Questions identified during architecture refinement (2026-04-05) that need resolution as we encounter them:

1. **Handoff artifact format:** The research/plan phase produces an "implementation contract" for the agent-orchestrated loop. What fields does this contract contain? Similar to current_task.yaml but richer — needs design decisions, rationale, scope boundaries from the discussion.

2. **Code-level research/plan:** Should the lowest level (code implementation) have a lightweight research/plan step for implementation strategy (e.g., "token bucket vs sliding window"), or is the detailed design sufficient input? Current position: probably a brief "read design, confirm approach" step within develop-code, not a separate skill.

3. **Legacy RE skill boundaries:** Legacy reverse-engineering is a use case combining analysis craft skills + templates + traceability. What specific analysis skills are needed? (Code structure analysis, behavior characterization, requirement inference, gap detection.) How do they differ from forward-engineering craft skills?

4. **Orchestration commonality:** Each V-model layer gets its own implementation loop initially. After building 3-4 layers, what patterns emerge? When do we refactor to shared orchestration?

5. **Documentation format:** The exhaustive per-artifact documentation — is it markdown files in docs/? Integrated into the HTML guide? Both? Current thinking: markdown source files that feed into the HTML guide.

6. **Test environment argumentation:** Host/target-like/target test environment selection and justification. Deferred until system test work begins.

---

## Component 1: Templates & Schemas

Artifact definitions, envelopes, checklists. Usable by humans, agents, or both.

### 1.1 Common Artifact Envelope

- [x] Define common fields (artifact_id, artifact_type, version, status)
- [x] Define artifact_id format (TYPE-nnn) and content hash strategy
- [x] Define status lifecycle (draft → in_review → approved → baselined → superseded)
- [x] Define assurance_level as optional field
- [x] Define body as type-specific extension point

> Done: `schemas/artifacts/artifact-envelope.schema.yaml`

### 1.2 Artifact Type Schemas

**Left side (development):**
- [x] System Requirements schema
- [x] SW Requirements schema
- [x] SW Architecture schema
- [x] Detailed Design schema

**Right side (verification):**
- [x] System Test Case schema
- [ ] Review Record schema (checklist, findings, verdict, reviewer qualification)

**Plans:**
- [ ] Development Plan schema
- [ ] Verification Plan schema
- [ ] Configuration Management Plan schema
- [ ] Quality Assurance Plan schema

### 1.3 Assurance Level Configuration

- [ ] Define generic assurance level scale (1-5, mapping to DAL A-E / ASIL D-QM)
- [ ] Define rigor parameters per level
- [ ] Define default configuration (high-rigor)
- [ ] Define override mechanism

### 1.4 Translation Layer

- [ ] Define translation schema (generic term → domain term)
- [ ] Create DO-178C translation
- [ ] Create ASPICE/ISO 26262 translation
- [ ] Create IEC 62304 translation (stretch goal)

### 1.5 Scaffold Tool (concept, deferred to Phase B)

- [ ] Define template format for blank artifacts
- [ ] Define CLI interface concept
- [ ] Define auto-ID generation strategy

---

## Component 2: Traceability

Coupled to our templates. Link model, validation rules, coverage analysis, impact analysis.

### 2A: Data Model (current)

- [x] Define trace schema (source, source_hash, links with target + target_hash)
- [x] Define link type catalog (verified-by, allocated-to, derived-from, implemented-by, results, coverage)
- [ ] Define link type rules (which artifact types can be source/target)
- [ ] Define completeness rules (which links required per artifact type per assurance level)
- [ ] Define orphan detection rules
- [ ] Define coverage metrics definitions
- [x] Define staleness detection (content hash comparison)
- [ ] Define trace matrix output format

### 2A-temp: Agent Skill as Temporary Engine

- [ ] Create trace-validation craft skill (reads YAML files, validates links, reports gaps)
- [ ] Explicitly temporary — replaced by deterministic tool in Phase 2B

### 2B: Deterministic Engine (later, own project)

- [ ] Choose language (Rust/Go)
- [ ] Implement CLI: read YAML artifacts + trace files, validate, report
- [ ] CI/CD integration (exit codes, machine-readable output)
- [ ] Scaffold tool integration

### 2C: Web GUI (future)

- [ ] Web interface for non-developer documentation interaction
- [ ] Deferred until framework is stable

---

## Component 3: Documentation

Single source of truth. AI skills are derived from this. Per-artifact documentation must be complete before the corresponding skill is developed.

### Documentation Structure Per Artifact Type

Each artifact type gets a complete documentation page covering:
1. V-model context (what, where, why, inputs/outputs)
2. Best practices (how to produce a high-quality artifact)
3. Anti-patterns (common mistakes with examples)
4. Examples (good and bad)
5. Framework integration (template reference, traceability links, related AI skills)

### 3.1 Code Implementation Documentation (current — lowest V-level)

- [x] Clean code best practices (`docs/guide/best-practices/implementation/`)
- [ ] V-model context for code implementation (where code sits in V-model, what "implements" means, relationship to detailed design)
- [ ] Anti-patterns for code implementation (beyond clean code — V-model specific: untraceable code, scope creep beyond design, missing error handling specified in design)
- [ ] Examples of good and bad implementation relative to detailed design input
- [ ] Framework integration section (how code links to detailed design via traceability, how test results link back)

### 3.2 Unit Test Documentation (current — lowest V-level)

- [ ] V-model context for unit testing (verification of detailed design, what "verifies" means at this level)
- [ ] Best practices for unit test writing (test derivation strategies: requirement-based, equivalence partitioning, boundary value analysis, fault injection)
- [ ] Anti-patterns for unit testing (code-based testing instead of requirement-based, testing implementation details, missing coverage criteria)
- [ ] Examples of good and bad unit tests relative to detailed design input
- [ ] Framework integration section (how tests trace to detailed design, coverage criteria per assurance level)

### 3.3 Detailed Design Documentation (next — one layer up)

- [ ] V-model context (what detailed design is, level of detail expected, relationship to architecture above and code below)
- [ ] Best practices (algorithms, data structures, interfaces, error handling, timing — per standards requirements)
- [ ] Anti-patterns (design-after-code, paraphrasing code, missing "why", insufficient detail for direct implementation)
- [ ] Examples
- [ ] Framework integration

### 3.4 SW Architecture Documentation (later)

- [ ] V-model context
- [ ] Best practices (component decomposition, interface design, modularity, encapsulation)
- [ ] Anti-patterns
- [ ] Examples
- [ ] Framework integration

### 3.5 SW Requirements Documentation (later)

- [ ] V-model context
- [ ] Best practices (EARS syntax, testability, unambiguity, completeness)
- [ ] Anti-patterns
- [ ] Examples
- [ ] Framework integration

### 3.6 System Requirements Documentation (later)

- [ ] V-model context
- [ ] Best practices
- [ ] Anti-patterns
- [ ] Examples
- [ ] Framework integration

### 3.7 System Test Documentation (later)

- [ ] V-model context
- [ ] Best practices
- [ ] Anti-patterns
- [ ] Examples
- [ ] Framework integration

### 3.8 Review Documentation (later)

- [ ] V-model context for reviews (independent review requirements, what reviews verify)
- [ ] Best practices per artifact type being reviewed
- [ ] Anti-patterns
- [ ] Examples

### 3.9 General Documentation

- [ ] V-model overview (what the V-model is, philosophy, layers, how they connect)
- [ ] DRTDD explanation (Design-Requirement-Test Driven Development)
- [ ] Safety analysis basics (FMEA, FTA — how they feed into requirements)
- [ ] Framework user manual (how to use DoWorkflow)

---

## Component 4: AI Skills

Two categories: craft skills (standalone, derived from documentation) and framework skills (DoWorkflow-specific).

### Skill Foundation

- [x] Define craft skill contract schema (design-time reference)
- [x] Define orchestration pipeline contract schema (design-time reference)
- [ ] Reconcile `schemas/core/craft-skill.schema.yaml` with agentskills.io SKILL.md format

### 4.1 Code Implementation Skills (current — lowest V-level)

**Craft skills (standalone, framework-independent):**
- [x] derive-test-cases — V-model test derivation (4 strategies, coverage matrix)
- [x] develop-code — implementation with quantified quality rules

**Framework skills (DoWorkflow-specific):**
- [ ] code-implementation orchestration — agent-orchestrated implement → self-check → review loop for code + tests
- [ ] code-review framework skill — review agent validates code against detailed design, template, traceability

> Eval results (iteration 1, Haiku, combined): +67% delta vs baseline. Research: `research/implementation/`. Docs: `docs/guide/best-practices/implementation/`.

### 4.2 Unit Test Skills (current — lowest V-level)

**Craft skills:**
- [x] derive-test-cases (shared with 4.1)
- [ ] test-review craft skill — best practices for reviewing test quality

**Framework skills:**
- [ ] test-validation framework skill — verify tests trace to detailed design, check coverage criteria

### 4.3 Detailed Design Skills (next — one layer up)

**Craft skills:**
- [ ] write-detailed-design — best practices for detailed design (derived from documentation 3.3)
- [ ] review-detailed-design — quality checklist for detailed design review

**Framework skills:**
- [ ] detailed-design research/plan skill — context gathering, impact analysis, back-and-forth with human, produces implementation contract
- [ ] detailed-design orchestration — implement → self-check → review loop
- [ ] detailed-design review framework skill

### 4.4 SW Architecture Skills (later)

**Craft skills:**
- [ ] write-architecture
- [ ] review-architecture

**Framework skills:**
- [ ] architecture research/plan skill
- [ ] architecture orchestration
- [ ] architecture review framework skill

### 4.5 SW Requirements Skills (later)

**Craft skills:**
- [ ] write-requirement (EARS approach)
- [ ] review-requirement
- [ ] decompose-requirement

**Framework skills:**
- [ ] requirements research/plan skill
- [ ] requirements orchestration
- [ ] requirements review framework skill

### 4.6 System Requirements Skills (later)

**Craft skills:**
- [ ] write-system-requirement
- [ ] review-system-requirement

**Framework skills:**
- [ ] system requirements research/plan skill
- [ ] system requirements orchestration

### 4.7 System Test Skills (later)

- [ ] Craft and framework skills for system test case writing and review

### 4.8 Legacy Retrofit Skills (use case — combines other skills)

Specialized analysis/inference skills for reverse-engineering V-model artifacts from existing code:

- [ ] Code structure analysis skill
- [ ] Behavior characterization skill
- [ ] Requirement inference skill (extract implicit requirements from code)
- [ ] Design inference skill (extract implicit architecture/design from code)
- [ ] Gap analysis skill (what's missing, what needs formalization)
- [ ] Improvement suggestion skill (based on best practices documentation)
- [ ] Cross-session handoff documents for large analysis work

### 4.9 Integration Skills (after craft + framework skills proven)

- [ ] Trace link creation skill
- [ ] Trace validation skill (temporary engine — see 2A-temp)
- [ ] Schema compliance checking

### 4.10 Orchestration (deferred)

- [ ] DRTDD pipeline (phase sequencing, handoffs, gates)
- [ ] Legacy scan pipeline (module-by-module analysis)
- [ ] Compliance report pipeline (evidence aggregation)

---

## Build Order

Bottom-up, one V-model layer at a time. For each layer: documentation first, then template, then craft skills, then framework skills.

### Phase 1: Lowest V-Level (Code + Unit Tests) — CURRENT

```
1. Documentation for code implementation (3.1)
   └── V-model context, anti-patterns, examples, framework integration
2. Documentation for unit testing (3.2)
   └── V-model context, test derivation best practices, coverage criteria
3. Framework skills for code + unit test layer (4.1, 4.2)
   └── Orchestration loop, review skills, traceability integration
4. Traceability: link type rules for code ↔ detailed design (2A partial)
```

### Phase 2: Detailed Design Layer

```
1. Documentation for detailed design (3.3)
2. Craft skills: write-detailed-design, review-detailed-design (4.3 craft)
3. Framework skills: research/plan, orchestration, review (4.3 framework)
4. Traceability: link type rules for detailed design ↔ code, detailed design ↔ SW architecture (2A partial)
5. Wire full V-pair: detailed design → code + unit tests (end-to-end)
```

### Phase 3: SW Architecture Layer

```
1. Documentation for SW architecture (3.4)
2. Craft + framework skills (4.4)
3. Traceability extensions
4. Wire V-pair: architecture → detailed design → code
```

### Phase 4: SW Requirements Layer

```
1. Documentation for SW requirements (3.5)
2. Craft + framework skills (4.5)
3. Traceability extensions
4. Wire V-pair: requirements → architecture → detailed design → code
```

### Phase 5: System Level + Legacy Retrofit

```
1. System requirements documentation + skills (3.6, 4.6)
2. System test documentation + skills (3.7, 4.7)
3. Legacy retrofit skills (4.8)
4. General documentation (3.9 — V-model overview, DRTDD, safety analysis)
5. Deterministic traceability engine (2B)
```

### Phase 6: Polish + Tooling

```
1. Review documentation + skills (3.8)
2. Plan schemas (1.2 plans)
3. Assurance level configuration (1.3)
4. Translation layer (1.4)
5. Scaffold tool (1.5)
6. Integration skills (4.9)
7. Orchestration pipelines (4.10)
8. Web GUI (2C)
```

---

## Guiding Principles

1. **Documentation is the foundation.** Write the documentation first, derive everything else from it. If we can't explain it in documentation, we can't claim the AI skills will produce compliant output.
2. **Bottom-up build order.** Start from code, work up through each V-model layer. Prove each layer end-to-end.
3. **Components are independent (SOLID).** No forced coupling. But designed to compose.
4. **Individual loops per layer.** Each V-model layer gets its own orchestration. Refactor to shared patterns only after building 3-4 layers.
5. **Human drives, AI executes, human verifies.** Not an autonomous pipeline. Mid-senior engineers orchestrating AI agents.
6. **Deterministic where possible.** Validation is a tool concern, not an agent concern.
7. **EARS is a skill preference, not a framework requirement.**
8. **Discuss before writing.** Explain approach, show visually, motivate, get approval.
9. **Model-tier aware.** Skills must work on Haiku. Test with baseline comparison.
10. **Incremental always.** Module-by-module. Layer-by-layer.
11. **Follow agentskills.io spec.** SKILL.md format, progressive disclosure, scripts for determinism.
12. **Use `/skill-creator` for development.** Draft, test, evaluate, iterate.
