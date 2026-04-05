# Research 08: Detailed Design — What V-Model Standards Actually Require vs. What's Realistic

## Research Question

Detailed design is required by every major V-model standard. But what does "detailed design" actually mean in practice? A 300k-line codebase cannot have 300k lines of design documentation that simply paraphrases the code. What do standards *actually* expect, what do assessors *actually* look for, and how does our current schema measure up?

---

## 1. Standard-by-Standard Requirements

### 1.1 DO-178C (Aviation Software)

DO-178C distinguishes **High-Level Requirements (HLR)** from **Low-Level Requirements (LLR)**:

- **HLR**: Specify *what* the software must do, derived from system requirements. Architecture-independent.
- **LLR**: Specify *how* the architecture implements functions, at sufficient detail that "source code can be directly implemented without further information." Must include algorithm specifics, data structures, I/O descriptions, error handling, and timing.

The **Software Design Description (SDD)** is the primary artifact. DO-178C Section 11 requires it to be unambiguous, complete, verifiable, consistent, modifiable, and traceable. Bidirectional traceability is mandatory: System Req → HLR → LLR → Source Code.

**Can code be the LLR?** The general advice is **no**. AdaCore's compliance guidance states: "the general advice is to avoid using a programming language as the medium for expressing -- even in part -- the software design" and "Requirements are properties to be verified by the code and are not the code itself." Exception: Ada's specification/body separation can serve design documentation purposes. Model-based development (SCADE, DO-331) is another path where the model IS the design.

DO-178C defines **31 review/analysis objectives** (sections 6.3, 6.4.5). For DAL A/B, 16 of 31 require independent review, including LLR verification.

**Sources:**
- AdaCore DO-178C Compliance Analysis: https://learn.adacore.com/booklets/adacore-technologies-for-airborne-software/chapters/analysis.html (tool vendor, authored by DO-178C domain experts)
- AdaCore Blog — DO-178C Reviews: https://www.adacore.com/blog/a-fresh-take-on-do-178c-software-reviews (practitioner)
- Rapita Systems DO-178C Guide: https://www.rapitasystems.com/do178 (verification tool vendor)
- Leanna Rierson, "Developing Safety-Critical Software" (CRC Press, 2013) — co-author of DO-178C

### 1.2 DO-330 (Tool Qualification)

DO-330 mirrors DO-178C's process structure but applies it to tools. Five Tool Qualification Levels (TQL-1 through TQL-5):

| Criterion | DAL A | DAL B | DAL C | DAL D |
|-----------|-------|-------|-------|-------|
| Tool output is airborne SW | TQL-1 | TQL-2 | TQL-3 | TQL-4 |
| Eliminates/reduces verification | TQL-4 | TQL-4 | TQL-4 | TQL-5 |
| Could fail to detect errors | TQL-5 | TQL-5 | TQL-5 | TQL-5 |

TQL-1 approaches DAL A rigor. The Tool Development Process requires HLR, LLR, and code with full traceability — same structure as DO-178C. Key artifacts include Tool Qualification Plan (TQP), Tool Operational Requirements (TOR), Tool Verification Plan (TVP), and Tool Accomplishment Summary (TAS).

**Sources:**
- LDRA DO-330 Overview: https://ldra.com/do-330/ (verification tool vendor)
- AFuzion DO-330 Introduction: https://afuzion.com/do-330-introduction-tool-qualification/ (certification consultancy)

### 1.3 ASPICE SWE.3 (Automotive)

SWE.3 (Software Detailed Design and Unit Construction) defines 8 base practices:

- **BP.1**: Develop detailed design for each software component
- **BP.2**: Identify, specify, document interfaces of each software unit — names, types, units, resolutions, ranges, default values. Quote: "without this information, proper testing of the interfaces in the unit test is impossible"
- **BP.3**: Evaluate and detail dynamic behavior of relevant units (not ALL units — only where relevant)
- **BP.4**: Evaluate design for interoperability, criticality, testability
- **BP.5**: Establish bidirectional traceability (requirements ↔ units, architecture ↔ detailed design)
- **BP.6**: Ensure consistency between all design levels
- **BP.7**: Communicate design to stakeholders
- **BP.8**: Develop code per the detailed design

**Critical practical insight from UL Solutions (ASPICE assessment body):**

1. "The higher the coverage goal, the more detail is required by the detailed design." For branch coverage, design must identify branches. Statement coverage requires less detail.
2. "If you write the detailed design after documenting your code, the point of the unit test is lost." Design must exist BEFORE code.
3. Using source code markup as design documentation is listed as a **common assessment failure**.

