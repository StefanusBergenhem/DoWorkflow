# Interface contracts

Interfaces are the committable surface of an Architecture. Components come and go; interfaces, once published, are load-bearing for everything downstream. **Specify interfaces to the level that a second team could build against them without talking to the first.**

## Contents

- Syntax vs semantics — the load-bearing split
- Design by Contract (Meyer / Hoare)
- Postcondition triple
- SEI nine-part interface template
- Interface Segregation Principle (ISP, Martin)
- Versioning and deprecation
- What goes in Architecture vs what goes in Detailed Design
- Authn / authz at the interface boundary

## Syntax vs semantics — the load-bearing split

| | Syntactic | Semantic |
|---|---|---|
| What | Signature, types, error enum | Preconditions, postconditions, invariants, failure modes, quality-of-service |
| Checked by | Compiler / schema validator | Tests / human review / Quality Bar |
| What ships in most docs | This | Often missing |
| Where most integration failures come from | Not this | This |

An Architecture interface entry specifies **both** halves, or the interface is not specified.

## Design by Contract (Meyer / Hoare)

Every externally callable operation has three contract clauses:

- **Preconditions** — what the caller must satisfy on the way in.
- **Postconditions** — what the supplier guarantees on the way out, including error returns.
- **Invariants** — state properties the operation preserves before and after.

Hoare's foundational axiom: `{P} S {Q}` — "if P holds and S executes, then Q holds".

**Why three clauses, not two:** fault attribution is the value.

- Postcondition failure → supplier is at fault.
- Precondition failure → caller is at fault.
- Invariant violation → either; the contract narrows the search.

At Architecture level the clauses are prose or structured YAML; at Detailed Design they become assertable predicates; at test level they become test cases. The chain breaks if any layer skips them.

## Postcondition triple

Every interface entry carries postconditions for **three branches**, not just one:

```yaml
postconditions:
  on_success:
    - <<what the system guarantees when it returned 2xx / OK>>
  on_precondition_failure:
    - <<typed error returned + 'no state mutation'>>
  on_downstream_failure:
    - <<typed error returned + state cleanup window>>
```

A single-branch `postconditions:` block is half-specified.

## SEI nine-part interface template

The Carnegie Mellon SEI's *Views and Beyond* gives a complete interface document structure. At Architecture level, **the first five are mandatory**; the last four are where drift hides:

1. Identity (name, version)
2. Resources provided (operations with syntax + semantics)
3. Data types (structures passed across)
4. Exceptions / errors (raised when, propagated how)
5. Quality attributes (performance, throughput, availability budget carried by this interface)
6. Element requirements (constraints on caller or supplier beyond the operations)
7. Rationale (why this interface, why not a different shape)
8. Usage guide (how to call correctly)
9. *(Plus standard SEI metadata)*

Slot-fill check: an entry missing #1-5 is incomplete. Entries missing #6-7 drift first.

Source: Clements, Bachmann, Bass, Garlan et al., *Documenting Software Architectures: Views and Beyond* (SEI/Addison-Wesley, 2nd ed., 2011).

### Authoring shape — single-file or helicopter+bundle (Rule 8)

For small architectures (≤4 interfaces, modest DbC volume), author each interface inline with full DbC clauses. For larger architectures where the artifact would otherwise breach the working token budget, MAY author in helicopter+detail-bundle form: keep the slim form (`name, from, to, protocol, contract.operation, contract.summary_postcondition, contract.key_invariants, contract.rationale, detail`) in `architecture.md` and move the full preconditions, postconditions (multi-branch), invariants, errors, quality_attributes, authentication, authorisation, version, deprecation_policy to `architecture/interfaces/<NAME>.md`. `summary_postcondition` is one sentence summarising the success guarantee plus halt/error behaviour; `key_invariants` is 1–3 IDs (REQ / IC / ADR) the seam structurally enforces. The framework treats helicopter + bundle as one logical artifact.

## Interface Segregation Principle (ISP, Martin)

No component should depend on an interface method it does not use. Fat interfaces couple consumers to changes they do not care about.

**Rule:** when a Decomposition entry allocates three distinct responsibilities, each responsibility gets its own narrow interface — not a single god-interface whose every change forces a re-review of every consumer. Segregated interfaces also survive teardown: retiring one responsibility does not force a breaking change on the other two.

Tell: an interface with 12+ operations covering different concerns. Split.

## Versioning and deprecation

Any externally visible interface needs a versioning scheme **and** a deprecation policy. Both go in the Architecture artifact, not invented during a breaking-change emergency.

- **Scheme:** semantic versioning (`MAJOR.MINOR.PATCH`) is the defensible default.
  - `1.0.0` for a new interface.
  - `MINOR` bump on additive change.
  - `MAJOR` bump on breaking change, preceded by a deprecation window with both versions live.
- **Deprecation window:** explicit time unit (weeks, months, quarters). For a *Published Language* interface (DDD context-mapping), this is a hard commitment.

Slot-fill:

```yaml
version: "1.0.0"
deprecation_policy: "Minimum 12 months between v(N) and v(N+1) breaking change; both versions live in parallel during the window."
```

## What goes in Architecture vs what goes in Detailed Design

| At Architecture | At Detailed Design |
|---|---|
| Contract shape (operations, types, errors) | Internal algorithm |
| Preconditions / postconditions / invariants | Data structure choice (LinkedHashMap vs TreeMap) |
| Quality-of-service obligation (p95 latency, availability) | Caching strategy inside a component |
| "All requests are idempotent" (a contract) | "Implemented using a Redis dedup store" (an implementation) |
| Externally-imposed protocols cited by RFC/spec | Specific framework's routing layer |
| Authn/authz at boundary + evaluation layer | Hashing algorithm internals |

Cross-link: see SKILL.md hard refusal B and `anti-patterns.md` (laundered architecture).

## Authn / authz at the interface boundary

Every externally callable interface entry states:

- **Authentication:** what identity is asserted, how it is verified, what claims are carried.
- **Authorisation:** what permission is required, evaluated at which layer (gateway / middleware / handler).

Mixing these up is how systems authenticate correctly and authorise catastrophically. Architecture is the right layer to state them — putting them in DD alone duplicates the decision per component.

Cross-link: `observability-and-security.md` for trust zones + STRIDE; `anti-patterns.md` for fat-interface tells.
