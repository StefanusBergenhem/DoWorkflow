# Testing Anti-Patterns

Check every test against this list before delivering.

---

## 1. No Assertion / Assert-Doesn't-Throw

The only "assertion" is that calling the function doesn't raise an exception — or there is no
assertion at all. This test passes for *any* implementation, including an empty one.

**Fix:** Assert the actual output value against an expected value from the design.

---

## 2. The Mirror Test

The test recomputes the expected value using the same logic as the implementation. If the
implementation has a bug, the test has the same bug.

**Fix:** Use hardcoded expected values derived from the design spec or manual calculation.

---

## 3. The Untargeted Mock

Mocking something without understanding what it does. The mock might not behave like the real
dependency — your test passes against fiction.

**Fix:** Every mock needs a one-line comment: what it replaces, what contract it follows.
If you can't write that comment, don't mock it.

---

## 4. Tautology / Structural-Only Assertion

Asserting something that is always true (e.g., `result != null`, `payload.isNotEmpty()`). The
implementation could return garbage and the test passes. This is especially common with
union/variant return types — checking `result.isError()` without verifying the error code and
diagnostic fields.

**Fix:** Assert specific field values derived from the design. For union types, verify the
variant-specific fields: error_code, byte_offset, message_type, decoded payload values.

---

## 5. The Giant Test

One test that sets up complex state, calls multiple methods, makes many unrelated assertions.
When it fails, you don't know which behavior broke.

**Fix:** One logical concept per test. Multiple asserts are fine if they verify one scenario.

---

## 6. Testing the Framework

Testing that the language or framework works, not your code. If removing your implementation
would still let the test pass, the test is worthless.

---

## Quick self-check

For each test:
1. **Delete test:** Would this fail if I deleted the implementation?
2. **Wrong answer test:** Would this catch a wrong but structurally valid answer?
3. **Mock audit:** Can I explain each mock in one sentence?
4. **Name check:** Does the name tell me the scenario?
