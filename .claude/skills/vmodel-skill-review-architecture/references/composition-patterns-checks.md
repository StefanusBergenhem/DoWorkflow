# Composition pattern checks

Mirrors the author-side `composition-patterns.md`. Composition is the most load-bearing section of an Architecture document; the four hard-reject checks below enforce structural completeness. Soft checks cover the wiring depth that distinguishes a real Composition section from a tick-box one.

## Contents

- `check.composition.missing` (HARD)
- `check.composition.no-named-pattern` (HARD)
- `check.composition.no-sequence-diagram` (HARD)
- `check.composition.deployment-intent-missing` (HARD, root only)
- `check.composition.middleware-stack-unordered`
- `check.composition.di-strategy-unnamed`
- `check.composition.message-bus-topology-unspecified`
- `check.composition.failure-path-sequence-diagram-missing`
- `check.composition.no-rationale-on-pattern`

## check.composition.missing (HARD — refusal C)

**Check that** the document has a Composition section.

**Reject when** there is no Composition section, or the Composition heading is present but its body is empty / a single line.

**Approve when** the Composition section exists with substantive prose, structured wiring, and at least one diagram.

**Evidence pattern:** quote the heading and the body length (or absence).

**recommended_action:** *"Author a non-trivial Composition section per refusal C. Composition is load-bearing; an empty section is a structural failure regardless of how good Decomposition and Interfaces are."*

## check.composition.no-named-pattern (HARD — refusal C)

**Check that** the Composition section names exactly one runtime pattern (or stated combination, e.g., "request-response with outbox-relay for async events").

**Reject when** no runtime pattern is named, OR the section says "we'll figure it out at build time" / "a bunch of services that talk to each other" / "see deployment config".

**Approve when** a pattern from the catalog is named: request-response / event-driven / event-sourcing / saga (orchestration or choreography) / pipeline / layered / hexagonal / clean / microservices / modular monolith / serverless. Combinations are allowed when explicitly named.

**recommended_action:** *"Name a runtime pattern from the catalog. Pattern-naming bounds the wiring concerns; without it, every consumer invents their own."*

## check.composition.no-sequence-diagram (HARD — refusal C)

**Check that** the Composition section carries at least one sequence diagram for the happy path.

**Reject when** there is no sequence diagram (Mermaid `sequenceDiagram` block, or equivalent). Static block diagrams without temporal order do not satisfy this check.

**Approve when** at least one sequence diagram is present for the happy path. (Failure-path diagrams are recommended; their absence is `check.composition.failure-path-sequence-diagram-missing`, soft.)

**recommended_action:** *"Add a sequence diagram for the happy path. Static diagrams describe structure; sequence diagrams describe runtime — Composition needs both."*

## check.composition.deployment-intent-missing (HARD — refusal C, root only)

**Check that** at root scope (`parent_scope: null`), the Composition section enumerates environments, names an orchestration target, and maps Decomposition components to runtime units.

**Reject when** root scope and any of: environments not enumerated; orchestration target not named; no runtime-unit boundary mapping; deployment intent collapsed to "see infra repo".

**Approve when** all three are stated explicitly with rationale tying choices to requirements or governing ADRs.

**Conditional gating:** applies only when `parent_scope: null`. At branch scope, deployment intent inherits from root and this check is skipped.

**recommended_action:** *"Add the root-scope deployment intent: environments, orchestration target, runtime-unit boundaries. IaC artifacts implement; Composition specifies the intent."*

## check.composition.middleware-stack-unordered (soft)

**Check that** the middleware stack is stated AND ordered.

**Reject when** middleware is named without order (a bullet list with no sequence implication), OR ordering does not match the canonical safety sequence (tracing outermost; authN before authZ; rate limit before handler; logging on exit).

**Approve when** the stack is enumerated with explicit order and the order matches a defensible discipline.

**recommended_action:** *"Order the middleware stack explicitly. Discovering it at integration time is how rate-limit-after-handler bugs ship."*

## check.composition.di-strategy-unnamed (soft)

**Check that** the dependency-injection strategy is named.

**Reject when** the document does not state how components acquire dependencies (constructor injection / DI container / composition root / service locator) — leaving leaf Detailed Designs to reinvent it.

**Approve when** the DI strategy is named once, applies to all children consistently, and is testable (substitutable for fakes in unit tests).

**recommended_action:** *"Name the DI strategy at composition root. Leaf DDs that each pick their own DI strategy produce inconsistent test patterns and obscure wiring."*

## check.composition.message-bus-topology-unspecified (soft, where applicable)

**Check that** when the runtime pattern uses messaging (event-driven, event-sourcing, saga-choreography, request-response with outbox), the message-bus topology is specified.

**Reject when** messaging is in play but topics, partition keys, retention windows, dead-letter routing, or consumer-group layout are absent.

**Approve when** all five are stated: topic naming, partition-key choice (and rationale — usually preserves per-aggregate ordering), retention window, DLQ routing + TTL, consumer-group layout per downstream service.

**Conditional gating:** applies only when the runtime pattern names messaging.

**recommended_action:** *"Specify the bus topology — topics / partition key / retention / DLQ / consumer groups. These are architectural, not infra detail."*

## check.composition.failure-path-sequence-diagram-missing (soft)

**Check that** at least one failure-path sequence diagram is present (PSP timeout, downstream circuit-open, partial failure with compensation, etc.).

**Reject when** only the happy path is diagrammed and the runtime pattern has any non-trivial failure surface (most do).

**Approve when** a critical failure path (chosen by the author for its load-bearing failure mode) has a sequence diagram showing the compensation / rollback / typed-error response.

**recommended_action:** *"Add at least one failure-path sequence diagram. Failure paths are where Composition's invariants are tested — happy-path-only diagrams hide the load-bearing details."*

## check.composition.no-rationale-on-pattern (soft)

**Check that** the runtime pattern choice carries rationale (inline at the pattern naming, or via `governing_adrs:`).

**Reject when** the pattern is named without rationale ("we use request-response" period), and no `governing_adrs:` reference covers it.

**Approve when** rationale ties the pattern to user-wait latency, decoupling needs, fan-out shape, or another concrete trade-off.

**recommended_action:** *"Add rationale tying the pattern choice to a requirement, ADR, or named trade-off. 'Trendy default' is not rationale."*

Cross-link: `anti-patterns-catalog.md` (ad-hoc-composition); `quality-bar-gate.md` (Composition card); `deployment-intent-checks.md` (root-only checks).
