# Case quality

The bar a single case must clear. Five disciplines: F.I.R.S.T., AAA, oracle specificity, determinism, per-case independence.

## Table of contents

- [F.I.R.S.T. — case-level properties](#first--case-level-properties)
- [AAA — one Act per case](#aaa--one-act-per-case)
- [Oracle specificity (the C refusal)](#oracle-specificity-the-c-refusal)
- [Determinism — clocks, randomness, ordering](#determinism--clocks-randomness-ordering)
- [Per-case independence](#per-case-independence)

## F.I.R.S.T. — case-level properties

| Letter | Property | TestSpec evidence |
|---|---|---|
| **F**ast | Case runs in milliseconds at the layer | `preconditions:` does not name long-running setup that is not load-bearing |
| **I**ndependent | Case does not depend on another case's residue | No "after TC-001" / "with TC-001's database state" in `preconditions:` |
| **R**epeatable | Case produces the same outcome every run | Clocks named (frozen / fixed); random seeds named; ordering of inputs explicit |
| **S**elf-validating | Pass/fail decision is in `expected:` (no human inspection) | `expected:` is a specific value or bounded predicate, not "verify by inspection" |
| **T**imely | Case authored from the spec, not after the test code | Documented derivation seed (`verifies:` on the case + a strategy `type:`) |

When a case violates **R** because of an unnamed clock or random source → finding `check.case-quality.firs-violation`. Fix: name the clock fixture (`Clock: fixed at 2026-01-01T00:00:00Z`) and the random seed (`Random seed: 42`) in `preconditions:`.

## AAA — one Act per case

Arrange / Act / Assert. The Act is the single call or single event that the case is verifying. When `steps:` enumerates two or more material acts → split the case.

| Tell | Fix |
|---|---|
| `steps:` lists two `<call into the public interface>` lines | Split into two cases — one per Act |
| Branch case fires three sequential events without a precondition driving the second | The second event is a hidden Arrange; surface it explicitly or split |
| Root-journey case has 12 numbered steps where 8 are Arrange | Move Arrange into `preconditions:`; leave Act as the journey's load-bearing event |

## Oracle specificity (the C refusal)

`expected:` must be a specific value, an enumerated set, or a bounded predicate. The phrases below are refusal C — author does not emit, review hard-rejects:

| Refused phrase | What replaces it |
|---|---|
| `"verifies behaviour"` | The exact value or predicate the behaviour produces |
| `"works correctly"` | The state predicate that "correctly" means |
| `"does not throw"` (alone) | The return value or post-state when no throw — pair with the value to make it self-validating |
| `"non-null"` (alone) | The constraints on the non-null value (type + invariants + domain) |
| `"instance of X"` (alone) | The properties of the instance that make it correct |

Bounded-predicate examples that do clear the bar: `<= 50ms`, `between 0 and 100`, `subset of {A, B, C}`, `is a permutation of input AND non-descending`, `contains exactly one element matching <regex>`.

When a case's `expected:` is a property over an input space → state the universally-quantified predicate explicitly (`for any cart, sum(line.line_total) == subtotal`). Property cases without a predicate are decoration.

## Determinism — clocks, randomness, ordering

When the spec depends on the current time → name the clock fixture in `preconditions:` (`Clock: fixed at 2026-01-01T00:00:00Z` or `Clock: advanceable, controlled by test`). Never let the test pull from the system clock.

When the spec depends on randomness → name the seed (`Random seed: 42`). When randomness is for property-based testing → name the generator and sample count.

When the spec depends on ordering of inputs (e.g., a stream of events) → name the order explicitly. Set / unordered-collection inputs are flagged when ordering is load-bearing.

## Per-case independence

Each case is a standalone story:
- `preconditions:` enumerate every fixture / double / seed / state the case requires.
- The case does not assume residue from a previous case.
- The case does not pollute global state for the next case (`expected:` covers post-state cleanup if applicable, or the fixture handles teardown).

Shared mutable test fixtures across cases are flagged. Sharing across cases produces order-dependent suites, which violate **R** and **I**.

## Cross-link

`derivation-strategies.md` (the `type:` enum that drives derivation) · `verifies-traceability.md` (the per-case `verifies:` rule) · `test-double-discipline.md` (named-doubles in `preconditions:`) · `anti-patterns.md` (weak-assertions, flaky-tests, mystery-guest)
