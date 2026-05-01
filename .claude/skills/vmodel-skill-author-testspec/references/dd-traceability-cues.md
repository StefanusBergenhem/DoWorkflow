# DD ↔ leaf TestSpec traceability cues

When authoring a leaf TestSpec, the parent Detailed Design is the derivation source. Walk the DD top to bottom and read each clause as a derivation seed. The mappings below name what each DD clause produces in the TestSpec.

## Table of contents

- [The closed-correspondence mappings](#the-closed-correspondence-mappings)
- [Slot-fill: postcondition → contract case](#slot-fill--postcondition--contract-case)
- [Slot-fill: error matrix row → robustness case](#slot-fill--error-matrix-row--robustness-case)
- [Slot-fill: invariant → property case](#slot-fill--invariant--property-case)
- [Slot-fill: state transition → state-transition case](#slot-fill--state-transition--state-transition-case)
- [When the DD clause is under-specified](#when-the-dd-clause-is-under-specified)

## The closed-correspondence mappings

| DD clause | TestSpec case (one or more) | Strategy `type:` |
|---|---|---|
| Public Interface — postcondition `on_success` | Contract case asserting the property | `functional` (or `property` for invariants) |
| Public Interface — postcondition `on_failure` | Robustness case asserting the typed error + state guarantee | `error` |
| Public Interface — precondition | Error case forcing precondition violation | `error` |
| Public Interface — invariant | Property case across sampled input | `property` |
| Public Interface — input range with bounds | BVA cases per ON / OFF / IN / OUT point | `boundary` |
| Public Interface — error entry (typed) | Robustness case forcing the detection condition | `error` |
| Data Structure — field invariant | Property case asserting the invariant holds for any value of the structure | `property` |
| Data Structure — ownership / lifetime | Functional case verifying the lifetime contract (constructed-when, released-when) | `functional` |
| Algorithms — result property | Functional + property cases over the property's domain | `functional` + `property` |
| State machine — transition | State-transition case driving source state, firing event, asserting target | `state-transition` |
| State machine — undefined-event handling | Negative case firing the undefined event in a defined state | `error` (or `state-transition`) |
| Error matrix — row | Robustness case forcing the detection, asserting containment + recovery + caller-receives | `error` (often `fault-injection` for downstream-failure rows) |
| `[NEEDS-TEST: ...]` marker | Surface as a case with the marker as `notes:` until resolved | strategy from the marker description |

When a DD clause produces no TestSpec case → it is decoration (drop the clause) or the case is missing (under-derivation). The matched review skill flags both directions as soft-rejects unless explicitly justified.

## Slot-fill — postcondition → contract case

```yaml
# From DD:
# postconditions:
#   on_success:
#     - "Returned list is non-descending AND a permutation of input"

- id: TC-<scope>-N
  title: "sort returns ordered permutation of input"
  type: functional
  verifies:
    - "DD-<scope>.public_interface.sort.postconditions.on_success"
  inputs:
    list: [3, 1, 4, 1, 5, 9, 2, 6]
  expected:
    - "result == [1, 1, 2, 3, 4, 5, 6, 9]"      # specific value (functional)
    - "result is a permutation of input"         # property holds (paired property case below)
```

A postcondition typically yields one functional case (representative input) plus one property case (universal quantification).

## Slot-fill — error matrix row → robustness case

```yaml
# From DD error matrix:
# | Database timeout | Pool timeout (2s) | Circuit breaker | retry-once | QueueUnavailable |

- id: TC-<scope>-N
  title: "DB timeout opens circuit and surfaces QueueUnavailable"
  type: fault-injection
  verifies:
    - "DD-<scope>.error_handling.database_timeout"
  preconditions:
    - "Test double: ConnectionPool as fake injecting 2s timeout on first call"
  expected:
    - "After 2s ± 100ms: one retry attempt observed, then circuit-breaker state == open"
    - "Caller receives: QueueUnavailable raised"
    - "No row mutation"
```

The mapping is one-to-one: one error-matrix row → one robustness case (more if multiple equivalence classes of detection apply).

## Slot-fill — invariant → property case

```yaml
# From DD:
# invariants:
#   - "Sum of line.line_total over Lines equals subtotal"

- id: TC-<scope>-N
  title: "cart subtotal equals sum of line totals"
  type: property
  verifies:
    - "DD-<scope>.data_structures.Cart.invariants.subtotal_sum"
  inputs:
    generator: "random Cart with 0..50 lines"
    samples: 200
  expected:
    - "for any sampled Cart: sum(lines.line_total) == subtotal"
```

## Slot-fill — state transition → state-transition case

```yaml
# From DD state machine:
#   Idle --on(arrive)--> Busy [guard: queue.size == 0; action: pop]

- id: TC-<scope>-N
  title: "arrival in Idle with empty queue transitions to Busy and pops"
  type: state-transition
  verifies:
    - "DD-<scope>.state.transitions.Idle_arrive_Busy"
  preconditions:
    - "Source state: Idle"
    - "Queue: empty"
  inputs:
    event: arrive
    payload: { item: <example> }
  expected:
    target_state: Busy
    side_effects:
      - "queue size remains 0 (popped immediately on transition)"
      - "current_item == payload.item"
```

## When the DD clause is under-specified

When a DD clause cannot generate a specific `expected:` value or bounded predicate → the gap is upstream, not at the TestSpec level. Surface as a question to the human; do not invent a number to make the case pass shape checks.

Examples:
- "returns a reasonable result" → upstream DD does not specify "reasonable"; HALT and ask.
- "completes in a reasonable time" → upstream DD does not specify the bound; HALT and ask.
- "log the failure" → upstream DD does not specify what the log entry must contain; HALT and ask.

## Cross-link

`derivation-strategies.md` (the strategy enum) · `verifies-traceability.md` (granularity for leaf cases) · `case-quality.md` (oracle specificity for the `expected:` field) · `retrofit-discipline.md` (when retrofit, the DD is reconstructed alongside the TestSpec)
