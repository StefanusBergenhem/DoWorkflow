---
name: vmodel-skill-author-architecture
description: Author a software architecture specification document (Markdown with YAML front-matter, embedded YAML blocks, Mermaid diagrams) for one non-leaf scope. Use when decomposing a scope into children with responsibilities and interface contracts, specifying a runtime composition pattern, allocating quality attributes to components or interfaces, authoring deployment intent at root scope, or retrofitting from existing code with recovery_status discipline. Produces one Markdown file with Structure Diagram, per-child Decomposition entries, Interface entries with Design-by-Contract clauses (preconditions, postconditions, invariants, typed errors), and a Composition section. Refuses Detailed Design content inside Architecture, fabricated rationale on retrofit, and shipping without a non-trivial Composition section. Triggers — write architecture, draft architecture.md, decompose this scope, specify component interfaces, design composition, add deployment intent, retrofit architecture from code.
type: skill
---

# Author architecture specification document

This skill produces a single Markdown file: an Architecture specification for one non-leaf scope. The document carries a Structure Diagram, per-child Decomposition entries, Interface entries with Design-by-Contract contracts, and a non-trivial Composition section. The skill is authored under hard quality gates that prevent the most common architecture-authoring failures — Detailed Design smuggling, fabricated retrofit rationale, and shipping without a real Composition section.

The skill is self-contained. Every reference, template, anti-pattern catalog, and quality-bar checklist it needs is bundled in the `references/` and `templates/` directories. No external lookups are needed.

## When to use

Activate this skill when the user asks to:

- Write or draft an Architecture document for a non-leaf scope (root or branch)
- Decompose a scope into children with responsibilities and interface contracts
- Specify a runtime composition pattern (request-response, event-driven, hexagonal, saga, pipeline, …)
- Allocate quality attributes (latency, throughput, availability) to components or interfaces
- Author deployment intent at root scope (environments, orchestration target, runtime-unit boundaries)
- Retrofit an Architecture from existing code with `recovery_status` discipline

Do **not** activate this skill for:

- Authoring Requirements, Detailed Design, ADR, or TestSpec — those are separate authoring skills' jobs
- Reviewing or auditing an existing Architecture document — that is the matched review skill's job
- Writing tests, code, or per-child algorithms — those are downstream artifacts

## Inputs

Expected upstream context (ask if missing):

- **Scope identifier** — name and path of the non-leaf scope being architected
- **Parent Requirements artifact** — the Requirements for this scope (or, at root, the root Requirements)
- **Parent's allocation set** — the requirements this scope inherits from its ancestor (root scopes: the same as own Requirements)
- **Governing ADRs** — the cross-cutting decisions that bound choices at this scope
- **Recovery posture** — greenfield (omit `recovery_status`) or retrofit (declare `recovery_status` and supply source-code references)
- **Mode flags** — root vs branch (root activates deployment intent), greenfield vs retrofit

