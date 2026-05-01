# Observability and security at boundaries

Cross-cutting concerns that teams try to bolt on post-hoc are the ones most punished for the effort. The Architecture artifact specifies **where they enter**.

## Contents

- Observability — telemetry emergence points (logs / metrics / traces / OpenTelemetry default)
- Security — trust zones
- STRIDE (per zone crossing)
- Secrets flow
- Authentication and authorisation as interface concerns
- Citations

## Observability — telemetry emergence points

Three axes; the architectural decision is **where each enters and who consumes it**.

### Logs

- **Common context fields** carried on every record: request ID, tenant, user, correlation ID, span context.
- **Log level discipline** — which components log at which level (DEBUG / INFO / WARN / ERROR).
- **Retention + search** — how long, what tool, who has access.

### Metrics

- **SLO-bearing** — metrics that back user-visible promises (availability, latency p95).
- **Diagnostic** — metrics for on-call (queue depth, retry counts, cache hit ratio).
- **Dimensions** — what cardinality is acceptable (high-cardinality dimensions break Prometheus-like stores).

### Traces

- **Cross-process boundary propagation** — every cross-process call propagates trace context (W3C TraceContext).
- **Sampling policy** — explicit. Common baseline: 100% of errors + 1% of successes.
- **Exemplars** — link traces to metrics for "what slow requests look like".

### Default: OpenTelemetry

OpenTelemetry is the current de facto standard for instrumentation. The Architecture entry names it — or a reasoned alternative.

*Observability bolted on at incident time is always slower and always misses the context that would have made the incident short.*

Slot-fill:

```yaml
observability:
  logs:
    common_fields: [<<request_id, tenant, user, correlation_id, span_context>>]
    retention: "<<N days, tool>>"
  metrics:
    slo_bearing: [<<...>>]
    diagnostic: [<<...>>]
  traces:
    instrumentation: "<<OpenTelemetry | alternative + rationale>>"
    sampling: "<<100% errors + 1% successes | ...>>"
```

## Security — trust zones

A **trust zone** is a region of the system that shares a threat model. Standard zones in a typical web service:

- The internet (untrusted).
- The authenticated user session.
- The internal network between services.
- The service's own process.

Architecture **draws zone boundaries explicitly** and specifies what crosses each boundary and with what guarantees.

## STRIDE (per zone crossing)

Standard checklist for each crossing — name the threats considered and the controls applied.

- **S** — Spoofing (identity)
- **T** — Tampering (data integrity)
- **R** — Repudiation (audit)
- **I** — Information disclosure (confidentiality)
- **D** — Denial of service (availability)
- **E** — Elevation of privilege (authorisation)

Architecture does not teach STRIDE — it names the threats considered per crossing and points to the controls.

Slot-fill per zone crossing:

```yaml
zone_crossings:
  - from: "<<zone>>"
    to:   "<<zone>>"
    threats_considered: [S, T, R, I, D, E]   # or subset with rationale
    controls:
      spoofing: "<<mTLS | OIDC bearer | n/a — not applicable>>"
      tampering: "<<TLS + body integrity check>>"
      repudiation: "<<signed audit log>>"
      info_disclosure: "<<redaction at boundary>>"
      dos: "<<rate limit at gateway>>"
      elevation: "<<authz at evaluation layer X>>"
```

## Secrets flow

Credentials, tokens, keys, and signed URLs cross boundaries and **get logged, cached, and inherited**. The Architecture entry for a secret names:

- **Origin** — secret manager, KMS, external identity provider.
- **In-memory holders** — which components hold it (and for how long).
- **Bearer-only components** — components that see only an opaque bearer token, never the underlying secret.
- **Forbidden surfaces** — where it must never appear: logs, metrics, traces, error messages, exception stack traces.

The laws of leakage are not subtle; the shape of the leakage is almost always in the Architecture's omissions.

Slot-fill:

```yaml
secrets:
  - name: "<<DB credential | API key | signing key>>"
    origin: "<<secret manager | KMS | OIDC>>"
    in_memory_holders: [<<component-id>>]
    bearer_only: [<<component-id>>]
    forbidden_surfaces: [logs, metrics, traces, exception_messages]
```

## Authentication and authorisation as interface concerns

Every externally callable interface entry states:

- **Authentication** — what identity is asserted, how it is verified, what claims are carried.
- **Authorisation** — what permission is required, evaluated at which layer (gateway / middleware / handler).

Mixing these up is how you get systems that authenticate correctly and authorise catastrophically.

The Architecture is the right layer to state these — it is where cross-component guarantees become visible. Putting them in DD alone duplicates the decision per component.

Cross-link: see `interface-contracts.md` for the slot-fill in the interface entry.

## Citations

- OpenTelemetry Specification — opentelemetry.io.
- STRIDE — Microsoft, *The STRIDE Threat Model*; widely adopted threat-modelling checklist.
- OWASP Application Security Verification Standard (ASVS) — secrets-handling and authn/authz controls.
