# Backlog

Organized by component. Build order is top-down for documentation: start from system requirements and work down through the V-model layers to understand the complete information cascade and handoff contracts. Skills are built after documentation coverage is complete, informed by the full picture. For each layer: documentation first, then template, then craft skill, then framework skill.

---

## Open Questions

Questions identified during architecture refinement (2026-04-05) that need resolution as we encounter them:

1. **Handoff artifact format:** The research/plan phase produces an "implementation contract" for the agent-orchestrated loop. What fields does this contract contain? Similar to current_task.yaml but richer — needs design decisions, rationale, scope boundaries from the discussion.

2. **Code-level research/plan:** Should the lowest level (code implementation) have a lightweight research/plan step for implementation strategy (e.g., "token bucket vs sliding window"), or is the detailed design sufficient input? Current position: probably a brief "read design, confirm approach" step within develop-code, not a separate skill.

3. **Legacy RE skill boundaries:** Legacy reverse-engineering is a use case combining analysis craft skills + templates + traceability. What specific analysis skills are needed? (Code structure analysis, behavior characterization, requirement inference, gap detection.) How do they differ from forward-engineering craft skills?

4. **Orchestration commonality:** Each V-model layer gets its own implementation loop initially. After building 3-4 layers, what patterns emerge? When do we refactor to shared orchestration?

5. ~~**Documentation format:**~~ RESOLVED (2026-04-05). Per-artifact documentation is HTML pages in `docs/guide/artifacts/`. Three-tier knowledge system: research (raw sources) → HTML docs (refined, single source of truth) → agent skills (LLM-distilled). No markdown intermediate layer — absorbed into HTML and deleted.

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

**Pre-requirements (project entry point):**
- [ ] Stakeholder Register schema (stakeholders, role/authority/interest, elicitation method, concurrence status)
- [ ] Stakeholder Need schema (captured need, source stakeholder, priority, validation status, trace to system req)
- [ ] Concept of Operations (ConOps) schema (mission, operational environment, scenarios, mode state machine, support concept)
- [ ] Completeness Analysis Record schema (method used, scope, findings, derived requirements generated, residual gaps — generic for FTA/FMEA/STPA/PBR)
- [ ] Allocation Matrix schema (requirement-to-component mapping, rationale, budget decomposition, derived requirement flags)

**Left side (development):**
- [x] System Requirements schema
- [x] SW Requirements schema
- [x] SW Architecture schema
- [x] Detailed Design schema

**Right side (verification):**
- [x] System Test Case schema
- [ ] Review Record schema (checklist, findings, verdict, reviewer qualification — also used for PBR multi-perspective reviews and concurrence gates)
- [ ] Requirements Baseline schema (baseline snapshot, version, included requirements, approval authority, change control ref)

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

### Completed Documentation (lower V, bottom-up)

| Artifact | Output | Lines | Key features |
|---|---|---|---|
| 3.1 Code Implementation | `docs/guide/artifacts/source-code.html` | 1314 | 6 sections, SOLID, clean code, hexagonal, AI-assisted |
| 3.2 Unit Test | `docs/guide/artifacts/unit-test.html` | 1211 | 7 knowledge-domain subsections, coverage matrix, AI test quality |
| 3.3 Detailed Design | `docs/guide/artifacts/detailed-design.html` | 1096 | 8 knowledge domains, Layer 1/2/3 model, 2 worked examples, schema v2.0 |

### Upper V Research (serves 3.4–3.9) — ALL DONE

Six research efforts covering the full cascade from stakeholder needs through system requirements, SW requirements, and architecture down to the detailed design handoff, plus AI capabilities at the system level.

| Research | Topic | Output | Lines |
|---|---|---|---|
| 1 | Standards — system-level processes | `research/system-level/01-standards-system-level-processes.md` | ~500 |
| 2 | Stakeholder analysis and needs engineering | `research/system-level/02a` through `02d` (4 docs) | ~3100 |
| 3 | System requirements engineering craft | `research/system-level/03a` through `03d` (4 docs) | ~3750 |
| 4 | Safety analysis → requirements interface | `research/system-level/04a` through `04c` (3 docs) | ~2700 |
| 5 | System and SW architecture craft | `research/system-level/05a` through `05f` (6 docs) | ~3100 |
| 6 | AI at the system level (lighter) | `research/system-level/06a`, `06b` (2 docs) | ~700 |

Total: 20 research documents, ~13,850 lines. Key cross-cutting learnings absorbed into the research docs themselves.

Codex ingestion pending from Research 2-4: Alexander 2005, Shull/Basili 2000 PBR, Kelly/Sherif/Hops 1992, Kamsties 2001, Rashid 2021, Gotel & Finkelstein 1994 (403), Leveson STPA handbook, MIL-STD-882E, Bosch FMEA booklet, FAA DOT/FAA/TC-24/16 (403), Wilkinson/Fleming (ECONNREFUSED).

**Research quality bound:** Web-searchable sources are good for standards and frameworks. Core craft knowledge lives in books not fully available online — research captures frameworks and key ideas, with pointers to specific book chapters for deeper reading.

