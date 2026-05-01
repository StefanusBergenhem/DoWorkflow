---
name: vmodel-skill-review-adr
description: Review one Architecture Decision Record (ADR) and emit a structured verdict — APPROVED, REJECTED, or DESIGN_ISSUE — plus findings tied to ADR sections (Context, Decision, Alternatives, Rationale, Consequences) or document-wide. Use when checking the three-condition threshold (load-bearing AND ≥2 real options AND contingent on changeable assumptions), canonical body shape, named-driver Context, ≥2-real-alternatives discipline, driver-citing Rationale (not generic praise), Consequences both-signs, Reversibility sub-prompt answered with rollback path or named sign-off, propagation hybrid rule (new requirement OR `governing_adrs` from child), supersession lineage, scope_tags non-empty, retrofit honesty (`recovery_status: unknown` on human-only fields; no fabricated rationale), or the Spec Ambiguity Test meta-gate. Emits verdict and findings only — does not rewrite. Triggers — review ADR, audit ADR, verdict on ADR, check reversibility, validate alternatives, retrofit ADR.
type: skill
---

# Review ADR artifact

This skill takes one Architecture Decision Record as input and produces a structured verdict plus a list of findings. The skill is adversarial: it does not rewrite, does not negotiate hard-reject triggers, and does not invent missing rationale, alternatives, or reversibility content on the document's behalf.

The skill is self-contained. Every check, anti-pattern catalog, and gate it needs is bundled in `references/` and `templates/`. No external lookups.

## When to use

Activate this skill when the user asks to:

