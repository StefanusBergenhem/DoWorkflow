# Test Derivation Strategies

This reference details four strategies for deriving test cases from a detailed design artifact.
Each strategy targets a different dimension of coverage. A well-tested unit uses all four.

---

## Strategy 1: Requirement-Based Testing

**Source:** The `behavior` section of the detailed design.

Each behavior entry describes a rule, condition, or processing step. Every one of these is a
testable claim about what the unit does.

### How to derive tests

1. Read each behavior entry
2. Identify the **trigger** (condition or step) and the **expected outcome**
3. Write one test per entry that:
   - Sets up the trigger condition
   - Calls the unit
   - Asserts the expected outcome

### Handling compound conditions

If a behavior entry has multiple conditions (e.g., "when mode is startup AND rate exceeds limit"),
derive separate tests:
- Condition A true, Condition B true (both met — the rule fires)
- Condition A true, Condition B false (rule should NOT fire)
- Condition A false, Condition B true (rule should NOT fire)

This is decision table testing — don't test all permutations blindly, but cover every condition's
influence on the outcome.

### Handling sequential steps

If the behavior describes a sequence of steps, test:
- The full happy path (all steps succeed)
- Each step's failure (what happens if step N fails — check `on_failure` if present)

---

## Strategy 2: Equivalence Class Partitioning

**Source:** The `interfaces.inputs` and `interfaces.outputs` sections.

Equivalence class partitioning divides the input domain into groups where the unit should behave
identically for all values within a group. You test one representative from each group instead of
exhaustively testing all values.

### How to identify classes

For each input, look at:

| Input property | Classes to derive |
|---|---|
| `type: enum` with `values` | One class per enum value |
| `type: integer/float` with `constraints` | Valid range, below range, above range |
| `type: string` | Empty, typical, at max length (if constrained) |
| `type: boolean` | true, false |
| `type: list` | Empty, single element, multiple elements, at max (if constrained) |
| `type: object` | Valid complete, missing optional fields, missing required fields |

### Invalid equivalence classes

For each input, also derive invalid classes:
- `null` or missing (if the design doesn't mark it optional)
- Wrong type (string where integer expected)
- Out of range (below min, above max)
- Empty when non-empty required

These feed into error handling tests (Strategy 4) but are derived here.

### Combining inputs

When a unit has multiple inputs, don't test all combinations (combinatorial explosion). Instead:
- Test each input's classes independently while holding others at valid defaults
- Add combination tests only for inputs the design says interact (look for behavior rules that
  reference multiple inputs)

---

## Strategy 3: Boundary Value Analysis

**Source:** The `interfaces.inputs` constraints and `configuration` defaults.

Boundary value analysis targets the edges of valid ranges, where off-by-one errors and incorrect
comparisons hide.

### The boundary value set

For a numeric input with constraint `>= min` and `<= max`:

| Test point | Value | Expected |
|---|---|---|
| Below minimum | min - 1 (or min - epsilon for floats) | Error / rejection |
| At minimum | min | Valid behavior |
| Nominal | (min + max) / 2 | Valid behavior |
| At maximum | max | Valid behavior |
| Above maximum | max + 1 (or max + epsilon) | Error / rejection |

For floats, also consider:
- Negative zero (`-0.0`)
- Very small positive value (near epsilon)
- `NaN` and `Infinity` (if the language supports them and the design doesn't exclude them)

### String and collection boundaries

| Input type | Boundary tests |
|---|---|
| String with length constraint | Empty, length 1, at max length, max + 1 |
| List with min/max items | Empty, min items, max items, max + 1 |
| Map/object | Empty, one entry, at capacity |

### Configuration boundaries

If the design has `configuration` entries with constraints or defaults, apply boundary testing
to those as well. Configuration values set at init time can mask bugs that only appear at
specific settings.

---

## Strategy 4: Error Handling and Fault Injection

**Source:** The `error_handling` section, plus gaps not covered by explicit error handling.

### Explicit error handling

Each entry in `error_handling` has a `condition` and `behavior`. For each:
1. Set up the error condition
2. Call the unit
3. Assert the specified behavior (return error code, throw specific exception, return default, etc.)

### Implicit error handling (fault injection)

The design may not cover every failure mode. Probe for these:

| Fault category | What to test |
|---|---|
| Null inputs | Pass null/nil/None for each input — does it fail gracefully? |
| Type violations | Pass wrong types if the language allows (dynamic languages) |
| Resource exhaustion | Empty collections, zero-length strings, maximum-size inputs |
| Dependency failures | If the design implies external calls, what if they fail/timeout? |
| Concurrent access | If `constraints` mention thread-safety, test concurrent calls |
| State corruption | If `internal_state` exists, call methods in unexpected order |

### When to mock

Mock only when:
- The dependency is external to the unit (network, file system, database)
- The design explicitly describes the dependency interface
- You can state in one sentence what the mock replaces and why

Never mock the unit under test itself. Never mock to make a test pass — mock to isolate the unit
from things outside its control.

Document every mock with a comment:
```
// Mock: replaces SensorInterface because this unit test should not
// depend on actual hardware. Contract: returns SensorReading per
// the sensor-interface architecture component spec.
```
