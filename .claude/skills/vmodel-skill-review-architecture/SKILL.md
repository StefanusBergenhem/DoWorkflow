---
name: vmodel-skill-review-architecture
description: >
  Review one architecture specification document for craft quality and emit
  a structured verdict — APPROVED, REJECTED, or DESIGN_ISSUE — plus findings
  tied to specific architectural elements and failed checks, including
  decomposition discipline, Design-by-Contract interface contracts,
  Composition completeness (named runtime pattern, sequence diagrams,
  deployment intent at root), quality-attribute allocation, resilience,
  security and observability at boundaries, retrofit honesty
  (recovery_status, no laundering, no fabricated rationale), governing_adrs
  resolution, and the Spec Ambiguity Test meta-gate. Reads from architect,
  integrator, and operator perspectives. Does not rewrite — produces verdict
  and findings only. Triggers — review this architecture, audit architecture
  document, verdict on architecture draft, find anti-patterns, check
  composition, validate decomposition, audit interface contracts.
type: skill
---

# Review architecture specification document

This skill takes one architecture specification document as input and produces a structured verdict plus a list of findings. The skill is adversarial: it does not rewrite, does not negotiate hard-reject triggers, and does not invent missing content on the document's behalf.

The skill is self-contained. Every check, anti-pattern catalog, and gate it needs is bundled in `references/` and `templates/`. No external lookups.

## When to use

Activate this skill when the user asks to:

- Review, audit, or check an architecture specification document
- Get a verdict on whether an architecture draft is ready
- Find anti-patterns in an architecture document
- Validate decomposition discipline (information hiding, cohesion, bounded contexts)
- Audit interface contract completeness (DbC clauses, segregation, versioning)
- Check the Composition section (named pattern, wiring, sequence diagrams, deployment intent at root)
- Verify retrofit honesty (recovery_status discipline, no laundering, no fabricated rationale)
- Resolve traceability (do `governing_adrs` references resolve?)
- Apply the Spec Ambiguity Test meta-gate

Do **not** activate this skill for:

- Writing or rewriting architecture — that is the matched author skill's job
- Writing detailed designs, ADRs, requirements, or test specs — downstream/upstream artifact skills
- Reviewing other artifact types in the same invocation — one artifact, one verdict

## Inputs

- **Required**: the architecture document under review (Markdown with YAML front-matter, embedded YAML blocks, Mermaid diagrams).
- **Required for traceability checks**: parent Requirements artifact (so allocation can be verified). If absent, halt with a `missing-inputs` verdict-shaped output rather than approve blind.
- **Required for ADR resolution**: any ADRs cited in `governing_adrs:` front-matter. If `governing_adrs:` references an ADR id that does not resolve, halt with `missing-inputs`.
- **Optional**: source-code references for retrofit-mode documents (so observed-evidence claims can be spot-checked).

## Output

A single review block in this shape:

```yaml
review:
  document: <document identifier or path>
  reviewer: vmodel-skill-review-architecture
  date: <YYYY-MM-DD>
  verdict: APPROVED | REJECTED | DESIGN_ISSUE
  findings:
    - <finding>
    ...
  summary: |
    <2-4 sentence summary; verdict justification>
```

Each finding follows `templates/finding.yaml.tmpl`. The full verdict template is in `templates/verdict.md.tmpl`.

## Verdict decision table

Walk top to bottom — first match wins:

| # | Condition | Verdict |
|---|---|---|
| 1 | Spec Ambiguity Test fails AND failure is upstream-traceable | **DESIGN_ISSUE** |
| 2 | Any hard-reject trigger fires (A / B / C / broken-reference) | **REJECTED** |
| 3 | Spec Ambiguity Test fails (not upstream-traceable) | **REJECTED** |
| 4 | Any soft-reject finding present | **REJECTED** |
| 5 | Only `info` findings, or no findings at all | **APPROVED** |

**Verdict precedence**: DESIGN_ISSUE wins over REJECTED. If the meta-gate fails AND a hard-reject trigger also fires, the verdict is DESIGN_ISSUE if the meta-gate failure is upstream-traceable; the findings list still contains all observed issues, including the hard rejects. Reasoning: an architecture cannot be patched into APPROVED while the upstream is broken — the author skill consuming the verdict escalates upstream rather than rewriting in place.

