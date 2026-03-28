# Legacy Java Retrofit Strategies

Research into techniques for retrofitting legacy Java codebases with proper documentation, testing, and compliance artifacts.

---

## 1. Legacy Java Code Analysis Techniques

### 1.1 Static Analysis Tools

#### SonarQube

SonarQube is the de facto standard for continuous code quality inspection in Java ecosystems.

- **What it provides:** Bug detection, vulnerability scanning, code smell identification, technical debt estimation, and coverage tracking.
- **Retrofit strategy:** Deploy SonarQube against the legacy codebase as-is. Use the initial scan as a baseline. Configure quality gates that apply only to *new/changed code* (the "clean as you code" approach), so the team is not overwhelmed by thousands of existing issues.
- **Key metrics for legacy assessment:**
  - Cognitive complexity per method (threshold: 15)
  - Duplicated lines percentage
  - Technical debt ratio (maintainability rating)
  - Reliability rating (bug density)
  - Security hotspot count
- **Integration:** Maven plugin (`sonar-maven-plugin`), Gradle plugin (`org.sonarqube`), or CI/CD scanner.
- **Quality profiles:** Start with "Sonar way" (default), then customize. For legacy code, consider creating a relaxed profile initially and tightening over time.

#### PMD

PMD performs static analysis focused on coding standards, best practices, and common programming flaws.

- **Strengths for legacy analysis:**
  - Copy-paste detection (CPD) identifies duplicated code blocks across the codebase.
  - Unused code detection (dead code, unused variables, unreachable code).
  - Overly complex expressions and methods.
- **Retrofit approach:** Run PMD with the full ruleset first to get a comprehensive picture, then create a custom ruleset focused on the most impactful rules. Use the `suppressions.xml` baseline approach to suppress existing violations and enforce rules only on new code.
- **Maven integration:** `maven-pmd-plugin` with `<failOnViolation>false</failOnViolation>` initially, then tighten.

#### SpotBugs (successor to FindBugs)

SpotBugs analyzes bytecode to find real bugs that would be missed by source-level analysis.

- **Unique value for legacy code:**
  - Null pointer dereference detection.
  - Concurrency bugs (e.g., inconsistent synchronization).
  - Resource leaks (unclosed streams, connections).
  - Serialization problems.
  - Incorrect API usage patterns.
- **Retrofit approach:** Run with all detectors enabled, then focus on HIGH and MEDIUM confidence findings. Use the `fb-contrib` and `find-sec-bugs` plugins for additional coverage.
- **Baseline file:** SpotBugs supports an exclusion filter XML file. Generate one from the initial scan to suppress existing findings, then enforce zero new findings.

#### Checkstyle

Checkstyle enforces coding conventions and formatting standards.

- **Legacy retrofit consideration:** Do NOT apply Checkstyle formatting rules retroactively to the entire codebase at once. Mass formatting commits destroy `git blame` history. Instead:
  1. Agree on the target style (Google Java Style or Sun conventions).
  2. Apply Checkstyle only to new/modified files.
  3. Use `SuppressionFilter` with a file listing legacy files.
  4. Optionally, format individual files as they are touched for other reasons.

#### Combined Static Analysis Strategy

For a legacy codebase, layer these tools in order of priority:

1. **SpotBugs first** -- finds real bugs, highest signal-to-noise ratio.
2. **SonarQube second** -- provides the dashboard and tracks improvement over time.
3. **PMD third** -- catches coding standard violations and dead code.
4. **Checkstyle last** -- formatting is lowest priority in legacy rescue.

### 1.2 Architecture Recovery Tools

#### Structure101

Structure101 (now part of Restructure101 by Headway Software) is a commercial tool for understanding and controlling Java architecture.

- **Capabilities:**
  - Visualizes package/class dependency graphs.
  - Identifies architectural layers and cycles.
  - Detects "fat" packages with too many responsibilities.
  - Defines and enforces dependency rules (layered architecture constraints).
- **Retrofit use:** Generate an architecture diagram from the existing code, identify cyclical dependencies, and create a target architecture. Use the tool to track progress toward the target.

#### jQAssistant

jQAssistant scans Java bytecode and project structure into a Neo4j graph database, enabling architecture queries via Cypher.

- **Retrofit power:**
  - Query arbitrary structural questions: "Which classes in package X depend on package Y?"
  - Define architecture constraints as executable Cypher rules.
  - Detect layer violations, naming convention violations, and dependency rule breaches.
  - Integrate with Maven/Gradle for continuous architecture validation.
- **Example queries for legacy analysis:**
  ```cypher
  // Find classes with more than 20 methods (god classes)
  MATCH (c:Class)-[:DECLARES]->(m:Method)
  WITH c, count(m) as methodCount
  WHERE methodCount > 20
  RETURN c.fqn, methodCount ORDER BY methodCount DESC

  // Find circular package dependencies
  MATCH path = (p1:Package)-[:DEPENDS_ON*2..5]->(p1)
  RETURN path
  ```
