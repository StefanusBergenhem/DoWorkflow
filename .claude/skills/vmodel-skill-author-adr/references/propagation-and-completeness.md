# Propagation and completeness

## Contents

- [The hybrid propagation rule](#the-hybrid-propagation-rule)
- [Route 1 — new requirement at this scope](#route-1--new-requirement-at-this-scope)
- [Route 2 — `governing_adrs` from child artifacts](#route-2--governing_adrs-from-child-artifacts)
- [Route 3 — both](#route-3--both)
- [Completeness rule — no orphan consequences](#completeness-rule--no-orphan-consequences)
- [Propagation block — what to author](#propagation-block--what-to-author)

## The hybrid propagation rule

Every consequence in the Consequences section creates a downstream obligation. The framework propagates obligations in two ways, and one ADR can use both. Every consequence picks at least one route.

When a consequence has neither propagation route in the artifact tree: `check.completeness.consequence-orphan-suspected` (soft). The decision said the system would behave a certain way; nothing in the spec tree commits to it.

The author skill emits a Propagation block listing each consequence with its route. This is the artifact-side evidence; downstream tooling enforces the rule.

## Route 1 — new requirement at this scope

When a consequence is **testable at this ADR's layer**, materialise a new requirement at that layer.

Example: "latency < 50 ms at queue ingress" → REQ at the scope owning the queue. The consequence becomes a Quality Attribute requirement that the scope's TestSpec verifies.

```markdown
## Propagation
- New requirement at this scope: **REQ-app-jobs-018** — *"Job enqueue MUST occur in the same database transaction as the domain write that produces the job."* Testable at `app/jobs` integration level.
```

When a consequence is testable at this scope but no requirement materialised: `check.propagation.testable-consequence-no-requirement` (soft). The author skill prompts the user to add a requirement (or send the user to the requirements-author skill) before finalising.

## Route 2 — `governing_adrs` from child artifacts

When a consequence **only bounds choices made lower down** in the spec tree, it does not become a testable requirement at this scope. Instead, child artifacts (Architecture, Detailed Design) cite this ADR via `governing_adrs:`.

Example: "job payloads are JSON" propagates as a constraint the child Detailed Design honours. There is no testable assertion at the ADR's scope; the constraint shapes child design choices.

```markdown
## Propagation
- Constraint on child designs: worker implementations reference this ADR via `governing_adrs: [ADR-017]` and use the `SKIP LOCKED` dequeue pattern.
```

When a consequence bounds child choices but no child carries `governing_adrs: [<this-ADR>]`: `check.propagation.child-bound-no-governing-link` (soft). Surface to the user; the consuming Architecture or DD needs the link added on its next pass.

## Route 3 — both

A consequence can be both testable here AND bound child choices — use both routes.

Example: a queue durability decision generates a testable requirement at the queue scope ("enqueue is in-transaction with domain write") AND constrains child worker DDs ("workers use `SKIP LOCKED` to dequeue").

## Completeness rule — no orphan consequences

Every consequence is either:
1. Satisfied by a requirement at the ADR's scope (Route 1), or
2. Referenced as a constraint by a child artifact (Route 2), or
3. Both.

A consequence that sits in the ADR and surfaces nowhere downstream is a **dropped obligation**. The decision said the system would behave a certain way and nothing in the spec tree commits to it.

When a consequence appears testable at this scope but no co-located requirement AND no child carries `governing_adrs:` for it: `check.completeness.consequence-orphan-suspected` (soft, flag-not-scan). The author skill flags the suspicion before finalising.

## Propagation block — what to author

End the ADR body with a Propagation block enumerating each consequence with its route. Slot-fill:

```markdown
## Propagation

- **Consequence:** *"Enqueue and domain write share a transaction."*
  Route: new requirement at this scope.
  Materialised as: **REQ-app-jobs-018** — *"Job enqueue MUST occur in the same database transaction as the domain write that produces the job."*

- **Consequence:** *"Workers must use `FOR UPDATE SKIP LOCKED` for dequeue."*
  Route: governing_adrs from child design.
  Bound by: `DD-app-jobs-worker` (carries `governing_adrs: [ADR-017]`).

- **Consequence:** *"Throughput ceiling ~2k jobs/sec — revisit above."*
  Route: revisit trigger; no co-located requirement (the threshold is monitoring, not a hard target).
```

The block lists every consequence (positive or negative) with a propagation route or an explicit "no route — revisit trigger only" note. Empty Propagation blocks are a tell of `check.completeness.consequence-orphan-suspected`.

## Cross-link

`consequences-and-reversibility.md` (where consequences originate) · `extraction-cues.md` (the seam from Architecture's `governing_adrs:` mechanism) · `templates/adr.md.tmpl` (the Propagation block slot) · `anti-patterns.md` (completeness, propagation groups)
