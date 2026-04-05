# Human-in-the-Loop and Skill Composition Patterns

Sources:
- Web search results (fetched 2026-03-31)
- HumanLayer repo analysis (02_humanlayer_repo.md)
- Claude Code official docs

---

## Part A: Human-in-the-Loop Integration Patterns

### A1. Core HITL Mechanisms

**Framework-level interrupts (LangGraph pattern):**
Pausing graph execution at specific points, displaying information to humans, awaiting input before resuming.
Source: https://towardsdatascience.com/building-human-in-the-loop-agentic-workflows/

**HumanLayer SDK approach:**
Intercepts agent tool calls before execution, routes to approval channel (Slack, email, web UI). If approved → tool executes. If rejected → agent receives rejection with reason.

**Claude Code pattern (observed in HumanLayer workflow):**
Agents write to output files, pause, and include explicit human-gate language in their prompts:
```
Phase [N] Complete - Ready for Manual Verification
...
Let me know when manual testing is complete so I can proceed to Phase [N+1].
```

### A2. Synchronous vs Asynchronous Approval

**Synchronous (blocking):**
- Agent stops, waits for human response in same session
- Simpler state management — conversation context is the state
- Works naturally in Claude Code's conversational model
- Downside: human must be available; session may time out

**Asynchronous (notify + resume):**
- Agent writes state to file, closes session
- Notification sent (Slack, email) with context
- Human responds on their timeline
- New agent session picks up from handoff document
- HumanLayer's `create_handoff` / `resume_handoff` implements this
- Requires explicit state serialization

**Recommendation for VModelWorkflow:**
Both patterns are needed:
- Synchronous: within a single DRTDD phase (quick human approval of generated artifact)
- Asynchronous: between phases (human reviews, approves, then triggers next phase when ready)

### A3. Rejection and Revision Cycles

From HumanLayer `implement_plan`:
- If plan doesn't match reality → STOP and present mismatch clearly:
  ```
  Issue in Phase [N]:
  Expected: [what plan says]
  Found: [actual situation]
  Why this matters: [explanation]
  How should I proceed?
  ```
- Do NOT check off manual verification items until human confirms
- Multiple revision rounds are expected and structured

From the existing VModelWorkflow `wf-skill-receiving-feedback`:
- Activates when `feedback.yaml` is present or review returns REJECT
- Enforces surgical fixes (not wholesale rewrites)
- Escalates after 3 failed attempts

**Key pattern**: Rejection produces structured feedback (not just "rejected"). Agent must diagnose and fix the specific issue, not retry blindly.

### A4. State Preservation During Human Gates

**What state must survive:**
- The artifact being reviewed (the draft)
- Which phase/step we're at
- What has been approved so far (checkmarks)
- What questions were asked / what answers were given
- Why specific decisions were made

**Mechanisms:**
1. **File-based state** (HumanLayer pattern): Plan file with checkboxes, updated as work proceeds
2. **Handoff documents**: Compact structured summaries for cross-session continuity
3. **Claude Code memory**: Persistent MEMORY.md for cross-session context
4. **Pipeline state files** (VModelWorkflow pattern): `pipeline_state.yaml`, `current_task.yaml`, `review_ready.yaml`, `feedback.yaml`

**Recommendation**: For VModelWorkflow artifacts, the artifact file itself IS the state. Use status/phase fields within the artifact schema. Supplemented by a lightweight pipeline state file.

### A5. Enterprise HITL Design Principles (2026)

