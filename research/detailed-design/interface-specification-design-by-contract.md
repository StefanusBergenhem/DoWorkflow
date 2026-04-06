# Interface Specification and Design by Contract

**Research for:** Section 3.2 — Interface Specification, Detailed Design documentation page  
**Date:** 2026-04-05  
**Status:** Draft — for review before HTML authoring

---

## 1. Why Interface Specification Is the Core of Detailed Design

ASPICE SWE.3 BP.2 (Software Detailed Design and Unit Construction, Base Practice 2) requires that
the interfaces of each software unit be defined explicitly. The Automotive SPICE Process Reference
Model frames this directly: without interface information it is impossible to properly test interfaces
in unit verification (SWE.4). [Source: Synopsys ASPICE assessment guide — see §8 below.]

DO-178C carries the same obligation from a different angle. Its Table A-5 objectives require
bidirectional traceability from system requirements through high-level requirements → low-level
requirements → source code. Interface specifications are the mechanism by which low-level
requirements become testable: they define exactly what a unit promises to callers, and that promise
is what the test exercises. [Source: DO-178C Wikipedia overview; Parasoft DO-178C traceability
guide — see §8.]

ISO 26262 Part 6 (Software at the vehicle level) requires that software unit design describe the
interfaces of each unit, including input/output data ranges, error handling, and initialization
conditions. The standard's notation table — natural language / semi-formal / formal — scales
formality with ASIL: semi-formal methods are "highly recommended" for ASIL C/D, and formal methods
for ASIL D verification. [Source: EmbeddedInEmbedded ISO 26262 Part 6 explanation — see §8.]

The theoretical foundations predate all three standards by decades and are worth understanding in
full, because the standards reflect them.

---

## 2. Hoare Logic: The Formal Bedrock (1969)

