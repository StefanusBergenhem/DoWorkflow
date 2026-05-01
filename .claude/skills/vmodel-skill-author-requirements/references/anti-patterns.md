# Anti-patterns — universal nine + AI-era seven

**Contents**
- [Universal (nine)](#universal-nine) — patterns 1–9 with mechanical tells
- [AI-era and retrofit (seven)](#ai-era-and-retrofit-seven) — patterns 10–16 needing judgement
- [Sweep order](#sweep-order)

---

When delivering a requirements document: sweep this catalog. Each entry has a tell and a recipe.

## Universal (nine)

### 1. Compound requirement

**Tell**: two or more `shall`s in one statement; "shall X **and** shall Y"; comma-separated list of behaviours.

**Recipe**: split into atomic statements, one `shall` and one behaviour each. Renumber and re-link `derived_from` on each fragment to the original upstream source.

```
BAD:  "The system shall authenticate the user and shall log the attempt."
GOOD: REQ-A: "When a user submits a credential pair, the system shall validate
              it against the identity provider."
      REQ-B: "When the system validates a credential pair, it shall emit a
              'credential-validation' audit event with outcome and user identifier."
```

### 2. Vague adjective

**Tell**: *fast, sufficient, appropriate, adequate, user-friendly, robust, scalable, maintainable, efficient, intuitive*. The statement reads approvable but no test distinguishes pass from fail.

**Recipe**: replace the adjective with a measured quantity (apply the NFR five-element rule), or remove the requirement.

```
BAD:  "The session service shall be highly available."
GOOD: "The session-validation endpoint shall achieve ≥ 99.95% availability over
       any rolling 30-day window, excluding planned maintenance windows declared
       at least 48 hours in advance via the status page."
```

### 3. Passive voice without actor

**Tell**: "<thing> shall be <verb>ed" with no named subject — "Session data shall be validated."

**Recipe**: rewrite with a named subject. Ask: by whom? at what point? against what?

```
BAD:  "Session tokens shall be invalidated on logout."
GOOD: "When a user logs out, the session service shall transition the session
       to INVALIDATED and shall reject all subsequent validation requests for
       that session with status 401 and reason 'invalidated'."
```

### 4. Unbounded list / escape clause

**Tell**: "such as X, Y, Z, **etc.**", "**including but not limited to**", "**as appropriate**", "**where applicable**", "and so on", "for example".

**Recipe**: name the complete list, or scope the requirement to a defined set. If the list genuinely cannot be enumerated, the requirement is too vague to be useful.

```
BAD:  "The system shall validate inputs including but not limited to email,
       phone, and address fields."
GOOD: List the validation rule per field type, each as its own requirement, or
      author a data requirement that names the exhaustive field set.
```

### 5. Unbounded negative requirement

**Tell**: "The system shall **not** <unbounded outcome>" — *"shall not crash"*, *"shall not fail"*, *"shall not be slow"*. The negative cannot be exhaustively verified; the scope is infinite.

**Recipe**: rewrite as a positive unwanted-behaviour EARS statement (`If <condition>, then the system shall <response>`). Bounded negatives are legitimate (e.g. "shall not log session tokens in plaintext to any destination"); unbounded ones are not.

```
BAD:  "The session service shall not fail."
GOOD: "If the session service is unable to reach the backing store, then it
       shall return 503 to validation requests with header 'Retry-After: 1',
       shall fail the readiness probe, and shall not return 200 with valid=false."
```

### 6. Implementation prescription (design smuggled as requirement)

**Tell**: a named technology, library, framework, data structure, or algorithm inside a requirement statement (outside externally imposed interface protocols).

**Recipe**: move the design choice to an architectural decision (ADR). Replace here with the behaviour the design choice was meant to ensure.

```
BAD:  "The session service shall use Redis with a 5-second TTL for active
       session state."
GOOD: Move "Redis + 5-second TTL" to ADR-NNN. Author here:
      "While a session is in the ACTIVE state, the session service shall reflect
       session-state changes to all nodes within 5 seconds of commit, measured
       at p99 under normal load."
```

### 7. Wishful thinking

**Tell**: impossible targets — *"zero latency"*, *"infinite concurrent users"*, *"100% uptime"*, *"never lose data"*. These are aspirations, not requirements.

**Recipe**: replace with bounded, achievable targets. Cite the source of the aspiration in rationale (often it is leadership messaging, not an actual requirement).

```
BAD:  "The system shall provide zero-latency response to all user requests."
GOOD: Apply the NFR five-element rule with an honest target.
```

### 8. Ambiguous pronoun or reference

**Tell**: *it*, *that*, *this*, *they* without a clear antecedent in the statement; *appropriate*, *suitable*, *correct* without specifying against what.

**Recipe**: rewrite with explicit referents. Every noun phrase names exactly one thing.

```
BAD:  "When the request arrives, the system shall validate it and return an
       appropriate response."
GOOD: "When a session-validation request arrives at the API gateway, the
       session service shall verify the token's signature, expiry, and IDLE
       status, and shall return a JSON response per the schema in REQ-NNN."
```

### 9. TBD/TBR/TODO in a baselined requirement

**Tell**: literal "TBD", "TBR", "TODO", "[?]", or any placeholder text inside a requirement that has been declared baselined or active.

**Recipe**: either resolve the placeholder before baselining, or remove the requirement from the active set and mark it `status: draft` until the placeholder is filled.

## AI-era and retrofit (seven)

### 10. Fabricated rationale

**Tell**: perfect-grammar rationales on retrofit requirements for which no preserved conversation or document exists; rationales that never say "because the original team chose X over Y" — only "because X is better than Y" (which is reasoning, not recall); rationales using generic phrases like "industry-standard", "balances X with Y", "best practice".

**Recipe**: replace with `rationale: <pending — requires human input>` or `rationale: unknown` per the no-fabrication rule. Surface the gap; do not paper over it.

### 11. EARS cargo-culting

**Tell**: statements that obey EARS grammar with no underlying discipline — vague triggers, vague responses, glossary-free terms.

```
BAD:  "When the system experiences load, the system shall respond appropriately."
      Structurally event-driven; semantically vacuous. "Load" is undefined,
      "appropriately" is unmeasurable.
```

**Recipe**: do not rewrite to "better EARS" — that hides the problem. Force the box test (see `statement-quality.md`). If the author cannot sketch the test from the statement plus glossary alone, the statement does not yet say enough.

### 12. Requirements smuggling design

**Tell**: a generated requirement contains an implementation choice disguised as behaviour, often with named technologies sticky from training priors. *"The system shall store session state in Redis with 5-second TTL."*

**Recipe**: same as anti-pattern 6. Specifically audit AI-generated text for named technologies, named data structures, and named algorithms.

### 13. Code-driven requirement fabrication

**Tell**: requirements generated by reading existing code that describe whatever the code happens to do, including bugs and accidental features. Rationale then justifies the bugs as intentional.

**Recipe**: in retrofit mode, behaviour observable from code is captured with `recovery_status: reconstructed`; intent behind the behaviour is marked `unknown` until a human supplies it. Distinguish sharply between *what the code does* (reconstructable) and *what the system was meant to do* (only verifiable from preserved decision records).

### 14. Level confusion

**Tell**: a single document mixes statements at different scopes — stakeholder need ("the finance team wants fewer errors"), system requirement ("the system shall …"), and subsystem/component requirement ("the reconciliation job shall …") — without distinction.

**Recipe**: enforce the scope axis explicitly. Every requirement's scope must match this artifact's scope. Parent-scope concerns are upstream `derived_from` references, not requirements here. Child-scope concerns belong in the child's document.

### 15. Test-as-requirement inversion

**Tell**: a characterisation test written against existing behaviour is promoted to "the requirement", closing the loop with no stakeholder in it. Rationale becomes circular: the requirement exists because the test exists.

**Recipe**: separate the layers. Behaviour observed from code is a candidate requirement; promotion to a baselined requirement requires a human confirming that this behaviour is intended, not accidental.

### 16. Laundering the current state

**Tell**: requirements that exactly describe the deployed system, with rationale that defends the current design as if it were the only possible choice. No requirement ever deviates from what the code does; no rationale ever cites a rejected alternative.

**Recipe**: in retrofit, the document is not a description of the deployed system — it is a hypothesis about what the deployed system was meant to do. When the two diverge, the divergence is itself a finding (a defect in the code? a defect in the requirement? worth investigating). Allow the gap to surface; do not hide it by editing the requirement to match the code.

## Sweep order

When sweeping a draft, work through the catalog top to bottom. Patterns 1–9 have mechanical tells (regex / structural); a single pass with grep-style attention catches them. Patterns 10, 13, 16 require reading the document end-to-end with a designer or tester perspective, and benefit from a fresh reader.