---

### 3.4 Stakeholder Identification & Needs Documentation — NEXT

The true entry point to any project. Before requirements exist, you need to know who the stakeholders are, what they need, and how to validate that understanding. This is where every V-model project starts — or should start.

**Scope:** Stakeholder discovery, elicitation techniques, needs capture, conflict resolution, concurrence gates, validation. The artifact is the stakeholder register + captured needs. This is not just a safety concern — any project benefits from structured stakeholder analysis.

**Depends on:** Research 2 (02a, 02b, 02d) — done.

- [ ] Complete artifact page: `docs/guide/artifacts/stakeholder-needs.html`
  - [ ] Section 1: What are stakeholder needs (stakeholders, their concerns, the gap between needs and requirements)
  - [ ] Section 2: V-model context (precedes all requirements; inputs from project initiation, outputs to ConOps and system requirements)
  - [ ] Section 3: Producing quality stakeholder analysis (the bulk — stakeholder identification techniques, elicitation methods (interviews, observation, document analysis, workshops, assumptions analysis), silent stakeholder discovery, conflict resolution (AHP), concurrence gates, validation via PBR and operational scenarios)
  - [ ] Section 4: V-model specific considerations (traceability from every requirement back to stakeholder source, concurrence as formal gate, stakeholder turnover as program risk, regulatory stakeholders)
  - [ ] Section 5: Framework integration (stub — stakeholder register template, stakeholder need template, trace links)
  - [ ] Section 6: AI skills integration (stub)

**Handoff down:** Validated stakeholder needs with source attribution, priority, and concurrence status → feeds ConOps and system requirements.
**Feedback up:** Derived requirements discovered downstream that may require stakeholder re-concurrence.

### 3.5 Concept of Operations (ConOps) Documentation

The operational picture before requirements exist. Translates stakeholder needs into an operational context that system requirements can be derived from. Defines what the system does in its environment, not how it does it internally.

**Scope:** Mission/objectives, operational environment, operational scenarios, mode state machine, user organization, support concept, performance criteria. Environmental characterization. Operator workload analysis.

**Depends on:** Research 2 (02c), Research 5 (05a for modes) — done.

- [ ] Complete artifact page: `docs/guide/artifacts/conops.html`
  - [ ] Section 1: What is a ConOps (the bridge between stakeholder needs and system requirements)
  - [ ] Section 2: V-model context (receives stakeholder needs, outputs to system requirements and completeness analysis; CONOPS-to-requirements traceability mandatory)
  - [ ] Section 3: Producing a quality ConOps (the bulk — mission/objective definition, operational environment characterization (physical, regulatory, interoperability), operational scenarios as testable conditions, mode state machine (startup/normal/degraded/emergency/shutdown/maintenance), mode transition timing, user organization and roles, support concept, performance criteria, operator workload analysis)
  - [ ] Section 4: V-model specific considerations (ConOps as source for system test scenarios, mode completeness as requirements completeness technique, environmental constraints driving derived requirements)
  - [ ] Section 5: Framework integration (stub — ConOps template, trace links to stakeholder needs above and system requirements below)
  - [ ] Section 6: AI skills integration (stub)

**Handoff down:** Operational scenarios, mode state machine, environmental constraints, performance criteria → feeds system requirements derivation and completeness analysis.
**Feedback up:** Design constraints discovered during architecture that invalidate operational assumptions.

### 3.6 Completeness Analysis Documentation

Techniques that discover requirements stakeholders cannot articulate. Generalized from safety analysis — the goal is completeness discovery, applicable to any system quality goal. Safety is the highest-rigor application, but the techniques (asking "what could go wrong" and "what's implicit") have value at any rigor level.

**Scope:** FTA, FMEA, STPA as requirement-generating methods (not just risk assessment). Three requirement classes: negative (prohibited states), implicit assumptions (timing, environmental, dependencies), interaction (cross-component). Mode analysis. PBR (Perspective-Based Reading) as validation technique. Scaling by rigor level — full safety analysis at high assurance, lightweight completeness checks at lower levels.

**Depends on:** Research 4 (04a, 04b, 04c), Research 2 (02b for validation), Research 6 (06a for PBR, 06b for AI safety analysis limits) — done.

