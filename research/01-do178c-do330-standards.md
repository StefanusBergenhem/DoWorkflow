# DO-178C, DO-330, and DO-278A Standards Research

## Document Purpose

This document provides a comprehensive reference on the RTCA DO-178C, DO-330, and DO-278A
standards for aviation software. It focuses on concrete objectives, required artifacts, processes,
and their relationships -- intended to inform tooling and workflow design for aviation software
development environments.

**Note:** The DO-178C, DO-330, and DO-278A documents are copyrighted by RTCA, Inc. This research
summarizes publicly available information about the standards' structure and requirements. For
authoritative and complete details, refer to the official RTCA publications.

---

## 1. DO-178C Overview

**Full Title:** DO-178C, "Software Considerations in Airborne Systems and Equipment Certification"

**Published:** December 2011 by RTCA (supersedes DO-178B, published 1992)

**EUROCAE Equivalent:** ED-12C

**Purpose:** Provides guidelines for the production of airborne software that performs its intended
function with a level of confidence in safety that complies with airworthiness requirements. It is
the primary means of compliance for software aspects of airborne system certification, referenced
by FAA Advisory Circular AC 20-115D and EASA AMC 20-115.

### 1.1 The V-Model Lifecycle Structure

DO-178C follows a V-model lifecycle, though it does not mandate a specific development methodology.
The standard defines **processes** rather than phases, meaning activities can overlap and iterate.
The key processes are:

```
Planning Processes
    |
    v
Development Processes
    |---> Requirements Process (System -> High-Level -> Low-Level)
    |---> Design Process
    |---> Coding Process
    |---> Integration Process
    |
    v
Integral Processes (run concurrently throughout)
    |---> Verification Process
    |---> Configuration Management Process
    |---> Quality Assurance Process
    |---> Certification Liaison Process
```

#### 1.1.1 Planning Processes (Section 4)

Planning establishes the software development and integral process activities. Planning produces:
- **Plan for Software Aspects of Certification (PSAC)** -- the top-level document describing how
  the applicant will comply with DO-178C
- **Software Development Plan (SDP)**
- **Software Verification Plan (SVP)**
- **Software Configuration Management Plan (SCMP)**
- **Software Quality Assurance Plan (SQAP)**

Planning defines:
- The software lifecycle(s) to be used
- The development environment (tools, hardware, compilers)
- Standards for requirements, design, code
- Transition criteria between processes
- How traceability will be achieved

#### 1.1.2 Development Processes (Section 5)

The development processes transform system requirements allocated to software into a verified
implementation:

**Software Requirements Process (Section 5.1)**
- Develops **High-Level Requirements (HLR)** from system requirements allocated to software
- HLRs describe software behavior, performance, and safety constraints
- HLRs must be verifiable, conformant, and traceable to system requirements

**Software Design Process (Section 5.2)**
- Develops **Low-Level Requirements (LLR)** and software architecture from HLRs
- LLRs are requirements from which Source Code can be directly implemented without further
  information
- Architecture describes the software structure and data/control flow between components

**Software Coding Process (Section 5.3)**
- Implements Source Code from LLRs and architecture
- Source Code must conform to coding standards
- Source Code must be traceable to LLRs

**Integration Process (Section 5.4)**
- Combines software components into the integrated software
- Loads software into target hardware
- Produces the Executable Object Code

#### 1.1.3 Integral Processes

**Verification Process (Section 6)** -- see Section 5 below for detail.

**Software Configuration Management Process (Section 7)**
- Identification: unique identification of configuration items
- Baselines: establishing controlled baselines for lifecycle data
- Change control: managing changes with problem reports and change requests
- Status accounting: recording and reporting CM status
- Archive and retrieval
- Release: controlling software product release
- Data control categories based on DAL (CC1 and CC2)

**Software Quality Assurance Process (Section 8)**
- Ensures processes comply with approved plans and standards
- Performs audits and reviews of processes and outputs
- Monitors transition criteria
- Ensures deviations are detected, recorded, evaluated, tracked, and resolved
- SQA has organizational independence from development

**Certification Liaison Process (Section 9)**
- Communication with the certification authority
- Establishing the means of compliance
- Compliance substantiation
- Managing the certification basis

### 1.2 V-Model Diagram (Conceptual)

```
System Requirements                                    System Verification
       \                                                    /
        v                                                  ^
    High-Level Requirements (HLR)          Verification of HLR (Tests)
            \                                          /
             v                                        ^
         Low-Level Requirements (LLR)      Verification of LLR (Tests)
                 \                                /
                  v                              ^
              Source Code                  Code Reviews/Analysis
                      \                      /
                       v                    ^
                    Integration ---------> Testing
                                    (on target hardware)
```

