# Backlog

Organized by component. Build order is bottom-up: start from the lowest V-model layer (code + unit tests) and work upward through each layer, completing documentation → template → craft skill → framework skill for each layer before moving to the next.

---

## Open Questions

Questions identified during architecture refinement (2026-04-05) that need resolution as we encounter them:

1. **Handoff artifact format:** The research/plan phase produces an "implementation contract" for the agent-orchestrated loop. What fields does this contract contain? Similar to current_task.yaml but richer — needs design decisions, rationale, scope boundaries from the discussion.

2. **Code-level research/plan:** Should the lowest level (code implementation) have a lightweight research/plan step for implementation strategy (e.g., "token bucket vs sliding window"), or is the detailed design sufficient input? Current position: probably a brief "read design, confirm approach" step within develop-code, not a separate skill.

3. **Legacy RE skill boundaries:** Legacy reverse-engineering is a use case combining analysis craft skills + templates + traceability. What specific analysis skills are needed? (Code structure analysis, behavior characterization, requirement inference, gap detection.) How do they differ from forward-engineering craft skills?

4. **Orchestration commonality:** Each V-model layer gets its own implementation loop initially. After building 3-4 layers, what patterns emerge? When do we refactor to shared orchestration?

5. **Documentation format:** ~~RESOLVED (2026-04-05).~~ Per-artifact documentation is HTML pages in `docs/guide/artifacts/`. Three-tier knowledge system: research (raw sources) → HTML docs (refined, single source of truth) → agent skills (LLM-distilled). No markdown intermediate layer — absorbed into HTML and deleted.

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

Each artifact type gets a comprehensive HTML page (`docs/guide/artifacts/<artifact>.html`) covering:
1. What it is (V-model independent introduction)
2. V-model context (where it fits, why, standards perspective)
3. Producing a quality workproduct (the bulk — V-model independent, craft-level)
   - Best practices, patterns/anti-patterns, quality metrics, examples for every principle
4. V-model specific considerations (if any additional V-model gotchas)
5. Framework integration (our templates, schemas, traceability)
6. AI skills integration (future — how agent skills relate)

Priority: quality workproduct first, V-model fit second.

### 3.1 Code Implementation Documentation (DONE — lowest V-level)

- [x] Complete artifact page: `docs/guide/artifacts/source-code.html` (1200+ lines)
- [x] Section 1: What is source code (general intro)
- [x] Section 2: V-model context (position, "implements" meaning, standards perspective)
- [x] Section 3: Producing quality source code (coding standards, SOLID, clean code, DRY/KISS/YAGNI, function/class design, code smells, immutability, design patterns, architecture at code level, hexagonal/ports-adapters, testability, cohesion/coupling, AI-assisted development, code review checklist)
- [x] Section 4: V-model specific considerations (traceability, dead code, gold plating, source-to-object, review independence)
- [x] Section 5: Framework integration (detailed design input, trace links, artifact envelope, review record)
- [x] Section 6: AI skills integration (stub)
- [x] Absorbed and deleted `docs/guide/best-practices/implementation/` (5 markdown files)

> Done: `docs/guide/artifacts/source-code.html`

### 3.2 Unit Test Documentation (DONE — lowest V-level, reworked 2026-04-05)

- [x] Complete artifact page: `docs/guide/artifacts/unit-test.html` (1150+ lines)
- [x] Section 1: What is unit testing (general intro)
- [x] Section 2: V-model context ("tests verify design, not code", standards perspective, coverage criteria by assurance level)
- [x] Section 3: Producing quality unit tests — 7 knowledge-domain subsections:
  - [x] 3.1 Test design principles (behavior vs implementation, tests as specifications, assertion quality, testability as design signal, F.I.R.S.T. properties, economics)
  - [x] 3.2 Test derivation strategies (4 strategies with examples, coverage matrix — reframed from procedure to knowledge)
  - [x] 3.3 Test structure and readability (AAA, naming conventions, parameterized tests, @Nested, reducing noise)
  - [x] 3.4 Test doubles (Meszaros taxonomy, state vs behavior verification, preference hierarchy, over-mocking, contract testing)
  - [x] 3.5 Test smells and maintainability (8 smells, fragile test deep dive, anti-patterns, builders, custom assertions)
  - [x] 3.6 Coverage and completeness (requirements vs structural, MC/DC, mutation testing as quality metric)
  - [x] 3.7 AI-assisted test development (empirical data, failure modes, spec-to-test vs code-to-test, safety implications)
- [x] Section 4: V-model specific considerations (independence, documentation, traceability, robustness, regression)
- [x] Section 5: Framework integration (design artifact mapping, trace links, coverage matrix as trace validation)
- [x] Section 6: AI skills integration (stub)
- [x] Absorbed and deleted `docs/guide/best-practices/testing/` (3 markdown files)
- [x] Backed by 6 research documents in `research/implementation/`

> Done: `docs/guide/artifacts/unit-test.html`

### 3.3 Detailed Design Documentation (DONE — one layer up)

