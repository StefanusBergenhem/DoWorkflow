# Requirements + PB ↔ root TestSpec traceability cues

When authoring a root TestSpec, the parent specs are root Requirements + the Product Brief. Walk both end-to-end. Root cases verify requirements at the system level using PB vocabulary in `expected:`.

## Table of contents

- [The closed-correspondence mappings](#the-closed-correspondence-mappings)
- [Slot-fill: functional requirement → user-journey case](#slot-fill--functional-requirement--user-journey-case)
- [Slot-fill: NFR (five-element) → specialised case](#slot-fill--nfr-five-element--specialised-case)
- [Slot-fill: interface requirement → contract case per dimension](#slot-fill--interface-requirement-five-dimension--contract-case-per-dimension)
- [Slot-fill: PB outcome → user-journey case](#slot-fill--pb-outcome--user-journey-case)
- [What does NOT belong at root](#what-does-not-belong-at-root)

## The closed-correspondence mappings

| Upstream element | Root TestSpec case | Strategy `type:` |
|---|---|---|
| Functional requirement | At least one user-journey case verifying the requirement | `functional` |
| Quality-attribute requirement (NFR) — five-element form (subject / metric / threshold / condition / verification) | Specialised case at the named threshold under the named condition | `performance` / `security` / `accessibility` (matching the QA) |
| Interface requirement — five-dimension form (data / protocol / authn / errors / availability) | Contract or integration case per dimension | `contract` / `error` |
| Inherited-constraint requirement (regulatory, platform, third-party) | Case asserting compliance under the named scenario | `functional` (or specialised, depending) |
| PB outcome statement (e.g., "tenants can self-onboard within 5 minutes") | User-journey case in PB vocabulary | `functional` |
| PB persona | At least one journey per primary persona, named in `preconditions:` | `functional` |
| PB business state outcome (e.g., "subscription activates on payment") | Functional case asserting business-state observable | `functional` |

When a requirement has no root case (and no branch case verifies it directly under allocation) → finding `check.requirements-traceability.requirement-unverified`. Every requirement must be verifiable somewhere in the scope tree.

## Slot-fill — functional requirement → user-journey case

```yaml
# From Requirements:
# REQ-007: When a tenant admin invites a teammate by email, the teammate receives
#          an invitation email and can accept it within 7 days to gain Editor role.

- id: TC-<scope>-N
  title: "tenant admin invites teammate; teammate accepts within 7 days; gains Editor role"
  type: functional
  verifies:
    - "REQ-007"
    - "PB-outcome-collaboration-onboarding"
  preconditions:
    - "Environment: production-like (named tier, mail server stub)"
    - "Tenant: 'acme-corp' with admin user 'admin@acme'"
    - "Persona: 'first-time admin' (PB persona)"
    - "Feature flag: invitations.enabled = true"
  steps:
    - "Admin opens Team page and submits Invite form with teammate email 'new@acme'"
    - "System sends invitation email"
    - "Teammate opens invitation link within 7 days"
    - "Teammate completes signup form"
  expected:
    - "Invitation email received at 'new@acme' within 60s"
    - "Email body contains a link with a token expiring at +7 days from send"
    - "After teammate accepts: tenant 'acme-corp' has the teammate as a member with Editor role"
    - "Teammate sees the tenant in their dashboard"
```

## Slot-fill — NFR (five-element) → specialised case

```yaml
# From Requirements:
# REQ-NFR-002: subject=API; metric=p95 latency; threshold=<= 250 ms;
#              condition=under 100 RPS sustained; verification=load test in production-like env

- id: TC-<scope>-N
  title: "API p95 latency <= 250ms at 100 RPS"
  type: performance
  verifies:
    - "REQ-NFR-002"
  preconditions:
    - "Load: 100 RPS sustained, 5-min ramp, 10-min steady"
    - "Environment: production-like (named tier, prod-shaped data set)"
  expected:
    metric: p95
    threshold: "<= 250 ms"
    sample_size: 60000
```

When the NFR misses elements (e.g., no condition, no threshold) → finding `check.requirements-traceability.nfr-no-threshold-case` and a HALT request to fix the upstream Requirements (the gap is in the spec, not the test).

## Slot-fill — interface requirement (five-dimension) → contract case per dimension

```yaml
# From Requirements:
# REQ-IF-001: External webhook endpoint:
#   data: JSON; protocol: HTTPS POST; authn: HMAC-SHA256 with shared secret;
#   errors: 4xx for client, 5xx for server; availability: 99.9% over rolling 30 days

# Five cases — one per dimension:

- id: TC-<scope>-N
  title: "webhook accepts JSON payload with required fields"
  type: contract
  verifies: [ "REQ-IF-001.data" ]

- id: TC-<scope>-N+1
  title: "webhook over HTTPS rejects HTTP traffic"
  type: contract
  verifies: [ "REQ-IF-001.protocol" ]

- id: TC-<scope>-N+2
  title: "webhook with invalid HMAC returns 401; with valid HMAC returns 200"
  type: security
  verifies: [ "REQ-IF-001.authn" ]
```

The interface five-dimension form maps one-to-one to cases per dimension. Skipping a dimension is a coverage gap.

## Slot-fill — PB outcome → user-journey case

```yaml
# From Product Brief:
# Outcome: "Tenants self-onboard from signup to first meaningful project in <= 5 minutes"

- id: TC-<scope>-N
  title: "first-time admin signs up to first project in <= 5 minutes"
  type: functional
  verifies:
    - "PB-outcome-self-onboarding"
    - "REQ-006"   # the requirement that allocates the outcome
  preconditions:
    - "Persona: first-time admin (PB persona)"
    - "Environment: production-like; cold cache"
  steps:
    - "Open signup with valid email"
    - "Complete signup form"
    - "Confirm email"
    - "Create first project"
    - "Save first project"
  expected:
    - "Total elapsed time from signup-open to project-saved <= 5 minutes (sample of 10 runs, median <= 5 min, p95 <= 7 min)"
```

## What does NOT belong at root

| Pattern | Where it actually belongs |
|---|---|
| Single function return value | Leaf TestSpec |
| Single interface postcondition w/o user vocabulary | Branch TestSpec |
| Internal API responses as `expected:` | Lower layers — root uses PB vocabulary |

Mistargeting these at root produces ice-cream-cone coverage (many e2e, few lower-level cases) and is flagged.

## Cross-link

`per-layer-weight.md` (root case shape — journey narrative, PB vocabulary) · `derivation-strategies.md` (specialised strategies for QA requirements) · `integration-and-system-specifics.md` (production-like environment shape, version pinning) · `verifies-traceability.md` (granularity for root cases)
