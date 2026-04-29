# Gap-finding

Active gap-finding is the discipline of raising categories the stakeholder has not volunteered. Stakeholders typically describe what they care about most; a competent architect notices what they have not yet said and probes those gaps before the elicitation session ends.

## The six gap categories

### 1. Interfaces

- What it covers: who or what does the system talk to — humans, other systems, devices, external services.
- Why it matters: every interface is a contract that downstream design must honour. Missed interfaces become rework when implementation discovers them.
- Trigger question: "Beyond the people who use the system, what other systems does this need to send things to or receive things from? Are there any external services, partner APIs, files dropped into a folder, sensor inputs, or integrations with internal tools?"

### 2. Non-functional requirements

- What it covers: at minimum availability, latency, throughput, security. Often also reliability, observability, recoverability, maintainability.
- Why it matters: NFRs are usually under-specified by stakeholders because they feel like architect concerns — but they are stakeholder needs about how the system feels and behaves under pressure.
- Trigger question: "Beyond what the system does, let's talk about how it has to perform. How fast does it need to feel? How many users at peak? How much downtime is OK? What's the worst that could happen if the data is exposed?" (See `explanation-while-eliciting.md` for per-NFR translations.)

### 3. Exception flows / error paths

- What it covers: behaviour when things go wrong — invalid input, upstream system unavailable, network partition, partial failure mid-operation.
- Why it matters: stakeholders typically describe the happy path. The unhappy paths often dominate operational reality and shape downstream design more than the happy path does.
- Trigger question: "Walk me through what should happen when things go wrong. If a user submits something invalid — what do they see? If `<dependency>` is down — does the system wait, fail, or give a degraded answer? If something fails halfway through, can the user recover, or do they have to start over?"

### 4. Success metrics

- What it covers: how the stakeholder will know the system is doing its job in production.
- Why it matters: needs without success metrics are hopes. Metrics make needs falsifiable and give downstream design something to optimise for.
- Trigger question: "How will we know this is working once it's in production? What number, dashboard, or signal would make you say `yes, this is succeeding`? And what would make you say `no, this is broken — we need to fix it`?"

### 5. Constraints

- What it covers: regulatory, contractual, organisational, technical bounds the system must operate inside.
- Why it matters: constraints are often non-negotiable and frame the design space. Discovering a constraint late is expensive.
- Trigger question: "Are there any rules — from law, your auditor, an industry body, a customer contract, an internal policy, or operations — that the system has to follow? Anything about where data can live, who has to approve changes, what languages or runtimes are allowed in your environment, or what we can and can't depend on?"

### 6. Ops / lifecycle

- What it covers: install, configure, update, monitor, retire.
- Why it matters: a system that works once does not necessarily work for years. Lifecycle needs shape architecture deeply.
- Trigger question: "How do you imagine this gets installed? Updated? Monitored? Eventually retired? Is there an ops team, or does the user run it themselves? When something goes wrong in production, who is on the hook to fix it, and what do they need to do their job?"

## The relevance heuristic

Not every category fits every project. A small internal tool with three users does not need an availability SLA conversation; a payment system does. The skill should:

- Form a quick judgement at session start about which categories are plausibly project-relevant.
- Raise relevant categories during elicitation, paced so the stakeholder isn't flooded.
- For categories judged not relevant, raise a single confirming question ("I don't think `<category>` matters here because `<reason>` — agreed?") and let it die quickly with stakeholder confirmation.
- For every category that *is* relevant, ensure it surfaces at least once before the session terminates.

## The discipline

Gap-finding is not a flat questionnaire. It is a sweep — done with judgement, paced through the session, and concluded with explicit "deferred" or "not relevant" markers in `needs.md` for any category the stakeholder cannot or will not address. A `needs.md` that goes to authoring with all six categories silently absent is a failure of the discipline; a `needs.md` that explicitly defers four of them is fine.