---

## 2. Design Assurance Levels (DAL A through E)

The Design Assurance Level is determined by the **system safety assessment process** (per ARP 4754A
and ARP 4761), not by DO-178C itself. DAL is assigned based on the severity of the failure
condition that the software could contribute to:

| DAL | Failure Condition Severity | Description |
|-----|---------------------------|-------------|
| **A** | Catastrophic | Failure conditions that would prevent continued safe flight and landing. All occupants presumed fatally injured. |
| **B** | Hazardous / Severe-Major | Large reduction in safety margins or functional capabilities. Serious or fatal injuries to a small number of occupants. |
| **C** | Major | Significant reduction in safety margins or functional capabilities. Physical discomfort to occupants. Significant increase in crew workload. |
| **D** | Minor | Slight reduction in safety margins. Slight increase in crew workload. Some inconvenience to occupants. |
| **E** | No Effect | No effect on aircraft operational capability or pilot workload. |

### 2.1 Objective Counts by DAL

DO-178C Annex A contains a table of **71 objectives** across all processes. The number of
**applicable** objectives varies by DAL:

| DAL | Applicable Objectives | Objectives with Independence | Notes |
|-----|----------------------|------------------------------|-------|
| **A** | 71 | 30 | All objectives apply; highest independence requirements |
| **B** | 69 | 18 | Nearly all objectives; reduced independence |
| **C** | 62 | 5 | Significant reduction in both count and independence |
| **D** | 26 | 2 | Substantial reduction; many verification objectives not applicable |
| **E** | 0 | 0 | No software objectives; only need to declare DAL E |

**Independence** means the verification activity must be performed by someone other than the
developer of the item being verified. At DAL A, many reviews and analyses require independence.
At DAL D, very few do.

### 2.2 Key Differences Across DALs

| Activity | DAL A | DAL B | DAL C | DAL D |
|----------|-------|-------|-------|-------|
| Requirements-based testing | Yes | Yes | Yes | Yes |
| Structural coverage (Statement) | Yes | Yes | Yes | No |
| Structural coverage (Decision) | Yes | Yes | No | No |
| Structural coverage (MC/DC) | Yes | No | No | No |
| Robustness testing (abnormal) | Yes | Yes | Yes | No |
| HLR review with independence | Yes | Yes | No | No |
| LLR review with independence | Yes | No | No | No |
| Source Code review with independence | Yes | No | No | No |
| Architecture review with independence | Yes | Yes | No | No |
| Test case/procedure review w/ independence | Yes | No | No | No |
| Traceability: HLR to System Req | Yes | Yes | Yes | Yes |
| Traceability: LLR to HLR | Yes | Yes | Yes | No |
| Traceability: Source to LLR | Yes | Yes | Yes | No |
| Traceability: Test Cases to HLR | Yes | Yes | Yes | Yes |
| Traceability: Test Cases to LLR | Yes | Yes | Yes | No |

---

## 3. Annex A Objectives by Process

The objectives in Annex A (Table A-1 through A-10) are organized by process. Below is a summary
of the tables and their focus areas.

### Table A-1: Software Planning Process (7 objectives)
- Objectives related to defining and documenting:
  - Software development standards (requirements, design, code)
  - Software plans (SDP, SVP, SCMP, SQAP)
  - Transition criteria
  - Development and verification environments

### Table A-2: Software Development Process (7 objectives)
- High-level requirements comply with system requirements
- High-level requirements are accurate and consistent
- Compatible with target computer
- Verifiable and conforming to standards
- Traceable to system requirements
- Algorithms are accurate
- Software architecture is compatible, consistent, and verifiable

### Table A-3: Verification of Outputs of Software Requirements Process (7 objectives)
- HLR compliance with system requirements
- HLR accuracy and consistency
- Compatibility with target computer
- Verifiability
- Conformance to standards
- Traceability to system requirements
- Algorithm accuracy

### Table A-4: Verification of Outputs of Software Design Process (13 objectives)
- LLR compliance with HLR
- LLR accuracy and consistency
- Compatibility with target computer
- Verifiability and conformance to standards
- Traceability to HLR
- Algorithm accuracy
- Architecture compatibility, consistency, verifiability
- Architecture conformance to standards
- Partitioning integrity (if used)

