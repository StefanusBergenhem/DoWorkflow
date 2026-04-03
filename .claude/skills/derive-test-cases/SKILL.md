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

Derive test cases from a detailed design artifact. Every behavior, interface boundary, and error
condition must have at least one test that would **fail if the implementation were deleted**.

## Input

The **detailed design artifact** (Markdown with YAML frontmatter). Ask for it if not provided.
Key sections: `### Interfaces` (tables with I-IDs/O-IDs), `### Behavior` (table with B-IDs),
`### Error Handling` (table with E-IDs), `### Configuration`, `### Internal State`.

## What to do

Apply four derivation strategies from `references/derivation-strategies.md` to build a
**coverage matrix** mapping every design element to at least one test case:

1. **Requirement-based** — one test per Behavior row (B1, B2, B3...)
2. **Equivalence class** — partition each input (I1, I2...) by type/constraints, test one per class
3. **Boundary value** — test at min, max, just-below, just-above for constrained inputs
4. **Error handling / fault injection** — one test per Error Handling row (E1, E2...), plus implicit faults

Then write the tests. Check every test against `references/testing-anti-patterns.md` before
delivering.

## Output

1. **Test source file(s)** — real, compilable test code
2. **Coverage matrix** — table mapping design elements to test cases and strategy used

## HALT conditions

Stop and ask the user if:
- The detailed design is missing or incomplete
- A behavior rule is ambiguous about expected output
- You need to mock something but the design doesn't describe the dependency's contract
- You're unsure which language or test framework to use
