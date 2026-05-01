# Architecture ↔ branch TestSpec traceability cues

When authoring a branch (non-leaf) TestSpec, the parent Architecture is the derivation source. Walk the Architecture's Decomposition, Interface entries, and Composition section. Branch cases verify cross-child behaviour through interfaces — they do not duplicate leaf-level contract verification.

## Table of contents

- [The closed-correspondence mappings](#the-closed-correspondence-mappings)
- [Slot-fill: interface postcondition → integration case](#slot-fill--interface-postcondition--integration-case)
- [Slot-fill: composition invariant → cross-child property case](#slot-fill--composition-invariant--cross-child-property-case)
- [Slot-fill: QA allocation → specialised case](#slot-fill--qa-allocation--specialised-case)
- [Slot-fill: fault-injection on resilience strategy](#slot-fill--fault-injection-on-resilience-strategy)
- [What does NOT belong at the branch](#what-does-not-belong-at-the-branch)

## The closed-correspondence mappings

| Architecture clause | Branch TestSpec case | Strategy `type:` |
|---|---|---|
| Interface entry — precondition | Error case forcing precondition violation across the boundary | `error` |
| Interface entry — postcondition | Integration case asserting the interface postcondition is met by the providing child | `functional` (or `contract`) |
| Interface entry — invariant | Property case asserting the invariant across child boundaries | `property` |
| Composition section — runtime pattern (e.g., pipeline, event-driven) | Integration case driving the pattern's load-bearing flow | `functional` |
| Composition invariant — cross-child property (e.g., "ordering preserved across children") | Property case across the composition boundary | `property` |
| Allocated requirement — chunk of root requirement realised by this branch | Integration case directly verifying the requirement at this layer | `functional` (often paired with `boundary`/`error`) |
| Quality-attribute allocation — performance / availability / security to a component or interface | Specialised case at the named threshold | `performance` / `security` / `accessibility` |
| Component runtime pattern — expected interaction shape (e.g., circuit-breaker, retry, fallback) | Fault-injection case forcing the failure and asserting the pattern's response | `fault-injection` |
| Resilience strategy — declared at the boundary | Fault-injection case driving the strategy | `fault-injection` |
| Observability — declared event / metric / trace at boundary | Functional case asserting the observable is emitted on the named scenario | `functional` |

When an Architecture interface has no corresponding branch case → finding `check.architecture-traceability.interface-uncovered`. The interface is a contract; the branch TestSpec verifies it.

## Slot-fill — interface postcondition → integration case

```yaml
# From Architecture interface 'OrderPlacement':
# postcondition: "On accept, OrderPlaced event is published with payload matching the request"

- id: TC-<scope>-N
  title: "OrderPlacement.accept publishes OrderPlaced with matching payload"
  type: contract
  verifies:
    - "ARCH-<scope>.interfaces.OrderPlacement.postconditions.on_accept"
  preconditions:
    - "Environment: test-containers (Postgres 16, Kafka 3.6)"
    - "Test double: PaymentGateway as stub returning Authorised"
    - "Provider version pinned: order-service@v2.4.0"
  steps:
    - "Call OrderPlacement.accept with valid OrderRequest"
  expected:
    - "Subscriber on 'orders.placed' receives OrderPlaced event within 500ms"
    - "Event payload.orderId matches request.orderId"
    - "Event payload.lineCount equals request.lineCount"
```

## Slot-fill — composition invariant → cross-child property case

```yaml
# From Architecture composition section:
# invariant: "Causal ordering of CartItemAdded events is preserved across the
#             cart-service to checkout-service boundary"

- id: TC-<scope>-N
  title: "causal ordering preserved across cart-checkout boundary"
  type: property
  verifies:
    - "ARCH-<scope>.composition.causal_ordering"
  preconditions:
    - "Environment: in-process (cart-service + checkout-service composed)"
  inputs:
    generator: "random sequence of 10..100 CartItemAdded events"
    samples: 50
  expected:
    - "for any sampled sequence: events received at checkout in same causal order as published at cart"
```

## Slot-fill — QA allocation → specialised case

```yaml
# From Architecture quality-attribute allocation:
# performance: "OrderPlacement.accept p95 < 250ms under 100 RPS"

- id: TC-<scope>-N
  title: "OrderPlacement.accept p95 < 250ms at 100 RPS"
  type: performance
  verifies:
    - "ARCH-<scope>.qa.OrderPlacement.performance"
  preconditions:
    - "Load: 100 RPS, 5-min ramp, 10-min steady"
    - "Environment: production-like (named tier)"
  expected:
    metric: p95
    threshold: "<= 250 ms"
    sample_size: 60000
```

## Slot-fill — fault-injection on resilience strategy

```yaml
# From Architecture: 'PaymentGateway calls use circuit-breaker; open after 5 consecutive 5xx'

- id: TC-<scope>-N
  title: "circuit opens after 5 consecutive PaymentGateway 5xx responses"
  type: fault-injection
  verifies:
    - "ARCH-<scope>.resilience.payment_gateway_breaker"
  preconditions:
    - "Test double: PaymentGateway as fake injecting HTTP 503 on first 5 calls"
  steps:
    - "Issue 5 OrderPlacement.accept calls in succession"
    - "Issue 6th call"
  expected:
    - "Calls 1-5: 503 surfaces as PaymentUnavailable"
    - "Call 6: circuit-breaker is open; PaymentUnavailable surfaces without contacting PaymentGateway"
    - "After 30s half-open probe: breaker resets if probe succeeds"
```

## What does NOT belong at the branch

| Pattern | Where it actually belongs |
|---|---|
| Verifying a leaf's internal data-structure invariant | Leaf TestSpec (`property`) — the leaf owns the invariant |
| Verifying a leaf function's input boundary | Leaf TestSpec (`boundary`) — the leaf's contract owns the bound |
| Verifying internal state of a single child between calls | Leaf TestSpec — branch cases observe cross-child state |
| Performance of a single function in isolation | Leaf TestSpec — branch performance cases verify cross-child paths |

Mistargeting these at the branch is over-coverage; the leaf already has them. Branch cases are about the seam, not the leaves.

## Cross-link

`per-layer-weight.md` (branch case shape) · `derivation-strategies.md` (contract / fault-injection / property strategies) · `integration-and-system-specifics.md` (contract testing posture, environment shape, version pinning) · `verifies-traceability.md` (granularity for branch cases)
