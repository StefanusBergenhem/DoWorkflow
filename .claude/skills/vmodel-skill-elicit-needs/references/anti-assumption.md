# Anti-assumption

## Contents

- Ambiguity tells (six tells with starter questions)
- The four-line clarification template
- Worked example
- The 2-to-4 options rule
- The discipline

Anti-assumption is the named discipline behind Hard Refusal #2: detect every ambiguity tell in stakeholder material and surface it as a structured clarification question. Never silently fill the gap.

## Ambiguity tells

Six common ambiguity tells appear in stakeholder narrative. Each has a starter question.

### 1. Plurals without count

The stakeholder uses a plural without bounding the count.

- Tell: "users", "files", "messages", "events", "requests", "operations"
- Example: "Users should be able to upload files."
- Starter question: "How many users do you expect on day one, in the first year, and at full scale? And what does a typical file look like — average size, peak size, expected count per user?"

### 2. Undefined comparatives

A judgment word that has no measurable referent.

- Tell: "fast", "secure", "easy", "scalable", "responsive", "robust", "reliable", "intuitive"
- Example: "It should be fast."
- Starter question: "When you say `fast`, what are you comparing it to? What's the worst speed you would accept? When would a user notice a slowdown and complain?"

### 3. Unscoped pronouns

A pronoun whose referent is not pinned down.

- Tell: "it should", "they need", "this would", "those have to"
- Example: "It should send a notification when they finish."
- Starter question: "Help me pin down `it` and `they` — `it` here is the system / a service / a workflow? And `they` is the end user / the operator / something the system does?"

### 4. Unbounded operations

A verb that hides parameters.

- Tell: "save", "share", "process", "handle", "manage", "support", "deal with"
- Example: "The system should save the data."
- Starter question: "When you say `save`, walk me through what you mean — what gets saved, when, who can read it back, and what happens if the save fails halfway through?"

### 5. Unstated audiences

A reference to people without specifying which.

- Tell: "everyone", "people", "users", "the team", "anyone", "stakeholders"
- Example: "Everyone should be able to see their data."
- Starter question: "When you say `everyone`, do you mean every signed-in user sees their own data, or every signed-in user sees everyone's data, or anyone-with-the-link-no-login sees the data? These are very different."

### 6. Unstated triggers

An event that has no defined precondition.

- Tell: "when needed", "as appropriate", "automatically", "from time to time", "if necessary"
- Example: "The system should clean up old files automatically."
- Starter question: "When does `automatically` fire — on a schedule (how often), when a threshold is hit (which threshold), when a user takes an action (which one), or some combination?"

## The four-line clarification template

Every detected tell surfaces as a structured question, not a free-form one. The format has four lines (matching `templates/anti-assumption-question.md.tmpl`):

```
Clarification needed — <topic>

Expected: <what an architect would naturally assume>

Found: <what the stakeholder actually said or what's in the input>

Why this matters: <consequence in stakeholder-accessible language>

How should I proceed? Please pick one:
- <option A — concrete>
- <option B — concrete>
- <option C — concrete>
- something else — please describe
```

## Worked example

> **Clarification needed — file uploads**
>
> Expected: When you said "users should be able to upload files", I would naturally assume registered users only, with a maximum file size enforced.
>
> Found: The transcript says "users should be able to upload files" — no scope on which users, no size cap.
>
> Why this matters: If anonymous users can upload, abuse risk goes way up and we'd plan for moderation. If only registered users, the system gets simpler. Similarly, file size affects what we promise about upload speed and storage cost.
>
> How should I proceed? Please pick one:
> - Registered, signed-in users only — files up to 10 MB.
> - Registered, signed-in users only — files up to 100 MB (we want video).
> - Anyone with a link — files up to 10 MB, with manual moderation.
> - Something else — please describe.

## The 2-to-4 options rule

The "How should I proceed?" line presents 2–4 concrete options. Never an open-ended "what do you want?" — that re-introduces the ambiguity instead of resolving it. The options should span the plausible range; the stakeholder picks one or describes a different one.

## The discipline

Every detected ambiguity surfaces. Surface count is acceptable, even if it slows the session — the cost of silent fill is much higher (a need committed to `needs.md` that the stakeholder never agreed with). When in doubt: ask.
