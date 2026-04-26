---
id: PB
artifact_type: product-brief
title: "vmodel-core — Product Brief"
summary: "vmodel-core is a deterministic command-line tool for VModelWorkflow projects that validates, queries, and reports on the spec tree, so that humans and AI agents share trustworthy ground truth during authoring and review."
status: draft
date: "2026-04-25"
version: 1
---

<!--
  This Product Brief is the anchor artifact for the vmodel-core tool product
  (see TARGET_ARCHITECTURE §10 for the three-product structure). It stages
  in docs/plan/phase4-tool-briefs/core/ during Phase 4 and lifts to the
  vmodel-core repo root at Phase 6 kickoff.

  Authoring state (2026-04-24): Section A complete; B–G pending.
-->

## Stakeholders

`vmodel-core` is called by both humans and AI agents as first-class users, and its output shapes the work of a wider ring of roles that never invoke it directly. This section walks every Onion Model slot and records source-chain honesty per Quality Bar item `stakeholder-completeness-004`.

**Source-chain context.** The framework author (Stefanus Bergenhem) is the sole available human voice at Phase 4 authoring time — framework author, adopter #1, no external stakeholder interviews accessible. Entries below are marked *direct* where the author speaks for himself and *single-hop surrogate* where he projects needs for hypothetical future adopters from `TARGET_ARCHITECTURE §10`, `§11`, `§12`. No multi-hop surrogacy is in use.

### Our system — operators and the product itself

```yaml
stakeholders:
  - role: Normal users / operators — automated callers
    representatives:
      - CI / CD pipeline (pre-commit, pre-merge gates)
      - Authoring skills (Phase 5; subprocess callers during draft-check)
      - Review skills (Phase 5; subprocess callers during verdict)
      - Retrofit skills (Phase 5; subprocess callers during leaf / branch / root recovery)
    source_chain: single-hop surrogate (author projects agent needs from §10/§11/§12)
    primary_concerns:
      - structured JSON output
      - deterministic exit codes
      - non-interactive invocation
      - stdin accepted for inputs
      - idempotent retries

  - role: Normal users / operators — human callers
    representatives:
      - Framework adopter engineers running ad-hoc validation locally
    source_chain: direct (author is adopter #1)
    primary_concerns:
      - readable --format text output
      - actionable errors with file + rule + fix
      - --help with examples at every subcommand

  - role: Maintainers / on-call
    representative: Author at Phase 4 kickoff; core-maintainer team post-Phase-6
    source_chain: direct
    primary_concerns:
      - stable CLI contract to minimise breakage incidents
      - structured logs
      - semver-clean versioning

  - role: Support / training
    representative: "n/a — no staffed support. Documentation via docs/guide + progressive --help with examples."
```

### Containing system — interfaces and beneficiaries

```yaml
  - role: Interfacing systems
    representatives:
      - CI systems (GitHub Actions, GitLab CI, Jenkins, and equivalents)
      - pre-commit hook frameworks
      - vmodel-author (scaffolder / renderer; depends on core via library or subprocess — product ADR decides)
      - vmodel-retrofit (gap-report aggregator; consumes trace graph)
    interface_contract: stable CLI per TARGET_ARCHITECTURE §10 (Output discipline + AI-ergonomic CLI)
    source_chain: direct (contract anchored in §10)

  - role: Functional beneficiaries
    representatives:
      - Spec authors (artifacts are validated; read error output via CI or skill verdicts)
      - PR reviewers (consume validation summaries as review context)
      - Future Build-workflow agents (read the derived trace graph; §8.4 deferred)
    source_chain: single-hop surrogate
    primary_concerns:
      - no false positives that erode gate trust
      - stable query contract
      - concise error summaries

  - role: Product champions
    representative: "Stefanus Bergenhem — framework author, adopter #1"
    source_chain: direct

  - role: Technical leadership / rigor-posture observers
    representatives:
      - Engineering leaders in adopter orgs tracking spec-quality and traceability posture across projects
      - Compliance / quality functions needing periodic evidence that rigor gates hold
    source_chain: single-hop surrogate (author projects need from practitioner experience with ISO-26262 traceability-visualisation tools; no direct interview)
    primary_concerns:
      - high-level rigor-posture dashboards (gap trends, coverage percentages, ADR-consequence closure rates)
      - periodic evidence artefacts rather than real-time query
      - structured JSON output that drives non-vmodel dashboarding tools
```

### Wider environment — regulators, negatives, suppliers, purchasers

