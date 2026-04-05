# V-Model Standards: Software Implementation (Coding Phase) Requirements

## Research Question

What do major V-model safety standards require or recommend for the actual
software implementation (coding) activity? This research focuses exclusively
on the coding phase -- not design, not testing -- covering coding standards,
code reviews, language subsets, complexity limits, defensive coding, and
traceability from design to code.

---

## 1. DO-178C (Aviation Software)

### 1.1 Software Coding Process (Section 5.3)

DO-178C Section 5.3 defines the Software Coding Process as one of four
development sub-processes (requirements, design, coding, integration). The
coding process transforms Low-Level Requirements (LLR) and the software
architecture into Source Code.

**Objectives of Section 5.3:**

- Source Code is developed from LLR and software architecture
- Source Code conforms to the defined coding standards
- Source Code is traceable to LLR

DO-178C does not prescribe HOW to code. It defines WHAT the coding process
must produce and WHAT properties the output must have. The standard is
objectives-based, not prescriptive.

### 1.2 Coding Standards (Section 11.8)

DO-178C requires that coding standards be defined in the Software Development
Plan (SDP) and applied during the coding process. The standard does not
mandate a specific coding standard (e.g., MISRA) but requires that the
chosen standard address:

| Topic | Requirement |
|-------|-------------|
| **Naming conventions** | Consistent, unambiguous identifier naming |
| **Code structure** | Rules for modularity, function size, nesting |
| **Complexity constraints** | Limits on code complexity |
| **Comments** | Documentation requirements within source code |
| **Error handling** | Consistent error detection and handling patterns |
| **Language subset** | Restrictions on language features that are ambiguous, error-prone, or compiler-dependent |
| **Unambiguous syntax** | The programming language must have unambiguous syntax and clear data control |

The standard requires a "programming language with unambiguous syntax and
clear control of data with definite naming conventions and constraints on
complexity." Popular choices include MISRA C/C++, JSF++ AV (F-35 standard),
and CERT C/C++.

### 1.3 Verification of Source Code (Table A-5)

