# ASPICE and ISO 26262: Common Patterns with DO-178C

## Purpose

This document analyzes Automotive SPICE (ASPICE) and ISO 26262 standards,
identifying structural and conceptual patterns shared with aviation's DO-178C.
The goal is to extract the common V-model abstractions that could form the
foundation of a reusable, domain-agnostic safety-critical development framework.

---

## 1. ASPICE Process Reference Model -- The V-Model Structure

### 1.1 Overview

Automotive SPICE (ASPICE) is a process assessment model derived from ISO/IEC
15504 (now ISO/IEC 33000 series). It defines a process reference model (PRM)
and a process assessment model (PAM) used to evaluate the maturity and
capability of software development processes in the automotive industry.

ASPICE organizes its processes into a V-model structure with two primary
process groups relevant to product development:

- **SYS (System Engineering)** -- system-level processes
- **SWE (Software Engineering)** -- software-level processes

### 1.2 System Engineering Processes (SYS)

| Process ID | Process Name                     | V-Model Position       |
|------------|----------------------------------|------------------------|
| SYS.1      | Requirements Elicitation         | Left side, top         |
| SYS.2      | System Requirements Analysis     | Left side, top         |
| SYS.3      | System Architectural Design      | Left side, upper       |
| SYS.4      | System Integration and Testing   | Right side, upper      |
| SYS.5      | System Qualification Testing     | Right side, top        |

### 1.3 Software Engineering Processes (SWE)

| Process ID | Process Name                            | V-Model Position       |
|------------|-----------------------------------------|------------------------|
| SWE.1      | Software Requirements Analysis          | Left side, mid         |
| SWE.2      | Software Architectural Design           | Left side, mid-lower   |
| SWE.3      | Software Detailed Design and Unit Const. | Left side, bottom     |
| SWE.4      | Software Unit Verification              | Right side, bottom     |
| SWE.5      | Software Integration and Integration Test| Right side, mid-lower |
| SWE.6      | Software Qualification Testing          | Right side, mid        |

### 1.4 Supporting and Management Processes

Beyond the V-model product engineering processes, ASPICE also defines:

- **SUP (Support) processes**: SUP.1 (Quality Assurance), SUP.2 (Verification),
  SUP.7 (Documentation), SUP.8 (Configuration Management), SUP.9 (Problem
  Resolution Management), SUP.10 (Change Request Management)
- **MAN (Management) processes**: MAN.3 (Project Management), MAN.5 (Risk
  Management), MAN.6 (Measurement)
- **ACQ (Acquisition) processes**: ACQ.4 (Supplier Monitoring)
- **REU (Reuse) processes**: REU.2 (Reuse Program Management)

### 1.5 Capability Levels

ASPICE assesses each process against capability levels (0-5):

| Level | Name          | Description                                        |
|-------|---------------|----------------------------------------------------|
| 0     | Incomplete    | Process not implemented or fails to achieve purpose |
| 1     | Performed     | Process achieves its purpose                        |
| 2     | Managed       | Process is planned, monitored, and adjusted         |
| 3     | Established   | Process uses a defined standard process              |
| 4     | Predictable   | Process operates within defined limits               |
| 5     | Innovating    | Process is continuously improved                     |

Most automotive OEMs require their suppliers to achieve **Level 2 or Level 3**
across the key SYS and SWE processes.

---

## 2. ISO 26262 ASIL Levels and Mapping to DO-178C DAL Levels

### 2.1 ISO 26262 ASIL Classification

ISO 26262 ("Road vehicles -- Functional safety") defines Automotive Safety
Integrity Levels (ASILs) based on three risk parameters:

- **Severity** (S0-S3): severity of harm to persons
- **Exposure** (E0-E4): probability of exposure to hazardous situation
- **Controllability** (C0-C3): ability of the driver to control the situation

The resulting ASIL levels, from lowest to highest rigor:

| ASIL Level | Rigor       | Typical Application Examples                    |
|------------|-------------|------------------------------------------------|
| QM         | Quality Mgmt| Non-safety-related (standard quality only)      |
| ASIL A     | Lowest      | Rear lights, comfort features with minor safety |
| ASIL B     | Moderate    | Headlights, indicators                          |
| ASIL C     | High        | Brake lights, stability control components      |
| ASIL D     | Highest     | Airbags, ABS, steering, powertrain safety       |

