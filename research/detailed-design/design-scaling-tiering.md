# Research: Design Scaling and Tiering Strategies

**Research Question:** How do you decide what needs a design document, and how much detail is appropriate? What frameworks, metrics, and precedents exist for scaling design documentation effort proportionally to risk, complexity, and value?

**Feeds into:** Section 3.6 (Scaling: What Needs a Design and How Much Detail) of the detailed design HTML documentation page.

**Related:** `research/08-detailed-design-realism-and-compliance.md` (standard-by-standard requirements and tiered template proposal)

---

## 1. The Economics of Design Documentation

### 1.1 The Cost Reality

Design documentation costs real money. Capers Jones' large-scale software productivity research — documented in *Estimating Software Costs* (McGraw-Hill, 2007) and *Applied Software Measurement* (McGraw-Hill, 2008) — found that on defense and aerospace projects, the cost of producing paper documents can be **twice the cost of writing the code itself**. This is not an anomaly; it reflects the compounding cost of specification, review, update, and configuration management applied to every documented artifact.

The academic literature confirms the cost picture but is thin on hard numbers. Zhi, Garousi-Yusifoğlu et al., "Cost, benefits and quality of software development documentation: A systematic mapping" (*Journal of Systems and Software*, Vol. 99, 2015, pp. 175–198) reviewed 69 empirical papers from 1971–2011. Key findings:

- Only **12 of 69 papers** (17%) addressed documentation cost at all.
- Only **6 papers** (8%) discussed production or development cost specifically.
- The authors concluded: "documentation cost aspect seems to have been neglected in the existing literature and there are no systematic methods or models to measure cost."
- The most-studied quality attributes were completeness, consistency, and accessibility — not cost-effectiveness.

**Implication for practitioners:** There is no reliable published model to calculate documentation ROI per unit. Decision frameworks must rely on engineering judgment guided by proxies (complexity, criticality, change rate) rather than precise cost models.

**Source:** Zhi et al. (2015) — https://www.sciencedirect.com/science/article/abs/pii/S0164121214002131

### 1.2 When Documentation Pays for Itself

Design documentation generates measurable return in these scenarios:

**Bug prevention during initial development.** When detailed design exists before coding, defects are caught at design review (cheapest fix point) rather than integration test or field. The cost-to-fix ratio — introduced by Barry Boehm's empirical work in *Software Engineering Economics* (Prentice Hall, 1981) and repeatedly confirmed — shows defects found in design cost 1–5x to fix vs. 100x+ if found in production. However, the benefit accrues only when the design is created first and actually reviewed; post-hoc design documentation captures none of this benefit.

**Onboarding and knowledge transfer.** On long-lived codebases, design docs reduce the time new engineers need to reach productivity. This is particularly acute for safety-critical embedded systems where the cost of a new engineer misunderstanding a critical module is high.

**Compliance and certification.** For standards-governed products (DO-178C, ASPICE, IEC 62304 Class C), design documentation is not optional — it is a prerequisite for certification. In these contexts, the ROI question is moot: the cost is a regulatory cost of doing business.

**High-complexity algorithms.** State machines, concurrency primitives, and safety-critical algorithms are too complex to verify from code alone. Design documentation here serves as the verification reference — without it, test designers cannot derive correct coverage.

### 1.3 When Documentation Does Not Pay

Design documentation is negative-value in these scenarios:

**Trivial code.** A getter, setter, DTO (data transfer object), or simple utility function adds nothing when documented beyond its type signature. The code is already self-documenting. Documenting it creates maintenance burden without adding verification value.

**Rapidly changing prototypes.** Documentation written during exploratory development goes stale before it can be used. The maintenance tax exceeds the benefit. The rule: do not document until design has stabilized.

**Throwaway code.** If the code will be discarded after a short trial period, documentation is waste. (Caution: "throwaway" code that survives longer than planned is a recognized source of technical debt.)

**Code already covered by comprehensive tests.** When exhaustive test coverage exists and the tests are readable, they serve as executable specifications. The value of additional prose design documentation is marginal.

### 1.4 The Maintenance Tax: Stale Documentation Is Worse Than None

This principle is established across multiple authoritative sources and is not contested.

