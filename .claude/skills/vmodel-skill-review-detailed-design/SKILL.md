---
name: vmodel-skill-review-detailed-design
description: Review a detailed design specification document for craft quality and emit a structured verdict — APPROVED, REJECTED, or DESIGN_ISSUE — plus findings tied to Public Interface entries, Data Structure entries, error-matrix rows, or document-wide concerns. Use when checking Design-by-Contract completeness (preconditions, postconditions split by outcome, invariants, typed errors, thread-safety), data structures by invariant (ownership, lifetime, returned-object semantics), result-property algorithms (no code paraphrase, no algorithmic postconditions), state-machine completeness, error-matrix coverage, rationale, retrofit honesty (Overview narrowing), ADR traceability, TestSpec seams, or the Spec Ambiguity Test. Reads from implementer, tester, and (retrofit) historian perspectives. Emits verdict and findings only — does not rewrite. Triggers — review detailed design, audit DD, verdict on DD draft, find anti-patterns, check function contracts, validate error matrix, audit retrofit DD, Spec Ambiguity Test on DD.
type: skill
---

# Review detailed design specification document

This skill takes one Detailed Design (DD) document as input and produces a structured verdict plus a list of findings. The skill is adversarial: it does not rewrite, does not negotiate hard-reject triggers, and does not invent missing content on the document's behalf.

The skill is self-contained. Every check, anti-pattern catalog, and gate it needs is bundled in `references/` and `templates/`. No external lookups.

## When to use

Activate this skill when the user asks to:

- Review, audit, or check a Detailed Design document
- Get a verdict on whether a DD draft is ready
- Find anti-patterns in a DD document
- Validate Design-by-Contract completeness on every public function
- Audit data-structure invariants, ownership, lifetime
- Check the algorithm specifications (result-property vs algorithmic postcondition; code paraphrase)
- Verify the state machine (transitions, undefined-event handling, thread-safety)
- Audit the error-handling matrix (six questions per row, recovery strategy named, no "undefined" state-after-error)
- Verify retrofit honesty (`recovery_status` discipline; Overview narrowed to verified|unknown — schema)
- Resolve traceability (`governing_adrs:` references resolve; body citations present)
- Apply the Spec Ambiguity Test meta-gate

Do **not** activate this skill for:

- Writing or rewriting a DD — that is the matched author skill's job
- Writing tests, code, or implementation — those are downstream artifacts
- Reviewing other artifact types in the same invocation — one artifact, one verdict

## Inputs

- **Required**: the DD document under review (Markdown with YAML front-matter, embedded YAML blocks, optional Mermaid state diagrams).
- **Required for traceability checks**: parent Architecture artifact (so the leaf-allocation can be verified). If absent, halt with a `missing-inputs` verdict-shaped output rather than approve blind.
- **Required for ADR resolution**: any ADRs cited in `governing_adrs:` front-matter. If a `governing_adrs:` entry does not resolve, halt with `missing-inputs`.
- **Required for derived-from resolution**: the requirements / sibling DDs / ARCH interfaces listed in `derived_from`.
- **Optional**: source-code references for retrofit-mode documents (so observed-evidence claims can be spot-checked).

## Output

A single review block. Schema:

```yaml
review:
  document: <document identifier or path>
  reviewer: vmodel-skill-review-detailed-design
  date: <YYYY-MM-DD>
  verdict: APPROVED | REJECTED | DESIGN_ISSUE
  findings:
    - <finding>
    ...
  summary: |
    <2-4 sentence summary; verdict justification>
```

Each finding follows `templates/finding.yaml.tmpl`. The full verdict template is in `templates/verdict.md.tmpl`.

## Cross-cutting authoring discipline

