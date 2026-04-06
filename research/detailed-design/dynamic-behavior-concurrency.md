# Dynamic Behavior and Concurrency Modeling

**Research for:** Section 3.5 (Dynamic Behavior and Concurrency) of the Detailed Design documentation page  
**Date:** 2026-04-05  
**Status:** Draft — for review before HTML authoring

---

## Purpose and Scope

This document researches the specification of dynamic behavior at the detailed design level — state machines, concurrency, timing constraints, and thread safety. The driver is ASPICE SWE.3 BP3, which requires engineers to "evaluate and detail dynamic behavior of relevant software units." The research is focused on what a design document must say, how to say it, and where each claim is grounded.

The pilot target is Java 17. Where Java specifics matter, they are called out. The standards backdrop is DO-178C (aviation) and ASPICE/ISO 26262 (automotive), both of which have explicit requirements for dynamic behavior specification.

---

## 1. ASPICE SWE.3 BP3: The Standard's Demand

ASPICE SWE.3 BP3 ("Describe dynamic behavior") requires:

> Evaluate and document the dynamic behavior of and the interaction between relevant software units.

The key qualifier is "relevant." Not every unit has dynamic behavior worth modeling. ASPICE explicitly acknowledges this: "Not all software units have dynamic behavior to be described." The engineering judgment call is whether the dynamic behavior of a unit is complex enough that a reader cannot derive correct tests or a correct implementation from the static interface description alone.

Indicators that dynamic behavior specification is needed:
- The unit has more than one observable state
- Behavior differs based on sequence of prior calls (temporal coupling)
- Multiple threads may interact with the unit simultaneously
- Timing constraints are imposed by requirements or the runtime environment
- The unit manages resources that must be released in specific orders

Sources:
- ASPICE SWE.3 BP3 text, reproduced at: https://alef1986.github.io/ASPICE-Archi/0c6fbcf4-57de-4e25-a1b4-d9a0fa460c16/elements/c0718f06-8669-428c-b190-3387fb6d6f57.html
- Commentary on scope: https://www.flecsim.de/images/download/AutomotiveSpiceShortened/Automotive%20Spice%203.1/SWE.3.html

---

## 2. State Machine Design in Practice

### 2.1 Statecharts: The Foundation

David Harel introduced statecharts in 1987 as an extension of conventional state-transition diagrams. The core paper, "Statecharts: A Visual Formalism for Complex Systems" (Science of Computer Programming, Vol. 8, No. 3, pp. 231–274, 1987), identified three fundamental limitations of flat FSMs and addressed each:

1. **Hierarchy** — States can contain sub-states, reducing diagram size by orders of magnitude
2. **Concurrency** — Orthogonal regions allow parallel sub-states within a single state
3. **Communication** — Events can be broadcast between concurrent regions

Harel's original claim was that statecharts are "compact and expressive — small diagrams can express complex behavior — as well as compositional and modular." This holds for design purposes: a statechart in a design document can express behavior that would require thousands of states in a flat FSM.

Sources:
- Original paper: https://www.sciencedirect.com/science/article/pii/0167642387900359
- PDF: https://dubroy.com/refs/Statecharts_a_visual_formalism_for_complex_systems.pdf
- Semantic Scholar entry: https://www.semanticscholar.org/paper/Statecharts:-A-Visual-Formalism-for-Complex-Systems-Harel/18493175642909909196e99b90a6af0bf3ef803b

### 2.2 The State Explosion Problem

Flat FSMs scale badly. When two independent FSMs with M and N states respectively are composed into a single system, the combined state space is M × N. Add a third with P states and it becomes M × N × P. This is the state explosion problem.

For design documents, state explosion manifests as diagrams that become impossible to read, review, or maintain. The practical consequence is that engineers stop keeping them up to date — the design document diverges from the implementation.

Mitigations at design level:
- **Use hierarchy** (Harel statecharts): one top-level state can hide a sub-machine. Document the sub-machine separately.
- **Decompose by concern**: separate state machines for lifecycle management, error handling, and functional behavior rather than one monolithic machine.
- **Use orthogonal regions** only when the regions are genuinely independent. If they communicate, the independence is false and the explosion returns in guard logic.