### 2.2 DO-178C DAL Classification

DO-178C defines Design Assurance Levels (DALs) based on failure condition
severity:

| DAL Level | Failure Condition  | Description                              |
|-----------|--------------------|------------------------------------------|
| DAL E     | No Effect          | No safety effect                         |
| DAL D     | Minor              | Slight reduction in safety margins       |
| DAL C     | Major              | Significant reduction in safety margins  |
| DAL B     | Hazardous          | Large reduction in safety, serious injury|
| DAL A     | Catastrophic       | Hull loss or multiple fatalities         |

### 2.3 Conceptual Mapping: ASIL to DAL

The two classification schemes are **not formally equivalent** -- they arise
from different hazard analysis methodologies and operate in different regulatory
contexts. However, a conceptual alignment exists based on rigor of required
evidence and verification activities:

| Automotive (ISO 26262)  | Aviation (DO-178C)  | Rigor Alignment     |
|-------------------------|---------------------|---------------------|
| QM (Quality Management) | DAL E (No Effect)   | Minimal / None      |
| ASIL A                  | DAL D (Minor)       | Low                 |
| ASIL B                  | DAL C (Major)       | Moderate            |
| ASIL C                  | DAL B (Hazardous)   | High                |
| ASIL D                  | DAL A (Catastrophic)| Highest             |

**Key observations on the mapping:**

1. **Scale inversion**: ASIL levels increase A-D (D is highest), while DAL
   levels decrease A-E (A is highest). This is a common source of confusion.

2. **QM/DAL E equivalence**: Both represent the case where no specific safety
   process rigor is required beyond standard quality practices.

3. **Rigor gradient**: Both systems scale the number of required objectives,
   the independence of verification activities, and the depth of analysis
   as the level increases.

4. **ASIL decomposition vs. DAL allocation**: ISO 26262 allows ASIL
   decomposition (splitting a high ASIL across redundant elements). DO-178C
   has a similar concept via item DAL allocation during system safety
   assessment but the mechanisms differ.

5. **Structural coverage**: Both standards require increasing structural
   code coverage at higher levels. ASIL D requires MC/DC (Modified
   Condition/Decision Coverage), which is also the signature requirement of
   DAL A in DO-178C. This is a direct and notable overlap.

---

## 3. ASPICE Work Products

### 3.1 Work Product Categories

ASPICE defines specific work products (outputs) for each process. These are
the artifacts that assessors evaluate during a SPICE assessment.

### 3.2 Left Side of the V (Development)

| Process | Key Work Products                                              |
|---------|----------------------------------------------------------------|
| SYS.1   | Stakeholder requirements specification                         |
| SYS.2   | System requirements specification, system requirements verification report |
| SYS.3   | System architectural design, interface specifications          |
| SWE.1   | Software requirements specification, software requirements verification report |
| SWE.2   | Software architectural design, interface design specifications |
| SWE.3   | Software detailed design, source code                          |

### 3.3 Right Side of the V (Verification)

| Process | Key Work Products                                              |
|---------|----------------------------------------------------------------|
| SWE.4   | Unit test specification, unit test results/report              |
| SWE.5   | Integration test specification, integration test results/report|
| SWE.6   | Software qualification test specification, test results/report |
| SYS.4   | System integration test specification, test results/report     |
| SYS.5   | System qualification test specification, test results/report   |

### 3.4 Mapping to DO-178C Equivalents

| ASPICE Work Product                    | DO-178C Equivalent                        |
|----------------------------------------|-------------------------------------------|
| System requirements specification      | System requirements (from system process)  |
| Software requirements specification    | Software Requirements Data (SRD)          |
| Software architectural design          | Software Design Description (SDD)         |
| Software detailed design               | Software Design Description (low-level)   |
| Source code                            | Source Code                                |
| Unit test specification                | Software Verification Cases & Procedures  |
| Unit test results                      | Software Verification Results             |
| Integration test specification         | Software Verification Cases & Procedures  |
| Integration test results               | Software Verification Results             |
| SW qualification test specification    | Software Verification Cases & Procedures  |
| SW qualification test results          | Software Verification Results             |
| Traceability record                    | Trace Data (within SAS, SRD, SVP)         |
| Configuration management plan          | Software Configuration Management Plan    |
| Quality assurance plan                 | Software Quality Assurance Plan (SQAP)    |

