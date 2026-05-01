---
name: vmodel-skill-review-requirements
description: >
  Review one requirements specification document for craft quality and emit
  a structured verdict (APPROVED, REJECTED, or DESIGN_ISSUE) plus findings
  tied to specific requirement IDs and failed checks. Use when checking a
  requirements document for atomicity, testability, EARS conformance, NFR
  measurability (five-element rule), interface contract completeness (five
  dimensions), state-driven complementary-pair coverage, fabricated rationale,
  smuggled design, traceability gaps, or the Spec Ambiguity Test. Reads from
  designer, tester, and (where stakeholder outcomes are at stake) user
  perspectives. Does not rewrite — produces verdict and findings only; the
  matched author skill consumes findings and rewrites. Triggers — review this
  requirements document, audit these requirements, verdict on requirements
  draft, find anti-patterns in this doc, check requirement testability,
  what's wrong with this requirements doc.
---

# Review requirements specification document

This skill takes one requirements specification document as input and produces a structured verdict plus a list of findings. When asked to soften a hard-reject finding: refuse and restate the trigger. When asked to rewrite: refuse and route to the matched author skill. When content is missing: emit the missing-content finding, do not invent.

The skill is self-contained. Every check, anti-pattern catalog, and gate it needs is bundled in `references/` and `templates/`. No external lookups.

## When to use

Activate this skill when the user asks to:

- Review, audit, or check a requirements specification document
- Get a verdict on whether a requirements draft is ready
- Find anti-patterns in a requirements document
- Check a specific requirement for testability, atomicity, or solution-freedom
- Validate NFR measurability or interface contract completeness
- Identify what is wrong with a requirements draft

Do **not** activate this skill for:

- Writing or rewriting requirements — use the matched author skill for this artifact type
- Writing tests, designs, or architecture allocations — use the author skill for the relevant downstream artifact type (architecture, detailed design, or test specification)
- Reviewing other artifact types (architecture, ADR, detailed design, test specification) — use the matched review skill for the artifact type under review

## Inputs

- **Required**: the requirements document under review (Markdown with YAML front-matter and embedded YAML blocks per requirement).
- **Optional**: the upstream input the document derives from (parent requirements, product brief, governing architectural decisions). Used for cross-validation and for DESIGN_ISSUE detection.
- **Optional**: a scope path, used for level-confusion checks if the document's frontmatter `scope` is missing or contested.

## Output

A single review block in this shape:

```yaml
review:
  document: <document identifier or path>
  reviewer: vmodel-skill-review-requirements
  date: <YYYY-MM-DD>
  verdict: APPROVED | REJECTED | DESIGN_ISSUE
  findings:
    - <finding>
    - <finding>
    ...
  summary: |
    <2-4 sentence summary; verdict justification>
```

Each finding follows the schema in `templates/finding.yaml.tmpl`. The full verdict template is in `templates/verdict.md.tmpl`.

## Verdict decision table

Walk top to bottom — first match wins:

| # | Condition | Verdict |
|---|---|---|
| 1 | Any DESIGN_ISSUE trigger fires (see below) | **DESIGN_ISSUE** |
| 2 | Any hard-reject trigger fires (see below) | **REJECTED** |
| 3 | Any soft-reject finding present (Quality Bar item is No, or anti-pattern hit other than the hard-reject ones) | **REJECTED** |
| 4 | Spec Ambiguity Test fails | **REJECTED** |
| 5 | Only `info` findings, or no findings at all | **APPROVED** |

See `references/quality-bar-gate.md` for the canonical verdict-decision table and trigger catalog.

## Hard-reject and DESIGN_ISSUE triggers

See `references/quality-bar-gate.md` for the canonical verdict-decision table and trigger catalog. DESIGN_ISSUE detection requires the optional upstream input; without it, the skill cannot surface most DESIGN_ISSUE triggers and should note this in the verdict's `summary` field.

## Finding format

Every finding follows this schema (full template in `templates/finding.yaml.tmpl`):

