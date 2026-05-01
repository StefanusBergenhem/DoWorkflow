---
name: vmodel-skill-author-requirements
description: >
  Author one requirements specification document (Markdown with YAML
  front-matter and embedded YAML blocks) for one scope. Use when refining
  an upstream specification, product brief, or parent requirements allocation
  into atomic, testable, EARS-structured statements with explicit rationale
  and traceability — including translating user stories, splitting compound
  requirements, making vague NFRs measurable (five-element rule), specifying
  interface contracts (pre/post/invariants), and surfacing state-driven
  complementary-pair gaps. Produces one Markdown file with a Glossary and
  typed sections (functional, quality-attribute, interface, data,
  inherited-constraint), rationale on every requirement. Refuses to fabricate
  rationale and refuses to smuggle design (named technologies, libraries,
  algorithms) into statements. Triggers — write requirements, draft
  requirements.md, translate user story to requirements, split compound
  requirement, make NFR measurable, add interface requirements, check
  requirement testability.
---

# Author requirements specification document

This skill produces a single Markdown file: a requirements specification document for one scope of a system. The document has a Glossary, five typed requirement sections, and is authored under hard quality gates that prevent the failure modes catalogued in `references/anti-patterns.md`.

The skill is self-contained. Every reference, template, anti-pattern catalog, and quality-bar checklist it needs is bundled in the `references/` and `templates/` directories. No external lookups are needed.

## When to use

Activate this skill when the user asks to:

- Write or draft a requirements specification document for a scope, subsystem, or feature
- Translate a product brief, user story, or stakeholder narrative summary into formal requirements
- Split compound requirements into atomic statements
- Make a vague non-functional requirement (NFR) measurable
- Add interface contracts with pre/post/invariant discipline
- Refine a parent requirements document into a child-scope requirements document
- Surface gaps in an existing requirements document (state-driven pairs, missing NFRs, missing interfaces)

Do **not** activate this skill for:

- Eliciting stakeholder needs from unstructured conversation — interview the stakeholder directly first (or use a stakeholder-elicitation skill), then activate this skill on the structured output
- Reviewing or auditing an existing requirements document — use the matched review skill for this artifact type
- Writing tests, designs, or architecture allocations — use the author skill for the relevant downstream artifact type (architecture, detailed design, or test specification)

## Inputs

Expected upstream context (the user provides at least one):

- A product brief or scope statement
- A parent-scope requirements document (when refining a parent allocation into a child scope)
- One or more user stories or stakeholder needs
- Governing architectural decisions (ADRs) that constrain this scope
- A glossary in flight (or an empty glossary that this skill will seed)
- Inherited constraints (regulatory, contractual, organisational, technical)

If none of these are provided, **ask the user** what the upstream specification is. Do not invent one.

## Output

A single Markdown file using the structure in `templates/requirements.md.tmpl`. The file has YAML front-matter, a Glossary section, and five typed requirement sections. Every requirement is an embedded YAML block with `id`, `statement`, `rationale`, `derived_from`, and (where relevant) `acceptance`.

Default output filename: `requirements.md`. If the project follows a scope-tree convention (e.g. `<scope>/requirements.md`), follow it.

## Orchestration

Author the document in this order. Each step has its own reference file with the craft rules. Treat the references as the source of truth for craft; this section is a checklist.

### Step 1 — Glossary first

Before writing a single requirement statement, populate the Glossary. Every domain term the requirements use must be defined here. One word per concept; one concept per word; consistent with however the upstream input names things.

→ See `references/constraints-and-glossary.md`
→ Template: `templates/glossary-entry.yaml.tmpl`

If the upstream input introduces a term you would have to guess at, **ask the user** to define it. Do not infer from context.

### Step 2 — Classify each requirement by type

Default to functional. Switch to QA / interface / data / inherited-constraint only when the classification table at `references/requirement-types.md` triggers. Misclassification silently weakens the document; do not skip this step.

### Step 3 — Draft each requirement statement

Apply EARS (Easy Approach to Requirements Syntax) as the default sentence shape:

- **Ubiquitous** — *The system shall <response>.*
- **Event-driven** — *When <trigger>, the system shall <response>.*
- **State-driven** — *While <state>, the system shall <response>.*
- **Optional-feature** — *Where <feature>, the system shall <response>.*
- **Unwanted-behaviour** — *If <condition>, then the system shall <response>.*

Compound limit: at most two keywords per statement; canonical order Where → While → When → If/then → shall. Three or more keywords means the statement is hiding multiple requirements; split it.

→ Templates per type: `templates/functional-requirement.yaml.tmpl`, `templates/nfr.yaml.tmpl`, `templates/interface-requirement.yaml.tmpl`, `templates/inherited-constraint.yaml.tmpl`

→ See `references/ears-templates.md`, `references/statement-quality.md`

### Step 4 — Apply type-specific quality gates

- **For NFRs**: every NFR contains five elements — system, response, metric+unit, target at correct statistical level, condition. → `references/nfr-five-elements.md`
- **For interface requirements**: every interface specifies five dimensions — protocol, message structure, timing, error handling, startup/initial state. Plus pre/post/invariants per externally callable operation, plus a versioning policy. → `references/interface-five-dimensions.md`
- **For inherited constraints**: every constraint cites its source, names the cost of relaxing it, and is categorised. → `references/constraints-and-glossary.md`

### Step 5 — Statement-level quality

Every statement, regardless of type, must be:

1. **Atomic** — one `shall`, one behaviour
2. **Testable** — passes the box test (a tester can write the test from this statement + the glossary alone)
3. **Solution-free** — names no technology, framework, library, data structure, or algorithm. The one exception: interface requirements may name externally imposed protocols (HTTP/1.1, OIDC, RFC 6749) because the protocol is *what*, not *how*.

