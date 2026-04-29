# State machine

The elicitation session runs as a five-state loop: ELICIT → DRAFT → READBACK → CONFIRM → COMMIT, repeating per topic until termination fires. Within-state behaviour is heuristic; transitions between states are fragile contracts.

## States

### ELICIT

**Entry contract.** A topic is on the table — either the next item from the topic queue, a stakeholder-volunteered new thread, or a gap category that needs raising.

**Allowed actions.** Ask clarifying questions. Apply anti-assumption (surface ambiguity tells). Apply explanation-while-eliciting (translate architect concepts into stakeholder language before asking). Apply gap-finding (raise project-relevant gap categories). Probe design language back to the underlying need.

**Exit contract.** Enough material exists to compose a single candidate Needs entry on the topic. The material is in stakeholder voice. No silent gap-fill happened: every detected ambiguity has either been resolved by the stakeholder or recorded as an open gap.

### DRAFT

**Entry contract.** ELICIT has produced enough material on the topic to draft a single Needs entry.

**Allowed actions.** Internally compose the entry. Pick the section it would land in (functional, quality, constraint, interface, gap, metric). Phrase it in stakeholder voice. Do not commit to disk; do not show the stakeholder yet — that happens in READBACK.

**Exit contract.** A single candidate entry exists, in stakeholder voice, with a target section. The entry contains no design language, no architect jargon left untranslated, and no silent gap-fill.

### READBACK

**Entry contract.** A single candidate entry from DRAFT is ready to present.

**Allowed actions.** Present the candidate entry to the stakeholder using the four-part readback format: topic, what I heard you say, what I'm proposing to write, confirmation prompt offering three accepted response shapes. The format is fragile — see `readback-protocol.md` for the exact shape.

**Exit contract.** The readback message has been delivered to the stakeholder and the stakeholder has been given the opportunity to respond.

### CONFIRM

**Entry contract.** The stakeholder has responded to a readback.

**Allowed actions.** Classify the response into one of: explicit confirm (`yes`), explicit reject-with-edit (`no — change X`), explicit confirm-with-addition (`almost — also Y`), or ambiguous (anything else: `ok`, `sure`, `fine`, silence, change of subject).

**Exit contract.** The response is classified.
- On explicit confirm → transition to COMMIT.
- On explicit reject-with-edit or confirm-with-addition → return to DRAFT with the requested change incorporated.
- On ambiguous → return to ELICIT (the stakeholder is still working through the topic; the readback was premature or the content needs more material).

### COMMIT

**Entry contract.** CONFIRM produced an explicit-confirm token.

**Allowed actions.** Append the agreed entry to `needs.md` under the correct section, marking `*Confirmed: yes.*`. Note the readback turn the entry was confirmed in (a session-resume aid).

**Exit contract.** The entry is on disk. The next-topic decision is made: pop the topic queue, or take a stakeholder-volunteered new thread, or raise the next gap category.

## Transitions

| From | To | Trigger | Notes |
|---|---|---|---|
| ELICIT | DRAFT | enough material on a topic to compose one Needs entry | exit contract checked |
| DRAFT | READBACK | a single candidate entry exists in stakeholder voice | candidate not yet shown to stakeholder |
| READBACK | CONFIRM | readback message delivered using the four-part format | exact format from `readback-protocol.md` |
| CONFIRM | COMMIT | response classified as explicit confirm (`yes`) | the only path to disk |
| CONFIRM | DRAFT | response classified as `no — change X` or `almost — also Y` | redraft incorporating the requested delta, then re-readback |
| CONFIRM | ELICIT | response is ambiguous (`ok`, `sure`, `fine`, silence, change of subject) | not a confirmation; the readback was premature or the topic needs more material |
| COMMIT | ELICIT | another topic exists in the queue, the stakeholder volunteers a new thread, or the next gap category needs raising | one-cycle-per-entry; outer loop continues |
| COMMIT | (terminate) | termination signal fires | session ends; wrap-up phase runs — see `references/termination.md` |

Summary: `ELICIT → DRAFT → READBACK → CONFIRM → COMMIT`, with CONFIRM branching back to DRAFT (on edit-request) or ELICIT (on ambiguous response), and COMMIT looping to ELICIT until a termination signal fires.

## Loop semantics

One full ELICIT → COMMIT cycle covers exactly one Needs entry. The session is the outer loop over entries. Termination is checked after every COMMIT and on stakeholder cue at any point. Wrap-up runs after the last COMMIT and includes a final readback over the assembled `needs.md`.

## Lineage

The skill is grounded in the INCOSE Guide to Writing Requirements V4 lifecycle: Stakeholder Real-World Expectations → Lifecycle Concepts and Needs Definition → Integrated Set of Needs → Design Input Requirements Definition → Design Input Requirements. This skill covers the first two transformations — moving from raw stakeholder material to an Integrated Set of Needs in stakeholder voice. The Design Input Requirements transformation is the matched authoring skill's job and consumes the output of this one.

## Session interruption and resume

Sessions can be interrupted at any state. To make resume cheap:

- After every COMMIT, the in-flight `needs.md` is on disk and reflects all confirmed entries to that point.
- Maintain a session-state note alongside `needs.md` that records: the topic queue (what's covered, what's pending, what's deferred), the current in-flight draft if any (with the state it is in: DRAFT / READBACK awaiting response / CONFIRM in progress), and the stakeholder identifier.
- On resume: re-read the session-state note, brief the stakeholder on where the previous session left off, and replay the last readback if one was outstanding before continuing.
- If the previous session ended mid-READBACK (the stakeholder never responded), the resumed session re-issues the readback verbatim. Do not silently treat the previous absence as confirmation.
