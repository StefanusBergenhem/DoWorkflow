# Bad session — skipped readback

A counter-example showing how Hard Refusal #1 (never commit without an explicit confirmation token) gets violated. The elicitation itself is reasonable — the failure is in the readback / confirm contract.

## Input — short stakeholder conversation

The stakeholder is a product owner for a small internal incident-tracker tool.

**Stakeholder (opening):**
> We need a way for our team to track production incidents. About 8 engineers. Should let us log an incident when one happens, write notes as we investigate, and mark it resolved when done. Notifications would be nice — Slack maybe — but not a must-have for v1.

## What the model did wrong

### Turn 1 — model elicits, then commits without readback

**Model:**
> Got it. A few quick questions: how do incidents get assigned — round-robin, or whoever picks one up first? And do you want a history view, or just the active list?

**Stakeholder:**
> Whoever picks one up first claims it. History is useful — let's say last 90 days, then archive.

**Model:**
> Perfect, I have what I need. Writing it up now.
>
> ```markdown
> ## Functional needs
> - The team can log a new incident with a title and a description. *Confirmed: yes.*
> - Any team member can claim an unassigned incident. *Confirmed: yes.*
> - The team can write progress notes on an active incident. *Confirmed: yes.*
> - The team can mark an incident resolved. *Confirmed: yes.*
> - Active incidents and incidents resolved within the last 90 days are visible; older incidents are archived. *Confirmed: yes.*
> ```

<!-- BAD: Hard Refusal #1 violated. The model committed five entries with `*Confirmed: yes.*` without ever reading anything back to the stakeholder. The stakeholder said `whoever picks one up first claims it` — that's elicitation material, not a confirmation token for any specific drafted entry. The drafted text the model committed has wording the stakeholder never saw, let alone agreed to. -->

### Turn 2 — variant: soft-confirmation accepted as `yes`

A second variant of the failure. After Turn 1 elicitation, the model does readback — but accepts a soft response.

**Model:**
> **Readback — incident lifecycle**
>
> What I heard you say:
> > Engineers claim incidents on a first-come basis, log progress notes, and mark them resolved. Incidents resolved within the last 90 days are visible; older are archived.
>
> What I'm proposing to write:
> > **Functional needs:** Engineers claim unassigned incidents on a first-come-first-served basis. Any engineer can post progress notes on an active incident and mark it resolved. The active list and incidents resolved within the last 90 days are visible to all engineers; older incidents are archived.
>
> Is this right? Please reply with one of:
> - **yes** — I'll commit as-is.
> - **no — change X**
> - **almost — also Y**

**Stakeholder:**
> ok

**Model:**
> Great, committed.

<!-- BAD: Hard Refusal #1 violated again, this time via Failure Mode 2 from readback-protocol.md (soft confirmation acceptance). "ok" is not in the three accepted response shapes. The stakeholder may have been acknowledging hearing the readback, not agreeing with it. -->

## What the model should have done

### Corrected Turn 1 — readback after elicitation

After the elicitation, the model drafts internally (DRAFT state) and then issues a readback (READBACK state):

**Corrected model turn:**
> **Readback — incident lifecycle**
>
> What I heard you say:
> > Engineers claim incidents on a first-come basis, log progress notes, and mark them resolved. Incidents resolved within the last 90 days are visible; older are archived.
>
> What I'm proposing to write:
> > **Functional needs:** Engineers claim unassigned incidents on a first-come-first-served basis. Any engineer can post progress notes on an active incident and mark it resolved. The active list and incidents resolved within the last 90 days are visible to all engineers; older incidents are archived.
>
> Is this right? Please reply with one of:
> - **yes** — I'll commit as-is.
> - **no — change X**
> - **almost — also Y**
> (Ambiguous replies like "ok" or "sure" — I'll treat as a request to keep elicitating, not a confirmation.)

### Corrected Turn 2 — re-prompt on soft confirmation

If the stakeholder replies "ok", the model re-prompts:

**Corrected model turn:**
> Just to be sure — was that a yes, or do you want me to keep working on the wording? I want to be precise here because what I commit is what downstream design will work from. Could you reply with one of:
> - **yes** — commit as-is
> - **no — change X**
> - **almost — also Y**

If the stakeholder still gives an ambiguous response, treat it as continued elicitation: ask what specifically about the draft is not landing right. Do not commit.

## Why this matters

The bad session committed five entries the stakeholder never explicitly agreed with. The wording — phrases like "first-come-first-served", "active list", "all engineers" — is the model's wording, not the stakeholder's. Those phrases will travel into downstream design as if the stakeholder vouched for them. By the time the discrepancy surfaces, it's expensive to fix.

The discipline is: the exact text confirmed is the exact text committed. Soft confirmation is not confirmation. If the readback chain breaks at any point, restart from DRAFT.