- [x] Complete artifact page: `docs/guide/artifacts/detailed-design.html` (1096 lines)
- [x] Section 1: What is detailed design (general intro, test derivation litmus test)
- [x] Section 2: V-model context (position, design-before-code, standards table: DO-178C, ASPICE, ISO 26262, IEC 62304)
- [x] Section 3: Producing quality detailed designs — 8 knowledge-domain subsections:
  - [x] 3.1 Design documentation fundamentals (Parnas, Reeves, IEEE 1016, what-to-document table)
  - [x] 3.2 Interface specification (DbC, Hoare triples, complete contract checklist, good/bad examples, LSP)
  - [x] 3.3 Behavioral specification (algorithm spec, decision tables, state machines, specification patterns, formalism spectrum)
  - [x] 3.4 Design rationale and decisions (Kruchten, ADRs, constraint taxonomy)
  - [x] 3.5 Error handling and fault containment (Bloch exception design, Nygard stability patterns, error handling matrix, safety-critical)
  - [x] 3.6 Dynamic behavior and concurrency (Goetz thread safety, timing constraints, ARINC 653/FFI)
  - [x] 3.7 Scaling: what needs a design and how much detail (economics, standards tiering, 3-tier model, metrics, legacy retrofit)
  - [x] 3.8 AI-assisted detailed design (forward generation, legacy retrofit, post-hoc paraphrase trap, regulatory status)
- [x] Section 4: V-model specific considerations (traceability, derived requirements, review independence, code-as-LLR, ASPICE failures)
- [x] Section 5: Framework integration — full schema walkthrough (Layer 1/2/3 model, heading structure, table formats, ID conventions, principle-to-schema mapping)
- [x] Section 6: AI skills integration (stub)
- [x] Two complete worked examples: Fuel Control component (Layer 3 with embedded unit) and Configuration Loader (Layer 1 only)
- [x] Backed by 7 research documents in `research/detailed-design/` + existing Category A doc
- [x] Updated detailed-design.schema.yaml to v2.0 markdown format with Layer 1/2/3 model

> Done: `docs/guide/artifacts/detailed-design.html`

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
- [ ] Framework user manual (how to use VModelWorkflow)

---

## Component 4: AI Skills

Two categories: craft skills (standalone, derived from documentation) and framework skills (VModelWorkflow-specific).

### Skill Foundation

- [x] Define craft skill contract schema (design-time reference)
- [x] Define orchestration pipeline contract schema (design-time reference)
- [ ] Reconcile `schemas/core/craft-skill.schema.yaml` with agentskills.io SKILL.md format

### 4.1 Code Implementation Skills (current — lowest V-level)

**Craft skills (standalone, framework-independent):**
- [x] derive-test-cases — V-model test derivation (4 strategies, coverage matrix)
- [x] develop-code — implementation with quantified quality rules

**Framework skills (VModelWorkflow-specific):**
- [ ] code-implementation orchestration — agent-orchestrated implement → self-check → review loop for code + tests
- [ ] code-review framework skill — review agent validates code against detailed design, template, traceability

> Eval results (iteration 1, Haiku, combined): +67% delta vs baseline. Research: `research/implementation/`. Docs: `docs/guide/artifacts/source-code.html`.

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

### Phase 1: Lowest V-Level (Code + Unit Tests) — DONE (documentation)

```
1. [DONE] Documentation for code implementation (3.1)
   └── docs/guide/artifacts/source-code.html — comprehensive, 6 sections
2. [DONE] Documentation for unit testing (3.2)
   └── docs/guide/artifacts/unit-test.html — comprehensive, 6 sections
3. [NEXT] Skills for the lower V (code + unit tests + detailed design)
   └── Craft skills, framework skills, orchestration
4. Traceability: link type rules for code ↔ detailed design (2A partial)
```

### Phase 2: Detailed Design Layer — DONE (documentation)

