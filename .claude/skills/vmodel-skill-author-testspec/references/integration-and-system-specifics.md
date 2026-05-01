# Integration and system specifics

Branch and root TestSpecs carry derivation surfaces that leaf TestSpecs do not: contract testing, environment shape, and specialised cases for quality attributes (performance, security, accessibility). Pin versions and environments in `preconditions:` so the test-author downstream knows what to provision.

## Table of contents

- [Contract testing](#contract-testing)
- [Environment shapes (pick one per case)](#environment-shapes-pick-one-per-case)
- [Specialised cases — performance / security / accessibility](#specialised-cases--performance--security--accessibility)
- [Version pinning](#version-pinning)

## Contract testing

When the parent Architecture (branch) names a published interface that is consumed by another scope or another system → emit `contract` cases asserting the interface's contract holds.

Two postures, pick one default per interface:

| Posture | One-line discriminator | When to pick |
|---|---|---|
| **Consumer-driven** | The consumer publishes the contract; the provider runs cases asserting they meet it | Internal interfaces under your team's control on both sides |
| **Provider-driven** | The provider publishes the contract; consumers run cases asserting they read it correctly | External APIs you publish to outside teams; long-lived public interfaces |

Slot-fill:

```yaml
- id: TC-<scope>-N
  title: "<consumer> reads <provider> response shape"
  type: contract
  verifies:
    - "ARCH-<scope>.interfaces.<name>"
  preconditions:
    - "Posture: consumer-driven"
    - "Provider version pinned: <provider>@<sha-or-tag>"
    - "Consumer version pinned: <consumer>@<sha-or-tag>"
  steps:
    - "Consumer issues <request shape>"
  expected:
    - "Response matches contract: <fields, types, invariants>"
```

When versions are not pinned → finding `check.test-doubles.fake-without-contract-test` (analogous; the contract drifts on either side's upgrade). Pin both sides.

## Environment shapes (pick one per case)

Cases run somewhere. The environment is part of the case's contract; declare it in `preconditions:`.

| Shape | One-line discriminator | When to pick |
|---|---|---|
| **In-process** | All collaborators in the test process | Leaf tests; branch tests with cheap fakes |
| **Test-containers** | Real dependencies in container fixtures | Branch tests crossing real DB / queue / cache |
| **Shared staging** | A long-running staging deployment | Late-stage branch tests for cross-service integration; not the default |
| **Production-like** | Full prod-shaped infrastructure (named) | Root-level system / acceptance tests |

State explicitly: `Environment: test-containers (Postgres 16, Redis 7)`. Vague references (`integration env`) are flagged.

## Specialised cases — performance / security / accessibility

When the parent spec carries a quality-attribute allocation → emit one specialised case per QA target at the named threshold.

### Performance

```yaml
- id: TC-<scope>-N
  title: "<journey or interface> p95 latency under named load"
  type: performance
  verifies:
    - "REQ-<id>"   # the NFR
  preconditions:
    - "Load shape: <e.g., '100 concurrent users, 1 request/sec each, 5 min ramp'>"
    - "Environment: production-like (named)"
  expected:
    metric: p95
    threshold: "<value+unit, e.g., '<= 250 ms'>"
    sample_size: "<e.g., 1000 requests after warm-up>"
```

When the threshold is named only as "fast enough" → finding (NFR not measurable; escalate to the upstream Requirements spec). Performance cases without a number or a sample size are decoration.

### Security

Derive from the parent's threat model. STRIDE / OWASP-style threats produce `security` cases with the threat scenario in `preconditions:` and the mitigation observable in `expected:`.

```yaml
- id: TC-<scope>-N
  title: "<threat name>"
  type: security
  verifies:
    - "REQ-<id>"   # the security requirement
  preconditions:
    - "Threat: <e.g., 'tampering with session token via signature stripping'>"
  expected:
    - "Mitigation observable: <e.g., 'request rejected with TokenInvalid; audit log entry emitted'>"
```

### Accessibility

Derive from a named criterion (WCAG-or-equivalent). Pair with the user agent / assistive technology used.

```yaml
- id: TC-<scope>-N
  title: "<criterion> on <user journey>"
  type: accessibility
  verifies:
    - "REQ-<id>"   # the accessibility requirement
  preconditions:
    - "Criterion: <e.g., 'WCAG 2.1 SC 2.1.1 Keyboard'>"
    - "User agent: <e.g., 'Chrome 120 + screen reader X'>"
  expected:
    - "<criterion-met observable, e.g., 'all interactive elements reachable via Tab; focus order matches reading order'>"
```

## Version pinning

Whenever a case references an external system, library, or service:
- Name the version (`Postgres 16`, `Stripe API v2024-04-10`, `Spring Boot 3.2.5`)
- For project-internal dependencies under semver, name the major or pinned commit

Vague references (`Postgres latest`, `Stripe API`) are flagged. The case is reproducible only when the environment is reproducible.

## Cross-link

`per-layer-weight.md` (branch and root case shapes) · `derivation-strategies.md` (contract / performance / security / accessibility strategies) · `architecture-traceability-cues.md` (when interface contracts seed cases) · `requirements-traceability-cues.md` (NFR five-element seed)
