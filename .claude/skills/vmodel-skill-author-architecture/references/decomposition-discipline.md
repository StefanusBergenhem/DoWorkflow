# Decomposition discipline

Decomposition is the primary architectural decision at any non-leaf scope. A wrong decomposition cannot be rescued by clever interfaces, resilient composition, or strong tests — it leaks entropy into every downstream artifact. Five principles, in order of precedence, then a depth-stop trio.

## 1. Information hiding (Parnas, 1972)

Each module's boundary hides a design decision likely to change. The published test is not "what does this module do?" but **"if requirement X changes, how many modules have to change with it?"** A good decomposition answers *one*.

Mental test:

- List the next three changes the business is likely to demand.
- Check that each change lands in one module, not a diagonal across the tree.
- If a likely change cuts through 3+ modules, the boundary is wrong.

Source: Parnas, *On the Criteria To Be Used in Decomposing Systems into Modules*, CACM 15(12), 1972.

## 2. Cohesion and coupling (Constantine / Stevens / Myers)

- **Inside a module:** elements should belong together because they contribute to one well-defined responsibility (*functional cohesion* — strongest form). Avoid temporal, logical, coincidental cohesion.
- **Between modules:** the interaction should carry the minimum information needed (*data coupling* > stamp/structure > control > shared state).

Architect's quick test: write **one sentence** stating what the module is responsible for. If the sentence needs *and*, an enumerated list, or a comma-list, the module is doing two jobs and will change for two reasons. Split it, or accept that one box hides two that need separate interfaces.

## 3. Bounded contexts (Evans, DDD)

A bounded context is a region within which a single model of the domain applies and every term carries one meaning. The boundary is **linguistic**: when the same word (*order*, *customer*, *shipment*) starts to mean different things in two regions, a context boundary is forming whether you named it or not.

At Architecture time, look for where the **ubiquitous language fractures** — those are the lines of the diagram. Drawing a child at a boundary that cuts through the language will cost you interface breakage for the lifetime of the system.

Source: Evans, *Domain-Driven Design Reference* (2015); *Domain-Driven Design* (Addison-Wesley, 2003).

## 4. Context-mapping patterns (DDD strategic)

When children are themselves bounded contexts that have to talk, three patterns cover the majority of real decisions:

| Pattern | Use when | Cost of getting it wrong |
|---|---|---|
| **Anticorruption Layer (ACL)** | One context's model would contaminate the other (legacy or third-party API). | Without ACL: vocabulary leaks across boundary; the legacy model becomes load-bearing in the new context. |
| **Partnership** | Two contexts evolve in lock-step with mutual dependency acknowledged; releases coordinate. | Defaulting to Partnership because it is comfortable produces N teams that cannot ship independently. |
| **Published Language** | A stable, versioned vocabulary (schema/event/protocol) third parties depend on; deprecation policy explicit. | Without versioning + deprecation, every change breaks downstream consumers. |

Evans catalogues nine; these three cover most cases. If the right pattern is not one of these, name it explicitly and cite the source.

## 5. Conway's Law, inverted (Skelton & Pais, Team Topologies, 2019)

Conway: systems replicate the communication structure of the organisations that build them. Skelton/Pais's flip: choose the contexts you want teams to own, then staff to match.

At Architecture time, the question is symmetric:

- If your decomposition forces 6 teams to coordinate on every change, the decomposition is wrong.
- If it maps one child to one team with a clear cognitive load, the decomposition has a chance of staying honest under pressure.

Architecture that ignores team topology designs its own drift.

Team types worth naming when allocating ownership:

- **Stream-aligned** — owns a value stream end-to-end.
- **Platform** — provides internal capabilities to stream-aligned teams.
- **Complicated-subsystem** — narrow specialisation (ML pipeline, codec, real-time engine).
- **Enabling** — short-term uplift; coaches another team then dissolves.

## Depth-stop trio (when to stop subdividing)

Three rules of thumb. They pull against each other — that is the point. A good decomposition sits where none of them fires.

1. **Depth test.** If children interact in ways the parent's Architecture cannot explain, go deeper — the parent is under-specifying; the design lives a level down.
2. **Cognitive-load test.** A scope should fit in one Requirements / Architecture pair a reviewer can hold in their head. A diagram with 14 children is a decomposition that has not decomposed.
3. **Change-blast test.** If subdividing further would not let parts change independently, you have gone too deep — split something with no seam. Undo.

Record which test fired if the decomposition was revised — reviewers ask.

## Slot-fill check before declaring decomposition complete

For each child, fill these slots. If any slot can't be filled, the boundary is wrong.

```
- id:                    <<single hyphenated id>>
- one-sentence purpose:  <<no 'and', no comma list>>
- responsibilities:      <<≤3 architectural-level items>>
- allocates:             <<≥1 parent requirement id>>
- likely-change driver:  <<one specific anticipated change this child absorbs alone>>
```

Banned fields (Rule 8). Decomposition entries must NOT carry `bounded_context_line`, `owning_team_type`, or `test_seam`. These were team-topology-derived shapes; not load-bearing for AI-first frameworks where Conway's-law concerns do not apply. Test-design content (test surface, fake strategy, port enumeration) lives in TestSpec at the corresponding scope.

## Anti-patterns local to this discipline

- **Decomposition by processing step** (input → process → output) — every change to *what* gets processed cascades through every step. Use information hiding instead.
- **Decomposition by layer** (UI / business logic / data) at non-trivial scope — produces distributed monolith; teams change feature-by-feature, not layer-by-layer.
- **Premature decomposition** — 10 services for a system 3 people can fit in a room for. Distributed-system tax without independent-evolution benefit.
- **God child** — one child owns the domain; peripherals are thin wrappers. The one-sentence test fails loudly.

Cross-link: see `anti-patterns.md` for the catalogue with tells.