From search results (https://www.permit.io/blog/human-in-the-loop-for-ai-agents-best-practices-frameworks-use-cases-and-demo):
- Moving beyond simple approval gates
- Agents handle routine cases autonomously, flag edge cases for human review
- Humans provide sparse supervision
- Human gates at "key decision points" not every micro-step
- Auditable approval process (especially for quality-critical domains)

**For V-model / DO-178C context:**
Regulatory standards require evidence of human review. The approval gates are not optional overhead — they are compliance artifacts. The gate output (human signature/timestamp/decision) must be captured as part of the artifact.

---

## Part B: Skill Composition Patterns

### B1. Core Agentic Design Patterns

From Microsoft Azure Architecture Center and AWS Prescriptive Guidance (fetched 2026-03-31):

**Manager-Worker Pattern:**
Central "manager" agent acts as team lead, delegates to specialized "worker" agents. Workers can employ their own patterns.
- HumanLayer: `create_plan` (manager) delegates to `codebase-locator`, `codebase-analyzer` (workers)
- VModelWorkflow: orchestrator delegates to build, review, retrospective agents

**ReAct (Reasoning and Acting):**
Observe → Think → Act → Observe loop. Transparency: we can see step-by-step decision making.
Works well for single agents. Less applicable to multi-agent orchestration.

**Pipeline:**
Linear sequence: A → B → C. Each step takes the output of the previous.
VModelWorkflow already implements this: plan → build → review → retrospective.
Good for: sequential V-model phases (REQUIRE → DESIGN → TEST → IMPLEMENT → REFACTOR → VERIFY)

**DAG (Directed Acyclic Graph):**
Parallel branches that merge.
Good for: parallel research (HumanLayer spawns codebase-locator AND codebase-analyzer AND thoughts-locator simultaneously).

**Event-Driven:**
Actions triggered by state changes in files.
Good for: hooks-based automation (PostToolUse triggers next step).

### B2. Separation of Concerns: Craft vs Orchestration

**Craft skills** (from VModelWorkflow CLAUDE.md):
> "Teach HOW to do one thing well"
> "Standalone, composable"
> "Completely independent of Pillar 1 and 2"

Examples from HumanLayer:
- `codebase-analyzer`: ONLY explains how code works. Does NOT suggest improvements. Does NOT create plans.
- `codebase-locator`: ONLY finds files. Does NOT read them. Does NOT analyze them.

**Principle**: Single Responsibility. A skill that does two things is a skill that does two things poorly.

**Orchestration skills** (from VModelWorkflow CLAUDE.md):
> "Handle WHEN and WHAT to hand off"
> "Route using contracts, not internal knowledge"

Examples from HumanLayer:
- `create_plan`: coordinates research, presents options, builds plan structure, writes output
- `implement_plan`: reads plan, executes phases, pauses for human gates

**The key insight**: Orchestration skills should know NOTHING about the internal implementation of craft skills. They interact only through the skill's output contract (files, YAML structures).

### B3. Contract-Driven Artifact Passing

From VModelWorkflow CLAUDE.md:
> "Skills communicate through typed YAML schemas. Orchestration routes using contracts, not internal knowledge."

**Pattern:**
1. Skill A produces `artifact.yaml` with defined schema
2. Orchestrator reads ONLY the contract fields it needs
3. Skill B reads `artifact.yaml` — doesn't know how Skill A produced it
4. Validation tool independently verifies schema compliance

**Why this matters:**
- Can swap Skill A for a different implementation → Skill B is unaffected
- Can run Skill A manually (human generates artifact) → pipeline still works
- Traceability: the artifact IS the handoff, not a side effect

### B4. Key Composition Anti-Patterns

**1. "Do Everything" skills**
> "A 'do everything' skill is just a worse version of no skill at all." — Claude Code docs
Skills should be focused. One skill, one job.

**2. Tight coupling through internal knowledge**
Orchestrator knowing the internal steps of a craft skill → fragile, hard to evolve.

**3. Not waiting for all parallel tasks**
HumanLayer explicitly states: "Wait for ALL sub-tasks to complete before proceeding."
Synthesizing partial results creates inconsistent plans.

**4. Blind retry**
VModelWorkflow CLAUDE.md: "Each retry must use a different approach. On 2nd consecutive failure, apply root-cause tracing."

**5. Stale evidence**
"Never claim 'all tests pass' without running them in the current session."

**6. Nested subagents**
Claude Code docs: "Subagents cannot spawn other subagents." This is a hard limit.
Design implication: orchestration must happen at the main-conversation or skill level, not inside a subagent.

### B5. The Separation of Concerns for VModelWorkflow Pillar 3

Based on research, three distinct layers emerge:

**Layer 1: Craft Skills (domain knowledge)**
- Independent of any framework
- Teach HOW to write a good requirement, design document, test case, etc.
- Could be used by a human team without any tooling
- Examples: `write-requirement`, `write-architecture`, `write-test-case`, `review-artifact`
- No knowledge of YAML schemas, trace links, or pipeline state

**Layer 2: Pillar 1+2 Integration Skills**
- Know about our V-model schemas and trace model
- Adapt craft output into schema-compliant artifacts
- Validate trace links, detect gaps
- Examples: `emit-artifact`, `link-artifacts`, `validate-trace`
- Uses craft skills as sub-components

**Layer 3: Pilot Onboarding Skills**
- Project-specific: the Java/Gradle pilot
- Handle the legacy codebase context
- Incrementally extract and formalize existing artifacts
- Examples: `analyze-existing-module`, `bootstrap-requirements-from-code`, `gap-analysis`
- Uses Layer 1+2 skills as components

This three-layer architecture supports the principle that each layer can evolve independently.
