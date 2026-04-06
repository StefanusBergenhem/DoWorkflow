# Error Handling Specification in Detailed Design

**Research for:** Section 3.x — Error Handling and Fault Containment, Detailed Design documentation page
**Date:** 2026-04-05
**Status:** Draft — for review before HTML authoring

---

## 1. Error Handling Is a Design Decision, Not an Implementation Detail

The most damaging misconception in software development is that error handling is something
implementers decide while writing code. This framing is wrong, and in safety-critical development it
is a defect in the design process itself.

The position is stated plainly in research on structured exception handling architecture: "To achieve
desired levels of reliability, mechanisms for detecting and handling errors should be developed
systematically from early phases of development, from requirements through analysis and design
(architectural design and detailed design) phases. This kind of design decision is not left open to
the component developer, which avoids situations where developers adopt bad practices such as
swallowing exceptions, including empty catch blocks, and propagating meaningless exceptions."
[Source: SciELO Brazil — "A systematic approach for structuring exception handling in robust
component-based software," *Journal of the Brazilian Computer Society*,
https://www.scielo.br/j/jbcos/a/sL6DtGPgr4Sc6XJrh8kxsnd/]

The InfoQ analysis of SOA error handling makes the same architectural point: "Thorough analysis of
error handling requirements during the analysis and design phase is the key to getting services
designed and implemented right." Crucially, "error propagation to service consumers can be
accomplished in many different ways, making it important to have an architecture design decision to
choose the most appropriate style." [Source: InfoQ, "Error Handling Considerations in SOA Analysis
& Design," https://www.infoq.com/articles/error-handling-soa-design/]

What belongs in a design document about errors — not left to implementation:

- **Error catalog**: every error condition a unit can encounter or produce, named and described.
- **Error classification**: which errors are recoverable, which are programming defects, which are
  domain violations.
- **Detection method**: how each error is detected (precondition check, return value inspection,
  try-catch boundary, hardware signal).
- **Propagation decision**: does this error stay inside the unit, translate at the layer boundary,
  or propagate to the caller?
- **Recovery strategy**: for each error class, what action does the unit (or its caller) take?
- **Containment boundary**: where does the component guarantee to stop an error from spreading?

When these decisions are absent from the design, each implementer makes them independently and
inconsistently. The result is systems where errors are sometimes swallowed, sometimes thrown raw,
sometimes logged and re-thrown, and sometimes silently corrupting state — with no way to
systematically test the behavior because no behavior was specified.

### Why Ad-Hoc Error Handling Is a Design Defect

Defensive programming that is not grounded in specified error conditions is a design defect for a
specific reason: it is untestable. DO-178C (Section 6.4) requires requirements-based testing:
every test must trace to a requirement. If error behavior is not specified as a requirement, then:
(a) there is nothing to trace to, (b) robustness tests cannot be systematically derived, and (c)
reviewers cannot determine whether the implementation is correct. [Source: Complete Verification and
Validation for DO-178C Whitepaper, Vector Informatik,
https://cdn.vector.com/cms/content/know-how/aerospace/Documents/Complete_Verification_and_Validation_for_DO-178C.pdf]

ASPICE SWE.3 (Software Detailed Design and Unit Construction) requires that dynamic behavior of
software units be defined. Error behavior is part of dynamic behavior — what happens to state and
interfaces under abnormal conditions is not separable from what happens under normal conditions.
[Source: SWE.3 Software Detailed Design and Unit Construction overview,
https://polarion.code.blog/2022/04/21/swe-3-software-detailed-design-and-unit-construction/]

---

## 2. Error Propagation Strategy

### 2.1 Checked vs. Unchecked Exceptions as a Design Choice (Java)

Joshua Bloch's *Effective Java*, 3rd Edition (Addison-Wesley, 2018) is the authoritative source on
exception design for Java. The core principle, stated in Item 70: "Use checked exceptions for
recoverable conditions and runtime exceptions for programming errors."

The design-level consequence of this rule is significant: the decision about whether an exception is
checked or unchecked is not a coding decision — it is a specification of the caller's obligation.
A checked exception tells callers: "this situation is anticipated, you are expected to handle it."
An unchecked exception tells callers: "this situation represents a programming error; handling it at
runtime is inappropriate." Making this distinction at design time, before implementation, ensures
the interface communicates the right contract.

Item 71 adds a constraint to checked exceptions: "avoid unnecessary use of checked exceptions." If
callers cannot actually recover from a condition, a checked exception forces them to write catch
blocks that serve no purpose — this is API friction without benefit. The design must ask: can the
caller do something useful upon receiving this exception? If not, it should be unchecked.

Item 72 recommends reusing standard exceptions where semantically appropriate
(`IllegalArgumentException`, `IllegalStateException`, `NullPointerException`,
`IndexOutOfBoundsException`, `UnsupportedOperationException`) because this lowers cognitive overhead
for callers. Custom exceptions are warranted when a standard exception does not convey the domain
meaning precisely enough.

Item 69 provides the boundary condition: "Use exceptions only for exceptional conditions." Exceptions
must not be used for normal flow control. This is both a performance concern (stack trace capture is
expensive) and a readability concern — code that uses exceptions for flow control is impossible to
reason about at the design level.
[Source: Effective Java, 3rd Edition, Joshua Bloch, Addison-Wesley 2018; summary at
https://ahdak.github.io/blog/effective-java-part-9/]

### 2.2 Error Codes vs. Exceptions vs. Result/Either Types

The choice of error representation mechanism is a design-level decision with cross-cutting
consequences for readability, composability, and interface clarity.

**Error codes** (returning an integer or enum status alongside output) are the traditional C pattern
and remain common in embedded/safety-critical systems. Advantages: explicit, no hidden control flow,
compatible with C-based standards (MISRA-C). Disadvantages: callers can ignore them silently
(returning the code does not force handling), and the result and error code must be returned together
(output parameter or struct). The design must specify which return value is authoritative when an
error occurs.

**Exceptions** (Java's primary mechanism) make error propagation automatic and explicit in the type
signature when checked. Advantages: cannot be ignored without effort (checked), carry structured
context (message, cause chain, stack trace). Disadvantages: unchecked exceptions can be silently
ignored; exception handling adds complexity at each layer boundary.

**Result/Either types** (functional approach) treat errors as data: a function returns either a
success value or an error value, explicitly typed. In Java, this is achievable via libraries (Vavr's
`Either<L,R>`) or bespoke `Result<T,E>` wrappers. The JVM Weekly analysis of Project Amber notes
that Java is moving toward making this pattern more ergonomic. Advantages: forces callers to handle
both paths; error handling is composable via map/flatMap; no hidden control flow. Disadvantages:
unfamiliar to Java developers trained on exceptions; introduces functional idioms that increase
onboarding complexity; mismatches with existing Java frameworks that throw exceptions.
[Source: JVM Weekly, "On Modern Error Handling (Not Just in Java): Monads, Effects and Project
Amber," https://www.jvm-weekly.com/p/on-modern-error-handling-not-just]

The key design insight: "Errors are not the same as exceptions: a validation error should not be
thrown since it is part of our domain, and not exceptional behaviour." Functional languages use
exceptions for genuinely unexpected situations and typed results for anticipated failures. This
distinction is a design specification concern — which conditions are domain-anticipated errors vs.
programming exceptions must be stated in the design, not inferred from implementation.

### 2.3 Exception Hierarchy Design

A custom exception hierarchy is warranted when the default Java exception classes do not express
domain semantics. Design principles for exception hierarchy:

**Keep hierarchies shallow.** A hierarchy more than two levels deep (abstract base → specific
concrete) is almost always a smell. Deep hierarchies create catch precision problems: callers must
decide how specific to catch, and intermediate levels are often neither useful as a catch target
nor semantically meaningful.

**Domain-specific vs. technical exceptions are different categories.** Domain exceptions represent
anticipated failure conditions within the business logic (e.g., `InsufficientFuelException`,
`InvalidFlightPlanException`). Technical exceptions represent infrastructure failures
(`DatabaseConnectionException`, `SerializationException`). The design must not mix these categories
in the same hierarchy — callers have different handling strategies for each.

**Exception translation at layer boundaries.** The infrastructure layer throws technology-specific
exceptions. These must not propagate into the domain layer unchanged, because that couples the domain
to infrastructure. The design must specify where translation occurs: the persistence layer catches
`SQLException` and re-throws a domain-meaningful `DataAccessException`. This is an exception
translation boundary — it must be an explicit design decision, not a coincidental catch.
[Source: prgrmmng.com, "Best Practices for Designing Custom Exceptions in Java,"
https://prgrmmng.com/best-practices-custom-exceptions-java]

**Exception context preservation.** When wrapping, always preserve the original cause. Design must
require: `throw new DomainException("context message", originalCause)`. Losing the causal chain
makes production diagnosis impossible.

### 2.4 Error Propagation Across Component Boundaries

The "exception boundary" concept is the design-level decision about where in the component hierarchy
an exception is caught, handled, and either resolved or translated. Every component boundary where
exceptions cross is a design decision point. The design must state for each boundary:

1. Which exception types are permitted to cross this boundary.
2. Which are caught and translated.
3. Which are caught and handled locally (recovery occurs here).
4. What the caller receives in each case.

Uncontrolled propagation — where infrastructure exceptions propagate through domain and presentation
layers unchanged — is an anti-pattern because it leaks implementation details, violates layer
separation, and makes behavior impossible to specify at the interface level.
[Source: Error handling in layered architectures analysis, Medium/Flutter España,
https://medium.com/flutter-españa/error-handling-in-layered-architectures-with-dart-patterns-650b199000d7]

---

## 3. Fault Containment and Isolation

### 3.1 The Bulkhead Pattern

Michael Nygard's *Release It! Design and Deploy Production-Ready Software*, 2nd Edition (Pragmatic
Programmers, 2018) is the primary source on production stability patterns. The Bulkhead pattern
is named after the watertight compartments in a ship's hull: if one section floods, the others
remain sealed. In software, the pattern requires isolating failure by assigning separate resource
pools (thread pools, connection pools, process boundaries) to different workloads.

The design implication is that bulkhead boundaries must be decided at design time, not retrofitted
when a failure occurs in production. The design document must answer: if component A fails and
exhausts its thread pool, can it starve component B? If yes, the design must specify that A and B
require separate pools.

The Microsoft Azure Architecture Center states this precisely: "Isolate critical resources, such as
connection pools, memory, and CPU for each workload or service. By using bulkheads, a single
workload (or service) can't consume all of the resources, starving others."
[Source: Bulkhead Pattern — Azure Architecture Center,
https://learn.microsoft.com/en-us/azure/architecture/patterns/bulkhead]

### 3.2 The Circuit Breaker Pattern

Martin Fowler documented the circuit breaker pattern at the architectural level; Nygard popularized
it in *Release It!*. The pattern models an electrical circuit breaker: under normal conditions the
circuit is closed (requests flow); above a failure threshold it opens (requests are immediately
rejected without attempting the failing operation); after a timeout period it enters half-open
(limited requests probe whether the dependency has recovered).

The design-level decisions that must be specified:

- **Failure threshold**: what count or rate of failures trips the breaker open?
- **Timeout**: how long does the breaker remain open before attempting half-open probe?
- **Half-open probe policy**: how many requests, and what success criteria, to restore closed?
- **Fallback behavior**: what does the caller receive when the breaker is open? (error, cached
  response, degraded result — this is a separate design decision)

Fowler's bliki entry on Circuit Breaker:
https://martinfowler.com/bliki/CircuitBreaker.html
[Source: Resilience4j CircuitBreaker docs, https://resilience4j.readme.io/docs/circuitbreaker;
Wikipedia Circuit Breaker Design Pattern,
https://en.wikipedia.org/wiki/Circuit_breaker_design_pattern]

### 3.3 Error Containment Boundaries Map to Component Boundaries

Robert Hanmer's *Patterns for Fault Tolerant Software* (Wiley, 2007) organizes fault tolerance into
four execution-time phases: error detection, error recovery, error mitigation, and fault treatment.
The book identifies 63 patterns organized around these phases. Key patterns relevant to containment:

**Error Containment Barrier**: an explicit architectural boundary that prevents errors from spreading
beyond a defined perimeter. The component that detects an error inside the barrier is responsible for
resolving it before anything crosses the barrier's interface.

**Quarantine**: a pattern that isolates a failing component so its errors cannot affect the rest of
the system. The component continues to run (or is isolated and monitored) while the system proceeds
without it.

The design implication is that containment barriers are V-model component boundaries made explicit in
error handling terms. Each component in a detailed design should specify: "Errors that originate
within this component will not propagate beyond its interface in raw form. The interface contract
specifies what callers receive."
[Source: Patterns for Fault Tolerant Software, Robert Hanmer, Wiley 2007, ISBN 0470319798;
O'Reilly preview: https://www.oreilly.com/library/view/patterns-for-fault/9780470319796/ch03.html;
Alex Bespoyasov review: https://bespoyasov.me/blog/patterns-for-fault-tolerant-software/]

---

## 4. Recovery, Fail-Fast, and Degraded Operation

### 4.1 Fail-Fast

Jim Shore published "Fail Fast" in *IEEE Software*, vol. 21, no. 5, September/October 2004, pp.
21–25. DOI: 10.1109/MS.2004.1331296. The article is also available at:
https://www.jamesshore.com/v2/blog/2004/fail-fast

Shore's argument: "Bugs are much easier to find in programs that fail hard and visibly upon
encountering something unexpected than in programs that silently ignore the unexpected." The worst
bugs are those that manifest hours after the triggering condition, under unusual circumstances, with
misleading stack traces. The fail-fast technique — stopping immediately when an unexpected state is
detected, rather than continuing in a potentially corrupted state — surfaces these bugs at the
earliest possible moment.

The design implication: components should assert invariants at boundaries, fail on unexpected state
rather than attempting to continue, and report failures visibly rather than logging them quietly and
resuming. This is a design-level specification — the component's error strategy is "fail fast on
violation of assumed state." In safety-critical systems, this maps to: a component that detects a
condition outside its specified operating envelope must not silently proceed.

Martin Fowler's summary: "'Failing immediately and visibly' sounds like it would make your software
more fragile, but it actually makes it more robust. Bugs are easier to find and fix, so fewer go
into production." [Source: martinfowler.com, failFast.pdf, cited at
https://medium.com/@christian.ppl/introduction-to-the-fail-fast-principle-in-software-development-865ccab28979]

**Fail-fast is a design strategy, not a crash tolerance.** In production safety-critical systems,
"crash" may not be an option. The correct interpretation is: detect violations at the earliest
possible boundary, surface them explicitly rather than masking them, and escalate to the appropriate
recovery handler. Shore himself notes the importance of a global error handler so that overall
system stability is preserved while individual component failures are visible.

### 4.2 Recovery Strategies

Recovery strategy is a per-error-class design decision. The design document must specify, for each
error class, which strategy applies:

**Retry**: appropriate for transient failures (network blip, temporary resource unavailability).
Design must specify: maximum retry count, retry interval (fixed, exponential backoff, jitter),
whether the operation is idempotent (retry is safe only for idempotent operations). The Azure
Architecture Center Retry Pattern:
https://learn.microsoft.com/en-us/azure/architecture/patterns/retry

**Fallback**: if the primary path fails, use an alternative. Design must specify: what is the
fallback (cached data, simplified response, static default), how stale the fallback data can be,
and what the caller is told about the degraded response. Codecentric's resilience design patterns
analysis is explicit: "Fallback logic must be simpler and more reliable than what it's replacing.
A fallback that queries three other services and performs complex calculations defeats the purpose."
[Source: Codecentric, "Resilience Design Patterns: Retry, Fallback, Timeout, Circuit Breaker,"
https://www.codecentric.de/en/knowledge-hub/blog/resilience-design-patterns-retry-fallback-timeout-circuit-breaker]

**Compensating transaction**: for operations that have partially committed state, the recovery
strategy is to undo completed steps. This is required for multi-step operations where partial
completion is worse than no completion. The design must specify: which steps can be compensated,
whether compensation itself can fail, and what happens when compensation fails. The Azure
Compensating Transaction Pattern:
https://learn.microsoft.com/en-us/azure/architecture/patterns/compensating-transaction

**Fail-safe / safe state**: in safety-critical systems, the recovery strategy may be to transition
to a safe state — a known configuration where hazards are suppressed. This is distinct from
"degraded operation" in that safe state may provide no useful functionality. The design must define
what the safe state is for each component, and under what error conditions it is entered.

### 4.3 Degraded Operation

Graceful degradation is the design decision to maintain partial functionality when a dependency or
subsystem fails, rather than failing completely.

AWS Well-Architected Framework defines this precisely: "Implement graceful degradation to transform
applicable hard dependencies into soft dependencies." A hard dependency is one whose failure
propagates as a complete failure of the dependent component. A soft dependency is one where failure
causes reduced functionality but not complete unavailability.
[Source: AWS Well-Architected Reliability Pillar — REL05-BP01,
https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/rel_mitigate_interaction_failure_graceful_degradation.html]

The design must specify the degradation hierarchy explicitly:

```
Full functionality
  → Degraded: feature X unavailable, core function continues
  → Minimal: only safety-critical operations available
  → Safe state: no useful functionality, hazards suppressed
```

This hierarchy is a design artifact. It cannot be inferred from the code. Implementers who encounter
a dependency failure without a specified degradation strategy will make inconsistent decisions.

ISO 26262 formalizes this as "functional degradation": a system continues to operate, but provides
a reduced level of functionality rather than failing completely. The concept of fail-operational vs.
fail-degraded vs. fail-safe maps directly to this hierarchy and must be specified per ASIL level.
[Source: "A Safety Argumentation for Fail-Operational Automotive Systems in Compliance with ISO
26262," ResearchGate,
https://www.researchgate.net/publication/339176369_A_Safety_Argumentation_for_Fail-Operational_Automotive_Systems_in_Compliance_with_ISO_26262]

---

## 5. Safety-Critical Error Handling Requirements

### 5.1 DO-178C: Requirements-Based, Not Ad-Hoc

DO-178C (Software Considerations in Airborne Systems and Equipment Certification, RTCA/EUROCAE,
2011) is the governing standard for aviation software certification. Its Section 6.4 specifies
software verification objectives that include requirements-based testing.

The standard mandates two test categories: normal range test cases and robustness test cases.
"The objective of robustness test cases is to demonstrate the ability of the software to respond
to abnormal inputs and conditions." At Design Assurance Levels (DAL) C and above, "robustness
testing must show that the software displays no untoward behaviour in the event of abnormal inputs
or conditions." [Source: Rapita Systems DO-178C Testing Overview,
https://www.rapitasystems.com/do178c-testing]

The critical compliance gap: robustness tests must trace to requirements. If the design document
does not specify what the unit does when it receives an out-of-range value, a NULL pointer, or an
invalid state, then there is no requirement to test against. The absence of error handling
specification in the design creates an untestable hole in the verification chain.

"DO-178 requires 'Robustness' testing, whereby both requirements (DAL A–D) and code (DAL A–C)
need to be tested for potential abnormal scenarios." DAL A (catastrophic failure condition) requires
the most complete coverage; the standard does not make error handling optional at any DAL.
[Source: AFuzion DO-178 Introduction, https://afuzion.com/do-178-introduction-old/]

The Vector Informatik whitepaper on DO-178C verification confirms: all requirements-based testing
is specification-driven. Ad-hoc defensive code (checks added by implementers without corresponding
requirements) cannot be verified against the standard, because there is no requirement against which
to verify it.
[Source: Vector Informatik Complete Verification and Validation for DO-178C,
https://cdn.vector.com/cms/content/know-how/aerospace/Documents/Complete_Verification_and_Validation_for_DO-178C.pdf]

### 5.2 ASPICE: Error Handling Must Be Traceable

Automotive SPICE (ASPICE) SWE.3 (Software Detailed Design and Unit Construction) requires that:

- Interfaces to each software unit are documented (BP.2)
- Dynamic behavior of software units is defined (BP.3)
- Bidirectional traceability between software requirements and software units is established (BP.5)

Error behavior is part of the interface specification (what does the unit return/throw under each
error condition) and part of the dynamic behavior specification (what state transitions occur when
an error is detected). An ASPICE assessment that finds error handling undocumented at the detailed
design level will raise a finding against BP.2, BP.3, and BP.5.
[Source: SWE.3 Software Detailed Design and Unit Construction, Xenban,
https://xenban.com/index.php/2025/02/12/automotive-spice-swe-3-software-detailed-design-and-unit-construction/;
ASPICE PAM 4.0 reference: https://a-spice.de/wp-content/uploads/2024/05/ASPICE_31_vs_40_part_SWE_1-6.pdf]

### 5.3 ISO 26262: Fault Tolerance Mechanisms and ASIL

ISO 26262 (Road Vehicles — Functional Safety) Part 6 addresses software-level design. The standard
requires fault tolerance mechanisms and safety mechanisms to be designed explicitly and traceable
to safety goals.

Key concepts:
- **Safety mechanism**: a technical measure implemented by an E/E system to detect and respond to
  a fault. Safety mechanisms must be designed — they cannot emerge from ad-hoc error handling.
- **Fault Tolerance Time Interval (FTTI)**: the maximum time between fault occurrence and transition
  to safe state. The FTTI is a design constraint that error handling strategies must satisfy. If a
  fault must be contained within 10ms, the error detection and recovery design must meet that
  deadline.
- **Fault metrics**: ISO 26262 Part 5 defines diagnostic coverage (DC) and single-point fault
  metric (SPFM) / latent fault metric (LFM) requirements by ASIL. These metrics are computed from
  the probability that a safety mechanism detects a fault — meaning the safety mechanisms
  themselves (which are error handling design decisions) drive the ASIL compliance numbers.

[Source: ISO 26262 Wikipedia overview, https://en.wikipedia.org/wiki/ISO_26262;
Embitel ISO 26262 safety mechanisms, https://www.embitel.com/blog/embedded-blog/the-critical-role-of-safety-mechanisms-in-iso-26262-compliance;
ISO 26262 FTTI, https://nvdungx.github.io/FUSA-FTTI/;
ISO 26262 Fault Metrics Intro, https://functionalsafetyengineer.com/intro-to-iso-26262-fault-metrics/]

### 5.4 MISRA-C: No Silent Failures

MISRA-C (Motor Industry Software Reliability Association C guidelines) is required in automotive
and aerospace C/C++ code. Its mandatory and required rules address error handling directly:

- Functions that can fail (e.g., `malloc`, `fopen`, I/O operations) must have their return values
  inspected. Ignoring a return value is a MISRA-C violation.
- There are no constructs in C that force error handling (unlike Java checked exceptions). MISRA-C
  compensates with rules that make ignoring errors a static analysis violation.
- All run-time errors must have explicit handling. The C language has no built-in dynamic error
  propagation mechanism; MISRA-C requires that developers introduce explicit checks and handling
  for every operation that can fail.

"It is important to ensure that there are measures in place to detect and handle run-time errors,
which is particularly crucial for safety-critical systems, as C language does not have built-in
mechanisms to handle dynamic errors."
[Source: Perforce MISRA-C overview, https://www.perforce.com/resources/qac/misra-c-cpp;
Medium MISRA-C violations guide, https://medium.com/@mkklyci/how-to-approach-misra-c-violations-and-code-defects-tips-and-suggestions-e5d3183c9d17]

### 5.5 Safety-Critical vs. Non-Safety Errors: A Design Classification

Not all errors in a safety-critical system are safety errors. The design must classify errors
explicitly:

| Classification | Definition | Required Handling |
|---|---|---|
| Safety-critical | Failure could cause or contribute to a hazardous event | Must be handled per specification; behavior traceable to safety requirements |
| Non-safety | Failure causes service degradation but not a hazard | Must still be handled (no silent failures), but handling is not safety-prescribed |
| Programming defect | Violation of precondition / invariant; should not occur in production | Fail-fast / assert; may be caught and logged at system boundary |

This classification is a design artifact. The safety-critical column feeds into ISO 26262 / DO-178C
evidence. The non-safety column feeds into robustness requirements. The programming defect column
defines assertion and invariant policy.

---

## 6. Specifying Error Handling in Design Documents

### 6.1 The Error Catalog

An error catalog is an enumerated list of every error condition a unit can encounter or produce.
For each entry:

- **Error identifier**: a unique name (e.g., `SENSOR_TIMEOUT`, `INVALID_ALTITUDE_RANGE`)
- **Trigger condition**: the precise condition that produces this error
- **Classification**: safety-critical / non-safety / programming defect
- **Detectability**: how is this condition detected (input validation, timeout, exception from
  dependency, hardware signal)
- **Traceability**: which requirement this error behavior implements (must link to a stated
  requirement, not be a spontaneous design addition)

The error catalog is the foundation for robustness test derivation. Each catalog entry generates
at minimum one robustness test case. Without the catalog, robustness tests are ad-hoc, and
DO-178C / ASPICE traceability cannot be established.

### 6.2 The Error Handling Matrix

An error handling matrix maps each catalog entry to its handling strategy:

| Error | Detection | Containment | Recovery Action | Caller Receives |
|---|---|---|---|---|
| SENSOR_TIMEOUT | Timer expiry | Within SensorManager | Log + degrade to last-known | DegradedReadingResult |
| INVALID_CONFIG | Input validation | Within ConfigLoader | Throw + reject startup | ConfigurationException |
| DB_UNREACHABLE | Connection exception | Within DataAccessLayer | Circuit breaker open, fallback to cache | StaleDataResult |
| NULL_INPUT | Precondition check | Within unit | Assert / IllegalArgumentException | (programming defect — caller is wrong) |

The matrix makes the design's error strategy explicit, reviewable, and testable. Each row
corresponds to test cases: one for the normal path and one (or more) for the error path.

### 6.3 How Error Specification Feeds Test Derivation

Every row in the error handling matrix generates:

1. **A robustness test case**: inject the error condition, verify the "Caller Receives" column.
2. **A containment test case**: verify the error does not propagate beyond the specified boundary.
3. **A state test case** (where applicable): verify the unit's internal state is consistent after
   the error (no partial mutations, no corrupted invariants).

This is the mechanistic connection between detailed design error specification and the test layer.
In V-model terms: error specification in detailed design → test derivation in unit test design →
test execution in unit verification.

---

## 7. Error Handling Anti-Patterns at Design Level

### 7.1 No Error Strategy

**Pattern**: The design document specifies interfaces and behavior for normal operation only. Error
conditions are not cataloged. Each implementer decides independently.

**Consequence**: Inconsistent behavior across units. Some swallow exceptions, some propagate raw,
some log and continue. The system has no testable error contract. Robustness testing is impossible
to derive systematically.

### 7.2 Catch-and-Ignore (Exception Swallowing)

**Pattern**: Exceptions are caught in empty catch blocks or blocks that only log. Callers receive
normal return values or null. The error is masked.

Research confirms this is pervasive: in one large-scale system study, exception handling anti-patterns
were found in 974 classes (21% of the system), with "catch and do nothing" being among the most
common. [Source: "Studying the evolution of exception handling anti-patterns in a long-lived
large-scale project," *Journal of the Brazilian Computer Society*, Springer,
https://link.springer.com/article/10.1186/s13173-019-0095-5]

The design-level anti-pattern is the absence of a policy. Where the design does not specify what
happens on error, implementers default to the path of least resistance: catch it, log it, return
something plausible. This is a design defect — the absence of specification creates the swallowing.

**Rule**: "Never catch an exception without logging it, wrapping it, or re-throwing it to the upper
level." [Source: prgrmmng.com, "Anti-Patterns in Exception Handling,"
https://prgrmmng.com/anti-patterns-exception-handling]

### 7.3 Over-Catching (Loss of Specificity)

**Pattern**: `catch (Exception e)` or `catch (Throwable t)` where a specific exception was
warranted. Catches types the handler does not understand and cannot correctly process.

**Consequence**: Error information is lost. The catch block cannot take a meaningful recovery action
because it doesn't know what went wrong. Higher layers that could have handled a specific error
never receive it because the over-catcher absorbed it.

At the design level: specify the exact exception types that each catch block handles. "Catch the
most specific exception possible." [Source: anti-patterns exception handling analysis,
https://prgrmmng.com/anti-patterns-exception-handling]

### 7.4 Stringly-Typed Errors

**Pattern**: Errors are represented as string codes or magic strings (`"ERR_001"`,
`"INVALID_INPUT"`) that are compared at runtime. Callers must match string values to understand
the error.

**Consequence**: Silent failures when strings are misspelled. No compile-time verification. No IDE
support. No static analysis. The design has no way to enumerate all possible error states at
design time.

**Rule**: Error codes should be strongly typed — enumerations, sealed classes, or distinct exception
types. "Using named constants or enumerations provides context and prevents errors."
[Source: Scott Hanselman, "Stringly Typed vs Strongly Typed,"
https://www.hanselman.com/blog/stringly-typed-vs-strongly-typed]

### 7.5 Exception Tunneling

**Pattern**: Infrastructure exceptions (e.g., `SQLException`, `JAXBException`) propagate through
domain and presentation layers without translation. Upper layers receive exceptions they cannot
interpret or handle correctly.

**Consequence**: Layer separation is violated. The domain layer becomes coupled to the persistence
implementation. Error handling in the presentation layer requires knowledge of database internals.

**Rule**: Exception translation is required at every significant layer boundary. The design must
specify: this layer catches infrastructure exception X and re-throws domain exception Y.
[Source: Java anti-patterns analysis, https://www.odi.ch/prog/design/newbies.php;
Oracle Effective Java Exceptions article,
https://www.oracle.com/technical-resources/articles/enterprise-architecture/effective-exceptions-part1.html]

### 7.6 Missing Error Specification in Interfaces

**Pattern**: Function signatures say nothing about what can go wrong. A method returns a value but
its Javadoc (or equivalent design spec) does not enumerate the exceptions it throws or the error
codes it returns.

**Consequence**: Callers cannot write correct error handling without reading the implementation.
The interface contract is incomplete. Automated review tools cannot verify handling.

Bloch on documentation: "Always declare checked exceptions individually, and document precisely the
conditions under which each one is thrown using the Javadoc `@throws` tag."
[Source: Effective Java, 3rd Edition, Item 74; summary at
https://ahdak.github.io/blog/effective-java-part-9/]

---

## 8. Source Index

| Source | Reference |
|---|---|
| Joshua Bloch, *Effective Java*, 3rd Edition, Addison-Wesley, 2018 | Items 69–74 on exception design |
| Michael Nygard, *Release It! 2nd Edition*, Pragmatic Programmers, 2018, ISBN 978-1680502398 | Stability patterns: Bulkhead, Circuit Breaker, Timeout, Fail Fast |
| Jim Shore, "Fail Fast," *IEEE Software*, vol. 21 no. 5, Sep/Oct 2004, pp. 21–25, DOI: 10.1109/MS.2004.1331296 | Fail-fast principle; https://www.jamesshore.com/v2/blog/2004/fail-fast |
| Robert Hanmer, *Patterns for Fault Tolerant Software*, Wiley, 2007, ISBN 0470319796 | Error Containment Barrier, Quarantine, four fault tolerance phases |
| DO-178C, RTCA/DO-178C, 2011 | Requirements-based testing, robustness testing, Table A-5 to A-7 objectives |
| ASPICE PAM 3.1 / 4.0 | SWE.3 BP.2, BP.3, BP.5 — detailed design interface, dynamic behavior, traceability |
| ISO 26262:2018, Part 6 | Software-level fault tolerance, FTTI, safety mechanisms |
| MISRA-C:2012 | Rules on error handling, return value inspection, no silent failures |
| Martin Fowler, "CircuitBreaker," martinfowler.com/bliki/CircuitBreaker.html | Circuit breaker state machine |
| SciELO Brazil — "A systematic approach for structuring exception handling in robust component-based software" | https://www.scielo.br/j/jbcos/a/sL6DtGPgr4Sc6XJrh8kxsnd/ |
| Springer — "Studying the evolution of exception handling anti-patterns" | https://link.springer.com/article/10.1186/s13173-019-0095-5 |
| AWS Well-Architected Framework — Reliability Pillar REL05-BP01 | https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/rel_mitigate_interaction_failure_graceful_degradation.html |
| Azure Architecture Center — Bulkhead Pattern | https://learn.microsoft.com/en-us/azure/architecture/patterns/bulkhead |
| Azure Architecture Center — Circuit Breaker Pattern | https://learn.microsoft.com/en-us/azure/architecture/patterns/circuit-breaker |
| Azure Architecture Center — Compensating Transaction | https://learn.microsoft.com/en-us/azure/architecture/patterns/compensating-transaction |
| Codecentric — Resilience Design Patterns | https://www.codecentric.de/en/knowledge-hub/blog/resilience-design-patterns-retry-fallback-timeout-circuit-breaker |
| JVM Weekly vol. 81/172 — Modern Error Handling in Java | https://www.jvm-weekly.com/p/on-modern-error-handling-not-just |
| InfoQ — Error Handling Considerations in SOA Analysis & Design | https://www.infoq.com/articles/error-handling-soa-design/ |
| Scott Hanselman — Stringly Typed vs Strongly Typed | https://www.hanselman.com/blog/stringly-typed-vs-strongly-typed |
| Rapita Systems — DO-178C Testing | https://www.rapitasystems.com/do178c-testing |
| Vector Informatik — Complete Verification and Validation for DO-178C | https://cdn.vector.com/cms/content/know-how/aerospace/Documents/Complete_Verification_and_Validation_for_DO-178C.pdf |
| Embitel — Safety Mechanisms in ISO 26262 | https://www.embitel.com/blog/embedded-blog/the-critical-role-of-safety-mechanisms-in-iso-26262-compliance |

---

## 9. Gaps and Honest Assessment

**Items verified against named sources:**
- Bloch's exception items (69–74) are well-documented in secondary sources; the primary text is
  Addison-Wesley 2018.
- Jim Shore's "Fail Fast" publication details (IEEE Software, Sep/Oct 2004) are confirmed by IEEE
  Xplore: https://ieeexplore.ieee.org/document/1331296/
- Nygard's Bulkhead and Circuit Breaker authorship is consistently attributed across independent
  sources.
- Hanmer's four-phase fault tolerance model is described consistently in multiple O'Reilly previews
  and secondary reviews.
- DO-178C and ASPICE requirements are described in vendor and standards-body materials; the primary
  standards documents require purchase (RTCA for DO-178C, intacs for ASPICE PAM).
- ISO 26262:2018 is a purchased standard; citations above refer to secondary materials that quote
  from the standard.

**Gaps:**
- MISRA-C specific rule numbers for error handling were not retrievable in this research session.
  Rule categories (mandatory, required, advisory) and their application to return value checking
  are described in secondary sources but specific rule identifiers (e.g., Rule 17.7) should be
  verified against MISRA-C:2012 directly.
- DO-178C Tables A-5, A-6, A-7 specific objective text requires the primary document. Descriptions
  above reflect secondary sources (Rapita Systems, Vector Informatik) that cite these tables.
- The Java Project Amber direction toward better Result/Either ergonomics (JVM Weekly) is a current
  trend as of 2026; the specifics of Project Amber proposals should be verified against
  OpenJDK JEP listings.

---

*End of research document.*
