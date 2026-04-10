---
name: vmodel-skill-review-code
description: >
  Review source code and unit tests together against a design document. Enforces code quality,
  test quality, and cross-checks between all three artifacts. Produces a structured verdict
  (APPROVED, REJECTED, or DESIGN_ISSUE). Use this skill when the user asks to review code,
  check code quality, audit tests, verify implementation against design, or do a code review.
  Also use when the user says "review this", "check this code", "does this match the design",
  "audit these tests", or mentions code review, quality check, or design compliance verification.
user-invocable: true
---

# Review Code

Review source code and unit tests against a design document. The reviewer's job is to find
defects — places where the code, tests, or their relationship to the design is wrong.

You are not the author. You are not here to improve style or suggest alternatives. You are
here to catch problems that the author can't easily see in their own work: logic that doesn't
match the design, tests that don't actually verify anything, code that has no corresponding
design element, and design behaviors that nothing tests.

## Input

Three artifacts, all required:

1. **Design document** — what should be implemented (any format)
2. **Source code** — what was implemented
3. **Test code** — what is verified

Read all three completely before starting. If the design document is not available or too
vague to determine intent, HALT and ask.

## Principles

These guide every review decision.

- **Review against the design, not your preferences.** The design is the specification. If the
  code does what the design says, it's correct — even if you'd have designed it differently.
- **Every finding must be concrete.** Cite the specific checklist item violated, the specific
  file and line, and what specifically is wrong. "Could be improved" is not a finding.
- **Don't rewrite — identify.** Your job is to find and describe problems, not to provide
  corrected code. State what's wrong and which checklist item it violates.
- **All findings block.** There is no "minor" category that can be deferred. In an agentic
  workflow, unaddressed findings accumulate into systemic quality erosion. Everything gets fixed.
- **DESIGN_ISSUE is not REJECTED.** If the code correctly implements a flawed design, that's
  not a code defect. Rejecting the developer for a design problem creates an unresolvable loop.
  Flag it as DESIGN_ISSUE so it escalates to the right level.

## Review Procedure

Three sequential passes. Complete each pass fully before starting the next. For each pass,
read the corresponding reference file and check every applicable item.

### Pass 1: Code Quality

Read `references/code-quality-checks.md` and check every item against the source code.

Focus areas (these are where defects hide most often):

- **Design compliance** — every public method/class traces to a design element. No gold plating
  (functionality the design didn't ask for). Algorithms match the design specification.
- **Complexity** — functions under 50 lines, cyclomatic complexity under 10, nesting under 4,
  3 or fewer parameters. These aren't style preferences — they're defect predictors.
- **Error handling** — no null returns, no empty catch blocks, fail-fast at boundaries, error
  messages include context (operation, input, expected state). This is where most production
  bugs hide, especially in AI-generated code.
- **SOLID** — single responsibility (describe without "and"), open/closed, Liskov substitution,
  interface segregation, dependency inversion.
- **Architecture boundaries** — domain code has zero infrastructure imports. Dependencies point
  inward. Pure logic separated from I/O.
- **Naming** — reveals intent, consistent vocabulary, domain terms used.
- **Dead code** — no unreachable paths, no unused functions, no commented-out code.

### Pass 2: Test Quality

Read `references/testing-anti-patterns.md` and `references/ai-testing-failures.md`. Check
every test against both lists.

Focus areas:

- **Anti-patterns #1-#3 are critical** — assertion-free tests, mirror tests, and tautological
  assertions are the most common and most damaging. A test that passes for any implementation
  is worse than no test (it creates false confidence).
- **Derivation completeness** — tests should cover the four strategies: requirement-based
  (one per behavior rule), equivalence class (one per partition), boundary value (at edges),
  and error/fault (one per error condition). Focus on observable behaviors and design-level
  interfaces. Internal implementation steps that are tested indirectly through the success
  path don't need independent tests — the question is whether a bug in that step would be
  caught, not whether it has its own test function.
- **Assertion specificity** — every test must assert a specific expected value derived from
  the design. `assertNotNull`, `assertTrue(x)`, or "does not throw" as a sole assertion is
  a finding.
- **Mock audit** — mocks should only appear at infrastructure boundaries (database, HTTP,
  filesystem, clock). Domain collaborators should use real implementations or simple fakes.
  Every mock needs a clear justification. Over-mocking (>2 mocked dependencies) is a finding.
- **Test smells** — fragile tests (break on unrelated changes), obscure tests (can't see
  cause and effect), eager tests (verify too many things), mystery guests (invisible data
  dependencies).

### Pass 3: Cross-Checks

Read `references/review-checklist-code.md` section 3. This is the reviewer's unique value —
things neither the code author nor the test author can easily see alone.

- **Coverage completeness** — every behavior rule in the design has at least one test.
- **No orphan code** — every public method/class traces to a design element.
- **No orphan tests** — every test traces to a design behavior (no tests for imaginary features).
- **No untested error paths** — every error condition in the design has a corresponding test.
- **Interface match** — code signatures match the design's interface specification (types,
  parameters, return types, nullability).
- **Boundary consistency** — boundary values in tests match the ranges specified in the design.
- **No design drift** — if the code deviates from the design, is it documented and justified,
  or is it a defect?

## Output

Use the exact template from `references/review-verdict-template.md`.

Three possible verdicts:

- **APPROVED** — all checks pass across all three passes. No findings.
- **REJECTED** — one or more findings. List every finding in the table with pass, check, and
  location. All findings must be addressed before re-review.
- **DESIGN_ISSUE** — the review reveals a problem that cannot be fixed without changing the
  design (wrong decomposition, missing interface, contradictory specification, impossible
  constraint). The code may be correct per the design — the design itself is the problem.
  This escalates to human rather than cycling back to the developer.

A review can be both REJECTED and have a DESIGN_ISSUE. In that case, use DESIGN_ISSUE as the
verdict (it's the higher-priority signal) and include both code findings and the design issue
detail in the output.

## Self-Check

Before delivering the verdict:

1. **Completeness:** Did I check every item in all three passes?
2. **Evidence:** Does every finding cite a specific checklist item and a specific location?
3. **No false positives:** Is every finding a real defect, not a style preference?
4. **Design vs code:** Did I correctly distinguish code defects (REJECTED) from design
   defects (DESIGN_ISSUE)?
5. **Cross-check coverage:** Did I verify that every design element has both code and tests?

## HALT Conditions

Stop and ask the user if:
- The design document is not available
- The design is too vague to determine what the code should do
- You cannot determine which code files implement the design (scope unclear)
- The code and design appear to be for completely different things
