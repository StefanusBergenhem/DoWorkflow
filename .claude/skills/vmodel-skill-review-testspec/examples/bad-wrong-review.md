# Counter-example — what NOT to do as a reviewer

Five reviewer failure modes annotated. Each is a verdict block emitted by an undisciplined reviewer; the annotation calls out what the verdict missed and what the correct verdict would have been.

*Note: the verdict blocks shown are illustrative — the failure modes are constructed to teach reviewer discipline, not to round-trip with any specific external bad TestSpec.*

## Failure mode 1 — Missed orphan case marked soft instead of hard

The reviewer is given a TestSpec whose case TC-leaf-08 has `verifies: []`. The reviewer notes the issue but assigns `severity: soft_reject`.

```yaml
review:
  document: /specs/app/foo/bar/testspec.md
  reviewer: vmodel-skill-review-testspec
  date: 2026-05-01
  verdict: REJECTED
  inputs_provided:
    testspec_document: true
    parent_spec: true
    governing_adrs: true
    derived_from_artifacts: true
  perspectives_applied: [author, layer-derivation]
  findings:
    - id: F-001
      case_id: TC-leaf-08
      check_failed: check.verifies.case-level-empty
      severity: soft_reject              # WRONG severity
      category: verifies
      evidence: |
        Case TC-leaf-08 has `verifies: []` — empty list.
      recommended_action: |
        Add a verifies link.
  summary: |
    One soft-reject finding on missing verifies. Rejected.
  recommended_next_action: |
    Hand findings to the matched author skill for rewrite.
  counts: { hard_reject: 0, soft_reject: 1, info: 0, design_issue: 0 }
```

### What's wrong

`check.verifies.case-level-empty` is a **hard-reject** identifier — it is the schema-level alias for refusal B (orphan tests). Case-level `verifies: []` is non-negotiable; the matched author skill refuses to ship it, and review must hard-reject symmetrically. The reviewer treated it as a soft issue. The verdict is still REJECTED (so the human-visible outcome is the same), but the **counts and severity routing are wrong** — downstream tooling that aggregates hard-reject classes for prioritisation will under-report the severity, and the consumer will not know to treat refusal B as urgent.

The correct severity is `hard_reject`. Counts: `hard_reject: 1, soft_reject: 0`. Summary should name refusal B explicitly.

## Failure mode 2 — Wrong severity on weak-assertions

The reviewer is given a TestSpec where TC-leaf-03 has `expected: "verifies behaviour"`. The reviewer notes the issue and marks it `info`.

```yaml
review:
  document: /specs/app/foo/bar/testspec.md
  reviewer: vmodel-skill-review-testspec
  date: 2026-05-01
  verdict: APPROVED                    # WRONG verdict consequence
  inputs_provided:
    testspec_document: true
    parent_spec: true
    governing_adrs: true
    derived_from_artifacts: true
  perspectives_applied: [author, layer-derivation]
  findings:
    - id: F-001
      case_id: TC-leaf-03
      check_failed: anti-pattern.weak-assertions
      severity: info                   # WRONG — refusal C is hard
      category: oracle
      evidence: |
        TC-leaf-03 expected: "verifies behaviour" — qualitative phrase.
      recommended_action: |
        Tighten the assertion.
  summary: |
    One info finding on a vague assertion. Approved with a polish note.
  recommended_next_action: |
    Proceed to test-code.
  counts: { hard_reject: 0, soft_reject: 0, info: 1, design_issue: 0 }
```

### What's wrong

`anti-pattern.weak-assertions` is **hard_reject** ★ — it is refusal C, non-negotiable. The reviewer demoted it to `info` and approved the document. Refusal C exists precisely because qualitative non-bounded oracles let any implementation pass; downgrading the severity collapses the gate.

The correct severity is `hard_reject`. The verdict is REJECTED, with refusal C named in the summary.

The failure mode: the reviewer treated severity as a matter of personal calibration rather than catalog-driven discipline. Severities are encoded in `quality-bar-gate.md`; the reviewer reads them, not invents them.

## Failure mode 3 — Vague finding without check_failed catalog ID

The reviewer is given a TestSpec with a poorly-named case. The reviewer files a finding without citing a catalog id.

```yaml
findings:
  - id: F-001
    case_id: TC-leaf-05
    check_failed: looks-bad             # NOT IN CATALOG
    severity: soft_reject
    category: shape
    evidence: |
      The case feels off.
    recommended_action: |
      Make it better.
```

### What's wrong

- **`check_failed: looks-bad` is not in the catalog.** Every finding identifier must appear in `references/quality-bar-gate.md` or `references/anti-patterns-catalog.md`. The reviewer must not invent ad-hoc strings.
- **Evidence is vague.** "The case feels off" is not actionable — the matched author skill cannot rewrite from this. Evidence is verbatim quotes or specific structural observations.
- **Recommended action is vague.** "Make it better" does not point to a rule. The action must reference the rule violated and the relevant `*-checks.md` file.