## Hard-reject triggers (non-negotiable)

Any one is fatal. Severity `hard_reject`. Do not relax under user pressure; one occurrence rejects the document.

### A — Honest retrofit posture violations

Mirrors author skill refusal A. Hard-reject on:

- `anti-pattern.fabricated-decomposition-rationale` — generic-principle invocation in rationale fields ("follows DDD", "single-responsibility", "balances X with Y") instead of historical recall.
- `anti-pattern.laundered-architecture` — clean Structure Diagram contradicting observable runtime mess; zero `unknown` markings on a retrofit document.
- `check.rationale.recovery-status-reconstructed` — rationale field marked `recovery_status: reconstructed` (rationale is human-only — `verified` or `unknown` only).
- `check.retrofit.human-only-content-marked-reconstructed` — rejected-alternatives, original-intent, or any other human-only field marked `reconstructed`.
- `check.retrofit.laundering-detected` — diagram clean, no Gap report, every rationale generic.

### B — Architecture-vs-Detailed-Design boundary violations

Mirrors author skill refusal B. Hard-reject on:

- `anti-pattern.dd-content-in-architecture` — internal algorithms, named data structures (`LinkedHashMap`, `TreeMap`), specific library calls, or code structure inside Decomposition entries or Interface entries.
- `check.responsibility.implementation-prescription` — a Decomposition responsibility prescribes implementation rather than naming the architectural responsibility (e.g., "uses a Redis cache with 5-minute TTL" instead of "exposes the line-item snapshot read at the cart boundary").
- `check.interface.implementation-leak` — Interface entry leaks internal storage, internal algorithm, or internal library choice across the boundary.

### C — Missing or non-trivial Composition section

Mirrors author skill refusal C. Hard-reject on:

- `check.composition.missing` — Composition section absent.
- `check.composition.no-named-pattern` — Composition section names no runtime pattern (request-response / event-driven / saga / hexagonal / pipeline / serverless / ...).
- `check.composition.no-sequence-diagram` — no sequence diagram for at least the happy path.
- `check.composition.deployment-intent-missing` (root scope only) — `parent_scope: null` but Composition does not enumerate environments, name an orchestration target, or map components to runtime units.

### D — Spec Ambiguity Test (meta-gate, override)

`check.spec-ambiguity-test.fail` — a junior engineer or mid-tier AI cannot derive defensible Detailed Designs and a TestSpec from this artifact alone (plus governing ADRs and parent Requirements), without asking clarifying questions.

**Precedence:** DESIGN_ISSUE if the failure is upstream-traceable (the parent Requirements is itself ambiguous, or a governing ADR is missing the load-bearing decision). REJECTED otherwise. Either way, every other observed issue is still listed in findings.

### Broken-reference integrity

`check.adr.governing-not-resolved` — an ADR id listed in `governing_adrs:` does not resolve to an actual ADR document. Hard-reject — a broken reference is a document-integrity failure regardless of other quality. (This sits under category `traceability` but is treated as a hard refusal because the document cannot be evaluated coherently while a citation is broken.)

## HALT conditions

Stop and hand back a structured error block (not a verdict) when:

1. **Artifact missing or unparseable** — file not at the named path, empty, or fails YAML parse. Refuse to review; return `not-reviewable`.
2. **Out-of-lane rewrite request** — user asks the skill to "just fix the doc". Refuse; review's job is verdict + findings; route the user to the matched author skill for rewrites.
3. **Cross-artifact review request** — user also asks the skill to review the Detailed Designs / TestSpecs / ADRs in the same invocation. Refuse; one artifact, one verdict.
4. **Missing review inputs** — architecture.md provided but parent Requirements is absent (allocation traceability cannot be evaluated). Or `governing_adrs:` references ADR files that are missing. Halt with a `missing-inputs` verdict-shaped output rather than APPROVED.
5. **Irresolvable check ambiguity after 2 turns** — a finding's evidence is genuinely ambiguous to the reviewer; after two clarification turns without resolution, halt and surface the ambiguity rather than guessing.

