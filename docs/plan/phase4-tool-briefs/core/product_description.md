---
id: PD
artifact_type: product-description
title: "vmodel-core — Product Description"
summary: "vmodel-core is the deterministic CLI for validating VModelWorkflow spec artifacts, querying the derived traceability graph, and producing rigor-posture reports — invoked by humans, CI, and Phase 5 framework skills."
status: draft
date: "2026-04-26"
version: 1
---

<!--
  Product Description (PD) for vmodel-core. PD is the engineering-flavoured
  alternative to the business-flavoured Product Brief (PB) — see Finding #9
  in PHASE4_AUTHORING_PATTERN.md for the rationale and history.

  This PD stages in docs/plan/phase4-tool-briefs/core/ during Phase 4 and
  lifts to the vmodel-core repo root at Phase 6 kickoff. The artifact is
  designed to be self-contained when its dependency manifest (§6) is
  vendored alongside it.

  Authoring state (2026-04-26): pilot complete; first concrete PD; schema
  for the PD artifact type to be authored after pilot fan-out (vmodel-author
  + vmodel-retrofit) per Phase 4 schema-after-artifact decision.
-->

## 1. Vision

`vmodel-core` is the deterministic command-line tool that validates VModelWorkflow spec artifacts and queries the derived traceability graph. It is invoked by humans during local authoring, by CI pipelines as a pre-commit and pre-merge gate, and by Phase 5 framework skills as a subprocess during draft, review, and retrofit work. Every invocation is mechanical — no language model is involved — and produces structured output (`--format json | text | html`) on stdout.

`vmodel-core` is the first of three sibling tool products under VModelWorkflow's three-product structure (`TARGET_ARCHITECTURE §10`); `vmodel-author` (scaffolding / rendering) and `vmodel-retrofit` (legacy reverse-engineering) depend on it. It is open-source, ships as a self-contained binary with no runtime prerequisites beyond the host operating system, and is intended for adopter install via download-and-run.

## 2. Functional Scope

Capabilities the tool delivers. Each one or two lines; downstream Requirements artifact details semantics.

