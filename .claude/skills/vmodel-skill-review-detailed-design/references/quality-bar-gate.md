# Quality Bar gate — checklist + canonical check identifier catalog

Two purposes in one file:
1. Yes/No checklist the review skill walks. Every No → finding.
2. Canonical catalog of `check_failed` identifiers — stable name space for findings.

> **Cap exception (~150-line soft cap):** This file is the single source of truth for the canonical `check.*` and `anti-pattern.*` catalog used skill-wide. Splitting would fragment the catalog and break the convention that one file owns one identifier namespace. The file is structured as a TOC + per-section catalog tables for navigation; readers do not need to read end-to-end.

## Contents

- [Quality Bar — Yes/No checklist](#quality-bar--yesno-checklist) — walk per sweep
- [Canonical `check_failed` identifier catalog](#canonical-check_failed-identifier-catalog) — IDs, severities, gating
- [Rule for new check identifiers](#rule-for-new-check-identifiers) — extension protocol

## Quality Bar — Yes/No checklist

### Structure

- [ ] All seven sections present (with explicit absence assertion on stateless State)
- [ ] Metadata has `parent_architecture` (HARD), `derived_from` (non-empty), `governing_adrs` where applicable
- [ ] Overview names what slice of the parent Architecture this leaf realises

### Function contracts

- [ ] Every public function has 9 contract elements (signature, preconditions, postconditions split, invariants, errors, nullability, side-effects, thread-safety, complexity-when-contractual)
- [ ] Postconditions have both `on_success` and `on_failure` branches
- [ ] Postconditions express result properties, not steps (HARD)
- [ ] Both halves stated where applicable (HARD)
- [ ] Numeric parameters carry units

### Data and invariants

- [ ] Every data structure: fields with invariants where type insufficient; ownership; lifetime
- [ ] Returned-object semantics stated when crossing public interface
- [ ] Shared mutable state names lock + happens-before + reader/writer

### Algorithms

- [ ] No code paraphrase (HARD)
- [ ] Algorithm contractual → named AND why-named
- [ ] Specification-pattern fits behaviour shape (decision table / state machine / sequence)

### State and concurrency

- [ ] Stateless leaf asserts absence in one line
- [ ] Stateful leaf has state inventory + transition table + undefined-event handling
- [ ] Thread-safety category named (per leaf and per shared field)
- [ ] Cancellation contract on long-running ops
- [ ] Timing constraints have all five elements

### Error handling

- [ ] Six questions answered per error class
- [ ] Five-column matrix populated
- [ ] Recovery strategy named per row (one of fail-fast / retry / fallback / compensate / propagate)
- [ ] Bounded retry budget (no unbounded retry)
- [ ] No "undefined" state-after-error

### Rationale and traceability

- [ ] Inline rationale on every non-obvious decision
- [ ] Constraint kind named (external / architectural / resource / temporal)
- [ ] No generic-principle invocation (soft greenfield; HARD retrofit)
- [ ] `governing_adrs:` resolves (HARD if dangling) and is body-cited
- [ ] Load-bearing + cross-cutting + hard-to-reverse decisions extracted to ADR

### Retrofit honesty (retrofit only)

- [ ] `recovery_status: { overview: ... }` is `verified` or `unknown` only (HARD; schema enforces)
- [ ] Rationale fields `verified` or `unknown` only — never `reconstructed` (HARD)
- [ ] Every `reconstructed` field cites file/line/commit/schema evidence
- [ ] Every `unknown` paired with follow-up owner + action
- [ ] Gap Report populated (lost rationale, behavioural drift, missing ADRs, coverage gaps)
- [ ] No fabricated rationale (HARD)
- [ ] No laundered retrofit pattern (HARD)

### TestSpec traceability

- [ ] Every error-matrix row → test target ([ROBUSTNESS-TEST] or [NEEDS-TEST: ...])
- [ ] Every load-bearing postcondition → test target ([CONTRACT-TEST] or [ROBUSTNESS-TEST])
- [ ] Every load-bearing invariant → [PROPERTY-TEST] target

### Spec Ambiguity Test (meta-gate, override)

- [ ] Junior engineer can implement from this DD alone (with parent ARCH, governing ADRs, derived REQs)
- [ ] Test engineer can derive unit-test suite without seeing code
- [ ] Equivalent implementation in a different language satisfies the same DD

If any answer is No → DD fails regardless of other items.

---

## Canonical `check_failed` identifier catalog

### `check.shape.*` and `check.parent-architecture.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.shape.section-missing` | soft_reject | — |
| `check.shape.metadata-missing` | soft_reject | — |
| `check.shape.overview-thin` | soft_reject | — |
| `check.parent-architecture.missing` | **hard_reject** ★ (refusal B) | — |
| `check.parent-architecture.allocation-mismatch` | **hard_reject** ★ (refusal B) | — |
| `check.dd.cross-component-content` | **hard_reject** ★ (refusal B) | — |
| `check.derived-from.empty` | soft_reject | — |
| `check.derived-from.unresolvable` | **hard_reject** ★ (broken-reference) | — |

### `check.contract.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.contract.signature-only` | soft_reject | — |
| `check.contract.precondition-missing` | soft_reject | — |
| `check.contract.postcondition-success-missing` | soft_reject | — |
| `check.contract.postcondition-failure-branch-missing` | soft_reject | — |
| `check.contract.units-missing` | soft_reject | numeric parameters present |
| `check.contract.thread-safety-unstated` | soft_reject | — |
| `check.contract.nullability-unstated` | soft_reject | — |

### `check.data.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.data.fields-without-invariant` | soft_reject | — |
| `check.data.ownership-unstated` | soft_reject | — |
| `check.data.lifetime-unstated` | soft_reject | — |
| `check.data.returned-mutable-without-semantics` | soft_reject | crosses public interface |
| `check.data.shared-mutable-without-contract` | soft_reject | shared mutable present |
| `check.data.types-language-specific` | soft_reject | — |
| `check.data.invariant-untestable` | soft_reject | — |

### `check.algorithm.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.algorithm.contractual-without-reason` | soft_reject | algorithm named |
| `check.algorithm.too-vague` | soft_reject | — |
| `check.algorithm.pattern-mismatch` | soft_reject | — |
| `check.algorithm.decision-table-incomplete` | soft_reject | decision table present |
| `check.algorithm.sequence-without-invariants` | soft_reject | numbered sequence present |

### `check.state.*` and `check.thread-safety.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.state.absence-not-asserted` | soft_reject | stateless leaf |
| `check.state.transition-table-missing` | soft_reject | stateful leaf |
| `check.state.invariant-per-state-missing` | soft_reject | stateful leaf |
| `check.state.undefined-event-handling-unstated` | soft_reject | stateful leaf |
| `check.state.terminal-states-unstated` | soft_reject | stateful leaf |
| `check.thread-safety.leaf-category-unstated` | soft_reject | multi-threaded |
| `check.thread-safety.shared-field-without-contract` | soft_reject | shared mutable present |
| `check.timing.unmeasurable` | soft_reject | timing constraint present |

### `check.error.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.error.matrix-missing` | soft_reject | — |
| `check.error.six-questions-unanswered` | soft_reject | — |
| `check.error.recovery-not-named` | soft_reject | — |
| `check.error.unbounded-retry` | soft_reject | retry strategy present |
| `check.error.state-after-error-undefined` | soft_reject | stateful leaf |
| `check.error.inconsistent-form` | soft_reject | — |
| `check.error.mixed-domain-infrastructure` | soft_reject | — |
| `check.error.failure-side-effects-unstated` | info | — |

### `check.rationale.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.rationale.missing` | soft_reject | — |
| `check.rationale.constraint-kind-unnamed` | soft_reject | — |
| `check.rationale.generic-principle-invocation` | soft_reject (greenfield); **hard_reject** ★ (retrofit → `anti-pattern.fabricated-rationale`) | — |
| `check.rationale.adr-extraction-missed` | soft_reject | — |
| `check.rationale.unknown-without-followup` | soft_reject | — |

### `check.retrofit.*` (only when `recovery_status:` declared)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.recovery-status.overview-reconstructed` | **hard_reject** ★ (refusal A) | retrofit; schema-enforced |
| `check.retrofit.human-only-content-marked-reconstructed` | **hard_reject** ★ (refusal A) | retrofit |
| `check.retrofit.observed-not-marked-with-evidence` | soft_reject | retrofit |
| `check.retrofit.derived-from-vague` | soft_reject | retrofit |
| `check.retrofit.unknown-without-followup` | soft_reject | retrofit |
| `check.retrofit.gap-report-missing` | soft_reject | retrofit |

### `check.adr.*` and `check.traceability.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.adr.governing-not-resolved` | **hard_reject** ★ (broken-reference) | `governing_adrs:` non-empty |
| `check.adr.governing-not-cited-in-body` | soft_reject | `governing_adrs:` non-empty |
| `check.adr.body-citation-unresolved` | soft_reject | body cites ADR |
| `check.adr.needs-adr-stub-not-resolved` | soft_reject | `status: active` |
| `check.adr.inline-decision-should-be-extracted` | soft_reject | — |
| `check.traceability.error-row-not-tested` | soft_reject | — |
| `check.traceability.postcondition-without-test-target` | soft_reject | — |
| `check.traceability.invariant-without-property-test-target` | soft_reject | — |
| `check.traceability.test-target-undeliverable` | soft_reject | — |
| `check.traceability.needs-test-stub-in-finalised` | soft_reject | `status: active` |
| `check.traceability.test-without-dd-row` | info | TestSpec supplied |

### `check.spec-ambiguity-test.*` (meta-gate, override)

| Identifier | Severity | Verdict-precedence |
|---|---|---|
| `check.spec-ambiguity-test.fail` | **override** | DESIGN_ISSUE if upstream-traceable; REJECTED otherwise |

### `anti-pattern.*` — see `anti-patterns-catalog.md`

| Identifier | Severity |
|---|---|
| `anti-pattern.undefined-range-precondition` | soft_reject |
| `anti-pattern.implicit-unit` | soft_reject |
| `anti-pattern.implementation-leaking-interface` | soft_reject |
| `anti-pattern.silent-null-return` | soft_reject |
| `anti-pattern.algorithmic-postcondition` | **hard_reject** ★ (refusal C) |
| `anti-pattern.no-error-strategy` | soft_reject |
| `anti-pattern.exception-swallowing` | soft_reject |
| `anti-pattern.exception-tunneling` | soft_reject |
| `anti-pattern.designing-for-races` | soft_reject |
| `anti-pattern.state-explosion` | soft_reject |
| `anti-pattern.missing-cancellation` | soft_reject |
| `anti-pattern.llm-confident-invention` | soft_reject |
| `anti-pattern.code-paraphrase` | **hard_reject** ★ (refusal C) |
| `anti-pattern.test-as-spec-inversion` | soft_reject |
| `anti-pattern.happy-path-bias` | soft_reject |
| `anti-pattern.permutation-half-omitted` | **hard_reject** ★ (refusal C) |
| `anti-pattern.fabricated-rationale` | **hard_reject** ★ (refusal A; retrofit) |
| `anti-pattern.laundered-retrofit` | **hard_reject** ★ (refusal A; retrofit) |

★ marks hard-reject triggers (one occurrence rejects the document, except `check.spec-ambiguity-test.fail` which routes per precedence rule).

## Rule for new check identifiers

1. Pick a stable dotted name in the appropriate namespace.
2. Add it to this catalog with severity and conditional gating.
3. Add the rule to the appropriate `*-checks.md`.
4. Add a Yes/No item if it is checklist-shaped.

Do not invent ad-hoc `check_failed` strings during a review.