```yaml
- id: F-NNN
  requirement_id: <REQ-id or "GLOBAL" for document-wide>
  check_failed: <dotted catalog identifier>
  severity: hard_reject | soft_reject | info
  category: vocabulary | type | statement-quality | nfr | interface | constraints | rationale | traceability | anti-pattern | meta-gate | design-issue
  evidence: |
    <verbatim quote from the document or specific structural observation>
  recommended_action: |
    <generic pointer — what kind of fix; never specific replacement wording>
```

### Severity levels

- **hard_reject** — fatal; one occurrence triggers REJECTED (or DESIGN_ISSUE if upstream).
- **soft_reject** — Quality Bar item No; anti-pattern hit (other than hard-reject ones); accumulates to REJECTED.
- **info** — observation worth surfacing but not affecting verdict (e.g., glossary term defined but unused).

### Check identifier catalog

The full catalog of `check_failed` identifiers lives in `references/quality-bar-gate.md`. Identifiers are stable and dotted: `<category>.<specific-check>`. Anti-patterns use the `anti-pattern.<name>` namespace. Structural / completeness checks use `check.<area>.<specific>`. DESIGN_ISSUE checks use `design-issue.<specific>`.

### Recommended-action discipline

`recommended_action` points to the kind of fix and the relevant author-side reference; it does **not** write the replacement statement. Acceptable: *"Split into atomic statements per the EARS one-shall rule."* Not acceptable: *"Replace with: 'When a user submits a credential pair, the system shall validate it against the identity provider.'"* The latter is the author skill's job.

## Orchestration — eight steps

Read the document once before any sweep. Then run in this order:

### Step 1 — Structural / vocabulary pre-check

- Frontmatter present and parseable
- `id`, `scope`, `parent_scope`, `status`, `date` populated
- For retrofit-mode documents (presence of `recovery_status` block): all per-section recovery_status values present and use only allowed enum values
- Glossary section present and non-empty (unless document is trivially small)
- Every domain term used in a requirement statement appears in the glossary
- No generic placeholder terms (Manager, Service, Handler, Record, Processor) used as if they were domain concepts

→ See `references/constraints-and-glossary-checks.md`

If the document fails structural pre-checks fatally (unparseable, no scope, retrofit-mode without recovery_status), HALT — return a `not-reviewable` error rather than a verdict.

### Steps 2–4 — Perspective-Based Reading (PBR)

Apply Perspective-Based Reading: Designer always; Tester always; User/Stakeholder when the document's scope is root (`parent_scope: null`) OR any inherited constraint has `category: regulatory`.

| Perspective | Question |
|---|---|
| Designer | "If I were allocating this to children, could I?" |
| Tester | "If I were deriving test cases from this, could I?" |
| User / stakeholder | "Does this capture what the stakeholder actually wants?" |

→ See `references/statement-quality-checks.md`, `references/ears-conformance.md`, `references/anti-patterns-catalog.md`

### Step 5 — Anti-pattern catalog sweep

Mechanical pass through the 16 patterns. Each pattern has tells; flag occurrences.

→ See `references/anti-patterns-catalog.md`

### Step 6 — Quality Bar gate

Walk the Yes/No checklist; every No is a finding. The checklist groups: vocabulary, type clarity, statement-level quality, NFR measurability, interface completeness, constraints discipline, rationale (no fabrication), traceability completeness, retrofit honesty (when applicable).

→ See `references/quality-bar-gate.md`

### Step 7 — NFR / interface dimension presence checks

Two specialised mechanical sweeps:
- For every NFR: are all five elements (system / response / metric+unit / target+statistical-level / condition) present?
- For every interface requirement: are all five dimensions (protocol / message / timing / error / startup) plus DbC pre/post/invariants plus versioning present?

→ See `references/nfr-five-elements-checks.md`, `references/interface-five-dimensions-checks.md`

### Step 8 — Spec Ambiguity Test (meta-gate)

*Could a junior engineer or mid-tier AI, reading only this document plus its glossary and governing decisions, derive a defensible architecture allocation, detailed design, and test specification — without asking clarifying questions?*