**Notable difference**: DO-178C consolidates verification artifacts into
fewer document types (Software Verification Cases and Procedures, Software
Verification Results) that span all verification levels. ASPICE keeps test
specifications and results separate for each V-model level.

---

## 4. Traceability Requirements in ASPICE

### 4.1 The Traceability Principle

Traceability is a cornerstone of ASPICE. The standard requires **bidirectional
traceability** between adjacent levels of the V-model. This means:

- **Forward traceability** (top-down): from higher-level artifacts to
  lower-level artifacts that implement them
- **Backward traceability** (bottom-up): from lower-level artifacts back to
  the higher-level requirements they satisfy

### 4.2 Required Traceability Links

```
Stakeholder Requirements (SYS.1)
        |  ^
        v  |
System Requirements (SYS.2) <-------> System Qualification Tests (SYS.5)
        |  ^
        v  |
System Architecture (SYS.3) <-------> System Integration Tests (SYS.4)
        |  ^
        v  |
SW Requirements (SWE.1)     <-------> SW Qualification Tests (SWE.6)
        |  ^
        v  |
SW Architecture (SWE.2)     <-------> SW Integration Tests (SWE.5)
        |  ^
        v  |
SW Detailed Design (SWE.3)  <-------> Unit Tests (SWE.4)
        |  ^
        v  |
   Source Code (SWE.3)
```

### 4.3 Specific Traceability Requirements

1. **SYS.2 <-> SYS.1**: Each system requirement must trace to one or more
   stakeholder requirements, and vice versa.

2. **SWE.1 <-> SYS.2**: Each software requirement must trace to one or more
   system requirements.

3. **SWE.2 <-> SWE.1**: Each architectural element must trace to the software
   requirements it addresses.

4. **SWE.3 <-> SWE.2**: Each detailed design element and unit must trace to
   the architectural component it implements.

5. **SWE.4 <-> SWE.3**: Each unit test must trace to the detailed design
   element or unit it verifies.

6. **SWE.5 <-> SWE.2**: Each integration test must trace to the architectural
   interfaces and interactions it verifies.

7. **SWE.6 <-> SWE.1**: Each software qualification test must trace to the
   software requirement(s) it verifies.

8. **SYS.4 <-> SYS.3**: System integration tests trace to system architecture.

9. **SYS.5 <-> SYS.2**: System qualification tests trace to system requirements.

### 4.4 Consistency and Coverage

ASPICE requires not just the existence of trace links but also:

- **Completeness**: every requirement at each level has at least one trace
  link in both directions
- **Consistency**: traced items are semantically aligned (the lower-level
  item actually addresses the higher-level item)
- **Coverage analysis**: gaps in traceability must be identified and resolved

### 4.5 Comparison with DO-178C Traceability

DO-178C has essentially identical traceability requirements but uses different
terminology:

| ASPICE Trace                          | DO-178C Equivalent Trace                  |
|---------------------------------------|-------------------------------------------|
| System Req -> SW Req                  | System Req -> High-Level Req (HLR)        |
| SW Req -> SW Architecture             | HLR -> Low-Level Req (LLR) / Design       |
| SW Architecture -> Detailed Design    | LLR -> Source Code                         |
| SW Req -> SW Qualification Tests      | HLR -> Test Cases                          |
| Detailed Design -> Unit Tests         | LLR -> Test Cases                          |
| Test -> Requirements (coverage)       | Test Coverage Analysis                     |

Both standards mandate that traceability be **bidirectional** and that
**coverage analysis** be performed to identify untested requirements or
orphaned test cases.

---

## 5. The Common V-Model Pattern

### 5.1 The Shared Structure

Both automotive (ASPICE/ISO 26262) and aviation (DO-178C) follow a remarkably
similar V-model structure. The left side decomposes requirements into
progressively more detailed specifications; the right side verifies each level
against its corresponding specification.

