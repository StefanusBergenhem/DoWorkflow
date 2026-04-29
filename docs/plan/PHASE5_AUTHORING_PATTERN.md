# Phase 5 — Authoring Pattern & Session Handoff

Status document for Phase 5 (Skills — per-artifact author/review pairs, framework skills, the stakeholder-elicitation skill carried over from Phase 4 closeout). Mirrors the Phase 2 / Phase 3 / Phase 4 precedent (`archive/phase{2,3,4}/PHASE{N}_AUTHORING_PATTERN.md`, all archived on phase completion).

Load this file alongside `CLAUDE.md` + `BACKLOG.md` + `TARGET_ARCHITECTURE.md` at every Phase 5 session start. Archive this file to `archive/phase5/` on Phase 5 completion.

---

## 1. Status as of 2026-04-29 (pattern locked + elicit-needs landed)

**Phase 5 goal:** Build per-artifact authoring + review skills (6 × 2 = 12 per-artifact skills), plus framework skills (orchestration, traceability, retrofit), plus the stakeholder-elicitation skill carried over from Phase 4 closeout, plus a rewrite of `docs/guide/skills-architecture.html` for the new 6-artifact model.

**Work landed:**

| Date | Step | What |
|---|---|---|
| 2026-04-27 | Pattern-setter pair | `vmodel-skill-author-requirements` (18 files, ~2071 lines) and `vmodel-skill-review-requirements` (14 files, ~1823 lines) authored under `.claude/skills/`. Both self-contained, framework-neutral, lean-fragile. The author skill encodes the EARS / NFR-five-elements / interface-five-dimensions / no-fabrication / no-smuggled-design disciplines. The review skill mirrors them as checks plus a structured-verdict (APPROVED / REJECTED / DESIGN_ISSUE) with deterministic gates and a stable `check_failed` identifier catalog. |
| 2026-04-29 | Phase 4 carryover | `vmodel-skill-elicit-needs` (17 files, ~1340 lines) authored under `.claude/skills/`. Renamed from `vmodel-skill-elicit-requirements` during authoring to align with INCOSE's Needs vs Requirements distinction (Stakeholder Real-World Expectations → Integrated Set of Needs → Design Input Requirements). Output is a rough `needs.md` in **prototype mode** — not a tracked framework artifact yet (decision γ — prototype before formalizing). Encodes a state-machine spine (ELICIT → DRAFT → READBACK → CONFIRM → COMMIT) with readback-for-joint-agreement as a fragile contract. Self-review surfaced 2 MAJOR findings (description over 1024-char cap; WRAP-UP state coherence drift), both fixed at landing. |

**Pending tasks (Phase 5 backlog):**
- [ ] `vmodel-skill-author-product-brief` and `vmodel-skill-review-product-brief`.
- [ ] `vmodel-skill-author-architecture` and `vmodel-skill-review-architecture`.
- [ ] `vmodel-skill-author-adr` and `vmodel-skill-review-adr`.
- [ ] `vmodel-skill-author-detailed-design` and `vmodel-skill-review-detailed-design` (supersedes the paused C2–C4 DD skills).
- [ ] `vmodel-skill-author-testspec` and `vmodel-skill-review-testspec`.
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

---

## 3. Open Questions

### Q1 — When and how to run Haiku-floor evals

Phase 5 success criteria say each skill passes `/skill-creator` Haiku evaluation. The requirements pair was authored without evals — explicitly deferred. Three sub-questions:

- (a) Eval budget per skill: how many scenarios? Phase 5's earlier draft said 3+; the prompt-skill-agent-builder reference says 3 minimum.
- (b) Run evals incrementally (after each pair) or as a Phase 5 closeout pass?
- (c) Threshold for pass: pass-rate, qualitative judgement, both?

**Recommendation when reopened:** start incremental — run the requirements pair through Haiku eval first to surface concrete failure modes; let those failures shape the eval discipline for the remaining 14 skills.

### Q2 — Order of remaining pairs

Six per-artifact pairs total; one done. The remaining five:
- `product-brief` (root-only artifact; smallest, most templated)
- `architecture` (with mandatory Composition section)
- `adr` (with Reversibility sub-prompt)
- `detailed-design` (junior-implementable; supersedes paused C2–C4)
- `testspec` (with mandatory non-empty `verifies`)

**Recommendation:** product-brief next (simplest, lowest risk; further validates the pattern). Then `testspec` (rigid `verifies` requirement is mechanically checkable, low novelty). Then `architecture` + `adr` together (cross-cutting; Composition / Reversibility have novel sub-shapes that benefit from co-authoring). Detailed-design last (largest scope per artifact; superseding C2–C4 introduces migration concerns).

### Q3 — When to do the elicitation skill — RESOLVED 2026-04-29

`vmodel-skill-elicit-needs` (renamed during authoring from `vmodel-skill-elicit-requirements`) landed in a single-skill session as recommended. The single-session approach was correct — the design surfaced two non-trivial architectural decisions during the interview (output type vs Requirements; positioning relative to the 6-artifact set) that would have been costly to resolve under per-artifact-pair batching pressure. Decision γ (prototype-mode `needs.md`, no upstream commitment to formalization) preserves optionality until pilot reps inform the choice between (α) new 7th artifact, (β) merger into Product Brief, or (γ) staying as transient elicitation output. Self-review found the skill clean against Phase 5 §2 conventions; only minor description-cap and state-coherence drifts surfaced and were fixed at landing.

### Q4 — Framework skills

`vmodel-skill-traceability`, `vmodel-skill-orchestration`, `vmodel-skill-retrofit` are operationally distinct from the per-artifact craft pairs. They orchestrate, validate cross-artifact links, and enforce process-level rules (e.g., the retrofit skill's no-fabrication enforcement at the skill-dispatcher level).

**Recommendation:** defer until at least 3 per-artifact pairs are done, so the framework skills have a stable surface to orchestrate over.

### Q5 — Skills-architecture HTML rewrite

Pre-pivot version is stale. Should describe the post-pivot 6-artifact model and the author/review pair convention.

**Recommendation:** defer to Phase 5 closeout. The HTML page documents skills; documenting them while they are in flux costs more than it earns.

---

## 4. Recommended Next Step

**Author the `product-brief` pair** (`vmodel-skill-author-product-brief` + `vmodel-skill-review-product-brief`) in a single session, following the pattern locked in §2. Expected size: ~3,000–4,000 lines across ~26–30 files. Pattern-validation — if the pair lands without significant deviation from §2, the pattern is robust enough to template the remaining four per-artifact pairs.

If pattern stress is observed (e.g., product-brief shape doesn't fit the 5-section requirements layout), surface the deviation explicitly in this handoff doc before locking it.
