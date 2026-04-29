# Good needs.md output

A well-formed prototype-mode `needs.md` for a small but plausible project: a small internal incident-tracker tool for an 8-engineer team. Stakeholder voice throughout, no design language, every entry traced to a confirmation token, deferred gaps explicit.

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

## Intent

The platform team needs a shared place to track production incidents from detection through resolution. The aim is faster, more consistent incident response: a clear active list, a visible record of what's been tried, and a low-friction way to update the team and the rest of the company. Success means engineers reach for this tool first when an alert fires, instead of falling back to ad-hoc chat threads. *Confirmed: yes.*

## Stakeholders

- **On-call engineers (8 in the platform team)** — log incidents, claim them, post progress, mark them resolved. *Confirmed: yes.*
- **Engineering leadership (3 managers)** — see the active list and recent history at a glance to know what's burning and what's settled. *Confirmed: yes.*
- **The wider engineering org (~80 people)** — read-only awareness of active incidents that might affect their work. *Confirmed: yes.*

## Needs

### Functional needs

- Any platform engineer can log a new incident with a title, severity, and a free-form description. *Confirmed: yes.*
- Any platform engineer can claim an unassigned incident, post progress notes on it, and mark it resolved. *Confirmed: yes.*
- The active list is the default landing view; resolved incidents from the past 90 days are accessible via a "recent history" view. *Confirmed: yes.*
- Incidents older than 90 days are archived — out of the day-to-day views but retrievable on request. *Confirmed: yes.*

### Quality needs

- Logging a new incident from the home view takes no more than 15 seconds of typing for an experienced user. *Confirmed: yes.*
- The active list updates within 10 seconds when a teammate posts a note or changes status. *Confirmed: yes.*
- The tool is reachable during an active incident at least as reliably as the company's primary email — outages of more than 30 minutes during business hours are treated as severe regressions. *Confirmed: yes.*

### Constraints

- Sign-in must use the company's existing identity provider — no separate accounts. *Confirmed: yes.*
- Hosting must be inside the company's existing approved infrastructure footprint — no third-party SaaS. *Confirmed: yes.*

### Interfaces / integrations

- The tool posts a one-line message to the platform team's chat channel when an incident is logged and when it is resolved. The format and frequency are tuneable later. *Confirmed: yes.*

## Open gaps

- **Mobile experience** — *Status:* deferred. The team operates from laptops; mobile-friendly views would be nice but are out of scope for v1.
- **Post-incident review tooling** — *Status:* pending-stakeholder-input. Stakeholder will check with engineering leadership on whether the tool should generate a post-incident-review template, or whether that workflow stays in the existing wiki.

## Success metrics

- Engineers report reaching for this tool first when an alert fires (measured by quarterly survey of the platform team). *Confirmed: yes.*
- Median time-to-claim for new incidents during business hours is under 5 minutes. *Confirmed: yes.*

## Session notes

- Regulatory category — confirmed not relevant for this scope (internal tool, no regulated data).
- Recoverability — minimal: incident records are nice-to-keep but not load-bearing legal artefacts; if the tool loses up to a day's worth on rare incidents, the team will reconstruct from chat. *Confirmed via readback at session end.*
- Throughput / scale — 8 engineers, peak ~5 active incidents simultaneously, growth not expected past ~12 engineers in the next year. *Confirmed via readback at session end.*
```
