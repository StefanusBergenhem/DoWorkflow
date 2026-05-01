# Constraints and glossary

**Contents**
- [Glossary discipline](#glossary-discipline) — rules, slot-fill, example
- [Inherited constraints](#inherited-constraints) — rules, categories, slot-fill, example
- [Cross-checks](#cross-checks)

---

Glossary first; one word per concept; check every domain term resolves before authoring requirements.

## Glossary discipline

### Rules

1. **One word per concept, one concept per word** — within this scope.
2. **Terms are model-anchored** — they name domain concepts, not implementation. *Session* is a domain concept; *SessionRow* is an implementation detail and does not belong here.
3. **Generic placeholder terms are audited out** — *Manager*, *Service*, *Handler*, *Record*, *Processor* — unless they name an actual domain concept.

### Slot-fill template

```yaml
glossary:
  - term: <one canonical term>
    definition: |
      <single meaning, complete sentence; mention what it is and what bounds it>
    distinct_from: <optional — names of similar concepts and how they differ>
    model_refs: <optional — entity, value-object, or aggregate names from the domain model>
```

### Example

```yaml
glossary:
  - term: "Session"
    definition: |
      A server-side record of an authenticated user's continuous interaction
      with the system, scoped to a single device and identified by an opaque
      session token. Created on successful authentication; destroyed on
      logout, idle timeout, or explicit invalidation.
    distinct_from: "Elevated session — a session that has additionally completed step-up auth."
    model_refs: ["Session entity in the Identity bounded context"]

  - term: "Idle timeout"
    definition: |
      A session invariant: the maximum duration between the end of one
      authenticated request on the session and the start of the next, before
      the session is destroyed.
    distinct_from: "Absolute timeout."

  - term: "Absolute timeout"
    definition: |
      A session invariant: the maximum duration from session creation to
      forced destruction, independent of activity.

  - term: "Elevated session"
    definition: |
      A session that has completed step-up authentication within the last
      N minutes, eligible to perform security-sensitive operations.
    distinct_from: "Session — without the elevation marker."
```

## Inherited constraints

Constraints are distinct from requirements. A requirement specifies behaviour the system must exhibit (testable by observing the system); a constraint bounds the space of acceptable solutions (verifiable often by inspection, process, or architecture review rather than by runtime test).

### Rules

1. **Every constraint cites its source.** "Must use Java 17" is a constraint; who imposed it and why? An ADR, a customer contract, a platform-team policy? A constraint without a source is a preference pretending to be a rule.
2. **Every constraint names the cost of relaxing it.** If the constraint could be relaxed at no cost, it is not really a constraint. Naming the cost is what lets a future review reopen the constraint when circumstances change.
3. **Constraints with downstream behavioural consequences propagate as derived requirements.** "Must use Java 17" alone changes nothing at the behavioural layer; "Must use a specific datastore" combined with the system's latency target may imply derived requirements about query patterns.
4. **Constraints are categorised** for review hygiene.

### Categories

| Category | Examples |
|---|---|
| **Technical** | Mandated platforms, frameworks, protocols |
| **Regulatory / legal** | GDPR, HIPAA, contractual obligations |
| **Organisational** | Company-wide policies, coding standards, procurement rules |
| **Financial** | Infrastructure budget ceilings, vendor cost caps |
| **Temporal** | Hard launch dates, freeze windows, audit windows |

Categorising helps reviewers spot under-represented categories — most teams over-list technical and under-list regulatory.

### Slot-fill template

```yaml
inherited_constraints:
  - id: IC-NNN
    source: <citation — ADR, regulation, contract, policy>
    summary: <what the constraint requires, in one or two sentences>
    category: <technical | regulatory | organisational | financial | temporal>
    cost_of_relaxing: |
      <what specifically would break if this were lifted — e.g. "customer-wide
       re-certification", "audit finding with material fines", "supply-chain
       blockage", "loss of platform support">
    derived_requirements: [<list of REQ-IDs derived from this constraint, if any>]
```

### Example

```yaml
inherited_constraints:
  - id: IC-001
    source: "GDPR Article 17 (Right to erasure), Regulation (EU) 2016/679"
    summary: |
      Data subjects may request erasure of their personal data without undue
      delay, subject to Article 17(3) exceptions.
    category: regulatory
    cost_of_relaxing: |
      Data Processing Agreement breach with all EU customers, supervisory
      authority intervention, material fines up to 4% of global turnover.
    derived_requirements: [REQ-050, REQ-051, REQ-052]
```

## Cross-checks

Before delivering, run these:

- Every term used in a requirement statement appears in the Glossary
- Every Glossary term is used by at least one requirement (otherwise: dead term, remove or note why retained)
- Every inherited constraint with behavioural consequences has at least one `derived_requirement` link
- Every derived requirement (any type) cites the constraint or governing decision in its `derived_from`
