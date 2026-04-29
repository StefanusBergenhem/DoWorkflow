# Termination

Termination is the success-mode end of an elicitation session: enough material has been gathered, the stakeholder agrees there is nothing else, and the assembled `needs.md` is ready for handoff. This is distinct from HALT, which is a failure-mode stop (see SKILL.md HALT conditions).

## Termination signals

The session is ready to terminate when one or more of the following hold:

1. **Two consecutive turns yield no new material**, AND the stakeholder confirms there is nothing else to cover. Two turns is the threshold; one quiet turn is not enough.
2. **Stakeholder explicitly says they are done** — phrasings like "I think we're done", "that covers it", "that's everything I can think of right now", "let's wrap up". Take the cue.
3. **All project-relevant gap categories from `gap-finding.md` have been raised** — every relevant one surfaced as a confirmed need or recorded need; every irrelevant one explicitly raised once and confirmed irrelevant.

If any one of these holds, transition to wrap-up. Do not require all three.

## Wrap-up phase actions

Before declaring `needs.md` complete, run the wrap-up sequence:

1. **Stakeholder voice pass.** Re-read the assembled `needs.md` end to end. Surface any entry that contains design language (named technology, library, algorithm, specific data shape, specific UI pattern, specific protocol version). Flag for stakeholder discussion before final readback.
2. **Confirmation trace pass.** Every entry must carry `*Confirmed: yes.*` or be explicitly placed under "Open gaps" with a status (deferred / unknown / pending-stakeholder-input). Any entry without one of these is a discipline failure — surface and resolve.
3. **Deferred-gap explicitness pass.** For every gap category from `gap-finding.md` that did not surface as a need, ensure there is an entry under "Open gaps" or a one-line note ("`<category>` — confirmed not relevant for this scope") in the session notes. Categories that simply went unraised are silent gap-fill — surface and resolve.
4. **Final readback over the assembled document.** Present the assembled `needs.md` to the stakeholder. The format is the same four-part readback (topic = "the needs document as a whole"; what I heard you say = the high-level summary; what I'm proposing to write = the assembled file; confirmation prompt). The stakeholder may request edits — incorporate and re-readback as usual.
5. **Commit the final version.** When the final readback gets `yes`, the file is the final version. Note the date and the session marker in the front-matter.

## Termination versus HALT

Termination is the success-mode end. HALT is the failure-mode end. They are not interchangeable:

- Stakeholder unavailable mid-session → HALT condition #1, not termination. Hand back partial material flagged unconfirmed.
- Stakeholder disengages mid-session → HALT condition #5, not termination. Hand back what was confirmed before disengagement.
- Stakeholder says "we're done" — but only the happy path was covered, no NFRs, no gaps surfaced → discuss before terminating. The stakeholder may not realise the gap categories matter; raise the relevance heuristic from `gap-finding.md`. If they still insist on terminating, defer the unraised gaps explicitly under "Open gaps" with status `pending-stakeholder-input` and proceed to wrap-up.

## "Come back later"

A common stakeholder response: "I want to keep going but I need to think / check with someone / talk to my team. Can we pause?"

This is a successful pause, not a HALT. Do:

1. Run a partial wrap-up: stakeholder voice pass, confirmation trace pass, deferred-gap pass on what has been covered.
2. Skip the final-document readback (the document isn't final yet).
3. Write `needs.md` with `status: in-progress` in the front-matter (instead of `prototype-mode`) and a session-resume note listing: which topics are confirmed, which gap categories are still open, what the stakeholder said they need to think about, and the topic queue for the next session.
4. Surface to the stakeholder: "I'll save what we have. When you come back, we'll pick up at `<topic>` and `<unraised gap>`. Anything else you'd like me to flag for the next session?"

## The rule

Terminate only on success-mode signals. Run the four wrap-up actions before declaring done. Distinguish termination from HALT and from "come back later". The assembled `needs.md` is final only after a final-document readback gets `yes`.
