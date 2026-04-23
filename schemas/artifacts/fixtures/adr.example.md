---
id: ADR-001-use-redis-for-url-cache
artifact_type: adr
title: "Use Redis as the URL-code mapping store"
status: accepted
date: "2026-04-23"
version: 1
scope_tags:
  - ""
affected_scopes:
  - "api/shortener"
---

## Context

The service must resolve short codes to URLs within 50 ms at p99 under 500
concurrent requests (REQ-002). A relational database introduces query-planning
overhead incompatible with that budget; a purpose-built key-value store fits
naturally.

## Decision

Use Redis as the sole persistence backend. Short codes are stored as simple
string keys; URL values are string values. AOF persistence is enabled with
`appendfsync everysec` to bound data loss to at most one second of writes.

## Alternatives Considered

- **PostgreSQL** — ruled out because a single-table primary-key lookup at
  p99 under concurrent load typically lands between 2–10 ms without connection
  pooling overhead; pooling adds latency jitter that is hard to bound.
- **In-memory map (process-local)** — ruled out because it provides no
  durability and prevents horizontal scaling.

## Rationale

Redis GET is O(1) and typically sub-millisecond on localhost, leaving ample
headroom for network and application overhead within the 50 ms budget.
The operational model (single Redis instance or Redis Sentinel for HA) is
well-understood and compatible with the single-container constraint.

## Consequences

Using Redis couples the service to Redis availability; a Redis outage makes
the service unavailable rather than degraded. This is acceptable for v1
given the single-container deployment target.

**Reversibility:** This decision is reversible at medium cost. The Redis
adapter is behind an interface (`UrlStore`); swapping to a different backend
requires a new adapter implementation and a Redis-to-target data migration
script. The migration effort is bounded to the adapter and migration tooling —
no application logic changes are needed. A reversal trigger would be a
requirement for multi-region active-active replication, which Redis Cluster
supports but adds operational complexity.