### Table A-5: Verification of Outputs of Software Coding & Integration (9 objectives)
- Source Code compliance with LLR and architecture
- Source Code verifiability, conformance, accuracy
- Source Code traceability to LLR
- Executable Object Code compliance with HLR
- Executable Object Code robustness with HLR
- Executable Object Code compatibility with target computer

### Table A-6: Testing of Outputs of Integration Process (5 objectives)
- Executable Object Code complies with HLR (normal range)
- Executable Object Code robustness with HLR (robustness testing)
- Executable Object Code complies with LLR (normal range)
- Executable Object Code robustness with LLR
- Executable Object Code is compatible with target computer

### Table A-7: Verification of Verification Process Results (9 objectives)
- Test procedures are correct
- Test results are correct and discrepancies explained
- Test coverage of HLR is achieved
- Test coverage of LLR is achieved
- Test coverage of software structure (Statement) is achieved
- Test coverage of software structure (Decision) is achieved
- Test coverage of software structure (MC/DC) is achieved
- Test coverage of software structure (data coupling and control coupling) is achieved
- Verification of additional code not traceable to requirements (dead code analysis)

### Table A-8: Software Configuration Management (6 objectives)
- Configuration items are identified
- Baselines and traceability are established
- Problem reporting, tracking, and corrective action
- Change control integrity
- Change review
- Configuration status accounting

### Table A-9: Software Quality Assurance (5 objectives)
- Assurance of compliance with plans and standards
- Assurance of transition criteria satisfaction
- SQA independence (organizational independence from development)
- Deviation and nonconformance handling
- SQA of software lifecycle processes

### Table A-10: Certification Liaison (3 objectives)
- Communication and understanding of certification basis
- Means of compliance proposal
- Compliance substantiation

---

## 4. DO-178C Lifecycle Data Items (Artifacts)

DO-178C Section 11 specifies the lifecycle data that must be produced and submitted or made
available to the certification authority. These are organized by type:

### 4.1 Planning Data

| Artifact | Section | Description | CC1/CC2 |
|----------|---------|-------------|---------|
| **Plan for Software Aspects of Certification (PSAC)** | 11.1 | Top-level compliance roadmap; describes system overview, software overview, certification considerations, software lifecycle, development environment, and how DO-178C will be satisfied. Submitted to certification authority. | CC1 |
| **Software Development Plan (SDP)** | 11.2 | Describes development processes, methods, tools, standards, lifecycle model, and development environment. | CC2 |
| **Software Verification Plan (SVP)** | 11.3 | Describes verification processes, methods, tools, environment, transition criteria, and activities for each verification activity. | CC2 |
| **Software Configuration Management Plan (SCMP)** | 11.4 | Describes CM processes: identification, baselines, change control, status accounting, archive, retrieval, release, environment control. | CC2 |
| **Software Quality Assurance Plan (SQAP)** | 11.5 | Describes QA processes, authority, independence, scope, activities, records, and interaction with CM. | CC2 |

### 4.2 Development Data

| Artifact | Section | Description | CC1/CC2 |
|----------|---------|-------------|---------|
| **Software Requirements Standards (SRS)** | 11.6 | Standards for developing HLR -- methods, rules, tools, notation constraints. | CC2 |
| **Software Design Standards (SDS)** | 11.7 | Standards for software design -- design methods, naming conventions, complexity restrictions, constraints on design. | CC2 |
| **Software Code Standards (SCS)** | 11.8 | Standards for coding -- language restrictions, coding rules, complexity restrictions. | CC2 |
| **Software Requirements Data (SRD)** | 11.9 | The high-level requirements themselves. | CC1 |
| **Software Design Description (SDD)** | 11.10 | The low-level requirements and software architecture. | CC1 (A,B,C) / CC2 (D) |
| **Source Code** | 11.11 | The code implementing the design. | CC1 (A,B,C) / CC2 (D) |
| **Executable Object Code (EOC)** | 11.12 | The compiled/linked software loaded on the target. | CC1 |

### 4.3 Verification Data

| Artifact | Section | Description | CC1/CC2 |
|----------|---------|-------------|---------|
| **Software Verification Cases and Procedures (SVCP)** | 11.13 | Test cases (inputs, expected results, pass/fail criteria) and test procedures (step-by-step instructions). Includes review/analysis cases. | CC1 (A,B) / CC2 (C,D) |
| **Software Verification Results (SVR)** | 11.14 | Results of test execution, reviews, and analyses. | CC1 (A,B) / CC2 (C,D) |

### 4.4 Configuration Management Data

