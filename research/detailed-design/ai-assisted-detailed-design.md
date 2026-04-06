# AI-Assisted Detailed Design Quality

**Research for:** Section 3.7 (AI-Assisted Detailed Design)
**Date:** 2026-04-05
**Feeds into:** `docs/guide/artifacts/detailed-design.html`

---

## Overview

This research examines the current state of AI (specifically LLM-based tools) applied to detailed design activities: generating design from requirements, reverse-engineering design from existing code, and the quality failure modes that accompany both. A secondary focus is the regulatory implications for safety-critical software development under standards such as DO-178C (aviation) and ISO 26262 (automotive).

The honest finding is this: as of 2026, LLMs can produce design-shaped text quickly and fluently, but the correspondence of that text to correct, complete, and verifiable design intent is unreliable and understudied. Empirical evidence is sparse. Industry experience is mixed. Regulatory guidance explicitly defers specifics. The sections below are careful to distinguish what has been empirically measured from what is practitioner opinion or inference from adjacent studies.

---

## Part 1: AI Generating Design from Requirements

### 1.1 What LLMs Can Currently Do

LLMs can take natural language requirements and produce design artifacts that *look* like detailed design: interface specifications with method signatures, data types, and behavioral descriptions; state machine descriptions in prose or structured notation; module decompositions; and pseudo-code that implies an implementation structure.

The most rigorous study framework for this domain is an empirical study design accepted at SANER 2026 (Stage 1 Registered Report), which investigates "specification-driven code generation" — the workflow where a human writes a specification and an LLM generates conforming code. The study uses CURRANTE, a VS Code extension that separates specification, test, and function generation into three sequential human-in-the-loop stages. The fact that the study is structured this way — with human checkpoints at every stage — is itself a signal about where the research community believes human oversight is mandatory.

Source: "Understanding Specification-Driven Code Generation with LLMs: An Empirical Study Design." SANER 2026, Stage 1 Registered Report. https://arxiv.org/html/2601.03878v1

### 1.2 Requirements Engineering Studies — Adjacent Evidence

LLM use in requirements engineering (RE) is better studied than LLM use in design generation, and the RE findings are directly relevant because design generation inherits RE's failure modes and adds its own.

A 2025 systematic review in *Frontiers in Computer Science* found that LLMs can automate creation of user stories, epics, and functional design specification documents, but "final outputs still require expert review to ensure technical completeness and contextual specificity." The same review found that Zero-shot and Few-shot prompting dominate (73% of published approaches combined), while more robust approaches like RAG and interactive prompting remain under-explored at 6% and 5% respectively.

Source: "Research directions for using LLM in software requirement engineering: a systematic review." *Frontiers in Computer Science*, 2025. https://www.frontiersin.org/journals/computer-science/articles/10.3389/fcomp.2025.1519437/full

A 2025 paper ("Large Language Models (LLMs) for Requirements Engineering") surveys the state of LLM4RE across published literature, noting that LLM4RE studies now account for almost a fifth of all NLP4RE publications. The paper documents that common defects in LLM-generated user stories include conjunctions that compromise atomicity and inconsistencies in format or structure of acceptance criteria across items — defects that carry directly into design artifacts derived from those requirements.

Source: "Large Language Models (LLMs) for Requirements Engineering (RE)." arXiv:2509.11446. https://arxiv.org/pdf/2509.11446

### 1.3 Quality of AI-Generated Specifications — What the Data Shows

The clearest empirical signal on LLM-generated specification quality comes from code generation studies that use specifications as input. A 2025 analysis ("Where Do LLMs Still Struggle? An In-Depth Analysis of Code Generation Benchmarks") found that LLMs are inconsistent in reasoning across complex specification scenarios: "ChatGPT showed inconsistency between the reasoning steps obtained and the final verdict, occasionally mis-categorizing a variable despite intermediate steps suggesting otherwise." If LLMs cannot reason consistently about the specifications they receive, their output when generating specifications from requirements will exhibit the same instability.

Source: "Where Do LLMs Still Struggle? An In-Depth Analysis of Code Generation Benchmarks." arXiv:2511.04355. https://arxiv.org/html/2511.04355v1

