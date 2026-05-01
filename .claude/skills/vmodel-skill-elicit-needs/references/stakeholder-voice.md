# Stakeholder voice

Capture needs in user-realm vocabulary (what / for whom / under what conditions / what good looks like). Design language belongs in downstream artifacts (architecture, ADRs, detailed design), not in `needs.md`.

## Tells of design smuggling

When stakeholder material contains any of the following, design has been smuggled in:

- **Named technologies** — "we'll use Postgres", "the queue will be Kafka", "it'll run on Kubernetes"
- **Named libraries / frameworks** — "it'll be a React app", "we'll use Spring Boot", "via the gRPC client"
- **Named algorithms** — "use Raft for consensus", "AES-256 for the at-rest encryption", "SHA-256 hashing"
- **Specific data shapes** — "a JSON column with these five fields", "a Mongo document with subkeys", "a Parquet file with these columns"
- **Specific UI patterns** — "a left nav with these tabs", "a Material card layout", "a kanban board with three columns"
- **Specific protocol versions** — "REST with OAuth2", "GraphQL over websockets", "HTTP/2 with mTLS"
- **Specific architectural patterns** — "event-sourced", "microservice per bounded context", "CQRS"
- **Implementation verbs** — "save to database", "send via webhook", "render in browser"

## The probe pattern

When the stakeholder uses design language, the response is not to silently strip it out and capture only the surrounding language. The response is to probe for the underlying need.

**The probe:**
> "What would `<choice>` give you that other options wouldn't? If a different `<technology / framework / pattern>` met the same goal, would that be acceptable?"

The stakeholder's answer to the probe is what `needs.md` captures — not the original choice.

## Worked examples

### Example 1 — named technology

- **Stakeholder said:** "We'll use Postgres for storage."
- **Probe:** "What would Postgres give you that other options wouldn't?"
- **Stakeholder answer:** "I want to be able to ad-hoc query the data with SQL when something goes wrong, and I want strong consistency on financial fields."
- **Captured in needs.md:** "Operators can run ad-hoc SQL-style queries against the data when investigating issues. *Confirmed: yes.*" and "Financial-field updates are strongly consistent — once an update is acknowledged, every subsequent read sees it. *Confirmed: yes.*"

### Example 2 — named UI pattern

- **Stakeholder said:** "It'll be a React app with a left nav and tabs."
- **Probe:** "What's the user experience you're picturing? What does the left nav give them?"
- **Stakeholder answer:** "I want users to switch between three top-level tasks without losing their place in any of them, and the navigation needs to feel native and fast — under 100 ms response time."
- **Captured in needs.md:** "Users can switch between three top-level tasks without losing in-task state. *Confirmed: yes.*" and "Top-level navigation feels immediate to the user — under 100 ms perceived response. *Confirmed: yes.*"

### Example 3 — named algorithm

- **Stakeholder said:** "Use AES-256 for the at-rest encryption."
- **Probe:** "What's the threat you're protecting against, and is AES-256 a hard requirement (e.g., from a contract or auditor) or your best-guess for the threat model?"
- **Stakeholder answer:** "Our security auditor told us we need at-rest encryption that meets FIPS-140-2 — AES-256 was their suggestion."
- **Captured in needs.md:** "At-rest data is encrypted to a level that meets FIPS-140-2 (mandated by the security auditor). *Confirmed: yes.*"

The standard FIPS-140-2 is regulatory and is captured at the needs layer; AES-256 as the specific cipher is a design choice that downstream artifacts make.

## Edge case — the stakeholder insists on the design term

If the stakeholder insists they really want Postgres specifically (e.g., "we have a contract with our DB vendor", or "the rest of the platform is on Postgres and operations refuses to add another database"), that is not a need — it is an inherited constraint. Capture it under the constraints section, with the reason explicit.

> "The data store must be Postgres (organisational constraint — operations does not support adding another database)."

This is fine in `needs.md` as long as the reason is in the entry. A bare "use Postgres" with no reason is design smuggling; a "must be Postgres because operations" is a constraint.

## Rule

`needs.md` captures the answer to the probe, not the original design choice. When the stakeholder insists, capture as a constraint with reason. Bare design language never survives into `needs.md`.
