# Anti-patterns catalog — sweep targets

Eleven failure modes. Each has a tell, a `check_failed` identifier, a severity, and a generic `recommended_action`. Walk every section and front-matter element through this catalog. Every hit becomes a finding.

Hard-reject triggers marked ★. Items 1, 6, 7, 8, 9, 10 are hard refusals (C / E / A / A / A / B). Item 11 is hard via the broken-reference integrity class.

## Table of contents

- [Threshold and signal-to-noise (1)](#threshold-and-signal-to-noise-1) — **routine-choice** ★
- [Decision and rationale discipline (2)](#decision-and-rationale-discipline-2) — **single-option** ★, generic-justification
- [Consequences discipline (2)](#consequences-discipline-2) — missing-negatives, **missing-reversibility** ★
- [Context and assumptions discipline (2)](#context-and-assumptions-discipline-2) — buried-assumptions, post-hoc-rationalisation
- [Retrofit discipline (3)](#retrofit-discipline-3) — **test-as-requirement-inversion** ★, **llm-confident-invention** ★, **laundering-current-state** ★
- [Linkage integrity (1)](#linkage-integrity-1) — **orphan-adr** ★
- [Sweep order](#sweep-order)
- [Aggregation rule](#aggregation-rule)

## Threshold and signal-to-noise (1)

### 6. Routine-choice ADR (HARD ★ refusal E)

- **Tell**: ADR for naming convention, import organisation, method signature, directory layout; >1–2 ADRs per scope per sprint in steady state
- **check_failed**: `anti-pattern.routine-choice` (aliases `check.threshold.routine-choice`)
- **severity**: `hard_reject` ★ (refusal E)
- **recommended_action**: *"Drop the ADR; record the choice inline in Architecture or Detailed Design. See `references/adr-purpose-and-shape-checks.md`."*

## Decision and rationale discipline (2)

### 1. Single-option ADR (HARD ★ refusal C)

- **Tell**: one option listed plus justification; alternatives section empty, absent, or filled with straw men ("do nothing")
- **check_failed**: `anti-pattern.single-option` (aliases `check.alternatives.fewer-than-two-real`)
- **severity**: `hard_reject` ★ (refusal C)
- **evidence pattern**: quote the alternatives section verbatim; cite refusal C
- **recommended_action**: *"Surface ≥2 real alternatives with concrete context-specific rejection reasons, or drop the ADR. See `references/alternatives-checks.md`."*

### 2. Generic justification

- **Tell**: rationale reads "more flexible", "more modern", "industry standard", "best practice"; the same paragraph would fit unchanged into twenty unrelated ADRs
- **check_failed**: `anti-pattern.generic-justification` (aliases `check.rationale.generic-praise`)
- **severity**: `soft_reject`
- **recommended_action**: *"Rewrite Rationale citing the drivers from this ADR by name. See `references/decision-rationale-checks.md`."*

## Consequences discipline (2)

### 3. Missing negatives

- **Tell**: Consequences list only upsides; negatives empty or hand-waved ("some additional complexity")
- **check_failed**: `anti-pattern.missing-negatives` (aliases `check.consequences-discipline.negatives-missing` / `negatives-handwave`)
- **severity**: `soft_reject`
- **recommended_action**: *"Replace handwave with a concrete cost (latency, ops surface, vendor coupling, throughput ceiling). See `references/consequences-and-reversibility-checks.md`."*

### 10. Missing Reversibility answer (HARD ★ refusal B)

- **Tell**: Reversibility sub-prompt unanswered, acknowledged with stock phrase, hedged ("partially reversible", "somewhat reversible", "depends" without separating parts), reversible-without-rollback-path, or irreversible-without-named-signoff
- **check_failed**: `anti-pattern.missing-reversibility` (aliases `check.consequences-discipline.reversibility-unanswered` / `reversibility-hedged`)
- **severity**: `hard_reject` ★ (refusal B)
- **evidence pattern**: quote the Reversibility paragraph verbatim; cite refusal B
- **recommended_action**: *"Answer the Reversibility prompt directly: yes → state rollback path; no → state recovery plan and named signoff. See `references/consequences-and-reversibility-checks.md`."*

## Context and assumptions discipline (2)

### 4. Buried assumptions

- **Tell**: assumptions implicit in Context prose; no place a future maintainer can point to and say "if this changes, revisit"
- **check_failed**: `anti-pattern.buried-assumptions`
- **severity**: `soft_reject`
- **recommended_action**: *"Enumerate assumptions as a separate list of revisit triggers. See `references/context-and-drivers-checks.md`."*

### 5. Post-hoc rationalisation

- **Tell**: design drafted first, ADR written afterwards; rejected alternatives all reduce to the chosen option (no materially different design would have resulted)
- **check_failed**: `anti-pattern.post-hoc-rationalisation`
- **severity**: `soft_reject`
- **recommended_action**: *"Re-derive ADR from the actual decision conversation; if no conversation existed, mark as retrofit with `recovery_status: unknown`. See `references/alternatives-checks.md`."*

## Retrofit discipline (3)

All three apply only when `recovery_status:` declared.

### 7. Test-as-requirement inversion (HARD ★ refusal A — retrofit)

- **Tell**: characterization test's assertion read as original intent; rationale paraphrases the test expectation; Context describes no forces
- **check_failed**: `anti-pattern.test-as-requirement-inversion`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: retrofit only
- **recommended_action**: *"Strip rationale paraphrasing the test; mark `rationale: unknown` if no honest record. See `references/retrofit-discipline-checks.md`."*

### 8. LLM confident invention (HARD ★ refusal A — retrofit)

- **Tell**: committee-style prose with named alternatives and neat rejection reasons despite no preserved conversation, no archive, no accessible deciders
- **check_failed**: `anti-pattern.llm-confident-invention`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: retrofit only
- **recommended_action**: *"Replace fabricated content with `unknown`; record the decision observed from code with file path; flag for human follow-up. See `references/retrofit-discipline-checks.md`."*

### 9. Laundering the current state (HARD ★ refusal A — retrofit)

- **Tell**: every alternative rejected for a property the current design has; ADR reads as post-hoc defence of the present design
- **check_failed**: `anti-pattern.laundering-current-state`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: retrofit only
- **recommended_action**: *"Drop the laundered alternatives; mark `alternatives_considered: unknown`. See `references/retrofit-discipline-checks.md`."*

## Linkage integrity (1)

### 11. Orphan ADR (HARD ★ broken-reference)

- **Tell**: `scope_tags` empty AND no citing artifact references the ADR via `governing_adrs:`; decision exists in the ADR directory and nowhere else
- **check_failed**: `anti-pattern.orphan-adr` (aliases `check.linkage.scope-tags-empty`; schema-enforced via `minItems: 1`)
- **severity**: `hard_reject` ★ (broken-reference)
- **recommended_action**: *"Set `scope_tags` to the scope(s) the decision applies to; if none apply, the ADR is not load-bearing — drop it. See `references/linkage-and-lineage-checks.md`."*

## Sweep order

Walk top to bottom. Items 1, 3, 4, 5 are content-shape detectable; items 6 and 11 are front-matter detectable; items 7–9 gate on `recovery_status:` declaration; item 10 is content-shape detectable in Consequences. Score hard-rejects with full evidence first so verdict precedence is unambiguous.

## Aggregation rule

Multiple findings of the same anti-pattern across multiple sections are surfaced as separate findings (one per section) — not aggregated. Use `section: "GLOBAL"` for document-wide patterns (orphan-adr at front-matter; laundering-current-state when every rejection reason exhibits the tell).

## Cross-link

`alternatives-checks.md` (1, 5) · `decision-rationale-checks.md` (2) · `consequences-and-reversibility-checks.md` (3, 10) · `context-and-drivers-checks.md` (4) · `adr-purpose-and-shape-checks.md` (6) · `retrofit-discipline-checks.md` (7, 8, 9) · `linkage-and-lineage-checks.md` (11) · `quality-bar-gate.md` (final gate)