The Eclipse iceoryx project's documented guidelines (https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md) state it explicitly: "Implementation documentation should never describe what happens, that does already the code for you. It should describe **why** it is implemented in the way it is."

A Stripe-commissioned survey (2019, reported by Stripe) found developers spend up to 17 hours per week dealing with technical debt and maintenance issues, with poor or stale documentation identified as a primary contributor. This translates to approximately $85 billion annually in lost global software productivity.

The academic framing is "documentation debt" — a recognized subcategory of technical debt. A paper on documentation technical debt by Rios et al., "Documentation Technical Debt" (*XXXIII Brazilian Symposium on Software Engineering*, ACM DL, 2019, DOI: 10.1145/3350768.3350773) catalogs stale documentation as a first-class technical debt type with real maintenance cost consequences.

**Key principle:** A design document that drifts from the implementation it describes creates false confidence. An engineer or auditor reading it believes they understand the system when they do not. This is not neutral — it is actively harmful. Documentation that is not maintained should be removed or explicitly deprecated, not left to mislead.

---

## 2. Risk-Based and Criticality-Based Tiering

### 2.1 Standards That Explicitly Scale Rigor with Criticality

The strongest explicit support for tiering comes from standards that define risk classes:

**IEC 62304 (Medical Device Software)** is the clearest example of formal tiering. Section 5.4 of the standard requires detailed software design **only for Class C software** (software where a failure could cause death or serious injury). Class A (no injury possible) and Class B (non-serious injury possible) do not require detailed design at all.

| Class | Risk | Detailed Design Required |
|-------|------|--------------------------|
| A | No injury possible | No |
| B | Non-serious injury possible | No |
| C | Death or serious injury possible | Yes — Section 5.4 |

Source: Johner Institute IEC 62304 Safety Class Guide — https://blog.johner-institute.com/iec-62304-medical-software/safety-class-iec-62304/; Greenlight Guru IEC 62304 Classifications — https://www.greenlight.guru/glossary/iec-62304

This is not a pragmatic concession — it is the standard's explicit position. IEC 62304 formally recognizes that not all software warrants the same documentation investment, and that the deciding criterion is risk to the patient.

**ISO 26262 Part 6 (Automotive)** implements tiering through ASIL-dependent notation requirements for software unit design. The notation requirements escalate with ASIL level:

| Notation | ASIL A | ASIL B | ASIL C | ASIL D |
|----------|--------|--------|--------|--------|
| Natural language | + | + | + | + |
| Semi-formal (UML, state machines) | + | + | ++ | ++ |
| Formal notation | o | o | + | ++ |

Legend: ++ highly recommended, + recommended, o no recommendation

This means at ASIL C/D, natural language alone is no longer sufficient — semi-formal or formal notations are required to meet the rigor that the standard expects. At ASIL A, natural language suffices. Source: ISO 26262 Wikipedia summary — https://en.wikipedia.org/wiki/ISO_26262; New Eagle ISO 26262 update guide — https://neweagle.net/blog/how-iso-26262-2018-update-affects-you/

**DO-178C (Aviation)** implements tiering between DAL levels — DAL A requires more objectives, independent review, and coverage than DAL D — but within a given DAL, all software at that level is treated uniformly. There is no mechanism in DO-178C itself to tier design documentation depth based on unit complexity within a single project. The DAL is assigned at the system level based on failure conditions; from that point, all software at that DAL receives the same process rigor. Source: Rapita Systems DO-178C Guide — https://www.rapitasystems.com/do178; Real-Time Consulting DAL Analysis — https://real-time-consulting.com/case-study/3956/

**ASPICE SWE.3** covers all software components uniformly — there is no formal tiering mechanism built into the standard itself. However, as discussed in Section 6 of this document, the ASPICE assessment framework can accommodate risk-based differentiation when combined with ISO 26262.

### 2.2 Applying the Risk Principle to Documentation Depth

Even where a standard does not formally define tiers, the underlying risk principle is applicable as a matter of engineering judgment. The logic is straightforward:

Design documentation adds value proportional to:
1. The consequences of a defect in the unit
2. The difficulty of deriving correct behavior from code alone
3. The likelihood that tests will be derived from the design (rather than the code)
4. The frequency with which the unit will be changed or reviewed

From this, a practical tiering criterion emerges:

