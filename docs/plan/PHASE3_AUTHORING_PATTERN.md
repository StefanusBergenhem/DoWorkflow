# Phase 3 — Authoring Pattern & Session Handoff

Status document for Phase 3 (schemas). Mirrors the Phase 2 precedent (`PHASE2_AUTHORING_PATTERN.md`, archived on Phase 2 completion). Archive this file on Phase 3 completion.

Load this file at the start of every Phase 3 session.

---

## 1. Status as of 2026-04-22

**Phase 3 work to date (11 commits):**

| Commit | Step | What |
|---|---|---|
| `2993385` | Phase 3 prep | Archive 4 pre-pivot schemas (`sw-architecture`, `sw-requirement`, `system-requirement`, `system-test-case`) + EARS dir. Drop speculative `priority` field from `requirements.html`. |
| `a00181f` | Envelope + shared defs | Author `envelope.schema.json` + `common-defs.schema.json` as JSON Schema draft 2020-12. Archive pre-pivot-tainted `artifact-envelope.schema.yaml`. |
| `7c0dca8` | Authoring pattern | Phase 3 session handoff / authoring pattern doc (this file). |
| `5b84d62` | #5 Product Brief schema | Pattern-setter for per-artifact schemas. Front-matter-only validation; `id` const `"PB"`; `summary` required; `recovery_status: reconstructed` banned at scalar + map levels (human-only artifact). |
| `bf0fb76` | Status-wording sweep | Reconcile PB + Requirements HTML pages to the universal lifecycle enum — `accepted` → `active` in 6 places. ADR page intentionally kept on Nygard convention (see §2 lock below). |
| `7742783` | #6 Requirements schema | Front-matter + `$defs/requirement` for per-req embedded-YAML blocks. Artifact `id` prefix `REQS-`; per-req `id` prefix `REQ-` (do not conflate). `rationale_recovery_status` narrowed to `verified \| unknown` (no-fabrication on human-only rationale). |
| `831bf2d` | Handoff-doc update | Log PB / sweep / Requirements commits; lock ADR status override in §2 Lifecycle; refresh recommended next step. |
| `4a6a4d7` | Envelope status relaxation | Refactor motivated by ADR override need: envelope owns status *shape* (required, non-empty); per-artifact schemas own *vocabulary* (enum). See §2 Vocabulary ownership lock. Touches envelope + PB + Requirements. |
| `0dcba63` | #8 ADR schema | Third per-artifact schema. Nygard status enum override (`proposed \| accepted \| superseded`); `id` requires mandatory kebab slug; `scope_tags` minItems 1; `supersedes` / `superseded_by` tightened to ADR id pattern; `recovery_status` narrowed on four human-only keys (context, alternatives_considered, rationale, consequences). Archive pre-pivot `adr.schema.yaml`. |
| `a392c94` | #7 Architecture schema | Fourth per-artifact schema. `id` pattern `ARCH` / `ARCH-{flattened-scope}`; universal lifecycle vocabulary; `governing_adrs` tightened to ADR id pattern; top-level `recovery_status` left broad (Architecture content is code-observable); `$defs/decomposition_child` (required `id`/`purpose`/`responsibilities`/`allocates`; `allocates` tightened to REQ id pattern; no `maxItems` on `responsibilities` — rigor lives in Quality Bar; `rationale` as `string` OR `{status: verified\|unknown, note}`; per-child `recovery_status` scalar) and `$defs/interface` (required `name`/`from`/`to`/`protocol`/`contract`; contract required `operation`/`preconditions`/`postconditions.on_success`; `errors` shape with kebab `code`, 4xx/5xx `http`, `meaning`; empty `quality_attributes` object rejected). No pre-pivot file to archive — `sw-architecture.schema.yaml` archived in `2993385`. |
| _this commit_ | #9 Detailed Design schema | Fifth per-artifact schema. `id` pattern `DD-{flattened-scope}-{name}` (no structural scope/name split enforced); universal lifecycle; `parent_architecture` required + tightened to ARCH id pattern; `derived_from` minItems 1; `governing_adrs` tightened to ADR pattern. `recovery_status` scalar broad (DD's dominant content is code-observable); map form narrows `overview` key to `verified\|unknown` (Overview carries intent — human-only under no-fabrication rule), other map keys stay broad. `$defs/public_interface_entry` lifts DbC shape from arch (preconditions / postconditions.on_success\|on_failure / invariants / typed errors) but sub-fields are optional — shape vs completeness split; errors shape lighter than arch's (no http / kebab-code — DD errors describe raise/return, not wire); postcondition split is 2-way (on_success / on_failure — error attribution belongs in the Error Handling matrix, not DbC); `thread_safety` enum of 5 categories including `thread-hostile`. `$defs/data_structure_entry`: required `name` + `fields` (each field required `name` + `type`, optional `invariant`); `ownership` / `lifetime` / `returned_semantics` optional strings. `$defs/error_matrix_row`: 5-col per HTML (`error`/`detection`/`containment`/`recovery`/`caller_receives`); `recovery` closed enum (`fail-fast\|retry\|fallback\|compensate\|propagate`). HTML drift flagged: front-matter example shows `scope_tags` (plural) but TARGET §5.3 specifies singular `scope`+`parent_scope` — schema follows TARGET; HTML sweep deferred to §6. Pre-pivot `detailed-design.schema.yaml` (Layer 1/2/3 model) archived in same commit. |

