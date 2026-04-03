# Test Derivation Strategies

Four strategies for deriving test cases from a detailed design. Use all four.

---

## Strategy 1: Requirement-Based Testing

**Source:** The `behavior` section.

One test per behavior rule or condition. For compound conditions (A AND B), test each condition's
influence independently. For sequential steps, test the happy path and each step's `on_failure`.

---

## Strategy 2: Equivalence Class Partitioning

**Source:** The `interfaces.inputs` and `interfaces.outputs` sections.

Divide each input into groups where the unit behaves identically. Test one representative per group.

| Input property | Classes to derive |
|---|---|
| `type: enum` with `values` | One class per enum value |
| `type: integer/float` with `constraints` | Valid range, below range, above range |
| `type: string` | Empty, typical, at max length (if constrained) |
| `type: boolean` | true, false |
| `type: list` | Empty, single element, multiple elements |
| `type: object` | Valid complete, missing optional fields, missing required fields |

Also derive invalid classes: null/missing, wrong type, out of range.

When a unit has multiple inputs, test each input's classes independently while holding others at
valid defaults. Only combine inputs the design says interact (behavior rules referencing multiple
inputs).

---

## Strategy 3: Boundary Value Analysis

**Source:** The `interfaces.inputs` constraints and `configuration` defaults.

For numeric inputs with constraint `>= min` and `<= max`:

| Test point | Value | Expected |
|---|---|---|
| Below minimum | min - 1 (or min - epsilon) | Error / rejection |
| At minimum | min | Valid behavior |
| At maximum | max | Valid behavior |
| Above maximum | max + 1 (or max + epsilon) | Error / rejection |

Apply the same to string lengths, collection sizes, and `configuration` entries.

---

## Strategy 4: Error Handling and Fault Injection

**Source:** The `error_handling` section, plus gaps not covered.

One test per explicit `error_handling` entry. Then probe for implicit failures:

| Fault category | What to test |
|---|---|
| Null inputs | Pass null/nil/None for each input |
| Resource exhaustion | Empty collections, zero-length strings, maximum-size inputs |
| Dependency failures | If the design implies external calls, what if they fail? |
| Concurrent access | If `constraints` mention thread-safety, test concurrent calls |
| State corruption | If `internal_state` exists, call methods in unexpected order |