- **Integration:** Maven plugin (`jqassistant-maven-plugin`), runs during build.

#### ArchUnit

ArchUnit is a library for checking Java architecture rules as unit tests.

- **Advantages for legacy retrofit:**
  - Architecture rules are expressed as JUnit tests -- fits the existing test infrastructure.
  - Rules are version-controlled alongside the code.
  - Freezing mechanism: `FreezingArchRule` captures existing violations and only fails on new ones.
- **Example rules:**
  ```java
  @ArchTest
  static final ArchRule no_cycles =
      slices().matching("com.example.(*)..")
          .should().beFreeOfCycles();

  @ArchTest
  static final ArchRule services_should_not_access_controllers =
      noClasses().that().resideInAPackage("..service..")
          .should().dependOnClassesThat()
          .resideInAPackage("..controller..");
  ```
- **Freeze strategy:** Use `FreezingArchRule.freeze(rule)` to record all existing violations. The test passes as long as no *new* violations are introduced. Remove frozen violations incrementally.

### 1.3 Dependency Analysis

#### Maven/Gradle Dependency Tools

- **`mvn dependency:tree`** -- full transitive dependency tree.
- **`mvn dependency:analyze`** -- finds unused declared dependencies and used undeclared dependencies.
- **`gradle dependencies`** -- Gradle equivalent.
- **OWASP Dependency-Check** (`dependency-check-maven`) -- scans dependencies against the National Vulnerability Database (NVD). Critical for compliance.

#### JDepend / JDepend2

- Measures package-level metrics: afferent/efferent coupling, abstractness, instability, distance from the main sequence.
- Identifies packages that are "zones of pain" (high stability + low abstractness) or "zones of uselessness" (high abstractness + low stability).

#### Dependency Analysis Strategy for Legacy Code

1. Run `mvn dependency:analyze` to identify unused dependencies (candidates for removal).
2. Run OWASP Dependency-Check to identify known vulnerabilities.
3. Use `mvn versions:display-dependency-updates` to identify stale dependencies.
4. Prioritize: security vulnerabilities first, then unused dependency removal, then version updates.

### 1.4 Code Complexity Metrics

Key metrics to collect for legacy Java code:

| Metric | Tool | Threshold | Meaning |
|--------|------|-----------|---------|
| Cyclomatic Complexity | PMD, SonarQube | >10 per method | Too many branches |
| Cognitive Complexity | SonarQube | >15 per method | Hard for humans to understand |
| Lines per Method | PMD, Checkstyle | >50 | Method does too much |
| Lines per Class | PMD, Checkstyle | >500 | God class |
| Depth of Inheritance | SonarQube | >5 | Over-engineered hierarchy |
| Coupling Between Objects | JDepend, SonarQube | >20 | Too many dependencies |
| LCOM4 (Lack of Cohesion) | SonarQube | >1 | Class has multiple responsibilities |
| NPath Complexity | PMD | >200 | Exponential test paths |

**Strategy:** Generate a heat map of the codebase by complexity. Focus testing and documentation efforts on high-complexity, high-change-frequency areas first (the "hot spots").

---

## 2. Incremental Testing Strategies for Legacy Java

### 2.1 Working Effectively with Legacy Code (Michael Feathers) Patterns

Feathers defines "legacy code" as code without tests. His core workflow for adding tests to legacy code:

1. **Identify change points** -- where you need to make a change.
2. **Find test points** -- where you can observe the effects of the change.
3. **Break dependencies** -- make the code testable.
4. **Write tests** -- characterization tests first, then change-driving tests.
5. **Make changes and refactor.**

#### Key Dependency-Breaking Techniques

| Technique | When to Use | Example |
|-----------|-------------|---------|
| **Extract Interface** | Class has concrete dependencies | Extract `UserRepository` interface from `JdbcUserRepository` |
| **Parameterize Constructor** | Constructor creates its own dependencies | Pass dependencies in instead of `new`-ing them |
| **Subclass and Override Method** | Need to neutralize a side-effecting method | Override `sendEmail()` with a no-op in test subclass |
| **Extract and Override Call** | Inline static/global calls | Extract `getCurrentTime()` method, override in tests |
| **Introduce Instance Delegator** | Static methods block testing | Create instance method that delegates to static, override in tests |
| **Replace Global Reference with Getter** | Singletons block testing | Access singleton through overridable getter |
| **Adapt Parameter** | Method depends on hard-to-construct types | Introduce a wrapper interface |
| **Break Out Method Object** | Method is too long/complex to test | Move method body to a new class |
| **Sprout Method/Class** | Need to add new behavior without modifying existing | Add new behavior in a new, tested method/class |
| **Wrap Method/Class** | Need to add behavior before/after existing | Decorator pattern around existing code |

