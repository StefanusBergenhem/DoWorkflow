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

## 7d. Conventions learned on Architecture

- **Stress-test framework-specific rules against industry practice before authoring.** Learned on Architecture (2026-04-20): during the propose step the user asked whether doing something unique means doing the wrong thing, re: TARGET §5.1's wiring-as-leaf-sibling rule. Industry review showed the rule was over-generalised (common practice: Architecture Composition + IaC is the spec; no separate DD layer above IaC). TARGET amended pre-authoring: §5.1 softened, §6 Architecture Quality Bar strengthened with deployment-intent checks, §8.4 + §15 updated with the deferred question. Pattern: if a page has to defend a framework-unique rule, check whether the rule survives industry-practice scrutiny first. Teach sound rules or stay silent; do not teach loud-wrong.
- **Generalizability callout in §1.** Added an explicit paragraph in V-model Context stating the craft holds across domains (Java enterprise, desktop, mobile, batch, embedded) with concrete vocabulary substitutions ("orchestration target" = installer / Play Store channel / scheduler / firmware image depending on domain). Defuses generalist-bias concern that framework reads as web-only when examples are web-service. Useful pattern for future pages where deployment-time concerns risk reading as web-coded.
- **Two accent cards where Product Brief used one.** Composition-completeness carries the accent because silent omission has the largest blast radius at this layer; Spec Ambiguity Test carries it as the meta-gate. Pattern: accent cards mark structurally load-bearing sections, not just the meta-gate. Use sparingly (≤2 per page) to preserve signal.
- **Four Mermaid diagrams, all earning their keep.** Scope tree in §1 (V-model position); structure, sequence, and deployment in §4 (worked example). Architecture legitimately uses more diagrams than prior pages because the artifact has three mandatory visual elements (Structure Diagram, Composition sequences, deployment at root). Rule stays "diagrams earn their keep" — no quota, no cap.
- **Option A (completeness push) did not inflate length.** Projected 14-15k words for a completeness push over the 11k Requirements envelope; delivered 11.7k. Every audit gap got treatment, but disciplined subsections keep density. "Longer page" is authorisation, not outcome; density discipline still applies.
- **Single-domain worked example carried seven sub-artifacts.** One order-checkout domain, seven concrete demonstrations in §4: structure diagram, decomposition YAML, DbC interface entry, happy-path sequence diagram, deployment diagram, bad-decomposition contrast, retrofit-laundered-vs-honest contrast. Pattern continues from Requirements (session-management, seven examples); holds for any artifact with multiple example shapes.
- **C4 bridge in §1, not §2.** Generalist-expected model (C4) gets named in V-model Context as a bridge, not relegated to a Best-Practice subsection. Same placement logic as the user-stories-to-Requirements bridge on the Requirements page: address paradigm mismatch upfront so the rest of the page reads coherently.

## 7e. Conventions learned on TestSpec

