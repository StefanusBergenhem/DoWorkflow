# Interface requirements — five dimensions

**Contents**
- [The five dimensions](#the-five-dimensions)
- [Slot-fill template](#slot-fill-template)
- [Worked example](#worked-example)
- [Design by Contract at the requirements layer](#design-by-contract-at-the-requirements-layer)
- [Versioning](#versioning)

---

When drafting an interface requirement: cover all five dimensions (protocol+endpoint, payload contract, error contract, version policy, observability). Flag missing dimensions. Plus Design-by-Contract clauses per operation. Plus a version policy.

## The five dimensions

| # | Dimension | What goes here |
|---|---|---|
| 1 | **Protocol** | Communication standard and version (HTTP/1.1, gRPC, AMQP 0.9.1, OIDC 1.0, TLS 1.3, OAuth grant type). Externally imposed → legitimately named here. |
| 2 | **Message structure** | Fields, types, units, ranges, resolutions, defaults, optionality. Request/response schemas; queue payloads; read/write contracts. |
| 3 | **Timing** | Latency budget, rate limits, retry expectations, heartbeat intervals. Timing is part of the contract, not a separate NFR. |
| 4 | **Error handling** | What the interface does on call failure, timeout, unexpected data, rate-limit hit. Including which errors are retryable, with what back-off, to what failure threshold. |
| 5 | **Startup / initial state** | What the interface does before it has been established — cold start, connection lost, first request after restart. Silently-omitted-here issues surface in production incidents. |

## Slot-fill template

```yaml
interface_requirement:
  id: REQ-NNN
  type: interface
  operation: <name and method, e.g. "POST /sessions/{id}/invalidate">

  protocol: |
    <wire protocol + version + transport security; cite RFC/spec where applicable>

  message_structure:
    request:
      - field: <name>, <required|optional>, <type>, <constraints>
      # ...
    response_2xx: [...]
    response_4xx: [...]
    response_5xx: [...]

  timing: |
    <latency target>, <rate limit>, <timeout>, <retry expectations>

  error_handling: |
    <what happens on each failure mode>

  startup_initial_state: |
    <cold-start behaviour; readiness signal; first-request-after-restart>

  precondition:
    - <what the caller must guarantee>

  postcondition_on_success:
    - <what the system guarantees if preconditions hold>

  postcondition_on_error:
    - <what the system guarantees on each error response>

  invariants:
    - <conditions true before and after, always>

  versioning: |
    <version label, compatibility regime, deprecation policy>

  rationale: |
    <why this contract>

  derived_from: [<upstream needs>]
```

## Worked example

```yaml
REQ-020:
  type: interface
  operation: "POST /sessions/{id}/invalidate"

  protocol: |
    HTTP/1.1 over TLS 1.3. API version v1. Request and response bodies are
    application/json.

  message_structure:
    request:
      - id: required, string, matches /^[A-Za-z0-9_-]{16,64}$/
    response_200:
      - body: empty
    response_404:
      - body: { reason: string, in [not_found, already_invalidated] }
    response_401:
      - body: { reason: 'unauthorised' }

  timing: |
    Response within 200 ms at p95 under normal load. Server-side timeout 1 s
    (returns 504 if exceeded).

  error_handling: |
    On downstream store unavailability, the service shall retry the internal
    operation up to 2 times with 50 ms back-off, then fail with 503 and header
    'Retry-After: 1'.

  startup_initial_state: |
    On cold start before the backing store is reachable, the service shall
    respond 503 to all requests with body { ready: false, reason: 'warming' }
    and shall fail the '/health/ready' probe until the store has been
    reachable for 10 consecutive seconds.

  precondition:
    - "Authenticated caller holds role 'session-admin' OR owns the target session."
    - "Session {id} exists and is in state ACTIVE or IDLE."

  postcondition_on_success:
    - "Session {id} is in state INVALIDATED."
    - "Subsequent calls to validate this session-id return 401 reason 'invalidated'."
    - "An invalidation audit event has been appended to the session history record."

  postcondition_on_error:
    - "404: session {id} does not exist OR is already INVALIDATED. No state changes."

  invariants:
    - "Operation is idempotent; calling it on an already-INVALIDATED session is
       a 404 with no state mutation, never a 5xx."
    - "No response discloses whether the caller would have been authorised had
       the session existed (prevents session enumeration)."

  versioning: |
    Additive-only changes within v1 (new enum values require client handling).
    Breaking changes increment the major version. v1 remains available for
    minimum 12 months after v2 is announced.

  rationale: |
    Idempotency supports retry under network failure. Enumeration prevention
    is a security requirement; without it, an attacker can probe for valid
    session-ids by 401-vs-404 differential.

  derived_from: [<parent_requirement_id>, <governing_decision_id>]
```

## Design by Contract at the requirements layer

Apply Meyer's Design-by-Contract (preconditions, postconditions, invariants) at the requirements layer for each externally callable operation. Slot-fill:

- **Preconditions** — what the caller must guarantee
- **Postconditions** — what the system guarantees if preconditions hold (success and error variants)
- **Invariants** — conditions true before and after, always

## Versioning

State a version policy. Unversioned interfaces silently commit to never changing. Slot-fill:

- **Version label** — v1, 2026-04, semver, etc.
- **Compatibility regime** — semver, date-based, additive-only-within-major
- **Deprecation policy** — how long old versions remain available; how callers are notified
