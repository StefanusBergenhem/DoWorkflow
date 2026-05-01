# Phase 5 — Authoring Pattern & Session Handoff

Status document for Phase 5 (Skills — per-artifact author/review pairs, framework skills, the stakeholder-elicitation skill carried over from Phase 4 closeout). Mirrors the Phase 2 / Phase 3 / Phase 4 precedent (`archive/phase{2,3,4}/PHASE{N}_AUTHORING_PATTERN.md`, all archived on phase completion).

Load this file alongside `CLAUDE.md` + `BACKLOG.md` + `TARGET_ARCHITECTURE.md` at every Phase 5 session start. Archive this file to `archive/phase5/` on Phase 5 completion.

---

## 1. Status as of 2026-05-01 (testspec pair landed; ADR pair is the only remaining per-artifact pair)

**Phase 5 goal:** Build per-artifact authoring + review skills (6 × 2 = 12 per-artifact skills), plus framework skills (orchestration, traceability, retrofit), plus the stakeholder-elicitation skill carried over from Phase 4 closeout, plus a rewrite of `docs/guide/skills-architecture.html` for the new 6-artifact model.

**Work landed:**

| Date | Step | What |
|---|---|---|
| 2026-04-27 | Pattern-setter pair | `vmodel-skill-author-requirements` (18 files, ~2071 lines) and `vmodel-skill-review-requirements` (14 files, ~1823 lines) authored under `.claude/skills/`. Both self-contained, framework-neutral, lean-fragile. The author skill encodes the EARS / NFR-five-elements / interface-five-dimensions / no-fabrication / no-smuggled-design disciplines. The review skill mirrors them as checks plus a structured-verdict (APPROVED / REJECTED / DESIGN_ISSUE) with deterministic gates and a stable `check_failed` identifier catalog. |
| 2026-04-29 | Phase 4 carryover | `vmodel-skill-elicit-needs` (17 files, ~1340 lines) authored under `.claude/skills/`. Renamed from `vmodel-skill-elicit-requirements` during authoring to align with INCOSE's Needs vs Requirements distinction (Stakeholder Real-World Expectations → Integrated Set of Needs → Design Input Requirements). Output is a rough `needs.md` in **prototype mode** — not a tracked framework artifact yet (decision γ — prototype before formalizing). Encodes a state-machine spine (ELICIT → DRAFT → READBACK → CONFIRM → COMMIT) with readback-for-joint-agreement as a fragile contract. Self-review surfaced 2 MAJOR findings (description over 1024-char cap; WRAP-UP state coherence drift), both fixed at landing. |
| 2026-04-30 | Phase 5 second pair | `vmodel-skill-author-architecture` (22 files, ~2270 lines) and `vmodel-skill-review-architecture` (17 files, ~1805 lines) authored under `.claude/skills/`. Pair total ~4075 lines (under Option-2 budget of ~4500). **12 references per side** (vs requirements pair's 9), reflecting architecture's broader surface (9 best-practice disciplines vs requirements' 5-or-so) — `protocols-sync-async` merged into `composition-patterns` per Option 2 of the size-trim discussion. Hard refusals A/B/C/D + Spec Ambiguity Test meta-gate. `check_failed` catalog of **10 `anti-pattern.*` + 58 `check.*` = 68 IDs**, partitioned across `anti-patterns-catalog.md` and `quality-bar-gate.md`. **13 hard-reject IDs**, **1 override** (`check.spec-ambiguity-test.fail`), rest soft-reject. Order-of-pairs deviation from the §3 Q2 recommendation: architecture taken before product-brief at user's call (rationale: rich-artifact stress-test of the locked pattern; pattern held). Self-review clean except documented exception — `quality-bar-gate.md` at 233 lines (over ~150 soft cap), justified by single-source-of-truth catalog density and acknowledged inline in the file at line 7 (decision A taken 2026-04-30: accept as documented exception; do not split). Architecture-specific seam captured in `references/adr-extraction-cues.md` + `references/adr-traceability-checks.md` — the `[NEEDS-ADR: <decision>]` stub mechanism formalises when an Architecture decision should be externalised to an ADR (load-bearing AND cross-cutting AND hard-to-reverse) and how the matched review skill verifies `governing_adrs:` resolution. |
| 2026-04-30 | Phase 5 third pair | `vmodel-skill-author-detailed-design` (22 files, ~2166 lines) and `vmodel-skill-review-detailed-design` (19 files, ~1954 lines) authored under `.claude/skills/`. Pair total ~4120 lines (under Option-2 budget of ~4500, comparable to architecture). **12 references per side** held; **7 templates** author-side, **2 templates** review-side; **2 examples** per side (matched pattern). Hard refusals A/B/C/D mirror across pair: A — Overview narrowed to `verified \| unknown` (schema-enforced); B — DD-without-parent-Architecture; C — code paraphrase / algorithmic postcondition / permutation-half-omitted (the "two halves rule"); D — Spec Ambiguity Test override. `check_failed` catalog ~16 anti-patterns + ~50 `check.*` IDs. Cross-artifact seams: Architecture → DD via leaf-allocation contract (refusal B); DD → TestSpec via dedicated `testspec-traceability-{cues,checks}.md` files (mirroring architecture's adr-extraction pattern). **Catalog-file exception**: review's `quality-bar-gate.md` at 238 lines, documented inline at file head per architecture-pair precedent. **Builder-skill drift discovered mid-build**: see §2.12 (new locked decision) and §3 Q6 (audit needed). |
| 2026-05-01 | Builder-skill conformance audit | All four shipped pairs/skills audited against `prompt-skill-agent-builder/references/{skill,anti-patterns}.md`. Audit pre-flight invoked the builder skill via `Skill` tool per §2.12. **Requirements pair**: 62 findings, all fixed (substantive prime-don't-teach pass on 18 sites + maxim/alternatives/duplication strip; structural pass added 13 TOCs, trimmed both descriptions under 1024 cap, added inline cap-exception block to `quality-bar-gate.md`); one tactical catalog superset (added `check.ears.non-conformant` to back rewritten alternatives fix); all 48 `check.*` + 16 `anti-pattern.*` IDs preserved. **Elicit-needs**: 9 findings, all fixed (small prime-don't-teach trims + 1 TOC); state machine, hard refusals, readback contract intact. **DD pair**: 4 findings, all fixed (description trims + project-path placeholder + canonical cap-exception wording); cleanest pair, no Phase A needed (refs already prime + slot motivation, no theory derivation). **Architecture pair**: 21 findings; structural fixes applied (10 TOCs, description trim 1065→908, canonical cap-exception wording, project-path placeholder); 9 audit-flagged "substantive" findings (Parnas/CAP/STRIDE/circuit-breaker/Ford/Hoare "re-derivation") **dismissed as false positives** after spot-check of `decomposition-discipline.md` and `data-and-persistence.md` — both files name + cite + give operational discriminator + slot, with zero theory derivation; the audit pattern-matched on keywords without inspecting structure. Identifier counts unchanged (58 `check.*` + 10 `anti-pattern.*`). Lesson: builder-skill anti-pattern matching needs verb-level inspection, not keyword matching, on subsequent audits. **§3 Q6 RESOLVED.** |
| 2026-05-01 | Phase 5 fourth pair | `vmodel-skill-author-testspec` (23 files, ~2162 lines) and `vmodel-skill-review-testspec` (20 files, ~1783 lines) authored under `.claude/skills/`. Pair total ~3945 lines. Pre-flight invoked `prompt-skill-agent-builder` via Skill tool per §2.12; build proceeded against a single `/tmp/testspec-pair-contract.md` consumed by two parallel build subagents. **14 author references + 15 review references** — deviation from the four-prior-pair pattern of 9–12 per side, justified by **three upstream seams** (DD ↔ leaf, Architecture ↔ branch, Requirements + Product Brief ↔ root) vs DD's single downstream seam to TestSpec. Hard refusals A/B/C/D: A — fabricated retrofit intent on `title`/`notes` (anti-pattern 13); B — `verifies` empty or unresolvable at artifact or case level (anti-pattern 12); C — weak/unbounded oracle on `expected` (anti-pattern 5); D — Spec Ambiguity Test override. **One derived-hard reject beyond A/B/C/D**: `check.coverage-mutation.section-missing` — the load-bearing QB group flag forces structural enforcement (the *bar* must be declared; *values* are project-policy and not prescribed). This is one extra hard-reject ID, not a fifth hard refusal. `check_failed` catalog: **13 `anti-pattern.*` + 53 `check.*` = 66 IDs**, partitioned across `anti-patterns-catalog.md` and `quality-bar-gate.md`. Catalog-file exception held: review's `quality-bar-gate.md` at ~250 lines with canonical cap-exception block. **Self-review**: 0 findings on author side; 1 MAJOR + 3 minor on review side. The MAJOR was a self-violation — SKILL.md cited two `check_failed` IDs (`check.adr.governing-not-resolved`, `check.derived-from.unresolvable`) that were not in the canonical catalog; fixed by adding both as new front-matter-reference-integrity sections (both hard-reject). Three minors fixed: cross-pair example coupling clarified with illustrative-only notes, "too many collaborators" prose tightened to numeric bound, "many codebases" tightened to the empirical 100% line / ~5% mutation figure. Cross-artifact seam files: `dd-traceability-cues.md` + `dd-traceability-checks.md` (mirroring DD pair's `testspec-traceability-{cues,checks}.md` from inverse direction); `architecture-traceability-{cues,checks}.md`; `requirements-traceability-{cues,checks}.md`. |

**Pending tasks (Phase 5 backlog):**
- [-] ~~`vmodel-skill-author-product-brief` and `vmodel-skill-review-product-brief`.~~ **DEFERRED INDEFINITELY 2026-04-30.** `needs.md` from `vmodel-skill-elicit-needs` will carry the root-scope upstream role for now. Re-evaluate alongside the elicit-needs decision γ (promote / merge / stay-transient) once pilot reps inform whether a formal Product Brief authoring skill is load-bearing or ceremonial. Framework still retains the Product Brief artifact type — only the authoring/review *skill pair* is skipped; if a formal PB is needed for a specific project, it can be hand-authored against the existing `docs/guide/artifacts/product-brief.html` craft doc + `schemas/artifacts/product-brief.schema.json`.
- [x] ~~`vmodel-skill-author-architecture` and `vmodel-skill-review-architecture`.~~ (landed 2026-04-30)
- [x] ~~`vmodel-skill-author-detailed-design` and `vmodel-skill-review-detailed-design`~~ (landed 2026-04-30; supersedes the paused C2–C4 DD skills)
- [x] ~~**Builder-skill conformance audit across the four shipped pairs**~~ (completed 2026-05-01; see §3 Q6 RESOLVED).
- [x] ~~`vmodel-skill-author-testspec` and `vmodel-skill-review-testspec`~~ (landed 2026-05-01)
- [ ] `vmodel-skill-author-adr` and `vmodel-skill-review-adr`. **Recommended next** — see §4.
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

### Q2 — Order of remaining pairs — RESOLVED 2026-05-01 (final)

Six per-artifact pairs originally; **four landed** (requirements 2026-04-27, architecture 2026-04-30, detailed-design 2026-04-30, testspec 2026-05-01); **one deferred indefinitely** (product-brief — `needs.md` from elicit-needs carries root-scope upstream until pilot reps re-evaluate). One remaining:
- `adr` (with Reversibility sub-prompt) — **next and last per-artifact pair**

**What the testspec pair taught:**
- **Three upstream seams force higher reference-count.** Testspec has DD ↔ leaf, Architecture ↔ branch, Requirements + Product Brief ↔ root. Each got its own dedicated cues/checks file pair. Reference counts: 14 author / 15 review (vs DD pair's 11/11). The locked pattern absorbs the deviation cleanly when each seam is its own single-topic file. ADR pair will be the inverse — fewer seams (ADR-as-laterally-cross-cutting; primary seam is governing_adrs resolution from Architecture/DD/TestSpec inward), so expect lower ref count.
- **One derived-hard reject beyond A/B/C/D is structurally legitimate.** The load-bearing flag on the QB coverage-mutation group forced `check.coverage-mutation.section-missing` as hard-reject (the *bar* must be declared; *values* are project-policy). This is one extra hard ID, not a fifth hard refusal — the slot is structurally required, the values are not prescribed. Pattern: when QB JSON marks a group `load_bearing: true`, encode structural enforcement at the framework level and value enforcement at the project level.
- **Build contract as `/tmp/<pair>-contract.md` worked well for parallel build subagents.** Single source of truth consumed by two parallel build subagents (one author, one review). Catalog ID alignment held; the only catalog drift was a 5-extra-IDs minor (review side adding `check.integration.*` and `check.retrofit.gap-report-missing` to back §6-mandated check files), which was justifiable in-scope and accepted at build time.
- **Self-violation by SKILL.md cite-without-catalog-add is a real risk.** Review skill's SKILL.md cited `check.adr.governing-not-resolved` and `check.derived-from.unresolvable` in a "Broken-reference integrity" section, but neither was in the canonical catalog. Self-review caught it. **Lesson for ADR pair**: every `check_failed` mentioned anywhere in the skill must be added to `quality-bar-gate.md` at write time, not after-the-fact.
- **Catalog-file exception held**: review's `quality-bar-gate.md` at ~250 lines, canonical cap-exception wording. Confirms the architecture/DD/testspec pattern. ADR pair likely has lower ID density (Reversibility sub-prompt + governing_adrs resolution + retrofit honesty form the bulk; smaller catalog) and may not need the exception — but plan for it just in case.

**Why ADR last:**
- Smallest catalog surface among per-artifact pairs (Reversibility sub-prompt is unique but not multi-discipline).
- Inward-facing seams: ADR is what `governing_adrs:` cites *into*, so the seam is already specified from the architecture/DD/testspec side. The ADR pair specifies the artifact itself + the resolution check from ADR's own perspective.
- No new pattern-stress expected; architecture pair laid the `[NEEDS-ADR: <decision>]` stub mechanism and three subsequent pairs have built on it.

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

**Build the ADR pair** (`vmodel-skill-author-adr` + `vmodel-skill-review-adr`). With the testspec pair landed (2026-05-01), this is the only remaining per-artifact author/review pair. After ADR, Phase 5 turns to the three framework skills (traceability, orchestration, retrofit), the deferred Haiku-floor evaluations, and the `docs/guide/skills-architecture.html` rewrite.

**Pre-flight (mandatory per §2.12):**

1. **Invoke `prompt-skill-agent-builder` via the Skill tool** at session start.
2. **Read** `references/skill.md` and `references/anti-patterns.md` from that skill before reading anything else.
3. PHASE5 §2 conventions layer on top of the builder's craft floor — they do not replace it.

**Pre-build checklist for the ADR pair:**
- Read `docs/guide/artifacts/adr.html` — Phase 2 craft doc.
- Read `schemas/artifacts/adr.schema.json` and `schemas/artifacts/quality-bar/adr.quality-bar.json`.
- The ADR ↔ Architecture seam is already specified from the architecture side (`adr-extraction-cues.md` + `adr-traceability-checks.md` + the `[NEEDS-ADR: <decision>]` stub mechanism). Author the ADR pair from the inverse perspective: ADR is what gets created when an extraction cue fires; review enforces ADR shape and confirms the resolution back to citing artifacts (Architecture, DD, TestSpec via `governing_adrs:`).
- Likely hard refusals: *(A)* honest retrofit posture (no fabricated rationale; `recovery_status: unknown` on reconstructed decision context); *(B)* Reversibility sub-prompt non-empty (an ADR without a documented reversibility analysis is decoration); *(C)* Decision and Consequences both present and not-empty (the load-bearing pair; an ADR missing either is incomplete); *(D)* Spec Ambiguity Test meta-gate.
- Likely smaller catalog than testspec (ADR is a single-decision artifact with a tighter surface). Plan for ~10 references / side, ~50 IDs, possibly no catalog cap-exception. If `quality-bar-gate.md` does cross 150 lines, use the canonical cap-exception block per §1.7 of the contract pattern.
- **At write time, every `check_failed` mentioned anywhere in the skill must be added to `quality-bar-gate.md`** — testspec-pair self-review caught a self-violation where SKILL.md cited two IDs not in the catalog. Audit the catalog against every reference file and SKILL.md before declaring the build done.

**Build pattern proven across four pairs:** draft a single `/tmp/adr-pair-contract.md` source-of-truth, dispatch two parallel build subagents (author + review), run builder-skill self-review, apply fixes inline. Pair budget likely ~3,000–3,500 lines (smaller than the testspec pair's ~3,945).

If pattern stress is observed, surface the deviation explicitly before locking.