| Artifact | Section | Description | CC1/CC2 |
|----------|---------|-------------|---------|
| **Software Life Cycle Environment Configuration Index (SECI)** | 11.15 | Identifies the tools, qualified tools, hardware, and environment used for development and verification. | CC1 |
| **Software Configuration Index (SCI)** | 11.16 | Identifies the configuration of the software product: source code, EOC, and related data items with their versions. | CC1 |
| **Problem Reports (PRs)** | 11.17 | Records of problems discovered, their disposition, and corrective actions. | CC1 |

### 4.5 Quality Assurance Data

| Artifact | Section | Description | CC1/CC2 |
|----------|---------|-------------|---------|
| **Software Quality Assurance Records (SQAR)** | 11.18 | Records of QA activities: audit results, review records, conformity findings, deviation records. | CC2 |

### 4.6 Certification Liaison Data

| Artifact | Section | Description | CC1/CC2 |
|----------|---------|-------------|---------|
| **Software Accomplishment Summary (SAS)** | 11.19 | Summary of the software lifecycle activities, compliance statement, and certification data. Submitted to the certification authority. A companion to the PSAC. | CC1 |

### 4.7 Control Categories (CC1 vs CC2)

- **CC1 (Control Category 1):** Data items under full configuration management with formal change
  control -- baselines established, changes require formal review and approval, full traceability
  maintained.
- **CC2 (Control Category 2):** Data items under configuration management but with reduced rigor --
  identified, tracked, and retrievable but with less formal change control than CC1.

---

## 5. Verification Objectives (DO-178C Section 6)

The verification process is the most extensive section of DO-178C. It encompasses:

### 5.1 Verification Methods

DO-178C defines four verification methods:

1. **Reviews** -- qualitative assessment of correctness, including peer reviews, inspections, and
   walkthroughs. Reviews assess compliance, accuracy, consistency, verifiability, traceability,
   and conformance to standards.

2. **Analyses** -- examination providing repeatable evidence of correctness. More rigorous than
   reviews; includes:
   - Traceability analysis
   - Coverage analysis (requirements and structural)
   - Data flow and control flow analysis
   - Worst-case execution timing analysis
   - Memory usage analysis (stack, heap)
   - Complexity analysis (cyclomatic complexity, etc.)

3. **Testing** -- execution of the software with defined inputs and comparison against expected
   results. Testing is the primary verification method.

4. **Service Experience** -- using prior operational history (rarely used as a sole method).

### 5.2 Reviews and Analyses of Requirements

- **Compliance:** HLRs comply with system requirements; LLRs comply with HLRs
- **Accuracy and Consistency:** Requirements are unambiguous, non-conflicting, mathematically
  correct where algorithms are specified
- **Compatibility with Target Computer:** Requirements consider hardware characteristics
  (word sizes, I/O limitations, timing, memory constraints)
- **Verifiability:** Each requirement can be verified (testable or analyzable)
- **Conformance to Standards:** Requirements follow the Requirements Standards
- **Traceability:** Each requirement traces to its source; each derived requirement is identified
  and fed back to the system process
- **Algorithm Accuracy:** Complex algorithms are mathematically verified

### 5.3 Requirements-Based Testing

This is a core principle of DO-178C. Testing must be **requirements-based**, meaning:

- Every test case traces to one or more requirements (HLR or LLR)
- Tests exercise **normal range** inputs (within specified behavior)
- Tests exercise **robustness** cases (behavior at boundaries, abnormal inputs, failure modes)
- Tests are conducted on the **target hardware** (or an equivalent justified environment)

#### Normal Range Testing
- Tests that exercise the software within the requirements' specified operating conditions
- Tests verify correct behavior for valid inputs and expected operational scenarios

#### Robustness Testing
- Tests that exercise behavior beyond normal range:
  - Boundary values
  - Invalid inputs
  - Failure modes and error handling
  - Timing edge cases
  - Resource exhaustion scenarios

### 5.4 Structural Coverage Analysis

After requirements-based testing is complete, structural coverage analysis determines whether
the code structure has been adequately exercised:

| Coverage Criterion | DAL A | DAL B | DAL C | DAL D |
|-------------------|-------|-------|-------|-------|
| **Statement Coverage (SC)** | Required | Required | Required | Not required |
| **Decision Coverage (DC)** | Required | Required | Not required | Not required |
| **Modified Condition/Decision Coverage (MC/DC)** | Required | Not required | Not required | Not required |

**Statement Coverage:** Every statement in the program has been invoked at least once.

**Decision Coverage:** Every decision (if/else, switch, loop) has taken all possible outcomes
(true and false) at least once. Subsumes statement coverage.

