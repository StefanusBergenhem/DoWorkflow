# LLM-Tier Compatibility and Existing Skill Libraries

Sources: Web search results (fetched 2026-03-31), GitHub repos, Claude Code docs

---

## Part A: LLM-Tier Compatibility

### A1. The Problem

VModelWorkflow skills must work on smaller/older LLMs. This means:
- No long-context assumptions
- Crisp, unambiguous instructions
- Structured output must be reliable
- Reasoning chains must be explicit, not assumed

### A2. Structured Output Reliability Techniques

From research (https://blog.promptlayer.com/how-json-schema-works-for-structured-outputs-and-tool-integration/):

**Key finding:** When given a JSON/YAML schema with definition + example + strict rules + validation instruction, LLMs achieve >99% schema adherence, even smaller models.

**Four-layer approach for skill prompts:**
1. Define the schema (field names, types, enums)
2. Show one perfect example
3. Add strict formatting rules ("Do NOT add unlisted fields")
4. Include validation instruction ("Verify output before returning")

**Temperature:** For artifact generation, specify `effort: low` or set instructions to use low temperature equivalents.

**Output priming for simpler models:**
Start the expected output in the prompt itself:
```
Generate the requirement. Begin your response with:
```yaml
schema: sw-requirement
version: "1.0"
id: SWR-
```

**Enum constraints:** Explicitly list all valid values. "Possible values for status: draft, under-review, approved, rejected. Use no other values."

**Common failure modes on small LLMs:**
- Adding markdown code fences around YAML (add rule: "Return raw YAML without code blocks")
- Hallucinating extra fields (add rule: "Do NOT add fields not in the schema")
- Ignoring enum constraints (add rule: "Only use these exact values: X, Y, Z")
- Long context drift — later instructions ignored

### A3. Prompt Engineering Principles for Tier Compatibility

**Principle 1: Front-load the task**
The most important instruction goes first. Small LLMs are more likely to follow early instructions than late ones.

**Principle 2: Explicit over implicit**
Never assume the model "knows" the standard. State it explicitly in the skill.
Bad: "Write an EARS requirement"
Good: "Write a requirement using EARS syntax. EARS uses these templates: [list them]"

**Principle 3: One thing at a time**
Each skill does ONE thing. Don't ask a small LLM to simultaneously:
- Extract requirements from code AND
- Write them in EARS format AND
- Link them to existing tests AND
- Validate them for completeness

Split into separate skills, each focused.

**Principle 4: Chain-of-thought for quality**
For reasoning tasks (review, validation), include step-by-step reasoning instructions:
"First identify X. Then check Y. Then conclude Z."
This dramatically improves accuracy on smaller models.

**Principle 5: Short skills**
Claude Code docs: "Keep SKILL.md under 500 lines."
Small LLMs drop context. If the skill is 2000 lines, the model may not follow instructions from line 1500.
Use supporting files (`reference.md`, `examples.md`) loaded on demand.

**Principle 6: Explicit output format in every skill**
Never rely on model default. Always specify:
- Format (YAML, JSON, Markdown)
- Schema version
- Required fields
- Example output

### A4. Model Tier Assignment Strategy

Based on Claude Code docs and HumanLayer patterns:

| Task type | Model tier | Rationale |
|-----------|------------|-----------|
| Codebase locating (find files) | Haiku | Fast, cheap, deterministic |
| Codebase analysis | Sonnet | Needs some reasoning |
| Requirement writing | Sonnet | Moderate complexity |
| Architecture design | Opus | High reasoning, complex tradeoffs |
| Planning/orchestration | Opus | Maximum reasoning for coordination |
| Schema validation | Tool (not LLM) | Deterministic, no LLM needed |
| Review/adversarial QA | Sonnet/Opus | Quality gate requires rigor |
| Handoff summarization | Sonnet | Summarization is well-supported |

**Implementation in skill frontmatter:**
```yaml
model: sonnet   # or haiku, opus, inherit
effort: medium  # low, medium, high, max (Opus 4.6 only)
```

### A5. Context Window Assumptions

Minimum safe assumption: 32K tokens (conservative for compatibility).

**Techniques to stay within limits:**
- Supporting files (load only what's needed)
- `context: fork` to isolate skill in fresh context
- Pipe large outputs to files: `command > /tmp/output.log`
- Subagents for high-volume operations (keep output out of main context)
- Handoff documents for cross-session continuity (summary, not full history)

---

## Part B: Existing Skill Libraries and Frameworks

### B1. Claude Code Skill Repositories (GitHub)

Significant repositories found (search 2026-03-31):

**1. hesreallyhim/awesome-claude-code**
https://github.com/hesreallyhim/awesome-claude-code
Curated list of skills, hooks, slash-commands, agent orchestrators, applications, plugins.

Relevant to VModelWorkflow:
- **AB Method** — Spec-driven workflow: transforms large problems into focused, incremental missions using specialized sub-agents. Most directly aligned with V-model approach.
- **RIPER Workflow** — Research, Innovate, Plan, Execute, Review separation phases. Similar to our DRTDD.
- **Trail of Bits Security Skills** — Professional collection for code auditing, variant analysis, vulnerability detection, differential code review. Reusable pattern for our review layer.
- **Design Review Workflow** — Automated review with specialized sub-agents. Pattern for our artifact review.
- **agnix** — Linter for Claude Code agent files. Validates CLAUDE.md, AGENTS.md, SKILL.md. Meta-tool for our skill development.
- **AgentSys** — Multi-agent code review, PR management. Pattern reference.

**2. VoltAgent/awesome-agent-skills**
https://github.com/VoltAgent/awesome-agent-skills
1000+ agent skills from official dev teams and community.
Includes official skills from: Anthropic, Google Labs, Vercel, Stripe, Cloudflare, Netlify, Trail of Bits, Sentry, Expo, Hugging Face, Figma.

**3. sickn33/antigravity-awesome-skills**
https://github.com/sickn33/antigravity-awesome-skills
1,326+ agentic skills for Claude Code, Cursor, Codex CLI, Gemini CLI.

**4. rohitg00/awesome-claude-code-toolkit**
https://github.com/rohitg00/awesome-claude-code-toolkit
135 agents, 35 curated skills (+400,000 via SkillKit), 42 commands, 150+ plugins, 19 hooks, 15 rules, 7 templates, 8 MCP configs.

### B2. Skills Directly Relevant to VModelWorkflow

From awesome-claude-code analysis:

| Skill/Resource | Relevance to VModelWorkflow |
|----------------|------------------------|
| AB Method | Spec-driven development pattern, very close to DRTDD |
| RIPER Workflow | Research-Plan-Execute-Review phases, maps to V-model |
| Trail of Bits Security Skills | Pattern for adversarial review skills |
| Design Review Workflow | Template for our artifact review pattern |
| agnix | Validate our own skills during development |
| Auto-Claude (SDLC automation) | Pipeline orchestration pattern reference |
| Claude Code PM | Project management workflow pattern |

### B3. Other Agentic Frameworks (Reference)

**LangChain / LangGraph**
- Graph-based agent orchestration
- HITL via interrupts at graph nodes
- Not directly applicable (different paradigm) but HITL pattern is transferable

**CrewAI**
- Role-based multi-agent system
- Manager/worker hierarchy
- Crew = our orchestration layer; Agents = our craft agents
- Good pattern reference for role specialization

**AutoGen**
- Microsoft's multi-agent conversation framework
- Good for: sequential conversation patterns between agents
- Less applicable: our state is file-based, not conversation-based

**Google ADK**
- Agent Development Kit
- Artifact versioning: each save creates new version, all versions retained
- Pattern: artifacts with automatic versioning

### B4. What Doesn't Exist (Gap = Our Opportunity)

No existing skill library specifically addresses:
1. V-model compliance (DO-178C, ASPICE, ISO 26262) artifact generation
2. EARS syntax guidance as a skill
3. Requirements traceability link generation
4. V-model artifact review (requirement quality assessment)
5. Legacy codebase archaeology (extracting implicit requirements from code)
6. Domain translation (generic V-model → DO-178C specific terminology)

**These are our Layer 1 craft skills to build.**

### B5. Skills Worth Installing for VModelWorkflow Development

Based on research, recommend evaluating:

| Skill | Purpose for us | Source |
|-------|----------------|--------|
| `skill-creator` | Already installed — for building our skills | Built-in |
| `wf-skill-build` / `wf-skill-review` | Already installed — pipeline pattern | Built-in |
| `tdd` | TDD methodology — relevant to DRTDD | Built-in |
| Trail of Bits skills | Pattern reference for adversarial review | VoltAgent repo |
| agnix | Validate our skill files | awesome-claude-code |
| AB Method | Spec-driven workflow pattern | awesome-claude-code |

**Investigation needed:** Check VoltAgent/awesome-agent-skills for any requirements engineering or architecture skills before building from scratch.