**Full detailed design is warranted when any of the following apply:**
- The unit implements safety-critical functionality (failure contributes to a hazard)
- The unit contains a complex algorithm that is not obvious from code structure
- The unit implements a state machine with multiple modes or complex transitions
- The unit involves concurrency, synchronization, or shared mutable state
- The unit has a high defect history or has experienced repeated regressions
- The unit is changed frequently and by multiple engineers (high churn, high coupling risk)
- The unit's interface contract has complex pre/postconditions that cannot be captured in type signatures

**Lighter documentation (interface-level) is sufficient when all of the following apply:**
- The unit performs simple, well-understood operations (CRUD, delegation, simple transformations)
- The unit is a DTO, value object, or data class with no significant logic
- The unit implements a well-understood standard pattern (Builder, Factory, Adapter) with no deviations
- The unit's behavior is fully and unambiguously specified by its interface and comprehensive test suite
- The unit has no safety implications at any applicable assurance level

---

## 3. Practical Tiering Approaches

### 3.1 Per-Unit Full Design vs. Per-Component Summary

The traditional safety-critical approach (DO-178C, high-ASIL ISO 26262) writes one Software Design Description per component or subsystem, with unit-level detail embedded for each unit. The document covers:

- Component purpose and responsibility boundaries
- Interface specification for every unit (input/output types, units, ranges, constraints)
- Behavioral specification for each unit (pre/postconditions, algorithms, error handling)
- Dynamic behavior (state machines, sequence diagrams where relevant)
- Shared data structures and their access patterns

This approach scales poorly to large codebases because it produces one heavy document per component rather than naturally separating concerns. For a component with 40 units, 30 of which are trivial and 10 of which are complex, a single document forces a choice: either over-document the trivial units or create a document with uneven depth that is hard to navigate.

### 3.2 Interface-Only Documentation (The Header-File Model)

The Eclipse iceoryx approach — proven viable for ASPICE SWE.3 compliance — documents only the public API (the "contract") and explicitly prohibits documentation that describes implementation internals.

The iceoryx SWE.3 documentation guidelines (https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md) define the following principles:

- Document in header files, not source files (the contract is the interface, not the implementation)
- Use structured Doxygen tags: `@brief` (one-line purpose), `@details` (behavioral contract), `@param`/`@return` (interface spec), `@req` (traceability), `@concurrent` (threading model), `@startuml` (state/sequence diagrams)
- **Forbidden**: documenting what the type system already captures, re-stating parameter names and types in prose, describing implementation steps
- The documentation must describe **why** the design is as it is — rationale, constraints, trade-offs — not **what** the code does (the code already says what)

This approach aligns with ASPICE SWE.3 BP.2 ("Identify, specify, document interfaces of each software unit — names, types, units, resolutions, ranges, default values") and is acceptable to ASPICE assessors when the interface documentation is clearly a **contract** rather than a code description.

**Critical caveat from UL Solutions** (ASPICE assessment body): using source code markup as design documentation is a listed common assessment failure **if** it merely describes what code does rather than serving as a design contract that predates the code. The distinction is not stylistic — it is about the temporal and epistemic relationship: was the interface contract written to constrain the implementation, or was it added afterward to annotate what the implementation already did?

Source: UL Solutions SWE.3 Insights — https://www.ul.com/sis/insights/software-detailed-design-and-unit-construction-swe3-automotive-spice

### 3.3 Component-Level Design Documents with Selective Unit Detail

A scalable pattern that works for large codebases:

```
Component Design Document
├── Component header: purpose, scope, external interfaces
├── Unit inventory: table of all units with one-line description each
├── Shared concerns: error strategy, threading model, common data structures
├── Full unit specifications (only for Tier 1 units: complex, safety-critical)
│   ├── Unit A: full behavioral spec, state machine, error handling
│   └── Unit B: full behavioral spec, algorithm, concurrency model
└── Interface table for Tier 2 units: name, inputs/outputs/constraints only
    (Tier 3 units: named in inventory only, implementation is self-documenting)
```

This is close to what DO-178C expects in a Software Design Description. The LLR (Low-Level Requirements) section is expected to provide enough detail to directly implement without further information — but this expectation applies to the units that carry the design decisions. For a component with many simple delegation units, the design decisions live at the component boundary (interfaces, responsibilities, error contracts), not inside each unit.

