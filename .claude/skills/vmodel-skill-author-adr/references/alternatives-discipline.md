# Alternatives discipline

## Contents

- [The ≥2-real-options bar](#the-2-real-options-bar)
- [Concrete, context-specific rejection](#concrete-context-specific-rejection)
- [The single-option smell test](#the-single-option-smell-test)
- [Straw man and "do nothing"](#straw-man-and-do-nothing)
- [Post-hoc tells](#post-hoc-tells)

## The ≥2-real-options bar

Refusal C: refuse to ship an ADR with fewer than two real alternatives.

A real alternative is one that:
1. Was on the table at the time of the decision.
2. Would have produced a materially different design if chosen.

Listed alternatives that fail either test do not count. When the candidate list is "use Postgres / do nothing", that is one option. When the candidate list is "use Postgres / use Postgres but with a different table layout", these are not materially different — that is one option.

When fewer than two real alternatives are listed: `check.alternatives.fewer-than-two-real` (hard_reject — refusal C; alias `anti-pattern.single-option`).

## Concrete, context-specific rejection

Each alternative carries a rejection reason that is concrete and context-specific. The reason names a driver from this ADR's Context and explains why this option fails against it.

**Generic — fails the bar:**
> Redis. Rejected: more complex.

**Concrete and context-specific — passes:**
> Redis with a Lua-scripted reliable queue. Rejected: introduces a second stateful system to operate; ops team familiarity is zero; durability story is weaker without AOF tuning the team does not currently do.

The second names what makes Redis "more complex" *for this team in this context* — and ties the rejection to a driver that appears in Context (operational familiarity).

When the rejection reason is generic ("more complex", "less mature", "industry preference"): `check.alternatives.rejection-not-context-specific` (soft).

## The single-option smell test

Apply this smell test on every ADR draft:

> Would any rejected alternative have produced a materially different design from the chosen one?

If no — every rejection collapses to the same design as the chosen option — the option space was already collapsed before the ADR was written. This is the post-hoc rationalisation tell (`anti-pattern.post-hoc-rationalisation`, soft) and the single-option smell (`anti-pattern.single-option`, hard).

When all rejected alternatives reduce to the chosen design: refuse. Surface the real options that were considered, or admit (inline in Architecture) that there was only one path.

## Straw man and "do nothing"

"Do nothing" is not an alternative unless doing nothing was a credible path — for example, if the system could continue without the feature being decided about. If "do nothing" appears as the sole alternative beside the chosen option, refusal C fires.

A straw man is a listed alternative that was never on the table; rejection reason is trivial or fabricated. Tell: rejection reason fits in five words ("too slow", "too complex", "unsupported"). Example:

> Quantum job queue. Rejected: not commercially available.

When a straw-man alternative appears: `check.alternatives.straw-man` (soft). Replace with a real candidate or remove from the list.

## Post-hoc tells

When the design was drafted first and the ADR is back-derived to match:

- Rejected alternatives are described in the same vocabulary as the chosen design ("could have used X but X does not have feature Y" — where Y is exactly the property the chosen option has).
- The rejection reasons are too tidy — no contextual grit, no specific incident, no named constraint.
- The rejected alternatives are described at a level of abstraction that prevents comparison ("we considered other databases" rather than "we considered MySQL with X-extension").

Tell: `anti-pattern.post-hoc-rationalisation` (soft). Fix at intake — apply the capture-then-design flow (see `adr-purpose-and-shape.md`).

When the user asks for an ADR "because we already chose X but need to document the rationale": this is post-hoc by definition. The legitimate move is either (a) a retrofit ADR with `recovery_status: unknown` on human-only fields (see `retrofit-discipline.md`), or (b) inline rationale in the consuming Architecture if the threshold is not actually met.

## Cross-link

`adr-purpose-and-shape.md` (capture-then-design flow) · `templates/alternatives-block.tmpl` (slot-fill) · `retrofit-discipline.md` (the legitimate retrofit move) · `anti-patterns.md` (1, 5, option-space group)
