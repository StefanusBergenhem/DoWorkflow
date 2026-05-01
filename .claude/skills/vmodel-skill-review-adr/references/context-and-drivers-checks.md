# Context and drivers — checks

Context-completeness sweep (review step 3). Surfaces under-specified Context that breaks downstream Rationale citation.

## Generic problem-domain statement

When Context describes the problem domain in general terms instead of the specific situation that forced this decision now.

- **check_failed**: `check.context.generic-problem-statement`
- **severity**: `soft_reject`
- **evidence pattern**: quote the offending Context paragraph; cite the missing "why now" anchor.
- **recommended_action**: *"Rewrite Context to name the specific situation, the specific date or milestone, the specific system state that forced the decision now."*

## Forces not named

When the Context does not name the constraints, deadlines, or dependencies in play. Forces are the things the decision must respect — without them named, the Rationale cannot reason against them.

- **check_failed**: `check.context.forces-not-named`
- **severity**: `soft_reject`
- **evidence pattern**: enumerate which categories of forces are absent (e.g., "no constraint named", "no deadline named", "no dependency named").
- **recommended_action**: *"Name the forces in Context — constraints, deadlines, dependencies — explicitly. Each force is a sentence or bullet."*

## Drivers implicit

When drivers (the quality attributes / constraints / deadlines the decision is responsible to) are implicit in prose rather than enumerated. Tell: the Rationale cites no driver by name because there is no name to cite.

- **check_failed**: `check.context.drivers-implicit`
- **severity**: `soft_reject`
- **evidence pattern**: quote the Rationale's praise paragraph; show that no driver from Context is cited by name.
- **recommended_action**: *"List drivers in Context with stable names (e.g., **operational familiarity**, **ACID enqueue for idempotency-critical retries**) so the Rationale can cite them by name."*

## Buried assumptions (anti-pattern 4)

When assumptions are implicit in Context prose with no enumerated revisit triggers. The future-maintainer test: is there a place a reader can point to and say "if this changes, revisit this ADR"?

- **check_failed**: `anti-pattern.buried-assumptions`
- **severity**: `soft_reject`
- **evidence pattern**: quote the Context prose where assumptions are embedded; note absence of an Assumptions list / revisit-triggers list.
- **recommended_action**: *"Enumerate assumptions as a separate list under Context, each one phrased as a revisit trigger ('if X changes, revisit')."*

## Sweep order in this step

1. Read the full Context section.
2. Apply the three context-completeness tells (generic-problem, forces-not-named, drivers-implicit).
3. Apply the buried-assumptions tell.
4. Cross-reference against Rationale — every named driver in Context should appear by name in Rationale (citation feedback loop; the actual driver-citation check fires in `decision-rationale-checks.md`).
