# State Persistence, Evaluation, and Versioning

Sources: Web search results (fetched 2026-03-31), Claude Code docs, HumanLayer repo analysis

---

## Part A: State Persistence Across Skill Invocations

### A1. The Core Problem

Each Claude Code session starts fresh. Between sessions, nothing is automatically preserved. Skills and agents are stateless by default. For V-model artifact development, we need state to survive across:
- Multiple sessions (days between phases)
- Human review periods
- Different agents (build → review → retrospective)
- Different team members

### A2. Persistence Mechanisms Available in Claude Code

**1. File system (primary mechanism)**
The file system IS the state. Artifacts written to disk persist across sessions.
- YAML artifacts (our schemas) are the canonical state
- Status/phase fields within artifacts track workflow state
- Checkboxes in plan files track completion (HumanLayer pattern)

**2. Claude Code memory (MEMORY.md)**
- `~/.claude/projects/<project>/memory/` — cross-session project memory
- MEMORY.md (first 200 lines) injected at every session start
- Best for: context, preferences, lessons learned — NOT artifact state

**3. Subagent persistent memory**
- `memory: user/project/local` frontmatter field
- Agent-specific memory directory
- Best for: accumulated domain knowledge, codebase patterns (e.g., code reviewer accumulating findings over time)

**4. .workflow/ state files (VModelWorkflow pattern)**
- `pipeline_state.yaml` — current phase, metadata
- `current_task.yaml` — active task contract
- `review_ready.yaml` — build completion claim
- `feedback.yaml` — review rejection details

**5. Handoff documents (HumanLayer pattern)**
```
thoughts/shared/handoffs/ENG-XXXX/YYYY-MM-DD_HH-MM-SS_description.md
```
Compact structured summaries for cross-session continuity.

### A3. The Artifact-as-State Pattern (Recommended for VModelWorkflow)

**Core insight from research:**
> "An artifact is any tangible output created by an AI agent that persists beyond the conversation or session. Each save creates a new version and all versions are retained."
Source: https://fast.io/resources/ai-agent-artifacts/

**For VModelWorkflow:**
The V-model artifact file IS the state. Add status fields to artifact schemas:

```yaml
# In artifact schema
status:
  phase: draft | under-review | approved | rejected
  approved_by: null | "human-name"
  approved_at: null | "ISO-8601"
  version: "1.0"
  revision_notes: ""
```

This means:
- An artifact's state is always readable without a separate pipeline file
- Any tool or human can check artifact status
- State transitions are traceable (part of the artifact history)
- Git diff shows what changed AND when it was approved

### A4. Versioning Handoff Documents

**Critical finding from research:**
> "Your agent from two weeks ago wrote v0.8 handoffs while your agent today expects v1.0. Without a version field, your loader silently misinterprets fields."
Source: https://dev.to/aureus_c_b3ba7f87cc34d74d49/building-reliable-state-handoffs-between-ai-agent-sessions-1bk3

**Always include `schema_version` in state files:**
```yaml
schema_version: "1.0"
```

### A5. Draft → Review → Approved State Machine

```
draft
  └─ [agent submits] ──→ under-review
                              ├─ [human approves] ──→ approved (terminal)
                              └─ [human rejects]  ──→ rejected
                                                        └─ [agent revises] ──→ draft
```

Implementation:
- Agent produces artifact in `draft` state
- Agent writes `review_ready.yaml` (claim)
- Review skill reads artifact, produces verdict
- If APPROVED: updates artifact status to `approved`, captures approver/timestamp
- If REJECTED: writes `feedback.yaml`, artifact returns to `draft`
- All state changes tracked in git

---

## Part B: Evaluation and Quality Gates

### B1. Types of Evaluation for V-Model Artifacts

**Structural validation (deterministic — tool concern)**
- Schema compliance: does the artifact match the YAML schema?
- Required fields present?
- Enum values valid?
→ This is Pillar 2's job (traceability and validation engine)
→ Never an agent concern

