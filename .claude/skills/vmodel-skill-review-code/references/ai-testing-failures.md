# AI Testing Failures

Self-check for AI-generated tests. These are failure modes that AI agents produce at
significantly higher rates than human developers. Check your output against this list.

---

## Why This Matters

AI-generated tests can achieve 100% line/branch coverage with only 4% mutation score — meaning
the tests execute every line but detect almost no faults. Coverage metrics alone are
near-worthless as a quality indicator for AI-generated tests.

---

## 1. Tautological Tests (Mirror Test)

Recomputing expected values using the same logic as the implementation. If the formula has a bug,
the test has the same bug. This is the most common AI testing failure.

**Why AI does this:** The model reads the implementation (or just wrote it) and unconsciously
mirrors the logic in the assertion.

**Fix:** Use hardcoded expected values derived from the design spec, not computed from the code.
See testing-anti-patterns.md #2 for details.

---

## 2. Assertion-Free Tests

Tests that only check "doesn't throw" or `assertNotNull`. Passes for any implementation.

**Why AI does this:** The model optimizes for "test that compiles and passes" rather than
"test that verifies behavior."

**Fix:** Every test must assert a specific output value. See testing-anti-patterns.md #1.

---

## 3. Happy-Path Bias

AI generates mostly positive test cases. Error paths, boundaries, and invalid inputs are
systematically underrepresented.

**Why AI does this:** Training data is dominated by happy-path examples. The model must be
explicitly prompted for negative cases.

**Fix:** Apply all four derivation strategies (see derivation-strategies.md). After writing
tests, count: how many test error paths vs happy paths? If the ratio is below 1:2, you're
missing cases.

---

## 4. Over-Mocking

Mocking everything, including things that should use real implementations. AI agents mock at
~95% rate vs ~91% for humans, with less diversity in test double strategies.

**Why AI does this:** Mocking eliminates setup complexity. The model takes the path of least
resistance to a compiling test.

**Fix:** Mock only at infrastructure boundaries (DB, HTTP, filesystem). Domain collaborators
should use real instances or simple fakes. If you're mocking more than 2 dependencies in one
test, the design may need revisiting.

---

## 5. Hallucinated Assertions

Assertions on properties that don't exist, or that don't mean what the model thinks they mean.
The test may compile (dynamic languages) but verify fiction.

**Why AI does this:** The model generates plausible-sounding field names from context rather
than checking the actual type definition.

**Fix:** Verify every asserted field/method exists in the actual type. In typed languages,
compilation catches this. In dynamic languages, run the test and check it fails for the right
reason before making it pass.

---

## 6. Copy-Paste Test Patterns

Many structurally identical tests with slight variations that don't add meaningful coverage.
10 tests that all exercise the same equivalence class add noise, not confidence.

**Why AI does this:** Generating variations is easy. Thinking about which variations matter
requires understanding the design's equivalence classes.

**Fix:** Each test should target a distinct equivalence class, boundary, or error condition.
If two tests would fail for the same bug, one of them is redundant.

---

## 7. Code-to-Test (instead of Spec-to-Test)

Generating tests by reading the implementation rather than the design specification. Tests
mirror the code, including its bugs. This is structurally at odds with the V-model principle
that tests verify the design, not the code.

**Why AI does this:** Every current AI testing tool defaults to code-to-test. It's the path
of least resistance when no specification is provided.

**Fix:** Always derive tests from the design document. If no design exists, that's a process
problem — don't paper over it with code-derived tests.