The 2025 paper "Leveraging LLMs for Formal Software Requirements: Challenges and Prospects" identifies a scaling problem: "As program size and complexity of the specification goal increase, the chances of ambiguity, under-specification, and logical inconsistency increase." This is directly relevant to detailed design: component-level design documents are more complex than individual requirements, so the inconsistency risk scales upward.

Source: "Leveraging LLMs for Formal Software Requirements: Challenges and Prospects." arXiv:2507.14330. https://arxiv.org/html/2507.14330v1

### 1.4 Design Patterns and Structural Understanding

A 2025 study ("Do Code LLMs Understand Design Patterns?") evaluated LLM capability on recognizing and applying design patterns. The finding: "Current code LLMs often fail to properly understand existing design patterns and coding styles of a project, leading to generated code that doesn't meet project requirements, creating an additional burden for developers requiring extensive code revisions and post-processing."

This is not a marginal finding. A detailed design document for a safety-critical system typically specifies precise structural patterns — which components use which design patterns, which interfaces are abstract vs. concrete, and why. LLMs that do not reliably understand design patterns when reading code are unlikely to apply them correctly when generating design from requirements.

Source: "Do Code LLMs Understand Design Patterns?" arXiv:2501.04835. https://arxiv.org/html/2501.04835v1

---

## Part 2: AI Reverse-Engineering Design from Legacy Code

### 2.1 Why This Is the Primary Use Case

For legacy retrofit — the primary market entry point for VModelWorkflow — the starting point is existing code with little or no design documentation. The question is not "can AI generate design from requirements?" but "can AI produce useful design documentation from code?" These are different problems with different quality profiles.

The critical framing distinction is between two possible outputs:
- **Code paraphrase**: the AI describes what the code does, line by line or block by block, in English or pseudo-formal notation. This is not design documentation; it is a different encoding of the same information, with all the same gaps.
- **Recovered design intent**: the AI identifies the abstractions, boundaries, invariants, and behavioral contracts that the code *implements*, expressed at a level above the implementation. This is what design documentation is supposed to contain.

The gap between these two outcomes is the central quality question for AI-assisted legacy retrofit, and empirical research on it is in early stages.

### 2.2 Architecture Recovery Studies (2025)

Two 2025 papers directly address automated architecture recovery from source code using LLMs.

**Paper 1: Generating Software Architecture Description from Source Code using Reverse Engineering and LLM** (arXiv:2511.05165)

This paper proposes a semi-automated approach that combines traditional reverse engineering tools with LLMs. The pipeline: (1) reverse engineering tools extract a class diagram from source code; (2) an LLM abstracts this into a component diagram using prompt engineering; (3) the LLM generates state machine diagrams from component-level code using few-shot prompting. The authors report that LLMs demonstrate "potent capability to abstract the component diagram, thereby reducing the reliance on human expert involvement, and accurately represent complex software behaviors, especially when enriched with domain-specific knowledge through few-shot prompting."

The honest limitation: the paper reports qualitative assessment of a single case study. There is no quantitative quality metric comparing LLM-recovered designs to authoritative ground-truth designs. The "accuracy" claim is not operationally defined.

Source: "Generating Software Architecture Description from Source Code using Reverse Engineering and Large Language Model." arXiv:2511.05165. https://arxiv.org/abs/2511.05165

**Paper 2: ArchAgent — Scalable Legacy Software Architecture Recovery with LLMs** (arXiv:2601.13007)

ArchAgent combines static analysis, adaptive code segmentation, and LLM synthesis to reconstruct multi-view, business-aligned architectures from cross-repository codebases. The paper reports "significant improvements over existing benchmarks" and confirms that "dependency context improves the accuracy of generated architectures of production-level repositories." An ablation study confirms that including cross-repository dependency context improves outputs.

This is a stronger empirical claim — ablation studies with baselines — but the benchmark is other automated tools, not human-authored ground-truth designs. What "accuracy" means in the absence of ground truth is a genuine methodological gap across this entire research area.

