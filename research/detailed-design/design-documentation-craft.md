# Design Documentation Craft

**Research for:** Section 3.1 (Design Documentation Fundamentals) and Section 3.4 (Design Rationale and Decisions)
**Date:** 2026-04-05
**Feeds into:** `docs/guide/artifacts/detailed-design.html`

---

## Overview

This research examines what separates useful design documentation from code paraphrase. Two questions drive it: (1) what is design documentation fundamentally *for*, and (2) what is the unique content that only design documentation — not code — can provide? The answers converge on a single axis: **intent, decisions, and constraints are invisible in code**. A design document is the artifact that makes them visible and independently verifiable.

---

## Part 1: Design Documentation Fundamentals

### 1.1 The Foundation: Parnas and Clements on Rational Design Process

The most rigorous academic treatment of what design documentation *is* comes from David L. Parnas and Paul C. Clements. Their 1986 paper "A Rational Design Process: How and Why to Fake It" (IEEE Transactions on Software Engineering, Vol. 12, No. 2, pp. 251–257) establishes a framework that remains the clearest statement of what design documents are for.

Parnas and Clements argue that even when software is developed through iterative and exploratory means, the documentation should be written *as if* a rational linear process was followed. This is not dishonesty — it is the discipline of presenting design knowledge in the most useful form for those who must verify, maintain, or extend the system. The paper's key insight is that **documentation is not a record of what happened; it is a structured presentation of what is known**.

Their model describes design documentation as a sequence of artifacts, each answering specific questions:

- What are the requirements the design must satisfy?
- How is the system divided into modules, and on what principle?
- What does each module hide from the rest?
- What are the interfaces between modules?
- What assumptions does each part make about the rest?

The paper argues explicitly that documentation should be structured to ensure there is **one, and only one, place for every fact** in the design. Redundancy creates inconsistency under change. Every fact belongs to exactly one document section.

**Critical implication for quality:** A design document written *after* coding — or worse, inferred from code — is almost always a structural failure. It reports the code rather than documenting the design. Parnas and Clements treat documentation as the *medium* of design: decisions are not made until they are documented.

Source: Parnas, D.L. and Clements, P.C. (1986). "A Rational Design Process: How and Why to Fake It." *IEEE Transactions on Software Engineering*, 12(2), 251–257.
Available: https://ieeexplore.ieee.org/document/6312940

### 1.2 Information Hiding as a Design Documentation Principle

Before the 1986 paper, Parnas established the theoretical basis for *what* a design document must capture in his 1972 paper "On the Criteria To Be Used in Decomposing Systems into Modules" (Communications of the ACM, Vol. 15, No. 12, pp. 1053–1058).

The principle: every module is characterized by **its knowledge of a design decision which it hides from all others**. Its interface is chosen to reveal as little as possible about its inner workings.

This has a direct consequence for documentation. The design document for a module must describe:

1. **What the module hides** — the design decision it encapsulates
2. **What the module reveals** — its interface, the contract with callers
3. **Why that particular boundary** was drawn — the reasoning behind the decomposition

Code cannot reliably communicate (1) or (3). The fact that a variable is stored as a sorted array, or that a timestamp is encoded as Unix epoch, is visible in the code. But *why that choice was made*, and *what alternatives were rejected*, is not. Parnas's information hiding principle defines precisely what documentation must supply.

Source: Parnas, D.L. (1972). "On the Criteria To Be Used in Decomposing Systems into Modules." *Communications of the ACM*, 15(12), 1053–1058.
DOI: https://dl.acm.org/doi/10.1145/361598.361623

### 1.3 The A-7E Module Guide: Parnas's Existence Proof

Parnas applied his documentation principles to a real system in the A-7E Software Module Guide (NRL Memorandum Report 4702, Naval Research Laboratory, Washington D.C., 1981), co-authored with K.H. Britton.

