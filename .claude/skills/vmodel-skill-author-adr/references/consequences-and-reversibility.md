# Consequences and Reversibility — load-bearing

> **Load-bearing.** Refusal B (Reversibility prompt non-empty and answered) and most of refusal C (Consequences both-signs completeness) live here.

## Contents

- [Positives and negatives, both required](#positives-and-negatives-both-required)
- [The Reversibility sub-prompt — verbatim and last](#the-reversibility-sub-prompt--verbatim-and-last)
- [Reversible answer shape](#reversible-answer-shape)
- [Irreversible answer shape](#irreversible-answer-shape)
- [Hedged answers — refused](#hedged-answers--refused)

## Positives and negatives, both required

The Consequences section lists positive consequences AND negative consequences. Both are non-empty. Both are concrete.

**Positive consequences** are the gains the chosen option produces — operational, architectural, capability. Each is a named effect with enough specificity that a reader can verify it.

**Negative consequences** are the costs accepted by choosing this option — what is given up, the new failure modes, the upper bound at which the decision needs revisiting. Replace handwave ("some additional complexity") with a measurable cost or threshold.

When the section is absent: `check.consequences-discipline.section-missing` (hard_reject — refusal C). When both subsections are empty: `check.consequences-discipline.both-signs-empty` (hard_reject — refusal C).

When positives are absent or empty alone: `check.consequences-discipline.positives-missing` (soft). When negatives are absent or empty alone: `check.consequences-discipline.negatives-missing` (soft); `anti-pattern.missing-negatives`. When negatives are vague ("some additional complexity"): `check.consequences-discipline.negatives-handwave` (soft).

**Concrete negative — passes:**
> Peak throughput ceiling (~2k jobs/sec on our current hardware) is lower than a dedicated broker would provide. Above that, we revisit.

**Handwave negative — fails:**
> Some additional complexity in the worker layer.

## The Reversibility sub-prompt — verbatim and last

Every ADR's Consequences section ends with the Reversibility sub-prompt, **verbatim**, as the last paragraph:

> *Is this decision reversible? If yes: state the rollback path. If no: state the recovery plan and name who must sign off before implementation.*

The prompt is **answered**, not acknowledged. "We acknowledge this is reversible" is not an answer; "Reversibility: noted" is not an answer.

When the prompt is absent or only acknowledged: `check.consequences-discipline.reversibility-unanswered` (hard_reject — refusal B); `anti-pattern.missing-reversibility`.

## Reversible answer shape

When the decision is reversible, the answer states:

1. **Reversibility = yes.**
2. **Rollback path.** Concrete steps describing how the system gets back to the prior state. Cite the seam that makes rollback feasible (interface adapter, feature flag, dual-run window).
3. **Migration cost estimate.** Rough effort, e.g., "~1 engineer-week".
4. **Risk during rollback.** What is at stake while rolling back (data shape changes, downtime, dual-run window).

**Pass:**
> **Reversibility.** Reversible. Rollback path: workers read via a queue-client adapter (see DD-app-jobs-worker); swapping to Redis or a broker means implementing a second adapter behind the same interface and running both in parallel for one release while draining the Postgres queue. Migration cost estimated at ~1 engineer-week; no data-loss risk because queued jobs are derivable from domain events.

When reversible but no rollback path is stated: `check.consequences-discipline.reversibility-rollback-missing` (soft).

## Irreversible answer shape

When the decision is irreversible, the answer states:

1. **Reversibility = no.**
2. **Recovery plan.** What is done if the decision turns out to be wrong — the recovery posture, not the rollback. Often involves data migration, parallel rebuild, or a rewrite scope.
3. **Named sign-off before implementation.** A `@username` or named role who approves before the implementation lands. The sign-off binds; the named human is on the hook.

**Pass:**
> **Reversibility.** Irreversible. Reversing this means re-issuing every customer's auth tokens, which forces a full sign-out across web and mobile clients and a 24-hour window of degraded UX. Recovery plan: a parallel auth subsystem stood up alongside the legacy one, dual-issue tokens for two release cycles, then deprecate the legacy path. Required sign-off before implementation: @cto and @security-lead.

When irreversible but no named sign-off is given: `check.consequences-discipline.reversibility-signoff-missing` (soft). The signoff is not optional — the cost of being wrong is a rewrite, not a sprint, so the named human matters.

## Hedged answers — refused

Hedged Reversibility answers fail. Tells: "partially reversible", "somewhat reversible", "depends on context", "reversible-ish". When the decision has reversible parts and irreversible parts, **separate them** — list each part on its own terms with its own answer.

**Fail:**
> **Reversibility.** Partially reversible.

**Pass (split):**
> **Reversibility.** Mixed — split:
> - The application-layer queue access (worker pool, adapter) is reversible. Rollback path: implement a second adapter and dual-run for one release; ~1 engineer-week.
> - The data-shape decision (per-tenant queue partitioning vs single shared queue) is irreversible without a 4-week migration. Recovery plan: parallel rebuild + cut-over window. Required sign-off before partitioning ships: @data-platform-lead.

When the answer is hedged without separating the parts: `check.consequences-discipline.reversibility-hedged` (hard_reject — refusal B). Refuse to ship.

## Cross-link

`context-and-drivers.md` (assumptions pair with Reversibility — they are the revisit triggers) · `propagation-and-completeness.md` (consequences propagate to requirements or child artifacts) · `templates/reversibility-block.tmpl` (slot-fill for both branches) · `anti-patterns.md` (3, 10, consequences group)
