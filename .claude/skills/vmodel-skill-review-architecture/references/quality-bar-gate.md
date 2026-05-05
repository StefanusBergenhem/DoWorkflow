# Quality Bar gate — checklist + canonical check identifier catalog

Two purposes in one file:
1. The Yes/No checklist that the review skill walks as part of the final sweep. Every No is a finding.
2. The canonical catalog of `check_failed` identifiers used across this skill — the stable name space for findings.

> **Cap exception (~150-line soft cap):** This file is the single source of truth for the canonical `check.*` and `anti-pattern.*` catalog used skill-wide. Splitting would fragment the catalog and break the convention that one file owns one identifier namespace. The file is structured as a TOC + per-section catalog tables for navigation; readers do not need to read end-to-end.

## Contents

- Quality Bar — Yes/No checklist
- Canonical `check_failed` identifier catalog
  - `check.decomposition.*` and `check.responsibility.*`
  - `check.interface.*`
  - `check.composition.*`
  - `check.qa.*`
  - `check.resilience.*`
  - `check.security.*` and `check.observability.*`
  - `check.rationale.*`, `check.adr.*`, `check.traceability.*`, `check.fitness-function.*`
  - `check.retrofit.*`
  - `check.spec-ambiguity-test.*`
  - `check.discipline.*` and `check.bundle.*`
  - `anti-pattern.*`
- Rule for new check identifiers

## Quality Bar — Yes/No checklist

Walk every applicable item. Every No becomes a finding with the `check_failed` id from the catalog.

### Decomposition and boundaries

- [ ] Every child's purpose is one sentence with no conjunctions
- [ ] Every child's responsibility list ≤3 items, all architectural-level
- [ ] No responsibility prescribes implementation (HARD)
- [ ] Every parent-allocated requirement lands in at least one child (no requirement orphans)
- [ ] Every child has at least one allocation (no component orphans)
- [ ] Depth/cognitive-load/change-blast trio recorded if revision happened
- [ ] Bounded-context fractures are at child boundaries

### Interfaces and contracts

- [ ] Preconditions, all three postcondition branches, invariants, typed errors, quality attributes per interface
- [ ] Authn/authz at every externally callable interface with evaluation layer named
- [ ] No fat god-interfaces (ISP)
- [ ] Versioning scheme + deprecation policy per versioned interface
- [ ] Rationale on every interface
- [ ] Externally-imposed protocols cited by RFC/spec id
- [ ] No implementation leak across the boundary (HARD)

### Composition completeness — load-bearing

- [ ] Composition section present and non-trivial (HARD)
- [ ] Runtime pattern named (HARD)
- [ ] Sequence diagram for happy path (HARD)
- [ ] Deployment intent at root (environments, orchestration target, runtime units) (HARD, root-only)
- [ ] Middleware stack ordered
- [ ] DI strategy named
- [ ] Message-bus topology specified where applicable
- [ ] Failure-path sequence diagram
- [ ] Rationale on the runtime-pattern choice

### Quality-attribute coverage

- [ ] Every parent NFR allocated to a component, interface, or composition commitment
- [ ] Latency / throughput / availability budgets allocated per interface
- [ ] Consistency model named per data path
- [ ] Cost model stated at root scope (envelope, cost-per-request, cost-of-a-9)

### Resilience coverage

- [ ] Bulkhead partitions named for shared resources
- [ ] Circuit breakers at cross-service boundaries
- [ ] Retry policy specified with idempotency design
- [ ] Graceful degradation designed in for non-essential dependencies
- [ ] Failure domains named and mapped
- [ ] Redundancy claims backed by named independence properties

### Security and observability at boundaries

- [ ] Trust zones drawn explicitly
- [ ] Authn/authz evaluation layer named per externally callable interface
- [ ] Secrets flow specified (origin, in-memory holders, forbidden surfaces)
- [ ] Telemetry emergence points specified (logs, metrics, traces, common context fields)
- [ ] Sampling policy specified

### Rationale and traceability

- [ ] Rationale on every Decomposition entry, Interface entry, load-bearing composition choice
- [ ] No generic principle invocation in rationale (soft greenfield; HARD retrofit — see anti-patterns)
- [ ] Governing ADRs resolve (HARD if dangling)
- [ ] Governing ADRs cited at the body decision point
- [ ] Load-bearing cross-cutting hard-to-reverse decisions extracted to ADR (not inlined)
- [ ] Every parent-allocated requirement lands in this artifact (artifact-level allocation)
- [ ] Derived requirements marked and citing the introducing decision
- [ ] Fitness functions named for load-bearing properties

