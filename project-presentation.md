# VModelWorkflow — V-Model Development Framework

## The Problem

Software that can endanger human lives — in aircraft, vehicles, medical devices, railways — must be developed according to strict V-model standards. These standards exist to guarantee that proper engineering discipline, safety analysis, and verification have been applied before software is deployed in the real world.

### What is the V-Model?

The V-model is a development and verification framework used across safety-critical industries. It structures software development as a series of decomposition steps (left side) paired with corresponding verification steps (right side):

```
Left Side (Development)              Right Side (Verification)
─────────────────────────           ──────────────────────────
System Requirements          <-->   System Tests
    │                                       │
SW Requirements              <-->   Integration Tests
    │                                       │
SW Architecture              <-->   Component Tests
    │                                       │
Detailed Design              <-->   Unit Tests
    │
Source Code
```

Each horizontal pair requires **bidirectional traceability** — every requirement must trace to tests, every design to code, and back again. This is universal across all major standards: DO-178C (aviation), ISO 26262 (automotive), ASPICE (automotive process), IEC 62304 (medical), and EN 50128 (railway).

### The Pain Points

**Engineers struggle with V-model compliance:**

- **Understanding what is needed.** Standards are dense, domain-specific documents. Engineers know they need "bidirectional traceability" and "requirements-based testing," but translating that into day-to-day work is hard.
- **Lack of practical frameworks.** Existing tools handle pieces (requirements management, test coverage), but none provide an end-to-end framework connecting templates, traceability, best practices, and development workflow.
- **Documentation is painful.** Traceability maintenance without tooling consumes 30-50% of documentation time. Retrofitting documentation onto legacy code costs 2-3x the effort of greenfield development.
- **Quality disconnect.** Compliance artifacts are often treated as checkbox exercises, disconnected from actual engineering quality. Teams either over-engineer (wasting time) or under-deliver (failing audits).

The result: significant overhead, audit failures, and compliance work that doesn't improve the actual software.

---

## The Opportunity: AI Changes Everything

AI models are becoming increasingly competent at structured engineering tasks. But there is a key insight:

**AI performs better with more guardrails, stricter instructions, and layered design with clear context boundaries. This is exactly what the V-model provides.**

The V-model's hierarchical decomposition maps almost perfectly onto how AI agents work best: focused tasks, clear inputs/outputs, explicit success criteria.

| What Standards Demand | What Clean Code Teaches | What AI Needs |
|----------------------|------------------------|---------------|
| Design before code | Understand before coding | Spec-first approach |
| Small, testable units | Functions under 20 lines | Accuracy degrades with size |
| Naming conventions | Names reveal intent | Names are primary context signal |
| No dead code | Delete unused code | AI over-generates; prune |
| Structured error handling | No null returns | AI omits error paths without guidance |
| Testability | If hard to test, design is wrong | Test-first gives AI success criteria |

**V-model compliance, clean code, and AI-driven development all demand the same discipline — from different motivations.** This convergence is the foundation of VModelWorkflow.

---

## What VModelWorkflow Provides

Four independent components designed to work together:

### 1. Comprehensive Documentation

The foundation of the entire framework. Per artifact type and V-model activity:

- **V-model education** — What each artifact is, where it sits, why it exists, how the layers connect. Detailed enough that someone with no V-model background can understand what to do.
- **Best practices** — Exhaustive guidance on producing high-quality artifacts. How to write good requirements, good designs, good tests. Clean code, clean architecture, safety analysis. Enough for a junior engineer to produce industry-standard output.
- **Anti-patterns** — Common mistakes with concrete examples of what not to do.
- **Examples** — Good and bad artifacts side by side.

This is not a summary — it is equivalent in depth to the union of major V-model standards, expressed in generic terms applicable across all industries.

### 2. Templates & Schemas

Concrete, actionable artifact definitions that will, if followed, produce V-model compliant artifacts that pass assessment:

- **Artifact schemas** for every V-model level (requirements, design, test cases, review records, plans)
- **Assurance level configuration** — How rigor scales with safety level (DAL, ASIL, SIL)
- **Domain translation** — Generic V-model terms mapped to DO-178C, ASPICE, ISO 26262, and other domain vocabularies

### 3. Traceability Framework

A data model and validation engine for linking artifacts across V-model levels:

