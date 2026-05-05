# Retrofit discipline (DD)

In retrofit mode, structure is recoverable from code; intent is not.

## Contents

- [`recovery_status` vocabulary](#recovery_status-vocabulary)
- [Schema-enforced refusal A — Overview narrowing](#schema-enforced-refusal-a--overview-narrowing)
- [The observed-vs-interpreted split](#the-observed-vs-interpreted-split)
- [Honest vs laundered — slot-fill comparison](#honest-vs-laundered--slot-fill-comparison)
- [Citation form for `reconstructed` evidence](#citation-form-for-reconstructed-evidence)
- [Per-section retrofit posture](#per-section-retrofit-posture)
- [Pair every `unknown` with a follow-up](#pair-every-unknown-with-a-follow-up)
- [Retrofit gap report — required section](#retrofit-gap-report--required-section)

## `recovery_status` vocabulary

| Value | Meaning | Allowed on |
|---|---|---|
| `verified` | Human-confirmed accurate | All sections |
| `reconstructed` | Observed from code with evidence cited | Public Interface, Data Structures, Algorithms, State, Error Handling |
| `unknown` | Human-only with no preserved source | Overview (intent), rationale fields |

**Rationale fields are never `reconstructed`** — refusal A. Allowed: `verified` (with cited human source) or `unknown`.

## Schema-enforced refusal A — Overview narrowing

The DD schema's `recovery_status` map form narrows the `overview` key:

```json
"overview": { "enum": ["verified", "unknown"] }
```

Schema rejects `recovery_status: { overview: reconstructed }`. The Overview carries intent (why this leaf exists, what invariants must hold) — not derivable from observable behaviour.

Other map keys (`public_interface`, `data_structures`, `algorithms`, `state`, `error_handling`) accept the full vocabulary because those sections are largely code-observable.

## The observed-vs-interpreted split

| Observable from code | Human-only |
|---|---|
| Public function signatures, return types, exceptions raised | Why the boundary was drawn here |
| Data structure fields, observable invariants | Original intent behind a field that has drifted |
| Sequential pseudocode (extractable from method bodies) | Whether a quirk is a feature or a forgotten workaround |
| State transitions (observable from code paths) | Whether observed behaviour is intended behaviour |
| Errors raised, propagated, caught | Why a particular recovery strategy was chosen |

When asked for content from the right column with no human source → output `unknown`, never an invention.

## Honest vs laundered — slot-fill comparison

```yaml
# LAUNDERED — refuse to ship
recovery_status:
  overview: reconstructed       # ← schema rejects
  rationale: reconstructed      # ← refusal A

## Overview
After evaluating token formats in the 2019 platform refresh, the team selected
HMAC-SHA256 with a 30-minute lifetime to balance security against ergonomics.
# ← committee prose; no preserved source; could defend any lifetime
```

```yaml
# HONEST — correct retrofit output
recovery_status:
  overview: unknown
  public_interface: reconstructed
  state: reconstructed

## Overview
unknown — no preserved design notes for this leaf. Observable behaviour
described below; the why of those choices is an open question.

## Rationale
status: unknown
note: |
  Observed lifetime (src/auth/token.py:14, config/prod.yaml:30). Forces
  driving the choice not recoverable.
follow_up:
  owner: "@auth-team-lead"
  action: "Confirm under current threat model; supply rationale or propose revision."
```

## Citation form for `reconstructed` evidence

Every `reconstructed` field cites at least one of:

- File path + line range (`src/auth/token.py:42-78`)
- Commit hash + brief description (`abc123 — initial import, 2021-06`)
- Schema artifact (`db/migration/V019__add_session_table.sql`)
- Operational log reference (`prod logs 2024-09 #INC-4521`)

`derived_from` in retrofit cites observed evidence:

```yaml
derived_from:
  - "observed_behaviour: src/auth/token.py:14-95"
  - "observed_config: config/prod.yaml:auth.session_ttl_minutes=30"
```

Vague `derived_from: [auth, security]` (categories, not artifacts) → finding `check.retrofit.derived-from-vague`.

## Per-section retrofit posture

| Section | Typical status | Notes |
|---|---|---|
| Metadata | `reconstructed` (where derivable from package structure) | — |
| Overview | `verified` (when team source) OR `unknown` — **never `reconstructed`** | Schema enforces |
| Public Interface | `reconstructed` (signatures, observable invariants) | Per-clause rationale `unknown` when no source |
| Data Structures | `reconstructed` (fields, observable invariants); ownership / lifetime often `unknown` | Shared-mutable semantics often need human confirmation |
| Algorithms | `reconstructed` only when contractual (rare); otherwise "implementation choice; observable from code" | Avoid pseudocode-paraphrasing — refusal C |
| State | `reconstructed` (transitions); undefined-event handling often `unknown` | — |
| Error Handling | `reconstructed` (errors raised); recovery strategies often `unknown` per row | — |

## Pair every `unknown` with a follow-up

```yaml
follow_up:
  owner: "@<team-or-person>"
  action: "<concrete next step>"
  deadline: "<date or sprint marker>"
```

`unknown` without follow-up → finding `check.retrofit.unknown-without-followup`.

## Retrofit gap report — required section

Populate four buckets:

| Bucket | What lives here |
|---|---|
| **Lost rationale** | Per-decision `unknown` with follow-up owners |
| **Behavioural drift** | Where observed code differs from what tests assert |
| **Missing ADRs** | Load-bearing decisions in production with no preserved record — emit `[DEFER-ADR: ...]` |
| **Coverage gaps** | Public functions with no observable test coverage; tests with no DD postcondition |

Retrofit DD without a populated Gap report → finding `anti-pattern.laundered-retrofit`.

## Cross-link

`rationale-capture.md` · `adr-extraction-cues.md` (`[DEFER-ADR]` markers in retrofit) · `anti-patterns.md` · refusal A in `SKILL.md` · schema `recovery_status` shape
