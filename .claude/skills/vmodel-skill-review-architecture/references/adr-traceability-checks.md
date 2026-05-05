# ADR and traceability checks

Mirrors the author-side `adr-extraction-cues.md`. ADR references in front-matter must resolve and must be cited in the body where the decision lands. Three ADR checks plus three traceability checks; one is HARD (broken-reference integrity), the rest are soft.

## check.adr.governing-not-resolved (HARD — broken-reference integrity)

**Check that** every ADR id listed in `governing_adrs:` resolves to an actual ADR document.

**Reject when** any `governing_adrs:` entry is a dangling id (no document at the expected path, or the document does not declare the id in its own front-matter).

**Approve when** every entry resolves and is parseable.

**Evidence pattern:** quote the dangling ADR id; note where it was looked for.

**recommended_action:** *"Resolve the broken ADR reference. The architecture cannot be coherently evaluated while a citation in `governing_adrs:` does not point to a real document."*

**Note on HALT:** if every `governing_adrs:` entry is broken (no ADRs were supplied with the review inputs), this is a `missing-inputs` HALT condition (per SKILL.md HALT #4) rather than a finding. Use this check when *some* ADRs resolve but specific others do not.

## check.adr.governing-not-cited-in-body (soft)

**Check that** every ADR listed in `governing_adrs:` is cited at least once at a body decision point.

**Reject when** an ADR appears in the front-matter list but the artifact body never references it inline (`per ADR-NNN`, `(ADR-NNN)`, etc.) where the decision applies.

**Approve when** every front-matter ADR has at least one body citation. Decoration in the front-matter without body citations is an unresolvable reference.

**Conditional gating:** applies only when `governing_adrs:` is non-empty.

**recommended_action:** *"Cite each governing ADR at the spot in the body where its decision applies. A `governing_adrs:` list with no body citations is decoration; the inline citation is what makes the reference resolvable for downstream readers."*

## check.adr.inline-decision-should-be-extracted (soft)

**Check that** load-bearing decisions that meet ALL THREE extraction criteria (load-bearing, cross-cutting, hard-to-reverse) are not inlined in Architecture.

**Reject when** the document contains rationale or composition prose that obviously satisfies all three criteria but lacks a `governing_adrs:` reference or a `[DEFER-ADR: ...]` marker. Examples: "we use Kafka over RabbitMQ for message bus", "we chose mTLS over JWT for inter-service authn", "Postgres over Cassandra for transactional data".

**Approve when** all three-criteria decisions are referenced via `governing_adrs:` (with body citation), OR carry an explicit `[DEFER-ADR: ...]` marker flagging the decision is parked at its right home.

**Evidence pattern:** quote the inlined rationale; explain why it meets all three criteria.

**recommended_action:** *"Extract the decision to an ADR (or emit a `[DEFER-ADR: ...]` marker). Inlining a load-bearing cross-cutting hard-to-reverse decision makes the rationale invisible to other scopes that depend on it."*

## check.traceability.requirement-not-allocated (soft)

**Check that** every requirement appearing in the parent Requirements artifact's allocation set lands in either a Decomposition entry's `allocates:`, an Interface entry's `quality_attributes:` (for NFRs), or a stated cross-cutting composition commitment.

**Reject when** a parent allocation does not appear anywhere in the artifact. (This is the document-level companion to `check.decomposition.requirement-orphan`, which fires per child; this check fires at the artifact level.)

**Approve when** every parent allocation is traceable to one or more places in the artifact.

**Evidence pattern:** name the orphaned requirement id; note the parent Requirements path.

**recommended_action:** *"Allocate the requirement to a child or cross-cutting commitment. An unallocated parent requirement is a silent gap in coverage; downstream Detailed Designs and tests will not address it."*

## check.traceability.derived-requirement-not-marked (soft)

**Check that** every requirement that this scope introduces (rather than inheriting from the parent) is marked as derived (`derivation: derived`) and cites the introducing decision.

**Reject when** a requirement is named in the artifact, has no parent in the upstream Requirements, but is not flagged `derived`, OR is flagged `derived` but does not cite the introducing decision (an ADR or governing constraint).

**Approve when** every locally-introduced requirement is marked and cited.

**recommended_action:** *"Mark locally-introduced requirements as derived and cite the introducing decision. Derived requirements without their introducing decision are floating constraints."*

## check.fitness-function.not-named-for-load-bearing-property

(Cross-reference; full definition in `evolution-and-fitness-functions-checks.md`.)

This check appears under Rationale & ADR & Traceability in the Quality Bar gate because fitness functions are the runtime traceability of architectural intent — they are how a load-bearing property stays honest.

## Sweep order

Walk top to bottom. The HARD broken-reference check is first because a broken reference invalidates the artifact; do not waste reviewer cycles on a document with dangling citations. ADR body-citation discipline next. Then the artifact-level traceability checks (requirement allocation, derivation flagging).

Cross-link: `decomposition-checks.md` (`check.decomposition.requirement-orphan` — the per-child equivalent of `check.traceability.requirement-not-allocated`); `quality-bar-gate.md` (Rationale & ADR card); `retrofit-discipline-checks.md` (retrofit `derived_from` discipline).