### 3.4 How Different Organizations Handle This in Practice

**Eclipse iceoryx (automotive robotics, ASPICE-compliant open source):** Interface documentation in headers only. Implementation documentation is the code. Rationale lives in commit messages and design decision records, referenced from `@details` tags. No separate design documents. Verified as ASPICE-viable by the project's own compliance documentation.

Source: https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md

**RTCA DO-178C programs (aviation):** Typically maintain separate Software Design Description documents that exist as controlled artifacts independent of code. The design document is reviewed and baselined before coding begins. Any change to the code that constitutes a design change requires a design document change and re-review. This is heavyweight but is what the standard expects for DAL A/B.

**Model-Based Development programs (DO-331, MathWorks Simulink/SCADE):** The model IS the design. Code generation eliminates the LLR-to-code gap. This is the highest-rigor, most expensive path, but removes the maintenance burden of keeping two artifacts synchronized.

---

## 4. Design Documentation for Large Legacy Codebases

### 4.1 The Retrofit Problem

Writing design documentation for existing code introduces a fundamental compliance risk: the standard assumption is that design precedes code. The UL Solutions assessment guidance states explicitly: "If you write the detailed design after documenting your code, the point of the unit test is lost."

The full ASPICE SWE.3 causal chain is:
```
Architecture → Detailed Design → Code → Unit Tests (verify code against design)
```

When retrofitting, this chain is broken. The code was written without design documentation, and tests were written to verify the code — not a prior design. From an ASPICE assessor's perspective, this means:

1. The detailed design may merely describe what the code does rather than what it was designed to do.
2. The tests verify the current behavior, not the designed behavior. Defects in the original implementation may be "baked in" as correct behavior.
3. Traceability from architecture to detailed design to code cannot demonstrate forward derivation.

**This is not a hypothetical concern.** Polarion's SWE.3 practitioner blog (https://polarion.code.blog/2022/04/21/swe-3-software-detailed-design-and-unit-construction/) notes this as a common real-world scenario, and the UL Solutions assessment body identifies post-hoc design as the #1 SWE.3 assessment failure mode.

### 4.2 Prioritization for Legacy Retrofit

For a 300k-line legacy codebase, a systematic prioritization framework is needed. The following criteria, applied in combination, identify where retrofit documentation investment has highest return:

**Criterion 1: Safety and criticality.** Any unit that contributes to a safety-relevant function (regardless of ASIL/DAL) is the highest priority. Safety-critical units have the highest cost of defects and the highest audit scrutiny.

**Criterion 2: Cyclomatic complexity.** McCabe's cyclomatic complexity metric, defined in NIST Special Publication 500-235 ("Structured Testing: A Testing Methodology Using the Cyclomatic Complexity Metric," Watson, McCabe, NIST, 1996), provides an objective proxy for the number of independent execution paths in a unit. NIST SP 500-235 recommends a complexity limit of 10 as the baseline, with limits up to 15 acceptable under controlled conditions. Units with complexity > 10 are strong candidates for full design documentation because:
- They have multiple branches that must be independently verified
- Their behavior is not obvious from reading the code
- They require explicit design documentation to derive correct test cases

Source: NIST SP 500-235 — https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication500-235.pdf; McCabe Software NIST report page — https://www.mccabe.com/iq_research_nist.htm

**Criterion 3: Change frequency (hotspot analysis).** Adam Tornhill's "Your Code as a Crime Scene" (*Pragmatic Programmers*, 2015; second edition 2024) introduced the concept of code hotspots: the intersection of change frequency and code size. About 1–2% of a codebase accounts for up to 70% of development work (power law distribution). Hotspot analysis using version control history identifies which units are changed most frequently. Frequently changed units:
- Have higher probability of defect introduction per unit time
- Have higher documentation drift risk (design docs written once, code changed repeatedly)
- Represent the modules where understanding and design clarity have the most leverage

Source: Tornhill, Adam. *Your Code as a Crime Scene, Second Edition*. Pragmatic Programmers, 2024 — https://pragprog.com/titles/atcrime2/your-code-as-a-crime-scene-second-edition/

