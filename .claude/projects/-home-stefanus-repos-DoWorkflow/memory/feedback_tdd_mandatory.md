---
name: TDD mandatory for all code
description: All code in the repo must follow TDD red-green-refactor — write tests first, confirm they fail, then implement
type: feedback
---

Every piece of code written in the repo must follow TDD: red phase (write failing tests) then green phase (implement to pass).

**Why:** User's explicit rule for the project. Aligns with the DRTDD methodology the framework promotes.

**How to apply:** When writing any code (tools, scripts, engines), always start by writing the test file first, run it to confirm red, then implement. Never write implementation before tests.
