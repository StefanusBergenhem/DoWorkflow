# Decision and rationale

## Contents

- [Decision — active voice + named option](#decision--active-voice--named-option)
- [Rationale — cite drivers by name](#rationale--cite-drivers-by-name)
- [The 20-ADRs paste test](#the-20-adrs-paste-test)
- [Driver-cite reconciliation pass](#driver-cite-reconciliation-pass)

## Decision — active voice + named option

The Decision section names what is being decided in active voice, with the chosen option named explicitly.

**Pass:**
> We will store queued jobs in a Postgres table and dequeue with `FOR UPDATE SKIP LOCKED` from a small fleet of worker processes.

**Fail (passive, unnamed):**
> A queue technology was selected.

**Fail (option not named):**
> We will use the database for jobs.

When the Decision is passive or the chosen option is not named: `check.decision.passive-or-unnamed-option` (soft).

When the Decision section is missing or empty: `check.decision.section-missing-or-empty` (hard_reject — refusal C).

One sentence is usually enough. Two if the chosen option needs disambiguation (e.g., naming the variant or the integration shape). More than three sentences is a tell that the Decision is doing the Rationale's job — split.

## Rationale — cite drivers by name

The Rationale is where the Decision is justified against the drivers from this ADR's Context. It cites the drivers **by name** — not generic praise.

**Pass (driver-citing):**
> Two drivers decided it: (1) **operational familiarity on the ops team** — adding Postgres tables is free, adding a new stateful system is not, and (2) **ACID enqueue for idempotency-critical retries** — jobs enqueued in the same transaction as the domain write cannot diverge from it, which Redis and RabbitMQ can only approximate with outbox patterns.

**Fail (generic praise):**
> Postgres is more flexible and more modern than the alternatives. It is the industry standard.

**Fail (no driver citation):**
> Postgres is a good fit for our needs.

When the Rationale cites no driver from this ADR or reads as generic option praise: `check.rationale.generic-praise` (soft); `anti-pattern.generic-justification`.

## The 20-ADRs paste test

Apply this on every Rationale draft:

> Could this paragraph be pasted unchanged into 20 unrelated ADRs?

If yes, the Rationale is generic. The same sentence describing why Postgres won the queue decision should not also work to describe why Kafka won the event-bus decision and why TypeScript won the language decision.

Pass tells: the Rationale names a specific team, a specific constraint, a specific feature interaction, a specific deadline. The grit makes it un-pasteable.

When the Rationale would survive the paste test unchanged: `anti-pattern.generic-justification` (soft). Fix: surface a driver from Context that is unique to this ADR's situation, and tie the chosen option to it explicitly.

## Driver-cite reconciliation pass

Run a one-pass reconciliation between Context drivers and Rationale citations on every draft:

1. List the drivers from Context (named items in the "Drivers" enumeration or the named forces in the prose).
2. For each driver, find its name in the Rationale.
3. If a driver is uncited: either drop it from Context (it was not actually load-bearing for this decision) or add the citation (the Rationale is incomplete).
4. If the Rationale cites a name that is not in Context: surface the driver in Context first.

This pass closes the loop: every named driver does work in both halves of the ADR, and every Rationale claim ties back to something a reader can inspect in Context. Context that names drivers but Rationale that doesn't cite them — `check.context.drivers-implicit` (soft).

## Cross-link

`context-and-drivers.md` (where drivers are surfaced) · `alternatives-discipline.md` (rejection reasons cite the same drivers) · `templates/adr.md.tmpl` (Decision and Rationale slots) · `anti-patterns.md` (2, decision group)
