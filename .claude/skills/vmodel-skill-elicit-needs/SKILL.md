---
name: vmodel-skill-elicit-needs
description: >
  Interactive stakeholder elicitation that produces a rough `needs.md` from
  unstructured input (interview notes, transcripts, drafts, scattered ideas).
  Use when starting from raw stakeholder material at root scope — converting
  an interview transcript into structured needs, running a discovery session,
  refining a stakeholder-written draft, or bridging a conversation into
  specification work. Applies four disciplines: anti-assumption (surface
  ambiguity, never silently fill gaps), explanation-while-eliciting (translate
  architect concepts into stakeholder language), gap-finding (raise unknowns
  a competent architect would flag), readback-for-joint-agreement (read
  understanding back for confirmation before committing). Distinct from
  author-requirements, which expects structured upstream input. Triggers —
  elicit requirements, interview stakeholder, capture needs, requirements
  discovery, convert transcript to needs.
---

# Elicit stakeholder needs

This skill conducts an interactive elicitation session with a stakeholder and produces a single rough Markdown file: `needs.md`. The output is a stakeholder-voice record of what the stakeholder wants, captured in user-realm vocabulary, with every committed entry traced to a confirmation token from a readback turn. The structure is bundled in `templates/needs.md.tmpl` but is intentionally prototype-mode — not yet schema-validated or formally specified. The shape may be promoted later once pilot reps reveal what works.

The skill is self-contained. Every reference, template, and example it needs is bundled in the `references/`, `templates/`, and `examples/` directories. No external lookups are needed.

## When to use

Activate this skill when the user asks to:

- Convert an interview transcript into structured needs
- Run a requirements-discovery session with a stakeholder
- Capture stakeholder input from a live conversation
- Refine a stakeholder-written draft into a needs document
- Cluster scattered notes from a discovery workshop into a coherent needs record
- Bridge a stakeholder conversation into specification work that downstream authoring skills can consume

## Do not activate this skill for

- Writing a requirements specification document — that is the matched authoring skill's job; activate it on the structured output of this skill
- Reviewing or auditing an existing needs document — that is a separate review concern
- Writing tests, designs, architecture, or any downstream artifact
- Producing a needs document when the stakeholder is unavailable for the readback handshake — without the handshake the load-bearing discipline cannot run; HALT instead
- Operating on input that is already a structured spec (a Requirements artifact, formal user stories with acceptance criteria, an allocated parent requirement) — wrong tool; route to an authoring skill

## Inputs

Expected stakeholder material (the user provides at least one):

- Live conversation access (the stakeholder is reachable in this session)
- An interview transcript (text capture of a stakeholder conversation)
- A draft document or partial product description authored by a stakeholder
- Scattered notes (bullet points, whiteboard photos, chat snippets) from a discovery workshop
- A voice transcript of an unstructured stakeholder narrative

Stakeholder availability for the readback handshake is mandatory regardless of the input shape. If the input is offline (transcript, notes, draft) and the stakeholder is not reachable, HALT — readback cannot be skipped.

## Output

A single Markdown file using the structure in `templates/needs.md.tmpl`. The file has YAML front-matter, a stakeholder-voice intent paragraph, typed need sections (functional, quality, constraints, interfaces), a gap section, and success metrics. Every entry carries an explicit `*Confirmed: yes/no.*` flag traced to a readback turn.

Default output filename: `needs.md`. If the user has a scope-tree convention (e.g. `/specs/needs.md`), follow it.

## Orchestration

The session runs as a state machine: ELICIT → DRAFT → READBACK → CONFIRM → COMMIT, looping until termination fires. Within-state behaviour is heuristic; transitions between states are fragile contracts.

### Step 1 — ELICIT

Gather material from the stakeholder on a topic. Apply the four disciplines:

- Anti-assumption — surface every ambiguity tell as a structured question; never silently fill gaps.
- Explanation-while-eliciting — translate every architect concept into stakeholder language before asking about it.
- Active gap-finding — proactively raise gap categories the stakeholder has not volunteered.
- Stakeholder-voice discipline — probe design language back to the underlying need; do not let named technologies, libraries, algorithms, or specific data shapes survive into the draft.

Seeding the first few turns depends on the input shape.

→ See `references/state-machine.md` for state contracts
→ See `references/input-intake.md` for per-input-shape seeding
→ See `references/stakeholder-voice.md` for the voice discipline
→ See `references/anti-assumption.md` for ambiguity tells and the question template
→ See `references/explanation-while-eliciting.md` for the architect-concept trigger list
→ See `references/gap-finding.md` for the gap-category checklist

### Step 2 — DRAFT

Internally compose a candidate Needs entry in stakeholder voice. Pick the section it would land in (functional, quality, constraint, interface, gap, metric). Do not commit.

### Step 3 — READBACK

Present the draft to the stakeholder using the readback template. The readback message has four parts: topic, what I heard you say (paraphrased in stakeholder voice), what I'm proposing to write, and an explicit confirmation prompt offering three accepted response shapes.

→ See `references/readback-protocol.md`
→ Template: `templates/readback.md.tmpl`

### Step 4 — CONFIRM

Wait for an explicit confirmation token: `yes`, `no — change X`, or `almost — also Y`. Ambiguous responses (`ok`, `sure`, `fine`, `yeah whatever`, silence, change of subject) are treated as continued elicitation, not confirmation. On `no` or `almost`, redraft and re-readback.

