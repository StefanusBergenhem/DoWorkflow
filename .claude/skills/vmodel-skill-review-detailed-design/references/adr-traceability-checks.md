# ADR traceability checks

Mirrors `adr-extraction-cues.md` on the author side.

## check.adr.governing-not-resolved (HARD ★ broken-reference)

**Check that** every entry in `governing_adrs:` resolves to an actual ADR document.

**Reject when** a listed ADR id cannot be located.

**Approve when** all entries resolve.

**Evidence pattern:** quote the unresolvable entry; note the absence at the expected path.

**recommended_action:** *"Resolve the unresolvable id, OR remove it from `governing_adrs:` if it was a stale reference. A broken reference is a document-integrity failure."*

## check.adr.governing-not-cited-in-body (soft)

**Check that** every ADR listed in `governing_adrs:` appears at least once at a body-citation point.

**Reject when** an ADR is listed in front-matter but never cited at the spot in the body where its decision applies.

**Approve when** every listed ADR has at least one body citation.

**Evidence pattern:** name the ADR id; note the absence of body citation.

**recommended_action:** *"Cite the ADR at the body decision-application point. A `governing_adrs:` list with no body citations is decoration."*

## check.adr.body-citation-unresolved (soft)

**Check that** every body citation of the form `(per ADR-NNN)` corresponds to an entry in `governing_adrs:`.

**Reject when** the body cites an ADR id that does not appear in front-matter.

**Approve when** every body citation has a front-matter entry.

**recommended_action:** *"Add the cited ADR to `governing_adrs:` OR remove the body citation if the ADR is not actually governing this DD."*

## check.adr.inline-decision-should-be-extracted (soft)

**Check that** rationale obviously meeting the three ADR criteria (load-bearing AND cross-cutting AND hard-to-reverse) is extracted to an ADR rather than inlined.

**Reject when** inline rationale describes a decision that affects multiple scopes, is hard to reverse, and is load-bearing — but no ADR is referenced.

**Approve when** such decisions are extracted (or carry a `[DEFER-ADR: ...]` marker).

**Evidence pattern:** quote the inline rationale; identify the three criteria it satisfies.

**recommended_action:** *"Extract the decision to an ADR. Inline a `[DEFER-ADR: <decision>]` marker if no ADR exists yet — the marker stays in place until the ADR is authored."*

## Cross-link

`anti-patterns-catalog.md` · `quality-bar-gate.md` (Rationale and traceability card) · `templates/governing-adr-reference.yaml.tmpl` (author side)