- [ ] Complete artifact page: `docs/guide/artifacts/completeness-analysis.html`
  - [ ] Section 1: What is completeness analysis (the gap between what stakeholders ask for and what the system actually needs)
  - [ ] Section 2: V-model context (runs in parallel with requirements capture, not after; feeds requirements at every level; outputs to system requirements, SW requirements, architecture)
  - [ ] Section 3: Producing quality completeness analysis (the bulk):
    - [ ] 3.1 Deductive methods (FTA): top-down from undesired event, minimal cut sets, single-point-failure identification, failure rate budgets
    - [ ] 3.2 Inductive methods (FMEA): bottom-up from components, five-step process, S/O/D scoring, Action Priority over RPN
    - [ ] 3.3 Systems-theoretic methods (STPA): control-based, UCAs, loss scenarios, finds unsafe interactions between correctly-functioning components — addresses software blind spot
    - [ ] 3.4 Perspective-Based Reading (PBR): multi-perspective validation (designer, tester, customer), systematic defect detection
    - [ ] 3.5 Three requirement classes discovered by analysis (negative, implicit, interaction) with examples
    - [ ] 3.6 Mode analysis as completeness technique (startup, degraded, emergency requirements)
    - [ ] 3.7 Scaling by rigor level (full safety analysis → lightweight "what could go wrong" checklist)
    - [ ] 3.8 FTTI timing decomposition as example of derived requirements chain
  - [ ] Section 4: V-model specific considerations (safety analysis as source not just verification, derived requirements feedback loop, FDAL/IDAL allocation, independence of analysis, CMA requirements)
  - [ ] Section 5: Framework integration (stub — completeness analysis record template, trace links to requirements)
  - [ ] Section 6: AI skills integration (stub — honest about AI limits: brainstorming strong, completeness certification not viable)

**Handoff down:** Derived requirements (negative, implicit, interaction) with method traceability → feeds into system requirements and SW requirements.
**Feedback up:** Design decisions that create new failure modes requiring re-analysis.

### 3.7 System Requirements Documentation

How validated stakeholder needs, ConOps, and completeness analysis results become formal system requirements. The first formal specification layer — everything upstream is "what we need," this is "what the system shall do."

**Depends on:** Research 1, 2, 3, 4 (and partially 5 for allocation context) — all done.

- [ ] Complete artifact page: `docs/guide/artifacts/system-requirements.html`
  - [ ] Section 1: What are system requirements (the bridge from needs/analysis to formal specification)
  - [ ] Section 2: V-model context (receives from stakeholder needs, ConOps, completeness analysis; outputs to SW/HW allocation; bidirectional with completeness analysis)
  - [ ] Section 3: Producing quality system requirements (the bulk — requirement writing craft (INCOSE rules, EARS patterns, performance requirement 5-element rule), completeness techniques (functional/structural/behavioral/constraint/interface), allocation as budgeting (timing, reliability, resources), operational scenario coverage, non-functional requirements, requirements management (baselines, change control, impact analysis), traceability to stakeholder needs)
  - [ ] Section 4: V-model specific considerations (bidirectional traceability, derived requirements feedback loop (Peterson study: known to be broken in practice), safety requirements as first-class (carry ASIL/DAL), independence of verification, FTTI decomposition example)
  - [ ] Section 5: Framework integration (stub — template, schema, allocation matrix, trace links)
  - [ ] Section 6: AI skills integration (stub)

**Handoff down:** Allocated system requirements with budget decomposition, verification criteria, and traceability obligations → feeds SW requirements and HW requirements.
**Feedback up:** Derived requirements from SW/HW design that aren't traceable to parent requirements → must be classified (safety-relevant or internal design decision) and fed back into baseline.

### 3.8 SW Requirements Documentation

Directly receives allocated system requirements. The first purely software-scoped layer and where most of this project's tooling will begin operating.

**Depends on:** Research 1, 3 (and partially 2 for upstream context) — all done.

- [ ] Complete artifact page: `docs/guide/artifacts/sw-requirements.html`
  - [ ] Section 1: What are SW requirements (system allocation → SW requirements → architecture input)
  - [ ] Section 2: V-model context (receives from system, outputs to architecture + qualification tests)
  - [ ] Section 3: Producing quality SW requirements (the bulk — EARS patterns, testability as quality gate, completeness checks, non-functional decomposition, interface requirements, behavioral vs declarative style, requirement granularity)
  - [ ] Section 4: V-model specific considerations (derived requirements flow-up, traceability, review independence, safety-critical requirements handling)
  - [ ] Section 5: Framework integration (template, schema, trace links to system req above and architecture below)
  - [ ] Section 6: AI skills integration (stub)

**Handoff down:** SW requirements with verification criteria and traceability to system requirements → feeds architecture decomposition.
**Feedback up:** Derived requirements from architecture/design decisions → classified and fed back to system requirements if safety-relevant.

### 3.9 SW Architecture Documentation

Receives SW requirements, decomposes into components, defines interfaces, allocates requirements to components. Produces the structure that detailed design fills in.

**Depends on:** Research 1, 5 — all done.

- [ ] Complete artifact page: `docs/guide/artifacts/sw-architecture.html`
  - [ ] Section 1: What is SW architecture (component decomposition, interface definition, allocation)
  - [ ] Section 2: V-model context (receives SW requirements, outputs to detailed design + integration tests)
  - [ ] Section 3: Producing quality architecture (the bulk — volatility-based decomposition, Parnas information hiding, cohesion/coupling metrics, interface specification (Design by Contract), dependency principles (DIP, SDP, SAP), architecture evaluation (lightweight ATAM, fitness functions), fault tolerance patterns, error propagation strategy, modular monolith as underused pattern, architectural patterns for critical systems, partitioning)
  - [ ] Section 4: V-model specific considerations (derived requirements, deactivated code, partitioning for safety, traceability to requirements, review independence, integration test derivation from interface contracts)
  - [ ] Section 5: Framework integration (template, schema, trace links to SW req above and DD below)
  - [ ] Section 6: AI skills integration (stub)

