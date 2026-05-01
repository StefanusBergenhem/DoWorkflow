# Retrofit discipline — checks

**Refusal A enforcement.** All checks here apply only when front-matter declares `recovery_status:`. The four human-only fields are `context`, `alternatives_considered`, `rationale`, and anticipated `consequences`.

## Recovery status `reconstructed` on a human-only field

When `recovery_status` is set (scalar or map form) and a human-only field carries `reconstructed`. Schema bans this; the review enforces it independently as well.

- **check_failed**: `check.retrofit-honesty.reconstructed-on-human-only`
- **severity**: `hard_reject` ★ (refusal A; also schema-banned)
- **conditional gating**: retrofit (`recovery_status:` declared)
- **evidence pattern**: quote the front-matter showing `reconstructed` on `context | alternatives_considered | rationale | consequences`.
- **recommended_action**: *"Replace `reconstructed` with `verified` (if a human supplied the content) or `unknown` (if no honest record exists). The four human-only fields cannot be AI-reconstructed."*

## Fabricated content (aggregator for anti-patterns 7 / 8 / 9)

When the human-only fields carry plausible-sounding content but no preserved conversation, archive, or accessible decider supports it. Three sub-tells, each its own anti-pattern but all aggregating here.

- **check_failed**: `check.retrofit-honesty.fabricated-content`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: retrofit
- **evidence pattern**: quote the suspect content; note absence of preserved evidence.
- **recommended_action**: *"Replace fabricated content with `unknown` on the field; record what is observable from code with file path; mark for human follow-up. The honest-unknown ADR is the correct retrofit outcome when rationale is gone."*

## Anti-pattern 7 — Test-as-requirement inversion

When the Rationale paraphrases a characterization test's assertion, treating the test as the original intent. Tell: rationale reads as "the system does X because the test asserts X" rather than naming forces.

- **check_failed**: `anti-pattern.test-as-requirement-inversion`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: retrofit
- **evidence pattern**: quote the rationale; cite the matching test assertion if available.
- **recommended_action**: *"Strip rationale that paraphrases test expectations. Either record actual rationale from a human source, or mark `rationale: unknown`."*

## Anti-pattern 8 — LLM confident invention

When committee-style prose appears with named alternatives and tidy rejection reasons despite no preserved conversation, no archive, no accessible deciders.

- **check_failed**: `anti-pattern.llm-confident-invention`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: retrofit
- **evidence pattern**: quote the suspiciously crisp prose; note absence of preserved evidence in inputs.
- **recommended_action**: *"Replace fabricated alternatives and rationale with `unknown`. Record only what code reveals (Decision observed from file path); flag for human follow-up."*

## Anti-pattern 9 — Laundering the current state

When every alternative is rejected for a property the current design happens to have. The ADR reads as a post-hoc defence of the present design rather than a record of what was actually decided.

- **check_failed**: `anti-pattern.laundering-current-state`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: retrofit
- **evidence pattern**: quote rejection reasons; show that each cites a property of the chosen design.
- **recommended_action**: *"Drop the laundered alternatives. Mark `alternatives_considered: unknown`; the current design is what code shows, not what was decided against."*

## Honest-unknown shape (positive reference)

A correctly-shaped honest-unknown retrofit ADR records:
- The Decision observed from code (with file path).
- Observable Consequences (production-measured throughput, durability shape, etc.).
- `recovery_status: unknown` on each lost human-only field.
- A "recovery posture: unknown — forward ADR required before any migration" closing note.
- An owner for follow-up (`@role` or `@username`).

When this shape is present, no retrofit-honesty findings fire even though the human-only fields are sparse.

## Sweep order in this step

1. Check `recovery_status:` declared. If not → skip this entire sweep.
2. If declared scalar `reconstructed` → not allowed at all (schema-banned); hard finding.
3. If declared map: walk each human-only key; flag `reconstructed` on any (hard).
4. For each human-only field marked `verified` or absent: scan content for fabrication tells (anti-patterns 7 / 8 / 9).
5. Check honest-unknown shape (file path on Decision, observable consequences, owner) — if absent, surface as soft `check.retrofit-honesty.fabricated-content`.
