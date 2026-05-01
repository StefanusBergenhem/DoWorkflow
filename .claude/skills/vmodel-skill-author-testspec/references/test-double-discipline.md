# Test-double discipline

When a case names a collaborator the leaf does not own — the collaborator stands in via a test double. The double type is contract; ambiguous "mock" usage hides whether the case is verifying state or interaction.

## The five named types (Meszaros)

| Type | One-line discriminator | When to use |
|---|---|---|
| **dummy** | Object passed but never used (placeholder for a required parameter) | Required parameter with no behavioural role in the case |
| **stub** | Returns canned values; no assertions on calls | Indirect input — the case needs the collaborator to return X |
| **spy** | Records calls; assertions on call shape after the fact | The case verifies that a call was made (interaction is observable behaviour) |
| **mock** | Pre-programmed expectations; assertions inline as the call happens | The case verifies a specific call sequence as the contract |
| **fake** | Working implementation simpler than production (in-memory DB, in-memory queue) | Indirect input AND output where canned values are insufficient |

When `preconditions:` say "test double" without naming the type → finding `check.test-doubles.type-unnamed`. Name the type explicitly:

```yaml
preconditions:
  - "Test double: ClockSource as stub returning 2026-01-01T00:00:00Z"
  - "Test double: NotificationService as spy"
```

## Fake → contract test required

When a fake is named in `preconditions:` → the TestSpec must elsewhere carry a `contract` case asserting the fake matches the real implementation's contract. Otherwise the fake silently drifts from production.

Slot-fill:

```yaml
- id: TC-<scope>-N
  title: "InMemoryQueue fake matches Postgres queue contract"
  type: contract
  verifies:
    - "ARCH-<scope>.interfaces.JobQueue"
  preconditions:
    - "Subject: InMemoryQueue (fake)"
    - "Reference: PostgresJobQueue (real implementation)"
  expected:
    - "All published interface postconditions hold for the fake under the same input"
```

When the fake is third-party (named library) → the contract case names the version; pin the version in `preconditions:`. Without version pinning, the contract drifts on dependency upgrade.

## Max two doubles per leaf case

When a leaf case names more than two test doubles in `preconditions:` → finding `check.test-doubles.leaf-over-threshold`. The case is over-mocking; usually the case actually belongs at the branch (where the collaborators compose) or the leaf has too many collaborators (a design issue surfaced through tests).

Default escape hatch: when three doubles are genuinely needed, escalate the case to the branch TestSpec. When the branch is also tight, the design surfaces a coupling that warrants a finding upstream — emit a comment on the case noting the upstream design issue.

## Interaction verification — reserved use

When the case asserts `verify(mock).method(args)` style assertions → the interaction itself must be the observable behaviour the spec demands (e.g., "publishes event X" / "calls audit log on every reject"). When the spec specifies state, not interaction → use stubs and assert state, not mocks.

Tells of overuse:
- Multiple `verify(...)` lines per case where the spec specifies a return value
- `verify` count exceeds the case's state assertions
- `verify` checks irrelevant call ordering with no spec basis

When detected → finding `check.test-doubles.interaction-overuse`. Replace with stubs and state assertions where possible.

## "Don't mock what you don't own"

When the collaborator is a third-party library, framework, or external service → wrap the dependency in a project-owned interface (DD's responsibility), then mock the project-owned interface. Direct mocks of third-party APIs:
- Drift on library upgrade
- Encode brittle assumptions about the library's contract
- Couple test code to library internals

The wrapping is a DD-level concern; the TestSpec records the wrapper boundary in `preconditions:` and mocks at the boundary.

## Cross-link

`per-layer-weight.md` (the leaf vs branch shape distinction) · `case-quality.md` (interaction-vs-state distinction in oracle specificity) · `architecture-traceability-cues.md` (when contract cases land at branch) · `anti-patterns.md` (`anti-pattern.over-mocking`)
