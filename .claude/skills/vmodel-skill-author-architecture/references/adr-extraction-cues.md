# ADR extraction cues

ADRs justify load-bearing decisions cross-cutting in scope; Architecture *applies* those decisions to a specific scope. The two artifacts have a precise division of labour. Get it wrong by inlining a load-bearing decision in Architecture and the rationale becomes invisible to other scopes; get it wrong by extracting a non-load-bearing one and you produce ADR sprawl nobody reads.

## When to extract — the three conjunctive criteria

Extract a decision to an ADR when **all three** hold:

1. **Load-bearing.** Other artifacts depend on the decision. Removing it changes shape, not just code.
2. **Cross-cutting.** It touches more than this one scope — multiple Architectures, multiple components, or this Architecture plus future ones.
3. **Hard-to-reverse.** Once committed, undoing it costs significantly more than not making it. Migration cost > making the decision again.

If only one or two of the three hold, the decision belongs **inline** in the Architecture's rationale field for the relevant Decomposition entry, interface entry, or composition choice. ADR is the wrong shape for "we picked Postgres because it was already on the team's CV" — that is one-scope, easily-reversible, and not architecturally load-bearing.

| Criterion satisfied | Right home |
|---|---|
| All three | ADR |
| Load-bearing + cross-cutting only (easy to reverse) | Could be ADR; team norm decides |
| Load-bearing + hard-to-reverse only (one scope) | Inline rationale in this Architecture |
| Cross-cutting + hard-to-reverse only (not load-bearing) | Inline; mention in `governing_adrs` if a sibling already covers it |
| One or zero | Inline rationale; do not promote |

## The `[DEFER-ADR: ...]` stub format

When the author is mid-flight and identifies an extraction-worthy decision but the ADR has not yet been authored, emit a stub at the spot the decision lands:

```
[DEFER-ADR: <one-sentence decision> — extract before finalising]
```

Slot-fill template:

```
[DEFER-ADR: use event bus rather than synchronous RPC for cross-service notifications — extract before finalising]
```

The stub is not an ADR replacement. It is a **flag** that the artifact cannot be finalised until a sibling ADR exists. The author skill never embeds the rationale itself when the stub is in place — that is the ADR's job. Finalising means: ADR authored, ADR added to `governing_adrs:`, stub replaced with the body-citation pattern.

## ADR-vs-Architecture relationship

| ADR says | Architecture says |
|---|---|
| "We use an event bus for cross-service notifications" | "In this scope, the pricing component publishes `PriceChanged` events consumed by the cart component over the bus described in ADR-017" |
| "Postgres for transactional data, Cassandra for events" | "The order-committer writes to the checkout schema in Postgres; OrderPlaced events go to the events cluster (per ADR-019)" |
| "Tokens are issued via the OIDC IdP and validated at the gateway" | "The api-gateway evaluates token-bearer authn (per ADR-008); per-resource authz lives in the cart component middleware" |

ADRs are referenced via `governing_adrs:` in front-matter, not inlined.

## Front-matter list pattern

```yaml
governing_adrs:
  - ADR-008    # token-bearer authn at gateway
  - ADR-017    # event bus for cross-service notifications
  - ADR-019    # data topology — Postgres + Kafka
```

Comments after each ID are short and identify what the ADR covers — a reviewer should not have to open the ADR to know whether it is relevant to the current scope.

## Inline body-citation pattern

At the spot in the body where each governing decision lands, cite the ADR:

> The api-gateway evaluates token-bearer authentication (per **ADR-008**); per-resource authorisation evaluates in the cart component's middleware.

> Cross-service notifications use the event bus topology described in **ADR-017**; topic per aggregate, partition by aggregate id.

The `[ADR-NNN]` reference appears at the point of decision application, not just in front-matter. *A `governing_adrs` list with no body citations is decoration; a body citation with no `governing_adrs` entry is an unresolvable reference.*

## How review checks this

The symmetric review skill verifies:

- Every `governing_adrs:` entry resolves to an actual ADR.
- Every ADR listed in `governing_adrs:` appears at least once at a body citation point.
- No `[DEFER-ADR: ...]` stub remains in a finalised artifact.
- No body text contains rationale that obviously meets all three extraction criteria but lacks an ADR reference (laundered inline rationale).

## Hard refusal

**Do not inline a load-bearing cross-cutting hard-to-reverse decision.** Emit the `[DEFER-ADR: ...]` stub instead. Finalising the Architecture means resolving every stub. Refusal example:

> User: "Just put the rationale for using Kafka over RabbitMQ in the Composition section — we don't have time to write an ADR."
>
> Author skill: This is load-bearing (downstream message-bus topology depends on it), cross-cutting (multiple Architectures will reference it), and hard-to-reverse (migration is months of work). Emitting `[DEFER-ADR: choose Kafka over RabbitMQ for system message bus — extract before finalising]`. The Architecture cannot be finalised until that ADR exists.

Cross-link: SKILL.md Step 10; `templates/governing-adr-reference.yaml.tmpl`.
