# Anti-patterns — sweep before delivering

Thirteen failure modes. Each has a tell. Sweep top-to-bottom on every draft. Hard-reject triggers marked ★ (refusal A/B/C in `SKILL.md`).

## Contents

- [Derivation gaps (4)](#derivation-gaps-4) — code-to-test, tautological, test-as-spec inversion, happy-path bias
- [Oracle quality (3)](#oracle-quality-3) — weak assertions, unbounded negatives, flaky tests
- [Coupling and isolation (3)](#coupling-and-isolation-3) — over-mocking, mystery guest, ice-cream-cone
- [Bar and traceability (3)](#bar-and-traceability-3) — coverage-as-quality-metric, orphan tests, fabricated retrofit intent
- [Sweep order](#sweep-order) and [Hard-reject mapping](#hard-reject-mapping)

## Derivation gaps (4)

### 1. Code-to-test derivation — `anti-pattern.code-to-test-derivation`

Tell: cases enumerate paths visible in the implementation; cases mirror the structure of test files; cases verify what the code happens to do, not what the spec demands.
Fix: derive cases from the parent spec (DD / Architecture / Requirements + PB), not from code or tests. Use the seam reference for the layer.

### 2. Tautological tests — `anti-pattern.tautological-tests`

Tell: `expected:` is computed using the same logic as the implementation (e.g., `expected: <invoke same algorithm>`). The case passes whenever the algorithm is consistent with itself, regardless of correctness.
Fix: state `expected:` as a specific value or property of the result, derived from the spec — not by re-running the implementation logic.

### 3. Test-as-requirement inversion — `anti-pattern.test-as-requirement-inversion`

Tell: in retrofit, the spec is back-derived from existing tests; cases exist for tests that already pass; the gap report is empty.
Fix: derive the TestSpec from the spec FIRST, then map existing tests against it. See `retrofit-discipline.md`.

### 4. Happy-path bias — `anti-pattern.happy-path-bias`

Tell: error / happy ratio < 1:2; many `functional` cases, no `error` / `boundary` / `fault-injection`.
Fix: walk the parent spec's preconditions, error-matrix rows, and downstream-failure paths; emit cases for each.

## Oracle quality (3)

### 5. Weak assertions ★ (refusal C) — `anti-pattern.weak-assertions`

Tell: `expected:` is `"verifies behaviour"`, `"works correctly"`, `"does not throw"` (alone), `"non-null"` (alone), `"instance of X"` (alone), or any qualitative non-bounded phrase.
Fix: replace with a specific value, an enumerated set, or a bounded predicate. See `case-quality.md`.

### 6. Unbounded negatives — `anti-pattern.unbounded-negative-tests`

Tell: `expected:` says "never X", "always Y", "for all Z" without bounding the domain (`for any sampled Cart with 0..50 lines: ...` is bounded; `never throws` over an undefined input space is not).
Fix: bound the domain — sample shape, equivalence class, or specific scenario.

### 7. Flaky tests — `anti-pattern.flaky-tests`

Tell: `preconditions:` does not name clocks / random seeds / ordering on inputs that need them; or the case description acknowledges intermittent failure ("usually passes").
Fix: name fixtures controlling time, randomness, ordering. Cases that pass most of the time are not self-validating.

## Coupling and isolation (3)

### 8. Over-mocking — `anti-pattern.over-mocking`

Tell: leaf case names > 2 test doubles in `preconditions:`; branch case is mock-heavy where state assertions would suffice; assertions are mostly `verify(mock)` calls.
Fix: cap leaf at 2 doubles; reserve interaction verification for cases where the interaction is the observable behaviour. See `test-double-discipline.md`.

### 9. Mystery guest — `anti-pattern.mystery-guest`

Tell: case relies on a fixture, seed, or test-data file not named in `preconditions:`; reading the case alone does not tell the test-author what to provide.
Fix: name every fixture / seed / external file in `preconditions:` so the case is self-describing.

### 10. Ice-cream-cone coverage — `anti-pattern.ice-cream-cone-coverage`

Tell: many e2e / system cases at the root, few unit / integration cases at lower layers. The cone is upside-down compared to the standard test pyramid.
Fix: push cases down to the layer that owns them. Root cases verify user-observable outcomes; leaves verify contracts.

## Bar and traceability (3)

### 11. Coverage as quality metric — `anti-pattern.coverage-as-quality-metric`

Tell: `coverage_mutation_bar:` declares structural coverage but not mutation score; or treats line coverage as the test-suite quality measurement.
Fix: declare both structural coverage and mutation score; name the gap. See `coverage-mutation-bar.md`.

### 12. Orphan tests ★ (refusal B) — `anti-pattern.orphan-tests`

Tell: artifact-level `verifies: []`; case missing `verifies:`; `verifies:` element does not resolve to an upstream ID; `verifies:` references look like file paths.
Fix: every case and the artifact carry non-empty `verifies:` pointing at upstream IDs that resolve. See `verifies-traceability.md`.

### 13. Fabricated retrofit intent ★ (refusal A) — `anti-pattern.fabricated-retrofit-intent`

Tell: retrofit case carries a confident `title:` derived from a method name; `notes:` reads like inferred reasoning; `recovery_status: verified` on reconstructed `verifies` without human confirmation; gap report missing or empty.
Fix: leave `title:` and `notes:` empty (`# HUMAN-ONLY`) until human supplies intent; mark reconstructed `verifies` as `recovery_status: unknown`; produce a populated gap report. See `retrofit-discipline.md`.

## Sweep order

Walk top to bottom. Items 1–4 are derivation gaps that surface at draft time (read the parent spec, count the cases). Items 5–10 surface on per-case review (oracle, fixtures, double counts). Items 11–13 are document-level invariants caught on the final pass.

## Hard-reject mapping

| Anti-pattern | Refusal | Severity |
|---|---|---|
| #5 Weak assertions | C | hard_reject |
| #12 Orphan tests | B | hard_reject |
| #13 Fabricated retrofit intent | A | hard_reject |
| `check.coverage-mutation.section-missing` | derived-hard | hard_reject |
| `check.spec-ambiguity-test.fail` | D | override (DESIGN_ISSUE) |

All other anti-patterns are soft-reject (accumulate to REJECTED).

## Cross-link

`derivation-strategies.md` (1–4) · `case-quality.md` (5–7) · `test-double-discipline.md` (8) · `per-layer-weight.md` (9–10) · `coverage-mutation-bar.md` (11) · `verifies-traceability.md` (12) · `retrofit-discipline.md` (13) · `quality-bar-checklist.md` (final gate)
