---
name: vmodel-skill-author-detailed-design
description: Author a detailed design specification document (Markdown with YAML front-matter, embedded YAML blocks, optional Mermaid state diagrams) for one leaf scope. Use when refining a parent Architecture's leaf-allocation into a DD — the level at which code can be implemented and unit tests derived without guessing. Produces one Markdown file with seven mandatory sections (Metadata, Overview, Public Interface, Data Structures, Algorithms, State, Error Handling), Design-by-Contract clauses on every public function, invariant-style data structures, result-property algorithms, an error-handling matrix, and rationale captured at decision time. Refuses code-paraphrase, algorithmic postconditions, fabricated retrofit rationale, authoring without a parent Architecture, and shipping when the Spec Ambiguity Test fails. Triggers — write detailed design, draft detailed_design.md, refine this leaf, specify function contracts, design data invariants, document error handling, retrofit DD from code.
type: skill
---

# Author detailed design specification document

This skill produces a single Markdown file: a Detailed Design (DD) specification for one leaf scope. The document carries the seven mandatory sections (Metadata, Overview, Public Interface, Data Structures, Algorithms, State, Error Handling). Every public function carries a full Design-by-Contract clause set; every internal data structure is specified by invariant; every algorithm is specified at the result level except where the algorithm itself is contractual; every error class is named with a five-column matrix entry. The skill is authored under hard quality gates that prevent the most common DD-authoring failures — code paraphrase, algorithmic postconditions, fabricated retrofit rationale, and DDs that pass syntactic checks but cannot guide a junior engineer.

The skill is self-contained. Every reference, template, anti-pattern catalog, and quality-bar checklist it needs is bundled in the `references/` and `templates/` directories. No external lookups are needed.

## When to use

Activate this skill when the user asks to:

- Write or draft a Detailed Design document for a leaf scope
- Refine a parent Architecture's leaf-allocation into per-function contracts, data invariants, algorithms, state, and error semantics
- Specify the contract every public function in a leaf must satisfy (preconditions / postconditions / invariants / errors / side effects / thread-safety)
- Design data structures by invariant (fields / ownership / lifetime / returned-object semantics)
- Specify error handling as a five-column matrix (error / detection / containment / recovery / caller-receives)
- Retrofit a DD from existing code with `recovery_status` discipline (overview narrowed to verified | unknown only)

Do **not** activate this skill for:

- Authoring Requirements, Architecture, ADR, TestSpec, or Product Brief — those are separate authoring skills' jobs
- Reviewing or auditing an existing DD — that is the matched review skill's job
- Writing implementation code or unit tests — those are downstream artifacts (`develop-code` and `derive-test-cases`)

## Inputs

Expected upstream context (ask if missing):