**Practitioner example — Eclipse iceoryx** (open-source, ASPICE-compliant):
- Documentation lives in **header files only** (interfaces, not implementation)
- "Implementation documentation should never describe what happens, that does already the code for you. It should describe **why** it is implemented in the way it is."
- Doxygen tags map to SWE.3 concerns: `@brief`/`@details`, `@param`/`@return`, `@req` for traceability, `@concurrent` for threading, `@startuml` for dynamic behavior
- They explicitly FORBID documenting things the type system already captures

**Sources:**
- UL Solutions SWE.3 Guide: https://www.ul.com/sis/resources/process-swe-3 (ASPICE assessment body — highly authoritative)
- UL Solutions SWE.3 Insights: https://www.ul.com/sis/insights/software-detailed-design-and-unit-construction-swe3-automotive-spice (same body)
- Eclipse iceoryx SWE.3 Guidelines: https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md (open-source practitioner)
- Polarion SWE.3 Blog: https://polarion.code.blog/2022/04/21/swe-3-software-detailed-design-and-unit-construction/ (tool vendor)

### 1.4 ISO 26262 Part 6 (Automotive Functional Safety)

Clause 8 covers Software Unit Design and Implementation. Uses ASIL-dependent method recommendations:

| Method | ASIL A | ASIL B | ASIL C | ASIL D |
|--------|--------|--------|--------|--------|
| Natural language | + | + | + | + |
| Semi-formal notation | + | + | ++ | ++ |
| Formal notation | o | o | + | ++ |

Legend: ++ highly recommended, + recommended, o no recommendation

For ASIL C/D: semi-formal or formal notation (UML, state machines, formal specs) highly recommended. Plain natural language alone is insufficient at high ASIL levels. Design must address simplicity, comprehensibility, consistency, modularity, verifiability, encapsulation, and abstraction.

**2018 edition changes:** ASIL B semi-formal downgraded from ++ to +; ASIL C design principles upgraded from + to ++.

**Sources:**
- ISO 26262 Wikipedia: https://en.wikipedia.org/wiki/ISO_26262
- ISO 26262 2011 vs 2018 comparison: https://www.linkedin.com/pulse/functional-safety-iso-26262-ver2011-vs-ver2018-part-bao-nguyen (practitioner)

### 1.5 IEC 62304 (Medical Device Software)

**Detailed design is required ONLY for Class C** (risk of death or serious injury):

| Process | Class A | Class B | Class C |
|---------|---------|---------|---------|
| Software detailed design | — | — | **Required** |
| Software unit verification | — | Required | Required |

For Class C, section 5.4 requires design with enough detail for correct implementation and documented interfaces between units. Less prescriptive about format than DO-178C or ASPICE. Amendment 1 (2015) shifted to risk-based classification but detailed design remains Class C only.

**Sources:**
- IEC 62304 Wikipedia: https://en.wikipedia.org/wiki/IEC_62304
- Johner Institute Safety Classes: https://blog.johner-institute.com/iec-62304-medical-software/safety-class-iec-62304/ (medical device consultancy)

---

## 2. Cross-Cutting Findings: What "Detailed Design" Actually Means

### 2.1 What Assessors Actually Look For

Every standard emphasizes these — NOT line-by-line prose mirroring the code:

1. **Traceability chains, not prose.** The primary audit artifact is the traceability matrix: requirements → architecture → detailed design → code → tests. Assessors check coverage percentages and focus on gaps.

2. **Interfaces, not internals.** Every standard emphasizes interface documentation — names, types, units, ranges, constraints, pre/postconditions. The API contract matters; the internal implementation detail is secondary.

3. **Design rationale ("why"), not code description ("what").** The iceoryx principle: "implementation documentation should never describe what happens, that does already the code for you. It should describe why."

4. **Design BEFORE code.** The UL Solutions guidance: if you write design after code, the unit test is pointless. This is the single most common SWE.3 failure mode.

5. **Detail scales with criticality and coverage goals.** Branch coverage (ASIL B+) requires design that identifies branches. Statement coverage needs less. DAL A/B requires independent review. IEC 62304 Class A/B don't need detailed design at all.

### 2.2 The Fundamental Tension

The core issue: **design documents must add information that the code alone does not provide**, otherwise they're just maintenance burden. Design adds value when it captures:

