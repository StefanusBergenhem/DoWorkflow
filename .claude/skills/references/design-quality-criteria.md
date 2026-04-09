# Design Quality Criteria

Quality checks for detailed design documents. A design is not a code description — it must
contain information that the code cannot provide.

---

## Two Rules That Both Apply

A detailed design must satisfy two rules simultaneously. They appear contradictory but
operate at different abstraction levels.

**Rule 1 — Don't duplicate the code.** The design must not paraphrase implementation logic.
If a skilled developer could derive a fact by reading the code alone, that fact does not
belong in the design.

**Rule 2 — Be specific enough to implement and test.** A junior developer reading the
design must be able to produce a working implementation. A test engineer reading the design
must be able to derive tests *without seeing the code*.

These coexist because "specific" and "duplicating code" are different things.

### What the code shows (do NOT duplicate)

- Variable names and types
- Loop constructs and language-specific syntax
- Step-by-step implementation logic
- Data structure choice, *unless constrained by something external*
- Line-by-line control flow

### What the design shows (BE specific about)

- **Contract:** input ranges, valid state, output guarantees, error semantics
- **Behavior rules:** what property must hold for which inputs
- **Algorithm constraints:** "O(n log n) worst case because of timing requirement X" — not "use mergesort"
- **Rationale:** why these constraints exist
- **Edge cases:** what happens at boundaries
- **What the module hides** and why that boundary was drawn

### The test of specificity

A correctly written design has all three properties simultaneously:

1. Two different developers reading it would produce different code (different names,
   different loop styles, maybe different data structures within the constraints)
2. Both implementations would pass the same test suite
3. A test engineer could write that test suite without ever seeing the code

If (3) fails, the design is too vague. If (1) fails, the design is paraphrasing code.

### Mental model

Think of the design as defining a **box** in implementation space. The walls are the
contract — what is required and what is forbidden. Inside the box are many valid
implementations. Outside the box are bugs.

- **Too vague:** no walls — any implementation passes, including wrong ones
- **Code paraphrase:** box collapsed to a single point — only one implementation fits,
  and the design is just code in another language
- **Right level:** walls placed where correctness, performance, or safety demand them;
  the interior is left open

### Worked example

**Too vague:**
> "The function sorts the input list."

A developer doesn't know: ascending or descending, stable or not, what about nulls,
what's the performance budget. A test engineer cannot derive meaningful tests.

**Code paraphrase:**
> "Declare a result list, iterate from 0 to length-1, compare elements using natural
> ordering, swap when out of order, return the result."

Tells the developer *which* implementation to write — only one. Tells the test engineer
nothing the code wouldn't already show.

**Right level:**
> "Returns a list containing exactly the elements of the input, in non-descending order
> according to the natural ordering of the element type. Equal elements preserve their
> relative order from the input (stability is required because downstream processing
> depends on insertion order as a tiebreaker). Performance: O(n log n) worst case is
> required for compliance with SYS-TIMING-003. Null inputs are rejected with
> IllegalArgumentException."

A developer can implement this. Two developers might produce mergesort vs Timsort with
different variable names — both pass the same tests. A test engineer can derive boundary,
stability, performance, and null-rejection tests without seeing any code.

---

## Interface Completeness

Every public interface must specify all seven contract elements. Omitting any is a defect
unless explicitly marked "not applicable" with justification.

1. **Signature** — name, parameter types, return type
2. **Preconditions** — value ranges, state conditions, relationship constraints, units
3. **Postconditions** — return value range, state changes, idempotency, old-value relationships
4. **Invariants** — properties that hold across all public calls
5. **Error semantics** — what happens on invalid input: exception type, error code, or undefined?
6. **Nullability** — explicit for every parameter and return type
7. **Thread-safety guarantee** — immutable, thread-safe, conditionally thread-safe, or not thread-safe

A signature-only interface ("calculates the discount for an order") is not a design. It is a
placeholder that prevents test derivation.

---

## Behavioral Specification

Specify *what* the unit does, not *how* the code works. Two failure modes:

- **Too abstract:** "process the data" — untestable
- **Too concrete:** pseudocode mirroring the implementation — circular, useless for independent testing

Choose the right specification form:

| Behavior Type | Use |
|---|---|
| Pure function / math transformation | Pre/postcondition with property constraints |
| Rule-based logic (many conditions → action) | Decision table |
| Mode-dependent / event-driven | State machine with transition table |
| Protocol / ordered sequence | Scenario-based description |
| Complex boolean guard logic | Truth table |

For state machines: include state inventory with invariants, transition table (source, event,
guard, action, target), initial/terminal states, and undefined event handling policy.

---

## Design Rationale

Rationale is what separates a design document from a code description. Without it, maintainers
encounter decisions that appear arbitrary and either reverse them (breaking hidden constraints)
or preserve them unnecessarily.

**Every non-obvious design decision must document:**
- What was decided
- What alternatives were considered and rejected
- What constraints limited the solution space
- What consequences follow (what becomes easier, what becomes harder)

**Constraint categories** (document which apply):
- **External** — standards mandates, platform limitations, certification requirements
- **Architectural** — decisions from a higher layer
- **Resource** — timing budgets, memory limits, stack depth
- **Temporal** — correct now but may be lifted later (mark with expected expiry)

If the next engineer cannot distinguish "this design is bad and should be changed" from
"this design is forced by an external constraint and cannot be changed," the rationale
is missing.

---

## Error Handling Design

Error handling is a design decision, not an implementation detail. The design must answer
six questions for each unit:

1. What errors can this unit encounter or produce?
2. How is each error detected?
3. How does each error propagate (or get contained)?
4. What is the recovery strategy for each error class?
5. What state is the unit left in after an error?
6. What does the caller receive (exception, error code, degraded result)?

Use an error handling matrix for clarity:

| Error | Detection | Containment | Recovery | Caller Receives |
|---|---|---|---|---|
| Invalid input range | Precondition check | Reject at boundary | None (caller's bug) | IllegalArgumentException |
| Dependency timeout | Timeout config | Circuit breaker | Retry 2x, then fallback | Stale data + staleness flag |

**Anti-patterns:**
- No error strategy (each implementer handles differently) — inconsistent, untestable
- Missing error specification in interfaces — callers must guess
- Exception tunneling — low-level exceptions leak through layers

---

## Dynamic Behavior (when applicable)

Only specify when the unit has: multiple states, history-dependent behavior, concurrency,
or timing constraints.

**Thread safety** — document for every shared mutable field: which lock guards it, what
happens-before relationship is established. An undocumented shared field is a design defect.

**Timing constraints** must specify: constraint type (deadline, period, response time),
numeric bound with unit, conditions (worst-case, nominal), source requirement, and
verification method.

---

## Layering: How Much Design Is Enough

Follow the schema's additive Layer model. Higher layers build on lower — never skip a layer.

- **Layer 1 — Component Design** (always present): Purpose, external interfaces, unit inventory,
  shared patterns. The map of what exists and how it connects.
- **Layer 2 — Unit Interface Contracts** (per unit as needed): Per-unit interfaces, behavior
  rules, error handling. The level from which unit tests can be derived.
- **Layer 3 — Deep Design Evidence** (critical/complex units): Rationale, state machines,
  concurrency model, negative requirements.

Every unit appears in the Layer 1 inventory with its assigned layer. Prioritize units for
Layer 2/3 using: cyclomatic complexity > 10, high fan-in, high change frequency, above-average
defect density, safety-critical functionality.

---

## AI Self-Check

- [ ] Design contains information the code cannot provide — not a code paraphrase
- [ ] Every interface has all 7 contract elements (or explicit "N/A" with justification)
- [ ] Behavioral specification is testable — a test engineer can derive tests without seeing the code
- [ ] Rationale present for every non-obvious decision, with rejected alternatives
- [ ] Error handling specified per unit, not left to implementer
- [ ] No hallucinated constraints — every constraint traces to a requirement or documented source
- [ ] Layering decision is explicit and justified
