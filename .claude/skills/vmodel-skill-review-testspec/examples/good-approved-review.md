# Example — APPROVED review of a leaf-scope TestSpec

The reviewer is given a clean leaf-scope TestSpec for `app/session-store/expiry-calculator` (greenfield; parent DD `DD-session-store.expiry-calculator`; one governing ADR `ADR-021`). The reviewer was given the TestSpec, the parent DD, the governing ADR, and the upstream requirement. The reviewer applied the author and layer-derivation perspectives. Anti-pattern sweep found nothing. Spec Ambiguity Test passed.

*Note: the document under review here is a synthetic example constructed to demonstrate review craft on a clean TestSpec; it is not a round-trip review of any specific external author-side example. The matched author skill ships its own good example independently per the self-containment convention.*

## Verdict block

```yaml
review:
  document: /specs/app/session-store/expiry-calculator/testspec.md
  reviewer: vmodel-skill-review-testspec
  date: 2026-05-01

  verdict: APPROVED

  inputs_provided:
    testspec_document: true
    parent_spec: true
    governing_adrs: true
    derived_from_artifacts: true

  perspectives_applied:
    - author
    - layer-derivation

  findings:
    - id: F-001
      case_id: TC-leaf-04
      check_failed: check.shape.id-pattern-violation
      severity: info
      category: shape
      evidence: |
        TC-leaf-04 reads `title: "Token expiry at boundary - one second past
        expiry"`. The hyphen-vs-en-dash convention used elsewhere in the
        TestSpec is an em-dash. Polish-only — no defect.
      recommended_action: |
        No action — the case identifier is well-formed and the title is
        unambiguous. Optional refinement noted for cross-document consistency.

    - id: F-002
      case_id: GLOBAL
      check_failed: check.coverage-mutation.frequency-unnamed
      severity: info
      category: coverage-mutation
      evidence: |
        `coverage_mutation_bar.enforcement_frequency: per-PR` is named, with a
        cross-reference to ADR-021's CI-policy decision. Citation discipline is
        exemplary. Included as a positive observation.
      recommended_action: |
        No action — frequency is named and traceable to the policy ADR.

  summary: |
    Verdict: APPROVED. The expiry-calculator TestSpec passes every applicable
    Quality Bar item and the Spec Ambiguity Test. Front-matter is complete:
    `verifies` resolves to three DD fields; `coverage_mutation_bar` declares
    structural threshold (95%), mutation threshold (70%), tool category
    (PIT-class), and frequency (per-PR). Twelve cases cover every behaviour
    rule (functional), every numeric boundary (boundary), every error-matrix
    row (error), and the time-monotonicity invariant (property). Oracles are
    specific values or bounded predicates throughout — no qualitative phrasing.
    Each case carries non-empty `verifies` to a DD field. Two info-level
    observations noted; no defects.

  recommended_next_action: |
    Proceed to test-code derivation against this TestSpec. Implementation can
    begin in parallel using `develop-code` against the parent DD with the
    TestSpec as the test contract.

  counts:
    hard_reject: 0
    soft_reject: 0
    info: 2
    design_issue: 0
```

## What this example demonstrates

1. **APPROVED is not silent.** Two info-level observations are surfaced. They do not affect the verdict but are useful to the consumer for ongoing maintenance.

2. **Findings cite specific elements.** F-001 ties to a specific case (`TC-leaf-04`); F-002 ties to "GLOBAL" because it observes a document-wide coverage-mutation property.

3. **Recommended actions are generic and point to the rule.** Both info-level findings note "no action" because they are positive observations. When negative, the recommended action would point to the rule, not write the replacement text.

4. **The summary cites the dominant signal.** It names the load-bearing pieces (front-matter complete, coverage-mutation populated, every behaviour rule covered, oracles specific, every case has resolved `verifies`) and confirms what the Spec Ambiguity Test passed against.

5. **Counts are explicit.** Zero soft_reject, zero hard_reject; only info findings — the verdict is conservative and well-grounded.

6. **Two perspectives applied — author and layer-derivation.** Greenfield (no `recovery_status:` declared) → historian perspective is not load-bearing.

7. **All inputs were provided.** Traceability checks could be run against the parent DD; ADR-021 resolved; `derived_from` resolved.

8. **Recommended next action points forward, not back.** APPROVED → "proceed to test-code derivation". The verdict is read by orchestration that needs to know what to do next.