### Retrofit honesty (retrofit mode only)

- [ ] Observed structure marked `reconstructed` with evidence
- [ ] Human-only content marked `verified` or `unknown` only — never `reconstructed` (HARD)
- [ ] Every `unknown` paired with follow-up owner + action
- [ ] Gap report populated (lost rationale, structural drift, missing ADRs, coverage gaps)
- [ ] No laundering — diagram acknowledges runtime mess (HARD if absent)

### Spec Ambiguity Test (meta-gate, override)

- [ ] Could a junior engineer or mid-tier AI, reading this artifact (plus governing ADRs and parent Requirements), produce defensible Detailed Designs and a TestSpec — without asking clarifying questions?

If No, the artifact fails regardless of how many other items passed.

---

## Canonical `check_failed` identifier catalog

Stable identifiers used across all findings. Two namespaces visible here:
- `check.<area>.<specific>` — structural / completeness checks (this file).
- `anti-pattern.<name>` — see `anti-patterns-catalog.md` for full table.

### `check.decomposition.*` and `check.responsibility.*` (Decomposition card)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.decomposition.purpose-not-one-sentence` | soft_reject | — |
| `check.decomposition.responsibility-too-many` | soft_reject | — |
| `check.decomposition.responsibility-conjunction-and-test` | soft_reject | — |
| `check.decomposition.requirement-orphan` | soft_reject | — |
| `check.decomposition.component-orphan` | soft_reject | — |
| `check.decomposition.depth-test-not-applied` | soft_reject | non-trivial scope only |
| `check.decomposition.bounded-context-fracture-ignored` | soft_reject | — |
| `check.responsibility.implementation-prescription` | **hard_reject** ★ (refusal B) | — |

### `check.interface.*` (Interfaces card)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.interface.missing-precondition` | soft_reject | — |
| `check.interface.missing-postcondition` | soft_reject | — |
| `check.interface.missing-invariant` | soft_reject | — |
| `check.interface.missing-typed-error` | soft_reject | — |
| `check.interface.missing-quality-attribute` | soft_reject | — |
| `check.interface.missing-authn-authz` | soft_reject | externally callable only |
| `check.interface.fat-god-interface` | soft_reject | — |
| `check.interface.missing-versioning-policy` | soft_reject | — |
| `check.interface.missing-rationale` | soft_reject | — |
| `check.interface.protocol-not-cited-by-spec` | soft_reject | — |
| `check.interface.implementation-leak` | **hard_reject** ★ (refusal B) | — |

### `check.composition.*` (Composition card)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.composition.missing` | **hard_reject** ★ (refusal C) | — |
| `check.composition.no-named-pattern` | **hard_reject** ★ (refusal C) | — |
| `check.composition.no-sequence-diagram` | **hard_reject** ★ (refusal C) | — |
| `check.composition.deployment-intent-missing` | **hard_reject** ★ (refusal C) | root only (`parent_scope: null`) |
| `check.composition.middleware-stack-unordered` | soft_reject | — |
| `check.composition.di-strategy-unnamed` | soft_reject | — |
| `check.composition.message-bus-topology-unspecified` | soft_reject | messaging-pattern only |
| `check.composition.failure-path-sequence-diagram-missing` | soft_reject | — |
| `check.composition.no-rationale-on-pattern` | soft_reject | — |

### `check.qa.*` (Quality-attribute card)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.qa.nfr-not-allocated` | soft_reject | — |
| `check.qa.budget-not-allocated-to-interface` | soft_reject | — |
| `check.qa.consistency-model-not-specified` | soft_reject | — |
| `check.qa.cost-model-missing` | soft_reject | root only |

### `check.resilience.*` (Resilience card)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.resilience.bulkhead-partitions-not-named` | soft_reject | — |
| `check.resilience.circuit-breaker-missing` | soft_reject | cross-service boundary only |
| `check.resilience.retry-policy-unspecified` | soft_reject | — |
| `check.resilience.degradation-not-designed` | soft_reject | non-essential dependency present |
| `check.resilience.failure-domains-unnamed` | soft_reject | — |
| `check.resilience.redundancy-without-independence-property` | soft_reject | redundancy claim present |

### `check.security.*` and `check.observability.*` (Security & Observability card)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.security.trust-zones-not-drawn` | soft_reject | — |
| `check.security.authn-authz-evaluation-layer-unnamed` | soft_reject | externally callable interface present |
| `check.security.secrets-flow-unspecified` | soft_reject | secrets present |
| `check.observability.telemetry-emergence-unspecified` | soft_reject | — |
| `check.observability.sampling-policy-unspecified` | soft_reject | tracing present |

