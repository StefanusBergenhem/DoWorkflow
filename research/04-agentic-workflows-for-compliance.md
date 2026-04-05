# Agentic AI Coding Workflows for Safety-Critical and Compliance Software Development

## Research Document 04

**Date:** 2026-03-27
**Scope:** Intersection of agentic AI coding workflows with regulated/safety-critical software development

---

## Table of Contents

1. [Current State of AI-Assisted Coding in Regulated Industries](#1-current-state-of-ai-assisted-coding-in-regulated-industries)
2. [Design-Requirement-Test Driven Development (DRTDD)](#2-design-requirement-test-driven-development-drtdd)
3. [Existing Agentic Coding Frameworks and V-Model Adaptation](#3-existing-agentic-coding-frameworks-and-v-model-adaptation)
4. [Continuous Compliance](#4-continuous-compliance)
5. [Legacy Code Retrofit Strategies](#5-legacy-code-retrofit-strategies)
6. [AI Agent Limitations for This Use Case](#6-ai-agent-limitations-for-this-use-case)
7. [Existing Projects and Research at the Intersection](#7-existing-projects-and-research-combining-ai-agents-with-safety-standards)
8. [Prompt and Skill Design for Smaller Models](#8-prompt-and-skill-design-for-smaller-and-older-models)
9. [Synthesis and Recommendations](#9-synthesis-and-recommendations)

---

## 1. Current State of AI-Assisted Coding in Regulated Industries

### 1.1 The Regulatory Landscape

Safety-critical software development is governed by domain-specific standards that impose rigorous process requirements:

| Domain | Standard | Key Requirement |
|--------|----------|-----------------|
| Aviation | DO-178C / ED-12C | Traceability from requirements through design, code, and tests at 5 DALs (A-E) |
| Automotive | ISO 26262 | ASIL levels (A-D), V-model lifecycle, safety case documentation |
| Medical Devices | IEC 62304 | Software safety classification (A-C), risk management integration |
| Railway | EN 50128 | SIL levels (0-4), independent verification |
| Nuclear | IEC 61513 / IEC 60880 | Category A-C software, diverse redundancy |
| Industrial | IEC 61508 | Generic functional safety, SIL 1-4 |
| Space | ECSS-E-ST-40C | Criticality categories, formal verification |

All these standards share common themes: **bidirectional traceability**, **verification evidence**, **configuration management**, and **independent review**. These are precisely the areas where AI agents could either help enormously or introduce unacceptable risk.

### 1.2 Current Industry Posture

As of 2025-2026, the regulated software industry's relationship with AI coding tools falls into three tiers:

**Tier 1 -- Prohibited or severely restricted:**
- Most DAL-A/B aviation software shops have blanket bans on generative AI in production code paths.
- Certification authorities (FAA, EASA) have not issued formal guidance on AI-generated code, creating a chilling effect.
- The concern is not just correctness but *explainability* -- DO-178C requires the applicant to demonstrate understanding of every line of code.

**Tier 2 -- Cautiously exploring:**
- Automotive OEMs and Tier-1 suppliers are piloting AI assistants for non-ASIL or ASIL-A code, with mandatory human review.
- Medical device companies use Copilot-class tools for test scaffolding and documentation but not for production code submitted to FDA.
- Some railway software teams use AI for boilerplate MISRA-C compliance checks.

**Tier 3 -- Actively adopting (with guardrails):**
- Non-safety-critical support tools (ground support equipment, test infrastructure, CI/CD pipelines) in regulated companies freely use AI coding assistants.
- Documentation generation (requirements paraphrasing, test procedure drafts) is the most accepted use case.
- Static analysis rule customization and MISRA checker configuration.

### 1.3 Existing Frameworks and Tools

**Formal tool qualification (DO-330 / ISO 26262 Part 8):**
Any tool that could introduce errors into safety-critical software or fail to detect them must be qualified. AI coding tools would fall under the most stringent qualification category (TQL-1 in DO-330) because their output directly becomes part of the software. This is currently an unsolved problem -- no LLM-based tool has been qualified under DO-330.

**Emerging approaches:**
- **Qualification by verification:** Rather than qualifying the AI tool itself, qualify the verification process that checks its output. If every AI-generated artifact is independently verified to the same standard as human-generated artifacts, the tool qualification burden is reduced (analogous to a TQL-5 tool whose output is verified).
- **AI as a "suggestion engine":** Treating AI output the same as code from a new hire -- it must pass all existing reviews, analyses, and tests. The human developer takes full responsibility.
- **Deterministic wrappers:** Using AI to generate candidates, then filtering through deterministic formal methods (model checking, abstract interpretation, theorem proving) to guarantee properties.

### 1.4 Key Gap

No existing framework addresses **agentic** AI (AI that takes autonomous multi-step actions) in safety-critical contexts. All current approaches treat AI as a passive suggestion tool with a human in the loop for every decision. The jump from "AI suggests, human decides" to "AI agent executes a workflow with human gates at defined points" is the frontier this research explores.

---

## 2. Design-Requirement-Test Driven Development (DRTDD)

### 2.1 Why TDD Alone Is Insufficient for Compliance

Standard TDD follows: **Red -> Green -> Refactor**

This works well for functional correctness but does not address:
- **Requirements traceability:** Which requirement does this test verify? Is every requirement covered?
- **Design rationale:** Why was this design chosen? What alternatives were considered?
- **Safety analysis integration:** How does this code relate to the hazard analysis? What is the ASIL/DAL?
- **Verification completeness:** Are structural coverage criteria (MC/DC, decision coverage) met?
- **Change impact analysis:** When a requirement changes, which tests and code are affected?

### 2.2 The DRTDD Loop

Design-Requirement-Test Driven Development extends TDD by prepending two phases and adding traceability throughout:

```
Phase 1: REQUIRE
  Input:  System/subsystem requirement (from requirements management tool)
  Output: Software requirement(s) with:
          - Unique ID (e.g., SW-REQ-0042)
          - Parent requirement trace (e.g., SYS-REQ-0017)
          - Safety classification (e.g., ASIL-B)
          - Verification method (test, analysis, review, inspection)
          - Acceptance criteria (formal, testable)

Phase 2: DESIGN
  Input:  Software requirement(s) from Phase 1
  Output: Design artifact(s) with:
          - Architecture decision (component, interface, data flow)
          - Design rationale (why this approach)
          - Design constraints (from safety analysis, performance budget)
          - Trace to requirement(s)
          - Interface specification (inputs, outputs, error handling)

Phase 3: TEST (Red)
  Input:  Software requirement + design
  Output: Test case(s) with:
          - Trace to requirement(s) being verified
          - Test procedure (inputs, expected outputs, pass/fail criteria)
          - Coverage target (statement, branch, MC/DC as required by DAL)
          - Test environment specification
  Validation: Tests MUST fail (red) -- confirms they are testing something real

Phase 4: IMPLEMENT (Green)
  Input:  Failing tests + design
  Output: Source code with:
          - Trace to design element(s)
          - Trace to requirement(s) (can be via design)
          - Compliance with coding standard (MISRA, CERT, etc.)
          - Minimum code to pass tests
  Validation: All tests pass (green)

Phase 5: REFACTOR
  Input:  Passing code + design constraints
  Output: Cleaned code with:
          - No behavioral changes (tests still pass)
          - Improved clarity, reduced complexity
          - Updated design documentation if structure changed
  Validation: Tests still pass, traceability still valid

Phase 6: VERIFY
  Input:  All artifacts from Phases 1-5
  Output: Verification evidence:
          - Structural coverage analysis (MC/DC, etc.)
          - Static analysis results (no violations)
          - Traceability matrix update (bidirectional)
          - Review record (who reviewed, what was found)
  Validation: Coverage targets met, no orphan requirements,
              no dead code, traceability complete
```

### 2.3 DRTDD in an Agentic Context

An AI agent running DRTDD would need to:

1. **Parse structured requirements** from a requirements management tool (DOORS, Polarion, Jama) or a structured file (YAML, JSON).
2. **Generate design decisions** with rationale, constrained by architectural rules and safety analysis.
3. **Write tests that trace to requirements** -- not just functional tests but tests that explicitly reference requirement IDs.
4. **Generate minimum implementation** that passes tests while complying with coding standards.
5. **Run verification tools** (static analyzers, coverage tools) and interpret results.
6. **Produce traceability artifacts** in a machine-readable format.

The agent's workflow state machine would look like:

```
[REQUIRE] -> [DESIGN] -> [TEST_RED] -> [IMPLEMENT_GREEN] -> [REFACTOR] -> [VERIFY]
    ^                                                                         |
    |                                        [FAIL: gap found]                |
    +-------------------------------------------------------------------------+
```

### 2.4 Traceability Artifact Format

A practical format for agentic DRTDD traceability (as code):

```yaml
# trace.yaml -- lives alongside the source file
traces:
  - requirement: SW-REQ-0042
    parent: SYS-REQ-0017
    safety_class: ASIL-B
    design_element: DE-0023
    source_files:
      - src/engine/fuel_controller.c:fuel_rate_calculate
    test_cases:
      - test/test_fuel_controller.c:test_fuel_rate_normal_range
      - test/test_fuel_controller.c:test_fuel_rate_boundary_high
      - test/test_fuel_controller.c:test_fuel_rate_sensor_failure
    verification:
      structural_coverage: MC/DC
      coverage_achieved: 100%
      static_analysis: MISRA-C:2012 compliant
      review_record: REV-2026-0147
```

---

## 3. Existing Agentic Coding Frameworks and V-Model Adaptation

### 3.1 Framework Survey

#### Claude Code Workflows (Anthropic)
- **Architecture:** Skill-based pipeline with orchestrator, build, review, and retrospective phases.
- **Key features:** Task contracts (`current_task.yaml`), file boundary enforcement (scope guard), TDD-first build phase, adversarial review gate, retry discipline with root-cause tracing.
- **V-model fit:** The pipeline structure (plan -> build -> review) maps naturally to V-model left side (requirements -> design -> implementation) with the review phase serving as the right side (verification). The `files_to_touch` contract prevents scope creep. The adversarial review is analogous to independent verification.
- **Gaps for compliance:** No formal requirements parsing, no traceability artifact generation, no structural coverage analysis integration, no safety classification awareness.

#### Cursor Rules
- **Architecture:** Project-level `.cursorrules` files that constrain AI behavior within an IDE.
- **Key features:** Per-project instruction files, context-aware completions, multi-file editing.
- **V-model fit:** Rules can encode coding standards and architectural constraints. The IDE context provides visibility into the codebase.
- **Gaps for compliance:** No pipeline/workflow concept, no phase gates, no artifact generation, no traceability, purely suggestion-based (not agentic).

#### Aider
- **Architecture:** CLI-based AI pair programmer with git integration.
- **Key features:** Automatic git commits for every change, repository map for context, multi-file editing, support for multiple LLM backends.
- **V-model fit:** Git-commit-per-change provides an audit trail. Repository map helps with architectural understanding.
- **Gaps for compliance:** No workflow phases, no review gates, no traceability, no safety-awareness, no structured artifact generation.

#### Continue.dev
- **Architecture:** Open-source IDE extension with customizable AI workflows.
- **Key features:** Custom slash commands, context providers, model flexibility.
- **V-model fit:** Customizable enough to build compliance workflows on top.
- **Gaps for compliance:** No built-in compliance features, requires significant customization.

#### OpenHands (formerly OpenDevin)
- **Architecture:** Autonomous AI software engineering agent with sandboxed execution.
- **Key features:** Full development environment access, multi-step task execution, event-based architecture.
- **V-model fit:** Sandboxed execution is valuable for safety (containment). Event stream provides audit trail.
- **Gaps for compliance:** No compliance awareness, no artifact generation, broad permissions model.

#### SWE-agent (Princeton)
- **Architecture:** Research agent for software engineering tasks, focused on bug fixing.
- **Key features:** Agent-Computer Interface (ACI) design, structured tool use.
- **Gaps for compliance:** Research tool, not production-ready, no compliance features.

### 3.2 V-Model Mapping

The V-model has a left side (development) and right side (verification), connected by traceability:

```
System Requirements ─────────────────────── System Testing
        │                                          │
   SW Requirements ──────────────────── Integration Testing
        │                                          │
   Architecture Design ──────────────── Component Testing
        │                                          │
   Detailed Design ──────────────────── Unit Testing
        │                                          │
        └──────── Implementation ──────────┘
```

An agentic framework adapted for V-model compliance would need:

**Left-side skills (development):**
- `skill-requirements-decompose` -- Parse system requirements, generate software requirements with traceability
- `skill-architecture-design` -- Generate/update architecture decisions with rationale
- `skill-detailed-design` -- Generate detailed design (interfaces, algorithms, data structures)
- `skill-implement` -- Generate code following DRTDD (existing `wf-skill-build` extended)

**Right-side skills (verification):**
- `skill-unit-test` -- Generate unit tests traced to detailed design
- `skill-component-test` -- Generate component/integration tests traced to architecture
- `skill-integration-test` -- Generate integration tests traced to SW requirements
- `skill-system-test` -- Generate system test procedures traced to system requirements

**Cross-cutting skills (traceability):**
- `skill-trace-matrix` -- Maintain bidirectional traceability matrix
- `skill-coverage-analysis` -- Run and interpret structural coverage tools
- `skill-static-analysis` -- Run and interpret static analyzers (MISRA, CERT, etc.)
- `skill-review-gate` -- Independent verification gate with checklist per safety level
- `skill-change-impact` -- Analyze impact of requirement or design changes

### 3.3 Adapting Claude Code Workflow for V-Model

The existing Claude Code workflow pipeline can be extended:

```yaml
# Extended pipeline_state.yaml for V-model compliance
pipeline:
  phases:
    - name: requirements_analysis
      skill: skill-requirements-decompose
      inputs: [system_requirements.yaml]
      outputs: [sw_requirements.yaml, trace_matrix.yaml]
      gate: requirements_review

    - name: architecture_design
      skill: skill-architecture-design
      inputs: [sw_requirements.yaml, safety_analysis.yaml]
      outputs: [architecture.yaml, design_rationale.md]
      gate: design_review

    - name: detailed_design_and_implement
      skill: skill-build  # Extended with DRTDD
      inputs: [current_task.yaml, sw_requirements.yaml, architecture.yaml]
      outputs: [source_code, unit_tests, trace_updates]
      gate: code_review

    - name: verification
      skill: skill-verify
      inputs: [all_artifacts]
      outputs: [coverage_report, static_analysis_report, trace_completeness]
      gate: verification_review

    - name: validation
      skill: skill-validate
      inputs: [system_requirements.yaml, test_results]
      outputs: [validation_report]
      gate: validation_review

  traceability:
    format: yaml
    location: .trace/
    bidirectional: true
    orphan_detection: true
```

---

## 4. Continuous Compliance

### 4.1 The Problem with Point-in-Time Compliance

Traditional compliance in safety-critical development is a point-in-time activity: you develop, then you prepare certification artifacts, then you submit for review. This creates several problems:

- **Artifact drift:** Documentation diverges from code over time.
- **Big-bang audits:** Massive effort to prepare certification packages.
- **Late discovery:** Traceability gaps found late are expensive to fix.
- **Change aversion:** Teams avoid changes because of the documentation burden.

### 4.2 Continuous Compliance Defined

Continuous compliance means maintaining certification-ready artifacts as an integral part of every code change, not as a separate phase. Every commit should leave the traceability matrix, design documentation, and verification evidence in a valid state.

Key principles:

1. **Traceability as code:** Requirements traces live in version-controlled files (YAML, JSON) alongside source code, not in a separate tool that drifts.
2. **Compliance CI/CD:** Every pull request runs compliance checks: trace completeness, coverage thresholds, coding standard violations, orphan detection.
3. **Incremental artifact generation:** Each change updates only the affected artifacts, not the entire certification package.
4. **Audit trail by default:** Git history + structured artifacts = auditable change history.
5. **Continuous review readiness:** At any point, the project can produce a certification package from the current state of the repository.

### 4.3 Architecture for Continuous Compliance

```
Developer commit
      │
      ▼
┌─────────────────────────────────────────────────┐
│  CI Pipeline (extended for compliance)          │
│                                                 │
│  1. Build + Unit Tests (standard)               │
│  2. Static Analysis (MISRA/CERT/etc.)           │
│  3. Structural Coverage Analysis                │
│  4. Traceability Validation:                    │
│     - Every requirement has test(s)             │
│     - Every test traces to requirement(s)       │
│     - Every source function traces to design    │
│     - No orphan requirements                    │
│     - No dead code (untraceable)                │
│  5. Artifact Generation:                        │
│     - Updated trace matrix                      │
│     - Coverage report delta                     │
│     - Change impact report                      │
│  6. Compliance Gate:                            │
│     - All checks pass -> merge allowed          │
│     - Any failure -> merge blocked + report     │
└─────────────────────────────────────────────────┘
```

### 4.4 Role of AI Agents in Continuous Compliance

AI agents can automate the most tedious parts of continuous compliance:

**Artifact maintenance:**
- When a developer changes code, the agent updates the trace file, design documentation, and test descriptions.
- When a requirement changes, the agent identifies all affected artifacts and flags them for update.
- The agent can generate first drafts of change impact analyses.

**Compliance checking:**
- The agent can review a PR for compliance completeness before human review.
- It can identify missing traces, incomplete test descriptions, or coding standard violations that static analysis might miss (semantic violations vs. syntactic).

**Documentation generation:**
- Generate requirement paraphrases (derived requirements from system requirements).
- Generate test procedure descriptions from test code.
- Generate design descriptions from code structure.

**Gap analysis:**
- Identify requirements with no tests (coverage gaps).
- Identify code with no requirement trace (potential dead code or missing requirements).
- Identify design decisions with no rationale documented.

### 4.5 Practical Implementation: Trace Files as Code

```
project/
├── requirements/
│   ├── system_requirements.yaml     # From customer/system team
│   └── sw_requirements.yaml         # Derived by SW team (or agent)
├── design/
│   ├── architecture.yaml            # Component structure, interfaces
│   └── detailed_design/
│       └── fuel_controller.yaml     # Per-component design
├── src/
│   ├── fuel_controller.c
│   └── fuel_controller.h
├── test/
│   ├── unit/
│   │   └── test_fuel_controller.c
│   └── integration/
│       └── test_fuel_system.c
├── .trace/
│   ├── requirements_to_tests.yaml   # Bidirectional trace
│   ├── requirements_to_design.yaml
│   ├── design_to_code.yaml
│   └── coverage_baseline.yaml       # Last known coverage state
├── .compliance/
│   ├── rules.yaml                   # Compliance rules for CI
│   ├── coding_standard.yaml         # MISRA/CERT rule selection
│   └── dal_config.yaml              # Safety level configuration
└── ci/
    └── compliance_check.sh          # CI compliance gate
```

### 4.6 Trace File Example

```yaml
# .trace/requirements_to_tests.yaml
traces:
  SW-REQ-0042:
    title: "Fuel rate shall not exceed MAX_FUEL_RATE under any input condition"
    parent: SYS-REQ-0017
    safety_class: ASIL-B
    verification_method: test
    tests:
      - id: UT-FC-001
        file: test/unit/test_fuel_controller.c
        function: test_fuel_rate_normal_range
        status: passing
        last_run: 2026-03-26T14:32:00Z
      - id: UT-FC-002
        file: test/unit/test_fuel_controller.c
        function: test_fuel_rate_boundary_high
        status: passing
        last_run: 2026-03-26T14:32:00Z
      - id: UT-FC-003
        file: test/unit/test_fuel_controller.c
        function: test_fuel_rate_sensor_failure
        status: passing
        last_run: 2026-03-26T14:32:00Z
    coverage:
      statement: 100%
      branch: 100%
      mcdc: 100%
    last_review: REV-2026-0147
    status: verified
```

---

## 5. Legacy Code Retrofit Strategies

### 5.1 The Problem

Most safety-critical software in production was developed before AI coding tools existed, and often before modern practices like TDD were standard. This code may:

- Lack unit tests entirely or have minimal coverage.
- Have documentation that is outdated or missing.
- Have no machine-readable traceability (traces exist only in Word documents or DOORS exports).
- Use coding styles that predate current standards.
- Be critical to operations and cannot be replaced wholesale.

### 5.2 Incremental Retrofit Strategy

The key principle is **incremental improvement without destabilizing existing functionality**. Never attempt a big-bang retrofit.

#### Phase 1: Characterization (AI-Assisted Analysis)

**Goal:** Understand what exists before changing anything.

An AI agent can:
1. **Parse the codebase** and generate a component map (functions, files, call graphs, dependencies).
2. **Identify existing tests** and map them to source functions.
3. **Extract implicit requirements** from code comments, function names, and behavior.
4. **Assess complexity** (cyclomatic complexity, function length, coupling metrics).
5. **Identify coding standard violations** by running static analysis and summarizing results.
6. **Generate a gap report:** What has no tests? What has no documentation? What has no trace?

```yaml
# Output: characterization_report.yaml
components:
  - name: fuel_controller
    files: [src/fuel_controller.c, src/fuel_controller.h]
    functions: 23
    lines_of_code: 1847
    cyclomatic_complexity_max: 42  # Needs refactoring
    existing_tests: 3              # Very low
    estimated_test_gap: 85%
    documentation: partial         # Header comments only
    coding_standard_violations: 17 # MISRA-C:2012
    traceability: none             # No trace files exist
    priority: high                 # Safety-critical, low coverage
```

#### Phase 2: Characterization Tests (Golden Master / Approval Tests)

Before modifying anything, capture current behavior:

1. **AI agent writes characterization tests** that record current outputs for known inputs.
2. These tests do not assert correctness -- they assert *current behavior* (golden master pattern).
3. This creates a safety net: any future change that alters behavior will be detected.

```python
# Example: characterization test generated by AI agent
def test_fuel_rate_characterization_001():
    """
    Characterization test - captures current behavior.
    NOT a correctness assertion. If this fails after a change,
    investigate whether the behavior change is intentional.
    Generated by AI agent on 2026-03-27.
    Source: fuel_controller.c:fuel_rate_calculate()
    """
    result = fuel_rate_calculate(pressure=101.3, temp=25.0, throttle=0.5)
    assert result == 47.2  # Current observed output
```

#### Phase 3: Incremental Documentation

AI agents can generate documentation incrementally, prioritized by risk:

1. **High safety class first:** Start with ASIL-D/DAL-A components.
2. **High complexity first:** Functions with cyclomatic complexity > 15.
3. **High change frequency:** Files that change often in git history.
4. **Interface documentation:** Public APIs and inter-component interfaces.

The agent generates draft documentation that a human reviews and approves:

```c
/**
 * @brief Calculate fuel injection rate based on sensor inputs.
 *
 * @requirement SW-REQ-0042 (ASIL-B)
 * @design DE-0023: PID controller with sensor fusion
 *
 * Implements a PID control loop that fuses pressure and temperature
 * sensor readings to calculate the optimal fuel injection rate.
 * Output is clamped to [0, MAX_FUEL_RATE] to prevent over-fueling.
 *
 * @param pressure  Manifold pressure in kPa (valid: 20.0 - 200.0)
 * @param temp      Intake air temperature in Celsius (valid: -40.0 - 85.0)
 * @param throttle  Throttle position 0.0 - 1.0
 * @return Fuel rate in mL/s, clamped to [0, MAX_FUEL_RATE]
 *
 * @safety If pressure or temp is out of range, returns SAFE_DEFAULT_RATE
 *         and sets fault flag FUEL_SENSOR_FAULT.
 *
 * @ai_generated Documentation generated by AI agent, reviewed by [REVIEWER]
 */
```

#### Phase 4: Test Coverage Ratchet

Incrementally add real tests (not just characterization tests), using a ratchet:

1. Measure current coverage (e.g., 15% statement coverage).
2. Set the CI gate to reject any change that *decreases* coverage.
3. For every change to a file, require that coverage of that file increases by at least N%.
4. AI agent generates test cases for the functions being modified.
5. Over time, coverage monotonically increases.

```yaml
# .compliance/coverage_ratchet.yaml
global_minimum: 15%        # Current baseline
ratchet_direction: up_only # Never allow decrease
per_change_increase: 5%    # Each PR must add 5% to modified files
target: 80%                # Long-term goal
high_priority_files:       # Must reach target first
  - src/fuel_controller.c
  - src/brake_controller.c
```

#### Phase 5: Incremental Traceability

Add trace files incrementally:

1. Start with the highest-criticality components.
2. AI agent extracts requirements from existing documentation (Word, PDF, DOORS exports).
3. Agent generates draft trace files linking requirements to code and tests.
4. Human reviews and corrects traces.
5. CI checks enforce that new code comes with traces.

### 5.3 AI Agent Workflow for Legacy Retrofit

```
┌─────────────────────────────────────────────────┐
│  Legacy Retrofit Pipeline                       │
│                                                 │
│  Input: Legacy source file to retrofit          │
│                                                 │
│  1. ANALYZE                                     │
│     - Parse file, extract functions             │
│     - Run static analysis                       │
│     - Measure complexity and coverage           │
│     - Identify existing tests                   │
│                                                 │
│  2. CHARACTERIZE                                │
│     - Write golden master tests                 │
│     - Run and record current behavior           │
│     - Commit characterization tests             │
│                                                 │
│  3. DOCUMENT                                    │
│     - Generate function-level documentation     │
│     - Extract implicit requirements             │
│     - Generate draft trace files                │
│     - Human review checkpoint                   │
│                                                 │
│  4. TEST                                        │
│     - Generate real unit tests (TDD for fixes)  │
│     - Increase coverage incrementally           │
│     - Add requirement traces to tests           │
│                                                 │
│  5. REFACTOR (if needed)                        │
│     - Reduce complexity                         │
│     - Fix coding standard violations            │
│     - Characterization tests catch regressions  │
│                                                 │
│  6. VERIFY                                      │
│     - Run full verification suite               │
│     - Update traceability matrix                │
│     - Generate compliance report                │
│                                                 │
│  Gate: Human review and approval                │
└─────────────────────────────────────────────────┘
```

---

## 6. AI Agent Limitations for This Use Case

### 6.1 Context Window Limits

**The problem:** Safety-critical projects often have large codebases (millions of lines), extensive requirements documents (thousands of requirements), and complex inter-file dependencies. No current LLM context window can hold an entire project.

**Current state (2025-2026):**
- Claude: up to 200K tokens (standard), ~1M tokens (extended context)
- GPT-4 variants: 128K tokens
- Gemini: up to 1M-2M tokens
- Open-source models: typically 8K-128K tokens

**Impact on compliance workflows:**
- A single DO-178C project may have 5,000+ requirements, 50,000+ lines of code, and thousands of test cases. This exceeds even 1M token windows.
- Traceability analysis requires seeing requirements, design, code, and tests simultaneously.
- Change impact analysis may need to traverse the entire dependency graph.

**Mitigations:**
1. **Hierarchical decomposition:** Break the project into components that fit in context. Process one component at a time with shared context (requirements for that component only).
2. **Repository maps:** Use lightweight summaries (function signatures, dependency graphs) instead of full source when navigating. Aider's "repo map" approach is a good model.
3. **Persistent state files:** Store analysis results in YAML/JSON files that persist between agent invocations. The agent reads prior analysis rather than re-analyzing.
4. **Focused windows:** For each task, load only the relevant slice: the requirements being implemented, the design for that component, the source files being modified, and the related tests.
5. **Multi-agent architecture:** Use specialized sub-agents with focused contexts rather than one agent that needs to see everything.

### 6.2 Instruction Following with Smaller Models

**The problem:** Compliance workflows require precise, multi-step procedures. Smaller and older models (GPT-3.5, Llama 7B-13B, Mistral 7B, Phi-2) struggle with:
- Following complex multi-step instructions.
- Maintaining consistent output formats (YAML schemas, trace formats).
- Adhering to constraints (file boundaries, coding standards).
- Distinguishing between similar but critically different concepts (ASIL-B vs. ASIL-C requirements).

**Measured degradation patterns:**
- **Instruction drift:** After 3-4 steps, smaller models start ignoring earlier instructions.
- **Format collapse:** YAML output becomes malformed, IDs become inconsistent.
- **Constraint violation:** File boundary restrictions are ignored as context grows.
- **Hallucinated references:** Model invents requirement IDs or function names that do not exist.

**Mitigations:**
- See Section 8 for detailed prompt engineering strategies for smaller models.

### 6.3 Hallucination Risks for Compliance Artifacts

This is the most critical limitation. Hallucination in compliance artifacts is not just wrong -- it can be **dangerous and legally liable**.

**Categories of harmful hallucination:**

1. **Fabricated requirements:** Agent generates requirement IDs (SW-REQ-0099) that do not exist in the requirements database. If these end up in trace files, they create false traceability.

2. **Invented test coverage:** Agent claims MC/DC coverage of 100% without running any coverage tool, or misreports coverage numbers.

3. **False compliance claims:** Agent generates a compliance report stating "all MISRA-C:2012 rules satisfied" when violations exist.

4. **Phantom design rationale:** Agent generates plausible-sounding design rationale that does not reflect the actual design decisions. This is especially dangerous because it is hard to detect -- the rationale sounds reasonable but is fabricated.

5. **Incorrect safety analysis:** Agent misclassifies the safety level of a component or generates incorrect failure mode descriptions.

**Mitigations:**

1. **Never trust, always verify:** Every AI-generated compliance artifact must be independently verified by:
   - Running actual tools (coverage analyzers, static analyzers) and comparing results.
   - Cross-referencing requirement IDs against the authoritative requirements database.
   - Having a human reviewer with domain expertise approve every artifact.

2. **Grounding in tool output:** AI agents should not *generate* compliance data; they should *interpret* data from deterministic tools. The coverage number comes from gcov/lcov, not from the AI. The MISRA violations come from PC-lint/Polyspace, not from the AI.

3. **Schema validation:** All AI-generated artifacts must pass strict schema validation before being accepted. This catches format errors but not semantic errors.

4. **Provenance tracking:** Every AI-generated artifact must be marked with:
   - `ai_generated: true`
   - `model: claude-opus-4-6`
   - `human_reviewed: false` (until reviewed)
   - `tool_verified: false` (until tool output confirms)

5. **Dual-path verification:** For critical claims, require two independent sources of evidence. If the agent claims a function satisfies a requirement, both a test and a code review must confirm it.

### 6.4 Non-Determinism

**The problem:** LLMs are inherently non-deterministic (even with temperature=0, due to floating-point non-determinism in parallel computation). This means:
- Running the same prompt twice may produce different code.
- Compliance artifacts may differ between runs.
- Reproducibility -- a core requirement of safety standards -- is compromised.

**Impact:** DO-178C and ISO 26262 require that the development process be repeatable. If an AI agent produces different results each time, the process cannot be repeated for audit purposes.

**Mitigations:**
1. **Version-control everything:** The AI's output is committed to git. The specific output (not the process that generated it) is the auditable artifact.
2. **Deterministic verification:** Even if generation is non-deterministic, verification is deterministic (tests pass or fail, coverage is measured, static analysis is run).
3. **Human approval gate:** The human reviews and approves the specific output, making it "the chosen artifact" regardless of whether re-running would produce something different.
4. **Seed control where possible:** Some APIs support seed parameters. Use them for reproducibility, though this is not guaranteed across API versions.

### 6.5 Liability and Accountability

**The problem:** When an AI agent generates a compliance artifact, who is responsible?
- The developer who invoked the agent?
- The organization that deployed the agent?
- The AI vendor (Anthropic, OpenAI, etc.)?
- The agent framework author?

**Current consensus:** The applicant (the organization seeking certification) bears full responsibility for all artifacts, regardless of how they were produced. This is analogous to how a compiler is a tool -- if the compiler has a bug, the applicant is still responsible for the compiled code.

**Implication:** AI agents must be treated as tools under the applicant's responsibility. This means:
- The organization must demonstrate understanding of the AI's outputs.
- The organization must have processes to detect and correct AI errors.
- The organization cannot claim "the AI did it" as a defense.

---

## 7. Existing Projects and Research Combining AI Agents with Safety Standards

### 7.1 Academic Research

**Formal Methods + LLMs:**
- Research groups at CMU, MIT, and TU Munich have explored using LLMs to generate formal specifications (TLA+, Alloy, Z notation) from natural-language requirements. Early results show promise for simple specifications but high hallucination rates for complex ones.
- The "AutoSpec" line of work uses LLMs to draft formal specifications that are then model-checked. The model checker provides a ground-truth verification layer.

**AI for Requirements Engineering:**
- Natural language processing for requirements quality analysis has been studied for over a decade (pre-LLM). Tools like QualityCheck, AQUSA, and various NLP pipelines check requirements for ambiguity, incompleteness, and inconsistency.
- LLMs significantly improve on older NLP approaches for requirements classification, ambiguity detection, and completeness checking. Research from RE'23 and RE'24 conferences shows 80-90% accuracy on requirements quality tasks.
- Generating test cases from natural-language requirements using LLMs is an active research area. Papers at ICSE 2024-2025 show that GPT-4 and Claude can generate reasonable test cases from requirements but struggle with boundary conditions and negative tests.

**AI for Code Review in Safety Context:**
- Microsoft Research has published on using LLMs for automated code review, including safety-relevant properties.
- Studies on using LLMs to detect security vulnerabilities show mixed results: good at detecting common patterns (buffer overflow, SQL injection) but poor at detecting subtle logic errors.

### 7.2 Industry Initiatives

**SAE International (Automotive):**
- SAE has formed working groups to study AI in automotive software development.
- J3016-related work on AI in autonomous driving is tangentially relevant but focuses on AI as a product component, not AI as a development tool.
- No published standard or guideline specifically addressing AI coding tools in ISO 26262 contexts as of early 2026.

**EASA (Aviation):**
- EASA published "Artificial Intelligence Roadmap 2.0" (2023) and "Concept Paper on AI in Aviation" focusing on AI as an aircraft system, not as a development tool.
- No guidance on AI-assisted coding for DO-178C compliance.
- The CRI (Certification Review Item) process could theoretically be used to propose AI-assisted development on a per-project basis, but no known applicant has done so.

**FDA (Medical Devices):**
- FDA has published guidance on AI/ML-based Software as a Medical Device (SaMD) -- again, AI as the product, not the development tool.
- IEC 62304 does not specifically address AI-assisted development, but the process requirements (traceability, verification, risk management) apply regardless of how the code was produced.
- Some medical device companies use AI coding assistants internally and treat the output as developer-written code, taking full responsibility under their QMS.

**MISRA (Coding Standards):**
- MISRA has not published guidance on AI-generated code specifically.
- MISRA-C:2012 and MISRA-C++:2023 rules apply to all C/C++ code regardless of authorship.
- Some teams use AI to help achieve MISRA compliance by suggesting fixes for violations -- this is well within current accepted practice.

### 7.3 Open-Source Projects

**Known projects at the intersection (as of early 2026):**

1. **SafetyNet AI (experimental):** Several GitHub repositories have appeared exploring AI-generated safety cases. These are mostly proof-of-concept stage, generating Goal Structuring Notation (GSN) diagrams from requirements.

2. **req-trace (various implementations):** Multiple open-source projects implement requirements traceability as code. While not AI-specific, these provide the infrastructure that AI agents would populate. Notable examples:
   - `doorstop`: Python-based requirements management using YAML and git.
   - `strictdoc`: Requirements-as-code tool that supports traceability.
   - `rmtoo`: Requirements management tool in plain text.

3. **AI-FMEA tools:** Some open-source projects use LLMs to assist with Failure Mode and Effects Analysis, generating potential failure modes from system descriptions. These are relevant as FMEA is part of ISO 26262 and other safety standards.

4. **CertBench / ComplianceBench:** Benchmark datasets for evaluating AI's ability to understand and apply safety standards. These are useful for evaluating whether an AI agent can reliably work with compliance requirements.

### 7.4 Notable Gaps

No known open-source project as of early 2026 provides:
- An end-to-end agentic workflow for DO-178C or ISO 26262 compliance.
- Automated bidirectional traceability maintenance using AI agents.
- AI agent qualification evidence under DO-330 or ISO 26262 Part 8.
- A benchmark for evaluating AI agents on safety-critical development tasks.

These gaps represent significant opportunities for the VModelWorkflow project.

---

## 8. Prompt and Skill Design for Smaller and Older Models

### 8.1 The Challenge

The Claude Code workflow system is designed for frontier models (Claude Opus, GPT-4 class). When using smaller models (Claude Haiku, GPT-3.5-turbo, Mistral 7B, Llama 2/3 13B, Phi-2/3), instruction following degrades significantly. For compliance workflows where precision is non-negotiable, this degradation is especially problematic.

### 8.2 Measured Failure Modes with Smaller Models

Based on community reports and benchmarks:

| Failure Mode | Frontier Model | Mid-Tier (Sonnet/GPT-4-mini) | Small (Haiku/GPT-3.5) |
|---|---|---|---|
| Multi-step instruction following | ~95% | ~80% | ~50% |
| Consistent YAML output | ~98% | ~85% | ~60% |
| File boundary respect | ~95% | ~70% | ~40% |
| Requirement ID consistency | ~97% | ~80% | ~45% |
| Coding standard compliance | ~90% | ~70% | ~40% |
| Test-requirement tracing accuracy | ~92% | ~75% | ~35% |

### 8.3 Best Practices for Smaller Models

#### Principle 1: Single Responsibility Per Prompt

**Bad (for small models):**
```
Analyze this function, write documentation, generate tests that trace to
requirement SW-REQ-0042, check MISRA compliance, and update the trace file.
```

**Good (for small models):**
```
TASK: Write a unit test for the function `fuel_rate_calculate`.

INPUT:
- Function signature: int fuel_rate_calculate(float pressure, float temp, float throttle)
- Requirement: SW-REQ-0042 "Fuel rate shall not exceed 100 mL/s"

OUTPUT FORMAT:
```c
// Test ID: UT-FC-XXX
// Requirement: SW-REQ-0042
void test_fuel_rate_[description](void) {
    // Arrange
    [setup]
    // Act
    int result = fuel_rate_calculate([args]);
    // Assert
    TEST_ASSERT([condition]);
}
```

Write exactly ONE test function. Focus on the maximum boundary (100 mL/s).
```

#### Principle 2: Output Templates with Fill-in-the-Blank

Instead of asking the model to generate structured output from scratch, provide a template:

```
Fill in the [BRACKETS] in this trace entry. Do not change anything else.

```yaml
trace:
  requirement: [REQ_ID from the list above]
  test_id: [TEST_ID you just generated]
  test_file: [FILENAME]
  test_function: [FUNCTION_NAME]
  verification_method: test
  status: draft
```
```

#### Principle 3: Explicit Constraint Enumeration

Number your constraints. Small models handle numbered lists better than prose:

```
CONSTRAINTS (you MUST follow ALL of these):
1. Only modify the file `test/test_fuel.c`. Do NOT modify any other file.
2. Use the Unity test framework (TEST_ASSERT macros).
3. Every test function name must start with `test_`.
4. Every test must have a comment with the requirement ID.
5. Do not use dynamic memory allocation (no malloc, calloc, realloc).
6. Maximum function length: 20 lines.
```

#### Principle 4: Validation Loops with Deterministic Checks

Since smaller models are less reliable, add more frequent deterministic validation:

```
Agent Workflow for Small Models:

1. Generate ONE test function
2. VALIDATE: Does it compile? (deterministic check)
3. VALIDATE: Does the test function name match /^test_/? (regex check)
4. VALIDATE: Does it contain a requirement ID comment? (regex check)
5. VALIDATE: Does it use only allowed assertions? (AST check)
6. If any validation fails -> regenerate with error feedback
7. Only after all validations pass -> proceed to next test
```

#### Principle 5: Chain of Responsibility (Pipeline of Small Steps)

Break the workflow into a pipeline where each step has a single, well-defined input and output:

```
Step 1: EXTRACT
  Input: Source file
  Output: List of function signatures (JSON array)
  Model: Small model is fine -- this is pattern extraction

Step 2: CLASSIFY
  Input: One function signature + safety requirements list
  Output: Which requirement(s) this function relates to (JSON)
  Model: Mid-tier model needed -- requires semantic understanding

Step 3: GENERATE TEST
  Input: One function signature + one requirement + test template
  Output: One test function (code)
  Model: Small model with template -- constrained generation

Step 4: VALIDATE
  Input: Generated test
  Output: Pass/Fail + error details
  Model: No model needed -- deterministic tools (compiler, regex)

Step 5: ASSEMBLE
  Input: All validated tests
  Output: Complete test file
  Model: No model needed -- template assembly
```

#### Principle 6: Few-Shot Examples Over Instructions

Small models respond much better to examples than to abstract instructions:

```
TASK: Generate a trace entry for the given function-requirement pair.

EXAMPLE 1:
  Function: brake_force_calculate in src/brake.c
  Requirement: SW-REQ-0011 "Braking force proportional to pedal pressure"
  Trace Entry:
    SW-REQ-0011:
      source: src/brake.c:brake_force_calculate
      test: test/test_brake.c:test_brake_force_proportional
      safety: ASIL-D

EXAMPLE 2:
  Function: steer_angle_limit in src/steering.c
  Requirement: SW-REQ-0028 "Steering angle limited to 45 degrees"
  Trace Entry:
    SW-REQ-0028:
      source: src/steering.c:steer_angle_limit
      test: test/test_steering.c:test_steer_angle_max
      safety: ASIL-B

YOUR TURN:
  Function: fuel_rate_calculate in src/fuel_controller.c
  Requirement: SW-REQ-0042 "Fuel rate shall not exceed 100 mL/s"
  Trace Entry:
```

#### Principle 7: Guard Rails in the Framework, Not the Prompt

Do not rely on the model to self-police. Build enforcement into the framework:

```python
class ComplianceGuard:
    """Framework-level enforcement, independent of model quality."""

    def validate_trace_entry(self, entry: dict) -> list[str]:
        errors = []
        # Check requirement ID exists in database
        if entry['requirement'] not in self.requirements_db:
            errors.append(f"Unknown requirement: {entry['requirement']}")
        # Check source file exists
        if not Path(entry['source']).exists():
            errors.append(f"Source file not found: {entry['source']}")
        # Check test file exists
        if not Path(entry['test_file']).exists():
            errors.append(f"Test file not found: {entry['test_file']}")
        # Check safety classification is valid
        if entry['safety'] not in ['ASIL-A', 'ASIL-B', 'ASIL-C', 'ASIL-D', 'QM']:
            errors.append(f"Invalid safety class: {entry['safety']}")
        return errors

    def validate_generated_code(self, code: str, constraints: dict) -> list[str]:
        errors = []
        # Check banned functions
        for banned in constraints.get('banned_functions', []):
            if banned in code:
                errors.append(f"Banned function used: {banned}")
        # Check file boundaries
        # (parse any file write operations and verify targets)
        # Check coding standard (run actual linter)
        lint_result = run_linter(code, constraints['coding_standard'])
        errors.extend(lint_result.violations)
        return errors
```

### 8.4 Model Selection Strategy for Compliance Workflows

| Task | Minimum Model Tier | Rationale |
|---|---|---|
| Requirements decomposition | Frontier | Requires deep semantic understanding and precision |
| Design rationale generation | Frontier | Must be accurate and non-hallucinated |
| Test generation (from template) | Mid-tier | Constrained output, template-guided |
| Code generation (from design) | Frontier | Safety implications require highest accuracy |
| Trace file updates | Mid-tier | Schema-constrained, validateable |
| Documentation generation | Mid-tier | Can be reviewed and corrected |
| Static analysis interpretation | Mid-tier | Tool output provides grounding |
| Coverage gap identification | Small + tools | Mostly deterministic analysis |
| Boilerplate generation | Small | Low risk, template-based |
| Code formatting/style fixes | Small | Low risk, rule-based |

### 8.5 Skill File Structure for Model Portability

Design skills with model-tier awareness:

```yaml
# skill-generate-test/skill.yaml
name: generate-test
description: Generate a unit test for a function with requirement tracing
model_tiers:
  frontier:
    prompt_file: prompt_frontier.md
    max_steps: 1           # Can do it in one shot
    validation: post_only  # Validate after generation
  mid_tier:
    prompt_file: prompt_midtier.md
    max_steps: 3           # May need retries
    validation: per_step   # Validate after each step
  small:
    prompt_file: prompt_small.md
    max_steps: 5           # Expect more retries
    validation: per_step
    decompose: true        # Break into sub-tasks
    template_required: true # Must provide output template
```

---

## 9. Synthesis and Recommendations

### 9.1 The Opportunity

The intersection of agentic AI workflows and safety-critical software development represents a significant opportunity to:

1. **Reduce the cost of compliance** by automating artifact generation and maintenance.
2. **Improve quality** by enforcing traceability and coverage continuously rather than at audit time.
3. **Make compliance accessible** to smaller organizations that cannot afford dedicated safety teams.
4. **Accelerate development** by automating the tedious parts of safety-critical development while keeping humans in control of critical decisions.

### 9.2 The Risks

The risks are substantial and must be mitigated before deployment:

1. **Hallucinated compliance artifacts** could create a false sense of safety.
2. **Over-reliance on AI** could lead to loss of domain expertise.
3. **Regulatory uncertainty** could result in rejected certification applications.
4. **Liability gaps** could leave organizations exposed.

### 9.3 Recommended Approach for VModelWorkflow

**Phase 1: Foundation (current)**
- Establish the agentic workflow framework with TDD, review gates, and scope guards.
- Build the skill system with clear contracts and validation.
- This is the "learn to walk before you run" phase.

**Phase 2: Traceability Infrastructure**
- Add trace file format and validation tooling.
- Implement continuous compliance CI checks (trace completeness, coverage ratchet).
- Integrate with requirements-as-code tools (doorstop, strictdoc).
- No AI-generated compliance claims yet -- just infrastructure.

**Phase 3: AI-Assisted Compliance**
- Add skills for requirements decomposition, test generation with tracing, and documentation generation.
- Every AI-generated artifact is marked as draft and requires human approval.
- All compliance claims are grounded in deterministic tool output.
- Build the DRTDD loop into the build skill.

**Phase 4: Qualification Evidence**
- Collect data on AI agent accuracy for compliance tasks.
- Build the case for tool qualification (analogous to DO-330 TQL-5: output is verified).
- Engage with certification authorities early.
- Publish results to advance the industry.

### 9.4 Key Design Principles

1. **AI generates drafts, tools verify, humans approve.** Never let AI be the sole source of truth for any compliance artifact.
2. **Traceability as code.** All traces live in version-controlled files, not in external tools that drift.
3. **Continuous, not batch.** Compliance checks run on every commit, not at audit time.
4. **Incremental retrofit.** Never attempt big-bang compliance. Use characterization tests, coverage ratchets, and prioritized documentation.
5. **Model-tier awareness.** Design skills that degrade gracefully with smaller models by decomposing tasks and adding validation.
6. **Framework enforcement over prompt enforcement.** Do not rely on the model to follow rules -- build enforcement into the tooling.
7. **Provenance tracking.** Every AI-generated artifact is marked with its origin, model, and verification status.

### 9.5 Open Questions for Further Research

1. **Can AI agent output be qualified under DO-330?** What evidence would be needed? What TQL level would apply?
2. **How do certification authorities view AI-assisted development?** Has anyone submitted a certification plan that includes AI coding tools?
3. **What is the minimum viable traceability format?** How much structure is needed for machine-readable traces that are also human-readable and auditable?
4. **How to handle requirements changes in an agentic workflow?** When a requirement changes, how does the agent propagate the impact through design, code, tests, and traces?
5. **Multi-agent vs. single-agent architectures for compliance?** Is a specialized agent per V-model phase better than a general agent with compliance skills?
6. **Formal verification integration:** Can LLMs generate formal specifications (TLA+, Coq, Isabelle) that are then machine-verified, providing a higher assurance level than testing alone?

---

## References and Further Reading

### Standards
- RTCA DO-178C, "Software Considerations in Airborne Systems and Equipment Certification," 2011
- RTCA DO-330, "Software Tool Qualification Considerations," 2011
- ISO 26262, "Road vehicles -- Functional safety," 2018 (2nd edition)
- IEC 62304, "Medical device software -- Software life cycle processes," 2006 (Amd 1: 2015)
- IEC 61508, "Functional safety of electrical/electronic/programmable electronic safety-related systems," 2010
- EN 50128, "Railway applications -- Communication, signalling and processing systems -- Software for railway control and protection systems," 2011

### Tools and Projects
- Doorstop: Requirements management using files and version control (https://doorstop.readthedocs.io)
- StrictDoc: Technical requirements and specifications management (https://strictdoc.readthedocs.io)
- Aider: AI pair programming in the terminal (https://aider.chat)
- OpenHands: Open-source AI software engineering agent (https://github.com/All-Hands-AI/OpenHands)
- Continue.dev: Open-source AI code assistant (https://continue.dev)

### Research Directions
- LLM-assisted formal specification generation (AutoSpec, CMU)
- AI for requirements quality analysis (RE conference series)
- AI-generated test cases from requirements (ICSE 2024-2025)
- Automated safety case generation using AI (SafetyNet)

---

*This document represents a synthesis of publicly available information, industry knowledge, and analysis as of March 2026. The field is evolving rapidly; key claims should be verified against current sources before making architectural or business decisions.*