```yaml
  - role: Regulators
    representative: "n/a — no regulatory surface for this developer tooling artifact. Downstream projects that vmodel-core helps document may themselves carry regulatory constraints; vmodel-core does not itself trigger any."

  - role: Negative stakeholders
    representatives:
      - "Teams wanting to bypass rigor gates: friction is by design (principle #3, uniform high rigor; no opt-out). Recorded explicitly, not apologised for."
      - "Alternative validator authors: none visible at Phase 4 kickoff. Future competing implementations are a supported outcome per principle #6 (projects declare their tool subset), not a negative."

  - role: Partners / suppliers (ecosystem dependencies)
    representatives:
      - JSON Schema draft 2020-12 implementations (validation dependency)
      - Mermaid parser (diagram-in-artifact input)
      - Markdown parser
      - Language / runtime chosen by product ADRs (candidate set Go / Python / Rust; decision deferred to product spec chain)
    source_chain: direct (tech picks internal to the product)

  - role: Purchasers / sponsors
    representative: "context-dependent. The install decision is instantiated per adopter — org lead in an enterprise adoption, platform team in a large codebase, individual engineer in a private project. No single named purchaser fixed at Phase 4."
    source_chain: undetermined; role resolves only at adoption time.
```

## Problem

The framework's rigor claims collapse to aspirational prose without mechanical enforcement, and without a query capability the traceability graph stays tacit. Every non-trivial adoption, greenfield or retrofit, hits this wall within the first spec tree big enough for a human to stop holding in their head.

The problem is not *"we need a mechanical validator tool"* — that is a solution. The world-facing problem is that the framework's rigor discipline cannot hold without something mechanically enforcing it and something mechanically surfacing the state of the traceability graph.

### Compound consequences as the spec tree grows

1. **Human reviewers become de-facto mechanical validators.** Attention that principle #8 reserves for design intent gets spent on link integrity, schema conformance, and orphan checks. The framework's entire leverage premise — *"the human reviews specs, not code"* — fails because the human is reviewing spec mechanics instead of spec meaning.

2. **AI authoring and review skills cannot self-verify.** Without a trusted external oracle, a skill's assessment of its own output is circular. Agent-authored artifacts look structurally plausible while quietly violating allocation, verification, or ADR-consequence rules. Errors compound because no mechanical gate catches them before they propagate to downstream scopes.

3. **Traceability stays tacit.** The framework's promises — *"what tests verify REQ-042?"*, *"what's the impact of changing ADR-012?"*, *"which leaves have a DD but no TestSpec?"* — require eyeballing artifacts instead of querying a graph. The traceability model (`TARGET_ARCHITECTURE §7`) is undelivered.

4. **Mechanical errors surface late.** Caught at PR review, integration handoff, or retrofit completion — where remediation is expensive. The early-catch economics where rigor must either pay off or degrade into bureaucracy are not established.

