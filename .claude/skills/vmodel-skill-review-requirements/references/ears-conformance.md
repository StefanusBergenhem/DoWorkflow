# EARS conformance — checks

Apply EARS five-pattern conformance. Emit findings per checks below. NFRs use `nfr-five-elements-checks.md`; interfaces use `interface-five-dimensions-checks.md`.

## The five patterns

| Pattern | Template |
|---|---|
| **Ubiquitous** | *The \<system> shall \<response>.* |
| **Event-driven** | *When \<trigger>, the \<system> shall \<response>.* |
| **State-driven** | *While \<state>, the \<system> shall \<response>.* |
| **Optional-feature** | *Where \<feature>, the \<system> shall \<response>.* |
| **Unwanted-behaviour** | *If \<condition>, then the \<system> shall \<response>.* |

## Conformance checks

Walk every functional / unwanted-behaviour requirement statement through these checks:

### Check 1 — Pattern match

Does the statement match exactly one of the five patterns? A statement that does not match any pattern is suspect — either it is mis-typed (probably an NFR, interface, data, or constraint) or it is malformed.

- **check_failed**: `check.ears.invalid-pattern`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement + indicate which pattern keywords are missing or malformed
- **recommended_action**: *"Re-classify into one of the five EARS patterns, or reclassify the requirement type."*

### Check 2 — Compound keyword limit

Count the EARS keywords (Where, While, When, If/then) in the statement. Maximum 2.

- **check_failed**: `check.ears.compound-too-many-keywords`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement, name the keywords found
- **recommended_action**: *"Split into two or more atomic statements per the EARS compound-limit rule."*

### Check 3 — Canonical compound order

Compound keywords must appear in canonical order: **Where → While → When → If/then → shall**. Out-of-order compounds make the precondition / trigger ambiguous.

- **check_failed**: `check.ears.compound-out-of-order`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement, name the order found vs canonical
- **recommended_action**: *"Re-order the compound keywords; if re-ordering is unnatural, the statement is probably hiding two requirements — split it."*

### Check 4 — One `shall`

Exactly one `shall` per atomic statement. Two `shall`s = compound requirement (anti-pattern 1) — see `anti-patterns-catalog.md`.

- **check_failed**: `anti-pattern.compound`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement, point to both `shall`s
- **recommended_action**: *"Split into atomic statements per the EARS one-shall rule."*

### Check 5 — Cargo-culting (semantic emptiness despite valid grammar)

The statement matches a pattern syntactically but is semantically vacuous: vague trigger, vague response, glossary-free terms, wishful adjectives.

Tells:
- *"When the system experiences load, the system shall respond appropriately."* — vague trigger ("experiences load"), vague response ("respond appropriately")
- *"If an error occurs, the system shall handle it gracefully."* — vague condition ("error occurs"), vague response ("handle it gracefully")
- *"While the system is operating, the system shall maintain reasonable performance."* — vague state, vague metric

The pattern is right, the discipline is missing.

- **check_failed**: `anti-pattern.ears-cargo-cult`
- **severity**: `soft_reject`
- **evidence shape**: quote the statement, name the vague tokens
- **recommended_action**: *"Re-author with concrete trigger / state / response. Apply the box test from `statement-quality-checks.md`."*

Do not "rewrite to better EARS" — the cargo-cult problem is not solvable by template-shaping. The author needs to pass the box test.

## When EARS is not the right tool

Default: expect EARS. Escape hatch: accept Planguage syntax for tiered NFR targets — emit no finding. For other forms (GWT, narrative), emit `check.ears.non-conformant`.

| Type | Accepted form |
|---|---|
| NFR | Planguage; or five-element flat |
| Interface | Five-dimensions form |
| GWT in `acceptance` block | Allowed only there, not in `statement` |

- **check_failed**: `check.ears.mixed-structured-forms`
- **severity**: `soft_reject`
- **recommended_action**: *"Use one structured form per statement; place GWT in the `acceptance` block, not in the `statement` field."*