**Pending tasks (Phase 3 backlog):**

| # | Task | Notes |
|---|---|---|
| 3 | Quality Bar YAML container shape | Its own session. Do before #12. |
| 10 | test-spec schema | New; mandatory non-empty `verifies`. |
| 11 | traceability link-types + validation rules | Parallel-able with per-artifact work; good short-session candidate. |
| 12 | Extract Quality Bar YAML per artifact | Depends on #3. |
| 13 | Minimal-example fixtures per artifact | Depends on all schemas. |

**Pre-pivot-tainted schemas still in place:** none. All pre-pivot artifact schemas archived.

---

## 2. Decisions Locked (do not reopen without cause)

### Format & tooling

- **JSON Schema draft 2020-12.** Not YAML, not custom. Rationale: off-the-shelf validation (ajv, python-jsonschema, santhosh-tekuri/jsonschema for Go) → the future "schema validator" tool (`TARGET_ARCHITECTURE §10`) is a thin CLI wrapper, not a bespoke parser.
- **File extension: `.schema.json`** (pure JSON; broadest tooling compatibility).
- **Commit cadence: one per step.** Matches Phase 2 rhythm.

### Schema identity

- **`$id` form:** `https://vmodelworkflow.dev/schemas/<domain>/<name>/v1`.
  - Placeholder domain (`vmodelworkflow.dev`); not required to resolve today. Gives us a publish path later without rework.
  - Path version (`/v1`) bumps on breaking schema changes.

### Cross-schema references

- Shared `$defs` live in `common-defs.schema.json`. Per-artifact schemas `$ref` by `$id` URI with a fragment pointer (`#/$defs/<def_name>`).
- Per-artifact schemas compose envelope via `allOf`:
  ```json
  {
    "allOf": [
      { "$ref": "https://vmodelworkflow.dev/schemas/artifacts/envelope/v1" },
      { "type": "object",
        "required": ["scope", "parent_scope"],
        "properties": { "...": "per-artifact extras" } }
    ]
  }
  ```

### Universal envelope fields (`envelope.schema.json`)

- **Required:** `id`, `title`, `artifact_type`, `status`, `date`, `version`.
- **Optional:** `recovery_status`, `supersedes`, `superseded_by`.
- `additionalProperties: true` so per-artifact schemas can add fields.

### Lifecycle

