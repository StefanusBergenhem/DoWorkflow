# Counter-example — fabricated retrofit rationale

This example shows a retrofit ADR that fabricates rationale for a pre-existing decision whose human record is gone. Annotations call out the refusal A trips. The corrected honest-unknown form follows.

---

## The fabricated form (refused)

```markdown
---
id: ADR-099-legacy-job-queue
artifact_type: adr
title: "Use Postgres for the job queue"
status: accepted
date: 2021-06-14                                                            <-- back-dated; no evidence of true decision date
scope_tags: [legacy/jobs]
recovery_status:
  context: reconstructed                                                    <-- ILLEGAL state for human-only field (schema-banned)
  alternatives_considered: reconstructed
  rationale: reconstructed
---

# ADR-099: Use Postgres for the job queue

## Context
The team was evaluating asynchronous job processing for the 2021 relaunch.
After reviewing operational experience with Redis and RabbitMQ in the          <-- crisp committee-style prose;
previous quarter's spike, the architecture group concluded that a               no preserved conversation backs
Postgres-based queue was the most pragmatic fit for the team's skill            any of this
profile and the system's durability goals.

## Alternatives considered
- Redis. Rejected due to operational complexity and the team's preference       <-- suspiciously neat rejection reasons;
  for a single stateful system.                                                  no contextual grit; reads as if
- RabbitMQ. Rejected due to the overhead of broker management.                   minutes were taken
- Kafka. Rejected as over-engineered for the expected throughput.

## Rationale
Postgres with SKIP LOCKED offered ACID guarantees for enqueue and a minimal     <-- paraphrases what the code does;
operational footprint, aligning with the team's existing expertise.              "laundering current state"
```

### Refusal A trips

| Trip | Anti-pattern / check | Severity | Reason |
|---|---|---|---|
| `recovery_status: reconstructed` on human-only fields | `check.retrofit-honesty.reconstructed-on-human-only` | hard_reject | Schema-banned. The four human-only fields (`context`, `alternatives_considered`, `rationale`, `consequences`) only accept `verified` or `unknown`. AI inference is refused at the skill level. |
| Committee-style prose with no preserved record | `anti-pattern.llm-confident-invention` | hard_reject | The retrofit had no preserved conversation, archive, or accessible deciders, yet the ADR reads as if minutes were taken. |
| Tidy rejection reasons with no contextual grit | `anti-pattern.llm-confident-invention` (alias), `check.alternatives.rejection-not-context-specific` | hard_reject + soft | Rejection reasons fit five-word patterns ("operational complexity", "broker management overhead") with no specific incident, named constraint, or on-call rotation grounding. |
| Rationale paraphrases what the code does | `anti-pattern.laundering-current-state` | hard_reject | Every alternative rejected for a property the current design has (ACID guarantees, minimal operational footprint = both Postgres properties). |
| Back-dated to guessed decision date | `check.retrofit-honesty.fabricated-content` (aggregator) | hard_reject | The `date` is not the retrofit record date but a guessed original-decision date — no evidence supports it. |

The author skill **refuses to emit** this form.

---

## The corrected honest-unknown form

```markdown
---
id: ADR-099-legacy-job-queue
artifact_type: adr
title: "Use Postgres for the job queue (retrofit)"
status: accepted
date: 2026-04-19                                                            <-- date of the retrofit RECORD, not a guessed decision date
scope_tags: [legacy/jobs]
recovery_status:
  context: unknown
  alternatives_considered: unknown
  rationale: unknown
  consequences: unknown
---

# ADR-099: Use Postgres for the job queue (retrofit)

## Context
unknown — no preserved design notes, no accessible deciders as of the retrofit date.

## Decision (observed from code)
The service uses Postgres with `FOR UPDATE SKIP LOCKED` as its async job queue. See `src/queue/postgres_worker.py`.

## Alternatives considered
unknown.

## Rationale
unknown.

## Consequences (observable)
- Durability of queued jobs matches the primary database's durability.
- Throughput observed in production ~800 jobs/sec; no stress test on record.

**Reversibility.** Unknown at retrofit time; a forward ADR is required before any migration is attempted. Owner: @ops-lead.

## Propagation
(none materialised at retrofit time — a forward ADR captures propagation when the decision is next revisited)
```

### Why this passes the retrofit bar

- `recovery_status` map uses only `unknown` on the four human-only fields — no `reconstructed`.
- The Decision section records what is **observable from code** with a file path, not inferred intent.
- Consequences lists **observed** behaviours (production-measured throughput, durability shape) not anticipated ones.
- Reversibility states the honest unknown and names the owner who carries the follow-up.
- The closing note ("forward ADR required before any migration") is mandatory on retrofit.
- `date` reflects the retrofit record date, not a guessed decision date.

This form is honest, useful, and forward-compatible. It records what the code reveals, admits what has been lost, and does not pretend to reconstruct a conversation that no longer exists.

## Cross-link

`good-postgres-job-queue.md` (the greenfield passing version of the same decision) · `bad-redis-async-queue.md` (refusals B + C) · `references/retrofit-discipline.md` · `templates/retrofit-stub.md.tmpl`