- **Intent** — Why this approach, not just what it does
- **Constraints** — What the unit must NOT do, performance/safety bounds
- **Decision rationale** — Why this algorithm over alternatives
- **Expected behavior under edge cases** — Explicitly enumerated, not left to interpretation
- **Interface contracts** — Pre/postconditions, valid ranges, error semantics

Design does NOT add value when it:
- Paraphrases function signatures already visible in code
- Describes step-by-step what the code does (the code does that)
- Duplicates information already in the type system
- Restates comments already in the source

### 2.3 Realistic Approaches That Pass Assessment

**Approach A: Separate Design Documents per Component** (traditional)
- One design document per architectural component (not per file/function)
- Content: purpose, interfaces, key algorithms, state machines, constraints, error strategy
- Detail: enough to derive tests from, not enough to re-implement from
- Best for: DO-178C, high-ASIL ISO 26262, high-integrity projects
- Scale: ~10-20% of code volume in design documentation, weighted toward complex/critical units

**Approach B: Interface Documentation in Code** (header-file model)
- Document public APIs in header files / interface definitions
- Structured annotations (Doxygen, Javadoc) mapping to standard concerns
- Generate traceability matrices from annotations
- Works well for: ASPICE SWE.3 (proven by iceoryx)
- **Caveat**: SWE.3 assessors flag "source code markup as design" as a failure if it merely describes what code does rather than serving as a contract that existed before the code

**Approach C: Model-Based** (most rigorous)
- Model IS the design (SCADE, Simulink)
- Auto-generated code eliminates LLR-to-code gap
- Supported by DO-331 (Model-Based Development supplement)
- Most expensive, most rigorous

**Approach D: Risk/Criticality-Based Tiering** (pragmatic)
- Full detailed design for safety-critical, high-complexity, frequently-changed modules
- Lighter documentation for well-understood, low-risk, stable modules
- Explicitly supported by ISO 26262 (ASIL-dependent) and IEC 62304 (class-dependent)
- NOT explicitly supported by DO-178C (uniform treatment per DAL) or ASPICE (SWE.3 covers all components)

### 2.4 Anti-Patterns

1. **Design-as-code-paraphrase**: "This function takes an integer and returns a string" when the code shows `String convert(int x)`. Zero value, 100% maintenance burden.
2. **Post-hoc design**: Writing design after code to satisfy audit. Assessors can tell. Defeats verification purpose.
3. **Over-documentation of trivial code**: A getter/setter needs no design document. Focus where decisions were made.
4. **Under-documentation of complex/critical code**: State machines, concurrency, error recovery — these NEED design docs because code alone doesn't convey intent.
5. **Stale design docs**: Design that drifts from code creates false confidence. Keep close to code or automate sync checking.

---

## 3. Gap Analysis: VModelWorkflow's Current Schema vs. Standards

### 3.1 What the Schema Gets Right

Our `detailed-design.schema.yaml` already captures the core of what standards require:

| Standard Need | Schema Coverage | Assessment |
|---------------|-----------------|------------|
| Unit identification | `unit_id`, `component` | **Strong** |
| Interfaces with types, units, constraints | `interfaces.inputs/outputs` with type, unit, constraints, values | **Strong** — matches ASPICE BP.2 exactly |
| Behavioral specification | `behavior` with condition/result/step patterns | **Strong** |
| Error handling | `error_handling` with condition/behavior pairs | **Strong** |
| State documentation | `internal_state` (optional, stateful units only) | **Good** |
| Configuration | `configuration` with defaults | **Good** |
| Non-functional constraints | `constraints` list | **Good** |
| Code/test traceability | `realization` with source_files, test_files | **Present** (light touch, defers to Pillar 2) |

### 3.2 Gaps Identified

#### Gap 1: No Explicit "Level of Detail" Guidance

**Problem:** The schema defines structure but gives no guidance on WHEN a unit needs a detailed design and HOW MUCH detail is appropriate. Standards are clear that detail scales with criticality and coverage goals. A trivial utility function and a safety-critical state machine should not get the same treatment.

**Standards say:**
- ASPICE: "The higher the coverage goal, the more detail is required"
- ISO 26262: ASIL-dependent notation requirements (natural language OK for ASIL A, semi-formal/formal needed for C/D)
- IEC 62304: Detailed design only required for Class C
- DO-178C: Uniform per DAL, but LLR detail varies based on complexity

**Impact:** Without guidance, users will either over-document trivial units (waste) or under-document critical ones (compliance risk).