**Modified Condition/Decision Coverage (MC/DC):** Every condition within a decision has been
shown to independently affect the outcome of the decision. This is the most rigorous criterion.
Each condition in a decision must be varied while holding all other conditions fixed, and the
overall decision outcome must change. MC/DC subsumes both decision coverage and statement coverage.

**Data Coupling and Control Coupling Analysis (DAL A):** Verifies that the data passed between
software components is used and defined properly (data coupling) and that the sequence and
conditions of control transfer between components is correct (control coupling).

#### Handling Coverage Gaps

If structural coverage analysis reveals code not exercised by requirements-based tests:
1. **Missing test cases:** requirements-based tests were insufficient -- add more tests
2. **Inadequate requirements:** requirements are incomplete -- add/modify requirements, then test
3. **Dead code:** code that cannot be reached -- must be removed (dead code is not allowed)
4. **Deactivated code:** code intentionally disabled for certain configurations -- must be
   verified or justified

### 5.5 Independence in Verification

Independence means the person performing the verification activity is not the person who
developed the item being verified. The degree of independence required depends on the DAL:

- **DAL A:** Independence required for most reviews, analyses, and test case development
- **DAL B:** Independence required for many reviews and analyses, fewer than DAL A
- **DAL C:** Independence required for a few critical activities
- **DAL D:** Minimal independence requirements
- **DAL E:** No requirements

Independence does not require a separate organization -- it requires a different person who has
the authority and qualifications to perform the verification activity without undue influence.

---

## 6. Traceability Requirements

Traceability is a fundamental principle throughout DO-178C, providing evidence that all
requirements are implemented and all implementation elements trace to requirements.

### 6.1 Required Traceability Chains

DO-178C requires the following bidirectional traces:

```
System Requirements
       ^  |
       |  v
High-Level Requirements (HLR)
       ^  |          ^  |
       |  v          |  v
Low-Level Requirements (LLR)    Test Cases (HLR-level)
       ^  |          ^  |
       |  v          |  v
   Source Code       Test Cases (LLR-level)
       |
       v
Executable Object Code
```

#### Forward Traceability (Top-Down)
1. **System Requirements -> HLR:** Every system requirement allocated to software is addressed
   by one or more HLRs
2. **HLR -> LLR:** Every HLR is refined into one or more LLRs
3. **LLR -> Source Code:** Every LLR is implemented in Source Code
4. **HLR -> Test Cases:** Every HLR has at least one test case verifying it
5. **LLR -> Test Cases:** Every LLR has at least one test case verifying it (DAL A, B, C)

#### Backward Traceability (Bottom-Up)
1. **Source Code -> LLR:** Every Source Code element traces back to an LLR (identifies code
   not traceable to requirements -- potential dead code or missing requirements)
2. **LLR -> HLR:** Every LLR traces back to an HLR (identifies derived LLRs)
3. **HLR -> System Requirements:** Every HLR traces back to a system requirement (identifies
   derived HLRs)
4. **Test Cases -> HLR:** Every test case traces to the HLR(s) it verifies
5. **Test Cases -> LLR:** Every test case traces to the LLR(s) it verifies

### 6.2 Derived Requirements

A **derived requirement** is a requirement that does not directly trace to a higher-level
requirement. Derived requirements arise from the design process (e.g., initialization sequences,
error handling not specified at system level, internal data structure choices).

Derived requirements are legitimate but must be:
- Identified explicitly as derived
- Fed back to the system safety assessment process (because they may introduce new failure modes)
- Verified like any other requirement

### 6.3 Traceability Data Applicability by DAL

| Trace | DAL A | DAL B | DAL C | DAL D |
|-------|-------|-------|-------|-------|
| System Req -> HLR | Yes | Yes | Yes | Yes |
| HLR -> LLR | Yes | Yes | Yes | No |
| LLR -> Source Code | Yes | Yes | Yes | No |
| HLR -> Test Cases | Yes | Yes | Yes | Yes |
| LLR -> Test Cases | Yes | Yes | Yes | No |
| Source Code -> LLR (reverse) | Yes | Yes | Yes | No |

---

## 7. DO-330: Software Tool Qualification Considerations

**Full Title:** DO-330, "Software Tool Qualification Considerations"

**Published:** December 2011 (companion to DO-178C)

**EUROCAE Equivalent:** ED-215

**Purpose:** Provides guidelines for qualifying software development and verification tools used
in the DO-178C (and DO-278A) lifecycle. DO-330 replaces the tool qualification guidance that was
previously in Section 12.2 of DO-178B.

### 7.1 Why Tool Qualification?

