# Quality Bar — self-check before delivering

Yes/No checklist grouped by concern. Every No is flagged inline in the artifact, not silently passed. The Spec Ambiguity Test (last card) is the **override gate**.

## Structure and completeness

- [ ] All seven sections present (Metadata, Overview, Public Interface, Data Structures, Algorithms, State, Error Handling); each has substantive content OR an explicit assertion of absence
- [ ] Metadata includes `scope`, `parent_architecture`, `derived_from` (non-empty), `governing_adrs` where applicable
- [ ] Overview names what slice of the parent Architecture this leaf realises, at one level above the code

## Contracts on every public function

- [ ] Every public function has preconditions, postconditions (split `on_success` / `on_failure`), error semantics, nullability, side effects, thread-safety category
- [ ] Numeric parameters carry a typed unit-wrapper OR have the unit named in the contract
- [ ] Postconditions express properties of the result, not steps the implementation takes
- [ ] Where transformation results apply, both halves are stated (ordered AND permutation; filtered AND complete; mapped AND positionally-corresponding)

## Data and invariants

- [ ] Every internal data structure lists fields, invariants, ownership, lifetime
- [ ] Mutable state shared across threads or callers states locking, happens-before, and per-field reader/writer
- [ ] Functions that return mutable objects state the returned-object semantics (copy / live view / read-only reference / snapshot / ownership transfer)

## Algorithms, state, control flow

- [ ] Where the algorithm is contractual (determinism / worst-case bound / operational constraint / wire compatibility), it is named AND the reason is named
- [ ] Where the algorithm is implementer's choice, the DD states the result property and leaves the algorithm open
- [ ] If a state machine is present: states, transitions, guards, and undefined-event handling named; fits on two pages

## Error handling

- [ ] Every error class answers all six questions (what, detection, containment, recovery, end state, caller receives)
- [ ] Error-handling matrix populated; each row corresponds to a testable robustness case
- [ ] Every row picks one of the five recovery strategies (fail-fast / retry-bounded / fallback / compensate / propagate)
- [ ] No row is "undefined" on the state-after-error column

## Rationale and traceability

- [ ] Every non-obvious decision carries inline rationale naming the forcing constraint (one of: external / architectural / resource / temporal)
- [ ] Load-bearing + cross-cutting + hard-to-reverse decisions are extracted to ADR (not inlined); `governing_adrs:` lists them; body citations appear at decision-application points
- [ ] `derived_from` points to upstream artifact ids (REQ, ARCH interface, sibling DD, or ADR); non-empty

## Retrofit honesty (retrofit only)

- [ ] `recovery_status` set per kind: code-derivable fields `verified`/`reconstructed`/`unknown`; human-only fields (Overview, rationale) `verified`/`unknown` only — schema enforces
- [ ] Every `reconstructed` field cites file/line/commit/schema evidence
- [ ] Every `unknown` field is paired with a follow-up owner and action
- [ ] Gap report populated (lost rationale, behavioural drift, missing ADRs, coverage gaps)
- [ ] No fabricated rationale (no generic-principle invocation)
- [ ] Observable-vs-inferred distinction marked ("observed from code" passages tagged)

## TestSpec traceability

- [ ] Every postcondition `on_success` / `on_failure` clause has a test target identified (contract / robustness / property)
- [ ] Every error-matrix row maps to a sibling TestSpec robustness row (by id or by `[NEEDS-TEST: ...]` stub)
- [ ] Every load-bearing invariant (function, data, state) has a property-test target identified

## Spec Ambiguity Test — meta-gate (override)

- [ ] Could a junior engineer, reading only this DD (plus parent Architecture, governing ADRs, derived requirements), produce a correct implementation without guessing?
- [ ] Could a test engineer, reading only this DD, write the unit-test suite without seeing the code?
- [ ] Would an equivalent implementation in a different language (Java / Python / Go) satisfy the same DD?

If any answer is No → the DD is not done. **This test overrides every box above.** A DD that ticks every Yes/No item but cannot guide a junior engineer has not done the job DD exists to do.

## Cross-link

Each card has a backing reference: `function-contracts.md`, `data-structures-by-invariant.md`, `algorithms.md`, `state-and-concurrency.md`, `error-handling.md`, `rationale-capture.md`, `adr-extraction-cues.md`, `retrofit-discipline.md`, `testspec-traceability-cues.md`, `anti-patterns.md`.