```
          SYSTEM                                    SYSTEM
       REQUIREMENTS ---------------------------------> QUALIFICATION
            |           (verified against)                TESTING
            |                                              ^
            v                                              |
        SOFTWARE                                    SOFTWARE
      REQUIREMENTS ---------------------------------> QUALIFICATION
            |           (verified against)                TESTING
            |                                              ^
            v                                              |
        SOFTWARE                                    SOFTWARE
      ARCHITECTURE ---------------------------------> INTEGRATION
            |           (verified against)                TESTING
            |                                              ^
            v                                              |
        DETAILED                                      UNIT
         DESIGN ------------------------------------> TESTING
            |           (verified against)                ^
            |                                              |
            v                                              |
       IMPLEMENTATION  ================================>---+
                        (code is the pivot point)
```

### 5.2 Level-by-Level Comparison

| V-Model Level        | ASPICE Process      | DO-178C Activity           |
|-----------------------|--------------------|-----------------------------|
| System Requirements   | SYS.2              | System process (ARP 4754A)  |
| SW Requirements       | SWE.1              | Software Requirements Process |
| SW Architecture       | SWE.2              | Software Design Process (HLR) |
| Detailed Design       | SWE.3              | Software Design Process (LLR) |
| Implementation        | SWE.3 (coding)     | Software Coding Process     |
| Unit Testing          | SWE.4              | SW Verification (unit level)|
| Integration Testing   | SWE.5              | SW Verification (integration)|
| SW Qualification Test | SWE.6              | SW Verification (HLR-based) |
| System Testing        | SYS.4, SYS.5       | System Verification         |

### 5.3 The Pivot Point

In both domains, **source code** (implementation) sits at the bottom of the V.
It is the artifact that:

- Is derived from all the left-side specifications
- Is the subject of all right-side verification activities
- Must be traceable upward to every level of requirements
- Must have its structure analyzed (structural coverage) at higher
  assurance levels

### 5.4 Horizontal Traceability

The horizontal arrows in the V-model represent the requirement that each
verification level is designed **against** its corresponding specification
level. This is not arbitrary -- it ensures that:

- Unit tests verify detailed design behavior (not system requirements directly)
- Integration tests verify architectural interfaces (not unit behavior)
- Qualification tests verify requirements (not implementation details)
- System tests verify system-level behavior (not component behavior)

This principle of **level-appropriate verification** is identical in both
domains.

---

## 6. Key Differences Between ASPICE/ISO 26262 and DO-178C

### 6.1 Structural Differences

| Aspect                  | ASPICE / ISO 26262           | DO-178C                        |
|-------------------------|------------------------------|--------------------------------|
| **Standard type**       | Process model (ASPICE) + Safety standard (ISO 26262) | Combined process + objectives standard |
| **Process definition**  | Two separate standards work together | Single standard covers both process and objectives |
| **Assessment model**    | Capability levels 0-5 per process | Pass/fail per objective         |
| **Certification body**  | OEM-driven assessment, no regulatory cert | Regulatory certification (FAA/EASA DER) |
| **Regulation**          | Customer-contractual requirement | Legal regulatory requirement    |

### 6.2 Terminology Differences

| Concept                     | ASPICE / ISO 26262            | DO-178C                        |
|-----------------------------|-------------------------------|--------------------------------|
| Safety criticality level    | ASIL (A-D)                    | DAL (A-E)                      |
| High-level requirements     | Software Requirements (SWE.1) | High-Level Requirements (HLR)  |
| Low-level requirements      | Detailed Design (SWE.3)       | Low-Level Requirements (LLR)   |
| Design documentation        | SW Architectural Design       | Software Design Description    |
| Test against requirements   | SW Qualification Testing      | Requirements-Based Testing     |
| Structural analysis         | Structural Coverage (ISO 26262 Part 6) | Structural Coverage Analysis |
| Process oversight            | Quality Assurance (SUP.1)     | SQA (Software Quality Assurance) |
| Configuration control        | Configuration Mgmt (SUP.8)    | SCM (Software Config Mgmt)     |
| Plan documents               | Project Plan, QA Plan         | PSAC, SDP, SVP, SCMP, SQAP     |

### 6.3 Process and Lifecycle Differences

1. **Plan documents**: DO-178C requires a specific set of planning documents
   (Plan for Software Aspects of Certification -- PSAC, Software Development
   Plan, Software Verification Plan, etc.) that must be agreed with the
   certification authority. ASPICE does not prescribe a fixed set of plans
   but expects planning evidence within each process.