**Criterion 4: Fan-in (coupling impact).** Fan-in measures how many other units depend on a given unit. High fan-in units (shared utilities, core services, foundational abstractions) have an outsized blast radius when defects occur. A defect in a unit called by 50 other units can affect all 50 call sites. Research published in IEEE Xplore ("An evolutionary study of fan-in and fan-out metrics in OSS," 2010, DOI: 10.1109/ICPC.2010.21) confirms that high fan-in is associated with higher fault propensity and change impact. High fan-in units are strong candidates for full interface documentation and design contracts.

Source: IEEE Xplore — https://ieeexplore.ieee.org/document/5507329/

**Criterion 5: Defect history.** Units with a high density of historical defects are empirically more likely to contain further defects (defects cluster spatially in code). Defect density, measured as defects per KLOC, is a well-established quality metric. BrowserStack and software.com both document it as a standard engineering metric. Units with above-average historical defect density are priority candidates for design documentation — if the design decisions were unclear enough to produce repeated defects, making them explicit is the correct remediation.

### 4.3 The "Design-After-Code" Assessment Strategy

When presenting retrofit work to an assessor, the approach that survives scrutiny is:

**Acknowledge the historical baseline clearly.** Do not attempt to present post-hoc design documentation as if it were design-before-code. Assessors can infer from document metadata and commit history. Attempting to obscure this is a credibility risk.

**Demonstrate rigor of the retrospective analysis.** Show that the retrofit design documentation was produced via disciplined analysis — code review, test review, interface extraction — not just copying code comments into a YAML file.

**Implement design-before-code discipline going forward.** Any new development or significant modification to existing code should follow the proper sequence. Document this commitment in the project quality plan. This shows the assessor that the process is improving, not just that legacy debt is being papered over.

**Use the retrofit as a gap-finding exercise.** A disciplined retrofit often uncovers undocumented behavior, implicit assumptions, and missing test coverage. Documenting these findings as a gap analysis demonstrates the value of the exercise beyond compliance box-checking.

Source: Synopsys blog on ASPICE unit verification preparation — https://www.synopsys.com/blogs/chip-design/preparing-for-automotive-spice-assessment.html; Sodiuswillert on ASPICE traceability evidence — https://www.sodiuswillert.com/en/blog/aspice-traceability-what-assessors-look-for-throughout-the-lifecycle

### 4.4 Module-by-Module Approach

The recommended strategy for large legacy codebases is incremental retrofit, module by module, using the prioritization criteria above:

1. **Start with safety-critical modules.** These have the highest compliance urgency and highest defect cost.
2. **Use complexity metrics to triage remaining modules.** Generate a cyclomatic complexity report for the entire codebase. All units with complexity > 10 go on the design documentation queue.
3. **Apply hotspot analysis to prioritize within the queue.** Among high-complexity units, start with the ones that change most frequently.
4. **Apply fan-in analysis for interface documentation priority.** High fan-in units need at minimum rigorous interface documentation even if their internal complexity is low.
5. **Accept that some modules will never need full documentation.** DTOs, simple utilities, and stable low-complexity modules can remain at the interface-table tier indefinitely. This is not a compliance failure — it is a deliberate tiering decision.

---

## 5. Metrics and Decision Criteria

### 5.1 Cyclomatic Complexity as a Proxy for "Needs Design"

Thomas J. McCabe introduced cyclomatic complexity in his 1976 paper "A Complexity Measure" (*IEEE Transactions on Software Engineering*, Vol. SE-2, No. 4, Dec. 1976). The metric counts the number of linearly independent paths through a program's control flow graph. It is directly calculable from source code and is supported by virtually every static analysis tool.

**Threshold recommendations (NIST SP 500-235):**
- Complexity 1–10: Simple, well-structured code. Adequate coverage achievable through standard testing. Limited design documentation needed.
- Complexity 11–15: Moderate complexity. Design documentation beneficial, especially for test derivation.
- Complexity > 15: High complexity. Design documentation required for adequate test coverage and verification. Consider refactoring.

The methodology's policy recommendation: "For each module, either limit cyclomatic complexity to the agreed-upon limit or provide a written explanation of why the limit was exceeded." This is directly applicable as a design documentation trigger: any unit with complexity above the agreed threshold gets a design document; units below the threshold can operate with interface-level documentation only.

