# Requirement Syntax Best Practices in V-Model Standards

## Research Summary

EARS is one of several structured approaches to writing requirements in safety-critical domains. This document surveys the broader landscape: alternative syntax methods, standard-specific quality criteria, automated checking approaches, and what assessors actually look for during certification audits.

---

## 1. Alternative Requirement Syntax Methods

### 1.1 Rupp/SOPHIST Templates (Requirements Engineering Fundamentals)

Developed by Chris Rupp and the SOPHIST Group (Germany), widely adopted in automotive (ASPICE/ISO 26262). The approach defines sentence templates based on requirement type:

**Core Template:**
```
[Condition] The <system> SHALL/SHOULD/WILL <process> <object> [constraint].
```

**Legal obligation levels:**
| Keyword | Meaning |
|---------|---------|
| SHALL | Mandatory requirement (legally binding) |
| SHOULD | Recommended, deviation must be justified |
| WILL | Statement of intent or future action |

**Conditional variants:**

```
IF <condition>, THEN the <system> SHALL <process> <object>.

AFTER <event>, the <system> SHALL <process> <object>.

AS LONG AS <state>, the <system> SHALL <process> <object>.
```

**Example (automotive):**
```
IF vehicle speed exceeds 120 km/h,
THEN the braking system SHALL apply maximum deceleration of 9.8 m/s²
within 150 ms of brake pedal activation.
```

**Strengths:** Very popular in German automotive industry, well-integrated with ASPICE, explicit temporal/conditional keywords, maps well to test case generation.

