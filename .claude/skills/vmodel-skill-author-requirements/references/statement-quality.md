# Statement-level quality

When drafting a statement: check it has one `shall`, can be tested from statement+glossary alone, and names no technology / library / algorithm. Plus the complementary-pair rule for state-driven statements.

## Gate 1 — Atomic

One `shall` per statement, verifying one behaviour.

Compound statements break atomicity:

```
BAD:  "The system shall authenticate the user and shall log the attempt."
      Two `shall`s, joined by "and". Two requirements glued together.

FIX:  Split into two atomic statements:
      REQ-A: "When a user submits a credential pair, the session service shall
              validate it against the identity provider."
      REQ-B: "When the session service validates a credential pair, it shall
              emit a 'credential-validation' audit event with outcome
              (success|failure) and user identifier."
```

The compound test: if a test would need two independent check groups to verify the requirement, it is two requirements.

Tells of a compound statement:

- Two `shall`s in one statement
- "shall X **and** shall Y"
- "shall X, **including** Y, **and also** Z"
- A list separated by commas where each item is a behaviour

## Gate 2 — Testable (the box test)

A tester reading the statement alone, with the glossary, can write a test that distinguishes conforming from non-conforming behaviour.

The box test is the author's own check: *can I, reading only this statement and the glossary, sketch the test?* If the answer is "I would need to ask someone", the statement is not yet testable.

```
BAD:  "The system shall handle errors gracefully."
      What is "gracefully"? What counts as "error"? A tester cannot write a
      passing or failing test from this.

FIX:  "If the session service is unable to reach the backing store, then it
       shall return 503 to validation requests with header 'Retry-After: 1',
       shall fail the readiness probe, and shall not return 200 with
       valid=false."
```

Common testability killers:

- **Vague adjectives** — *fast, sufficient, appropriate, adequate, user-friendly, robust*
- **Ambiguous pronouns** — "validate it and return an appropriate response" — what is *it*? what is *appropriate*?
- **Unbounded lists / escape clauses** — "such as X, Y, Z, etc.", "including but not limited to", "as appropriate", "where applicable"
- **Wishful thinking** — "process requests with zero latency", "handle infinite concurrent users"
- **TBD/TBR/TODO markers** — a placeholder cannot be tested

## Gate 3 — Solution-free

The statement constrains *what* the system does, not *how*. Naming a database, framework, library, data structure, or algorithm inside the statement is a category error.

```
BAD:  "The session service shall use Redis with a 5-second TTL for active
       session state."
      Names two implementation choices (Redis, 5-second TTL).

FIX:  Move the storage choice to an ADR. Replace the requirement with the
      behaviour the choice was meant to ensure:
      "While a session is in the ACTIVE state, the session service shall
       reflect session-state changes to all nodes within 5 seconds of commit,
       measured at p99 under normal load."
```

**The one exception** — interface requirements may legitimately name externally imposed protocols (HTTP/1.1, gRPC, OIDC 1.0, TLS 1.3, RFC 6749). The protocol is *what*, not *how*: it is a constraint imposed from outside, not a design choice made inside.

Audit specifically for: named technologies, named libraries, named data structures, named algorithms, named patterns ("singleton", "publish-subscribe"), named language features. None of these belong in a requirement statement.

## Complementary-pair rule (state-driven statements)

Every state-driven (*While …*) requirement implicitly raises: what does the system do *outside* that state? A document that says one half of the pair has a latent gap; a reviewer cannot tell whether the omission was deliberate or accidental.

Two acceptable resolutions:

### Resolution A — author the complementary pair

```
REQ-A: "While the session is elevated, the session service shall allow
        changes to payment methods."

REQ-B: "While the session is not elevated, the session service shall require
        step-up authentication before allowing changes to payment methods."
```

### Resolution B — explicitly mark out-of-state as out of scope

If the system's behaviour outside the state is genuinely undefined or owned by another scope, say so in the rationale of the single statement:

```
REQ-A: "While the session is elevated, the session service shall allow
        changes to payment methods."
       rationale: "Behaviour for non-elevated sessions is governed by the
                   parent-scope authentication requirements; this scope is
                   only responsible for elevated-session policy."
```

What is **not** acceptable: silently leaving the out-of-state behaviour unspecified. That is a gap, not a decision.
