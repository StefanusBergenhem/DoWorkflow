# Detailed Design Review Checklist

Review checklist for detailed design documents. The reviewer's job is to verify that the
design is complete, correct against upstream requirements, and testable.

---

## Prerequisites

Before starting review, confirm you have:
- The upstream requirements/architecture that this design satisfies
- The detailed design document under review
- The schema (detailed-design.schema.yaml) for structural validation

---

## 1. Design Quality

Verify against `design-quality-criteria.md`. Focus reviewer attention on:

- [ ] Core test passes — design contains information the code cannot provide, not code paraphrase
- [ ] Interface completeness — all 7 contract elements present per public interface
- [ ] Behavioral specification is testable — a test engineer can derive tests without seeing code
- [ ] Rationale present for non-obvious decisions, with rejected alternatives
- [ ] Error handling specified per unit, not deferred to implementer
- [ ] Layering decision explicit and justified per the Layer model

---

## 2. Upstream Traceability

- [ ] Every design element traces to a requirement or architectural component
- [ ] No orphan design elements (elements without a parent requirement)
- [ ] No missing coverage (requirements that have no corresponding design element)
- [ ] Derived requirements explicitly identified and justified
- [ ] Design does not contradict or exceed the scope of upstream requirements

---

## 3. Testability Assessment

The design's primary consumer is the test engineer. If tests cannot be derived, the design
has failed its purpose.

- [ ] Every behavior rule can produce at least one concrete test case with specific inputs and expected outputs
- [ ] Preconditions are specific enough to set up test fixtures
- [ ] Postconditions are specific enough to write assertions against
- [ ] Error conditions are enumerated — not "handles errors gracefully"
- [ ] State machines (if any) have complete transition tables — no implicit "stays in current state"
- [ ] Decision tables (if any) cover all condition combinations — no missing rows

---

## 4. Consistency

- [ ] Terminology consistent within the document and with upstream artifacts
- [ ] Interface signatures consistent across all references within the design
- [ ] No contradictions between behavioral rules
- [ ] Error handling strategy consistent across units in the same component

---

## Verdict

- **APPROVED** — all checks pass
- **REJECTED** — specific findings listed with checklist item reference
- **DESIGN_ISSUE** — fundamental structural problem that cannot be fixed by the author alone (e.g., missing upstream requirement, architectural gap)
