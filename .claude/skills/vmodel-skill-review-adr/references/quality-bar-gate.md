# Quality Bar gate — checklist + canonical check identifier catalog

> **Cap-exception note.** This file is the canonical single source of truth for all `check.*` identifiers (35) used skill-wide. The ~150-line soft cap from the builder skill anti-patterns is intentionally exceeded here because catalog density beats split-file lookup. Per the architecture-pair precedent (2026-04-30) and the testspec-pair precedent (2026-05-01), this exception is documented in-place rather than refactored.

Two purposes in one file:
1. Yes/No checklist the review skill walks. Every No → finding.
2. Canonical catalog of `check_failed` identifiers — stable name space for findings.

## Table of contents

- [Quality Bar — Yes/No checklist](#quality-bar--yesno-checklist) — walk per sweep
- [Canonical `check_failed` identifier catalog](#canonical-check_failed-identifier-catalog) — IDs, severities, gating
- [Rule for new check identifiers](#rule-for-new-check-identifiers) — extension protocol

## Quality Bar — Yes/No checklist

### Shape and front-matter

- [ ] Required front-matter fields all present (`id`, `artifact_type`, `status`, `scope_tags`, `date`) — HARD if any missing
- [ ] `id` matches `ADR-\d{3,}-[a-z0-9]+(-[a-z0-9]+)*` — HARD if not
- [ ] `status` in the enum `proposed | accepted | superseded` — HARD if not
- [ ] Body sections present: Context / Decision / Alternatives / Rationale / Consequences (Decision and Consequences absences are HARD per refusal C)
- [ ] Body sections in canonical order

### Threshold

- [ ] Decision is load-bearing
- [ ] At least two real options were on the table
- [ ] Decision is contingent on assumptions or drivers that may change
- [ ] Decision is not a routine choice (naming, imports, method signatures) — HARD if it is

### Context completeness

- [ ] Context describes the specific situation that forced the decision (not a generic problem-domain statement)
- [ ] Forces — constraints, deadlines, dependencies — named
- [ ] Drivers explicit so Rationale can cite them by name
- [ ] Assumptions enumerated as revisit triggers, not buried in prose

### Option-space integrity (refusal C lives here)

- [ ] At least two real alternatives listed — HARD ("do nothing" and straw men do not count)
- [ ] Each alternative has a concrete, context-specific rejection reason
- [ ] No straw man alternatives
- [ ] Single-option smell test: would any rejected alternative have produced a materially different design?

### Decision clarity

- [ ] Decision section present and non-empty — HARD (refusal C)
- [ ] Decision in active voice with the chosen option named
- [ ] Rationale cites drivers from this ADR by name (not generic praise)

### Consequences and Reversibility (refusals B and C live here)

- [ ] Consequences section present — HARD (refusal C)
- [ ] Both positive and negative subsections non-empty — HARD if both empty (refusal C)
- [ ] Positives listed
- [ ] Negatives listed and concrete (not "some additional complexity")
- [ ] Reversibility verbatim prompt present and answered — HARD if unanswered (refusal B)
- [ ] Reversibility answer not hedged ("partially", "somewhat", "depends" without separating parts) — HARD if hedged (refusal B)
- [ ] If reversible: rollback path stated
- [ ] If irreversible: recovery plan stated AND named human signoff

### Linkage and lineage

- [ ] `scope_tags` non-empty — HARD (also schema-enforced; refusal: orphan-ADR)
- [ ] If `supersedes` set: predecessor's `superseded_by` points back; if `superseded_by` set: successor's `supersedes` points back
- [ ] `supersedes` and `superseded_by` not both set on the same ADR
- [ ] `affected_scopes` set if reach extends beyond `scope_tags`
- [ ] Back-resolution flag: at least one citing artifact references this ADR via `governing_adrs:` OR a propagation requirement materialised at the ADR's scope (flag-not-scan)

### Propagation and completeness

- [ ] Each consequence has a propagation route (new requirement at this scope OR `governing_adrs` from child OR both)
- [ ] No orphan consequence (testable here but no requirement; bounds children but no `governing_adrs`)

### Retrofit honesty (only when `recovery_status:` declared)

- [ ] No `recovery_status: reconstructed` on `context | alternatives_considered | rationale | consequences` — HARD (refusal A; also schema-banned)
- [ ] No AI-fabricated content in human-only fields (no preserved conversation/archive/decider) — HARD (refusal A)

### Spec Ambiguity Test (meta-gate, override)

- [ ] A junior engineer or low-mid-tier AI, reading only this ADR, can derive the same design without guessing

If any meta-gate answer is No → ADR fails regardless of other items (verdict per precedence).

---

## Canonical `check_failed` identifier catalog

### `check.front-matter.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.front-matter.required-field-missing` | **hard_reject** ★ (broken-reference) | — |
| `check.front-matter.id-pattern-invalid` | **hard_reject** ★ (broken-reference) | — |

### `check.status.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.status.invalid-lifecycle-state` | **hard_reject** ★ (broken-reference) | — |

### `check.body.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.body.canonical-section-missing` | soft_reject | — |
| `check.body.section-ordering` | info | — |

### `check.threshold.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.threshold.routine-choice` | **hard_reject** ★ (refusal E; aliases `anti-pattern.routine-choice`) | — |
| `check.threshold.condition-load-bearing-missing` | soft_reject | — |
| `check.threshold.condition-options-missing` | soft_reject | — |
| `check.threshold.condition-contingency-missing` | soft_reject | — |

### `check.context.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.context.generic-problem-statement` | soft_reject | — |
| `check.context.forces-not-named` | soft_reject | — |
| `check.context.drivers-implicit` | soft_reject | — |

### `check.alternatives.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.alternatives.fewer-than-two-real` | **hard_reject** ★ (refusal C; aliases `anti-pattern.single-option`) | — |
| `check.alternatives.rejection-not-context-specific` | soft_reject | — |
| `check.alternatives.straw-man` | soft_reject | — |

### `check.decision.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.decision.section-missing-or-empty` | **hard_reject** ★ (refusal C) | — |
| `check.decision.passive-or-unnamed-option` | soft_reject | — |

### `check.rationale.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.rationale.generic-praise` | soft_reject (aliases `anti-pattern.generic-justification`) | — |

### `check.consequences-discipline.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.consequences-discipline.section-missing` | **hard_reject** ★ (refusal C) | — |
| `check.consequences-discipline.both-signs-empty` | **hard_reject** ★ (refusal C) | — |
| `check.consequences-discipline.positives-missing` | soft_reject | section present |
| `check.consequences-discipline.negatives-missing` | soft_reject | section present |
| `check.consequences-discipline.negatives-handwave` | soft_reject | section present |
| `check.consequences-discipline.reversibility-unanswered` | **hard_reject** ★ (refusal B; aliases `anti-pattern.missing-reversibility`) | — |
| `check.consequences-discipline.reversibility-hedged` | **hard_reject** ★ (refusal B) | — |
| `check.consequences-discipline.reversibility-rollback-missing` | soft_reject | answered "yes" |
| `check.consequences-discipline.reversibility-signoff-missing` | soft_reject | answered "no" |

### `check.linkage.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.linkage.scope-tags-empty` | **hard_reject** ★ (broken-reference; aliases `anti-pattern.orphan-adr`; schema-enforced) | — |
| `check.linkage.supersession-chain-broken` | soft_reject | `supersedes` or `superseded_by` set |
| `check.linkage.both-supersedes-and-superseded-set` | soft_reject | both fields set |
| `check.linkage.affected-scopes-omitted` | info | — |
| `check.linkage.governing-adrs-back-resolution-flag` | soft_reject (flag-not-scan) | status `accepted` |

### `check.completeness.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.completeness.consequence-orphan-suspected` | soft_reject (flag-not-scan) | — |

### `check.propagation.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.propagation.testable-consequence-no-requirement` | soft_reject (flag-not-scan) | — |
| `check.propagation.child-bound-no-governing-link` | soft_reject (flag-not-scan) | — |

### `check.retrofit-honesty.*` (only when `recovery_status:` declared)

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.retrofit-honesty.reconstructed-on-human-only` | **hard_reject** ★ (refusal A; schema-banned) | retrofit |
| `check.retrofit-honesty.fabricated-content` | **hard_reject** ★ (refusal A; aggregator for anti-patterns 7/8/9) | retrofit |

### `check.y-statement.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.y-statement.shape-malformed` | soft_reject | Y-statement present |

### `check.immutability.*`

| Identifier | Severity | Conditional gating |
|---|---|---|
| `check.immutability.body-edit-on-accepted` | info | status `accepted` or `superseded` |

### `check.spec-ambiguity-test.*` (meta-gate, override)

| Identifier | Severity | Verdict-precedence |
|---|---|---|
| `check.spec-ambiguity-test.fail` | **override** | DESIGN_ISSUE if upstream-traceable; REJECTED otherwise |

### `anti-pattern.*` — see `anti-patterns-catalog.md`

| Identifier | Severity |
|---|---|
| `anti-pattern.single-option` | **hard_reject** ★ (refusal C) |
| `anti-pattern.generic-justification` | soft_reject |
| `anti-pattern.missing-negatives` | soft_reject |
| `anti-pattern.buried-assumptions` | soft_reject |
| `anti-pattern.post-hoc-rationalisation` | soft_reject |
| `anti-pattern.routine-choice` | **hard_reject** ★ (refusal E) |
| `anti-pattern.test-as-requirement-inversion` | **hard_reject** ★ (refusal A; retrofit) |
| `anti-pattern.llm-confident-invention` | **hard_reject** ★ (refusal A; retrofit) |
| `anti-pattern.laundering-current-state` | **hard_reject** ★ (refusal A; retrofit) |
| `anti-pattern.missing-reversibility` | **hard_reject** ★ (refusal B) |
| `anti-pattern.orphan-adr` | **hard_reject** ★ (broken-reference; aliases `check.linkage.scope-tags-empty`) |

★ marks hard-reject triggers (one occurrence rejects, except `check.spec-ambiguity-test.fail` which routes per precedence — DESIGN_ISSUE when upstream-traceable, REJECTED otherwise).

## Rule for new check identifiers

1. Pick a stable dotted name in the appropriate namespace.
2. Add it to this catalog with severity and conditional gating.
3. Add the rule to the appropriate `*-checks.md`.
4. Add a Yes/No item if it is checklist-shaped.

Do not invent ad-hoc `check_failed` strings during a review.
