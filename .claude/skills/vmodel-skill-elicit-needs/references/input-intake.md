# Input intake — seeding the session

The skill runs the same five-state loop regardless of input shape, but the first few ELICIT turns differ depending on what raw material the stakeholder brings. This file covers the four input shapes and how to seed each.

In every case, stakeholder availability for the readback handshake is mandatory. If the input is offline (transcript, draft, notes) and the stakeholder is not reachable, HALT (HALT condition #1).

## Live conversation

The stakeholder is reachable in this session and there is no prior written material.

**Approach.** Start with open-ended prompts. Let the stakeholder volunteer first; they will frame the problem in terms of what they care about, which is exactly what `needs.md` should capture. Probe for ambiguity tells as they appear. Pace the gap-finding — do not flood the stakeholder with all six gap categories on turn one.

**First-question template.**
> "Tell me what you're trying to build, in your own words. Don't worry about being precise yet — I'll ask follow-ups. Start with: who is this for, and what changes for them when it works?"

After the opening response, pick one ambiguity tell or one gap category and probe it. One thread at a time.

## Interview transcript

The stakeholder produced (or was the subject of) a recorded conversation. The text is available; the stakeholder is reachable for follow-up.

**Approach.** Read the transcript and extract three lists: stakeholder-voice claims (candidate needs), ambiguity tells (plurals without count, undefined comparatives, etc.), and design-smuggled claims (named technologies, libraries, specific data shapes). Do not commit any extracted claim. Every extracted claim must be verified with the stakeholder before it lands in `needs.md` — the transcript itself is not a confirmation token.

**First-question template.**
> "I read through the transcript. I want to make sure I understood you correctly. I'll read back what I think you said, one thread at a time, and I'd like a yes / no / almost on each. The first thread is `<topic>`. Here's what I heard …"

This effectively front-loads the READBACK state. Each transcript-extracted claim still needs a confirmation token.

## Draft document or partial product description

A stakeholder authored a draft. It contains stakeholder-voice claims and may also contain design-smuggled claims, ambiguity, and gaps.

**Approach.** Read the draft for two things: stakeholder-voice claims (candidates) and design-smuggled claims (probe back to the underlying need). Flag the latter for the stakeholder to clarify; do not silently strip them out and commit the rest. The stakeholder may have included design language deliberately — find out why.

**First-question template.**
> "I read the draft. There are some places where you wrote about specific technologies (`<list 1–3>`); I want to understand what you were trying to achieve there before I shape the needs document. For each of those, can you tell me what would happen if a different choice met the same goal? And separately, there are a few places where the draft says `<ambiguity>` — I'd like to clarify those before committing anything."

## Scattered notes

Bullet points, whiteboard photos, chat snippets, voice transcripts of an unstructured stakeholder narrative. The material is incomplete and unstructured by design.

**Approach.** Cluster the notes by topic — group items that talk about the same thing. Verify the clustering with the stakeholder before deepening any cluster. Do not invent topics that were not in the notes. Once clusters are confirmed, run the ELICIT → COMMIT loop one cluster at a time.

**First-question template.**
> "I clustered the notes into `<N>` themes: `<theme A>`, `<theme B>`, `<theme C>`. Before I dig into any one of them, do these themes match how you think about the problem, or am I miscutting it? I'd rather get the cuts right first than push deeper on a wrong cut."

## Cross-cutting rules

- **Verify before committing**, no matter the input shape. Offline material (transcripts, drafts, notes) is a starting point, not a confirmation token. The full readback contract still applies to every entry that ends up in `needs.md`.
- **Surface design smuggling explicitly**. Do not silently rewrite stakeholder language; the stakeholder may have meant the design term and need it raised back to them as the probe pattern in `stakeholder-voice.md`.
- **Pace the gap-finding**. The gap-category checklist in `gap-finding.md` is comprehensive; do not run it as a flat questionnaire. Raise gaps as they become project-relevant or after the stakeholder has aired the topic they brought.