- **Leaf scope identifier** — the leaf-scope path and name being designed (e.g., `app/jobs/dequeue-service`)
- **Parent Architecture artifact** — the Architecture that allocates work to this leaf via its Decomposition entry; the leaf's responsibilities and the interfaces it must honour with siblings come from there
- **Derived-from set** — at least one Requirement (REQ-…) plus optionally sibling DDs, ARCH interfaces, or ADRs that shape the contract; an empty `derived_from` is an orphan design
- **Governing ADRs** — cross-cutting decisions that constrain choices at this leaf (often inherited from the parent Architecture's `governing_adrs`)
- **Recovery posture** — greenfield (omit `recovery_status`) or retrofit (declare `recovery_status` and supply source-code references)

If the parent Architecture is not provided, **HALT** (see HALT condition #1) — refusal B fires when DD authoring proceeds without a parent allocation.

## Output

A single Markdown file using the structure in `templates/detailed-design.md.tmpl`. The file has YAML front-matter, an Overview section, and the six body sections (Public Interface, Data Structures, Algorithms, State, Error Handling — five sections; Metadata is the front-matter, totalling seven addressable areas). Embedded YAML blocks render Public Interface entries (matching `$defs/public_interface_entry`), Data Structure entries (matching `$defs/data_structure_entry`), and Error Handling matrix rows (matching `$defs/error_matrix_row`).

Default output filename: `<scope>/detailed_design.md`. Follow the project's scope-tree convention when one exists.

## Cross-cutting authoring discipline

Apply the six rules in `references/authoring-discipline.md` across every authoring step. Most relevant here: Rule 0 (no `n/a + justification` for omitted slots, no self-attestation prose), Rule 1 inverts at this layer — a DD describes leaf internals (Public Interface, Data Structures, Algorithms, State, Error Handling) and must NOT re-state the parent Architecture's boundary contract; reference it instead, Rule 2 (small-system collapse: when the parent scope has all-leaf children and fewer than 5, recognise the combined `architecture-and-design.md` mode and author the *Detailed Design* sub-section per child rather than a standalone DD file), Rule 3 (rationale = one line + ADR cite when a governing ADR exists, no re-narration), Rule 4 (state diagram OR prose transition table per state machine, not both), Rule 5 (cite parent Architecture interfaces, governing ADRs, and derived requirements by ID, do not restate). Review skills enforce all six as `check.discipline.<rule>` findings.

## Authoring procedure

Author the document in this order. Each step has its own reference file with the craft rules. Treat the references as the source of truth; this section is a checklist.

### Step 1 — Locate the leaf in the parent Architecture

Identify the parent Architecture's Decomposition entry that allocates work to this leaf. Note the responsibilities the parent allocated, the interfaces this leaf must consume / produce, and the governing ADRs the parent inherits. Do not re-derive what the parent Architecture already fixed; receive its allocation as input.

→ See `references/dd-purpose-and-shape.md` (sections "Where DD sits", "Inputs from parent Architecture")

### Step 2 — Author Metadata (front-matter)

Populate the YAML front-matter: `id`, `artifact_type: detailed-design`, `scope`, `parent_scope`, `parent_architecture`, `derived_from` (non-empty), `governing_adrs` (when applicable), `status`, `date`, and (retrofit only) `recovery_status`. The `parent_architecture` field is required — schema-level enforcement of refusal B.

→ See `templates/detailed-design.md.tmpl`

### Step 3 — Author Overview

Two to three paragraphs: what this leaf is, what slice of its parent Architecture's allocation it realises, and the approach at one level of abstraction above the code. Name what *must be true* of any valid implementation; do not paraphrase the implementation. Overview is human-only intent — under retrofit, `recovery_status.overview` is narrowed to `verified | unknown` only (see refusal A).

→ See `references/dd-purpose-and-shape.md` (section "What a DD is for")

### Step 4 — Author Public Interface entries

For every externally visible function, class, or type, write a complete Design-by-Contract entry: signature, preconditions, postconditions (split by outcome), invariants, errors (typed), side effects, thread-safety category (Goetz taxonomy), nullability per parameter and return, and complexity notes when contractual. A signature alone is not a contract; it is a name for one.

→ See `references/function-contracts.md`
→ Template: `templates/public-interface-entry.yaml.tmpl`

### Step 5 — Author Data Structures (by invariant)

For every internal data structure, state fields with invariants (not field layout), ownership (who constructs / mutates / releases), lifetime (request-scoped, per-connection, process-singleton), and returned-object semantics (copy / live view / read-only reference / ownership transfer). Shared mutable state is always a contract — name the lock, the happens-before, and the per-field ownership.

→ See `references/data-structures-by-invariant.md`
→ Template: `templates/data-structure-entry.yaml.tmpl`

### Step 6 — Author Algorithms

Specify the result property; select the algorithm only when it matters. State the requirement (what property the output satisfies) and leave the algorithm to the implementer — *unless* determinism, worst-case bounds, or operational constraints make a specific approach contractual, in which case say so and say why. Pick the specification pattern that fits the behaviour shape (prose + property form for pure functions; decision table for rule-based logic; state machine for mode-dependent; sequence description for multi-step protocols).

→ See `references/algorithms.md`
→ Template: `templates/decision-table.md.tmpl` (when applicable)

### Step 7 — Author State

If the leaf is stateless, say so in one line and move on — explicit assertion of absence is content, not a missing section. If stateful, populate: state inventory with per-state invariants, transition table (source / event / guard / action / target), initial and terminal states, undefined-event handling (ignore / log / fault / raise — silence here is dangerous), entry / exit actions where applicable. State machine that does not fit on a page or two is too large — decompose. Thread safety belongs here when the leaf is used across threads (Goetz category per shared field; lock + happens-before per field).

→ See `references/state-and-concurrency.md`
→ Template: `templates/state-machine.mmd.tmpl` (when applicable)

### Step 8 — Author Error Handling

For each error class, answer the six questions: what can fail, how is it detected, how is it propagated or contained, what is the recovery strategy, what state is the leaf in afterwards, what does the caller receive. Render as a five-column matrix (error / detection / containment / recovery / caller-receives). Each row corresponds directly to a robustness test case in the sibling TestSpec. The recovery strategy is one of five: fail-fast, retry (bounded), fallback, compensate, propagate. Silence is not an option.

→ See `references/error-handling.md`
→ Template: `templates/error-matrix-row.yaml.tmpl`

### Step 9 — Capture rationale at decision time

Inline rationale at the site of every non-obvious decision (one or two sentences naming the forcing constraint — external standard, architectural choice, resource budget, temporal constraint). Load-bearing decisions that meet the ADR threshold (load-bearing AND cross-cutting AND hard-to-reverse) belong in a sibling ADR — emit `[DEFER-ADR: <decision>]` markers and reference them via `governing_adrs` once authored. Do not inline rationale that obviously meets the three ADR criteria.

→ See `references/rationale-capture.md`, `references/adr-extraction-cues.md`
→ Template: `templates/governing-adr-reference.yaml.tmpl`

### Step 10 — Apply retrofit posture (retrofit only)

Mark observed structure (`Public Interface`, `Data Structures`, `Algorithms` where extracted from code, `State` where machine is observable, `Error Handling` where matrix rows are derived from code paths) with `recovery_status: reconstructed` and cite source-code evidence (file/line/commit/schema). Mark `Overview` (intent) as `verified` (with a human source) or `unknown` only — *never* `reconstructed`. This is hard refusal A and is enforced at schema level via the `overview` enum. Do not invent rationale; pair every `unknown` with a follow-up owner.

→ See `references/retrofit-discipline.md`

### Step 11 — Sweep TestSpec traceability cues

Every error-matrix row → robustness test row in the sibling TestSpec. Every postcondition (on_success / on_failure) → a contract test. Every invariant → a property test. Where the parent Architecture interface contract carries an invariant ("ordering preserved", "idempotent under same key"), the leaf DD's `invariants:` field re-states it locally and the TestSpec's verification target points back. Surface gaps inline (`[NEEDS-TEST: ...]`) rather than emitting a DD that the matched TestSpec author skill cannot consume.

→ See `references/testspec-traceability-cues.md`

### Step 12 — Anti-pattern self-check

Sweep the document against the sixteen anti-patterns: five interface/contract (undefined-range precondition, implicit unit, implementation-leaking interface, silent null return, algorithmic postcondition); three error (no error strategy, exception swallowing, exception tunneling); three state/concurrency (designing for races, state explosion, missing cancellation); five AI-era (LLM confident invention, code paraphrase, test-as-spec inversion, happy-path bias, post-hoc DD).

→ See `references/anti-patterns.md`

### Step 13 — Run Quality Bar checklist + Spec Ambiguity Test

Run the Yes/No checklist. Items that cannot be answered Yes are flagged inline in the output, not silently passed. Apply the Spec Ambiguity Test as the meta-gate (override): a junior engineer must produce a correct implementation from the DD alone; a test engineer must derive unit tests without seeing the code; an equivalent implementation in a different language must satisfy the same DD.

→ See `references/quality-bar-checklist.md`

## Hard refusals (the four non-negotiables)

**A — Honest retrofit posture.** Refuse to:
- Mark the `Overview` field's `recovery_status` as `reconstructed` (schema enforces enum: `verified | unknown` only — Overview carries intent, which is human-only).
- Generate fabricated rationale on retrofit when no preserved decision record exists ("chosen for performance", "follows clean architecture", "balances concerns").
- Write committee-grade prose about decisions that cannot be sourced — refuse the form; output `unknown` with a follow-up owner instead.

Example refusal: a retrofit asks for rationale on a 30-minute session-token lifetime; the user has no preserved decision record and the original team is gone. Output: `rationale: { status: unknown, note: "no preserved design notes; observed lifetime in code and deployment config; forces driving the choice not recoverable; follow up with @owner" }` — never an invented "balances security against session ergonomics" string.

**B — DD-without-parent-Architecture is incomplete.** Refuse to:
- Author a DD when the parent Architecture is not supplied. The leaf does not exist in isolation; its responsibilities, sibling interfaces, and governing ADRs come from the parent. Authoring without that context produces designs that drift from the rest of the system and re-derive what the parent already fixed.
- Re-derive the parent Architecture's choices inside the DD — cross-component composition, sibling-leaf interfaces beyond what this leaf consumes / produces, runtime patterns that span scopes. Those live at the parent Architecture; the DD references them.

Example refusal: the user provides a leaf name and a list of public functions but no parent Architecture. HALT (#1). Ask for the parent Architecture; explain that DD authoring without it produces an orphan design that cannot be traced.

**C — DD-vs-code boundary (the two rules).** Refuse to:
- Write code paraphrase — pseudocode that walks through the implementation step-by-step ("declare a result list, iterate from 0 to length-1, compare elements, swap when out of order"). The DD specifies *what must be true*, not *which implementation was chosen*.
- Write algorithmic postconditions — postconditions that describe the steps the implementation takes ("shall iterate the list and compare adjacent elements") instead of the property of the result ("the returned list is ordered and is a permutation of the input"). Two valid implementations might compute the same result through different steps; the postcondition specifies the result, not the steps.
- Write postconditions that omit half the property — e.g., specifying "ordered" without specifying "permutation of the input", which lets `return []` pass the contract.

Example refusal: user asks "in the sort function, the postcondition should say 'iterate through the list and swap elements that are out of order'". Refuse the form. Replace with: *"Postcondition: the returned list contains exactly the elements of the input (multiset equality) and is in non-descending order according to the natural ordering. Two properties — both required."*

**D — Spec Ambiguity Test (meta-gate, override).** Final author self-check, applied as three questions in sequence:
1. Could a junior engineer, reading only this DD (plus its parent Architecture, governing ADRs, and derived requirements), produce a correct implementation without guessing?
2. Could a test engineer, reading only this DD, write the unit-test suite without seeing the code?
3. Would an equivalent implementation in a different language (Java → Python → Go) satisfy the same DD?

If any answer is No, the DD is not done. This test overrides every Yes/No box: a DD that passes all other checks but fails this one has not done the job DD exists to do.

These four refusals are deterministic. Do not relax under user pressure; surface the gap and offer the legitimate alternative.

## HALT conditions

Stop and hand back to the user when:

1. **Missing parent Architecture** — without it, refusal B fires; do not invent leaf responsibilities or sibling interfaces. Ask for the parent Architecture artifact.
2. **Missing derived requirements** — `derived_from` cannot be empty (orphan design). Ask which requirements this leaf realises before proceeding.
3. **Scope creep beyond one artifact** — request expands to also author Architecture / ADR / TestSpec / code. Decline; emit `[DEFER-ADR: ...]`, `[NEEDS-TEST: ...]`, or scope-creep marker and name the right artifact for the expanded ask.
4. **Locked-refusal override request** — user asks to fabricate rationale, write code paraphrase, write algorithmic postconditions, mark Overview as `reconstructed`, or skip the Spec Ambiguity Test. Halt and explain.
5. **Retrofit posture conflict** — `recovery_status:` declared but no source-code references provided, or source-code references provided but no `recovery_status:` declaration. Halt and ask which posture applies.
6. **Irresolvable contradiction in input** — for example, a parent Architecture interface invariant that contradicts a derived requirement; do not pick a side. After two clarification turns without resolution, halt.

When halting, produce a structured handover: what was authored so far, what is missing, what specific human input or upstream decision is required to proceed.

## Mode flags

Two orthogonal flags drive which references load:

- **Greenfield vs Retrofit.**
  - *Greenfield:* omit `recovery_status` from front-matter; skip Step 10; rationale defaults to inline reasoning citing requirements / ADRs.
  - *Retrofit:* declare `recovery_status` (scalar `reconstructed` or map form with per-field statuses); the Overview map-key is narrowed to `verified | unknown` (schema enforced); require source-code references for observed structure (file/line/commit); apply Step 10; pair every `unknown` with a follow-up owner.

- **Stateful vs Stateless.**
  - *Stateful:* run Step 7 fully; populate state inventory, transition table, undefined-event handling.
  - *Stateless:* Step 7 produces one line — *"This leaf is stateless between calls; all state lives in [where]."* Explicit assertion of absence; do not omit the section.

## Self-check before delivering

Before declaring the document complete, work through `references/quality-bar-checklist.md`. Items that cannot be answered Yes are flagged inline in the output, not silently passed. The Spec Ambiguity Test is the override gate.

## File layout produced by this skill

```
{output-path}/detailed_design.md
```

That's it — one file. The skill does not create directories, schemas, validators, or sibling artifacts.

## Pointers

- `references/authoring-discipline.md` — 6 cross-cutting rules (product-shape, layering, compression) — applies to all authoring steps
- `references/dd-purpose-and-shape.md` — purpose of DD, two rules (no duplication, specific enough), the box mental model, 7-section shape, junior-implementable / language-portable / test-derivable triple, parent-Architecture inputs
- `references/function-contracts.md` — Design-by-Contract (Hoare / Meyer / Liskov), 9 contract elements, units of measure, defensive programming vs DbC, signature-vs-contract distinction
- `references/data-structures-by-invariant.md` — fields-by-invariant, ownership, lifetime, returned-object semantics, shared-mutable as contract
- `references/algorithms.md` — result-property vs algorithm-selection, specification-pattern selection table, decision tables, postcondition discipline
- `references/state-and-concurrency.md` — state inventory, transition table, undefined-event handling, Goetz thread-safety taxonomy, timing constraints as testable
- `references/error-handling.md` — six questions, error-handling matrix, checked-vs-unchecked as contract, five strategies (fail-fast, retry, fallback, compensate, propagate)
- `references/rationale-capture.md` — inline rationale, ADR threshold, four constraint kinds (external, architectural, resource, temporal)
- `references/retrofit-discipline.md` — DD-specific retrofit (Overview narrowed to verified|unknown — schema-enforced), no-fabrication, observable-vs-inferred markings
- `references/adr-extraction-cues.md` — DD ↔ ADR seam: when DD-level decisions become ADRs, [DEFER-ADR] marker, governing_adrs reference pattern
- `references/testspec-traceability-cues.md` — DD ↔ TestSpec seam: error matrix → robustness tests, postconditions → contract tests, invariants → property tests
- `references/anti-patterns.md` — 16 anti-patterns (5 interface/contract, 3 error, 3 state/concurrency, 5 AI-era)
- `references/quality-bar-checklist.md` — 8 Quality Bar cards + Spec Ambiguity Test meta-gate
- `templates/detailed-design.md.tmpl` — full 7-section artifact scaffold
- `templates/public-interface-entry.yaml.tmpl` — DbC contract YAML (matches schema $defs)
- `templates/data-structure-entry.yaml.tmpl` — invariant-style data-structure YAML
- `templates/error-matrix-row.yaml.tmpl` — five-column matrix row YAML
- `templates/state-machine.mmd.tmpl` — Mermaid stateDiagram-v2 skeleton
- `templates/decision-table.md.tmpl` — markdown decision-table for rule-based behaviour
- `templates/governing-adr-reference.yaml.tmpl` — front-matter list + body-citation pattern
- `examples/good-dequeue-service.md` — worked example, all seven sections, greenfield, governing ADR
- `examples/bad-fabricated-retrofit.md` — counter-example, fabricated retrofit on a session-token validator with annotated tells
