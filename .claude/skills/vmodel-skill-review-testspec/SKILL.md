---
name: vmodel-skill-review-testspec
description: Review one TestSpec artifact and emit a structured verdict — APPROVED, REJECTED, or DESIGN_ISSUE — plus findings tied to case IDs or document-wide concerns. Use when checking derivation-strategy coverage, `verifies` traceability (mandatory non-empty at artifact and case level), oracle specificity (weak-assertion, unbounded-negative, tautological-form), per-layer case shape (thin leaf, fixtures-rich branch, journey-narrative root), test-double discipline (named types, fake contract-test, max-double thresholds), coverage and mutation bar declaration, retrofit discipline (no inferred intent on title/notes, `recovery_status: unknown` on reconstructed `verifies`), cross-artifact traceability (DD ↔ leaf, Architecture ↔ branch, Requirements + Product Brief ↔ root), or the Spec Ambiguity Test meta-gate. Emits verdict and findings only — does not rewrite. Triggers — review testspec, audit testspec, verdict on testspec, find anti-patterns, validate verifies, audit retrofit testspec, Spec Ambiguity Test on testspec.
type: skill
---

# Review TestSpec artifact

This skill takes one TestSpec document as input and produces a structured verdict plus a list of findings. The skill is adversarial: it does not rewrite, does not negotiate hard-reject triggers, and does not invent missing content on the document's behalf.

The skill is self-contained. Every check, anti-pattern catalog, and gate it needs is bundled in `references/` and `templates/`. No external lookups.

## When to use

Activate this skill when the user asks to:

- Review, audit, or check a TestSpec document
- Get a verdict on whether a TestSpec draft is ready
- Find anti-patterns in a TestSpec document
- Validate `verifies` traceability (artifact-level and case-level non-emptiness; resolution to live upstream IDs; layer-correct granularity)
- Audit oracle specificity (weak-assertion detection, unbounded-negative detection, tautological-form detection)
- Check per-layer case shape (thin leaf, fixtures-rich branch, journey-narrative root)
- Verify test-double discipline (named types, contract-test for fakes, max-double thresholds, interaction-overuse)
- Audit the coverage-and-mutation bar declaration at artifact level
- Verify retrofit honesty (`recovery_status` discipline; no inferred intent on `title`/`notes`)
- Resolve traceability seams (DD at leaf, Architecture at branch, Requirements + Product Brief at root)
- Apply the Spec Ambiguity Test meta-gate

Do **not** activate this skill for:

- Writing or rewriting a TestSpec — that is the matched author skill's job
- Writing test code or implementation — those are downstream
- Reviewing other artifact types in the same invocation — one artifact, one verdict

## Inputs

- **Required**: the TestSpec document under review (Markdown with YAML front-matter and embedded YAML case blocks).
- **Required for `verifies` resolution**: the parent spec for the layer.
  - **Leaf TestSpec** → parent Detailed Design.
  - **Branch TestSpec** → parent Architecture.
  - **Root TestSpec** → root Requirements + Product Brief.
  - If the parent spec for the layer is absent, halt with `missing-inputs` rather than approve blind.
- **Required for ADR resolution**: any ADRs cited in `governing_adrs:` front-matter. If a `governing_adrs:` entry does not resolve, halt with `missing-inputs`.
- **Optional**: existing test-code references for retrofit-mode documents (so observed-evidence claims can be spot-checked).

## Output

A single review block. Schema:

```yaml
review:
  document: <document identifier or path>
  reviewer: vmodel-skill-review-testspec
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
| 2 | Any hard-reject trigger fires (A / B / C / coverage-mutation section absent / broken-reference) | **REJECTED** |
| 3 | Spec Ambiguity Test fails (not upstream-traceable) | **REJECTED** |
| 4 | Any soft-reject finding present | **REJECTED** |
| 5 | Only `info` findings, or no findings at all | **APPROVED** |

**Verdict precedence**: DESIGN_ISSUE wins over REJECTED, which wins over APPROVED. If `check.spec-ambiguity-test.fail` fires AND a hard-reject trigger also fires, the verdict is DESIGN_ISSUE if the meta-gate failure is upstream-traceable (parent spec at the layer is itself ambiguous; a governing ADR is missing the load-bearing decision; an upstream Requirement or Product Brief outcome is under-specified). The findings list still contains all observed issues.

## Hard-reject triggers (non-negotiable)

Any one is fatal. Severity `hard_reject`. Do not relax under user pressure.

### A — Fabricated retrofit intent

Mirrors author skill refusal A. Hard-reject on:

- `anti-pattern.fabricated-retrofit-intent` — retrofit case carries stated intent on `title` or `notes`, OR `recovery_status` is `verified` / absent on a reconstructed `verifies` link without cited human source
- `check.retrofit.intent-on-title` — retrofit case `title` reads as an intent statement rather than an observed-behaviour scenario
- `check.retrofit.intent-on-notes` — retrofit case `notes` field carries inferred designer intent rather than observed evidence
- `check.retrofit.recovery-status-reconstructed-verifies` — `recovery_status: verified` on a `verifies` link reconstructed without human confirmation

### B — Orphan tests

Mirrors author skill refusal B. Hard-reject on:

- `check.verifies.artifact-level-empty` — front-matter `verifies: []` or absent
- `check.verifies.case-level-empty` — case missing `verifies` field, or `verifies: []`
- `check.verifies.unresolvable` — `verifies` element points at an ID that does not exist in any upstream spec, OR points at a filename rather than an artifact ID
- `anti-pattern.orphan-tests` — aggregator alias for the three above

### C — Weak assertion / unspecified oracle

Mirrors author skill refusal C. Hard-reject on:

- `anti-pattern.weak-assertions` — case `expected` is a qualitative non-bounded phrase (`"verifies behaviour"`, `"works correctly"`, `"does not throw"` as sole assertion, `"non-null"` alone, `"instance of X"` alone)
- `check.oracle.weak-assertion` — schema-level alias for the above

### Derived hard — coverage-mutation section absent

Per the load-bearing Quality Bar group, the coverage-mutation bar section must be present on every TestSpec (placeholder values acceptable; project policy fills thresholds). Section presence is a hard structural invariant; the four sub-thresholds are soft.

- `check.coverage-mutation.section-missing` — front-matter has no `coverage_mutation_bar:` block at all (HARD)

### D — Spec Ambiguity Test (meta-gate, override)

`check.spec-ambiguity-test.fail` — a junior engineer reading only this TestSpec, the parent spec for the layer, and governing ADRs cannot write the test code implementing every case as specified; OR a reviewer reading only the TestSpec cannot tell whether every equivalence class, every boundary, and every error path was considered.

**Precedence:** DESIGN_ISSUE if the failure is upstream-traceable; REJECTED otherwise. Either way, every other observed issue is still listed.

### Broken-reference integrity

`check.adr.governing-not-resolved` — an ADR id listed in `governing_adrs:` does not resolve to an actual ADR document. Hard-reject — broken reference is a document-integrity failure regardless of other quality.

`check.derived-from.unresolvable` — `derived_from` contains an id that does not resolve. Hard-reject.

## HALT conditions

Stop and hand back a structured error block (not a verdict) when:

1. **Artifact missing or unparseable** — file not at the named path, empty, or fails YAML parse. Return `not-reviewable`.
2. **Out-of-lane rewrite request** — user asks the skill to "just fix the TestSpec". Refuse; route to the matched author skill.
3. **Cross-artifact review request** — user also asks the skill to review the parent DD, sibling TestSpec, or ADR in the same invocation. Refuse; one artifact, one verdict.
4. **Missing review inputs** — TestSpec provided but parent spec at the layer absent, OR `governing_adrs:` references unresolvable ADRs, OR `derived_from` contains unresolvable ids. Return `missing-inputs`.
5. **Irresolvable check ambiguity after 2 turns** — finding evidence is genuinely ambiguous; halt rather than guess.

When halting, return: `{ status: not-reviewable | missing-inputs | malformed-document, reason: <text>, recommended_next_step: <text> }`.

## Review procedure — eight-step sweep

Read the document once before any sweep. Then run in this order:

### Step 1 — Shape and metadata sweep

All required front-matter fields present (`id`, `scope`, `level`, `verifies`, `derived_from`, `coverage_mutation_bar`, `recovery_status` when retrofit); id pattern conformant; declared `level` matches scope position (root→system / non-leaf→integration / leaf→unit); case-block structure well-formed.

→ See `references/testspec-shape-checks.md`

### Step 2 — Derivation strategy sweep

Every case has `type` from the closed enum. Each applicable derivation strategy was applied: functional (RBT) per behaviour rule, ECP+BVA per input range, error-path coverage per error-matrix row at leaf, plus state-transition / contract / property / fault-injection / performance / security / accessibility / error-guessing where the parent spec demands.

→ See `references/derivation-strategy-checks.md`

### Step 3 — Per-layer weight sweep

Layer-correct case shape: thin leaf (title + type + verifies + inputs + expected; steps usually omitted); fixtures-rich branch (preconditions name fixtures/doubles/seeds/environment; steps enumerate cross-child interactions); journey-narrative root (preconditions name environment/tenants/flags/personas; expected in Product Brief vocabulary).

→ See `references/per-layer-weight-checks.md`

### Step 4 — Case quality and oracle sweep

F.I.R.S.T. tells (no shared mutable state, no real clocks/random without naming, deterministic). AAA structure (one Act per case). Oracle specificity (refusal C delegates to oracle-checks); unbounded-negative detection; tautological-form detection.

→ See `references/case-quality-checks.md`, `references/oracle-checks.md`

### Step 5 — Verifies and test-double sweep

`verifies` non-empty at artifact and case level (refusal B); each ID resolves to a live upstream element; granularity per layer (leaf→DD field; branch→Architecture interface or composition invariant; root→Requirement or Product Brief outcome). Test doubles: named type per double (dummy/stub/spy/mock/fake); fake declares contract-test pointer; max 2 doubles per leaf case; interaction-verification reserved for cases where the interaction is the observable behaviour.

→ See `references/verifies-traceability-checks.md`, `references/test-double-discipline-checks.md`

### Step 6 — Coverage-mutation bar + integration / system specifics

Coverage-mutation section present (HARD if absent). Structural threshold, mutation threshold, mutation tool category, enforcement frequency named (placeholder values acceptable; soft if absent). Integration / system specifics: contract-testing presence at branch; environment shape named at branch and root; specialised cases for quality-attribute requirements; version pinning in preconditions where the environment depends on it.

→ See `references/coverage-mutation-bar-checks.md`, `references/integration-and-system-checks.md`

### Step 7 — Retrofit discipline + cross-artifact traceability sweep

Retrofit posture honest: `recovery_status` declared on every reconstructed link; no intent on `title` or `notes` (refusal A); gap report present. Cross-artifact seam coverage: DD error-matrix rows / postconditions / invariants / `[NEEDS-TEST: ...]` markers covered (leaf); Architecture interfaces / composition invariants / quality-attribute allocations covered (branch); Requirements + Product Brief outcomes covered (root).

→ See `references/retrofit-discipline-checks.md`, `references/dd-traceability-checks.md`, `references/architecture-traceability-checks.md`, `references/requirements-traceability-checks.md`

### Step 8 — Anti-pattern catalog + Quality Bar gate + Spec Ambiguity Test

Mechanical pass through the 13 anti-patterns; walk the Quality Bar Yes/No gate covering the full canonical catalog; finally apply the Spec Ambiguity Test as the override.

→ See `references/anti-patterns-catalog.md`, `references/quality-bar-gate.md`

### Verdict assembly

Apply the decision table. Emit verdict + findings using `templates/verdict.md.tmpl`.

## Finding format

Every finding follows this schema (full template in `templates/finding.yaml.tmpl`):

```yaml
- id: F-NNN
  case_id: <case id | "GLOBAL" | "FRONTMATTER">
  check_failed: <dotted catalog identifier>
  severity: hard_reject | soft_reject | info
  category: shape | derivation | per-layer | case-quality | oracle | verifies | test-double | coverage-mutation | integration-system | retrofit | dd-traceability | architecture-traceability | requirements-traceability | meta-gate
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