Source: "ArchAgent: Scalable Legacy Software Architecture Recovery with LLMs." arXiv:2601.13007. https://arxiv.org/html/2601.13007

**LLM-Assisted Architecture Traceability Recovery** (ICSA 2025)

A paper from ICSA 2025 specifically addresses using LLMs to recover traceability links between architecture descriptions and source code, framing the problem as architecture entity recognition. The result: LLMs can identify component names and structural relationships with useful precision, but struggle with "complex abstractions such as class relationships and fine-grained design patterns."

Source: "Enabling Architecture Traceability by LLM." ICSA 2025. https://fuchss.org/assets/pdf/2025/icsa-25.pdf

### 2.3 Comparison Study: LLMs vs. Model-Driven Reverse Engineering

A July 2025 paper in *Frontiers in Computer Science* directly compares LLMs against traditional model-driven reverse engineering (MDRE) tools for reverse engineering tasks. Key finding: LLMs and MDRE have complementary strengths — LLMs perform better on tasks requiring natural language understanding and abstraction, while MDRE tools perform better on tasks requiring formal structural extraction. The paper introduces MDRE-LLM, a hybrid tool that combines both approaches.

Source: "A comparison of large language models and model-driven reverse engineering for reverse engineering." *Frontiers in Computer Science*, July 2025. https://www.frontiersin.org/journals/computer-science/articles/10.3389/fcomp.2025.1516410/full

### 2.4 The Post-Hoc Design Trap

The Gartner Peer Community discussion on using LLMs for mainframe legacy code reverse engineering (posted 2024–2025) is notable as practitioner-level evidence. A practitioner with direct experience writes: "My limited experimentation and research tells me I am being overly optimistic" regarding the goal of generating requirements and technical designs directly from code. The response thread notes that LLMs "quite good at providing low-level documentation" (i.e., code paraphrase) but the gap to business-process-level design remains large.

Source: Gartner Peer Community. "Has anybody considered using LLM's to reverse engineer legacy (mainframe) code to generate documentation?" https://www.gartner.com/peer-community/post/anybody-considered-using-llm-s-to-reverse-engineer-legacy-mainframe-code-to-generate-documentation-github-copilot-quite-good

Thoughtworks, in their Technology Radar (2024–2025), rates "Using GenAI to understand legacy codebases" as a "Trial" technique, noting clear evidence that it "can significantly accelerate comprehension of large and complex systems" through tools such as Cursor, Claude Code, Copilot, and others. However, the Thoughtworks framing is explicitly about *understanding* codebases — surfacing business rules, summarizing logic, identifying dependencies — not about producing V-model-compliant design documentation. The distinction matters: comprehension assistance is not design documentation.

Thoughtworks also highlights that a knowledge-graph RAG approach "preserves structural information about the codebase beyond what an LLM could derive from the textual code alone," which is particularly relevant for legacy codebases that "are less self-descriptive and cohesive." Setup effort for such approaches "scales with the size and complexity of the codebase."

Source: Thoughtworks Technology Radar. "Using GenAI to understand legacy codebases." https://www.thoughtworks.com/en-us/radar/techniques/using-genai-to-understand-legacy-codebases

### 2.5 User Story Recovery from Code — Specific Findings

A 2025 paper ("Reverse Engineering User Stories from Code using Large Language Models," arXiv:2509.19587) investigates whether LLMs can recover user stories from source code. User stories are a simpler artifact than detailed design documents, yet the study finds that "prompt design impacts output quality" significantly and that results vary substantially by codebase characteristics. The implication: if user story recovery from code is quality-sensitive to prompt design at this level, detailed design recovery — a structurally richer artifact — will exhibit greater sensitivity.

Source: "Reverse Engineering User Stories from Code using Large Language Models." arXiv:2509.19587. https://arxiv.org/html/2509.19587v1

---

## Part 3: AI Design Quality Failure Modes

### 3.1 Hallucinated Constraints

The hallucination problem in LLM-generated specifications is well-documented in the adjacent literature. A December 2024 survey ("A Survey of Bugs in AI-Generated Code," arXiv:2512.05239) categorizes hallucination as a distinct bug class: "the generation of inaccurate or fabricated information by a model occurs despite appearing plausible on the surface." For design documents, this manifests as:

- Interface constraints stated with false precision (e.g., specific timing bounds, buffer size limits, concurrency semantics that the model inferred but no requirement specifies)
- Behavioral invariants that sound authoritative but have no traceability to any requirement
- Design rationale that is plausible-sounding but fabricated — the AI explains *a* reason for a design decision, but not *the* reason made by the original authors

Source: "A Survey of Bugs in AI-Generated Code." arXiv:2512.05239. https://arxiv.org/html/2512.05239v1

### 3.2 Edge Case and Boundary Omission

A consistent finding across AI code generation research is that "AI models tend to focus on typical scenarios while neglecting edge cases." For design documentation, this is potentially more damaging than for code: code that misses an edge case fails at runtime, where it can be caught. Design documentation that omits an edge case creates a gap in the specification that is not caught until much later — or not caught at all if verification coverage is derived from the (incomplete) design rather than from independent requirements analysis.

Source: AI Code Generation Risks analysis. https://www.highvisionsystems.com/ai-code-generation-risks/

### 3.3 Missing Rationale — The Invisible Failure

Design documentation serves two purposes: (1) specifying what the system does, and (2) explaining why design decisions were made. AI tools can attempt the first. The second is structurally inaccessible: the *why* of design decisions is not in the code. It exists in the minds of original designers, in discarded alternatives, in constraint discussions that never made it into version control. An LLM reading code cannot recover this; it can only invent a plausible-sounding rationale, which is worse than silence because it obscures the gap.

This connects directly to the Parnas and Clements framework (see design-documentation-craft.md): design documentation is the artifact that makes intent, decisions, and constraints visible and independently verifiable. AI-generated design documentation from code will systematically lack the decision rationale that gives design documents their verification value.

### 3.4 Structural Code Quality Effects

The January 2024 GitClear study ("Coding on Copilot: 2023 Data Suggests Downward Pressure on Code Quality") analyzed approximately 153 million changed lines of code from 2020–2023. It found that code churn — lines reverted or updated within two weeks — is projected to double in 2024 compared to the pre-AI 2021 baseline. Static analysis warnings increase by 30% and code complexity increases by 41% after LLM tool adoption.

This is evidence about code quality, not documentation quality. But if AI-generated code has these structural defects, then design documentation derived from that code inherits them. Conversely, design documentation generated for a system will be implemented with AI tools that exhibit these characteristics — meaning the gap between specified design and actual implementation is likely to widen.

Source: GitClear. "Coding on Copilot: 2023 Data Suggests Downward Pressure on Code Quality." January 2024. https://www.gitclear.com/coding_on_copilot_data_shows_ais_downward_pressure_on_code_quality

### 3.5 Documentation Quantity vs. Quality — DORA 2024

Google's 2024 DORA report ("Accelerate State of DevOps Report 2024") provides the most widely-cited industry-scale data on AI's impact on software development. For documentation specifically: documentation was among the top five task types where AI assistance is used. However, the report also found that increased AI adoption was "accompanied by an estimated decrease in delivery throughput by 1.5% and an estimated reduction in delivery stability by 7.2%," with the decline attributed partly to "developers over-relying on AI-generated code, leading to larger, less manageable change lists."

The DORA data does not measure documentation quality directly. What it establishes is that AI use increases documentation *production* but does not straightforwardly improve development *outcomes*. These are different things.

Source: Google DORA. "Accelerate State of DevOps Report 2024." https://dora.dev/research/2024/dora-report/

---

## Part 4: Safety-Critical Implications

### 4.1 DO-178C and Independence Requirements

DO-178C (RTCA/EUROCAE ED-12C, 2012) requires that for Design Assurance Level A and B software, the person verifying an artifact may not be the person who authored it, and this separation must be clearly documented. The standard requires "end-to-end, bidirectional traceability from system requirements to software requirements, design, code, tests, and verification results."