- **Many-to-many linking** — requirements to designs, designs to code, code to tests, tests back to requirements
- **Automated validation** — orphan detection, coverage analysis, completeness checking, staleness detection
- **Change impact analysis** — when a requirement changes, instantly identify which designs, code, and tests are affected
- **Phased delivery** — starting with AI-driven validation, moving to a deterministic CLI tool for CI/CD, eventually a web GUI for non-developer stakeholders

### 4. AI Skills

Two categories of AI capabilities:

**Craft Skills (standalone, framework-independent):**
- AI-optimized versions of the documentation best practices
- Each skill teaches one thing well: write requirements, derive tests, review designs
- Usable on any project, in any framework — not tied to VModelWorkflow
- Proven to work on smaller, cheaper AI models (not just the largest)

**Framework Skills (VModelWorkflow-specific):**
- Per V-model layer: research/plan with human → agent-orchestrated implementation → human final review
- Template integration, traceability maintenance, review automation
- Human transitions between layers, AI handles implementation within each layer

---

## How It Works Together

Each component is useful alone. The real value is the combination:

| What You Use | What You Get |
|-------------|-------------|
| Documentation alone | Comprehensive V-model education and best practices reference |
| Templates alone | Artifact definitions and checklists for manual V-model compliance |
| Craft skills alone | Quality requirement-writing, test derivation, or code development skills for any project |
| Documentation + Templates + Traceability | Manual V-model development with automated completeness checking |
| All together | **Full agentic V-model development** — AI generates, tools validate, humans verify |

### The Full Workflow

When everything works together, a mid-senior engineer works through each V-model layer:

```
For each V-model layer:

  HUMAN-DRIVEN                    AGENT-ORCHESTRATED              HUMAN-DRIVEN
  ┌─────────────────┐            ┌─────────────────────┐         ┌──────────────┐
  │ Research & Plan │            │ Implementation Loop │         │ Final Review │
  │                 │            │                     │         │              │
  │ Human provides  │──agreed──>│ AI writes artifacts  │──done──>│ Human reviews│
  │ context         │  plan     │ AI self-checks       │         │ and approves │
  │ AI analyzes     │            │ AI review agent      │         │ or rejects   │
  │ impact          │            │ validates            │         │              │
  │ Back-and-forth  │            │ Traceability updated │         │              │
  │ discussion      │            │ automatically        │         │              │
  └─────────────────┘            └─────────────────────┘         └──────────────┘
                                                                        │
                                                                  Next layer ↓
```

This is not an autonomous pipeline. It is a **human-orchestrated workflow** where AI handles the heavy lifting within each layer, and skilled engineers make the strategic decisions and final quality calls.

---

## The Innovation: DRTDD

Design-Requirement-Test Driven Development extends traditional TDD with V-model discipline:

```
REQUIRE  -->  DESIGN  -->  TEST (red)  -->  IMPLEMENT (green)  -->  REFACTOR  -->  VERIFY
   ^                                                                                  |
   +--- [gap found] -----------------------------------------------------------------+
```

Each phase produces traceable artifacts. Human gates between phases. When verification reveals a gap, the loop feeds back to the requirement level — not just to the code.

Standard TDD addresses functional correctness but misses requirements traceability, design rationale, safety analysis integration, and verification completeness. DRTDD closes these gaps.

---

## Primary Market Entry: Legacy Retrofit

The biggest value proposition is not greenfield development — it is taking **existing legacy codebases** and making them V-model compliant.

The volume of legacy code needing compliance far exceeds new development. VModelWorkflow provides specialized AI skills that:

- **Analyze existing code** — understand structure, dependencies, implicit design decisions
- **Reverse-engineer V-model artifacts** — infer requirements, designs, and test coverage from code
- **Identify gaps** — what's missing, what's implicit, what needs formalization
- **Suggest improvements** — based on best practices documentation, highlight where code falls short

This is not a separate product — it's the same components (documentation, templates, traceability, skills) applied bottom-up instead of top-down.

---

## Market Context

### Standards Coverage

VModelWorkflow targets the universal V-model pattern shared across all major safety-critical domains:

| Domain | Standard | Safety Levels | Key Coverage Requirement |
|--------|----------|--------------|--------------------------|
| Aviation | DO-178C / DO-330 | DAL A-E | MC/DC at A, Decision at B, Statement at C |
| Automotive | ISO 26262 | ASIL A-D | MC/DC at D, Decision at C, Statement at B |
| Automotive | ASPICE | Level 1-5 | Process maturity assessment |
| Medical | IEC 62304 | Class A-C | MC/DC at C, Statement at B |
| Railway | EN 50128 | SIL 0-4 | Varies by level |