Source: NIST SP 500-235 — https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication500-235.pdf; Wikipedia on Cyclomatic Complexity — https://en.wikipedia.org/wiki/Cyclomatic_complexity; Klocwork McCabe documentation — https://help.klocwork.com/current/en-us/concepts/mccabecyclomaticcomplexity.htm

### 5.2 Fan-In and Fan-Out for Design Priority

**Fan-in** (number of callers or dependents) identifies shared infrastructure. High fan-in means a defect propagates widely. Prioritize interface documentation.

**Fan-out** (number of dependencies) identifies units with complex orchestration or wide integration surface. High fan-out means the unit's behavior depends on many external contracts. Prioritize behavioral documentation and error handling specification.

Research from Aivosto's Project Metrics Help (https://www.aivosto.com/project/help/pm-sf.html) and the IEEE evolutionary study (DOI: 10.1109/ICPC.2010.21) establish that:
- High SFOUT (structural fan-out): strongly coupled code, higher execution complexity, harder to test in isolation.
- High SFIN (structural fan-in): good reuse indicator, but high cross-module coupling risk.

**Rule of thumb:** Any unit with fan-in > N (project-specific threshold, commonly 10–20 for medium-sized codebases) is a design documentation candidate regardless of its internal complexity, because the interface contract affects all N callers.

### 5.3 Defect Density as Priority Signal

Defect density (defects per KLOC) is calculated from defect tracking history. The established industry practice (documented by software quality measurement platforms including Kiuwan, BrowserStack, and Count.co) is to use defect density as a quality indicator and prioritization tool.

**For design documentation prioritization:** units with above-average defect density have a demonstrated history of being misunderstood or misimplemented. This is evidence that the design intent is not clear from the code, which is precisely the condition that design documentation addresses.

The corollary: defect density is also a strong leading indicator that the unit needs refactoring (which typically requires design-first thinking to do safely). Design documentation and refactoring are complementary investments for high-defect-density units.

### 5.4 Change Frequency as Maintenance Cost Predictor

Tornhill's hotspot analysis uses version control commit history to measure how frequently each module is changed. The distribution follows a power law: a small fraction of modules account for the majority of changes.

**For design documentation:** high-change modules have the highest documentation drift risk. Design documentation written once for a module that changes 200 times per year will be stale within weeks unless it is actively maintained. This creates a design documentation maintenance policy:

- High-change modules: documentation must be a core part of the change process (update design doc as part of the change ticket)
- Low-change modules: documentation can be written once and reviewed periodically
- Very-high-change modules with low complexity: may be better served by comprehensive tests than by design documents

### 5.5 A Practical Decision Framework

Combining the above metrics into a tractable decision process:

```
For each unit, score on four dimensions (0 = low, 1 = high):

  S = Safety-critical? (0 or 1)
  C = Cyclomatic complexity > threshold? (0 or 1, threshold typically 10)
  F = Fan-in > threshold? (0 or 1, threshold depends on codebase size)
  D = Defect density above average? (0 or 1)

  Score = S*4 + C*2 + F*2 + D*1  (safety is given highest weight)

  Score 5–9: Tier 1 — Full detailed design required
  Score 3–4: Tier 2 — Interface documentation + key behavior notes
  Score 0–2: Tier 3 — Interface table in component-level document only
```

This is a heuristic, not a formal algorithm. The weights reflect the relative importance of safety (non-negotiable) vs. complexity (strong predictor of value) vs. coupling (moderate predictor). Calibrate thresholds to the project's codebase characteristics.

**Note on change frequency:** Hotspot analysis is best used to prioritize the sequence of documentation work rather than to determine the tier. A safety-critical unit that is rarely changed still warrants Tier 1 documentation; change frequency determines whether it goes first or last in the documentation queue, not whether it gets documented.

---

## 6. Cross-Standard Mapping

### 6.1 Where Standards Explicitly Support Tiering

**IEC 62304:** Explicit formal tiering. Class A and B do not require detailed design. Class C does. This is the clearest model of standards-supported tiering.

**ISO 26262:** Explicit ASIL-dependent notation requirements. Not about whether to document but about how rigorously (notation type) to document. ASIL decomposition (ISO 26262-9) allows higher ASIL requirements to be split across independent elements, which can reduce the ASIL level of individual components and thus reduce their documentation rigor requirements.

