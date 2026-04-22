# Phase 3 — Authoring Pattern & Session Handoff

Status document for Phase 3 (schemas). Mirrors the Phase 2 precedent (`PHASE2_AUTHORING_PATTERN.md`, archived on Phase 2 completion). Archive this file on Phase 3 completion.

Load this file at the start of every Phase 3 session.

---

## 1. Status as of 2026-04-22

**Phase 3 work to date (2 commits):**

| Commit | Step | What |
|---|---|---|
| `2993385` | Phase 3 prep | Archive 4 pre-pivot schemas (`sw-architecture`, `sw-requirement`, `system-requirement`, `system-test-case`) + EARS dir. Drop speculative `priority` field from `requirements.html`. |
| `a00181f` | Envelope + shared defs | Author `envelope.schema.json` + `common-defs.schema.json` as JSON Schema draft 2020-12. Archive pre-pivot-tainted `artifact-envelope.schema.yaml`. |

**Pending tasks (Phase 3 backlog):**

| # | Task | Notes |
|---|---|---|
| 3 | Quality Bar YAML container shape | Its own session. Do before #12. |
| 5 | product-brief schema | Simplest; **recommended pattern-setter**. |
| 6 | requirements schema | Rewrite from scratch. |
| 7 | architecture schema | Deepest (DbC contract shape). |
| 8 | adr schema | Rewrite; universal lifecycle applies. |
| 9 | detailed-design schema | Rewrite; drops pre-pivot Layer 1/2/3 model. |
| 10 | test-spec schema | New; mandatory non-empty `verifies`. |
| 11 | traceability link-types + validation rules | Parallel-able with per-artifact work. |
| 12 | Extract Quality Bar YAML per artifact | Depends on #3. |
| 13 | Minimal-example fixtures per artifact | Depends on all schemas. |

**Pre-pivot-tainted schemas still in place (to be archived as their replacements land):**
- `schemas/artifacts/adr.schema.yaml` (pre-pivot sections + fields)
- `schemas/artifacts/detailed-design.schema.yaml` (pre-pivot Layer 1/2/3 model)

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

- **Universal enum:** `draft | active | superseded`. Applies to all six artifact types including ADR (semantic match: proposed ≡ draft, accepted ≡ active).
- **NOT `deprecated`** — explicitly rejected. Wrong metaphor for specs (specs are in force or not; they don't fade).

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

- **ADR status wording.** Unified to `draft | active | superseded`. If industry-standard `proposed | accepted | superseded` is preferred, add a per-artifact enum override in `adr.schema.json` — one-line change.
- **Quality Bar container shape (task #3).** Its own session. HTML sections are authoritative for content; task is to design the YAML shape that tools consume. Must precede task #12 extraction.
- **`priority` field.** Dropped from requirements.html this session. Reintroduce only when grounded in actual use.

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

**Task #5 — Product Brief schema.** Root-only, simplest per-artifact shape, pattern-setter for the remaining five. Follow §4 Authoring Rhythm.

Alternative: **Task #11 — traceability link-types + validation rules.** Small, TARGET §7 is well-specified, parallel-able with per-artifact work.
