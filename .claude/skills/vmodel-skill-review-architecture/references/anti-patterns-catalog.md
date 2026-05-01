# Anti-patterns catalog — sweep targets

Ten failure modes, each with a tell, a `check_failed` identifier, a severity, and a generic recommended_action. Walk every Decomposition entry, Interface entry, Composition section, and the document as a whole through this catalog. Every hit becomes a finding.

Patterns 1–6 are universal; patterns 7–10 are AI-era / retrofit-specific. Hard-reject triggers are flagged ★.

## Contents

- Universal six (1. Big ball of mud / 2. Distributed monolith / 3. God component / 4. Premature decomposition / 5. Stale architecture / 6. Cyclic dependencies)
- AI-era and retrofit four (7. Laundered architecture HARD / 8. Fabricated decomposition rationale HARD / 9. Ad-hoc composition / 10. DD-content-in-architecture HARD)
- Sweep order
- Aggregation rule

## Universal six

### 1. Big ball of mud

- **Tell**: Structure Diagram is missing, has no arrows, or is a spider's web with no discernible flow direction; "where does X happen?" can only be answered by a guided tour.
- **check_failed**: `anti-pattern.big-ball-of-mud`
- **severity**: `soft_reject`
- **evidence pattern**: name the diagram (or its absence); cite specific arrows that contradict each other or paths that imply no architecture.
- **recommended_action**: *"Redo decomposition from scratch using information hiding, cohesion, bounded contexts. If a clean decomposition cannot be drawn, the system needs refactoring before an Architecture artifact is meaningful."*

### 2. Distributed monolith

- **Tell**: services share a database, a schema, a deploy cadence, or a synchronous request chain. Every release requires all services to ship together; one service going down takes the whole feature down.
- **check_failed**: `anti-pattern.distributed-monolith`
- **severity**: `soft_reject`
- **evidence pattern**: name the shared resource (DB, deploy pipeline, sync chain) and the services that share it.
- **recommended_action**: *"Redecompose. Either go to a modular monolith (one DB, one deploy) or do the work for genuine independence (per-service DB, async edges, independent deploy)."*

### 3. God component

- **Tell**: one Decomposition entry's responsibility list needs ≥2 conjunctions; peripherals are thin wrappers; the one-sentence test fails.
- **check_failed**: `anti-pattern.god-component`
- **severity**: `soft_reject`
- **evidence pattern**: quote the responsibility list with the conjunctions underlined.
- **recommended_action**: *"Split. Apply information hiding — list the next three changes the business will demand and check that each lands in one component."*

### 4. Premature decomposition

- **Tell**: ten services for a system three people can fit in a room for; services that always deploy together; "we need to call across three services for a login"; one team, one cadence, but N services.
- **check_failed**: `anti-pattern.premature-decomposition`
- **severity**: `soft_reject`
- **evidence pattern**: name the service count, team size, and deploy cadence implied by the artifact.
- **recommended_action**: *"Start with a modular monolith; split into separate runtime units only when independence (scaling / failure / ownership) earns its keep."*

### 5. Stale architecture

- **Tell**: front-matter or body describes a topology that contradicts observable code (cyclic imports the diagram does not show, components that have been merged or split, interfaces that have been retired).
- **check_failed**: `anti-pattern.stale-architecture`
- **severity**: `soft_reject`
- **evidence pattern**: name the contradiction — diagram says A; code says B.
- **recommended_action**: *"Update the artifact to match reality. Add fitness functions that fail when the diagram drifts from the code (dependency-direction checks, structural-coupling thresholds)."*

### 6. Cyclic dependencies

- **Tell**: A imports B imports C imports A in the Decomposition or in the implied imports between children. No independent build order, no independent release.
- **check_failed**: `anti-pattern.cyclic-dependencies`
- **severity**: `soft_reject`
- **evidence pattern**: name the cycle (A → B → C → A) and where in the Decomposition or Structure Diagram it is observable.
- **recommended_action**: *"Break the cycle: extract a shared dependency, move a method to the other side, or apply dependency inversion. Add a fitness function to keep it broken."*

## AI-era and retrofit (four)

### 7. Laundered architecture (HARD ★)

- **Tell**: clean Structure Diagram contradicting observable code mess; zero `unknown` markings on a retrofit document; every rationale generic; Gap report empty or missing; diagram looks too clean for a system that was retrofitted.
- **check_failed**: `anti-pattern.laundered-architecture`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: applies only when `recovery_status:` is declared in front-matter.
- **evidence pattern**: name the absence pattern (zero unknowns + empty Gap report + all generic rationales). The combination is the laundering tell.
- **recommended_action**: *"Apply retrofit discipline. Mark observed structure with evidence. Mark rationale `verified` (with human source) or `unknown` (with follow-up owner). Populate the Gap report. The diagram should look like reality, including the messy parts."*

### 8. Fabricated decomposition rationale (HARD ★)

- **Tell**: rationales that say "chosen because it matches single-responsibility" / "follows DDD bounded-context separation" / "industry-standard pattern" / "balances X with Y" / "best practice" — generic principle invocation rather than historical recall.
- **check_failed**: `anti-pattern.fabricated-decomposition-rationale`
- **severity**: `hard_reject` ★ (refusal A)
- **conditional gating**: applies only when `recovery_status:` is declared. (In greenfield, generic rationale is `check.rationale.generic-principle-invocation` — soft.)
- **evidence pattern**: quote the generic-principle phrase; note the absence of historical recall (commit, decision record, ADR, named person).
- **recommended_action**: *"Replace with `rationale: { status: unknown, note: 'no preserved decision record; follow up with @owner' }`. Better prompting will not fix this — only structural refusal works."*

### 9. Ad-hoc composition

- **Tell**: Composition section is a sentence or a single diagram with no stated pattern, no wiring strategy, no runtime-unit boundaries. The system is "a bunch of services that talk to each other".
- **check_failed**: `anti-pattern.ad-hoc-composition`
- **severity**: `soft_reject`
- **evidence pattern**: quote the Composition section; note the absent pattern, wiring, and failure-mode story.
- **recommended_action**: *"Name a runtime pattern from the catalog. State wiring concerns: DI strategy, middleware stack ordering, message-bus topology. Draw a sequence diagram for the happy path and at least one failure path."*

### 10. Detailed-Design content in Architecture (HARD ★)

- **Tell**: internal algorithms / named data structures / specific library calls / framework-internal mechanics inside Decomposition entries or Interface entries.
- **check_failed**: `anti-pattern.dd-content-in-architecture`
- **severity**: `hard_reject` ★ (refusal B)
- **evidence pattern**: quote the leak verbatim — name the data structure, library, or algorithm; identify which Decomposition or Interface entry it lives in.
- **recommended_action**: *"Move to Detailed Design (or, if cross-cutting, an ADR). Replace here with the externally observable invariant."*

## Sweep order

Walk top to bottom. The first six are easier to spot mechanically (decomposition / structural shapes, observable contradictions). The AI-era four require judgement and benefit from a fresh-eyes pass.

## Aggregation rule

Multiple findings of the same anti-pattern across multiple elements are surfaced as separate findings (one per element) — not aggregated. This preserves per-element granularity that the matched author skill needs to act on.

For document-wide patterns (laundered-architecture, ad-hoc-composition at the artifact level), use `element_id: "GLOBAL"`.

Cross-link: `quality-bar-gate.md` (canonical id catalog); `decomposition-checks.md` (god-component, premature-decomposition, cyclic-dependencies); `composition-patterns-checks.md` (ad-hoc-composition); `retrofit-discipline-checks.md` (laundered-architecture, fabricated-decomposition-rationale).
