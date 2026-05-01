# Counter-example — what NOT to do as a reviewer

Four reviewer failure modes annotated. Each is a verdict block emitted by an undisciplined reviewer; the annotation calls out what the verdict missed and what the correct verdict would have been.

*Note: the verdict blocks are illustrative — the failure modes are constructed to teach reviewer discipline, not to round-trip with any specific external bad ADR.*

## Failure mode 1 — False approve on hedged Reversibility

The reviewer is given an ADR whose Consequences section ends with "**Reversibility.** Partially reversible." The reviewer notes the answer but approves anyway.

```yaml
review:
  document: /specs/adrs/adr-042-foo.md
  reviewer: vmodel-skill-review-adr
  date: 2026-05-01
  verdict: APPROVED                            # WRONG — refusal B is hard
  inputs_provided:
    adr_document: true
    supersedes_target: n/a
    superseded_by_target: n/a
    citing_artifacts: partial
  perspectives_applied: [architect, integrator]
  findings:
    - id: F-001
      section: Consequences
      check_failed: check.consequences-discipline.reversibility-hedged
      severity: soft_reject                    # WRONG severity
      category: consequences-reversibility
      evidence: |
        Consequences ends: "**Reversibility.** Partially reversible."
      recommended_action: |
        Tighten the Reversibility answer.
  summary: |
    One soft note on a hedged Reversibility answer. Approved with a
    polish note.
  recommended_next_action: |
    Proceed to Architecture authoring.
  counts: { hard_reject: 0, soft_reject: 1, info: 0, design_issue: 0 }
```

### What's wrong

`check.consequences-discipline.reversibility-hedged` is **hard_reject** ★ — refusal B, non-negotiable. The reviewer demoted it to `soft_reject` and then to APPROVED on top. Refusal B exists precisely because hedged answers leave the risk profile ambiguous; downgrading the severity collapses the gate.

The correct severity is `hard_reject`. The verdict is REJECTED. Summary names refusal B explicitly.

The failure mode: the reviewer treated severity as a matter of personal calibration rather than catalog-driven discipline. Severities are encoded in `quality-bar-gate.md`; the reviewer reads them, not invents them.

## Failure mode 2 — Subjective reject with no catalog identifier

The reviewer is given an ADR with stylistic preferences they disagree with. The reviewer files a finding without citing a catalog id.

```yaml
findings:
  - id: F-001
    section: Decision
    check_failed: feels-vague                 # NOT IN CATALOG
    severity: soft_reject
    category: decision-rationale
    evidence: |
      The Decision wording could be punchier.
    recommended_action: |
      Make it more decisive.
```

### What's wrong

- **`check_failed: feels-vague` is not in the catalog.** Every finding identifier must appear in `references/quality-bar-gate.md` or `references/anti-patterns-catalog.md`. The reviewer must not invent ad-hoc strings.
- **Evidence is subjective.** "Could be punchier" is taste, not a cataloged defect. Evidence is verbatim quotes or specific structural observations against a named rule.
- **Recommended action is vague.** "Make it more decisive" does not point to a rule. The action must reference the rule violated and the relevant `*-checks.md` file.

The correct shape: if the Decision is genuinely passive or omits the chosen option, file `check.decision.passive-or-unnamed-option`; if it is actively voiced and names the option, there is no defect. If the issue is genuinely outside the catalog, surface it as a self-review note rather than minting a new identifier mid-review.

## Failure mode 3 — recommended_action carrying specific replacement text

The reviewer is given an ADR with a single-option Alternatives section. The reviewer writes the replacement alternatives into `recommended_action`.

```yaml
findings:
  - id: F-001
    section: Alternatives
    check_failed: check.alternatives.fewer-than-two-real
    severity: hard_reject
    category: alternatives
    evidence: |
      Alternatives section: "- Don't use a queue." — one straw-man option;
      refusal C.
    recommended_action: |
      Add these alternatives:                 # WRONG — specific replacement text
      - Redis with Lua-scripted reliable queue. Rejected: ops familiarity zero.
      - RabbitMQ. Rejected: broker operations overhead too high.
      - Kafka. Rejected: over-engineered for expected throughput.
```

