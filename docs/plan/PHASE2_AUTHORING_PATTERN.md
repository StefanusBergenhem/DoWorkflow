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

1. **Explore** — substrate in the three-tier order above. Assess what the existing pre-pivot page carries vs. needs rewriting. Parallelize via subagents when substrate is sprawling (codex + `research/` + existing HTML scouts run well in parallel).
2. **Generalist-gap audit** — after substrate is digested and *before* the outline is proposed, step back from the three-tier substrate and ask: *from a general software-development perspective, what is missing that a working software engineer authoring this artifact would expect?* All three substrate tiers are V-model / spec-driven-dev biased by construction; the audit is the place to catch blindspots that would make the page read as framework-fluent but industry-naive. Surface each candidate gap with a recommend-include / skip-with-reason / deeper-treatment call; bring them into the propose step alongside the substrate findings. Learned on Requirements (2026-04-20): the audit surfaced 8 substantive gaps (user stories ↔ requirements bridge, glossary / ubiquitous language discipline, type taxonomy as explicit teaching, constraints craft, compliance-as-constraints, EARS-vs-GWT positioning, negative-requirement nuance, prioritisation as attribute) that none of the three substrate tiers flagged. Without the audit these would have shipped missing.
3. **Propose** — summarize findings + a 5-section outline + gaps (both codex-substrate gaps and generalist gaps). Wait for user decisions.
4. **Write HTML** — author the new page **in the main context**; replace the existing file. **Do not dispatch HTML authoring to subagents.** Learned on Detailed Design (2026-04-19): subagent authoring produced a truncated, under-specified first pass that required a full main-context rewrite anyway. Subagents are the right tool for exploration and inventory (step 1); authoring stays in main because it needs the substrate reports, the user's approved outline, and the carry-over snippets all in one working context. Cost is controlled because steps 1–3 already reduced the substrate to a digest.
5. **Review + iterate** — user reviews the rendered page; iterate until accepted. Update this pattern doc if new decisions surface.

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
- **Quality Bar rendering:** grouped concern cards (matches `.card` style used on `detailed-design.html`). Each card = one concern group with 2–4 Yes/No items. Give the Spec Ambiguity Test card a visual accent to mark its meta-gate status. **Other meta or load-bearing cards may share the accent** (established on Product Brief 2026-04-19: the NFR & constraint capture card carries the accent because it is the section whose silent omission has the largest blast radius).
- **Examples rendering:** code blocks with inline `←` annotations marking failure points on bad examples.
- **Anti-patterns:** numbered list; each item is a 1–2 sentence failure mode + a concrete tell.
- **Navigation:** confirm the page is linked in `docs/guide/index.html`; add if missing.
- **References:** codex-backed citations at page end.
- **Mermaid diagrams:** loaded via CDN at the bottom of the page, immediately before `app.js`. Established on Product Brief 2026-04-19 (Onion Model). Pattern:

  ```html
  <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
  <script>mermaid.initialize({ startOnLoad: true, theme: 'neutral' });</script>
  ```

  Diagrams are wrapped in `<div class="mermaid">…</div>`. Pages that don't use Mermaid don't load the script. The Architecture page will need this for its mandatory Structure Diagram and sequence diagrams in Composition.

---

## 7. Conventions learned on ADR

- **Examples are realistic for the primary audience** — web-service engineering domains (Postgres, job queues, web APIs). Avoid safety-critical contexts in examples even when the primary market is aviation/automotive; those engineers still understand the web-service examples, and the reverse is not true.
- **Retrofit / AI-era bad examples** work best as a side-by-side: fabricated version + honest retrofit rewrite, inline-annotated with the tells (committee-style prose, neat rejection reasons, illegal `reconstructed` states on human-only fields).
- **Quantify example content** where it illustrates a point (e.g., "~1 engineer-week rollback"). Keep numbers clearly illustrative.
- **Name retrofit / AI-era anti-patterns specifically** (e.g., "LLM confident invention," "laundering the current state," "test-as-requirement inversion"). Vague warnings don't land.

## 7b. Conventions learned on Product Brief