5. **Uniform high rigor (principle #3) is unsustainable by hand.** The framework explicitly rejects tiers and relaxed variants. Maintaining the bar manually scales O(n²) with spec-tree size; past the smallest trees, *"check every link by eye"* is not a real policy.

6. **Rigor without visible payoff looks bureaucratic.** When a traceability link is added manually and its value is invisible — no query, no dashboard, no alert when it's missing — the link looks like paperwork. Maintainers stop caring; links rot; negative stakeholders (teams who feel gated for no visible benefit) gain legitimate grievance. `vmodel-core`'s query engine and structural gates turn latent links into visible gates: coverage gaps surface on demand, integrity breaks raise errors, distance-to-target for an ISO-26262-style traceability bar becomes measurable rather than opinion. Without this feedback loop, the rigor discipline degrades back to rhetoric and the learning-curve cost of adopting the framework is paid without the cost being recouped.

### Evidence

Honestly weak at Phase 4 authoring time. Stated as hypothesis plus internal observations; external adopter data will test the hypothesis and may surface consequences not named here.

- **Internal evidence.** Phase 3's own fixture-validation work (13 commits) built `jsonschema` + `referencing` tooling ad-hoc to validate six fixtures against six schemas. The absence of `vmodel-core` was felt during the framework's own construction — the framework could not eat its own dog food without building a throwaway validator first.

- **External adopter evidence.** None. Phase 4 precedes any adoption. The author acknowledges the problem floor is hypothesis-grounded in framework-design logic plus the Phase 3 in-house experience.

- **Inference from analogues.** Rigor frameworks without mechanical enforcement (historical observations of hand-audited ISO 26262 / DO-178C compliance in industry) tend to degrade into *"check the box"* rather than *"prove it holds."* `vmodel-core` exists to keep the framework on the *"prove it holds"* side. Cited to engineering-codex where codified; no claim is made beyond well-established industry observation.

- **Practitioner observation (automotive / ISO 26262 context).** The author has seen in practice that traceability-visualization tools — showing current coverage, orphan links, and distance from the required link structure — bring real value during ISO 26262 adoption. Teams use them to focus remediation and to convert abstract rigor requirements into actionable work lists. Mechanical visibility of gap-state transforms *"rigor"* from audit preparation into day-to-day engineering signal. The effect generalises: it is the visibility of the gap, not the specific standard, that drives the value.

- **Prior art — Doorstop.** A requirements / traceability management tool in the same functional category that the author has used in practice. `vmodel-core` is not Doorstop and is not trying to be — the artifact set here is broader (six types, not requirements-centric), the rigor model is different (uniform high via Quality Bar, no tier configuration), and AI-ergonomic CLI is a first-class concern — but the category has precedent. Existing tools in this space demonstrate that mechanical traceability management is a viable, valued product shape.

## Desired Outcomes

Per-stakeholder-group narrative of the post-`vmodel-core` state, anchored to the roles named in §A. Each outcome describes what is observably different once the tool is in place — not the features that produce the difference. Aspirational adjectives (*"better"*, *"easier"*, *"more efficient"*) are avoided; each outcome commits to something a reviewer could picture recognising.

### 1. Automated callers — CI pipelines and Phase 5 skills

Before `vmodel-core`: CI has no spec-rigor gate; authoring skills cannot self-check drafts; review skills must interleave mechanical and semantic judgment; retrofit skills have no deterministic way to surface orphans. After: spec-rigor enforcement runs automatically at commit and merge; authoring and review skills iterate against a mechanical oracle before the human sees the draft; retrofit skills emit gap reports as a first-class output. Errors are caught at their source rather than propagating to downstream scopes.

### 2. Human adopter engineers — local ad-hoc use

Before: the engineer writes a spec, pushes, and waits hours or days for CI or PR review to surface violations. After: violations surface within seconds of the edit, locally, with file + rule + fix pointing the way. The framework's learning curve becomes a tight iteration loop measurable in minutes per artifact rather than a multi-day guessing exercise, and the `--help` subsystem lets a new adopter discover the CLI progressively rather than drowning in upfront documentation.

### 3. Spec authors and reviewers — functional beneficiaries

Before: reviewers eyeball compliance; authors receive *"validation failed"*-style error output. After: authors receive pointed errors they can act on without guessing; reviewers trust that mechanical violations are caught upstream and can focus attention on design intent — the attention the framework was built to unlock under principle #8.

### 4. Maintainers and interfacing systems

Before: each integration with a CI platform, hook framework, or sibling tool (`vmodel-author`, `vmodel-retrofit`) needs bespoke wiring and bespoke debugging. After: the stable CLI contract per `TARGET_ARCHITECTURE §10` lets integrations drop in with minimal configuration; upgrades are routine thanks to semver-clean versioning; on-call debugging relies on structured logs rather than reverse-engineering the tool's behaviour.

### 5. Negative stakeholders — teams under bypass pressure

Before: teams who feel gated for no visible payoff either block indefinitely or work around the framework, corrupting the spec tree with undocumented shortcuts. After: the `gap-report` output lays out every failing rule with file, rule, and fix, so the friction is **visible, itemised, and addressable**. What the team does next is their call — remediate, opt out at project level, or escalate for a framework-level conversation — but the options are now explicit and principled rather than implicit and evasive. The framework's position (stated, not hidden) is that a team whose friction cost exceeds its rigor-value should opt out at framework-level, not at tool-level, per `§6 "no relaxed variants"`; `vmodel-core`'s output makes that decision *informable* rather than forcing it.

### 6. Technical leadership — rigor-posture observers

Before: leaders tracking rigor posture across an adopter org live in a *"semi-trust, semi-hope"* regime — engineers say the gates hold, and leaders hope no unseen gap will surface during audit or incident review. After: the `gap-report` output feeds high-level dashboards that show rigor posture at a glance — coverage percentages, orphan counts, ADR-consequence closure rates, distance from an ISO-26262-style traceability bar. The practitioner-observed effect from standards-driven contexts (§B *Evidence*) applies here: visibility of gap-state converts rigor from audit preparation into day-to-day engineering signal that leaders can act on without becoming engineers themselves. Where adopter orgs lack observability infrastructure, `gap-report --format html` produces a self-contained browsable page directly — rigor visibility does not require a dashboarding stack to exist.

### 7. The framework itself — meta-outcome

Before: rigor claims live as prose in `TARGET_ARCHITECTURE`; traceability is a model on paper. After: rigor is mechanically enforced across every adopter and every spec tree; traceability is a graph that can be queried on demand. Principle #8 — human leverage at the spec level, not the code — becomes a measurable property of the system rather than an aspiration.

### Purchasers / sponsors (context-dependent)

Outcome is context-dependent because the decision-maker role is not fixed at Phase 4 (see §A). The common thread across adoption contexts: an adopter decision-maker can justify the install by pointing at quantifiable evidence — compliance rates, gap-report trends, query-output examples — rather than arguing from principle.

## Operational Concept

Per PB doc §2 D: seven-question compact form covering mission, environment, scenarios, modes, users, support, and performance in operation. Architecture decisions (specific tech, language, package format) are deliberately deferred to product ADRs.

### 1. Mission (operational terms, not architectural)

`vmodel-core` mechanically validates the structural rigor of `VModelWorkflow` spec artifacts and makes the derived traceability graph queryable. It runs in three modes of invocation: **on-demand** (a human engineer types a command), **on-trigger** (pre-commit hook or CI pipeline stage), and **in-subprocess** (a Phase 5 authoring, review, or retrofit skill calls it from its own harness). Every invocation reads artifact files from disk (or stdin), applies deterministic checks or graph queries, and emits structured results on stdout.

### 2. Environment

- **Developer workstations** — local ad-hoc validation during authoring; expected on Linux, macOS, and Windows (no runtime prerequisites per Hard Constraint #5).
- **CI runners** — `vmodel-core` is invoked as a build step on whichever CI platform the adopter uses (GitHub Actions, GitLab CI, Jenkins, Buildkite, Azure Pipelines, and equivalents).
- **Pre-commit hook frameworks** — the binary is called as a hook script on the developer's machine before commit is allowed to complete.
- **Phase 5 skill execution harnesses** — called as a subprocess by AI-agent harnesses hosting authoring, review, or retrofit skills.
- **Dependencies at runtime:** read access to the project's `specs/` directory (or inputs piped via stdin), plus stdout/stderr for structured output. No persistent state, no network calls, no external database.
- **Surroundings:** the containing project's filesystem, its CI config, its pre-commit configuration, and (at the leadership-dashboard level) whatever observability stack the adopter org runs — or, where no observability stack exists, a static HTML page produced directly by `vmodel-core`.

### 3. Scenarios (end-to-end stories of normal use)

**Scenario A — Pre-commit validation on a single edited artifact.**
A human adopter engineer authors a `requirements.md` file locally. On save, the pre-commit hook fires `vmodel-core validate <file>`. The tool parses, schema-validates, checks cross-references, and exits either clean (code 0) or with a list of violations tagged *file + rule + fix* (non-zero code). The engineer iterates against the tool in seconds until it passes, then commits.

**Scenario B — CI gate on a PR affecting multiple scopes.**
A PR opens or updates. The CI job invokes `vmodel-core validate --dir specs/ --format json`. The tool walks every artifact affected by the diff plus their transitive trace-graph neighbours, emits a JSON violation list, and exits with non-zero if any rule fails. CI renders the summary as a PR check. Human review begins only after the mechanical gate is green, focusing attention on design intent rather than rule compliance.

**Scenario C — Authoring skill self-verifying its draft.**
A Phase 5 `vmodel-skill-author-requirements` produces a draft artifact inside its own reasoning loop. Before handing the draft to a human reviewer, it calls `vmodel-core validate --stdin --format json`, pipes the draft in, reads the structured violation list, adjusts, and re-pipes until the output is clean. Only then does the skill emit its deliverable. Review skills (`vmodel-skill-review-*`) follow the same pattern for structural gates, freeing their LLM tokens for semantic judgment.

**Scenario D — Traceability query during impact analysis.**
An ADR is proposed for change. The engineer (or a Build-workflow agent, post-`§8.4`) runs `vmodel-core query impact --adr ADR-012 --format json`. The tool returns the candidate-set of downstream artifacts that may need updates per `§8.3`. The engineer works through the set, updating or explicitly closing each as *"no change needed."*

**Scenario E — Gap-report snapshot for a leadership dashboard.**
A scheduled job runs `vmodel-core query gap-report --format json` on a cadence (for example, nightly) and posts the result to an adopter-org observability stack — *or* runs `--format html` and posts a self-contained browsable page when the adopter has no dashboarding infrastructure. A technical leader opens the dashboard or the HTML page and sees current rigor posture at a glance — coverage percentages, orphan counts, ADR-consequence closure rates, distance from a stated traceability bar. Gap trends feed leadership's prioritisation of remediation work.

### 4. Modes

- **Normal.** On-demand or triggered invocation; tool parses, runs the requested operation, emits results, exits. Stateless between invocations.
- **Degraded.** When a required input is missing, malformed, or inconsistent with the spec-tree assumptions, the tool emits a specific actionable error, points at the failing piece, and exits with a non-zero code. No silent degradation, no best-effort partial results that could be mistaken for success.
- **Startup / shutdown.** Each invocation is a short-lived process — no daemon, no server. Startup and shutdown are per-invocation and take whatever the host OS's process-spawn cost is. (A future ADR may introduce a long-running daemon mode if query-latency targets demand it; out of scope at Phase 4.)
- **Emergency.** `vmodel-core` is developer tooling, not a runtime system — there is no *"service down"* emergency. If the binary itself fails to execute, the containing project's CI or pre-commit tooling fails; recovery is reinstall, pin to a prior version, or temporarily disable the hook.
- **Maintenance.** Upgrades happen via standard package management (specific mechanism is ADR territory). No in-tool maintenance mode.

### 5. Users and roles

References §A by role:

- **Human adopter engineers** — Scenarios A, D (local ad-hoc; interactive iteration).
- **CI / CD pipelines** — Scenario B.
- **Phase 5 authoring, review, and retrofit skills** — Scenario C (and variants of B, D for the review/retrofit cases).
- **Technical leadership / rigor-posture observers** — Scenario E.
- **Build-workflow agents (future, deferred per `§8.4`)** — Scenario D.

### 6. Support

- **Core maintainer team** (post-Phase-6) is responsible for bug triage, CLI-contract evolution, and release management.
- **No 24/7 on-call expected** — `vmodel-core` is developer tooling, not a runtime service.
- **First-line support** — the documentation subsystem (`--help` per subcommand with examples) plus the adopter project's own `specs/` context.
- **Defect reporting** — via the project's issue tracker (specific channel deferred to product ADRs).
- **Adopter-internal support** — platform teams or TPLs in adopter orgs provide internal support for their own integrations. Framework maintainers do not provide consulting.

### 7. Performance in operation

Targets sufficient to honour the Outcomes named in §C:

- **Interactive invocation on a single artifact** — well under one second wall-clock, perceptibly instant. Slower than this and Scenario A's iteration-loop value collapses.
- **Full spec-tree validation on a typical ~100-artifact tree** — single-digit seconds. Slower and CI adds measurable PR-throughput friction; faster costs nothing.
- **Common queries** (what-verifies-X, what-allocates-X on a single ID) — must feel interactive, comparable to the single-artifact validation target.
- **Heavy queries** (full impact set across the tree; nightly gap-report over the whole graph) — single-digit seconds acceptable; these are not on the human-iterative-loop critical path.

These are operational targets; they harden into measurable NFRs in §E *Constraints*.

## Constraints

The load-bearing section per PB doc §2 E — *the one whose omission silently shapes a wrong architecture.* Three categories: hard constraints (imposed from outside), architecture-driving NFRs (quality attributes the tool must meet), and assumptions (beliefs that, if wrong, invalidate the Brief).

### Hard constraints (imposed from outside)

1. **Mechanical-only execution path (principle #5).** No LLM is invoked anywhere in `vmodel-core`'s runtime. Every check, query, and graph operation is deterministic and reproducible. Any capability requiring interpretation belongs in a Phase 5 skill, not in this tool.
2. **CLI / subprocess invocation model for external callers (`§10` *Integration interface*).** Phase 5 skills and third-party adopter callers invoke `vmodel-core` as a subprocess and consume structured stdout. Sibling tools (`vmodel-author`, `vmodel-retrofit`) may link `vmodel-core` as a library *or* invoke as subprocess — each sibling product's ADR decides. No library API is exposed to external (skill or adopter) callers.
3. **Stateless between invocations.** No database, no on-disk cache, no daemon. Each invocation reads from disk or stdin, computes, writes to stdout/stderr, exits. (A future ADR may introduce a daemon mode if query latency demands it; that decision is outside this PB.)
4. **Read-only on the adopter's spec tree.** `vmodel-core` never writes, renames, or deletes artifacts. Reading is its only filesystem mutation surface. Load-bearing for adopter trust and for the Spec Ambiguity Test — a validator that silently rewrites cannot be the source of truth for what passed.
5. **Self-contained binary distribution.** The tool ships as a single executable that runs on Linux, macOS, and Windows without requiring any language runtime, interpreter, JVM, or shared-library installation beyond what the OS provides natively. No `pip install`, no JVM, no Node.js, no Python. Adopter install is *download-and-run*; CI install is one binary in the runner image. This pre-narrows the product-ADR language choice to compiled, statically-linkable runtimes (Go, Rust, Zig, or similar); the specific pick is deferred to product ADRs.
6. **Uniform high rigor (`§3 #3`, `§6`).** No per-adopter relaxation flags, no `--lenient` mode, no rigor-tier configuration. The framework's stance — *"if rigor is not needed, the framework is not the right tool"* — is enforced at the tool level by absence of opt-out.
7. **Open-source distribution.** `vmodel-core` is released as open-source software. Specific licence and distribution channel deferred to product ADRs.

### Architecture-driving NFRs — full Discovery-Checklist walk

```yaml
nfr_walk:
  performance_and_scale:
    availability:
      target: "n/a — short-lived per-invocation process. Per-invocation success is the operability surface; structural failures emit specific exit codes and diagnostics, never silent partial success."
      load_bearing: false

    latency:
      target: |
        - Single-artifact validation: well under 1s wall-clock (interactive
          iteration loop in §D Scenario A).
        - Common queries (what-verifies-X): comparable to single-artifact
          target; must feel interactive.
        - Full-tree validation on ~100-artifact tree: single-digit seconds.
        - Heavy queries (impact set across full tree, nightly gap-report):
          single-digit seconds acceptable; not on interactive critical path.
      load_bearing: true
      scenario_form: see quality-attribute scenarios below

    throughput_and_scale:
      target: "Spec trees up to ~1,000 artifacts handled within stated latency targets at Phase 4 baseline. Beyond ~5,000 artifacts, see Assumption #1 invalidation trigger."
      load_bearing: true

  security_privacy_compliance:
    security:
      target: "n/a — no authentication surface; tool runs in adopter's process space with adopter's filesystem permissions. Tool must not open network connections, must not exfiltrate, must not modify state outside its own logging."
      load_bearing: true (negative — see Negative Requirements below)

    privacy_and_compliance:
      target: "n/a — tool processes whatever artifacts the adopter places in scope; does not log, transmit, or persist artifact content. Adopter retains ownership and compliance accountability for the spec content itself."
      load_bearing: false

  data_and_integration:
    data:
      target: "n/a — tool is stateless; adopter's spec files are the data, owned by adopter. Tool emits ephemeral stdout/stderr only. Backups, retention, durability are not the tool's concern."
      load_bearing: false

    compatibility:
      target: |
        - Platform: Linux, macOS, Windows. No runtime dependency beyond
          the OS — see Hard Constraint #5.
        - CLI output contract: stable per §10, semver-versioned, breaking
          changes only at major version bumps.
        - JSON output schema: published, versioned independently of binary
          version where the JSON shape is intended to outlive the binary
          (gap-report, query results consumed by external dashboards).
        - `--format html` available on report-flavoured output
          (gap-report, validate, query). Self-contained single-file HTML,
          no external assets, no JS framework dependency. Suitable for
          unit-test-tool-style human review and for static hosting in a
          leadership dashboard pipeline without dashboard infrastructure.
        - New artifact-type support: ships within one release cycle of the
          framework adding the type; existing CLI contract unchanged.
      load_bearing: true

  evolution_and_operation:
    extensibility:
      target: "Adding new artifact types, new traceability link types, or new validation rules must be possible without breaking the CLI contract. Internal extensibility mechanism (plugin / first-class module / regenerated binary) deferred to product ADRs."
      load_bearing: true

    operability_and_observability:
      target: |
        - Structured logs to stderr (level-tagged; --verbose, --quiet flags).
        - Exit codes carry semantic meaning (0 clean; specific non-zero
          codes for distinct failure classes — schema violation,
          traceability violation, malformed input, system error).
        - JSON output for the gap-report scenario (§D Scenario E) must be
          stable across patch versions to support third-party dashboarding.
      load_bearing: true

    cost:
      target: "n/a — open-source distribution per Hard Constraint #7. Adopter cost is install + integration time only; no per-invocation pricing, no SaaS dependency."
      load_bearing: false
```

### Quality-attribute scenarios (load-bearing NFRs)

```yaml
- name: latency_interactive_validation
  stimulus: an engineer runs `vmodel-core validate <single-artifact>`
  source: human adopter engineer at §D Scenario A iteration loop
  environment: normal mode, single artifact, no prior cache
  artifact: vmodel-core CLI process
  response: parse, schema-validate, cross-reference-check, emit result, exit
  response_measure: p95 wall-clock under 1s on a developer-grade laptop

- name: latency_ci_full_tree
  stimulus: CI invokes `vmodel-core validate --dir specs/ --format json`
  source: CI / CD pipeline
  environment: normal mode, ~100-artifact spec tree, cold start
  artifact: vmodel-core CLI process
  response: walk every artifact, check structure + cross-refs, emit JSON
  response_measure: p95 wall-clock under 5s; CI step does not become rate-limiting on PR throughput
```

### Three classes stakeholders rarely articulate

**Negative requirements (shall-not).**

1. Shall not write, rename, or delete files outside its own logging output.
2. Shall not open network connections during any validation or query operation.
3. Shall not embed or invoke LLMs as part of its runtime path.
4. Shall not provide a per-adopter "lightweight" or "lenient" mode.
5. Shall not exit zero on partial failure or emit output that could be confused with success when checks have failed.

**Implicit requirements** (beliefs the PB rests on).

1. Adopter spec artifacts are laid out per `TARGET §5.4` directory convention (configurable per-project, but conformant in shape).
2. Adopter has a working OS install (Linux, macOS, or Windows). No further runtime prerequisites — see Hard Constraint #5.
3. Adopter's CI / pre-commit infrastructure can call subprocesses and consume their structured output.
4. Adopter accepts CLI-and-JSON as the integration surface for `vmodel-core`. No GUI, no web UI, no IDE plugin in scope (third parties may build adapters atop the CLI contract).

**Interaction requirements** (behaviour from how subsystems combine).

1. **`vmodel-core` × `vmodel-author`** — `vmodel-author` consumes parsing and schema-validation capabilities. Whether via library re-use or subprocess invocation is each product's ADR call; the CLI contract is the integration guarantee.
2. **`vmodel-core` × `vmodel-retrofit`** — `vmodel-retrofit` consumes trace-graph queries and gap-report aggregation. Same integration mechanism choice as above.
3. **`vmodel-core` × Phase 5 skills** — skills call `vmodel-core` via subprocess (per `§10`), consume structured JSON, and reason about exit-code semantics. Skills do not embed `vmodel-core` as a library.
4. **`vmodel-core` × adopter CI platform** — integration is via exit codes + JSON. Rendering JSON to platform-specific PR-comment formats (GitHub Checks, GitLab MR notes) is the adopter's CI configuration concern, not `vmodel-core`'s.

### Assumptions (with invalidation triggers)

1. **Spec trees stay below ~1,000 artifacts in real adopter use.** Invalidation: real-adopter tree exceeds ~5,000 artifacts and validation / query latency degrades past §D #7 targets — would force a daemon-mode or persistence-cache ADR.
2. **The six-artifact set is stable enough to be a long-term validation contract.** Invalidation: a Phase 5+ design change adds, removes, or substantially restructures an artifact type; `vmodel-core`'s per-artifact validators would then be moving targets requiring continuous CLI evolution.
3. **JSON-on-stdout suffices for leadership-dashboard consumption (§C Outcome #6 / §D Scenario E).** Invalidation: real adopter dashboarding needs streaming, pub-sub, or incremental updates that batch JSON cannot serve — would require an export-mode ADR or a sibling tool.
4. **CLI subprocess invocation fits all foreseeable external callers.** Invalidation: a Phase 5+ skill or adopter AI harness needs in-process performance that subprocess fork cost cannot meet — would force a library-API ADR and revisit `§10`'s subprocess-only stance for skills.
5. **No LLM is needed in `vmodel-core`'s runtime path, ever.** Invalidation: query needs evolve toward natural-language *"tell me what changed if I update ADR-012"* requests — those become a Phase 5 query-translator skill that calls `vmodel-core`, not `vmodel-core` itself. Hard rule, not a soft target.

### Open Questions

Genuine unknowns that need resolution. Distinct from Assumptions above (beliefs we hold pending invalidation) — these are gaps where we have no answer yet and a forward decision is required. Each carries owner + resolution trigger so the question survives this PB and gets answered downstream.

```yaml
open_questions:
  - id: OQ-1
    question: Language / runtime choice for vmodel-core implementation.
    context: Hard Constraint #5 narrows the option space to Go, Rust, Zig, or similar (compiled, statically-linkable, zero runtime prerequisites). The specific pick is undetermined.
    owner: Stefanus (initial author) + post-Phase-6 maintainer team.
    resolution_target: vmodel-core product ADR authored at Phase 6 kickoff.

  - id: OQ-2
    question: Is a long-running daemon mode required to meet query-latency targets?
    context: Hard Constraint #3 + §D Mode #4 reserve this option but defer the decision. Per-invocation startup cost may or may not be acceptable for high-frequency query use under realistic adopter spec-tree sizes.
    owner: Stefanus.
    resolution_target: post-personal-pilot evaluation against §E latency QA scenarios; revisit if p95 targets are missed.

  - id: OQ-3
    question: Library-vs-subprocess integration between sibling vmodel-* tools.
    context: Hard Constraint #2 allows either; each sibling product's ADRs decide independently.
    owner: vmodel-author and vmodel-retrofit product spec authors.
    resolution_target: at sibling-product Architecture or Detailed Design authoring, Phase 6.

  - id: OQ-4
    question: CLI subcommand-tree shape — resource-verb ordering, namespace boundaries, and query language for the graph.
    context: §10 sets the AI-ergonomic CLI principles but the specific subcommand tree (e.g., `vmodel-core validate schema` vs. `vmodel-core schema validate`) is product-internal architecture.
    owner: vmodel-core product spec author (Stefanus initially).
    resolution_target: vmodel-core Architecture artifact at Phase 6.

  - id: OQ-5
    question: Project config format (e.g., `.vmodel/tools.yaml`) for declaring which tool products an adopter has installed.
    context: BACKLOG §6 Q4. Cross-product framework concern; affects vmodel-core's discovery of sibling tools and Phase 5 framework skills' invocation logic.
    owner: framework lead (Stefanus).
    resolution_target: before Phase 5 framework skills reference it.
```

## Non-Goals

`vmodel-core` is deliberately not the following. Each non-goal closes off a gravitational pull that the tool will resist as it evolves.

1. **Not an artifact authoring tool.** `vmodel-core` only reads artifacts; it never creates or modifies them. No `vmodel-core fix` subcommand, no auto-correction, no template generation. Authoring is `vmodel-author`'s job.
2. **Not a renderer of artifacts.** The artifact content itself (markdown + YAML + Mermaid) is rendered to HTML or PDF by `vmodel-author`, not by `vmodel-core`. *Reports about artifacts* — validation summaries, gap reports, query results — are rendered as HTML by `vmodel-core` directly via `--format html` (see §E NFR compatibility), the same way unit-test runners produce their own HTML reports rather than delegating to a separate renderer. The distinction: artifact rendering = `vmodel-author`; report-about-artifact rendering = `vmodel-core`.
3. **Not a topology-discovery tool.** Reverse-engineering scope trees from code is `vmodel-retrofit`'s job. `vmodel-core` operates on artifacts that already exist.
4. **Not a code generator.** No source code, scaffolding, or implementation artifacts produced. Code generation belongs to the (deferred) Build workflow.
5. **Not a runtime for framework skills.** Phase 5 skills run in their own AI-agent harnesses; they call `vmodel-core` as a subprocess. `vmodel-core` does not host, schedule, or invoke skills.
6. **Not an orchestrator.** `vmodel-core` does not coordinate Spec / Build / Retrofit workflows or sequence operations across phases. Orchestration is a Phase 5 framework skill (`vmodel-skill-orchestration`).
7. **Not a UI tool.** No web UI, no GUI, no IDE plugin. CLI + structured output (JSON / text / HTML) only. Third parties may build dashboards or IDE adapters atop the CLI contract; those are not part of `vmodel-core`.
8. **Not a real-time monitor.** Runs on-demand or on-trigger; does not watch the filesystem, push notifications, or run continuously. Scheduled invocation is the adopter's CI / cron concern.
9. **Not a backwards-compatibility shim for legacy V-model tooling.** Operates on the post-pivot artifact set only. Legacy DOORS exports, ASPICE-tier-templated documents, and pre-pivot framework artifacts are out of scope.
10. **Not a configuration manager.** `vmodel-core` may read project config (e.g., `.vmodel/tools.yaml` per `BACKLOG §6 Q4`) but does not author or modify it.

## Success Criteria

The Success Criteria section answers a single question per PB doc §G: *did the post-deployment world change in the way the Brief predicted?* Each criterion anchors back to a named Outcome in §C and is measurable, observable, falsifiable.

For `vmodel-core` at Phase 4 authoring time, this section is split into two categories: **engineering-completeness criteria** (committed; serve as Phase 6 Build closure conditions) and **outcome-validation criteria** (deliberately deferred until personal-pilot reps stabilise the product shape and adoption strategy). The split, the reasoning, and the deferral trigger are recorded explicitly below.

### Engineering-completeness criteria — closure conditions for Phase 6 Build

Honest closure conditions, not outcome bets. They tell the Build phase when to stop and root-scope TestSpec what to verify. They do not claim anything about world-impact.

| # | Criterion | Anchors | Measurement |
|---|---|---|---|
| **SC-EC1** | The product builds and runs on Linux, macOS, and Windows with zero runtime prerequisites beyond a working OS install. | Outcome #4; Hard Constraint #5 | CI cross-platform smoke test. |
| **SC-EC2** | All NFR targets in §E are met or exceeded under the conditions described in the §E quality-attribute scenarios. | Outcomes #1, #2; §E QA scenarios | Performance test suite. |
| **SC-EC3** | `--format json`, `--format text`, and `--format html` all produce well-formed, schema-conformant output across the documented subcommands (`validate`, `query`, `gap-report`). | Outcomes #1, #6; §E NFR compatibility | Output-shape conformance test. |
| **SC-EC4** | All thirteen traceability validation rules (`schemas/traceability/validation-rules.catalog.json`) and all six per-artifact Quality Bar structural checks produce correct verdicts on documented positive and negative test inputs. | Outcomes #1, #3 | Automated rule-coverage test suite. |
| **SC-EC5** | A real spec-authoring task on one of Stefanus's own projects completes end-to-end using `vmodel-core` as the validation gate, without requiring out-of-band patches to the tool. | Outcomes #2, #7 | Self-tracked first non-trivial use. |
| **SC-EC6** | The new-artifact-type extension path is exercised end-to-end against a synthetic seventh artifact type; the existing CLI contract for the original six is unchanged. | Compatibility NFR | Integration test. |

### Outcome-validation criteria — deliberately deferred

> At Phase 4 authoring time, the author has insufficient experience with the product to commit to outcome-validation criteria. The product shape, dominant use-cases, and adoption strategy (personal vs. organisational) are not yet stable enough to predict which world-state changes constitute success. Forcing outcome bets at this stage would risk anchoring iteration to the wrong target, in tension with the *measure-to-learn* posture appropriate for an exploratory dogfooded tool.
>
> **Trigger to author this category.** Revisit §G after the author has used `vmodel-core` on at least two real spec-authoring tasks across his own projects, *and* after the personal-vs-organisational adoption decision has firmed up. Until then, the engineering-completeness criteria above serve as the closure condition for Phase 6 Build, and the world-impact question is deliberately held open.
>
> **Stance.** Deferral is honest; fiction is not. A blank section here is preferable to fictive metrics committed without grounding.

### Framework-level note

This deferred-outcome-criteria pattern is the first dogfooding feedback Phase 4 has produced about the Phase 2 PB craft doc. The PB doc currently frames §G as outcome-validation only and does not accommodate exploratory or early-stage products. Whether Section G should formally allow this two-tier split — engineering-completeness + outcome-deferral — is logged as a Phase 4 learning to be addressed in a Phase 2 docs revision after the three Phase 4 PBs are complete (see `PHASE4_AUTHORING_PATTERN.md` *Open Points*).
