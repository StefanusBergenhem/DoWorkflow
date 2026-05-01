# Example — APPROVED review of an ADR

The reviewer is given a clean ADR for the `app/jobs` scope (greenfield; capturing the choice of Postgres `SKIP LOCKED` over Redis / RabbitMQ / cloud-managed queue). Reviewer applied the architect and integrator perspectives. Anti-pattern sweep found nothing structural. Spec Ambiguity Test passed.

*Note: the document under review is a synthetic example constructed to demonstrate review craft on a clean ADR; it is not a round-trip review of any specific external author-side example. The matched author skill ships its own good example independently per the self-containment convention.*

## Verdict block

```yaml
review:
  document: /specs/adrs/adr-017-use-postgres-for-job-queue.md
  reviewer: vmodel-skill-review-adr
  date: 2026-05-01

  verdict: APPROVED

  inputs_provided:
    adr_document: true
    supersedes_target: n/a
    superseded_by_target: n/a
    citing_artifacts: partial

  perspectives_applied:
    - architect
    - integrator

  findings:
    - id: F-001
      section: FRONTMATTER
      check_failed: check.linkage.affected-scopes-omitted
      severity: info
      category: linkage-lineage
      evidence: |
        Front-matter sets `affected_scopes: [app/jobs, app/ops]` and the
        Decision text references the ops team's operational surface. The
        linkage is explicit; no defect. Included as a positive observation
        because the back-resolution flag would otherwise have fired with
        partial citing-artifact inputs.
      recommended_action: |
        No action — `affected_scopes` is set and matches the Decision text.

    - id: F-002
      section: GLOBAL
      check_failed: check.linkage.governing-adrs-back-resolution-flag
      severity: info
      category: linkage-lineage
      evidence: |
        The ADR's status is `accepted` and `citing_artifacts` was provided
        as `partial` — the parent Architecture for `app/jobs` was supplied
        and references this ADR via `governing_adrs:`. The flag does not
        fire. Surfaced as info to confirm the back-resolution check ran.
      recommended_action: |
        No action — propagation is wired through the parent Architecture.

  summary: |
    Verdict: APPROVED. The ADR-017 document passes every applicable Quality
    Bar item and the Spec Ambiguity Test. Three real alternatives (Redis
    with Lua, RabbitMQ, cloud-managed SQS/Pub-Sub) each carry a concrete
    context-specific rejection reason; the Rationale cites two named
    drivers (operational familiarity, ACID enqueue) by name; Consequences
    list both signs concretely (positives include single-source durability;
    negatives include ~2k jobs/sec ceiling with revisit threshold); the
    Reversibility prompt is answered "yes" with a rollback path
    (queue-client adapter swap + dual-run window) and a ~1 engineer-week
    cost estimate. The Propagation block lists the new requirement
    REQ-app-jobs-018 at this scope plus a `governing_adrs` link from the
    child worker DD. Two info-level observations noted; no defects.

  recommended_next_action: |
    Proceed to Architecture / DD authoring against this ADR. Implementation
    can begin in parallel using `develop-code` against the child worker DD
    with the ADR's `SKIP LOCKED` constraint and the new requirement
    REQ-app-jobs-018 as the test contract.

  counts:
    hard_reject: 0
    soft_reject: 0
    info: 2
    design_issue: 0
```

## What this example demonstrates

1. **APPROVED is not silent.** Two info-level observations are surfaced. They do not affect the verdict but document the back-resolution check's outcome (positive — the citing Architecture resolved).

2. **Findings cite specific sections.** F-001 ties to FRONTMATTER (`affected_scopes`); F-002 ties to GLOBAL (back-resolution flag is a document-wide check).

3. **Recommended actions are generic and point to the rule.** Both info findings note "no action" because they are positive observations. When negative, the recommended action would point to the rule, not write the replacement text.

4. **The summary cites the dominant signal.** It names every load-bearing piece (three alternatives; named drivers cited by name; both-sign Consequences with revisit threshold; Reversibility answered with rollback path and cost estimate; Propagation block wired) and confirms what the Spec Ambiguity Test passed against.

5. **Counts are explicit.** Zero soft_reject, zero hard_reject; only info findings — the verdict is conservative and well-grounded.

6. **Two perspectives applied — architect and integrator.** Greenfield (no `recovery_status:` declared) → historian perspective is not load-bearing.

7. **Inputs were provided in expected shape.** No supersession lineage to resolve (n/a both ways); citing artifacts available as `partial` — sufficient to discharge the back-resolution flag.

8. **Recommended next action points forward, not back.** APPROVED → "proceed to Architecture / DD authoring". The verdict is read by orchestration that needs to know what to do next.
