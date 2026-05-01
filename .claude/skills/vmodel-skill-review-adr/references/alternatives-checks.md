# Alternatives — checks

Option-space-integrity sweep (review step 4) plus single-option and post-hoc tells. Refusal C lives here.

## Fewer than two real alternatives — refusal C

When the Alternatives section lists fewer than two real options. "Do nothing" and straw men do not count. Aliases `anti-pattern.single-option`.

- **check_failed**: `check.alternatives.fewer-than-two-real` (alias `anti-pattern.single-option`)
- **severity**: `hard_reject` ★ (refusal C)
- **evidence pattern**: quote the entire Alternatives section verbatim; cite refusal C.
- **recommended_action**: *"Surface ≥2 real alternatives with concrete context-specific rejection reasons, or drop the ADR. The single-option ADR is description, not decision."*

## Straw-man alternative

When a listed alternative is one no participant ever genuinely entertained — included to fill the count. Tell: rejection reason is trivial ("nobody uses this anymore") or the alternative is "do nothing" framed as a real option.

- **check_failed**: `check.alternatives.straw-man`
- **severity**: `soft_reject`
- **evidence pattern**: quote the offending alternative + its rejection reason; cite the trivial-rejection tell.
- **recommended_action**: *"Replace the straw man with a real alternative that was on the table, or drop it. If the count drops below two, refusal C fires."*

## Rejection reason not context-specific

When an alternative's rejection reason is generic ("more complex", "less mature", "harder to operate") rather than anchored to a driver named in this ADR's Context.

- **check_failed**: `check.alternatives.rejection-not-context-specific`
- **severity**: `soft_reject`
- **evidence pattern**: quote the rejection reason; show that no driver from Context is referenced.
- **recommended_action**: *"Anchor the rejection reason to a driver named in this ADR's Context (e.g., 'ops team has zero familiarity; on-call rotation cannot absorb a second stateful system this quarter' rather than 'more complex')."*

## Single-option smell (anti-pattern 1, smell test)

The smell-test layer beneath refusal C. Apply when ≥2 alternatives are listed nominally. Test: would any rejected alternative have produced a materially different design? If no, the option space was already collapsed.

- **check_failed**: `anti-pattern.single-option`
- **severity**: `hard_reject` ★ (refusal C; aliases `check.alternatives.fewer-than-two-real` when refusal C also fires structurally)
- **evidence pattern**: paraphrase the rejected alternatives; show that all would have led to ~the same design.
- **recommended_action**: *"Either find real alternatives that would have produced different designs, or drop the ADR; the option space had already collapsed before the ADR was drafted."*

## Post-hoc rationalisation (anti-pattern 5)

When the design was drafted first and the ADR back-derived. Tell: rejection reasons all reduce to properties the chosen design has; the rejected alternatives could not realistically have led anywhere.

- **check_failed**: `anti-pattern.post-hoc-rationalisation`
- **severity**: `soft_reject`
- **evidence pattern**: quote rejection reasons; show that each rejection cites a property of the chosen option.
- **recommended_action**: *"Re-derive the ADR from the actual decision conversation. If no conversation existed, mark as retrofit with `recovery_status: unknown` on the human-only fields."*

## Sweep order in this step

1. Count distinct real alternatives (excluding "do nothing" and straw men).
2. If <2 → refusal C hard finding immediately.
3. For each alternative present, evaluate its rejection reason: straw-man? generic? post-hoc?
4. Apply the single-option smell test even when ≥2 alternatives exist nominally.
