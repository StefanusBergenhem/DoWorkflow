# Decision and Rationale — checks

Decision-clarity sweep (review step 5) plus Y-statement and immutability tells.

## Decision section absent or empty — refusal C

When the Decision section is missing entirely, or present but empty.

- **check_failed**: `check.decision.section-missing-or-empty`
- **severity**: `hard_reject` ★ (refusal C)
- **evidence pattern**: note the absence of the section header or quote the empty body; cite refusal C.
- **recommended_action**: *"Add a Decision section in active voice naming the chosen option. The Decision is the load-bearing artefact of the ADR."*

## Decision passive or option not named

When the Decision is present but in passive voice ("It was decided that…") or does not name the chosen option (e.g., "We will use a queueing mechanism" rather than "We will use Postgres `SKIP LOCKED`").

- **check_failed**: `check.decision.passive-or-unnamed-option`
- **severity**: `soft_reject`
- **evidence pattern**: quote the Decision sentence; show passive construction or absent option name.
- **recommended_action**: *"Rewrite Decision in active voice ('We will…') with the chosen option named."*

## Rationale generic praise (also anti-pattern 2 — generic-justification)

When the Rationale reads as generic praise of the option ("more flexible", "more modern", "industry standard", "best practice", "production-ready") rather than citing the drivers from this ADR by name.

The paste-into-twenty-other-ADRs test: would the same paragraph fit unchanged into twenty unrelated ADRs? If yes, the Rationale is generic.

- **check_failed**: `check.rationale.generic-praise` (alias `anti-pattern.generic-justification`)
- **severity**: `soft_reject`
- **evidence pattern**: quote the Rationale; flag the generic phrases; note which Context-named drivers are uncited.
- **recommended_action**: *"Rewrite Rationale to cite each driver from Context by name and explain how the chosen option scores against it. If a driver is not actually load-bearing, drop it from Context; if the Rationale is incomplete, add the missing driver discussion."*

## Driver-citation feedback loop

Per-driver pass: read each driver listed in Context; check it appears by name in Rationale. A driver in Context absent from Rationale signals one of:

- The driver is not actually load-bearing — drop from Context.
- The Rationale is incomplete — add the driver discussion.

This is captured under `check.rationale.generic-praise` (alias for "drivers not cited"), not a separate identifier.

## Sweep order in this step

1. Check Decision presence and emptiness (refusal C if absent/empty).
2. Check Decision voice and option-naming.
3. Read drivers from Context (collected during step 3).
4. For each driver, scan Rationale for citation by name.
5. Apply the paste-into-twenty-others test on the Rationale paragraph.

## Y-statement (when present)

When the optional Y-statement is included but not in canonical form: "In the context of {situation}, facing {concern}, we decided for {option}, to achieve {quality}, accepting {downside}."

- **check_failed**: `check.y-statement.shape-malformed`
- **severity**: `soft_reject`
- **evidence pattern**: quote the Y-statement; identify the missing or malformed anchor.
- **recommended_action**: *"Restate the Y-statement in canonical five-anchor form, or remove it. The five anchors map to context / drivers / decision / rationale / chief negative consequence."*

## Immutability (info-level)

When `status` is `accepted` or `superseded` and the body shows evidence of edit beyond status / superseded_by front-matter (e.g., contradicting earlier captured rationale).

- **check_failed**: `check.immutability.body-edit-on-accepted`
- **severity**: `info`
- **evidence pattern**: cite the apparent edit; note the immutability rule.
- **recommended_action**: *"If a decision has changed, write a new ADR that supersedes this one. Bodies of accepted ADRs are immutable; the supersession dance preserves the historical graph."*