The AI authorship question creates an immediate practical problem: if an LLM generates a design document, what is the authorship status of that document? The standard's independence requirement is written around human author/reviewer pairs. AI-generated content where the human role is prompt engineering and review creates ambiguity about where development ends and verification begins. The DER (Designated Engineering Representative) or equivalent authority must accept the evidence package; no current FAA or EASA guidance specifies how AI-generated lifecycle data is assessed.

Source: AdaCore. "A Fresh Take on DO-178C Software Reviews." https://www.adacore.com/blog/a-fresh-take-on-do-178c-software-reviews

Source: DO-178C overview, ConsuNova. https://consunova.com/do-178c/certification-unpacked-a-practical-guide-to-do-178c-certification-explained/

### 4.2 FAA Position (2024)

The FAA released "Roadmap for Artificial Intelligence Safety Assurance Version I" in July 2024. Key characteristics of the FAA's current stance:

- The roadmap takes an explicitly incremental approach: "Incrementally implementing AI in aviation, learning and adapting safety assurance methods based on real-world application and experience."
- The FAA cautions against anthropomorphizing AI: "The system designer must delineate the responsibilities that are assigned to human beings as compared to the requirements that are assigned to systems and tools."
- The FAA distinguishes "learned AI" from "learning AI" — a distinction that matters for design tools but is not yet operationalized in guidance.
- Certification position papers were expected in Q1 2026. As of this writing (April 2026), those papers have not yet been incorporated into this research.
- The AI/ML Certification Framework focuses on low-risk/low-safety AI applications; high-assurance AI certification methodology remains an open problem.

Source: FAA. "Roadmap for Artificial Intelligence Safety Assurance Version I." July 2024. https://www.faa.gov/aircraft/air_cert/step/roadmap_for_AI_safety_assurance

Source: FAA Presentation to REDAC-NAS, Fall 2024. https://www.faa.gov/about/office_org/headquarters_offices/ang/redac/REDAC_508.06_Fall_2024_FAA_Roadmap_on_AI_Safety_09042024

### 4.3 EASA Position (2025)

EASA published Notice of Proposed Amendment NPA 2025-07 in 2025, which proposes detailed specifications on AI trustworthiness for aviation, aligned with the EU AI Act (Regulation (EU) 2024/1689). The NPA covers:

- Level 1 AI: AI assistance to humans
- Level 2 AI: Human-AI teaming
- Level 3 AI (advanced automation): guidance estimated to be ready by end of 2025

NPA 2025-07 focuses on setting AI trustworthiness requirements rather than specifying how AI-generated lifecycle data (such as design documents) is treated within DO-178C certification. A second NPA planned for 2026 will deploy the generic framework to specific aviation domains. The consultation period closed March 2026; final rule-making is ongoing.

The EASA initiative "has potential to define global standard" given FAA's historically slower rulemaking pace.

Source: EASA. "NPA 2025-07 — Detailed specifications and associated acceptable means of compliance and guidance material — Artificial intelligence trustworthiness." https://www.easa.europa.eu/en/document-library/notices-of-proposed-amendment/npa-2025-07

Source: JDA Solutions. "EASA's AI initiative has potential to define global standard." https://jdasolutions.aero/blog/does-easas-ai-initiative-require-immediate-faa-response/

### 4.4 ISO/IEC TR 5469:2024 — AI in Functional Safety

For automotive (ISO 26262) and related functional safety domains, ISO/IEC TR 5469:2024 "Artificial intelligence — Functional safety and AI systems" was published in January 2024. It describes properties and risk factors of AI in functional safety contexts. ASPICE and ISO 26262 do not yet contain AI-specific design documentation guidance, but TR 5469 establishes the framework within which such guidance will emerge.

Source: ISO/IEC TR 5469:2024. Referenced in: Parasoft. "ISO 26262 Software Compliance in the Automotive Industry." https://www.parasoft.com/learning-center/iso-26262/what-is/

Source: Frontiers paper on certifiable AI in aviation: "Towards certifiable AI in aviation: landscape, challenges, and opportunities." arXiv:2409.08666. https://arxiv.org/html/2409.08666v1

### 4.5 Honest Gap Assessment