When meta-gate Spec-Ambiguity-Test answer is No: emit `check.meta-gate.spec-ambiguity-test-fails` with severity `hard_reject`. Verdict is REJECTED regardless of other counts.

### Verdict assembly

Apply the decision table at the top. Emit verdict + findings. Use `templates/verdict.md.tmpl`.

## When the upstream input is not provided

If the user supplies only the document under review and not the upstream input:
- DESIGN_ISSUE detection is partially blocked. Note this in the verdict summary: *"Upstream input not provided; DESIGN_ISSUE triggers cannot be fully evaluated."*
- All other checks proceed normally.
- Verdict can still be APPROVED, REJECTED, or DESIGN_ISSUE — but a DESIGN_ISSUE verdict from incomplete inputs should be especially well-cited.

## Hard refusals

1. **Do not rewrite the document — emit findings.** The matched author skill consumes findings and rewrites; the reviewer's role is signalling, not authoring.
2. **Do not negotiate hard-reject triggers — restate the trigger and stand on the verdict.** If the user pushes back on a hard-reject finding ("but it's just a placeholder", "the rationale is fine, you're being too strict"), the verdict stands. The user can override at their own risk in their own context, but the skill does not soften the verdict.
3. **Do not invent missing content — emit the missing-content finding.** If rationale is missing and there is no `pending`/`unknown` marker, the finding is `check.rationale.missing`. Do not propose what the rationale should say.

## HALT conditions

Stop and hand back when:

1. **Document is malformed at the structural level** — Markdown + YAML cannot be parsed → return `malformed-document` error, not a verdict.
2. **Document is empty or stub-only** — no requirements present → return `not-reviewable`, route the user to the author skill.
3. **Reviewer cannot determine scope** — frontmatter `scope` field missing AND no scope path supplied AND document body does not unambiguously identify a scope → return `not-reviewable`.
4. **Document is in retrofit mode but `recovery_status` markers are absent** — this is a different artifact shape that needs explicit retrofit handling → return `not-reviewable` with a note about the retrofit shape.
5. **Scope creep** — user asks the skill to also rewrite, also do architecture review, also write tests → decline and name the right skill for each expanded ask.

When halting, return a structured error block (not a verdict): `{ status: not-reviewable | malformed-document, reason: <text>, recommended_next_step: <text> }`.

## Self-checks before delivering the verdict

Before emitting:

- [ ] Every finding has a stable `check_failed` identifier (no ad-hoc strings).
- [ ] Every `hard_reject` finding maps to one of the five hard-reject triggers.
- [ ] Every `design-issue` finding maps to one of the four DESIGN_ISSUE triggers.
- [ ] Verdict matches the decision table (no manual overrides).
- [ ] No `recommended_action` field contains specific replacement wording.
- [ ] The `summary` field cites every `hard_reject` finding; soft-rejects only when they cluster (≥3 in one category).

## Pointers

- `references/ears-conformance.md` — five EARS patterns, conformance check, compound limit, cargo-cult tell
- `references/requirement-types-classification.md` — five-type taxonomy, level-confusion check
- `references/statement-quality-checks.md` — atomic / testable / solution-free, complementary-pair, box test
- `references/nfr-five-elements-checks.md` — element-presence checklist for NFRs
- `references/interface-five-dimensions-checks.md` — dimension-presence + DbC + versioning checklist
- `references/constraints-and-glossary-checks.md` — vocabulary checks + inherited-constraint checks
- `references/rationale-and-traceability-checks.md` — fabrication tells, retrofit `recovery_status` rules, traceability completeness
- `references/anti-patterns-catalog.md` — 16 patterns with tells, severities, and `check_failed` identifiers
- `references/quality-bar-gate.md` — canonical Yes/No checklist + full identifier catalog
- `templates/verdict.md.tmpl` — APPROVED / REJECTED / DESIGN_ISSUE structured form
- `templates/finding.yaml.tmpl` — per-finding slot-fill
- `examples/good-approved-review.md` — clean review of a passing document
- `examples/bad-wrong-review.md` — counter-example: reviewer failures (false-approve, subjective-reject, missed-meta-gate)