**Comparison with EARS:** Rupp templates are more granular in conditional types (IF/AFTER/AS LONG AS vs. EARS's single WHEN/IF/WHERE). EARS is simpler; Rupp gives finer control for complex timing behaviors.

### 1.2 Planguage (Tom Gilb)

Planguage (Planning Language) is a keyword-driven specification language developed by Tom Gilb for quantifying quality requirements and design specifications.

**Structure:**
```
Tag:          <requirement ID>
Type:         <Performance | Resource | Design | ...>
Stakeholder:  <who cares>
Scale:        <unit of measure>
Meter:        <how to measure>
Past:         <current/baseline level>
Must:         <minimum acceptable level>
Plan:         <target level>
Wish:         <ideal level>
```

**Example:**
```
Tag:          RESP-TIME-01
Type:         Performance Requirement
Gist:         System response time for brake command processing
Scale:        Milliseconds from pedal sensor input to actuator command output
Meter:        Oscilloscope measurement at actuator bus interface
Past:         45 ms (current legacy system)
Must:         25 ms (safety minimum per ISO 26262 analysis)
Plan:         15 ms
Wish:         10 ms
```

**Strengths:** Forces quantification of "soft" requirements, built-in testability (Scale + Meter), makes trade-offs explicit (Must vs Plan vs Wish).

**Weakness:** Verbose for simple functional requirements; best suited for non-functional/quality requirements.

**Relevance to VModelWorkflow:** Excellent for performance, reliability, and safety integrity requirements where measurable targets are needed. Could complement EARS for non-functional requirements.

### 1.3 Boilerplate Requirements (CESAR Project)

The CESAR European research project (Cost-Efficient methods and processes for SAfety-Relevant embedded systems) developed a boilerplate approach specifically for embedded safety-critical systems.

**Philosophy:** Define a fixed set of sentence fragments (boilerplates) that can be combined. Each fragment has formal semantics, enabling automated analysis.

**Boilerplate fragments:**
```
<actor> SHALL <action> <object>
WITHIN <time constraint> OF <trigger event>
UNDER <operating condition>
WITH <quality constraint>
UNLESS <exception condition>
```

**Example:**
```
The flight_control_computer SHALL send aileron_command TO actuator_controller
WITHIN 20ms OF receiving pilot_input
UNDER normal_operating_mode
WITH accuracy ±0.5 degrees
UNLESS emergency_override IS active.
```

**Key advantage:** Because each fragment has formal semantics, requirements written in boilerplate form can be automatically checked for consistency, completeness, and even model-checked against system models.

### 1.4 SysML Requirement Diagrams

SysML (Systems Modeling Language) provides a graphical notation for requirements as first-class model elements.

**Requirement stereotype properties:**
- `id`: Unique identifier
- `text`: Natural language statement
- `risk`: Associated risk level
- `verifyMethod`: Analysis, Demonstration, Inspection, Test

**Relationship types:**
- `<<deriveReqt>>` — Requirement derived from parent
- `<<satisfy>>` — Design element satisfies requirement
- `<<verify>>` — Test case verifies requirement
- `<<refine>>` — Model element refines requirement
- `<<trace>>` — General traceability link

**Relevance to VModelWorkflow:** SysML is the notation, not the syntax. Requirements inside SysML blocks still need structured text (EARS, Rupp, etc.). However, SysML's relationship types map directly to our traceability model.

### 1.5 INCOSE Guide for Writing Requirements

The International Council on Systems Engineering (INCOSE) published the *Guide for Writing Requirements* (2017, updated), which is standard-agnostic and widely referenced.

**INCOSE's requirement characteristics:**
1. **Necessary** — Removal would create a deficiency
2. **Appropriate** — Correct level of abstraction
3. **Unambiguous** — Single interpretation
4. **Complete** — Fully stated, no TBDs
5. **Singular** — States one thing (atomic)
6. **Feasible** — Achievable within constraints
7. **Verifiable** — Can be proven through test/analysis/inspection/demonstration
8. **Correct** — Accurate statement of need
9. **Conforming** — Follows approved template/style

**INCOSE's set-level characteristics (for a specification document):**
1. **Complete** — All requirements present, no gaps
2. **Consistent** — No contradictions between requirements
3. **Feasible** — Set is achievable as a whole
4. **Comprehensible** — Organized and readable
5. **Able to be validated** — Can confirm it meets stakeholder needs

**Practical rules from INCOSE:**
- Use "shall" for requirements, "will" for facts/declarations, "should" for goals
- One requirement per sentence
- Avoid vague terms: "appropriate", "as applicable", "but not limited to", "etc."
- Avoid combinators: "and/or" in a single requirement
- Use defined terms consistently (glossary)
- Positive statements preferred ("shall do X" not "shall not do Y" unless a constraint)

### 1.6 MaRCo (Mapping Requirements to Code)

Academic method focused on bridging the gap between natural language requirements and code-level implementation. Uses intermediate "requirement patterns" that map to code patterns.

Less relevant for our V-model compliance focus but worth noting for the implementation traceability aspect.

---

## 2. Standard-Specific Requirement Quality Criteria

### 2.1 DO-178C (Aviation Software)

DO-178C Section 6.3 specifies that high-level requirements shall be:
- **Compliant** with system requirements
- **Accurate and consistent** — no contradictions
- **Compatible with target computer** (feasible)
- **Verifiable** — each requirement can be verified through review, analysis, or test
- **Conforming to standards** — follows project-defined standards
- **Traceable** — bidirectional traceability to system requirements
- **Algorithm accurate** — mathematical formulations correct

**DO-178C explicitly requires:**
- Requirements are written before code (waterfall discipline)
- Derived requirements (not traceable to system requirements) must be identified and fed back to system safety assessment
- Requirements must be reviewed against source (system requirements)

**What DERs (Designated Engineering Representatives) look for:**
- Can every requirement be tested? (If not, it's a finding)
- Are requirements atomic? (Compound requirements hide untested conditions)
- Are there any TBDs, TBRs left in the final baseline?
- Is every "shall" statement traced to a test case?
- Are derived requirements flagged and justified?

### 2.2 ISO 26262 (Automotive Functional Safety)

**Part 8, Clause 6 — Specification and Management of Safety Requirements:**

Requirements shall be:
- **Unambiguous** — clear, single interpretation
- **Comprehensible** — understandable by all stakeholders
- **Atomic** — one requirement per statement
- **Internally consistent** — no self-contradiction
- **Feasible** — implementable within constraints
- **Verifiable** — can be proven met

**Part 8, Clause 6 also specifies requirement attributes:**
- Unique ID
- Status (draft, approved, deleted)
- ASIL level (A through D, or QM)
- Verification criteria and method
- Traceability links (to parent and child)

**ISO 26262 Part 6 (Software Development) adds:**
- Software safety requirements shall be derived from technical safety requirements
- Each software requirement shall specify: functional behavior, timing constraints, resource consumption
- Requirements shall address: nominal behavior, degraded behavior, fault reactions

### 2.3 IEC 61508 (General Functional Safety)

**Part 1, Clause 7.2 — Software Safety Requirements Specification:**

The specification shall include:
- Safety functions to be performed by software
- SIL (Safety Integrity Level) for each function
- Response time and timing constraints
- Interfaces to non-safety software
- Configuration and calibration data requirements

**Requirement properties (IEC 61508 Part 1, Annex A):**
- Bidirectional traceability (a normative objective at higher SILs)
- Completeness — all safety functions addressed
- Freedom from contradiction
- Testability of each requirement

### 2.4 ASPICE (Automotive SPICE)

**SWE.1 (Software Requirements Analysis) expects:**
- Requirements are analyzed for correctness, technical feasibility, and verifiability
- Requirements are categorized (functional, non-functional, interface, design constraint)
- Requirements have bidirectional traceability
- Impact of requirements on operating environment is identified
- Requirements are agreed upon with all relevant stakeholders
- Requirements are prioritized

**Assessment tip:** ASPICE assessors use a rating scale (N/P/L/F = Not/Partially/Largely/Fully achieved). To score "Fully achieved" on SWE.1:
- Every requirement must be traceable
- Every requirement must have an explicit verification method
- Requirements review records must exist

---

## 3. Requirement Anti-Patterns and Common Defects

### 3.1 The "Killer" Anti-Patterns

| Anti-Pattern | Example | Problem |
|---|---|---|
| **Vague adjective** | "The system shall be *sufficiently* reliable" | Untestable |
| **Compound requirement** | "The system shall log errors *and* notify the operator *and* retry" | Three requirements in one; easy to miss one in testing |
| **Passive voice without actor** | "The data shall be validated" | By whom/what? |
| **Unbounded list** | "...such as X, Y, Z, etc." | "etc." is untestable |
| **Negative requirement** | "The system shall not crash" | How do you prove a negative? How do you test all possible crash scenarios? |
| **Implementation prescription** | "The system shall use a linked list to store records" | Constrains design unnecessarily |
| **Wishful thinking** | "The system shall process all inputs in zero time" | Not feasible |
| **Escape clause** | "The system shall comply with all applicable standards" | "applicable" is undefined |
| **TBD/TBR** | "The system shall respond within TBD seconds" | Incomplete |

### 3.2 Most Common Findings in Certification Audits

Based on published audit experience from DERs and ASPICE assessors:

1. **Missing derived requirements** — Safety analysis produces conditions that need requirements, but they're never written (DO-178C §6.3.4 violation)
2. **Untraceable requirements** — Requirement exists but no link to parent or verification
3. **Ambiguous timing** — "quickly", "promptly", "in a timely manner" instead of milliseconds
4. **Missing fault/degraded behavior** — Only nominal behavior specified, no error handling requirements
5. **Inconsistent terminology** — Same concept called different names in different requirements
6. **Requirements at wrong abstraction level** — Design details in high-level requirements, or vague goals in low-level requirements
7. **Missing interface requirements** — Behavior at system boundaries undefined

---

## 4. Automated Requirement Quality Checking

### 4.1 Available Tools and Approaches

| Tool/Approach | What It Checks | Standard Alignment |
|---|---|---|
| **QVscribe (by QRA)** | INCOSE quality metrics, ambiguity detection, completeness | INCOSE, DO-178C |
| **IBM DOORS Quality Checker** | Template compliance, TBD detection, metric dashboards | General |
| **Innoslate** | Requirement quality scoring, consistency analysis | INCOSE |
| **Requirements Quality Suite (REUSE)** | NLP-based ambiguity detection, readability scoring | ISO 26262 |
| **Custom NLP pipelines** | Regex + NLP for banned words, passive voice, compound detection | Any |

### 4.2 Automatable Quality Rules

These rules can be implemented as automated checks (relevant for VModelWorkflow's tooling):

```yaml
requirement_quality_rules:
  - name: no_vague_terms
    pattern: "\\b(adequate|appropriate|as applicable|as needed|but not limited to|capable|effective|etc|if possible|normal|reasonable|sufficient|timely|user-friendly)\\b"
    severity: error
    message: "Vague term detected — replace with measurable criterion"

  - name: atomic_check
    pattern: "\\bshall\\b.*\\b(and|or)\\b.*\\bshall\\b"
    severity: warning
    message: "Possible compound requirement — consider splitting"

  - name: passive_voice
    pattern: "\\bshall be (\\w+ed|\\w+en)\\b"
    severity: warning
    message: "Passive voice detected — specify the actor"

  - name: no_tbd
    pattern: "\\b(TBD|TBR|TBS|TODO|FIXME)\\b"
    severity: error
    message: "Incomplete requirement — resolve before baseline"

  - name: has_measurable_criterion
    pattern: "\\bshall\\b(?!.*(\\d+\\s*(ms|sec|Hz|%|MB|KB|dB|m/s)))"
    severity: info
    message: "No numeric criterion found — verify requirement is testable"

  - name: no_escape_clause
    pattern: "\\b(as appropriate|if applicable|when possible|as required|as necessary)\\b"
    severity: error
    message: "Escape clause detected — requirement must be unconditional"
```

---

## 5. Practical Comparison: Which Syntax Method When?

| Method | Best For | Domain Adoption | Complexity | Tool Support |
|---|---|---|---|---|
| **EARS** | Functional requirements, event-driven behavior | Aviation, general | Low | Good (templates) |
| **Rupp/SOPHIST** | Complex conditional/temporal requirements | Automotive (DE) | Medium | Good |
| **Planguage** | Non-functional, quality, performance requirements | Cross-domain | Medium | Limited |
| **Boilerplate (CESAR)** | Formally analyzable requirements | Research, aviation | High | Specialized |
| **INCOSE Guide** | Any — quality checklist, not a syntax | Universal | Low | N/A (guidance) |
| **SysML** | System-level, model-based development | Aerospace, auto | High | MBSE tools |

### 5.1 Recommended Approach for VModelWorkflow

A pragmatic combination:

1. **EARS** as the primary syntax for functional requirements (already adopted)
2. **Rupp temporal keywords** (AFTER, AS LONG AS) as extensions for complex timing requirements that EARS WHEN/WHILE don't adequately express
3. **Planguage-style quantification** for non-functional requirements (performance, reliability, availability)
4. **INCOSE quality checklist** as the validation criteria for all requirements regardless of syntax
5. **Automated quality rules** (Section 4.2) integrated into the traceability engine

This gives us syntax flexibility while maintaining consistent quality standards.

---

## 6. What Assessors Actually Look For

### 6.1 DO-178C DER Review Checklist (Requirements Focus)

A DER reviewing software requirements will check:

- [ ] Every system requirement allocated to software has a corresponding software requirement
- [ ] Every software requirement traces to at least one system requirement, OR is identified as derived
- [ ] Derived requirements are fed back to system safety assessment (SSA)
- [ ] Every requirement has a unique identifier
- [ ] Every requirement uses "shall" (not "should", "will", "may" for mandatory behavior)
- [ ] No TBDs or TBRs remain in the approved baseline
- [ ] Requirements are verifiable (each has a defined verification method)
- [ ] Requirements are reviewed for accuracy, consistency, and completeness (review records exist)
- [ ] Transition criteria from requirements to design phase are met
- [ ] Requirements standard is defined and followed (in the Software Development Plan)

### 6.2 ISO 26262 Assessor Checklist

- [ ] Safety requirements are derived from safety goals via Functional Safety Concept
- [ ] ASIL inheritance is correct (no ASIL decomposition errors)
- [ ] Each safety requirement has: ID, ASIL, status, verification method, traceability
- [ ] Requirements address nominal, degraded, and fault reaction behaviors
- [ ] Timing constraints are explicit (not "fast" but "within 50ms")
- [ ] Interface requirements are complete (inputs, outputs, protocols, error signaling)
- [ ] Requirements review is documented with evidence of independence (per ASIL)

### 6.3 Common Assessment Findings That Fail Audits

1. **"Show me the trace"** — Assessor picks a random system requirement and walks it down to test case. If any link is missing, finding.
2. **"How do you test this?"** — Assessor picks a requirement and asks for the verification method. "We will test it" is not enough — must specify how.
3. **"Where's the derived requirement?"** — Safety analysis found a hazard, mitigation is in the design, but no requirement exists for it.
4. **"This is two requirements"** — Compound statement with AND. One part was tested, the other wasn't.
5. **"What does 'appropriate' mean?"** — Vague term with no definition in the glossary.

---

## 7. References

1. Mavin, A. et al. (2009). "Easy Approach to Requirements Syntax (EARS)." *IEEE RE'09*, pp. 317-322.
2. Rupp, C. (2014). *Requirements-Engineering und -Management*. 6th edition. Hanser.
3. Gilb, T. (2005). *Competitive Engineering*. Elsevier.
4. INCOSE (2017). *Guide for Writing Requirements*. INCOSE-TP-2010-006-03.
5. IEEE 29148:2018. *Systems and Software Engineering — Life Cycle Processes — Requirements Engineering*.
6. RTCA DO-178C (2011). *Software Considerations in Airborne Systems and Equipment Certification*. Section 6.3.
7. ISO 26262:2018. *Road Vehicles — Functional Safety*. Part 8, Clause 6.
8. IEC 61508:2010. *Functional Safety of E/E/PE Safety-Related Systems*. Part 1, Clause 7.2.
9. CESAR Project (2012). *Cost-Efficient Methods and Processes for Safety-Relevant Embedded Systems*. Springer.
10. Automotive SPICE v3.1, Process Assessment Model, SWE.1 Software Requirements Analysis.
11. FAA Order 8110.49A — Software Approval Guidelines (DER guidance).