No published study as of April 2026 has evaluated whether LLM-generated detailed design documentation can pass a DO-178C software review at any DAL level. The question has not been empirically tested in the open literature. The regulatory frameworks (FAA, EASA) are in active development and have not yet produced specific guidance for AI-generated lifecycle data. Any claim that AI-generated design documentation can satisfy certification requirements must currently be treated as unsubstantiated.

---

## Part 5: Mitigation Strategies

### 5.1 Structured Templates as Guardrails

The research literature on LLM hallucination and under-specification consistently finds that structured prompting reduces failure rates. For design documentation, this translates to: structured templates constrain what the AI can generate to the schema of what a valid design document contains. An LLM generating a design document against a template that requires explicit interface pre/post-conditions, explicit error handling states, and explicit traceability to requirements cannot silently omit these elements — the template structure makes omission visible.

Source: "Leveraging LLMs for Formal Software Requirements: Challenges and Prospects." arXiv:2507.14330. https://arxiv.org/html/2507.14330v1

Source: TechOps framework for AI Act documentation. arXiv:2508.08804. https://arxiv.org/html/2508.08804v1

### 5.2 Human Review Patterns

The research consensus is that AI-generated design documentation requires substantive human review — not superficial approval of plausible-looking output. Multiple sources distinguish "human-in-the-loop" (human approves each step) from "human-on-the-loop" (human monitors overall process). For safety-critical design documentation, the minimum viable model is human-in-the-loop at every significant decision point.

The SANER 2026 empirical study design (specification-driven generation) structures exactly this: separate human verification stages for specification, tests, and implementation. This three-stage model is a practical pattern for AI-assisted design work.

Source: SANER 2026 study design. https://arxiv.org/html/2601.03878v1

The specific failure mode to guard against is what the DORA report data implies: AI increases documentation production while potentially reducing quality. A review process that checks volume ("is there a design section for each requirement?") without checking content quality ("does this design section specify the correct pre/post-conditions with traceability?") will not catch the failure modes described in Part 3.

### 5.3 AI as Analyst, Not Author

The Thoughtworks framing is useful here: GenAI for legacy codebases is a "comprehension accelerator" — it helps engineers understand what the code does, surface patterns, and identify boundaries. The engineer then authors the design document using that understanding. This model — AI as analyst, human as author — avoids the hallucinated-rationale problem because the human author supplies the design intent and judgment; the AI supplies the reading efficiency.

Source: Thoughtworks Technology Radar. https://www.thoughtworks.com/en-us/radar/techniques/using-genai-to-understand-legacy-codebases

### 5.4 Knowledge Graph RAG for Legacy Codebases

For large legacy codebases, the Thoughtworks recommendation of knowledge-graph RAG is empirically better-grounded than naive LLM querying. A knowledge graph preserves structural relationships (call graphs, dependency graphs, inheritance hierarchies) that LLMs cannot reliably reconstruct from flat text. Using a knowledge graph as the retrieval substrate for design document generation ensures that the structural skeleton of the recovered architecture is accurate even if the LLM-generated prose requires human correction.

Source: Thoughtworks Technology Radar. GraphRAG technique. https://www.thoughtworks.com/radar/techniques/graphrag

### 5.5 Traceability as the Quality Gate

The most robust mitigation against AI design document quality failures is bidirectional traceability enforcement. If every design claim must trace to a specific requirement, and every requirement must have at least one design element addressing it, then:

- Hallucinated constraints that have no requirement parent are caught at traceability validation time
- Missing coverage of requirements (edge cases, error conditions) is caught as traceability gaps
- Inconsistencies between design sections become detectable as conflicting traces to the same requirement

This is a mechanical enforcement mechanism that does not require the human reviewer to read every sentence for correctness — it requires that the *structure* of coverage is complete. Human review can then focus on the substantive content of each traced claim.

The limitation: traceability enforcement catches structural gaps; it does not catch *wrong* content that traces correctly. A hallucinated timing constraint that traces to a performance requirement still passes structural traceability validation. Content correctness requires domain expertise, not tooling.

---

## Part 6: Empirical Data — Honest Assessment

