# Clean Code, Scalable Software, and AI-Agent Coding Best Practices

Research compilation for the VModelWorkflow implementation skill.
Covers principles, patterns, and anti-patterns from multiple authoritative sources.

---

## Part 1: Clean Code Principles

### 1.1 SOLID Principles

The SOLID principles were introduced by Robert C. Martin and form the backbone of maintainable object-oriented design. They apply across languages (Java, C++, Python, TypeScript) and scale from single classes to entire subsystems.

**Single Responsibility Principle (SRP)**

> A class should have one, and only one, reason to change. -- Robert C. Martin

- Each class/module handles one specific concern.
- "Reason to change" means one stakeholder or business rule.
- Violations manifest as classes that grow endlessly because multiple concerns are entangled.
- Practical test: can you describe the class's purpose in one sentence without using "and"?

Example violation: a `ReportGenerator` class that fetches data, applies business rules, formats output, and sends email. Each of these is a separate responsibility.

**Open/Closed Principle (OCP)**

> Software entities should be open for extension but closed for modification.

- Add new behavior by adding new code (new classes, new implementations), not by editing existing working code.
- Achieved through abstractions: interfaces, abstract classes, strategy patterns.
- Practical test: can you add a new payment method / export format / validation rule without touching existing classes?

**Liskov Substitution Principle (LSP)**

> Subtypes must be substitutable for their base types without altering the correctness of the program.

- If `S` extends `T`, then objects of type `T` can be replaced with objects of type `S` without breaking behavior.
- Classic violation: `Square extends Rectangle` where `setWidth()` silently changes height.
- Practical test: does the subclass honor all contracts (preconditions, postconditions, invariants) of the parent?

**Interface Segregation Principle (ISP)**

> Clients should not be forced to depend upon interfaces they do not use.

- Prefer many small, focused interfaces over one large "god interface."
- Violations force implementations to provide stub methods for capabilities they don't have.
- In testing: violating ISP means test doubles must implement irrelevant methods, increasing boilerplate.
- Practical test: does any implementor throw `UnsupportedOperationException` or leave methods empty?

**Dependency Inversion Principle (DIP)**

> High-level modules should not depend on low-level modules. Both should depend on abstractions.

- Business logic depends on interfaces, not concrete implementations.
- Infrastructure (database, HTTP, filesystem) implements those interfaces.
- Enables testing business logic in isolation with fakes/mocks.
- Practical test: can you swap the database from PostgreSQL to SQLite for testing without changing business logic code?

