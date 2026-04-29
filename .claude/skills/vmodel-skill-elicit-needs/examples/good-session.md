# Good session — happy path

A worked happy-path elicitation. The stakeholder is the head of the marketing team at a mid-size company. They asked to build "a small internal tool for sharing campaign assets" and produced a handful of scattered notes.

## Input — scattered notes from a discovery workshop

```
- need a place to put PDFs and images for campaigns
- people on team should upload, share with agency
- some files are big (videos)
- need to know who downloaded what (audit)
- works on laptop and phone
- can't use Dropbox - finance won't approve another vendor
- live next quarter
- maybe versioning?
```

## Session turns

### Turn 1 — opening (state: ELICIT)

**Model:**
> I read your notes. Before I dig in, can I check the rough shape of the project with you? It looks like a file-sharing tool for the marketing team and an external agency, with audit on downloads, mobile access, and a finance constraint that rules out third-party SaaS. Live next quarter. Did I get the rough shape right, or am I miscutting it?

**Stakeholder:**
> Yeah that's right. It's mostly the team and one agency, maybe two later. And finance is firm on the SaaS thing — we already lost that argument with Dropbox.

<!-- ANNOTATION: opening clusters the scattered notes (input-intake.md, scattered notes path) and verifies the cut before deepening. -->

### Turn 2 — anti-assumption on a plural-without-count (state: ELICIT)

**Model:**
> **Clarification needed — team and agency size**
>
> Expected: I would naturally assume a marketing team of ~10 people and one agency partner of ~5 people, since you mentioned "the team" and "one agency" — total ~15 active users.
>
> Found: Your notes say "people on team" and "share with agency" without numbers.
>
> Why this matters: If we're talking 15 people total it shapes the tool very differently than if we're talking 100. It also changes how concerned we should be about heavy load on launch.
>
> How should I proceed? Please pick one:
> - 10–15 active users total (team + one agency).
> - 30–50 active users (team + multiple agencies once you scale).
> - 100+ active users.
> - Something else — please describe.

**Stakeholder:**
> Option 1 for now — 12 on the team, maybe 5 from the agency. We won't add a second agency until end of next year.