The correct shape: pick a cataloged identifier (e.g. `check.shape.case-block-malformed` or `check.shape.id-pattern-violation`); quote the offending line; point to the rule. If the issue is genuinely outside the catalog, surface it as a self-review note rather than minting a new identifier mid-review.

## Failure mode 4 — recommended_action carrying specific replacement text

The reviewer is given a TestSpec whose `expected` is a weak assertion. The reviewer writes the replacement text into `recommended_action`.

```yaml
findings:
  - id: F-001
    case_id: TC-leaf-03
    check_failed: anti-pattern.weak-assertions
    severity: hard_reject
    category: oracle
    evidence: |
      TC-leaf-03 expected: "verifies behaviour" — qualitative phrase.
    recommended_action: |
      Replace with:                       # WRONG — specific replacement text
      `expected: { expires_at: 2026-04-30T12:00:00Z, refreshes_at: 2026-04-30T11:55:00Z }`
```

### What's wrong

`recommended_action` discipline is generic-pointer-only. The reviewer signals rule violations; the matched author skill rewrites under direction from cataloged checks. Writing specific replacement text:

- **Couples the reviewer to one solution** when other replacements may also satisfy the rule.
- **Crosses the lane separation** between review and authoring — the reviewer is a gatekeeper, not a stylist.
- **Pollutes the verdict block** with content that only makes sense as a rewrite proposal.

The correct shape: *"Replace the qualitative phrase with a specific value or bounded invariant. See `oracle-checks.md` (refusal C)."*

## Failure mode 5 — APPROVED that missed the meta-gate

The reviewer ticks every Quality Bar item. Every cataloged check is Yes. Anti-pattern sweep is clean. The reviewer approves.

But: the TestSpec's TC-leaf-04 has `expected: "claim returned successfully"`. Front-matter `verifies` resolves; case `verifies` resolves; oracle is technically not in the closed weak-assertion phrase set; type is `functional`. Every syntactic check is Yes. But the oracle is vacuous — a junior engineer cannot derive what to assert.

```yaml
review:
  verdict: APPROVED                    # WRONG — meta-gate fails
  findings: []
  summary: |
    All Quality Bar items checked Yes. Anti-pattern sweep clean. Approved.
  counts: { hard_reject: 0, soft_reject: 0, info: 0, design_issue: 0 }
```

### What was missed

`check.spec-ambiguity-test.fail` is the **override**. Even when every other Yes/No item passes, the meta-gate determines whether the artifact has done the job TestSpec exists to do. An oracle reading "claim returned successfully" is the kind of vacuous-prose-passing-syntactic-checks failure the meta-gate catches.

The correct finding:

```yaml
- id: F-001
  case_id: TC-leaf-04
  check_failed: check.spec-ambiguity-test.fail
  severity: hard_reject
  category: meta-gate
  evidence: |
    TC-leaf-04 expected: "claim returned successfully". A junior engineer
    cannot derive what to assert: which fields of the Claim must be present?
    What relationship to the input row? What lease window applies? The
    Spec Ambiguity Test fails on this clause.
  recommended_action: |
    Replace the vacuous oracle with a specific value or bounded invariant
    naming the fields and relationships under test. See `oracle-checks.md`.
```

The verdict is **REJECTED** if the parent DD's postcondition fully specifies the claim semantics (this TestSpec failed to derive an oracle from them); **DESIGN_ISSUE** if the parent DD itself is ambiguous about claim semantics (this TestSpec cannot derive what the parent did not specify). Either way, the meta-gate is the override and forces DESIGN_ISSUE when upstream-traceable.

The failure mode: the reviewer treated the Spec Ambiguity Test as a checkbox rather than a meta-gate. The check is irreducibly heuristic — it requires honest application of "could a junior write the test code from this oracle alone?".

## Five canonical reviewer mistakes

1. **Wrong-severity-on-hard-reject.** Marking a refusal-A / B / C / coverage-mutation-section / broken-reference as soft or info. Severities are catalog-driven; reviewer reads them, does not invent.
2. **Wrong-verdict-from-correct-finding.** Identifying the right issue but not propagating its hard severity into the verdict.
3. **Catalog-violation.** Inventing ad-hoc `check_failed` strings or vague evidence. Every identifier must appear in `quality-bar-gate.md` or `anti-patterns-catalog.md`.
4. **recommended_action overreach.** Writing specific replacement text instead of pointing to the rule. Reviewer signals; the matched author skill rewrites.
5. **Missed-meta-gate.** Treating the Spec Ambiguity Test as a checkbox. Approving documents that pass every Yes/No item but cannot be acted on by a junior engineer.

The eight-step sweep guards against (5); the catalog discipline guards against (1)–(4); the verdict precedence rule routes (5) correctly when fired.
