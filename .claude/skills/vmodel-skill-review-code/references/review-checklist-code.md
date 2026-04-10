# Code and Test Review Checklist

Review checklist for combined code + unit test review. The reviewer's job is to verify
correctness against the design, not to second-guess style choices.

---

## Prerequisites

Before starting review, confirm you have:
- The detailed design document for the unit(s) under review
- The code under review
- The unit tests under review

---

## 1. Code Quality

Verify all items in `code-quality-checks.md` pass. Focus reviewer attention on items the
author is least likely to self-catch:

- [ ] Design compliance — every public method traces to a design element, no gold-plating
- [ ] Algorithm correctness — logic matches the design specification, not just "looks right"
- [ ] Error handling matches the design's error strategy (not ad-hoc)
- [ ] Architecture boundaries respected — no infrastructure imports in domain code

---

## 2. Test Quality

Verify tests against `testing-anti-patterns.md`. Focus on:

- [ ] No anti-patterns from the checklist (especially #1-#3: assertion-free, mirror, tautology)
- [ ] Tests derived from the design, not from reading the code (spec-to-test, not code-to-test)
- [ ] All four derivation strategies applied (requirement-based, equivalence, boundary, fault)
- [ ] Error paths tested, not just happy paths
- [ ] Mocks justified — only at infrastructure boundaries, each with a clear rationale

If AI-generated, additionally check against `ai-testing-failures.md`.

---

## 3. Cross-Checks (code ↔ tests ↔ design)

These are the checks that neither the code author nor the test author can easily do alone.
This is the reviewer's unique value.

- [ ] **Coverage completeness:** every design behavior rule has at least one test
- [ ] **No orphan code:** every public method/class traces to a design element
- [ ] **No orphan tests:** every test traces to a design behavior (no tests for imaginary features)
- [ ] **No untested error paths:** every error condition in the design has a corresponding test
- [ ] **Interface match:** code signatures match the design's interface specification (types, parameters, return types, nullability)
- [ ] **Boundary consistency:** boundary values in tests match the ranges specified in the design
- [ ] **No design drift:** if the code deviates from the design, is the deviation documented and justified, or is it a defect?

---

## Verdict

- **APPROVED** — all required checks pass
- **REJECTED** — specific findings listed with checklist item reference
