# Target Architecture

**Framework for AI-augmented, spec-driven software development with V-model rigor.** This document is the stable architectural reference. See `BACKLOG.md` for the execution plan and current state.

**Pivoted 2026-04-18.** Safety-specific framing, HW/SW split, and tier-based rigor were removed. The pre-pivot design record is preserved at `archive/pre-pivot-2026-04-18/` (status.md — session record; README.md — concept map old → new).

---

## 1. Vision

A framework that combines four independently usable components — Documentation, Templates & Schemas, Traceability, AI Skills — to let AI agents produce well-structured software that human engineers can fully understand and verify. The V-model contributes layered thinking, traceability, and design-test coupling; the framework removes the safety-specific baggage and optimizes the remaining rigor for AI authoring.

**End goal:** software built by AI under tight human design review, where the human leverage point is the spec layer rather than code review.

**Primary use cases:**
- **Greenfield development** — top-down through the scope tree.
- **Legacy retrofit** — bottom-up reverse engineering from existing code. Primary market entry point.

**Each component is independently usable.** Templates without skills, skills without the full pipeline, traceability for a team that authors docs by hand — all valid. Full value comes from combining all four.

---

## 2. Scope and Non-Goals

**In scope** (this document defines):
- Specification workflow (forward and retrofit modes).
- Artifact model (six types), scope tree, per-layer pattern.
- Quality Bar rigor mechanism.
- Traceability model.
- File shape, directory layout, ID scheme.
- Tools architecture and tool/skill split.
- Naming conventions.

**Explicitly deferred** (not in this pivot):
- **Build workflow** — how spec artifacts are decomposed into TDD tasks and executed. Own design session.
- **Human guards** — structural reintroduction of human review gates if uniform high rigor proves insufficient.
- **Rigor tiers** — may be reintroduced if human guards require them; not before.
- **Web GUI for traceability** — after the framework stabilises.
- **Plan schemas** — development plan, verification plan, CM, QA.

---

## 3. Core Principles

Ten load-bearing axioms. Every later section implements one or more.

1. **Documentation is the foundation.** Domain knowledge lives in docs first; schemas, templates, skills are derived. If we can't explain it in docs, we can't claim AI skills will produce compliant output.

2. **Spec Ambiguity Test.** Every specification artifact — at every layer — must be unambiguous enough that a junior engineer or low-mid-tier AI could act on it without guessing. This is the meta-gate for all specification work.

3. **Uniform high rigor.** No rigor tiers. Every artifact is authored at maximum rigor. Rigor is encoded per-artifact as a Quality Bar checklist (see §6), not as a configurable level.

4. **Workflow decoupling.** Specification, Build, and Retrofit are independent workflows, loosely coupled through artifacts only. No workflow calls, subscribes to, or has runtime knowledge of another.

5. **Tool/skill split.** Tools do mechanical deterministic work (no LLM). Skills do interpretive creative work (LLM-driven). Never skills for what tools can do; never tools for what needs judgment.

6. **Tools as independent products.** The framework bundles no tools. Tools live in their own repos, are compiled/distributed separately, and are invoked via stable CLI contracts. Each project declares its available tool subset.

7. **Retrofit never fabricates.** During retrofit, AI must never invent content that is not derivable from code, tests, or committed evidence. Human-only fields (rationale, context, alternatives) default to `recovery_status: unknown` unless a human supplies them.

8. **Human leverage at the spec level.** The human reviews specifications — where design intent lives — not code. If the spec is good enough, code review becomes optional. Upper layers have higher human involvement per change; lower layers have higher AI autonomy.

9. **Components are independent (SOLID).** No forced coupling between Documentation, Templates, Traceability, and Skills. Adopt incrementally.

10. **Tests derive from the layer's artifact, not from the code below.** V-model verification restored: unit tests derive from Detailed Design, integration tests from Architecture, system tests from Product Brief + Requirements at the root.

---

## 4. Four Components (overview)

```
DOCUMENTATION              TEMPLATES & SCHEMAS         TRACEABILITY              AI SKILLS
(source of truth)          (structural rigor)          (link + validate)         (interpretive work)

┌─────────────────┐       ┌──────────────────┐       ┌──────────────────┐       ┌──────────────────┐
│ 5-section HTML  │       │ Per-artifact     │       │ Forward links    │       │ Craft skills     │
│ per artifact    │       │ YAML schemas     │       │ embedded in      │       │ (standalone best │
│ (V-model ctx,   │       │                  │       │ artifacts        │       │ practices)       │
│  best practices,│       │ Quality Bar YAML │       │                  │       │                  │
│  anti-patterns, │       │ (canonical,      │       │ Reverse derived  │       │ Framework skills │
│  examples,      │       │  Phase 3)        │       │ by tooling       │       │ (orchestration,  │
│  Quality Bar)   │       │ Scaffolder       │       │                  │       │ template, trace, │
│                 │       │ templates        │       │ Validation rules │       │ retrofit)        │
│ Direct SW-eng   │       │                  │       │                  │       │                  │
│ English (§14)   │       │                  │       │                  │       │                  │
└─────────────────┘       └──────────────────┘       └──────────────────┘       └──────────────────┘

Any component usable standalone. Full value comes from combining all four.
```