When halting, return: `{ status: not-reviewable | missing-inputs | malformed-document, reason: <text>, recommended_next_step: <text> }`.

## Review procedure — eight-step sweep

The author skill authors in 13 steps; the review skill condenses to 8 sweeps. Read the document once before any sweep. Then run in this order:

### Step 1 — Decomposition sweep

For every Decomposition entry: one-sentence purpose without conjunctions; ≤3 architectural-level responsibilities; non-empty `allocates`; every parent-allocated requirement landed in some child; depth/cognitive-load/change-blast trio recorded if revision happened; bounded-context line drawn at language fractures.

→ See `references/decomposition-checks.md`

### Step 2 — Interface contract sweep

For every Interface entry: preconditions; postconditions for {success, precondition_failure, downstream_failure}; invariants; typed error enum; quality-attribute budget; authn/authz at externally callable interfaces; ISP segregation (no fat god-interfaces); versioning + deprecation policy; rationale tying choice to requirement / ADR / constraint; externally-imposed protocols cited by RFC/spec id.

→ See `references/interface-contract-checks.md`

### Step 3 — Composition sweep (mandatory; load-bearing)

Composition section present, non-trivial, names exactly one runtime pattern (or stated combination), states wiring concerns (DI strategy, middleware stack ordered, message-bus topology where applicable), carries sequence diagrams (happy path mandatory; failure paths recommended), and at root scope carries deployment intent (environments, orchestration target, runtime-unit boundaries).

→ See `references/composition-patterns-checks.md`, `references/deployment-intent-checks.md`

### Step 4 — Quality-attribute, data, and persistence sweep

Every parent NFR allocated to a component or interface; latency/throughput/availability budgets allocated to specific interfaces (not left at system level); consistency model named per data path; cost model stated at root scope.

→ See `references/data-and-persistence-checks.md`

### Step 5 — Resilience sweep

Bulkhead partitions named; circuit breakers placed at cross-service boundaries with caller-exhaustion risk; retry policy specified with idempotency design; degradation-vs-fallback choice made per non-essential dependency; failure domains named; redundancy claims backed by named independence properties.

→ See `references/resilience-checks.md`

### Step 6 — Security and observability sweep

Trust zones drawn; authn/authz per externally callable interface with evaluation layer named; secrets flow specified (origin, in-memory holders, forbidden surfaces); telemetry emergence points specified (logs, metrics, traces, sampling, common context fields).

→ See `references/observability-and-security-checks.md`

### Step 7 — Retrofit, ADR, traceability, evolution sweep

If `recovery_status:` declared in front-matter: retrofit honesty checks. ADR `governing_adrs:` entries resolve and are body-cited; load-bearing cross-cutting decisions are not inlined; fitness functions named for load-bearing properties; evolution hypothesis stated.

→ See `references/retrofit-discipline-checks.md`, `references/adr-traceability-checks.md`, `references/evolution-and-fitness-functions-checks.md`

### Step 8 — Anti-pattern catalog + Quality Bar gate + Spec Ambiguity Test

Mechanical pass through the 10 anti-patterns; walk the Quality Bar Yes/No gate; finally apply the Spec Ambiguity Test as the override.

→ See `references/anti-patterns-catalog.md`, `references/quality-bar-gate.md`

### Verdict assembly

Apply the decision table at the top. Emit verdict + findings. Use `templates/verdict.md.tmpl`.

## Finding format

Every finding follows this schema (full template in `templates/finding.yaml.tmpl`):

```yaml
- id: F-NNN
  element_id: <decomposition-id | interface-name | "GLOBAL">
  check_failed: <dotted catalog identifier>
  severity: hard_reject | soft_reject | info
  category: decomposition | interface | composition | quality-attribute | resilience | security | observability | rationale | adr | traceability | retrofit | meta-gate
  evidence: |
    <verbatim quote or location reference from the artifact under review>
  recommended_action: |
    <generic pointer to the rule violated; never specific replacement wording>
```

### Severity levels