`recommended_action` points to the rule violated and the relevant author-side reference; it does not write the replacement text. The matched author skill rewrites; this skill points to the rule, not the replacement text.

### Catalog discipline

Every `check_failed` identifier must appear in `references/quality-bar-gate.md` (canonical catalog) or `references/anti-patterns-catalog.md` (anti-pattern aliases). Do not invent ad-hoc strings. If a check is genuinely missing from the catalog, surface it as a self-review note rather than minting a new identifier mid-review.

## Hard refusals

1. **Do not rewrite the document.** Findings are findings; the matched author skill rewrites.
2. **Do not negotiate hard-reject triggers.** If the user pushes back ("but the orphan case is fine", "the assertion is good enough"), the verdict stands.
3. **Do not invent missing content.** If `verifies` is empty, the finding is `check.verifies.case-level-empty`. Do not propose what the verifies should resolve to.
4. **Do not name the sister skill explicitly.** When pointing the user to the rewriter, say "the matched author skill" rather than naming it.

## Conditional gating

Several checks apply only under stated conditions:

- All `check.retrofit.*` identifiers apply only when front-matter declares `recovery_status:`
- `check.adr.governing-not-resolved` applies only when `governing_adrs:` is non-empty
- `check.dd-traceability.*` applies only at leaf scope
- `check.architecture-traceability.*` applies only at branch (non-leaf, non-root) scope
- `check.requirements-traceability.*` applies only at root scope
- `check.test-doubles.*` applies only when preconditions declare doubles

