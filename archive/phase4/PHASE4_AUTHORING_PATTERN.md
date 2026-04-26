# Phase 4 — Authoring Pattern & Session Handoff

Status document for Phase 4 (Product Briefs for the three purpose-built tool products). Mirrors the Phase 2 / Phase 3 precedent (`PHASE2_AUTHORING_PATTERN.md`, `PHASE3_AUTHORING_PATTERN.md`, both archived on phase completion).

Load this file alongside `CLAUDE.md` + `BACKLOG.md` + `TARGET_ARCHITECTURE.md` at every Phase 4 session start. Archive this file to `archive/phase4/` on Phase 4 completion.

---

## 1. Status as of 2026-04-23 (kickoff)

**Phase 4 goal:** Product Briefs for the three purpose-built tool products defined in `TARGET_ARCHITECTURE §10` — `vmodel-core`, `vmodel-author`, `vmodel-retrofit`. Context-only phase; does not block skills (Phase 5) or tools (Phase 6).

**Kickoff work landed this session:**

| Commit | Step | What |
|---|---|---|
| (pending) | Kickoff | `TARGET_ARCHITECTURE §10` rewritten from a flat ten-capability list to a three-product structure (`vmodel-core` / `vmodel-author` / `vmodel-retrofit`). `BACKLOG §3.4` task list updated to three PBs with staging-dir deliverable; `BACKLOG §6 Q5` closed. Staging dirs created under `docs/plan/phase4-tool-briefs/{core,author,retrofit}/`. This handoff doc authored. |
| (pending) | AI-ergonomic CLI | `TARGET_ARCHITECTURE §10` extended with *AI-ergonomic CLI* subsection (8 patterns: non-interactive, idempotent, `--dry-run`, `--yes`/`--force`, predictable resource+verb, stdin, progressive `--help`, structured success output). Sourced from engineering-codex ingestion of Zakariasson X-post on CLI design for AI agents. Applies uniformly across all three tool products — PBs cite `§10`, do not re-derive. |

**Pending tasks (Phase 4 backlog):**
- [ ] Product Brief for `vmodel-core` (pilot — pattern-setter).
- [ ] Product Brief for `vmodel-author`.
- [ ] Product Brief for `vmodel-retrofit`.

**Phase 4 closeout signal:** when all three PBs round-trip clean through `product-brief.schema.json` + envelope + common-defs, and pass the Product Brief Quality Bar end-to-end. On closeout, archive this file to `archive/phase4/`.

---

## 2. Decisions Locked (do not reopen without cause)

### Product consolidation (the load-bearing call)

**Ten capabilities → three products.** `TARGET_ARCHITECTURE §10` was originally drafted as a flat list of ten purpose-built tools. Phase 4 kickoff consolidated these into three cohesion-grouped products: `vmodel-core` (six capabilities — parser, schema-validate, trace-validate, quality-bar, graph-build, query), `vmodel-author` (two — scaffold, render), `vmodel-retrofit` (two — topology, gap-report).

**Rationale:** Product Brief anchors a product, not a function. Ten briefs for ten tiny utilities misuses the artifact; three briefs match real stakeholder cuts (every adopter / spec writers / retrofit pilot teams) and preserve principle #6 ("tools as independent products"). Three independent repos at Phase 6 kickoff still satisfy the "framework bundles no tools" rule.

**Do not reopen** unless real-pilot evidence shows the groupings don't hold. If any capability proves to have a distinct stakeholder and lifecycle, it earns its own product (new PB, new repo).

### Staging location

- **`docs/plan/phase4-tool-briefs/{core,author,retrofit}/product_brief.md`.** One directory per product. Each directory lifts cleanly to its own repo root at Phase 6 kickoff — no nested reshuffle.
- **Not in `/specs/`.** These are briefs *for future products*, not specs of this framework repo. `/specs/` (if/when it appears) describes the framework itself, not its tool outputs.

### Authoring discipline