**Independence statements:**
- Documentation alone → teaches the craft.
- Templates + docs → manual spec authoring.
- Traceability + templates → automated completeness checking.
- Craft skills + docs → AI-assisted spec authoring without framework-specific pieces.
- All four → full AI-augmented spec-driven development.

---

## 5. Artifact Model

### 5.1 Scope Tree

The system is modelled as a **tree of scopes**, discovered per-system, of variable depth. The framework uses abstract terms:

- **Root** — the whole product. Exactly one per system. Holds the Product Brief.
- **Branch** — any non-leaf scope. Has its own Requirements, Architecture, TestSpec, and children (more branches or leaves).
- **Leaf** — where subdivision stops earning its keep. Holds a Detailed Design, TestSpec, and ultimately code.

**Layer axis = decision kind** (not structural scope). At any depth, the same decision types apply: requirements, architecture, design, tests. Structural scope is a tagged property of each artifact, not a layer axis.

**Projects may tag scopes with domain-local names** (`level_name: "subsystem"`, `"component"`, `"module"`) for display. The framework still uses root/branch/leaf internally.

**Heuristics for when to subdivide further:**
- **Depth test** — if children interact in ways the parent's Architecture can't explain, go deeper.
- **Cognitive-load test** — a scope should fit in one R+A pair a reviewer can hold in their head.
- **Change-blast test** — if subdividing wouldn't let parts change independently, you've gone too deep.

**Deployment and wiring.** Architecture Composition (§5.3) is the authoritative spec for runtime pattern, wiring approach, and deployment intent. IaC artifacts (terraform/k8s/compose) implement that intent rather than sitting under a separate Detailed Design. Whether some imperative composition code (DI containers, middleware stacks, event-bus setup) warrants its own DD is deferred to the Build workflow design (see §8.4, §15). Integration and end-to-end coverage are carried by branch- and root-scope TestSpec tracing to Architecture Composition and child interfaces — not by a redundant DD layer above IaC.

### 5.2 Artifact Set

Six artifact types plus one cross-cutting:

| Type | Lives at | Job | Verified by |
|---|---|---|---|
| **Product Brief** | Root only | Anchor: why, for whom, what problem | Stakeholder review |
| **Requirements** | Non-leaf scopes | Testable behavior specifications | Acceptance / integration tests |
| **Architecture** | Non-leaf scopes | Decomposition + composition pattern | Integration tests |
| **ADR** | Cross-cutting | Justify individual load-bearing decisions | Review |
| **Detailed Design** | Leaf scopes | Internal logic, contracts, algorithms | Unit tests |
| **TestSpec** | Every scope | Test specification for this scope | (its own test code) |

### 5.3 Per-artifact structure

#### Product Brief (root only) — 7 sections

1. **Stakeholders** — who has the problem, who pays, who uses, who blocks.
2. **Problem** — concrete evidence of pain or opportunity.
3. **Desired Outcomes** — qualitative narrative of a better future state per stakeholder group.
4. **Operational Concept** — key scenarios, environments, integrations (ConOps folded in).
5. **Constraints** — regulatory, technical, financial, timeline bounds.
6. **Non-Goals** — what is explicitly not being solved.
7. **Success Criteria** — measurable indicators that outcomes were achieved.

#### Requirements (non-leaf scopes) — 6 sections

1. **Metadata** — scope, parent_scope, derived_from.
2. **Functional Requirements** — EARS preferred.
3. **Quality Attributes** — measurable non-functional targets.
4. **Interface Requirements** — external contracts.
5. **Data Requirements** — format, retention, privacy class.
6. **Inherited Constraints** — pointers to upstream constraints, including ADR references.

**Per requirement:**
- `id` — required (traceability).
- `statement` — required.
- `rationale` — required (can be terse; keeps human-in-loop anchor, enables trade-offs, preserves "why" in retrofit).
- `acceptance` — optional; required only when the statement alone leaves test conditions ambiguous.

#### Architecture (non-leaf scopes) — 6 sections, all mandatory

1. **Metadata** — scope, parent, derived_from, governing ADRs.
2. **Overview** — 2–3 paragraphs: what this scope is, what slice of the parent it realises, high-level approach.
3. **Structure Diagram** — Mermaid; children + connections.
4. **Decomposition** — per-child entries.
5. **Interfaces** — contracts between children.
6. **Composition** — runtime pattern (event-driven / request-response / pipeline / …), wiring approach (message bus topology, DI strategy, middleware stack), key sequence diagrams (Mermaid), and, at root scope only, deployment intent (environments, orchestration target, boundaries between runtime units). **Minimum bar: a developer reading this could decide what files/configs to create without re-deciding any patterns.**

