# Resilience patterns

Resilience is a property of **composition**, not of components. A component that behaves correctly in isolation can bring down a system when composed with others under load or partial failure. Six patterns are baseline architectural vocabulary; the Architecture artifact names which apply where and why.

## Contents

- Bulkhead
- Circuit breaker
- Retry with backoff and jitter
- Graceful degradation vs fallback (prefer degradation)
- Failure domain design
- Common-cause failure (the redundancy honesty test)
- Citations

## Bulkhead

Partition a shared resource so failure in one partition cannot starve others. Classic form: per-dependency thread and connection pools.

Example: if the pricing service is slow, only the pool allocated to pricing fills; the inventory service's pool is untouched, and inventory calls continue. Without bulkheads, a single slow dependency drains the global pool and cascades.

Slot-fill: list the partitions explicitly.

```yaml
bulkheads:
  - resource: "<<thread pool | connection pool | semaphore>>"
    partitioned_by: "<<downstream dependency name>>"
    cap: "<<N concurrent>>"
```

## Circuit breaker

Three-state machine:

- **Closed** — normal; count errors.
- **Open** — reject requests fast when error rate exceeds threshold; let the downstream recover.
- **Half-open** — probe with a small number of requests; if they succeed, close.

The value is **not** "retry less" — it is "stop the feedback loop where a failing dependency exhausts its caller."

**Use at:** cross-service boundaries.
**Do not use:** inside a tight internal loop where the operation cost is low enough that failing fast saves nothing.

Modal behaviour (different responses in different states) complicates testing — AWS Builders' Library explicitly recommends caution; ensure each state has a sequence diagram.

## Retry with backoff and jitter

- **Exponential backoff** (`wait = 2^n × base`) spreads retries away from the failure moment.
- **Jitter** (random component of the wait) prevents synchronised retry storms.

Without jitter, every caller retries at the same second and the downstream sees a second wave identical in size to the first.

**Cautionary number:** a 5-layer call chain where each layer retries 3× on failure sees `3^5 = 243×` normal load on the innermost dependency. *Do not stack retries.*

**Retry is only safe with idempotent operations.** For side-effecting operations, idempotency is a deliberate design (idempotency keys, conditional writes, deduplication stores).

Slot-fill:

```yaml
retry_policy:
  applies_to: "<<interface or operation name>>"
  backoff: "<<exponential, base 50ms, max 5s>>"
  jitter: "<<full | equal | none>>"
  max_attempts: <<N>>
  idempotency_strategy: "<<idempotency-key | conditional-write | dedup-store | n/a (read-only)>>"
```

## Graceful degradation vs fallback (prefer degradation)

| | Graceful degradation | Fallback |
|---|---|---|
| Mechanism | Shrink scope on the same path | Parallel substitute path (try primary, then secondary) |
| Code paths | Fewer | More — primary + fallback paths |
| Blast radius | Smaller | Larger — fallback has its own failure mode |
| Failure modes to test | Fewer | More — distinct failure shapes per path |

**Examples:**
- **Degradation:** e-commerce landing page loads without the recommendation carousel; checkout proceeds without loyalty-points lookup.
- **Fallback:** try primary payment processor; on failure try secondary processor.

**Default to degradation.** Fallback looks attractive until the fallback path has its own failure mode that differs from the primary's, and your incident report has three distinct failure categories.

The Architecture decides, **per non-essential dependency**, whether degradation is designed in. Slot-fill:

```yaml
non_essential_dependencies:
  - dependency: "<<name>>"
    on_failure: "<<degrade-by-omitting-X | fallback-to-Y>>"
    rationale: "<<why this choice>>"
```

## Failure domain design

A **failure domain** is a set of components that fail together. Two components sharing a process, a host, a database, a region, or a cloud provider are in the same failure domain at that level.

The Architecture artifact is explicit:

- Which domains matter (process / host / availability zone / region / provider).
- Which components share which domains.
- Where the boundaries are drawn on purpose.

## Common-cause failure (the redundancy honesty test)

Two supposedly independent redundant components can fail together if they share a cause:

- Same library with the same bug.
- Same specification interpreted the same wrong way.
- Same operator running the same bad command on both.

**Redundancy without independence is theatre.**

The Architecture entry for any redundancy mechanism names the **independence property**:

- *Real:* "primary and secondary regions are in different cloud providers."
- *Not real:* "we run two instances of the same binary behind a load balancer" — shared binary, shared library versions, shared deploy pipeline; every common-cause path open.

Slot-fill:

```yaml
redundancy:
  - mechanism: "<<active-active | active-passive | hot-standby>>"
    independence_property: "<<the specific shared-cause path that is broken — naming what is NOT shared>>"
```

## Citations

- Nygard, *Release It!* (2nd ed., Pragmatic, 2018) — bulkhead, circuit breaker.
- AWS Builders' Library — retry+jitter, circuit-breaker caution.
- Kleppmann, *Designing Data-Intensive Applications* (O'Reilly, 2017) — failure-domain reasoning.