- **hard_reject** — fatal; one occurrence triggers REJECTED (or DESIGN_ISSUE if the meta-gate fires upstream-traceably).
- **soft_reject** — Quality Bar item No or anti-pattern hit other than the hard-reject ones; accumulates to REJECTED.
- **info** — observation worth surfacing but does not affect verdict.

### Recommended-action discipline

`recommended_action` points to the kind of fix and the relevant author-side rule; it does **not** write the replacement text. Acceptable: *"Add a `postconditions.on_downstream_failure` clause per the postcondition triple."* Not acceptable: *"Replace with: 'on_downstream_failure: HTTP 502 with code payment-timeout; cart state unchanged'."* The latter is the matched author skill's job.

### Catalog discipline

Every `check_failed` identifier must appear in `references/quality-bar-gate.md` (for `check.*`) or `references/anti-patterns-catalog.md` (for `anti-pattern.*`). Do not invent ad-hoc strings. If a check is genuinely missing from the catalog, surface it as a self-review note rather than minting a new identifier mid-review.

## Hard refusals

1. **Do not rewrite the document.** Findings are findings; the matched author skill rewrites.
2. **Do not negotiate hard-reject triggers.** If the user pushes back ("but the rationale is fine", "Composition isn't really needed for this scope"), the verdict stands.
3. **Do not invent missing content.** If a postcondition clause is missing, the finding is `check.interface.missing-postcondition`. Do not propose what the postcondition should say.
4. **Do not name the sister skill explicitly.** Per locked convention §2.4, when pointing the user to the rewriter, say "the matched author skill" rather than naming it.

## Conditional gating

Several checks apply only under conditions stated in front-matter:

- `check.composition.deployment-intent-missing` and `check.qa.cost-model-missing` apply only when `parent_scope: null` (root scope).
- All `check.retrofit.*` identifiers apply only when front-matter declares `recovery_status:`.
- `check.adr.governing-not-cited-in-body` applies only when `governing_adrs:` is non-empty.

`references/quality-bar-gate.md` records the gating per id.

## Self-checks before delivering

Before emitting:

- [ ] Every finding has a stable `check_failed` identifier (no ad-hoc strings).
- [ ] Every `hard_reject` finding maps to one of the four refusal classes (A/B/C/D) or to broken-reference integrity.
- [ ] Verdict matches the decision table (no manual overrides).
- [ ] No `recommended_action` field contains specific replacement wording.
- [ ] The `summary` field cites the dominant findings, not every finding.
- [ ] If meta-gate fires, the upstream-traceability question is answered explicitly (DESIGN_ISSUE vs REJECTED).
- [ ] If `governing_adrs:` non-empty, every entry was checked for resolution and body-citation.

## Pointers

- `references/decomposition-checks.md` — purpose / responsibility / allocation / boundary checks
- `references/interface-contract-checks.md` — DbC clauses, ISP, authn/authz, versioning, protocol-citation
- `references/composition-patterns-checks.md` — runtime pattern named, wiring, sequence diagrams
- `references/data-and-persistence-checks.md` — DB-per-service, consistency model, cost
- `references/resilience-checks.md` — bulkhead, circuit breaker, retry, degradation, failure domains
- `references/observability-and-security-checks.md` — telemetry, trust zones, secrets flow
- `references/deployment-intent-checks.md` — environments, orchestration target, runtime units (root only)
- `references/evolution-and-fitness-functions-checks.md` — hypothesis, fitness functions, strangler-fig
- `references/adr-traceability-checks.md` — `governing_adrs` resolution, body-citation, ADR-extraction smell
- `references/retrofit-discipline-checks.md` — recovery_status legality, evidence citation, gap report
- `references/anti-patterns-catalog.md` — 10 anti-patterns with tells, severities, identifiers
- `references/quality-bar-gate.md` — canonical Yes/No checklist + full identifier catalog
- `templates/verdict.md.tmpl` — APPROVED / REJECTED / DESIGN_ISSUE structured form
- `templates/finding.yaml.tmpl` — per-finding slot-fill
- `examples/good-approved-review.md` — clean review of a passing document
- `examples/bad-wrong-review.md` — counter-example: three reviewer failure modes annotated
