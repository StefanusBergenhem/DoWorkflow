# Coverage and mutation bar

A TestSpec carries an artifact-level `coverage_mutation_bar:` block in front-matter. Its presence is mandatory (refusal — derived-hard). Its values are project-policy: the author does not prescribe specific thresholds or specific tools; placeholder values are acceptable on a first cut, with the project's policy filling them later.

## Why both, not just structural coverage

Structural coverage (line / branch) tells you the test suite executed the code. It does not tell you whether the assertions caught the behaviour. A pathological suite can hit 100% line coverage with no assertions and still let bugs through.

Mutation testing closes that gap: small synthetic faults are introduced into the implementation, the suite re-runs, and the *killed* fraction is reported. A high mutation score means assertions are sensitive to code change.

When only structural coverage is declared with no mutation bar → finding `anti-pattern.coverage-as-quality-metric`. The bar is incomplete; structural without mutation is a measurement of presence, not effectiveness.

## The required block (front-matter slot)

```yaml
coverage_mutation_bar:
  structural_coverage:
    threshold_pct: <number | "TBD-by-project-policy">
    metric: <line | branch | statement>
  mutation_score:
    threshold_pct: <number | "TBD-by-project-policy">
    tool_category: <PIT-class | Stryker-class | mutmut-class | "TBD-by-project-policy">
  enforcement:
    frequency: <per-PR | nightly | weekly | "TBD-by-project-policy">
    blocking: <true | false | "TBD-by-project-policy">
```

The author populates the block. When a value is not yet set by project policy → use `"TBD-by-project-policy"` as the literal placeholder (a reviewer can tell the author did not invent a number).

## What the author does NOT prescribe

The author does not pick threshold values. Concrete numbers (e.g., 80% line / 60% mutation) are project-policy decisions: they depend on layer, criticality, code under test, and the team's history. The skill emits the *structure* of the bar; the values come from the project.

When the user asks "what threshold should I pick?" → respond: *"Threshold values are project-policy. The TestSpec carries the structural slot; populate the values from your project's testing policy. If no policy exists, surface the gap to the human; the framework will not invent a number."*

## The "100% line / 4% mutation" gap

Industry observation: 100% line coverage paired with mutation scores below ~5% is empirically reachable and ships no real verification. The gap between the two is the load-bearing measurement — it tells the team how much of the executed code is actually under assertion. Naming the gap is part of declaring the bar; ignoring mutation hides the gap.

When the project has no mutation tooling → the bar still names a `tool_category` placeholder ("TBD-by-project-policy") so the gap is visible and tooling adoption surfaces in the next planning cycle.

## Per-layer guidance (no values, just shape)

| Layer | What is typically named | Notes |
|---|---|---|
| **Leaf** | Line OR branch coverage; mutation score; per-PR enforcement | Mutation is most informative at leaf because mutators target arithmetic / boundary / conditional code |
| **Branch** | Branch coverage; contract-test pass rate; nightly enforcement | Mutation may be less applicable at branch; a contract-test pass rate is the analogous bar |
| **Root** | User-journey pass rate; key-business-flow latency P95; weekly enforcement | Coverage as % is rarely meaningful at system level; pass-rate against a fixed journey suite is the analogous bar |

## Cross-link

`derivation-strategies.md` (the strategy mix that drives mutation kill rate) · `case-quality.md` (oracle specificity is what makes assertions kill mutants) · `anti-patterns.md` (`anti-pattern.coverage-as-quality-metric`, `anti-pattern.ice-cream-cone-coverage`) · `templates/coverage-mutation-bar.yaml.tmpl`