Enforce the six rules in `references/authoring-discipline.md` across every review check, emitting `check.discipline.<rule>` findings on violation. Most relevant here: Rule 0 (flag `n/a + justification` on omitted slots and self-attestation prose as `check.discipline.product-shape`), Rule 1 (flag re-statement of the parent Architecture's boundary contract within the DD body — the DD describes leaf internals and references the boundary by ID — as `check.discipline.upstream-restatement`; the DD-leakage direction lives in the Architecture review), Rule 2 (recognise the combined `architecture-and-design.md` mode when present, and flag misapplied collapse as `check.discipline.collapse-eligibility-misapplied`), Rule 3 (flag re-narrated rationale that does not cite a governing ADR as `check.discipline.rationale-narration`), Rule 4 (flag a state diagram plus a transition table that state the same machine as `check.discipline.diagram-prose-duplication`), Rule 5 (flag verbatim restatement of upstream parent-Architecture-interface or governing-ADR content as `check.discipline.upstream-restatement`).

## Verdict decision table

Walk top to bottom — first match wins:

| # | Condition | Verdict |
|---|---|---|
| 1 | Spec Ambiguity Test fails AND failure is upstream-traceable | **DESIGN_ISSUE** |
| 2 | Any hard-reject trigger fires (A / B / C / broken-reference) | **REJECTED** |
| 3 | Spec Ambiguity Test fails (not upstream-traceable) | **REJECTED** |
| 4 | Any soft-reject finding present | **REJECTED** |
| 5 | Only `info` findings, or no findings at all | **APPROVED** |

**Verdict precedence**: DESIGN_ISSUE wins over REJECTED. If the meta-gate fails AND a hard-reject trigger also fires, the verdict is DESIGN_ISSUE if the meta-gate failure is upstream-traceable (parent Architecture's allocation is itself ambiguous, a governing ADR is missing the load-bearing decision, or an upstream Requirement is under-specified). The findings list still contains all observed issues.

## Hard-reject triggers (non-negotiable)

Any one is fatal. Severity `hard_reject`. Do not relax under user pressure.

### A — Honest retrofit posture violations

Mirrors author skill refusal A. Hard-reject on:

- `check.recovery-status.overview-reconstructed` — `recovery_status: { overview: reconstructed }` (schema-enum violation; Overview is human-only)
- `anti-pattern.fabricated-rationale` — generic-principle invocation in rationale fields on a retrofit ("balances X with Y", "follows clean architecture", "industry-standard") instead of historical recall
- `check.retrofit.human-only-content-marked-reconstructed` — rationale, original-intent, or rejected-alternatives fields marked `reconstructed`

### B — DD-without-parent-Architecture

Mirrors author skill refusal B. Hard-reject on:

- `check.parent-architecture.missing` — `parent_architecture` field absent or does not resolve
- `check.parent-architecture.allocation-mismatch` — DD's responsibilities do not match the parent Architecture's Decomposition entry for this leaf
- `check.dd.cross-component-content` — DD specifies cross-component composition, sibling-leaf interfaces beyond what this leaf consumes/produces, or runtime patterns spanning scopes

### C — DD-vs-code boundary violations (the two rules)

Mirrors author skill refusal C. Hard-reject on:

- `anti-pattern.code-paraphrase` — Algorithms section walks through implementation step-by-step; every clause is derivable from reading the implementation
- `anti-pattern.algorithmic-postcondition` — postcondition begins *"shall iterate"* / *"shall compute"* / *"shall walk"* instead of stating a property of the result
- `anti-pattern.permutation-half-omitted` — transformation postcondition states only one half of the property (e.g., "ordered" without "permutation"; lets `return []` pass)

### D — Spec Ambiguity Test (meta-gate, override)

`check.spec-ambiguity-test.fail` — a junior engineer cannot derive a correct implementation from this DD alone; OR a test engineer cannot derive the unit-test suite without seeing the code; OR an equivalent implementation in a different language would not satisfy the same DD.

**Precedence:** DESIGN_ISSUE if the failure is upstream-traceable; REJECTED otherwise. Either way, every other observed issue is still listed.

### Broken-reference integrity

`check.adr.governing-not-resolved` — an ADR id listed in `governing_adrs:` does not resolve to an actual ADR document. Hard-reject — broken reference is a document-integrity failure regardless of other quality.

`check.derived-from.unresolvable` — `derived_from` contains an id that does not resolve. Hard-reject.

## HALT conditions

Stop and hand back a structured error block (not a verdict) when:

1. **Artifact missing or unparseable** — file not at the named path, empty, or fails YAML parse. Return `not-reviewable`.
2. **Out-of-lane rewrite request** — user asks the skill to "just fix the DD". Refuse; route to the matched author skill.
3. **Cross-artifact review request** — user also asks the skill to review the TestSpec, sibling DD, or ADR in the same invocation. Refuse; one artifact, one verdict.
4. **Missing review inputs** — DD provided but parent Architecture absent (allocation cannot be verified), OR `governing_adrs:` references unresolvable ADRs, OR `derived_from` contains unresolvable ids. Return `missing-inputs`.
5. **Irresolvable check ambiguity after 2 turns** — finding evidence is genuinely ambiguous; halt rather than guess.

When halting, return: `{ status: not-reviewable | missing-inputs | malformed-document, reason: <text>, recommended_next_step: <text> }`.

## Review procedure — eight-step sweep

The author skill authors in 13 steps; the review skill condenses to 8 sweeps. Read the document once before any sweep. Then run in this order:

### Step 1 — Shape and metadata sweep

All seven sections present (with explicit absence assertion on stateless State sections); front-matter has `parent_architecture`, `derived_from` (non-empty), `governing_adrs` (where applicable); Overview names what slice of the parent Architecture this leaf realises.

→ See `references/dd-shape-checks.md`

### Step 2 — Public Interface contract sweep

For every public function: 9 contract elements present; postconditions split into `on_success` and `on_failure` with both branches populated; numeric parameters carry units; postconditions express result properties not steps; both halves of transformation properties stated; thread-safety category named.

→ See `references/function-contract-checks.md`

### Step 3 — Data Structures and invariants sweep

Every data structure: fields with per-field invariants where type-system insufficient; ownership and lifetime stated; returned-object semantics stated when crossing the public interface; shared mutable state names lock + happens-before + per-field reader/writer.

→ See `references/data-and-invariant-checks.md`

### Step 4 — Algorithms sweep (HARD on code paraphrase)

Result-property statements where the algorithm is implementer's choice; algorithm named AND why-named where contractual; specification-pattern fits the behaviour shape (decision table for rule-based; state machine for mode-dependent; sequence for protocols).

→ See `references/algorithm-checks.md`

### Step 5 — State and concurrency sweep

Stateless leaves assert absence in one line; stateful leaves carry state inventory + transition table + undefined-event handling; thread-safety per leaf and per shared field; cancellation contract on long-running ops; timing constraints are testable (five elements).

→ See `references/state-and-concurrency-checks.md`

### Step 6 — Error handling sweep

Six questions answered for every error class; matrix populated; recovery strategy named per row (one of fail-fast / retry-bounded / fallback / compensate / propagate); no "undefined" state-after-error; bounded retry budget; no exception swallowing; no exception tunneling.

→ See `references/error-handling-checks.md`

### Step 7 — Rationale, retrofit, ADR, TestSpec-seam sweep

Every non-obvious decision carries inline rationale with constraint kind named; load-bearing+cross-cutting+hard-to-reverse decisions extracted to ADR (not inlined); `governing_adrs:` resolves and is body-cited; retrofit posture honest (Overview narrowed; rationale verified-or-unknown); every error-matrix row maps to a TestSpec robustness target.

→ See `references/rationale-checks.md`, `references/retrofit-discipline-checks.md`, `references/adr-traceability-checks.md`, `references/testspec-traceability-checks.md`

### Step 8 — Anti-pattern catalog + Quality Bar gate + Spec Ambiguity Test

Mechanical pass through the 16 anti-patterns; walk the Quality Bar Yes/No gate; finally apply the Spec Ambiguity Test as the override.

→ See `references/anti-patterns-catalog.md`, `references/quality-bar-gate.md`

### Verdict assembly

Apply the decision table. Emit verdict + findings using `templates/verdict.md.tmpl`.

## Finding format

Every finding follows this schema (full template in `templates/finding.yaml.tmpl`):

```yaml
- id: F-NNN
  element_id: <function-name | data-structure-name | error-matrix-row-id | "GLOBAL">
  check_failed: <dotted catalog identifier>
  severity: hard_reject | soft_reject | info
  category: shape | function-contract | data-invariant | algorithm | state | error-handling | rationale | adr | testspec-traceability | retrofit | meta-gate
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

`recommended_action` points to the rule violated and the relevant author-side reference; it does not write the replacement text.

| Acceptable | Not acceptable |
|---|---|
| *"Add `postconditions.on_failure` clause per the postcondition split. See `function-contract-checks.md` `check.contract.postcondition-failure-branch-missing`."* | *"Replace with: 'on_failure: typed error TokenInvalid; no state mutation'."* |

The matched author skill rewrites; this skill points to the rule, not the replacement text.

### Catalog discipline

Every `check_failed` identifier must appear in `references/quality-bar-gate.md` (for `check.*`) or `references/anti-patterns-catalog.md` (for `anti-pattern.*`). Do not invent ad-hoc strings. If a check is genuinely missing from the catalog, surface it as a self-review note rather than minting a new identifier mid-review.

## Hard refusals

1. **Do not rewrite the document.** Findings are findings; the matched author skill rewrites.
2. **Do not negotiate hard-reject triggers.** If the user pushes back ("but the rationale is fine", "the algorithmic postcondition is just clearer"), the verdict stands.
3. **Do not invent missing content.** If a postcondition clause is missing, the finding is `check.contract.postcondition-failure-branch-missing`. Do not propose what the postcondition should say.
4. **Do not name the sister skill explicitly.** When pointing the user to the rewriter, say "the matched author skill" rather than naming it.

## Conditional gating

Several checks apply only under stated conditions:

- All `check.retrofit.*` identifiers apply only when front-matter declares `recovery_status:`
- `check.adr.governing-not-cited-in-body` applies only when `governing_adrs:` is non-empty
- `check.state.*` (machine completeness checks) apply only when the State section has substantive content (i.e., the leaf is stateful)
- `check.composition.*` does NOT apply (DD is leaf-scope; composition is the parent Architecture's concern)

`references/quality-bar-gate.md` records the gating per id.

## Self-checks before delivering

- [ ] Every finding has a stable `check_failed` identifier (no ad-hoc strings)
- [ ] Every `hard_reject` finding maps to one of A / B / C / broken-reference, or is the meta-gate
- [ ] Verdict matches the decision table (no manual overrides)
- [ ] No `recommended_action` field contains specific replacement wording
- [ ] The `summary` field cites dominant findings, not every finding
- [ ] If meta-gate fires, the upstream-traceability question is answered explicitly (DESIGN_ISSUE vs REJECTED)
- [ ] If `governing_adrs:` non-empty, every entry was checked for resolution and body-citation
- [ ] If retrofit mode, the Overview-narrowing schema rule was checked

## Pointers

- `references/authoring-discipline.md` — 6 cross-cutting rules (product-shape, layering, compression) — applies to all review checks
- `references/dd-shape-checks.md` — section presence, metadata, Overview
- `references/function-contract-checks.md` — DbC clauses, units, postcondition discipline
- `references/data-and-invariant-checks.md` — invariants, ownership, lifetime, returned-object semantics
- `references/algorithm-checks.md` — result-property vs algorithmic-postcondition; specification-pattern selection
- `references/state-and-concurrency-checks.md` — state machine, undefined-event, thread-safety, timing
- `references/error-handling-checks.md` — six-question coverage, matrix completeness, recovery strategy
- `references/rationale-checks.md` — inline rationale, constraint kinds, fabrication detection
- `references/retrofit-discipline-checks.md` — `recovery_status` legality, Overview narrowing, evidence citation
- `references/adr-traceability-checks.md` — `governing_adrs` resolution, body-citation, ADR-extraction smell
- `references/testspec-traceability-checks.md` — error-matrix → robustness; postcondition → contract; invariant → property
- `references/anti-patterns-catalog.md` — 16 anti-patterns with tells, identifiers, severities
- `references/quality-bar-gate.md` — canonical Yes/No gate + full identifier catalog
- `templates/verdict.md.tmpl` — APPROVED / REJECTED / DESIGN_ISSUE structured form
- `templates/finding.yaml.tmpl` — per-finding slot-fill
- `examples/good-approved-review.md` — clean review of a passing document
- `examples/bad-wrong-review.md` — counter-example: three reviewer failure modes annotated
