# Phase 5 — Authoring Pattern & Session Handoff

Status document for Phase 5 (Skills — per-artifact author/review pairs, framework skills, the stakeholder-elicitation skill carried over from Phase 4 closeout). Mirrors the Phase 2 / Phase 3 / Phase 4 precedent (`archive/phase{2,3,4}/PHASE{N}_AUTHORING_PATTERN.md`, all archived on phase completion).

Load this file alongside `CLAUDE.md` + `BACKLOG.md` + `TARGET_ARCHITECTURE.md` at every Phase 5 session start. Archive this file to `archive/phase5/` on Phase 5 completion.

---

## 1. Status as of 2026-05-01 (builder-skill conformance audit complete; testspec pair next)

**Phase 5 goal:** Build per-artifact authoring + review skills (6 × 2 = 12 per-artifact skills), plus framework skills (orchestration, traceability, retrofit), plus the stakeholder-elicitation skill carried over from Phase 4 closeout, plus a rewrite of `docs/guide/skills-architecture.html` for the new 6-artifact model.

**Work landed:**

| Date | Step | What |
|---|---|---|
| 2026-04-27 | Pattern-setter pair | `vmodel-skill-author-requirements` (18 files, ~2071 lines) and `vmodel-skill-review-requirements` (14 files, ~1823 lines) authored under `.claude/skills/`. Both self-contained, framework-neutral, lean-fragile. The author skill encodes the EARS / NFR-five-elements / interface-five-dimensions / no-fabrication / no-smuggled-design disciplines. The review skill mirrors them as checks plus a structured-verdict (APPROVED / REJECTED / DESIGN_ISSUE) with deterministic gates and a stable `check_failed` identifier catalog. |
| 2026-04-29 | Phase 4 carryover | `vmodel-skill-elicit-needs` (17 files, ~1340 lines) authored under `.claude/skills/`. Renamed from `vmodel-skill-elicit-requirements` during authoring to align with INCOSE's Needs vs Requirements distinction (Stakeholder Real-World Expectations → Integrated Set of Needs → Design Input Requirements). Output is a rough `needs.md` in **prototype mode** — not a tracked framework artifact yet (decision γ — prototype before formalizing). Encodes a state-machine spine (ELICIT → DRAFT → READBACK → CONFIRM → COMMIT) with readback-for-joint-agreement as a fragile contract. Self-review surfaced 2 MAJOR findings (description over 1024-char cap; WRAP-UP state coherence drift), both fixed at landing. |
| 2026-04-30 | Phase 5 second pair | `vmodel-skill-author-architecture` (22 files, ~2270 lines) and `vmodel-skill-review-architecture` (17 files, ~1805 lines) authored under `.claude/skills/`. Pair total ~4075 lines (under Option-2 budget of ~4500). **12 references per side** (vs requirements pair's 9), reflecting architecture's broader surface (9 best-practice disciplines vs requirements' 5-or-so) — `protocols-sync-async` merged into `composition-patterns` per Option 2 of the size-trim discussion. Hard refusals A/B/C/D + Spec Ambiguity Test meta-gate. `check_failed` catalog of **10 `anti-pattern.*` + 58 `check.*` = 68 IDs**, partitioned across `anti-patterns-catalog.md` and `quality-bar-gate.md`. **13 hard-reject IDs**, **1 override** (`check.spec-ambiguity-test.fail`), rest soft-reject. Order-of-pairs deviation from the §3 Q2 recommendation: architecture taken before product-brief at user's call (rationale: rich-artifact stress-test of the locked pattern; pattern held). Self-review clean except documented exception — `quality-bar-gate.md` at 233 lines (over ~150 soft cap), justified by single-source-of-truth catalog density and acknowledged inline in the file at line 7 (decision A taken 2026-04-30: accept as documented exception; do not split). Architecture-specific seam captured in `references/adr-extraction-cues.md` + `references/adr-traceability-checks.md` — the `[NEEDS-ADR: <decision>]` stub mechanism formalises when an Architecture decision should be externalised to an ADR (load-bearing AND cross-cutting AND hard-to-reverse) and how the matched review skill verifies `governing_adrs:` resolution. |
| 2026-04-30 | Phase 5 third pair | `vmodel-skill-author-detailed-design` (22 files, ~2166 lines) and `vmodel-skill-review-detailed-design` (19 files, ~1954 lines) authored under `.claude/skills/`. Pair total ~4120 lines (under Option-2 budget of ~4500, comparable to architecture). **12 references per side** held; **7 templates** author-side, **2 templates** review-side; **2 examples** per side (matched pattern). Hard refusals A/B/C/D mirror across pair: A — Overview narrowed to `verified \| unknown` (schema-enforced); B — DD-without-parent-Architecture; C — code paraphrase / algorithmic postcondition / permutation-half-omitted (the "two halves rule"); D — Spec Ambiguity Test override. `check_failed` catalog ~16 anti-patterns + ~50 `check.*` IDs. Cross-artifact seams: Architecture → DD via leaf-allocation contract (refusal B); DD → TestSpec via dedicated `testspec-traceability-{cues,checks}.md` files (mirroring architecture's adr-extraction pattern). **Catalog-file exception**: review's `quality-bar-gate.md` at 238 lines, documented inline at file head per architecture-pair precedent. **Builder-skill drift discovered mid-build**: see §2.12 (new locked decision) and §3 Q6 (audit needed). |
| 2026-05-01 | Builder-skill conformance audit | All four shipped pairs/skills audited against `prompt-skill-agent-builder/references/{skill,anti-patterns}.md`. Audit pre-flight invoked the builder skill via `Skill` tool per §2.12. **Requirements pair**: 62 findings, all fixed (substantive prime-don't-teach pass on 18 sites + maxim/alternatives/duplication strip; structural pass added 13 TOCs, trimmed both descriptions under 1024 cap, added inline cap-exception block to `quality-bar-gate.md`); one tactical catalog superset (added `check.ears.non-conformant` to back rewritten alternatives fix); all 48 `check.*` + 16 `anti-pattern.*` IDs preserved. **Elicit-needs**: 9 findings, all fixed (small prime-don't-teach trims + 1 TOC); state machine, hard refusals, readback contract intact. **DD pair**: 4 findings, all fixed (description trims + project-path placeholder + canonical cap-exception wording); cleanest pair, no Phase A needed (refs already prime + slot motivation, no theory derivation). **Architecture pair**: 21 findings; structural fixes applied (10 TOCs, description trim 1065→908, canonical cap-exception wording, project-path placeholder); 9 audit-flagged "substantive" findings (Parnas/CAP/STRIDE/circuit-breaker/Ford/Hoare "re-derivation") **dismissed as false positives** after spot-check of `decomposition-discipline.md` and `data-and-persistence.md` — both files name + cite + give operational discriminator + slot, with zero theory derivation; the audit pattern-matched on keywords without inspecting structure. Identifier counts unchanged (58 `check.*` + 10 `anti-pattern.*`). Lesson: builder-skill anti-pattern matching needs verb-level inspection, not keyword matching, on subsequent audits. **§3 Q6 RESOLVED.** |

**Pending tasks (Phase 5 backlog):**
- [-] ~~`vmodel-skill-author-product-brief` and `vmodel-skill-review-product-brief`.~~ **DEFERRED INDEFINITELY 2026-04-30.** `needs.md` from `vmodel-skill-elicit-needs` will carry the root-scope upstream role for now. Re-evaluate alongside the elicit-needs decision γ (promote / merge / stay-transient) once pilot reps inform whether a formal Product Brief authoring skill is load-bearing or ceremonial. Framework still retains the Product Brief artifact type — only the authoring/review *skill pair* is skipped; if a formal PB is needed for a specific project, it can be hand-authored against the existing `docs/guide/artifacts/product-brief.html` craft doc + `schemas/artifacts/product-brief.schema.json`.
- [x] ~~`vmodel-skill-author-architecture` and `vmodel-skill-review-architecture`.~~ (landed 2026-04-30)
- [x] ~~`vmodel-skill-author-detailed-design` and `vmodel-skill-review-detailed-design`~~ (landed 2026-04-30; supersedes the paused C2–C4 DD skills)
- [x] ~~**Builder-skill conformance audit across the four shipped pairs**~~ (completed 2026-05-01; see §3 Q6 RESOLVED).
- [ ] `vmodel-skill-author-testspec` and `vmodel-skill-review-testspec`. **Recommended next** — see §4.
- [ ] `vmodel-skill-author-adr` and `vmodel-skill-review-adr`.
- [ ] `vmodel-skill-traceability` (framework).
- [ ] `vmodel-skill-orchestration` (framework).
- [ ] `vmodel-skill-retrofit` (framework — four-phase retrofit mode; enforces `recovery_status` discipline at the skill level).
- [ ] Rewrite `docs/guide/skills-architecture.html` for the 6-artifact model.

**Phase 5 closeout signal:** all 16 skills authored, each one's anti-pattern catalogue and check identifiers stable, the skills-architecture HTML rewritten, and `/skill-creator` Haiku-floor evaluations run (deferred — see §3 below).

---

## 2. Decisions Locked (do not reopen without cause)

These are the conventions established by the requirements author/review pair. Apply them uniformly to the remaining 5 per-artifact pairs, the 3 framework skills, and (with caveats) the elicitation skill.

### 2.1 Self-containment — content, not location

- **Every skill bundles its own copies** of the references, templates, examples, and quality-bar checklist it needs. No skill points out to `docs/guide/`, `schemas/`, or other framework directories. The skill works as a drop-in directory in any repo.
- **Self-contained is a content property, not a location property.** Skills install project-local at `/.claude/skills/<name>/`; users who want to use the skill in another repo copy the directory. The directory is portable because all content is inlined.

### 2.2 Framework-neutral body

- **Only the `name:` field carries the `vmodel-skill-` namespace marker.** Every other line of the skill body — descriptions, references, templates, examples — uses generic software-engineering vocabulary. No mention of "VModel", no project-specific paths, no schema file references.
- **Reasoning:** the user's primary use case is with the framework, but locking framework-specific text into the body adds zero value and removes optionality. Keeping the body neutral lets the skill be reused, copied, or adapted without a body-edit pass.

### 2.3 Lean-fragile DoF calibration

- **Default to fragile** (templates, decision tables, fill-in-the-blank slots, regex-checkable tells) for every sub-task that is failure-prone if left to model judgment.
- **Reserve heuristic** (principles only) for tasks that genuinely require domain judgment — glossary authoring, rationale content, the Spec Ambiguity Test meta-gate, perspective-based reading sweeps.
- **Reasoning:** self-contained skills must work on whatever model the host repo runs. Fragile templates work on cheaper models; principles need stronger ones. Lean fragile = predictable across model tiers.

### 2.4 Sister naming

- **Author / review pair convention.** `vmodel-skill-author-<artifact>` produces the artifact; `vmodel-skill-review-<artifact>` validates it. Each is a complete, separate skill — neither references the other by name in its body (the workflow split is described abstractly as "the matched author/review skill").

### 2.5 Project-local install

- **Path:** `/<repo>/.claude/skills/<name>/`. Matches the precedent of `derive-test-cases`, `develop-code`, `vmodel-skill-review-code`, and the requirements pair just landed.
- **Not `~/.claude/skills/`** — the `vmodel-` prefix is a project namespace; project-local keeps the skill versioned with the framework.

### 2.6 File layout — author skills

- **`SKILL.md`** ≤ ~300 lines (orchestration + hard refusals + HALT + pointers).
- **`references/`** — one file per craft discipline (typically 8–10 files, each ≤ ~150 lines, single-topic, with section headers). Content is rules + slot-fill templates + worked examples.
- **`templates/`** — fill-in-the-blank scaffolds, one per artifact-shape produced. The requirements author skill has 6: full document + per-type YAML stubs + glossary entry.
- **`examples/`** — paired good + bad. The bad example is load-bearing: hard refusals must be vivid.

### 2.7 File layout — review skills

- **`SKILL.md`** ≤ ~300 lines.
- **`references/`** — one file per check area (typically 8–10 files), framed as *"Check that …"*, *"Reject when …"*, *"Approve when …"*. Each finding has a stable `check_failed` identifier.
- **`templates/`** — only 2 needed: a verdict template and a per-finding template. Review emits structured outputs, not artifacts.
- **`examples/`** — paired good (correct APPROVED) + bad (counter-example showing reviewer failure modes: false approve, subjective reject, missed meta-gate).

### 2.8 Hard refusals (deterministic, non-negotiable)

Every author skill carries 1–3 hard refusals; every review skill carries the symmetric hard-reject triggers. For the requirements pair:

- **No fabricated rationale.** Author refuses to invent rationale; review hard-rejects on fabrication tells (`anti-pattern.fabricated-rationale`, `check.rationale.recovery-status-reconstructed`, `check.rationale.missing`).
- **No design smuggled into requirements.** Author refuses to write requirements naming technologies / libraries / algorithms / data structures (outside externally imposed interface protocols); review hard-rejects on `anti-pattern.implementation-prescription`.
- **Spec Ambiguity Test is the meta-gate.** Author runs it as a self-check; review applies it as the override gate.

For each new artifact, identify its hard refusals at design time. Pattern: every author skill's "do not do X" becomes the matching review skill's "hard-reject X".

### 2.9 Verdict format (review skills only)

- **Verdict:** APPROVED, REJECTED, or DESIGN_ISSUE.
- **Precedence:** DESIGN_ISSUE > REJECTED. If both fire, the verdict is DESIGN_ISSUE; findings list still contains all observed issues. Reasoning: DESIGN_ISSUE signals the path forward is upstream-fix, not rewrite.
- **Findings:** structured per `templates/finding.yaml.tmpl`. Required fields: `id`, `requirement_id` (or analogous artifact-component-id, or "GLOBAL"), `check_failed` (dotted catalog identifier — must appear in the canonical catalog), `severity` (hard_reject / soft_reject / info), `category`, `evidence`, `recommended_action`.
- **`recommended_action` discipline:** generic pointer to the rule; never specific replacement wording. The author skill rewrites; the review skill signals.

### 2.10 No scripts, no evals (yet)

- **No executable logic** in any skill. Schema validation, traceability validation, Quality Bar structural checks belong to the mechanical tools (Phase 6 — `vmodel-tool-validate` etc., per the framework's tool/skill split).
- **Evals deferred.** Per Phase 5 success criteria, every skill needs `/skill-creator` evaluation on Haiku at agreed thresholds. Not blocking individual skill landings; blocking Phase 5 closeout. See §3 open question Q1.

### 2.11 The two-skill batch — what the pattern-setter teaches

The requirements pair took ~3,900 lines across 32 files (18 author + 14 review). Per-pair budget for the remaining 5 author/review pairs is ~3,500–4,500 lines. Total Phase 5 estimate: ~25,000 lines across ~190 files (12 per-artifact + 1 elicitation + 3 framework + the HTML rewrite).

The author and review for one artifact are best done back-to-back in the same session, because the review's `check_failed` catalog mirrors the author's hard refusals and slot-fill templates. Splitting them across sessions costs context-loading overhead.

### 2.12 Builder skill is the primary authority — PHASE5 conventions LAYER ON TOP

**Locked 2026-04-30 after the DD pair build.**

Every Phase 5 skill pair must conform to the discipline encoded in the `prompt-skill-agent-builder` skill's `references/skill.md` and `references/anti-patterns.md`. Those files define the craft floor — frontmatter shape, reference-file caps with TOC requirement, prime-don't-teach, implementation intentions ("when X, do Y" not general maxims), no-alternatives-dumps, and the rest. PHASE5 §2 conventions (self-containment, framework-neutral body, hard-refusal ABCD pattern, verdict format) are project-specific *additions* on top of the builder's craft rules — they do not replace them.

**Mandatory at session start:**

1. When the user invokes `/prompt-skill-agent-builder` (or any skill via slash-prefix), call the `Skill` tool with that skill name **before** doing anything else. The slash-prefix is a tool invocation, not a textual reference.
2. Read the active type's reference (`references/skill.md`) and the universal anti-patterns (`references/anti-patterns.md`) before opening PHASE5_AUTHORING_PATTERN.md.
3. Walk the builder's 8-step flow (parse intent → type-mismatch check → load type ref → interview → termination → output path → self-review → eval scaffold). PHASE5 §2 layers on top of step 7 (self-review).
4. Run the builder's anti-pattern self-review against every reference, template, and example before declaring the pair done. Notably:
   - Reference files >100 lines need a Table of Contents.
   - "Prime, don't teach" — do not derive Hoare/Meyer/Liskov/Goetz from first principles; state the contract shape and let the model bring its prior.
   - "When X, do Y" implementation intentions, not "X is generally Y" maxims.
   - No alternatives dumps — pick a default plus one escape hatch.

**Why this needed to be written down:** the requirements / elicit-needs / architecture / DD pairs were authored without invoking the builder skill. PHASE5 §2 was extracted from the requirements pair which had the same blind spot, so the discipline drifted across all four pairs. The DD-pair build surfaced the drift mid-stream; this section is the structural fix so the next pair starts from the right authority.

---

## 3. Open Questions

### Q1 — When and how to run Haiku-floor evals

Phase 5 success criteria say each skill passes `/skill-creator` Haiku evaluation. The requirements pair was authored without evals — explicitly deferred. Three sub-questions:

- (a) Eval budget per skill: how many scenarios? Phase 5's earlier draft said 3+; the prompt-skill-agent-builder reference says 3 minimum.
- (b) Run evals incrementally (after each pair) or as a Phase 5 closeout pass?
- (c) Threshold for pass: pass-rate, qualitative judgement, both?

**Recommendation when reopened:** start incremental — run the requirements pair through Haiku eval first to surface concrete failure modes; let those failures shape the eval discipline for the remaining 14 skills.

### Q2 — Order of remaining pairs — RESOLVED 2026-04-30 (revised)

Six per-artifact pairs originally; **two landed** (requirements 2026-04-27, architecture 2026-04-30); **one deferred indefinitely** (product-brief — `needs.md` from elicit-needs carries root-scope upstream until pilot reps re-evaluate). Three remaining:
- `detailed-design` (junior-implementable; supersedes paused C2–C4 DD skills) — **next**
- `adr` (with Reversibility sub-prompt)
- `testspec` (with mandatory non-empty `verifies`)

**What the architecture pair taught:**
- The locked §2 pattern absorbs richer artifacts cleanly. 12 references / 7 templates / 2 examples per side held up; total pair size ~4075 lines (Option-2 budget honoured). The size-trim discussion (Option 1-4 in the build session) is a useful template for stress-testing the next pair-size pre-build.
- One genuine cap-violation site emerged: catalog files. `quality-bar-gate.md` at 233 lines is irreducible without splitting the single-source-of-truth catalog. Future pairs with comparable ID-density (likely DD given its breadth, ADR less so) should plan for the same exception up front rather than fight it in self-review.
- Cross-artifact seams (here: ADR ↔ Architecture via `[NEEDS-ADR: ...]` stub + `governing_adrs:` resolution check) work well as dedicated reference files (`adr-extraction-cues.md` author-side + `adr-traceability-checks.md` review-side) rather than inline in other references. Apply this pattern to other seams: ADR ↔ Detailed Design (via `governing_adrs` from DD), TestSpec ↔ everything (via `verifies` traceability), Architecture ↔ Detailed Design (via the leaf-allocation contract).

**Why detailed-design next (user call 2026-04-30):**
- Product-brief deferred — see §1 pending tasks for rationale.
- Detailed-design is the largest, most consequential remaining pair: it specifies leaf-scope artifacts that AI agents implement against; DD craft quality is the bottleneck for AI-augmented development workflows in the framework's primary use case.
- Pre-pivot C2–C4 DD skills are paused waiting for replacement; the new author/review pair takes their content into account. There is also pre-pivot doc material (`docs/guide/artifacts/detailed-design.html` — already updated in Phase 2) and three operational sister skills (`develop-code`, `derive-test-cases`, `vmodel-skill-review-code`) whose seams the new DD pair must understand.
- Stress-test for the locked pattern: DD has the broadest surface (function-by-function specification at junior-implementable level); will it stay within Option 2 budget or force a third file-count exception? Useful pattern signal.

**Order for the remaining two after DD:** `testspec` (rigid `verifies` requirement is mechanically checkable; smallest of the three); `adr` last (Reversibility sub-prompt is a unique sub-shape; ADR ↔ Architecture seam is already partially specified by the architecture-side, so the ADR pair is mostly anchored).

### Q3 — When to do the elicitation skill — RESOLVED 2026-04-29

`vmodel-skill-elicit-needs` (renamed during authoring from `vmodel-skill-elicit-requirements`) landed in a single-skill session as recommended. The single-session approach was correct — the design surfaced two non-trivial architectural decisions during the interview (output type vs Requirements; positioning relative to the 6-artifact set) that would have been costly to resolve under per-artifact-pair batching pressure. Decision γ (prototype-mode `needs.md`, no upstream commitment to formalization) preserves optionality until pilot reps inform the choice between (α) new 7th artifact, (β) merger into Product Brief, or (γ) staying as transient elicitation output. Self-review found the skill clean against Phase 5 §2 conventions; only minor description-cap and state-coherence drifts surfaced and were fixed at landing.

### Q4 — Framework skills

`vmodel-skill-traceability`, `vmodel-skill-orchestration`, `vmodel-skill-retrofit` are operationally distinct from the per-artifact craft pairs. They orchestrate, validate cross-artifact links, and enforce process-level rules (e.g., the retrofit skill's no-fabrication enforcement at the skill-dispatcher level).

**Recommendation:** defer until at least 3 per-artifact pairs are done, so the framework skills have a stable surface to orchestrate over.

### Q5 — Skills-architecture HTML rewrite

Pre-pivot version is stale. Should describe the post-pivot 6-artifact model and the author/review pair convention.

**Recommendation:** defer to Phase 5 closeout. The HTML page documents skills; documenting them while they are in flux costs more than it earns.

### Q6 — Builder-skill conformance audit — RESOLVED 2026-05-01

Audit ran across all four shipped pairs/skills with builder-skill pre-flight per §2.12. See §1 status row dated 2026-05-01 for full per-pair counts. Headline:

- **96 audit findings across the four** (62 requirements / 9 elicit-needs / 21 architecture / 4 DD).
- **87 fixed**: structural (TOCs, description trims, canonical cap-exception wording, project-path placeholders) + substantive (prime-don't-teach compression, maxim → "when X, do Y", alternatives → default + escape hatch, duplication strip).
- **9 dismissed** (architecture-pair only): audit pattern-matched on keywords ("Parnas", "CAP", "STRIDE", "circuit-breaker", "Hoare") and flagged "re-derivation" without inspecting structure. Spot-check confirmed those refs are already prime-by-name + operational discriminator + slot-fill — the textbook prime-don't-teach shape.
- **Catalog integrity preserved**: zero `check.*` or `anti-pattern.*` IDs removed across the four; one tactical addition (`check.ears.non-conformant` in requirements review pair) to back a rewritten alternatives fix.
- **Hard refusals preserved** (A/B/C/D in architecture and DD pairs; 3 in elicit-needs; 2 in requirements pairs).

**Lessons for future audits:**

1. The audit subagent's first-pass anti-pattern detection is keyword-biased. Always have the fix subagent re-validate substantive findings against actual file structure before applying.
2. Each subagent prompt must include the explicit licence to dismiss false positives with reasoning, not "skip and flag" them as deferred (which is what the architecture audit did).
3. The cap-exception block on `quality-bar-gate.md` is now standardised across requirements / architecture / DD review pairs (canonical "~150-line soft cap" wording).
4. `prompt-skill-agent-builder` should be invoked via `Skill` tool at audit pre-flight, not just for skill creation. Confirmed in §2.12.

---

## 4. Recommended Next Step

**Build the testspec pair** (`vmodel-skill-author-testspec` + `vmodel-skill-review-testspec`). The four shipped pairs/skills are now audit-conformant against `prompt-skill-agent-builder` (see §3 Q6 RESOLVED). The testspec pair is the smaller of the two remaining (rigid `verifies` requirement is mechanically checkable; ADR pair is partially anchored by architecture-side already). ADR last.

**Pre-flight (mandatory per §2.12):**

1. **Invoke `prompt-skill-agent-builder` via the Skill tool** at session start.
2. **Read** `references/skill.md` and `references/anti-patterns.md` from that skill before reading anything else.
3. PHASE5 §2 conventions layer on top of the builder's craft floor — they do not replace it.

**Pre-build checklist for the testspec pair:**
- Read `docs/guide/artifacts/testspec.html` — Phase 2 craft doc.
- Read `schemas/artifacts/test-spec.schema.json` and `schemas/artifacts/quality-bar/test-spec.quality-bar.json`.
- Cross-artifact seams to specify: TestSpec ↔ DD (every error-matrix row → robustness test; postconditions → contract tests; invariants → property tests — already partially specified by `testspec-traceability-{cues,checks}.md` in the DD pair); TestSpec ↔ Architecture (interface contracts → integration tests); TestSpec ↔ Requirements (every requirement → at least one verification target via `verifies`).
- Likely hard refusals: *(A)* honest retrofit posture on rationale; *(B)* `verifies` field non-empty per schema (testspec without verification targets is decoration); *(C)* every test case has an oracle (the assertion is named, not "verify behaviour"); *(D)* Spec Ambiguity Test meta-gate.

If pattern stress is observed, surface the deviation explicitly before locking.