### `check.rationale.*`, `check.adr.*`, `check.traceability.*`, `check.fitness-function.*` (Rationale & ADR & Traceability card)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.rationale.missing` | soft_reject | — |
| `check.rationale.generic-principle-invocation` | soft_reject | greenfield (retrofit → anti-pattern.fabricated-decomposition-rationale, HARD) |
| `check.rationale.recovery-status-reconstructed` | **hard_reject** ★ (refusal A) | — |
| `check.adr.governing-not-resolved` | **hard_reject** ★ (broken-reference integrity) | `governing_adrs:` non-empty |
| `check.adr.governing-not-cited-in-body` | soft_reject | `governing_adrs:` non-empty |
| `check.adr.inline-decision-should-be-extracted` | soft_reject | — |
| `check.traceability.requirement-not-allocated` | soft_reject | parent Requirements supplied |
| `check.traceability.derived-requirement-not-marked` | soft_reject | — |
| `check.fitness-function.not-named-for-load-bearing-property` | soft_reject | — |

### `check.retrofit.*` (Retrofit honesty card — conditional on `recovery_status:` declared)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.retrofit.observed-not-marked-with-evidence` | soft_reject | retrofit mode |
| `check.retrofit.human-only-content-marked-reconstructed` | **hard_reject** ★ (refusal A) | retrofit mode |
| `check.retrofit.unknown-without-followup-owner` | soft_reject | retrofit mode |
| `check.retrofit.gap-report-missing` | soft_reject | retrofit mode |
| `check.retrofit.laundering-detected` | **hard_reject** ★ (refusal A) | retrofit mode |

### `check.spec-ambiguity-test.*` (meta-gate, override)

| Identifier | Severity | Verdict-precedence behaviour |
|---|---|---|
| `check.spec-ambiguity-test.fail` | **override** | DESIGN_ISSUE if upstream-traceable; REJECTED otherwise |

The override is the meta-gate. If the artifact fails this test, the verdict is set by the precedence rule regardless of how many other items passed. Apply judgment: an artifact that passes every Yes/No box but cannot be acted on by a junior engineer has not done the job Architecture exists to do.

### `check.discipline.*` and `check.bundle.*` (cross-cutting authoring discipline + bundle integrity)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.discipline.architecture-bundle-shape` | soft_reject | Rule 8 — banned decomposition fields, or helicopter form incomplete |
| `check.discipline.scope-tree-shape` | soft_reject | Rule 7 — child scope ID collides with reserved subdirectory name |
| `check.bundle.detail-link-broken` | **hard_reject** ★ | helicopter form — `detail:` path does not resolve |
| `check.bundle.belongs-to-mismatch` | **hard_reject** ★ | helicopter form — detail file `belongs_to` / `subject` does not match helicopter back-link |

### `anti-pattern.*` (see `anti-patterns-catalog.md` for full entries)

| Identifier | Severity |
|---|---|
| `anti-pattern.big-ball-of-mud` | soft_reject |
| `anti-pattern.distributed-monolith` | soft_reject |
| `anti-pattern.god-component` | soft_reject |
| `anti-pattern.premature-decomposition` | soft_reject |
| `anti-pattern.stale-architecture` | soft_reject |
| `anti-pattern.cyclic-dependencies` | soft_reject |
| `anti-pattern.laundered-architecture` | **hard_reject** ★ (refusal A) |
| `anti-pattern.fabricated-decomposition-rationale` | **hard_reject** ★ (refusal A) |
| `anti-pattern.ad-hoc-composition` | soft_reject |
| `anti-pattern.dd-content-in-architecture` | **hard_reject** ★ (refusal B) |
| `anti-pattern.team-topology-fields-in-architecture` | soft_reject |
| `anti-pattern.helicopter-without-detail` | **hard_reject** ★ (broken-reference integrity, Rule 8) |

★ marks hard-reject triggers (one occurrence rejects the document, except `check.spec-ambiguity-test.fail` which routes per precedence rule).

## Rule for new check identifiers

When a check is added (e.g., a new anti-pattern is recognised, a new sub-check is introduced):

1. Pick a stable dotted name in the appropriate namespace.
2. Add it to this catalog with severity and conditional gating.
3. Add the rule to the appropriate reference file (`*-checks.md` in `references/`).
4. Add a row to the Quality Bar checklist above if it is a Yes/No item.

Do not invent ad-hoc `check_failed` strings during a review — every finding's identifier must appear in this catalog or in `anti-patterns-catalog.md`.
