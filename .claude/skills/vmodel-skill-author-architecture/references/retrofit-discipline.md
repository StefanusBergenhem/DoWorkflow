# Retrofit discipline

A retrofit Architecture is a **mixed-evidence** document. Decomposition (where boundaries actually run) and interfaces (what each component exposes / consumes) are largely recoverable from code: dependency analysis, package structure, deployment manifests, and runtime call graphs all leave fingerprints. **Rationale** — why boundaries were drawn, which alternatives were rejected, which constraints were binding — is not. The framework rule is explicit: **rationale is human-only**.

## Contents

- `recovery_status` discipline
- The observed-vs-interpreted split
- Honest vs laundered — side-by-side
- Citation form for observed evidence
- Gap report
- Pair every `unknown` with a follow-up

## `recovery_status` discipline

A YAML field on every retrofit-derived block, with three legal values:

| Value | Meaning | Allowed on |
|---|---|---|
| `reconstructed` | Observed structure, evidence cited (file/line/commit/schema/operational-log) | Decomposition entries' structural fields, interface signatures, observed wiring |
| `verified` | Human-confirmed interpretation; the reviewer signed off as accurate | Anything `reconstructed` can be plus rationale fields |
| `unknown` | Human-only content with no preserved human source available | Rationale, rejected-alternatives, original-intent |

**Rationale is never `reconstructed`** — this is hard refusal A. An agent emitting `recovery_status: reconstructed` against a rationale field is fabricating; the value cannot be derived from observable code. Allowed on rationale: `verified` (human source cited) or `unknown` (no source) only.

## The observed-vs-interpreted split

| Observable from code | Not observable — human-only |
|---|---|
| Where component boundaries run (packages, deployment units) | *Why* the boundary was drawn there |
| What each component exposes / consumes (call graphs, schemas) | What alternatives were considered and rejected |
| The data topology (DB schemas, table ownership) | The original intent behind a quirk that has since drifted |
| The wiring (DI containers, manifest references, env wiring) | Whether a quirk is a feature or a forgotten workaround |
| Observed runtime behaviour (logs, traces, performance probes) | Whether observed behaviour is *intended* behaviour |

Drawing this split clearly — and refusing to fabricate the right column — is the entirety of retrofit honesty.

## Honest vs laundered — side-by-side

```yaml
# LAUNDERED (the kind of output an unguarded AI produces — refuse to ship this)

decomposition:
  - id: cart
    purpose: "Holds the user's shopping cart, optimised for extensibility and single responsibility."
    responsibilities:
      - "Manage cart state with clean separation of concerns."
      - "Coordinate with pricing and inventory following DDD principles."
    allocates: [REQ-001, REQ-002]
    recovery_status: reconstructed                                  # ← rationale is reconstructed (illegal)
    rationale: "Chosen because it adheres to single-responsibility and provides clean bounded-context separation, following DDD best practices."
    # generic principle invocation; no historical record; no file/commit citation
```

```yaml
# HONEST (correct retrofit output)

decomposition:
  - id: cart
    purpose: "Holds line items and orchestrates checkout workflow across pricing, inventory, payment, order."
    responsibilities:                                               # observed via code + call graphs
      - "Line-item state (observed: CartController + CartRepository, src/main/java/.../cart/)"
      - "Calls PricingClient on every update (observed: CartService.updateLine, src/main/java/.../CartService.java:142)"
      - "Calls InventoryClient on reservation (observed: CheckoutService.reserve, ditto:201)"
      - "Writes via shared CheckoutDb (observed: flyway migrations V001..V034 in db/migration/)"
    allocates: [REQ-001, REQ-002]
    recovery_status: reconstructed                                  # for the observed structure above
    rationale:
      status: unknown                                               # human-only field, no fabrication
      note: |
        No design document, ADR, or decision-record preserved for why the cart
        boundary was drawn here. Git history shows the boundary stable since
        commit abc123 (2021-06); no commit message cites a motivating decision.
        Follow up with @original-lead before marking verified.
```

The laundered version reads smoother. It is generic to the point of interchangeability; its rationale cites principles rather than history. The honest version carries the same observed structure tied to specific files and lines, and the rationale field says *we do not know* rather than inventing a plausible answer.

## Citation form for observed evidence

Every `reconstructed` field cites at least one of:

- File path + line number range (`src/main/java/.../CartService.java:142-198`)
- Commit hash + brief description (`abc123 — initial cart boundary, 2021-06`)
- Schema artifact (`db/migration/V019__add_cart_outbox.sql`)
- Operational log reference (`prod-logs 2024-09-12 incident #INC-4521 — observed timeout pattern`)

`derived_from` in retrofit points to observed evidence:

```yaml
derived_from:
  - "observed_behaviour: src/main/java/.../CartService.java:142-198"
  - "observed_config: config/prod.yaml#cart.idle_timeout_seconds = 1800"
  - "observed_schema: db/migration/V019__add_cart_outbox.sql"
```

Vague `derived_from: [user-experience, security]` (categories, not artifacts) is a retrofit smell — fix at source, do not mirror upward.

## Gap report

Every retrofit Architecture carries a Gap report (a section in the artifact) populating four buckets:

| Bucket | What lives here |
|---|---|
| **Lost rationale** | Decomposition / interface / composition entries where the *why* is `unknown`; pair each with a follow-up owner and action |
| **Structural drift** | Where the cleaned-up Architecture diagram differs from the actual runtime (cyclic imports the diagram does not show, shared mutable state across boxes the diagram pretends are independent) |
| **Missing ADRs** | Load-bearing cross-cutting hard-to-reverse decisions in production with no preserved decision record — emit `[NEEDS-ADR: ...]` stubs |
| **Coverage gaps** | Parent requirements with no observable allocation in the running system (the requirement may have been silently dropped; the requirement may exist as a manual process; the requirement may have been forgotten) |

A retrofit artifact without a populated Gap report is laundered by omission.

## Pair every `unknown` with a follow-up

```yaml
follow_up:
  - owner: "@original-lead"
    action: "Confirm whether the cart-boundary placement was a deliberate ACL choice; if yes, supply rationale; if no, propose redecomposition."
    deadline: "<<date or sprint marker>>"
```

`unknown` without follow-up is a fact resigned-to, not a finding. Follow-ups make the gap actionable.

Cross-link: `anti-patterns.md` items #7 (Laundering the current architecture) and #8 (Fabricated decomposition rationale); SKILL.md hard refusal A and Step 11.