When a tool is used in the software lifecycle, its output may affect the airborne software.
If a tool has an error, it could:
- **Insert errors** into the software (e.g., a compiler that generates incorrect object code)
- **Fail to detect errors** that the tool's output claims to verify (e.g., a test tool that
  reports pass when the test actually fails)

If processes rely on a tool's output without independent verification of that output, then the
tool's correctness becomes critical. Tool qualification provides confidence that the tool
performs its intended function correctly.

### 7.2 When is Tool Qualification Required?

Tool qualification is required when a tool's output is used in the software lifecycle AND the
tool's output is not verified by another activity that would detect the tool's errors.

The determination is based on three criteria:

#### Criteria for Tool Qualification Assessment

**Criterion 1 -- Tool Output is Part of the Software:**
The tool output is part of the airborne software and thus could insert an error.
Examples: compilers, linkers, code generators, auto-code tools.

**Criterion 2 -- Tool Automates a Verification Activity and its Output is Used to Eliminate/Reduce
Another Verification Activity:**
The tool automates a process that could fail to detect an error, and the tool's output is used
in lieu of another verification activity.
Examples: static analyzers used to replace code reviews, test coverage tools used to claim
structural coverage, requirements-based test generators whose output is accepted without review.

**Criterion 3 -- Tool Output Could Not Insert an Error (and Does Not Automate Verification):**
The tool could fail to detect an error but its outputs are independently verified, OR the tool
has no impact on the airborne software.
Examples: text editors, configuration management tools, requirements management tools (when
outputs are reviewed), compilers (when the object code is fully verified by testing).

If a tool falls under Criterion 3, no tool qualification is needed (though its usage is still
identified in the SECI).

### 7.3 Tool Qualification Levels (TQL 1-5)

DO-330 defines five Tool Qualification Levels. The TQL is determined by the combination of the
**tool qualification criterion** (1, 2, or 3) and the **software DAL** of the airborne software
the tool is used for:

| | DAL A | DAL B | DAL C | DAL D |
|---|-------|-------|-------|-------|
| **Criterion 1** (output is part of software) | TQL-1 | TQL-2 | TQL-3 | TQL-4 |
| **Criterion 2** (automates/replaces verification) | TQL-4 | TQL-4 | TQL-5 | TQL-5 |
| **Criterion 3** (neither -- independently verified) | No qualification | No qualification | No qualification | No qualification |

#### TQL-1 (Most Rigorous)
- Equivalent to developing the tool as DAL A software
- All DO-330 objectives apply
- Full tool operational requirements, tool requirements, tool design, tool code reviews
- Full requirements-based testing of the tool
- Structural coverage (MC/DC) of the tool
- Full CM, QA, and independent verification

#### TQL-2
- Equivalent to developing the tool as DAL B software
- Most DO-330 objectives apply
- Requirements-based testing of the tool
- Structural coverage (Decision Coverage) of the tool
- Full CM and QA

#### TQL-3
- Equivalent to developing the tool as DAL C software
- Reduced objectives compared to TQL-2
- Requirements-based testing of the tool
- Structural coverage (Statement Coverage) of the tool

#### TQL-4
- Significantly reduced objectives
- Tool operational requirements must be defined
- Tool verification (testing against tool operational requirements)
- Reduced documentation requirements
- No structural coverage of the tool required

#### TQL-5 (Least Rigorous)
- Minimal objectives
- Tool operational requirements must be defined
- Verification that the tool satisfies its operational requirements
- Minimal documentation

### 7.4 DO-330 Lifecycle and Artifacts

DO-330 defines its own lifecycle for tool development/qualification, mirroring DO-178C's
structure but applied to the tool itself:

| DO-330 Artifact | Analogous DO-178C Artifact |
|----------------|---------------------------|
| Tool Qualification Plan (TQP) | PSAC |
| Tool Development Plan | SDP |
| Tool Verification Plan | SVP |
| Tool CM Plan | SCMP |
| Tool QA Plan | SQAP |
| Tool Operational Requirements (TOR) | HLR |
| Tool Requirements | LLR |
| Tool Design Description | SDD |
| Tool Source Code | Source Code |
| Tool Executable Object Code | EOC |
| Tool Verification Cases & Procedures | SVCP |
| Tool Verification Results | SVR |
| Tool Configuration Index | SCI |
| Tool Accomplishment Summary (TAS) | SAS |

### 7.5 Common Tool Qualification Scenarios

