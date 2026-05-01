# Interface five dimensions — checks

**Contents**
- [The five dimensions](#the-five-dimensions)
- [Dimension-presence checks](#dimension-presence-checks) — protocol, message structure, timing, error handling, startup
- [Design by Contract checks](#design-by-contract-checks)
- [Versioning check](#versioning-check)
- [Protocol-citation check](#protocol-citation-check)

---

Interface requirements are the most commonly under-specified type — typically *syntax without semantics* (a function signature or schema with no statement of what actually happens). The review skill checks every interface requirement for all five dimensions, plus DbC, plus versioning.

## The five dimensions

| # | Dimension | What it covers |
|---|---|---|
| 1 | **Protocol** | Communication standard + version (HTTP/1.1, gRPC, AMQP 0.9.1, OIDC 1.0, TLS 1.3, OAuth grant type) |
| 2 | **Message structure** | Fields, types, units, ranges, optionality. Request / response / error schemas |
| 3 | **Timing** | Latency budget, rate limits, retry expectations, heartbeats. Timing is part of the contract, not a separate NFR |
| 4 | **Error handling** | What happens on call failure, timeout, unexpected data, rate-limit hit. Retry policy, back-off, failure threshold |
| 5 | **Startup / initial state** | Cold-start behaviour, connection lost, first request after restart, readiness probe |

## Dimension-presence checks

For every requirement filed under Interface Requirements, walk these five checks. Each missing dimension is its own finding.

### Check 1 — Protocol named

Is the wire protocol + version specified? Externally imposed protocols (HTTP/1.1, gRPC, OIDC 1.0) are legitimately named here.

- **check_failed**: `check.interface.missing-protocol`
- **severity**: `soft_reject`

### Check 2 — Message structure

Is the request / response / error message structure specified? Field names, types, optionality, constraints. A schema reference (e.g., "per OpenAPI doc /sessions.yaml") is acceptable if the reference is concrete and stable.

- **check_failed**: `check.interface.missing-message-structure`
- **severity**: `soft_reject`

### Check 3 — Timing

Is the timing contract specified? Latency target, rate limits, server-side timeout, retry expectations. A reference to a separate NFR (e.g., "see REQ-NNN") is acceptable.

- **check_failed**: `check.interface.missing-timing`
- **severity**: `soft_reject`

### Check 4 — Error handling

Is error-handling behaviour specified? What happens on downstream failure, timeout, unexpected data, rate-limit hit. Retry policy with back-off and failure threshold.

- **check_failed**: `check.interface.missing-error-handling`
- **severity**: `soft_reject`

### Check 5 — Startup / initial state

Is cold-start / initial-state behaviour specified? Readiness probe behaviour, behaviour before backing dependencies are reachable, first-request-after-restart.

This is the most-omitted dimension. Interfaces silent here have latent bugs that surface in production incidents.

- **check_failed**: `check.interface.missing-startup-state`
- **severity**: `soft_reject`

## Design by Contract checks

Apply Meyer's Design-by-Contract checks: each interface requirement names preconditions, postconditions, invariants. Emit a finding per missing clause.

### Check 6 — Preconditions

- **check_failed**: `check.interface.missing-precondition`
- **severity**: `soft_reject`

### Check 7 — Postconditions

Both success and error variants should be specified.

- **check_failed**: `check.interface.missing-postcondition`
- **severity**: `soft_reject`
- **evidence shape**: note whether success postcondition, error postcondition, or both are missing

### Check 8 — Invariants

Examples: idempotency, non-disclosure, monotonicity.

- **check_failed**: `check.interface.missing-invariants`
- **severity**: `soft_reject`

## Versioning check

### Check 9 — Versioning policy

Check version policy. Flag unversioned interfaces. Each policy includes:

- **Version label** — v1, 2026-04, semver
- **Compatibility regime** — semver, date-based, additive-only-within-major
- **Deprecation policy** — minimum availability of older versions, how callers are notified

- **check_failed**: `check.interface.missing-versioning`
- **severity**: `soft_reject`
- **evidence shape**: note which versioning sub-element is missing (label, regime, or deprecation policy)
- **recommended_action**: *"Add versioning element: version label + compatibility regime + deprecation policy. See `interface-five-dimensions-checks.md` Check 9."*

## Protocol-citation check

### Check 10 — Protocols cited by specification, not informal name

When the interface uses an externally imposed protocol (OIDC, OAuth, REST, etc.), the requirement should cite the specification (RFC number, OIDC version, protocol draft). Informal names without specs leave room for interpretation drift.

Tells:
- "OAuth" without "OAuth 2.0 RFC 6749"
- "REST" without an OpenAPI / message-structure spec
- "OIDC" without "OpenID Connect 1.0 §3.1.2.5"

- **check_failed**: `check.interface.protocol-by-informal-name`
- **severity**: `info`
- **recommended_action**: *"Cite the specification (RFC number, OIDC version, draft) rather than only the informal name."*

## Recommended-action template for all dimension findings

*"Add the missing dimension(s) per the interface five-dimensions form. See `interface-five-dimensions-checks.md` element \<N>."*

(Generic — points to the rule, does not write the replacement contract.)
