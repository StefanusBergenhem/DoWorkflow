# Explanation while eliciting

When raising an architect concept (NFRs, edge cases, integrations, error paths, scale, regulatory) in a question, translate it into stakeholder language in the same turn before asking. Do not assume the stakeholder will Google the term mid-session.

## Architect-concept trigger list

For each concept: a short architect framing, the stakeholder-facing translation pattern, and a heuristic for when to raise it.

### Availability / reliability

- Architect concept: what fraction of the time the system is reachable / functioning correctly under defined conditions.
- Stakeholder-facing translation: "What should happen if the system is unreachable? Is it OK if it's down for 10 minutes once a quarter? What about for 4 hours once a year? What's the worst-case downtime where you would consider this a failure?"
- Raise when: the system is operationally consumed by a stakeholder, an integration partner, or paying customers.

### Latency

- Architect concept: time from a triggering event to a response observable to the user.
- Stakeholder-facing translation: "How fast does this need to feel to the user? When is a delay long enough that a user would notice and complain? What's the worst experience you'd accept on a slow day?"
- Raise when: there is interactive use, real-time control, or any user-perceptible action chain.

### Throughput / scale

- Architect concept: number of operations per unit time the system must sustain.
- Stakeholder-facing translation: "How many people will use this on day 1? Day 100? Day 1000? Are there bursts — month-end, sales events, cron-triggered backfills — where the load looks very different from a quiet day?"
- Raise when: the load is non-trivial or growth is expected. Also for clearly small / single-user tools, raise once and let it die quickly if the answer is "tiny".

### Security

- Architect concept: confidentiality, integrity, authentication, authorisation, and audit posture.
- Stakeholder-facing translation: "What's the worst that could happen if this data falls into the wrong hands? Who is allowed to read it? Who is allowed to change it? Are there rules from outside (auditor, regulator, customer contract) that govern how this is protected?"
- Raise when: the system stores, processes, or transmits any data with non-zero sensitivity, or whenever the system has any external interface.

### Observability

- Architect concept: ability to know what the system is doing in production from logs, metrics, traces.
- Stakeholder-facing translation: "When something goes wrong, who needs to know, and how soon? Is there a person on call? What information do they need to figure out what happened — for a typical user issue, and for a system-wide incident?"
- Raise when: the system has any operational consumer.

### Recoverability

- Architect concept: ability to restore service or data after a failure.
- Stakeholder-facing translation: "If this loses data, what would a good recovery look like? How much loss is acceptable — a minute's worth, a day's worth, none? How fast does it need to come back? Is there a manual fallback if the system is unavailable?"
- Raise when: the system holds state that cannot be reconstructed from elsewhere.

### Edge cases

- Architect concept: input or state combinations outside the happy path.
- Stakeholder-facing translation: "What should happen if `<specific weird input>`? For example: an empty input, an input twice the expected size, a duplicate of one already submitted, an action triggered while a previous one is still in progress."
- Raise when: drafting any functional need that has obvious boundary conditions.

### Integrations

- Architect concept: external systems the target system depends on or feeds into.
- Stakeholder-facing translation: "What other systems does this need to talk to? What does it send out? What does it pull in? Are any of those systems run by another team or another company?"
- Raise when: the stakeholder hasn't volunteered any integrations yet, or when a need implies one indirectly.

### Error paths

- Architect concept: behaviour when an upstream dependency fails or returns an error.
- Stakeholder-facing translation: "What should happen if `<upstream system>` is down? Should the user wait, see an error, get a degraded version, or queue and retry later? Who should be notified if the dependency stays down?"
- Raise when: any integration is named — error path is the half of the integration story that stakeholders typically forget.

### Ops / lifecycle

- Architect concept: install, configure, update, monitor, retire.
- Stakeholder-facing translation: "How do you imagine this gets installed and updated? Is there an operations team that runs it, or does the user install it themselves? What about retiring it — what's the plan when it's no longer needed?"
- Raise when: not in early discovery — bring in once the core flows are stable, since lifecycle is often shaped by the flows.

### Regulatory

- Architect concept: external rules the system must obey (legal, audit, industry standards, contracts).
- Stakeholder-facing translation: "Are there rules or standards this has to follow — anything from law, your auditor, an industry body, or a contract with a customer? Any data residency requirements, data retention rules, or audit logging expectations?"
- Raise when: the project domain has any plausible regulatory dimension (finance, health, safety, aviation, automotive, government, EU users, paid customers).

## The rule

When using an architect term in a question, translate it first. Untranslated, the stakeholder cannot answer.