C. A. R. Hoare published "An Axiomatic Basis for Computer Programming" in *Communications of the
ACM*, vol. 12, no. 10, pp. 576–580, October 1969.
[ACM Digital Library: https://dl.acm.org/doi/10.1145/363235.363259]

Hoare introduced the triple notation:

```
{P} S {Q}
```

Read: "If P holds before statement S executes, and S terminates, then Q holds afterwards."

- **P** — the *precondition*: what must be true about the state before S is called.
- **S** — the *command* or program fragment.
- **Q** — the *postcondition*: what the command guarantees about the resulting state.

This is partial correctness: Hoare triples do not assert termination; total correctness requires a
separate termination proof. The paper provides axioms (the assignment axiom) and inference rules
(composition, conditional, iteration) that allow correctness proofs to be composed from smaller
pieces.

Why this matters for interface specification: every function interface is, in essence, a Hoare
triple. The precondition names the obligations of the caller; the postcondition names the
obligations of the implementer. This is not metaphor — it is the exact mathematical structure.
Calling it "documentation" or "a contract" does not change the underlying logic.

Stanford SOCO course notes (Crawford Roberts, 2008) describe Hoare logic as the foundation for all
assertion-based reasoning in programming languages.
[https://cs.stanford.edu/people/eroberts/courses/soco/projects/2008-09/tony-hoare/logic.html]

CMU 15-819O lecture notes (Jonathan Aldrich) provide accessible treatment of the axioms and
inference rules.
[https://www.cs.cmu.edu/~aldrich/courses/15-819O-13sp/resources/hoare-logic.pdf]

---

## 3. Information Hiding and the Module Interface (1972)

David Parnas, "On the Criteria To Be Used in Decomposing Systems into Modules," *Communications of
the ACM*, vol. 15, no. 12, pp. 1053–1058, December 1972.
[ACM Digital Library: https://dl.acm.org/doi/10.1145/361598.361623]
[PDF at MIT: http://sunnyday.mit.edu/16.355/parnas-criteria.html]

Parnas compared two decomposition strategies for a KWIC (Key Word in Context) index system. His
conclusion: modules should be decomposed around *design decisions that are likely to change*, not
around processing steps. Each module hides one such decision. Its interface must reveal enough for
other modules to use it correctly, and nothing more.

The key quote from the paper:

> "Every module in the decomposition is characterized by its knowledge of a design decision
> which it hides from all others. Its interface or definition was chosen to reveal as little as
> possible about its inner workings."

This establishes two binding constraints on interface specification:

1. **The interface must be sufficient.** Every fact a caller needs to use the module correctly must
   appear in the interface.
2. **The interface must be minimal.** Every fact that is an internal implementation decision must
   be absent from the interface.

These two constraints together define the scope of an interface contract. Violation of the first
produces callers that rely on undocumented behaviour (fragile, untestable). Violation of the second
produces interface-implementation coupling (callers that break when internal structure changes).

Parnas's paper is the direct intellectual predecessor of encapsulation in object-oriented
programming, though it predates it. H. Conrad Cunningham's annotated version of the Parnas papers
(University of Mississippi) provides useful commentary.
[https://john.cs.olemiss.edu/~hcc/researchMethods/notes/ClassicParnas/ACMannotated/ClassicParnasRevisionAnnotated.pdf]

---

## 4. Design by Contract (1988 / 1992 / 1997)

### 4.1 Origin

Bertrand Meyer introduced Design by Contract (DbC) as a first-class language feature in the Eiffel
programming language. The foundational book is:

> Bertrand Meyer, *Object-Oriented Software Construction*, Prentice Hall, 1st ed. 1988, 2nd ed.
> 1997.

The widely-cited shorter article:

> Bertrand Meyer, "Applying 'Design by Contract'," *IEEE Computer*, vol. 25, no. 10, pp. 40–51,
> October 1992.
> [PDF at KTH: https://www.kth.se/social/files/59526bfb56be5b4f17000807/meyer-92-contracts.pdf]

A chapter-length treatment from Meyer's ETH Zurich publications:
[https://se.inf.ethz.ch/~meyer/publications/old/dbc_chapter.pdf]

### 4.2 The Three Contract Elements

**Precondition (`require` in Eiffel):** A boolean condition the *caller* must satisfy before
invoking the routine. If the precondition holds and the routine is invoked, the supplier guarantees
the postcondition. If the precondition does not hold, the behaviour of the routine is undefined —
the caller has broken the contract, and the supplier owes nothing.

**Postcondition (`ensure` in Eiffel):** A boolean condition the *supplier* guarantees upon return,
provided the precondition held on entry. The keyword `old` allows reference to the pre-call value
of expressions.

**Class Invariant (`invariant` in Eiffel):** A condition that must hold for every accessible
instance of the class at all externally visible moments — after construction, before and after every
public call. Invariants are automatically appended to every precondition and postcondition.

Eiffel syntax example (from official Eiffel documentation at eiffel.org):

```eiffel
class ACCOUNT
  feature
    balance: INTEGER
    minimum_balance: INTEGER = 1000

    deposit (sum: INTEGER)
      require
        non_negative: sum >= 0
      do
        balance := balance + sum
      ensure
        updated: balance = old balance + sum
        one_more: deposit_count = old deposit_count + 1
      end

  invariant
    consistent_balance: balance >= minimum_balance
end
```

[Source: https://archive.eiffel.com/doc/online/eiffel50/intro/language/tutorial-09.html]
[Source: https://www.eiffel.org/doc/eiffel/ET-_Design_by_Contract_(tm),_Assertions_and_Exceptions]

### 4.3 The Client-Supplier Metaphor

Meyer uses a legal contract metaphor explicitly. A software routine is a service; the class
providing it is the *supplier*; the calling code is the *client*. The contract specifies:

- What the client must guarantee before the call (precondition).
- What the supplier guarantees in return (postcondition).
- General conditions the supplier must maintain at all times (invariant).

If the precondition fails, the client is in breach — it called with invalid inputs. If the
postcondition fails when the precondition held, the supplier is in breach — a bug in the
implementation. This clean blame assignment is what makes DbC useful for testing: test failures
point unambiguously to either caller or callee.

### 4.4 DbC Maps Directly to Unit Test Design

The connection is structural:

| Contract element | Unit test role |
|---|---|
| Precondition | Test setup — arrange phase establishes state that satisfies pre |
| Postcondition | Test assertion — assert phase verifies post holds after call |
| Invariant | Assertion after every mutating call; also checked by constructor tests |
| `old` values | Before/after comparison in test assertions |

Without an explicit postcondition, a unit test is guessing what the function should produce. An
explicit postcondition makes the correct expected value derivable, not invented.

---

## 5. Defensive Programming vs. Design by Contract

These are philosophically opposed. Understanding the opposition is important because safety-critical
embedded systems often default to defensive programming without examining the trade-off.

**Defensive programming:** Every routine validates all inputs, regardless of what callers are
supposed to guarantee. The routine handles bad inputs gracefully (return error, clamp, substitute
default, log and continue). The reasoning: callers are unreliable, so suppliers must protect
themselves.

**Design by Contract:** Preconditions define what the caller must provide. If the precondition
holds, the supplier can proceed without redundant checks. If the precondition does not hold, the
caller has a bug — calling the routine with invalid input is a programming error, not a runtime
condition to handle gracefully.

Meyer's position (from the 1992 IEEE Computer article and the Eiffel documentation): defensive
programming blurs responsibility. If every routine checks everything, then when something goes
wrong, it is unclear who was responsible for what. DbC assigns each consistency condition to
exactly one party — either it is the caller's precondition, or it is the supplier's problem to
handle.

The ScienceDirect overview of DbC states this plainly: "Design by contract and defensive
programming are in some ways in opposition to one another since with DBC you set agreements between
collaborators and build programs on the premise that they will be upheld, whereas when programming
defensively you assume that your colleagues will break their agreements."
[https://www.sciencedirect.com/topics/computer-science/design-by-contract]

**Practical synthesis for safety-critical systems:** DO-178C and ISO 26262 context does not
eliminate this tension. A common pragmatic approach:

- Use DbC semantics in the interface specification (documented preconditions and postconditions).
- Apply defensive checks at *trust boundaries* (hardware interfaces, external inputs, IPC).
- Use assertions (not defensive recovery) for internal module interfaces.

The state-machine.com embedded DbC article (Quantum Leaps) makes this concrete for C/C++:
[https://www.state-machine.com/dbc]

---

## 6. Liskov Substitution Principle: Contracts and Subtyping (1994)

Barbara H. Liskov and Jeannette M. Wing, "A Behavioral Notion of Subtyping," *ACM Transactions on
Programming Languages and Systems (TOPLAS)*, vol. 16, no. 6, pp. 1811–1841, November 1994.
[ACM Digital Library: https://dl.acm.org/doi/10.1145/197320.197383]
[PDF at CMU: https://www.cs.cmu.edu/~wing/publications/LiskovWing94.pdf]

This paper formalizes what Liskov had introduced informally in her 1987 keynote. The formal
statement:

> "Let ϕ(x) be a property provable about objects x of type T. Then ϕ(y) should be true for
> objects y of type S where S is a subtype of T."

The practical interface-specification implication: a subtype's contract must be *compatible* with
its supertype's contract. Specifically:

- **Preconditions may only be weakened** in subtypes (a subtype cannot refuse inputs the supertype
  accepts).
- **Postconditions may only be strengthened** in subtypes (a subtype cannot deliver less than the
  supertype promises).
- **Invariants must be maintained** at least as strongly.

This rule — preconditions can only weaken, postconditions can only strengthen — is sometimes called
*contravariance of preconditions* and *covariance of postconditions*. It is a testable property of
interface specifications: reviewers can verify that overriding methods do not violate it.

Liskov and Wing's improvement over Meyer's earlier treatment was accounting for aliasing (when
multiple references point to the same object), which their "history constraint" formalizes.

The LSP is not just an OOP principle — it is a statement about what interface contracts mean across
a type hierarchy. Interfaces (in the Java/C# sense) are contracts that all implementors sign; the
LSP constrains what variations across those implementations are legitimate.

---

## 7. What Constitutes a Complete Interface Contract

This section synthesizes from multiple sources. Not every element applies to every function, but
completeness means having consciously decided each element.

### 7.1 Signature

The minimum: name, parameter types, return type. This is the syntactic contract and is what most
languages enforce at compile time. It is not sufficient.

### 7.2 Semantic Preconditions

What conditions must hold on entry? Examples:

- Value ranges: `index >= 0 && index < array.length`
- State conditions: object must be initialized, connection must be open
- Relationship conditions: `end >= start`
- Units of measure: a parameter named `altitude` with type `float` is ambiguous; the precondition
  should state the unit (meters, feet) and valid range

The Mars Climate Orbiter failure (1999) resulted from one module producing force in pound-force
units and another expecting newtons. The interface contract did not specify the unit. [Source:
Unified Code for Units of Measure background; confirmed in the units-of-measurement Springer paper
— see §8.]

### 7.3 Semantic Postconditions

What conditions hold on return (given preconditions were met)? Examples:

- Return value range or domain
- State changes: what fields or globals were modified?
- Idempotency: does calling twice produce the same result?
- `old` value relationships: `result == old_balance + amount`

### 7.4 Class / Object Invariants

What properties must hold before and after every public call? Examples:

- `balance >= minimum_balance`
- `size >= 0 && size <= capacity`
- Internal consistency conditions

### 7.5 Error Semantics

What happens on invalid input? This must be explicitly stated — it is part of the contract.

Options (these are design choices, not implementation details):

- **Exception:** throws `IllegalArgumentException` with message X
- **Return sentinel:** returns `null` / `-1` / `Optional.empty()` (with defined meaning)
- **Assertion / contract violation:** precondition documented; undefined behaviour in production
  (caller's bug, not handled)
- **Error code:** returns `STATUS_INVALID_PARAM` (common in C and embedded systems)

Leaving this unspecified makes the interface untestable — a test cannot distinguish "correct
behaviour on invalid input" from "bug" without knowing what was promised.

### 7.6 Nullability

Java's type system historically made nullability implicit. The `@Nullable` / `@NonNull` annotation
ecosystem (FindBugs/SpotBugs, Checker Framework, JetBrains annotations, JSpecify) makes
nullability an explicit part of the interface contract.

JSpecify (https://jspecify.dev/) is the current cross-tool standard, with contributions from
Google, JetBrains, Spring, and others. Spring Framework 7 migrated its entire codebase to JSpecify
annotations. [Source: DEV Community JSpecify article — see §8.]

Every parameter and return type should have explicit nullability in the interface contract:
- `@NonNull` — callers may not pass null; this function will never return null
- `@Nullable` — null is a valid value with defined meaning

### 7.7 Thread-Safety Guarantee

Thread safety is an interface property, not an implementation detail. Java's own documentation
practice (confirmed in MIT 6.031 notes) is: "When a data type in the Java library is thread-safe,
its documentation will explicitly state that fact." [Source: MIT 6.031 Thread Safety lecture —
see §8.]

Categories to document:

- **Immutable:** safe for concurrent access without synchronization
- **Thread-safe:** all methods are synchronized; safe for concurrent access
- **Conditionally thread-safe:** some methods are thread-safe, others are not (document which)
- **Not thread-safe:** caller must provide synchronization

Arrow ADBC documentation demonstrates a complete thread-safety contract: "Objects allow serialized
access from multiple threads: one thread may make a call, and once finished, another thread may
make a call. They do not allow concurrent access from multiple threads."
[https://arrow.apache.org/adbc/0.4.0/cpp/concurrency.html]

---

## 8. Language Support for Contracts

### 8.1 Native Language Support

**Eiffel:** DbC is a first-class language feature. `require`, `ensure`, `invariant` keywords.
Contracts are checked at runtime by default; can be disabled for release builds.
[https://www.eiffel.com/values/design-by-contract/]

**Ada 2012:** Introduced `Pre` and `Post` aspects for preconditions and postconditions, and `Type_Invariant`.
These are checked at runtime when assertions are enabled (`-gnata` in GNAT). Class-wide conditions
use `Pre'Class` and `Post'Class` aspects for inheritance-compatible contracts.
Ada Rationale for 2012: https://www.adacore.com/uploads/technical-papers/Ada2012_Rationale_Chp1_contracts_and_aspects.pdf
Ada Learn course: https://learn.adacore.com/courses/intro-to-ada/chapters/contracts.html

```ada
procedure Deposit (Account : in out Bank_Account; Amount : in Natural)
  with Pre  => Amount > 0,
       Post => Account.Balance = Account.Balance'Old + Amount;
```

**D:** Built-in contract programming via `in`, `out`, and `invariant` blocks. Precondition failure
indicates caller bug; postcondition failure indicates implementor bug. Contracts are compiled out
with the `-release` flag. [https://dlang.org/book/contracts.html]

```d
long square_root(long x)
in  { assert(x >= 0); }
out (result) { assert(result * result <= x); }
do  { return cast(long)sqrt(cast(double)x); }
```

### 8.2 Annotation-Based (JVM/CLR)

**Java assertions:** `assert condition : message;` — enabled with `-ea` JVM flag. Standard Java
mechanism for contract-style checks, but: not enforced in production by default; no tool support
for inheritance rules; no separation of preconditions vs. postconditions.

**JML (Java Modeling Language):** Gary T. Leavens, Albert L. Baker, Clyde Ruby. Iowa State
University, developed since 1999. Published in:
> G. T. Leavens et al., "Preliminary Design of JML," *ACM SIGSOFT Software Engineering Notes*,
> vol. 31, no. 3, 2006. https://dl.acm.org/doi/10.1145/1127878.1127884
> Full paper PDF: https://www.cs.ucf.edu/~leavens/JML/prelimdesign.pdf

JML specifications are written as structured Java comments (`//@ ...`). They support pre/post
conditions, invariants, assignable clauses (frame conditions), and model fields. JML is a
Behavioral Interface Specification Language (BISL) — it specifies observable behaviour, not
implementation.

```java
//@ requires amount > 0;
//@ ensures balance == \old(balance) + amount;
//@ assignable balance;
public void deposit(int amount) { ... }
```

JML is supported by OpenJML (static verification and runtime assertion checking):
https://www.openjml.org/about/

Formal specification with JML chapter (Chalmers University):
https://www.cse.chalmers.se/~ahrendt/papers/JML16chapter.pdf

**Microsoft .NET Code Contracts:** Released with .NET 4.0. Methods `Contract.Requires<T>()`,
`Contract.Ensures()`, `Contract.Invariant()` in the `System.Diagnostics.Contracts` namespace.
Supported static verification via a binary rewriter and static checker. Now archived — not
supported in .NET 5+. Nullable reference types (`string?` / `string`) are the current idiomatic
replacement for the nullability subset.
[https://learn.microsoft.com/en-us/dotnet/framework/debug-trace-profile/code-contracts]
[GitHub: https://github.com/microsoft/CodeContracts]

---

## 9. Formal and Semi-Formal Specification

### 9.1 When Formal Methods Are Warranted

Formal methods impose cost. Jackson's *Software Abstractions* (MIT Press, 2006, revised 2012) makes
the case for "lightweight" formal methods — not full proof, but structured model checking that
gives early feedback on design.

> Daniel Jackson, *Software Abstractions: Logic, Language, and Analysis*, MIT Press, 2006 (revised
> 2012). ISBN 9780262528900.
> https://mitpress.mit.edu/9780262101141/software-abstractions/

The VDM and Z literature notes that formal specification is most cost-effective at system
boundaries and for operations with complex state-change semantics. For routine CRUD-style
operations, informal or semi-formal specification may be sufficient.

ISO 26262 Part 6 Table 1 scales formality with ASIL:
- ASIL A/B: informal or semi-formal ("natural language + diagrams") is acceptable
- ASIL C: semi-formal recommended
- ASIL D: semi-formal highly recommended; formal methods for certain properties

### 9.2 Hoare Logic (Already Covered in §2)

The foundational framework. Every other approach either implements or approximates Hoare logic.

### 9.3 VDM (Vienna Development Method)

Originated at IBM Vienna Laboratory. VDM specifications explicitly separate pre- and postconditions
(involving only the before-state vs. the after-state respectively) and declare which state
components an operation reads and writes.

VDM and Z comparison: https://staff.itee.uq.edu.au/ianh/Papers/ndb.pdf

Industry use: TradeOne trading system (Japan) used VDM to reduce operating costs.

### 9.4 Z Notation

Based on set theory and first-order predicate calculus. Developed at Oxford. Widely used in UK
defence and finance sectors. Reference manual:
> J. M. Spivey, *The Z Notation: A Reference Manual*, 2nd ed., Prentice Hall, 1992.
> PDF: https://www.cs.umd.edu/~mvz/handouts/z-manual.pdf

Z is popular for critical systems. The B method (a descendant) was used for Paris Metro Line 14
and the Roissy CDG airport shuttle (full formal proof of the control software). [Source: Z/B
comparison papers — see above.]

### 9.5 Alloy

Daniel Jackson's *lightweight* formal method. Alloy is a declarative language based on relational
first-order logic; the Alloy Analyzer performs fully automated analysis (bounded model checking),
giving rapid feedback on structural invariants and reachability properties without manual proof.

Alloy tools and book: https://alloytools.org/book.html

Suitable for: checking interface invariants, finding counterexamples to design claims, verifying
data structure consistency. Not suitable for: full functional correctness proofs (that requires
tools like Isabelle, Coq, or Frama-C).

### 9.6 ACSL (ANSI/ISO C Specification Language)

Used with Frama-C for formal verification of C code. ACSL specifications are written as C
annotation comments. They support function contracts (pre/postconditions), loop invariants, and
assigns clauses (frame conditions).

Main reference: https://frama-c.com/acsl.html
ACSL specification PDF: https://frama-c.com/download/acsl.pdf

```c
/*@ requires \valid(a) && n > 0;
  @ ensures \result >= 0;
  @ assigns \nothing;
  @*/
int find_max(int *a, int n);
```

ACSL is the formal method of choice for automotive C code when DO-178C DAL A or ISO 26262 ASIL D
demands proof-level assurance. Fraunhofer's "ACSL by Example" document provides worked examples:
https://publica.fraunhofer.de/entities/publication/beb926ba-c3d6-4570-acc6-dd50da41843f

### 9.7 JML (Already Covered in §8.2)

JML occupies the semi-formal tier for Java. It is more expressive than Javadoc but lighter than
full Z or VDM. OpenJML supports both runtime checking (quasi-defensive, but contracts are the
document) and static verification.

---

## 10. Javadoc and Doxygen: When Documentation-as-Contract Works (and When It Does Not)

### What works

Oracle's Javadoc tool guide describes the API specification as a contract: "The API specification
for methods is a contract between a caller and an implementor."
[https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html]

Javadoc and Doxygen are effective for:
- Documenting parameter meaning, units, valid ranges (in prose)
- Documenting return value semantics
- Documenting `@throws` / `@exception` to specify error semantics
- Documenting thread-safety guarantees (by convention)

Stephen Colebourne's Javadoc coding standards (widely referenced in Java community) specify that
all public and protected methods should be documented with `@param`, `@return`, and `@throws`.
[https://blog.joda.org/2012/11/javadoc-coding-standards.html]

Atlassian's internal Javadoc standards require all methods declared in an interface to have doc
comments. [https://developer.atlassian.com/server/confluence/javadoc-standards/]

### Where it breaks down

Prose documentation is not machine-checkable. The following gaps exist when relying solely on
Javadoc/Doxygen:

1. **Unchecked contracts:** Nothing prevents an implementation from violating the documented
   precondition or postcondition. The contract is advisory, not enforced.
2. **Inheritance is not handled:** A Javadoc comment on a superclass method is not automatically
   a constraint on overriding methods. The LSP rules (§6) must be applied manually.
3. **No frame conditions:** There is no standard Javadoc mechanism to specify which fields a
   method modifies (the "assignable" clause of JML). Callers cannot know what side effects to
   expect.
4. **Units are stringly-typed:** A parameter documented as "altitude in meters" is enforced by
   convention only. Nothing in the type system or tool chain verifies it.
5. **Stale documentation:** Prose comments drift from implementation. JML annotations and ACSL
   contracts can be checked for consistency with the code.

The practical rule: Javadoc is the minimum bar. For interfaces where incorrect usage would cause
incorrect behaviour (not just degraded performance), supplement with machine-checkable contracts
(JML/OpenJML for Java, ACSL/Frama-C for C, Ada aspects for Ada).

---

## 11. Interface Specification for Different Paradigms

### 11.1 Object-Oriented

DbC fits OOP naturally. Invariants are class properties. The LSP constrains inheritance. Methods
carry pre/postconditions. The class itself is the supplier unit.

### 11.2 Procedural (C, embedded systems)

DbC applies directly: a C function has preconditions (valid pointer, value range), postconditions
(return value semantics, memory ownership, state changes), and module invariants (internal data
structure consistency). ACSL is the formal tool; defensive assertions (`assert()`) are the
lightweight tool.

The state-machine.com "Design by Contract for Embedded Software" article demonstrates C idioms.
[https://www.state-machine.com/dbc]

### 11.3 Functional

In purely functional code (no side effects), postconditions are purely about the return value.
Invariants are properties of the data types (enforced by construction). Preconditions remain
relevant for partial functions (e.g., `head` on an empty list). Property-based testing (QuickCheck,
Hypothesis) can be understood as automated precondition+postcondition verification over generated
inputs.

### 11.4 Interfaces (Java interface / C++ abstract class / Protocol)

An interface declaration is a collective contract that all implementors sign. The Javadoc on the
interface method is the normative contract. All implementing classes must satisfy it. This is where
the LSP is most practically important: if a concrete implementation strengthens a precondition or
weakens a postcondition, it violates the interface contract and will break callers that rely only on
the interface.

---

## 12. How Interface Specs Enable Independent Testing

This is the V-model payoff. The V-model exists to enable parallel development of code and test. The
left side produces specifications; the right side produces verification evidence. The linkage is
through the interface contract.

Given a complete interface contract (preconditions, postconditions, invariants, error semantics,
nullability, thread-safety), a test engineer can derive test cases without reading the
implementation:

- **Normal cases:** Choose inputs satisfying the precondition. Verify postconditions.
- **Boundary cases:** Choose inputs at the edges of the precondition domain.
- **Error cases:** Choose inputs that violate the precondition. Verify the documented error
  behaviour (exception, return code, etc.).
- **Invariant checks:** After mutating operations, verify invariants still hold.

This derivability is precisely what ASPICE SWE.3 BP.2 and SWE.4 require. SWE.3 BP.2 defines the
contract; SWE.4 derives verification criteria from it. If the contract is absent or incomplete,
test derivation becomes guesswork — and the assessment finding is a gap between SWE.3 and SWE.4.

An incomplete interface specification is therefore not just a documentation problem; it is a
structural gap in the V-model that breaks the left-right linkage.

---

## 13. Common Anti-Patterns

These are observed failure modes in interface specification practice. Examples are constructed for
illustration; the patterns are generalizations from the literature.

### AP-1: The Undefined Range

```java
/**
 * Sets the timeout.
 * @param ms timeout in milliseconds
 */
void setTimeout(int ms);
```

Problem: No precondition. Is `ms = 0` valid? Is `-1` valid (meaning infinite)? What happens with
`Integer.MAX_VALUE`? The caller cannot know without reading the implementation.

Better:
```java
/**
 * Sets the connection timeout.
 * @param ms timeout in milliseconds; must be in range [100, 30000].
 *           Use {@link #TIMEOUT_INFINITE} for no timeout.
 * @throws IllegalArgumentException if ms < 100 or ms > 30000
 */
void setTimeout(int ms);
```

### AP-2: The Unspecified Side Effect

```java
/**
 * Authenticates the user and returns a session token.
 */
String login(String username, String password);
```

Problem: Does this modify any state? Is it idempotent? Can it be called concurrently? None of this
is specified.

Better: Document what state changes occur (session created, audit log written), whether it is
thread-safe, and what the token's lifecycle is.

### AP-3: The Implicit Unit

A parameter `altitude` typed as `double` with no documented unit. Callers guess. This was the
cause of the Mars Climate Orbiter failure (NASA, 1999). [Source: units-of-measurement Springer
paper cited in §8.]

### AP-4: The Implementation-Leaking Interface

```java
/**
 * Returns the list of pending orders. The list is backed by the internal
 * ArrayList; modifications to it will modify the internal state.
 */
List<Order> getPendingOrders();
```

Problem: The interface specifies implementation details (ArrayList). This locks the implementation
and creates a coupling that Parnas's information-hiding principle prohibits.

Better: Specify whether the returned collection is mutable or immutable, whether it is a live view
or a snapshot — without naming the concrete type.

### AP-5: The Silent Null Return

```java
/**
 * Finds the user by ID.
 * @return the user, or null if not found
 */
User findById(long id);
// Better: document this explicitly, or use Optional<User>
```

This is acceptable only when nullability is explicitly documented. Returning null without
documenting it violates the nullability contract. [Source: JSpecify documentation — see §8.]

---

## 14. Practical Patterns for V-Model Compliance

### Pattern 1: Contract-First Interface Design

Write the interface contract (pre/postconditions, error semantics, invariants) before writing any
implementation. This is the design equivalent of TDD's test-first discipline. It forces the
designer to answer the questions a tester would ask, during design.

### Pattern 2: Machine-Checkable Annotations

Use JML annotations (Java), ACSL (C), Ada aspects, or D contracts where the criticality level
warrants it. This converts the contract from advisory prose to an enforceable specification that
tools can check.

### Pattern 3: Traceability Link from Contract to Test

Each postcondition is a test oracle. Each precondition domain is a test partitioning boundary.
Formally number the contract clauses; trace test cases to them. This provides the SWE.3-to-SWE.4
traceability that ASPICE requires.

### Pattern 4: Separate the Trust-Boundary Decision

Decide explicitly for each interface: is this an internal interface (DbC semantics — assert
preconditions, not handle them) or a trust-boundary interface (defensive — validate and return
error). Document the decision in the interface header.

### Pattern 5: Review Checklist for Interface Completeness

Before any implementation begins, review the interface contract against:

- [ ] All parameters have documented type, unit, range, nullability
- [ ] Return value has documented type, unit, range, nullability
- [ ] Error semantics are specified for all precondition violations
- [ ] Thread-safety guarantee is stated
- [ ] All state changes (side effects) are documented
- [ ] Invariants are stated for stateful objects
- [ ] No implementation details appear in the interface specification

---

## Sources

### Primary Sources (Foundational Papers and Books)

- C. A. R. Hoare, "An Axiomatic Basis for Computer Programming," *Communications of the ACM*,
  vol. 12, no. 10, pp. 576–580, 1969. https://dl.acm.org/doi/10.1145/363235.363259

- D. L. Parnas, "On the Criteria To Be Used in Decomposing Systems into Modules," *Communications
  of the ACM*, vol. 15, no. 12, pp. 1053–1058, 1972.
  https://dl.acm.org/doi/10.1145/361598.361623

- Bertrand Meyer, *Object-Oriented Software Construction*, Prentice Hall, 1st ed. 1988, 2nd ed.
  1997. (No free URL for the book; cited by title and publisher.)

- Bertrand Meyer, "Applying 'Design by Contract'," *IEEE Computer*, vol. 25, no. 10, pp. 40–51,
  1992. https://www.kth.se/social/files/59526bfb56be5b4f17000807/meyer-92-contracts.pdf

- Barbara H. Liskov and Jeannette M. Wing, "A Behavioral Notion of Subtyping," *ACM TOPLAS*,
  vol. 16, no. 6, pp. 1811–1841, 1994. https://dl.acm.org/doi/10.1145/197320.197383
  https://www.cs.cmu.edu/~wing/publications/LiskovWing94.pdf

- G. T. Leavens, A. L. Baker, C. Ruby, "Preliminary Design of JML," *ACM SIGSOFT Software
  Engineering Notes*, vol. 31, no. 3, 2006. https://dl.acm.org/doi/10.1145/1127878.1127884
  https://www.cs.ucf.edu/~leavens/JML/prelimdesign.pdf

- Daniel Jackson, *Software Abstractions: Logic, Language, and Analysis*, MIT Press, 2006 (revised
  2012). https://mitpress.mit.edu/9780262101141/software-abstractions/

- J. M. Spivey, *The Z Notation: A Reference Manual*, 2nd ed., Prentice Hall, 1992.
  https://www.cs.umd.edu/~mvz/handouts/z-manual.pdf

### Language Documentation

- Eiffel DbC tutorial: https://archive.eiffel.com/doc/online/eiffel50/intro/language/tutorial-09.html
- Eiffel DbC reference: https://www.eiffel.org/doc/eiffel/ET-_Design_by_Contract_(tm),_Assertions_and_Exceptions
- Ada 2012 Pre/Post aspects: https://learn.adacore.com/courses/intro-to-ada/chapters/contracts.html
- Ada 2012 Rationale: https://www.adacore.com/uploads/technical-papers/Ada2012_Rationale_Chp1_contracts_and_aspects.pdf
- D contract programming: https://dlang.org/book/contracts.html
- .NET Code Contracts (archived): https://learn.microsoft.com/en-us/dotnet/framework/debug-trace-profile/code-contracts
- .NET Code Contracts GitHub: https://github.com/microsoft/CodeContracts

### Tools and Formal Methods

- ACSL reference: https://frama-c.com/acsl.html
- ACSL specification PDF: https://frama-c.com/download/acsl.pdf
- ACSL by Example (Fraunhofer): https://publica.fraunhofer.de/entities/publication/beb926ba-c3d6-4570-acc6-dd50da41843f
- OpenJML: https://www.openjml.org/about/
- JML formal specification chapter (Chalmers): https://www.cse.chalmers.se/~ahrendt/papers/JML16chapter.pdf
- Alloy tools and book: https://alloytools.org/book.html
- Hoare logic lecture notes (CMU): https://www.cs.cmu.edu/~aldrich/courses/15-819O-13sp/resources/hoare-logic.pdf

### Standards and Industry Guidance

- ASPICE SWE.3 BP.2 overview: https://www.synopsys.com/blogs/chip-design/preparing-for-automotive-spice-assessment.html
- ASPICE SWE.3 LinkedIn article: https://www.linkedin.com/pulse/swe3-software-detailed-design-unit-construction-gamze-da%C4%9Flo%C4%9Flu-dwuqf
- DO-178C overview: https://en.wikipedia.org/wiki/DO-178C
- DO-178C traceability (Parasoft): https://www.parasoft.com/learning-center/do-178c/requirements-traceability/
- ISO 26262 Part 6 (EmbeddedInEmbedded): http://embeddedinembedded.blogspot.com/2017/11/iso-26262-part-6.html

### Practical and Community Sources

- Design by Contract for Embedded Software (Quantum Leaps): https://www.state-machine.com/dbc
- Javadoc tool guide (Oracle): https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html
- Javadoc coding standards (Colebourne): https://blog.joda.org/2012/11/javadoc-coding-standards.html
- Atlassian Javadoc standards: https://developer.atlassian.com/server/confluence/javadoc-standards/
- Information hiding (Wikipedia): https://en.wikipedia.org/wiki/Information_hiding
- JSpecify null safety: https://dev.to/headf1rst/solving-the-billion-dollar-mistake-modern-java-null-safety-with-jspecify-and-nullaway-2ie7
- Thread safety contract (MIT 6.031): https://web.mit.edu/6.031/www/fa17/classes/20-thread-safety/
- Arrow ADBC concurrency contract: https://arrow.apache.org/adbc/0.4.0/cpp/concurrency.html
- Parnas annotated papers (U. of Mississippi): https://john.cs.olemiss.edu/~hcc/researchMethods/notes/ClassicParnas/ACMannotated/ClassicParnasRevisionAnnotated.pdf
- Meyer DbC blog: https://bertrandmeyer.com/category/design-by-contract/
- Design by Contract (Wikipedia): https://en.wikipedia.org/wiki/Design_by_contract
- Liskov Substitution Principle (Wikipedia): https://en.wikipedia.org/wiki/Liskov_substitution_principle
- Behavioral subtyping (Wikipedia): https://en.wikipedia.org/wiki/Behavioral_subtyping
- Units of measurement in formal specifications (Springer): https://link.springer.com/article/10.1007/BF01211077
- Units in scientific component interfaces: https://damevski.github.io/files/dims.pdf

---

## Gaps and Caveats

1. **Eiffel syntax examples** are taken from the official Eiffel Software documentation (eiffel.org
   and archive.eiffel.com). They are accurate as of the documentation's publication; Eiffel syntax
   has not changed materially for DbC features.

2. **.NET Code Contracts** is archived. The search results confirm it is not supported in .NET 5+.
   Nullable reference types are the current idiomatic partial replacement, but they cover only
   nullability, not full pre/postcondition semantics. The gap for .NET DbC tooling is real and not
   resolved by any currently active library at the same level.

3. **ISO 26262 Part 6 notation table** is described through secondary sources (EmbeddedInEmbedded
   blog, IEEE Xplore abstract). The standard itself is paywalled. The three-tier informal /
   semi-formal / formal structure is confirmed across multiple secondary sources and is consistent
   with how practitioners describe the standard.

4. **ASPICE SWE.3 BP.2 wording** is from secondary sources (Synopsys blog, LinkedIn summary). The
   exact wording of the base practice may vary between ASPICE PAM versions. The substance — that
   interface specification is required for unit test derivation — is consistent across all secondary
   sources reviewed.

5. **Mars Climate Orbiter unit failure:** Widely cited in units-of-measurement literature. NASA
   Mishap Investigation Board Report is the primary source (1999) and is publicly available through
   NASA, though not searched directly in this session.