<!-- ANNOTATION: ambiguity tell (plural without count, anti-assumption.md tell #1). Surfaced via the four-line template. The stakeholder picked an option; the model did not silently assume "15 active users". -->

### Turn 3 — explanation-while-eliciting on availability (state: ELICIT)

**Model:**
> Two architect-flavoured questions to get out of the way. Both translated:
>
> **Availability** — what should happen if the tool is unreachable for a stretch? Like, is it OK if it's down for 30 minutes mid-afternoon once a quarter? What about for a whole working day once a year?
>
> **Latency** — how fast does it need to feel to upload and download? When would your team or the agency notice a slowdown and complain?

**Stakeholder:**
> 30 minutes once in a while is fine — it's not like we're a 24/7 trading floor. Whole working day would be a problem. Speed-wise, big PDF (50 MB) needs to download in under 30 seconds on the office network, otherwise people will complain. Upload of a video (300 MB) — they'll start it and walk away, so a few minutes is fine, but I want some kind of progress bar.

<!-- ANNOTATION: explanation-while-eliciting (explanation-while-eliciting.md, availability + latency). Both architect concepts translated into stakeholder language before the question. The stakeholder gave concrete numbers and a progress-bar UX want. -->

### Turn 4 — gap-finding on integrations (state: ELICIT)

**Model:**
> Looking at the gaps — anything to know about other systems? Beyond the people who use the tool, does it need to talk to anything else? An identity system for sign-in, an internal calendar, a CRM, anything that pulls files in or out automatically?

**Stakeholder:**
> Sign-in should use the company SSO — that's a hard ask from IT, otherwise they won't let me run it. Otherwise nothing automated, it's just people uploading and downloading.

<!-- ANNOTATION: gap-finding (gap-finding.md category 1, interfaces). Stakeholder volunteered an SSO constraint that wasn't in the notes; this would have been a silent gap if not surfaced. -->

### Turn 5 — readback + confirm (states: DRAFT, READBACK, CONFIRM)

**Model:**
> Let me readback what I've got so far on the team and access:
>
> **Readback — team and access**
>
> What I heard you say:
> > The marketing team (12 people) and one agency partner (5 people) need to upload PDFs, images, and videos and share them. Sign-in must go through the company SSO — IT requires it. The total active user count won't grow past ~17 in the next year.
>
> What I'm proposing to write:
> > **Stakeholders:** Marketing team (12 internal staff) and one agency partner (5 external collaborators). Active user count is capped at ~17 for the next year.
> >
> > **Constraints:** Sign-in must use the company SSO (mandated by IT — the tool will not be approved for production otherwise).
>
> Is this right? Please reply with one of:
> - **yes** — I'll commit as-is.
> - **no — change X** — tell me what to change and I'll re-readback.
> - **almost — also Y** — tell me what to add or adjust and I'll re-readback.

**Stakeholder:**
> yes

<!-- ANNOTATION: full DRAFT → READBACK → CONFIRM cycle. Explicit `yes` token. The exact text confirmed is the exact text that commits. -->

### Turn 6 — explicit defer of an unraised gap (state: ELICIT, then a minor commit cycle)

**Model:**
> Two more gap categories to raise: regulatory and ops. On regulatory — are there any rules about who can hold campaign assets, where they can live (data residency), or how long they must be kept? On ops — once it's live, who runs it? Is there an internal team that monitors, or is this on you?

**Stakeholder:**
> Regulatory — campaign assets aren't sensitive in any legal sense, no rules I know of. Skip it. Ops — IT will run the infrastructure, but nobody is dedicated to monitoring this specifically. So if it breaks, it breaks until someone notices.

**Model:**
> Got it. I'll readback those two as deferred / not-applicable so they're explicit and don't disappear silently.
>
> **Readback — regulatory and ops**
>
> What I heard you say:
> > No regulatory rules apply. Ops monitoring will be best-effort by IT, with no dedicated owner — issues are noticed by users.
>
> What I'm proposing to write:
> > **Open gaps:** Dedicated production monitoring is not in place — confirmed acceptable; rely on user reports for incidents. (deferred, by stakeholder choice)
> >
> > **Session notes:** Regulatory category — confirmed not relevant for this scope.
>
> yes / no — change X / almost — also Y?

**Stakeholder:**
> yes

<!-- ANNOTATION: gap-finding (gap-finding.md categories 5 + 6). Categories confirmed not-relevant or deferred-by-choice are recorded explicitly, not silently dropped. -->

## Committed needs.md (excerpt)

```markdown
---
artifact_type: needs
scope: <root>
status: prototype-mode
elicitation_session_id: marketing-asset-share-2026-04-27-1
stakeholder: Head of Marketing
date: 2026-04-27
---

# Needs — Marketing Asset Share

## Stakeholders

- **Marketing team (12 internal staff)** — upload, share, and download campaign assets. *Confirmed: yes.*
- **Agency partner (5 external collaborators)** — receive and download campaign assets. *Confirmed: yes.*

## Needs

### Quality needs

- A 50 MB PDF download completes in under 30 seconds on the office network (typical case). *Confirmed: yes.*
- Video uploads of 300 MB show progress to the user during the upload. *Confirmed: yes.*
- The tool is unavailable for at most ~30 minutes per quarter; longer outages are unacceptable. *Confirmed: yes.*

### Constraints

- Sign-in must use the company SSO (IT mandate — production approval depends on this). *Confirmed: yes.*
- The tool runs on infrastructure approved by finance; third-party SaaS file-sharing vendors are not allowed. *Confirmed: yes.*

## Open gaps

- **Dedicated production monitoring** — *Status:* deferred (stakeholder accepts user-report-driven incident detection).

## Session notes

- Regulatory category — confirmed not relevant for this scope.
```

<!-- ANNOTATION: every entry carries `*Confirmed: yes.*`. Open gaps section captures stakeholder-accepted deferrals explicitly. Session notes capture the "not relevant" categories explicitly so they don't appear as silent omissions. -->
