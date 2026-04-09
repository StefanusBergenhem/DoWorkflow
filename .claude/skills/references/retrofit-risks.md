# Retrofit Risks

Risks specific to producing detailed design documentation from existing code, when no
upstream requirements or design artifacts exist. Read this before generating any retrofit
design content.

---

## The Situation

Retrofit means there is no design document, and likely no usable requirements either. The
code is the only evidence of what the system does. Your job is to produce a design document
that would allow a developer to rewrite the code and a test engineer to derive tests —
exactly the same goal as any detailed design.

There is no upstream to escalate to. The output must be the best honest description that
can be produced from the available evidence.

---

## The Test for What Belongs

A retrofit design is not a special kind of design. It is a design document produced from
code instead of from requirements. The test for whether something belongs in the design is
the same as for forward design:

**Could the current code have been written with this as the input specification?**

- If yes → it is real design content and belongs in the document
- If no → it is either speculation or description of the wrong level, and does not belong

This test cuts cleanly through the temptation to fill the document with plausible-sounding
content. A well-written contract that the existing code satisfies is design. A paragraph
speculating about why the author chose this approach is not.

---

## The Central Failure Mode: Invention

When asked to produce a design from code, the model is strongly tempted to fill gaps with
plausible-sounding content. This is the failure to avoid above all others.

- **Inventing rationale.** "This was chosen because..." — you don't know that. You weren't
  there. The original author may not even have had a reason.
- **Inventing constraints.** "This is required because of..." — only state a constraint if
  you can point to its source (a comment, a referenced standard, an obvious external API).
- **Inventing alternatives considered.** "This was chosen over X because..." — you have no
  idea what alternatives the author considered, or whether they considered any.

Plausible-sounding invented content is worse than omission. Invented rationale gets cited
as fact by future readers and creates fake constraints that prevent legitimate changes.

**If you cannot source rationale, leave it out.** A design without rationale is incomplete
but honest. A design with invented rationale is actively harmful.

---

## What the Code Evidences

These are the parts of the design the code gives you strong evidence for. They pass the
"could the code have been written with this as input" test:

- **Interface signatures** — names, parameters, return types, nullability
- **Preconditions** — value ranges, state conditions, input validation the code enforces
- **Postconditions** — return value guarantees, state changes, observable effects
- **Behavior rules** — what the function computes, expressed as a contract (not as pseudocode)
- **Error semantics** — which errors are produced for which conditions, how they propagate
- **State machines** — if the code implements one, the states, transitions, and guards
- **Invariants** — properties that hold across all method calls
- **Dependencies** — what this unit calls and what calls it

These form the contract of the unit. Writing them down is the design work, and a developer
given this contract could reproduce working code that passes the same tests.

---

## Rationale: Only When Sourced

Rationale is the hardest part of retrofit. It is not in the executable code, and you cannot
derive it from code structure alone. But it is sometimes present in the surrounding evidence:

- Comments that explain *why*, not just *what*
- Commit messages and change history
- Architecture decision records if any exist
- Linked issues or PR descriptions
- External standards or APIs the code obviously conforms to

If a rationale has a source in the evidence, capture it. If it does not, omit it. Do not
speculate. Do not write "this was probably chosen because..." — that is invention wearing
a hedge.

A retrofit design with no rationale section is a legitimate outcome when no rationale
source exists. It is incomplete in the same way a house with no basement is incomplete,
not in the way a fake ruin is.

---

## Working Method

1. **Read the code thoroughly** — including tests if they exist, since tests often encode
   behavioral intent the code alone does not
2. **Read the surrounding evidence** — comments, commit history, ADRs, linked issues
3. **Extract the contract** — interfaces, behavior, error handling, state transitions —
   using the same rigor as forward design
4. **Write the contract as design** — applying the two rules (don't duplicate code, be
   specific enough to implement and test)
5. **Capture rationale only when sourced** — quote or cite the source; omit otherwise
6. **Do not speculate** — if you cannot produce content that passes the "could the code
   have been written from this" test, leave it out

The result is a design document indistinguishable from forward design in form. The only
observable difference is that rationale may be sparser, reflecting the evidence actually
available.

---

## AI Self-Check

- [ ] Every element in the output passes the test: "could the current code have been written with this as input?"
- [ ] No rationale written without a citable source in comments, commits, ADRs, or external standards
- [ ] No constraints written without a citable source
- [ ] No speculation about intent, alternatives, or the author's reasoning
- [ ] The document looks like a regular detailed design — no two-tier labels or flagged sections
- [ ] Gaps where rationale cannot be sourced are omissions, not placeholders
