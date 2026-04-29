# Bad needs.md output

A counter-example showing three vivid failures in a `needs.md`. Each failure is annotated with the violated discipline and rewritten as it should appear.

```markdown
---
artifact_type: needs
scope: <root>
status: prototype-mode
elicitation_session_id: incident-tracker-2026-04-27-1
stakeholder: Product Owner, Platform Team
date: 2026-04-27
---

# Needs — Internal Incident Tracker

## Needs

### Functional needs

- The system shall use Postgres for storage of all incident records, with JSONB columns for free-form notes and a btree index on the (severity, created_at) pair.

<!-- BAD: Hard Refusal #3 violated — design language smuggled into a need. "Postgres", "JSONB", "btree index", "(severity, created_at) pair" are all design choices.
     - The stakeholder may have wanted strong consistency, ad-hoc query, or specific performance characteristics; those are needs.
     - The technology choice is downstream design's job.
     - Probe pattern: "What would Postgres give you that other options wouldn't?"
     CORRECTED:
     - Operators can run ad-hoc queries against the incident record when investigating issues. *Confirmed: yes.*
     - Once an incident is logged or updated, every subsequent read across the team sees the latest state. *Confirmed: yes.*
-->

- Users should be able to share files quickly.

<!-- BAD: Hard Refusal #2 violated — silent gap fill via unsurfaced ambiguity. Three tells in one sentence:
     - "Users" — plural without count (anti-assumption.md tell #1)
     - "share" — unbounded operation (tell #4)
     - "quickly" — undefined comparative (tell #2)
     - "files" — plural without count, no type/size scope
     The model committed an entry that papers over four ambiguities.
     CORRECTED FLOW: surface each tell with the four-line clarification template before drafting. The corrected entry might be:
     - The 12 marketing team members can send a file (PDF or image, up to 10 MB) to a recipient identified by company email; recipient receives the file in their inbox within 60 seconds. *Confirmed: yes.*
-->

- Logging a new incident from the home view takes no more than 15 seconds of typing for an experienced user.

<!-- BAD: missing `*Confirmed:*` flag. The wording is reasonable, but the confirmation trace is missing — there's no way to tell whether this was readback-confirmed by the stakeholder or quietly added.
     CORRECTED: append the trace, e.g.:
     - Logging a new incident from the home view takes no more than 15 seconds of typing for an experienced user. *Confirmed: yes.*
     If a confirmation token was never received, the entry should not be in this section at all — it belongs under "Open gaps" with a status like `pending-stakeholder-input`.
-->
```

## Why each of these matters

### Failure 1 — design smuggling

The Postgres / JSONB / index entry forecloses architectural decisions that should be made downstream from `needs.md`. Operations may already have a different storage standard; a different store may meet the same actual needs more cheaply; the auditor may have constraints that make the JSONB column problematic. The need is what the system has to do for whom under what conditions; the technology is how design intends to deliver it. Mixing them locks in design before the design has been justified.

### Failure 2 — unsurfaced ambiguity

"Users should be able to share files quickly" passes a casual read but does not pass the stakeholder-voice test. Downstream design will guess: who is "users" (registered? anonymous? signed-in via SSO?), what is "share" (link? attachment? co-editing access?), how big are "files" (50 KB? 500 MB?), what is "quickly" (sub-second? sub-minute?). Each guess is a divergence from what the stakeholder intended. Surfacing the four ambiguities at elicitation time costs four turns of conversation; surfacing them post-design costs a rewrite.

### Failure 3 — missing confirmation flag

The 15-second logging entry is plausible but unmoored. Without `*Confirmed: yes.*` (or an explicit deferred status), there is no trace from the entry back to a readback turn. The author skill consuming this `needs.md` has no way to tell which entries are agreed-upon stakeholder needs and which are model conjecture. Untraceable entries break the load-bearing discipline of this skill.

## The discipline

`needs.md` is a stakeholder-voice record. Every entry: stakeholder language, no design specifics, an explicit confirmation trace. Entries that fail any of these three are not fit for downstream consumption.