2. **Independence requirements**: DO-178C has strict independence requirements
   for verification activities at DAL A and B (the verifier must not be the
   developer). ISO 26262 has similar independence requirements but frames
   them differently using independence levels (1a through 3d).

3. **Tool qualification**: DO-178C has a detailed tool qualification process
   (DO-330) when tools are used to replace, reduce, or automate verification
   activities. ISO 26262 Part 8 addresses tool confidence levels (TCL1-3)
   with a similar but structurally different approach.

4. **Deactivated code, dead code**: DO-178C has specific objectives for
   identifying and addressing deactivated code and dead code. ASPICE does not
   explicitly address this at the same level of detail.

5. **Certification vs. assessment**: DO-178C culminates in regulatory
   certification (the software is approved for flight). ASPICE culminates
   in a capability assessment (the processes are rated). ISO 26262 requires
   a safety case and confirmation review but does not involve external
   regulatory certification in the same way as aviation.

6. **ASIL decomposition**: ISO 26262 provides a formal mechanism for ASIL
   decomposition across redundant elements (e.g., ASIL D = ASIL B(D) +
   ASIL B(D)). DO-178C does not have a direct equivalent, though system-level
   DAL allocation via ARP 4754A serves a similar architectural purpose.

### 6.4 Coverage Requirements Comparison

| Coverage Type                  | DO-178C Level Required | ISO 26262 ASIL Level Required |
|--------------------------------|------------------------|-------------------------------|
| Statement Coverage             | DAL C and above        | ASIL A and above (recommended)|
| Branch/Decision Coverage       | DAL B and above        | ASIL B and above              |
| MC/DC                          | DAL A                  | ASIL D                        |
| Requirements-Based Testing     | DAL D and above        | ASIL A and above              |
| Interface Testing              | DAL D and above        | ASIL A and above              |

The alignment of MC/DC at the highest level (DAL A = ASIL D) is one of the
most concrete evidence points that these standards share a common intellectual
heritage.

---

## 7. A Reusable V-Model Framework: Common Abstractions

### 7.1 Design Principles

A reusable V-model framework that spans both automotive and aviation (and
potentially other safety-critical domains like rail EN 50128, medical
IEC 62304) would need to abstract away domain-specific terminology while
preserving the structural invariants common to all.

### 7.2 Core Abstractions

#### 7.2.1 Artifact Levels

The framework defines a hierarchy of **artifact levels**, each representing a
stage of decomposition:

```
Level 0: Stakeholder / Operational Requirements
Level 1: System Requirements
Level 2: Component / Software Requirements (High-Level Requirements)
Level 3: Architecture / Design (Architectural Design)
Level 4: Detailed Design / Low-Level Requirements
Level 5: Implementation (Source Code)
```

Each domain maps its terminology onto these levels:

| Abstract Level | ASPICE            | DO-178C              | IEC 62304 (Medical)  |
|----------------|-------------------|-----------------------|----------------------|
| Level 0        | SYS.1 Stakeholder | Operational concept   | User needs           |
| Level 1        | SYS.2 System Req  | System Requirements   | System requirements  |
| Level 2        | SWE.1 SW Req      | HLR                   | Software requirements|
| Level 3        | SWE.2 SW Arch     | HLR (architectural)   | SW architecture      |
| Level 4        | SWE.3 Detailed    | LLR                   | Detailed design      |
| Level 5        | SWE.3 Code        | Source Code            | Source Code          |

#### 7.2.2 Verification Levels

Each artifact level (except Level 0) has a corresponding **verification level**:

```
Verification Level 5: Unit Verification       (verifies Level 4/5)
Verification Level 4: Integration Verification (verifies Level 3)
Verification Level 3: Qualification Verification (verifies Level 2)
Verification Level 2: System Verification      (verifies Level 1)
Verification Level 1: Acceptance Verification  (verifies Level 0)
```

#### 7.2.3 Traceability Links

The framework enforces a **traceability matrix** as a first-class concept:

```python
# Pseudocode for the traceability model
class TraceLink:
    source_level: ArtifactLevel
    source_id: str
    target_level: ArtifactLevel
    target_id: str
    direction: "derives_from" | "verified_by"

class TraceabilityMatrix:
    links: List[TraceLink]

    def coverage_at_level(level: ArtifactLevel) -> CoverageReport:
        """Returns which items at this level have/lack trace links."""

    def orphan_analysis() -> OrphanReport:
        """Finds items with no upward or no downward trace."""

    def bidirectional_check() -> ConsistencyReport:
        """Verifies every forward link has a corresponding backward link."""
```

#### 7.2.4 Assurance Level

A domain-agnostic **assurance level** replaces ASIL/DAL:

| Generic Level | Automotive | Aviation | Medical (IEC 62304) | Rail (EN 50128) |
|---------------|-----------|----------|---------------------|-----------------|
| None          | QM        | DAL E    | Class A             | SIL 0           |
| Low           | ASIL A    | DAL D    | Class B (partial)   | SIL 1           |
| Medium-Low    | ASIL B    | DAL C    | Class B             | SIL 2           |
| Medium-High   | ASIL C    | DAL B    | Class C (partial)   | SIL 3           |
| High          | ASIL D    | DAL A    | Class C             | SIL 4           |

The assurance level determines:
- Which verification objectives apply
- What level of structural coverage is required
- Whether independence of verification is required
- What level of tool qualification/confidence is needed

#### 7.2.5 Verification Objective

A **verification objective** is a domain-agnostic check that must be
satisfied:

```yaml
verification_objective:
  id: "VO-STRUCT-COVERAGE-MCDC"
  description: "MC/DC structural coverage of source code"
  applies_at_assurance_level: "High"  # ASIL D / DAL A
  verification_level: 5  # Unit level
  evidence_type: "coverage_report"
  independence_required: true  # at this assurance level
```

The framework provides a registry of objectives. Each domain binding selects
which objectives apply at which assurance levels.

#### 7.2.6 Work Product Template

A **work product** is an artifact produced by a process activity:

```yaml
work_product:
  id: "WP-SW-REQ"
  name: "Software Requirements Specification"
  artifact_level: 2
  content_requirements:
    - unique_id_per_requirement: true
    - verifiability: true
    - consistency_check: true
    - traceability_to_parent: true
    - traceability_to_children: true
  domain_mappings:
    aspice: "SWE.1 output: Software requirements specification"
    do178c: "Software Requirements Data (11.9)"
    iec62304: "Software requirements specification (5.2)"
```

### 7.3 Framework Architecture

```
+------------------------------------------------------------------+
|                    Domain Binding Layer                           |
|  (ASPICE binding, DO-178C binding, IEC 62304 binding, etc.)     |
|  - Maps domain terms to abstract levels                          |
|  - Selects applicable objectives per assurance level             |
|  - Defines domain-specific work product templates                |
+------------------------------------------------------------------+
|                    Core V-Model Engine                            |
|  - Artifact level hierarchy (0-5)                                |
|  - Verification level hierarchy (1-5)                            |
|  - Traceability matrix with coverage analysis                    |
|  - Assurance level registry                                      |
|  - Verification objective registry                               |
|  - Work product lifecycle management                             |
+------------------------------------------------------------------+
|                    Evidence & Reporting Layer                     |
|  - Trace coverage reports                                        |
|  - Structural coverage integration                               |
|  - Verification status dashboards                                |
|  - Gap analysis (missing traces, untested requirements)          |
|  - Assessment/certification artifact generation                  |
+------------------------------------------------------------------+
```

### 7.4 Key Invariants the Framework Must Enforce

These are the rules that hold true across ALL safety-critical domains:

1. **Every requirement must be verifiable.** If a requirement cannot be
   tested, it must be decomposed until it can be.

2. **Bidirectional traceability between adjacent levels.** No gaps allowed.
   Every item at Level N must trace to at least one item at Level N-1
   (upward) and at least one item at Level N+1 (downward), except at the
   top and bottom of the hierarchy.

3. **Verification is level-appropriate.** Unit tests verify detailed design,
   not system requirements. System tests verify system requirements, not
   code structure.

4. **Higher assurance = more objectives.** The set of required verification
   objectives is a strict superset as assurance level increases.

5. **Independence scales with assurance.** At lower levels, self-verification
   may be acceptable. At higher levels, an independent verifier is required.

