# Immutability and supersession

## Contents

- [Immutable bodies after acceptance](#immutable-bodies-after-acceptance)
- [The supersession dance](#the-supersession-dance)
- [What the historical graph buys](#what-the-historical-graph-buys)
- [Linkage integrity checks](#linkage-integrity-checks)

## Immutable bodies after acceptance

Once `status: accepted`, the ADR body is immutable. Editing a decision does not mean editing the old ADR — it means writing a new one that supersedes it.

The body of an `accepted` or `superseded` ADR is read-only. Front-matter is editable in two narrow cases:
- Flipping `status` from `accepted` → `superseded`.
- Setting `superseded_by: <new-id>` on the predecessor when a successor lands.

Any other edit to an `accepted` body or any front-matter field beyond those two is forbidden. When the body shows evidence of edit beyond those changes: `check.immutability.body-edit-on-accepted` (info).

## The supersession dance

Two-step dance when a new ADR replaces an existing one:

1. **Author the new ADR.** Set its `status: accepted` and `supersedes: <old-id>`. Do not touch the old ADR yet.
2. **Edit the old ADR's front-matter only.** Set `status: superseded` and `superseded_by: <new-id>`. Leave the body untouched.

```yaml
# new ADR (ADR-042)
id: ADR-042-postgres-queue-tenant-partitioned
status: accepted
supersedes: ADR-017-use-postgres-for-job-queue

# old ADR (ADR-017) — front-matter edit only
id: ADR-017-use-postgres-for-job-queue
status: superseded
superseded_by: ADR-042-postgres-queue-tenant-partitioned
# body unchanged
```

When the user asks to edit the old ADR's body to "update it" with the new decision: refuse. Write the new ADR. Edit only the predecessor's front-matter.

When the supersession chain is broken — `supersedes` set on the new ADR but predecessor's `superseded_by` does not point back, or vice versa: `check.linkage.supersession-chain-broken` (soft).

When the same ADR claims to both supersede and be superseded simultaneously: `check.linkage.both-supersedes-and-superseded-set` (soft). This is structurally inconsistent — the ADR is either a successor or a predecessor on a given chain link, not both.

## What the historical graph buys

The point of the dance is the historical graph. A maintainer revisiting a decision three years later needs to see:

1. **What was decided** (read the predecessor's body).
2. **What replaced it** (follow `superseded_by`).
3. **Why it was replaced** (read the successor's Context — it cites the predecessor's drivers and explains what changed).

A silently rewritten document destroys this — it shows a single body that reads as if it had always been the current decision. The supersession chain prevents that.

When the user pushes back ("the predecessor is misleading now, can we just delete it?"): refuse. The predecessor stays. Its `status: superseded` plus `superseded_by:` link tells the next maintainer the chain has moved on.

## Linkage integrity checks

The author skill's responsibility is to set the lineage fields correctly when authoring or superseding. Linkage check fires:

- `check.linkage.scope-tags-empty` (hard_reject — refusal C/E; also schema-enforced via `minItems: 1`). `scope_tags` empty.
- `check.linkage.supersession-chain-broken` (soft). One side of the chain set, other side not.
- `check.linkage.both-supersedes-and-superseded-set` (soft). Both directions set on same ADR.
- `check.linkage.affected-scopes-omitted` (info). Decision text suggests cross-scope reach but `affected_scopes` empty/absent.
- `check.linkage.governing-adrs-back-resolution-flag` (soft). ADR `accepted` but no citing artifact references it via `governing_adrs:` AND no propagation requirement materialised at the ADR's scope. Flag-not-scan; structural-suspicion only.

The orphan check (`anti-pattern.orphan-adr`) fires when `scope_tags` is empty AND no citing artifact references the ADR. Refusal C/E aliases.

## Cross-link

`canonical-fields-and-body.md` (the lineage front-matter fields) · `propagation-and-completeness.md` (governing_adrs back-resolution) · `extraction-cues.md` (where citing artifacts come from) · `anti-patterns.md` (11, linkage and immutability groups)
