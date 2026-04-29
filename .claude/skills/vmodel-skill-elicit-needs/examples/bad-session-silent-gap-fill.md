# Bad session — silent gap fill

A counter-example showing how Hard Refusal #2 (never silently fill an ambiguity gap) gets violated. Same input as `good-session.md` (the marketing asset-share scattered notes).

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

## What the model did wrong

### Turn 1 — opening

**Model:**
> Got it, sharing PDFs and images and videos for the marketing team and an agency. Audit on downloads. Mobile access. No Dropbox. Live next quarter. Let me ask a couple of questions and I'll start drafting.

**Stakeholder:**
> Sure.

<!-- BAD: the input contains "people on team" — a plural without count, an ambiguity tell from anti-assumption.md. The model did not surface it. -->

### Turn 2 — model probes file types

**Model:**
> What are the typical file types and sizes? Will videos be the biggest, or do you have other heavy assets like raw photos?

**Stakeholder:**
> Mostly PDFs (proposal decks, ~50 MB), images (~10 MB each), videos (200–400 MB). The videos are the heaviest.

### Turn 3 — model goes silent and commits

**Model:**
> Great, I have enough to draft a first version. Take a look:
>
> ```markdown
> ## Stakeholders
> - Marketing team (~25 internal staff) — upload, share, download campaign assets. *Confirmed: yes.*
> - Agency partners (~10 external collaborators) — receive and download. *Confirmed: yes.*
> ```

<!-- BAD: three discipline failures in this turn:
  (1) The model invented "~25 internal staff" and "~10 external collaborators". The stakeholder never said any number — that is a silent gap fill (Hard Refusal #2).
  (2) The model invented "Agency partners" plural — the notes said "agency" (singular). Silent gap fill again.
  (3) The model wrote `*Confirmed: yes.*` without a readback turn. The stakeholder has not confirmed anything. This is also a violation of Hard Refusal #1 (premature commit) — see bad-session-skipped-readback.md for that failure mode in isolation. -->

## What the model should have done

### Corrected Turn 1 — surface the ambiguity

The model should have detected the plural-without-count tell on first read of the notes ("people on team", "share with agency") and surfaced it before drafting anything:

**Corrected model turn:**
> Before I draft, I want to clarify the user count.
>
> **Clarification needed — team and agency size**
>
> Expected: I would naturally assume a marketing team of ~10 people and one agency partner of ~5 people, since you mentioned "the team" and "share with agency" — total ~15 active users.
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

**Then a normal stakeholder response, a DRAFT, a READBACK, and a CONFIRM** before any commit. (See `good-session.md` Turn 5 for the readback shape.)

## Why this matters

The bad session committed an entry with a fabricated user count to `needs.md`. Downstream design (capacity planning, infrastructure sizing, support model) will use that number. The stakeholder will discover the mismatch only when the system goes live and starts behaving wrong for the real load. The cost of a single surfaced clarification question (one or two turns of back-and-forth) is dramatically lower than the cost of building for the wrong scale.

The discipline is: every detected ambiguity surfaces. Surface count is acceptable. Silent fill is never acceptable.