### Step 5 — COMMIT

Only when an explicit confirmation token is received, write the agreed entry to `needs.md` under the correct section, marking `*Confirmed: yes.*` and noting the readback turn it was confirmed in. Then loop back to ELICIT for the next topic.

### Step 6 — Termination and wrap-up

Terminate when the stakeholder confirms there is nothing else to cover, or when two consecutive turns produce no new material and the stakeholder agrees coverage is complete, or when every relevant gap category has been raised. Wrap up with a final pass over `needs.md` for stakeholder voice, deferred-gap explicitness, and one final readback over the assembled document.

→ See `references/termination.md`

## Hard refusals (the three non-negotiables)

1. **Never commit content to `needs.md` without an explicit stakeholder confirmation token.** Readback-for-joint-agreement is the load-bearing discipline. Ambiguous responses (`ok`, `sure`, `fine`, silence, change of subject) are not confirmation. Treat them as a signal to keep elicitating.
2. **Never silently fill an ambiguity gap.** Every detected ambiguity tell (plurals without count, undefined comparatives, unscoped pronouns, unbounded operations, unstated audiences, unstated triggers) must surface as a structured clarification question with the four-line format (Expected / Found / Why this matters / How should I proceed?). Surface count is acceptable; silent fill is not.
3. **Never write design language into `needs.md`.** Named technologies, libraries, algorithms, data structures, specific UI patterns, or specific protocol versions are out of bounds at the needs layer. If the stakeholder uses such language, probe for the underlying need ("what would `<choice>` give you that other options wouldn't?") and capture the answer to the probe, not the original choice.

These three refusals are deterministic. They are not heuristics. Do not relax them under stakeholder pressure; instead surface the conflict and offer the legitimate alternative.

## HALT conditions

Stop and hand back when:

1. **Stakeholder unavailable for the readback handshake** — readback is non-skippable; without it the skill cannot commit. Hand back partial elicitation notes flagged as unconfirmed.
2. **Stakeholder gives contradictory information across turns and will not reconcile** when the contradiction is surfaced. Irresolvable contradiction.
3. **Stakeholder requests work outside the elicitation boundary** — design proposals, implementation plans, test design, architecture allocation. Decline and name the right artifact for each expanded ask.
4. **Input is already a structured spec** — a Requirements artifact, formal user stories with acceptance criteria, allocated parent requirements. Wrong skill; point at an authoring skill.
5. **Stakeholder disengages mid-session** — no response, refuses to engage with readback, repeated change of subject. Hand back what was confirmed before disengagement.
6. **Stakeholder repeatedly insists on design language** in `needs.md` after Hard Refusal #3 has been explained twice. The refusal cannot be silently capitulated; HALT and surface the conflict.

When halting, produce a structured handover: what was confirmed so far (committed entries), what was elicited but not confirmed (unconfirmed drafts), what gap categories remain unraised, and what specific stakeholder input or environmental change is required to resume.

## Self-check before delivering

Before declaring `needs.md` complete, verify:

1. Every entry under each section carries `*Confirmed: yes.*` (or is explicitly marked deferred / unknown / pending-stakeholder-input under "Open gaps")
2. No entry contains a named technology, library, algorithm, data structure, specific UI pattern, or specific protocol version
3. Every commit is traceable to a readback turn (the confirmation chain is unbroken)
4. Every relevant gap category from `references/gap-finding.md` has either surfaced as a need or been explicitly recorded as deferred
5. The stakeholder has had one final readback over the assembled `needs.md` and confirmed it as a whole
6. Items that cannot be answered Yes are flagged inline in the output, not silently passed

## File layout produced by this skill

```
{output-path}/needs.md
```

That's it — one file. The skill does not create directories, schemas, validators, or sibling artifacts.

## Pointers

- `references/state-machine.md` — formal state contracts (entry / actions / exit) for ELICIT / DRAFT / READBACK / CONFIRM / COMMIT, plus session-resume notes
- `references/input-intake.md` — per-input-shape seeding (live, transcript, draft, scattered notes) with first-question templates
- `references/stakeholder-voice.md` — voice discipline + design-smuggling tells + the probe pattern
- `references/anti-assumption.md` — ambiguity tells + the four-line clarification template
- `references/explanation-while-eliciting.md` — architect-concept trigger list with stakeholder-facing translations
- `references/gap-finding.md` — six gap categories with trigger questions and project-relevance heuristic
- `references/readback-protocol.md` — fragile readback contract, three accepted response shapes, three failure modes
- `references/termination.md` — termination signals, wrap-up phase, distinction from HALT, session-resume notes
- `templates/needs.md.tmpl` — the rough prototype-mode output structure
- `templates/readback.md.tmpl` — the in-loop readback message format (not committed to needs.md)
- `templates/anti-assumption-question.md.tmpl` — the four-line clarification question format
- `examples/good-session.md` — worked happy-path session with discipline annotations
- `examples/bad-session-silent-gap-fill.md` — counter-example showing skipped anti-assumption discipline
- `examples/bad-session-skipped-readback.md` — counter-example showing broken readback contract
- `examples/good-needs-md.md` — well-formed prototype-mode needs.md output
- `examples/bad-needs-md.md` — counter-example needs.md with three vivid failures
