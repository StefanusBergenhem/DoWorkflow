# Consequences and Reversibility — checks

**Load-bearing.** Refusal B (Reversibility) and refusal C (Consequences structural completeness) both live here.

## Consequences section absent — refusal C

When the Consequences section is missing entirely.

- **check_failed**: `check.consequences-discipline.section-missing`
- **severity**: `hard_reject` ★ (refusal C)
- **evidence pattern**: note the absent header; cite refusal C.
- **recommended_action**: *"Add a Consequences section with Positive / Negative subsections and the verbatim Reversibility sub-prompt at the end."*

## Both signs empty — refusal C

When the Consequences section is present but both Positive and Negative subsections are empty.

- **check_failed**: `check.consequences-discipline.both-signs-empty`
- **severity**: `hard_reject` ★ (refusal C)
- **evidence pattern**: quote the empty section; cite refusal C.
- **recommended_action**: *"Populate both Positive and Negative consequence lists. Every load-bearing decision trades something away."*

## Positives missing (one sign)

When Positive subsection is absent or empty but Negative is present.

- **check_failed**: `check.consequences-discipline.positives-missing`
- **severity**: `soft_reject`
- **recommended_action**: *"List positive consequences. The decision was made for a reason; name what it gains."*

## Negatives missing (one sign)

When Negative subsection is absent or empty but Positive is present.

- **check_failed**: `check.consequences-discipline.negatives-missing`
- **severity**: `soft_reject`
- **recommended_action**: *"List negative consequences. Marketing-style upside-only consequences are anti-pattern 3 (`missing-negatives`)."*

## Negatives handwave (anti-pattern 3 — missing-negatives)

When Negatives are present but vague ("some additional complexity", "minor tradeoffs", "operational overhead"). Concrete cost is not stated.

- **check_failed**: `check.consequences-discipline.negatives-handwave` (alias `anti-pattern.missing-negatives`)
- **severity**: `soft_reject`
- **evidence pattern**: quote the handwave phrase verbatim.
- **recommended_action**: *"Replace handwave with a concrete cost — name the latency, the throughput ceiling, the ops surface, the vendor coupling, the threshold at which the decision is revisited."*

## Reversibility prompt unanswered — refusal B

When the Reversibility sub-prompt is missing entirely, OR is acknowledged with a stock phrase ("good question", "we will revisit", "to be determined") without yes-or-no answer.

- **check_failed**: `check.consequences-discipline.reversibility-unanswered` (alias `anti-pattern.missing-reversibility`)
- **severity**: `hard_reject` ★ (refusal B)
- **evidence pattern**: note the absent paragraph or quote the acknowledgement phrase; cite refusal B.
- **recommended_action**: *"Answer the Reversibility prompt. Yes → state rollback path. No → state recovery plan and named signoff."*

## Reversibility hedged — refusal B

When the answer is "partially reversible", "somewhat reversible", "depends" without separating the reversible parts from the irreversible parts and treating each on its own terms.

- **check_failed**: `check.consequences-discipline.reversibility-hedged`
- **severity**: `hard_reject` ★ (refusal B)
- **evidence pattern**: quote the hedge phrase; cite refusal B.
- **recommended_action**: *"Separate the reversible parts from the irreversible parts. Treat each on its own terms — rollback path for the reversible parts, recovery plan and named signoff for the irreversible parts."*

## Reversible without rollback path

When the Reversibility answer is "yes" / "reversible" but no rollback path is stated (no concrete steps, no migration cost estimate, no seam named).

- **check_failed**: `check.consequences-discipline.reversibility-rollback-missing`
- **severity**: `soft_reject`
- **conditional gating**: answer "yes"
- **recommended_action**: *"State the rollback path: concrete steps, rough effort estimate (e.g., '~1 engineer-week'), the seam that makes rollback feasible (interface adapter, feature flag, dual-run window)."*

## Irreversible without named signoff

When the Reversibility answer is "no" / "irreversible" but no named human signoff is given (no @username, no role, no escalation path).

- **check_failed**: `check.consequences-discipline.reversibility-signoff-missing`
- **severity**: `soft_reject`
- **conditional gating**: answer "no"
- **recommended_action**: *"Name the human signoff (`@username` or role) who approves before implementation, and state the recovery plan if the decision proves wrong."*

## Sweep order in this step

1. Check Consequences presence (refusal C structural).
2. Check both-signs presence (refusal C if both empty).
3. Check each sign individually (positives-missing / negatives-missing / negatives-handwave).
4. Locate the Reversibility paragraph (last paragraph of Consequences).
5. Check Reversibility presence and answered-not-acknowledged (refusal B).
6. If hedged → refusal B hard finding.
7. If "yes" → check rollback path; if "no" → check named signoff (soft).
