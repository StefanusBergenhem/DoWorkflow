# TestSpec — purpose and shape

A TestSpec is the artifact that says **what must be verified at this layer** so that test code can be written from it without consulting implementation. It is authored before the test code, derived from the layer's spec — never derived from the tests, never derived from the code.

## Where TestSpec sits in the V-model

| Layer | Parent spec (derivation source) | Verification level |
|---|---|---|
| **Root** | Requirements + Product Brief | system / acceptance |
| **Branch** (non-leaf) | Architecture | integration |
| **Leaf** | Detailed Design | unit |

Each TestSpec lives at the same scope as its parent spec. One scope, one parent spec, one TestSpec.

## What a TestSpec is for

The TestSpec answers four questions for the test-author downstream of it:
1. **What is being verified?** — `verifies:` lists upstream IDs.
2. **By what strategy?** — `type:` on every case names a strategy from a fixed enum.
3. **With what oracle?** — `expected:` is a specific value or bounded predicate.
4. **At what bar?** — front-matter `coverage_mutation_bar:` declares the structural and mutation thresholds.

A test-author reading the TestSpec writes test code; a reviewer reading the TestSpec checks coverage of strategies and gaps in derivation, not whether the implementation passes.

## Pre-code authoring discipline

When the parent spec is authored → the TestSpec can be drafted. **Do not wait for code.** This is not the same as TDD red phase (which is test code first); this is *test specification* first — and the test code is then derived from the TestSpec.

When code already exists (retrofit) → derive the TestSpec from the spec FIRST, THEN map existing tests to it. Existing tests do not become the TestSpec; they become inputs to a gap report. See `retrofit-discipline.md`.

## Derivation source per layer

| Layer | What you read in the parent | What seeds a case |
|---|---|---|
| **Leaf (DD)** | Public Interface contracts; Data Structure invariants; Algorithms; State machine; Error matrix | Each postcondition (on_success / on_failure) → one or more cases. Each invariant → property case. Each error-matrix row → robustness case. Each state transition → state-transition case. Each `[NEEDS-TEST: ...]` → surfaced case. |
| **Branch (Architecture)** | Interface contracts (DbC); Composition invariants; Allocated requirements; Quality-attribute allocations to components or interfaces | Each interface → integration case(s). Each composition invariant → cross-child case. Each allocated requirement → traceable case. Each QA allocation → specialised case (perf/sec/a11y). |
| **Root (Requirements + PB)** | Each requirement; NFR five-element thresholds; Interface five-dimension contracts; PB outcome statements | Each requirement → ≥1 case. Each NFR → measurable specialised case at the named threshold. Each interface dimension → contract or integration case. Each PB outcome → user-journey case in PB vocabulary. |

The seam references — `dd-traceability-cues.md`, `architecture-traceability-cues.md`, `requirements-traceability-cues.md` — give the per-layer slot-fill cues.

## Document shape

The TestSpec is a single Markdown file. The shape is fixed:

```
---
<front-matter: id, artifact_type, scope, level, derived_from, verifies, governing_adrs,
                coverage_mutation_bar, recovery_status (retrofit only), status, date>
---

# TestSpec — <scope name>

## Overview
<one or two paragraphs: what this TestSpec covers, what slice of the parent spec, the
 derivation posture (greenfield / retrofit)>

## Cases
<one embedded YAML block per case>
```

Layer determines the case template (`case-leaf.yaml.tmpl`, `case-branch.yaml.tmpl`, `case-root.yaml.tmpl`). The artifact-level `verifies:` is a union over the case-level `verifies:` lists, but is non-empty independently — empty artifact-level `verifies` is hard-reject (refusal B).

## Anti-shape

| Tell | What it indicates |
|---|---|
| TestSpec written after the test code | Code-to-test derivation; cases reflect implementation choices, not spec coverage |
| TestSpec without artifact-level `verifies` | Orphan TestSpec; nothing in the upstream world demands these cases (refusal B) |
| TestSpec where every case is "happy path" | Derivation-strategy gap; error / boundary / fault paths missing |
| TestSpec with cases that name methods, not scenarios | Implementation-style titles; "test_validate_returns_true" instead of "valid token within 30-min lifetime is accepted" |
| TestSpec without `coverage_mutation_bar` | Load-bearing QB section absent; refusal (derived-hard) |

## Cross-link

`derivation-strategies.md` (the case-type enum) · `per-layer-weight.md` (case shape per layer) · `verifies-traceability.md` (the `verifies` rule) · `quality-bar-checklist.md` (the final gate)
