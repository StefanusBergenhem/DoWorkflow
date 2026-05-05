# Decomposition checks

Mirrors the author-side `decomposition-discipline.md`. Walk every Decomposition entry against the eight checks below. The first check (`check.responsibility.implementation-prescription`) is HARD; the rest are soft-reject.

## check.decomposition.purpose-not-one-sentence (soft)

**Check that** every child's `purpose` is exactly one sentence with no conjunctions (`and`, `or`, `as well as`) and no comma-separated lists of activities.

**Reject when** the purpose contains `and`, a comma list of activities, or runs longer than one sentence.

**Approve when** the sentence reads as a single architectural responsibility ("Session-lived container for line items and checkout progress" — note: "line items and checkout progress" is a single noun phrase, not two responsibilities).

**Evidence pattern:** quote the `purpose:` field verbatim. The conjunction is the tell.

**recommended_action:** *"Reduce to one architectural responsibility per child. If two are needed, the boundary is wrong — split or accept that one box hides two."*

## check.decomposition.responsibility-too-many (soft)

**Check that** the `responsibilities:` list has at most three items, each at architectural level (what the component owns or coordinates), not implementation level (specific algorithm, library, data structure).

**Reject when** the list has four or more items, or any item names internal mechanics ("uses a hash map for line items"). Implementation-level responsibilities are also caught by `check.responsibility.implementation-prescription` (HARD).

**Approve when** every item is a coordination, ownership, or boundary-enforcement statement.

**recommended_action:** *"Cap at three architectural responsibilities. Reframe implementation-level items as ownership or coordination."*

## check.decomposition.responsibility-conjunction-and-test (soft)

**Check that** no responsibility list smuggles a hidden conjunction — items like "Manage cart state and synchronise with pricing" pack two responsibilities behind one bullet.

**Reject when** any item joins two distinct architectural concerns with `and`, `plus`, `as well as`, or a comma.

**Approve when** each bullet is a single concern.

**recommended_action:** *"Apply the one-sentence-responsibility test per bullet. Split joined concerns into separate items, or split the child if two responsibilities cannot be unified."*

## check.decomposition.requirement-orphan (soft)

**Check that** every requirement allocated to this scope (visible in parent Requirements' allocation set, or in the document's own front-matter `derived_from:`) appears in at least one child's `allocates:` list.

**Reject when** a parent-allocated requirement is absent from every child's `allocates:`.

**Approve when** every parent requirement lands in at least one child.

**Evidence pattern:** name the orphaned requirement id. State which children were checked.

**recommended_action:** *"Either allocate the requirement to a child whose responsibilities cover it, or surface it as cross-cutting at composition level (and explain in rationale why no child owns it)."*

## check.decomposition.component-orphan (soft)

**Check that** every child has a non-empty `allocates:` list.

**Reject when** a child's `allocates:` is empty or missing.

**Approve when** every child carries at least one parent requirement.

**recommended_action:** *"A child with no allocations is a structural artifact without a purpose. Either assign at least one requirement, or remove the child."*

## check.decomposition.depth-test-not-applied (soft)

**Check that** if the decomposition was revised during authoring, the document records which of the depth-test trio fired (depth / cognitive-load / change-blast).

**Reject when** the document is non-trivial (≥4 children, mixed bounded contexts, or root scope) and the rationale for the chosen depth is wholly absent — no acknowledgement of why this depth and not deeper or shallower.

**Approve when** the rationale, the Overview, or a Notes section records which test fired (e.g., "change-blast test fired during decomposition; an earlier draft folded order-committer into cart, cascade through cart on adding loyalty consumers — split absorbed the change-driver cleanly").

**recommended_action:** *"Add a one-line note recording which depth-test pulled the decomposition to this granularity."*

## check.decomposition.bounded-context-fracture-ignored (soft)

**Check that** when the same domain term means different things across children (linguistic fracture), a child boundary is drawn at the fracture.

**Reject when** the same vocabulary (e.g., `order`, `customer`, `shipment`) carries two different meanings across children but no boundary is drawn between the two senses.

**Approve when** linguistic fractures map to child boundaries, OR the document explicitly notes "shared meaning preserved" in rationale.

**Evidence pattern:** name the term and the two senses observed.

**recommended_action:** *"Either redraw the child boundary at the linguistic fracture, or document why a single bounded context is preserved (with cost-of-context-mapping rationale)."*

## check.responsibility.implementation-prescription (HARD — refusal B sub-tell)

**Check that** no responsibility prescribes implementation — internal algorithm, named data structure, named library, or specific framework call.

**Reject when** any responsibility names: `LinkedHashMap`, `TreeMap`, `Redis cache with N TTL`, `JPA repository`, `kafka producer with X compression`, a specific algorithm by name, or any other implementation choice that should live in Detailed Design.

**Approve when** every responsibility names what is owned or coordinated, not how it is implemented.

**Evidence pattern:** quote the responsibility verbatim and underline the implementation tell.

**recommended_action:** *"Move implementation choices to Detailed Design (or, if cross-cutting, an ADR). Replace here with the architectural responsibility — what is owned or coordinated, not how it is built."*

### Banned fields (Rule 8)

Hard-fail Decomposition entries that carry any of: `bounded_context_line`, `owning_team_type`, `test_seam` (including its sub-fields `driving_ports`, `driven_ports`, `fake_strategy`). Severity: `soft_reject`. Identifier: `check.discipline.architecture-bundle-shape`. Reason: team-topology-derived fields, not load-bearing for AI-first frameworks; test-design content lives in TestSpec at the corresponding scope.

## Sweep order

Walk top to bottom. The HARD check is last in the list because it is also caught by the anti-pattern sweep (`anti-pattern.dd-content-in-architecture`); flagging it here as well preserves per-element granularity that the matched author skill needs.

Cross-link: `anti-patterns-catalog.md` (god-component, premature-decomposition, cyclic-dependencies); `quality-bar-gate.md` (Decomposition card).
