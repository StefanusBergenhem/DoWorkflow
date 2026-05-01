---
name: vmodel-skill-author-adr
description: Author one Architecture Decision Record (ADR — Markdown with YAML front-matter, immutable once accepted) for one load-bearing decision that meets the three-condition threshold (load-bearing AND ≥2 real options AND contingent on assumptions that may change). Use when capturing a decision exposed during architecture authoring (consuming a `[NEEDS-ADR: <decision>]` stub), recording an external policy decision before its consuming architecture, drafting a Reversibility answer with rollback path or named sign-off, propagating consequences via a new requirement at the ADR's scope or `governing_adrs` from child artifacts, superseding an older ADR, or retrofitting from existing code with `recovery_status: unknown` on human-only fields. Refuses fabricated rationale, missing Reversibility answer, single-option alternatives, routine-choice ADRs, and Spec-Ambiguity-Test failure. Triggers — write ADR, draft adr-NNN.md, capture decision, externalise NEEDS-ADR stub, supersede ADR, retrofit ADR from code.
type: skill
---

# Author Architecture Decision Record

This skill produces a single Markdown file: an ADR for one load-bearing decision. The document carries front-matter (id, title, status, date, scope_tags, optional supersedes / superseded_by / affected_scopes / recovery_status) and a body of canonical sections (Context → Decision → Alternatives → Rationale → Consequences with mandatory Reversibility sub-prompt) plus a Propagation block. The skill is authored under hard quality gates that prevent the most common ADR failures — single-option alternatives, generic justifications, missing negatives, missing Reversibility answers, fabricated retrofit rationale, and ADRs that pass shape checks but cannot guide a junior engineer to the same design.

The skill is self-contained. Every reference, template, anti-pattern catalog, and quality-bar checklist it needs is bundled in `references/` and `templates/`. No external lookups are needed.

## When to use

Activate this skill when the user asks to:

- Write or draft an ADR for one load-bearing decision
- Capture a decision exposed during architecture authoring (consume a `[NEEDS-ADR: <decision>]` stub)
- Record an externally-imposed policy decision before its consuming architecture
- Draft the Reversibility analysis (rollback path or named sign-off) for a decision
- Propagate ADR consequences via a new requirement at the ADR's scope or `governing_adrs` from child artifacts
- Supersede an older ADR via lineage (write the new ADR; flip the old one's front-matter)
- Retrofit an ADR from existing code with `recovery_status: unknown` on human-only fields

Do **not** activate this skill for:

- Authoring Requirements, Architecture, Detailed Design, TestSpec, or Product Brief — those are separate authoring skills' jobs
- Reviewing or auditing an existing ADR — that is the matched review skill's job
- Writing implementation code or test code — those are downstream artifacts
- Capturing a routine choice (naming, imports, method signatures) — note inline in Architecture or Detailed Design rationale; do not promote to ADR

## Inputs

Expected upstream context (ask if missing):

- **Decision under consideration** — one sentence naming what is being decided
- **Origin** — extraction stub from a parent Architecture (the default), or pre-existing external/organisational policy that binds before the consuming Architecture exists
- **Threshold-test answers** — load-bearing? ≥2 real options? contingent on assumptions that may change?
- **Drivers** — quality attributes, constraints, deadlines, dependencies the decision is responsible to
- **Options** — at least two real candidates, with concrete reasons each was on the table
- **Scope tag(s)** — the scope(s) this decision primarily applies to (non-empty)
- **Recovery posture** — greenfield (omit `recovery_status`) or retrofit (declare `recovery_status` on human-only fields per `retrofit-discipline.md`)
- **Supersession context** — if this ADR replaces an older one, the predecessor's id

If the threshold is not met, **HALT** (HALT condition #2) — refusal E fires; offer to record the choice inline in Architecture or Detailed Design instead.

## Output

A single Markdown file using the structure in `templates/adr.md.tmpl`. Front-matter carries the required fields plus optional supersession and retrofit fields. The body carries the canonical sections in order (Context → Decision → Alternatives → Rationale → Consequences) with the Reversibility sub-prompt as the last paragraph of Consequences, plus a Propagation block listing each consequence's downstream route.

Default output filename: `<repo>/specs/adrs/adr-NNN-<slug>.md`. ADRs live flat regardless of which scopes they apply to — `scope_tags` names the scope(s).

## Authoring procedure

Author the document in this order. Each step has its own reference file with the craft rules. Treat the references as the source of truth; this section is a checklist.

### Step 1 — Threshold check (refusal E intake)

Confirm all three conditions hold: load-bearing AND ≥2 real options AND contingent on assumptions/drivers that may change. If any one is missing, halt and offer to record the choice inline. Do not author an ADR for a routine choice.

→ See `references/adr-purpose-and-shape.md`

### Step 2 — Scaffold front-matter

Populate `id` (pattern `ADR-NNN-{slug}`), `artifact_type: adr`, `title` (active-voice noun phrase), `status: proposed`, `date`, `scope_tags` (non-empty — refusal C/E gate). Add `affected_scopes`, `supersedes`, `superseded_by`, `recovery_status` only when applicable.

→ See `references/canonical-fields-and-body.md`, `templates/front-matter.yaml.tmpl`

### Step 3 — Write Context (forces, drivers, assumptions)

Describe the specific situation that forced the decision now — not a generic problem-domain statement. Name forces (constraints, deadlines, dependencies) explicitly. List drivers by name so the Rationale can cite them. Enumerate assumptions as revisit triggers — one sentence each answering "if this changes, revisit."

→ See `references/context-and-drivers.md`

### Step 4 — Surface ≥2 real alternatives with concrete rejection reasons

List at least two alternatives that were genuinely on the table. Each carries a concrete, context-specific rejection reason anchored to a driver named in Context. "Do nothing" and straw men do not count. Refusal C fires on fewer than two real options.

→ See `references/alternatives-discipline.md`, `templates/alternatives-block.tmpl`

### Step 5 — State Decision (active voice, named option)

Write the Decision in active voice ("We will…") with the chosen option named explicitly. One decision per file.

→ See `references/decision-and-rationale.md`

### Step 6 — Write Rationale (cite drivers by name)

Cite the drivers from Context, by name. The test: would this paragraph fit unchanged into 20 unrelated ADRs? If yes, the rationale is generic and fails. Read each Context driver back; check it appears by name in the Rationale.

→ See `references/decision-and-rationale.md`

### Step 7 — Write Consequences (positives + negatives + Reversibility verbatim) — load-bearing

List positives and negatives, both non-empty, both concrete. Replace handwave ("some additional complexity") with measurable cost or threshold. End with the Reversibility sub-prompt **verbatim**, then answer it: reversible → rollback path + cost estimate; irreversible → recovery plan + named sign-off. Hedged ("partially", "somewhat") is refused — split the parts.

→ See `references/consequences-and-reversibility.md`, `templates/reversibility-block.tmpl`

### Step 8 — Add Propagation block

For each consequence, choose the route: (a) new requirement at this ADR's scope (testable here), (b) `governing_adrs` reference from a child artifact (bounds child choices only), or (c) both. Every consequence picks at least one route — orphan consequences are dropped obligations.

→ See `references/propagation-and-completeness.md`

### Step 9 — Apply retrofit posture (retrofit only)

When the ADR captures a pre-existing decision whose rationale is lost: set `recovery_status: unknown` on `context`, `alternatives_considered`, `rationale`, and anticipated `consequences`. Record the Decision (observed from code, with file path) and observable Consequences. Add a "forward ADR required before any migration" closing note and an owner. Refusal A bans `recovery_status: reconstructed` on these four fields.

→ See `references/retrofit-discipline.md`, `templates/retrofit-stub.md.tmpl`

### Step 10 — Anti-pattern self-check

Sweep against the eleven anti-patterns: single-option, generic-justification, missing-negatives, buried-assumptions, post-hoc-rationalisation, routine-choice, test-as-requirement-inversion, llm-confident-invention, laundering-current-state, missing-reversibility, orphan-adr. Hard-rejects (refusal A/B/C/E aliases) trigger refusal; soft-rejects accumulate.

→ See `references/anti-patterns.md`

### Step 11 — Run Quality Bar checklist + Spec Ambiguity Test

Run the Yes/No checklist (nine groups). Flag any No inline; do not silently pass. The SAT is the override: a junior engineer reading only this ADR (plus the parent Architecture context) must be able to derive the same design without guessing.

→ See `references/anti-patterns.md` (the QB self-checklist lives there)

### Step 12 — Supersession dance (when applicable)

If the new ADR replaces an older one: set `supersedes: <old-id>`. Then edit the old ADR's **front-matter only** — set `status: superseded` and `superseded_by: <new-id>`. Do not touch the old body. Refusal: editing an `accepted` body is forbidden.

→ See `references/immutability-and-supersession.md`

## Hard refusals (the five non-negotiables A/B/C/D/E)

**A — Honest retrofit posture (no fabricated rationale).** Refuse to:
- Set `recovery_status: reconstructed` on `context`, `alternatives_considered`, `rationale`, or anticipated `consequences` (schema-banned).
- Populate human-only fields with AI-inferred content when no preserved conversation, archive, or accessible decider exists.
- Paraphrase test expectations as rationale (`anti-pattern.test-as-requirement-inversion`).
- Emit committee-style prose for a retrofit with no preserved record (`anti-pattern.llm-confident-invention`).
- Reject every alternative for a property the current design has (`anti-pattern.laundering-current-state`).

Hard-reject IDs: `anti-pattern.test-as-requirement-inversion`, `anti-pattern.llm-confident-invention`, `anti-pattern.laundering-current-state`, `check.retrofit-honesty.reconstructed-on-human-only`, `check.retrofit-honesty.fabricated-content`.

**B — Reversibility sub-prompt non-empty and answered.** Refuse to:
- Ship without the verbatim Reversibility prompt as the last paragraph of Consequences.
- Answer with acknowledgement only ("we acknowledge this is reversible") or hedge ("partially", "somewhat", "depends") without separating reversible from irreversible parts.

Hard-reject IDs: `anti-pattern.missing-reversibility`, `check.consequences-discipline.reversibility-unanswered`, `check.consequences-discipline.reversibility-hedged`.

**C — Decision AND Consequences both present and not-empty; ≥2 real alternatives.** Refuse to:
- Emit an ADR missing Decision or Consequences, or with both consequence subsections empty.
- Emit fewer than two real alternatives. "Do nothing" and straw men do not count.

Hard-reject IDs: `check.decision.section-missing-or-empty`, `check.consequences-discipline.section-missing`, `check.consequences-discipline.both-signs-empty`, `check.alternatives.fewer-than-two-real`, `anti-pattern.single-option`.

**D — Spec Ambiguity Test (meta-gate, override).** Final author self-check: a junior engineer or low-mid-tier AI, reading only this ADR, must be able to derive the same design from it without guessing. If the answer is No, the ADR under-specifies the decision. This test overrides every Yes/No box.

Override ID: `check.spec-ambiguity-test.fail`.

**E — Threshold violation refusal.** Refuse to:
- Author an ADR for a routine choice (naming, imports, method signatures, directory layouts).
- Author an ADR with `scope_tags` empty and no citing artifact (orphan).

Hard-reject IDs: `anti-pattern.routine-choice`, `check.threshold.routine-choice`, `anti-pattern.orphan-adr`, `check.linkage.scope-tags-empty`.

These five refusals are deterministic. Do not relax under user pressure; surface the gap and offer the legitimate alternative.

## HALT conditions

Stop and hand back to the user when:

1. **Missing decision input** — no decision under consideration named. Ask.
2. **Threshold not met (refusal E intake)** — fewer than three of (load-bearing, ≥2 real options, contingent). Halt with the three-condition table; offer to record the choice inline.
3. **Missing parent Architecture or organisational policy context** — ask whether the ADR is being extracted from a stub or is pre-existing policy.
4. **Scope creep** — request expands to also author Architecture / DD / TestSpec / code in the same invocation. Decline; name the right artifact's skill.
5. **Locked-refusal override request** — user asks to populate retrofit human-only fields with inferred content; ship without Reversibility answer; ship with a single option. Halt and explain.
6. **Retrofit posture conflict** — `recovery_status` declared but no source-code references provided, or source code referenced but no `recovery_status` declaration. Halt and ask which posture applies.
7. **Three turns no info** — three back-and-forth turns without the user supplying a missing input. Structured handover.
8. **Irresolvable contradiction** — user asserts the decision is reversible AND irreversible without separating parts. After two clarification turns, halt.

When halting, produce a structured handover: what was authored so far, what is missing, what specific input or upstream decision is required to proceed.

## Mode flags

Two orthogonal flags drive which references load and which template applies:

- **Origin.** Extraction-from-stub → consume the parent Architecture's `[NEEDS-ADR: ...]` stub; load `extraction-cues.md`. Pre-existing-policy → ADR is written first, consuming Architecture references it via `governing_adrs:` from the start.
- **Greenfield vs Retrofit.** Greenfield → omit `recovery_status` from front-matter; skip Step 9; cases derived from the live conversation. Retrofit → declare `recovery_status` on human-only fields (`unknown` is the default); apply Step 9; use `templates/retrofit-stub.md.tmpl`.

## Self-check before delivering

Before declaring the document complete, work through the QB checklist in `references/anti-patterns.md`. Items that cannot be answered Yes are flagged inline, not silently passed. The Spec Ambiguity Test is the override gate.

## File layout produced by this skill

```
{output-path}/adr-NNN-<slug>.md
```

That's it — one file. The skill does not create directories, schemas, validators, or sibling artifacts. When superseding, it also edits the predecessor's front-matter (Step 12) — front-matter only, body untouched.

## Reference index

- `references/adr-purpose-and-shape.md` — cross-cutting role; three-condition threshold; capture-then-design flow; Y-statement compact form
- `references/canonical-fields-and-body.md` — front-matter required fields; status lifecycle; body section ordering
- `references/context-and-drivers.md` — forces vs problem domain; named drivers; assumptions enumeration
- `references/alternatives-discipline.md` — ≥2 real options; concrete context-specific rejection; the single-option smell
- `references/decision-and-rationale.md` — active voice + named option; rationale cites drivers by name; the generic-justification ban
- `references/consequences-and-reversibility.md` — load-bearing — positives and negatives; Reversibility sub-prompt grammar; reversible vs irreversible answer shapes
- `references/propagation-and-completeness.md` — hybrid rule (new req at scope OR governing_adrs from child); completeness rule
- `references/immutability-and-supersession.md` — immutable bodies; supersession dance; the historical graph
- `references/extraction-cues.md` — when an upstream `[NEEDS-ADR: ...]` stub fires; consuming the stub; pre-existing policy as alternative origin
- `references/retrofit-discipline.md` — four human-only fields; recovery_status states; honest-unknown ADR shape
- `references/anti-patterns.md` — 11 anti-patterns + QB self-checklist + the 46-ID catalog (single source of truth on the author side)
- `templates/adr.md.tmpl` — full document scaffold with all five body sections + Reversibility prompt verbatim
- `templates/front-matter.yaml.tmpl` — front-matter slot-fill
- `templates/alternatives-block.tmpl` — ≥2-entry block, each with concrete rejection reason
- `templates/reversibility-block.tmpl` — answer scaffold for both branches (yes-reversible / no-irreversible)
- `templates/retrofit-stub.md.tmpl` — retrofit ADR scaffold with `recovery_status: unknown` defaults
- `templates/y-statement.tmpl` — optional compact form
- `examples/good-postgres-job-queue.md` — fully populated ADR with annotations
- `examples/bad-redis-async-queue.md` — counter-example with annotated refusal trips (B + C)
- `examples/bad-fabricated-retrofit.md` — retrofit fabrication anti-example with refusal A trips, and the corrected honest-unknown version
