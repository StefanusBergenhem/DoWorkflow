# Context and drivers

## Contents

- [Specific situation, not problem domain](#specific-situation-not-problem-domain)
- [Forces — name them](#forces--name-them)
- [Drivers — list by name](#drivers--list-by-name)
- [Assumptions — enumerate as revisit triggers](#assumptions--enumerate-as-revisit-triggers)

## Specific situation, not problem domain

Context describes the **specific situation** that forced this decision now. "We need an async job queue for the app" is a generic statement of the problem domain — it would fit unchanged into any ADR about queues. "The app already writes its primary domain state to Postgres; several features now require side effects after a request commits; the ops team manages one Postgres cluster today and has no Redis or RabbitMQ experience" is the specific situation.

When the Context reads as a generic statement of the problem domain rather than the specific situation: `check.context.generic-problem-statement` (soft).

When writing Context: name the prior state, name what changed to make this a decision now, name the constraints the decision must respect. The reader must be able to tell why this decision could not have been made (or postponed) a year ago.

## Forces — name them

Forces are the constraints, deadlines, and dependencies acting on this decision. Name them explicitly. Examples:

- **Constraint:** "ops team has zero Redis production experience"
- **Deadline:** "queue must be live before the receipts feature launches in Q2"
- **Dependency:** "we have a portability requirement from ADR-003 that bans cloud-vendor-specific services"

When forces are not named: `check.context.forces-not-named` (soft). The Rationale cannot do its job (citing drivers by name) if Context did not surface them.

## Drivers — list by name

Drivers are the quality attributes / constraints / deadlines / dependencies the decision is **responsible to** — the things its rationale must answer to. List them by name in Context so the Rationale can cite them by name. Two to four named drivers per ADR is typical.

Example driver list for a queue ADR:

- **Operational familiarity on the ops team** — adding Postgres tables is free; adding a new stateful system is not.
- **ACID enqueue for idempotency-critical retries** — jobs enqueued in the same transaction as the domain write cannot diverge from it.
- **Portability** (per ADR-003) — no cloud-vendor lock-in.

When drivers are not made explicit (the Rationale cites nothing or cites generic praise): `check.context.drivers-implicit` (soft).

When the Rationale cites a driver that does not appear in Context: review-side check fires (`check.rationale.generic-praise` if the cite is generic). Author-side fix: surface the driver in Context first.

## Assumptions — enumerate as revisit triggers

Assumptions are the inputs that, if they change, invalidate the decision. They are the **revisit triggers**. Enumerate them explicitly — do not bury them in prose.

```markdown
### Assumptions
- Ops team headcount and on-call rotation remain at current shape; revisit if the org adds dedicated ops capacity for stateful brokers.
- Throughput stays under ~2k jobs/sec; revisit at the upper bound.
- The portability requirement (ADR-003) remains in force; revisit if it is superseded.
```

Each assumption is one sentence answering "if this changes, revisit." A reader must be able to point at the list and say "these are the conditions on which the decision rests."

When assumptions are implicit in Context prose with no enumerated revisit triggers: `anti-pattern.buried-assumptions` (soft). Tell: no place a reader can point to and say "if this changes, revisit."

The assumption list pairs with the Reversibility answer. Reversible decisions can be revisited cheaply when an assumption breaks; irreversible decisions need the recovery plan engaged before the change ships.

## Cross-link

`adr-purpose-and-shape.md` (the threshold — drivers and assumptions establish "contingent on something that may change") · `decision-and-rationale.md` (Rationale cites drivers by name) · `anti-patterns.md` (4, context group)
