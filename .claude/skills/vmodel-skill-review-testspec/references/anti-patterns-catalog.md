# Anti-patterns catalog — sweep targets

Thirteen failure modes. Each has a tell, a `check_failed` identifier, a severity, and a generic `recommended_action`. Walk every case, every front-matter element, and every Gap-Report entry through this catalog. Every hit becomes a finding.

Items 1–11 are universal-TestSpec; items 12–13 are non-negotiable hard refusals (B and A). Hard-reject triggers marked ★. Item 5 is also a hard refusal (C).

## Table of contents

- [Derivation discipline (3)](#derivation-discipline-3) — code-to-test-derivation, tautological-tests, test-as-requirement-inversion
- [Coverage and oracle discipline (3)](#coverage-and-oracle-discipline-3) — happy-path-bias, **weak-assertions** ★, unbounded-negative-tests
- [Test-double discipline (1)](#test-double-discipline-1) — over-mocking
- [Fixtures and isolation (1)](#fixtures-and-isolation-1) — mystery-guest
- [Pyramid and metric discipline (3)](#pyramid-and-metric-discipline-3) — ice-cream-cone-coverage, coverage-as-quality-metric, flaky-tests
- [Hard refusals (2)](#hard-refusals-2) — **orphan-tests** ★, **fabricated-retrofit-intent** ★
- [Sweep order](#sweep-order)
- [Aggregation rule](#aggregation-rule)

## Derivation discipline (3)

### 1. Code-to-test derivation

- **Tell**: cases enumerated by reading the implementation rather than the parent spec; coverage tracks code branches, not spec rules
- **check_failed**: `anti-pattern.code-to-test-derivation`
- **severity**: `soft_reject`
- **recommended_action**: *"Re-derive cases from the parent spec (DD / Architecture / Requirements + Product Brief). Code-derived tests pass for the wrong reasons."*

### 2. Tautological tests

- **Tell**: oracle recomputes the expected value with the same logic the implementation uses (e.g. `expected: a + b` on a function returning `a + b`)
- **check_failed**: `anti-pattern.tautological-tests`
- **severity**: `soft_reject`
- **recommended_action**: *"Replace the recomputation with an independent oracle (lookup table, round-trip, algebraic invariant). See `oracle-checks.md`."*

### 3. Test-as-requirement inversion

- **Tell**: retrofit synthesises spec elements from existing tests; intent is inferred, not observed
- **check_failed**: `anti-pattern.test-as-requirement-inversion`
- **severity**: `soft_reject`
- **recommended_action**: *"Derive the spec independently of the existing tests; map tests against spec afterwards. See `retrofit-discipline-checks.md`."*

## Coverage and oracle discipline (3)

### 4. Happy-path bias

- **Tell**: error / happy ratio < 1:2 across cases; error-path matrix has many gaps
- **check_failed**: `anti-pattern.happy-path-bias`
- **severity**: `soft_reject`
- **recommended_action**: *"Derive error / fault-injection cases per parent-spec error matrix. The matched author skill's derivation-strategies reference covers error-path coverage."*

### 5. Weak assertions (HARD ★ refusal C)

- **Tell**: `expected` is a qualitative non-bounded phrase (`"verifies behaviour"`, `"works correctly"`, `"does not throw"`, `"non-null"`, `"instance of X"`)
- **check_failed**: `anti-pattern.weak-assertions`
- **severity**: `hard_reject` ★ (refusal C)
- **evidence pattern**: quote the offending `expected` line verbatim
- **recommended_action**: *"Replace with a specific value or bounded invariant. See `oracle-checks.md`."*

### 6. Unbounded negative tests

- **Tell**: `expected` contains `never X` / `always Y` / `for all inputs Z` without a bounded domain
- **check_failed**: `anti-pattern.unbounded-negative-tests`
- **severity**: `soft_reject`
- **recommended_action**: *"Bound the negative with a finite domain or rephrase as a positive property. See `oracle-checks.md`."*

## Test-double discipline (1)

### 7. Over-mocking

- **Tell**: leaf case has > 2 doubles; or branch case is mock-heavy (every collaborator mocked, no real cross-child interaction)
- **check_failed**: `anti-pattern.over-mocking`
- **severity**: `soft_reject`
- **recommended_action**: *"Reduce doubles or introduce real collaborators. See `test-double-discipline-checks.md`."*

## Fixtures and isolation (1)

### 8. Mystery guest

- **Tell**: case depends on fixtures, seeds, or environment that are not named in the case (hidden in shared setup, an external file, or a test-runner config)
- **check_failed**: `anti-pattern.mystery-guest`
- **severity**: `soft_reject`
- **recommended_action**: *"Name fixtures, seeds, and environment in `preconditions`. Hidden state breaks Repeatability and Independence."*

## Pyramid and metric discipline (3)

### 9. Ice-cream-cone coverage

- **Tell**: many e2e / system cases at root, few unit / leaf cases — pyramid is upside-down
- **check_failed**: `anti-pattern.ice-cream-cone-coverage`
- **severity**: `soft_reject`
- **recommended_action**: *"Push the pyramid back into shape — derive leaf cases from DD; reserve root cases for user-journeys; use branch for cross-child integration. See `per-layer-weight-checks.md`."*

### 10. Coverage as quality metric

- **Tell**: structural coverage threshold declared without a mutation bar; coverage is treated as the goal
- **check_failed**: `anti-pattern.coverage-as-quality-metric`
- **severity**: `soft_reject`
- **recommended_action**: *"Pair structural coverage with a mutation bar. See `coverage-mutation-bar-checks.md`."*

### 11. Flaky tests

- **Tell**: cases marked flaky / quarantined / pass-most-of-the-time without a determinism plan; preconditions name real clocks / random / network without seeds or stubs
- **check_failed**: `anti-pattern.flaky-tests`
- **severity**: `soft_reject`
- **recommended_action**: *"Restore determinism (freeze clock, seed random, stub network) or remove the case until the unit-under-test is testable. See `case-quality-checks.md`."*

## Hard refusals (2)

### 12. Orphan tests (HARD ★ refusal B)

- **Tell**: artifact-level `verifies: []`; case missing `verifies`; `verifies` resolves to nothing in the upstream spec; `verifies` references look like file paths rather than artifact IDs
- **check_failed**: `anti-pattern.orphan-tests`
- **severity**: `hard_reject` ★ (refusal B)
- **evidence pattern**: quote the empty / unresolvable `verifies`; cite refusal B
- **recommended_action**: *"Populate `verifies` with resolvable upstream IDs at artifact level and on every case. See `verifies-traceability-checks.md`."*

### 13. Fabricated retrofit intent (HARD ★ refusal A — retrofit only)

- **Tell**: retrofit case carries stated intent on `title` or `notes`; `recovery_status` is `verified` / absent on reconstructed `verifies`; gap report lists no `unknown` cases despite retrofit context
- **check_failed**: `anti-pattern.fabricated-retrofit-intent`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: applies only when `recovery_status:` declared
- **recommended_action**: *"Strip inferred intent from `title` / `notes`; mark reconstructed `verifies` `recovery_status: unknown`. See `retrofit-discipline-checks.md`."*

## Sweep order

Walk top to bottom. Items 1–11 are mechanically detectable; items 12–13 are also mechanical (schema-enforced) but carry the hard-reject weight — score them last so the verdict precedence is unambiguous.

## Aggregation rule

Multiple findings of the same anti-pattern across multiple cases are surfaced as separate findings (one per case) — not aggregated. Use `case_id: "GLOBAL"` for document-wide patterns (happy-path-bias at artifact level; ice-cream-cone-coverage at artifact level; coverage-as-quality-metric on the bar block; orphan-tests when artifact-level `verifies` is empty).

## Cross-link

`oracle-checks.md` (5, 6) · `verifies-traceability-checks.md` (12) · `retrofit-discipline-checks.md` (3, 13) · `test-double-discipline-checks.md` (7) · `coverage-mutation-bar-checks.md` (10) · `quality-bar-gate.md` (final gate)