**Handoff down:** Component specifications with interface contracts, allocated requirements, and design rationale → feeds detailed design.
**Feedback up:** Design impossibilities, constraint violations, derived requirements → feeds back to SW requirements and potentially system requirements.

### 3.10 System Test Documentation (later)

- [ ] V-model context
- [ ] Best practices
- [ ] Anti-patterns
- [ ] Examples
- [ ] Framework integration

### 3.11 Review Documentation (later)

- [ ] V-model context for reviews (independent review requirements, what reviews verify)
- [ ] Best practices per artifact type being reviewed (including PBR multi-perspective reviews)
- [ ] Anti-patterns
- [ ] Examples

### 3.12 General Documentation

- [ ] V-model overview (what the V-model is, philosophy, layers, how they connect — generalized beyond safety)
- [ ] DRTDD explanation (Design-Requirement-Test Driven Development)
- [ ] Completeness analysis overview (FTA, FMEA, STPA — as engineering tools, not just safety tools)
- [ ] Framework user manual (how to use VModelWorkflow)

---

## Component 4: AI Skills

> **Architecture design document:** [`docs/guide/skills-architecture.html`](../guide/skills-architecture.html) — defines all skills, agents, orchestration, contracts, config, and file layout. This backlog tracks what to build and in what order; the architecture doc defines what each component is.

Three-layer architecture: Layer 1 (craft + framework skills) → Layer 2 (agents) → Layer 3 (orchestration). All components use `vmodel-skill-*` / `vmodel-agent-*` naming. Vendor-neutral model tiers (Tier 1 Reasoning / Tier 2 Workhorse / Tier 3 Fast).

### Skill Foundation

- [x] Define craft skill contract schema (design-time reference)
- [x] Define orchestration pipeline contract schema (design-time reference)
- [x] Skills architecture designed (`docs/guide/skills-architecture.html`, 2026-04-06)
- [ ] Reconcile `schemas/core/craft-skill.schema.yaml` with agentskills.io SKILL.md format
- [x] Finalize `.vmodel/config.yaml` schema (Phase A2, `schemas/core/vmodel-config.schema.yaml`, 2026-04-09). Add layer categories as higher V-model layers are built.

---

### Completed Lower V Skill Work

**Phase A: Foundation** — DONE (2026-04-06 to 2026-04-09)
Reference files extracted/updated from documentation. Config schema finalized. 8 reference files created/updated in `.claude/skills/` directories.

**Phase B: Revise existing craft skills** — DONE (2026-04-09 to 2026-04-10)

| Skill | Key changes | Eval (Haiku) |
|---|---|---|
| B1. `develop-code` | Added SOLID principles, functional core/imperative shell, 7-point self-check | 91% (+13% over baseline) |
| B2. `derive-test-cases` | Added 8 principles, coverage matrix, HALT for >2 mocks, 6-point self-check | 100% (+3% over old skill) |

**Phase C1: vmodel-skill-review-code** — DONE (2026-04-10)
Three-pass review (code quality, test quality, cross-checks), three verdicts (APPROVED, REJECTED, DESIGN_ISSUE). Eval (Haiku): 95.8% (+25% over baseline). Discriminating assertion: test anti-pattern detection.

---

### Phase C2-C4: Detailed Design Skills — PAUSED

Waiting for top-down documentation (3.4-3.6) to establish handoff contracts between V-model layers.

<details>
<summary>C2-C4 original scope (preserved for when work resumes)</summary>

**C2. vmodel-skill-develop-dd** (forward detailed design)

- [ ] Draft SKILL.md: interface specification (DbC), behavioral specification, rationale, error handling design, tiering guidance
- [ ] Include reference: `design-quality-criteria.md`
- [ ] Create eval scenarios: component with requirements as input, different complexity levels
- [ ] Output format: Markdown with YAML frontmatter (per detailed-design schema v2.0)
- [ ] Eval and iterate

Context to load:
- `docs/guide/artifacts/detailed-design.html` §3.1-§3.7
- `docs/guide/skills-architecture.html` §Craft Skills → vmodel-skill-develop-dd
- `schemas/artifacts/detailed-design.schema.yaml` (v2.0 with Layer model)
- Reference file from Phase A1: `design-quality-criteria.md`

**C3. vmodel-skill-retrofit-dd** (reverse-engineer design from code)

- [ ] Draft SKILL.md: guards against post-hoc paraphrase, decision vs happenstance identification, human involvement triggers
- [ ] Include references: `design-quality-criteria.md`, `retrofit-risks.md`
- [ ] Create eval scenarios: existing code of varying quality, some with comments, some without
- [ ] Output format: same as develop-dd (Markdown with YAML frontmatter)
- [ ] Eval and iterate — expect more HALT conditions than develop-dd