If any of the four primary inputs is unavailable, **HALT** (see HALT condition #1) and ask the user. Do not invent inputs.

## Output

A single Markdown file using the structure in `templates/architecture.md.tmpl`. The file has YAML front-matter, an Overview, a Structure Diagram (Mermaid), Decomposition (one YAML entry per child), Interfaces (one YAML entry per cross-child or external interface), and a Composition section.

Default output filename: `<scope>/architecture.md`. If the user has a scope-tree convention (e.g. root at `<root>/architecture.md`, branches at `<scope>/architecture.md`), follow it.

## Authoring procedure

Author the document in this order. Each step has its own reference file with the craft rules. Treat the references as the source of truth for craft; this section is a checklist.

### Step 1 — Decompose the scope

Decide the children. Apply information-hiding (Parnas), cohesion+coupling, bounded contexts, context-mapping patterns, Conway-inverse, and the depth/cognitive-load/change-blast trio. Every child gets one sentence of purpose (no conjunctions), at most three architectural-level responsibilities, and an explicit `allocates` list of parent requirement IDs. Every parent-allocated requirement must land in at least one child.

→ See `references/decomposition-discipline.md`
→ Template: `templates/decomposition-entry.yaml.tmpl`

### Step 2 — Specify interfaces

For every cross-child and externally callable interface, write a Design-by-Contract entry: preconditions, postconditions (on success / precondition-failure / downstream-failure), invariants, typed error enum, quality attributes, rationale, version, deprecation policy. Apply Interface Segregation — narrow per responsibility, no god-interfaces.

→ See `references/interface-contracts.md`
→ Template: `templates/interface-entry.yaml.tmpl`

### Step 3 — Choose protocol family + sync/async

Per interface, pick a protocol family (REST / gRPC / GraphQL / events) and the sync/async axis. State the rationale at interface level (or `governing_adrs`). Defaulting to async because it is trendy is how you get a distributed system with synchronous semantics and asynchronous blast radius.

→ See `references/composition-patterns.md` (first half)

### Step 4 — Choose composition pattern + wiring

Pick a named runtime pattern (request-response / event-driven / event-sourcing / saga / pipeline / layered / hexagonal / clean / microservices-vs-monolith / serverless). State three wiring concerns: DI strategy, middleware stack ordering, message-bus topology (where applicable). Draw a sequence diagram for the happy path; draw one or two for the critical failure paths.

→ See `references/composition-patterns.md` (second half)
→ Template: `templates/sequence-diagram.mmd.tmpl`

### Step 5 — Specify data + persistence

Name the database-per-service vs shared choice, the consistency model per data path, read-replica / CQRS choices where applicable, and multi-tenancy isolation tier per data category. Eventual consistency without a reconciliation story is a bug factory.

→ See `references/data-and-persistence.md`

### Step 6 — Apply resilience patterns

Name the bulkhead partitions, circuit-breaker placement, retry policy (backoff + jitter + idempotency), graceful-degradation vs fallback choice per non-essential dependency, failure domains, and named independence properties for any redundancy claim.

→ See `references/resilience-patterns.md`

### Step 7 — Specify observability + security at boundaries

Name the telemetry emergence points (logs/metrics/traces, common context fields, sampling). Draw trust zones explicitly. State authn/authz per externally callable interface with the evaluation layer named (gateway / middleware / handler). Specify secrets flow.

→ See `references/observability-and-security.md`

### Step 8 — Author deployment intent (root scope only)

Enumerate environments. Name the orchestration target. Map components to runtime units (deployment / scaling / failure boundaries). State the cost model. Reference IaC paths for implementation; do not re-specify what IaC already declares. Skip this step at branch scope.

→ See `references/deployment-intent.md`
→ Template: `templates/deployment-diagram.mmd.tmpl`

### Step 9 — Specify evolution + fitness functions

Name the architecture-as-hypothesis bet for this scope. Specify fitness functions for load-bearing properties (dependency direction, latency budget, module size + coupling, security posture). Where a strangler-fig migration is in play, name the routing mechanism, the retirement criteria, and the rollback point.

→ See `references/evolution-and-fitness-functions.md`

### Step 10 — Extract load-bearing decisions to ADR stubs

Decisions that are load-bearing AND cross-cutting AND hard-to-reverse are ADR material. Do not inline them in Architecture; emit `[NEEDS-ADR: <decision> — extract before finalising]` markers and reference them via `governing_adrs` once authored.

→ See `references/adr-extraction-cues.md`
→ Template: `templates/governing-adr-reference.yaml.tmpl`

### Step 11 — Apply retrofit posture (retrofit only)

Mark observed structure with `recovery_status: reconstructed` and cite evidence (file/line/commit/schema). Mark interpretation `verified` (with human source) or `unknown`. Mark rationale `verified` or `unknown` ONLY — never `reconstructed` (this is hard refusal A). Pair every `unknown` with a follow-up owner. Populate the gap report.

→ See `references/retrofit-discipline.md`

### Step 12 — Anti-pattern self-check

Sweep the document against the ten anti-patterns: six universal (big ball of mud, distributed monolith, god component, premature decomposition, stale documentation, cyclic dependencies) and four AI-era (laundered architecture, fabricated decomposition rationale, ad-hoc composition, missing composition spec).

→ See `references/anti-patterns.md`

### Step 13 — Run Quality Bar checklist + Spec Ambiguity Test

Run the Yes/No checklist. Items that cannot be answered Yes are flagged inline in the output, not silently passed. Apply the Spec Ambiguity Test as the meta-gate (override): a junior engineer or mid-tier AI must be able to derive defensible Detailed Designs and a TestSpec from this artifact alone (plus governing ADRs and parent Requirements), without asking clarifying questions.

→ See `references/quality-bar-checklist.md`

## Hard refusals (the four non-negotiables)

**A — Honest retrofit posture.** Refuse to:
- Mark rationale fields with `recovery_status: reconstructed` (rationale is human-only — `verified` or `unknown` only).
- Generate generic-principle rationale ("follows DDD", "single-responsibility", "clean separation"). Example refusal: a retrofit asks for rationale on a cart boundary; the user has no preserved decision record. Output: `rationale: { status: unknown, note: "no preserved decision record; follow up with @owner" }` — never an invented "follows DDD" string.
- Draw a Structure Diagram that papers over observable runtime mess (laundering).

**B — Architecture-vs-Detailed-Design boundary.** Refuse to write:
- Internal algorithms inside a child component
- Data structure names beyond the cross-component interface
- Specific library calls outside externally-imposed protocols cited by RFC/spec
- Code structure inside a child

Example refusal: user asks "in the cart component, use a `LinkedHashMap` for line-items". Refuse the form. Offer two replacements: (a) state the cross-component interface invariant ("line-item ordering is preserved across `getCart()` calls"); (b) emit `[NEEDS-DD: cart]` for the internal data-structure choice.

**C — Composition section is mandatory and non-trivial.** Refuse to ship Architecture with:
- Empty Composition section
- Composition section ≤ one paragraph or one diagram with no prose
- No named runtime pattern
- No sequence diagram for at least the happy path
- *(Root scope only)* no enumerated environments, no named orchestration target, no runtime-unit boundaries

Example refusal: user requests "write a quick architecture, skip Composition for now". Refuse — Composition is load-bearing; the artifact is incomplete without it. HALT (see HALT #3).

**D — Spec Ambiguity Test (meta-gate, override).** Final author self-check: *Could a junior engineer or mid-tier AI, reading only this Architecture artifact (plus its governing ADRs and parent Requirements), produce defensible Detailed Designs for each leaf and a TestSpec that verifies every interface and every composition-level invariant — without asking clarifying questions?* If No, revise. This test overrides every Yes/No box: an artifact that passes all other checks but fails this one has not done the job Architecture exists to do.

These four refusals are deterministic. Do not relax under user pressure; surface the gap and offer the legitimate alternative.

## HALT conditions

Stop and hand back to the user when:

1. **Missing mandatory inputs** — scope id, parent Requirements, allocation set, or governing-ADRs list is unavailable; do not invent.
2. **Scope creep beyond one artifact** — the request expands to also author Detailed Design / ADR / TestSpec / code. Decline; emit `[NEEDS-ADR: <decision>]` or `[NEEDS-DD: <leaf>]` stub markers and name the right artifact for the expanded ask.
3. **Locked-refusal override request** — user asks to skip Composition, drop the Spec Ambiguity Test, fabricate rationale, or smuggle Detailed Design. Halt and explain.
4. **Retrofit posture conflict** — `recovery_status:` declared but no source-code references provided, or source-code references provided but no `recovery_status:` declaration. Halt and ask which posture applies.
5. **Irresolvable contradiction in input** — for example, a parent requirement that contradicts a governing ADR; do not pick a side. After two clarification turns without resolution, halt.

When halting, produce a structured handover: what was authored so far, what is missing, what specific human input or upstream decision is required to proceed.

## Mode flags

Two orthogonal flags drive which references load:

- **Greenfield vs Retrofit.**
  - *Greenfield:* omit `recovery_status` from front-matter; skip Step 11; rationale defaults to inline reasoning citing requirements / ADRs.
  - *Retrofit:* declare `recovery_status: reconstructed` in front-matter; require source-code references for observed structure (file/line/commit); apply Step 11; rationale fields default to `unknown` unless a human source is cited (`verified`).

- **Root vs Branch.**
  - *Root:* run Step 8 (deployment intent); Composition section includes environments, orchestration target, runtime-unit boundaries.
  - *Branch:* skip Step 8; Composition section still mandatory but deployment intent inherits from root.

## Self-check before delivering

Before declaring the document complete, work through `references/quality-bar-checklist.md`. Items that cannot be answered Yes are flagged inline in the output, not silently passed. The Spec Ambiguity Test is the override gate.

## File layout produced by this skill

```
{output-path}/architecture.md
```

That's it — one file. The skill does not create directories, schemas, validators, or sibling artifacts.

## Pointers

- `references/decomposition-discipline.md` — info hiding, cohesion+coupling, bounded contexts, context-mapping, Conway-inverse, depth heuristics
- `references/interface-contracts.md` — syntax-vs-semantics, Design-by-Contract clauses, SEI nine-part template, ISP, versioning + deprecation
- `references/composition-patterns.md` — protocol families + sync/async + composition patterns catalog + wiring concerns
- `references/data-and-persistence.md` — DB-per-service vs shared, consistency model, read replicas + CQRS, multi-tenancy isolation
- `references/resilience-patterns.md` — bulkhead, circuit breaker, retry+jitter, degradation-vs-fallback, failure domains, common-cause failure
- `references/observability-and-security.md` — telemetry emergence points, trust zones + STRIDE, secrets flow, authn-vs-authz at boundaries
- `references/deployment-intent.md` — environments, orchestration target, runtime-unit boundaries, IaC-as-implementation, 12-factor, cost as constraint (root only)
- `references/evolution-and-fitness-functions.md` — architecture-as-hypothesis, fitness function classifications, four CI fitness function categories, strangler fig, testability
- `references/adr-extraction-cues.md` — when to extract a decision to ADR, the `[NEEDS-ADR]` stub, ADR-vs-Architecture relationship, front-matter + body-citation pattern
- `references/retrofit-discipline.md` — observed-structure marking, rationale `verified`-or-`unknown`, gap report, honest-vs-laundered side-by-side
- `references/anti-patterns.md` — 6 universal + 4 AI-era anti-patterns with tells and remedies
- `references/quality-bar-checklist.md` — 8 Quality Bar cards + Spec Ambiguity Test meta-gate
- `templates/architecture.md.tmpl` — full artifact scaffold
- `templates/decomposition-entry.yaml.tmpl` — per-child YAML stub
- `templates/interface-entry.yaml.tmpl` — full DbC interface contract YAML
- `templates/structure-diagram.mmd.tmpl` — Mermaid flowchart skeleton
- `templates/sequence-diagram.mmd.tmpl` — Mermaid sequenceDiagram skeleton (happy + failure path slot)
- `templates/deployment-diagram.mmd.tmpl` — Mermaid flowchart with subgraph (root only)
- `templates/governing-adr-reference.yaml.tmpl` — front-matter list + body-citation pattern
- `examples/good-checkout-service.md` — worked example, honest, root-scope
- `examples/bad-laundered-retrofit.md` — counter-example with annotated retrofit-laundering tells