Table A-5 ("Verification of Outputs of Software Coding and Integration
Process") contains **9 objectives** that must be satisfied. Applicability
depends on the Design Assurance Level (DAL):

| # | Objective (paraphrased) | A | B | C | D |
|---|------------------------|---|---|---|---|
| 1 | Source Code complies with LLR | Y | Y | Y | Y |
| 2 | Source Code complies with software architecture | Y | Y | Y | Y |
| 3 | Source Code is verifiable (supports testing/analysis) | Y | Y | N | N |
| 4 | Source Code conforms to coding standards | Y | Y | Y | Y |
| 5 | Source Code is traceable to LLR | Y | Y | Y | Y |
| 6 | Source Code is accurate and consistent | Y | Y | Y(i) | Y(i) |
| 7 | Output of integration process is complete and correct | Y | Y | Y | Y |
| 8 | Executable Object Code is robust with target hardware | Y | Y | Y | Y |
| 9 | Executable Object Code complies with HLR (object code verification) | Y | Y | N | N |

Y = applicable, Y(i) = applicable with independence, N = not applicable

Objectives 3 and 9 apply only at DAL A and B, reflecting the higher rigor
required at those levels.

### 1.4 Reviews and Analyses of Source Code (Section 6.3.4)

Section 6.3.4 defines the reviews and analyses required for source code.
These are part of the broader verification process (Section 6):

**Review objectives include verifying that:**
- Source Code matches the data flow and control flow defined in the architecture
- Source Code is conformant to coding standards
- Source Code is accurate and consistent
- Source Code is traceable to LLR
- Source Code contains no dead code or deactivated code without justification
- Algorithms are correct
- Memory usage is correct and within bounds

**Review methodology:**
- Use of checklists or similar aids (Section 6.3)
- Reviews provide qualitative assessment of correctness
- Analyses provide repeatable evidence of correctness
- Static code analysis can satisfy some review/analysis objectives

**Independence requirements:**
- DAL A: 16 of 31 total review/analysis objectives require independence
  (reviewer cannot be the developer)
- DAL B: Independence required for a subset of objectives
- DAL C/D: Self-review may be acceptable for most objectives

### 1.5 Dead Code and Deactivated Code

DO-178C has specific requirements regarding:

- **Dead code**: Code that cannot be executed (no control flow path reaches it).
  Must be identified and removed or justified.
- **Deactivated code**: Code that is intentionally disabled under certain
  configurations. Must be verified and documented even when inactive.

These requirements are unique to DO-178C among the standards surveyed and
reflect the aviation industry's zero-tolerance approach to unexplained code.

### 1.6 Source Code to Object Code Traceability

For DAL A and B, DO-178C requires additional verification that the compiler
did not introduce errors or unintended functionality:

- **Source-to-object code traceability** (DAL A)
- **Object code verification** when the compiler produces code not directly
  traceable to source (e.g., compiler-generated safety checks, optimization
  artifacts)

**Sources:**
- RTCA DO-178C, Sections 5.3, 6.3.4, 11.8, Table A-5
- [AdaCore DO-178C Compliance Analysis](https://learn.adacore.com/booklets/adacore-technologies-for-airborne-software/chapters/analysis.html)
- [AdaCore Blog: Fresh Take on DO-178C Reviews](https://blog.adacore.com/a-fresh-take-on-do-178c-software-reviews)
- [LDRA DO-178C Guide](https://ldra.com/do-178/)
- [Rapita Systems DO-178C Guide](https://www.rapitasystems.com/do178)
- [Parasoft DO-178C Overview](https://www.parasoft.com/learning-center/do-178c/what-is/)
- [Parasoft DO-178C Static Analysis](https://www.parasoft.com/learning-center/do-178c/static-analysis/)
- [TheCloudStrap DO-178C Objectives List](https://thecloudstrap.com/do-178c-objectives-list/)
- [Wikipedia: DO-178C](https://en.wikipedia.org/wiki/DO-178C)
- [NASA: Certification of Safety-Critical Software Under DO-178C](https://ntrs.nasa.gov/api/citations/20120016835/downloads/20120016835.pdf)

---

## 2. DO-330 (Tool Qualification)

### 2.1 Relationship to DO-178C

DO-330 mirrors DO-178C's development process structure but applies it to
software tools. When a tool's output becomes part of airborne software or
when a tool eliminates/reduces verification activities, the tool itself must
be developed and qualified with appropriate rigor.

### 2.2 Tool Qualification Levels (TQL)

| Criterion | DAL A | DAL B | DAL C | DAL D |
|-----------|-------|-------|-------|-------|
| Tool output IS part of airborne software | TQL-1 | TQL-2 | TQL-3 | TQL-4 |
| Tool eliminates/reduces verification | TQL-4 | TQL-4 | TQL-4 | TQL-5 |
| Tool could fail to detect errors | TQL-5 | TQL-5 | TQL-5 | TQL-5 |

TQL-1 approaches DAL A rigor. TQL-5 is the lightest level.

### 2.3 Implementation Requirements for Tool Development

For TQL-1 through TQL-3, DO-330 requires a full tool development lifecycle
that mirrors DO-178C:

**Coding-specific requirements:**
- Tool source code must be developed from Tool Low-Level Requirements
- Tool source code must conform to coding standards
- Tool source code must be traceable to Tool Operational Requirements (TOR)
  and Tool Requirements
- Code reviews are required with independence scaling by TQL
- Tool Verification Plan (TVP) must cover source code verification

**Key artifacts:**
- Tool Qualification Plan (TQP)
- Tool Operational Requirements (TOR)
- Tool Requirements (TR) -- analogous to HLR/LLR
- Tool Source Code
- Tool Verification Plan (TVP)
- Tool Accomplishment Summary (TAS)

**For TQL-4 and TQL-5:**
- Reduced rigor for coding activities
- TQL-5 requires primarily operational verification (does the tool work
  correctly?) rather than full development process evidence

### 2.4 Practical Impact

If you are building a code generator, test tool, or requirements management
tool used in a DO-178C project, that tool's own source code may need to meet
nearly the same coding rigor as the airborne software it supports. This is
often underestimated in practice.

**Sources:**
- RTCA DO-330, "Software Tool Qualification Considerations," 2011
- [LDRA DO-330 Overview](https://ldra.com/do-330/)
- [AFuzion DO-330 Introduction](https://afuzion.com/do-330-introduction-tool-qualification/)
- [Rapita Systems DO-330](https://www.rapitasystems.com/do-330)

---

## 3. ASPICE / ISO 26262 (Automotive)

### 3.1 ASPICE SWE.3: Software Detailed Design and Unit Construction

SWE.3 is the ASPICE process that covers BOTH detailed design AND coding
(unit construction). The coding activity is inseparable from design in this
process model.

#### 3.1.1 Base Practices (ASPICE 3.1)

SWE.3 defines 8 base practices. The implementation-specific ones are:

| BP | Name | Implementation Relevance |
|----|------|-------------------------|
| BP.1 | Develop detailed design | Design that code must conform to |
| BP.2 | Define interfaces | Names, types, units, ranges, defaults -- "without this, proper testing is impossible" (UL Solutions) |
| BP.5 | Establish bidirectional traceability | Code traceable to architecture and requirements |
| BP.6 | Ensure consistency | Code consistent with all design levels |
| BP.8 | Develop code per detailed design | The actual coding activity |

**Critical assessor expectations for BP.8:**
- Code must demonstrably follow the detailed design (not the reverse)
- Design-before-code ordering is essential -- post-hoc design is the #1
  SWE.3 assessment failure (UL Solutions)
- Using source code markup as design documentation is flagged as an
  assessment failure unless it serves as a contract that existed before code

#### 3.1.2 Base Practices (ASPICE 4.0)

ASPICE 4.0 consolidated the base practices:

| BP | Name |
|----|------|
| BP.1 | Specify static aspects of detailed design |
| BP.2 | Specify dynamic aspects of detailed design |
| BP.3 | Develop software units |
| BP.4 | Ensure consistency and establish bidirectional traceability |

The coding activity (BP.3) remains explicitly tied to the detailed design.

#### 3.1.3 Coding Standards in ASPICE Context

ASPICE does not prescribe specific coding standards. However, SWE.4 (Software
Unit Verification) requires static verification that may include:

- Static analysis against coding standards (e.g., MISRA rules)
- Code reviews
- Coding standards compliance checking

The choice of coding standard is a project decision, but its application must
be demonstrable and consistent.

#### 3.1.4 Traceability Requirements

ASPICE requires bidirectional traceability:

```
SW Requirements (SWE.1) <-> SW Architecture (SWE.2) <-> Detailed Design (SWE.3) <-> Source Code (SWE.3)
```

Every source code unit must trace to a detailed design element, which traces
to an architectural component, which traces to software requirements.

### 3.2 ISO 26262 Part 6: Software Unit Design and Implementation

ISO 26262 Part 6 Clause 8 addresses software unit design and implementation.
It uses ASIL-dependent method recommendations.

#### 3.2.1 Table 1: Coding and Modeling Guidelines

ISO 26262 Part 6 Table 1 defines topics for coding guidelines with
ASIL-dependent recommendations:

| Topic | ASIL A | ASIL B | ASIL C | ASIL D |
|-------|--------|--------|--------|--------|
| 1a. Enforcement of low complexity | ++ | ++ | ++ | ++ |
| 1b. Use of language subsets | ++ | ++ | ++ | ++ |
| 1c. Enforcement of strong typing | + | ++ | ++ | ++ |
| 1d. Use of defensive implementation techniques | + | + | + | ++ |
| 1e. Use of well-trusted design principles | + | ++ | ++ | ++ |
| 1f. Use of unambiguous graphical representation | + | + | + | ++ |
| 1g. Use of style guides | + | + | ++ | ++ |
| 1h. Use of naming conventions | ++ | ++ | ++ | ++ |

Legend: ++ highly recommended, + recommended, o no recommendation

**Key observations:**
- Low complexity enforcement and language subsets are **highly recommended
  at ALL ASIL levels** -- not optional
- Defensive programming is recommended for all levels and highly recommended
  for ASIL D
- Style guides become highly recommended at ASIL C and above
- Strong typing enforcement escalates from recommended (ASIL A) to highly
  recommended (ASIL B+)

#### 3.2.2 Table 6: Design Principles for Software Unit Design

ISO 26262 requires that software unit design and implementation follow
specific design principles:

| Principle | ASIL A | ASIL B | ASIL C | ASIL D |
|-----------|--------|--------|--------|--------|
| Correct order of execution | ++ | ++ | ++ | ++ |
| Consistency of interfaces | ++ | ++ | ++ | ++ |
| Correct data and control flow | ++ | ++ | ++ | ++ |
| Simplicity | + | + | ++ | ++ |
| Comprehensibility | + | ++ | ++ | ++ |
| Testability | + | ++ | ++ | ++ |
| Atomicity of design elements | + | + | ++ | ++ |

#### 3.2.3 Prohibited and Restricted Language Features

ISO 26262 references MISRA guidelines as the primary language subset for
C and C++. The standard's requirement for "use of language subsets" (Table 1,
item 1b) means in practice:

**Typically prohibited:**
- Unrestricted pointer arithmetic
- Unconditional jumps (goto)
- Recursion (without explicit justification)
- Dynamic memory allocation after initialization
- Multiple return statements (context-dependent)

**Typically restricted:**
- Implicit type conversions
- Bitwise operations on signed types
- Use of unions
- Compiler extensions
- Assembly language inline code

The specific prohibitions depend on the chosen language subset (MISRA,
CERT, JSF++, AUTOSAR C++14, etc.).

#### 3.2.4 Notation Requirements

ISO 26262 specifies notation formality requirements for unit design:

| Notation | ASIL A | ASIL B | ASIL C | ASIL D |
|----------|--------|--------|--------|--------|
| Natural language | + | + | + | + |
| Semi-formal (UML, state machines) | + | + | ++ | ++ |
| Formal notation | o | o | + | ++ |

At ASIL C/D, semi-formal or formal notation is highly recommended. Plain
natural language alone is considered insufficient at high ASIL levels.

#### 3.2.5 Code Review / Unit Verification (Part 6, Table 9)

ISO 26262 Part 6, Section 9 covers software unit verification methods:

| Method | ASIL A | ASIL B | ASIL C | ASIL D |
|--------|--------|--------|--------|--------|
| Walk-through | + | + | + | + |
| Inspection | + | + | ++ | ++ |
| Static code analysis | ++ | ++ | ++ | ++ |
| Formal verification | o | o | + | + |
| Control flow analysis | + | + | ++ | ++ |
| Data flow analysis | + | + | ++ | ++ |

**Notable:** Static code analysis is **highly recommended at ALL ASIL levels**
in the 2018 edition (upgraded from just recommended for ASIL A in 2011).

**Sources:**
- [UL Solutions SWE.3 Guide](https://www.ul.com/sis/resources/process-swe-3) (ASPICE assessment body)
- [UL Solutions SWE.3 Insights](https://www.ul.com/sis/insights/software-detailed-design-and-unit-construction-swe3-automotive-spice)
- [Eclipse iceoryx SWE.3 Guidelines](https://github.com/eclipse-iceoryx/iceoryx/blob/main/doc/aspice_swe3_4/swe_docu_guidelines.md)
- [ASPICE 3.1 vs 4.0 Comparison](https://a-spice.de/wp-content/uploads/2024/05/ASPICE_31_vs_40_part_SWE_1-6.pdf)
- [LDRA ASPICE Compliance](https://ldra.com/aspice/)
- [Perforce ISO 26262 Coding Standards](https://www.perforce.com/resources/qac/how-comply-iso-26262-standard)
- [Parasoft ISO 26262 MISRA](https://www.parasoft.com/learning-center/iso-26262/misra/)
- [Black Duck ISO 26262 Guidelines](https://www.blackduck.com/content/dam/black-duck/en-us/whitepapers/meeting-iso-26262-software-standards.pdf)
- [Parasoft ISO 26262 Static Analysis](https://www.parasoft.com/learning-center/iso-26262/static-analysis/)
- [Medium: ISO 26262 Part 6 Unit Design](https://medium.com/@animeshsarkar_18504/functional-safety-in-automotive-software-mastering-iso-26262-part-6-for-unit-design-implementation-1208268eff)
- ISO 26262:2018, Part 6, Clauses 8 and 9

---

## 4. IEC 62304 (Medical Device Software)

### 4.1 Section 5.5: Software Unit Implementation

IEC 62304 Section 5.5 covers software unit implementation. The requirements
are risk-class-dependent:

| Sub-clause | Requirement | Class A | Class B | Class C |
|------------|-------------|---------|---------|---------|
| 5.5.1 | Implement each software unit | Required | Required | Required |
| 5.5.2 | Establish software unit verification process | -- | Required | Required |
| 5.5.3 | Software unit acceptance criteria | -- | Required | Required |
| 5.5.4 | Additional software unit acceptance criteria | -- | -- | Required |
| 5.5.5 | Software unit verification | -- | Required | Required |

### 4.2 What Section 5.5 Requires

**For all classes:**
- Implement each software unit per the detailed design (or software
  architecture for Class A/B where detailed design may not be required)

**For Class B and C (5.5.2, 5.5.3, 5.5.5):**
- Establish a verification process for software units
- Define acceptance criteria for unit verification, including:
  - Code implements the requirements (including risk control measures)
  - Code is free from contradiction with documented interfaces
  - Code conforms to coding standards
- Verify each unit against acceptance criteria

**For Class C only (5.5.4):**
- Additional acceptance criteria covering:
  - Proper event sequence
  - Data and control flow correctness
  - Fault handling (resource, timeout, error conditions)
  - Memory management and variable initialization
  - Memory overflow detection
  - Boundary condition checking

### 4.3 Coding Standards in IEC 62304

IEC 62304 states that "to consistently achieve desirable code characteristics,
coding standards should be used to specify a preferred coding style," including:

- Requirements for code understandability
- Language usage rules or restrictions
- Complexity management rules

The standard does not mandate a specific coding standard but requires that
one be defined and applied. For Class B and C, compliance with the chosen
standard must be verified.

### 4.4 Comparison with Other Standards

IEC 62304 is notably **less prescriptive** about implementation than DO-178C
or ISO 26262:

- **No detailed design required for Class A or B** (only Class C)
- **No unit verification required for Class A**
- **No specific coding standard mandated** -- just "should be used"
- **No language subset requirement** -- only "language usage rules or
  restrictions" as a suggestion for coding standard content
- **No explicit complexity metrics** -- just "complexity management"

This lighter touch reflects the medical device industry's risk-based approach:
Class A software (no risk of harm) has minimal requirements, while Class C
(death or serious injury) approaches the rigor of automotive ASIL C/D.

**Sources:**
- IEC 62304:2006+AMD1:2015, Section 5.5
- [IEC 62304 LinkedIn Review](https://www.linkedin.com/pulse/iec-62304-ed-11-review-chandrasr-k-pmp-he-him-)
- [Wikipedia: IEC 62304](https://en.wikipedia.org/wiki/IEC_62304)
- [Johner Institute Safety Classes](https://blog.johner-institute.com/iec-62304-medical-software/safety-class-iec-62304/)
- [Perforce IEC 62304 Overview](https://www.perforce.com/blog/qac/what-iec-62304)
- [LDRA IEC 62304](https://ldra.com/iec-62304/)
- [QA Systems IEC 62304](https://www.qa-systems.com/solutions/iec-62304)
- [Ketryx IEC 62304 Guide](https://www.ketryx.com/blog/a-comprehensive-guide-to-iec-62304-navigating-the-standard-for-medical-device-software)

---

## 5. Cross-Standard Comparison: Implementation Requirements

### 5.1 Coding Standards Compliance

| Aspect | DO-178C | DO-330 | ASPICE | ISO 26262 | IEC 62304 |
|--------|---------|--------|--------|-----------|-----------|
| Coding standard required? | Yes (must define in SDP) | Yes (mirrors DO-178C per TQL) | Expected (assessor checks) | Yes (Table 1) | Should (not shall) |
| Specific standard mandated? | No | No | No | No (references MISRA) | No |
| Compliance verification? | Yes (Table A-5 obj. 4) | Yes (per TQL) | Yes (SWE.4 scope) | Yes (static analysis ++) | Yes (Class B/C) |
| Language subset required? | Yes | Yes (per TQL) | Not explicit | Yes (Table 1, 1b: ++) | Suggested |
| Complexity limits? | Yes (in coding std) | Yes (per TQL) | Not explicit in SWE.3 | Yes (Table 1, 1a: ++) | Suggested |

### 5.2 Code Review Requirements

| Aspect | DO-178C | DO-330 | ASPICE | ISO 26262 | IEC 62304 |
|--------|---------|--------|--------|-----------|-----------|
| Code review required? | Yes (6.3.4) | Yes (per TQL) | Expected (SWE.4) | Yes (Table 9) | Class B/C only |
| Independence required? | DAL A/B yes | TQL-1/2 yes | Not explicit | ASIL C/D: inspection ++ | Not explicit |
| Static analysis? | Can replace some reviews | Same | Expected practice | ++ all ASILs | Recognized method |
| Checklists required? | Yes (Section 6.3) | Same structure | Assessor expects | Recommended | Not explicit |

### 5.3 Traceability from Design to Code

| Aspect | DO-178C | DO-330 | ASPICE | ISO 26262 | IEC 62304 |
|--------|---------|--------|--------|-----------|-----------|
| Design-to-code traceability? | Yes (LLR -> Source) | Yes (Tool Req -> Source) | Yes (SWE.3 BP.5) | Yes (Part 6 Clause 8) | Class C only |
| Bidirectional? | Yes | Yes | Yes | Yes | Not explicit |
| Coverage analysis? | Yes (no orphan code) | Yes | Yes | Yes | Class C: yes |
| Dead code identification? | Yes (specific objective) | Per TQL | Not specific | Not as explicit | Not explicit |

### 5.4 Defensive Coding Practices

| Practice | DO-178C | ISO 26262 | IEC 62304 | ASPICE |
|----------|---------|-----------|-----------|--------|
| Defensive programming | Expected via coding std | Table 1, 1d: + to ++ | Class C: fault handling | Not explicit in SWE.3 |
| Input validation | Expected | Expected | 5.5.4: boundary checking | Expected practice |
| Error handling | Coding std topic | Design principle | 5.5.4: fault handling | Expected practice |
| Resource management | Expected | Expected | 5.5.4: memory mgmt | Expected practice |
| Boundary checking | Expected | Expected | 5.5.4: explicit requirement | Expected practice |

### 5.5 Language Subsets and Prohibited Features

| Standard | Language Subset Requirement | Common Implementations |
|----------|---------------------------|----------------------|
| DO-178C | Required (coding standard must define) | MISRA C/C++, JSF++ AV, CERT C |
| DO-330 | Required per TQL | Same as DO-178C |
| ASPICE | Not explicitly required (but expected practice) | MISRA C/C++ (de facto automotive) |
| ISO 26262 | Highly recommended all ASILs (Table 1, 1b) | MISRA C/C++, AUTOSAR C++14 |
| IEC 62304 | Suggested ("language usage rules or restrictions") | MISRA C, CERT C (common practice) |

### 5.6 Code Metrics Requirements

| Metric Type | DO-178C | ISO 26262 | IEC 62304 | ASPICE |
|-------------|---------|-----------|-----------|--------|
| Cyclomatic complexity | Via coding standard | Table 1, 1a: enforced | Suggested | Not explicit |
| Halstead metrics | Not explicit | Referenced in 1a context | Not explicit | Not explicit |
| Function/file size | Via coding standard | Via style guides (1g) | Not explicit | Not explicit |
| Nesting depth | Via coding standard | Via complexity enforcement | Not explicit | Not explicit |
| Code coverage (structural) | Yes (Table A-7) | Yes (Part 6 Table 12) | Not Class A | Via SWE.4 |

---

## 6. Common Themes Across All Standards

### 6.1 Universal Requirements

These elements appear in every standard surveyed, regardless of domain:

1. **Source code must implement the design.** Every standard requires that
   code is developed from and traceable to a design specification. The code
   is not a standalone artifact -- it is an implementation of documented
   decisions.

2. **Coding standards must be defined and applied.** No standard mandates a
   specific coding standard, but all require (or strongly recommend) that one
   exists, is documented, and is verifiable. The coding standard is a project
   artifact, not an afterthought.

3. **Code must be verified against design and standards.** Whether through
   reviews, static analysis, or formal methods, every standard requires
   evidence that code conforms to its inputs (design) and constraints
   (coding standards).

4. **Traceability is mandatory.** Code must be traceable upward to design
   elements and requirements. Orphan code (code not traceable to any
   requirement) is a finding in every standard.

5. **Rigor scales with criticality.** Every standard defines a criticality
   classification (DAL, ASIL, Class, TQL) and scales the required evidence
   accordingly. Higher criticality means more reviews, more independence,
   more formal methods, and more comprehensive traceability.

### 6.2 Strong Consensus (Most Standards)

These elements appear in most but not all standards:

1. **Language subsets** (DO-178C required, ISO 26262 ++, IEC 62304 suggested,
   ASPICE expected practice). The use of a defined language subset to eliminate
   ambiguous, undefined, or dangerous language constructs is near-universal.

2. **Complexity limits** (DO-178C via coding standard, ISO 26262 ++ all ASILs,
   IEC 62304 suggested). Enforcing low complexity -- measured by cyclomatic
   complexity, nesting depth, function length, etc. -- is a consistent theme.

3. **Defensive coding** (ISO 26262 explicit, DO-178C via coding standard,
   IEC 62304 Class C explicit). Techniques like input validation, boundary
   checking, resource management, and error handling are expected across all
   standards, though the explicitness varies.

4. **Static analysis** (ISO 26262 ++ all ASILs, DO-178C recognized method,
   IEC 62304 recognized, ASPICE expected). Automated static analysis is
   increasingly treated as a baseline verification method, not an optional
   enhancement.

5. **Code review with checklists** (DO-178C explicit, ISO 26262 inspection
   ++ for ASIL C/D, IEC 62304 Class B/C, ASPICE expected). Human review of
   source code remains a requirement, with increasing independence and
   formality at higher criticality levels.

### 6.3 Key Differences

| Topic | Aviation (DO-178C/DO-330) | Automotive (ASPICE/ISO 26262) | Medical (IEC 62304) |
|-------|--------------------------|-------------------------------|---------------------|
| Dead code rules | Explicit, strict | Not as specific | Not explicit |
| Object code verification | DAL A/B required | Not required | Not required |
| Design-before-code | Expected (verified by review) | Enforced (assessor checks) | Not enforced |
| Notation formality | Language-agnostic | ASIL-dependent (semi-formal/formal for C/D) | Not specified |
| Tool qualification | Full framework (DO-330) | Tool confidence levels (Part 8) | Tool validation (IEC 62304 5.6.7) |
| Independence | DAL A/B: required | ASIL C/D: recommended | Not explicit |

### 6.4 The Minimum Implementation Checklist

Based on the common requirements across all standards, any V-model-compliant
implementation activity should ensure:

- [ ] Coding standard is documented and approved before coding begins
- [ ] Language subset is defined (prohibited/restricted features identified)
- [ ] Complexity limits are defined (cyclomatic complexity, nesting, function size)
- [ ] Source code is developed from (not before) detailed design
- [ ] Each source code unit traces to at least one design element
- [ ] Code review has been performed (method appropriate to criticality)
- [ ] Static analysis has been performed against the coding standard
- [ ] No orphan code exists (all code traces to a requirement via design)
- [ ] Error handling follows documented patterns
- [ ] Coding standard compliance is demonstrated (analysis report)
- [ ] Code review records are retained as verification evidence

---

## 7. Implications for VModelWorkflow

### 7.1 Schema Needs

An implementation/coding artifact schema should capture:

1. **Coding standard reference** -- which standard, which version, any
   project-specific deviations
2. **Language subset definition** -- prohibited features, restricted features
   with justification requirements
3. **Complexity thresholds** -- maximum cyclomatic complexity, nesting depth,
   function/file length
4. **Code review requirements** -- method (walkthrough, inspection, formal),
   independence level, checklist reference
5. **Static analysis configuration** -- tool, ruleset, deviation process
6. **Traceability evidence** -- links from source units to design elements

### 7.2 Craft Skill Needs

Implementation-focused craft skills should guide:

1. **Coding standard selection** -- Help teams choose and configure appropriate
   standards (MISRA, CERT, AUTOSAR C++, language-specific equivalents)
2. **Code review execution** -- Structured review against standard objectives,
   checklist generation, finding documentation
3. **Static analysis interpretation** -- Triage findings, document deviations,
   link findings to design
4. **Traceability annotation** -- How to embed or maintain design-to-code
   trace links

### 7.3 Traceability Engine Needs

The traceability engine (Pillar 2) needs to support:

1. **Design-to-code links** -- Validate that every code unit traces to a
   design element
2. **Orphan code detection** -- Identify code not traceable to any requirement
3. **Dead code flagging** -- For aviation projects, identify code with no
   execution path
4. **Coding standard compliance status** -- Track which units have been
   verified against the coding standard

---

## Appendix A: Language Subset Standards Reference

| Standard | Language | Domain Focus | Key Characteristics |
|----------|----------|-------------|-------------------|
| MISRA C:2023 | C | Cross-domain safety | 175 rules (mandatory + required + advisory), defines safe C subset |
| MISRA C++:2023 | C++ | Cross-domain safety | Updated for C++17, replaces 2008 edition |
| AUTOSAR C++14 | C++ | Automotive | Based on MISRA C++, extended for AUTOSAR |
| JSF++ AV | C++ | Aviation/defense | Created for F-35, strict subset |
| CERT C | C | Security-focused | Recommendations + rules for secure coding |
| CERT C++ | C++ | Security-focused | Secure coding practices for C++ |
| BARR-C:2018 | C | Embedded systems | Coding style + safety rules |
| HIC++ | C++ | High-integrity | Derived from MISRA C++ |

**Note on Java and other languages:** The V-model standards are language-agnostic.
For Java projects (like our pilot target), equivalent practices include:
- Checkstyle, PMD, SpotBugs as static analysis tools
- SonarQube rulesets as a "language subset" equivalent
- Google Java Style Guide or similar as coding standard
- Complexity metrics via PMD/SonarQube (cyclomatic complexity, cognitive complexity)

The principle remains the same: define what is allowed, what is prohibited,
and verify compliance. The specific tool ecosystem differs by language.

---

## Appendix B: Source Classification

### Official / Highly Authoritative
- RTCA DO-178C, DO-330 (standard documents themselves)
- ISO 26262:2018 Part 6 (standard document)
- IEC 62304:2006+AMD1:2015 (standard document)
- Automotive SPICE PAM 3.1/4.0 (process assessment model)
- UL Solutions SWE.3 guides (ASPICE assessment body)
- NASA publications on DO-178C

### Tool Vendors / Consultancies (Practitioner Knowledge)
- AdaCore (DO-178C expertise, tool vendor)
- LDRA (verification tools, multi-standard)
- Parasoft (static analysis, multi-standard)
- Rapita Systems (verification tools, aviation focus)
- AFuzion (DO-178C/DO-330 certification consultancy)
- Perforce/QA Systems (static analysis, ISO 26262)
- Black Duck/Synopsys (static analysis, ISO 26262)

### Community / General Reference
- Wikipedia articles for standard overviews
- LinkedIn practitioner articles
- Medium practitioner articles

---

*Research compiled from publicly available information about copyrighted
standards. The actual standard documents are published by RTCA (DO-178C,
DO-330), ISO (26262), IEC (62304), and VDA (ASPICE). Consult official
documents for authoritative and complete requirements. Web search was used
to gather and cross-reference information from multiple practitioner and
vendor sources.*
