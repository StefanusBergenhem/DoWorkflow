# Counter-example — fabricated retrofit DD (refuse to ship)

A team retrofits an unfamiliar legacy leaf: a session-token validator. The original author has left, the commit history says only "initial import", and the only preserved evidence is the code itself (HMAC-SHA256, 30-minute lifetime, no design docs, no ADRs, no rationale).

A retrofit run with insufficient guardrails produces the artifact below. It exhibits the three retrofit-laundering tells in concentrated form: schema-illegal `recovery_status` on Overview, fabricated rationale on a human-only field, committee-grade prose without a preserved source. Every inline `<!-- VIOLATION -->` annotation marks what the matched review skill's hard-rejects would catch.

## The fabricated artifact (refuse to ship this)

```markdown
---
id: DD-legacy-session-validator
artifact_type: detailed-design
scope: legacy/auth/session-validator
parent_scope: legacy/auth
parent_architecture: ARCH-legacy-auth
derived_from:
  - REQ-legacy-auth-001
recovery_status:
  overview: reconstructed       # ← VIOLATION (refusal A): schema rejects this enum value
  rationale: reconstructed      # ← VIOLATION (refusal A): rationale is human-only
status: draft
date: 2026-04-30
---

# Detailed Design — session-validator

## Overview
After evaluating token formats in the 2019 platform refresh, the team selected
HMAC-SHA256 with a 30-minute lifetime to balance security against session
ergonomics for the sales operations workflow.
<!-- VIOLATION: anti-pattern.fabricated-rationale -
     "the team selected", "to balance security against ergonomics", "for the
     sales operations workflow" — committee-grade prose despite no accessible
     deciders, no archive, no preserved conversation. The same rationale would
     defend a 60-minute lifetime or HS512 — confident precisely where a real
     rationale would be specific. -->

The validator has been carefully designed for security and performance,
following industry-standard cryptographic practices and clean architecture
principles.
<!-- VIOLATION: anti-pattern.fabricated-rationale -
     "carefully designed", "industry-standard", "clean architecture principles"
     — name-drops without specific application. -->

## Public Interface

\```yaml
public_interface:
  - name: validate
    signature: "validate(token: String) -> SessionClaims"
    description: "Validates a session token and returns the claims."
    postconditions:
      on_success:
        - "Returns SessionClaims for a valid token"
      # ← VIOLATION: postconditions.on_failure missing — finding
      # check.contract.postcondition-failure-branch-missing
\```

## Rationale

The 30-minute lifetime matches the measured median session length for the
sales team, minimising re-authentication while capping the blast radius of
token theft. HMAC-SHA256 was chosen over JWT to avoid the algorithm-confusion
class of attacks and to keep the verification path simple enough to audit
inline.
<!-- VIOLATION: anti-pattern.fabricated-rationale -
     "the measured median session length" — what measurement? Cited where?
     "to avoid the algorithm-confusion class of attacks" — plausibly correct
     reasoning, but back-derived from what the code does. The retrofit had no
     access to the team that made the choice. -->
```

## Tells of fabrication

| Tell | Where it appears | What review hard-rejects |
|---|---|---|
| `recovery_status: { overview: reconstructed }` | Front-matter | Schema enum violation; finding `check.recovery-status.overview-reconstructed` (refusal A) |
| `recovery_status: { rationale: reconstructed }` | Front-matter | Rationale is human-only; finding `anti-pattern.fabricated-rationale` (refusal A) |
| Committee-grade prose despite no preserved decision record | Overview, Rationale | Generic phrasing that could defend any decision; finding `anti-pattern.fabricated-rationale` |
| Plausibly-correct rationale back-derived from observable code | Rationale | The retrofit has no access to the team that made the choice; back-derivation is fabrication; finding `anti-pattern.post-hoc-dd` |
| Missing `on_failure` branch | Public Interface | Half-specified contract; finding `check.contract.postcondition-failure-branch-missing` |
| No file/line citation for any structural claim | Throughout | Cannot verify against source; finding `check.retrofit.observed-not-marked-with-evidence` |

