# Anti-patterns catalog — sweep targets

**Contents**
- [Universal nine](#universal-nine) — patterns 1–9 (mechanical tells)
- [AI-era and retrofit (seven)](#ai-era-and-retrofit-seven) — patterns 10–16 (judgement)
- [Sweep order](#sweep-order)
- [Aggregation rule](#aggregation-rule)

---

Sixteen failure modes, each with a tell, a `check_failed` identifier, a severity, and a generic recommended_action. Walk every requirement (and the document as a whole) through this catalog. Every hit becomes a finding.

Patterns 1–9 are universal; patterns 10–16 are AI-era / retrofit-specific. Hard-reject triggers are flagged ★.

## Universal nine

### 1. Compound requirement

- **Tell**: two `shall`s in one statement; "shall X **and** shall Y"; comma-separated list of behaviours.
- **check_failed**: `anti-pattern.compound`
- **severity**: `soft_reject`
- **recommended_action**: *"Split into atomic statements per the EARS one-shall rule."*

### 2. Vague adjective

- **Tell**: *fast, sufficient, appropriate, adequate, user-friendly, robust, scalable, maintainable, efficient, intuitive, performant*. Statement reads approvable but no test distinguishes pass from fail.
- **check_failed**: `anti-pattern.vague-adjective`
- **severity**: `soft_reject`
- **recommended_action**: *"Replace the adjective with a measured quantity (apply NFR five-element rule), or remove the requirement."*

### 3. Passive voice without actor

- **Tell**: *"\<thing> shall be \<verb>ed"* with no named subject — *"Session data shall be validated."*
- **check_failed**: `anti-pattern.passive-no-actor`
- **severity**: `soft_reject`
- **recommended_action**: *"Rewrite with a named subject. Ask: by whom? at what point? against what?"*

### 4. Unbounded list / escape clause

- **Tell**: *"such as X, Y, Z, **etc.**"*, *"**including but not limited to**"*, *"**as appropriate**"*, *"**where applicable**"*, *"and so on"*, *"for example"*.
- **check_failed**: `anti-pattern.unbounded-list`
- **severity**: `soft_reject`
- **recommended_action**: *"Name the complete list, or scope the requirement to a defined set."*

### 5. Unbounded negative requirement

- **Tell**: *"shall not crash"*, *"shall not fail"*, *"shall not be slow"*. Scope is infinite; cannot be exhaustively verified.
- **check_failed**: `anti-pattern.unbounded-negative`
- **severity**: `soft_reject`
- **recommended_action**: *"Rewrite as a positive unwanted-behaviour EARS statement: 'If \<condition>, then the system shall \<response>'."*

### 6. ★ Implementation prescription (design smuggled as requirement)

- **Tell**: a named technology, library, framework, data structure, or algorithm inside a non-interface requirement statement (outside externally imposed protocols).
- **check_failed**: `anti-pattern.implementation-prescription`
- **severity**: `hard_reject` ★
- **recommended_action**: *"Move the design choice to an architectural decision (ADR). Replace here with the behaviour the design choice was meant to ensure."*

### 7. Wishful thinking

- **Tell**: impossible targets — *"zero latency"*, *"infinite concurrent users"*, *"100% uptime"*, *"never lose data"*.
- **check_failed**: `anti-pattern.wishful-thinking`
- **severity**: `soft_reject`
- **recommended_action**: *"Replace with bounded, achievable targets (apply NFR five-element rule)."*

### 8. Ambiguous pronoun or reference

- **Tell**: *it, that, this, they* without a clear antecedent in the statement; *appropriate, suitable, correct* without specifying against what.
- **check_failed**: `anti-pattern.ambiguous-pronoun`
- **severity**: `soft_reject`
- **recommended_action**: *"Rewrite with explicit referents. Every noun phrase names exactly one thing."*

### 9. TBD / TBR / TODO in baselined requirement

- **Tell**: literal "TBD", "TBR", "TODO", "[?]", or any placeholder text inside a requirement that has been declared baselined or active.
- **check_failed**: `anti-pattern.tbd-marker`
- **severity**: `soft_reject`
- **recommended_action**: *"Resolve the placeholder before baselining, or revert the requirement to `status: draft` until the placeholder is filled."*

## AI-era and retrofit (seven)

### 10. ★ Fabricated rationale

- **Tell**: rationale using generic phrases ("industry-standard", "balances X with Y", "best practice") without specific source; rationale that names "consultation with X" when no record exists; rationale that reasons rather than recalls.
- **check_failed**: `anti-pattern.fabricated-rationale`
- **severity**: `hard_reject` ★
- **recommended_action**: *"Replace with `rationale: pending` (greenfield) or `rationale: unknown` (retrofit) and queue a human follow-up. Never paper over absence of recall with reasoning."*

### 11. EARS cargo-culting

- **Tell**: statement obeys EARS grammar but is semantically vacuous — vague trigger, vague response, glossary-free terms. Example: *"When the system experiences load, the system shall respond appropriately."*
- **check_failed**: `anti-pattern.ears-cargo-cult`
- **severity**: `soft_reject`
- **recommended_action**: *"Re-author with concrete trigger / state / response. Apply the box test (`statement-quality-checks.md`)."*

### 12. ★ Requirements smuggling design

- **Tell**: AI-generated requirement contains an implementation choice disguised as behaviour — named technology sticky from training priors. *"The system shall store session state in Redis with 5-second TTL."*
- **check_failed**: `anti-pattern.requirements-smuggling-design`
- **severity**: `hard_reject` ★ (alias of pattern 6)
- **recommended_action**: *"Move named technology to ADR. Replace here with the behaviour."*

### 13. Code-driven requirement fabrication

- **Tell**: in retrofit mode, requirements that describe whatever the code happens to do, including bugs and accidental features. Rationale justifies the bugs as intentional.
- **check_failed**: `anti-pattern.code-driven-fabrication`
- **severity**: `soft_reject`
- **recommended_action**: *"Distinguish *what the code does* (reconstructable as `recovery_status: reconstructed` on the behaviour field) from *what the system was meant to do* (rationale; only `verified` or `unknown`)."*

### 14. Level confusion

- **Tell**: document mixes statements at different scopes — stakeholder need, system requirement, subsystem-component requirement — without distinction.
- **check_failed**: `anti-pattern.level-confusion`
- **severity**: `soft_reject`
- **recommended_action**: *"Move parent-scope concerns to upstream `derived_from` references; move child-scope concerns to child documents."*

### 15. Test-as-requirement inversion

- **Tell**: characterisation test promoted to "the requirement"; rationale is circular ("the system shall do X because test T verifies it").
- **check_failed**: `anti-pattern.test-as-requirement-inversion`
- **severity**: `soft_reject`
- **recommended_action**: *"Separate the layers. Behaviour observed from code is a candidate requirement; promotion requires a human confirming the behaviour is intended, not accidental."*

### 16. Laundering current state

- **Tell**: every requirement exactly describes the deployed system; no rationale ever cites a rejected alternative; rationale defends current design as if it were the only choice.
- **check_failed**: `anti-pattern.laundering-current-state`
- **severity**: `soft_reject`
- **recommended_action**: *"In retrofit, the document is a hypothesis about what the system was meant to do, not a description of what it does. Allow gaps between hypothesis and code to surface."*

## Sweep order

Walk top to bottom. Earlier patterns (1–9) are easier to spot mechanically. Later patterns (10, 13, 16) require judgment and benefit from the document's full context (upstream input, retrofit mode flag, etc.).

## Aggregation rule

When tallying findings: surface multiple hits as separate per-requirement findings (one per requirement). Use the precedence in the catalog header. DESIGN_ISSUE > REJECTED > APPROVED.

For document-wide patterns (laundering, level confusion at the artifact level), use `requirement_id: "GLOBAL"`.
