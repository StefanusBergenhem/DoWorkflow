# Traceability Tools Comparison for V-Model Artifact Management

**Date:** 2026-03-27
**Purpose:** Evaluate open-source and lightweight requirements management / traceability tools for supporting a full V-model artifact traceability system.

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Tool-by-Tool Analysis](#2-tool-by-tool-analysis)
   - 2.1 [Doorstop](#21-doorstop)
   - 2.2 [StrictDoc](#22-strictdoc)
   - 2.3 [rmtoo](#23-rmtoo)
   - 2.4 [Sphinx-Needs](#24-sphinx-needs)
   - 2.5 [OpenFastTrace](#25-openfasttrace)
   - 2.6 [TRLC (Treat Requirements Like Code)](#26-trlc)
   - 2.7 [LOBSTER](#27-lobster)
   - 2.8 [Reqflow](#28-reqflow)
   - 2.9 [Melexis Sphinx Traceability Extension](#29-melexis-sphinx-traceability-extension)
3. [ReqIF as a Data Model Reference](#3-reqif-as-a-data-model-reference)
4. [Cross-Cutting Comparison](#4-cross-cutting-comparison)
   - 4.1 [ID Handling Strategies](#41-id-handling-strategies)
   - 4.2 [Artifact Type Support](#42-artifact-type-support)
   - 4.3 [Many-to-Many Tracing](#43-many-to-many-tracing)
   - 4.4 [Safety-Critical Standards Support](#44-safety-critical-standards-support)
   - 4.5 [Git Friendliness](#45-git-friendliness)
5. [Comparison Matrix](#5-comparison-matrix)
6. [Architectural Lessons for V-Model Traceability](#6-architectural-lessons-for-v-model-traceability)
7. [Sources](#7-sources)

---

## 1. Executive Summary

No single open-source tool fully covers the needs of a complete V-model artifact traceability system spanning requirements, architecture, design, implementation, test specifications, test results, and review records. However, several tools offer important architectural patterns and design decisions worth studying:

- **TRLC + LOBSTER** (BMW stack) comes closest to a full V-model traceability system, with TRLC providing a typed DSL for requirements and LOBSTER providing cross-artifact traceability reporting. Designed explicitly for ISO 26262 contexts.
- **StrictDoc** is the most feature-rich single tool, with custom grammars, ReqIF support, and explicit DO-178C awareness, but remains requirements-centric.
- **OpenFastTrace** has the best built-in artifact type model with explicit support for feat/req/dsn/impl/utest/itest artifact types and deep coverage validation.
- **Sphinx-Needs** is the most flexible and extensible, with fully user-defined types, links, and validation, but requires Sphinx as infrastructure.
- **Doorstop** is the simplest and most git-native, but has limited link relation types and no ReqIF support.

**Key gap across all tools:** None natively handles review records, approval workflows, or formal baseline management as first-class artifact types.

---

## 2. Tool-by-Tool Analysis

### 2.1 Doorstop

**Repository:** https://github.com/doorstop-dev/doorstop
**Language:** Python | **License:** LGPLv3 | **Status:** Active (v3.x)

#### ID Handling

- **Format:** Prefix + zero-padded sequential number (e.g., `SRD001`, `SRD002`, `HLTC001`)
- **Generation:** Automatic sequential within a document, based on the document prefix
- **Uniqueness:** Guaranteed unique within a document by prefix + sequence; globally unique when prefixes are unique across documents
- **No UUID or hash-based IDs** -- purely human-readable sequential

#### Traceability

- **Link definition:** Embedded in each item's YAML file under the `links` field
- **Link format:** Array of parent item UIDs, each with an optional fingerprint hash
- **Direction:** Child-to-parent only (child items reference their parents); backward links computed at runtime
- **Many-to-many:** Supported at the item level -- an item can link to multiple parents, and multiple children can link to the same parent
- **Cross-document:** Links work across documents in the document tree
- **Limitation:** The document tree itself is hierarchical (each document has exactly one parent document), which constrains cross-cutting traceability patterns. Links are only parent-child; no typed relations (e.g., "satisfies", "verifies", "implements")

#### Item/Artifact Structure on Disk

One YAML file per item. Example:

```yaml
active: true
derived: false
header: ''
level: 2.1
normative: true
reviewed: 1f33605bbc5d1a39c9a6441b91389e88
links:
  - SRD002: 9a6441b91389e881f33605bbc5d1a39c
ref: ''
references: []
text: |
  The system shall provide unique identifiers
  to all traceable items.
```

Standard fields: `active`, `derived`, `normative`, `level`, `header`, `text`, `reviewed`, `links`, `ref`, `references`

#### Custom Fields

- Any arbitrary key-value pair can be added to the YAML file
- Extended attributes are queryable via REST API and Python API
- Defaults for custom attributes defined in `.doorstop.yml`
- Custom attributes can optionally contribute to the review fingerprint via `attributes.reviewed` config
- Custom attributes can be included in published output via `attributes.publish` config

#### Document Organization

- Each directory = one document (collection of items)
- Documents form a tree: `doorstop create HLTC ./tests/hl --parent SRD`
- Configuration stored in `.doorstop.yml` per document directory
- Hierarchy is document-level, not item-level

#### Validation & Completeness

- Running `doorstop` with no arguments performs integrity checks
- Validates that child items have links to parent document items
- Detects unreviewed changes via SHA256 fingerprint comparison
- Detects suspect links (parent changed since last review)
- **No explicit orphan detection reporting** beyond missing parent links
- **No coverage gap analysis** (e.g., "these requirements have no tests")

#### File Format & Git Friendliness

- **YAML** -- one file per item, very merge-friendly
- Small diffs when single items change
- Inherently git-native; designed for version control from the start
- Merge conflicts are rare and localized to individual item files

#### Extensibility

- Python API for programmatic access
- REST API for external tool integration
- `doorstop import/export` for CSV and XLSX
- No plugin system; extension via Python API or forking

#### Limitations for Full V-Model

- **No typed relations**: Cannot distinguish "verifies", "satisfies", "implements"
- **Hierarchical document tree**: Cannot model arbitrary cross-cutting relationships (e.g., a test case tracing to both a design element and a requirement)
- **No ReqIF support**
- **No concept of artifact types** -- everything is a "requirement" with custom attributes
- **No built-in coverage analysis** across artifact types
- Considered "hardly maintained" and "limited concerning link relations" by some communities (e.g., Zephyr RTOS project evaluated and moved away)

---

### 2.2 StrictDoc

**Repository:** https://github.com/strictdoc-project/strictdoc
**Language:** Python | **License:** Apache 2.0 | **Status:** Active (v0.x, rapid development)

#### ID Handling

- **Format:** User-defined UID strings (e.g., `REQ-001`, `SCA-001`, `SDOC-HIGH-DATA-MODEL`, `SAVOIR.OBC.PM.80`)
- **Generation:** Manual; user assigns UIDs following project conventions
- **Uniqueness:** Enforced by StrictDoc at the project level -- duplicate UIDs trigger validation errors
- **No auto-generation**: UIDs are not automatically generated
- **Flexible naming:** Supports any alphanumeric pattern the project wants

#### Traceability

- **Link definition:** Embedded in the requirement via `RELATIONS` block (must be last field)
- **Relation types:** `Parent`, `Child`, `File` (built-in); custom relation roles possible
- **Many-to-many:** Supported -- a requirement can have multiple Parent and Child relations
- **Roles:** Relations can be specialized with roles (e.g., `Refines`, `Verifies`)
- **File tracing:** `TYPE: File` links requirements to source code files
- **Source code markers:** `@relation` markers in C source connect back to requirements
- **Circular reference detection:** Validation prevents cycles in the relation graph

```
[REQUIREMENT]
UID: REQ-002
TITLE: Data Processing
STATEMENT: The system shall process data in real-time.
RELATIONS:
- TYPE: Parent
  VALUE: REQ-001
- TYPE: Parent
  VALUE: REQ-003
  ROLE: Refines
- TYPE: File
  VALUE: /src/processor.c
```

#### Item/Artifact Structure on Disk

SDoc format -- one file per document (not per item). Uses a custom markup language:

```
[DOCUMENT]
TITLE: System Requirements

[GRAMMAR]
ELEMENTS:
- TAG: REQUIREMENT
  FIELDS:
  - TITLE: UID
    TYPE: String
    REQUIRED: True
  - TITLE: VERIFICATION
    TYPE: String
    REQUIRED: False
  - TITLE: STATEMENT
    TYPE: String
    REQUIRED: True
  RELATIONS:
  - TYPE: Parent
  - TYPE: File

[[SECTION]]
TITLE: Functional Requirements

[REQUIREMENT]
UID: REQ-001
TITLE: System Initialization
STATEMENT: The system shall initialize within 5 seconds.

[[/SECTION]]
```

#### Custom Fields

- Defined via `[GRAMMAR]` at the document level
- Supported field types: `String`, `SingleChoice`, `MultipleChoice`, `Tag`
- Fields can be marked `REQUIRED: True/False`
- Supports domain-specific fields (e.g., `ASIL`, `Verification Method`, `PRIORITY`, `OWNER`)
- Custom relation roles registered in grammar

#### Document Organization

- File-per-document approach (one `.sdoc` file = one document)
- Sections provide internal hierarchy with auto-numbering
- `[DOCUMENT_FROM_FILE]` enables composition from fragments
- Project-level tree assembled from multiple `.sdoc` files
- Supports composite requirements (`[[COMPOSITE_REQUIREMENT]]`)

#### Validation & Completeness

- Circular reference detection in parent-child relations
- Grammar enforcement (required fields, field types)
- UID uniqueness checking across the project
- Relation type validation against grammar
- **Web UI** for viewing and editing documents
- Deep traceability views available in the web interface

#### File Format & Git Friendliness

- **SDoc format** (custom text format) -- human-readable, diff-friendly
- One file per document means changes to separate requirements in the same document create merge conflicts
- Also supports Markdown input (`.md`)
- **Export formats:** HTML, RST (Sphinx-compatible), ReqIF, PDF, JSON, Excel

#### Extensibility

- Custom grammars per document
- Python-based configuration (`strictdoc_config.py`)
- Web-based UI for viewing/editing
- ReqIF import/export for tool interoperability
- No formal plugin API

#### Limitations for Full V-Model

- **Document-centric**: Primarily designed for specification documents, not arbitrary artifact types
- **No built-in concept of test results or review records** as artifact types
- **File-per-document**: Less merge-friendly than file-per-item
- **Relation types are limited** to Parent/Child/File (though roles add some expressiveness)
- **No coverage gap analysis** or automated completeness metrics
- Strong focus on requirements and specifications; extending to architecture or test artifacts requires creative use of custom grammars
- **Active development**: API and features are still evolving; not yet 1.0

---

### 2.3 rmtoo

**Repository:** https://github.com/florath/rmtoo
**Language:** Python | **License:** GPLv3+ | **Status:** Maintained (latest release 26.0.2, July 2025)

#### ID Handling

- **Format:** Requirement name as identifier (e.g., `Documentation`, `Version`)
- **Generation:** Manual; user defines the name
- **Uniqueness:** By convention (name must be unique within the project)
- **File naming:** The requirement ID is also the filename (e.g., `Documentation.req`)

#### Traceability

- **Link definition:** `Solved by` field in the requirement file lists dependent requirements
- **Direction:** Parent-to-child via `Solved by`
- **Dependency tracking:** Automatic dependency analysis and graph generation
- **Output:** Generates dependency graphs (Graphviz)

#### Item/Artifact Structure on Disk

Plain text key-value format (`.req` files):

```
Name: Documentation
Type: requirement
Invented on: 2010-02-12
Invented by: flonatel
Owner: development
Description: \textsl{rmtoo} \textbf{must} be documented.
Status: not done
Priority: development:6 customers:5
Effort estimation: 3
Topic: Documentation
Solved by: DocManPage DocSlides Version
```

Fields: `Name`, `Type`, `Invented on`, `Invented by`, `Owner`, `Description`, `Status`, `Priority`, `Effort estimation`, `Topic`, `Solved by`

#### Document Organization

- Requirements stored in directories as individual `.req` files
- Topics provide hierarchical grouping
- Configuration via project config files

#### Validation & Completeness

- Automated requirement quality checks (details not well documented)
- Dependency analysis
- Statistics generation

#### File Format & Git Friendliness

- **Plain text** -- one file per requirement, very merge-friendly
- Supports YAML storage backend as well (backward-compatible)
- LaTeX markup in description fields

#### Extensibility

- Template projects available
- Contrib directory suggests community extensions
- Output plugins for different formats (HTML, LaTeX/PDF, dependency graphs, statistics)
- No formal plugin API documented

#### Limitations for Full V-Model

- **Narrowly focused on requirements** -- no concept of design, test, or architecture artifacts
- **Limited relation types** (only `Solved by`)
- **LaTeX-centric output** -- not ideal for modern workflows
- **Minimal documentation** on advanced features
- **CLI-only** -- no GUI or web interface
- **SCRUM-oriented** (backlogs, prioritization) rather than safety-critical / V-model oriented
- **No ReqIF support**
- **Small community** and limited ecosystem

---

### 2.4 Sphinx-Needs

**Website:** https://www.sphinx-needs.com/
**Repository:** https://github.com/useblocks/sphinx-needs
**Language:** Python (Sphinx extension) | **License:** MIT | **Status:** Active (v8.x)

#### ID Handling

- **Format:** Prefix-based, user-defined (e.g., `REQ_001`, `SPEC_001`, `TC_001`)
- **Generation:** Manual via `:id:` parameter on need directives
- **Prefix:** Configurable per type (e.g., requirements get `R_`, specs get `S_`)
- **Uniqueness:** Enforced by Sphinx-Needs at build time

#### Traceability

- **Link definition:** Via `:links:` option on individual needs
- **Custom link types:** Fully configurable via `needs_extra_links`
- **Bidirectional:** Each link type has outgoing and incoming descriptions (e.g., "checks" / "is checked by")
- **Many-to-many:** Fully supported -- any need can link to any number of other needs
- **Cross-project:** Links to needs in external Sphinx-Needs projects supported
- **No hierarchical constraints**: Any need can link to any other need regardless of type

```rst
.. req:: System Initialization
   :id: REQ_001
   :status: open
   :links: SPEC_001, SPEC_002
   :integrity: ASIL-D

   The system shall initialize within 5 seconds.
```

#### Custom Link Types

```python
needs_extra_links = [
    {
        "option": "checks",
        "incoming": "is checked by",
        "outgoing": "checks",
    },
    {
        "option": "implements",
        "incoming": "is implemented by",
        "outgoing": "implements",
    },
]
```

#### Item/Artifact Structure

Needs are defined inline in RST documents using directives. No separate files.

#### Custom Fields

- Defined via `needs_extra_options` in `conf.py`
- Any field name, used with `:option_name:` syntax
- Supports domain-specific fields (e.g., `:integrity: ASIL-D`, `:assignee: John`)
- Validation via `needs_warnings` configuration

#### Document Organization

- **Type-based:** Fully user-defined need types via `needs_types` in `conf.py`
- Each type has: directive name, title, prefix, color, style
- Types can represent anything: requirements, specs, tests, architecture decisions, reviews
- Organized within RST documents and Sphinx project structure

#### Validation & Completeness

- `needs_warnings`: Define not-allowed patterns (e.g., "requirements without links")
- Custom filter strings or Python functions for validation
- Status validation
- ID format validation
- **No built-in coverage gap analysis**, but powerful filtering enables custom checks

#### Reporting & Visualization

- `needtable`: Dynamic tables filtered by any attribute
- `needflow`: Flowchart/graph visualization of need relationships
- `needpie`: Pie charts for status distribution
- `needlist`: Filtered lists
- Powerful filter expressions (word-based, complex strings, Python functions)

#### File Format & Git Friendliness

- **RST (reStructuredText)** -- embedded in documentation source
- Needs are part of larger documents, not separate files
- Merge conflicts possible when multiple people edit the same document
- JSON import/export for data exchange

#### Extensibility

- 40+ configuration parameters
- Connectors for JIRA and codebeamer
- JSON data import
- Custom layouts and styles
- Python API access to all internal data
- **No ReqIF support** (JSON-based exchange instead)

#### Limitations for Full V-Model

- **Requires Sphinx infrastructure** -- not standalone
- **RST lock-in** -- must write requirements in RST markup
- **Needs embedded in documents** -- not easily machine-processable as standalone data
- **No standalone validation** -- requires Sphinx build to validate
- **Commercial features** via useblocks consulting may be needed for enterprise use
- **Well-suited for V-model** in terms of type flexibility, but the Sphinx dependency adds infrastructure overhead
- Proven in automotive (1000+ engineers, 100K+ objects) but documentation on safety-critical workflows is limited

---

### 2.5 OpenFastTrace

**Repository:** https://github.com/itsallcode/openfasttrace (also on Codeberg)
**Language:** Java | **License:** GPLv3 | **Status:** Active

#### ID Handling

- **Format:** `type~name~revision` (e.g., `req~html5-exporter~1`, `dsn~data-model~3`)
- **Generation:** Manual; user defines the triplet
- **Uniqueness:** The combination of type + name + revision is unique
- **Revision:** Positive integer, incremented when semantics change significantly
- **Artifact type encoded in the ID** -- this is a key differentiator

#### Traceability

- **Coverage model:** Items declare what artifact types they "Need" coverage in
- **Needs keyword:** `Needs: dsn, impl, utest` means this item requires coverage in design, implementation, and unit test artifact types
- **Covers keyword:** Items declare what they cover: `Covers: req~html5-exporter~1`
- **Delegation:** `dsn --> impl : req~web-ui~1` forwards coverage responsibility
- **Deep coverage:** Covering items must themselves be covered, creating complete chains
- **Many-to-many:** An item can need coverage from multiple types and be covered by multiple items

#### Item/Artifact Structure

Specification items embedded in Markdown or RST documents:

```markdown
`req~system-initialization~1`

The system shall initialize within 5 seconds.

Needs: dsn, itest

Tags: Safety, Startup
```

Coverage tags in source code:

```java
// [impl->req~system-initialization~1]
public void initialize() { ... }
```

#### Artifact Types (Built-in Concept)

This is OpenFastTrace's strongest feature for V-model work:

| Abbreviation | Meaning |
|---|---|
| `feat` | Feature (high-level) |
| `req` | Requirement |
| `dsn` | Design |
| `impl` | Implementation |
| `utest` | Unit test |
| `itest` | Integration test |

These are conventions, not hard-coded -- any abbreviation works. The artifact type is embedded in the specification item ID itself.

#### Validation & Completeness

- **Coverage checking:** Every normative item must have coverage in all declared "Needs" types
- **Deep coverage:** Covering items must themselves be covered (transitive closure)
- **Orphan detection:** Implicit -- items without coverage or that cover nothing are flagged
- **Exit code:** `-e` flag returns exit code 2 if coverage < 100%, enabling CI integration
- **Reports:** HTML, text traceability reports

#### File Format & Git Friendliness

- **Markdown or RST** for specification documents
- **Source code comments** for coverage tags
- **ReqM2 format** import supported
- Items embedded in larger documents; not standalone files
- Merge-friendly since items are small inline blocks

#### Extensibility

- Java API for programmatic access
- Maven and Gradle plugins for build integration
- Import from Markdown, RST, ReqM2
- Export to HTML and text reports
- Designed as a library (JAR) that can be embedded

#### Limitations for Full V-Model

- **Java ecosystem** -- requires JVM
- **No GUI or web interface**
- **No ReqIF import/export**
- **No custom fields** beyond the built-in tag mechanism
- **Items embedded in documents** -- not separately addressable files
- **No formal review/approval workflow**
- **Strong for traceability validation** but weak for requirements authoring/editing
- Best used as a validation/reporting layer rather than a complete management tool

---

### 2.6 TRLC (Treat Requirements Like Code)

**Repository:** https://github.com/bmw-software-engineering/trlc
**Language:** Python | **License:** GPLv3 | **Status:** Active (v3.x)

#### ID Handling

- **Format:** Type name + instance name (e.g., `Requirement Potato`, `SafetyRequirement REQ_001`)
- **Generation:** Manual; user defines the instance name
- **Uniqueness:** Enforced within package scope
- **Fully qualified:** Package + Type + Name for global uniqueness

#### Traceability

- **Link definition:** Via typed reference fields in the record definition
- **Direction:** Defined by the schema (e.g., `derived_from` points upward)
- **Many-to-many:** Supported via array references (e.g., `derived_from optional Requirement [1 .. *]`)
- **Type-safe:** References are type-checked -- a `Requirement` reference can only point to `Requirement` or its subtypes
- **Inline references:** Markup strings support `[[REQ_001]]` syntax for cross-references in text

#### Type System (.rsl files)

This is TRLC's killer feature -- a full type system for requirements:

```
enum ASIL { QM  A  B  C  D }

enum Verification_Method { Analysis  Inspection  Test  Review }

abstract type Base_Requirement {
  summary           String
  description        String
  verification_method Verification_Method
}

type Safety_Requirement extends Base_Requirement {
  asil         ASIL
  derived_from optional Safety_Requirement [0 .. *]
}

type Test_Specification extends Base_Requirement {
  verifies     Safety_Requirement [1 .. *]
  test_method  String
}
```

#### Requirement Instances (.trlc files)

```
Safety_Requirement REQ_001 {
  summary     = "System Initialization"
  description = "The system shall initialize within 5 seconds"
  verification_method = Verification_Method.Test
  asil        = ASIL.D
}

Test_Specification TST_001 {
  summary     = "Init Timing Test"
  description = "Verify system initializes within 5 seconds"
  verification_method = Verification_Method.Test
  test_method = "Automated timing measurement"
  verifies    = [REQ_001]
}
```

#### Supported Data Types

- `Boolean`, `Integer`, `Decimal`, `String`, `Markup_String`
- User-defined: `enum`, `tuple`, `record` (with inheritance)
- Arrays with bounds: `[lower .. upper]` or `[lower .. *]`
- Optional fields

#### Validation (Check Rules)

```
checks Safety_Requirement {
  len(description) >= 10,
    warning "Description is too short",
    description

  derived_from != null or top_level == true,
    error "Must have derivation or be top-level"
}
```

- Severity levels: `warning`, `error`, `fatal`
- Expression-based with field hints
- Evaluated after all references are resolved
- Inheritance-aware: base checks run before extension checks

#### Document Organization

- `.rsl` files: Type definitions (schema)
- `.trlc` files: Requirement instances (data)
- Packages for namespace management
- Import system for cross-package references

#### File Format & Git Friendliness

- **Custom DSL** -- plain text, human-readable, very diff-friendly
- One requirement per block in `.trlc` files
- Schema changes in `.rsl` files are separate from data
- Designed explicitly for version control

#### Extensibility

- Python API for building custom tools
- CI/CD integration (Bazel support)
- **Designed as a language** -- custom tools built on top
- Works with LOBSTER for traceability reporting
- Static analysis tooling included

#### Limitations for Full V-Model

- **No built-in reporting or visualization** -- requires custom tooling or LOBSTER
- **No GUI or web interface**
- **No ReqIF support**
- **Schema changes require updating all instances** -- no migration tooling
- **Learning curve** for the DSL
- **Best combined with LOBSTER** for full traceability
- **Strong type system** is a double-edged sword: powerful but rigid
- **No built-in review/approval workflow**

---

### 2.7 LOBSTER

**Repository:** https://github.com/bmw-software-engineering/lobster
**Language:** Python | **License:** AGPLv3 | **Status:** Active

#### Purpose

LOBSTER is not a requirements management tool -- it is a **traceability evidence report generator**. It extracts tracing tags from multiple sources and produces coverage reports.

#### Architecture

- **Core:** `bmw-lobster-core` -- API and report generators
- **Source extractors:** Tools for Python, C/C++, Java, JSON, TRLC
- **Test extractors:** Tools for Google Test (gtest), cpptest
- **Integration:** Connectors for Codebeamer (commercial RM tool)

#### Traceability Model

- Defines **traceability levels**: requirements, implementation, activity (test)
- Links are extracted from source artifacts (code comments, test annotations)
- Produces unified traceability reports showing coverage
- **Reduces duplication**: Links only added at lower levels of the hierarchy

#### Configuration

- Fully configurable via config files
- No built-in logic -- all traceability rules are user-defined
- Define levels, sources, and linking rules

#### Relevance for V-Model

- **Explicitly designed for ISO 26262** compliance evidence
- Pairs with TRLC for a complete requirements-to-test traceability stack
- Generates evidence reports suitable for safety audits
- Modular: can integrate with various source types

#### Limitations

- **Not a standalone tool** -- needs a requirements source (TRLC, Codebeamer, etc.)
- **Report generation only** -- no authoring, editing, or management
- **BMW-specific conventions** may need adaptation
- **No web interface**

---

### 2.8 Reqflow

**Website:** https://goeb.github.io/reqflow/
**Language:** C | **License:** GPLv2 | **Status:** Maintained

#### Purpose

Lightweight tool for analyzing traceability between requirements documents. Not a requirements authoring tool -- it analyzes existing documents.

#### ID Handling

- **Pattern-based extraction:** Uses PCRE (Perl Compatible Regular Expressions) to find requirement IDs in documents
- **Prefix support:** `-prefix-req` prevents ID conflicts across documents
- **No ID generation** -- IDs must exist in source documents

#### Supported Input Formats

- DOCX (Open XML), ODT (Open Document), HTML, PDF, plain text

#### Traceability

- Extracts requirements and references from documents
- Produces coverage matrices (forward and reverse)
- Reports uncovered requirements
- `-e` flag: exit code 2 when coverage < 100% (CI integration)

#### Limitations for V-Model

- **Analysis only** -- does not manage or author requirements
- **No custom fields or artifact types**
- **No structured data model** -- works with document patterns
- **Useful as a validation layer** on top of other tools
- **No ReqIF support**

---

### 2.9 Melexis Sphinx Traceability Extension

**Repository:** https://github.com/melexis/sphinx-traceability-extension
**Language:** Python (Sphinx extension) | **License:** GPLv3 | **Status:** Active

#### Purpose

Sphinx plugin specifically designed for ISO 26262 projects. Defines documentation items and relations between them.

#### Key Features

- Traceability matrix auto-generation
- Coverage calculation for relations
- Designed for automotive functional safety workflows
- Integration with Sphinx documentation ecosystem

#### Relevance

- Explicitly targets ISO 26262 compliance
- Provides coverage metrics needed for safety audits
- Limited compared to Sphinx-Needs in flexibility but more focused on safety-critical use cases

---

## 3. ReqIF as a Data Model Reference

**Standard:** OMG ReqIF 1.2 (July 2016)
**Specification:** https://www.omg.org/spec/ReqIF/1.2/

### Core Data Model

ReqIF provides a well-designed meta-model that is worth studying even if you do not use ReqIF as an interchange format:

| Concept | Purpose | Relevance |
|---|---|---|
| **SpecObject** | Container for a single requirement/artifact | The "item" or "artifact" concept |
| **SpecType** | Defines the schema of a SpecObject (fields, types) | The "artifact type" concept |
| **AttributeDefinition** | Defines a single field within a SpecType | Field schema with type constraints |
| **AttributeValue** | Actual value of a field on a SpecObject | Field data |
| **DatatypeDefinition** | Data types: String, Boolean, Integer, Real, Enumeration, Date, XHTML | Type system |
| **SpecRelation** | Link between two SpecObjects | Traceability link |
| **SpecRelationType** | Schema for a relation (can have its own attributes) | Typed, attributed links |
| **Specification** | Hierarchical tree organizing SpecObjects | Document structure |
| **SpecHierarchy** | Node in a Specification tree | Document ordering |
| **SpecRelationGroup** | Groups of relations with same source/target | Bulk relationship management |

### Key Design Decisions in ReqIF

1. **GUID-based identification**: Every object has a globally unique identifier (GUID), ensuring uniqueness across the entire supply chain
2. **Typed relations with attributes**: SpecRelations have their own SpecRelationType with AttributeDefinitions, so a link can carry metadata (e.g., verification status, confidence level)
3. **Separation of schema and data**: SpecTypes define the structure; SpecObjects hold the data. This is analogous to TRLC's `.rsl` / `.trlc` separation
4. **Multiple specifications over same objects**: A single SpecObject can appear in multiple Specification trees, supporting different views without data duplication
5. **Tool extensions**: `<REQ-IF-TOOL-EXTENSION>` allows tool-specific data without breaking interoperability

### Relevance for V-Model Traceability

ReqIF's data model is the most complete reference for designing a traceability system:
- **Typed artifacts** (SpecType per artifact kind)
- **Typed, attributed links** (SpecRelationType with its own fields)
- **Many-to-many relations** (SpecRelation is a standalone object, not embedded)
- **Hierarchical views** (Specification/SpecHierarchy)
- **GUID uniqueness** across organizational boundaries

The main limitation is that ReqIF is XML-based and not git-friendly. A traceability system inspired by ReqIF's data model but using YAML/text storage would combine the best of both worlds.

---

## 4. Cross-Cutting Comparison

### 4.1 ID Handling Strategies

| Tool | ID Format | Generation | Uniqueness Scope | Revision Tracking |
|---|---|---|---|---|
| Doorstop | `PREFIX###` (e.g., `SRD001`) | Auto-sequential | Per document | Via SHA256 fingerprint |
| StrictDoc | User-defined UID string | Manual | Project-wide | Not built-in |
| rmtoo | Requirement name | Manual | Project-wide | Not built-in |
| Sphinx-Needs | Prefix + user ID (e.g., `REQ_001`) | Manual | Sphinx project | Not built-in |
| OpenFastTrace | `type~name~revision` | Manual | Project-wide | Explicit revision number |
| TRLC | `Type Name` (e.g., `Requirement REQ_001`) | Manual | Package scope | Not built-in |
| ReqIF | GUID | Auto-generated | Global (supply chain) | Via `LAST-CHANGE` timestamp |

**Observations:**
- No tool uses content-hash-based IDs (like git commit hashes)
- Only OpenFastTrace embeds the artifact type in the ID, which is valuable for quick identification
- Only ReqIF provides true global uniqueness via GUIDs
- Doorstop is the only tool with automatic ID generation
- OpenFastTrace is the only tool with explicit revision tracking in the ID itself

### 4.2 Artifact Type Support

| Tool | Beyond Requirements? | Custom Types? | Built-in Types |
|---|---|---|---|
| Doorstop | Items can be anything via custom attrs | No formal type system | None (everything is an "item") |
| StrictDoc | Custom grammar nodes | Limited (REQUIREMENT, TEXT, COMPOSITE) | REQUIREMENT, TEXT |
| rmtoo | No | No | requirement only |
| Sphinx-Needs | Yes -- any type | Fully configurable | req, spec, impl, test (examples) |
| OpenFastTrace | Yes -- via type abbreviations | Convention-based | feat, req, dsn, impl, utest, itest |
| TRLC | Yes -- full type inheritance | Full type system with enums, tuples | None (everything user-defined) |
| LOBSTER | Levels (req, impl, activity) | Configurable | requirement, implementation, activity |
| ReqIF | Yes -- via SpecType | Fully typed | None (everything user-defined) |

**Best for V-model artifact diversity:** TRLC (full type system) or Sphinx-Needs (fully configurable types)

### 4.3 Many-to-Many Tracing

| Tool | Many-to-Many Items? | Cross-Type? | Typed Links? | Attributed Links? |
|---|---|---|---|---|
| Doorstop | Yes (item level) | Yes (across docs) | No | No |
| StrictDoc | Yes | Yes | Parent/Child/File + roles | No |
| rmtoo | Limited (`Solved by`) | No | No | No |
| Sphinx-Needs | Yes | Yes | Fully custom | No |
| OpenFastTrace | Yes (Needs/Covers) | Yes (core feature) | By artifact type | No |
| TRLC | Yes (array refs) | Yes (type-checked) | By field name | Via fields on referencing type |
| ReqIF | Yes | Yes | Via SpecRelationType | Yes (relations have attributes) |

**Key finding:** Only ReqIF supports true attributed links (links that carry their own metadata). TRLC approximates this by putting link-related metadata on the referencing record. No open-source tool fully implements attributed links as a first-class concept.

### 4.4 Safety-Critical Standards Support

| Tool | DO-178C | ISO 26262 | Explicit Support |
|---|---|---|---|
| Doorstop | No | No | `derived` field is DO-178C-inspired |
| StrictDoc | Yes (technical note) | Partial | DO-178C requirements tool analysis published |
| rmtoo | No | No | None |
| Sphinx-Needs | No | Partial | Used in automotive with ASIL fields |
| OpenFastTrace | No | No | General traceability; no standard-specific features |
| TRLC + LOBSTER | No | Yes | Explicitly designed for ISO 26262 at BMW |
| Melexis ext. | No | Yes | Specifically designed for ISO 26262 |

**Best for safety-critical:** TRLC + LOBSTER (ISO 26262), StrictDoc (DO-178C awareness)

### 4.5 Git Friendliness

| Tool | Format | Files | Merge Friendliness |
|---|---|---|---|
| Doorstop | YAML | One per item | Excellent -- isolated changes |
| StrictDoc | SDoc (custom) | One per document | Good -- text diffs, but same-file conflicts |
| rmtoo | Plain text / YAML | One per requirement | Excellent -- isolated changes |
| Sphinx-Needs | RST | Embedded in docs | Fair -- needs in larger files |
| OpenFastTrace | Markdown/RST + source | Embedded | Fair -- inline in documents |
| TRLC | Custom DSL | .rsl (schema) + .trlc (data) | Good -- schema/data separation |
| ReqIF | XML | Monolithic | Poor -- XML diffs are noisy |

---

## 5. Comparison Matrix

| Feature | Doorstop | StrictDoc | rmtoo | Sphinx-Needs | OFT | TRLC+LOBSTER |
|---|---|---|---|---|---|---|
| **Language** | Python | Python | Python | Python/Sphinx | Java | Python |
| **File format** | YAML | SDoc | Plain text | RST | MD/RST | Custom DSL |
| **File-per-item** | Yes | No (per-doc) | Yes | No (embedded) | No (embedded) | Yes (.trlc) |
| **Custom fields** | Yes (YAML) | Yes (grammar) | Limited | Yes (config) | Tags only | Yes (type system) |
| **Custom types** | No | Limited | No | Yes | Convention | Yes (inheritance) |
| **Typed links** | No | Roles | No | Custom | By artifact | By field |
| **Many-to-many** | Yes | Yes | Limited | Yes | Yes | Yes |
| **Validation** | Basic | Grammar + cycles | Basic | Warnings | Coverage | Check rules |
| **Coverage analysis** | No | No | No | No | Yes (core) | Via LOBSTER |
| **ReqIF support** | No | Export/Import | No | No | No | No |
| **Web UI** | Yes (basic) | Yes | No | Via Sphinx | No | No |
| **Git-friendly** | Excellent | Good | Excellent | Fair | Fair | Good |
| **Safety standards** | Minimal | DO-178C aware | None | Automotive use | General | ISO 26262 |
| **Maturity** | Stable | Rapid dev | Stable | Mature | Mature | Active |
| **Community size** | Medium | Growing | Small | Large | Small | Small (BMW) |

---

## 6. Architectural Lessons for V-Model Traceability

### What to Adopt from Each Tool

1. **From TRLC:** Schema/data separation with `.rsl` (type definitions) and `.trlc` (instances). The type inheritance model (`abstract type`, `extends`, `final`) is excellent for defining artifact hierarchies. Check rules provide declarative validation.

2. **From OpenFastTrace:** The artifact-type-in-ID pattern (`type~name~revision`) and the Needs/Covers coverage model. The concept of "deep coverage" (transitive closure of coverage) is essential for V-model completeness.

3. **From ReqIF:** Typed, attributed links (SpecRelationType with its own fields). The GUID approach to global uniqueness. The separation of data (SpecObject) from views (Specification/SpecHierarchy).

4. **From Doorstop:** File-per-item storage in YAML for maximum git friendliness. The SHA256 fingerprint mechanism for change detection and review tracking.

5. **From Sphinx-Needs:** Fully configurable types and link types at the project level. The visualization capabilities (needflow, needtable) demonstrate what reporting should look like.

6. **From LOBSTER:** The concept of a separate traceability report generator that pulls from multiple sources. The modular extractor architecture (one tool per source type).

### Gaps No Tool Fills

1. **Review records as artifacts**: No tool treats reviews, approvals, or sign-offs as traceable artifacts with their own type definitions and links.

2. **Baseline management**: No tool provides formal baseline snapshotting with the ability to compare baselines for change impact analysis.

3. **Architecture artifacts**: No tool has built-in support for architecture decisions, interface specifications, or component models as first-class traceable items.

4. **Attributed links**: Only ReqIF supports links that carry their own metadata (e.g., a "verifies" link with a verification status, a "satisfies" link with a coverage percentage). This is critical for V-model traceability where the nature and status of a relationship matters.

5. **Bidirectional validation with completeness metrics**: While OpenFastTrace checks coverage, no tool provides comprehensive metrics like "85% of requirements have tests; these 15% do not; 3% of tests are orphaned."

6. **Multi-project / supply-chain traceability**: Only ReqIF addresses cross-organizational traceability via GUIDs. Open-source tools assume a single repository.

### Recommended Data Model for a V-Model Traceability System

Based on this research, a purpose-built system should combine:

| Concept | Inspiration | Implementation |
|---|---|---|
| **Artifact types** | TRLC type system + ReqIF SpecType | Schema files defining artifact types with fields |
| **Artifact instances** | TRLC records + Doorstop YAML | One YAML file per artifact, typed |
| **Artifact IDs** | OFT `type~name~revision` + content hash | `TYPE-name` with content-hash for change detection |
| **Link types** | ReqIF SpecRelationType + Sphinx-Needs | Named, typed links with optional attributes |
| **Link storage** | Doorstop embedded + ReqIF standalone | Embedded for primary links; separate index for cross-cutting |
| **Validation** | OFT coverage + TRLC check rules | Declarative rules checking coverage and constraints |
| **Views** | ReqIF Specification + Sphinx-Needs tables | Generated views and reports from the artifact graph |

---

## 7. Sources

- [Doorstop GitHub Repository](https://github.com/doorstop-dev/doorstop)
- [Doorstop Documentation - Item Reference](https://github.com/doorstop-dev/doorstop/blob/develop/docs/reference/item.md)
- [StrictDoc GitHub Repository](https://github.com/strictdoc-project/strictdoc)
- [StrictDoc User Guide](https://strictdoc.readthedocs.io/en/stable/stable/docs/strictdoc_01_user_guide.html)
- [StrictDoc Feature Map - Deep Traceability](https://strictdoc.readthedocs.io/en/stable/stable/docs/strictdoc_02_feature_map-DEEP-TRACE.html)
- [StrictDoc DO-178C Technical Note](https://strictdoc.readthedocs.io/en/latest/latest/docs_extra/DO178_requirements.html)
- [rmtoo GitHub Repository](https://github.com/florath/rmtoo)
- [Sphinx-Needs Website](https://www.sphinx-needs.com/)
- [Sphinx-Needs Configuration](https://sphinx-needs.readthedocs.io/en/latest/configuration.html)
- [OpenFastTrace GitHub Repository](https://github.com/itsallcode/openfasttrace)
- [OpenFastTrace User Guide](https://github.com/itsallcode/openfasttrace/blob/main/doc/user_guide.md)
- [TRLC GitHub Repository](https://github.com/bmw-software-engineering/trlc)
- [TRLC Language Reference Manual](https://bmw-software-engineering.github.io/trlc/lrm.html)
- [LOBSTER GitHub Repository](https://github.com/bmw-software-engineering/lobster)
- [Reqflow Website](https://goeb.github.io/reqflow/)
- [Melexis Sphinx Traceability Extension](https://melexis.github.io/sphinx-traceability-extension/readme.html)
- [ReqIF Specification (OMG)](https://www.omg.org/spec/ReqIF/1.2/About-ReqIF)
- [ReqIF Wikipedia](https://en.wikipedia.org/wiki/Requirements_Interchange_Format)
- [Open Source Requirements Management Tools (Gist)](https://gist.github.com/stanislaw/aa40eb7de9f522ad482e5d239c435ff8)
- [Zephyr RTOS Requirements Tool Evaluation](https://github.com/zephyrproject-rtos/zephyr/issues/57703)
