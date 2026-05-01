# Per-layer case weight

Three case shapes — leaf, branch, root. The shape follows the layer; the layer follows the scope. A leaf case carrying branch-style preconditions or a root case carrying leaf-style internal API terms is shape-mismatched and signals the wrong layer or wrong derivation.

## Table of contents

- [The three shapes at a glance](#the-three-shapes-at-a-glance)
- [Leaf case shape](#leaf-case-shape)
- [Branch case shape](#branch-case-shape)
- [Root case shape](#root-case-shape)
- [`level:` and scope alignment](#level-and-scope-alignment)
- [Mismatch tells](#mismatch-tells)

## The three shapes at a glance

| Shape | Layer | Mandatory case fields | Vocabulary in `expected:` |
|---|---|---|---|
| **Leaf** | unit | id, title, type, verifies, inputs, expected | Function-return values, exception classes, data-structure invariants |
| **Branch** | integration | id, title, type, verifies, preconditions, steps, expected | Cross-child observable state (interface return values, side-effects on sibling components) |
| **Root** | system | id, title, type, verifies, preconditions, steps, expected | Product Brief vocabulary — user outcomes, business state, observable artifacts |

Templates: `case-leaf.yaml.tmpl`, `case-branch.yaml.tmpl`, `case-root.yaml.tmpl`.

## Leaf case shape

Thin. The leaf is one function or one small set of cohesive functions; cases call directly into the public interface, with at most two test doubles for collaborators outside the leaf's boundary.

```yaml
- id: TC-<scope>-001
  title: "<scenario, not method name>"
  type: <functional | boundary | error | property | state-transition | error-guessing>
  verifies:
    - "DD-<scope>.<field>.<sub-field>"   # field-level qualification permitted
  inputs:
    <param_a>: <value>
    <param_b>: <value>
  expected: <specific value | enumerated set | bounded predicate>
```

When `steps:` are needed at leaf → it usually means too many collaborators are involved; consider whether the case belongs at the branch instead. Leaf `steps:` are reserved for state-transition cases that drive a sequence (set state → fire event → observe target).

## Branch case shape

Fixtures-rich. The branch is a composition of children; cases drive the composition through a published interface and observe cross-child state.

```yaml
- id: TC-<scope>-001
  title: "<scenario>"
  type: <functional | error | fault-injection | contract | state-transition | property>
  verifies:
    - "ARCH-<scope>.interfaces.<name>"
    - "ARCH-<scope>.composition.<invariant>"
  preconditions:
    - "Fixture: <named fixture>"
    - "Test double: <child component> as <stub | spy | mock | fake>"
    - "Seed: <data fixture>"
    - "Environment: <in-process | test-containers | shared-staging>"
  steps:
    - "<call into the published interface with X>"
    - "<observe Y>"
  expected:
    - "<observable on child component A: state, return value, emitted event>"
    - "<cross-child invariant holds>"
```

When fixtures are not named → `mystery guest` anti-pattern; the case is implicitly relying on hidden test infrastructure. Name fixtures explicitly so the test-author downstream knows what to provide.

## Root case shape

Journey narrative. The root is the whole product; cases narrate user journeys end-to-end and assert observable outcomes in PB vocabulary.

```yaml
- id: TC-<scope>-001
  title: "<user journey, e.g., 'tenant signs up, creates first project, invites teammate'>"
  type: <functional | error | performance | security | accessibility>
  verifies:
    - "REQ-<id>"          # at least one root requirement
    - "PB-outcome-<id>"   # at least one PB outcome (where applicable)
  preconditions:
    - "Environment: <production-like shape>"
    - "Tenant: <named test tenant>"
    - "Feature flag: <flag set>"
    - "Persona: <named persona, e.g., 'first-time admin'>"
  steps:
    - "<step in user vocabulary, e.g., 'admin opens signup, completes form with valid email'>"
    - "<step>"
    - "<step>"
  expected:
    - "<observable in PB vocabulary, e.g., 'tenant exists in directory, owner role assigned, welcome email sent within 60s'>"
```

When `expected:` uses internal API terms ("`POST /api/v1/tenants` returns 201") at root → wrong abstraction; the user does not see HTTP. Use PB vocabulary; if internal verification is desired, the case belongs at the branch.

## `level:` and scope alignment

Front-matter `level:` follows scope:

| Scope position | `level:` |
|---|---|
| Root | `system` |
| Non-leaf (branch) | `integration` |
| Leaf | `unit` |

Mismatched `level:` (e.g., `level: unit` on a root TestSpec) is a soft-reject finding and usually signals the wrong layer was assumed when the document was scaffolded.

## Mismatch tells

| Tell | What it signals |
|---|---|
| Leaf case has 5 named test doubles | Over-mocking, or wrong layer (the case belongs at branch where the children compose) |
| Branch case has `inputs:` only and no `preconditions:` / `steps:` | Branch underweight — fixtures and cross-child interactions not surfaced |
| Root case `expected:` mentions internal class names | Root case using internal vocabulary — re-cast in PB vocabulary or move to branch |
| Branch case `verifies:` points only at DD field IDs | Wrong granularity — branch cases verify Architecture interfaces / composition invariants, not DD fields |
| Root case `verifies:` points only at DD field IDs | Wrong granularity — root cases verify Requirements and PB outcomes |

## Cross-link

`testspec-purpose-and-shape.md` (layer / parent spec / level mapping) · `derivation-strategies.md` (which strategies fit which layer) · `verifies-traceability.md` (granularity per layer) · `test-double-discipline.md` (the leaf double cap) · `integration-and-system-specifics.md` (branch and root specialised cases)