- **Manual authoring against Phase 2 + Phase 3 artifacts.** Phase 5 `vmodel-skill-author-product-brief` does not exist yet; Phase 4 PBs are hand-authored against the Phase 2 HTML doc and Phase 3 schema / Quality Bar JSON / fixture. First dogfooding run — expect to discover drift, surface gaps.
- **Pattern-setter: `vmodel-core`.** Matches the Phase 2 ADR-first and Phase 3 ADR-schema-first precedent. Author `vmodel-core` fully, get human sign-off, then scale `vmodel-author` + `vmodel-retrofit` against the locked pattern. Subagent fan-out an option for the latter two once the pattern is known.
- **Human as stakeholder; AI drafts from docs.** Product Brief is lowest-agent-autonomy artifact (`TARGET_ARCHITECTURE §12`). Stefanus is the primary stakeholder (framework author); secondary stakeholders (CI engineers, skill authors, retrofit pilots) are represented through the docs they've already driven (`§10`, `§11`, `§12`, MEMORY). AI may propose content from these sources; AI **must not fabricate** stakeholders, outcomes, or constraints beyond what's derivable.
- **Retrofit no-fabrication rule applies even in greenfield authoring of these PBs.** If content can't be derived from `TARGET_ARCHITECTURE` or explicit conversation, surface the question — don't pave over it.

### Structural rigor

- **7 sections per `docs/guide/artifacts/product-brief.html` §2.** Stakeholders, Problem, Desired Outcomes, Operational Concept, Constraints, Non-Goals, Success Criteria.
- **Schema validation is a hard gate.** Each PB must round-trip clean through `schemas/artifacts/product-brief.schema.json` + envelope + common-defs, same discipline as Phase 3 fixtures.
- **Quality Bar is the semantic gate.** `schemas/artifacts/quality-bar/product-brief.quality-bar.json` — 10 groups, 39 items, two load-bearing (NFR-and-constraint-capture + Spec Ambiguity Test meta-gate). Run end-to-end on each PB before marking the task done.

---

## 3. Authoring Source of Truth

| Need | Source | Notes |
|---|---|---|
| Craft (what a good PB looks like) | `docs/guide/artifacts/product-brief.html` | Post-pivot, 5-section page. Onion Model, NFR Discovery Checklist, 10-dim framework. |
| Structural shape | `schemas/artifacts/product-brief.schema.json` | Draft 2020-12; front-matter + summary. Round-trip gate. |
| Semantic rigor | `schemas/artifacts/quality-bar/product-brief.quality-bar.json` | 10 groups, 39 items. SAT meta-gate mandatory. |
| Reference example | `schemas/artifacts/fixtures/product-brief.example.md` | LinkSnip URL-shortener root PB. Shape exemplar. |
| Envelope / common defs | `schemas/artifacts/envelope.schema.json` + `common-defs.schema.json` | `id` const `"PB"`; universal lifecycle; iso_date. |

**Tool-universe reference** (who consumes each PB's output — useful for Operational Concept): `TARGET_ARCHITECTURE §10` (the new three-product structure, this session's edit).

---

## 4. Open Points