The state explosion problem is also the central challenge for formal verification tools (model checkers). Clarke and others documented techniques including symbolic model checking with BDDs and partial order reduction. (Clarke, E.M., Klieber, W., Nováček, M., Zuliani, P., "Model Checking and the State Explosion Problem," Springer, 2012 — https://link.springer.com/chapter/10.1007/978-3-642-35746-6_1)

For detailed design purposes, the relevant insight is: if a state machine is too large to fit on two pages, it is almost certainly too large for reviewers to reason about. Decompose it.

### 2.3 Testing State Machines

State-based testing has a well-established coverage hierarchy. From most permissive to most thorough:

- **State coverage** — every state is visited at least once
- **Transition coverage** — every transition is exercised at least once (also called 1-switch coverage)
- **Round-trip path coverage** — every cycle through the state machine is covered
- **Sneak-path testing** — verify that illegal transitions cannot be triggered

Sneak-path testing is specifically relevant to safety. A sneak path test exercises the system with an event that is undefined for the current state. The correct outcome is that the system ignores the event, rejects it, or enters a defined error state — not that it silently transitions somewhere unexpected. This is especially important when state-based faults include missing transitions (no response to an event), extra transitions (a response where none was specified), or incorrect actions.

"SBT can detect various state-based faults — incorrect transition, missing transition, missing or incorrect event, missing or incorrect action — which are difficult to detect in code-based testing." (Coverage criteria survey: https://www.researchgate.net/publication/330047546_Coverage_Criteria_for_State-Based_Testing_A_Systematic_Review)

The Delft course notes on sneak-path testing give the practical definition: "sneak-path testing is testing of how software reacts to unexpected events for a particular system state. The resulting sneak path test suite helps us to verify that illegal transactions cannot occur." (https://delftxdownloads.tudelft.nl/ST2x_Automated_Software_Testing/Module_2/ST2x_2018_Module_2_13_Sneak_Path_Testing-transcript.pdf)

### 2.4 What the Design Document Must Specify

A state machine section in a detailed design document must include:

1. **State inventory** — every state, with a description of the invariant that holds in that state
2. **Transition table** — source state, trigger event, guard condition, action, target state
3. **Initial state** — explicitly called out, not assumed
4. **Terminal states** — how and when the machine terminates (or whether it is intended to run indefinitely)
5. **Undefined event handling** — what happens when an event arrives in a state where it is not defined (ignore, log, fault, exception)
6. **Entry/exit actions** — if statecharts are used, entry and exit actions must be documented separately from transition actions because they execute differently

The transition table format is better than a diagram alone for design documentation because it is reviewable without specialized tools and is directly traceable to test cases.

### 2.5 Common Pitfalls

**Unreachable states**: A state is defined but no transition leads to it from the initial state or any reachable state. These are dead code at the state machine level and often indicate a design error.

**Ambiguous guards**: Two transitions from the same state on the same event with guards that can both be true simultaneously. The FSM is then nondeterministic. Design must ensure guards are mutually exclusive.

**Missing self-loop specification**: When an event arrives in a state and the intended behavior is "do nothing," this must be explicitly specified as a self-loop or explicitly listed as a rejected event. Silence is ambiguous.

**Unreachable terminal states**: The machine can never reach its terminal state, meaning resources it holds are never released.

---

## 3. Concurrency Specification

### 3.1 Thread Safety as a Design Contract

Thread safety is not an implementation detail — it is a contract that must be stated in the design. Brian Goetz et al. in "Java Concurrency in Practice" (Addison-Wesley, 2006, ISBN 0321349601) established the standard taxonomy for Java thread safety levels. The design document must declare which category applies to each class or component:

- **Immutable** — object state cannot change after construction; inherently thread-safe with no synchronization required
- **Thread-safe** — internal synchronization is sufficient; clients need no external coordination
- **Conditionally thread-safe** — some method sequences require external synchronization (e.g., `Iterator` usage on a synchronized collection)
- **Thread-compatible** — not thread-safe internally; safe to use from multiple threads only if all callers synchronize externally
- **Thread-hostile** — cannot be made thread-safe by any synchronization; avoid in multi-threaded contexts

This taxonomy is from JCIP Chapter 4 and is the de facto standard for Java design documentation. The book is at: https://www.oreilly.com/library/view/java-concurrency-in/0321349601/

Goetz's central formulation: "If multiple threads access the same mutable state variable without appropriate synchronization, there are three ways to fix it: Don't share the state variable across threads; Make the state variable immutable; or Use synchronization whenever accessing the state variable." (JCIP, Chapter 1)

### 3.2 Thread Safety Annotations

JSR-305 defines three annotations for documenting thread safety in Java source code, as formalized in JCIP:

- `@ThreadSafe` — the class is safe for use by multiple threads without external synchronization
- `@NotThreadSafe` — the class is not safe for concurrent use
- `@GuardedBy("lockName")` — the annotated field or method may only be accessed when holding the named lock
- `@Immutable` — the class is immutable and therefore inherently thread-safe

These annotations are design-level documentation first, tooling hints second. The SEI CERT Oracle Coding Standard for Java (rule CON52-J) requires documenting thread-safety and using annotations where applicable: https://wiki.sei.cmu.edu/confluence/display/java/CON52-J.+Document+thread-safety+and+use+annotations+where+applicable

For detailed design documents, the requirement is to state the thread-safety level and document the locking policy for every class that will be accessed from multiple threads. The annotations belong in the design specification before they appear in the code.

### 3.3 The Java Memory Model and Happens-Before

The Java Memory Model (JMM), formalized in JSR-133 (2004, Java 5.0), defines when writes by one thread are guaranteed to be visible to reads by another thread. The core concept is the **happens-before** relationship.

JSR-133 FAQ (William Pugh, University of Maryland): "Two actions can be ordered by a happens-before relationship. If one action happens-before another, then the first is visible to and ordered before the second." (https://www.cs.umd.edu/~pugh/java/memoryModel/jsr-133-faq.html)

The canonical happens-before rules in Java 17:
- **Monitor lock rule**: An unlock of a monitor happens-before every subsequent lock of that monitor
- **Volatile variable rule**: A write to a volatile field happens-before every subsequent read of that field
- **Thread start rule**: A call to `Thread.start()` happens-before any action in the started thread
- **Thread join rule**: All actions in a thread happen-before any other thread successfully returns from `Thread.join()` on that thread
- **Transitivity**: If A happens-before B, and B happens-before C, then A happens-before C

The design implication: if a field is shared between threads and there is no happens-before relationship between the write and the read, the read may observe a stale value. This is not hypothetical — it is permitted by the JMM specification and has been observed in production systems.

The underlying concept of happens-before as a partial ordering of distributed events originates with Lamport (1978): "Time, Clocks, and the Ordering of Events in a Distributed System," Communications of the ACM, Vol. 21, No. 7, pp. 558–565. https://dl.acm.org/doi/10.1145/359545.359563

### 3.4 Shared State Documentation

Every piece of shared mutable state must be documented at design level. This means:

1. **Identify the field** — which fields are shared across threads
2. **Identify the guard** — which lock (or other mechanism) protects each field
3. **Identify the happens-before** — for volatile fields, what the write/read ordering guarantees
4. **Identify the invariants** — what must remain true across all threads at all times

This is not optional documentation. An undocumented shared field is a design defect — future maintainers cannot know whether synchronization is missing or intentionally absent.

### 3.5 Synchronization Mechanisms in Java 17

For design-level specification, the choice of synchronization mechanism must be justified:

| Mechanism | Use When | Documented In |
|---|---|---|
| `synchronized` block/method | Simple mutual exclusion, monitor pattern | JCIP Ch. 2 |
| `java.util.concurrent.locks.ReentrantLock` | Need try-lock, timed lock, or interruptible lock | JCIP Ch. 13 |
| `volatile` field | Single field, write visibility only, no compound actions | JSR-133 |
| `java.util.concurrent.atomic.*` | Lock-free single-variable operations via CAS | JCIP Ch. 15 |
| Immutable value objects | No mutation after construction | JCIP Ch. 3 |
| Thread confinement (e.g., `ThreadLocal`) | Thread-specific state, no sharing required | JCIP Ch. 3 |

Lock-free programming using Compare-And-Swap (CAS) is covered formally in Herlihy & Shavit, "The Art of Multiprocessor Programming" (Morgan Kaufmann, 2012, ISBN 9780123973375), which introduces the formal notions of linearizability, lock-free progress, and wait-free progress. The design document should specify which progress guarantee is required: lock-free (some thread makes progress) vs. wait-free (every thread makes progress in bounded steps). Most Java production code uses lock-free where needed (via `java.util.concurrent.atomic`) but does not require wait-freedom.

Source: https://dl.acm.org/doi/book/10.5555/2385452

---

## 4. Timing and Performance Constraints

### 4.1 WCET: Definition and Design Role

Worst-Case Execution Time (WCET) is the maximum time a unit could take to execute on specific hardware. Wikipedia definition: "The worst-case execution time (WCET) of a computational task is the maximum length of time the task could take to execute on a specific hardware platform." https://en.wikipedia.org/wiki/Worst-case_execution_time

WCET is an input to schedulability analysis and a constraint on implementation. At detailed design level, WCET must be specified as a requirement on the implementation, not derived from it. The workflow is:

1. System requirements allocate timing budgets to functions
2. Detailed design decomposes those budgets to software units
3. The implementation must satisfy the WCET specified in design
4. Verification measures WCET and confirms it is within budget

For DO-178C, WCET connects to objective 6.3.4.f — evidence that source code is "accurate and consistent," which includes timing behavior. Rapita Systems documents this connection explicitly: "In airborne software some attention to software is required by DO-178C section 6.3.4." (https://www.rapitasystems.com/worst-case-execution-time)

The LDRA tool documentation confirms: "Timing analysis is just one small part of a much bigger picture that is painted by compliance with standards including DO-178C, ISO 26262, and IEC 61508." (https://ldra.com/capabilities/wcet/)

A comprehensive survey of WCET analysis methods is: Wilhelm et al., "The worst-case execution time problem — overview of methods and survey of tools," ACM Transactions on Embedded Computing Systems, Vol. 7, No. 3, 2008. https://dl.acm.org/doi/10.1145/1347375.1347389

### 4.2 Timing Constraint Specification Format

A timing constraint in a detailed design document must specify:

- **Constraint type**: deadline, period, minimum inter-arrival time, response time bound
- **Value**: numeric bound with unit (milliseconds, microseconds)
- **Conditions**: under what load/input conditions the bound applies (worst-case, average-case, nominal)
- **Source**: which higher-level requirement this derives from
- **Measurement method**: how this will be verified (static analysis, instrumented measurement, formal proof)

Vague timing specifications — "the function shall execute quickly" — are untestable and non-traceable. A concrete example of a well-formed timing constraint:

> `processFrame()` shall complete within 5 ms on the target hardware (ARM Cortex-M4 @ 168 MHz) under worst-case input conditions, as derived from system requirement SYS-TIMING-003. Verification method: WCET analysis using static analysis tool, supplemented by instrumented measurement on target hardware.

### 4.3 Schedulability Analysis

When multiple tasks compete for a processor, schedulability analysis determines whether all tasks can meet their deadlines. The two dominant algorithms are:

- **Rate Monotonic (RM)**: fixed priorities assigned by period (shorter period = higher priority). The RM utilization bound for schedulability is approximately 69% for a large number of tasks (Liu and Layland, 1973, JACM).
- **Earliest Deadline First (EDF)**: dynamic priorities; always runs the task with the earliest absolute deadline. EDF achieves 100% utilization on a single processor.

The key paper comparing these is Buttazzo, "Rate Monotonic vs. EDF: Judgment Day," IEEE Transactions on Computers, 2005. (https://www.eecs.umich.edu/courses/eecs571/reading/rm-vs-edf.pdf)

For design purposes: the scheduling algorithm used by the runtime must be documented because it affects what timing constraints are achievable. A design that allocates timing budgets assuming EDF scheduling is incompatible with a runtime that uses fixed-priority RM.

In ARINC 653 partitioned systems (used in DO-178C avionics), partitions have pre-allocated time windows in a major frame. The design must specify which partition window a unit executes in and whether its WCET fits within that window.

---

## 5. Modeling Dynamic Behavior: When and How

### 5.1 UML Activity Diagrams for Concurrent Behavior

UML activity diagrams support modeling concurrent behavior through fork and join nodes. A fork node creates multiple concurrent flows; a join node synchronizes them (it does not fire until all incoming flows have arrived). The distinction between a join and a merge is critical: a merge is an OR (any incoming flow activates it), a join is an AND (all incoming flows must arrive).

The OMG's specification for concurrency in UML 2.6 is the normative reference: https://www.omg.org/certification/uml/documents/concurrency_in_uml_version_2.6.pdf

Practical use cases for activity diagrams in detailed design:
- Specifying initialization sequences with parallel setup steps
- Documenting producer-consumer interactions between threads
- Showing cancellation paths alongside normal paths

### 5.2 Petri Nets for Concurrent Behavior

Petri nets are a formal model for concurrent and distributed systems. Their key advantage over activity diagrams is that they have a formal semantics enabling automated analysis: reachability analysis can detect deadlocks (states where no transition is enabled), livelocks, and boundedness violations.

"Since firing is nondeterministic, and multiple tokens may be present anywhere in the net, Petri nets are well suited for modeling the concurrent behavior of distributed systems." (Rawson, "Petri Nets for Concurrent Programming," arXiv:2208.02900 — https://arxiv.org/pdf/2208.02900)

Safety-critical application: Heiner and Heisel, "Modeling Safety-Critical Systems with Z and Petri Nets" (Semantic Scholar: https://www.semanticscholar.org/paper/Modeling-Safety-Critical-Systems-with-Z-and-Petri-Heiner-Heisel/19cf3eeee54a63576f69c028272df807f26aae94) demonstrates integrating Z specifications with Petri nets for safety analysis.

A systematic methodology for building Petri net models of concurrent systems from behavioral specifications is in: Frey et al., "A Systematic Approach to the Petri Net Based Specification of Concurrent Systems," Real-Time Systems, Vol. 15, 1998. https://link.springer.com/article/10.1023/A:1007907309442

The book-length reference is: Reisig and Rozenberg (eds.), "Petri Nets for Systems Engineering: A Guide to Modeling, Verification, and Applications," Springer, 2010. https://link.springer.com/book/10.1007/978-3-662-05324-9

**When to use Petri nets**: when concurrency is the primary design concern and automated deadlock/liveness analysis is needed. Petri nets are harder to read for engineers unfamiliar with the formalism. For most detailed design documents, activity diagrams with fork/join suffice. Petri nets are appropriate when shared resource access patterns are complex enough that informal review cannot confidently rule out deadlock.

### 5.3 Timing Diagrams

UML timing diagrams show the state of an object over time and the moments at which transitions occur. They are appropriate when timing relationships between state changes are the primary concern — for example, specifying that a signal must be stable for at least 5 ms before a state transition is triggered.

### 5.4 Formal Verification with UPPAAL

UPPAAL is an integrated tool for modeling, validating, and verifying real-time systems as networks of timed automata. It supports:
- Networks of communicating timed automata
- Clock variables with reset and comparison constraints
- Model checking of safety and bounded-liveness properties

"UPPAAL is an integrated tool environment for modeling, validation and verification of real-time systems modeled as networks of timed automata, extended with data types (bounded integers, arrays, etc.)." (UPPAAL home: https://uppaal.org/)

UPPAAL was developed by Uppsala University and Aalborg University. The canonical description is: Larsen et al., "UPPAAL — a tool suite for automatic verification of real-time systems," Springer Lecture Notes in Computer Science, 1996. https://link.springer.com/chapter/10.1007/BFb0020949

**When to use UPPAAL**: when timing constraints between units interact and informal analysis cannot confirm correctness. UPPAAL model checking is expensive in engineering time. The return on investment is highest for: real-time communication protocols, safety-critical state machines with timing guards, and cases where a certification authority requires formal evidence of timing compliance.

### 5.5 Tool Summary: When to Use What

| Scenario | Recommended Tool | Why |
|---|---|---|
| Simple state machine, one unit | State table + PlantUML state diagram | Readable, reviewable, no toolchain dependency |
| Complex state machine with hierarchy | Statechart (UML) in PlantUML or Enterprise Architect | Hierarchy reduces visual complexity |
| Concurrent interaction between units | UML activity diagram with fork/join (Mermaid or PlantUML) | Standard notation, renderable in most doc systems |
| Shared resource access, deadlock concern | Petri net model | Formal deadlock analysis |
| Timing constraints between components | UML timing diagram or timed automata | Explicit temporal relationships |
| Real-time verification for certification | UPPAAL model checker | Automated proof, defensible artifact for auditors |

PlantUML vs. Mermaid: Mermaid renders natively in GitHub, GitLab, and Notion without plugins. PlantUML offers more complete UML coverage and is better suited to long-lived regulated specifications. "Many teams keep both in the repo: Mermaid for narrative docs, PlantUML for formal system diagrams that must stay stable over time." (https://mermaideditor.com/blog/mermaid-vs-plantuml-vs-drawio)

---

## 6. Concurrency Anti-Patterns in Design

These are design-level errors — they manifest before a line of code is written, because they are errors in how the design specifies (or fails to specify) concurrent behavior.

### 6.1 Designing for Races

A race condition exists when the correctness of a computation depends on the relative timing of threads. The most common design-level source is **check-then-act** patterns where the check and the act are not atomic: a thread checks a condition (is there space in the buffer?), and acts based on it (write to the buffer), but another thread modifies the state between the check and the act.

At design level, race conditions appear as operations described in two sentences that must be atomic but are not specified to be atomic. The design document must explicitly state compound atomicity requirements.

### 6.2 Double-Checked Locking (Without volatile)

Double-checked locking is a specific anti-pattern documented at https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html as "The 'Double-Checked Locking is Broken' Declaration." The broken version reads: check if the object is null (without a lock), lock if null, check again, initialize. The problem: without `volatile`, the JMM allows the write to the reference to become visible before the writes to the object's fields. A reader can see a non-null reference to a partially constructed object.

The fix (Java 5+) is to declare the field `volatile`. This works because the volatile write happens-before any subsequent volatile read. (SEI CERT Oracle Standard rule LCK10-J: https://wiki.sei.cmu.edu/confluence/display/java/LCK10-J.+Use+a+correct+form+of+the+double-checked+locking+idiom)

The design implication: if a design specifies lazy initialization of a shared singleton, it must explicitly specify whether the field is `volatile` and why. Silence on this point will lead an implementer to produce broken code.

### 6.3 Over-Synchronization

Over-synchronization reduces parallelism and can cause deadlocks by extending lock scope unnecessarily. "Profiling shows lock contention can drop performance by up to 50% under heavy load." (https://dl.acm.org/doi/10.1145/3644032.3644466)

Eight Java lock contention anti-patterns have been identified empirically in the literature (IEEE Xplore: https://ieeexplore.ieee.org/document/10675981/). Common forms:

- **Excessive critical section size**: synchronizing code that does not need protection (I/O inside a synchronized block, for example)
- **Synchronizing on the wrong object**: different callers synchronize on different instances and thus provide no mutual exclusion
- **Using synchronized when volatile suffices**: volatile is cheaper than synchronized when only write visibility (not atomicity) is needed
- **Lock nesting without consistent ordering**: acquiring locks A then B in one code path, B then A in another — guaranteed deadlock under concurrent access

Design mitigation: specify the minimum critical section for each operation. If a design says "method X is synchronized," it must also specify which state is being protected and why the entire method (rather than a smaller block) requires synchronization.

### 6.4 Missing Cancellation Specification

Java's concurrency model uses cooperative cancellation via interruption. "Java does not provide any mechanism for safely forcing a thread to stop what it is doing. Instead, it provides interruption, a cooperative mechanism that lets one thread ask another to stop what it is doing." (JCIP Ch. 7, O'Reilly: https://www.oreilly.com/library/view/java-concurrency-in/0321349601/ch07.xhtml)

A design that specifies a long-running task (network operation, file I/O, computation loop) without specifying its cancellation policy is incomplete. JCIP defines cancellation policy as specifying three things: how other code requests cancellation, when the task checks for the cancellation request, and what actions the task takes in response.

Design documents must state:
- Whether a task is cancellable
- Which mechanism triggers cancellation (`interrupt()`, a flag, a shared `AtomicBoolean`)
- What the task does when cancelled (rollback, partial commit, resource cleanup)

### 6.5 Implicit Concurrency Assumptions in Interfaces

An interface that returns a mutable object hands the caller a reference to state that may be shared internally. If the caller modifies it, the component's invariants may be violated. If multiple callers do this concurrently, the result is a data race that did not originate in the component's implementation.

Design must specify: whether returned objects are owned by the caller (copied), shared (read-only), or live views (the caller must not hold them beyond the call). This is an interface contract, not an implementation detail.

---

## 7. Safety-Critical Concurrency

### 7.1 DO-178C: Spatial and Temporal Partitioning

DO-178C addresses concurrency primarily through partitioning. In Integrated Modular Avionics (IMA) systems, ARINC 653 provides spatial and temporal isolation between partitions.

ARINC 653 Wikipedia: "ARINC 653 is a software specification for space and time partitioning in safety-critical avionics real-time operating systems that allows hosting of multiple applications of different software levels on the same hardware." https://en.wikipedia.org/wiki/ARINC_653

Spatial partitioning: each partition has its own memory region enforced by hardware (MMU/MPU). Writes outside the partition's region cause a fault, not silent corruption.

Temporal partitioning: each partition receives a fixed time window in a repeating major frame. A partition that overruns its window is preempted. This prevents a misbehaving partition from starving others of CPU time.

The design implication for detailed design: software units in a partitioned system must be specified with their partition assignment, their timing window, and their WCET relative to that window. A unit whose WCET exceeds the partition window is a design defect, not an implementation problem.

Challenge in multicore: ARINC 653 partitioning is sufficient for single-core systems. In multicore systems, interference on shared resources (caches, buses, memory controllers) can violate temporal isolation even when time windows are enforced. Rapita Systems: "Robust partitioning for multicore systems doesn't mean freedom from interference." (https://www.rapitasystems.com/blog/robust-partitioning-multicore-systems-doesnt-mean-freedom-interference)

### 7.2 ISO 26262: Freedom from Interference

ISO 26262 defines freedom from interference (FFI) as "the absence of cascading failures between two or more elements that could lead to the violation of a safety requirement." ISO 26262 Part 6 Clause 7 requires that software components of different ASIL levels (or ASIL and QM) must not interfere.

Three interference categories under ISO 26262:
- **Timing and execution**: one component's execution time affects another's deadline compliance
- **Memory**: one component writes to another's memory region
- **Exchange of information**: corrupted data propagates between components

AUTOSAR addresses FFI through OS-Application memory protection (spatial), execution budget monitoring (temporal), and RTE-controlled communication (logical). (AUTOSAR FFI documentation: https://piembsystech.com/freedom-from-interference-iso-26262/)

Design documents for safety-critical automotive software must include an FFI analysis for every interface between components of different ASIL levels. This is not an implementation-phase concern — it is a design constraint that shapes the interface specification.

### 7.3 Shared Resource Specifications

Shared resources (semaphores, shared memory, hardware registers, communication channels) require explicit design documentation covering:

1. **Access protocol**: how a component requests access, what happens if the resource is unavailable, maximum wait time
2. **Ownership semantics**: who creates, who destroys, who is responsible for error recovery
3. **Conflict resolution**: what happens if two components request simultaneously (priority, first-come-first-served, error return)
4. **Timeout specification**: maximum time a component may hold the resource; what happens if it holds too long (watchdog, forced release)

---

## 8. What a Good Dynamic Behavior Specification Contains: Summary

A detailed design section on dynamic behavior is complete when a reviewer who has not seen the implementation can:

1. **Derive tests** from the state machine specification (every state, every transition, every guard, sneak paths for undefined events)
2. **Verify thread safety** without reading the implementation (the thread-safety level is declared, locking policy is stated, shared fields are identified with their guards)
3. **Verify timing compliance** without running the system (WCET budget is specified, scheduling context is identified, measurement method is defined)
4. **Identify all concurrency hazards** upfront (race conditions in compound operations are called out; cancellation policy is specified; shared resource protocols are documented)

Dynamic behavior that is only documented in comments in the source code is not a design artifact — it is undocumented behavior. The design document must exist independently of the implementation so that independent review and test derivation are possible.

---

## Gaps and Honest Uncertainties

1. **WCET for Java (JVM)**: Java's JIT compilation, garbage collection, and JVM startup make WCET analysis significantly harder than for compiled C/C++. The standard WCET analysis literature primarily addresses C on bare-metal or RTOS targets. Java is used in safety-critical contexts (avionics SPARK Ada is the usual path; Java is less common) but the tooling for Java WCET analysis is less mature. The research did not surface a Java-specific WCET methodology. Real-time Java (RTSJ, JSR-1) addresses some of this but is a specialized subset. Engineers using standard Java 17 in timing-sensitive contexts should treat WCET claims as approximate unless using a JVM with deterministic GC and bounded JIT (e.g., Azul Zing, IBM J9 deterministic mode).

2. **Petri net tooling**: The research found the theoretical case for Petri nets well documented but practical tool availability (beyond research tools like PIPE, CPNTools) is not confirmed in recent production safety engineering contexts. Domain experts should confirm whether Petri net analysis is expected by their certification body.

3. **ASPICE BP3 exact wording**: The direct page for SWE.3.BP3 exact normative text was not fully retrievable. The text cited above is consistent across multiple secondary sources but should be verified against the official ASPICE PAM document.

---

## Source Inventory

- Harel, D. (1987). Statecharts: A visual formalism for complex systems. *Science of Computer Programming*, 8(3), 231–274. https://www.sciencedirect.com/science/article/pii/0167642387900359
- Clarke, E.M. et al. (2012). Model Checking and the State Explosion Problem. Springer. https://link.springer.com/chapter/10.1007/978-3-642-35746-6_1
- Coverage Criteria for State-Based Testing (survey). https://www.researchgate.net/publication/330047546_Coverage_Criteria_for_State-Based_Testing_A_Systematic_Review
- Sneak-Path Testing transcript, Delft. https://delftxdownloads.tudelft.nl/ST2x_Automated_Software_Testing/Module_2/ST2x_2018_Module_2_13_Sneak_Path_Testing-transcript.pdf
- Goetz, B. et al. (2006). *Java Concurrency in Practice*. Addison-Wesley. ISBN 0321349601. https://www.oreilly.com/library/view/java-concurrency-in/0321349601/
- JSR-133: Java Memory Model and Thread Specification. https://www.cs.umd.edu/~pugh/java/memoryModel/jsr133.pdf
- JSR-133 FAQ. https://www.cs.umd.edu/~pugh/java/memoryModel/jsr-133-faq.html
- Lamport, L. (1978). Time, Clocks, and the Ordering of Events in a Distributed System. *Comm. ACM*, 21(7), 558–565. https://dl.acm.org/doi/10.1145/359545.359563
- Herlihy, M. & Shavit, N. (2012). *The Art of Multiprocessor Programming*. Morgan Kaufmann. https://dl.acm.org/doi/book/10.5555/2385452
- SEI CERT CON52-J. https://wiki.sei.cmu.edu/confluence/display/java/CON52-J.+Document+thread-safety+and+use+annotations+where+applicable
- SEI CERT LCK10-J. https://wiki.sei.cmu.edu/confluence/display/java/LCK10-J.+Use+a+correct+form+of+the+double-checked+locking+idiom
- Pugh, W. Double-Checked Locking is Broken. https://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
- Wilhelm, R. et al. (2008). The worst-case execution time problem — overview of methods and survey of tools. *ACM TECS*, 7(3). https://dl.acm.org/doi/10.1145/1347375.1347389
- Rapita Systems. Worst Case Execution Time. https://www.rapitasystems.com/worst-case-execution-time
- LDRA. WCET and DO-178C. https://ldra.com/capabilities/wcet/
- Buttazzo, G. (2005). Rate Monotonic vs. EDF: Judgment Day. https://www.eecs.umich.edu/courses/eecs571/reading/rm-vs-edf.pdf
- OMG. Concurrency in UML Version 2.6. https://www.omg.org/certification/uml/documents/concurrency_in_uml_version_2.6.pdf
- Rawson, M. (2022). Petri Nets for Concurrent Programming. arXiv:2208.02900. https://arxiv.org/pdf/2208.02900
- Frey, G. et al. (1998). A Systematic Approach to the Petri Net Based Specification of Concurrent Systems. *Real-Time Systems*. https://link.springer.com/article/10.1023/A:1007907309442
- Larsen, K.G. et al. (1996). UPPAAL — a tool suite for automatic verification of real-time systems. Springer LNCS. https://link.springer.com/chapter/10.1007/BFb0020949
- UPPAAL home. https://uppaal.org/
- PlantUML vs. Mermaid comparison. https://mermaideditor.com/blog/mermaid-vs-plantuml-vs-drawio
- Java lock contention anti-patterns (ACM AST 2024). https://dl.acm.org/doi/10.1145/3644032.3644466
- JCIP Ch. 7 (Cancellation and Shutdown). https://www.oreilly.com/library/view/java-concurrency-in/0321349601/ch07.xhtml
- ARINC 653 Wikipedia. https://en.wikipedia.org/wiki/ARINC_653
- Rapita on multicore partitioning. https://www.rapitasystems.com/blog/robust-partitioning-multicore-systems-doesnt-mean-freedom-interference
- ISO 26262 FFI (piembsystech). https://piembsystech.com/freedom-from-interference-iso-26262/
- ASPICE SWE.3 BP3. https://alef1986.github.io/ASPICE-Archi/0c6fbcf4-57de-4e25-a1b4-d9a0fa460c16/elements/c0718f06-8669-428c-b190-3387fb6d6f57.html
- ASPICE SWE.3 full process. https://www.flecsim.de/images/download/AutomotiveSpiceShortened/Automotive%20Spice%203.1/SWE.3.html