| Tool | Typical Criterion | Typical TQL (for DAL A software) | Notes |
|------|------------------|----------------------------------|-------|
| Compiler/Linker (output not fully tested) | 1 | TQL-1 | If object code is fully verified by testing, may reduce to Criterion 3 |
| Auto-code generator | 1 | TQL-1 | Output becomes part of the software |
| Static analysis tool (replacing review) | 2 | TQL-4 | Automates a verification activity |
| Test coverage tool | 2 | TQL-4 | Results used to claim structural coverage |
| Requirements management tool | 3 | None | Output reviewed by humans |
| Test execution framework | 2 | TQL-4 | If test pass/fail is accepted without independent check |
| Text editor | 3 | None | Cannot insert errors autonomously |
| Model checker/formal methods tool | 2 | TQL-4 | If replacing a verification activity |

### 7.6 DO-330 Key Principles

1. **Tool qualification is deterministic:** The criteria and DAL mechanically determine TQL.
2. **Tool qualification can be avoided:** If you independently verify the tool's output (e.g.,
   review all code generated by an auto-coder), you may not need to qualify the tool.
3. **Tool qualification is reusable:** A qualified tool can be reused across projects if the
   tool version, configuration, and operational environment remain the same.
4. **Development history can support qualification:** If a tool has a well-documented development
   history (even if not originally developed to DO-330), that history can be leveraged.

---

## 8. DO-278A: Software Integrity Assurance for Ground-Based Systems

**Full Title:** DO-278A, "Software Integrity Assurance Levels for CNS/ATM Systems and Equipment"

**Published:** December 2011

**EUROCAE Equivalent:** ED-109A

**Purpose:** Provides guidance for ground-based CNS/ATM (Communication, Navigation, Surveillance /
Air Traffic Management) software, analogous to what DO-178C provides for airborne software.

### 8.1 Key Differences from DO-178C

#### Assurance Levels (AL) vs Design Assurance Levels (DAL)

DO-278A uses **Software Assurance Levels (AL)** numbered 1-6 instead of DO-178C's DAL A-E:

| DO-278A AL | Approximate DO-178C DAL | Failure Impact |
|-----------|------------------------|----------------|
| AL 1 | DAL A | Failure could cause catastrophic effects on ATM operations |
| AL 2 | DAL B | Failure could cause hazardous effects |
| AL 3 | DAL C | Failure could cause major effects |
| AL 4 | DAL C/D | Failure could cause moderate effects |
| AL 5 | DAL D | Failure could cause minor effects |
| AL 6 | DAL E | No safety effect |

Note: DO-278A has **six levels** compared to DO-178C's five. The additional level (AL 4) provides
a finer granularity between Major and Minor, reflecting that ground-based systems have more
nuanced operational impact categories.

#### Scope and Applicability

| Aspect | DO-178C | DO-278A |
|--------|---------|---------|
| **Target systems** | Airborne software (onboard aircraft) | Ground-based CNS/ATM systems |
| **Certification authority** | FAA, EASA (airworthiness) | National aviation authorities (operational approval) |
| **Regulatory basis** | 14 CFR Part 25/23 airworthiness standards | National operational approval processes |
| **Safety assessment reference** | ARP 4754A / ARP 4761 | Local safety assessment standards |
| **Physical environment** | Constrained (aircraft environment, EMI, temperature, vibration) | Less constrained (ground facilities) |
| **Maintenance access** | Limited (in-flight, remote locations) | Generally accessible |
| **Update frequency** | Infrequent (requires STC or amendment) | More frequent updates possible |

#### Structural Coverage Differences

DO-278A has different structural coverage requirements compared to DO-178C:

| Coverage | DO-178C DAL A | DO-278A AL 1 |
|----------|-------------|-------------|
| MC/DC | Required | Required |
| Decision Coverage | Required | Required |
| Statement Coverage | Required | Required |

However, the mapping is not one-to-one for intermediate levels, and DO-278A provides more
flexibility in how coverage objectives can be satisfied for intermediate assurance levels.

#### Process Differences

1. **Operational aspects:** DO-278A places more emphasis on operational considerations because
   ground-based systems interact directly with ATM operations and human operators.

2. **Service history credit:** DO-278A provides more explicit guidance for using service history
   (prior operational experience) as a means of satisfying some objectives, reflecting that
   ground-based systems can accumulate operational data more readily.

3. **Availability considerations:** Ground-based ATM systems often have availability requirements
   that are not typically addressed in DO-178C. DO-278A acknowledges the role of redundancy,
   fault tolerance, and system availability in the safety argument.

4. **COTS/reuse:** DO-278A provides somewhat more guidance on the use of Commercial Off-The-Shelf
   (COTS) software and previously developed software (PDS), reflecting the reality that
   ground-based systems more frequently incorporate COTS components.

