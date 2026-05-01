# Derivation strategies

Every case carries `type:` from the fixed enum below. Pick the strategy that matches the upstream element being verified; pair strategies on the same element when applicable (ECP+BVA on input ranges, RBT+decision-table on compound predicates).

## Table of contents

- [The eleven strategies](#the-eleven-strategies)
- [Pairings (ECP, decision tables)](#pairings-ecp-decision-tables)
- [Per-strategy slot-fill](#per-strategy-slot-fill)
- [Strategy selection — when to pick which](#strategy-selection--when-to-pick-which)
- [The error / happy ratio rule](#the-error--happy-ratio-rule)

## The eleven strategies

| `type:` | Standard name | When applied | What case it produces |
|---|---|---|---|
| `functional` | RBT (requirement-based testing) | Each behaviour rule from a postcondition / requirement | One case per equivalence class of valid input asserting the rule's stated outcome |
| `boundary` | BVA (boundary value analysis) | Any bounded input range | One case per ON / OFF / IN / OUT point on each boundary |
| `error` | Error-path testing | Each error-matrix row / each typed error / each precondition | One case per detection condition asserting typed error + state guarantee |
| `fault-injection` | Fault injection | Each downstream-failure path (timeouts, dependency errors, resource exhaustion) | One case forcing the fault and asserting recovery + caller observation |
| `property` | Property-based testing | Each invariant on a function / data structure | One case asserting a universally-quantified property over a sampled input space |
| `state-transition` | State-transition testing | Each transition in a state machine | One case driving the source state, firing the event, asserting target + side effects |
| `contract` | Consumer-driven / provider-driven contract testing | Each interface boundary with a published contract | One case asserting the published contract from one side of the boundary |
| `performance` | Performance testing | Each NFR with a latency / throughput / capacity threshold | One case asserting the threshold under the named load and environment |
| `security` | Security testing | Each STRIDE / OWASP-style threat mapped to this scope | One case forcing the threat condition and asserting the mitigation |
| `accessibility` | Accessibility testing | Each WCAG-or-equivalent criterion mapped to a UI | One case asserting the criterion against a representative interaction |
| `error-guessing` | Error guessing | Operational hazards (clock skew, partial state, off-by-one historic bugs) | One case forcing the hazard and asserting graceful behaviour |

When a strategy from this list does not apply at the layer → omit; do not invent. Leaf TestSpecs typically use functional / boundary / error / property / state-transition. Branch TestSpecs add contract / fault-injection. Root TestSpecs add performance / security / accessibility.

## Pairings (ECP, decision tables)

Two techniques are not their own `type:` value but always pair with one of the above.

**ECP (equivalence class partitioning).** When `functional` is applied to a parameter with a range or set of equivalence classes → enumerate one case per class; pair with `boundary` for ON / OFF points at class edges. ECP partitions the space; BVA picks the points within the partition.

**Decision tables.** When `functional` is applied to a compound predicate (multiple Boolean inputs combine to multiple outcomes) → render the decision table inline in `notes:` and emit one case per non-redundant row. The table makes coverage of input combinations visible to the reviewer.

## Per-strategy slot-fill

Slot-fill cues (use when authoring a case under each strategy):

```yaml
# functional (RBT)
type: functional
inputs: { <param>: <value within an equivalence class> }
expected: <specific value or bounded predicate stated by the rule>

# boundary (BVA)
type: boundary
inputs: { <param>: <ON | OFF | IN | OUT point of the named boundary> }
expected: <specific outcome at this point — distinct from the next point>

# error
type: error
inputs: { <param>: <value violating the precondition / driving the detection> }
expected: <typed error + state guarantee (e.g., "raises X; no mutation")>

# fault-injection
type: fault-injection
preconditions: [ "<fault fixture: e.g., 'DB timeout injected at 2s'>" ]
expected: <recovery observation: retry count, breaker state, caller observation>

# property
type: property
inputs: { generator: "<sampling description>", samples: <count> }
expected: "<universally quantified predicate, e.g., 'sum(line.line_total) == subtotal for any cart'>"

# state-transition
type: state-transition
preconditions: [ "<source state>", "<context required>" ]
inputs: { event: <event name>, payload: <values> }
expected: { target_state: <target>, side_effects: [ "<observable>" ] }

# contract
type: contract
preconditions: [ "<contract version pinned>", "<consumer / provider role>" ]
expected: <pact match against the published contract>

# performance
type: performance
preconditions: [ "<load shape>", "<environment shape>" ]
expected: { metric: <p50 | p95 | p99 | throughput>, threshold: <value+unit>, sample_size: <n> }

# security
type: security
preconditions: [ "<threat scenario>" ]
expected: <mitigation observable: rejected, logged, blast-radius bound>

# accessibility
type: accessibility
preconditions: [ "<WCAG criterion>", "<user agent / AT>" ]
expected: <criterion met: e.g., 'all interactive elements reachable via keyboard alone'>

# error-guessing
type: error-guessing
preconditions: [ "<hazard, e.g., 'clock skew of -10s on the issuer'>" ]
expected: <graceful behaviour: bounded outcome, no silent corruption>
```

## Strategy selection — when to pick which

When a postcondition states a rule for valid input → `functional`.
When the rule has a bounded input range → also emit `boundary` cases.
When a precondition exists → emit `error` cases for its violation.
When the spec names a downstream dependency → emit `fault-injection` for its failure modes.
When an invariant is stated → `property`.
When a state machine is present → `state-transition` per edge plus undefined-event cases.
When the parent spec names an interface contract → `contract` (branch).
When the parent spec names an NFR threshold → matching specialised strategy at that threshold.
When operational experience suggests a hazard not yet covered → `error-guessing`.

## The error / happy ratio rule

When the case mix has an error / happy ratio below 1:2 → derivation is incomplete. Sweep the parent spec for uncovered error-matrix rows and uncovered preconditions; emit cases for them. A TestSpec that is all happy-path is a derivation gap, not a passing artifact (anti-pattern.happy-path-bias).

## Cross-link

`testspec-purpose-and-shape.md` (where strategies seed cases) · `case-quality.md` (the oracle bar each case must meet) · `dd-traceability-cues.md` / `architecture-traceability-cues.md` / `requirements-traceability-cues.md` (per-layer derivation seams) · `anti-patterns.md` (gaps when a strategy is missed)