- **Parse** — read VModelWorkflow `.md` artifacts into a structured representation usable by every other capability. Parse surface: YAML front-matter (`---` fenced at file start), Markdown body, embedded YAML blocks (triple-backtick-fenced with `yaml` language tag), Mermaid blocks (triple-backtick-fenced with `mermaid` language tag, retained as raw source). Configuration discovery — where the spec tree root lives and which schema set is active — depends on the project config format defined by the framework; first-priority ADR (see §5 Assumption #3 + `BACKLOG §6 Q4`). Foundation; every other capability depends on this.
- **Schema-validate** — check each parsed artifact against its per-type JSON Schema (envelope + common-defs + per-artifact). Reports schema violations with file + path + diagnostic.
- **Trace-validate** — check link integrity (every `derived_from` / `allocates` / `verifies` / `governing_adrs` resolves to a real artifact), completeness (no orphan components, every requirement allocated, every TestSpec case has non-empty `verifies`, every leaf has a DD + TestSpec), and cycles (no circular `derived_from` or `supersedes`). 13 rules at v1.
- **Quality-Bar-validate** — run the structural checks expressed in per-artifact Quality Bar JSON files; honour `applies_at` filtering and `references` cross-checks. Reports per-group and per-item pass-fail.
- **Build graph** — derive the cross-artifact traceability graph from front-matter and embedded YAML blocks. In-memory only per stateless constraint; rebuilt on each invocation.
- **Query** — answer the v1 fixed-set of queries against the derived graph. Five queries at v1, no others:
    - `what-verifies` — given an artifact ID, return the test cases / TestSpec entries whose `verifies` includes it.
    - `what-allocates` — given a requirement ID, return the Architecture children whose `allocates` includes it.
    - `impact` — given any artifact ID, return the candidate-set per `TARGET §8.3` candidate-set propagation.
    - `coverage` — given a scope, return per-artifact-type completeness against the framework's expected artifact set.
    - `leaves-missing` — return leaves lacking a Detailed Design or TestSpec.

    Query *language* (CLI flag shape, filter syntax) deferred to Architecture. Query *set* is fixed at v1 — adding new queries is a v1.x minor-version capability decision.
- **Gap report** — aggregate validation findings, traceability completeness gaps, and Quality Bar failures into a single report. Supports all three output formats; the HTML form is a self-contained browseable page suitable for static hosting on a leadership dashboard.

## 3. Non-Functional Scope

### Hard constraints (immutable)

- **No LLM** in the runtime path. Mechanical and deterministic only. Any capability requiring interpretation belongs in a Phase 5 skill, not in this tool.
- **Stateless** between invocations. No daemon, database, or persistent cache. Each invocation reads inputs, computes, writes output, exits.
- **Read-only** on the adopter's spec tree. Never writes, renames, or deletes spec artifacts. (Reports about artifacts go to stdout/stderr only; vmodel-author handles artifact-side mutations.)
- **OS-only dependency.** Self-contained binary; no JVM, interpreter, or shared library beyond what the OS provides natively. Pre-narrows the implementation to compiled, statically-linkable runtimes (Go / Rust / Zig / similar; specific choice deferred to ADR).
- **External callers use CLI subprocess.** Phase 5 skills and third-party adopter callers invoke as subprocess and consume structured stdout. Sibling tools (`vmodel-author`, `vmodel-retrofit`) may library-link or invoke as subprocess; each sibling product's ADR decides.
- **No relaxation modes.** No per-adopter `--lenient` flag, no rigor-tier configuration. Uniform high rigor per `TARGET §3 #3` is enforced by absence of opt-out — if rigor is not needed, the framework is not the right tool.
- **Open-source distribution.** Specific licence deferred to product ADR.

### Performance

- Single-artifact validation: p95 < 1 s on developer-grade hardware.
- Common queries (single-ID lookup): comparable to single-artifact target.
- Full-tree validation on ~100-artifact tree: p95 < 5 s.
- Heavy queries (full-tree impact, gap-report over full graph): single-digit seconds; not on interactive critical path.

### Scale

- Spec trees up to ~1,000 artifacts handled within stated targets at v1 baseline.
- Beyond ~5,000 artifacts: invalidates Assumption #1 (per-invocation startup acceptable; daemon mode reopens).

### Compatibility

- **Platform:** Linux, macOS, Windows. Zero runtime prerequisites beyond OS.
- **CLI contract:** semver-versioned; breaking changes only at major version bumps.
- **JSON output schema:** published, versioned independently of binary version where the JSON shape is intended to outlive the binary (gap-report and query results consumed by external dashboards).
- **HTML output:** self-contained single-file HTML, no external assets, no JS framework dependency, no CDN includes. Tabular data renders as native HTML tables. Where structure is the point — traceability graph snippets in gap reports, query result trees — diagrams render as **server-side-generated inline SVG** embedded in the HTML body. No client-side Mermaid runtime; no browser-side rendering dependency.
- **New artifact-type support:** ships within one release cycle of the framework adding the type; existing CLI contract for the original six unchanged.

### Security and privacy

- No network access in any operation.
- No file mutation outside the tool's own logging or output streams.
- No data persistence; ephemeral stdout/stderr only. The adopter retains compliance accountability for the spec content itself.

### CLI ergonomics and operability

The CLI is designed for both human and AI-agent callers as first-class users. The principles below are **working hypotheses** drawn from a single practitioner observation (Zakariasson, 2026; see §6) — not yet established design canon. They should be re-evaluated against pilot evidence and against a synthesised codex pattern page (`pat-cli-design-for-ai-agents`) once that page is authored. Until the rationale substrate is hardened, treat the principles as a starting position rather than a fixed contract; revisions during pilot are expected and welcomed.

- **Output formats:** `--format json` (default for pipe; structured), `--format text` (default for TTY; human-readable tables and colour), `--format html` (self-contained reports). Default selection is TTY-aware.
- **Errors are actionable:** every error reports file, rule, and what to fix. Never bare *"validation failed."*
- **Non-interactive by default:** every input passable as a flag; interactive prompts are fallback only when flags are missing.
- **Idempotent:** running the same operation twice yields the same result; agents retry safely.
- **Predictable resource + verb subcommand structure:** once an agent learns one pattern, it can guess others.
- **Accepts stdin for all inputs:** pipelines are first-class composition.
- **Progressive `--help`:** top-level lists subcommands; each subcommand `--help` includes examples.
- **Returns structured data on success:** ids, paths, durations as fields, not decorative formatting.
- **`--yes` / `--force`** bypass any confirmation prompts that may exist (no destructive operations at v1; flags reserved for forward consistency).
- **Structured logs** to stderr, level-tagged; `--verbose` and `--quiet` flags.
- **Exit codes** carry semantic meaning. The full v1 exit-code map:

    | Code | Meaning |
    |---|---|
    | `0` | Clean success — all checks passed; output written. |
    | `1` | Schema violation — one or more artifacts failed JSON-Schema validation. |
    | `2` | Traceability violation — one or more rules in `validation-rules.catalog.json` failed. |
    | `3` | Quality Bar violation — one or more structural QB checks failed. |
    | `4` | Malformed input — parse failure, missing required input, unreadable file, etc. |
    | `5` | System error — I/O failure, internal panic, resource exhaustion, etc. |

    No `--dry-run` flag at v1 — `vmodel-core` is read-only and stateless; the standard *"preview a destructive operation"* mental model does not apply. If a future capability introduces destructive behaviour, `--dry-run` returns to scope at that point.

## 4. Out of Scope

- **Not an artifact authoring tool.** (`vmodel-author`)
- **Not a renderer of artifacts** — the spec markdown / YAML / Mermaid. (`vmodel-author`) Reports *about* artifacts (validation, gap, query) *are* rendered as HTML by `vmodel-core` itself via `--format html`.
- **Not a topology-discovery tool.** (`vmodel-retrofit`)
- **Not a code generator.** (Build workflow.)
- **Not a Phase 5 skill runtime.** Skills run in their own AI-agent harnesses and call `vmodel-core` as a subprocess.
- **Not a workflow orchestrator.** (`vmodel-skill-orchestration`)
- **Not a UI / GUI / IDE plugin tool.** Third parties may build atop the CLI contract.
- **Not a real-time monitor.** Runs on-demand or on-trigger; does not watch the filesystem or push notifications.
- **Not a backwards-compatibility shim** for legacy V-model tooling (DOORS exports, ASPICE-tier templates, pre-pivot framework artifacts).
- **Not a configuration manager.** May read project config; does not author or modify it.

## 5. Assumptions

Beliefs the PD rests on. If invalidated, the PD requires update.

- **Per-invocation startup cost is acceptable** for the stated latency targets (§3 *Performance*) at v1 scale (~1,000-artifact spec trees). *Invalidation:* p95 latency targets missed in pilot use; reopens daemon-mode as a Hard-Constraint candidate.
- **An implementation language exists in the candidate set** (Go / Rust / Zig / similar) that satisfies all Hard Constraints simultaneously. *Invalidation:* no candidate satisfies OS-only-dependency + performance + extensibility together; reopens the language-choice scope.
- **Project config format will be specified by the framework** before Phase 5 skills consume it (see §6 *Framework reference documents*). *Invalidation:* skills land before the format is defined; `vmodel-core` would need to define a stub format ad-hoc.
- **Sibling-tool integration mode (library-link vs subprocess) is decided as a shared ADR** between `vmodel-core`, `vmodel-author`, and `vmodel-retrofit` before any sibling's Requirements artifact is considered complete. *Invalidation:* the ADR is never authored or the three tools land on different sides of the choice; `vmodel-core`'s public API surface (what gets exported as a library vs what stays internal behind the CLI) cannot be authoritatively specified until this resolves.

## 6. References and Dependencies

`vmodel-core` is one of three sibling tool products in the VModelWorkflow framework. To continue design and build in a fresh repo, the following must be available locally — vendored, submoduled, or accessible by published reference URL.

### Framework reference documents (required for design)

- **`TARGET_ARCHITECTURE.md`** — the framework's architectural reference. Critical sections for `vmodel-core`:
  - `§3` — ten core principles (uniform high rigor, tool/skill split, retrofit no-fabrication, …).
  - `§5` — artifact model, scope tree, file shape, ID conventions.
  - `§6` — Quality Bar rigor mechanism.
  - `§7` — traceability model and link types (catalog of nine link types).
  - `§8.3` — update mode and candidate-set propagation (consumed by the *Query — impact* capability).
  - `§10` — three-product structure, *Output discipline*, *AI-ergonomic CLI* principles (the latter inlined in §3 above).
- **`BACKLOG.md`** — execution plan; relevant items: `§6 Q4` (project config format).

### Schema and rules files (required at runtime — `vmodel-core`'s input data)

The tool consumes these as the things-to-be-validated. They must be vendored or published-and-pinned alongside the source code:

- `schemas/artifacts/envelope.schema.json` + `common-defs.schema.json` — universal envelope and shared `$defs` registry.
- `schemas/artifacts/{product-brief, requirements, architecture, adr, detailed-design, test-spec}.schema.json` — six per-artifact schemas (JSON Schema draft 2020-12).
- `schemas/artifacts/quality-bar/quality-bar.schema.json` — meta-schema for Quality Bar checklists.
- `schemas/artifacts/quality-bar/{...}.quality-bar.json` — six per-artifact Quality Bar checklists (one per artifact type).
- `schemas/traceability/link-types.{schema,catalog}.json` — link type definitions (nine types at v1).
- `schemas/traceability/validation-rules.{schema,catalog}.json` — 13 traceability validation rules.

### Sibling tool products (informative — own repos)

- **`vmodel-author`** — scaffolder + renderer of artifacts. Depends on `vmodel-core` via library-link or subprocess (its own ADR decides). Has its own PD.
- **`vmodel-retrofit`** — topology discovery + gap-report aggregator for legacy reverse-engineering. Depends on `vmodel-core` for trace-graph queries. Has its own PD.

### Downstream consumers (informative — establish the integration contract `vmodel-core` must hold)

- **Phase 5 framework skills** — per-artifact authoring and review skills (twelve total) plus framework skills (orchestration, retrofit, traceability). All invoke `vmodel-core` via subprocess and consume structured JSON.
- **Adopter CI pipelines and pre-commit hooks** — invoke `vmodel-core` as a build or commit gate; consume exit codes plus structured output.
- **Adopter dashboards or static-hosted HTML** — consume `vmodel-core`'s `gap-report --format json` output (for programmatic dashboarding) or `--format html` output (for direct human browse without a dashboard stack).

### External references cited in this PD

- `engineering-codex/wiki/sources/src-zakariasson-clis-for-agents-2026.md` — single practitioner observation (Zakariasson, 2026) that is the **only current rationale** for the CLI-for-AI principles inlined in §3 *CLI ergonomics and operability*. The codex flags this source as low-confidence (user-attested rather than fetch-verified). A synthesised codex pattern page — provisionally `engineering-codex/wiki/patterns/pat-cli-design-for-ai-agents` — should be authored to harden the rationale chain before the §3 principles are treated as a fixed contract. **Status:** pattern page does not yet exist; the §3 principles are working hypotheses pending corroboration.
