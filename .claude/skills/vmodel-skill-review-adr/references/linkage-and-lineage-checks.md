# Linkage and lineage — checks

Linkage QB group + structural integrity (review steps 2 + 7). Includes the orphan-ADR aggregator and the back-resolution flag.

## scope_tags empty — orphan ADR (broken-reference)

When `scope_tags` is empty (or missing); also schema-enforced via `minItems: 1`. Aggregator alias for `anti-pattern.orphan-adr`.

- **check_failed**: `check.linkage.scope-tags-empty` (alias `anti-pattern.orphan-adr`)
- **severity**: `hard_reject` ★ (broken-reference integrity)
- **evidence pattern**: quote front-matter showing absent or empty `scope_tags`.
- **recommended_action**: *"Set `scope_tags` to the scope(s) the decision applies to. If no scope applies, the decision is not load-bearing — drop the ADR. Empty `scope_tags` is the orphan-ADR anti-pattern."*

## Supersession chain broken

When `supersedes:` is set on this ADR but the referenced predecessor's `superseded_by:` does not point back to this one — or vice versa.

- **check_failed**: `check.linkage.supersession-chain-broken`
- **severity**: `soft_reject`
- **conditional gating**: `supersedes:` or `superseded_by:` set
- **evidence pattern**: quote both ends of the chain; show the asymmetry.
- **recommended_action**: *"Repair the supersession chain in both directions: predecessor's `superseded_by` matches successor's id; successor's `supersedes` matches predecessor's id."*

## Both supersedes and superseded_by set

When the same ADR claims both to supersede an older one and to be superseded by a newer one. This is structurally inconsistent on a single ADR — supersession is a one-step relationship.

- **check_failed**: `check.linkage.both-supersedes-and-superseded-set`
- **severity**: `soft_reject`
- **conditional gating**: both fields set
- **recommended_action**: *"Pick one role: either this ADR supersedes a predecessor (clear `superseded_by`), or it has been superseded itself (clear `supersedes`). Multi-step lineage is expressed across multiple ADRs, not collapsed onto one."*

## affected_scopes omitted (info)

When the Decision text or Consequences mention scopes beyond `scope_tags` but `affected_scopes:` is empty or absent.

- **check_failed**: `check.linkage.affected-scopes-omitted`
- **severity**: `info`
- **evidence pattern**: quote the Decision/Consequences passage referencing other scopes.
- **recommended_action**: *"Set `affected_scopes:` to the scopes the decision reaches beyond its primary `scope_tags`."*

## Governing-ADRs back-resolution flag (flag-not-scan)

When the ADR's `status` is `accepted` but no citing artifact (Architecture / DD / TestSpec) appears to reference it via `governing_adrs:` AND `scope_tags` does not point at a scope where a propagation requirement was materialised. Surfaces orphan-suspicion without a heavy cross-tree walk.

- **check_failed**: `check.linkage.governing-adrs-back-resolution-flag`
- **severity**: `soft_reject` (flag-not-scan)
- **conditional gating**: status `accepted`
- **evidence pattern**: note that the ADR is accepted but no inbound `governing_adrs:` reference is visible in the inputs provided AND the Propagation block lists no requirement at scope.
- **recommended_action**: *"Either wire the propagation (new requirement at this ADR's scope, or `governing_adrs:` from a child artifact) and verify with the matched author skill, or move the ADR to status `proposed` until propagation is complete."*

## Sweep order in this step

1. Check `scope_tags` non-empty (broken-reference hard).
2. If `supersedes:` set: resolve the predecessor's front-matter; check the chain.
3. If `superseded_by:` set: resolve the successor's front-matter; check the chain.
4. If both set: structural-inconsistency soft.
5. Read Decision and Consequences for scope-spanning references; compare with `affected_scopes`.
6. If status `accepted` and no inbound governing reference visible: back-resolution flag soft.

## Defer to other files

- Hybrid propagation rule (per-consequence routing) → `propagation-and-completeness-checks.md`
- Retrofit `recovery_status` checks → `retrofit-discipline-checks.md`