Source: Infineon ASIL Decomposition knowledge base — https://community.infineon.com/t5/Knowledge-Base-Articles/ASIL-decomposition-ISO-26262/ta-p/852405

### 6.2 Where Standards Are Silent on Internal Tiering

**DO-178C:** No internal tiering mechanism within a DAL level. All software at a given DAL receives the same process rigor. This does not mean all design documents must be equally long — complexity still drives how much content a unit's LLR needs — but the process (review, traceability, independence requirements) is uniform.

**ASPICE SWE.3:** No formal tiering mechanism. SWE.3 BP.1 requires "detailed design for each software component," BP.2 requires interface documentation for "each software unit." The standard does not distinguish simple from complex units; all are in scope.

### 6.3 How to Justify Tiering to an Assessor When the Standard Does Not Explicitly Support It

The ASPICE framework provides an opening through its integration with ISO 26262. Research from Reactive Systems' ASPICE Overview documentation (https://reactive-systems.com/automotive-spice/aspice-overview.html) and assessment body guidance notes that:

- ASPICE itself does not vary rigor by criticality, but most automotive projects also operate under ISO 26262
- The two standards are used together; the combined application allows ISO 26262 ASIL levels to drive the rigor of ASPICE process implementation
- A Level 5 ASPICE process can specify lower rigor for uncritical software provided the justification is documented

**Practical approach for justifying tiering to an ASPICE assessor:**

1. **Define the tiering criteria explicitly** in the Software Quality Plan or Development Plan. State that units are classified by criticality level, and that documentation depth is scaled accordingly.

2. **Map the tiering to ISO 26262 ASIL levels** (if applicable). ASIL QM units get lighter treatment; ASIL D units get full treatment. This cross-references a standard the assessor already recognizes.

3. **Ensure all units appear somewhere** in the documentation system. No unit should be undocumented; the question is how much detail each gets. Even Tier 3 units need to appear in an interface table.

4. **Document the tiering decision for each unit.** The design plan should state: "Unit X is Tier 3 because complexity = 3, no safety function, no historical defects." This demonstrates the decision was made consciously, not by omission.

5. **Be conservative at boundaries.** When in doubt between Tier 1 and Tier 2, choose Tier 1. The cost of over-documenting a few units is lower than the cost of an assessment finding.

Source: Reactive Systems ASPICE Overview — https://reactive-systems.com/automotive-spice/aspice-overview.html; Synopsys ASPICE preparation blog — https://www.synopsys.com/blogs/chip-design/preparing-for-automotive-spice-assessment.html

### 6.4 The DO-178C Special Case: Internal Tiering via Complexity

Within DO-178C, while all software at a DAL must meet the same process objectives, the *content* of LLRs naturally scales with complexity. A trivial utility function's LLR is short; a complex state machine's LLR is detailed. The standard does not mandate equal document length — it mandates that LLRs be sufficient to implement source code "without further information."

This creates an implicit complexity-based tiering within the LLR structure: the more complex the unit, the more content the LLR needs to satisfy the sufficiency criterion. For simple units, a few lines of behavioral description and interface specification may suffice. For complex algorithms, the LLR may need to specify the algorithm step-by-step, enumerate edge cases, and define error handling for every path.

Source: AdaCore DO-178C compliance analysis — https://learn.adacore.com/booklets/adacore-technologies-for-airborne-software/chapters/analysis.html

---

## 7. Honest Gaps in This Research

### 7.1 No Verified Empirical ROI Data for Design Documentation

Despite extensive search, no peer-reviewed study was found that quantifies the ROI of design documentation for software per unit or per module. The Zhi et al. 2015 systematic mapping confirms this gap is real: documentation cost and benefit studies are rare and methodologically weak. Practitioners must rely on first-principles reasoning and qualitative evidence.

### 7.2 No Industry Survey Data on Tiering Practices

No survey data was found on how many DO-178C, ASPICE, or ISO 26262 programs actually implement formal tiering vs. uniform documentation. Practitioner knowledge here is anecdotal (iceoryx is an example that works; others may exist but are proprietary).

### 7.3 ASPICE Assessor Tolerance for Tiering Is Variable

The guidance above is based on what the framework supports in principle. Individual assessors may have stricter interpretations. Organizations planning tiered documentation strategies should validate the approach with their specific assessment body early in the project.

