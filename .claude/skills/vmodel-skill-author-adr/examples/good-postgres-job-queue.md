# Worked example — fully populated ADR

This example shows a passing ADR. Annotations call out where each Quality Bar group is satisfied.

---

```markdown
---
id: ADR-017-use-postgres-for-job-queue
artifact_type: adr
title: "Use Postgres for the asynchronous job queue"
status: accepted
date: 2026-03-12
scope_tags: [app/jobs]
affected_scopes: [app/jobs, app/ops]
---

# ADR-017: Use Postgres for the asynchronous job queue

**Y-statement.** In the context of the app's background job processing, facing the need for at-least-once delivery with operationally cheap retries, we decided for Postgres (via `SKIP LOCKED`) as the queue backing store, to achieve transactional job enqueue with the same durability guarantees as the rest of our data, accepting lower peak throughput than a dedicated broker.

## Context

The app already writes its primary domain state to Postgres. Several features require side effects that must happen after a request commits (e.g., sending receipts, provisioning downstream accounts). We need a way to schedule and retry these jobs durably. The ops team manages one Postgres cluster today and has no experience operating Redis or RabbitMQ in production.

### Drivers
- **Operational familiarity on the ops team** — adding Postgres tables is free; adding a new stateful system is not.
- **ACID enqueue for idempotency-critical retries** — jobs enqueued in the same transaction as the domain write cannot diverge from it.
- **Portability** (per ADR-003) — no cloud-vendor lock-in.

### Assumptions
- Ops team headcount and on-call shape stay roughly as today; revisit if dedicated stateful-broker capacity is added.
- Queue throughput stays under ~2k jobs/sec at current hardware; revisit at the upper bound.
- The portability requirement (ADR-003) remains in force.

## Decision

We will store queued jobs in a Postgres table and dequeue with `FOR UPDATE SKIP LOCKED` from a small fleet of worker processes.

## Alternatives considered

- **Redis with a Lua-scripted reliable queue.** Rejected: introduces a second stateful system to operate; ops familiarity is zero; durability story is weaker without AOF tuning the team does not currently do.
- **RabbitMQ.** Rejected: delivers the feature set but adds broker operations (clustering, disk sizing, upgrade dance) that the ops team cannot take on this quarter; the throughput headroom is not needed at current load.
- **Cloud-managed queue (SQS / Pub-Sub).** Rejected: couples the app to a specific cloud vendor; we have a portability requirement from ADR-003.

## Rationale

Two drivers decided it: (1) **operational familiarity on the ops team** — adding Postgres tables is free, adding a new stateful system is not, and (2) **ACID enqueue for idempotency-critical retries** — jobs enqueued in the same transaction as the domain write cannot diverge from it, which Redis and RabbitMQ can only approximate with outbox patterns. The portability driver (ADR-003) eliminates the cloud-managed option but does not pick between the remaining three; operational familiarity does the rest.

## Consequences

**Positive**
- Single source of durability for domain state and job state; no dual-write inconsistency; no outbox pattern needed.
- Ops surface does not grow.
- Jobs are visible via SQL for debugging and reporting.

**Negative**
- Peak throughput ceiling (~2k jobs/sec on our current hardware) is lower than a dedicated broker would provide. Above that, we revisit.
- Long-running worker transactions can interact badly with autovacuum; we accept this and constrain job handlers to short transactions.

**Reversibility.** Reversible. Rollback path: workers read via a queue-client adapter (see DD-app-jobs-worker); swapping to Redis or a broker means implementing a second adapter behind the same interface and running both in parallel for one release while draining the Postgres queue. Migration cost estimated at ~1 engineer-week; no data-loss risk because queued jobs are derivable from domain events.

## Propagation

- **Consequence:** *"Job enqueue and the domain write share a transaction."*
  Route: new requirement at this scope.
  Materialised as: **REQ-app-jobs-018** — *"Job enqueue MUST occur in the same database transaction as the domain write that produces the job."* Testable at `app/jobs` integration level.

- **Consequence:** *"Workers use `FOR UPDATE SKIP LOCKED` to dequeue."*
  Route: governing_adrs from child design.
  Bound by: `DD-app-jobs-worker` (carries `governing_adrs: [ADR-017]`).

- **Consequence:** *"Throughput ceiling ~2k jobs/sec — revisit above."*
  Route: revisit-trigger only; no co-located requirement (this is monitoring, not a hard target).
```

---

## What this example gets right (annotations)

| Quality Bar group | Pass evidence |
|---|---|
| Context completeness | Specific situation (Postgres-already-here + ops-team shape) — not a generic queue problem. Forces named: prior state, dependency on ops team, portability requirement. |
| Option space integrity | Three real alternatives with concrete, context-specific rejection reasons (Redis: ops familiarity zero; RabbitMQ: broker ops capacity this quarter; cloud-managed: portability per ADR-003). |
| Decision clarity | Active voice, named option (`Postgres table + FOR UPDATE SKIP LOCKED`). |
| Decision rationale | Two drivers cited by name (operational familiarity + ACID enqueue); 20-ADRs paste test passes — would not fit unchanged into a database-engine ADR. |
| Consequences discipline | Positives and negatives both populated, both concrete; revisit threshold (~2k jobs/sec) named; Reversibility answered with rollback path + cost estimate + named seam (queue-client adapter). |
| Linkage | `scope_tags` non-empty; `affected_scopes` set (decision reaches `app/ops`); no supersession yet. |
| Completeness rule | Three consequences each have a route in the Propagation block. |
| Propagation rule | Testable consequence → new REQ at scope; child-bound consequence → governing_adrs link from `DD-app-jobs-worker`. |
| Spec Ambiguity Test (override) | A junior engineer reading only this ADR can derive the queue's high-level shape (Postgres table, `SKIP LOCKED` workers, in-transaction enqueue, ~2k/s ceiling). The DD then specifies the contract. |

## Cross-link

`bad-redis-async-queue.md` (counter-example with B + C trips) · `bad-fabricated-retrofit.md` (refusal A trips and the corrected honest-unknown form) · `references/anti-patterns.md` (catalog)