Sources:
- [SOLID Principles -- Wikipedia](https://en.wikipedia.org/wiki/SOLID)
- [Baeldung: A Solid Guide to SOLID Principles](https://www.baeldung.com/solid-principles)
- [SOLID Principles Real World Examples -- HackerNoon](https://hackernoon.com/solid-principles-in-practice-with-python-and-uml-examples-in-2025)
- [GeeksforGeeks: SOLID Principles with Real Life Examples](https://www.geeksforgeeks.org/system-design/solid-principle-in-programming-understand-with-real-life-examples/)
- [Real Python: SOLID Design Principles](https://realpython.com/solid-principles-python/)

---

### 1.2 Clean Code Key Principles (Robert C. Martin)

From *Clean Code: A Handbook of Agile Software Craftsmanship* (2008) and its community summaries.

#### Meaningful Names

- Names should reveal intent. If a name requires a comment, it doesn't reveal its intent.
- Use pronounceable, searchable names.
- Avoid encodings (Hungarian notation, member prefixes like `m_`).
- Class names should be nouns (`Customer`, `WikiPage`, `AddressParser`), not verbs.
- Method names should be verbs (`postPayment`, `deletePage`, `save`).
- Don't be cute. Say what you mean. `kill()` not `whack()`. `abort()` not `eatMyShorts()`.
- One word per concept: don't use `fetch`, `retrieve`, and `get` for the same semantic operation across different classes.
- Use solution domain names (CS terms) when appropriate, problem domain names otherwise.
- Add meaningful context, but don't add gratuitous context (`GSDAccountAddress` is overkill if `Address` is unambiguous).

#### Small Functions

- **The first rule of functions is that they should be small. The second rule is that they should be smaller than that.** -- Robert C. Martin
- Functions should do one thing. They should do it well. They should do it only.
- Ideal function length: 5-15 lines. Certainly under 20.
- One level of abstraction per function. Don't mix high-level policy with low-level detail.
- Reading code top-to-bottom: each function should lead to the next level of abstraction (the "Stepdown Rule").
- Extract till you drop: if you can extract a meaningful sub-function, do it.

#### Function Arguments

- Ideal number of arguments: zero (niladic).
- One argument (monadic) is fine: `fileExists("myFile")`.
- Two arguments (dyadic) are acceptable but harder to understand.
- Three or more: consider wrapping in an object.
- Flag arguments (boolean) are ugly -- they announce the function does more than one thing.
- Output arguments are confusing. Prefer return values.

#### No Side Effects

- A function that promises to do one thing but secretly modifies shared state, global variables, or passed-in objects is lying.
- Side effects create temporal couplings (function A must be called before function B).
- Command-Query Separation: a function should either change state (command) or return information (query), not both.

#### Error Handling

- **Use exceptions, not return codes.** Return codes force callers into nested `if` checking immediately after the call.
- Write `try-catch-finally` first when writing code that could throw.
- Don't return `null`. Every null return is a potential `NullPointerException` waiting to happen. Return empty collections, Optional, or use the Null Object pattern.
- Don't pass `null`. Passing null into a function is worse than returning it.
- Error handling is one thing -- functions that handle errors should do nothing else.
- Create informative error messages: include the operation attempted, the context, and what failed.

#### General Rules (from the "Clean Code" summary)

- Follow the Boy Scout Rule: leave the code cleaner than you found it.
- Follow the Principle of Least Surprise: code should do what readers expect.
- Follow standard conventions of the project and language.
- Keep it simple stupid (KISS). Simpler is always better. Reduce complexity as much as possible.
- Be consistent. If you do something a certain way, do all similar things the same way.
- Prefer polymorphism to if/else or switch/case.
- Replace magic numbers with named constants.
- Be precise: when you make a decision in your code, make sure you make it precisely.
- Encapsulate conditionals: `if (shouldBeDeleted(timer))` is better than `if (timer.hasExpired() && !timer.isRecurrent())`.
- Avoid negative conditionals: `if (buffer.shouldCompact())` beats `if (!buffer.shouldNotCompact())`.
- Dead code (unused functions, unreachable branches, commented-out code) must be deleted, not commented out. Version control remembers.

Sources:
- [Summary of 'Clean Code' by Robert C. Martin -- GitHub Gist (wojteklu)](https://gist.github.com/wojteklu/73c6914cc446146b8b533c0988cf8d29)
- [Clean Code -- Robert C. Martin's Way -- DZone](https://dzone.com/articles/clean-code-robert-c-martins-way)
- [Clean Code Principles -- Codacy Blog](https://blog.codacy.com/clean-code-principles)
- [Master Clean Code Principles -- Pull Checklist](https://www.pullchecklist.com/posts/clean-code-principles)

---

### 1.3 DRY, KISS, YAGNI

These three meta-principles form a practical decision framework for everyday coding choices.

#### DRY (Don't Repeat Yourself)

> Every piece of knowledge must have a single, unambiguous, authoritative representation within a system. -- Andy Hunt & Dave Thomas, *The Pragmatic Programmer*

- Applies to code, configuration, documentation, and schema definitions.
- Duplication means that changes require updates in multiple places -- a guaranteed source of bugs.
- **But beware over-DRYing**: not all similar-looking code is the same concern. Two functions that happen to have the same implementation today may evolve differently tomorrow. Premature abstraction (DRYing too early) can create worse coupling than the duplication it eliminates.
- The "Rule of Three": tolerate duplication twice. On the third occurrence, abstract.
- DRY applies to knowledge, not just code. A business rule expressed in code AND in a comment is a DRY violation -- the comment will rot.

#### KISS (Keep It Simple, Stupid)

- The simplest solution that works is almost always the best.
- Complexity is the enemy of reliability.
- Over-engineering (adding layers, patterns, abstractions "just in case") violates KISS.
- Simple code is easier to read, test, debug, maintain, and for AI agents to understand.
- **Practical test**: would a new team member understand this code in under 5 minutes?

#### YAGNI (You Ain't Gonna Need It)

> Always implement things when you actually need them, never when you just foresee that you need them. -- Ron Jeffries (XP co-founder)

- Do not build infrastructure for hypothetical future requirements.
- Every unused abstraction is maintenance cost with zero return.
- This does NOT mean "don't think about architecture." It means implement the simplest thing that satisfies current, real requirements while keeping the design open for extension (OCP).
- YAGNI saves time (no wasted development), produces cleaner code (no unused complexity), and prevents wrong abstractions (you cannot predict the future accurately).

**The tension**: DRY, KISS, and YAGNI can conflict. Excessive DRY creates complex abstractions (violating KISS). YAGNI can lead to duplication (violating DRY). The skill is in balancing them based on actual, concrete needs -- not hypothetical ones.

Sources:
- [DRY, KISS & YAGNI Principles -- Henrique Domareski (Medium)](https://henriquesd.medium.com/dry-kiss-yagni-principles-1ce09d9c601f)
- [KISS, DRY, SOLID, YAGNI Guide -- HlfDev (Medium)](https://medium.com/@hlfdev/kiss-dry-solid-yagni-a-simple-guide-to-some-principles-of-software-engineering-and-clean-code-05e60233c79f)
- [4 Most Important Software Development Principles -- Matti Lehtinen](https://mattilehtinen.com/articles/4-most-important-software-development-principles-dry-yagni-kiss-and-sine/)
- [Clean Code Essentials: YAGNI, KISS, DRY -- DEV Community](https://dev.to/juniourrau/clean-code-essentials-yagni-kiss-and-dry-in-software-engineering-4i3j)

---

### 1.4 Code Smells and Refactoring Patterns

The term "code smell" was coined by Kent Beck and popularized by Martin Fowler in *Refactoring: Improving the Design of Existing Code* (1999, 2nd ed. 2018).

> A code smell is a surface indication that usually corresponds to a deeper problem in the system. -- Martin Fowler

Code smells are NOT bugs. The code works. But they indicate structural weaknesses that slow development and increase defect risk.

#### The Most Impactful Code Smells

**Bloaters** -- code that has grown too large:
- Long Method: functions that do too many things. Extract Method is the cure.
- Large Class: classes with too many responsibilities. Extract Class, Extract Subclass.
- Long Parameter List: more than 3 parameters. Introduce Parameter Object.
- Primitive Obsession: using primitives instead of small objects (e.g., `String zipCode` instead of `ZipCode` value object).
- Data Clumps: groups of data that always appear together. They should be their own object.

**Object-Orientation Abusers**:
- Switch Statements: often replaceable with polymorphism (Strategy or State pattern).
- Refused Bequest: subclass doesn't use inherited methods. Indicates wrong inheritance hierarchy.
- Alternative Classes with Different Interfaces: classes that do similar things but have different method signatures.

**Change Preventers** -- code that makes changes disproportionately expensive:
- Divergent Change: one class is changed for many different reasons (SRP violation).
- Shotgun Surgery: one change requires many small changes across many classes (opposite of Divergent Change).
- Parallel Inheritance Hierarchies: adding a subclass in one hierarchy forces adding a subclass in another.

**Dispensables** -- code that serves no purpose:
- Dead Code: unreachable or unused code. Delete it. VCS has your back.
- Speculative Generality: infrastructure built for "someday" (YAGNI violation).
- Duplicate Code: the root of many evils. Extract and reuse.
- Comments that explain what code does (instead of why): the code should explain itself.
- Data Class: classes with only fields and getters/setters, no behavior. Move behavior into the class.

**Couplers** -- excessive coupling between classes:
- Feature Envy: a method uses another class's data more than its own. Move it.
- Inappropriate Intimacy: classes that know too much about each other's internals.
- Middle Man: a class that only delegates. Is it adding value, or just indirection?
- Message Chains: `a.getB().getC().getD()` -- violates Law of Demeter.

#### Key Refactoring Patterns

Fowler's catalog (refactoring.com/catalog/) contains 60+ named refactorings. The most frequently useful:

| Refactoring | Purpose |
|---|---|
| Extract Method | Break long functions into focused, named steps |
| Extract Class | Split a class doing too many things |
| Move Method / Move Field | Put behavior where the data is |
| Rename Method / Variable | Make names reveal intent |
| Introduce Parameter Object | Replace long parameter lists |
| Replace Conditional with Polymorphism | Eliminate switch/if chains |
| Replace Temp with Query | Remove unnecessary local variables |
| Introduce Null Object | Eliminate null checks |
| Pull Up / Push Down Method | Fix inheritance hierarchy |
| Replace Magic Number with Symbolic Constant | Self-documenting values |

Sources:
- [Martin Fowler -- Refactoring (book page)](https://martinfowler.com/books/refactoring.html)
- [Catalog of Refactorings -- refactoring.com](https://refactoring.com/catalog/)
- [Martin Fowler -- bliki: Code Smell](https://martinfowler.com/bliki/CodeSmell.html)
- [Code Smells -- Coding Horror](https://blog.codinghorror.com/code-smells/)
- [Code Smells Catalog -- luzkan.github.io](https://luzkan.github.io/smells/)
- [Bad Smells in Code (Fowler/Beck PDF)](http://www.laputan.org/pub/patterns/fowler/smells.pdf)

---

### 1.5 Comments and Documentation

#### When to Comment

- **Why**, never **what**. The code says what it does. Comments explain why it does it that way.
- Business rule context: "// Per FAA AC 20-115D, all safety-critical paths require MC/DC coverage"
- Warnings about consequences: "// Do not remove this sleep -- the hardware needs 50ms settle time"
- Workarounds with references: "// Workaround for JDK-12345678, fixed in Java 19"
- TODO/FIXME with ticket references: "// TODO(JIRA-456): Replace with batch API when available"
- Legal/license headers when required.
- Public API documentation (Javadoc, docstrings) for libraries and framework extension points.

#### When NOT to Comment

- Restating the code: `i++; // increment i` -- this is noise.
- Explaining bad naming: rename the variable/method instead.
- Commented-out code: delete it. Version control exists.
- Journal comments (change logs in code): that's what `git log` is for.
- Closing brace comments: `} // end if` -- if you need these, your function is too long.
- Misleading comments: worse than no comments at all.

#### The Rot Problem

Comments do not compile. When code is refactored, comments are often left behind, becoming misleading. This is why self-documenting code (meaningful names, small functions, clear structure) is the first priority, and comments are the supplement for what code cannot express.

Sources:
- [Stack Overflow Blog: Best Practices for Writing Code Comments](https://stackoverflow.blog/2021/12/23/best-practices-for-writing-code-comments/)
- [Anthony Sciamanna: Self Documenting Code and Meaningful Comments](https://anthonysciamanna.com/2014/04/05/self-documenting-code-and-meaningful-comments.html)
- [Swimm: Comments in Code -- Best Practices and Mistakes to Avoid](https://swimm.io/learn/code-collaboration/comments-in-code-best-practices-and-mistakes-to-avoid)
- [DEV Community: Self-Documenting Code vs. Comments](https://dev.to/actocodes/self-documenting-code-vs-comments-lessons-from-maintaining-large-scale-codebases-52im)

---

### 1.6 Dependency Management and Injection

#### What Dependency Injection Is

Dependency Injection (DI) is a technique where an object receives its dependencies from the outside rather than creating them internally. It is the practical application of the Dependency Inversion Principle.

#### Three Forms of DI

1. **Constructor Injection** (preferred): dependencies are passed as constructor parameters. All required dependencies are explicit and available at construction time.
2. **Setter/Property Injection**: dependencies are set after construction via setter methods. Useful for optional dependencies.
3. **Interface Injection**: the dependency provides an injector method that will inject the dependency into any client passed to it. Rarely used in practice.

#### Why Constructor Injection is Preferred

- Makes dependencies explicit and visible in the constructor signature.
- Object is never in a partially-initialized state.
- Enables immutability (fields can be `final` / `readonly`).
- Makes excessive dependencies obvious (too many constructor params = too many responsibilities = SRP violation).

#### Benefits

- **Testability**: swap real implementations for test doubles (mocks, stubs, fakes).
- **Parallel development**: teams agree on interfaces, implement independently.
- **Flexibility**: swap implementations (e.g., in-memory cache vs Redis cache) via configuration, not code changes.
- **Readability**: dependencies are declared, not hidden inside method bodies.

#### DI Without Frameworks

DI does NOT require Spring, Guice, or Dagger. Manual "poor man's DI" works:

```java
// Production wiring
var repo = new PostgresUserRepository(dataSource);
var service = new UserService(repo);

// Test wiring
var repo = new InMemoryUserRepository();
var service = new UserService(repo);
```

Frameworks add convenience (auto-wiring, lifecycle management, scope management) but are not essential. For small systems, manual DI is simpler and more transparent.

#### Pitfalls

- DI containers can hide the dependency graph, making it hard to trace.
- Over-injection: injecting everything leads to classes that are bags of dependencies with no cohesion.
- Service Locator anti-pattern: hiding dependencies behind a global lookup defeats the purpose of explicit dependency declaration.

Sources:
- [Stackify: Design Patterns Explained -- Dependency Injection](https://stackify.com/dependency-injection/)
- [Wikipedia: Dependency Injection](https://en.wikipedia.org/wiki/Dependency_injection)
- [Java Design Patterns: Dependency Injection Pattern in Java](https://java-design-patterns.com/patterns/dependency-injection/)
- [GeeksforGeeks: Dependency Injection Design Pattern](https://www.geeksforgeeks.org/system-design/dependency-injectiondi-design-pattern/)

---

### 1.7 Immutability and Functional Patterns

#### Why Immutability Matters

An immutable object's state cannot be modified after creation. Instead of changing existing data, you create new instances with the desired values.

**Benefits**:
- **Thread safety**: immutable objects can be shared across threads without locks, mutexes, or synchronization. No race conditions are possible.
- **Predictability**: no surprise mutations. What you create is what you get, forever.
- **Easier debugging**: if a value is wrong, it was wrong at creation time. No need to trace mutation history.
- **Safe sharing**: pass immutable objects freely without defensive copying.
- **Hashability**: immutable objects make reliable hash map keys.

**In practice (Java 17+)**:
- Use `record` types for value objects (immutable by default).
- Use `List.of()`, `Map.of()`, `Set.of()` for immutable collections.
- Declare fields `final` where possible.
- Return unmodifiable views from getters.

**In practice (general)**:
- Prefer `const` / `final` / `readonly` declarations.
- Use builder patterns for complex immutable objects.
- Structural sharing (as in persistent data structures) minimizes the copy overhead.

#### Functional Patterns Worth Adopting in OO Code

You do not need a pure functional language to benefit from functional thinking:

- **Pure functions**: given the same input, always return the same output, no side effects. Trivially testable.
- **Map/Filter/Reduce**: prefer declarative collection transformations over manual loops. More concise, less error-prone.
- **Optional/Maybe**: encode the possibility of absence in the type system instead of using null.
- **Function composition**: build complex behavior by composing simple, tested functions.
- **Immutable data + transformation pipelines**: instead of mutating state in-place, transform data through a pipeline of pure functions. Each step is independently testable.

#### When NOT to Use Immutability

- Hot loops where allocation pressure matters (profiler-driven decision, not premature optimization).
- Large data structures where structural sharing isn't available and full copies are too expensive.
- Frameworks that require mutability (some ORMs, serialization frameworks).

Sources:
- [Wikipedia: Immutable Object](https://en.wikipedia.org/wiki/Immutable_object)
- [Medium: Functional Programming and Immutable Data Structures](https://medium.com/@luizgabriel.info/functional-programming-and-immutable-data-structures-03e2b87e82cc)
- [O'Reilly: A Functional Approach to Java -- Ch4: Immutability](https://www.oreilly.com/library/view/a-functional-approach/9781098109912/ch04.html)
- [Belief Driven Design: Functional Programming With Java -- Immutability](https://belief-driven-design.com/functional-programming-with-java-immutability-ae3372311b9/)

---

## Part 2: Scalable Software Patterns

### 2.1 Separation of Concerns

The principle that each section of a program should address a single concern -- a specific piece of functionality or domain logic -- and should not be entangled with unrelated concerns.

**At the function level**: each function does one thing (SRP for functions).
**At the class level**: each class encapsulates one concept (SRP for classes).
**At the module/package level**: each module handles one bounded context.
**At the system level**: layers and services separate infrastructure from domain logic.

Separation of concerns is the foundation that all architectural patterns build on. Without it, you get the Big Ball of Mud -- the most common architecture in practice (and the worst).

---

### 2.2 Layered, Hexagonal, and Clean Architecture

These architectures share the same fundamental insight: **business logic must not depend on infrastructure**. The differences are mainly in terminology and how strictly they enforce boundaries.

#### Traditional Layered Architecture

```
Presentation -> Application -> Domain -> Infrastructure
```

- Each layer can only call the layer directly below it.
- Simple and well-understood.
- **Problem**: the dependency arrows point downward, meaning domain depends on infrastructure. This makes testing domain logic hard because it drags in database/HTTP dependencies.

#### Hexagonal Architecture (Ports and Adapters)

Proposed by Alistair Cockburn (2005).

```
                    [Adapter: REST API]
                          |
                       [Port: Input]
                          |
              +-----------+-----------+
              |     APPLICATION       |
              |       CORE            |
              |  (business logic)     |
              +-----------+-----------+
                          |
                       [Port: Output]
                          |
                    [Adapter: PostgreSQL]
```

- **Ports**: interfaces defined by the application core. Input ports (what the application offers) and output ports (what the application needs).
- **Adapters**: concrete implementations of ports. REST controller adapts HTTP to input port. PostgreSQL repository adapts output port to SQL.
- **The core knows nothing about adapters.** It only knows its own ports (interfaces).
- Swapping adapters (e.g., PostgreSQL -> MongoDB, REST -> gRPC) requires zero changes to business logic.

#### Clean Architecture (Robert C. Martin, 2012)

```
    Frameworks & Drivers (outermost)
        Interface Adapters
            Application Business Rules (Use Cases)
                Enterprise Business Rules (Entities) (innermost)
```

- **The Dependency Rule**: source code dependencies must point inward only. Inner layers know nothing about outer layers.
- **Entities**: core business objects and rules. No framework dependencies.
- **Use Cases**: application-specific business rules. Orchestrate entities.
- **Interface Adapters**: convert data between use cases and external formats (controllers, presenters, gateways).
- **Frameworks & Drivers**: the outermost layer. Web framework, database, UI. This is where the "details" live.

#### Practical Guidance

- For most applications, Hexagonal Architecture provides the right balance of structure and simplicity.
- Clean Architecture's four concentric layers are valuable for large systems with complex domain logic.
- The key insight across all three: **invert the dependency direction** so that business logic is at the center, infrastructure at the edges.
- Start simple. You can always add layers. You can rarely remove them.

Sources:
- [Uncle Bob: The Clean Architecture (blog post)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [DEV Community: Hexagonal Architecture and Clean Architecture with Examples](https://dev.to/dyarleniber/hexagonal-architecture-and-clean-architecture-with-examples-48oi)
- [Roman Glushach: Understanding Hexagonal, Clean, Onion, and Traditional Layered Architectures](https://romanglushach.medium.com/understanding-hexagonal-clean-onion-and-traditional-layered-architectures-a-deep-dive-c0f93b8a1b96)
- [Naresh IT: Clean Architecture & Hexagonal Patterns in Java (2025 Guide)](https://nareshit.com/blogs/clean-architecture-and-hexagonal-patterns-in-java)

---

### 2.3 Interface-Driven Design

Interfaces (or abstract types/protocols/traits depending on language) are the primary tool for achieving loose coupling.

**Principles**:
- Program to an interface, not an implementation (GoF, 1994).
- Define interfaces from the consumer's perspective, not the provider's. What does the caller need?
- Keep interfaces small and focused (ISP).
- Use interfaces to define boundaries between layers (ports in hexagonal architecture).

**When to use interfaces**:
- At architectural boundaries (between layers, between modules).
- When there will be multiple implementations (now or plausibly in the future).
- When you need test doubles.
- When crossing team boundaries.

**When NOT to use interfaces**:
- For every single class (over-engineering). If a class has exactly one implementation and no testing need for a double, an interface adds ceremony without value.
- For data/value objects -- these are concrete by nature.
- Premature abstraction: extract an interface when you have a second consumer or implementation, not before (YAGNI).

---

### 2.4 Testability as a Design Constraint

> Testability goes hand in hand with classical good design. Carefully considering how to test code in isolation is a tool that helps arrive at classic design qualities of cohesion, coupling, and separation of concerns. -- Microsoft, Patterns in Practice

**If code is hard to test, the design is wrong.** This is the single most actionable heuristic in software design.

**Testability signals**:
- Easy to instantiate in isolation = good cohesion, few dependencies.
- Easy to inject test data = proper use of DI.
- Deterministic results = no hidden state, no side effects.
- Small test surface = small, focused functions.

**Testability anti-signals** (code smells found through testing):
- Excessive setup / mocking = too many dependencies (SRP violation).
- Tests break when internal details change = testing implementation instead of behavior.
- Can't test without a running database / network = missing abstraction boundary.
- Tests are slow = coupling to I/O that should be behind an interface.
- Tests are flaky = hidden shared state, temporal coupling, or non-determinism.

**Design for testability practices**:
- Constructor injection for all dependencies.
- Pure functions where possible (easiest to test).
- Separate I/O from computation (Functional Core, Imperative Shell pattern).
- Small, focused classes with single responsibilities.
- Avoid static methods that carry state or perform I/O.
- Avoid Singletons (global state that persists across tests).

Sources:
- [Microsoft Learn: Patterns in Practice -- Design for Testability](https://learn.microsoft.com/en-us/archive/msdn-magazine/2008/december/patterns-in-practice-design-for-testability)
- [InfoQ: Design for Testability -- The True Story](https://www.infoq.com/articles/Testability/)
- [GeeksforGeeks: Design for Testability in Software Testing](https://www.geeksforgeeks.org/design-for-testability-dft-in-software-testing/)
- [Jennifer Plusplus: Designing for Testability](https://jenniferplusplus.com/designing-for-testability/)

---

### 2.5 Cohesion and Coupling

These are the two most fundamental metrics of software module quality.

#### Cohesion (internal measure)

How closely related are the elements within a module?

**Cohesion spectrum (worst to best)**:
1. **Coincidental**: elements are in the same module by accident.
2. **Logical**: elements are grouped by type (all validators, all parsers) rather than by feature.
3. **Temporal**: elements are grouped because they execute at the same time.
4. **Procedural**: elements are grouped because they follow a specific sequence.
5. **Communicational**: elements operate on the same data.
6. **Sequential**: output of one element is input to the next.
7. **Functional** (best): every element contributes to a single, well-defined task.

**Practical test**: if you had to split the class, would there be a natural seam? If yes, it has mixed responsibilities.

#### Coupling (external measure)

How much does one module depend on another?

**Coupling spectrum (worst to best)**:
1. **Content**: one module modifies another's internal data directly.
2. **Common**: modules share global data.
3. **Control**: one module controls another's logic via flags/parameters.
4. **Stamp**: modules share a data structure but each uses only part of it.
5. **Data** (best): modules communicate only through parameters with simple data.

**Connascence** (a more granular coupling model by Meilir Page-Jones):
- Connascence of Name (weakest): two components must agree on a name.
- Connascence of Type: must agree on a type.
- Connascence of Meaning: must agree on the interpretation of a value.
- Connascence of Position: must agree on ordering.
- Connascence of Algorithm: must agree on an algorithm.
- Connascence of Execution (strongest): must agree on execution order.

Rule of thumb: connascence should decrease as the distance between components increases.

**Goal**: HIGH cohesion within modules, LOW coupling between them.

Sources:
- [GeeksforGeeks: Coupling and Cohesion -- Software Engineering](https://www.geeksforgeeks.org/software-engineering/software-engineering-coupling-and-cohesion/)
- [Wikipedia: Coupling](https://en.wikipedia.org/wiki/Coupling_(computer_programming))
- [Wikipedia: Cohesion](https://en.wikipedia.org/wiki/Cohesion_(computer_science))
- [Codesmells Substack: Understanding Cohesion, Coupling and Connascence](https://codesmells.substack.com/p/understanding-cohesion-coupling-and)
- [Baeldung: Difference Between Cohesion and Coupling](https://www.baeldung.com/cs/cohesion-vs-coupling)

---

### 2.6 Design Patterns That Actually Matter

The Gang of Four (GoF) cataloged 23 patterns in 1994. Not all are equally useful in modern practice. Here are the ones that consistently solve real problems.

#### High-Value Patterns

**Strategy Pattern**
- Encapsulate a family of algorithms behind an interface. Switch implementations at runtime.
- Use when: you have multiple ways to do the same thing (sorting, validation, pricing, formatting).
- Modern: often replaced by function/lambda parameters in languages that support first-class functions. The principle remains the same.

**Factory Method / Abstract Factory**
- Create objects without specifying the exact class.
- Use when: the specific implementation depends on configuration, context, or runtime decisions.
- Essential for DI without a framework.

**Observer / Publish-Subscribe**
- One-to-many dependency: when one object changes, all dependents are notified.
- Use when: decoupling event producers from event consumers.
- Modern: event buses, reactive streams (RxJava, Project Reactor), message queues.

**Adapter**
- Convert one interface to another that clients expect.
- Use when: integrating with external APIs, legacy code, or libraries with incompatible interfaces.
- The workhorse of Hexagonal Architecture (adapters implement ports).

**Decorator**
- Add behavior to an object dynamically without changing its interface.
- Use when: cross-cutting concerns (logging, caching, retry, authentication) need to wrap existing behavior.
- Modern: middleware patterns in web frameworks are essentially Decorator chains.

**Template Method**
- Define the skeleton of an algorithm in a base class, let subclasses override specific steps.
- Use when: you have a fixed algorithm structure with variable steps.
- Caution: inheritance-based; prefer composition (Strategy) when possible.

**Builder**
- Construct complex objects step by step.
- Use when: objects have many optional parameters or immutable objects need multi-step construction.
- Ubiquitous in modern Java (Lombok `@Builder`, records with builders, `StringBuilder`).

**Null Object**
- Provide a do-nothing implementation instead of null checks.
- Use when: a default behavior is appropriate when no specific implementation is available.

#### Patterns to Use Sparingly

- **Singleton**: global state in disguise. Makes testing hard. Use DI-managed scoping instead.
- **Abstract Factory**: often over-engineered. Only use when you genuinely need families of related objects.
- **Visitor**: powerful but complex. Only when you need double dispatch over a stable type hierarchy.
- **Mediator**: can become a god object if not carefully scoped.

#### Anti-Patterns to Avoid

- **Service Locator**: hides dependencies. Use DI instead.
- **God Object / God Class**: a single class that knows and does everything.
- **Anemic Domain Model**: entities with only data, all behavior in service classes. Violates OO principles.
- **Spaghetti Code**: no discernible structure. Usually the result of no architecture at all.
- **Golden Hammer**: applying one pattern/technology to every problem.
- **Lava Flow**: dead code and design remnants left in the codebase from past iterations.

Sources:
- [DigitalOcean: Gang of 4 Design Patterns Explained](https://www.digitalocean.com/community/tutorials/gangs-of-four-gof-design-patterns)
- [GeeksforGeeks: Gang of Four Design Patterns](https://www.geeksforgeeks.org/system-design/gang-of-four-gof-design-patterns/)
- [Wikipedia: Design Patterns](https://en.wikipedia.org/wiki/Design_Patterns)
- [Spring Framework Guru: Gang of Four Design Patterns](https://springframework.guru/gang-of-four-design-patterns/)

---

## Part 3: AI Agent-Specific Coding Practices

### 3.1 The State of AI-Generated Code (2025-2026 Data)

The evidence is now substantial and sobering:

- **AI-created PRs had 75% more logic and correctness errors** than human-written ones, including dependency and configuration errors that look reasonable but fail at runtime. (Stack Overflow Blog, 2026)
- **AI generates 1.7x as many bugs as humans** across 470 studied GitHub repositories. Critical and major issues were 1.3-1.7x more frequent. Business logic errors appeared more than twice as often. (Stack Overflow Blog, 2026)
- **Google's 2025 DORA Report**: 90% increase in AI adoption correlated with a 9% climb in bug rates, 91% increase in code review time, and 154% increase in PR size.
- **Developer trust is declining**: Stack Overflow's 2025 Developer Survey found nearly half of developers do not trust AI output accuracy, and only 3% report high trust.
- **Security**: AI-generated code included vulnerabilities like improper password handling and insecure object references at a 1.5-2x greater rate than human code. Nearly half of automatically generated code can contain vulnerabilities.

The pattern is clear: AI agents are fast but careless. They produce plausible-looking code that passes cursory review but harbors subtle logic errors, security vulnerabilities, and architectural problems. This makes rigorous review and testing non-negotiable.

Sources:
- [Stack Overflow Blog: Are bugs and incidents inevitable with AI coding agents?](https://stackoverflow.blog/2026/01/28/are-bugs-and-incidents-inevitable-with-ai-coding-agents/)
- [Stack Overflow Blog: AI can 10x developers...in creating tech debt](https://stackoverflow.blog/2026/01/23/ai-can-10x-developers-in-creating-tech-debt/)
- [Stack Overflow Blog: Code smells for AI agents](https://stackoverflow.blog/2026/02/04/code-smells-for-ai-agents-q-and-a-with-eno-reyes-of-factory/)
- [Vocal Media: 8 AI Code Generation Mistakes Devs Must Fix](https://vocal.media/futurism/8-ai-code-generation-mistakes-devs-must-fix-to-win-2026)

---

### 3.2 Common Mistakes AI Agents Make

Based on research and accumulated industry experience:

#### Logic and Correctness Errors

- **Plausible but wrong**: AI generates code that looks correct syntactically but has subtle logic errors -- off-by-one, incorrect boundary conditions, wrong comparison operators.
- **Untested edge cases**: AI typically handles the happy path well but misses null inputs, empty collections, concurrent access, integer overflow, and boundary conditions.
- **Hallucinated APIs**: AI invents function signatures, library methods, or configuration options that do not exist. This is especially common with less popular libraries.
- **Stale knowledge**: AI may use deprecated APIs, removed features, or outdated patterns from its training data.

#### Architectural Problems

- **No architectural awareness**: without explicit constraints, AI defaults to common but often inefficient patterns. It may introduce unnecessary microservices, wrong abstraction layers, or over-engineered solutions.
- **Copy-paste patterns**: AI repeats patterns it has seen frequently, even when they don't fit the context. It may apply a Spring Boot pattern in a plain Java project or an Express.js pattern in a Fastify project.
- **Inconsistent style**: AI-generated code may be internally consistent but clash with the existing codebase's conventions, naming, and architecture.
- **Over-abstraction or under-abstraction**: AI may generate deeply nested abstractions for simple tasks, or flat procedural code for complex domains.

#### Testing Failures

- **Testing its own assumptions**: when asked to write tests, AI often tests what its implementation does rather than what it should do. The tests pass, but they're tautological.
- **Missing edge cases**: AI-generated tests tend to cover the happy path and miss the failure modes that actually cause production bugs.
- **Brittle tests**: AI often tests implementation details (specific method calls, internal state) rather than behavior, making tests break on any refactoring.
- **Over-mocking**: AI loves mocks. It will mock everything, creating tests that verify mock behavior rather than actual business logic.

#### Security and Configuration

- **Hardcoded secrets**: AI may embed API keys, passwords, or tokens directly in code.
- **Insecure defaults**: AI may skip input validation, use weak encryption, or expose debugging endpoints.
- **Dependency confusion**: AI may suggest packages with similar names to legitimate ones, or outdated versions with known vulnerabilities.
- **Missing error handling**: AI often generates the happy path and leaves exception handling as an afterthought or omits it entirely.

Sources:
- [Nimbalyst: Coding with AI Agents -- Best Practices for 2026](https://nimbalyst.com/blog/coding-with-ai-agents-best-practices-2026/)
- [DEV Community: AI Coding Best Practices in 2025](https://dev.to/ranndy360/ai-coding-best-practices-in-2025-4eel)
- [arxiv: A Survey of Bugs in AI-Generated Code](https://arxiv.org/html/2512.05239v1)
- [Ivan Turkovic: Almost Solved Is the Most Dangerous Phase](https://www.ivanturkovic.com/2026/03/31/ai-coding-almost-solved-most-dangerous-phase/)

---

### 3.3 Best Practices for AI-Assisted Development

#### Specification-First Approach

- **Write a clear spec before asking the agent to code.** Include goals, acceptance criteria, technical constraints, implementation notes, and non-functional requirements.
- **Define the interface contract first.** Give the agent the function signatures, input/output types, and expected behavior. Then let it implement.
- **Test-first (DRTDD/TDD)**: have the agent generate tests from acceptance criteria BEFORE implementation. Then implement until tests pass. This inversion works remarkably well with agents.

#### Review Every Line

- **Never merge AI-generated code without reviewing it.** Treat every AI PR like a PR from a talented but careless junior developer.
- **Focus on logic correctness**: AI gets syntax right but logic wrong. Walk through the code mentally with specific inputs.
- **Check error handling**: AI often omits or under-implements error paths.
- **Verify API usage**: confirm that the APIs, methods, and configurations used actually exist in the project's dependency versions.
- **Check for security issues**: input validation, authentication checks, authorization boundaries.

#### Incremental Generation

- **Small tasks, not big features.** Generate one function, one class, one test at a time. Verify each before proceeding.
- **Build complexity incrementally.** Don't ask the agent to handle multiple abstraction layers simultaneously.
- **One concern per prompt.** Mixing implementation, testing, documentation, and refactoring in one prompt degrades quality on all of them.

#### Context Management

- **Provide relevant context, not everything.** The agent needs the interface it's implementing, the types it's using, and the conventions it should follow. It does not need the entire codebase.
- **Keep project conventions documented.** CLAUDE.md, CONVENTIONS.md, or equivalent. The agent reads these to maintain consistency.
- **Reference existing code as examples.** "Follow the pattern in UserService.java" is more effective than describing the pattern in prose.

#### Coding Standards for AI Agents

From Stack Overflow's 2026 guidance on building shared coding guidelines:

- **Document architectural decisions.** AI agents cannot infer architecture from code alone. ADRs (Architecture Decision Records) and explicit layering documentation help agents stay on track.
- **Enforce conventions via linters and formatters, not comments.** Agents will follow automated enforcement more reliably than written instructions.
- **Keep files focused and small.** AI agents work better with files under 300 lines. Large files degrade understanding.
- **Use descriptive, consistent naming.** AI agents use names as primary context signals.

Sources:
- [Google Cloud Blog: Five Best Practices for Using AI Coding Assistants](https://cloud.google.com/blog/topics/developers-practitioners/five-best-practices-for-using-ai-coding-assistants)
- [Zencoder: How to Use AI in Coding -- 12 Best Practices in 2026](https://zencoder.ai/blog/how-to-use-ai-in-coding)
- [Axur Engineering: Best Practices for AI-Assisted Coding](https://engineering.axur.com/2025/05/09/best-practices-for-ai-assisted-coding.html)
- [Stack Overflow Blog: Building Shared Coding Guidelines for AI (and People Too)](https://stackoverflow.blog/2026/03/26/coding-guidelines-for-ai-agents-and-people-too/)
- [Codescene: Agentic AI Coding -- Best Practice Patterns for Speed with Quality](https://codescene.com/blog/agentic-ai-coding-best-practice-patterns-for-speed-with-quality)

---

### 3.4 Structuring Code So AI Agents Can Maintain It

Code written for human maintenance and code written for AI-assisted maintenance share the same qualities -- but AI amplifies the consequences of both good and bad structure.

#### File Organization

- **One concept per file.** One class, one interface, one enum. Not three related classes in one file.
- **Small files.** Target under 200-300 lines. AI context windows are large but accuracy degrades with size.
- **Flat-ish directory structure.** Deep nesting obscures location. Prefer `domain/user/UserService.java` over `com/company/project/internal/service/impl/v2/UserServiceImpl.java`.
- **Colocate related files.** Tests next to source. Interfaces next to implementations. This reduces the context the agent needs to load.

#### Function Design for AI Maintainability

- **Small functions (5-20 lines).** AI generates better modifications to small, focused functions.
- **Explicit input/output types.** AI relies heavily on type signatures to understand behavior.
- **No hidden state.** Functions that read from or write to global/static state are nearly impossible for AI to reason about correctly.
- **Minimize control flow complexity.** Deeply nested if/else, multiple loop levels, and complex boolean expressions increase AI error rates significantly.

#### Naming for AI Context

- **Names are the primary context signal for AI.** `calculateMonthlySalesTax(order)` gives the agent everything it needs. `calc(o)` gives it nothing.
- **Consistent vocabulary.** If you call it `User` in one place, don't call it `Account`, `Member`, and `Person` in others (unless they are genuinely different concepts).
- **Method names should describe behavior, not implementation.** `persistUser()` is better than `writeToPostgres()` for AI comprehension.

#### Documentation for AI Agents

- **README files per module/package.** AI loads these for context. Keep them current.
- **Interface contracts (Javadoc/docstrings on interfaces).** AI uses these to understand expected behavior when implementing or modifying.
- **Architecture documentation.** A single diagram showing the layers and their allowed dependencies helps AI stay within boundaries.
- **Keep docs close to code.** AI that has to search across distant directories for relevant documentation will more often miss it.

Sources:
- [Developer Toolkit: Project Structure for AI (Cursor & Claude Code)](https://developertoolkit.ai/en/shared-workflows/context-management/file-organization/)
- [Of Ash and Fire: AI Coding Standards & Guidelines](https://www.ofashandfire.com/blog/ai-coding-standards-guidelines)
- [Medium: Coding Standards for AI Agents (Chris Force)](https://medium.com/@christianforce/coding-standards-for-ai-agents-cb5c80696f72)
- [FreeCodeCamp: How to Improve and Restructure Your Codebase with AI Tools](https://www.freecodecamp.org/news/improve-and-restructure-codebase-with-ai-tools/)

---

### 3.5 Anti-Patterns in AI-Generated Code

These are patterns that appear frequently in AI output and should be flagged during review.

| Anti-Pattern | Description | Remedy |
|---|---|---|
| **Plausible Hallucination** | Code uses APIs, methods, or config options that don't exist | Verify against actual library documentation and project dependencies |
| **Tautological Tests** | Tests that verify the implementation does what the implementation does, not what it should do | Write tests from requirements/acceptance criteria, not from implementation |
| **Over-Mocking** | Every dependency is mocked, testing mock wiring rather than behavior | Use fakes for complex dependencies, real objects for simple ones |
| **Shotgun Abstraction** | Premature interfaces, factories, and indirection for simple cases | Apply YAGNI. Abstract when a second consumer appears |
| **Style Inconsistency** | AI follows its training patterns rather than project conventions | Provide linter configs, example code, and conventions documentation |
| **Copy-Paste Scaling** | AI duplicates code rather than extracting reusable components | Explicitly request refactoring after initial implementation |
| **Happy Path Only** | Error handling, edge cases, and failure modes are missing or shallow | Require explicit error handling in the spec. Review for missing catch blocks |
| **Dependency Confusion** | AI suggests wrong package names, outdated versions, or unnecessary dependencies | Verify every added dependency against the project's actual dependency management |
| **Excessive Comments** | AI adds comments explaining what every line does (trained on tutorial code) | Strip comments that restate the code. Keep only "why" comments |
| **God Method** | AI generates one long method instead of composing smaller functions | Explicitly constrain function length in instructions |

---

### 3.6 AI Agents: Existing Codebases vs. Greenfield

The approach differs significantly.

#### Greenfield Projects

- AI agents excel at scaffolding: project structure, boilerplate, initial patterns.
- Risk: AI will over-scaffold. It adds every feature it has seen in tutorials (CORS, logging, metrics, health checks) whether you need them or not. YAGNI applies.
- Best practice: start with the minimal structure. Add infrastructure as requirements demand it.
- Define conventions BEFORE the agent starts writing. Once AI establishes a pattern in a greenfield project, it will propagate it everywhere.

#### Existing Codebases

- AI agents struggle more with existing code. They must understand context, conventions, and constraints that are not explicit in the code.
- **Read before write**: always have the agent read relevant existing code before generating new code. "Follow the pattern in X" is the most effective instruction.
- **Provide architecture documentation**: without it, AI will introduce patterns that conflict with the existing architecture.
- **Incremental changes**: small, focused modifications are much more reliable than large-scale refactoring.
- **Module-by-module**: legacy codebases should be modernized one module at a time, not all at once.
- **Existing test coverage matters**: if existing tests exist, run them after every AI change to catch regressions. If they don't, write tests for the module being changed BEFORE changing it.

#### Common Failure Mode: The Rewrite Temptation

AI agents, when shown legacy code, will often suggest or attempt a full rewrite rather than incremental improvement. This is almost always wrong:
- Rewrites discard embedded domain knowledge.
- Rewrites invalidate all existing tests.
- Rewrites introduce new bugs while fixing old ones.
- The right approach: incremental refactoring, guided by tests, one smell at a time.

Sources:
- [Anthropic: Building Effective AI Agents](https://www.anthropic.com/research/building-effective-agents)
- [Anthropic: Effective Harnesses for Long-Running Agents](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
- [TeamDay: The Complete Guide to Agentic Coding in 2026](https://www.teamday.ai/blog/complete-guide-agentic-coding-2026)
- [DEV Community: The Four Modalities for Coding with Agents](https://dev.to/eabait/the-four-modalities-for-coding-with-agents-4cdf)

---

### 3.7 Context Window Management in Code

The context window is the AI agent's working memory. Everything that goes in (code, documentation, conversation history, tool output) competes for space. Managing it is a first-class concern.

#### File Size Guidelines

- **Target: under 200-300 lines per file.** AI accuracy degrades as file size increases.
- **Split large files proactively.** A 1000-line file is hard for humans and harder for AI.
- **One public class/interface per file.** This is already convention in Java; adopt it everywhere.

#### Function Length Guidelines

- **Target: 5-20 lines per function.** This is Clean Code's recommendation, and it helps AI even more than humans.
- **Maximum: 50 lines.** Beyond this, AI modification accuracy drops significantly.
- **Cyclomatic complexity under 10.** Deeply nested control flow confuses both humans and AI.

#### Reducing Context Needs

- **Strong typing reduces context.** The type signature tells the AI what a function does. Weakly typed / stringly typed code requires the AI to read more surrounding code.
- **Self-documenting names reduce context.** The agent doesn't need to read the function body to understand `calculateShippingCost(order, destination)`.
- **Small interfaces reduce context.** An interface with 3 methods is easier to implement correctly than one with 30.
- **Colocated tests reduce context.** The agent can read the test to understand expected behavior without loading distant test files.

#### Prompt/Instructions Context Budget

- Keep coding guidelines concise. Long, repetitive instructions waste context.
- Provide constraints, not prose. "Functions under 20 lines. No static mutable state. Constructor injection only." is better than a paragraph explaining why each is important.
- Reference existing code by path rather than pasting it into instructions.

Sources:
- [Medium: The Context Window Explained (Sharjeel Haider)](https://medium.com/@sharjeelhaidder/the-context-window-explained-your-key-to-high-performance-ai-coding-eb29a9c7791f)
- [Local AI Zone: Context Length Guide 2025](https://local-ai-zone.github.io/guides/context-length-optimization-ultimate-guide-2025.html)
- [GitHub Gist: General Guidelines for AI Code Generation](https://gist.github.com/juanpabloaj/d95233b74203d8a7e586723f14d3fb0e)
- [Developer Toolkit: File Organization for AI](https://developertoolkit.ai/en/shared-workflows/context-management/file-organization/)

---

## Appendix: Consolidated Rules for Implementation Skills

Distilled from all three parts, these are the concrete, enforceable rules suitable for an AI agent's coding skill:

### Naming
1. Names reveal intent. No abbreviations unless universally understood (e.g., `id`, `url`, `http`).
2. Classes are nouns. Methods are verbs.
3. One word per concept across the codebase. Be consistent.
4. No encodings, prefixes, or Hungarian notation.

### Functions
5. Functions do one thing. Under 20 lines.
6. Maximum 3 parameters. Use parameter objects beyond that.
7. No boolean flag arguments. Split into two functions.
8. No side effects. Command-Query Separation.
9. Return early to reduce nesting. Guard clauses at the top.

### Error Handling
10. Use exceptions, not error codes.
11. Never return null. Use Optional, empty collections, or Null Object.
12. Never pass null.
13. Create informative error messages with context.
14. Error handling functions do nothing else.

### Classes and Modules
15. Single Responsibility: one reason to change.
16. Small classes. If a class needs a table of contents, it's too big.
17. High cohesion: all methods should use most of the class's fields.
18. Prefer composition over inheritance.
19. Depend on abstractions at architectural boundaries.

### Architecture
20. Business logic has zero dependencies on infrastructure/frameworks.
21. Dependency arrows point inward (toward business logic).
22. Interfaces defined from the consumer's perspective.
23. Each layer can only call the layer directly below/inside it.

### Testing
24. Tests are first-class code. Same quality standards apply.
25. One assertion per test (logical assertion, not literal).
26. Test behavior, not implementation.
27. Fast, Independent, Repeatable, Self-validating, Timely (FIRST).
28. Excessive test setup is a design smell, not a testing problem.

### AI-Specific
29. Files under 300 lines. Functions under 20 lines.
30. Strong typing everywhere. Minimize stringly-typed code.
31. Colocate tests with source.
32. Document architecture constraints explicitly (agents can't infer them).
33. Review every AI-generated line. Trust nothing.
34. Generate incrementally: one function, one test, verify, repeat.
35. For existing code: read first, match conventions, change incrementally.

---

*Research compiled April 2026. Sources include Robert C. Martin (Clean Code, Clean Architecture, SOLID), Martin Fowler (Refactoring, Code Smells), Kent Beck (XP, TDD), Andy Hunt & Dave Thomas (Pragmatic Programmer), Gang of Four (Design Patterns), Alistair Cockburn (Hexagonal Architecture), Anthropic (Building Effective Agents), Stack Overflow Blog (2026 AI agent studies), Google DORA Report 2025, and multiple practitioner sources.*
