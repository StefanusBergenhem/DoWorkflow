# Phase 2 Authoring Pattern

Captured 2026-04-19 after ADR (first Phase 2 artifact) completed and accepted. This doc encodes session-local decisions so fresh sessions can continue subsequent artifacts (Detailed Design → Product Brief → Requirements → Architecture → TestSpec) with the same quality.

**Load this alongside `CLAUDE.md`, `BACKLOG.md`, and `TARGET_ARCHITECTURE.md` at the start of every Phase 2 session.**

---

## 1. Research substrate discipline

Three-tier priority order. Do not skip tiers.

### Tier 1 — Engineering codex (primary, software-first)

Path: `/home/stefanus/repos/engineering-codex/`.

1. Read `CLAUDE.md` (schema) and `index.md` (topic index).
2. Read concept pages (`wiki/concepts/`) relevant to the artifact.
3. Read source pages (`wiki/sources/`) backing those concepts.

The codex is software-first by construction. Cite codex pages and their primary sources in the artifact's References section.

If the codex is thin on a needed topic, flag the gap; do not fabricate. The user may want to ingest more later — do not ingest in the current session unless explicitly asked.

### Tier 2 — `research/` with explicit safety-bias caveat

Path: `/home/stefanus/repos/VModelWorkflow/research/`.

These docs were authored pre-pivot with ASPICE / DO-178C / ISO 26262 framing. **Extract craft substance only; discard framing.** Do not let safety-standard phrasing bleed into output.

### Tier 3 — Existing `docs/guide/artifacts/*.html` (pre-pivot output, reference only)

Inspect for HTML template structure (CSS, header/sidebar/footer partials, section styling) and voice passages that carry. **Not content substrate.** Content is rewritten from Tiers 1–2 plus framework rules in `TARGET_ARCHITECTURE.md`.

---

## 2. Per-artifact doc structure (5 sections + References)

1. **V-model context** — what, where (positioning on the V), why the framework has it.
2. **Best practices** — craft principles for authoring.
3. **Anti-patterns** — common failure modes + concrete tells. Include retrofit/AI-era failures where applicable.
4. **Examples** — concrete good and bad. If retrofit/AI-era failure is relevant, include an AI-era bad example (typically side-by-side: fabricated vs. honest retrofit).
5. **Quality Bar** — structured Yes/No checklist grouped by concern. **Spec Ambiguity Test as meta-gate** (every checklist ends: could a junior engineer or low-mid-tier AI act on this without guessing?).

**References** — codex-backed citations; format consistent across artifact pages.

**Dropped from pre-pivot structure:** framework integration, AI skills integration. Those belong in tool / skill docs, not craft docs.

**Phase 2 scope:** HTML authoring only. Canonical Quality Bar YAML extraction is Phase 3.

---

## 3. Voice and framing rules (strict)

- **Direct software-engineering English.** No domain translation plugin references. Content stands in plain software terms.
- **No standards-defensive framing.** We built this framework. We chose its artifacts. We do not motivate inclusion by comparing to DO-178C / ASPICE / ISO 26262 / IEC 62304. Do not write "standards don't require this" or "this isn't mandated by X." Compliance is not the subject.
- **No "framework synthesis, not literature-backed" hedging.** If a rule is the framework's, state the rule and explain it directly.
- **No empirical claims beyond what sources support.** If ROI data doesn't exist, use qualitative arguments only. No "studies show…" without a cited study.
- **Teach craft to engineers.** Human-facing documentation. Information density over length — no length target, no padding.

---

## 4. Per-artifact flow (fresh session each)

1. **Explore** — substrate in the three-tier order above. Assess what the existing pre-pivot page carries vs. needs rewriting. May parallelize via subagents if substrate is sprawling.
2. **Propose** — summarize findings + a 5-section outline + gaps. Wait for user decisions.
3. **Write HTML** — author the new page; replace existing file. Dispatch to subagent with a thorough prompt capturing voice rules and outline.
4. **Review + iterate** — user reviews the rendered page; iterate until accepted. Update this pattern doc if new decisions surface.

---

## 5. What to remove from pre-pivot artifact pages

- All HALT-pattern cross-references (pre-pivot build-phase integration).
- Links to `skills-architecture.html#…` (page flagged stale per `TARGET_ARCHITECTURE §11`).
- `layer:` frontmatter field → replace with `scope_tags`.
- `provenance: recorded` → replace with `recovery_status`; human-only fields use `unknown`, never `reconstructed`.
- Old "Framework integration" and "AI skills integration" sections — drop entirely.
- Safety-tier language (DAL / ASIL / rigor-level references) — replaced by uniform Quality Bar.
- HW/SW split framing — gone post-pivot.
- Domain translation plugin invocations in new content (JS hooks, `<select>` language switchers, `data-term` attributes). Shared `domain.js` / `app.js` script tags may still be loaded at the page level; don't add new hooks. Full plugin removal is a separate Phase 2 task.

---

## 6. HTML conventions (established on ADR page)

- **Template:** inherit from other `docs/guide/artifacts/*.html` pages — same head, sidebar nav, footer, CSS.
- **Quality Bar rendering:** grouped concern cards (matches `.card` style used on `detailed-design.html`). Each card = one concern group with 2–4 Yes/No items. Give the Spec Ambiguity Test card a visual accent to mark its meta-gate status.
- **Examples rendering:** code blocks with inline `←` annotations marking failure points on bad examples.
- **Anti-patterns:** numbered list; each item is a 1–2 sentence failure mode + a concrete tell.
- **Navigation:** confirm the page is linked in `docs/guide/index.html`; add if missing.
- **References:** codex-backed citations at page end.

---

## 7. Conventions learned on ADR

- **Examples are realistic for the primary audience** — web-service engineering domains (Postgres, job queues, web APIs). Avoid safety-critical contexts in examples even when the primary market is aviation/automotive; those engineers still understand the web-service examples, and the reverse is not true.
- **Retrofit / AI-era bad examples** work best as a side-by-side: fabricated version + honest retrofit rewrite, inline-annotated with the tells (committee-style prose, neat rejection reasons, illegal `reconstructed` states on human-only fields).
- **Quantify example content** where it illustrates a point (e.g., "~1 engineer-week rollback"). Keep numbers clearly illustrative.
- **Name retrofit / AI-era anti-patterns specifically** (e.g., "LLM confident invention," "laundering the current state," "test-as-requirement inversion"). Vague warnings don't land.

---

## 8. Remaining artifact order

1. ~~ADR~~ — complete (2026-04-19).
2. **Detailed Design** — compression exercise; existing page has substantial carry-over; remove old Section 8 Test Strategy; reference TestSpec.
3. **Product Brief** — new; consolidates former `stakeholder-needs` + `conops` + `completeness-analysis` pages.
4. **Requirements** — new; EARS craft, rationale discipline, measurable QAs.
5. **Architecture** — new; decomposition, interfaces, mandatory Composition section.
6. **TestSpec** — new; derivation strategies, per-layer emphasis, coverage targets.

After each artifact is accepted, update this doc with any new pattern decisions.

---

## 9. Cross-cutting Phase 2 tasks (not per-artifact)

- Remove domain translation plugin machinery from `docs/guide/` (`js/domain.js` wiring, domain selector UI); archive `docs/guide/domains/*.json` to `archive/pre-pivot-2026-04-18/domains/`.
- Validate navigation linkage across all six artifact pages + `index.html`.
- Strip any residual pre-pivot framing across pages.

---

*Captured after ADR completion. Update as subsequent artifacts reveal new decisions.*
