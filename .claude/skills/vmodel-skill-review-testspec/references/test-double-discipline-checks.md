# Test double discipline checks

Test doubles are dummy / stub / spy / mock / fake (Meszaros taxonomy). Each preconditions block declaring a double is checked for named type, contract-test pointer (fakes), max-double thresholds, and interaction-overuse. Conditional gating: only fires when preconditions declare doubles.

## check.test-doubles.type-unnamed (soft)

**Check that** every test double in `preconditions` is named with its type — dummy / stub / spy / mock / fake.

**Reject when** a precondition introduces a double without naming the category (e.g. *"a clock"*, *"a repository instance"*, *"a fake-ish thing"*).

**Approve when** every double carries an explicit type label — e.g. *"stub clock returning `2026-04-30T00:00:00Z`"* or *"fake in-memory repository implementing `JobRepository` (contract-test against `PostgresJobRepository`)"*.

**Evidence pattern:** name the case id; quote the unnamed double; cite the missing type label.

**recommended_action:** *"Name the double's type from the closed taxonomy (dummy/stub/spy/mock/fake). The matched author skill's test-double-discipline reference covers the discriminators."*

## check.test-doubles.fake-without-contract-test (soft)

**Check that** every fake declares a contract-test pointer — the test that verifies the fake matches the real implementation's contract.

**Reject when** a fake is introduced with no `contract_test` pointer (no test id, no DD reference, no statement of how the fake is kept honest).

**Approve when** the fake names a contract-test target — e.g. *"contract-test: TC-jobs-04 — JobRepository contract test runs against both fake and real impl"*.

**Evidence pattern:** name the case id; quote the fake without contract-test pointer.

**recommended_action:** *"Add a contract-test pointer for the fake. Fakes drift from the real impl unless contract-tested; without the pointer the fake is a liability."*

## check.test-doubles.leaf-over-threshold (soft)

**Check that** at leaf, no single case uses more than 2 doubles total.

**Reject when** a leaf case declares 3+ doubles in preconditions — the unit-under-test has more than 2 collaborators; the test is testing collaboration, not the unit.

**Approve when** leaf cases declare ≤ 2 doubles, OR the case is correctly classified as branch-level (and the TestSpec's level matches).

**Evidence pattern:** name the case id; count and quote the doubles.

**recommended_action:** *"Reduce the doubles to ≤ 2 (introduce a real collaborator, or split the case), OR move the case to the branch above. The matched author skill's test-double-discipline reference covers thresholds."*

## check.test-doubles.interaction-overuse (soft)

**Check that** interaction verification (mocks / spies asserted via `verify()` calls) is reserved for cases where the interaction itself is the observable behaviour.

**Reject when** a leaf case uses `verify()`-style interaction assertions on a stub or on a mock whose interaction is not the unit's contractual obligation. Interaction verification couples tests to implementation; when the contract is "produces value V from inputs X", the test asserts V, not the calls made along the way.

**Approve when** mocks / spies are used only on collaborators where the interaction (call sequence, parameter values, count) is the observable contract.

**Evidence pattern:** name the case id; quote the verify-call; describe what the contract actually asserts.

**recommended_action:** *"Replace interaction verification with state-or-result assertion when the interaction is not the observable contract. Save verify() for cases where the call itself is the behaviour. See the matched author skill's test-double-discipline reference."*

## Cross-link

`anti-patterns-catalog.md` (over-mocking 6, mystery-guest 7) · `case-quality-checks.md` · `quality-bar-gate.md` (Test-double card)