6. **Structural coverage complements requirements-based testing.** Both
   domains require structural analysis to ensure there is no unintended
   functionality (code not traceable to any requirement).

7. **Configuration management is mandatory.** All artifacts (requirements,
   design, code, tests, results) must be under configuration control with
   baseline management.

8. **Change impact analysis.** When an artifact at any level changes, the
   framework must identify all affected artifacts at adjacent levels that
   may need updating and re-verification.

### 7.5 What This Enables

A reusable V-model framework with these abstractions would allow:

- **Multi-domain projects**: A product with both automotive and aviation
  components (e.g., urban air mobility / eVTOL) could manage both under
  a single framework with different domain bindings.

- **Tooling reuse**: Requirements management, traceability analysis,
  coverage reporting, and test management tools could work against the
  abstract model and support any domain.

- **Process migration**: An organization moving from automotive to aviation
  (or vice versa) would retain their tooling and processes, changing only
  the domain binding.

- **Compliance evidence generation**: The framework could automatically
  generate domain-specific compliance evidence (ASPICE assessment artifacts
  or DO-178C certification artifacts) from the same underlying data.

- **Gap analysis across standards**: When a project must comply with
  multiple standards simultaneously, the framework can identify where
  objectives overlap and where additional work is needed.

---

## 8. Summary of Findings

### 8.1 What Is Shared (The 80%)

The following elements are structurally identical or nearly so across ASPICE,
ISO 26262, and DO-178C:

1. The V-model decomposition structure (requirements -> architecture ->
   design -> code -> unit test -> integration test -> qualification test ->
   system test)
2. Bidirectional traceability requirements between all adjacent levels
3. Requirements-based testing as the primary verification strategy
4. Structural coverage analysis as a complement to requirements-based testing
5. MC/DC as the highest level of structural coverage
6. Independence requirements scaling with criticality
7. Configuration management and baseline control
8. Change impact analysis requirements
9. Tool qualification/confidence requirements

### 8.2 What Differs (The 20%)

1. **Regulatory context**: Aviation has mandatory FAA/EASA certification;
   automotive has OEM-contractual assessment and a safety case
2. **Standard structure**: DO-178C is a single unified standard; automotive
   uses ASPICE (process) + ISO 26262 (safety) as complementary standards
3. **Assessment granularity**: ASPICE rates process capability on a 0-5
   scale; DO-178C is objective-based pass/fail
4. **Plan document set**: DO-178C prescribes specific plans (PSAC, SDP, SVP,
   SCMP, SQAP); ASPICE is more flexible
5. **ASIL decomposition**: ISO 26262 has a formal decomposition mechanism;
   DO-178C handles this at the system level via ARP 4754A
6. **Terminology**: Different names for the same concepts (HLR vs. SW
   Requirements, LLR vs. Detailed Design, etc.)

### 8.3 Implication for Framework Design

The high degree of structural commonality (estimated at 80% or more) makes
a reusable V-model framework not only feasible but highly practical. The
domain-specific variations are primarily in:

- Terminology mapping
- Which objectives apply at which assurance level
- Regulatory/contractual output format
- Assessment methodology (capability levels vs. objective satisfaction)

These variations are cleanly separable into a **domain binding layer** that
sits atop a common **V-model engine**, making the framework architecture
straightforward.

---

## References

- Automotive SPICE Process Reference Model and Process Assessment Model,
  VDA QMC, Version 3.1 / 4.0
- ISO 26262:2018, "Road vehicles -- Functional safety," Parts 1-12
- RTCA DO-178C, "Software Considerations in Airborne Systems and Equipment
  Certification," 2012
- RTCA DO-330, "Software Tool Qualification Considerations," 2011
- SAE ARP 4754A, "Guidelines for Development of Civil Aircraft and Systems,"
  2010
- IEC 62304:2006+AMD1:2015, "Medical device software -- Software life cycle
  processes"
- EN 50128:2011, "Railway applications -- Communication, signalling and
  processing systems -- Software for railway control and protection systems"

---

*Note: This research document is based on publicly available information about
these standards. The actual standards documents are copyrighted by their
respective organizations (VDA, ISO, RTCA/EUROCAE, SAE, IEC, CENELEC) and
should be obtained from official sources for implementation purposes. Web
search was not available during document creation; content is based on the
author's training data knowledge of these standards.*
