# Oracle checks

The oracle is what the case asserts. Refusal C lives here: weak assertions are non-negotiable hard-rejects. This file also covers unbounded-negative and tautological-form detection.

## anti-pattern.weak-assertions / check.oracle.weak-assertion (HARD ★ refusal C)

**Check that** every case `expected` is a specific value or a bounded invariant.

**Reject when** `expected` reads as a qualitative non-bounded phrase. Tells:
- `"verifies behaviour"`
- `"works correctly"`
- `"does not throw"` as the sole assertion
- `"non-null"` alone
- `"instance of X"` alone
- single-line qualitative phrase rather than enumerated value or bounded predicate

**Approve when** `expected` names a specific value (e.g. `expires_at: 2026-04-30T12:00:00Z`), an enumerated outcome (e.g. typed error `TokenInvalid` with stated state guarantee), or a bounded invariant (e.g. `result.length == input.length AND multiset(result) == multiset(input)`).

**Evidence pattern:** name the case id; quote the offending `expected` line verbatim; cite refusal C.

**recommended_action:** *"Replace the qualitative phrase with a specific value or bounded invariant. Refusal C — `expected` carries the oracle. See the matched author skill's case-quality reference."*

## check.oracle.unbounded-negative (soft)

**Check that** every negative `expected` (a property the function must not exhibit) is bounded by a concrete domain or invariant.

**Reject when** `expected` contains `never X` / `always Y` / `for all inputs Z` without a bounded domain — the assertion is not testable in finite time.

**Approve when** the negative is paired with a bounded domain (e.g. *"raises `TokenInvalid` for every input where `len < 8`"*), OR is rephrased as a positive property over a finite enumeration.

**Evidence pattern:** quote the unbounded-negative phrase; name the absent domain.

**recommended_action:** *"Bound the negative with a finite domain or rephrase as a positive property. The matched author skill's case-quality reference covers oracle bounding."*

## check.oracle.tautological-form (soft)

**Check that** the oracle does not recompute the expected value with the same logic the implementation would use.

**Reject when** `expected` reads as a recomputation (e.g. *"expected = sum(inputs)"* on a sum function; *"expected: a + b"* on a function that returns `a + b`). The oracle and the implementation cannot share the same algorithm — the test would always pass.

**Approve when** the oracle is computed independently (lookup table; reference implementation in a different shape; closed-form alternative; round-trip property; algebraic invariant).

**Evidence pattern:** name the case id; quote the recomputation; describe the shared logic.

**recommended_action:** *"Replace the recomputation with an independent oracle — table lookup, round-trip property, algebraic invariant, or reference value. Tautological tests pass for the wrong reasons."*

## Cross-link

`anti-patterns-catalog.md` (5 weak-assertions ★, 10 unbounded-negative-tests, 2 tautological-tests) · `case-quality-checks.md` · `quality-bar-gate.md` (Oracle card)
