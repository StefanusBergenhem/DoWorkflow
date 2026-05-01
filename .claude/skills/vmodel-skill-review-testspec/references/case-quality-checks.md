# Case quality checks

Per-case craft quality. Mirrors `case-quality.md` on the author side. Walk every case for F.I.R.S.T. adherence and AAA structure. Oracle specificity is delegated to `oracle-checks.md`.

## check.case-quality.firs-violation (soft)

**Check that** every case adheres to the F.I.R.S.T. property set: Fast (no real network/IO without explicit fixture); Independent (no order dependency between cases; no shared mutable state); Repeatable (deterministic — clocks frozen, randomness seeded, time zones declared); Self-validating (oracle is concrete); Timely (case authored alongside or before code, not after).

**Reject when** a case shows any tell:
- shared mutable state across cases (preconditions reference state set by an earlier case)
- real `Clock.now()` / `random()` / `LocalDateTime.now()` without naming the seed or freeze
- order-dependent assumptions (case `n` requires case `n-1` to have run)
- case asserts no oracle and only reports completion (e.g. `expected: succeeded`)

**Approve when** preconditions name fixtures explicitly, clocks/random are seeded or frozen with named values, and each case is self-contained.

**Evidence pattern:** name the case id; quote the offending precondition or step.

**recommended_action:** *"Apply F.I.R.S.T. property remediation per the matched author skill's case-quality reference. Name the seed / freeze / fixture; eliminate shared state; ensure each case is self-validating."*

## check.case-quality.aaa-violation (soft)

**Check that** every case has one Act — one observable invocation under test per case. Steps prior to Act are Arrange; assertions after Act are Assert.

**Reject when** `steps:` enumerates multiple Acts (multiple observable invocations of the unit-under-test in a single case) — splitting hides which Act drives the failure.

**Approve when** preconditions name Arrange, one numbered step is Act (or Act is implicit at leaf), assertions are Assert.

**Evidence pattern:** name the case id; quote the multi-Act steps.

**recommended_action:** *"Split each Act into its own case. AAA: one Arrange, one Act, one set of Asserts per case. The matched author skill's case-quality reference covers the split."*

## Cross-link

`oracle-checks.md` (oracle specificity, refusal C) · `per-layer-weight-checks.md` (per-layer case shape) · `quality-bar-gate.md` (Case quality card)
