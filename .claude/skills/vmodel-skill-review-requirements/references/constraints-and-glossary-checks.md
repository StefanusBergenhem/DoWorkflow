# Constraints and glossary — checks

**Contents**
- [Glossary checks](#glossary-checks) — checks 1–5
- [Inherited-constraint checks](#inherited-constraint-checks) — checks 6–9
- [Cross-check — Glossary terms vs requirement bodies](#cross-check--glossary-terms-vs-requirement-bodies)

---

Check the glossary exists and every domain term resolves. Flag placeholders, missing entries, multiple words for one concept.

## Glossary checks

### Check 1 — Glossary section present

- **check_failed**: `check.vocabulary.glossary-missing`
- **severity**: `soft_reject`
- **evidence shape**: note that the document has \<N> requirements but no Glossary section
- **recommended_action**: *"Add a Glossary section with one entry per domain term used in the requirements (canonical term, single-meaning definition, optional distinct_from / model_refs)."*

### Check 2 — Term coverage

A "domain term" is a noun or compound noun specific to the system's problem space. Common-English words (*user*, *request*, *time*) are not domain terms. Domain terms are the system's specific concepts (*Session*, *Idle timeout*, *Elevated session*).

- **check_failed**: `check.vocabulary.term-undefined`
- **severity**: `soft_reject`
- **evidence shape**: name the term, quote the requirement using it, note the term is absent from the glossary
- **recommended_action**: *"Add the term to the Glossary with a single-meaning definition. See `constraints-and-glossary-checks.md` Check 2."*

### Check 3 — Generic placeholder terms audited out

Tells: glossary entries or requirement statements using generic terms as if they named domain concepts:
- *Manager* — what does it manage?
- *Service* — every component is a service; this is filler
- *Handler* — what does it handle?
- *Record* — too generic
- *Processor* — what is processed?

These are filler that hides under-defined concepts. The exception is when the term is part of an actual external interface (e.g., "Event Handler" naming a specific contract) or a cited domain pattern.

- **check_failed**: `check.vocabulary.placeholder-term-used`
- **severity**: `soft_reject` (when the term appears in a requirement statement) or `info` (when only in glossary)
- **evidence shape**: name the placeholder term, quote the requirement using it
- **recommended_action**: *"Replace the placeholder term with a concrete domain concept, or remove it. See `constraints-and-glossary-checks.md` Check 3."*

### Check 4 — One word per concept (synonym drift)

Within this scope, each concept should have one canonical term. If two terms are used interchangeably (e.g., "session" and "user-session"; "token" and "session-token"), that is synonym drift.

- **check_failed**: `check.vocabulary.synonym-drift`
- **severity**: `soft_reject`
- **evidence shape**: name both terms, quote requirements using each
- **recommended_action**: *"Pick one canonical term and use it consistently. Update the glossary."*

### Check 5 — Dead glossary terms

Glossary terms that are not used by any requirement are dead. Report as `info` — the author may have authored the term in anticipation of forthcoming requirements, or it may be vestigial.

- **check_failed**: `check.vocabulary.glossary-term-unused`
- **severity**: `info`
- **evidence shape**: name the term, note no requirement uses it
- **recommended_action**: *"Either remove the term, or add a one-line note to the Glossary explaining why it is retained for future use."*

## Inherited-constraint checks

Inherited constraints are bounds imposed from above or outside this scope. They are distinct from requirements (which specify behaviour the system must exhibit). Each constraint requires three pieces of metadata.

### Check 6 — Source citation

Every inherited constraint cites its source — an architectural decision ID, a regulation reference, a contract clause, an organisational policy ID. A constraint without a source is a preference pretending to be a rule.

- **check_failed**: `check.constraints.missing-source`
- **severity**: `soft_reject`
- **evidence shape**: quote the constraint; note the missing `source` field
- **recommended_action**: *"Add the constraint's source citation: ADR ID, regulation reference, contract clause, or organisational policy ID. See `constraints-and-glossary-checks.md` Check 6."*

### Check 7 — Cost-of-relaxing named

Every inherited constraint names what specifically would break if it were lifted. If the constraint could be relaxed at no cost, it is not really a constraint — it is a preference.

Examples of acceptable cost-of-relaxing entries:
- *"Customer-wide re-certification (estimated 6 months)."*
- *"Audit finding under SOC 2 with material remediation cost."*
- *"Loss of platform support; system would need to be rebuilt on a different stack."*

Generic phrases like "would be bad" or "loss of compatibility" without specifics fail this check.

- **check_failed**: `check.constraints.missing-cost-of-relaxing`
- **severity**: `soft_reject`
- **evidence shape**: quote the constraint; note the missing or generic `cost_of_relaxing` field
- **recommended_action**: *"Specify what concrete consequence follows from relaxing the constraint. If the consequence is unclear, the constraint may be a preference, not a constraint."*

### Check 8 — Category present

Every inherited constraint has a category from the canonical set: technical, regulatory, organisational, financial, temporal. Categorising helps reviewers spot under-represented categories — most teams over-list technical and under-list regulatory.

- **check_failed**: `check.constraints.missing-category`
- **severity**: `soft_reject`
- **recommended_action**: *"Categorise the constraint as one of: technical | regulatory | organisational | financial | temporal."*

### Check 9 — Behavioural-consequences propagation

For each inherited constraint with downstream behavioural consequences at this scope, at least one derived requirement should be authored and cross-linked.

Heuristic: if the constraint summary contains action verbs (*shall*, *must*, *enforce*, *prohibit*) that imply observable behaviour, expect at least one entry in the constraint's `derived_requirements` field.

If absent → finding.

- **check_failed**: `check.constraints.no-derived-requirements`
- **severity**: `soft_reject`
- **evidence shape**: quote the constraint summary; note that it implies behaviour but `derived_requirements` is empty
- **recommended_action**: *"Author the derived requirement(s) that translate this constraint into testable behaviour. Cross-link via `derived_requirements`."*

If a constraint has no behavioural consequences (e.g., a temporal launch-window constraint), the empty `derived_requirements` field is acceptable — no finding.

## Cross-check — Glossary terms vs requirement bodies

A consistency cross-check the review skill runs once: extract every domain term used in any requirement statement; compare against the glossary's `term:` keys. Differences are findings:

- Term in statement, not in glossary → `check.vocabulary.term-undefined`
- Term in glossary, not in any statement → `check.vocabulary.glossary-term-unused` (info-only)
