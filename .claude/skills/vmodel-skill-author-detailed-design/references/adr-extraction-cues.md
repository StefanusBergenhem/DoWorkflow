# ADR extraction cues (DD ↔ ADR seam)

DD applies decisions; ADR justifies cross-cutting load-bearing ones. Get the split right or rationale becomes invisible (under-extracted) or ADR sprawl (over-extracted).

## When to extract — the three conjunctive criteria

Extract a decision to an ADR when **all three** hold:

| # | Criterion | Test |
|---|---|---|
| 1 | **Load-bearing** | Other artifacts depend on it; removing it changes shape, not just code |
| 2 | **Cross-cutting** | Touches more than this leaf — multiple DDs, multiple Architectures, or future ones |
| 3 | **Hard-to-reverse** | Migration cost > making the decision again |

| Criteria satisfied | Right home |
|---|---|
| All three | ADR |
| Load-bearing + cross-cutting only (easy to reverse) | Could be ADR; team norm decides |
| Load-bearing + hard-to-reverse only (one scope) | Inline rationale in this DD |
| Cross-cutting + hard-to-reverse only (not load-bearing) | Inline; mention in `governing_adrs` if a sibling already covers it |
| One or zero | Inline rationale; do not promote |

## The `[DEFER-ADR: ...]` marker

When DD authoring surfaces a decision meeting all three criteria, but the ADR is not yet authored → emit a marker at the spot the decision lands:

```text
[DEFER-ADR: <one-sentence decision>]
```

Example:

```text
[DEFER-ADR: choose at-least-once vs exactly-once delivery for OrderPlaced events]
```

The marker is a deferred-decision pointer, not a placeholder for justification. Under Rule 6, the marker can persist in active artifacts because the decision is parked at its right home (the ADR). When the ADR is later authored, the ADR id is added to `governing_adrs:` and the marker is replaced with the body-citation pattern (below).

## DD-vs-ADR — what each says

| ADR says | DD says |
|---|---|
| "OrderPlaced events delivered at-least-once" | "Idempotency key required on commit (per ADR-019); duplicate keys return the first response without re-commit" |
| "Postgres for transactional data" | "Order row + outbox row in single transaction (per ADR-019)" |
| "Tokens are bearer JWT validated at gateway" | "validate(token) trusts the gateway-validated subject claim (per ADR-008)" |

ADRs are referenced via `governing_adrs:` in front-matter, not inlined.

## Front-matter list pattern

```yaml
governing_adrs:
  - ADR-008    # bearer-JWT authn at gateway
  - ADR-019    # idempotency-key under at-least-once delivery
```

Comments after each id identify what the ADR covers. A reviewer should not need to open the ADR to know whether it is relevant to this DD.

## Inline body-citation pattern

At every spot in the body where a governing ADR's decision lands → cite it:

```text
postcondition: "Duplicate-key calls return the first response byte-for-byte
                and do not re-commit (per ADR-019)."
```

```text
recovery: "retry up to 2 times with exponential backoff (per ADR-019 —
           database write idempotency under at-least-once delivery)"
```

`governing_adrs:` list with no body citations → finding `check.adr.governing-not-cited-in-body`.

Body citation with no `governing_adrs:` entry → finding `check.adr.body-citation-unresolved`.

## How review checks this

Symmetric review skill verifies:

- Every `governing_adrs:` entry resolves to an actual ADR document
- Every ADR id in `governing_adrs:` appears at least once at a body citation point
- `[DEFER-ADR: ...]` markers are accepted in finalised artifacts (Rule 6 — the decision is parked at its right home)
- No body text contains rationale obviously meeting all three extraction criteria but lacking an ADR reference (laundered inline rationale)

## Hard refusal

When the user pushes to inline a load-bearing + cross-cutting + hard-to-reverse decision because "we don't have time for an ADR" → do not inline rationale. Emit the `[DEFER-ADR: ...]` marker. The marker remains a deferred-decision pointer until the ADR is authored; the decision is parked, not blocking finalisation.

Refusal example:

> User: "Just put the rationale for using HMAC-SHA256 over JWT in this DD — it's faster than writing an ADR."
>
> Skill: This is load-bearing (signature scheme; multiple validators depend on it), cross-cutting (used across all session-validating leaves), and hard-to-reverse (existing tokens would all become invalid). Emitting `[DEFER-ADR: choose HMAC-SHA256 over JWT for session tokens]`. The marker stays in place until the ADR is authored.

## Cross-link

`rationale-capture.md` (when inline; what the four constraint kinds are) · `templates/governing-adr-reference.yaml.tmpl` · refusal A guards rationale-fabrication
