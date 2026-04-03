# Testing Anti-Patterns

These are patterns that make tests look comprehensive while actually verifying nothing meaningful.
Check every test you write against this list before delivering.

---

## 1. The "Doesn't Throw" Test

**Anti-pattern:** The only assertion is that calling the function doesn't raise an exception.

```
// BAD
test("processes input") {
    unit.process(validInput)  // no assertion — test passes if no exception
}
```

**Why it's wrong:** This test passes for *any* implementation, including one that does nothing.
It would still pass if you deleted the implementation body.

**Fix:** Assert the actual output or side effect:
```
// GOOD
test("processes input and returns transformed result") {
    result = unit.process(validInput)
    assert(result.value == expectedValue)
}
```

---

## 2. The Mirror Test

**Anti-pattern:** The test recomputes the expected value using the same logic as the implementation.

```
// BAD
test("calculates rate") {
    expected = input.volume / input.time * CORRECTION_FACTOR  // duplicating impl logic
    assert(unit.calculateRate(input) == expected)
}
```

**Why it's wrong:** If the implementation has a bug in the formula, the test has the same bug.
They'll agree on the wrong answer.

**Fix:** Use a hardcoded expected value derived from the design specification or manual calculation:
```
// GOOD — expected value comes from the design document, not from reimplementing the formula
test("calculates rate for nominal input") {
    input = { volume: 100.0, time: 10.0 }
    assert(unit.calculateRate(input) == 7.5)  // 100/10 * 0.75 per design spec
}
```

---

## 3. The Untargeted Mock

**Anti-pattern:** Mocking something without understanding what it does or why you're replacing it.

```
// BAD
sensorMock = mock(Sensor)
sensorMock.read.returns(42)  // why 42? what does Sensor.read actually return?
```

**Why it's wrong:** The mock might not behave like the real dependency. Your test passes against
a fiction. When the real dependency is connected, the code fails.

**Fix:** Every mock needs a one-line comment explaining what it replaces and what contract it
follows. If you can't write that comment, you don't understand the dependency well enough to mock it.

```
// GOOD
// Mock: replaces HardwareSensor which returns SensorReading(float celsius, int timestamp).
// Using fixed value 42.0C to test nominal temperature processing.
sensorMock = mock(Sensor)
sensorMock.read.returns(SensorReading(42.0, 1000))
```

---

## 4. The Tautology Test

**Anti-pattern:** Asserting something that is always true regardless of the implementation.

```
// BAD
test("returns a result") {
    result = unit.process(input)
    assert(result != null)  // any non-trivial implementation returns something
}
```

**Why it's wrong:** It doesn't verify correctness, only existence. The implementation could return
garbage and this test passes.

**Fix:** Assert specific properties of the result that come from the design specification.

---

## 5. The Happy-Path-Only Suite

**Anti-pattern:** All tests use valid, typical inputs. No error cases, no boundaries, no edge cases.

**Why it's wrong:** Most bugs live at boundaries and in error handling, not in the happy path.
A suite of only happy-path tests gives false confidence.

**Fix:** For every happy-path test, ask: "what's the nearest way this could go wrong?" Then test
that too. Use the boundary value analysis and error handling strategies.

---

## 6. The Giant Test

**Anti-pattern:** One test that sets up complex state, calls multiple methods, and makes many
unrelated assertions.

```
// BAD
test("full workflow") {
    unit.init(config)
    unit.addItem(item1)
    unit.addItem(item2)
    result = unit.process()
    assert(result.count == 2)
    assert(result.total == 150.0)
    assert(result.status == "complete")
    unit.reset()
    assert(unit.isEmpty())
}
```

**Why it's wrong:** When this test fails, you don't know which behavior broke. It tests
initialization, adding, processing, status tracking, and reset all at once. A failure in `reset`
is masked if `process` fails first.

**Fix:** Split into focused tests, each verifying one behavior from the design:
- `test_process_returns_correct_count`
- `test_process_calculates_total`
- `test_reset_clears_state`

---

## 7. The Test That Tests the Framework

**Anti-pattern:** Testing that the language or framework works correctly, not that your code works.

```
// BAD
test("list can hold items") {
    list = new ArrayList()
    list.add("item")
    assert(list.size() == 1)  // you're testing Java's ArrayList, not your code
}
```

**Why it's wrong:** Framework code is already tested by the framework maintainers. Your tests
should verify *your* unit's behavior.

**Fix:** Only assert things that your unit's implementation determines. If removing your
implementation would still let the test pass (because it only exercises framework code), the test
is worthless.

---

## 8. The Assertion-Free Test

**Anti-pattern:** A test that calls the unit but has no assertions at all, relying on the test
framework's "no exception = pass" behavior.

This is a variant of anti-pattern #1 but sometimes harder to spot, especially in tests with
setup/teardown that look busy but never actually check anything.

**Fix:** Every test must have at least one assertion that checks output or state against an
expected value derived from the design.

---

## 9. The Structural-Only Assertion

**Anti-pattern:** When a function returns a composite type (object, union, variant, result wrapper),
the test only checks that the result exists or has the right type — not that the fields contain
correct values.

```
// BAD
test("parses valid message") {
    result = parser.parse(validBytes)
    assert(result != null)              // proves nothing about correctness
    assert(result.payload.isNotEmpty())  // any garbage payload passes
}
```

**Why it's wrong:** The test would pass if the parser returned a Success with completely wrong
field values. A corrupted sequence number, a mangled payload, a wrong message type — all pass
this test. It is a tautology dressed up as a happy-path test.

**Fix:** Assert specific field values derived from the input:
```
// GOOD
test("parses valid message with correct fields") {
    result = parser.parse(validBytes)
    assert(result.messageType == 0x01)       // known from the input bytes
    assert(result.sequenceNumber == 42)      // known from the input bytes
    assert(result.payload["temperature"] == 23.5)  // decoded from payload bytes
}
```

This applies equally to error variants — don't just check `result.isError()`. Check the specific
error code and diagnostic fields (e.g., byte_offset) match what the design specifies for that
failure mode.

---

## Quick self-check

Before delivering your test suite, run through this checklist for each test:

1. **Delete test:** If I deleted the implementation, would this test fail? If no, the test is worthless.
2. **Wrong answer test:** If the implementation returned a wrong but structurally valid answer, would this test catch it? If no, the assertion is too weak.
3. **Mock audit:** For every mock — can I state in one sentence what it replaces and why? If no, I don't understand the dependency well enough.
4. **Name check:** Does the test name tell me what scenario it covers? If I read just the names, do I understand the coverage?