`references/quality-bar-gate.md` records gating per id.

## Self-checks before delivering

- [ ] Every finding has a stable `check_failed` identifier (no ad-hoc strings)
- [ ] Every `hard_reject` finding maps to one of A / B / C / coverage-mutation-section-missing / broken-reference, or is the meta-gate
- [ ] Verdict matches the decision table (no manual overrides)
- [ ] No `recommended_action` field contains specific replacement wording
- [ ] The `summary` field cites dominant findings, not every finding
- [ ] If meta-gate fires, the upstream-traceability question is answered explicitly (DESIGN_ISSUE vs REJECTED)
- [ ] If `governing_adrs:` non-empty, every entry was checked for resolution
- [ ] If retrofit mode, `recovery_status` discipline was applied per layer

## Pointers

- `references/testspec-shape-checks.md` — front-matter, id pattern, level-scope alignment
- `references/derivation-strategy-checks.md` — per-strategy coverage, type enum, uncovered-rule detection
- `references/per-layer-weight-checks.md` — leaf / branch / root case shape
- `references/case-quality-checks.md` — F.I.R.S.T., AAA, determinism
- `references/verifies-traceability-checks.md` — artifact + case-level non-emptiness, resolution, granularity
- `references/oracle-checks.md` — weak-assertion, unbounded-negative, tautological-form
- `references/test-double-discipline-checks.md` — named types, fake-contract-test, max thresholds
- `references/coverage-mutation-bar-checks.md` — section presence, threshold/tool/frequency declarations
- `references/integration-and-system-checks.md` — contract testing, environment shape, specialised QA cases
- `references/retrofit-discipline-checks.md` — refusal A enforcement, recovery_status narrowing, gap report
- `references/dd-traceability-checks.md` — leaf seam against parent DD
- `references/architecture-traceability-checks.md` — branch seam against parent Architecture
- `references/requirements-traceability-checks.md` — root seam against Requirements + Product Brief
- `references/anti-patterns-catalog.md` — 13 anti-patterns with tells, identifiers, severities
- `references/quality-bar-gate.md` — canonical Yes/No gate + full identifier catalog
- `templates/verdict.md.tmpl` — APPROVED / REJECTED / DESIGN_ISSUE structured form
- `templates/finding.yaml.tmpl` — per-finding slot-fill
- `examples/good-approved-review.md` — clean review of a passing document
- `examples/bad-wrong-review.md` — counter-example: reviewer failure modes annotated
