# ADR — purpose and shape

## Contents

- [Cross-cutting placement](#cross-cutting-placement)
- [The three-condition threshold](#the-three-condition-threshold)
- [Capture-then-design flow](#capture-then-design-flow)
- [Y-statement compact form](#y-statement-compact-form)
- [What an ADR is not](#what-an-adr-is-not)

## Cross-cutting placement

ADRs live flat in `/specs/adrs/adr-NNN-{slug}.md`, regardless of which scopes the decision applies to. The `scope_tags` field names the applicable scope(s); a decision spanning two children is not buried under one of them.

When the user asks where an ADR should live: flat directory, scope_tags carries the binding. Do not place ADRs inside scope subtrees.

## The three-condition threshold

All three conditions must hold. If any one is missing, the decision is not ADR-worthy — note it inline in the consuming Architecture or Detailed Design rationale and move on.

1. **Load-bearing.** Changing the decision later could break the system or invalidate downstream design.
2. **≥2 real options existed.** Two or more alternatives were genuinely on the table. "Do nothing" and straw men do not count.
3. **Contingent on assumptions or drivers that may change.** The decision rests on something specific to the present context; if those inputs shift, the decision may need to be revisited.

When the user proposes an ADR for a routine choice (variable naming, import path, method signature, directory layout): refuse (refusal E — `anti-pattern.routine-choice`). The signal-to-noise ratio of the ADR stream collapses when written for everything; the decisions that matter stop being read. >1–2 ADRs per scope per sprint in steady state is a tell that the threshold is being violated.

When the user proposes an ADR but only one option was on the table: refuse (refusal C — `anti-pattern.single-option`). One option + a justification is description, not a decision. Either surface a real alternative or capture the choice inline.

When the user proposes an ADR for a decision that does not rest on any changeable input: refuse (`check.threshold.condition-contingency-missing`). With no revisit trigger possible, there is no future maintainer to write for.

## Capture-then-design flow

The sequence that produces a faithful ADR:

1. **Surface options.** Two or more real candidates, named.
2. **Name the drivers.** Quality attributes, constraints, deadlines, dependencies — explicit before any option is evaluated, so weighting does not creep in post-hoc.
3. **State assumptions.** What each option assumes about the world. These are the revisit triggers.
4. **Pick.** The chosen option scores best against the drivers. The rationale cites drivers by name.
5. **Capture.** Write the ADR. While the conversation is still in working memory.
6. **Design.** Derive the Architecture or Detailed Design from the ADR — not the other way around.

When the design is drafted first and the ADR is written afterwards: the option space has already collapsed. What remains is reconstruction, and reconstruction looks like rationale without being rationale (`anti-pattern.post-hoc-rationalisation`). Tell: rejected alternatives all reduce to approximately the same design as the chosen one — they were never real options.

When the user asks "can we just write the ADR after the architecture is done?": the answer is no for load-bearing decisions; the rationale is lossy the moment a decision is made. A week later it is approximation; a month later, plausible-sounding; a year later, a story that reads well but does not match what happened.

## Y-statement compact form

When the decision fits it, a Y-statement is an optional abstract line at the top of the ADR — not a replacement for the body. Canonical form:

> *In the context of {situation}, facing {concern}, we decided for {option}, to achieve {quality}, accepting {downside}.*

The five anchors map to Context, drivers, Decision, Rationale, and the chief negative consequence. Use it to let a skilled reader absorb the ADR in one sentence.

When the Y-statement appears but is not in canonical form: `check.y-statement.shape-malformed` (soft).

## What an ADR is not

- **Not a design document.** The ADR captures a decision; the Architecture or Detailed Design captures the structure that implements it.
- **Not a meeting record.** Discussion is summarised to the decision and the reasons that survive it. Transcripts do not belong.
- **Not a retrofit invention.** When rationale for a pre-existing decision is lost, the framework records that it is lost (`recovery_status: unknown`), not a plausible-sounding story.

## Cross-link

`canonical-fields-and-body.md` (the shape) · `extraction-cues.md` (where ADRs originate) · `anti-patterns.md` (1, 5, 6, threshold checks)