5. **Open Problem Reports:** The treatment of open problem reports at delivery may differ, as
   ground-based systems have different operational and maintenance paradigms.

#### Artifact Differences

DO-278A uses largely the same set of lifecycle data items as DO-178C but with some terminology
and emphasis differences:

| DO-178C Artifact | DO-278A Equivalent |
|-----------------|-------------------|
| PSAC (Plan for Software Aspects of Certification) | PSSAS (Plan for Software Aspects of System Assurance) |
| SAS (Software Accomplishment Summary) | SSAS (Software Aspects of System Assurance Summary) |
| SDP, SVP, SCMP, SQAP | Same names |
| SCI, SECI | Same names |

The PSAC/PSSAS naming difference reflects that ground-based systems undergo "assurance" or
"approval" rather than "certification" in the airworthiness sense.

---

## 9. DO-178C Technology Supplements

DO-178C is accompanied by four technology supplements that provide additional guidance for
specific development approaches:

| Supplement | Title | Purpose |
|-----------|-------|---------|
| **DO-331 / ED-218** | Model-Based Development and Verification | Guidance when models are used for development or verification |
| **DO-332 / ED-217** | Object-Oriented Technology and Related Techniques | Guidance for OOT (inheritance, polymorphism, dynamic dispatch) |
| **DO-333 / ED-216** | Formal Methods | Guidance for using formal methods as an alternative or complement to testing |
| **DO-330 / ED-215** | Software Tool Qualification Considerations | Tool qualification (covered in Section 7 above) |

These supplements **modify or extend** DO-178C objectives when the corresponding technology is
used. They add objectives and modify existing ones rather than replacing DO-178C.

---

## 10. Summary: Compliance Checklist Structure

For a DO-178C project, the following represents the minimum compliance structure:

### Planning Phase Deliverables
- [ ] PSAC -- approved by certification authority
- [ ] SDP -- defines development processes
- [ ] SVP -- defines verification processes
- [ ] SCMP -- defines configuration management
- [ ] SQAP -- defines quality assurance
- [ ] Software Requirements Standards
- [ ] Software Design Standards
- [ ] Software Code Standards

### Development Phase Deliverables
- [ ] High-Level Requirements (HLR) traceable to system requirements
- [ ] Low-Level Requirements (LLR) traceable to HLR
- [ ] Software Architecture description
- [ ] Source Code traceable to LLR
- [ ] Executable Object Code

### Verification Phase Deliverables
- [ ] Verification Cases and Procedures (test cases, review checklists)
- [ ] Verification Results (test results, review records)
- [ ] Traceability Data (requirements <-> design <-> code <-> tests)
- [ ] Structural Coverage Analysis results
- [ ] Review/analysis records for HLR, LLR, architecture, code

### Configuration Management Deliverables
- [ ] Software Configuration Index (SCI)
- [ ] Software Life Cycle Environment Configuration Index (SECI)
- [ ] Problem Reports (all PRs with disposition)

### Quality Assurance Deliverables
- [ ] SQA Records (audit findings, conformity records)

### Certification Liaison Deliverables
- [ ] Software Accomplishment Summary (SAS) -- submitted to certification authority

### Tool Qualification (if applicable)
- [ ] Tool Qualification Plan
- [ ] Tool Operational Requirements
- [ ] Tool verification evidence
- [ ] Tool Accomplishment Summary

---

## 11. References

- RTCA DO-178C, "Software Considerations in Airborne Systems and Equipment Certification," December 2011
- RTCA DO-330, "Software Tool Qualification Considerations," December 2011
- RTCA DO-278A, "Software Integrity Assurance Levels for CNS/ATM Systems and Equipment," December 2011
- RTCA DO-331, "Model-Based Development and Verification Supplement to DO-178C and DO-278A," December 2011
- RTCA DO-332, "Object-Oriented Technology and Related Techniques Supplement to DO-178C and DO-278A," December 2011
- RTCA DO-333, "Formal Methods Supplement to DO-178C and DO-278A," December 2011
- SAE ARP 4754A, "Guidelines for Development of Civil Aircraft and Systems," December 2010
- SAE ARP 4761, "Guidelines and Methods for Conducting the Safety Assessment Process on Civil Airborne Systems and Equipment," December 1996
- FAA Advisory Circular AC 20-115D, "Airborne Software Development Assurance Using EUROCAE ED-12() and RTCA DO-178()"
- EASA AMC 20-115, "Software Considerations for Airborne Systems and Equipment Certification"
