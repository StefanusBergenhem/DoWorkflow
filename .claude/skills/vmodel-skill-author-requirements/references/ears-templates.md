# EARS templates

**Contents**
- [The five patterns](#the-five-patterns) — Ubiquitous, Event-driven, State-driven, Optional-feature, Unwanted-behaviour
- [Compound templates](#compound-templates)
- [EARS cargo-culting — the trap to avoid](#ears-cargo-culting--the-trap-to-avoid)
- [When EARS is not the right fit](#when-ears-is-not-the-right-fit)

---

EARS five-pattern shape: Ubiquitous / Event-driven / Unwanted-behaviour / State-driven / Optional-feature. Compound-prohibition holds: max one trigger AND one state per statement. Default sentence shape for functional and unwanted-behaviour requirements; NFRs use the five-element rule (`nfr-five-elements.md`), interfaces the five dimensions (`interface-five-dimensions.md`).

## The five patterns

### 1. Ubiquitous

```
The <system> shall <response>.
```

Example:

```
REQ-001: The session service shall use a cryptographically secure pseudo-random
         number generator for all session token generation.
```

### 2. Event-driven

```
When <trigger>, the <system> shall <response>.
```

Example:

```
REQ-002: When a user submits a valid credential pair, the session service shall
         create a new session, persist it in the ACTIVE state, and return the
         session token and its absolute-timeout expiry timestamp.
```

### 3. State-driven

```
While <state>, the <system> shall <response>.
```

Example:

```
REQ-003: While a session is in the IDLE state, the session service shall reject
         validation requests for that session with status 401 and reason
         'idle-timeout'.
```

**State-driven complementary pair rule.** Every While-statement implicitly raises the question: what does the system do *outside* that state? Either author the complementary pair, or explicitly mark out-of-state as out of scope. → See `statement-quality.md`.

### 4. Optional-feature

```
Where <feature>, the <system> shall <response>.
```

Example:

```
REQ-004: Where the tenant has enabled multi-factor authentication, the session
         service shall require step-up authentication before promoting a session
         to the ELEVATED state.
```

### 5. Unwanted-behaviour

```
If <condition>, then the <system> shall <response>.
```

Example:

```
REQ-005: If three consecutive credential-validation attempts for the same user
         account fail within 10 minutes, then the session service shall lock
         the account for 15 minutes and shall emit a 'credential-lockout' audit
         event.
```

## Compound templates

Keywords combine in canonical order:

```
Where → While → When → If/then → shall
```

**Compound limit: at most two keywords per statement.** Three or more keywords means the statement is carrying multiple requirements that need to be split.

Example (Where + When, two keywords):

```
REQ-006: Where the tenant plan is Enterprise, when an elevated session requests
         a bulk export larger than 10 MB, the session service shall enqueue the
         export to the long-running-job queue and shall return a job token with
         status 202.
```

When you find yourself writing a three-keyword compound, stop and split. The result is two or three atomic statements, each easier to test and trace.

## EARS cargo-culting — the trap to avoid

EARS grammar without underlying discipline produces statements that *look* rigorous and *are* vacuous. Reject these and require revision:

```
BAD:  "When the system experiences load, the system shall respond appropriately."
      Structurally event-driven, semantically empty. "Load" is undefined,
      "appropriately" is unmeasurable.

GOOD: "When the session-validation endpoint receives more than 5,000 concurrent
       requests, the session service shall queue requests beyond that ceiling
       and shall return them in FIFO order within the request's timeout window."
```

The remedy is not "write better EARS" but "force the author to pass the box test" — see `statement-quality.md`. EARS is a framing for thinking, not a shield against it.

## When EARS is not the right fit

Default: use EARS for functional requirements. Escape hatch: switch to Planguage when targets are tiered (scale / meter / fail / goal / stretch / wish). GWT may appear in the optional `acceptance` block when the statement alone leaves conditions ambiguous. Do not mix two structured forms inside one statement.