- Review, audit, or check an ADR document
- Get a verdict on whether an ADR draft is ready
- Find anti-patterns in an ADR document
- Validate the three-condition threshold (load-bearing AND ≥2 real options AND contingent on changeable assumptions)
- Check canonical body shape (Context / Decision / Alternatives / Rationale / Consequences) and front-matter integrity
- Audit Context for named forces and explicit drivers
- Verify ≥2 real alternatives with concrete context-specific rejection reasons
- Confirm the Rationale cites drivers from this ADR by name (not generic praise)
- Audit Consequences for both signs and the Reversibility sub-prompt answered (rollback path or named sign-off)
- Apply the propagation hybrid rule (new requirement at scope OR `governing_adrs` from child OR both)
- Check supersession lineage integrity (`supersedes` ↔ predecessor's `superseded_by`)
- Audit retrofit honesty (`recovery_status: unknown` on human-only fields; no fabricated rationale; no laundered current state; no test-as-requirement inversion)
- Apply the Spec Ambiguity Test meta-gate

Do **not** activate this skill for:

- Writing or rewriting an ADR — that is the matched author skill's job
- Reviewing other artifact types in the same invocation — one artifact, one verdict
- Executing schema validation or traceability scans — those are mechanical-tool jobs

## Inputs

- **Required**: the ADR document under review (Markdown with YAML front-matter).
- **Required for `supersedes` / `superseded_by` resolution**: the cited predecessor or successor ADR. If a lineage entry is set but the cross-reference cannot be resolved, halt with `missing-inputs`.
- **Optional, for back-resolution flag**: citing artifacts (Architecture / DD / TestSpec) that may name this ADR via `governing_adrs:`. Absence triggers a flag-not-scan finding (soft) — review surfaces the suspicion without a heavy cross-tree walk.
- **Optional, for retrofit spot-checks**: source-code references when `recovery_status:` is declared.

## Output

A single review block. Schema:

```yaml
review:
  document: <document identifier or path>
  reviewer: vmodel-skill-review-adr
  date: <YYYY-MM-DD>
  verdict: APPROVED | REJECTED | DESIGN_ISSUE
  findings:
    - <finding>
    ...
  summary: |
    <2-4 sentence summary; verdict justification>
```

Each finding follows `templates/finding.yaml.tmpl`. The full verdict template is in `templates/verdict.md.tmpl`.

## Verdict decision table

Walk top to bottom — first match wins:

| # | Condition | Verdict |
|---|---|---|
| 1 | Spec Ambiguity Test fails AND failure is upstream-traceable | **DESIGN_ISSUE** |
| 2 | Any hard-reject trigger fires (A / B / C / D / E or broken-reference integrity) | **REJECTED** |
| 3 | Spec Ambiguity Test fails (not upstream-traceable) | **REJECTED** |
| 4 | Any soft-reject finding present | **REJECTED** |
| 5 | Only `info` findings, or no findings at all | **APPROVED** |

**Verdict precedence**: DESIGN_ISSUE wins over REJECTED, which wins over APPROVED. If `check.spec-ambiguity-test.fail` fires AND a hard-reject trigger also fires, the verdict is DESIGN_ISSUE if the meta-gate failure is upstream-traceable (the parent Architecture's stub under-specifies the decision; an upstream policy ADR is missing the load-bearing constraint; the Product Brief outcome the decision serves is itself ambiguous). The findings list still contains all observed issues.

## Hard-reject triggers (non-negotiable)

Any one is fatal. Severity `hard_reject`. Do not relax under user pressure.

### A — Honest retrofit posture (no fabricated rationale)

When the ADR captures a pre-existing decision whose rationale is lost, the four human-only fields (`context`, `alternatives_considered`, `rationale`, anticipated `consequences`) carry `recovery_status: unknown`. Schema bans `reconstructed` on these fields; AI inference is refused at the skill level.

- `check.retrofit-honesty.reconstructed-on-human-only`
- `check.retrofit-honesty.fabricated-content`
- `anti-pattern.test-as-requirement-inversion`
- `anti-pattern.llm-confident-invention`
- `anti-pattern.laundering-current-state`

### B — Reversibility sub-prompt answered

Every Consequences section ends with the verbatim Reversibility prompt, **answered** rather than acknowledged. Hedged answers ("partially reversible", "somewhat reversible", "depends") without separating the parts are refused.

- `check.consequences-discipline.reversibility-unanswered`
- `check.consequences-discipline.reversibility-hedged`
- `anti-pattern.missing-reversibility`

### C — Decision AND Consequences both present, Alternatives ≥2 real

The load-bearing structural pair plus the option-space invariant. An ADR missing Decision or Consequences, or carrying only one real alternative, is incomplete by definition.

- `check.decision.section-missing-or-empty`
- `check.consequences-discipline.section-missing`
- `check.consequences-discipline.both-signs-empty`
- `check.alternatives.fewer-than-two-real`
- `anti-pattern.single-option`

### D — Spec Ambiguity Test meta-gate (override)

A junior engineer or low-mid-tier AI, reading only this ADR, must be able to derive the same design from it without guessing. Override: if it fails, verdict is REJECTED (or DESIGN_ISSUE if upstream-traceable).

- `check.spec-ambiguity-test.fail`

### E — Threshold violation

The ADR-worthy threshold is **all three** of: load-bearing AND ≥2 real options AND contingent on assumptions/drivers that may change. ADRs for routine choices (naming, imports, method signatures) are hard-rejected so the signal-to-noise ratio of the ADR stream does not collapse.

- `check.threshold.routine-choice`
- `anti-pattern.routine-choice`

### Broken-reference integrity

Front-matter integrity failures are document-integrity hard-rejects regardless of other quality.

- `check.front-matter.required-field-missing`
- `check.front-matter.id-pattern-invalid`
- `check.status.invalid-lifecycle-state`
- `check.linkage.scope-tags-empty` (also schema-enforced via `minItems: 1`; the orphan-ADR aggregator)
- `anti-pattern.orphan-adr`

## HALT conditions

Stop and hand back a structured error block (not a verdict) when:

1. **Artifact missing or unparseable** — file not at the named path, empty, or fails YAML parse. Return `not-reviewable`.
2. **Out-of-lane rewrite request** — user asks the skill to "just fix the ADR". Refuse; route to the matched author skill.
3. **Cross-artifact review request** — user also asks the skill to review the parent Architecture, sibling ADR, or governed Detailed Design in the same invocation. Refuse; one artifact, one verdict.
4. **Missing review inputs** — `supersedes` / `superseded_by` references unresolvable; OR governing-citing artifacts not available when SAT-failure-upstream-traceable suspected. Return `missing-inputs`.
5. **Irresolvable check ambiguity after 2 turns** — finding evidence is genuinely ambiguous; halt rather than guess.

When halting, return: `{ status: not-reviewable | missing-inputs | malformed-document, reason: <text>, recommended_next_step: <text> }`.

## Review procedure — eight-step sweep

Read the document once before any sweep. Then run in this order:

### Step 1 — Shape and front-matter sweep

Required front-matter fields present (`id`, `artifact_type`, `status`, `scope_tags`, `date`); `id` pattern conformant; `status` in the enum; canonical body sections present and ordered.

→ See `references/front-matter-and-body-checks.md`

### Step 2 — Threshold and orphan-ADR sweep

Three-condition threshold (load-bearing AND ≥2 real options AND contingent on changeable assumptions); routine-choice rejection; `scope_tags` non-empty; flag-not-scan back-resolution check for orphan suspicion.

→ See `references/adr-purpose-and-shape-checks.md`, `references/linkage-and-lineage-checks.md`

### Step 3 — Context and drivers sweep

Specific situation (not generic problem-domain prose); forces named (constraints, deadlines, dependencies); drivers explicit so the Rationale can cite them by name; assumptions enumerated rather than buried in prose.

→ See `references/context-and-drivers-checks.md`

### Step 4 — Alternatives sweep

≥2 real alternatives (refusal C); each rejection reason concrete and context-specific; no straw men; no post-hoc rationalisation tell (rejected alternatives reduce to the chosen option).

→ See `references/alternatives-checks.md`

### Step 5 — Decision and Rationale sweep

Decision in active voice with the chosen option named; Rationale cites drivers from this ADR by name (not generic praise); the paste-into-twenty-other-ADRs test for genericity.

→ See `references/decision-rationale-checks.md`

### Step 6 — Consequences and Reversibility sweep (load-bearing)

Section present; both signs non-empty and concrete; Reversibility verbatim prompt present and answered (refusal B); reversible answer carries a rollback path; irreversible answer carries a recovery plan and a named human signoff.

→ See `references/consequences-and-reversibility-checks.md`

### Step 7 — Propagation, completeness, linkage sweep

Hybrid propagation rule (new requirement at this scope OR `governing_adrs` from child OR both); completeness rule (no orphan consequences); supersession chain integrity; `affected_scopes` set when reach exceeds `scope_tags`.

→ See `references/propagation-and-completeness-checks.md`, `references/linkage-and-lineage-checks.md`

### Step 8 — Retrofit + anti-pattern catalog + QB gate + Spec Ambiguity Test

Retrofit sweep gated on `recovery_status:` declaration (refusal A enforcement). Mechanical pass through the 11 anti-patterns. Walk the Quality Bar Yes/No gate covering the full canonical catalog. Finally apply the Spec Ambiguity Test as the override.

→ See `references/retrofit-discipline-checks.md`, `references/anti-patterns-catalog.md`, `references/quality-bar-gate.md`

### Verdict assembly

Apply the decision table. Emit verdict + findings using `templates/verdict.md.tmpl`.

## Finding format

Every finding follows this schema (full template in `templates/finding.yaml.tmpl`):

```yaml
- id: F-NNN
  section: <Context | Decision | Alternatives | Rationale | Consequences | FRONTMATTER | GLOBAL>
  check_failed: <dotted catalog identifier>
  severity: hard_reject | soft_reject | info
  category: shape | context | alternatives | decision-rationale | consequences-reversibility | propagation-completeness | linkage-lineage | retrofit | threshold | meta-gate
  evidence: |
    <verbatim quote or location reference from the artifact under review>
  recommended_action: |
    <generic pointer to the rule violated; never specific replacement wording>
```

### Severity

| Severity | Meaning |
|---|---|
| **hard_reject** | Fatal; one occurrence triggers REJECTED (or DESIGN_ISSUE if meta-gate fires upstream-traceably) |
| **soft_reject** | Quality Bar item No or anti-pattern hit other than the hard-reject ones; accumulates to REJECTED |
| **info** | Observation worth surfacing but does not affect verdict |

### `recommended_action` discipline

`recommended_action` points to the rule violated and the relevant author-side reference; it does not write the replacement text. The matched author skill rewrites; this skill points to the rule, not the replacement.

### Catalog discipline

Every `check_failed` identifier must appear in `references/quality-bar-gate.md` (canonical catalog) or `references/anti-patterns-catalog.md` (anti-pattern aliases). Do not invent ad-hoc strings. If a check is genuinely missing from the catalog, surface it as a self-review note rather than minting a new identifier mid-review.

## Hard refusals

1. **Do not rewrite the document.** Findings are findings; the matched author skill rewrites.
2. **Do not negotiate hard-reject triggers.** If the user pushes back ("but the single option is fine", "Reversibility is obvious"), the verdict stands.
3. **Do not invent missing content.** If `alternatives_considered` is empty or carries one option, the finding is `check.alternatives.fewer-than-two-real`. Do not propose what the second option should have been.
4. **Do not name the matched author skill explicitly.** When pointing the user to the rewriter, say "the matched author skill" rather than naming it.

## Conditional gating

Several checks apply only under stated conditions:

- All `check.retrofit-honesty.*` identifiers and anti-patterns 7 / 8 / 9 apply only when front-matter declares `recovery_status:`
- `check.linkage.supersession-chain-broken` and `check.linkage.both-supersedes-and-superseded-set` apply only when `supersedes:` or `superseded_by:` is set
- `check.consequences-discipline.reversibility-rollback-missing` applies only when the Reversibility answer is "yes"
- `check.consequences-discipline.reversibility-signoff-missing` applies only when the Reversibility answer is "no"
- `check.linkage.governing-adrs-back-resolution-flag` and `check.completeness.consequence-orphan-suspected` are flag-not-scan — review surfaces the suspicion without a heavy cross-tree walk

`references/quality-bar-gate.md` records gating per id.

## Self-checks before delivering

- [ ] Every finding has a stable `check_failed` identifier (no ad-hoc strings)
- [ ] Every `hard_reject` finding maps to one of A / B / C / D / E or broken-reference integrity, or is the meta-gate
- [ ] Verdict matches the decision table (no manual overrides)
- [ ] No `recommended_action` field contains specific replacement wording
- [ ] The `summary` field cites dominant findings, not every finding
- [ ] If meta-gate fires, the upstream-traceability question is answered explicitly (DESIGN_ISSUE vs REJECTED)
- [ ] If `supersedes` or `superseded_by` set, the cross-reference was checked for resolution
- [ ] If retrofit mode (`recovery_status:` declared), refusal A enforcement was applied to every human-only field
- [ ] The matched author skill is not named explicitly anywhere in the verdict block

## Pointers

- `references/adr-purpose-and-shape-checks.md` — threshold, orphan-ADR, routine-choice rejection
- `references/front-matter-and-body-checks.md` — required fields, id pattern, status enum, body section presence/order
- `references/context-and-drivers-checks.md` — context-completeness QB group + buried-assumptions tells
- `references/alternatives-checks.md` — option-space-integrity QB group + single-option / post-hoc tells
- `references/decision-rationale-checks.md` — decision-clarity QB group + generic-justification tells
- `references/consequences-and-reversibility-checks.md` — consequences-discipline QB group; refusal B; load-bearing
- `references/propagation-and-completeness-checks.md` — completeness-rule + propagation-rule QB groups
- `references/linkage-and-lineage-checks.md` — linkage QB group; supersession chain; back-resolution flag
- `references/retrofit-discipline-checks.md` — retrofit-honesty QB group + anti-patterns 7/8/9; refusal A
- `references/anti-patterns-catalog.md` — 11 anti-patterns with tells, identifiers, severities
- `references/quality-bar-gate.md` — canonical Yes/No gate + full identifier catalog
- `templates/verdict.md.tmpl` — APPROVED / REJECTED / DESIGN_ISSUE structured form
- `templates/finding.yaml.tmpl` — per-finding slot-fill
- `examples/good-approved-review.md` — clean review of a passing ADR
- `examples/bad-wrong-review.md` — counter-example: reviewer failure modes annotated