### What's wrong

`recommended_action` discipline is generic-pointer-only. The reviewer signals rule violations; the matched author skill rewrites under direction from cataloged checks. Writing specific replacement text:

- **Couples the reviewer to one solution** when other alternatives may also satisfy the rule (and may match the author's actual decision history better than the reviewer's invented options).
- **Crosses the lane separation** between review and authoring — the reviewer is a gatekeeper, not a stylist.
- **Risks fabricating options that were never actually on the table** — exactly the anti-pattern (`anti-pattern.llm-confident-invention`) the framework refuses on the author side.

The correct shape: *"Surface ≥2 real alternatives with concrete context-specific rejection reasons, or drop the ADR. See `alternatives-checks.md` (refusal C)."*

## Failure mode 4 — APPROVED that missed the meta-gate

The reviewer ticks every Quality Bar item. Every cataloged check is Yes. Anti-pattern sweep is clean. The reviewer approves.

But: the ADR's Decision reads "We will use a queue mechanism." The chosen option is not actually named (e.g., not "Postgres `SKIP LOCKED`" — just "a queue mechanism"). Three real alternatives are listed; Rationale cites two drivers; Consequences both signs populated; Reversibility answered "yes" with a rollback path. Every syntactic check is Yes. But a junior engineer reading the ADR cannot derive which queue mechanism to implement.

```yaml
review:
  verdict: APPROVED                          # WRONG — meta-gate fails
  findings: []
  summary: |
    All Quality Bar items checked Yes. Anti-pattern sweep clean. Approved.
  counts: { hard_reject: 0, soft_reject: 0, info: 0, design_issue: 0 }
```

### What was missed

`check.spec-ambiguity-test.fail` is the **override**. Even when every other Yes/No item passes, the meta-gate determines whether the ADR has done the job ADRs exist to do — make the design derivable. A Decision that says "we will use a queue mechanism" without naming the mechanism passes syntactic checks but fails the Spec Ambiguity Test.

The correct finding:

```yaml
- id: F-001
  section: Decision
  check_failed: check.spec-ambiguity-test.fail
  severity: hard_reject
  category: meta-gate
  evidence: |
    Decision reads: "We will use a queue mechanism." The chosen option is
    not named. A junior engineer cannot derive which mechanism to
    implement: Postgres SKIP LOCKED? Redis Lua? RabbitMQ? The Spec
    Ambiguity Test fails on this clause.
  recommended_action: |
    Name the chosen option in the Decision. See `decision-rationale-checks.md`
    (`check.decision.passive-or-unnamed-option` is the related catalog item;
    the meta-gate failure is the override).
```

The verdict is **REJECTED** if the Architecture stub or upstream policy ADR fully specifies which mechanism the team committed to (this ADR failed to capture it); **DESIGN_ISSUE** if the upstream stub itself was ambiguous about the mechanism (this ADR cannot specify what the parent did not). Either way, the meta-gate is the override and forces DESIGN_ISSUE when upstream-traceable.

The failure mode: the reviewer treated the Spec Ambiguity Test as a checkbox rather than a meta-gate. The check is irreducibly heuristic — it requires honest application of "could a junior derive the design from this ADR alone?".

## Four canonical reviewer mistakes

1. **Wrong-severity-on-hard-reject.** Marking a refusal-A / B / C / D / E / broken-reference as soft or info. Severities are catalog-driven; reviewer reads them, does not invent.
2. **Catalog-violation.** Inventing ad-hoc `check_failed` strings or subjective evidence. Every identifier must appear in `quality-bar-gate.md` or `anti-patterns-catalog.md`.
3. **recommended_action overreach.** Writing specific replacement text instead of pointing to the rule. Reviewer signals; the matched author skill rewrites.
4. **Missed-meta-gate.** Treating the Spec Ambiguity Test as a checkbox. Approving ADRs that pass every Yes/No item but cannot be acted on by a junior engineer.

The eight-step sweep guards against (4); the catalog discipline guards against (1)–(3); the verdict precedence rule routes (4) correctly when fired.
