# Quality Bar gate — checklist + canonical check identifier catalog

> **Cap exception (~150-line soft cap):** This file is the single source of truth for the canonical `check.*` and `anti-pattern.*` catalog used skill-wide. Splitting would fragment the catalog and break the convention that one file owns one identifier namespace. The file is structured as a TOC + per-section catalog tables for navigation; readers do not need to read end-to-end.

Two purposes in one file:
1. Yes/No checklist the review skill walks. Every No → finding.
2. Canonical catalog of `check_failed` identifiers — stable name space for findings.

## Table of contents

- [Quality Bar — Yes/No checklist](#quality-bar--yesno-checklist) — walk per sweep
- [Canonical `check_failed` identifier catalog](#canonical-check_failed-identifier-catalog) — IDs, severities, gating
- [Rule for new check identifiers](#rule-for-new-check-identifiers) — extension protocol

## Quality Bar — Yes/No checklist

### Shape

- [ ] Front-matter declares `id`, `scope`, `level`, `verifies`, `derived_from`, `coverage_mutation_bar` (HARD if any required field absent)
- [ ] Id pattern conformant
- [ ] Declared `level` matches scope position (root→system / non-leaf→integration / leaf→unit)
- [ ] Every case block well-formed (id unique, type, verifies, per-layer fields)

### Derivation strategy

- [ ] Every case has `type` (HARD if missing)
- [ ] Every case `type` is in the closed enum (HARD if not)
- [ ] Every parent-spec behaviour rule has ≥ 1 functional case
- [ ] Every parent-spec input range has ≥ 1 boundary case
- [ ] Every parent-spec error-matrix row has ≥ 1 error / fault-injection case

### Per-layer weight

- [ ] Leaf cases thin (no heavy preconditions / steps unless justified)
- [ ] Branch cases name fixtures / doubles / seeds / environment in preconditions; steps enumerate cross-child interactions
- [ ] Root cases use Product Brief vocabulary (no internal API names)
- [ ] Declared level matches scope position

### Case quality

- [ ] F.I.R.S.T. (Fast / Independent / Repeatable / Self-validating / Timely)
- [ ] AAA — one Act per case

### Oracle (refusal C lives here)

- [ ] No weak assertions (HARD)
- [ ] No unbounded negatives without bounded domain
- [ ] No tautological recomputation

### Verifies traceability (refusal B lives here)

- [ ] Artifact-level `verifies` non-empty (HARD)
- [ ] Every case `verifies` non-empty (HARD)
- [ ] Every `verifies` element resolves to a live upstream id (HARD)
- [ ] `verifies` granularity matches the layer

### Test-double discipline

- [ ] Every double's type named (dummy / stub / spy / mock / fake)
- [ ] Every fake declares a contract-test pointer
- [ ] Leaf case ≤ 2 doubles
- [ ] Interaction verification (`verify()`) reserved for cases where the interaction is the contract

### Coverage and mutation bar

- [ ] `coverage_mutation_bar:` block present (HARD if absent — derived hard)
- [ ] Structural threshold field present
- [ ] Mutation threshold field present
- [ ] Mutation tool category named
- [ ] Enforcement frequency named

### Integration and system specifics

- [ ] Branch cases declare contract tests per parent-Architecture interface
- [ ] Branch / root cases name environment shape
- [ ] Quality-attribute requirements have specialised cases (perf / sec / a11y)
- [ ] Versioned dependencies pinned in preconditions

### Retrofit honesty (retrofit only)

- [ ] No inferred intent on `title` (HARD)
- [ ] No inferred intent on `notes` (HARD)
- [ ] Every reconstructed `verifies` carries `recovery_status: unknown` (HARD)
- [ ] Every retrofit case carries `recovery_status` per case
- [ ] Gap Report present

### Cross-artifact traceability (per-layer gating)

- [ ] **Leaf**: every parent-DD error-matrix row covered; postcondition covered; invariant covered; `[NEEDS-TEST]` resolved
- [ ] **Branch**: every parent-Architecture interface covered; composition invariant covered; QA allocation covered
- [ ] **Root**: every root requirement has ≥ 1 case across the tree; every Product Brief outcome covered; every NFR has a threshold case

### Spec Ambiguity Test (meta-gate, override)

- [ ] Junior engineer can write test code from this TestSpec, the parent spec, and governing ADRs alone
- [ ] Reviewer can tell whether every equivalence class, boundary, and error path was considered, without reading code

If any answer is No → TestSpec fails regardless of other items.

---

## Canonical `check_failed` identifier catalog

### `check.shape.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.shape.frontmatter-missing-required-field` | **hard_reject** ★ (schema invariant) | — |
| `check.shape.id-pattern-violation` | soft_reject | — |
| `check.shape.level-scope-mismatch` | soft_reject | — |
| `check.shape.case-block-malformed` | soft_reject | — |

### `check.derivation.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.derivation.type-missing` | **hard_reject** ★ (schema invariant) | — |
| `check.derivation.type-not-in-enum` | **hard_reject** ★ (schema invariant) | — |
| `check.derivation.rbt-uncovered-rule` | soft_reject | — |
| `check.derivation.bva-missing-at-boundary` | soft_reject | input range present |
| `check.derivation.error-path-uncovered` | soft_reject | error matrix present |

### `check.per-layer.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.per-layer.leaf-overweight` | info | leaf scope |
| `check.per-layer.branch-underweight` | soft_reject | branch scope |
| `check.per-layer.root-internal-vocab` | soft_reject | root scope |
| `check.per-layer.scope-derived-level-mismatch` | soft_reject | — |

### `check.case-quality.*` and `check.oracle.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.case-quality.firs-violation` | soft_reject | — |
| `check.case-quality.aaa-violation` | soft_reject | — |
| `check.oracle.weak-assertion` | **hard_reject** ★ (refusal C; aliases `anti-pattern.weak-assertions`) | — |
| `check.oracle.unbounded-negative` | soft_reject | — |
| `check.oracle.tautological-form` | soft_reject | — |

### `check.verifies.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.verifies.artifact-level-empty` | **hard_reject** ★ (refusal B) | — |
| `check.verifies.case-level-empty` | **hard_reject** ★ (refusal B) | — |
| `check.verifies.unresolvable` | **hard_reject** ★ (refusal B; aliases `anti-pattern.orphan-tests`) | — |
| `check.verifies.granularity-mismatch` | soft_reject | — |

### `check.test-doubles.*` (gated on doubles being declared)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.test-doubles.type-unnamed` | soft_reject | doubles declared |
| `check.test-doubles.fake-without-contract-test` | soft_reject | fake declared |
| `check.test-doubles.leaf-over-threshold` | soft_reject | leaf scope |
| `check.test-doubles.interaction-overuse` | soft_reject | mock / spy declared |

### `check.coverage-mutation.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.coverage-mutation.section-missing` | **hard_reject** ★ (derived hard — load-bearing QB group) | — |
| `check.coverage-mutation.structural-threshold-missing` | soft_reject | section present |
| `check.coverage-mutation.mutation-threshold-missing` | soft_reject | section present |
| `check.coverage-mutation.tool-unnamed` | soft_reject | section present |
| `check.coverage-mutation.frequency-unnamed` | soft_reject | section present |

### `check.integration.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.integration.contract-testing-absent` | soft_reject | branch scope |
| `check.integration.environment-unnamed` | soft_reject | branch / root scope |
| `check.integration.qa-specialised-case-absent` | soft_reject | parent-spec QA req present |
| `check.integration.version-pinning-missing` | soft_reject | versioned dep referenced |

### `check.retrofit.*` (only when `recovery_status:` declared)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.retrofit.intent-on-title` | **hard_reject** ★ (refusal A) | retrofit |
| `check.retrofit.intent-on-notes` | **hard_reject** ★ (refusal A) | retrofit |
| `check.retrofit.recovery-status-reconstructed-verifies` | **hard_reject** ★ (refusal A; aliases `anti-pattern.fabricated-retrofit-intent`) | retrofit |
| `check.retrofit.recovery-status-missing` | soft_reject | retrofit |
| `check.retrofit.gap-report-missing` | soft_reject | retrofit |

### `check.dd-traceability.*` (leaf-only)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.dd-traceability.error-matrix-uncovered` | soft_reject | leaf scope |
| `check.dd-traceability.postcondition-uncovered` | soft_reject | leaf scope |
| `check.dd-traceability.invariant-uncovered` | soft_reject | leaf scope |
| `check.dd-traceability.marker-unresolved` | soft_reject | leaf scope |

### `check.architecture-traceability.*` (branch-only)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.architecture-traceability.interface-uncovered` | soft_reject | branch scope |
| `check.architecture-traceability.composition-invariant-uncovered` | soft_reject | branch scope |
| `check.architecture-traceability.quality-attribute-unallocated` | soft_reject | branch scope |

### `check.requirements-traceability.*` (root-only)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.requirements-traceability.requirement-unverified` | soft_reject | root scope |
| `check.requirements-traceability.outcome-unverified` | soft_reject | root scope |
| `check.requirements-traceability.nfr-no-threshold-case` | soft_reject | NFR present |

### `check.adr.*` (front-matter reference integrity)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.adr.governing-not-resolved` | **hard_reject** ★ (broken-reference integrity) | `governing_adrs:` present |

### `check.derived-from.*` (front-matter reference integrity)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.derived-from.unresolvable` | **hard_reject** ★ (broken-reference integrity) | — |

### `check.spec-ambiguity-test.*` (meta-gate, override)

| Identifier | Severity | Verdict-precedence |
|---|---|---|
| `check.spec-ambiguity-test.fail` | **override** | DESIGN_ISSUE if upstream-traceable; REJECTED otherwise — forces DESIGN_ISSUE regardless of other findings when upstream-traceable |

### `anti-pattern.*` — see `anti-patterns-catalog.md`

| Identifier | Severity |
|---|---|
| `anti-pattern.code-to-test-derivation` | soft_reject |
| `anti-pattern.tautological-tests` | soft_reject |
| `anti-pattern.test-as-requirement-inversion` | soft_reject |
| `anti-pattern.happy-path-bias` | soft_reject |
| `anti-pattern.weak-assertions` | **hard_reject** ★ (refusal C) |
| `anti-pattern.over-mocking` | soft_reject |
| `anti-pattern.mystery-guest` | soft_reject |
| `anti-pattern.ice-cream-cone-coverage` | soft_reject |
| `anti-pattern.coverage-as-quality-metric` | soft_reject |
| `anti-pattern.unbounded-negative-tests` | soft_reject |
| `anti-pattern.flaky-tests` | soft_reject |
| `anti-pattern.orphan-tests` | **hard_reject** ★ (refusal B) |
| `anti-pattern.fabricated-retrofit-intent` | **hard_reject** ★ (refusal A; retrofit) |

★ marks hard-reject triggers (one occurrence rejects the document, except `check.spec-ambiguity-test.fail` which routes per precedence rule — DESIGN_ISSUE when upstream-traceable, REJECTED otherwise).

## Rule for new check identifiers

1. Pick a stable dotted name in the appropriate namespace.
2. Add it to this catalog with severity and conditional gating.
3. Add the rule to the appropriate `*-checks.md`.
4. Add a Yes/No item if it is checklist-shaped.

Do not invent ad-hoc `check_failed` strings during a review.
