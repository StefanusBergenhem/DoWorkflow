# Interface contract checks

Mirrors the author-side `interface-contracts.md`. Walk every Interface entry against the eleven checks below. The last check (`check.interface.implementation-leak`) is HARD; the rest are soft-reject.

## Contents

- `check.interface.missing-precondition`
- `check.interface.missing-postcondition`
- `check.interface.missing-invariant`
- `check.interface.missing-typed-error`
- `check.interface.missing-quality-attribute`
- `check.interface.missing-authn-authz`
- `check.interface.fat-god-interface`
- `check.interface.missing-versioning-policy`
- `check.interface.missing-rationale`
- `check.interface.protocol-not-cited-by-spec`
- `check.interface.implementation-leak` (HARD)

## check.interface.missing-precondition (soft)

**Check that** every Interface entry has a non-empty `preconditions:` block stating what the caller must satisfy.

**Reject when** `preconditions:` is absent, empty, or trivially restated as "valid request".

**Approve when** preconditions name caller obligations: state assumptions, header presence, claim ownership, idempotency-key uniqueness, etc.

**recommended_action:** *"Add preconditions per the Design-by-Contract triple — preconditions narrow the search for fault attribution when the call fails."*

## check.interface.missing-postcondition (soft)

**Check that** every Interface entry has postconditions for **all three branches**: `on_success`, `on_precondition_failure`, `on_downstream_failure`.

**Reject when** any of the three branches is missing. A single `postconditions:` block stating only the success path is half-specified.

**Approve when** all three branches state guarantees: success state mutation, error returned + no-mutation on precondition failure, error returned + cleanup window on downstream failure.

**Evidence pattern:** name which branch is missing.

**recommended_action:** *"Complete the postcondition triple. A missing branch is where integration-time surprises live."*

## check.interface.missing-invariant (soft)

**Check that** every Interface entry has `invariants:` stating properties preserved before and after the operation.

**Reject when** `invariants:` is absent, empty, or restates a postcondition.

**Approve when** invariants name properties holding across the call (idempotency under same key, ordering preservation, monotonicity of state transitions).

**recommended_action:** *"State the invariants the operation preserves. An interface without invariants under-specifies what the supplier guarantees beyond the immediate outcome."*

## check.interface.missing-typed-error (soft)

**Check that** every Interface entry has a typed error enum — a list of `{code, http (or status), meaning}` triples.

**Reject when** errors are described in prose only, or reduced to "returns 4xx on bad request, 5xx on internal error".

**Approve when** every distinguishable error has a stable code, an HTTP status (or equivalent transport status), and a one-line meaning.

**recommended_action:** *"Tabulate the typed errors. Prose-only error descriptions force every consumer to invent their own code-mapping."*

## check.interface.missing-quality-attribute (soft)

**Check that** every Interface entry has `quality_attributes:` stating at least latency budget and (where applicable) availability and throughput obligations carried by this interface.

**Reject when** `quality_attributes:` is absent or names attributes without budget numbers.

**Approve when** budgets are concrete (p95 ≤ N ms, availability X% monthly) and tied to parent NFR ids where possible.

**recommended_action:** *"Add a quality-attribute block. Budgets at the interface are how downstream Detailed Designs know what to build to."*

## check.interface.missing-authn-authz (soft)

**Check that** every **externally callable** Interface entry states authentication (what identity, how verified, what claims) and authorisation (what permission, evaluated at which layer).

**Reject when** an externally callable interface omits authn or authz, or names them without the evaluation layer (gateway / middleware / handler).

**Approve when** authn and authz are stated with the evaluation layer named.

**Conditional gating:** does NOT apply to internal-only interfaces between trusted components in the same trust zone.

**recommended_action:** *"State authentication and authorisation per externally callable interface, with the evaluation layer named. Mixing them up is how systems authenticate correctly and authorise catastrophically."*

## check.interface.fat-god-interface (soft)

**Check that** no Interface entry combines unrelated responsibilities into one fat operation set (Interface Segregation Principle).

**Reject when** an interface has 12+ operations covering distinct concerns, OR exposes operations any one consumer would not use.

**Approve when** interfaces are narrow per responsibility — a Decomposition entry with three responsibilities has three segregated interfaces.

**recommended_action:** *"Segregate by responsibility. Fat interfaces couple consumers to changes they do not care about."*

## check.interface.missing-versioning-policy (soft)

**Check that** every externally visible interface names a versioning scheme AND a deprecation policy.

**Reject when** `version:` is absent, OR `deprecation_policy:` is absent, OR the policy is decorative without a window ("breaking changes increment MAJOR" with no parallel-live commitment).

**Approve when** the version is concrete (e.g., `1.0.0`) and the deprecation policy names a window in time units (months, quarters) for parallel-live versions.

**recommended_action:** *"Add a versioning scheme and a deprecation policy with a concrete parallel-live window. Both go in the artifact, not invented during a breaking-change emergency."*

## check.interface.missing-rationale (soft)

**Check that** every Interface entry carries a `rationale` field tying the choice (protocol, contract shape, error model) to a requirement, ADR, or named trade-off.

**Reject when** rationale is absent, empty, or generic ("REST is simple", "this is best practice").

**Approve when** rationale cites a requirement id, an ADR id, or a specific trade-off ("synchronous chosen because the user is waiting; outbox decouples bus availability from HTTP 200").

**recommended_action:** *"Tie the interface choice to a requirement or governing ADR. Generic principle invocation is fabrication, not rationale."*

## check.interface.protocol-not-cited-by-spec (soft)

**Check that** externally-imposed protocols are cited by RFC, spec id, or standard identifier rather than informal name.

**Reject when** the protocol is named informally ("OAuth2 with refresh tokens") without an RFC or spec id.

**Approve when** the citation is concrete: `OIDC 1.0 Core / RFC 6749 Section 1.5`, `HTTP/1.1 RFC 7230`, `gRPC over HTTP/2 RFC 7540`, etc.

**recommended_action:** *"Cite the protocol by its RFC or spec identifier. Informal names paper over version differences."*

## check.interface.implementation-leak (HARD — refusal B sub-tell)

**Check that** no Interface entry leaks internal storage choice, internal algorithm, or internal library across the boundary.

**Reject when** an Interface contract names: a database (`stored in Postgres`), an internal data structure (`returned as a LinkedHashMap`), an internal cache (`served from Redis`), a specific library call inside the supplier, OR any other implementation detail that should live in Detailed Design.

**Approve when** the contract states only what the supplier guarantees externally — types, ordering, idempotency, latency — without naming internal mechanics.

**Evidence pattern:** quote the leak verbatim and name what should have been said instead at the contract level.

**recommended_action:** *"Move implementation choice to Detailed Design. Replace here with the externally observable invariant the implementation was meant to guarantee."*

Cross-link: `anti-patterns-catalog.md` (dd-content-in-architecture); `quality-bar-gate.md` (Interfaces card).