### 6.1 What Exists

The empirical base for AI-generated design documentation quality (as distinct from code generation or requirements generation) is thin. As of April 2026, no controlled study has been published that:

- Takes a set of real-world requirements
- Has both humans and LLMs produce detailed design documentation for those requirements
- Has independent domain experts evaluate the design documents on completeness, correctness, and fitness for V-model assessment

The closest adjacent evidence comes from:

1. **Requirements generation quality** — user story defect studies, LLM RE surveys (see Part 1)
2. **Code generation quality** — GitClear, DORA, hallucination surveys (see Part 3)
3. **Architecture recovery** — arXiv:2511.05165, arXiv:2601.13007 (see Part 2) — but without ground-truth comparison against human-authored designs

### 6.2 What Industry Reports (Without Rigorous Measurement)

Practitioner evidence is present but methodologically weak:

- 46% of developers "do not fully trust AI results" and only 3% "highly trust" AI-generated outputs (AI coding assistant statistics, 2025, via Second Talent)
- Google DORA 2024: 39% of respondents reported "little to no trust in AI-generated code"
- The Gartner practitioner on mainframe legacy documentation: direct experience leads to "I am being overly optimistic"

Source: Second Talent. "AI Coding Assistant Statistics & Trends [2025]." https://www.secondtalent.com/resources/ai-coding-assistant-statistics/

Source: DORA 2024. https://dora.dev/research/2024/dora-report/

### 6.3 Empirical Gap Statement (for Documentation Page)

The section on AI-assisted detailed design should state this gap explicitly: **there are currently no published controlled studies measuring the quality of LLM-generated detailed design documentation against V-model quality criteria**. The adjacent evidence from code generation, requirements engineering, and architecture recovery provides a basis for informed judgment but not for quantitative claims. This is an active research area where the situation will change; the documentation should be revisited against new evidence when it emerges.

---

## Summary of Key Findings for Documentation Authors

1. **LLMs can generate design-shaped text.** Whether it corresponds to correct design intent is a different question, and the current evidence base does not support optimism.

2. **The primary risk in forward design generation** is hallucinated constraints and missing edge cases — omissions that look like complete design but are not.

3. **The primary risk in reverse-engineering design from code** is the post-hoc paraphrase trap: AI produces a sophisticated description of what the code does, not a design document that reveals intent, decisions, and constraints independently of the code.

4. **Missing rationale is a structural failure**, not a fixable omission: AI cannot recover design rationale from code because rationale is not in code. Any AI-generated rationale for a legacy system is invention, not recovery.

5. **Regulatory frameworks are developing, not settled.** FAA (July 2024 roadmap) and EASA (NPA 2025-07) are actively building AI guidance, but neither has produced specific requirements for AI-generated lifecycle data in DO-178C or equivalent certification contexts.

6. **DO-178C independence requirements** create a structural challenge for AI-authored design documentation: the reviewer/author separation requirement was written for human pairs, and AI authorship creates ambiguity that current guidance does not resolve.

7. **Structured templates, bidirectional traceability, and knowledge-graph RAG** are the most evidence-supported mitigations, but they address structural completeness — not content correctness. Content correctness requires domain-expert human review.

8. **The empirical gap is large.** Practitioners and researchers acknowledge the potential; controlled quality measurements are largely absent. Claims that AI-generated design documentation is ready for safety-critical certification contexts are not currently supported by published evidence.

---

## Sources Index

