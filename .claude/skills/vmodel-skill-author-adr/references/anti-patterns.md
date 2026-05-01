# Anti-patterns and Quality Bar self-checklist

> **Cap-exception note.** This file is the canonical single source of truth for all ADR `anti-pattern.*` and `check.*` identifiers on the author side. The ~150-line soft cap from the builder skill anti-patterns is intentionally exceeded here because catalog density beats split-file lookup. Per PHASE5 §1 (architecture-pair precedent 2026-04-30), the exception is documented in-place rather than refactored.

Single source of truth on the author side for all ADR `anti-pattern.*` IDs (11) and `check.*` IDs (35) — 46 total. Sweep before delivering. Hard-rejects marked ★ with refusal letter (A/B/C/D/E from `SKILL.md`).

## Contents

- [Anti-patterns (11)](#anti-patterns-11) — single-option, generic justification, missing negatives, buried assumptions, post-hoc, routine-choice, retrofit triple, missing reversibility, orphan
- [Checks by group](#checks-by-group) — context, alternatives, decision, consequences, linkage, completeness, propagation, retrofit, threshold, shape, SAT, Y-statement, immutability
- [Quality Bar self-checklist](#quality-bar-self-checklist) — Yes/No groups
- [Hard-reject mapping](#hard-reject-mapping)

## Anti-patterns (11)

### 1. Single-option ADR ★ (refusal C) — `anti-pattern.single-option`
Tell: alternatives section is empty, absent, or filled with straw men ("do nothing"). One option + justification = description, not a decision.
Fix: list ≥2 real alternatives, each with a concrete rejection reason. See `alternatives-discipline.md`.

### 2. Generic justification — `anti-pattern.generic-justification`
Tell: rationale reads "more flexible", "more modern", "industry standard", "best practice"; same sentence pasteable into 20 unrelated ADRs.
Fix: cite the drivers from this ADR's Context, by name. See `decision-and-rationale.md`.

### 3. Missing negatives — `anti-pattern.missing-negatives`
Tell: Consequences are upside-only; the cost side is empty or hand-waved ("some additional complexity").
Fix: list ≥1 concrete negative; replace handwave with a measurable cost or threshold. See `consequences-and-reversibility.md`.

### 4. Buried assumptions — `anti-pattern.buried-assumptions`
Tell: assumptions implicit in Context prose; no enumerated revisit triggers; no place a reader can point to and say "if this changes, revisit."
Fix: enumerate assumptions in Context. Each is one sentence answering "if this changes, revisit." See `context-and-drivers.md`.

### 5. Post-hoc rationalisation — `anti-pattern.post-hoc-rationalisation`
Tell: rejected alternatives all reduce to the same design as the chosen one — they were never on the table. ADR drafted after the design.
Fix: capture-then-design — author the ADR while the option space is still open. See `adr-purpose-and-shape.md`.

### 6. ADRs for routine choices ★ (refusal E) — `anti-pattern.routine-choice`
Tell: ADR for a naming convention, import path, or method signature; >1–2 ADRs per scope per sprint in steady state.
Fix: note the choice inline in Architecture or Detailed Design rationale; do not promote to ADR. See `adr-purpose-and-shape.md`.

### 7. Test-as-requirement inversion (retrofit) ★ (refusal A) — `anti-pattern.test-as-requirement-inversion`
Tell: rationale paraphrases a test expectation; Context describes no forces; ADR back-derived from a characterization test.
Fix: refuse to author. Mark `recovery_status: unknown` on rationale and human-only fields; record only the observable decision. See `retrofit-discipline.md`.

### 8. LLM confident invention (retrofit) ★ (refusal A) — `anti-pattern.llm-confident-invention`
Tell: committee-style prose despite no preserved conversation, archive, or accessible deciders; rejection reasons too tidy.
Fix: refuse to fabricate. Set human-only fields to `unknown`; record only what code shows. See `retrofit-discipline.md`.

### 9. Laundering current state (retrofit) ★ (refusal A) — `anti-pattern.laundering-current-state`
Tell: every alternative rejected for a property the current design has; ADR reads as post-hoc defence of the present design.
Fix: refuse. Mark alternatives `unknown`; do not invent rejection reasons that align with current code. See `retrofit-discipline.md`.

### 10. Missing Reversibility answer ★ (refusal B) — `anti-pattern.missing-reversibility`
Tell: Reversibility sub-prompt absent, acknowledged with stock phrase, or hedged ("partially reversible", "somewhat", "depends").
Fix: answer the prompt. Reversible → rollback path. Irreversible → recovery plan + named sign-off. Hedged → split the parts. See `consequences-and-reversibility.md`.

### 11. Orphan ADR ★ (refusal C/E) — `anti-pattern.orphan-adr`
Tell: `scope_tags` empty AND no artifact references the ADR via `governing_adrs:`. Decision exists nowhere downstream.
Fix: set `scope_tags`; ensure at least one citing artifact OR a propagated requirement at the ADR's scope. See `propagation-and-completeness.md`, `canonical-fields-and-body.md`.

## Checks by group

### Context-completeness
- `check.context.generic-problem-statement` — soft. Generic statement of the problem domain rather than the specific situation that forced the decision.
- `check.context.forces-not-named` — soft. Forces (constraints, deadlines, dependencies) not named.
- `check.context.drivers-implicit` — soft. Drivers are not explicit; rationale cannot cite them by name.

### Option-space-integrity
- `check.alternatives.fewer-than-two-real` — ★ hard_reject (refusal C). Fewer than two real alternatives.
- `check.alternatives.rejection-not-context-specific` — soft. Rejection reason generic, not anchored to this ADR's drivers.
- `check.alternatives.straw-man` — soft. Listed alternative was never on the table; rejection reason trivial.

### Decision-clarity
- `check.decision.section-missing-or-empty` — ★ hard_reject (refusal C). Decision section absent or empty.
- `check.decision.passive-or-unnamed-option` — soft. Passive voice; chosen option not named.
- `check.rationale.generic-praise` — soft. Rationale cites no driver from Context; reads as generic option praise.

### Consequences-discipline
- `check.consequences-discipline.section-missing` — ★ hard_reject (refusal C). Consequences section absent.
- `check.consequences-discipline.both-signs-empty` — ★ hard_reject (refusal C). Both positive and negative subsections empty.
- `check.consequences-discipline.positives-missing` — soft. Positive list absent or empty.
- `check.consequences-discipline.negatives-missing` — soft. Negative list absent or empty.
- `check.consequences-discipline.negatives-handwave` — soft. Negatives vague ("some additional complexity"); not concrete.
- `check.consequences-discipline.reversibility-unanswered` — ★ hard_reject (refusal B). Reversibility prompt missing or acknowledged only.
- `check.consequences-discipline.reversibility-hedged` — ★ hard_reject (refusal B). "Partially", "somewhat", "depends" without separating parts.
- `check.consequences-discipline.reversibility-rollback-missing` — soft. Reversible = yes but no rollback path stated.
- `check.consequences-discipline.reversibility-signoff-missing` — soft. Irreversible but no named signoff.

### Linkage
- `check.linkage.scope-tags-empty` — ★ hard_reject (refusal C/E; also schema-enforced via `minItems: 1`). `scope_tags` empty.
- `check.linkage.supersession-chain-broken` — soft. `supersedes` set but predecessor's `superseded_by` does not point back; or vice versa.
- `check.linkage.both-supersedes-and-superseded-set` — soft. Same ADR claims both directions; structural inconsistency.
- `check.linkage.affected-scopes-omitted` — info. Decision text suggests cross-scope reach but `affected_scopes` empty/absent.
- `check.linkage.governing-adrs-back-resolution-flag` — soft. ADR `accepted` but no citing artifact references via `governing_adrs:` AND no propagation requirement materialised at the scope. Flag-not-scan.

### Completeness-rule
- `check.completeness.consequence-orphan-suspected` — soft. Stated consequence appears testable at this scope but no co-located requirement AND no child artifact bound by `governing_adrs`. Flag-not-scan.

### Propagation-rule
- `check.propagation.testable-consequence-no-requirement` — soft. Consequence testable at this layer but no new requirement materialised.
- `check.propagation.child-bound-no-governing-link` — soft. Consequence bounds child design choices but no child carries `governing_adrs: [<this-ADR>]`.

### Retrofit-honesty
- `check.retrofit-honesty.reconstructed-on-human-only` — ★ hard_reject (refusal A; schema-banned). `recovery_status: reconstructed` set on `context`, `alternatives_considered`, `rationale`, or anticipated `consequences`.
- `check.retrofit-honesty.fabricated-content` — ★ hard_reject (refusal A). AI-fabricated content in human-only fields with no preserved conversation/archive/decider; aggregator alias for anti-patterns 7/8/9.

### Threshold
- `check.threshold.routine-choice` — ★ hard_reject (refusal E; alias for `anti-pattern.routine-choice`).
- `check.threshold.condition-load-bearing-missing` — soft. Decision is not load-bearing; failing a change does not break the system.
- `check.threshold.condition-options-missing` — soft. Only one real option was on the table.
- `check.threshold.condition-contingency-missing` — soft. Decision not contingent on anything that may change; no revisit trigger possible.

### Front-matter and body shape
- `check.front-matter.required-field-missing` — ★ hard_reject. `id`, `artifact_type`, `status`, `scope_tags`, or `date` missing.
- `check.front-matter.id-pattern-invalid` — ★ hard_reject. `id` does not match `ADR-\d{3,}-[a-z0-9]+(-[a-z0-9]+)*`.
- `check.status.invalid-lifecycle-state` — ★ hard_reject. `status` not in `proposed|accepted|superseded`.
- `check.body.canonical-section-missing` — soft. One of Context / Decision / Alternatives / Rationale / Consequences absent (Decision and Consequences also map to hard-rejects above).
- `check.body.section-ordering` — info. Sections present but out of canonical order.

### Spec Ambiguity Test (override)
- `check.spec-ambiguity-test.fail` — ★ override (refusal D). Junior engineer or low-mid AI cannot derive the design from this ADR without guessing. Beats every other Yes.

### Y-statement
- `check.y-statement.shape-malformed` — soft. Y-statement present but not in canonical "In the context of … facing … we decided … to achieve … accepting …" form.

### Immutability
- `check.immutability.body-edit-on-accepted` — info. Status `accepted` or `superseded` but body shows evidence of edit beyond status/`superseded_by` front-matter.

## Quality Bar self-checklist

Sweep all groups before delivering. Any No is flagged inline; do not silently pass.

**Context completeness.** Specific situation (not generic problem domain)? Forces named? Drivers explicit so the rationale can cite by name?

**Option space integrity.** ≥2 real alternatives? Each with a concrete, context-specific rejection reason? No single-option smell — would any rejected option have produced a materially different design?

**Decision clarity.** Active voice with chosen option named? Rationale cites the drivers from this ADR by name?

**Consequences discipline.** Positives listed? Negatives listed, non-empty, concrete? Reversibility sub-prompt answered with rollback path (reversible) or recovery plan + named sign-off (irreversible)?

**Linkage.** `scope_tags` non-empty? Supersession chain intact in both directions if used? `affected_scopes` set when reach exceeds `scope_tags`?

**Completeness rule.** Every consequence either satisfied by a requirement at the ADR's scope or referenced by a child artifact via `governing_adrs:`? No orphan consequences?

**Propagation rule.** For consequences testable at this layer: a new requirement materialised? For consequences that bound child choices: relevant child artifacts linked via `governing_adrs`?

**Retrofit honesty.** If retrofit and rationale lost: `recovery_status: unknown` on Context, Alternatives, Rationale (and anticipated Consequences)? All fields free of AI-fabricated content?

**Spec Ambiguity Test (override).** Could a junior engineer or low-mid-tier AI, reading only this ADR, derive the same design from it without guessing?

## Hard-reject mapping

| Trigger | Refusal | Severity |
|---|---|---|
| `check.alternatives.fewer-than-two-real`, `anti-pattern.single-option` | C | hard_reject |
| `check.decision.section-missing-or-empty` | C | hard_reject |
| `check.consequences-discipline.section-missing`, `.both-signs-empty` | C | hard_reject |
| `check.consequences-discipline.reversibility-unanswered`, `.reversibility-hedged`, `anti-pattern.missing-reversibility` | B | hard_reject |
| `check.retrofit-honesty.reconstructed-on-human-only`, `.fabricated-content`, `anti-pattern.test-as-requirement-inversion`, `.llm-confident-invention`, `.laundering-current-state` | A | hard_reject |
| `check.threshold.routine-choice`, `anti-pattern.routine-choice` | E | hard_reject |
| `check.linkage.scope-tags-empty`, `anti-pattern.orphan-adr` | C/E | hard_reject |
| `check.front-matter.required-field-missing`, `.id-pattern-invalid`, `check.status.invalid-lifecycle-state` | shape | hard_reject |
| `check.spec-ambiguity-test.fail` | D | override |

All other `check.*` IDs are soft-reject (accumulate to a single REJECTED verdict) or info.

## Cross-link

`adr-purpose-and-shape.md` (1, 5, 6, threshold) · `canonical-fields-and-body.md` (front-matter and body shape) · `context-and-drivers.md` (4, context group) · `alternatives-discipline.md` (1, 5, option-space group) · `decision-and-rationale.md` (2, decision group) · `consequences-and-reversibility.md` (3, 10, consequences group) · `propagation-and-completeness.md` (completeness, propagation groups) · `immutability-and-supersession.md` (linkage, immutability) · `extraction-cues.md` (origin) · `retrofit-discipline.md` (7, 8, 9, retrofit group)
