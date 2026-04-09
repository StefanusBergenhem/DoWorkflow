# Testing Anti-Patterns

Check every test against this list before delivering.

---

## 1. Assertion-Free / Assert-Doesn't-Throw

The only "assertion" is that calling the function doesn't raise an exception — or there is no
assertion at all. This test passes for *any* implementation, including an empty one.

**Fix:** Assert the actual output value against an expected value from the design.

---

## 2. The Mirror Test

The test recomputes the expected value using the same logic as the implementation. If the
implementation has a bug, the test has the same bug.

**Fix:** Use hardcoded expected values derived from the design spec or manual calculation.

---

## 3. Tautology / Structural-Only Assertion

Asserting something that is always true (e.g., `result != null`, `payload.isNotEmpty()`). The
implementation could return garbage and the test passes. This is especially common with
union/variant return types — checking `result.isError()` without verifying the error code and
diagnostic fields.

**Fix:** Assert specific field values derived from the design. For union types, verify the
variant-specific fields: error_code, byte_offset, message_type, decoded payload values.

---

## 4. The Untargeted Mock

Mocking something without understanding what it does. The mock might not behave like the real
dependency — your test passes against fiction.

**Fix:** Every mock needs a one-line comment: what it replaces, what contract it follows.
If you can't write that comment, don't mock it.

---

## 5. Eager Test (Giant Test)

One test that sets up complex state, calls multiple methods, makes many unrelated assertions.
When it fails, you don't know which behavior broke.

**Fix:** One logical concept per test. Multiple asserts are fine if they verify one scenario.

---

## 6. Testing the Framework

Testing that the language or framework works, not your code. If removing your implementation
would still let the test pass, the test is worthless.

---

## 7. Fragile Test

Test breaks when unrelated code changes. Four types of fragility:
- **Interface sensitivity:** breaks when method signatures change (use builders, not positional constructors)
- **Behavior sensitivity:** breaks when internal implementation changes but observable behavior does not (test behavior, not implementation; avoid strict mock verification of call sequences)
- **Data sensitivity:** breaks when shared test data changes (isolate test data per test)
- **Context sensitivity:** breaks in different environments (inject Clock, Locale; don't depend on timezone or machine state)

Fragile tests are the most damaging smell — developers stop trusting the suite, stop refactoring.

---

## 8. Obscure Test

Cannot see cause and effect between setup, action, and assertion. The reader must chase through
helper hierarchies or external files to understand one test.

**Fix:** Inline essential setup. Extract only irrelevant details to helpers.

---

## 9. Mystery Guest

Test depends on data or resources not visible in the test body (external files, database state,
environment variables).

**Fix:** Make all relevant data visible in the test. Use inline test data or Test Data Builders.

---

## 10. General Fixture

@BeforeEach / setUp creates far more state than any single test needs. Changes to the fixture
break unrelated tests. Hard to understand what each test actually depends on.

**Fix:** Move setup into individual tests or use Test Data Builders with sensible defaults.

---

## 11. Conditional Test Logic

if/else or loops in test code. The test itself might have bugs.

**Fix:** Tests should be straight-line code. Split into separate tests for each condition.

---

## 12. Erratic Test (Flaky)

Passes sometimes, fails sometimes, without code changes. Erodes trust in the entire suite.

**Fix:** Control non-determinism — inject Clock, seed Random, isolate shared state, avoid
real I/O in unit tests.

---

## 13. Slow Test

Test takes seconds instead of milliseconds. Developers run tests less often; bugs accumulate.

**Fix:** Replace I/O with test doubles. Move slow tests to an integration tier.

---

## Quick Self-Check

For each test:
1. **Delete test:** Would this fail if I deleted the implementation?
2. **Wrong answer test:** Would this catch a wrong but structurally valid answer?
3. **Mock audit:** Can I explain each mock in one sentence?
4. **Name check:** Does the test name tell me the scenario?