1. **`vmodel-core` product boundaries inside the product.** Six capabilities; likely one CLI binary with subcommands. Exact subcommand tree is Phase 5/6 territory (ADRs inside the product's spec chain), not a PB concern. PB should name outcomes, not architecture.
2. **Project config format (`.vmodel/tools.yaml` or similar).** `BACKLOG §6 Q4` — cross-product concern. Draft needed before Phase 5 framework skills reference it. May surface as a constraint when authoring `vmodel-core` Operational Concept (how does a project declare it has `vmodel-core` installed?). If it becomes load-bearing during Phase 4, escalate and resolve before continuing.
3. **Dependency story between products.** `§10` states `vmodel-author` and `vmodel-retrofit` depend on `vmodel-core` (library or subprocess — each product's ADRs decide). PB language should reflect this without committing to which mechanism.
4. **Stakeholder specificity.** "Every adopter" is true but imprecise. `vmodel-core` PB Stakeholders section needs concrete named personas (CI engineer, authoring-skill subprocess caller, review-skill subprocess caller, …) via Onion Model walkthrough. Drive this in conversation, don't auto-generate.
5. **Non-goals — explicit framework separation.** Each tool PB should declare "not a framework replacement" / "not a tool for authoring code" type non-goals. Expect two or three per product.
6. **Solo-stakeholder authoring.** Stefanus is the only available human voice for Phase 4 PBs — framework author + adopter #1. No external interviews possible. Recorded per QB `stakeholder-completeness-004` as: *direct* for his own adoption journey; *single-hop surrogate* for hypothetical future adopter groups (org leads, platform teams, CI engineers at unknown orgs). Content driven from the direct position is high confidence; hypothetical extensions are written minimally and flagged `recovery_status: unknown` where their absence would shape a constraint or non-goal. Stakeholder revisions are a lifecycle event when real adopters arrive, not a pre-req for Phase 4 completion.

7. **PB §G two-tier deferral pattern (framework-level finding from `vmodel-core` pilot).** During the `vmodel-core` PB authoring, Section G was authored as two explicit categories: **engineering-completeness criteria** (committed; serve as Phase 6 Build closure conditions) and **outcome-validation criteria** (deliberately deferred with a stated trigger to revisit). Reason: outcome bets at PB authoring time were premature for an exploratory dogfooded tool whose product shape and adoption strategy are not yet stable. The Phase 2 PB craft doc currently frames §G as outcome-validation only and does not accommodate this case. Whether to formalise the two-tier split as a first-class authoring pattern in `docs/guide/artifacts/product-brief.html` §G + §B + the PB Quality Bar is a candidate Phase 2 docs revision. **Action deferred** until all three Phase 4 PBs are complete — they may surface additional doc deltas worth bundling. Logged here so the finding survives Phase 4 closeout.

8. **Open-questions register form (framework-level finding from `vmodel-core` PB QB walk).** The PB Quality Bar item `assumptions-and-unknowns-003` asks whether open questions are surfaced explicitly with owner and target resolution. The PB craft doc (`docs/guide/artifacts/product-brief.html`) does not specify the *form* of the open-questions register — structured YAML block, inline prose deferral, or external open-questions doc. The `vmodel-core` PB satisfies the QB item by adding an explicit `### Open Questions` subsection inside §E *Constraints* with a structured YAML block (id, question, context, owner, resolution_target per item). Whether this form should be canonicalised in the PB craft doc is a candidate Phase 2 docs revision, distinct from the §G two-tier deferral finding above. **Status update:** subsumed by Finding #9 below — the PD shape converts open questions to Assumptions with invalidation triggers. The structured-block form remains useful for PB use but PD does not need a separate register.

9. **PD-vs-PB artifact split (the load-bearing Phase 4 finding).** The Phase 2 PB craft doc is implicitly business-product-flavoured — Onion Model stakeholder analysis, surrogacy chains, problem-evidence sections, before/after outcome paragraphs, working-backwards PR/FAQ. For engineering products (internal tooling, dogfooded products, single-author products, products whose value is engineering-internal), the 7-section PB shape produces ceremonial overweight. The `vmodel-core` pilot generated a 492-line business PB that the human author identified as wrong artifact for his actual need: an *engineering product description* that captures functional and non-functional scope plus constraints, not a *business product brief* that justifies investment to a sponsor.
   - **Decision (Phase 4 working pivot):** introduce a new artifact type, **Product Description (PD)**, distinct from Product Brief. PB is preserved as an option for products with real markets and real customers; PD is the engineering-flavoured alternative. The `vmodel-core` business-PB experiment is preserved as an exhibit at `archive/phase4-business-pb-experiment/product_brief.md`; the live `vmodel-core` anchor is now `docs/plan/phase4-tool-briefs/core/product_description.md`.
   - **PD shape (5 sections + dependency manifest):** Vision (1), Functional Scope (2), Non-Functional Scope (3, six subgroups: Hard / Performance / Scale / Compatibility / Security / CLI ergonomics & operability), Out of Scope (4), Assumptions (5, replaces PB's Open Questions — assumptions carry invalidation triggers), References and Dependencies (6, lists every external file the PD depends on so a fresh repo can stand up).
   - **Externalised implicit-aspect checklist:** the PD does not inline the Discovery-Checklist walk that PB requires. Instead, the implicit-aspect dimensions (performance, failure modes, state, concurrency, security, operability, distribution, compatibility, extensibility, dependencies, localisation, cost) are enforced by the Phase 5 PD authoring skill during the interview. The artifact carries the outputs (in §3) but not the checklist itself.
   - **Schema-after-artifact authoring order:** the PD artifact type does not yet have a schema or Quality Bar JSON. The decision (vs. Phase 3's schema-first approach for PB) is deliberate — discussing the PB schema in the abstract during Phase 3 missed the business-vs-engineering misfit because the schema is too abstract a surface to surface that question. PD authoring proceeds concretely first; schema and Quality Bar are derived from the three concrete PDs (this one + `vmodel-author` + `vmodel-retrofit`) in the Phase 2 docs revision after Phase 4 closeout.
   - **Phase 4 deliverable shift:** Phase 4 was originally framed as "Product Briefs for purpose-built tools" (BACKLOG §3.4). The actual deliverable is now Product Descriptions; BACKLOG §3.4 wording updated to reflect.
   - **Action items for Phase 2 docs revision (post-Phase-4):** (a) author PD craft doc under `docs/guide/artifacts/product-description.html` with the 5-section structure; (b) author PD JSON Schema (draft 2020-12) modelled on PB's; (c) author PD Quality Bar checklist; (d) revise PB craft doc to clarify the PB-vs-PD choice and to confirm PB applies for products with real customers / business stakes; (e) update `TARGET_ARCHITECTURE §5` (artifact set) from six to seven artifact types; (f) update `TARGET_ARCHITECTURE §3` core-principles list if PD-vs-PB pivot reveals a missing principle (early hypothesis: *anchor artifact must match product type*; not yet confirmed).
   - **Why this is bigger than findings #7 and #8 combined:** findings #7 and #8 are doc-shape refinements within the existing PB. Finding #9 is a new artifact type that resolves a category mismatch the framework didn't know it had. Both #7 and #8 partially or fully subsume into the new PD shape.

---

## 5. Recommended Next Step

Start `vmodel-core` Product Brief authoring conversation. Proposed flow:

1. Walk the Onion Model to identify `vmodel-core` stakeholders — Stefanus drives from his adopter picture.
2. Draft Problem section from `§10` tool/skill split + §11 skills architecture (why mechanical validators exist as a separate layer). AI proposes; human confirms evidence.
3. Drive Desired Outcomes from stakeholder cut — one narrative per stakeholder group.
4. Operational Concept from `§10` tool universe + `§8` workflow integration (pre-commit, CI, authoring-skill subprocess, review-skill subprocess).
5. Constraints from `§10` (CLI contract, `--format json|text`, TTY-aware, actionable errors, no LLM) + principle #5 (tool/skill split).
6. Non-Goals explicit.
7. Success Criteria measurable — land before marking the pilot complete.

After the first pass, validate against schema + Quality Bar, iterate if the SAT meta-gate flags ambiguity, then commit. Pattern locked; move to `vmodel-author` and `vmodel-retrofit`.

---

## 6. Session Handoff (2026-04-26 — pause)

### Current state

- **Architecture:** `TARGET §10` three-product structure + AI-ergonomic CLI subsection landed. `BACKLOG §3.4` reflects PB → PD pivot.
- **`vmodel-core` PB** (business-PB experiment, 492 lines): **archived** at `archive/phase4-business-pb-experiment/product_brief.md`. Not the live anchor.
- **`vmodel-core` PD** (engineering-PD pilot, 177 lines): at `docs/plan/phase4-tool-briefs/core/product_description.md`. Status is `draft` — pending human final read after the 8 fresh-repo-subagent findings were addressed. Not yet flipped to `active`.
- **`vmodel-author` PD:** not started. Pattern is locked from the `vmodel-core` PD.
- **`vmodel-retrofit` PD:** not started.
- **PD schema + Quality Bar + craft doc page:** all deferred per the schema-after-artifact decision. To be authored in the Phase 2 docs revision after all three Phase 4 PDs are complete.

### Load-bearing open question (raised by Stefanus at session pause)

**Are PD and root-scope Requirements distinct artifacts, or do they collapse for engineering products?** The `vmodel-core` PD §2 Functional Scope + §3 Non-Functional Scope contain content that arguably belongs in root-scope Requirements. The unique PD content is §1 Vision + §6 Dependency Manifest; everything else overlaps with what Requirements would carry.

Industry precedent splits both ways:
- **Separate:** INCOSE SE Handbook (StRS → SyRS), ISO 26262 (HLR vs LLR), DO-178C, classical V-model.
- **Combined:** IEEE 830 SRS, arc42, modern lean PRDs/RFCs.

The pivot point seems to be product scale (large = separate makes sense; small CLI = collapse). The framework currently presumes separation. Whether that holds for engineering-internal tools is unproven.

**This question must be researched and answered before authoring the PD schema** — the schema will encode the answer either way.

### Next-session priority order

1. **Final read of `vmodel-core` PD** — confirm the post-fixes state, flip `draft` → `active`, close task #5.
2. **Research the PD-vs-Requirements distinction.** Suggested angles:
   - Query the engineering-codex (`/home/stefanus/repos/engineering-codex/`) for existing coverage of stakeholder-vs-system requirements, SRS patterns, or the V-model layer split. Use the `query-codex` skill.
   - Surface external standards' positions (INCOSE, ISO 26262, DO-178C, IEEE 830, arc42, Volere).
   - Articulate the criterion that determines "separate or combined" for a given product (likely: scale, multi-subsystem, regulatory regime).
   - Present options to Stefanus before any artifact-set decision.
3. **Decide the artifact split** (combine, separate, or context-dependent). Consider whether this affects `TARGET §5` (artifact set) — currently 6 types. Adding PD makes 7; combining PD + root-Requirements may keep 6 with PD replacing root-Requirements role.
4. **Author PD schema (JSON Schema draft 2020-12) + Quality Bar JSON + craft doc page** — only after the above question is resolved.
5. **Dogfood with `vmodel-author` PD** — Stefanus's stated next pilot. Use the locked PD pattern; surface any new findings.
6. **Then `vmodel-retrofit` PD** — third pilot.
7. **Phase 4 closeout** — bundle all docs-revision findings (#7, #8, #9, plus any from the next two PDs) into a single Phase 2 docs revision pass. Archive `PHASE4_AUTHORING_PATTERN.md` to `archive/phase4/`.

### Bootstrap files to load at start of next session

- `~/.claude/CLAUDE.md` (global rules)
- `CLAUDE.md` (project)
- `MEMORY.md` (auto-loaded)
- `docs/plan/TARGET_ARCHITECTURE.md` — especially `§10` (now 3-product) and `§5` (artifact set, may change)
- `docs/plan/BACKLOG.md` — especially `§3.4` (PD pivot recorded)
- This file (`PHASE4_AUTHORING_PATTERN.md`) — Findings #1–#9 + this handoff
- `docs/plan/phase4-tool-briefs/core/product_description.md` — the live `vmodel-core` PD
- `archive/phase4-business-pb-experiment/product_brief.md` — only if studying the PD-vs-PB difference

### Tasks still open at pause

- #5 — Pilot PD: `vmodel-core` (pending final approval after fixes).
- All Phase 4 PD fan-out tasks (vmodel-author, vmodel-retrofit) — not yet created; create when starting.
- PD schema / Quality Bar / craft doc — Phase 2 docs revision, post-Phase-4.
- Codex pattern-page synthesis (`pat-cli-design-for-ai-agents`) — flagged in PD §6; cross-repo work for the codex.

---

---

## 7. Closeout (2026-04-26)

Phase 4 closes without producing the three PDs originally planned. The session-pause open question — *is PD distinct from root-scope Requirements, or do they collapse for engineering products?* (see §6) — was re-examined alongside two parallel research passes (codex sweep + web research on the AI-coding frontier for stakeholder-intent capture). The reframe that surfaced is bigger than the question.

**PD was a category error.** The Phase 4 PB → PD pivot (Finding #9) correctly identified that the vmodel-core PB experiment did not fit, but mis-diagnosed the cause as *artifact-shape misfit*. The actual cause is more specific: vmodel-core's stakeholder = architect = framework author. The translation layer that PB exists to bridge — between non-technical stakeholder and software architect — collapsed because there was no separation of roles. PB itself remains fit for purpose for products with non-architect stakeholders.

**The real gap is a missing elicitation skill.** What was actually wanted at the top of the spec tree was not a new artifact but a specific *interaction*: an AI skill that takes unstructured stakeholder narrative, runs an interview-style dialog with explicit anti-assumption discipline, explains architect-concepts in stakeholder-accessible terms, actively surfaces gaps a competent architect would find (NFRs, edge cases, integrations), and reads its structured understanding back to the stakeholder in stakeholder-accessible language for joint agreement (DDD-flavoured ubiquitous language). The output of that interaction is just the existing root-Requirements (in EARS or similar). No new artifact type needed.

**AI-frontier evidence (2026-04-26 research).** The closest tools that exist today — BMAD-METHOD analyst step, GitHub Spec Kit `/specify`, ChatPRD, Outset / Kraftful / Strella interview tools, Qlerify event-storming, LLMREI elicitation chatbot, ClarifyGPT, the Follow-Up Question Generation paper — all stop short of the **readback-for-joint-agreement** behaviour and the **anti-assumption discipline** described above. No shipping product names a "stakeholder-voice top-of-tree artifact" with consensus. The codex `pat-hitl-gates` rejection format (*Expected / Found / Why this matters / How should I proceed?*) is the closest discipline match in the project's substrate; it can carry the readback contract directly. Field signal: SLR (arXiv 2509.11446) reports +136% YoY paper growth on LLMs in RE, with only ~5% of studies using "interactive prompting." Early field — the framework leads here, not follows.

**Decisions locked.**

1. **Framework retains its 6-artifact set.** PD is not introduced. `TARGET_ARCHITECTURE §5` and the Phase 2 PB craft doc do not change.
2. **Phase 4 closes** without authoring vmodel-author or vmodel-retrofit PDs. Those tools are spec'd in Phase 5+ via the regular Specification workflow (Requirements → Architecture → DD → TestSpec).
3. **The vmodel-core PD draft is preserved** at `docs/plan/phase4-tool-briefs/core/product_description.md` as eval input for the new skill — it is the kind of unstructured-but-rich stakeholder narrative the skill is supposed to consume. The empty `phase4-tool-briefs/{author,retrofit}/` staging directories are removed.
4. **Phase 5 picks up `vmodel-skill-elicit-requirements`** as a separate skill from `vmodel-skill-author-requirements`. Same output (root-Requirements), different input (unstructured stakeholder narrative vs structured parent allocation). Behaviour: anti-assumption / explanation-while-eliciting / gap-finding / readback for joint agreement. See `BACKLOG §3.5`.
5. **Codex raw-folder additions** were proposed for: LLMREI, Follow-Up Question Generation, ClarifyGPT, Structured Uncertainty-guided Clarification, SLR on LLMs for RE, Teresa Torres Interview Coach + caution against synthetic interviews, BMAD analyst step, live AI-discovery products (Outset, Kraftful, Strella, Qlerify). Cross-checked against codex index and added where new — see commit history.

**Re-evaluation of earlier findings.**

- **Findings #7 and #8** (the §G two-tier deferral pattern and the open-questions register form) remain candidates for a Phase 2 PB-craft-doc revision but are no longer urgent; surface again only if a real PB is authored and the gaps re-emerge.
- The **PB JSON Schema** and **PB Quality Bar** are unchanged. PB is still in the 6-artifact set.
- `TARGET_ARCHITECTURE §10` (three-product structure + AI-ergonomic CLI subsection) is unchanged. The AI-ergonomic CLI principles continue to apply when each tool's specs are authored in Phase 5+.

This file is archived to `archive/phase4/PHASE4_AUTHORING_PATTERN.md` on closeout.

---

*Last updated: 2026-04-26 (Phase 4 closeout — PD was a category error; gap was a missing elicitation skill at root scope).*