#### The Legacy Code Change Algorithm

```
1. Identify the change point
2. Find an inflection point (a seam)
3. Cover the inflection point with tests
4. Make your change (TDD from here)
5. Refactor if needed
```

### 2.2 Characterization Testing / Golden Master Testing

Characterization tests capture the *actual* current behavior of the code, not the *intended* behavior. They serve as a safety net for refactoring.

#### Approach

```java
@Test
void characterize_calculateDiscount_existingBehavior() {
    // Arrange: use real inputs observed in production
    Order order = new Order("GOLD", 250.00, 5);

    // Act
    double discount = calculator.calculateDiscount(order);

    // Assert: record actual behavior, not expected behavior
    // First run: observe output, then encode it
    assertEquals(37.50, discount, 0.001);
}
```

#### Golden Master Pattern

For complex outputs (reports, XML, large data structures):

1. Run the legacy code with known inputs.
2. Capture the full output to a file (the "golden master").
3. In the test, run the same inputs and compare output to the golden master.
4. Any deviation from the golden master fails the test.

```java
@Test
void goldenMaster_invoiceReport() throws Exception {
    Report report = generator.generate(testData);
    String actual = report.toXml();

    Path goldenMaster = Path.of("src/test/resources/golden/invoice-report.xml");
    if (!Files.exists(goldenMaster)) {
        // First run: create the golden master
        Files.writeString(goldenMaster, actual);
        fail("Golden master created. Review and re-run.");
    }

    String expected = Files.readString(goldenMaster);
    assertEquals(expected, actual);
}
```

#### When to Use Characterization Tests

- Before any refactoring of legacy code.
- When the specification is missing or unreliable.
- When the code is the only source of truth for behavior.
- To build confidence before introducing dependency injection or other structural changes.

### 2.3 Seam Identification for Testability

