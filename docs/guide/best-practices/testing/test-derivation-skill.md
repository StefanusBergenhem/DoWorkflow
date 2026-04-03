---
name: derive-test-cases
description: >
  Derive comprehensive test cases from a detailed design artifact using V-model test derivation
  strategies: requirement-based testing, equivalence class partitioning, boundary value analysis,
  and fault injection. Use this skill when the user asks to write tests, derive test cases, create
  a test suite, or implement TDD red phase for a module that has a detailed design. Also use when
  the user says "derive tests", "write tests for this design", "what should I test", or mentions
  test derivation, test coverage planning, or wants to go from design to tests.
user-invocable: true
---

# Derive Test Cases

You are a test engineer deriving test cases from a detailed design artifact. Your job is to ensure
every behavior, interface boundary, and error condition has at least one test that would **fail if
the implementation were deleted**. You produce real, compilable test code — not descriptions of
tests.

## When to use this skill

- A detailed design artifact (YAML, following the project's `detailed-design.schema.yaml`) exists
  for the unit under test
- The user wants to create tests *before* implementation (TDD red phase)
- The user wants to verify test completeness against a design

## Inputs

You need one thing: the **detailed design artifact** for the unit. Ask for it if not provided.
The artifact follows this structure (see `schemas/artifacts/detailed-design.schema.yaml` for full spec):

- `interfaces.inputs` / `interfaces.outputs` — the testable surface
- `behavior` — rules and conditions that transform inputs to outputs
- `error_handling` — explicit error conditions and responses
- `internal_state` — state retained between calls (if stateful)
- `constraints` — non-functional requirements (thread-safety, timing, etc.)
- `configuration` — parameters set at init time that affect behavior

## Process

### Step 1: Analyze the design

Read the detailed design artifact completely. Before writing any test, build a mental model:

- What are the inputs and their types/constraints?
- What are the outputs and how do they vary?
- How many distinct behavior rules exist?
- What error conditions are explicitly handled?
- Is the unit stateful or stateless?
- Are there configuration parameters that change behavior?

### Step 2: Build a coverage matrix

Create a mapping from design elements to test cases. Every row in this matrix must have at least
one test. Use these four derivation strategies — read `references/derivation-strategies.md` for
detailed guidance on each:

**Strategy 1 — Requirement-based (from `behavior` section)**
One test per behavior rule or condition. If a rule has multiple conditions, test each independently.

**Strategy 2 — Equivalence class / interface testing (from `interfaces`)**
For each input, identify equivalence classes:
- Valid classes (partitioned by type constraints, enum values, ranges)
- Invalid classes (null, wrong type, out of range)
- Pick one representative value per class

**Strategy 3 — Boundary value analysis (from `interfaces` constraints)**
For each numeric input with constraints, test:
- Minimum valid value
- Maximum valid value
- Just below minimum (invalid)
- Just above maximum (invalid)
- Zero (if applicable)

**Strategy 4 — Error handling / fault injection (from `error_handling`)**
One test per `error_handling` entry. Additionally, consider:
- What happens with null/missing inputs not mentioned in error_handling?
- What happens if a dependency fails (if the design implies external dependencies)?
- What happens at resource limits (empty collections, maximum sizes)?

### Step 3: Write the tests

For each entry in the coverage matrix, write a test. Follow these rules:

**Test structure — Arrange / Act / Assert**
Every test has three clear sections:
1. **Arrange** — set up the inputs, state, and dependencies
2. **Act** — call the unit under test exactly once
3. **Assert** — verify the output matches the expected result from the design

**Test naming**
Name each test to describe the scenario, not the method:
- Good: `test_rejects_negative_fuel_rate_with_error`
- Good: `test_startup_mode_limits_rate_to_minimum`
- Bad: `test_calculate` or `test_error` or `test1`

**Language choice**
- Use the same language and test framework as the project
- If unclear, ask the user which language and framework to use
- Keep tests idiomatic for the language

**One logical concept per test**
Multiple assert statements are fine if they verify one scenario. But don't test startup behavior
and shutdown behavior in the same test.

**Read the anti-patterns reference**
Before finalizing, check your tests against `references/testing-anti-patterns.md`. This is
important — the most common failure mode is writing tests that *look* comprehensive but don't
actually verify anything meaningful.

### Step 4: Verify coverage completeness

After writing all tests, do a final check:

1. **Every behavior rule** in the design's `behavior` section — is it tested?
2. **Every error condition** in `error_handling` — is it tested?
3. **Every input** — is it covered by at least equivalence class + boundary tests?
4. **Stateful transitions** — if `internal_state` exists, are state changes tested?
5. **Configuration variants** — if `configuration` exists, are different configs tested?

Present the coverage matrix to the user as a comment block or table showing:
```
| Design Element              | Test Case(s)                        | Strategy    |
|-----------------------------|-------------------------------------|-------------|
| behavior[0]: startup limit  | test_startup_mode_limits_rate_to_min| req-based   |
| error: negative rate        | test_rejects_negative_fuel_rate     | error       |
| input: rate boundary 0-100  | test_rate_at_zero, test_rate_at_max | boundary    |
```

### Step 5: Handoff checklist

Before presenting the tests, verify:

- [ ] Every test would fail if the implementation were deleted
- [ ] No test duplicates implementation logic to compute expected values
- [ ] No test uses "assert does not throw" as its only assertion
- [ ] Every mock has a documented reason (comment explaining what it replaces and why)
- [ ] Test names describe scenarios, not method names
- [ ] Coverage matrix accounts for all design elements
- [ ] Tests are compilable / syntactically valid for the target language

If any check fails, fix it before presenting. If you cannot fix it (e.g., the design is ambiguous
about expected behavior), **HALT and ask the user** rather than guessing.

## Output

Deliver two things:

1. **Test source file(s)** — real, compilable test code
2. **Coverage matrix** — table mapping design elements to test cases (as shown in Step 4)

## HALT conditions

Stop and ask the user if:
- The detailed design is missing or incomplete (no `behavior` section, no `interfaces`)
- A behavior rule is ambiguous about the expected output
- You need to mock something but the design doesn't describe the dependency's contract
- The design implies integration with external systems not described in the artifact
- You're unsure which language or test framework to use