---

## Sources

### Peer-Reviewed / Academic

- Zhi, Garousi-Yusifoğlu, et al. "Cost, benefits and quality of software development documentation: A systematic mapping." *Journal of Systems and Software*, Vol. 99, 2015, pp. 175–198. DOI: 10.1016/j.jss.2014.09.050 — https://www.sciencedirect.com/science/article/abs/pii/S0164121214002131
- McCabe, T.J. "A Complexity Measure." *IEEE Transactions on Software Engineering*, Vol. SE-2, No. 4, December 1976.
- Watson, A.H. and McCabe, T.J. "Structured Testing: A Testing Methodology Using the Cyclomatic Complexity Metric." NIST Special Publication 500-235, 1996 — https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication500-235.pdf
- Rios, N. et al. "Documentation Technical Debt." *XXXIII Brazilian Symposium on Software Engineering*, ACM, 2019. DOI: 10.1145/3350768.3350773 — https://dl.acm.org/doi/10.1145/3350768.3350773
- IEEE Xplore: "An evolutionary study of fan-in and fan-out metrics in OSS." 2010 IEEE 18th ICPC — https://ieeexplore.ieee.org/document/5507329/

### Books

- Capers Jones. *Estimating Software Costs*, 2nd ed. McGraw-Hill, 2007.
- Capers Jones. *Applied Software Measurement*, 3rd ed. McGraw-Hill, 2008.
- Barry Boehm. *Software Engineering Economics*. Prentice Hall, 1981.
- Adam Tornhill. *Your Code as a Crime Scene, Second Edition*. Pragmatic Programmers, 2024 — https://pragprog.com/titles/atcrime2/your-code-as-a-crime-scene-second-edition/
- Leanna Rierson. *Developing Safety-Critical Software*. CRC Press, 2013. (DO-178C co-author)

### Standards Bodies and Assessment Organizations

- UL Solutions (ASPICE assessment body) — SWE.3 Insights: https://www.ul.com/sis/insights/software-detailed-design-and-unit-construction-swe3-automotive-spice
- UL Solutions — SWE.3 Guide: https://www.ul.com/sis/resources/process-swe-3
- Johner Institute — IEC 62304 Safety Classes: https://blog.johner-institute.com/iec-62304-medical-software/safety-class-iec-62304/
- Rapita Systems — DO-178C Guide: https://www.rapitasystems.com/do178
- Real-Time Consulting — DO-178C DAL Analysis: https://real-time-consulting.com/case-study/3956/
- Infineon Developer Community — ASIL Decomposition: https://community.infineon.com/t5/Knowledge-Base-Articles/ASIL-decomposition-ISO-26262/ta-p/852405

### Practitioner and Open-Source

- Eclipse iceoryx SWE.3 Documentation Guidelines — https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md
- Polarion SWE.3 Blog — https://polarion.code.blog/2022/04/21/swe-3-software-detailed-design-and-unit-construction/
- Synopsys ASPICE Assessment Preparation Blog — https://www.synopsys.com/blogs/chip-design/preparing-for-automotive-spice-assessment.html
- Sodiuswillert ASPICE Traceability Evidence Blog — https://www.sodiuswillert.com/en/blog/aspice-traceability-what-assessors-look-for-throughout-the-lifecycle
- Reactive Systems ASPICE Overview — https://reactive-systems.com/automotive-spice/aspice-overview.html
- AdaCore DO-178C Compliance Analysis — https://learn.adacore.com/booklets/adacore-technologies-for-airborne-software/chapters/analysis.html
- Greenlight Guru IEC 62304 Classifications — https://www.greenlight.guru/glossary/iec-62304

### Tool Vendors / Static Analysis Documentation

- Klocwork McCabe Cyclomatic Complexity: https://help.klocwork.com/current/en-us/concepts/mccabecyclomaticcomplexity.htm
- Aivosto Project Metrics — Structural Fan-In and Fan-Out: https://www.aivosto.com/project/help/pm-sf.html
- Wikipedia — Cyclomatic Complexity: https://en.wikipedia.org/wiki/Cyclomatic_complexity
- Wikipedia — ISO 26262: https://en.wikipedia.org/wiki/ISO_26262
