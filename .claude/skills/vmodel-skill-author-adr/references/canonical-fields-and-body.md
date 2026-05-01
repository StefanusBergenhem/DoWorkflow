# Canonical fields and body

## Contents

- [Front-matter required fields](#front-matter-required-fields)
- [Optional front-matter](#optional-front-matter)
- [Status lifecycle](#status-lifecycle)
- [Body section ordering](#body-section-ordering)
- [One decision per file](#one-decision-per-file)

## Front-matter required fields

Every ADR carries:

| Field | Pattern / shape |
|---|---|
| `id` | `ADR-NNN-{kebab-slug}` — three-or-more-digit sequence number, never reused |
| `artifact_type` | `adr` (fixed) |
| `title` | Short active-voice noun phrase naming the decision |
| `status` | `proposed` \| `accepted` \| `superseded` |
| `date` | Date of acceptance (or first draft if still `proposed`); ISO `YYYY-MM-DD` |
| `scope_tags` | Non-empty array of scope paths the decision primarily applies to |

When any required field is missing: `check.front-matter.required-field-missing` (hard_reject). When `id` does not match the pattern: `check.front-matter.id-pattern-invalid` (hard_reject). When `status` is outside the enum: `check.status.invalid-lifecycle-state` (hard_reject).

## Optional front-matter

| Field | When present |
|---|---|
| `affected_scopes` | When the decision's reach exceeds `scope_tags`; otherwise omit |
| `supersedes` | The predecessor ADR id this ADR replaces |
| `superseded_by` | The successor ADR id (set on the old ADR's front-matter when status flips to `superseded`) |
| `recovery_status` | Retrofit only (see `retrofit-discipline.md`) — scalar `verified`/`unknown`, or per-field map |

`supersedes` and `superseded_by` follow the same `ADR-NNN-...` pattern as `id`. ADR lineage crosses only to other ADRs.

When the decision text suggests cross-scope reach but `affected_scopes` is empty/absent: `check.linkage.affected-scopes-omitted` (info).

## Status lifecycle

```
proposed → accepted → superseded
```

- **`proposed`** — under discussion, body may evolve.
- **`accepted`** — body becomes immutable. Editing a decision means writing a new ADR (see `immutability-and-supersession.md`).
- **`superseded`** — replaced by another ADR. Front-matter (`status`, `superseded_by`) updates; body remains untouched.

When the body shows evidence of edit beyond status/`superseded_by` after acceptance: `check.immutability.body-edit-on-accepted` (info).

## Body section ordering

Canonical order, top to bottom:

1. **Y-statement** (optional abstract one-liner — see `adr-purpose-and-shape.md`)
2. **Context** — see `context-and-drivers.md`
3. **Decision** — see `decision-and-rationale.md`
4. **Alternatives considered** — see `alternatives-discipline.md`
5. **Rationale** — see `decision-and-rationale.md`
6. **Consequences** — see `consequences-and-reversibility.md`. Last paragraph is the Reversibility sub-prompt verbatim, then answered.
7. **Propagation** — see `propagation-and-completeness.md`

When a canonical section is missing (other than Decision/Consequences, which are hard-reject): `check.body.canonical-section-missing` (soft). When sections are present but out of canonical order: `check.body.section-ordering` (info).

When Decision is missing or empty: `check.decision.section-missing-or-empty` (hard_reject — refusal C). When Consequences is missing: `check.consequences-discipline.section-missing` (hard_reject — refusal C).

## One decision per file

When the user proposes bundling multiple decisions into one ADR: refuse. Multi-decision documents are not ADRs — they collapse the unit of supersession (you cannot cleanly replace one of two decisions in a single document) and break the historical graph.

Split into separate ADRs, each carrying its own threshold check, alternatives, and Reversibility answer.

## Cross-link

`adr-purpose-and-shape.md` (one-decision rule, threshold) · `templates/front-matter.yaml.tmpl` (slot-fill) · `templates/adr.md.tmpl` (full body scaffold) · `anti-patterns.md` (front-matter and body shape group)
