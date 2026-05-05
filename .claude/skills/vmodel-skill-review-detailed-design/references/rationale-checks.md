# Rationale checks

Mirrors `rationale-capture.md` on the author side.

## check.rationale.missing (soft)

**Check that** every non-obvious decision carries inline rationale.

**Reject when** a decision lands without rationale (a postcondition that constrains, an algorithm choice that is contractual, an error-handling strategy, an unusual invariant).

**Approve when** rationale is stated inline at the decision site, OR via a `governing_adrs:` reference for cross-cutting decisions.

**recommended_action:** *"Add inline rationale at the decision site. Name the forcing constraint."*

## check.rationale.constraint-kind-unnamed (soft)

**Check that** rationale names which of the four constraint kinds is forcing the decision: external / architectural / resource / temporal.

**Reject when** rationale is given but the kind is not named — future maintainers cannot tell if the constraint is permanent or expected to lift.

**Approve when** the kind is named (one of the four).

**recommended_action:** *"Name the constraint kind. Future readers need to know whether the constraint is permanent or temporal."*

## check.rationale.generic-principle-invocation (soft on greenfield; hard on retrofit)

**Check that** rationale names specific forcing constraints (REQ ids, ADR ids, named trade-offs), not generic principles.

**Reject (soft) when** greenfield rationale uses phrases like "follows best practice", "clean separation of concerns", "industry-standard pattern", "balances flexibility and performance", "single-responsibility principle".

**Reject (hard ★ refusal A) when** retrofit rationale uses these phrases — see `anti-pattern.fabricated-rationale`.

**Approve when** rationale cites a specific REQ / ADR / named trade-off OR a documented constraint.

**Evidence pattern:** quote the generic phrase; note the absence of historical recall.

**recommended_action:** *"Replace generic-principle invocation with a specific constraint citation. On retrofit with no preserved record, mark `unknown` with a follow-up."*

## anti-pattern.fabricated-rationale (HARD ★ refusal A — retrofit only)

**Check that** retrofit rationale fields cite preserved sources OR are marked `unknown`.

**Reject when** retrofit rationale is committee-grade prose ("the team selected X to balance Y with Z") with no preserved decision record, OR confident reasoning back-derived from observable code.

**Approve when** rationale is `verified` with cited human source, OR `unknown` with follow-up owner.

**Evidence pattern:** quote the fabricated phrase; note the absence of preserved evidence (commit message, decision record, accessible team member).

**recommended_action:** *"Replace with `rationale: { status: unknown, note: ..., follow_up: { owner, action } }`. Better prompting will not fix this — only structural refusal works."*

## check.rationale.adr-extraction-missed (soft)

**Check that** rationale obviously meeting the three ADR criteria (load-bearing AND cross-cutting AND hard-to-reverse) is extracted to an ADR rather than inlined.

**Reject when** rationale inline in the DD describes a decision that affects multiple scopes, is hard to reverse, and is load-bearing — but is not extracted to a `governing_adrs:` entry.

**Approve when** such decisions are linked via `governing_adrs:` and body-cited.

**recommended_action:** *"Extract the decision to an ADR. Inline a `[DEFER-ADR: <decision>]` marker if no ADR exists yet."*

## check.rationale.unknown-without-followup (soft on greenfield; required on retrofit)

**Check that** rationale marked `unknown` is paired with a follow-up owner and action.

**Reject when** the `unknown` marking has no follow-up — the gap is resigned-to rather than actionable.

**Approve when** owner + action (+ optional deadline) are stated.

**recommended_action:** *"Pair every `unknown` with `follow_up: { owner, action, deadline? }`. An unknown without a follow-up is not a finding; it is dropped data."*

## Cross-link

`anti-patterns-catalog.md` · `retrofit-discipline-checks.md` · `adr-traceability-checks.md` · refusal A in `SKILL.md`
