# Evolution and fitness functions

Architecture that cannot change becomes a liability before it becomes a legacy. Design for evolution deliberately: name the properties that must be preserved, automate their verification, plan the migration path for changes that will not preserve them.

## Contents

- Architecture as hypothesis
- Evolutionary architecture (Ford / Parsons / Kua)
- Four fitness function categories worth most non-trivial Architectures
- Strangler fig (Fowler)
- Testability as an architectural quality
- Slot-fill check

## Architecture as hypothesis

Every non-trivial decomposition is a bet on how the system will change over the next one to three years. The bet is wrong some of the time; the Architecture is a hypothesis, not a statute.

Naming the hypothesis converts an implicit assumption into a testable one:

> "We expect the checkout service to remain a single deployable while inventory and pricing scale independently."

> "We expect cart and pricing to remain coupled by team ownership; we expect order and fulfilment to diverge by 2027."

A named bet gives future teams a first-class place to record *when the hypothesis broke* — and with it, the cue that the Architecture, not just the code, needs revising.

Author the hypothesis in the Overview paragraph. One sentence per bet; no more than two or three bets per scope.

## Evolutionary architecture (Ford, Parsons, Kua, *Building Evolutionary Architectures*, 2nd ed., 2023)

Accept that Architecture evolves; build verification into the development loop to catch deviation early. The load-bearing device is the **fitness function**: an automated check that a stated architectural property still holds.

Ford et al. classify fitness functions on two axes:

|             | Triggered (events: CI, deploy)              | Continuous (in production)         |
|-------------|---------------------------------------------|-------------------------------------|
| **Atomic**  | One property, one check (CI test)           | One property, live monitor          |
| **Holistic**| Multiple properties checked together        | Multi-signal production observation |

Most teams reach for *atomic + triggered* first; *holistic + continuous* is where mature architectures earn their resilience.

## Four fitness function categories worth most non-trivial Architectures

| Category | What it catches | Tooling examples |
|---|---|---|
| **Dependency direction** | Forbidden imports (e.g., domain → infrastructure) | ArchUnit (Java), dependency-cruiser (JS/TS), custom AST scripts |
| **Latency budget** | A critical interface exceeds its budgeted p95 | Load test or synthetic on schedule, fail-on-regression gate |
| **Module size + coupling** | Slow slide toward god components (LOC, complexity, coupling thresholds) | Static analysis with thresholds in CI |
| **Security posture** | Known-bad patterns (SQL string concat, plaintext secret in config); missing controls (unauthenticated endpoint that should authenticate) | SAST + custom rules + boundary-control checks |

Fitness functions are **not a substitute** for integration tests; they cover a different shape of drift — the slow erosion of architectural properties no functional test will notice.

The Architecture artifact names which properties are load-bearing enough to warrant a fitness function, and references the check at the relevant Decomposition or Composition entry.

Slot-fill:

```yaml
fitness_functions:
  - property: "<<dependency direction: domain must not import infrastructure>>"
    classification: "atomic + triggered"
    check: "<<ArchUnit rule X in module Y; CI gate>>"
  - property: "<<p95 latency on ICartFinaliseCheckout ≤ 1200 ms>>"
    classification: "atomic + continuous"
    check: "<<synthetic load probe; alert on rolling 7-day regression>>"
```

## Strangler fig (Fowler, *StranglerFigApplication*, 2004)

Migration pattern for replacing a legacy system without a big-bang rewrite. Stand a new system alongside the old; route traffic to the new for the capabilities it covers; expand coverage; retire the old when the vine has covered the tree.

Relevant when an Architecture artifact describes a target state and the change plan from current to target is itself an architectural concern. The Architecture entry for a strangled component names:

- **Routing mechanism** — reverse proxy, feature flag, conditional dispatch (and where it lives)
- **Acceptance criteria for retiring the legacy path** — concrete, measurable
- **Rollback point** — what we revert to and how, if the new path fails

Slot-fill:

```yaml
strangler_migration:
  legacy_component: "<<id>>"
  new_component: "<<id>>"
  routing: "<<reverse-proxy on path X | feature flag Y | conditional dispatch on Z>>"
  retirement_criteria: "<<traffic % on new for N weeks AND error parity AND Δlatency ≤ X>>"
  rollback_point: "<<flag flip back to legacy; legacy preserved for N months post-cutover>>"
```

## Testability as an architectural quality

A system's testability — how easily integration and unit tests can be written against its components — is a property of its Architecture, not of its test suite.

Named test seams that earn their keep:

- **Hexagonal port separation** (driving + driven ports) — domain runs against fakes
- **Dependency injection** at composition root — tests substitute collaborators
- **Pure-core / impure-shell split** — pure logic tested without IO; shell tested at the boundary

An Architecture that makes every component depend on a singleton DB client mandates heavyweight integration tests for every unit test. One that specifies driven-port abstractions lets the same tests run in seconds against fakes.

Per Decomposition entry, name the test-seam shape the component provides:

```yaml
test_seam:
  driving_ports: [<<...>>]
  driven_ports:  [<<...>>]
  fake_strategy: "<<in-memory adapter | recorded interactions | n/a — pure component>>"
```

## Slot-fill check

- [ ] Architecture-as-hypothesis bet stated in Overview (one sentence)
- [ ] Load-bearing properties identified
- [ ] Fitness functions chosen across the four categories where relevant
- [ ] Strangler-fig migration named (routing / retirement / rollback) where in play
- [ ] Test-seam shape stated per Decomposition entry

Citations: Ford et al. (2023); Fowler (2004); Cockburn, *Hexagonal Architecture* (2005).
