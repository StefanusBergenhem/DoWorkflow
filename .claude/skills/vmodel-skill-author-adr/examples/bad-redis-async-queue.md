# Counter-example — single-option, generic, missing reversibility

This example shows an ADR that fails refusals B and C plus several soft-rejects. Annotations call out the trips.

---

```markdown
---
id: ADR-042-async-job-mechanism
artifact_type: adr
title: "Use Redis for async jobs"
status: accepted
date: 2026-03-12
scope_tags: [app/jobs]
---

# ADR-042: Use Redis for async jobs

## Context
We need an async job queue for the app.

## Decision
We will use Redis because it is more modern and flexible.

## Alternatives considered
- Don't use a queue.

## Rationale
Redis is the industry standard for this kind of workload.

## Consequences
- Fast.
- Developers are happy with it.
```

(Reversibility sub-prompt missing entirely.)

---

## Annotated refusal trips

### Refusal C — option-space and decision/consequences integrity

| Trip | Anti-pattern / check | Severity | Reason |
|---|---|---|---|
| Single-option alternatives | `anti-pattern.single-option`, `check.alternatives.fewer-than-two-real` | hard_reject | Only "Don't use a queue" listed beside the chosen option. "Don't use a queue" is a straw man (the situation requires asynchronous side-effects to be processed durably, so doing nothing was not on the table). |
| Straw-man alternative | `check.alternatives.straw-man` | soft | "Don't use a queue" with a one-line dismissal. |
| Consequences both signs incomplete | `check.consequences-discipline.negatives-missing` | soft | Only positives listed; "developers are happy" is not a system consequence. |

### Refusal B — Reversibility

| Trip | Anti-pattern / check | Severity | Reason |
|---|---|---|---|
| Reversibility prompt missing | `anti-pattern.missing-reversibility`, `check.consequences-discipline.reversibility-unanswered` | hard_reject | The verbatim sub-prompt is absent from Consequences. The author skill refuses to ship. |

### Refusal C also — empty negatives ladder

| Trip | Anti-pattern / check | Severity | Reason |
|---|---|---|---|
| Negatives missing | `anti-pattern.missing-negatives`, `check.consequences-discipline.negatives-missing` | soft | No negative consequences listed; the cost side is empty. |

### Soft trips — Context, Rationale

| Trip | Check | Severity | Reason |
|---|---|---|---|
| Generic problem statement | `check.context.generic-problem-statement` | soft | "We need an async job queue for the app." Names no forces, no drivers, no "why now." |
| Forces not named | `check.context.forces-not-named` | soft | No constraints, deadlines, dependencies named. |
| Drivers implicit | `check.context.drivers-implicit` | soft | The Rationale paragraph cites no driver from Context — Context surfaced none. |
| Generic justification | `anti-pattern.generic-justification`, `check.rationale.generic-praise` | soft | "Industry standard for this kind of workload." Pasteable into 20 unrelated ADRs unchanged. |
| Decision passive on chosen option | `check.decision.passive-or-unnamed-option` | soft | "Use Redis because it is more modern and flexible" — named option, but the rationale (more modern, more flexible) collapses into the Decision. |

## How the author skill responds

When asked to author this ADR, the skill **refuses** at three deterministic gates:

1. **Refusal C — alternatives.** Halts at Step 4: "Fewer than two real alternatives. 'Don't use a queue' is a straw man given the situation. Surface ≥2 real candidates (Redis, RabbitMQ, Postgres-with-SKIP-LOCKED, cloud-managed) with concrete rejection reasons, or capture the choice inline if no real options were on the table."
2. **Refusal C — consequences.** Halts at Step 7: "Both signs not populated. 'Developers are happy' is not a system consequence. Add ≥1 concrete negative cost or threshold."
3. **Refusal B — Reversibility.** Halts at Step 7: "Reversibility sub-prompt is missing. Append the verbatim prompt and answer it: reversible → rollback path + migration cost; irreversible → recovery plan + named sign-off."

After resolving the three hard-rejects, the soft-rejects (generic Context, generic Rationale, missing forces/drivers) accumulate to the Quality Bar self-check at Step 11 and are flagged inline before delivering.

## Cross-link

`good-postgres-job-queue.md` (the passing version) · `bad-fabricated-retrofit.md` (refusal A trips) · `references/anti-patterns.md` (catalog)