- **Four bridge cards in §1, not one.** TestSpec intersects four established mental models that generalists bring: tests-as-spec (TDD orthodoxy), BDD/Gherkin/GWT, TDD/ATDD/DRTDD positioning, test-pyramid/honeycomb/trophy shape debate. Each got its own bridge card in §1. Prior pages used one bridge card (C4 on Architecture; user-stories on Requirements). Pattern: bridge-card count scales with the number of paradigms generalists will want resolved before they can read the rest of the page. Undercount and readers carry the mismatch through the whole artifact; overcount and §1 bloats. Four was right for TestSpec; most artifacts need one or two.
- **"Tests as spec" bridge is the most important card on the page.** The tests-are-the-spec position (Beck, Adzic, Fowler) is a strong TDD tradition that denies the TestSpec artifact needs to exist. Addressed directly: the framework agrees about test code self-documentation at leaf; it disagrees about the conclusion because test code does not carry derivation choices, AI target, traceability anchor, or completeness check. Resolution: per-layer weight is thin-at-leaf-by-design. Framework agrees with the intuition *and* adds the missing layer. Pattern: when a practitioner tradition rejects the framework's artifact entirely, do not defend — name the tradition's valid point and show where the framework's answer sits relative to it.
- **Per-layer weight matches test-code self-documentation density.** The thin-leaf / heavy-root gradient is not arbitrary. At leaf, JUnit method names + AAA + specific assertions carry most of the verification intent; the TestSpec case is one line. At root, e2e scenarios need narrative weight that method names cannot carry. The weight rule unifies the "don't double-bookkeep" intuition with the "carry what the code can't" principle. Useful framing whenever a TestSpec-like artifact spans layers where the downstream artifact (code) has variable self-documentation density.
- **Multi-flavor artifact handled via per-layer table + inline callouts.** TestSpec is the only Phase 2 artifact that spans three scope layers in one envelope (ADR is cross-cutting but single-shape). §2.2 carries the per-layer table (Leaf/Branch/Root × source/weight); subsequent craft subsections call out per-layer emphasis inline where it matters (§2.4 granularity per layer; §2.6 doubles per-layer usage; §2.7 coverage bar per layer; §2.8 integration+system majored subsection). Worked cleanly; reader retains the table through the page. Pattern: a per-layer table anchor in §2 early, then inline per-layer notes, rather than three parallel sections or three separate pages.
- **Empirical numbers live on the page, not shunted to the skill.** User's explicit guidance: documentation is a superset of skills. The coverage-mutation gap, LLM empirical data (34–62% invalid, 100/4 possibility, Meta ACH 73% acceptance, MutGen 89.5%, Copilot 7.55%/45.28%), mutation thresholds (60/80%), Google flake rate (84%) all live on the page. Skills reference; they do not duplicate. Pattern: when empirical numbers are load-bearing for a craft rule, cite in full in the craft doc; skills cite the doc. Applies to any future page where a claim like "60% is the defensible minimum" rests on a named study.
- **13 anti-patterns, four AI-era failure modes.** Higher AI-era-failure count than Architecture (which named 2: laundering, fabricated rationale). TestSpec's AI-era cluster: code-to-test, tautological tests, test-as-requirement inversion, happy-path bias, weak assertions. Empirically the most failure-prone artifact for AI authoring per the 2024–2026 literature; the density of named failure modes reflects that. Anti-pattern count should scale with the empirical failure surface for the artifact, not with a quota.
- **Three Mermaid diagrams, all load-bearing.** Derivation-source-by-layer in §1 (the page's core thesis visualised); pyramid/honeycomb/trophy subgraph comparison in §1 bridge card (three-shape visual beats prose); traceability chain in §2.4 (PM elements → test cases → verifies link). Three is more than prior pages except Architecture (which had four); each earns its keep because the three-layer structure of TestSpec requires visual anchors that prose cannot carry. Rule unchanged: diagrams where they earn.
- **Accent-card pattern: meta-gate + highest-blast-radius silent omission.** Spec Ambiguity Test (always) + Coverage-Mutation Independence (measuring only structural coverage is the single largest silent-failure mode). Same pattern Architecture used (Spec Ambiguity + Composition completeness). The second accent goes on the concern whose *silent* omission has the largest blast radius for this artifact — not the concern that causes the loudest failures. For TestSpec that is coverage-mutation independence; for Architecture it was composition completeness; for Product Brief it was the NFR discovery checklist. Identify the silent-omission blast radius per artifact; that is where the second accent goes.
- **Examples structure matches Requirements / Architecture: single domain, seven sub-examples.** Session-management carried from Requirements. Three per-layer YAML examples (leaf/branch/root), coverage matrix, decision-table walkthrough, two bad-vs-good contrasts (code-to-test / spec-to-test; fabricated-retrofit / honest-retrofit). Now a solid Phase 2 convention: single-domain with seven sub-examples is the right density for an 11–13k-word craft page.
- **Bad-vs-good contrast for AI-era failures is now a Phase 2 convention.** Architecture (§4.7) did laundered-vs-honest; TestSpec (§4.7) did fabricated-retrofit-vs-honest-retrofit; TestSpec (§4.6) also did code-to-test-vs-spec-to-test. Side-by-side YAML with inline `←` annotations on the failure points is the established shape. Every Phase 2 page with meaningful AI-era or retrofit failure modes carries a bad-vs-good contrast in §4.

---

## 8. Remaining artifact order

1. ~~ADR~~ — complete (2026-04-19).
2. ~~Detailed Design~~ — complete (2026-04-19). Compression carried ~45% of the pre-pivot page's substance (DbC example, behavioural-spec example, error matrix, config-loader example, References core) under the new 5-section + 7-artifact-section shape; the rest was rewritten.
3. ~~Product Brief~~ — complete (2026-04-19). Consolidated former `stakeholder-needs` + `conops` + `completeness-analysis` (combined ~17.5k words pre-pivot) into a single ~6.5k-word page. NFR Discovery Checklist (canonical 10) and Mermaid Onion Model introduced; assumptions absorbed into Constraints with invalidation triggers; success criteria framed via OKR/SMART/HEART/AARRR (MOE/MOP/TPM dropped as too aerospace-coded).
4. ~~Requirements~~ — complete (2026-04-20). Eight Best-Practice subsections (vocabulary, type taxonomy, EARS+GWT, statement-level quality, NFRs, interfaces, constraints, lifecycle); seven worked examples in one session-management domain; introduced generalist-gap audit (§4 step 2); bounded-vs-unbounded nuance for negative-requirement anti-pattern; GDPR Article 17 compliance translation pattern; ~11k words (upper-density envelope).
5. ~~Architecture~~ — complete (2026-04-20). Greenfield (no pre-pivot HTML existed). Nine Best-Practice subsections including dedicated Composition-patterns catalog; ~11.7k words; four Mermaid diagrams (scope tree, structure, sequence, deployment). Order-checkout worked example with seven sub-demonstrations. **TARGET amended pre-authoring** after industry-practice challenge to wiring-as-leaf-sibling rule: §5.1 softened (Architecture Composition is authoritative spec, IaC is the implementation), §6 Quality Bar strengthened with deployment-intent checks, §8.4 + §15 updated with deferred question. C4 bridge in §1.
6. ~~TestSpec~~ — complete (2026-04-21). Greenfield replacement for pre-pivot `unit-test.html`. Nine Best-Practice subsections with per-layer weight table in §2.2 and dedicated major §2.8 for integration+system (contract testing, test envs, perf/sec/a11y). Four bridge cards in §1 (tests-as-spec, BDD/GWT, TDD/ATDD/DRTDD, pyramid/honeycomb/trophy) — most paradigm-mismatch addressing of any Phase 2 page. Three Mermaid diagrams (derivation-source-by-layer, pyramid/honeycomb/trophy comparison, traceability chain). Empirical LLM testing data carried directly on page per "docs are the superset of skills" rule (Yang 2024, Foster FSE 2025, Meta ACH). 13 anti-patterns with four AI-era cluster (code-to-test, tautological, test-as-requirement-inversion, happy-path bias, weak assertions). Single-domain session-management worked example (seven sub-examples including two bad-vs-good contrasts). ~13k words. Pre-pivot `unit-test.html` to be archived as cross-cutting task (§9).

**Phase 2 per-artifact authoring complete.** Six artifact pages authored under the 5-section structure. Cross-cutting tasks (§9) remain.

After each artifact is accepted, update this doc with any new pattern decisions.

---

## 9. Cross-cutting Phase 2 tasks (not per-artifact)

- Remove domain translation plugin machinery from `docs/guide/` (`js/domain.js` wiring, domain selector UI); archive `docs/guide/domains/*.json` to `archive/pre-pivot-2026-04-18/domains/`.
- Archive superseded pre-pivot artifact pages to `archive/pre-pivot-2026-04-18/`: `unit-test.html` (superseded by `testspec.html`), `stakeholder-needs.html` + `conops.html` + `completeness-analysis.html` (absorbed into `product-brief.html`). Remove their nav links from `index.html`.
- Validate navigation linkage across all six artifact pages + `index.html`.
- Strip any residual pre-pivot framing across pages.

---

*Captured after ADR completion. Update as subsequent artifacts reveal new decisions.*
