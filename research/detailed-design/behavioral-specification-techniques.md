# Behavioral Specification Techniques

**Research for:** Section 3.3 (Behavioral Specification) of the Detailed Design documentation page  
**Date:** 2026-04-05  
**Status:** Draft — for review before HTML authoring

---

## Purpose and Scope

This document researches how to specify *what* a unit does — its behavior — without simply paraphrasing how the code works. Behavioral specification is the craft of describing expected behavior at a level that enables independent test derivation and implementation by an engineer who has not seen the code.

This is a distinct skill from requirements writing. At the detailed design level, the unit already exists in the V-model decomposition. The question is: how do we describe its behavior precisely enough to derive tests from it, verify implementation against it, and support traceability upward to high-level requirements?

---

## 1. Algorithm Specification

### 1.1 The Core Problem: Level of Detail

The fundamental tension in algorithm specification is between two failure modes:

- **Too abstract:** "Process the data" — untestable, unimplementable, no verification possible.
- **Too concrete:** Copying pseudocode from the implementation — circular, useless for independent testing.

The right level is one that constrains behavior enough to derive tests and verify correctness, while leaving the implementer sufficient freedom to choose data structures, optimize, and refactor.

A widely cited principle from detailed design practice: pseudocode specification is complete when "the low level design should describe enough detail that it can be translated directly into code, so the coder should not have to create any algorithms." (Cal Poly Software Engineering, Detailed Design Document Format — http://users.csc.calpoly.edu/~jdalbey/308/Deliver/detailed.html)

### 1.2 Natural Language, Pseudocode, and Mathematical Notation

There is a spectrum of algorithm specification formalism:

**Natural language** ("sort the list in ascending order") is adequate when:
- The behavior is well-understood and unambiguous by convention.
- The criticality is low enough that the cost of ambiguity is tolerable.
- The algorithm is a single, indivisible operation from the unit's perspective.

**Pseudocode** is the workhorse of algorithm specification at the detailed design level. It is language-independent, readable without a compiler, and inspectable by designers and testers who did not write the code. Key properties of good pseudocode specification:
- Uses consistent structured constructs (sequence, selection, iteration).
- Avoids language-specific syntax (no Java generics, no C++ templates).
- Names variables for their role, not their type.
- Is complete: every branch is accounted for.

The Cal Poly Pseudocode Standard (http://users.csc.calpoly.edu/~jdalbey/SWE/pdl_std.html) provides a widely-used reference for pseudocode conventions in design documents, including capitalization of control keywords and indentation rules.

**Mathematical notation** (e.g., set notation, recurrence relations, big-O) is appropriate when:
- The algorithm has a precise mathematical definition (sorting, hashing, numerical methods).
- The correctness criterion is a mathematical property (e.g., "the output shall be a permutation of the input in non-decreasing order").
- The implementation team has the mathematical background to read it.

For sorting, a complete behavioral specification requires two conditions: the output is in the specified order *and* the output is a permutation of the input. Omitting either condition is an incomplete specification — a common error identified in formal verification literature on sorting algorithms. (HAL science open archive: https://hal.science/hal-00429040/document)

### 1.3 Algorithm Choice vs. Algorithm Behavior

Specification should distinguish between:

- **Behavioral requirement:** What property the algorithm must satisfy (e.g., "the output shall contain exactly the elements of the input, in ascending order").
- **Algorithm selection:** Which specific algorithm to use (e.g., "shall use quicksort with median-of-three pivot selection").

For most applications, specifying behavior is correct and leaving algorithm choice to the implementer is appropriate. The implementer retains freedom to optimize.

Algorithm selection becomes a specification concern when:
- **Determinism is required:** Some algorithms (e.g., stable sorts) preserve relative order of equal elements; others do not. If the calling system depends on this, it must be specified.
- **Performance bounds are contractual:** Real-time systems may require O(n log n) worst-case guarantees. Quicksort's O(n²) worst case may be unacceptable.
- **Safety standards require it:** DO-178C requires that Low-Level Requirements (LLRs) be detailed enough that "source code can be directly implemented without further information" (DO-178C, Section 11.9 definition of LLR, RTCA/DO-178C 2011). For DAL A software, the algorithm itself may need to be specified precisely enough to support formal verification.

### 1.4 DO-178C and Low-Level Requirement Detail

In DO-178C, the distinction between High-Level Requirements (HLRs) and Low-Level Requirements (LLRs) is precisely the level of behavioral detail:

> "Low-level requirements are software requirements from which source code can be directly implemented without further information." (DO-178C, Section 11.9 definition, as cited in: Parasoft, "What Is RTCA DO-178C?" — https://www.parasoft.com/learning-center/do-178c/what-is/)

This means an LLR must be a complete behavioral specification of a software unit. An HLR that says "the system shall filter sensor noise" is not an LLR. An LLR says: "the low-pass filter shall apply a Butterworth filter of order N with cutoff frequency F_c to the input signal, producing output y[n] such that..." — enough to write the code directly.

The practical implication: at the detailed design level, behavioral specifications *are* LLRs in DO-178C terms. This is not an academic distinction; it directly affects what verification evidence must be produced.

### 1.5 Decision Tables as Algorithm Specification

Decision tables excel at specifying rule-based algorithms where the behavior is determined by combinations of conditions rather than sequential steps.

Structure of a decision table:
- **Conditions** (top half): inputs or state variables, each with possible values.
- **Actions** (bottom half): outputs or effects that result from each rule.
- **Rules** (columns): each column is one complete combination of condition values mapped to actions.

Decision tables have two important formal properties:
- **Completeness:** Every possible combination of condition values has a rule. A table is incomplete if some input combination has no defined action.
- **Consistency:** No two rules with the same condition values produce different actions (unless conditions are explicitly mutually exclusive).

The relationship to testing is direct: each rule in the decision table is a minimal test case. A complete decision table gives a test suite with decision coverage by construction.

Source: ISTQB Foundation Level Syllabus, Section 4.2.3 "Decision Table Testing" — https://magdalenaolak.gitbook.io/istqb-foundation-level/4-test-techniques/4.2-black-box-test-techniques/4.2.3-decision-table-testing

### 1.6 Truth Tables for Boolean Logic

Truth tables are a degenerate form of decision table where all conditions are binary (true/false) and all combinations are enumerated. They are appropriate for specifying complex boolean expressions where the natural language formulation is ambiguous.

For example, specifying "the alarm shall activate when the temperature is high AND the pressure sensor is active, OR when the emergency override is set" is ambiguous about operator precedence. A truth table over the three binary variables makes the intended behavior unambiguous for all eight combinations.

Truth tables are most useful in specification when:
- The logic involves three or more conditions.
- Operator precedence in natural language is a known ambiguity source.
- The logic feeds directly into MC/DC test case derivation.

---

## 2. State Machine Specification

### 2.1 Finite State Machines: Core Concepts

A Finite State Machine (FSM) is a mathematical model of computation that is in exactly one of a finite number of states at any time. Behavior is defined by:

- A set of **states** (each representing a distinguishable mode of operation).
- A set of **transitions** (state changes triggered by events or conditions).
- **Guard conditions** on transitions (Boolean expressions that must hold for the transition to fire).
- **Actions** on transitions (operations executed when the transition fires).
- **Entry actions** on states (executed every time the state is entered).
- **Exit actions** on states (executed every time the state is exited).

The value of entry and exit actions is that they provide guaranteed initialization and cleanup, analogous to constructors and destructors. They simplify specifications because the action is specified once on the state, not repeated on every incoming or outgoing transition. (Miro Samek, "A Crash Course in UML State Machines", Quantum Leaps — https://www.state-machine.com/doc/AN_Crash_Course_in_UML_State_Machines.pdf)

FSMs are appropriate as behavioral specifications when a unit has distinct operational modes — when it behaves differently depending on prior history, not just current inputs. If a unit's output is determined entirely by its current inputs (stateless), an FSM adds complexity without value.

### 2.2 Harel Statecharts: The Key Innovation

The original FSM formalism has a known weakness: *state explosion*. For a system with N independent binary conditions, a flat FSM requires up to 2^N states. Real systems rapidly become unmaintainable.

David Harel's 1987 paper, "Statecharts: A Visual Formalism for Complex Systems," introduced three key extensions that solved this:

1. **Hierarchy (depth):** States can be nested. A superstate contains substates. A transition to the superstate applies to all substates. A default substate handles the initial entry.
2. **Concurrency (breadth/orthogonality):** A state can have orthogonal regions that run simultaneously, each with their own substate.
3. **Communication:** States can broadcast and receive events, supporting reactive coordination.

These extensions allow complex behavior to be expressed compactly. Harel developed statecharts while consulting for Israel Aircraft Industries on an avionics project (the Lavi fighter jet), making the formalism directly grounded in safety-critical embedded systems from its inception.

> Harel, D. (1987). "Statecharts: a visual formalism for complex systems." *Science of Computer Programming*, 8(3), 231–274.  
> Available: https://www.state-machine.com/doc/Harel87.pdf  
> DOI: https://doi.org/10.1016/0167-6423(87)90035-9

The paper is widely regarded as foundational. The ACM cites Harel's statechart work as among the most influential contributions to computer science. UML state machines are a direct descendant — the OMG's UML state machine semantics are explicitly based on Harel's statecharts. (UML State Machine, Wikipedia — https://en.wikipedia.org/wiki/UML_state_machine)

### 2.3 UML State Diagrams vs. State Tables

Both graphical (UML state diagram) and tabular (state transition table) notations can specify the same FSM. They have different strengths:

**UML state diagram** strengths:
- Visual topology makes the overall structure immediately comprehensible.
- Hierarchy and orthogonality are naturally expressed as nested or split boxes.
- Best for review by engineers who are not the author.
- Tooling (PlantUML, Mermaid, Stateflow, EA/Sparx) provides automatic layout and export.

**UML state diagram** weaknesses:
- As cited by UML State Machine Wikipedia: "State diagrams poorly represent the sequence of processing, be it order of evaluation of guards or order of dispatching events to orthogonal regions."
- Cannot easily represent complex guards or actions inline — requires supplementary text.
- A non-trivial state machine is "a mixture of graphical and textual representation." (UML State Machine, Wikipedia — https://en.wikipedia.org/wiki/UML_state_machine)

**State transition table** strengths:
- Every (state, event) pair has an explicit row — omissions are immediately visible.
- Completeness checking is mechanical: scan for empty cells.
- Machine-readable format suitable for automated test derivation.
- Easier to diff in version control.

**State transition table** weakness:
- Hierarchy is not naturally expressed — flat tables cannot directly capture superstate inheritance.

**Practical guidance:** Use a UML state diagram for communication and review. Use a state transition table as a completeness check and as the basis for test case derivation. Maintain both from the same source of truth when tooling supports it.

### 2.4 Textual State Table Format

A minimal textual state table has columns: `Current State | Event/Condition | Guard | Next State | Action`. Each row is one transition. A complete table has a row for every reachable (state, event) pair, including explicitly documented "ignored" transitions.

For safety-critical systems where omissions are dangerous, explicitly documenting "no transition" (or "error") for each unhandled event is better practice than leaving cells blank.

### 2.5 Tooling: PlantUML vs. Mermaid

Both PlantUML and Mermaid support state diagrams as code. The tradeoff:

- **PlantUML** (Java-based, mature): Broader UML feature coverage, more complex diagrams, preferred for formal regulated documentation. "PlantUML shows up in architecture reviews, regulated documentation, and long-lived specs because it models UML concepts explicitly." (Dan Does Code — https://www.dandoescode.com/blog/plantuml-vs-mermaid)
- **Mermaid** (JavaScript-based, lighter): Renders natively in GitHub/GitLab markdown, faster to write, adequate for simpler state machines. "Mermaid optimizes for speed and approachability." (ibid.)

For V-model compliance documentation, PlantUML is the safer default given its explicit UML semantics. Mermaid is appropriate for narrative documentation and PR descriptions.

### 2.6 When State Machine Specification Is Warranted

State machines add specification overhead. Use them when a unit:
- Has distinct operational modes that affect how inputs are processed (e.g., idle, initializing, running, error, shutdown).
- Has behavior that depends on history, not just current inputs.
- Has internal state that must be explicitly initialized and cleaned up (entry/exit actions matter).
- Interfaces with protocols that define legal message sequences.

Do not use a state machine specification when:
- The unit is a pure function (output determined by inputs alone, no side effects, no history).
- The unit has only two modes and the transition logic is trivial.
- The additional formalism exceeds the complexity of the behavior being described.

---

## 3. Decision Tables (Expanded)

### 3.1 Cause-Effect Graphing as a Precursor

Before decision tables can be written, the causes (inputs, conditions) and effects (outputs, actions) must be identified. Glenford Myers introduced cause-effect graphing in 1979 as a systematic method to enumerate these relationships using Boolean notation, which is then mechanically converted to a decision table.

The process:
1. Identify all causes (input conditions, state flags) and effects (outputs, state changes).
2. Draw the cause-effect graph: nodes are causes/effects, edges are logical relationships (AND, OR, NOT, constraints).
3. Convert the graph to a decision table by selecting high-yield input combinations.
4. Each column in the resulting table is one test case.

Myers introduced this technique specifically to address the combinatorial explosion problem: when there are many conditions, the number of combinations is 2^N, but many are impossible due to constraints. Cause-effect graphing prunes impossible combinations, giving a minimum high-yield test set.

> Myers, G.J. (1979). *The Art of Software Testing*. Wiley. Chapter 4 (starting p. 65).  
> Google Books: https://books.google.com/books/about/The_Art_of_Software_Testing.html?id=86rz6UExDEEC

### 3.2 Structure and Completeness

A decision table for specification (not just testing) has:
- **Limited-entry tables:** Conditions are binary (Yes/No, True/False). With N binary conditions, a complete table has 2^N rules.
- **Extended-entry tables:** Conditions can have more than two values. Completeness requires the Cartesian product of all condition values.

Completeness checking is mechanical: for N binary conditions, count the rules. If there are fewer than 2^N, the table is incomplete. For extended-entry tables, check that every combination appears.

Consistency checking: two rules with identical condition values must have identical actions (or the table is contradictory).

Source: BrowserStack, "What is Decision Table in Software Testing?" — https://www.browserstack.com/guide/decision-table

### 3.3 Decision Tables and MC/DC Coverage

Modified Condition/Decision Coverage (MC/DC) requires that each condition in a decision independently affects the outcome. MC/DC is required by DO-178C for DAL A and DAL B software, and is highly recommended for ASIL D in ISO 26262.

The relationship between decision tables and MC/DC is structural:

A complete decision table for a behavioral specification gives the full truth space for the decision logic. From this, an MC/DC test set can be derived systematically: for N conditions, N+1 test cases are sufficient (versus 2^N for exhaustive coverage). Each pair of test cases in the MC/DC set differs in exactly one condition while the outcome changes — this pair can be identified directly from the decision table.

> MC/DC Wikipedia: https://en.wikipedia.org/wiki/Modified_condition/decision_coverage  
> NASA Practical MC/DC: https://ntrs.nasa.gov/api/citations/20040086014/downloads/20040086014.pdf

This means: writing a complete decision table as part of the behavioral specification is not just good practice — it directly enables MC/DC test case derivation, making the specification a first-class input to the test process.

---

## 4. Sequence and Interaction Specification

### 4.1 When Sequence Matters at the Unit Level

Sequence diagrams are most commonly used at the architectural or system level to show how components interact. At the unit level, sequence specification is warranted when:

- The unit implements a **multi-step protocol** where the order of operations is contractually significant (e.g., initialize → configure → start → data → stop).
- The unit **manages callback sequences** or event delivery that must happen in a defined order.
- The unit interacts with hardware or external interfaces where operation ordering is externally imposed.
- The unit's behavior during a **multi-message transaction** needs to be specified independently of the larger system context.

A UML sequence diagram at the unit level shows the object/component under specification, its internal objects if relevant, and the time-ordered sequence of messages (method calls, events, signals) it sends and receives.

### 4.2 Interaction Specification Elements

Key elements from UML 2.x sequence diagram notation:
- **Lifelines:** Participants (objects, actors, subsystems) involved in the interaction.
- **Messages:** Synchronous calls, asynchronous signals, return messages, with optional parameters.
- **Guards/conditions:** `[condition]` on combined fragments (alt, opt, loop).
- **Combined fragments:** `alt` for alternatives (if/else), `loop` for iteration, `opt` for optional sequences.

The OMG UML specification is the normative reference. (UML 2.x Sequence Diagrams overview — https://www.uml-diagrams.org/sequence-diagrams.html)

### 4.3 Limits at the Unit Level

Sequence diagrams carry costs at the unit level:
- They specify *one scenario* — one trace through the system. A full behavioral specification may require many sequence diagrams for different input paths.
- State machine diagrams are more compact when behavior is mode-driven. Sequence diagrams are better when behavior is protocol-driven.
- Maintenance cost is high: every change to the sequence requires updating the diagram.

The practical decision: use sequence diagrams when the protocol is the primary specification challenge. For units where state or conditions are the specification challenge, use state machines or decision tables respectively.

---

## 5. Natural Language vs. Semi-Formal vs. Formal Specification

### 5.1 The Spectrum

Behavioral specification occupies a continuous spectrum from unconstrained natural language to fully formal mathematical notation:

```
Natural language
    → Structured natural language (EARS, IEEE 29148 shall-statements)
    → Pseudocode / structured English
    → Semi-formal notation (UML, SysML, state machines, decision tables)
    → Formal specification languages (Z, B, VDM, JML, ACSL)
    → Mechanically verified formal proofs (Coq, Isabelle, PVS)
```

Each step up the formalism ladder:
- **Increases precision** (fewer possible interpretations).
- **Increases verification power** (automated tools can check more).
- **Increases cost** (writing time, tooling investment, required expertise).
- **Decreases accessibility** (fewer stakeholders can read and review it).

Source: ResearchGate, "Trade-off between natural language and formal specifications" — https://www.researchgate.net/figure/Trade-off-between-natural-language-and-formal-specifications-4-inset-showing-the_fig1_260799988

### 5.2 ISO 26262 ASIL Guidance

ISO 26262 explicitly maps behavioral specification formalism to ASIL levels:

- **ASIL A / ASIL B:** Natural language with simple figures is sufficient ("information notation").
- **ASIL C / ASIL D:** Semi-formal notation is recommended. ISO 26262 "highly recommends" the use of semi-formal modeling languages for ASIL D designs.
- **ASIL D:** Formal verification using formal methods is highly recommended. Executable validation (prototyping or simulation) is mandatory for ASIL D.

Examples of semi-formal languages recommended by the standard: Stateflow (MathWorks), SysML.

Source: Parasoft, "How to Satisfy ISO 26262 ASIL Requirements" — https://alm.parasoft.com/hubfs/whitepaper-Achieving-Functional-Safety-Automotive-ISO-26262-ASIL.pdf

### 5.3 DO-178C and DO-333 Formal Methods

DO-178C does not mandate formal specification at any DAL level, but DO-333 (the Formal Methods Supplement to DO-178C) provides a framework for using formal methods to satisfy DO-178C objectives when formal specification is used.

DO-333 allows certain DO-178C verification objectives (that would otherwise require testing) to be satisfied through formal analysis, provided the formal analysis is demonstrated to be sound and complete. This is not an exemption from rigor — it is an alternative verification path.

Formal specification at DAL A under DO-333 typically uses:
- **Model checking:** Exhaustive state-space exploration to verify properties.
- **Theorem proving:** Mathematical proof of correctness properties.
- **Abstract interpretation:** Sound over-approximation for properties like runtime error freedom.

Source: AFuzion, "Introduction to DO-333 Formal Methods" — https://afuzion.com/do-333-introduction-formal-methods/  
Source: NASA NTRS, "Formal Methods Case Studies for DO-333" — https://ntrs.nasa.gov/citations/20140004055

### 5.4 Behavioral Interface Specification Languages (BISLs)

For Java and similar OO languages, a middle path exists between informal prose and full formal specification: Behavioral Interface Specification Languages (BISLs) that annotate code with formal specifications expressed in the host language's type system.

**Java Modeling Language (JML)** is the primary example. JML specifications are written as annotations in Java comments (beginning with `@`), making them syntactically valid Java while being machine-checkable by JML tools (OpenJML, KeY).

JML specification elements:
- `@requires` — precondition (what must be true when the method is called).
- `@ensures` — postcondition (what must be true when the method returns).
- `@invariant` — class invariant (must hold in all stable states).
- `@assignable` — frame condition (what state the method may modify).

JML inherits from Design by Contract (Meyer's Eiffel, 1986) and Hoare Logic (1969). The Hoare triple `{P} C {Q}` (precondition P, command C, postcondition Q) is the theoretical foundation.

> Leavens, G.T. et al. (2006). "Preliminary Design of JML: A Behavioral Interface Specification Language for Java." *ACM SIGSOFT Software Engineering Notes*, 31(3).  
> DOI: https://dl.acm.org/doi/10.1145/1127878.1127884  
> Leavens et al. (2012). "Behavioral interface specification languages." *ACM Computing Surveys*, 44(3).  
> DOI: https://dl.acm.org/doi/10.1145/2187671.2187678

---

## 6. Specification Patterns

### 6.1 Pre/Postcondition Pattern

**Structure:** "Given [precondition on inputs and state], when [the operation is invoked], then [postcondition on outputs and state changes]."

**Strengths:**
- Directly maps to Hoare logic and Design by Contract.
- Enables independent test derivation: the precondition defines valid test inputs; the postcondition defines the expected result.
- Machine-checkable in JML and similar languages.
- Natural fit for pure functions and stateless computations.

**Weaknesses:**
- Postconditions for stateful units must enumerate all state changes, which becomes verbose for complex behavior.
- Does not capture temporal sequences (order of operations).

**Example (correct form):**
```
Requires: the input buffer contains at least one element; the comparator function is non-null and implements a total order.
Ensures: the returned list contains exactly the elements of the input buffer; the elements are in non-descending order according to the comparator; the input buffer is unchanged.
```

**Common error:** Writing postconditions that describe the algorithm rather than the result — "shall iterate through the list and compare adjacent elements" is not a postcondition, it is pseudocode.

### 6.2 Scenario-Based Pattern (Given/When/Then)

Derived from BDD (Behavior-Driven Development), originating from Dan North's work on executable specifications and popularized through the Gherkin language used by Cucumber.

**Structure:** "Given [system state], When [event or action], Then [observable outcome]."

**Strengths:**
- Highly readable by non-technical stakeholders.
- Each scenario is a complete, executable test case.
- Natural for discrete event-driven behavior and protocol specifications.

**Weaknesses:**
- Multiple scenarios are needed to cover all behavior — no completeness guarantee without additional analysis.
- "BDD's communication benefit peaks at the acceptance layer and drops sharply at the unit layer." (BDD in Practice, DEV Community — https://dev.to/naina_garg/bdd-in-practice-where-givenwhenthen-actually-helps-2nd) The format can feel forced for low-level algorithmic behavior.
- Not well-suited for continuous-valued or purely mathematical behavior.

**When to use at unit level:** Protocol implementations, event-driven units, units with complex initialization/shutdown sequences, units where the scenario language naturally reflects the domain.

Source: BDD Wikipedia: https://en.wikipedia.org/wiki/Behavior-driven_development

### 6.3 Property-Based Pattern

**Structure:** "For all [valid inputs satisfying constraint], [invariant or relationship that must hold]."

**Strengths:**
- Directly enables property-based testing (QuickCheck, Hypothesis, jqwik).
- Captures universal invariants that should hold across all inputs rather than specific scenarios.
- Often more concise than listing scenarios: one property may replace dozens of test cases.
- Natural for mathematical operations, sorting, compression, encoding.

**Examples:**
- "For all non-null input lists, sort(sort(L)) = sort(L)." (idempotence)
- "For all non-null input lists, sort(L) is a permutation of L." (no element loss)
- "For all valid messages M, decode(encode(M)) = M." (round-trip property)

**Weaknesses:**
- Does not specify exact output values — only relationships. Not sufficient alone.
- Engineers unfamiliar with property-based thinking may find it harder to write.
- Does not directly specify behavior for invalid inputs (that requires a separate precondition or error-handling specification).

### 6.4 Tabular Input/Output Specification (Parnas Tables)

David Parnas developed tabular expressions at the U.S. Naval Research Laboratory (NRL) in the late 1970s as part of the Software Cost Reduction (SCR) project, applied to the A-7 aircraft Operational Flight Program.

Parnas tables specify a function by partitioning the input domain and giving the output for each partition. The table forces completeness: the partitions must cover the entire input domain, and must be non-overlapping (or overlaps explicitly resolved).

Key table types in the SCR formalism:
- **Function tables:** Specify output as a function of input partitions.
- **Event tables:** Specify when state transitions occur.

The SCR method was applied to safety-critical systems including the Darlington nuclear power plant, Bell telephone networks, and A-6/A-7 avionics.

> Parnas, D.L. (1972–1979). "Tabular Representation of Relations." (Various NRL technical reports.)  
> Summary: Wassyng, A., Janicki, R. "Tabular Expressions in Software Engineering." ResearchGate — https://www.researchgate.net/publication/228939082_Tabular_Expressions_in_Software_Engineering

### 6.5 Choosing the Right Pattern

| Behavior Type | Recommended Pattern |
|---|---|
| Pure function, mathematical transformation | Pre/postcondition + property-based |
| Rule-based logic (many conditions → action) | Decision table |
| Mode-dependent behavior, event-driven | State machine |
| Protocol, multi-step ordered sequence | Scenario-based or UML sequence diagram |
| Complex boolean guard logic | Truth table |
| Complex input domain partitioning | Parnas table / tabular specification |
| High ASIL/DAL, unit-level formal verification | JML or equivalent BISL |

No single pattern suffices for all behavior. A complete unit specification may combine a state machine (modes), decision tables (rule-logic within a mode), and pre/postconditions (per-transition behavior).

---

## 7. Cross-Cutting Concerns

### 7.1 Specification Must Enable Independent Test Derivation

A behavioral specification is complete if an engineer who has not seen the source code can derive a test suite from it. This is the test of a specification's quality, not its length or formalism.

If the test engineer must look at the implementation to understand what to test, the specification has failed.

### 7.2 Specification Should Not Paraphrase Implementation

A common anti-pattern: writing the specification after the code, describing what the code does. This produces circular documentation that:
- Fails to catch implementation errors (tests derived from the specification will match the code's bugs).
- Provides no value for review — reviewers cannot identify discrepancies.
- Violates the V-model intent that requirements drive implementation, not the reverse.

The correct process is specification-first: write the behavioral specification, derive tests, then implement. In retrofit (legacy) scenarios, the specification must be written based on the *required* behavior, then verified against the actual implementation. Discrepancies are findings, not specification adjustments.

### 7.3 Error Handling is Behavior

Behavioral specifications must cover invalid inputs and error conditions, not just the happy path. Common omissions:

- What happens when a precondition is violated (throw exception? return error code? undefined behavior?).
- What state the unit is left in after an error (is it recoverable?).
- What happens on boundary values (empty collections, null inputs, maximum values).

In safety-critical systems, the specified behavior for invalid inputs is often more important than the behavior for valid inputs — defensive programming patterns must match the specification.

### 7.4 Traceability From Specification to Tests

Each element of a behavioral specification should trace to at least one test case. This is the V-model's verification closure requirement. The specification patterns described above differ in how directly they support traceability:

- Decision tables: one-to-one mapping between rules and test cases.
- State machine transitions: one-to-one mapping between transitions and test cases.
- Pre/postconditions: each postcondition clause should trace to at least one test case.
- Property-based: each property should trace to a set of test cases that collectively exercise the property.

---

## 8. Honest Gaps and Limitations

1. **ISO 26262 source limitation:** The ASIL-to-notation mapping cited above is from secondary sources (Parasoft white paper, Wikipedia). The authoritative source is ISO 26262-6:2018, Section 7 and Tables. Engineers working in automotive safety should consult the standard directly — not this document.

2. **DO-178C LLR definition:** The LLR definition cited is from secondary sources. The normative source is RTCA DO-178C (2011), Section 11.9. Access requires RTCA membership or purchase.

3. **Formal specification adoption:** Claims about cost and adoption of formal methods are based on general characterizations in the literature. Project-specific cost data is scarce and highly context-dependent.

4. **JML tooling maturity:** JML tools (OpenJML, KeY) are research-grade in many respects. Industrial adoption is limited compared to the academic literature on the subject.

5. **Parnas tables tooling:** The SCR toolset from NRL is described in academic literature but the original tools are not actively maintained. Modern equivalents exist in model-checking tools that accept SCR-style specifications.

---

## Sources

### Primary: Standards and Specifications

- **RTCA DO-178C** (2011). *Software Considerations in Airborne Systems and Equipment Certification*. RTCA, Inc. (purchase required — https://www.rtca.org)
- **RTCA DO-333** (2011). *Formal Methods Supplement to DO-178C and DO-278A*. RTCA, Inc. Overview: https://afuzion.com/do-333-introduction-formal-methods/
- **ISO 26262-6:2018.** *Road Vehicles — Functional Safety — Part 6: Product Development at the Software Level.* ISO. Overview: https://www.iso.org (purchase required)
- **ISO/IEC/IEEE 29148:2018.** *Systems and software engineering — Life cycle processes — Requirements engineering.* IEEE: https://standards.ieee.org/standard/29148-2018.html

### Primary: Foundational Academic Works

- **Harel, D.** (1987). "Statecharts: a visual formalism for complex systems." *Science of Computer Programming*, 8(3), 231–274. DOI: https://doi.org/10.1016/0167-6423(87)90035-9. PDF: https://www.state-machine.com/doc/Harel87.pdf
- **Myers, G.J.** (1979). *The Art of Software Testing*. Wiley. (Cause-effect graphing, Chapter 4.) Google Books: https://books.google.com/books/about/The_Art_of_Software_Testing.html?id=86rz6UExDEEC
- **Meyer, B.** (1986). "Design by contract." Origins in Eiffel language design; described in detail in: *Object-Oriented Software Construction*, 2nd ed. (1997). Design by contract Wikipedia: https://en.wikipedia.org/wiki/Design_by_contract
- **Hoare, C.A.R.** (1969). "An axiomatic basis for computer programming." *Communications of the ACM*, 12(10), 576–580. Hoare logic Wikipedia: https://en.wikipedia.org/wiki/Hoare_logic
- **Leavens, G.T. et al.** (2006). "Preliminary Design of JML." *ACM SIGSOFT Software Engineering Notes*, 31(3). DOI: https://dl.acm.org/doi/10.1145/1127878.1127884
- **Leavens, G.T. et al.** (2012). "Behavioral interface specification languages." *ACM Computing Surveys*, 44(3). DOI: https://dl.acm.org/doi/10.1145/2187671.2187678
- **Parnas, D.L., Heninger, K.** (1978–1980). SCR tabular specification work at NRL. Summary: https://www.researchgate.net/publication/228939082_Tabular_Expressions_in_Software_Engineering

### Secondary: Industry References

- Parasoft. "What Is RTCA DO-178C?" — https://www.parasoft.com/learning-center/do-178c/what-is/
- Parasoft. "How to Satisfy ISO 26262 ASIL Requirements." — https://alm.parasoft.com/hubfs/whitepaper-Achieving-Functional-Safety-Automotive-ISO-26262-ASIL.pdf
- Miro Samek, Quantum Leaps. "A Crash Course in UML State Machines." — https://www.state-machine.com/doc/AN_Crash_Course_in_UML_State_Machines.pdf
- NASA NTRS. "A Practical Approach to Modified Condition/Decision Coverage." — https://ntrs.nasa.gov/api/citations/20040086014/downloads/20040086014.pdf
- NASA NTRS. "Formal Methods Case Studies for DO-333." — https://ntrs.nasa.gov/citations/20140004055
- Cal Poly SWE. "Pseudocode Standard." — http://users.csc.calpoly.edu/~jdalbey/SWE/pdl_std.html
- Cal Poly SWE. "Detailed Design Document Format." — http://users.csc.calpoly.edu/~jdalbey/308/Deliver/detailed.html
- ISTQB. "Decision Table Testing." Glossary: https://glossary.istqb.org/en_US/term/decision-table-testing
- ISTQB Foundation Level. Section 4.2.3 Decision Table Testing: https://magdalenaolak.gitbook.io/istqb-foundation-level/4-test-techniques/4.2-black-box-test-techniques/4.2.3-decision-table-testing
- BrowserStack. "What is Decision Table in Software Testing?" — https://www.browserstack.com/guide/decision-table
- HAL Science. "Specifying and Proving a Sorting Algorithm." — https://hal.science/hal-00429040/document
- UML State Machine, Wikipedia — https://en.wikipedia.org/wiki/UML_state_machine
- Hoare Logic, Wikipedia — https://en.wikipedia.org/wiki/Hoare_logic
- Modified condition/decision coverage, Wikipedia — https://en.wikipedia.org/wiki/Modified_condition/decision_coverage
- Behavior-driven development, Wikipedia — https://en.wikipedia.org/wiki/Behavior-driven_development
- Dan Does Code. "Software Diagrams — Plant UML vs Mermaid." — https://www.dandoescode.com/blog/plantuml-vs-mermaid
- BDD in Practice, DEV Community — https://dev.to/naina_garg/bdd-in-practice-where-givenwhenthen-actually-helps-2nd
- Recurse Center. "Paper of the Week: Statecharts." — https://www.recurse.com/blog/59-paper-of-the-week-statecharts-a-visual-formalism-for-complex-systems
- ResearchGate. "Trade-off between natural language and formal specifications." — https://www.researchgate.net/figure/Trade-off-between-natural-language-and-formal-specifications-4-inset-showing-the_fig1_260799988
- AFuzion. "Introduction to DO-333 Formal Methods." — https://afuzion.com/do-333-introduction-formal-methods/
- Barrgroup. "Introduction to Hierarchical State Machines." — https://barrgroup.com/embedded-systems/how-to/introduction-hierarchical-state-machines
