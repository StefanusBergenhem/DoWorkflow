# Skill Creation Learnings

Lessons learned from the derive-test-cases skill creation session (2026-04-01 to 2026-04-03).

## Key Finding: Less Is More for Agentic Skills

A verbose 549-line skill performed *worse* than no skill on Haiku (-4.2% pass rate delta). After cutting to 155 lines (72% reduction), the same skill became the strongest performer (+29.2% delta on Haiku, +8.3% on Opus).

### Why verbose skills hurt smaller models

- **Cognitive overload**: Multi-step processes (5 steps with sub-steps) cause smaller models to lose track and make mistakes they wouldn't make without the skill.
- **Override of natural idioms**: Detailed generic instructions (e.g., "use Arrange/Act/Assert") overrode Haiku's natural language-specific patterns (e.g., Go table-driven tests).
- **Redundancy confusion**: When the skill body repeated content from reference files, the model sometimes followed the summary instead of the reference, producing weaker results.

### What to keep in a skill

Only include instructions that the model wouldn't do on its own:
- **Coverage matrix** — the unique V-model value. Models don't produce traceability matrices without being told to.
- **Pointers to references** — "check against anti-patterns.md" is enough. Don't summarize the reference in the skill body.
- **HALT conditions** — safety nets for ambiguous situations.
- **Domain-specific output requirements** — what the V-model standard expects.

### What to remove from a skill

- Anything a competent model already knows (AAA structure, descriptive naming, one-concept-per-test)
- Detailed step-by-step processes that the model can figure out
- Duplicate information across skill body and reference files
- Language-specific guidance (keep skills language-agnostic)

## Quantitative Results

| Version | Lines | Model | With Skill | Baseline | Delta |
|---------|-------|-------|-----------|----------|-------|
| Verbose | 549 | Opus | 83.3% | 75.0% | +8.3% |
| Verbose | 549 | Haiku | 70.8% | 75.0% | -4.2% |
| Slim | 155 | Haiku | 87.5% | 58.3% | +29.2% |

## Evaluation Process Notes

- **skill-creator plugin** was used for the structured eval loop (draft → test → grade → iterate)
- **3 test cases** across 3 languages (Python/pytest, Java/JUnit5, Go/testing) validated language-agnostic behavior
- **With-skill vs baseline** comparison in every iteration revealed what the skill actually adds vs what the model already knows
- **Haiku as executor** is the right choice — if the skill works on Haiku, it works everywhere. Opus masks skill quality issues.
- **Subagent permission gotcha**: agents don't inherit parent session's tool approvals. Establish Write permissions before launching eval agents.

## Reference File Design

- Keep reference files focused on one concern
- Don't duplicate across files — the mock guidance lived in both derivation-strategies.md and anti-patterns.md, causing confusion
- Merge overlapping patterns (e.g., "doesn't throw" + "assertion-free" are the same anti-pattern)
- Human-facing docs can be verbose; agent-facing references should be concise

## Parallel Documentation Track

Maintain two versions of every best-practice document:
1. **Agent version** in `.claude/skills/` — optimized for LLM consumption (concise, no redundancy, action-oriented)
2. **Human version** in `docs/guide/best-practices/` — optimized for learning (verbose, examples, rationale, educational)

The human version is the "source of knowledge." The agent version is a distilled extraction. When updating, update the human version first, then distill into the agent version.
