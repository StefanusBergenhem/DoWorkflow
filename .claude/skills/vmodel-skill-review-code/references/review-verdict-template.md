# Review Verdict Template

Use this exact structure for every review output.

---

## Verdict: [APPROVED | REJECTED | DESIGN_ISSUE]

### Findings

| # | Pass | Check | Finding | Location |
|---|------|-------|---------|----------|
| 1 | Code | Design compliance | `foo()` has no corresponding design element — gold plating | src/Foo.java:42 |
| 2 | Test | Anti-pattern #1 | Assertion-free test — only checks no exception thrown | test/FooTest.java:18 |
| 3 | Cross | Coverage gap | Design behavior rule "shutdown on timeout" has no test | design §3.2 |

- **Pass:** Code, Test, or Cross
- **Check:** The specific checklist item violated (reference the check name from the checklist)
- **Finding:** What is wrong, stated concretely — not "could be improved" but what specifically fails
- **Location:** File and line number for code/test findings, or design section for cross-check findings

If there are no findings, the table is empty and the verdict is APPROVED.

### Summary

- Pass 1 (Code): X findings
- Pass 2 (Tests): Y findings
- Pass 3 (Cross-checks): Z findings

### Verdict Rationale

One paragraph explaining the verdict decision. For REJECTED: which findings are most critical and
why they matter. For DESIGN_ISSUE: what is wrong with the design and why it cannot be fixed by
changing the code.

### Design Issue Detail (only if verdict is DESIGN_ISSUE)

- **What:** Describe the design problem (e.g., "Interface X specifies return type Y but the
  required behavior needs Z")
- **Why it can't be fixed in code:** The code correctly implements the design — the design itself
  is the problem
- **Suggested resolution:** What the design should change (for human to evaluate)