Context to load:
- `docs/guide/artifacts/detailed-design.html` §3.8 (AI-Assisted Design, retrofit section)
- `docs/guide/skills-architecture.html` §Craft Skills → vmodel-skill-retrofit-dd
- Reference files from Phase A1: `design-quality-criteria.md`, `retrofit-risks.md`

**C4. vmodel-skill-review-dd** (detailed design review)

- [ ] Draft SKILL.md: interface completeness checklist, behavioral specification check, rationale presence, testability assessment
- [ ] Include references: `review-checklist-dd.md`, `design-quality-criteria.md`
- [ ] Create adversarial eval scenarios: good designs, designs with missing interfaces, designs without rationale, post-hoc paraphrase designs
- [ ] Output format: structured verdict (APPROVED / REJECTED / DESIGN_ISSUE)
- [ ] Eval and iterate

Context to load:
- `docs/guide/artifacts/detailed-design.html` §3 (all subsections — this is what the reviewer checks against)
- `docs/guide/skills-architecture.html` §Craft Skills → vmodel-skill-review-dd
- Reference files from Phase A1: `review-checklist-dd.md`, `design-quality-criteria.md`

</details>

---

### Phase D: Framework skills

Thin schema adapters. No craft knowledge. Validation is primarily "does the output match the schema?"

**D1. vmodel-skill-dd-template**

- [ ] Write SKILL.md: schema reference, Layer 1/2/3 model, heading structure, ID conventions, YAML frontmatter fields
- [ ] Content is extracted from `docs/guide/artifacts/detailed-design.html` §5 (Framework Integration)
- [ ] Lightweight validation: produce a design using the skill, check schema compliance

Context to load:
- `docs/guide/artifacts/detailed-design.html` §5 (Framework Integration, Schema sections)
- `schemas/artifacts/detailed-design.schema.yaml`
- `docs/guide/skills-architecture.html` §Framework Skills → vmodel-skill-dd-template

**D2. vmodel-skill-traceability**

- [ ] Write SKILL.md: trace file format, link types, content hash staleness, link rules per artifact type
- [ ] Content extracted from traceability schema + architecture doc

Context to load:
- `schemas/traceability/` (trace schema files)
- `docs/guide/skills-architecture.html` §Framework Skills → vmodel-skill-traceability
- `docs/guide/index.html` §Traceability sections

**D3. vmodel-skill-tool-checks**

- [ ] Write SKILL.md: config file format, resolution order, check execution, result interpretation
- [ ] Depends on `.vmodel/config.yaml` schema from Phase A2

Context to load:
- `.vmodel/config.yaml` schema from Phase A2
- `docs/guide/skills-architecture.html` §Configuration, §Framework Skills → vmodel-skill-tool-checks

---

### Phase E: Agents (integration)

Agent `.md` files define subagent system prompts that compose skills. Testing is integration-level: does the composition work correctly?

**E1. vmodel-agent-tdd-developer**

- [ ] Write agent definition: system prompt loading develop-code + derive-test-cases + traceability + tool-checks
- [ ] Define TDD process (red-green-refactor) in agent prompt
- [ ] Integration test: give task contract + design, verify agent produces code + tests + traces + check results

**E2. vmodel-agent-code-reviewer**

- [ ] Write agent definition: system prompt loading review-code + traceability + tool-checks
- [ ] Read-only tool access enforced
- [ ] Integration test: give task contract + design + developer output, verify structured verdict

**E3. vmodel-agent-dd-developer**

- [ ] Write agent definition: loads develop-dd OR retrofit-dd (mode selection), dd-template, traceability, tool-checks
- [ ] Integration test: forward mode with requirements input, retrofit mode with code input

**E4. vmodel-agent-dd-reviewer**

- [ ] Write agent definition: loads review-dd + dd-template + traceability + tool-checks
- [ ] Read-only tool access

**E5. vmodel-agent-task-decomposer**

- [ ] Write agent definition: reads implementation plan, produces task contracts
- [ ] No craft skills loaded — architectural reasoning task
- [ ] Tier 1 (Reasoning) model required

---

### Phase F: Orchestration

Wire everything together. This is the final phase for the lower V.

**F1. Research/plan skill (interactive)**

- [ ] Write skill for interactive research/plan phase
- [ ] Output: `implementation-plan.yaml` per architecture doc schema

**F2. Pipeline controller skill**

- [ ] Write lean orchestration skill: reads state, spawns agents, manages feedback loops
- [ ] Implements state machine from architecture doc
- [ ] Human gates: after decomposition, after all tasks
- [ ] Retry discipline: 3 attempts, different approach each time, root-cause before attempt 3

---

### Higher V-Model Layer Skills (later — same pattern)

### 4.4 Stakeholder Analysis Skills

**Interaction mode: Advisory** — AI helps identify stakeholder categories, suggests elicitation questions, checks for silent stakeholder gaps. Human drives all content (only humans know the real stakeholders).