#### Gap 2: No Design Rationale / Decision Records

**Problem:** The schema captures WHAT the design is, but has no structured place for WHY. The `notes` field is free-form and optional. Every standard emphasizes rationale as what separates design from code description. The iceoryx principle: "describe why it is implemented in the way it is."

**What's missing:**
- Why this algorithm over alternatives?
- Why these interface boundaries?
- What design trade-offs were considered?
- What constraints drove the design decisions?

#### Gap 3: No Semi-Formal / Formal Notation Support

**Problem:** ISO 26262 highly recommends semi-formal notation for ASIL C/D (UML, state machines). DO-178C values formal specifications for DAL A. Our schema is entirely natural-language structured YAML. There's no way to embed or reference:
- State machine diagrams / definitions
- Sequence diagrams for dynamic behavior
- Decision tables
- Formal pre/postconditions

**Standards say:**
- ISO 26262: Semi-formal ++ for ASIL C/D, formal ++ for ASIL D
- ASPICE SWE.3 BP.3: "Evaluate and detail dynamic behavior" — implies state/sequence diagrams
- DO-178C: Formal methods via DO-333 supplement

#### Gap 4: No Scaling Strategy for Large Codebases

**Problem:** The schema treats every unit as equal — same structure, same fields. For a 300k-line codebase with potentially hundreds of units, there's no strategy for:
- Which units need full detailed designs?
- Which can be adequately covered by interface documentation alone?
- How to handle groups of similar units (e.g., 50 DTO classes)?
- How to aggregate trivial units into component-level designs?

#### Gap 5: No Explicit "Design Before Code" Workflow Support

**Problem:** The UL Solutions guidance identifies post-hoc design as the #1 SWE.3 failure. Our schema doesn't enforce or even indicate design-before-code ordering. The `realization` field (pointing to source/test files) could be filled in at any time. There's no `status` in the body to track the design lifecycle (draft → approved → implemented → verified).

**Note:** The artifact envelope has `status`, but the design body itself doesn't capture the temporal relationship between design and code creation.

#### Gap 6: No "Negative Requirements" / Boundary Specification

**Problem:** Standards emphasize what the unit must NOT do as much as what it must do. Our `responsibilities` field captures positive responsibilities. There's no structured place for:
- Explicit exclusions ("this unit does NOT handle authentication")
- Resource limits ("must not allocate more than 1KB")
- Timing guarantees ("must complete within 10ms")

The `constraints` field partially covers this but is a flat string list. Timing, memory, and safety constraints benefit from structure.

#### Gap 7: No Concurrency / Thread-Safety Model

**Problem:** ASPICE SWE.3 BP.3 specifically calls out dynamic behavior. DO-178C requires partitioning and synchronization documentation. Our schema has `constraints` as a flat list where "thread-safe" can be mentioned, but there's no structured way to describe:
- Thread safety guarantees
- Synchronization mechanisms
- Shared state access patterns
- Reentrance properties

---

## 4. Ways Forward

### Option A: Tiered Design Templates (Recommended)

Add a **design tier** concept that maps to criticality/complexity:

```
Tier 1 — Full Design (safety-critical, complex algorithms, state machines)
  → All current schema fields
  → Plus: rationale section, semi-formal notation references, concurrency model
  → One artifact per unit

Tier 2 — Interface Design (moderate complexity, well-understood patterns)
  → Interfaces (full detail), responsibilities, error handling, constraints
  → Behavior section: high-level description, not step-by-step
  → One artifact per unit or small group of related units

Tier 3 — Component-Level Design Summary (simple/trivial units, DTOs, utilities)
  → One artifact per component covering all simple units
  → Interface listing with types and constraints
  → No per-unit behavior specification (code is self-documenting at this level)
```

**Why this works:** It directly addresses the scaling problem. Safety-critical units get full treatment. Trivial units get proportional effort. Assessors see intentional, justified tiering rather than uniform over/under-documentation.

**Mapping to standards:**
- DO-178C: Tier 1 for all units at DAL A/B, Tier 2 for DAL C/D
- ASPICE: Tier 1 for complex + safety-critical, Tier 2 for standard, Tier 3 for trivial
- ISO 26262: Tier 1 for ASIL C/D, Tier 2 for ASIL A/B, Tier 3 for QM
- IEC 62304: Tier 1 for Class C, Tier 2 optional for Class B, none for Class A

### Option B: Extend Current Schema with Optional Sections

Keep the single schema but add optional sections that enable richer documentation when needed:

```yaml
# New optional sections:

design_rationale:
  type: list<object>
  description: "Why this design, not just what it is"
  item_fields:
    decision: { type: string }
    alternatives_considered: { type: list<string> }
    justification: { type: string }

dynamic_behavior:
  type: list<object>
  description: "State machines, sequences, timing for complex units"
  item_fields:
    name: { type: string }
    type: { type: string }  # state_machine, sequence, timing
    notation: { type: string }  # plantuml, mermaid, table, natural_language
    definition: { type: string }

concurrency:
  type: object
  description: "Thread-safety model (omit for single-threaded units)"
  fields:
    thread_safety: { type: string }  # none, immutable, synchronized, lock-free
    shared_state: { type: list<string> }
    synchronization: { type: string }

negative_requirements:
  type: list<string>
  description: "What this unit explicitly must NOT do"
```

**Why this works:** Backward-compatible with existing designs. Teams add richness as needed. Doesn't force heavy documentation on simple units.

**Limitation:** Doesn't solve the fundamental scaling problem — every unit still gets its own artifact file.

### Option C: Dual-Mode Design (Separate Artifacts + Code Annotations)

Support two complementary modes:

1. **Standalone YAML design artifacts** (current approach) — for designs that must exist before code, for safety-critical units, for formal review artifacts.
2. **Code-embedded design annotations** — for interface contracts, traceability tags, rationale comments. Define a mapping standard (like iceoryx's Doxygen approach) where structured code annotations are recognized as design evidence.

The traceability engine (Pillar 2) would recognize both modes and validate coverage regardless of which mode was used.

**Why this works:** Reduces documentation that drifts from code. Enables the iceoryx-style approach that has proven viable for ASPICE. Keeps standalone artifacts where standards demand independence from code.

**Risk:** Code-annotation mode could become "design after code" anti-pattern if not disciplined.

### Option D: Component-Level Design Documents with Unit Details

Shift the primary artifact from per-unit to per-component:

```
Component Design Document:
├── Component purpose, interfaces, responsibilities
├── Unit inventory (which units, brief description each)
├── Shared patterns (error strategy, threading model, common constraints)
├── Detailed unit specs (only for complex/critical units)
│   ├── Unit A: full behavior, interfaces, error handling
│   └── Unit B: full behavior, interfaces, error handling
└── Simple units: interface table only (name, inputs, outputs, constraints)
```

**Why this works:** Natural grouping reduces file count. One document for a component with 20 units is more navigable than 20 separate files. Matches how developers think about systems.

**Limitation:** Larger documents are harder to version-control and review incrementally.

---

## 5. Recommendation

**Combine Options A and B: Tiered design with enriched schema.**

1. Introduce three design tiers mapped to criticality/complexity
2. Add optional schema sections (rationale, dynamic behavior, concurrency, negative requirements)
3. Provide clear guidance on which tier applies when, per standard
4. Keep the per-unit artifact model for Tier 1/2 (these are the units that matter)
5. Allow component-level aggregation for Tier 3 (trivial units)
6. Add documentation/best-practices for "design before code" discipline

This approach:
- Solves the 300k-line scaling problem
- Remains compliant across all target standards
- Preserves backward compatibility with existing examples
- Gives humans AND agents clear guidance on appropriate effort
- Maps naturally to the assurance_level concept already in the artifact envelope

---

## Appendix: Sources by Category

### Official / Highly Authoritative
- UL Solutions (ASPICE assessment body) — SWE.3 Guide and Insights articles
- Leanna Rierson, "Developing Safety-Critical Software" — DO-178C co-author
- AdaCore DO-178C Compliance Analysis — authored by domain experts
- Johner Institute — IEC 62304 safety class guidance

### Practitioner / Real-World Examples
- Eclipse iceoryx — open-source ASPICE-compliant project with SWE.3 documentation guidelines
- AdaCore Blog — fresh perspectives on DO-178C reviews
- Polarion SWE.3 Blog — tool vendor with practitioner focus

### Tool Vendors / Consultancies
- Rapita Systems — DO-178C and DO-330 guides
- LDRA — DO-330 overview
- AFuzion — DO-330 tool qualification introduction
- Parasoft — DO-178C and ISO 26262 overviews
- EE-Aero — aviation engineering glossary

### Community / General Reference
- Wikipedia articles for ISO 26262 and IEC 62304 (summary tables)
- LinkedIn practitioner articles comparing ISO 26262 editions
