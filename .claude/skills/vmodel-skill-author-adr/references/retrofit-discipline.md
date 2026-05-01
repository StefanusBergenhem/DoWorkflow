# Retrofit discipline

> **Refusal A.** Honest retrofit posture (no fabricated rationale) lives here.

## Contents

- [The four human-only fields](#the-four-human-only-fields)
- [Legal recovery_status states](#legal-recovery_status-states)
- [What an honest-unknown ADR records](#what-an-honest-unknown-adr-records)
- [The three retrofit anti-patterns](#the-three-retrofit-anti-patterns)
- [Posture conflict — HALT](#posture-conflict--halt)

## The four human-only fields

In a retrofit ADR (capturing a pre-existing decision whose rationale is lost), four fields are **human-only**:

1. `context` — the situation that forced the decision; not derivable from code.
2. `alternatives_considered` — what was on the table at decision time; not derivable from code.
3. `rationale` — why the chosen option won; not derivable from code.
4. anticipated `consequences` — the upsides and downsides foreseen at decision time; observable consequences (what code now does) are different and may be recorded.

Code does not contain these. They live in human memory, archive, or preserved conversation. When none of those exist, the framework records that they are gone — not a plausible-sounding story.

## Legal recovery_status states

The schema narrows `recovery_status` for ADR human-only fields. The only legal states are:

- **`verified`** — a human supplied the content from memory or archive. The skill records the human source by name (e.g., "verified by @ops-lead from preserved Slack archive").
- **`unknown`** — no honest record exists.

**`reconstructed` is schema-banned on these four fields.** The schema rejects `recovery_status: reconstructed` on `context`, `alternatives_considered`, `rationale`, or `consequences`. AI inference into these fields is refused at the skill level — not left to agent discretion.

When the user asks the skill to set `recovery_status: reconstructed` on a human-only field: refuse (refusal A). `check.retrofit-honesty.reconstructed-on-human-only` (hard_reject; schema-banned).

When AI-fabricated content appears in human-only fields with no preserved conversation, archive, or accessible decider: `check.retrofit-honesty.fabricated-content` (hard_reject — refusal A; aggregator alias for anti-patterns 7/8/9).

## What an honest-unknown ADR records

An honest retrofit ADR with lost rationale records:

1. **The Decision** (observed from code, with file path).
2. **Observable Consequences** (production-measured throughput, durability shape, latency) — these are observable, not anticipated.
3. **`recovery_status: unknown`** on each lost human-only field, in map form.
4. **A "forward ADR required before any migration" closing note.**
5. **An owner for follow-up** (`@role` or `@username`).

```yaml
recovery_status:
  context: unknown
  alternatives_considered: unknown
  rationale: unknown
  consequences: unknown   # if anticipated consequences are lost
```

```markdown
## Context
unknown — no preserved design notes, no accessible deciders as of <date>.

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
```

This is honest, useful, forward-compatible. It records what code reveals, admits what is gone, does not launder the current state as inevitable.

## The three retrofit anti-patterns

Three failure modes appear when the discipline is broken:

**`anti-pattern.test-as-requirement-inversion`** (hard_reject — refusal A). A characterization test's assertion is read as the original intent; the ADR is written to explain why the intent is the intent. Tell: the rationale paraphrases a test expectation; Context describes no forces.

**`anti-pattern.llm-confident-invention`** (hard_reject — refusal A). An agent asked to reconstruct rationale emits crisp, committee-grade prose with named alternatives and neat rejection reasons — all fabricated. Particularly insidious because the output looks like a good ADR. Tell: the retrofit had no preserved conversation, no archive, no deciders alive to interview, yet the ADR reads as if minutes were taken.

**`anti-pattern.laundering-current-state`** (hard_reject — refusal A). The ADR reads as a post-hoc defence of the present design. Tell: every alternative is rejected for a reason that happens to be a property the current design has.

When any of these tells appear in a retrofit draft: refuse. Switch to the honest-unknown shape.

## Posture conflict — HALT

Two posture-conflict signals trigger HALT:

1. `recovery_status` declared but no source-code references provided in the retrofit input.
2. Source code referenced but no `recovery_status` declaration on the ADR.

In both cases, halt and ask which posture applies (greenfield or retrofit). Do not pick a side. The conflict means the input shape is ambiguous; the user clarifies before the skill proceeds.

## Cross-link

`templates/retrofit-stub.md.tmpl` (the scaffold with `recovery_status: unknown` defaults and `# HUMAN-ONLY` markers) · `canonical-fields-and-body.md` (recovery_status field shape) · `anti-patterns.md` (7, 8, 9, retrofit-honesty group)