**Content quality (LLM concern)**
- Is the requirement atomic (one thing)?
- Is EARS syntax used correctly?
- Is the language unambiguous?
- Are acceptance criteria testable?
→ This is a review/reviewer craft skill

**Traceability completeness (tool + LLM)**
- All requirements linked to tests?
- All tests linked to requirements?
- Orphaned artifacts detected?
→ Pillar 2 tools detect gaps; review skills interpret and report

**Domain compliance (LLM concern)**
- Does this meet DO-178C level A rigor?
- Is the hazard analysis complete?
- Are safety arguments sound?
→ Domain-specific review skills

### B2. The Reviewer Pattern

From HumanLayer and VModelWorkflow research, the quality gate is a dedicated `review` skill/agent:

**VModelWorkflow already has this**: `wf-skill-review`
- Adversarial QA gatekeeper
- Validates developer work against task contract
- Produces: APPROVED, REJECTED, or DESIGN_ISSUE verdict

**For V-model artifact review, the same pattern applies:**
```
review-requirement: reads artifact, checks EARS syntax, atomicity, testability, traceability links → verdict
review-architecture: reads artifact, checks completeness, consistency, testability → verdict
```

**Key design**: Reviewer has NO knowledge of how the artifact was created. It only sees the artifact and the standard it must meet.

### B3. Automated Quality Gates Before Human Review

**Pre-flight checks that should run automatically:**
1. Schema validation (Pillar 2 tool) — artifact must be schema-compliant before review
2. Required links present — no orphaned artifact
3. Linter/style checks — EARS syntax, no ambiguous terms ("fast", "easy", "usually")

**Pattern**: PostToolUse hook runs schema validator after agent writes artifact file. If validation fails → agent is notified before claiming review-ready.

### B4. Evaluation Metrics for V-Model Artifacts

| Artifact | Automated | Human |
|----------|-----------|-------|
| System requirement | EARS syntax check, schema valid, linked to test case | Correctness, completeness, unambiguity |
| SW requirement | EARS syntax, derived from system req, schema valid | Technically feasible, testable |
| Architecture | Schema valid, all requirements allocated | Design quality, independence, safety argument |
| Detailed design | Schema valid, implements architecture | Correctness, completeness |
| Test case | Schema valid, linked to requirement | Adequate coverage, correct expected results |

---

## Part C: Skill Versioning and Portability

### C1. The Version Problem

Skills (prompt files) change over time. A skill that writes requirements in one format may change to produce a different format. Projects that depend on the old format break silently.

### C2. Versioning Strategies

**Option A: Version in skill name**
```
skills/craft/write-requirement-v1/
skills/craft/write-requirement-v2/
```
- Pro: explicit, no ambiguity
- Con: consumers must update references; namespace pollution

**Option B: Version in frontmatter**
```yaml
name: write-requirement
version: "2.0"
```
- Pro: clean namespace
- Con: no automatic compatibility checking

**Option C: Schema-version pinning (recommended for VModelWorkflow)**
```yaml
name: write-requirement
produces: sw-requirement@1.2   # artifact schema version it targets
```
- Skills are versioned by what schema version they produce
- If schema v1.2 changes to v1.3, skill must be updated too
- Consumers that need v1.2 artifacts pin to the v1.2-producing skill
- **Aligned with contract-driven design**: skill version = artifact schema version

### C3. Portability

**What makes a skill portable:**
1. No hardcoded project paths
2. Arguments via `$ARGUMENTS` placeholders
3. `${CLAUDE_SKILL_DIR}` for bundled resources
4. No assumptions about codebase structure
5. Output format defined by schema (Pillar 1), not embedded in skill

**Distribution mechanisms:**
- Commit `.claude/skills/` to version control → team-wide sharing
- Plugins for organization-wide distribution
- Enterprise managed settings for org-level enforcement

**Cross-project portability:**
Craft skills (Layer 1) should be globally installable (`~/.claude/skills/`).
Pillar integration skills (Layer 2) are project-level (`.claude/skills/`).
Pilot-specific skills (Layer 3) are project-level only.