- **Lean by default; no separate "scaling" section.** Pre-pivot pages had explicit small-project / large-project scaling callouts. Post-pivot the page IS the lean version; scaling-up to heavyweight ceremony is out of framework scope (no pointer to pre-pivot pages — keeps voice clean).
- **Walked checklists for completeness-bound sections.** Where the failure mode is silent omission, give the author an explicit checklist to walk: NFR Discovery Checklist (10 dimensions, grouped) and Onion Model slot walkthrough. The discipline is *walking the checklist explicitly* (every slot answered, including "n/a — reason"), not in populating every slot.
- **Architecture-driving NFRs at upstream artifacts.** Surfacing NFRs at the anchor layer is non-negotiable; an artifact that drives architecture cannot let availability / latency / security / extensibility default silently. The NFR-10 list (availability, latency, throughput, security, privacy/compliance, data, compatibility, extensibility, operability, cost) is the floor for software-product Briefs; domain NFRs (real-time, accessibility, i18n) are added as needed. Adopt the same posture in Requirements and Architecture pages.
- **Quality-attribute scenario form (six components)** — stimulus / source / environment / artifact / response / response_measure — is the canonical rigorous shape for an NFR. Cite Bass-Clements-Kazman; skip the QAW workshop ceremony.
- **Forcing-function authoring techniques.** Working-backwards / PR-FAQ ("if you can't write the launch announcement, the outcomes aren't concrete enough") and the one-line `summary` frontmatter field both serve as can-you-write-this-cleanly tests. Mention as authoring techniques in best practices, not as artifact sections.
- **Frameworks-by-name without endorsement.** For success criteria, name OKRs / SMART / HEART / AARRR explicitly with one-line trade-offs; teach the principles (outcome ≠ output, leading vs lagging, North Star) rather than mandating a framework. Avoid NASA/INCOSE measurement jargon (MOE/MOP/TPM) — opaque to the software audience.
- **Assumptions are first-class.** Every load-bearing assumption is named with an invalidation trigger. Buried-in-prose assumptions are the highest-blast-radius silent failure mode in the spec tree.
- **Honest unknowns over fabricated certainty.** Open questions, dependencies, and `recovery_status: unknown` (retrofit) are correct outcomes, not partial failures. Surface them visibly; do not pad them away.

## 7c. Conventions learned on Requirements

- **Generalist-gap audit pays.** Eight substantive gaps surfaced only once the V-model-biased substrate was put down and the page was audited against industry practice; see §4 step 2. Codify the audit as a step, not a virtue.
- **Eight Best-Practice subsections** worked for an artifact carrying distinct type-specific disciplines (functional / QA / interface / data / constraint). Earlier pages had 5–7; Requirements needed 8 to teach vocabulary (load-bearing foundation, §2.1), type taxonomy (the other subsections are type-specific, §2.2), then discipline-per-type. Scaled well under the information-density rule; ~11k words was the upper-density envelope — above that, re-ask whether the artifact has more than one concern that warrants splitting.
- **Single-domain worked examples.** All §4 examples in one domain (session-management service). Coherence across examples lets the reader track how the disciplines compose on one subject rather than re-loading domain context per example. Pattern for future pages with many examples.
- **Bounded-vs-unbounded distinction for simple rules.** The simple "negative requirements are an anti-pattern" rule is wrong for security / privacy; the correct rule is "*unbounded* negative requirements are an anti-pattern." When a craft rule has nuance a generalist will counter with, carry the nuance — do not ship the simple rule. Audit generalist counterexamples before crystallising any rule.
- **Compliance-constraint translation as a worked example.** GDPR Article 17 → one inherited constraint + three derived requirements across three types (data, unwanted-behaviour, interface). Concrete pattern a generalist SW-dev audience expects to see; absence would read as if the page only understood safety-regulatory regimes. Applies identically at Architecture (WCAG → interface reqs at UI boundary) and TestSpec (SOC 2 → audit-trail test cases).
- **User-story-to-requirements bridge as §1 card.** Generalist-facing pages need to address the elicitation-paradigm mismatch directly: user stories elicit; requirements formalise. One user story → multiple requirements across types. Pattern worth repeating at any artifact that generalists approach with a different mental model (Architecture vs. c4 diagrams; TestSpec vs. Gherkin feature files).
- **Second Mermaid diagram (traceability lineage, §1).** Used sparingly: one diagram per page where it earns its keep. Product Brief used Mermaid for the Onion Model (stakeholder spatial); Requirements used it for the 5-link traceability chain (PB → Req → Arch → DD → TestSpec). Architecture will almost certainly need more (structure diagrams, sequence diagrams for Composition) — that's fine; the rule is diagram-where-it-earns, not "one per page."

---

## 8. Remaining artifact order

1. ~~ADR~~ — complete (2026-04-19).
2. ~~Detailed Design~~ — complete (2026-04-19). Compression carried ~45% of the pre-pivot page's substance (DbC example, behavioural-spec example, error matrix, config-loader example, References core) under the new 5-section + 7-artifact-section shape; the rest was rewritten.
3. ~~Product Brief~~ — complete (2026-04-19). Consolidated former `stakeholder-needs` + `conops` + `completeness-analysis` (combined ~17.5k words pre-pivot) into a single ~6.5k-word page. NFR Discovery Checklist (canonical 10) and Mermaid Onion Model introduced; assumptions absorbed into Constraints with invalidation triggers; success criteria framed via OKR/SMART/HEART/AARRR (MOE/MOP/TPM dropped as too aerospace-coded).
4. ~~Requirements~~ — complete (2026-04-20). Eight Best-Practice subsections (vocabulary, type taxonomy, EARS+GWT, statement-level quality, NFRs, interfaces, constraints, lifecycle); seven worked examples in one session-management domain; introduced generalist-gap audit (§4 step 2); bounded-vs-unbounded nuance for negative-requirement anti-pattern; GDPR Article 17 compliance translation pattern; ~11k words (upper-density envelope).
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
