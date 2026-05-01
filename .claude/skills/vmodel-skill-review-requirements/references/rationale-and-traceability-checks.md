# Rationale and traceability — checks

**Contents**
- [Rationale checks](#rationale-checks) — present, fabrication tells, retrofit recovery_status, circularity, laundering
- [Derived-requirement checks](#derived-requirement-checks)
- [Traceability checks](#traceability-checks)
- [Recommended-action discipline reminder](#recommended-action-discipline-reminder)

---

Two related lifecycle checks. Rationale is the most-violated discipline in AI-authored requirements; this reference catalogs the tells, including the **hard-reject** trigger for retrofit `recovery_status` violations.

## Rationale checks

### Check 1 — Rationale present (or explicit unknown)

Every requirement must have a `rationale` field with non-trivial content, OR an explicit `pending` (greenfield) or `unknown` (retrofit) marker. Empty rationale and missing rationale are both findings.

- **check_failed**: `check.rationale.missing`
- **severity**: `hard_reject` ← one of the five hard-reject triggers
- **evidence shape**: cite the requirement ID; note that `rationale` is empty or absent and no `pending`/`unknown` marker is present
- **recommended_action**: *"Either capture the rationale (the why, options considered, criterion that drove the choice), or mark `pending` (greenfield) / `unknown` (retrofit) and queue a follow-up. Never leave silently empty."*

### Check 2 — Fabricated rationale tells

| Tell | Examples |
|---|---|
| Generic phrases without source | *"industry-standard"*, *"best practice"*, *"common approach"* |
| "Balances X with Y" without naming the trade-off | *"balances security with usability"* — could fit any value |
| "After consultation with X" without preserved record | *"chosen after consultation with the security team"* — when no record exists |
| Reasoning instead of recall | *"because CSPRNG is more secure"* (reasoning) vs *"because the 2024 entropy audit mandated CSPRNG"* (recall) |
| Defending a number with vague justification | *"30 minutes balances security and UX"* — could justify 10, 30, 60, 120 equally |

- **check_failed**: `anti-pattern.fabricated-rationale`
- **severity**: `hard_reject` ← one of the five hard-reject triggers
- **evidence shape**: quote the rationale; name the tell(s) present
- **recommended_action**: *"Replace with `rationale: pending` or `rationale: unknown` and queue a human follow-up. Never paper over absence of recall with reasoning."*

### Check 3 — Retrofit `recovery_status` legality on the rationale field

In retrofit mode the rationale field is human-only. Allowed values for `rationale_recovery_status`: `verified` or `unknown`. `reconstructed` is forbidden.

- **check_failed**: `check.rationale.recovery-status-reconstructed`
- **severity**: `hard_reject` ← one of the five hard-reject triggers
- **evidence shape**: cite the offending field
- **recommended_action**: *"Change `recovery_status` to `verified` (if a human can confirm) or `unknown` (if no recall available)."*

### Check 4 — Circular rationale (test-as-rationale inversion)

Tell: rationale that cites a test as the reason for the requirement. *"The system shall do X because that is what test T verifies."* The test is verifying the current code, not the original intent — rationale is circular.

- **check_failed**: `anti-pattern.test-as-requirement-inversion`
- **severity**: `soft_reject`
- **evidence shape**: quote the rationale; note the test cited
- **recommended_action**: *"Replace with the original decision's reasoning, or mark `unknown`. The test is verifying the requirement, not justifying it."*

### Check 5 — Laundering current state

Tell: rationale that defends the current design as if it were the only possible choice. *"Because the current implementation does X, X is correct."* No rejected alternative is ever named.

- **check_failed**: `anti-pattern.laundering-current-state`
- **severity**: `soft_reject`
- **evidence shape**: note that no rationale ever cites a rejected alternative; rationales ratify rather than reason
- **recommended_action**: *"Rewrite rationale to cite an actual decision: option chosen, alternatives considered, criterion that drove the choice."*

## Derived-requirement checks

### Check 6 — Derivation flagged

A derived requirement (one introduced by a decision at this scope, not by an upstream stakeholder need) must be flagged `derivation: derived`. Silent derivation drifts the document out of stakeholder alignment.

- **check_failed**: `check.rationale.derived-no-flag`
- **severity**: `soft_reject`
- **evidence shape**: cite the requirement; note that its `derived_from` cites only an ADR (not an upstream user need) but `derivation: derived` flag is missing
- **recommended_action**: *"Add `derivation: derived` to mark this as a requirement introduced at this scope, not by an upstream stakeholder need."*

### Check 7 — Derived requirement cites introducing decision

Every requirement flagged `derivation: derived` must cite, in `rationale` or `derived_from`, the specific decision (ADR, architectural choice, governing constraint) that introduced it.

- **check_failed**: `check.rationale.derived-no-decision-cited`
- **severity**: `soft_reject`
- **evidence shape**: cite the derived requirement; note no specific decision is named
- **recommended_action**: *"Cite the specific decision (ADR ID, governing constraint ID) that introduced this requirement."*

## Traceability checks

### Check 8 — `derived_from` non-empty

Every requirement has a non-empty `derived_from` field linking to upstream artifacts (parent requirement, product brief outcome, user story, inherited constraint, governing decision, or — in retrofit — observed evidence with file:line citation).

- **check_failed**: `check.traceability.derived-from-empty`
- **severity**: `soft_reject`
- **evidence shape**: cite the requirement
- **recommended_action**: *"Add at least one upstream link in `derived_from` — a parent requirement, product brief outcome, user story, inherited constraint, or governing decision."*

### Check 9 — `derived_from` cites concrete artifacts (not categories)

`derived_from` values must be artifact identifiers (REQ-IDs, ADR-IDs, IC-IDs, user-story IDs, observed-evidence references with file:line) — not abstract categories.

Tells:
- *"derived_from: [user-experience, security]"* — categories, not artifacts
- *"derived_from: [stakeholder needs]"* — vague
- *"derived_from: [common sense]"* — non-artifact

- **check_failed**: `check.traceability.derived-from-vague`
- **severity**: `soft_reject` (or `hard_reject` in retrofit mode where it indicates fabrication)
- **evidence shape**: quote the `derived_from` list
- **recommended_action**: *"Replace abstract categories with artifact identifiers. In retrofit mode, every link must be either a real upstream artifact or observed evidence (file:line, commit, log)."*

### Check 10 — Governing decisions referenced

Every governing ADR (or governing decision) listed in the document's frontmatter `governing_decisions` should be referenced by at least one requirement's `derived_from` or `rationale`. A governing decision listed but unused suggests either dead context or missed derivation.

- **check_failed**: `check.traceability.governing-decision-not-referenced`
- **severity**: `info` (when no requirement clearly *should* derive from it) or `soft_reject` (when at least one requirement evidently derives from it but doesn't cite it)
- **recommended_action**: *"Either remove the unused governing decision from frontmatter, or cite it in the `derived_from` of the requirement(s) it actually governs."*

### Check 11 — `derived_from` cites a non-existent upstream artifact (DESIGN_ISSUE)

When the optional upstream input is provided, cross-check every `derived_from` reference against the upstream input. Citations to artifacts that do not exist upstream are a DESIGN_ISSUE — the document's derivation is broken.

- **check_failed**: `design-issue.derived-from-cites-nonexistent-artifact`
- **severity**: `hard_reject` (DESIGN_ISSUE category)
- **evidence shape**: name the missing artifact ID; note that it appears in `derived_from` but not in the upstream input
- **recommended_action**: *"Either add the missing upstream artifact to the upstream specification, or correct the citation. Document cannot be APPROVED until upstream is resolved."*

## Recommended-action discipline reminder

For all checks above: `recommended_action` points to the kind of fix and the relevant author-side guide; it does not write the replacement text. The author skill rewrites.