100% alignment on the universal V-model structure, bidirectional traceability, and requirements-based testing across all five standards.

### AI in Regulated Industries (Current State)

The industry is at an inflection point:

- **No LLM-based tool has been qualified** under DO-330 (the tool qualification standard) as of 2026
- **~60% of DAL-A/B aviation organizations** have blanket bans on generative AI
- **~35% of automotive OEMs** are cautiously piloting AI for non-ASIL or ASIL-A code
- **Documentation generation** is the most widely accepted AI use case in regulated development
- Three emerging approaches: *qualification by verification*, *AI as suggestion engine*, and *deterministic wrappers*

**The gap VModelWorkflow fills:** No existing framework addresses agentic AI (autonomous multi-step workflows) in safety-critical development contexts. VModelWorkflow treats AI as a "suggestion engine" — AI generates candidates, deterministic tools verify, humans approve.

### Competitive Landscape

| Tool | What It Does | What It Misses |
|------|-------------|----------------|
| DOORS / Polarion | Requirements management | No AI integration, no code-level traceability |
| Doorstop / StrictDoc | Requirements-as-code | No test derivation, no design link, limited validation |
| TRLC + LOBSTER (BMW) | Traceability with ISO 26262 support | No AI skills, no templates, not end-to-end |
| SonarQube / SpotBugs | Code quality analysis | No V-model awareness, no requirements link |
| GitHub Copilot / Cursor | AI code generation | No safety awareness, no traceability, no compliance |

**No competitor provides end-to-end DRTDD + V-model compliance + traceability + agentic orchestration.**

---

## Estimated Impact

| Area | Current Cost | With VModelWorkflow |
|------|-------------|-----------------|
| Traceability maintenance | 30-50% of documentation time | Automated — near zero manual effort |
| Independent review overhead | +20-40% development time at highest safety levels | AI pre-review reduces human review burden |
| Legacy code retrofit | 2-3x effort of greenfield | Incremental module-by-module approach with AI analysis |
| Audit preparation | Weeks of evidence gathering | Compliance evidence generated continuously as a byproduct |
| Overall automation potential | — | 60-70% reduction with proper tooling + agent workflows |

---

## First Pilot

- **Target:** Java 17 legacy codebase (100k+ lines, ~10% test coverage, no documentation)
- **Build system:** Gradle, JUnit 5
- **Requirements:** Mixed formats (Word, spreadsheets, DOORS) — possibly incomplete or stale
- **Approach:** Incremental retrofit, module-by-module — not a big-bang rewrite
- **Goal:** Demonstrate that VModelWorkflow can take a real legacy codebase from "undocumented and untested" to "V-model compliant with full traceability" — incrementally, with AI assistance at every step

---

## Build Roadmap

Building bottom-up, one V-model layer at a time. For each layer: documentation first, then templates, then AI skills.

| Phase | Focus | Deliverables |
|-------|-------|-------------|
| **Phase 1** (current) | Code + Unit Tests | Implementation documentation, test documentation, framework skills for lowest V-level |
| **Phase 2** | Detailed Design | Design documentation, write/review skills, research/plan interaction, first full V-pair |
| **Phase 3** | SW Architecture | Architecture documentation, skills, traceability extensions |
| **Phase 4** | SW Requirements | Requirements documentation, EARS skills, full V-chain |
| **Phase 5** | System Level + Legacy | System requirements/tests, legacy retrofit skills, deterministic traceability engine |
| **Phase 6** | Polish + Tooling | Reviews, plans, translations, scaffold tool, web GUI |

---

## Summary

VModelWorkflow bridges the gap between what safety standards require and what development teams can practically deliver. It recognizes that V-model discipline, clean code principles, and AI-driven development are the same discipline viewed from different angles.

The framework is:
- **Modular** — adopt what you need, each component works independently
- **Incremental** — works on legacy code, module-by-module
- **Domain-agnostic** — same core across aviation, automotive, medical, railway
- **AI-native** — designed for mid-senior engineers orchestrating AI agents
- **Proven by documentation** — every claim backed by exhaustive best practices documentation

**The V-model was designed for human discipline. It turns out to be the perfect structure for AI discipline too.**