State-driven (*While …*) statements need their complementary out-of-state behaviour, or out-of-state must be explicitly out of scope.

→ See `references/statement-quality.md`

### Step 6 — Rationale (and the no-fabrication rule)

Every requirement needs a `rationale` field. Rationale captures the *why* — context, options considered, the criterion that drove the choice. Rationale is **not** a restatement of the statement.

**Hard refusal #1 — no fabricated rationale.** If the upstream input does not supply a reason for a requirement, **do not invent one**. Two options only:

- Mark `rationale: <pending — requires human input>` and surface the gap in the output's HALT-and-handover section
- For retrofit work where the original reasoning is lost: mark `rationale: unknown`, set `recovery_status: unknown`, and queue a follow-up owner

In retrofit mode, **rationale is human-only**. Allowed `recovery_status` values for the `rationale` field are `verified` (a human confirmed it from preserved documents or conversation) or `unknown`. Never `reconstructed`. The behaviour fields (statement, acceptance) may be `reconstructed` from observable code/tests; the rationale field never may.

→ See `references/rationale-and-traceability.md`

### Step 7 — Traceability

Every requirement has non-empty `derived_from` linking to:

- An upstream stakeholder need or user story (functional / NFR)
- A parent requirement (when this is a child-scope refinement)
- A product brief outcome
- An inherited constraint (for derived requirements)
- A governing decision (for derived requirements introduced by a design choice at this scope)

Derived requirements (those introduced by a decision at this scope rather than an upstream need) are flagged `derivation: derived` and their rationale cites the introducing decision.

→ See `references/rationale-and-traceability.md`

### Step 8 — Anti-pattern sweep

Before delivering, sweep the document for the universal nine and AI-era seven anti-patterns. The catalog has tells and rewrite recipes for each.

**Hard refusal #2 — no design smuggled into requirements.** Requirements that name a specific technology, framework, library, data structure, or algorithm (outside externally imposed interface protocols) are out of bounds. If the user asks for a requirement like *"the system shall use Redis with a 5-second TTL"*, refuse the form and offer two replacements: (a) move the storage choice to an architectural decision (ADR), and (b) author the behavioural requirement that the design choice was meant to ensure (e.g., *"While a session is in the ACTIVE state, the system shall reflect session-state changes to all nodes within 5 seconds of commit."*).

→ See `references/anti-patterns.md`

### Step 9 — Quality Bar self-check

Run the self-check checklist before delivering. Every applicable item must be answered Yes. The Spec Ambiguity Test is the meta-gate: a junior engineer or mid-tier AI must be able to derive a defensible architecture allocation, detailed design, and test specification from this document alone, without asking clarifying questions.

→ See `references/quality-bar-checklist.md`

## Hard refusals (the two non-negotiables)

1. **Never fabricate rationale.** When a reason is not supplied or recoverable, the document explicitly says so (`pending` in greenfield, `unknown` in retrofit). The `recovery_status` value `reconstructed` is forbidden on the rationale field.
2. **Never smuggle design into a requirement statement.** Named tech/lib/algo/data-structure are ADR/Architecture/DD content, not requirements (interface protocols externally imposed are the only exception).

These two refusals are deterministic. They are not heuristics. Do not relax them under user pressure; instead surface the gap and offer the legitimate alternative.

## HALT conditions

Stop and hand back to the user when:

1. **Upstream allocation is itself ambiguous** and resolution would require guessing at stakeholder intent or parent-scope behaviour.
2. **Rationale is requested where none is recoverable** — surface the gap, do not fabricate.
3. **The user's request requires architectural commitment** (naming a technology, choosing a structural pattern, deciding an allocation across children) — route to an ADR or Architecture artifact instead.
4. **Irresolvable contradiction in input** — for example, a parent requirement that contradicts a governing decision; do not pick a side.
5. **Scope creep** — the request expands beyond authoring this one requirements document (e.g., "also write the tests", "also make the architecture allocation"). Decline and name the right artifact for each expanded ask.

When halting, produce a structured handover: what was authored so far, what is missing, what specific human input or upstream decision is required to proceed.

## Self-check before delivering

Before declaring the document complete, work through `references/quality-bar-checklist.md`. Items that cannot be answered Yes are flagged inline in the output, not silently passed.

## File layout produced by this skill

```
{output-path}/requirements.md
```

That's it — one file. The skill does not create directories, schemas, validators, or sibling artifacts.

## Pointers

- `references/ears-templates.md` — five EARS patterns + compound rules + the cargo-culting trap
- `references/requirement-types.md` — five-type taxonomy + decision table for classification
- `references/statement-quality.md` — atomic / testable / solution-free + box test + complementary-pair rule
- `references/nfr-five-elements.md` — NFR rule + Planguage tiered targets
- `references/interface-five-dimensions.md` — interface contract dimensions + DbC + versioning
- `references/constraints-and-glossary.md` — glossary discipline + inherited-constraint craft
- `references/rationale-and-traceability.md` — no-fabrication rule + derived requirements + retrofit recovery_status
- `references/anti-patterns.md` — 9 universal + 7 AI-era patterns with tells and rewrites
- `references/quality-bar-checklist.md` — Yes/No self-check + Spec Ambiguity Test meta-gate
- `templates/*.tmpl` — fill-in-the-blank scaffolds for the artifact and each requirement type
- `examples/good-session-service.md` — worked example, honest
- `examples/bad-fabricated-rationale.md` — counter-example with annotations