- **Universal enum:** `draft | active | superseded`. Default for all artifact types; lives in `common-defs#/$defs/lifecycle_status`.
- **ADR exception — locked 2026-04-22, shipped `0dcba63`.** `adr.schema.json` overrides the universal `status` enum to Nygard's `proposed | accepted | superseded`. Preserved the 15-year ADR community convention rather than force uniformity; one principled exception costs less than fighting reader expectations across every ADR tool and article.
- **NOT `deprecated`** — explicitly rejected. Wrong metaphor for specs (specs are in force or not; they don't fade).

### Vocabulary ownership — locked 2026-04-22, shipped `4a6a4d7`

- **Envelope owns shape; per-artifact schemas own vocabulary.** The envelope declares `status` as required + non-empty string without mandating a specific enum. Each per-artifact schema supplies its vocabulary via `$ref common-defs#/$defs/lifecycle_status` (universal default — PB, Requirements) or inline `enum` (ADR's Nygard override).
- **Rationale.** `allOf` in JSON Schema narrows intersections but cannot override a `$ref`-enforced enum. Pushing vocabulary to per-artifact schemas is architecturally clean (envelope = universal structure, per-artifact = domain terms) and avoids conditional `if/then/else` patterns that would bake artifact-specific knowledge into the envelope. Discovered while writing the ADR schema; the refactor was applied before ADR landed.
- **Implication for future per-artifact schemas.** Every new per-artifact schema must explicitly declare its `status` property override in its inline `allOf` branch — inheriting from the envelope alone no longer enforces a vocabulary.

### Versioning — Model A

- Single live file per artifact on disk = the active version.
- `version: integer` — monotonic, bumped on material revisions.
- Git carries history. No filesystem multi-version coexistence.
- Supersession via `supersedes` / `superseded_by` links — rare, for when a differently-named artifact replaces a predecessor (common for ADRs).

### Recovery status (retrofit)

- Envelope field: `recovery_status: string | object`.
- Scalar form applies artifact-wide; object form maps field/section names to statuses.
- Allowed scalar values: `verified | reconstructed | unknown`.
- **No-fabrication rule** (enforced in per-artifact schemas): `reconstructed` banned on human-only fields — ADR context/alternatives/rationale, Requirements rationale, Product Brief content, DD *intent*. Allowed states on those fields: `verified | unknown` only.

### Scope path regex

- `^$|^[a-z0-9]+(-[a-z0-9]+)*(/[a-z0-9]+(-[a-z0-9]+)*)*$`
- Empty string = root scope.
- Kebab-case segments separated by `/`.

---

## 3. HTML-Derived Commitments

Phase 2 HTML pages are authoritative for per-artifact content; schemas must honor their commitments. Inventory from Haiku subagent scans run this session:

### Product Brief (`product-brief.html`)

- Single file `/specs/product_brief.md` (root only).
- Front-matter: `id` (const `"PB"`), `title`, `summary`, `status`, `date`, `recovery_status` (per-section map).
- 7 sections per TARGET §5.3 (Stakeholders, Problem, Desired Outcomes, Operational Concept, Constraints, Non-Goals, Success Criteria).

### ADR (`adr.html`)

- Path: `/specs/adrs/adr-NNN-{slug}.md`; flat directory.
- Front-matter: `id`, `title`, `status`, `date`, `scope_tags`, `supersedes` / `superseded_by`, `affected_scopes`, `context`, `decision`, `alternatives_considered`, `rationale`, `consequences`.
- **Mandatory Reversibility sub-prompt** inside Consequences (TARGET §5.3).

### Requirements (`requirements.html`)

- Path: `/specs/{scope}/requirements.md` (non-leaf scopes).
- Front-matter: `id`, `title`, `parent_scope`, `status`, `date`, `derived_from`, `governing_adrs`.
- Per-req embedded YAML: `id`, `statement`, `rationale` (required), `acceptance` (optional), `derived_from`.
- **`priority` was removed** this session. Do not re-add unless a concrete need surfaces.

### Architecture (`architecture.html`)

- Path: `/specs/{scope}/architecture.md`; root at `/specs/architecture.md`.
- Front-matter (HTML commits to): `id`, `scope`, `parent_scope`, `derived_from`, `governing_adrs`, `recovery_status`.
- **ADD** `title`, `status`, `date` in the schema — arch.html under-specified, universal consistency wins.
- 6 mandatory sections: Metadata, Overview, Structure Diagram, Decomposition, Interfaces, Composition.
- **Per-child** (Decomposition): `id`, `purpose` (one sentence, no conjunctions), `responsibilities` (1–3), `allocates` (list of REQ IDs). Optional: `rationale`, `recovery_status`.
- **Per-interface:** `name`, `from`, `to`, `protocol`, `contract` (nested). Contract has Design-by-Contract shape:
  - `operation`, `preconditions`, `postconditions.{on_success, on_precondition_failure, on_downstream_failure}`, `invariants`, `errors[{code, http, meaning}]`, `quality_attributes`.
- **Composition section:** runtime pattern (named with rationale), sequence diagrams (Mermaid, happy + 1–2 failure paths), middleware stack (ordered), DI / composition-root strategy, message-bus topology where applicable.
- **Root-scope-only additions:** environments, orchestration target, runtime-unit boundaries + deployment cardinality, IaC reference, integration-test targets.

### Detailed Design (`detailed-design.html`)

- 7 sections: Metadata, Overview, Public Interface, Data Structures, Algorithms, State, Error Handling.
- Metadata: `scope_tags`, `parent_architecture`, `derived_from`, `governing_adrs`.
- **Pre-pivot Section 8 "Test Strategy" removed** — DD references leaf TestSpec via traceability.

### TestSpec (`testspec.html`)

- Path: `/specs/.../testspec.md` per scope (every scope has one).
- Envelope: `id`, `title`, `type`, `verifies` (**MANDATORY non-empty list**), `preconditions`, `inputs`, `steps`, `expected`.
- `type` enum — **11 values** per TARGET reconciliation:
  `functional | boundary | error | fault-injection | property | state-transition | contract | performance | security | accessibility | error-guessing`.
- Reserved growth fields for Phase 3: `fixtures`, `data`, `expected_range`.

---

## 4. Authoring Rhythm (per per-artifact schema)

1. **Read** `docs/guide/artifacts/<type>.html` — authoritative for content.
2. **Read** the matching `TARGET_ARCHITECTURE §5.3` entry — short, authoritative for structure.
3. **Draft** the schema — compose envelope via `allOf`, add per-artifact fields, tighten `id` pattern, declare required sections.
4. **Define embedded-YAML validators** where applicable (per-req, per-case, per-child) as `$defs` in the per-artifact schema.
5. **Meta-validate + smoke-test** — see §5 Validation Recipe.
6. **Archive** the pre-pivot-tainted file (if any) as part of the same commit.
7. **Commit** per-step: `Phase 3: <artifact> schema as JSON Schema 2020-12`.

---

## 5. Validation Recipe

A venv lives at `/tmp/vmodel-schema-check` with `jsonschema` + `referencing` installed. If recreating:

```bash
python3 -m venv /tmp/vmodel-schema-check
/tmp/vmodel-schema-check/bin/pip install --quiet jsonschema referencing
```

Validation template:

```python
/tmp/vmodel-schema-check/bin/python <<'EOF'
import json
from jsonschema import Draft202012Validator
from referencing import Registry, Resource

common = json.load(open('schemas/artifacts/common-defs.schema.json'))
env    = json.load(open('schemas/artifacts/envelope.schema.json'))
new    = json.load(open('schemas/artifacts/<type>.schema.json'))

Draft202012Validator.check_schema(common)
Draft202012Validator.check_schema(env)
Draft202012Validator.check_schema(new)

registry = Registry().with_resources([
    ('https://vmodelworkflow.dev/schemas/artifacts/common-defs/v1', Resource.from_contents(common)),
    ('https://vmodelworkflow.dev/schemas/artifacts/envelope/v1',    Resource.from_contents(env)),
    ('https://vmodelworkflow.dev/schemas/artifacts/<type>/v1',      Resource.from_contents(new)),
])
v = Draft202012Validator(new, registry=registry)

# --- smoke tests ---
good = { ... minimal valid instance ... }
print('ok:', list(v.iter_errors(good)))

for name, bad in [
    ('missing X',     { ... }),
    ('bad enum Y',    { ... }),
    ('bad pattern Z', { ... }),
]:
    errs = list(v.iter_errors(bad))
    print('rej' if errs else 'FAIL', name, errs[:1])
EOF
```

**Smoke-test checklist per schema:**
- Minimal valid example (all required present).
- One failure per declared constraint (missing required, bad enum, bad pattern, bad date format).
- Retrofit example with `recovery_status` scalar + map forms.
- Supersession example with `status: superseded` + `superseded_by`.

---

## 6. Open Architectural Points (defer decisions)

- **Quality Bar container shape (task #3).** Its own session. HTML sections are authoritative for content; task is to design the YAML shape that tools consume. Must precede task #12 extraction.
- **`priority` field.** Dropped from requirements.html this session. Reintroduce only when grounded in actual use.
- **HTML front-matter under-specification.** Several artifact pages (architecture, detailed-design, testspec) do not show `title` / `version` / `artifact_type` in their front-matter tables despite these being envelope-universal. Schemas drive consistency; HTML wording sweep is a follow-up when enough drift accumulates.
- **`partially_verified` recovery_status value** in `product-brief.html` retrofit example (line ~607) is not in the common-defs enum (`verified | reconstructed | unknown`). Content defect in the page example; fix when next touching the PB page.

---

## 7. References

- `CLAUDE.md` — project conventions, working discipline.
- `docs/plan/TARGET_ARCHITECTURE.md` — §3 principles, §5 artifact model, §6 rigor, §7 traceability, §10 tools.
- `docs/plan/BACKLOG.md` — Phase 3 scope in §3.3.
- `docs/guide/artifacts/*.html` — six authored artifact pages (authoritative for content).
- `schemas/artifacts/envelope.schema.json`, `schemas/artifacts/common-defs.schema.json` — Phase 3 foundation.
- MEMORY: `~/.claude/projects/-home-stefanus-repos-VModelWorkflow/memory/MEMORY.md`.

---

## 8. Recommended Next Step

**Task #10 — TestSpec schema.** Sixth and final per-artifact schema; new artifact with no pre-pivot file to archive. The load-bearing rule is **mandatory non-empty `verifies`** at both the envelope (artifact-level — the TestSpec must verify some upstream spec) and per-case level (every TC must trace to at least one spec element). This is the first cross-artifact mandatory-link constraint encoded inside a schema. `type` enum is the 11-value set per §3 TestSpec (functional | boundary | error | fault-injection | property | state-transition | contract | performance | security | accessibility | error-guessing). Reserved growth fields per HTML: `fixtures`, `data`, `expected_range`. Universal lifecycle; `id` pattern `TS` / `TS-{flattened-scope}`; per-case `id` pattern `TC-TS-{scope}-{seq}` (don't conflate: TS identifies the document, TC identifies a case inside it — same authoring rule as REQS/REQ from the Requirements schema). Per-case `verifies` items reference any spec id (REQ / ARCH / DD / PB); keep broad via `artifact_id_ref`, tightening to a specific pattern would block the legitimate cross-layer case. Follow §4 Authoring Rhythm.

Alternative 1: **Task #11 — traceability link-types + validation rules.** Small, TARGET §7 is well-specified, parallel-able with per-artifact work. Good for a short session or running alongside #10.

Alternative 2: **Task #3 — Quality Bar YAML container shape.** Its own session. Blocks task #12 (per-artifact QB extraction). HTML sections (`docs/guide/artifacts/*.html` §5) are authoritative for content; task is purely the container shape. Natural to schedule after #10 lands, so that all six per-artifact schemas are in place before the QB extraction design locks.
