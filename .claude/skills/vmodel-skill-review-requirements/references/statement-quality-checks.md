# Statement-level quality — checks

**Contents**
- [Gate 1 — Atomic](#gate-1--atomic)
- [Gate 2 — Testable (the box test)](#gate-2--testable-the-box-test)
- [Gate 3 — Solution-free](#gate-3--solution-free)
- [Complementary-pair rule (state-driven statements)](#complementary-pair-rule-state-driven-statements)
- [Negative-requirement check (bounded vs unbounded)](#negative-requirement-check-bounded-vs-unbounded)
- [Passive-voice check](#passive-voice-check)

---

Every requirement statement, regardless of type, must clear three gates: atomic, testable, solution-free. Plus the complementary-pair rule for state-driven statements.

The review skill applies these gates to every statement and emits a finding for each violation.

## Gate 1 — Atomic

One `shall` per statement, verifying one behaviour.

### Check — count of `shall` per statement

For each requirement statement, count tokens matching `shall`. More than one is a compound (anti-pattern 1).

Tells:
- Two `shall`s in one statement
- "shall X **and** shall Y"
- "shall X, **including** Y, **and also** Z"
- A list separated by commas where each item is a behaviour

```
"The system shall authenticate the user and shall log the attempt."
                      ↑                            ↑
                   shall 1                      shall 2
```

- **check_failed**: `anti-pattern.compound`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement, mark both `shall`s
- **recommended_action**: *"Split into atomic statements per the EARS one-shall rule. See `ears-conformance.md`."*

The compound test (asked of every statement): if a test would need two independent check groups to verify the requirement, it is two requirements.

## Gate 2 — Testable (the box test)

A tester reading the statement alone, with the glossary, must be able to write a test that distinguishes conforming from non-conforming behaviour.

The box test is the reviewer's question: *can a tester, reading only this statement and the glossary, sketch the test?* If the answer is "I would need to ask someone", the statement is not yet testable.

### Tells of untestable statements

| Tell | Examples |
|---|---|
| Vague adjectives | *fast, sufficient, appropriate, adequate, user-friendly, robust, scalable, efficient, intuitive* |
| Ambiguous pronouns / referents | "validate it and return an appropriate response" — what is *it*? what is *appropriate*? |
| Unbounded lists / escape clauses | "such as X, Y, Z, **etc.**", "including but not limited to", "as appropriate", "where applicable" |
| Wishful thinking | "process requests with zero latency", "handle infinite concurrent users" |
| TBD / TBR / TODO markers | a placeholder cannot be tested |

### Findings

| Tell | check_failed | Severity |
|---|---|---|
| Vague adjective | `anti-pattern.vague-adjective` | soft_reject |
| Ambiguous pronoun | `anti-pattern.ambiguous-pronoun` | soft_reject |
| Unbounded list / escape clause | `anti-pattern.unbounded-list` | soft_reject |
| Wishful thinking | `anti-pattern.wishful-thinking` | soft_reject |
| TBD / TBR / TODO present in baselined statement | `anti-pattern.tbd-marker` | soft_reject |
| Box test fails for some other reason (e.g., missing condition that would let a tester decide) | `check.statement-quality.box-test-fails` | soft_reject |

`recommended_action` template: *"Replace the vague token with a measured quantity, or rewrite with explicit referents. See `ears-conformance.md` and `nfr-five-elements-checks.md`."*

## Gate 3 — Solution-free

The statement constrains *what* the system does, not *how*. Naming a database, framework, library, data structure, or algorithm inside the statement is a category error.

### Tells

Any of the following inside a non-interface requirement statement:
- Named product / service / library (e.g., "Redis", "PostgreSQL", "Kafka", "Bash", "React")
- Named data structure (e.g., "hash map", "linked list", "B-tree", "queue", "trie")
- Named algorithm (e.g., "AES-256", "SHA-512", "RSA", "Bloom filter", "consistent hashing")
- Named pattern (e.g., "singleton", "publish-subscribe", "circuit breaker")
- Named language feature (e.g., "Java's Optional", "Python's asyncio")

### The one exception

Interface requirements may legitimately name externally imposed protocols (HTTP/1.1, gRPC, OIDC 1.0, TLS 1.3, RFC 6749). The protocol is *what*, not *how* — it is a constraint imposed from outside, not a design choice made inside.

If the named token is an externally imposed protocol AND the statement is filed under Interface Requirements, no finding.

### Findings

- **check_failed**: `anti-pattern.implementation-prescription` (also alias `anti-pattern.requirements-smuggling-design`)
- **severity**: `hard_reject`
- **evidence shape**: quote the statement, name the smuggled token
- **recommended_action**: *"Move the design choice to an architectural decision (ADR). Replace here with the behavioural requirement the design choice was meant to ensure."*

This is one of the five hard-reject triggers. One occurrence rejects the document.

## Complementary-pair rule (state-driven statements)

Every state-driven (*While …*) requirement implicitly raises: what does the system do *outside* that state? A document that says one half of the pair has a latent gap.

### Two acceptable resolutions

**Resolution A** — author the complementary pair (a second statement covering out-of-state behaviour).

**Resolution B** — explicitly mark out-of-state as out of scope, in the rationale of the single statement.

### Check

For every state-driven statement (*While \<state>, the system shall \<response>*):

1. Search the document for a complementary statement covering *not in \<state>*. If found → no finding.
2. If not found, inspect the rationale. If the rationale explicitly states out-of-state is out of scope (or governed by another scope) → no finding.
3. Otherwise → finding.

- **check_failed**: `check.statement-quality.state-driven-no-complement`
- **severity**: `soft_reject`
- **evidence shape**: quote the state-driven statement; note absence of complement and absence of out-of-scope marker in rationale
- **recommended_action**: *"Either author the complementary out-of-state statement, or add a one-line note in the statement's rationale explaining that out-of-state behaviour is governed by another scope. See `statement-quality-checks.md` Resolution A vs B."*

What is **not** acceptable: silently leaving the out-of-state behaviour unspecified. That is a gap, not a decision.

## Negative-requirement check (bounded vs unbounded)

A statement of the form *"The system shall not \<X>"* is allowed if X is bounded. Bounded negatives are legitimate (e.g., *"shall not log session tokens in plaintext to any destination"* — bounded scope, verifiable).

Unbounded negatives are not testable:
- *"shall not crash"*
- *"shall not fail"*
- *"shall not be slow"*

The scope is infinite; the negative cannot be exhaustively verified.

- **check_failed**: `anti-pattern.unbounded-negative`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement; note why the negative is unbounded (no clear scope, no enumerable cases)
- **recommended_action**: *"Rewrite as a positive unwanted-behaviour EARS statement: 'If \<condition>, then the system shall \<response>'. See `ears-conformance.md`."*

## Passive-voice check

A statement of the form *"\<thing> shall be \<verb>ed"* without a named subject leaves "by whom?" unanswered.

Tell:
- *"Session data shall be validated."*
- *"Tokens shall be invalidated on logout."*

- **check_failed**: `anti-pattern.passive-no-actor`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement; note the missing subject
- **recommended_action**: *"Rewrite with a named subject. Ask: by whom? at what point? against what?"*