**Per child** (in Decomposition): `id`, `purpose` (one line), `responsibilities` (architecture-level, not implementation), `allocates` (list of REQ-IDs).

**Per interface:** `name`, `from`, `to`, `protocol`, `contract` (reference).

**Mermaid is the default diagram format** (text-based, version-controllable, AI-readable). Embedded images are permitted where Mermaid is insufficient.

**ADRs are pulled out** as cross-cutting separate artifacts — not inlined in Architecture.

#### ADR (cross-cutting)

**Fields:**
- `id`, `title`, `status` (proposed / accepted / superseded), `date`.
- `scope_tags` — scopes this primarily applies to.
- `supersedes` / `superseded_by`.
- `context` — situation addressed.
- `decision` — what was chosen.
- `alternatives_considered` — at least two, with reasons for rejection.
- `rationale`.
- `consequences` — **with a mandatory Reversibility sub-prompt:**
  > *Is this decision reversible? If yes: state the rollback path. If no: state the recovery plan and name who must sign off before implementation.*
- `affected_scopes` (optional, when different from scope_tags).

ADRs live in a flat `/specs/adrs/` directory, cross-cutting across scopes.

**Technical-selection propagation rule (hybrid):** when an ADR creates downstream obligations, the author authors a new requirement at the ADR's own layer *if the consequence is testable at that layer*, and propagates via `governing_adrs` reference *if the consequence only bounds choices at child layers*. Both can happen for one ADR.

**Completeness rule:** every ADR consequence must be either satisfied by a requirement at the ADR's scope or referenced as a constraint by a child artifact.

#### Detailed Design (leaf scopes) — 7 sections

1. **Metadata** — scope, parent_scope, parent_architecture, derived_from, governing_adrs.
2. **Overview** — 2–3 paragraphs.
3. **Public Interface** — every externally visible function/class/type with contracts.
4. **Data Structures** — internal data with invariants, ownership, lifetime.
5. **Algorithms** — pseudocode for the trickiest, prose for clearer ones, decision tables for complex conditionals.
6. **State** (if applicable) — state machines (Mermaid), persistent state, side effects.
7. **Error Handling** — what can occur, how detected, how propagated.

