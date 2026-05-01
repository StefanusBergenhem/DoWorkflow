# Front-matter and body — checks

Shape and front-matter sweep (review step 1). All hard-rejects in this file are document-integrity broken-reference triggers.

## Required front-matter fields

When `id`, `artifact_type`, `status`, `scope_tags`, or `date` is missing.
- **check_failed**: `check.front-matter.required-field-missing`
- **severity**: `hard_reject` ★ (broken-reference integrity)
- **evidence pattern**: name the absent field; quote the front-matter block.
- **recommended_action**: *"Add the missing field per the front-matter scaffold. Required set: id, artifact_type, status, scope_tags, date."*

## `id` pattern

When `id` does not match `ADR-\d{3,}-[a-z0-9]+(-[a-z0-9]+)*` (three-or-more-digit sequence + kebab-slug).
- **check_failed**: `check.front-matter.id-pattern-invalid`
- **severity**: `hard_reject` ★ (broken-reference integrity)
- **evidence pattern**: quote the offending `id`; cite the pattern.
- **recommended_action**: *"Rename to match `ADR-NNN-{kebab-slug}` with NNN ≥ 3 digits, slug lowercase + hyphens. Sequence numbers are globally monotonic and never reused."*

## Status enum

When `status` is not in `proposed | accepted | superseded`.
- **check_failed**: `check.status.invalid-lifecycle-state`
- **severity**: `hard_reject` ★ (broken-reference integrity)
- **evidence pattern**: quote the offending `status` value.
- **recommended_action**: *"Set `status` to one of `proposed`, `accepted`, or `superseded`. The ADR community lifecycle is locked at this set."*

## Canonical body sections

Canonical order: Context → Decision → Alternatives → Rationale → Consequences. Y-statement (optional) precedes Context. Reversibility sub-prompt is the last paragraph of Consequences.

When Decision or Consequences is absent: routes through `check.decision.section-missing-or-empty` or `check.consequences-discipline.section-missing` as **hard_reject** (refusal C) — see `decision-rationale-checks.md` and `consequences-and-reversibility-checks.md`.

When Context, Alternatives, or Rationale is absent (Decision and Consequences present):
- **check_failed**: `check.body.canonical-section-missing`
- **severity**: `soft_reject`
- **evidence pattern**: name the missing section.
- **recommended_action**: *"Add the missing canonical section per the document scaffold."*

When sections present but out of canonical order:
- **check_failed**: `check.body.section-ordering`
- **severity**: `info`
- **recommended_action**: *"Reorder sections to canonical order: Context → Decision → Alternatives → Rationale → Consequences."*

## Sweep order in this step

1. Parse front-matter; check required-field, id-pattern, status-enum (three hards).
2. Identify body sections by H2 headings.
3. Check Decision and Consequences presence (defer to refusal-C hards in their files).
4. Check remaining canonical-section presence (soft).
5. Check section ordering (info).

## Defer to other files

- Decision absence → `decision-rationale-checks.md`
- Consequences absence → `consequences-and-reversibility-checks.md`
- `scope_tags` empty → `linkage-and-lineage-checks.md` (orphan-ADR aggregator)
