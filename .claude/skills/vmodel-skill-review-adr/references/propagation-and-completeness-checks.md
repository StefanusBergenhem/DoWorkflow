# Propagation and completeness — checks

Hybrid propagation rule + completeness rule (review step 7). All checks here are flag-not-scan: the review surfaces structural suspicion without a heavy cross-tree walk. Mechanical completeness scanning is a Phase 6 tool job.

## Hybrid propagation rule

Every consequence chooses one of:
1. **New requirement at this scope** when the consequence is testable at this layer (e.g., "latency < 50 ms at queue ingress" → REQ at the queue scope).
2. **`governing_adrs:` from child artifacts** when the consequence only bounds choices made lower down (e.g., "job payloads are JSON" → child DDs cite this ADR via `governing_adrs:`).
3. Both.

Author skill emits a Propagation block listing each consequence with its propagation route. Review checks the block's structural completeness against the Consequences listed.

## Testable consequence with no requirement at scope

When a consequence appears testable at this ADR's layer (named threshold, bounded measure, observable behaviour at the scope) but no requirement materialised at the ADR's scope.

- **check_failed**: `check.propagation.testable-consequence-no-requirement`
- **severity**: `soft_reject` (flag-not-scan)
- **evidence pattern**: quote the consequence; note that the Propagation block does not list a corresponding REQ-id at this scope.
- **recommended_action**: *"Either materialise the testable consequence as a new requirement at this ADR's scope, or strike it from Consequences if it is not actually a binding obligation."*

## Child-bound consequence with no governing link

When a consequence bounds child design choices but no child artifact carries `governing_adrs: [<this ADR>]` (or the Propagation block does not list a child reference).

- **check_failed**: `check.propagation.child-bound-no-governing-link`
- **severity**: `soft_reject` (flag-not-scan)
- **evidence pattern**: quote the consequence; note absence of child `governing_adrs` reference in the Propagation block.
- **recommended_action**: *"Cite the child artifacts that consume this constraint via `governing_adrs:`. If no child consumes it, the constraint is not binding — drop from Consequences or move to a different ADR."*

## Consequence orphan suspected (completeness rule)

When a consequence has neither propagation route — no requirement at this scope, no `governing_adrs` from any child. The consequence sits in the ADR and goes nowhere.

- **check_failed**: `check.completeness.consequence-orphan-suspected`
- **severity**: `soft_reject` (flag-not-scan)
- **evidence pattern**: quote the orphan consequence; note absence in the Propagation block.
- **recommended_action**: *"Either propagate the consequence (new REQ at scope, or `governing_adrs` from a child), or drop it from Consequences if no obligation actually exists."*

## Sweep order in this step

1. Collect every consequence (positive AND negative) listed in Consequences.
2. Read the Propagation block (if present).
3. For each consequence, determine its propagation route per the hybrid rule.
4. If neither route → `consequence-orphan-suspected`.
5. If consequence appears testable at this layer but Propagation block lists no REQ → `testable-consequence-no-requirement`.
6. If consequence appears to bound a child but Propagation block lists no child reference → `child-bound-no-governing-link`.

## Flag-not-scan boundary

These checks are structural-suspicion only. Confirming that a child DD actually carries `governing_adrs: [<this ADR>]` requires walking the spec tree — that work belongs to a mechanical traceability tool (Phase 6). The review surfaces the suspicion via the Propagation block's content.