This document demonstrates the principle at scale. The module guide describes the basic organization of the A-7E onboard flight software, structured according to the information hiding principle. Its stated purposes are:

- Orient new software engineers to the system
- Explain the principles used to design the decomposition
- Show how responsibilities are allocated without exposing irrelevant implementation details

The guide supplements the software with a hierarchically-structured document that allows both designers and maintainers to identify relevant parts without reading through unrelated code. It is explicitly a *navigation tool* for a complex system — not a code summary.

**Lesson:** Design documentation must be structured to support independent navigation of the system. A reader who needs to understand module A should not be required to first understand module B. Documentation structure reflects design structure.

Source: Parnas, D.L. and Britton, K.H. (1981). *A-7E Software Module Guide*. NRL Memorandum Report 4702.
Available via DTIC: https://apps.dtic.mil/docs/citations/ADA108649

### 1.4 Jack Reeves: Code Is Design — What This Means for Documentation

Jack Reeves' 1992 essay "What Is Software Design?" (C++ Journal, Fall 1992) argued that **source code is the real software design**. Unlike structural engineering where blueprints are designs and buildings are products, software's "building" phase (compilation) is trivially cheap. This means the real intellectual work — the design — is in writing the code.

This argument is frequently misread as "no documentation needed." Reeves explicitly corrected this reading. His position is:

- The source code *is* the design, therefore it must be written with design-level care
- Code cannot always express all aspects of design; **auxiliary documentation** is needed for what is difficult to extract directly from code
- Many of these aspects are best depicted graphically (structure diagrams, state machines, sequence flows)

Reeves' framework actually clarifies what documentation must add: **everything that a skilled reader could not eventually derive from reading the code, plus everything that makes the code navigable and understandable at scale**.

The implication: if a piece of information *can* be reliably derived from the code by a competent reader, documenting it is waste. If it *cannot* — because it represents intent, rejected alternatives, external constraints, or historical context — documentation is obligatory.

Source: Reeves, J.W. (1992). "What Is Software Design?" *C++ Journal*, Fall 1992.
Full text: https://www.developerdotstar.com/mag/articles/reeves_design.html
PDF: https://jpaulgibson.synology.me/~jpaulgibson/TSP/Teaching/Teaching-ReadingMaterial/Reeves92.pdf

### 1.5 Martin Fowler: Code as Primary Documentation, Not Sole Documentation

Martin Fowler's position reinforces and extends Reeves. In "Code as Documentation" (martinfowler.com), Fowler states that code is the primary documentation of a software system, because it is the only artifact sufficiently detailed and precise to serve that role. However, he explicitly warns against the common misconception that this means documentation is unnecessary:

> "Usually there is a need for further documentation to act as a supplement to the code."

Fowler's criterion for when additional documentation is required: when information cannot be inferred from the code alone, or when the code requires context to be understood.

The key observation: saying code *is* documentation does not mean a given codebase *is good* documentation. Code can be clear or opaque. Supplementary documentation compensates for opacity but, more importantly, provides the *why* that no amount of readable code can substitute.

Source: Fowler, M. "Code As Documentation." martinfowler.com.
URL: https://martinfowler.com/bliki/CodeAsDocumentation.html

### 1.6 IEEE 1016: The Standard for Software Design Descriptions

IEEE Std 1016-2009 "Standard for Information Technology — Systems Design — Software Design Descriptions" (revision of IEEE Std 1016-1998) defines what a Software Design Description (SDD) is and what it must contain.

Key provisions from the standard:

- An SDD is "a representation of a software design used for recording and communicating design information to key design stakeholders"
- The standard is explicitly applicable to both **high-level and detailed design** documentation
- It covers reverse engineering ("situations when a design description is recovered from an existing implementation")
- It does not prescribe specific design methodologies but establishes requirements on what the SDD must convey

