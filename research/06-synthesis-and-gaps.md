# Synthesis: Cross-Domain V-Model Patterns and Gap Analysis

## 1. The Universal V-Model Abstraction

Across DO-178C, ASPICE/ISO 26262, IEC 62304, and EN 50128, the same structural pattern emerges:

```
Stakeholder/System Requirements
        |  ^
        v  |
   System Requirements  <------->  System Qualification Tests
        |  ^
        v  |
   SW Requirements      <------->  SW Qualification Tests
        |  ^
        v  |
   SW Architecture       <------->  Integration Tests
        |  ^
        v  |
   Detailed Design       <------->  Unit Tests
        |  ^
        v  |
     Source Code
```

Each horizontal pair requires **bidirectional traceability**. The left side decomposes, the right side verifies. This is domain-invariant.

## 2. Common Artifact Types (Domain-Agnostic)

| Abstract Artifact | DO-178C | ASPICE | Purpose |
|---|---|---|---|
| Safety/Assurance Plan | PSAC | Safety Plan | Top-level compliance strategy |
| Development Plan | SDP | Project Plan | How work will be done |
| Verification Plan | SVP | Test Plan(s) | How verification is done |
| Configuration Mgmt Plan | SCMP | CM Plan | How artifacts are managed |
| Quality Plan | SQAP | QA Plan | How quality is assured |
| SW Requirements | SRD (HLR) | SWE.1 WP | What the SW must do |
| SW Architecture | SDD (arch) | SWE.2 WP | How SW is structured |
| Detailed Design | SDD (LLR) | SWE.3 WP | How components work |
| Source Code | Source Code | Source Code | The implementation |
| Test Cases & Procedures | SVCP | Test Specs | How to verify |
| Test Results | SVR | Test Reports | Evidence of verification |
| Traceability Data | Trace Data | Trace Matrix | Links between all levels |

## 3. Common Process Requirements

Regardless of domain, every V-model standard requires:

1. **Bidirectional traceability** between adjacent levels
2. **Requirements-based testing** (every requirement has at least one test)
3. **Structural coverage analysis** (scaled by safety level)
4. **Configuration management** (baselines, change control)
5. **Independent verification** (review by someone other than author)
6. **Problem reporting and resolution**
7. **Change impact analysis** (when something changes, what's affected?)

## 4. Safety Level Mapping

| Rigor | DO-178C | ISO 26262 | IEC 62304 | Coverage Requirement |
|---|---|---|---|---|
| Highest | DAL A | ASIL D | Class C | MC/DC |
| High | DAL B | ASIL C | Class C | Decision + MC/DC |
| Moderate | DAL C | ASIL B | Class B | Decision |
| Low | DAL D | ASIL A | Class A | Statement |
| None | DAL E | QM | - | Best practice |

## 5. What Exists That We Can Build On

### Requirements-as-Code Tools
- **doorstop** (Python): YAML-based requirements with traceability, git-native
- **strictdoc** (Python): RST-based, supports traceability and export
- **rmtoo**: Plain-text requirements management

### Agentic Framework
- **Claude Code Workflow** (already in this repo): pipeline orchestration, TDD, review gates, scope guards

### Key Gap: Nothing Combines These
No existing project provides:
- End-to-end agentic V-model workflow
- EARS-based requirement parsing integrated with code generation
- Automated bidirectional traceability maintenance
- DRTDD (Design-Requirement-Test Driven Development) loop
- Legacy codebase scanning and artifact generation
- Model-tier-aware skills for smaller LLMs

## 6. The DRTDD Loop (Our Core Innovation)

```
REQUIRE → DESIGN → TEST(red) → IMPLEMENT(green) → REFACTOR → VERIFY
    ^                                                           |
    +--- [gap found] ------------------------------------------+
```

Each phase produces traceable artifacts. The agent maintains traceability automatically. Human gates exist between REQUIRE→DESIGN and after VERIFY.

## 7. Legacy Retrofit Strategy

For existing codebases without compliance artifacts:

1. **Scan**: Analyze code structure, dependencies, complexity
2. **Recover**: Reverse-engineer architecture from code
3. **Characterize**: Write characterization tests for existing behavior
4. **Document**: Generate requirement candidates from behavior
5. **Trace**: Establish traceability links (code → tests → requirements)
6. **Harden**: Fill coverage gaps, add missing tests
7. **Validate**: Human review of all generated artifacts