**Craft skills:**
- [ ] vmodel-skill-stakeholder-analysis (advisory — identify categories, suggest elicitation questions, check gaps)
- [ ] vmodel-skill-review-stakeholder-needs (check completeness, conflict detection, concurrence readiness)

**Framework skills:**
- [ ] vmodel-skill-stakeholder-register-template
- [ ] vmodel-skill-stakeholder-need-template

> Requires: Documentation 3.4 (Stakeholder Needs) first.

### 4.5 ConOps Skills

**Interaction mode: Advisory** — AI helps structure operational scenarios, checks mode completeness, flags missing environmental characterization. Human drives the operational vision.

**Craft skills:**
- [ ] vmodel-skill-develop-conops (advisory — structure scenarios, check mode completeness, flag gaps)
- [ ] vmodel-skill-review-conops (scenario coverage, mode state machine completeness, environmental characterization)

**Framework skills:**
- [ ] vmodel-skill-conops-template

> Requires: Documentation 3.5 (ConOps) first.

### 4.6 Completeness Analysis Skills

**Interaction mode: Advisory/Collaborative** — AI brainstorms failure modes (its strong suit), guides structured analysis methods, identifies requirement classes. Human owns the judgment calls (what's a real hazard vs. noise).

**Craft skills:**
- [ ] vmodel-skill-completeness-analysis (guide through FTA/FMEA/STPA for requirement discovery, brainstorm failure modes, identify negative/implicit/interaction requirements)
- [ ] vmodel-skill-pbr-review (Perspective-Based Reading — simulate designer/tester/customer perspectives on requirements or designs)
- [ ] vmodel-skill-review-completeness (check analysis coverage, flag gaps in requirement classes)

**Framework skills:**
- [ ] vmodel-skill-completeness-record-template

> Requires: Documentation 3.6 (Completeness Analysis) first.
> Note: AI limits are real here — brainstorming strong, completeness certification not viable (<70% on benchmarks). Skills must be honest about this.

### 4.7 System Requirements Skills

**Interaction mode: Advisory** — agent structures human thinking, checks completeness/consistency against upstream artifacts (stakeholder needs, ConOps, completeness analysis). Human drives content.

**Craft skills:**
- [ ] vmodel-skill-write-system-req (advisory — check against INCOSE rules, EARS patterns, allocation budgets, traceability to needs/ConOps/analysis)
- [ ] vmodel-skill-review-system-req (completeness checks: functional/structural/behavioral/constraint/interface)
- [ ] vmodel-skill-allocate-req (help decompose requirements to components with budget tracking, suggest allocation criteria)

**Framework skills:**
- [ ] vmodel-skill-system-req-template
- [ ] vmodel-skill-allocation-matrix-template
- [ ] vmodel-skill-baseline-template

> Requires: Documentation 3.7 (System Requirements) first.

### 4.8 SW Requirements Skills

**Interaction mode: Advisory** — agent structures human thinking, checks completeness/consistency, suggests EARS patterns. Human drives content (captures stakeholder intent that only humans know).

**Craft skills:**
- [ ] vmodel-skill-write-req (EARS approach, advisory — structures and checks, human drives)
- [ ] vmodel-skill-review-req
- [ ] vmodel-skill-decompose-req

**Framework skills:**
- [ ] vmodel-skill-req-template

**Agents:**
- [ ] vmodel-agent-req-developer
- [ ] vmodel-agent-req-reviewer

> Requires: Documentation 3.8 (SW Requirements) first.

### 4.9 SW Architecture Skills

**Interaction mode: Collaborative** — agent proposes decomposition and checks consistency, human decides trade-offs (system-wide consequences).

**Craft skills:**
- [ ] vmodel-skill-develop-arch (collaborative — proposes, human decides)
- [ ] vmodel-skill-review-arch
- [ ] vmodel-skill-evaluate-arch (lightweight ATAM, fitness functions, quality attribute scenarios)

**Framework skills:**
- [ ] vmodel-skill-arch-template

**Agents:**
- [ ] vmodel-agent-arch-developer
- [ ] vmodel-agent-arch-reviewer

> Requires: Documentation 3.9 (SW Architecture) first.

### 4.10 System Test Skills (later)

- [ ] System test craft + framework skills
- [ ] Corresponding agents

### 4.11 Legacy Retrofit Skills (use case — combines other skills)

Specialized analysis/inference craft skills for reverse-engineering. `vmodel-skill-retrofit-dd` (Phase C3) is the first of these.

- [ ] Code structure analysis skill
- [ ] Behavior characterization skill
- [ ] Requirement inference skill
- [ ] Design inference skill
- [ ] Gap analysis skill
- [ ] Cross-session handoff documents for large analysis work

### 4.12 Integration Skills (after craft + framework skills proven)

- [ ] Trace link creation skill
- [ ] Trace validation skill (temporary engine — see 2A-temp)
- [ ] Schema compliance checking

### 4.13 Orchestration Pipelines (after per-layer orchestration proven)

- [ ] DRTDD pipeline (phase sequencing, handoffs, gates)
- [ ] Legacy scan pipeline (module-by-module analysis)
- [ ] Compliance report pipeline (evidence aggregation)

---

## Build Order

Top-down for documentation: understand the complete information cascade from customer request to code. Skills are built after documentation gives us the full picture of handoff contracts between layers.

### Completed Work

```
Lower V documentation (bottom-up, done before direction change):
  [DONE] Documentation for code implementation (3.1) — source-code.html, 1314 lines
  [DONE] Documentation for unit testing (3.2) — unit-test.html, 1211 lines
  [DONE] Documentation for detailed design (3.3) — detailed-design.html, 1096 lines, schema v2.0
  [DONE] Skills architecture designed — skills-architecture.html

Upper V research (top-down):
  [DONE] Research 1-6 — 20 docs, ~13,850 lines in research/system-level/

Lower V skills (partial):
  A.    [DONE] Foundation: reference files, config schema
  B.    [DONE] Revise existing craft skills (develop-code, derive-test-cases)
  C1.   [DONE] New craft skill: review-code
  C2-C4.[PAUSED] develop-dd, retrofit-dd, review-dd — waiting for top-down handoff knowledge
```

### Phase 3: Top-Down Documentation — NEXT

Work down from the true top of the V — stakeholder identification — through the complete cascade to architecture. Each layer's documentation explicitly captures what it hands off to the layer below AND what feedback flows back up. The V-model is generalized beyond safety: these techniques have value at any rigor level.

```
1. [NEXT] Stakeholder Identification & Needs documentation (3.4)
   └── Understand: who are the stakeholders, how to capture and validate their needs
   └── Handoff down: validated needs → ConOps + system requirements
   └── Feedback up: derived requirements requiring re-concurrence
2. Concept of Operations documentation (3.5)
   └── Understand: operational picture before requirements exist (scenarios, modes, environment)
   └── Handoff down: operational scenarios, mode state machine, constraints → system requirements
   └── Feedback up: design constraints invalidating operational assumptions
3. Completeness Analysis documentation (3.6)
   └── Understand: techniques for discovering requirements stakeholders can't articulate
   └── Generalized from safety: FTA/FMEA/STPA as completeness tools + PBR for validation
   └── Handoff down: derived requirements (negative, implicit, interaction) → system/SW requirements
   └── Feedback up: design decisions creating new failure modes
4. System Requirements documentation (3.7)
   └── Understand: needs + ConOps + analysis results → formal system specification
   └── Allocation as budgeting, FTTI decomposition, requirements management
   └── Handoff down: allocated requirements with budgets → SW requirements
   └── Feedback up: derived requirements from design → baseline update
5. SW Requirements documentation (3.8)
   └── Understand: system allocation → SW requirements → qualification test derivation
   └── Handoff down: SW requirements with verification criteria → architecture
6. SW Architecture documentation (3.9)
   └── Understand: SW requirements → component decomposition → interface definition → allocation
   └── Volatility-based decomposition, lightweight ATAM, fitness functions
   └── Handoff down: component specs with interface contracts → detailed design
7. Review and refine detailed-design.html (3.3) based on what we learned about the cascade
```

### Phase 4: Skills with Full Handoff Knowledge

With the complete documentation cascade in place, build skills knowing exactly what flows between layers. **Key principle:** agent autonomy varies by V-level. Upper V skills are interactive advisors (human drives, agent assists). Lower V skills are autonomous executors (agent drives, human reviews). Skill design must match the autonomy level.

```
1. Stakeholder analysis skills (4.4): stakeholder-analysis, review-stakeholder-needs
   └── Advisory mode — AI suggests categories and questions, human drives all content
2. ConOps skills (4.5): develop-conops, review-conops
   └── Advisory mode — AI structures scenarios, human drives operational vision
3. Completeness analysis skills (4.6): completeness-analysis, pbr-review, review-completeness
   └── Advisory/collaborative — AI brainstorms failure modes (strong suit), human owns judgment
   └── PBR as early experiment (novel, measurable, no published study)
4. System requirements skills (4.7): write-system-req, review-system-req, allocate-req
   └── Advisory mode — AI checks against rules and upstream artifacts
5. SW Requirements skills (4.8): write-req, review-req, decompose-req
   └── Advisory mode — human drives content, agent structures and checks
6. SW Architecture skills (4.9): develop-arch, review-arch, evaluate-arch
   └── Collaborative mode — agent proposes, human decides trade-offs
7. Resume C2-C4: detailed design skills (develop-dd, retrofit-dd, review-dd)
   └── Now informed by architecture→DD handoff contract
   └── Agent-driven with human validation
8. Framework skills across all layers (D1-D3 + higher layer equivalents)
9. Agents across all layers (E1-E5 + higher layer equivalents)
10. Orchestration (F1-F2)
```

### Phase 5: Traceability + Integration

```
1. Traceability: link type rules across all layers including new upper V artifacts (2A completion)
2. Wire full V-pairs: stakeholder needs → ConOps → system req → SW req → architecture → DD → code (end-to-end)
3. Legacy retrofit skills (4.11) — informed by full V knowledge
4. General documentation (3.12 — V-model overview, DRTDD, completeness analysis overview)
```

### Phase 6: Polish + Tooling

```
1. System test documentation + skills (3.10)
2. Review documentation + skills (3.11)
3. Plan schemas (1.2 plans)
4. Assurance level configuration (1.3)
5. Translation layer (1.4)
6. Scaffold tool (1.5)
7. Integration skills (4.12)
8. Orchestration pipelines (4.13)
9. Deterministic traceability engine (2B)
10. Web GUI (2C)
```

---

## Guiding Principles

1. **Documentation is the foundation.** Write the documentation first, derive everything else from it. If we can't explain it in documentation, we can't claim the AI skills will produce compliant output.
2. **Top-down documentation, then skills.** Understand the full information cascade (stakeholder needs → ConOps → completeness analysis → system req → SW req → architecture → detailed design → code) before building skills. Each documentation layer captures what it hands off down AND what feedback flows back up.
3. **Components are independent (SOLID).** No forced coupling. But designed to compose.
4. **Individual loops per layer.** Each V-model layer gets its own orchestration. Refactor to shared patterns only after building 3-4 layers.
5. **Human drives, AI executes, human verifies.** Not an autonomous pipeline. Mid-senior engineers orchestrating AI agents.
6. **Deterministic where possible.** Validation is a tool concern, not an agent concern.
7. **EARS is a skill preference, not a framework requirement.**
8. **Discuss before writing.** Explain approach, show visually, motivate, get approval.
9. **Model-tier aware.** Skills must work on cheapest viable tier (Tier 3 Fast). Test with baseline comparison. Vendor-neutral tiers: Tier 1 Reasoning / Tier 2 Workhorse / Tier 3 Fast.
10. **Incremental always.** Module-by-module. Layer-by-layer.
11. **Value is at the top of the V.** Agents already perform well at code/test level given clear input (eval data: +3% to +13% over baseline). The real gains come from helping produce quality designs and requirements — the upper V is where ambiguity, trade-offs, and human judgment live. Upper V skills are interactive advisors, not autonomous executors.
12. **V-model is engineering infrastructure, not just safety compliance.** Safety is the highest-rigor application, but the techniques (structured requirements, completeness analysis, traceability, formal verification) have value at any rigor level. The framework generalizes — safety-critical projects scale up, non-safety projects still benefit from the structure.
13. **Follow agentskills.io spec.** SKILL.md format, progressive disclosure, scripts for determinism.
14. **Use `/skill-creator` for development.** Draft, test, evaluate, iterate.

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
**Typical output:** One research document, 300-500 lines.

#### Category B: Craft Best Practices Research
**What:** The professional engineering knowledge needed to produce a high-quality artifact of this type, independent of any V-model or safety standard.
**Expect 3-5 research documents per artifact type.** Each 300-600 lines, with attributed claims and specific sources.

#### Category C: AI-Specific Research
**What:** How does AI-assisted development specifically affect this artifact type? Empirical data on AI quality. Common AI failure modes. Mitigation strategies.
**Typical output:** One research document, 200-400 lines.

### Research Quality Bar

- **Attributed claims.** Every factual claim cites a specific source. No "it is generally accepted that..." without a source.
- **Real sources.** Web-searchable, verifiable. Never present LLM training knowledge as research.
- **Cross-referenced.** Where multiple sources address the same topic, note agreement or disagreement.
- **Practical examples.** Key concepts illustrated with concrete code or artifact examples.
- **Honest about gaps.** If a claim's provenance is questionable, say so.

### Research Needs Per Upcoming Artifact

| Artifact (Section) | Cat A: Standards | Cat B: Craft | Cat C: AI |
|---|---|---|---|
| Unit Test (3.2) | Done | Done (5 docs) | Done (1 doc) |
| Detailed Design (3.3) | Done | Done (6 docs) | Done (1 doc) |
| Stakeholder Needs (3.4) | Done (Research 1+2) | Done (02a, 02b, 02d) | Done (Research 6: 06a) |
| ConOps (3.5) | Done (Research 1+2) | Done (02c, 05a) | Done (Research 6: 06a) |
| Completeness Analysis (3.6) | Done (Research 1+4) | Done (04a, 04b, 04c, 02b) | Done (Research 6: 06a, 06b) |
| System Requirements (3.7) | Done (Research 1+2+3) | Done (03a-03d, partially 05f) | Done (Research 6: 06a) |
| SW Requirements (3.8) | Done (Research 1+3) | Done (03a-03d) | Done (Research 6: 06a) |
| SW Architecture (3.9) | Done (Research 1+5) | Done (05a-05f) | Done (Research 6: 06b) |
| System Test (3.10) | Needed | ~3-4 docs | Needed |
| Review (3.11) | Needed | ~2-3 docs | Needed |

These estimates will be refined when each artifact's research phase begins.