The 2009 revision elevated the standard from a recommended practice to a full standard and extended the concepts of view, viewpoint, stakeholder, and concern from architecture description to support detailed design documentation. This positions IEEE 1016 as the closest thing to a normative definition of what a detailed design document must address.

Source: IEEE Std 1016-2009. *IEEE Standard for Information Technology — Systems Design — Software Design Descriptions*.
IEEE Xplore: https://ieeexplore.ieee.org/document/5167255
Standard page: https://standards.ieee.org/ieee/1016/4502/

### 1.7 ASPICE SWE.3: Industry Normative Requirements for Detailed Design

Automotive SPICE (ASPICE) Process Reference Model, process SWE.3 "Software Detailed Design and Unit Construction," defines what detailed design must document for safety-critical automotive software.

The base practices require:

- A detailed design developed for **every software component** defined in the architectural design
- Documentation of **all relevant interfaces** for each software unit
- Documentation of **dynamic behavior** including inter-unit interaction
- **Bidirectional traceability**: software requirements → detailed design, architectural design → detailed design, detailed design → software units

Most importantly for this research, the ASPICE guideline requires that the detailed design "detail the design of individual components or modules" with output constituting Low-Level Design (LLD) — the direct specification basis from which test cases are derived.

This establishes a normative binding: the detailed design document is the test basis for unit testing. A design that cannot support test derivation is incomplete by definition under SWE.3.

Sources:
- UL/SIS ASPICE SWE.3 Guide: https://www.ul.com/sis/resources/process-swe-3
- Xenban ASPICE SWE.3 overview: https://xenban.com/index.php/2025/02/12/automotive-spice-swe-3-software-detailed-design-and-unit-construction/

### 1.8 The "Could You Derive Tests From This?" Litmus Test

The connection between detailed design and test derivation is not merely a bureaucratic traceability requirement — it is a quality criterion for the design document itself.

**The reasoning:**

DO-178C (Software Considerations in Airborne Systems and Equipment Certification) specifies that software verification must be "requirements-based" — test cases are derived from requirements, not inferred from code. The detailed design document constitutes the low-level requirements that drive unit test case derivation. DO-178C Wikipedia entry: https://en.wikipedia.org/wiki/DO-178C

In practice, this creates a clear and immediately applicable completeness criterion:

> If a reviewer cannot derive a meaningful set of test cases from the design document alone — without reading the code — the design document is incomplete.

The information gap that prevents test derivation is diagnostic. Common gaps include:

- **Missing boundary conditions**: the design states "validates input" without specifying valid ranges
- **Undocumented invariants**: postconditions on state are implicit in the implementation but absent from the design
- **Absent error behavior**: normal-case behavior is documented; what happens on constraint violation is not
- **Implicit assumptions**: a function assumes its caller has locked a mutex, but this precondition is not stated

Each of these gaps represents information that the implementer knew but did not document. An independent verifier writing tests from the design would miss the invariant, skip the error case, and violate the precondition without warning. The design has failed its primary function: enabling independent verification.

