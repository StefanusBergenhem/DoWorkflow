# Research Summary and Skill Design Input

Summary of findings from the three research documents, with analysis of commonalities/differences and design input for the three skills.

---

## Research Findings Summary

### V-Model Standards on Implementation

The standards are remarkably aligned on **what** but differ on **how prescriptive** they are:

| Theme | DO-178C | ISO 26262 | ASPICE | IEC 62304 |
|-------|---------|-----------|--------|-----------|
| Coding standard required? | Yes, must define before coding | Yes, Table 1 (++ all ASILs) | Assessor expects it | "Should" (not "shall") |
| Language subset | Required | ++ all ASILs | Not explicit | Suggested |
| Complexity limits | Via coding standard | ++ all ASILs | Not explicit | Suggested |
| Design-before-code | Expected | Expected | **Enforced** (#1 audit failure) | Class C only |
| Code reviews | Required, with checklists | Static analysis ++ all ASILs | Expected | Class B/C |
| Dead code rules | Explicit, strict | Not as specific | Not specific | Not explicit |
| Traceability | Bidirectional, every unit | Bidirectional | Bidirectional | Coarser grain |

**The universal message:** Define your rules before you code. Code from a design. Prove compliance. Scale rigor by criticality. No orphan code.

### V-Model Standards on Unit Testing

Key finding: **ISO 26262 is the only standard that explicitly names test derivation methods** (equivalence partitioning, BVA, error guessing in Table 11). DO-178C requires the same practices in substance ("normal range testing" + "robustness testing") but uses different terminology. ASPICE and IEC 62304 leave method choice to the project.

All standards agree: **tests verify the design, not the code.** Structural coverage (statement/decision/MC/DC) is an adequacy check, not a test derivation method.

### Clean Code

The principles cluster around one idea: **code should be simple, focused, and honest.** Small functions doing one thing, named to reveal intent, with no hidden state or side effects. SOLID at the class level, hexagonal architecture at the system level. The practical test for all of it: *is it easy to test in isolation?*

### AI Agent Practices

The data says AI generates ~1.7x more bugs than humans, with specific failure modes: hallucinated APIs, happy-path-only logic, tautological tests, over-mocking, style inconsistency. The remedies are: spec-first, incremental generation, review every line, keep files small and types explicit.

---

## Commonalities Across All Three Perspectives

Massive overlap — they're saying the same things from different motivations:

| Principle | V-Model Standards | Clean Code | AI Best Practices |
|-----------|------------------|------------|-------------------|
| **Design before code** | Required by all standards | "Understand before implementing" | "Spec-first approach" |
| **Small, focused units** | Complexity limits, function size | "Functions do one thing, under 20 lines" | "AI accuracy degrades with size" |
| **Explicit naming** | Naming conventions in coding standard | "Names reveal intent" | "Names are AI's primary context signal" |
| **No dead/orphan code** | Explicit requirement (esp. DO-178C) | "Delete dead code, VCS remembers" | "AI over-generates; prune aggressively" |
| **Error handling** | Required, documented patterns | "Exceptions not error codes, never return null" | "AI omits error paths; make them explicit in spec" |
| **Testability** | Traceability + coverage requirements | "If hard to test, design is wrong" | "Test-first gives AI clear success criteria" |
| **Review** | Code reviews required, scaled by criticality | "Boy Scout Rule" | "Review every AI-generated line" |
| **Consistency** | Coding standard compliance | "Follow conventions, be consistent" | "Provide conventions or AI invents its own" |

**They're saying the same things from different motivations.** Standards want safety evidence. Clean Code wants maintainability. AI practices want accuracy. The solution is the same: small, focused, well-named, well-tested code with clear boundaries.

## Key Differences

| Topic | V-Model | Clean Code | AI Practices |
|-------|---------|------------|-------------|
| **Formality** | Demands documented evidence and audit trail | Principles, not paperwork | Doesn't care about records |
| **Traceability** | Every code unit links to a design element | Not mentioned | Not mentioned (but helps AI) |
| **Independence** | Higher criticality = independent reviewer | Not mentioned | "Don't trust AI output" (same spirit) |
| **Language subsets** | Restrict dangerous features formally | "Avoid problematic patterns" (informal) | Not mentioned |
| **Assurance scaling** | Explicit levels (DAL, ASIL, Class) | Same rigor for everything | Same rigor for everything |
| **Coverage metrics** | Statement/Decision/MC/DC by level | Not mentioned | Not mentioned |

---

## Skill Design Input

### Skill 1: `derive-test-cases` (reworked)

**What changes from current version:** Remove the Pillar 1 dependency (the current skill assumes a specific detailed design YAML format with frontmatter, I-IDs, B-IDs, E-IDs). Instead, accept **any design input** — markdown, YAML, even natural language descriptions — and teach the agent to identify testable elements regardless of format.

**What stays:** The four derivation strategies, coverage matrix output, anti-pattern checks. These are the V-model value-add the model wouldn't do on its own.

**Core insight from research:** ISO 26262 Table 11 explicitly names the methods. DO-178C requires them implicitly. The skill should use the ISO terminology (it's the most explicit) and note the DO-178C equivalence.

### Skill 2: `develop-code` (new)

**Scope:** Pure coding best practices — the intersection of standards compliance, clean code, and AI-aware development. No awareness of our framework.

**What the model wouldn't do on its own:**
- Complexity constraints (quantified limits, not vague advice)
- Error handling discipline (the specific rules: no null returns, no swallowed exceptions, fail-fast)
- Architecture boundary enforcement (business logic has zero infrastructure imports)
- The V-model mandate to code from design, not the other way around

**What to leave out** (model already knows): SOLID basics, naming basics, "write clean code." Keep the skill focused on the things that actually change behavior.

### Skill 3: `develop-in-VModelWorkflow` (new)

**Scope:** Thin bridge layer that makes the agent aware of Pillar 1 and 2. This is where the framework-specific stuff lives.

**Content:**
- Where to find the design input (Pillar 1 artifact schemas, detailed design format)
- What trace artifacts to produce (Pillar 2 trace file format)
- What review preparation looks like (review record schema)
- Where outputs go in the repo structure
- How to use `develop-code` and `derive-test-cases` within the VModelWorkflow context

This keeps the framework knowledge in one place. If Pillar 1/2 schemas change, only this skill updates.