A **seam** (Feathers' term) is a place where you can alter behavior without editing the code at that point.

#### Types of Seams in Java

| Seam Type | Mechanism | Example |
|-----------|-----------|---------|
| **Object seam** | Polymorphism / interfaces | Pass a mock `EmailService` instead of the real one |
| **Preprocessor seam** | N/A in Java | Not applicable |
| **Link seam** | Classpath manipulation | Put a test version of a class earlier on the classpath |
| **Constructor seam** | Dependency injection | Different wiring in test vs. production |

#### Identifying Seams in Legacy Java Code

Look for:
- **Constructor calls (`new`)** inside methods -- these are anti-seams. Each `new` is a hardcoded dependency.
- **Static method calls** -- these cannot be overridden (without PowerMock/Mockito inline).
- **Interface parameters** -- these are natural seams. Code that accepts interfaces is already testable.
- **Protected/package-private methods** -- these can be overridden in test subclasses.
- **Configuration files** -- Spring XML, properties files, JNDI -- these are deployment-time seams.

#### Making Seams Where None Exist

Priority order for introducing seams (least disruptive to most):

1. **Extract parameter** -- pass the dependency as a method argument.
2. **Extract interface + parameterize constructor** -- the standard DI pattern.
3. **Subclass and override** -- test-only subclass overrides specific methods.
4. **Sprout class** -- put new logic in a new, fully tested class.
5. **Wrap class** -- decorator around the legacy class.

### 2.4 Approval Testing Approaches

Approval testing (also called snapshot testing) extends the golden master concept with tooling support.

#### ApprovalTests.Java

The `ApprovalTests` library provides infrastructure for approval-based testing:

```java
@Test
void approval_test_report_output() {
    String result = reportGenerator.generate(sampleData);
    Approvals.verify(result);
}
```

- On first run, creates a `.received.txt` file.
- Developer reviews and renames to `.approved.txt`.
- Subsequent runs compare output to the approved version.
- Diff tool integration for reviewing changes.

#### Combination Approvals

Test many input combinations at once:

```java
@Test
void approval_combination_test() {
    String[] customers = {"GOLD", "SILVER", "BRONZE"};
    Double[] amounts = {10.0, 100.0, 1000.0};
    Integer[] quantities = {1, 5, 10};

    CombinationApprovals.verifyAllCombinations(
        this::calculateDiscount,
        customers, amounts, quantities
    );
}
```

This generates a matrix of all combinations and their outputs, capturing current behavior comprehensively.

#### Benefits for Legacy Code

- No need to understand the code to write the test.
- Captures emergent behavior (bugs that have become features).
- Fast to write -- one test can cover hundreds of scenarios.
- Diff-based review when behavior changes.

### 2.5 Integration Test Harnesses

#### Strategy for Legacy Systems

Legacy Java systems often have deep integration points (databases, message queues, external services). Testing strategies:

**Testcontainers**

Use Testcontainers to spin up real infrastructure in Docker for integration tests:

```java
@Testcontainers
class OrderRepositoryIT {
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15")
            .withInitScript("legacy-schema.sql");

    @Test
    void should_persist_and_retrieve_order() {
        // Test against real database with legacy schema
    }
}
```

**Database Test Fixtures**

For legacy systems with complex database state:

1. Take a sanitized snapshot of production data.
2. Load it into a Testcontainers database.
3. Run integration tests against realistic data.
4. Use DbUnit or database-rider for dataset management.

**WireMock for External Services**

Record-and-replay pattern for external service dependencies:

```java
@WireMockTest(httpPort = 8089)
class PaymentGatewayIT {
    @Test
    void should_process_payment() {
        // Stub captured from production traffic
        stubFor(post("/api/charge")
            .willReturn(okJson(loadFixture("charge-response.json"))));

        PaymentResult result = gateway.charge(order);
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
    }
}
```

**Incremental Integration Test Strategy**

1. Start with the outermost layer (controllers/endpoints) -- highest value, easiest to write.
2. Add database integration tests for repositories -- validate SQL/schema assumptions.
3. Add external service tests with WireMock -- capture current integration contracts.
4. Work inward toward unit tests as you refactor.

---

## 3. Documentation Generation from Existing Code

### 3.1 JavaDoc Extraction and Enrichment

#### Current State Assessment

Most legacy codebases have inconsistent or missing JavaDoc. Strategy:

1. **Audit existing JavaDoc coverage:**
   ```bash
   # Maven JavaDoc plugin with failure on warnings
   mvn javadoc:javadoc -Dquiet=false
   ```
   Use Checkstyle's `JavadocMethod`, `JavadocType`, and `JavadocVariable` checks to measure coverage.

2. **Prioritize documentation targets:**
   - Public API classes and methods (highest value).
   - Classes with high fan-in (many callers depend on them).
   - Complex methods (high cyclomatic complexity).
   - Entry points (controllers, message handlers, scheduled tasks).

3. **Enrichment approach:**
   - Add `@param`, `@return`, `@throws` to public methods.
   - Add class-level JavaDoc explaining responsibility and collaborators.
   - Add `@see` cross-references between related classes.
   - Add `@since` tags when version history is available from git.
   - Add `{@link}` references to related types.

#### Automated JavaDoc Generation Aids

- **JavaDoc from tests:** Well-named test methods serve as executable documentation. Use tools that extract test names into human-readable documentation.
- **AI-assisted enrichment:** Use LLMs to draft initial JavaDoc from method signatures and bodies, then have developers review and correct.
- **JavaDoc quality enforcement:** Configure Checkstyle to require JavaDoc on public API elements in new/modified code only.

### 3.2 Architecture Documentation from Code Structure

#### Automated Architecture Extraction

**C4 Model Generation**

Use tools to generate C4 architecture diagrams from code:

- **Structurizr** -- Define architecture models in Java DSL, extract components from Spring annotations and package structure.
- **jQAssistant + PlantUML** -- Query the code graph and generate PlantUML diagrams.
- **ArchUnit + custom reporters** -- Extract layer definitions and dependencies into diagram formats.

**Package-Level Documentation**

Create `package-info.java` files for every significant package:

```java
/**
 * Order processing domain.
 *
 * <p>This package contains the core order lifecycle management,
 * including creation, validation, pricing, and fulfillment.
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link com.example.order.OrderService} - orchestrates order lifecycle</li>
 *   <li>{@link com.example.order.Order} - aggregate root</li>
 *   <li>{@link com.example.order.PricingEngine} - calculates order totals</li>
 * </ul>
 *
 * <h2>Dependencies</h2>
 * <p>Depends on: {@code com.example.inventory}, {@code com.example.pricing}
 * <p>Depended on by: {@code com.example.api}, {@code com.example.fulfillment}
 */
package com.example.order;
```

**Module Documentation**

For multi-module Maven/Gradle projects, maintain a module dependency diagram:

```
project-root/
  docs/
    architecture/
      module-dependencies.puml    # PlantUML of module relationships
      data-flow.puml              # How data moves through the system
      deployment.puml             # Deployment topology
```

### 3.3 Reverse-Engineering Requirements from Behavior

#### Behavior Extraction Techniques

1. **Test names as requirements:**
   Well-structured tests encode requirements. Extract and organize them:
   ```
   OrderServiceTest
     should_reject_order_when_inventory_insufficient
     should_apply_loyalty_discount_for_gold_customers
     should_notify_warehouse_on_confirmed_order
   ```
   These map directly to business rules.

2. **Validation rules as requirements:**
   Find all validation logic (Bean Validation annotations, manual checks) and compile them into a requirements list.

3. **Configuration-driven behavior:**
   Properties files, feature flags, and configuration classes encode business rules. Document them.

4. **Database constraints as requirements:**
   Schema constraints (NOT NULL, UNIQUE, CHECK, foreign keys) encode invariants. Extract them.

5. **Exception handling as edge case requirements:**
   Catch blocks and custom exceptions reveal error-handling requirements.

#### Requirement Recovery Process

```
Step 1: Identify entry points (REST endpoints, scheduled jobs, message handlers)
Step 2: Trace each entry point through the call graph
Step 3: Document the happy path
Step 4: Document validation rules and error paths
Step 5: Document side effects (database writes, events, notifications)
Step 6: Cross-reference with any existing requirements documents
Step 7: Review with domain experts to fill gaps
```

### 3.4 Test-as-Documentation Patterns

#### Living Documentation

Tests that serve as executable specifications:

**BDD-Style Tests with JUnit 5**

```java
@Nested
@DisplayName("When a Gold customer places an order")
class GoldCustomerOrders {

    @Test
    @DisplayName("should apply 15% loyalty discount")
    void loyaltyDiscount() { /* ... */ }

    @Test
    @DisplayName("should waive shipping for orders over $100")
    void freeShipping() { /* ... */ }

    @Test
    @DisplayName("should earn double loyalty points")
    void doublePoints() { /* ... */ }
}
```

**Cucumber/Gherkin for Business-Readable Specs**

```gherkin
Feature: Order Pricing
  As a sales manager
  I want loyalty discounts applied correctly
  So that we retain valuable customers

  Scenario: Gold customer discount
    Given a customer with "GOLD" loyalty tier
    When they place an order for $200.00
    Then a 15% discount should be applied
    And the total should be $170.00
```

**Test Report as Documentation**

Configure Maven Surefire to generate readable test reports:
- **Allure Reports** -- rich HTML test reports with steps, attachments, and history.
- **Serenity BDD** -- generates living documentation from tests.
- **Custom JUnit 5 `TestReporter`** -- outputs structured test results.

---

## 4. Traceability in Java Projects

### 4.1 Annotation-Based Traceability

#### Custom Annotations Linking Code to Requirements

Define annotations that link implementation to requirements:

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Requirement {
    String id();
    String description() default "";
    String[] verifiedBy() default {};
}

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DesignDecision {
    String id();
    String rationale();
    String[] alternatives() default {};
}
```

Usage:

```java
@Requirement(id = "REQ-ORD-042",
    description = "Gold customers receive 15% discount",
    verifiedBy = {"OrderPricingTest#goldCustomerDiscount"})
public double calculateDiscount(Order order) {
    // ...
}
```

#### Annotation Processing for Traceability Reports

Write an annotation processor or reflection-based scanner that:

1. Scans all classes for `@Requirement` annotations.
2. Cross-references with the requirements document.
3. Identifies requirements without implementation (gaps).
4. Identifies implementation without requirements (orphans).
5. Generates a traceability matrix.

```java
public class TraceabilityScanner {
    public TraceabilityMatrix scan(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Method> annotated = reflections
            .getMethodsAnnotatedWith(Requirement.class);

        Map<String, List<String>> reqToCode = new HashMap<>();
        for (Method m : annotated) {
            Requirement req = m.getAnnotation(Requirement.class);
            reqToCode.computeIfAbsent(req.id(), k -> new ArrayList<>())
                .add(m.getDeclaringClass().getName() + "#" + m.getName());
        }
        return new TraceabilityMatrix(reqToCode);
    }
}
```

#### Test-Level Traceability

Link tests to requirements:

```java
@Test
@Requirement(id = "REQ-ORD-042")
@DisplayName("Gold customers receive 15% discount on orders over $100")
void goldCustomerDiscount() {
    // ...
}
```

### 4.2 Naming Conventions that Encode Traceability

#### Convention-Based Traceability

When annotation infrastructure is too heavy, use naming conventions:

**Test Class Naming:**
```
REQ_ORD_042_GoldCustomerDiscountTest
```

**Test Method Naming:**
```java
@Test
void REQ_ORD_042_should_apply_15_percent_discount_for_gold_customers() {}
```

**Package Naming:**
```
com.example.order.pricing    -> maps to component "Order Pricing"
com.example.order.validation -> maps to component "Order Validation"
```

**Commit Message Convention:**
```
[REQ-ORD-042] Implement gold customer discount logic
```

#### Extracting Traceability from Naming Conventions

Build a simple scanner that:

1. Parses test class/method names for requirement IDs (regex: `REQ_[A-Z]+_\d+`).
2. Parses git commit messages for requirement references.
3. Maps package structure to architectural components.
4. Generates a traceability report.

### 4.3 Build Tool Integration for Traceability Reports

#### Maven Plugin for Traceability

Create a custom Maven plugin or use the `maven-site-plugin` with a custom report:

```xml
<plugin>
    <groupId>com.example</groupId>
    <artifactId>traceability-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>traceability-report</goal>
            </goals>
            <configuration>
                <requirementsFile>docs/requirements.yaml</requirementsFile>
                <basePackage>com.example</basePackage>
                <outputFormat>html,csv</outputFormat>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Gradle Task for Traceability

```groovy
task traceabilityReport(type: JavaExec) {
    classpath = sourceSets.main.runtimeClasspath
    mainClass = 'com.example.tools.TraceabilityReportGenerator'
    args = [
        '--requirements', 'docs/requirements.yaml',
        '--source', 'src/main/java',
        '--tests', 'src/test/java',
        '--output', 'build/reports/traceability'
    ]
}
check.dependsOn traceabilityReport
```

#### CI/CD Integration

- Run traceability report as part of CI pipeline.
- Fail the build if coverage of critical requirements drops below threshold.
- Publish traceability matrix as a build artifact.
- Track traceability coverage trend over time.

---

## 5. Scanning and Synchronization

### 5.1 Detecting Code Changes that Affect Documentation

#### Change Impact Classification

Not all code changes require documentation updates. Classify changes:

| Change Type | Documentation Impact | Detection Method |
|-------------|---------------------|------------------|
| Public API signature change | High -- JavaDoc, API docs, integration guides | AST diff of public methods |
| Behavior change (same signature) | Medium -- requirement docs, test docs | Test failure, golden master diff |
| Internal refactoring | Low -- architecture docs only if structure changes | Package/class rename detection |
| Dependency update | Medium -- deployment docs, security docs | `pom.xml` / `build.gradle` diff |
| Configuration change | Medium -- operations docs, deployment guides | Properties/YAML file diff |
| New feature addition | High -- all documentation types | New class/method detection |

#### Automated Detection Approaches

**AST-Based Change Detection**

Use JavaParser to compare AST between commits:

```java
CompilationUnit before = StaticJavaParser.parse(oldSource);
CompilationUnit after = StaticJavaParser.parse(newSource);

// Compare public method signatures
List<MethodDeclaration> oldMethods = before.findAll(MethodDeclaration.class)
    .stream().filter(m -> m.isPublic()).collect(toList());
List<MethodDeclaration> newMethods = after.findAll(MethodDeclaration.class)
    .stream().filter(m -> m.isPublic()).collect(toList());

// Diff and report changes
```

**Annotation-Based Change Tracking**

```java
@DocumentedBehavior(doc = "docs/pricing-rules.md", section = "Gold Discount")
public double calculateGoldDiscount(Order order) {
    // If this method changes, flag docs/pricing-rules.md for review
}
```

A pre-commit hook or CI check scans for changes to methods annotated with `@DocumentedBehavior` and flags the referenced documents for review.

### 5.2 Git Diff-Based Artifact Impact Analysis

#### Implementation Strategy

Build a tool that analyzes `git diff` output and identifies which documentation artifacts need updating:

```yaml
# .workflow/doc-mapping.yaml
mappings:
  - pattern: "src/main/java/com/example/order/**"
    artifacts:
      - "docs/design/order-processing.md"
      - "docs/api/order-endpoints.md"
    tags: ["order-domain"]

  - pattern: "src/main/java/com/example/security/**"
    artifacts:
      - "docs/compliance/security-controls.md"
      - "docs/design/authentication-flow.md"
    tags: ["security", "compliance"]

  - pattern: "pom.xml"
    artifacts:
      - "docs/deployment/dependencies.md"
      - "docs/compliance/dependency-audit.md"
    tags: ["dependencies"]

  - pattern: "src/main/resources/application*.yml"
    artifacts:
      - "docs/deployment/configuration-guide.md"
    tags: ["operations"]
```

#### Git Hook Implementation

```bash
#!/bin/bash
# .git/hooks/pre-push or CI step

changed_files=$(git diff --name-only origin/main...HEAD)
impacted_docs=$(python3 tools/doc-impact.py \
    --mapping .workflow/doc-mapping.yaml \
    --changes "$changed_files")

if [ -n "$impacted_docs" ]; then
    echo "WARNING: The following documentation may need updating:"
    echo "$impacted_docs"
    # Optionally: create a review task or fail the build
fi
```

### 5.3 Continuous Documentation Validation

#### Documentation Freshness Checks

**Link Validation:**
- Verify all `@see` and `{@link}` references in JavaDoc resolve to existing classes/methods.
- Verify all cross-references in Markdown docs point to existing files.
- Run as part of CI.

**Code-Doc Consistency:**
- Parse JavaDoc `@param` tags and compare with actual method parameters.
- Detect methods where signature changed but JavaDoc was not updated.
- SonarQube has rules for this (e.g., `squid:S1176`).

**Architecture Conformance:**
- Run ArchUnit tests on every build to verify documented architecture constraints hold.
- Run jQAssistant constraints to verify component boundaries.
- Compare generated architecture diagrams with documented ones.

**Staleness Detection:**

```yaml
# Track document freshness
documents:
  - path: "docs/design/order-processing.md"
    last_validated: "2026-01-15"
    max_age_days: 90
    related_code:
      - "src/main/java/com/example/order/**"
    status: "current"  # or "stale", "needs-review"
```

A CI job checks if any document has exceeded its `max_age_days` or if related code has changed since `last_validated`.

---

## 6. Java-Specific Compliance Patterns

### 6.1 Compliance Artifact Structure

#### Typical Java Project Compliance Layout

```
project-root/
  docs/
    compliance/
      requirements/
        REQ-ORD-001.yaml          # Individual requirement specs
        REQ-ORD-002.yaml
        requirements-index.yaml   # Master requirement list
      design/
        DES-ORD-001.yaml          # Design decisions
        architecture-overview.md  # High-level architecture
        component-diagram.puml   # Component relationships
      traceability/
        traceability-matrix.csv   # Auto-generated
        coverage-report.html      # Auto-generated
      security/
        threat-model.md
        security-controls.yaml
        dependency-audit.md       # From OWASP scan
      testing/
        test-plan.md
        test-coverage-report/     # Auto-generated
        integration-test-results/ # Auto-generated
      audit/
        change-log.md             # Significant changes
        review-records/           # Code review evidence
```

### 6.2 Maven/Gradle Plugins for Compliance Reporting

#### Maven Plugins

```xml
<build>
  <plugins>
    <!-- Code coverage -->
    <plugin>
      <groupId>org.jacoco</groupId>
      <artifactId>jacoco-maven-plugin</artifactId>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.80</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </plugin>

    <!-- Dependency vulnerability scanning -->
    <plugin>
      <groupId>org.owasp</groupId>
      <artifactId>dependency-check-maven</artifactId>
      <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <formats>
          <format>HTML</format>
          <format>JSON</format>
        </formats>
      </configuration>
    </plugin>

    <!-- License compliance -->
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>license-maven-plugin</artifactId>
      <configuration>
        <excludedLicenses>
          <excludedLicense>GNU General Public License v3.0</excludedLicense>
        </excludedLicenses>
        <failOnBlacklist>true</failOnBlacklist>
      </configuration>
    </plugin>

    <!-- Static analysis -->
    <plugin>
      <groupId>org.sonarsource.scanner.maven</groupId>
      <artifactId>sonar-maven-plugin</artifactId>
    </plugin>
  </plugins>
</build>

<!-- Reporting aggregation -->
<reporting>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-site-plugin</artifactId>
    </plugin>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-project-info-reports-plugin</artifactId>
    </plugin>
  </plugins>
</reporting>
```

#### Gradle Plugins

```groovy
plugins {
    id 'jacoco'
    id 'org.owasp.dependencycheck' version '9.0.0'
    id 'com.github.hierynomus.license' version '0.16.1'
    id 'org.sonarqube' version '5.0.0'
}

jacoco {
    toolVersion = "0.8.11"
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80
            }
        }
    }
}

dependencyCheck {
    failBuildOnCVSS = 7.0f
    formats = ['HTML', 'JSON']
}

// Aggregate compliance report
task complianceReport {
    dependsOn 'test', 'jacocoTestReport', 'dependencyCheckAnalyze'
    doLast {
        // Aggregate all reports into a single compliance package
    }
}
```

### 6.3 YAML/Markdown-Based Requirement and Design Documents

#### Requirement Specification in YAML

```yaml
# docs/compliance/requirements/REQ-ORD-042.yaml
id: REQ-ORD-042
title: "Gold Customer Loyalty Discount"
status: implemented
priority: high
category: business-rule
description: |
  Customers with Gold loyalty tier must receive a 15% discount
  on all orders exceeding $100.00.
acceptance_criteria:
  - "Discount applies only to Gold tier customers"
  - "Order subtotal must exceed $100.00 before discount"
  - "Discount is calculated as 15% of the subtotal"
  - "Discount appears as a separate line item on the invoice"
implementation:
  classes:
    - "com.example.order.pricing.LoyaltyDiscountCalculator"
    - "com.example.order.pricing.GoldTierStrategy"
  methods:
    - "LoyaltyDiscountCalculator#calculate"
verification:
  test_classes:
    - "com.example.order.pricing.LoyaltyDiscountCalculatorTest"
  test_methods:
    - "goldCustomerDiscount_appliesCorrectPercentage"
    - "goldCustomerDiscount_requiresMinimumAmount"
    - "goldCustomerDiscount_appearsOnInvoice"
  coverage_target: 95%
traceability:
  design_decisions:
    - "DES-ORD-015"
  parent_requirement: "REQ-ORD-040"
  change_history:
    - date: "2025-06-15"
      author: "jsmith"
      description: "Initial implementation"
    - date: "2025-09-01"
      author: "mjones"
      description: "Updated threshold from $50 to $100"
```

#### Design Decision in YAML

```yaml
# docs/compliance/design/DES-ORD-015.yaml
id: DES-ORD-015
title: "Strategy Pattern for Loyalty Tier Discounts"
status: accepted
date: "2025-06-10"
context: |
  The system needs to support different discount calculations
  for each loyalty tier (Bronze, Silver, Gold, Platinum).
  New tiers may be added in the future.
decision: |
  Use the Strategy pattern with a DiscountStrategy interface
  and tier-specific implementations. Strategies are resolved
  via a factory keyed on the tier enum value.
alternatives_considered:
  - title: "Switch statement in a single method"
    rejected_because: "Violates OCP, hard to extend"
  - title: "Database-driven discount rules"
    rejected_because: "Over-engineering for current 4-tier model"
consequences:
  positive:
    - "Easy to add new tier strategies"
    - "Each strategy is independently testable"
  negative:
    - "More classes than a simple switch"
requirements:
  - "REQ-ORD-040"
  - "REQ-ORD-042"
components:
  - "com.example.order.pricing"
```

#### Traceability Matrix Generation

Build a tool (or Maven/Gradle task) that:

1. Reads all `REQ-*.yaml` files.
2. Reads all `DES-*.yaml` files.
3. Scans source code for `@Requirement` annotations or naming conventions.
4. Scans test code for requirement references.
5. Runs JaCoCo to get coverage data for referenced test classes.
6. Generates a traceability matrix:

```
| Req ID       | Title                    | Design   | Implementation Class               | Test Class                          | Coverage |
|-------------|--------------------------|----------|-------------------------------------|--------------------------------------|----------|
| REQ-ORD-042 | Gold Customer Discount   | DES-015  | LoyaltyDiscountCalculator          | LoyaltyDiscountCalculatorTest       | 97%      |
| REQ-ORD-043 | Silver Customer Discount | DES-015  | LoyaltyDiscountCalculator          | LoyaltyDiscountCalculatorTest       | 94%      |
| REQ-ORD-050 | Order Confirmation Email | DES-018  | OrderNotificationService           | -- MISSING --                        | 0%       |
```

Row `REQ-ORD-050` with `-- MISSING --` test class and 0% coverage flags a compliance gap.

---

## Summary: Retrofit Playbook

### Phase 1: Assess (Weeks 1-2)
1. Run SonarQube, SpotBugs, PMD, OWASP dependency-check against the full codebase.
2. Generate architecture diagrams using jQAssistant or Structure101.
3. Measure complexity metrics and identify hot spots.
4. Catalog existing tests and measure coverage with JaCoCo.

### Phase 2: Stabilize (Weeks 3-6)
1. Add ArchUnit tests with `FreezingArchRule` to lock current architecture.
2. Write characterization tests for critical paths (golden master / approval tests).
3. Add integration tests for database and external service interactions.
4. Fix critical SpotBugs and OWASP findings.

### Phase 3: Document (Weeks 4-8, overlapping with Phase 2)
1. Create `package-info.java` for all significant packages.
2. Add JavaDoc to public API classes and methods (prioritized by fan-in).
3. Write YAML requirement specs for business rules extracted from code.
4. Write YAML design decisions for key architectural choices.
5. Set up traceability annotations or naming conventions.

### Phase 4: Automate (Weeks 7-10)
1. Configure CI pipeline with all static analysis tools (baseline mode).
2. Add traceability matrix generation to the build.
3. Set up doc-impact analysis (git diff to documentation mapping).
4. Configure quality gates: no new bugs, no new vulnerabilities, minimum test coverage on new code.
5. Set up documentation freshness monitoring.

### Phase 5: Continuous Improvement (Ongoing)
1. Tighten static analysis baselines quarterly.
2. Remove `FreezingArchRule` frozen violations as areas are refactored.
3. Increase test coverage targets incrementally.
4. Update requirement and design documents as part of the definition of done for every story.
5. Track and report compliance metrics trend over time.

### Key Principle: Clean As You Code

Do not attempt to retrofit the entire codebase at once. Apply the "Boy Scout Rule" -- leave code better than you found it. Every change to a legacy file should:

1. Add or update tests for the changed behavior.
2. Add or update JavaDoc for touched methods.
3. Update requirement/design documents if behavior changes.
4. Pass all static analysis rules for the changed lines.
5. Maintain traceability (annotation or naming convention) for new logic.

Over time, the codebase converges toward full compliance without a disruptive big-bang retrofit.
