# Rationale and traceability

**Contents**
- [Rationale](#rationale)
- [The no-fabrication rule (hard refusal)](#the-no-fabrication-rule-hard-refusal)
- [Derived requirements](#derived-requirements)
- [Traceability](#traceability)
- [Inspection — Perspective-Based Reading (PBR)](#inspection--perspective-based-reading-pbr)

---

Two related lifecycle disciplines: rationale captures the *why* behind each requirement; traceability captures the *what derives from what*.

## Rationale

Rationale is the why behind a requirement; never invent it. Tells of fabrication: generic phrasing, no specific source, reasoning-from-statement-not-recall.

### Capture at decision time

When you cannot recall a specific decision, mark `recovery_status: unknown` — do not reason backward from the statement.

## The no-fabrication rule (hard refusal)

When the upstream input does not supply a reason for a requirement, **do not invent one**. Two acceptable forms only:

### Form A — pending human input (greenfield)

```yaml
- id: REQ-NNN
  statement: "<the requirement statement>"
  rationale: "<pending — requires human input>"
  rationale_status: pending
  follow_up:
    - owner: "<role or person to ask>"
    - action: "Confirm reasoning behind <the load-bearing choice in this requirement>."
```

The document does not silently pass; it explicitly surfaces the gap.

### Form B — unknown (retrofit)

For retrofit work where the original reasoning is lost:

```yaml
- id: REQ-R-NNN
  statement: "<reconstructed from observable code/tests>"
  statement_recovery_status: reconstructed   # behaviour fields may be reconstructed
  rationale: unknown
  rationale_recovery_status: unknown          # rationale field — only verified or unknown allowed
  follow_up:
    - owner: "<team or role>"
    - action: "Confirm whether <reconstructed behaviour> is intended policy."
  derived_from: [<observed_evidence — file:line, commit, log>]
```

### Hard rule on `recovery_status` for the rationale field

Allowed values: `verified` (a human confirmed it from preserved documents or conversation) or `unknown`. **Never** `reconstructed`. The behaviour fields (statement, acceptance) may be `reconstructed` from observable code/tests; the rationale field never may.

A retrofit document with many `rationale: unknown` entries is **correct** — it honestly reports what is lost — not partial.

### Tells of fabricated rationale

- Generic, abstract reasoning that could fit any requirement ("balances X with Y", "industry-standard", "best practice")
- No specific decision, no specific alternative considered, no specific source
- Rationale that reads as "because <obvious benefit>" rather than "because <choice between alternatives>"
- In retrofits: rationales that exist for every requirement when no decision records have been preserved

## Derived requirements

A derived requirement is one introduced not by an upstream stakeholder need but by a design or architectural decision **at this scope** that creates a new constraint. They are flagged explicitly and their rationale cites the introducing decision.

```yaml
- id: REQ-NNN
  derivation: derived
  statement: "Per the architectural decision on token generation (ADR-012),
              the session service shall use a CSPRNG for all session token
              generation."
  rationale: "ADR-012 mandated CSPRNG following the 2024 entropy audit; this
              requirement makes the choice testable at this layer."
  derived_from: [ADR-012]
```

Derived requirements **must** be flagged. The easiest way to drift out of stakeholder alignment is to quietly add requirements no upstream party agreed to.

## Traceability

Traceability is not paperwork; it is the mechanism that makes every other discipline provable.

### Forward traceability (authored)

Every requirement has non-empty `derived_from` linking to:

- An upstream stakeholder need or user story (functional / NFR)
- A parent requirement (when this is a child-scope refinement)
- A product brief outcome
- An inherited constraint (for derived requirements)
- A governing architectural decision (for derived requirements introduced at this scope)
- Observed evidence — file path + line range, commit hash, operational log entry (retrofit only)

### Backward traceability (computed by tooling, surfaced by review)

Authored documents do not carry backward links — those are computed by tooling. But the author should keep the backward shape in mind:

- Every stakeholder need resolves to at least one requirement
- Every requirement resolves to at least one allocated child or to at least one verifying test case
- Every governing decision is referenced by at least one requirement or derived requirement

### Completeness check (run before delivering)

Before declaring the document done, verify:

- [ ] Every requirement has `derived_from` non-empty.
- [ ] Every inherited constraint has a `source`.
- [ ] Every governing decision is referenced by at least one requirement or derived requirement in the body.
- [ ] Every requirement has `rationale` non-empty (or marked `pending`/`unknown` per the no-fabrication rule, never both empty).
- [ ] Every NFR has the five elements present (system, response, metric+unit, target, condition).
- [ ] Every interface requirement has the five dimensions present.

Missing links in either direction are findings — an orphan requirement is unjustified; an unverified requirement is unenforced.

## Inspection — Perspective-Based Reading (PBR)

Apply Perspective-Based Reading: read as designer, tester, and (when stakeholder outcomes apply) user.

| Perspective | Focus |
|---|---|
| **Designer** | "If I were allocating this to children, could I?" |
| **Tester** | "If I were deriving test cases from this, could I?" |
| **User / stakeholder** | "Does this capture what the stakeholder actually wants?" |

Run Designer and Tester always; User/Stakeholder when a stakeholder-facing outcome is at stake.