- FAA. "Roadmap for Artificial Intelligence Safety Assurance Version I." July 2024. https://www.faa.gov/aircraft/air_cert/step/roadmap_for_AI_safety_assurance
- FAA. "AI/ML Certification Framework." Fall 2024 REDAC presentation. https://www.faa.gov/about/office_org/headquarters_offices/ang/redac/REDAC_508.05_Fall_2024_NAS_Ops_Briefing_AIML_Cert_Framework-v3
- EASA. "NPA 2025-07 — AI Trustworthiness." https://www.easa.europa.eu/en/document-library/notices-of-proposed-amendment/npa-2025-07
- EASA. "EASA's first regulatory proposal on Artificial Intelligence for Aviation." https://www.easa.europa.eu/en/newsroom-and-events/news/easas-first-regulatory-proposal-artificial-intelligence-aviation-now-open
- JDA Solutions. "EASA's AI initiative has potential to define global standard." https://jdasolutions.aero/blog/does-easas-ai-initiative-require-immediate-faa-response/
- arXiv:2601.03878. "Understanding Specification-Driven Code Generation with LLMs: An Empirical Study Design." SANER 2026. https://arxiv.org/html/2601.03878v1
- arXiv:2509.11446. "Large Language Models (LLMs) for Requirements Engineering (RE)." https://arxiv.org/pdf/2509.11446
- arXiv:2511.04355. "Where Do LLMs Still Struggle? An In-Depth Analysis of Code Generation Benchmarks." https://arxiv.org/html/2511.04355v1
- arXiv:2507.14330. "Leveraging LLMs for Formal Software Requirements: Challenges and Prospects." https://arxiv.org/html/2507.14330v1
- arXiv:2501.04835. "Do Code LLMs Understand Design Patterns?" https://arxiv.org/html/2501.04835v1
- arXiv:2511.05165. "Generating Software Architecture Description from Source Code using Reverse Engineering and Large Language Model." https://arxiv.org/abs/2511.05165
- arXiv:2601.13007. "ArchAgent: Scalable Legacy Software Architecture Recovery with LLMs." https://arxiv.org/html/2601.13007
- arXiv:2509.19587. "Reverse Engineering User Stories from Code using Large Language Models." https://arxiv.org/html/2509.19587v1
- arXiv:2512.05239. "A Survey of Bugs in AI-Generated Code." https://arxiv.org/html/2512.05239v1
- arXiv:2508.08804. "TechOps: Technical Documentation Templates for the AI Act." https://arxiv.org/html/2508.08804v1
- Frontiers in Computer Science. "A comparison of large language models and model-driven reverse engineering." July 2025. https://www.frontiersin.org/journals/computer-science/articles/10.3389/fcomp.2025.1516410/full
- Frontiers in Computer Science. "Research directions for using LLM in software requirement engineering." 2025. https://www.frontiersin.org/journals/computer-science/articles/10.3389/fcomp.2025.1519437/full
- ICSA 2025. "Enabling Architecture Traceability by LLM." https://fuchss.org/assets/pdf/2025/icsa-25.pdf
- Google DORA. "Accelerate State of DevOps Report 2024." https://dora.dev/research/2024/dora-report/
- GitClear. "Coding on Copilot: 2023 Data Suggests Downward Pressure on Code Quality." January 2024. https://www.gitclear.com/coding_on_copilot_data_shows_ais_downward_pressure_on_code_quality
- Thoughtworks Technology Radar. "Using GenAI to understand legacy codebases." https://www.thoughtworks.com/en-us/radar/techniques/using-genai-to-understand-legacy-codebases
- Thoughtworks Technology Radar. "GraphRAG." https://www.thoughtworks.com/radar/techniques/graphrag
- Gartner Peer Community. "Has anybody considered using LLM's to reverse engineer legacy (mainframe) code?" https://www.gartner.com/peer-community/post/anybody-considered-using-llm-s-to-reverse-engineer-legacy-mainframe-code-to-generate-documentation-github-copilot-quite-good
- AdaCore. "A Fresh Take on DO-178C Software Reviews." https://www.adacore.com/blog/a-fresh-take-on-do-178c-software-reviews
- ConsuNova. "DO-178C Certification Explained." https://consunova.com/do-178c/certification-unpacked-a-practical-guide-to-do-178c-certification-explained/
- Second Talent. "AI Coding Assistant Statistics & Trends [2025]." https://www.secondtalent.com/resources/ai-coding-assistant-statistics/
- High Vision Systems. "AI Code Generation Risks in Today's Technology." https://www.highvisionsystems.com/ai-code-generation-risks/
- arXiv:2409.08666. "Towards certifiable AI in aviation: landscape, challenges, and opportunities." https://arxiv.org/html/2409.08666v1