```
1. [DONE] Documentation for detailed design (3.3)
   └── docs/guide/artifacts/detailed-design.html — 1096 lines, 8 knowledge domains
   └── 7 research docs in research/detailed-design/ + Category A doc
   └── Schema v2.0 with Layer 1/2/3 model, two worked examples
2. [NEXT] Craft skills: write-detailed-design, review-detailed-design (4.3 craft)
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

---

## Procedure: Research Before Documentation

Every artifact documentation page (Component 3) requires thorough research before writing. This procedure ensures the documentation is authoritative, sourced, and at the caliber needed to serve as the single source of truth from which AI skills are derived.

### Why This Exists

The unit test documentation (3.2) was initially written from V-model standards research alone. On review, it read like an expanded agent skill — procedural, thin on craft knowledge, organized as workflow steps rather than knowledge domains. The source code documentation (3.1), which had three research documents behind it (standards, clean code best practices, and a synthesis), was significantly deeper. The difference is directly attributable to research depth.

**Rule: documentation quality is bounded by research quality.** No amount of writing skill compensates for thin source material.

### Research Categories Per Artifact

Each artifact documentation page covers six sections. Research must support all of them, but the bulk goes to Section 3 (producing quality work products). The research breaks into three categories:

#### Category A: V-Model Standards Research
**What:** What do DO-178C, ISO 26262, ASPICE, and IEC 62304 specifically require or recommend for this artifact type? Cross-standard comparison tables. Scaling by assurance level.

**Already done for:** Source code (`research/implementation/v-model-standards-implementation.md`), Unit tests (`research/implementation/v-model-standards-unit-testing.md`).

**Typical output:** One research document, 300-500 lines. Covers each standard's specific sections, then a cross-standard comparison.

#### Category B: Craft Best Practices Research
**What:** The professional engineering knowledge needed to produce a high-quality artifact of this type, independent of any V-model or safety standard. This is the equivalent of "Clean Code" for source code — the authoritative body of knowledge from recognized experts.

**This is the bulk of the research and the most commonly underestimated.** For each artifact type, identify:

1. **Foundational authors and works** — who are the recognized authorities? (e.g., for source code: Martin, Fowler, Beck, Meszaros, Freeman/Pryce. For requirements: Robertson, Wiegers, Hull/Jackson/Dick, Mavin for EARS.)
2. **Core principles and philosophy** — the "why" behind the practices, not just the "how"
3. **Patterns and anti-patterns** — with concrete examples, attributed to sources
4. **Quality metrics** — measurable indicators of quality for this artifact type
5. **Common mistakes** — what goes wrong in practice, with empirical data where available

**Expect 3-5 research documents per artifact type for this category.** The source code chapter needed: SOLID/clean code, architecture patterns, design patterns, AI-assisted development. The unit test chapter needs: test design philosophy, test doubles, test smells/maintainability, test organization, AI test quality.

**Typical output per document:** 300-600 lines. Specific claims attributed to specific sources with URLs.

#### Category C: AI-Specific Research
**What:** How does AI-assisted development specifically affect this artifact type? Empirical data on AI quality for this artifact. Common AI failure modes. Mitigation strategies.

**Typical output:** One research document, 200-400 lines. Must include quantitative data where available (2023-2025 studies preferred).

### Research Quality Bar

Each research document must meet these criteria:

- **Attributed claims.** Every factual claim cites a specific source. No "it is generally accepted that..." without a source.
- **Real sources.** Web-searchable, verifiable. Published books, peer-reviewed papers, recognized industry blogs (Fowler, Google Testing Blog), official standards documents. Never present LLM training knowledge as research.
- **Cross-referenced.** Where multiple sources address the same topic, note agreement or disagreement.
- **Practical examples.** Key concepts illustrated with concrete code or artifact examples.
- **Honest about gaps.** If a claim's provenance is questionable, say so (e.g., the IBM "100x cost" data).

### Procedure Steps

For each artifact documentation page:

```
1. IDENTIFY research needs
   - List the knowledge domains needed for Section 3 (quality workproduct)
   - Check what research already exists in research/
   - Gap analysis: what's covered vs. what's needed

2. RESEARCH (parallelizable)
   - Category A: V-model standards (if not already done)
   - Category B: Craft best practices (typically 3-5 parallel research efforts)
   - Category C: AI-specific concerns (1 research effort)
   - All research written to research/ directory

3. REVIEW research with human
   - Present findings summary
   - Identify gaps or areas needing deeper investigation
   - Get approval before proceeding to documentation

4. WRITE documentation
   - Section 3 first (the bulk — craft knowledge)
   - Sections 1-2 (intro and V-model context)
   - Section 4 (V-model considerations)
   - Section 5 (framework integration)
   - Section 6 (AI skills — stub until skills are built)

5. REVIEW documentation against source code chapter caliber
   - Is Section 3 organized by knowledge domains (not procedures)?
   - Does every principle have a concrete example (good and bad)?
   - Is the depth comparable to source-code.html?
   - Would this be useful to a human engineer with no AI agent context?
```

### Research Needs Per Upcoming Artifact (preliminary)

| Artifact (Section) | Cat A: Standards | Cat B: Craft (estimated docs) | Cat C: AI |
|---|---|---|---|
| Unit Test (3.2 rework) | Done | 5 docs (done) | 1 doc (done) |
| Detailed Design (3.3) | Done (08-detailed-design-realism-and-compliance.md) | 6 docs (done): design-documentation-craft, interface-specification-design-by-contract, behavioral-specification-techniques, dynamic-behavior-concurrency, design-scaling-tiering, error-handling-specification | 1 doc (done): ai-assisted-detailed-design |
| SW Architecture (3.4) | Needed | ~3-4 (component decomposition, architecture styles, interface design, modularity/coupling metrics) | Needed |
| SW Requirements (3.5) | Partially done (EARS research exists) | ~3-4 (requirements engineering, EARS deep dive, testability/verifiability, requirements management) | Needed |
| System Requirements (3.6) | Needed | ~2-3 (system-level requirements, allocation, derived requirements) | Needed |
| System Test (3.7) | Needed | ~3-4 (system test strategy, test environment, acceptance criteria, regression strategy) | Needed |
| Review (3.8) | Needed | ~2-3 (review techniques, inspection process, review psychology) | Needed |

These estimates will be refined when each artifact's research phase begins.
