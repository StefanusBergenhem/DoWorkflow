# Quality Bar gate — checklist + canonical check identifier catalog

> **Cap exception (~150-line soft cap):** This file is the single source of truth for the canonical `check.*` and `anti-pattern.*` catalog used across the entire skill. Splitting would fragment the catalog and break the convention that one file owns one identifier namespace. The file is structured as a TOC + per-section catalog tables for navigation; readers do not need to read end-to-end.

**Contents**
- [Quality Bar — Yes/No checklist](#quality-bar--yesno-checklist)
- [Canonical `check_failed` identifier catalog](#canonical-check_failed-identifier-catalog)
  - [`check.vocabulary.*`](#checkvocabulary)
  - [`check.type.*`](#checktype)
  - [`check.ears.*`](#checkears)
  - [`check.statement-quality.*`](#checkstatement-quality)
  - [`check.nfr.*`](#checknfr)
  - [`check.interface.*`](#checkinterface)
  - [`check.constraints.*`](#checkconstraints)
  - [`check.rationale.*`](#checkrationale)
  - [`check.traceability.*`](#checktraceability)
  - [`check.meta-gate.*`](#checkmeta-gate)
  - [`anti-pattern.*` (the 16)](#anti-pattern-the-16)
  - [`design-issue.*`](#design-issue)
- [Rule for new check identifiers](#rule-for-new-check-identifiers)

---

Two purposes in one file:
1. The Yes/No checklist that the review skill walks as Step 6 of orchestration. Every No is a finding.
2. The canonical catalog of `check_failed` identifiers used across the skill — the stable name space for findings.

## Quality Bar — Yes/No checklist

Walk every applicable item. Every No becomes a finding with the `check_failed` identifier listed in the catalog below.

### Vocabulary discipline

- [ ] Glossary section present and non-empty for non-trivial documents
- [ ] Every domain term used in a requirement statement is defined in the glossary
- [ ] One word per concept and one concept per word within this scope
- [ ] Generic placeholder terms (Manager, Service, Handler, Record, Processor) are audited out

### Requirement type clarity

- [ ] Every requirement is filed under the correct type section
- [ ] NFRs are in NFR form (not functional with an adjective); functional requirements are in functional form (not NFRs with smuggled behaviour)
- [ ] Level confusion has been audited — every requirement's scope matches this document's scope

### Statement-level quality

- [ ] Every statement is atomic (one `shall`, one behaviour)
- [ ] Every statement passes the box test (a tester can write the test from the statement and the glossary alone)
- [ ] Every statement is solution-free (no named technologies, frameworks, libraries, data structures, or algorithms — except externally imposed protocols in interface requirements)
- [ ] Every state-driven (`While …`) requirement is paired with its complementary out-of-state behaviour, or out-of-state is explicitly out of scope
- [ ] Structured language (EARS primary; GWT in `acceptance` blocks where useful) is used consistently rather than mixed within a statement

### NFR measurability — five-element rule

- [ ] Every NFR names the **system or subsystem** specifically
- [ ] Every NFR names the **response or behaviour** being measured
- [ ] Every NFR gives a **metric with unit**
- [ ] Every NFR gives a **target value** at the correct statistical level (percentile where applicable, not raw mean)
- [ ] Every NFR states the **condition** (load, environment, operating mode, measurement point)
- [ ] For tiered NFRs, the Planguage form (scale, meter, fail, goal, stretch, wish) is used rather than a single flat target

### Interface contract completeness

- [ ] Every interface requirement specifies protocol, message structure, timing, error handling, startup/initial state
- [ ] Pre/post-conditions and invariants are stated for each externally callable operation
- [ ] A versioning and deprecation policy is stated for every versioned interface
- [ ] Externally imposed protocols are cited by specification (RFC, OIDC version, draft) rather than informal name

### Constraints discipline

- [ ] Every inherited constraint cites its source
- [ ] Every inherited constraint names the cost of relaxing it
- [ ] Every inherited constraint is categorised
- [ ] Constraints with behavioural consequences have derived requirements authored and cross-linked

### Rationale — no fabrication

- [ ] Every requirement carries a `rationale` field with non-trivial content, OR explicit `pending`/`unknown` marker
- [ ] Rationale is captured at decision time (or marked `unknown` for retrofit when original reasoning is lost)
- [ ] Rationale in retrofit mode is marked `verified` or `unknown` only — **never** `reconstructed` (HARD REJECT)
- [ ] Rationales are audited for circularity (not "because test T passes") and laundering (not "because the current design is right")

### Traceability completeness

- [ ] Every requirement has non-empty `derived_from`
- [ ] Every derived requirement (those flagged `derivation: derived`) cites the introducing decision
- [ ] Every governing decision is referenced by at least one requirement or derived requirement

### Inspection discipline (informational)

- [ ] Document has been read from at least the Designer and Tester perspectives before delivery
- [ ] Where a stakeholder-facing outcome is at stake, the User/Stakeholder perspective has also been read

### Retrofit honesty (retrofit mode only)

- [ ] Behaviour fields are marked `verified` or `reconstructed`
- [ ] Rationale, open-alternatives, and intent fields are marked `verified` or `unknown` only
- [ ] Every `unknown` field has a follow-up owner and action queued
- [ ] `derived_from` links point to *observed evidence* (file:line, commit, log), not fabricated artifact names

### Spec Ambiguity Test (meta-gate)

- [ ] Could a junior engineer or mid-tier AI, reading only this document plus its glossary and governing decisions, derive a defensible architecture allocation, detailed design, and test specification — **without asking clarifying questions**?

When meta-gate Spec-Ambiguity-Test answer is No: emit `check.meta-gate.spec-ambiguity-test-fails` with severity `hard_reject`. Verdict is REJECTED regardless of other counts.

---

## Canonical `check_failed` identifier catalog

Stable identifiers used across all findings. Three namespaces:
- `check.<area>.<specific>` — structural / completeness checks
- `anti-pattern.<name>` — the 16 catalogued anti-patterns
- `design-issue.<specific>` — DESIGN_ISSUE triggers (problem is upstream)

### `check.vocabulary.*`

| Identifier | Severity |
|---|---|
| `check.vocabulary.glossary-missing` | soft_reject |
| `check.vocabulary.term-undefined` | soft_reject |
| `check.vocabulary.placeholder-term-used` | soft_reject |
| `check.vocabulary.synonym-drift` | soft_reject |
| `check.vocabulary.glossary-term-unused` | info |

### `check.type.*`

| Identifier | Severity |
|---|---|
| `check.type.misclassified` | soft_reject |
| `check.type.nfr-as-functional` | soft_reject |
| `check.type.level-confusion` | soft_reject |
| `check.type.section-missing` | info |

### `check.ears.*`

| Identifier | Severity |
|---|---|
| `check.ears.invalid-pattern` | soft_reject |
| `check.ears.compound-too-many-keywords` | soft_reject |
| `check.ears.compound-out-of-order` | soft_reject |
| `check.ears.mixed-structured-forms` | soft_reject |
| `check.ears.non-conformant` | soft_reject |

### `check.statement-quality.*`

| Identifier | Severity |
|---|---|
| `check.statement-quality.box-test-fails` | soft_reject |
| `check.statement-quality.state-driven-no-complement` | soft_reject |

### `check.nfr.*`

| Identifier | Severity |
|---|---|
| `check.nfr.missing-system` | soft_reject |
| `check.nfr.missing-response` | soft_reject |
| `check.nfr.missing-metric-unit` | soft_reject |
| `check.nfr.missing-target` | soft_reject |
| `check.nfr.missing-statistical-level` | soft_reject |
| `check.nfr.missing-condition` | soft_reject |
| `check.nfr.planguage-missing-scale` | soft_reject |
| `check.nfr.planguage-missing-meter` | soft_reject |
| `check.nfr.planguage-missing-goal-or-fail` | info |
| `check.nfr.mixed-forms` | soft_reject |

### `check.interface.*`

| Identifier | Severity |
|---|---|
| `check.interface.missing-protocol` | soft_reject |
| `check.interface.missing-message-structure` | soft_reject |
| `check.interface.missing-timing` | soft_reject |
| `check.interface.missing-error-handling` | soft_reject |
| `check.interface.missing-startup-state` | soft_reject |
| `check.interface.missing-precondition` | soft_reject |
| `check.interface.missing-postcondition` | soft_reject |
| `check.interface.missing-invariants` | soft_reject |
| `check.interface.missing-versioning` | soft_reject |
| `check.interface.protocol-by-informal-name` | info |

### `check.constraints.*`

| Identifier | Severity |
|---|---|
| `check.constraints.missing-source` | soft_reject |
| `check.constraints.missing-cost-of-relaxing` | soft_reject |
| `check.constraints.missing-category` | soft_reject |
| `check.constraints.no-derived-requirements` | soft_reject |

### `check.rationale.*`

| Identifier | Severity |
|---|---|
| `check.rationale.missing` | **hard_reject** ★ |
| `check.rationale.recovery-status-reconstructed` | **hard_reject** ★ |
| `check.rationale.derived-no-flag` | soft_reject |
| `check.rationale.derived-no-decision-cited` | soft_reject |

### `check.traceability.*`

| Identifier | Severity |
|---|---|
| `check.traceability.derived-from-empty` | soft_reject |
| `check.traceability.derived-from-vague` | soft_reject (or hard_reject in retrofit) |
| `check.traceability.governing-decision-not-referenced` | info or soft_reject |

### `check.meta-gate.*`

| Identifier | Severity |
|---|---|
| `check.meta-gate.spec-ambiguity-test-fails` | **hard_reject** ★ |

### `anti-pattern.*` (the 16)

| Identifier | Severity |
|---|---|
| `anti-pattern.compound` | soft_reject |
| `anti-pattern.vague-adjective` | soft_reject |
| `anti-pattern.passive-no-actor` | soft_reject |
| `anti-pattern.unbounded-list` | soft_reject |
| `anti-pattern.unbounded-negative` | soft_reject |
| `anti-pattern.implementation-prescription` | **hard_reject** ★ |
| `anti-pattern.wishful-thinking` | soft_reject |
| `anti-pattern.ambiguous-pronoun` | soft_reject |
| `anti-pattern.tbd-marker` | soft_reject |
| `anti-pattern.fabricated-rationale` | **hard_reject** ★ |
| `anti-pattern.ears-cargo-cult` | soft_reject |
| `anti-pattern.requirements-smuggling-design` | **hard_reject** ★ (alias of implementation-prescription) |
| `anti-pattern.code-driven-fabrication` | soft_reject |
| `anti-pattern.level-confusion` | soft_reject |
| `anti-pattern.test-as-requirement-inversion` | soft_reject |
| `anti-pattern.laundering-current-state` | soft_reject |

### `design-issue.*`

| Identifier | Verdict trigger |
|---|---|
| `design-issue.parent-allocation-contradicts-decision` | DESIGN_ISSUE |
| `design-issue.product-brief-outcome-not-derivable` | DESIGN_ISSUE |
| `design-issue.missing-governing-decision` | DESIGN_ISSUE |
| `design-issue.derived-from-cites-nonexistent-artifact` | DESIGN_ISSUE |

★ marks the five hard-reject triggers (one occurrence rejects the document).

## Rule for new check identifiers

When a check is added (e.g., a new anti-pattern is recognised, a new NFR element-presence sub-check is introduced):

1. Pick a stable dotted name in the appropriate namespace.
2. Add it to this catalog with severity.
3. Add the rule to the appropriate reference file (`*.md` in `references/`).
4. Add a row to the Quality Bar checklist above if it is a Yes/No item.

Do not invent ad-hoc `check_failed` strings during a review — every finding's identifier must appear in this catalog.
