# Readback protocol

Readback-for-joint-agreement is the load-bearing discipline of this skill. Every entry in `needs.md` is preceded by a readback turn and a confirmation turn. If the chain breaks, the entry does not commit.

This is a fragile contract. The format is fixed; the response classification is rule-based; the failure modes are explicit.

## The four-part readback message

The readback message has exactly four parts (matching `templates/readback.md.tmpl`):

1. **Topic** — what we just talked about, in one short phrase. Anchors the readback to a specific thread.
2. **What I heard you say** — paraphrased in stakeholder voice, not architect voice. The paraphrase is what the stakeholder will compare against their own intent.
3. **What I'm proposing to write to needs.md** — the exact candidate Needs entry, prefixed with the section it would land in (e.g., "Quality needs: …" or "Constraint: …"). This is what gets committed if the stakeholder says yes.
4. **Confirmation prompt** — explicit, structured, requesting one of three accepted response shapes.

## The three accepted response shapes

The confirmation prompt asks for one of:

- **`yes`** — commit the proposed entry as-is. Transition to COMMIT.
- **`no — change X`** — reject the draft with a specific edit. Transition back to DRAFT, incorporate the change, re-readback.
- **`almost — also Y`** — accept the bulk and add or adjust. Transition back to DRAFT, incorporate, re-readback.

Anything else is not a confirmation token. The unaccepted responses are a signal that elicitation should continue, not that the entry should commit.

## Unaccepted responses

These are not confirmation tokens. Treat as continued elicitation; do not commit:

- `ok`, `okay`, `OK` — too soft; the stakeholder may be acknowledging hearing the readback, not agreeing with it.
- `sure` — even softer; often a politeness reflex.
- `fine` — sometimes resigned, sometimes engaged; either way, not explicit agreement.
- `yeah`, `yeah whatever`, `whatever` — disengagement; the stakeholder is not committing.
- Silence or no response — must be re-prompted. If still no response after one re-prompt, suspect HALT condition #5 (disengagement).
- Change of subject — the stakeholder has moved on without answering. Bring them back to the readback once. If they do not engage, treat the entry as unconfirmed.
- A monologue that may or may not address the readback — re-prompt with the structured choice; do not interpret freeform text as `yes`.

## Three failure modes the protocol guards against

### Failure mode 1 — premature commit

Writing to `needs.md` without a readback at all. Example: model captures elicitation material, drafts internally, commits to disk. The stakeholder finds out only when they read the file.

Guard: COMMIT may only be entered from CONFIRM with an explicit-confirm token. The state machine forbids ELICIT → COMMIT or DRAFT → COMMIT directly.

### Failure mode 2 — soft confirmation acceptance

Treating `ok`, `sure`, `fine`, etc. as a `yes`. The stakeholder did not actually agree; they were filling silence.

Guard: only the three explicit response shapes count. Soft responses route back to ELICIT.

### Failure mode 3 — implicit modification

After readback, the model edits the candidate entry (for clarity, for the file's overall consistency, for any reason) and commits the edited version without re-reading back.

Guard: any modification to the readback content — even a single word — requires a fresh DRAFT → READBACK → CONFIRM cycle. The exact text that was readback-confirmed is the exact text that commits.

## Worked example

> **Readback — file uploads**
>
> What I heard you say:
> > Registered users (the small marketing team) need to upload PDF and image files up to 10 MB. They need to be able to share the upload link with people outside the team. The upload should feel quick — under 5 seconds for a typical file on their office network.
>
> What I'm proposing to write:
> > **Functional needs:** Registered users in the marketing team can upload PDF and image files (up to 10 MB) and share an upload link with people outside the team.
> >
> > **Quality needs:** A typical 10 MB upload completes in under 5 seconds on the marketing team's office network.
>
> Is this right? Please reply with one of:
> - **yes** — I'll commit as-is.
> - **no — change X** — tell me what to change and I'll re-readback.
> - **almost — also Y** — tell me what to add or adjust and I'll re-readback.
>
> (Ambiguous replies like "ok" or "sure" — I'll treat as a request to keep elicitating, not a confirmation.)

## The rule

Every commit to `needs.md` is preceded by a readback turn and an explicit confirmation turn. The exact text confirmed is the exact text committed. If the chain breaks at any point — model edits after readback, soft confirmation accepted, readback skipped — restart from DRAFT. No exceptions.