This criterion is consistent with the testability quality attribute identified in requirements engineering literature. A testable requirement/specification is one "expressed so clearly, so unambiguously, so completely that there can only be one interpretation of what's actually required, and from which test cases could be designed." (Prolifics Testing, "Ten Attributes of a Testable Requirement": https://www.prolifics-testing.com/news/ten-attributes-of-a-testable-requirement)

### 1.9 The Fundamental Tension: Enough Detail, Not Too Much

The design must be detailed enough to implement from and verify against, but not so detailed that it simply paraphrases the code. This tension has no algorithmic resolution — it requires judgment. However, the principle from Reeves and Fowler offers a heuristic:

**Document what cannot be recovered from code.** Do not document what competent code reading would provide.

What cannot be recovered:
- Why this algorithm was chosen over alternatives
- What external constraint forced this data structure
- What the valid state space of a parameter is (ranges, invariants)
- What happens when preconditions are violated
- What the expected performance envelope is and why
- What the module hides, and why that boundary was drawn

What should not be paraphrased:
- The step-by-step implementation logic (that is the code's job)
- Variable names and their types (visible in declarations)
- Control flow that is clear from structure

The iceoryx project's official documentation guidelines state this directly:
> "The implementation documentation should never describe *what* happens, that does already the code for you, but it should describe *why* it is implemented in the way it is."

Source: Eclipse iceoryx, `doc/aspice_swe3_4/swe_docu_guidelines.md`, main branch.
GitHub: https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md

---

## Part 2: Design Rationale and Decisions

### 2.1 What Rationale Is and Why It Matters

Design rationale is an explicit documentation of the reasons behind decisions made when designing a system. The Wikipedia definition — "an explicit documentation of the reasons behind decisions made when designing a system or artifact" — captures the core, but the deeper significance is stated by the WPI Design Rationale group:

> "Capturing the design rationale, the alternatives considered while designing, and the reasons for accepting or rejecting them, offers a richer view into both the product and the decision-making process."

Source: WPI Design Rationale Research Group: https://web.cs.wpi.edu/Research/aidg/DesignRationale.html

The practical impact: when a design decision is revisited — during maintenance, migration, or safety assessment — rationale documentation prevents two costly failure modes:

1. **Reinventing a rejected wheel**: an engineer proposes an alternative that was already evaluated and discarded
2. **Violating a hidden constraint**: a modification looks locally safe but violates an external constraint that drove the original choice

Without rationale, both errors are invisible until they cause damage. With rationale, they are preventable by reading a document.

### 2.2 Kruchten's Ontology: Design Decisions as First-Class Entities

Philippe Kruchten's 2004 paper "An Ontology of Architectural Design Decisions in Software-Intensive Systems" (2nd Groningen Workshop on Software Variability Management) makes the foundational argument: **architectural design decisions should be first-class entities** in software development, not implementation footnotes.

The paper presents a taxonomy of design decisions and argues that preserving the graph of decisions and their interdependencies supports system evolution and maintenance. The classification distinguishes between:

- **Existence decisions**: deciding that something will exist in the system (creating a component, an interface, a constraint)
- **Property decisions**: deciding the characteristics of something that already exists
- **Executive decisions**: deciding how things will be coordinated or managed

The key contribution for design documentation: decisions have **attributes and relationships**, not just outcomes. Documenting a decision means documenting its context, its alternatives, its rationale, and its connections to other decisions. An isolated decision record is incomplete.

Source: Kruchten, P. (2004). "An Ontology of Architectural Design Decisions in Software-Intensive Systems." 2nd Groningen Workshop on Software Variability Management, Groningen, NL.
Semantic Scholar: https://www.semanticscholar.org/paper/An-Ontology-of-Architectural-Design-Decisions-in-Kruchten/d8f4b86003517e0da16f9415eeac84f12df61f7d
Author's PDF: https://philippe.kruchten.com/wp-content/uploads/2009/07/kruchten-2004-design-decisions.pdf

### 2.3 Tyree and Akerman: The Template That Made ADRs Practical

Jeff Tyree and Art Akerman's 2005 paper "Architecture Decisions: Demystifying Architecture" (IEEE Software, Vol. 22, No. 2) introduced a practical template for documenting architectural decisions that remains widely referenced.

The paper argues that **explicitly documenting major architecture decisions makes the architecture development process more structured and transparent**, and clarifies the architects' rationale for stakeholders, designers, and other architects.

The Tyree-Akerman template is comprehensive, covering:
- **Problem**: the question being decided
- **Constraints**: what limits the solution space
- **Options**: the alternatives under consideration
- **Recommendation**: the chosen option
- **Justification**: why this option was chosen
- **Implications**: what changes as a consequence of this decision
- **Derived requirements**: requirements generated by this decision

For detailed design documentation, the Tyree-Akerman model offers the conceptual structure for documenting non-trivial design choices within a module: not just "we use a hash map" but the full reasoning chain from constraints through options to justification.

Source: Tyree, J. and Akerman, A. (2005). "Architecture Decisions: Demystifying Architecture." *IEEE Software*, 22(2).
IEEE Xplore: https://ieeexplore.ieee.org/document/1407822
ResearchGate: https://www.researchgate.net/publication/3248217_Architecture_Decisions_Demystifying_Architecture

### 2.4 Michael Nygard: Lightweight Decision Records for Everyday Use

Michael Nygard's 2011 blog post "Documenting Architecture Decisions" (Cognitect.com, November 15, 2011) introduced the lightweight ADR format that became the dominant practical approach. The format is intentionally minimal: **Title, Status, Context, Decision, Consequences**.

Nygard's rationale for the format:

> "An architecture decision record is a short text file in a format similar to an Alexandrian pattern, with each record describing a set of forces and a single decision in response to those forces."

The **Context** section is value-dense: it describes the forces at play — technological, organizational, temporal — that make the problem non-trivial. The **Consequences** section records what becomes easier, harder, or differently constrained as a result of the decision.

ADRs are kept in the project repository (typically `doc/arch/adr-NNN.md`), numbered sequentially, and are never deleted — deprecated or superseded ADRs are marked as such and linked to the replacement decision. This preserves the decision history rather than overwriting it.

**Adaptation to detailed design:** The ADR structure is directly applicable to unit-level design decisions that are not architectural but are non-trivial. Examples: choice of synchronization primitive, selection of error propagation strategy, choice of encoding for a protocol field, decision to use memoization. Each of these decisions has context, alternatives, justification, and consequences that belong in the design document.

Source: Nygard, M. (2011). "Documenting Architecture Decisions." Cognitect.com blog.
URL: https://www.cognitect.com/blog/2011/11/15/documenting-architecture-decisions
Martin Fowler's ADR note: https://martinfowler.com/bliki/ArchitectureDecisionRecord.html
ADR GitHub community: https://adr.github.io/

### 2.5 The iceoryx Principle: The Why, Never the What

The iceoryx project's documentation guidelines, maintained in `doc/aspice_swe3_4/swe_docu_guidelines.md`, state the rationale principle as a hard rule:

> "The implementation documentation should never describe what happens, that does already the code for you, but it should describe why it is implemented in the way it is."

This is more prescriptive than most academic formulations. It does not say "prefer rationale over description" — it prohibits code description entirely. The rationale: the code is already the authoritative statement of what happens. Duplicating it in prose creates two artifacts that will diverge under maintenance and generates zero additional value for any reader.

The iceoryx project targets ASPICE SWE.3/SWE.4 compliance (the `aspice_swe3_4` directory path is deliberate). This means the principle is applied in a real safety-critical automotive context, not as a theoretical ideal. The codebase is used in production autonomous driving and robotics systems.

Source: Eclipse iceoryx, `doc/aspice_swe3_4/swe_docu_guidelines.md`
GitHub: https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md

### 2.6 Burge and Brown: The Cost of Rationale Absence

Janet Burge and David C. Brown at WPI have conducted research on design rationale systems, focusing specifically on how rationale is *used* rather than merely captured. Their analysis establishes the costs of rationale absence in engineering:

- **Design verification** becomes impossible without rationale: an auditor cannot assess whether a design decision was appropriate without knowing the alternatives that were rejected
- **Conflict mitigation** in collaborative design fails because engineers cannot trace the origin of conflicting constraints
- **Knowledge transfer** between development and maintenance phases breaks: the maintainer encounters decisions that appear arbitrary and either reverses them (breaking constraints) or preserves them (losing the opportunity to improve)

Burge and Brown's work identifies a key property of useful rationale documentation: it must record **not just the decision made but the alternatives rejected and the reasons for rejection**. Without rejected alternatives, rationale is incomplete — a reader cannot evaluate whether the decision was appropriate without knowing what options were available.

Source: Burge, J. and Brown, D.C. "Reasoning with Design Rationale." *AI in Design* workshop proceedings.
Paper: http://web.cs.wpi.edu/~dcb/Papers/AID00-janet.pdf
WPI Design Rationale group: https://web.cs.wpi.edu/Research/aidg/DesignRationale.html

### 2.7 Design Rationale in Safety-Critical Contexts

In safety-critical domains, design rationale has normative weight beyond engineering best practice.

**DO-178C** (aviation software): The standard requires demonstration that software requirements are correctly implemented and that coverage is adequate. Safety assessments — including independent assessments by DERs (Designated Engineering Representatives) — routinely examine design rationale to evaluate whether a decision is appropriate for the assurance level. A decision that was reasonable at DAL D may be inadequate at DAL A. Without documented rationale and constraints, the assessor cannot evaluate this.

Source: DO-178C Wikipedia overview: https://en.wikipedia.org/wiki/DO-178C

**ASPICE SWE.3**: The process requires that the detailed design be "checked for relevant areas during evaluation" and that "verification should confirm that the design meets all requirements (direct and derived)." The distinction between direct and derived requirements is significant: derived requirements often originate from design decisions. Rationale documentation is the evidence that derived requirements are correctly traced back to their source decisions.

Source: Xenban ASPICE SWE.3: https://xenban.com/index.php/2025/02/12/automotive-spice-swe-3-software-detailed-design-and-unit-construction/

The "Psychology of Architecture Decision Records" (IEEE Software, 2022) discusses how ADRs reduce cognitive overhead in long-lived projects and safety-critical contexts specifically.
Source: https://ieeexplore.ieee.org/document/9928205

### 2.8 Constraint Documentation: The Invisible Driver

A decision cannot be fully understood without documenting the constraints that limited the solution space. Constraints fall into several categories that design documentation must distinguish:

**External constraints**: standards mandates, platform limitations, certification requirements, customer contracts. Example: "MISRA-C compliance required; dynamic memory allocation after initialization is prohibited."

**Architectural constraints**: decisions made at a higher layer that propagate downward. Example: "The system uses a single-threaded event loop; blocking operations are architecturally prohibited in this module."

**Resource constraints**: timing budgets, memory limits, stack depth limits. Example: "This function is called from interrupt context; stack usage is bounded to 256 bytes."

**Temporal constraints**: design decisions that were correct at the time but may not be revisited easily. Example: "Chosen to match the interface of the legacy system; the legacy system will be decommissioned in Phase 4 — this constraint will then be lifted."

Without explicit constraint documentation, the next engineer who touches the design has no way to distinguish "this design is bad and should be changed" from "this design is forced by an external constraint and cannot be changed." Both look identical in code.

This four-category constraint taxonomy is derived from synthesis of Tyree-Akerman's template constraints section and Nygard's context section, with the temporal category added from practical safety-critical experience. No single paper enumerates these four exactly; the synthesis is the author's.

### 2.9 The Information Content Stack: What Each Layer Provides

A useful organizing principle for understanding what detailed design documentation must add:

```
Requirements layer
  └── What the system must do (behavior, quality attributes, constraints)

Architecture layer
  └── How the system is divided (components, connections, deployment)
  └── What each major component's responsibility is

Detailed design layer           <-- This document's scope
  └── How each unit is internally structured
  └── What each unit hides and why
  └── The design decisions within a unit and their rationale
  └── Preconditions, postconditions, invariants
  └── Error handling strategy and error propagation
  └── Concurrency model and synchronization approach
  └── Performance targets and design choices that serve them

Code layer
  └── The exact implementation of each unit
  └── All control flow, data structures, algorithms
```

The detailed design layer occupies the space between architecture (what components exist) and code (exactly how they work). Its unique contribution is: **the design decisions within units, and the reasons those decisions were made**. This is the layer that explains what code cannot — not because the code is unclear, but because design intent is categorically absent from executable statements.

This information content stack is a synthesis of Parnas and Clements (1986), Clements et al. *Documenting Software Architectures* (SEI Series, 2nd ed., 2010), and Reeves (1992). The stack structure is the author's synthesis.

Source for *Documenting Software Architectures*:
SEI: https://sei.cmu.edu/library/documenting-software-architectures-views-and-beyond-second-edition/
Amazon: https://www.amazon.com/Documenting-Software-Architectures-Views-Beyond/dp/0321552687

---

## Part 3: Identified Gaps and Open Questions

### 3.1 Empirical Evidence for Design-Before-Code

The claim that design-before-code reduces defects is widely asserted but empirical data is limited. Kemerer and Paulk's data (cited in Pitt studies on PSP) suggests design review effort correlates with defect reduction, but this is at the process level rather than the design document content level.

The search did not surface a controlled study directly measuring the impact of detailed design documentation quality on downstream test effectiveness or defect detection. This is likely a genuine gap in the literature. Design-before-code is advocated primarily on principled grounds (Parnas, Reeves, IEEE 1016) and on audit/compliance grounds (DO-178C, ASPICE), not on directly quantified empirical grounds.

Source: Kemerer and Paulk, impact of design reviews on software quality:
https://sites.pitt.edu/~ckemerer/PSP_Data.pdf

### 3.2 The "How Detailed Is Detailed?" Question

Neither Parnas-Clements, IEEE 1016, nor the ADR literature gives a precise answer to "how much detail is enough in a detailed design?" The closest formulation is the test-derivability criterion (Section 1.8 above) and the Reeves/iceoryx "document the why, not the what" principle (Section 1.9 / 2.5). In practice, the threshold is context-dependent:

- Safety-critical software (DAL A/B aviation, ASIL C/D automotive): the threshold is higher because independent verifiers must be able to audit the design without reading the code
- Commercial software with high test coverage: the threshold may be lower because tests themselves serve as partial specification

The detailed design documentation page should acknowledge this context-dependence rather than prescribing a single level of detail.

### 3.3 ADR Adaptation to Unit-Level Design

The ADR format was designed for architectural decisions (system-wide, long-lived, expensive to reverse). Its adaptation to unit-level detailed design decisions is a matter of calibration, not principle. Small decisions within a module (choice of data structure, loop termination strategy) do not warrant full ADR entries. A decision record is appropriate when:

- The decision is non-obvious to a competent reader of the code
- The decision was constrained by factors not visible in the code
- The decision was made in preference to a plausible alternative
- The decision has implications that a future maintainer must understand before modifying

No published paper sets this threshold precisely. The threshold is the author's synthesis.

---

## Sources

### Primary: Peer-Reviewed Papers and Standards

| Source | Reference | URL |
|--------|-----------|-----|
| Parnas & Clements (1986) | "A Rational Design Process: How and Why to Fake It." IEEE Trans. SE, 12(2), 251–257 | https://ieeexplore.ieee.org/document/6312940 |
| Parnas (1972) | "On the Criteria To Be Used in Decomposing Systems into Modules." CACM, 15(12), 1053–1058 | https://dl.acm.org/doi/10.1145/361598.361623 |
| Kruchten (2004) | "An Ontology of Architectural Design Decisions." 2nd Groningen Workshop on Software Variability Management | https://philippe.kruchten.com/wp-content/uploads/2009/07/kruchten-2004-design-decisions.pdf |
| Tyree & Akerman (2005) | "Architecture Decisions: Demystifying Architecture." IEEE Software, 22(2) | https://ieeexplore.ieee.org/document/1407822 |
| Reeves (1992) | "What Is Software Design?" C++ Journal, Fall 1992 | https://www.developerdotstar.com/mag/articles/reeves_design.html |
| IEEE Std 1016-2009 | IEEE Standard for Information Technology — Software Design Descriptions | https://ieeexplore.ieee.org/document/5167255 |
| "Psychology of ADRs" (2022) | IEEE Software | https://ieeexplore.ieee.org/document/9928205 |

### Primary: Books

| Source | Reference | URL |
|--------|-----------|-----|
| Clements et al. | *Documenting Software Architectures: Views and Beyond*, 2nd ed. SEI Series. Addison-Wesley, 2010 | https://sei.cmu.edu/library/documenting-software-architectures-views-and-beyond-second-edition/ |
| Parnas & Britton (1981) | *A-7E Software Module Guide*. NRL Memorandum Report 4702 | https://apps.dtic.mil/docs/citations/ADA108649 |

### Primary: Industry Standards

| Source | Reference | URL |
|--------|-----------|-----|
| DO-178C | Software Considerations in Airborne Systems and Equipment Certification (RTCA, 2011) | https://en.wikipedia.org/wiki/DO-178C |
| ASPICE SWE.3 | Automotive SPICE Process Reference Model, Software Detailed Design and Unit Construction | https://www.ul.com/sis/resources/process-swe-3 |

### Primary: Project Documentation (Real Systems)

| Source | Reference | URL |
|--------|-----------|-----|
| iceoryx (2021+) | `doc/aspice_swe3_4/swe_docu_guidelines.md`, Eclipse iceoryx project | https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md |
| Nygard (2011) | "Documenting Architecture Decisions." Cognitect blog | https://www.cognitect.com/blog/2011/11/15/documenting-architecture-decisions |

### Secondary: Research Surveys

| Source | Reference | URL |
|--------|-----------|-----|
| Burge & Brown | "Reasoning with Design Rationale." AI in Design proceedings | http://web.cs.wpi.edu/~dcb/Papers/AID00-janet.pdf |
| WPI Design Rationale Group | Survey of design rationale systems and approaches | https://web.cs.wpi.edu/Research/aidg/DesignRationale.html |

### Secondary: Web Sources (Referenced for Specific Claims)

| Source | Reference | URL |
|--------|-----------|-----|
| Fowler (2005+) | "Code As Documentation." martinfowler.com | https://martinfowler.com/bliki/CodeAsDocumentation.html |
| Fowler (2020) | "Architecture Decision Record." martinfowler.com | https://martinfowler.com/bliki/ArchitectureDecisionRecord.html |
| ADR community | ADR templates and tooling | https://adr.github.io/ |
| Prolifics Testing | "Ten Attributes of a Testable Requirement" | https://www.prolifics-testing.com/news/ten-attributes-of-a-testable-requirement |

---

## Notes on Research Limitations

1. **IEEE 1016 full text not accessed**: The standard is paywalled. The descriptions above are drawn from the standard's own abstract and scope documents on IEEE Xplore, the 1998 revision available at https://people.eecs.ku.edu/~saiedian/Teaching/Stds/1016.pdf, and Wikipedia's SDD article (https://en.wikipedia.org/wiki/Software_design_description). Claims about the standard's content are limited to what these sources state.

2. **DO-178C full text not accessed**: The standard is paywalled. Claims about DO-178C are drawn from recognized secondary sources (Rapita Systems, Parasoft, Vector) and the standard's Wikipedia article.

3. **Section 2.8 constraint taxonomy**: The four-category constraint taxonomy is a synthesis, not a direct citation from any single paper. It is labeled as such in the text.

4. **Section 2.9 information content stack**: The stack is a synthesis of multiple sources, not a verbatim citation. It is labeled as such.

5. **Tyree-Akerman paper year**: The paper is dated 2005 in the IEEE record (Vol. 22, No. 2 is the March/April 2005 issue), not 2004 as sometimes cited in secondary literature.