*(The pre-pivot Section 8 "Test Strategy" was removed; DDs now reference the leaf's TestSpec via traceability.)*

**Per public-interface entry:** `name`, `signature` (language-neutral), `description` (1–2 sentences), `preconditions`, `postconditions`, `errors` (what can be raised, when), `side_effects`, `complexity_notes` (if relevant).

**Per data structure:** `name`, `description`, `fields`, `invariants`.

**Success criteria for a DD:**
- **Junior-dev-implementable** — someone unfamiliar can produce correct code from the design alone.
- **Language-portable** — equivalent code in Java, Python, Go.
- **Test-derivable** — unit tests can be written from the design before code exists.

#### TestSpec (every scope)

**Envelope:**
```yaml
id: TESTSPEC-<scope>-<level>
scope: <scope-path>
parent_scope: <or null at root>
level: system | integration | unit
verifies: [list of spec artifact IDs]
derived_from: <spec artifact this is written from>
governing_adrs: [optional]
```

**Structure:** flat list of cases, each with an optional `suite` tag (not nested). Per case:
```yaml
id: TC-<num>
title: <one line>
suite: <optional tag>
type: functional | boundary | error | fault-injection | property | state-transition | contract | performance | security | accessibility | error-guessing
verifies: [IDs from this layer's spec — MANDATORY non-empty]
preconditions: <state assumed true>
inputs: <stimuli>
steps: <ordered actions; omit if trivial>
expected: <pass criteria>
notes: <optional>
```

**Per-layer emphasis** (same envelope, different weight):
- **Root** — system / e2e tests; heavy `steps`, `preconditions`, `expected`; scenarios map to Product Brief outcomes.
- **Branch** — integration tests; `preconditions` carry fixtures and test doubles; `verifies` crosses children.
- **Leaf** — unit tests; `inputs` + `expected`; `verifies` → DD function / contract; `steps` often trivial.

**Mandatory non-empty `verifies`** is the anti-orphan rule: tests without traceable intent are either misplaced or testing unspecified behaviour.

### 5.4 File shape, directory layout, IDs

**File shape** — single-file Markdown per artifact:
- YAML front-matter for metadata + outgoing links.
- Body: prose sections + embedded YAML blocks for repeated structured items (per-req, per-child, per-test-case, per-function) + Mermaid for diagrams.

**Directory layout** — scope tree mirrors directory tree, under a configurable `/specs/` root:

```
/specs/
  product_brief.md            # root scope
  requirements.md             # root scope
  architecture.md             # root scope
  testspec.md                 # root scope
  /adrs/                      # flat, cross-cutting
    adr-001-use-postgres.md
  /app/                       # branch scope
    requirements.md
    architecture.md
    testspec.md
    /checkout/                # leaf scope
      detailed_design.md
      testspec.md
```

Root-scope artifacts live directly at `/specs/`. "Root" is implicit — not a directory name. ADRs flat in `/specs/adrs/`.

**Filenames** — snake_case canonical per artifact type: `product_brief.md`, `requirements.md`, `architecture.md`, `detailed_design.md`, `testspec.md`, `adr-NNN-{slug}.md`. **Scope directories** use kebab-case.

**IDs** — prefix-scope-sequence, stable across renames; root scope implicit:

| Artifact | Format | Example |
|---|---|---|
| Product Brief | `PB` | `PB` |
| Requirement | `REQ-{scope}-{seq}` | `REQ-app-checkout-042`; at root: `REQ-001` |
| Architecture | `ARCH-{scope}` | `ARCH-app-checkout`; at root: `ARCH` |
| Detailed Design | `DD-{scope}-{name}` | `DD-app-checkout-discount-calc` |
| TestSpec | `TS-{scope}` | `TS-app-checkout`; at root: `TS` |
| Test Case | `TC-{testspec-id}-{seq}` | `TC-TS-app-checkout-007` |
| ADR | `ADR-{seq}-{slug}` | `ADR-012-use-postgres` |

Traceability links reference IDs, not paths.

---

## 6. Rigor Model — Quality Bar (no tiers)

**No tier system.** No T0/T1/T2/T3, no DAL, no ASIL. Every artifact is authored at uniform highest rigor. Tier-based thinking from safety standards was rejected: the rigor-cost curve is flat for AI authoring, and tier systems create escape valves that defeat themselves.

**Rigor is encoded per-artifact in two layers:**

### Structural rigor (mechanical)
Lives in the artifact schema. Mandatory sections, required fields, required traceability links. Mechanically validatable: `vmodel validate <artifact>` returns pass/fail.

### Semantic rigor (Quality Bar checklist)
A concrete Yes/No checklist grouped by concern, subject to the **Spec Ambiguity Test** as a meta-test. Lives as the **5th section** in each `docs/guide/artifacts/<type>.html` page (alongside the other four: V-model context, best practices, anti-patterns, examples). Extracted as canonical YAML in Phase 3 alongside other schemas, consumed by templates, authoring skills, and review skills — single source of truth.

**Quality Bar is additive per layer.** Upstream failure blocks downstream approval. A leaf DD cannot pass review if its parent Architecture Quality Bar has unresolved items. Tooling surfaces the chain rather than allowing a reviewer to rubber-stamp a leaf on top of a broken parent.

**No relaxed variants.** There is no "lightweight mode" for small projects or prototypes. If the rigor is not needed, the framework is not the right tool.

**Reversibility** is captured as a mandatory sub-prompt inside ADR Consequences (see §5.3 ADR), not as a separate flag or field. Model tier (junior engineer / low-mid AI) is a **complexity heuristic** used when judging artifact quality — never a property stored on an artifact.

**Per-artifact rigor dimensions** (HTML checklists authored in Phase 2, YAML extraction in Phase 3 — see BACKLOG):

| Artifact | Key dimensions |
|---|---|
| Product Brief | Stakeholder completeness, problem evidence, outcome specificity, non-goals explicit, success criteria measurable |
| Requirements | EARS where apt, rationale present, QAs measurable, interface contracts, constraint inheritance traced |
| Architecture | Composition section complete (runtime patterns have rationale; deployment intent resolves to concrete IaC artifacts; runtime-unit boundaries have integration-test targets), every child allocated, every req allocated, ADRs linked, interfaces contracted |
| ADR | Context specific, ≥ 2 alternatives, reversibility sub-prompt answered, affected scopes listed |
| Detailed Design | Junior-implementable, contracts on every public function, state machines explicit, error handling complete, language-portable |
| TestSpec | Derivation strategies applied (requirement-based, equivalence class, boundary, state transitions, error paths, fault injection), coverage targets declared, every spec element has ≥ 1 test |

---

## 7. Traceability Model

### Canonical link types

| Link | Meaning | Source | Target |
|---|---|---|---|
| `derived_from` | Refinement / breakdown lineage | Spec artifact | Parent spec at layer above |
| `allocates` | Parent assigns responsibility | Architecture child entry | List of REQ-IDs |
| `verifies` | Test case / TestSpec verifies spec element | TestSpec case | Req / Architecture child / DD element |
| `governing_adrs` | Artifact subject to ADR | Any spec artifact | List of ADR IDs |
| `supersedes` / `superseded_by` | ADR lineage | ADR | ADR |
| `scope_tags` | ADR's primary scopes | ADR | Scope list |
| `affected_scopes` | ADR's downstream consequence scopes | ADR | Scope list (optional) |

**Reverse relationships** (`realized_by`, `verified_by`, `derives`) are **computed** by tooling, not stored.

### Storage

**Embedded** — each artifact carries its outgoing links in front-matter. A tooling-derived index provides fast queries. Embedded keeps artifacts self-contained (critical for the Spec Ambiguity Test — a spec that requires a separate lookup to be understood fails it) and avoids the DOORS-era drift problem where a central table and documents diverge.

### Granularity

**Artifact-level default; field-level allowed only for `verifies`.** Test cases legitimately target specific preconditions / postconditions / error paths. Other link types don't earn field-level precision.

### Validation rules

**Reference integrity**
- Every `derived_from` / `allocates` / `verifies` / `governing_adrs` / `supersedes` target resolves to an existing artifact.
- Every scope reference resolves in the agreed scope tree.

**Completeness** (the teeth)
- Every requirement at layer N is allocated to at least one child.
- Every Architecture child allocates at least one requirement (no orphan components).
- Every requirement / DD contract has at least one verifying test case.
- Every test case has non-empty `verifies`.
- Every leaf has a DD and a TestSpec.
- Every non-leaf has Requirements, Architecture, TestSpec.
- Every ADR consequence is either satisfied by a requirement at the ADR's scope or referenced by a child artifact.

**Cycles**
- No circular `derived_from`; no circular `supersedes`.

**Retrofit discipline**
- No `reconstructed` status on human-only fields.

**Quality Bar cascade**
- Upstream Quality Bar failure blocks downstream approval.

### Forward-compatibility for Build-side artifacts

The link model reserves `realizes` for future Build-workflow artifacts: code file → DD function, test file → TestSpec case, deployment config → Architecture Composition (at root). Spec-side artifact shapes do not change. Build-side artifacts add themselves to the graph via `realizes` when they exist.

---

## 8. Workflows

All workflows in the framework follow a single principle:

> **Workflows are independently designed and loosely coupled through artifacts only.** No workflow calls, subscribes to, or has runtime knowledge of another.

Specification, Build, and Retrofit are independent. Cross-workflow status (e.g., "is this spec implemented?") is queryable from the traceability graph as a derived property — **pull, never push**.

### 8.1 Specification workflow — greenfield

Top-down from root scope.

**Per-layer unified pattern:**

```
Input: parent allocation (or Product Brief at root)
        │
        ▼
   SPECIFY (what)
   ├── Non-leaf: Requirements + Architecture
   ├── Leaf:     Detailed Design
   ├── Any:      ADRs (technical picks → constraints flow down)
   └── Any:      TestSpec (authored here, before any code)
        │
        ▼
   (Build workflow — separately, elsewhere)
   ├── Root:    deploy orchestration + e2e / system tests
   ├── Branch:  wiring / glue code   + integration tests
   └── Leaf:    code                 + unit tests
        │
        ▼
   Non-leaf: allocate reqs to children → recurse
```

**Completion criterion — "specification complete" per scope:**
- Every spec artifact passes its Quality Bar (structural + semantic).
- Traceability validation passes for the scope.
- Every TestSpec case has non-empty `verifies`.
- No `recovery_status: unknown` on any blocking field.

Anyone can query this. The Specification workflow emits it *at nobody*.

**Human-gate gradient** — higher layers drive more human involvement per change; lower layers have higher AI autonomy. This is a property of skills and flow, **not** encoded in schemas.

### 8.2 Specification workflow — retrofit mode

Retrofit is **not a separate workflow**. It is the Specification workflow run bottom-up with a topology-discovery phase prepended. Same artifact set, same Quality Bar, same Spec Ambiguity Test.

**Four phases:**

1. **Topology discovery** — dependency / package / module analysis → candidate scope tree. AI proposes, human validates. Output: an agreed scope tree before any artifact authoring.
2. **Leaf recovery** (per leaf) — from code + existing tests: draft DD, draft TestSpec, draft leaf ADRs for visible technology. Human adds intent (why this leaf exists, what invariants must hold). Spec Ambiguity Test is the gate.
3. **Branch + root recovery** (bottom-up) — draft Architecture from child DDs / Architectures (composition pattern, interfaces, allocations); draft Requirements from child contracts + externally supplied QAs; draft TestSpec (integration at branch, system at root); draft ADRs for visible technology. Human supplies missing rationale.
4. **Product Brief** (stakeholder-driven, optional) — requires conversations with product owners / users. Deferred when stakeholders are inaccessible; the system runs on everything below without it.

**Retrofit is read-only on code.** It produces specs + a gap report; code modifications are a separate follow-up activity.

**`recovery_status` field** — split by field type:

**Code-derivable fields** (evidence exists in code, tests, git, schemas, committed comments):
- Allowed states: `verified` | `reconstructed` | `unknown`.

**Human-only fields** (no code-derivable source):
- ADR `context` / `alternatives_considered` / `rationale` / anticipated `consequences`.
- Requirements `rationale`.
- Product Brief — all sections.
- DD *intent* (why this leaf exists, invariants).
- Allowed states: `verified` | `unknown` only. **No `reconstructed` state.**

The retrofit skill **must refuse** to populate human-only fields with AI inference. Enforced at the skill / tool level, never left to agent discretion. **Fabrication of rationale is a hard rule violation, not a quality issue.**

**Primary purpose** of retrofit: establish an honest floor of documentation from which forward development can proceed. **Secondary:** surface gaps. A retrofit output with many `unknown` fields is **not** a partial failure — it is the correct outcome when rationale is lost.

**Gap report** (first-class deliverable, alongside artifacts):

| Gap | Meaning | Framework response |
|---|---|---|
| Unintentioned leaf | Leaf with no discoverable purpose after human review | Candidate for removal / refactoring |
| Test coverage gap | DD contract has no linked test | Backlog item for test writing |
| Ambiguous spec | Artifact fails Spec Ambiguity Test even after human review | Refactor code for clarity, or document the gap |
| Lost rationale | Requirement derivable but rationale absent | `recovery_status: unknown` → stakeholder follow-up |
| Structural drift | Retrofit-derived architecture ≠ existing documented architecture | Flag; reconciliation required |
| ADR without decision record | Technology visible in code but context lost | ADR drafted with `recovery_status: unknown` on context / alternatives |

### 8.3 Update mode — entry points and impact propagation

**Principle: enter at the highest layer that must change.** A change enters at layer N if N is where something genuinely new (not derivable from existing artifacts) gets introduced. If a code fix implies the spec was wrong, the spec is the entry — not the code.

**Entry-layer decision table:**

| Change source | Entry layer |
|---|---|
| New stakeholder, outcome, scope expansion | Product Brief |
| New behavior under an existing outcome | Requirements (at the scope where it applies) |
| Re-allocation of existing behavior to a different component | Architecture |
| Technology swap / pattern change | ADR + Architecture Composition |
| Algorithm or state-machine change | Detailed Design |
| Bug fix where spec was correct, code was wrong | Implementation (Build workflow) |
| Bug fix where spec was ambiguous or wrong | The layer whose spec was wrong |

**Candidate-set propagation:** tooling walks traceability forward from the entry-layer change and produces a **candidate set** of artifacts that may need updates. The change is incomplete until every candidate is either updated or explicitly closed as "no change needed" with a brief reason.

**Upward propagation is legitimate.** If a gap is discovered downstream, the change's entry layer retroactively rises. The impact set recomputes from the higher entry.

**No Change Set artifact** (for now). Change tracking is process / tooling territory; every team has its own ticketing.

### 8.4 Build workflow (deferred)

The Build workflow consumes specifications and produces code + tests. It will be designed in its own later session. Points already decided:

- Build has no runtime coupling to Specification — artifacts are the sole contract.
- Build is free to decide what chunk to pick up based on its own context budget and decomposition strategy.
- Build-side artifacts add `realizes` links to the traceability graph; Spec artifacts are oblivious to them.
- When Build finds a spec insufficient to implement, the change enters Specification workflow at the appropriate layer (per §8.3).
- **Build-side rule** (to be encoded when Build is designed): do not hunt external channels for product / design intent. If intent isn't in the spec, the Spec Ambiguity Test failed and the spec updates.
- **Wiring / IaC spec question** (deferred from §5.1): Architecture Composition is the current authoritative spec for deployment and wiring. Build will decide whether imperative wiring code (DI containers, middleware stacks, event-bus setup) warrants its own Detailed Design, or whether Composition + integration TestSpec is sufficient. Declarative IaC (terraform, k8s, compose) is already decided — no separate DD layer above it.

---

## 9. ADR Model

ADRs are **cross-cutting** first-class artifacts — not inlined in Architecture.

**Fields** (canonical list in §5.3): id, title, status, date, scope_tags, supersedes/superseded_by, context, decision, alternatives_considered, rationale, consequences (with mandatory Reversibility sub-prompt), affected_scopes (optional).

**Status lifecycle:** `proposed` → `accepted` → `superseded`. ADRs are immutable once accepted; supersession is by new ADR + back-link, never in-place edit.

**Authoring pattern:**
- **Default** — authored *during* Architecture authoring as the composition exposes a decision. Architecture references `governing_adrs: [ADR-042]`.
- **Alternative** — ADR can predate Architecture when an external / org-standard decision already binds (e.g., "org standard is Postgres"). The Architecture author still references it via `governing_adrs`.
- **Forward-only during retrofit** — when rationale / alternatives are lost, capture `recovery_status: unknown`. Never fabricate.

**Propagation rule** (technical selections, hybrid — see §5.3): consequences testable at the ADR's own layer become new requirements there; consequences that only bound child choices propagate via `governing_adrs` reference.

**Completeness check:** every ADR consequence must be either (a) satisfied by a requirement at the ADR's scope, or (b) referenced as a constraint by a child artifact. Tooling enforces this.

---

## 10. Tools Architecture

**Tools are independent products.** Each lives in its own repo, has its own build / release cycle, and is distributed as a compiled binary or installable package. The framework repo bundles no tool source code.

**Framework's role:** orchestrate via stable **CLI contracts**. Each project has a **project config** declaring its available tools — a subset of what exists in the ecosystem. Framework skills and orchestration read this config and invoke tools by name.

**Tool universe** (for this framework's use cases — projects pick subsets):

**Purpose-built:**
- Artifact parser (`.md` + YAML + Mermaid → structured data).
- Schema validator (structural rigor per artifact type).
- Traceability validator (link integrity, completeness, cycles).
- Quality Bar structural runner.
- Graph builder (derived traceability graph).
- Query engine (coverage, impact, candidate sets, what-verifies-what).
- Scaffolder (generate skeleton `.md` from artifact type).
- Renderer (artifacts → HTML / static docs).
- Topology discovery (scope tree from code).
- Gap report aggregator (retrofit gaps).

**Pre-existing general tools:**
- git, graphviz, jq, ripgrep, linters, compilers, test runners — whatever the project already uses.

**Skills (LLM-driven, interpretive — distinct from tools):**
- Leaf DD draft from code (retrofit).
- Quality Bar semantic review.
- Spec Ambiguity Test evaluation.
- Per-artifact authoring skills.
- Per-artifact review skills.

### Tool / skill split (design principle)

- **Tools** perform mechanical, deterministic work. Same input → same output. No LLM involved.
- **Skills** perform interpretive, creative work. Draw on language models.
- Never have skills do what tools can do deterministically.
- Never have tools attempt what needs judgment.

### Output discipline (applies to every tool)

- `--format json` (structured, AI-default).
- `--format text` (human-default: tables, color, formatting).
- Default is **TTY-aware** — terminal gets `text`, pipe gets `json`.
- Errors are **actionable** — report file, rule, and what to fix. Never just "validation failed."

### Integration interface

**CLI subprocess calls**, not in-process library. Skills invoke tools via subprocess and consume structured output. Language-agnostic; clean contract; CI-friendly; matches the AI-skill abstraction (issue commands, don't share process state).

### Dogfooding

Purpose-built tools are the **first customers** of this Specification workflow. Each tool gets its own Product Brief → Requirements → Architecture → DD → TestSpec before Build. Tech choices per tool come from that tool's own ADRs — the framework stays tech-agnostic about its tools.

---

## 11. Skills Architecture

Three-layer model, preserved from the pre-pivot design (to be refactored in Phase 5 against the new artifact set — see BACKLOG):

- **Layer 1 — Skills** (atomic units). Craft skills (standalone best practices, derived from documentation; framework-independent). Framework skills (VModelWorkflow-specific: template formats, traceability link creation, orchestration glue).
- **Layer 2 — Agents** (specialised execution environments). Subagents with isolated contexts, scoped tool access, structured I/O contracts. Compose Layer 1 skills into producer and reviewer roles.
- **Layer 3 — Orchestration** (pipeline control). Research / plan session, task decomposer, pipeline controller. Document-based handoffs via YAML on disk — any session can crash and restart from the last written artifact.

**Document-based handoffs**, **`vmodel-skill-*` / `vmodel-agent-*` naming**, **model-tier-aware** skill design — all preserved.

> **Stale reference note.** The detailed skill-architecture HTML at `docs/guide/skills-architecture.html` is **pre-pivot** and does not reflect the new 6-artifact model. It will be rewritten in Phase 5 (see BACKLOG §3.5). Until then, treat the HTML as historical record; this §11 plus memory entries are authoritative.

---

## 12. Human-Agent Interaction Model

Human drives at the strategic level, AI executes at the tactical level, human verifies at the quality gate. **Not an autonomous pipeline with human checkpoints.**

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
                                                                    to next scope ↓
```

**The Spec Ambiguity Test is the universal floor** (§3 #2): no artifact leaves Research & Plan without satisfying it.

**Agent autonomy varies by artifact position:**

| Layer / Artifact | Agent Role | Human Role | Agent Autonomy |
|---|---|---|---|
| Product Brief | Proposes structure, checks gaps, drafts candidate outcomes from interviews | Drives all content (only humans know stakeholders and intent) | Low |
| Requirements (non-leaf) | Structures, checks completeness, suggests EARS patterns, flags ambiguity | Drives content; validates against parent allocation | Medium |
| Architecture (non-leaf) | Proposes decomposition, checks consistency, drafts Composition | Decides trade-offs; decides technology picks → ADRs | Medium |
| ADR | Drafts from discussion, enforces reversibility prompt | Owns the decision | Medium drafting / Low decision |
| Detailed Design (leaf) | Heavy lifting on specification | Validates trade-offs, approves | Medium-High |
| TestSpec | Derives cases from the layer's spec using derivation strategies | Reviews coverage against intent | Medium-High |
| (Build workflow — code + tests) | Autonomous executor given a complete spec | Reviews output | High |

**Upper layers are interactive advisors; lower layers are autonomous executors.** Skill design must match the autonomy level.

---

## 13. Use Cases

### 13.1 Greenfield development (top-down)

Start from Product Brief at root scope, work down. Each layer follows research/plan → implement → review with traceability built as artifacts are created. Scopes decompose into subtrees; each scope goes through Specification → Build in sequence.

### 13.2 Legacy retrofit (bottom-up, primary market entry)

Start from existing code; reverse-engineer artifacts upward using Retrofit mode of the Specification workflow (§8.2). Topology discovery produces the scope tree; leaf recovery produces DDs; branch + root recovery produces Architecture, Requirements, ADRs; optional Product Brief recovery requires stakeholder engagement.

The primary value: an honest floor of documentation for forward development, plus a gap report that drives remediation priorities.

---

## 14. Domain Translation — deferred

The pre-pivot design positioned generic V-model terminology internally with a **translation plugin system** (`docs/guide/domains/*.json`) mapping to standards-specific vocabulary (DO-178C, ASPICE, ISO 26262, IEC 62304, …) at runtime.

**Current status (as of 2026-04-22):** the plugin mechanism is removed and the JSON files are archived. Phase 2 content is authored in direct software-engineering English across the landing page and the six artifact pages.

1. **Phase 2 content is authored in direct software-engineering English.** No generic/standards translation layer is applied to the text.
2. **Rationale for removal.** Running the plugin machinery while simultaneously establishing voice, depth, and rigor in new content creates drift risk: the generic surface and the standards-specific rendering can diverge in both directions. Separating the concerns — get the content right first, translation later — protected Phase 2 quality.
3. **Archival status.** `docs/guide/domains/*.json` has been moved to `archive/pre-pivot-2026-04-18/domains/`. `js/domain.js`, the `.domain-switcher` CSS, and all `data-term` hooks have been removed from the live `docs/guide/`.
4. **Reintroduction horizon.** The mechanism (or a simpler successor — e.g., manual domain-specific page variants) may be reintroduced when a concrete domain-translation need surfaces. See §15.

---

## 15. Open Architectural Questions

Genuine architectural uncertainties to revisit as we learn more. (Execution open questions live in BACKLOG §6.)

1. **Rigor tiers — may we need them later?** Current decision: no tiers (§6). Revisit if human guards are reintroduced or if real use shows the uniform bar is mis-priced in some dimension.
2. **Human guards — what they look like.** Deferred from §2. When the need arises (e.g., regulated domain adoption), a first-class review-gate concept may re-enter the framework.
3. **Scope-tree depth heuristics — can they be codified?** §5.1 names depth/cognitive-load/change-blast tests as heuristics. Do these admit a more formal statement (e.g., complexity metrics), or do they stay as author judgment?
4. **Build workflow contract.** Explicitly deferred. When designed: how Build chunks a scope bundle, how `realizes` links are emitted, whether Build publishes a readable structure for coverage queries.
5. **Retrofit completeness threshold.** How many `unknown` fields make a retrofit "not useful" vs "partial but usable"? No principled answer today; real retrofit runs will inform.
6. **Product Brief for micro-projects.** The 7-section shape fits real products. For internal tools or one-week prototypes a "Product Brief Lite" may be useful — out of scope until we see the need.
7. **Domain translation reintroduction.** Whether, when, and how to reinstate a domain-vocabulary translation mechanism once Phase 2 content is stable. Options include the pre-pivot JSON-plugin runtime, manual per-domain page variants, or abandoning translation in favor of a single-voice corpus. Deferred until we have stable content to evaluate against.
8. **Imperative wiring spec layer.** Does imperative composition code (DI containers, middleware stacks, event-bus setup) warrant its own Detailed Design, or is Architecture Composition + integration TestSpec sufficient? Declarative IaC (terraform, k8s, compose) is already decided — no separate DD; Architecture Composition is the intent spec, IaC is the implementation. Deferred to Build workflow design.