## What the honest retrofit looks like

```markdown
---
id: DD-legacy-session-validator
artifact_type: detailed-design
scope: legacy/auth/session-validator
parent_scope: legacy/auth
parent_architecture: ARCH-legacy-auth
derived_from:
  - "observed_behaviour: src/auth/token.py:14-95"
  - "observed_config: config/prod.yaml:auth.session_ttl_minutes=30"
recovery_status:
  overview: unknown               # ← legal: intent not recoverable
  public_interface: reconstructed
  state: reconstructed
  error_handling: reconstructed
status: draft
date: 2026-04-30
---

# Detailed Design — session-validator

## Overview

unknown — no preserved design notes for this leaf as of 2026-04-30. Observable
behaviour described below; the *why* of those choices is an open question.

The leaf validates session tokens issued elsewhere; tokens are observed (in
src/auth/token.py:14) to be HMAC-SHA256 over a canonical payload, with a
30-minute lifetime from the `iat` claim. The forces that drove either choice
(signature scheme, lifetime) are not recoverable from code, configuration, or
accessible team members.

## Public Interface

\```yaml
public_interface:
  - name: validate
    signature: "validate(token: String) -> SessionClaims"
    description: "Validates a session token and returns the claims."
    preconditions:
      - "observed from code: token is a non-empty UTF-8 string; no further input
         validation enforced (src/auth/token.py:42)"
    postconditions:
      on_success:
        - "Returns SessionClaims with subject, issued_at, expires_at
           (observed: src/auth/token.py:78)"
      on_failure:
        - "Raises TokenInvalid (signature mismatch, expired iat+30m, unknown
           issuer); no partial result; no state mutation
           (observed: src/auth/token.py:62-95)"
    errors:
      - error: "TokenInvalid"
        raised_when: "Signature verification failure OR iat+30m < now() OR
                      issuer not in known set"
        meaning: "Token is not acceptable; caller should treat session as
                  unauthenticated"
    thread_safety: "thread-safe (observed: no shared mutable state in the
                    validator class, src/auth/token.py:14)"
\```

## Rationale

status: unknown
note: |
  Observed lifetime (30 min) and signature scheme (HMAC-SHA256) in code
  (src/auth/token.py:14, src/auth/token.py:36) and deployment config
  (config/prod.yaml:auth.session_ttl_minutes=30). The forces driving these
  choices (security model, session ergonomics, regulatory constraint) are not
  recoverable from the codebase or accessible team members.
follow_up:
  owner: "@auth-team-lead"
  action: |
    Confirm under current threat model whether the 30-minute lifetime and
    HMAC-SHA256 choice are still appropriate; supply rationale for the record
    OR propose changes with migration plan.

## Retrofit Gap Report

| Bucket | Items |
|---|---|
| Lost rationale | Why 30-min lifetime; why HMAC-SHA256 over JWT; why no refresh-token path |
| Behavioural drift | None observed (code matches deployment config) |
| Missing ADRs | [DEFER-ADR: signature scheme for session tokens] |
| Coverage gaps | No observable test for issuer-not-in-known-set path (test file `test_token.py:88` skips with `# TODO: cover unknown issuers`) |
```

## Why the honest version is the right shape

- **`recovery_status.overview: unknown`** — schema-legal; honest. Intent is human-only; no human source available; no fabrication.
- **`Rationale: status: unknown`** with concrete `follow_up` — actionable; the gap is recorded as a queue entry for the right owner.
- **Every structural field cites file/line/config evidence** — `src/auth/token.py:42`, `config/prod.yaml:auth.session_ttl_minutes=30`. Reviewers can verify against source; the `reconstructed` markings are not bare claims.
- **Gap report is populated** — lost rationale, missing ADR (with a `[DEFER-ADR: ...]` marker), test coverage gaps. The retrofit surfaces what was lost rather than papering over it.

The honest version reads less smoothly. That is the signal that it is honest — laundering is what produces the smooth read.
