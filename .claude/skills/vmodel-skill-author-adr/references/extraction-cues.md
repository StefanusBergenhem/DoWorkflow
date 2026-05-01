# Extraction cues — consuming and originating

## Contents

- [Default origin — extraction stub from Architecture](#default-origin--extraction-stub-from-architecture)
- [Alternative origin — pre-existing external policy](#alternative-origin--pre-existing-external-policy)
- [Consuming a `[NEEDS-ADR: ...]` stub](#consuming-a-needs-adr--stub)
- [The matched-side seam](#the-matched-side-seam)

## Default origin — extraction stub from Architecture

The default path: during Architecture authoring, the decomposition exposes a decision that meets the three-condition threshold (load-bearing, ≥2 real options, contingent). The architect drops a `[NEEDS-ADR: <decision>]` stub at the spot in the Architecture body where the decision would otherwise have been inlined as rationale.

```
[NEEDS-ADR: use event bus rather than synchronous RPC for cross-service notifications — extract before finalising]
```

When this skill is invoked, the user has typically just hit such a stub. The job: consume the stub by authoring the ADR.

When the user asks "should I just inline the rationale here?": apply the threshold check. If all three conditions hold, refuse to inline — the load-bearing decision deserves its own ADR. If only one or two hold, inline rationale in the consuming Architecture is the right home.

## Alternative origin — pre-existing external policy

The alternative path: an external or organisational decision already binds before the consuming Architecture exists. Examples:

- "We run on Postgres" — the org has standardised on a database before this scope is being designed.
- "Tokens are issued via the OIDC IdP" — a security policy from a sibling team.
- "We have a portability requirement banning cloud-vendor lock-in."

The ADR is written **first**, and the consuming Architecture references it via `governing_adrs:` from the start.

When the user supplies a decision that is already in force before this scope: ask whether a sibling ADR already documents it. If yes, the consuming Architecture should add the `governing_adrs:` link rather than the user authoring a new ADR. If no, this skill authors the policy ADR; later Architectures cite it.

## Consuming a `[NEEDS-ADR: ...]` stub

The consume sequence:

1. **Read the surrounding Architecture context** for the stub. Identify the decomposition seam, the children at play, the quality attributes the decision affects.
2. **Identify drivers** from the Architecture's allocated requirements and quality-attribute allocations. The drivers in the new ADR's Context are the constraints the Architecture has already surfaced.
3. **Surface ≥2 real alternatives.** The Architecture's `[NEEDS-ADR: ...]` text typically names the chosen option; surface the candidates that lost.
4. **Author the ADR per the standard procedure** (see `SKILL.md`).
5. **Hand back to Architecture**: the consuming Architecture replaces the stub with `governing_adrs: [<new-adr-id>]` and a body citation at the stub site.

The stub is a flag, not a placeholder for inline rationale. The author skill never embeds the rationale itself when the stub is in place — that is the ADR's job.

When the consuming Architecture cannot be finalised because a stub remains: this is correct workflow — the Architecture cannot ship without resolving every stub.

## The matched-side seam

The Architecture authoring skill defines when a stub fires (its three-criteria conjunction: load-bearing AND cross-cutting AND hard-to-reverse). This skill is the receiving side — it does not duplicate the trigger criteria.

When the user is uncertain whether a decision should be extracted to an ADR or stay inline, route to the Architecture skill's extraction cues: the `[NEEDS-ADR: ...]` mechanism is owned there. This skill's job starts when the stub already exists (or when a pre-existing policy needs an ADR before the Architecture is authored).

The two skills compose: Architecture flags → ADR author consumes → Architecture finalises by replacing the stub with `governing_adrs:` plus a body citation.

## Cross-link

`adr-purpose-and-shape.md` (the three-condition threshold) · `propagation-and-completeness.md` (after the ADR ships, propagation links downstream) · `immutability-and-supersession.md` (lineage when superseding) · `anti-patterns.md` (orphan-adr)
